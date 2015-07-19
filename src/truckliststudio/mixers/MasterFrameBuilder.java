/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package truckliststudio.mixers;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
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
import static truckliststudio.components.MasterPanel.masterVolume;
import truckliststudio.streams.Stream;
import truckliststudio.util.Tools;

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
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_SPEED);
            g.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS,java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            g.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_DISABLE);
            g.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED);
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
//                    float mix = buffer.get() * f.getVolume();
                    outputBuffer.mark();
                    if (outputBuffer.position()< outputBuffer.limit()){ //25fps IOException                     
                        mix += outputBuffer.get();
                    }
                    outputBuffer.reset();
                    if (mix > Short.MAX_VALUE) {
                        mix = Short.MAX_VALUE;
                    } else if (mix < Short.MIN_VALUE) {
                        mix = Short.MIN_VALUE;
                    }
                    if (outputBuffer.position()< outputBuffer.limit()){ //25fps IOException                          
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
        ArrayList<Future<Frame>> resultsT = new ArrayList<>();
        mark = System.currentTimeMillis();
        int r = MasterMixer.getInstance().getRate();
        long frameDelay = 1000 / r;
        long timeCode = System.currentTimeMillis();
        ExecutorService pool = java.util.concurrent.Executors.newCachedThreadPool();
        while (!stopMe) {
            timeCode += frameDelay;
            Frame targetFrame = frameBuffer.getFrameToUpdate();
            frames.clear();
            try {
                resultsT = ((ArrayList) pool.invokeAll(streams, 5, TimeUnit.SECONDS)); //modify to 10 give more time to pause
                ArrayList<Future<Frame>> results = resultsT;
                int i=0;
                Frame f;
                for (Future stream : results) {
                    if ((Frame)stream.get() != null) {
                        if (!streams.isEmpty()) {
                            f = (Frame)stream.get();
                            frames.add(f);
                        }
                    }
                    i++;
                }
                mixAudio(frames, targetFrame);
                mixImages(frames, targetFrame);
                targetFrame = null;
                frameBuffer.doneUpdate();
                MasterMixer.getInstance().setCurrentFrame(frameBuffer.pop());
                fps++;
                float delta = System.currentTimeMillis() - mark;
                if (delta >= 1000) {
                    mark = System.currentTimeMillis();
                    MasterMixer.getInstance().setFPS(fps / (delta / 1000F));
                    fps = 0;
                }
                long sleepTime = timeCode - System.currentTimeMillis();
                if (sleepTime > 0) {
                    Tools.sleep(sleepTime + 10);
                }
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(MasterFrameBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
