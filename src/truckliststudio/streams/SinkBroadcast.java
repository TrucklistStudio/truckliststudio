/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.streams;

import java.awt.image.BufferedImage;
import static truckliststudio.TrucklistStudio.os;
import static truckliststudio.TrucklistStudio.outFMEbe;
import static truckliststudio.TrucklistStudio.x64;
import truckliststudio.externals.FME;
import truckliststudio.externals.ProcessRenderer;
import truckliststudio.mixers.MasterMixer;
import truckliststudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class SinkBroadcast extends Stream {

    private ProcessRenderer capture = null;
    private FME fme = null;
    private boolean isPlaying = false;
    private String standard = "STD";
    public SinkBroadcast(FME fme) {
        this.fme=fme;
        name=fme.getName();
        url = fme.getUrl()+"/"+fme.getStream();
        if (outFMEbe == 0){
            this.setComm("FF");
        } else if (outFMEbe == 1) {
            this.setComm("AV");
        } else if (outFMEbe == 2) {
            this.setComm("GS");
        }
    }
    @Override
    public String getName(){
        return name;
    }
    @Override
    public void read() {
        isPlaying=true;
        rate = MasterMixer.getInstance().getRate();
        captureWidth = MasterMixer.getInstance().getWidth();
        captureHeight = MasterMixer.getInstance().getHeight();
        if (!"".equals(this.fme.getMount())) {
            String plugin = "iceCast";
            String pluginHD = "iceCastHQ";
            if (os == Tools.OS.WINDOWS){
                if  (!x64){
                    plugin = "iceCast86";
                    pluginHD = "iceCastHQ86";
                }
            }
            if (standard.equals("STD")) {
                capture = new ProcessRenderer(this, fme, plugin);
            } else {
                capture = new ProcessRenderer(this, fme, pluginHD);
            }
        } else {
            String plugin = "broadcast";
            String pluginHD = "broadcastHQ";
            if (os == Tools.OS.WINDOWS){
                if  (!x64){
                    plugin = "broadcast86";
                    pluginHD = "broadcastHQ86";
                }
            }
            if (standard.equals("STD")) {
                capture = new ProcessRenderer(this, fme, plugin);
            } else {
                capture = new ProcessRenderer(this, fme, pluginHD);
            }
        }
        capture.writeCom();
    }

    @Override
    public void pause() {
        // nothing here.
    }
    
    @Override
    public void stop() {
        isPlaying=false;
        if  (capture!=null){
            capture.stop();
            capture=null;
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
    public boolean hasAudio() {
        return true;
    }

    @Override
    public boolean hasVideo() {
        return true;
    }

    @Override
    public void readNext() {
        // nothing here.
    }

    @Override
    public void play() {
        // nothing here.
    }

    @Override
    public void setNeedSeek(boolean seek) {
        // Nothing here
    }
}
