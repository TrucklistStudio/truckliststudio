/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author pballeux (modified by karl)
 */
public class Mirror4 extends Effect {

    @Override
    public void applyEffect(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        Graphics2D buffer = img.createGraphics();
        BufferedImage original = cloneImage(img);
        buffer.setBackground(new java.awt.Color(0, 0, 0, 0));
        buffer.clearRect(0, 0, w, h / 2);
        buffer.drawImage(original, 0, h / 2, w, 0, 0, h / 2, w, h, null);
    }

    @Override
    public JPanel getControl() {
        return null;
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
