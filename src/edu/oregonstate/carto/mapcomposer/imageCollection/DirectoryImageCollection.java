/*
 * DirectoryImageCollection.java
 *
 * Created on August 3, 2007, 9:22 AM
 *
 */
package edu.oregonstate.carto.mapcomposer.imageCollection;

import edu.oregonstate.carto.utilities.DirectoryUtils;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 * @author Charlotte Hoarau - COGIT Laboratory, IGN France.
 */
public class DirectoryImageCollection implements ImageCollection {

    private File directory;

    public File getDirectory() {
        return this.directory;
    }

    /**
     * Creates a new instance of DirectoryImageCollection
     */
    public DirectoryImageCollection(String directoryPath) {
        this.directory = new File(directoryPath);
        if (!this.directory.isDirectory()) {
            throw new IllegalArgumentException("not a directory");
        }
    }

    @Override
    public String[] getImageNames() {
        return directory.list(DirectoryUtils.IMAGE_FILTER);

    }

    @Override
    public String urlForImageName(String imageName) {

        if (imageName == null) {
            return null;
        }

        String[] imageNames = this.getImageNames();
        for (String image : imageNames) {
            if (image.equals(imageName)) {
                try {
                    File file = new File(directory, imageName);
                    URL url = file.toURI().toURL();
                    return url.toExternalForm();
                } catch (MalformedURLException ex) {
                    Logger.getLogger(DirectoryImageCollection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;

    }

    @Override
    public BufferedImage loadImage(String imageName) throws MalformedURLException, IOException {

        String urlStr = this.urlForImageName(imageName);
        if (urlStr == null) {
            return null;
        }
        return ImageIO.read(new URL(urlStr));

    }

    public HashMap<String, BufferedImage> createEmptyResult() {
        HashMap<String, BufferedImage> results = new HashMap<String, BufferedImage>();

        BufferedImage resultImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = resultImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 1000, 1000);

        results.put("map", resultImage);

        return results;
    }

    @Override
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public boolean isValidImageURL(String nameImage) {

        String imageURL = this.urlForImageName(nameImage);

        if (imageURL == null || imageURL.length() == 0) {
            return false;
        }

        try {
            new URL(imageURL);
        } catch (MalformedURLException e) {
            return false;
        }

        return ika.utils.ImageUtils.canReadImage(imageURL);

    }
}
