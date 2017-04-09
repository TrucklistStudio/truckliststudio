/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.externals;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import truckliststudio.mixers.MasterMixer;

/**
 *
 * @author patrick (modified by karl)
 */
public class FME {

    private String url = "";
    private String name = "";
    private String abitrate = "";
    private String vbitrate = "";
    private String vcodec = "";
    private String acodec = "";
    private String width = "";
    private String height = "";
    private String stream = "";
    private String mount = "";
    private String password = "";
    private String port = "";
    private String keyInt = "";
    private final MasterMixer mixer = MasterMixer.getInstance();
    private String standard = "STD";
    
    public FME(File xml) {
        try{
            parse(xml);
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
            Logger.getLogger(FME.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public FME(String url,String stream, String name, String abitrate,String vbitrate, String vcodec, String acodec, String width, String height, String mount, String password, String port, String keyint){
        this.name = name;
        this.width = width;
        this.height = height;
        this.vcodec = vcodec;
        this.vbitrate = vbitrate;
        this.acodec = acodec;
        this.abitrate = abitrate;
        this.url = url;
        this.stream = stream;
        this.mount = mount;
        this.password = password;
        this.port = port;
        this.keyInt = keyint;   
    }
    
    public FME(){
        
    }
 
    private void parse(File xml) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        XPath path = XPathFactory.newInstance().newXPath();
        String root = "/flashmediaencoder_profile";
        
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
        name = (String)path.evaluate(root + "/preset/name", doc,XPathConstants.STRING);
        if (name==null || name.length()==0){
            root = "/flashmedialiveencoder_profile";
            name = (String)path.evaluate(root + "/preset/name", doc,XPathConstants.STRING);
        }
        width = (String)path.evaluate(root + "/capture/video/size/width", doc,XPathConstants.STRING);
        height = (String)path.evaluate(root + "/capture/video/size/height", doc,XPathConstants.STRING);
        vcodec = (String)path.evaluate(root + "/encode/video/format", doc,XPathConstants.STRING);
        vbitrate = (String)path.evaluate(root + "/encode/video/datarate", doc,XPathConstants.STRING);
        if (vbitrate.indexOf(';')!=-1){
            vbitrate = vbitrate.substring(0, vbitrate.indexOf(';'));
        }
        acodec = (String)path.evaluate(root + "/encode/audio/format", doc,XPathConstants.STRING);
        abitrate = (String)path.evaluate(root + "/encode/audio/datarate", doc,XPathConstants.STRING);
        url = (String)path.evaluate(root + "/output/rtmp/url", doc,XPathConstants.STRING);
        stream = (String)path.evaluate(root + "/output/rtmp/stream", doc,XPathConstants.STRING);
        mount = (String)path.evaluate(root + "/output/rtmp/mount", doc,XPathConstants.STRING);
        password = (String)path.evaluate(root + "/output/rtmp/password", doc,XPathConstants.STRING);
        port = (String)path.evaluate(root + "/output/rtmp/port", doc,XPathConstants.STRING);
        
        String keyI = (String)path.evaluate(root + "/encode/video/advanced/keyframe_frequency", doc,XPathConstants.STRING);
        if (!"".equals(keyI)) {
            String[] kInt = keyI.split(" ");
            String g = kInt[0].replaceAll(" ", "");
            int sec = Integer.parseInt(g);
            int kInteger = sec*mixer.getRate();
            keyInt = Integer.toString(kInteger);
        } else {
            int kInteger = 5*mixer.getRate();
            keyInt = Integer.toString(kInteger);
        }
//        System.out.println("Pharsed KeyInt: "+keyInt+"###");
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String sUrl) {
        this.url = sUrl;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    public void setName(String n) {
        name = n;
    }

    public void setStandard(String gStandard) {
        standard = gStandard;
    }
    
    public String getStandard() {
        return standard;
    }
    
    /**
     * @return the abitrate
     */
    public String getAbitrate() {
        return abitrate;
    }
    
    public void setAbitrate(String sAbitRate) {
        abitrate = sAbitRate;
    }
    
    /**
     * @return the vbitrate
     */
    public String getVbitrate() {
        return vbitrate;
    }
    
    public void setVbitrate(String sVbitRate) {
        vbitrate = sVbitRate;
    }
    
    /**
     * @return the vcodec
     */
    public String getVcodec() {
        return vcodec;
    }

    /**
     * @return the acodec
     */
    public String getAcodec() {
        return acodec;
    }

    /**
     * @return the width
     */
    public String getWidth() {
        return width;
    }

    public void setWidth(String sW) {
        width = sW;
    }
    
    /**
     * @return the height
     */
    public String getHeight() {
        return height;
    }

    public void setHeight(String sH) {
        height = sH;
    }
    
    /**
     * @return the stream
     */
    public String getStream() {
        return stream;
    }
    
    public void setStream(String sStr) {
        stream = sStr;
    }
    
    /**
     * @return
     */
    public String getMount() {
        return mount;
    }
    
    public void setMount(String sMount) {
        mount = sMount;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String sPsw) {
        password = sPsw;
    }
    
    public String getPort() {
        return port;
    }
    
    public void setPort(String sPort) {
        port = sPort;
    }
    
    public String getKeyInt() {
        return keyInt;
    }
    
    public void setKeyInt(String sKeyI) {
        keyInt = sKeyI;
    }
}
