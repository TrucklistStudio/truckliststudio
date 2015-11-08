/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * StreamDesktop.java
 *
 * Created on 15-Apr-2012, 12:29:14 AM
 */
package truckliststudio.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import static truckliststudio.TrucklistStudio.wsDistroWatch;
import static truckliststudio.components.TrackPanel.lblPlayingTrack;
import static truckliststudio.components.TrackPanel.listTracks;
import truckliststudio.mixers.MasterFrameBuilder;
import truckliststudio.mixers.MasterMixer;
import truckliststudio.mixers.PreviewFrameBuilder;
import truckliststudio.streams.SourceAudioSource;
import truckliststudio.streams.SourceImage;
import truckliststudio.streams.SourceImageGif;
//import truckliststudio.streams.SourceImageU;
import truckliststudio.streams.SourceMovie;
import truckliststudio.streams.SourceMusic;
import truckliststudio.streams.SourceText;
import truckliststudio.streams.Stream;
import truckliststudio.util.AudioSource;
import truckliststudio.util.PaCTL;
import truckliststudio.util.BackEnd;
import truckliststudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class StreamDesktop extends javax.swing.JInternalFrame {

    StreamPanel panel = null;
    StreamPanelText panelText = null;
    Stream stream = null;
    Listener listener = null;
    private boolean runMe = true;
    private int speed = 1; // + is faster - is slower
    AudioSource[] sourcesAudio;
    String distro = wsDistroWatch();

    public interface Listener{
        public void selectedSource(Stream source);
        public void closeSource(String name);
    }
    /** Creates new form StreamDesktop
     * @param s
     * @param l */
    public StreamDesktop(final Stream s,Listener l) {
        listener = l;
        stream = s;
        initComponents();
        
        jCBAVConv.setVisible(BackEnd.avconvDetected());
        jCBFFmpeg.setVisible(BackEnd.ffmpegDetected());
        if (distro.equals("windows")) {
            jCBGStreamer.setVisible(false);
        }
        if (s instanceof SourceText) {
            StreamPanelText p = new StreamPanelText((Stream)s);
            this.setLayout(new BorderLayout());
            this.add(p, BorderLayout.CENTER);
            this.setTitle(s.getName());
            this.setVisible(true);
            jMControls.setVisible(false);
            jMBackEnd.setVisible(false);
            jMAudioSource.setVisible(false);
            jMLoop.setVisible(false);
            panelText = p;
            s.setPanelType("PanelText");
        } else {
            if (s instanceof SourceAudioSource) {
                final ArrayList<JCheckBoxMenuItem> aSMenuItem = new ArrayList<>();
                try {
                    sourcesAudio = PaCTL.getSources();
                } catch (IOException ex) {
                    Logger.getLogger(StreamDesktop.class.getName()).log(Level.SEVERE, null, ex);
                }
                for (AudioSource audioSource : sourcesAudio){
                    final JCheckBoxMenuItem jCBMenuItem = new JCheckBoxMenuItem();
                    jCBMenuItem.setText(audioSource.description);
                    jCBMenuItem.setName(audioSource.device); // NOI18N
                    jCBMenuItem.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            s.setAudioSource(jCBMenuItem.getName());
                            for (JCheckBoxMenuItem jCb : aSMenuItem) {
                                if (!jCb.getName().equals(s.getAudioSource())) {
                                    jCb.setSelected(false);
                                }
                            }
                        }
                    });
                    jMAudioSource.add(jCBMenuItem);
                    aSMenuItem.add(jCBMenuItem);
                    if (s.getLoaded()){
                        for (JCheckBoxMenuItem jCb : aSMenuItem) {
                            if (jCb.getName().equals(s.getAudioSource())) {
                                jCb.setSelected(true);
                            }
                        }
                    } else {
                        JCheckBoxMenuItem initJCb = aSMenuItem.get(0);
                        initJCb.setSelected(true);
                        s.setAudioSource(initJCb.getName());
                    }
                }
            }
            
            StreamPanel p = new StreamPanel(s);
            this.setLayout(new BorderLayout());
            this.add(p, BorderLayout.CENTER);
            this.setTitle(s.getName());
            this.setVisible(true);
            if (stream.getLoaded()){
                switch (stream.getComm()) {
                    case "AV":
                        jCBAVConv.setSelected(true);
                        stream.setComm("AV");
                        jCBGStreamer.setSelected(false);
                        break;
                    case "GS":
                        jCBGStreamer.setSelected(true);
                        stream.setComm("GS");
                        jCBAVConv.setSelected(false);
                        break;
                    case "FF":
                        jCBFFmpeg.setSelected(true);
                        stream.setComm("FF");
                        stream.setBackFF(true);
                        jCBAVConv.setSelected(false);
                        jCBGStreamer.setSelected(false);
                        break;
                    default:
                        if (stream instanceof SourceAudioSource) { // ||stream instanceof SourceImageU
                            jCBGStreamer.setSelected(true);
                            stream.setComm("GS");
                            jCBAVConv.setSelected(false);
                            jCBFFmpeg.setSelected(false);
                        } else {
                            jCBAVConv.setSelected(true);
                            stream.setComm("AV");
                            jCBGStreamer.setSelected(false);
                            jCBFFmpeg.setSelected(false);
                        }
                        break;
                }
            } else {
                if (stream instanceof SourceAudioSource) { // ||stream instanceof SourceImageU
//                    if (distro.equals("windows")) {
//                        stream.setComm("FF");
//                        jCBFFmpeg.setSelected(true);    
//                    } else {
                        jCBGStreamer.setSelected(true);
                        stream.setComm("GS");
                        jCBAVConv.setSelected(false);
                        jCBFFmpeg.setSelected(false);
//                    }
                } else {
                    if (distro.toLowerCase().equals("ubuntu")){
                        jCBAVConv.setSelected(true);
                        stream.setComm("AV");
                        jCBGStreamer.setSelected(false);
                        jCBFFmpeg.setSelected(false);
                    } else if (distro.toLowerCase().equals("windows")){
                        stream.setComm("FF");
                        stream.setBackFF(true);
                        jCBFFmpeg.setSelected(true);
                    } else {
                        jCBAVConv.setSelected(false);
                        stream.setComm("FF");
                        stream.setBackFF(true);
                        jCBGStreamer.setSelected(false);
                        jCBFFmpeg.setSelected(true);
                    }
                }
                jMLoop.setVisible(false);
            }
            if (stream instanceof SourceImageGif  || stream instanceof SourceImage){
                jMBackEnd.setVisible(false);
                jMLoop.setVisible(false);
            }
            panel = p;
            s.setPanelType("Panel");
            jCBMoreOptions.setEnabled(true);
            if (s instanceof SourceAudioSource) {
                jMAudioSource.setVisible(true);
                jMLoop.setVisible(false);
            } else {
                jMAudioSource.setVisible(false);
            }
            if (s instanceof SourceMovie || s instanceof SourceMusic) {
                jMLoop.setVisible(true);
                jCBLoop.setSelected(s.getLoop());
            }
        }
        this.setVisible(true);
        this.setDesktopIcon(new DesktopIcon(this,s));
        this.setClosable(true);
        this.setToolTipText(stream.getName());
        pack();
        if (stream.getMore()){
            jCBMoreOptions.doClick();
        }
        if (stream.getSliders()) {
            jCBShowSliders.doClick();
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMBOptions = new javax.swing.JMenuBar();
        jMControls = new javax.swing.JMenu();
        jCBShowSliders = new javax.swing.JCheckBoxMenuItem();
        jCBMoreOptions = new javax.swing.JCheckBoxMenuItem();
        jMScroll = new javax.swing.JMenu();
        jCBRightToLeft = new javax.swing.JCheckBoxMenuItem();
        jCBLeftToRight = new javax.swing.JCheckBoxMenuItem();
        jCBBottomToTop = new javax.swing.JCheckBoxMenuItem();
        jCBTopToBottom = new javax.swing.JCheckBoxMenuItem();
        jCBHBouncing = new javax.swing.JCheckBoxMenuItem();
        jMSpeed = new javax.swing.JMenu();
        radioSpeed1 = new javax.swing.JRadioButtonMenuItem();
        radioSpeed2 = new javax.swing.JRadioButtonMenuItem();
        radioSpeed3 = new javax.swing.JRadioButtonMenuItem();
        radioSpeed4 = new javax.swing.JRadioButtonMenuItem();
        radioSpeed5 = new javax.swing.JRadioButtonMenuItem();
        jMLoop = new javax.swing.JMenu();
        jCBLoop = new javax.swing.JCheckBoxMenuItem();
        jMRefresh = new javax.swing.JMenu();
        jMAudioSource = new javax.swing.JMenu();
        jMBackEnd = new javax.swing.JMenu();
        jCBGStreamer = new javax.swing.JCheckBoxMenuItem();
        jCBAVConv = new javax.swing.JCheckBoxMenuItem();
        jCBFFmpeg = new javax.swing.JCheckBoxMenuItem();

        setClosable(true);
        setIconifiable(true);
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/user-desktop.png"))); // NOI18N
        setVisible(true);
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosing(evt);
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameIconified(evt);
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameDeiconified(evt);
            }
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameActivated(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
        });
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
                formAncestorMoved(evt);
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        jMBOptions.setName("jMBOptions"); // NOI18N
        jMBOptions.setPreferredSize(new java.awt.Dimension(74, 17));
        jMBOptions.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jMBOptionsMousePressed(evt);
            }
        });

        jMControls.setForeground(new java.awt.Color(74, 7, 1));
        jMControls.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/settings2-button-small.png"))); // NOI18N
        jMControls.setToolTipText("Settings");
        jMControls.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jMControls.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jMControls.setName("jMControls"); // NOI18N

        jCBShowSliders.setText("Show Control Sliders");
        jCBShowSliders.setName("jCBShowSliders"); // NOI18N
        jCBShowSliders.setPreferredSize(new java.awt.Dimension(177, 15));
        jCBShowSliders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBShowSlidersActionPerformed(evt);
            }
        });
        jMControls.add(jCBShowSliders);
        jCBShowSliders.getAccessibleContext().setAccessibleParent(jMControls);

        jCBMoreOptions.setText("Show more Options");
        jCBMoreOptions.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jCBMoreOptions.setName("jCBMoreOptions"); // NOI18N
        jCBMoreOptions.setPreferredSize(new java.awt.Dimension(169, 15));
        jCBMoreOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBMoreOptionsActionPerformed(evt);
            }
        });
        jMControls.add(jCBMoreOptions);
        jCBMoreOptions.getAccessibleContext().setAccessibleParent(jMControls);

        jMBOptions.add(jMControls);

        jMScroll.setForeground(new java.awt.Color(74, 7, 1));
        jMScroll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/cruz-small-2.png"))); // NOI18N
        jMScroll.setToolTipText("Scroll");
        jMScroll.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jMScroll.setName("jMScroll"); // NOI18N

        jCBRightToLeft.setText("RightToLeft");
        jCBRightToLeft.setName("jCBRightToLeft"); // NOI18N
        jCBRightToLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBRightToLeftActionPerformed(evt);
            }
        });
        jMScroll.add(jCBRightToLeft);

        jCBLeftToRight.setText("LeftToRight");
        jCBLeftToRight.setName("jCBLeftToRight"); // NOI18N
        jCBLeftToRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBLeftToRightActionPerformed(evt);
            }
        });
        jMScroll.add(jCBLeftToRight);

        jCBBottomToTop.setText("BottomToTop");
        jCBBottomToTop.setName("jCBBottomToTop"); // NOI18N
        jCBBottomToTop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBBottomToTopActionPerformed(evt);
            }
        });
        jMScroll.add(jCBBottomToTop);

        jCBTopToBottom.setText("TopToBottom");
        jCBTopToBottom.setName("jCBTopToBottom"); // NOI18N
        jCBTopToBottom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBTopToBottomActionPerformed(evt);
            }
        });
        jMScroll.add(jCBTopToBottom);

        jCBHBouncing.setText("H-Bouncing");
        jCBHBouncing.setName("jCBHBouncing"); // NOI18N
        jCBHBouncing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBHBouncingActionPerformed(evt);
            }
        });
        jMScroll.add(jCBHBouncing);

        jMSpeed.setText("Speed");
        jMSpeed.setName("jMSpeed"); // NOI18N

        radioSpeed1.setSelected(true);
        radioSpeed1.setText("1");
        radioSpeed1.setName("radioSpeed1"); // NOI18N
        radioSpeed1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSpeed1ActionPerformed(evt);
            }
        });
        jMSpeed.add(radioSpeed1);

        radioSpeed2.setText("2");
        radioSpeed2.setName("radioSpeed2"); // NOI18N
        radioSpeed2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSpeed2ActionPerformed(evt);
            }
        });
        jMSpeed.add(radioSpeed2);

        radioSpeed3.setText("3");
        radioSpeed3.setName("radioSpeed3"); // NOI18N
        radioSpeed3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSpeed3ActionPerformed(evt);
            }
        });
        jMSpeed.add(radioSpeed3);

        radioSpeed4.setText("4");
        radioSpeed4.setName("radioSpeed4"); // NOI18N
        radioSpeed4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSpeed4ActionPerformed(evt);
            }
        });
        jMSpeed.add(radioSpeed4);

        radioSpeed5.setText("5");
        radioSpeed5.setName("radioSpeed5"); // NOI18N
        radioSpeed5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSpeed5ActionPerformed(evt);
            }
        });
        jMSpeed.add(radioSpeed5);

        jMScroll.add(jMSpeed);

        //jMBOptions.add(Box.createHorizontalGlue());

        jMBOptions.add(jMScroll);

        jMLoop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/loop4-small.png"))); // NOI18N
        jMLoop.setToolTipText("Loop Source");
        jMLoop.setName("jMLoop"); // NOI18N

        jCBLoop.setText("On");
        jCBLoop.setName("jCBLoop"); // NOI18N
        jCBLoop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBLoopActionPerformed(evt);
            }
        });
        jMLoop.add(jCBLoop);

        jMBOptions.add(jMLoop);

        jMRefresh.setForeground(new java.awt.Color(1, 188, 3));
        jMRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/view-refresh-small.png"))); // NOI18N
        jMRefresh.setToolTipText("Refresh Properties");
        jMRefresh.setFont(new java.awt.Font("Ubuntu Condensed", 1, 8)); // NOI18N
        jMRefresh.setName("jMRefresh"); // NOI18N
        jMRefresh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMRefreshMouseClicked(evt);
            }
        });
        jMBOptions.add(jMRefresh);

        jMAudioSource.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/audiosource.png"))); // NOI18N
        jMAudioSource.setToolTipText("Audio Source Selector");
        jMAudioSource.setName("jMAudioSource"); // NOI18N
        jMBOptions.add(jMAudioSource);

        jMBackEnd.setForeground(new java.awt.Color(74, 7, 1));
        jMBackEnd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/bkend-switch5-button-small.png"))); // NOI18N
        jMBackEnd.setToolTipText("Back-End Switch");
        jMBackEnd.setBorderPainted(true);
        jMBackEnd.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jMBackEnd.setName("jMBackEnd"); // NOI18N

        jCBGStreamer.setText("GStreamer");
        jCBGStreamer.setName("jCBGStreamer"); // NOI18N
        jCBGStreamer.setPreferredSize(new java.awt.Dimension(107, 15));
        jCBGStreamer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBGStreamerActionPerformed(evt);
            }
        });
        jMBackEnd.add(jCBGStreamer);

        jCBAVConv.setText("AVConv");
        jCBAVConv.setName("jCBAVConv"); // NOI18N
        jCBAVConv.setPreferredSize(new java.awt.Dimension(107, 15));
        jCBAVConv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBAVConvActionPerformed(evt);
            }
        });
        jMBackEnd.add(jCBAVConv);

        jCBFFmpeg.setText("FFmpeg");
        jCBFFmpeg.setName("jCBFFmpeg"); // NOI18N
        jCBFFmpeg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBFFmpegActionPerformed(evt);
            }
        });
        jMBackEnd.add(jCBFFmpeg);

        jMBOptions.add(Box.createHorizontalGlue());

        jMBOptions.add(jMBackEnd);

        setJMenuBar(jMBOptions);
        jMBOptions.getAccessibleContext().setAccessibleParent(this);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formInternalFrameIconified(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameIconified
       if (panel!=null){
        this.setFrameIcon(panel.getIcon());
       }
    }//GEN-LAST:event_formInternalFrameIconified

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        System.out.println("Closed");
        if (stream.getisATrack() && stream.getName().equals(lblPlayingTrack.getText())) {
            listTracks.repaint();
            lblPlayingTrack.setText("");
        }
        listener.closeSource(stream.getName());
        stream.setLoop(false);
        PreviewFrameBuilder.unregister(stream);
        MasterFrameBuilder.unregister(stream);
        stream.destroy();
        stream = null;
        panel = null;
        truckliststudio.TrucklistStudio.tabControls.removeAll();
        truckliststudio.TrucklistStudio.tabControls.repaint();
    }//GEN-LAST:event_formInternalFrameClosing

    private void jCBMoreOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBMoreOptionsActionPerformed
        switch (stream.getPanelType()) {
            case "Panel":
                if (jCBMoreOptions.isSelected()){
                    if (stream.needSeekCTRL()) {
                        this.setSize(new Dimension(this.getWidth(),443));
                        this.revalidate();
                        this.repaint();
                    } else {
                        this.setSize(new Dimension(this.getWidth(),423));
                        this.revalidate();
                        this.repaint();
                    }
                    stream.setMore(true);
                } else {
                    this.setSize(new Dimension(this.getWidth(),338));
                    this.revalidate();
                    this.repaint();
                    stream.setMore(false);
                }   break;
            case "PanelDVB":
                if (jCBMoreOptions.isSelected()){
                    this.setSize(new Dimension(this.getWidth(),533));
                    this.revalidate();
                    this.repaint();
                    stream.setMore(true);
                } else {
                    this.setSize(new Dimension(this.getWidth(),448));
                    this.revalidate();
                    this.repaint();
                    stream.setMore(false);
                }   break;
            case "PanelURL":
                if (jCBMoreOptions.isSelected()){
                    this.setSize(new Dimension(this.getWidth(),463));
                    this.revalidate();
                    this.repaint();
                    stream.setMore(true);
                } else {
                    this.setSize(new Dimension(this.getWidth(),378));
                    this.revalidate();
                    this.repaint();
                    stream.setMore(false);
                }   break;
            case "PanelIPCam":
                if (jCBMoreOptions.isSelected()){
                    this.setSize(new Dimension(this.getWidth(),463));
                    this.revalidate();
                    this.repaint();
                    stream.setMore(true);
                } else {
                    this.setSize(new Dimension(this.getWidth(),378));
                    this.revalidate();
                    this.repaint();
                    stream.setMore(false);
                }   break;
        }
    }//GEN-LAST:event_jCBMoreOptionsActionPerformed

    private void jCBGStreamerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBGStreamerActionPerformed
        if (jCBGStreamer.isSelected()){
            stream.setComm("GS");
            stream.setBackFF(false);
            jCBAVConv.setSelected(false);
            jCBFFmpeg.setSelected(false);
            if (listener!=null){
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        listener.selectedSource(stream);
                    }
                }).start();

            }
        } else {
            jCBAVConv.setSelected(true);
            jCBGStreamer.setSelected(false);
            jCBFFmpeg.setSelected(false);
            stream.setBackFF(false);
            stream.setComm("AV");
        }
    }//GEN-LAST:event_jCBGStreamerActionPerformed

    private void jCBAVConvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBAVConvActionPerformed
        if (jCBAVConv.isSelected()){
            stream.setComm("AV");
            stream.setBackFF(false);
            jCBGStreamer.setSelected(false);
            jCBFFmpeg.setSelected(false);
            if (listener!=null){
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        listener.selectedSource(stream);
                    }
                }).start();

            }
        } else {
            jCBGStreamer.setSelected(true);
            jCBAVConv.setSelected(false);
            jCBFFmpeg.setSelected(false);
            stream.setComm("GS");
            stream.setBackFF(false);
        }
    }//GEN-LAST:event_jCBAVConvActionPerformed

    private void jCBShowSlidersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBShowSlidersActionPerformed
        if (jCBShowSliders.isSelected()){
            this.setSize(new Dimension(298,this.getHeight()));
            this.revalidate();
            this.repaint();
            stream.setSliders(true);
        } else {
            this.setSize(new Dimension(136,this.getHeight()));
            this.revalidate();
            this.repaint();
            stream.setSliders(false);
        }
    }//GEN-LAST:event_jCBShowSlidersActionPerformed

    private void jCBRightToLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBRightToLeftActionPerformed
        final int oldBkX = stream.getX();
        runMe = true;
        if (jCBRightToLeft.isSelected()) {
            
            jCBLeftToRight.setEnabled(false);
            jCBBottomToTop.setEnabled(false);
            jCBTopToBottom.setEnabled(false);
            jCBHBouncing.setEnabled(false);
            
            Thread scrollRtL = new Thread(new Runnable() {
                int deltaX = 0;
                @Override
                public void run() {
                    int startX = stream.getX();
                    while (runMe && stream.isPlaying()){
                        final int mixerW = MasterMixer.getInstance().getWidth();
                        final int streamW = stream.getWidth();
//                        System.out.println("deltax="+(startX+streamW));
                        if ((startX+streamW) <= 0) {
                            stream.setX(mixerW);
                        }
                        final int rate = stream.getRate();
                        for (int i = 0; i<rate;i++){
                            if (runMe) {
                                startX = stream.getX();
                                stream.setX(startX - speed);
                                Tools.sleep(1000/rate);
                            } else {
                                stream.setX(oldBkX);
                                break;
                            }
                        }
                    }
                    stream.setX(oldBkX);
                }
            });
            scrollRtL.setPriority(Thread.MIN_PRIORITY);
            scrollRtL.start();
        } else {
            runMe = false;
            stream.setX(oldBkX);
            jCBLeftToRight.setEnabled(true);
            jCBBottomToTop.setEnabled(true);
            jCBTopToBottom.setEnabled(true);
            jCBHBouncing.setEnabled(true);
        }
    }//GEN-LAST:event_jCBRightToLeftActionPerformed

    private void jCBLeftToRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBLeftToRightActionPerformed
        final int oldBkX = stream.getX();      
        runMe = true;
        if (jCBLeftToRight.isSelected()) {
            
            jCBRightToLeft.setEnabled(false);
            jCBBottomToTop.setEnabled(false);
            jCBTopToBottom.setEnabled(false);
            jCBHBouncing.setEnabled(false);
            
            Thread scrollRtL = new Thread(new Runnable() {

            @Override
            public void run() {
                int startX = stream.getX();
                while (runMe && stream.isPlaying()){
                    final int mixerW = MasterMixer.getInstance().getWidth();
                    final int streamW = stream.getWidth();
//                        System.out.println("deltax="+(startX+streamW));
                    if ((startX) >= mixerW) {
                        stream.setX(-streamW);
                    }
                    final int rate = stream.getRate();
                    for (int i = 0; i<rate;i++){
                        if (runMe) {
                            startX = stream.getX();
                            stream.setX(startX + speed);
                            Tools.sleep(1000/rate);
                        } else {
                            stream.setX(oldBkX);
                            break;
                        }
                    }
                }
                stream.setX(oldBkX);
            }
        }); 
        scrollRtL.setPriority(Thread.MIN_PRIORITY);
        scrollRtL.start();
        } else {
            runMe = false;
            stream.setX(oldBkX);            
            jCBRightToLeft.setEnabled(true);
            jCBBottomToTop.setEnabled(true);
            jCBTopToBottom.setEnabled(true);
            jCBHBouncing.setEnabled(true);
        }
    }//GEN-LAST:event_jCBLeftToRightActionPerformed

    private void jCBBottomToTopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBBottomToTopActionPerformed
        final int oldBkY = stream.getY();     
        runMe = true;
        if (jCBBottomToTop.isSelected()) {
            
            jCBLeftToRight.setEnabled(false);
            jCBRightToLeft.setEnabled(false);
            jCBTopToBottom.setEnabled(false);
            jCBHBouncing.setEnabled(false);

            Thread scrollRtL = new Thread(new Runnable() {
                int deltaY = 0;
            @Override
            public void run() {
                int startY = stream.getY();
                while (runMe && stream.isPlaying()){
                    final int mixerH = MasterMixer.getInstance().getHeight();
                    final int streamH = stream.getHeight();
//                        System.out.println("deltax="+(startX+streamW));
                    if ((startY+streamH) <= 0) {
                        stream.setY(mixerH);
                    }
                    final int rate = stream.getRate();
                    for (int i = 0; i<rate;i++){
                        if (runMe) {
                            startY = stream.getY();
                            stream.setY(startY - speed);
                            Tools.sleep(1000/rate);
                        } else {
                            stream.setY(oldBkY);
                            break;
                        }
                    }
                }
                stream.setY(oldBkY);
            }
        }); 
        scrollRtL.setPriority(Thread.MIN_PRIORITY);
        scrollRtL.start();
        } else {
            runMe = false;
            stream.setY(oldBkY);
            jCBLeftToRight.setEnabled(true);
            jCBRightToLeft.setEnabled(true);
            jCBTopToBottom.setEnabled(true);
            jCBHBouncing.setEnabled(true);
        }
    }//GEN-LAST:event_jCBBottomToTopActionPerformed

    private void jCBTopToBottomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBTopToBottomActionPerformed
        final int oldBkY = stream.getY();
        runMe = true;
        if (jCBTopToBottom.isSelected()) {
            
            jCBLeftToRight.setEnabled(false);
            jCBBottomToTop.setEnabled(false);
            jCBRightToLeft.setEnabled(false);
            jCBHBouncing.setEnabled(false);

            Thread scrollRtL = new Thread(new Runnable() {

            @Override
            public void run() {
                int startY = stream.getY();
                while (runMe && stream.isPlaying()){
                    final int mixerH = MasterMixer.getInstance().getHeight();
                    final int streamH = stream.getHeight();
//                        System.out.println("deltax="+(startX+streamW));
                    if ((startY) >= mixerH) {
                        stream.setY(-streamH);
                    }
                    final int rate = stream.getRate();
                    for (int i = 0; i < rate; i++){
                        if (runMe) {
                            startY = stream.getY();
                            stream.setY(startY + speed);
                            Tools.sleep(1000/rate);
                        } else {
                            stream.setY(oldBkY);
                            break;
                        }
                    }
                }
                stream.setY(oldBkY);
            }
        }); 
        scrollRtL.setPriority(Thread.MIN_PRIORITY);
        scrollRtL.start();
        } else {
            runMe = false;
            stream.setY(oldBkY);
            jCBLeftToRight.setEnabled(true);
            jCBBottomToTop.setEnabled(true);
            jCBRightToLeft.setEnabled(true);
            jCBHBouncing.setEnabled(true);
        }
    }//GEN-LAST:event_jCBTopToBottomActionPerformed

    private void jCBHBouncingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBHBouncingActionPerformed
        final int oldBkX = stream.getX();
        runMe = true;
        
        if (jCBHBouncing.isSelected()) {
            
            jCBLeftToRight.setEnabled(false);
            jCBBottomToTop.setEnabled(false);
            jCBTopToBottom.setEnabled(false);
            jCBRightToLeft.setEnabled(false);
            
            Thread scrollRtL = new Thread(new Runnable() {
                int deltaX = 0;
            @Override
            public void run() {
                int startX = stream.getX();
                boolean oneWay = true;
                boolean toLeft = true;
                final int rate = stream.getRate();
                final int mixerW = MasterMixer.getInstance().getWidth();
                while (runMe && stream.isPlaying()){
                    int streamW = stream.getWidth();
                    for (int i = 0; i<rate;i++){
                        if (runMe) {
                            startX = stream.getX();
                            if (startX > 0 && oneWay) {
                                toLeft = true;
                                stream.setX(startX - speed);
                            } 
                            if (startX <= 0 || !oneWay) {
                                toLeft = false;
                                oneWay = false;
                                stream.setX(startX + speed);
                                if ((startX+streamW) >= mixerW) {
                                    oneWay = true;
                                }
                            }
                            Tools.sleep(1000/rate);
                        } else {
                            stream.setX(oldBkX);
                            break;
                        }
                    }
                    if ((startX+streamW) < mixerW && !toLeft) {
                        oneWay = false;
                    } else {
                        oneWay = true;
                    }
                }
                stream.setX(oldBkX);
            }
        }); 
        scrollRtL.setPriority(Thread.MIN_PRIORITY);
        scrollRtL.start();
        } else {
            runMe = false;
            stream.setX(oldBkX);
            jCBLeftToRight.setEnabled(true);
            jCBBottomToTop.setEnabled(true);
            jCBTopToBottom.setEnabled(true);
            jCBRightToLeft.setEnabled(true);
        }
    }//GEN-LAST:event_jCBHBouncingActionPerformed

    private void jCBFFmpegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBFFmpegActionPerformed
        if (jCBFFmpeg.isSelected()){
            stream.setComm("FF");
            stream.setBackFF(true);
            jCBAVConv.setSelected(false);
            jCBGStreamer.setSelected(false);
            if (listener!=null){
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        listener.selectedSource(stream);
                    }
                }).start();

            }
        } else {
            stream.setComm("AV");
            stream.setBackFF(false);
            jCBAVConv.setSelected(true);
            jCBGStreamer.setSelected(false);
        }
    }//GEN-LAST:event_jCBFFmpegActionPerformed

    private void jMRefreshMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMRefreshMouseClicked
        if (listener!=null){
            new Thread(new Runnable(){

                @Override
                public void run() {
                    listener.selectedSource(stream);
                }
            }).start();

        }
    }//GEN-LAST:event_jMRefreshMouseClicked

    private void jCBLoopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBLoopActionPerformed
        if (jCBLoop.isSelected()){
            stream.setLoop(true);
        } else {
            stream.setLoop(false);
        }
    }//GEN-LAST:event_jCBLoopActionPerformed

    private void radioSpeed1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioSpeed1ActionPerformed
        speed = 1;
        radioSpeed2.setSelected(false);
        radioSpeed3.setSelected(false);
        radioSpeed4.setSelected(false);
        radioSpeed5.setSelected(false);
    }//GEN-LAST:event_radioSpeed1ActionPerformed

    private void radioSpeed2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioSpeed2ActionPerformed
        speed = 3;
        radioSpeed1.setSelected(false);
        radioSpeed3.setSelected(false);
        radioSpeed4.setSelected(false);
        radioSpeed5.setSelected(false);
    }//GEN-LAST:event_radioSpeed2ActionPerformed

    private void radioSpeed3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioSpeed3ActionPerformed
        speed = 5;
        radioSpeed1.setSelected(false);
        radioSpeed2.setSelected(false);
        radioSpeed4.setSelected(false);
        radioSpeed5.setSelected(false);
    }//GEN-LAST:event_radioSpeed3ActionPerformed

    private void radioSpeed4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioSpeed4ActionPerformed
        speed = 7;
        radioSpeed1.setSelected(false);
        radioSpeed2.setSelected(false);
        radioSpeed3.setSelected(false);
        radioSpeed5.setSelected(false);
    }//GEN-LAST:event_radioSpeed4ActionPerformed

    private void radioSpeed5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioSpeed5ActionPerformed
        speed = 9;
        radioSpeed1.setSelected(false);
        radioSpeed2.setSelected(false);
        radioSpeed3.setSelected(false);
        radioSpeed4.setSelected(false);
    }//GEN-LAST:event_radioSpeed5ActionPerformed

    private void formAncestorMoved(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorMoved
        stream.setPanelX(this.getX());
        stream.setPanelY(this.getY());
        stream.setMore(jCBMoreOptions.isSelected());
        stream.setSliders(jCBShowSliders.isSelected());
    }//GEN-LAST:event_formAncestorMoved

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
//        if (listener!=null){
//            new Thread(new Runnable(){
//                
//                @Override
//                public void run() {
//                    listener.selectedSource(stream);
//                }
//            }).start();
//            
//        }
    }//GEN-LAST:event_formMousePressed

    private void jMBOptionsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMBOptionsMousePressed
        if (listener!=null){
            new Thread(new Runnable(){
                
                @Override
                public void run() {
                    listener.selectedSource(stream);
                }
            }).start();
            
        }
    }//GEN-LAST:event_jMBOptionsMousePressed

    private void formInternalFrameActivated(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameActivated
        if (listener!=null){
            new Thread(new Runnable(){
                
                @Override
                public void run() {
                    listener.selectedSource(stream);
                }
            }).start();
            
        }
    }//GEN-LAST:event_formInternalFrameActivated

    private void formInternalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameDeiconified
        if (listener!=null){
            new Thread(new Runnable(){
                
                @Override
                public void run() {
                    listener.selectedSource(stream);
                }
            }).start();
            
        }
    }//GEN-LAST:event_formInternalFrameDeiconified

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem jCBAVConv;
    private javax.swing.JCheckBoxMenuItem jCBBottomToTop;
    private javax.swing.JCheckBoxMenuItem jCBFFmpeg;
    private javax.swing.JCheckBoxMenuItem jCBGStreamer;
    private javax.swing.JCheckBoxMenuItem jCBHBouncing;
    private javax.swing.JCheckBoxMenuItem jCBLeftToRight;
    private javax.swing.JCheckBoxMenuItem jCBLoop;
    private javax.swing.JCheckBoxMenuItem jCBMoreOptions;
    private javax.swing.JCheckBoxMenuItem jCBRightToLeft;
    private javax.swing.JCheckBoxMenuItem jCBShowSliders;
    private javax.swing.JCheckBoxMenuItem jCBTopToBottom;
    private javax.swing.JMenu jMAudioSource;
    private javax.swing.JMenuBar jMBOptions;
    private javax.swing.JMenu jMBackEnd;
    private javax.swing.JMenu jMControls;
    private javax.swing.JMenu jMLoop;
    private javax.swing.JMenu jMRefresh;
    private javax.swing.JMenu jMScroll;
    private javax.swing.JMenu jMSpeed;
    private javax.swing.JRadioButtonMenuItem radioSpeed1;
    private javax.swing.JRadioButtonMenuItem radioSpeed2;
    private javax.swing.JRadioButtonMenuItem radioSpeed3;
    private javax.swing.JRadioButtonMenuItem radioSpeed4;
    private javax.swing.JRadioButtonMenuItem radioSpeed5;
    // End of variables declaration//GEN-END:variables
}
