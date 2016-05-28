/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * MasterPanel.java
 *
 * Created on 4-Apr-2012, 6:52:17 PM
 */
package truckliststudio.components;

import static com.jhlabs.image.ImageUtils.cloneImage;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.SpinnerNumberModel;
import truckliststudio.TSPreview;
import truckliststudio.TrucklistStudio;
import truckliststudio.tracks.MasterTracks;
import static truckliststudio.components.TrackPanel.listenerCPOP;
import truckliststudio.mixers.Frame;
import truckliststudio.mixers.MasterMixer;
import truckliststudio.mixers.PrePlayer;
import truckliststudio.mixers.PreviewMixer;
import truckliststudio.streams.SourceTrack;
import truckliststudio.streams.SourceImage;
import truckliststudio.streams.SourceText;
import truckliststudio.streams.Stream;
import truckliststudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class MasterPanel extends javax.swing.JPanel implements MasterMixer.SinkListener, TrackPanel.Listener, TSPreview.Listener, PreviewMixer.SinkListener { //FullScreen.Listener, 

    protected PreViewer preViewer = new PreViewer(false);
    private PrePlayer prePlayer = null;
    private final MasterMixer mixer = MasterMixer.getInstance();
    private final PreviewMixer preMixer = PreviewMixer.getInstance();
    MasterTracks master = MasterTracks.getInstance();
    final static public Dimension PANEL_SIZE = new Dimension(150, 400);
    final static public Dimension smallPANEL_SIZE = new Dimension(240, 125);
    ArrayList<Stream> streamM = MasterTracks.getInstance().getStreams();
    Stream stream = null;
    SourceText sTx = null;
    SourceImage sImg = null;
    boolean lockRatio = false;
    private BufferedImage liveImg = null;
    int opacity = 0;
    int rate = mixer.getRate();
    int i = rate;
    public static float masterVolume = 0f;
    boolean transition = false;

    /**
     * Creates new form MasterPanel
     */
    public MasterPanel() {
        initComponents();
        jslOpacity.setValue(0);
        lblCurtainPre.setVisible(false);
        spinFPS.setModel(new SpinnerNumberModel(5, 5, 30, 5));
        spinWidth.setValue(mixer.getWidth());
        spinHeight.setValue(mixer.getHeight());
        this.setVisible(true);
        preViewer.setOpaque(true);
        panelPreviewer.add(preViewer, BorderLayout.CENTER);
        prePlayer = PrePlayer.getPreInstance(preViewer);
        mixer.register(this);
        preMixer.register(this);
        spinFPS.setValue(MasterMixer.getInstance().getRate());
        final MasterPanel instanceSinkMP = this;
        TSPreview.setListenerPW(instanceSinkMP);
        TrackPanel.setListenerCPMPanel(instanceSinkMP);
        panPreview.setLayout(null);
        panPreview.validate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabMixers = new javax.swing.JTabbedPane();
        panMixer = new javax.swing.JPanel();
        lblWidth = new javax.swing.JLabel();
        lblHeight = new javax.swing.JLabel();
        spinWidth = new javax.swing.JSpinner();
        spinHeight = new javax.swing.JSpinner();
        btnApply = new javax.swing.JButton();
        lblHeight1 = new javax.swing.JLabel();
        spinFPS = new javax.swing.JSpinner();
        btnApplyToStreams = new javax.swing.JButton();
        tglLockRatio = new javax.swing.JToggleButton();
        sldMasterVolume = new javax.swing.JSlider();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        panPreview = new javax.swing.JPanel();
        btnPreview = new javax.swing.JButton();
        lblHeight2 = new javax.swing.JLabel();
        tglSound = new javax.swing.JToggleButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jslOpacity = new javax.swing.JSlider();
        panelPreviewer = new javax.swing.JPanel();
        lblCurtainPre = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Viewer"));
        setMinimumSize(new java.awt.Dimension(273, 350));
        setPreferredSize(new java.awt.Dimension(273, 400));
        setLayout(new java.awt.BorderLayout());

        tabMixers.setName("tabMixers"); // NOI18N
        tabMixers.setPreferredSize(new java.awt.Dimension(257, 353));

        panMixer.setName("panMixer"); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("truckliststudio/Languages"); // NOI18N
        lblWidth.setText(bundle.getString("WIDTH")); // NOI18N
        lblWidth.setName("lblWidth"); // NOI18N

        lblHeight.setText(bundle.getString("HEIGHT")); // NOI18N
        lblHeight.setName("lblHeight"); // NOI18N

        spinWidth.setName("spinWidth"); // NOI18N
        spinWidth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinWidthStateChanged(evt);
            }
        });

        spinHeight.setName("spinHeight"); // NOI18N

        btnApply.setText(bundle.getString("APPLY")); // NOI18N
        btnApply.setToolTipText("Apply/Reset Mixer Settings");
        btnApply.setName("btnApply"); // NOI18N
        btnApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApplyActionPerformed(evt);
            }
        });

        lblHeight1.setText(bundle.getString("FRAMERATE")); // NOI18N
        lblHeight1.setName("lblHeight1"); // NOI18N

        spinFPS.setName("spinFPS"); // NOI18N

        btnApplyToStreams.setText("Apply to Streams");
        btnApplyToStreams.setToolTipText("Apply Mixer Settings Proportionally to all Streams.");
        btnApplyToStreams.setName("btnApplyToStreams"); // NOI18N
        btnApplyToStreams.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApplyToStreamsActionPerformed(evt);
            }
        });

        tglLockRatio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/LockButton-open_small.png"))); // NOI18N
        tglLockRatio.setText("A/R");
        tglLockRatio.setToolTipText("Lock Mixer Aspect Ratio");
        tglLockRatio.setName("tglLockRatio"); // NOI18N
        tglLockRatio.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/LockButton-open_small.png"))); // NOI18N
        tglLockRatio.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/LockButton-close_small.png"))); // NOI18N
        tglLockRatio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglLockRatioActionPerformed(evt);
            }
        });

        sldMasterVolume.setMajorTickSpacing(50);
        sldMasterVolume.setMinimum(-100);
        sldMasterVolume.setMinorTickSpacing(10);
        sldMasterVolume.setPaintTicks(true);
        sldMasterVolume.setSnapToTicks(true);
        sldMasterVolume.setToolTipText("TS Master Audio Level");
        sldMasterVolume.setValue(0);
        sldMasterVolume.setName("sldMasterVolume"); // NOI18N
        sldMasterVolume.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldMasterVolumeStateChanged(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("0");
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/volume_icon_25.png"))); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        javax.swing.GroupLayout panMixerLayout = new javax.swing.GroupLayout(panMixer);
        panMixer.setLayout(panMixerLayout);
        panMixerLayout.setHorizontalGroup(
            panMixerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panMixerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panMixerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panMixerLayout.createSequentialGroup()
                        .addGroup(panMixerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblHeight1)
                            .addComponent(lblWidth)
                            .addComponent(lblHeight))
                        .addGap(14, 14, 14)
                        .addGroup(panMixerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spinFPS, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panMixerLayout.createSequentialGroup()
                                .addGroup(panMixerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(spinHeight, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                                    .addComponent(spinWidth))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                                .addComponent(tglLockRatio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(panMixerLayout.createSequentialGroup()
                        .addComponent(btnApply, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(btnApplyToStreams, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE))
                    .addGroup(panMixerLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sldMasterVolume, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panMixerLayout.setVerticalGroup(
            panMixerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panMixerLayout.createSequentialGroup()
                .addGroup(panMixerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panMixerLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panMixerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spinWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblWidth))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panMixerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spinHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblHeight)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panMixerLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(tglLockRatio)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panMixerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinFPS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblHeight1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panMixerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(sldMasterVolume, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panMixerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnApplyToStreams, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnApply, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42))
        );

        tabMixers.addTab(bundle.getString("MIXER"), panMixer); // NOI18N

        panPreview.setName("panPreview"); // NOI18N
        panPreview.setPreferredSize(new java.awt.Dimension(242, 279));
        panPreview.setLayout(null);

        btnPreview.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/PreviewButton2.png"))); // NOI18N
        btnPreview.setToolTipText("WebcamStudio Preview Window");
        btnPreview.setMinimumSize(new java.awt.Dimension(0, 0));
        btnPreview.setName("btnPreview"); // NOI18N
        btnPreview.setPreferredSize(new java.awt.Dimension(20, 20));
        btnPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviewActionPerformed(evt);
            }
        });
        panPreview.add(btnPreview);
        btnPreview.setBounds(10, 80, 110, 28);

        lblHeight2.setFont(new java.awt.Font("Ubuntu", 0, 10)); // NOI18N
        lblHeight2.setText(bundle.getString("FAST PREVIEW")); // NOI18N
        lblHeight2.setName("lblHeight2"); // NOI18N
        panPreview.add(lblHeight2);
        lblHeight2.setBounds(90, 46, 90, 12);

        tglSound.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/audio-card.png"))); // NOI18N
        tglSound.setToolTipText("Java Sound AudioSystem Out (Unstable)");
        tglSound.setName("tglSound"); // NOI18N
        tglSound.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglSoundActionPerformed(evt);
            }
        });
        panPreview.add(tglSound);
        tglSound.setBounds(120, 80, 110, 28);

        jLabel1.setFont(new java.awt.Font("Noto Sans", 1, 12)); // NOI18N
        jLabel1.setText("LiVEView");
        jLabel1.setName("jLabel1"); // NOI18N
        panPreview.add(jLabel1);
        jLabel1.setBounds(10, 10, 60, 17);

        jLabel4.setFont(new java.awt.Font("Noto Sans", 1, 12)); // NOI18N
        jLabel4.setText("PreView");
        jLabel4.setName("jLabel4"); // NOI18N
        panPreview.add(jLabel4);
        jLabel4.setBounds(183, 10, 50, 17);

        jslOpacity.setName("jslOpacity"); // NOI18N
        jslOpacity.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jslOpacityStateChanged(evt);
            }
        });
        panPreview.add(jslOpacity);
        jslOpacity.setBounds(8, 22, 225, 30);

        tabMixers.addTab("Viewer Controls", panPreview);

        add(tabMixers, java.awt.BorderLayout.CENTER);
        tabMixers.getAccessibleContext().setAccessibleName(bundle.getString("MIXER")); // NOI18N

        panelPreviewer.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        panelPreviewer.setToolTipText("Click on the video to Hide");
        panelPreviewer.setMaximumSize(new java.awt.Dimension(180, 120));
        panelPreviewer.setMinimumSize(new java.awt.Dimension(180, 120));
        panelPreviewer.setName("panelPreviewer"); // NOI18N
        panelPreviewer.setPreferredSize(new java.awt.Dimension(180, 120));
        panelPreviewer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                panelPreviewerMouseClicked(evt);
            }
        });
        panelPreviewer.setLayout(new java.awt.BorderLayout());

        lblCurtainPre.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/curtain.png"))); // NOI18N
        lblCurtainPre.setToolTipText("Click on the curtain to Unhide");
        lblCurtainPre.setName("lblCurtainPre"); // NOI18N
        lblCurtainPre.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCurtainPreMouseClicked(evt);
            }
        });
        panelPreviewer.add(lblCurtainPre, java.awt.BorderLayout.CENTER);

        add(panelPreviewer, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    public void releaseTglButton() {
    }

    public void applyLoadedMixer() {
        int w = (Integer) spinWidth.getValue();
        int h = (Integer) spinHeight.getValue();
        mixer.stop();
        mixer.setWidth(w);
        mixer.setHeight(h);
        mixer.setRate((Integer) spinFPS.getValue());
        MasterMixer.getInstance().start();
        preMixer.stop();
        preMixer.setWidth(w);
        preMixer.setHeight(h);
        PreviewMixer.getInstance().start();
    }

    private void btnApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApplyActionPerformed
        PrePlayer.getPreInstance(null).stop();
        Tools.sleep(10);
        MasterTracks.getInstance().stopAllStream();
        for (Stream s : streamM) {
            s.updateStatus();
        }
        Tools.sleep(30);
        listenerCPOP.resetButtonsStates(evt);
        TrackPanel.btnStopOnlyStream.doClick();
        int w = (Integer) spinWidth.getValue();
        int h = (Integer) spinHeight.getValue();
        mixer.stop();
        preMixer.stop();
        mixer.setWidth(w);
        preMixer.setWidth(w);
        mixer.setHeight(h);
        preMixer.setHeight(h);
        mixer.setRate((Integer) spinFPS.getValue());
        mixer.start();
        preMixer.start();
        for (Stream s : streamM) {
            String streamName = s.getClass().getName();
//            System.out.println("StreamName: "+streamName);
            s.setRate(mixer.getRate());
            if (streamName.contains("SinkFile") || streamName.contains("SinkUDP")) {
//                System.out.println("Sink New Size: "+w+"x"+h);
                s.setWidth(w);
                s.setHeight(h);
                s.updateStatus();
            }
        }
        listenerCPOP.resetButtonsStates(evt);
        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Mixer Settings Applied");
        ResourceMonitor.getInstance().addMessage(label);
    }//GEN-LAST:event_btnApplyActionPerformed

    private void btnApplyToStreamsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApplyToStreamsActionPerformed
        ArrayList<Stream> allStreams = MasterTracks.getInstance().getStreams();
        int wi = mixer.getWidth();
        int he = mixer.getHeight();
        int rate = mixer.getRate();
        int oldCW;
        int oldCH;
        for (Stream oneStream : allStreams) {
            if (!oneStream.getClass().toString().contains("Sink")) { // Don't Update SinkStreams
//                System.out.println("Processing "+oneStream.getName()+": ...");
                if (oneStream instanceof SourceText) {
                    sTx = (SourceText) oneStream;
                    oldCW = sTx.getTextCW();
                    oldCH = sTx.getTextCH();
                } else if (oneStream instanceof SourceImage) {
                    sImg = (SourceImage) oneStream;
                    oldCW = sImg.getImgCW();
                    oldCH = sImg.getImgCH();
                } else {
                    oldCW = oneStream.getCaptureWidth();
                    oldCH = oneStream.getCaptureHeight();
                }
//                System.out.println("oldCW: "+oldCW);
//                System.out.println("oldCH: "+oldCH);
                int oldW = oneStream.getWidth();
//                System.out.println("oldW: "+oldW);
                int oldH = oneStream.getHeight();
//                System.out.println("oldH: "+oldH);
                int oldX = oneStream.getX();
//                System.out.println("oldX: "+oldX);
                int oldY = oneStream.getY();
//                System.out.println("oldY: "+oldY);
                int newW = (oldW * wi) / oldCW;
//                System.out.println("newW: "+newW);
                int newH = (oldH * he) / oldCH;
//                System.out.println("newH: "+newH);
                int newX = (oldX * wi) / oldCW;
//                System.out.println("newX: "+newX);
                int newY = (oldY * he) / oldCH;
//                System.out.println("newY: "+newY);
                if (oneStream instanceof SourceText) {
                    oneStream.setWidth(newW);
                    oneStream.setHeight(newH);
                    oneStream.setX(newX);
                    oneStream.setY(newY);
                    oneStream.setCaptureWidth(newW);
                    oneStream.setCaptureHeight(newH);
                    oneStream.setRate(rate);
                    sTx.setTextCW(wi);
                    sTx.setTextCH(he);
//                    System.out.println(oneStream.getName()+" UpdateStatus !!!");
                    oneStream.updateStatus();
                    for (SourceTrack ssc : oneStream.getTracks()) {
                        ssc.setWidth(newW);
                        ssc.setHeight(newH);
                        ssc.setX(newX);
                        ssc.setY(newY);
                        ssc.setCapWidth(newW);
                        ssc.setCapHeight(newH);
                    }
                } else if (oneStream instanceof SourceImage) {
                    oneStream.setWidth(newW);
                    oneStream.setHeight(newH);
//                    System.out.println(oneStream.getName()+" Height:"+newH);
                    oneStream.setX(newX);
                    oneStream.setY(newY);
                    oneStream.setCaptureWidth(newW);
                    oneStream.setCaptureHeight(newH);
                    oneStream.setRate(rate);
                    sImg.setImgCW(wi);
                    sImg.setImgCH(he);
//                    System.out.println(oneStream.getName()+" UpdateStatus !!!");
                    oneStream.updateStatus();
                    for (SourceTrack ssc : oneStream.getTracks()) {
                        ssc.setWidth(newW);
                        ssc.setHeight(newH);
                        ssc.setX(newX);
                        ssc.setY(newY);
                        ssc.setCapWidth(newW);
                        ssc.setCapHeight(newH);
                    }
                } else {
                    oneStream.setWidth(newW);
                    oneStream.setHeight(newH);
                    oneStream.setX(newX);
                    oneStream.setY(newY);
                    oneStream.setCaptureWidth(wi);
                    oneStream.setCaptureHeight(he);
                    oneStream.setRate(rate);
//                    System.out.println(oneStream.getName()+" UpdateStatus !!!");
                    oneStream.updateStatus();
                    for (SourceTrack ssc : oneStream.getTracks()) {
                        ssc.setWidth(newW);
                        ssc.setHeight(newH);
                        ssc.setX(newX);
                        ssc.setY(newY);
                        ssc.setCapWidth(wi);
                        ssc.setCapHeight(he);
                    }
                }
            }
        }
        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Mixer Settings to All Streams");
        ResourceMonitor.getInstance().addMessage(label);
    }//GEN-LAST:event_btnApplyToStreamsActionPerformed

    private void tglLockRatioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglLockRatioActionPerformed
        if (tglLockRatio.isSelected()) {
            spinHeight.setEnabled(false);
            lockRatio = true;
        } else {
            spinHeight.setEnabled(true);
            lockRatio = false;
        }
    }//GEN-LAST:event_tglLockRatioActionPerformed

    private void spinWidthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinWidthStateChanged
        int oldW = mixer.getWidth();
        int oldH = mixer.getHeight();
        int w = (Integer) spinWidth.getValue();
        int h;
        if (tglLockRatio.isSelected()) {
            h = (oldH * w) / oldW;
            spinHeight.setValue(h);
        }
    }//GEN-LAST:event_spinWidthStateChanged

    private void tglSoundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglSoundActionPerformed
        if (tglSound.isSelected()) {
            try {
                prePlayer.play();
            } catch (LineUnavailableException ex) {
                Logger.getLogger(MasterPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            prePlayer.stop();
        }
    }//GEN-LAST:event_tglSoundActionPerformed

    private void btnPreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviewActionPerformed
        btnPreview.setEnabled(false);
        TSPreview window = new TSPreview();
        TSPreviewScreen frame = new TSPreviewScreen(preViewer);
        window.add(frame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        panelPreviewer.remove(preViewer);
        lblCurtainPre.setOpaque(true);
        lblCurtainPre.setVisible(true);
        panelPreviewer.add(lblCurtainPre);
        try {
            frame.setSelected(true);
            frame.setMaximum(true);
        } catch (PropertyVetoException ex) {
        }

        window.setLocationRelativeTo(TrucklistStudio.cboAnimations);
        Dimension d = new Dimension(640, 360);
        window.setSize(d);
        window.setAlwaysOnTop(true);
        window.setVisible(true);
    }//GEN-LAST:event_btnPreviewActionPerformed

    private void lblCurtainPreMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCurtainPreMouseClicked
        lblCurtainPre.setVisible(false);
        preViewer.setOpaque(true);
        panelPreviewer.add(preViewer, BorderLayout.CENTER);
        prePlayer = PrePlayer.getPreInstance(preViewer);
        this.repaint();
        this.revalidate();
    }//GEN-LAST:event_lblCurtainPreMouseClicked

    private void panelPreviewerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelPreviewerMouseClicked
        panelPreviewer.remove(preViewer);
        lblCurtainPre.setOpaque(true);
        lblCurtainPre.setVisible(true);
        panelPreviewer.add(lblCurtainPre);
        this.repaint();
        this.revalidate();
    }//GEN-LAST:event_panelPreviewerMouseClicked

    private void jslOpacityStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jslOpacityStateChanged
        opacity = jslOpacity.getValue();
    }//GEN-LAST:event_jslOpacityStateChanged

    private void sldMasterVolumeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldMasterVolumeStateChanged
        Object value = sldMasterVolume.getValue();
        float v = 0;
        String volText = "0";
        if (value instanceof Float) {
            v = (Float) value;
        } else if (value instanceof Integer) {
            v = ((Number) value).floatValue();
            volText = value.toString();
        }
        jLabel2.setText(volText);
        masterVolume = v / 100f;
    }//GEN-LAST:event_sldMasterVolumeStateChanged

    /**
     *
     * @param evt
     */
    @Override
    public void resetPreviewer(ActionEvent evt) {
        lblCurtainPre.setVisible(false);
        preViewer.setOpaque(true);
        panelPreviewer.add(preViewer, BorderLayout.CENTER);
        prePlayer = PrePlayer.getPreInstance(preViewer);
        btnPreview.setEnabled(true);
        this.repaint();
        this.revalidate();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnApply;
    private javax.swing.JButton btnApplyToStreams;
    private javax.swing.JButton btnPreview;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSlider jslOpacity;
    private javax.swing.JLabel lblCurtainPre;
    private javax.swing.JLabel lblHeight;
    private javax.swing.JLabel lblHeight1;
    private javax.swing.JLabel lblHeight2;
    private javax.swing.JLabel lblWidth;
    private javax.swing.JPanel panMixer;
    private javax.swing.JPanel panPreview;
    private javax.swing.JPanel panelPreviewer;
    private javax.swing.JSlider sldMasterVolume;
    public static javax.swing.JSpinner spinFPS;
    public static javax.swing.JSpinner spinHeight;
    public static javax.swing.JSpinner spinWidth;
    private javax.swing.JTabbedPane tabMixers;
    private javax.swing.JToggleButton tglLockRatio;
    private javax.swing.JToggleButton tglSound;
    // End of variables declaration//GEN-END:variables

    @Override
    public void newFrame(Frame frame) {
        prePlayer.addLiveFrame(frame);
        liveImg = frame.getImage();
    }

//    BufferedImage deepCopy(BufferedImage bi) {
//        if (bi != null) {
//            ColorModel cm = bi.getColorModel();
//            boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
//            WritableRaster raster = bi.copyData(null);
//            BufferedImage temp = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
//            return temp;
//        } else {
//            return null;
//        }
//    }
    @Override
    public void newPreFrame(Frame frame) {
        BufferedImage img = cloneImage(frame.getImage());
        BufferedImage lImg = null;
        if (liveImg != null) {
            lImg = cloneImage(liveImg);
        }
        int w = img.getWidth();
        int h = img.getHeight();
        if (lImg != null) {
            Graphics2D buffer = lImg.createGraphics();
            buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, opacity / 100F));
            buffer.drawImage(img, 0, 0, null);
            buffer.dispose();
            frame.setImage(lImg);
        }
        prePlayer.addFrame(frame);
    }

    @Override
    public void requestReset() {
//        System.out.println("Apply buttton pressed ...");
        btnApply.doClick();
    }

    @Override
    public void resetButtonsStates(ActionEvent evt) {
        // nothing here
    }

    @Override
    public void requestStart() {
        // nothing here
    }

    @Override
    public void requestStop() {
        // nothing here
    }
}
