/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OutputPanel.java
 *
 * Created on 15-Apr-2012, 1:28:32 AM
 */
package truckliststudio.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.filechooser.FileNameExtensionFilter;
import truckliststudio.TrucklistStudio;
import static truckliststudio.TrucklistStudio.theme;
import static truckliststudio.TrucklistStudio.wsDistroWatch;
import static truckliststudio.components.TrackPanel.lblOnAir;
import truckliststudio.tracks.MasterTracks;
import truckliststudio.externals.FME;
import truckliststudio.media.renderer.Exporter;
import truckliststudio.mixers.MasterMixer;
import truckliststudio.streams.SinkAudio;
import truckliststudio.streams.SinkBroadcast;
import truckliststudio.streams.SinkFile;
import truckliststudio.streams.SinkUDP;
import truckliststudio.streams.Stream;
import truckliststudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class OutputPanel extends javax.swing.JPanel implements Stream.Listener, TrucklistStudio.Listener, TrackPanel.Listener, Exporter.Listener {

    TreeMap<String, SinkFile> files = new TreeMap<>();
    TreeMap<String, SinkBroadcast> broadcasts = new TreeMap<>();
    ArrayList<String> broadcastsOut = new ArrayList<>();
    TreeMap<String, SinkUDP> udpOut = new TreeMap<>();
    TreeMap<String, SinkAudio> audioOut = new TreeMap<>();
    TreeMap<String, FME> fmes = new TreeMap<>();
    private final static String userHomeDir = Tools.getUserHome();
    int fmeCount = 0;
    TreeMap<String, ResourceMonitorLabel> labels = new TreeMap<>();
    JPanel wDPanel;
    FME currFME;
    JPopupMenu fmePopup = new JPopupMenu();
    JPopupMenu sinkFilePopup = new JPopupMenu();
    JPopupMenu sinkUDPPopup = new JPopupMenu();
    File f;
    SinkFile fileStream;
    SinkUDP udpStream;
    SinkAudio audioStream;
    private boolean audioOutState = false;
    private boolean udpOutState = false;
    private boolean audioOutSwitch = false;
    private boolean udpOutSwitch = false;
    private boolean fmeOutState = false;
    private boolean fmeOutSwitch = false;
    
    /** Creates new form OutputPanel
     * @param aPanel */
    public OutputPanel(JPanel aPanel) {
        initComponents();
        f = new File(userHomeDir + "/.truckliststudio/Record To File");
        udpStream = new SinkUDP();
        fileStream = new SinkFile(f);
        audioStream = new SinkAudio();
//        System.out.println("SinkAudio"+audioStream);
        
        tglRecordToFile.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                JToggleButton button = ((JToggleButton) evt.getSource());
                if (!button.isSelected()) {
                    sinkFileRightMousePressed(evt);
                }
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                JToggleButton button = ((JToggleButton) evt.getSource());
                if (!button.isSelected()) {
                    sinkFileRightMousePressed(evt);
                }
            }
        });
        
        tglUDP.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                JToggleButton button = ((JToggleButton) evt.getSource());
                if (!button.isSelected()) {
                    sinkUDPRightMousePressed(evt);
                }
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                JToggleButton button = ((JToggleButton) evt.getSource());
                if (!button.isSelected()) {
                    sinkUDPRightMousePressed(evt);
                }
            }
        });
        
        wDPanel = aPanel;
        fmeInitPopUp();
        sinkFileInitPopUp();
        sinkUDPInitPopUp();
        final OutputPanel instanceSinkOP = this;
        TrucklistStudio.setListenerOP(instanceSinkOP);
        TrackPanel.setListenerCPOPanel(instanceSinkOP);
        Exporter.setListenerEx(instanceSinkOP);
        
        fileStream.setWidth(MasterMixer.getInstance().getWidth());
        fileStream.setHeight(MasterMixer.getInstance().getHeight());
        fileStream.setRate(MasterMixer.getInstance().getRate());
        
        udpStream.setWidth(MasterMixer.getInstance().getWidth());
        udpStream.setHeight(MasterMixer.getInstance().getHeight());
        udpStream.setRate(MasterMixer.getInstance().getRate());
        
        if (wsDistroWatch().toLowerCase().equals("windows")) {
            tglAudioOut.setEnabled(false);
        }
        this.setDropTarget(new DropTarget() {

            @Override
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
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
                                System.out.println("Supported: " + d.getDefaultRepresentationClassAsString());
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
                            fileName = file.getName();
                            if (file.exists() && file.getName().toLowerCase().endsWith("xml")) {
                                success = true;
                                FME fme = new FME(file);
                                fmes.put(fme.getName(), fme);
                                addButtonBroadcast(fme);
                            }
                        }
                    }
                    evt.dropComplete(success);
                    if (!success) {
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 5000, "Unsupported file: " + fileName);
                        ResourceMonitor.getInstance().addMessage(label);
                    }
                } catch (UnsupportedFlavorException | IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
            
        });
    }

    public void loadPrefs(Preferences prefs) {
        Preferences fmePrefs = prefs.node("fme");
        Preferences filePrefs = prefs.node("filerec");
        Preferences udpPrefs = prefs.node("udp");
        try {
            String[] services = fmePrefs.childrenNames();          
            String[] servicesF = filePrefs.childrenNames();           
            String[] servicesU = udpPrefs.childrenNames();
                      
            for (String s : servicesF){
                Preferences serviceF = filePrefs.node(s);
                fileStream.setVbitrate(serviceF.get("vbitrate", "1200"));
                fileStream.setAbitrate(serviceF.get("abitrate", "128"));
            }
            
            for (String s : servicesU){
                Preferences serviceU = udpPrefs.node(s);
                udpStream.setVbitrate(serviceU.get("vbitrate", "1200"));
                udpStream.setAbitrate(serviceU.get("abitrate", "128"));
                udpStream.setStandard(serviceU.get("standard", "STD"));
            }
            
            for (String s : services) {
                Preferences service = fmePrefs.node(s);
                String url = service.get("url", "");
                String name = service.get("name", "");
                String abitrate = service.get("abitrate", "512000");
                String vbitrate = service.get("vbitrate", "96000");
                String vcodec = service.get("vcodec", "");
                String acodec = service.get("acodec", "");
                String width = service.get("width", "");
                String height = service.get("height", "");
                String stream = service.get("stream", "");
                String mount = service.get("mount", "");
                String password = service.get("password", "");
                String port = service.get("port", "");
                String keyInt = service.get("keyint", "");
                String standard = service.get("standard", "STD");
                // for compatibility before KeyInt
                if ("".equals(keyInt)) {
                    keyInt = "125";
                }
                
//                System.out.println("Loaded KeyInt: "+keyInt+"###");
                FME fme = new FME(url, stream, name, abitrate, vbitrate, vcodec, acodec, width, height, mount, password, port, keyInt);
                fme.setStandard(standard);
                fmes.put(fme.getName(), fme);
                addButtonBroadcast(fme);
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void savePrefs(Preferences prefs) {
        Preferences fmePrefs = prefs.node("fme");
        Preferences filePrefs = prefs.node("filerec");
        Preferences udpPrefs = prefs.node("udp");
        try {
            fmePrefs.removeNode();
            fmePrefs.flush();
            fmePrefs = prefs.node("fme");
            filePrefs.removeNode();
            filePrefs.flush();
            filePrefs = prefs.node("filerec");
            udpPrefs.removeNode();
            udpPrefs.flush();
            udpPrefs = prefs.node("udp");
        } catch (BackingStoreException ex) {
            Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (FME fme : fmes.values()) {
            Preferences service = fmePrefs.node(fme.getName());
            service.put("url", fme.getUrl());
            service.put("name", fme.getName());
            service.put("abitrate", fme.getAbitrate());
            service.put("vbitrate", fme.getVbitrate());
            service.put("vcodec", fme.getVcodec());
            service.put("acodec", fme.getAcodec());
            service.put("width", fme.getWidth());
            service.put("height", fme.getHeight());
            service.put("stream", fme.getStream());
            service.put("mount", fme.getMount());
            service.put("password", fme.getPassword());
            service.put("port", fme.getPort());
            service.put("keyint", fme.getKeyInt());
            service.put("standard", fme.getStandard());
        }
        Preferences serviceF = filePrefs.node("frecordset");
        serviceF.put("abitrate", fileStream.getAbitrate());
        serviceF.put("vbitrate", fileStream.getVbitrate());
        Preferences serviceU = udpPrefs.node("uoutset");
        serviceU.put("abitrate", udpStream.getAbitrate());
        serviceU.put("vbitrate", udpStream.getVbitrate());
        serviceU.put("standard", udpStream.getStandard());
    }
    
    private String checkDoubleBroad(String s) {
        String res = s;
        for (String broName : broadcastsOut) {
            if (s.equals(broName)){
                res = "";
            }
        }
        return res;
    }
    
    public void addButtonBroadcast(final FME fme) {
        final OutputPanel instanceSinkFME = this;
        JToggleButton button = new JToggleButton();
        Dimension d = new Dimension(139,22);
        button.setPreferredSize(d);
        button.setText(fme.getName());
        button.setActionCommand(fme.getUrl()+"/"+fme.getStream());
        button.setIcon(tglRecordToFile.getIcon());
        button.setSelectedIcon(tglRecordToFile.getSelectedIcon());
        button.setRolloverEnabled(false);
        button.setToolTipText("Drag to the right to Remove... - Right Click for Settings");
        button.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                JToggleButton button = ((JToggleButton) evt.getSource());
                FME fme = fmes.get(button.getText());
                if (button.isSelected()) {
                    if (fme != null){
                        fmeOutState = true;
                        String cleanBroad = checkDoubleBroad(button.getText());
                        if (!"".equals(cleanBroad)) {
                            broadcastsOut.add(cleanBroad);
//                            System.out.println("broadcastsOut: "+broadcastsOut);
                        }
                        fmeCount ++;
                        SinkBroadcast broadcast = new SinkBroadcast(fme);
                        broadcast.setStandard(fme.getStandard()); 
                        broadcast.setRate(MasterMixer.getInstance().getRate());
                        broadcast.setWidth(MasterMixer.getInstance().getWidth());
                        fme.setWidth(Integer.toString(MasterMixer.getInstance().getWidth()));
                        broadcast.setHeight(MasterMixer.getInstance().getHeight());
                        fme.setHeight(Integer.toString(MasterMixer.getInstance().getHeight()));
                        broadcast.setListener(instanceSinkFME);
                        broadcast.read();
                        broadcasts.put(button.getText(), broadcast);
                        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Broadcasting to " + fme.getName());
                        labels.put(fme.getName(), label);
                        ResourceMonitor.getInstance().addMessage(label);
                        lblOnAir.setForeground(Color.RED);
                    } else {
                        fmeCount --;
                        button.setSelected(false);  
                    }
//                    System.out.println("StartFMECount = "+fmeCount);
                } else {
                    fmeOutState = fmeCount > 0;
                    broadcastsOut.remove(button.getText());
                    SinkBroadcast broadcast = broadcasts.get(button.getText());
                    if (broadcast != null) {
                        fmeCount --;
                        broadcast.stop();
                        broadcast.destroy();
                        broadcasts.remove(fme.getName());
                        ResourceMonitorLabel label = labels.get(fme.getName());
                        labels.remove(fme.getName());
//                        System.out.println("StopFMECount = "+fmeCount);
                        ResourceMonitor.getInstance().removeMessage(label);
                        if (fmeCount == 0 && !udpOutState) {
                            if (theme.equals("Dark")) {
                                lblOnAir.setForeground(Color.WHITE);
                            } else {
                                lblOnAir.setForeground(Color.BLACK);
                            }
                        }
                    }
                }
            }
        });
        button.addMouseMotionListener(new java.awt.event.MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (e.getX() > getWidth()) {
                    JToggleButton button = ((JToggleButton) e.getSource());
                    if (!button.isSelected() && e.getX() > getWidth()) {
                        System.out.println(button.getText()+" removed ...");
                        SinkBroadcast broadcast = broadcasts.remove(button.getText());
                        if (broadcast != null) {
                            MasterTracks.getInstance().unregister(broadcast);
                        }
                        fmes.remove(button.getText());
                        labels.remove(fme.getName());
                        remove(button);
                        repaint();
                        revalidate();
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                JToggleButton button = ((JToggleButton) evt.getSource());
                if (!button.isSelected()) {
                    fmeRightMousePressed(evt, fme);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent evt) {
                JToggleButton button = ((JToggleButton) evt.getSource());
                if (!button.isSelected()) {
                    fmeRightMousePressed(evt, fme);
                }
            }
        });
        this.add(button);
        this.revalidate();
    }
    
    private void fmeRightMousePressed(java.awt.event.MouseEvent evt, FME fme) {                                      
        if (evt.isPopupTrigger()) {
            fmePopup.show(evt.getComponent(), evt.getX(), evt.getY());
            currFME = fme;
        }
    }
    
    private void sinkFileRightMousePressed(java.awt.event.MouseEvent evt) { // , SinkFile sinkfile
        if (evt.isPopupTrigger()) {
            sinkFilePopup.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    private void sinkUDPRightMousePressed(java.awt.event.MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            sinkUDPPopup.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    private void fmeInitPopUp(){
        JMenuItem fmeSettings = new JMenuItem (new AbstractAction("FME Settings") {
            @Override
            public void actionPerformed(ActionEvent e) {
                FMESettings fmeSet = new FMESettings(currFME);
                fmeSet.setLocationRelativeTo(TrucklistStudio.cboAnimations);
                fmeSet.setAlwaysOnTop(true);
                fmeSet.setVisible(true);
            }
        });
        fmeSettings.setIcon(new ImageIcon(getClass().getResource("/truckliststudio/resources/tango/working-6.png"))); // NOI18N
        fmePopup.add(fmeSettings);
    }
    
    private void sinkFileInitPopUp(){
        JMenuItem sinkSettings = new JMenuItem (new AbstractAction("Record Settings") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SinkSettings sinkSet = new SinkSettings(fileStream, null);
                sinkSet.setLocationRelativeTo(TrucklistStudio.cboAnimations);
                sinkSet.setAlwaysOnTop(true);
                sinkSet.setVisible(true);
            }
        });
        sinkSettings.setIcon(new ImageIcon(getClass().getResource("/truckliststudio/resources/tango/working-6.png"))); // NOI18N
        sinkFilePopup.add(sinkSettings);
    }
    
    private void sinkUDPInitPopUp(){
        JMenuItem sinkSettings = new JMenuItem (new AbstractAction("UDP Settings") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SinkSettings sinkSet = new SinkSettings(null, udpStream);
                sinkSet.setLocationRelativeTo(TrucklistStudio.cboAnimations);
                sinkSet.setAlwaysOnTop(true);
                sinkSet.setVisible(true);
            }
        });
        sinkSettings.setIcon(new ImageIcon(getClass().getResource("/truckliststudio/resources/tango/working-6.png"))); // NOI18N
        sinkUDPPopup.add(sinkSettings);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        tglAudioOut = new javax.swing.JToggleButton();
        tglRecordToFile = new javax.swing.JToggleButton();
        tglUDP = new javax.swing.JToggleButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 3), new java.awt.Dimension(0, 3), new java.awt.Dimension(32767, 3));
        sepFME = new javax.swing.JSeparator();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 3), new java.awt.Dimension(0, 3), new java.awt.Dimension(32767, 3));
        jLabel2 = new javax.swing.JLabel();
        btnAddFME = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("truckliststudio/Languages"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("OUTPUT"))); // NOI18N
        setToolTipText(bundle.getString("DROP_OUTPUT")); // NOI18N
        setMinimumSize(new java.awt.Dimension(150, 70));
        setPreferredSize(new java.awt.Dimension(200, 90));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Defaults:");
        jLabel1.setName("jLabel1"); // NOI18N
        add(jLabel1);

        tglAudioOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/audio-card.png"))); // NOI18N
        tglAudioOut.setText("Audio Output");
        tglAudioOut.setToolTipText("TrucklistStudio Master Audio Output");
        tglAudioOut.setMinimumSize(new java.awt.Dimension(135, 21));
        tglAudioOut.setName("tglAudioOut"); // NOI18N
        tglAudioOut.setPreferredSize(new java.awt.Dimension(32, 22));
        tglAudioOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglAudioOutActionPerformed(evt);
            }
        });
        add(tglAudioOut);

        tglRecordToFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-record.png"))); // NOI18N
        tglRecordToFile.setText(bundle.getString("RECORD")); // NOI18N
        tglRecordToFile.setToolTipText("Save to FIle - Right Click for Settings");
        tglRecordToFile.setMinimumSize(new java.awt.Dimension(87, 21));
        tglRecordToFile.setName("tglRecordToFile"); // NOI18N
        tglRecordToFile.setPreferredSize(new java.awt.Dimension(87, 22));
        tglRecordToFile.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglRecordToFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglRecordToFileActionPerformed(evt);
            }
        });
        add(tglRecordToFile);

        tglUDP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-record.png"))); // NOI18N
        tglUDP.setText(bundle.getString("UDP_MPEG_OUT")); // NOI18N
        tglUDP.setToolTipText("(Min 25fps) Stream to udp://@127.0.0.1:7000 - Right Click for Settings");
        tglUDP.setMinimumSize(new java.awt.Dimension(237, 21));
        tglUDP.setName("tglUDP"); // NOI18N
        tglUDP.setPreferredSize(new java.awt.Dimension(237, 22));
        tglUDP.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglUDP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglUDPActionPerformed(evt);
            }
        });
        add(tglUDP);

        filler1.setName("filler1"); // NOI18N
        add(filler1);

        sepFME.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        sepFME.setMaximumSize(new java.awt.Dimension(32767, 3));
        sepFME.setName("sepFME"); // NOI18N
        sepFME.setPreferredSize(new java.awt.Dimension(50, 5));
        add(sepFME);

        filler2.setName("filler2"); // NOI18N
        add(filler2);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("FMEs:");
        jLabel2.setName("jLabel2"); // NOI18N
        add(jLabel2);

        btnAddFME.setFont(new java.awt.Font("Noto Sans", 3, 12)); // NOI18N
        btnAddFME.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/list-add.png"))); // NOI18N
        btnAddFME.setText("Add FME");
        btnAddFME.setName("btnAddFME"); // NOI18N
        btnAddFME.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFMEActionPerformed(evt);
            }
        });
        add(btnAddFME);
    }// </editor-fold>//GEN-END:initComponents

    private void tglRecordToFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglRecordToFileActionPerformed
        if (tglRecordToFile.isSelected()) {
            boolean overWrite = true;
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter aviFilter = new FileNameExtensionFilter("AVI files (*.avi)", "avi");
            FileNameExtensionFilter mp4Filter = new FileNameExtensionFilter("MP4 files (*.mp4)", "mp4");
            FileNameExtensionFilter flvFilter = new FileNameExtensionFilter("FLV files (*.flv)", "flv");
            
            chooser.setFileFilter(aviFilter);
            chooser.setFileFilter(mp4Filter);
            chooser.setFileFilter(flvFilter);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("Choose Destination File ...");
            int retval = chooser.showSaveDialog(this);
            f = chooser.getSelectedFile();
            if (retval == JFileChooser.APPROVE_OPTION && f != null) {
                if (chooser.getFileFilter().equals(aviFilter)) {
                    if(!chooser.getSelectedFile().getAbsolutePath().endsWith(".avi")){
                        f =  new File(chooser.getSelectedFile() + ".avi");
                    }
                } else if (chooser.getFileFilter().equals(mp4Filter) && !chooser.getSelectedFile().getAbsolutePath().endsWith(".mp4")) {
                    f =  new File(chooser.getSelectedFile() + ".mp4");
                } else if (chooser.getFileFilter().equals(flvFilter) && !chooser.getSelectedFile().getAbsolutePath().endsWith(".flv")) {
                    f =  new File(chooser.getSelectedFile() + ".flv");
                }
                if(f.exists()){
                    int result = JOptionPane.showConfirmDialog(this,"File exists, overwrite?","Attention",JOptionPane.YES_NO_CANCEL_OPTION);
                    switch(result){
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
            }
            if (retval == JFileChooser.APPROVE_OPTION && overWrite) {
                fileStream.setFile(f);
                fileStream.setListener(instanceSink);
                // Fix lost prefs
                if ("".equals(fileStream.getVbitrate())) {
                    fileStream.setVbitrate("1200");
                }
                if ("".equals(fileStream.getAbitrate())) {
                    fileStream.setAbitrate("128");
                }
                
                fileStream.read();
//                System.out.println("VBitRate: "+fileStream.getVbitrate());
                files.put("RECORD", fileStream);
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Recording to " + f.getName());
                labels.put("RECORD", label);
                ResourceMonitor.getInstance().addMessage(label);
            } else {
                tglRecordToFile.setSelected(false);
                ResourceMonitorLabel label3 = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Record Cancelled!");
                ResourceMonitor.getInstance().addMessage(label3);
            }
        } else {
            SinkFile fileStream = files.get("RECORD");
            if (fileStream != null) {
                fileStream.stop();
                fileStream = null;
                files.remove("RECORD");
                ResourceMonitorLabel label = labels.get("RECORD");
                ResourceMonitor.getInstance().removeMessage(label);

                ResourceMonitorLabel label2 = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "File is saved!");
                ResourceMonitor.getInstance().addMessage(label2);
            }
        }
        
    }//GEN-LAST:event_tglRecordToFileActionPerformed

    private void tglUDPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglUDPActionPerformed
        if (tglUDP.isSelected()) {
            udpOutState = true;
            udpStream.setListener(instanceSink);
            // Fix lost prefs
            if ("".equals(udpStream.getVbitrate())) {
                udpStream.setVbitrate("1200");
            }
            if ("".equals(udpStream.getAbitrate())) {
                udpStream.setAbitrate("128");
            }
            if ("".equals(udpStream.getStandard())) {
                udpStream.setStandard("STD");
            }
            
            udpStream.read();
            udpOut.put("UDPOut", udpStream);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Unicast mpeg2 to udp://127.0.0.1:7000");
            labels.put("UDPOut", label);
            ResourceMonitor.getInstance().addMessage(label);
            lblOnAir.setForeground(Color.RED);
        } else {
            udpOutState = false;
            SinkUDP udpStream = udpOut.get("UDPOut");
            if (udpStream != null) {
                udpStream.stop();
                udpStream = null;
                udpOut.remove("UDPOut");
                ResourceMonitorLabel label = labels.get("UDPOut");
                ResourceMonitor.getInstance().removeMessage(label);
            }
            if (fmeCount == 0) {
                if (theme.equals("Dark")) {
                    lblOnAir.setForeground(Color.WHITE);
                } else {
                    lblOnAir.setForeground(Color.BLACK);
                }
            }
        }
        
    }//GEN-LAST:event_tglUDPActionPerformed

    @Override
    public void resetFMECount() {
        fmeCount=0;
    }

    @Override
    public void resetSinks(ActionEvent evt) {
        fileStream.destroy();
        udpStream.destroy();
        audioStream.destroy();
        f = new File(userHomeDir + "/.truckliststudio/Record To File");
        fileStream = new SinkFile(f);
        udpStream = new SinkUDP();
        audioStream = new SinkAudio();
        Preferences filePrefs = TrucklistStudio.prefs.node("filerec");
        Preferences udpPrefs = TrucklistStudio.prefs.node("udp");
        try {
            String[] servicesF = filePrefs.childrenNames();           
            String[] servicesU = udpPrefs.childrenNames();
                      
            for (String s : servicesF){
                Preferences serviceF = filePrefs.node(s);
                fileStream.setVbitrate(serviceF.get("vbitrate", ""));
                fileStream.setAbitrate(serviceF.get("abitrate", ""));
            }
            
            for (String s : servicesU){
                Preferences serviceU = udpPrefs.node(s);
                udpStream.setVbitrate(serviceU.get("vbitrate", ""));
                udpStream.setAbitrate(serviceU.get("abitrate", ""));
                udpStream.setStandard(serviceU.get("standard", ""));
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void requestReset() {
        audioOutSwitch = audioOutState;
        udpOutSwitch = udpOutState;
        if (fmeOutState) {
            fmeOutSwitch = true;
            fmeCount = 0;
        } else {
            fmeOutSwitch = false;
        }
    }

    @Override
    public void requestStart() {
        String[] currentBroadcasts = new String[broadcastsOut.size()];
        currentBroadcasts = broadcastsOut.toArray(currentBroadcasts);
        if (audioOutSwitch){
            tglAudioOut.doClick();
        }
        if (udpOutSwitch){
            tglUDP.doClick();
        }
        if (fmeOutSwitch){
            for (String bro : currentBroadcasts) {
                for (Component c : this.getComponents()) {
                    if (c instanceof JToggleButton) {
                        JToggleButton b = (JToggleButton) c;
                        if (b.getText().equals(bro)) {
                            b.doClick();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void requestStop() {
        audioOutSwitch = false;
        udpOutSwitch = false;
        fmeOutSwitch = false;
    }

    @Override
    public void resetAutoPLBtnState(ActionEvent evt) {
        // Nothing Here
    }

    @Override
    public void resetBtnStates(ActionEvent evt) {
        fmeCount = 0;
        broadcastsOut.clear();
    }

    @Override
    public void setRemoteOn() {
        // Nothing Here
    }

    private void tglAudioOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglAudioOutActionPerformed
        
        if (tglAudioOut.isSelected()) {
            audioOutState = true;
            audioStream.setListener(instanceSink);
            audioStream.read();
            audioOut.put("AudioOut", audioStream);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Master Audio to Speakers");
            labels.put("AudioOut", label);
            ResourceMonitor.getInstance().addMessage(label);
        } else {
            audioOutState = false;
            SinkAudio audioStream = audioOut.get("AudioOut");
            if (audioStream != null) {
                audioStream.stop();
                audioStream = null;
                audioOut.remove("AudioOut");
                ResourceMonitorLabel label = labels.get("AudioOut");
                ResourceMonitor.getInstance().removeMessage(label);
            }
        }
    }//GEN-LAST:event_tglAudioOutActionPerformed

    private void btnAddFMEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFMEActionPerformed
        final FME fme = new FME();
        final FMEDialog fmeDiag = new FMEDialog(fme);
        fmeDiag.setLocationRelativeTo(TrucklistStudio.cboAnimations);
        fmeDiag.setAlwaysOnTop(true);
        fmeDiag.setVisible(true);
        Thread addFME = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (fmeDiag.isVisible()) {
                        Tools.sleep(100);
                    }
                    if (FMEDialog.add.equals("ok")) {
                        fmes.put(fme.getName(), fme);
                        addButtonBroadcast(fme);
                    }
                } 
            });
            addFME.setPriority(Thread.MIN_PRIORITY);
            addFME.start();
    }//GEN-LAST:event_btnAddFMEActionPerformed
    
    public static void execPACTL(String command) throws IOException, InterruptedException {
//        System.out.println(command);
        Process p = Runtime.getRuntime().exec(command);
        try (InputStream in = p.getInputStream(); InputStreamReader isr = new InputStreamReader(in)) {
            BufferedReader reader = new BufferedReader(isr);
            reader.readLine();
            reader.close();
        }
        p.waitFor();
        p.destroy();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddFME;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator sepFME;
    private javax.swing.JToggleButton tglAudioOut;
    private javax.swing.JToggleButton tglRecordToFile;
    final OutputPanel instanceSink = this;
    private javax.swing.JToggleButton tglUDP;
    // End of variables declaration//GEN-END:variables

    
    @Override
    public void sourceUpdated(Stream stream) {
        if (stream instanceof SinkFile) {
            tglRecordToFile.setSelected(stream.isPlaying());
        } else if (stream instanceof SinkUDP) {
            tglUDP.setSelected(stream.isPlaying());
        } else if (stream instanceof SinkAudio) {
            tglAudioOut.setSelected(stream.isPlaying());
        } else if (stream instanceof SinkBroadcast) {
            String name = stream.getName();
            for (Component c : this.getComponents()) {
                if (c instanceof JToggleButton) {
                    JToggleButton b = (JToggleButton) c;
                    if (b.getText().equals(name)) {
                        b.setSelected(stream.isPlaying());
                    }
                }
            }
            if (!stream.isPlaying()){
                fmeCount --;
            }
            if (fmeCount == 0 && !udpOutState) {
                if (theme.equals("Dark")) {
                    lblOnAir.setForeground(Color.WHITE);
                } else {
                    lblOnAir.setForeground(Color.BLACK);
                }
            }
        } 
    }

    @Override
    public void updatePreview(BufferedImage image) {
        // nothing here.   
    }

    @Override
    public void stopChTime(ActionEvent evt) {
        // nothing here.
    }

    @Override
    public void resetButtonsStates(ActionEvent evt) {
        fmeCount = 0;
    }

    @Override
    public void addLoadingTrack(String name) { // used addLoadingTrack to activate Output from command line.
        for (Component c : this.getComponents()) {
            // At this moment this workaround. After will make the proper fix.
            if (c instanceof JCheckBox) {
                // Nothing here.
            } else if (c instanceof JToggleButton) {
                JToggleButton b = (JToggleButton) c;
                if (b.getText().contains(name)) {
                    b.doClick();
                }
            }
        }
    }

    @Override
    public void removeTracks(String removeSc, int a) {
        
    }

    /**
     *
     * @param name
     */
    @Override
    public void closeItsTrack(String name) {
        // nothing here
    }
}
