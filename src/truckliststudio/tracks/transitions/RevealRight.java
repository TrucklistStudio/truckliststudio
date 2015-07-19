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
public class RevealRight extends Transition{

    public RevealRight(Stream source){
        super(source);
    }
    @Override
    protected void execute() {
        
        int newW = channel.getWidth();
        int rate = source.getRate();
        int x = source.getX();
        int frames = rate * 1;
        for (int i = 0;i<frames;i++){
            source.setWidth(i*newW/frames+1);
            source.setX(x + (newW - i*newW/frames));
            source.setOpacity(i*100/frames);
            Tools.sleep(1000/rate);
        }
        source.setWidth(newW);
    }
}
