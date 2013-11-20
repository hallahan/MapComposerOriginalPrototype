/*
 * MapLayer.java
 *
 * Created on July 31, 2007, 9:27 AM
 *
 */
package edu.oregonstate.carto.mapcomposer.map;

import com.jhlabs.composite.MultiplyComposite;
import com.jhlabs.image.BicubicScaleFilter;
import com.jhlabs.image.BoxBlurFilter;
import com.jhlabs.image.ImageUtils;
import com.jhlabs.image.LightFilter;
import com.jhlabs.image.ShadowFilter;
import com.jhlabs.image.TileImageFilter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.net.URL;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import edu.oregonstate.carto.mapcomposer.imageCollection.DirectoryImageCollection;
import edu.oregonstate.carto.mapcomposer.imageCollection.ImageCollection;
import edu.oregonstate.carto.mapcomposer.imageCollection.TiledImageCollection;
import edu.oregonstate.carto.mapcomposer.imageFilters.AdobeCurveReader;
import edu.oregonstate.carto.mapcomposer.imageFilters.CurvesFilter;
import edu.oregonstate.carto.mapcomposer.imageFilters.TintFilter;
import edu.oregonstate.carto.mapcomposer.map.style.Emboss;
import edu.oregonstate.carto.mapcomposer.map.style.Shadow;
import edu.oregonstate.carto.mapcomposer.map.style.Tint;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 * @author Charlotte Hoarau, COGIT Laboratory, IGN France.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MapLayer {

    public static final int BLENDINGNORMAL = 0;
    public static final int BLENDINGMULTIPLY = 1;
    @XmlTransient
    private static ColorModel maskColorModel;

    static {
        byte[] cmap = new byte[256];
        for (int i = 0; i < cmap.length; i++) {
            cmap[i] = (byte) i;
        }
        maskColorModel = new IndexColorModel(8, 256, cmap, cmap, cmap, cmap);
    }
    @XmlElement(name = "visible")
    private boolean visible = true;
    @XmlElement(name = "nameLayer")
    private String nameLayer;
    @XmlElement(name = "imageName")
    private String imageName;
    @XmlElement(name = "textureURL")
    private String textureURL;
    @XmlElement(name = "maskName")
    private String maskName;
    @XmlElement(name = "blending")
    private int blending = BLENDINGNORMAL;
    @XmlElement(name = "opacity")
    private float opacity = 1;
    @XmlElement(name = "curveURL")
    private String curveURL;
    @XmlElement(name = "tint")
    private Tint tint = null;
    @XmlElement(name = "textureScale")
    private float textureScale = 1f;
    @XmlElement(name = "invertMask")
    private boolean invertMask = false;
    @XmlElement(name = "maskBlur")
    private float maskBlur = 0;
    @XmlElement(name = "shadow")
    private Shadow shadow = null;
    @XmlElement(name = "emboss")
    private Emboss emboss = null;

    /**
     * Creates a new instance of MapLayer
     */
    public MapLayer() {
    }

    public MapLayer(String name) {
        this.nameLayer = name;
    }

    @Override
    public String toString() {
        return getNameLayer();
    }
    
    // FIXME move this timer out of this class
    @XmlTransient
    private ika.utils.NanoTimer timer = new ika.utils.NanoTimer();
    @XmlTransient
    private long time = timer.nanoTime();

    private void measureTime(String message) {

        if (time != 0) {
            long currentTime = this.timer.nanoTime();
            long dT = (currentTime - this.time) / 1000 / 1000;
            message = dT + " msec \t" + message;
        }
        System.out.println(message);
        this.time = this.timer.nanoTime();
    }

    /**
     * Render this layer and composite with the content of a Graphics2D context.
     *
     * @param g2d
     * @param imageTile
     * @param maskImageTile
     * @param textureImage
     * @param h
     * @param w
     * @param backgroundImage
     * @param imageCollection
     * @return
     * @throws IOException
     */
    public BufferedImage renderToTile(Graphics2D g2d, BufferedImage imageTile, BufferedImage maskImageTile, BufferedImage textureImage, int h, int w, BufferedImage backgroundImage, ImageCollection imageCollection) throws IOException {

        // load plain image
        BufferedImage image = null;
        if (isValidImage(imageTile)) {
            image = imageTile;
            // convert to ARGB. All following manipulations are optimized for 
            // this modus.
            image = ImageUtils.convertImageToARGB(image);
            this.measureTime("Image loaded");
        }

        // texture
        if (isValidImage(textureImage)) {

            BufferedImage texturePatch = textureImage;
            texturePatch = ImageUtils.convertImageToARGB(texturePatch);

            // scale texture patch if needed
            if (textureScale != 1f) {
                int textureW = (int) (texturePatch.getWidth() * this.textureScale);
                int textureH = (int) (texturePatch.getHeight() * this.textureScale);
                BicubicScaleFilter scaleFilter = new BicubicScaleFilter(textureW, textureH);
                texturePatch = scaleFilter.filter(texturePatch, null);
            }

            TileImageFilter tiler = new TileImageFilter();

            tiler.setHeight(w);
            tiler.setWidth(h);
            BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            image = tiler.filter(texturePatch, dst);
            this.measureTime("Image textured");
        }

        // create solid white background image if no image has been
        // loaded and no textured image has been created.
        if (image == null) {

            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);
            g.dispose();

            this.measureTime("Image filled");
        }

        // gradation curve
        if (this.curveURL != null && this.curveURL.length() > 0) {
            image = curve(image, this.curveURL);
            this.measureTime("Gradation curve applied");
        }

        // tinting
        if (this.tint != null) {
            // use the pre-existing image for modulating brightness if the image
            // exists (i.e. a texture image has been created or an image has
            // been loaded).
            if (isValidImage(textureImage) || isValidImage(imageTile)) {
                TintFilter tintFilter = new TintFilter();
                tintFilter.setTint(this.tint.getTintColor());
                image = tintFilter.filter(image, null);
                this.measureTime("Image tinted");
            } else {
                // no pre-existing image, create a solid color image
                image = solidColorImage(w, h, this.tint.getTintColor());
                this.measureTime("New solid color image");
            }
        }

        // masking
        BufferedImage maskImage = maskImageTile;
        if (isValidImage(maskImage)) {
            if (this.maskBlur > 0) {

                BoxBlurFilter blurFilter = new BoxBlurFilter();
                blurFilter.setHRadius(this.maskBlur);
                blurFilter.setVRadius(this.maskBlur);
                blurFilter.setPremultiplyAlpha(false);
                blurFilter.setIterations(1);

                /*
                 GaussianFilter blurFilter = new GaussianFilter();
                 blurFilter.setRadius(this.maskBlur);
                 */
                maskImage = blurFilter.filter(maskImage, null);

            }
            image = alphaChannelFromGrayImage(image, maskImage, this.invertMask);
            this.measureTime("Alpha channel applied");
        }

        // embossing
        if (this.emboss != null) {
            // this solution works fine, but is slow
            LightFilter lightFilter = new LightFilter();
            lightFilter.setBumpSource(LightFilter.BUMPS_FROM_IMAGE_ALPHA);
            lightFilter.setBumpHeight(this.emboss.getEmbossHeight());
            lightFilter.setBumpSoftness(this.emboss.getEmbossSoftness());
            LightFilter.Light forestLight = (LightFilter.Light) (lightFilter.getLights().get(0));
            forestLight.setAzimuth((float) Math.toRadians(this.emboss.getEmbossAzimuth() - 90));
            forestLight.setElevation((float) Math.toRadians(this.emboss.getEmbossElevation()));
            forestLight.setDistance(0);
            forestLight.setIntensity(1f);
            //lightFilter.getMaterial().highlight = 10f;
            lightFilter.getMaterial().highlight = 10f;
            image = lightFilter.filter(image, null);

            /*
             // this solution produces hard shadows but is faster (?)
             BorderEmbossFilter filter = new BorderEmbossFilter();
             filter.setBumpHeight(this.embossHeight * 100f);
             filter.setAzimuth((float)Math.toRadians(this.embossAzimuth));
             filter.setElevation((float)Math.toRadians(this.embossElevation));
             image = filter.filter(image, null);
             */
            this.measureTime("Image embossed");
        }

        // drop shadow: draw it onto the destination image
        if (this.shadow != null) {
            BufferedImage shadowImage = ImageUtils.cloneImage(image);

            //x negative : left  -  x positive : right
            //y negative : down  -  y positive : up
            //TODO : distinguish x and y offset OR use a mouving offset !!
            ShadowFilter shadowFilter = new ShadowFilter(this.shadow.getShadowFuziness(), this.shadow.getShadowOffset(), -this.shadow.getShadowOffset(), 1f);
            shadowFilter.setShadowColor(this.shadow.getShadowColor().getRGB());
            shadowImage = shadowFilter.filter(shadowImage, null);

            if (imageCollection instanceof TiledImageCollection) {
                g2d.drawImage(((TiledImageCollection) imageCollection).cutTileFromMegaTile(shadowImage), null, null);
            } else if (imageCollection instanceof DirectoryImageCollection) {
                g2d.drawImage(shadowImage, null, null);
            }
            this.measureTime("Drop shadow");
        }

        // draw this layer into the destination image
        if (imageCollection instanceof TiledImageCollection) {
            g2d.drawImage(((TiledImageCollection) imageCollection).cutTileFromMegaTile(image), null, null);
            return ((TiledImageCollection) imageCollection).cutTileFromMegaTile(image);
        } else if (imageCollection instanceof DirectoryImageCollection) {
            g2d.drawImage(image, null, null);
            return image;
        } else {
            return null;
        }

    }

    private static BufferedImage curve(BufferedImage image, String curveURL) throws IOException {
        AdobeCurveReader acr = new AdobeCurveReader();
        acr.readACV(new URL(curveURL));
        CurvesFilter curvesFilter = new CurvesFilter();
        CurvesFilter.Curve curve = acr.getCurve(0);
        curve.normalize();
        curvesFilter.setCurve(curve);
        return curvesFilter.filter(image, null);
    }

    private static BufferedImage multiply(BufferedImage img1, BufferedImage img2, float alpha) {
        Graphics2D g = img1.createGraphics();
        g.setComposite(new MultiplyComposite(alpha));
        g.drawImage(img2, null, null);
        g.dispose();
        return img1;
    }

    /**
     * Use a grayscale image as alpha channel for another image.
     */
    private static BufferedImage alphaChannelFromGrayImage(BufferedImage image,
            BufferedImage mask, boolean invertMask) {
        // make sure the images have the same size !!! ???

        image = ImageUtils.convertImageToARGB(image);

        // convert mask to grayscale image if necessary
        if (mask.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            System.out.println("!!!! Alpha Mask not in Grayscale Modus !!!!");
            BufferedImage tmpMask = new BufferedImage(mask.getWidth(),
                    mask.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            ColorConvertOp toGray = new ColorConvertOp(null);
            mask = toGray.filter(mask, tmpMask);
        }

        // for TYPE_INT_ARGB with a TYPE_BYTE_GRAY mask
        if (mask.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            byte ad[] = ((DataBufferByte) mask.getRaster().getDataBuffer()).getData();
            int d[] = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            int size = image.getWidth() * image.getHeight();
            if (invertMask) {
                for (int i = 0; i < size; i++) {
                    d[i] = (d[i] & 0xFFFFFF) | ((((int) ad[i]) << 24) ^ 0xff000000);
                }
            } else {
                for (int i = 0; i < size; i++) {
                    d[i] = (d[i] & 0xFFFFFF) | (((int) ad[i]) << 24);
                }
            }
        }
        return image;
    }

    /**
     * Copy the alpha channel of one image and apply it to another image.
     */
    private static BufferedImage copyAlphaChannel(BufferedImage image,
            BufferedImage alpha) {

        // make sure the images have the same size !!! ???

        // make sure both are of type TYPE_INT_ARGB
        image = ImageUtils.convertImageToARGB(image);
        alpha = ImageUtils.convertImageToARGB(alpha);

        int ad[] = ((DataBufferInt) alpha.getRaster().getDataBuffer()).getData();
        int d[] = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        int size = image.getWidth() * image.getHeight();

        for (int i = 0; i < size; i++) {
            d[i] = (d[i] & 0xFFFFFF) | (ad[i] & 0xFF000000);
        }

        return image;

    }

    private static BufferedImage solidColorImage(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return image;
    }

    private static boolean isValidImage(BufferedImage image) {
        if (image == null) {
            return false;
        }

        return true;

    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getNameLayer() {
        return nameLayer;
    }

    public void setNameLayer(String nameLayer) {
        this.nameLayer = nameLayer;
    }

    @XmlTransient
    public String getImageName() {
        return this.imageName;
    }

    public void setImageName(String name) {
        this.imageName = name;
    }

    @XmlTransient
    public String getMaskName() {
        return this.maskName;
    }

    public void setMaskName(String name) {
        this.maskName = name;
    }

    @XmlTransient
    public String getTextureURL() {
        return this.textureURL;
    }

    public void setTextureURL(String name) {
        this.textureURL = name;
    }

    public int getBlending() {
        return blending;
    }

    public void setBlending(int blending) {
        this.blending = blending;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public String getCurveURL() {
        return curveURL;
    }

    public void setCurveURL(String curveURL) {
        this.curveURL = curveURL;
    }

    public Tint getTint() {
        return tint;
    }

    public void setTint(Tint tint) {
        this.tint = tint;
    }

    public float getTextureScale() {
        return textureScale;
    }

    public void setTextureScale(float textureScale) {
        this.textureScale = textureScale;
    }

    public boolean isInvertMask() {
        return invertMask;
    }

    public void setInvertMask(boolean invertMask) {
        this.invertMask = invertMask;
    }

    public float getMaskBlur() {
        return maskBlur;
    }

    public void setMaskBlur(float maskBlur) {
        this.maskBlur = maskBlur;
    }

    public Shadow getShadow() {
        return shadow;
    }

    public void setShadow(Shadow shadow) {
        this.shadow = shadow;
    }

    public Emboss getEmboss() {
        return emboss;
    }

    public void setEmboss(Emboss emboss) {
        this.emboss = emboss;
    }
}