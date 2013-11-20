package edu.oregonstate.carto.mapcomposer.imageFilters;

import com.jhlabs.image.PointFilter;
import java.awt.image.BufferedImage;

/*
 * AlphaFilter.java
 *
 * Created on July 25, 2007, 3:46 PM
 *
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class AlphaFilter extends PointFilter {
    
    private BufferedImage alpha;
    
    /** Creates a new instance of AlphaFilter */
    public AlphaFilter() {
        canFilterIndexColorModel = false;
    }
    
    public int filterRGB(int x, int y, int rgb) {
        
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4243485
            
        if (x >= alpha.getWidth() || y >= alpha.getHeight())
            return rgb;
        
        int alphaRGB = alpha.getRGB(x, y);
        final int ar = ( 0xff0000 & alphaRGB ) >> 16;
        final int ag = ( 0xff00 & alphaRGB ) >> 8;
        final int ab = 0xff & alphaRGB;
        final int a = (ar + ag + ab) / 3;
        rgb = (rgb & 0xFFFFFF) | (a << 24);
        return rgb;
    }
    
    public String toString() {
        return "Alpha";
    }

    public BufferedImage getAlphaChannel() {
        return alpha;
    }

    public void setAlphaChannel(BufferedImage mask) {
        this.alpha = mask;
    }
}
