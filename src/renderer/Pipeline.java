package renderer;

import java.awt.Color;
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
		// TODO fill this in.
		return false;
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
		
		double theta = Math.acos(unitNormal.dotProduct(d));
		
		int[] rgbO = new int[3];
		float[] rgbA = new float[3];
		rgbA = a.getColorComponents(rgbA);
		float[] rgbL = new float[3];
		rgbL = l.getColorComponents(rgbL);
		float[] rgbR = new float[3];
		rgbR = R.getColorComponents(rgbR);
		
		for (int i=0; i<3; i++) {
			rgbO[i] = (int) (((int) (rgbA[i]) + (int) (rgbL[i]) * Math.cos(theta)) * (int) (rgbR[i]));
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

	/**
	 * This method should rotate the polygons and light such that the viewer is
	 * looking down the Z-axis. The idea is that it returns an entirely new
	 * Scene object, filled with new Polygons, that have been rotated.
	 * 
	 * @param scene
	 *            The original Scene.
	 * @param xAngle
	 *            An angle describing the viewer's rotation in the YZ-plane (i.e
	 *            around the X-axis).
	 * @param yAngle
	 *            An angle describing the viewer's rotation in the XZ-plane (i.e
	 *            around the Y-axis).
	 * @return A new Scene where all the polygons and the light source have been
	 *         rotated accordingly.
	 */
	public static Scene rotateScene(Scene scene, float xAngle, float yAngle) {
		
		List<Scene.Polygon> polygons = scene.getPolygons();
		Vector3D lightSource = scene.getLight();
		
		Vector3D newLightSource = transformBy(lightSource, new float[][]{
			{(float) (Math.cos(xAngle)), (float) (-Math.sin(xAngle)), 0f},
			{(float) (Math.sin(xAngle)), (float) (Math.cos(yAngle)), 0f},
			{0f, 0f, 0f}});
		
		for (Scene.Polygon p : polygons) {
			for (int i=0; i<p.vertices.length; i++) {
				Vector3D newPoint = transformBy(p.vertices[i], new float[][]{
					{(float) (Math.cos(xAngle)), (float) (-Math.sin(xAngle)), 0f},
					{(float) (Math.sin(xAngle)), (float) (Math.cos(yAngle)), 0f},
					{0f, 0f, 0f}});
				p.vertices[i] = newPoint;
			}
		}
		
		return new Scene(polygons, newLightSource);
	}
	
	/**
	 * Transforms a given point by a given matrix.
	 * 
	 * @param point
	 * 				The original point
	 * @param matrix
	 * 				The matrix to transform by
	 * @return The transformed point
	 */
	public static Vector3D transformBy(Vector3D point, float[][] matrix) {
		float[] pointAsArray = new float[] {point.x, point.y, point.z};
		float[] newPointAsArray = new float[] {0, 0, 0};
		for (int row=0; row<3; row++) {
			for (int column=0; column<3; column++) {
				newPointAsArray[row] += matrix[row][column] * pointAsArray[column];
			}
		}
		return new Vector3D(newPointAsArray[0], newPointAsArray[1], newPointAsArray[2]);
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
		// TODO fill this in.
		return null;
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
		// TODO fill this in.
	}
}

// code for comp261 assignments
