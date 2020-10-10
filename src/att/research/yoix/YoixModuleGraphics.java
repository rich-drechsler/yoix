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

package att.research.yoix;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

abstract
class YoixModuleGraphics extends YoixModule

    implements YoixConstantsGraphics

{

    static String  $MODULENAME = M_GRAPHICS;

    static String  $YOIXCONSTANTSGRAPHICS = YOIXPACKAGE + ".YoixConstantsGraphics";

    //
    // The builtins in this module have been updated to work with Yoix
    // graphics model that's implemented using Java2D. In other words,
    // changing the graphics state associated with a drawable affects
    // the results you get from these builtins in the expected way.
    //

    static Integer  $BUTTCAP = new Integer(YOIX_CAP_BUTT);
    static Integer  $MITERJOIN = new Integer(YOIX_JOIN_MITER);
    static Integer  $SRCOVER = new Integer(YOIX_COMPOSITE_SRC_OVER);

    static Object  $module[] = {
    //
    // NAME                    ARG                  COMMAND     MODE   REFERENCE
    // ----                    ---                  -------     ----   ---------
       null,                   "46",                $LIST,      $RORO, $MODULENAME,
       $YOIXCONSTANTSGRAPHICS, "YOIX_\tYOIX_",      $READCLASS, $LR__, null,

       "clearRect",            "5",                 $BUILTIN,   $LR_X, null,
       "copyArea",             "-7",                $BUILTIN,   $LR_X, null,
       "drawArc",              "7",                 $BUILTIN,   $LR_X, null,
       "drawImage",            "4",                 $BUILTIN,   $LR_X, null,
       "drawLine",             "5",                 $BUILTIN,   $LR_X, null,
       "drawOval",             "5",                 $BUILTIN,   $LR_X, null,
       "drawPolygon",          "-3",                $BUILTIN,   $LR_X, null,
       "drawPolyline",         "-3",                $BUILTIN,   $LR_X, null,
       "drawRect",             "5",                 $BUILTIN,   $LR_X, null,
       "drawRoundRect",        "7",                 $BUILTIN,   $LR_X, null,
       "drawString",           "-4",                $BUILTIN,   $LR_X, null,
       "fillArc",              "7",                 $BUILTIN,   $LR_X, null,
       "fillOval",             "5",                 $BUILTIN,   $LR_X, null,
       "fillPolygon",          "-3",                $BUILTIN,   $LR_X, null,
       "fillRect",             "5",                 $BUILTIN,   $LR_X, null,
       "fillRoundRect",        "7",                 $BUILTIN,   $LR_X, null,
       "pointInPolygon",       "-3",                $BUILTIN,   $LR_X, null,
       "pointInRect",          "-2",                $BUILTIN,   $LR_X, null,
       "rectContainsRect",     "-2",                $BUILTIN,   $LR_X, null,
       "rectIntersectionRect", "-2",                $BUILTIN,   $LR_X, null,
       "rectIntersectsRect",   "-2",                $BUILTIN,   $LR_X, null,
       "rectUnionRect",        "-2",                $BUILTIN,   $LR_X, null,
       "stringBounds",         "-2",                $BUILTIN,   $LR_X, null,
       "stringWidth",          "2",                 $BUILTIN,   $LR_X, null,

       T_FONT,                 "25",                $DICT,      $L___, T_FONT,
       N_MAJOR,                $FONT,               $INTEGER,   $LR__, null,
       N_MINOR,                "0",                 $INTEGER,   $LR__, null,
       N_ADVANCE,              "0",                 $DOUBLE,    $LR__, null,
       N_ASCENT,               "0",                 $DOUBLE,    $LR__, null,
       N_DESCENT,              "0",                 $DOUBLE,    $LR__, null,
       N_HEIGHT,               "0",                 $DOUBLE,    $LR__, null,
       N_FAMILY,               T_STRING,            $NULL,      $LR__, null,
       N_FONTFACENAME,         T_STRING,            $NULL,      $LR__, null,
       N_LEADING,              "0",                 $DOUBLE,    $LR__, null,
       N_MATRIX,               T_MATRIX,            $NULL,      $LR__, null,
       N_NAME,                 T_STRING,            $NULL,      $RW_,  null,
       N_POINTSIZE,            "0",                 $DOUBLE,    $LR__, null,
       N_PSNAME,               T_STRING,            $NULL,      $LR__, null,
       N_ROTATEFONT,           T_CALLABLE,          $NULL,      $L__X, null,
       N_SCALEFONT,            T_CALLABLE,          $NULL,      $L__X, null,
       N_SHEARFONT,            T_CALLABLE,          $NULL,      $L__X, null,
       N_STRINGBOUNDS,         T_CALLABLE,          $NULL,      $L__X, null,
       N_STRINGFIT,            T_CALLABLE,          $NULL,      $L__X, null,
       N_STRINGSIZE,           T_CALLABLE,          $NULL,      $L__X, null,
       N_STRINGWIDTH,          T_CALLABLE,          $NULL,      $L__X, null,
       N_SIZE,                 "0",                 $INTEGER,   $LR__, null,
       N_STYLE,                T_STRING,            $NULL,      $LR__, null,
       N_TRANSFORMFONT,        T_CALLABLE,          $NULL,      $L__X, null,
       N_TRANSLATEFONT,        T_CALLABLE,          $NULL,      $L__X, null,
       N_TYPE,                 "0",                 $INTEGER,   $RW_,  null,

       T_PATH,                 "47",                $DICT,      $L___, T_PATH,
       null,                   "-1",                $GROWTO,    null,  null,
       N_MAJOR,                $PATH,               $INTEGER,   $LR__, null,
       N_MINOR,                "0",                 $INTEGER,   $LR__, null,
       N_ADD,                  T_CALLABLE,          $NULL,      $L__X, null,
       N_APPENDPATH,           T_CALLABLE,          $NULL,      $L__X, null,
       N_ARC,                  T_CALLABLE,          $NULL,      $L__X, null,
       N_ARCN,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_ARCT,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_CLOSEPATH,            T_CALLABLE,          $NULL,      $L__X, null,
       N_CTM,                  T_MATRIX,            $NULL,      $RW_,  null,
       N_CURRENTPATH,          T_CALLABLE,          $NULL,      $L__X, null,
       N_CURRENTPOINT,         T_CALLABLE,          $NULL,      $L__X, null,
       N_CURVETO,              T_CALLABLE,          $NULL,      $L__X, null,
       N_ELEMENTS,             T_ARRAY,             $NULL,      $LR__, null,
       N_EOADD,                T_CALLABLE,          $NULL,      $L__X, null,
       N_EOINTERSECT,          T_CALLABLE,          $NULL,      $L__X, null,
       N_EOINTERSECTS,         T_CALLABLE,          $NULL,      $L__X, null,
       N_EOSUBTRACT,           T_CALLABLE,          $NULL,      $L__X, null,
       N_EOXOR,                T_CALLABLE,          $NULL,      $L__X, null,
       N_FLATTENPATH,          T_CALLABLE,          $NULL,      $L__X, null,
       N_INEOFILL,             T_CALLABLE,          $NULL,      $L__X, null,
       N_INFILL,               T_CALLABLE,          $NULL,      $L__X, null,
       N_INSTROKE,             T_CALLABLE,          $NULL,      $L__X, null,
       N_INTERSECT,            T_CALLABLE,          $NULL,      $L__X, null,
       N_INTERSECTS,           T_CALLABLE,          $NULL,      $L__X, null,
       N_LINECAP,              $BUTTCAP,            $INTEGER,   $RW_,  null,
       N_LINEJOIN,             $MITERJOIN,          $INTEGER,   $RW_,  null,
       N_LINETO,               T_CALLABLE,          $NULL,      $L__X, null,
       N_LINEWIDTH,            "1.0",               $DOUBLE,    $RW_,  null,
       N_MITERLIMIT,           "10.0",              $DOUBLE,    $RW_,  null,
       N_MOVETO,               T_CALLABLE,          $NULL,      $L__X, null,
       N_NEWPATH,              T_CALLABLE,          $NULL,      $L__X, null,
       N_PAINT,                T_CALLABLE,          $NULL,      $RWX,  null,
       N_PATHBBOX,             T_CALLABLE,          $NULL,      $L__X, null,
       N_PATHFORALL,           T_CALLABLE,          $NULL,      $L__X, null,
       N_QUADTO,               T_CALLABLE,          $NULL,      $L__X, null,
       N_RCURVETO,             T_CALLABLE,          $NULL,      $L__X, null,
       N_RLINETO,              T_CALLABLE,          $NULL,      $L__X, null,
       N_RMOVETO,              T_CALLABLE,          $NULL,      $L__X, null,
       N_ROTATEPATH,           T_CALLABLE,          $NULL,      $L__X, null,
       N_RQUADTO,              T_CALLABLE,          $NULL,      $L__X, null,
       N_SCALEPATH,            T_CALLABLE,          $NULL,      $L__X, null,
       N_SHEARPATH,            T_CALLABLE,          $NULL,      $L__X, null,
       N_SUBTRACT,             T_CALLABLE,          $NULL,      $L__X, null,
       N_TRANSFORMPATH,        T_CALLABLE,          $NULL,      $L__X, null,
       N_TRANSLATEPATH,        T_CALLABLE,          $NULL,      $L__X, null,
       N_TRIMTOSTROKE,         T_CALLABLE,          $NULL,      $L__X, null,
       N_XOR,                  T_CALLABLE,          $NULL,      $L__X, null,

       T_GRAPHICS,             "120",               $DICT,      $L___, T_GRAPHICS,
       null,                   "-1",                $GROWTO,    null,  null,
       N_MAJOR,                $GRAPHICS,           $INTEGER,   $LR__, null,
       N_MINOR,                "0",                 $INTEGER,   $LR__, null,
       N_ANTIALIASING,         "-1",                $INTEGER,   $RW_,  null,
       N_APPENDPATH,           T_CALLABLE,          $NULL,      $L__X, null,
       N_ARC,                  T_CALLABLE,          $NULL,      $L__X, null,
       N_ARCN,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_ARCT,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_ASHOW,                T_CALLABLE,          $NULL,      $L__X, null,
       N_AWIDTHSHOW,           T_CALLABLE,          $NULL,      $L__X, null,
       N_BACKGROUND,           T_COLOR,             $NULL,      $RW_,  null,
       N_CHARPATH,             T_CALLABLE,          $NULL,      $L__X, null,
       N_CLIP,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_CLIPPATH,             T_CALLABLE,          $NULL,      $L__X, null,
       N_CLOSEPATH,            T_CALLABLE,          $NULL,      $L__X, null,
       N_COMPOSITERULE,        $SRCOVER,            $INTEGER,   $RW_,  null,
       N_CONCAT,               T_CALLABLE,          $NULL,      $L__X, null,
       N_CONCATMATRIX,         T_CALLABLE,          $NULL,      $L__X, null,
       N_CTM,                  T_MATRIX,            $NULL,      $RW_,  null,
       N_CURRENTMATRIX,        T_CALLABLE,          $NULL,      $L__X, null,
       N_CURRENTPATH,          T_CALLABLE,          $NULL,      $L__X, null,
       N_CURRENTPOINT,         T_CALLABLE,          $NULL,      $L__X, null,
       N_CURVETO,              T_CALLABLE,          $NULL,      $L__X, null,
       N_DASHARRAY,            T_ARRAY,             $NULL,      $RW_,  null,
       N_DASHPHASE,            "0.0",               $DOUBLE,    $RW_,  null,
       N_DIVIDEMATRIX,         T_CALLABLE,          $NULL,      $L__X, null,
       N_DRAWABLE,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAWABLEBBOX,         T_CALLABLE,          $NULL,      $L__X, null,
       N_DTRANSFORM,           T_CALLABLE,          $NULL,      $L__X, null,
       N_EOCLIP,               T_CALLABLE,          $NULL,      $L__X, null,
       N_EOERASE,              T_CALLABLE,          $NULL,      $L__X, null,
       N_EOFILL,               T_CALLABLE,          $NULL,      $L__X, null,
       N_EOINTERSECTS,         T_CALLABLE,          $NULL,      $L__X, null,
       N_ERASE,                T_CALLABLE,          $NULL,      $L__X, null,
       N_ERASEDRAWABLE,        T_CALLABLE,          $NULL,      $L__X, null,
       N_FILL,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_FLATTENPATH,          T_CALLABLE,          $NULL,      $L__X, null,
       N_FONT,                 T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,           T_OBJECT,            $NULL,      $RW_,  null,
       N_FRACTIONALMETRICS,    $FALSE,              $INTEGER,   $RW_,  null,
       N_GRESTORE,             T_CALLABLE,          $NULL,      $L__X, null,
       N_GRESTOREALL,          T_CALLABLE,          $NULL,      $L__X, null,
       N_GSAVE,                T_CALLABLE,          $NULL,      $L__X, null,
       N_IDENTMATRIX,          T_CALLABLE,          $NULL,      $L__X, null,
       N_IDTRANSFORM,          T_CALLABLE,          $NULL,      $L__X, null,
       N_IMAGEPATH,            T_CALLABLE,          $NULL,      $L__X, null,
       N_INEOFILL,             T_CALLABLE,          $NULL,      $L__X, null,
       N_INFILL,               T_CALLABLE,          $NULL,      $L__X, null,
       N_INITCLIP,             T_CALLABLE,          $NULL,      $L__X, null,
       N_INITGRAPHICS,         T_CALLABLE,          $NULL,      $L__X, null,
       N_INITIALIZER,          T_CALLABLE,          $NULL,      $RWX,  null,
       N_INITMATRIX,           T_CALLABLE,          $NULL,      $L__X, null,
       N_INSTROKE,             T_CALLABLE,          $NULL,      $L__X, null,
       N_INTERSECTS,           T_CALLABLE,          $NULL,      $L__X, null,
       N_INVERTMATRIX,         T_CALLABLE,          $NULL,      $L__X, null,
       N_ITRANSFORM,           T_CALLABLE,          $NULL,      $L__X, null,
       N_KSHOW,                T_CALLABLE,          $NULL,      $L__X, null,
       N_LINECAP,              $BUTTCAP,            $INTEGER,   $RW_,  null,
       N_LINEJOIN,             $MITERJOIN,          $INTEGER,   $RW_,  null,
       N_LINETO,               T_CALLABLE,          $NULL,      $L__X, null,
       N_LINEWIDTH,            "1.0",               $DOUBLE,    $RW_,  null,
       N_MAPTOPIXEL,           T_CALLABLE,          $NULL,      $L__X, null,
       N_MITERLIMIT,           "10.0",              $DOUBLE,    $RW_,  null,
       N_MOVETO,               T_CALLABLE,          $NULL,      $L__X, null,
       N_NEWPATH,              T_CALLABLE,          $NULL,      $L__X, null,
       N_PATH,                 T_PATH,              $NULL,      $RW_,  null,
       N_PATHBBOX,             T_CALLABLE,          $NULL,      $L__X, null,
       N_PATHFORALL,           T_CALLABLE,          $NULL,      $L__X, null,
       N_QUADTO,               T_CALLABLE,          $NULL,      $L__X, null,
       N_RCURVETO,             T_CALLABLE,          $NULL,      $L__X, null,
       N_RECTBUTTON,           T_CALLABLE,          $NULL,      $L__X, null,
       N_RECTCLIP,             T_CALLABLE,          $NULL,      $L__X, null,
       N_RECTCOPY,             T_CALLABLE,          $NULL,      $L__X, null,
       N_RECTERASE,            T_CALLABLE,          $NULL,      $L__X, null,
       N_RECTFILL,             T_CALLABLE,          $NULL,      $L__X, null,
       N_RECTMOVE,             T_CALLABLE,          $NULL,      $L__X, null,
       N_RECTSTROKE,           T_CALLABLE,          $NULL,      $L__X, null,
       N_RENDERING,            "-1",                $INTEGER,   $RW_,  null,
       N_RLINETO,              T_CALLABLE,          $NULL,      $L__X, null,
       N_RMOVETO,              T_CALLABLE,          $NULL,      $L__X, null,
       N_ROTATE,               T_CALLABLE,          $NULL,      $L__X, null,
       N_ROTATEFONT,           T_CALLABLE,          $NULL,      $L__X, null,
       N_ROTATEPATH,           T_CALLABLE,          $NULL,      $L__X, null,
       N_RQUADTO,              T_CALLABLE,          $NULL,      $L__X, null,
       N_SCALE,                T_CALLABLE,          $NULL,      $L__X, null,
       N_SCALEFONT,            T_CALLABLE,          $NULL,      $L__X, null,
       N_SCALEPATH,            T_CALLABLE,          $NULL,      $L__X, null,
       N_SETCMYKCOLOR,         T_CALLABLE,          $NULL,      $L__X, null,
       N_SETDASH,              T_CALLABLE,          $NULL,      $L__X, null,
       N_SETFONT,              T_CALLABLE,          $NULL,      $L__X, null,
       N_SETGRADIENT,          T_CALLABLE,          $NULL,      $L__X, null,
       N_SETGRAY,              T_CALLABLE,          $NULL,      $L__X, null,
       N_SETHSBCOLOR,          T_CALLABLE,          $NULL,      $L__X, null,
       N_SETLINECAP,           T_CALLABLE,          $NULL,      $L__X, null,
       N_SETLINEJOIN,          T_CALLABLE,          $NULL,      $L__X, null,
       N_SETLINEWIDTH,         T_CALLABLE,          $NULL,      $L__X, null,
       N_SETMATRIX,            T_CALLABLE,          $NULL,      $L__X, null,
       N_SETMITERLIMIT,        T_CALLABLE,          $NULL,      $L__X, null,
       N_SETPATH,              T_CALLABLE,          $NULL,      $L__X, null,
       N_SETRGBCOLOR,          T_CALLABLE,          $NULL,      $L__X, null,
       N_SETTEXTURE,           T_CALLABLE,          $NULL,      $L__X, null,
       N_SHEAR,                T_CALLABLE,          $NULL,      $L__X, null,
       N_SHEARFONT,            T_CALLABLE,          $NULL,      $L__X, null,
       N_SHEARPATH,            T_CALLABLE,          $NULL,      $L__X, null,
       N_SHOW,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_SHOWIMAGE,            T_CALLABLE,          $NULL,      $L__X, null,
       N_STRINGADVANCE,        T_CALLABLE,          $NULL,      $L__X, null,
       N_STRINGBOUNDS,         T_CALLABLE,          $NULL,      $L__X, null,
       N_STRINGWIDTH,          T_CALLABLE,          $NULL,      $L__X, null,
       N_STROKE,               T_CALLABLE,          $NULL,      $L__X, null,
       N_STROKEPATH,           T_CALLABLE,          $NULL,      $L__X, null,
       N_TEXTANTIALIASING,     "-1",                $INTEGER,   $RW_,  null,
       N_TRANSFORM,            T_CALLABLE,          $NULL,      $L__X, null,
       N_TRANSFORMFONT,        T_CALLABLE,          $NULL,      $L__X, null,
       N_TRANSFORMPATH,        T_CALLABLE,          $NULL,      $L__X, null,
       N_TRANSLATE,            T_CALLABLE,          $NULL,      $L__X, null,
       N_TRANSLATEFONT,        T_CALLABLE,          $NULL,      $L__X, null,
       N_TRANSLATEPATH,        T_CALLABLE,          $NULL,      $L__X, null,
       N_TRIMTOSTROKE,         T_CALLABLE,          $NULL,      $L__X, null,
       N_WIDTHSHOW,            T_CALLABLE,          $NULL,      $L__X, null,
       N_XORMODE,              $FALSE,              $INTEGER,   $RW_,  null,
    };

    ///////////////////////////////////
    //
    // YoixModuleGraphics Methods
    //
    ///////////////////////////////////

    public static YoixObject
    clearRect(YoixObject arg[]) {

	YoixObject  background;
	Graphics    g;

	if (arg[0].isDrawable()) {
	    if (arg[1].isNumber()) {
		if (arg[2].isNumber()) {
		    if (arg[3].isNumber()) {
			if (arg[4].isNumber()) {
			    if ((g = getGraphics(arg[0], true)) != null) {
				g.fillRect(
				    arg[1].intValue(),
				    arg[2].intValue(),
				    arg[3].intValue(),
				    arg[4].intValue()
				);
				g.dispose();
			    }
			} else VM.badArgument(4);
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    copyArea(YoixObject arg[]) {

	if (arg.length == 7 || arg.length == 8) {
	    if (arg[0].isDrawable()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			if (arg[3].isNumber()) {
			    if (arg[4].isNumber()) {
				if (arg[5].isNumber()) {
				    if (arg[6].isNumber()) {
					if (arg.length == 7 || arg[7].isNumber()) {
					    YoixMiscJFC.copyArea(
						arg[0],
						arg[1].doubleValue(), 
						arg[2].doubleValue(), 
						arg[3].doubleValue(), 
						arg[4].doubleValue(), 
						arg[5].doubleValue(), 
						arg[6].doubleValue(), 
						arg.length == 7 ? false : arg[7].booleanValue()
					    );
					} else VM.badArgument(7);
				    } else VM.badArgument(6);
				} else VM.badArgument(5);
			    } else VM.badArgument(4);
			} else VM.badArgument(3);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(null);
    }


    public static YoixObject
    drawArc(YoixObject arg[]) {

	Graphics  g;

	if (arg[0].isDrawable()) {
	    if (arg[1].isNumber()) {
		if (arg[2].isNumber()) {
		    if (arg[3].isNumber()) {
			if (arg[4].isNumber()) {
			    if (arg[5].isNumber()) {
				if (arg[6].isNumber()) {
				    if ((g = getGraphics(arg[0])) != null) {
					g.drawArc(
					    arg[1].intValue(),
					    arg[2].intValue(),
					    arg[3].intValue(),
					    arg[4].intValue(),
					    arg[5].intValue(),
					    arg[6].intValue()
					);
					g.dispose();
				    }
				} else VM.badArgument(6);
			    } else VM.badArgument(5);
			} else VM.badArgument(4);
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    drawImage(YoixObject arg[]) {

	AffineTransform  transform;
	YoixBodyMatrix   matrix;
	YoixObject       graphics;
	YoixObject       mtx;
	YoixObject       ctm;
	Graphics2D       g;
	Image            image;
	Point            p;

	//
	// A bit harder than you might expect, but it all seems to be
	// required if we want to have drawImage() track the current
	// graphics state.
	//

	if (arg.length == 4) {		// maybe more arguments - later
	    if (arg[0].isDrawable()) {
		if (arg[1].isImage()) {
		    if (arg[2].isNumber()) {
			if (arg[3].isNumber()) {
			    if ((image = (Image)arg[1].getManagedDrawable(null)) != null) {
				if ((graphics = arg[0].getObject(N_GRAPHICS)) != null) {
				    if ((g = (Graphics2D)getGraphics(arg[0])) != null) {
					if ((ctm = graphics.getObject(N_CTM)) != null) {
					    p = YoixMake.javaPoint(
						arg[2].doubleValue(),
						arg[3].doubleValue(),
						(YoixBodyMatrix)ctm.body()
					    );
					    try {
						transform = VM.getDefaultTransform().createInverse();
						transform.preConcatenate(g.getTransform());
						g.setTransform(transform);
						g.drawImage(image, p.x, p.y, null);
					    }
					    catch(NoninvertibleTransformException e) {}
					}
					g.dispose();
				    }
				}
			    }
			} else VM.badArgument(3);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(null);
    }


    public static YoixObject
    drawLine(YoixObject arg[]) {

	Graphics  g;

	if (arg[0].isDrawable()) {
	    if (arg[1].isNumber()) {
		if (arg[2].isNumber()) {
		    if (arg[3].isNumber()) {
			if (arg[4].isNumber()) {
			    if ((g = getGraphics(arg[0])) != null) {
				g.drawLine(
				    arg[1].intValue(),
				    arg[2].intValue(),
				    arg[3].intValue(),
				    arg[4].intValue()
				);
				g.dispose();
			    }
			} else VM.badArgument(4);
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    drawOval(YoixObject arg[]) {

	Graphics  g;

	if (arg[0].isDrawable()) {
	    if (arg[1].isNumber()) {
		if (arg[2].isNumber()) {
		    if (arg[3].isNumber()) {
			if (arg[4].isNumber()) {
			    if ((g = getGraphics(arg[0])) != null) {
				g.drawOval(
				    arg[1].intValue(),
				    arg[2].intValue(),
				    arg[3].intValue(),
				    arg[4].intValue()
				);
				g.dispose();
			    }
			} else VM.badArgument(4);
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    drawPolygon(YoixObject arg[]) {

	YoixObject  obj;
	Graphics    g;
	int         xpoints[];
	int         ypoints[];
	int         npoints = 0;
	int         x;
	int         y;
	int         n;

	if (arg[0].isDrawable()) {
	    if (arg[1].isArray() && arg[2].isArray()) {
		if (arg.length == 3 || arg.length == 4) {
		    if (arg.length == 4) {
			if (arg[3].isNumber())
			    npoints = Math.max(0, arg[3].intValue());
			else VM.badArgument(3);
		    } else npoints = Math.min(arg[1].length(), arg[2].length());
		} else VM.badCall();
		xpoints = new int[npoints];
		ypoints = new int[npoints];
		for (n = 0; n < npoints; n++) {
		    obj = arg[1].get(n, false);
		    if (obj.isNumber()) {
			x = obj.intValue();
			obj = arg[2].get(n, false);
			if (obj.isNumber()) {
			    y = obj.intValue();
			    xpoints[n] = x;
			    ypoints[n] = y;
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		}
	    } else {
		npoints = (arg.length - 1)/2;
		xpoints = new int[npoints];
		ypoints = new int[npoints];
		for (n = 1; n < arg.length - 1; n += 2) {
		    if (arg[n].isNumber()) {
			if (arg[n+1].isNumber()) {
			    xpoints[n/2] = arg[n].intValue();
			    ypoints[n/2] = arg[n+1].intValue();
			} else VM.badArgument(n+1);
		    } else VM.badArgument(n);
		}
	    }
	    if ((g = getGraphics(arg[0])) != null) {
		g.drawPolygon(xpoints, ypoints, npoints);
		g.dispose();
	    }
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    drawPolyline(YoixObject arg[]) {

	YoixObject  obj;
	Graphics    g;
	int         xpoints[];
	int         ypoints[];
	int         npoints = 0;
	int         x;
	int         y;
	int         n;

	if (arg[0].isDrawable()) {
	    if (arg[1].isArray() && arg[2].isArray()) {
		if (arg.length == 3 || arg.length == 4) {
		    if (arg.length == 4) {
			if (arg[3].isNumber())
			    npoints = Math.max(0, arg[3].intValue());
			else VM.badArgument(3);
		    } else npoints = Math.min(arg[1].length(), arg[2].length());
		} else VM.badCall();
		xpoints = new int[npoints];
		ypoints = new int[npoints];
		for (n = 0; n < npoints; n++) {
		    obj = arg[1].get(n, false);
		    if (obj.isNumber()) {
			x = obj.intValue();
			obj = arg[2].get(n, false);
			if (obj.isNumber()) {
			    y = obj.intValue();
			    xpoints[n] = x;
			    ypoints[n] = y;
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		}
	    } else {
		npoints = (arg.length - 1)/2;
		xpoints = new int[npoints];
		ypoints = new int[npoints];
		for (n = 1; n < arg.length - 1; n += 2) {
		    if (arg[n].isNumber()) {
			if (arg[n+1].isNumber()) {
			    xpoints[n/2] = arg[n].intValue();
			    ypoints[n/2] = arg[n+1].intValue();
			} else VM.badArgument(n+1);
		    } else VM.badArgument(n);
		}
	    }
	    if ((g = getGraphics(arg[0])) != null) {
		g.drawPolyline(xpoints, ypoints, npoints);
		g.dispose();
	    }
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    drawRect(YoixObject arg[]) {

	Graphics  g;

	if (arg[0].isDrawable()) {
	    if (arg[1].isNumber()) {
		if (arg[2].isNumber()) {
		    if (arg[3].isNumber()) {
			if (arg[4].isNumber()) {
			    if ((g = getGraphics(arg[0])) != null) {
				g.drawRect(
				    arg[1].intValue(),
				    arg[2].intValue(),
				    arg[3].intValue(),
				    arg[4].intValue()
				);
				g.dispose();
			    }
			} else VM.badArgument(4);
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    drawRoundRect(YoixObject arg[]) {

	Graphics  g;

	if (arg[0].isDrawable()) {
	    if (arg[1].isNumber()) {
		if (arg[2].isNumber()) {
		    if (arg[3].isNumber()) {
			if (arg[4].isNumber()) {
			    if (arg[5].isNumber()) {
				if (arg[6].isNumber()) {
				    if ((g = getGraphics(arg[0])) != null) {
					g.drawRoundRect(
					    arg[1].intValue(),
					    arg[2].intValue(),
					    arg[3].intValue(),
					    arg[4].intValue(),
					    arg[5].intValue(),
					    arg[6].intValue()
					);
					g.dispose();
				    }
				} else VM.badArgument(6);
			    } else VM.badArgument(5);
			} else VM.badArgument(4);
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    drawString(YoixObject arg[]) {

	YoixObject  graphics;
	YoixObject  font;
	Graphics    g;
	String      str;

	if (arg.length == 4 || arg.length == 5) {
	    if (arg[0].isDrawable()) {
		if (arg[1].isString()) {
		    if (arg[2].isNumber()) {
			if (arg[3].isNumber()) {
			    if (arg.length == 4 || arg[4].isNumber()) {
				if ((str = arg[1].stringValue()) != null && str.length() > 0) {
				    if ((graphics = arg[0].getObject(N_GRAPHICS)) != null) {
					if ((font = graphics.getObject(N_FONT)) != null) {
					    if (font.isFont() && font.notNull()) {
						((YoixBodyFont)font.body()).drawString(
						    str,
						    arg[2].intValue(),
						    arg[3].intValue(),
						    arg.length == 5 ? arg[4].doubleValue() : 1.0,
						    graphics
						);
					    }
					}
				    }
				}
			    } else VM.badArgument(4);
			} else VM.badArgument(3);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(null);
    }


    public static YoixObject
    fillArc(YoixObject arg[]) {

	Graphics  g;

	if (arg[0].isDrawable()) {
	    if (arg[1].isNumber()) {
		if (arg[2].isNumber()) {
		    if (arg[3].isNumber()) {
			if (arg[4].isNumber()) {
			    if (arg[5].isNumber()) {
				if (arg[6].isNumber()) {
				    if ((g = getGraphics(arg[0])) != null) {
					g.fillArc(
					    arg[1].intValue(),
					    arg[2].intValue(),
					    arg[3].intValue(),
					    arg[4].intValue(),
					    arg[5].intValue(),
					    arg[6].intValue()
					);
					g.dispose();
				    }
				} else VM.badArgument(6);
			    } else VM.badArgument(5);
			} else VM.badArgument(4);
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    fillOval(YoixObject arg[]) {

	Graphics  g;

	if (arg[0].isDrawable()) {
	    if (arg[1].isNumber()) {
		if (arg[2].isNumber()) {
		    if (arg[3].isNumber()) {
			if (arg[4].isNumber()) {
			    if ((g = getGraphics(arg[0])) != null) {
				g.fillOval(
				    arg[1].intValue(),
				    arg[2].intValue(),
				    arg[3].intValue(),
				    arg[4].intValue()
				);
				g.dispose();
			    }
			} else VM.badArgument(4);
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    fillPolygon(YoixObject arg[]) {

	YoixObject  obj;
	Graphics    g;
	int         xpoints[];
	int         ypoints[];
	int         npoints = 0;
	int         x;
	int         y;
	int         n;

	if (arg[0].isDrawable()) {
	    if (arg[1].isArray() && arg[2].isArray()) {
		if (arg.length == 3 || arg.length == 4) {
		    if (arg.length == 4) {
			if (arg[3].isNumber())
			    npoints = Math.max(0, arg[3].intValue());
			else VM.badArgument(3);
		    } else npoints = Math.min(arg[1].length(), arg[2].length());
		} else VM.badCall();
		xpoints = new int[npoints];
		ypoints = new int[npoints];
		for (n = 0; n < npoints; n++) {
		    obj = arg[1].get(n, false);
		    if (obj.isNumber()) {
			x = obj.intValue();
			obj = arg[2].get(n, false);
			if (obj.isNumber()) {
			    y = obj.intValue();
			    xpoints[n] = x;
			    ypoints[n] = y;
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		}
	    } else {
		npoints = (arg.length - 1)/2;
		xpoints = new int[npoints];
		ypoints = new int[npoints];
		for (n = 1; n < arg.length - 1; n += 2) {
		    if (arg[n].isNumber()) {
			if (arg[n+1].isNumber()) {
			    xpoints[n/2] = arg[n].intValue();
			    ypoints[n/2] = arg[n+1].intValue();
			} else VM.badArgument(n+1);
		    } else VM.badArgument(n);
		}
	    }
	    if ((g = getGraphics(arg[0])) != null) {
		g.fillPolygon(xpoints, ypoints, npoints);
		g.dispose();
	    }
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    fillRect(YoixObject arg[]) {

	Graphics  g;

	if (arg[0].isDrawable()) {
	    if (arg[1].isNumber()) {
		if (arg[2].isNumber()) {
		    if (arg[3].isNumber()) {
			if (arg[4].isNumber()) {
			    if ((g = getGraphics(arg[0])) != null) {
				g.fillRect(
				    arg[1].intValue(),
				    arg[2].intValue(),
				    arg[3].intValue(),
				    arg[4].intValue()
				);
				g.dispose();
			    }
			} else VM.badArgument(4);
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    fillRoundRect(YoixObject arg[]) {

	Graphics  g;

	if (arg[0].isDrawable()) {
	    if (arg[1].isNumber()) {
		if (arg[2].isNumber()) {
		    if (arg[3].isNumber()) {
			if (arg[4].isNumber()) {
			    if (arg[5].isNumber()) {
				if (arg[6].isNumber()) {
				    if ((g = getGraphics(arg[0])) != null) {
					g.fillRoundRect(
					    arg[1].intValue(),
					    arg[2].intValue(),
					    arg[3].intValue(),
					    arg[4].intValue(),
					    arg[5].intValue(),
					    arg[6].intValue()
					);
					g.dispose();
				    }
				} else VM.badArgument(6);
			    } else VM.badArgument(5);
			} else VM.badArgument(4);
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    pointInPolygon(YoixObject arg[]) {

	YoixObject  obj;
	Polygon     poly;
	boolean     result = false;
	Point       p;
	int         xpoints[];
	int         ypoints[];
	int         npoints = 0;
	int         x;
	int         y;
	int         n;

	if (arg[0].isPoint()) {
	    p = new Point(arg[0].getInt(N_X, 0), arg[0].getInt(N_Y, 0));
	    if (arg[1].isArray() && arg[2].isArray()) {
		if (arg.length == 3 || arg.length == 4) {
		    if (arg.length == 4) {
			if (arg[3].isNumber())
			    npoints = Math.max(0, arg[3].intValue());
			else VM.badArgument(3);
		    } else npoints = Math.min(arg[1].length(), arg[2].length());
		} else VM.badCall();
		xpoints = new int[npoints];
		ypoints = new int[npoints];
		for (n = 0; n < npoints; n++) {
		    obj = arg[1].get(n, false);
		    if (obj.isNumber()) {
			x = obj.intValue();
			obj = arg[2].get(n, false);
			if (obj.isNumber()) {
			    y = obj.intValue();
			    xpoints[n] = x;
			    ypoints[n] = y;
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		}
	    } else {
		npoints = (arg.length - 1)/2;
		xpoints = new int[npoints];
		ypoints = new int[npoints];
		for (n = 1; n < arg.length - 1; n += 2) {
		    if (arg[n].isNumber()) {
			if (arg[n+1].isNumber()) {
			    xpoints[n/2] = arg[n].intValue();
			    ypoints[n/2] = arg[n+1].intValue();
			} else VM.badArgument(n+1);
		    } else VM.badArgument(n);
		}
	    }
	    poly = new Polygon(xpoints, ypoints, npoints);
	    result = poly.contains(p);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    pointInRect(YoixObject arg[]) {

	Rectangle2D   rect = null;
	boolean       result = false;

	if (arg.length == 2 || arg.length == 5) {
	    if (arg[0].isPoint() || arg[0].isNull()) {
		if (arg[0].notNull()) {
		    if (arg.length == 2) {
			if (arg[1].isRectangle() || arg[1].isNull()) {
			    if (arg[1].notNull()) {
				rect = new Rectangle2D.Double(
				    arg[1].getDouble(N_X, 0),
				    arg[1].getDouble(N_Y, 0),
				    arg[1].getDouble(N_WIDTH, 0),
				    arg[1].getDouble(N_HEIGHT, 0)
				);
			    }
			} else VM.badArgument(1);
		    } else if (arg.length == 5) {
			if (arg[1].isNumber()) {
			    if (arg[2].isNumber()) {
				if (arg[3].isNumber()) {
				    if (arg[4].isNumber()) {
					rect = new Rectangle2D.Double(
					    arg[1].doubleValue(),
					    arg[2].doubleValue(),
					    arg[3].doubleValue(),
					    arg[4].doubleValue()
					);
				    } else VM.badArgument(4);
				} else VM.badArgument(3);
			    } else VM.badArgument(2);
			} else VM.badArgument(1);
		    }
		    if (rect != null)
			result = rect.contains(arg[0].getDouble(N_X, 0), arg[0].getDouble(N_Y, 0));
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    rectContainsRect(YoixObject arg[]) {

	Rectangle2D  rect1;
	Rectangle2D  rect2 = null;
	boolean      result = false;

	if (arg.length == 2 || arg.length == 5) {
	    if (arg[0].isRectangle() || arg[0].isNull()) {
		if (arg[0].notNull()) {
		    rect1 = new Rectangle2D.Double(
			arg[0].getDouble(N_X, 0),
			arg[0].getDouble(N_Y, 0),
			arg[0].getDouble(N_WIDTH, 0),
			arg[0].getDouble(N_HEIGHT, 0)
		    );
		    if (arg.length == 2) {
			if (arg[1].isRectangle() || arg[1].isNull()) {
			    if (arg[1].notNull()) {
				rect2 = new Rectangle2D.Double(
				    arg[1].getDouble(N_X, 0),
				    arg[1].getDouble(N_Y, 0),
				    arg[1].getDouble(N_WIDTH, 0),
				    arg[1].getDouble(N_HEIGHT, 0)
				);
			    }
			} else VM.badArgument(1);
		    } else if (arg.length == 5) {
			if (arg[1].isNumber()) {
			    if (arg[2].isNumber()) {
				if (arg[3].isNumber()) {
				    if (arg[4].isNumber()) {
					rect2 = new Rectangle2D.Double(
					    arg[1].doubleValue(),
					    arg[2].doubleValue(),
					    arg[3].doubleValue(),
					    arg[4].doubleValue()
					);
				    } else VM.badArgument(4);
				} else VM.badArgument(3);
			    } else VM.badArgument(2);
			} else VM.badArgument(1);
		    }
		    if (rect2 != null) {
			result = rect1.contains(
			    rect2.getX(),
			    rect2.getY(),
			    rect2.getWidth(),
			    rect2.getHeight()
			);
		    }
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    rectIntersectionRect(YoixObject arg[]) {

	Rectangle2D  rect1;
	Rectangle2D  rect2 = null;
	Rectangle2D  rect3;
	Rectangle2D  result = null;

	if (arg.length == 2 || arg.length == 5) {
	    if (arg[0].isRectangle() || arg[0].isNull()) {
		if (arg[0].notNull()) {
		    rect1 = new Rectangle2D.Double(
			arg[0].getDouble(N_X, 0),
			arg[0].getDouble(N_Y, 0),
			arg[0].getDouble(N_WIDTH, 0),
			arg[0].getDouble(N_HEIGHT, 0)
		    );
		    if (arg.length == 2) {
			if (arg[1].isRectangle() || arg[1].isNull()) {
			    if (arg[1].notNull()) {
				rect2 = new Rectangle2D.Double(
				    arg[1].getDouble(N_X, 0),
				    arg[1].getDouble(N_Y, 0),
				    arg[1].getDouble(N_WIDTH, 0),
				    arg[1].getDouble(N_HEIGHT, 0)
				);
			    }
			} else VM.badArgument(1);
		    } else if (arg.length == 5) {
			if (arg[1].isNumber()) {
			    if (arg[2].isNumber()) {
				if (arg[3].isNumber()) {
				    if (arg[4].isNumber()) {
					rect2 = new Rectangle2D.Double(
					    arg[1].doubleValue(),
					    arg[2].doubleValue(),
					    arg[3].doubleValue(),
					    arg[4].doubleValue()
					);
				    } else VM.badArgument(4);
				} else VM.badArgument(3);
			    } else VM.badArgument(2);
			} else VM.badArgument(1);
		    }
		    if (rect2 != null) {
			rect3 = new Rectangle2D.Double();
			Rectangle2D.intersect(rect1, rect2, rect3);
			result = rect3.isEmpty() ? null : rect3;
		    }
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newRectangle(result));
    }


    public static YoixObject
    rectIntersectsRect(YoixObject arg[]) {

	Rectangle2D  rect1;
	Rectangle2D  rect2 = null;
	boolean      result = false;

	if (arg.length == 2 || arg.length == 5) {
	    if (arg[0].isRectangle() || arg[0].isNull()) {
		if (arg[0].notNull()) {
		    rect1 = new Rectangle2D.Double(
			arg[0].getDouble(N_X, 0),
			arg[0].getDouble(N_Y, 0),
			arg[0].getDouble(N_WIDTH, 0),
			arg[0].getDouble(N_HEIGHT, 0)
		    );
		    if (arg.length == 2) {
			if (arg[1].isRectangle() || arg[1].isNull()) {
			    if (arg[1].notNull()) {
				rect2 = new Rectangle2D.Double(
				    arg[1].getDouble(N_X, 0),
				    arg[1].getDouble(N_Y, 0),
				    arg[1].getDouble(N_WIDTH, 0),
				    arg[1].getDouble(N_HEIGHT, 0)
				);
			    }
			} else VM.badArgument(1);
		    } else if (arg.length == 5) {
			if (arg[1].isNumber()) {
			    if (arg[2].isNumber()) {
				if (arg[3].isNumber()) {
				    if (arg[4].isNumber()) {
					rect2 = new Rectangle2D.Double(
					    arg[1].doubleValue(),
					    arg[2].doubleValue(),
					    arg[3].doubleValue(),
					    arg[4].doubleValue()
					);
				    } else VM.badArgument(4);
				} else VM.badArgument(3);
			    } else VM.badArgument(2);
			} else VM.badArgument(1);
		    }
		    if (rect2 != null) {
			result = rect1.intersects(
			    rect2.getX(),
			    rect2.getY(),
			    rect2.getWidth(),
			    rect2.getHeight()
			);
		    }
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    rectUnionRect(YoixObject arg[]) {

	Rectangle2D  rect1 = null;
	Rectangle2D  rect2 = null;
	Rectangle2D  rect3;
	Rectangle2D  result = null;

	if (arg.length == 2 || arg.length == 5) {
	    if (arg[0].isRectangle() || arg[0].isNull()) {
		if (arg[0].notNull()) {
		    rect1 = new Rectangle2D.Double(
			arg[0].getDouble(N_X, 0),
			arg[0].getDouble(N_Y, 0),
			arg[0].getDouble(N_WIDTH, 0),
			arg[0].getDouble(N_HEIGHT, 0)
		    );
		}
		if (arg.length == 2) {
		    if (arg[1].isRectangle() || arg[1].isNull()) {
			if (arg[1].notNull()) {
			    rect2 = new Rectangle2D.Double(
				arg[1].getDouble(N_X, 0),
				arg[1].getDouble(N_Y, 0),
				arg[1].getDouble(N_WIDTH, 0),
				arg[1].getDouble(N_HEIGHT, 0)
			    );
			}
		    } else VM.badArgument(1);
		} else if (arg.length == 5) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    if (arg[3].isNumber()) {
				if (arg[4].isNumber()) {
				    rect2 = new Rectangle2D.Double(
					arg[1].doubleValue(),
					arg[2].doubleValue(),
					arg[3].doubleValue(),
					arg[4].doubleValue()
				    );
				} else VM.badArgument(4);
			    } else VM.badArgument(3);
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		}
		if (rect1 != null) {
		    if (rect2 != null) {
			rect3 = new Rectangle2D.Double();
			Rectangle2D.union(rect1, rect2, rect3);
			result = rect3.isEmpty() ? null : rect3;
		    } else result = rect1;
		} else result = rect2;
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newRectangle(result));
    }


    public static YoixObject
    stringBounds(YoixObject arg[]) {

	Rectangle2D  rect;
	YoixObject   font;
	YoixObject   dest;
	YoixObject   bounds = null;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isFont() || arg[0].isString() || arg[0].isDrawable()) {
		if (arg[0].notNull()) {
		    if (arg[0].isString()) {
			font = YoixMake.yoixFont(YoixMakeScreen.javaFont(arg[0]));
			dest = null;
		    } else if (arg[0].isFont()) {
			font = arg[0];
			dest = null;
		    } else {
			font = arg[0].getObject(N_GRAPHICS).getObject(N_FONT);
			dest = arg[0];
		    }
		    if (font != null) {
			if (arg[1].isString() || arg[1].isNull()) {
			    if (arg.length == 2 || arg[2].isNumber()) {
				rect = ((YoixBodyFont)font.body()).stringBounds(
				    arg[1].stringValue(),
				    arg.length == 3 ? arg[2].booleanValue() : false,
				    dest
				);
				bounds = YoixMakeScreen.yoixRectangle(rect);
			    } else VM.badArgument(2);
			} else VM.badArgument(1);
		    } else VM.badArgument(0);
		} else VM.badArgument(0);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(bounds != null ? bounds : YoixMake.yoixType(T_RECTANGLE));
    }


    public static YoixObject
    stringWidth(YoixObject arg[]) {

	YoixObject  font;
	YoixObject  dest;
	double      width = 0;

	if (arg[0].isFont() || arg[0].isString() || arg[0].isDrawable()) {
	    if (arg[0].notNull()) {
		if (arg[0].isString()) {
		    font = YoixMake.yoixFont(YoixMakeScreen.javaFont(arg[0]));
		    dest = null;
		} else if (arg[0].isFont()) {
		    font = arg[0];
		    dest = null;
		} else {
		    font = arg[0].getObject(N_GRAPHICS).getObject(N_FONT);
		    dest = arg[0];
		}
		if (font != null) {
		    if (arg[1].isString() || arg[1].isNull()) {
			width = ((YoixBodyFont)font.body()).stringWidth(
			    arg[1].stringValue(),
			    dest
			);
			width = YoixMakeScreen.yoixDistance(width);
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    } else VM.badArgument(0);
	} else VM.badArgument(0);

	return(YoixObject.newDouble(width));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static Graphics
    getGraphics(YoixObject obj) {

	return(getGraphics(obj, false));
    }


    private static Graphics
    getGraphics(YoixObject obj, boolean erase) {

	YoixObject  graphics;
	Graphics    g;

	if ((graphics = obj.getObject(N_GRAPHICS)) != null)
	    g = ((YoixBodyGraphics)graphics.body()).getCompatibleGraphics(erase);
	else g = null;

	return(g);
    }
}

