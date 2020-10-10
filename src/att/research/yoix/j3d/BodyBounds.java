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

class BodyBounds extends J3DPointerActive

    implements Constants

{

    private Bounds  bounds = null;
    private int     type   = 0;

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

    private static HashMap  activefields = new HashMap(12);

    static {
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyBounds(J3DObject data) {

	super(data);
	this.data = data;
	buildBounds(data);
	setFixedSize();
	setPermissions(permissions);
    }


    BodyBounds(Bounds bounds) {
	    
	this((J3DObject)YoixMake.yoixType(T_BOUNDS), bounds);
    }


    BodyBounds(J3DObject data, Bounds bounds) {

	super(data);
	this.data = data;
	this.bounds = bounds;
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

	return(BOUNDS);
    }

    ///////////////////////////////////
    //
    // BodyBounds Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    default:
		obj = null;
		break;
	}
	return(obj);
    }


    protected void
    finalize() {

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

	return(bounds);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {

	    default:
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
    buildBounds(J3DObject data) {

	YoixObject  yobj;

	if ((type = getInt(NL_TYPE, 0)) != 0) {
	    switch(type) {
		case J3D_BOUNDINGBOX:
		    bounds = new BoundingBox();
		    if ((yobj = getJ3DObject(NL_LOWER)) == null)
			((BoundingBox)bounds).setLower(-1.0, -1.0, -1.0);
		    else ((BoundingBox)bounds).setLower(Make.javaPoint3d(yobj));
		    if ((yobj = getJ3DObject(NL_UPPER)) == null)
			((BoundingBox)bounds).setLower(1.0, 1.0, 1.0);
		    else ((BoundingBox)bounds).setUpper(Make.javaPoint3d(yobj));
		    break;

		case J3D_BOUNDINGSPHERE:
		    bounds = new BoundingSphere();
		    if ((yobj = getJ3DObject(NL_CENTER)) == null)
			((BoundingSphere)bounds).setCenter(new Point3d(0.0, 0.0, 0.0));
		    else ((BoundingSphere)bounds).setCenter(Make.javaPoint3d(yobj));
		    if ((yobj = getObject(NL_RADIUS)) == null)
			((BoundingSphere)bounds).setRadius(Double.POSITIVE_INFINITY);
		    else ((BoundingSphere)bounds).setRadius(Math.abs(yobj.doubleValue()));
		    break;

		default:
		    VM.abort(BADVALUE, NL_TYPE);
		    break;
	    }
	}
    }

}
