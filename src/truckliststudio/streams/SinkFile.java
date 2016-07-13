/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import static truckliststudio.TrucklistStudio.os;
import static truckliststudio.TrucklistStudio.outFMEbe;
import static truckliststudio.TrucklistStudio.x64;
import truckliststudio.externals.ProcessRenderer;
import truckliststudio.mixers.Frame;
import truckliststudio.mixers.MasterMixer;
import truckliststudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class SinkFile extends Stream {

    private ProcessRenderer capture = null;
//    protected String abitrate = "128";
//    protected String vbitrate = "1200";
    

    public SinkFile(File f) {
        file = f;
        name = f.getName();
        if (outFMEbe == 0){
            this.setComm("FF");
        } else if (outFMEbe == 1) {
            this.setComm("AV");
        } else if (outFMEbe == 2) {
            this.setComm("GS");
        }
    }

    @Override
    public void read() {
        rate = MasterMixer.getInstance().getRate();
        captureWidth = MasterMixer.getInstance().getWidth();
        captureHeight = MasterMixer.getInstance().getHeight();
        String plugin = "file";
        if (os == Tools.OS.WINDOWS){
            if  (!x64){
                plugin = "file86";
            }
        }
        capture = new ProcessRenderer(this, ProcessRenderer.ACTION.OUTPUT, plugin, comm);
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
        return true;
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
