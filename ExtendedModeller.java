/**
 * ExtendedModeller - Adapted from SimpleModeller-3D_JavaApplication-JOGL
   Still trying to work with GitHub.
 */
import java.lang.Math;
import java.util.Vector;

import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.BoxLayout;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
//import com.jogamp.opengl.GLCanvas;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLAutoDrawable;
// import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.gl2.GLUT;

import smTrace.SmTrace;		// Execution Trace support



class Scene {
	public Vector< OurBlock > ourBlocks = new Vector< OurBlock >();

	AlignedBox3D boundingBoxOfScene = new AlignedBox3D();
	boolean isBoundingBoxOfSceneDirty = false;




	public Scene() {
	}

	public AlignedBox3D getBoundingBoxOfScene() {
		if ( isBoundingBoxOfSceneDirty ) {
			boundingBoxOfScene.clear();
			for ( int i = 0; i < ourBlocks.size(); ++i ) {
				OurBlock block = (OurBlock)ourBlocks.elementAt(i).block;
				boundingBoxOfScene.bound(block.boundingBox());
			}
			isBoundingBoxOfSceneDirty = false;
		}
		return boundingBoxOfScene;
	}

	public void addBlock(
		String blockType, 		// Block type
		AlignedBox3D box,		// Bounding box
		float red, float green, float blue,
		float alpha
	) {
		OurBlock cb = OurBlock.getNewBlock(blockType, box, red, green, blue, alpha);
		
		if (cb == null || !cb.isOk()) {
			System.out.println(String.format("Couldn't create block type '%s'",
					blockType));
			return;
		}
		ourBlocks.addElement(cb);
		isBoundingBoxOfSceneDirty = true;
	}

	public void addColoredBox(
		AlignedBox3D box,
		float red, float green, float blue,
		float alpha
	) {
		OurBlock cb = new ColoredBox( "box", box, red, green, blue, alpha );
		ourBlocks.addElement( cb );
		isBoundingBoxOfSceneDirty = true;
	}

	public int getIndexOfIntersectedBox(
		Ray3D ray, // input
		Point3D intersectionPoint, // output
		Vector3D normalAtIntersection // output
	) {
		boolean intersectionDetected = false;
		int indexOfIntersectedBox = -1;
		float distanceToIntersection = 0;

		// candidate intersection
		Point3D candidatePoint = new Point3D();
		Vector3D candidateNormal = new Vector3D();
		float candidateDistance;

		for ( int i = 0; i < ourBlocks.size(); ++i ) {
			OurBlock block = ourBlocks.elementAt(i);
			if (block.intersects(ray,candidatePoint,candidateNormal)) {
				candidateDistance = Point3D.diff(
					ray.origin, candidatePoint
				).length();
				if (
					! intersectionDetected
					|| candidateDistance < distanceToIntersection
				) {
					// We've found a new, best candidate
					intersectionDetected = true;
					indexOfIntersectedBox = i;
					distanceToIntersection = candidateDistance;
					intersectionPoint.copy( candidatePoint );
					normalAtIntersection.copy( candidateNormal );
				}
			}
		}
		return indexOfIntersectedBox;
	}

	public AlignedBox3D getBox( int index ) {
		if ( 0 <= index && index < ourBlocks.size() )
			return ourBlocks.elementAt(index).getBox();
		return null;
	}

	public boolean getSelectionStateOfBox( int index ) {
		if ( 0 <= index && index < ourBlocks.size() )
			return ourBlocks.elementAt(index).isSelected();
		return false;
	}
	public void setSelectionStateOfBox( int index, boolean state ) {
		if ( 0 <= index && index < ourBlocks.size() )
			ourBlocks.elementAt(index).setSelected(state);
	}
	public void toggleSelectionStateOfBox( int index ) {
		if ( 0 <= index && index < ourBlocks.size() ) {
			OurBlock cb = ourBlocks.elementAt(index);
			cb.toggleSelected();
		}
	}

	public void setColorOfBlock( int index, float r, float g, float b ) {
		if ( 0 <= index && index < ourBlocks.size() ) {
			OurBlock cb = ourBlocks.elementAt(index);
			cb.setColor(r,g,b);
		}
	}

