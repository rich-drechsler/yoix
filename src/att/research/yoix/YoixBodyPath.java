/*
 *  This software may only be used by you under license from AT&T Corp.
 *  ("AT&T").  A copy of AT&T's Source Code Agreement is available at
 *  AT&T's Internet website having the URL:
 *
 *    <http://www.research.att.com/sw/tools/yoix/license/source.html>
 *
 *  If you received this software without first entering into a license
 *  with AT&T, you have an infringing copy of this software and cannot
 *  use it without violating AT&T's intellectual property rights.
 */

package att.research.yoix;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

final
class YoixBodyPath extends YoixPointerActive

    implements YoixConstantsGraphics

{

    private YoixBodyMatrix  currentmatrix;
    private GeneralPath     path;

    //
    // A Yoix paint function. It's obscure, undocumented, and may not be
    // used anywhere. Even so we decided to leave the code in, at least
    // for now, just in case.
    //
    // Actually it's now used, or can be, when a path is assigned to the
    // shape field in a frame, dialog, or window.
    //

    private YoixObject  paint = null;

    //
    // Flag tells us we're pretty much finished with initialization.
    //

    private boolean  initialized = false;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	N_CTM,              $LR__,       null,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(45);

    static {
	activefields.put(N_ADD, new Integer(V_ADD));
	activefields.put(N_APPENDPATH, new Integer(V_APPENDPATH));
	activefields.put(N_ARC, new Integer(V_ARC));
	activefields.put(N_ARCN, new Integer(V_ARCN));
	activefields.put(N_ARCT, new Integer(V_ARCT));
	activefields.put(N_CLOSEPATH, new Integer(V_CLOSEPATH));
	activefields.put(N_CTM, new Integer(V_CTM));
	activefields.put(N_CURRENTPATH, new Integer(V_CURRENTPATH));
	activefields.put(N_CURRENTPOINT, new Integer(V_CURRENTPOINT));
	activefields.put(N_CURVETO, new Integer(V_CURVETO));
	activefields.put(N_ELEMENTS, new Integer(V_ELEMENTS));
	activefields.put(N_EOADD, new Integer(V_EOADD));
	activefields.put(N_EOINTERSECT, new Integer(V_EOINTERSECT));
	activefields.put(N_EOINTERSECTS, new Integer(V_EOINTERSECTS));
	activefields.put(N_EOSUBTRACT, new Integer(V_EOSUBTRACT));
	activefields.put(N_EOXOR, new Integer(V_EOXOR));
	activefields.put(N_FLATTENPATH, new Integer(V_FLATTENPATH));
	activefields.put(N_INEOFILL, new Integer(V_INEOFILL));
	activefields.put(N_INFILL, new Integer(V_INFILL));
	activefields.put(N_INTERSECT, new Integer(V_INTERSECT));
	activefields.put(N_INTERSECTS, new Integer(V_INTERSECTS));
	activefields.put(N_INSTROKE, new Integer(V_INSTROKE));
	activefields.put(N_LINETO, new Integer(V_LINETO));
	activefields.put(N_MOVETO, new Integer(V_MOVETO));
	activefields.put(N_NEWPATH, new Integer(V_NEWPATH));
	activefields.put(N_PAINT, new Integer(V_PAINT));
	activefields.put(N_PATHBBOX, new Integer(V_PATHBBOX));
	activefields.put(N_PATHFORALL, new Integer(V_PATHFORALL));
	activefields.put(N_QUADTO, new Integer(V_QUADTO));
	activefields.put(N_RCURVETO, new Integer(V_RCURVETO));
	activefields.put(N_RLINETO, new Integer(V_RLINETO));
	activefields.put(N_RMOVETO, new Integer(V_RMOVETO));
	activefields.put(N_ROTATEPATH, new Integer(V_ROTATEPATH));
	activefields.put(N_RQUADTO, new Integer(V_RQUADTO));
	activefields.put(N_SCALEPATH, new Integer(V_SCALEPATH));
	activefields.put(N_SHEARPATH, new Integer(V_SHEARPATH));
	activefields.put(N_SUBTRACT, new Integer(V_SUBTRACT));
	activefields.put(N_TRANSFORMPATH, new Integer(V_TRANSFORMPATH));
	activefields.put(N_TRANSLATEPATH, new Integer(V_TRANSLATEPATH));
	activefields.put(N_TRIMTOSTROKE, new Integer(V_TRIMTOSTROKE));
	activefields.put(N_XOR, new Integer(V_XOR));
    }

    //
    // Integer constants used to represent path types whenever we need
    // to save the initial represenation in a Vector.
    //

    private static Integer  INTEGER_SEG_MOVETO = new Integer(YOIX_SEG_MOVETO);
    private static Integer  INTEGER_SEG_LINETO = new Integer(YOIX_SEG_LINETO);
    private static Integer  INTEGER_SEG_QUADTO = new Integer(YOIX_SEG_QUADTO);
    private static Integer  INTEGER_SEG_CUBICTO = new Integer(YOIX_SEG_CUBICTO);
    private static Integer  INTEGER_SEG_CLOSE = new Integer(YOIX_SEG_CLOSE);

    //
    // This should be true when we need to compensate for a bug that first
    // appeared in Java 1.6.0. The fix is to explicitly close all subpaths
    // before doing a fill, which is something that Java used to do and we
    // believe is a bug that they need to fix. We expect there eventually
    // will be a Java fix, so make sure to check new Java versions as they
    // come out.
    //

    private static boolean  CLOSE_SUBPATHS = (YoixMisc.jvmCompareTo("1.6.0") >= 0);

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyPath(YoixObject data) {

	super(data);
	buildPath();
	setFixedSize();
	setPermissions(permissions);
	repaint();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(PATH);
    }

    ///////////////////////////////////
    //
    // YoixBodyPath Methods
    //
    ///////////////////////////////////

    final synchronized GeneralPath
    copyGeneralPath() {

	return((GeneralPath)path.clone());
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_ADD:
		obj = builtinConstruct(name, argv, V_ADD, YOIX_WIND_NON_ZERO);
		break;

	    case V_APPENDPATH:
		obj = builtinAppendPath(name, argv);
		break;

	    case V_ARC:
		obj = builtinArc(name, argv, false);
		break;

	    case V_ARCN:
		obj = builtinArc(name, argv, true);
		break;

	    case V_ARCT:		// low level code is probably missing
		obj = builtinArct(name, argv);
		break;

	    case V_CLOSEPATH:
		obj = builtinClosePath(name, argv);
		break;

	    case V_CURRENTPATH:
		obj = builtinCurrentPath(name, argv);
		break;

	    case V_CURRENTPOINT:
		obj = builtinCurrentPoint(name, argv);
		break;

	    case V_CURVETO:
		obj = builtinCurveTo(name, argv);
		break;

	    case V_EOADD:
		obj = builtinConstruct(name, argv, V_ADD, YOIX_WIND_EVEN_ODD);
		break;

	    case V_EOINTERSECT:
		obj = builtinConstruct(name, argv, V_INTERSECT, YOIX_WIND_EVEN_ODD);
		break;

	    case V_EOINTERSECTS:
		obj = builtinIntersects(name, argv, YOIX_WIND_EVEN_ODD);
		break;

	    case V_EOSUBTRACT:
		obj = builtinConstruct(name, argv, V_SUBTRACT, YOIX_WIND_EVEN_ODD);
		break;

	    case V_EOXOR:
		obj = builtinConstruct(name, argv, V_XOR, YOIX_WIND_EVEN_ODD);
		break;

	    case V_FLATTENPATH:
		obj = builtinFlattenPath(name, argv);
		break;

	    case V_INEOFILL:
		obj = builtinInEOFill(name, argv);
		break;

	    case V_INFILL:
		obj = builtinInFill(name, argv);
		break;

	    case V_INTERSECT:
		obj = builtinConstruct(name, argv, V_INTERSECT, YOIX_WIND_NON_ZERO);
		break;

	    case V_INTERSECTS:
		obj = builtinIntersects(name, argv, YOIX_WIND_NON_ZERO);
		break;

	    case V_INSTROKE:
		obj = builtinInStroke(name, argv);
		break;

	    case V_LINETO:
		obj = builtinLineTo(name, argv);
		break;

	    case V_MOVETO:
		obj = builtinMoveTo(name, argv);
		break;

	    case V_NEWPATH:
		obj = builtinNewPath(name, argv);
		break;

	    case V_PATHBBOX:
		obj = builtinPathBBox(name, argv);
		break;

	    case V_PATHFORALL:
		obj = builtinPathForAll(name, argv);
		break;

	    case V_QUADTO:
		obj = builtinQuadTo(name, argv);
		break;

	    case V_RCURVETO:
		obj = builtinRCurveTo(name, argv);
		break;

	    case V_RLINETO:
		obj = builtinRLineTo(name, argv);
		break;

	    case V_RMOVETO:
		obj = builtinRMoveTo(name, argv);
		break;

	    case V_ROTATEPATH:
		obj = builtinRotatePath(name, argv);
		break;

	    case V_RQUADTO:
		obj = builtinRQuadTo(name, argv);
		break;

	    case V_SCALEPATH:
		obj = builtinScalePath(name, argv);
		break;

	    case V_SHEARPATH:
		obj = builtinShearPath(name, argv);
		break;

	    case V_SUBTRACT:
		obj = builtinConstruct(name, argv, V_SUBTRACT, YOIX_WIND_NON_ZERO);
		break;

	    case V_TRANSFORMPATH:
		obj = builtinTransformPath(name, argv);
		break;

	    case V_TRANSLATEPATH:
		obj = builtinTranslatePath(name, argv);
		break;

	    case V_TRIMTOSTROKE:
		obj = builtinTrimToStroke(name, argv);
		break;

	    case V_XOR:
		obj = builtinConstruct(name, argv, V_XOR, YOIX_WIND_NON_ZERO);
		break;

	    default:
		obj = null;
		break;
	}

	return(obj);
    }


    protected final void
    finalize() {

	currentmatrix = null;
	path = null;
	super.finalize();
    }


    final synchronized Area
    getCurrentArea(int rule) {

	path.setWindingRule(rule == YOIX_WIND_EVEN_ODD ? YOIX_WIND_EVEN_ODD : YOIX_WIND_NON_ZERO);
	return(new Area(path));
    }


    final synchronized double[]
    getCurrentDevicePoint() {

	Point2D  point;
	double   pt[];

	if ((point = path.getCurrentPoint()) != null)
	    pt = new double[] {point.getX(), point.getY()};
	else pt = null;

	return(pt);
    }


    final synchronized double[]
    getCurrentPoint() {

	Point2D  point;
	double   pt[];

	if ((point = path.getCurrentPoint()) != null)
	    pt = currentmatrix.itransform(point.getX(), point.getY());
	else pt = null;

	return(pt);
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_CTM:
		obj = getCTM();
		break;

	    case V_ELEMENTS:
		obj = getElements();
		break;
	}

	return(obj);
    }


    final synchronized double[]
    getFirstDevicePoint() {

	PathIterator  pi;
	double        coords[];
	double        pt[];

	pi = path.getPathIterator(null);

	if (pi.isDone() == false) {
	    coords = new double[6];		// 2 should be sufficient
	    if (pi.currentSegment(coords) == YOIX_SEG_MOVETO)
		pt = new double[] {coords[0], coords[1]};
	    else pt = null;
	} else pt = null;

	return(pt);
    }


    protected final Object
    getManagedObject() {

	return(path);
    }


    final void
    paint(Rectangle bounds) {

	YoixObject  funct;
	YoixObject  args[];

	if ((funct = paint) != null && funct.notNull()) {
	    pathReset();
	    if (funct.callable(1)) {
		if (bounds == null)
		    args = new YoixObject[] {YoixObject.newNull()};
		else args = new YoixObject[] {YoixMake.yoixBBox(bounds)};
	    } else args = new YoixObject[0];
	    call(funct, args);
	}
    }


    final void
    pathAlignedRectangle(double x, double y, double width, double height) {

	double  corner1[];
	double  corner2[];
	double  corner3[];
	double  corner4[];

	corner1 = currentmatrix.transform(x, y);
	corner2 = currentmatrix.transform(x + width, y);
	corner3 = currentmatrix.transform(x + width, y + height);
	corner4 = currentmatrix.transform(x, y + height);

	corner1[0] = Math.floor(corner1[0]);
	corner1[1] = Math.floor(corner1[1]);
	corner2[0] = Math.floor(corner2[0]);
	corner2[1] = Math.floor(corner2[1]);
	corner3[0] = Math.floor(corner3[0]);
	corner3[1] = Math.floor(corner3[1]);
	corner4[0] = Math.floor(corner4[0]);
	corner4[1] = Math.floor(corner4[1]);

	path.moveTo((float)corner1[0], (float)corner1[1]);
	path.lineTo((float)corner2[0], (float)corner2[1]);
	path.lineTo((float)corner3[0], (float)corner3[1]);
	path.lineTo((float)corner4[0], (float)corner4[1]);
	path.closePath();
    }


    final void
    pathAlignedRectangleBottomRight(double x, double y, double width, double height, double border) {

	double  corner2[];
	double  corner3[];
	double  corner4[];
	double  corner6[];
	double  corner7[];
	double  corner8[];

	corner2 = currentmatrix.transform(x + width, y);
	corner3 = currentmatrix.transform(x + width, y + height);
	corner4 = currentmatrix.transform(x, y + height);
	corner6 = currentmatrix.transform(x + width - border, y + border);
	corner7 = currentmatrix.transform(x + width - border, y + height - border);
	corner8 = currentmatrix.transform(x + border, y + height - border);

	corner2[0] = Math.floor(corner2[0]);
	corner2[1] = Math.floor(corner2[1]);
	corner3[0] = Math.floor(corner3[0]);
	corner3[1] = Math.floor(corner3[1]);
	corner4[0] = Math.floor(corner4[0]);
	corner4[1] = Math.floor(corner4[1]);

	corner6[0] = Math.floor(corner6[0]);
	corner6[1] = Math.floor(corner6[1]);
	corner7[0] = Math.floor(corner7[0]);
	corner7[1] = Math.floor(corner7[1]);
	corner8[0] = Math.floor(corner8[0]);
	corner8[1] = Math.floor(corner8[1]);

	path.moveTo((float)corner3[0], (float)corner3[1]);
	path.lineTo((float)corner2[0], (float)corner2[1]);
	path.lineTo((float)corner6[0], (float)corner6[1]);
	path.lineTo((float)corner7[0], (float)corner7[1]);
	path.lineTo((float)corner8[0], (float)corner8[1]);
	path.lineTo((float)corner4[0], (float)corner4[1]);
	path.closePath();
    }


    final void
    pathAlignedRectangleTopLeft(double x, double y, double width, double height, double border) {

	double  corner1[];
	double  corner2[];
	double  corner4[];
	double  corner5[];
	double  corner6[];
	double  corner8[];

	corner1 = currentmatrix.transform(x, y);
	corner2 = currentmatrix.transform(x + width, y);
	corner4 = currentmatrix.transform(x, y + height);
	corner5 = currentmatrix.transform(x + border, y + border);
	corner6 = currentmatrix.transform(x + width - border, y + border);
	corner8 = currentmatrix.transform(x + border, y + height - border);

	corner1[0] = Math.floor(corner1[0]);
	corner1[1] = Math.floor(corner1[1]);
	corner2[0] = Math.floor(corner2[0]);
	corner2[1] = Math.floor(corner2[1]);
	corner4[0] = Math.floor(corner4[0]);
	corner4[1] = Math.floor(corner4[1]);

	corner5[0] = Math.floor(corner5[0]);
	corner5[1] = Math.floor(corner5[1]);
	corner6[0] = Math.floor(corner6[0]);
	corner6[1] = Math.floor(corner6[1]);
	corner8[0] = Math.floor(corner8[0]);
	corner8[1] = Math.floor(corner8[1]);

	path.moveTo((float)corner1[0], (float)corner1[1]);
	path.lineTo((float)corner2[0], (float)corner2[1]);
	path.lineTo((float)corner6[0], (float)corner6[1]);
	path.lineTo((float)corner5[0], (float)corner5[1]);
	path.lineTo((float)corner8[0], (float)corner8[1]);
	path.lineTo((float)corner4[0], (float)corner4[1]);
	path.closePath();
    }


    final synchronized void
    pathAppendShape(Shape shape) {

	path.append(shape, false);
    }


    final synchronized void
    pathClosePath() {

	path.closePath();
    }


    final synchronized void
    pathDraw(Graphics2D g) {

	AffineTransform  transform;
	AffineTransform  inverse;
	Point2D          point;
	Stroke           stroke;
	Shape            shape = path;
	int              type;

	//
	// Harder than you might expect because currentmatrix is supposed
	// to influence the conversion of path to the outline that's filled
	// when the path is stroked. It's particularly difficult when we're
	// dealing with non-uniform scaling because in that case we have to
	// take control if we want the effects of the non-uniform scaling
	// to be visible in the features (e.g., linewidth, linecaps) of the
	// path that we draw.
	//

	path.setWindingRule(YOIX_WIND_NON_ZERO);	// apparently doesn't matter

	stroke = g.getStroke();
	if (stroke instanceof BasicStroke) {
	    if (((BasicStroke)stroke).getLineWidth() > 0 || ((BasicStroke)stroke).getDashArray() != null) {
		transform = currentmatrix.getCurrentAffineTransform();
		if (transform.isIdentity() == false) {
		    type = transform.getType();
		    if ((type&AffineTransform.TYPE_UNIFORM_SCALE) != 0) {
			//
			// In this instance we should be able transform the
			// linewidth to device space, build a new BasicStroke
			// that uses the adjusted linewidth, and use that new
			// BasicStroke when we draw the shape.
			//
			point = new Point2D.Double(((BasicStroke)stroke).getLineWidth(), 0);
			point = transform.deltaTransform(point, null);
			g.setStroke(
			    new BasicStroke(
				(float)Math.sqrt(point.getX()*point.getX() + point.getY()*point.getY()),
				((BasicStroke)stroke).getEndCap(),
				((BasicStroke)stroke).getLineJoin(),
				((BasicStroke)stroke).getMiterLimit(),
				((BasicStroke)stroke).getDashArray(),
				((BasicStroke)stroke).getDashPhase()
			    )
			);
		    } else {
			try {
			    inverse = transform.createInverse();
			    shape = inverse.createTransformedShape(path);
			    g.transform(transform);
		        }
		        catch(NoninvertibleTransformException e) {
			    //
			    // Force a zero linewidth and a null dasharray in
			    // stroke??
			    //
			}
		    }
		}
	    }
	}

	g.draw(shape);
	g.setStroke(stroke);
    }


    final synchronized void
    pathEOFill(Graphics2D g) {

	path.setWindingRule(YOIX_WIND_EVEN_ODD);
	g.fill(CLOSE_SUBPATHS ? closeSubPaths(path) : path);
    }


    final synchronized void
    pathFill(Graphics2D g) {

	path.setWindingRule(YOIX_WIND_NON_ZERO);
	g.fill(CLOSE_SUBPATHS ? closeSubPaths(path) : path);
    }


    final synchronized boolean
    pathInEOFill(double x, double y) {

	double  pt[] = currentmatrix.transform(x, y);

	path.setWindingRule(YOIX_WIND_EVEN_ODD);
	return((CLOSE_SUBPATHS ? closeSubPaths(path) : path).contains(pt[0], pt[1]));
    }


    final synchronized boolean
    pathInFill(double x, double y) {

	double  pt[] = currentmatrix.transform(x, y);

	path.setWindingRule(YOIX_WIND_NON_ZERO);
	return((CLOSE_SUBPATHS ? closeSubPaths(path) : path).contains(pt[0], pt[1]));
    }


    final synchronized boolean
    pathInStroke(double x, double y, BasicStroke stroke) {

	AffineTransform  transform;
	AffineTransform  inverse;
	boolean          result = false;
	Shape            shape;

	//
	// We're assuming, for now anyway, that createStrokedShape() closes
	// all subpaths, so we can ignore CLOSE_SUBPATHS. Didn't run tests
	// to verify this assumption, so there's a chance we could be wrong.
	//

	if (stroke != null) {
	    path.setWindingRule(YOIX_WIND_NON_ZERO);	// apparently doesn't matter
	    transform = currentmatrix.getCurrentAffineTransform();
	    try {
		inverse = transform.createInverse();
		shape = stroke.createStrokedShape(inverse.createTransformedShape(path));
		result = shape.contains(x, y);
	    }
	    catch(NoninvertibleTransformException e) {}
	}

	return(result);
    }


    final void
    pathMoveTo(double x, double y) {

	addMoveTo(x, y);
    }


    final void
    pathLineTo(double x, double y) {

	addLineTo(x, y);
    }


    final void
    pathRectangle(double x, double y, double width, double height) {

	addMoveTo(x, y);
	addLineTo(x + width, y);
	addLineTo(x + width, y + height);
	addLineTo(x, y + height);
	path.closePath();
    }


    final synchronized void
    pathReset() {

	path.reset();
    }


    final void
    pathRestore(Object details) {

	if (details instanceof Shape)
	    pathSetShape((Shape)details);
	else VM.die(INTERNALERROR);
    }


    final void
    pathRLineTo(double dx, double dy) {

	if (dx != 0 || dy != 0)
	    addRLineTo(dx, dy);
    }


    final void
    pathRMoveTo(double dx, double dy) {

	if (dx != 0 || dy != 0)
	    addRMoveTo(dx, dy);
    }


    final Object
    pathSave() {

	return(path.clone());
    }


    final synchronized void
    pathSetShape(Shape shape) {

	path.reset();
	path.append(shape, false);
    }


    final synchronized void
    pathSetShape(Area area) {

	Rectangle2D  rect;

	//
	// This method was added on 1/10/08 to address a problem that we
	// observed when a Yoix script used something like
	//
	//	gsave();
	//	clippath();
	//	pathbbox();
	//	...
	//	grestore();
	//
	// in a paint() function of a window that we were rapidly resizing.
	// Problem was that clippath() would occasionally hand us an empty
	// which resulted in an emtpy path and caused pathbbox() to abort
	// because there wasn't a current point (fixed on 1/10/08).
	//

	path.reset();
	if (area.isEmpty()) {
	    rect = area.getBounds2D();
	    path.moveTo((float)rect.getX(), (float)rect.getY());
	} else path.append(area, false);
    }


    final synchronized void
    pathStrokePath(BasicStroke stroke) {

	AffineTransform  transform;
	AffineTransform  inverse;
	Point2D          point;
	Shape            shape;
	int              type;

	//
	// This is much harder than you might expect because currentmatrix
	// is supposed to influence the conversion of path to the outline
	// that represents the stroked path but there's no way to get that
	// information to the stroke.createStrokedShape() method. Most of
	// the time we're probably dealing with uniform scaling transform
	// so we should be able to ignore the complications, but if not we
	// have to transform the entire path back to user space before we
	// use createStrokedShape(), and then transform the result back to
	// device space if we expect the effects to be visible in features
	// like linecaps and linewidths.
	//

	path.setWindingRule(YOIX_WIND_NON_ZERO);	// apparently doesn't matter
	if (stroke.getLineWidth() > 0 || stroke.getDashArray() != null) {
	    transform = currentmatrix.getCurrentAffineTransform();
	    if (transform.isIdentity() == false) {
		type = transform.getType();
		if ((type&AffineTransform.TYPE_UNIFORM_SCALE) != 0) {
		    //
		    // In this instance we should be able transform the
		    // linewidth to device space, build a new BasicStroke
		    // that uses the adjusted linewidth, and use that new
		    // BasicStroke to obtain the outline. In addition we
		    // also make sure the calculated linewidth in device
		    // space is at least one pixel, otherwise portions of
		    // the path might not show up when the resulting path
		    // is filled. Results were inconsistent and probably
		    // also device dependent when the linewidth was less
		    // than one and we omitted the adjustment or used the
		    // other branch (which seems like it should worked).
		    // This change was added on 3/6/08.
		    //
		    point = new Point2D.Double(stroke.getLineWidth(), 0);
		    point = transform.deltaTransform(point, null);
		    stroke = new BasicStroke(
			(float)Math.max(1.0, Math.sqrt(point.getX()*point.getX() + point.getY()*point.getY())),
			stroke.getEndCap(),
			stroke.getLineJoin(),
			stroke.getMiterLimit(),
			stroke.getDashArray(),
			stroke.getDashPhase()
		    );
		    shape = stroke.createStrokedShape(path);
		} else {
		    //
		    // Prior to 3/6/08 there was no TYPE_UNIFORM_SCALE
		    // checking and all non-zero linewidth cases that
		    // weren't using the identity matrix went through
		    // this code. Unfortunately filling stroked paths
		    // when the linewidth was small often didn't yield
		    // good results, so we added a TYPE_UNIFORM_SCALE
		    // check that hopefully reserves this branch for
		    // cases that really need it.
		    //
		    try {
			inverse = transform.createInverse();
			shape = stroke.createStrokedShape(inverse.createTransformedShape(path));
			shape = transform.createTransformedShape(shape);
		    }
		    catch(NoninvertibleTransformException e) {
			//
			// Force a zero linewidth and a null dasharray in
			// stroke?? Not sure what's best, so leaving it be
			// for now.
			//
			shape = stroke.createStrokedShape(path);
		    }
		}
	    } else shape = stroke.createStrokedShape(path);
	} else shape = stroke.createStrokedShape(path);
	path.reset();
	path.append(shape, false);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_CTM:
		    setCTM(obj);
		    break;

		case V_PAINT:
		    setPaint(obj);
		    break;
	    }
	}

	return(obj);
    }


    final void
    setOwner(YoixBodyGraphics owner) {

	if (owner != null)
	    currentmatrix = (YoixBodyMatrix)owner.getField(N_CTM, null).body();
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    addArc(double xc, double yc, double r, double angle1, double angle2, double sx, double sy, boolean clockwise) {

	YoixBodyMatrix  matrix;
	double          pt0[];
	double          pt1[];
	double          pt2[];
	double          a1;
	double          am;
	double          a2;
	double          x0;
	double          y0;
	double          xm;
	double          ym;
	double          x3;
	double          y3;
	double          len;
	double          da;
	double          da4;

	//
	// The conditional angle recalculation designed to match enpoints
	// with the arc orientation agrees with PostScript interpreters,
	// even though something like,
	//
	//	angle1 + 2*Math.PI*Math.floor(da/(2*Math.PI)) + da%(2*Math.PI)
	//
	// which adjusts angle2 when we're drawing a counterclockwise arc,
	// probably is more consistent. PostScript tests were run using gs
	// and at least one Adobe interpreter.
	//

	if (clockwise) {
	    if (angle1 < angle2) {
		da = angle2 - angle1;
		angle1 = angle2 + 2*Math.PI - (da%(2*Math.PI));
	    }
	    da = -Math.PI/2;
	} else {
	    if (angle2 < angle1) {
		da = angle1 - angle2;
		angle2 = angle1 + 2*Math.PI - (da%(2*Math.PI));
	    }
	    da = Math.PI/2;
	}

	if (sx != 1.0 || sy != 1.0) {
	    matrix = (YoixBodyMatrix)currentmatrix.clone();
	    matrix.scale(sx, sy);
	} else matrix = currentmatrix;

	while (angle1 != angle2) {
	    a1 = angle1;
	    a2 = Math.abs(angle2 - angle1) <= .75*Math.PI ? angle2 : angle1 + da;
	    am = (a1 + a2)/2.0;

	    x0 = xc + r*Math.cos(a1);
	    y0 = yc + r*Math.sin(a1);
	    xm = xc + r*Math.cos(am);
	    ym = yc + r*Math.sin(am);
	    x3 = xc + r*Math.cos(a2);
	    y3 = yc + r*Math.sin(a2);

	    da4 = (a2 - a1)/4.0;
	    len = r/Math.cos(da4);

	    pt0 = matrix.transform(
		4.0*(xc - x0 + len*Math.cos(a1 + da4))/3.0 + x0,
		4.0*(yc - y0 + len*Math.sin(a1 + da4))/3.0 + y0
	    );
	    pt1 = matrix.transform(
		4.0*(xc - x3 + len*Math.cos(a2 - da4))/3.0 + x3,
		4.0*(yc - y3 + len*Math.sin(a2 - da4))/3.0 + y3
	    );
	    pt2 = matrix.transform(x3, y3);

	    try {
		path.curveTo(
		    (float)pt0[0], (float)pt0[1],
		    (float)pt1[0], (float)pt1[1],
		    (float)pt2[0], (float)pt2[1]
		);
	    }
	    catch(IllegalPathStateException e) {
		VM.abort(NOCURRENTPOINT);
	    }

	    angle1 = a2;
	}
    }


    private void
    addArct(double x0, double y0, double x1, double y1, double x2, double y2, double r, double sx, double sy) {

	VM.abort(UNIMPLEMENTED);
    }


    private void
    addCurveTo(double x0, double y0, double x1, double y1, double x2, double y2) {

	double  pt0[] = currentmatrix.transform(x0, y0);
	double  pt1[] = currentmatrix.transform(x1, y1);
	double  pt2[] = currentmatrix.transform(x2, y2);

	try {
	    path.curveTo(
		(float)pt0[0], (float)pt0[1],
		(float)pt1[0], (float)pt1[1],
		(float)pt2[0], (float)pt2[1]
	    );
	}
	catch(IllegalPathStateException e) {
	    VM.abort(NOCURRENTPOINT);
	}
    }


    private void
    addLineTo(double x, double y) {

	double  pt[] = currentmatrix.transform(x, y);

	try {
	    path.lineTo((float)pt[0], (float)pt[1]);
	}
	catch(IllegalPathStateException e) {
	    VM.abort(NOCURRENTPOINT);
	}
    }


    private void
    addMoveTo(double x, double y) {

	double  pt[] = currentmatrix.transform(x, y);

	path.moveTo((float)pt[0], (float)pt[1]);
    }


    private void
    addQuadTo(double x0, double y0, double x1, double y1) {

	double  pt0[] = currentmatrix.transform(x0, y0);
	double  pt1[] = currentmatrix.transform(x1, y1);

	try {
	    path.quadTo(
		(float)pt0[0], (float)pt0[1],
		(float)pt1[0], (float)pt1[1]
	    );
	}
	catch(IllegalPathStateException e) {
	    VM.abort(NOCURRENTPOINT);
	}
    }


    private void
    addRCurveTo(double dx0, double dy0, double dx1, double dy1, double dx2, double dy2) {

	Point2D  point;
	double   delta0[];
	double   delta1[];
	double   delta2[];
	double   x;
	double   y;

	try {
	    point = path.getCurrentPoint();
	    x = point.getX();
	    y = point.getY();
	    delta0 = currentmatrix.dtransform(dx0, dy0);
	    delta1 = currentmatrix.dtransform(dx1, dy1);
	    delta2 = currentmatrix.dtransform(dx2, dy2);
	    path.curveTo(
		(float)(x + delta0[0]), (float)(y + delta0[1]),
		(float)(x + delta1[0]), (float)(y + delta1[1]),
		(float)(x + delta2[0]), (float)(y + delta2[1])
	    );
	}
	catch(NullPointerException e) {
	    VM.abort(NOCURRENTPOINT);
	}
	catch(IllegalPathStateException e) {
	    VM.abort(NOCURRENTPOINT);
	}
    }


    private void
    addRLineTo(double dx, double dy) {

	Point2D  point;
	double   delta[];
	double   x;
	double   y;

	try {
	    point = path.getCurrentPoint();
	    x = point.getX();
	    y = point.getY();
	    delta = currentmatrix.dtransform(dx, dy);
	    path.lineTo((float)(x + delta[0]), (float)(y + delta[1]));
	}
	catch(NullPointerException e) {
	    VM.abort(NOCURRENTPOINT);
	}
	catch(IllegalPathStateException e) {
	    VM.abort(NOCURRENTPOINT);
	}
    }


    private void
    addRMoveTo(double dx, double dy) {

	Point2D  point;
	double   delta[];
	double   x;
	double   y;

	try {
	    point = path.getCurrentPoint();
	    x = point.getX();
	    y = point.getY();
	    delta = currentmatrix.dtransform(dx, dy);
	    path.moveTo((float)(x + delta[0]), (float)(y + delta[1]));
	}
	catch(NullPointerException e) {
	    VM.abort(NOCURRENTPOINT);
	}
	catch(IllegalPathStateException e) {
	    VM.abort(NOCURRENTPOINT);
	}
    }


    private void
    addRQuadTo(double dx0, double dy0, double dx1, double dy1) {

	Point2D  point;
	double   delta0[];
	double   delta1[];
	double   x;
	double   y;

	try {
	    point = path.getCurrentPoint();
	    x = point.getX();
	    y = point.getY();
	    delta0 = currentmatrix.dtransform(dx0, dy0);
	    delta1 = currentmatrix.dtransform(dx1, dy1);
	    path.quadTo(
		(float)(x + delta0[0]), (float)(y + delta0[1]),
		(float)(x + delta1[0]), (float)(y + delta1[1])
	    );
	}
	catch(NullPointerException e) {
	    VM.abort(NOCURRENTPOINT);
	}
	catch(IllegalPathStateException e) {
	    VM.abort(NOCURRENTPOINT);
	}
    }


    private void
    buildPath() {

	initialized = false;
	path = new GeneralPath();
	currentmatrix = (YoixBodyMatrix)YoixMake.yoixType(T_MATRIX).body();
	setField(N_CTM);
	setField(N_PAINT);
	initialized = true;
    }


    private synchronized YoixObject
    builtinAppendPath(String name, YoixObject arg[]) {

	AffineTransform  aft;
	YoixBodyPath     right;
	GeneralPath      rhs;
	boolean          connect;
	double           first[];
	double           origin[];
	double           dx;
	double           dy;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isPath()) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    right = (YoixBodyPath)arg[0].body();
		    if ((first = right.getFirstDevicePoint()) != null) {
			rhs = right.copyGeneralPath();
			if ((origin = getCurrentDevicePoint()) != null) {
			    dx = origin[0];	// was - first[0];
			    dy = origin[1];	// was - first[1];
			    if (dx != 0 || dy != 0) {
				aft = new AffineTransform();
				aft.setToTranslation(dx, dy);
				rhs.transform(aft);
			    }
			    connect = (arg.length == 1) ? false : !arg[1].booleanValue();
			    path.append(rhs, connect);
			} else pathSetShape(rhs);
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinArc(String name, YoixObject arg[], boolean clockwise) {

	double  values[];
	double  x0;
	double  y0;
	int     n;

	if (arg.length == 5 || arg.length == 7) {
	    values = new double[7];
	    for (n = 0; n < arg.length; n++) {
		if (arg[n].isNumber())
		    values[n] = arg[n].doubleValue();
		else VM.badArgument(name, n);
	    }
	    if (n == 5) {
		values[5] = 1.0;
		values[6] = 1.0;
	    }
	    values[3] = values[3]*Math.PI/180.0;
	    values[4] = values[4]*Math.PI/180.0;
	    x0 = values[0] + values[5]*values[2]*Math.cos(values[3]);
	    y0 = values[1] + values[6]*values[2]*Math.sin(values[3]);
	    if (path.getCurrentPoint() != null)
		addLineTo(x0, y0);
	    else addMoveTo(x0, y0);
	    addArc(
		values[0]/values[5], values[1]/values[6],
		values[2],
		values[3], values[4],
		values[5], values[6],
		clockwise
	    );
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinArct(String name, YoixObject arg[]) {

	Point2D  point;
	double   values[];
	double   pt[];
	int      n;

	if (arg.length == 5 || arg.length == 7) {
	    values = new double[7];
	    for (n = 0; n < 5; n++) {
		if (arg[n].isNumber())
		    values[n] = arg[n].doubleValue();
		else VM.badArgument(name, n);
	    }
	    if (n == 5) {
		values[5] = 1.0;
		values[6] = 1.0;
	    }
	    if ((point = path.getCurrentPoint()) != null) {
		pt = currentmatrix.itransform(point.getX(), point.getY());
		addArct(
		    pt[0], pt[1],
		    values[0], values[1],
		    values[2], values[3],
		    values[4],
		    values[5], values[6]
		);
	    } else VM.abort(NOCURRENTPOINT, name);

	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinClosePath(String name, YoixObject arg[]) {

	if (arg.length == 0) {
	    try {
		path.closePath();
	    }
	    catch(IllegalPathStateException e) {
		VM.abort(NOCURRENTPOINT);
	    }
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinConstruct(String name, YoixObject arg[], int operation, int rule) {

	Area  lhs;
	Area  rhs;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isPath() || arg[0].isRectangle() || arg[0].isNull()) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    lhs = getCurrentArea(rule);
		    rule = (arg.length == 2) ? arg[1].intValue() : rule;
		    if (arg[0].isPath())
			rhs = ((YoixBodyPath)arg[0].body()).getCurrentArea(rule);
		    else if (arg[0].isRectangle())
			rhs = getAlignedRectangleArea(arg[0].getDouble(N_X, 0), arg[0].getDouble(N_Y, 0), arg[0].getDouble(N_WIDTH, 0), arg[0].getDouble(N_HEIGHT, 0));
		    else rhs = new Area();
		    switch (operation) {
			case V_ADD:
			    lhs.add(rhs);
			    break;

			case V_INTERSECT:
			    lhs.intersect(rhs);
			    break;

			case V_SUBTRACT:
			    lhs.subtract(rhs);
			    break;

			case V_XOR:
			    lhs.exclusiveOr(rhs);
			    break;
		    }
		    path = new GeneralPath(lhs);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinCurrentPath(String name, YoixObject arg[]) {

	YoixBodyPath  copy;
	YoixObject    obj = null;

	if (arg.length == 0) {
	    if (path != null) {
		copy = new YoixBodyPath(getData());
		copy.pathRestore(this.pathSave());
		obj = YoixObject.newPointer(copy);
	    }
	} else VM.badCall(name);

	return(obj != null ? obj : YoixObject.newNull());
    }


    private synchronized YoixObject
    builtinCurrentPoint(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	double      pt[];

	if (arg.length == 0) {
	    if ((pt = getCurrentPoint()) != null)
		obj = YoixObject.newPoint(pt[0], pt[1]);
	} else VM.badCall(name);

	return(obj != null ? obj : YoixObject.newNull());
    }


    private synchronized YoixObject
    builtinCurveTo(String name, YoixObject arg[]) {

	if (arg.length == 3 || arg.length == 6) {
	    if (arg.length == 3) {
		if (arg[0].isPoint()) {
		    if (arg[1].isPoint()) {
			if (arg[2].isPoint()) {
			    addCurveTo(
				arg[0].getDouble(N_X, 0), arg[0].getDouble(N_Y, 0),
				arg[1].getDouble(N_X, 0), arg[1].getDouble(N_Y, 0),
				arg[2].getDouble(N_X, 0), arg[2].getDouble(N_Y, 0)
			    );
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    if (arg[3].isNumber()) {
				if (arg[4].isNumber()) {
				    if (arg[5].isNumber()) {
					addCurveTo(
					    arg[0].doubleValue(), arg[1].doubleValue(),
					    arg[2].doubleValue(), arg[3].doubleValue(),
					    arg[4].doubleValue(), arg[5].doubleValue()
					);
				    } else VM.badArgument(name, 5);
				} else VM.badArgument(name, 4);
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinFlattenPath(String name, YoixObject arg[]) {

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 0 || arg[0].isNumber())
		path = getFlattenedPath(arg.length == 1 ? arg[0].doubleValue() : .5);
	    else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinInEOFill(String name, YoixObject arg[]) {

	boolean  result = false;
	double   x = 0;
	double   y = 0;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].isPoint()) {
		    x = arg[0].getDouble(N_X, 0);
		    y = arg[0].getDouble(N_Y, 0);
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			x = arg[0].doubleValue();
			y = arg[1].doubleValue();
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	    result = pathInEOFill(x, y);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    builtinInFill(String name, YoixObject arg[]) {

	boolean  result = false;
	double   x = 0;
	double   y = 0;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].isPoint()) {
		    x = arg[0].getDouble(N_X, 0);
		    y = arg[0].getDouble(N_Y, 0);
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			x = arg[0].doubleValue();
			y = arg[1].doubleValue();
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	    result = pathInFill(x, y);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    builtinInStroke(String name, YoixObject arg[]) {

	BasicStroke  stroke;
	boolean      result = false;
	double       x = 0;
	double       y = 0;

	//
	// We added linewidth, linecap, linejoin, and miterlimit fields to
	// paths, even though they're only used here, so we could give the
	// user better control over the stroke that we use. The old version
	// built a BasicStroke with default values and a test implementation
	// tried passing stroke values as optional arguments. This approach
	// seems like the most consistent when compared to YoixBodyGraphics,
	// which is why we picked it. Change was made on 4/3/08.
	// 

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].isPoint()) {
		    x = arg[0].getDouble(N_X, 0);
		    y = arg[0].getDouble(N_Y, 0);
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			x = arg[0].doubleValue();
			y = arg[1].doubleValue();
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	    stroke = new BasicStroke(
		data.getFloat(N_LINEWIDTH, 1.0f),
		data.getInt(N_LINECAP, BasicStroke.CAP_BUTT),
		data.getInt(N_LINEJOIN, BasicStroke.JOIN_MITER),
		data.getFloat(N_MITERLIMIT, 10.0f)
	    );
	    result = pathInStroke(x, y, stroke);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    builtinIntersects(String name, YoixObject arg[], int rule) {

	boolean  result = false;
	double   corner[];
	double   x;
	double   y;
	double   width;
	double   height;
	Area     lhs;
	Area     rhs;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isPath() || arg[0].isRectangle() || arg[0].isNull()) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    if (arg[0].notNull()) {
			lhs = getCurrentArea(rule);
			if (lhs.isEmpty() == false) {
			    if (arg[0].isPath()) {
				rule = (arg.length == 2) ? arg[1].intValue() : rule;
				rhs = ((YoixBodyPath)arg[0].body()).getCurrentArea(rule);
				lhs.intersect(rhs);
				result = (lhs.isEmpty() == false);
			    } else {
				x = arg[0].getDouble(N_X, 0);
				y = arg[0].getDouble(N_Y, 0);
				width = arg[0].getDouble(N_WIDTH, 0);
				height = arg[0].getDouble(N_HEIGHT, 0);
				rhs = getAlignedRectangleArea(x, y, width, height);
				if (rhs.isEmpty() == false) {
				    lhs.intersect(rhs);
				    result = (lhs.isEmpty() == false);
				} else if (width != 0 && height != 0) {
				    //
				    // This situation can happen when corners of
				    // the aligned rectangle are mapped to the
				    // same pixel. Only an issue because we used
				    // getAlignedRectangleArea() which makes our
				    // answer compatible with rectangle builtins
				    // like rectfill().
				    //
				    corner = currentmatrix.transform(x, y);
				    result = lhs.contains(Math.floor(corner[0]), Math.floor(corner[1]));
				}
			    }
			}
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    builtinLineTo(String name, YoixObject arg[]) {

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].isPoint())
		    addLineTo(arg[0].getDouble(N_X, 0), arg[0].getDouble(N_Y, 0));
		else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber())
			addLineTo(arg[0].doubleValue(), arg[1].doubleValue());
		    else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinMoveTo(String name, YoixObject arg[]) {

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].isPoint())
		    addMoveTo(arg[0].getDouble(N_X, 0), arg[0].getDouble(N_Y, 0));
		else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber())
			addMoveTo(arg[0].doubleValue(), arg[1].doubleValue());
		    else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinPathBBox(String name, YoixObject arg[]) {

	GeneralPath  target;
	Rectangle2D  rect;
	YoixObject   obj = null;
	double       flatness;
	double       pixels;

	//
	// The pixels argument is a kludge that helped an application use
	// a slightly expanded pathbbox() for rather complicated paths as
	// a way to determine whether that path intersected the clipping
	// path and therefore needed to be stroked. We tried alternatives,
	// like using stokepath and flattenpath, but there always seemed
	// some cases where the intersection check failed. This definitely
	// is a kludge that likely will not be documented, however we need
	// to track the real cause down. Happened in PT when we marked the
	// table with a path of curves near the top and slowly panned up
	// and down at different (usually small) scalings using automatic
	// panning code in YoixSwingJCanvas.java to handle painting and
	// copyarea. There are a number of possible culprits and it could
	// take a few days to really track down and fix.
	//

	if (arg.length <= 2) {
	    if (arg.length == 0 || arg[0].isNumber()) {
		if (arg.length <= 1 || arg[1].isNumber()) {
		    if (getCurrentPoint() != null) {
			flatness = (arg.length > 0) ? arg[0].doubleValue() : 0;
			pixels = (arg.length > 1) ? arg[1].doubleValue() : 0;
			if (flatness > 0)
			    target = getFlattenedPath(flatness);
			else target = path;
			rect = target.getBounds2D();
			if (pixels != 0) {		// small kludge
			    rect.setRect(
				rect.getX() - pixels,
				rect.getY() - pixels,
				rect.getWidth() + 2*pixels,
				rect.getHeight() + 2*pixels
			    );
			}
			rect = YoixMake.javaBBox(rect, currentmatrix);
			obj = YoixMake.yoixType(T_RECTANGLE);
			obj.put(N_X, YoixObject.newDouble(rect.getX()), false);
			obj.put(N_Y, YoixObject.newDouble(rect.getY()), false);
			obj.put(N_WIDTH, YoixObject.newDouble(rect.getWidth()), false);
			obj.put(N_HEIGHT, YoixObject.newDouble(rect.getHeight()), false);
		    } else VM.abort(NOCURRENTPOINT, name);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(obj);
    }


    private synchronized YoixObject
    builtinPathForAll(String name, YoixObject arg[]) {

	PathIterator  pi;
	YoixObject    funct;
	YoixObject    args[];
	YoixObject    extra;
	double        coords[];
	double        pt[];
	int           incr;
	int           type;
	int           argc;
	int           m;
	int           n;

	if (arg.length == 1 || arg.length == 2 || arg.length == 5) {
	    if (arg.length == 1 || arg.length == 2) {
		if (arg[0].isCallable()) {
		    funct = arg[0];
		    if (arg.length == 1) {
			extra = null;
			incr = 1;
		    } else {
			extra = arg[1];
			incr = 2;
		    }
		    coords = new double[6];
		    pi = path.getPathIterator(null);
		    for (; pi.isDone() == false; pi.next()) {
			argc = -1;
			switch (type = pi.currentSegment(coords)) {
			    case YOIX_SEG_MOVETO:
			    case YOIX_SEG_LINETO:
				argc = 2;
				break;

			    case YOIX_SEG_QUADTO:
				argc = 4;
				break;

			    case YOIX_SEG_CUBICTO:
				argc = 6;
				break;

			    case YOIX_SEG_CLOSE:
				argc = 0;
				break;
			}
			if (argc >= 0) {
			    if (funct.callable(argc + incr)) {
				args = new YoixObject[argc + incr];
				args[0] = YoixObject.newInt(type);
				for (n = 0, m = 1; n < argc; n += 2, m += 2) {
				    pt = currentmatrix.itransform(coords[n], coords[n+1]);
				    args[m] = YoixObject.newDouble(pt[0]);
				    args[m+1] = YoixObject.newDouble(pt[1]);
				}
				if (extra != null)
				    args[m] = extra;
				funct.call(args, null);
			    }
			}
		    }
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].callable(2) || arg[0].isNull()) {
		    if (arg[1].callable(2) || arg[1].isNull()) {
			if (arg[2].callable(4) || arg[2].isNull()) {
			    if (arg[3].callable(6) || arg[3].isNull()) {
				if (arg[4].callable(0) || arg[4].isNull()) {
				    coords = new double[6];
				    pi = path.getPathIterator(null);
				    for (; pi.isDone() == false; pi.next()) {
					funct = null;
					argc = 0;
					switch (pi.currentSegment(coords)) {
					    case YOIX_SEG_MOVETO:
						funct = arg[0];
						argc = 2;
						break;

					    case YOIX_SEG_LINETO:
						funct = arg[1];
						argc = 2;
						break;

					    case YOIX_SEG_QUADTO:
						funct = arg[2];
						argc = 4;
						break;

					    case YOIX_SEG_CUBICTO:
						funct = arg[3];
						argc = 6;
						break;

					    case YOIX_SEG_CLOSE:
						funct = arg[4];
						argc = 0;
						break;
					}
					if (funct != null && funct.notNull()) {
					    args = new YoixObject[argc];
					    for (n = 0; n < argc; n += 2) {
						pt = currentmatrix.itransform(coords[n], coords[n+1]);
						args[n] = YoixObject.newDouble(pt[0]);
						args[n+1] = YoixObject.newDouble(pt[1]);
					    }
					    funct.call(args, null);
					}
				    }
				} else VM.badArgument(name, 4);
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinNewPath(String name, YoixObject arg[]) {

	if (arg.length == 0)
	    pathReset();
	else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinQuadTo(String name, YoixObject arg[]) {

	if (arg.length == 2 || arg.length == 4) {
	    if (arg.length == 2) {
		if (arg[0].isPoint()) {
		    if (arg[1].isPoint()) {
			addQuadTo(
			    arg[0].getDouble(N_X, 0), arg[0].getDouble(N_Y, 0),
			    arg[1].getDouble(N_X, 0), arg[1].getDouble(N_Y, 0)
			);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    if (arg[3].isNumber()) {
				addQuadTo(
				    arg[0].doubleValue(), arg[1].doubleValue(),
				    arg[2].doubleValue(), arg[3].doubleValue()
				);
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinRCurveTo(String name, YoixObject arg[]) {

	if (arg.length == 3 || arg.length == 6) {
	    if (arg.length == 3) {
		if (arg[0].isPoint()) {
		    if (arg[1].isPoint()) {
			if (arg[2].isPoint()) {
			    addRCurveTo(
				arg[0].getDouble(N_X, 0), arg[0].getDouble(N_Y, 0),
				arg[1].getDouble(N_X, 0), arg[1].getDouble(N_Y, 0),
				arg[2].getDouble(N_X, 0), arg[2].getDouble(N_Y, 0)
			    );
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    if (arg[3].isNumber()) {
				if (arg[4].isNumber()) {
				    if (arg[5].isNumber()) {
					addRCurveTo(
					    arg[0].doubleValue(), arg[1].doubleValue(),
					    arg[2].doubleValue(), arg[3].doubleValue(),
					    arg[4].doubleValue(), arg[5].doubleValue()
					);
				    } else VM.badArgument(name, 5);
				} else VM.badArgument(name, 4);
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinRLineTo(String name, YoixObject arg[]) {

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].isPoint())
		    addRLineTo(arg[0].getDouble(N_X, 0), arg[0].getDouble(N_Y, 0));
		else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber())
			addRLineTo(arg[0].doubleValue(), arg[1].doubleValue());
		    else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinRMoveTo(String name, YoixObject arg[]) {

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].isPoint())
		    addRMoveTo(arg[0].getDouble(N_X, 0), arg[0].getDouble(N_Y, 0));
		else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber())
			addRMoveTo(arg[0].doubleValue(), arg[1].doubleValue());
		    else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinRotatePath(String name, YoixObject arg[]) {

	AffineTransform  aft;
	double           angle;

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
	        if ((angle = arg[0].doubleValue()) != 0) {
		    if (getCurrentDevicePoint() != null) {
		        aft = new AffineTransform();
		        aft.rotate((angle * Math.PI)/180.0);
		        path.transform(aft);
		    }
		}
	    } else VM.badArgument(name, 0);
	}

	return(getContext());
    }


    private synchronized YoixObject
    builtinRQuadTo(String name, YoixObject arg[]) {

	if (arg.length == 2 || arg.length == 4) {
	    if (arg.length == 2) {
		if (arg[0].isPoint()) {
		    if (arg[1].isPoint()) {
			addRQuadTo(
			    arg[0].getDouble(N_X, 0), arg[0].getDouble(N_Y, 0),
			    arg[1].getDouble(N_X, 0), arg[1].getDouble(N_Y, 0)
			);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    if (arg[3].isNumber()) {
				addRQuadTo(
				    arg[0].doubleValue(), arg[1].doubleValue(),
				    arg[2].doubleValue(), arg[3].doubleValue()
				);
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinScalePath(String name, YoixObject arg[]) {

	AffineTransform  aft;
        double           xscale, yscale;

	if (arg.length == 2) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
                    xscale = arg[0].doubleValue();
                    yscale = arg[1].doubleValue();
                    if (xscale != 1 || yscale != 1) {
			aft = new AffineTransform();
			aft.scale(xscale, yscale);
			path.transform(aft);
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	}

	return(getContext());
    }


    private synchronized YoixObject
    builtinShearPath(String name, YoixObject arg[]) {

	AffineTransform  aft;

	if (arg.length == 2) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (getCurrentDevicePoint() != null) {
			aft = new AffineTransform();
			aft.shear(arg[0].doubleValue(), arg[1].doubleValue());
			path.transform(aft);
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	}

	return(getContext());
    }


    private synchronized YoixObject
    builtinTransformPath(String name, YoixObject arg[]) {

	AffineTransform  aft;

	if (arg.length == 1) {
	    if (arg[0].isMatrix()) {
		if (getCurrentDevicePoint() != null) {
		    aft = ((YoixBodyMatrix)arg[0].body()).getCurrentAffineTransform();
		    path.transform(aft);
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinTranslatePath(String name, YoixObject arg[]) {

	AffineTransform  aft;
	double           pt[];

	if (arg.length == 2) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (getCurrentDevicePoint() != null) {
			pt = currentmatrix.dtransform(arg[0].doubleValue(), arg[1].doubleValue());
			aft = new AffineTransform();
			aft.translate(pt[0], pt[1]);
			path.transform(aft);
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	}

	return(getContext());
    }


    private synchronized YoixObject
    builtinTrimToStroke(String name, YoixObject arg[]) {

	PathIterator  pi;
	YoixBodyPath  body;
	GeneralPath   trimmed;
	GeneralPath   segment;
	BasicStroke   stroke;
	YoixObject    obj = null;
	Point2D       point;
	double        flatness;
	float         coords[];
	Area          covered;
	Area          area;

	if (arg.length == 0) {
	    if (path != null) {
		flatness = 0.0;
		stroke = new BasicStroke(
		    data.getFloat(N_LINEWIDTH, 1.0f),
		    data.getInt(N_LINECAP, BasicStroke.CAP_BUTT),
		    data.getInt(N_LINEJOIN, BasicStroke.JOIN_MITER),
		    data.getFloat(N_MITERLIMIT, 10.0f)
		);
		coords = new float[6];
		trimmed = new GeneralPath();
		segment = new GeneralPath();
		covered = new Area();
		for (pi = path.getPathIterator(null, flatness); pi.isDone() == false; pi.next()) {
		    switch (pi.currentSegment(coords)) {
			case YOIX_SEG_MOVETO:
			    trimmed.moveTo(coords[0], coords[1]);
			    break;

			case YOIX_SEG_LINETO:
			    if ((point = trimmed.getCurrentPoint()) != null) {
				segment.reset();
				segment.moveTo((float)point.getX(), (float)point.getY());
				segment.lineTo(coords[0], coords[1]);
				area = new Area(stroke.createStrokedShape(segment));
				area.subtract(covered);
				if (area.isEmpty() == false) {
				    covered.add(area);
				    trimmed.moveTo((float)point.getX(), (float)point.getY());
				    trimmed.lineTo(coords[0], coords[1]);
				} else trimmed.moveTo(coords[0], coords[1]);
			    }
			    break;

			case YOIX_SEG_CLOSE:
			    trimmed.closePath();
			    break;
		    }
		}

		body = new YoixBodyPath(getData());
		body.pathRestore(trimmed);
		obj = YoixObject.newPointer(body);
	    }
	} else VM.badCall(name);

	return(obj != null ? obj : YoixObject.newNull());
    }


    private GeneralPath
    closeSubPaths(GeneralPath source) {

	PathIterator  pi;
	GeneralPath   dest;
	float         coords[];

	dest = new GeneralPath(source.getWindingRule());
	coords = new float[6];

	for (pi = source.getPathIterator(null); pi.isDone() == false; pi.next()) {
	    switch (pi.currentSegment(coords)) {
		case YOIX_SEG_MOVETO:
		    if (dest.getCurrentPoint() != null)
			dest.closePath();
		    dest.moveTo(coords[0], coords[1]);
		    break;

		case YOIX_SEG_LINETO:
		    dest.lineTo(coords[0], coords[1]);
		    break;

		case YOIX_SEG_CUBICTO:
		    dest.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
		    break;

		case YOIX_SEG_QUADTO:
		    dest.quadTo(coords[0], coords[1], coords[2], coords[3]);
		    break;

		case YOIX_SEG_CLOSE:
		    dest.closePath();
		    break;
	    }
	}
	return(dest);

    }


    private final Area
    getAlignedRectangleArea(double x, double y, double width, double height) {

	return(new Area(getAlignedRectanglePath(x, y, width, height)));
    }


    private final GeneralPath
    getAlignedRectanglePath(double x, double y, double width, double height) {

	GeneralPath  rectpath;
	double       corner1[];
	double       corner2[];
	double       corner3[];
	double       corner4[];

	corner1 = currentmatrix.transform(x, y);
	corner2 = currentmatrix.transform(x + width, y);
	corner3 = currentmatrix.transform(x + width, y + height);
	corner4 = currentmatrix.transform(x, y + height);

	corner1[0] = Math.floor(corner1[0]);
	corner1[1] = Math.floor(corner1[1]);
	corner2[0] = Math.floor(corner2[0]);
	corner2[1] = Math.floor(corner2[1]);
	corner3[0] = Math.floor(corner3[0]);
	corner3[1] = Math.floor(corner3[1]);
	corner4[0] = Math.floor(corner4[0]);
	corner4[1] = Math.floor(corner4[1]);

	rectpath = new GeneralPath();
	rectpath.moveTo((float)corner1[0], (float)corner1[1]);
	rectpath.lineTo((float)corner2[0], (float)corner2[1]);
	rectpath.lineTo((float)corner3[0], (float)corner3[1]);
	rectpath.lineTo((float)corner4[0], (float)corner4[1]);
	rectpath.closePath();

	return(rectpath);
    }


    private YoixObject
    getCTM() {

	return(currentmatrix.getContext());
    }


    private synchronized YoixObject
    getElements() {

	PathIterator  pi;
	Vector        buffer;
	double        coords[];

	buffer = new Vector();
	coords = new double[6];

	for (pi = path.getPathIterator(null); pi.isDone() == false; pi.next()) {
	    switch (pi.currentSegment(coords)) {
		case YOIX_SEG_MOVETO:
		    buffer.addElement(INTEGER_SEG_MOVETO);
		    buffer.addElement(new Double(coords[0]));
		    buffer.addElement(new Double(coords[1]));
		    break;

		case YOIX_SEG_LINETO:
		    buffer.addElement(INTEGER_SEG_LINETO);
		    buffer.addElement(new Double(coords[0]));
		    buffer.addElement(new Double(coords[1]));
		    break;

		case YOIX_SEG_CUBICTO:
		    buffer.addElement(INTEGER_SEG_CUBICTO);
		    buffer.addElement(new Double(coords[0]));
		    buffer.addElement(new Double(coords[1]));
		    buffer.addElement(new Double(coords[2]));
		    buffer.addElement(new Double(coords[3]));
		    buffer.addElement(new Double(coords[4]));
		    buffer.addElement(new Double(coords[5]));
		    break;

		case YOIX_SEG_QUADTO:
		    buffer.addElement(INTEGER_SEG_QUADTO);
		    buffer.addElement(new Double(coords[0]));
		    buffer.addElement(new Double(coords[1]));
		    buffer.addElement(new Double(coords[2]));
		    buffer.addElement(new Double(coords[3]));
		    break;

		case YOIX_SEG_CLOSE:
		    buffer.addElement(INTEGER_SEG_CLOSE);
		    break;
	    }
	}
	return(YoixMisc.copyIntoArray(buffer));
    }


    private GeneralPath
    getFlattenedPath(double flatness) {

	PathIterator  pi;
	GeneralPath   flat;
	float         coords[];

	flatness = Math.max(0.2, Math.min(flatness, 100));
	flat = new GeneralPath();
	coords = new float[6];		// 2 should be sufficient

	for (pi = path.getPathIterator(null, flatness); pi.isDone() == false; pi.next()) {
	    switch (pi.currentSegment(coords)) {
		case YOIX_SEG_MOVETO:
		    flat.moveTo(coords[0], coords[1]);
		    break;

		case YOIX_SEG_LINETO:
		    flat.lineTo(coords[0], coords[1]);
		    break;

		case YOIX_SEG_CUBICTO:
		case YOIX_SEG_QUADTO:
		    VM.die(INTERNALERROR);
		    break;

		case YOIX_SEG_CLOSE:
		    flat.closePath();
		    break;
	    }
	}

	return(flat);
    }


    private void
    repaint() {

	YoixObject  funct;

	if (initialized) {
	    if ((funct = paint) != null && funct.notNull()) {
		pathReset();
		if (funct.callable(1))
		    call(funct, new YoixObject[] {YoixObject.newNull()});
		else call(funct, new YoixObject[0]);
	    }
	}
    }


    private synchronized void
    setCTM(YoixObject obj) {

	if (currentmatrix != null) {
	    if (obj.isNull())
		obj = VM.getDefaultMatrix();
	    currentmatrix.setMatrix((YoixBodyMatrix)obj.body());
	}
    }


    private synchronized void
    setPaint(YoixObject obj) {

	if (obj.isNull() || obj.callable(1) || obj.callable(0))
	    paint = obj.notNull() ? obj : null;
	else VM.abort(TYPECHECK, N_PAINT);
    }
}

