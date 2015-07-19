/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author pballeux (modified by karl)
 */
public abstract class Effect {

    public static java.util.TreeMap<String, Effect> getEffects() {
        java.util.TreeMap<String, Effect> retValue = new java.util.TreeMap<>();
        retValue.put(FlipHorizontal.class.getSimpleName(), new FlipHorizontal());
        retValue.put(FlipVertical.class.getSimpleName(), new FlipVertical());
        retValue.put(Mirror1.class.getSimpleName(), new Mirror1());
        retValue.put(Mirror2.class.getSimpleName(), new Mirror2());
        retValue.put(Mirror3.class.getSimpleName(), new Mirror3());
        retValue.put(Mirror4.class.getSimpleName(), new Mirror4());
        retValue.put(Mosaic.class.getSimpleName(), new Mosaic());
        retValue.put(Gray.class.getSimpleName(), new Gray());
        retValue.put(Sharpen.class.getSimpleName(), new Sharpen());
        retValue.put(Rotation.class.getSimpleName(), new Rotation());
        retValue.put(ChromaKey.class.getSimpleName(), new ChromaKey());
        retValue.put(Contrast.class.getSimpleName(), new Contrast());
        retValue.put(SwapRedBlue.class.getSimpleName(), new SwapRedBlue());
        retValue.put(Perspective.class.getSimpleName(), new Perspective());
        retValue.put(Opacity.class.getSimpleName(), new Opacity());
        retValue.put(RGB.class.getSimpleName(), new RGB());
        retValue.put(Stretch.class.getSimpleName(), new Stretch());
        retValue.put(Blink.class.getSimpleName(), new Blink());
        retValue.put(Gain.class.getSimpleName(), new Gain());
        retValue.put(HSB.class.getSimpleName(), new HSB());
        retValue.put(Shapes.class.getSimpleName(), new Shapes());
        retValue.put(RevealRightNFade.class.getSimpleName(), new RevealRightNFade());
        retValue.put(RevealLeftNFade.class.getSimpleName(), new RevealLeftNFade());
        retValue.put(Crop.class.getSimpleName(), new Crop());
        return retValue;
    }
    protected boolean needApply=true;

    public boolean needApply(){
        return needApply;
    }
    public abstract void applyEffect(BufferedImage img);
    public abstract javax.swing.JPanel getControl();
    public abstract void resetFX();
    public BufferedImage cloneImage(BufferedImage src) {
        BufferedImage tempimage = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(src.getWidth(), src.getHeight(), BufferedImage.TRANSLUCENT);
        Graphics2D tempbuffer = tempimage.createGraphics();
        tempbuffer.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_SPEED);
        tempbuffer.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
        tempbuffer.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        tempbuffer.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS,
                java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        tempbuffer.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING,
                java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED);
        tempbuffer.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING,
                java.awt.RenderingHints.VALUE_DITHER_DISABLE);
        tempbuffer.drawImage(src, 0, 0, null);
        tempbuffer.dispose();
        return tempimage;
    }
    
    public String getName(){
        return getClass().getSimpleName();
    }
    
    @Override
    public String toString(){
        return getClass().getSimpleName();
    }
    
    public void setShape(String shapeImg){
        // nothing here.
    }
    
    public void setDoOne(boolean b) {
        // nothing here.
    }
    
    public void clearEffect(Effect e) {
        e = null;
    }

    }