	public void translateBlock( int index, Vector3D translation ) {
		if ( 0 <= index && index < ourBlocks.size() ) {
			OurBlock cb = ourBlocks.elementAt(index);
			cb.translate(translation);
			isBoundingBoxOfSceneDirty = true;
		}
	}

	public void resizeBlock(
		int indexOfBlock, int indexOfCornerToResize, Vector3D translation
	) {
		if ( 0 <= indexOfBlock && indexOfBlock < ourBlocks.size() ) {
			OurBlock cb = ourBlocks.elementAt(indexOfBlock);
			cb.resize(indexOfCornerToResize, translation);
			isBoundingBoxOfSceneDirty = true;
		}
	}

	public void deleteBlock( int index ) {
		if ( 0 <= index && index < ourBlocks.size() ) {
			ourBlocks.removeElementAt( index );
			isBoundingBoxOfSceneDirty = true;
		}
	}


	public void deleteAllBlocks() {
		ourBlocks.removeAllElements();
		isBoundingBoxOfSceneDirty = true;
	}


	static public void drawBlock(
		GL2 gl,
		OurBlock block,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		block.draw(gl, expand, drawAsWireframe, cornersOnly);
	}


	public void drawScene(
		GL2 gl,
		int indexOfHilitedBox, // -1 for none
		boolean useAlphaBlending
	) {
		if ( useAlphaBlending ) {
			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.glDepthMask(false);
			gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE );
			gl.glEnable( GL.GL_BLEND );
		}
		for ( int i = 0; i < ourBlocks.size(); ++i ) {
			OurBlock cb = ourBlocks.elementAt(i);
			if ( useAlphaBlending )
				gl.glColor4f(cb.getRed(), cb.getGreen(), cb.getBlue(), cb.getAlpha());
			else
				gl.glColor3f(cb.getRed(), cb.getGreen(), cb.getBlue());
			drawBlock( gl, cb, false, false, false );
		}
		if ( useAlphaBlending ) {
			gl.glDisable( GL.GL_BLEND );
			gl.glDepthMask(true);
			gl.glEnable(GL.GL_DEPTH_TEST);
		}
		for ( int i = 0; i < ourBlocks.size(); ++i ) {
			OurBlock cb = ourBlocks.elementAt(i);
			if (SmTrace.tr("ball"))
				if (cb.blockType().equals("ball")) {
					System.out.println(String.format("ball at[%d]", i));
					if (cb.isSelected()) {
						System.out.println("ball selected");
					} else {
						System.out.println("ball not selected");
					}
				}
			if ( cb.isSelected() && indexOfHilitedBox == i )
				gl.glColor3f( 1, 1, 0 );
			else if ( cb.isSelected() )
				gl.glColor3f( 1, 0, 0 );
			else if ( indexOfHilitedBox == i )
				gl.glColor3f( 0, 1, 0 );
			else continue;
			drawBlock( gl, cb, true, true, true );
		}
	}

	@SuppressWarnings("serial")
	public void drawBoundingBoxOfScene( GL2 gl ) {
		AlignedBox3D box = getBoundingBoxOfScene();
		if ( ! box.isEmpty() )
			ColoredBox.drawBox( gl, box, false, true, false );
	}
}




class SceneViewer extends GLCanvas implements MouseListener, MouseMotionListener, GLEventListener {
	SmTrace smTrace;				// Trace support
	GLUT glut;

	public Scene scene = new Scene();
	public int indexOfSelectedBlock = -1; // -1 for none
	private Point3D selectedPoint = new Point3D();
	private Vector3D normalAtSelectedPoint = new Vector3D();
	public int indexOfHilitedBox = -1; // -1 for none
	private Point3D hilitedPoint = new Point3D();
	private Vector3D normalAtHilitedPoint = new Vector3D();

	Camera3D camera = new Camera3D();

