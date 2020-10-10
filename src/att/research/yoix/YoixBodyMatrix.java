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

public final
class YoixBodyMatrix extends YoixPointerActive

{

    //
    // For PostScript-style transformation matrix support. Currently
    // not using the Java 2D package, but it should be easy to change.
    // Don't need much synchronization because elements[] is always
    // valid and all changes (after initialization) replace the array,
    // rather than individual elements in that array.
    //

    private boolean  initialized = false;
    private double   elements[];

    //
    // An array used to set permissions on some of the fields that
    // users should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(30);

    static {
	activefields.put(N_CONCAT, new Integer(V_CONCAT));
	activefields.put(N_CONCATMATRIX, new Integer(V_CONCATMATRIX));
	activefields.put(N_CURRENTMATRIX, new Integer(V_CURRENTMATRIX));
	activefields.put(N_DIVIDEMATRIX, new Integer(V_DIVIDEMATRIX));
	activefields.put(N_DTRANSFORM, new Integer(V_DTRANSFORM));
	activefields.put(N_ELEMENTS, new Integer(V_ELEMENTS));
	activefields.put(N_IDENTMATRIX, new Integer(V_IDENTMATRIX));
	activefields.put(N_IDTRANSFORM, new Integer(V_IDTRANSFORM));
	activefields.put(N_INITMATRIX, new Integer(V_INITMATRIX));
	activefields.put(N_INVERTMATRIX, new Integer(V_INVERTMATRIX));
	activefields.put(N_ITRANSFORM, new Integer(V_ITRANSFORM));
	activefields.put(N_MAPTOPIXEL, new Integer(V_MAPTOPIXEL));
	activefields.put(N_ROTATE, new Integer(V_ROTATE));
	activefields.put(N_SCALE, new Integer(V_SCALE));
	activefields.put(N_SETMATRIX, new Integer(V_SETMATRIX));
	activefields.put(N_SHEAR, new Integer(V_SHEAR));
	activefields.put(N_SHX, new Integer(V_SHX));
	activefields.put(N_SHY, new Integer(V_SHY));
	activefields.put(N_SX, new Integer(V_SX));
	activefields.put(N_SY, new Integer(V_SY));
	activefields.put(N_TRANSFORM, new Integer(V_TRANSFORM));
	activefields.put(N_TRANSLATE, new Integer(V_TRANSLATE));
	activefields.put(N_TX, new Integer(V_TX));
	activefields.put(N_TY, new Integer(V_TY));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyMatrix(YoixObject data) {

	super(data, true);
	buildMatrix();
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceCloneable Methods
    //
    ///////////////////////////////////

    public final synchronized Object
    clone() {

	YoixBodyMatrix  obj;

	obj = (YoixBodyMatrix)super.clone();
	if (obj.elements != null) {
	    obj.elements = new double[elements.length];
	    System.arraycopy(elements, 0, obj.elements, 0, elements.length);
	    obj.flags = RWX;		// cheating - sort of
	}

	return(obj);
    }


    public final Object
    copy(HashMap copied) {

	return((YoixBodyMatrix)clone());
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(MATRIX);
    }

    ///////////////////////////////////
    //
    // YoixBodyMatrix Methods
    //
    ///////////////////////////////////

    final void
    concat(YoixBodyMatrix matrix) {

	if (matrix.canRead())
	    concat(matrix.elements);
	else VM.abort(INVALIDACCESS);
    }


    final void
    concat(double m[]) {

	if (canRead() && canWrite())
	    elements = concat(m, elements);
	else VM.abort(INVALIDACCESS);
    }


    final void
    divide(YoixBodyMatrix matrix) {

	if (matrix.canRead())
	    divide(matrix.elements);
	else VM.abort(INVALIDACCESS);
    }


    final void
    divide(double m[]) {

	if (canRead() && canWrite())
	    elements = divide(elements, m);
	else VM.abort(INVALIDACCESS);
    }


    final double[]
    dtransform(double dx, double dy) {

	if (canRead())
	    return(dtransform(dx, dy, elements));
	else VM.abort(INVALIDACCESS);

	return(null);		// not reached
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_CONCAT:
		obj = builtinConcat(name, argv);
		break;

	    case V_CONCATMATRIX:
		obj = builtinConcatMatrix(name, argv);
		break;

	    case V_CURRENTMATRIX:
		obj = builtinCurrentMatrix(name, argv);
		break;

	    case V_DIVIDEMATRIX:
		obj = builtinDivideMatrix(name, argv);
		break;

	    case V_DTRANSFORM:
		obj = builtinDTransform(name, argv);
		break;

	    case V_IDENTMATRIX:
		obj = builtinIdentMatrix(name, argv);
		break;

	    case V_IDTRANSFORM:
		obj = builtinIDTransform(name, argv);
		break;

	    case V_INITMATRIX:
		obj = builtinInitMatrix(name, argv);
		break;

	    case V_INVERTMATRIX:
		obj = builtinInvertMatrix(name, argv);
		break;

	    case V_ITRANSFORM:
		obj = builtinITransform(name, argv);
		break;

	    case V_MAPTOPIXEL:
		obj = builtinMapToPixel(name, argv);
		break;

	    case V_ROTATE:
		obj = builtinRotate(name, argv);
		break;

	    case V_SCALE:
		obj = builtinScale(name, argv);
		break;

	    case V_SETMATRIX:
		obj = builtinSetMatrix(name, argv);
		break;

	    case V_SHEAR:
		obj = builtinShear(name, argv);
		break;

	    case V_TRANSFORM:
		obj = builtinTransform(name, argv);
		break;

	    case V_TRANSLATE:
		obj = builtinTranslate(name, argv);
		break;

	    default:
		obj = null;
		break;
	}

	return(obj);
    }


    protected final void
    finalize() {

	super.finalize();
    }


    final AffineTransform
    getCompatibleAffineTransform(Graphics2D g) {

	AffineTransform  transform;

	transform = getCurrentAffineTransform();
	if (g != null && g.getTransform().isIdentity() == false)
	    transform.preConcatenate(g.getTransform());
	return(transform);
    }


    final AffineTransform
    getCurrentAffineTransform() {

	return(new AffineTransform(
	    elements[0], elements[1],
	    elements[2], elements[3],
	    elements[4], elements[5]
	));
    }


    final double
    getDeterminant() {

	return(elements[0]*elements[3] - elements[1]*elements[2]);
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_SHX:
		obj = YoixObject.newDouble(elements[2]);
		break;

	    case V_SHY:
		obj = YoixObject.newDouble(elements[1]);
		break;

	    case V_SX:
		obj = YoixObject.newDouble(elements[0]);
		break;

	    case V_SY:
		obj = YoixObject.newDouble(elements[3]);
		break;

	    case V_TX:
		obj = YoixObject.newDouble(elements[4]);
		break;

	    case V_TY:
		obj = YoixObject.newDouble(elements[5]);
		break;
	}

	return(obj);
    }


