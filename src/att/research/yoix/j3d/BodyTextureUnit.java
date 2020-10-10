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
import javax.media.j3d.NodeComponent;
import javax.media.j3d.RestrictedAccessException;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.TextureUnitState;
import javax.media.j3d.Transform3D;
import att.research.yoix.*;

class BodyTextureUnit extends BodyNodeComponent

    implements Constants

{

    //
    // Decided to keep our own copy of peer to eliminate some casting.
    //

    private TextureUnitState  textureunit = null;

    //
    // Flag that tells us we're pretty much finished with initialization.
    //

    private boolean  initialized = false;

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
	activefields.put(NL_CAPABILITIES, new Integer(VL_CAPABILITIES));
	activefields.put(NL_COMBINERGBMODE, new Integer(VL_COMBINERGBMODE));
	activefields.put(NL_COMBINEALPHAMODE, new Integer(VL_COMBINEALPHAMODE));
	activefields.put(NL_COMPILED, new Integer(VL_COMPILED));
	activefields.put(NL_DEFAULTCAPABILITY, new Integer(VL_DEFAULTCAPABILITY));
	activefields.put(NL_ENABLED, new Integer(VL_ENABLED));
	activefields.put(NL_GENERATIONMODE, new Integer(VL_GENERATIONMODE));
	activefields.put(NL_LIVE, new Integer(VL_LIVE));
	activefields.put(NL_PATH, new Integer(VL_PATH));
	activefields.put(NL_TEXTURE, new Integer(VL_TEXTURE));
	activefields.put(NL_TEXTUREMODE, new Integer(VL_TEXTUREMODE));
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
    // Another one that's a not quite standard and that probably also
    // needs to be able to put an array of field names, rather than a
    // single name, in the table.
    //

    private static Object  capabilities[] = {
     //
     // NAME                    CAPABILITY                                           VALUE
     // ----                    ----------                                           -----
	"ALLOW_STATE_READ",     new Integer(TextureUnitState.ALLOW_STATE_READ),      null,
	"ALLOW_STATE_WRITE",    new Integer(TextureUnitState.ALLOW_STATE_WRITE),     null,
    };

    static {
	loadCapabilities(capabilities, BodyTextureUnit.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyTextureUnit(J3DObject data) {

	this(null, data);
    }


    BodyTextureUnit(TextureUnitState attributes) {

	this(attributes, (J3DObject)VM.getTypeTemplate(T_TEXTUREUNIT));
    }


    private
    BodyTextureUnit(TextureUnitState attributes, J3DObject data) {

	super(attributes, data);
	buildTextureUnit(attributes);
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

	return(TEXTUREUNIT);
    }

    ///////////////////////////////////
    //
    // BodyTextureUnit Methods
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

	textureunit = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	int  field;

	try {
	    switch (field = activeField(name, activefields)) {
		case VL_COMBINEALPHAMODE:
		    obj = getCombineAlphaMode(obj);
		    break;

		case VL_COMBINERGBMODE:
		    obj = getCombineRGBMode(obj);
		    break;

		case VL_ENABLED:
		    obj = getEnabled(obj);
		    break;

		case VL_GENERATIONMODE:
		    obj = getGenerationMode(obj);
		    break;

		case VL_TEXTURE:
		    obj = getTexture(obj);
		    break;

		case VL_TEXTUREMODE:
		    obj = getTextureMode(obj);
		    break;

		case VL_TRANSFORM:
		    obj = getTransform(obj);
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
		    case VL_COMBINEALPHAMODE:
			setCombineAlphaMode(obj);
			break;

		    case VL_COMBINERGBMODE:
			setCombineRGBMode(obj);
			break;

		    case VL_ENABLED:
			setEnabled(obj);
			break;

		    case VL_GENERATIONMODE:
			setGenerationMode(obj);
			break;

		    case VL_TEXTURE:
			setTexture(obj);
			break;

		    case VL_TEXTUREMODE:
			setTextureMode(obj);
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

	NodeComponent  component;
	boolean        readable;
	boolean        writable;

	updateDefaultCapabilities(NL_TEXTURE);

	readable = textureunit.getCapability(TextureUnitState.ALLOW_STATE_READ);
	writable = textureunit.getCapability(TextureUnitState.ALLOW_STATE_WRITE);
	if ((component = textureunit.getTexCoordGeneration()) != null) {
	    changeCapability(component, TexCoordGeneration.ALLOW_ENABLE_READ, readable);
	    changeCapability(component, TexCoordGeneration.ALLOW_FORMAT_READ, readable);
	    changeCapability(component, TexCoordGeneration.ALLOW_MODE_READ, readable);
	    changeCapability(component, TexCoordGeneration.ALLOW_PLANE_READ, readable);
	    changeCapability(component, TexCoordGeneration.ALLOW_ENABLE_WRITE, writable);
	    changeCapability(component, TexCoordGeneration.ALLOW_PLANE_WRITE, writable);
	}
	if ((component = textureunit.getTextureAttributes()) != null) {
	    changeCapability(component, TextureAttributes.ALLOW_MODE_READ, readable);
	    changeCapability(component, TextureAttributes.ALLOW_COMBINE_READ, readable);
	    changeCapability(component, TextureAttributes.ALLOW_TRANSFORM_READ, readable);
	    changeCapability(component, TextureAttributes.ALLOW_MODE_WRITE, writable);
	    changeCapability(component, TextureAttributes.ALLOW_COMBINE_WRITE, writable);
	    changeCapability(component, TextureAttributes.ALLOW_TRANSFORM_WRITE, writable);
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildTextureUnit(TextureUnitState textureunit) {

	if ((this.textureunit = textureunit) == null) {
	    this.textureunit = new TextureUnitState(
		null,
		new TextureAttributes(),
		new TexCoordGeneration()
	    );
	    peer = this.textureunit;
	    setField(NL_DEFAULTCAPABILITY);
	    setField(NL_TEXTURE);
	    setField(NL_GENERATIONMODE);
	    setField(NL_ENABLED);
	}
	setField(NL_CAPABILITIES);
	initialized = true;
    }


    private YoixObject
    getCombineAlphaMode(YoixObject obj) {

	TextureAttributes  attributes;

	if ((attributes = textureunit.getTextureAttributes()) != null)
	    obj = J3DMake.yoixConstant("TextureCombineMode", attributes.getCombineAlphaMode());
	else obj = J3DMake.yoixConstant("TextureCombineMode");
	return(obj);
    }


    private YoixObject
    getCombineRGBMode(YoixObject obj) {

	TextureAttributes  attributes;

	if ((attributes = textureunit.getTextureAttributes()) != null)
	    obj = J3DMake.yoixConstant("TextureCombineMode", attributes.getCombineRgbMode());
	else obj = J3DMake.yoixConstant("TextureCombineMode");
	return(obj);
    }


    private YoixObject
    getEnabled(YoixObject obj) {

	TexCoordGeneration  coord;
	Texture             texture;
	boolean             result = false;

	//
	// This is questionable - TexCoordGeneration and Texture can both
	// be enabled, so what do we do here??
	//

	if ((coord = textureunit.getTexCoordGeneration()) != null)
	    result = coord.getEnable();
	else result = false;
	return(YoixObject.newInt(result));
    }


    private YoixObject
    getGenerationMode(YoixObject obj) {

	TexCoordGeneration  coord;

	if ((coord = textureunit.getTexCoordGeneration()) != null)
	    obj = J3DMake.yoixConstant("TextureGenerationMode", coord.getGenMode());
	else obj = J3DMake.yoixConstant("TextureGenerationMode");
	return(obj);
    }


    private YoixObject
    getTexture(YoixObject obj) {

	return(J3DObject.newTexture(textureunit.getTexture()));
    }


    private YoixObject
    getTextureMode(YoixObject obj) {

	TextureAttributes  attributes;

	if ((attributes = textureunit.getTextureAttributes()) != null)
	    obj = J3DMake.yoixConstant("TextureMode", attributes.getTextureMode());
	else obj = J3DMake.yoixConstant("TextureMode");
	return(obj);
    }


    private YoixObject
    getTransform(YoixObject obj) {

	TextureAttributes  attributes;
	Transform3D        transform;

	if ((attributes = textureunit.getTextureAttributes()) != null) {
	    transform = new Transform3D();
	    attributes.getTextureTransform(transform);
	} else transform = null;
	return(J3DObject.newTransform3D(transform));
    }


    private void
    setCombineAlphaMode(YoixObject obj) {

	TextureAttributes  attributes;

	if ((attributes = textureunit.getTextureAttributes()) != null)
	    attributes.setCombineAlphaMode(J3DMake.javaInt("TextureCombineMode", obj));
    }


    private void
    setCombineRGBMode(YoixObject obj) {

	TextureAttributes  attributes;

	if ((attributes = textureunit.getTextureAttributes()) != null)
	    attributes.setCombineRgbMode(J3DMake.javaInt("TextureCombineMode", obj));
    }


    private void
    setEnabled(YoixObject obj) {

	TexCoordGeneration  coord;

	//
	// This is questionable - TexCoordGeneration and Texture can both
	// be enabled, so what do we do here??
	//

	if ((coord = textureunit.getTexCoordGeneration()) != null)
	    coord.setEnable(obj.booleanValue());
    }


    private void
    setGenerationMode(YoixObject obj) {

	TexCoordGeneration  coord;

	if ((coord = textureunit.getTexCoordGeneration()) != null)
	    coord.setGenMode(J3DMake.javaInt("TextureGenerationMode", obj));
    }


    private void
    setTexture(YoixObject obj) {

	if (J3DObject.isTexture(obj) || obj.isNull()) {
	    if (obj.notNull())
		textureunit.setTexture(((J3DObject)obj).getManagedTexture());
	    else textureunit.setTexture(null);
	} else VM.abort(TYPECHECK, NL_TEXTURE);
    }


    private void
    setTextureMode(YoixObject obj) {

	TextureAttributes  attributes;

	if ((attributes = textureunit.getTextureAttributes()) != null)
	    attributes.setTextureMode(J3DMake.javaInt("TextureMode", obj));
    }


    private void
    setTransform(YoixObject obj) {

	TextureAttributes  attributes;

	if ((attributes = textureunit.getTextureAttributes()) != null) {
	    if (obj.notNull())
		attributes.setTextureTransform(((J3DObject)obj).getManagedTransform());
	    if (initialized)
		attributes.setTextureTransform(new Transform3D());
	}
    }
}

