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
	private int size;

	public EdgeList(int startY, int endY) {
		this.startY = startY;
		this.endY = endY;
		size = endY - startY + 1;
		this.data = new float[4][size];
	}
	
	public int getStartY() {
		return this.startY;
	}

	public int getEndY() {
		return this.endY;
	}

    public float getLeftX(int y) {
        return data[0][y];
    }

    public void setLeftX(int y, float value){
        data[0][y] = value;
    }

    public float getRightX(int y) {
        return data[2][y];
    }

    public void setRightX(int y, float val){
        data[2][y] = val;
    }

    public float getLeftZ(int y) {
        return data[1][y];
    }

    public void setLeftZ(int y, float val){
        data[1][y] = val;
    }

    public float getRightZ(int y) {
        return data[3][y];
    }

    public void setRightZ(int y, float val){
        data[3][y] = val;
    }
	
	public float[][] getData() {
		return this.data;
	}
}

// code for comp261 assignments
