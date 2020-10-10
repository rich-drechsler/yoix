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
import javax.media.j3d.Node;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.RestrictedAccessException;
import javax.media.j3d.Transform3D;
import javax.media.j3d.View;
import javax.media.j3d.ViewPlatform;
import javax.media.j3d.VirtualUniverse;
import javax.vecmath.Vector3d;
import att.research.yoix.*;

class BodyViewPlatform extends BodyNode

    implements Constants

{

    //
    // This class currently doesn't fit the model that you'll find in many
    // of the BodyXXX classes. We will eventually will clean things up.
    //

    private PhysicalEnvironment  environment = null;
    private PhysicalBody         body = null;
    private View                 view = null;

    //
    // Think we need this...
    //

    private BodyVirtualUniverse  virtualuniverse = null;
    private J3DCanvas3D          canvas = null;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	NL_COMPILE,         $LR__,       null,
	NL_TAG,             $LR__,       $LR__,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(25);

    static {
	activefields.put(NL_ACTIVATIONRADIUS, new Integer(VL_ACTIVATIONRADIUS));
	activefields.put(NL_BACKCLIPDISTANCE, new Integer(VL_BACKCLIPDISTANCE));
	activefields.put(NL_CAPABILITIES, new Integer(VL_CAPABILITIES));
	activefields.put(NL_COMPILE, new Integer(VL_COMPILE));
	activefields.put(NL_COMPILED, new Integer(VL_COMPILED));
	activefields.put(NL_DEFAULTCAPABILITY, new Integer(VL_DEFAULTCAPABILITY));
	activefields.put(NL_FIELDOFVIEW, new Integer(VL_FIELDOFVIEW));
	activefields.put(NL_FRONTCLIPDISTANCE, new Integer(VL_FRONTCLIPDISTANCE));
	activefields.put(NL_INTERPOLATOR, new Integer(VL_INTERPOLATOR));
	activefields.put(NL_LIVE, new Integer(VL_LIVE));
	activefields.put(NL_LOCALEYELIGHTING, new Integer(VL_LOCALEYELIGHTING));
	activefields.put(NL_LOCATION, new Integer(VL_LOCATION));
	activefields.put(NL_MOVEMENTPOLICY, new Integer(VL_MOVEMENTPOLICY));
	activefields.put(NL_ORIENTATION, new Integer(VL_ORIENTATION));
	activefields.put(NL_PATH, new Integer(VL_PATH));
	activefields.put(NL_POSITION, new Integer(VL_POSITION));
	activefields.put(NL_RESIZEPOLICY, new Integer(VL_RESIZEPOLICY));
	activefields.put(NL_TAG, new Integer(VL_TAG));
	activefields.put(NL_TRANSFORM, new Integer(VL_TRANSFORM));
    }

    //
    // A table that's used to control capabilities - low level setup
    // happens once when the loadCapabilities() methods are called in
    // the static initialization block that follows the table. Current
    // implementation seems error prone because we're required to pass
    // the correct classes to loadCapabilities(), so be careful if you
    // copy this stuff to different classes!!
    //

    private static Object  capabilities[] = {
     //
     // NAME                     CAPABILITY                                      VALUE
     // ----                     ----------                                      -----
	"ALLOW_POLICY_READ",     new Integer(ViewPlatform.ALLOW_POLICY_READ),    null,
	"ALLOW_POLICY_WRITE",    new Integer(ViewPlatform.ALLOW_POLICY_WRITE),   null,
    };

    static {
	loadCapabilities(BodyNode.class, BodyViewPlatform.class);
	loadCapabilities(capabilities, BodyViewPlatform.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyViewPlatform(J3DObject data) {

	super(data);
	buildViewPlatform(null);
	setFixedSize();
	setPermissions(permissions);
    }


    BodyViewPlatform(VirtualUniverse universe) {

	//
	// Notice that this constructor takes a VirtualUniverse as its only
	// argument.
	//

	super((J3DObject)VM.getTypeTemplate(T_VIEWPLATFORM));
	buildViewPlatform(universe);
	setFixedSize();
	setPermissions(permissions);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(VIEWPLATFORM);
    }

    ///////////////////////////////////
    //
    // BodyViewPlatform Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj = null;
	int         field;

	try {
	    switch (field = activeField(name, activefields)) {
		default:
		    obj = executeField(field, name, argv);
		    break;
	    }
	}
	catch(RestrictedAccessException e) {
	    abort(e, name);
	}
	return(obj);
    }


    protected void
    finalize() {

	view = null;
	body = null;
	environment = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	int  field;

	try {
	    switch (field = activeField(name, activefields)) {
		case VL_ACTIVATIONRADIUS:
		    obj = getActivationRadius(obj);
		    break;

		case VL_BACKCLIPDISTANCE:
		    obj = getBackClipDistance(obj);
		    break;

		case VL_FIELDOFVIEW:
		    obj = getFieldOfView(obj);
		    break;

		case VL_FRONTCLIPDISTANCE:
		    obj = getFrontClipDistance(obj);
		    break;

		case VL_LOCALEYELIGHTING:
		    obj = getLocalEyeLighting(obj);
		    break;

		case VL_MOVEMENTPOLICY:
		    obj = getMovementPolicy(obj);
		    break;

		case VL_RESIZEPOLICY:
		    obj = getResizePolicy(obj);
		    break;

		default:
		    obj = getField(field, obj);
		    break;
	    }
	}
	catch(RestrictedAccessException e) {
	    abort(e, name);
	}
	return(obj);
    }


    final synchronized void
    setCanvas(J3DCanvas3D canvas) {

	//
	// Didn't put any thought into what should really happen if view
	// or virtualuniverse haven't been set yet. Don't think it will
	// be an issue, at least not for a while.
	// 

	if (canvas != null) {
	    if (view != null) {
		view.addCanvas3D(canvas);
		this.canvas = canvas;
		if (virtualuniverse != null)
		    canvas.setUniverse(virtualuniverse);
	    } else VM.abort(INTERNALERROR);
	}
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	int  field;

	if (obj != null) {
	    try {
		switch (field = activeField(name, activefields)) {
		    case VL_ACTIVATIONRADIUS:
			setActivationRadius(obj);
			break;

		    case VL_BACKCLIPDISTANCE:
			setBackClipDistance(obj);
			break;

		    case VL_FIELDOFVIEW:
			setFieldOfView(obj);
			break;

		    case VL_FRONTCLIPDISTANCE:
			setFrontClipDistance(obj);
			break;

		    case VL_LOCALEYELIGHTING:
			setLocalEyeLighting(obj);
			break;

		    case VL_MOVEMENTPOLICY:
			setMovementPolicy(obj);
			break;

		    case VL_RESIZEPOLICY:
			setResizePolicy(obj);
			break;

		    default:
			setField(field, obj);
			break;
		}
	    }
	    catch(RestrictedAccessException e) {
		abort(e, name);
	    }
	}
	return(obj);
    }


    final synchronized void
    setUniverse(BodyVirtualUniverse universe) {

	//
	// Didn't put any thought into what should really happen if we've
	// already set virtualuniverse. Don't think it will be an issue,
	// at least not for a while.
	// 

	if (virtualuniverse == null || virtualuniverse == universe) {
	    virtualuniverse = universe;
	    if (canvas != null)
		canvas.setUniverse(virtualuniverse);
	} else VM.abort(INTERNALERROR);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildViewPlatform(VirtualUniverse universe) {

	double  fov;
	double  z;

	//
	// Doesn't fit nicely into the format of the buildXXX() methods that
	// you'll find in most other classes. We'll eventually try to clean
	// things up, but it probably will always be non-standard. Written
	// very early in our 3D work so there's undoubtedly lots of room for
	// improvement.
	//

	peer = new ViewPlatform();
	view = new View();
	body = new PhysicalBody();
	environment = new PhysicalEnvironment();
	view.setPhysicalBody(body);
	view.setPhysicalEnvironment(environment);
	view.attachViewPlatform((ViewPlatform)peer);

	setField(NL_ACTIVATIONRADIUS);
	setField(NL_BACKCLIPDISTANCE);
	setField(NL_FRONTCLIPDISTANCE);
	setField(NL_FIELDOFVIEW);
	setField(NL_LOCALEYELIGHTING);

	if (universe != null) {
	    if ((fov = view.getFieldOfView()) > 0 && fov < Math.PI)
		z = 1.0/Math.tan(fov/2.0);
	    else z = 0;
	    data.putObject(NL_TRANSFORM, J3DObject.newTransform3D(new Transform3D()));
	    data.putObject(NL_ORIENTATION, J3DObject.newEulerAngle(0, 0, 0));
	    data.putObject(NL_POSITION, J3DObject.newPoint3D(0, 0, z));
	    data.putString(NL_TAG, TAG_VIEWPLATFORM);
	}

	setField(NL_DEFAULTCAPABILITY);
	setField(NL_TAG);
	setField(NL_INTERPOLATOR);
	setField(NL_TRANSFORM);
	setField(NL_ORIENTATION);
	setField(NL_POSITION);
	setField(NL_MOVEMENTPOLICY);
	setField(NL_RESIZEPOLICY);
	setField(NL_CAPABILITIES);
    }


    private YoixObject
    getActivationRadius(YoixObject obj) {

	Object  comp;
	double  radius;

	//
	// j3d works in meters, so do we need to convert the radius to something
	// else before returning it - later??
	//

	comp = peer;		// snapshot - just to be safe
	if (comp instanceof ViewPlatform)
	    radius = ((ViewPlatform)comp).getActivationRadius();
	else radius = 0;

	return(YoixObject.newDouble(radius));
    }


    private YoixObject
    getBackClipDistance(YoixObject obj) {

	//
	// j3d works in meters, so do we need to convert the distance to something
	// else before returning it - later??
	//

	return(YoixObject.newDouble(view.getBackClipDistance()));
    }


    private YoixObject
    getFieldOfView(YoixObject obj) {

	return(YoixObject.newDouble(view.getFieldOfView()));
    }


    private YoixObject
    getFrontClipDistance(YoixObject obj) {

	//
	// j3d works in meters, so do we need to convert the distance to something
	// else before returning it - later??
	//

	return(YoixObject.newDouble(view.getFrontClipDistance()));
    }


    private YoixObject
    getLocalEyeLighting(YoixObject obj) {

	return(YoixObject.newInt(view.getLocalEyeLightingEnable()));
    }


    private YoixObject
    getMovementPolicy(YoixObject obj) {

	return(J3DMake.yoixConstant("WindowMovementPolicy", view.getWindowMovementPolicy()));
    }


    private YoixObject
    getResizePolicy(YoixObject obj) {

	return(J3DMake.yoixConstant("WindowResizePolicy", view.getWindowResizePolicy()));
    }


    private void
    setActivationRadius(YoixObject obj) {

	Object  comp;
	float   radius;

	//
	// j3d works in meters, so do we need to convert the value before storing
	// it in the ViewPlatform??
	//

	comp = peer;			// snapshot - just to be safe

	if (obj.isDouble() || obj.isNull()) {
	    if (obj.isDouble()) {
		if ((radius = obj.floatValue()) > 0) {
		    if (comp instanceof ViewPlatform)
			((ViewPlatform)comp).setActivationRadius(radius);
		} else VM.abort(BADVALUE, NL_ACTIVATIONRADIUS);
	    }
	} else VM.abort(TYPECHECK, NL_ACTIVATIONRADIUS);
    }


    private void
    setBackClipDistance(YoixObject obj) {

	float  distance;

	//
	// j3d works in meters, so do we need to convert the value before storing
	// it in the ViewPlatform??
	//

	if (obj.isDouble() || obj.isNull()) {
	    if (obj.isDouble()) {
		if ((distance = obj.floatValue()) > 0)
		    view.setBackClipDistance(distance);
		else VM.abort(BADVALUE, NL_BACKCLIPDISTANCE);
	    }
	} else VM.abort(TYPECHECK, NL_BACKCLIPDISTANCE);
    }


    private void
    setFieldOfView(YoixObject obj) {

	double  value;

	value = obj.doubleValue();
	if (!Double.isNaN(value)) {
	    if (value > 0 && value < Math.PI)
		view.setFieldOfView(value);
	}
    }


    private void
    setFrontClipDistance(YoixObject obj) {

	float  distance;

	//
	// j3d works in meters, so do we need to convert the value before storing
	// it in the ViewPlatform??
	//

	if (obj.isDouble() || obj.isNull()) {
	    if (obj.isDouble()) {
		if ((distance = obj.floatValue()) > 0)
		    view.setFrontClipDistance(distance);
		else VM.abort(BADVALUE, NL_FRONTCLIPDISTANCE);
	    }
	} else VM.abort(TYPECHECK, NL_FRONTCLIPDISTANCE);
    }


    private void
    setLocalEyeLighting(YoixObject obj) {

	view.setLocalEyeLightingEnable(obj.booleanValue());
    }


    private void
    setMovementPolicy(YoixObject obj) {

	view.setWindowMovementPolicy(J3DMake.javaInt("WindowMovementPolicy", obj));
    }


    private void
    setResizePolicy(YoixObject obj) {

	view.setWindowResizePolicy(J3DMake.javaInt("WindowResizePolicy", obj));
    }
}

