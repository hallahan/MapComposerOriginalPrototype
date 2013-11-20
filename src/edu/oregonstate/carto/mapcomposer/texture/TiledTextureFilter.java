package edu.oregonstate.carto.mapcomposer.texture;

import com.jhlabs.image.LinearColormap;
import com.jhlabs.image.PointFilter;
import com.jhlabs.image.TextureFilter;
import com.jhlabs.image.Gradient;
import edu.oregonstate.carto.utilities.Grid;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import edu.oregonstate.carto.mapcomposer.texture.style.Texture;

/**
 * Image filter to create tiled texture using Perlin Noise.
 * The values of the Perlin Noise are calculated 
 * depending on the coordinates of the tile in order to avoid seams.
 * 
 * @author Charlotte Hoarau, COGIT Laboratory, IGN France
 */
public class TiledTextureFilter extends TextureFilter {

    private boolean scaleAdaptive = false;
    private boolean colorAdaptive = false;

    private int column;
    private int row;
    private BufferedImage thematicImage = null;
    private BufferedImage orthoImage = null;
    private Grid elevation = null;
    
    public final static float[] corvallisElevationRanges = new float[]{50, 250, 300, 450, 500, 650};
    public final static float[] mountHoodElevationRanges = new float[]{150, 1000, 1200, 2000, 2200, 3000}; //Mount Hood
    public final static float[] rotationTestElevationRamges = new float[]{50, 650};
    
    public final static LinearColormap[] testColorMaps = new LinearColormap[]{
        new LinearColormap(0xffFAF0E6, 0xffFFFFD4), //White, bright
        new LinearColormap(0xff9fe855, 0xff3a9d23), //Light green
        new LinearColormap(0xff096a09, 0xff18391e), //Dark green
        new LinearColormap(0xff9D3E0C, 0xffAE642D) //Brown
    };
    
    public final static LinearColormap[] forestColorMaps = new LinearColormap[]{
        new LinearColormap(0xff9fe855, 0xff3a9d23), //Light green
        new LinearColormap(0xff3a9d23, 0xff096a09), //Medium green
        new LinearColormap(0xff096a09, 0xff18391e), //Dark green
        new LinearColormap(0xff18391e, 0xff00250f)  // Very Dark green
    };
    
    public final static float[] stretchRangesTest = new float[]{ 0f, 0.2f, 0.5f, 0.8f };
    public final static float[] stretchRangesTest2 = new float[]{15f, 12f, 8f, 5f};
    
    public final static float[] anglesTest = new float[]{ 0f, 0.5f, 1f, 1.5f };
    public final static float[] anglesTest2 = new float[]{3.14f, 0f};
    
    private LinearColormap[] colorMaps = forestColorMaps;
    private float[] elevations = mountHoodElevationRanges;
    private float[] stretchs = stretchRangesTest2;
    private float[] angleSteps = anglesTest2;
    
    public TiledTextureFilter() {
    }

    public TiledTextureFilter(Texture texture) {
        this.setAmount(texture.getAmount());
        this.setAngle(texture.getAngle());
        this.setStretch(texture.getStretch());
        this.setScale(texture.getScale());
        this.setTurbulence(texture.getTurbulence());
        Color c1 = texture.getGradient().getColor1();
        Color c2 = texture.getGradient().getColor2();
        LinearColormap g = new LinearColormap(c1.getRGB(), c2.getRGB());
        this.setColormap(g);
//        this.referenceScale = texture.getScale();
    }

    public TiledTextureFilter(int c, int r) {
        this.column = c;
        this.row = r;
    }

    public TiledTextureFilter(int c, int r, float z) {
        this.column = c;
        this.row = r;
//        this.zoom = adaptZoom2(z);
    }

    public void setColumnRow(int c, int r) {
        this.column = c;
        this.row = r;
    }

    public void setColumnRowZoom(int c, int r, float z) {
        this.column = c;
        this.row = r;
//        this.zoom = adaptZoom2(z);
    }

    @Override
    public int filterRGB(int x, int y, int rgb) {
//        if (this.scaleAdaptive) {
//            this.setScale(referenceScale * zoom);
//        }
        
        if (thematicImage != null) {
            //adaptColorToThematicData(x, y);
        }
        if (elevation != null) {
            adaptColorToElevation(x, y);
            //adaptLodToElevation(x,y);
            //textureFilter.setStretch(0.2f);
            //adaptOrientation2(x, y);
        }
        if (orthoImage != null) {
            generateColorFromOrtho(x, y);
        }

        //Google texture
        //return textureFilter.filterRGB(column + x, row + y, rgb);

        //None Tiled texture
        //return textureFilter.filter(x, y, rgb, column, row);

        //TMS texture
        return super.filterRGB(column + x, row - y, rgb);
    }