	RadialMenuWidget radialMenu = new RadialMenuWidget();
	private static final int COMMAND_CREATE_BOX = 10;
	private static final int COMMAND_CREATE_BALL = 11;
	private static final int COMMAND_CREATE_CYLINDAR = 12;
	private static final int COMMAND_CREATE_CONE = 13;
	private static final int COMMAND_DUPLICATE_BLOCK = 14;
	private static final int COMMAND_COLOR_RED = 1;
	private static final int COMMAND_COLOR_YELLOW = 2;
	private static final int COMMAND_COLOR_GREEN = 3;
	private static final int COMMAND_COLOR_BLUE = 4;
	private static final int COMMAND_DELETE = 5;

	public boolean displayWorldAxes = false;
	public boolean displayCameraTarget = false;
	public boolean displayBoundingBox = false;
	public boolean enableCompositing = false;

	int mouse_x, mouse_y, old_mouse_x, old_mouse_y;

	public SceneViewer( GLCapabilities caps, SmTrace trace ) {

		super( caps );
		this.smTrace = trace;				// Passed in
		addGLEventListener(this);

		addMouseListener( this );
		addMouseMotionListener( this );

		radialMenu.setItemLabelAndID( RadialMenuWidget.CENTRAL_ITEM, "", COMMAND_DUPLICATE_BLOCK );
		radialMenu.setItemLabelAndID( 1, "Duplicate Block", COMMAND_DUPLICATE_BLOCK );
		radialMenu.setItemLabelAndID( 2, "Set Color to Red", COMMAND_COLOR_RED );
		radialMenu.setItemLabelAndID( 3, "Set Color to Yellow", COMMAND_COLOR_YELLOW );
		radialMenu.setItemLabelAndID( 4, "Set Color to Green", COMMAND_COLOR_GREEN );
		radialMenu.setItemLabelAndID( 5, "Set Color to Blue", COMMAND_COLOR_BLUE );
		radialMenu.setItemLabelAndID( 7, "Delete Box", COMMAND_DELETE );

		camera.setSceneRadius( (float)Math.max(
			5 * OurBlock.DEFAULT_SIZE,
			scene.getBoundingBoxOfScene().getDiagonal().length() * 0.5f
		) );
		camera.reset();

	}
	public Dimension getPreferredSize() {
		return new Dimension( 512, 512 );
	}

	float clamp( float x, float min, float max ) {
		if ( x < min ) return min;
		if ( x > max ) return max;
		return x;
	}
	public void createNewBlock(String blockType) {
		Vector3D halfDiagonalOfNewBox = new Vector3D(
			OurBlock.DEFAULT_SIZE*0.5f,
			OurBlock.DEFAULT_SIZE*0.5f,
			OurBlock.DEFAULT_SIZE*0.5f
		);
		if ( indexOfSelectedBlock >= 0 ) {
			OurBlock cb = scene.ourBlocks.elementAt(indexOfSelectedBlock);
			Point3D centerOfNewBox = Point3D.sum(
				Point3D.sum(
					cb.getCenter(),
					Vector3D.mult(
						normalAtSelectedPoint,
						0.5f*(float)Math.abs(Vector3D.dot(cb.getDiagonal(),normalAtSelectedPoint))
					)
				),
				Vector3D.mult( normalAtSelectedPoint, OurBlock.DEFAULT_SIZE*0.5f )
			);
			scene.addBlock(
				blockType,
				new AlignedBox3D(
					Point3D.diff( centerOfNewBox, halfDiagonalOfNewBox ),
					Point3D.sum( centerOfNewBox, halfDiagonalOfNewBox )
				),
				clamp( cb.getRed() + 0.5f*((float)Math.random()-0.5f), 0, 1 ),
				clamp( cb.getGreen() + 0.5f*((float)Math.random()-0.5f), 0, 1 ),
				clamp( cb.getBlue() + 0.5f*((float)Math.random()-0.5f), 0, 1 ),
				cb.getAlpha()
			);
		}
		else {
			Point3D centerOfNewBox = camera.target;
			scene.addBlock(
				blockType,
				new AlignedBox3D(
					Point3D.diff( centerOfNewBox, halfDiagonalOfNewBox ),
					Point3D.sum( centerOfNewBox, halfDiagonalOfNewBox )
				),
				(float)Math.random(),
				(float)Math.random(),
				(float)Math.random(),
				OurBlock.DEFAULT_ALPHA
			);
			normalAtSelectedPoint = new Vector3D(1,0,0);
		}
		// update selection to be new box
		scene.setSelectionStateOfBox( indexOfSelectedBlock, false );
		indexOfSelectedBlock = scene.ourBlocks.size() - 1;
		scene.setSelectionStateOfBox( indexOfSelectedBlock, true );
	}

