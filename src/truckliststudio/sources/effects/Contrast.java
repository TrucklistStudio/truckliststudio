/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import truckliststudio.sources.effects.controls.ContrastControl;

/**
 *
 * @author pballeux (modified by karl)
 */
public class Contrast extends Effect {

    private final com.jhlabs.image.ContrastFilter filter = new com.jhlabs.image.ContrastFilter();
    private final float ratio = 100f;
    private float brightness = 100f / ratio;
    private float contrast = 100f / ratio;

    @Override
    public void applyEffect(BufferedImage img) {
        filter.setBrightness(brightness);
        filter.setContrast(contrast);
        Graphics2D buffer = img.createGraphics();
        BufferedImage temp = filter.filter(img, null);
        buffer.setBackground(new java.awt.Color(0, 0, 0, 0));
        buffer.clearRect(0, 0, img.getWidth(), img.getHeight());
        buffer.drawImage(temp, 0, 0, null);
        buffer.dispose();
    }

    @Override
    public boolean needApply() {
        return needApply = true;
    }

    @Override
    public JPanel getControl() {
        return new ContrastControl(this);
    }

    /**
     * @return the brightness
     */
    public int getBrightness() {
        return (int) (brightness * ratio);
    }

    /**
     * @param brightness the brightness to set
     */
    public void setBrightness(int brightness) {
        this.brightness = brightness / ratio;
    }

    /**
     * @return the contrast
     */
    public int getContrast() {
        return (int) (contrast * ratio);
    }

    /**
     * @param contrast the contrast to set
     */
    public void setContrast(int contrast) {
        this.contrast = contrast / ratio;
    }

    @Override
    public void resetFX() {
        // nothing here.
    }

}
