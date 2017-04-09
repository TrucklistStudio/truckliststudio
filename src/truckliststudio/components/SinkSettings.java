/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package truckliststudio.components;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import truckliststudio.TrucklistStudio;
import truckliststudio.streams.SinkFile;
import truckliststudio.streams.SinkHLS;
import truckliststudio.streams.SinkUDP;
import truckliststudio.streams.Stream;

/**
 *
 * @author karl
 */
public class SinkSettings extends javax.swing.JFrame implements Stream.Listener {
    SinkFile thisSinkFile = null;
    SinkUDP thisSinkUDP = null;
    SinkHLS thisSinkHLS = null;
    /**
     * Creates new form FMESettings
     * @param sFile
     * @param udp
     */
    public SinkSettings(SinkFile sFile, SinkUDP udp, SinkHLS hls) {
        initComponents();
        // for now we keep this not visible
        lblOW.setVisible(false);
        lblOH.setVisible(false);
        spinOutW.setVisible(false);
        spinOutH.setVisible(false);
        
        if (sFile != null) {
            chkHQMode.setEnabled(false);
            lblrtmpURL.setEnabled(false);
            chkHQMode.setForeground(Color.gray);
            thisSinkFile = sFile;
            thisSinkUDP = null;
            thisSinkHLS = null;
            lblName.setText(thisSinkFile.getName());
            this.setTitle(thisSinkFile.getName() + " Settings");
            if (thisSinkFile.getVbitrate().equals("")) {
                spinVideoRate.setValue(0);
                spinVideoRate.setEnabled(false);
            } else {
                spinVideoRate.setValue(Integer.parseInt(thisSinkFile.getVbitrate()));
            }
            if (thisSinkFile.getWidth() == 0) {
                spinOutW.setValue(0);
                spinOutW.setEnabled(false);
            } else {
                spinOutW.setValue(thisSinkFile.getWidth());
            }
            if (thisSinkFile.getHeight() == 0) {
                spinOutH.setValue(0);
                spinOutH.setEnabled(false);
            } else {
                spinOutH.setValue(thisSinkFile.getHeight());
            }
            if (thisSinkFile.getAbitrate().equals("")) {
                spinAudioRate.setValue(0);
                spinAudioRate.setEnabled(false);
            } else {
                spinAudioRate.setValue(Integer.parseInt(thisSinkFile.getAbitrate()));
            }
        } else if (udp != null) {
            thisSinkUDP = udp;
            thisSinkFile = null;
            thisSinkHLS = null;
            lblrtmpURL.setEnabled(false);
            lblName.setText(thisSinkUDP.getName());
            this.setTitle(thisSinkUDP.getName() + " Settings");
            if (thisSinkUDP.getVbitrate().equals("")) {
                spinVideoRate.setValue(0);
                spinVideoRate.setEnabled(false);
            } else {
                spinVideoRate.setValue(Integer.parseInt(thisSinkUDP.getVbitrate()));
            }
            if (thisSinkUDP.getWidth() == 0) {
                spinOutW.setValue(0);
                spinOutW.setEnabled(false);
            } else {
                spinOutW.setValue(thisSinkUDP.getWidth());
            }
            if (thisSinkUDP.getHeight() == 0) {
                spinOutH.setValue(0);
                spinOutH.setEnabled(false);
            } else {
                spinOutH.setValue(thisSinkUDP.getHeight());
            }
            if (thisSinkUDP.getAbitrate().equals("")) {
                spinAudioRate.setValue(0);
                spinAudioRate.setEnabled(false);
            } else {
                spinAudioRate.setValue(Integer.parseInt(thisSinkUDP.getAbitrate()));
            }
            chkHQMode.setSelected(thisSinkUDP.getStandard().equals("HQ"));
        } else {
            thisSinkUDP = null;
            thisSinkFile = null;
            thisSinkHLS = hls;
            lblrtmpURL.setEnabled(true);
            lblName.setText(thisSinkHLS.getName());
            this.setTitle(thisSinkHLS.getName() + " Settings");
            if (thisSinkHLS.getVbitrate().equals("")) {
                spinVideoRate.setValue(0);
                spinVideoRate.setEnabled(false);
            } else {
                spinVideoRate.setValue(Integer.parseInt(thisSinkHLS.getVbitrate()));
            }
            if (thisSinkHLS.getWidth() == 0) {
                spinOutW.setValue(0);
                spinOutW.setEnabled(false);
            } else {
                spinOutW.setValue(thisSinkHLS.getWidth());
            }
            if (thisSinkHLS.getHeight() == 0) {
                spinOutH.setValue(0);
                spinOutH.setEnabled(false);
            } else {
                spinOutH.setValue(thisSinkHLS.getHeight());
            }
            if (thisSinkHLS.getAbitrate().equals("")) {
                spinAudioRate.setValue(0);
                spinAudioRate.setEnabled(false);
            } else {
                spinAudioRate.setValue(Integer.parseInt(thisSinkHLS.getAbitrate()));
            }
            chkHQMode.setSelected(thisSinkHLS.getStandard().equals("HQ"));
            
            textMount.setText(thisSinkHLS.getMount());
            System.out.println("SinkSettingsMount="+thisSinkHLS.getMount());
            textMount.setEnabled(true);
            lblMount.setText("M3U8 Location");
            lblMount.setEnabled(true);
            
            textURL.setText(thisSinkHLS.getURL());
            textURL.setEnabled(true);
            lblURL.setEnabled(true);
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

        jLabel12 = new javax.swing.JLabel();
        lblEncode = new javax.swing.JLabel();
        lblVideoRate = new javax.swing.JLabel();
        lblAudioRate = new javax.swing.JLabel();
        lblrtmpURL = new javax.swing.JLabel();
        lblURL = new javax.swing.JLabel();
        lblStream = new javax.swing.JLabel();
        textURL = new javax.swing.JTextField();
        textStream = new javax.swing.JTextField();
        spinVideoRate = new javax.swing.JSpinner();
        spinAudioRate = new javax.swing.JSpinner();
        btnCancel = new javax.swing.JButton();
        btnOK = new javax.swing.JButton();
        lblMount = new javax.swing.JLabel();
        lblPsw = new javax.swing.JLabel();
        lblPort = new javax.swing.JLabel();
        lblKeyInt = new javax.swing.JLabel();
        textMount = new javax.swing.JTextField();
        textPsw = new javax.swing.JPasswordField();
        spinPort = new javax.swing.JSpinner();
        spinKeyInt = new javax.swing.JSpinner();
        lblName = new javax.swing.JLabel();
        lblOW = new javax.swing.JLabel();
        lblOH = new javax.swing.JLabel();
        spinOutW = new javax.swing.JSpinner();
        spinOutH = new javax.swing.JSpinner();
        chkHQMode = new javax.swing.JCheckBox();

        jLabel12.setText("jLabel12");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Sink Settings");
        setResizable(false);

        lblEncode.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblEncode.setText("Encode:");

        lblVideoRate.setText("Video Data Rate:");

        lblAudioRate.setText("Audio Data Rate:");

        lblrtmpURL.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        lblrtmpURL.setText("Streaming Url:");

        lblURL.setText("URL:");
        lblURL.setEnabled(false);

        lblStream.setText("Stream:");
        lblStream.setEnabled(false);

        textURL.setEnabled(false);

        textStream.setEnabled(false);

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnOK.setText("OK");
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });

