package edu.oregonstate.carto.mapcomposer.texture.style;

import edu.oregonstate.carto.utilities.DirectoryUtils;
import com.jhlabs.image.LinearColormap;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import edu.oregonstate.carto.mapcomposer.texture.TiledTextureFilter;

/**
 * @author Charlotte Hoarau - COGIT Laboratory, IGN France
 */
@XmlRootElement(name = "Texture")
public class Texture {

    public Texture() {
    }

    public Texture(TiledTextureFilter tiledTexture) {
        LinearColormap colorMap = (LinearColormap) tiledTexture.getColormap();
        this.gradient = new TextureGradient(new Color(colorMap.getColor1()), new Color(colorMap.getColor2()));
        this.scale = tiledTexture.getScale();
        this.turbulence = tiledTexture.getTurbulence();
        this.angle = tiledTexture.getAngle();
        this.stretch = tiledTexture.getStretch();
        this.amount = tiledTexture.getAmount();
    }
    //TODO Make an array of gradient in order to store all the gradient in case of adaptive color texture
    @XmlElement(name = "gradient")
    private edu.oregonstate.carto.mapcomposer.texture.style.TextureGradient gradient;
    @XmlElement(name = "scale")
    private float scale;
    @XmlElement(name = "angle")
    private float angle;
    @XmlElement(name = "stretch")
    private float stretch;
    @XmlElement(name = "amount")
    private float amount;
    @XmlElement(name = "turbulence")
    private float turbulence;

    @XmlTransient
    public TextureGradient getGradient() {
        return gradient;
    }

    public void setGradient(TextureGradient gradient) {
        this.gradient = gradient;
    }

    @XmlTransient
    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    @XmlTransient
    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    @XmlTransient
    public float getStretch() {
        return stretch;
    }

    public void setStretch(float stretch) {
        this.stretch = stretch;
    }

    @XmlTransient
    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    @XmlTransient
    public float getTurbulence() {
        return turbulence;
    }

    public void setTurbulence(float turbulence) {
        this.turbulence = turbulence;
    }

    public static Texture unmarshal(String fileName) {
        try {
            JAXBContext context = JAXBContext.newInstance(Texture.class);
            Unmarshaller m = context.createUnmarshaller();

            Texture texture = (Texture) m.unmarshal(new FileInputStream(fileName));
            return texture;

        } catch (JAXBException ex) {
            Logger.getLogger(Texture.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Texture.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new Texture();

    }

    public void marshal(String fileName) {
        try {
            JAXBContext context = JAXBContext.newInstance(Texture.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, new FileOutputStream(fileName));

        } catch (JAXBException ex) {
            Logger.getLogger(Texture.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Texture.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static Texture getTextureFromDirectoryPath(String path) {
        File textureDirectory = new File(path);
        String xmlPath = textureDirectory.list(DirectoryUtils.XML_FILTER)[0];
        return Texture.unmarshal(textureDirectory.getAbsolutePath() + "/" + xmlPath);
    }
}
