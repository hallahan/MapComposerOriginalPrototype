package edu.oregonstate.carto.mapcomposer.texture;

import ika.utils.FileUtils;
import edu.oregonstate.carto.imageCollection.TileGenerator;
import edu.oregonstate.carto.imageCollection.TiledDTM;
import edu.oregonstate.carto.imageCollection.TiledImage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Charlotte Hoarau, COGIT Laboratory, IGN France
 */
public class TiledTextureProvider implements TileGenerator {

    private TiledTextureFilter ttf = null;
    private TiledImage resultImage = null;
    
    public TiledTextureFilter getTTF() {
        return this.ttf;
    }
    
    public TiledTextureProvider(TiledTextureFilter ttf, TiledImage resultImage) {
        this.ttf = ttf;
        this.resultImage = resultImage;
    }
    
    private TiledImage thematicImage;
    
    public TiledImage getThematicImage() {
        return thematicImage;
    }

    public void setThematicImage(TiledImage thematicImage) {
        this.thematicImage = thematicImage;
    }
    
    public TiledDTM dem;

    public TiledDTM getDem() {
        return dem;
    }

    public void setDem(TiledDTM dem) {
        this.dem = dem;
    }
    
    private TiledImage orthoImage;

    public TiledImage getOrthoImage() {
        return orthoImage;
    }

    public void setOrthoImage(TiledImage orthoImage) {
        this.orthoImage = orthoImage;
    }
    
    @Override
    public BufferedImage createTile(String tileName) {
        
        BufferedImage textureImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        
        if (thematicImage != null) {
            try {
                ttf.setThematicImage(thematicImage.loadImage(ika.utils.FileUtils.replaceExtension(tileName, "png")));
            } catch (MalformedURLException ex) {
                Logger.getLogger(TiledTextureProvider.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(TiledTextureProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (dem != null) {
            try {
                //FIXME It can be .asc file !
                ttf.setElevation(dem.loadDTM(ika.utils.FileUtils.replaceExtension(tileName, "bil")));
            } catch (IOException ex) {
                Logger.getLogger(TiledTextureProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (orthoImage != null) {
            try {
                ttf.setOrthoImage(orthoImage.loadImage(ika.utils.FileUtils.replaceExtension(tileName, "png")));
            } catch (IOException ex) {
                Logger.getLogger(TiledTextureProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        int col = Integer.parseInt(resultImage.colForImageName(tileName));
        int row = Integer.parseInt(FileUtils.getFileNameWithoutExtension(resultImage.rowForImageName(tileName)));

        ttf.setColumnRow(col * 256, row * 256);
        
        textureImage = ttf.filter(textureImage, null);
        return textureImage;
    }
    
}
