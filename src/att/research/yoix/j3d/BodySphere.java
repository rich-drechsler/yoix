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
import javax.media.j3d.Appearance;
import javax.media.j3d.RestrictedAccessException;
import com.sun.j3d.utils.geometry.Sphere;
import att.research.yoix.*;

class BodySphere extends BodyPrimitive

    implements Constants

{

    //
    // Decided to keep our own copy of peer to eliminate some casting.
    //

    private Sphere  sphere = null;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD                     OBJECT       BODY
     // -----                     ------       ----
	NL_DIVISIONS,             $LR__,       null,
	NL_GENERATENORMALS,       $LR__,       null,
	NL_GENERATETEXTURECOORDS, $LR__,       null,
	NL_RADIUS,                $LR__,       null,
	NL_SHARED,                $LR__,       null,
	NL_TAG,                   $LR__,       $LR__,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(25);

    static {
	activefields.put(NL_APPEARANCE, new Integer(VL_APPEARANCE));
	activefields.put(NL_CAPABILITIES, new Integer(VL_CAPABILITIES));
	activefields.put(NL_COLLIDABLE, new Integer(VL_COLLIDABLE));
	activefields.put(NL_COMPILED, new Integer(VL_COMPILED));
	activefields.put(NL_DEFAULTCAPABILITY, new Integer(VL_DEFAULTCAPABILITY));
	activefields.put(NL_DIVISIONS, new Integer(VL_DIVISIONS));
	activefields.put(NL_GENERATENORMALS, new Integer(VL_GENERATENORMALS));
	activefields.put(NL_GENERATETEXTURECOORDS, new Integer(VL_GENERATETEXTURECOORDS));
	activefields.put(NL_INTERPOLATOR, new Integer(VL_INTERPOLATOR));
	activefields.put(NL_LIVE, new Integer(VL_LIVE));
	activefields.put(NL_LOCATION, new Integer(VL_LOCATION));
	activefields.put(NL_ORIENTATION, new Integer(VL_ORIENTATION));
	activefields.put(NL_PATH, new Integer(VL_PATH));
	activefields.put(NL_PICKABLE, new Integer(VL_PICKABLE));
	activefields.put(NL_POSITION, new Integer(VL_POSITION));
	activefields.put(NL_RADIUS, new Integer(VL_RADIUS));
	activefields.put(NL_SHARED, new Integer(VL_SHARED));
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
     // NAME                          CAPABILITY                                      VALUE
     // ----                          ----------                                      -----
    };

    static {
	loadCapabilities(BodyPrimitive.class, BodySphere.class);
	loadCapabilities(capabilities, BodySphere.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodySphere(J3DObject data) {

	this(null, data, null);
    }


    BodySphere(Sphere sphere) {

	this(sphere, (J3DObject)VM.getTypeTemplate(T_SPHERE), null);
    }


    BodySphere(Sphere sphere, String tag) {

	//
	// This should only be used when the object is part of scene that
	// was created elsewhere and loaded by our SceneGraphLoader. The
	// only use probably should come from Make.yoixSceneGraphObject().
	//

	this(sphere, (J3DObject)VM.getTypeTemplate(T_SPHERE), tag);
    }


    private
    BodySphere(Sphere sphere, J3DObject data, String tag) {

	super(sphere, data, tag);
	buildSphere(sphere);
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

	return(SPHERE);
    }

    ///////////////////////////////////
    //
    // BodySphere Methods
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

	sphere = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	int  field;

	try {
	    switch (field = activeField(name, activefields)) {
		case VL_DIVISIONS:
		    obj = getDivisions(obj);
		    break;

		case VL_RADIUS:
		    obj = getRadius(obj);
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
		    case VL_APPEARANCE:
			setAppearance(obj);
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


    protected final void
    updateCapabilities() {

	//
	// This is sufficient for a Sphere, but Primitives that have more
	// than one part use a different approach (see BodyCone.java).
	//

	updateDefaultCapabilities(NL_APPEARANCE);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildSphere(Sphere sphere) {

	float  radius;
	int    divisions;
	int    flags = 0;

	if ((this.sphere = sphere) == null) {
	    radius = getFloat(NL_RADIUS, 1.0f);
	    divisions = getInt(NL_DIVISIONS, 16);
	    flags |= getBoolean(NL_SHARED) ? 0 : Sphere.GEOMETRY_NOT_SHARED;
	    flags |= getBoolean(NL_GENERATENORMALS) ? Sphere.GENERATE_NORMALS : 0;
	    flags |= getInt(NL_GENERATENORMALS, 0) < 0 ? Sphere.GENERATE_NORMALS_INWARD : 0;
	    flags |= getBoolean(NL_GENERATETEXTURECOORDS) ? Sphere.GENERATE_TEXTURE_COORDS : 0;

	    this.sphere = new Sphere(
		radius > 0 ? radius : 1.0f,
		flags,
		divisions > 0 ? divisions : 0
	    );
	    peer = this.sphere;

	    setField(NL_DEFAULTCAPABILITY);
	    setField(NL_TAG);
	    setField(NL_COLLIDABLE);
	    setField(NL_PICKABLE);
	    setField(NL_INTERPOLATOR);
	    setField(NL_TRANSFORM);
	    setField(NL_ORIENTATION);
	    setField(NL_POSITION);
	    setField(NL_APPEARANCE);
	}
	setField(NL_CAPABILITIES);
    }


    private YoixObject
    getDivisions(YoixObject obj) {

	return(YoixObject.newInt(sphere.getDivisions()));
    }


    private YoixObject
    getRadius(YoixObject obj) {

	return(YoixObject.newDouble(sphere.getRadius()));
    }


    private void
    setAppearance(YoixObject obj) {

	if (J3DObject.isAppearance(obj) || obj.isNull()) {
	    if (obj.notNull())
		sphere.setAppearance(((J3DObject)obj).getManagedAppearance());
	    else sphere.setAppearance(new Appearance());
	} else VM.abort(TYPECHECK, NL_APPEARANCE);
    }
}

