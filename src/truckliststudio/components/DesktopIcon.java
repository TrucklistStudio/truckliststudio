/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JInternalFrame.JDesktopIcon;
import org.imgscalr.Scalr;
import truckliststudio.mixers.MasterMixer;
import truckliststudio.streams.SourceText;
import truckliststudio.streams.Stream;

/**
 *
 * @author patrick
 */
public class DesktopIcon extends JDesktopIcon {

    Stream stream = null;
    Viewer viewer = null;
    JInternalFrame frameDesktop = null;
    BufferedImage imgBtn;
    public DesktopIcon(JInternalFrame f, Stream s) {
        super(f);
        frameDesktop = f;
        stream = s;
        this.removeAll();
        this.setLayout(new BorderLayout());
        viewer = new Viewer();
        ImageIcon pauseIcon = new ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-playback-play.png"));
        imgBtn = new BufferedImage(pauseIcon.getIconWidth(), pauseIcon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        pauseIcon.paintIcon(null, imgBtn.getGraphics(), 0, 0);
        final int mW = MasterMixer.getInstance().getWidth()/3;
        final int mH = MasterMixer.getInstance().getHeight()/3;
        imgBtn = Scalr.resize(imgBtn, Scalr.Mode.FIT_EXACT, mW, mH);
//        System.out.println("Pause W:"+sW+" - Pause H:"+sH);
        add(viewer, BorderLayout.CENTER);
        this.setToolTipText("Stream: " + s.getName() + " | Layer: " + s.getZOrder());
        this.setVisible(true);
        this.setSize(64, 64);
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!frameDesktop.isClosed()) {
                    try {
                        if (frameDesktop.isIcon()) {
                            BufferedImage img = stream.getPreview();                            
                            if (stream.isPlaying()) {
                                if (img != null){
                                    int ImgHeight = img.getHeight();
                                    int ImgWidth = img.getWidth();
                                    BufferedImage newImg = new BufferedImage(ImgWidth,ImgHeight, BufferedImage.TYPE_INT_RGB);
                                    Graphics2D gr = newImg.createGraphics();
                                    gr.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                                                        java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                                    gr.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                                                        java.awt.RenderingHints.VALUE_RENDER_SPEED);
                                    gr.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                                                        java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
                                    gr.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                                                        java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                                    gr.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS,
                                                        java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
                                    gr.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING,
                                                        java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED);
                                    gr.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING,
                                                        java.awt.RenderingHints.VALUE_DITHER_DISABLE);
                                    
                                    gr.setColor(Color.green);
                                    gr.fillRect(0,0,newImg.getWidth(),newImg.getHeight());

                                    gr.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 75 / 100F));
                                    gr.drawImage(img,0,0,null);
                                    if (stream.getisPaused()) {
                                        gr.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 100 / 100F));
                                        gr.drawImage(imgBtn,mW,mH,null);
                                    }
                                    img = newImg;
                                }
                            } else {
                                if (img != null){
                                    BufferedImage stopImg = stream.getPreview();
                                    img=stopImg;
                                }
                            } 
                            viewer.setImage(img);
                            viewer.setAudioLevel(stream.getAudioLevelLeft(), stream.getAudioLevelRight());
                            viewer.repaint();
                            String sTip = null;
                            if (stream instanceof SourceText){
                                SourceText sc = (SourceText)stream;
                                sTip = sc.getContent();
                                if (sTip.length() > 10) {
                                    setToolTipText("Text: " + sTip.substring(0, 10) + "..." + " | Layer: " + sc.getZOrder());
                                } else {
                                    setToolTipText("Text: " + sTip + " | Layer: " + sc.getZOrder());
                                }
                            } else {
                                setToolTipText("Stream: " + stream.getName() + " | Layer: " + stream.getZOrder());
                            }
                        }
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DesktopIcon.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
        
    }
}
