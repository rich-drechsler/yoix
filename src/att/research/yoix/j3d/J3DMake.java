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
import javax.media.j3d.Node;
import javax.media.j3d.*;
import att.research.yoix.*;

abstract
class J3DMake

    implements Constants

{

    //
    // NOTE - the format of the $table[] table has changed, so be careful.
    // The main reason for the change is that we wanted to be able to map
    // values in either direction without requiring new entries that would
    // essentially duplicate information that was already in the table. We
    // also removed the "." separator from table entries and changed how the
    // default entry is specified - see loadTable() for the details. Right
    // now the two yoixConstant() methods are the only ones that use the
    // "inverse" mappings that are available in map.
    //
    // NOTE - we changed javaInt() to only take a YoixObject as its second
    // argument, which means the individual entries can now map strings or
    // integers to Java constants. When we build the inverse mappings later
    // entries always override earlier definitions, so the order of entries
    // determines the value returned by yoixConstant(). For example, if we
    // defined
    //
    //    "Qwert", new Integer(J3D_NONE), new Integer(Qwert.NONE),
    //    "Qwert", "NONE", new Integer(Qwert.NONE),
    //
    // then the int J3D_NONE and the String "NONE" are mapped to Qwert.NONE
    // by the javaInt() method, but the yoixConstant() method would always
    // map Qwert.NONE to "NONE" because it came later in the table.
    //

    private static HashMap map;

    private static Object $table[] = {
	"Alpha", new Integer(J3D_INCREASING), new Integer(Alpha.INCREASING_ENABLE),
	"Alpha", new Integer(J3D_DECREASING), new Integer(Alpha.DECREASING_ENABLE),
	"Alpha", new Integer(J3D_COMPLETE), new Integer(Alpha.INCREASING_ENABLE|Alpha.DECREASING_ENABLE),
	"DEFAULT", null, null,

	//
	// Style of these entries means integers or case-insensitive strings
	// will be accepted and the order of the entries means the inverse
	// mapping table will return strings. We suspect it's a style that
	// will be adopted by some of the other entries in this table.
	//

	"ColorTarget", new Integer(J3D_AMBIENT), new Integer(Material.AMBIENT),
	"ColorTarget", "AMBIENT", null,
	"ColorTarget", new Integer(J3D_AMBIENT_AND_DIFFUSE), new Integer(Material.AMBIENT_AND_DIFFUSE),
	"ColorTarget", "AMBIENT_AND_DIFFUSE", null,
	"ColorTarget", new Integer(J3D_DIFFUSE), new Integer(Material.DIFFUSE),
	"ColorTarget", "DIFFUSE", null,
	"DEFAULT", null, null,
	"ColorTarget", new Integer(J3D_EMISSIVE), new Integer(Material.EMISSIVE),
	"ColorTarget", "EMISSIVE", null,
	"ColorTarget", new Integer(J3D_SPECULAR), new Integer(Material.SPECULAR),
	"ColorTarget", "SPECULAR", null,

	"Culling", new Integer(J3D_BACK), new Integer(PolygonAttributes.CULL_BACK),
	"Culling", "BACK", null,
	"DEFAULT", null, null,
	"Culling", new Integer(J3D_FRONT), new Integer(PolygonAttributes.CULL_FRONT),
	"Culling", "FRONT", null,
	"Culling", new Integer(J3D_NONE), new Integer(PolygonAttributes.CULL_NONE),
	"Culling", "NONE", null,

	"PolygonMode", new Integer(J3D_FILL), new Integer(PolygonAttributes.POLYGON_FILL),
	"PolygonMode", "FILL", null,
	"DEFAULT", null, null,
	"PolygonMode", new Integer(J3D_POINT), new Integer(PolygonAttributes.POLYGON_POINT),
	"PolygonMode", "POINT", null,
	"PolygonMode", new Integer(J3D_LINE), new Integer(PolygonAttributes.POLYGON_LINE),
	"PolygonMode", "LINE", null,

	"BlendFunction", new Integer(J3D_BLEND_ZERO), new Integer(TransparencyAttributes.BLEND_ZERO),
	"BlendFunction", new Integer(J3D_ZERO), null,
	"BlendFunction", "ZERO", null,
	"BlendFunction", new Integer(J3D_BLEND_ONE), new Integer(TransparencyAttributes.BLEND_ONE),
	"BlendFunction", new Integer(J3D_ONE), null,
	"BlendFunction", "ONE", null,
	"BlendFunction", new Integer(J3D_BLEND_SRC_ALPHA), new Integer(TransparencyAttributes.BLEND_SRC_ALPHA),
	"BlendFunction", new Integer(J3D_ALPHA), null,
	"BlendFunction", "ALPHA", null,
	"DEFAULT", null, null,
	"BlendFunction", new Integer(J3D_BLEND_ONE_MINUS_SRC_ALPHA), new Integer(TransparencyAttributes.BLEND_ONE_MINUS_SRC_ALPHA),
	"BlendFunction", new Integer(J3D_ONE_MINUS_ALPHA), null,
	"BlendFunction", "ONE_MINUS_ALPHA", null,

	"TransparencyMode", new Integer(J3D_BLENDED), new Integer(TransparencyAttributes.BLENDED),
	"TransparencyMode", "BLENDED", null,
	"TransparencyMode", new Integer(J3D_FASTEST), new Integer(TransparencyAttributes.FASTEST),
	"TransparencyMode", "FASTEST", null,
	"TransparencyMode", new Integer(J3D_NICEST), new Integer(TransparencyAttributes.NICEST),
	"TransparencyMode", "NICEST", null,
	"TransparencyMode", new Integer(J3D_NONE), new Integer(TransparencyAttributes.NONE),
	"TransparencyMode", "NONE", null,
	"DEFAULT", null, null,
	"TransparencyMode", new Integer(J3D_SCREEN_DOOR), new Integer(TransparencyAttributes.SCREEN_DOOR),
	"TransparencyMode", "SCREEN_DOOR", null,

	"ShadeModel", new Integer(J3D_FASTEST), new Integer(ColoringAttributes.FASTEST),
	"ShadeModel", "FASTEST", null,
	"ShadeModel", new Integer(J3D_GOURAUD), new Integer(ColoringAttributes.SHADE_GOURAUD),
	"ShadeModel", "GOURAUD", null,
	"DEFAULT", null, null,
	"ShadeModel", new Integer(J3D_NICEST), new Integer(ColoringAttributes.NICEST),
	"ShadeModel", "NICEST", null,
	"ShadeModel", new Integer(J3D_FLAT), new Integer(ColoringAttributes.SHADE_FLAT),
	"ShadeModel", "FLAT", null,

	"TextureFormat", new Integer(J3D_ALPHA), new Integer(Texture.ALPHA),
	"TextureFormat", "ALPHA", null,
	"TextureFormat", new Integer(J3D_INTENSITY), new Integer(Texture.INTENSITY),
	"TextureFormat", "INTENSITY", null,
	"TextureFormat", new Integer(J3D_LUMINANCE), new Integer(Texture.LUMINANCE),
	"TextureFormat", "LUMINANCE", null,
	"TextureFormat", new Integer(J3D_LUMINANCE_ALPHA), new Integer(Texture.LUMINANCE_ALPHA),
	"TextureFormat", "LUMINANCE_ALPHA", null,
	"TextureFormat", new Integer(J3D_RGB), new Integer(Texture.RGB),
	"TextureFormat", "RGB", null,
	"DEFAULT", null, null,
	"TextureFormat", new Integer(J3D_RGBA), new Integer(Texture.RGBA),
	"TextureFormat", "RGBA", null,

	"TextureBoundaryMode", new Integer(J3D_CLAMP), new Integer(Texture.CLAMP),
	"TextureBoundaryMode", "CLAMP", null,
	"TextureBoundaryMode", new Integer(J3D_CLAMP_TO_BOUNDARY), new Integer(Texture.CLAMP_TO_BOUNDARY),
	"TextureBoundaryMode", "CLAMP_TO_BOUNDARY", null,
	"TextureBoundaryMode", new Integer(J3D_CLAMP_TO_EDGE), new Integer(Texture.CLAMP_TO_EDGE),
	"TextureBoundaryMode", "CLAMP_TO_EDGE", null,
	"TextureBoundaryMode", new Integer(J3D_WRAP), new Integer(Texture.WRAP),
	"TextureBoundaryMode", "WRAP", null,
	"DEFAULT", null, null,

	"TextureMagFilter", new Integer(J3D_BASE_LEVEL_LINEAR), new Integer(Texture.BASE_LEVEL_LINEAR),
	"TextureMagFilter", "BASE_LEVEL_LINEAR", null,
	"DEFAULT", null, null,
	"TextureMagFilter", new Integer(J3D_BASE_LEVEL_POINT), new Integer(Texture.BASE_LEVEL_POINT),
	"TextureMagFilter", "BASE_LEVEL_POINT", null,
	"TextureMagFilter", new Integer(J3D_FASTEST), new Integer(Texture.FASTEST),
	"TextureMagFilter", "FASTEST", null,
	"TextureMagFilter", new Integer(J3D_FILTER4), new Integer(Texture.FILTER4),
	"TextureMagFilter", "FILTER4", null,
	"TextureMagFilter", new Integer(J3D_LINEAR_SHARPEN), new Integer(Texture.LINEAR_SHARPEN),
	"TextureMagFilter", "LINEAR_SHARPEN", null,
	"TextureMagFilter", new Integer(J3D_LINEAR_SHARPEN_RGB), new Integer(Texture.LINEAR_SHARPEN_RGB),
	"TextureMagFilter", "LINEAR_SHARPEN_RGB", null,
	"TextureMagFilter", new Integer(J3D_LINEAR_SHARPEN_ALPHA), new Integer(Texture.LINEAR_SHARPEN_ALPHA),
	"TextureMagFilter", "LINEAR_SHARPEN_ALPHA", null,
	"TextureMagFilter", new Integer(J3D_NICEST), new Integer(Texture.NICEST),
	"TextureMagFilter", "NICEST", null,

	"TextureMinFilter", new Integer(J3D_BASE_LEVEL_LINEAR), new Integer(Texture.BASE_LEVEL_LINEAR),
	"TextureMinFilter", "BASE_LEVEL_LINEAR", null,
	"DEFAULT", null, null,
	"TextureMinFilter", new Integer(J3D_BASE_LEVEL_POINT), new Integer(Texture.BASE_LEVEL_POINT),
	"TextureMinFilter", "BASE_LEVEL_POINT", null,
	"TextureMinFilter", new Integer(J3D_FASTEST), new Integer(Texture.FASTEST),
	"TextureMinFilter", "FASTEST", null,
	"TextureMinFilter", new Integer(J3D_FILTER4), new Integer(Texture.FILTER4),
	"TextureMinFilter", "FILTER4", null,
	"TextureMinFilter", new Integer(J3D_MULTI_LEVEL_POINT), new Integer(Texture.MULTI_LEVEL_POINT),
	"TextureMinFilter", "MULTI_LEVEL_POINT", null,
	"TextureMinFilter", new Integer(J3D_MULTI_LEVEL_LINEAR), new Integer(Texture.MULTI_LEVEL_LINEAR),
	"TextureMinFilter", "MULTI_LEVEL_LINEAR", null,
	"TextureMinFilter", new Integer(J3D_NICEST), new Integer(Texture.NICEST),
	"TextureMinFilter", "NICEST", null,

	"TextureLoader", new Integer(J3D_ALPHA), "ALPHA",
	"TextureLoader", "ALPHA", "ALPHA",
	"TextureLoader", new Integer(J3D_LUMINANCE), "LUMINANCE",
	"TextureLoader", "LUMINANCE", "LUMINANCE",
	"TextureLoader", new Integer(J3D_LUMINANCE_ALPHA), "LUM8_ALPHA8",
	"TextureLoader", "LUMINANCE_ALPHA", "LUM8_ALPHA8",
	"TextureLoader", "LUM8_ALPHA8", "LUM8_ALPHA8",
	"TextureLoader", "LUM4_ALPHA4", "LUM4_ALPHA4",
	"TextureLoader", new Integer(J3D_RGB), "RGB",
	"TextureLoader", "RGB", "RGB",
	"DEFAULT", null, null,
	"TextureLoader", "RGB4", "RGB4",
	"TextureLoader", "RGB5", "RGB5",
	"TextureLoader", "R3_G3_B2", "R3_G3_B2",
	"TextureLoader", new Integer(J3D_RGBA), "RGBA",
	"TextureLoader", "RGBA", "RGBA",
	"TextureLoader", "RGBA4", "RGBA4",
	"TextureLoader", "RGB5_A1", "RGB5_A1",

	//
	// Omitted two constants that only work with TextureCubeMap, which
	// we haven't implemented yet.
	//

	"TextureGenerationMode", new Integer(J3D_EYE_LINEAR), new Integer(TexCoordGeneration.EYE_LINEAR),
	"TextureGenerationMode", "EYE_LINEAR", null,
	"TextureGenerationMode", new Integer(J3D_OBJECT_LINEAR), new Integer(TexCoordGeneration.OBJECT_LINEAR),
	"TextureGenerationMode", "OBJECT_LINEAR", null,
	"DEFAULT", null, null,
	"TextureGenerationMode", new Integer(J3D_SPHERE_MAP), new Integer(TexCoordGeneration.SPHERE_MAP),
	"TextureGenerationMode", "SPHERE_MAP", null,

	"TextureMode", new Integer(J3D_MODULATE), new Integer(TextureAttributes.MODULATE),
	"TextureMode", "MODULATE", null,
	"TextureMode", new Integer(J3D_DECAL), new Integer(TextureAttributes.DECAL),
	"TextureMode", "DECAL", null,
	"TextureMode", new Integer(J3D_BLEND), new Integer(TextureAttributes.BLEND),
	"TextureMode", "BLEND", null,
	"TextureMode", new Integer(J3D_REPLACE), new Integer(TextureAttributes.REPLACE),
	"TextureMode", "REPLACE", null,
	"DEFAULT", null, null,
	"TextureMode", new Integer(J3D_COMBINE), new Integer(TextureAttributes.COMBINE),
	"TextureMode", "COMBINE", null,

	"TextureCombineMode", new Integer(J3D_REPLACE), new Integer(TextureAttributes.COMBINE_REPLACE),
	"TextureCombineMode", "REPLACE", null,
	"TextureCombineMode", new Integer(J3D_MODULATE), new Integer(TextureAttributes.COMBINE_MODULATE),
	"TextureCombineMode", "MODULATE", null,
	"DEFAULT", null, null,
	"TextureCombineMode", new Integer(J3D_ADD), new Integer(TextureAttributes.COMBINE_ADD),
	"TextureCombineMode", "ADD", null,
	"TextureCombineMode", new Integer(J3D_ADD_SIGNED), new Integer(TextureAttributes.COMBINE_ADD_SIGNED),
	"TextureCombineMode", "ADD_SIGNED", null,
	"TextureCombineMode", new Integer(J3D_SUBTRACT), new Integer(TextureAttributes.COMBINE_SUBTRACT),
	"TextureCombineMode", "SUBTRACT", null,
	"TextureCombineMode", new Integer(J3D_INTERPOLATE), new Integer(TextureAttributes.COMBINE_INTERPOLATE),
	"TextureCombineMode", "INTERPOLATE", null,
	"TextureCombineMode", new Integer(J3D_DOT3), new Integer(TextureAttributes.COMBINE_DOT3),
	"TextureCombineMode", "DOT3", null,

	"GeometryArrayColor", new Integer(J3D_NONE), new Integer(0),
	"GeometryArrayColor", "NONE", null,
	"DEFAULT", null, null,
	"GeometryArrayColor", new Integer(J3D_RGB), new Integer(GeometryArray.COLOR_3),
	"GeometryArrayColor", "RGB", null,
	"GeometryArrayColor", new Integer(J3D_RGBA), new Integer(GeometryArray.COLOR_4),
	"GeometryArrayColor", "RGBA", null,

	"GeometryArrayTexture", new Integer(J3D_NONE), new Integer(0),
	"GeometryArrayTexture", "NONE", null,
	"DEFAULT", null, null,
	"GeometryArrayTexture", new Integer(J3D_2D), new Integer(GeometryArray.TEXTURE_COORDINATE_2),
	"GeometryArrayTexture", "2D", null,
	"GeometryArrayTexture", new Integer(J3D_3D), new Integer(GeometryArray.TEXTURE_COORDINATE_3),
	"GeometryArrayTexture", "3D", null,
	"GeometryArrayTexture", new Integer(J3D_4D), new Integer(GeometryArray.TEXTURE_COORDINATE_4),
	"GeometryArrayTexture", "4D", null,

	"WindowResizePolicy", new Integer(J3D_PHYSICAL_WORLD), new Integer(View.PHYSICAL_WORLD),
	"WindowResizePolicy", "PHYSICAL_WORLD", null,
	"DEFAULT", null, null,
	"WindowResizePolicy", new Integer(J3D_VIRTUAL_WORLD), new Integer(View.VIRTUAL_WORLD),
	"WindowResizePolicy", "VIRTUAL_WORLD", null,

	"WindowMovementPolicy", new Integer(J3D_PHYSICAL_WORLD), new Integer(View.PHYSICAL_WORLD),
	"WindowMovementPolicy", "PHYSICAL_WORLD", null,
	"DEFAULT", null, null,
	"WindowMovementPolicy", new Integer(J3D_VIRTUAL_WORLD), new Integer(View.VIRTUAL_WORLD),
	"WindowMovementPolicy", "VIRTUAL_WORLD", null,
    };

    static {
	map = new HashMap(2*$table.length/3);
	loadTable($table);
	$table = null;		// no longer needed
    }

    //
    // Strings used to build mapping table entries.
    //

    private static final String  SEP = ".";
    private static final String  DEFAULT = "DEFAULT";
    private static final String  TO_JAVA_PREFIX = "";		// unused
    private static final String  TO_YOIX_PREFIX = "YOIX_";

    ///////////////////////////////////
    //
    // J3DMake Methods
    //
    ///////////////////////////////////

    static int
    javaInt(String tag) {

	return(javaInt(tag, null));
    }


    static int
    javaInt(String tag, YoixObject arg) {

	int  value;

	if (arg != null) {
	    if (arg.isNumber())
		value = ((Integer)getObject(tag, arg.intValue())).intValue();
	    else if (arg.isString())
		value = ((Integer)getObject(tag, arg.stringValue())).intValue();
	    else value = ((Integer)getObject(tag, DEFAULT)).intValue();
	} else value = ((Integer)getObject(tag, DEFAULT)).intValue();

	return(value);
    }


    static String
    javaString(String tag) {

	return(javaString(tag, null));
    }


    static String
    javaString(String tag, YoixObject arg) {

	String  value;

	if (arg != null) {
	    if (arg.isNumber())
		value = (String)getObject(tag, arg.intValue());
	    else if (arg.isString())
		value = (String)getObject(tag, arg.stringValue());
	    else value = (String)getObject(tag, DEFAULT);
	} else value = (String)getObject(tag, DEFAULT);

	return(value);
    }


    static YoixObject
    yoixConstant(String tag) {

	return((YoixObject)getObject(TO_YOIX_PREFIX + tag, DEFAULT));
    }


    static YoixObject
    yoixConstant(String tag, int arg) {

	Object  obj;

	if ((obj = getObject(TO_YOIX_PREFIX + tag, arg)) != null) {
	    if (!(obj instanceof YoixObject)) {
		if (obj instanceof Number)
		    obj = YoixObject.newNumber((Integer)obj);
		else if (obj instanceof String)
		    obj = YoixObject.newString((String)obj);
		else obj = null;
	    }
	}
	return((YoixObject)obj);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static void
    loadTable(Object table[]) {

	String  tag;
	String  lasttag = null;
	Object  arg1 = null;
	Object  arg2 = null;
	int     n;

	//
	// Modified version that parses the new table and adds two sets
	// of mappings to the map HashMap. The inverse mapping
	// is currently only used by the yoixConstant() methods.
	//

	for (n = 0; n < table.length; n += 3) {
	    tag = (String)table[n];
	    if (table[n + 1] != null)
		arg1 = table[n + 1];
	    if (table[n + 2] != null)
		arg2 = table[n + 2];
	    if (arg1 != null && arg2 != null) {
		if (tag == null || tag.equals(DEFAULT)) {
		    if (lasttag != null) {
			tag = lasttag + SEP + DEFAULT;
			map.put(tag, arg2);
			if (arg1 instanceof Number)
			    map.put(TO_YOIX_PREFIX + tag, YoixObject.newNumber((Number)arg1));
			else if (arg1 instanceof String)
			    map.put(TO_YOIX_PREFIX + tag, YoixObject.newString((String)arg1));
			else map.put(TO_YOIX_PREFIX + tag, arg1);
		    } else VM.die(INTERNALERROR);
		} else {
		    lasttag = tag;
		    tag = tag + SEP;
		    map.put(tag + arg1, arg2);
		    if (arg1 instanceof Number)
			map.put(TO_YOIX_PREFIX + tag + arg2, YoixObject.newNumber((Number)arg1));
		    else if (arg1 instanceof String)
			map.put(TO_YOIX_PREFIX + tag + arg2, YoixObject.newString((String)arg1));
		    else map.put(TO_YOIX_PREFIX + tag + arg2, arg1);
		}
	    }
	}
    }


    private static Object
    getObject(String tag, int arg) {

	Object  obj;

	if ((obj = map.get(tag + SEP + arg)) == null) {
	    if ((obj = map.get(tag + SEP + DEFAULT)) == null)
		VM.die(INTERNALERROR);
	}
	return(obj);
    }


    static Object
    getObject(String tag, String arg) {

        Object  obj;

        if ((obj = map.get(tag + SEP + arg)) == null) {
	    if ((obj = map.get(tag + SEP + arg.toUpperCase())) == null) {
		if ((obj = map.get(tag + SEP + DEFAULT)) == null)
		    VM.die(INTERNALERROR);
	    }
	}
	return(obj);
    }


}

