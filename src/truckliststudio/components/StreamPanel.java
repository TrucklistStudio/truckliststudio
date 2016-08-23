/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * StreamPanel.java
 *
 * Created on 4-Apr-2012, 4:07:51 PM
 */
package truckliststudio.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.Painter;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIDefaults;
import static truckliststudio.TrucklistStudio.theme;
import static truckliststudio.TrucklistStudio.wsDistroWatch;
import static truckliststudio.components.TrackPanel.lblPlayingTrack;
import static truckliststudio.components.TrackPanel.listTracks;
import truckliststudio.mixers.MasterMixer;
import truckliststudio.streams.SourceImage;
import truckliststudio.streams.SourceImageGif;
import truckliststudio.streams.SourceMovie;
import truckliststudio.streams.SourceMusic;
import truckliststudio.streams.Stream;
import truckliststudio.util.BackEnd;

/**
 *
 * @author patrick (modified by karl)
 */
public class StreamPanel extends javax.swing.JPanel implements Stream.Listener { //, StreamDesktop.Listener 

    Stream stream = null;
    private float volume = 0;
    private float vol = 0;
    BufferedImage icon = null;
    boolean lockRatio = false;
    boolean muted = false;
    int oldW;
    int oldH;
    String distro = wsDistroWatch();
    boolean ffmpeg = BackEnd.ffmpegDetected();
    boolean avconv = BackEnd.avconvDetected();
    JLabel titleLabel = new JLabel();
    JTabbedPane JTp;
    Color sActiveLblCol = Color.GREEN;
    Color sStopLblCol = Color.WHITE;
    int tabIndex = 0;

    public interface Listener {

        public void startItsTrack(String name);

        public void stopItsTrack();

        public void selectedSource(Stream source);

        public void closeSource(String name);
    }

    static Listener listenerTP = null;

    public static void setListenerTP(Listener l) {
        listenerTP = l;
    }

    static Listener listenerTS = null;

    public static void setListenerTS(Listener l) {
        listenerTS = l;
    }

