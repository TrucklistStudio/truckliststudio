/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.sources.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import truckliststudio.mixers.MasterMixer;
import truckliststudio.sources.effects.controls.CropControl;

/**
 *
 * @author pballeux (modified by karl)
 */
public class Crop extends Effect {

    private final com.jhlabs.image.CropFilter filter = new com.jhlabs.image.CropFilter();
    private int x = 0;
    private int y = 0;
    private int width = MasterMixer.getInstance().getWidth();
    private int height = MasterMixer.getInstance().getHeight();

    @Override
    public void applyEffect(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        filter.setX(x);
        filter.setY(y);
        filter.setWidth(width);
        filter.setHeight(height);
        Graphics2D buffer = img.createGraphics();
        BufferedImage temp = filter.filter(img, null);
        buffer.setBackground(new Color(0, 0, 0, 0));
        buffer.clearRect(0, 0, w, h);
        buffer.drawImage(temp, 0, 0, null);
        buffer.dispose();
    }

    @Override
    public JPanel getControl() {
        return new CropControl(this);
    }

    @Override
    public boolean needApply() {
        return needApply = true;
    }

    /**
     * @return the x1
     */
    public int getX() {
        return x;
    }

    /**
     * @param x1 the x1 to set
     */
    public void setX(int x1) {
        this.x = x1;
    }

    /**
     * @return the y1
     */
    public int getY() {
        return y;
    }

    /**
     * @param y1 the y1 to set
     */
    public void setY(int y1) {
        this.y = y1;
    }

    /**
     * @return the x3
     */
    public float getWidth() {
        return width;
    }

    /**
     * @param width1
     */
    public void setWidth(int width1) {
        this.width = width1;
    }

    public int getHeight() {
        return height;
    }

    /**
     * @param height1
     */
    public void setHeight(int height1) {
        this.height = height1;
    }

    @Override
    public void resetFX() {
        width = MasterMixer.getInstance().getWidth();
        height = MasterMixer.getInstance().getHeight();
    }
}
