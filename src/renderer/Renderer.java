package renderer;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Renderer extends GUI {
	
	private Scene currentScene;
	
	@Override
	protected void onLoad(File file) throws IOException {
		/*
		 * This method should parse the given file into a Scene object, which
		 * you store and use to render an image.
		 */
		List<Scene.Polygon> polygons = new ArrayList<>();
		
		List<String> sceneData = Files.readAllLines(file.toPath());
		
		String[] splitFirstLine = sceneData.get(0).split(" ");
		Vector3D lightSource = new Vector3D(
				Float.parseFloat(splitFirstLine[0]), 
				Float.parseFloat(splitFirstLine[1]), 
				Float.parseFloat(splitFirstLine[2]));
		
		for (int i=1; i<sceneData.size(); i++) {
			String[] splitData = sceneData.get(i).split(" ");
			Vector3D a = new Vector3D(
					Float.parseFloat(splitData[0]), 
					Float.parseFloat(splitData[1]), 
					Float.parseFloat(splitData[2]));
			Vector3D b = new Vector3D(
					Float.parseFloat(splitData[3]),
					Float.parseFloat(splitData[4]), 
					Float.parseFloat(splitData[5]));
			Vector3D c = new Vector3D(
					Float.parseFloat(splitData[6]), 
					Float.parseFloat(splitData[7]), 
					Float.parseFloat(splitData[8]));
			
			Color reflectance = new Color(
					Integer.parseInt(splitData[9]), 
					Integer.parseInt(splitData[10]), 
					Integer.parseInt(splitData[11]));
			
			polygons.add(new Scene.Polygon(a, b, c, reflectance));
		}
		currentScene = new Scene(polygons, lightSource);
	}

	@Override
	protected void onKeyPress(KeyEvent ev) {
		// TODO fill this in.

		/*
		 * This method should be used to rotate the user's viewpoint.
		 */
	}

	@Override
	protected BufferedImage render() {
		if (currentScene != null) {
			for (Scene.Polygon p : currentScene.getPolygons()) {
				Pipeline.getShading(p, currentScene.getLight(), new Color(255, 0, 0), new Color(0, 0, 255));
			}
		}
		/*
		 * This method should put together the pieces of your renderer, as
		 * described in the lecture. This will involve calling each of the
		 * static method stubs in the Pipeline class, which you also need to
		 * fill in.
		 */
		return null;
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
