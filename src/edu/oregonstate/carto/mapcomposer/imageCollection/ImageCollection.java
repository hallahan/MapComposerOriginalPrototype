/*
 * ImageCollection.java
 *
 * Created on August 3, 2007, 9:20 AM
 *
 */

package edu.oregonstate.carto.mapcomposer.imageCollection;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public interface ImageCollection {
    
    /**
     * Returns an array of image names. This is not a full URL or a file path,
     * but only the names of the images.
     */
    public String[] getImageNames();
    
    public String urlForImageName(String imageName);
    
    public BufferedImage loadImage(String imageName) throws MalformedURLException, IOException;

    public boolean isValidImageURL(String imageName);
}
