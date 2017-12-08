/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import truckliststudio.tracks.MasterTracks;
import truckliststudio.mixers.Frame;
import truckliststudio.mixers.MasterMixer;
import truckliststudio.mixers.PreviewFrameBuilder;
import truckliststudio.sources.effects.Effect;
/**
 *
 * @author patrick (modified by karl)
 */
public abstract class Stream implements Callable<Frame>{

    public static Stream getInstance(File file) {
        Stream stream = null;
        String ext = file.getName().toLowerCase().trim();
        if (ext.endsWith(".avi")
                || ext.endsWith(".ogg")
                || ext.endsWith(".ogv")
                || ext.endsWith(".mp4")
                || ext.endsWith(".m4v")
                || ext.endsWith(".mpg")
                || ext.endsWith(".divx")
                || ext.endsWith(".wmv")
                || ext.endsWith(".flv")
                || ext.endsWith(".mov")
                || ext.endsWith(".mkv")
                || ext.endsWith(".vob")) {
            stream = new SourceMovie(file);
        } else if (ext.endsWith(".png")
                || ext.endsWith(".jpg")
                || ext.endsWith(".bmp")
                || ext.endsWith(".jpeg")) {
            stream = new SourceImage(file);
        } else if (ext.endsWith(".gif")) {
            stream = new SourceImageGif(file);
        } else if (ext.endsWith(".mp3")
                || ext.endsWith(".wav")
                || ext.endsWith(".wma")
                || ext.endsWith(".m4a")
                || ext.endsWith(".mp2")) {
            stream = new SourceMusic(file);
        }
//        } else if (ext.endsWith(".wss")){
//            stream = new SourceCustom(file);
//        }
        return stream;
    }

    private final MasterMixer mixer = MasterMixer.getInstance();
    protected String uuid = java.util.UUID.randomUUID().toString();
    protected int captureWidth = mixer.getWidth();
    protected int captureHeight = mixer.getHeight();
    protected int width = mixer.getWidth();
    protected int height = mixer.getHeight();
    protected int x = 0;
    protected int y = 0;
    protected int panelX = 0;
    protected int panelY = 0;
    protected boolean more = false;
    protected boolean sliders = false;
    protected float volume = 0.5f;
    protected int opacity = 100;
    protected int rate = mixer.getRate();
    protected int seek = 0;
    protected int zorder = 0;
    protected File file = null;
    protected String name = "Default";
    protected String url = ""; // was null
    protected String mount = "Segment Path";
    protected int audioLevelLeft = 0;
    protected int audioLevelRight = 0;
    protected ArrayList<Effect> effects = new ArrayList<>();
    protected ArrayList<SourceTrack> tracks = new ArrayList<>();
    protected String gsEffect = "";
    protected SourceTrack track = new SourceTrack();
    protected boolean hasVideo=true;
    protected boolean hasAudio=true;
    protected boolean hasFakeVideo=false;
    protected boolean hasFakeAudio=false;
    protected boolean needSeekCTRL=true;
    protected boolean loaded=false;
    protected int ADelay = 0;
    protected int VDelay = 0;
    protected String abitrate = "128";
    protected String vbitrate = "1200";
    protected int color = 0;
    protected boolean isATimer = false;
    protected boolean isACDown = false;
    protected String isIntSrc = "false";
    protected boolean isPlayList = false;
    protected boolean isQRCode = false;
    protected String comm = "AV";
    protected String content = "";
    protected String fontName = "";
    protected boolean isOAudio = false;
    protected boolean isOVideo = false;
    protected boolean loop = false;
    protected String trkName = "NoTrack";
    protected Frame nextFrame = null;
    Listener listener = null;
    protected String panelType = "Panel";
    protected String streamTime = "N/A";
    protected int duration = 0;
    protected String audioSource = "";
    protected String guid = "";
    protected boolean preView = false;
    protected boolean isPaused = false;
    protected boolean isATrack = false;
    protected boolean isATitle = false;

    protected Stream() {
        MasterTracks.getInstance().register(this);
    }
    
    public ArrayList<Effect> getAllEffects() {
        return effects;
    }

    public boolean getisPaused() {
        return isPaused;
    }
    
    public void setisPaused(boolean isP) {
        this.isPaused = isP;
    }
    
    public boolean getisATrack() {
        return isATrack;
    }
    
    public void setisATrack(boolean isAT) {
        this.isATrack = isAT;
    }
    
    public boolean getisATitle() {
        return isATitle;
    }
    
    public void setisATitle(boolean isAT) {
        this.isATitle = isAT;
    }
    
    public boolean getIsATimer() {
        return isATimer;
    }
    
    public void setIsATimer(boolean tTimer) {
        this.isATimer = tTimer;
    }
    
    public boolean getIsACDown() {
        return isACDown;
    }
    
    public void setIsACDown(boolean cDown) {
        this.isACDown = cDown;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int dur) {
        this.duration = dur;
    }
    
    public void setPlayList (boolean b) {
        isPlayList = b;
    }
    
    public boolean getPlayList() {
        return isPlayList;
    }
    
