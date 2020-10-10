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
import java.awt.image.*;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.*;

public abstract
class YoixMakeScreen

    implements YoixAPI,
	       YoixConstants,
	       YoixConstantsSwing

{

    //
    // Object builders that need information about your display, which
    // means it's safe to trigger the YoixVM.java screen building code.
    //

    private static HashMap  typetemplates = new HashMap();

    //
    // Saving a copy of defaultmatrix triggers the screen building code
    // in YoixVM.java, if it hasn't run already. Shouldn't be a problem
    // because methods that don't need access to information about your
    // display don't belong here!!!
    //

    private static YoixBodyMatrix  defaultmatrix = null;

    static {
	defaultmatrix = (YoixBodyMatrix)VM.getDefaultMatrix().body();
    }

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public static Color
    javaBackground(YoixObject obj) {

	Color  color;

	if ((color = YoixMake.javaColor(obj)) == null) {
	    if ((color = YoixMake.javaColor(VM.getObject(N_BACKGROUND))) == null)
		color = DEFAULT_BACKGROUND;
	}
	return(color);
    }


    public static Border
    javaBorder(YoixObject obj) {

	return(javaBorder(obj, null, null));
    }


    public static Border
    javaBorder(YoixObject obj, Border border, JComponent comp) {

	YoixObject  entry;
	boolean     lowered;
	boolean     rounded;
	String      title;
	Insets      insets;
	double      width;
	Border      child;
	Color       background;
	Color       highlight;
	Color       shadow;
	Icon        icon;
	int         thickness;
	int         type;

	//
	// NOTE - N_WIDTH probably is an undocumented field and its use
	// here is primarily for backward compatibility. Seems like the
	// same thing could be accomplished, perhaps more elegantly, if
	// javaInsets() would build an Insets from a Yoix number. Turns
	// out javaInsets() works that way now, but we still have that
	// one very important application that has to be checked and/or
	// modified before we can make permanent changes here.
	//

	if (obj.notNull()) {
	    if (obj.notString() && obj.notNumber() && obj.notInsets()) {
		type = obj.getInt(N_TYPE, 0);
		background = obj.getColor(N_BACKGROUND, comp == null ? null : comp.getBackground());
		insets = javaInsets(obj.getObject(N_INSETS));
		width = obj.getDouble(N_THICKNESS, 0);

		//
		// This is only here to support an old version of the Border
		// that defined an entry named width - probably still used by
		// one important application, so don't remove without testing.
		// 
		if ((entry = obj.getObject(N_WIDTH)) != null && entry.isNumber()) {
		    width = entry.doubleValue();
		    if ((entry = obj.getObject(N_INSETS)) == null || entry.isNull())
			insets = javaInsets(width);
		}
		if ((highlight = obj.getColor(N_HIGHLIGHT)) == null && background != null)
		    highlight = background.brighter();
		if ((shadow = obj.getColor(N_SHADOW)) == null && background != null)
		    shadow = background.darker();
		lowered = (type&BORDER_BEVEL_MASK) == YOIX_LOWERED;
		if ((entry = obj.getObject(N_RAISED)) != null && entry.isNumber())
		    lowered = !entry.booleanValue();
		rounded = (type&YOIX_ROUNDED) == YOIX_ROUNDED;
		if ((entry = obj.getObject(N_ROUNDED)) != null && entry.isNumber())
		    rounded = entry.booleanValue();
		child = javaBorder(obj.getObject(N_CHILD));
		switch (type&BORDER_TYPE_MASK) {
		    case YOIX_BEVELED:
			border = new BevelBorder(
			    lowered ? BevelBorder.LOWERED : BevelBorder.RAISED,
			    highlight != null ? highlight.brighter() : null,
			    highlight,
			    shadow != null ? shadow.darker() : null,
			    shadow
			);
			break;

		    case YOIX_EMPTY:
			border = new EmptyBorder(insets);
			break;

		    case YOIX_ETCHED:
			border = new EtchedBorder(
			    lowered ? EtchedBorder.LOWERED : EtchedBorder.RAISED,
			    highlight,
			    shadow
			);
			break;

		    case YOIX_LINED:
			if ((thickness = javaDistance(width)) <= 0)
			    thickness = 1;
			if (background != null)
			    border = new LineBorder(background, thickness, rounded);
			else border = new EmptyBorder(thickness, thickness, thickness, thickness);
			break;

		    case YOIX_MATTE:
			if ((icon = YoixMake.javaIcon(obj.getObject(N_ICON))) == null) {
			    if (background != null) {
				border = new MatteBorder(
				    insets.top,
				    insets.left,
				    insets.bottom,
				    insets.right,
				    background
				);
			    } else border = new EmptyBorder(insets);
			} else {
			    if (obj.getObject(N_INSETS).isNull()) {
				border = new MatteBorder(
				    -1,
				    -1,
				    -1,
				    -1,
				    icon
				);
			    } else {
				border = new MatteBorder(
				    insets.top,
				    insets.left,
				    insets.bottom,
				    insets.right,
				    icon
				);
			    }
			}
			break;

		    case YOIX_SOFTBEVELED:
			border = new SoftBevelBorder(
			    lowered ? BevelBorder.LOWERED : BevelBorder.RAISED,
			    highlight != null ? highlight.brighter() : null,
			    highlight,
			    shadow != null ? shadow.darker() : null,
			    shadow
			);
			break;
		}

		if ((title = obj.getString(N_TITLE)) != null) {
		    if (title.length() > 0) {
			//
			// Changed the font and color code to make sure
			// the look-and-feel wins when the values aren't
			// explicitly set in obj (9/4/07).
			//
			border = new TitledBorder(
			    border,
			    title,
			    YoixBodyComponentSwing.jfcInt("BorderAlignment", obj, N_ALIGNMENT),
			    YoixBodyComponentSwing.jfcInt("BorderPosition", obj, N_POSITION),
			    javaFont(obj.getObject(N_FONT), null),
			    YoixMake.javaColor(obj.getObject(N_FOREGROUND), null)
			);
		    }
		}

		if (border != null) {
		    if (child != null)
			border = new CompoundBorder(border, child);
		} else border = child;
	    } else if (obj.isString()) {
		border = new TitledBorder(obj.stringValue());
		//
		// Older version included the lines,
		//
		//    ((TitledBorder)border).setTitleFont(comp == null ? javaFont(VM.getObject(N_FONT)) : comp.getFont());
		//    ((TitledBorder)border).setTitleColor(comp == null ? javaForeground(null) : comp.getForeground());
		//
		// but we now (9/4/07) always defer to the look-and-feel.
		// There's probably more chance of interference between
		// comp's background and the title's color, but user's
		// can always take control with a Yoix Border object or
		// by setting the TitledBorder.titleColor property.
		// 
	    } else if (obj.isNumber() || obj.isInsets())
		border = new EmptyBorder(javaInsets(obj));
	}
	return(border);
    }


    public static Cursor
    javaCursor(YoixObject obj) {

	return(javaCursor(obj, null, null));
    }


    public static Cursor
    javaCursor(YoixObject obj, Component comp) {

	return(javaCursor(obj, comp, null));
    }


    public static Cursor
    javaCursor(YoixObject obj, String name) {

	return(javaCursor(obj, null, name));
    }


    public static Cursor
    javaCursor(YoixObject obj, Component comp, String name) {

	Cursor  cursor = null;

	if (obj != null) {
	    if (obj.isString() && obj.notNull()) {
		if (YoixRegistryCursor.isRegisteredCursor(obj) == false) {
		    obj = YoixObject.newImage(obj.stringValue());
		    cursor = newCursor(obj, name, YoixRegistryCursor.getStandardCursor(comp));
		} else cursor = YoixRegistryCursor.getCursor(obj, comp);
	    } else if (obj.isImage() && obj.notNull())
		cursor = newCursor(obj, name, YoixRegistryCursor.getStandardCursor(comp));
	    else if (obj.isInteger() || obj.isNull())
		cursor = YoixRegistryCursor.getCursor(obj, comp);
	}
	return(cursor);
    }


    public static Dimension
    javaDimension(YoixObject obj) {

	return(obj != null
	    ? javaDimension(obj.getObject(N_WIDTH), obj.getObject(N_HEIGHT))
	    : new Dimension(0, 0)
	);
    }


    public static Dimension
    javaDimension(YoixObject obj, Dimension defaultsize) {

	Dimension  size;

	if (obj != null) {
	    size = javaDimension(obj.getObject(N_WIDTH), obj.getObject(N_HEIGHT));
	    if (size.width <= 0)
		size.width = (defaultsize != null) ? defaultsize.width : 0;
	    if (size.height <= 0)
		size.height = (defaultsize != null) ? defaultsize.height : 0;
	} else if (defaultsize != null)
	    size = new Dimension(defaultsize);
	else size = new Dimension(0, 0);

	return(size);
    }


    public static Dimension
    javaDimension(YoixObject width, YoixObject height) {

	double  size[];

	if (width != null && height != null)
	    size = defaultmatrix.dtransform(width.doubleValue(), height.doubleValue());
	else size = new double[] {0, 0};

	return(new Dimension((int)Math.round(size[0]), (int)Math.round(size[1])));
    }


    public static Dimension
    javaDimension(double width, double height) {

	double  size[] = defaultmatrix.dtransform(width, height);

	return(new Dimension((int)Math.round(size[0]), (int)Math.round(size[1])));
    }


    public static int
    javaDistance(double width) {

	double  size[];
	int     sign;

	sign = (width > 0) ? 1 : -1;
	size = defaultmatrix.dtransform(width, 0);
	width = (sign)*(Math.sqrt(size[0]*size[0] + size[1]*size[1]));

	return((int)Math.round(width));
    }


    public static int
    javaDistance(YoixObject obj) {

	double  size[];
	double  width;
	int     sign;

	if (obj != null) {
	    width = obj.doubleValue();
	    sign = (width > 0) ? 1 : -1;
	    size = defaultmatrix.dtransform(width, 0);
	    width = (sign)*(Math.sqrt(size[0]*size[0] + size[1]*size[1]));
	} else width = 0;

	return((int)Math.round(width));
    }


    public static Font
    javaFont(YoixObject obj) {

	String  name;
	double  scale;
	Font    font = null;
	int     size;

	if (obj == null || obj.isNull())
	    obj = VM.getObject(N_FONT);

	if (obj.isString() || obj.isNull())
	    font = javaFont((String)(obj.isString() ? obj.stringValue() : null));
	else if (obj.isFont())
	    font = ((YoixBodyFont)obj.body()).getCurrentJavaFont();
	else VM.abort(TYPECHECK);

	return(font);
    }


    public static Font
    javaFont(YoixObject obj, Font font) {

	return(obj != null && obj.notNull() ? javaFont(obj) : font);
    }


    public static Color
    javaForeground(YoixObject obj) {

	Color  color;

	if ((color = YoixMake.javaColor(obj)) == null) {
	    if ((color = YoixMake.javaColor(VM.getObject(N_FOREGROUND))) == null)
		color = DEFAULT_FOREGROUND;
	}
	return(color);
    }


    public static Insets
    javaInsets(double width) {

	int  distance = javaDistance(width);

	return(new Insets(distance, distance, distance, distance));
    }


    public static Insets
    javaInsets(YoixObject obj) {

	Insets  insets;
	double  ul[];
	double  lr[];

	if (obj != null && obj.notNull()) {
	    if (obj.notNumber()) {
		ul = defaultmatrix.dtransform(obj.getDouble(N_LEFT, 0), obj.getDouble(N_TOP, 0));
		lr = defaultmatrix.dtransform(obj.getDouble(N_RIGHT, 0), obj.getDouble(N_BOTTOM, 0));
		insets = new Insets(
		    (int)Math.round(ul[1]),
		    (int)Math.round(ul[0]),
		    (int)Math.round(lr[1]),
		    (int)Math.round(lr[0])
		);
	    } else insets = javaInsets(obj.doubleValue());
	} else insets = (Insets)ZEROINSETS.clone();

	return(insets);
    }


    public static Point
    javaPoint(YoixObject obj) {

	double  loc[];

	if (obj != null)
	    loc = defaultmatrix.transform(obj.getDouble(N_X, 0), obj.getDouble(N_Y, 0));
	else loc = new double[] {0, 0};

	return(new Point((int)Math.floor(loc[0]), (int)Math.floor(loc[1])));
    }


    public static Point
    javaPoint(double x, double y) {

	double  loc[] = defaultmatrix.transform(x, y);

	return(new Point((int)Math.floor(loc[0]), (int)Math.floor(loc[1])));
    }


    public static Point2D
    javaPoint2D(YoixObject obj) {

	double  loc[];

	if (obj != null)
	    loc = defaultmatrix.transform(obj.getDouble(N_X, 0), obj.getDouble(N_Y, 0));
	else loc = new double[] {0, 0};

	return(new Point2D.Double(loc[0], loc[1]));
    }


    public static Point2D
    javaPoint2D(double x, double y) {

	double  loc[] = defaultmatrix.transform(x, y);

	return(new Point2D.Double(loc[0], loc[1]));
    }


    public static Rectangle
    javaRectangle(YoixObject obj) {

	Rectangle  rect;
	double     loc[];
	double     size[];

	if (obj != null) {
	    loc = defaultmatrix.transform(obj.getDouble(N_X, 0), obj.getDouble(N_Y, 0));
	    size = defaultmatrix.dtransform(obj.getDouble(N_WIDTH, 0), obj.getDouble(N_HEIGHT, 0));
	    rect = new Rectangle(
		(int)Math.floor(loc[0]),
		(int)Math.floor(loc[1]),
		(int)Math.round(size[0]),
		(int)Math.round(size[1])
	    );
	} else rect = new Rectangle(0, 0, 0, 0);

	return(rect);
    }


    public static Rectangle
    javaRectangle(double x, double y, double width, double height) {

	double  loc[] = defaultmatrix.transform(x, y);
	double  size[] = defaultmatrix.dtransform(width, height);

	return(
	    new Rectangle(
		(int)Math.floor(loc[0]),
		(int)Math.floor(loc[1]),
		(int)Math.round(size[0]),
		(int)Math.round(size[1])
	    )
	);
    }


    public static YoixObject
    yoixBorder(Border border) {

	YoixObject  obj = null;
	YoixObject  child;
	Object      value;

	//
	// Written quickly, not well tested, and not used much. Currently
	// returns null when border isn't a standard Swing Border, which
	// is typically the case for default UIManager borders.
	//

	if (border != null) {
	    if ((obj = VM.getTypeTemplate(T_BORDER)) != null) {
		if (border instanceof CompoundBorder) {
		    if ((child = yoixBorder(((CompoundBorder)border).getInsideBorder())) != null) {
			obj.put(N_CHILD, child, false);
			border = ((CompoundBorder)border).getOutsideBorder();
		    } else border = null;
		}
		if (border instanceof TitledBorder) {
		    if ((value = ((TitledBorder)border).getTitle()) != null)
			obj.putString(N_TITLE, (String)value);
		    if ((value = ((TitledBorder)border).getTitleColor()) != null)
			obj.put(N_FOREGROUND, YoixMake.yoixColor((Color)value), false);
		    if ((value = ((TitledBorder)border).getTitleFont()) != null)
			obj.put(N_FONT, YoixMake.yoixFont((Font)value), false);
		    switch (((TitledBorder)border).getTitlePosition()) {
			case TitledBorder.ABOVE_BOTTOM:
			    obj.putInt(N_POSITION, YOIX_ABOVE_BOTTOM);
			    break;

			case TitledBorder.ABOVE_TOP:
			    obj.putInt(N_POSITION, YOIX_ABOVE_TOP);
			    break;

			case TitledBorder.BELOW_BOTTOM:
			    obj.putInt(N_POSITION, YOIX_BELOW_BOTTOM);
			    break;

			case TitledBorder.BELOW_TOP:
			    obj.putInt(N_POSITION, YOIX_BELOW_TOP);
			    break;

			case TitledBorder.BOTTOM:
			    obj.putInt(N_POSITION, YOIX_BOTTOM);
			    break;

			case TitledBorder.TOP:
			case TitledBorder.DEFAULT_POSITION:
			    obj.putInt(N_POSITION, YOIX_TOP);
			    break;
		    }
		    switch (((TitledBorder)border).getTitleJustification()) {
			case TitledBorder.CENTER:
			    obj.putInt(N_ALIGNMENT, YOIX_CENTER);
			    break;

			case TitledBorder.LEADING:
			    obj.putInt(N_ALIGNMENT, YOIX_LEADING);
			    break;

			case TitledBorder.LEFT:
			    obj.putInt(N_ALIGNMENT, YOIX_LEFT);
			    break;

			case TitledBorder.RIGHT:
			    obj.putInt(N_ALIGNMENT, YOIX_RIGHT);
			    break;

			case TitledBorder.TRAILING:
			    obj.putInt(N_ALIGNMENT, YOIX_TRAILING);
			    break;

			default:
			    obj.putInt(N_ALIGNMENT, YOIX_LEFT);
			    break;
		    }
		    border = ((TitledBorder)border).getBorder();
		}
		if (border instanceof BevelBorder) {
		    if (border instanceof SoftBevelBorder)
			obj.putInt(N_TYPE, YOIX_SOFTBEVELED);
		    else obj.putInt(N_TYPE, YOIX_BEVELED);
		    if (((BevelBorder)border).getBevelType() == BevelBorder.RAISED)
			obj.putInt(N_RAISED, true);
		    else obj.putInt(N_RAISED, false);
		    if ((value = ((BevelBorder)border).getHighlightInnerColor()) != null)
			obj.put(N_HIGHLIGHT, YoixMake.yoixColor((Color)value), false);
		    if ((value = ((BevelBorder)border).getShadowInnerColor()) != null)
			obj.put(N_SHADOW, YoixMake.yoixColor((Color)value), false);
		} else if (border instanceof EmptyBorder) {
		    if (border instanceof MatteBorder) {
			obj.putInt(N_TYPE, YOIX_MATTE);
			if ((value = ((MatteBorder)border).getMatteColor()) != null)
			    obj.put(N_BACKGROUND, YoixMake.yoixColor((Color)value), false);
			if ((value = ((MatteBorder)border).getTileIcon()) != null)
			    obj.put(N_ICON, YoixMake.yoixIcon((Icon)value), false);
		    } else obj.putInt(N_TYPE, YOIX_EMPTY);
		    if ((value = ((EmptyBorder)border).getBorderInsets()) != null)
			obj.put(N_INSETS, yoixInsets((Insets)value), false);
		} else if (border instanceof EtchedBorder) {
		    obj.putInt(N_TYPE, YOIX_ETCHED);
		    if (((EtchedBorder)border).getEtchType() == EtchedBorder.RAISED)
			obj.putInt(N_RAISED, true);
		    else obj.putInt(N_RAISED, false);
		    if ((value = ((EtchedBorder)border).getHighlightColor()) != null)
			obj.put(N_HIGHLIGHT, YoixMake.yoixColor((Color)value), false);
		    if ((value = ((EtchedBorder)border).getShadowColor()) != null)
			obj.put(N_SHADOW, YoixMake.yoixColor((Color)value), false);
		} else if (border instanceof LineBorder) {
		    obj.putInt(N_TYPE, YOIX_LINED);
		    obj.putInt(N_ROUNDED, ((LineBorder)border).getRoundedCorners());
		    if ((value = ((LineBorder)border).getLineColor()) != null)
			obj.put(N_BACKGROUND, YoixMake.yoixColor((Color)value), false);
		    obj.putDouble(N_THICKNESS, yoixDistance(((LineBorder)border).getThickness()));
		} else obj = null;
	    }
 	}
	return(obj);
    }


    public static double
    yoixDistance(double width) {

	double  size[];

	if ((size = defaultmatrix.idtransform(Math.round(width), 0, null)) != null)
	    width = Math.sqrt(size[0]*size[0] + size[1]*size[1]);
	else width = 0;

	return(width);
    }


    public static YoixObject
    yoixDimension(Dimension dimen) {

	YoixObject  dest;
	double      size[];

	//
	// Not sure about error return - perhaps NaN for both fields
	// would be better?? Suspect caller of this method should be
	// in control.
	//

	dest = newType(T_DIMENSION);
	if ((size = defaultmatrix.idtransform(dimen.width, dimen.height, null)) != null) {
	    dest.put(N_WIDTH, YoixObject.newDouble(size[0]), false);
	    dest.put(N_HEIGHT, YoixObject.newDouble(size[1]), false);
	}
	return(dest);
    }


    public static YoixObject
    yoixDimension(double width, double height) {

	YoixObject  dest;
	double      size[];

	dest = newType(T_DIMENSION);
	if ((size = defaultmatrix.idtransform(width, height, null)) != null) {
	    dest.put(N_WIDTH, YoixObject.newDouble(size[0]), false);
	    dest.put(N_HEIGHT, YoixObject.newDouble(size[1]), false);
	}
	return(dest);
    }


    public static YoixObject
    yoixDimension(int width, int height) {

	return(yoixDimension(new Dimension(width, height)));
    }


    public static YoixObject
    yoixInsets(Insets insets) {

	YoixObject  dest;
	double      ul[];
	double      lr[];

	//
	// Not sure about error return - perhaps NaN for both fields
	// would be better?? Suspect caller of this method should be
	// in control.
	//

	dest = newType(T_INSETS);
	if ((ul = defaultmatrix.idtransform(insets.left, insets.top, null)) != null) {
	    if ((lr = defaultmatrix.idtransform(insets.right, insets.bottom, null)) != null) {
		dest.put(N_LEFT, YoixObject.newDouble(ul[0]), false);
		dest.put(N_TOP, YoixObject.newDouble(ul[1]), false);
		dest.put(N_RIGHT, YoixObject.newDouble(lr[0]), false);
		dest.put(N_BOTTOM, YoixObject.newDouble(lr[1]), false);
	    }
	}
	return(dest);
    }


    public static YoixObject
    yoixPoint(Point loc) {

	return(yoixPoint(loc, newType(T_POINT)));
    }


    public static YoixObject
    yoixPoint(Point2D point) {

	YoixObject dest;
	double     loc[];

	//
	// Not sure about error return - perhaps NaN for both fields
	// would be better?? Suspect caller of this method should be
	// in control.
	//

	dest = newType(T_POINT);
	if ((loc = defaultmatrix.itransform(point.getX(), point.getY(), null)) != null) {
	    dest.put(N_X, YoixObject.newDouble(loc[0]), false);
	    dest.put(N_Y, YoixObject.newDouble(loc[1]), false);
	}
	return(dest);
    }


    public static YoixObject
    yoixPoint(Point point, YoixObject dest) {

	double  loc[];

	//
	// Not sure about error return - perhaps NaN for both fields
	// would be better?? Suspect caller of this method should be
	// in control.
	//

	if (dest == null || dest.isNull())
	    dest = newType(T_POINT);
	if ((loc = defaultmatrix.itransform(point.x, point.y, null)) != null) {
	    dest.put(N_X, YoixObject.newDouble(loc[0]), false);
	    dest.put(N_Y, YoixObject.newDouble(loc[1]), false);
	}
	return(dest);
    }


    public static YoixObject
    yoixRectangle(Rectangle2D rect) {

	YoixObject  dest;
	double      loc[];
	double      size[];

	//
	// Not sure about error return - perhaps NaN for both fields
	// would be better?? Suspect caller of this method should be
	// in control.
	//

	dest = newType(T_RECTANGLE);
	if ((loc = defaultmatrix.itransform(rect.getX(), rect.getY(), null)) != null) {
	    if ((size = defaultmatrix.idtransform(Math.ceil(rect.getWidth()), Math.ceil(rect.getHeight()), null)) != null) {
		dest.put(N_X, YoixObject.newDouble(loc[0]), false);
		dest.put(N_Y, YoixObject.newDouble(loc[1]), false);
		dest.put(N_WIDTH, YoixObject.newDouble(size[0]), false);
		dest.put(N_HEIGHT, YoixObject.newDouble(size[1]), false);
	    }
	}
	return(dest);
    }


    public static YoixObject
    yoixRectangle(Rectangle rect) {

	YoixObject  dest;
	double      loc[];
	double      size[];

	//
	// Not sure about error return - perhaps NaN for both fields
	// would be better?? Suspect caller of this method should be
	// in control.
	//

	dest = newType(T_RECTANGLE);
	if ((loc = defaultmatrix.itransform(rect.x, rect.y, null)) != null) {
	    if ((size = defaultmatrix.idtransform(rect.width, rect.height, null)) != null) {
		dest.put(N_X, YoixObject.newDouble(loc[0]), false);
		dest.put(N_Y, YoixObject.newDouble(loc[1]), false);
		dest.put(N_WIDTH, YoixObject.newDouble(size[0]), false);
		dest.put(N_HEIGHT, YoixObject.newDouble(size[1]), false);
	    }
	}
	return(dest);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static Font
    javaFont(String name) {

	double  scale;
	Font    font = null;
	int     size;

	if (name != null && name.length() > 0) {
	    if (Character.isDigit(name.charAt(name.length() - 1)) == false)
		name = name + "-1";
	} else name = DEFAULT_JAVA_FONTNAME;		// for backwards compatibility
	if ((font = Font.decode(name)) != null) {
	    if ((scale = VM.getFontMagnification()) > 0.0 && scale != 1.0) {
		size = (int)Math.round(font.getSize()*scale);
		font = new Font(font.getName(), font.getStyle(), size);
	    }
	} else VM.abort(BADFONT, name);

	return(font);
    }


    private static Cursor
    newCursor(YoixObject obj, String name, Cursor cursor) {

	BufferedImage  image;
	YoixObject     currentsize;
	Dimension      size;
	Point          hotspot;

	//
	// We start by forcing the image's size to a value that the system
	// will accept as a cursor. Then we get a copy of the image, which
	// means the image's paint() function will get a chance to adjust
	// the hotspot if we changed the image's size. After paint() runs
	// we read the image's hotspot field and create the cursor. Order
	// of these operations is important!!
	//

	if (obj.isImage() && obj.notNull()) {
	    if ((currentsize = obj.getObject(N_SIZE)) != null) {
		if (currentsize.notNull()) {
		    size = javaDimension(currentsize);
		    size = YoixAWTToolkit.getBestCursorSize(size.width, size.height);
		    obj.putObject(N_SIZE, yoixDimension(size));

		    image = ((YoixBodyImage)obj.body()).copyCurrentImage();
		    hotspot = YoixMakeScreen.javaPoint(obj.getObject(N_HOTSPOT));
		    cursor = YoixAWTToolkit.createCustomCursor(image, hotspot, name);
		    obj.putObject(N_SIZE, currentsize);
		}
	    }
	}
	return(cursor);
    }


    private static YoixObject
    newType(String name) {

	YoixObject  obj;

	if ((obj = (YoixObject)typetemplates.get(name)) == null) {
	    if ((obj = YoixMake.yoixType(name)) != null) {
		synchronized(typetemplates) {
		    typetemplates.put(name, obj);
		}
	    } else VM.die(INTERNALERROR);
	}
	return(obj.duplicate());
    }
}

