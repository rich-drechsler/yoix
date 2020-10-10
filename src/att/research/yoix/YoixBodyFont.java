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
import java.util.*;

final
class YoixBodyFont extends YoixPointerActive

{

    //
    // Much more general that we currently need, but there's a small
    // chance we'll support custom fonts in the distant future.
    //

    private YoixInterfaceFont  currentfont = null;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	N_NAME,             $LR__,       $LR__,
	N_TYPE,             $LR__,       null,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(10);

    static {
	activefields.put(N_MATRIX, new Integer(V_MATRIX));
	activefields.put(N_ROTATEFONT, new Integer(V_ROTATEFONT));
	activefields.put(N_SCALEFONT, new Integer(V_SCALEFONT));
	activefields.put(N_SHEARFONT, new Integer(V_SHEARFONT));
	activefields.put(N_STRINGBOUNDS, new Integer(V_STRINGBOUNDS));
	activefields.put(N_STRINGFIT, new Integer(V_STRINGFIT));
	activefields.put(N_STRINGSIZE, new Integer(V_STRINGSIZE));
	activefields.put(N_STRINGWIDTH, new Integer(V_STRINGWIDTH));
	activefields.put(N_TRANSFORMFONT, new Integer(V_TRANSFORMFONT));
	activefields.put(N_TRANSLATEFONT, new Integer(V_TRANSLATEFONT));
	activefields.put(N_TYPE, new Integer(V_TYPE));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyFont(YoixObject data) {

	super(data);
	buildFont();
	setFixedSize();
	setPermissions(permissions);
    }


    YoixBodyFont(YoixObject data, YoixInterfaceFont currentfont) {

	super(data);
	this.currentfont = currentfont;
	buildFont();
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

	return(FONT);
    }

    ///////////////////////////////////
    //
    // YoixBodyFont Methods
    //
    ///////////////////////////////////

    final void
    drawString(String str, double x, double y, double alpha, YoixObject graphics) {

	YoixInterfaceFont  font;
	Object             body;

	if (str != null && graphics != null) {
	    if ((font = currentfont) != null) {
		body = graphics.body();
		if (body instanceof YoixBodyGraphics)
		    font.fontDrawString(str, x, y, alpha, (YoixBodyGraphics)body);
	    }
	}
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_ROTATEFONT:
		obj = builtinRotateFont(name, argv);
		break;

	    case V_SCALEFONT:
		obj = builtinScaleFont(name, argv);
		break;

	    case V_SHEARFONT:
		obj = builtinShearFont(name, argv);
		break;

	    case V_STRINGBOUNDS:
		obj = builtinStringBounds(name, argv);
		break;

	    case V_STRINGFIT:
		obj = builtinStringFit(name, argv);
		break;

	    case V_STRINGSIZE:
		obj = builtinStringSize(name, argv);
		break;

	    case V_STRINGWIDTH:
		obj = builtinStringWidth(name, argv);
		break;

	    case V_TRANSFORMFONT:
		obj = builtinTransformFont(name, argv);
		break;

	    case V_TRANSLATEFONT:
		obj = builtinTranslateFont(name, argv);
		break;

	    default:
		obj = null;
		break;
	}

	return(obj);
    }


    protected final void
    finalize() {

	currentfont = null;
	super.finalize();
    }


    final YoixInterfaceFont
    getCurrentFont() {

	return(currentfont);
    }


