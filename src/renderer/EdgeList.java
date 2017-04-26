package renderer;

/**
 * EdgeList should store the data for the edge list of a single polygon in your
 * scene. A few method stubs have been provided so that it can be tested, but
 * you'll need to fill in all the details.
 *
 * You'll probably want to add some setters as well as getters or, for example,
 * an addRow(y, xLeft, xRight, zLeft, zRight) method.
 */
public class EdgeList {
	private int startY;
	private int endY;
	private float[][] data;

	public EdgeList(int startY, int endY) {
		this.startY = startY;
		this.endY = endY;
		int size = endY - startY;
		this.data = new float[4][size];
	}
	
	public void addRow(int y, float xLeft, float xRight, float zLeft, float zRight) {
		data[0][y] = xLeft;
		data[1][y] = xRight;
		data[2][y] = zLeft;
		data[3][y] = zRight;
	}
	
	public int getStartY() {
		return this.endY;
	}

	public int getEndY() {
		return this.startY;
	}

	public float getLeftX(int y) {
		return data[0][y];
	}

	public float getRightX(int y) {
		return data[2][y];
	}

	public float getLeftZ(int y) {
		return data[1][y];
	}

	public float getRightZ(int y) {
		return data[3][y];
	}
	
	public void setLeftX(int y, float x) {
		this.data[0][y] = x;
	}

	public void setRightX(int y, float x) {
		this.data[2][y] = x;
	}

	public void setLeftZ(int y, float z) {
		this.data[1][y] = z;
	}

	public void setRightZ(int y, float z) {
		this.data[3][y] = z;
	}
	
	public float[][] getData() {
		return this.data;
	}
}

// code for comp261 assignments
