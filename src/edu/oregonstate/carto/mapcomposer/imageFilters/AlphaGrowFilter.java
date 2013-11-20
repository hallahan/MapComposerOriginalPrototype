/*
 * AlphaGrowFilter.java
 *
 * Created on August 7, 2007, 3:38 PM
 *
 */

package edu.oregonstate.carto.mapcomposer.imageFilters;

import com.jhlabs.image.WholeImageFilter;
import java.awt.*;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class AlphaGrowFilter extends WholeImageFilter {
    
    /** Creates a new instance of AlphaGrowFilter */
    public AlphaGrowFilter() {
    }
    
    protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
        
        int[] outPixels = new int[width * height];
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (((inPixels[index] >> 24) & 0xff) == 0) {
                    int alphaTot = 0;
                    int k = 0;
                    for (int dy = -1; dy <= 1; dy++) {
                        final int iy = y+dy;
                        if (0 <= iy && iy < height) {
                            int ioffset = iy*width;
                            for (int dx = -1; dx <= 1; dx++) {
                                final int ix = x+dx;
                                if (0 <= ix && ix < width) {
                                    int a = (inPixels[ioffset+ix] >> 24) & 0xff;
                                    if (a > 0) {
                                        alphaTot += a;
                                        ++k;
                                    }
                                }
                            }
                        }
                    }
                    if (k > 0) {
                        int a = alphaTot / k;
                        a = a + 50 > 255 ? 255 : a + 50;
                        outPixels[index] = (inPixels[index] & 0xFFFFFF) | (a << 24);
                    }
                } else {
                    int a = (inPixels[index] >> 24) & 0xff;
                    a = a + 50 > 255 ? 255 : a + 50;
                    outPixels[index] = (inPixels[index] & 0xFFFFFF) | (a << 24);
                }
                ++index;
            }
        }
        
        return outPixels;
    }
    
    public String toString() {
        return "Alpha Grow";
    }
    
}