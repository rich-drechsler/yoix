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
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.RestrictedAccessException;
import att.research.yoix.*;

class BodyPolygonAttributes extends BodyNodeComponent

    implements Constants

{

    //
    // Decided to keep our own copy of peer to eliminate some casting.
    //

    private PolygonAttributes  attributes = null;

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

    private static HashMap  activefields = new HashMap(10);

    static {
	activefields.put(NL_CAPABILITIES, new Integer(VL_CAPABILITIES));
	activefields.put(NL_COMPILED, new Integer(VL_COMPILED));
	activefields.put(NL_CULLING, new Integer(VL_CULLING));
	activefields.put(NL_DEFAULTCAPABILITY, new Integer(VL_DEFAULTCAPABILITY));
	activefields.put(NL_LIVE, new Integer(VL_LIVE));
	activefields.put(NL_MODE, new Integer(VL_MODE));
	activefields.put(NL_PATH, new Integer(VL_PATH));
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
     // NAME                      CAPABILITY                                             VALUE
     // ----                      ----------                                             -----
	"ALLOW_CULL_FACE_READ",   new Integer(PolygonAttributes.ALLOW_CULL_FACE_READ),   null,
	"ALLOW_CULL_FACE_WRITE",  new Integer(PolygonAttributes.ALLOW_CULL_FACE_WRITE),  null,
	"ALLOW_MODE_READ",        new Integer(PolygonAttributes.ALLOW_MODE_READ),        null,
	"ALLOW_MODE_WRITE",       new Integer(PolygonAttributes.ALLOW_MODE_WRITE),       null,
    };

    static {
	loadCapabilities(capabilities, BodyPolygonAttributes.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyPolygonAttributes(J3DObject data) {

	this(null, data);
    }


    BodyPolygonAttributes(PolygonAttributes attributes) {

	this(attributes, (J3DObject)VM.getTypeTemplate(T_POLYGONATTRIBUTES));
    }


    private
    BodyPolygonAttributes(PolygonAttributes attributes, J3DObject data) {

	super(attributes, data);
	buildPolygonAttributes(attributes);
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

	return(POLYGONATTRIBUTES);
    }

    ///////////////////////////////////
    //
    // BodyPolygonAttributes Methods
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

	attributes = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	int  field;

	try {
	    switch (field = activeField(name, activefields)) {
		case VL_CULLING:
		    obj = getCulling(obj);
		    break;

		case VL_MODE:
		    obj = getMode(obj);
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
		    case VL_CULLING:
			setCulling(obj);
			break;

		    case VL_MODE:
			setMode(obj);
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
    buildPolygonAttributes(PolygonAttributes attributes) {

	if ((this.attributes = attributes) == null) {
	    this.attributes = new PolygonAttributes();
	    peer = this.attributes;
	    setField(NL_DEFAULTCAPABILITY);
	    setField(NL_CULLING);
	    setField(NL_MODE);
	}
	setField(NL_CAPABILITIES);
    }


    private YoixObject
    getCulling(YoixObject obj) {

	return(J3DMake.yoixConstant("Culling", attributes.getCullFace()));
    }


    private YoixObject
    getMode(YoixObject obj) {

	return(J3DMake.yoixConstant("PolygonMode", attributes.getPolygonMode()));
    }


    private void
    setCulling(YoixObject obj) {

	attributes.setCullFace(J3DMake.javaInt("Culling", obj));
    }


    private void
    setMode(YoixObject obj) {

	attributes.setPolygonMode(J3DMake.javaInt("PolygonMode", obj));
    }
}

