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
import javax.media.j3d.RestrictedAccessException;
import com.sun.j3d.utils.geometry.ColorCube;
import att.research.yoix.*;

class BodyColorCube extends BodyNode

    implements Constants

{

    //
    // Decided to keep our own copy of peer to eliminate some casting.
    //

    private ColorCube  cube = null;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	NL_SCALE,           $LR__,       null,
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
	activefields.put(NL_CAPABILITIES, new Integer(VL_CAPABILITIES));
	activefields.put(NL_COLLIDABLE, new Integer(VL_COLLIDABLE));
	activefields.put(NL_COMPILED, new Integer(VL_COMPILED));
	activefields.put(NL_DEFAULTCAPABILITY, new Integer(VL_DEFAULTCAPABILITY));
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
     // NAME                          CAPABILITY                                      VALUE
     // ----                          ----------                                      -----
    };

    static {
	loadCapabilities(BodyNode.class, BodyColorCube.class);
	loadCapabilities(capabilities, BodyColorCube.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyColorCube(J3DObject data) {

	this(null, data, null);
    }


    BodyColorCube(ColorCube cube) {

	this(cube, (J3DObject)VM.getTypeTemplate(T_COLORCUBE), null);
    }


    BodyColorCube(ColorCube cube, String tag) {

	//
	// This should only be used when the object is part of scene that
	// was created elsewhere and loaded by our SceneGraphLoader. The
	// only use probably should come from Make.yoixSceneGraphObject().
	//

	this(cube, (J3DObject)VM.getTypeTemplate(T_COLORCUBE), tag);
    }


    private
    BodyColorCube(ColorCube cube, J3DObject data, String tag) {

	super(cube, data, tag);
	buildColorCube(cube);
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

	return(COLORCUBE);
    }

    ///////////////////////////////////
    //
    // BodyColorCube Methods
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

	cube = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	int  field;

	try {
	    switch (field = activeField(name, activefields)) {
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
    buildColorCube(ColorCube cube) {

	//
	// Do we need to force scale into bounds??
	//

	if ((this.cube = cube) == null) {
	    this.cube = new ColorCube(getDouble(NL_SCALE, 1.0));
	    peer = this.cube;
	    setField(NL_DEFAULTCAPABILITY);
	    setField(NL_TAG);
	    setField(NL_COLLIDABLE);
	    setField(NL_PICKABLE);
	    setField(NL_INTERPOLATOR);
	    setField(NL_TRANSFORM);
	    setField(NL_ORIENTATION);
	    setField(NL_POSITION);
	}
	setField(NL_CAPABILITIES);
    }
}

