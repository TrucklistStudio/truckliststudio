/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.streams;

import java.awt.image.BufferedImage;
import static truckliststudio.TrucklistStudio.os;
import static truckliststudio.TrucklistStudio.outFMEbe;
import static truckliststudio.TrucklistStudio.x64;
import truckliststudio.externals.ProcessRenderer;
import truckliststudio.mixers.Frame;
import truckliststudio.util.Tools;

/**
 *
 * @author karl
 */
public class SinkAudio extends Stream {

    private ProcessRenderer capture = null;

    public SinkAudio() {
        name = "AudioOut";
        this.setOnlyAudio(true);
//        System.out.println("SinkAudio outFMEbe= "+outFMEbe);
        if (outFMEbe == 0){
            this.setComm("FF");
        } else if (outFMEbe == 1) {
            this.setComm("AV");
        } else if (outFMEbe == 2) {
            this.setComm("GS");
        }
//        System.out.println("SinkAudio BE= "+this.getComm());
    }

    @Override
    public void read() {
        String plugin = "spkAudioOut";
        if (os == Tools.OS.WINDOWS){
            if  (!x64){
                plugin = "spkAudioOut86";
            }
        }
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.OUTPUT, plugin, comm); //"spkAudioOut"
        capture.writeCom();
    }

    @Override
    public void pause() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void stop() {
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

    @Override
    public void setNeedSeek(boolean seek) {
        // Nothing here
    }
}
