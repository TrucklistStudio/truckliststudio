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
import truckliststudio.mixers.MasterMixer;
import truckliststudio.util.Tools;

/**
 *
 * @author karl
 */
public class SinkUDP extends Stream {

    private ProcessRenderer capture = null;
    private String standard = "STD";

    public SinkUDP() {
        name = "UDP";
//        System.out.println("SinkUDP outFMEbe= "+outFMEbe);
//        System.out.println("Making UDP");
        if (outFMEbe == 0){
            this.setComm("FF");
        } else if (outFMEbe == 1) {
            this.setComm("AV");
        } else if (outFMEbe == 2) {
            this.setComm("GS");
        }
//        System.out.println("SinkUDP BE= "+this.getComm());
    }

    @Override
    public void read() {
        rate = MasterMixer.getInstance().getRate();
        captureWidth = MasterMixer.getInstance().getWidth();
        captureHeight = MasterMixer.getInstance().getHeight();
        String plugin = "udp";
        String pluginHD = "udpHQ";
        if (os == Tools.OS.WINDOWS){
            if  (!x64){
                plugin = "udp86";
                pluginHD = "udpHQ86";
            }
        }
        if (standard.equals("STD")) {
            capture = new ProcessRenderer(this, ProcessRenderer.ACTION.OUTPUT, plugin, comm);
        } else {
            capture = new ProcessRenderer(this, ProcessRenderer.ACTION.OUTPUT, pluginHD, comm);
        }
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
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setNeedSeek(boolean seek) {
        // Nothing here
    }
}
