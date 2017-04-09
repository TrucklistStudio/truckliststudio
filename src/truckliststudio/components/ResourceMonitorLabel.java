/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.components;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import static truckliststudio.TrucklistStudio.theme;

/**
 *
 * @author patrick
 */
public class ResourceMonitorLabel extends JLabel {

    long endTime = 0;

    public ResourceMonitorLabel(long endTime, String text) {
        this.endTime = endTime;
        int fontSize = getFont().getSize();
        Font font = new Font(getFont().getName(), Font.BOLD, fontSize);
        setFont(font);
        setText(text);
        if (theme.equals("Dark")) {
            setForeground(Color.yellow);
        } else {
            setForeground(Color.red.darker());
        }
    }

    public long getEndTime() {
        return endTime;
    }
}
