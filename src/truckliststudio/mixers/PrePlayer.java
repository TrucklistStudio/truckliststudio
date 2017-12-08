/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.mixers;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import static truckliststudio.TrucklistStudio.audioFreq;
import truckliststudio.components.PreViewer;
import truckliststudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class PrePlayer implements Runnable {

    private static PrePlayer preInstance = null;

    public static PrePlayer getPreInstance(PreViewer viewer) {
        if (preInstance == null) {
            preInstance = new PrePlayer(viewer);
        }
        return preInstance;
    }

    boolean stopMe = false;
    public boolean stopMePub = stopMe;
    private SourceDataLine source;
    private ExecutorService executor = null;
    private final ArrayList<byte[]> buffer = new ArrayList<>();
    private FrameBuffer frames = null;
    private PreViewer preViewer = null;

    private PrePlayer(PreViewer viewer) {
        this.preViewer = viewer;
    }

    public void addFrame(Frame frame) {
        BufferedImage fImage = frame.getImage();
        preViewer.setImage(fImage);
        int lAL = PreviewMixer.getInstance().getAudioLevelLeft();
        int lAR = PreviewMixer.getInstance().getAudioLevelRight();
        preViewer.setAudioLevel(lAL, lAR);
        int liveAL = MasterMixer.getInstance().getAudioLevelLeft();
        int liveAR = MasterMixer.getInstance().getAudioLevelRight();
        preViewer.setLiveAudioLevelFX(liveAL, liveAR);
        preViewer.repaint();
    }

    public void addLiveFrame(Frame frame) {
        if (source != null) {
            frames.push(frame);
        }
    }

    public void play() throws LineUnavailableException {
        frames = new FrameBuffer(MasterMixer.getInstance().getWidth(), MasterMixer.getInstance().getHeight(), MasterMixer.getInstance().getRate());
        AudioFormat format = new AudioFormat(audioFreq, 16, 2, true, true);
        source = javax.sound.sampled.AudioSystem.getSourceDataLine(format);
        Tools.sleep(50);
        source.open();
        Tools.sleep(50);
        source.start();
        executor = java.util.concurrent.Executors.newCachedThreadPool();
        executor.submit(this);
        executor.shutdown();
    }

    @Override
    public void run() {
        stopMe = false;
        frames.clear();
        while (!stopMe) {
            Frame frame = frames.pop();
            byte[] d = frame.getAudioData();
            if (d != null) {
                source.write(d, 0, d.length);
            }
        }
    }

    public void stop() {
        stopMe = true;
        if (frames != null) {
            Tools.sleep(30);
            frames.abort();
        }
        if (source != null) {
            Tools.sleep(30);
            source.stop();
            Tools.sleep(30);
            source.close();
            Tools.sleep(30);
            source = null;
        }
        Tools.sleep(20);
        executor = null;
    }

}
