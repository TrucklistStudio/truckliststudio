/**
 *  truckliststudio for GNU/Linux
 *  Copyright (C) 2008  Patrick Balleux
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * 
 */

package truckliststudio.components;

/**
 *
 * @author  pballeux
 */
public class ColorChooser extends javax.swing.JDialog {
    
    /** Creates new form ColorChooser
     * @param parent
     * @param modal */
    public ColorChooser(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jColorChooser1 = new javax.swing.JColorChooser();
        panButtons = new javax.swing.JPanel();
        btnApprove = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().add(jColorChooser1, java.awt.BorderLayout.CENTER);

        panButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnApprove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/document-save.png"))); // NOI18N
        btnApprove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApproveActionPerformed(evt);
            }
        });
        panButtons.add(btnApprove);

        btnCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truckliststudio/resources/tango/process-stop.png"))); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        panButtons.add(btnCancel);

        getContentPane().add(panButtons, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        color=null;
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnApproveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApproveActionPerformed
        color = jColorChooser1.getColor();
        dispose();
    }//GEN-LAST:event_btnApproveActionPerformed

    public java.awt.Color getColor(){
        return color;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                ColorChooser dialog = new ColorChooser(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnApprove;
    private javax.swing.JButton btnCancel;
    private javax.swing.JColorChooser jColorChooser1;
    private javax.swing.JPanel panButtons;
    // End of variables declaration//GEN-END:variables
    private java.awt.Color color = null;
}
