/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * TrackPanel.java
 *
 * Created on 23-Apr-2012, 12:17:31 AM
 */
package truckliststudio.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import truckliststudio.TrucklistStudio;
import static truckliststudio.TrucklistStudio.selColLbl2;
import static truckliststudio.TrucklistStudio.setListenerTSTP;
import static truckliststudio.TrucklistStudio.theme;
import static truckliststudio.components.StreamPanel.setListenerTP;
import truckliststudio.tracks.MasterTracks;
import truckliststudio.mixers.PrePlayer;
import truckliststudio.remote.Listener;
import truckliststudio.remote.WebRemote;
import truckliststudio.streams.SourceTrack;
import truckliststudio.streams.Stream;
import truckliststudio.studio.Studio;
import truckliststudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class TrackPanel extends javax.swing.JPanel implements TrucklistStudio.Listener, Studio.Listener, Listener, StreamPanel.Listener {

    public static MasterTracks master = MasterTracks.getInstance();
    private static final DefaultListModel model = new DefaultListModel();
    private static final ArrayList<Integer> CHTimers = new ArrayList<>();
    private static final ArrayList<String> arrayListTracks = new ArrayList<>();
    private final WebRemote remote;
    ArrayList<Stream> streamS = MasterTracks.getInstance().getStreams();
    String selectTrack = null;
    int trkOn = 0;
    String trkNxName = null;
    int trkNextTime = 0;
    public static int timeToTimer = 0;
    public static int totalToTimer = 0;
    static int CHTimer = 0;
    private Timer trkT = new Timer();
    String CHptS = null;
    private Boolean stopTrkPt = false;
    private static boolean inTimer = false;
    JPopupMenu remotePopup = new JPopupMenu();
    private static String remUser = "truckliststudio";
    private static String remPsw = "truckliststudio";
    private static int remPort = 8000;
    Preferences preferences = Preferences.userNodeForPackage(this.getClass());
    int playingIndex = 0;

    @Override
    public void resetAutoPLBtnState(ActionEvent evt) {
        // nothing here - removed AutoPlayList Button
    }

    @Override
    public void requestStart() {
        tglStartTrack.doClick();
        listenerCPOP.requestStart();
    }

    @Override
    public void requestStop() {
        btnStopOnlyStream.doClick();
        listenerCPOP.requestStop();
    }

    @Override
    public void listening(String localURL) {

    }

    @Override
    public void requestReset() {
        listenerCPMP.requestReset();
        listenerCPOP.requestReset();
    }

    @Override
    public void resetSinks(ActionEvent evt) { // used resetSinks to AutoPlay from command line.
        tglStartTrack.doClick();
    }

    @Override
    public String requestlogin(String login) {
        String res = "";
        String[] loginSplit = login.split("\\?");
        String userPsw = loginSplit[1].replace("j_username=", "");
        userPsw = userPsw.replace("j_password=", "");
        userPsw = userPsw.replace(" HTTP/1.1", "");
//        System.out.println("userPsw: "+userPsw);
        if (!userPsw.equals("&")) {
            String[] userPswSplit = userPsw.split("&");
            if (!userPsw.equals("&")) {
                if (userPswSplit[0].equals(remUser) && userPswSplit[1].equals(remPsw)) {
                    boolean play = false;
                    for (Stream stream : streamS) {
                        if (!stream.getClass().toString().contains("Sink")) {
                            if (stream.isPlaying()) {
                                play = true;
                            }
                        }
                    }
                    if (play) {
                        res = "/run";
                    } else {
                        res = "/stop";
                    }
                } else {
                    res = "/error";
                }
            }
        } else {
            res = "/login";
        }
        return res;
    }

    @Override
    public void setRemoteOn() {
        tglRemote.doClick();
    }

    @Override
    public void closeItsTrack(String name) {
        ArrayList<Stream> allStreams = MasterTracks.getInstance().getStreams();
        for (Stream s : allStreams) {
            if (s.getisATrack()) {
                if (s.getTrkName().equals(name)) {
                    s.setisATrack(false);
//                    System.out.println("StreamName="+s.getName());
//                    System.out.println("IsaTrack="+s.getisATrack());
                }
            }
        }
        boolean isATrack = false;
        int SelectCHIndex = 0;
        for (String currClosing : arrayListTracks) {
            if (currClosing.equals(name)) {
                isATrack = true;
                break;
            }
            SelectCHIndex++;
        }
//        System.out.println(SelectCHIndex);
        if (isATrack) {
            if (SelectCHIndex == 0 && model.size() > 1) {
                master.removeTrack(name);
                model.removeElement(name);
                CHTimers.remove(SelectCHIndex);
                arrayListTracks.remove(name);
                listTracks.revalidate();
            } else {
                master.removeTrack(name);
                model.removeElement(name);
                CHTimers.remove(SelectCHIndex);
                arrayListTracks.remove(name);
                listTracks.revalidate();
            }
        }
//        updateTrackOn();
//        System.out.println("StreamDurationArray="+CHTimers.toString());

    }

    @Override
    public void selectedSource(Stream source) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closeSource(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public interface Listener {

        public void resetButtonsStates(ActionEvent evt);

        public void requestReset();

        public void requestStop();

        public void requestStart();
    }

    static Listener listenerCPOP = null;

    public static void setListenerCPOPanel(Listener l) {
        listenerCPOP = l;
    }

    static Listener listenerCPMP = null;

    public static void setListenerCPMPanel(Listener l) {
        listenerCPMP = l;
    }

    /**
     * Creates new form TrackPanel
     */
    @SuppressWarnings("unchecked")
    public TrackPanel() {
        initComponents();
//        btnRename.setVisible(false);
        remoteInitPopUp();
        final TrackPanel instanceChPnl = this;
        lblPlayingTrack.setVisible(false);
        listTracks.setModel(model);
        listTracks.setCellRenderer(new TrackListCellRender());
        setListenerTSTP(instanceChPnl);
        setListenerTP(instanceChPnl);
        Studio.setListener(this);
        remote = new WebRemote(this);
        if (theme.equals("Dark")) {
            lblPlayingTrack.setForeground(Color.YELLOW);
        }
        btnAdd.setVisible(false);
        listTracks.revalidate();

        loadPrefs();
        ((JSpinner.DefaultEditor) trkDuration.getEditor()).getTextField().addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
//                System.out.println("PRESSED!");
                String inputN = ((JSpinner.DefaultEditor) trkDuration.getEditor()).getTextField().getText();
                if (inputN != null) {
                    try {
                        CHTimer = Integer.parseInt(inputN) * 1000;
                    } catch (NumberFormatException ex) {
//                        System.out.println("Characters Are Not Allowed !!!");
                    }
                    if (listTracks.getSelectedIndex() != -1) {
                        int ChIndex = listTracks.getSelectedIndex();
                        CHTimers.set(ChIndex, CHTimer);
                    }
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

        });
    }

    private void loadPrefs() {
        remUser = preferences.get("remoteuser", "truckliststudio");
        remPsw = preferences.get("remotepsw", "truckliststudio");
        remPort = preferences.getInt("remoteport", 8000);
        remote.setPort(remPort);
    }

    public void savePrefs() {
        preferences.put("remoteuser", remUser);
        preferences.put("remotepsw", remPsw);
        preferences.putInt("remoteport", remPort);
    }

    public static void setRemPsw(String psw) {
        remPsw = psw;
    }

    public static String getRemPsw() {
        return remPsw;
    }

    public static void setRemUsr(String usr) {
        remUser = usr;
    }

    public static String getRemUsr() {
        return remUser;
    }

    public static void setRemPort(int port) {
        remPort = port;
    }

    public static int getRemPort() {
        return remPort;
    }

    public static void setInTimer(boolean inT) {
        inTimer = inT;
    }

    public static boolean getInTimer() {
        return inTimer;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lstTracksScroll = new javax.swing.JScrollPane();
        listTracks = new javax.swing.JList();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        trkDuration = new javax.swing.JSpinner();
        lblTrackDuration = new javax.swing.JLabel();
        lblPlayingTrack = new javax.swing.JLabel();
        trkProgressTime = new javax.swing.JProgressBar();
        btnStopAllStream = new javax.swing.JButton();
        btnUp = new javax.swing.JButton();
        btnDown = new javax.swing.JButton();
        btnClearAllTrk = new javax.swing.JButton();
        tglRemote = new javax.swing.JToggleButton();
        btnStopOnlyStream = new javax.swing.JButton();
        PanelResource = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        tglStartTrack = new javax.swing.JToggleButton();
        lblOnAir = new javax.swing.JLabel();
        btnSkipTrack = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        spinJumpPos = new javax.swing.JSpinner();
        btnJump = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        btnDuplicateTrk = new javax.swing.JButton();
        btnAddGAP = new javax.swing.JButton();
        spinGAP = new javax.swing.JSpinner();

        setFont(new java.awt.Font("Noto Sans", 1, 18)); // NOI18N

        lstTracksScroll.setMinimumSize(new java.awt.Dimension(502, 22));
        lstTracksScroll.setName("lstTracksScroll"); // NOI18N

        listTracks.setFont(new java.awt.Font("Noto Sans", 0, 18)); // NOI18N
        listTracks.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        listTracks.setToolTipText("Double Click to play selected track");
        listTracks.setMinimumSize(new java.awt.Dimension(500, 180));
        listTracks.setName("listTracks"); // NOI18N
        listTracks.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                listTracksComponentAdded(evt);
            }
        });
        listTracks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listTracksMouseClicked(evt);
            }
        });
        listTracks.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listTracksValueChanged(evt);
            }
        });
        lstTracksScroll.setViewportView(listTracks);

        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/list-add.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("truckliststudio/Languages"); // NOI18N
        btnAdd.setToolTipText(bundle.getString("ADD_CHANNEL")); // NOI18N
        btnAdd.setEnabled(false);
        btnAdd.setName("btnAdd"); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/process-stop.png"))); // NOI18N
        btnRemove.setText("Delete");
        btnRemove.setToolTipText("Remove selected Track");
        btnRemove.setName("btnRemove"); // NOI18N
        btnRemove.setPreferredSize(new java.awt.Dimension(32, 30));
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        btnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/view-refresh.png"))); // NOI18N
        btnUpdate.setText("Update");
        btnUpdate.setToolTipText("Update Selected Track Layout");
        btnUpdate.setName("btnUpdate"); // NOI18N
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        trkDuration.setFont(new java.awt.Font("Ubuntu", 3, 18)); // NOI18N
        trkDuration.setModel(new javax.swing.SpinnerNumberModel());
        trkDuration.setToolTipText("0 = Infinite");
        trkDuration.setName("trkDuration"); // NOI18N
        trkDuration.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                trkDurationStateChanged(evt);
            }
        });

        lblTrackDuration.setFont(new java.awt.Font("Noto Sans", 0, 18)); // NOI18N
        lblTrackDuration.setText(bundle.getString("DURATION")); // NOI18N
        lblTrackDuration.setName("lblTrackDuration"); // NOI18N
        lblTrackDuration.setPreferredSize(null);

        lblPlayingTrack.setFont(new java.awt.Font("Ubuntu", 0, 14)); // NOI18N
        lblPlayingTrack.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPlayingTrack.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblPlayingTrack.setName("lblPlayingTrack"); // NOI18N

        trkProgressTime.setFont(new java.awt.Font("Noto Sans", 1, 24)); // NOI18N
        trkProgressTime.setMaximumSize(new java.awt.Dimension(32767, 100));
        trkProgressTime.setMinimumSize(new java.awt.Dimension(10, 40));
        trkProgressTime.setName("trkProgressTime"); // NOI18N
        trkProgressTime.setPreferredSize(new java.awt.Dimension(150, 30));

        btnStopAllStream.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-playback-stop-bk.png"))); // NOI18N
        btnStopAllStream.setText(bundle.getString("STOP_ALL")); // NOI18N
        btnStopAllStream.setToolTipText("Stop All");
        btnStopAllStream.setName("btnStopAllStream"); // NOI18N
        btnStopAllStream.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnStopAllStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopAllStreamActionPerformed(evt);
            }
        });

        btnUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/go-up.png"))); // NOI18N
        btnUp.setToolTipText("Move selected Track UP");
        btnUp.setName("btnUp"); // NOI18N
        btnUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpActionPerformed(evt);
            }
        });

        btnDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/go-down.png"))); // NOI18N
        btnDown.setToolTipText("Move selected Track DOWN");
        btnDown.setName("btnDown"); // NOI18N
        btnDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownActionPerformed(evt);
            }
        });

        btnClearAllTrk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/button-small-clear.png"))); // NOI18N
        btnClearAllTrk.setToolTipText("Remove All Tracks");
        btnClearAllTrk.setName("btnClearAllTrk"); // NOI18N
        btnClearAllTrk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearAllTrkActionPerformed(evt);
            }
        });

        tglRemote.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/rss.png"))); // NOI18N
        tglRemote.setToolTipText("Remote Control (Beta) - Right Click for Settings");
        tglRemote.setEnabled(false);
        tglRemote.setFocusable(false);
        tglRemote.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglRemote.setMaximumSize(new java.awt.Dimension(29, 28));
        tglRemote.setMinimumSize(new java.awt.Dimension(25, 25));
        tglRemote.setName("tglRemote"); // NOI18N
        tglRemote.setPreferredSize(new java.awt.Dimension(28, 29));
        tglRemote.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/rss.png"))); // NOI18N
        tglRemote.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/rss_selected.png"))); // NOI18N
        tglRemote.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tglRemote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglRemoteActionPerformed(evt);
            }
        });
        tglRemote.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                JToggleButton button = ((JToggleButton) evt.getSource());
                if (!button.isSelected()) {
                    remoteRightMousePressed(evt);
                }
            }
        });

        btnStopOnlyStream.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-playback-stop-bk.png"))); // NOI18N
        btnStopOnlyStream.setText(bundle.getString("STREAMS")); // NOI18N
        btnStopOnlyStream.setToolTipText("Stop Media Playback Only");
        btnStopOnlyStream.setName("btnStopOnlyStream"); // NOI18N
        btnStopOnlyStream.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnStopOnlyStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopOnlyStreamActionPerformed(evt);
            }
        });

        PanelResource.setName("PanelResource"); // NOI18N
        PanelResource.setLayout(new java.awt.BorderLayout());

        jSeparator1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jSeparator1.setFont(new java.awt.Font("Noto Sans", 1, 18)); // NOI18N
        jSeparator1.setMaximumSize(new java.awt.Dimension(32767, 3));
        jSeparator1.setMinimumSize(new java.awt.Dimension(1, 1));
        jSeparator1.setName("jSeparator1"); // NOI18N
        jSeparator1.setPreferredSize(new java.awt.Dimension(100, 3));

        jSeparator2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jSeparator2.setFont(new java.awt.Font("Noto Sans", 1, 18)); // NOI18N
        jSeparator2.setMaximumSize(new java.awt.Dimension(32767, 3));
        jSeparator2.setName("jSeparator2"); // NOI18N
        jSeparator2.setPreferredSize(new java.awt.Dimension(0, 3));

        tglStartTrack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-playback-start.png"))); // NOI18N
        tglStartTrack.setText("Start/Stop Track");
        tglStartTrack.setToolTipText("Start/Stop selected Track Playlist");
        tglStartTrack.setName("tglStartTrack"); // NOI18N
        tglStartTrack.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-playback-start.png"))); // NOI18N
        tglStartTrack.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglStartTrack.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-playback-stop.png"))); // NOI18N
        tglStartTrack.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tglStartTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglStartTrackActionPerformed(evt);
            }
        });

        lblOnAir.setFont(new java.awt.Font("Ubuntu", 1, 18)); // NOI18N
        lblOnAir.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblOnAir.setText("ON AIR");
        lblOnAir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblOnAir.setName("lblOnAir"); // NOI18N

        btnSkipTrack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/media-skip-forward.png"))); // NOI18N
        btnSkipTrack.setToolTipText("Skip Track");
        btnSkipTrack.setName("btnSkipTrack"); // NOI18N
        btnSkipTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSkipTrackActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Noto Sans", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Current Track Timer (sec)");
        jLabel1.setName("jLabel1"); // NOI18N

        spinJumpPos.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        spinJumpPos.setToolTipText("Jump position.");
        spinJumpPos.setName("spinJumpPos"); // NOI18N
        spinJumpPos.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinJumpPosStateChanged(evt);
            }
        });

        btnJump.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/jump_button_4.png"))); // NOI18N
        btnJump.setText("Jump");
        btnJump.setToolTipText("Move the selected Track to the desired position.");
        btnJump.setName("btnJump"); // NOI18N
        btnJump.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnJumpActionPerformed(evt);
            }
        });

        jSeparator3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jSeparator3.setFont(new java.awt.Font("Noto Sans", 1, 18)); // NOI18N
        jSeparator3.setMaximumSize(new java.awt.Dimension(32767, 3));
        jSeparator3.setName("jSeparator3"); // NOI18N
        jSeparator3.setPreferredSize(new java.awt.Dimension(0, 3));

        btnDuplicateTrk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/duplicatebutton.png"))); // NOI18N
        btnDuplicateTrk.setText("Clone");
        btnDuplicateTrk.setToolTipText("Duplicate Selected Track");
        btnDuplicateTrk.setName("btnDuplicateTrk"); // NOI18N
        btnDuplicateTrk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDuplicateTrkActionPerformed(evt);
            }
        });

        btnAddGAP.setText("Add GAP");
        btnAddGAP.setToolTipText("Add a GAP between all Tracks");
        btnAddGAP.setName("btnAddGAP"); // NOI18N
        btnAddGAP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddGAPActionPerformed(evt);
            }
        });

        spinGAP.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        spinGAP.setToolTipText("GAP in seconds");
        spinGAP.setName("spinGAP"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(PanelResource, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblOnAir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnUp, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnDown, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnJump)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinJumpPos, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 116, Short.MAX_VALUE)
                                .addComponent(btnDuplicateTrk)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRemove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnClearAllTrk, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lstTracksScroll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tglStartTrack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSkipTrack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnStopOnlyStream, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnStopAllStream, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(trkProgressTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(lblTrackDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(trkDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(btnAddGAP)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinGAP, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblPlayingTrack, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(tglRemote, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnUp, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tglRemote, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(btnDown, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(spinJumpPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(btnJump, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addComponent(lblPlayingTrack, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnDuplicateTrk, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnClearAllTrk, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(spinGAP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(trkDuration)
                                    .addComponent(lblTrackDuration, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(trkProgressTime, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(btnStopOnlyStream, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnStopAllStream)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(btnSkipTrack)
                                        .addComponent(tglStartTrack))))
                            .addComponent(lstTracksScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblOnAir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(PanelResource, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAddGAP, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void listTracksValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listTracksValueChanged
        if (listTracks.getSelectedIndex() != -1) {
            selectTrack = listTracks.getSelectedValue().toString();
            int SelectCHIndex = listTracks.getSelectedIndex();
            if (lblPlayingTrack.getText().equals(selectTrack)) {
                btnRemove.setEnabled(false);
            } else {
                btnRemove.setEnabled(true);
            }
            trkDuration.setValue(CHTimers.get(SelectCHIndex) / 1000);
            tglRemote.setEnabled(true);
        } else {
            tglRemote.setEnabled(false);
        }
    }//GEN-LAST:event_listTracksValueChanged
    @SuppressWarnings("unchecked")
    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
//        String name = txtName.getText();
//        boolean noDuplicateCh = true;
//        for (String chName : arrayListTracks){
//            if (name.equals(chName)){
//                noDuplicateCh = false;
//                break;
//            }
//        }
//        
//        if (name.length() > 0 && noDuplicateCh) {
//            master.addTrack(name);
//            master.addTrkTransitions(name);
//            model.addElement(name);
//            CHTimers.add(CHTimer);
//            arrayListTracks.add(name);
//            listTracks.revalidate();
//            listTracks.setSelectedValue(name, true);
//        } else {
//            if (!noDuplicateCh){
//                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Track "+name+" Duplicated !!!");
//                ResourceMonitor.getInstance().addMessage(label);
//            }
//        }
    }//GEN-LAST:event_btnAddActionPerformed
    @SuppressWarnings("unchecked")
    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        if (listTracks.getSelectedIndex() != -1) {
            String name = listTracks.getSelectedValue().toString();
            int result = JOptionPane.showConfirmDialog(this, "Track \"" + name + "\" will be Deleted !!!", "Attention", JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JFileChooser.APPROVE_OPTION) {
                ArrayList<Stream> allStreams = MasterTracks.getInstance().getStreams();
                for (Stream s : allStreams) {
                    if (s.getisATrack()) {
                        if (s.getTrkName().equals(name)) {
                            s.setisATrack(false);
//                    System.out.println("StreamName="+s.getName());
//                    System.out.println("IsaTrack="+s.getisATrack());
                        }
                    }
                }
                int SelectCHIndex = listTracks.getSelectedIndex();
//        System.out.println(SelectCHIndex);
                if (SelectCHIndex == 0 && model.size() > 1) {
                    master.removeTrack(name);
                    model.removeElement(name);
                    CHTimers.remove(SelectCHIndex);
                    trkDuration.setValue(0);
                    arrayListTracks.remove(name);
                    listTracks.revalidate();
                } else {
                    master.removeTrack(name);
                    model.removeElement(name);
                    CHTimers.remove(SelectCHIndex);
                    trkDuration.setValue(0);
                    arrayListTracks.remove(name);
                    listTracks.revalidate();
                }
                updateTrackOn();
                if (name.length() > 25) {
                    name = name.substring(0, 25) + " ...";
                }
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "<html>&nbsp;Track <font color=" + selColLbl2 + ">\"" + name + "\"</font> Deleted.</html>");
                ResourceMonitor.getInstance().addMessage(label);
            } else {
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Delete Track Cancelled.");
                ResourceMonitor.getInstance().addMessage(label);
            }
//        System.out.println("StreamDurationArray="+CHTimers.toString());
        }
    }//GEN-LAST:event_btnRemoveActionPerformed

    @Override
    public ArrayList<Integer> getCHTimers() {
        return CHTimers;
    }

    private void remoteInitPopUp() {
        JMenuItem remoteSettings = new JMenuItem(new AbstractAction("Remote Settings") {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePrefs();
                RemoteSettings remoteSet = new RemoteSettings();
                remoteSet.setLocationRelativeTo(TrucklistStudio.cboAnimations);
                remoteSet.setAlwaysOnTop(true);
                remoteSet.setVisible(true);
            }
        });
        remoteSettings.setIcon(new ImageIcon(getClass().getResource("/truckliststudio/resources/tango/working-6.png"))); // NOI18N
        remotePopup.add(remoteSettings);
    }

    private void remoteRightMousePressed(java.awt.event.MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            remotePopup.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    @Override
    public void removeTracks(String removeSc, int a) {
        model.removeElement(removeSc);
        CHTimers.remove(a);
        arrayListTracks.remove(removeSc);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addLoadingTrack(String name) {
        boolean noDuplicateTrk = true;
        for (String chName : arrayListTracks) {
            if (name.equals(chName)) {
                noDuplicateTrk = false;
                break;
            }
        }
        if (noDuplicateTrk) {
            if (name.length() > 0) {
                model.addElement(name);
                Tools.sleep(100);
                listTracks.revalidate();
                arrayListTracks.add(name);
            }
            listTracks.setSelectedIndex(0);
        } else {
            if (name.length() > 25) {
                name = name.substring(0, 25) + " ...";
            }
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "<html>&nbsp;Track <font color=" + selColLbl2 + ">\"" + name + "\"</font> Duplicated.</html>");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }

    @Override
    public void stopChTime(ActionEvent evt) {
        RemoteStopCHTimerActionPerformed();
    }

    @Override
    public void resetBtnStates(ActionEvent evt) {
//        txtName.setText("");
        btnRemove.setEnabled(true);
//        btnRename.setEnabled(true);
        lblPlayingTrack.setText("");
        if (theme.equals("Dark")) {
            lblOnAir.setForeground(Color.WHITE);
        } else {
            lblOnAir.setForeground(Color.BLACK);
        }
        arrayListTracks.clear();
        CHTimers.clear();
        trkDuration.setValue(0);
    }

    class UpdateCHtUITask extends TimerTask {

        @Override
        public void run() {
            CHptS = null;
            trkProgressTime.setValue(0);
            trkProgressTime.setStringPainted(true);
            int CHpTemptime = trkNextTime / 1000;
            trkProgressTime.setMaximum(CHpTemptime);
            long beginTime = System.currentTimeMillis() / 1000;;
            long endTime = beginTime + CHpTemptime;
            while (CHpTemptime > 0 && stopTrkPt == false) {
                timeToTimer = CHpTemptime;
                CHptS = Integer.toString((int) (endTime - System.currentTimeMillis() / 1000));
                trkProgressTime.setValue((int) (endTime - System.currentTimeMillis() / 1000));
                trkProgressTime.setString(CHptS);

                for (int i = 0; i < 10; i++) {
                    Tools.sleep(100);
                    if (stopTrkPt) {
                        break;
                    }
                }

                CHpTemptime = (int) (endTime - System.currentTimeMillis() / 1000);
            }
            UpdateCHtUITask.this.stop();
        }

        public void stop() {
            stopTrkPt = true;
        }
    }

    class TSelectActionPerformed extends TimerTask {

        @Override
        public void run() {
            updateTrackOn();
//            System.out.println("IndexPlaying="+trkOn+" PlaylistSize="+arrayListTracks.size());
            if (trkOn == arrayListTracks.size() - 1) {
                trkNxName = arrayListTracks.get(0);
            } else {
                trkNxName = arrayListTracks.get(trkOn + 1);
            }
            int n = 0;
            for (String h : arrayListTracks) {
                if (h.equals(trkNxName)) {
                    trkNextTime = CHTimers.get(n);
                }
                n += 1;
            }
            totalToTimer = trkNextTime / 1000;
            listTracks.setSelectedValue(trkNxName, true);
            master.selectTrack(trkNxName);
            String name = listTracks.getSelectedValue().toString();
            System.out.println("Playing: " + name);
            lblPlayingTrack.setText(name);
            if (trkNextTime != 0) {
                trkT = new Timer();
                trkT.schedule(new TSelectActionPerformed(), trkNextTime);
                trkNextTime = CHTimers.get(listTracks.getSelectedIndex());
                stopTrkPt = false;
                trkProgressTime.setValue(0);
                trkT.schedule(new UpdateCHtUITask(), 0);
            } else {
                trkT.cancel();
                trkT.purge();
                stopTrkPt = true;
                listTracks.setEnabled(true);
                trkDuration.setEnabled(true);
                inTimer = false;
                trkProgressTime.setValue(0);
                trkProgressTime.setString("0");
            }
            btnRemove.setEnabled(false);
            listTracks.repaint();
        }
    }

    public static String getSelectedTrack() {
        return (listTracks.getSelectedValue().toString());
    }

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        if (listTracks.getSelectedIndex() != -1) {
            String name = listTracks.getSelectedValue().toString();
            master.updateTrackBtn(name);
            if (name.length() > 25) {
                name = name.substring(0, 25) + " ...";
            }
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "<html>&nbsp;Track <font color=" + selColLbl2 + ">\"" + name + "\"</font> Updated.</html>");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void RemoteStopCHTimerActionPerformed() {
        stopTrkPt = true;
        trkT.cancel();
        trkT.purge();
        listTracks.setEnabled(true);
        trkDuration.setEnabled(true);
        inTimer = false;
        trkProgressTime.setValue(0);
        trkProgressTime.setString("0");
        tglStartTrack.setSelected(false);
        if (theme.equals("Dark")) {
            lblOnAir.setForeground(Color.WHITE);
        } else {
            lblOnAir.setForeground(Color.BLACK);
        }
    }

    public void RemoteStopCHTimerOnlyActionPerformed() {
        trkT.cancel();
        trkT.purge();
        stopTrkPt = true;
        inTimer = false;
        trkProgressTime.setValue(0);
        trkProgressTime.setString("0");
        if (theme.equals("Dark")) {
            lblOnAir.setForeground(Color.WHITE);
        } else {
            lblOnAir.setForeground(Color.BLACK);
        }
    }

    private void btnStopAllStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopAllStreamActionPerformed
        PrePlayer.getPreInstance(null).stop();
        Tools.sleep(10);
        MasterTracks.getInstance().stopAllStream();
        for (Stream s : streamS) {
            s.updateStatus();
        }
        Tools.sleep(30);
        lblPlayingTrack.setText("");
        btnRemove.setEnabled(true);
        if (inTimer) {
            RemoteStopCHTimerActionPerformed();
        } else {
            RemoteStopCHTimerOnlyActionPerformed();
        }
        if (tglStartTrack.isSelected()) {
            tglStartTrack.setSelected(false);
        }
        listenerCPOP.resetButtonsStates(evt);
        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "All Stopped.");
        ResourceMonitor.getInstance().addMessage(label);
        listTracks.repaint();
//        System.gc();
    }//GEN-LAST:event_btnStopAllStreamActionPerformed

    @SuppressWarnings("unchecked")
    private void btnRenameActionPerformed(java.awt.event.ActionEvent evt) {
//        if (listTracks.getSelectedIndex() != -1) {
//            if (listTracks != null && txtName.getText().length() > 0) {
//                String rnName = txtName.getText();
//                String chName = listTracks.getSelectedValue().toString();
//                int selectCHIndex = listTracks.getSelectedIndex();
//                for (Stream stream : streamS){
//                    if (stream.getTrkName().equals(chName)) {
//                        stream.setTrkName(rnName);
//                    }
//                    for (SourceTrack sc : stream.getTracks()) {
//                        if (sc.getName().equals(chName)){
//                            sc.setName(rnName);
//                        }
//                    }
//                }
//                master.addTrackAt(rnName, selectCHIndex);
//                master.removeTrackAt(chName);
//                model.removeElement(chName);
//                CHTimers.remove(selectCHIndex);
//                arrayListTracks.remove(chName);
//                listTracks.revalidate();
//                model.insertElementAt(rnName, selectCHIndex);
//                CHTimers.add(selectCHIndex, CHTimer);
//                arrayListTracks.add(selectCHIndex, rnName);
//                listTracks.revalidate();
//            }
//        }
    }

    @SuppressWarnings("unchecked")
    public static void makeATrack(Stream stream) {
        String sourceName = stream.getName();
        boolean noDuplicateCh = true;
        boolean noTrack = false;
        for (String chName : arrayListTracks) {
            if (sourceName.equals(chName)) {
                noDuplicateCh = false;
                break;
            }
        }
        if (arrayListTracks.isEmpty()) {
            noTrack = true;
        }
        if (noDuplicateCh) {
            if (sourceName.length() > 0) {
                stream.setIsPlaying(true);
                stream.setisATrack(true);
                stream.setTrkName(sourceName);
                String playingTrack = lblPlayingTrack.getText();
                if (playingTrack.isEmpty()) {
                    master.addTrack(sourceName);
                } else {
//                    System.out.println("PlayingTrack="+playingTrack);
                    master.addPlayTrack(sourceName, lblPlayingTrack.getText());
                }
                model.addElement(sourceName);

                String sPrepTime = stream.getStreamTime().replaceAll("s", "");
                int sDuration = Integer.parseInt(sPrepTime);
                int chTimer = sDuration * 1000;
                CHTimers.add(chTimer);
                arrayListTracks.add(sourceName);
                if (noTrack) {
                    listTracks.setSelectedIndex(0);
                }
                listTracks.revalidate();
                stream.setIsPlaying(false);
            }
//            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+5000, sourceName + " Track made.");
//            ResourceMonitor.getInstance().addMessage(label);
        } else {
//            System.out.println("stream Destroy !!!");
//            stream.destroy();
            if (sourceName.length() > 25) {
                sourceName = sourceName.substring(0, 25) + " ...";
            }
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "<html>&nbsp;Track <font color=" + selColLbl2 + ">\"" + sourceName + "\"</font> Duplicated.</html>");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }

    @SuppressWarnings("unchecked")
    private void btnDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownActionPerformed
        if (listTracks.getSelectedIndex() != -1) {
            int selectedCHIndex = listTracks.getSelectedIndex();
            String selectedChName = arrayListTracks.get(selectedCHIndex);
            int selectedCHTimer = CHTimers.get(selectedCHIndex);
            int nextCHIndex;
            String nextChName;
            int nextCHTimer;
            if (listTracks != null && selectedCHIndex < arrayListTracks.size() - 1) {
                if (selectedCHIndex == arrayListTracks.size() - 2) {
                    nextCHIndex = selectedCHIndex + 1;
                    nextChName = arrayListTracks.get(nextCHIndex);
                    nextCHTimer = CHTimers.get(nextCHIndex);
                    //                System.out.println("Master Channels Before:"+master.getTracks());
                    // Update Master Channels
                    master.removeTrackAt(selectedChName);
                    master.removeTrackAt(nextChName);
                    master.addToTracks(nextChName);
                    master.addToTracks(selectedChName);
                    // Update Streams Channels
                    for (Stream stream : streamS) {
                        String streamName = stream.getClass().getName();
                        if (!streamName.contains("Sink")) {
                            SourceTrack tempSelSC = null;
                            SourceTrack tempNextSC = null;
                            for (SourceTrack sc : stream.getTracks()) {
                                if (sc.getName().equals(selectedChName)) {
                                    tempSelSC = sc;
                                }
                                if (sc.getName().equals(nextChName)) {
                                    tempNextSC = sc;
                                }
                            }
                            stream.addTrackAt(tempSelSC, nextCHIndex);
                            stream.addTrackAt(tempNextSC, selectedCHIndex);
                        }
                    }
                    //                System.out.println("Master Channels After:"+master.getTracks());
                    // Update UI lists and WS lists Channels
                    model.removeElement(selectedChName);
                    model.removeElement(nextChName);
                    CHTimers.remove(selectedCHIndex);
                    CHTimers.remove(selectedCHIndex);
                    arrayListTracks.remove(selectedChName);
                    arrayListTracks.remove(nextChName);
                    listTracks.revalidate();
                    model.addElement(nextChName);
                    model.addElement(selectedChName);
                    CHTimers.add(nextCHTimer);
                    CHTimers.add(selectedCHTimer);
                    arrayListTracks.add(nextChName);
                    arrayListTracks.add(selectedChName);
                    listTracks.revalidate();
                    listTracks.setSelectedIndex(nextCHIndex);
                } else {
                    nextCHIndex = selectedCHIndex + 1;
                    nextChName = arrayListTracks.get(nextCHIndex);
                    nextCHTimer = CHTimers.get(nextCHIndex);
                    //                System.out.println("Master Channels Before:"+master.getTracks());
                    // Update Master Channels
                    master.removeTrackAt(selectedChName);
                    master.removeTrackAt(nextChName);
                    master.addTrackAt(nextChName, selectedCHIndex);
                    master.addTrackAt(selectedChName, nextCHIndex);
                    // Update Streams Channels
                    for (Stream stream : streamS) {
                        String streamName = stream.getClass().getName();
                        if (!streamName.contains("Sink")) {
                            SourceTrack tempSelSC = null;
                            SourceTrack tempNextSC = null;
                            for (SourceTrack sc : stream.getTracks()) {
                                if (sc.getName().equals(selectedChName)) {
                                    tempSelSC = sc;
                                }
                                if (sc.getName().equals(nextChName)) {
                                    tempNextSC = sc;
                                }
                            }
                            stream.addTrackAt(tempSelSC, nextCHIndex);
                            stream.addTrackAt(tempNextSC, selectedCHIndex);
                        }
                    }
                    //                System.out.println("Master Channels After:"+master.getTracks());
                    // Update UI Channels lists and WS lists
                    model.removeElement(selectedChName);
                    model.removeElement(nextChName);
                    CHTimers.remove(selectedCHIndex);
                    CHTimers.remove(selectedCHIndex);
                    //                System.out.println("List Channels Timers Removed: "+CHTimers);
                    arrayListTracks.remove(selectedChName);
                    arrayListTracks.remove(nextChName);
                    listTracks.revalidate();
                    model.insertElementAt(nextChName, selectedCHIndex);
                    model.insertElementAt(selectedChName, nextCHIndex);
                    CHTimers.add(selectedCHIndex, nextCHTimer);
                    CHTimers.add(nextCHIndex, selectedCHTimer);
                    //                System.out.println("List Channels Timers After: "+CHTimers);
                    arrayListTracks.add(selectedCHIndex, nextChName);
                    arrayListTracks.add(nextCHIndex, selectedChName);
                    listTracks.revalidate();
                    listTracks.setSelectedIndex(nextCHIndex);
                }
                listTracks.ensureIndexIsVisible(listTracks.getSelectedIndex());
                updateTrackOn();
            }
        }
    }//GEN-LAST:event_btnDownActionPerformed
    @SuppressWarnings("unchecked")
    private void btnUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpActionPerformed
        if (listTracks.getSelectedIndex() != -1) {
            int selectedCHIndex = listTracks.getSelectedIndex();
            String selectedChName = arrayListTracks.get(selectedCHIndex);
            int selectedCHTimer = CHTimers.get(selectedCHIndex);
            int previousCHIndex;
            String previousChName;
            int previousCHTimer;
            //        System.out.println("List Channels Timers: "+CHTimers);
            if (listTracks != null && selectedCHIndex > 0) {
                if (selectedCHIndex == 1) {
                    previousCHIndex = selectedCHIndex - 1;
                    previousChName = arrayListTracks.get(previousCHIndex);
                    previousCHTimer = CHTimers.get(previousCHIndex);
                    //                System.out.println("Master Channels Before:"+master.getTracks());
                    // Update Master Channels
                    master.removeTrackAt(selectedChName);
                    master.removeTrackAt(previousChName);
                    master.addTrackAt(selectedChName, previousCHIndex);
                    master.addTrackAt(previousChName, selectedCHIndex);
                    // Update Streams Channels
                    for (Stream stream : streamS) {
                        String streamName = stream.getClass().getName();
                        if (!streamName.contains("Sink")) {
                            SourceTrack tempSelSC = null;
                            SourceTrack tempPrevSC = null;
                            for (SourceTrack sc : stream.getTracks()) {
                                if (sc.getName().equals(selectedChName)) {
                                    tempSelSC = sc;
                                }
                                if (sc.getName().equals(selectedChName)) {
                                    tempSelSC = sc;
                                }
                                if (sc.getName().equals(previousChName)) {
                                    tempPrevSC = sc;
                                }
                            }
                            stream.addTrackAt(tempSelSC, previousCHIndex);
                            stream.addTrackAt(tempPrevSC, selectedCHIndex);
                        }
                    }
                    //                System.out.println("Master Channels After:"+master.getTracks());
                    // Update UI lists and WS lists Channels
                    model.removeElement(selectedChName);
                    model.removeElement(previousChName);
                    CHTimers.remove(selectedCHIndex);
                    CHTimers.remove(previousCHIndex);
                    arrayListTracks.remove(selectedChName);
                    arrayListTracks.remove(previousChName);
                    listTracks.revalidate();
                    model.insertElementAt(selectedChName, previousCHIndex);
                    model.insertElementAt(previousChName, selectedCHIndex);
                    CHTimers.add(previousCHIndex, selectedCHTimer);
                    CHTimers.add(selectedCHIndex, previousCHTimer);
                    arrayListTracks.add(previousCHIndex, selectedChName);
                    arrayListTracks.add(selectedCHIndex, previousChName);
                    listTracks.revalidate();
                    listTracks.setSelectedIndex(previousCHIndex);
                } else {
                    previousCHIndex = selectedCHIndex - 1;
                    previousChName = arrayListTracks.get(previousCHIndex);
                    previousCHTimer = CHTimers.get(previousCHIndex);
                    //                System.out.println("Master Channels Before:"+master.getTracks());
                    // Update Master Channels
                    master.removeTrackAt(selectedChName);
                    master.removeTrackAt(previousChName);
                    master.addTrackAt(selectedChName, previousCHIndex);
                    master.addTrackAt(previousChName, selectedCHIndex);
                    // Update Streams Channels
                    for (Stream stream : streamS) {
                        String streamName = stream.getClass().getName();
                        if (!streamName.contains("Sink")) {
                            SourceTrack tempSelSC = null;
                            SourceTrack tempPrevSC = null;
                            for (SourceTrack sc : stream.getTracks()) {
                                if (sc.getName().equals(selectedChName)) {
                                    tempSelSC = sc;
                                }
                                if (sc.getName().equals(previousChName)) {
                                    tempPrevSC = sc;
                                }
                            }
                            stream.addTrackAt(tempSelSC, previousCHIndex);
                            stream.addTrackAt(tempPrevSC, selectedCHIndex);
                        }
                    }
                    //                System.out.println("Master Channels After:"+master.getTracks());
                    // Update UI Channels lists and WS lists
                    model.removeElement(selectedChName);
                    model.removeElement(previousChName);
                    CHTimers.remove(selectedCHIndex);
                    CHTimers.remove(previousCHIndex);
                    arrayListTracks.remove(selectedChName);
                    arrayListTracks.remove(previousChName);
                    listTracks.revalidate();
                    model.insertElementAt(selectedChName, previousCHIndex);
                    model.insertElementAt(previousChName, selectedCHIndex);
                    CHTimers.add(previousCHIndex, selectedCHTimer);
                    CHTimers.add(selectedCHIndex, previousCHTimer);
                    arrayListTracks.add(previousCHIndex, selectedChName);
                    arrayListTracks.add(selectedCHIndex, previousChName);
                    listTracks.revalidate();
                    listTracks.setSelectedIndex(previousCHIndex);
                }
                listTracks.ensureIndexIsVisible(listTracks.getSelectedIndex());
                updateTrackOn();
            }
        }
    }//GEN-LAST:event_btnUpActionPerformed

    private void btnClearAllTrkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearAllTrkActionPerformed
        int result = JOptionPane.showConfirmDialog(this, "All Tracks will be Deleted !!!", "Attention", JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == JFileChooser.APPROVE_OPTION) {
            ArrayList<Stream> allStreams = MasterTracks.getInstance().getStreams();
            for (Stream s : allStreams) {
                if (s.getisATrack()) {
                    s.setisATrack(false);
                }
            }
            ArrayList<String> sourceChI = MasterTracks.getInstance().getTracks();
            if (sourceChI.size() > 0) {
                do {
                    for (int a = 0; a < sourceChI.size(); a++) {
                        String removeSc = sourceChI.get(a);
                        MasterTracks.getInstance().removeTrack(removeSc);
                        removeTracks(removeSc, a);
                    }
                } while (sourceChI.size() > 0);
                resetBtnStates(evt);
            }
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "All Tracks Deleted!");
            ResourceMonitor.getInstance().addMessage(label);
        } else {
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Delete All Tracks Cancelled!");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_btnClearAllTrkActionPerformed

    private void listTracksComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_listTracksComponentAdded
        int index = listTracks.getSelectedIndex();
        if (index != -1) {
            btnClearAllTrk.setEnabled(true);
        }
    }//GEN-LAST:event_listTracksComponentAdded

    private void tglRemoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglRemoteActionPerformed
        if (tglRemote.isSelected()) {
            remote.setPort(remPort);
            remote.start();
        } else {
            remote.stop();
        }
    }//GEN-LAST:event_tglRemoteActionPerformed

    private void btnStopOnlyStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopOnlyStreamActionPerformed
        MasterTracks.getInstance().stopOnlyStream();
        for (Stream s : streamS) {
            s.updateStatus();
        }
        Tools.sleep(30);
        lblPlayingTrack.setText("");
        btnRemove.setEnabled(true);
//        btnRename.setEnabled(true);
        if (inTimer) {
            RemoteStopCHTimerActionPerformed();
        } else {
            RemoteStopCHTimerOnlyActionPerformed();
        }
        if (tglStartTrack.isSelected()) {
            tglStartTrack.setSelected(false);
        }
        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Streams Stopped.");
        ResourceMonitor.getInstance().addMessage(label);
        listTracks.repaint();
//        System.gc();
    }//GEN-LAST:event_btnStopOnlyStreamActionPerformed

    private void listTracksMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listTracksMouseClicked
        if (evt.getClickCount() == 2 && !evt.isConsumed()) {
            evt.consume();
            if (!tglStartTrack.isSelected()) {
                
                if (listTracks.getSelectedIndex() != -1) {
                    String name = listTracks.getSelectedValue().toString();
                    master.selectTrack(name);
                    savePrefs();
                    System.out.println("Playing: " + name);
                    lblPlayingTrack.setText(name);
                    btnRemove.setEnabled(false);
                    tglRemote.setEnabled(true);

                    if (CHTimers.get(listTracks.getSelectedIndex()) != 0) {
                        inTimer = true;
                        trkT = new Timer();
                        trkT.schedule(new TSelectActionPerformed(), CHTimers.get(listTracks.getSelectedIndex()));
                        trkNextTime = CHTimers.get(listTracks.getSelectedIndex());
                        totalToTimer = trkNextTime / 1000;
                        stopTrkPt = false;
                        trkT.schedule(new UpdateCHtUITask(), 0);
                    }
                }
                tglStartTrack.setSelected(true);
                updateTrackOn();

            } else {
                if (trkOn != listTracks.getSelectedIndex()) {
                    RemoteStopCHTimerActionPerformed();
                    master.stopTextCDown();
                    Tools.sleep(100);

                    if (listTracks.getSelectedIndex() != -1) {
                        String name = listTracks.getSelectedValue().toString();
                        master.selectTrack(name);
                        savePrefs();
                        System.out.println("Playing: " + name);
                        lblPlayingTrack.setText(name);
                        btnRemove.setEnabled(false);
                        tglRemote.setEnabled(true);

                        if (CHTimers.get(listTracks.getSelectedIndex()) != 0) {
                            inTimer = true;
                            trkT = new Timer();
                            trkT.schedule(new TSelectActionPerformed(), CHTimers.get(listTracks.getSelectedIndex()));
                            trkNextTime = CHTimers.get(listTracks.getSelectedIndex());
                            totalToTimer = trkNextTime / 1000;
                            stopTrkPt = false;
                            trkT.schedule(new UpdateCHtUITask(), 0);
                        }
                    }

                }
                tglStartTrack.setSelected(true);
                updateTrackOn();
            }
            listTracks.repaint();
        }
    }//GEN-LAST:event_listTracksMouseClicked

    private void trkDurationStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_trkDurationStateChanged
        CHTimer = trkDuration.getValue().hashCode() * 1000;
        if (listTracks.getSelectedIndex() != -1) {
            int ChIndex = listTracks.getSelectedIndex();
            CHTimers.set(ChIndex, CHTimer);
        }
    }//GEN-LAST:event_trkDurationStateChanged

    public class TrackListCellRender extends JLabel implements ListCellRenderer {

        public TrackListCellRender() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setText("[" + (index + 1) + "] " + value.toString());
            setFont(new java.awt.Font("Noto Sans", 0, 18));
            String sTrack = listTracks.getModel().getElementAt(index).toString();
            if (sTrack.equals(lblPlayingTrack.getText())) {
                setForeground(Color.YELLOW);
                setFont(new java.awt.Font("Noto Sans", 1, 18));
            } else {
                setForeground(Color.WHITE);
                setFont(new java.awt.Font("Noto Sans", 0, 18));
            }

            if (isSelected) {
                setBackground(Color.DARK_GRAY);
            } else {
                setBackground(Color.LIGHT_GRAY.darker());
            }
            return this;
        }
    }

    private void updateTrackOn() {
        int index = 0;
        for (String currPlaying : arrayListTracks) {
            if (currPlaying.equals(lblPlayingTrack.getText())) {
                trkOn = index;
                break;
            }
            index++;
        }
    }

    public void startItsTrack(String name) {
        RemoteStopCHTimerActionPerformed();
        master.stopTextCDown();
        Tools.sleep(100);
        lblPlayingTrack.setText(name);
        updateTrackOn();
        master.selectTrack(name);
        savePrefs();
        System.out.println("Playing: " + name);
        lblPlayingTrack.setText(name);
        btnRemove.setEnabled(false);
        tglRemote.setEnabled(true);
        tglStartTrack.setSelected(true);
        if (CHTimers.get(trkOn) != 0) {
            inTimer = true;
            trkT = new Timer();
            trkT.schedule(new TSelectActionPerformed(), CHTimers.get(trkOn));
            trkNextTime = CHTimers.get(trkOn);
            totalToTimer = trkNextTime / 1000;
            stopTrkPt = false;
            trkT.schedule(new UpdateCHtUITask(), 0);
        }
        listTracks.ensureIndexIsVisible(trkOn);
        listTracks.setSelectedIndex(trkOn);
        listTracks.repaint();
    }

    public void stopItsTrack() {
        RemoteStopCHTimerActionPerformed();
        master.stopTextCDown();
        Tools.sleep(100);
        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Track Stopped.");
        ResourceMonitor.getInstance().addMessage(label);
        tglStartTrack.setSelected(false);
        listTracks.repaint();
    }

    private void tglStartTrackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglStartTrackActionPerformed
        if (tglStartTrack.isSelected()) {
            if (listTracks.getSelectedIndex() != -1) {
                String name = listTracks.getSelectedValue().toString();
                master.selectTrack(name);
                savePrefs();
                System.out.println("Playing: " + name);
                lblPlayingTrack.setText(name);
                btnRemove.setEnabled(false);
                tglRemote.setEnabled(true);

                if (CHTimers.get(listTracks.getSelectedIndex()) != 0) {
                    inTimer = true;
                    trkT = new Timer();
                    trkT.schedule(new TSelectActionPerformed(), CHTimers.get(listTracks.getSelectedIndex()));
                    trkNextTime = CHTimers.get(listTracks.getSelectedIndex());
                    totalToTimer = trkNextTime / 1000;
                    stopTrkPt = false;
                    trkT.schedule(new UpdateCHtUITask(), 0);
                }
            }
            updateTrackOn();
        } else {
            RemoteStopCHTimerActionPerformed();
            master.stopTextCDown();
            Tools.sleep(100);
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "Track Timer Stopped.");
            ResourceMonitor.getInstance().addMessage(label);
        }
        listTracks.repaint();

    }//GEN-LAST:event_tglStartTrackActionPerformed

    private void btnSkipTrackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSkipTrackActionPerformed
        if (tglStartTrack.isSelected()) {
            RemoteStopCHTimerActionPerformed();
            master.stopTextCDown();
            Tools.sleep(100);
            updateTrackOn();
//            System.out.println("IndexPlaying="+trkOn+" PlaylistSize="+arrayListTracks.size());
            if (trkOn == arrayListTracks.size() - 1) {
                trkNxName = arrayListTracks.get(0);
            } else {
                trkNxName = arrayListTracks.get(trkOn + 1);
            }
            int n = 0;
            for (String h : arrayListTracks) {
                if (h.equals(trkNxName)) {
                    trkNextTime = CHTimers.get(n);
                }
                n += 1;
            }
            totalToTimer = trkNextTime / 1000;
            listTracks.setSelectedValue(trkNxName, true);
            master.selectTrack(trkNxName);
            String name = listTracks.getSelectedValue().toString();
            System.out.println("Playing: " + name);
            lblPlayingTrack.setText(name);
//            listTracks.repaint();
            if (trkNextTime != 0) {
                trkT = new Timer();
                trkT.schedule(new TSelectActionPerformed(), trkNextTime);
                trkNextTime = CHTimers.get(listTracks.getSelectedIndex());
                stopTrkPt = false;
                trkProgressTime.setValue(0);
                trkT.schedule(new UpdateCHtUITask(), 0);
            } else {
                trkT.cancel();
                trkT.purge();
                stopTrkPt = true;
                listTracks.setEnabled(true);
                trkDuration.setEnabled(true);
                inTimer = false;
                trkProgressTime.setValue(0);
                trkProgressTime.setString("0");
            }
            tglStartTrack.setSelected(true);
            listTracks.repaint();
        }
    }//GEN-LAST:event_btnSkipTrackActionPerformed
    @SuppressWarnings("unchecked")
    private void btnJumpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnJumpActionPerformed
        if (listTracks.getSelectedIndex() != -1) {
            int selectedCHIndex = listTracks.getSelectedIndex();
            String selectedChName = arrayListTracks.get(selectedCHIndex);
            int selectedCHTimer = CHTimers.get(selectedCHIndex);
            int jumpPos = (Integer) spinJumpPos.getValue() - 1;

            if (selectedCHIndex > jumpPos) {
                int deltaPos = selectedCHIndex - jumpPos;
                int previousCHIndex;
                String previousChName;
                int previousCHTimer;
                for (int t = 0; t < deltaPos; t++) {
                    if (listTracks != null && selectedCHIndex > 0) {
                        if (selectedCHIndex == 1) {
                            previousCHIndex = selectedCHIndex - 1;
                            previousChName = arrayListTracks.get(previousCHIndex);
                            previousCHTimer = CHTimers.get(previousCHIndex);
                            // Update Master Tracks
                            master.removeTrackAt(selectedChName);
                            master.removeTrackAt(previousChName);
                            master.addTrackAt(selectedChName, previousCHIndex);
                            master.addTrackAt(previousChName, selectedCHIndex);
                            // Update Streams Tracks
                            for (Stream stream : streamS) {
                                String streamName = stream.getClass().getName();
                                if (!streamName.contains("Sink")) {
                                    SourceTrack tempSelSC = null;
                                    SourceTrack tempPrevSC = null;
                                    for (SourceTrack sc : stream.getTracks()) {
                                        if (sc.getName().equals(selectedChName)) {
                                            tempSelSC = sc;
                                        }
                                        if (sc.getName().equals(selectedChName)) {
                                            tempSelSC = sc;
                                        }
                                        if (sc.getName().equals(previousChName)) {
                                            tempPrevSC = sc;
                                        }
                                    }
                                    stream.addTrackAt(tempSelSC, previousCHIndex);
                                    stream.addTrackAt(tempPrevSC, selectedCHIndex);
                                }
                            }
                            // Update UI Tracklist and TS lists Tracks
                            model.removeElement(selectedChName);
                            model.removeElement(previousChName);
                            CHTimers.remove(selectedCHIndex);
                            CHTimers.remove(previousCHIndex);
                            arrayListTracks.remove(selectedChName);
                            arrayListTracks.remove(previousChName);
                            listTracks.revalidate();
                            model.insertElementAt(selectedChName, previousCHIndex);
                            model.insertElementAt(previousChName, selectedCHIndex);
                            CHTimers.add(previousCHIndex, selectedCHTimer);
                            CHTimers.add(selectedCHIndex, previousCHTimer);
                            arrayListTracks.add(previousCHIndex, selectedChName);
                            arrayListTracks.add(selectedCHIndex, previousChName);
                            listTracks.revalidate();
                            listTracks.setSelectedIndex(previousCHIndex);
                        } else {
                            previousCHIndex = selectedCHIndex - 1;
                            previousChName = arrayListTracks.get(previousCHIndex);
                            previousCHTimer = CHTimers.get(previousCHIndex);
                            // Update Master Tracks
                            master.removeTrackAt(selectedChName);
                            master.removeTrackAt(previousChName);
                            master.addTrackAt(selectedChName, previousCHIndex);
                            master.addTrackAt(previousChName, selectedCHIndex);
                            // Update Streams Tracks
                            for (Stream stream : streamS) {
                                String streamName = stream.getClass().getName();
                                if (!streamName.contains("Sink")) {
                                    SourceTrack tempSelSC = null;
                                    SourceTrack tempPrevSC = null;
                                    for (SourceTrack sc : stream.getTracks()) {
                                        if (sc.getName().equals(selectedChName)) {
                                            tempSelSC = sc;
                                        }
                                        if (sc.getName().equals(previousChName)) {
                                            tempPrevSC = sc;
                                        }
                                    }
                                    stream.addTrackAt(tempSelSC, previousCHIndex);
                                    stream.addTrackAt(tempPrevSC, selectedCHIndex);
                                }
                            }
                            // Update UI Tracks lists and TS lists
                            model.removeElement(selectedChName);
                            model.removeElement(previousChName);
                            CHTimers.remove(selectedCHIndex);
                            CHTimers.remove(previousCHIndex);
                            arrayListTracks.remove(selectedChName);
                            arrayListTracks.remove(previousChName);
                            listTracks.revalidate();
                            model.insertElementAt(selectedChName, previousCHIndex);
                            model.insertElementAt(previousChName, selectedCHIndex);
                            CHTimers.add(previousCHIndex, selectedCHTimer);
                            CHTimers.add(selectedCHIndex, previousCHTimer);
                            arrayListTracks.add(previousCHIndex, selectedChName);
                            arrayListTracks.add(selectedCHIndex, previousChName);
                            listTracks.revalidate();
                            listTracks.setSelectedIndex(previousCHIndex);
                        }
                    }
                    selectedCHIndex--;
                }
                listTracks.ensureIndexIsVisible(listTracks.getSelectedIndex());
                updateTrackOn();
            } else if (selectedCHIndex < jumpPos) {
                int deltaPos = jumpPos - selectedCHIndex;
                int nextCHIndex;
                String nextChName;
                int nextCHTimer;
                for (int t = 0; t < deltaPos; t++) {
                    if (listTracks != null && selectedCHIndex < arrayListTracks.size() - 1) {
                        if (selectedCHIndex == arrayListTracks.size() - 2) {
                            nextCHIndex = selectedCHIndex + 1;
                            nextChName = arrayListTracks.get(nextCHIndex);
                            nextCHTimer = CHTimers.get(nextCHIndex);
                            // Update Master Tracks
                            master.removeTrackAt(selectedChName);
                            master.removeTrackAt(nextChName);
                            master.addToTracks(nextChName);
                            master.addToTracks(selectedChName);
                            // Update Streams Tracks
                            for (Stream stream : streamS) {
                                String streamName = stream.getClass().getName();
                                if (!streamName.contains("Sink")) {
                                    SourceTrack tempSelSC = null;
                                    SourceTrack tempNextSC = null;
                                    for (SourceTrack sc : stream.getTracks()) {
                                        if (sc.getName().equals(selectedChName)) {
                                            tempSelSC = sc;
                                        }
                                        if (sc.getName().equals(nextChName)) {
                                            tempNextSC = sc;
                                        }
                                    }
                                    stream.addTrackAt(tempSelSC, nextCHIndex);
                                    stream.addTrackAt(tempNextSC, selectedCHIndex);
                                }
                            }
                            // Update UI lists and TS lists Tracks
                            model.removeElement(selectedChName);
                            model.removeElement(nextChName);
                            CHTimers.remove(selectedCHIndex);
                            CHTimers.remove(selectedCHIndex);
                            arrayListTracks.remove(selectedChName);
                            arrayListTracks.remove(nextChName);
                            listTracks.revalidate();
                            model.addElement(nextChName);
                            model.addElement(selectedChName);
                            CHTimers.add(nextCHTimer);
                            CHTimers.add(selectedCHTimer);
                            arrayListTracks.add(nextChName);
                            arrayListTracks.add(selectedChName);
                            listTracks.revalidate();
                            listTracks.setSelectedIndex(nextCHIndex);
                        } else {
                            nextCHIndex = selectedCHIndex + 1;
                            nextChName = arrayListTracks.get(nextCHIndex);
                            nextCHTimer = CHTimers.get(nextCHIndex);
                            // Update Master Tracks
                            master.removeTrackAt(selectedChName);
                            master.removeTrackAt(nextChName);
                            master.addTrackAt(nextChName, selectedCHIndex);
                            master.addTrackAt(selectedChName, nextCHIndex);
                            // Update Streams Tracks
                            for (Stream stream : streamS) {
                                String streamName = stream.getClass().getName();
                                if (!streamName.contains("Sink")) {
                                    SourceTrack tempSelSC = null;
                                    SourceTrack tempNextSC = null;
                                    for (SourceTrack sc : stream.getTracks()) {
                                        if (sc.getName().equals(selectedChName)) {
                                            tempSelSC = sc;
                                        }
                                        if (sc.getName().equals(nextChName)) {
                                            tempNextSC = sc;
                                        }
                                    }
                                    stream.addTrackAt(tempSelSC, nextCHIndex);
                                    stream.addTrackAt(tempNextSC, selectedCHIndex);
                                }
                            }
                            // Update UI Tracks lists and TS lists
                            model.removeElement(selectedChName);
                            model.removeElement(nextChName);
                            CHTimers.remove(selectedCHIndex);
                            CHTimers.remove(selectedCHIndex);
                            arrayListTracks.remove(selectedChName);
                            arrayListTracks.remove(nextChName);
                            listTracks.revalidate();
                            model.insertElementAt(nextChName, selectedCHIndex);
                            model.insertElementAt(selectedChName, nextCHIndex);
                            CHTimers.add(selectedCHIndex, nextCHTimer);
                            CHTimers.add(nextCHIndex, selectedCHTimer);
                            arrayListTracks.add(selectedCHIndex, nextChName);
                            arrayListTracks.add(nextCHIndex, selectedChName);
                            listTracks.revalidate();
                            listTracks.setSelectedIndex(nextCHIndex);
                        }
                    }
                    selectedCHIndex++;
                }
                listTracks.ensureIndexIsVisible(listTracks.getSelectedIndex());
                updateTrackOn();
            }
        }
    }//GEN-LAST:event_btnJumpActionPerformed

    private void spinJumpPosStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinJumpPosStateChanged
        int listSize = listTracks.getModel().getSize();
        if (listSize > 0) {
            if ((Integer) spinJumpPos.getValue() > listSize) {
                spinJumpPos.setValue(listSize);
            }
        }
    }//GEN-LAST:event_spinJumpPosStateChanged

    public class StringComparator implements Comparator<String> {

        public int compare(String c1, String c2) {
            return c1.compareTo(c2);
        }
    }

    @SuppressWarnings("unchecked")
    private void btnDuplicateTrkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDuplicateTrkActionPerformed
        if (listTracks.getSelectedIndex() != -1) {
            int selectedTrkIndex = listTracks.getSelectedIndex();
            final String selectedTrkName = arrayListTracks.get(selectedTrkIndex);

            int count = 0;
            count = arrayListTracks.stream().filter((trkName) -> (trkName.contains(selectedTrkName))).map((_item) -> 1).reduce(count, Integer::sum);
            String duplicatedTrkName = selectedTrkName + "(" + count + ")";
            boolean nodup = false;
            ArrayList<String> temp = new ArrayList<>();
            for (String c : arrayListTracks) {
                temp.add(c);
            }
            while (nodup != true) {
                for (String trkName : arrayListTracks) {
                    if (trkName.equals(duplicatedTrkName)) {
                        count++;
                        duplicatedTrkName = selectedTrkName + "(" + count + ")";
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
//            System.out.println("I'm Out !!!");
            int selectedTrkTimer = CHTimers.get(selectedTrkIndex);

            ArrayList<Stream> allStreams = MasterTracks.getInstance().getStreams();
            for (Stream s : allStreams) {
                if (!s.getClass().toString().contains("Sink")) {
//                    System.out.println("StreamName="+s.getName());
                    SourceTrack streamTrk = master.getTrack(selectedTrkName, s);
                    SourceTrack dupTrk = SourceTrack.duplicateTrack(streamTrk);
                    dupTrk.setName(duplicatedTrkName);
                    dupTrk.setIsCloned(true);
                    s.addTrack(dupTrk);
                }
            }

            master.addTrack2List(duplicatedTrkName);
            model.addElement(duplicatedTrkName);
            CHTimers.add(selectedTrkTimer);
            arrayListTracks.add(duplicatedTrkName);
            listTracks.revalidate();
            //            System.out.println("Tracks="+arrayListTracks);
            String labelName = selectedTrkName;
            if (selectedTrkName.length() > 25) {
                labelName = selectedTrkName.substring(0, 25) + " ...";
            }
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "<html>&nbsp;Track <font color=" + selColLbl2 + ">\"" + labelName + "\"</font> Duplicated.</html>");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_btnDuplicateTrkActionPerformed

    private void btnAddGAPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddGAPActionPerformed
        int i = 0;
        for (int timer : CHTimers) {
            timer = timer + spinGAP.getValue().hashCode() * 1000;
            CHTimers.set(i, timer);
            i++;
        }
        if (listTracks.getSelectedIndex() != -1) {
            selectTrack = listTracks.getSelectedValue().toString();
            int SelectCHIndex = listTracks.getSelectedIndex();
            trkDuration.setValue(CHTimers.get(SelectCHIndex) / 1000);
        }
    }//GEN-LAST:event_btnAddGAPActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JPanel PanelResource;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnAddGAP;
    private javax.swing.JButton btnClearAllTrk;
    private javax.swing.JButton btnDown;
    private javax.swing.JButton btnDuplicateTrk;
    private javax.swing.JButton btnJump;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnSkipTrack;
    private javax.swing.JButton btnStopAllStream;
    public static javax.swing.JButton btnStopOnlyStream;
    private javax.swing.JButton btnUp;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    public static javax.swing.JLabel lblOnAir;
    public static javax.swing.JLabel lblPlayingTrack;
    private javax.swing.JLabel lblTrackDuration;
    public static javax.swing.JList listTracks;
    private javax.swing.JScrollPane lstTracksScroll;
    private javax.swing.JSpinner spinGAP;
    private javax.swing.JSpinner spinJumpPos;
    private javax.swing.JToggleButton tglRemote;
    private javax.swing.JToggleButton tglStartTrack;
    private javax.swing.JSpinner trkDuration;
    private javax.swing.JProgressBar trkProgressTime;
    // End of variables declaration//GEN-END:variables

}