	/**
	 * Duplicate selected block - offset a bit
	 */
	public void duplicateBlock() {
		String blockType = "box";		// Default
		if ( indexOfSelectedBlock >= 0 ) {
			OurBlock cb = scene.ourBlocks.elementAt(indexOfSelectedBlock);
			blockType = cb.blockType();
		}
		createNewBlock(blockType);
	}

	
	public void setColorOfSelection( float r, float g, float b ) {
		if ( indexOfSelectedBlock >= 0 ) {
			scene.setColorOfBlock( indexOfSelectedBlock, r, g, b );
		}
	}

	public void deleteSelection() {
		if ( indexOfSelectedBlock >= 0 ) {
			scene.deleteBlock( indexOfSelectedBlock );
			indexOfSelectedBlock = -1;
			indexOfHilitedBox = -1;
		}
	}

	public void deleteAll() {
		scene.deleteAllBlocks();
		indexOfSelectedBlock = -1;
		indexOfHilitedBox = -1;
	}

	public void lookAtSelection() {
		if ( indexOfSelectedBlock >= 0 ) {
			Point3D p = scene.getBox( indexOfSelectedBlock ).getCenter();
			camera.lookAt(p);
		}
	}

	public void resetCamera() {
		camera.setSceneRadius( (float)Math.max(
			5 * OurBlock.DEFAULT_SIZE,
			scene.getBoundingBoxOfScene().getDiagonal().length() * 0.5f
		) );
		camera.reset();
	}

	public void init( GLAutoDrawable drawable ) {
		GL2 gl = (GL2) drawable.getGL();
		gl.glClearColor( 0, 0, 0, 0 );
		glut = new GLUT();
	}
	public void reshape(
		GLAutoDrawable drawable,
		int x, int y, int width, int height
	) {
		GL gl = drawable.getGL();
		camera.setViewportDimensions(width, height);

		// set viewport
		gl.glViewport(0, 0, width, height);
	}
	public void displayChanged(
		GLAutoDrawable drawable,
		boolean modeChanged,
		boolean deviceChanged
	) {
		// leave this empty
	}
	public void display( GLAutoDrawable drawable ) {
		GL2 gl = (GL2) drawable.getGL();
		gl.glMatrixMode( GL2.GL_PROJECTION );
		gl.glLoadIdentity();
		camera.transform( gl );
		gl.glMatrixMode( GL2.GL_MODELVIEW );
		gl.glLoadIdentity();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl.glDepthFunc( GL.GL_LEQUAL );
		gl.glEnable( GL.GL_DEPTH_TEST );
		gl.glEnable( GL.GL_CULL_FACE );
		gl.glFrontFace(GL.GL_CCW);
		gl.glDisable( GL2.GL_LIGHTING );
		gl.glShadeModel( GL2.GL_FLAT );

		scene.drawScene( gl, indexOfHilitedBox, enableCompositing );

		if ( displayWorldAxes ) {
			gl.glBegin( GL.GL_LINES );
				gl.glColor3f( 1, 0, 0 );
				gl.glVertex3f(0,0,0);
				gl.glVertex3f(1,0,0);
				gl.glColor3f( 0, 1, 0 );
				gl.glVertex3f(0,0,0);
				gl.glVertex3f(0,1,0);
				gl.glColor3f( 0, 0, 1 );
				gl.glVertex3f(0,0,0);
				gl.glVertex3f(0,0,1);
			gl.glEnd();
		}
		if ( displayCameraTarget ) {
			gl.glBegin( GL.GL_LINES );
				gl.glColor3f( 1, 1, 1 );
				gl.glVertex3fv( Point3D.sum( camera.target, new Vector3D(-0.5f,    0,    0) ).get(), 0 );
				gl.glVertex3fv( Point3D.sum( camera.target, new Vector3D( 0.5f,    0,    0) ).get(), 0 );
				gl.glVertex3fv( Point3D.sum( camera.target, new Vector3D(    0,-0.5f,    0) ).get(), 0 );
				gl.glVertex3fv( Point3D.sum( camera.target, new Vector3D(    0, 0.5f,    0) ).get(), 0 );
				gl.glVertex3fv( Point3D.sum( camera.target, new Vector3D(    0,    0,-0.5f) ).get(), 0 );
				gl.glVertex3fv( Point3D.sum( camera.target, new Vector3D(    0,    0, 0.5f) ).get(), 0 );
			gl.glEnd();
		}
		if ( displayBoundingBox ) {
			gl.glColor3f( 0.5f, 0.5f, 0.5f );
			scene.drawBoundingBoxOfScene( gl );
		}

		if ( radialMenu.isVisible() ) {
			radialMenu.draw( gl, glut, getWidth(), getHeight() );
		}

		// gl.glFlush(); // I don't think this is necessary
	}

