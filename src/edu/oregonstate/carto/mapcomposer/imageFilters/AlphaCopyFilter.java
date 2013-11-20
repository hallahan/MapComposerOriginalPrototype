package edu.oregonstate.carto.mapcomposer.imageFilters;

import com.jhlabs.image.PointFilter;
import java.awt.image.BufferedImage;

/*
 * AlphaCopyFilter.java
 *
 * Created on July 25, 2007, 3:46 PM
 *
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class AlphaCopyFilter extends PointFilter {

    private BufferedImage alpha;

    /**
     * Creates a new instance of AlphaFilter
     */
    public AlphaCopyFilter() {
        canFilterIndexColorModel = false;
    }

    public int filterRGB(int x, int y, int rgb) {

        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4243485

        if (x >= alpha.getWidth() || y >= alpha.getHeight()) {
            return rgb;
        }

        final int alphaRGB = alpha.getRGB(x, y);
        rgb = (rgb & 0xFFFFFF) | (0xff000000 & alphaRGB);
        return rgb;
    }

    public String toString() {
        return "Alpha Copy";
    }

    public BufferedImage getAlphaChannel() {
        return alpha;
    }

    public void setAlphaChannel(BufferedImage mask) {
        this.alpha = mask;
    }
}
