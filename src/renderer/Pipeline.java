package renderer;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import renderer.Scene.Polygon;

/**
 * The Pipeline class has method stubs for all the major components of the
 * rendering pipeline, for you to fill in.
 * 
 * Some of these methods can get quite long, in which case you should strongly
 * consider moving them out into their own file. You'll need to update the
 * imports in the test suite if you do.
 */
public class Pipeline {

	/**
	 * Returns true if the given polygon is facing away from the camera (and so
	 * should be hidden), and false otherwise. Effectively determines whether or not the
	 * polygon should be rendered.
	 * 
	 * @param poly
	 * 			The polygon being calculated.
	 * @return whether or not the z-component of the unit normal is greater than 0
	 * 			i.e. if it is pointing away from the camera.
	 */
	public static boolean isHidden(Polygon poly) {
		return (getUnitNormal(poly).z > 0);
	}

	/**
	 * Computes the colour of a polygon on the screen, once the lights, their
	 * angles relative to the polygon's face, and the reflectance of the polygon
	 * have been accounted for.
	 * 
	 * @param lightDirection
	 *            The Vector3D pointing to the directional light read in from
	 *            the file.
	 * @param lightColor
	 *            The color of that directional light.
	 * @param ambientLight
	 *            The ambient light in the scene, i.e. light that doesn't depend
	 *            on the direction.
	 * @return the total shading of the polygon, as a colour.
	 */
	public static Color getShading(Polygon poly, Vector3D lightDirection, Color lightColor, Color ambientLight) {
		Vector3D unitNormal = getUnitNormal(poly);
		
		Vector3D d = lightDirection.unitVector();
		Color R = poly.getReflectance();
		
		float theta = (float) (Math.acos(unitNormal.dotProduct(d)));
		
		int[] rgbO = new int[3];
		float[] rgbA = findLightIntensity(colourAsArray(ambientLight));
		float[] rgbL = findLightIntensity(colourAsArray(lightColor));
		int[] rgbR = colourAsArray(R);
		
		for (int i = 0; i < 3; i++) {
			rgbO[i] = (int) ((rgbA[i] + rgbL[i] * Math.max(0, Math.cos(theta))) * rgbR[i]);
		}
		
		return new Color(clamp(rgbO[0], 0, 255), clamp(rgbO[1], 0, 255), clamp(rgbO[2], 0, 255));
	}
	
	/**
	 * An alternate shading calculator for finding the colour from multiple light sources.
	 * Takes an array of light sources rather than a single one. Currently unused.
	 * 
	 * @param poly
	 * 			The polygon whose shading is being calculated.
	 * @param lightSources
	 * 			The directions of the light sources.
	 * @param lightColours
	 * 			The colours of the light sources.
	 * @param ambientLight
	 * 			The colour of the ambient light.
	 * @return the total shading of the polygon, given all light sources, as a colour.
	 */
	public static Color getShading(Polygon poly, ArrayList<Vector3D> lightSources, ArrayList<Color> lightColours, Color ambientLight) {
		Vector3D unitNormal = getUnitNormal(poly);
		
		Vector3D[] lightDirections = new Vector3D[lightSources.size()];

		float[] allTheta = new float[lightSources.size()];

		for (int i = 0; i < lightSources.size(); i++) {
			Vector3D d = lightSources.get(i).unitVector();
			lightDirections[i] = d;
			allTheta[i] = (float) (float) (Math.acos(unitNormal.dotProduct(d)));
		}

		float[][] lightSourceIntensities = new float[lightColours.size()][3];

		Color R = poly.getReflectance();

		int[] rgbO = new int[3];
		float[] rgbA = findLightIntensity(colourAsArray(ambientLight));
		for (int i = 0; i < lightColours.size(); i++) {
			lightSourceIntensities[i] = findLightIntensity(colourAsArray(lightColours.get(i)));
		}
		int[] rgbR = colourAsArray(R);
		
		for (int i = 0; i < 3; i++) {
			float totalSourceValue = 0;
			for (int j = 0; j<allTheta.length; j++) {
				totalSourceValue += lightSourceIntensities[j][i] * Math.max(0, Math.cos(allTheta[j]));
			}
			rgbO[i] = (int) (((rgbA[i] + totalSourceValue)) * rgbR[i]);
		}
		
		return new Color(clamp(rgbO[0], 0, 255), clamp(rgbO[1], 0, 255), clamp(rgbO[2], 0, 255));
	}
	
