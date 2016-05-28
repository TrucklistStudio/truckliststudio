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
public class Blink extends Effect {

    long mark = System.currentTimeMillis();
    boolean blink = false;

    @Override
    public void applyEffect(BufferedImage img) {
        if (blink) {
            Graphics2D buffer = img.createGraphics();
            buffer.setBackground(new java.awt.Color(0, 0, 0, 0));
            buffer.clearRect(0, 0, img.getWidth(), img.getHeight());
            buffer.dispose();
        }
        if (System.currentTimeMillis() - mark > 1000) {
            blink = !blink;
            mark = System.currentTimeMillis();
        }

    }

    @Override
    public boolean needApply() {
        return needApply = true;
    }

    @Override
    public JPanel getControl() {
        return null;
    }

    @Override
    public void resetFX() {
        // nothing here.
    }
}
