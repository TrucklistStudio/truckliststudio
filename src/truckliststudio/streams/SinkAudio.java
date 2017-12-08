/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.streams;

import java.awt.image.BufferedImage;
import truckliststudio.externals.ProcessRenderer;
import truckliststudio.mixers.Frame;

/**
 *
 * @author karl
 */
public class SinkAudio extends Stream {

    private ProcessRenderer capture = null;

    public SinkAudio() {
        name = "AudioOut";
        this.setOnlyAudio(true);
    }

    @Override
    public void read() {
        String plugin = "spkAudioOut";
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.OUTPUT, plugin); //"spkAudioOut"
        capture.writeCom();
    }

    @Override
    public void pause() {
    }
    
    @Override
    public void stop() {
        if (capture != null) {
            capture.stop();
            capture = null;
        }
    }

    @Override
    public boolean isPlaying() {
        if (capture != null) {
            return !capture.isStopped();
        } else {
            return false;
        }
    }

    @Override
    public BufferedImage getPreview() {
        return null;
    }
    
    @Override
    public Frame getFrame() {
        return null;
    }

    @Override
    public boolean hasAudio() {
        return true;
    }

    @Override
    public boolean hasVideo() {
        return false;
    }

    @Override
    public void readNext() {
        
    }

    @Override
    public void play() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
