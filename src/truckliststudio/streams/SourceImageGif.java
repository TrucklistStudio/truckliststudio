/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import static truckliststudio.TrucklistStudio.selColLbl2;
import truckliststudio.components.GifDecoder;
import truckliststudio.components.ResourceMonitor;
import truckliststudio.components.ResourceMonitorLabel;
import truckliststudio.mixers.Frame;
import truckliststudio.mixers.MasterFrameBuilder;
import truckliststudio.mixers.PreviewFrameBuilder;
import truckliststudio.util.Tools;

/**
 *
 * @author patrick
 */
public class SourceImageGif extends Stream {

    BufferedImage image = null;
    boolean playing = false;
    boolean stop = true;
    Frame frame = null;
    GifDecoder decoder = new GifDecoder();
    int index = 0;

    public SourceImageGif(File img) {
        super();
        file = img;
        name = img.getName();
    }

    public SourceImageGif(String name, URL url) {
        this.url = url.toString();
        this.name = name;
    }

    private void loadImage() throws IOException {
        if (file != null) {
            decoder.read(file.toURI().toURL().openStream());
        } else if (url != null) {
            decoder.read(new URL(url).openStream());
        }
        image = new BufferedImage(decoder.getImage().getWidth(), decoder.getImage().getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        new Thread(new Runnable() {

            @Override
            public void run() {
                updateImage();
            }
        }).start();
    }

    private void updateImage() {
        if (decoder != null) {
            while (!stop) {
                index++;
                if (index >= decoder.getFrameCount()) {
                    index = 0;
                }
                Tools.sleep(decoder.getDelay(index));
            }
        }
    }

    @Override
    public void read() {
        if (file != null) {
            if (!file.exists()) {
                String sName = this.getName();
                if (sName.length() > 25) {
                    sName = sName.substring(0, 25) + " ...";
                }
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 9000, "<html>&nbsp;<font color=red>WARNING !!!</font> File <font color=" + selColLbl2 + ">\"" + sName + "\"</font> not found !!!</html>");
                ResourceMonitor.getInstance().addMessage(label);
            }
        }
        playing = true;
        stop = false;
        try {
            loadImage();
            frame = new Frame(uuid, image, null);
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
            if (getPreView()) {
                PreviewFrameBuilder.register(this);
            } else {
                MasterFrameBuilder.register(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        // nothing here.
    }

    @Override
    public void stop() {
        playing = false;
        stop = true;
        if (getPreView()) {
            PreviewFrameBuilder.unregister(this);
        } else {
            MasterFrameBuilder.unregister(this);
        }
    }

    @Override
    public Frame getFrame() {
        return nextFrame;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void setIsPlaying(boolean setIsPlaying) {
        playing = setIsPlaying;
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

    @Override
    public void readNext() {
        image = decoder.getFrame(index);
        frame = new Frame(uuid, image, null);
        if (frame != null) {
            frame.setOutputFormat(x, y, width, height, opacity, volume);
            frame.setZOrder(zorder);
        }
        nextFrame = frame;
    }

    @Override
    public void play() {
        // nothing Here.
    }

}