    /**
     * = Texture "dilatation" Small texture scale (x 0.5) for lower zooms (big
     * cartographic scale) Big texture scale (x 5) for big zooms (small
     * cartographic scale)
     *
     * @param z
     * @return
     */
    public float adaptZoom(float z) {
        return (float) ((z + 1) / 4);
    }

    /**
     * = Texture generalization Big texture scale (x 5) for lower zooms (big
     * cartographic scale) Small texture scale (x 0.5) for big zooms (small
     * cartographic scale)
     *
     * @param z
     * @return
     */
    public float adaptZoom2(float z) {
        return (float) ((z - 20) / -4);
    }

    /**
     * This method adapts the texture colormap depending on a thematic information.
     * FIXME Values and corresponding Colormaps should be given as input parameters.
     * 
     * @param x Horizontal coordinate of the pixel
     * @param y Vertical coordinate of the pixel
     */
    public void adaptColorToThematicData(int x, int y) {
        int value = thematicImage.getRGB(x, y);
        if (value == -3618161) {
            this.setColormap(new LinearColormap(0xff3A9D23, 0xff095228));
        } else if (value == -3684409) {
            this.setColormap(new LinearColormap(0xff77B5FE, 0xff318CE7));
        } else if (value == -328966) {
            this.setColormap(new LinearColormap(0xffFCDC12, 0xffE7A854));
        } else {
            this.setColormap(new LinearColormap(0xffffffff, 0xff000000));
        }
    }