    final Font
    getCurrentJavaFont() {

	YoixInterfaceFont  font;

	return((font = currentfont) != null ? font.fontGetJavaFont() : null);
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	YoixInterfaceFont  font = currentfont;

	switch (activeField(name, activefields)) {
	    case V_MATRIX:
		obj = (font != null) ? font.fontGetFontMatrix() : YoixObject.newMatrix();
		break;

	}
	return(obj);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_TYPE:
		    setType(obj);
		    break;
	    }
	}
	return(obj);
    }


    final Rectangle2D
    stringBounds(String str, boolean tight, YoixObject dest) {

	YoixInterfaceFont  font;
	Rectangle2D        rect = null;

	if (str != null) {
	    if ((font = currentfont) != null)
		rect = font.fontStringBounds(str, tight, dest);
	}

	return(rect);
    }


    final double
    stringWidth(String str, YoixObject dest) {

	YoixInterfaceFont  font;
	double             width = 0;

	if (str != null) {
	    if ((font = currentfont) != null)
		width = font.fontStringWidth(str, dest);
	}

	return(width);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildFont() {

	setField(N_TYPE);		// this must be first
    }


    private synchronized YoixObject
    builtinRotateFont(String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		if (currentfont != null)
		    obj = currentfont.fontRotateFont(arg[0].doubleValue());
	    } else VM.badArgument(name, 0);
	}

	return(obj != null ? obj : YoixObject.newFont());
    }


    private synchronized YoixObject
    builtinScaleFont(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	double      sx;
	double      sy;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isNumber()) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    if (currentfont != null) {
			sx = arg[0].doubleValue();
			sy = (arg.length == 2) ? arg[1].doubleValue() : sx;
			obj = currentfont.fontScaleFont(sx, sy);
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	}

	return(obj != null ? obj : YoixObject.newFont());
    }


    private synchronized YoixObject
    builtinShearFont(String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg.length == 2) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (currentfont != null) {
			obj = currentfont.fontShearFont(
			    arg[0].doubleValue(),
			    arg[1].doubleValue()
			);
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	}

	return(obj != null ? obj : YoixObject.newFont());
    }


    private synchronized YoixObject
    builtinStringBounds(String name, YoixObject arg[]) {

	Rectangle2D  rect;
	YoixObject   bounds = null;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    if (currentfont != null) {
			rect = currentfont.fontStringBounds(
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
    builtinStringFit(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	Object      target = null;
	boolean     fractionalmetrics = true;
	boolean     antialiasing = false;
	String      str;
	String      source;
	double      size[];

	//
	// We'll eventually will consider fitting text to a rectangle (or
	// Dimension), but for now this will be sufficient.
	//
	// NOTE - arguments 5 and 6 were a late addition and might not be
	// documented. If that's the case you shouldn't use them.
	// 

	if (arg.length >= 2 && arg.length <= 6) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if (arg[1].isNumber()) {
		    if (arg.length <= 2 || arg[2].isString() || arg[2].isNull()) {
			if (arg.length <= 3 || arg[3].isString() || arg[3].isNull()) {
			    if (arg.length == 5) {
				if (arg[4].isNumber() || arg[4].isDrawable() || arg[4].isGraphics() || arg[4].isNull()) {
				    if (arg[4].notNumber())
					target = arg[4].notNull() ? arg[4] : null;
				    else fractionalmetrics = arg[4].booleanValue();
				} else VM.badArgument(name, 4);
			    } else if (arg.length == 6) {
				if (arg[4].isNumber()) {
				    if (arg[5].isNumber()) {
					fractionalmetrics = arg[4].booleanValue();
					antialiasing = arg[5].booleanValue();
				    } else VM.badArgument(name, 5);
				} else VM.badArgument(name, 4);
			    }
			    if (currentfont != null) {
				//
				// The only reason we transform the target
				// width on our own instead of doing it with
				// YoixMakeScreen.javaDistance() is so we end
				// up with a double rather than a rounded int.
				// Undoubtedly not a big deal!!
				//
				source = arg[0].stringValue();
				size = VM.getDefaultMatrix().dtransform(arg[1].doubleValue(), 0);
				str = currentfont.fontStringFit(
				    source,
				    Math.sqrt(size[0]*size[0] + size[1]*size[1]),
				    arg.length > 2 ? arg[2].stringValue() : null,
				    arg.length > 3 ? arg[3].stringValue() : null,
				    fractionalmetrics,
				    antialiasing,
				    target
				);
				obj = (str != source) ? YoixObject.newString(str) : arg[0];
			    }
			} else VM.badArgument(name, 3);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(obj != null ? obj : YoixObject.newString());
    }


    private synchronized YoixObject
    builtinStringSize(String name, YoixObject arg[]) {

	Rectangle2D rect;
	YoixObject  size = null;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    if (currentfont != null) {
			rect = currentfont.fontStringBounds(
			    arg[0].stringValue(),
			    arg.length == 2 ? arg[1].booleanValue() : false,
			    this
			);
			size = YoixMakeScreen.yoixDimension(rect.getWidth(), rect.getHeight());
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(size != null ? size : YoixMake.yoixType(T_DIMENSION));
    }


    private synchronized YoixObject
    builtinStringWidth(String name, YoixObject arg[]) {

	double  width = 0;

	if (arg.length == 1) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if (currentfont != null) {
		    width = currentfont.fontStringWidth(arg[0].stringValue(), this);
		    width = YoixMakeScreen.yoixDistance(width);
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newDouble(width));
    }


    private synchronized YoixObject
    builtinTransformFont(String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg.length == 1) {
	    if (arg[0].isMatrix()) {
		if (currentfont != null)
		    obj = currentfont.fontTransformFont((YoixBodyMatrix)arg[0].body());
	    } else VM.badArgument(name, 0);
	}

	return(obj != null ? obj : YoixObject.newFont());
    }


    private synchronized YoixObject
    builtinTranslateFont(String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg.length == 2) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (currentfont != null) {
			obj = currentfont.fontTranslateFont(
			    arg[0].doubleValue(),
			    arg[1].doubleValue()
			);
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	}

	return(obj != null ? obj : YoixObject.newFont());
    }


    private void
    setType(YoixObject obj) {

	int  type;

	//
	// Only allowed during initialization because the type field
	// is protected after that, so there's currently no need to
	// synchronize, and we can't think of a good reason for any
	// other approach. Careful if you make changes - some subtle
	// synchronization may be required.
	//

	if (currentfont == null) {
	    VM.pushAccess(LRW_);
	    switch (type = obj.intValue()) {
		case 0:
		    currentfont = new YoixFontType0(data);
		    break;

		default:
		    //
		    // We eventually could support more types, which
		    // will follow PostScript numbering when possible.
		    // For example, type 3 PostScript fonts are user
		    // defined fonts and types 1 and 2 are also well
		    // defined in PostScript.
		    //
		    VM.abort(UNIMPLEMENTED, N_TYPE, new String[] {OFFENDINGVALUE, type + ""});
		    break;
	    }
	    VM.popAccess();
	}
    }
}

