/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package truckliststudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import truckliststudio.sources.effects.controls.MosaicControl;

/**
 *
 * @author pballeux (modified by karl)
 */
public class Mosaic extends Effect {

    private int nbSquaresWidthHeight = 3;

    @Override
    public void applyEffect(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        
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
        
        int smallWidth = w / nbSquaresWidthHeight;
        int smallHeight = h / nbSquaresWidthHeight;
        BufferedImage original = cloneImage(img);
        while (smallWidth * nbSquaresWidthHeight < w) {
            smallWidth++;
        }
        while (smallHeight * nbSquaresWidthHeight < h) {
            smallHeight++;
        }
        buffer.setBackground(new java.awt.Color(0,0,0,0));
        buffer.clearRect(0,0,img.getWidth(),img.getHeight());

        for (int y = 0; y < h; y += smallHeight) {
            for (int x = 0; x < w; x += smallWidth) {
                buffer.drawImage(original, x, y, x + smallWidth, y + smallHeight, 0, 0, w, h, null);
            }
        }
    }
    public void setSplitValue(int splitTime){
        nbSquaresWidthHeight = splitTime;
    }
    public int getSplitValue(){
        return nbSquaresWidthHeight;
    }

    @Override
    public JPanel getControl() {
        return new MosaicControl(this);
    }

    @Override
    public boolean needApply(){
        return needApply=true;
    }

    @Override
    public void resetFX() {
        // nothing here.
    }
}
