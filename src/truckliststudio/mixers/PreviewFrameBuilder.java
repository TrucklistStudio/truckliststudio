/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package truckliststudio.mixers;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import truckliststudio.streams.Stream;
import truckliststudio.util.Tools;
import java.util.concurrent.CancellationException;

/**
 *
 * @author patrick (modified by karl)
 */
public class PreviewFrameBuilder implements Runnable {

    private static final ArrayList<Stream> preStreams = new ArrayList<>();// add private
    private static int fps = 0;
    private static int sRate = 0;

    public static synchronized void register(Stream s) {
        if (!preStreams.contains(s)) {
            preStreams.add(s);
//            System.out.println("Register Preview Stream Size: "+preStreams.size());
            s.setRate(PreviewMixer.getInstance().getRate());
        }
    }

    public static synchronized void unregister(Stream s) {
        preStreams.remove(s);
//        System.out.println("UnRegister Preview Stream Size: "+preStreams.size());
        s.setRate(MasterMixer.getInstance().getRate());
    }
    private Image imageF;
    private int imageX, imageY, imageW, imageH;
    private boolean stopMe = false;
    private long mark = System.currentTimeMillis();
    private FrameBuffer frameBuffer = null;
    private final TreeMap<Integer, Frame> orderedFrames = new TreeMap<>();

    public PreviewFrameBuilder(int w, int h, int r) {
        frameBuffer = new FrameBuffer(w, h, r);
    }

    public void stop() {
        stopMe = true;
    }

    private void mixImages(Collection<Frame> frames, Frame targetFrame) {
        for (Frame f : frames) {
            orderedFrames.put(f.getZOrder(), f);
        }

        BufferedImage image = targetFrame.getImage();
        if (image != null) {
            Graphics2D g = image.createGraphics();
            g.clearRect(0, 0, image.getWidth(), image.getHeight());
            final float dash1[] = {10.0f};
            final BasicStroke dashed
                    = new BasicStroke(5.0f,
                            BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_MITER,
                            10.0f, dash1, 0.0f);
            g.setStroke(dashed);
            g.draw(new RoundRectangle2D.Double(0, 0,
                    image.getWidth() / 2,
                    image.getHeight(),
                    10, 10));
            g.draw(new RoundRectangle2D.Double(image.getWidth() / 2, 0,
                    image.getWidth() / 2,
                    image.getHeight(),
                    10, 10));
            g.draw(new RoundRectangle2D.Double(0, 0,
                    image.getWidth(),
                    image.getHeight() / 2,
                    10, 10));
            for (Frame f : orderedFrames.values()) {
                imageF = f.getImage();
                imageX = f.getX();
                imageY = f.getY();
                imageW = f.getWidth();
                imageH = f.getHeight();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f.getOpacity() / 100F));
                g.drawImage(imageF, imageX, imageY, imageW, imageH, null);
            }
            g.dispose();
        }
        orderedFrames.clear();
    }

    private void mixAudio(Collection<Frame> frames, Frame targetFrame) {
        byte[] audioData = targetFrame.getAudioData();
        ShortBuffer outputBuffer = ByteBuffer.wrap(audioData).asShortBuffer();
        for (int i = 0; i < audioData.length; i++) {
            audioData[i] = 0;
        }
        for (Frame f : frames) {
            byte[] data = f.getAudioData();
            if (data != null) {
                ShortBuffer buffer = ByteBuffer.wrap(data).asShortBuffer();
                outputBuffer.rewind();
                while (buffer.hasRemaining()) {
                    float mix = buffer.get() * f.getVolume();
                    outputBuffer.mark();
                    if (outputBuffer.position() < outputBuffer.limit()) { //25fps IOException                     
                        mix += outputBuffer.get();
                    }
                    outputBuffer.reset();
                    if (mix > Short.MAX_VALUE) {
                        mix = Short.MAX_VALUE;
                    } else if (mix < Short.MIN_VALUE) {
                        mix = Short.MIN_VALUE;
                    }
                    if (outputBuffer.position() < outputBuffer.limit()) { //25fps IOException                          
                        outputBuffer.put((short) mix);
                    }
                }
                f.setAudio(null);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() throws NullPointerException {
        stopMe = false;
        ArrayList<Frame> frames = new ArrayList<>();
        mark = System.currentTimeMillis();
        int r = PreviewMixer.getInstance().getRate();
        long frameDelay = 1000 / r;
        long timeCode = System.currentTimeMillis();
        while (!stopMe) {
            timeCode += frameDelay;
            Frame targetFrame = frameBuffer.getFrameToUpdate();
            frames.clear();
            boolean threadedCaptureMode = true;
            ExecutorService pool = java.util.concurrent.Executors.newCachedThreadPool();
            if (threadedCaptureMode) {
                ArrayList<Future<Frame>> resultsT = new ArrayList<>();

                try {
                    resultsT = ((ArrayList) pool.invokeAll(preStreams, 5, TimeUnit.SECONDS));
                } catch (InterruptedException ex) {
                    Logger.getLogger(MasterFrameBuilder.class.getName()).log(Level.SEVERE, null, ex);
                }
                ArrayList<Future<Frame>> results = resultsT;
                Frame f;
                for (Future stream : results) {
                    try {
                        f = (Frame) stream.get();

                        if (f != null) {
                            frames.add(f);
                        }
                    } catch (CancellationException | InterruptedException | ExecutionException ex) {
                        Logger.getLogger(MasterFrameBuilder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                for (int i = 0; i < preStreams.size(); i++) {
                    Frame f;

                    try {
                        Stream s = preStreams.get(i);
                        f = s.call();
                        if (f != null) {
                            frames.add(f);
                        }
                    } catch (Exception e) {
                    }
                }
            }
            long now = System.currentTimeMillis();
            long sleepTime = (timeCode - now);
            fps++;
            mixAudio(frames, targetFrame);
            mixImages(frames, targetFrame);
            targetFrame = null;
            frameBuffer.doneUpdate();
            PreviewMixer.getInstance().setCurrentFrame(frameBuffer.pop());
            float delta = (now - mark);
            if (delta >= 1000) {
                mark = now;
                PreviewMixer.getInstance().setFPS(fps / (delta / 1000F));
                fps = 0;
            }
            if (sleepTime > 0) {
                Tools.sleep(sleepTime);
            }
        }
    }
}
