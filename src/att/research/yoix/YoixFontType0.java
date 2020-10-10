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
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;

class YoixFontType0

    implements YoixConstants,
	       YoixInterfaceFont

{

    //
    // We derive showfont from font and use it when methods in this class
    // are asked to draw text. It's derived by cancelling out the scaling
    // that's built into the interpreter's default transformation matrix.
    //

    private YoixBodyMatrix  fontmatrix = null;
    private Font            font = null;
    private Font            showfont = null;

    //
    // A flag that currently affects how the non-standard show methods
    // place individual characters when the method's extra arguments
    // would allow showing the entire string. Setting it false means
    // font metrics and string drawing are completely consistent.
    //

    private boolean  placeall = true;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixFontType0(YoixObject data) {

	this.font = YoixMakeScreen.javaFont(data.getObject(N_NAME, null));
	buildFont(data);
    }


    YoixFontType0(YoixObject data, Font font) {

	this.font = font;
	buildFont(data);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceFont Methods
    //
    ///////////////////////////////////

    public final void
    fontAShow(String str, double ax, double ay, double alpha, YoixBodyGraphics graphics) {

	StringBuffer  buf;
	YoixBodyPath  path;
	Graphics2D    g;
	double        origin[];
	int           length;
	int           n;

	if (str != null && graphics != null && font != null) {
	    path = graphics.getCurrentPath();
	    if ((origin = path.getCurrentPoint()) != null) {
		if ((g = graphics.getGraphics2D(alpha)) != null) {
		    g.setTransform(graphics.getCompatibleAffineTransform(g));
		    if (placeall || ax != 0 || ay != 0) {
			buf = new StringBuffer(" ");	// for setCharAt()
			length = str.length();
			for (n = 0; n < length; n++) {
			    buf.setCharAt(0, str.charAt(n));
			    type0Show(new String(buf), origin, ax, ay, g);
			}
		    } else type0Show(str, origin, 0, 0, g);
		    path.pathMoveTo(origin[0], origin[1]);
		    g.dispose();
		}
	    } else VM.abort(NOCURRENTPOINT);
	}
    }


    public final void
    fontAWidthShow(String str, double cx, double cy, int code, double ax, double ay, double alpha, YoixBodyGraphics graphics) {

	StringBuffer  buf;
	YoixBodyPath  path;
	Graphics2D    g;
	double        origin[];
	char          ch;
	int           length;
	int           n;

	if (str != null && graphics != null && font != null) {
	    path = graphics.getCurrentPath();
	    if ((origin = path.getCurrentPoint()) != null) {
		if ((g = graphics.getGraphics2D(alpha)) != null) {
		    g.setTransform(graphics.getCompatibleAffineTransform(g));
		    if (placeall || ax != 0 || ay != 0 || cx != 0 || cy != 0) {
			buf = new StringBuffer(" ");	// for setCharAt()
			length = str.length();
			for (n = 0; n < length; n++) {
			    ch = str.charAt(n);
			    buf.setCharAt(0, ch);
			    if (ch == code)
				type0Show(new String(buf), origin, ax+cx, ay+cy, g);
			    else type0Show(new String(buf), origin, ax, ay, g);
			}
		    } else type0Show(str, origin, 0, 0, g);
		    path.pathMoveTo(origin[0], origin[1]);
		    g.dispose();
		}
	    } else VM.abort(NOCURRENTPOINT);
	}
    }


    public final void
    fontCharPath(String str, boolean stroke, YoixBodyGraphics graphics) {

	FontRenderContext  frc;
	AffineTransform    defaulttransform;
	AffineTransform    transform;
	YoixBodyPath       path;
	GlyphVector        glyphs;
	Graphics2D         g;
	double             origin[];

	if (str != null && graphics != null && font != null) {
	    path = graphics.getCurrentPath();
	    if ((origin = path.getCurrentDevicePoint()) != null) {
		if ((g = graphics.getGraphics2D(false)) != null) {
		    transform = graphics.getCurrentAffineTransform();
		    defaulttransform = VM.getDefaultTransform();
		    transform.scale(1.0/defaulttransform.getScaleX(), 1.0/defaulttransform.getScaleY());
		    transform = new AffineTransform(
			transform.getScaleX(), transform.getShearY(),
			transform.getShearX(), transform.getScaleY(),
			0, 0
		    );
		    frc = g.getFontRenderContext();
		    glyphs = font.deriveFont(transform).createGlyphVector(frc, str);
		    path.pathAppendShape(glyphs.getOutline((float)origin[0], (float)origin[1]));
		    g.dispose();
		}
	    } else VM.abort(NOCURRENTPOINT);
	}
    }


    public final void
    fontDrawString(String str, double x, double y, double alpha, YoixBodyGraphics graphics) {

	Graphics2D  g;

	if (str != null && graphics != null && font != null) {
	    if ((g = graphics.getGraphics2D(alpha)) != null) {
		g.setTransform(graphics.getCompatibleAffineTransform(g));
		g.setFont(showfont);
		g.drawString(str, (float)x, (float)y);
		g.dispose();
	    }
	}
    }


    public final void
    fontDrawString(String str, double x, double y, double alpha, Graphics2D g, YoixBodyGraphics graphics) {

	AffineTransform  currenttransform;
	Font             currentfont;

	if (str != null && g != null && graphics != null && font != null) {
	    currenttransform = g.getTransform();
	    currentfont = g.getFont();
	    g.setTransform(graphics.getCompatibleAffineTransform(g));
	    g.setFont(showfont);
	    g.drawString(str, (float)x, (float)y);
	    g.setFont(currentfont);
	    g.setTransform(currenttransform);
	}
    }


    public final YoixObject
    fontGetFontMatrix() {

	YoixObject  matrix;

	if (fontmatrix == null && font != null) {
	    matrix = YoixMake.yoixType(T_MATRIX);
	    ((YoixBodyMatrix)matrix.body()).setMatrix(font.getTransform());
	    matrix.setAccessBody(LR_X);
	    fontmatrix = (YoixBodyMatrix)matrix.body();
	}

	return(fontmatrix != null ? fontmatrix.getContext() : YoixObject.newMatrix());
    }


    public final Font
    fontGetJavaFont() {

	return(font);
    }


    public final void
    fontKShow(String str, YoixObject proc, double alpha, YoixBodyGraphics graphics) {

	StringBuffer  buf;
	YoixBodyPath  path;
	YoixObject    args[];
	Graphics2D    g;
	double        origin[];
	char          ch;
	int           length;
	int           n;


	if (str != null && graphics != null && font != null) {
	    path = graphics.getCurrentPath();
	    proc = (proc != null && proc.notNull()) ? proc : null;
	    if ((g = graphics.getGraphics2D(alpha)) != null) {
		g.setTransform(graphics.getCompatibleAffineTransform(g));
		if (placeall || proc != null) {
		    args = new YoixObject[] {proc, null, null};
		    buf = new StringBuffer(" ");	// for setCharAt()
		    length = str.length();
		    for (n = 0; n < length; ) {
			if ((origin = path.getCurrentPoint()) != null) {
			    ch = str.charAt(n++);
			    buf.setCharAt(0, ch);
			    type0Show(new String(buf), origin, 0, 0, g);
			    path.pathMoveTo(origin[0], origin[1]);
			    if (proc != null && n < length) {
				args[1] = YoixObject.newInt(ch);
				args[2] = YoixObject.newInt(str.charAt(n));
				graphics.call(args);
			    }
			} else VM.abort(NOCURRENTPOINT);
		    }
		    g.dispose();
		} else if ((origin = path.getCurrentPoint()) != null)
		    type0Show(str, origin, 0, 0, g);
		else VM.abort(NOCURRENTPOINT);
	    }
	}
    }


    public final YoixObject
    fontRotateFont(double angle) {

	AffineTransform  transform;
	YoixObject       obj;

	if (font != null) {
	    transform = font.getTransform();
	    transform.rotate((angle * Math.PI)/180.0);
	    obj = type0DeriveFont(transform);
	} else obj = YoixObject.newFont();

	return(obj);
    }


    public final YoixObject
    fontScaleFont(double sx, double sy) {

	AffineTransform  transform;
	YoixObject       obj;

	if (font != null) {
	    transform = font.getTransform();
	    transform.scale(sx, sy);
	    obj = type0DeriveFont(transform);
	} else obj = YoixObject.newFont();

	return(obj);
    }


    public final YoixObject
    fontShearFont(double shx, double shy) {

	AffineTransform  transform;
	YoixObject       obj;

	if (font != null) {
	    transform = font.getTransform();
	    transform.shear(shx, shy);
	    obj = type0DeriveFont(transform);
	} else obj = YoixObject.newFont();

	return(obj);
    }


    public final void
    fontShow(String str, double alpha, YoixBodyGraphics graphics) {

	YoixBodyPath  path;
	Graphics2D    g;
	double        origin[];

	if (str != null && graphics != null && font != null) {
	    path = graphics.getCurrentPath();
	    if ((origin = path.getCurrentPoint()) != null) {
		if ((g = graphics.getGraphics2D(alpha)) != null) {
		    g.setTransform(graphics.getCompatibleAffineTransform(g));
		    type0Show(str, origin, 0, 0, g);
		    path.pathMoveTo(origin[0], origin[1]);
		    g.dispose();
		}
	    } else VM.abort(NOCURRENTPOINT);
	}
    }


    public final Point2D
    fontStringAdvance(String str, Object arg) {

	Rectangle2D  bounds = fontStringBounds(str, false, arg);

	//
	// Not sure how to detect Java fonts that don't always advance
	// along the horizontal axis, if they even exist, so for now we
	// assume the advance is always horizontal.
	//

	return(new Point2D.Double(bounds.getWidth(), 0));
    }


    public final Rectangle2D
    fontStringBounds(String str, boolean tight, Object arg) {

	FontRenderContext  frc;
	Rectangle2D        bounds = null;
	TextLayout	   layout;
	Graphics2D         g;

	//
	// Decided to use font rather than showfont, so the numbers in the
	// rectangle that we return are in device space. Implies that other
	// classes that implement YoixInterfaceFont should behave the same
	// way.
	//

	if (str != null && font != null && str.length() > 0) {
	    if (arg instanceof YoixObject && ((YoixObject)arg).isDrawable())
		arg = ((YoixObject)arg).getObject(N_GRAPHICS).body();
	    if (arg instanceof YoixBodyGraphics) {
		if ((g = ((YoixBodyGraphics)arg).getGraphics2D(false)) != null) {
		    frc = g.getFontRenderContext();
		    g.dispose();
		} else frc = new FontRenderContext(new AffineTransform(), false, tight);
	    } else frc = new FontRenderContext(new AffineTransform(), false, tight);

	    if (tight) {
		layout = new TextLayout(str, font, frc);
		bounds = layout.getBounds();
	    } else bounds = font.getStringBounds(str, frc);
	}
	return(bounds != null ? bounds : new Rectangle2D.Double());
    }


    public final String
    fontStringFit(String str, double width, String suffix, String substitute, Object arg) {

	return(fontStringFit(str, width, suffix, substitute, true, false, arg));
    }


    public final String
    fontStringFit(String str, double width, String suffix, String substitute, boolean fractionalmetrics, boolean antialiasing, Object arg) {

	FontRenderContext  frc;
	Rectangle2D        bounds = null;
	TextLayout	   layout;
	Graphics2D         g;
	int                length;

	if (str != null && font != null && str.length() > 0) {
	    if (arg instanceof YoixObject && ((YoixObject)arg).isDrawable())
		arg = ((YoixObject)arg).getObject(N_GRAPHICS).body();
	    if (arg instanceof YoixBodyGraphics) {
		if ((g = ((YoixBodyGraphics)arg).getGraphics2D(false)) != null) {
		    frc = g.getFontRenderContext();
		    g.dispose();
		} else frc = new FontRenderContext(new AffineTransform(), antialiasing, fractionalmetrics);
	    } else frc = new FontRenderContext(new AffineTransform(), antialiasing, fractionalmetrics);
	    if (getStringWidth(str, font, frc) > width) {
		if (width > 0) {
		    //
		    // Brute force right now, but we undoubtedly could be smarter.
		    //
		    suffix = (suffix != null) ? suffix : "";
		    if (getStringWidth(suffix, font, frc) <= width) {
			for (length = str.length() - 1; length >= 0; length--) {
			    str = str.substring(0, length) + suffix;
			    if (getStringWidth(str, font, frc) <= width)
				break;
			}
		    } else if (substitute != null)
			str = (getStringWidth(substitute, font, frc) <= width) ? substitute : "";
		    else str = "";
		} else str = "";
	    }
	}
	return(str);
    }


    public final double
    fontStringWidth(String str, Object arg) {

	return(fontStringBounds(str, false, arg).getWidth());
    }


    public final YoixObject
    fontTransformFont(YoixBodyMatrix matrix) {

	AffineTransform  transform;
	YoixObject       obj;

	if (font != null) {
	    transform = font.getTransform();
	    transform.concatenate(matrix.getCurrentAffineTransform());
	    obj = type0DeriveFont(transform);
	} else obj = YoixObject.newFont();

	return(obj);
    }


    public final YoixObject
    fontTranslateFont(double tx, double ty) {

	AffineTransform  transform;
	YoixObject       obj;

	if (font != null) {
	    transform = font.getTransform();
	    transform.translate(tx, ty);
	    obj = type0DeriveFont(transform);
	} else obj = YoixObject.newFont();

	return(obj);
    }


    public final void
    fontWidthShow(String str, double cx, double cy, int code, double alpha, YoixBodyGraphics graphics) {

	StringBuffer  buf;
	YoixBodyPath  path;
	Graphics2D    g;
	double        origin[];
	char          ch;
	int           length;
	int           n;

	if (str != null && graphics != null && font != null) {
	    path = graphics.getCurrentPath();
	    if ((origin = path.getCurrentPoint()) != null) {
		if ((g = graphics.getGraphics2D(alpha)) != null) {
		    g.setTransform(graphics.getCompatibleAffineTransform(g));
		    if (placeall || ((cx != 0 || cy != 0) && str.indexOf(code) >= 0)) {
			buf = new StringBuffer(" ");	// for setCharAt()
			length = str.length();
			for (n = 0; n < length; n++) {
			    ch = str.charAt(n);
			    buf.setCharAt(0, ch);
			    if (ch == code)
				type0Show(new String(buf), origin, cx, cy, g);
			    else type0Show(new String(buf), origin, 0, 0, g);
			}
		    } else type0Show(str, origin, 0, 0, g);
		    path.pathMoveTo(origin[0], origin[1]);
		    g.dispose();
		}
	    } else VM.abort(NOCURRENTPOINT);
	}
    }

    ///////////////////////////////////
    //
    // YoixFontType0 Methods
    //
    ///////////////////////////////////

    protected final void
    finalize() {

	font = null;
	showfont = null;
	fontmatrix = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildFont(YoixObject data) {

	AffineTransform  transform;

	//
	// The scaling used to create showfont is supposed to cancel the
	// scaling in the default transformation matrix, which means that
	// the transform assigned to the Graphics2D before type0Show() is
	// called will remove the scaling applied here and as a result we
	// should end up drawing with the original font. In addition any
	// distances obtained by measuring text using showfont will be in
	// the default Yoix coordinate system, while distances obtained
	// using font will be in device space.
	//

	transform = VM.getDefaultTransform();
	transform.setToScale(1.0/transform.getScaleX(), 1.0/transform.getScaleY());
	transform.preConcatenate(font.getTransform());
	showfont = font.deriveFont(transform);
	type0LoadData(data);
    }


    private double
    getStringWidth(String str, Font font, FontRenderContext frc) {

	Rectangle2D  bounds;
	TextLayout   layout;
	double       width = 0;

	if (str != null && str.length() > 0) {
	    layout = new TextLayout(str, font, frc);
	    bounds = layout.getBounds();
	    width = bounds.getWidth();
	}
	return(width);
    }


    private YoixObject
    type0DeriveFont(AffineTransform transform) {

	YoixObject  obj;
	YoixObject  data;
	Point2D     origin;
	double      ascent;
	double      descent;
	Font        derived;

	derived = font.deriveFont(transform);
	data = VM.getTypeTemplate(T_FONT);
	VM.pushAccess(LRW_);
	obj = YoixObject.newFont(data, new YoixFontType0(data, derived));
	if (transform.isIdentity() == false) {
	    origin = transform.transform(new Point2D.Double(), null);
	    ascent = data.getDouble(N_ASCENT, 0);
	    descent = data.getDouble(N_DESCENT, 0);
	    data.putDouble(N_ASCENT, ascent - origin.getY());
	    data.putDouble(N_DESCENT, descent + origin.getY());
	}
	VM.popAccess();

	return(obj);
    }


    private void
    type0LoadData(YoixObject dict) {

	YoixAWTFontMetrics  fm;
	YoixBodyMatrix      ctm;
	String              fontname;
	String              style;
	double              pointsize;
	double              advance;
	double              ascent;
	double              descent;
	double              height;
	double              leading;
	double              scale;
	double              fail[] = {0.0, 0.0};
	Font                dummy;
	int                 size;

	if (font != null && dict != null) {
	    if ((fm = YoixAWTToolkit.getFontMetrics(font)) != null) {
		ctm = (YoixBodyMatrix)VM.getDefaultMatrix().body();
		fontname = YoixMake.javaFontName(font);
		pointsize = type0PointSize(font.getTransform());
		advance = ctm.idtransform(fm.getMaxAdvance(), 0, fail)[0];
		ascent = ctm.idtransform(0, fm.getMaxAscent(), fail)[1];
		descent = ctm.idtransform(0, fm.getMaxDescent(), fail)[1];
		leading = ctm.idtransform(0, fm.getLeading(), fail)[1];
		height = ctm.idtransform(0, fm.getMaxAscent() + fm.getMaxDescent() + fm.getLeading(), fail)[1];
		dict.putString(N_NAME, fontname);
		dict.putString(N_FAMILY, font.getFamily());
		dict.putString(N_FONTFACENAME, font.getFontName());
		dict.putString(N_PSNAME, font.getPSName());
		dict.putDouble(N_POINTSIZE, pointsize);
		dict.putDouble(N_ADVANCE, advance);
		dict.putDouble(N_ASCENT, ascent);
		dict.putDouble(N_DESCENT, descent);
		dict.putDouble(N_LEADING, leading);
		dict.putDouble(N_HEIGHT, height);

		if ((scale = VM.getFontMagnification()) > 0.0 && scale != 1.0) {
		    if ((dummy = Font.decode(fontname)) != null)
			dict.putInt(N_SIZE, dummy.getSize());
		    else dict.putInt(N_SIZE, font.getSize());
		} else dict.putInt(N_SIZE, font.getSize());

		if (font.isPlain() == false) {
		    style = font.isBold() ? "bold" : "";
		    if (font.isItalic())
			style += "italic";
		} else style = "plain";
		dict.putString(N_STYLE, style);
	    }
	}
    }


    private double
    type0PointSize(AffineTransform transform) {

	Point2D  point;
	double   value;

	value = font.getSize();

	if (transform.isIdentity() == false) {
	    point = new Point2D.Double(0, value);
	    point = transform.deltaTransform(point, null);
	    value = Math.sqrt(point.getX()*point.getX() + point.getY()*point.getY());
	}

	return(value);
    }


    private void
    type0Show(String str, double origin[], double dx, double dy, Graphics2D g) {

	g.setFont(showfont);
	g.drawString(str, (float)origin[0], (float)origin[1]);
	origin[0] += showfont.getStringBounds(str, g.getFontRenderContext()).getWidth();
	origin[0] += dx;
	origin[1] += dy;
    }
}

