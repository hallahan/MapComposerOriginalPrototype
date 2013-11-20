package edu.oregonstate.carto.mapcomposer.texture.style;

import java.awt.Color;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import edu.oregonstate.carto.mapcomposer.utils.ColorJaxbAdaptor;

/**
 * @author Charlotte Hoarau - COGIT Laboratory, IGN France
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TextureGradient {

    public TextureGradient(){}
    
    public TextureGradient(Color color1, Color color2) {
        this.color1 = color1;
        this.color2 = color2;
    }
    
    @XmlJavaTypeAdapter(ColorJaxbAdaptor.class)
    @XmlElement(name = "color1")
    private Color color1 = new Color(0, 0, 0);

    @XmlTransient
    public Color getColor1() {
        return this.color1;
    }

    public void setColor1(Color color1) {
        this.color1 = color1;
    }
    
    @XmlJavaTypeAdapter(ColorJaxbAdaptor.class)
    @XmlElement(name = "color2")
    private Color color2 = new Color(0, 0, 0);

    @XmlTransient
    public Color getColor2() {
        return this.color2;
    }

    public void setColor2(Color color2) {
        this.color1 = color2;
    }
    
}