    /**
     * Creates new form StreamPanel
     *
     * @param stream
     */
    public StreamPanel(Stream stream) {

        initComponents();

        oldW = stream.getWidth();
        oldH = stream.getHeight();
        volume = stream.getVolume();
        vol = stream.getVolume();

        if (theme.equals("Dark")) {
            sStopLblCol = Color.WHITE;
            sActiveLblCol = Color.GREEN;
        } else {
            sStopLblCol = Color.BLACK;
            sActiveLblCol = Color.GREEN.darker();
        }

        try {
            icon = ImageIO.read(getClass().getResource("/truckliststudio/resources/tango/speaker4.png"));
        } catch (IOException ex) {
            Logger.getLogger(StreamPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        UIDefaults sliderDefaults = new UIDefaults();
        sliderDefaults.put("Slider.paintValue", true);
        sliderDefaults.put("Slider.thumbHeight", 13);
        sliderDefaults.put("Slider.thumbWidth", 13);

        sliderDefaults.put("Slider:SliderThumb.backgroundPainter", new Painter() {

            @Override
            public void paint(Graphics2D g, Object object, int w, int h) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.drawImage(icon, 0, -5, null);
            }

        });

        sliderDefaults.put("Slider:SliderTrack.backgroundPainter", new Painter() {

            @Override
            public void paint(Graphics2D g, Object object, int w, int h) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(new BasicStroke(2f));
                g.setColor(Color.WHITE);
                g.drawRoundRect(0, 2, w - 1, 1, 1, 1);
            }

        });

        jSlSpinV.putClientProperty("JComponent.sizeVariant", "small");
        jSlSpinV.putClientProperty("Nimbus.Overrides", sliderDefaults);
        jSlSpinV.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
        jSlSpinV.setOpaque(true);

        spinVolume.setVisible(false);
        jSlSpinV.setVisible(stream.hasAudio());
        labelVol.setVisible(stream.hasAudio());
        this.stream = stream;
        spinX.setValue(stream.getX());
        spinY.setValue(stream.getY());
        spinW.setValue(stream.getWidth());
        spinH.setValue(stream.getHeight());
        spinOpacity.setModel(new SpinnerNumberModel(100, 0, 100, 1));
        spinOpacity.setValue(stream.getOpacity());
        spinVolume.setModel(new SpinnerNumberModel(50, 0, 300, 1));
        spinVolume.setValue(stream.getVolume() * 100);
        jSlSpinV.setEnabled(stream.hasAudio());
        spinZOrder.setValue(stream.getZOrder());
        spinVDelay.setValue(stream.getVDelay());
        spinADelay.setValue(stream.getADelay());
        spinVDelay.setEnabled(stream.hasVideo());
        jSlSpinVD.setEnabled(stream.hasVideo());
        spinADelay.setEnabled(stream.hasAudio());
        jSlSpinAD.setEnabled(stream.hasAudio());
        spinSeek.setValue(stream.getSeek());
        spinSeek.setVisible(stream.needSeekCTRL());
        jSlSpinSeek.setVisible(stream.needSeekCTRL());
        labelSeek.setVisible(stream.needSeekCTRL());
        jlbDuration.setText("Play Time " + stream.getStreamTime());

        stream.setListener(this);
        if (!stream.hasVideo()) {
            spinX.setEnabled(false);
            jSlSpinX.setEnabled(false);
            spinY.setEnabled(false);
            jSlSpinY.setEnabled(false);
            spinW.setEnabled(false);
            jSlSpinW.setEnabled(false);
            spinH.setEnabled(false);
            jSlSpinH.setEnabled(false);
            spinOpacity.setEnabled(false);
            jSlSpinO.setEnabled(false);
        }
        if (stream instanceof SourceMusic) {
            tglAudio.setVisible(false);
            tglPause.setVisible(true);
            this.add(tglVideo, new org.netbeans.lib.awtextra.AbsoluteConstraints(57, 120, 30, 20));
        } else if (stream instanceof SourceMovie) {
            if (stream.isOnlyVideo()) {
                tglAudio.setVisible(false);
                tglVideo.setVisible(false);
                this.add(tglActiveStream, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 120, 80, 20));
            } else {
                tglAudio.setVisible(true);
                tglVideo.setVisible(false);
            }
        } else {
            jlbDuration.setText(" ");
            jlbDuration.setVisible(jSlSpinV.isVisible());
            tglAudio.setVisible(false);
            tglPause.setVisible(false);
            tglVideo.setVisible(false);
            this.add(tglActiveStream, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 28, 110, 20));
        }
        tglVideo.setSelected(stream.isOnlyAudio());
        tglAudio.setSelected(!stream.hasAudio());
        if (tglAudio.isSelected()) {
            tglAudio.setEnabled(true);
            tglVideo.setEnabled(false);
        } else if (tglVideo.isSelected()) {
            tglVideo.setEnabled(true);
            tglAudio.setEnabled(false);
        } else {
            tglAudio.setEnabled(true);
            tglVideo.setEnabled(true);
        }
        if (stream.getLoaded()) {
            if (ffmpeg && !avconv) {
                switch (stream.getComm()) {
                    case "AV":
                        tglFFmpeg.setSelected(true);
                        stream.setComm("FF");
                        stream.setBackFF(true);
                        tglAVconv.setEnabled(false);
                        tglGst.setSelected(false);
                        break;
                    case "GS":
                        tglGst.setSelected(true);
                        stream.setComm("GS");
                        tglAVconv.setEnabled(false);
                        break;
                    case "FF":
                        tglFFmpeg.setSelected(true);
                        stream.setComm("FF");
                        stream.setBackFF(true);
                        tglAVconv.setEnabled(false);
                        tglGst.setSelected(false);
                        break;
                    default:
                        tglFFmpeg.setSelected(true);
                        stream.setComm("FF");
                        stream.setBackFF(true);
                        tglAVconv.setEnabled(false);
                        tglGst.setSelected(false);
                        break;
                }

            } else if (ffmpeg && avconv) {
                switch (stream.getComm()) {
                    case "AV":
                        tglAVconv.setSelected(true);
                        stream.setComm("AV");
                        tglGst.setSelected(false);
                        break;
                    case "GS":
                        tglGst.setSelected(true);
                        stream.setComm("GS");
                        tglAVconv.setSelected(false);
                        break;
                    case "FF":
                        tglFFmpeg.setSelected(true);
                        stream.setComm("FF");
                        stream.setBackFF(true);
                        tglAVconv.setSelected(false);
                        tglGst.setSelected(false);
                        break;
                    default:
                        tglAVconv.setSelected(true);
                        stream.setComm("AV");
                        tglGst.setSelected(false);
                        tglFFmpeg.setSelected(false);
                        break;
                }
            } else if (!ffmpeg && avconv) {
                switch (stream.getComm()) {
                    case "AV":
                        tglAVconv.setSelected(true);
                        stream.setComm("AV");
                        tglGst.setSelected(false);
                        tglFFmpeg.setEnabled(false);
                        break;
                    case "GS":
                        tglGst.setSelected(true);
                        stream.setComm("GS");
                        tglAVconv.setSelected(false);
                        tglFFmpeg.setEnabled(false);
                        break;
                    case "FF":
                        tglAVconv.setSelected(true);
                        stream.setComm("AV");
                        tglGst.setSelected(false);
                        tglFFmpeg.setEnabled(false);
                        break;
                    default:
                        tglAVconv.setSelected(true);
                        stream.setComm("AV");
                        tglGst.setSelected(false);
                        tglFFmpeg.setEnabled(false);
                        break;
                }
            }
            this.revalidate();
        } else if (distro.toLowerCase().equals("ubuntu")) {
            if (ffmpeg && !avconv) {
                tglFFmpeg.setSelected(true);
                stream.setComm("FF");
                stream.setBackFF(true);
                tglAVconv.setEnabled(false);
                tglGst.setSelected(false);
            } else if (ffmpeg && avconv) {
                tglAVconv.setSelected(true);
                stream.setComm("AV");
                tglGst.setSelected(false);
                tglFFmpeg.setSelected(false);
            } else if (!ffmpeg && avconv) {
                tglAVconv.setSelected(true);
                stream.setComm("AV");
                tglGst.setSelected(false);
                tglFFmpeg.setEnabled(false);
            }
        } else if (distro.toLowerCase().equals("windows")) {
            stream.setComm("FF");
            stream.setBackFF(true);
            tglFFmpeg.setSelected(true);
            tglFFmpeg.setEnabled(false);
            tglAVconv.setVisible(false);
            tglGst.setVisible(false);
        } else {
            tglAVconv.setEnabled(false);
            stream.setComm("FF");
            stream.setBackFF(true);
            tglGst.setSelected(false);
            tglFFmpeg.setSelected(true);
        } //                tglLoop.setVisible(false);
        if (stream instanceof SourceImageGif || stream instanceof SourceImage) {
            tglAVconv.setVisible(false);
            tglFFmpeg.setVisible(false);
            tglGst.setVisible(false);
            tglLoop.setVisible(false);
            labelVD.setVisible(false);
            spinVDelay.setVisible(false);
            jSlSpinVD.setVisible(false);
            labelAD.setVisible(false);
            spinADelay.setVisible(false);
            jSlSpinAD.setVisible(false);
            lblBE.setVisible(false);
        }
        if (stream instanceof SourceMovie || stream instanceof SourceMusic) {
            tglLoop.setSelected(stream.getLoop());
        }

    }

    public void setParent() {
        JTp = (JTabbedPane) this.getParent();
        int totalTabs = JTp.getTabCount();
        String sName = stream.getName();
        for (int i = 0; i < totalTabs; i++) {
            String titleAt = JTp.getTitleAt(i).replace("<html><body><table width='20'>", "").replace("</table></body></html>", "").replace("<span></span>", "");
            if (sName.equals(titleAt)) {
                tabIndex = i;
                break;
            }
        }

        String t = JTp.getTitleAt(tabIndex);
        titleLabel.setText(t);
        int fontSize = titleLabel.getFont().getSize();
        Font font = new Font(titleLabel.getFont().getName(), Font.BOLD, fontSize);
        titleLabel.setFont(font);
        JTp.setTabComponentAt(tabIndex, titleLabel);
    }

    public ImageIcon getIcon() {
        ImageIcon icon = null;
        if (stream.getPreview() != null) {
            icon = new ImageIcon(stream.getPreview().getScaledInstance(32, 32, BufferedImage.SCALE_FAST));
        }

        return icon;
    }

    public void remove() {
        stream.stop();
        stream = null;

    }

    @Override
    public void sourceUpdated(Stream stream) {

        int mixerW = MasterMixer.getInstance().getWidth();
        int mixerH = MasterMixer.getInstance().getHeight();

        if (jSlSpinX.getValue() > mixerW) {
            spinX.setValue(stream.getX());
        }
        jSlSpinX.setMaximum(mixerW);

        if (jSlSpinX.getValue() < -mixerW) {
            spinX.setValue(stream.getX());
        }
        jSlSpinX.setMinimum(-mixerW);

        if (jSlSpinY.getValue() > mixerH) {
            spinY.setValue(stream.getY());
        }
        jSlSpinY.setMaximum(mixerH);

        if (jSlSpinY.getValue() < -mixerH) {
            spinY.setValue(stream.getY());
        }
        jSlSpinY.setMinimum(-mixerH);

        if (jSlSpinW.getValue() > mixerW) {
            spinW.setValue(stream.getWidth());
        }
        jSlSpinW.setMaximum(mixerW);

        if (jSlSpinH.getValue() > mixerH) {
            spinH.setValue(stream.getHeight());
        }
        jSlSpinH.setMaximum(mixerH);

        spinX.setValue(stream.getX());
        spinY.setValue(stream.getY());
        spinH.setValue(stream.getHeight());
        spinW.setValue(stream.getWidth());
        spinOpacity.setValue(stream.getOpacity());
        spinVolume.setValue(stream.getVolume() * 100);
        spinZOrder.setValue(stream.getZOrder());
        tglActiveStream.setSelected(stream.isPlaying());
        boolean seek = stream.needSeekCTRL();
        labelSeek.setVisible(seek);
        spinSeek.setVisible(seek);
        jSlSpinSeek.setVisible(seek);
        if (stream.isPlaying()) {

            tglPause.setSelected(stream.getisPaused());

            spinVDelay.setEnabled(false);
            jSlSpinVD.setEnabled(false);
            spinADelay.setEnabled(false);
            jSlSpinAD.setEnabled(false);
            spinSeek.setEnabled(false);
            jSlSpinSeek.setEnabled(false);
            tglAudio.setEnabled(false);
            tglPreview.setEnabled(false);
            tglVideo.setEnabled(false);
            spinVolume.setEnabled(stream.hasAudio());
            jSlSpinV.setEnabled(stream.hasAudio());
            tglPause.setEnabled(true);

            titleLabel.setForeground(sActiveLblCol);
            JTp.setSelectedIndex(tabIndex);
            this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, sActiveLblCol));
        } else {

            tglPause.setSelected(false);
            stream.setisPaused(false);
            titleLabel.setForeground(sStopLblCol);
            spinVDelay.setEnabled(stream.hasVideo());
            jSlSpinVD.setEnabled(stream.hasVideo());
            spinADelay.setEnabled(stream.hasAudio());
            jSlSpinAD.setEnabled(stream.hasAudio());
            spinSeek.setEnabled(seek);
            jSlSpinSeek.setEnabled(seek);
            tglPreview.setEnabled(true);
            if (tglAudio.isSelected()) {
                tglAudio.setEnabled(true);
            } else if (tglVideo.isSelected()) {
                tglVideo.setEnabled(true);
            } else {
                tglAudio.setEnabled(true);
                tglVideo.setEnabled(true);
            }
            spinVolume.setEnabled(stream.hasAudio());
            jSlSpinV.setEnabled(stream.hasAudio());
            tglPause.setSelected(false);
            tglPause.setEnabled(false);
            this.setBorder(BorderFactory.createEtchedBorder());
        }
        tglActiveStream.revalidate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        spinX = new javax.swing.JSpinner();
        spinY = new javax.swing.JSpinner();
        spinW = new javax.swing.JSpinner();
        spinH = new javax.swing.JSpinner();
        spinOpacity = new javax.swing.JSpinner();
        spinVolume = new javax.swing.JSpinner();
        tglActiveStream = new javax.swing.JToggleButton();
        spinZOrder = new javax.swing.JSpinner();
        labelX = new javax.swing.JLabel();
        labelY = new javax.swing.JLabel();
        labelW = new javax.swing.JLabel();
        labelH = new javax.swing.JLabel();
        labelO = new javax.swing.JLabel();
        labelZ = new javax.swing.JLabel();
        spinVDelay = new javax.swing.JSpinner();
        spinADelay = new javax.swing.JSpinner();
        spinSeek = new javax.swing.JSpinner();
        labelSeek = new javax.swing.JLabel();
        jSlSpinX = new javax.swing.JSlider();
        jSlSpinY = new javax.swing.JSlider();
        jSlSpinW = new javax.swing.JSlider();
        jSlSpinH = new javax.swing.JSlider();
        jSlSpinO = new javax.swing.JSlider();
        jSlSpinVD = new javax.swing.JSlider();
        jSlSpinAD = new javax.swing.JSlider();
        jSlSpinSeek = new javax.swing.JSlider();
        jSlSpinZOrder = new javax.swing.JSlider();
        labelVD = new javax.swing.JLabel();
        labelAD = new javax.swing.JLabel();
        tglPause = new javax.swing.JToggleButton();
        tglAudio = new javax.swing.JToggleButton();
        jcbLockAR = new javax.swing.JCheckBox();
        tglVideo = new javax.swing.JToggleButton();
        tglPreview = new javax.swing.JToggleButton();
        jSlSpinV = new javax.swing.JSlider();
        jlbDuration = new javax.swing.JLabel();
        labelVol = new javax.swing.JLabel();
        tglGst = new javax.swing.JToggleButton();
        tglAVconv = new javax.swing.JToggleButton();
        tglFFmpeg = new javax.swing.JToggleButton();
        tglLoop = new javax.swing.JToggleButton();
        lblBE = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        setMaximumSize(new java.awt.Dimension(298, 367));
        setMinimumSize(new java.awt.Dimension(298, 367));
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(298, 367));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
        });

        spinX.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinX.setName("spinX"); // NOI18N
        spinX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinXStateChanged(evt);
            }
        });

        spinY.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinY.setName("spinY"); // NOI18N
        spinY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinYStateChanged(evt);
            }
        });

        spinW.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinW.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        spinW.setInputVerifier(jSlSpinW.getInputVerifier());
        spinW.setName("spinW"); // NOI18N
        spinW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinWStateChanged(evt);
            }
        });

        spinH.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinH.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        spinH.setName("spinH"); // NOI18N
        spinH.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinHStateChanged(evt);
            }
        });

        spinOpacity.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinOpacity.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        spinOpacity.setName("spinOpacity"); // NOI18N
        spinOpacity.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinOpacityStateChanged(evt);
            }
        });

        spinVolume.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinVolume.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        spinVolume.setName("spinVolume"); // NOI18N
        spinVolume.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinVolumeStateChanged(evt);
            }
        });

        tglActiveStream.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-playback-start.png"))); // NOI18N
        tglActiveStream.setName("tglActiveStream"); // NOI18N
        tglActiveStream.setRolloverEnabled(false);
        tglActiveStream.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglActiveStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglActiveStreamActionPerformed(evt);
            }
        });

        spinZOrder.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinZOrder.setName("spinZOrder"); // NOI18N
        spinZOrder.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinZOrderStateChanged(evt);
            }
        });

        labelX.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("truckliststudio/Languages"); // NOI18N
        labelX.setText(bundle.getString("X")); // NOI18N
        labelX.setName("labelX"); // NOI18N

        labelY.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelY.setText(bundle.getString("Y")); // NOI18N
        labelY.setName("labelY"); // NOI18N

        labelW.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelW.setText(bundle.getString("WIDTH")); // NOI18N
        labelW.setName("labelW"); // NOI18N

        labelH.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelH.setText(bundle.getString("HEIGHT")); // NOI18N
        labelH.setName("labelH"); // NOI18N

        labelO.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelO.setText(bundle.getString("OPACITY")); // NOI18N
        labelO.setName("labelO"); // NOI18N

        labelZ.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelZ.setText(bundle.getString("LAYER")); // NOI18N
        labelZ.setMaximumSize(new java.awt.Dimension(30, 10));
        labelZ.setMinimumSize(new java.awt.Dimension(30, 10));
        labelZ.setName("labelZ"); // NOI18N
        labelZ.setPreferredSize(new java.awt.Dimension(30, 10));

        spinVDelay.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinVDelay.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        spinVDelay.setToolTipText("Milliseconds");
        spinVDelay.setName("spinVDelay"); // NOI18N
        spinVDelay.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinVDelayStateChanged(evt);
            }
        });

        spinADelay.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinADelay.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        spinADelay.setToolTipText("Milliseconds");
        spinADelay.setName("spinADelay"); // NOI18N
        spinADelay.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinADelayStateChanged(evt);
            }
        });

        spinSeek.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        spinSeek.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        spinSeek.setName("spinSeek"); // NOI18N
        spinSeek.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinSeekStateChanged(evt);
            }
        });

        labelSeek.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelSeek.setText(bundle.getString("SEEK")); // NOI18N
        labelSeek.setMaximumSize(new java.awt.Dimension(30, 10));
        labelSeek.setMinimumSize(new java.awt.Dimension(30, 10));
        labelSeek.setName("labelSeek"); // NOI18N
        labelSeek.setPreferredSize(new java.awt.Dimension(30, 10));

        jSlSpinX.setMaximum(MasterMixer.getInstance().getWidth());
        jSlSpinX.setMinimum(- MasterMixer.getInstance().getWidth());
        jSlSpinX.setValue(0);
        jSlSpinX.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinX.setName("jSlSpinX"); // NOI18N
        jSlSpinX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinXStateChanged(evt);
            }
        });

        jSlSpinY.setMaximum(MasterMixer.getInstance().getHeight());
        jSlSpinY.setMinimum(- MasterMixer.getInstance().getHeight());
        jSlSpinY.setValue(0);
        jSlSpinY.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinY.setInverted(true);
        jSlSpinY.setName("jSlSpinY"); // NOI18N
        jSlSpinY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinYStateChanged(evt);
            }
        });

        jSlSpinW.setMajorTickSpacing(10);
        jSlSpinW.setMaximum(MasterMixer.getInstance().getWidth());
        jSlSpinW.setMinimum(1);
        jSlSpinW.setMinorTickSpacing(1);
        jSlSpinW.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinW.setName("jSlSpinW"); // NOI18N
        jSlSpinW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinWStateChanged(evt);
            }
        });

        jSlSpinH.setMajorTickSpacing(10);
        jSlSpinH.setMaximum(MasterMixer.getInstance().getHeight());
        jSlSpinH.setMinimum(1);
        jSlSpinH.setMinorTickSpacing(1);
        jSlSpinH.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinH.setName("jSlSpinH"); // NOI18N
        jSlSpinH.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinHStateChanged(evt);
            }
        });

        jSlSpinO.setValue(100);
        jSlSpinO.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinO.setName("jSlSpinO"); // NOI18N
        jSlSpinO.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinOStateChanged(evt);
            }
        });

        jSlSpinVD.setMaximum(10000);
        jSlSpinVD.setPaintLabels(true);
        jSlSpinVD.setValue(0);
        jSlSpinVD.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinVD.setName("jSlSpinVD"); // NOI18N
        jSlSpinVD.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinVDStateChanged(evt);
            }
        });

        jSlSpinAD.setMaximum(10000);
        jSlSpinAD.setPaintLabels(true);
        jSlSpinAD.setValue(0);
        jSlSpinAD.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinAD.setName("jSlSpinAD"); // NOI18N
        jSlSpinAD.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinADStateChanged(evt);
            }
        });

        jSlSpinSeek.setMaximum(10000);
        jSlSpinSeek.setPaintLabels(true);
        jSlSpinSeek.setValue(0);
        jSlSpinSeek.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinSeek.setName("jSlSpinSeek"); // NOI18N
        jSlSpinSeek.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinSeekStateChanged(evt);
            }
        });

        jSlSpinZOrder.setMajorTickSpacing(10);
        jSlSpinZOrder.setMaximum(10);
        jSlSpinZOrder.setMinimum(-10);
        jSlSpinZOrder.setMinorTickSpacing(1);
        jSlSpinZOrder.setPaintTicks(true);
        jSlSpinZOrder.setSnapToTicks(true);
        jSlSpinZOrder.setValue(0);
        jSlSpinZOrder.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinZOrder.setName("jSlSpinZOrder"); // NOI18N
        jSlSpinZOrder.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinZOrderStateChanged(evt);
            }
        });

        labelVD.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelVD.setText(bundle.getString("VIDEO_DELAY")); // NOI18N
        labelVD.setName("labelVD"); // NOI18N

        labelAD.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        labelAD.setText(bundle.getString("AUDIO_DELAY")); // NOI18N
        labelAD.setName("labelAD"); // NOI18N

        tglPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-playback-pause.png"))); // NOI18N
        tglPause.setEnabled(false);
        tglPause.setName("tglPause"); // NOI18N
        tglPause.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-playback-pause.png"))); // NOI18N
        tglPause.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-playback-play.png"))); // NOI18N
        tglPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglPauseActionPerformed(evt);
            }
        });

        tglAudio.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        tglAudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/audio-volume-muted.png"))); // NOI18N
        tglAudio.setToolTipText("No Audio Switch (Force Only Video Source)");
        tglAudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglAudio.setMaximumSize(new java.awt.Dimension(40, 32));
        tglAudio.setMinimumSize(new java.awt.Dimension(0, 0));
        tglAudio.setName("tglAudio"); // NOI18N
        tglAudio.setPreferredSize(new java.awt.Dimension(29, 53));
        tglAudio.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/audio-volume-muted.png"))); // NOI18N
        tglAudio.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/audio-volume-selected-muted.png"))); // NOI18N
        tglAudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglAudioActionPerformed(evt);
            }
        });

        jcbLockAR.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        jcbLockAR.setToolTipText("Lock A/R");
        jcbLockAR.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jcbLockAR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/LockButton-open_small.png"))); // NOI18N
        jcbLockAR.setName("jcbLockAR"); // NOI18N
        jcbLockAR.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/LockButton-open_small.png"))); // NOI18N
        jcbLockAR.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/LockButton-close_small.png"))); // NOI18N
        jcbLockAR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbLockARActionPerformed(evt);
            }
        });

        tglVideo.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        tglVideo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/edit-delete.png"))); // NOI18N
        tglVideo.setToolTipText("No Video Switch (Force Only Audio Source)");
        tglVideo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglVideo.setMaximumSize(new java.awt.Dimension(40, 32));
        tglVideo.setMinimumSize(new java.awt.Dimension(26, 30));
        tglVideo.setName("tglVideo"); // NOI18N
        tglVideo.setPreferredSize(new java.awt.Dimension(20, 20));
        tglVideo.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/edit-delete.png"))); // NOI18N
        tglVideo.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/edit-delete-selected.png"))); // NOI18N
        tglVideo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglVideoActionPerformed(evt);
            }
        });

        tglPreview.setFont(new java.awt.Font("Ubuntu", 0, 5)); // NOI18N
        tglPreview.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/PreviewButton2.png"))); // NOI18N
        tglPreview.setToolTipText("Preview Mode");
        tglPreview.setName("tglPreview"); // NOI18N
        tglPreview.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/PreviewButton2.png"))); // NOI18N
        tglPreview.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/PreviewButtonSelected4.png"))); // NOI18N
        tglPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglPreviewActionPerformed(evt);
            }
        });

        jSlSpinV.setForeground(java.awt.Color.white);
        jSlSpinV.setMaximum(200);
        jSlSpinV.setToolTipText("Volume Control - Double Click to Mute/Unmute");
        jSlSpinV.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSlSpinV.setMaximumSize(new java.awt.Dimension(110, 30));
        jSlSpinV.setMinimumSize(new java.awt.Dimension(110, 30));
        jSlSpinV.setName("jSlSpinV"); // NOI18N
        jSlSpinV.setPreferredSize(new java.awt.Dimension(110, 25));
        jSlSpinV.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlSpinVStateChanged(evt);
            }
        });
        jSlSpinV.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jSlSpinVFocusLost(evt);
            }
        });
        jSlSpinV.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jSlSpinVMouseClicked(evt);
            }
        });

        jlbDuration.setBackground(java.awt.Color.black);
        jlbDuration.setFont(new java.awt.Font("Ubuntu Mono", 0, 12)); // NOI18N
        jlbDuration.setForeground(java.awt.Color.white);
        jlbDuration.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlbDuration.setText("Sec.");
        jlbDuration.setName("jlbDuration"); // NOI18N
        jlbDuration.setOpaque(true);

        labelVol.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/volume_icon_25.png"))); // NOI18N
        labelVol.setName("labelVol"); // NOI18N

        tglGst.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/gstreamer.png"))); // NOI18N
        tglGst.setToolTipText("Use GStreamer Backend.");
        tglGst.setFocusable(false);
        tglGst.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglGst.setMaximumSize(new java.awt.Dimension(29, 28));
        tglGst.setMinimumSize(new java.awt.Dimension(25, 25));
        tglGst.setName("tglGst"); // NOI18N
        tglGst.setPreferredSize(new java.awt.Dimension(28, 29));
        tglGst.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/gstreamer.png"))); // NOI18N
        tglGst.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/gstreamerSelected.png"))); // NOI18N
        tglGst.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tglGst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglGstActionPerformed(evt);
            }
        });

        tglAVconv.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/FFmpeg.png"))); // NOI18N
        tglAVconv.setToolTipText("Use Libav Backend.");
        tglAVconv.setFocusable(false);
        tglAVconv.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglAVconv.setMaximumSize(new java.awt.Dimension(29, 28));
        tglAVconv.setMinimumSize(new java.awt.Dimension(25, 25));
        tglAVconv.setName("tglAVconv"); // NOI18N
        tglAVconv.setPreferredSize(new java.awt.Dimension(28, 29));
        tglAVconv.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/FFmpeg.png"))); // NOI18N
        tglAVconv.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/FFmpegSelected.png"))); // NOI18N
        tglAVconv.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tglAVconv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglAVconvActionPerformed(evt);
            }
        });

        tglFFmpeg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/FFmpeg.png"))); // NOI18N
        tglFFmpeg.setToolTipText("Use FFmpeg Backend.");
        tglFFmpeg.setFocusable(false);
        tglFFmpeg.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglFFmpeg.setMaximumSize(new java.awt.Dimension(29, 28));
        tglFFmpeg.setMinimumSize(new java.awt.Dimension(25, 25));
        tglFFmpeg.setName("tglFFmpeg"); // NOI18N
        tglFFmpeg.setPreferredSize(new java.awt.Dimension(28, 29));
        tglFFmpeg.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/FFmpeg.png"))); // NOI18N
        tglFFmpeg.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/FFmpegSelected.png"))); // NOI18N
        tglFFmpeg.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tglFFmpeg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglFFmpegActionPerformed(evt);
            }
        });

        tglLoop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/loop4.png"))); // NOI18N
        tglLoop.setToolTipText("Stream Loop ON/OFF");
        tglLoop.setFocusable(false);
        tglLoop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglLoop.setMaximumSize(new java.awt.Dimension(29, 28));
        tglLoop.setMinimumSize(new java.awt.Dimension(25, 25));
        tglLoop.setName("tglLoop"); // NOI18N
        tglLoop.setPreferredSize(new java.awt.Dimension(28, 29));
        tglLoop.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/loop4.png"))); // NOI18N
        tglLoop.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/loop4-activated.png"))); // NOI18N
        tglLoop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tglLoop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglLoopActionPerformed(evt);
            }
        });

        lblBE.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        lblBE.setText(bundle.getString("SOURCEBACKEND")); // NOI18N
        lblBE.setName("lblBE"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jlbDuration, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(labelO, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                                    .addComponent(labelH, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(labelW, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(spinOpacity, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jSlSpinO, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(spinW, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(spinH, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jSlSpinW, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                            .addComponent(jSlSpinH, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jcbLockAR)
                                        .addGap(1, 1, 1))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(labelAD, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelZ, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(labelVD, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelSeek, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(spinSeek, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jSlSpinSeek, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(spinADelay, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jSlSpinAD, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(spinVDelay, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jSlSpinVD, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(spinZOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jSlSpinZOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(labelVol, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                                    .addComponent(labelY, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(labelX, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblBE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(spinX, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jSlSpinX, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(spinY, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jSlSpinY, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                    .addComponent(jSlSpinV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(tglAVconv, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tglFFmpeg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tglGst, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(spinVolume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 65, Short.MAX_VALUE)))))
                        .addGap(13, 13, 13))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tglActiveStream, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tglPause, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tglVideo, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tglAudio, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tglPreview, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tglLoop, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tglActiveStream, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tglAudio, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tglPause, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tglPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tglVideo, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tglLoop, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jlbDuration)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelVol)
                    .addComponent(jSlSpinV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tglFFmpeg, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tglAVconv, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tglGst, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(spinVolume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblBE, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelX)
                        .addComponent(spinX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSlSpinX, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelY)
                        .addComponent(spinY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSlSpinY, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSlSpinW, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(labelW)
                                .addComponent(spinW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(labelH)
                                .addComponent(spinH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jSlSpinH, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jcbLockAR, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(spinOpacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelO))
                    .addComponent(jSlSpinO, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(spinZOrder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSlSpinZOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelVD)
                        .addComponent(spinVDelay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSlSpinVD, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelAD)
                        .addComponent(spinADelay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSlSpinAD, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(spinSeek, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelSeek, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSlSpinSeek, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jSlSpinVD.getAccessibleContext().setAccessibleDescription("");

        getAccessibleContext().setAccessibleDescription("");
        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents
    private void tglActiveStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglActiveStreamActionPerformed
        if (tglActiveStream.isSelected()) {
            if (stream.getisATrack() && !stream.getPreView()) {
                String name = stream.getName();
                listenerTP.startItsTrack(name);
            } else {
                if (tglVideo.isSelected()) {
                    stream.setOnlyAudio(true);
                } else {
                    stream.setOnlyAudio(false);
                }
                tglVideo.setEnabled(false);
                titleLabel.setForeground(sActiveLblCol);
                spinVDelay.setEnabled(false);
                jSlSpinVD.setEnabled(false);
                spinADelay.setEnabled(false);
                jSlSpinAD.setEnabled(false);
                spinSeek.setEnabled(false);
                jSlSpinSeek.setEnabled(false);
                tglAudio.setEnabled(false);
                tglPreview.setEnabled(false);
                tglPause.setEnabled(true);
                this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, sActiveLblCol));
                stream.read();
            }
        } else {
            if (stream.getisATrack() && lblPlayingTrack.getText().contains(stream.getName())) {
                listTracks.repaint();
                lblPlayingTrack.setText("");
            }
            titleLabel.setForeground(sStopLblCol);
            spinVDelay.setEnabled(stream.hasVideo());
            jSlSpinVD.setEnabled(stream.hasVideo());
            spinADelay.setEnabled(stream.hasAudio());
            jSlSpinAD.setEnabled(stream.hasAudio());
            spinSeek.setEnabled(stream.needSeekCTRL());
            jSlSpinSeek.setEnabled(stream.needSeekCTRL());
            tglPreview.setEnabled(true);
            if (tglAudio.isSelected()) {
                tglAudio.setEnabled(true);
            } else if (tglVideo.isSelected()) {
                tglVideo.setEnabled(true);
            } else {
                tglAudio.setEnabled(true);
                tglVideo.setEnabled(true);
            }
            tglPause.setSelected(false);
            tglPause.setEnabled(false);
            stream.setisPaused(false);
            if (stream.getLoop()) {
                stream.setLoop(false);
                stream.stop();
                stream.setLoop(true);
                stream.setVolume(volume);
            } else {
                stream.stop();
                if (stream.getVolume() == 0) {
                    stream.setVolume(0);
                } else {
                    stream.setVolume(volume);
                }
            }
            if (stream.getisATrack() && !stream.getPreView()) {
                listenerTP.stopItsTrack();
            }
            this.setBorder(BorderFactory.createEtchedBorder());
        }
    }//GEN-LAST:event_tglActiveStreamActionPerformed

    private void spinOpacityStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinOpacityStateChanged
        stream.setOpacity((Integer) spinOpacity.getValue());
        jSlSpinO.setValue((Integer) spinOpacity.getValue());
    }//GEN-LAST:event_spinOpacityStateChanged

    private void spinZOrderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinZOrderStateChanged
        stream.setZOrder((Integer) spinZOrder.getValue());
        jSlSpinZOrder.setValue((Integer) spinZOrder.getValue());
    }//GEN-LAST:event_spinZOrderStateChanged

    private void spinWStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinWStateChanged
        int w = (Integer) spinW.getValue();
        jSlSpinW.setValue(w);
        int h = oldH;
        if (lockRatio) {
            h = (oldH * w) / oldW;
            spinH.setValue(h);
        }
        stream.setWidth(w);
    }//GEN-LAST:event_spinWStateChanged

    private void spinHStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinHStateChanged
        int h = (Integer) spinH.getValue();
        jSlSpinH.setValue(h);
        if (!lockRatio) {
            oldH = stream.getHeight();
        }
        stream.setHeight(h);
    }//GEN-LAST:event_spinHStateChanged

    private void spinXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinXStateChanged
        stream.setX((Integer) spinX.getValue());
        jSlSpinX.setValue((Integer) spinX.getValue());
    }//GEN-LAST:event_spinXStateChanged

    private void spinYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinYStateChanged
        stream.setY((Integer) spinY.getValue());
        jSlSpinY.setValue((Integer) spinY.getValue());
    }//GEN-LAST:event_spinYStateChanged

    private void spinVolumeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinVolumeStateChanged
        String jSVol = spinVolume.getValue().toString().replace(".0", "");
        int jVol = Integer.parseInt(jSVol);
        jSlSpinV.setValue(jVol);
        Object value = spinVolume.getValue();
        float v = 0;
        if (value instanceof Float) {
            v = (Float) value;
        } else if (value instanceof Integer) {
            v = ((Number) value).floatValue();
        }
        if (stream.getisPaused()) {
            if (v / 100f != 0) {
                vol = v / 100f;
            }
        } else {
            stream.setVolume(v / 100f);
            volume = v / 100f;
        }
    }//GEN-LAST:event_spinVolumeStateChanged

    private void spinVDelayStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinVDelayStateChanged
        stream.setVDelay((Integer) spinVDelay.getValue());
        jSlSpinVD.setValue((Integer) spinVDelay.getValue());
    }//GEN-LAST:event_spinVDelayStateChanged

    private void spinADelayStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinADelayStateChanged
        stream.setADelay((Integer) spinADelay.getValue());
        jSlSpinAD.setValue((Integer) spinADelay.getValue());
    }//GEN-LAST:event_spinADelayStateChanged

    private void spinSeekStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinSeekStateChanged
        stream.setSeek((Integer) spinSeek.getValue());
        jSlSpinSeek.setValue((Integer) spinSeek.getValue());
    }//GEN-LAST:event_spinSeekStateChanged

    private void jSlSpinXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinXStateChanged
        spinX.setValue(jSlSpinX.getValue());
    }//GEN-LAST:event_jSlSpinXStateChanged

    private void jSlSpinYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinYStateChanged
        spinY.setValue(jSlSpinY.getValue());
    }//GEN-LAST:event_jSlSpinYStateChanged

    private void jSlSpinWStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinWStateChanged
        int w = (Integer) jSlSpinW.getValue();
        spinW.setValue(w);
    }//GEN-LAST:event_jSlSpinWStateChanged

    private void jSlSpinHStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinHStateChanged
        int h = (Integer) jSlSpinH.getValue();
        spinH.setValue(h);
    }//GEN-LAST:event_jSlSpinHStateChanged

    private void jSlSpinOStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinOStateChanged
        spinOpacity.setValue(jSlSpinO.getValue());
    }//GEN-LAST:event_jSlSpinOStateChanged

    private void jSlSpinVStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinVStateChanged
        spinVolume.setValue(jSlSpinV.getValue());
    }//GEN-LAST:event_jSlSpinVStateChanged

    private void jSlSpinVDStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinVDStateChanged
        spinVDelay.setValue(jSlSpinVD.getValue());
    }//GEN-LAST:event_jSlSpinVDStateChanged

    private void jSlSpinADStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinADStateChanged
        spinADelay.setValue(jSlSpinAD.getValue());
    }//GEN-LAST:event_jSlSpinADStateChanged

    private void jSlSpinSeekStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinSeekStateChanged
        spinSeek.setValue(jSlSpinSeek.getValue());
    }//GEN-LAST:event_jSlSpinSeekStateChanged

    private void jSlSpinZOrderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlSpinZOrderStateChanged
        spinZOrder.setValue(jSlSpinZOrder.getValue());
    }//GEN-LAST:event_jSlSpinZOrderStateChanged

    private void tglAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglAudioActionPerformed
        if (tglAudio.isSelected()) {
            stream.setHasAudio(false);
            stream.setOnlyVideo(true);
            tglVideo.setEnabled(false);
        } else {
            stream.setHasAudio(true);
            stream.setOnlyVideo(false);
            tglVideo.setEnabled(true);
        }
    }//GEN-LAST:event_tglAudioActionPerformed

    private void tglPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglPauseActionPerformed
        if (tglPause.isSelected()) {
            stream.setVolume(0);
            stream.setisPaused(true);
            stream.pause();
        } else {
            stream.setVolume(vol);
            spinVolume.setValue(vol * 100f);
            stream.setisPaused(false);
            stream.play();
        }
    }//GEN-LAST:event_tglPauseActionPerformed

    private void jcbLockARActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbLockARActionPerformed
        if (jcbLockAR.isSelected()) {
            spinH.setEnabled(false);
            jSlSpinH.setEnabled(false);
            lockRatio = true;
            oldW = stream.getWidth();
            oldH = stream.getHeight();
        } else {
            spinH.setEnabled(true);
            jSlSpinH.setEnabled(true);
            lockRatio = false;
            oldW = stream.getWidth();
            oldH = stream.getHeight();
        }
    }//GEN-LAST:event_jcbLockARActionPerformed

    private void jSlSpinVMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jSlSpinVMouseClicked
        if (evt.getClickCount() == 2 && !evt.isConsumed()) {
            evt.consume();
            if (muted) {
                stream.setVolume(volume);
//                System.out.println("Reset Volume to = "+volume);
                jSlSpinV.setEnabled(true);
                try {
                    icon = ImageIO.read(getClass().getResource("/truckliststudio/resources/tango/speaker4.png"));
                } catch (IOException ex) {
                    Logger.getLogger(StreamPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                UIDefaults sliderDefaults = new UIDefaults();
                sliderDefaults.put("Slider.paintValue", true);
                sliderDefaults.put("Slider.thumbHeight", 13);
                sliderDefaults.put("Slider.thumbWidth", 13);

                sliderDefaults.put("Slider:SliderThumb.backgroundPainter", new Painter() {

                    @Override
                    public void paint(Graphics2D g, Object object, int w, int h) {
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.drawImage(icon, 0, -5, null);
                    }
                });

                sliderDefaults.put("Slider:SliderTrack.backgroundPainter", new Painter() {
                    @Override
                    public void paint(Graphics2D g, Object object, int w, int h) {
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setStroke(new BasicStroke(2f));
                        g.setColor(Color.WHITE);
                        g.drawRoundRect(0, 2, w - 1, 1, 1, 1);
                    }
                });

                jSlSpinV.putClientProperty("JComponent.sizeVariant", "small");
                jSlSpinV.putClientProperty("Nimbus.Overrides", sliderDefaults);
                jSlSpinV.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
                muted = false;

            } else {
                jSlSpinV.setEnabled(false);
                Object value = spinVolume.getValue();
                float v = 0;
                if (value instanceof Float) {
                    v = (Float) value;
                } else if (value instanceof Integer) {
                    v = ((Number) value).floatValue();
                }
                volume = v / 100f;
//                System.out.println("Stored Volume = "+volume);
                stream.setVolume(0);
                try {
                    icon = ImageIO.read(getClass().getResource("/truckliststudio/resources/tango/speaker4-mute.png"));
                } catch (IOException ex) {
                    Logger.getLogger(StreamPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                UIDefaults sliderDefaults = new UIDefaults();

                sliderDefaults.put("Slider.paintValue", true);
                sliderDefaults.put("Slider.thumbHeight", 13);
                sliderDefaults.put("Slider.thumbWidth", 13);
                sliderDefaults.put("Slider:SliderThumb.backgroundPainter", new Painter() {

                    @Override
                    public void paint(Graphics2D g, Object object, int w, int h) {
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.drawImage(icon, 0, -5, null);
                    }
                });

                sliderDefaults.put("Slider:SliderTrack.backgroundPainter", new Painter() {
                    @Override
                    public void paint(Graphics2D g, Object object, int w, int h) {
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setStroke(new BasicStroke(2f));
                        g.setColor(Color.GRAY);
                        g.fillRoundRect(0, 2, w - 1, 2, 2, 2);
                        g.setColor(Color.WHITE);
                        g.drawRoundRect(0, 2, w - 1, 1, 1, 1);
                    }
                });

                jSlSpinV.putClientProperty("JComponent.sizeVariant", "small");
                jSlSpinV.putClientProperty("Nimbus.Overrides", sliderDefaults);
                jSlSpinV.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
                muted = true;
            }
        }
    }//GEN-LAST:event_jSlSpinVMouseClicked

    private void tglVideoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglVideoActionPerformed
        if (tglVideo.isSelected()) {
            tglAudio.setEnabled(false);
            stream.setOnlyAudio(true);
        } else {
            tglAudio.setEnabled(true);
            stream.setOnlyAudio(false);
        }
    }//GEN-LAST:event_tglVideoActionPerformed

    private void tglPreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglPreviewActionPerformed
        if (tglPreview.isSelected()) {
            stream.setPreView(true);
        } else {
            stream.setPreView(false);
        }
    }//GEN-LAST:event_tglPreviewActionPerformed

    private void jSlSpinVFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jSlSpinVFocusLost
        if (jSlSpinV.getValue() / 100f != 0) {
            vol = jSlSpinV.getValue() / 100f;
        }
    }//GEN-LAST:event_jSlSpinVFocusLost

    private void tglFFmpegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglFFmpegActionPerformed
        if (tglFFmpeg.isSelected()) {
            stream.setComm("FF");
            stream.setNeedSeek(false);
            stream.setBackFF(true);
            tglAVconv.setSelected(false);
            tglGst.setSelected(false);
            if (listenerTS != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        listenerTS.selectedSource(stream);
                    }
                }).start();
            }
        } else {
            stream.setComm("AV");
            stream.setBackFF(false);
            tglAVconv.setSelected(true);
            tglGst.setSelected(false);
        }
        stream.updateStatus();
    }//GEN-LAST:event_tglFFmpegActionPerformed

    private void tglAVconvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglAVconvActionPerformed
        if (tglAVconv.isSelected()) {
            stream.setComm("AV");
            stream.setNeedSeek(false);
            stream.setBackFF(false);
            tglGst.setSelected(false);
            tglFFmpeg.setSelected(false);
            if (listenerTS != null) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        listenerTS.selectedSource(stream);
                    }
                }).start();

            }
        } else {
            tglGst.setSelected(true);
            tglAVconv.setSelected(false);
            tglFFmpeg.setSelected(false);
            stream.setComm("GS");
            stream.setBackFF(false);
        }
        stream.updateStatus();
    }//GEN-LAST:event_tglAVconvActionPerformed

    private void tglGstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglGstActionPerformed
        if (tglGst.isSelected()) {
            stream.setComm("GS");
            stream.setNeedSeek(true);
            stream.setBackFF(false);
            tglAVconv.setSelected(false);
            tglFFmpeg.setSelected(false);
            if (listenerTS != null) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        listenerTS.selectedSource(stream);
                    }
                }).start();

            }
        } else {
            tglAVconv.setSelected(true);
            tglGst.setSelected(false);
            tglFFmpeg.setSelected(false);
            stream.setBackFF(false);
            stream.setComm("AV");
        }
        stream.updateStatus();
    }//GEN-LAST:event_tglGstActionPerformed

    private void tglLoopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglLoopActionPerformed
        if (tglLoop.isSelected()) {
            stream.setLoop(true);
        } else {
            stream.setLoop(false);
        }
    }//GEN-LAST:event_tglLoopActionPerformed

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        if (listenerTS != null) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    listenerTS.selectedSource(stream);
                }
            }).start();

        }
    }//GEN-LAST:event_formMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider jSlSpinAD;
    private javax.swing.JSlider jSlSpinH;
    private javax.swing.JSlider jSlSpinO;
    private javax.swing.JSlider jSlSpinSeek;
    private javax.swing.JSlider jSlSpinV;
    private javax.swing.JSlider jSlSpinVD;
    private javax.swing.JSlider jSlSpinW;
    private javax.swing.JSlider jSlSpinX;
    private javax.swing.JSlider jSlSpinY;
    private javax.swing.JSlider jSlSpinZOrder;
    private javax.swing.JCheckBox jcbLockAR;
    private javax.swing.JLabel jlbDuration;
    private javax.swing.JLabel labelAD;
    private javax.swing.JLabel labelH;
    private javax.swing.JLabel labelO;
    private javax.swing.JLabel labelSeek;
    private javax.swing.JLabel labelVD;
    private javax.swing.JLabel labelVol;
    private javax.swing.JLabel labelW;
    private javax.swing.JLabel labelX;
    private javax.swing.JLabel labelY;
    private javax.swing.JLabel labelZ;
    private javax.swing.JLabel lblBE;
    private javax.swing.JSpinner spinADelay;
    private javax.swing.JSpinner spinH;
    private javax.swing.JSpinner spinOpacity;
    private javax.swing.JSpinner spinSeek;
    private javax.swing.JSpinner spinVDelay;
    private javax.swing.JSpinner spinVolume;
    private javax.swing.JSpinner spinW;
    private javax.swing.JSpinner spinX;
    private javax.swing.JSpinner spinY;
    private javax.swing.JSpinner spinZOrder;
    private javax.swing.JToggleButton tglAVconv;
    private javax.swing.JToggleButton tglActiveStream;
    private javax.swing.JToggleButton tglAudio;
    private javax.swing.JToggleButton tglFFmpeg;
    private javax.swing.JToggleButton tglGst;
    private javax.swing.JToggleButton tglLoop;
    private javax.swing.JToggleButton tglPause;
    private javax.swing.JToggleButton tglPreview;
    private javax.swing.JToggleButton tglVideo;
    // End of variables declaration//GEN-END:variables

    @Override
    public void updatePreview(BufferedImage image) {
//        // nothing here.
    }

    public Stream getStream() {
        return this.stream;
    }
}
