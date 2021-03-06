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
import javax.media.j3d.LineAttributes;
import javax.media.j3d.RestrictedAccessException;
import att.research.yoix.*;

class BodyLineAttributes extends BodyNodeComponent

    implements Constants

{

    //
    // Decided to keep our own copy of peer to eliminate some casting.
    //

    private LineAttributes  attributes = null;

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
	activefields.put(NL_ANTIALIASING, new Integer(VL_ANTIALIASING));
	activefields.put(NL_CAPABILITIES, new Integer(VL_CAPABILITIES));
	activefields.put(NL_COMPILED, new Integer(VL_COMPILED));
	activefields.put(NL_DEFAULTCAPABILITY, new Integer(VL_DEFAULTCAPABILITY));
	activefields.put(NL_LIVE, new Integer(VL_LIVE));
	activefields.put(NL_PATH, new Integer(VL_PATH));
	//
	// There will be more, but I'm not particulary happy with the
	// way Java3D defines line patterns!! For example, are pixels
	// the right approach? Also seems silly that the pattern mask
	// is an int but they only use 16 bits - more late
	//
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
     // NAME                          CAPABILITY                                             VALUE
     // ----                          ----------                                             -----
	"ALLOW_ANTIALIASING_READ",    new Integer(LineAttributes.ALLOW_ANTIALIASING_READ),   null,
	"ALLOW_ANTIALIASING_WRITE",   new Integer(LineAttributes.ALLOW_ANTIALIASING_WRITE),  null,
    };

    static {
	loadCapabilities(capabilities, BodyLineAttributes.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyLineAttributes(J3DObject data) {

	this(null, data);
    }


    BodyLineAttributes(LineAttributes attributes) {

	this(attributes, (J3DObject)VM.getTypeTemplate(T_LINEATTRIBUTES));
    }


    private
    BodyLineAttributes(LineAttributes attributes, J3DObject data) {

	super(attributes, data);
	buildLineAttributes(attributes);
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

	return(LINEATTRIBUTES);
    }

    ///////////////////////////////////
    //
    // BodyLineAttributes Methods
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
		case VL_ANTIALIASING:
		    obj = getAntiAliasing(obj);
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
		    case VL_ANTIALIASING:
			setAntiAliasing(obj);
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
    buildLineAttributes(LineAttributes attributes) {

	if ((this.attributes = attributes) == null) {
	    this.attributes = new LineAttributes();
	    peer = this.attributes;
	    setField(NL_DEFAULTCAPABILITY);
	    setField(NL_ANTIALIASING);
	}
	setField(NL_CAPABILITIES);
    }


    private YoixObject
    getAntiAliasing(YoixObject obj) {

	return(YoixObject.newInt(attributes.getLineAntialiasingEnable()));
    }


    private void
    setAntiAliasing(YoixObject obj) {

	attributes.setLineAntialiasingEnable(obj.booleanValue());
    }
}

