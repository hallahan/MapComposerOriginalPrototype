/*
 * Main.java
 *
 * Created on July 25, 2007, 12:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.oregonstate.carto.mapcomposer.map.gui;

import edu.oregonstate.carto.mapcomposer.imageFilters.AdobeCurveReader;
import edu.oregonstate.carto.mapcomposer.imageFilters.CurvesFilter;
import edu.oregonstate.carto.mapcomposer.imageFilters.AlphaFilter;
import com.jhlabs.composite.*;
import com.jhlabs.image.*;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class MainLauncher {
    
    private static final int imageWidth = 1000;
    private static final int imageHeight = 1000;
    
    private static final String BASE = "file:///Volumes/FileServer/MapComposer/MapComposer/data/schaffhaussen/";
    private static final String TEXTURE_BASE = "file:///Volumes/FileServer/MapComposer/MapComposer/data/textures/";
    private static final String CURVE_BASE = "file:///Volumes/FileServer/MapComposer/MapComposer/data/curves/";

    private static final String forestTextureURL = TEXTURE_BASE + "foresttexture.jpg";
    private static final String forestMaskURL = BASE + "forestmask.jpg";
    private static final String forestCurveURL = CURVE_BASE + "forestcurve.acv";
    
    private static final String riverTextureURL = TEXTURE_BASE + "rivertexture.png";
    private static final String riverMaskURL = BASE + "river.jpg";
    private static final String drainageMaskURL = BASE + "drainage.jpg";
    private static final Color DRAINAGE_TINT = new Color(14, 113, 249);
    
    private static final String roadsMaskURL = BASE + "roads.jpg";
    private static final Color ROADS_TINT = new Color(250, 250, 215);
    
    private static final String shadingURL = BASE + "shading.jpg";
    private static final String shadingCurveURL = CURVE_BASE + "shadingcurve.acv";
    private static final Color SHADING_TINT = new Color (215, 251, 138);
    private static final float SHADING_TINT_TRANSPARENCY = 0.5f;
    
    private static final String orthophotoURL = BASE + "orthophoto.jpg";
    private static final String orthophotoMaskURL = BASE + "orthophotomask.jpg";
    private static final String orthophotoCurveURL = CURVE_BASE + "orthophotocurve.acv";
    
    private static final String buildingsURL = BASE + "buildings.jpg";
    private static final Color BUILDINGS_TINT = new Color (195, 63, 63);
    private static final Color BUILDINGS_SHADOW_TINT = new Color (0, 0, 0);
    private static final float BUILDINGS_TINT_TRANSPARENCY = 1f;
    private static final int BUILDINGS_SHADOW_OFFSET = 1; // pixels
    
    private static final String typeURL = BASE + "type.png";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try {
            
            BufferedImage forestTexture = loadImage(forestTextureURL);
            BufferedImage forestMask = loadImage(forestMaskURL);
            BufferedImage shading = loadImage(shadingURL);
            BufferedImage orthophoto = loadImage(orthophotoURL);
            BufferedImage orthophotoMask = loadImage(orthophotoMaskURL);
            BufferedImage buildings = loadImage(buildingsURL);
            BufferedImage drainage = loadImage(drainageMaskURL);
            BufferedImage riverTexture = loadImage(riverTextureURL);
            BufferedImage riverMask = loadImage(riverMaskURL);
            BufferedImage roads = loadImage(roadsMaskURL);
            BufferedImage type = loadImage(typeURL);
            
            BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, imageWidth, imageHeight);
            
            // tint the shaded relief
            shading = ImageUtils.convertImageToARGB(shading);
            shading = curve(shading, shadingCurveURL);
            BufferedImage tint = solidColorImage(imageWidth, imageHeight, SHADING_TINT);
            shading = multiply(shading, tint, SHADING_TINT_TRANSPARENCY);
            
            // create the forest
            TileImageFilter tiler = new TileImageFilter();
            tiler.setHeight(imageHeight);
            tiler.setWidth(imageWidth);
            BufferedImage forest = tiler.filter(forestTexture, null);
            // set the alpha channel of the forest
            forest = alphaChannelFromGrayImage(forest, forestMask);
            
            // emboss the forest
            LightFilter forestLightFilter = new LightFilter();
            forestLightFilter.setBumpSource(LightFilter.BUMPS_FROM_IMAGE_ALPHA);
            forestLightFilter.setBumpHeight(0.8f);
            forestLightFilter.setBumpSoftness(10f);
            LightFilter.Light forestLight = (LightFilter.Light)(forestLightFilter.getLights().get(0));    
            forestLight.setAzimuth((float)Math.toRadians(315-90));
            forestLight.setElevation((float)Math.toRadians(33));
            forestLight.setDistance(0);
            forestLight.setIntensity(1f);
            forestLightFilter.getMaterial().highlight = 10f;
            forest = forestLightFilter.filter(forest, null);

            forest = curve(forest, forestCurveURL);
            
            // set the alpha channel of the orthophoto           
            orthophoto = alphaChannelFromGrayImage(orthophoto, orthophotoMask);
            orthophoto = curve(orthophoto, orthophotoCurveURL);
            
            // shadow for buildings
            tint = solidColorImage(imageWidth, imageHeight, BUILDINGS_SHADOW_TINT);
            BufferedImage buildingsShadow = alphaChannelFromGrayImage(tint, buildings);
            buildingsShadow = new InvertAlphaFilter().filter(buildingsShadow, null);
            
            // buildings
            tint = solidColorImage(imageWidth, imageHeight, BUILDINGS_TINT);
            buildings = alphaChannelFromGrayImage(tint, buildings);
            buildings = new InvertAlphaFilter().filter(buildings, null);
            
            // rivers
            BufferedImage rivers = tiler.filter(riverTexture, null);
            rivers = alphaChannelFromGrayImage(rivers, riverMask);
            rivers = new InvertAlphaFilter().filter(rivers, null);
            
            // drainage
            tint = solidColorImage(imageWidth, imageHeight, DRAINAGE_TINT);
            drainage = alphaChannelFromGrayImage(tint, drainage);
            drainage = new InvertAlphaFilter().filter(drainage, null);
            
            // roads
            tint = solidColorImage(imageWidth, imageHeight, ROADS_TINT);
            roads = alphaChannelFromGrayImage(tint, roads);
            roads = new InvertAlphaFilter().filter(roads, null);
            // emboss roads
            LightFilter roadsLightFilter = new LightFilter();
            roadsLightFilter.setBumpSource(LightFilter.BUMPS_FROM_IMAGE_ALPHA);
            roadsLightFilter.setBumpHeight(-0.5f);
            roadsLightFilter.setBumpSoftness(2f);
            LightFilter.Light roadsLight = (LightFilter.Light)(roadsLightFilter.getLights().get(0));    
            roadsLight.setAzimuth((float)Math.toRadians(315-90));
            roadsLight.setElevation((float)Math.toRadians(33));
            roadsLight.setDistance(0);
            roadsLight.setIntensity(1f);
            roadsLightFilter.getMaterial().highlight = 10f;
            roads = roadsLightFilter.filter(roads, null);
            
            // composite the image
            
            g.drawImage(shading, null, null);
            g.setComposite(new MultiplyComposite(1f));
            g.drawImage(orthophoto, null, null);
            g.drawImage(forest, null, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g.drawImage(buildingsShadow, (BufferedImageOp)null, 
                    BUILDINGS_SHADOW_OFFSET, BUILDINGS_SHADOW_OFFSET);
            g.drawImage(buildings, null, null);
            g.drawImage(drainage, null, null);
            g.drawImage(rivers, null, null);
            g.drawImage(roads, null, null);
            
            // construct mask for type layer: reduce contrast and increase 
            // brightness of the image. Then set mask to area around type. 
            // Finally draw the mask and then the type.
            /*
            ContrastFilter contrastFilter = new ContrastFilter();
            contrastFilter.setBrightness(2);
            contrastFilter.setContrast(0.5f);
            BufferedImage brightImage = contrastFilter.filter(ImageUtils.cloneImage(image), null);

            GaussianFilter gaussianFilter = new GaussianFilter();
            gaussianFilter.setRadius(7);
            BufferedImage typeMask = gaussianFilter.filter(ImageUtils.cloneImage(type), null);
            ContrastFilter c2 = new ContrastFilter();
            c2.setBrightness(1);
            c2.setContrast(2);
            typeMask = c2.filter(typeMask, null);
            brightImage = alphaChannelFromGrayImage(brightImage, typeMask);
            brightImage = new InvertAlphaFilter().filter(brightImage, null);
            g.drawImage(brightImage, null, null);
            */
            g.drawImage(type, null, null);

            ika.utils.ImageUtils.displayImageInWindow(image);
            
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    
    private static BufferedImage multiply(BufferedImage img1, BufferedImage img2, float alpha) {
        Graphics2D g = img1.createGraphics();
        g.setComposite(new MultiplyComposite(alpha));
        g.drawImage(img2, null, null);   
        return img1;
    }
    
    private static BufferedImage alphaChannelFromGrayImage(BufferedImage image, BufferedImage alpha) {
        AlphaFilter alphaFilter = new AlphaFilter();
        alphaFilter.setAlphaChannel(alpha);
        image = ImageUtils.convertImageToARGB(image);
        image = alphaFilter.filter(image, null);
        return image;
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
    
    private static BufferedImage curve(BufferedImage image, float[] x, float[] y) {
        CurvesFilter curvesFilter = new CurvesFilter();
        CurvesFilter.Curve curve = new CurvesFilter.Curve();
        curve.setKnots(x, y);
        curvesFilter.setCurve(curve);
        return curvesFilter.filter(image, null);
    }
    
    private static void mask(BufferedImage src, BufferedImage mask) {
        /*int[] alphaChannel = null;
        
        WritableRaster araster = src.getAlphaRaster();
        mask.getRaster().getPixels(0, 0, mask.getWidth(), mask.getHeight(), alphaChannel);
        araster.setSamples(0, 0, mask.getWidth(), mask.getHeight(), 0, alphaChannel);
        */
        Graphics2D g = src.createGraphics();
        g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
        g.setComposite( AlphaComposite.getInstance( AlphaComposite.CLEAR) );
        g.drawImage( mask, null, null );
        
    }
    
    private static BufferedImage solidColorImage(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        return image;
    }

    private static BufferedImage loadImage(String urlStr) throws MalformedURLException, IOException {
        URL url = new URL(urlStr);
        BufferedImage image = ImageIO.read(url);
        return image;
    }
}