	private void updateHiliting() {
		Ray3D ray = camera.computeRay(mouse_x,mouse_y);
		Point3D newIntersectionPoint = new Point3D();
		Vector3D newNormalAtIntersection = new Vector3D();
		int newIndexOfHilitedBox = scene.getIndexOfIntersectedBox(
			ray, newIntersectionPoint, newNormalAtIntersection
		);
		hilitedPoint.copy( newIntersectionPoint );
		normalAtHilitedPoint.copy( newNormalAtIntersection );
		if ( newIndexOfHilitedBox != indexOfHilitedBox ) {
			indexOfHilitedBox = newIndexOfHilitedBox;
			repaint();
		}
	}

	public void mouseClicked( MouseEvent e ) { }
	public void mouseEntered( MouseEvent e ) { }
	public void mouseExited( MouseEvent e ) { }

	public void mousePressed( MouseEvent e ) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
		if (SmTrace.tr("mouse"))
			System.out.println(String.format("mousePressed(%d,%d)",
					mouse_x, mouse_y));
		if ( radialMenu.isVisible() || (SwingUtilities.isRightMouseButton(e) && !e.isShiftDown() && !e.isControlDown()) ) {
			int returnValue = radialMenu.pressEvent( mouse_x, mouse_y );
			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}

		updateHiliting();

