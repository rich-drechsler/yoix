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
import java.util.Enumeration;
import java.util.HashMap;
import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.RestrictedAccessException;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;
import att.research.yoix.*;

class BodyShape3D extends BodyNode

    implements Constants

{

    //
    // Decided to keep our own copy of peer to eliminate some casting.
    //

    private Shape3D  shape = null;

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

    private static HashMap  activefields = new HashMap(20);

    static {
	activefields.put(NL_APPEARANCE, new Integer(VL_APPEARANCE));
	activefields.put(NL_APPEARANCEOVERRIDE, new Integer(VL_APPEARANCEOVERRIDE));
	activefields.put(NL_CAPABILITIES, new Integer(VL_CAPABILITIES));
	activefields.put(NL_COLLIDABLE, new Integer(VL_COLLIDABLE));
	activefields.put(NL_COMPILED, new Integer(VL_COMPILED));
	activefields.put(NL_DEFAULTCAPABILITY, new Integer(VL_DEFAULTCAPABILITY));
	activefields.put(NL_GEOMETRY, new Integer(VL_GEOMETRY));
	activefields.put(NL_INTERPOLATOR, new Integer(VL_INTERPOLATOR));
	activefields.put(NL_LIVE, new Integer(VL_LIVE));
	activefields.put(NL_LOCATION, new Integer(VL_LOCATION));
	activefields.put(NL_ORIENTATION, new Integer(VL_ORIENTATION));
	activefields.put(NL_PATH, new Integer(VL_PATH));
	activefields.put(NL_PICKABLE, new Integer(VL_PICKABLE));
	activefields.put(NL_POSITION, new Integer(VL_POSITION));
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
     // NAME                                CAPABILITY                                             VALUE
     // ----                                ----------                                             -----
	"ALLOW_APPEARANCE_OVERRIDE_READ",   new Integer(Shape3D.ALLOW_APPEARANCE_OVERRIDE_READ),   null,
	"ALLOW_APPEARANCE_OVERRIDE_WRITE",  new Integer(Shape3D.ALLOW_APPEARANCE_OVERRIDE_WRITE),  null,
	"ALLOW_APPEARANCE_READ",            new Integer(Shape3D.ALLOW_APPEARANCE_READ),            null,
	"ALLOW_APPEARANCE_WRITE",           new Integer(Shape3D.ALLOW_APPEARANCE_WRITE),           null,
	"ALLOW_GEOMETRY_READ",              new Integer(Shape3D.ALLOW_GEOMETRY_READ),              null,
	"ALLOW_GEOMETRY_WRITE",             new Integer(Shape3D.ALLOW_GEOMETRY_WRITE),             null,
    };

    static {
	loadCapabilities(BodyNode.class, BodyShape3D.class);
	loadCapabilities(capabilities, BodyShape3D.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyShape3D(J3DObject data) {

	this(null, data, null);
    }


    BodyShape3D(Shape3D shape) {

	this(shape, (J3DObject)VM.getTypeTemplate(T_SHAPE3D), null);
    }


    BodyShape3D(Shape3D shape, String tag) {

	//
	// This should only be used when the object is part of scene that
	// was created elsewhere and loaded by our SceneGraphLoader. The
	// only use probably should come from Make.yoixSceneGraphObject().
	//

	this(shape, (J3DObject)VM.getTypeTemplate(T_SHAPE3D), tag);
    }


    private
    BodyShape3D(Shape3D shape, J3DObject data, String tag) {

	super(shape, data, tag);
	buildShape3D(shape);
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

	return(SHAPE3D);
    }

    ///////////////////////////////////
    //
    // BodyShape3D Methods
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

	shape = null;
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

		case VL_APPEARANCEOVERRIDE:
		    obj = getAppearanceOverride(obj);
		    break;

		case VL_GEOMETRY:
		    obj = getGeometry(obj);
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

		    case VL_APPEARANCEOVERRIDE:
			setAppearanceOverride(obj);
			break;

		    case VL_GEOMETRY:
			setGeometry(obj);
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

	updateDefaultCapabilities(
	    new String[] {
		NL_APPEARANCE,
		NL_GEOMETRY,
	    }
	);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildShape3D(Shape3D shape) {

	if ((this.shape = shape) == null) {
	    this.shape = new Shape3D();
	    peer = this.shape;
	    setField(NL_DEFAULTCAPABILITY);
	    setField(NL_TAG);
	    setField(NL_COLLIDABLE);
	    setField(NL_PICKABLE);
	    setField(NL_INTERPOLATOR);
	    setField(NL_TRANSFORM);
	    setField(NL_ORIENTATION);
	    setField(NL_POSITION);
	    setField(NL_APPEARANCE);
	    setField(NL_APPEARANCEOVERRIDE);
	    setField(NL_GEOMETRY);
	}
	setField(NL_CAPABILITIES);
    }


    private synchronized YoixObject
    getAppearance(YoixObject obj) {

	return(J3DObject.newAppearance(shape.getAppearance()));
    }


    private synchronized YoixObject
    getAppearanceOverride(YoixObject obj) {

	return(YoixObject.newInt(shape.getAppearanceOverrideEnable()));
    }


    private synchronized YoixObject
    getGeometry(YoixObject obj) {

	Enumeration  enm;
	YoixObject   geometry;

	obj = YoixObject.newArray(0, -1);
	for (enm = shape.getAllGeometries(); enm.hasMoreElements(); ) {
	    if ((geometry = J3DObject.newGeometry((Geometry)enm.nextElement())) != null)
		obj.putObject(obj.sizeof(), geometry);
	}
	return(obj);
    }


    private synchronized void
    setAppearance(YoixObject obj) {

	Appearance  appearance;

	if (J3DObject.isAppearance(obj) || obj.isNull()) {
	    if (obj.isNull()) {
		appearance = new Appearance();
		data.putObject(NL_APPEARANCE, J3DObject.newAppearance(appearance));
	    } else appearance = ((J3DObject)obj).getManagedAppearance();
	    shape.setAppearance(appearance);
	} else VM.abort(TYPECHECK, NL_APPEARANCE);
    }


    private synchronized void
    setAppearanceOverride(YoixObject obj) {

	shape.setAppearanceOverrideEnable(obj.booleanValue());
    }


    private synchronized void
    setGeometry(YoixObject obj) {

	YoixObject  entry;
	Geometry    geometries[];
	int         length;
	int         n;

	if (J3DObject.isGeometry(obj) || obj.isArray() || obj.isNull()) {
	    if (obj.notNull()) {
		if (obj.isArray()) {
		    geometries = new Geometry[obj.sizeof()];
		    length = obj.length();
		    for (n = obj.offset(); n < length; n++) {
			if ((entry = obj.getObject(n)) != null) {
			    if (entry.notNull()) {
				if (J3DObject.isGeometry(entry))
				    geometries[n] = ((J3DObject)entry).getManagedGeometry();
				else VM.abort(BADVALUE, NL_GEOMETRY, n);
			    }
			}
		    }
		} else geometries = new Geometry[] {((J3DObject)obj).getManagedGeometry()};
		shape.removeAllGeometries();
		for (n = 0; n < geometries.length; n++) {
		    if (geometries[n] != null)
			shape.addGeometry(geometries[n]);
		}
	    } else shape.removeAllGeometries();
	}
    }
}