    public boolean getIsQRCode() {
        return isQRCode;
    }
    
    public void setIsQRCode(boolean tQRCode) {
        this.isQRCode = tQRCode;
    }

    public void setOnlyVideo(boolean setOVideo) {
        isOVideo = setOVideo;
    }
    
    public boolean isOnlyVideo() {
        return isOVideo;
    }
    
    public void setOnlyAudio(boolean setOAudio) {
        isOAudio = setOAudio;
    }
    
    public boolean isOnlyAudio() {
        return isOAudio;
    }
    
    public void setPreView(boolean setPre) {
        preView = setPre;
    }
    
    public boolean getPreView() {
        return preView;
    }

    public boolean getLoaded() {
        return loaded;
    }
    
    public void setLoaded(boolean sLoaded) {
        this.loaded = sLoaded;
    }
    
    public String getTrkName() {
        return trkName;
    }
    
    public void setTrkName(String chName) {
        this.trkName = chName;
    }

    public void setLoop (boolean sLoop) {
        loop = sLoop;
    }
    
    public boolean getLoop () {
        return loop;
    }

    public void setListener(Listener l) {
        listener = l;
    }

    public void updateStatus() {
        if (listener != null) {
            listener.sourceUpdated(this);
        }
    }
    public void updatePreview(){
        if (listener != null) {
            listener.updatePreview(this.getPreview());
        }
    }
    public void destroy() {
        stop();
        MasterTracks.getInstance().unregister(this);
    }
    public void updateContent(String content) {
    }
    
    public void updateLineContent(String content) {
    }
    
    public void updatePNG() {
    }
    
    public String getContent() {
        return content;
    }
    
    public String setContent(String con) {
        return content = con;
    }
    
    public void setFont(String f) {
//        fontName = f;
//        updateContent(content);
    }

    public String getFont() {
        return fontName;
    }
    
    public void setColor(int c) {
//        color = c;
//        updateContent(content);
    }
    
    public int getColor() {
        return color;
    }
    
    public abstract void read();

    public abstract void stop();
    
    public abstract void pause();
    
    public abstract void play();

    public abstract boolean isPlaying();
 
    public abstract BufferedImage getPreview();
    
    public abstract void readNext();

    public boolean hasAudio(){
        return hasAudio;
    }

    public void unRegister() {
            PreviewFrameBuilder.unregister(this);
    }
    
    public void register() {
            PreviewFrameBuilder.register(this);
    }
    
    public void setIsPlaying(boolean setIsPlaying) {
    }

    public void setHasAudio(boolean setHasAudio) {
        hasAudio = setHasAudio;
    }
    
    public void setHasVideo(boolean setHasVideo) {
        hasVideo = setHasVideo;
    }
    
    public boolean hasVideo(){
        return hasVideo;
    }
    
    public void setMore(boolean setMo) {
        more = setMo;
    }
    
    public boolean getMore(){
        return more;
    }
    
    public void setSliders(boolean setSl) {
        sliders = setSl;
    }
    
    public boolean getSliders(){
        return sliders;
    }
    
    public void setPanelType(String sPanelType) {
        panelType = sPanelType;
    }
    
    public String getPanelType(){
        return panelType;
    }
    
    public void setStreamTime(String sStreamTime) {
        streamTime = sStreamTime;
    }
    
    public String getStreamTime(){
        return streamTime;
    }
    
    public void setAudioSource(String sAS) {
        audioSource = sAS;
    }
    
    public String getAudioSource(){
        return audioSource;
    }
    
    public void setGuid(String sGid) {
        guid = sGid;
    }
    
    public String getGuid(){
        return guid;
    }
    
    public boolean hasFakeVideo(){
        return hasFakeVideo;
    }
    
    public boolean hasFakeAudio(){
        return hasFakeAudio;
    }

    public void setVideo(boolean hasIt){
        hasVideo=hasIt;
    }
    
    public void setFakeVideo(boolean hasIt) {
        hasFakeVideo=hasIt;
    }
    
    public void setFakeAudio(boolean hasIt) {
        hasFakeAudio=hasIt;
    }
    
    public void setAudio(boolean hasIt){
        hasAudio = hasIt;
    }
    
    public int getAudioLevelLeft() {
        return audioLevelLeft;
    }

    public int getAudioLevelRight() {
        return audioLevelRight;
    }

    public void addTrack(SourceTrack sc) {
        tracks.add(sc);
    }

    public void removeTrack(SourceTrack sc) {
        tracks.remove(sc);
//        System.out.println("TotalStreamTracks="+tracks);
    }
    
    public void addTrackAt(SourceTrack sc, int y) {
        tracks.set(y, sc);
    }

    public void removeTrackAt(int y) {
        tracks.set(y,null);
    }

    public void selectChannel(String name) {
        for (SourceTrack sc : tracks) {
            if (sc.getName().equals(name)) {
                sc.apply(this);
                break;
            }
        }
    }

    public ArrayList<SourceTrack> getTracks() {
        return tracks;
    }
    
    public SourceTrack getChannel() {
        return track;
    }

    public void setName(String n) {
        name = n;
    }

