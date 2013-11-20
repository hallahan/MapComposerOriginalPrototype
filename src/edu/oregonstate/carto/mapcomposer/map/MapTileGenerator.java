/*
 * MapTileGenerator.java
 *
 * Created on May 2013
 *
 */
package edu.oregonstate.carto.mapcomposer.map;

import com.jhlabs.composite.MultiplyComposite;
import edu.oregonstate.carto.imageCollection.TileGenerator;
import edu.oregonstate.carto.imageCollection.TiledImage;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static edu.oregonstate.carto.mapcomposer.map.MapLayer.BLENDINGNORMAL;
import edu.oregonstate.carto.mapcomposer.imageCollection.DirectoryImageCollection;
import edu.oregonstate.carto.mapcomposer.imageCollection.ImageCollection;
import edu.oregonstate.carto.mapcomposer.imageCollection.TiledImageCollection;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 * @author Charlotte Hoarau - COGIT Laboratory, IGN France
 */
public class MapTileGenerator implements TileGenerator {

    private Map map;
    private ImageCollection imageCollection;

    public MapTileGenerator(Map map, ImageCollection imageCollection) {
        this.map = map;
        this.imageCollection = imageCollection;
    }

    @Override
    public BufferedImage createTile(String tileName) {
        BufferedImage backgroundImage = new BufferedImage(
                map.getImageWidth(),
                map.getImageHeight(),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = backgroundImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, map.getImageWidth(), map.getImageHeight());

        for (MapLayer layer : map.getLayers()) {
            try {
                if (!layer.isVisible()) {
                    continue;
                }

                if (layer.getBlending() == BLENDINGNORMAL) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity()));
                } else {
                    g2d.setComposite(new MultiplyComposite(layer.getOpacity()));
                }

                int w = (int) g2d.getDeviceConfiguration().getBounds().getWidth();
                int h = (int) g2d.getDeviceConfiguration().getBounds().getHeight();

                BufferedImage imageTile = null;
                BufferedImage maskTile = null;
                BufferedImage textureTile = null;
                
                if (imageCollection instanceof DirectoryImageCollection) {
                    if (layer.getTextureURL() != null) {
                        try {
                            textureTile = ImageIO.read(new File(layer.getTextureURL()));
                        } catch (IOException ex) {
                            textureTile = null;
                            // FIXME
                            System.err.println("could not load texture image for " + layer.getNameLayer());
                        }
                    }
                    if (imageCollection.isValidImageURL(layer.getImageName())) {
                        imageTile = imageCollection.loadImage(layer.getImageName());
                    }
                    if (imageCollection.isValidImageURL(layer.getMaskName())) {
                        maskTile = imageCollection.loadImage(layer.getMaskName());
                    }

                } else if (imageCollection instanceof TiledImageCollection) {

                    if (layer.getTextureURL() != null) {
                        TiledImage textureTileSet = new TiledImage(layer.getTextureURL());
                        textureTile = textureTileSet.createMegaTile(tileName);
                    }

                    TiledImageCollection tileSets = ((TiledImageCollection) imageCollection);
                    if (layer.getImageName() != null) {
                        imageTile = tileSets.getLayer(layer.getImageName()).createMegaTile(tileName);
                    }
                    if (layer.getMaskName() != null) {
                        maskTile = tileSets.getLayer(layer.getMaskName()).createMegaTile(tileName);
                    }
                    w *= 3;
                    h *= 3;
                }
                layer.renderToTile(g2d, imageTile, maskTile, textureTile, w, h, backgroundImage, imageCollection);

            } catch (MalformedURLException ex) {
                Logger.getLogger(MapTileGenerator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MapTileGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return backgroundImage;
    }
}
