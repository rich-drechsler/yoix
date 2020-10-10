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

package att.research.yoix.j3d;
import java.util.HashMap;
import javax.media.j3d.*;
import javax.vecmath.*;
import att.research.yoix.*;

public
class BodyTransform3D extends J3DPointerActive

    implements Constants

{

    private Transform3D  transform = null;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	NL_TAG,             $LR__,       $LR__,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(50);

    static {
	activefields.put(NL_AUTONORMALIZE, new Integer(VL_AUTONORMALIZE));
	activefields.put(NL_DETERMINANT, new Integer(VL_DETERMINANT));
	activefields.put(NL_DTRANSFORM, new Integer(VL_DTRANSFORM));
	activefields.put(NL_EQUALS, new Integer(VL_EQUALS));
	activefields.put(NL_IDTRANSFORM, new Integer(VL_IDTRANSFORM));
	activefields.put(NL_INVERT, new Integer(VL_INVERT));
	activefields.put(NL_ITRANSFORM, new Integer(VL_ITRANSFORM));
	activefields.put(NL_MUL, new Integer(VL_MUL));
	activefields.put(NL_NORMALIZE, new Integer(VL_NORMALIZE));
	activefields.put(NL_ROTATEX, new Integer(VL_ROTATEX));
	activefields.put(NL_ROTATEY, new Integer(VL_ROTATEY));
	activefields.put(NL_ROTATEZ, new Integer(VL_ROTATEZ));
	activefields.put(NL_SCALE, new Integer(VL_SCALE));
	activefields.put(NL_SETTOEULER, new Integer(VL_SETTOEULER));
	activefields.put(NL_SETTOFRUSTUM, new Integer(VL_SETTOFRUSTUM));
	activefields.put(NL_SETTOIDENTITY, new Integer(VL_SETTOIDENTITY));
	activefields.put(NL_SETTOLOOKAT, new Integer(VL_SETTOLOOKAT));
	activefields.put(NL_SETTOORTHO, new Integer(VL_SETTOORTHO));
	activefields.put(NL_SETTOPERSPECTIVE, new Integer(VL_SETTOPERSPECTIVE));
	activefields.put(NL_SETTOROTATIONX, new Integer(VL_SETTOROTATIONX));
	activefields.put(NL_SETTOROTATIONY, new Integer(VL_SETTOROTATIONY));
	activefields.put(NL_SETTOROTATIONZ, new Integer(VL_SETTOROTATIONZ));
	activefields.put(NL_SETTOSCALE, new Integer(VL_SETTOSCALE));
	activefields.put(NL_SETTOSHEARX, new Integer(VL_SETTOSHEARX));
	activefields.put(NL_SETTOSHEARY, new Integer(VL_SETTOSHEARY));
	activefields.put(NL_SETTOSHEARZ, new Integer(VL_SETTOSHEARZ));
	activefields.put(NL_SETTOTRANSFORM, new Integer(VL_SETTOTRANSFORM));
	activefields.put(NL_SETTOTRANSLATION, new Integer(VL_SETTOTRANSLATION));
	activefields.put(NL_SETTOVIEWAT, new Integer(VL_SETTOVIEWAT));
	activefields.put(NL_SETTOZERO, new Integer(VL_SETTOZERO));
	activefields.put(NL_SHEARX, new Integer(VL_SHEARX));
	activefields.put(NL_SHEARY, new Integer(VL_SHEARY));
	activefields.put(NL_SHEARZ, new Integer(VL_SHEARZ));
	activefields.put(NL_SHXY, new Integer(VL_SHXY));
	activefields.put(NL_SHXZ, new Integer(VL_SHXZ));
	activefields.put(NL_SHYX, new Integer(VL_SHYX));
	activefields.put(NL_SHYZ, new Integer(VL_SHYZ));
	activefields.put(NL_SHZX, new Integer(VL_SHZX));
	activefields.put(NL_SHZY, new Integer(VL_SHZY));
	activefields.put(NL_SX, new Integer(VL_SX));
	activefields.put(NL_SY, new Integer(VL_SY));
	activefields.put(NL_SZ, new Integer(VL_SZ));
	activefields.put(NL_TRANSFORM, new Integer(VL_TRANSFORM));
	activefields.put(NL_TRANSLATE, new Integer(VL_TRANSLATE));
	activefields.put(NL_TRANSPOSE, new Integer(VL_TRANSPOSE));
	activefields.put(NL_TX, new Integer(VL_TX));
	activefields.put(NL_TY, new Integer(VL_TY));
	activefields.put(NL_TYPE, new Integer(VL_TYPE));
	activefields.put(NL_TZ, new Integer(VL_TZ));
    }

    //
    // This is only used when we constuct the YoixObject (probably an
    // array) that represents the type of the transform. We currently
    // assume typeflags[] ordering agrees with Transform3D.getBestType(),
    // which means the best type ends up as the first element in the
    // array built by getType().
    //

    private static Object  typeflags[] = {
	new Integer(Transform3D.ZERO), YoixObject.newString(NL_ZERO),
	new Integer(Transform3D.IDENTITY), YoixObject.newString(NL_IDENTITY),
	new Integer(Transform3D.SCALE), YoixObject.newString(NL_SCALE),
	new Integer(Transform3D.ORTHOGONAL), YoixObject.newString(NL_ORTHOGONAL),
	new Integer(Transform3D.RIGID), YoixObject.newString(NL_RIGID),
	new Integer(Transform3D.CONGRUENT), YoixObject.newString(NL_CONGRUENT),
	new Integer(Transform3D.AFFINE), YoixObject.newString(NL_AFFINE),
    };

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    BodyTransform3D(J3DObject data) {

	super(data);
	buildTransform3D();
	setFixedSize();
	setPermissions(permissions);
	initializer();			// calls NY_INITIALIZER if possible
    }


    BodyTransform3D(Transform3D transform) {

	this((J3DObject)VM.getTypeTemplate(T_TRANSFORM3D), transform);
    }


    BodyTransform3D(J3DObject data, Transform3D transform) {

	super(data);
	buildTransform3D(transform);
	setFixedSize();
	setPermissions(permissions);
	initializer();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(TRANSFORM3D);
    }

    ///////////////////////////////////
    //
    // BodyTransform3D Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case VL_DTRANSFORM:
		obj = builtinDTransform(name, argv);
		break;

	    case VL_EQUALS:
		obj = builtinEquals(name, argv);
		break;

	    case VL_IDTRANSFORM:
		obj = builtinIDTransform(name, argv);
		break;

	    case VL_INVERT:
		obj = builtinInvert(name, argv);
		break;

	    case VL_ITRANSFORM:
		obj = builtinITransform(name, argv);
		break;

	    case VL_MUL:
		obj = builtinMul(name, argv);
		break;

	    case VL_NORMALIZE:
		obj = builtinNormalize(name, argv);
		break;

	    case VL_ROTATEX:
		obj = builtinRotateX(name, argv);
		break;

	    case VL_ROTATEY:
		obj = builtinRotateY(name, argv);
		break;

	    case VL_ROTATEZ:
		obj = builtinRotateZ(name, argv);
		break;

	    case VL_SCALE:
		obj = builtinScale(name, argv);
		break;

	    case VL_SETTOEULER:
		obj = builtinSetToEuler(name, argv);
		break;

	    case VL_SETTOFRUSTUM:
		obj = builtinSetToFrustum(name, argv);
		break;

	    case VL_SETTOIDENTITY:
		obj = builtinSetToIdentity(name, argv);
		break;

	    case VL_SETTOLOOKAT:
		obj = builtinSetToLookAt(name, argv);
		break;

	    case VL_SETTOORTHO:
		obj = builtinSetToOrtho(name, argv);
		break;

	    case VL_SETTOPERSPECTIVE:
		obj = builtinSetToPerspective(name, argv);
		break;

	    case VL_SETTOROTATIONX:
		obj = builtinSetToRotationX(name, argv);
		break;

	    case VL_SETTOROTATIONY:
		obj = builtinSetToRotationY(name, argv);
		break;

	    case VL_SETTOROTATIONZ:
		obj = builtinSetToRotationZ(name, argv);
		break;

	    case VL_SETTOSCALE:
		obj = builtinSetToScale(name, argv);
		break;

	    case VL_SETTOSHEARX:
		obj = builtinSetToShearX(name, argv);
		break;

	    case VL_SETTOSHEARY:
		obj = builtinSetToShearY(name, argv);
		break;

	    case VL_SETTOSHEARZ:
		obj = builtinSetToShearZ(name, argv);
		break;

	    case VL_SETTOTRANSFORM:
		obj = builtinSetToTransform(name, argv);
		break;

	    case VL_SETTOTRANSLATION:
		obj = builtinSetToTranslation(name, argv);
		break;

	    case VL_SETTOVIEWAT:
		obj = builtinSetToViewAt(name, argv);
		break;

	    case VL_SETTOZERO:
		obj = builtinSetToZero(name, argv);
		break;

	    case VL_SHEARX:
		obj = builtinShearX(name, argv);
		break;

	    case VL_SHEARY:
		obj = builtinShearY(name, argv);
		break;

	    case VL_SHEARZ:
		obj = builtinShearZ(name, argv);
		break;

	    case VL_TRANSFORM:
		obj = builtinTransform(name, argv);
		break;

	    case VL_TRANSLATE:
		obj = builtinTranslate(name, argv);
		break;

	    case VL_TRANSPOSE:
		obj = builtinTranspose(name, argv);
		break;

	    default:
		obj = null;
		break;
	}
	return(obj);
    }


    protected void
    finalize() {

	transform = null;
	super.finalize();
    }



    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case VL_AUTONORMALIZE:
		obj = YoixObject.newInt(transform.getAutoNormalize());
		break;

	    case VL_DETERMINANT:
		obj = YoixObject.newDouble(transform.determinant());
		break;

	    case VL_SHXY:	// m10
		obj = getElement(4);
		break;

	    case VL_SHXZ:	// m20
		obj = getElement(8);
		break;

	    case VL_SHYX:	// m01
		obj = getElement(1);
		break;

	    case VL_SHYZ:	// m21
		obj = getElement(9);
		break;

	    case VL_SHZX:	// m02
		obj = getElement(2);
		break;

	    case VL_SHZY:	// m12
		obj = getElement(6);
		break;

	    case VL_SX:		// m00
		obj = getElement(0);
		break;

	    case VL_SY:		// m11
		obj = getElement(5);
		break;

	    case VL_SZ:		// m22
		obj = getElement(10);
		break;

	    case VL_TX:		// m03
		obj = getElement(3);
		break;

	    case VL_TY:		// m13
		obj = getElement(7);
		break;

	    case VL_TYPE:
		obj = getType();
		break;

	    case VL_TZ:		// m23
		obj = getElement(11);
		break;

	    default:
		break;
	}
	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(transform);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case VL_AUTONORMALIZE:
		    transform.setAutoNormalize(obj.booleanValue());
		    break;

		case VL_SHXY:		// m10
		    setElement(obj, 4);
		    break;

		case VL_SHXZ:		// m20
		    setElement(obj, 8);
		    break;

		case VL_SHYX:		// m01
		    setElement(obj, 1);
		    break;

		case VL_SHYZ:		// m21
		    setElement(obj, 9);
		    break;

		case VL_SHZX:		// m02
		    setElement(obj, 2);
		    break;

		case VL_SHZY:		// m12
		    setElement(obj, 6);
		    break;

		case VL_SX:		// m00
		    setElement(obj, 0);
		    break;

		case VL_SY:		// m11
		    setElement(obj, 5);
		    break;

		case VL_SZ:		// m22
		    setElement(obj, 10);
		    break;

		case VL_TX:		// m03
		    setElement(obj, 3);
		    break;

		case VL_TY:		// m13
		    setElement(obj, 7);
		    break;

		case VL_TZ:		// m23
		    setElement(obj, 11);
		    break;
	    }
	}

	return(obj);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildTransform3D() {

	this.transform = new Transform3D(
	    new double[] {
		data.getDouble(NL_SX, 1.0),
		data.getDouble(NL_SHYX, 0.0),
		data.getDouble(NL_SHZX, 0.0),
		data.getDouble(NL_TX, 0.0),
		data.getDouble(NL_SHXY, 0.0),
		data.getDouble(NL_SY, 1.0),
		data.getDouble(NL_SHZY, 0.0),
		data.getDouble(NL_TY, 0.0),
		data.getDouble(NL_SHXZ, 0.0),
		data.getDouble(NL_SHYZ, 0.0),
		data.getDouble(NL_SZ, 1.0),
		data.getDouble(NL_TZ, 0.0),
		0.0,
		0.0,
		0.0,
		1.0
	    }
	);

	setField(NL_AUTONORMALIZE);
    }


    private void
    buildTransform3D(Transform3D transform) {

	this.transform = transform;
    }


    private synchronized YoixObject
    builtinDTransform(String name, YoixObject arg[]) {

	J3DObject  obj = null;
	Vector3d   vector = null;

	if (arg.length == 1 || arg.length == 3) {
	    if (arg.length == 1) {
		if (J3DObject.isVector3D(arg[0])) {
		    vector = new Vector3d(
			arg[0].getDouble(NL_X, 0),
			arg[0].getDouble(NL_Y, 0),
			arg[0].getDouble(NL_Z, 0)
		    );
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    vector = new Vector3d(
				arg[0].doubleValue(),
				arg[1].doubleValue(),
				arg[2].doubleValue()
			    );
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	    if (vector != null) {	// should be unnecessary
		transform.transform(vector);
		obj = J3DObject.newVector3D(vector);
	    }
	} else VM.badCall(name);

	return(obj != null ? obj : J3DObject.newJ3DNull(T_VECTOR3D));
    }


    private synchronized YoixObject
    builtinEquals(String name, YoixObject arg[]) {

	Transform3D  t;
	boolean      result = false;

	if (arg.length == 1 || arg.length == 2) {
	    if (J3DObject.isTransform3D(arg[0])) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    t = ((J3DObject)arg[0]).getManagedTransform();
		    if (arg.length == 2)
			result = transform.epsilonEquals(t, arg[1].doubleValue());
		    else result = transform.equals(t);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    builtinIDTransform(String name, YoixObject arg[]) {

	Transform3D  t;
	J3DObject    obj = null;
	Vector3d     vector = null;

	if (arg.length == 1 || arg.length == 3) {
	    if (arg.length == 1) {
		if (J3DObject.isVector3D(arg[0])) {
		    vector = new Vector3d(
			arg[0].getDouble(NL_X, 0),
			arg[0].getDouble(NL_Y, 0),
			arg[0].getDouble(NL_Z, 0)
		    );
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    vector = new Vector3d(
				arg[0].doubleValue(),
				arg[1].doubleValue(),
				arg[2].doubleValue()
			    );
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	    if (vector != null) {	// should be unnecessary
		try {
		    t = new Transform3D(transform);
		    t.invert();
		    t.transform(vector);
		    obj = J3DObject.newVector3D(vector);
		}
		catch(SingularMatrixException e) {
		    VM.abort(UNDEFINEDRESULT);
		}
	    }
	} else VM.badCall(name);

	return(obj != null ? obj : J3DObject.newJ3DNull(T_VECTOR3D));
    }


    private synchronized YoixObject
    builtinInvert(String name, YoixObject arg[]) {

	if (arg.length == 0) {
	    if (canRead() && canWrite()) {
		try {
		    transform.invert();
		}
		catch(SingularMatrixException e) {
		    VM.abort(UNDEFINEDRESULT);
		}
	    } else VM.abort(INVALIDACCESS);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinITransform(String name, YoixObject arg[]) {

	Transform3D  t;
	J3DObject    obj = null;
	Point3d      point = null;

	if (arg.length == 1 || arg.length == 3) {
	    if (arg.length == 1) {
		if (J3DObject.isPoint3D(arg[0])) {
		    point = new Point3d(
			arg[0].getDouble(NL_X, 0),
			arg[0].getDouble(NL_Y, 0),
			arg[0].getDouble(NL_Z, 0)
		    );
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    point = new Point3d(
				arg[0].doubleValue(),
				arg[1].doubleValue(),
				arg[2].doubleValue()
			    );
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	    if (point != null) {	// should be unnecessary
		try {
		    t = new Transform3D(transform);
		    t.invert();
		    t.transform(point);
		    obj = J3DObject.newPoint3D(point);
		}
		catch(SingularMatrixException e) {
		    VM.abort(UNDEFINEDRESULT);
		}
	    }
	} else VM.badCall(name);

	return(obj != null ? obj : J3DObject.newJ3DNull(T_POINT3D));
    }


    private synchronized YoixObject
    builtinMul(String name, YoixObject arg[]) {

	if (arg.length == 1) {
	    if (J3DObject.isTransform3D(arg[0])) {
		if (arg[0].canRead() && canWrite())
		    transform.mul(((J3DObject)arg[0]).getManagedTransform());
		else VM.abort(INVALIDACCESS);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinNormalize(String name, YoixObject arg[]) {

	if (arg.length == 0) {
	    if (canRead() && canWrite())
		transform.normalize();
	    else VM.abort(INVALIDACCESS);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinRotateX(String name, YoixObject arg[]) {

	Transform3D  t;
	double       angle;
	double       sin;
	double       cos;

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		if (canRead() && canWrite()) {
		    angle = (arg[0].doubleValue() * Math.PI)/180.0;
		    sin = Math.sin(angle);
		    cos = Math.cos(angle);
		    t = new Transform3D(
			new double[] {
			    1.0, 0.0, 0.0, 0.0,
			    0.0, cos, -sin, 0.0,
			    0.0, sin, cos, 0.0,
			    0.0, 0.0, 0.0, 1.0,
			}
		    );
		    transform.mul(t);
		} else VM.abort(INVALIDACCESS);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinRotateY(String name, YoixObject arg[]) {

	Transform3D  t;
	double       angle;
	double       sin;
	double       cos;

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		if (canRead() && canWrite()) {
		    angle = (arg[0].doubleValue() * Math.PI)/180.0;
		    sin = Math.sin(angle);
		    cos = Math.cos(angle);
		    t = new Transform3D(
			new double[] {
			    cos, 0.0, sin, 0.0,
			    0.0, 1.0, 0.0, 0.0,
			    -sin, 0.0, cos, 0.0,
			    0.0, 0.0, 0.0, 1.0,
			}
		    );
		    transform.mul(t);
		} else VM.abort(INVALIDACCESS);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinRotateZ(String name, YoixObject arg[]) {

	Transform3D  t;
	double       angle;
	double       sin;
	double       cos;

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		if (canRead() && canWrite()) {
		    angle = (arg[0].doubleValue() * Math.PI)/180.0;
		    sin = Math.sin(angle);
		    cos = Math.cos(angle);
		    t = new Transform3D(
			new double[] {
			    cos, -sin, 0.0, 0.0,
			    sin, cos, 0.0, 0.0,
			    0.0, 0.0, 1.0, 0.0,
			    0.0, 0.0, 0.0, 1.0,
			}
		    );
		    transform.mul(t);
		} else VM.abort(INVALIDACCESS);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinScale(String name, YoixObject arg[]) {

	Transform3D  t;
	double       sx;
	double       sy;
	double       sz;

	if (arg.length == 3) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			if (canRead() && canWrite()) {
			    sx = arg[0].doubleValue();
			    sy = arg[1].doubleValue();
			    sz = arg[2].doubleValue();
			    t = new Transform3D(
				new double[] {
				    sx, 0.0, 0.0, 0.0,
				    0.0, sy, 0.0, 0.0,
				    0.0, 0.0, sz, 0.0,
				    0.0, 0.0, 0.0, 1.0,
				}
			    );
			    transform.mul(t);
			} else VM.abort(INVALIDACCESS);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToEuler(String name, YoixObject arg[]) {

	if (arg.length == 3) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			if (canRead() && canWrite()) {
			    transform.setEuler(
				new Vector3d(
				    (arg[0].doubleValue() * Math.PI)/180.0,
				    (arg[1].doubleValue() * Math.PI)/180.0,
				    (arg[2].doubleValue() * Math.PI)/180.0
				)
			    );
			} else VM.abort(INVALIDACCESS);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToFrustum(String name, YoixObject arg[]) {

	double  near;
	double  far;

	//
	// Documentation claims limits on near and far, but Java didn't
	// complain when we allowed bad values through.
	//

	if (arg.length == 6) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			if (arg[3].isNumber()) {
			    if (arg[4].isNumber()) {
				if (arg[5].isNumber()) {
				    if ((near = arg[4].doubleValue()) > 0) {
					if ((far = arg[5].doubleValue()) > near) {
					    if (canRead() && canWrite()) {
						transform.frustum(
						    arg[0].doubleValue(),	// left
						    arg[1].doubleValue(),	// right
						    arg[2].doubleValue(),	// bottom
						    arg[3].doubleValue(),	// top
						    near,
						    far
						);
					    } else VM.abort(INVALIDACCESS);
					} else VM.badArgumentValue(name, 5);
				    } else VM.badArgumentValue(name, 4);
				} else VM.badArgument(name, 5);
			    } else VM.badArgument(name, 4);
			} else VM.badArgument(name, 3);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToIdentity(String name, YoixObject arg[]) {

	if (arg.length == 0) {
	    if (canRead() && canWrite())
		transform.setIdentity();
	    else VM.abort(INVALIDACCESS);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToLookAt(String name, YoixObject arg[]) {

	if (arg.length == 3 || arg.length == 9) {
	    if (arg.length == 9) {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    if (arg[3].isNumber()) {
				if (arg[4].isNumber()) {
				    if (arg[5].isNumber()) {
					if (arg[6].isNumber()) {
					    if (arg[7].isNumber()) {
						if (arg[8].isNumber()) {
						    transform.lookAt(
							new Point3d(		// eye
							    arg[0].doubleValue(),
							    arg[1].doubleValue(),
							    arg[2].doubleValue()
							),
							new Point3d(		// origin
							    arg[3].doubleValue(),
							    arg[4].doubleValue(),
							    arg[5].doubleValue()
							),
							new Vector3d(		// up
							    arg[6].doubleValue(),
							    arg[7].doubleValue(),
							    arg[8].doubleValue()
							)
						    );
						} else VM.badArgument(name, 8);
					    } else VM.badArgument(name, 7);
					} else VM.badArgument(name, 6);
				    } else VM.badArgument(name, 5);
				} else VM.badArgument(name, 4);
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else {
		if (J3DObject.isPoint3D(arg[0])) {
		    if (J3DObject.isPoint3D(arg[1])) {
			if (J3DObject.isVector3D(arg[2])) {
			    if (canRead() && canWrite()) {
				transform.lookAt(
				    new Point3d(		// eye
					arg[0].getDouble(NL_X, 0),
					arg[0].getDouble(NL_Y, 0),
					arg[0].getDouble(NL_Z, 0)
				    ),
				    new Point3d(		// origin
					arg[1].getDouble(NL_X, 0),
					arg[1].getDouble(NL_Y, 0),
					arg[1].getDouble(NL_Z, 0)
				    ),
				    new Vector3d(		// up
					arg[2].getDouble(NL_X, 0),
					arg[2].getDouble(NL_Y, 0),
					arg[2].getDouble(NL_Z, 0)
				    )
				);
			    } else VM.abort(INVALIDACCESS);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToOrtho(String name, YoixObject arg[]) {

	if (arg.length == 6) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			if (arg[3].isNumber()) {
			    if (arg[4].isNumber()) {
				if (arg[5].isNumber()) {
				    if (canRead() && canWrite()) {
					transform.ortho(
					    arg[0].doubleValue(),	// left
					    arg[1].doubleValue(),	// right
					    arg[2].doubleValue(),	// bottom
					    arg[3].doubleValue(),	// top
					    arg[4].doubleValue(),	// near
					    arg[5].doubleValue()	// far
					);
				    } else VM.abort(INVALIDACCESS);
				} else VM.badArgument(name, 5);
			    } else VM.badArgument(name, 4);
			} else VM.badArgument(name, 3);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToPerspective(String name, YoixObject arg[]) {

	double  fov;
	double  near;

	//
	// The documentation mentioned the restriction on near, but didn't
	// mention any restrictions on far (unlike frustum documentation).
	//

	if (arg.length == 4) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			if (arg[3].isNumber()) {
			    if ((near = arg[2].doubleValue()) > 0) {
				if (canRead() && canWrite()) {
				    fov = (arg[0].doubleValue() * Math.PI)/180.0;
				    transform.perspective(
					fov,
					arg[1].doubleValue(),	// aspect
					near,
					arg[3].doubleValue()	// far
				    );
				} else VM.abort(INVALIDACCESS);
			    } else VM.badArgumentValue(name, 2);
			} else VM.badArgument(name, 3);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToRotationX(String name, YoixObject arg[]) {

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		if (canRead() && canWrite())
		    transform.rotX((arg[0].doubleValue() * Math.PI)/180.0);
		else VM.abort(INVALIDACCESS);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToRotationY(String name, YoixObject arg[]) {

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		if (canRead() && canWrite())
		    transform.rotY((arg[0].doubleValue() * Math.PI)/180.0);
		else VM.abort(INVALIDACCESS);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToRotationZ(String name, YoixObject arg[]) {

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		if (canRead() && canWrite())
		    transform.rotZ((arg[0].doubleValue() * Math.PI)/180.0);
		else VM.abort(INVALIDACCESS);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToScale(String name, YoixObject arg[]) {

	double  sx;
	double  sy;
	double  sz;

	if (arg.length == 3) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			if (canRead() && canWrite()) {
			    sx = arg[0].doubleValue();
			    sy = arg[1].doubleValue();
			    sz = arg[2].doubleValue();
			    transform.set(
				new double[] {
				    sx, 0.0, 0.0, 0.0,
				    0.0, sy, 0.0, 0.0,
				    0.0, 0.0, sz, 0.0,
				    0.0, 0.0, 0.0, 1.0,
				}
			    );
			} else VM.abort(INVALIDACCESS);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToShearX(String name, YoixObject arg[]) {

	double  hshear;
	double  vshear;

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		if (canRead() && canWrite()) {
		    hshear = arg[0].doubleValue();
		    vshear = arg[1].doubleValue();
		    transform.set(
			new double[] {
			    1.0, 0.0, 0.0, 0.0,
			    0.0, 1.0, hshear, 0.0,
			    0.0, vshear, 1.0, 0.0,
			    0.0, 0.0, 0.0, 1.0,
			}
		    );
		} else VM.abort(INVALIDACCESS);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToShearY(String name, YoixObject arg[]) {

	double  hshear;
	double  vshear;

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		if (canRead() && canWrite()) {
		    hshear = arg[0].doubleValue();
		    vshear = arg[1].doubleValue();
		    transform.set(
			new double[] {
			    1.0, 0.0, vshear, 0.0,
			    0.0, 1.0, 0.0, 0.0,
			    hshear, 0.0, 1.0, 0.0,
			    0.0, 0.0, 0.0, 1.0,
			}
		    );
		} else VM.abort(INVALIDACCESS);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToShearZ(String name, YoixObject arg[]) {

	double  hshear;
	double  vshear;

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		if (canRead() && canWrite()) {
		    hshear = arg[0].doubleValue();
		    vshear = arg[1].doubleValue();
		    transform.set(
			new double[] {
			    1.0, hshear, 0.0, 0.0,
			    vshear, 1.0, 0.0, 0.0,
			    0.0, 0.0, 1.0, 0.0,
			    0.0, 0.0, 0.0, 1.0,
			}
		    );
		} else VM.abort(INVALIDACCESS);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToTransform(String name, YoixObject arg[]) {

	if (arg.length == 1) {
	    if (J3DObject.isTransform3D(arg[0])) {
		if (arg[0].canRead() && canWrite())
		    transform.set(((J3DObject)arg[0]).getManagedTransform());
		else VM.abort(INVALIDACCESS);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToTranslation(String name, YoixObject arg[]) {

	double  tx;
	double  ty;
	double  tz;

	if (arg.length == 3) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			tx = arg[0].doubleValue();
			ty = arg[1].doubleValue();
			tz = arg[2].doubleValue();
			if (canRead() && canWrite()) {
			    transform.set(
				new double[] {
				    1.0, 0.0, 0.0, tx,
				    0.0, 1.0, 0.0, ty,
				    0.0, 0.0, 1.0, tz,
				    0.0, 0.0, 0.0, 1.0,
				}
			    );
			} else VM.abort(INVALIDACCESS);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToViewAt(String name, YoixObject arg[]) {

	builtinSetToLookAt(name, arg);
	builtinInvert(name, new YoixObject[0]);
	return(getContext());
    }


    private synchronized YoixObject
    builtinSetToZero(String name, YoixObject arg[]) {

	if (arg.length == 0) {
	    if (canRead() && canWrite())
		transform.setZero();
	    else VM.abort(INVALIDACCESS);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinShearX(String name, YoixObject arg[]) {

	Transform3D  t;
	double       hshear;
	double       vshear;

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		if (canRead() && canWrite()) {
		    hshear = arg[0].doubleValue();
		    vshear = arg[1].doubleValue();
		    t = new Transform3D(
			new double[] {
			    1.0, 0.0, 0.0, 0.0,
			    0.0, 1.0, hshear, 0.0,
			    0.0, vshear, 1.0, 0.0,
			    0.0, 0.0, 0.0, 1.0,
			}
		    );
		    transform.mul(t);
		} else VM.abort(INVALIDACCESS);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinShearY(String name, YoixObject arg[]) {

	Transform3D  t;
	double       hshear;
	double       vshear;

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		if (canRead() && canWrite()) {
		    hshear = arg[0].doubleValue();
		    vshear = arg[1].doubleValue();
		    t = new Transform3D(
			new double[] {
			    1.0, 0.0, vshear, 0.0,
			    0.0, 1.0, 0.0, 0.0,
			    hshear, 0.0, 1.0, 0.0,
			    0.0, 0.0, 0.0, 1.0,
			}
		    );
		    transform.mul(t);
		} else VM.abort(INVALIDACCESS);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinShearZ(String name, YoixObject arg[]) {

	Transform3D  t;
	double       hshear;
	double       vshear;

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		if (canRead() && canWrite()) {
		    hshear = arg[0].doubleValue();
		    vshear = arg[1].doubleValue();
		    t = new Transform3D(
			new double[] {
			    1.0, hshear, 0.0, 0.0,
			    vshear, 1.0, 0.0, 0.0,
			    0.0, 0.0, 1.0, 0.0,
			    0.0, 0.0, 0.0, 1.0,
			}
		    );
		    transform.mul(t);
		} else VM.abort(INVALIDACCESS);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinTransform(String name, YoixObject arg[]) {

	J3DObject  obj = null;
	Point3d    point = null;

	if (arg.length == 1 || arg.length == 3) {
	    if (arg.length == 1) {
		if (J3DObject.isPoint3D(arg[0])) {
		    point = new Point3d(
			arg[0].getDouble(NL_X, 0),
			arg[0].getDouble(NL_Y, 0),
			arg[0].getDouble(NL_Z, 0)
		    );
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    point = new Point3d(
				arg[0].doubleValue(),
				arg[1].doubleValue(),
				arg[2].doubleValue()
			    );
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	    if (point != null) {	// should be unnecessary
		transform.transform(point);
		obj = J3DObject.newPoint3D(point);
	    }
	} else VM.badCall(name);

	return(obj != null ? obj : J3DObject.newJ3DNull(T_POINT3D));
    }


    private synchronized YoixObject
    builtinTranslate(String name, YoixObject arg[]) {

	Transform3D  t;
	double       tx;
	double       ty;
	double       tz;

	if (arg.length == 3) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			tx = arg[0].doubleValue();
			ty = arg[1].doubleValue();
			tz = arg[2].doubleValue();
			if (canRead() && canWrite()) {
			    t = new Transform3D(
				new double[] {
				    1.0, 0.0, 0.0, tx,
				    0.0, 1.0, 0.0, ty,
				    0.0, 0.0, 1.0, tz,
				    0.0, 0.0, 0.0, 1.0,
				}
			    );
			    transform.mul(t);
			} else VM.abort(INVALIDACCESS);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private synchronized YoixObject
    builtinTranspose(String name, YoixObject arg[]) {

	if (arg.length == 0) {
	    if (canRead() && canWrite())
		transform.transpose();
	    else VM.abort(INVALIDACCESS);
	} else VM.badCall(name);

	return(getContext());
    }


    private YoixObject
    getElement(int index) {

	YoixObject  obj;
	double      elements[];

	elements = new double[16];
	transform.get(elements);
	return(YoixObject.newDouble(elements[index]));
    }


    private YoixObject
    getType() {

	YoixObject  obj;
	int         type;
	int         m;
	int         n;

	type = transform.getType();
	obj = YoixObject.newArray(0, -1);
	for (n = 0, m = 0; n < typeflags.length - 1; n += 2) {
	    if ((type & ((Integer)typeflags[n]).intValue()) != 0)
		obj.put(m++, (YoixObject)typeflags[n+1], false);
	}
	return(obj);
    }


    private void
    setElement(YoixObject obj, int index) {

	double  elements[];
	double  value;

	elements = new double[16];
	transform.get(elements);
	if ((value = obj.doubleValue()) != elements[index]) {
	    elements[index] = value;
	    transform.set(elements);
	}
    }
}

