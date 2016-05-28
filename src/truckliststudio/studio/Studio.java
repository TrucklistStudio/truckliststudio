/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.studio;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import truckliststudio.TrucklistStudio;
import truckliststudio.tracks.MasterTracks;
import truckliststudio.tracks.transitions.Transition;
import truckliststudio.mixers.MasterMixer;
import truckliststudio.sources.effects.Blink;
import truckliststudio.sources.effects.ChromaKey;
import truckliststudio.sources.effects.Contrast;
import truckliststudio.sources.effects.Crop;
import truckliststudio.sources.effects.Effect;
import truckliststudio.sources.effects.FlipHorizontal;
import truckliststudio.sources.effects.FlipVertical;
import truckliststudio.sources.effects.Gain;
import truckliststudio.sources.effects.Gray;
import truckliststudio.sources.effects.HSB;
import truckliststudio.sources.effects.Mirror1;
import truckliststudio.sources.effects.Mirror2;
import truckliststudio.sources.effects.Mirror3;
import truckliststudio.sources.effects.Mirror4;
import truckliststudio.sources.effects.Mosaic;
import truckliststudio.sources.effects.Opacity;
import truckliststudio.sources.effects.Perspective;
import truckliststudio.sources.effects.RGB;
import truckliststudio.sources.effects.RevealLeftNFade;
import truckliststudio.sources.effects.RevealRightNFade;
import truckliststudio.sources.effects.Rotation;
import truckliststudio.sources.effects.Shapes;
import truckliststudio.sources.effects.Sharpen;
import truckliststudio.sources.effects.Stretch;
import truckliststudio.sources.effects.SwapRedBlue;
import truckliststudio.streams.SourceTrack;
import truckliststudio.streams.SourceImageGif;
import truckliststudio.streams.SourceMovie;
import truckliststudio.streams.SourceMusic;
import truckliststudio.streams.SourceText;
import static truckliststudio.streams.SourceText.Shape.NONE;
import static truckliststudio.streams.SourceText.Shape.OVAL;
import static truckliststudio.streams.SourceText.Shape.RECTANGLE;
import static truckliststudio.streams.SourceText.Shape.ROUNDRECT;
import truckliststudio.streams.Stream;

/**
 *
 * @author patrick (modified by karl)
 */
public class Studio {

    private static final String ELEMENT_SOURCES = "Sources";
    private static final String ELEMENT_TRACKS = "Tracks";
    private static final String ELEMENT_EFFECTS = "Effects";
    private static final String ELEMENT_SOURCE = "Source";
    private static final String ELEMENT_TRACK = "Track";
    private static final String ELEMENT_ROOT = "TrucklistStudio";
    private static final String ELEMENT_MIXER = "Mixer";
    public static ArrayList<Stream> extstream = new ArrayList<>();
    public static ArrayList<SourceTrack> trackLoad = new ArrayList<>();
    public static File filename;
    public static String shapeImg = null;
    public static ArrayList<SourceText> LText = new ArrayList<>();
    public static ArrayList<String> ImgMovMus = new ArrayList<>();
    public static ArrayList<String> aGifKeys = new ArrayList<>();
    static boolean FirstChannel=false;
    static Listener listener = null;
    public static void setListener(Studio.Listener l) {
        listener = l;
    }

    public static void load(File file, String loadType) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Studio studio = new Studio();
        System.out.println("Loading Studio ...");
        filename = file; 
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        XPath path = XPathFactory.newInstance().newXPath();

        //Loading Tracks
        NodeList nodeTracks = (NodeList) path.evaluate("/TrucklistStudio/Tracks/Track", doc.getDocumentElement(), XPathConstants.NODESET);
        for (int i = 0; i < nodeTracks.getLength(); i++) {
            Node channel = nodeTracks.item(i);
            String name = channel.getAttributes().getNamedItem("name").getTextContent();
            String Sduration = channel.getAttributes().getNamedItem("duration").getTextContent();
            int duration = Integer.parseInt(Sduration);
            studio.tracks.add(name);
//            System.out.println("StudioTrack="+name);
            studio.Durations.add(duration);
            System.out.println("StudioTrack: " + name + " - Duration: " + duration);  
        }
        