    final double[]
    idtransform(double dx, double dy) {

	if (canRead())
	    return(dtransform(dx, dy, invert(elements)));
	else VM.abort(INVALIDACCESS);

	return(null);		// not reached
    }


    final double[]
    idtransform(double dx, double dy, double fail[]) {

	double  m[] = elements;

	return(canRead() && invertable(m) ? dtransform(dx, dy, invert(m)) : fail);
    }


    final void
    invert() {

	if (canRead() && canWrite())
	    elements = invert(elements);
	else VM.abort(INVALIDACCESS);
    }


    final double[]
    itransform(double coords[]) {

	if (canRead())
	    return(transform(coords, invert(elements)));
	else VM.abort(INVALIDACCESS);

	return(null);		// not reached
    }


    final double[]
    itransform(double x, double y) {

	if (canRead())
	    return(transform(x, y, invert(elements)));
	else VM.abort(INVALIDACCESS);

	return(null);		// not reached
    }


    final double[]
    itransform(double x, double y, double fail[]) {

	double  m[] = elements;

	return(canRead() && invertable(m) ? transform(x, y, invert(m)) : fail);
    }


    final void
    matrixInitialize() {

	elements = new double[] {1.0, 0.0, 0.0, 1.0, 0.0, 0.0};
    }


    final void
    matrixReset() {

	setMatrix((YoixBodyMatrix)VM.getDefaultMatrix().body());
    }


