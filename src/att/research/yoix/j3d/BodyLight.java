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
import javax.media.j3d.BoundingLeaf;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.Light;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import att.research.yoix.*;

abstract
class BodyLight extends BodyNode

    implements Constants

{

    //
    // Decided to keep our own copy of peer to eliminate some casting.
    //

    protected Light  light = null; 

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
     // NAME                        CAPABILITY                                 VALUE
     // ----                        ----------                                 -----
	"ALLOW_COLOR_READ",         new Integer(Light.ALLOW_COLOR_READ),       null,
	"ALLOW_COLOR_WRITE",        new Integer(Light.ALLOW_COLOR_WRITE),      null,
	"ALLOW_STATE_READ",         new Integer(Light.ALLOW_STATE_READ),       null,
	"ALLOW_STATE_WRITE",        new Integer(Light.ALLOW_STATE_WRITE),      null,
    };

    static {
	loadCapabilities(BodyNode.class, BodyLight.class);
	loadCapabilities(capabilities, BodyLight.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyLight(J3DObject data) {

	super(data);
    }


    BodyLight(Light light, J3DObject data) {

	super(light, data);
    }


    BodyLight(Light light, J3DObject data, String tag) {

	super(light, data, tag);
    }

    ///////////////////////////////////
    //
    // BodyLight Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	light = null;
	super.finalize();
    }


    protected final YoixObject
    getField(int field, YoixObject obj) {

	switch (field) {
	    case VL_COLOR:
		obj = getColor(obj);
		break;

	    case VL_ENABLED:
		obj = getEnabled(obj);
		break;

	    default:
		obj = super.getField(field, obj);
		break;
	}
	return(obj);
    }


    protected final YoixObject
    setField(int field, YoixObject obj) {

	if (obj != null) {
	    switch (field) {
		case VL_BOUNDINGLEAF:
		    setBoundingLeaf(obj);
		    break;

		case VL_BOUNDS:
		    setBounds(obj);
		    break;

		case VL_COLOR:
		    setColor(obj);
		    break;

		case VL_ENABLED:
		    setEnabled(obj);
		    break;

		default:
		    super.setField(field, obj);
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

    private YoixObject
    getColor(YoixObject obj) {

	Color3f  color;

	color = new Color3f();
	light.getColor(color);
	return(Make.yoixColor(color));
    }


    private YoixObject
    getEnabled(YoixObject obj) {

	return(YoixObject.newInt(light.getEnable()));
    }


    private void
    setBoundingLeaf(YoixObject obj) {

	if (obj.isNull())
	    light.setInfluencingBoundingLeaf(null);
	else light.setInfluencingBoundingLeaf(new BoundingLeaf(((J3DObject)obj).getManagedBounds()));
    }


    private void
    setBounds(YoixObject obj) {

	if (obj.isNull()) {
	    light.setInfluencingBounds(
		new BoundingSphere(
		    new Point3d(),
		    Double.POSITIVE_INFINITY
		)
	    );
	} else light.setInfluencingBounds(((J3DObject)obj).getManagedBounds());
    }


    private void
    setColor(YoixObject obj) {

	light.setColor(Make.javaColor3f(obj, COLOR3F_WHITE));
    }


    private void
    setEnabled(YoixObject obj) {

	light.setEnable(obj.booleanValue());
    }
}

