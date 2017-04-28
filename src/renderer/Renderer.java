package renderer;

import java.awt.Color;
import java.awt.RenderingHints.Key;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Renderer extends GUI {
	
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
			if (splitData.length != 12)
				throw new IllegalArgumentException();
			float[] polyVertices = new float[9];
			int[] reflectance = new int[3];
			for (int j = 0; j < 9; j++) {
				polyVertices[j] = Float.parseFloat(splitData[j]);
			}
			for (int j = 9; j < splitData.length; j++) {
				reflectance[j - 9] = Integer.parseInt(splitData[j]);
			}
			polys.add(new Scene.Polygon(polyVertices, reflectance));
		}
		scene = new Scene(polys, lightSource);
	}

    protected void onKeyPress(KeyEvent ev) {
        // TODO fill this in.

        /*
         * This method should be used to rotate the user's viewpoint.
         */

        if(ev.getKeyCode() == KeyEvent.VK_LEFT){
            scene = Pipeline.rotateScene(scene, 0,(float) (-0.1*Math.PI));
            
        }else if(ev.getKeyCode() == KeyEvent.VK_RIGHT){
            scene = Pipeline.rotateScene(scene, 0,(float) (0.1*Math.PI));
        
        }else if(ev.getKeyCode() == KeyEvent.VK_UP){
            scene = Pipeline.rotateScene(scene, (float) (0.1*Math.PI),0);
        
        }else if(ev.getKeyCode() == KeyEvent.VK_DOWN){
            scene = Pipeline.rotateScene(scene, (float) (-0.1*Math.PI),0);
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

		for (int i = 0; i < CANVAS_WIDTH; i++) {
			for (int j = 0; j < CANVAS_HEIGHT; j++) {
				zBuffer[i][j] = Color.white;
				zDepth[i][j] = Float.POSITIVE_INFINITY;
			}
		}
		
		for (Scene.Polygon p : scene.getPolygons()) {
			if (!Pipeline.isHidden(p)) {
				Color c = Pipeline.getShading(
						p, 
						scene.getLight(), 
						p.getReflectance(), 
						new Color(
								getAmbientLight()[0], 
								getAmbientLight()[1], 
								getAmbientLight()[2]));
				edges = Pipeline.computeEdgeList(p);
				Pipeline.computeZBuffer(zBuffer, zDepth, edges, c);
			}
		}
		/*
		 * This method should put together the pieces of your renderer, as
		 * described in the lecture. This will involve calling each of the
		 * static method stubs in the Pipeline class, which you also need to
		 * fill in.
		 */
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
