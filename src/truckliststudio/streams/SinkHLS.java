/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.streams;

import java.awt.image.BufferedImage;
import truckliststudio.externals.ProcessRenderer;
import truckliststudio.mixers.Frame;
import truckliststudio.mixers.MasterMixer;

/**
 *
 * @author karl
 */
public class SinkHLS extends Stream {

    private ProcessRenderer capture = null;
    private String standard = "STD";

    public SinkHLS() {
        name = "HLS";
    }

    @Override
    public void read() {
        rate = MasterMixer.getInstance().getRate();
        captureWidth = MasterMixer.getInstance().getWidth();
        captureHeight = MasterMixer.getInstance().getHeight();
        String plugin = "hls";
        String pluginHD = "hlsHQ";
        if (standard.equals("STD")) {
            capture = new ProcessRenderer(this, ProcessRenderer.ACTION.OUTPUT, plugin);
        } else {
            capture = new ProcessRenderer(this, ProcessRenderer.ACTION.OUTPUT, pluginHD);
        }
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
    
    public void setStandard(String gStandard) {
        standard = gStandard;
    }
    
    public String getStandard() {
        return standard;
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
        return true;
    }

    @Override
    public void readNext() {
        
    }

    @Override
    public void play() {
    }

}
