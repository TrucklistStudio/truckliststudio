/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.components;

import java.awt.Component;
import java.util.ArrayList;
import truckliststudio.streams.SourceImage;
import truckliststudio.streams.SourceImageGif;
import truckliststudio.streams.SourceText;
import truckliststudio.streams.Stream;

/**
 *
 * @author patrick (modified by karl)
 */
public class SourceControls {

    public static ArrayList<Component> getControls(Stream source) {
        ArrayList<Component> comps = new ArrayList<>();
        Component c = null;
        Component d = null;
        c = new SourceControlTransitions(source);
        comps.add(c);
        d = new SourceControlTracks(source);
        d.setName("Track Opt");
        comps.add(d);
        if (source instanceof SourceText) {
            c = new SourceControlsText((SourceText) source);
            comps.add(c);
            c = new SourceControlEffects(source);
            c.setName("FX");
            comps.add(c);     
        } else if (source instanceof SourceImage) {
            c = new SourceControlEffects(source);
            c.setName("FX");
            comps.add(c);
        } else if (source instanceof SourceImageGif) {
            
        } else {
            c = new SourceControlEffects(source);
            c.setName("FX");
            comps.add(c);
            c = new SourceControlGSEffects(source);
            c.setName("GS FX");
            comps.add(c);
        } 
        return comps;
    }

    private SourceControls() {
    }
}
