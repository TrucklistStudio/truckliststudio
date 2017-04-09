/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.sources.effects;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Rotate;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import truckliststudio.sources.effects.controls.RotationControl;

/**
 *
 * @author karl
 */
public class Rotation extends Effect {

    private int rotation = 0;

    @Override
    public void applyEffect(BufferedImage img) {

        FastBitmap imageIn = new FastBitmap(img);
        Rotate.Algorithm algorithm = Rotate.Algorithm.BILINEAR;
        Rotate c = new Rotate(rotation, algorithm);
        c.applyInPlace(imageIn);
        BufferedImage temp = imageIn.toBufferedImage();
        Graphics2D buffer = img.createGraphics();
        buffer.setBackground(new Color(0, 0, 0, 0));
        buffer.clearRect(0, 0, img.getWidth(), img.getHeight());
        buffer.drawImage(temp, 0, 0, img.getWidth(), img.getHeight(), 0, 0, temp.getWidth(), temp.getHeight(), null);
        buffer.dispose();
    }

    @Override
    public boolean needApply() {
        return needApply = true;
    }

    public void setRotation(int value) {
        rotation = value;
    }

    public int getRotation() {
        return rotation;
    }

    @Override
    public JPanel getControl() {
        return new RotationControl(this);
    }

    @Override
    public void resetFX() {
        // nothing here.
    }
}
