/*
 * Copyright (C) 2014 patrick
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package truckliststudio.util;

import java.io.IOException;

/**
 *
 * @author patrick
 */
public class BackEnd {

    public static boolean avconvDetected(){
        boolean retValue = false;
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("avconv");
            p.waitFor();
//            System.out.println(p.exitValue());
            retValue = p.exitValue() == 1;
        } catch (IOException | InterruptedException ex) {
//            System.err.println(ex.getMessage());
        } finally {
            if (p != null){
                p.destroy();
                p=null;
            }
        }
        return retValue;
    }
    
    public static boolean ffmpegDetected(){
        boolean retValue = false;
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("ffmpeg");
            p.waitFor();
//            System.out.println(p.exitValue());
            retValue = p.exitValue() == 1;
        } catch (IOException | InterruptedException ex) {
//            System.err.println(ex.getMessage());
        } finally {
            if (p != null){
                p.destroy();
                p=null;
            }
        }
        return retValue;
    }

}