        // Loading mixer settings
        if (loadType.equals("load")) {
            Node nodeMixer = (Node) path.evaluate("/TrucklistStudio/Mixer", doc.getDocumentElement(), XPathConstants.NODE);
            String width = nodeMixer.getAttributes().getNamedItem("width").getTextContent();
            int widthInt = Integer.parseInt(width);
            MasterMixer.getInstance().setWidth(widthInt);
            String height = nodeMixer.getAttributes().getNamedItem("height").getTextContent();
            int heightInt = Integer.parseInt(height);
            MasterMixer.getInstance().setHeight(heightInt);
            String rate = nodeMixer.getAttributes().getNamedItem("rate").getTextContent();
            int rateInt = Integer.parseInt(rate);
            MasterMixer.getInstance().setRate(rateInt);
            System.out.println("Setting Mixer to: " + width + "X" + height + "@" + rate + "fps");
        }
    }
    
    public static void save(File file) throws IOException, XMLStreamException, TransformerConfigurationException, TransformerException, IllegalArgumentException, IllegalAccessException {
        ArrayList<String> channels = MasterTracks.getInstance().getTracks();
        ArrayList<Stream> streams = MasterTracks.getInstance().getStreams();
        ArrayList<Integer> Durations = listener.getCHTimers();
        StringWriter writer = new StringWriter();
        System.out.println("Saving Studio ...");

        XMLStreamWriter xml = javax.xml.stream.XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
        xml.writeStartDocument();
        xml.writeStartElement(ELEMENT_ROOT);
        // Save Tracks
        xml.writeStartElement(ELEMENT_TRACKS);

        for (String c : channels) {
            int index = channels.indexOf(c);
            xml.writeStartElement(ELEMENT_TRACK);
            System.out.println("Saving Track: "+c);
            xml.writeAttribute("name", c);
            xml.writeAttribute("duration", Durations.get(index) + "");
            xml.writeEndElement();
            
        }
        xml.writeEndElement();
        xml.writeStartElement(ELEMENT_SOURCES);
        for (Stream s : streams) {
            String clazzSink = s.getClass().getCanonicalName();
            if (clazzSink.contains("Sink")){
//                System.out.println("Skipping Sink: "+clazzSink);
            } else {
                System.out.println("Saving Stream: "+s.getName());
                xml.writeStartElement(ELEMENT_SOURCE);
                writeObject(s, xml);
                xml.writeEndElement(); // Save Source
            }
        }
        xml.writeEndElement();  //Save Sources

        xml.writeStartElement(ELEMENT_MIXER);
        xml.writeAttribute("width", MasterMixer.getInstance().getWidth() + "");
        xml.writeAttribute("height", MasterMixer.getInstance().getHeight() + "");
        xml.writeAttribute("rate", MasterMixer.getInstance().getRate() + "");
        xml.writeEndElement(); //Save Mixer

        xml.writeEndElement(); //Save TrucklistStudio
        xml.writeEndDocument();
        xml.flush();
        xml.close();
        TransformerFactory factory = TransformerFactory.newInstance();

        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new StreamSource(new StringReader(writer.getBuffer().toString())), new StreamResult(file));

    }

    private static void writeObject(Object o, XMLStreamWriter xml) throws IllegalArgumentException, IllegalAccessException, XMLStreamException {
        
        Field[] fields = o.getClass().getDeclaredFields();
        Field[] superFields = null;
        if (o instanceof Stream) {
            superFields = o.getClass().getSuperclass().getDeclaredFields();
        }
        String clazz = o.getClass().getCanonicalName();
        if (clazz != null) {
            xml.writeAttribute("clazz", clazz);
        }
        if (superFields != null) {
            for (Field f : superFields) {
                f.setAccessible(true);
                String name = f.getName();
                Object value = f.get(o);
                if (value instanceof Integer) {
                    xml.writeAttribute(name, f.getInt(o) + "");
                } else if (value instanceof Float) {
                    xml.writeAttribute(name, f.getFloat(o) + "");
                } else if (value instanceof Boolean) {
                    xml.writeAttribute(name, f.getBoolean(o) + "");
                }
                
            }
        }
        for (Field f : fields) {
            f.setAccessible(true);
            String name = f.getName();
            Object value = f.get(o);
            if (value instanceof Integer) {
                xml.writeAttribute(name, f.getInt(o) + "");
            } else if (value instanceof Float) {
                xml.writeAttribute(name, f.getFloat(o) + "");
            } else if (value instanceof Boolean) {
                xml.writeAttribute(name, f.getBoolean(o) + "");
            }
        }    
        if (superFields != null) {
            for (Field f : superFields) {
                f.setAccessible(true);
                String name = f.getName();
                Object value = f.get(o);
                if (value instanceof String) {
                    xml.writeStartElement(name);
                    xml.writeCData(value.toString());
                    xml.writeEndElement();
                } else if (value instanceof File) {
                    xml.writeStartElement(name);
                    xml.writeCData(((File) value).getAbsolutePath());
                    xml.writeEndElement();
                }
            }
        }
        for (Field f : fields) {
            f.setAccessible(true);
            String name = f.getName();
            Object value = f.get(o);
            if (value instanceof String) {
                xml.writeStartElement(name);
                xml.writeCData(value.toString());                
                xml.writeEndElement();
            } else if (value instanceof File) {
                xml.writeStartElement(name);
                xml.writeCData(((File) value).getAbsolutePath());
                xml.writeEndElement();
            }
        }
        if (superFields != null) {
            for (Field f : superFields) {
                f.setAccessible(true);
                String name = f.getName();
                Object value = f.get(o);
                if (value instanceof List) { 
                    switch (name) {
                        case "tracks":
                            xml.writeStartElement(ELEMENT_TRACKS);
                            for (Object subO : ((Iterable<? extends Object>) value)) {
                                if (clazz != null){
                                    xml.writeStartElement(name);
                                    writeObject(subO, xml);
                                    xml.writeEndElement(); 
                                }
                            }
                            break;
                        case "effects":
                            xml.writeStartElement(ELEMENT_EFFECTS);
                            for (Object subO : ((Iterable<? extends Object>) value)) {
                                if (clazz != null){
                                    xml.writeStartElement(name);
                                    writeObject(subO, xml);
                                    xml.writeEndElement(); 
                                }
                            }
                            break;
                        default:
                            xml.writeStartElement(name);
                            for (Object subO : ((Iterable<? extends Object>) value)) {
                                if (clazz != null){
                                    writeObject(subO, xml);
                                }
                            
                            }
                            break;
                    }
                    xml.writeEndElement();
                }
            }
        }
        for (Field f : fields) {
            f.setAccessible(true);
            String name = f.getName();
            Object value = f.get(o);
            if (value instanceof List) {
                if ("effects".equals(name)) {
                    xml.writeStartElement(ELEMENT_EFFECTS);
                    for (Object subO : ((Iterable<? extends Object>) value)) {
                        if (clazz != null){
                            xml.writeStartElement(name);
                            writeObject(subO, xml);
                            xml.writeEndElement(); 
                        }
                    }
                } else {
                    xml.writeStartElement(name);
                    for (Object subO : ((Iterable<? extends Object>) value)) {
                        writeObject(subO, xml);
                    }
                }
                xml.writeEndElement();
            }
        }
    }
    private static void loadTransitions(ArrayList<SourceTrack> SCL, Stream stream, ArrayList<String> subSTrans, ArrayList<String> subETrans, ArrayList<String> SubChNames, ArrayList<String> SubText, ArrayList<String> SubFont) {
        int op=0;
        for (SourceTrack scs : SCL) {
            scs.setName(SubChNames.get(op));
            if (SubText != null) {
                scs.setText(SubText.get(op));
                scs.setFont(SubFont.get(op));
            }
            if (!subSTrans.isEmpty() && subSTrans.get(op) != null){
                if (subSTrans.get(op).endsWith("FadeIn")){
                    Transition t = Transition.getInstance(stream, "FadeIn");
                    scs.startTransitions.add(t);
                } if (subSTrans.get(op).endsWith("AudioFadeIn")){
                    Transition t = Transition.getInstance(stream, "AudioFadeIn");
                    scs.startTransitions.add(t);
                } if (subSTrans.get(op).endsWith("TranslateIn")){
                    Transition t = Transition.getInstance(stream, "TranslateIn");
                    scs.startTransitions.add(t);
                } if (subSTrans.get(op).endsWith("ResizeIn")){
                    Transition t = Transition.getInstance(stream, "ResizeIn");
                    scs.startTransitions.add(t);
                } if (subSTrans.get(op).endsWith("RevealLeft")){
                    Transition t = Transition.getInstance(stream, "RevealLeft");
                    scs.startTransitions.add(t);
                } if (subSTrans.get(op).endsWith("CornerResize")){
                    Transition t = Transition.getInstance(stream, "CornerResize");
                    scs.startTransitions.add(t);
                } if (subSTrans.get(op).endsWith("RevealRight")){
                    Transition t = Transition.getInstance(stream, "RevealRight");
                    scs.startTransitions.add(t);
                }
            }
            if (!subETrans.isEmpty() && subETrans.get(op) != null){
                if (subETrans.get(op).endsWith("FadeOut")){
                    Transition t = Transition.getInstance(stream, "FadeOut");
                    scs.endTransitions.add(t);
                } if (subETrans.get(op).endsWith("TranslateOut")){
                    Transition t = Transition.getInstance(stream, "TranslateOut");
                    scs.endTransitions.add(t);
                } if (subETrans.get(op).endsWith("AudioFadeOut")){
                    Transition t = Transition.getInstance(stream, "AudioFadeOut");
                    scs.endTransitions.add(t);
                } if (subETrans.get(op).endsWith("ShrinkOut")){
                    Transition t = Transition.getInstance(stream, "ShrinkOut");
                    scs.endTransitions.add(t);
                } if (subETrans.get(op).endsWith("HideLeft")){
                    Transition t = Transition.getInstance(stream, "HideLeft");
                    scs.endTransitions.add(t);
                } if (subETrans.get(op).endsWith("HideRight")){
                    Transition t = Transition.getInstance(stream, "HideRight");
                    scs.endTransitions.add(t);
                } if (subETrans.get(op).endsWith("CornerShrink")){
                    Transition t = Transition.getInstance(stream, "CornerShrink");
                    scs.endTransitions.add(t);
                }
            }
            stream.addTrack(scs);
            op+=1;
        }
        SCL.clear();
        SubChNames.clear();
        subSTrans.clear();
        subETrans.clear();
    }
    
    private static Effect loadEffects(String sClazz, Node SuperChild){
        Effect effeX = null;
        try {
            if (sClazz.endsWith("ChromaKey")) {
                effeX = new ChromaKey();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Blink")) {
                effeX = new Blink();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Contrast")) {
                effeX = new Contrast();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("FlipHorizontal")) {
                effeX = new FlipHorizontal();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("FlipVertical")) {
                effeX = new FlipVertical();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Gain")) {
                effeX = new Gain();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Gray")) {
                effeX = new Gray();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("HSB")) {
                effeX = new HSB();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Sharpen")) {
                effeX = new Sharpen();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Mirror1")) {
                effeX = new Mirror1();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Mirror2")) {
                effeX = new Mirror2();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Mirror3")) {
                effeX = new Mirror3();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Mirror4")) {
                effeX = new Mirror4();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Mosaic")) {
                effeX = new Mosaic();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Crop")) {
                effeX = new Crop();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Opacity")) {
                effeX = new Opacity();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Perspective")) {
                effeX = new Perspective();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("RGB")) {
                effeX = new RGB();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("RevealRightNFade")) {
                effeX = new RevealRightNFade();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("RevealLeftNFade")) {
                effeX = new RevealLeftNFade();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Stretch")) {
                effeX = new Stretch();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Rotation")) {
                effeX = new Rotation();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("SwapRedBlue")) {
                effeX = new SwapRedBlue();
                readObjectFx(effeX, SuperChild);
                
            } else if (sClazz.endsWith("Shapes")) {
                effeX = new Shapes();
                readObjectFx(effeX, SuperChild);
                effeX.setShape(shapeImg);
                
            }
        } catch (IllegalArgumentException | IllegalAccessException illegalArgumentException) {
        }
        return effeX;
    }

    private static void readStreams (Document xml) throws IllegalArgumentException, IllegalAccessException, XPathExpressionException{
        XPath path = XPathFactory.newInstance().newXPath();
        NodeList sources = (NodeList) path.evaluate("/" + ELEMENT_ROOT + "/" + ELEMENT_SOURCES + "/" + ELEMENT_SOURCE, xml.getDocumentElement(), XPathConstants.NODESET);
        String videoDev;      
        ArrayList<String> videoDevs = new ArrayList<>();
        ArrayList<Stream> extstreamBis = new ArrayList<>();
        if (sources != null) {
            for (int i = 0; i < sources.getLength(); i++) {
                Node source = sources.item(i);            
                String clazz = source.getAttributes().getNamedItem("clazz").getTextContent();
                String file = null;
                String ObjText = null;
                String fontName = null;
                String trackName = null;
                String comm = null;
                String streamTime = null;
                String streamAudioSrc = null;
                String strShapez = null;
                ArrayList<String> SubChNames = new ArrayList<>();
                ArrayList<String> SubText = new ArrayList<>();
                ArrayList<String> SubFont = new ArrayList<>();
                ArrayList<String> subSTrans = new ArrayList<>();
                ArrayList<String> subETrans = new ArrayList<>();
                ArrayList<String> sNames = new ArrayList<>();
                String sName = null;
                Stream stream = null;
                SourceTrack sc = null;
                Effect effeX = null;
                ArrayList<SourceTrack> SCL = new ArrayList<>();
                ArrayList<Effect> fXL = new ArrayList<>();
                SourceText text;
                for (int j = 0; j < source.getChildNodes().getLength(); j++) {
                    Node child = source.getChildNodes().item(j);
                    if (child.getNodeName().equals("file")) {                       
                        file = child.getTextContent();
                        ImgMovMus.add(file);
                        if (child.getTextContent().contains("/dev/video")){
                            videoDev = child.getTextContent();
                            videoDevs.add(videoDev);
                        }
                    }
                    if (child.getNodeName().equals("name")) {                       
                        sName = child.getTextContent();
                        sNames.add(sName);
                    }
                    if (child.getNodeName().equals("content")) {
                        ObjText = child.getTextContent();
                    }
                    if (child.getNodeName().equals("fontName")) {
                        fontName = child.getTextContent();
                    }
                    if (child.getNodeName().equals("strShape")) {
                        strShapez = child.getTextContent();
                    }
                    if (child.getNodeName().equals("trkName")) {
                        trackName = child.getTextContent();
                    }
                    if (child.getNodeName().equals("comm")) {                       
                        comm = child.getTextContent();
                    }
                    if (child.getNodeName().equals("streamTime")) {                       
                        streamTime = child.getTextContent();
                    }
                    if (child.getNodeName().equals("audioSource")) {                       
                        streamAudioSrc = child.getTextContent();
                    }
                    
                    if (child.getNodeName().equals("Effects")) { // Read Effects
//                        System.out.println("childnodename: "+child.getNodeName());
                        for (int nc = 0; nc < child.getChildNodes().getLength(); nc++) {
                            Node SuperChild = child.getChildNodes().item(nc);
//                            System.out.println("SuperChildnodename: "+SuperChild.getNodeName());
                            if (SuperChild.getNodeName().equals("effects")) {
                                for (int ncc = 0; ncc < SuperChild.getChildNodes().getLength(); ncc++) {
                                    Node SSuperChild = SuperChild.getChildNodes().item(ncc);
                                    if (SSuperChild.getNodeName().equals("shapeS")){
                                        shapeImg = SSuperChild.getTextContent();
//                                        System.out.println("Ass ShapeImg: "+ shapeImg);
                                    }
                                }
                                String sClazz = SuperChild.getAttributes().getNamedItem("clazz").getTextContent();
                                effeX = loadEffects (sClazz, SuperChild);
                                fXL.add(effeX);
//                                System.out.println("effect clazz: "+ sClazz);
                            }
                        }
                    }
                    if (child.getNodeName().equals("Tracks")) { // Read Tracks
                        for (int nc = 0; nc < child.getChildNodes().getLength(); nc++) {
                            Node SuperChild = child.getChildNodes().item(nc);
                            for (int ncc = 0; ncc < SuperChild.getChildNodes().getLength(); ncc++) {
                                Node SSuperChild = SuperChild.getChildNodes().item(ncc);                        
                                if (SSuperChild.getNodeName().equals("name")) {
                                    SubChNames.add(SSuperChild.getTextContent());
                                    sc = new SourceTrack();                                    
                                    readObjectSC(sc, SuperChild);
                                    SCL.add(sc);                                    
                                    trackLoad.add(sc);
                                }  
                                if (SSuperChild.getNodeName().equals("Effects")) {
                                    for (int ncs = 0; ncs < SSuperChild.getChildNodes().getLength(); ncs++) {
                                        Node SSSuperChild = SSuperChild.getChildNodes().item(ncs);            
                                        if (SSSuperChild.getNodeName().equals("effects")) {
                                            for (int nccC = 0; nccC < SSSuperChild.getChildNodes().getLength(); nccC++) {
                                                Node SSSSuperChildC = SSSuperChild.getChildNodes().item(nccC);
                                                if (SSSSuperChildC.getNodeName().equals("shapeS")){
                                                    shapeImg = SSSSuperChildC.getTextContent();
//                                                System.out.println("Ass ShapeImg Chan: "+ shapeImg);
                                                }
                                            }
                                            String sClazz = SSSuperChild.getAttributes().getNamedItem("clazz").getTextContent();
                                            effeX = loadEffects (sClazz, SSSuperChild);
                                            sc.addEffects(effeX);
//                                            System.out.println("channel effect clazz: "+ sClazz);
                                        }     
                                    }
                                }
                                if (SSuperChild.getNodeName().equals("startTransitions")) {
                                    if (SSuperChild.getAttributes().getLength()!= 0) {
                                        String sClazz = SSuperChild.getAttributes().getNamedItem("clazz").getTextContent();
                                        subSTrans.add(sClazz);
                                    } else {
                                        subSTrans.add("None");                                   
                                    }
                                }  
                                if (SSuperChild.getNodeName().equals("endTransitions")) {
                                    if (SSuperChild.getAttributes().getLength()!= 0) {
                                        String sClazz = SSuperChild.getAttributes().getNamedItem("clazz").getTextContent();
                                        subETrans.add(sClazz);
                                    } else {
                                        subETrans.add("None");                                   
                                    }
                                }  
                                if (SSuperChild.getNodeName().equals("text") && SSuperChild.getTextContent() != null) {
                                    SubText.add(SSuperChild.getTextContent());
                                }  
                                if (SSuperChild.getNodeName().equals("font") && SSuperChild.getTextContent() != null) {
                                    SubFont.add(SSuperChild.getTextContent());
                                }
                            }
                        }
                    }
                }              
                if (file != null) {
                    File fileL = new File(file);
                    stream = Stream.getInstance(fileL);
                    extstream.add(stream); 
                    extstreamBis.add(stream);
                    readObject(stream, source);
                    stream.setComm(comm);
                    for (Effect fx : fXL) {
                        if (fx.getName().endsWith("Shapes")){
                            fx.setDoOne(true);
                        }
                        stream.addEffect(fx);
                    }
                    if (streamTime != null){
                        stream.setStreamTime(streamTime);
                    } else {
                        if (stream instanceof SourceMovie || stream instanceof SourceMusic) {
                            TrucklistStudio.getStreamParams(stream, fileL, null);
                        }
                    }
                    stream.setLoaded(true);
                    loadTransitions(SCL, stream, subSTrans, subETrans, SubChNames, null, null);
                    stream.setName(sName);
                    stream.setTrkName(trackName);
                    fXL.clear();
                } else if (clazz.toLowerCase().endsWith("sourcetext")) {
                    text = new SourceText(ObjText);
                    LText.add(text);
                    readObject(text, source);
                    for (Effect fx : fXL) {
                        if (fx.getName().endsWith("Shapes")){
                            fx.setDoOne(true);
                        }
                        text.addEffect(fx);
                    }
                    if (strShapez != null) {
                        switch (strShapez) {
                            case "none":
                                text.setBackground(NONE); 
                                text.setStrBackground("none");
                                break;
                            case "rectangle":
                                text.setBackground(RECTANGLE);
                                text.setStrBackground("rectangle");
                                break;
                            case "oval":
                                text.setBackground(OVAL);
                                text.setStrBackground("oval");
                                break;
                            case "roundrect":
                                text.setBackground(ROUNDRECT);
                                text.setStrBackground("roundrect");
                                break;
                        }
                    }
                    text.setFont(fontName);
                    text.setLoaded(true);
                    loadTransitions(SCL, text, subSTrans, subETrans, SubChNames, SubText, SubFont);
                    text.setName(sName);
                    SCL.clear();
                    SubChNames.clear();
                    subSTrans.clear();
                    subETrans.clear();
                } 
//                else if (clazz.toLowerCase().endsWith("sourceaudiosource")) {
//                    stream = new SourceAudioSource();
//                    extstream.add(stream);
//                    extstreamBis.add(stream);
//                    ImgMovMus.add("Mic");
//                    readObject(stream, source);
//                    stream.setComm(comm);
//                    stream.setAudioSource(streamAudioSrc);
//                    stream.setLoaded(true);
//                    loadTransitions(SCL, stream, subSTrans, subETrans, SubChNames, null, null);
//                } 
                else if (clazz.toLowerCase().endsWith("sourceimagegif")) {
                    for (int an=0;an < truckliststudio.TrucklistStudio.cboAnimations.getItemCount(); an++){
                        for (String aKey : sNames){
                            if (aKey == null ? truckliststudio.TrucklistStudio.cboAnimations.getItemAt(an).toString() == null : aKey.equals(truckliststudio.TrucklistStudio.cboAnimations.getItemAt(an).toString())){
                                String res = truckliststudio.TrucklistStudio.animations.getProperty(aKey);
                                URL url = TrucklistStudio.class.getResource("/truckliststudio/resources/animations/" + res);
                                stream = new SourceImageGif(aKey, url);
                                extstream.add(stream);
                                extstreamBis.add(stream);
                                ImgMovMus.add("ImageGif");
                                readObject(stream, source);  
                                loadTransitions(SCL, stream, subSTrans, subETrans, SubChNames, null, null);
                            }
                        }                   
                    }
                } else {
                    System.err.println("Cannot handle " + clazz);
                }
            }
            for (Stream dST : extstreamBis) {
                int multi=0;
                String streamName = dST.getName();
//                System.out.println("Found Stream Name: "+streamName);
                for (String vDev : videoDevs){
                    if (vDev.contains(streamName)){
                        multi += 1; 
                    }
                }
                if (multi>1) {
                    extstream.remove(dST);
                    ImgMovMus.remove("/dev/"+streamName);
//                    System.out.println(dST+" Removed ...");
//                    System.out.println(streamName+" Removed ...");
                    multi=0;
                }
            }
        }   
    }
    private static void readObject(Stream stream, Node source) throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = stream.getClass().getDeclaredFields();
        Field[] superFields = stream.getClass().getSuperclass().getDeclaredFields();
        // Read integer and floats
        for (Field field : superFields) {
            field.setAccessible(true);
            String name = field.getName();
            String value;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                if (field.get(stream) instanceof Integer) {
                    field.setInt(stream, new Integer(value));
                } else if (field.get(stream) instanceof Float) {
                    field.setFloat(stream, new Float(value));
                } else if (field.get(stream) instanceof Boolean) {
                    field.setBoolean(stream, Boolean.valueOf(value));
                } else if (field.get(stream) instanceof String) {
                    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
                        Node node = source.getChildNodes().item(i);
                        if (node.getNodeName().equals(name)) {
                            field.set(stream, node.getTextContent());
                        }
                    }
                }
            }
        }
        
        
        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            String value;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                if (field.get(stream) instanceof Integer) {
                    field.setInt(stream, new Integer(value));
                } else if (field.get(stream) instanceof Float) {
                    field.setFloat(stream, new Float(value));
                } else if (field.get(stream) instanceof Boolean) {
                    field.setBoolean(stream, Boolean.valueOf(value));
                } else if (field.get(stream) instanceof String) {
                    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
                        Node node = source.getChildNodes().item(i);
                        if (node.getNodeName().equals(name)) {
                            field.set(stream, node.getTextContent());
                        }
                    }
                }

            }
        }
        // Read List
    }

    private static void readObjectFx(Effect fx, Node source) throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = fx.getClass().getDeclaredFields();
        Field[] superFields = fx.getClass().getSuperclass().getDeclaredFields();
        // Read integer and floats
        for (Field field : superFields) {
            field.setAccessible(true);
            String name = field.getName();
            String value;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                if (field.get(fx) instanceof Integer) {
                    field.setInt(fx, new Integer(value));
                } else if (field.get(fx) instanceof Float) {
                    field.setFloat(fx, new Float(value));
                } else if (field.get(fx) instanceof Boolean) {
                    field.setBoolean(fx, Boolean.valueOf(value));
                } else if (field.get(fx) instanceof String) {
                    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
                        Node node = source.getChildNodes().item(i);
                        if (node.getNodeName().equals(name)) {
                            field.set(fx, node.getTextContent());
                        }
                    }
                }
            }
        }
        
        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            String value;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                if (field.get(fx) instanceof Integer) {
                    field.setInt(fx, new Integer(value));
                } else if (field.get(fx) instanceof Float) {
                    field.setFloat(fx, new Float(value));
                } else if (field.get(fx) instanceof Boolean) {
                    field.setBoolean(fx, Boolean.valueOf(value));
                } else if (field.get(fx) instanceof String) {
                    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
                        Node node = source.getChildNodes().item(i);
                        if (node.getNodeName().equals(name)) {
                            field.set(fx, node.getTextContent());
                        }
                    }
                }

            }
        }
        // Read List
    }
    private static void readObjectSC (SourceTrack sc, Node source) throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = sc.getClass().getDeclaredFields();
        Field[] superFields = sc.getClass().getSuperclass().getDeclaredFields();
        // Read integer and floats
        for (Field field : superFields) {
            field.setAccessible(true);
            String name = field.getName();
            String value;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                if (field.get(sc) instanceof Integer) {
                    field.setInt(sc, new Integer(value));
                } else if (field.get(sc) instanceof Float) {
                    field.setFloat(sc, new Float(value));
                } else if (field.get(sc) instanceof Boolean) {
                    field.setBoolean(sc, Boolean.valueOf(value));
                } else if (field.get(sc) instanceof String) {
                    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
                        Node node = source.getChildNodes().item(i);
                        if (node.getNodeName().equals(name)) {
                            field.set(sc, node.getTextContent());
                        }
                    }                       
                }
            }
        }

        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            String value;
            if (source.getAttributes().getNamedItem(name) != null) {
                value = source.getAttributes().getNamedItem(name).getTextContent();
                if (field.get(sc) instanceof Integer) {
                    field.setInt(sc, new Integer(value));
                } else if (field.get(sc) instanceof Float) {
                    field.setFloat(sc, new Float(value));
                } else if (field.get(sc) instanceof Boolean) {
                    field.setBoolean(sc, Boolean.valueOf(value));
                } else if (field.get(sc) instanceof String) {
                    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
                        Node node = source.getChildNodes().item(i);
                        if (node.getNodeName().equals(name)) {
                            field.set(sc, node.getTextContent());
                        }
                    }
                }
            }
        }
        // Read List 
    }
    public static void main() { // removed (String[] args)
        try {
            try {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Studio.filename);
                readStreams(doc);
            } catch (    IllegalArgumentException | IllegalAccessException | XPathExpressionException | SAXException | IOException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    private final ArrayList<String> tracks = MasterTracks.getInstance().getTracks();
    private final ArrayList<Integer> Durations = listener.getCHTimers();
    ArrayList<Stream> streams = MasterTracks.getInstance().getStreams();
    Stream streamC = null;
    protected Studio() {
    }

    public interface Listener {
        public ArrayList<Integer> getCHTimers ();
    }
}
