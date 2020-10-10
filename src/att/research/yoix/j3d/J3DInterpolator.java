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
import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.behaviors.interpolators.*;
import att.research.yoix.*;

public
class J3DInterpolator extends Interpolator

    implements Constants

{

    //
    // This class is a front for the 16 or so interpolators Java3D provides
    // (and for any others we choose to provide at some later date)
    //


    YoixObject  initialize = null;
    YoixObject  preprocessstimulus = null;
    YoixObject  postprocessstimulus = null;
    J3DObject   j3d_target;

    private InterpolatorSpecs  specs[];
    private WakeupCondition    waker;
    private J3DAlpha           alpha;
    private int                type = 0;

    private static int  specorder[];

    static {
	specorder = new int[INTERPOLATOR_ARRAY_SIZE];
	specorder[0]  = J3D_POSITIONINTERPOLATOR;
	specorder[1]  = J3D_ROTATIONINTERPOLATOR;
	specorder[2]  = J3D_SCALEINTERPOLATOR;
	specorder[3]  = J3D_KBSPLINEINTERPOLATOR;
	specorder[4]  = J3D_TCBSPLINEINTERPOLATOR;
	specorder[5]  = J3D_COLORINTERPOLATOR;
	specorder[6]  = J3D_SWITCHVALUEINTERPOLATOR;
	specorder[7]  = J3D_TRANSPARENCYINTERPOLATOR;
	specorder[8]  = J3D_CUSTOMCOLORINTERPOLATOR;
	specorder[9]  = J3D_CUSTOMSWITCHINTERPOLATOR;
	specorder[10] = J3D_CUSTOMTRANSFORMINTERPOLATOR;
	specorder[11] = J3D_CUSTOMTRANSPARENCYINTERPOLATOR;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    J3DInterpolator(J3DObject j3d_target) {
	super();

	waker = new WakeupOnElapsedFrames(0); // for now

	this.j3d_target = j3d_target;

	alpha = null;
	specs = new InterpolatorSpecs[INTERPOLATOR_ARRAY_SIZE + 1];
    }

    ///////////////////////////////////
    //
    // Interpolator Methods
    //
    ///////////////////////////////////

    public final void
    initialize() {

	if (initialize != null)
	    ((J3DPointerActive)(j3d_target.body())).call(initialize, new YoixObject[0], ((J3DPointerActive)(j3d_target.body())).getContext());
	wakeupOn(waker);
    }


    public final void
    processStimulus(Enumeration criteria) {

	J3DAlpha        alpha;
	Material        mt_target;
	TransformGroup  tg_target;
	Transform3D     transform;
	int             type_snap = type;
	int             mm, nn, ii;

	double       value;
	double[]     dbl_values;
	double       dbl;
	Point3d[]    p3d_values;
	Point3d      p3d;
	Quat4d[]     q4d_values;
	Quat4d       q4d;
	Color3f[]    clr_values;
	Color3f      clr;
	Color3f      matclr;
	double       shiny;
	double       interp;
	double       sin;
	double       cos;
	Transform3D  t3d;

	InterpolatorSpecs spec;

	transform = null;
	matclr = null;
	shiny = -1;

	if (preprocessstimulus  != null) {
	    ((J3DPointerActive)(j3d_target.body())).call(preprocessstimulus, new YoixObject[0], ((J3DPointerActive)(j3d_target.body())).getContext());
	}

	for (nn=0; nn<specorder.length; nn++) {
	    mm = specorder[nn];
	    spec = specs[mm];
	    if (spec != null) {
		switch(spec.type) {
		case  J3D_CUSTOMCOLORINTERPOLATOR:
		case  J3D_CUSTOMTRANSFORMINTERPOLATOR:
		    if (spec.processstimulus  != null) {
			((J3DPointerActive)(j3d_target.body())).call(spec.processstimulus, new YoixObject[0], ((J3DPointerActive)(j3d_target.body())).getContext());
		    }
		    break;
		case  J3D_POSITIONINTERPOLATOR:
		    if (spec.alpha == null)
			alpha = (J3DAlpha)this.getAlpha();
		    else alpha = spec.alpha;
		    if (transform == null)
			transform = new Transform3D();
		    value = alpha.value();
		    if (spec.knots == null) {
			if (spec.values instanceof double[]) {
			    dbl_values = (double[])spec.values;
			    interp = dbl_values[0] + (dbl_values[1] - dbl_values[0]) * value;
			    t3d = new Transform3D(
				new double[] {
				    1.0, 0.0, 0.0, interp,
				    0.0, 1.0, 0.0, 0.0,
				    0.0, 0.0, 1.0, 0.0,
				    0.0, 0.0, 0.0, 1.0,
				}
				);
			} else {
			    p3d = new Point3d();
			    p3d_values = (Point3d[])spec.values;
			    p3d.x = p3d_values[0].x + (p3d_values[1].x - p3d_values[0].x) * value;
			    p3d.y = p3d_values[0].y + (p3d_values[1].y - p3d_values[0].y) * value;
			    p3d.z = p3d_values[0].z + (p3d_values[1].z - p3d_values[0].z) * value;
			    t3d = new Transform3D(
				new double[] {
				    1.0, 0.0, 0.0, p3d.x,
				    0.0, 1.0, 0.0, p3d.y,
				    0.0, 0.0, 1.0, p3d.z,
				    0.0, 0.0, 0.0, 1.0,
				}
				);
			}
			transform.mul(t3d);
		    } else {
			if (spec.values instanceof double[]) {
			    dbl_values = (double[])spec.values;
			    for (ii=1; ii < spec.knots.length; ii++) {
				if (value <= spec.knots[ii]) {
				    if (value == spec.knots[ii]) {
					interp = dbl_values[ii];
				    } else {
					value =  (value - spec.knots[ii-1])/(spec.knots[ii]-spec.knots[ii-1]);
					interp = dbl_values[ii-1] + (dbl_values[ii] - dbl_values[ii-1]) * value;
				    }
				    t3d = new Transform3D(
					new double[] {
					    1.0, 0.0, 0.0, interp,
					    0.0, 1.0, 0.0, 0.0,
					    0.0, 0.0, 1.0, 0.0,
					    0.0, 0.0, 0.0, 1.0,
					}
					);
				    transform.mul(t3d);
				    break;
				}
			    }
			} else {
			    p3d = new Point3d();
			    p3d_values = (Point3d[])spec.values;
			    for (ii=1; ii < spec.knots.length; ii++) {
				if (value <= spec.knots[ii]) {
				    if (value == spec.knots[ii]) {
					p3d.x = p3d_values[ii].x;
					p3d.y = p3d_values[ii].y;
					p3d.z = p3d_values[ii].z;
				    } else {
					value =  (value - spec.knots[ii-1])/(spec.knots[ii]-spec.knots[ii-1]);
					p3d.x = p3d_values[ii-1].x + (p3d_values[ii].x - p3d_values[ii-1].x) * value;
					p3d.y = p3d_values[ii-1].y + (p3d_values[ii].y - p3d_values[ii-1].y) * value;
					p3d.z = p3d_values[ii-1].z + (p3d_values[ii].z - p3d_values[ii-1].z) * value;
				    }
				    t3d = new Transform3D(
					new double[] {
					    1.0, 0.0, 0.0, p3d.x,
					    0.0, 1.0, 0.0, p3d.y,
					    0.0, 0.0, 1.0, p3d.z,
					    0.0, 0.0, 0.0, 1.0,
					}
					);
				    transform.mul(t3d);
				    break;
				}
			    }
			}
		    }
		    break;
		case  J3D_ROTATIONINTERPOLATOR:
		    if (spec.alpha == null)
			alpha = (J3DAlpha)this.getAlpha();
		    else alpha = spec.alpha;
		    if (transform == null)
			transform = new Transform3D();
		    value = alpha.value();
		    if (spec.knots == null) {
			if (spec.values instanceof double[]) {
			    dbl_values = (double[])spec.values;
			    interp = dbl_values[0] + (dbl_values[1] - dbl_values[0]) * value;
			    sin = Math.sin(interp);
			    cos = Math.cos(interp);
			    t3d = new Transform3D(
				new double[] {
				    cos,  0.0, sin, 0.0,
				    0.0,  1.0, 0.0, 0.0,
				    -sin, 0.0, cos, 0.0,
				    0.0,  0.0, 0.0, 1.0,
				}
				);
			} else {
			    q4d_values = (Quat4d[])spec.values;
			    q4d = new Quat4d();
			    q4d.interpolate(q4d_values[0], q4d_values[1], value);
			    t3d = new Transform3D();
			    t3d.set(q4d);
			}
			transform.mul(t3d);
		    } else {
			if (spec.values instanceof double[]) {
			    dbl_values = (double[])spec.values;
			    for (ii=1; ii < spec.knots.length; ii++) {
				if (value <= spec.knots[ii]) {
				    if (value == spec.knots[ii]) {
					interp = dbl_values[ii];
				    } else {
					value =  (value - spec.knots[ii-1])/(spec.knots[ii]-spec.knots[ii-1]);
					interp = dbl_values[ii-1] + (dbl_values[ii] - dbl_values[ii-1]) * value;
				    }
				    sin = Math.sin(interp);
				    cos = Math.cos(interp);
				    t3d = new Transform3D(
					new double[] {
					    cos,  0.0, sin, 0.0,
					    0.0,  1.0, 0.0, 0.0,
					    -sin, 0.0, cos, 0.0,
					    0.0,  0.0, 0.0, 1.0,
					}
					);
				    transform.mul(t3d);
				    break;
				}
			    }
			} else {
			    q4d = new Quat4d();
			    q4d_values = (Quat4d[])spec.values;
			    for (ii=1; ii < spec.knots.length; ii++) {
				if (value <= spec.knots[ii]) {
				    if (value == spec.knots[ii]) {
					q4d.x = q4d_values[ii].x;
					q4d.y = q4d_values[ii].y;
					q4d.z = q4d_values[ii].z;
					q4d.w = q4d_values[ii].w;
				    } else {
					value =  (value - spec.knots[ii-1])/(spec.knots[ii]-spec.knots[ii-1]);
					q4d_values = (Quat4d[])spec.values;
					q4d.interpolate(q4d_values[ii-1], q4d_values[ii], value);
				    }
				    t3d = new Transform3D();
				    t3d.set(q4d);
				    transform.mul(t3d);
				    break;
				}
			    }
			}
		    }
		    break;
		case  J3D_SCALEINTERPOLATOR:
		    if (spec.alpha == null)
			alpha = (J3DAlpha)this.getAlpha();
		    else alpha = spec.alpha;
		    if (transform == null)
			transform = new Transform3D();
		    value = alpha.value();
		    dbl_values = (double[])spec.values;
		    if (spec.knots == null) {
			interp = dbl_values[0] + (dbl_values[1] - dbl_values[0]) * value;
			t3d = new Transform3D(
			    new double[] {
				interp, 0.0,    0.0,    0.0,
				0.0,    interp, 0.0,    0.0,
				0.0,    0.0,    interp, 0.0,
				0.0,    0.0,    0.0,    1.0,
			    }
			    );
			transform.mul(t3d);
		    } else {
			dbl_values = (double[])spec.values;
			for (ii=1; ii < spec.knots.length; ii++) {
			    if (value <= spec.knots[ii]) {
				if (value == spec.knots[ii]) {
				    interp = dbl_values[ii];
				} else {
				    value =  (value - spec.knots[ii-1])/(spec.knots[ii]-spec.knots[ii-1]);
				    interp = dbl_values[ii-1] + (dbl_values[ii] - dbl_values[ii-1]) * value;
				}
				t3d = new Transform3D(
				    new double[] {
					interp, 0.0,    0.0,    0.0,
					0.0,    interp, 0.0,    0.0,
					0.0,    0.0,    interp, 0.0,
					0.0,    0.0,    0.0,    1.0,
				    }
				    );
				transform.mul(t3d);
				break;
			    }
			}
		    }
		    break;
		case  J3D_COLORINTERPOLATOR:
		    if (spec.alpha == null)
			alpha = (J3DAlpha)this.getAlpha();
		    else alpha = spec.alpha;
		    value = alpha.value();
		    if (spec.values instanceof double[]) {
			dbl_values = (double[])spec.values;
			if (spec.knots == null) {
			    shiny = dbl_values[0] + (dbl_values[1] - dbl_values[0]) * value;
			} else {
			    for (ii=1; ii < spec.knots.length; ii++) {
				if (value <= spec.knots[ii]) {
				    if (value == spec.knots[ii]) {
					interp = dbl_values[ii];
				    } else {
					value =  (value - spec.knots[ii-1])/(spec.knots[ii]-spec.knots[ii-1]);
					shiny = dbl_values[ii-1] + (dbl_values[ii] - dbl_values[ii-1]) * value;
				    }
				    break;
				}
			    }
			}
		    } else {
			clr_values = (Color3f[])spec.values;
			if (spec.knots == null) {
			    matclr = new Color3f();
			    matclr.interpolate(clr_values[0], clr_values[1], (float)value);
			} else {
			    for (ii=1; ii < spec.knots.length; ii++) {
				if (value <= spec.knots[ii]) {
				    if (value == spec.knots[ii]) {
					matclr = clr_values[ii];
				    } else {
					value =  (value - spec.knots[ii-1])/(spec.knots[ii]-spec.knots[ii-1]);
					matclr = new Color3f();
					matclr.interpolate(clr_values[ii-1], clr_values[ii], (float)value);
				    }
				    break;
				}
			    }
			}
		    }
		    break;
		case  J3D_CUSTOMSWITCHINTERPOLATOR:
		case  J3D_CUSTOMTRANSPARENCYINTERPOLATOR:
		case  J3D_KBSPLINEINTERPOLATOR:
		case  J3D_TCBSPLINEINTERPOLATOR:
		case  J3D_SWITCHVALUEINTERPOLATOR:
		case  J3D_TRANSPARENCYINTERPOLATOR:
		default:
		    VM.abort(INTERNALERROR);
		    break;
		}
	    }
	}
	if (transform != null) {
	    tg_target = j3d_target.getTargetTransform();
	    if (tg_target != null)
		tg_target.setTransform(transform);
	}
	if (shiny >= 0) {
	    mt_target = j3d_target.getTargetMaterial();
	    if (mt_target != null)
		mt_target.setShininess((float)(1.0 + 127.0 * shiny));
	} else if (matclr != null) {
	    mt_target = j3d_target.getTargetMaterial();
	    if (mt_target != null) {
		switch(mt_target.getColorTarget()) {
		case Material.AMBIENT:
		    mt_target.setAmbientColor(matclr);
		    break;
		case Material.DIFFUSE:
		    mt_target.setDiffuseColor(matclr);
		    break;
		case Material.AMBIENT_AND_DIFFUSE:
		    mt_target.setAmbientColor(matclr);
		    mt_target.setDiffuseColor(matclr);
		    break;
		case Material.EMISSIVE:
		    mt_target.setEmissiveColor(matclr);
		    break;
		case Material.SPECULAR:
		    mt_target.setSpecularColor(matclr);
		    break;
		}
	    }
	}

	if (postprocessstimulus  != null) {
	    ((J3DPointerActive)(j3d_target.body())).call(postprocessstimulus, new YoixObject[0], ((J3DPointerActive)(j3d_target.body())).getContext());
	}

	wakeupOn(waker);
    }

    ///////////////////////////////////
    //
    // J3DInterpolator Methods
    //
    ///////////////////////////////////

    final void
    addChildToGroup() {

	Object  obj;

	if (j3d_target != null) {
	    if ((obj = j3d_target.getManagedGroup()) == null)
		if ((obj = j3d_target.getInterpolatorGroup()) == null)
		    VM.abort(BADVALUE, NL_TARGET);
	    ((Group)obj).addChild(this);
	}
    }


    final void
    cleanup() {

	removeChildFromGroup();
	j3d_target = null;
	alpha = null;
	specs = null;
	initialize = null;
	preprocessstimulus = null;
	postprocessstimulus = null;
    }


    protected void
    finalize() {

	cleanup();
    }


    public final Alpha
    getAlpha() {

	if (alpha == null)
	    alpha = new J3DAlpha();
	return(alpha);
    }


    final void
    removeChildFromGroup() {

	Object  obj;

	if (j3d_target != null) {
	    if ((obj = j3d_target.getManagedGroup()) == null)
		if ((obj = j3d_target.getInterpolatorGroup()) == null)
		    VM.abort(BADVALUE, NL_TARGET);
	    ((Group)obj).removeChild(this);
	}
    }


    public final void
    setAlpha(Alpha alpha) {

	this.alpha = (J3DAlpha)alpha;
    }


    final void
    setColorInterpolator(int type, J3DAlpha alpha, double[] knots, Object values) {

	InterpolatorSpecs  spec;

	spec = new InterpolatorSpecs(type, alpha, knots, values);
	// assume type is in range
	specs[type] = spec;
    }


    final void
    setCustomInterpolator(int type, J3DAlpha alpha, YoixObject proc) {

	InterpolatorSpecs  spec;

	if (proc != null) {
	    spec = new InterpolatorSpecs(type, alpha, proc);
	    // assume type is in range
	    specs[type] = spec;
	} else specs[type] = null;
    }


    final void
    setInitialize(YoixObject init) {

	if (init != null && init.callable(0))
	    initialize = init;
	else initialize = null;
    }


    final void
    setPostprocessStimulus(YoixObject postproc) {

	if (postproc != null && postproc.callable(0))
	    postprocessstimulus = postproc;
	else postprocessstimulus = null;
    }


    final void
    setPreprocessStimulus(YoixObject preproc) {

	if (preproc != null && preproc.callable(0))
	    preprocessstimulus = preproc;
	else preprocessstimulus = null;
    }


    final void
    setTransformInterpolator(int type, J3DAlpha alpha, Transform3D axis, double[] knots, Object values) {

	InterpolatorSpecs  spec;

	spec = new InterpolatorSpecs(type, alpha, axis, knots, values);
	// assume type is in range
	specs[type] = spec;
    }

    ///////////////////////////////////
    //
    // Inner Classes
    //
    ///////////////////////////////////

    class InterpolatorSpecs {

	int               type;
	J3DAlpha          alpha;

	Transform3D       tg_axis;
	double[]          knots;
	Object            values;

	YoixObject        processstimulus;

	InterpolatorSpecs(int type, J3DAlpha alpha, Transform3D axis, double[] knots, Object values) {
	    this.type = type;
	    this.alpha = alpha;

	    switch(type) {
	    case  J3D_POSITIONINTERPOLATOR:
	    case  J3D_ROTATIONINTERPOLATOR:
	    case  J3D_SCALEINTERPOLATOR:
		this.tg_axis = axis;
		this.knots = knots;
		this.values = values;
		break;
	    case  J3D_COLORINTERPOLATOR:
	    case  J3D_CUSTOMCOLORINTERPOLATOR:
	    case  J3D_CUSTOMSWITCHINTERPOLATOR:
	    case  J3D_CUSTOMTRANSFORMINTERPOLATOR:
	    case  J3D_CUSTOMTRANSPARENCYINTERPOLATOR:
	    case  J3D_KBSPLINEINTERPOLATOR:
	    case  J3D_TCBSPLINEINTERPOLATOR:
	    case  J3D_SWITCHVALUEINTERPOLATOR:
	    case  J3D_TRANSPARENCYINTERPOLATOR:
	    default:
		VM.abort(INTERNALERROR);
		break;
	    }
	}

	InterpolatorSpecs(int type, J3DAlpha alpha, YoixObject proc) {
	    this.type = type;
	    this.alpha = alpha;

	    switch(type) {
	    case  J3D_CUSTOMTRANSFORMINTERPOLATOR:
		if (proc != null && proc.callable(0))
		    processstimulus = proc;
		else processstimulus = null;
		break;
	    case  J3D_KBSPLINEINTERPOLATOR:
		VM.abort(INTERNALERROR);
		break;
	    case  J3D_COLORINTERPOLATOR:
	    case  J3D_CUSTOMCOLORINTERPOLATOR:
	    case  J3D_CUSTOMSWITCHINTERPOLATOR:
	    case  J3D_CUSTOMTRANSPARENCYINTERPOLATOR:
	    case  J3D_POSITIONINTERPOLATOR:
	    case  J3D_ROTATIONINTERPOLATOR:
	    case  J3D_SCALEINTERPOLATOR:
	    case  J3D_TCBSPLINEINTERPOLATOR:
	    case  J3D_SWITCHVALUEINTERPOLATOR:
	    case  J3D_TRANSPARENCYINTERPOLATOR:
	    default:
		VM.abort(INTERNALERROR);
		break;
	    }
	}

	InterpolatorSpecs(int type, J3DAlpha alpha, double[] knots, Object values) {
	    this.type = type;
	    this.alpha = alpha;

	    switch(type) {
	    case  J3D_COLORINTERPOLATOR:
		this.knots = knots;
		this.values = values;
		break;
	    case  J3D_CUSTOMCOLORINTERPOLATOR:
	    case  J3D_CUSTOMSWITCHINTERPOLATOR:
	    case  J3D_CUSTOMTRANSFORMINTERPOLATOR:
	    case  J3D_CUSTOMTRANSPARENCYINTERPOLATOR:
	    case  J3D_KBSPLINEINTERPOLATOR:
	    case  J3D_POSITIONINTERPOLATOR:
	    case  J3D_ROTATIONINTERPOLATOR:
	    case  J3D_SCALEINTERPOLATOR:
	    case  J3D_SWITCHVALUEINTERPOLATOR:
	    case  J3D_TCBSPLINEINTERPOLATOR:
	    case  J3D_TRANSPARENCYINTERPOLATOR:
	    default:
		VM.abort(INTERNALERROR);
		break;
	    }
	}

	public String toString() {

	    StringBuffer sb = new StringBuffer();

	    sb.append(getClass().getName());
	    sb.append(": type=");
	    sb.append(type);
	    sb.append(": values=");
	    sb.append(values.getClass().getName());
	    sb.append("\n");

	    return(sb.toString());
	}
    }
}
