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
import java.awt.*;
import java.util.HashMap;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.RestrictedAccessException;
import javax.vecmath.Vector3f;
import att.research.yoix.*;

class BodyDirectionalLight extends BodyLight

    implements Constants

{

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

    private static HashMap  activefields = new HashMap(15);

    static {
	activefields.put(NL_BOUNDINGLEAF, new Integer(VL_BOUNDINGLEAF));
	activefields.put(NL_BOUNDS, new Integer(VL_BOUNDS));
	activefields.put(NL_CAPABILITIES, new Integer(VL_CAPABILITIES));
	activefields.put(NL_COLOR, new Integer(VL_COLOR));
	activefields.put(NL_COMPILED, new Integer(VL_COMPILED));
	activefields.put(NL_DEFAULTCAPABILITY, new Integer(VL_DEFAULTCAPABILITY));
	activefields.put(NL_DIRECTION, new Integer(VL_DIRECTION));
	activefields.put(NL_ENABLED, new Integer(VL_ENABLED));
	activefields.put(NL_INTERPOLATOR, new Integer(VL_INTERPOLATOR));
	activefields.put(NL_LIVE, new Integer(VL_LIVE));
	activefields.put(NL_PATH, new Integer(VL_PATH));
	activefields.put(NL_TAG, new Integer(VL_TAG));
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
     // NAME                        CAPABILITY                                             VALUE
     // ----                        ----------                                             -----
	"ALLOW_DIRECTION_READ",     new Integer(DirectionalLight.ALLOW_DIRECTION_READ),    null,
	"ALLOW_DIRECTION_WRITE",    new Integer(DirectionalLight.ALLOW_DIRECTION_WRITE),   null,
    };

    static {
	loadCapabilities(BodyLight.class, BodyDirectionalLight.class);
	loadCapabilities(capabilities, BodyDirectionalLight.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyDirectionalLight(J3DObject data) {

	this(null, data, null);
    }


    BodyDirectionalLight(DirectionalLight light) {

	this(light, (J3DObject)VM.getTypeTemplate(T_DIRECTIONALLIGHT), null);
    }


    BodyDirectionalLight(DirectionalLight light, String tag) {

	//
	// This should only be used when the object is part of scene that
	// was created elsewhere and loaded by our SceneGraphLoader. The
	// only use probably should come from Make.yoixSceneGraphObject().
	//

	this(light, (J3DObject)VM.getTypeTemplate(T_DIRECTIONALLIGHT), tag);
    }


    private
    BodyDirectionalLight(DirectionalLight light, J3DObject data, String tag) {

	super(light, data, tag);
	buildDirectionalLight(light);
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

	return(DIRECTIONALLIGHT);
    }

    ///////////////////////////////////
    //
    // BodyDirectionalLight Methods
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

	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	int  field;

	try {
	    switch (field = activeField(name, activefields)) {
		case VL_DIRECTION:
		    obj = getDirection(obj);
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


    protected final YoixObject
    setField(String name, YoixObject obj) {

	int  field;

	if (obj != null) {
	    try {
		switch (field = activeField(name, activefields)) {
		    case VL_DIRECTION:
			setDirection(obj);
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

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildDirectionalLight(DirectionalLight light) {

	if ((this.light = light) == null) {
	    this.light = new DirectionalLight();
	    peer = this.light;
	    setField(NL_DEFAULTCAPABILITY);
	    setField(NL_TAG);
	    setField(NL_BOUNDINGLEAF);
	    setField(NL_BOUNDS);
	    setField(NL_DIRECTION);
	    setField(NL_COLOR);
	    setField(NL_INTERPOLATOR);
	    setField(NL_ENABLED);
	}
	setField(NL_CAPABILITIES);
    }


    private YoixObject
    getDirection(YoixObject obj) {

	Vector3f  vector;

	vector = new Vector3f();
	((DirectionalLight)light).getDirection(vector);
	return(J3DObject.newVector3D(vector));
    }


    private void
    setDirection(YoixObject obj) {

	((DirectionalLight)light).setDirection(Make.javaVector3f(obj));
    }
}

