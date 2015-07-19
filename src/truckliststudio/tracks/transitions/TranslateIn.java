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
public class TranslateIn extends Transition{
    public TranslateIn(Stream source){
        super(source);
    }
    @Override
    protected void execute() {
        int oldX = -source.getCaptureWidth();
        int oldY = -source.getCaptureHeight();
        int newX = source.getX();
        int newY = source.getY();
        int deltaX = newX - oldX;
        int deltaY = newY - oldY;
        int rate = source.getRate();
        int totalFrames = rate * 1;
        for (int i = 0; i<totalFrames;i++){
            source.setX(oldX + i*deltaX/totalFrames);
            source.setY(oldY + i*deltaY/totalFrames);
            source.setOpacity(i*100/totalFrames);
            Tools.sleep(1000/rate);
        }
        source.setX(oldX);
        source.setY(oldY);
        
    }
    
}
