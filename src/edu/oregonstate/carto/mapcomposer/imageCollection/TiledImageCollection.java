package edu.oregonstate.carto.mapcomposer.imageCollection;

import edu.oregonstate.carto.utilities.DirectoryUtils;
import edu.oregonstate.carto.imageCollection.TiledImage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

/**
 *
 * @author Charlotte Hoarau - COGIT Laboratory, IGN France.
 */
public class TiledImageCollection implements ImageCollection {
    
    private File directory;
    
    public File getDirectory() {
        return directory;
    }
    
    // FIXME bad hack! - this should not be required!
    public File getRefDir() {
        return getLayers().get(0).getDirectory();
    }

    private ArrayList<TiledImage> layers = new ArrayList<TiledImage>();
    
    public ArrayList<TiledImage> getLayers() {
        return layers;
    }
    
    public TiledImageCollection(String directoryPath) {
        this.directory = new File(directoryPath);
        if (!this.directory.isDirectory())
            throw new IllegalArgumentException("not a directory");
        
        for (File folder : this.directory.listFiles(DirectoryUtils.DIRECTORY_FILTER)) {
            layers.add(new TiledImage(folder.getAbsolutePath()));
        }
    }
    
    public TiledImage getLayer(String name) { 
        for (TiledImage tiledImage : layers) {
            if (tiledImage.getDirectory().getName().equalsIgnoreCase(name)) {
                return tiledImage;
            }
        }
        return null;
    }
    
    /**
     * For tiled images, this method returns the name of the tile sets.
     * @return 
     */
    @Override
    public String[] getImageNames() {
          return this.directory.list(DirectoryUtils.DIRECTORY_FILTER);
    }
    
    //TODO Faire une methode urlForImageName(imageName, layer)
    //pour ne pas chercher le layer
    @Override
    public String urlForImageName(String imageName) {
        if (imageName == null)
            return null;
        
        String tileSet = imageName.split("[_]")[0];

        for (TiledImage tiledImage : layers) {
            if (tileSet.equalsIgnoreCase(tiledImage.getDirectory().getName())) {
                return tiledImage.urlForImageName(imageName);
            }
        }
        return null;
    }
    
    public String urlForTileSetName(String tileSetName) {
        
        if (tileSetName == null)
            return null;
        
        String[] imageNames = this.getImageNames();
        for (String image : imageNames) {
            if (image.equals(tileSetName)) {
                String filePath = new File(directory, tileSetName).getAbsolutePath();
                return filePath;
            }
        }
        return tileSetName;
        
    }
    
    public String tileSetForImageName(String imageName){
        if (imageName == null)
            return null;
        
        return imageName.split("[_]")[0];
    }
    
    //TODO Method loadImage(imageName, layer) pour ne pas chercher le layer
    @Override
    public BufferedImage loadImage(String imageName) throws MalformedURLException, IOException {
        if (imageName == null)
            return null;
        
        String tileSet = imageName.split("[_]")[0];
        
        // FIXME make this more efficient
        for (TiledImage tiledImage : layers) {
            if (tileSet.equalsIgnoreCase(tiledImage.getDirectory().getName())) {
                String imgName = 
                        imageName.split("[_]")[1] + "_" +
                        imageName.split("[_]")[2] + "_" +
                        imageName.split("[_]")[3];
                return tiledImage.loadImage(imgName);
            }
        }
        
        return null;        
    }
    
    public BufferedImage createMegaTile(String imageName, String layerName) throws MalformedURLException, IOException {
        if (layerName == null) {
            return null;
        }
        for (TiledImage tiledImage : layers) {
            if (layerName.equalsIgnoreCase(tiledImage.getDirectory().getName())) {
                String imgName = 
                        imageName.split("[_]")[1] + "_" +
                        imageName.split("[_]")[2] + "_" +
                        imageName.split("[_]")[3];
                return tiledImage.createMegaTile(imgName);
            }
        }
        return null;
    }
    
    public BufferedImage cutTileFromMegaTile(BufferedImage imageName) {
        return this.layers.get(0).cutTileFromMegaTile(imageName);
    }
    
    @Override
    public boolean isValidImageURL(String imageName) {
        //FIXME
        return true;
    };
}
