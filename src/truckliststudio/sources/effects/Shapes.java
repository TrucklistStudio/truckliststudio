/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.sources.effects;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import org.imgscalr.Scalr;
import truckliststudio.mixers.MasterMixer;
import truckliststudio.sources.effects.controls.ShapesControl;

/**
 *
 * @author pballeux (modified by karl)
 */
public class Shapes extends Effect {

    private BufferedImage overlay = null;
    private String shapeS = "Sun";
    private boolean doReverseShapeMask = false;
    private int width = MasterMixer.getInstance().getWidth();
    private int height = MasterMixer.getInstance().getHeight();
    private boolean doOne = true;
    private Properties shapeP = new Properties();
    public Shapes(){
        try {
            this.overlay = ImageIO.read(getClass().getResource("/truckliststudio/resources/shapes/"+shapeS.toLowerCase()+".png"));
            this.overlay = Scalr.resize(overlay, Scalr.Mode.AUTOMATIC, width, height);
        } catch (IOException ex) {
            Logger.getLogger(Shapes.class.getName()).log(Level.SEVERE, null, ex);
        }
        try { 
            shapeP.load(getClass().getResourceAsStream("/truckliststudio/resources/shapes/Shapes.properties"));
        } catch (IOException ex) {
            Logger.getLogger(Shapes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public JPanel getControl() {
        return new ShapesControl(this);
    }
    
    @Override
    public boolean needApply(){
        return needApply=true;
    }
    @Override
    public void applyEffect(BufferedImage img) {
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
        if (doReverseShapeMask) {
            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.DST_OUT, 1.0F));
        } else {
            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.DST_IN, 1.0F));
        }
        buffer.setColor(java.awt.Color.BLACK);
        buffer.drawImage(overlay, 0, 0, img.getWidth(), img.getHeight(), 0, 0, overlay.getWidth(), overlay.getHeight(), null);
        buffer.dispose();
    }
    public String getShape() {
        return this.shapeS;
    }
    
    @Override
    public void setDoOne(boolean doOn) {
        this.doOne = doOn;
//        System.out.println("SetDoOne ...");
    }
    
    @Override
    public void setShape(String shape) {
        this.shapeS = shape;
//        System.out.println("SetShape: "+this.shapeS);
        String shapeF = shapeP.getProperty(shapeS);
//        System.out.println("shapeFile: "+shapeF);
        try {
            this.overlay = ImageIO.read(getClass().getResource("/truckliststudio/resources/shapes/"+shapeF));
            this.overlay = Scalr.resize(overlay, Scalr.Mode.AUTOMATIC, width, height);
        } catch (IOException ex) {
            Logger.getLogger(Shapes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public boolean getReverse() {
        return this.doReverseShapeMask;
    }
    public void setReverse(boolean reverse) {
        this.doReverseShapeMask = reverse;
    }

    @Override
    public void resetFX() {
        // nothing here.
    }
}