		if ( SwingUtilities.isLeftMouseButton(e) && !e.isControlDown() ) {
			if ( indexOfSelectedBlock >= 0 )
				// de-select the old box
				scene.setSelectionStateOfBox( indexOfSelectedBlock, false );
			indexOfSelectedBlock = indexOfHilitedBox;
			selectedPoint.copy( hilitedPoint );
			normalAtSelectedPoint.copy( normalAtHilitedPoint );
			if ( indexOfSelectedBlock >= 0 ) {
				scene.setSelectionStateOfBox( indexOfSelectedBlock, true );
			}
			repaint();
		}
	}

	public void mouseReleased( MouseEvent e ) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
		if (SmTrace.tr("mouse"))
			System.out.println(String.format("mouseReleased(%d,%d)",
					mouse_x, mouse_y));

		if ( radialMenu.isVisible() ) {
			int returnValue = radialMenu.releaseEvent( mouse_x, mouse_y );

			int itemID = radialMenu.getItemID(radialMenu.getSelection());
			System.out.println(String.format("itemID=%d returnValue=%d", itemID, returnValue));
			switch ( itemID ) {
			case COMMAND_DUPLICATE_BLOCK :
				duplicateBlock();		// Duplicate selected block
				break;
			case COMMAND_CREATE_BOX :
				createNewBlock("box");
				break;
			case COMMAND_CREATE_BALL :
				createNewBlock("ball");
				break;
			case COMMAND_CREATE_CONE :
				createNewBlock("cone");
				break;
			case COMMAND_CREATE_CYLINDAR :
				createNewBlock("cylinder");
				break;
			case COMMAND_COLOR_RED :
				setColorOfSelection( 1, 0, 0 );
				break;
			case COMMAND_COLOR_YELLOW :
				setColorOfSelection( 1, 1, 0 );
				break;
			case COMMAND_COLOR_GREEN :
				setColorOfSelection( 0, 1, 0 );
				break;
			case COMMAND_COLOR_BLUE :
				setColorOfSelection( 0, 0, 1 );
				break;
			case COMMAND_DELETE :
				deleteSelection();
				break;
			}

			repaint();

			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
	}

	public void mouseMoved( MouseEvent e ) {

		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();

		if ( radialMenu.isVisible() ) {
			int returnValue = radialMenu.moveEvent( mouse_x, mouse_y );
			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}

		updateHiliting();
	}

	public void mouseDragged( MouseEvent e ) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
		int delta_x = mouse_x - old_mouse_x;
		int delta_y = old_mouse_y - mouse_y;

		if ( radialMenu.isVisible() ) {
			int returnValue = radialMenu.dragEvent( mouse_x, mouse_y );
			if ( returnValue == CustomWidget.S_REDRAW )
				repaint();
			if ( returnValue != CustomWidget.S_EVENT_NOT_CONSUMED )
				return;
		}
		else if (e.isControlDown()) {
			if (
				SwingUtilities.isLeftMouseButton(e)
				&& SwingUtilities.isRightMouseButton(e)
			) {
				camera.dollyCameraForward(
					(float)(3*(delta_x+delta_y)), false
				);
			}
			else if ( SwingUtilities.isLeftMouseButton(e) ) {
				camera.orbit(old_mouse_x,old_mouse_y,mouse_x,mouse_y);
			}
			else {
				camera.translateSceneRightAndUp(
					(float)(delta_x), (float)(delta_y)
				);
			}
			repaint();
		}
		else if (
			SwingUtilities.isLeftMouseButton(e) && !e.isControlDown()
			&& indexOfSelectedBlock >= 0
		) {
			if ( !e.isShiftDown() ) {
				// translate a box

				Ray3D ray1 = camera.computeRay( old_mouse_x, old_mouse_y );
				Ray3D ray2 = camera.computeRay( mouse_x, mouse_y );
				Point3D intersection1 = new Point3D();
				Point3D intersection2 = new Point3D();
				Plane plane = new Plane( normalAtSelectedPoint, selectedPoint );
				if (
					plane.intersects( ray1, intersection1, true )
					&& plane.intersects( ray2, intersection2, true )
				) {
					Vector3D translation = Point3D.diff( intersection2, intersection1 );
					scene.translateBlock( indexOfSelectedBlock, translation );
					repaint();
				}
			}
			else {
				// resize a box

				Ray3D ray1 = camera.computeRay( old_mouse_x, old_mouse_y );
				Ray3D ray2 = camera.computeRay( mouse_x, mouse_y );
				Point3D intersection1 = new Point3D();
				Point3D intersection2 = new Point3D();
				Vector3D v1 = Vector3D.cross( normalAtSelectedPoint, ray1.direction );
				Vector3D v2 = Vector3D.cross( normalAtSelectedPoint, v1 );
				Plane plane = new Plane( v2, selectedPoint );
				if (
					plane.intersects( ray1, intersection1, true )
					&& plane.intersects( ray2, intersection2, true )
				) {
					Vector3D translation = Point3D.diff( intersection2, intersection1 );

					// project the translation onto the normal, so that it is only along one axis
					translation = Vector3D.mult( normalAtSelectedPoint, Vector3D.dot( normalAtSelectedPoint, translation ) );
					scene.resizeBlock(
						indexOfSelectedBlock,
						scene.ourBlocks.elementAt(indexOfSelectedBlock).getBox().getIndexOfExtremeCorner(normalAtSelectedPoint),
						translation
					);
					repaint();
				}
			}
		}
	}
	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}
}

public class ExtendedModeller implements ActionListener {

