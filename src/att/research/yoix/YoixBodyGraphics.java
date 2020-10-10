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
import java.awt.color.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

final
class YoixBodyGraphics extends YoixPointerActive

    implements YoixConstantsGraphics

{

    //
    // All the rect builtins (e.g., rectclip, recterase()) now treat a
    // NULL argument as a reference to the entire drawable, if there is
    // one. Turns out to be particularly convenient and consistent in
    // Yoix paint() functions that are called with an argument that's
    // either NULL or the Rectangle that needs to be repainted.
    //
    // NOTE - getGraphics2D() now always includes currentstroke in the
    // Graphics2D object that it returns. The change was made on 3/23/08
    // because we saw at least one example (a Yoix script and standalone
    // Java program) where the stroke stored in a Graphics2D influenced
    // the behavior of fill(). See the comments in getGraphics2D() for
    // more details.
    //

    private RenderingHints  currenthints = null;
    private YoixBodyMatrix  currentmatrix = null;
    private YoixBodyPath    currentpath = null;
    private YoixBodyFont    currentfont = null;
    private BasicStroke     currentstroke = null;
    private GeneralPath     currentclip = null;
    private YoixObject      currentdrawable = null;
    private Paint           currentpaint = null;

    private boolean  xormode;
    private float    linewidth;
    private float    miterlimit;
    private float    dasharray[];
    private float    dashphase;
    private int      compositerule;
    private int      linecap;
    private int      linejoin;

    //
    // Stack used to save and restore the graphics state.
    //

    private Vector  savestack = new Vector();

    //
    // A temporary scratch path that can be shared by builtins that
    // are synchronized and otherwise well behaved. The path should
    // always be initialized (e.g., via pathReset()) because there's
    // no requirement that you clear it when you're finished. This
    // was added quickly and it's only for efficiency and use from
    // synchronized methods.
    //

    private YoixBodyPath  scratchpath = null;

    //
    // An array used to set permissions on some of the fields that
    // users should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	N_CTM,              $LR__,       null,
	N_PATH,             $LR__,       null,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(125);

    static {
	activefields.put(N_ANTIALIASING, new Integer(V_ANTIALIASING));
	activefields.put(N_APPENDPATH, new Integer(V_APPENDPATH));
	activefields.put(N_ARC, new Integer(V_ARC));
	activefields.put(N_ARCN, new Integer(V_ARCN));
	activefields.put(N_ARCT, new Integer(V_ARCT));
	activefields.put(N_ASHOW, new Integer(V_ASHOW));
	activefields.put(N_AWIDTHSHOW, new Integer(V_AWIDTHSHOW));
	activefields.put(N_BACKGROUND, new Integer(V_BACKGROUND));
	activefields.put(N_CHARPATH, new Integer(V_CHARPATH));
	activefields.put(N_CLIP, new Integer(V_CLIP));
	activefields.put(N_CLIPPATH, new Integer(V_CLIPPATH));
	activefields.put(N_CLOSEPATH, new Integer(V_CLOSEPATH));
	activefields.put(N_COMPOSITERULE, new Integer(V_COMPOSITERULE));
	activefields.put(N_CONCAT, new Integer(V_CONCAT));
	activefields.put(N_CONCATMATRIX, new Integer(V_CONCATMATRIX));
	activefields.put(N_CTM, new Integer(V_CTM));
	activefields.put(N_CURRENTMATRIX, new Integer(V_CURRENTMATRIX));
	activefields.put(N_CURRENTPATH, new Integer(V_CURRENTPATH));
	activefields.put(N_CURRENTPOINT, new Integer(V_CURRENTPOINT));
	activefields.put(N_CURVETO, new Integer(V_CURVETO));
	activefields.put(N_DASHARRAY, new Integer(V_DASHARRAY));
	activefields.put(N_DASHPHASE, new Integer(V_DASHPHASE));
	activefields.put(N_DIVIDEMATRIX, new Integer(V_DIVIDEMATRIX));
	activefields.put(N_DRAWABLE, new Integer(V_DRAWABLE));
	activefields.put(N_DRAWABLEBBOX, new Integer(V_DRAWABLEBBOX));
	activefields.put(N_DTRANSFORM, new Integer(V_DTRANSFORM));
	activefields.put(N_EOCLIP, new Integer(V_EOCLIP));
	activefields.put(N_EOERASE, new Integer(V_EOERASE));
	activefields.put(N_EOFILL, new Integer(V_EOFILL));
	activefields.put(N_EOINTERSECTS, new Integer(V_EOINTERSECTS));
	activefields.put(N_ERASE, new Integer(V_ERASE));
	activefields.put(N_ERASEDRAWABLE, new Integer(V_ERASEDRAWABLE));
	activefields.put(N_FILL, new Integer(V_FILL));
	activefields.put(N_FLATTENPATH, new Integer(V_FLATTENPATH));
	activefields.put(N_FONT, new Integer(V_FONT));
	activefields.put(N_FOREGROUND, new Integer(V_FOREGROUND));
	activefields.put(N_FRACTIONALMETRICS, new Integer(V_FRACTIONALMETRICS));
	activefields.put(N_GRESTORE, new Integer(V_GRESTORE));
	activefields.put(N_GRESTOREALL, new Integer(V_GRESTOREALL));
	activefields.put(N_GSAVE, new Integer(V_GSAVE));
	activefields.put(N_IDENTMATRIX, new Integer(V_IDENTMATRIX));
	activefields.put(N_IDTRANSFORM, new Integer(V_IDTRANSFORM));
	activefields.put(N_IMAGEPATH, new Integer(V_IMAGEPATH));
	activefields.put(N_INEOFILL, new Integer(V_INEOFILL));
	activefields.put(N_INFILL, new Integer(V_INFILL));
	activefields.put(N_INITCLIP, new Integer(V_INITCLIP));
	activefields.put(N_INITGRAPHICS, new Integer(V_INITGRAPHICS));
	activefields.put(N_INITMATRIX, new Integer(V_INITMATRIX));
	activefields.put(N_INSTROKE, new Integer(V_INSTROKE));
	activefields.put(N_INTERSECTS, new Integer(V_INTERSECTS));
	activefields.put(N_INVERTMATRIX, new Integer(V_INVERTMATRIX));
	activefields.put(N_ITRANSFORM, new Integer(V_ITRANSFORM));
	activefields.put(N_KSHOW, new Integer(V_KSHOW));
	activefields.put(N_LINECAP, new Integer(V_LINECAP));
	activefields.put(N_LINEJOIN, new Integer(V_LINEJOIN));
	activefields.put(N_LINETO, new Integer(V_LINETO));
	activefields.put(N_LINEWIDTH, new Integer(V_LINEWIDTH));
	activefields.put(N_MAPTOPIXEL, new Integer(V_MAPTOPIXEL));
	activefields.put(N_MITERLIMIT, new Integer(V_MITERLIMIT));
	activefields.put(N_MOVETO, new Integer(V_MOVETO));
	activefields.put(N_NEWPATH, new Integer(V_NEWPATH));
	activefields.put(N_PATH, new Integer(V_PATH));
	activefields.put(N_PATHBBOX, new Integer(V_PATHBBOX));
	activefields.put(N_PATHFORALL, new Integer(V_PATHFORALL));
	activefields.put(N_QUADTO, new Integer(V_QUADTO));
	activefields.put(N_RCURVETO, new Integer(V_RCURVETO));
	activefields.put(N_RECTBUTTON, new Integer(V_RECTBUTTON));
	activefields.put(N_RECTCLIP, new Integer(V_RECTCLIP));
	activefields.put(N_RECTCOPY, new Integer(V_RECTCOPY));
	activefields.put(N_RECTERASE, new Integer(V_RECTERASE));
	activefields.put(N_RECTFILL, new Integer(V_RECTFILL));
	activefields.put(N_RECTMOVE, new Integer(V_RECTMOVE));
	activefields.put(N_RECTSTROKE, new Integer(V_RECTSTROKE));
	activefields.put(N_RENDERING, new Integer(V_RENDERING));
	activefields.put(N_RLINETO, new Integer(V_RLINETO));
	activefields.put(N_RMOVETO, new Integer(V_RMOVETO));
	activefields.put(N_ROTATE, new Integer(V_ROTATE));
	activefields.put(N_ROTATEFONT, new Integer(V_ROTATEFONT));
	activefields.put(N_ROTATEPATH, new Integer(V_ROTATEPATH));
	activefields.put(N_RQUADTO, new Integer(V_RQUADTO));
	activefields.put(N_SCALE, new Integer(V_SCALE));
	activefields.put(N_SCALEFONT, new Integer(V_SCALEFONT));
	activefields.put(N_SCALEPATH, new Integer(V_SCALEPATH));
	activefields.put(N_SETCMYKCOLOR, new Integer(V_SETCMYKCOLOR));
	activefields.put(N_SETDASH, new Integer(V_SETDASH));
	activefields.put(N_SETFONT, new Integer(V_SETFONT));
	activefields.put(N_SETGRADIENT, new Integer(V_SETGRADIENT));
	activefields.put(N_SETGRAY, new Integer(V_SETGRAY));
	activefields.put(N_SETHSBCOLOR, new Integer(V_SETHSBCOLOR));
	activefields.put(N_SETLINECAP, new Integer(V_SETLINECAP));
	activefields.put(N_SETLINEJOIN, new Integer(V_SETLINEJOIN));
	activefields.put(N_SETLINEWIDTH, new Integer(V_SETLINEWIDTH));
	activefields.put(N_SETLINECAP, new Integer(V_SETLINECAP));
	activefields.put(N_SETMATRIX, new Integer(V_SETMATRIX));
	activefields.put(N_SETMITERLIMIT, new Integer(V_SETMITERLIMIT));
	activefields.put(N_SETPATH, new Integer(V_SETPATH));
	activefields.put(N_SETRGBCOLOR, new Integer(V_SETRGBCOLOR));
	activefields.put(N_SETTEXTURE, new Integer(V_SETTEXTURE));
	activefields.put(N_SHEAR, new Integer(V_SHEAR));
	activefields.put(N_SHEARFONT, new Integer(V_SHEARFONT));
	activefields.put(N_SHEARPATH, new Integer(V_SHEARPATH));
	activefields.put(N_SHOW, new Integer(V_SHOW));
	activefields.put(N_SHOWIMAGE, new Integer(V_SHOWIMAGE));
	activefields.put(N_STRINGADVANCE, new Integer(V_STRINGADVANCE));
	activefields.put(N_STRINGBOUNDS, new Integer(V_STRINGBOUNDS));
	activefields.put(N_STRINGWIDTH, new Integer(V_STRINGWIDTH));
	activefields.put(N_STROKE, new Integer(V_STROKE));
	activefields.put(N_STROKEPATH, new Integer(V_STROKEPATH));
	activefields.put(N_TEXTANTIALIASING, new Integer(V_TEXTANTIALIASING));
	activefields.put(N_TRANSFORM, new Integer(V_TRANSFORM));
	activefields.put(N_TRANSFORMFONT, new Integer(V_TRANSFORMFONT));
	activefields.put(N_TRANSFORMPATH, new Integer(V_TRANSFORMPATH));
	activefields.put(N_TRANSLATE, new Integer(V_TRANSLATE));
	activefields.put(N_TRANSLATEFONT, new Integer(V_TRANSLATEFONT));
	activefields.put(N_TRANSLATEPATH, new Integer(V_TRANSLATEPATH));
	activefields.put(N_TRIMTOSTROKE, new Integer(V_TRIMTOSTROKE));
	activefields.put(N_WIDTHSHOW, new Integer(V_WIDTHSHOW));
	activefields.put(N_XORMODE, new Integer(V_XORMODE));
    }

    //
    // Names of fields in data dictionary that are explicitly saved and
    // restore by the gsave and grestore builtins. The only fields that
    // need to be listed here are the ones that require consistent data
    // dictionary entries, either because they're used internally or can
    // be returned directly to user programs. We undoubtedly are saving
    // more than required - check carefully later.
    //

    private static final String  gsavekeys[] = {
	N_ANTIALIASING,
	N_BACKGROUND,
	N_COMPOSITERULE,
	N_DASHARRAY,
	N_DASHPHASE,
	N_FOREGROUND,
	N_FRACTIONALMETRICS,
	N_LINECAP,
	N_LINEJOIN,
	N_LINEWIDTH,
	N_MITERLIMIT,
	N_RENDERING,
	N_TEXTANTIALIASING,
	N_XORMODE,
    };

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyGraphics(YoixObject data) {

	super(data);
	buildGraphics();
	setFixedSize();
	setPermissions(permissions);
	initializer();			// calls N_INITIALIZER if possible
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(GRAPHICS);
    }

    ///////////////////////////////////
    //
    // YoixBodyGraphics Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_CONCAT:
	    case V_CONCATMATRIX:
	    case V_CURRENTMATRIX:
	    case V_DIVIDEMATRIX:
	    case V_DTRANSFORM:
	    case V_IDENTMATRIX:
	    case V_IDTRANSFORM:
	    case V_INITMATRIX:
	    case V_INVERTMATRIX:
	    case V_ITRANSFORM:
	    case V_MAPTOPIXEL:
	    case V_ROTATE:
	    case V_SCALE:
	    case V_SETMATRIX:
	    case V_SHEAR:
	    case V_TRANSFORM:
	    case V_TRANSLATE:
		obj = currentmatrix.executeField(name, argv);
		break;

	    case V_APPENDPATH:
	    case V_ARC:
	    case V_ARCN:
	    case V_ARCT:
	    case V_CLOSEPATH:
	    case V_CURRENTPATH:
	    case V_CURRENTPOINT:
	    case V_CURVETO:
	    case V_EOINTERSECTS:
	    case V_FLATTENPATH:
	    case V_INTERSECTS:
	    case V_LINETO:
	    case V_MOVETO:
	    case V_NEWPATH:
	    case V_PATHBBOX:
	    case V_PATHFORALL:
	    case V_QUADTO:
	    case V_RCURVETO:
	    case V_RLINETO:
	    case V_RMOVETO:
	    case V_ROTATEPATH:
	    case V_RQUADTO:
	    case V_SCALEPATH:
	    case V_SHEARPATH:
	    case V_TRANSFORMPATH:
	    case V_TRANSLATEPATH:
	    case V_TRIMTOSTROKE:
		obj = currentpath.executeField(name, argv);
		break;

	    case V_ROTATEFONT:
	    case V_SCALEFONT:
	    case V_SHEARFONT:
	    case V_TRANSFORMFONT:
	    case V_TRANSLATEFONT:
		obj = currentfont.executeField(name, argv);
		break;

	    case V_ASHOW:
		obj = builtinAShow(name, argv);
		break;

	    case V_AWIDTHSHOW:
		obj = builtinAWidthShow(name, argv);
		break;

	    case V_CHARPATH:
		obj = builtinCharPath(name, argv);
		break;

	    case V_CLIP:
		obj = builtinClip(name, argv);
		break;

	    case V_CLIPPATH:
		obj = builtinClipPath(name, argv);
		break;

	    case V_DRAWABLEBBOX:
		obj = builtinDrawableBBox(name, argv);
		break;

	    case V_EOCLIP:
		obj = builtinEOClip(name, argv);
		break;

	    case V_EOERASE:
		obj = builtinEOFill(name, argv, true);
		break;

	    case V_EOFILL:
		obj = builtinEOFill(name, argv, false);
		break;

	    case V_ERASE:
		obj = builtinFill(name, argv, true);
		break;

	    case V_ERASEDRAWABLE:
		obj = builtinEraseDrawable(name, argv);
		break;

	    case V_FILL:
		obj = builtinFill(name, argv, false);
		break;

	    case V_GRESTORE:
		obj = builtinGRestore(name, argv);
		break;

	    case V_GRESTOREALL:
		obj = builtinGRestoreAll(name, argv);
		break;

	    case V_GSAVE:
		obj = builtinGSave(name, argv);
		break;

	    case V_IMAGEPATH:
		obj = builtinImagePath(name, argv);
		break;

	    case V_INEOFILL:
		obj = builtinInEOFill(name, argv);
		break;

	    case V_INFILL:
		obj = builtinInFill(name, argv);
		break;

	    case V_INITCLIP:
		obj = builtinInitClip(name, argv);
		break;

	    case V_INITGRAPHICS:
		obj = builtinInitGraphics(name, argv);
		break;

	    case V_INSTROKE:
		obj = builtinInStroke(name, argv);
		break;

	    case V_KSHOW:
		obj = builtinKShow(name, argv);
		break;

	    case V_RECTBUTTON:
		obj = builtinRectButton(name, argv);
		break;

	    case V_RECTCLIP:
		obj = builtinRectClip(name, argv);
		break;

	    case V_RECTCOPY:
		obj = builtinRectCopy(name, argv, false);
		break;

	    case V_RECTERASE:
		obj = builtinRectDraw(name, argv, false, true);
		break;

	    case V_RECTFILL:
		obj = builtinRectDraw(name, argv, false, false);
		break;

	    case V_RECTMOVE:
		obj = builtinRectCopy(name, argv, true);
		break;

	    case V_RECTSTROKE:
		obj = builtinRectDraw(name, argv, true, false);
		break;

	    case V_SETCMYKCOLOR:
		obj = builtinSetCMYKColor(name, argv);
		break;

	    case V_SETDASH:
		obj = builtinSetDash(name, argv);;
		break;

	    case V_SETFONT:
		obj = builtinSetFont(name, argv);
		break;

	    case V_SETGRADIENT:
		obj = builtinSetGradient(name, argv);
		break;

	    case V_SETGRAY:
		obj = builtinSetGray(name, argv);
		break;

	    case V_SETHSBCOLOR:
		obj = builtinSetHSBColor(name, argv);
		break;

	    case V_SETLINECAP:
		obj = builtinSetLineCap(name, argv);
		break;

	    case V_SETLINEJOIN:
		obj = builtinSetLineJoin(name, argv);
		break;

	    case V_SETLINEWIDTH:
		obj = builtinSetLineWidth(name, argv);
		break;

	    case V_SETMITERLIMIT:
		obj = builtinSetMiterLimit(name, argv);
		break;

	    case V_SETPATH:
		obj = builtinSetPath(name, argv);
		break;

	    case V_SETRGBCOLOR:
		obj = builtinSetRGBColor(name, argv);
		break;

	    case V_SETTEXTURE:
		obj = builtinSetTexture(name, argv);
		break;

	    case V_SHOW:
		obj = builtinShow(name, argv);
		break;

	    case V_SHOWIMAGE:
		obj = builtinShowImage(name, argv);
		break;

	    case V_STRINGADVANCE:
		obj = builtinStringAdvance(name, argv);
		break;

	    case V_STRINGBOUNDS:
		obj = builtinStringBounds(name, argv);
		break;

	    case V_STRINGWIDTH:
		obj = builtinStringWidth(name, argv);
		break;

	    case V_STROKE:
		obj = builtinStroke(name, argv);
		break;

	    case V_STROKEPATH:
		obj = builtinStrokePath(name, argv);
		break;

	    case V_WIDTHSHOW:
		obj = builtinWidthShow(name, argv);
		break;

	    default:
		obj = null;
		break;
	}

	return(obj);
    }


    protected final void
    finalize() {

	currenthints = null;
	currentdrawable = null;
	currentmatrix = null;
	currentclip = null;
	currentpaint = null;
	currentpath = null;
	currentstroke = null;
	currentfont = null;
	dasharray = null;
	super.finalize();
    }


    final AffineTransform
    getCompatibleAffineTransform(Graphics2D g) {

	YoixBodyMatrix  matrix;

	return((matrix = currentmatrix) != null
	    ? matrix.getCompatibleAffineTransform(g)
	    : g.getTransform()
	);
    }


    final Graphics2D
    getCompatibleGraphics(boolean erase) {

	Graphics2D  g;

	if ((g = getGraphics2D(erase)) != null)
	    g.setTransform(getCompatibleAffineTransform(g));
	return(g);
    }


    final AffineTransform
    getCurrentAffineTransform() {

	YoixBodyMatrix  matrix;

	return((matrix = currentmatrix) != null
	    ? matrix.getCurrentAffineTransform()
	    : new AffineTransform()
	);
    }


    final synchronized YoixInterfaceFont
    getCurrentInterfaceFont() {

	return(currentfont != null ? currentfont.getCurrentFont() : null);
    }


    final YoixBodyMatrix
    getCurrentMatrix() {

	return(currentmatrix);
    }


    final YoixBodyPath
    getCurrentPath() {

	return(currentpath);
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_BACKGROUND:
		obj = getBackground(obj);
		break;

	    case V_CTM:
		obj = currentmatrix.getContext();
		break;

	    case V_DRAWABLE:
		obj = getDrawable(obj);
		break;

	    case V_FONT:
		obj = getFont(obj);
		break;

	    case V_FOREGROUND:
		obj = getForeground(obj);
		break;

	    case V_PATH:
		obj = currentpath.getContext();
		break;
	}

	return(obj);
    }


    final Graphics2D
    getGraphics2D() {

	return(getGraphics2D(true, false, 1.0));
    }


    final Graphics2D
    getGraphics2D(boolean erase) {

	return(getGraphics2D(true, erase, 1.0));
    }


    final Graphics2D
    getGraphics2D(double alpha) {

	return(getGraphics2D(true, false, alpha));
    }


    final synchronized Graphics2D
    getGraphics2D(boolean stroke, boolean erase, double alpha) {

	Graphics2D  g = null;
	YoixObject  background;
	Object      dest;

	//
	// Explicitly adding currentstroke is a recent change the we made
	// on 3/23/08 because we saw evidence, both in a Yoix script and
	// a simple standalone Java program, that the Graphics2D's fill()
	// can be affected by the stroke stored in that Graphics2D object.
	// It was unexpected behavior that we only verified on Linux using
	// 1.6 (it didn't happen with 1.5) and it took a somewhat unusual
	// example (a very long line segment with one point near the center
	// of the screen and the second point having a large negative x or
	// or y coordinate). Perhaps fill() uses information in its stroke
	// object to pick an algorithm and when the linewidth is near zero
	// it ends up using a platform (or hardware) dependent algorithm
	// that doesn't always agree well with the pixels selected by the
	// more general (and maybe slower) algorithm.
	//
	// Adding currentstroke to the object we return means a Yoix script
	// can, if necessary, request a "large" linewidth when it needs to
	// compensate for bad behavior by fill(). The alternative would be
	// change pathFill() and pathEOFill() in YoixBodyPath so that they
	// explicitly select a sufficiently larget stroke before calling
	// fill() and restore the old value when fill() returned. We didn't
	// feel comfortable with that change, at least not now, because the
	// problem was intermittent (changing end points slightly affected
	// the results) and probably was platform (and hardware) dependent,
	// but we didn't much of an understaning of the bug.
	//

	if (currentdrawable != null) {
	    if ((dest = currentdrawable.getManagedDrawable(this)) != null) {
		if (dest instanceof YoixInterfaceDrawable)
		    g = (Graphics2D)((YoixInterfaceDrawable)dest).getPaintGraphics();
		else if (dest instanceof Image)
		    g = (Graphics2D)((Image)dest).getGraphics();
		else g = (Graphics2D)((Component)dest).getGraphics();	// should not happen!!
		if (g != null) {
		    g.setStroke(getCurrentStroke());	// 3/23/08 fill() kludge addition
		    if (currentclip != null)
			g.setClip(currentclip);
		    if (erase) {
			background = currentdrawable.getObject(N_BACKGROUND);
			g.setPaint(YoixMakeScreen.javaBackground(background));
			if (alpha != 1.0 || compositerule != YOIX_COMPOSITE_SRC_OVER) {
			    alpha = Math.max(0.0, Math.min(alpha, 1.0));
			    g.setComposite(AlphaComposite.getInstance(compositerule, (float)alpha));
			}
		    } else {
			g.setPaint(getCurrentPaint());
			if (alpha != 1.0 || compositerule != YOIX_COMPOSITE_SRC_OVER) {
			    alpha = Math.max(0.0, Math.min(alpha, 1.0));
			    g.setComposite(AlphaComposite.getInstance(compositerule, (float)alpha));
			}
			if (xormode)
			    g.setXORMode(YoixMake.javaColor(getBackground(null)));
		    }
		}
		g.setRenderingHints(currenthints);
	    }
	}
	return(g);
    }


    final synchronized void
    rectButton(double x, double y, double width, double height, double border, int state, Graphics2D g) {

	double  coords[];
	Color   color;

	scratchpath.pathReset();
	scratchpath.pathAlignedRectangle(x, y, width, height);
	scratchpath.pathFill(g);

	if (state >= 0) {
	    coords = currentmatrix.dtransform(border, border);
	    coords[0] = Math.round(coords[0]);
	    coords = currentmatrix.idtransform(coords[0], coords[0]);
	    if ((border = coords[0]) > 0) {
		color = g.getColor();

		scratchpath.pathReset();
		scratchpath.pathAlignedRectangleBottomRight(x, y, width, height, border);
		g.setColor(state > 0 ? color.brighter() : color.darker());
		scratchpath.pathFill(g);

		scratchpath.pathReset();
		scratchpath.pathAlignedRectangleTopLeft(x, y, width, height, border);
		g.setColor(state == 0 ? color.brighter() : color.darker());
		scratchpath.pathFill(g);

		g.setColor(color);
	    }
	}
    }


    final synchronized void
    rectFill(double x, double y, double width, double height, Graphics2D g) {

	scratchpath.pathReset();
	scratchpath.pathAlignedRectangle(x, y, width, height);
	scratchpath.pathFill(g);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_ANTIALIASING:
		    setAntiAliasing(obj);
		    break;

		case V_BACKGROUND:
		    setBackground(obj);
		    break;

		case V_COMPOSITERULE:
		    setCompositeRule(obj);
		    break;

		case V_CTM:
		    setCTM(obj);
		    break;

		case V_DASHARRAY:
		    setDashArray(obj);
		    break;

		case V_DASHPHASE:
		    setDashPhase(obj);
		    break;

		case V_DRAWABLE:
		    setDrawable(obj);
		    break;

		case V_FONT:
		    setFont(obj);
		    break;

		case V_FOREGROUND:
		    setForeground(obj);
		    break;

		case V_FRACTIONALMETRICS:
		    setFractionalMetrics(obj);
		    break;

		case V_LINECAP:
		    setLineCap(obj);
		    break;

		case V_LINEJOIN:
		    setLineJoin(obj);
		    break;

		case V_LINEWIDTH:
		    setLineWidth(obj);
		    break;

		case V_MITERLIMIT:
		    setMiterLimit(obj);
		    break;

		case V_PATH:
		    setPath(obj);
		    break;

		case V_RENDERING:
		    setRendering(obj);
		    break;

		case V_TEXTANTIALIASING:
		    setTextAntiAliasing(obj);
		    break;

		case V_XORMODE:
		    setXORMode(obj);
		    break;
	    }
	}

	return(obj);
    }


    final synchronized void
    setOwner(YoixObject obj) {

	//
	// Setting foreground here is currently only required when it's
	// a texture (i.e., an image) and drawable is a component, like
	// a JFrame or JDialog, that has decorations that can actually
	// be measured (see setForeground()). It those cases the call
	// should resync the texture to account for the decorations.
	//

	setDrawable(obj);
	if (obj.notNull()) {
	    data.get(N_DRAWABLE).setAccess(LR__);
	    setField(N_FOREGROUND);
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildGraphics() {

	currenthints = new RenderingHints(null);
	currentmatrix = (YoixBodyMatrix)YoixMake.yoixType(T_MATRIX).body();
	currentpath = (YoixBodyPath)YoixMake.yoixType(T_PATH).body();
	currentmatrix.setOwner(this);
	currentpath.setOwner(this);
	scratchpath = (YoixBodyPath)YoixMake.yoixType(T_PATH).body();
	scratchpath.setOwner(this);

	setField(N_CTM);
	setField(N_PATH);
	setField(N_FOREGROUND);
	setField(N_FONT);
	setField(N_LINECAP);
	setField(N_LINEJOIN);
	setField(N_LINEWIDTH);
	setField(N_MITERLIMIT);
	setField(N_DASHARRAY);
	setField(N_DASHPHASE);
	setField(N_COMPOSITERULE);
	setField(N_XORMODE);
	setField(N_ANTIALIASING);
	setField(N_TEXTANTIALIASING);
	setField(N_FRACTIONALMETRICS);
	setField(N_RENDERING);
    }


    private synchronized YoixObject
    builtinAShow(String name, YoixObject arg[]) {

	YoixInterfaceFont  font;

	if (arg.length == 3 || arg.length == 4) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isString() || arg[2].isNull()) {
			if (arg.length == 3 || arg[3].isNumber()) {
			    if ((font = getCurrentInterfaceFont()) != null) {
				font.fontAShow(
				    arg[2].stringValue(),
				    arg[0].doubleValue(),
				    arg[1].doubleValue(),
				    arg.length == 4 ? arg[3].doubleValue() : 1.0,
				    this
				);
			    }
			} else VM.badArgument(name, 3);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinAWidthShow(String name, YoixObject arg[]) {

	YoixInterfaceFont  font;

	if (arg.length == 6 || arg.length == 7) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			if (arg[3].isNumber()) {
			    if (arg[4].isNumber()) {
				if (arg[5].isString() || arg[5].isNull()) {
				    if (arg.length == 6 || arg[6].isNumber()) {
					if ((font = getCurrentInterfaceFont()) != null) {
					    font.fontAWidthShow(
						arg[5].stringValue(),
						arg[0].doubleValue(),
						arg[1].doubleValue(),
						arg[2].intValue(),
						arg[3].doubleValue(),
						arg[4].doubleValue(),
						arg.length == 7 ? arg[6].doubleValue() : 1.0,
						this
					    );
					}
				    } else VM.badArgument(name, 6);
				} else VM.badArgument(name, 5);
			    } else VM.badArgument(name, 4);
			} else VM.badArgument(name, 3);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinCharPath(String name, YoixObject arg[]) {

	YoixInterfaceFont  font;
	boolean            stroke;

	//
	// Decided not accept the second argument that's supported by
	// the PostScript version, at least not right now. It's hard
	// to document, particuarly because we don't currently support
	// stroked fonts.
	//

	if (arg.length == 1) {		// eventually may allow arg.length == 2
	    if (arg[0].isString() || arg[0].isNull()) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    stroke = (arg.length == 2) && arg[1].booleanValue();
		    if ((font = getCurrentInterfaceFont()) != null)
			font.fontCharPath(arg[0].stringValue(), stroke, this);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinClip(String name, YoixObject arg[]) {

	GeneralPath  cp;
	Rectangle    rect;
	boolean      clear;
	boolean      subtract;
	Area         area;

	if (arg.length == 0 || arg.length == 1 || arg.length == 2) {
	    if (arg.length == 0 || arg[0].isNumber()) {
		if (arg.length <= 1 || arg[1].isNumber()) {
		    clear = (arg.length == 0) ? false : arg[0].booleanValue();
		    subtract = (arg.length <= 1) ? false : arg[1].booleanValue();
		    if (subtract && currentclip == null) {
			if ((rect = getDrawableBounds()) != null)
			    currentclip = new GeneralPath(rect);
		    }
		    if (currentclip != null) {
			area = new Area(currentclip);
			cp = currentpath.copyGeneralPath();
			cp.setWindingRule(YOIX_WIND_NON_ZERO);
			if (subtract)
			    area.subtract(new Area(cp));
			else area.intersect(new Area(cp));
			currentclip = new GeneralPath(area);
			currentclip.setWindingRule(YOIX_WIND_NON_ZERO);
		    } else {
			currentclip = currentpath.copyGeneralPath();
			currentclip.setWindingRule(YOIX_WIND_NON_ZERO);
		    }
		    if (clear)
			currentpath.pathReset();
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinClipPath(String name, YoixObject arg[]) {

	Rectangle  screen;
	Object     dest;
	Area       area = null;

	//
	// In older version Yoix scripts that used code something like,
	//
	//	gsave();
	//	clippath();
	//	pathbbox();
	//	...
	//	grestore();
	//
	// could, under unusual circumstances, have problems because the
	// implemention of pathbbox() aborts if there's no current point.
	// It happened when area.isEmpty() was true and the pathSetShape()
	// method in YoixBodyPath, rather than in this method, is where we
	// addressed the behavior. The problem only surfaced in an extreme
	// test where we rapidly resized a window while the Yoix script was
	// executing something like the above code in the screen's paint()
	// function. The fix was done on 1/10/08.
	//

	if (arg.length == 0) {
	    screen = new Rectangle(YoixAWTToolkit.getScreenSize());
	    if (currentdrawable != null) {
		if ((dest = currentdrawable.getManagedDrawable(this)) != null) {
		    if ((area = YoixMiscJFC.getDrawableArea(dest)) == null)
			 area = new Area(screen);
		} else area = new Area(screen);
	    } else area = new Area(screen);
	    if (currentclip != null)
		area.intersect(new Area(currentclip));
	    currentpath.pathSetShape(area);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinDrawableBBox(String name, YoixObject arg[]) {

	Rectangle2D  rect;
	YoixObject   obj = null;
	Rectangle    screen;
	Object       dest;
	Area         area = null;

	if (arg.length == 0) {
	    screen = new Rectangle(YoixAWTToolkit.getScreenSize());
	    if (currentdrawable != null) {
		if ((dest = currentdrawable.getManagedDrawable(this)) != null) {
		    if ((area = YoixMiscJFC.getDrawableArea(dest)) == null)
			 area = new Area(screen);
		} else area = new Area(screen);
	    } else area = new Area(screen);
	    rect = YoixMake.javaBBox(area.getBounds2D(), currentmatrix);
	    obj = YoixMake.yoixType(T_RECTANGLE);
	    obj.put(N_X, YoixObject.newDouble(rect.getX()), false);
	    obj.put(N_Y, YoixObject.newDouble(rect.getY()), false);
	    obj.put(N_WIDTH, YoixObject.newDouble(rect.getWidth()), false);
	    obj.put(N_HEIGHT, YoixObject.newDouble(rect.getHeight()), false);
	} else VM.badCall(name);

	return(obj != null ? obj : YoixObject.newRectangle());
    }


    private synchronized YoixObject
    builtinEOClip(String name, YoixObject arg[]) {

	GeneralPath  cp;
	Rectangle    rect;
	boolean      clear;
	boolean      subtract;
	Area         area;

	if (arg.length == 0 || arg.length == 1 || arg.length == 2) {
	    if (arg.length == 0 || arg[0].isNumber()) {
		if (arg.length <= 1 || arg[1].isNumber()) {
		    clear = (arg.length == 0) ? false : arg[0].booleanValue();
		    subtract = (arg.length <= 1) ? false : arg[1].booleanValue();
		    if (subtract && currentclip == null) {
			if ((rect = getDrawableBounds()) != null)
			    currentclip = new GeneralPath(rect);
		    }
		    if (currentclip != null) {
			area = new Area(currentclip);
			cp = currentpath.copyGeneralPath();
			cp.setWindingRule(YOIX_WIND_EVEN_ODD);
			if (subtract)
			    area.subtract(new Area(cp));
			else area.intersect(new Area(cp));
			currentclip = new GeneralPath(area);
			currentclip.setWindingRule(YOIX_WIND_EVEN_ODD);
		    } else {
			currentclip = currentpath.copyGeneralPath();
			currentclip.setWindingRule(YOIX_WIND_EVEN_ODD);
		    }
		    if (clear)
			currentpath.pathReset();
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinEOFill(String name, YoixObject arg[], boolean erase) {

	Graphics2D  g;
	double      alpha;

	if (arg.length <= 2) {
	    if (arg.length == 0 || arg[0].isNumber()) {
		if (arg.length <= 1 || arg[1].isNumber()) {
		    if (arg.length > 0)
			alpha = Math.max(Math.min(arg[0].doubleValue(), 1.0), 0.0);
		    else alpha = 1.0;
		    if ((g = getGraphics2D(false, erase, alpha)) != null) {
			currentpath.pathEOFill(g);
			g.dispose();
		    }
		    if (arg.length < 2 || arg[1].booleanValue())
			currentpath.pathReset();
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinEraseDrawable(String name, YoixObject arg[]) {

	YoixBodyImage  image;
	YoixBodyPath   path;
	Graphics2D     g;
	Rectangle      rect;
	Object         dest;
	double         alpha;

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 0 || arg[0].isNumber()) {
		if (currentdrawable != null) {
		    if (arg.length == 1)
			alpha = Math.max(Math.min(arg[0].doubleValue(), 1.0), 0.0);
		    else alpha = 1.0;
		    if (currentdrawable.isImage() == false) {
			if ((dest = currentdrawable.getManagedDrawable(this)) != null) {
			    if ((g = getGraphics2D(false, true, alpha)) != null) {
				if (dest instanceof YoixInterfaceDrawable) {
				    ((YoixInterfaceDrawable)dest).paintBackground(g);
				    ((YoixInterfaceDrawable)dest).paintBackgroundImage(g);
				} else if ((rect = getDrawableBounds()) != null) {
				    path = (YoixBodyPath)YoixMake.yoixType(T_PATH).body();
				    path.setOwner(this);
				    path.pathSetShape(rect);
				    path.pathFill(g);
				}
				g.dispose();
			    }
			}
		    } else if ((image = (YoixBodyImage)currentdrawable.body()) != null)
			image.eraseCurrentImage(alpha);
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinFill(String name, YoixObject arg[], boolean erase) {

	Graphics2D  g;
	double      alpha;

	if (arg.length <= 2) {
	    if (arg.length == 0 || arg[0].isNumber()) {
		if (arg.length <= 1 || arg[1].isNumber()) {
		    if (arg.length > 0)
			alpha = Math.max(Math.min(arg[0].doubleValue(), 1.0), 0.0);
		    else alpha = 1.0;
		    if ((g = getGraphics2D(false, erase, alpha)) != null) {
			currentpath.pathFill(g);
			g.dispose();
		    }
		    if (arg.length < 2 || arg[1].booleanValue())
			currentpath.pathReset();
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinGRestore(String name, YoixObject arg[]) {

	Object  details;
	String  savename;
	int     level = 0;
	int     n;

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 1) {
		if (arg[0].isString() || arg[0].isNull()) {
		    if (arg[0].notNull()) {
			savename = arg[0].stringValue();
			for (n = 0; n < savestack.size(); n++) {
			    details = savestack.elementAt(n);
			    if (savename.equals(((Object[])details)[0])) {
				level = savestack.size() - n;
				break;
			    }
			}
		    } else level = savestack.size();
		} else VM.badArgument(name, 0);
		while (level > 0 && savestack.size() > level)
		    savestack.removeElementAt(0);
	    } else level = savestack.size();
	    if (level > 0 && savestack.size() == level) {
		details = savestack.elementAt(0);
		savestack.removeElementAt(0);
		graphicsRestore(details);
	    }
	} else VM.badCall(name);

	return(YoixObject.newInt(savestack.size()));
    }


    private synchronized YoixObject
    builtinGRestoreAll(String name, YoixObject arg[]) {

	Object  details;

	if (arg.length == 0) {
	    if (savestack.size() > 0) {
		details = savestack.lastElement();
		savestack.removeAllElements();
		graphicsRestore(details);
	    }
	} else VM.badCall(name);

	return(YoixObject.newInt(savestack.size()));
    }


    private synchronized YoixObject
    builtinGSave(String name, YoixObject arg[]) {

	String  savename;

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 0 || arg[0].isString() || arg[0].isNull()) {
		if (arg.length == 1) {
		    if (arg[0].notNull())
			savename = arg[0].stringValue();
		    else savename = null;
		} else savename = null;
		savestack.insertElementAt(graphicsSave(savename), 0);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newInt(savestack.size()));
    }


    private synchronized YoixObject
    builtinImagePath(String name, YoixObject arg[]) {

	YoixBodyMatrix  matrix;
	YoixBodyMatrix  extra;
	YoixBodyImage   imagebody;
	BufferedImage   image;
	double          metrics[];
	double          delta[];
	double          sb[];
	double          pt[];
	double          pt0[];
	double          pt1[];
	double          pt2[];
	double          pt3[];
	double          width;
	double          height;
	double          x0;
	double          y0;

	//
	// Calculations are a bit hard to follow, but we want to end
	// up with is a transformation matrix (stored in matrix) that
	// maps pixels in the image to device space. Stuff here should
	// closely match the implementation of the showimage builtin,
	// so be careful making changes.
	//
	// There's still much room for improvement - inverting a copy
	// of currentmatrix and concat it with matrix and the second
	// set of transformations could be eliminated.
	//

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isImage() || arg[0].isNull()) {
		if (arg.length == 1 || arg[1].isMatrix() || arg[1].isNull()) {
		    if (arg.length > 1 && arg[1].isMatrix())
			extra = (YoixBodyMatrix)arg[1].body();
		    else extra = null;
		    if ((pt = currentpath.getCurrentDevicePoint()) != null) {
			if ((image = (BufferedImage)arg[0].getManagedDrawable(this)) != null) {
			    imagebody = (YoixBodyImage)arg[0].body();
			    metrics = imagebody.getMetrics(image);
			    matrix = (YoixBodyMatrix)YoixMake.yoixType(T_MATRIX).body();
			    matrix.setMatrix(currentmatrix);
			    delta = matrix.idtransform(metrics[2], metrics[3]);
			    matrix.divide((YoixBodyMatrix)VM.getDefaultMatrix().body());

			    sb = matrix.dtransform(metrics[0], metrics[1]);
			    delta = matrix.dtransform(delta[0], delta[1]);
			    pt = matrix.itransform((int)pt[0] + sb[0], (int)pt[1] + sb[1]);
			    matrix.translate(pt[0], pt[1]);
			    if (extra != null)
				matrix.concat(extra);
			    x0 = -1;
			    y0 = -1;
			    width = image.getWidth() + 1;
			    height = image.getHeight() + 1;
			    pt0 = matrix.transform(x0, y0);
			    pt1 = matrix.transform(width, y0);
			    pt2 = matrix.transform(width, height);
			    pt3 = matrix.transform(x0, height);
			    pt0 = currentmatrix.itransform(pt0[0], pt0[1]);
			    pt1 = currentmatrix.itransform(pt1[0], pt1[1]);
			    pt2 = currentmatrix.itransform(pt2[0], pt2[1]);
			    pt3 = currentmatrix.itransform(pt3[0], pt3[1]);
			    pt = currentpath.getCurrentPoint();
			    currentpath.pathMoveTo(pt0[0], pt0[1]);
			    currentpath.pathLineTo(pt1[0], pt1[1]);
			    currentpath.pathLineTo(pt2[0], pt2[1]);
			    currentpath.pathLineTo(pt3[0], pt3[1]);
			    currentpath.pathClosePath();
			    currentpath.pathMoveTo(pt[0] + delta[0], pt[1] + delta[1]);
			}
		    } else VM.abort(NOCURRENTPOINT, name);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinInitClip(String name, YoixObject arg[]) {

	if (arg.length == 0)
	    currentclip = null;
	else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinInitGraphics(String name, YoixObject arg[]) {

	if (arg.length == 0) {
	    currentmatrix.matrixReset();
	    currentpath.pathReset();
	    currentclip = null;
	    currentpaint = null;
	    currentstroke = null;
	    setField(N_LINECAP, YoixObject.newInt(YOIX_CAP_SQUARE));
	    setField(N_LINEJOIN, YoixObject.newInt(YOIX_JOIN_MITER));
	    setField(N_LINEWIDTH, YoixObject.newInt(1));
	    setField(N_MITERLIMIT, YoixObject.newDouble(10.0));
	    setField(N_BACKGROUND, YoixObject.newNull());
	    setField(N_FOREGROUND, YoixObject.newNull());
	    setField(N_DASHARRAY, YoixObject.newNull());
	    setField(N_DASHPHASE, YoixObject.newDouble(0.0));
	    setField(N_COMPOSITERULE, YoixObject.newInt(YOIX_COMPOSITE_SRC_OVER));
	    setField(N_XORMODE, YoixObject.newInt(false));
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinInEOFill(String name, YoixObject arg[]) {

	boolean  result = false;
	double   x = 0;
	double   y = 0;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].isPoint()) {
		    x = arg[0].getDouble(N_X, 0);
		    y = arg[0].getDouble(N_Y, 0);
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			x = arg[0].doubleValue();
			y = arg[1].doubleValue();
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	    result = currentpath.pathInEOFill(x, y);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    builtinInFill(String name, YoixObject arg[]) {

	boolean  result = false;
	double   x = 0;
	double   y = 0;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].isPoint()) {
		    x = arg[0].getDouble(N_X, 0);
		    y = arg[0].getDouble(N_Y, 0);
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			x = arg[0].doubleValue();
			y = arg[1].doubleValue();
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	    result = currentpath.pathInFill(x, y);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    builtinInStroke(String name, YoixObject arg[]) {

	boolean  result = false;
	double   x = 0;
	double   y = 0;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].isPoint()) {
		    x = arg[0].getDouble(N_X, 0);
		    y = arg[0].getDouble(N_Y, 0);
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			x = arg[0].doubleValue();
			y = arg[1].doubleValue();
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	    result = currentpath.pathInStroke(x, y, getCurrentStroke());
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    builtinKShow(String name, YoixObject arg[]) {

	YoixInterfaceFont  font;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].callable(2) || arg[0].isNull()) {
		if (arg[1].isString() || arg[1].isNull()) {
		    if (arg.length == 2 || arg[2].isNumber()) {
			if ((font = getCurrentInterfaceFont()) != null) {
			    font.fontKShow(
				arg[1].stringValue(),
				arg[0],
				arg.length == 3 ? arg[2].doubleValue() : 1.0,
				this
			    );
			}
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinRectButton(String name, YoixObject arg[]) {

	YoixBodyPath  path;
	Graphics2D    g;
	boolean       haveshadow = false;
	double        values[] = null;
	double        coords[];
	double        corners[];
	double        border = 0;
	Color         color;
	int           type;
	int           state = 0;

	//
	// A modified version of buildtinPathDraw() that can be used to draw
	// rectangles with borders that make them look like raised or lowered
	// buttons.
	//

	if (arg.length >= 1 && arg.length <= 6) {
	    if (arg.length < 4) {
		if (arg[0].isRectangle()) {
		    if (arg.length <= 1 || arg[1].isNumber()) {
			if (arg.length <= 2 || arg[2].isNumber()) {
			    values = new double[] {
				arg[0].getDouble(N_X, 0),
				arg[0].getDouble(N_Y, 0),
				arg[0].getDouble(N_WIDTH, 0),
				arg[0].getDouble(N_HEIGHT, 0)
			    };
			    border = (arg.length > 1) ? arg[1].doubleValue() : 0;
			    state = (arg.length > 2) ? arg[2].intValue() : 0;
			} else VM.badArgument(name, 1);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else if (arg.length >= 4) {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    if (arg[3].isNumber()) {
				if (arg.length <= 4 || arg[4].isNumber()) {
				    if (arg.length <= 5 || arg[5].isNumber()) {
					values = new double[] {
					    arg[0].doubleValue(),
					    arg[1].doubleValue(),
					    arg[2].doubleValue(),
					    arg[3].doubleValue()
					};
					border = (arg.length > 4) ? arg[4].doubleValue() : 0;
					state = (arg.length > 5) ? arg[5].intValue() : 0;
				    } else VM.badArgument(name, 5);
				} else VM.badArgument(name, 4);
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	    if ((g = getGraphics2D(false, false, 1.0)) != null) {
		path = scratchpath;
		path.pathReset();
		path.pathAlignedRectangle(values[0], values[1], values[2], values[3]);
		path.pathFill(g);

		if (border > 0 && state >= 0) {
		    coords = currentmatrix.dtransform(border, 0);
		    coords[0] = Math.round(coords[0]);
		    coords[1] =  Math.round(coords[1]);
		    coords = currentmatrix.idtransform(coords[0], coords[1]);
		    if ((border = coords[0]) > 0) {
			color = g.getColor();

			path.pathReset();
			path.pathAlignedRectangleBottomRight(values[0], values[1], values[2], values[3], border);
			g.setColor(state > 0 ? color.brighter() : color.darker());
			path.pathFill(g);

			path.pathReset();
			path.pathAlignedRectangleTopLeft(values[0], values[1], values[2], values[3], border);
			g.setColor(state == 0 ? color.brighter() : color.darker());
			path.pathFill(g);
		    }
		}
		g.dispose();
	    }
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinRectClip(String name, YoixObject arg[]) {

	YoixBodyPath  path;
	GeneralPath   cp;
	Rectangle     rect;
	boolean       clear = false;
	boolean       subtract = false;
	double        values[];
	Area          area;

	if (arg.length >= 1 && arg.length <= 6) {
	    values = null;
	    if (arg.length < 4 ) {
		if (arg[0].isRectangle() || arg[0].isNull()) {
		    if (arg.length <= 1 || arg[1].isNumber()) {
			if (arg.length <= 2 || arg[2].isNumber()) {
			    if (arg[0].notNull()) {
				values = new double[] {
				    arg[0].getDouble(N_X, 0),
				    arg[0].getDouble(N_Y, 0),
				    arg[0].getDouble(N_WIDTH, 0),
				    arg[0].getDouble(N_HEIGHT, 0)
				};
			    } else values = null;
			    clear = (arg.length <= 1) ? false : arg[1].booleanValue();
			    subtract = (arg.length <= 2) ? false : arg[2].booleanValue();
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    if (arg[3].isNumber()) {
				if (arg.length <= 4 || arg[4].isNumber()) {
				    if (arg.length <= 5 || arg[5].isNumber()) {
					values = new double[] {
					    arg[0].doubleValue(),
					    arg[1].doubleValue(),
					    arg[2].doubleValue(),
					    arg[3].doubleValue()
					};
					clear = (arg.length <= 4) ? false : arg[4].booleanValue();
					subtract = (arg.length <= 5) ? false : arg[5].booleanValue();
				    } else VM.badArgument(name, 5);
				} else VM.badArgument(name, 4);
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	    if (values != null) {
		path = clear ? currentpath : scratchpath;
		path.pathReset();
		path.pathAlignedRectangle(values[0], values[1], values[2], values[3]);
		if (subtract && currentclip == null) {
		    if ((rect = getDrawableBounds()) != null)
			currentclip = new GeneralPath(rect);
		}
		if (currentclip != null) {
		    area = new Area(currentclip);
		    cp = path.copyGeneralPath();
		    cp.setWindingRule(YOIX_WIND_NON_ZERO);
		    if (subtract)
			area.subtract(new Area(cp));
		    else area.intersect(new Area(cp));
		    currentclip = new GeneralPath(area);
		} else currentclip = path.copyGeneralPath();
		if (clear)
		    path.pathReset();
	    }
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinRectCopy(String name, YoixObject arg[], boolean move) {

	double  values[] = null;
	double  coords[];
	double  dx;
	double  dy;
	int     argn = arg.length;

	if (arg.length == 3 || arg.length == 4 || arg.length == 6 || arg.length == 7) {
	    if (arg.length == 3 || arg.length == 4) {
		if (arg[0].isRectangle() || arg[0].isNull()) {
		    if (arg.length == 1 || arg[1].isNumber()) {
			if (arg[0].notNull()) {
			    values = new double[] {
				arg[0].getDouble(N_X, 0),
				arg[0].getDouble(N_Y, 0),
				arg[0].getDouble(N_WIDTH, 0),
				arg[0].getDouble(N_HEIGHT, 0)
			    };
			    argn = 1;
			} else values = null;
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    if (arg[3].isNumber()) {
				values = new double[] {
				    arg[0].doubleValue(),
				    arg[1].doubleValue(),
				    arg[2].doubleValue(),
				    arg[3].doubleValue()
				};
				argn = 4;
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }
	    if (arg[argn].isNumber()) {
		if (arg[argn+1].isNumber()) {
		    if (argn+2 <= arg.length || arg[argn+2].isNumber()) {
			dx = arg[argn].doubleValue();
			dy = arg[argn+1].doubleValue();
			coords = currentmatrix.dtransform(dx, dy);
			coords[0] = Math.round(coords[0]);
			coords[1] = Math.round(coords[1]);
			coords = currentmatrix.idtransform(coords[0], coords[1]);
			dx = coords[0];
			dy = coords[1];
			if (dx != 0 || dy != 0) {
			    //
			    // Automatically adjusts the rectangle's upper left
			    // corner when move is true and the first argument
			    // was a rectangle. The assumption we're making is
			    // that a rectangle argument somehow controls the
			    // painting of the appropriate area on the screen,
			    // so the automatic repaints generated when we call
			    // YoixMiscJFC.copyArea() will be correct. There's
			    // no magic here - the script that called us must
			    // also make sure its painting code consults the
			    // rectangle that we adjust!!
			    //
			    if (move && arg[0].isRectangle()) {
				arg[0].putDouble(N_X, values[0] + dx);
				arg[0].putDouble(N_Y, values[1] + dy);
			    }
			    YoixMiscJFC.copyArea(
				currentdrawable,
				values[0],
				values[1],
				values[2],
				values[3],
				dx,
				dy,
				argn+2 < arg.length ? arg[argn+2].booleanValue() : move
			    );
			}
		    } else VM.badArgument(name, argn+2);
		} else VM.badArgument(name, argn+1);
	    } else VM.badArgument(name, argn);

	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinRectDraw(String name, YoixObject arg[], boolean stroke, boolean erase) {

	YoixBodyPath  path;
	Graphics2D    g;
	double        values[];
	double        alpha;

	if (arg.length > 0 && arg.length < 6) {
	    values = null;
	    alpha = 1.0;
	    if (arg.length < 3) {
		if (arg[0].isRectangle() || arg[0].isNull()) {
		    if (arg.length == 1 || arg[1].isNumber()) {
			if (arg[0].notNull()) {
			    values = new double[] {
				arg[0].getDouble(N_X, 0),
				arg[0].getDouble(N_Y, 0),
				arg[0].getDouble(N_WIDTH, 0),
				arg[0].getDouble(N_HEIGHT, 0)
			    };
			} else values = null;
			alpha = (arg.length == 2) ? arg[1].doubleValue() : 1;
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else if (arg.length > 3) {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    if (arg[3].isNumber()) {
				values = new double[] {
				    arg[0].doubleValue(),
				    arg[1].doubleValue(),
				    arg[2].doubleValue(),
				    arg[3].doubleValue()
				};
				alpha = (arg.length == 5) ? arg[4].doubleValue() : 1;
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	    alpha = Math.max(Math.min(alpha, 1.0), 0.0);
	    if ((g = getGraphics2D(stroke, erase, alpha)) != null) {
		path = scratchpath;
		path.pathReset();
		if (values != null)
		    path.pathAlignedRectangle(values[0], values[1], values[2], values[3]);
		else path.pathSetShape(getDrawableBounds());

		if (stroke)
		    path.pathDraw(g);
		else path.pathFill(g);
		g.dispose();
	    }
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinSetCMYKColor(String name, YoixObject arg[]) {

	boolean use_adobe = true;
	Color   color;
	float   cyan;
	float   magenta;
	float   yellow;
	float   black;

	if (arg.length == 3 || arg.length == 4 || arg.length == 5) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			if (arg.length == 3 || arg[3].isNumber()) {
			    cyan = Math.max(Math.min(arg[0].floatValue(), 1.0f), 0.0f);
			    magenta = Math.max(Math.min(arg[1].floatValue(), 1.0f), 0.0f);
			    yellow = Math.max(Math.min(arg[2].floatValue(), 1.0f), 0.0f);
			    if (arg.length >= 4)
				black = Math.max(Math.min(arg[3].floatValue(), 1.0f), 0.0f);
			    else black = 0.0f;
			    if (arg.length > 4) {
				if (arg[4].isNumber())
				    use_adobe = arg[4].booleanValue();
				else VM.badArgument(4);
			    }
			    if (use_adobe) {	// from Adobe's PostScript Language Reference Manual
				color = new Color(
				    1.0f - Math.min(1.0f, cyan + black),
				    1.0f - Math.min(1.0f, magenta + black),
				    1.0f - Math.min(1.0f, yellow + black)
				);
			    } else {		// from wikipedia.org (CMYK entry) or comp.graphics FAQ
				color = new Color(
				    1.0f - Math.min(1.0f, cyan*(1.0f - black) + black),
				    1.0f - Math.min(1.0f, magenta*(1.0f - black) + black),
				    1.0f - Math.min(1.0f, yellow*(1.0f - black) + black)
				);
			    }
			    currentpaint = color;
			} else VM.badArgument(name, 3);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinSetDash(String name, YoixObject arg[]) {

	YoixObject  phase;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isArray() || arg[0].isNull()) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    setDashArray(arg[0]);
		    setDashPhase(arg.length == 2 ? arg[1] : YoixObject.newInt(0));
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinSetFont(String name, YoixObject arg[]) {

	YoixInterfaceFont  font;
	YoixObject         obj;
	double             scale;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isFont() || arg[0].isString() || arg[0].isNull()) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    if (arg[0].isString() || arg[0].isNull())
			obj = YoixMake.yoixFont(YoixMakeScreen.javaFont(arg[0]));
		    else obj = arg[0];
		    if (arg.length == 2) {
			if (obj != null && obj.isFont()) {
			    font = ((YoixBodyFont)obj.body()).getCurrentFont();
			    scale = arg[1].doubleValue();
			    obj = font.fontScaleFont(scale, scale);
			}
		    }
		    setFont(obj);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinSetGradient(String name, YoixObject arg[]) {

	YoixObject  obj;

	//
	// Using put() to update currentpaint means the data entry will
	// be valid, and that in turn means getForeground() doesn't have
	// to do anything special when currentpaint is a GradientPaint.
	//

	if (arg.length == 6 || arg.length == 7) {
	    if (arg[0].isColor()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			if (arg[3].isColor()) {
			    if (arg[4].isNumber()) {
				if (arg[5].isNumber()) {
				    if (arg.length == 6 || arg[6].isNumber()) {
					obj = YoixMisc.copyIntoArray(arg, false);
					put(N_FOREGROUND, obj, false);		// syncs data entry
				    } else VM.badArgument(name, 6);
				} else VM.badArgument(name, 5);
			    } else VM.badArgument(name, 4);
			} else VM.badArgument(name, 3);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinSetGray(String name, YoixObject arg[]) {

	Color  color;
	float  gray;

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		gray = Math.max(Math.min(arg[0].floatValue(), 1.0f), 0.0f);
		color = new Color(gray, gray, gray);
		currentpaint = color;
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinSetHSBColor(String name, YoixObject arg[]) {

	Color  color;

	if (arg.length == 3) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			color = Color.getHSBColor(
			    (float)Math.max(Math.min(arg[0].doubleValue(), 1.0), 0.0),
			    (float)Math.max(Math.min(arg[1].doubleValue(), 1.0), 0.0),
			    (float)Math.max(Math.min(arg[2].doubleValue(), 1.0), 0.0)
			);
			currentpaint = color;
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinSetLineCap(String name, YoixObject arg[]) {

	if (arg.length == 1) {
	    if (arg[0].isNumber())
		setLineCap(arg[0]);
	    else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinSetLineJoin(String name, YoixObject arg[]) {

	if (arg.length == 1) {
	    if (arg[0].isNumber())
		setLineJoin(arg[0]);
	    else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinSetLineWidth(String name, YoixObject arg[]) {

	if (arg.length == 1) {
	    if (arg[0].isNumber())
		setLineWidth(arg[0]);
	    else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinSetMiterLimit(String name, YoixObject arg[]) {

	if (arg.length == 1) {
	    if (arg[0].isNumber())
		setMiterLimit(arg[0]);
	    else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinSetPath(String name, YoixObject arg[]) {

	if (arg.length == 1) {
	    if (arg[0].isPath() || arg[0].isNull())
		setPath(arg[0]);
	    else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinSetRGBColor(String name, YoixObject arg[]) {

	Color  color;

	if (arg.length == 3) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			color = new Color(
			    Math.max(Math.min(arg[0].floatValue(), 1.0f), 0.0f),
			    Math.max(Math.min(arg[1].floatValue(), 1.0f), 0.0f),
			    Math.max(Math.min(arg[2].floatValue(), 1.0f), 0.0f)
			);
			currentpaint = color;
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else if (arg.length == 1 && arg[0].isColor())
	    currentpaint = YoixMake.javaColor(arg[0]);
	else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinSetTexture(String name, YoixObject arg[]) {

	YoixObject  obj;

	//
	// Using put() to update currentpaint means the data entry will
	// be valid, and that in turn means getForeground() doesn't have
	// to do anything special when currentpaint is a TexturePaint.
	//

	if (arg.length == 1 || arg.length == 3) {
	    if (arg[0].isImage()) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    if (arg.length == 1 || arg[2].isNumber()) {
			if (arg.length == 3)
			    obj = YoixMisc.copyIntoArray(arg, false);
			else obj = arg[0];
			put(N_FOREGROUND, obj, false);		// syncs data entry
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinShow(String name, YoixObject arg[]) {

	YoixInterfaceFont  font;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    if ((font = getCurrentInterfaceFont()) != null) {
			font.fontShow(
			    arg[0].stringValue(),
			    arg.length == 2 ? arg[1].doubleValue() : 1.0,
			    this
			);
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinShowImage(String name, YoixObject arg[]) {

	AffineTransform  transform;
	YoixBodyMatrix   matrix;
	YoixBodyMatrix   extra;
	YoixBodyImage    imagebody;
	BufferedImage    image;
	Graphics2D       g;
	double           metrics[];
	double           delta[];
	double           sb[];
	double           pt[];
	double           alpha;

	//
	// Careful if you make any changes here - results here need to be
	// synchronized with the imagepath builtin.
	//

	if (arg.length == 1 || arg.length == 2 || arg.length == 3) {
	    if (arg[0].isImage() || arg[0].isNull()) {
		if (arg.length == 1 || arg[1].isMatrix() || arg[1].isNull() || arg[1].isNumber()) {
		    if (arg.length < 3 || arg[2].isNumber()) {
			if (arg.length > 1 && arg[arg.length - 1].isNumber())
			    alpha = arg[arg.length - 1].doubleValue();
			else alpha = 1.0;
			if (arg.length > 1 && arg[1].isMatrix())
			    extra = (YoixBodyMatrix)arg[1].body();
			else extra = null;
			if ((pt = currentpath.getCurrentDevicePoint()) != null) {
			    if ((image = (BufferedImage)arg[0].getManagedDrawable(this)) != null) {
				if ((g = getGraphics2D(false, false, alpha)) != null) {
				    imagebody = (YoixBodyImage)arg[0].body();
				    metrics = imagebody.getMetrics(image);
				    matrix = (YoixBodyMatrix)YoixMake.yoixType(T_MATRIX).body();
				    matrix.setMatrix(currentmatrix);
				    delta = matrix.idtransform(metrics[2], metrics[3]);
				    matrix.divide((YoixBodyMatrix)VM.getDefaultMatrix().body());
				    delta = matrix.dtransform(delta[0], delta[1]);
				    sb = matrix.dtransform(metrics[0], metrics[1]);
				    pt = matrix.itransform((int)pt[0] + sb[0], (int)pt[1] + sb[1]);
				    matrix.translate(pt[0], pt[1]);
				    if (extra != null)
					matrix.concat(extra);
				    transform = g.getTransform();
				    transform.concatenate(matrix.getCurrentAffineTransform());
				    g.setTransform(transform);
				    g.drawImage(image, 0, 0, null);
				    g.dispose();
				    currentpath.pathRMoveTo(delta[0], delta[1]);
				}
			    }
			} else VM.abort(NOCURRENTPOINT, name);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinStringAdvance(String name, YoixObject arg[]) {

	YoixInterfaceFont  font;
	YoixObject         advance = null;
	Point2D            point;

	if (arg.length == 1) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if ((font = getCurrentInterfaceFont()) != null) {
		    point = font.fontStringAdvance(arg[0].stringValue(), this);
		    advance = YoixObject.newDimension(point.getX(), point.getY());
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(advance != null ? advance : YoixMake.yoixType(T_POINT));
    }


    private synchronized YoixObject
    builtinStringBounds(String name, YoixObject arg[]) {

	YoixInterfaceFont  font;
	Rectangle2D        rect;
	YoixObject         bounds = null;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    if ((font = getCurrentInterfaceFont()) != null) {
			rect = font.fontStringBounds(
			    arg[0].stringValue(),
			    arg.length == 2 ? arg[1].booleanValue() : false,
			    this
			);
			bounds = YoixMakeScreen.yoixRectangle(rect);
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(bounds != null ? bounds : YoixMake.yoixType(T_RECTANGLE));
    }


    private synchronized YoixObject
    builtinStringWidth(String name, YoixObject arg[]) {

	YoixInterfaceFont  font;
	double             width = 0;

	if (arg.length == 1) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if ((font = getCurrentInterfaceFont()) != null) {
		    width = font.fontStringWidth(arg[0].stringValue(), this);
		    width = YoixMakeScreen.yoixDistance(width);
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newDouble(width));
    }


    private synchronized YoixObject
    builtinStroke(String name, YoixObject arg[]) {

	Graphics2D  g;
	double      alpha;

	if (arg.length <= 2) {
	    if (arg.length == 0 || arg[0].isNumber()) {
		if (arg.length <= 1 || arg[1].isNumber()) {
		    if (arg.length > 0)
			alpha = Math.max(Math.min(arg[0].doubleValue(), 1.0), 0.0);
		    else alpha = 1.0;
		    if ((g = getGraphics2D(true, false, alpha)) != null) {
			currentpath.pathDraw(g);
			g.dispose();
		    }
		    if (arg.length < 2 || arg[1].booleanValue())
			currentpath.pathReset();
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinStrokePath(String name, YoixObject arg[]) {

	if (arg.length == 0)
	    currentpath.pathStrokePath(getCurrentStroke());
	else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinWidthShow(String name, YoixObject arg[]) {

	YoixInterfaceFont  font;

	if (arg.length == 4 || arg.length == 5) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			if (arg[3].isString() || arg[3].isNull()) {
			    if (arg.length == 4 || arg[4].isNumber()) {
				if ((font = getCurrentInterfaceFont()) != null) {
				    font.fontWidthShow(
					arg[3].stringValue(),
					arg[0].doubleValue(),
					arg[1].doubleValue(),
					arg[2].intValue(),
					arg.length == 5 ? arg[4].doubleValue() : 1.0,
					this
				    );
				}
			    } else VM.badArgument(name, 4);
			} else VM.badArgument(name, 3);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private YoixObject
    getBackground(YoixObject obj) {

	if ((obj = data.getObject(N_BACKGROUND)) == null || obj.isNull())
	    obj = VM.getObject(N_BACKGROUND);
	return(obj);
    }


    private synchronized Paint
    getCurrentPaint() {

	if (currentpaint == null)
	    currentpaint = YoixMakeScreen.javaForeground(data.getObject(N_FOREGROUND));
	return(currentpaint);
    }


    private synchronized BasicStroke
    getCurrentStroke() {

	if (currentstroke == null) {
	    currentstroke = new BasicStroke(
		linewidth,
		linecap,
		linejoin,
		miterlimit,
		dasharray,
		dashphase
	    );
	}
	return(currentstroke);
    }


    private synchronized YoixObject
    getDrawable(YoixObject obj) {

	return(currentdrawable != null ? currentdrawable : YoixObject.newNull());
    }


    private synchronized Rectangle
    getDrawableBounds() {

	Rectangle  rect = null;
	Object     dest;

	if (currentdrawable != null) {
	    if ((dest = currentdrawable.getManagedDrawable(this)) != null) {
		if (dest instanceof Image) {
		    rect = new Rectangle();
		    rect.width = ((Image)dest).getWidth(null);
		    rect.height = ((Image)dest).getHeight(null);
		} else rect = new Rectangle(((Component)dest).getSize());
	    }
	}

	return(rect);
    }


    private synchronized YoixObject
    getFont(YoixObject obj) {

	return(currentfont != null ? currentfont.getContext() : YoixObject.newNull());
    }


    private synchronized YoixObject
    getForeground(YoixObject obj) {

	if (currentpaint != null) {
	    if (currentpaint instanceof Color)
		obj = YoixMake.yoixColor((Color)currentpaint);
	} else obj = VM.getObject(N_FOREGROUND);

	return(obj);
    }


    private synchronized void
    graphicsRestore(Object details) {

	YoixObject  value;
	Object      state[];
	int         mode;
	int         n;

	//
	// Reasonably efficient, but there may still be a little room
	// for improvement. Must be coordinated with graphicsSave().
	//

	if (details instanceof Object[]) {
	    VM.pushAccess(LRW_);
	    state = (Object[])details;
	    currentdrawable = (YoixObject)state[1];
	    currentmatrix.matrixRestore(state[2]);
	    currentpath.pathRestore(state[3]);
	    currentstroke = (BasicStroke)state[4];
	    currentclip = (GeneralPath)state[5];
	    currentpaint = (Paint)state[6];
	    currentfont = (YoixBodyFont)state[7];
	    dasharray = (float[])state[8];
	    dashphase = ((Float)state[9]).floatValue();
	    for (n = 0; n < gsavekeys.length; n++) {
		value = (YoixObject)state[n + 10];
		mode = value.getAccess();
		value = data.put(gsavekeys[n], value, false);
		value.setAccess(mode);
	    }
	    VM.popAccess();
	}
    }


    private synchronized Object
    graphicsSave(String name) {

	Object  state[];
	int     n;

	//
	// Reasonably efficient, but there may still be a little room
	// for improvement. Must be coordinated with graphicsRestore().
	//

	VM.pushAccess(LRW_);
	state = new Object[gsavekeys.length + 10];
	state[0] = name;
	state[1] = currentdrawable;
	state[2] = currentmatrix.matrixSave();
	state[3] = currentpath.pathSave();
	state[4] = currentstroke;
	state[5] = currentclip;
	state[6] = currentpaint;
	state[7] = currentfont;
	state[8] = dasharray;
	state[9] = new Float(dashphase);
	for (n = 0; n < gsavekeys.length; n++)
	    state[n + 10] = data.getObject(gsavekeys[n]);
	VM.popAccess();

	return(state);
    }


    private synchronized void
    setAntiAliasing(YoixObject obj) {

	Object  value;

	switch (obj.intValue()) {
	    case 0:
		value = RenderingHints.VALUE_ANTIALIAS_OFF;
		break;

	    case 1:
		value = RenderingHints.VALUE_ANTIALIAS_ON;
		break;

	    default:
		value = RenderingHints.VALUE_ANTIALIAS_DEFAULT;
		break;
	}

	currenthints.put(RenderingHints.KEY_ANTIALIASING, value);
    }


    private synchronized void
    setBackground(YoixObject obj) {

	if (obj.notNull()) {
	    obj = YoixMake.yoixColor(YoixMake.javaColor(obj));
	    data.put(N_BACKGROUND, obj, false);
	}
    }


    private synchronized void
    setCompositeRule(YoixObject obj) {

	int  value;

	switch (value = obj.intValue()) {
	    case YOIX_COMPOSITE_CLEAR:
	    case YOIX_COMPOSITE_DST_IN:
	    case YOIX_COMPOSITE_DST_OUT:
	    case YOIX_COMPOSITE_DST_OVER:
	    case YOIX_COMPOSITE_SRC:
	    case YOIX_COMPOSITE_SRC_IN:
	    case YOIX_COMPOSITE_SRC_OUT:
	    case YOIX_COMPOSITE_SRC_OVER:
		compositerule = value;
		break;

	    default:
		compositerule = YOIX_COMPOSITE_SRC_OVER;
		break;
	}
    }


    private synchronized void
    setCTM(YoixObject obj) {

	if (currentmatrix != null) {
	    if (obj.isNull())
		obj = VM.getDefaultMatrix();
	    currentmatrix.setMatrix((YoixBodyMatrix)obj.body());
	}
    }


    private synchronized void
    setDashArray(YoixObject obj) {

	boolean  dashed;
	float    array[];
	int      length;
	int      m;
	int      n;

	if (obj.notNull() && obj.sizeof() > 0) {
	    dashed = false;
	    array = new float[obj.sizeof()];
	    length = obj.length();
	    for (n = obj.offset(), m = 0; n < length; n++, m++) {
		array[m] = (float)Math.max((float)obj.getDouble(n, 0), 0.0);
		if (dashed == false && array[m] > 0)
		    dashed = true;
	    }
	    if (dashed) {
		if (dasharray != null) {
		    if (dasharray.length == array.length) {
			for (n = 0; n < dasharray.length; n++) {
			    if (dasharray[n] != array[n])
				break;
			}
			if (n == dasharray.length)
			    array = dasharray;
		    }
		}
	    } else array = null;
	} else array = null;

	dasharray = array;
	currentstroke = null;
    }


    private synchronized void
    setDashPhase(YoixObject obj) {

	dashphase = (float)Math.max(obj.doubleValue(), 0.0);
	currentstroke = null;
    }


    private synchronized void
    setDrawable(YoixObject obj) {

	if (obj.isDrawable() || obj.isNull()) {
	    if (obj.notNull())
		currentdrawable = obj;
	    else currentdrawable = null;
	} else VM.abort(TYPECHECK, N_DRAWABLE);
    }


    private synchronized void
    setFont(YoixObject obj) {

	YoixObject  font;

	//
	// We're now keep our own local copy of the current font, so
	// we toss the copy in data and no longer try to keep it up
	// to date. Means gsave() and grestore() don't have to worry
	// about the N_FONT entry in data, but it also means that we
	// shouldn't go directly into data when we need the current
	// font!! In other words, data.getObject(N_FONT) won't work.
	//

	data.put(N_FONT, YoixObject.newNull(), false);		// toss it

	if (obj != null && obj.notNull()) {
	    if (obj.isFont() == false)
		obj = YoixMake.yoixFont(YoixMakeScreen.javaFont(obj));
	} else obj = null;

	if (obj == null || obj.isFont() == false)
	    obj = YoixMake.yoixFont(YoixMakeScreen.javaFont((YoixObject)null));
	currentfont = (YoixBodyFont)obj.body();
    }


    private synchronized void
    setForeground(YoixObject obj) {

	YoixBodyMatrix  matrix;
	BufferedImage   texture;
	YoixObject      arg[];
	YoixObject      element;
	double          delta[];
	double          pt0[];
	double          pt1[];
	Point           offset;
	int             xoffset;
	int             yoffset;
	int             width;
	int             height;
	int             count;
	int             n;

	if (obj.notNull()) {
	    if (obj.isArray()) {
		arg = new YoixObject[obj.sizeof()];
		for (n = obj.offset(), count = 0; n < obj.length(); n++) {
		    if ((element = obj.getObject(n)) != null)
			arg[count++] = element;
		}
	    } else {
		arg = new YoixObject[] {obj};
		count = 1;
	    }
	    if (count > 0) {
		if (arg[0].isColor()) {
		    if (count == 1)
			currentpaint = YoixMake.javaColor(obj);
		    else if (count == 6 || count == 7) {
			if (arg[3].isColor()) {
			    if (arg[1].isNumber() && arg[2].isNumber()) {
				if (arg[4].isNumber() && arg[5].isNumber()) {
				    if (count == 6 || arg[6].isNumber()) {
					pt0 = currentmatrix.transform(
					    arg[1].doubleValue(),
					    arg[2].doubleValue()
					);
					pt1 = currentmatrix.transform(
					    arg[4].doubleValue(),
					    arg[5].doubleValue()
					);
					currentpaint = new GradientPaint(
					    (float)pt0[0],
					    (float)pt0[1],
					    YoixMake.javaColor(arg[0]),
					    (float)pt1[0],
					    (float)pt1[1],
					    YoixMake.javaColor(arg[3]),
					    arg.length == 7 && arg[6].booleanValue()
					);
				    } else VM.abort(TYPECHECK, N_FOREGROUND);
				} else VM.abort(TYPECHECK, N_FOREGROUND);
			    } else VM.abort(TYPECHECK, N_FOREGROUND);
			} else VM.abort(TYPECHECK, N_FOREGROUND);
		    } else VM.abort(TYPECHECK, N_FOREGROUND);
		} else if (arg[0].isImage()) {
		    if (count == 1 || count == 3) {
			if (count == 1 || arg[1].isNumber()) {
			    if (count == 1 || arg[2].isNumber()) {
				if ((texture = (BufferedImage)arg[0].getManagedDrawable(this)) != null) {
				    width = texture.getWidth();
				    height = texture.getHeight();
				    xoffset = 0;
				    yoffset = 0;
				    if (count == 3) {
					matrix = (YoixBodyMatrix)VM.getDefaultMatrix().body();
					delta = matrix.dtransform(
					    arg[1].doubleValue(),
					    arg[2].doubleValue()
					);
					xoffset = (int)delta[0];
					yoffset = (int)delta[1];
				    } else if (currentdrawable != null) {
					offset = YoixMiscJFC.getWindowOffset(currentdrawable.getManagedObject());
					xoffset = offset.x;
					yoffset = offset.y;
				    }
				    currentpaint = new YoixAWTTexturePaint(
					texture,
					new Rectangle2D.Double(xoffset, yoffset, width, height)
				    );
				}
			    } else VM.abort(TYPECHECK, N_FOREGROUND);
			} else VM.abort(TYPECHECK, N_FOREGROUND);
		    } else VM.abort(TYPECHECK, N_FOREGROUND);
		} else VM.abort(TYPECHECK, N_FOREGROUND);
	    } else VM.abort(TYPECHECK, N_FOREGROUND);
	} else currentpaint = null;
    }


    private synchronized void
    setFractionalMetrics(YoixObject obj) {

	Object  value;

	switch (obj.intValue()) {
	    case 0:
		value = RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
		break;

	    case 1:
		value = RenderingHints.VALUE_FRACTIONALMETRICS_ON;
		break;

	    default:
		value = RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT;
		break;
	}

	currenthints.put(RenderingHints.KEY_FRACTIONALMETRICS, value);
    }


    private synchronized void
    setLineCap(YoixObject obj) {

	switch (obj.intValue()) {
	    case YOIX_CAP_BUTT:
		linecap = YOIX_CAP_BUTT;
		break;

	    case YOIX_CAP_ROUND:
		linecap = YOIX_CAP_ROUND;
		break;

	    case YOIX_CAP_SQUARE:
		linecap = YOIX_CAP_SQUARE;
		break;

	    default:
		linecap = YOIX_CAP_SQUARE;
		break;
	}
	currentstroke = null;
    }


    private synchronized void
    setLineJoin(YoixObject obj) {

	switch (obj.intValue()) {
	    case YOIX_JOIN_BEVEL:
		linejoin = YOIX_JOIN_BEVEL;
		break;

	    case YOIX_JOIN_ROUND:
		linejoin = YOIX_JOIN_ROUND;
		break;

	    case YOIX_JOIN_MITER:
		linejoin = YOIX_JOIN_MITER;
		break;

	    default:
		linejoin = YOIX_JOIN_MITER;
		break;
	}
	currentstroke = null;
    }


    private synchronized void
    setLineWidth(YoixObject obj) {

	linewidth = (float)Math.max(0.0, obj.doubleValue());
	currentstroke = null;
    }


    private synchronized void
    setMiterLimit(YoixObject obj) {

	miterlimit = (float)Math.max(1.0, obj.doubleValue());
	currentstroke = null;
    }


    private synchronized void
    setPath(YoixObject obj) {

	if (obj.notNull())
	    currentpath.pathSetShape(((YoixBodyPath)obj.body()).copyGeneralPath());
	else currentpath.pathReset();
    }


    private synchronized void
    setRendering(YoixObject obj) {

	Object  value;

	switch (obj.intValue()) {
	    case 0:
		value = RenderingHints.VALUE_RENDER_SPEED;
		break;

	    case 1:
		value = RenderingHints.VALUE_RENDER_QUALITY;
		break;

	    default:
		value = RenderingHints.VALUE_RENDER_DEFAULT;
		break;
	}

	currenthints.put(RenderingHints.KEY_RENDERING, value);
    }


    private synchronized void
    setTextAntiAliasing(YoixObject obj) {

	Object  value;

	switch (obj.intValue()) {
	    case 0:
		value = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
		break;

	    case 1:
		value = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
		break;

	    default:
		value = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
		break;
	}

	currenthints.put(RenderingHints.KEY_TEXT_ANTIALIASING, value);
    }


    private synchronized void
    setXORMode(YoixObject obj) {

	xormode = obj.booleanValue();
    }
}

