/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package truckliststudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import truckliststudio.sources.effects.controls.GainControl;

/**
 *
 * @author pballeux (modified by karl)
 */
public class Gain extends Effect{
    private final com.jhlabs.image.GainFilter filter = new com.jhlabs.image.GainFilter();
    private final float  ratio = 100f;
    private float gain = 50f/ratio;
    private float bias = 50f/ratio;

    @Override
    public void applyEffect(BufferedImage img) {
        filter.setGain(gain);
        filter.setBias(bias);
        Graphics2D buffer = img.createGraphics();
        buffer.setRenderingHint(RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_SPEED);
        buffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_OFF);
        buffer.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        buffer.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                           RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        buffer.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                           RenderingHints.VALUE_COLOR_RENDER_SPEED);
        buffer.setRenderingHint(RenderingHints.KEY_DITHERING,
                           RenderingHints.VALUE_DITHER_DISABLE);
        BufferedImage temp = filter.filter(img, null);
        buffer.setBackground(new java.awt.Color(0,0,0,0));
        buffer.clearRect(0,0,img.getWidth(),img.getHeight());
        buffer.drawImage(temp, 0, 0,null);
        buffer.dispose();
    }

    @Override
    public JPanel getControl() {
        return new GainControl(this);
    }
    @Override
    public boolean needApply(){
        return needApply=true;
    }

    /**
     * @return the brightness
     */
    public int getGain() {
        return (int)(gain*ratio);
    }

    /**
     * @param gain
     */
    public void setGain(int gain) {
        this.gain = gain/ratio;
    }

    /**
     * @return the contrast
     */
    public int getBias() {
        return (int)(bias*ratio);
    }

    /**
     * @param bias
     */
    public void setBias(int bias) {
        this.bias = bias/ratio;
    }

    @Override
    public void resetFX() {
        // nothing here.
    }
}
