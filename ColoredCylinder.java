import java.awt.Canvas;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.glu.GLU;

public class ColoredCylinder extends OurBlock {

	private AlignedBox3D box;
	private boolean isOk = false;	// Set OK upon successful construction

	// The color and alpha components, each in [0,1]
	private float r=1, g=1, b=1, a=1;

	private boolean isSelected = false;


	public String blockType() {
		return "cylinder";
	}
	
	
	public boolean isSelected() {
		return isSelected;
	}

	public boolean setSelected(boolean state) {
		isSelected = state;
		return state;
	}

	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		return getBox().intersects(ray, intersection, normalAtIntersection);		
	}
	/**
	 * Check if ok block
	 */

	public boolean isOk() {
		return isOk;
	}
	
	public ColoredCylinder(
		String blockType,
		AlignedBox3D alignedBox3D,
		float red,
		float green,
		float blue,
		float alpha
	) {
		box = new AlignedBox3D(
			alignedBox3D.getMin(),
			alignedBox3D.getMax()
		);
		r = red;
		g = green;
		b = blue;
		a = alpha;
		isOk = true;
	}
	


	public void draw(
		GL2 gl,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		AlignedBox3D box = getBox();
		drawCylinder(gl, box, expand, drawAsWireframe, cornersOnly);
	}
	

	// Overridden by all nontrivial blocks
	public Vector3D getDiagonal() {
		return box.getDiagonal();
	}
	

	public float getRed() {
		return r;
	}

	public float getGreen() {
		return g;
	}

	public float getBlue() {
		return b;
	}

	public float getAlpha() {
		return a;
	}

	public void setColor(float red, float green, float blue,
			float alpha) {
		setColor(red, green, blue);
		setAlpha(a);
	}

	public void setRed(float val) {
		r = val;
	}

	public void setGreen(float val) {
		g = val;
	}

	public void setBlue(float val) {
		b = val;
	}

	public void setAlpha(float val) {
		a = val;
	}

	public void setColor(float red, float green, float blue
		) {
		setRed(red);
		setGreen(green);
		setBlue(blue);
		}

	public void resize(
		int indexOfCornerToResize, Vector3D translation
	) {
		AlignedBox3D oldBox = getBox();
		AlignedBox3D newBox = new AlignedBox3D();

		// One corner of the new box will be the corner of the old
		// box that is diagonally opposite the corner being resized ...
		 newBox.bound( oldBox.getCorner( indexOfCornerToResize ^ 7 ) );

		// ... and the other corner of the new box will be the
		// corner being resized, after translation.
		newBox.bound( Point3D.sum( oldBox.getCorner( indexOfCornerToResize ), translation ) );
	}

	public void translate(Vector3D translation ) {
		AlignedBox3D oldBox = getBox();
		box = new AlignedBox3D(
			Point3D.sum( oldBox.getMin(), translation ),
			Point3D.sum( oldBox.getMax(), translation )
		);
	}

	// Not a box so return a bounding box
	public AlignedBox3D getBox() {
		return boundingBox();
	}
	
	public Point3D getCenter() {
		return getBox().getCenter();
	}
	
	public AlignedBox3D boundingBox() {
		return box.boundingBox();
	}



	static public void drawCylinder(
		GL2 gl,
		AlignedBox3D box,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		if ( expand ) {
			float diagonal = box.getDiagonal().length();
			diagonal /= 20;
			Vector3D v = new Vector3D( diagonal, diagonal, diagonal );
			box = new AlignedBox3D( Point3D.diff(box.getMin(),v), Point3D.sum(box.getMax(),v) );
		}
		if ( drawAsWireframe ) {
			if ( cornersOnly ) {
				gl.glBegin( GL.GL_LINES );
				for ( int dim = 0; dim < 3; ++dim ) {
					Vector3D v = Vector3D.mult( Point3D.diff(box.getCorner(1<<dim),box.getCorner(0)), 0.1f );
					for ( int a = 0; a < 2; ++a ) {
						for ( int b = 0; b < 2; ++b ) {
							int i = (a << ((dim+1)%3)) | (b << ((dim+2)%3));
							gl.glVertex3fv( box.getCorner(i).get(), 0 );
							gl.glVertex3fv( Point3D.sum( box.getCorner(i), v ).get(), 0 );
							i |= 1 << dim;
							gl.glVertex3fv( box.getCorner(i).get(), 0 );
							gl.glVertex3fv( Point3D.diff( box.getCorner(i), v ).get(), 0 );
						}
					}
				}
				gl.glEnd();
			}
			else {
				gl.glBegin( GL.GL_LINE_STRIP );
					gl.glVertex3fv( box.getCorner( 0 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 1 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 3 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 2 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 6 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 7 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 5 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 4 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 0 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 2 ).get(), 0 );
				gl.glEnd();
				gl.glBegin( GL.GL_LINES );
					gl.glVertex3fv( box.getCorner( 1 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 5 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 3 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 7 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 4 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 6 ).get(), 0 );
				gl.glEnd();
			}
		}
		else {
			int nLongitudes = 20;
			int nLatitudes = nLongitudes;
			Vector3D diagonal = box.getDiagonal();
			float xlen = Math.abs(diagonal.x());
			float ylen = Math.abs(diagonal.y());
			float zlen = Math.abs(diagonal.z());
			float minlen = Math.min(xlen, ylen);
			minlen = Math.min(minlen, zlen);
			float r = minlen/2;
			float h = zlen;
			Point3D center = box.getCenter();
			GLU glu = new GLU();

			GLUquadric cylinder = glu.gluNewQuadric();
			float bx = center.x();
			float by = center.y();
			float bz = center.z();
			gl.glTranslatef(bx, by, bz);
			glu.gluCylinder(cylinder, r, r, h, nLongitudes, nLatitudes);
			gl.glTranslatef(-bx, -by, -bz);
///			glut.glutSolidSphere(r, nLongitudes, nLatitudes);
		}
	}
	

	
}
