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
import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.RestrictedAccessException;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureUnitState;
import javax.vecmath.Color3f;
import att.research.yoix.*;

class BodyAppearance extends BodyNodeComponent

    implements Constants

{

    //
    // Decided to keep our own copy of peer to eliminate some casting.
    //

    private Appearance  appearance = null;

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

    private static HashMap  activefields = new HashMap(20);

    static {
	activefields.put(NL_CAPABILITIES, new Integer(VL_CAPABILITIES));
	activefields.put(NL_COLORING, new Integer(VL_COLORING));
	activefields.put(NL_COMPILED, new Integer(VL_COMPILED));
	activefields.put(NL_DEFAULTCAPABILITY, new Integer(VL_DEFAULTCAPABILITY));
	activefields.put(NL_LINES, new Integer(VL_LINES));
	activefields.put(NL_LIVE, new Integer(VL_LIVE));
	activefields.put(NL_MATERIAL, new Integer(VL_MATERIAL));
	activefields.put(NL_PATH, new Integer(VL_PATH));
	activefields.put(NL_POINTS, new Integer(VL_POINTS));
	activefields.put(NL_POLYGONS, new Integer(VL_POLYGONS));
	activefields.put(NL_RENDERING, new Integer(VL_RENDERING));
	activefields.put(NL_TEXTUREUNIT, new Integer(VL_TEXTUREUNIT));
	activefields.put(NL_TRANSPARENCY, new Integer(VL_TRANSPARENCY));
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
     // NAME                                    CAPABILITY                                                    VALUE
     // ----                                    ----------                                                    -----
	"ALLOW_COLORING_ATTRIBUTES_READ",       new Integer(Appearance.ALLOW_COLORING_ATTRIBUTES_READ),       null,
	"ALLOW_COLORING_ATTRIBUTES_WRITE",      new Integer(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE),      null,
	"ALLOW_MATERIAL_READ",                  new Integer(Appearance.ALLOW_MATERIAL_READ),                  null,
	"ALLOW_MATERIAL_WRITE",                 new Integer(Appearance.ALLOW_MATERIAL_WRITE),                 null,
	"ALLOW_POINT_ATTRIBUTES_READ",          new Integer(Appearance.ALLOW_POINT_ATTRIBUTES_READ),          null,
	"ALLOW_POINT_ATTRIBUTES_WRITE",         new Integer(Appearance.ALLOW_POINT_ATTRIBUTES_WRITE),         null,
	"ALLOW_LINE_ATTRIBUTES_READ",           new Integer(Appearance.ALLOW_LINE_ATTRIBUTES_READ),           null,
	"ALLOW_LINE_ATTRIBUTES_WRITE",          new Integer(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE),          null,
	"ALLOW_POLYGON_ATTRIBUTES_READ",        new Integer(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ),        null,
	"ALLOW_POLYGON_ATTRIBUTES_WRITE",       new Integer(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE),       null,
	"ALLOW_RENDERING_ATTRIBUTES_READ",      new Integer(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ),      null,
	"ALLOW_RENDERING_ATTRIBUTES_WRITE",     new Integer(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE),     null,
	"ALLOW_TRANSPARENCY_ATTRIBUTES_READ",   new Integer(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ),   null,
	"ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE",  new Integer(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE),  null,
	"ALLOW_TEXGEN_READ",                    new Integer(Appearance.ALLOW_TEXGEN_READ),                    null,
	"ALLOW_TEXGEN_WRITE",                   new Integer(Appearance.ALLOW_TEXGEN_WRITE),                   null,
	"ALLOW_TEXTURE_ATTRIBUTES_READ",        new Integer(Appearance.ALLOW_TEXTURE_ATTRIBUTES_READ),        null,
	"ALLOW_TEXTURE_ATTRIBUTES_WRITE",       new Integer(Appearance.ALLOW_TEXTURE_ATTRIBUTES_WRITE),       null,
	"ALLOW_TEXTURE_READ",                   new Integer(Appearance.ALLOW_TEXTURE_READ),                   null,
	"ALLOW_TEXTURE_WRITE",                  new Integer(Appearance.ALLOW_TEXTURE_WRITE),                  null,
	"ALLOW_TEXTURE_UNIT_STATE_READ",        new Integer(Appearance.ALLOW_TEXTURE_UNIT_STATE_READ),        null,
	"ALLOW_TEXTURE_UNIT_STATE_WRITE",       new Integer(Appearance.ALLOW_TEXTURE_UNIT_STATE_WRITE),       null,
    };

    static {
	loadCapabilities(capabilities, BodyAppearance.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyAppearance(J3DObject data) {

	this(null, data);
    }


    BodyAppearance(Appearance appearance) {

	this(appearance, (J3DObject)VM.getTypeTemplate(T_APPEARANCE));
    }


    private
    BodyAppearance(Appearance appearance, J3DObject data) {

	super(appearance, data);
	buildAppearance(appearance);
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

	return(APPEARANCE);
    }

    ///////////////////////////////////
    //
    // BodyAppearance Methods
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

	appearance = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	int  field;

	try {
	    switch (field = activeField(name, activefields)) {
		case VL_COLORING:
		    obj = getColoring(obj);
		    break;

		case VL_LINES:
		    obj = getLines(obj);
		    break;

		case VL_MATERIAL:
		    obj = getMaterial(obj);
		    break;

		case VL_POINTS:
		    obj = getPoints(obj);
		    break;

		case VL_POLYGONS:
		    obj = getPolygons(obj);
		    break;

		case VL_RENDERING:
		    obj = getRendering(obj);
		    break;

		case VL_TEXTUREUNIT:
		    obj = getTextureUnit(obj);
		    break;

		case VL_TRANSPARENCY:
		    obj = getTransparency(obj);
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
		    case VL_COLORING:
			setColoring(obj);
			break;

		    case VL_LINES:
			setLines(obj);
			break;

		    case VL_MATERIAL:
			setMaterial(obj);
			break;

		    case VL_POINTS:
			setPoints(obj);
			break;

		    case VL_POLYGONS:
			setPolygons(obj);
			break;

		    case VL_RENDERING:
			setRendering(obj);
			break;

		    case VL_TEXTUREUNIT:
			setTextureUnit(obj);
			break;

		    case VL_TRANSPARENCY:
			setTransparency(obj);
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

	updateDefaultCapabilities(
	    new String[] {
		NL_COLORING,
		NL_LINES,
		NL_MATERIAL,
		NL_POINTS,
		NL_POLYGONS,
		NL_RENDERING,
		NL_TEXTUREUNIT,
		NL_TRANSPARENCY
	    }
	);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildAppearance(Appearance appearance) {

	if ((this.appearance = appearance) == null) {
	    this.appearance = new Appearance();
	    this.appearance.setColoringAttributes(new ColoringAttributes());
	    peer = this.appearance;

	    setField(NL_DEFAULTCAPABILITY);
	    setField(NL_COLORING);
	    setField(NL_LINES);
	    setField(NL_MATERIAL);
	    setField(NL_POINTS);
	    setField(NL_POLYGONS);
	    setField(NL_RENDERING);
	    setField(NL_TEXTUREUNIT);
	    setField(NL_TRANSPARENCY);
	}
	setField(NL_CAPABILITIES);
    }


    private YoixObject
    getColoring(YoixObject obj) {

	return(J3DObject.newColoringAttributes(appearance.getColoringAttributes()));
    }


    private YoixObject
    getLines(YoixObject obj) {

	return(J3DObject.newLineAttributes(appearance.getLineAttributes()));
    }


    private YoixObject
    getMaterial(YoixObject obj) {

	return(J3DObject.newMaterial(appearance.getMaterial()));
    }


    private YoixObject
    getPoints(YoixObject obj) {

	return(J3DObject.newPointAttributes(appearance.getPointAttributes()));
    }


    private YoixObject
    getPolygons(YoixObject obj) {

	return(J3DObject.newPolygonAttributes(appearance.getPolygonAttributes()));
    }


    private YoixObject
    getRendering(YoixObject obj) {

	return(J3DObject.newRenderingAttributes(appearance.getRenderingAttributes()));
    }


    private YoixObject
    getTextureUnit(YoixObject obj) {

	TextureUnitState  units[];
	TextureUnitState  textureunit;
	Texture           texture = null;

	//
	// Harder than you might expect because Java insists that we don't
	// use a TextureUnitState and a Texture in the same Appearance. Not
	// a problem if this was created via a Yoix description, but that
	// isn't necessarily true if this was loaded by a SceneLoader. An
	// alternative approach could automatically move Texture and the
	// other related fields into a TextureUnitState - maybe later.
	//
	// NOTE - we Currently just returning base level textureunit, but
	// that eventually should change.
	//

	if ((units = appearance.getTextureUnitState()) == null) {
	    if ((texture = appearance.getTexture()) != null) {
		textureunit = new TextureUnitState(
		    texture,
		    appearance.getTextureAttributes(),
		    appearance.getTexCoordGeneration()
		);
	    } else textureunit = null;
	} else {
	    //
	    // Eventually will have to do more...
	    //
	    textureunit = units.length > 0 ? units[0] : null;
	}

	return(J3DObject.newTextureUnit(textureunit));
    }


    private YoixObject
    getTransparency(YoixObject obj) {

	return(J3DObject.newTransparencyAttributes(appearance.getTransparencyAttributes()));
    }


    private void
    setColoring(YoixObject obj) {

	if (J3DObject.isColoringAttributes(obj) || obj.isNull()) {
	    if (obj.notNull())
		appearance.setColoringAttributes(((J3DObject)obj).getManagedColoringAttributes());
	    else appearance.setColoringAttributes(null);
	} else VM.abort(TYPECHECK, NL_COLORING);	// probably impossible
    }


    private void
    setLines(YoixObject obj) {

	if (J3DObject.isLineAttributes(obj) || obj.isNull()) {
	    if (obj.notNull())
		appearance.setLineAttributes(((J3DObject)obj).getManagedLineAttributes());
	    else appearance.setLineAttributes(null);
	} else VM.abort(TYPECHECK, NL_LINES);	// probably impossible
    }


    private void
    setMaterial(YoixObject obj) {

	if (J3DObject.isMaterial(obj) || obj.isNull()) {
	    if (obj.notNull())
		appearance.setMaterial(((J3DObject)obj).getManagedMaterial());
	    else appearance.setMaterial(null);
	} else VM.abort(TYPECHECK, NL_MATERIAL);	// probably impossible
    }


    private void
    setPoints(YoixObject obj) {

	if (J3DObject.isPointAttributes(obj) || obj.isNull()) {
	    if (obj.notNull())
		appearance.setPointAttributes(((J3DObject)obj).getManagedPointAttributes());
	    else appearance.setPointAttributes(null);
	} else VM.abort(TYPECHECK, NL_POINTS);	// probably impossible
    }


    private void
    setPolygons(YoixObject obj) {

	if (J3DObject.isPolygonAttributes(obj) || obj.isNull()) {
	    if (obj.notNull())
		appearance.setPolygonAttributes(((J3DObject)obj).getManagedPolygonAttributes());
	    else appearance.setPolygonAttributes(null);
	} else VM.abort(TYPECHECK, NL_POLYGONS);	// probably impossible
    }


    private void
    setRendering(YoixObject obj) {

	if (J3DObject.isRenderingAttributes(obj) || obj.isNull()) {
	    if (obj.notNull())
		appearance.setRenderingAttributes(((J3DObject)obj).getManagedRenderingAttributes());
	    else appearance.setRenderingAttributes(null);
	} else VM.abort(TYPECHECK, NL_RENDERING);	// probably impossible
    }


    private void
    setTextureUnit(YoixObject obj) {

	TexCoordGeneration  texcoord = null;
	TextureAttributes   attributes = null;
	TextureUnitState    units[] = null;
	Texture             texture = null;

	//
	// Harder than you might expect because Java insists that we don't
	// use a TextureUnitState and a Texture in the same Appearance. Not
	// a problem if this was created via a Yoix description, but that
	// isn't necessarily true if this was loaded by a SceneLoader. An
	// alternative approach could automatically move Texture and the
	// other related fields into a TextureUnitState - maybe later.
	//
	// NOTE - we eventually need to implement the Array case!!!
	//

	if (J3DObject.isTextureUnit(obj) || obj.isArray() || obj.isNull()) {
	    if (obj.notNull()) {
		if (obj.isArray() == false) {
		    units = new TextureUnitState[] {
			((J3DObject)obj).getManagedTextureUnit()
		    };
		} else VM.abort(UNIMPLEMENTED, NL_TEXTUREUNIT);		// later...
	    } else units = new TextureUnitState[] {null};
	    if (appearance.getTexture() != null) {
		if (units != null && units[0] != null) {
		    texture = units[0].getTexture();
		    attributes = units[0].getTextureAttributes();
		    texcoord = units[0].getTexCoordGeneration();
		}
		appearance.setTexture(texture);
		appearance.setTextureAttributes(attributes);
		appearance.setTexCoordGeneration(texcoord);
	    } else appearance.setTextureUnitState(units);
	} else VM.abort(TYPECHECK, NL_TEXTUREUNIT);
    }


    private void
    setTransparency(YoixObject obj) {

	if (J3DObject.isTransparencyAttributes(obj) || obj.isNull()) {
	    if (obj.notNull())
		appearance.setTransparencyAttributes(((J3DObject)obj).getManagedTransparencyAttributes());
	    else appearance.setTransparencyAttributes(null);
	} else VM.abort(TYPECHECK, NL_TRANSPARENCY);	// probably impossible
    }
}

