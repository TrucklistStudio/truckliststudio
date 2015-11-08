/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package truckliststudio.mixers;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.Executors.newCachedThreadPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import static truckliststudio.components.MasterPanel.masterVolume;
import truckliststudio.streams.Stream;
import static truckliststudio.util.Tools.sleep;

/**
 *
 * @author patrick (modified by karl)
 */
public class MasterFrameBuilder implements Runnable {

    private static final ArrayList<Stream> streams = new ArrayList<>();// add private
    private static int fps = 0;
    public static synchronized void register(Stream s) {
        if (!streams.contains(s)) {
            streams.add(s);
//            System.out.println("Register Master Stream Size: "+streams.size());
        }
    }

    public static synchronized void unregister(Stream s) {
        streams.remove(s);
//        System.out.println("UnRegister Master Stream Size: "+streams.size());
    }
    private Image imageF;
    private int imageX, imageY, imageW, imageH;
    private boolean stopMe = false;
    private long mark = System.currentTimeMillis();
    private FrameBuffer frameBuffer = null;
    private final TreeMap<Integer, Frame> orderedFrames = new TreeMap<>();

    public MasterFrameBuilder(int w, int h, int r) {
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
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            g.clearRect(0, 0, image.getWidth(), image.getHeight());
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
                    //                    System.out.println("Volume="+f.getVolume());
                    float volume = f.getVolume() + masterVolume;
                    if (volume < 0) {
                        volume = 0;
                    }
                    float mix = buffer.get() * (volume);
                    outputBuffer.mark();
                    if (outputBuffer.position() < outputBuffer.limit()){ //25fps IOException
                        mix += outputBuffer.get();
                    }
                    outputBuffer.reset();
                    if (mix > Short.MAX_VALUE) {
                        mix = Short.MAX_VALUE;
                    } else if (mix < Short.MIN_VALUE) {
                        mix = Short.MIN_VALUE;
                    }
                    if (outputBuffer.position() < outputBuffer.limit()){ //25fps IOException
                        outputBuffer.put((short) mix);
                    }
                }
                f.setAudio(null);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() throws NullPointerException{
        stopMe = false;
        ArrayList<Frame> frames = new ArrayList<>();
        mark = System.currentTimeMillis();
        int r = MasterMixer.getInstance().getRate();
        long frameDelay = 1000 / r;
        long timeCode = System.currentTimeMillis();
//        long frameNum = 0;
        while (!stopMe) {
            timeCode += frameDelay;
            Frame targetFrame = frameBuffer.getFrameToUpdate();
            frames.clear();
//            long captureTime = 0;
//            long captureStartTime = System.nanoTime();
            // threaded capture mode runs frame capture for each source in a different thread
            // In principle it should be better but the overhead of threading appears to be more trouble than it's worth.
            boolean threadedCaptureMode = true;
            ExecutorService pool = newCachedThreadPool();
            if (threadedCaptureMode) {
                ArrayList<Future<Frame>> resultsT = new ArrayList<>();
            try {
                    resultsT = ((ArrayList)pool.invokeAll(streams, 5, TimeUnit.SECONDS));
                } catch (InterruptedException ex) {
                    Logger.getLogger(MasterFrameBuilder.class.getName()).log(Level.SEVERE, null, ex);
                }
                ArrayList<Future<Frame>> results = resultsT;
//                int i=0;
                Frame f;
                for (Future stream : results) {
                    try {
                        f = (Frame)stream.get();
                        if (f != null) {
                            frames.add(f);
                        }
                    } catch (CancellationException | InterruptedException | ExecutionException ex) {
//                        Logger.getLogger(MasterFrameBuilder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                for (int i = 0; i < streams.size(); i++) {
                    Frame f;
                    try {
                        Stream s = streams.get(i);
                        f = s.call();
                        // Due to race conditions when sources start up, a source may not really be ready to operate by the time it's active in MasterFrameBuilder. (Ultimately that should probably be fixed)
                        // For that reason, we guard against (f == null) here, so streams
                        if (f != null) {
                            frames.add(f);
                        }
                    }
                    catch (Exception e)
                    {}
                }
            }
            long now = System.currentTimeMillis();
//            captureTime = (now - captureStartTime);
            long sleepTime = (timeCode - now);
            // Drop frames if we're running behind - but no more than half of them
//            if ((sleepTime > 0) || ((frameNum % 2) != 0)) {
                fps++;

                mixAudio(frames, targetFrame);
                mixImages(frames, targetFrame);
                targetFrame = null;
                frameBuffer.doneUpdate();
                MasterMixer.getInstance().setCurrentFrame(frameBuffer.pop());
//            }

            float delta = (now - mark);
                if (delta >= 1000) {
                mark = now;
                    MasterMixer.getInstance().setFPS(fps / (delta / 1000F));
                    fps = 0;
                }
                if (sleepTime > 0) {
                sleep(sleepTime);
                }

//            frameNum++;
            }
        }
    }
