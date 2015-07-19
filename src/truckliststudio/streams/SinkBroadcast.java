/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.streams;

import java.awt.image.BufferedImage;
import static truckliststudio.TrucklistStudio.outFMEbe;
import truckliststudio.externals.FME;
import truckliststudio.externals.ProcessRenderer;
import truckliststudio.mixers.MasterMixer;

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
            if (standard.equals("STD")) {
                capture = new ProcessRenderer(this,fme,"iceCast");
            } else {
                capture = new ProcessRenderer(this,fme,"iceCastHQ");
            }
        } else {
            if (standard.equals("STD")) {
                capture = new ProcessRenderer(this,fme,"broadcast");
            } else {
                capture = new ProcessRenderer(this,fme,"broadcastHQ");
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
}
