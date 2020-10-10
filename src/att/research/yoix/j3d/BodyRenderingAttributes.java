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
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.RestrictedAccessException;
import javax.vecmath.Color3f;
import att.research.yoix.*;

class BodyRenderingAttributes extends BodyNodeComponent

    implements Constants

{

    //
    // Decided to keep our own copy of peer to eliminate some casting.
    //

    private RenderingAttributes  attributes = null;

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
	activefields.put(NL_DEFAULTCAPABILITY, new Integer(VL_DEFAULTCAPABILITY));
	activefields.put(NL_IGNOREVERTEXCOLORS, new Integer(VL_IGNOREVERTEXCOLORS));
	activefields.put(NL_LIVE, new Integer(VL_LIVE));
	activefields.put(NL_PATH, new Integer(VL_PATH));
	activefields.put(NL_VISIBLE, new Integer(VL_VISIBLE));
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
     // NAME                                CAPABILITY                                                          VALUE
     // ----                                ----------                                                          -----
	"ALLOW_IGNORE_VERTEX_COLORS_READ",  new Integer(RenderingAttributes.ALLOW_IGNORE_VERTEX_COLORS_READ),   null,
	"ALLOW_IGNORE_VERTEX_COLORS_WRITE", new Integer(RenderingAttributes.ALLOW_IGNORE_VERTEX_COLORS_WRITE),  null,
	"ALLOW_VISIBLE_READ",               new Integer(RenderingAttributes.ALLOW_VISIBLE_READ),                null,
	"ALLOW_VISIBLE_WRITE",              new Integer(RenderingAttributes.ALLOW_VISIBLE_WRITE),               null,
    };

    static {
	loadCapabilities(capabilities, BodyRenderingAttributes.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyRenderingAttributes(J3DObject data) {

	this(null, data);
    }


    BodyRenderingAttributes(RenderingAttributes attributes) {

	this(attributes, (J3DObject)VM.getTypeTemplate(T_RENDERINGATTRIBUTES));
    }


    private
    BodyRenderingAttributes(RenderingAttributes attributes, J3DObject data) {

	super(attributes, data);
	buildRenderingAttributes(attributes);
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

	return(RENDERINGATTRIBUTES);
    }

    ///////////////////////////////////
    //
    // BodyRenderingAttributes Methods
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
		case VL_IGNOREVERTEXCOLORS:
		    obj = getIgnoreVertexColors(obj);
		    break;

		case VL_VISIBLE:
		    obj = getVisible(obj);
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
		    case VL_IGNOREVERTEXCOLORS:
			setIgnoreVertexColors(obj);
			break;

		    case VL_VISIBLE:
			setVisible(obj);
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
    buildRenderingAttributes(RenderingAttributes attributes) {

	if ((this.attributes = attributes) == null) {
	    this.attributes = new RenderingAttributes();
	    peer = this.attributes;
	    setField(NL_DEFAULTCAPABILITY);
	    setField(NL_IGNOREVERTEXCOLORS);
	    setField(NL_VISIBLE);
	}
	setField(NL_CAPABILITIES);
    }


    private YoixObject
    getIgnoreVertexColors(YoixObject obj) {

	return(YoixObject.newInt(attributes.getIgnoreVertexColors()));
    }


    private YoixObject
    getVisible(YoixObject obj) {

	return(YoixObject.newInt(attributes.getVisible()));
    }


    private void
    setIgnoreVertexColors(YoixObject obj) {

	attributes.setIgnoreVertexColors(obj.booleanValue());
    }


    private void
    setVisible(YoixObject obj) {

	attributes.setVisible(obj.booleanValue());
    }
}