	/**
	 * Clamps a given integer between two other integers.
	 * Used, in this case, to make sure that a colour does not exceed its colour space limits.
	 * 
	 * @param num
	 * 			The number to be clamped.
	 * @param lowerBound
	 * 			The minimum value.
	 * @param upperBound
	 * 			The maximum value.
	 * @return the number, provided it is between the two bounds.
	 */
	private static int clamp(int num, int lowerBound, int upperBound) {
		return Math.min(Math.max(num, lowerBound), upperBound);
	}
	
	/**
	 * Gets the unit normal vector of a polygon.
	 * i.e. the direction of the vector that points directly out from the face of the polygon.
	 * Used to find the difference between the light source and the polygon's face.
	 * 
	 * @param poly
	 * 			The polygon whose display colour is being calculated.
	 * @return a unit vector defining the direction of the polygon's normal
	 */
	private static Vector3D getUnitNormal(Polygon poly) {
		Vector3D a = poly.getVertices()[0], b = poly.getVertices()[1], c = poly.getVertices()[2];
		return b.minus(a).crossProduct(c.minus(b)).unitVector();
	}

	/**
	 * Converts a colour to an array without altering the values.
	 * 
	 * @param c
	 * 			A given colour.
	 * @return that same colour, split into an array of the RGB values.
	 */
	public static int[] colourAsArray(Color c) {
		int[] rgbArray = new int[3];
		rgbArray[0] = c.getRed();
		rgbArray[1] = c.getGreen();
		rgbArray[2] = c.getBlue();

		return rgbArray;
	}

	/**
	 * Converts an RGB colour to an intensity.
	 * Finds the decimal proportion of full light that a given colour is.
	 * 
	 * @param c
	 * 			An array of the RGB values of a given colour.
	 * @return an altered float array of the light intensity.
	 */
	public static float[] findLightIntensity(int[] c) {
		float[] rgbIntensity = new float[3];

		rgbIntensity[0] = (float) (c[0]) / 255;
		rgbIntensity[1] = (float) (c[1]) / 255;
		rgbIntensity[2] = (float) (c[2]) / 255;

		return rgbIntensity;
	}

	/**
	 * This method should rotate the polygons and light such that the viewer is
	 * looking down the Z-axis. The idea is that it returns an entirely new
	 * Scene object, filled with new Polygons, that have been rotated.
	 * 
	 * @param scene
	 *            The original Scene.
	 * @param xAngle
	 *            An angle describing the viewer's rotation in the YZ-plane (i.e
	 *            around the X-axis). Will be 0 if rotating around the Y-axis.
	 * @param yAngle
	 *            An angle describing the viewer's rotation in the XZ-plane (i.e
	 *            around the Y-axis). Will be 0 if rotating around the X-axis.
	 * @return A new Scene where all the polygons and the light source have been
	 *         rotated accordingly.
	 */
	public static Scene rotateScene(Scene scene, float xAngle, float yAngle) {
		if (scene == null) {
			return null;
		}
		Transform tX = Transform.newXRotation(xAngle);
		Transform tY = Transform.newYRotation(yAngle);

		List<Polygon> newPolygons = new ArrayList<>(scene.getPolygons());

		for (Scene.Polygon p : newPolygons) {
			for (int i = 0; i < p.getVertices().length; i++) {
				if (xAngle != 0.0f) {
					p.getVertices()[i] = tX.multiply(p.getVertices()[i]);
				}
				if (yAngle != 0.0f) {
					p.getVertices()[i] = tY.multiply(p.getVertices()[i]);
				}
			}
		}
		ArrayList<Vector3D> newLightSources = scene.getLights();

		for (int i=0; i<newLightSources.size(); i++) {
			if (xAngle != 0.0f) {
				newLightSources.set(i, tX.multiply(scene.getLights().get(i)));
			}
			if (yAngle != 0.0f) {
				newLightSources.set(i, tY.multiply(scene.getLights().get(i)));
			}
		}

		return new Scene(newPolygons, newLightSources);
	}