        lblMount.setText("Mount:");
        lblMount.setEnabled(false);

        lblPsw.setText("Password:");
        lblPsw.setEnabled(false);

        lblPort.setText("Port:");
        lblPort.setEnabled(false);

        lblKeyInt.setText("KeyInt:");
        lblKeyInt.setEnabled(false);

        textMount.setEnabled(false);

        textPsw.setEnabled(false);

        spinPort.setEnabled(false);

        spinKeyInt.setEnabled(false);

        lblName.setFont(new java.awt.Font("Ubuntu", 1, 24)); // NOI18N
        lblName.setText("SinkName");

        lblOW.setText("Out Width:");
        lblOW.setEnabled(false);

        lblOH.setText("Out Height:");
        lblOH.setEnabled(false);

        spinOutW.setEnabled(false);

        spinOutH.setEnabled(false);

        chkHQMode.setFont(new java.awt.Font("Ubuntu", 3, 15)); // NOI18N
        chkHQMode.setForeground(new java.awt.Color(180, 1, 1));
        chkHQMode.setText("HQ Mode");
        chkHQMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkHQModeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblURL)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textURL))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblStream)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textStream))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblMount)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textMount))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblPsw)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textPsw))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(lblKeyInt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinKeyInt))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(lblPort)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinPort, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblAudioRate)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinAudioRate, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblEncode)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblVideoRate)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinVideoRate, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblrtmpURL, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(lblOH)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(spinOutH, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(lblOW)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(spinOutW, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(chkHQMode, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(chkHQMode))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblrtmpURL)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblURL)
                    .addComponent(textURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblStream)
                    .addComponent(textStream, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblEncode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVideoRate)
                    .addComponent(spinVideoRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblOW)
                    .addComponent(spinOutW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAudioRate)
                    .addComponent(spinAudioRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinOutH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblOH))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMount)
                    .addComponent(textMount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPsw)
                    .addComponent(textPsw, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPort)
                    .addComponent(spinPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblKeyInt)
                    .addComponent(spinKeyInt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnOK)
                    .addComponent(btnCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    @SuppressWarnings("deprecation")
    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        if (thisSinkFile != null) {
            if (thisSinkFile.getVbitrate().equals("")) {

            } else {
                thisSinkFile.setVbitrate(Integer.toString(spinVideoRate.getValue().hashCode()));
            }
            
            if (thisSinkFile.getAbitrate().equals("")) {

            } else {
                thisSinkFile.setAbitrate(Integer.toString(spinAudioRate.getValue().hashCode()));
            }

        } else if (thisSinkUDP != null) {
            if (thisSinkUDP.getVbitrate().equals("")) {

            } else {
                thisSinkUDP.setVbitrate(Integer.toString(spinVideoRate.getValue().hashCode()));
            }
            if (thisSinkUDP.getAbitrate().equals("")) {

            } else {
                thisSinkUDP.setAbitrate(Integer.toString(spinAudioRate.getValue().hashCode()));
            }
        } else {
            if (thisSinkHLS.getVbitrate().equals("")) {

            } else {
                thisSinkHLS.setVbitrate(Integer.toString(spinVideoRate.getValue().hashCode()));
            }
            if (thisSinkHLS.getAbitrate().equals("")) {

            } else {
                thisSinkHLS.setAbitrate(Integer.toString(spinAudioRate.getValue().hashCode()));
            }
            thisSinkHLS.setMount(textMount.getText());
            thisSinkHLS.setURL(textURL.getText());
        }
        if (thisSinkFile != null) {
            Preferences filePrefs = TrucklistStudio.prefs.node("filerec");
            try {
                filePrefs.removeNode();
                filePrefs.flush();
                filePrefs = TrucklistStudio.prefs.node("filerec");
            } catch (BackingStoreException ex) {
                Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            Preferences serviceF = filePrefs.node("frecordset");
            serviceF.put("abitrate", thisSinkFile.getAbitrate());
            serviceF.put("vbitrate", thisSinkFile.getVbitrate());
        } else if (thisSinkUDP != null) {
            Preferences udpPrefs = TrucklistStudio.prefs.node("udp");
            try {
                udpPrefs.removeNode();
                udpPrefs.flush();
                udpPrefs = TrucklistStudio.prefs.node("udp");
            } catch (BackingStoreException ex) {
                Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            Preferences serviceU = udpPrefs.node("uoutset");
            serviceU.put("abitrate", thisSinkUDP.getAbitrate());
            serviceU.put("vbitrate", thisSinkUDP.getVbitrate());
            serviceU.put("standard", thisSinkUDP.getStandard());
        } else {
            Preferences hlsPrefs = TrucklistStudio.prefs.node("hls");
            try {
                hlsPrefs.removeNode();
                hlsPrefs.flush();
                hlsPrefs = TrucklistStudio.prefs.node("hls");
            } catch (BackingStoreException ex) {
                Logger.getLogger(OutputPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            Preferences serviceH = hlsPrefs.node("houtset");
            serviceH.put("abitrate", thisSinkHLS.getAbitrate());
            serviceH.put("vbitrate", thisSinkHLS.getVbitrate());
            serviceH.put("standard", thisSinkHLS.getStandard());
            serviceH.put("mount", thisSinkHLS.getMount());
            serviceH.put("url", thisSinkHLS.getURL());
        }
        if (chkHQMode.isSelected()) {
            if (thisSinkUDP != null) {
                thisSinkUDP.setStandard("HQ");
            } else {
                thisSinkHLS.setStandard("HQ");
            }
        } else {
            if (thisSinkUDP != null) {
                thisSinkUDP.setStandard("STD");
            } else {
                thisSinkHLS.setStandard("STD");
            }
        }
        this.dispose();
    }//GEN-LAST:event_btnOKActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void chkHQModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkHQModeActionPerformed
        // NOTHING HERE
    }//GEN-LAST:event_chkHQModeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    private javax.swing.JCheckBox chkHQMode;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel lblAudioRate;
    private javax.swing.JLabel lblEncode;
    private javax.swing.JLabel lblKeyInt;
    private javax.swing.JLabel lblMount;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblOH;
    private javax.swing.JLabel lblOW;
    private javax.swing.JLabel lblPort;
    private javax.swing.JLabel lblPsw;
    private javax.swing.JLabel lblStream;
    private javax.swing.JLabel lblURL;
    private javax.swing.JLabel lblVideoRate;
    private javax.swing.JLabel lblrtmpURL;
    private javax.swing.JSpinner spinAudioRate;
    private javax.swing.JSpinner spinKeyInt;
    private javax.swing.JSpinner spinOutH;
    private javax.swing.JSpinner spinOutW;
    private javax.swing.JSpinner spinPort;
    private javax.swing.JSpinner spinVideoRate;
    private javax.swing.JTextField textMount;
    private javax.swing.JPasswordField textPsw;
    private javax.swing.JTextField textStream;
    private javax.swing.JTextField textURL;
    // End of variables declaration//GEN-END:variables

    @Override
    public void sourceUpdated(Stream stream) {
//        System.out.println("New Size: "+stream.getWidth()+" x "+stream.getHeight());
        spinOutW.setValue(stream.getWidth());
        spinOutH.setValue(stream.getHeight());
        textMount.setText(stream.getMount());
        textURL.setText(stream.getURL());
    }

    @Override
    public void updatePreview(BufferedImage image) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
