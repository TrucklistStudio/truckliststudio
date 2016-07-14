/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * TrucklistStudio.java
 *
 * Created on 4-Apr-2012, 3:48:07 PM
 */
package truckliststudio;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.ColorUIResource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;
import static truckliststudio.WaitingDialog.cancel;
import static truckliststudio.WaitingDialog.jProgressBar1;
import truckliststudio.components.BottomPanel;
import truckliststudio.tracks.MasterTracks;
import truckliststudio.components.TrackPanel;
import truckliststudio.components.MasterPanel;
import static truckliststudio.components.MasterPanel.spinFPS;
import static truckliststudio.components.MasterPanel.spinHeight;
import static truckliststudio.components.MasterPanel.spinWidth;
import truckliststudio.components.OutputPanel;
import truckliststudio.components.ResourceMonitor;
import truckliststudio.components.ResourceMonitorLabel;
import truckliststudio.components.SourceControls;
import truckliststudio.components.StreamPanel;
import static truckliststudio.components.StreamPanel.setListenerTS;
import truckliststudio.components.StreamPanelText;
import static truckliststudio.components.StreamPanelText.setListenerTextTS;
import static truckliststudio.components.TrackPanel.lblPlayingTrack;
import static truckliststudio.components.TrackPanel.listTracks;
import static truckliststudio.components.TrackPanel.master;
import truckliststudio.externals.ProcessRenderer;
import truckliststudio.mixers.MasterFrameBuilder;
import truckliststudio.mixers.MasterMixer;
import truckliststudio.mixers.PrePlayer;
import truckliststudio.mixers.PreviewFrameBuilder;
import truckliststudio.mixers.PreviewMixer;
import truckliststudio.streams.SourceTrack;
import truckliststudio.streams.SourceImage;
import truckliststudio.streams.SourceImageGif;
import truckliststudio.streams.SourceMovie;
import truckliststudio.streams.SourceMusic;
import truckliststudio.streams.SourceText;
import truckliststudio.streams.Stream;
import truckliststudio.studio.Studio;
import truckliststudio.util.BackEnd;
import truckliststudio.util.Tools;
import truckliststudio.util.Tools.OS;

/**
 *
 * @author patrick (modified by karl)
 */
public final class TrucklistStudio extends JFrame implements StreamPanel.Listener, StreamPanelText.Listener { //StreamDesktop.Listener,

    public static Preferences prefs = null;
    public static Properties animations = new Properties();
    // FF = 0 ; AV = 1 ; GS = 2
    public static int outFMEbe = 1;
    private final static String userHomeDir = Tools.getUserHome();
    BottomPanel bottomPanel = new BottomPanel();
    OutputPanel recorder = new OutputPanel(bottomPanel);
    Frame about = new Frame();
    private static File cmdFile = null;
    private static String cmdOut = null;
    private static boolean cmdAutoStart = false;
    private static boolean cmdRemote = false;
    public static int audioFreq = 22050;
    public static String theme = "Classic";
    ArrayList<Stream> streamS = MasterTracks.getInstance().getStreams();
    private File lastFolder = null;
    boolean ffmpeg = BackEnd.ffmpegDetected();
    boolean avconv = BackEnd.avconvDetected();
    public static boolean gsNLE = BackEnd.nleDetected();
    boolean firstRun = true;
    static boolean autoAR = false;
    public static OS os = Tools.getOS();
    static boolean autoTrack = false;
    static boolean autoTitle = false;
    private int numVideos = 0;
    private int numMusics = 0;
    private int numPictures = 0;
    private int numTexts = 0;
    private JLabel lblVideo = new JLabel("Videos(0)");
    private JLabel lblMusic = new JLabel("Musics(0)");
    private JLabel lblPicture = new JLabel("Pictures(0)");
    private JLabel lblText = new JLabel("Texts(0)");
    public static int textN = 0;
    private Color busyTab = Color.red;
    private Color resetTab = Color.black;
    private String selColLbl = "black";
    private String selColLbl2 = "green";
    ArrayList<JTabbedPane> tabs = new ArrayList<>();
    public static boolean x64 = false;
    public static boolean winGS = false;
    private boolean editingPhase = true;

    @Override
    public void closeSource(String name) {
        lblSourceSelected.setText("");
        int tabIndex = tabSources.getSelectedIndex();
        String tabTitle = tabSources.getTitleAt(tabIndex);
        if (tabTitle.contains("Videos")) {
            numVideos -= 1;
            if (numVideos > 0) {
                lblVideo.setText("Videos(" + numVideos + ")");
            } else {
                lblVideo.setForeground(resetTab);
                Font font = new Font("Ubuntu", Font.PLAIN, 11);
                lblVideo.setFont(font);
                lblVideo.setText("Videos(" + numVideos + ")");
            }
        } else if (tabTitle.contains("Musics")) {
            numMusics -= 1;
            if (numMusics > 0) {
                lblMusic.setText("Musics(" + numMusics + ")");
            } else {
                lblMusic.setForeground(resetTab);
                Font font = new Font("Ubuntu", Font.PLAIN, 11);
                lblMusic.setFont(font);
                lblMusic.setText("Musics(" + numMusics + ")");
            }
        } else if (tabTitle.contains("Pictures")) {
            numPictures -= 1;
            if (numPictures > 0) {
                lblPicture.setText("Pictures(" + numPictures + ")");
            } else {
                lblPicture.setForeground(resetTab);
                Font font = new Font("Ubuntu", Font.PLAIN, 11);
                lblPicture.setFont(font);
                lblPicture.setText("Pictures(" + numPictures + ")");
            }
        } else if (tabTitle.contains("Texts")) {
            numTexts -= 1;
            if (numTexts > 0) {
                lblText.setText("Texts(" + numTexts + ")");
            } else {
                lblText.setForeground(resetTab);
                Font font = new Font("Ubuntu", Font.PLAIN, 11);
                lblText.setFont(font);
                lblText.setText("Texts(" + numTexts + ")");
            }
        }
        listenerTSTP.closeItsTrack(name);
    }

    @Override
    public void startItsTrack(String name) {
        //Nothing Here
    }

    @Override
    public void stopItsTrack() {
        //Nothing Here
    }

    public interface Listener {

        public void stopChTime(java.awt.event.ActionEvent evt);

        public void resetBtnStates(java.awt.event.ActionEvent evt);

        public void resetAutoPLBtnState(java.awt.event.ActionEvent evt); // not used

        public void resetSinks(java.awt.event.ActionEvent evt);

        public void addLoadingTrack(String name);

        public void removeTracks(String removeSc, int a);

        public void setRemoteOn();

        public void closeItsTrack(String name);
    }

    static Listener listenerTSTP = null;

    public static void setListenerTSTP(Listener l) {
        listenerTSTP = l;
    }

    static Listener listenerOP = null;

    public static void setListenerOP(Listener l) {
        listenerOP = l;
    }

