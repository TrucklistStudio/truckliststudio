/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.sources.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import truckliststudio.sources.effects.controls.OpacityControl;

/**
 *
 * @author pballeux (modified by karl)
 */
public class Opacity extends Effect {

    private int opacity = 100;

    @Override
    public void applyEffect(BufferedImage img) {
        BufferedImage temp = cloneImage(img);
        Graphics2D buffer = img.createGraphics();
        buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, opacity / 100F));
        buffer.setBackground(new Color(0, 0, 0, 0));
        buffer.clearRect(0, 0, img.getWidth(), img.getHeight());
        buffer.drawImage(temp, 0, 0, null);
        buffer.dispose();
    }

    public void setOpacity(int o) {
        opacity = o;
    }

    public int getOpacity() {
        return opacity;
    }

    @Override
    public JPanel getControl() {
        return new OpacityControl(this);
    }

    @Override
    public boolean needApply() {
        return needApply = true;
    }

    @Override
    public void resetFX() {
        // nothing here.
    }

}
