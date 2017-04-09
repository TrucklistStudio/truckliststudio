/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.sources.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author pballeux (modified by karl)
 */
public class Mirror1 extends Effect {

    @Override
    public void applyEffect(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        Graphics2D buffer = img.createGraphics();
        BufferedImage original = cloneImage(img);
        buffer.setBackground(new Color(0, 0, 0, 0));
        buffer.clearRect(w / 2, 0, w / 2, h);
        buffer.drawImage(original, w, 0, w / 2, h, 0, 0, w / 2, h, null);
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
