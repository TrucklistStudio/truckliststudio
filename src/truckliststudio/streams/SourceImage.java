/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.streams;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;
import static truckliststudio.TrucklistStudio.selColLbl2;
import truckliststudio.components.ResourceMonitor;
import truckliststudio.components.ResourceMonitorLabel;
import truckliststudio.mixers.Frame;
import truckliststudio.mixers.MasterFrameBuilder;
import truckliststudio.mixers.MasterMixer;
import truckliststudio.mixers.PreviewFrameBuilder;
import truckliststudio.sources.effects.Effect;

/**
 *
 * @author patrick
 */
public class SourceImage extends Stream{

    BufferedImage image = null;
    BufferedImage bkImage = null;
    boolean isPlaying = false;
    Frame frame = null;
    private final MasterMixer mixer = MasterMixer.getInstance();
    private int imgCW = mixer.getWidth();
    private int imgCH = mixer.getHeight();
    
    public SourceImage(File img){
        super();
        file = img;
        name = img.getName();
    }
    
    private void loadImage(File f) throws IOException{
        BufferedImage capImg = ImageIO.read(f);
        bkImage = ImageIO.read(f);
        image = Scalr.resize(capImg, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, width, height);
        captureWidth = image.getWidth();
        captureHeight = image.getHeight();
    }
    
    @Override
    public void read() {
        if (!this.getFile().exists()) {
            String sName = this.getName();
            if (sName.length() > 25) {
                sName = sName.substring(0, 25) + " ...";
            }
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "<html>&nbsp;<font color=red>WARNING !!!</font> File <font color=" + selColLbl2 + ">\"" + sName + "\"</font> not found !!!</html>");
            ResourceMonitor.getInstance().addMessage(label);
        }
        isPlaying = true;   
        try{
            loadImage(file);
            frame = new Frame(captureWidth,captureHeight,rate);
            frame.setImage(image);
            frame.setAudio(null);
            frame.setID(uuid);
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
            if (getPreView()){
                PreviewFrameBuilder.register(this);
            } else {
                MasterFrameBuilder.register(this);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        // do nothing.
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
        frame = null;
        if (getPreView()){
            PreviewFrameBuilder.unregister(this);
        } else {
            MasterFrameBuilder.unregister(this);
        }
    }

    @Override
    public Frame getFrame(){
        return nextFrame;
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
        return image;
    }

    @Override
    public boolean hasAudio() {
        return false;
    }

    @Override
    public boolean hasVideo() {
        return true;
    }
    
    /**
     *
     */
    @Override
    public void readNext() {
        frame.setImage(image);
        if (isPlaying) {
            BufferedImage img = frame.getImage(); 
            applyEffects(img);
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
        }
        nextFrame=frame;
    }
    
    @Override
    public void updatePNG() {
//        System.out.println("updatePNG !!!");
        captureWidth = width;
        captureHeight = height;
        image = Scalr.resize(bkImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, width, height);
        frame = new Frame(captureWidth,captureHeight,rate);
        frame.setImage(image);
    }
    
    public void setImgCW (int tCW) {
        imgCW = tCW;
    }
    
    public int getImgCW(){
        return imgCW;
    }
    
    public void setImgCH(int tCH) {
        imgCH = tCH;
    }
    
    public int getImgCH(){
        return imgCH;
    }
    
    @Override
    public void setWidth(int w) {
        if (w < 1){
            w = 1;
        }
        width = w;
        if (this.isPlaying)
        updatePNG();
//        System.out.println("W set ... "+w);
    }
    
    @Override
    public void setHeight(int h) {
        if (h < 1){
            h = 1;
        }
        height = h;
        if (this.isPlaying)
            updatePNG();
//        System.out.println("Set " + this.getName() + " To = " + this.height);
//        System.out.println("H set ... "+h);
    }
    
    @Override
    public void play() {
        // nothing here.
    }

}