    /**
     * Creates new form TrucklistStudio
     *
     * @throws java.io.IOException
     */
    public TrucklistStudio() throws IOException {

        initComponents();

        setTitle("TrucklistStudio " + Version.version);
        ImageIcon icon = new ImageIcon(this.getClass().getResource("/truckliststudio/resources/icon.png"));
        this.setIconImage(icon.getImage());
        lblSourceSelected.setFont(new java.awt.Font("Noto Sans", 1, 14));
        lblSourceSelected.setForeground(Color.GREEN.darker());
        tabs.add(videoDesktop);
        tabs.add(musicDesktop);
        tabs.add(pictureDesktop);
        tabs.add(textDesktop);
        btnMinimizeAll.setVisible(false);

        setListenerTS(this);
        setListenerTextTS(this);

        tabSources.setDropTarget(new DropTarget() {

            @Override
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    final ArrayList<String> allStreams = new ArrayList<>();
                    for (Stream str : MasterTracks.getInstance().getStreams()) {
                        if (!str.toString().toLowerCase().contains("sink")) {
                            allStreams.add(str.getName());
                        }
                    }
                    String fileName = "";
                    evt.acceptDrop(DnDConstants.ACTION_REFERENCE);
                    boolean success = false;
                    DataFlavor dataFlavor = null;
                    if (evt.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dataFlavor = DataFlavor.javaFileListFlavor;
                    } else if (evt.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        dataFlavor = DataFlavor.stringFlavor;
                    } else {
                        for (DataFlavor d : evt.getTransferable().getTransferDataFlavors()) {
                            if (evt.getTransferable().isDataFlavorSupported(d)) {
//                                System.out.println("Supported: " + d.getDefaultRepresentationClassAsString());
                                dataFlavor = d;
                                break;
                            }
                        }
                    }
                    Object data = evt.getTransferable().getTransferData(dataFlavor);
                    String files = "";
                    if (data instanceof Reader) {
                        char[] text = new char[65536];
                        files = new String(text).trim();
                    } else if (data instanceof InputStream) {
                        char[] text = new char[65536];
                        files = new String(text).trim();
                    } else if (data instanceof String) {
                        files = data.toString().trim();
                    } else {
                        List list = (List) data;
                        for (Object o : list) {
                            files += new File(o.toString()).toURI().toURL().toString() + "\n";
                        }
                    }
                    if (files.length() > 0) {
                        String[] lines = files.split("\n");
                        for (String line : lines) {
                            File file = new File(new URL(line.trim()).toURI());
                            if (file.exists()) {
                                fileName = file.getName();
                                Stream stream = Stream.getInstance(file);
                                boolean noError = true;
                                boolean noDouble = true;
                                if (stream != null) {
                                    getStreamParams(stream, file, null);
                                    if (stream.getStreamTime().equals("N/A") && (stream instanceof SourceMovie || stream instanceof SourceMusic)) {
                                        noError = false;
                                        stream.destroy();
                                    }
                                    for (String str : allStreams) {
                                        if (stream.getName().equals(str)) {
                                            noDouble = false;
                                            stream.destroy();
                                        }
                                    }
                                    if (noError && noDouble) {
                                        ArrayList<String> allChan = new ArrayList<>();
                                        for (String scn : MasterTracks.getInstance().getTracks()) {
                                            allChan.add(scn);
                                        }
                                        for (String sc : allChan) {
                                            stream.addTrack(SourceTrack.getTrack(sc, stream));
                                        }
                                        StreamPanel frame = new StreamPanel(stream);
                                        String sName = stream.getName();
                                        String[] tokens = split(sName, 10);
                                        String splitTitle = "";
                                        for (String token : tokens) {
                                            splitTitle = splitTitle + token + "<span></span>";
                                        }
                                        if (stream instanceof SourceMovie) {
                                            numVideos += 1;
                                            videoDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                                            lblVideo.setForeground(busyTab);
                                            Font font = new Font("Ubuntu", Font.BOLD, 11);
                                            lblVideo.setFont(font);
                                            lblVideo.setText("Videos(" + numVideos + ")");
                                        } else if (stream instanceof SourceMusic) {
                                            numMusics += 1;
                                            musicDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                                            lblMusic.setForeground(busyTab);
                                            Font font = new Font("Ubuntu", Font.BOLD, 11);
                                            lblMusic.setFont(font);
                                            lblMusic.setText("Musics(" + numMusics + ")");
                                        } else if (stream instanceof SourceImage || stream instanceof SourceImageGif) { //|| stream instanceof SourceImageU  
                                            numPictures += 1;
                                            pictureDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                                            lblPicture.setForeground(busyTab);
                                            Font font = new Font("Ubuntu", Font.BOLD, 11);
                                            lblPicture.setFont(font);
                                            lblPicture.setText("Pictures(" + numPictures + ")");
                                        }
                                        if (autoTrack) {
                                            if (stream instanceof SourceMovie || stream instanceof SourceMusic) {
                                                TrackPanel.makeATrack(stream);
                                            }
                                        }
                                        success = true;
                                    } else {
                                        if (!noError) {
                                            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Error adding " + file.getName() + "!");
                                            ResourceMonitor.getInstance().addMessage(label);
                                        }
                                        if (!noDouble) {
                                            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, file.getName() + " Duplicated!");
                                            ResourceMonitor.getInstance().addMessage(label);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    evt.dropComplete(success);
                    if (!success) {
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 5000, "Unsupported file: " + fileName);
                        ResourceMonitor.getInstance().addMessage(label);
                    }
                } catch (UnsupportedFlavorException | IOException | URISyntaxException ex) {
                    Logger.getLogger(TrucklistStudio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        prefs = Preferences.userNodeForPackage(this.getClass());

        loadPrefs();

        if (theme.equals("Dark")) {
            // setting WS Dark Theme
            UIDefaults dialogTheme = new UIDefaults();

            UIManager.put("text", Color.WHITE);
            UIManager.put("control", Color.darkGray);
            UIManager.put("nimbusGreen", new Color(91, 181, 0));
            UIManager.put("nimbusBlueGrey", Color.darkGray);
            UIManager.put("nimbusBase", Color.darkGray);
            UIManager.put("nimbusLightBackground", new Color(134, 137, 143));
            UIManager.put("info", new Color(195, 160, 0));
            UIManager.put("nimbusDisabledText", Color.black);
            UIManager.put("nimbusSelectionBackground", Color.yellow);
            UIManager.put("nimbusSelectedText", Color.blue);
            UIManager.put("nimbusSelectionBackground", new Color(255, 220, 35));
            ColorUIResource colorResource = new ColorUIResource(Color.red.darker().darker());
            UIManager.put("nimbusOrange", colorResource);

            dialogTheme.put("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter", new Painter(Painter.BACKGROUND_ENABLED));
            dialogTheme.put("TabbedPane:TabbedPaneTab[Disabled].backgroundPainter", new Painter(Painter.BACKGROUND_DISABLED));
            dialogTheme.put("TabbedPane:TabbedPaneTab[Enabled+MouseOver].backgroundPainter", new Painter(Painter.BACKGROUND_ENABLED_MOUSEOVER));
            dialogTheme.put("TabbedPane:TabbedPaneTab[Enabled+Pressed].backgroundPainter", new Painter(Painter.BACKGROUND_ENABLED_PRESSED));
            dialogTheme.put("TabbedPane:TabbedPaneTab[Selected].backgroundPainter", new Painter(Painter.BACKGROUND_SELECTED));
            dialogTheme.put("TabbedPane:TabbedPaneTab[Disabled+Selected].backgroundPainter", new Painter(Painter.BACKGROUND_SELECTED_DISABLED));
            dialogTheme.put("TabbedPane:TabbedPaneTab[Focused+Selected].backgroundPainter", new Painter(Painter.BACKGROUND_SELECTED_FOCUSED));
            dialogTheme.put("TabbedPane:TabbedPaneTab[MouseOver+Selected].backgroundPainter", new Painter(Painter.BACKGROUND_SELECTED_MOUSEOVER));
            dialogTheme.put("TabbedPane:TabbedPaneTab[Focused+MouseOver+Selected].backgroundPainter", new Painter(Painter.BACKGROUND_SELECTED_MOUSEOVER_FOCUSED));
            dialogTheme.put("TabbedPane:TabbedPaneTab[Pressed+Selected].backgroundPainter", new Painter(Painter.BACKGROUND_SELECTED_PRESSED));
            dialogTheme.put("TabbedPane:TabbedPaneTab[Focused+Pressed+Selected].backgroundPainter", new Painter(Painter.BACKGROUND_SELECTED_PRESSED_FOCUSED));
            dialogTheme.put("TabbedPane:TabbedPaneTabArea[Disabled].backgroundPainter", new AreaPainter(AreaPainter.BACKGROUND_DISABLED));
            dialogTheme.put("TabbedPane:TabbedPaneTabArea[Enabled+MouseOver].backgroundPainter", new AreaPainter(AreaPainter.BACKGROUND_ENABLED_MOUSEOVER));
            dialogTheme.put("TabbedPane:TabbedPaneTabArea[Enabled+Pressed].backgroundPainter", new AreaPainter(AreaPainter.BACKGROUND_ENABLED_PRESSED));
            dialogTheme.put("TabbedPane:TabbedPaneTabArea[Enabled].backgroundPainter", new AreaPainter(AreaPainter.BACKGROUND_ENABLED));
//            tabSources.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
//            tabSources.putClientProperty("Nimbus.Overrides", dialogTheme);
            videoDesktop.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
            videoDesktop.putClientProperty("Nimbus.Overrides", dialogTheme);
            musicDesktop.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
            musicDesktop.putClientProperty("Nimbus.Overrides", dialogTheme);
            pictureDesktop.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
            pictureDesktop.putClientProperty("Nimbus.Overrides", dialogTheme);
            textDesktop.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
            textDesktop.putClientProperty("Nimbus.Overrides", dialogTheme);
            busyTab = Color.red.darker();
            resetTab = Color.WHITE;
            selColLbl = "white";
            selColLbl2 = "#ADFF2F";
        }

        lblText.setForeground(resetTab);
        Font font = new Font("Ubuntu", Font.PLAIN, 11);
        lblVideo.setFont(font);

        lblMusic.setFont(font);
        lblPicture.setFont(font);
        lblText.setFont(font);

        tabSources.setTabComponentAt(0, lblVideo);
        tabSources.setTabComponentAt(1, lblMusic);
        tabSources.setTabComponentAt(2, lblPicture);
        tabSources.setTabComponentAt(3, lblText);

        MasterMixer.getInstance().start();
        PreviewMixer.getInstance().start();
        panMaster.add(new MasterPanel(), BorderLayout.CENTER);
        TrackPanel trkPanel = new TrackPanel();

        ResourceMonitor resMon = ResourceMonitor.getInstance();
        trkPanel.PanelResource.add(resMon, BorderLayout.CENTER);
        resMon.setVisible(true);

        bottomPanel.mainHorizontalSplit.setLeftComponent(trkPanel);
        bottomPanel.mainHorizontalSplit.setRightComponent(recorder);

        mainVerticalSplit.setBottomComponent(bottomPanel);
        masterPanelSplit.setEnabled(false);

        if (os == OS.WINDOWS) {
            checkWinBits();
            checkWinGS();
            tglAVconv.setVisible(false);
            lblAVconv.setVisible(false);
        }
        System.out.println("NLE=" + gsNLE);
        initAnimations();
        initAudioMainSW();
        initThemeMainSW();
        initMainOutBE();
        tglAutoAR.setSelected(autoAR);
        tglAutoTrack.setSelected(autoTrack);
        tglAutoTitle.setSelected(autoTitle);
        listenerOP.resetSinks(null);
        if (cmdFile != null) {
            loadAtStart(cmdFile, null);
        }
        if (cmdOut != null) {
            listenerOP.addLoadingTrack(cmdOut); // used addLoadingTrack to activate Output from command line.
        }
        if (cmdAutoStart) {
            listenerTSTP.resetSinks(null); // used resetSinks to AutoPlay from command line.
        }
        if (cmdRemote) {
            listenerTSTP.setRemoteOn();
        }
        firstRun = false;
    }

    private boolean checkWinBits() {
        File prgSystemDir = new File("C:\\");
        File[] listFile = prgSystemDir.listFiles();
        for (File f : listFile) {
            System.out.println("File Name: " + f.getName());
            if (f.getName().equals("Program Files (x86)")) {
                x64 = true;
                break;
            } else {
                x64 = false;
            }
        }
        System.out.println("bit64: " + x64);
        return x64;
    }

    private boolean checkWinGS() {
        File prgSystemDir = new File("C:\\");
        File[] listFile = prgSystemDir.listFiles();
        for (File f : listFile) {
            System.out.println("File Name: " + f.getName());
            if (f.getName().equals("gstreamer")) {
                winGS = true;
                break;
            } else {
                winGS = false;
            }
        }
        System.out.println("winGS: " + winGS);
        return winGS;
    }

    @SuppressWarnings("unchecked")
    private void initAnimations() {
        try {
            animations.load(getClass().getResourceAsStream("/truckliststudio/resources/animations/animations.properties"));
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (Object o : animations.keySet()) {
                model.addElement(o);
            }
            cboAnimations.setModel(model);
        } catch (IOException ex) {
            Logger.getLogger(TrucklistStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private void initAudioMainSW() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("22050Hz");
        model.addElement("44100Hz");
        cboAudioHz.setModel(model);
        if (audioFreq == 22050) {
            cboAudioHz.setSelectedItem("22050Hz");
        } else {
            cboAudioHz.setSelectedItem("44100Hz");
        }
    }

    @SuppressWarnings("unchecked")
    private void initThemeMainSW() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("Classic");
        model.addElement("Dark");
        cboTheme.setModel(model);
        if (theme.equals("Classic")) {
            cboTheme.setSelectedItem("Classic");
        } else {
            cboTheme.setSelectedItem("Dark");
        }
    }

    private void initMainOutBE() {
        // FF = 0 ; AV = 1 ; GS = 2
        if (wsDistroWatch().toLowerCase().equals("windows")) {
            if (winGS) {
                tglGst.setEnabled(true);
            } else {
                tglGst.setEnabled(false);
            }
            if (outFMEbe == 0) {
                outFMEbe = 0;
                tglFFmpeg.setSelected(true);
            } else if (outFMEbe == 2) {
                if (tglGst.isEnabled()) {
                    tglFFmpeg.setSelected(false);
                    tglGst.setSelected(true);
                } else {
                    outFMEbe = 0;
                    tglFFmpeg.setSelected(true);
                }
            }
        } else if (ffmpeg && !avconv) {
            if (outFMEbe == 0 || outFMEbe == 1) {
                outFMEbe = 0;
                tglFFmpeg.setSelected(true);
                tglAVconv.setEnabled(false);
                tglGst.setEnabled(true);
            } else if (outFMEbe == 2) {
                tglFFmpeg.setSelected(false);
                tglFFmpeg.setEnabled(true);
                tglAVconv.setEnabled(false);
                tglGst.setSelected(true);
            }
        } else if (ffmpeg && avconv) {
            switch (outFMEbe) {
                case 0:
                    tglFFmpeg.setSelected(true);
                    tglAVconv.setEnabled(true);
                    tglGst.setEnabled(true);
                    break;
                case 1:
                    tglFFmpeg.setEnabled(true);
                    tglAVconv.setSelected(true);
                    tglGst.setEnabled(true);
                    break;
                case 2:
                    tglFFmpeg.setEnabled(true);
                    tglAVconv.setEnabled(true);
                    tglGst.setSelected(true);
                    break;
            }
        } else if (!ffmpeg && avconv) {
            if (outFMEbe == 1 || outFMEbe == 0) {
                outFMEbe = 1;
                tglAVconv.setSelected(true);
                tglFFmpeg.setEnabled(false);
                tglGst.setEnabled(true);
            } else if (outFMEbe == 2) {
                tglFFmpeg.setEnabled(false);
                tglAVconv.setEnabled(true);
                tglGst.setSelected(true);
            }
        }
//        System.out.println("OutFMEbe: "+outFMEbe);
    }

    private void loadPrefs() {
        int x = prefs.getInt("main-x", 100);
        int y = prefs.getInt("main-y", 100);
        int w = prefs.getInt("main-w", 800);
        int h = prefs.getInt("main-h", 400);
        MasterMixer.getInstance().setWidth(prefs.getInt("mastermixer-w", MasterMixer.getInstance().getWidth()));
        MasterMixer.getInstance().setHeight(prefs.getInt("mastermixer-h", MasterMixer.getInstance().getHeight()));
        MasterMixer.getInstance().setRate(prefs.getInt("mastermixer-r", MasterMixer.getInstance().getRate()));
        PreviewMixer.getInstance().setWidth(prefs.getInt("mastermixer-w", MasterMixer.getInstance().getWidth()));
        PreviewMixer.getInstance().setHeight(prefs.getInt("mastermixer-h", MasterMixer.getInstance().getHeight()));
        PreviewMixer.getInstance().setRate(15);
        mainSplit.setDividerLocation(prefs.getInt("split-x", mainSplit.getDividerLocation()));
        mainSplit.setDividerLocation(prefs.getInt("split-last-x", mainSplit.getLastDividerLocation()));
        mainVerticalSplit.setDividerLocation(prefs.getInt("split-y", mainVerticalSplit.getDividerLocation()));
        mainVerticalSplit.setDividerLocation(prefs.getInt("split-last-y", mainVerticalSplit.getLastDividerLocation()));
        bottomPanel.mainHorizontalSplit.setDividerLocation(prefs.getInt("split-x-bottom", bottomPanel.mainHorizontalSplit.getDividerLocation()));
        bottomPanel.mainHorizontalSplit.setDividerLocation(prefs.getInt("split-last-x-bottom", bottomPanel.mainHorizontalSplit.getLastDividerLocation()));
        lastFolder = new File(prefs.get("lastfolder", "."));
        audioFreq = prefs.getInt("audio-freq", audioFreq);
        theme = prefs.get("theme", theme);
        outFMEbe = prefs.getInt("out-FME", outFMEbe);
        autoAR = prefs.getBoolean("autoar", autoAR);
        autoTrack = prefs.getBoolean("autotrack", autoTrack);
        autoTitle = prefs.getBoolean("autotitle", autoTitle);
        this.setLocation(x, y);
        this.setSize(w, h);
        recorder.loadPrefs(prefs);
    }

    private void savePrefs() {
        prefs.putInt("main-x", this.getX());
        prefs.putInt("main-y", this.getY());
        prefs.putInt("main-w", this.getWidth());
        prefs.putInt("main-h", this.getHeight());
        prefs.putInt("mastermixer-w", MasterMixer.getInstance().getWidth());
        prefs.putInt("mastermixer-h", MasterMixer.getInstance().getHeight());
        prefs.putInt("mastermixer-r", MasterMixer.getInstance().getRate());
        prefs.putInt("split-x", mainSplit.getDividerLocation());
        prefs.putInt("split-last-x", mainSplit.getLastDividerLocation());
        prefs.putInt("split-y", mainVerticalSplit.getDividerLocation());
        prefs.putInt("split-last-y", mainVerticalSplit.getLastDividerLocation());
        prefs.putInt("split-x-bottom", bottomPanel.mainHorizontalSplit.getDividerLocation());
        prefs.putInt("split-last-x-bottom", bottomPanel.mainHorizontalSplit.getLastDividerLocation());
        if (lastFolder != null) {
            prefs.put("lastfolder", lastFolder.getAbsolutePath());
        }
        prefs.putInt("audio-freq", audioFreq);
        prefs.put("theme", theme);
        prefs.putInt("out-FME", outFMEbe);
        prefs.putBoolean("autoar", autoAR);
        prefs.putBoolean("autotrack", autoTrack);
        prefs.putBoolean("autotitle", autoTitle);
        recorder.savePrefs(prefs);
        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(TrucklistStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainToolbar = new javax.swing.JToolBar();
        btnNewStudio = new javax.swing.JButton();
        btnImportStudio = new javax.swing.JButton();
        btnSaveStudio = new javax.swing.JButton();
        TSAbout = new javax.swing.JButton();
        jSeparator16 = new javax.swing.JToolBar.Separator();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(3, 0), new java.awt.Dimension(0, 0));
        lblAudioFreq = new javax.swing.JLabel();
        cboAudioHz = new javax.swing.JComboBox();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(2, 0), new java.awt.Dimension(0, 0));
        jSeparator10 = new javax.swing.JToolBar.Separator();
        lblThemeSwitch = new javax.swing.JLabel();
        cboTheme = new javax.swing.JComboBox();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(2, 0), new java.awt.Dimension(0, 0));
        jSeparator7 = new javax.swing.JToolBar.Separator();
        btnSysGC = new javax.swing.JButton();
        lblSysGC = new javax.swing.JLabel();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(2, 0), new java.awt.Dimension(0, 0));
        jSeparator12 = new javax.swing.JToolBar.Separator();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(3, 0), new java.awt.Dimension(0, 0));
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        lblFFmpeg3 = new javax.swing.JLabel();
        tglFFmpeg = new javax.swing.JToggleButton();
        lblFFmpeg = new javax.swing.JLabel();
        tglAVconv = new javax.swing.JToggleButton();
        lblAVconv = new javax.swing.JLabel();
        tglGst = new javax.swing.JToggleButton();
        lblGst = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(3, 0), new java.awt.Dimension(0, 0));
        mainVerticalSplit = new javax.swing.JSplitPane();
        masterPanelSplit = new javax.swing.JSplitPane();
        panMaster = new javax.swing.JPanel();
        mainSplit = new javax.swing.JSplitPane();
        panSources = new javax.swing.JPanel();
        toolbar = new javax.swing.JToolBar();
        btnAddFile = new javax.swing.JButton();
        btnAddFolder = new javax.swing.JButton();
        btnAddText = new javax.swing.JButton();
        btnAddAnimation = new javax.swing.JButton();
        cboAnimations = new javax.swing.JComboBox();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        tglAutoAR = new javax.swing.JToggleButton();
        tglAutoTrack = new javax.swing.JToggleButton();
        tglAutoTitle = new javax.swing.JToggleButton();
        btnApplyTitle = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnRemoveSource = new javax.swing.JButton();
        btnMinimizeAll = new javax.swing.JButton();
        tabSources = new javax.swing.JTabbedPane();
        videoScroll = new javax.swing.JScrollPane();
        videoDesktop = new javax.swing.JTabbedPane();
        musicScroll = new javax.swing.JScrollPane();
        musicDesktop = new javax.swing.JTabbedPane();
        pictureScroll = new javax.swing.JScrollPane();
        pictureDesktop = new javax.swing.JTabbedPane();
        textScroll = new javax.swing.JScrollPane();
        textDesktop = new javax.swing.JTabbedPane();
        panControls = new javax.swing.JPanel();
        tabControls = new javax.swing.JTabbedPane();
        lblSourceSelected = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TrackStudio");
        setMinimumSize(new java.awt.Dimension(916, 600));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        mainToolbar.setFloatable(false);
        mainToolbar.setMargin(new java.awt.Insets(0, 0, 0, 50));
        mainToolbar.setName("mainToolbar"); // NOI18N

        btnNewStudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/document-new.png"))); // NOI18N
        btnNewStudio.setToolTipText("New Studio");
        btnNewStudio.setFocusable(false);
        btnNewStudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNewStudio.setMaximumSize(new java.awt.Dimension(29, 28));
        btnNewStudio.setMinimumSize(new java.awt.Dimension(25, 25));
        btnNewStudio.setName("btnNewStudio"); // NOI18N
        btnNewStudio.setPreferredSize(new java.awt.Dimension(28, 28));
        btnNewStudio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNewStudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewStudioActionPerformed(evt);
            }
        });
        mainToolbar.add(btnNewStudio);

        btnLoadStudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/document-open.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("truckliststudio/Languages"); // NOI18N
        btnLoadStudio.setToolTipText(bundle.getString("LOAD")); // NOI18N
        btnLoadStudio.setFocusable(false);
        btnLoadStudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLoadStudio.setMaximumSize(new java.awt.Dimension(29, 28));
        btnLoadStudio.setMinimumSize(new java.awt.Dimension(25, 25));
        btnLoadStudio.setName("btnLoadStudio"); // NOI18N
        btnLoadStudio.setPreferredSize(new java.awt.Dimension(28, 28));
        btnLoadStudio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnLoadStudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadStudioActionPerformed(evt);
            }
        });
        mainToolbar.add(btnLoadStudio);

        btnImportStudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/chan-add.png"))); // NOI18N
        btnImportStudio.setToolTipText("Import Studio");
        btnImportStudio.setFocusable(false);
        btnImportStudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnImportStudio.setMaximumSize(new java.awt.Dimension(29, 28));
        btnImportStudio.setMinimumSize(new java.awt.Dimension(25, 25));
        btnImportStudio.setName("btnImportStudio"); // NOI18N
        btnImportStudio.setPreferredSize(new java.awt.Dimension(28, 28));
        btnImportStudio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnImportStudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportStudioActionPerformed(evt);
            }
        });
        mainToolbar.add(btnImportStudio);

        btnSaveStudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/document-save.png"))); // NOI18N
        btnSaveStudio.setToolTipText(bundle.getString("SAVE")); // NOI18N
        btnSaveStudio.setFocusable(false);
        btnSaveStudio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSaveStudio.setMaximumSize(new java.awt.Dimension(29, 28));
        btnSaveStudio.setMinimumSize(new java.awt.Dimension(25, 25));
        btnSaveStudio.setName("btnSaveStudio"); // NOI18N
        btnSaveStudio.setPreferredSize(new java.awt.Dimension(28, 28));
        btnSaveStudio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSaveStudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveStudioActionPerformed(evt);
            }
        });
        mainToolbar.add(btnSaveStudio);

        TSAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/user-info.png"))); // NOI18N
        TSAbout.setToolTipText("About");
        TSAbout.setFocusable(false);
        TSAbout.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        TSAbout.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        TSAbout.setMaximumSize(new java.awt.Dimension(29, 28));
        TSAbout.setMinimumSize(new java.awt.Dimension(25, 25));
        TSAbout.setName("TSAbout"); // NOI18N
        TSAbout.setPreferredSize(new java.awt.Dimension(28, 28));
        TSAbout.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        TSAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TSAboutActionPerformed(evt);
            }
        });
        mainToolbar.add(TSAbout);

        jSeparator16.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSeparator16.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jSeparator16.setName("jSeparator16"); // NOI18N
        jSeparator16.setOpaque(true);
        jSeparator16.setSeparatorSize(new java.awt.Dimension(3, 10));
        mainToolbar.add(jSeparator16);

        filler1.setName("filler1"); // NOI18N
        mainToolbar.add(filler1);

        lblAudioFreq.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/audio-Hz.png"))); // NOI18N
        lblAudioFreq.setToolTipText("Master Audio Sample Rate");
        lblAudioFreq.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 1));
        lblAudioFreq.setName("lblAudioFreq"); // NOI18N
        mainToolbar.add(lblAudioFreq);

        cboAudioHz.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboAudioHz.setToolTipText("Choose Default Audio Output Quality.");
        cboAudioHz.setName("cboAudioHz"); // NOI18N
        cboAudioHz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboAudioHzActionPerformed(evt);
            }
        });
        mainToolbar.add(cboAudioHz);

        filler2.setName("filler2"); // NOI18N
        mainToolbar.add(filler2);

        jSeparator10.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSeparator10.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jSeparator10.setName("jSeparator10"); // NOI18N
        jSeparator10.setOpaque(true);
        jSeparator10.setSeparatorSize(new java.awt.Dimension(5, 10));
        mainToolbar.add(jSeparator10);

        lblThemeSwitch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/image-x-generic.png"))); // NOI18N
        lblThemeSwitch.setToolTipText("Master Theme Selector");
        lblThemeSwitch.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 1));
        lblThemeSwitch.setName("lblThemeSwitch"); // NOI18N
        mainToolbar.add(lblThemeSwitch);

        cboTheme.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboTheme.setToolTipText("Choose Default WS Theme.");
        cboTheme.setName("cboTheme"); // NOI18N
        cboTheme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboThemeActionPerformed(evt);
            }
        });
        mainToolbar.add(cboTheme);

        filler3.setName("filler3"); // NOI18N
        mainToolbar.add(filler3);

        jSeparator7.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSeparator7.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jSeparator7.setName("jSeparator7"); // NOI18N
        jSeparator7.setOpaque(true);
        jSeparator7.setSeparatorSize(new java.awt.Dimension(5, 10));
        mainToolbar.add(jSeparator7);

        btnSysGC.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/button-small-clear.png"))); // NOI18N
        btnSysGC.setToolTipText("Try to Clean Up some memory");
        btnSysGC.setFocusable(false);
        btnSysGC.setName("btnSysGC"); // NOI18N
        btnSysGC.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSysGC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSysGCActionPerformed(evt);
            }
        });
        mainToolbar.add(btnSysGC);

        lblSysGC.setFont(new java.awt.Font("Ubuntu Condensed", 0, 12)); // NOI18N
        lblSysGC.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSysGC.setText("RAM");
        lblSysGC.setToolTipText("Try to Clean Up some memory");
        lblSysGC.setName("lblSysGC"); // NOI18N
        mainToolbar.add(lblSysGC);

        filler5.setName("filler5"); // NOI18N
        mainToolbar.add(filler5);

        jSeparator12.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSeparator12.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jSeparator12.setName("jSeparator12"); // NOI18N
        jSeparator12.setOpaque(true);
        jSeparator12.setSeparatorSize(new java.awt.Dimension(5, 10));
        mainToolbar.add(jSeparator12);

        filler4.setName("filler4"); // NOI18N
        mainToolbar.add(filler4);

        filler7.setName("filler7"); // NOI18N
        mainToolbar.add(filler7);

        lblFFmpeg3.setBackground(new java.awt.Color(102, 102, 102));
        lblFFmpeg3.setFont(new java.awt.Font("Ubuntu Condensed", 1, 14)); // NOI18N
        lblFFmpeg3.setText("OUT Back.end: ");
        lblFFmpeg3.setToolTipText("Select Available Outputs Back-Ends");
        lblFFmpeg3.setName("lblFFmpeg3"); // NOI18N
        mainToolbar.add(lblFFmpeg3);

        tglFFmpeg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/FFmpeg.png"))); // NOI18N
        tglFFmpeg.setToolTipText("Use FFmpeg Output Backend.");
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
        mainToolbar.add(tglFFmpeg);

        lblFFmpeg.setFont(new java.awt.Font("Ubuntu Condensed", 0, 12)); // NOI18N
        lblFFmpeg.setText("FFmpeg  ");
        lblFFmpeg.setName("lblFFmpeg"); // NOI18N
        mainToolbar.add(lblFFmpeg);

        tglAVconv.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/FFmpeg.png"))); // NOI18N
        tglAVconv.setToolTipText("Use Libav Output Backend.");
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
        mainToolbar.add(tglAVconv);

        lblAVconv.setFont(new java.awt.Font("Ubuntu Condensed", 0, 12)); // NOI18N
        lblAVconv.setText("Libav ");
        lblAVconv.setName("lblAVconv"); // NOI18N
        mainToolbar.add(lblAVconv);

        tglGst.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/gstreamer.png"))); // NOI18N
        tglGst.setToolTipText("Use GStreamer Output Backend.");
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
        mainToolbar.add(tglGst);

        lblGst.setFont(new java.awt.Font("Ubuntu Condensed", 0, 12)); // NOI18N
        lblGst.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblGst.setText("GStreamer");
        lblGst.setName("lblGst"); // NOI18N
        mainToolbar.add(lblGst);

        filler6.setName("filler6"); // NOI18N
        mainToolbar.add(filler6);

        getContentPane().add(mainToolbar, java.awt.BorderLayout.PAGE_START);

        mainVerticalSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        mainVerticalSplit.setName("mainVerticalSplit"); // NOI18N
        mainVerticalSplit.setOneTouchExpandable(true);

        masterPanelSplit.setDividerSize(0);
        masterPanelSplit.setName("masterPanelSplit"); // NOI18N

        panMaster.setName("panMaster"); // NOI18N
        panMaster.setPreferredSize(new java.awt.Dimension(100, 0));
        panMaster.setLayout(new java.awt.BorderLayout());
        masterPanelSplit.setLeftComponent(panMaster);

        mainSplit.setDividerLocation(500);
        mainSplit.setName("mainSplit"); // NOI18N
        mainSplit.setOneTouchExpandable(true);

        panSources.setName("panSources"); // NOI18N

        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setMinimumSize(new java.awt.Dimension(200, 34));
        toolbar.setName("toolbar"); // NOI18N

        btnAddFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/studio-add.png"))); // NOI18N
        btnAddFile.setToolTipText("Load Media");
        btnAddFile.setFocusable(false);
        btnAddFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddFile.setMaximumSize(new java.awt.Dimension(29, 28));
        btnAddFile.setMinimumSize(new java.awt.Dimension(25, 25));
        btnAddFile.setName("btnAddFile"); // NOI18N
        btnAddFile.setPreferredSize(new java.awt.Dimension(28, 28));
        btnAddFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFileActionPerformed(evt);
            }
        });
        toolbar.add(btnAddFile);

        btnAddFolder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-add-folder.png"))); // NOI18N
        btnAddFolder.setToolTipText("Load Media Folder (Video/Music)");
        btnAddFolder.setFocusable(false);
        btnAddFolder.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddFolder.setMaximumSize(new java.awt.Dimension(29, 28));
        btnAddFolder.setMinimumSize(new java.awt.Dimension(25, 25));
        btnAddFolder.setName("btnAddFolder"); // NOI18N
        btnAddFolder.setPreferredSize(new java.awt.Dimension(28, 28));
        btnAddFolder.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFolderActionPerformed(evt);
            }
        });
        toolbar.add(btnAddFolder);

        btnAddText.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/accessories-text-editor.png"))); // NOI18N
        btnAddText.setToolTipText("Text/QRCode");
        btnAddText.setFocusable(false);
        btnAddText.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddText.setMaximumSize(new java.awt.Dimension(29, 28));
        btnAddText.setMinimumSize(new java.awt.Dimension(25, 25));
        btnAddText.setName("btnAddText"); // NOI18N
        btnAddText.setPreferredSize(new java.awt.Dimension(28, 28));
        btnAddText.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTextActionPerformed(evt);
            }
        });
        toolbar.add(btnAddText);

        btnAddAnimation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/Anim-add.png"))); // NOI18N
        btnAddAnimation.setToolTipText(bundle.getString("ADD_ANIMATION")); // NOI18N
        btnAddAnimation.setFocusable(false);
        btnAddAnimation.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddAnimation.setMaximumSize(new java.awt.Dimension(29, 28));
        btnAddAnimation.setMinimumSize(new java.awt.Dimension(25, 25));
        btnAddAnimation.setName("btnAddAnimation"); // NOI18N
        btnAddAnimation.setPreferredSize(new java.awt.Dimension(28, 28));
        btnAddAnimation.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddAnimation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddAnimationActionPerformed(evt);
            }
        });
        toolbar.add(btnAddAnimation);

        cboAnimations.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboAnimations.setToolTipText(bundle.getString("ANIMATIONS")); // NOI18N
        cboAnimations.setName("cboAnimations"); // NOI18N
        toolbar.add(cboAnimations);

        jSeparator4.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jSeparator4.setName("jSeparator4"); // NOI18N
        jSeparator4.setOpaque(true);
        toolbar.add(jSeparator4);

        tglAutoAR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/ar_button.png"))); // NOI18N
        tglAutoAR.setToolTipText("Automatic A/R detection Switch.");
        tglAutoAR.setFocusable(false);
        tglAutoAR.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglAutoAR.setMaximumSize(new java.awt.Dimension(29, 28));
        tglAutoAR.setMinimumSize(new java.awt.Dimension(25, 25));
        tglAutoAR.setName("tglAutoAR"); // NOI18N
        tglAutoAR.setPreferredSize(new java.awt.Dimension(28, 29));
        tglAutoAR.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/ar_button.png"))); // NOI18N
        tglAutoAR.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/ar_button_selected.png"))); // NOI18N
        tglAutoAR.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tglAutoAR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglAutoARActionPerformed(evt);
            }
        });
        toolbar.add(tglAutoAR);

        tglAutoTrack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/track_button.png"))); // NOI18N
        tglAutoTrack.setToolTipText("Automatic \"Make a Track\" Switch.");
        tglAutoTrack.setFocusable(false);
        tglAutoTrack.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglAutoTrack.setMaximumSize(new java.awt.Dimension(29, 28));
        tglAutoTrack.setMinimumSize(new java.awt.Dimension(25, 25));
        tglAutoTrack.setName("tglAutoTrack"); // NOI18N
        tglAutoTrack.setPreferredSize(new java.awt.Dimension(28, 29));
        tglAutoTrack.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/track_button.png"))); // NOI18N
        tglAutoTrack.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/track_button_selected2.png"))); // NOI18N
        tglAutoTrack.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tglAutoTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglAutoTrackActionPerformed(evt);
            }
        });
        toolbar.add(tglAutoTrack);

        tglAutoTitle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/overlayText.png"))); // NOI18N
        tglAutoTitle.setToolTipText("Automatic make a Text overlay Title for each Track");
        tglAutoTitle.setFocusable(false);
        tglAutoTitle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglAutoTitle.setMaximumSize(new java.awt.Dimension(29, 28));
        tglAutoTitle.setMinimumSize(new java.awt.Dimension(25, 25));
        tglAutoTitle.setName("tglAutoTitle"); // NOI18N
        tglAutoTitle.setPreferredSize(new java.awt.Dimension(28, 29));
        tglAutoTitle.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/overlayText.png"))); // NOI18N
        tglAutoTitle.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/overlayText_active.png"))); // NOI18N
        tglAutoTitle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tglAutoTitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglAutoTitleActionPerformed(evt);
            }
        });
        toolbar.add(tglAutoTitle);

        btnApplyTitle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/apply_titling.png"))); // NOI18N
        btnApplyTitle.setToolTipText(bundle.getString("ICON_ALL")); // NOI18N
        btnApplyTitle.setFocusable(false);
        btnApplyTitle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnApplyTitle.setMaximumSize(new java.awt.Dimension(29, 28));
        btnApplyTitle.setMinimumSize(new java.awt.Dimension(25, 25));
        btnApplyTitle.setName("btnApplyTitle"); // NOI18N
        btnApplyTitle.setPreferredSize(new java.awt.Dimension(28, 28));
        btnApplyTitle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnApplyTitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApplyTitleActionPerformed(evt);
            }
        });
        toolbar.add(btnApplyTitle);

        jSeparator2.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jSeparator2.setName("jSeparator2"); // NOI18N
        jSeparator2.setOpaque(true);
        toolbar.add(jSeparator2);

        btnRemoveSource.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/process-stop.png"))); // NOI18N
        btnRemoveSource.setToolTipText(bundle.getString("ICON_TAB_ALL")); // NOI18N
        btnRemoveSource.setFocusable(false);
        btnRemoveSource.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRemoveSource.setMaximumSize(new java.awt.Dimension(29, 28));
        btnRemoveSource.setMinimumSize(new java.awt.Dimension(25, 25));
        btnRemoveSource.setName("btnRemoveSource"); // NOI18N
        btnRemoveSource.setPreferredSize(new java.awt.Dimension(28, 28));
        btnRemoveSource.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRemoveSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveSourceActionPerformed(evt);
            }
        });
        toolbar.add(btnRemoveSource);

        btnMinimizeAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/minimize-all.png"))); // NOI18N
        btnMinimizeAll.setToolTipText(bundle.getString("ICON_ALL")); // NOI18N
        btnMinimizeAll.setFocusable(false);
        btnMinimizeAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnMinimizeAll.setMaximumSize(new java.awt.Dimension(29, 28));
        btnMinimizeAll.setMinimumSize(new java.awt.Dimension(25, 25));
        btnMinimizeAll.setName("btnMinimizeAll"); // NOI18N
        btnMinimizeAll.setPreferredSize(new java.awt.Dimension(28, 28));
        btnMinimizeAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnMinimizeAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMinimizeAllActionPerformed(evt);
            }
        });
        toolbar.add(btnMinimizeAll);

        tabSources.setName("tabSources"); // NOI18N
        tabSources.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabSourcesStateChanged(evt);
            }
        });
        tabSources.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabSourcesMouseClicked(evt);
            }
        });

        videoScroll.setName("videoScroll"); // NOI18N

        videoDesktop.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        videoDesktop.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        videoDesktop.setName("videoDesktop"); // NOI18N
        videoDesktop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                videoDesktopStateChanged(evt);
            }
        });
        videoDesktop.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                videoDesktopMouseWheelMoved(evt);
            }
        });
        videoScroll.setViewportView(videoDesktop);

        tabSources.addTab("Videos", videoScroll);

        musicScroll.setName("musicScroll"); // NOI18N

        musicDesktop.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        musicDesktop.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        musicDesktop.setName("musicDesktop"); // NOI18N
        musicDesktop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                musicDesktopStateChanged(evt);
            }
        });
        musicDesktop.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                musicDesktopMouseWheelMoved(evt);
            }
        });
        musicScroll.setViewportView(musicDesktop);

        tabSources.addTab("Musics", musicScroll);

        pictureScroll.setName("pictureScroll"); // NOI18N

        pictureDesktop.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        pictureDesktop.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        pictureDesktop.setName("pictureDesktop"); // NOI18N
        pictureDesktop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                pictureDesktopStateChanged(evt);
            }
        });
        pictureDesktop.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                pictureDesktopMouseWheelMoved(evt);
            }
        });
        pictureScroll.setViewportView(pictureDesktop);

        tabSources.addTab("Pictures", pictureScroll);

        textScroll.setName("textScroll"); // NOI18N

        textDesktop.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        textDesktop.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        textDesktop.setName("textDesktop"); // NOI18N
        textDesktop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                textDesktopStateChanged(evt);
            }
        });
        textDesktop.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                textDesktopMouseWheelMoved(evt);
            }
        });
        textScroll.setViewportView(textDesktop);

        tabSources.addTab("Texts", textScroll);

        javax.swing.GroupLayout panSourcesLayout = new javax.swing.GroupLayout(panSources);
        panSources.setLayout(panSourcesLayout);
        panSourcesLayout.setHorizontalGroup(
            panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
            .addComponent(tabSources)
        );
        panSourcesLayout.setVerticalGroup(
            panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panSourcesLayout.createSequentialGroup()
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabSources, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE))
        );

        mainSplit.setLeftComponent(panSources);

        panControls.setName("panControls"); // NOI18N
        panControls.setPreferredSize(new java.awt.Dimension(200, 455));
        panControls.setLayout(new java.awt.BorderLayout());

        tabControls.setBorder(javax.swing.BorderFactory.createTitledBorder("Source Properties"));
        tabControls.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tabControls.setName("tabControls"); // NOI18N
        tabControls.setPreferredSize(new java.awt.Dimension(200, 455));
        panControls.add(tabControls, java.awt.BorderLayout.CENTER);

        lblSourceSelected.setName("lblSourceSelected"); // NOI18N
        panControls.add(lblSourceSelected, java.awt.BorderLayout.SOUTH);

        mainSplit.setRightComponent(panControls);

        masterPanelSplit.setRightComponent(mainSplit);

        mainVerticalSplit.setTopComponent(masterPanelSplit);

        getContentPane().add(mainVerticalSplit, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        boolean close = true;
        ArrayList<Stream> streamzI = MasterTracks.getInstance().getStreams();
        ArrayList<String> sourceChI = MasterTracks.getInstance().getTracks();
        if (streamzI.size() > 0 || sourceChI.size() > 0) {
            int result = JOptionPane.showConfirmDialog(this, "Really Close TrucklistStudio ?", "Save Studio Remainder", JOptionPane.YES_NO_CANCEL_OPTION);
            switch (result) {
                case JOptionPane.YES_OPTION:
                    close = true;
                    break;
                case JOptionPane.NO_OPTION:
                    close = false;
                    break;
                case JOptionPane.CANCEL_OPTION:
                    close = false;
                    break;
                case JOptionPane.CLOSED_OPTION:
                    close = false;
                    break;
            }
            if (close) {
                savePrefs();
                PrePlayer.getPreInstance(null).stop();
                Tools.sleep(10);
                MasterTracks.getInstance().endAllStream();
                Tools.sleep(10);
                MasterMixer.getInstance().stop();
                PreviewMixer.getInstance().stop();
                Tools.sleep(100);
                listenerOP.resetBtnStates(null);
                listenerOP.resetSinks(null);
                tabControls.removeAll();
                tabControls.repaint();
                Tools.sleep(300);

                cleanDesktops();

                Tools.sleep(10);
                System.out.println("Cleaning up ...");
                File directory = new File(userHomeDir + "/.truckliststudio");
                for (File f : directory.listFiles()) {
                    if (f.getName().startsWith("WSU") || f.getName().startsWith("WSC")) {
                        f.delete();
                    }
                }
                System.out.println("Thanks for using TrucklistStudio ...");
                System.out.println("GoodBye!");
                System.exit(0);
            } else {
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Quit Cancelled.");
                ResourceMonitor.getInstance().addMessage(label);
            }
        } else {
            savePrefs();
            PrePlayer.getPreInstance(null).stop();
            Tools.sleep(10);
            MasterTracks.getInstance().endAllStream();
            Tools.sleep(10);
            MasterMixer.getInstance().stop();
            PreviewMixer.getInstance().stop();
            System.out.println("arrayListStreamsThanks for using TrucklistStudio ...");
            System.out.println("GoodBye!");
            System.exit(0);
        }
    }//GEN-LAST:event_formWindowClosing

    private void btnAddTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTextActionPerformed
        SourceText streamTXT;
        if (textN < numTexts) {
            textN = numTexts;
        }

        boolean found = false;
        do {
            for (Stream s : streamS) {
                if (s instanceof SourceText) {
                    if (s.getName().equals("Text(" + textN + ")")) {
                        textN++;
                        found = true;
                    }
                }
            }
        } while (found = false);

        streamTXT = new SourceText("ts");
        ArrayList<String> allChan = new ArrayList<>();
        for (String scn : MasterTracks.getInstance().getTracks()) {
            allChan.add(scn);
        }
        for (String sc : allChan) {
            streamTXT.addTrack(SourceTrack.getTrack(sc, streamTXT));
        }
        StreamPanelText frame = new StreamPanelText((Stream) streamTXT);
        numTexts += 1;
        textDesktop.add(streamTXT.getName(), frame);
        lblText.setForeground(busyTab);
        Font font = new Font("Ubuntu", Font.BOLD, 11);
        lblText.setFont(font);
        lblText.setText("Texts(" + numTexts + ")");
        frame.setParent();
    }//GEN-LAST:event_btnAddTextActionPerformed

    private void btnAddFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFileActionPerformed
        final ArrayList<String> allStreams = new ArrayList<>();
        for (Stream str : MasterTracks.getInstance().getStreams()) {
            if (!str.toString().toLowerCase().contains("sink")) {
                allStreams.add(str.getName());
            }
        }
        JFileChooser chooser = new JFileChooser(lastFolder);
        FileNameExtensionFilter mediaFilter = new FileNameExtensionFilter("Supported Media files", "avi", "ogg", "jpeg", "ogv", "mp4", "m4v", "mpg", "divx", "wmv", "flv", "mov", "mkv", "vob", "jpg", "bmp", "png", "gif", "mp3", "wav", "wma", "m4a", ".mp2");
        chooser.setFileFilter(mediaFilter);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Add Media file ...");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int retVal = chooser.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                lastFolder = file.getParentFile();