	/**
	 * This should translate the scene by the appropriate amount.
	 * Calculates the difference between the top-left corner of the object, and the origin.
	 * Translates the object by that amount.
	 * 
	 * @param scene
	 * 			The current scene.
	 * @return a translated scene.
	 */
	public static Scene translateScene(Scene scene) {
		Rectangle bBox = boundingBox(scene.getPolygons());
		
		float xDiff = -bBox.x;
		float yDiff = -bBox.y;
		
		Transform t = Transform.newTranslation(new Vector3D(xDiff, yDiff, 0));
		
		for (Scene.Polygon p : scene.getPolygons()) {
			for (int i=0; i<p.getVertices().length; i++) {
				p.getVertices()[i] = t.multiply(p.getVertices()[i]);
			}
		}
		
		return new Scene(scene.getPolygons(), scene.getLights());
	}

	/**
	 * This should scale the scene.
	 * Finds the factor by which the object exceeds the canvas size, then scales it by that amount.
	 * 
	 * @param scene
	 * 			The current scene.
	 * @return a new, scaled scene.
	 */
	public static Scene scaleScene(Scene scene) {
		Rectangle bBox = boundingBox(scene.getPolygons());
		
		float width = (float) (bBox.getWidth());
		float height = (float) (bBox.getHeight());
		
		float scaleFactor = 1;
		
		// determines whether or not the longest length of the shape is the width or height.
		boolean useWidth = (width - GUI.CANVAS_WIDTH > height - GUI.CANVAS_HEIGHT);
		
		if (width > GUI.CANVAS_WIDTH && useWidth) {
			scaleFactor = GUI.CANVAS_WIDTH / width;
		}
		if (height > GUI.CANVAS_HEIGHT && !useWidth) {
			scaleFactor = GUI.CANVAS_HEIGHT / height;
		}
		// saves processing time by stopping if the shape won't be scaled.
		if (scaleFactor == 1.0f) {
			return scene;
		}
		
		Transform t = Transform.newScale(scaleFactor, scaleFactor, scaleFactor);
		
		for (Scene.Polygon p : scene.getPolygons()) {
			for (int i=0; i<p.getVertices().length; i++) {
				p.getVertices()[i] = t.multiply(p.getVertices()[i]);
			}
		}
		
		ArrayList<Vector3D> newLights = scene.getLights();
		for (int i=0; i<newLights.size(); i++) {
			newLights.set(i, t.multiply(newLights.get(i)));
		}
		
		return new Scene(scene.getPolygons(), newLights);
	}
	
	/**
	 * Gets the bounding box of the object displayed on the screen
	 * i.e. a 2d shape that encompasses what the viewer can see.
	 * 
	 * @param polygons
	 * 			All the polygons in the current scene.
	 * @return the smallest possible rectangle that encompasses the entire object.
	 */
	public static Rectangle boundingBox(List<Scene.Polygon> polygons) {
		float minY = Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;
		
		float minX = Float.MAX_VALUE;
		float maxX = -Float.MAX_VALUE;
		
		for (Scene.Polygon poly : polygons) {
			Vector3D[] vectors = Arrays.copyOf(poly.getVertices(), 3);
			
			for (Vector3D v : vectors) {
				minY = Math.min(minY, v.y);
				maxY = Math.max(maxY, v.y);
				minX = Math.min(minX, v.x);
				maxX = Math.max(maxX, v.x);
			}
		}
		
		return new Rectangle(
				Math.round(minX), 
				Math.round(minY), 
				Math.round(maxX - minX), 
				Math.round(maxY - minY));
	}

