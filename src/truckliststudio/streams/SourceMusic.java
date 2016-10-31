/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import static truckliststudio.TrucklistStudio.gsNLE;
import static truckliststudio.TrucklistStudio.selColLbl2;
import truckliststudio.components.ResourceMonitor;
import truckliststudio.components.ResourceMonitorLabel;
import truckliststudio.externals.ProcessRenderer;
import truckliststudio.mixers.Frame;
import truckliststudio.mixers.MasterFrameBuilder;
import truckliststudio.mixers.MasterMixer;
import truckliststudio.mixers.PreviewFrameBuilder;
import truckliststudio.sources.effects.Effect;

/**
 *
 * @author patrick (modified by karl)
 */
public class SourceMusic extends Stream {

    ProcessRenderer capture = null;
    BufferedImage lastPreview = null;
    boolean isPlaying = false;

    public SourceMusic(File music) {
        super();
        rate = MasterMixer.getInstance().getRate();
        file = music;
        name = music.getName();

    }

    @Override
    public void read() {
        if (!this.getFile().exists()) {
            String sName = this.getName();
            if (sName.length() > 25) {
                sName = sName.substring(0, 25) + " ...";
            }
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 7000, "<html>&nbsp;<font color=red>WARNING !!!</font> File <font color=" + selColLbl2 + ">\"" + sName + "\"</font> not found !!!</html>");
            ResourceMonitor.getInstance().addMessage(label);
        }
        isPlaying = true;
        if (getPreView()) {
            PreviewFrameBuilder.register(this);
        } else {
            MasterFrameBuilder.register(this);
        }
        lastPreview = new BufferedImage(captureWidth, captureHeight, BufferedImage.TYPE_INT_ARGB);
        if (gsNLE) {
            capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "nlemusic", comm);
        } else {
            capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "music", comm);
        }
        capture.read();
    }

    @Override
    public void pause() {
        isPaused = true;
        capture.pause();
    }

    @Override
    public void stop() {
        if (loop) {
            if (capture != null) {
                capture.stop();
                capture = null;
            }
            if (this.getBackFF()) {
                this.setComm("FF");
            }
            this.read();
        } else {
            for (int fx = 0; fx < this.getEffects().size(); fx++) {
                Effect fxT = this.getEffects().get(fx);
                if (fxT.getName().endsWith("Stretch") || fxT.getName().endsWith("Crop")) {
                    // do nothing.
                } else {
                    fxT.resetFX();
                }
            }
            isPlaying = false;
            if (getPreView()) {
                PreviewFrameBuilder.unregister(this);
            } else {
                MasterFrameBuilder.unregister(this);
            }
            if (capture != null) {
                capture.stop();
                capture = null;
            }
            if (this.getBackFF()) {
                this.setComm("FF");
            }
        }

    }

    @Override
    public boolean needSeek() {
        if (this.getComm().equals("GS")) {
         needSeekCTRL = true;
        } else {
         needSeekCTRL = false;
        }
        return needSeekCTRL;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void setIsPlaying(boolean setIsPlaying) {
        isPlaying = setIsPlaying;
    }

    @Override
    public BufferedImage getPreview() {
        return lastPreview;
    }

    @Override
    public Frame getFrame() {

        return nextFrame;
    }

    @Override
    public boolean hasFakeVideo() {
        return true;
    }

    @Override
    public boolean hasFakeAudio() {
        return true;
    }

    @Override
    public boolean hasAudio() {
        return true;
    }

    @Override
    public boolean hasVideo() {
        return true;
    }

    @Override
    public void readNext() {
        Frame f = null;
        if (capture != null) {
            f = capture.getFrame();
            if (f != null) {
                BufferedImage img = f.getImage();
                applyEffects(img);
            }
            if (f != null) {
                setAudioLevel(f);
                lastPreview.getGraphics().drawImage(f.getImage(), 0, 0, null);
            }
        }
        nextFrame = f;
    }

    @Override
    public void play() {
        isPaused = false;
        capture.play();
    }

    @Override
    public void setNeedSeek(boolean seek) {
        needSeekCTRL = seek;
    }
}