//                String FileName = file.getName();
//                System.out.println("Name: " + FileName);
            }
            if (file != null) {
                Stream s = Stream.getInstance(file);
                boolean noError = true;
                boolean noDouble = true;
                if (s != null) {
                    getStreamParams(s, file, null);
//                    System.out.println("IsOnlyAudio="+s.isOnlyAudio());
//                    System.out.println("IsOnlyVideo="+s.isOnlyVideo());
                    if (s.getStreamTime().equals("N/A") && (s instanceof SourceMovie || s instanceof SourceMusic)) {
                        noError = false;
                        s.destroy();
                    }
                    for (String str : allStreams) {
                        if (s.getName().equals(str)) {
                            noDouble = false;
                            s.destroy();
                        }
                    }
                    if (noError && noDouble) {
                        ArrayList<String> allChan = new ArrayList<>();
                        for (String scn : MasterTracks.getInstance().getTracks()) {
                            allChan.add(scn);
                        }
                        for (String sc : allChan) {
                            s.addTrack(SourceTrack.getTrack(sc, s));
                        }

                        StreamPanel frame = new StreamPanel(s);
                        String sName = s.getName();
                        String splitTitle = "";
                        String[] tokens = split(sName, 10);
                        for (String token : tokens) {
                            splitTitle = splitTitle + token + "<span></span>";
                        }

                        if (s instanceof SourceMovie) {
                            numVideos += 1;
                            videoDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                            lblVideo.setForeground(busyTab);
                            Font font = new Font("Ubuntu", Font.BOLD, 11);
                            lblVideo.setFont(font);
                            lblVideo.setText("Videos(" + numVideos + ")");
                        } else if (s instanceof SourceMusic) {
                            numMusics += 1;
                            musicDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                            lblMusic.setForeground(busyTab);
                            Font font = new Font("Ubuntu", Font.BOLD, 11);
                            lblMusic.setFont(font);
                            lblMusic.setText("Musics(" + numMusics + ")");
                        } else if (s instanceof SourceImage || s instanceof SourceImageGif) { //|| s instanceof SourceImageU  
                            numPictures += 1;
                            pictureDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                            lblPicture.setForeground(busyTab);
                            Font font = new Font("Ubuntu", Font.BOLD, 11);
                            lblPicture.setFont(font);
                            lblPicture.setText("Pictures(" + numPictures + ")");
                        }
//                        System.out.println("Adding Source: " + s.getName());
                        frame.setParent();
                        if (autoTrack) {
                            if (s instanceof SourceMovie || s instanceof SourceMusic) {
                                TrackPanel.makeATrack(s);
                            }
                        }
                        if (autoTitle && s.getisATrack()) {
                            boolean isTitle = false;
                            SourceText streamTXT = null;
                            String trkName = s.getName();
                            String trkTitle = FilenameUtils.removeExtension(trkName);
//                            System.out.println("TrackName=" + trkTitle);
                            for (Stream str : master.getStreams()) {

                                if (str.getisATitle() && !str.getClass().toString().contains("Sink")) {
//                                    System.out.println("Titles=" + str.getName());
                                    streamTXT = (SourceText) str;
                                    streamTXT.setContent(trkTitle);
                                    streamTXT.setIsPlaying(true);
                                    boolean wasStopped = true;
//                                    System.out.println("Source Name="+s.getName());

                                    Stream playingStr = null;
                                    for (Stream stream : master.getStreams()) {
                                        if (stream.getisATrack() && stream.isPlaying()) {
                                            playingStr = stream;
                                            stream.setIsPlaying(false);
                                        }
                                    }

                                    if (s.isPlaying()) {
                                        wasStopped = false;
                                    } else {
                                        s.setIsPlaying(true);
                                    }

                                    master.updateTrack(trkName);
                                    master.addTrkTransitions(trkName);

                                    if (wasStopped) {
                                        s.setIsPlaying(false);
                                    }

                                    if (playingStr != null) {
                                        playingStr.setIsPlaying(true);
                                    }

                                    streamTXT.setIsPlaying(false);
                                    isTitle = true;
                                    break;
                                }
                            }
                            if (!isTitle) {
                                streamTXT = new SourceText(trkTitle);
                                for (String scn : MasterTracks.getInstance().getTracks()) {
                                    allChan.add(scn);
                                }
                                for (String sc : allChan) {
                                    streamTXT.addTrack(SourceTrack.getTrack(sc, streamTXT));
                                }
                                streamTXT.setisATitle(true);
                                streamTXT.setName("Titles");
                                streamTXT.setZOrder(1);
                                streamTXT.setIsPlaying(true);
                                boolean wasStopped = true;
                                if (s.isPlaying()) {
                                    wasStopped = false;
                                } else {
                                    s.setIsPlaying(true);
                                }
                                master.updateTrack(trkName);
                                master.addTrkTransitions(trkName);
                                if (wasStopped) {
                                    s.setIsPlaying(false);
                                }
                                streamTXT.setIsPlaying(false);
                                StreamPanelText txtTitle = new StreamPanelText((Stream) streamTXT);
                                numTexts += 1;
                                textDesktop.add(streamTXT.getName(), txtTitle);
                                lblText.setForeground(busyTab);
                                Font font = new Font("Ubuntu", Font.BOLD, 11);
                                lblText.setFont(font);
                                lblText.setText("Texts(" + numTexts + ")");
                                txtTitle.setParent();
                            }
                        }
                    } else {
                        if (!noError) {
                            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Error adding " + file.getName() + "!");
                            ResourceMonitor.getInstance().addMessage(label);
                        }
                        if (!noDouble) {
                            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, file.getName() + " Duplicated!");
                            ResourceMonitor.getInstance().addMessage(label);
                        }
                    }
                }
            } else {
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "No File Selected!");
                ResourceMonitor.getInstance().addMessage(label);
            }
        } else {
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Loading Cancelled!");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_btnAddFileActionPerformed

    public static String[] split(String src, int len) {
        String[] result = new String[(int) Math.ceil((double) src.length() / (double) len)];
        for (int i = 0; i < result.length; i++) {
            result[i] = src.substring(i * len, Math.min(src.length(), (i + 1) * len));
        }
        return result;
    }

    private void btnAddAnimationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAnimationActionPerformed
        try {
            String key = cboAnimations.getSelectedItem().toString();
            String res = animations.getProperty(key);
            URL url = getClass().getResource("/truckliststudio/resources/animations/" + res);

            int count = 0;
            count = streamS.stream().filter((s) -> (s.getName().contains(key))).map((_item) -> 1).reduce(count, Integer::sum);
            String duplicatedTrkName = key + "(" + count + ")";
            boolean found = false;
            boolean nodup = false;
            ArrayList<String> temp = new ArrayList<>();
            for (Stream c : streamS) {
                temp.add(c.getName());
            }
            while (nodup != true) {
                for (Stream s : streamS) {
                    if (s.getName().equals(duplicatedTrkName)) {
                        count++;
                        duplicatedTrkName = key + "(" + count + ")";
                        found = true;
                    }
                }
                List<String> duplicates = new ArrayList<>();

                Set<String> stringSet = new TreeSet<>(new StringComparator());

                temp.add(duplicatedTrkName);

                for (String c : temp) {
                    if (!stringSet.add(c)) {
                        duplicates.add(c);
                    }
                }
//                System.out.println(duplicates.size());

                if (duplicates.size() > 1) {
                    nodup = true;
                } else {
                    nodup = false;
                }
//                System.out.println(nodup);
            }

            Stream streamAnm;
            streamAnm = new SourceImageGif(duplicatedTrkName, url);
            streamAnm.setIntSrc("true");
            BufferedImage gifImage = ImageIO.read(url);
            getStreamParams(streamAnm, null, gifImage);
            ArrayList<String> allChan = new ArrayList<>();
            for (String scn : MasterTracks.getInstance().getTracks()) {
                allChan.add(scn);
            }
            for (String sc : allChan) {
                streamAnm.addTrack(SourceTrack.getTrack(sc, streamAnm));
            }
            StreamPanel frame = new StreamPanel(streamAnm);
            numPictures += 1;
            String splitTitle = "";
            String[] tokens = split(streamAnm.getName(), 10);

            for (String token : tokens) {
                splitTitle = splitTitle + token + "<span></span>";
            }
            pictureDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
            lblPicture.setForeground(busyTab);
            Font font = new Font("Ubuntu", Font.BOLD, 11);
            lblPicture.setFont(font);
            lblPicture.setText("Pictures(" + numPictures + ")");
            frame.setParent();
        } catch (IOException ex) {
            Logger.getLogger(TrucklistStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAddAnimationActionPerformed

    public class StringComparator implements Comparator<String> {

        public int compare(String c1, String c2) {
            return c1.compareTo(c2);
        }
    }

    private void btnRemoveSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveSourceActionPerformed

        int tabIndex = tabSources.getSelectedIndex();
        String tabTitle = tabSources.getTitleAt(tabIndex);
        String sName = "";
        JTabbedPane tabPaneSelected = null;
        int selectedNumber = 0;

        if (tabTitle.contains("Videos")) {
            tabPaneSelected = videoDesktop;
        } else if (tabTitle.contains("Musics")) {
            tabPaneSelected = musicDesktop;
            selectedNumber = 1;
        } else if (tabTitle.contains("Pictures")) {
            tabPaneSelected = pictureDesktop;
            selectedNumber = 2;
        } else if (tabTitle.contains("Texts")) {
            tabPaneSelected = textDesktop;
            selectedNumber = 3;
        }

        int index = tabPaneSelected.getSelectedIndex();
        if (index != -1) {

            if (selectedNumber == 3) {
                sName = ((StreamPanelText) tabPaneSelected.getComponentAt(index)).getStream().getName();
            } else {
                sName = ((StreamPanel) tabPaneSelected.getComponentAt(index)).getStream().getName();
            }

            int result = JOptionPane.showConfirmDialog(this, sName + " will be Removed !!!", "Attention", JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JFileChooser.APPROVE_OPTION) {
                lblSourceSelected.setText("");

                switch (selectedNumber) {
                    case 0:
                        numVideos -= 1;
                        if (numVideos > 0) {
                            lblVideo.setText("Videos(" + numVideos + ")");
                        } else {
                            lblVideo.setForeground(resetTab);
                            Font font = new Font("Ubuntu", Font.PLAIN, 11);
                            lblVideo.setFont(font);
                            lblVideo.setText("Videos(" + numVideos + ")");
                        }
                        StreamPanel sPV = (StreamPanel) videoDesktop.getComponentAt(index);
                        Stream sV = sPV.getStream();
                        System.out.println("sName=" + sName);
                        tabPaneSelected.remove(sPV);
                        System.out.println("Closed");
                        if (sV.getisATrack() && sV.getName().equals(lblPlayingTrack.getText())) {
                            listTracks.repaint();
                            lblPlayingTrack.setText("");
                        }
                        sV.setLoop(false);
                        PreviewFrameBuilder.unregister(sV);
                        MasterFrameBuilder.unregister(sV);
                        sV.destroy();
                        sV = null;
                        truckliststudio.TrucklistStudio.tabControls.removeAll();
                        truckliststudio.TrucklistStudio.tabControls.repaint();
                        break;
                    case 1:
                        numMusics -= 1;
                        if (numMusics > 0) {
                            lblMusic.setText("Musics(" + numMusics + ")");
                        } else {
                            lblMusic.setForeground(resetTab);
                            Font font = new Font("Ubuntu", Font.PLAIN, 11);
                            lblMusic.setFont(font);
                            lblMusic.setText("Musics(" + numMusics + ")");
                        }
                        StreamPanel sPA = (StreamPanel) musicDesktop.getComponentAt(index);
                        Stream sA = sPA.getStream();
                        System.out.println("sName=" + sName);
                        tabPaneSelected.remove(sPA);
                        System.out.println("Closed");
                        if (sA.getisATrack() && sA.getName().equals(lblPlayingTrack.getText())) {
                            listTracks.repaint();
                            lblPlayingTrack.setText("");
                        }
                        sA.setLoop(false);
                        PreviewFrameBuilder.unregister(sA);
                        MasterFrameBuilder.unregister(sA);
                        sA.destroy();
                        sA = null;
                        truckliststudio.TrucklistStudio.tabControls.removeAll();
                        truckliststudio.TrucklistStudio.tabControls.repaint();
                        break;
                    case 2:
                        numPictures -= 1;
                        if (numPictures > 0) {
                            lblPicture.setText("Pictures(" + numPictures + ")");
                        } else {
                            lblPicture.setForeground(resetTab);
                            Font font = new Font("Ubuntu", Font.PLAIN, 11);
                            lblPicture.setFont(font);
                            lblPicture.setText("Pictures(" + numPictures + ")");
                        }
                        StreamPanel sPP = (StreamPanel) pictureDesktop.getComponentAt(index);
                        Stream sP = sPP.getStream();
                        sName = sP.getName();
                        System.out.println("sName=" + sName);
                        tabPaneSelected.remove(sPP);
                        System.out.println("Closed");
                        if (sP.getisATrack() && sP.getName().equals(lblPlayingTrack.getText())) {
                            listTracks.repaint();
                            lblPlayingTrack.setText("");
                        }
                        sP.setLoop(false);
                        PreviewFrameBuilder.unregister(sP);
                        MasterFrameBuilder.unregister(sP);
                        sP.destroy();
                        sP = null;
                        truckliststudio.TrucklistStudio.tabControls.removeAll();
                        truckliststudio.TrucklistStudio.tabControls.repaint();
                        break;
                    case 3:
                        numTexts -= 1;
                        if (numTexts > 0) {
                            lblText.setText("Texts(" + numTexts + ")");
                        } else {
                            lblText.setForeground(resetTab);
                            Font font = new Font("Ubuntu", Font.PLAIN, 11);
                            lblText.setFont(font);
                            lblText.setText("Texts(" + numTexts + ")");
                        }
                        StreamPanelText sPT = (StreamPanelText) textDesktop.getComponentAt(index);
                        Stream sT = sPT.getStream();
                        System.out.println("sName=" + sName);
                        tabPaneSelected.remove(sPT);
                        System.out.println("Closed");
                        if (sT.getisATrack() && sT.getName().equals(lblPlayingTrack.getText())) {
                            listTracks.repaint();
                            lblPlayingTrack.setText("");
                        }
                        sT.setLoop(false);
                        PreviewFrameBuilder.unregister(sT);
                        MasterFrameBuilder.unregister(sT);
                        sT.destroy();
                        sT = null;
                        truckliststudio.TrucklistStudio.tabControls.removeAll();
                        truckliststudio.TrucklistStudio.tabControls.repaint();
                        break;
                    default:
                        break;
                }
                listenerTSTP.closeItsTrack(sName);
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, sName + " Removed!");
                ResourceMonitor.getInstance().addMessage(label);
            } else {
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Remove Source Cancelled!");
                ResourceMonitor.getInstance().addMessage(label);
            }
        }
    }//GEN-LAST:event_btnRemoveSourceActionPerformed

    private void btnSaveStudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveStudioActionPerformed
        final java.awt.event.ActionEvent sEvt = evt;
        try {
            File file;
            boolean overWrite = true;
            ArrayList<Stream> streamzI = MasterTracks.getInstance().getStreams();
            ArrayList<String> sourceChI = MasterTracks.getInstance().getTracks();
            if (streamzI.size() > 0 || sourceChI.size() > 0) {
                Object[] options = {"OK"};
                JOptionPane.showOptionDialog(this,
                        "All Streams will be Stopped !!!", "Attention",
                        JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]);
            }
            JFileChooser chooser = new JFileChooser(lastFolder);
            FileNameExtensionFilter studioFilter = new FileNameExtensionFilter("Studio files (*.studio)", "studio");
            chooser.setFileFilter(studioFilter);
            chooser.setDialogTitle("Save a Studio ...");
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int retval = chooser.showSaveDialog(this);
            file = chooser.getSelectedFile();
            if (retval == JFileChooser.APPROVE_OPTION && file != null && file.exists()) {
                int result = JOptionPane.showConfirmDialog(this, "File exists, overwrite?", "Attention", JOptionPane.YES_NO_CANCEL_OPTION);
                switch (result) {
                    case JOptionPane.YES_OPTION:
                        overWrite = true;
                        break;
                    case JOptionPane.NO_OPTION:
                        overWrite = false;
                        break;
                    case JOptionPane.CANCEL_OPTION:
                        overWrite = false;
                        break;
                    case JOptionPane.CLOSED_OPTION:
                        overWrite = false;
                        break;
                }
            }
            if (retval == JFileChooser.APPROVE_OPTION && overWrite) {
                final WaitingDialog waitingD = new WaitingDialog(this);
                final File fileF = file;
                lblSourceSelected.setText("");
                SwingWorker<?, ?> worker = new SwingWorker<Void, Integer>() {
                    @Override
                    protected Void doInBackground() throws InterruptedException {
                        if (fileF != null) {
                            File fileS = fileF;
                            lastFolder = fileS.getParentFile();
                            PrePlayer.getPreInstance(null).stop();
                            Tools.sleep(100);
                            MasterTracks.getInstance().stopAllStream();
                            Tools.sleep(100);
                            listenerTSTP.stopChTime(sEvt);
                            for (Stream s : MasterTracks.getInstance().getStreams()) {
                                s.updateStatus();
                            }
                            if (!fileS.getName().endsWith(".studio")) {
                                fileS = new File(fileS.getParent(), fileS.getName() + ".studio");
                            }
                            try {
                                Studio.save(fileS);
                            } catch (IOException | XMLStreamException | IllegalArgumentException | IllegalAccessException | TransformerException ex) {
                                Logger.getLogger(TrucklistStudio.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Studio is saved!");
                            ResourceMonitor.getInstance().addMessage(label);
                            setTitle("TrucklistStudio " + Version.version + " (" + fileS.getName() + ")");
                            jProgressBar1.setValue(100);
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        Tools.sleep(10);
                        waitingD.dispose();
                    }
                };
                worker.execute();
                waitingD.toFront();
                waitingD.setVisible(true);
            } else {
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Saving Cancelled!");
                ResourceMonitor.getInstance().addMessage(label);
            }
        } catch (HeadlessException ex) {
            Logger.getLogger(TrucklistStudio.class.getName()).log(Level.SEVERE, null, ex);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Error: " + ex.getMessage());
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_btnSaveStudioActionPerformed

    public static void getStreamParams(Stream stream, File file, BufferedImage image) {
        String fileType = "not";
        if (stream instanceof SourceMovie) {
            fileType = "mov";
        } else if (stream instanceof SourceMusic) {
            fileType = "mus";
        } else if (stream instanceof SourceImage || stream instanceof SourceImageGif) { //stream instanceof SourceImageU ||
            fileType = "pic";
        }
        if (image != null) {
            if (autoAR) {
                int w = image.getWidth();
                int h = image.getHeight();
                int mixerW = MasterMixer.getInstance().getWidth();
                int mixerH = MasterMixer.getInstance().getHeight();
                int hAR = (mixerW * h) / w;
                int wAR = (mixerH * w) / h;
                if (hAR > mixerH) {
                    hAR = mixerH;
                    int xPos = (mixerW - wAR) / 2;
                    stream.setX(xPos);
                    stream.setWidth(wAR);
                }
                if (w > mixerW) {
                    int yPos = (mixerH - hAR) / 2;
                    stream.setY(yPos);
                    stream.setHeight(hAR);
                } else if (h < mixerH) {
                    int yPos = (mixerH - hAR) / 2;
                    stream.setY(yPos);
                } else {
                    hAR = mixerH;
                }
                stream.setHeight(hAR);
            }
        } else {
            String infoCmd;
            File fileD;
            String batchDurationComm;
            Runtime rt = Runtime.getRuntime();
            if (wsDistroWatch().toLowerCase().equals("windows")) {
                infoCmd = "ffmpeg -i " + "\"" + file.getAbsolutePath() + "\"";
                fileD = new File(userHomeDir + "/.truckliststudio/" + "DCalc.bat");
                FileOutputStream fosD;
                Writer dosD = null;
                try {
                    fosD = new FileOutputStream(fileD);
                    dosD = new OutputStreamWriter(fosD);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    if (dosD != null) {
                        dosD.write(infoCmd + "\n");
                        dosD.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                fileD.setExecutable(true);
                batchDurationComm = userHomeDir + "/.truckliststudio/" + "DCalc.bat";
            } else {
                if (BackEnd.avconvDetected()) {
                    infoCmd = "avconv -i " + "\"" + file.getAbsolutePath() + "\"";
                } else {
                    infoCmd = "ffmpeg -i " + "\"" + file.getAbsolutePath() + "\"";
                }
                fileD = new File(userHomeDir + "/.truckliststudio/" + "DCalc.sh");
                FileOutputStream fosD;
                Writer dosD = null;
                try {
                    fosD = new FileOutputStream(fileD);
                    dosD = new OutputStreamWriter(fosD);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    if (dosD != null) {
                        dosD.write("#!/bin/bash\n");
                        dosD.write(infoCmd + "\n");
                        dosD.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                fileD.setExecutable(true);
                batchDurationComm = userHomeDir + "/.truckliststudio/" + "DCalc.sh";
            }
//            System.out.println(infoCmd);

            try {
                Process duration = rt.exec(batchDurationComm);
                boolean audiofind = false;
                Tools.sleep(10);
//                duration.waitFor(); //Author spoonybard896
                InputStream lsOut = duration.getErrorStream();
                InputStreamReader isr = new InputStreamReader(lsOut);
                BufferedReader in = new BufferedReader(isr);
                String lineR;

                while ((lineR = in.readLine()) != null) {
                    if (lineR.contains("Duration:") && !fileType.equals("pic")) {
                        lineR = lineR.replaceFirst("Duration: ", "");
                        lineR = lineR.trim();
                        String resu = lineR.substring(0, 8);
                        String[] temp;
                        temp = resu.split(":");
                        int hours = Integer.parseInt(temp[0]);
                        int minutes = Integer.parseInt(temp[1]);
                        int seconds = Integer.parseInt(temp[2]);
                        int totalTime = hours * 3600 + minutes * 60 + seconds;
                        String strDuration = Integer.toString(totalTime);
                        stream.setStreamTime(strDuration + "s");
                    }

                    if (lineR.contains("Audio:")) {
                        if (lineR.contains("0 channels")) {
                            audiofind = false;
                        } else {
                            audiofind = true;
                        }
                    }

                    if (autoAR && !fileType.equals("mus")) {
                        if (lineR.contains("Video:")) {
                            if (lineR.toLowerCase().contains("could not find")) {
                                System.out.println("Cannot get Video parameters from this Source !!!");
                            } else {
                                String[] videoNativeSize = null;
                                int w = 0;
                                int h = 0;
                                if (os == OS.WINDOWS) {
                                    String[] lineRParts = lineR.split(",");
                                    String[] tempNativeSize = lineRParts[3].split(" ");
                                    videoNativeSize = tempNativeSize[1].split("x");
                                } else {
                                    String[] lineRParts = lineR.split(",");
                                    String[] tempNativeSize = lineRParts[2].split(" ");
                                    videoNativeSize = tempNativeSize[1].split("x");
                                }
                                try {
                                    w = Integer.parseInt(videoNativeSize[0]);
                                    h = Integer.parseInt(videoNativeSize[1]);
                                } catch (NumberFormatException e) {
                                    System.out.println("Number Format Exception! ...trying MS Way...");
                                    String[] lineRParts = lineR.split(",");
                                    String[] tempNativeSize = lineRParts[3].split(" ");
                                    videoNativeSize = tempNativeSize[1].split("x");
                                    w = Integer.parseInt(videoNativeSize[0]);
                                    h = Integer.parseInt(videoNativeSize[1]);
                                }

                                int mixerW = MasterMixer.getInstance().getWidth();
                                int mixerH = MasterMixer.getInstance().getHeight();
                                int hAR = (mixerW * h) / w;
                                int wAR = (mixerH * w) / h;
                                if (hAR > mixerH) {
                                    hAR = mixerH;
                                    int xPos = (mixerW - wAR) / 2;
                                    stream.setX(xPos);
                                    stream.setWidth(wAR);
                                }
                                if (w > mixerW) {
                                    int yPos = (mixerH - hAR) / 2;
                                    stream.setY(yPos);
                                    stream.setHeight(hAR);
                                } else if (h < mixerH) {
                                    int yPos = (mixerH - hAR) / 2;
                                    stream.setY(yPos);
                                } else {
                                    hAR = mixerH;
                                }
                                stream.setHeight(hAR);
                            }
                        }
                    }
                }
//                System.out.println(audiofind);
                stream.setOnlyVideo(!audiofind);
                stream.setAudio(audiofind);
            } catch (IOException e) {
            }
        }
    }

    public static String wsDistroWatch() {
        String system = null;
        Runtime rt = Runtime.getRuntime();
        String distroCmd = "uname -a";
        if (os == OS.LINUX) {
            try {
                Process distroProc = rt.exec(distroCmd);
                Tools.sleep(10);
                distroProc.waitFor();
                BufferedReader buf = new BufferedReader(new InputStreamReader(
                        distroProc.getInputStream()));
                String lineR;
                while ((lineR = buf.readLine()) != null) {
                    if (lineR.toLowerCase().contains("ubuntu")) {
                        system = "ubuntu";
                    } else {
                        system = "others";
                    }
                }
            } catch (IOException | InterruptedException | NumberFormatException e) {
            }
        } else if (os == OS.WINDOWS) {
            system = "windows";
        }

        return system;
    }

    @SuppressWarnings("unchecked")
    private void btnLoadStudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadStudioActionPerformed
        final java.awt.event.ActionEvent fEvt = evt;
        ArrayList<Stream> streamzI = MasterTracks.getInstance().getStreams();
        ArrayList<String> sourceChI = MasterTracks.getInstance().getTracks();
        int sinkStream = 0;
        for (Stream s : streamzI) {
            if (s.getClass().toString().contains("Sink")) {
                sinkStream++;
            }
        }
        if (streamzI.size() - sinkStream > 0 || sourceChI.size() > 0) {
            Object[] options = {"OK"};
            JOptionPane.showOptionDialog(this,
                    "Current Studio will be closed !!!", "Attention",
                    JOptionPane.PLAIN_MESSAGE,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);
        }
        JFileChooser chooser = new JFileChooser(lastFolder);
        FileNameExtensionFilter studioFilter = new FileNameExtensionFilter("Studio files (*.studio)", "studio");
        chooser.setFileFilter(studioFilter);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Load a Studio ...");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int retval = chooser.showOpenDialog(this);
        final File file = chooser.getSelectedFile();
        if (retval == JFileChooser.APPROVE_OPTION) {
            final WaitingDialog waitingD = new WaitingDialog(this);
            setTrkStudioState(false);
            editingPhase = false;
            SwingWorker<?, ?> worker = new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws InterruptedException {
                    if (file != null) {
                        lastFolder = file.getParentFile();
                        PrePlayer.getPreInstance(null).stop();
                        Tools.sleep(10);
                        MasterTracks.getInstance().endAllStream();
                        for (Stream s : MasterTracks.getInstance().getStreams()) {
                            s.updateStatus();
                        }
                        ArrayList<Stream> streamz = MasterTracks.getInstance().getStreams();
                        ArrayList<String> sourceCh = MasterTracks.getInstance().getTracks();
                        do {
                            for (int l = 0; l < streamz.size(); l++) {
                                Stream removeS = streamz.get(l);
                                Tools.sleep(20);
                                removeS.destroy();
                                removeS = null;
                            }
                            for (int a = 0; a < sourceCh.size(); a++) {
                                String removeSc = sourceCh.get(a);
                                MasterTracks.getInstance().removeTrack(removeSc);
                                Tools.sleep(20);
                                listenerTSTP.removeTracks(removeSc, a);
                            }
                        } while (streamz.size() > 0 || sourceCh.size() > 0);
                        PrePlayer.getPreInstance(null).stop();
                        Tools.sleep(10);
                        MasterTracks.getInstance().endAllStream();
                        listenerTSTP.stopChTime(fEvt);
                        listenerTSTP.resetBtnStates(fEvt);
                        listenerOP.resetBtnStates(fEvt);
                        tabControls.removeAll();
                        lblSourceSelected.setText("");
                        tabControls.repaint();
                        Tools.sleep(300);

                        cleanDesktops();

                        Tools.sleep(50);
                        try {
                            Studio.LText = new ArrayList<>();
                            Studio.extstream = new ArrayList<>();
                            Studio.ImgMovMus = new ArrayList<>();
                            Studio.load(file, "load");
                            Studio.main();
                            spinWidth.setValue(MasterMixer.getInstance().getWidth());
                            spinHeight.setValue(MasterMixer.getInstance().getHeight());
                            spinFPS.setValue(MasterMixer.getInstance().getRate());
                            int mW = (Integer) spinWidth.getValue();
                            int mH = (Integer) spinHeight.getValue();
                            MasterMixer.getInstance().stop();
                            MasterMixer.getInstance().setWidth(mW);
                            MasterMixer.getInstance().setHeight(mH);
                            MasterMixer.getInstance().setRate((Integer) spinFPS.getValue());
                            MasterMixer.getInstance().start();
                            PreviewMixer.getInstance().stop();
                            PreviewMixer.getInstance().setWidth(mW);
                            PreviewMixer.getInstance().setHeight(mH);
                            PreviewMixer.getInstance().start();
                        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
                            Logger.getLogger(TrucklistStudio.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        // loading studio streams
                        int cont = 0;
                        int barValue = 0;
                        int streamsPlusTracks = Studio.ImgMovMus.size() + Studio.LText.size() + MasterTracks.getInstance().getTracks().size();
                        for (int u = 0; u < Studio.ImgMovMus.size(); u++) {
                            Stream s = Studio.extstream.get(u);
                            if (s != null) {
                                StreamPanel frame = new StreamPanel(s);
                                String sName = s.getName();
                                String[] tokens = split(sName, 10);
                                String splitTitle = "";
                                for (String token : tokens) {
                                    splitTitle = splitTitle + token + "<span></span>";
                                }
                                if (s instanceof SourceMovie) {
                                    numVideos += 1;
                                    videoDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                                    lblVideo.setForeground(busyTab);
                                    Font font = new Font("Ubuntu", Font.BOLD, 11);
                                    lblVideo.setFont(font);
                                    lblVideo.setText("Videos(" + numVideos + ")");
                                } else if (s instanceof SourceMusic) {
                                    numMusics += 1;
                                    musicDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                                    lblMusic.setForeground(busyTab);
                                    Font font = new Font("Ubuntu", Font.BOLD, 11);
                                    lblMusic.setFont(font);
                                    lblMusic.setText("Musics(" + numMusics + ")");
                                } else if (s instanceof SourceImage || s instanceof SourceImageGif) { //|| s instanceof SourceImageU 
                                    numPictures += 1;
                                    pictureDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                                    lblPicture.setForeground(busyTab);
                                    Font font = new Font("Ubuntu", Font.BOLD, 11);
                                    lblPicture.setFont(font);
                                    lblPicture.setText("Pictures(" + numPictures + ")");
                                }
                                barValue = (100 * (cont + 1)) / (streamsPlusTracks);
                                jProgressBar1.setValue(barValue);
                                cont++;
                                s.setLoaded(false);
                                frame.setParent();
                            }
                            if (cancel) {
                                break;
                            }
//                            System.out.println("Adding Source: " + s.getName());
                        }
                        Studio.extstream.clear();
                        Studio.extstream = null;
                        Studio.ImgMovMus.clear();
                        Studio.ImgMovMus = null;
                        for (SourceText text : Studio.LText) {
                            if (cancel) {
                                break;
                            }
                            if (text != null) {
                                StreamPanelText frame = new StreamPanelText((Stream) text);
                                numTexts += 1;
                                textDesktop.add(text.getName(), frame);
                                lblText.setForeground(busyTab);
                                Font font = new Font("Ubuntu", Font.BOLD, 11);
                                lblText.setFont(font);
                                lblText.setText("Texts(" + numTexts + ")");
                                barValue = (100 * (cont + 1)) / (streamsPlusTracks);
                                jProgressBar1.setValue(barValue);
                                cont++;
                                text.setLoaded(false);
                                frame.setParent();
                            }
//                            System.out.println("Adding Source: " + text.getName());

                        }
                        Studio.LText.clear();
                        Studio.LText = null;
                        Tools.sleep(300);
                        // loading studio tracks
                        for (String chsc : MasterTracks.getInstance().getTracks()) {
                            if (cancel) {
                                break;
                            }
                            Tools.sleep(10);
                            listenerTSTP.addLoadingTrack(chsc);
                            barValue = (100 * (cont + 1)) / (streamsPlusTracks);
                            jProgressBar1.setValue(barValue);
                            cont++;
                        }
                        Studio.trackLoad.clear();
                        listenerOP.resetSinks(fEvt);
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Studio is loaded!");
                        ResourceMonitor.getInstance().addMessage(label);
                        setTitle("TrucklistStudio " + Version.version + " (" + file.getName() + ")");
                    }
                    setTrkStudioState(true);
                    editingPhase = true;
                    cancel = false;
                    return null;
                }

                @Override
                protected void done() {
                    waitingD.dispose();
                }
            };
            worker.execute();
            waitingD.toFront();
            waitingD.setVisible(true);
        } else {
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Loading Cancelled!");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_btnLoadStudioActionPerformed

    private void TSAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TSAboutActionPerformed
        About TAbout = new About(about, true);
        TAbout.setLocationRelativeTo(TrucklistStudio.cboAnimations);
        TAbout.setVisible(true);
    }//GEN-LAST:event_TSAboutActionPerformed

    private void btnNewStudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewStudioActionPerformed
        boolean doNew = true;
        ArrayList<Stream> streamzI = MasterTracks.getInstance().getStreams();
        int sinkStream = 0;
        for (Stream s : streamzI) {
//            System.out.println("Stream: "+s);
            if (s.getClass().toString().contains("Sink")) {
                sinkStream++;
            }
        }
        ArrayList<String> sourceChI = MasterTracks.getInstance().getTracks();
        if (streamzI.size() - sinkStream > 0 || sourceChI.size() > 0) {
            int result = JOptionPane.showConfirmDialog(this, "Current Studio will be closed !!!", "Attention", JOptionPane.YES_NO_CANCEL_OPTION);
            switch (result) {
                case JOptionPane.YES_OPTION:
                    doNew = true;
                    break;
                case JOptionPane.NO_OPTION:
                    doNew = false;
                    break;
                case JOptionPane.CANCEL_OPTION:
                    doNew = false;
                    break;
                case JOptionPane.CLOSED_OPTION:
                    doNew = false;
                    break;
            }
        }
        if (doNew) {
            PrePlayer.getPreInstance(null).stop();
            Tools.sleep(10);
            MasterTracks.getInstance().endAllStream();
            for (Stream s : MasterTracks.getInstance().getStreams()) {
                s.updateStatus();
            }
            ArrayList<Stream> streamz = MasterTracks.getInstance().getStreams();
            ArrayList<String> sourceCh = MasterTracks.getInstance().getTracks();
            do {
                for (int l = 0; l < streamz.size(); l++) {
                    Stream removeS = streamz.get(l);
                    removeS.destroy();
                    removeS = null;
                }
                for (int a = 0; a < sourceCh.size(); a++) {
                    String removeSc = sourceCh.get(a);
                    MasterTracks.getInstance().removeTrack(removeSc);
                    listenerTSTP.removeTracks(removeSc, a);
                }
            } while (streamz.size() > 0 || sourceCh.size() > 0);
            listenerTSTP.stopChTime(evt);
            listenerTSTP.resetBtnStates(evt);
            listenerOP.resetBtnStates(evt);
            listenerOP.resetSinks(evt);
            tabControls.removeAll();
            lblSourceSelected.setText("");
            tabControls.repaint();
            Tools.sleep(300);

            cleanDesktops();

            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "New Studio Created.");
            ResourceMonitor.getInstance().addMessage(label);
            setTitle("TrucklistStudio " + Version.version);
        } else {
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "New Studio Cancelled.");
            ResourceMonitor.getInstance().addMessage(label);
        }
        System.gc();
    }//GEN-LAST:event_btnNewStudioActionPerformed

    private void cleanDesktops() {
        Font font = new Font("Ubuntu", Font.PLAIN, 11);
        numVideos = 0;
        lblVideo.setForeground(resetTab);
        lblVideo.setFont(font);
        lblVideo.setText("Videos(" + numVideos + ")");
        videoDesktop.removeAll();
        videoDesktop.repaint();

        numMusics = 0;
        lblMusic.setForeground(resetTab);
        lblMusic.setFont(font);
        lblMusic.setText("Musics(" + numMusics + ")");
        musicDesktop.removeAll();
        musicDesktop.repaint();

        numPictures = 0;
        lblPicture.setForeground(resetTab);
        lblPicture.setFont(font);
        lblPicture.setText("Pictures(" + numPictures + ")");
        pictureDesktop.removeAll();
        pictureDesktop.repaint();

        numTexts = 0;
        lblText.setForeground(resetTab);
        lblText.setFont(font);
        lblText.setText("Texts(" + numTexts + ")");
        textDesktop.removeAll();
        textDesktop.repaint();

    }

    private void cboAudioHzActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboAudioHzActionPerformed
        final String audioHz = cboAudioHz.getSelectedItem().toString();
        if (audioHz.equals("22050Hz")) {
            audioFreq = 22050;
        } else {
            audioFreq = 44100;
        }
        MasterMixer.getInstance().stop();
        PreviewMixer.getInstance().stop();
        Tools.sleep(100);
        PrePlayer.getPreInstance(null).stop();
        Tools.sleep(30);
        MasterTracks.getInstance().stopAllStream();
        for (Stream s : streamS) {
            s.updateStatus();
        }
        MasterMixer.getInstance().start();
        PreviewMixer.getInstance().start();
        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Audio set to: " + audioFreq + "Hz");
        ResourceMonitor.getInstance().addMessage(label);
    }//GEN-LAST:event_cboAudioHzActionPerformed

    @SuppressWarnings("unchecked")
    private void btnImportStudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportStudioActionPerformed
        final ArrayList<String> allStreams = new ArrayList<>();
        for (Stream str : MasterTracks.getInstance().getStreams()) {
            if (!str.toString().toLowerCase().contains("sink")) {
                allStreams.add(str.getName());
            }
        }
        JFileChooser chooser = new JFileChooser(lastFolder);
        FileNameExtensionFilter studioFilter = new FileNameExtensionFilter("Studio files (*.studio)", "studio");
        chooser.setFileFilter(studioFilter);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Import a Studio ...");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int retval = chooser.showOpenDialog(this);
        final File file = chooser.getSelectedFile();
        if (retval == JFileChooser.APPROVE_OPTION) {
            final WaitingDialog waitingD = new WaitingDialog(this);
            setTrkStudioState(false);
            editingPhase = false;
            SwingWorker<?, ?> worker = new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws InterruptedException {
                    if (file != null) {
                        lastFolder = file.getParentFile();
                        try {
                            Studio.LText = new ArrayList<>();
                            Studio.extstream = new ArrayList<>();
                            Studio.ImgMovMus = new ArrayList<>();
                            Studio.load(file, "add");
                            Studio.main();
                        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
                            Logger.getLogger(TrucklistStudio.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        int cont = 0;
                        int barValue = 0;
                        int streamsPlusTracks = Studio.ImgMovMus.size() + Studio.LText.size() + MasterTracks.getInstance().getTracks().size();
                        for (int u = 0; u < Studio.ImgMovMus.size(); u++) {
                            Tools.sleep(10);
                            Stream s = Studio.extstream.get(u);
                            boolean noDouble = true;
                            if (s != null) {
//                            System.out.println("Stream Ch: "+s.getTracks());
                                // to fix 0 channels .studio import
                                for (String str : allStreams) {
//                                    System.out.println("ComparedStreamName="+str);
                                    if (s.getName().equals(str) && s.getisATrack()) {
//                                        System.out.println("Double Stream !!!");
                                        noDouble = false;
                                        s.destroy();
                                    }
                                }
                                if (noDouble) {
                                    if (s.getTracks().isEmpty()) {
                                        ArrayList<String> allChan = new ArrayList<>();
                                        for (String scn : MasterTracks.getInstance().getTracks()) {
                                            allChan.add(scn);
//                                        System.out.println("Current Studio Ch: "+scn+" added.");
                                        }
                                        for (String sc : allChan) {
                                            s.addTrack(SourceTrack.getTrack(sc, s));
                                        }
                                    }
                                    StreamPanel frame = new StreamPanel(s);
                                    String sName = s.getName();
                                    String[] tokens = split(sName, 10);
                                    String splitTitle = "";
                                    for (String token : tokens) {
                                        splitTitle = splitTitle + token + "<span></span>";
                                    }

                                    if (s instanceof SourceMovie) {
                                        numVideos += 1;
                                        videoDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                                        lblVideo.setForeground(busyTab);
                                        Font font = new Font("Ubuntu", Font.BOLD, 11);
                                        lblVideo.setFont(font);
                                        lblVideo.setText("Videos(" + numVideos + ")");
                                    } else if (s instanceof SourceMusic) {
                                        numMusics += 1;
                                        musicDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                                        lblMusic.setForeground(busyTab);
                                        Font font = new Font("Ubuntu", Font.BOLD, 11);
                                        lblMusic.setFont(font);
                                        lblMusic.setText("Musics(" + numMusics + ")");
                                    } else if (s instanceof SourceImage || s instanceof SourceImageGif) { //|| s instanceof SourceImageU 
                                        numPictures += 1;
                                        pictureDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                                        lblPicture.setForeground(busyTab);
                                        Font font = new Font("Ubuntu", Font.BOLD, 11);
                                        lblPicture.setFont(font);
                                        lblPicture.setText("Pictures(" + numPictures + ")");
                                    }
                                    barValue = (100 * (cont + 1)) / (streamsPlusTracks);
                                    jProgressBar1.setValue(barValue);
                                    cont++;
                                    if (cancel) {
                                        break;
                                    }
                                    s.setLoaded(false);
                                    frame.setParent();
                                }
                            }
                        }
                        Studio.extstream.clear();
                        Studio.extstream = null;
                        Studio.ImgMovMus.clear();
                        Studio.ImgMovMus = null;
                        for (int t = 0; t < Studio.LText.size(); t++) {
                            if (cancel) {
                                break;
                            }
                            SourceText text = Studio.LText.get(t);
                            // to fix 0 channels .studio import
                            if (text.getTracks().isEmpty()) {
                                ArrayList<String> allChan = new ArrayList<>();
                                for (String scn : MasterTracks.getInstance().getTracks()) {
                                    allChan.add(scn);
//                                    System.out.println("Current Studio Ch: "+scn+" added.");
                                }
                                for (String sc : allChan) {
                                    text.addTrack(SourceTrack.getTrack(sc, text));
                                }
                            }
                            StreamPanelText frame = new StreamPanelText((Stream) text);
                            numTexts += 1;
                            textDesktop.add(text.getName(), frame);
                            lblText.setForeground(busyTab);
                            Font font = new Font("Ubuntu", Font.BOLD, 11);
                            lblText.setFont(font);
                            lblText.setText("Texts(" + numTexts + ")");
                            barValue = (100 * (cont + 1)) / (streamsPlusTracks);
                            jProgressBar1.setValue(barValue);
                            cont++;
                            text.setLoaded(false);
                            frame.setParent();
                        }
                        Studio.LText.clear();
                        Studio.LText = null;
                        Tools.sleep(300);
                        MasterTracks master = MasterTracks.getInstance(); //
                        ArrayList<String> chNameL = new ArrayList<>();
                        for (SourceTrack chsct : Studio.trackLoad) {
                            if (cancel) {
                                break;
                            }
                            chNameL.add(chsct.getName());
//                                System.out.println("TrackLoad="+chsct.getName());
                        }
                        LinkedHashSet<String> hs = new LinkedHashSet<>(chNameL);
                        chNameL.clear();
                        chNameL.addAll(hs);
                        for (String chsct : chNameL) {
                            if (cancel) {
                                break;
                            }
                            listenerTSTP.addLoadingTrack(chsct);
                            master.insertStudio(chsct);
                            barValue = (100 * (cont + 1)) / (streamsPlusTracks);
                            jProgressBar1.setValue(barValue);
                            cont++;
                        }

                        Studio.trackLoad.clear();
                        ArrayList<String> allTracks = new ArrayList<>();
                        for (String scn : master.getInstance().getTracks()) {
                            allTracks.add(scn);
                        }
                        for (Stream s : master.getStreams()) {
                            if (cancel) {
                                break;
                            }
                            if (s.getisATrack()) {
                                int i = 0;
                                for (String track : allTracks) {
                                    boolean cloned = false;
                                    if (track.contains(s.getTrkName()) && track.contains("(") && track.endsWith(")")) {
                                        cloned = true;
                                    }
                                    if (s.getTrkName().equals(track) || cloned) {
                                        boolean backState = false;
                                        if (s.isPlaying()) {
                                            backState = true;
                                        }
                                        s.setIsPlaying(true);
                                        s.addTrackAt(SourceTrack.getTrack(allTracks.get(i), s), i);
                                        if (backState) {
                                            s.setIsPlaying(true);
                                        } else {
                                            s.setIsPlaying(false);
                                        }
                                    } else {
                                        s.addTrackAt(SourceTrack.getTrackIgnorePlay(allTracks.get(i), s), i);
                                    }
                                    i++;
                                }
                            }
                        }
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Studio is Imported!");
                        ResourceMonitor.getInstance().addMessage(label);
                    } else {
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Import Cancelled!");
                        ResourceMonitor.getInstance().addMessage(label);
                    }
                    setTrkStudioState(true);
                    editingPhase = true;
                    cancel = false;
                    return null;
                }

                @Override
                protected void done() {
                    waitingD.dispose();
                }
            };
            worker.execute();
            waitingD.toFront();
            waitingD.setVisible(true);
        } else {
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Import Cancelled!");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_btnImportStudioActionPerformed

    private void tglFFmpegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglFFmpegActionPerformed
        if (tglFFmpeg.isSelected()) {
            tglAVconv.setSelected(false);
            tglGst.setSelected(false);
            outFMEbe = 0;
            listenerOP.resetSinks(evt);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Outputs switched to FFmpeg.");
            ResourceMonitor.getInstance().addMessage(label);
        } else {
            outFMEbe = 2;
            tglAVconv.setEnabled(avconv);
            tglGst.setEnabled(true);
            tglGst.setSelected(true);
            listenerOP.resetSinks(evt);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Outputs switched to GStreamer.");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_tglFFmpegActionPerformed

    public void setTrkStudioState(boolean state) {
        this.setEnabled(state);
    }

    private void btnAddFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFolderActionPerformed
        ArrayList<String> allTracks = new ArrayList<>();
        for (String scn : MasterTracks.getInstance().getTracks()) {
            allTracks.add(scn);
        }
        final ArrayList<String> allStreams = new ArrayList<>();
        for (Stream str : MasterTracks.getInstance().getStreams()) {
            if (!str.toString().toLowerCase().contains("sink")) {
                allStreams.add(str.getName());
            }
        }

        JFileChooser chooser = new JFileChooser(lastFolder);
        FileNameExtensionFilter mediaFilter = new FileNameExtensionFilter("Supported Media files", "avi", "ogg", "jpeg", "ogv", "mp4", "m4v", "mpg", "divx", "wmv", "flv", "mov", "mkv", "vob", "jpg", "bmp", "png", "gif", "mp3", "wav", "wma", "m4a", ".mp2");
        chooser.setFileFilter(mediaFilter);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Add Media Folder ...");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int retVal = chooser.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            final File dir = chooser.getSelectedFile();
//            System.out.println("Dir: "+dir);
            final WaitingDialog waitingD = new WaitingDialog(this);
            setTrkStudioState(false);
            editingPhase = false;
            SwingWorker<?, ?> worker = new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws InterruptedException {

                    File[] contents = null;
                    if (dir != null) {
                        lastFolder = dir.getAbsoluteFile();
                        contents = dir.listFiles();
//                        for ( File f : contents) {
//                            String fileName = f.getName();
//                            System.out.println("Name: " + fileName);
//                        }
                        int cont = 0;
                        for (File file : contents) {
                            Stream s = Stream.getInstance(file);
                            boolean noError = true;
                            boolean noDouble = true;

                            if (s != null) {
                                boolean isMovie = s instanceof SourceMovie;
                                boolean isMusic = s instanceof SourceMusic;
                                if (isMovie || isMusic) {
                                    getStreamParams(s, file, null);
                                    if (s.getStreamTime().equals("N/A")) {
                                        noError = false;
                                        s.destroy();
                                    }
//                                    System.out.println("StreamName="+s.getName());
                                    for (String str : allStreams) {
//                                        System.out.println("ComparedStreamName="+str);
                                        if (s.getName().equals(str)) {
//                                            System.out.println("Double Stream !!!");
                                            noDouble = false;
                                            s.destroy();
                                        }
                                    }
                                    if (noError && noDouble) {
                                        StreamPanel frame = new StreamPanel(s);
                                        String sName = s.getName();
                                        String[] tokens = split(sName, 10);
                                        String splitTitle = "";
                                        for (String token : tokens) {
                                            splitTitle = splitTitle + token + "<span></span>";
                                        }

                                        if (s instanceof SourceMovie) {
                                            numVideos += 1;
                                            videoDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                                            lblVideo.setForeground(busyTab);
                                            Font font = new Font("Ubuntu", Font.BOLD, 11);
                                            lblVideo.setFont(font);
                                            lblVideo.setText("Videos(" + numVideos + ")");
                                        } else if (s instanceof SourceMusic) {
                                            numMusics += 1;
                                            musicDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                                            lblMusic.setForeground(busyTab);
                                            Font font = new Font("Ubuntu", Font.BOLD, 11);
                                            lblMusic.setFont(font);
                                            lblMusic.setText("Musics(" + numMusics + ")");
                                        }
//                                        System.out.println("Adding Source: " + s.getName());
                                        int barValue = (100 * (cont + 1)) / (contents.length);
                                        jProgressBar1.setValue(barValue);
//                                        System.out.println("Bar Value: " + barValue);
                                        cont++;
                                        frame.setParent();
                                        if (autoTrack) {
                                            TrackPanel.makeATrack(s);
                                        }
                                        if (autoTitle && s.getisATrack()) {
                                            boolean isTitle = false;
                                            SourceText streamTXT = null;
                                            String trkName = s.getName();
                                            String trkTitle = FilenameUtils.removeExtension(trkName);
                                            for (Stream str : master.getStreams()) {

                                                if (str.getisATitle() && !str.getClass().toString().contains("Sink")) {
//                                                    System.out.println("Titles=" + str.getName());
                                                    streamTXT = (SourceText) str;
                                                    streamTXT.setContent(trkTitle);
                                                    streamTXT.setIsPlaying(true);

                                                    Stream playingStr = null;
                                                    for (Stream stream : master.getStreams()) {
                                                        if (stream.getisATrack() && stream.isPlaying()) {
                                                            playingStr = stream;
                                                            stream.setIsPlaying(false);
                                                        }
                                                    }

                                                    boolean wasStopped = true;
                                                    if (s.isPlaying()) {
                                                        wasStopped = false;
                                                    } else {
                                                        s.setIsPlaying(true);
                                                    }

                                                    master.updateTrack(trkName);
                                                    master.addTrkTransitions(trkName);

                                                    if (wasStopped) {
                                                        s.setIsPlaying(false);
                                                    }

                                                    if (playingStr != null) {
                                                        playingStr.setIsPlaying(true);
                                                    }

                                                    streamTXT.setIsPlaying(false);
                                                    isTitle = true;
                                                    break;
                                                }
                                            }
                                            if (!isTitle) {

                                                streamTXT = new SourceText(trkTitle);
                                                for (String scn : MasterTracks.getInstance().getTracks()) {
                                                    allTracks.add(scn);
                                                }
                                                for (String sc : allTracks) {
                                                    streamTXT.addTrack(SourceTrack.getTrack(sc, streamTXT));
                                                }
                                                streamTXT.setisATitle(true);
                                                streamTXT.setName("Titles");
                                                streamTXT.setZOrder(1);
                                                streamTXT.setIsPlaying(true);
                                                boolean wasStopped = true;
                                                if (s.isPlaying()) {
                                                    wasStopped = false;
                                                } else {
                                                    s.setIsPlaying(true);
                                                }
                                                master.updateTrack(trkName);
                                                master.addTrkTransitions(trkName);
                                                if (wasStopped) {
                                                    s.setIsPlaying(false);
                                                }
                                                streamTXT.setIsPlaying(false);
                                                StreamPanelText txtTitle = new StreamPanelText((Stream) streamTXT);
                                                numTexts += 1;
                                                textDesktop.add(streamTXT.getName(), txtTitle);
                                                lblText.setForeground(busyTab);
                                                Font font = new Font("Ubuntu", Font.BOLD, 11);
                                                lblText.setFont(font);
                                                lblText.setText("Texts(" + numTexts + ")");
                                                txtTitle.setParent();
                                            }
                                        }
                                    } else {
                                        if (!noError) {
                                            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Error adding " + file.getName() + "!");
                                            ResourceMonitor.getInstance().addMessage(label);
                                        }
                                        if (!noDouble) {
                                            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, file.getName() + " Duplicated!");
                                            ResourceMonitor.getInstance().addMessage(label);
                                        }
                                    }
                                } else {
                                    System.out.println("Not a Movie/Music !!!");
                                    s.destroy();
                                }
                            }
                            if (cancel) {
                                break;
                            }
                        }
                        cancel = false;
                        MasterTracks master = MasterTracks.getInstance(); //
                        ArrayList<String> allTrack = new ArrayList<>();
                        for (String scn : master.getInstance().getTracks()) {
                            allTrack.add(scn);
                        }

                        for (String track : allTrack) {
                            master.insertStudio(track);
                        }

                        for (Stream s : master.getStreams()) {
                            if (s.getisATrack()) {
                                int i = 0;
                                for (String track : allTrack) {
                                    boolean cloned = false;
                                    if (track.contains(s.getTrkName()) && track.contains("(") && track.endsWith(")")) {
                                        cloned = true;
                                    }
                                    if (s.getTrkName().equals(track) || cloned) {
                                        boolean backState = false;
                                        if (s.isPlaying()) {
                                            backState = true;
                                        }
                                        s.setIsPlaying(true);
                                        s.addTrackAt(SourceTrack.getTrack(allTrack.get(i), s), i);
                                        if (backState) {
                                            s.setIsPlaying(true);
                                        } else {
                                            s.setIsPlaying(false);
                                        }
                                    } else {
                                        s.addTrackAt(SourceTrack.getTrackIgnorePlay(allTrack.get(i), s), i);
                                    }
                                    i++;
                                }
                            }
                        }
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Folder Import is Complete.");
                        ResourceMonitor.getInstance().addMessage(label);
                    } else {
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "No Folder Selected!");
                        ResourceMonitor.getInstance().addMessage(label);
                    }
                    setTrkStudioState(true);
                    editingPhase = true;
                    return null;

                }

                @Override
                protected void done() {
                    waitingD.dispose();
                }
            };
            worker.execute();

            waitingD.toFront();

            waitingD.setVisible(
                    true);

        } else {
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Loading Cancelled!");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_btnAddFolderActionPerformed

    private void tglAVconvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglAVconvActionPerformed
        if (tglAVconv.isSelected()) {
            tglFFmpeg.setSelected(false);
            tglGst.setSelected(false);
            outFMEbe = 1;
            listenerOP.resetSinks(evt);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Outputs switched to Libav.");
            ResourceMonitor.getInstance().addMessage(label);
        } else {
            outFMEbe = 2;
            tglFFmpeg.setEnabled(ffmpeg);
            tglGst.setEnabled(true);
            tglGst.setSelected(true);
            listenerOP.resetSinks(evt);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Outputs switched to Gstreamer.");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_tglAVconvActionPerformed

    private void tglGstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglGstActionPerformed
        if (tglGst.isSelected()) {
            tglFFmpeg.setSelected(false);
            tglAVconv.setSelected(false);
            outFMEbe = 2;
            listenerOP.resetSinks(evt);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Outputs switched to GStreamer.");
            ResourceMonitor.getInstance().addMessage(label);
        } else {
            if (ffmpeg && !avconv) {
                outFMEbe = 0;
                tglFFmpeg.setSelected(true);
                tglAVconv.setEnabled(false);
                tglGst.setEnabled(true);
            } else if (ffmpeg && avconv) {
                outFMEbe = 1;
                tglFFmpeg.setEnabled(true);
                tglAVconv.setSelected(true);
                tglGst.setEnabled(true);

            } else {
                outFMEbe = 1;
                tglFFmpeg.setEnabled(false);
                tglAVconv.setSelected(true);
                tglGst.setEnabled(true);
            }

            listenerOP.resetSinks(evt);
            if (outFMEbe == 1) {
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Outputs switched to Libav.");
                ResourceMonitor.getInstance().addMessage(label);
            } else {
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Outputs switched to FFmpeg.");
                ResourceMonitor.getInstance().addMessage(label);
            }
        }
    }//GEN-LAST:event_tglGstActionPerformed

    private void btnSysGCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSysGCActionPerformed
        System.gc();
    }//GEN-LAST:event_btnSysGCActionPerformed

    private void cboThemeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboThemeActionPerformed
        final String themeSW = cboTheme.getSelectedItem().toString();
        if (themeSW.equals("Classic")) {
            theme = "Classic";
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Theme set to \"" + theme + "\"");
            ResourceMonitor.getInstance().addMessage(label);
        } else {
            theme = "Dark";
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Theme set to \"" + theme + "\"");
            ResourceMonitor.getInstance().addMessage(label);
        }
        Thread wsRestart = new Thread(new Runnable() {
            @Override
            public void run() {
                restartDialog();
            }
        });
        if (!firstRun) {
            wsRestart.start();
        }
    }//GEN-LAST:event_cboThemeActionPerformed

    private void tglAutoARActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglAutoARActionPerformed
        if (tglAutoAR.isSelected()) {
            autoAR = true;
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Media Aspect Ratio detection \"On\"");
            ResourceMonitor.getInstance().addMessage(label);
        } else {
            autoAR = false;
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Media Aspect Ratio detection \"Off\"");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_tglAutoARActionPerformed

    private void tglAutoTrackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglAutoTrackActionPerformed
        if (tglAutoTrack.isSelected()) {
            autoTrack = true;
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Media AutoTrack \"On\"");
            ResourceMonitor.getInstance().addMessage(label);
        } else {
            autoTrack = false;
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Media AutoTrack \"Off\"");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_tglAutoTrackActionPerformed

    private void btnMinimizeAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMinimizeAllActionPerformed

    }//GEN-LAST:event_btnMinimizeAllActionPerformed

    private void tabSourcesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabSourcesMouseClicked
        JTabbedPane tabPane = (JTabbedPane) evt.getSource();
        int selIndex = tabPane.getSelectedIndex();
        if (selIndex != -1) {
            String streamName = tabPane.getTitleAt(selIndex).replace("<html><body><table width='20'>", "").replace("</table></body></html>", "").replace("<span></span>", "");
            for (Stream s : streamS) {
                if (s.getName().equals(streamName)) {
                    selectedSource(s);
                }
            }
        }
    }//GEN-LAST:event_tabSourcesMouseClicked

    private void videoDesktopMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_videoDesktopMouseWheelMoved
        JTabbedPane tabPane = (JTabbedPane) evt.getSource();
        int dir = evt.getWheelRotation();
        int selIndex = tabPane.getSelectedIndex();
        int maxIndex = tabPane.getTabCount() - 1;
        if ((selIndex == 0 && dir < 0) || (selIndex == maxIndex && dir > 0)) {
            selIndex = maxIndex - selIndex;
        } else {
            selIndex += dir;
        }
        tabPane.setSelectedIndex(selIndex);
    }//GEN-LAST:event_videoDesktopMouseWheelMoved

    private void musicDesktopMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_musicDesktopMouseWheelMoved
        JTabbedPane tabPane = (JTabbedPane) evt.getSource();
        int dir = evt.getWheelRotation();
        int selIndex = tabPane.getSelectedIndex();
        int maxIndex = tabPane.getTabCount() - 1;
        if ((selIndex == 0 && dir < 0) || (selIndex == maxIndex && dir > 0)) {
            selIndex = maxIndex - selIndex;
        } else {
            selIndex += dir;
        }
        tabPane.setSelectedIndex(selIndex);
    }//GEN-LAST:event_musicDesktopMouseWheelMoved

    private void pictureDesktopMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_pictureDesktopMouseWheelMoved
        JTabbedPane tabPane = (JTabbedPane) evt.getSource();
        int dir = evt.getWheelRotation();
        int selIndex = tabPane.getSelectedIndex();
        int maxIndex = tabPane.getTabCount() - 1;
        if ((selIndex == 0 && dir < 0) || (selIndex == maxIndex && dir > 0)) {
            selIndex = maxIndex - selIndex;
        } else {
            selIndex += dir;
        }
        tabPane.setSelectedIndex(selIndex);
    }//GEN-LAST:event_pictureDesktopMouseWheelMoved

    private void textDesktopMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_textDesktopMouseWheelMoved
        JTabbedPane tabPane = (JTabbedPane) evt.getSource();
        int dir = evt.getWheelRotation();
        int selIndex = tabPane.getSelectedIndex();
        int maxIndex = tabPane.getTabCount() - 1;
        if ((selIndex == 0 && dir < 0) || (selIndex == maxIndex && dir > 0)) {
            selIndex = maxIndex - selIndex;
        } else {
            selIndex += dir;
        }
        tabPane.setSelectedIndex(selIndex);
    }//GEN-LAST:event_textDesktopMouseWheelMoved

    private void videoDesktopStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_videoDesktopStateChanged
        if (editingPhase) {
            JTabbedPane tabPane = (JTabbedPane) evt.getSource();
            int selIndex = tabPane.getSelectedIndex();
            if (selIndex != -1) {
                String streamName = tabPane.getTitleAt(selIndex).replace("<html><body><table width='20'>", "").replace("</table></body></html>", "").replace("<span></span>", "");
                for (Stream s : streamS) {
                    if (s.getName().equals(streamName)) {
                        selectedSource(s);
                        if (s.isPlaying()) {
                            btnRemoveSource.setEnabled(false);
                        } else {
                            btnRemoveSource.setEnabled(true);
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_videoDesktopStateChanged

    private void musicDesktopStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_musicDesktopStateChanged
        if (editingPhase) {
            JTabbedPane tabPane = (JTabbedPane) evt.getSource();
            int selIndex = tabPane.getSelectedIndex();
            if (selIndex != -1) {
                String streamName = tabPane.getTitleAt(selIndex).replace("<html><body><table width='20'>", "").replace("</table></body></html>", "").replace("<span></span>", "");
                for (Stream s : streamS) {
                    if (s.getName().equals(streamName)) {
                        selectedSource(s);
                        if (s.isPlaying()) {
                            btnRemoveSource.setEnabled(false);
                        } else {
                            btnRemoveSource.setEnabled(true);
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_musicDesktopStateChanged

    private void pictureDesktopStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pictureDesktopStateChanged
        if (editingPhase) {
            JTabbedPane tabPane = (JTabbedPane) evt.getSource();
            int selIndex = tabPane.getSelectedIndex();
            if (selIndex != -1) {
                String streamName = tabPane.getTitleAt(selIndex).replace("<html><body><table width='20'>", "").replace("</table></body></html>", "").replace("<span></span>", "");
                for (Stream s : streamS) {
                    if (s.getName().equals(streamName)) {
                        selectedSource(s);
                        if (s.isPlaying()) {
                            btnRemoveSource.setEnabled(false);
                        } else {
                            btnRemoveSource.setEnabled(true);
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_pictureDesktopStateChanged

    private void textDesktopStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_textDesktopStateChanged
        if (editingPhase) {
            JTabbedPane tabPane = (JTabbedPane) evt.getSource();
            int selIndex = tabPane.getSelectedIndex();
            if (selIndex != -1) {
                String streamName = tabPane.getTitleAt(selIndex);
                for (Stream s : streamS) {
                    if (s.getName().equals(streamName)) {
                        selectedSource(s);
                        if (s.isPlaying()) {
                            btnRemoveSource.setEnabled(false);
                        } else {
                            btnRemoveSource.setEnabled(true);
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_textDesktopStateChanged

    private void tabSourcesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabSourcesStateChanged
        if (editingPhase) {
            JTabbedPane tabPane = (JTabbedPane) evt.getSource();
            int tabIndex = tabPane.getSelectedIndex();
            String tabTitle = tabSources.getTitleAt(tabIndex);
            String sName = "";
            JTabbedPane tabPaneSelected = null;

            if (tabTitle.contains("Videos")) {
                tabPaneSelected = videoDesktop;
            } else if (tabTitle.contains("Musics")) {
                tabPaneSelected = musicDesktop;
            } else if (tabTitle.contains("Pictures")) {
                tabPaneSelected = pictureDesktop;
            } else if (tabTitle.contains("Texts")) {
                tabPaneSelected = textDesktop;
            }
            int selIndex = tabPaneSelected.getSelectedIndex();

            if (selIndex != -1) {
                String streamName = tabPaneSelected.getTitleAt(selIndex).replace("<html><body><table width='20'>", "").replace("</table></body></html>", "").replace("<span></span>", "");
                for (Stream s : streamS) {
                    if (s.getName().equals(streamName)) {
                        selectedSource(s);
                        if (s.isPlaying()) {
                            btnRemoveSource.setEnabled(false);
                        } else {
                            btnRemoveSource.setEnabled(true);
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_tabSourcesStateChanged

    private void tglAutoTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglAutoTitleActionPerformed
        if (tglAutoTitle.isSelected()) {
            autoTitle = true;
        } else {
            autoTitle = false;
        }
    }//GEN-LAST:event_tglAutoTitleActionPerformed

    private void btnApplyTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApplyTitleActionPerformed
        int result = JOptionPane.showConfirmDialog(this, "!!! Warning !!! Overwrites all previous Tracks titles.", "Attention", JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == JFileChooser.APPROVE_OPTION) {
            
            ArrayList<Stream> trkList = new ArrayList<>();
            for (Stream s : master.getStreams()) {
                if (s.getisATrack() && !s.getClass().toString().contains("Sink")) {
                    trkList.add(s);
                }
            }
            ArrayList<String> allTracks = new ArrayList<>();
            for (String scn : MasterTracks.getInstance().getTracks()) {
                allTracks.add(scn);
            }
            for (Stream s : trkList) {
                boolean isTitle = false;
                SourceText streamTXT = null;
                String trkName = s.getName();
                String trkTitle = FilenameUtils.removeExtension(trkName);
                for (Stream str : master.getStreams()) {

                    if (str.getisATitle() && !str.getClass().toString().contains("Sink")) {
//                                                    System.out.println("Titles=" + str.getName());
                        streamTXT = (SourceText) str;
                        streamTXT.setContent(trkTitle);
                        streamTXT.setIsPlaying(true);

                        Stream playingStr = null;
                        for (Stream stream : master.getStreams()) {
                            if (stream.getisATrack() && stream.isPlaying()) {
                                playingStr = stream;
                                stream.setIsPlaying(false);
                            }
                        }

                        boolean wasStopped = true;
                        if (s.isPlaying()) {
                            wasStopped = false;
                        } else {
                            s.setIsPlaying(true);
                        }

                        master.updateTrack(trkName);
                        master.addTrkTransitions(trkName);

                        if (wasStopped) {
                            s.setIsPlaying(false);
                        }

                        if (playingStr != null) {
                            playingStr.setIsPlaying(true);
                        }

                        streamTXT.setIsPlaying(false);
                        isTitle = true;
                        break;
                    }
                }
                if (!isTitle) {

                    streamTXT = new SourceText(trkTitle);
                    for (String scn : MasterTracks.getInstance().getTracks()) {
                        allTracks.add(scn);
                    }
                    for (String sc : allTracks) {
                        streamTXT.addTrack(SourceTrack.getTrack(sc, streamTXT));
                    }
                    streamTXT.setisATitle(true);
                    streamTXT.setName("Titles");
                    streamTXT.setZOrder(1);
                    streamTXT.setIsPlaying(true);
                    boolean wasStopped = true;
                    if (s.isPlaying()) {
                        wasStopped = false;
                    } else {
                        s.setIsPlaying(true);
                    }
                    master.updateTrack(trkName);
                    master.addTrkTransitions(trkName);
                    if (wasStopped) {
                        s.setIsPlaying(false);
                    }
                    streamTXT.setIsPlaying(false);
                    StreamPanelText txtTitle = new StreamPanelText((Stream) streamTXT);
                    numTexts += 1;
                    textDesktop.add(streamTXT.getName(), txtTitle);
                    lblText.setForeground(busyTab);
                    Font font = new Font("Ubuntu", Font.BOLD, 11);
                    lblText.setFont(font);
                    lblText.setText("Texts(" + numTexts + ")");
                    txtTitle.setParent();
                }

            }
        }
    }//GEN-LAST:event_btnApplyTitleActionPerformed

    /**
     *
     */
    public void restartDialog() {
        Object[] options = {"OK"};
        JOptionPane.showOptionDialog(this,
                "You need to restart TrucklistStudio for the changes to take effect.", "Information",
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String args[]) throws IOException {
        if (System.getProperty("jna.nosys") == null) {
            System.setProperty("jna.nosys", "true");
        }
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true"); // Java 8 Drag'n'Drop Fix
        File dir = new File(userHomeDir, ".truckliststudio");
        if (!dir.exists()) {
            dir.mkdir();
        }
        System.out.println("Welcome to TrucklistStudio " + Version.version + " build " + new Version().getBuild() + " ...");

        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
        }

        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    new TrucklistStudio().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(TrucklistStudio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        if (args != null) {
            int c = 0;
            for (String arg : args) {
//                System.out.println("Argument: "+arg);
                if (arg.endsWith("studio")) {
                    cmdFile = new File(arg);
                }
                if (arg.equals("-o")) {
                    cmdOut = args[c + 1];
                }
                if (arg.equals("-autoplay")) {
                    cmdAutoStart = true;
                }
                if (arg.equals("-remote")) {
                    cmdRemote = true;
                }
                c++;
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton TSAbout;
    private javax.swing.JButton btnAddAnimation;
    private javax.swing.JButton btnAddFile;
    private javax.swing.JButton btnAddFolder;
    private javax.swing.JButton btnAddText;
    private javax.swing.JButton btnApplyTitle;
    private javax.swing.JButton btnImportStudio;
    private final javax.swing.JButton btnLoadStudio = new javax.swing.JButton();
    private javax.swing.JButton btnMinimizeAll;
    private javax.swing.JButton btnNewStudio;
    private javax.swing.JButton btnRemoveSource;
    private javax.swing.JButton btnSaveStudio;
    private javax.swing.JButton btnSysGC;
    public static javax.swing.JComboBox cboAnimations;
    private javax.swing.JComboBox cboAudioHz;
    private javax.swing.JComboBox cboTheme;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.JToolBar.Separator jSeparator10;
    private javax.swing.JToolBar.Separator jSeparator12;
    private javax.swing.JToolBar.Separator jSeparator16;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JLabel lblAVconv;
    private javax.swing.JLabel lblAudioFreq;
    private javax.swing.JLabel lblFFmpeg;
    private javax.swing.JLabel lblFFmpeg3;
    private javax.swing.JLabel lblGst;
    private javax.swing.JLabel lblSourceSelected;
    private javax.swing.JLabel lblSysGC;
    private javax.swing.JLabel lblThemeSwitch;
    private javax.swing.JSplitPane mainSplit;
    private javax.swing.JToolBar mainToolbar;
    private javax.swing.JSplitPane mainVerticalSplit;
    private javax.swing.JSplitPane masterPanelSplit;
    private javax.swing.JTabbedPane musicDesktop;
    private javax.swing.JScrollPane musicScroll;
    private javax.swing.JPanel panControls;
    private javax.swing.JPanel panMaster;
    private javax.swing.JPanel panSources;
    private javax.swing.JTabbedPane pictureDesktop;
    private javax.swing.JScrollPane pictureScroll;
    public static javax.swing.JTabbedPane tabControls;
    private javax.swing.JTabbedPane tabSources;
    private javax.swing.JTabbedPane textDesktop;
    private javax.swing.JScrollPane textScroll;
    private javax.swing.JToggleButton tglAVconv;
    private javax.swing.JToggleButton tglAutoAR;
    private javax.swing.JToggleButton tglAutoTitle;
    private javax.swing.JToggleButton tglAutoTrack;
    private javax.swing.JToggleButton tglFFmpeg;
    private javax.swing.JToggleButton tglGst;
    private javax.swing.JToolBar toolbar;
    private javax.swing.JTabbedPane videoDesktop;
    private javax.swing.JScrollPane videoScroll;
    // End of variables declaration//GEN-END:variables

    @Override
    public void selectedSource(Stream source) {
        String sourceName = source.getName();
        String shortName = "";
        if (sourceName.length() > 30) {
            shortName = source.getName().substring(0, 30) + " ...";
        } else {
            shortName = sourceName;
        }

        tabControls.removeAll();
        tabControls.repaint();
        ArrayList<Component> comps = SourceControls.getControls(source);
        for (Component c : comps) {
            String cName = c.getName();
            tabControls.add(cName, c);
        }
        lblSourceSelected.setText("<html>&nbsp;&nbsp;<font color=" + selColLbl + ">Selected:</font><font color=" + selColLbl2 + "> \"" + shortName + "\"</font></html>");
    }

    public void loadAtStart(final File file, final java.awt.event.ActionEvent fEvt) {
        final WaitingDialog waitingD = new WaitingDialog(this);
        setTrkStudioState(false);
        editingPhase = false;
        SwingWorker<?, ?> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws InterruptedException {
                if (file != null) {
                    lastFolder = file.getParentFile();
                    PrePlayer.getPreInstance(null).stop();
                    Tools.sleep(10);
                    MasterTracks.getInstance().endAllStream();
                    for (Stream s : MasterTracks.getInstance().getStreams()) {
                        s.updateStatus();
                    }
                    ArrayList<Stream> streamz = MasterTracks.getInstance().getStreams();
                    ArrayList<String> sourceCh = MasterTracks.getInstance().getTracks();
                    do {
                        for (int l = 0; l < streamz.size(); l++) {
                            Stream removeS = streamz.get(l);
                            Tools.sleep(20);
                            removeS.destroy();
                            removeS = null;
                        }
                        for (int a = 0; a < sourceCh.size(); a++) {
                            String removeSc = sourceCh.get(a);
                            MasterTracks.getInstance().removeTrack(removeSc);
                            Tools.sleep(20);
                            listenerTSTP.removeTracks(removeSc, a);
                        }
                    } while (streamz.size() > 0 || sourceCh.size() > 0);
                    PrePlayer.getPreInstance(null).stop();
                    Tools.sleep(10);
                    MasterTracks.getInstance().endAllStream();
                    listenerTSTP.stopChTime(fEvt);
                    listenerTSTP.resetBtnStates(fEvt);
                    listenerOP.resetBtnStates(fEvt);
                    tabControls.removeAll();
                    tabControls.repaint();
                    Tools.sleep(300);
                    videoDesktop.removeAll();
                    videoDesktop.repaint();
                    Tools.sleep(50);
                    try {
                        Studio.LText = new ArrayList<>();
                        Studio.extstream = new ArrayList<>();
                        Studio.ImgMovMus = new ArrayList<>();
                        Studio.load(file, "load");
                        Studio.main();
                        spinWidth.setValue(MasterMixer.getInstance().getWidth());
                        spinHeight.setValue(MasterMixer.getInstance().getHeight());
                        spinFPS.setValue(MasterMixer.getInstance().getRate());
                        int mW = (Integer) spinWidth.getValue();
                        int mH = (Integer) spinHeight.getValue();
                        MasterMixer.getInstance().stop();
                        MasterMixer.getInstance().setWidth(mW);
                        MasterMixer.getInstance().setHeight(mH);
                        MasterMixer.getInstance().setRate((Integer) spinFPS.getValue());
                        MasterMixer.getInstance().start();
                        PreviewMixer.getInstance().stop();
                        PreviewMixer.getInstance().setWidth(mW);
                        PreviewMixer.getInstance().setHeight(mH);
                        PreviewMixer.getInstance().start();
                    } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
                        Logger.getLogger(TrucklistStudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // loading studio streams
                    int cont = 0;
                    int barValue = 0;
                    int streamsPlusTracks = Studio.ImgMovMus.size() + Studio.LText.size() + MasterTracks.getInstance().getTracks().size();
                    for (int u = 0; u < Studio.ImgMovMus.size(); u++) {
                        Stream s = Studio.extstream.get(u);
                        if (s != null) {
                            StreamPanel frame = new StreamPanel(s);
                            String sName = s.getName();
                            String[] tokens = split(sName, 10);
                            String splitTitle = "";
                            for (String token : tokens) {
                                splitTitle = splitTitle + token + "<span></span>";
                            }
                            if (s instanceof SourceMovie) {
                                numVideos += 1;
                                videoDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                                lblVideo.setForeground(busyTab);
                                Font font = new Font("Ubuntu", Font.BOLD, 11);
                                lblVideo.setFont(font);
                                lblVideo.setText("Videos(" + numVideos + ")");
                            } else if (s instanceof SourceMusic) {
                                numMusics += 1;
                                musicDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                                lblMusic.setForeground(busyTab);
                                Font font = new Font("Ubuntu", Font.BOLD, 11);
                                lblMusic.setFont(font);
                                lblMusic.setText("Musics(" + numMusics + ")");
                            } else if (s instanceof SourceImage || s instanceof SourceImageGif) { //|| s instanceof SourceImageU 
                                numPictures += 1;
                                pictureDesktop.add("<html><body><table width='20'>" + splitTitle + "</table></body></html>", frame);
                                lblPicture.setForeground(busyTab);
                                Font font = new Font("Ubuntu", Font.BOLD, 11);
                                lblPicture.setFont(font);
                                lblPicture.setText("Pictures(" + numPictures + ")");
                            }
                            barValue = (100 * (cont + 1)) / (streamsPlusTracks);
                            jProgressBar1.setValue(barValue);
                            cont++;
                            s.setLoaded(false);
                            frame.setParent();
                        }
//                        System.out.println("Adding Source: "+s.getName());
                    }
                    Studio.extstream.clear();
                    Studio.extstream = null;
                    Studio.ImgMovMus.clear();
                    Studio.ImgMovMus = null;
                    for (SourceText text : Studio.LText) {
                        if (text != null) {
                            StreamPanelText frame = new StreamPanelText((Stream) text);
                            numTexts += 1;
                            textDesktop.add(text.getName(), frame);
                            lblText.setForeground(busyTab);
                            Font font = new Font("Ubuntu", Font.BOLD, 11);
                            lblText.setFont(font);
                            lblText.setText("Texts(" + numTexts + ")");
                            barValue = (100 * (cont + 1)) / (streamsPlusTracks);
                            jProgressBar1.setValue(barValue);
                            cont++;
                            text.setLoaded(false);
                            frame.setParent();
                        }
//                        System.out.println("Adding Source: "+text.getName());
                    }
                    Studio.LText.clear();
                    Studio.LText = null;
                    Tools.sleep(300);
                    // loading studio tracks
                    for (String chsc : MasterTracks.getInstance().getTracks()) {
                        Tools.sleep(10);
                        listenerTSTP.addLoadingTrack(chsc);
                        barValue = (100 * (cont + 1)) / (streamsPlusTracks);
                        jProgressBar1.setValue(barValue);
                        cont++;
                    }
                    Studio.trackLoad.clear();
                    listenerOP.resetSinks(fEvt);
                    ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Studio is loaded!");
                    ResourceMonitor.getInstance().addMessage(label);
                    setTitle("TrucklistStudio " + Version.version + " (" + file.getName() + ")");
                }
                setTrkStudioState(true);
                editingPhase = true;
                return null;
            }

            @Override
            protected void done() {
                waitingD.dispose();
            }
        };
        worker.execute();
        waitingD.toFront();
        waitingD.setVisible(true);
    }
}
