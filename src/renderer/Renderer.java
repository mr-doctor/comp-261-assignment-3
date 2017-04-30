package renderer;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
/**
 * Takes a file describing a model from a number of polygons, 
 * 		and then renders that model in 3D.
 * 
 * @author Daniel Pinfold
 *
 */
public class Renderer extends GUI {
	
	ArrayList<Color> directLightColours = new ArrayList<>();
	
	ArrayList<Vector3D> directLightSources = new ArrayList<>();
	
	private Scene scene;
	
	protected void onLoad(File file) throws IOException {
		List<String> allLines = Files.readAllLines(file.toPath());
		List<Scene.Polygon> polys = new ArrayList<>();
		
		String splitData[] = allLines.get(0).split(" ");
		float[] lightVector = new float[3];
		
		for (int i = 0; i < lightVector.length; i++) {
			lightVector[i] = Float.parseFloat(splitData[i]);
		}
		Vector3D lightSource = new Vector3D(lightVector[0], lightVector[1], lightVector[2]);
		
		for (int i = 1; i < allLines.size(); i++) {
			splitData = allLines.get(i).split(" ");
			
			float[] polyVertices = new float[9];
			int[] reflectance = new int[3];
			
			// for calculating the polygon vertices.
			for (int j = 0; j < 9; j++) {
				polyVertices[j] = Float.parseFloat(splitData[j]);
			}
			// for calculating the polygon reflectance.
			for (int j = 9; j < splitData.length; j++) {
				reflectance[j - 9] = Integer.parseInt(splitData[j]);
			}
			polys.add(new Scene.Polygon(polyVertices, reflectance));
		}
		
		// adds the starting light source and colour
		directLightSources.add(lightSource);
		directLightColours.add(new Color(100, 100, 100));
		
		scene = new Scene(polys, directLightSources);
	}
	
    protected void onKeyPress(KeyEvent ev) {
    	// WASD and arrow keys can be used interchangeably
        if(ev.getKeyCode() == KeyEvent.VK_LEFT || ev.getKeyCode() == KeyEvent.VK_A){
            scene = Pipeline.rotateScene(scene, 0,(float) (-0.1*Math.PI));
            
        }else if(ev.getKeyCode() == KeyEvent.VK_RIGHT || ev.getKeyCode() == KeyEvent.VK_D){
            scene = Pipeline.rotateScene(scene, 0,(float) (0.1*Math.PI));
        
        }else if(ev.getKeyCode() == KeyEvent.VK_UP|| ev.getKeyCode() == KeyEvent.VK_W){
            scene = Pipeline.rotateScene(scene, (float) (0.1*Math.PI), 0);
        
        }else if(ev.getKeyCode() == KeyEvent.VK_DOWN || ev.getKeyCode() == KeyEvent.VK_S){
            scene = Pipeline.rotateScene(scene, (float) (-0.1*Math.PI), 0);
        }
    }
    
    @Override
    protected void addNewLightSource() {
    	// makes the colour be completely random on RGB, between 0 and 255.
    	directLightColours.add(new Color(
    			(int)(Math.random()*255), 
    			(int)(Math.random()*255), 
    			(int)(Math.random()*255)));
    	// makes the point be completely random, each component being between -1 and 1.
    	directLightSources.add(new Vector3D(
    			(float) (Math.random() - Math.random()),
    			(float) (Math.random() - Math.random()),
    			(float) (Math.random() - Math.random())));
    }
    

	@Override
	protected void removeLightSource() {
		if (directLightColours.size() > 0) {
			directLightColours.remove(directLightColours.size()-1);
			directLightSources.remove(directLightSources.size()-1);
		}
	}
	
	@Override
	protected BufferedImage render() {
		if (scene == null) {
			return null;
		}
		
		scene = Pipeline.translateScene(scene);
		scene = Pipeline.scaleScene(scene);
		Color[][] zBuffer = new Color[CANVAS_WIDTH][CANVAS_HEIGHT];
		float[][] zDepth = new float[CANVAS_WIDTH][CANVAS_HEIGHT];
		
		EdgeList edges;

		for (int x = 0; x < CANVAS_WIDTH; x++) {
			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				zBuffer[x][y] = Color.white;
				zDepth[x][y] = Float.POSITIVE_INFINITY;
			}
		}
		
		for (Scene.Polygon p : scene.getPolygons()) {
			// determines if the polygon should be rendered.
			if (!Pipeline.isHidden(p)) {
				// gets the display colour of the polygon.
				Color c = Pipeline.getShading(
						p, 
						scene.getLights(), 
						directLightColours,
						new Color(
								getAmbientLight()[0], 
								getAmbientLight()[1], 
								getAmbientLight()[2]));
				// gets the edgelist of the polygon.
				edges = Pipeline.computeEdgeList(p);
				// adds the polygon's zBuffer to the total zBuffer.
				Pipeline.computeZBuffer(zBuffer, zDepth, edges, c);
			}
		}
		
		return convertBitmapToImage(zBuffer);
	}
	
	/**
	 * Converts a 2D array of Colors to a BufferedImage. Assumes that bitmap is
	 * indexed by column then row and has imageHeight rows and imageWidth
	 * columns. Note that image.setRGB requires x (col) and y (row) are given in
	 * that order.
	 */
	private BufferedImage convertBitmapToImage(Color[][] bitmap) {
		BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < CANVAS_WIDTH; x++) {
			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				image.setRGB(x, y, bitmap[x][y].getRGB());
			}
		}
		return image;
	}

	public static void main(String[] args) {
		new Renderer();
	}

}

// code for comp261 assignments
