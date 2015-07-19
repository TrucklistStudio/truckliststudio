/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.streams;

import java.awt.image.BufferedImage;
import truckliststudio.externals.ProcessRenderer;
import truckliststudio.mixers.Frame;
import truckliststudio.mixers.MasterFrameBuilder;
import truckliststudio.mixers.MasterMixer;
import truckliststudio.mixers.PreviewFrameBuilder;
import truckliststudio.sources.effects.Effect;

/**
 *
 * @author karl
 */
public class SourceAudioSource extends Stream {

    ProcessRenderer capture = null;
    BufferedImage lastPreview = null;
    boolean isPlaying = false;

    public SourceAudioSource() {
        super();
        rate = MasterMixer.getInstance().getRate();
        name = "AudioSource";

    }

    @Override
    public void read() {
        if (getPreView()){
            PreviewFrameBuilder.register(this);
        } else {
            MasterFrameBuilder.register(this);
        }
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.CAPTURE, "audiosource", comm);
        capture.read();
        lastPreview = new BufferedImage(captureWidth,captureHeight,BufferedImage.TYPE_INT_ARGB);
        isPlaying = true;
    }
    
    @Override
    public void pause() {
        capture.pause();
    }
    
    @Override
    public void stop() {
        for (int fx = 0; fx < this.getEffects().size(); fx++) {
            Effect fxT = this.getEffects().get(fx);
            if (fxT.getName().endsWith("Stretch") || fxT.getName().endsWith("Crop")) {
                // do nothing.
            } else {
                fxT.resetFX();
            }
        }
        isPlaying = false;
        if (getPreView()){
            PreviewFrameBuilder.unregister(this);
        } else {
            MasterFrameBuilder.unregister(this);
        }
        if (capture != null) {
            capture.stop();
            capture = null;
        }
        if (this.getBackFF()){
            this.setComm("FF");
        }

    }
    
    @Override
    public boolean needSeek() {
            return needSeekCTRL=false;
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
    public boolean hasFakeVideo(){
        return true;
    }
    
    @Override
    public boolean hasFakeAudio(){
        return true;
    }
    
    @Override
    public boolean hasAudio() {
        return this.hasAudio;
    }

    @Override
    public boolean hasVideo() {
        return !this.isOnlyAudio();
    }
    
    @Override
    public void readNext() {
        Frame f = null;
        if (capture != null) {
            f = capture.getFrameC();
            if (f != null) {
                BufferedImage img = f.getImage(); 
                applyEffects(img);
            }
            if (f != null) {
                setAudioLevel(f);
                lastPreview.getGraphics().drawImage(f.getImage(), 0, 0, null);
            }
        }
        nextFrame=f;
    }

    @Override
    public void play() {
        capture.play();
    }
}
