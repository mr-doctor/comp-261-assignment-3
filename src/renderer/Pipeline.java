package renderer;

import java.awt.Color;
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
		return (getUnitNormal(poly.vertices[0], poly.vertices[1], poly.vertices[2]).z > 0);
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
		
		Vector3D unitNormal = getUnitNormal(poly.vertices[0], poly.vertices[1], poly.vertices[2]);
		
		Vector3D d = lightDirection.unitVector();
		Color a = ambientLight;
		Color l = lightColor;
		Color R = poly.getReflectance();
		
		float theta = (float) (Math.acos(unitNormal.dotProduct(d)));
		
		int[] rgbO = new int[3];
		
		float[] rgbA = findLightIntensity(colourAsArray(a));
		float[] rgbL = findLightIntensity(colourAsArray(l));
		int[] rgbR = colourAsArray(R);
		
		for (int i=0; i<3; i++) {
			rgbO[i] = (int) ((rgbA[i] + rgbL[i] * Math.max(0, Math.cos(theta))) * rgbR[i]);
		}
		
		return new Color(rgbO[0], rgbO[1], rgbO[2]);
	}
	
	public static Vector3D getUnitNormal(Vector3D vertexA, Vector3D vertexB, Vector3D vertexC) {
		float aX = vertexB.x - vertexA.x;
		float aY = vertexB.y - vertexA.y;
		float aZ = vertexB.z - vertexA.z;
		
		float bX = vertexC.x - vertexB.x;
		float bY = vertexC.y - vertexB.y;
		float bZ = vertexC.z - vertexB.z;
		
		float nX = aX * bZ - aZ * bY;
		float nY = aZ * bX - aX * bZ;
		float nZ = aX * bY - aY * bX;

		float normal = (float) Math.sqrt(Math.pow(nX, 2) + Math.pow(nY, 2) + Math.pow(nZ, 2));
		
		return new Vector3D(
				nX / normal,
				nY / normal,
				nZ / normal);
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
		
		List<Scene.Polygon> polygons = scene.getPolygons();
		Vector3D lightSource = scene.getLight();
		
		Vector3D newLightSource = null;
		if (xAngle > 0.0f) {
			newLightSource = new Vector3D (
				lightSource.x,
				(float) (Math.cos(xAngle) * lightSource.y - (Math.sin(xAngle) * lightSource.z)),
				(float) (Math.sin(xAngle) * lightSource.y + (Math.cos(xAngle) * lightSource.z)));
		} else if (yAngle > 0.0f) {
			newLightSource = new Vector3D (
					(float) (Math.cos(yAngle) * lightSource.x - (Math.sin(yAngle) * lightSource.z)),
					lightSource.y,
					(float) (-Math.cos(yAngle) * lightSource.x + Math.sin(yAngle) * lightSource.z));
		} else {
			newLightSource = lightSource;
		}
		
		for (Scene.Polygon p : polygons) {
			for (int i=0; i<p.vertices.length; i++) {
				Vector3D newPoint = null;
				if (xAngle > 0.0f) {
					newPoint = new Vector3D (
							p.vertices[i].x,
							(float) (Math.cos(xAngle) * p.vertices[i].y - (Math.sin(xAngle) * p.vertices[i].z)),
							(float) (Math.sin(xAngle) * p.vertices[i].y + (Math.cos(xAngle) * p.vertices[i].z)));
				} else if (yAngle > 0.0f) {
					newPoint = new Vector3D (
							(float) (Math.cos(yAngle) * p.vertices[i].x - (Math.sin(yAngle) * p.vertices[i].z)),
							p.vertices[i].y,
							(float) (-Math.cos(yAngle) * p.vertices[i].x + Math.sin(yAngle) * p.vertices[i].z));
				} else {
					newPoint = p.vertices[i];
				}
				p.vertices[i] = newPoint;
			}
		}
		
		return new Scene(polygons, newLightSource);
	}
	
	/**
	 * This should translate the scene by the appropriate amount.
	 * 
	 * @param scene
	 * @return
	 */
	public static Scene translateScene(Scene scene) {
		// TODO fill this in.
		return null;
	}

	/**
	 * This should scale the scene.
	 * 
	 * @param scene
	 * @return
	 */
	public static Scene scaleScene(Scene scene) {
		// TODO fill this in.
		return null;
	}

	/**
	 * Computes the edgelist of a single provided polygon, as per the lecture
	 * slides.
	 */
	public static EdgeList computeEdgeList(Polygon poly) {
		Vector3D[] vectors = Arrays.copyOf(poly.vertices, 3);

		EdgeList edgeList = new EdgeList(0, 0);
		
		Vector3D a;
		Vector3D b;
		
		for (int i=0; i<3; i++) {
			a = vectors[i];
			b = vectors[(i+1) % 3];
			
			float slopeX = (b.x - a.x) / (b.y - a.y);
			float slopeZ = (b.z - a.z) / (b.y - a.y);
			
			float x = a.x;
			int y = Math.round(a.y);
			float z = a.z;
			
			if (a.y < b.y) {
				while (y<= Math.round(b.y)) {
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
		for (int y = 0; y < polyEdgeList.getData().length; y++) {
			float slope = (polyEdgeList.getRightZ(y) - polyEdgeList.getLeftZ(y)) / 
					(polyEdgeList.getRightX(y) - polyEdgeList.getLeftZ(y));
			
			float z = polyEdgeList.getLeftZ(y);
			int x = Math.round(polyEdgeList.getLeftX(y));
			while (x <= Math.round(polyEdgeList.getRightX(y))) {
				if (z < zdepth[x][y]) {
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
