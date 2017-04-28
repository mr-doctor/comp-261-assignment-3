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
	 * should be hidden), and false otherwise.
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

	
	private static int clamp(int in, int min, int max) {
		return Math.min(Math.max(in, min), max);
	}

	private static Vector3D getUnitNormal(Polygon poly) {
		Vector3D a = poly.getVertices()[0], b = poly.getVertices()[1], c = poly.getVertices()[2];
		return b.minus(a).crossProduct(c.minus(b)).unitVector();
	}

	public static int[] colourAsArray(Color c) {
		int[] rgbArray = new int[3];
		rgbArray[0] = c.getRed();
		rgbArray[1] = c.getGreen();
		rgbArray[2] = c.getBlue();

		return rgbArray;
	}

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
		/*Vector3D newLightSource = scene.getLight();

		if (xAngle != 0.0f) {
			newLightSource = tX.multiply(scene.getLight());
		}
		if (yAngle != 0.0f) {
			newLightSource = tY.multiply(newLightSource);
		}*/

		return new Scene(newPolygons, scene.getLight());
	}

	/**
	 * This should translate the scene by the appropriate amount.
	 * 
	 * @param scene
	 * @return
	 */
	public static Scene translateScene(Scene scene) {
		Rectangle bBox = boundingBox(scene.getPolygons());
		
		float xDiff = 0-bBox.x;
		float yDiff = 0-bBox.y;
		
		Transform t = Transform.newTranslation(new Vector3D(xDiff, yDiff, 0));
		for (Scene.Polygon p : scene.getPolygons()) {
			for (int i=0; i<p.vertices.length; i++) {
				p.vertices[i] = t.multiply(p.vertices[i]);
			}
		}
		Vector3D newLight = scene.getLight();//t.multiply(scene.getLight());
		
		return new Scene(scene.getPolygons(), newLight);
	}

	/**
	 * This should scale the scene.
	 * 
	 * @param scene
	 * @return
	 */
	public static Scene scaleScene(Scene scene) {
		Rectangle bBox = boundingBox(scene.getPolygons());
		
		float xMax = (float) (bBox.x + bBox.getWidth());
		float yMax = (float) (bBox.y + bBox.getHeight());
		
		float scaleFactor = 1;
		
		if (xMax > GUI.CANVAS_WIDTH) {
			scaleFactor = GUI.CANVAS_WIDTH / xMax;
		} else if (yMax > GUI.CANVAS_HEIGHT) {
			scaleFactor = GUI.CANVAS_HEIGHT / yMax;
		}
		
		if (scaleFactor == 1.0f) {
			return scene;
		}
		
		Transform t = Transform.newScale(scaleFactor, scaleFactor, scaleFactor);
		
		for (Scene.Polygon p : scene.getPolygons()) {
			for (int i=0; i<p.vertices.length; i++) {
				p.vertices[i] = t.multiply(p.vertices[i]);
			}
		}
		Vector3D newLight = scene.getLight();//t.multiply(scene.getLight());
		
		return new Scene(scene.getPolygons(), newLight);
	}
	
	public static Rectangle boundingBox(List<Scene.Polygon> polygons) {
		float minY = Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;
		
		float minX = Float.MAX_VALUE;
		float maxX = -Float.MAX_VALUE;
		
		for (Scene.Polygon poly : polygons) {
			Vector3D[] vectors = Arrays.copyOf(poly.vertices, 3);
			
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
	 */
	public static EdgeList computeEdgeList(Polygon poly) {
		Vector3D[] vectors = Arrays.copyOf(poly.vertices, 3);

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
	 * @param zbuffer
	 *            A double array of colours representing the Color at each pixel
	 *            so far.
	 * @param zdepth
	 *            A double array of floats storing the z-value of each pixel
	 *            that has been coloured in so far.
	 * @param polyEdgeList
	 *            The edgelist of the polygon to add into the zbuffer.
	 * @param polyColor
	 *            The colour of the polygon to add into the zbuffer.
	 */
	public static void computeZBuffer(Color[][] zbuffer, float[][] zdepth, EdgeList polyEdgeList, Color polyColor) {
		for (int y = polyEdgeList.getStartY(); y < polyEdgeList.getEndY(); y++) {
			float slope = (polyEdgeList.getRightZ(y) - polyEdgeList.getLeftZ(y))
					/ (polyEdgeList.getRightX(y) - polyEdgeList.getLeftZ(y));

			float z = polyEdgeList.getLeftZ(y);
			int x = Math.round(polyEdgeList.getLeftX(y));
			while (x <= Math.round(polyEdgeList.getRightX(y)) - 1) {
				if (y >= 0 && x >= 0 && z < zdepth[x][y]) {
					zbuffer[x][y] = polyColor;
					zdepth[x][y] = z;
				}
				z += slope;
				x++;
			}
		}
	}
}

// code for comp261 assignments
