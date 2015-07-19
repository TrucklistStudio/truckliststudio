/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.tracks.transitions;

import truckliststudio.streams.Stream;
import truckliststudio.util.Tools;

/**
 *
 * @author patrick
 */
public class HideRight extends Transition{

    public HideRight(Stream source){
        super(source);
    }
    @Override
    protected void execute() {
        
        int newW = channel.getWidth();
        int rate = source.getRate();
        int x = source.getX();
        int frames = rate * 1;
        for (int i = 0;i<frames;i++){
            source.setWidth(newW - i*newW/frames);
            source.setX(x + i*newW/frames);
            source.setOpacity(100-i*100/frames);
            Tools.sleep(1000/rate);
        }
        source.setWidth(newW);
        source.setX(x);
    }
    
}
