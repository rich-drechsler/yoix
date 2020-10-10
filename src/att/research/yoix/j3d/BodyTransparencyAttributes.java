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
import javax.media.j3d.TransparencyAttributes;
import att.research.yoix.*;

class BodyTransparencyAttributes extends BodyNodeComponent

    implements Constants

{

    //
    // Decided to keep our own copy of peer to eliminate some casting.
    //

    private TransparencyAttributes  attributes = null;

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
	activefields.put(NL_DESTINATIONBLEND, new Integer(VL_DESTINATIONBLEND));
	activefields.put(NL_LIVE, new Integer(VL_LIVE));
	activefields.put(NL_MODE, new Integer(VL_MODE));
	activefields.put(NL_PATH, new Integer(VL_PATH));
	activefields.put(NL_SOURCEBLEND, new Integer(VL_SOURCEBLEND));
	activefields.put(NL_VALUE, new Integer(VL_VALUE));
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
     // NAME                           CAPABILITY                                                       VALUE
     // ----                           ----------                                                       -----
	"ALLOW_BLEND_FUNCTION_READ",   new Integer(TransparencyAttributes.ALLOW_BLEND_FUNCTION_READ),   null,
	"ALLOW_BLEND_FUNCTION_WRITE",  new Integer(TransparencyAttributes.ALLOW_BLEND_FUNCTION_WRITE),  null,
	"ALLOW_MODE_READ",             new Integer(TransparencyAttributes.ALLOW_MODE_READ),             null,
	"ALLOW_MODE_WRITE",            new Integer(TransparencyAttributes.ALLOW_MODE_WRITE),            null,
	"ALLOW_VALUE_READ",            new Integer(TransparencyAttributes.ALLOW_VALUE_READ),            null,
	"ALLOW_VALUE_WRITE",           new Integer(TransparencyAttributes.ALLOW_VALUE_WRITE),           null,
    };

    static {
	loadCapabilities(capabilities, BodyTransparencyAttributes.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyTransparencyAttributes(J3DObject data) {

	this(null, data);
    }


    BodyTransparencyAttributes(TransparencyAttributes attributes) {

	this(attributes, (J3DObject)VM.getTypeTemplate(T_TRANSPARENCYATTRIBUTES));
    }


    private
    BodyTransparencyAttributes(TransparencyAttributes attributes, J3DObject data) {

	super(attributes, data);
	buildTransparencyAttributes(attributes);
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

	return(TRANSPARENCYATTRIBUTES);
    }

    ///////////////////////////////////
    //
    // BodyTransparencyAttributes Methods
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
		case VL_DESTINATIONBLEND:
		    obj = getDestinationBlend(obj);
		    break;

		case VL_MODE:
		    obj = getMode(obj);
		    break;

		case VL_SOURCEBLEND:
		    obj = getSourceBlend(obj);
		    break;

		case VL_VALUE:
		    obj = getValue(obj);
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
		    case VL_DESTINATIONBLEND:
			setDestinationBlend(obj);
			break;

		    case VL_MODE:
			setMode(obj);
			break;

		    case VL_SOURCEBLEND:
			setSourceBlend(obj);
			break;

		    case VL_VALUE:
			setValue(obj);
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
    buildTransparencyAttributes(TransparencyAttributes attributes) {

	if ((this.attributes = attributes) == null) {
	    this.attributes = new TransparencyAttributes();
	    peer = this.attributes;
	    setField(NL_DEFAULTCAPABILITY);
	    setField(NL_MODE);
	    setField(NL_DESTINATIONBLEND);
	    setField(NL_SOURCEBLEND);
	    setField(NL_VALUE);
	}
	setField(NL_CAPABILITIES);
    }


    private YoixObject
    getDestinationBlend(YoixObject obj) {

	return(J3DMake.yoixConstant("BlendFunction", attributes.getSrcBlendFunction()));
    }


    private YoixObject
    getMode(YoixObject obj) {

	return(J3DMake.yoixConstant("TransparencyMode", attributes.getTransparencyMode()));
    }


    private YoixObject
    getSourceBlend(YoixObject obj) {

	return(J3DMake.yoixConstant("BlendFunction", attributes.getDstBlendFunction()));
    }


    private YoixObject
    getValue(YoixObject obj) {

	return(YoixObject.newDouble(attributes.getTransparency()));
    }


    private void
    setDestinationBlend(YoixObject obj) {

	attributes.setDstBlendFunction(J3DMake.javaInt("BlendFunction", obj));
    }


    private void
    setMode(YoixObject obj) {

	attributes.setTransparencyMode(J3DMake.javaInt("TransparencyMode", obj));
    }


    private void
    setSourceBlend(YoixObject obj) {

	attributes.setSrcBlendFunction(J3DMake.javaInt("BlendFunction", obj));
    }


    private void
    setValue(YoixObject obj) {

	attributes.setTransparency(Math.max(0.0f, Math.min(obj.floatValue(), 1.0f)));
    }
}

