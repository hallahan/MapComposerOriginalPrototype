package edu.oregonstate.carto.mapcomposer.texture;

import ika.utils.FileUtils;
import edu.oregonstate.carto.imageCollection.TiledDTM;
import edu.oregonstate.carto.imageCollection.TiledImage;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import edu.oregonstate.carto.mapcomposer.texture.style.Texture;

/**
 * This class is useful to generate adaptive textures.
 * FIXME It should be replaced by a nice user interface ...
 * 
 * @author Charlotte Hoarau, COGIT Laboratory, IGN France
 */
public class MainTextureRenderer {
    private static final String HTML_PREVIEW_FILE = "Local_Tiles_TMS.html";
    
    public static void main(String[] args) throws IOException, InterruptedException {

        //---------- Creating Result directory
        File resultDirectory = new File("/Volumes/FileServer/MapComposer/MapComposer/data/results/Mount_Hood_ColorFromOrtho_Forest");
        org.apache.commons.io.FileUtils.forceDelete(resultDirectory);
        org.apache.commons.io.FileUtils.forceMkdir(resultDirectory);
        TiledImage result = new TiledImage(resultDirectory.getAbsolutePath());
        
        Texture forestTexture = Texture.unmarshal("/Volumes/FileServer/MapComposer/MapComposer/data/tiledTextures/forest2/forest2.xml");
        TiledTextureFilter filter = new TiledTextureFilter(forestTexture);
        filter.setAmount(1.0f);
//        TiledImage thematicImage = new TiledImage("/Volumes/FileServer/MapComposer/MapComposer/data/fake_thematic_data/zoom11");
        TiledDTM dem = new TiledDTM("/Volumes/FileServer/Oregon_Data/DEM/Tiled_DEM10m");
//        TiledImage ortho = new TiledImage("/Volumes/FileServer/Oregon_Data/Orthoimagery/Mount_Hood/Tiled_Orthoimages");
        
        TiledTextureProvider generator = new TiledTextureProvider(filter, result);
//        generator.setThematicImage(thematicImage);
        generator.setDem(dem);
//        generator.setOrthoImage(ortho);
        
        // FIXME
        File structureDirectory = new File("/Volumes/FileServer/Oregon_Data/Orthoimagery/Mount_Hood/Tiled_Orthoimages");
                
        System.out.println("---- Starting Rendering the texture ----");
        result.createTiledImage(structureDirectory, true, generator, null);
        
        System.out.println("---- Saving Texture parameters ----");
        Texture texture = new Texture(filter);
        File xml = new File(resultDirectory, FileUtils.getFileNameWithoutExtension(resultDirectory.getName()));
        texture.marshal(xml.getAbsolutePath() + ".xml");
            
        System.out.println("---- Creating sample ----");
        filter.createAdaptiveSample(filter.getAngleSteps());
        
        System.out.println("---- Displaying result in a webpage ----");
        URL inputUrl = MainTextureRenderer.class.getResource("/" + HTML_PREVIEW_FILE);
        File dest = new File(resultDirectory.getAbsolutePath() + "/Local_Tiles_TMS.html");
        org.apache.commons.io.FileUtils.copyURLToFile(inputUrl, dest);
        Desktop.getDesktop().browse(dest.toURI());
        
        System.out.println("---- The End ----");
    }
    
}
