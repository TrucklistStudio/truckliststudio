/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.sources.effects;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Grayscale;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author pballeux (modified by karl)
 */
public class Gray extends Effect {

    @Override
    public void applyEffect(BufferedImage img) {
        FastBitmap imageIn = new FastBitmap(img);
        imageIn.toRGB();
        Grayscale g = new Grayscale();
        g.applyInPlace(imageIn);
        BufferedImage temp = imageIn.toBufferedImage();

        Graphics2D buffer = img.createGraphics();
        buffer.drawImage(temp, 0, 0, null);
        buffer.dispose();
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
