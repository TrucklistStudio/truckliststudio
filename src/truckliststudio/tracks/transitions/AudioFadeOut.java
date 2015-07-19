/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.tracks.transitions;

import truckliststudio.streams.Stream;
import truckliststudio.util.Tools;

/**
 *
 * @author pballeux
 */
public class AudioFadeOut extends Transition {
    protected AudioFadeOut(Stream source){
        super(source);
    }
    @Override
    public void execute() {
        int rate = source.getRate();
        float volume = source.getVolume();
        for (int i = 0; i < rate; i++) {
                source.setVolume(volume- (i * volume)/rate);
                Tools.sleep(1000/rate);
        }
        source.setVolume(volume);
    }
}
