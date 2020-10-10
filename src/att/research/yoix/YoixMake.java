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
import java.awt.datatransfer.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

public abstract
class YoixMake

    implements YoixAPI,
	       YoixConstants,
	       YoixConstantsJFC,
	       YoixConstantsSwing

{

    //
    // Miscellaneous object builders. Some important code in this file
    // (e.g., initialize()) is very confusing. We apologize and hope to
    // eventually improve it.
    //

    private static JComponent  iconpainters[] = null;

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public static boolean
    javaBoolean(String str) {

	boolean  value;

	if (str != null) {
	    if (str.equalsIgnoreCase("true"))
		value = true;
	    else if (str.equalsIgnoreCase("false"))
		value = false;
	    else value = (javaInt(str, 0) != 0);
	} else value = false;
	return(value);
    }


    public static Boolean
    javaBooleanObject(String str) {

	return(javaBoolean(str) ? Boolean.TRUE : Boolean.FALSE);
    }


    public static byte[]
    javaByteArray(String str) {

	byte  array[];

	if (str != null) {
	    try {
		array = str.getBytes(YoixConverter.getISO88591Encoding()); // was getDefaultEncoding
	    }
	    catch(UnsupportedEncodingException e) {
		array = new byte[0];
	    }
	} else array = new byte[0];

	return(array);
    }


    static byte[]
    javaByteArray(YoixObject bytes) {

	YoixObject  yobj;
	byte        bytearray[];
	int         m;
	int         n;

	if (bytes != null && bytes.notNull() && bytes.isArray()) {
	    bytearray = new byte[bytes.sizeof()];
	    for (m = bytes.offset(), n=0; n<bytearray.length; m++, n++) {
		yobj = bytes.get(m, false);
		if (yobj.notNull() && yobj.isNumber()) {
		    bytearray[n] = (byte)(0xFF & yobj.intValue());
		} else bytearray[n] = 0;
	    }
	} else bytearray = null;

	return(bytearray);
    }


    static String
    javaByteArrayString(YoixObject bytes) {

	StringBuffer  sb;
	YoixObject    yobj;
	String        str;
	byte          bite;
	int           len;
	int           m;
	int           n;

	if (bytes != null && bytes.notNull() && bytes.isArray()) {
	    len = bytes.sizeof();
	    sb = new StringBuffer(2*len);
	    for (m = bytes.offset(), n=0; n<len; m++, n++) {
		yobj = bytes.get(m, false);
		if (yobj.notNull() && yobj.isNumber()) {
		    bite = (byte)(0xFF & yobj.intValue());
		} else bite = 0;
		sb.append(YoixMisc.HEXCHARS[0x0F & (bite>>4)]);
		sb.append(YoixMisc.HEXCHARS[0x0F & bite]);
	    }
	    str = sb.toString();
	} else str = null;

	return(str);
    }


    public static int
    javaCharacter(String str) {

	int  value = 0;
	int  n;

	str = javaString(str, true, true);
	for (n = 0; n < str.length(); n++)
	    value = (value << 16) | str.charAt(n);

	return(value);
    }


    public static Color
    javaColor(YoixObject obj) {

	YoixObject  red;
	YoixObject  green;
	YoixObject  blue;
	Color       color;

	if (obj != null && obj.notNull()) {
	    red = obj.getObject(N_RED, 0);
	    green = obj.getObject(N_GREEN, 0);
	    blue = obj.getObject(N_BLUE, 0);
	    color = new Color(
		Math.max(Math.min(red.isInteger() ? red.intValue() : (int)(255*red.doubleValue() + 0.5), 255), 0),
		Math.max(Math.min(green.isInteger() ? green.intValue() : (int)(255*green.doubleValue() + 0.5), 255), 0),
		Math.max(Math.min(blue.isInteger() ? blue.intValue() : (int)(255*blue.doubleValue() + 0.5), 255), 0)
	    );
	} else color = null;

	return(color);
    }


    public static Color
    javaColor(YoixObject obj, Color color) {

	return(obj != null && obj.notNull() ? javaColor(obj) : color);
    }


    public static int
    javaColorValue(YoixObject obj) {

	YoixObject  component;
	int         red;
	int         green;
	int         blue;
	int         value;

	if (obj != null && obj.notNull()) {
	    component = obj.getObject(N_RED, 0);
	    red = Math.max(Math.min(component.isInteger() ? component.intValue() : (int)(255*component.doubleValue() + 0.5), 255), 0);
	    component = obj.getObject(N_GREEN, 0);
	    green = Math.max(Math.min(component.isInteger() ? component.intValue() : (int)(255*component.doubleValue() + 0.5), 255), 0);
	    component = obj.getObject(N_BLUE, 0);
	    blue = Math.max(Math.min(component.isInteger() ? component.intValue() : (int)(255*component.doubleValue() + 0.5), 255), 0);
	    value = ((red&0xFF) << 16) | ((green&0xFF) << 8) | (blue&0xFF);
	} else value = 0;

	return(value);
    }


    public static double
    javaDouble(Object obj) {

	double  value;

	if (obj instanceof String)
	    value = javaDouble((String)obj, -1);
	else if (obj instanceof Number)
	    value = ((Number)obj).doubleValue();
	else if (obj instanceof Boolean)
	    value = ((Boolean)obj).booleanValue() ? 1 : 0;
	else if (obj instanceof YoixObject)
	    value = ((YoixObject)obj).isNumber() ? ((YoixObject)obj).doubleValue() : 0;
	else value = 0;

	return(value);
    }


    public static double
    javaDouble(String str, double value) {

	try {
	    value = Double.valueOf(str).doubleValue();
	}
	catch(NumberFormatException e) {
	    if (str != null) {
		if (str.equals("NaN"))
		    value = Double.NaN;
		else if (str.equals("Infinity"))
		    value = Double.POSITIVE_INFINITY;
		else if (str.equals("-Infinity"))
		    value = Double.NEGATIVE_INFINITY;
	    }
	}

	return(value);
    }


    public static float
    javaFloat(Object obj) {

	float  value;

	if (obj instanceof String)
	    value = javaFloat((String)obj, -1);
	else if (obj instanceof Number)
	    value = ((Number)obj).floatValue();
	else if (obj instanceof Boolean)
	    value = ((Boolean)obj).booleanValue() ? 1 : 0;
	else if (obj instanceof YoixObject)
	    value = ((YoixObject)obj).isNumber() ? ((YoixObject)obj).floatValue() : 0;
	else value = 0;

	return(value);
    }


    public static float
    javaFloat(String str, float value) {

	try {
	    value = Float.valueOf(str).floatValue();
	}
	catch(NumberFormatException e) {}

	return(value);
    }


    public static Icon
    javaIcon(YoixObject obj) {

	YoixObject  desc;
	String      description;
	Image       image;
	Icon        icon = null;

	if ((image = javaImage(obj)) != null) {
	    if ((desc = obj.getObject(N_DESCRIPTION)) != null) {
		if (desc.isString() && desc.notNull())
		    description = desc.stringValue();
		else description = null;
	    } else description = null;
	    icon = new ImageIcon(image, description);
	}
	return(icon);
    }


    public static Image
    javaImage(YoixObject obj) {

	BufferedImage  image = null;

	if (obj.notNull()) {
	    if (obj.isString()) {
		obj = YoixObject.newImage(obj.stringValue());
		image = ((YoixBodyImage)(obj.body())).getCurrentImage();
	    } else if (obj.isImage())
		image = ((YoixBodyImage)(obj.body())).copyCurrentImage();
	}
	return(image);
    }


    public static int
    javaInt(Object obj) {

	int  value;

	if (obj instanceof String)
	    value = javaInt((String)obj, 10, -1);
	else if (obj instanceof Number)
	    value = ((Number)obj).intValue();
	else if (obj instanceof Boolean)
	    value = ((Boolean)obj).booleanValue() ? 1 : 0;
	else if (obj instanceof YoixObject)
	    value = ((YoixObject)obj).isNumber() ? ((YoixObject)obj).intValue() : 0;
	else value = 0;			// very old versions died here

	return(value);
    }


    public static int
    javaInt(String str, int value) {

	return(javaInt(str, 10, value));
    }


    public static int
    javaInt(String str, int radix, int value) {

	try {
	    value = Integer.parseInt(str, radix);
	}
	catch(NumberFormatException e) {}

	return(value);
    }


    public static Point
    javaPoint(double x, double y, YoixBodyMatrix mtx) {

	double  loc[] = mtx.transform(x, y);

	return(new Point((int)Math.floor(loc[0]), (int)Math.floor(loc[1])));
    }



    public static String
    javaString(byte bytes[]) {

	return(bytes != null ? javaString(bytes, 0, bytes.length) : null);
    }


    public static String
    javaString(byte bytes[], int offset, int length) {

	String  str = null;
	String  encoding;

	if (bytes != null) {
	    encoding = YoixConverter.getISO88591Encoding(); // was getDefaultEncoding
	    try {
		str = new String(bytes, offset, length, encoding);
	    }
	    catch(RuntimeException e) {
		try {
		    length = Math.min(bytes.length - offset, length);
		    str = new String(bytes, offset, length, encoding);
		}
		catch(UnsupportedEncodingException ee) {}
	    }
	    catch(UnsupportedEncodingException e) {}
	}

	return(str);
    }


    public static String
    javaString(ByteArrayOutputStream stream) {

	String  str = null;

	if (stream != null) {
	    try {
		str = stream.toString(YoixConverter.getISO88591Encoding()); // was getDefaultEncoding
	    }
	    catch(UnsupportedEncodingException e) {}
	}

	return(str);
    }


    public static String
    javaString(int ch) {
	return(javaString(ch, false));
    }


    public static String
    javaString(int ch, boolean forced) {

	String  str;
	int     n;

	switch (ch) {
	    case '\b':
		str = "\\b";
		break;

	    case '\f':
		str = "\\f";
		break;

	    case '\n':
		str = "\\n";
		break;

	    case '\r':
		str = "\\r";
		break;

	    case '\t':
		str = "\\t";
		break;

	    case '\\':
		str = "\\\\";
		break;

	    case '"':
		str = "\\\"";
		break;

	    default:
		if (forced || ch < ' ' || ch > '~') {
		    if (ch > 0xFF) {
			str = Integer.toHexString(ch);
			for (n = str.length(); n < 4; n++)
			    str = '0' + str;
			str = "\\x" + str;
		    } else {
			str = Integer.toOctalString(ch);
			for (n = str.length(); n < 3; n++)
			    str = '0' + str;
			str = "\\" + str;
		    }
		} else str = (char)ch + "";
		break;
	}

	return(str);
    }


    public static String
    javaString(String str, boolean escapes, boolean delimiters) {

	char  buf[];
	char  ch;
	int   delim;
	int   digit;
	int   limit;
	int   m;
	int   n;

	if (escapes && str.indexOf('\\') >= 0) {
	    m = 0;
	    n = 0;
	    buf = str.toCharArray();
	    delim = (delimiters) ? buf[n++] : -1;
	    while (n < buf.length && (ch = buf[n++]) != delim) {
		if (ch == '\\' && n < buf.length) {
		    switch (ch = buf[n++]) {
			case 'b':
			    ch = '\b';
			    break;

			case 'f':
			    ch = '\f';
			    break;

			case 'n':
			    ch = '\n';
			    break;

			case 'r':
			    ch = '\r';
			    break;

			case 't':
			    ch = '\t';
			    break;

			case 'x':
			    ch = 0;
			    for (limit = n + 4; n < limit && n < buf.length; n++) {
				if ((digit = YoixMisc.hexDigit(buf[n], -1)) >= 0)
				    ch = (char)((ch << 4) | digit);
				else break;
			    }
			    break;

			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			    ch = (char)YoixMisc.octalDigit(ch, 0);
			    for (limit = n + 2; n < limit && n < buf.length; n++) {
				if ((digit = YoixMisc.octalDigit(buf[n], -1)) >= 0)
				    ch = (char)((ch << 3) | digit);
				else break;
			    }
			    break;

			case '\r':
			    if (buf[n] == '\n')		// always true - check grammar??
				n++;
			    continue;

			case '\n':
			    continue;
		    }
		}
		buf[m++] = ch;
	    }
	    str = new String(buf, 0, m);
	} else if (delimiters) {
	    if ((n = str.lastIndexOf(str.charAt(0))) > 0)
		str = str.substring(1, n);
	    else str = str.substring(1);
	}

	return(str);
    }


    public static YoixObject
    yoixArray(YoixObject args[]) {

	return(yoixArray(args, 0));
    }


    public static YoixObject
    yoixArray(String args[]) {

	YoixObject  obj;
	int         n;

	obj = YoixObject.newArray(args.length);

	for (n = 0; n < args.length; n++) {
	    if (args[n] != null)
		obj.put(n, YoixObject.newString(args[n]), true);
	}

	return(obj);
    }


    public static YoixObject
    yoixArray(AbstractList args) {

	YoixObject  obj;
	Iterator    iter;
	Object      element;
	int         size;
	int         n;

	if (args != null) {
	    size = args.size();
	    obj = YoixObject.newArray(size);
	    iter = args.iterator();
	    for (n = 0; iter.hasNext(); n++) {
		element = iter.next();
		if (element instanceof YoixObject)
		    obj.put(n, (YoixObject)element, true);
		else if (element instanceof YoixObject[])
		    obj.put(n, yoixArray((YoixObject[])element), true);
	    }
	} else obj = YoixObject.newArray();

	return(obj);
    }


    public static YoixObject
    yoixArray(YoixObject args[], int start) {

	YoixObject  obj;
	int         n;

	obj = YoixObject.newArray(Math.max(args.length - start, 0));

	for (n = start; n < args.length; n++) {
	    if (args[n] != null)
		obj.put(n - start, args[n], true);
	}

	return(obj);
    }


    public static YoixObject
    yoixBBox(Rectangle rect, YoixObject data) {

	YoixObject  graphics;
	YoixObject  mtx;

	if ((graphics = data.getObject(N_GRAPHICS)) != null) {
	    if ((mtx = graphics.getObject(N_CTM)) == null)
		mtx = VM.getDefaultMatrix();
	} else mtx = VM.getDefaultMatrix();

	return(yoixBBox(rect, (YoixBodyMatrix)mtx.body()));
    }


    public static YoixObject
    yoixColor(Color color) {

	YoixObject  obj;

	if (color != null) {
	    obj = yoixType(T_COLOR);
	    obj.put(N_RED, YoixObject.newDouble(color.getRed()/255.0), false);
	    obj.put(N_GREEN, YoixObject.newDouble(color.getGreen()/255.0), false);
	    obj.put(N_BLUE, YoixObject.newDouble(color.getBlue()/255.0), false);
	} else obj = YoixObject.newNull(T_COLOR);

	return(obj);
    }


    public static YoixObject
    yoixFont(Font font) {

	YoixObject  obj;
	YoixObject  data;

	if (font != null) {
	    data = YoixObject.newDictionary(1);
	    data.putString(N_NAME, javaFontName(font));
	    obj = yoixType(T_FONT, data);
	} else obj = null;

	return(obj);
    }


    public static YoixObject
    yoixPoint(Point point, YoixBodyMatrix matrix) {

	YoixObject  dest;
	double      loc[];

	//
	// Not sure about error return - perhaps NaN for both fields
	// would be better?? Suspect caller of this method should be
	// in control.
	//

	dest = yoixType(T_POINT);
	if ((loc = matrix.itransform(point.x, point.y, null)) != null) {
	    dest.put(N_X, YoixObject.newDouble(loc[0]), false);
	    dest.put(N_Y, YoixObject.newDouble(loc[1]), false);
	}
	return(dest);
    }


    public static YoixObject
    yoixType(String name) {

	return(yoixType(name, -1, -2, YoixObject.newEmpty(), null, false));
    }


    public static YoixObject
    yoixType(String name, YoixObject ival) {

	if (ival == null)
	    ival = YoixObject.newEmpty();
	return(yoixType(name, -1, -2, ival, null, false));
    }

    ///////////////////////////////////
    //
    // YoixMake Methods
    //
    ///////////////////////////////////

    static String
    CString(YoixObject obj) {

	return(CString(obj, 0));
    }


    static String
    CString(YoixObject obj, int max) {

	String  str;
	int     index;

	if (!obj.isString())
	    str = obj.toString().trim();
	else if (obj.offset() != obj.length()) {
	    str = obj.stringValue();
	    if ((index = str.indexOf('\0')) != -1)
		str = str.substring(0, index);
	} else str = "";

	return(max == -1 || (max > 0 && str.length() < max) ? str + '\0' : str);
    }


    static YoixObject
    initialize(YoixObject obj, YoixObject ival) {

	return(obj != null && ival != null ? initialize(obj, ival, null, false) : obj);
    }


    static YoixObject[]
    javaArray(YoixObject obj) {

	YoixObject  array[] = null;
	int         offset;
	int         length;
	int         n;

	if (obj.isArray()) {
	    if (obj.notNull()) {
		offset = obj.offset();
		length = obj.length();
		array = new YoixObject[length - offset];
		for (n = 0; offset < length; n++, offset++) {
		    if (obj.defined(offset))
			array[n] = obj.get(offset, true);
		    else array[n] = null;
		}
	    }
	} else VM.abort(TYPECHECK);

	return(array);
    }


    static Rectangle2D
    javaBBox(Rectangle2D rect, YoixBodyMatrix matrix) {

	return(getBoundingBox(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), matrix));
    }


    static Rectangle2D
    javaBBox(double x, double y, double width, double height, YoixBodyMatrix matrix) {

	return(getBoundingBox(x, y, width, height, matrix));
    }


    static String
    javaFontName(Font font) {

	String  name;
	String  style;

	if (font != null) {
	    name = font.getName();
	    if (font.isBold())
		style = font.isItalic() ? "bolditalic" : "bold";
	    else style = font.isItalic() ? "italic" : "plain";
	    name += "-" + style + "-" + (int)scaleFont(font);
	} else name = null;

	return(name);
    }


    static String
    javaHexString(String str) {

	byte  buf[];
	int   length;
	int   digit;
	int   m;
	int   n;

	//
	// May eventually want to generalize this. One approach could
	// accept hex strings in the format,
	//
	//		0x"......"
	//		0x1"......"
	//		0x2"......"
	//		0x3"......"
	//		0x4"......"
	//
	// and use the number following the 0x (if any) to group hex
	// digits, rather than always assuming 2 digits per character.
	//

	length = str.length();
	buf = new byte[(length + 1)/2];

	for (m = 0, n = 3; n < length; n++) {
	    if ((digit = YoixMisc.hexDigit(str.charAt(n), -1)) >= 0) {
		if (m%2 == 0)
		    buf[m/2] = (byte)(digit << 4);
		else buf[m/2] |= (byte)digit;
		m++;
	    }
	}

	return(javaString(buf, 0, (m + 1)/2));
    }


    static Locale
    javaLocale(YoixObject obj) {

	Object  locale;

	if (obj != null && (locale = obj.getManagedObject()) != null) {
	    if (locale instanceof Locale)
		locale = (((Locale)locale).clone());
	     else locale = Locale.getDefault();
	} else locale = Locale.getDefault();

	return((Locale)locale);
    }


    static Object
    javaObject(YoixObject yobj) {

	return(javaObject(yobj, null));
    }


    static Object
    javaObject(YoixObject yobj, Object obj) {

	YoixObject  element;
	String      tobj[];
	Object      nobj = null;
	int         length;
	int         offset;
	int         n;

	if (yobj.notNull()) {
	    if (yobj.isNumber()) {
		if (obj instanceof Boolean)
		    nobj = new Boolean(yobj.booleanValue());
		else if (obj instanceof Integer)
		    nobj = new Integer(yobj.intValue());
		else if (obj instanceof Double)
		    nobj = new Double(yobj.doubleValue());
		else if (yobj.isInteger())
		    nobj = new Integer(yobj.intValue());
		else nobj = new Double(yobj.doubleValue());
	    } else if (yobj.isString()) {
		if (obj instanceof StringBuffer)
		    nobj = new StringBuffer(yobj.stringValue());
		else nobj = new String(yobj.stringValue());
	    } else if (yobj.isArray()) {
		if (obj instanceof String[]) {
		    length = yobj.length();
		    offset = yobj.offset();
		    tobj = new String[length - offset];
		    for (n = 0; offset < length; n++, offset++) {
			element = yobj.get(offset, false);
			if (element.isNull() || !element.isString()) {
			    tobj = null;
			    break;
			}
			tobj[n] = new String(element.stringValue());
		    }
		    nobj = tobj;
		} else nobj = null;
	    } else if (yobj.isBorder())
		nobj = YoixMakeScreen.javaBorder(yobj);
	    else if (yobj.isColor())
		nobj = javaColor(yobj);
	    else if (yobj.isDimension())
		nobj = YoixMakeScreen.javaDimension(yobj);
	    else if (yobj.isFont())
		nobj = YoixMakeScreen.javaFont(yobj);
	    else if (yobj.isImage())
		nobj = javaIcon(yobj);
	    else if (yobj.isInsets())
		nobj = YoixMakeScreen.javaInsets(yobj);
	    else nobj = null;
	} else nobj = null;

	return(nobj);
    }

    static String
    javaRegexString(String str) {

	char  buf[];
	char  ch;
	int   delim;
	int   digit;
	int   limit;
	int   m;
	int   n;

	if (str.indexOf('\\') >= 0) {
	    m = 0;
	    n = 0;
	    buf = str.toCharArray();
	    delim =  buf[n++];
	    while (n < buf.length && (ch = buf[n++]) != delim) {
		if (ch == '\\' && n < buf.length) {
		    switch (ch = buf[n++]) {
			case 'b':
			    ch = '\b';
			    break;

			case 'f':
			    ch = '\f';
			    break;

			case 'n':
			    ch = '\n';
			    break;

			case 'r':
			    ch = '\r';
			    break;

			case 't':
			    ch = '\t';
			    break;

			case 'x':
			    ch = 0;
			    for (limit = n + 4; n < limit && n < buf.length; n++) {
				if ((digit = YoixMisc.hexDigit(buf[n], -1)) >= 0)
				    ch = (char)((ch << 4) | digit);
				else break;
			    }
			    break;

			case '\\':
			    break;

			case '\r':
			    if (buf[n] == '\n')		// always true - check grammar??
				n++;
			    continue;

			case '\n':
			    continue;

		        default:
			    if (ch != delim) {
				buf[m++] = '\\';
			    }
			    break;
		    }
		}
		buf[m++] = ch;
	    }
	    str = new String(buf, 0, m);
	} else str = str.substring(1, str.length() - 1);

	return(str);
    }


    static String[]
    javaStringArray(YoixObject obj) {

	return(javaStringArray(obj, false, null));
    }


    static String[]
    javaStringArray(YoixObject obj, boolean nullpad) {

	return(javaStringArray(obj, nullpad, null));
    }


    static String[]
    javaStringArray(YoixObject obj, boolean nullpad, String padding) {

	YoixObject  element;
	String      array[];
	String      temp[];
	String      value;
	int         length;
	int         count;
	int         n;

	if (obj.isArray() && obj.notNull()) {
	    length = obj.length();
	    array = new String[obj.sizeof()];
	    for (count = 0, n = obj.offset(); n < length; n++) {
		if ((element = obj.getObject(n)) != null) {
		    if (element.isString())
			value = element.stringValue();
		    else value = null;
		} else value = null;
		if (value != null)
		    array[count++] = value;
		else if (nullpad)
		    array[count++] = padding;
	    }
	    if (count < array.length) {
		temp = new String[count];
		System.arraycopy(array, 0, temp, 0, count);
		array = temp;
	    }
	} else array = null;

	return(array);
    }


    static String[]
    javaStringArray(List list) {

	return(javaStringArray(list, false, null));
    }


    static String[]
    javaStringArray(List list, boolean nullpad) {

	return(javaStringArray(list, nullpad, null));
    }


    static String[]
    javaStringArray(List list, boolean nullpad, String padding) {

	Object  element;
	String  array[];
	String  temp[];
	String  value;
	int     length;
	int     count;
	int     n;

	if (list != null) {
	    length = list.size();
	    array = new String[length];
	    for (count = 0, n = 0; n < length; n++) {
		if ((element = list.get(n)) != null) {
		    if (element instanceof YoixObject) {
			if (((YoixObject)element).isString())
			    value = ((YoixObject)element).stringValue();
			else value = null;
		    } else if (element instanceof String)
			value = (String)element;
		    else value = null;
		} else value = null;
		if (value != null)
		    array[count++] = value;
		else if (nullpad)
		    array[count++] = padding;
	    }
	    if (count < array.length) {
		temp = new String[count];
		System.arraycopy(array, 0, temp, 0, count);
		array = temp;
	    }
	} else array = null;

	return(array);
    }


    static TimeZone
    javaTimeZone(YoixObject obj) {

	Object  tz;

	if (obj != null && (tz = obj.getManagedObject()) != null) {
	    if (tz instanceof TimeZone)
		tz = (((TimeZone)tz).clone());
	     else tz = YoixMiscTime.getDefaultTimeZone();
	} else tz = YoixMiscTime.getDefaultTimeZone();

	return((TimeZone)tz);
    }


    static byte[]
    javaUTFByteArray(String str) {

	byte  array[];

	if (str != null) {
	    try {
		array = str.getBytes(YoixConverter.getUTF8Encoding());
	    }
	    catch(UnsupportedEncodingException e) {
		array = new byte[0];
	    }
	} else array = new byte[0];

	return(array);
    }


    static String
    javaUTFString(byte bytes[]) {

	String  str = null;

	if (bytes != null) {
	    try {
		str = new String(bytes, 0, bytes.length, YoixConverter.getUTF8Encoding());
	    }
	    catch(UnsupportedEncodingException e) {}
	}

	return(str);
    }


    static String
    javaUTFString(ByteArrayOutputStream stream) {

	String  str = null;

	if (stream != null) {
	    try {
		str = stream.toString(YoixConverter.getUTF8Encoding());
	    }
	    catch(UnsupportedEncodingException e) {}
	}

	return(str);
    }


    static YoixObject
    yoixBBox(Rectangle rect) {

	return(yoixBBox(rect, (YoixBodyMatrix)VM.getDefaultMatrix().body()));
    }


    static YoixObject
    yoixBBox(Rectangle rect, YoixBodyMatrix matrix) {

	Rectangle2D  bbox;
	YoixObject   dest;

	bbox = getBoundingBox(rect.x + 0.5, rect.y + 0.5, rect.width, rect.height, matrix);
	dest = yoixType(T_RECTANGLE);
	dest.put(N_X, YoixObject.newDouble(bbox.getX()), false);
	dest.put(N_Y, YoixObject.newDouble(bbox.getY()), false);
	dest.put(N_WIDTH, YoixObject.newDouble(bbox.getWidth()), false);
	dest.put(N_HEIGHT, YoixObject.newDouble(bbox.getHeight()), false);
	return(dest);
    }


    static YoixObject
    yoixByteArray(byte bytearray[]) {

	YoixObject  bytes;
	int         n;

	if (bytearray != null) {
	    bytes = YoixObject.newArray(bytearray.length);
	    for (n = 0; n < bytearray.length; n++)
		bytes.putInt(n, (int)(0xFF & bytearray[n]));
	} else bytes = YoixObject.newArray();

	return(bytes);
    }


    static YoixObject
    yoixByteArrayString(byte bytearray[]) {

	StringBuffer  sb;
	YoixObject    bytes;
	int           n;

	if (bytearray != null) {
	    sb = new StringBuffer(2*bytearray.length);
	    for (n = 0; n < bytearray.length; n++) {
		sb.append(YoixMisc.HEXCHARS[0x0F & (bytearray[n]>>4)]);
		sb.append(YoixMisc.HEXCHARS[0x0F & bytearray[n]]);
	    }
	    bytes = YoixObject.newString(sb.toString());
	} else bytes = YoixObject.newString();

	return(bytes);
    }


    static YoixObject
    yoixGlobal(YoixObject dict) {

	YoixObject  argv;
	YoixObject  argc;
	YoixObject  envp;
	YoixObject  errordict;
	YoixObject  importdict;
	YoixObject  typedict;
	YoixObject  vm;
	boolean     restricted;
	int         n;

	//
	// N_TYPEDICT entry should be readonly, which also implies that
	// this method needs to be more careful about put() calls. Think
	// it should be rewritten and do checks and put() calls only when
	// necessary. The other version (below) also needs attention.
	//

	argv = dict.getObject(N_ARGV);
	argc = dict.getObject(N_ARGC);
	envp = dict.getObject(N_ENVP, null);
	errordict = dict.getObject(N_ERRORDICT, null);
	importdict = dict.getObject(N_IMPORTDICT, null);
	typedict = dict.getObject(N_TYPEDICT, null);
	vm = dict.getObject(N_VM, null);

	if (argv == null || argv.isArray()) {
	    if (argc == null || argc.isInteger()) {
		if (envp == null || envp.isArray()) {
		    if (errordict == null || errordict.isDictionary()) {
			if (typedict == null || typedict.isDictionary()) {
			    if (vm == null || vm.isDictionary()) {
				if (argv == null) {
				    argv = YoixObject.newArray(1);
				    argv.putString(0, "--unknown--");
				    dict.declare(N_ARGV, argv, RW_);
				}
				if (argc == null)
				    dict.declare(N_ARGC, YoixObject.newInt(argv.sizeof()), RW_);
				if (envp == null)
				    dict.declare(N_ENVP, YoixObject.newArray(), RW_);
				if (typedict == null)
				    dict.declare(N_TYPEDICT, VM.newTypedict(), LR__);
				if (errordict == null)
				    dict.declare(N_ERRORDICT, VM.newErrordict(), LR__);
				if (vm == null)
				    dict.declare(N_VM, VM.newVM(), LR__);
				if (importdict == null)
				    dict.declare(N_IMPORTDICT, VM.newImports(), LR__);
			    } else VM.abort(BADGLOBALBLOCK, N_VM);
			} else VM.abort(BADGLOBALBLOCK, N_TYPEDICT);
		    } else VM.abort(BADGLOBALBLOCK, N_ERRORDICT);
		} else VM.abort(BADGLOBALBLOCK, N_ENVP);
	    } else VM.abort(BADGLOBALBLOCK, N_ARGC);
	} else VM.abort(BADGLOBALBLOCK, N_ARGV);

	return(dict);
    }


    static YoixObject
    yoixIcon(Icon icon) {

	BufferedImage  image;
	YoixObject     iconimage = null;
	Graphics       g;
	int            n;
	int            height;
	int            width;

	//
	// Recently changed (12/22/05) to really handle UI resource icons,
	// which sometimes need a real component, like JButton(), when we
	// ask them to paint themselves in the BufferedImage. Unfortunately
	// low level Java code in some look-and-feels explicitly cast the
	// Component argument to something it might not be. Caused problems
	// (e.g., ClassCastExceptions) that we try to avoid using an array
	// of a few components (arrived at by trial and error) that seem
	// to let us eventually get our hands on a properly painted image.
	// Definitely a kludge, but there probably isn't a better fix.
	// 

	if (icon != null) {
	    width = icon.getIconWidth();
	    height = icon.getIconHeight();
	    if (width > 0 && height > 0) {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		if ((g = image.getGraphics()) != null) {
		    if (iconpainters == null) {
			iconpainters = new JComponent[] {
			    new JRadioButton(),
			    new JCheckBox(),
			    new JPanel()		// just in case
			};
		    }

		    for (n = 0; n < iconpainters.length; n++) {
			try {
			    icon.paintIcon(iconpainters[n], g, 0, 0);
			    iconimage = YoixObject.newImage(image);
			    if (icon instanceof ImageIcon)
				iconimage.putString(N_DESCRIPTION, ((ImageIcon)icon).getDescription());
			    break;
			}
			catch (Throwable t) {}
		    }
		    g.dispose();
		}
	    }
	}
	return(iconimage != null ? iconimage : YoixObject.newImage());
    }


    static YoixObject
    yoixInstance(String name) {

	YoixObject  instance;
	YoixObject  obj;

	//
	// Much like yoixType() but the null ival is supposed to make
	// sure we don't create too much (e.g., AWT Components).
	//

	if ((instance = yoixType(name, -1, -2, null, null, false)) != null) {
	    if (instance.notNull()) {
		if ((obj = YoixObject.newNull(instance)) != null)
		    instance = obj;
	    }
	} else VM.die(INTERNALERROR);

	return(instance);
    }


    static YoixObject
    yoixJTreeNode(Object arg) {

	YoixObject  obj;

	if (arg instanceof String) {
	    obj = yoixType(T_JTREENODE);
	    obj.putString(N_TEXT, (String)arg);
	} else if (arg instanceof DefaultMutableTreeNode)
	    obj = YoixSwingJTree.yoixJTreeNode2((DefaultMutableTreeNode)arg);
	else obj = YoixObject.newNull(T_JTREENODE);

	return(obj);
    }


    static YoixObject
    yoixMimeTypes(DataFlavor flavors[]) {

	YoixObject  obj;
	int         n;

 	if (flavors != null) {
	    obj = YoixObject.newArray(flavors.length);
	    for (n = 0; n < flavors.length; n++)
		obj.putString(n, flavors[n].getMimeType());
	} else obj = YoixObject.newArray();
	return(obj);
    }


    static YoixObject
    yoixObject(Object obj) {

	YoixObject  yobj = null;

	if (obj == null)
	    yobj = YoixObject.newNull();
	else if (obj instanceof Boolean)
	    yobj = YoixObject.newInt(((Boolean)obj).booleanValue());
	else if (obj instanceof Border)
	    yobj = YoixMakeScreen.yoixBorder((Border)obj);
	else if (obj instanceof Color)
	    yobj = yoixColor((Color)obj);
	else if (obj instanceof Dimension)
	    yobj = YoixMakeScreen.yoixDimension((Dimension)obj);
	else if (obj instanceof Font)
	    yobj = yoixFont((Font)obj);
	else if (obj instanceof Image)
	    yobj = YoixObject.newImage((Image)obj);
	else if (obj instanceof Icon)
	    yobj = yoixIcon((Icon)obj);
	else if (obj instanceof Insets)
	    yobj = YoixMakeScreen.yoixInsets((Insets)obj);
	else if (obj instanceof Number)
	    yobj = YoixObject.newNumber(((Number)obj).intValue());
	else if (obj instanceof String)
	    yobj = YoixObject.newString((String)obj);
	else if (obj instanceof StringBuffer)
	    yobj = YoixObject.newString((StringBuffer)obj);
	else if (obj instanceof String[])
	    yobj = yoixArray((String[])obj);
	else if (obj instanceof YoixObject)
	    yobj = (YoixObject)obj;
	return(yobj);
    }


    static YoixObject
    yoixRestricted(YoixObject dict) {

	YoixObject  argv;
	YoixObject  restricted;
	String      name;
	int         n;

	//
	// Transforms dict into a dictionary that can be used to start a
	// restricted block. We make sure dict pass lots of tests, which
	// currently means it can only be used once. Chance we'll relax
	// some of the checking in the future, but we don't recommend
	// you do it!!
	//

	if (dict.defined(N_ARGV) == false) {
	    if (dict.defined(N_ARGC) == false) {
		if (dict.defined(N_ENVP) == false) {
		    if (dict.defined(N_ERRORDICT) == false) {
			if (dict.defined(N_IMPORTDICT) == false) {
			    if (dict.defined(N_TYPEDICT) == false) {
				if (dict.defined(N_VM) == false) {
				    if ((restricted = YoixBodyBlock.getReserved(true)) != null) {
					for (n = 0; n < restricted.length(); n++) {
					    if ((name = restricted.name(n)) != null) {
						if (dict.defined(name) == false)
						    dict.declare(name, restricted.getObject(name));
						else VM.abort(BADRESTRICTEDBLOCK, name);
					    }
					}
					argv = YoixObject.newArray(1);
					argv.putString(0, "--unknown--");
					dict.declare(N_ARGV, argv, RW_);
					dict.declare(N_ARGC, YoixObject.newInt(argv.sizeof()), RW_);
					dict.declare(N_ENVP, YoixObject.newArray(), RW_);
					dict.declare(N_ERRORDICT, VM.newErrordict(), LR__);
					dict.declare(N_IMPORTDICT, VM.newImports(), LR__);
					dict.declare(N_TYPEDICT, VM.newTypedict(dict.getObject(N_TYPENAMES)), LR__);
					dict.declare(N_VM, VM.newVM(true), LR__);
				    } else VM.abort(BADRESTRICTEDBLOCK, N_RESERVED);
				} else VM.abort(BADRESTRICTEDBLOCK, N_VM);
			    } else VM.abort(BADRESTRICTEDBLOCK, N_TYPEDICT);
			} else VM.abort(BADRESTRICTEDBLOCK, N_IMPORTDICT);
		    } else VM.abort(BADRESTRICTEDBLOCK, N_ERRORDICT);
		} else VM.abort(BADRESTRICTEDBLOCK, N_ENVP);
	    } else VM.abort(BADRESTRICTEDBLOCK, N_ARGC);
	} else VM.abort(BADRESTRICTEDBLOCK, N_ARGV);

	return(dict);
    }


    static YoixObject
    yoixThread(Thread thread) {

	YoixObject  obj;

	if ((obj = YoixBodyThread.activeThread(thread)) == null)
	    obj = YoixObject.newThread(thread);

	return(obj);
    }


    static YoixObject
    yoixType(String name, int length, int limit, YoixObject ival, YoixObject tags, boolean adjust) {

	YoixObject  argv[];
	YoixObject  type;
	YoixObject  obj;
	boolean     force;
	String      classname;
	int         major;
	int         minor;

	//
        // Very confusing code, particularly when asked to duplicate a
	// template, but it has improved slightly. Still assumes ival
	// is non-null, but that should be easy to change if you want.
	// The adjust argument a new kludge that's only used to figure
	// out when the length argument can be adjusted. Only used when
	// we build a dictionary and the declaration looked something
	// like,
	//
	//	Dictionary d[] = {"name1", "value1", "name2", "value2"};
	//
	// because in the interpreter will set length to 4, but we have
	// recently changed how the values in non-compound initializers
	// are used.
	//

	obj = null;
	major = YOIX_EOF;
	minor = YOIX_EOF;
	classname = null;
	force = false;

	if ((type = VM.getTypeDefinition(name)) != null) {
	    if (type.isInteger()) {
		switch (type.intValue()) {
		    case ARRAY:
			obj = YoixObject.newArray(length);
			break;

		    case BUILTIN:
			if (length < 0)
			    obj = YoixObject.newBuiltin();
			else VM.abort(BADDECLARATION, name);
			break;

		    case CALLABLE:
			if (length < 0) {
			    obj = YoixObject.newFunction();
			    obj.setModeBits(ANYMINOR);
			} else VM.abort(BADDECLARATION, name);
			break;

		    case DICTIONARY:
			//
			// Changed on 5/13/08 to allow array initializers without
			// requiring square brackets in the declaration. Old code
			// did
			//
			//	if (adjust && ival != null && length > 0) {
			//	    if (ival.compound() == false)
			//		length /= 2;
			//	}
			//
			// so we tried to maintain that logic even though the code
			// probably could be improved. Incidentally, the old code
			// let strings through as initializers so we didn't change
			// that either!!
			//
			if (ival != null && ival.compound() == false) {
			    if (length >= 0) {
				if (adjust)
				    length /= 2;
			    } else if (ival.sizeof() > 0)
				length = ival.sizeof()/2;
			}
			obj = YoixObject.newDictionary(length);
			break;

		    case DOUBLE:
			if (length < 0)
			    obj = YoixObject.newDouble(0);
			else VM.abort(BADDECLARATION, name);
			break;

		    case ELEMENT:
			if (length < 0)
			    obj = YoixObject.newElement();
			else VM.abort(BADDECLARATION, name);
			break;

		    case FUNCTION:
			if (length < 0)
			    obj = YoixObject.newFunction();
			else VM.abort(BADDECLARATION, name);
			break;

		    case INTEGER:
			if (length < 0)
			    obj = YoixObject.newInt(0);
			else VM.abort(BADDECLARATION, name);
			break;

		    case NUMBER:
			if (length < 0) {
			    obj = YoixObject.newInt(0);
			    obj.setModeBits(ANYMINOR);
			} else VM.abort(BADDECLARATION, name);
			break;

		    case OBJECT:
			if (length < 0) {
			    obj = YoixObject.newNull();
			    obj.setModeBits(ANYMAJOR|RWX);
			} else VM.abort(BADDECLARATION, name);
			break;

		    case POINTER:
			if (length < 0) {
			    obj = YoixObject.newNullPointer();
			    obj.setModeBits(ANYMINOR);
			} else VM.abort(BADDECLARATION, name);
			break;

		    case STREAM:
			if (length < 0)
			    obj = YoixObject.newStream();
			else VM.abort(BADDECLARATION, name);
			break;

		    case STRING:
			obj = YoixObject.newString(length);
			break;

		    default:
			VM.abort(UNIMPLEMENTED, name);
			break;
		}
	    } else if (type.isPointer() && length < 0) {
		obj = VM.getTypeTemplate(name);
		//
		// Confusing - this needs work!!!
		//
		if ((minor = obj.getInt(N_MINOR, YOIX_EOF)) != YOIX_EOF) {
		    major = obj.getInt(N_MAJOR, YOIX_EOF);
		    classname = obj.getString(N_CLASSNAME, null);
		    if (ival == null || ival.isNull() || ival.getInt(N_MINOR, YOIX_EOF) == minor) {
			if (ival != null) {
			    if (ival.isNull())
				obj = YoixObject.newNull(obj);
			    else obj = (YoixObject)ival.clone();
			    ival = YoixObject.newEmpty();
			} else obj = YoixObject.newNull(obj);
			minor = YOIX_EOF;
			major = YOIX_EOF;
		    }
		}
	    } else if (type.isCallable() && type.notNull()) {
		if (ival == null || ival.isEmpty() || ival.isDictionary()) {
		    argv = new YoixObject[] {
			YoixObject.newInt(length),
			YoixObject.newInt(limit)
		    };
		    obj = type.call(argv, null);
		    obj = obj.resolveClone();
		    obj.setTypename(type.getTypename());
		    force = (length > 0);
		    limit = -2;
		} else VM.abort(BADDECLARATION, name);
	    } else VM.abort(BADDECLARATION, name);

	    if (limit != -2) {
		obj.setGrowable(true);
		obj.setGrowto(limit);
	    }

	    obj = initialize(obj, ival, tags, force);

	    //
	    // Probably belongs somewhere else - all the N_MINOR stuff
	    // was added very quickly so dictionaries that would normally
	    // be assigned to dval could instead be used as arguments to
	    // an appropriate pseudo-constructor. Not certain it's needed,
	    // but I suspect it's the best approach for objects that we
	    // will use to represent AWT Components.
	    //
	    if (major != YOIX_EOF && minor != YOIX_EOF) {
		if ((obj = YoixObject.newPointer(major, -1, obj, classname)) == null)
		    VM.abort(BADDECLARATION, name);
	    }
	} else VM.abort(BADTYPENAME, name);

	return(obj);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static Rectangle2D
    getBoundingBox(double x0, double y0, double width, double height, YoixBodyMatrix matrix) {

	Rectangle2D  rect;
	double       coords[];
	double       x1;
	double       y1;
	int          n;

	if (width > 0 || height > 0) {
	    if (matrix != null) {
		coords = matrix.itransform(
	    	    new double[] {
			x0, y0,
			x0 + width, y0,
			x0 + width, y0 + height,
			x0, y0 + height
		    }
		);
		x0 = x1 = coords[0];
		y0 = y1 = coords[1];
		for (n = 2; n < coords.length; n += 2) {
		    if (coords[n] < x0)
			x0 = coords[n];
		    else if (coords[n] > x1)
			x1 = coords[n];
		}
		for (n = 3; n < coords.length; n += 2) {
		    if (coords[n] < y0)
			y0 = coords[n];
		    else if (coords[n] > y1)
			y1 = coords[n];
		}
		rect = new Rectangle2D.Double(x0, y0, x1 - x0, y1 - y0);
	    } else rect = new Rectangle2D.Double(x0, y0, width, height);
	} else rect = new Rectangle2D.Double();

	return(rect);
    }


    private static YoixObject
    initialize(YoixObject obj, YoixObject ival, YoixObject tags, boolean force) {

	YoixObject  lval;
	YoixObject  tmp;
	String      name;
	String      str;
	int         length;
	int         offset;
	int         count;
	int         index;
	int         mode;
	int         m;
	int         n;

	//
	// Important and confusing code that needs attention!!
	//

	if (ival != null && ival.notEmpty()) {
	    if (ival.notNullPointer() && obj.notNullPointer()) {
		if (obj.getTypename() != ival.getTypename() || obj.getTypename() == null || force) {
		    if ((obj.mode() & ANYMAJOR) == 0) {
			switch (obj.minor()) {
			    case ARRAY:
				count = obj.capacity(ival.sizeof());
				offset = ival.offset();
				for (n = 0; n < count; n++) {
				    index = n + offset;
				    if (ival.defined(index)) {
					lval = YoixObject.newLvalue(obj, n);
					initializeLvalue(lval, ival, tags, index);
				    }
				}
				break;

			    case DICTIONARY:
				if (ival.compound()) {
				    count = ival.length();
				    for (n = 0; n < count; n++) {
					if (ival.defined(n)) {
					    if ((offset = obj.reserve(ival.name(n))) != -1) {
						lval = YoixObject.newLvalue(obj, offset);
						initializeLvalue(lval, ival, tags, n);
					    } else VM.abort(BADDECLARATION, ival.name(n));
					}
				    }
				} else {
				    //
				    // A recent addition that will affect existing
				    // code that use non-dictionary initializers.
				    // Old code went through the other loop, which
				    // means keys were just the indices converted to
				    // strings. Seems better, but it needs some work.
				    // Not convinced by name generation when ival is
				    // a string???
				    //
				    count = ival.length() - 1;
				    for (n = ival.offset(); n < count; n += 2) {
					if (ival.defined(n)) {
					    tmp = ival.get(n, false);
					    if (tmp.isString() == false) {
						if (ival.isString())
						    name = String.valueOf((char)tmp.intValue());
						else name = tmp.toString().trim();
					    } else name = tmp.stringValue(false);
					    if ((offset = obj.reserve(name)) != -1) {
						if (ival.defined(n+1)) {
						    lval = YoixObject.newLvalue(obj, offset);
						    initializeLvalue(lval, ival, tags, n+1);
						}
					    } else VM.abort(BADDECLARATION, name);
					}
				    }
				}
				break;

			    case STRING:
				count = obj.capacity(ival.sizeof());
				if (count > 0) {
				    offset = ival.offset();
				    if (ival.isString() == false) {
					count = obj.sizeof();
					for (n = 0, m = 0; m < count; n++, m++) {
					    index = n + offset;
					    if (ival.defined(index)) {
						tmp = ival.get(n, false);
						if (tmp.isString()) {
						    str = tmp.stringValue();
						    length = Math.min(str.length(), count - m);
						    obj.overlay(str, m + obj.offset(), length);
						    m += length - 1;
						} else obj.put(m, ival.get(index, true), false);
					    }
					}
				    } else obj.overlay(ival.stringValue(), obj.offset(), count);
				}
				break;

			    default:
				obj = YoixObject.cast(ival, obj, false);
				break;
			}
		    } else initializeObject(obj, ival, null);
		} else obj = YoixObject.cast(ival, obj, false);
	    } else obj = YoixObject.cast(ival, obj, false);
	}

	return(obj);
    }


    private static void
    initializeLvalue(YoixObject lval, YoixObject ival, YoixObject tags, int index) {

	YoixObject  obj;
	YoixObject  tag;

	ival = ival.get(index, true);
	if (lval.defined()) {
	    obj = lval.get();
	    tag = (tags != null) ? tags.getObject(index) : null;
	    if (obj.notNullPointer() && ival.notNullPointer()) {
		if ((ival = initialize(obj, ival, tags, false)) != obj)
		    initializeObject(lval, ival, tag);
	    } else initializeObject(lval, ival, tag);
	} else lval.declare(ival);
    }


    private static void
    initializeObject(YoixObject lval, YoixObject ival, YoixObject tag) {

	YoixObject  dest;
	int         perm;

	//
	// Stores ival in the location referenced by lval, but tries to
	// preserve ival's access flags. If something goes wrong and we
	// catch a YoixError we also try to update location information
	// stored in the YoixError object using tag. Pushing and poping
	// tag, if it's not null, would also work.
	//

	try {
	    perm = ival.mode();
	    lval.put(ival);
	    dest = lval.get();
	    if (dest.canUnlock())
		dest.setAccess(perm);
	}
	catch(YoixError e) {
	    if (tag != null)
		e.setLocation(tag);
	    throw(e);
	}
    }


    private static double
    scaleFont(Font font) {

	double  scale;
	double  size;

	if (font != null) {
	    size = font.getSize();
	    if ((scale = VM.getFontMagnification()) > 0.0 && scale != 1.0)
		size = (size + 0.5)/scale;
	} else size = 0;

	return(size);
    }
}