	static final String applicationName = "Simple Modeller - extended";
	public static SmTrace smTrace;
	JFrame frame;
	Container toolPanel;
	SceneViewer sceneViewer;

	JMenuItem deleteAllMenuItem, quitMenuItem, aboutMenuItem;
	JButton createBoxButton;
	JButton createBallButton;
	JButton createConeButton;
	JButton createCylinderButton;
	JButton deleteSelectionButton;
	JButton lookAtSelectionButton;
	JButton resetCameraButton;
	JCheckBox displayWorldAxesCheckBox;
	JCheckBox displayCameraTargetCheckBox;
	JCheckBox displayBoundingBoxCheckBox;
	JCheckBox enableCompositingCheckBox;

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if ( source == deleteAllMenuItem ) {
			int response = JOptionPane.showConfirmDialog(
				frame,
				"Really delete all?",
				"Confirm Delete All",
				JOptionPane.YES_NO_OPTION
			);

			if (response == JOptionPane.YES_OPTION) {
				sceneViewer.deleteAll();
				sceneViewer.repaint();
			}
		}
		else if ( source == quitMenuItem ) {
			int response = JOptionPane.showConfirmDialog(
				frame,
				"Really quit?",
				"Confirm Quit",
				JOptionPane.YES_NO_OPTION
			);

			if (response == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		}
		else if ( source == aboutMenuItem ) {
			JOptionPane.showMessageDialog(
				frame,
				"'" + applicationName + "' Sample Program\n"
					+ "Original version written April-May 2008",
				"About",
				JOptionPane.INFORMATION_MESSAGE
			);
		}
		else if ( source == createBoxButton ) {
			if (SmTrace.tr("menubutton"))
					System.out.println(String.format("Create Box button"));
			sceneViewer.createNewBlock("box");
			sceneViewer.repaint();
		}
		else if ( source == createBallButton ) {
			if (SmTrace.tr("menubutton"))
					System.out.println(String.format("Create Ball button"));
			sceneViewer.createNewBlock("ball");
			sceneViewer.repaint();
		}
		else if ( source == createConeButton ) {
			if (SmTrace.tr("menubutton"))
					System.out.println(String.format("Create Cone button"));
			sceneViewer.createNewBlock("cone");
			sceneViewer.repaint();
		}
		else if ( source == createCylinderButton ) {
			if (SmTrace.tr("menubutton"))
					System.out.println(String.format("Create Cylinder button"));
			sceneViewer.createNewBlock("cylinder");
			sceneViewer.repaint();
		}
		else if ( source == deleteSelectionButton ) {
			sceneViewer.deleteSelection();
			sceneViewer.repaint();
		}
		else if ( source == lookAtSelectionButton ) {
			sceneViewer.lookAtSelection();
			sceneViewer.repaint();
		}
		else if ( source == resetCameraButton ) {
			sceneViewer.resetCamera();
			sceneViewer.repaint();
		}
		else if ( source == displayWorldAxesCheckBox ) {
			sceneViewer.displayWorldAxes = ! sceneViewer.displayWorldAxes;
			sceneViewer.repaint();
		}
		else if ( source == displayCameraTargetCheckBox ) {
			sceneViewer.displayCameraTarget = ! sceneViewer.displayCameraTarget;
			sceneViewer.repaint();
		}
		else if ( source == displayBoundingBoxCheckBox ) {
			sceneViewer.displayBoundingBox = ! sceneViewer.displayBoundingBox;
			sceneViewer.repaint();
		}
		else if ( source == enableCompositingCheckBox ) {
			sceneViewer.enableCompositing = ! sceneViewer.enableCompositing;
			sceneViewer.repaint();
		}
	}