	/**
	 * Computes the edgelist of a single provided polygon, as per the lecture
	 * slides.
	 * 
	 * @param poly
	 * 			The given polygon being rendered at the time.
	 * @return the edgelist of the polygon.
	 */
	public static EdgeList computeEdgeList(Polygon poly) {
		Vector3D[] vectors = Arrays.copyOf(poly.getVertices(), 3);
		
		int minY = Integer.MAX_VALUE;
		int maxY = -Integer.MAX_VALUE;
		
		for (Vector3D v : vectors) {
			if (v.y > maxY) {
				maxY = Math.round(v.y);
			}
			if (v.y < minY) {
				minY = Math.round(v.y);
			}
		}
		EdgeList edgeList = new EdgeList(minY, maxY);

		Vector3D a;
		Vector3D b;

		for (int i = 0; i < 3; i++) {
			a = vectors[i];
			b = vectors[(i + 1) % 3];

			float slopeX = (b.x - a.x) / (b.y - a.y);
			float slopeZ = (b.z - a.z) / (b.y - a.y);

			float x = a.x;
			int y = Math.round(a.y);
			float z = a.z;

			if (a.y < b.y) {
				while (y <= Math.round(b.y)) {
					edgeList.setLeftX(y, x);
					edgeList.setLeftZ(y, z);
					x += slopeX;
					z += slopeZ;
					y++;
				}
			} else {
				while (y >= Math.round(b.y)) {
					edgeList.setRightX(y, x);
					edgeList.setRightZ(y, z);
					x -= slopeX;
					z -= slopeZ;
					y--;
				}
			}
		}
		return edgeList;
	}

	/**
	 * Fills a zbuffer with the contents of a single edge list according to the
	 * lecture slides.
	 * 
	 * The idea here is to make zbuffer and zdepth arrays in your main loop, and
	 * pass them into the method to be modified.
	 * 
	 * @param zBuffer
	 *            A double array of colours representing the Color at each pixel
	 *            so far.
	 * @param zDepth
	 *            A double array of floats storing the z-value of each pixel
	 *            that has been coloured in so far.
	 * @param polyEdgeList
	 *            The edgelist of the polygon to add into the zbuffer.
	 * @param polyColor
	 *            The colour of the polygon to add into the zbuffer.
	 */
	public static void computeZBuffer(Color[][] zBuffer, float[][] zDepth, EdgeList polyEdgeList, Color polyColor) {
		for (int y = polyEdgeList.getStartY(); y < polyEdgeList.getEndY(); y++) {
			float slope = (polyEdgeList.getRightZ(y) - polyEdgeList.getLeftZ(y))
					/ (polyEdgeList.getRightX(y) - polyEdgeList.getLeftZ(y));

			float z = polyEdgeList.getLeftZ(y);
			int x = Math.round(polyEdgeList.getLeftX(y));
			while (x <= Math.round(polyEdgeList.getRightX(y)) - 1) {
				if (withinBounds(x, y) && z < zDepth[x][y]) {
					zBuffer[x][y] = polyColor;
					zDepth[x][y] = z;
				}
				z += slope;
				x++;
			}
		}
	}
	
	/**
	 * A helper function that makes sure that the pixel being rendered is actually on the screen.
	 * @param x
	 * 			The x-position of the pixel.
	 * @param y
	 * 			The y-position of the pixel.
	 * @return a boolean determining whether or not the pixel is within the canvas.
	 */
	public static boolean withinBounds(int x, int y) {
		return y >= 0 && x >= 0 && y < GUI.CANVAS_HEIGHT && x < GUI.CANVAS_WIDTH;
	}
}

// code for comp261 assignments
