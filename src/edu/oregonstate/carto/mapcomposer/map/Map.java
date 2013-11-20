/*
 * Map.java
 *
 * Created on July 31, 2007, 9:34 AM
 *
 */

package edu.oregonstate.carto.mapcomposer.map;

import edu.oregonstate.carto.imageCollection.TiledImage;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 * @author Charlotte Hoarau, COGIT Laboratory, IGN France
 */
@XmlRootElement(name = "Map")
public class Map {
    @XmlTransient
    private TiledImage tiledImage;

    public void setTiledImage(TiledImage tiledImage) {
        this.tiledImage = tiledImage;
    }

    @XmlTransient
    public TiledImage getTiledImage() {
        return this.tiledImage;
    }
    
    private BufferedImage image;
    
    @XmlTransient
    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
    
    @XmlElement(name = "imageWidth")
    private int imageWidth = 1000;

    @XmlElement(name = "imageHeight")
    private int imageHeight = 1000;
    
    @XmlTransient
    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    @XmlTransient
    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }
    
    @XmlElement(name = "Layer")
    private ArrayList<MapLayer> layers = new ArrayList<MapLayer>();
    
    /** Creates a new instance of Map */
    public Map() {
    }
    
    public void addLayer (MapLayer mapLayer, int id) {
        this.layers.add(id, mapLayer);
    }
    
    public void removeLayer (MapLayer mapLayer) {
        this.layers.remove(mapLayer);
    }
    
    public MapLayer removeLayer (int id) {
        return this.layers.remove(id);
    }

    public ArrayList<MapLayer> getLayers() {
        return layers;
    }

    public MapLayer getLayer(int i) {
        if (i < 0 || i >= this.layers.size())
            return null;
        return this.layers.get(i);
    }
    
    public int getLayerCount() {
        return this.layers.size();
    }
        
    public static Map unmarshal(String fileName) {
        try {
            JAXBContext context = JAXBContext.newInstance(Map.class);
            Unmarshaller m = context.createUnmarshaller();
            Map map = (Map)m.unmarshal(new FileInputStream(fileName));
            return map;
            
        } catch (JAXBException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new Map();
    }
    
    public void marshal(String fileName) {
        try {
            JAXBContext context = JAXBContext.newInstance(Map.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, new FileOutputStream(fileName));
            
        } catch (JAXBException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
