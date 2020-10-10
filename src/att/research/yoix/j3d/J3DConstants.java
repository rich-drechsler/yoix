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
import javax.media.j3d.*;
import javax.vecmath.Color3f;
import com.sun.j3d.loaders.Loader;
import att.research.yoix.*;

public
interface J3DConstants

{

    //
    // The constants in this file that begin with the prefix "J3D_" will
    // also be defined in the yoix.j3d dictionary.
    //

    static final int  J3D_PRIORITY = VirtualUniverse.getJ3DThreadPriority();

    //
    // Alpha constants.
    //

    static final int  J3D_INCREASING  = 0x01;
    static final int  J3D_DECREASING  = 0x02;
    static final int  J3D_COMPLETE    = J3D_INCREASING|J3D_DECREASING;

    //
    // Bounds constants.
    //

    static final int  J3D_BOUNDINGBOX = 1;
    static final int  J3D_BOUNDINGSPHERE = 2;
    static final int  J3D_BOUNDINGPOLYTOPE = 3;

    //
    // Constants needed for J3DInterpolator stuff
    //

    // leave slot zero unused
    static final int  J3D_COLORINTERPOLATOR               =  1;
    static final int  J3D_CUSTOMCOLORINTERPOLATOR         =  2;
    static final int  J3D_CUSTOMSWITCHINTERPOLATOR        =  3;
    static final int  J3D_CUSTOMTRANSFORMINTERPOLATOR     =  4;
    static final int  J3D_CUSTOMTRANSPARENCYINTERPOLATOR  =  5;
    static final int  J3D_KBSPLINEINTERPOLATOR            =  6;
    static final int  J3D_POSITIONINTERPOLATOR            =  7;
    static final int  J3D_ROTATIONINTERPOLATOR            =  8;
    static final int  J3D_SCALEINTERPOLATOR               =  9;
    static final int  J3D_SWITCHVALUEINTERPOLATOR         = 10;
    static final int  J3D_TCBSPLINEINTERPOLATOR           = 11;
    static final int  J3D_TRANSPARENCYINTERPOLATOR        = 12;

    static final int  INTERPOLATOR_ARRAY_SIZE       = J3D_TRANSPARENCYINTERPOLATOR;

    //
    // Constants used to set flags in 3D file loaders. Must be flags
    // and in this case exactly matching the Java values is important
    // because loaders (e.g., ObjectFile) can define additional flags
    // that must be based on definitions in Loader.java. Actually it
    // looks like the definition Loader.LOAD_ALL probably could step
    // on flags defined in other loaders - decided not to change it
    // right now even though it could cause problems later no.
    //

    static final int  J3D_LOAD_LIGHT_NODES = Loader.LOAD_LIGHT_NODES;
    static final int  J3D_LOAD_FOG_NODES = Loader.LOAD_FOG_NODES;
    static final int  J3D_LOAD_BACKGROUND_NODES = Loader.LOAD_BACKGROUND_NODES;
    static final int  J3D_LOAD_BEHAVIOR_NODES = Loader.LOAD_BEHAVIOR_NODES;
    static final int  J3D_LOAD_VIEW_GROUPS = Loader.LOAD_VIEW_GROUPS;
    static final int  J3D_LOAD_SOUND_NODES = Loader.LOAD_SOUND_NODES;

    static final int  J3D_LOAD_ALL = Loader.LOAD_ALL;	// Java value is questionable

    //
    // Decided to overload these values, something like what we did in
    // our AWT and Swing implementations, but in this case we probably
    // shouldn't depend on the values stored in the data dictionary so
    // we added (quickly) some inverse mapping capabilites to the j3d
    // lookup code that's now in J3DObject.java. Seems to work, but it
    // may still need to be cleaned up some.
    // 

    static final int  J3D_ALPHA = 1;
    static final int  J3D_AMBIENT = 2;
    static final int  J3D_AMBIENT_AND_DIFFUSE = 3;
    static final int  J3D_BACK = 4;
    static final int  J3D_BASE_LEVEL_LINEAR = 5;
    static final int  J3D_BASE_LEVEL_POINT = 6;
    static final int  J3D_BLEND_ZERO = 7;
    static final int  J3D_BLEND_ONE = 8;
    static final int  J3D_BLEND_SRC_ALPHA = 9;
    static final int  J3D_BLEND_ONE_MINUS_SRC_ALPHA = 10;
    static final int  J3D_BLENDED = 11;
    static final int  J3D_CLAMP = 12;
    static final int  J3D_CLAMP_TO_EDGE = 13;
    static final int  J3D_CLAMP_TO_BOUNDARY = 14;
    static final int  J3D_DIFFUSE = 15;
    static final int  J3D_EMISSIVE = 16;
    static final int  J3D_FASTEST = 17;
    static final int  J3D_FILL = 18;
    static final int  J3D_FILTER4 = 19;
    static final int  J3D_FLAT = 20;
    static final int  J3D_FRONT = 21;
    static final int  J3D_GOURAUD = 22;
    static final int  J3D_INTENSITY = 23;
    static final int  J3D_LINE = 24;
    static final int  J3D_LINEAR_SHARPEN = 25;
    static final int  J3D_LINEAR_SHARPEN_RGB = 26;
    static final int  J3D_LINEAR_SHARPEN_ALPHA = 27;
    static final int  J3D_LUMINANCE = 28;
    static final int  J3D_LUMINANCE_ALPHA = 29;
    static final int  J3D_MULTI_LEVEL_POINT = 30;
    static final int  J3D_MULTI_LEVEL_LINEAR = 31;
    static final int  J3D_NICEST = 32;
    static final int  J3D_NONE = 33;
    static final int  J3D_ONE = 34;
    static final int  J3D_ONE_MINUS_ALPHA = 35;
    static final int  J3D_POINT = 36;
    static final int  J3D_RGB = 37;
    static final int  J3D_RGBA = 38;
    static final int  J3D_SCREEN_DOOR = 39;
    static final int  J3D_SPECULAR = 40;
    static final int  J3D_WRAP = 41;
    static final int  J3D_ZERO = 42;

    static final int  J3D_OBJECT_LINEAR = 43;
    static final int  J3D_EYE_LINEAR = 44;
    static final int  J3D_SPHERE_MAP = 45;
    static final int  J3D_NORMAL_MAP = 46;
    static final int  J3D_REFLECTION_MAP = 47;

    static final int  J3D_MODULATE = 48;
    static final int  J3D_DECAL = 49;
    static final int  J3D_BLEND = 50;
    static final int  J3D_REPLACE = 51;
    static final int  J3D_COMBINE = 52;

    static final int  J3D_ADD = 53;
    static final int  J3D_ADD_SIGNED = 54;
    static final int  J3D_SUBTRACT = 55;
    static final int  J3D_INTERPOLATE = 56;
    static final int  J3D_DOT3 = 57;

    static final int  J3D_TRIANGLE_ARRAY = 58;
    static final int  J3D_QUAD_ARRAY = 59;
    static final int  J3D_TRIANGLE_FAN_ARRAY = 60;
    static final int  J3D_TRIANGLE_STRIP_ARRAY = 61;
    static final int  J3D_POLYGON_ARRAY = 62;

    static final int  J3D_VIRTUAL_WORLD = 63;
    static final int  J3D_PHYSICAL_WORLD = 64;

    static final int  J3D_2D = 65;
    static final int  J3D_3D = 66;
    static final int  J3D_4D = 67;
}

