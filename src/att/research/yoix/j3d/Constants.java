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
import att.research.yoix.*;

public
interface Constants

    extends ConstantsErrorName,
	    J3DConstants,
	    YoixConstants,
	    YoixConstantsImage

{

    //
    // Several standard colors as Color3f constants.
    //

    static final Color3f  COLOR3F_WHITE = new Color3f(Color.white);
    static final Color3f  COLOR3F_LIGHT_GRAY = new Color3f(Color.lightGray);
    static final Color3f  COLOR3F_GRAY = new Color3f(Color.gray);
    static final Color3f  COLOR3F_BLACK = new Color3f(Color.black);

    //
    // Module type names - no reason to make a copy, because the Yoix
    // interpreter won't allow typedict collisions. By convention all
    // constants that start with T_ represent type names.
    //

    static final String  T_AXISANGLE = "AxisAngle";
    static final String  T_BRANCHGROUP = "BranchGroup";
    static final String  T_CANVAS3D = "Canvas3D";
    static final String  T_COLORCUBE = "ColorCube";
    static final String  T_EULERANGLE = "EulerAngle";
    static final String  T_LOCALE3D = "Locale3D";
    static final String  T_POINT3D = "Point3D";
    static final String  T_SHAPE3D = "Shape3D";
    static final String  T_TRANSFORM3D = "Transform3D";
    static final String  T_VECTOR3D = "Vector3D";
    static final String  T_VIEWPLATFORM = "ViewPlatform";
    static final String  T_VIRTUALUNIVERSE = "VirtualUniverse";

    static final String  T_ALPHA = "Alpha";
    static final String  T_BOUNDS = "Bounds";
    static final String  T_INTERPOLATOR = "Interpolator";
    static final String  T_CONFIGOLATOR = "Configolator";
    static final String  T_QUAT4D = "Quat4D";

    static final String  T_TEXT2D = "Text2D";
    static final String  T_SPHERE = "Sphere";
    static final String  T_APPEARANCE = "Appearance";
    static final String  T_AMBIENTLIGHT = "AmbientLight";
    static final String  T_DIRECTIONALLIGHT = "DirectionalLight";
    static final String  T_POINTLIGHT = "PointLight";
    static final String  T_MATERIAL = "Material";
    static final String  T_POINTATTRIBUTES = "PointAttributes";
    static final String  T_LINEATTRIBUTES = "LineAttributes";
    static final String  T_RENDERINGATTRIBUTES = "RenderingAttributes";
    static final String  T_POLYGONATTRIBUTES = "PolygonAttributes";
    static final String  T_TRANSPARENCYATTRIBUTES = "TransparencyAttributes";
    static final String  T_TEXTURE2D = "Texture2D";
    static final String  T_TEXTUREUNIT = "TextureUnit";
    static final String  T_COLORINGATTRIBUTES = "ColoringAttributes";

    static final String  T_POINTARRAY = "PointArray";
    static final String  T_SCENELOADER = "SceneLoader";
    static final String  T_SCENEGRAPHOBJECT = "SceneGraphObject";
    static final String  T_WAKEUP = "Wakeup";
    static final String  T_CONE = "Cone";
    static final String  T_CYLINDER = "Cylinder";
    static final String  T_TRIANGLEARRAY = "TriangleArray";
    static final String  T_LINEARRAY = "LineArray";
    static final String  T_QUADARRAY = "QuadArray";
    static final String  T_LINESTRIPARRAY = "LineStripArray";
    static final String  T_TRIANGLESTRIPARRAY = "TriangleStripArray";
    static final String  T_TRIANGLEFANARRAY = "TriangleFanArray";

    //
    // Numbers used to identify active components. The values aren't
    // important except that they must be unique and shouldn't equal
    // values assigned to the automatically generated constants, like
    // COMPONENT, that are used in YoixModule.newPointer. Starting at
    // LASTTOKEN + 1 should be safe. Automatically generated constants,
    // including LASTTOKEN, are defined in ../YoixParser.jjt.
    //
    // NOTE - decided to include all types for now even though simple
    // ones, like T_POINT3D, don't use their selected number.
    //

    static final int  AXISANGLE = LASTTOKEN + 1;
    static final int  BRANCHGROUP = LASTTOKEN + 2;
    static final int  CANVAS3D = LASTTOKEN + 3;
    static final int  COLORCUBE = LASTTOKEN + 4;
    static final int  EULERANGLE = LASTTOKEN + 5;
    static final int  LOCALE3D = LASTTOKEN + 6;
    static final int  POINT3D = LASTTOKEN + 7;
    static final int  SHAPE3D = LASTTOKEN + 8;
    static final int  TRANSFORM3D = LASTTOKEN + 9;
    static final int  VECTOR3D = LASTTOKEN + 10;
    static final int  VIEWPLATFORM = LASTTOKEN + 11;
    static final int  VIRTUALUNIVERSE = LASTTOKEN + 12;

    static final int  ALPHA = LASTTOKEN + 20;
    static final int  INTERPOLATOR = LASTTOKEN + 21;
    static final int  BOUNDS = LASTTOKEN + 22;
    static final int  CONFIGULATOR = LASTTOKEN + 23;
    static final int  QUAT4D = LASTTOKEN + 24;

    static final int  TEXT2D = LASTTOKEN + 30;
    static final int  SPHERE = LASTTOKEN + 31;
    static final int  APPEARANCE = LASTTOKEN + 32;
    static final int  AMBIENTLIGHT = LASTTOKEN + 33;
    static final int  DIRECTIONALLIGHT = LASTTOKEN + 34;
    static final int  POINTLIGHT = LASTTOKEN + 35;
    static final int  MATERIAL = LASTTOKEN + 36;
    static final int  POINTATTRIBUTES = LASTTOKEN + 37;
    static final int  LINEATTRIBUTES = LASTTOKEN + 38;
    static final int  RENDERINGATTRIBUTES = LASTTOKEN + 39;
    static final int  POLYGONATTRIBUTES = LASTTOKEN + 40;
    static final int  TRANSPARENCYATTRIBUTES = LASTTOKEN + 41;
    static final int  TEXTURE2D = LASTTOKEN + 42;
    static final int  TEXTUREUNIT = LASTTOKEN + 43;
    static final int  COLORINGATTRIBUTES = LASTTOKEN + 44;

    static final int  POINTARRAY = LASTTOKEN + 45;
    static final int  SCENELOADER = LASTTOKEN + 46;
    static final int  SCENEGRAPHOBJECT = LASTTOKEN + 47;
    static final int  WAKEUP = LASTTOKEN + 50;
    static final int  CONE = LASTTOKEN + 51;
    static final int  CYLINDER = LASTTOKEN + 52;
    static final int  TRIANGLEARRAY = LASTTOKEN + 53;
    static final int  LINEARRAY = LASTTOKEN + 54;
    static final int  QUADARRAY = LASTTOKEN + 55;
    static final int  LINESTRIPARRAY = LASTTOKEN + 56;
    static final int  TRIANGLESTRIPARRAY = LASTTOKEN + 57;
    static final int  TRIANGLEFANARRAY = LASTTOKEN + 58;

    //
    // These are default tag strings that are automatically assigned to
    // various NL_TAG fields when we're asked to do things, like build
    // the default ViewPlatform associated with a VirtualUniverse.
    //

    static final String  TAG_VIEWPLATFORM = "viewplatform";

    //
    // Official Yoix field names - required to make classes that extend
    // YoixPointerActive work properly. Low level Java code, mostly in
    // YoixMake.java, uses them when they're defined in a type template.
    // The NY_ prefix is supposed to emphasize that these are official
    // Yoix names (defined in ../YoixConstants.java).
    //

    static final String  NY_CLASSNAME = N_CLASSNAME;
    static final String  NY_MAJOR = N_MAJOR;
    static final String  NY_MINOR = N_MINOR;

    //
    // A few more offical Yoix field names, mostly for the plot example
    // that was added very late and is only supposed to be a template
    // that may help you get started.
    //

    static final String  NY_BACKGROUND = N_BACKGROUND;
    static final String  NY_BACKGROUNDHINTS = N_BACKGROUNDHINTS;
    static final String  NY_BACKGROUNDIMAGE = N_BACKGROUNDIMAGE;
    static final String  NY_BORDER = N_BORDER;
    static final String  NY_BORDERCOLOR = N_BORDERCOLOR;
    static final String  NY_BUTTONMASK = N_BUTTONMASK;
    static final String  NY_CURSOR = N_CURSOR;
    static final String  NY_DRAWABLE = N_DRAWABLE;
    static final String  NY_ENABLED = N_ENABLED;
    static final String  NY_ETC = N_ETC;
    static final String  NY_FONT = N_FONT;
    static final String  NY_FOREGROUND = N_FOREGROUND;
    static final String  NY_GRAPHICS = N_GRAPHICS;
    static final String  NY_INITIALIZER = N_INITIALIZER;
    static final String  NY_JAVACONSTRUCTOR = N_JAVACONSTRUCTOR;
    static final String  NY_JAVASETUP = N_JAVASETUP;
    static final String  NY_LOCATION = N_LOCATION;
    static final String  NY_MAXIMUMSIZE = N_MAXIMUMSIZE;
    static final String  NY_MINIMUMSIZE = N_MINIMUMSIZE;
    static final String  NY_PAINT = N_PAINT;
    static final String  NY_POPUP = N_POPUP;
    static final String  NY_PREFERREDSIZE = N_PREFERREDSIZE;
    static final String  NY_REPAINT = N_REPAINT;
    static final String  NY_REQUESTFOCUS = N_REQUESTFOCUS;
    static final String  NY_ROOT = N_ROOT;
    static final String  NY_SHOWING = N_SHOWING;
    static final String  NY_SIZE = N_SIZE;
    static final String  NY_STATE = N_STATE;
    static final String  NY_TAG = N_TAG;
    static final String  NY_VISIBLE = N_VISIBLE;

    //
    // Local field names. These correspond to the large collection of
    // N_XXX names that you'll find in ../YoixConstants.java, but we
    // changed the prefix to NL_ to avoid collisions and to emphasize
    // that they're local to this module.
    //

    static final String  NL_AFFINE = "affine";
    static final String  NL_ALIVE = "alive";
    static final String  NL_ALPHA = "alpha";
    static final String  NL_AMBIENTCOLOR = "ambientcolor";
    static final String  NL_ANGLE = "angle";
    static final String  NL_ANTIALIASING = "antialiasing";
    static final String  NL_APPEARANCE = "appearance";
    static final String  NL_AUTONORMALIZE = "autonormalize";
    static final String  NL_AXIS = "axis";
    static final String  NL_BACKGROUND = "background";
    static final String  NL_BACKGROUNDHINTS = "backgroundhints";
    static final String  NL_BACKGROUNDIMAGE = "backgroundimage";
    static final String  NL_BETA = "beta";
    static final String  NL_BOUNDINGLEAF = "boundingleaf";
    static final String  NL_BOUNDS = "bounds";
    static final String  NL_CENTER = "center";
    static final String  NL_COLOR = "color";
    static final String  NL_COLORTARGET = "colortarget";
    static final String  NL_COMPILE = "compile";
    static final String  NL_COMPILED = "compiled";
    static final String  NL_CONFIGURATION = "configuration";
    static final String  NL_CONGRUENT = "congruent";
    static final String  NL_CONTENT = "content";
    static final String  NL_CONVENTION = "convention";
    static final String  NL_CULLING = "culling";
    static final String  NL_DESTINATIONBLEND = "destinationblend";
    static final String  NL_DETERMINANT = "determinant";
    static final String  NL_DIFFUSECOLOR = "diffusecolor";
    static final String  NL_DIRECTION = "direction";
    static final String  NL_DIVISIONS = "divisions";
    static final String  NL_DOUBLEBUFFERED = "doublebuffered";
    static final String  NL_DTRANSFORM = "dtransform";
    static final String  NL_EMISSIVECOLOR = "emissivecolor";
    static final String  NL_ENABLED = "enabled";
    static final String  NL_EQUALS = "equals";
    static final String  NL_FIELDOFVIEW = "fieldofview";
    static final String  NL_FONT = "font";
    static final String  NL_GAMMA = "gamma";
    static final String  NL_GOINGTO = "goingto";
    static final String  NL_IDENTITY = "identity";
    static final String  NL_IDTRANSFORM = "idtransform";
    static final String  NL_INITIALIZE = "initialize";
    static final String  NL_INTERPOLATOR = "interpolator";
    static final String  NL_INVERT = "invert";
    static final String  NL_ITRANSFORM = "itransform";
    static final String  NL_KNOTS = "knots";
    static final String  NL_LAYOUT = "layout";
    static final String  NL_LINES = "lines";
    static final String  NL_LIVE = "live";
    static final String  NL_LOCALE = "locale";
    static final String  NL_LOCATION = "location";
    static final String  NL_LOOP = "loop";
    static final String  NL_LOWER = "lower";
    static final String  NL_MATERIAL = "material";
    static final String  NL_MODE = "mode";
    static final String  NL_MODEL = "model";
    static final String  NL_MUL = "mul";
    static final String  NL_NODES = "nodes";
    static final String  NL_NORMALIZE = "normalize";
    static final String  NL_ORIENTATION = "orientation";
    static final String  NL_ORTHOGONAL = "orthogonal";
    static final String  NL_PARENT = "parent";
    static final String  NL_PAUSETIME = "pausetime";
    static final String  NL_PHASEDELAY = "phasedelay";
    static final String  NL_POINTS = "points";
    static final String  NL_POLYGONS = "polygons";
    static final String  NL_POSITION = "position";
    static final String  NL_POSTPROCESSSTIMULUS = "postprocessStimulus";
    static final String  NL_POSTRENDER = "postRender";
    static final String  NL_POSTSWAP = "postSwap";
    static final String  NL_PREPROCESSSTIMULUS = "preprocessStimulus";
    static final String  NL_PRERENDER = "preRender";
    static final String  NL_PRIORITY = "priority";
    static final String  NL_PROCESSSTIMULUS = "processStimulus";
    static final String  NL_RADIUS = "radius";
    static final String  NL_RENDERFIELD = "renderField";
    static final String  NL_RENDERING = "rendering";
    static final String  NL_RESUMETIME = "resumetime";
    static final String  NL_RIGID = "rigid";
    static final String  NL_ROOT = "root";
    static final String  NL_ROTATEX = "rotateX";
    static final String  NL_ROTATEY = "rotateY";
    static final String  NL_ROTATEZ = "rotateZ";
    static final String  NL_RUN = "run";
    static final String  NL_SCALE = "scale";
    static final String  NL_SETTOEULER = "setToEuler";
    static final String  NL_SETTOFRUSTUM = "setToFrustum";
    static final String  NL_SETTOIDENTITY = "setToIdentity";
    static final String  NL_SETTOLOOKAT = "setToLookAt";
    static final String  NL_SETTOORTHO = "setToOrtho";
    static final String  NL_SETTOPERSPECTIVE = "setToPerspective";
    static final String  NL_SETTOROTATIONX = "setToRotationX";
    static final String  NL_SETTOROTATIONY = "setToRotationY";
    static final String  NL_SETTOROTATIONZ = "setToRotationZ";
    static final String  NL_SETTOSCALE = "setToScale";
    static final String  NL_SETTOSHEARX = "setToShearX";
    static final String  NL_SETTOSHEARY = "setToShearY";
    static final String  NL_SETTOSHEARZ = "setToShearZ";
    static final String  NL_SETTOTRANSFORM = "setToTransform";
    static final String  NL_SETTOTRANSLATION = "setToTranslation";
    static final String  NL_SETTOVIEWAT = "setToViewAt";
    static final String  NL_SETTOZERO = "setToZero";
    static final String  NL_SHADEMODEL = "shademodel";
    static final String  NL_SHEARX = "shearX";
    static final String  NL_SHEARY = "shearY";
    static final String  NL_SHEARZ = "shearZ";
    static final String  NL_SHININESS = "shininess";
    static final String  NL_SHXY = "shxy";
    static final String  NL_SHXZ = "shxz";
    static final String  NL_SHYX = "shyx";
    static final String  NL_SHYZ = "shyz";
    static final String  NL_SHZX = "shzx";
    static final String  NL_SHZY = "shzy";
    static final String  NL_SIZE = "size";
    static final String  NL_SOURCEBLEND = "sourceblend";
    static final String  NL_SPECULARCOLOR = "specularcolor";
    static final String  NL_STARTINGFROM = "startingfrom";
    static final String  NL_STARTTIME = "starttime";
    static final String  NL_SX = "sx";
    static final String  NL_SY = "sy";
    static final String  NL_SZ = "sz";
    static final String  NL_TAG = "tag";
    static final String  NL_TAGGED = "tagged";
    static final String  NL_TARGET = "target";
    static final String  NL_TEXT = "text";
    static final String  NL_TRANSFORM = "transform";
    static final String  NL_TRANSLATE = "translate";
    static final String  NL_TRANSPARENCY = "transparency";
    static final String  NL_TRANSPOSE = "transpose";
    static final String  NL_TRIGGERTIME = "triggertime";
    static final String  NL_TX = "tx";
    static final String  NL_TY = "ty";
    static final String  NL_TYPE = "type";
    static final String  NL_TYPES = "types";
    static final String  NL_TZ = "tz";
    static final String  NL_UNIVERSE = "universe";
    static final String  NL_UPPER = "upper";
    static final String  NL_VALUE = "value";
    static final String  NL_VALUES = "values";
    static final String  NL_VIEW = "view";
    static final String  NL_VISIBLE = "visible";
    static final String  NL_W = "w";
    static final String  NL_WAKEUP = "wakeup";
    static final String  NL_WAVEFORM = "waveform";
    static final String  NL_X = "x";
    static final String  NL_Y = "y";
    static final String  NL_Z = "z";
    static final String  NL_ZERO = "zero";

    static final String  NL_OFFSCREENBUFFER = "offscreenbuffer";
    static final String  NL_TEXTURE = "texture";
    static final String  NL_YUP = "yup";
    static final String  NL_BYREFERENCE = "byreference";
    static final String  NL_FORMAT = "format";
    static final String  NL_WIDTH = "width";
    static final String  NL_HEIGHT = "height";
    static final String  NL_IMAGE = "image";
    static final String  NL_MIPMAP = "mipmap";
    static final String  NL_MIPMAPLEVELS = "mipmaplevels";
    static final String  NL_SOURCE = "source";
    static final String  NL_BASELEVEL = "baselevel";
    static final String  NL_LODOFFSET = "lodoffset";
    static final String  NL_LODRANGE = "lodrange";
    static final String  NL_MAGFILTER = "magfilter";
    static final String  NL_MINFILTER = "minfilter";
    static final String  NL_OFFSET = "offset";
    static final String  NL_S = "s";
    static final String  NL_T = "t";
    static final String  NL_R = "r";
    static final String  NL_BOUNDARY = "boundary";
    static final String  NL_SHARPEN = "sharpen";
    static final String  NL_FILTER4 = "filter4";
    static final String  NL_PROPERTIES = "properties";
    static final String  NL_ANISOTROPICDEGREE = "anisotropicdegree";
    static final String  NL_GENERATIONMODE = "generationmode";
    static final String  NL_TEXTUREMODE = "texturemode";
    static final String  NL_COMBINERGBMODE = "combinergbmode";
    static final String  NL_COMBINEALPHAMODE = "combinealphamode";
    static final String  NL_TEXTUREUNIT = "textureunit";
    static final String  NL_MAXIMUMLEVEL = "maximumlevel";

    static final String  NL_COORDINATES = "coordinates";
    static final String  NL_COLORS = "colors";
    static final String  NL_NORMALS = "normals";
    static final String  NL_TEXTURECOORDINATES = "texturecoordinates";
    static final String  NL_GEOMETRY = "geometry";
    static final String  NL_LOCALEYELIGHTING = "localeyelighting";

    static final String  NL_LOADERS = "loaders";
    static final String  NL_JAVACLASS = "javaclass";
    static final String  NL_FLAGS = "flags";
    static final String  NL_LOAD = "load";
    static final String  NL_ERRORDICT = "errordict";
    static final String  NL_ERRORMODEL = "errormodel";
    static final String  NL_ERRORSCENE = "errorscene";
    static final String  NL_PATH = "path";
    static final String  NL_ETC = "etc";
    static final String  NL_POSTLOAD = "postLoad";
    static final String  NL_PRELOAD = "preLoad";

    static final String  NL_RESIZEPOLICY = "resizepolicy";
    static final String  NL_MOVEMENTPOLICY = "movementpolicy";

    static final String  NL_COLLIDABLE = "collidable";
    static final String  NL_PICKABLE = "pickable";

    static final String  NL_COLORING = "coloring";
    static final String  NL_CAPABILITIES = "capabilities";
    static final String  NL_APPEARANCEOVERRIDE = "appearanceoverride";
    static final String  NL_DEFAULTCAPABILITY = "defaultcapability";

    static final String  NL_DETACH = "detached";

    static final String  NL_CAP = "cap";
    static final String  NL_BODY = "body";
    static final String  NL_SHARED = "shared";
    static final String  NL_GENERATENORMALS = "generatenormals";
    static final String  NL_GENERATETEXTURECOORDS = "generatetexturecoords";
    static final String  NL_BOTTOM = "bottom";
    static final String  NL_TOP = "top";
    static final String  NL_COLORFORMAT = "colorformat";
    static final String  NL_TEXTUREFORMAT = "textureformat";

    static final String  NL_RED = "red";
    static final String  NL_GREEN = "green";
    static final String  NL_BLUE = "blue";
    static final String  NL_VERTEXCOUNT = "vertexcount";
    static final String  NL_STRIPVERTEXCOUNTS = "stripvertexcounts";
    static final String  NL_IGNOREVERTEXCOLORS = "ignorevertexcolors";

    static final String  NL_ACTIVATIONRADIUS = "activationradius";

    static final String  NL_BACKCLIPDISTANCE = "backclipdistance";
    static final String  NL_FRONTCLIPDISTANCE = "frontclipdistance";

    //
    // Values associated with local field names.
    //

    static final int  VL_AFFINE = 1;
    static final int  VL_ALIVE = 2;
    static final int  VL_ALPHA = 3;
    static final int  VL_AMBIENTCOLOR = 4;
    static final int  VL_ANGLE = 5;
    static final int  VL_ANTIALIASING = 6;
    static final int  VL_APPEARANCE = 7;
    static final int  VL_AUTONORMALIZE = 8;
    static final int  VL_AXIS = 9;
    static final int  VL_BACKGROUND = 10;
    static final int  VL_BACKGROUNDHINTS = 11;
    static final int  VL_BACKGROUNDIMAGE = 12;
    static final int  VL_BETA = 13;
    static final int  VL_BOUNDINGLEAF = 14;
    static final int  VL_BOUNDS = 15;
    static final int  VL_CENTER = 16;
    static final int  VL_COLOR = 17;
    static final int  VL_COLORTARGET = 18;
    static final int  VL_COMPILE = 19;
    static final int  VL_COMPILED = 20;
    static final int  VL_CONFIGURATION = 21;
    static final int  VL_CONGRUENT = 22;
    static final int  VL_CONTENT = 23;
    static final int  VL_CONVENTION = 24;
    static final int  VL_CULLING = 25;
    static final int  VL_DESTINATIONBLEND = 26;
    static final int  VL_DETERMINANT = 27;
    static final int  VL_DIFFUSECOLOR = 28;
    static final int  VL_DIRECTION = 29;
    static final int  VL_DIVISIONS = 30;
    static final int  VL_DOUBLEBUFFERED = 31;
    static final int  VL_DTRANSFORM = 32;
    static final int  VL_EMISSIVECOLOR = 33;
    static final int  VL_ENABLED = 34;
    static final int  VL_EQUALS = 35;
    static final int  VL_FIELDOFVIEW = 36;
    static final int  VL_FONT = 37;
    static final int  VL_GAMMA = 38;
    static final int  VL_GOINGTO = 39;
    static final int  VL_IDENTITY = 40;
    static final int  VL_IDTRANSFORM = 41;
    static final int  VL_INITIALIZE = 42;
    static final int  VL_INTERPOLATOR = 43;
    static final int  VL_INVERT = 44;
    static final int  VL_ITRANSFORM = 45;
    static final int  VL_KNOTS = 46;
    static final int  VL_LAYOUT = 47;
    static final int  VL_LINES = 48;
    static final int  VL_LIVE = 49;
    static final int  VL_LOCALE = 50;
    static final int  VL_LOCATION = 51;
    static final int  VL_LOOP = 52;
    static final int  VL_LOWER = 53;
    static final int  VL_MATERIAL = 54;
    static final int  VL_MODE = 55;
    static final int  VL_MODEL = 56;
    static final int  VL_MUL = 57;
    static final int  VL_NODES = 58;
    static final int  VL_NORMALIZE = 59;
    static final int  VL_ORIENTATION = 61;
    static final int  VL_ORTHOGONAL = 62;
    static final int  VL_PARENT = 63;
    static final int  VL_PAUSETIME = 64;
    static final int  VL_PHASEDELAY = 65;
    static final int  VL_POINTS = 66;
    static final int  VL_POLYGONS = 67;
    static final int  VL_POSITION = 68;
    static final int  VL_POSTPROCESSSTIMULUS = 69;
    static final int  VL_POSTRENDER = 70;
    static final int  VL_POSTSWAP = 71;
    static final int  VL_PREPROCESSSTIMULUS = 72;
    static final int  VL_PRERENDER = 73;
    static final int  VL_PRIORITY = 74;
    static final int  VL_PROCESSSTIMULUS = 75;
    static final int  VL_RADIUS = 76;
    static final int  VL_RENDERFIELD = 77;
    static final int  VL_RENDERING = 78;
    static final int  VL_RESUMETIME = 79;
    static final int  VL_RIGID = 80;
    static final int  VL_ROOT = 81;
    static final int  VL_ROTATEX = 82;
    static final int  VL_ROTATEY = 83;
    static final int  VL_ROTATEZ = 84;
    static final int  VL_RUN = 85;
    static final int  VL_SCALE = 86;
    static final int  VL_SETTOEULER = 87;
    static final int  VL_SETTOFRUSTUM = 88;
    static final int  VL_SETTOIDENTITY = 89;
    static final int  VL_SETTOLOOKAT = 90;
    static final int  VL_SETTOORTHO = 91;
    static final int  VL_SETTOPERSPECTIVE = 92;
    static final int  VL_SETTOROTATIONX = 93;
    static final int  VL_SETTOROTATIONY = 94;
    static final int  VL_SETTOROTATIONZ = 95;
    static final int  VL_SETTOSCALE = 96;
    static final int  VL_SETTOSHEARX = 97;
    static final int  VL_SETTOSHEARY = 98;
    static final int  VL_SETTOSHEARZ = 99;
    static final int  VL_SETTOTRANSFORM = 100;
    static final int  VL_SETTOTRANSLATION = 101;
    static final int  VL_SETTOVIEWAT = 102;
    static final int  VL_SETTOZERO = 103;
    static final int  VL_SHADEMODEL = 104;
    static final int  VL_SHEARX = 105;
    static final int  VL_SHEARY = 106;
    static final int  VL_SHEARZ = 107;
    static final int  VL_SHININESS = 108;
    static final int  VL_SHXY = 109;
    static final int  VL_SHXZ = 110;
    static final int  VL_SHYX = 111;
    static final int  VL_SHYZ = 112;
    static final int  VL_SHZX = 113;
    static final int  VL_SHZY = 114;
    static final int  VL_SIZE = 115;
    static final int  VL_SOURCEBLEND = 116;
    static final int  VL_SPECULARCOLOR = 117;
    static final int  VL_STARTINGFROM = 118;
    static final int  VL_STARTTIME = 119;
    static final int  VL_SX = 120;
    static final int  VL_SY = 121;
    static final int  VL_SZ = 122;
    static final int  VL_TAG = 123;
    static final int  VL_TAGGED = 124;
    static final int  VL_TARGET = 125;
    static final int  VL_TEXT = 126;
    static final int  VL_TRANSFORM = 127;
    static final int  VL_TRANSLATE = 128;
    static final int  VL_TRANSPARENCY = 129;
    static final int  VL_TRANSPOSE = 130;
    static final int  VL_TRIGGERTIME = 131;
    static final int  VL_TX = 132;
    static final int  VL_TY = 133;
    static final int  VL_TYPE = 134;
    static final int  VL_TYPES = 135;
    static final int  VL_TZ = 136;
    static final int  VL_UNIVERSE = 137;
    static final int  VL_UPPER = 138;
    static final int  VL_VALUE = 139;
    static final int  VL_VALUES = 140;
    static final int  VL_VIEW = 141;
    static final int  VL_VISIBLE = 142;
    static final int  VL_W = 143;
    static final int  VL_WAKEUP = 144;
    static final int  VL_WAVEFORM = 145;
    static final int  VL_X = 146;
    static final int  VL_Y = 147;
    static final int  VL_Z = 148;
    static final int  VL_ZERO = 149;

    static final int  VL_OFFSCREENBUFFER = 150;
    static final int  VL_TEXTURE = 151;
    static final int  VL_YUP = 152;
    static final int  VL_BYREFERENCE = 153;
    static final int  VL_FORMAT = 154;
    static final int  VL_WIDTH = 155;
    static final int  VL_HEIGHT = 156;
    static final int  VL_IMAGE = 157;
    static final int  VL_MIPMAP = 158;
    static final int  VL_MIPMAPLEVELS = 159;
    static final int  VL_SOURCE = 160;
    static final int  VL_BASELEVEL = 161;
    static final int  VL_MAGFILTER = 162;
    static final int  VL_MINFILTER = 163;
    static final int  VL_LODOFFSET = 164;
    static final int  VL_LODRANGE = 165;
    static final int  VL_MAXIMUM = 166;
    static final int  VL_MINIMUM = 167;
    static final int  VL_OFFSET = 168;
    static final int  VL_S = 169;
    static final int  VL_T = 170;
    static final int  VL_R = 171;
    static final int  VL_BOUNDARY = 172;
    static final int  VL_SHARPEN = 173;
    static final int  VL_FILTER4 = 174;
    static final int  VL_PROPERTIES = 175;
    static final int  VL_ANISOTROPICDEGREE = 176;
    static final int  VL_GENERATIONMODE = 177;
    static final int  VL_TEXTUREMODE = 178;
    static final int  VL_COMBINERGBMODE = 179;
    static final int  VL_COMBINEALPHAMODE = 180;
    static final int  VL_TEXTUREUNIT = 181;
    static final int  VL_MAXIMUMLEVEL = 182;

    static final int  VL_COORDINATES = 183;
    static final int  VL_COLORS = 184;
    static final int  VL_NORMALS = 185;
    static final int  VL_TEXTURECOORDINATES = 186;
    static final int  VL_GEOMETRY = 187;
    static final int  VL_LOCALEYELIGHTING = 188;

    static final int  VL_LOADERS = 189;
    static final int  VL_JAVACLASS = 190;
    static final int  VL_FLAGS = 191;
    static final int  VL_LOAD = 192;
    static final int  VL_ERRORDICT = 193;
    static final int  VL_ERRORMODEL = 194;
    static final int  VL_ERRORSCENE = 195;
    static final int  VL_PATH = 196;
    static final int  VL_ETC = 197;
    static final int  VL_POSTLOAD = 198;
    static final int  VL_PRELOAD = 199;

    static final int  VL_RESIZEPOLICY = 200;
    static final int  VL_MOVEMENTPOLICY = 201;

    static final int  VL_COLLIDABLE = 202;
    static final int  VL_PICKABLE = 203;

    static final int  VL_COLORING = 204;
    static final int  VL_CAPABILITIES = 205;
    static final int  VL_APPEARANCEOVERRIDE = 206;
    static final int  VL_DEFAULTCAPABILITY = 207;

    static final int  VL_DETACH = 208;

    static final int  VL_CAP = 209;
    static final int  VL_BODY = 210;
    static final int  VL_SHARED = 211;
    static final int  VL_GENERATENORMALS = 212;
    static final int  VL_GENERATETEXTURECOORDS = 213;
    static final int  VL_BOTTOM = 214;
    static final int  VL_TOP = 215;
    static final int  VL_COLORFORMAT = 216;
    static final int  VL_TEXTUREFORMAT = 217;

    static final int  VL_RED = 218;
    static final int  VL_GREEN = 219;
    static final int  VL_BLUE = 220;
    static final int  VL_VERTEXCOUNT = 221;
    static final int  VL_STRIPVERTEXCOUNTS = 222;
    static final int  VL_IGNOREVERTEXCOLORS = 223;

    static final int  VL_ACTIVATIONRADIUS = 224;

    static final int  VL_BACKCLIPDISTANCE = 225;
    static final int  VL_FRONTCLIPDISTANCE = 226;
}

