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
import javax.media.j3d.Shape3D;
import com.sun.j3d.utils.geometry.Cone;
import att.research.yoix.*;

class BodyCone extends BodyPrimitive

    implements Constants

{

    //
    // Decided to keep our own copy of peer to eliminate some casting.
    //

    private Cone  cone = null;

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
	NL_HEIGHT,                $LR__,       null,
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
	activefields.put(NL_HEIGHT, new Integer(VL_HEIGHT));
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
	loadCapabilities(BodyPrimitive.class, BodyCone.class);
	loadCapabilities(capabilities, BodyCone.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyCone(J3DObject data) {

	this(null, data, null);
    }


    BodyCone(Cone cone) {

	this(cone, (J3DObject)VM.getTypeTemplate(T_CONE), null);
    }


    BodyCone(Cone cone, String tag) {

	//
	// This should only be used when the object is part of scene that
	// was created elsewhere and loaded by our SceneGraphLoader. The
	// only use probably should come from Make.yoixSceneGraphObject().
	//

	this(cone, (J3DObject)VM.getTypeTemplate(T_CONE), tag);
    }


    private
    BodyCone(Cone cone, J3DObject data, String tag) {

	super(cone, data, tag);
	buildCone(cone);
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

	return(CONE);
    }

    ///////////////////////////////////
    //
    // BodyCone Methods
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

	cone = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	int  field;

	try {
	    switch (field = activeField(name, activefields)) {
		case VL_APPEARANCE:
		    obj = getAppearance(obj);
		    break;

		case VL_DIVISIONS:
		    obj = getDivisions(obj);
		    break;

		case VL_HEIGHT:
		    obj = getHeight(obj);
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

	YoixObject  obj;
	Appearance  appearance;
	Shape3D     part;
	int         setting;

	//
	// Creating the Yoix version of each part and updating the default
	// capability of each one is simpler and also works, but it does a
	// bit more than we want right now.
	//

	setting = getCapabilitySetting();
	if ((part = cone.getShape(Cone.BODY)) != null) {
	    changeCapabilitySetting(part, Shape3D.ALLOW_APPEARANCE_READ, setting);
	    changeCapabilitySetting(part, Shape3D.ALLOW_APPEARANCE_WRITE, setting);
	    obj = J3DObject.newAppearance(part.getAppearance());
	    obj.putInt(NL_DEFAULTCAPABILITY, setting);
	}
	if ((part = cone.getShape(Cone.CAP)) != null) {
	    changeCapabilitySetting(part, Shape3D.ALLOW_APPEARANCE_READ, setting);
	    changeCapabilitySetting(part, Shape3D.ALLOW_APPEARANCE_WRITE, setting);
	    obj = J3DObject.newAppearance(part.getAppearance());
	    obj.putInt(NL_DEFAULTCAPABILITY, setting);
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildCone(Cone cone) {

	YoixObject  obj;
	float       radius;
	float       height;
	int         xdivisions = 15;
	int         ydivisions = 1;
	int         flags = 0;

	if ((this.cone = cone) == null) {
	    obj = getObject(NL_DIVISIONS);
	    if (obj.isDictionary()) {
		xdivisions = obj.getInt(NL_X, 15);
		ydivisions = obj.getInt(NL_Y, 1);
	    } else if (obj.isNumber()) {
		xdivisions = obj.intValue();
		ydivisions = 1;
	    } else VM.abort(TYPECHECK, NL_DIVISIONS);

	    radius = getFloat(NL_RADIUS, 1.0f);
	    height = getFloat(NL_HEIGHT, 2.0f);
	    flags |= getBoolean(NL_SHARED) ? 0 : Cone.GEOMETRY_NOT_SHARED;
	    flags |= getBoolean(NL_GENERATENORMALS) ? Cone.GENERATE_NORMALS : 0;
	    flags |= getInt(NL_GENERATENORMALS, 0) < 0 ? Cone.GENERATE_NORMALS_INWARD : 0;
	    flags |= getBoolean(NL_GENERATETEXTURECOORDS) ? Cone.GENERATE_TEXTURE_COORDS : 0;

	    this.cone = new Cone(
		radius > 0 ? radius : 1.0f,
		radius > 0 ? height : 2.0f,
		flags,
		xdivisions >= 1 ? xdivisions : 1,
		ydivisions > 0 ? ydivisions : 0,
		null
	    );
	    peer = this.cone;

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
    getAppearance(YoixObject obj) {

	obj = YoixObject.newDictionary(2);
	obj.putObject(NL_BODY, J3DObject.newAppearance(cone.getAppearance(Cone.BODY)));
	obj.putObject(NL_CAP, J3DObject.newAppearance(cone.getAppearance(Cone.CAP)));
	return(obj);
    }


    private YoixObject
    getDivisions(YoixObject obj) {

	obj = YoixObject.newDictionary(2);
	obj.putInt(NL_X, cone.getXdivisions());
	obj.putInt(NL_Y, cone.getYdivisions());
	return(obj);
    }


    private YoixObject
    getHeight(YoixObject obj) {

	return(YoixObject.newDouble(cone.getHeight()));
    }


    private YoixObject
    getRadius(YoixObject obj) {

	return(YoixObject.newDouble(cone.getRadius()));
    }


    private void
    setAppearance(YoixObject obj) {

	Appearance  appearance;
	YoixObject  entry;
	Shape3D     body;
	Shape3D     cap;

	if (J3DObject.isAppearance(obj) || obj.isDictionary() || obj.isNull()) {
	    if (obj.notNull()) {
		if (obj.isDictionary()) {
		    body = cone.getShape(Cone.BODY);
		    cap = cone.getShape(Cone.CAP);
		    if ((entry = obj.getObject(NL_BODY)) != null) {
			if (J3DObject.isAppearance(entry))
			    body.setAppearance(((J3DObject)entry).getManagedAppearance());
			else if (entry.isNull())
			    body.setAppearance(new Appearance());
			else VM.abort(BADVALUE, NL_APPEARANCE, NL_BODY);
		    }
		    if ((entry = obj.getObject(NL_CAP)) != null) {
			if (J3DObject.isAppearance(entry))
			    cap.setAppearance(((J3DObject)entry).getManagedAppearance());
			else if (entry.isNull())
			    cap.setAppearance(new Appearance());
			else VM.abort(BADVALUE, NL_APPEARANCE, NL_CAP);
		    }
		} else cone.setAppearance(((J3DObject)obj).getManagedAppearance());
	    } else cone.setAppearance(new Appearance());
	} else VM.abort(TYPECHECK, NL_APPEARANCE);
    }
}