    final void
    matrixRestore(Object details) {

	if (details instanceof double[] && ((double[])details).length == elements.length)
	    elements = (double[])details;
	else VM.die(INTERNALERROR);
    }


    final Object
    matrixSave() {

	return(elements.clone());
    }



    final void
    rotate(double angle) {

	if (canRead() && canWrite())
	    elements = rotate(angle, elements);
	else VM.abort(INVALIDACCESS);
    }


    final void
    scale(double sx, double sy) {

	if (canRead() && canWrite())
	    elements = scale(sx, sy, elements);
	else VM.abort(INVALIDACCESS);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_SHX:
		    setElement(obj, 2);
		    break;

		case V_SHY:
		    setElement(obj, 1);
		    break;

		case V_SX:
		    setElement(obj, 0);
		    break;

		case V_SY:
		    setElement(obj, 3);
		    break;

		case V_TX:
		    setElement(obj, 4);
		    break;

		case V_TY:
		    setElement(obj, 5);
		    break;
	    }
	}

	return(obj);
    }


    final void
    setMatrix(AffineTransform transform) {

	double  temp[];

	if (canRead() && canWrite()) {
	    temp = new double[elements.length];
	    temp[0] = transform.getScaleX();
	    temp[1] = transform.getShearY();
	    temp[2] = transform.getShearX();
	    temp[3] = transform.getScaleY();
	    temp[4] = transform.getTranslateX();
	    temp[5] = transform.getTranslateY();
	    elements = temp;
	} else VM.abort(INVALIDACCESS);
    }


    final void
    setMatrix(YoixBodyMatrix matrix) {

	double  temp[];

	if (canWrite() && matrix.canRead()) {
	    temp = new double[elements.length];
	    System.arraycopy(matrix.elements, 0, temp, 0, temp.length);
	    elements = temp;
	} else VM.abort(INVALIDACCESS);
    }


    final void
    setOwner(YoixBodyGraphics owner) {

	if (owner != null)
	    matrixReset();
    }


    final void
    shear(double shx, double shy) {

	if (canRead() && canWrite())
	    elements = shear(shx, shy, elements);
	else VM.abort(INVALIDACCESS);
    }


    final double[]
    transform(double coords[]) {

	if (canRead())
	    return(transform(coords, elements));
	else VM.abort(INVALIDACCESS);

	return(null);		// not reached
    }


    final double[]
    transform(double x, double y) {

	if (canRead())
	    return(transform(x, y, elements));
	else VM.abort(INVALIDACCESS);

	return(null);		// not reached
    }


    final void
    translate(double tx, double ty) {

	if (canRead() && canWrite())
	    elements = translate(tx, ty, elements);
	else VM.abort(INVALIDACCESS);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildMatrix() {

	elements = new double[] {1.0, 0.0, 0.0, 1.0, 0.0, 0.0};
	setField(N_SHX);
	setField(N_SHY);
	setField(N_SX);
	setField(N_SY);
	setField(N_TX);
	setField(N_TY);
	initialized = true;
    }


    private synchronized YoixObject
    builtinConcat(String name, YoixObject arg[]) {

	if (arg.length == 1) {
	    if (arg[0].isMatrix())
		concat(((YoixBodyMatrix)arg[0].body()).elements);
	    else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinConcatMatrix(String name, YoixObject arg[]) {

	YoixBodyMatrix  matrix = null;

	if (arg.length == 1) {
	    if (arg[0].isMatrix()) {
		matrix = (YoixBodyMatrix)this.clone();
		matrix.concat((YoixBodyMatrix)arg[0].body());
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(matrix.getContext());
    }


    private synchronized YoixObject
    builtinCurrentMatrix(String name, YoixObject arg[]) {

	YoixBodyMatrix  matrix = null;

	if (arg.length == 0)
	    matrix = (YoixBodyMatrix)this.clone();
	else VM.badCall(name);

	return(matrix.getContext());
    }


    private synchronized YoixObject
    builtinDivideMatrix(String name, YoixObject arg[]) {

	YoixBodyMatrix  matrix = null;

	if (arg.length == 1) {
	    if (arg[0].isMatrix()) {
		if (getDeterminant() != 0) {
		    matrix = (YoixBodyMatrix)this.clone();
		    matrix.divide((YoixBodyMatrix)arg[0].body());
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(matrix != null ? matrix.getContext() : YoixObject.newMatrix());
    }


    private synchronized YoixObject
    builtinDTransform(String name, YoixObject arg[]) {

	double  coords[] = null;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].defined(N_WIDTH) && arg[0].defined(N_HEIGHT)) {
		    coords = dtransform(
			arg[0].getDouble(N_WIDTH, 0),
			arg[0].getDouble(N_HEIGHT, 0)
		    );
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber())
			coords = dtransform(arg[0].doubleValue(), arg[1].doubleValue());
		    else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	} else VM.badCall(name);

	return(YoixObject.newDimension(coords));
    }


    private synchronized YoixObject
    builtinIdentMatrix(String name, YoixObject arg[]) {

	if (arg.length == 0)
	    matrixInitialize();
	else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinIDTransform(String name, YoixObject arg[]) {

	double  coords[] = null;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].defined(N_WIDTH) && arg[0].defined(N_HEIGHT)) {
		    if (getDeterminant() != 0) {
			coords = idtransform(
			    arg[0].getDouble(N_WIDTH, 0),
			    arg[0].getDouble(N_HEIGHT, 0)
			);
		    }
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (getDeterminant() != 0) {
			    coords = idtransform(
				arg[0].doubleValue(),
				arg[1].doubleValue()
			    );
			}
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	} else VM.badCall(name);

	return(coords != null ? YoixObject.newDimension(coords) : YoixObject.newDimension());
    }


    private synchronized YoixObject
    builtinInitMatrix(String name, YoixObject arg[]) {

	if (arg.length == 0)
	    matrixReset();
	else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinInvertMatrix(String name, YoixObject arg[]) {

	YoixBodyMatrix  matrix = null;

	if (arg.length == 0) {
	    if (getDeterminant() != 0) {
		matrix = (YoixBodyMatrix)this.clone();
		matrix.invert();
	    }
	} else VM.badCall(name);

	return(matrix != null ? matrix.getContext() : YoixObject.newMatrix());
    }


    private synchronized YoixObject
    builtinITransform(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	double      coords[];
	double      dimensions[];

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].defined(N_X) && arg[0].defined(N_Y)) {
		    if (getDeterminant() != 0) {
			coords = itransform(
			    arg[0].getDouble(N_X, 0),
			    arg[0].getDouble(N_Y, 0)
			);
			if (arg[0].defined(N_WIDTH) && arg[0].defined(N_HEIGHT)) {
			    dimensions = idtransform(
				arg[0].getDouble(N_WIDTH, 0),
				arg[0].getDouble(N_HEIGHT, 0)
			    );
			} else dimensions = null;
			if (coords != null && dimensions != null) {
			    obj = YoixObject.newRectangle(
				coords[0], coords[1],
				dimensions[0], dimensions[1]
			    );
			} else obj = YoixObject.newPoint(coords);
		    }
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (getDeterminant() != 0) {
			    coords = itransform(
				arg[0].doubleValue(),
				arg[1].doubleValue()
			    );
			    obj = YoixObject.newPoint(coords);
			}
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	} else VM.badCall(name);

	return(obj != null ? obj : YoixObject.newPoint());
    }


    private synchronized YoixObject
    builtinMapToPixel(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	double      coords[];
	double      dimensions[];

	//
	// Eventually could take an optional third argument that controls
	// device space pixel selection. For example, UPPERLEFT or TOPLEFT
	// might be what we're doing here - later!!!
	//

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].defined(N_X) && arg[0].defined(N_Y)) {
		    if (getDeterminant() != 0) {
			coords = transform(
			    arg[0].getDouble(N_X, 0),
			    arg[0].getDouble(N_Y, 0)
			);
			coords[0] = Math.floor(coords[0]);
			coords[1] = Math.floor(coords[1]);
			coords = itransform(coords[0], coords[1]);

			//
			// Haven't put any real thought into this part
			// yet, so it probably shouldn't be documented
			// until it's revisited.
			//
			if (arg[0].defined(N_WIDTH) && arg[0].defined(N_HEIGHT)) {
			    dimensions = dtransform(
				arg[0].getDouble(N_WIDTH, 0),
				arg[0].getDouble(N_HEIGHT, 0)
			    );
			    dimensions[0] = Math.ceil(dimensions[0]);
			    dimensions[1] = Math.ceil(dimensions[1]);
			    dimensions = idtransform(
				arg[0].getDouble(N_WIDTH, 0),
				arg[0].getDouble(N_HEIGHT, 0)
			    );
			} else dimensions = null;
			if (coords != null && dimensions != null) {
			    obj = YoixObject.newRectangle(
				coords[0], coords[1],
				dimensions[0], dimensions[1]
			    );
			} else obj = YoixObject.newPoint(coords);
		    }
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (getDeterminant() != 0) {
			    coords = transform(
				arg[0].doubleValue(),
				arg[1].doubleValue()
			    );
			    coords[0] = Math.floor(coords[0]);
			    coords[1] = Math.floor(coords[1]);
			    coords = itransform(coords[0], coords[1]);
			    obj = YoixObject.newPoint(coords);
			}
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	} else VM.badCall(name);

	return(obj != null ? obj : YoixObject.newPoint());
    }


    private synchronized YoixObject
    builtinRotate(String name, YoixObject arg[]) {

	if (arg.length == 1) {
	    if (arg[0].isNumber())
		rotate(arg[0].doubleValue());
	    else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinScale(String name, YoixObject arg[]) {

	double  sx;
	double  sy;

	if (arg.length == 2) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if ((sx = arg[0].doubleValue()) != 0) {
			if ((sy = arg[1].doubleValue()) != 0)
			    scale(sx, sy);
			else VM.badArgumentValue(name, 1);
		    } else VM.badArgumentValue(name, 0);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetMatrix(String name, YoixObject arg[]) {

	if (arg.length == 1) {
	    if (arg[0].isMatrix())
		setMatrix((YoixBodyMatrix)arg[0].body());
	    else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinShear(String name, YoixObject arg[]) {

	if (arg.length == 2) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber())
		    shear(arg[0].doubleValue(), arg[1].doubleValue());
		else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinTransform(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	double      coords[];
	double      dimensions[];

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].defined(N_X) && arg[0].defined(N_Y)) {
		    coords = transform(
			arg[0].getDouble(N_X, 0),
			arg[0].getDouble(N_Y, 0)
		    );
		    if (arg[0].defined(N_WIDTH) && arg[0].defined(N_HEIGHT)) {
			dimensions = dtransform(
			    arg[0].getDouble(N_WIDTH, 0),
			    arg[0].getDouble(N_HEIGHT, 0)
			);
		    } else dimensions = null;
		    if (coords != null && dimensions != null) {
			obj = YoixObject.newRectangle(
			    coords[0], coords[1],
			    dimensions[0], dimensions[1]
			);
		    } else obj = YoixObject.newPoint(coords);
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			coords = transform(arg[0].doubleValue(), arg[1].doubleValue());
			obj = YoixObject.newPoint(coords);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	} else VM.badCall(name);

	return(obj != null ? obj : YoixObject.newPoint());
    }


    private synchronized YoixObject
    builtinTranslate(String name, YoixObject arg[]) {

	if (arg.length == 2) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber())
		    translate(arg[0].doubleValue(), arg[1].doubleValue());
		else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private static double[]
    concat(double m1[], double m2[]) {

	if (m1.length == 6 && m2.length == 6) {
	    return(new double[] {
		m1[0]*m2[0] + m1[1]*m2[2],
		m1[0]*m2[1] + m1[1]*m2[3],
		m1[2]*m2[0] + m1[3]*m2[2],
		m1[2]*m2[1] + m1[3]*m2[3],
		m1[4]*m2[0] + m1[5]*m2[2] + m2[4],
		m1[4]*m2[1] + m1[5]*m2[3] + m2[5]
	    });
	} else VM.abort(BADMATRIX);

	return(null);		// not reached
    }


    private static double[]
    divide(double m1[], double m2[]) {

	int  n;

	if (m1.length == 6 && m2.length == 6) {
	    for (n = 0; n < 6; n++) {
		if (m1[n] != m2[n])
		    return(concat(m1, invert(m2)));
	    }
	    return(new double[] {1.0, 0.0, 0.0, 1.0, 0.0, 0.0});
	} else VM.abort(BADMATRIX);

	return(null);		// not reached
    }


    private static double[]
    dtransform(double dx, double dy, double m[]) {

	return(new double[] {m[0]*dx + m[2]*dy, m[1]*dx + m[3]*dy});
    }


    private static double[]
    invert(double m[]) {

	double  det;

	if (m.length == 6) {
	    if ((det = m[0]*m[3] - m[1]*m[2]) != 0) {
		return(new double[] {
		    m[3]/det,
		    -m[1]/det,
		    -m[2]/det,
		    m[0]/det,
		    (m[2]*m[5] - m[3]*m[4])/det,
		    (m[1]*m[4] - m[0]*m[5])/det
		});
	    } else VM.abort(UNDEFINEDRESULT);
	} else VM.abort(BADMATRIX);

	return(null);		// not reached
    }


    private static boolean
    invertable(double m[]) {

	return((m[0]*m[3] - m[1]*m[2]) != 0);
    }


    private static double[]
    rotate(double angle, double m[]) {

	double  sin;
	double  cos;

	angle = (angle * Math.PI)/180.0;	// degrees to radians??
	sin = Math.sin(angle);
	cos = Math.cos(angle);

	return(concat(new double[] {cos, sin, -sin, cos, 0, 0}, m));
    }


    private static double[]
    scale(double sx, double sy, double m[]) {

	return(concat(new double[] {sx, 0, 0, sy, 0, 0}, m));
    }


    private void
    setElement(YoixObject obj, int index) {

	double  temp[];
	double  value;

	if ((value = obj.doubleValue()) != elements[index]) {
	    if (initialized) {
		temp = new double[elements.length];
		System.arraycopy(elements, 0, temp, 0, temp.length);
		temp[index] = value;
		elements = temp;
	    } else elements[index] = value;
	}
    }


    private static double[]
    shear(double shx, double shy, double m[]) {

	return(concat(new double[] {1, shy, shx, 1, 0, 0}, m));
    }


    private static double[]
    transform(double x, double y, double m[]) {

	return(new double[] {m[0]*x + m[2]*y + m[4], m[1]*x + m[3]*y + m[5]});
    }


    private static double[]
    transform(double coords[], double m[]) {

	double  pt[];
	int  n;

	for (n = 0; n < coords.length - 1; n += 2) {
	    pt = transform(coords[n], coords[n+1], m);
	    coords[n] = pt[0];
	    coords[n+1] = pt[1];
	}

	return(coords);
    }


    private static double[]
    translate(double tx, double ty, double m[]) {

	return(concat(new double[] {1.0, 0, 0, 1.0, tx, ty}, m));
    }
}