    /**
     * This method creates a image sample of the adaptive texture.
     * Elevation is varying vertically.
     *
     * @return An image sample of the adaptive texture.
     */
    public BufferedImage createAdaptiveSample(float[] steps) {
        BufferedImage textureImage = new BufferedImage(100, (int) elevations[elevations.length - 1] + 100, BufferedImage.TYPE_INT_ARGB);
        Grid g = new Grid(100, (int) elevations[elevations.length - 1] + 100, 50);
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < (int) elevations[elevations.length - 1] + 100; j++) {
                g.setValue(j, i, j);
            }
        }
        this.setElevation(g);
        this.setThematicImage(null);

        textureImage = this.filter(textureImage, null);

        BufferedImage image = new BufferedImage(200, (int) elevations[elevations.length - 1] + 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.drawImage(textureImage, 0, 0, null);
        g2d.setColor(Color.RED);
        for (float e : elevations) {
            g2d.drawLine(100, (int) e, 200, (int) e);
            g2d.drawString(String.valueOf(e), 120, (int) e);
        }
        g2d.drawString(String.valueOf(steps[0]), 120, elevations[0] / 2);
        for (int i = 1; i < steps.length - 1; i++) {
            g2d.drawString(String.valueOf(steps[i]), 120, (elevations[i * 2] + elevations[i * 2 - 1]) / 2);

        }
        g2d.drawString(String.valueOf(steps[steps.length - 1]), 120, elevations[elevations.length - 1] + 50);

        return image;
    }

    /**
     * This method adapts the texture colormap depending on the elevation. The
     * color is calculated or interpolated for each pixel.
     *
     * @param x Horizontal coordinate of the pixel
     * @param y Vertical coordinate of the pixel
     */
    public void adaptColorToElevation(int x, int y) {
        float value = elevation.getValue(x, y);
        if (value <= elevations[0]) {
            this.setColormap(colorMaps[0]);
        }
        for (int i = 0; i < elevations.length - 1; i = i + 2) {
            if (value > elevations[i] && value <= elevations[i + 1]) {

                LinearColormap interpolatedColorMap = createInterpolateColormap(
                        colorMaps[i / 2], colorMaps[i / 2 + 1],
                        elevations[i], elevations[i + 1],
                        value);
                this.setColormap(interpolatedColorMap);
            } else if ((i + 1 < elevations.length - 1) && value > elevations[i + 1] && value <= elevations[i + 2]) {
                this.setColormap(colorMaps[(i + 2) / 2]);
            } else if (value > elevations[i + 1]) {
                this.setColormap(colorMaps[(i + 2) / 2]);
            }
        }

    }

    /**
     * This method adapts the texture stretch depending on the elevation. The
     * stretch value is alternatively calculated or interpolated for each pixel.
     * FIXME This method so far do not work with tiled textures.
     * 
     * @param x Horizontal coordinate of the pixel
     * @param y Vertical coordinate of the pixel
     */
    public void adaptLodToElevation(int x, int y) {
        float value = elevation.getValue(x, y);
        float stretch = 0f;
        if (value <= elevations[0]) {
            stretch = stretchs[0];
        }
        for (int i = 0; i < elevations.length - 1; i = i + 2) {
            if (value > elevations[i] && value <= elevations[i + 1]) {
                stretch = interpolateStretch(stretchs[i / 2], stretchs[i / 2 + 1],
                        elevations[i], elevations[i + 1], value);
            } else if ((i + 1 < elevations.length - 1) && value > elevations[i + 1] && value <= elevations[i + 2]) {
                stretch = stretchs[(i + 2) / 2];

            } else if (value > elevations[i + 1]) {
                stretch = stretchs[(i + 2) / 2];

            }
        }
        this.setScale(stretch);

    }

    /**
     * This method adapts the texture orientation depending on the elevation. The
     * stretch value is alternatively calculated or interpolated for each pixel.
     * FIXME This method so far do not work with tiled textures.
     * FIXME This method should further be used with orientation grids 
     * (it does not make sense to use it with elevation data, it was just to test if it work).
     * 
     * @param x Horizontal coordinate of the pixel
     * @param y Vertical coordinate of the pixel
     */
    public void adaptOrientation2(int x, int y) {
        float value = elevation.getValue(x, y);
        float angle = 0f;
        if (value <= elevations[0]) {
            angle = angleSteps[0];
        }
        for (int i = 0; i < elevations.length - 1; i = i + 2) {
            if (value > elevations[i] && value <= elevations[i + 1]) {
                angle = interpolateStretch(angleSteps[i / 2], angleSteps[i / 2 + 1],
                        elevations[i], elevations[i + 1], value);
            } else if ((i + 1 < elevations.length - 1) && value > elevations[i + 1] && value <= elevations[i + 2]) {
                angle = angleSteps[(i + 2) / 2];

            } else if (value > elevations[i + 1]) {
                angle = angleSteps[(i + 2) / 2];

            }
        }
        this.setAngle(angle);
        this.setStretch(1f);
    }

    /**
     * This method create an interpolated colormap for the texture.
     *
     * @param l1 First LinearColormap
     * @param l2 Second LinearColormap
     * @param z1 First elevation value
     * @param z2 Second elevation value
     * @param value Current elevation value
     * @return
     */
    public LinearColormap createInterpolateColormap(
            LinearColormap l1, LinearColormap l2, float z1, float z2, float value) {
        Color c1 = interpolateColor(
                new Color(l1.getColor1()), new Color(l2.getColor1()),
                z1, z2, value);
        Color c2 = interpolateColor(
                new Color(l1.getColor2()), new Color(l2.getColor2()),
                z1, z2, value);
        return new LinearColormap(c1.getRGB(), c2.getRGB());
    }

    /**
     * Linear interpolation between two Colors, for given elevation values 
     * and the current elevation for which the Color has to be interpolated.
     * 
     * @param c1 Color 1
     * @param c2 Color 2
     * @param z1 Elevation 1
     * @param z2 Elevation 2
     * @param value Current elevation value
     * @return Interpolated Color
     */
    public Color interpolateColor(Color c1, Color c2, float z1, float z2, float value) {
        float ratio = (value - z1) / (z2 - z1);
        int r = (int) (ratio * (c2.getRed() - c1.getRed()) + c1.getRed());
        int v = (int) (ratio * (c2.getGreen() - c1.getGreen()) + c1.getGreen());
        int b = (int) (ratio * (c2.getBlue() - c1.getBlue()) + c1.getBlue());
        return new Color(r, v, b);
    }

    /**
     * Linear interpolation between two stretch values, for given elevation values 
     * and the current elevation for which the stretch has to be interpolated.
     * 
     * @param s1 Stretch value 1
     * @param s2 Stretch value 2
     * @param z1 Elevation 1
     * @param z2 Elevation 2
     * @param value Current elevation value
     * @return Interpolated stretch value
     */
    public float interpolateStretch(
            float s1, float s2, float z1, float z2, float value) {
        float ratio = (value - z1) / (z2 - z1);
        float stretch = ratio * (s2 - s1) + s1;
        return stretch;
    }

    /**
     * This method get the color of the given pixel on the orthoimagery, 
     * build a LinearColormap with this color and a brighter one, 
     * and set the new Colormap to this texture filter.
     * 
     * @param x Horizontal coordinate of the pixel
     * @param y Vertical coordinate of the pixel
     */
    public void generateColorFromOrtho(int x, int y) {
        int[] coordinates = new int[4];
        orthoImage.getData().getPixel(x, y, coordinates);
        Color c = new Color(coordinates[0], coordinates[1], coordinates[2]);

        this.setColormap(new LinearColormap(c.getRGB(), c.brighter().brighter().getRGB()));
    }

    public static void main(String[] args) throws IOException {
        generateTiledTextureSamples();
    }

    /**
     * Method for generate different examples of tiles textures, 
     * testing available parameters
     */
    public static void generateTiledTextureSamples() {
        TextureFilter textureFilter = new TextureFilter();
        textureFilter.setColormap(new LinearColormap(0xff3A9D23, 0xff095228));
        TiledTextureFilter.testTexture(textureFilter);


        TiledTextureFilter ttf = new TiledTextureFilter(0, 0);
        ttf.scaleAdaptive = false;

        ttf.setColormap(new LinearColormap(0xff3A9D23, 0xff095228));
        TiledTextureFilter.testTiledTexture(ttf);

        ttf.setColormap(new LinearColormap(0xff77B5FE, 0xff318CE7));
        TiledTextureFilter.testTiledTexture(ttf);

        ttf.setColormap(new LinearColormap(0xffFCDC12, 0xffE7A854));
        TiledTextureFilter.testTiledTexture(ttf);

        ttf.setColormap(new Gradient(
                new int[]{0, 150, 250},
                new int[]{0xff3A9D23, 0xff77B5FE, 0xffFCDC12},
                new byte[]{
            Gradient.HUE_CW | Gradient.SPLINE,
            Gradient.HUE_CW | Gradient.SPLINE,
            Gradient.HUE_CW | Gradient.SPLINE}));
        TiledTextureFilter.testTiledTexture(ttf);

        ttf.setColormap(new Gradient(
                new int[]{-1, 10, 256},
                new int[]{0xff3A9D23, 0xff77B5FE, 0xffFCDC12},
                new byte[]{
            Gradient.HUE_CW | Gradient.SPLINE,
            Gradient.HUE_CW | Gradient.SPLINE,
            Gradient.HUE_CW | Gradient.SPLINE}));
        TiledTextureFilter.testTiledTexture(ttf);

        ttf.setColormap(new Gradient(
                new int[]{-1, 100, 200, 256},
                new int[]{0xff3A9D23, 0xff095228, 0xff3A9D23, 0xff095228},
                new byte[]{
            Gradient.CONSTANT | Gradient.LINEAR,
            Gradient.CONSTANT | Gradient.LINEAR,
            Gradient.CONSTANT | Gradient.LINEAR,
            Gradient.CONSTANT | Gradient.LINEAR,}));
        TiledTextureFilter.testTiledTexture(ttf);

        ttf.setColormap(new Gradient(
                new int[]{-1, 100, 150, 180, 200, 256},
                new int[]{0xff3A9D23, 0xff095228, 0xff3A9D23, 0xff095228, 0xff3A9D23, 0xff095228},
                new byte[]{
            Gradient.RGB | Gradient.LINEAR,
            Gradient.RGB | Gradient.LINEAR,
            Gradient.RGB | Gradient.LINEAR,
            Gradient.RGB | Gradient.LINEAR,
            Gradient.RGB | Gradient.LINEAR,
            Gradient.RGB | Gradient.LINEAR,}));
        TiledTextureFilter.testTiledTexture(ttf);

        ttf.setColormap(new Gradient());
        ttf.setStretch(0.8f);
        ttf.setScale(0.6f);
        TiledTextureFilter.testTiledTexture(ttf);
    }

    /**
     * Static method useful to test the generation of texture 
     * with a classic texture filter from jery huxtable.
     * @param ttf 
     */
    public static void testTexture(PointFilter textureFilter) {
        BufferedImage texturedImage1 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        texturedImage1 = textureFilter.filter(texturedImage1, null);

        BufferedImage texturedImage2 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        texturedImage2 = textureFilter.filter(texturedImage2, null);

        BufferedImage texturedImage3 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        texturedImage3 = textureFilter.filter(texturedImage3, null);

        BufferedImage texturedImage4 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        texturedImage4 = textureFilter.filter(texturedImage4, null);

        BufferedImage texturedImage5 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        texturedImage5 = textureFilter.filter(texturedImage5, null);

        BufferedImage texturedImage6 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        texturedImage6 = textureFilter.filter(texturedImage6, null);

        BufferedImage texturedImage7 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        texturedImage7 = textureFilter.filter(texturedImage7, null);

        BufferedImage texturedImage8 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        texturedImage8 = textureFilter.filter(texturedImage8, null);

        BufferedImage texturedImage9 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        texturedImage9 = textureFilter.filter(texturedImage9, null);

        BufferedImage result = new BufferedImage(256 * 3, 256 * 3, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(texturedImage1, 0, 0, null);
        g2d.drawImage(texturedImage2, 256, 0, null);
        g2d.drawImage(texturedImage3, 256 * 2, 0, null);
        g2d.drawImage(texturedImage4, 0, 256, null);
        g2d.drawImage(texturedImage5, 256, 256, null);
        g2d.drawImage(texturedImage6, 256 * 2, 256, null);
        g2d.drawImage(texturedImage7, 0, 256 * 2, null);
        g2d.drawImage(texturedImage8, 256, 256 * 2, null);
        g2d.drawImage(texturedImage9, 256 * 2, 256 * 2, null);
        ika.utils.ImageUtils.displayImageInWindow(result);
    }

    /**
     * Static method useful to test the generation of tiled texture using this class.
     * @param ttf 
     */
    public static void testTiledTexture(TiledTextureFilter ttf) {
        ttf.setColumnRow(0, 0);
        BufferedImage texturedImage1 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        texturedImage1 = ttf.filter(texturedImage1, null);

        BufferedImage texturedImage2 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        ttf.setColumnRow(256, 0);
        texturedImage2 = ttf.filter(texturedImage2, null);

        BufferedImage texturedImage3 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        ttf.setColumnRow(256 * 2, 0);
        texturedImage3 = ttf.filter(texturedImage3, null);

        BufferedImage texturedImage4 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        ttf.setColumnRow(0, 256);
        texturedImage4 = ttf.filter(texturedImage4, null);

        BufferedImage texturedImage5 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        ttf.setColumnRow(256, 256);
        texturedImage5 = ttf.filter(texturedImage5, null);

        BufferedImage texturedImage6 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        ttf.setColumnRow(256 * 2, 256);
        texturedImage6 = ttf.filter(texturedImage6, null);

        BufferedImage texturedImage7 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        ttf.setColumnRow(0, 256 * 2);
        texturedImage7 = ttf.filter(texturedImage7, null);

        BufferedImage texturedImage8 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        ttf.setColumnRow(256, 256 * 2);
        texturedImage8 = ttf.filter(texturedImage8, null);

        BufferedImage texturedImage9 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        ttf.setColumnRow(256 * 2, 256 * 2);
        texturedImage9 = ttf.filter(texturedImage9, null);

        BufferedImage result = new BufferedImage(256 * 3, 256 * 3, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(texturedImage1, 0, 0, null);
        g2d.drawImage(texturedImage2, 256, 0, null);
        g2d.drawImage(texturedImage3, 256 * 2, 0, null);
        g2d.drawImage(texturedImage4, 0, 256, null);
        g2d.drawImage(texturedImage5, 256, 256, null);
        g2d.drawImage(texturedImage6, 256 * 2, 256, null);
        g2d.drawImage(texturedImage7, 0, 256 * 2, null);
        g2d.drawImage(texturedImage8, 256, 256 * 2, null);
        g2d.drawImage(texturedImage9, 256 * 2, 256 * 2, null);
        ika.utils.ImageUtils.displayImageInWindow(result);
    }
    
    
    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }
    
    public BufferedImage getThematicImage() {
        return thematicImage;
    }

    public void setThematicImage(BufferedImage thematicImage) {
        this.thematicImage = thematicImage;
    }

    public BufferedImage getOrthoImage() {
        return orthoImage;
    }

    public void setOrthoImage(BufferedImage orthoImage) {
        this.orthoImage = orthoImage;
    }

    public Grid getElevation() {
        return elevation;
    }

    public void setElevation(Grid elevation) {
        this.elevation = elevation;
    }
    
    public float[] getAngleSteps() {
        return angleSteps;
    }

    /**
     * This part of the code could be added again to work on the adaptation 
     * of the scale of the texture with the zoom level, 
     * using adaptToZoom and adaptZoom2 methods.
     */
//    private float zoom = 1;
//    public float getZoom() {
//        return zoom;
//    }
//    public void setZoom(float zoom) {
//        this.zoom = zoom;
//    }
    
//    private float referenceScale;
//    public float getReferenceScale() {
//        return referenceScale;
//    }
//
//    public void setReferenceScale(float referenceScale) {
//        this.referenceScale = referenceScale;
//        this.setScale(referenceScale);
//    }
}