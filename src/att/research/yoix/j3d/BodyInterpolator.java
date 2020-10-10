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
import javax.media.j3d.Bounds;
import javax.media.j3d.BoundingLeaf;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Transform3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import att.research.yoix.*;

class BodyInterpolator extends J3DPointerActive

    implements Constants

{

    //
    // Java's Interpolator is a SceneGraphObject, so I think this class
    // should extend BodySceneGraphObject. Implementation details are a
    // bit different from our other classes, so this it probably will
    // take a little work to make the change.
    //

    private J3DInterpolator  interpolator = null;
    private J3DObject        j3d_target = null;
    private int              type = 0;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
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

    private static HashMap  activefields = new HashMap(10);

    static {
	activefields.put(NL_ALPHA, new Integer(VL_ALPHA));
	activefields.put(NL_BOUNDS, new Integer(VL_BOUNDS));
	activefields.put(NL_BOUNDINGLEAF, new Integer(VL_BOUNDINGLEAF));
	activefields.put(NL_CONFIGURATION, new Integer(VL_CONFIGURATION));
	activefields.put(NL_INITIALIZE, new Integer(VL_INITIALIZE));
	activefields.put(NL_POSTPROCESSSTIMULUS, new Integer(VL_POSTPROCESSSTIMULUS));
	activefields.put(NL_PREPROCESSSTIMULUS, new Integer(VL_PREPROCESSSTIMULUS));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyInterpolator(J3DObject data) {

	super(data);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(INTERPOLATOR);
    }

    ///////////////////////////////////
    //
    // BodyInterpolator Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj = null;

	switch (activeField(name, activefields)) {
	    default:
		obj = null;
		break;
	}
	return(obj);
    }


    protected void
    finalize() {

	if (interpolator != null)
	    interpolator.cleanup();
	interpolator = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    default:
		break;
	}
	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(interpolator);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case VL_ALPHA:
		    setAlpha(obj);
		    break;

		case VL_BOUNDS:
		    setBounds(obj);
		    break;

		case VL_BOUNDINGLEAF:
		    setBoundingLeaf(obj);
		    break;

		case VL_CONFIGURATION:
		    setConfiguration(obj);
		    break;

		case VL_INITIALIZE:
		    setInitialize(obj);
		    break;

		case VL_POSTPROCESSSTIMULUS:
		    setPostprocessStimulus(obj);
		    break;

		case VL_PREPROCESSSTIMULUS:
		    setPreprocessStimulus(obj);
		    break;

		default:
		    break;
	    }
	}
	return(obj);
    }


    final synchronized void
    setTarget(J3DObject j3d_target) {

	if (this.j3d_target != null) {
	    if (j3d_target == null) {
		if (interpolator != null) {
		    interpolator.cleanup();
		    interpolator = null;
		}
		this.j3d_target = null;
		data.forcePutObject(NL_TARGET, YoixObject.newNull());
	    } else {
		if (this.j3d_target.getManagedObject() != j3d_target.getManagedObject()) {
		    if (interpolator != null) {
			interpolator.cleanup();
			interpolator = null;
		    }
		    this.j3d_target = j3d_target;
		    data.forcePutObject(NL_TARGET, j3d_target);
		    setField(NL_CONFIGURATION);
		}
	    }
	} else if (j3d_target != null) {
	    this.j3d_target = j3d_target;
	    data.forcePutObject(NL_TARGET, j3d_target);
	    setField(NL_CONFIGURATION);
	} else data.forcePutObject(NL_TARGET, YoixObject.newNull());
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    setAlpha(YoixObject obj) {

	if (interpolator != null) {
	    if (obj.isNull())
		interpolator.setAlpha(null);
	    else interpolator.setAlpha(((J3DObject)obj).getManagedAlpha());
	}
    }


    private void
    setBoundingLeaf(YoixObject obj) {

	Bounds  bounds = null;

	if (interpolator != null) {
	    if (obj.isNull() || (bounds = ((J3DObject)obj).getManagedBounds()) == null)
		interpolator.setSchedulingBoundingLeaf(null);
	    else interpolator.setSchedulingBoundingLeaf(new BoundingLeaf(bounds));
	}
    }


    private void
    setBounds(YoixObject obj) {

	Bounds  bounds;

	if (interpolator != null) {
	    if (obj.isNull() || (bounds = ((J3DObject)obj).getManagedBounds()) == null) {
		// will want to change this to some smaller bounds
		bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), Double.POSITIVE_INFINITY);
	    }
	    interpolator.setSchedulingBounds(bounds);
	}
    }


    private void
    setConfiguration(YoixObject obj) {

	J3DInterpolator  interpolator;
	Transform3D      axis;
	YoixObject       configolators;
	YoixObject       yobj;
	YoixObject       yval;
	YoixObject       ynbr;
	J3DObject        configo;
	J3DObject        j3dobj;
	J3DObject        j3d_bounds;
	J3DObject        config;
	J3DAlpha         alpha;
	Bounds           bounds;
	Bounds           leafbounds;
	double           starting_from;
	double           going_to;
	double           knots[];
	Object           jobj;
	Object           values[];
	double           dbl_values[];
	Point3d          p3d_values[];
	Color3f          clr_values[];
	Quat4d           q4d_values[];
	double           dbl;
	int              off;
	int              len;
	int              argc;
	int              pos;
	int              okn;
	int              ovl;
	int              sz;
	int              ac;
	boolean          all_dbl;
	boolean          all_clr;

	interpolator = null;

	if (j3d_target != null && obj != null && obj.notNull()) {

	    interpolator = new J3DInterpolator(j3d_target);

	    if (J3DObject.isConfigolator(obj)) {
		configolators = YoixObject.newArray(1);
		configolators.put(0, obj, false);
	    } else if (obj.isArray()) {
		configolators = obj;
	    } else {
		VM.abort(BADVALUE, NL_CONFIGURATION);
		configolators = null; // for compiler
	    }

	    len = configolators.length();
	    for (off=configolators.offset(), argc=0; off<len; off++, argc++) {
		yobj = configolators.get(off, false);

		if (J3DObject.isConfigolator(yobj)) {
		    configo = (J3DObject)yobj;

		    yobj = configo.get(NL_ALPHA);
		    if (yobj.isNull())
			alpha = null;
		    else alpha = (J3DAlpha)(((J3DObject)yobj).getManagedObject());

		    type = configo.getInt(NL_TYPE, 0);

		    switch(type) {
		    case J3D_CUSTOMTRANSFORMINTERPOLATOR:
			interpolator.setCustomInterpolator(type, alpha, configo.getObject(NL_PROCESSSTIMULUS));
			break;

		    case  J3D_CUSTOMCOLORINTERPOLATOR:
		    case  J3D_CUSTOMSWITCHINTERPOLATOR:
		    case  J3D_CUSTOMTRANSPARENCYINTERPOLATOR:
			VM.abort(BADVALUE, NL_TYPE);
			break;

		    case J3D_POSITIONINTERPOLATOR:
		    case J3D_ROTATIONINTERPOLATOR:
		    case J3D_SCALEINTERPOLATOR:

			yobj = configo.get(NL_AXIS);
			if (yobj.isNull())
			    axis = new Transform3D();
			else if ((axis = ((J3DObject)yobj).getManagedTransform()) == null)
			    VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_AXIS);

			yobj = configo.getObject(NL_KNOTS);
			yval = configo.getObject(NL_VALUES);
			// general checking first
			if (yval.isNull() || !yval.isArray() || yval.sizeof() < 2)
			    VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_VALUES);
			else if (yobj.notNull() && !yobj.isArray())
			    VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_KNOTS);
			else if (yval.sizeof() > 2 && yval.sizeof() != yobj.sizeof())
			    VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_KNOTS + "/" + NL_VALUES);

			all_dbl = true;
			sz = yval.sizeof();
			if (sz > 2) {
			    okn = yobj.offset();
			    knots = new double[sz];
			    for (ac=0; ac<sz; ac++) {
				if (ac == 0)
				    knots[0] = 0;
				else if (ac == (sz-1))
				    knots[sz-1] = 1;
				else {
				    ynbr = yobj.get(okn+ac, false);
				    if (ynbr.isNumber()) {
					dbl = ynbr.doubleValue();
					if (dbl > knots[ac-1] && dbl < 1)
					    knots[ac] = dbl;
					else VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_KNOTS + "[" + ac + "]");
				    } else VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_KNOTS + "[" + ac + "]");
				}
			    }
			} else knots = null;
			ovl = yval.offset();
			switch(type) {
			case J3D_POSITIONINTERPOLATOR:
			    values = new Object[sz];
			    for (ac=0; ac<sz; ac++) {
				ynbr = yval.get(ovl+ac, false);
				if (J3DObject.isPoint3D(ynbr)) {
				    all_dbl = false;
				    values[ac] = Make.javaPoint3d(ynbr);
				} else if (ynbr.isNumber()) {
				    values[ac] = new Double(ynbr.doubleValue());
				} else VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_VALUES + "[" + ac + "]");
			    }
			    if (all_dbl) {
				dbl_values = new double[sz];
				for (ac=0; ac<sz; ac++) {
				    dbl_values[ac] = ((Double)values[ac]).doubleValue();
				}
				jobj = dbl_values;
			    } else {
				p3d_values = new Point3d[sz];
				for (ac=0; ac<sz; ac++) {
				    if (values[ac] instanceof Double)
					p3d_values[ac] = new Point3d(((Double)values[ac]).doubleValue(), 0, 0);
				    else p3d_values[ac] = (Point3d)values[ac];
				}
				jobj = p3d_values;
			    }
			    interpolator.setTransformInterpolator(type, alpha, axis, knots, jobj);
			    break;
			case J3D_ROTATIONINTERPOLATOR:
			    values = new Object[sz];
			    for (ac=0; ac<sz; ac++) {
				ynbr = yval.get(ovl+ac, false);
				if (!ynbr.isNumber()) {
				    all_dbl = false;
				    break;
				}
			    }
			    if (all_dbl) {
				dbl_values = new double[sz];
				for (ac=0; ac<sz; ac++) {
				    ynbr = yval.get(ovl+ac, false);
				    dbl_values[ac] = YoixMisc.toRadians(ynbr.doubleValue());
				}
				jobj = dbl_values;
			    } else {
				q4d_values = new Quat4d[sz];
				for (ac=0; ac<sz; ac++) {
				    ynbr = yval.get(ovl+ac, false);
				    if (J3DObject.isOrientation3D(ynbr)) {
					q4d_values[ac] = Make.javaQuat4dFromOrientation3D(ynbr);
				    } else VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_VALUES + "[" + ac + "]");
				}
				jobj = q4d_values;
			    }
			    interpolator.setTransformInterpolator(type, alpha, axis, knots, jobj);
			    break;
			case J3D_SCALEINTERPOLATOR:
			    dbl_values = new double[sz];
			    for (ac=0; ac<sz; ac++) {
				ynbr = yval.get(ovl+ac, false);
				if (ynbr.isNumber()) {
				    dbl_values[ac] = ynbr.doubleValue();
				} else VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_VALUES + "[" + ac + "]");
			    }
			    interpolator.setTransformInterpolator(type, alpha, axis, knots, dbl_values);
			    break;
			default:
			    VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_TYPE);
			    break;
			}
			break;
		    case  J3D_COLORINTERPOLATOR:
			yobj = configo.getObject(NL_KNOTS);
			yval = configo.getObject(NL_VALUES);
			// general checking first
			if (yval.isNull() || !yval.isArray() || yval.sizeof() < 2)
			    VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_VALUES);
			else if (yobj.notNull() && !yobj.isArray())
			    VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_KNOTS);
			else if (yval.sizeof() > 2 && yval.sizeof() != yobj.sizeof())
			    VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_KNOTS + "/" + NL_VALUES);

			sz = yval.sizeof();
			if (sz > 2) {
			    okn = yobj.offset();
			    knots = new double[sz];
			    for (ac=0; ac<sz; ac++) {
				if (ac == 0)
				    knots[0] = 0;
				else if (ac == (sz-1))
				    knots[sz-1] = 1;
				else {
				    ynbr = yobj.get(okn+ac, false);
				    if (ynbr.isNumber()) {
					dbl = ynbr.doubleValue();
					if (dbl > knots[ac-1] && dbl < 1)
					    knots[ac] = dbl;
					else VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_KNOTS + "[" + ac + "]");
				    } else VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_KNOTS + "[" + ac + "]");
				}
			    }
			} else knots = null;
			ovl = yval.offset();
			values = new Object[sz];
			all_dbl = true;
			all_clr = true;
			for (ac=0; ac<sz; ac++) {
			    ynbr = yval.get(ovl+ac, false);
			    if (!ynbr.isNumber()) {
				all_dbl = false;
				if (!all_clr)
				    break;
			    } else if (ynbr.isNull() || !ynbr.isColor()) {
				all_clr = false;
				if (!all_dbl)
				    break;
			    }
			}
			if (!all_dbl && !all_clr) {
			    VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_VALUES);
			}
			if (all_dbl) {
			    dbl_values = new double[sz];
			    for (ac=0; ac<sz; ac++) {
				ynbr = yval.get(ovl+ac, false);
				dbl_values[ac] = ynbr.doubleValue();
				if (dbl_values[ac] < 0)
				    dbl_values[ac] = 0;
				else if (dbl_values[ac] > 1)
				    dbl_values[ac] = 1;
			    }
			    jobj = dbl_values;
			} else {
			    clr_values = new Color3f[sz];
			    for (ac=0; ac<sz; ac++) {
				ynbr = yval.get(ovl+ac, false);
				if (ynbr.notNull() && ynbr.isColor()) {
				    clr_values[ac] = Make.javaColor3f(ynbr, null);
				} else VM.abort(BADVALUE, NL_CONFIGURATION, argc, NL_VALUES + "[" + ac + "]");
			    }
			    jobj = clr_values;
			}
			interpolator.setColorInterpolator(type, alpha, knots, jobj);
			break;

		    case  J3D_KBSPLINEINTERPOLATOR:
		    case  J3D_TCBSPLINEINTERPOLATOR:
		    case  J3D_SWITCHVALUEINTERPOLATOR:
		    case  J3D_TRANSPARENCYINTERPOLATOR:
		    default:
			VM.abort(BADVALUE, NL_TYPE);
			break;
		    }
		} else VM.abort(BADVALUE, NL_CONFIGURATION, argc);
	    }

	    synchronized(this) {
		if (this.interpolator != null)
		    this.interpolator.cleanup();
		this.interpolator = interpolator;
		setField(NL_BOUNDINGLEAF);
		setField(NL_BOUNDS);
		setField(NL_INITIALIZE);
		setField(NL_PREPROCESSSTIMULUS);
		setField(NL_POSTPROCESSSTIMULUS);
		setField(NL_ALPHA);
		interpolator.addChildToGroup();
	    }
	}
    }


    private void
    setInitialize(YoixObject obj) {

	if (interpolator != null)
	    interpolator.setInitialize(obj);
    }


    private void
    setPostprocessStimulus(YoixObject obj) {

	if (interpolator != null)
	    interpolator.setPostprocessStimulus(obj);
    }


    private void
    setPreprocessStimulus(YoixObject obj) {

	if (interpolator != null)
	    interpolator.setPreprocessStimulus(obj);
    }
}

