package edu.oregonstate.carto.mapcomposer.texture.style;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @deprecated
 * @author Charlotte Hoarau, COGIT Laboratory, IGN France
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TextureGradientColor {
    
    @XmlElement(name = "color")
    private int color;
    
    @XmlElement(name = "position")
    private int position;
    
    @XmlElement(name = "interpolation")
    private int interpolation;
    
    @XmlElement(name = "colorSpace")
    private int colorSpace;
    
    public TextureGradientColor() { }

    public TextureGradientColor(int color, int position, int interpolation, int colorSpace) {
        this.color = color;
        this.position = position;
        this.interpolation = interpolation;
        this.colorSpace = colorSpace;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getInterpolation() {
        return interpolation;
    }
    
    public byte getType() {
        return (byte) (colorSpace|interpolation);
    }

    public void setInterpolation(int interpolation) {
        this.interpolation = interpolation;
    }

    public int getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(int colorSpace) {
        this.colorSpace = colorSpace;
    }
    
}
