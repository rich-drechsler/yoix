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

//
// Under construction... will not compile! (it is not in j3d.mk)
//

package att.research.yoix.j3d;
import java.awt.*;
import java.awt.GraphicsConfiguration;
import java.awt.event.*;
import java.util.HashMap;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.geometry.*;
import att.research.yoix.*;

public
class BodyWapeup extends J3DPointerActive

    implements Constants

{

    private WapeupCondition  wakeup = null;
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

    private static HashMap  activefields = new HashMap(12);

    static {
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    BodyWakeup(J3DObject data) {

	super(data);
	this.data = data;
	buildWakeup(data);
	setFixedSize();
	setPermissions(permissions);
    }


    public
    BodyWakeup(WakeupCondition wakeup) {
	    
	this((J3DObject)YoixMake.yoixType(T_WAKEUP), wakeup);
    }


    public
    BodyWakeup(J3DObject data, WakeupCondition wakeup) {

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
    // BodyWakeup Methods
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
    buildWakeup(J3DObject data) {

	YoixObject  yobj;
	double      dval;

	type = data.getInt(NL_TYPE, 0);

	if (type != 0) {
	    switch(type) {
	    case J3D_BOUNDINGBOX:
	    {
		BoundingBox newbounds = new BoundingBox();
		if ((yobj = getJ3DObject(NL_LOWER)) == null)
		    newbounds.setLower(-1.0, -1.0, -1.0);
		else newbounds.setLower(Make.javaPoint3d(yobj));
		if ((yobj = getJ3DObject(NL_UPPER)) == null)
		    newbounds.setLower(1.0, 1.0, 1.0);
		else newbounds.setUpper(Make.javaPoint3d(yobj));
		bounds = newbounds;
	    }
	    break;
	    case J3D_BOUNDINGSPHERE:
	    {
		BoundingSphere newbounds = new BoundingSphere();
		if ((yobj = getJ3DObject(NL_CENTER)) == null)
		    newbounds.setCenter(new Point3d(0.0, 0.0, 0.0));
		else newbounds.setCenter(Make.javaPoint3d(yobj));
		if ((yobj = getObject(NL_RADIUS)) == null)
		    newbounds.setRadius(1.0);
		else {
		   dval = yobj.doubleValue();
		   if (dval < 0)
		       VM.abort(BADVALUE, NL_RADIUS);// or ignore sign? dval = -dval;
		   newbounds.setRadius(dval);
		}
		bounds = newbounds;
	    }
	    break;
	    case J3D_BOUNDINGPOLYTOPE:
	    {
		VM.abort(UNIMPLEMENTED, NL_TYPE);
	    }
	    break;
	    default:
	    {
		VM.abort(BADVALUE, NL_TYPE);
	    }
	    break;
	    }
	}
    }

}
