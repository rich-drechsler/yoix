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
import att.research.yoix.*;

public abstract
class Module extends YoixModule

    implements Constants

{

    //
    // Java3D module support.
    //

    public static final String  $MODULENAME = "j3d";	// the official name
    public static final String  $MODULECREATED = "Sun Mar 13 09:35:37 EST 2005";
    public static final String  $MODULENOTICE = YOIXNOTICE;
    public static final String  $MODULEVERSION = "0.4";

    //
    // We use READCLASS to extract constants from this class that begin
    // with the J3D_ prefix. We could eventually move constants that are
    // also supposed to be available to Yoix scripts through the yoix.j3d
    // module to a different class (say ConstantsExport), but that will
    // only be necessary if there are J3D_ constants in Constants.java
    // that we don't want to export.
    //

    public static String $CONSTANTS = J3DConstants.class.getName();

    //
    // This is the name of the class with the method (either newObject()
    // or newPointer()) that's called by YoixObject.newPointer() when we
    // need to build Yoix versions of special objects, like Transform3D,
    // that are "active" objects (ie., they extend YoixPointerActive).
    // When $CLASSNAME is null YoixObject.newPointer() expects that it
    // will find newObject() (or newPointer()) in this class. There's
    // still a bunch we can do to improve things - later!!
    //

    public static final String  $CLASSNAME = J3DObject.class.getName();

    //
    // These numbers are used to identify active components (i.e., the
    // ones that extend YoixPointerActive). Values should be stored in
    // the NY_MAJOR field in the component's type template that's defined
    // in $module. Currently only have one, but there probably will be
    // more.
    //

    public static Integer  $BRANCHGROUP = new Integer(BRANCHGROUP);
    public static Integer  $CANVAS3D = new Integer(CANVAS3D);
    public static Integer  $COLORCUBE = new Integer(COLORCUBE);
    public static Integer  $LOCALE3D = new Integer(LOCALE3D);
    public static Integer  $SHAPE3D = new Integer(SHAPE3D);
    public static Integer  $TRANSFORM3D = new Integer(TRANSFORM3D);
    public static Integer  $VIEWPLATFORM = new Integer(VIEWPLATFORM);
    public static Integer  $VIRTUALUNIVERSE = new Integer(VIRTUALUNIVERSE);

    public static Integer  $APPEARANCE = new Integer(APPEARANCE);
    public static Integer  $AMBIENTLIGHT = new Integer(AMBIENTLIGHT);
    public static Integer  $DIRECTIONALLIGHT = new Integer(DIRECTIONALLIGHT);
    public static Integer  $COLORINGATTRIBUTES = new Integer(COLORINGATTRIBUTES);
    public static Integer  $LINEATTRIBUTES = new Integer(LINEATTRIBUTES);
    public static Integer  $MATERIAL = new Integer(MATERIAL);
    public static Integer  $POINTATTRIBUTES = new Integer(POINTATTRIBUTES);
    public static Integer  $POINTLIGHT = new Integer(POINTLIGHT);
    public static Integer  $POLYGONATTRIBUTES = new Integer(POLYGONATTRIBUTES);
    public static Integer  $RENDERINGATTRIBUTES = new Integer(RENDERINGATTRIBUTES);
    public static Integer  $SPHERE = new Integer(SPHERE);
    public static Integer  $TEXT2D = new Integer(TEXT2D);
    public static Integer  $TEXTURE2D = new Integer(TEXTURE2D);
    public static Integer  $TRANSPARENCYATTRIBUTES = new Integer(TRANSPARENCYATTRIBUTES);
    public static Integer  $TEXTUREUNIT = new Integer(TEXTUREUNIT);
    public static Integer  $POINTARRAY = new Integer(POINTARRAY);
    public static Integer  $SCENELOADER = new Integer(SCENELOADER);

    public static Integer  $ALPHA = new Integer(ALPHA);
    public static Integer  $BOUNDS = new Integer(BOUNDS);
    public static Integer  $INTERPOLATOR = new Integer(INTERPOLATOR);
    public static Integer  $WAKEUP = new Integer(WAKEUP);
    public static Integer  $CONE = new Integer(CONE);
    public static Integer  $CYLINDER = new Integer(CYLINDER);
    public static Integer  $TRIANGLEARRAY = new Integer(TRIANGLEARRAY);
    public static Integer  $LINEARRAY = new Integer(LINEARRAY);
    public static Integer  $QUADARRAY = new Integer(QUADARRAY);
    public static Integer  $LINESTRIPARRAY = new Integer(LINESTRIPARRAY);
    public static Integer  $TRIANGLESTRIPARRAY = new Integer(TRIANGLESTRIPARRAY);
    public static Integer  $TRIANGLEFANARRAY = new Integer(TRIANGLEFANARRAY);

    //
    // SceneLoader support.
    //

    public static String  $DEFAULTSCENELOADERS = "DefaultSceneLoaders";

    public static String  $WAVEFRONT_LOADER = "com.sun.j3d.loaders.objectfile.ObjectFile";
    public static String  $LIGHTWAVE_LOADER = "com.sun.j3d.loaders.lw3d.Lw3dLoader";

    //
    // Miscellaneous stuff
    //

    public static Integer  $J3D_2D = new Integer(J3D_2D);
    public static Integer  $J3D_ALPHA = new Integer(J3D_ALPHA);
    public static Integer  $J3D_BACK = new Integer(J3D_BACK);
    public static Integer  $J3D_BASE_LEVEL_LINEAR = new Integer(J3D_BASE_LEVEL_LINEAR);
    public static Integer  $J3D_DIFFUSE = new Integer(J3D_DIFFUSE);
    public static Integer  $J3D_FILL = new Integer(J3D_FILL);
    public static Integer  $J3D_GOURAUD = new Integer(J3D_GOURAUD);
    public static Integer  $J3D_INCREASING = new Integer(J3D_INCREASING);
    public static Integer  $J3D_LOAD_ALL = new Integer(J3D_LOAD_ALL);
    public static Integer  $J3D_MODULATE = new Integer(J3D_MODULATE);
    public static Integer  $J3D_NONE = new Integer(J3D_NONE);
    public static Integer  $J3D_OBJECT_LINEAR = new Integer(J3D_OBJECT_LINEAR);
    public static Integer  $J3D_ONE_MINUS_ALPHA = new Integer(J3D_ONE_MINUS_ALPHA);
    public static Integer  $J3D_PHYSICAL_WORLD = new Integer(J3D_PHYSICAL_WORLD);
    public static Integer  $J3D_PRIORITY = new Integer(J3D_PRIORITY);
    public static Integer  $J3D_REPLACE = new Integer(J3D_REPLACE);
    public static Integer  $J3D_RGB = new Integer(J3D_RGB);
    public static Integer  $MAXINT = new Integer(Integer.MAX_VALUE);
    public static Integer  $STANDARDCURSOR = new Integer(V_STANDARD_CURSOR);
    public static Integer  $TILEHINT = new Integer(YOIX_SCALE_TILE);

    public static Double   $TEXT2DSCALE = new Double(1.0/256.0);

    public static Object  $module[] = {
    //
    // NAME                      ARG                      COMMAND     MODE   REFERENCE
    // ----                      ---                      -------     ----   ---------
       $MODULENAME,              "102",                   $LIST,      $RORO, $MODULENAME,
       $CONSTANTS,               "J3D_",                  $READCLASS, $LR__, null,

       "generateStrips",         "1",                     $BUILTIN,   $LR_X, null,

       "getDefaultErrorScene",   "0",                     $BUILTIN,   $LR_X, null,
       "getDefaultPreLoad",      "0",                     $BUILTIN,   $LR_X, null,
       "getDefaultPostLoad",     "0",                     $BUILTIN,   $LR_X, null,
       "getDefaultSceneLoaders", "0",                     $BUILTIN,   $LR_X, null,

       "setDefaultErrorScene",   "1",                     $BUILTIN,   $LR_X, null,
       "setDefaultPreLoad",      "1",                     $BUILTIN,   $LR_X, null,
       "setDefaultPostLoad",     "1",                     $BUILTIN,   $LR_X, null,
       "setDefaultSceneLoaders", "1",                     $BUILTIN,   $LR_X, null,

    //
    // The currently supported SceneLoaders along with a writable/growable
    // list of SceneLoaders that maps file name suffixes to loaders. The
    // individual loader descriptions are dictionaries that serve as the
    // initializers when the actual SceneLoader is built, which currently
    // happens the first time it's used.
    //
    // NOTE - setting the mode of entries in $DEFAULTSCENELOADERS to $ARW_
    // is needed for the current collection and should be used when any new
    // new loader is added to the $DEFAULTSCENELOADERS table. Omit it and
    // users will get a typecheck error when the interpreter builds the Yoix
    // version and then tries to store it back in table.
    //

       null,                     "3",                     $DICT,      $RW_,  $WAVEFRONT_LOADER,
       null,                     "-1",                    $GROWTO,    null,  null,
       NL_ERRORMODEL,            "1",                     $INTEGER,   $RW_,  null,
       NL_FLAGS,                 $J3D_LOAD_ALL,           $INTEGER,   $RW_,  null,
       NL_JAVACLASS,             $WAVEFRONT_LOADER,       $STRING,    $RW_,  null,

       null,                     "3",                     $DICT,      $RW_,  $LIGHTWAVE_LOADER,
       null,                     "-1",                    $GROWTO,    null,  null,
       NL_ERRORMODEL,            "1",                     $INTEGER,   $RW_,  null,
       NL_FLAGS,                 $J3D_LOAD_ALL,           $INTEGER,   $RW_,  null,
       NL_JAVACLASS,             $LIGHTWAVE_LOADER,       $STRING,    $RW_,  null,

       $DEFAULTSCENELOADERS,     "2",                     $DICT,      $LR__, $DEFAULTSCENELOADERS,
       null,                     "-1",                    $GROWTO,    null,  null,
       "lwob",                   $LIGHTWAVE_LOADER,       $GET,       $ARW_, null,
       "obj",                    $WAVEFRONT_LOADER,       $GET,       $ARW_, null,

    //
    // This is the Yoix represenatation of a SceneLoader.
    //

       T_SCENELOADER,            "14",                    $DICT,      $L___, T_SCENELOADER,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $SCENELOADER,            $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_ENABLED,               $TRUE,                   $INTEGER,   $RW_,  null,
       NL_ERRORDICT,             T_DICT,                  $NULL,      $LRW_, null,
       NL_ERRORMODEL,            "1",                     $INTEGER,   $RW_,  null,
       NL_ERRORSCENE,            T_STRING,                $NULL,      $RW_,  null,
       NL_FLAGS,                 $J3D_LOAD_ALL,           $INTEGER,   $RW_,  null,
       NL_JAVACLASS,             T_STRING,                $NULL,      $RW_,  null,
       NY_JAVACONSTRUCTOR,       T_ARRAY,                 $NULL,      $RW_,  null,
       NY_JAVASETUP,             T_ARRAY,                 $NULL,      $RW_,  null,
       NL_LOAD,                  T_CALLABLE,              $NULL,      $L__X, null,
       NL_PRELOAD,               T_CALLABLE,              $NULL,      $RWX,  null,
       NL_POSTLOAD,              T_CALLABLE,              $NULL,      $RWX,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

    //
    // Currently a dummy type that we use when we need to represet any
    // Java SceneGraphObject that don't support and/or don't know how
    // to translate into something that we do support. Small chance it
    // will become a J3DPointerObject.
    //

       T_SCENEGRAPHOBJECT,       "3",                     $DICT,      $L___, T_SCENEGRAPHOBJECT,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NL_ETC,                   T_STRING,                $NULL,      $RW_,  null,
       NL_TAG,                   T_STRING,                $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,


    //
    // Suspect we only need one variation of Java3D Points and Vectors.
    // The NY_CLASSNAME is a recent addition that tells the interpreter
    // to try to make sure the template that ends up in typedict really
    // is a J3DObject. Part of the job also removes NY_CLASSNAME from
    // the dictionary template so it doesn't waste a slot to save the
    // NY_CLASSNAME field because (in these cases) it won't be needed
    // again.
    //

       T_POINT3D,                "4",                     $DICT,      $L___, T_POINT3D,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NL_X,                     "0.0",                   $DOUBLE,    $RW_,  null,
       NL_Y,                     "0.0",                   $DOUBLE,    $RW_,  null,
       NL_Z,                     "0.0",                   $DOUBLE,    $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_QUAT4D,                 "5",                     $DICT,      $L___, T_QUAT4D,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NL_W,                     "0.0",                   $DOUBLE,    $RW_,  null,
       NL_X,                     "0.0",                   $DOUBLE,    $RW_,  null,
       NL_Y,                     "0.0",                   $DOUBLE,    $RW_,  null,
       NL_Z,                     "0.0",                   $DOUBLE,    $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_VECTOR3D,               "4",                     $DICT,      $L___, T_VECTOR3D,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NL_X,                     "0.0",                   $DOUBLE,    $RW_,  null,
       NL_Y,                     "0.0",                   $DOUBLE,    $RW_,  null,
       NL_Z,                     "0.0",                   $DOUBLE,    $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_AXISANGLE,              "5",                     $DICT,      $L___, T_AXISANGLE,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NL_ANGLE,                 "0.0",                   $DOUBLE,    $RW_,  null,
       NL_X,                     "0.0",                   $DOUBLE,    $RW_,  null,
       NL_Y,                     "0.0",                   $DOUBLE,    $RW_,  null,
       NL_Z,                     "0.0",                   $DOUBLE,    $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

    //
    // There are lots of different Euler angle conventions and many name
    // the angles phi, theta, and psi, however what the angles mean for a
    // particular convention (e.g., ZXZ) isn't well defined. We decided to
    // use alpha, beta, and gamma (mentioned in several references) and will
    // always assume that rotations are applied in that order no matter what
    // Euler angle convention is being used. We currently only support one
    // convention (Java's) but that could change. The alternative to picking
    // selecting angle names would be to use a three element array and just
    // let the order of the elements in that array match the order that the
    // rotations (for a particular convention) are applied, which is pretty
    // much what Java did. The websites
    //
    //    http://mathworld.wolfram.com/EulerAngles.html
    //    http://www.euclideanspace.com/maths/geometry/rotations/euler/
    //    http://ccp14.minerals.csiro.au/ccp/web-mirrors/klaus_eichele_software/klaus/nmr/conventions/euler/euler.html
    //
    // are a good place to start if you want more information about Euler
    // angles.
    //
    // NOTE - we're considering only directly supporting Java's convention
    // and if we do we may also change the fields to NL_X, NL_Y, and NL_Z.
    // Not urgent so we're not going to rush any changes.
    //

       T_EULERANGLE,             "5",                     $DICT,      $L___, T_EULERANGLE,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NL_ALPHA,                 "0.0",                   $DOUBLE,    $RW_,  null,
       NL_BETA,                  "0.0",                   $DOUBLE,    $RW_,  null,
       NL_GAMMA,                 "0.0",                   $DOUBLE,    $RW_,  null,
       NL_CONVENTION,            "0",                     $INTEGER,   $LR__, null,
       null,                     null,                    $TYPEDEF,   null,  null,

    //
    // We eventually expect this will be a straightforward interface to the
    // Java3D Transform3D class, but for now we only provide very limited
    // capabilites.
    //

       T_TRANSFORM3D,            "53",                    $DICT,      $L___, T_TRANSFORM3D,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $TRANSFORM3D,            $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_AUTONORMALIZE,         $FALSE,                  $INTEGER,   $RW_,  null,
       NL_DETERMINANT,           "0.0",                   $DOUBLE,    $LR__, null,
       NL_DTRANSFORM,            T_CALLABLE,              $NULL,      $L__X, null,
       NL_EQUALS,                T_CALLABLE,              $NULL,      $L__X, null,
       NY_INITIALIZER,           T_CALLABLE,              $NULL,      $RWX,  null,
       NL_INVERT,                T_CALLABLE,              $NULL,      $L__X, null,
       NL_IDTRANSFORM,           T_CALLABLE,              $NULL,      $L__X, null,
       NL_ITRANSFORM,            T_CALLABLE,              $NULL,      $L__X, null,
       NL_MUL,                   T_CALLABLE,              $NULL,      $L__X, null,
       NL_NORMALIZE,             T_CALLABLE,              $NULL,      $L__X, null,
       NL_ROTATEX,               T_CALLABLE,              $NULL,      $L__X, null,
       NL_ROTATEY,               T_CALLABLE,              $NULL,      $L__X, null,
       NL_ROTATEZ,               T_CALLABLE,              $NULL,      $L__X, null,
       NL_SCALE,                 T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOEULER,            T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOFRUSTUM,          T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOIDENTITY,         T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOLOOKAT,           T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOORTHO,            T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOPERSPECTIVE,      T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOROTATIONX,        T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOROTATIONY,        T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOROTATIONZ,        T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOSCALE,            T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOSHEARX,           T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOSHEARY,           T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOSHEARZ,           T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOTRANSFORM,        T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOTRANSLATION,      T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOVIEWAT,           T_CALLABLE,              $NULL,      $L__X, null,
       NL_SETTOZERO,             T_CALLABLE,              $NULL,      $L__X, null,
       NL_SHEARX,                T_CALLABLE,              $NULL,      $L__X, null,
       NL_SHEARY,                T_CALLABLE,              $NULL,      $L__X, null,
       NL_SHEARZ,                T_CALLABLE,              $NULL,      $L__X, null,
       NL_SHXY,                  "0.0",                   $DOUBLE,    $RW_,  null,
       NL_SHXZ,                  "0.0",                   $DOUBLE,    $RW_,  null,
       NL_SHYX,                  "0.0",                   $DOUBLE,    $RW_,  null,
       NL_SHYZ,                  "0.0",                   $DOUBLE,    $RW_,  null,
       NL_SHZX,                  "0.0",                   $DOUBLE,    $RW_,  null,
       NL_SHZY,                  "0.0",                   $DOUBLE,    $RW_,  null,
       NL_SX,                    "1.0",                   $DOUBLE,    $RW_,  null,
       NL_SY,                    "1.0",                   $DOUBLE,    $RW_,  null,
       NL_SZ,                    "1.0",                   $DOUBLE,    $RW_,  null,
       NL_TRANSFORM,             T_CALLABLE,              $NULL,      $L__X, null,
       NL_TRANSLATE,             T_CALLABLE,              $NULL,      $L__X, null,
       NL_TRANSPOSE,             T_CALLABLE,              $NULL,      $L__X, null,
       NL_TX,                    "0.0",                   $DOUBLE,    $RW_,  null,
       NL_TY,                    "0.0",                   $DOUBLE,    $RW_,  null,
       NL_TYPE,                  T_ARRAY,                 $NULL,      $LR__, null,
       NL_TZ,                    "0.0",                   $DOUBLE,    $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

    //
    // Universes - the background stuff is a recent addition (it was moved
    // from Canvas3D) that probably will change.
    //

       T_VIRTUALUNIVERSE,        "11",                    $DICT,      $L___, T_VIRTUALUNIVERSE,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $VIRTUALUNIVERSE,        $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_BACKGROUND,            T_COLOR,                 $NULL,      $RW_,  null,
       NL_BACKGROUNDHINTS,       $TILEHINT,               $INTEGER,   $RW_,  null,
       NL_BACKGROUNDIMAGE,       T_IMAGE,                 $NULL,      $RW_,  null,
       NL_COMPILE,               $FALSE,                  $INTEGER,   $RW_,  null,
       NL_LAYOUT,                T_ARRAY,                 $NULL,      $RW_,  null,
       NL_LOADERS,               T_DICT,                  $NULL,      $RW_,  null,
       NL_PRIORITY,              $J3D_PRIORITY,           $INTEGER,   $RW_,  null,
       NL_TAGGED,                T_DICT,                  $NULL,      $LR__, null,
       null,                     null,                    $TYPEDEF,   null,  null,

    //
    // Type templates - $TYPEDEF lines are required and must be last. The
    // NY_CLASSNAME, NY_MAJOR, and NY_MINOR fields are special and must be
    // properly set (the way we've done here) when the object that you're
    // describing depends on a Java class that extends YoixPointerActive.
    //
    // NOTE - changing the various background fields doesn't accomplish
    // anything but at least one is needed otherwise YoixBodyComponent
    // may misbehave (null pointer exception in pickBackground()), so we
    // removed write permission from all of them. We will investigate and
    // probably fix YoixBodyComponent.java in the near future.
    //

       T_CANVAS3D,               "35",                    $DICT,      $L___, T_CANVAS3D,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $COMPONENT,              $INTEGER,   $LR__, null,
       NY_MINOR,                 $CANVAS3D,               $INTEGER,   $LR__, null,
       NL_ALIVE,                 $TRUE,                   $INTEGER,   $RW_,  null,
       NY_BACKGROUND,            T_COLOR,                 $NULL,      $LR__, null,
       NY_BACKGROUNDHINTS,       $TILEHINT,               $INTEGER,   $LR__, null,
       NY_BACKGROUNDIMAGE,       T_IMAGE,                 $NULL,      $LR__, null,
       NY_CURSOR,                $STANDARDCURSOR,         $OBJECT,    $RW_,  null,
       NL_DOUBLEBUFFERED,        T_OBJECT,                $NULL,      $RW_,  null,
       NY_ENABLED,               $TRUE,                   $INTEGER,   $RW_,  null,
       NY_ETC,                   T_OBJECT,                $NULL,      $LR__, null,
       NY_FONT,                  T_OBJECT,                $NULL,      $RW_,  null,
       NY_FOREGROUND,            T_COLOR,                 $NULL,      $RW_,  null,
       NY_GRAPHICS,              T_GRAPHICS,              $NULL,      $RW_,  null,
       NY_LOCATION,              T_POINT,                 $NULL,      $RW_,  null,
       NY_MAXIMUMSIZE,           T_DIMENSION,             $NULL,      $RW_,  null,
       NY_MINIMUMSIZE,           T_DIMENSION,             $NULL,      $RW_,  null,
       NL_OFFSCREENBUFFER,       T_IMAGE,                 $NULL,      $RW_,  null,
       NY_PAINT,                 T_CALLABLE,              $NULL,      $RWX,  null,
       NY_POPUP,                 T_POPUPMENU,             $NULL,      $RW_,  null,
       NL_POSTRENDER,            T_CALLABLE,              $NULL,      $RWX,  null,
       NL_POSTSWAP,              T_CALLABLE,              $NULL,      $RWX,  null,
       NY_PREFERREDSIZE,         T_DIMENSION,             $NULL,      $RW_,  null,
       NL_PRERENDER,             T_CALLABLE,              $NULL,      $RWX,  null,
       NL_PROPERTIES,            T_DICT,                  $NULL,      $LR__, null,
       NL_RENDERFIELD,           T_CALLABLE,              $NULL,      $RWX,  null,
       NY_REPAINT,               T_CALLABLE,              $NULL,      $L__X, null,
       NY_REQUESTFOCUS,          $FALSE,                  $INTEGER,   $RW_,  null,
       NY_ROOT,                  T_OBJECT,                $NULL,      $LR__, null,
       NY_SHOWING,               $FALSE,                  $INTEGER,   $LR__, null,
       NY_SIZE,                  T_DIMENSION,             $NULL,      $RW_,  null,
       NY_TAG,                   T_STRING,                $NULL,      $RW_,  null,
       NL_UNIVERSE,              T_OBJECT,                $NULL,      $LR__, null,
       NL_VIEW,                  T_OBJECT,                $NULL,      $RW_,  null,
       NY_VISIBLE,               $TRUE,                   $INTEGER,   $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

    //
    // Groups...
    //

       T_BRANCHGROUP,            "21",                    $DICT,      $L___, T_BRANCHGROUP,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $BRANCHGROUP,            $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COMPILE,               $FALSE,                  $INTEGER,   $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_COLLIDABLE,            $TRUE,                   $INTEGER,   $RW_,  null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_DETACH,                T_CALLABLE,              $NULL,      $L__X, null,
       NL_INTERPOLATOR,          T_OBJECT,                $NULL,      $RW_,  null,
       NL_LAYOUT,                T_ARRAY,                 $NULL,      $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_LOCATION,              T_POINT3D,               $NULL,      $LR__, null,
       NL_LOADERS,               T_DICT,                  $NULL,      $RW_,  null,
       NL_ORIENTATION,           T_OBJECT,                $NULL,      $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_PICKABLE,              $TRUE,                   $INTEGER,   $RW_,  null,
       NL_POSITION,              T_POINT3D,               $NULL,      $RW_,  null,
       NL_TAG,                   T_STRING,                $NULL,      $RW_,  null,
       NL_TAGGED,                T_DICT,                  $NULL,      $LR__, null,
       NL_TRANSFORM,             T_TRANSFORM3D,           $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_VIEWPLATFORM,           "23",                    $DICT,      $L___, T_VIEWPLATFORM,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $VIEWPLATFORM,           $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_ACTIVATIONRADIUS,      T_OBJECT,                $NULL,      $RW_,  null,
       NL_BACKCLIPDISTANCE,      T_OBJECT,                $NULL,      $RW_,  null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COMPILE,               $FALSE,                  $INTEGER,   $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_FIELDOFVIEW,           $NAN,                    $DOUBLE,    $LR__, null,
       NL_FRONTCLIPDISTANCE,     T_OBJECT,                $NULL,      $RW_,  null,
       NL_INTERPOLATOR,          T_OBJECT,                $NULL,      $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_LOCALEYELIGHTING,      $FALSE,                  $INTEGER,   $RW_,  null,
       NL_LOCATION,              T_POINT3D,               $NULL,      $LR__, null,
       NL_MOVEMENTPOLICY,        $J3D_PHYSICAL_WORLD,     $OBJECT,    $RW_,  null,
       NL_ORIENTATION,           T_OBJECT,                $NULL,      $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_POSITION,              T_POINT3D,               $NULL,      $RW_,  null,
       NL_RESIZEPOLICY,          $J3D_PHYSICAL_WORLD,     $OBJECT,    $RW_,  null,
       NL_TAG,                   T_STRING,                $NULL,      $RW_,  null,
       NL_TAGGED,                T_DICT,                  $NULL,      $LR__, null,
       NL_TRANSFORM,             T_TRANSFORM3D,           $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

    //
    // Locale - currently unused and not conviced it's needed.
    //

       T_LOCALE3D,               "6",                     $DICT,      $L___, T_LOCALE3D,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $LOCALE3D,               $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_LAYOUT,                T_ARRAY,                 $NULL,      $RW_,  null,
       NL_LOCATION,              T_DICT,                  $NULL,      $RW_,  null,
       NL_TAGGED,                T_DICT,                  $NULL,      $LR__, null,
       null,                     null,                    $TYPEDEF,   null,  null,

    //
    // New stuff
    //

       T_ALPHA,                  "19",                    $DICT,      $L___, T_ALPHA,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $ALPHA,                  $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_ALIVE,                 $TRUE,                   $INTEGER,   $RW_,  null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_LOOP,                  "-1",                    $INTEGER,   $RW_,  null,
       NL_MODE,                  $J3D_INCREASING,         $INTEGER,   $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_PAUSETIME,             "0.0",                   $DOUBLE,    $RW_,  null,
       NL_PHASEDELAY,            "0.0",                   $DOUBLE,    $RW_,  null,
       NL_RESUMETIME,            "0.0",                   $DOUBLE,    $RW_,  null,
       NL_RUN,                   $TRUE,                   $INTEGER,   $RW_,  null,
       NL_STARTTIME,             "0,0",                   $DOUBLE,    $RW_,  null,
       NL_TRIGGERTIME,           "0.0",                   $DOUBLE,    $RW_,  null,
       NL_VALUE,                 T_CALLABLE,              $NULL,      $L__X, null,
       NL_WAVEFORM,              T_OBJECT,                $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_BOUNDS,                 "8",                     $DICT,      $L___, T_BOUNDS,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $BOUNDS,                 $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CENTER,                T_POINT3D,               $NULL,      $RW_,  null,
       NL_LOWER,                 T_POINT3D,               $NULL,      $RW_,  null,
       NL_RADIUS,                "1",                     $DOUBLE,    $RW_,  null,
       NL_TYPE,                  $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_UPPER,                 T_POINT3D,               $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_CONFIGOLATOR,           "8",                     $DICT,      $L___, T_CONFIGOLATOR,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NL_ALPHA,                 T_ALPHA,                 $NULL,      $RW_,  null,
       NL_AXIS,                  T_TRANSFORM3D,           $NULL,      $RW_,  null,
       NL_ENABLED,               $TRUE,                   $INTEGER,   $RW_,  null,
       NL_KNOTS,                 T_ARRAY,                 $NULL,      $RW_,  null,
       NL_PROCESSSTIMULUS,       T_CALLABLE,              $NULL,      $RWX,  null,
       NL_VALUES,                T_OBJECT,                $NULL,      $RW_,  null,
       NL_TYPE,                  "0",                     $INTEGER,   $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_INTERPOLATOR,           "12",                    $DICT,      $L___, T_INTERPOLATOR,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $INTERPOLATOR,           $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_ALPHA,                 T_ALPHA,                 $NULL,      $RW_,  null,
       NL_BOUNDINGLEAF,          T_BOUNDS,                $NULL,      $RW_,  null,
       NL_BOUNDS,                T_BOUNDS,                $NULL,      $RW_,  null, // T_OBJECT?
       NL_CONFIGURATION,         T_OBJECT,                $NULL,      $RW_,  null,
       NL_INITIALIZE,            T_CALLABLE,              $NULL,      $RWX,  null,
       NL_POSTPROCESSSTIMULUS,   T_CALLABLE,              $NULL,      $RWX,  null,
       NL_PREPROCESSSTIMULUS,    T_CALLABLE,              $NULL,      $RWX,  null,
       NL_TARGET,                T_OBJECT,                $NULL,      $LR__, null,
       NL_WAKEUP,                T_ARRAY,                 $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_WAKEUP,                 "4",                     $DICT,      $L___, T_WAKEUP,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $WAKEUP,                 $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_TYPE,                  "0",                     $INTEGER,   $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

    //
    // Appearance related stuff. Some of the attributes may eventually be
    // combined (e.g., PointAttributes moved into RenderingAttributes) and
    // we may even decide to rename some types (e.g., RenderingAttributes
    // might be called Graphics). No rush and need to think about it some.
    //

       T_COLORINGATTRIBUTES,     "10",                    $DICT,      $L___, T_COLORINGATTRIBUTES,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $COLORINGATTRIBUTES,     $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLOR,                 T_COLOR,                 $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_SHADEMODEL,            $J3D_GOURAUD,            $OBJECT,    $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_LINEATTRIBUTES,         "9",                     $DICT,      $L___, T_LINEATTRIBUTES,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $LINEATTRIBUTES,         $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_ANTIALIASING,          $FALSE,                  $INTEGER,   $RW_,  null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_MATERIAL,               "15",                    $DICT,      $L___, T_MATERIAL,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $MATERIAL,               $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_AMBIENTCOLOR,          T_COLOR,                 $NULL,      $RW_,  null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLORTARGET,           $J3D_DIFFUSE,            $OBJECT,    $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_DIFFUSECOLOR,          T_COLOR,                 $NULL,      $RW_,  null,
       NL_EMISSIVECOLOR,         T_COLOR,                 $NULL,      $RW_,  null,
       NL_ENABLED,               $TRUE,                   $INTEGER,   $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_SHININESS,             "0.5",                   $DOUBLE,    $RW_,  null,
       NL_SPECULARCOLOR,         T_COLOR,                 $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_POINTATTRIBUTES,        "10",                    $DICT,      $L___, T_POINTATTRIBUTES,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $POINTATTRIBUTES,        $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_ANTIALIASING,          $FALSE,                  $INTEGER,   $RW_,  null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_SIZE,                  "1.0",                   $DOUBLE,    $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_POLYGONATTRIBUTES,      "10",                    $DICT,      $L___, T_POLYGONATTRIBUTES,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $POLYGONATTRIBUTES,      $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_CULLING,               $J3D_BACK,               $OBJECT,    $RW_,  null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_MODE,                  $J3D_FILL,               $OBJECT,    $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_RENDERINGATTRIBUTES,    "10",                    $DICT,      $L___, T_RENDERINGATTRIBUTES,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $RENDERINGATTRIBUTES,    $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_IGNOREVERTEXCOLORS,    $FALSE,                  $INTEGER,   $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_VISIBLE,               $TRUE,                   $INTEGER,   $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_TEXTURE2D,              "25",                    $DICT,      $L___, T_TEXTURE2D,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $TEXTURE2D,              $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_ANISOTROPICDEGREE,     "1.0",                   $DOUBLE,    $RW_,  null,
       NL_BASELEVEL,             "0",                     $INTEGER,   $RW_,  null,
       NL_BOUNDARY,              T_DICT,                  $NULL,      $RW_,  null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_ENABLED,               $TRUE,                   $INTEGER,   $RW_,  null,
       NL_FILTER4,               T_ARRAY,                 $NULL,      $RW_,  null,
       NL_FORMAT,                $J3D_RGB,                $OBJECT,    $RW_,  null,
       NL_HEIGHT,                "0",                     $INTEGER,   $LR__, null,
       NL_IMAGE,                 T_OBJECT,                $NULL,      $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_LODOFFSET,             T_ARRAY,                 $NULL,      $RW_,  null,
       NL_LODRANGE,              T_ARRAY,                 $NULL,      $RW_,  null,
       NL_MAGFILTER,             $J3D_BASE_LEVEL_LINEAR,  $OBJECT,    $RW_,  null,
       NL_MAXIMUMLEVEL,          $MAXINT,                 $INTEGER,   $RW_,  null,
       NL_MINFILTER,             $J3D_BASE_LEVEL_LINEAR,  $OBJECT,    $RW_,  null,
       NL_MIPMAP,                $FALSE,                  $INTEGER,   $RW_,  null,
       NL_MIPMAPLEVELS,          "1",                     $INTEGER,   $LR__, null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_SHARPEN,               T_ARRAY,                 $NULL,      $RW_,  null,
       NL_WIDTH,                 "0",                     $INTEGER,   $LR__, null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_TEXTUREUNIT,            "15",                    $DICT,      $L___, T_TEXTUREUNIT,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $TEXTUREUNIT,            $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COMBINEALPHAMODE,      $J3D_MODULATE,           $OBJECT,    $RW_,  null,
       NL_COMBINERGBMODE,        $J3D_MODULATE,           $OBJECT,    $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_ENABLED,               $TRUE,                   $INTEGER,   $RW_,  null,
       NL_GENERATIONMODE,        $J3D_OBJECT_LINEAR,      $OBJECT,    $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_TEXTURE,               T_OBJECT,                $NULL,      $RW_,  null,
       NL_TEXTUREMODE,           $J3D_REPLACE,            $OBJECT,    $RW_,  null,
       NL_TRANSFORM,             T_TRANSFORM3D,           $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_TRANSPARENCYATTRIBUTES, "12",                    $DICT,      $L___, T_TRANSPARENCYATTRIBUTES,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $TRANSPARENCYATTRIBUTES, $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_DESTINATIONBLEND,      $J3D_ONE_MINUS_ALPHA,    $OBJECT,    $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_MODE,                  $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_SOURCEBLEND,           $J3D_ALPHA,              $OBJECT,    $RW_,  null,
       NL_VALUE,                 "0.0",                   $DOUBLE,    $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_APPEARANCE,             "16",                    $DICT,      $L___, T_APPEARANCE,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $APPEARANCE,             $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLORING,              T_COLORINGATTRIBUTES,    $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_LINES,                 T_LINEATTRIBUTES,        $NULL,      $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_MATERIAL,              T_MATERIAL,              $NULL,      $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_POINTS,                T_POINTATTRIBUTES,       $NULL,      $RW_,  null,
       NL_POLYGONS,              T_POLYGONATTRIBUTES,     $NULL,      $RW_,  null,
       NL_RENDERING,             T_RENDERINGATTRIBUTES,   $NULL,      $RW_,  null,
       NL_TEXTUREUNIT,           T_OBJECT,                $NULL,      $RW_,  null,
       NL_TRANSPARENCY,          T_TRANSPARENCYATTRIBUTES,$NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

    //
    // Geometry related types - chance we'll eventually fold all these
    // into a single type (e.g., T_GEOMETRYARRAY) and use a type field
    // identify the low level details.
    //

       T_POINTARRAY,             "14",                    $DICT,      $L___, T_POINTARRAY,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $POINTARRAY,             $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLORFORMAT,           $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_COLORS,                T_ARRAY,                 $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_COORDINATES,           T_ARRAY,                 $NULL,      $RW_,  null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_TEXTURECOORDINATES,    T_ARRAY,                 $NULL,      $RW_,  null,
       NL_TEXTUREFORMAT,         $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_VERTEXCOUNT,           "-1",                    $INTEGER,   $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_LINEARRAY,              "14",                    $DICT,      $L___, T_LINEARRAY,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $LINEARRAY,              $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLORFORMAT,           $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_COLORS,                T_ARRAY,                 $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_COORDINATES,           T_ARRAY,                 $NULL,      $RW_,  null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_TEXTURECOORDINATES,    T_ARRAY,                 $NULL,      $RW_,  null,
       NL_TEXTUREFORMAT,         $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_VERTEXCOUNT,           "-1",                    $INTEGER,   $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_TRIANGLEARRAY,          "16",                    $DICT,      $L___, T_TRIANGLEARRAY,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $TRIANGLEARRAY,          $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLORFORMAT,           $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_COLORS,                T_ARRAY,                 $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_COORDINATES,           T_ARRAY,                 $NULL,      $RW_,  null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_GENERATENORMALS,       $TRUE,                   $INTEGER,   $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_NORMALS,               T_ARRAY,                 $NULL,      $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_TEXTURECOORDINATES,    T_ARRAY,                 $NULL,      $RW_,  null,
       NL_TEXTUREFORMAT,         $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_VERTEXCOUNT,           "-1",                    $INTEGER,   $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_QUADARRAY,              "16",                    $DICT,      $L___, T_QUADARRAY,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $QUADARRAY,              $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLORFORMAT,           $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_COLORS,                T_ARRAY,                 $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_COORDINATES,           T_ARRAY,                 $NULL,      $RW_,  null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_GENERATENORMALS,       $TRUE,                   $INTEGER,   $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_NORMALS,               T_ARRAY,                 $NULL,      $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_TEXTURECOORDINATES,    T_ARRAY,                 $NULL,      $RW_,  null,
       NL_TEXTUREFORMAT,         $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_VERTEXCOUNT,           "-1",                    $INTEGER,   $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_LINESTRIPARRAY,         "15",                    $DICT,      $L___, T_LINESTRIPARRAY,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $LINESTRIPARRAY,         $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLORFORMAT,           $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_COLORS,                T_ARRAY,                 $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_COORDINATES,           T_ARRAY,                 $NULL,      $RW_,  null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_STRIPVERTEXCOUNTS,     T_ARRAY,                 $NULL,      $RW_,  null,
       NL_TEXTURECOORDINATES,    T_ARRAY,                 $NULL,      $RW_,  null,
       NL_TEXTUREFORMAT,         $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_VERTEXCOUNT,           "-1",                    $INTEGER,   $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_TRIANGLEFANARRAY,       "17",                    $DICT,      $L___, T_TRIANGLEFANARRAY,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $TRIANGLEFANARRAY,       $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLORFORMAT,           $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_COLORS,                T_ARRAY,                 $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_COORDINATES,           T_ARRAY,                 $NULL,      $RW_,  null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_GENERATENORMALS,       $TRUE,                   $INTEGER,   $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_NORMALS,               T_ARRAY,                 $NULL,      $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_STRIPVERTEXCOUNTS,     T_ARRAY,                 $NULL,      $RW_,  null,
       NL_TEXTURECOORDINATES,    T_ARRAY,                 $NULL,      $RW_,  null,
       NL_TEXTUREFORMAT,         $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_VERTEXCOUNT,           "-1",                    $INTEGER,   $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_TRIANGLESTRIPARRAY,     "17",                    $DICT,      $L___, T_TRIANGLESTRIPARRAY,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $TRIANGLESTRIPARRAY,     $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLORFORMAT,           $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_COLORS,                T_ARRAY,                 $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_COORDINATES,           T_ARRAY,                 $NULL,      $RW_,  null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_GENERATENORMALS,       $TRUE,                   $INTEGER,   $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_NORMALS,               T_ARRAY,                 $NULL,      $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_STRIPVERTEXCOUNTS,     T_ARRAY,                 $NULL,      $RW_,  null,
       NL_TEXTURECOORDINATES,    T_ARRAY,                 $NULL,      $RW_,  null,
       NL_TEXTUREFORMAT,         $J3D_NONE,               $OBJECT,    $RW_,  null,
       NL_VERTEXCOUNT,           "-1",                    $INTEGER,   $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

    //
    // Currently only use Shape3D as a type name, but that undoubtedly will
    // change.
    //

       T_SHAPE3D,                "19",                    $DICT,      $L___, T_SHAPE3D,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $SHAPE3D,                $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_APPEARANCE,            T_APPEARANCE,            $NULL,      $RW_,  null,
       NL_APPEARANCEOVERRIDE,    $FALSE,                  $INTEGER,   $RW_,  null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLLIDABLE,            $TRUE,                   $INTEGER,   $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_GEOMETRY,              T_OBJECT,                $NULL,      $RWX,  null,
       NL_INTERPOLATOR,          T_OBJECT,                $NULL,      $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_LOCATION,              T_POINT3D,               $NULL,      $LR__, null,
       NL_ORIENTATION,           T_OBJECT,                $NULL,      $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_PICKABLE,              $TRUE,                   $INTEGER,   $RW_,  null,
       NL_POSITION,              T_POINT3D,               $NULL,      $RW_,  null,
       NL_TAG,                   T_STRING,                $NULL,      $RW_,  null,
       NL_TRANSFORM,             T_TRANSFORM3D,           $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_COLORCUBE,              "17",                    $DICT,      $L___, T_COLORCUBE,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $COLORCUBE,              $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLLIDABLE,            $TRUE,                   $INTEGER,   $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_INTERPOLATOR,          T_OBJECT,                $NULL,      $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_LOCATION,              T_POINT3D,               $NULL,      $LR__, null,
       NL_ORIENTATION,           T_OBJECT,                $NULL,      $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_PICKABLE,              $TRUE,                   $INTEGER,   $RW_,  null,
       NL_POSITION,              T_POINT3D,               $NULL,      $RW_,  null,
       NL_SCALE,                 "1.0",                   $DOUBLE,    $RW_,  null,
       NL_TAG,                   T_STRING,                $NULL,      $RW_,  null,
       NL_TRANSFORM,             T_TRANSFORM3D,           $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_SPHERE,                 "22",                    $DICT,      $L___, T_SPHERE,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $SPHERE,                 $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_APPEARANCE,            T_APPEARANCE,            $NULL,      $RW_,  null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLLIDABLE,            $TRUE,                   $INTEGER,   $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_DIVISIONS,             "16",                    $INTEGER,   $RW_,  null,
       NL_GENERATENORMALS,       "1",                     $INTEGER,   $RW_,  null,
       NL_GENERATETEXTURECOORDS, $FALSE,                  $INTEGER,   $RW_,  null,
       NL_INTERPOLATOR,          T_OBJECT,                $NULL,      $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_LOCATION,              T_POINT3D,               $NULL,      $LR__, null,
       NL_ORIENTATION,           T_OBJECT,                $NULL,      $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_PICKABLE,              $TRUE,                   $INTEGER,   $RW_,  null,
       NL_POSITION,              T_POINT3D,               $NULL,      $RW_,  null,
       NL_RADIUS,                "1.0",                   $DOUBLE,    $RW_,  null,
       NL_SHARED,                $TRUE,                   $INTEGER,   $RW_,  null,
       NL_TAG,                   T_STRING,                $NULL,      $RW_,  null,
       NL_TRANSFORM,             T_TRANSFORM3D,           $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_CONE,                   "23",                    $DICT,      $L___, T_CONE,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $CONE,                   $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_APPEARANCE,            T_OBJECT,                $NULL,      $RW_,  null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLLIDABLE,            $TRUE,                   $INTEGER,   $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_DIVISIONS,             "16",                    $INTEGER,   $ARW_, null,
       NL_GENERATENORMALS,       "1",                     $INTEGER,   $RW_,  null,
       NL_GENERATETEXTURECOORDS, $FALSE,                  $INTEGER,   $RW_,  null,
       NL_HEIGHT,                "2.0",                   $DOUBLE,    $RW_,  null,
       NL_INTERPOLATOR,          T_OBJECT,                $NULL,      $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_LOCATION,              T_POINT3D,               $NULL,      $LR__, null,
       NL_ORIENTATION,           T_OBJECT,                $NULL,      $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_PICKABLE,              $TRUE,                   $INTEGER,   $RW_,  null,
       NL_POSITION,              T_POINT3D,               $NULL,      $RW_,  null,
       NL_RADIUS,                "1.0",                   $DOUBLE,    $RW_,  null,
       NL_SHARED,                $TRUE,                   $INTEGER,   $RW_,  null,
       NL_TAG,                   T_STRING,                $NULL,      $RW_,  null,
       NL_TRANSFORM,             T_TRANSFORM3D,           $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_CYLINDER,               "23",                    $DICT,      $L___, T_CYLINDER,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $CYLINDER,               $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_APPEARANCE,            T_OBJECT,                $NULL,      $RW_,  null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLLIDABLE,            $TRUE,                   $INTEGER,   $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_DIVISIONS,             "16",                    $INTEGER,   $ARW_, null,
       NL_GENERATENORMALS,       "1",                     $INTEGER,   $RW_,  null,
       NL_GENERATETEXTURECOORDS, $FALSE,                  $INTEGER,   $RW_,  null,
       NL_HEIGHT,                "2.0",                   $DOUBLE,    $RW_,  null,
       NL_INTERPOLATOR,          T_OBJECT,                $NULL,      $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_LOCATION,              T_POINT3D,               $NULL,      $LR__, null,
       NL_ORIENTATION,           T_OBJECT,                $NULL,      $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_PICKABLE,              $TRUE,                   $INTEGER,   $RW_,  null,
       NL_POSITION,              T_POINT3D,               $NULL,      $RW_,  null,
       NL_RADIUS,                "1.0",                   $DOUBLE,    $RW_,  null,
       NL_SHARED,                $TRUE,                   $INTEGER,   $RW_,  null,
       NL_TAG,                   T_STRING,                $NULL,      $RW_,  null,
       NL_TRANSFORM,             T_TRANSFORM3D,           $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_TEXT2D,                 "20",                    $DICT,      $L___, T_TEXT2D,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $TEXT2D,                 $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COLLIDABLE,            $TRUE,                   $INTEGER,   $RW_,  null,
       NL_COLOR,                 T_COLOR,                 $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_FONT,                  T_OBJECT,                $NULL,      $RW_,  null,
       NL_INTERPOLATOR,          T_OBJECT,                $NULL,      $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_LOCATION,              T_POINT3D,               $NULL,      $LR__, null,
       NL_ORIENTATION,           T_OBJECT,                $NULL,      $RW_,  null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_PICKABLE,              $TRUE,                   $INTEGER,   $RW_,  null,
       NL_POSITION,              T_POINT3D,               $NULL,      $RW_,  null,
       NL_SCALE,                 $TEXT2DSCALE,            $DOUBLE,    $RW_,  null,
       NL_TAG,                   T_STRING,                $NULL,      $RW_,  null,
       NL_TEXT,                  T_STRING,                $NULL,      $RW_,  null,
       NL_TRANSFORM,             T_TRANSFORM3D,           $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

    //
    // Lights...
    //

       T_AMBIENTLIGHT,           "13",                    $DICT,      $L___, T_AMBIENTLIGHT,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $AMBIENTLIGHT,           $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_BOUNDS,                T_BOUNDS,                $NULL,      $RW_,  null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_COLOR,                 T_COLOR,                 $NULL,      $RW_,  null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_ENABLED,               $FALSE,                  $INTEGER,   $RW_,  null,
       NL_INTERPOLATOR,          T_OBJECT,                $NULL,      $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_TAG,                   T_STRING,                $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_DIRECTIONALLIGHT,       "14",                    $DICT,      $L___, T_DIRECTIONALLIGHT,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $DIRECTIONALLIGHT,       $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_BOUNDS,                T_BOUNDS,                $NULL,      $RW_,  null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_COLOR,                 T_COLOR,                 $NULL,      $RW_,  null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_DIRECTION,             T_VECTOR3D,              $NULL,      $RW_,  null,
       NL_ENABLED,               $FALSE,                  $INTEGER,   $RW_,  null,
       NL_INTERPOLATOR,          T_OBJECT,                $NULL,      $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_TAG,                   T_STRING,                $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,

       T_POINTLIGHT,             "15",                    $DICT,      $L___, T_POINTLIGHT,
       null,                     "-1",                    $GROWTO,    null,  null,
       NY_CLASSNAME,             $CLASSNAME,              $CLASS,     $RORO, null,
       NY_MAJOR,                 $POINTLIGHT,             $INTEGER,   $LR__, null,
       NY_MINOR,                 "0",                     $INTEGER,   $LR__, null,
       NL_BOUNDS,                T_BOUNDS,                $NULL,      $RW_,  null,
       NL_CAPABILITIES,          T_DICT,                  $NULL,      $RW_,  null,
       NL_COMPILED,              $FALSE,                  $INTEGER,   $LR__, null,
       NL_COLOR,                 T_COLOR,                 $NULL,      $RW_,  null,
       NL_DEFAULTCAPABILITY,     "-1",                    $INTEGER,   $RW_,  null,
       NL_ENABLED,               $FALSE,                  $INTEGER,   $RW_,  null,
       NL_INTERPOLATOR,          T_OBJECT,                $NULL,      $RW_,  null,
       NL_LIVE,                  $FALSE,                  $INTEGER,   $LR__, null,
       NL_LOCATION,              T_POINT3D,               $NULL,      $LR__, null,
       NL_PATH,                  T_STRING,                $NULL,      $LR__, null,
       NL_POSITION,              T_POINT3D,               $NULL,      $RW_,  null,
       NL_TAG,                   T_STRING,                $NULL,      $RW_,  null,
       null,                     null,                    $TYPEDEF,   null,  null,
    };

    //
    // This gets filled in by YoixModule with the real values right after
    // the $module table is processed. It can then be used by the loaded()
    // method (defined below), which can handle some custom initialization.
    //

    public static Object  extracted[] = {
	$DEFAULTSCENELOADERS,
    };

    ///////////////////////////////////
    //
    // Module Methods
    //
    ///////////////////////////////////

    public static YoixObject
    generateStrips(YoixObject arg[]) {

	YoixObject  obj;

	return(VM.abort(UNIMPLEMENTED));
    }


    public static YoixObject
    getDefaultErrorScene(YoixObject arg[]) {

	YoixObject  obj;

	if ((obj = BodySceneLoader.getDefaultErrorScene()) == null)
	    obj = YoixObject.newNull();
	return(obj);
    }


    public static YoixObject
    getDefaultPostLoad(YoixObject arg[]) {

	YoixObject  obj;

	if ((obj = BodySceneLoader.getDefaultPostLoad()) == null)
	    obj = YoixObject.newNull();
	return(obj);
    }


    public static YoixObject
    getDefaultPreLoad(YoixObject arg[]) {

	YoixObject  obj;

	if ((obj = BodySceneLoader.getDefaultPreLoad()) == null)
	    obj = YoixObject.newNull();
	return(obj);
    }


    public static YoixObject
    getDefaultSceneLoaders(YoixObject arg[]) {

	YoixObject  obj;

	if ((obj = BodySceneLoader.getDefaultSceneLoaders()) == null)
	    obj = YoixObject.newNull();
	return(obj);
    }


    public static void
    loaded() {

	//
	// A method that the Yoix loader calls after all the module
	// loading dirty work is finished. We use it here to register
	// extracted objects with the appropriate support class, but
	// it could occasionally be a useful low level debugging tool.
	//

	if (extracted[0] instanceof YoixObject)
	    BodySceneLoader.setDefaultSceneLoaders((YoixObject)extracted[0]);
    }


    public static YoixObject
    setDefaultErrorScene(YoixObject arg[]) {

	if (arg[0].isString() || arg[0].isNull())
	    BodySceneLoader.setDefaultErrorScene(arg[0]);
	else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    setDefaultPostLoad(YoixObject arg[]) {

	if (arg[0].callable(2) || arg[0].isNull())
	    BodySceneLoader.setDefaultPostLoad(arg[0]);
	else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    setDefaultPreLoad(YoixObject arg[]) {

	if (arg[0].callable(1) || arg[0].isNull())
	    BodySceneLoader.setDefaultPreLoad(arg[0]);
	else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    setDefaultSceneLoaders(YoixObject arg[]) {

	if (arg[0].isDictionary() || arg[0].isNull())
	    BodySceneLoader.setDefaultSceneLoaders(arg[0]);
	else VM.badArgument(0);

	return(null);
    }
}

