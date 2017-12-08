/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.streams;

import java.awt.image.BufferedImage;
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

            if (standard.equals("STD")) {
                capture = new ProcessRenderer(this, fme, plugin);
            } else {
                capture = new ProcessRenderer(this, fme, pluginHD);
            }
        } else {
            String plugin = "broadcast";
            String pluginHD = "broadcastHQ";
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