	// For thread safety, this should be invoked
	// from the event-dispatching thread.
	//
	private void createUI() {
		if ( ! SwingUtilities.isEventDispatchThread() ) {
			System.out.println(
				"Warning: UI is not being created in the Event Dispatch Thread!");
			assert false;
		}

		frame = new JFrame( applicationName );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		JMenuBar menuBar = new JMenuBar();
			JMenu menu = new JMenu("File");
				deleteAllMenuItem = new JMenuItem("Delete All");
				deleteAllMenuItem.addActionListener(this);
				menu.add(deleteAllMenuItem);

				menu.addSeparator();

				quitMenuItem = new JMenuItem("Quit");
				quitMenuItem.addActionListener(this);
				menu.add(quitMenuItem);
			menuBar.add(menu);
			menu = new JMenu("Help");
				aboutMenuItem = new JMenuItem("About");
				aboutMenuItem.addActionListener(this);
				menu.add(aboutMenuItem);
			menuBar.add(menu);
		frame.setJMenuBar(menuBar);

		toolPanel = new JPanel();
		toolPanel.setLayout( new BoxLayout( toolPanel, BoxLayout.Y_AXIS ) );

		// Need to set visible first before starting the rendering thread due
		// to a bug in JOGL. See JOGL Issue #54 for more information on this
		// https://jogl.dev.java.net/issues/show_bug.cgi?id=54
		frame.setVisible(true);

		GLCapabilities caps = new GLCapabilities(null);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);
		sceneViewer = new SceneViewer(caps, this.smTrace);

		Container pane = frame.getContentPane();
		// We used to use a BoxLayout as the layout manager here,
		// but it caused problems with resizing behavior due to
		// a JOGL bug https://jogl.dev.java.net/issues/show_bug.cgi?id=135
		pane.setLayout( new BorderLayout() );
		pane.add( toolPanel, BorderLayout.LINE_START );
		pane.add( sceneViewer, BorderLayout.CENTER );

		createBoxButton = new JButton("Create Box");
		createBoxButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		createBoxButton.addActionListener(this);
		toolPanel.add( createBoxButton );

		createBallButton = new JButton("Create Ball");
		createBallButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		createBallButton.addActionListener(this);
		toolPanel.add( createBallButton );

		createConeButton = new JButton("Create Cone");
		createConeButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		createConeButton.addActionListener(this);
		toolPanel.add( createConeButton );

		createCylinderButton = new JButton("Create Cylindar");
		createCylinderButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		createCylinderButton.addActionListener(this);
		toolPanel.add( createCylinderButton );

		deleteSelectionButton = new JButton("Delete Selection");
		deleteSelectionButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		deleteSelectionButton.addActionListener(this);
		toolPanel.add( deleteSelectionButton );

		lookAtSelectionButton = new JButton("Look At Selection");
		lookAtSelectionButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		lookAtSelectionButton.addActionListener(this);
		toolPanel.add( lookAtSelectionButton );

		resetCameraButton = new JButton("Reset Camera");
		resetCameraButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		resetCameraButton.addActionListener(this);
		toolPanel.add( resetCameraButton );

		displayWorldAxesCheckBox = new JCheckBox("Display World Axes", sceneViewer.displayWorldAxes );
		displayWorldAxesCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayWorldAxesCheckBox.addActionListener(this);
		toolPanel.add( displayWorldAxesCheckBox );

		displayCameraTargetCheckBox = new JCheckBox("Display Camera Target", sceneViewer.displayCameraTarget );
		displayCameraTargetCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayCameraTargetCheckBox.addActionListener(this);
		toolPanel.add( displayCameraTargetCheckBox );

		displayBoundingBoxCheckBox = new JCheckBox("Display Bounding Box", sceneViewer.displayBoundingBox );
		displayBoundingBoxCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayBoundingBoxCheckBox.addActionListener(this);
		toolPanel.add( displayBoundingBoxCheckBox );

		enableCompositingCheckBox = new JCheckBox("Enable Compositing", sceneViewer.enableCompositing );
		enableCompositingCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		enableCompositingCheckBox.addActionListener(this);
		toolPanel.add( enableCompositingCheckBox );

		frame.pack();
		frame.setVisible( true );
	}

	public static void main( String[] args ) {
		SmTrace.setFlags(args[0]);		// Use first arg
		// Schedule the creation of the UI for the event-dispatching thread.
		javax.swing.SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					ExtendedModeller sp = new ExtendedModeller();
					sp.createUI();
				}
			}
		);
	}
}

