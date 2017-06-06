import com.jogamp.opengl.GL2;

/*
 * Contains some block
 */
public class OurBlock {
	public static final float DEFAULT_SIZE = 0.5f;
	public static final float DEFAULT_ALPHA = 0.5f;
	public Object block = null;

	
	public OurBlock() {
		block = this;
	}

	public static OurBlock getNewBlock(
		String blockType, 		// Block type
		AlignedBox3D box,		// Bounding box
		float red, float green, float blue,
		float alpha
	) {
		OurBlock block = null;
		if (blockType.equals("box"))
			block = new ColoredBox(blockType, box, red, green, blue, alpha);
		else if (blockType.equals("ball"))
			block = new ColoredBall(blockType, box, red, green, blue, alpha);
		else if (blockType.equals("cone"))
			block = new ColoredCone(blockType, box, red, green, blue, alpha);
		else if (blockType.equals("cylinder"))
			block = new ColoredCylinder(blockType, box, red, green, blue, alpha);
		else
			System.out.println(String.format("Unsupported block type %s",
					blockType));
		return block;
	}

	/**
	 * Check if ok block
	 */
					// Inheriting objects override
	public boolean isOk() {
		return false;
	}
	
	// Overridden by all nontrivial blocks
	public void draw(
		GL2 gl,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		System.out.println("draw - ignored");
	}
	
	public void resize(
		int indexOfCornerToResize, Vector3D translation
	) {
		System.out.println(String.format(
					"no object for: resize(corner(%d) translation(%s)",
					indexOfCornerToResize, translation.toString()));
	}

					// Create new copy of current block
	public void translate(Vector3D translate
	) {
		System.out.println("translate - ignored");
	}

	public String blockType() {
		return "UNKNOWN";
	}
	
						// Overridden by all nontrivial blocks
	public AlignedBox3D boundingBox() {
		System.out.println("boundingBox()");
		return new AlignedBox3D();
	}
	
						// Overridden by all nontrivial blocks
	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		boolean allowIntersectionEvenIfRayOriginatesInsideSphere
	) {
		System.out.println("intersects");
		return false;		
	}
	
								// Overridden by all nontrivial blocks
	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		System.out.println("intersects(ray, intersection, normalAtIntersection)");
		return false;		
	}

								// Overridden by all nontrivial blocks
	public AlignedBox3D getBox() {
		System.out.println("boundingBox()");
		return new AlignedBox3D();
	}
	
								// Overridden by all nontrivial blocks
	public boolean isSelected() {
		System.out.println("isSelected()");
		return false;
	}

	// Overridden by all nontrivial blocks
	public boolean setSelected(boolean state) {
		System.out.print("setSelect ignored\n");
		return false;
	}

	public boolean toggleSelected() {
		return setSelected(!isSelected());
	}

	// Overridden by all nontrivial blocks
	public float getRed() {
		return 0;
	}

	// Overridden by all nontrivial blocks
	public float getGreen() {
		System.out.println("getGreen - ignored");
		return 0;
	}

	// Overridden by all nontrivial blocks
	public float getBlue() {
		System.out.println("getBlue - ignored");
		return 0;
	}

	// Overridden by all nontrivial blocks
	public float getAlpha() {
		System.out.println("getAlpha - ignored");
		return 0;
	}
	
	// Overridden by all nontrivial blocks
	public Point3D getCenter() {
		System.out.print("getCenter ignored\n");
		return new Point3D();
	}

	// Overridden by all nontrivial blocks
	public Vector3D getDiagonal() {
		System.out.print("getDiagonal ignored\n");
		return new Vector3D();
	}
	
	
		// Overridden by all nontrivial blocks
	public void setColor(float red, float green, float blue,
		float alpha) {
		System.out.print("setColor4 ignored\n");
		}

								// Overridden by all nontrivial blocks
	public void setColor(float red, float green, float blue) {
		System.out.print("setColor3 ignored\n");
	}

}
