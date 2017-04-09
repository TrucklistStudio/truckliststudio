/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.tracks;

import java.util.ArrayList;
import truckliststudio.streams.SourceMovie;
import truckliststudio.streams.SourceTrack;
import truckliststudio.streams.Stream;
import truckliststudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class MasterTracks {
    
    static MasterTracks instance = null;
    
    public static MasterTracks getInstance() {
        if (instance == null) {
            instance = new MasterTracks();
        }
        return instance;
    }
    ArrayList<String> trackNames = new ArrayList<>();
    ArrayList<Stream> streams = new ArrayList<>();
    int rmAddIndex = 0;
    ArrayList<SourceTrack> tempSC = null;
    
    private MasterTracks() {
    }
    
    public void register(Stream s) {
        String streamName = s.getClass().getName();
        streamName = streamName.replace("truckliststudio.streams.", "");
        if (!s.getClass().toString().contains("Sink")) {
            System.out.println(streamName + " registered.");
        }
        streams.add(s);
    }
    
    public void unregister(Stream s) {
        if (!s.getClass().toString().contains("Sink")) {
//            System.out.println(s.getName() + " unregistered.");
        }
        streams.remove(s);
    }
    
    public void addTrack(String name) {
        trackNames.add(name);
        for (Stream s : streams) {
            s.addTrack(SourceTrack.getTrack(name, s));
        }
    }
    
    public void addTrack2List(String name) {
        trackNames.add(name);
    }
    
    public void addPlayTrack(String name, String playTrk) {
        trackNames.add(name);
        for (Stream s : streams) {
            if (playTrk.contains(s.getTrkName())) {
                s.setIsPlaying(false);
                s.addTrack(SourceTrack.getTrack(name, s));
                s.setIsPlaying(true);
            } else {
                s.addTrack(SourceTrack.getTrack(name, s));
            }
        }
    }
    
    public static SourceTrack getTrack(String trackName, Stream s) {
        SourceTrack track = null;
        for (SourceTrack tr : s.getTracks()) {
            if (tr.getName().equals(trackName)) {
                track = tr;
            }
        }
        return track;
    }
    
    public void addToTracks(String name) {
        trackNames.add(name);
        
    }
    
    public void addTrackAt(String name, int index) {
        trackNames.add(index, name);
        
    }
    
    public void updateTrack(String name) {
        for (Stream s : streams) {
            String streamName = s.getClass().getName();
            SourceTrack sc = null;
            ArrayList<SourceTrack> sourceCh = s.getTracks();
            int x = 0;
            for (int i = 0; i < sourceCh.size(); i++) {
                if (sourceCh.get(i).getName().equals(name)) {
                    sc = sourceCh.get(i);
                    x = i;
                    break;
                }
            }
            if (!streamName.contains("Sink")) {
                if (sc != null) {
                    s.removeTrackAt(x);
                }
                s.addTrackAt(SourceTrack.getTrack(name, s), x);
                x = 0;
            }
        }
    }
    
    public void updateTrackBtn(String name) {
        for (Stream st : streams) {
            if (st.getisATrack()) {
                Stream upStream = null;
                for (Stream s : streams) {
                    if (s.getName().equals(name) || (name.contains(s.getName()) && name.contains("(") && name.endsWith(")"))) {
                        upStream = s;
//                        System.out.println("Up Stream=" + s.getName());
                    }
                }
                
                ArrayList<SourceTrack> sourceCh = upStream.getTracks();
                int i = 0;
                for (SourceTrack sTrk : sourceCh) {
                    sTrk.setX(upStream.getX());
                    sTrk.setY(upStream.getY());
                    sTrk.setWidth(upStream.getWidth());
                    sTrk.setHeight(upStream.getHeight());
                    sTrk.setOpacity(upStream.getOpacity());
                    sTrk.addAllEffects(upStream.getAllEffects());
                    sTrk.setVolume(upStream.getVolume());
                    sTrk.setZOrder(upStream.getZOrder());
                    if (upStream.getName().equals(sTrk.getName())) {
                        sTrk.setIsPlaying(upStream.isPlaying());
                    }
                    sTrk.setIsPaused(upStream.getisPaused());
                    sTrk.setCapHeight(upStream.getCaptureHeight());
                    sTrk.setCapWidth(upStream.getCaptureWidth());
                    
                    upStream.removeTrackAt(i);
                    
                    upStream.addTrackAt(sTrk, i);
                    i++;
                }
            } else {
                
                String streamName = st.getClass().getName();
                SourceTrack sc = null;
                ArrayList<SourceTrack> sourceCh = st.getTracks();
                int x = 0;
                for (int i = 0; i < sourceCh.size(); i++) {
                    if (sourceCh.get(i).getName().equals(name)) {
                        sc = sourceCh.get(i);
                        x = i;
                        break;
                    }
                }
                if (!streamName.contains("Sink")) {
                    if (sc != null) {
                        st.removeTrackAt(x);
                    }
                    st.addTrackAt(SourceTrack.getTrack(name, st), x);
                    x = 0;
                }
            }
        }
    }
    
    public void insertStudio(String name) {
        for (Stream s : streams) {
            int co = 0;
            for (SourceTrack ssc : s.getTracks()) {
                if (ssc.getName().equals(name)) {
                    co++;
                }
            }
            if (co == 0) {
                if (s.getisATrack()) {
                    boolean backState = false;
                    if (s.isPlaying()) {
                        s.setIsPlaying(false);
                        backState = true;
                    }
                    s.addTrack(SourceTrack.getTrack(name, s));
                    if (backState) {
                        s.setIsPlaying(true);
                    }
                } else {
                    boolean backState = false;
                    if (s.isPlaying()) {
                        backState = true;
                    }
                    s.addTrack(SourceTrack.getTrack(name, s));
                    if (backState) {
                        s.setIsPlaying(true);
                    } else {
                        s.setIsPlaying(false);
                    }
                }
            } else {
                ArrayList<String> allChan = new ArrayList<>();
                for (String scn : MasterTracks.getInstance().getTracks()) {
                    allChan.add(scn);
                }
                for (SourceTrack scc3 : s.getTracks()) {
                    String removech = scc3.getName();
                    allChan.remove(removech);
                }
                for (String ssc2 : allChan) {
                    s.addTrack(SourceTrack.getTrack(ssc2, s));
                }
            }
        }
    }
    
    public void removeTrack(String name) {
        trackNames.remove(name);
        for (Stream s : streams) {
            SourceTrack toRemove = null;
            for (SourceTrack sc : s.getTracks()) {
                if (sc.getName().equals(name)) {
                    toRemove = sc;
                }
            }
            if (toRemove != null) {
                s.removeTrack(toRemove);
            }
        }
    }
    
    public void removeTrackAt(String name) {
        trackNames.remove(name);
        
    }
    
    public void removeTrackIndex(int index) {
        trackNames.remove(index);
    }
    
    public void selectTrack(String name) {
        for (Stream stream : streams) {
            for (SourceTrack sc : stream.getTracks()) {
                if (sc.getName().equals(name)) {
                    sc.apply(stream);
                    break;
                }
            }
        }
    }
    
    public ArrayList<String> getTracks() {
        return trackNames;
    }
    
    public void stopAllStream() {
        for (Stream s : streams) {
            if (s.isPlaying()) {
                if (s.getLoop()) {
                    s.setLoop(false);
                    Tools.sleep(30);
                    s.stop();
                    s.setLoop(true);
                } else {
                    Tools.sleep(30);
                    s.stop();
                }
            }
        }
    }
    
    public void endAllStream() {
        for (Stream s : streams) {
            if (s.isPlaying()) {
                if (s.getLoop()) {
                    s.setLoop(false);
                    Tools.sleep(30);
                    s.stop();
                } else {
                    Tools.sleep(30);
                    s.stop();
                }
            }
        }
    }
    
    public void stopTextCDown() {
        for (Stream s : streams) {
            String streamName = s.getClass().getName();
            if (!streamName.contains("Sink")) {
                if (streamName.endsWith("SourceText")) {
                    if (s.getIsACDown()) {
                        s.stop();
                        s.updateStatus();
                    }
                }
            }
        }
    }
    
    public void stopOnlyStream() {
        for (Stream s : streams) {
            String streamName = s.getClass().getName();
            if (!streamName.contains("Sink")) {
                if (s.isPlaying()) {
                    if (s.getLoop()) {
                        s.setLoop(false);
                        Tools.sleep(30);
                        s.stop();
                        s.setLoop(true);
                    } else {
                        Tools.sleep(30);
                        s.stop();
                    }
                }
            }
        }
    }
    
    public ArrayList<Stream> getStreams() {
        return streams;
    }
}
