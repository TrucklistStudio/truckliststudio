/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import truckliststudio.sources.effects.controls.HSBControl;

/**
 *
 * @author pballeux (modified by karl)
 */
public class HSB extends Effect {

    private final com.jhlabs.image.HSBAdjustFilter filter = new com.jhlabs.image.HSBAdjustFilter();
    private final float ratio = 100f;
    private float hFactor = 0f / ratio;//gain
    private float sFactor = 0f / ratio;//bias
    private float bFactor = 0f / ratio;

    @Override
    public void applyEffect(BufferedImage img) {
        filter.setHFactor(hFactor);
        filter.setSFactor(sFactor);
        filter.setBFactor(bFactor);
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
        return new HSBControl(this);
    }

    /**
     * @return the brightness
     */
    public int getHFactor() {
        return (int) (hFactor * ratio);
    }

    /**
     * @param hFactor
     */
    public void setHFactor(int hFactor) {
        this.hFactor = hFactor / ratio;
    }

    /**
     * @return the contrast
     */
    public int getSFactor() {
        return (int) (sFactor * ratio);
    }

    /**
     * @param sFactor
     */
    public void setSFactor(int sFactor) {
        this.sFactor = sFactor / ratio;
    }

    public int getBFactor() {
        return (int) (bFactor * ratio);
    }

    /**
     * @param bFactor
     */
    public void setBFactor(int bFactor) {
        this.bFactor = bFactor / ratio;
    }

    @Override
    public void resetFX() {
        // nothing here.
    }
}