    public ArrayList<Effect> getEffects() {
        return effects;
    }

    public synchronized void setEffects(ArrayList<Effect> list) {
        effects = list;
    }
    
    public String getGSEffect() {
        return gsEffect;
    }

    public synchronized void setGSEffect(String gsFx) {
        gsEffect = gsFx;
    }
    
    public synchronized void addEffect(Effect e) {
        effects.add(e);
    }

    public synchronized void removeEffect(Effect e) {
        effects.remove(e);
        e.clearEffect(e);
    }
    
    public synchronized void applyEffects(BufferedImage img) {
        ArrayList<Effect> temp = new ArrayList<>();
        temp.addAll(effects);
        for (Effect e : temp) {
            e.applyEffect(img);
        }
    }

    protected void setAudioLevel(Frame f) {
        if (f != null) {
            byte[] data = f.getAudioData();
            if (data != null) {
                audioLevelLeft = 0;
                audioLevelRight = 0;
                int tempValue = 0;
                for (int i = 0; i < data.length; i += 4) {
                    tempValue = (data[i] << 8 & (data[i + 1])) / 256;
                    if (tempValue < 0) {
                        tempValue *= -1;
                    }
                    if (audioLevelLeft < tempValue) {
                        audioLevelLeft = tempValue;
                    }
                    tempValue = (data[i + 2] << 8 & (data[i + 3])) / 256;

                    if (tempValue < 0) {
                        tempValue *= -1;
                    }
                    if (audioLevelRight < tempValue) {
                        audioLevelRight = tempValue;
                    }
                }
                audioLevelLeft = (int) (audioLevelLeft * volume);
                audioLevelRight = (int) (audioLevelRight * volume);
            }
        }
    }

    public String getURL() {
        return url;
    }

    public String getName() {
        return name;
    }

    public Frame getFrame() {
        return nextFrame;
    }

    public String getID() {
        return uuid;
    }

    public File getFile() {
        return file;
    }
    
    public void setFile(File f) {
        file = f;
    }
    
    public void setZOrder(int z) {
        zorder = z;
    }

    public int getZOrder() {
        return zorder;
    }

    /**
     * @return the captureWidth
     */
    public int getCaptureWidth() {
        return captureWidth;
    }

    /**
     * @param captureWidth the captureWidth to set
     */
    public void setCaptureWidth(int captureWidth) {
        this.captureWidth = captureWidth;
    }

    /**
     * @return the captureHeight
     */
    public int getCaptureHeight() {
        return captureHeight;
    }

    /**
     * @param captureHeight the captureHeight to set
     */
    public void setCaptureHeight(int captureHeight) {
        this.captureHeight = captureHeight;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
//        System.out.println("Set " + this.getName() + " To = " + this.height);
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    public int getPanelY() {
        return panelY;
    }

    /**
     * @param y the y to set
     */
    public void setPanelY(int pY) {
        this.panelY = pY;
    }
    
    public int getPanelX() {
        return panelX;
    }

    /**
     * @param x the x to set
     */
    public void setPanelX(int pX) {
        this.panelX = pX;
    }
    
    /**
     * @return the volume
     */
    public float getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(float volume) {
        this.volume = volume;
    }

    /**
     * @return the opacity
     */
    public int getOpacity() {
        return opacity;
    }

    /**
     * @param opacity the opacity to set
     */
    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    /**
     * @return the rate
     */
    public int getRate() {
        return rate;
    }

    /**
     * @param rate the rate to set
     */
    public void setRate(int rate) {
        this.rate = rate;
    }

    /**
     * @return the seek
     */
    public int getSeek() {
        return seek;
    }

    /**
     * @param seek the seek to set
     */
    public void setSeek(int seek) {
        this.seek = seek;
    }
    
    public void setVDelay (int VDealy) {
        this.VDelay = VDealy;
    }
    
    public void setADelay (int ADealy) {
        this.ADelay = ADealy;
    }
    
    public int getVDelay () {
        return VDelay;
    }
    
    public int getADelay () {
        return ADelay;
    }
    
    public String getAbitrate() {
        return abitrate;
    }
    
    public void setAbitrate(String sAbitRate) {
        abitrate = sAbitRate;
    }
    
    public String getMount() {
        return mount;
    }
    
    public void setMount(String sMount) {
        mount = sMount;
    }
    
    public void setURL(String sUrl) {
        this.url = sUrl;
    }
    
    public String getVbitrate() {
        return vbitrate;
    }
    
    public void setVbitrate(String sVbitRate) {
        vbitrate = sVbitRate;
    }
    public String getIsIntSrc() {
        return isIntSrc;
    }
    
    public void setIntSrc(String intSrc) {
        this.isIntSrc = intSrc;
    }
    
    public void startItsTrack(String name) {
        
    };
    @Override
    public Frame call() throws Exception {
        readNext();
        updatePreview();
        return nextFrame;
    }

    public interface chListener {

        public void loadingPostOP();
    }

    public interface Listener {

        public void sourceUpdated(Stream stream);

        public void updatePreview(BufferedImage image);
    }
}
