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
import javax.media.j3d.Material;
import javax.media.j3d.RestrictedAccessException;
import javax.vecmath.Color3f;
import att.research.yoix.*;

class BodyMaterial extends BodyNodeComponent

    implements Constants

{

    //
    // Decided to keep our own copy of peer to eliminate some casting.
    //

    private Material  material = null;

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

    private static HashMap  activefields = new HashMap(15);

    static {
	activefields.put(NL_AMBIENTCOLOR, new Integer(VL_AMBIENTCOLOR));
	activefields.put(NL_CAPABILITIES, new Integer(VL_CAPABILITIES));
	activefields.put(NL_COLORTARGET, new Integer(VL_COLORTARGET));
	activefields.put(NL_COMPILED, new Integer(VL_COMPILED));
	activefields.put(NL_DEFAULTCAPABILITY, new Integer(VL_DEFAULTCAPABILITY));
	activefields.put(NL_DIFFUSECOLOR, new Integer(VL_DIFFUSECOLOR));
	activefields.put(NL_EMISSIVECOLOR, new Integer(VL_EMISSIVECOLOR));
	activefields.put(NL_ENABLED, new Integer(VL_ENABLED));
	activefields.put(NL_LIVE, new Integer(VL_LIVE));
	activefields.put(NL_PATH, new Integer(VL_PATH));
	activefields.put(NL_SHININESS, new Integer(VL_SHININESS));
	activefields.put(NL_SPECULARCOLOR, new Integer(VL_SPECULARCOLOR));
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
     // NAME                        CAPABILITY                                     VALUE
     // ----                        ----------                                     -----
	"ALLOW_COMPONENT_READ",     new Integer(Material.ALLOW_COMPONENT_READ),    null,
	"ALLOW_COMPONENT_WRITE",    new Integer(Material.ALLOW_COMPONENT_WRITE),   null,
    };

    static {
	loadCapabilities(capabilities, BodyMaterial.class);
	capabilities = null;
    }

    //
    // Default values...
    //

    private static Color3f  DEFAULT_AMBIENT_COLOR = new Color3f(0.2f, 0.2f, 0.2f);
    private static Color3f  DEFAULT_DIFFUSE_COLOR = COLOR3F_WHITE;
    private static Color3f  DEFAULT_EMISSIVE_COLOR = COLOR3F_BLACK;
    private static Color3f  DEFAULT_SPECULAR_COLOR = COLOR3F_WHITE;
    private static String   DEFAULT_COLOR_TARGET_NAME = NL_DIFFUSECOLOR;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyMaterial(J3DObject data) {

	this(null, data);
    }


    BodyMaterial(Material material) {

	this(material, (J3DObject)VM.getTypeTemplate(T_MATERIAL));
    }


    private
    BodyMaterial(Material material, J3DObject data) {

	super(material, data);
	buildMaterial(material);
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

	return(MATERIAL);
    }

    ///////////////////////////////////
    //
    // BodyMaterial Methods
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

	material = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	int  field;

	try {
	    switch (field = activeField(name, activefields)) {
		case VL_AMBIENTCOLOR:
		    obj = getAmbientColor(obj);
		    break;

		case VL_COLORTARGET:
		    obj = getColorTarget(obj);
		    break;

		case VL_DIFFUSECOLOR:
		    obj = getDiffuseColor(obj);
		    break;

		case VL_EMISSIVECOLOR:
		    obj = getEmissiveColor(obj);
		    break;

		case VL_ENABLED:
		    obj = getEnabled(obj);
		    break;

		case VL_SHININESS:
		    obj = getShininess(obj);
		    break;

		case VL_SPECULARCOLOR:
		    obj = getSpecularColor(obj);
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
		    case VL_AMBIENTCOLOR:
			setAmbientColor(obj);
			break;

		    case VL_COLORTARGET:
			setColorTarget(obj);
			break;

		    case VL_DIFFUSECOLOR:
			setDiffuseColor(obj);
			break;

		    case VL_EMISSIVECOLOR:
			setEmissiveColor(obj);
			break;

		    case VL_ENABLED:
			setEnabled(obj);
			break;

		    case VL_SHININESS:
			setShininess(obj);
			break;

		    case VL_SPECULARCOLOR:
			setSpecularColor(obj);
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
    buildMaterial(Material material) {

	if ((this.material = material) == null) {
	    this.material = new Material();
	    peer = this.material;
	    setField(NL_DEFAULTCAPABILITY);
	    setField(NL_AMBIENTCOLOR);
	    setField(NL_DIFFUSECOLOR);
	    setField(NL_EMISSIVECOLOR);
	    setField(NL_SPECULARCOLOR);
	    setField(NL_SHININESS);
	    setField(NL_COLORTARGET);
	    setField(NL_ENABLED);
	}
	setField(NL_CAPABILITIES);
    }


    private YoixObject
    getAmbientColor(YoixObject obj) {

	Color3f  color;

	color = new Color3f();
	material.getAmbientColor(color);
	return(Make.yoixColor(color));
    }


    private YoixObject
    getColorTarget(YoixObject obj) {

	return(J3DMake.yoixConstant("ColorTarget", material.getColorTarget()));
    }


    private YoixObject
    getDiffuseColor(YoixObject obj) {

	Color3f  color;

	color = new Color3f();
	material.getDiffuseColor(color);
	return(Make.yoixColor(color));
    }


    private YoixObject
    getEmissiveColor(YoixObject obj) {

	Color3f  color;

	color = new Color3f();
	material.getEmissiveColor(color);
	return(Make.yoixColor(color));
    }


    private YoixObject
    getEnabled(YoixObject obj) {

	return(YoixObject.newInt(material.getLightingEnable()));
    }


    private YoixObject
    getShininess(YoixObject obj) {

	return(YoixObject.newDouble((material.getShininess() - 1)/127.0));
    }


    private YoixObject
    getSpecularColor(YoixObject obj) {

	Color3f  color;

	color = new Color3f();
	material.getSpecularColor(color);
	return(Make.yoixColor(color));
    }


    private void
    setAmbientColor(YoixObject obj) {

	material.setAmbientColor(Make.javaColor3f(obj, DEFAULT_AMBIENT_COLOR));
    }


    private void
    setColorTarget(YoixObject obj) {

	material.setColorTarget(J3DMake.javaInt("ColorTarget", obj));
    }


    private void
    setDiffuseColor(YoixObject obj) {

	material.setDiffuseColor(Make.javaColor3f(obj, DEFAULT_DIFFUSE_COLOR));
    }


    private void
    setEmissiveColor(YoixObject obj) {

	material.setEmissiveColor(Make.javaColor3f(obj, DEFAULT_EMISSIVE_COLOR));
    }


    private void
    setEnabled(YoixObject obj) {

	material.setLightingEnable(obj.booleanValue());
    }


    private void
    setShininess(YoixObject obj) {

	material.setShininess(127*obj.floatValue() + 1);
    }


    private void
    setSpecularColor(YoixObject obj) {

	material.setSpecularColor(Make.javaColor3f(obj, DEFAULT_SPECULAR_COLOR));
    }
}

