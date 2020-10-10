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
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.zip.*;

abstract
class YoixModuleString extends YoixModule

{

    //
    // Mechanism used to turn a YoixBodyString into a String depends
    // on the builtin. The strXXX() builtins are supposed to behave
    // like C functions that work with null terminated strings, so
    // you usually use something like
    //
    //		YoixMake.CString(obj);
    //
    // to get the corresponding Java String. Most others builtins can
    // use YoixObject.stringValue().
    //

    static String  $MODULENAME = M_STRING;

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       null,                 "52",                $LIST,      $RORO, $MODULENAME,
       "atof",               "1",                 $BUILTIN,   $LR_X, null,
       "atoh",               "-1",                $BUILTIN,   $LR_X, null,
       "btoh",               "1",                 $BUILTIN,   $LR_X, null,
       "atoi",               "-1",                $BUILTIN,   $LR_X, null,
       "compareTo",          "2",                 $BUILTIN,   $LR_X, null,
       "crc32",              "1",                 $BUILTIN,   $LR_X, null,
       "cstring",            "-1",                $BUILTIN,   $LR_X, null,
       "csvsplit",           "-1",                $BUILTIN,   $LR_X, null,
       "endsWith",           "2",                 $BUILTIN,   $LR_X, null,
       "fmt",                "-1",                $BUILTIN,   $LR_X, null,
       "htmlDecode",         "1",                 $BUILTIN,   $LR_X, null,
       "htmlEncode",         "-1",                $BUILTIN,   $LR_X, null,
       "htoa",               "-1",                $BUILTIN,   $LR_X, null,
       "htob",               "1",                 $BUILTIN,   $LR_X, null,
       "indexOf",            "2",                 $BUILTIN,   $LR_X, null,
       "lastIndexOf",        "2",                 $BUILTIN,   $LR_X, null,
       "linesplit",          "-1",                $BUILTIN,   $LR_X, null,
       "overlay",            "-2",                $BUILTIN,   $LR_X, null,
       "replace",            "3",                 $BUILTIN,   $LR_X, null,
       "startsWith",         "2",                 $BUILTIN,   $LR_X, null,
       "strcasecmp",         "2",                 $BUILTIN,   $LR_X, null,
       "strcat",             "2",                 $BUILTIN,   $LR_X, null,
       "strchr",             "2",                 $BUILTIN,   $LR_X, null,
       "strcmp",             "2",                 $BUILTIN,   $LR_X, null,
       "strcpy",             "2",                 $BUILTIN,   $LR_X, null,
       "strcspn",            "2",                 $BUILTIN,   $LR_X, null,
       "strdel",             "2",                 $BUILTIN,   $LR_X, null,
       "strdup",             "1",                 $BUILTIN,   $LR_X, null,
       "strfmt",             "-1",                $BUILTIN,   $LR_X, null,
       "strins",             "2",                 $BUILTIN,   $LR_X, null,
       "strjoin",            "-1",                $BUILTIN,   $LR_X, null,
       "strlen",             "1",                 $BUILTIN,   $LR_X, null,
       "strncasecmp",        "3",                 $BUILTIN,   $LR_X, null,
       "strncat",            "3",                 $BUILTIN,   $LR_X, null,
       "strncmp",            "3",                 $BUILTIN,   $LR_X, null,
       "strncpy",            "3",                 $BUILTIN,   $LR_X, null,
       "strpbrk",            "2",                 $BUILTIN,   $LR_X, null,
       "strrchr",            "2",                 $BUILTIN,   $LR_X, null,
       "strrstr",            "2",                 $BUILTIN,   $LR_X, null,
       "strsplit",           "-2",                $BUILTIN,   $LR_X, null,
       "strspn",             "2",                 $BUILTIN,   $LR_X, null,
       "strstr",             "2",                 $BUILTIN,   $LR_X, null,
       "strtod",             "2",                 $BUILTIN,   $LR_X, null,
       "strtok",             "2",                 $BUILTIN,   $LR_X, null,
       "strton",             "-2",                $BUILTIN,   $LR_X, null,
       "substring",          "-2",                $BUILTIN,   $LR_X, null,
       "toLowerCase",        "1",                 $BUILTIN,   $LR_X, null,
       "toUpperCase",        "1",                 $BUILTIN,   $LR_X, null,
       "trim",               "-1",                $BUILTIN,   $LR_X, null,
       "urlDecode",          "-1",                $BUILTIN,   $LR_X, null,
       "urlEncode",          "-1",                $BUILTIN,   $LR_X, null,
       "utf8len",            "1",                 $BUILTIN,   $LR_X, null,
    };

    private static final int  MARKER = '\uFFFF';

    ///////////////////////////////////
    //
    // YoixModuleString Methods
    //
    ///////////////////////////////////

    public static YoixObject
    atof(YoixObject arg[]) {

	String  str;
	double  value = 0;
	int     length;

	if (arg[0].isString() && arg[0].notNull()) {
	    str = YoixMake.CString(arg[0], 0).trim();
	    for (length = str.length(); length > 0; length--) {
		try {
		    value = Double.valueOf(str).doubleValue();
		    break;
		}
		catch(NumberFormatException e) {
		    str = str.substring(0, length - 1);
		}
	    }
	} else VM.badArgument(0);

	return(YoixObject.newDouble(value));
    }


    public static YoixObject
    atoh(YoixObject arg[]) {

	String  value = null;

	if (arg.length == 1 || arg.length == 2 || arg.length == 3) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg.length < 2 || arg[1].isArray() || arg[1].isNull()) {
		    if (arg.length < 3 || arg[2].isArray() || arg[2].isNull()) {
			value = YoixMisc.hexFromAscii(arg[0].stringValue());
			if (arg.length > 1) {
			    value = YoixMisc.pad(
				value,
				arg.length > 2 ? arg[1] : null,
				arg.length > 2 ? arg[2] : arg[1],
				2
			    );
			}
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(value));
    }


    public static YoixObject
    atoi(YoixObject arg[]) {

	int  value = 0;

	if (arg.length >= 1 && arg.length <= 3) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg.length <= 1 || arg[1].isNumber()) {
		    if (arg.length <= 2 || arg[2].isNumber()) {
			value = YoixMisc.atoi(
			    YoixMake.CString(arg[0], 0).trim(),
			    (arg.length > 1) ? arg[1].intValue() : 10,
			    (arg.length > 2) ? arg[2].intValue() : 0
			);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    btoh(YoixObject arg[]) {

	String  value = null;

	if (arg.length == 1) {
	    if (arg[0].isArray() && arg[0].notNull()) {
		if ((value = YoixMake.javaByteArrayString(arg[0])) == null)
		    VM.badArgumentValue(0);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(value));
    }


    public static YoixObject
    compareTo(YoixObject arg[]) {

	String  str1;
	String  str2;
	int     value = 0;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		str1 = arg[0].stringValue();
		str2 = arg[1].stringValue();
		value = str1.compareTo(str2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    crc32(YoixObject arg[]) {

	double   value = -1;
	CRC32    crc;

	if (arg[0].isString() && arg[0].notNull()) {
	    crc = new CRC32();
	    crc.update(YoixMake.javaByteArray(arg[0].stringValue()));
	    value = crc.getValue();
	} else VM.badArgument(0);

	return(YoixObject.newDouble(value));
    }


    public static YoixObject
    cstring(YoixObject arg[]) {

	StringBuffer  sb;
	String        cstr;
	String        str = null;
	String        value = null;
	char          chars[];
	int           n;

	if (arg.length == 1 || (arg.length == 2 && (arg[1].isNull() || arg[1].isEmptyString()))) {
	    if (arg[0].isString() && arg[0].notNull())
		value = ((YoixBodyString)(arg[0].body())).cstringValue(arg[0].offset());
	    else VM.badArgument(0);
	} else if (arg.length == 2) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg[1].isString()) { // we know it is notNull
		    chars = arg[0].stringValue().toCharArray();
		    str = arg[1].stringValue();
		    sb = new StringBuffer(chars.length);
		    synchronized(sb) {
			for (n = 0; n < chars.length; n++) {
			    cstr = YoixMake.javaString(chars[n]);
			    if (cstr.length() == 1 && str.indexOf(chars[n]) >= 0)
				cstr = YoixMake.javaString(chars[n], true);
			    sb.append(cstr);
			}
			value = sb.toString();
		    }
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(value));
    }


    public static YoixObject
    csvsplit(YoixObject arg[]) {

	StringBuffer  buffer = null;
	YoixObject    obj = null;
	YoixObject    yobj = null;
	ArrayList     list;
	boolean       fill;
	String        delim;
	String        source;
	int           argc;
	int           end;
	int           last;
	int           length;
	int           stop;
	int           next;
	int           skip;
	int           start;

	if (arg.length >= 1 && arg.length <= 3) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg.length == 1 || arg[1].isString() || arg[1].isNull()) {
		    if (arg.length <= 2 || arg[2].isInteger()) {
			argc = (arg.length == 3) ? arg[2].intValue() : 0;
			if (fill = (argc < 0)) {
			    argc = -argc;
			    list = new ArrayList(argc);
			} else list = new ArrayList();
			end = 0;
			next = 0;
			last = argc - 1;
			source = YoixMake.CString(arg[0], 0);
			if (arg.length == 1 || arg[1].notNull()) {
			    if (arg.length == 1)
				delim = ",";
			    else delim = YoixMake.CString(arg[1], 0);
			    skip = delim.length();
			    length = source.length();
			    stop = length + skip;
			    for (start = 0; start < stop; start = end + skip) {
				if (last < 0 || next < last) {
				    if (skip > 0) {
					if (start < length && source.charAt(start) == '"') {
					    if (buffer == null)
						buffer = new StringBuffer();
					    else buffer.setLength(0);
					    start++;
					    while(start < length) {
						if ((end = source.indexOf('"', start)) < start) {
						    end = length;
						    buffer.append(source.substring(start, end));
						    break;
						} else {
						    if ((end+1) < length && source.charAt(end+1) == '"') {
							buffer.append(source.substring(start, end+1));
							start = end + 2;
						    } else {
							buffer.append(source.substring(start, end));
							break;
						    }
						}
					    }
					    list.add(
						next++,
						YoixObject.newString(buffer.toString())
					    );
					    start = end;
					    if ((end = source.indexOf(delim, start)) < start)
						end = length;
					    continue;
					}
					if ((end = source.indexOf(delim, start)) < start)
					    end = length;
				    } else end = start + 1;
				} else end = length;
				list.add(
				    next++,
				    YoixObject.newString(source.substring(start, end))
				);
			    }
			} else list.add(next++, YoixObject.newString(source));
			if (fill) {
			    while (next <= last)
				list.add(next++, YoixObject.newString());
			}
			obj = YoixMisc.copyIntoArray(list);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj);
    }


    public static YoixObject
    endsWith(YoixObject arg[]) {

	boolean  result = false;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull())
		result = arg[0].stringValue().endsWith(arg[1].stringValue());
	    else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    fmt(YoixObject arg[]) {

	String  str = null;
	int     goal = -1;
	int     maximum = -1;
	boolean forced = false;

	//
	// Each line attempts to be about "goal" long, but no longer than
	// the maximum (goal+10 by default) unless there is no break point
	// in the string or forced is non-zero. Breaks only occur at spaces
	// (' ') and newlines ('\n'). Carriage returns are skipped and removed
	// (and only put back if NL contains one). A non-zero "squeeze" will
	// squeeze multiple spaces or newlines between words to one. Spaces at
	// the beginning of a line are taken as indentation and all following
	// lines are equally indented until two newlines in a row (or \r\n
	// combos in a row) are encountered.
	//

	if (arg.length >= 1 && arg.length <= 3) {
	    if (arg[0].isString()) {
		if (arg.length < 2 || arg[1].isNumber() || arg[1].isArray()) {
		    if (arg.length < 3 || arg[2].isNumber()) {
			if (arg.length > 1) {
			    if (arg[1].isArray()) {
				goal = arg[1].getInt(0, -1);
				maximum = arg[1].getInt(1, -1);
				forced = arg[1].getBoolean(2, false);
			    } else goal = arg[1].intValue();
			}
			if (goal <= 0)
			    goal = 65;
			if (maximum <= 0)
			    maximum = goal + 10;
			str = YoixMisc.fmt(
			    arg[0].stringValue(),
			    goal,
			    Math.max(goal, maximum),
			    forced,
			    (arg.length > 2) ? arg[2].booleanValue() : false
			);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(str));
    }


    public static YoixObject
    htoa(YoixObject arg[]) {

	String  value = null;
	char    buf[];

	//
	// The second argument is new (and undocumented) and controls
	// whether we stop at a null byte or not. In addition, when a
	// second argument is supplied htoa tries to grab the internal
	// buffer used by the string, which should improve perfomance
	// when the grab succeeds.
	//

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg.length == 1 || arg[1].isInteger()) {
		    if (arg.length == 2) {
			if (arg[0].offset() == 0) {
			    if ((buf = arg[0].toCharArray(true, null)) == null)
				buf = arg[0].toCharArray();
			} else buf = arg[0].toCharArray();
			value = YoixMisc.hexToAscii(buf, arg[1].booleanValue());
		    } else value = YoixMisc.hexToAscii(YoixMake.CString(arg[0], 0));
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(value));
    }


    public static YoixObject
    htob(YoixObject arg[]) {

	YoixObject  value = null;
	byte        bytearray[];

	if (arg.length == 1) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if ((bytearray = YoixMisc.hexStringToBytes(arg[0].stringValue())) != null) {
		    if ((value = YoixMake.yoixByteArray(bytearray)) == null)
			VM.badArgumentValue(0);
		} else VM.badArgumentValue(0);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(value == null ? YoixObject.newArray() : value);
    }


    public static YoixObject
    htmlDecode(YoixObject arg[]) {

	String  value = null;

	if (arg[0].isString() && arg[0].notNull())
	    value = YoixMisc.htmlToAscii(arg[0].stringValue());
	else VM.badArgument(0);

	return(YoixObject.newString(value));
    }


    public static YoixObject
    htmlEncode(YoixObject arg[]) {

	String  value = null;
	String  extra;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg.length == 1 || arg[1].isString() || arg[1].isNull()) {
		    extra = (arg.length == 1 || arg[1].sizeof() == 0) ? null : arg[1].stringValue();
		    value = YoixMisc.htmlFromAscii(arg[0].stringValue(), extra);
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(value));
    }


    public static YoixObject
    indexOf(YoixObject arg[]) {

	int  value = -1;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isInteger())
		value = arg[0].stringValue().indexOf(arg[1].intValue());
	    else if (arg[1].isString() && arg[1].notNull())
		value = arg[0].stringValue().indexOf(arg[1].stringValue());
	    else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    lastIndexOf(YoixObject arg[]) {

	int  value = -1;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isInteger())
		value = arg[0].stringValue().lastIndexOf(arg[1].intValue());
	    else if (arg[1].isString() && arg[1].notNull())
		value = arg[0].stringValue().lastIndexOf(arg[1].stringValue());
	    else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    linesplit(YoixObject arg[]) {

	StringTokenizer  tok;
	YoixObject       lines = null;
	String           cstring;
	String           token;
	String           tokens = "\r\n";
	boolean          returnDelims = false;
	int              idx;
	int              beg;
	char[]           chars;

	if (arg.length < 3) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg.length == 2) {
		    if (arg[1].isInteger())
			returnDelims = arg[1].booleanValue();
		    else VM.badArgument(1);
		}
		cstring = YoixMake.CString(arg[0]);
		if (returnDelims) {
		    lines = YoixObject.newArray(0, -1);
		    chars = cstring.toCharArray();
		    beg = idx = 0;
		    for (int n = 0; n < chars.length; n++) {
			if (tokens.indexOf(chars[n]) >= 0) {
			    if (n == beg)
				lines.putString(idx++, "");
			    else lines.putString(idx++, cstring.substring(beg, n));
			    if ((n+1) < chars.length) {
				if (tokens.indexOf(chars[n+1]) >= 0) {
				    // consider end of line marked by \n, \r, \r\n or even \n\r
				    if (chars[n] != chars[n+1])
					n++;
				}
			    }
			    beg = n + 1;
			}
		    }
		    // if string ends with a delim, we do *not* append a trailing empty line
		    if (beg < chars.length)
			lines.putString(idx++, cstring.substring(beg));
		    lines.setGrowable(false);
		} else {
		    tok = new StringTokenizer(cstring, tokens, false);
		    lines = YoixObject.newArray(tok.countTokens());
		    idx = 0;
		    while (tok.hasMoreTokens())
			lines.putString(idx++, tok.nextToken());
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(lines);
    }


    public static YoixObject
    overlay(YoixObject arg[]) {

	String  src;
	int     offset;
	int     count = -1;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg[1].isString() && arg[1].notNull()) {
		    offset = arg[0].offset();
		    src = arg[1].stringValue();
		    if (arg.length == 3) {
			if (arg[2].isInteger())
			    count = arg[2].intValue();
			else VM.badArgument(2);
		    } else count = src.length();
		    arg[0].overlay(src, offset, count);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(arg[0]);
    }


    public static YoixObject
    replace(YoixObject arg[]) {

	String  value = null;
	char    ch1;
	char    ch2;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isInteger()) {
		if (arg[2].isInteger()) {
		    ch1 = (char)arg[1].intValue();
		    ch2 = (char)arg[2].intValue();
		    value = arg[0].stringValue().replace(ch1, ch2);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newString(value));
    }


    public static YoixObject
    startsWith(YoixObject arg[]) {

	boolean  result = false;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull())
		result = arg[0].stringValue().startsWith(arg[1].stringValue());
	    else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    strcasecmp(YoixObject arg[]) {

	String  str1;
	String  str2;
	int     value = 0;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		str1 = YoixMake.CString(arg[0], 0).toLowerCase();
		str2 = YoixMake.CString(arg[1], 0).toLowerCase();
		value = str1.compareTo(str2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    strcat(YoixObject arg[]) {

	String  src;
	int     start;
	int     offset;
	int     length;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		offset = arg[0].offset();
		length = arg[0].length();
		if ((start = arg[0].indexOf('\0', offset)) == -1)
		    start = length;
		else start += offset;
		src = YoixMake.CString(arg[1], length - start);
		arg[0].overlay(src, start, src.length());
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(arg[0]);
    }


    public static YoixObject
    strchr(YoixObject arg[]) {

	YoixObject  obj = null;
	String      str;
	int         index;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isInteger()) {
		str = YoixMake.CString(arg[0], arg[0].sizeof());
		if ((index = str.indexOf(arg[1].intValue())) != -1)
		    obj = YoixObject.newLvalue(arg[0], index);
		else obj = YoixObject.newString();
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(obj);
    }


    public static YoixObject
    strcmp(YoixObject arg[]) {

	String  str1;
	String  str2;
	int     value = 0;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		str1 = YoixMake.CString(arg[0], 0);
		str2 = YoixMake.CString(arg[1], 0);
		value = str1.compareTo(str2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    strcpy(YoixObject arg[]) {

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull())
		arg[0].overlay(YoixMake.CString(arg[1], arg[0].sizeof()));
	    else VM.badArgument(1);
	} else VM.badArgument(0);

	return(arg[0]);
    }


    public static YoixObject
    strcspn(YoixObject arg[]) {

	String  str1;
	String  str2;
	int     value = 0;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		str1 = YoixMake.CString(arg[0], 0);
		str2 = YoixMake.CString(arg[1], 0);
		for (; value < str1.length(); value++) {
		    if (str2.indexOf(str1.charAt(value)) != -1)
			break;
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    strdel(YoixObject arg[]) {

	String  src;
	int     count;
	int     len;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isInteger()) {
		if ((len = arg[1].intValue()) > 0) {
		    src = YoixMake.CString(arg[0], 0);
		    if (len >= src.length())
			src = "";
		    else src = src.substring(len);
		    if ((count = arg[0].sizeof()) >= 0)
			arg[0].overlay(src, arg[0].offset(), count);
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(arg[0]);
    }


    public static YoixObject
    strdup(YoixObject arg[]) {

	String  value = null;

	if (arg[0].isString() && arg[0].notNull())
	    value = YoixMake.CString(arg[0], 0);
	else VM.badArgument(0);

	return(YoixObject.newString(value));
    }


    public static YoixObject
    strfmt(YoixObject arg[]) {

	String  value = null;

	if (arg[0].isString() && arg[0].notNull())
	    value = YoixMiscPrintf.print(arg, 0);
	else VM.badArgument(0);

	return(YoixObject.newString(value));
    }


    public static YoixObject
    strins(YoixObject arg[]) {

	String  src;
	String  str;
	int     length;
	int     offset;
	int     start;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		str = YoixMake.CString(arg[0], arg[0].sizeof());
		src = YoixMake.CString(arg[1], arg[1].sizeof());
		offset = arg[0].offset();
		length = arg[0].length();
		if ((start = arg[0].indexOf('\0', offset)) == -1)
		    start = length;
		else start += offset;
		arg[0].overlay(src, offset, src.length());
		arg[0].overlay(str, offset + src.length(), str.length());
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(arg[0]);
    }


    public static YoixObject
    strjoin(YoixObject arg[]) {

	StringBuffer  buf = null;
	YoixObject    src;
	YoixObject    element;
	boolean       undefined;
	String        value = null;
	String	      delim;
	String        sep;
	int	      n;

	if (arg.length >= 1 && arg.length <= 3) {
	    if (arg[0].isArray() || arg[0].isNull()) {
		if (arg.length == 1 || arg[1].isString() || arg[1].isNull()) {
		    if (arg.length <= 2 || arg[2].isNumber()) {
			if (arg[0].notNull()) {
			    src = arg[0];
			    delim = (arg.length > 1 && arg[1].notNull()) ? YoixMake.CString(arg[1], 0) : "";
			    undefined = (arg.length > 2) ? arg[2].booleanValue() : false;
			    sep = "";
			    buf = new StringBuffer("");
			    if (delim.length() > 0) {
				for (n = src.offset(); n < src.length(); n++) {
				    if ((element = src.getObject(n)) != null) {
					buf.append(sep);
					buf.append(YoixMake.CString(element, 0));
					sep = delim;
				    } else if (undefined) {
					buf.append(sep);
					sep = delim;
				    }
				}
			    } else {
				for (n = src.offset(); n < src.length(); n++) {
				    if ((element = src.getObject(n)) != null)
					buf.append(YoixMake.CString(element, 0));
				}
			    }
			    value = new String(buf);
			}
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(value));
    }


    public static YoixObject
    strlen(YoixObject arg[]) {

	int  offset;
	int  value = -1;

	if (arg[0].isString() && arg[0].notNull()) {
	    if ((offset = arg[0].offset()) >= 0) {
		if ((value = arg[0].indexOf('\0', offset)) == -1)
		    value = Math.max(-1, arg[0].sizeof());
	    } else value = -1;
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    strncasecmp(YoixObject arg[]) {

	String  str1;
	String  str2;
	int     count;
	int     value = 0;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		if (arg[2].isInteger()) {
		    str1 = YoixMake.CString(arg[0]).toLowerCase();
		    str2 = YoixMake.CString(arg[1]).toLowerCase();
		    if ((count = arg[2].intValue()) > 0) {
			str1 = str1.substring(0, Math.min(str1.length(), count));
			str2 = str2.substring(0, Math.min(str2.length(), count));
			value = str1.compareTo(str2);
		    } else value = 0;
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    strncat(YoixObject arg[]) {

	String  src;
	int     count;
	int     offset;
	int     start;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		if (arg[2].isInteger()) {
		    if ((count = arg[2].intValue()) > 0) {
			offset = arg[0].offset();
			if ((start = arg[0].indexOf('\0', offset)) == -1)
			    start = arg[0].length();
			else start += offset;
			src = YoixMake.CString(arg[1], count);
			arg[0].overlay(src, start, count);
		    }
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(arg[0]);
    }


    public static YoixObject
    strncmp(YoixObject arg[]) {

	String  str1;
	String  str2;
	int     count;
	int     value = 0;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		if (arg[2].isInteger()) {
		    str1 = YoixMake.CString(arg[0]);
		    str2 = YoixMake.CString(arg[1]);
		    if ((count = arg[2].intValue()) > 0) {
			str1 = str1.substring(0, Math.min(str1.length(), count));
			str2 = str2.substring(0, Math.min(str2.length(), count));
			value = str1.compareTo(str2);
		    } else value = 0;
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    strncpy(YoixObject arg[]) {

	int  count;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		if (arg[2].isInteger()) {
		    if ((count = arg[2].intValue()) > 0) {
			arg[0].overlay(
			    YoixMake.CString(arg[1]),
			    arg[0].offset(),
			    count
			);
		    }
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(arg[0]);
    }


    public static YoixObject
    strpbrk(YoixObject arg[]) {

	String  str1;
	String  str2;
	int     index = -1;
	int     n;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		str1 = YoixMake.CString(arg[0], 0);
		str2 = YoixMake.CString(arg[1], 0);
		for (n = 0; n < str1.length(); n++) {
		    if (str2.indexOf(str1.charAt(n)) != -1) {
			index = n;
			break;
		    }
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(index != -1
	    ? YoixObject.newLvalue(arg[0], index)
	    : YoixObject.newString()
	);
    }


    public static YoixObject
    strrchr(YoixObject arg[]) {

	YoixObject  obj = null;
	String      str;
	int         index;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isInteger()) {
		str = YoixMake.CString(arg[0], arg[0].sizeof());
		if ((index = str.lastIndexOf(arg[1].intValue())) != -1)
		    obj = YoixObject.newLvalue(arg[0], index);
		else obj = YoixObject.newString();
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(obj);
    }


    public static YoixObject
    strrstr(YoixObject arg[]) {

	YoixObject  obj = null;
	String      str1;
	String      str2;
	int         index;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		str1 = YoixMake.CString(arg[0], 0);
		str2 = YoixMake.CString(arg[1], 0);
		if ((index = str1.lastIndexOf(str2)) != -1)
		    obj = YoixObject.newLvalue(arg[0], index);
		else obj = YoixObject.newString();
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(obj);
    }


    public static YoixObject
    strsplit(YoixObject arg[]) {

	YoixObject  obj = null;
	ArrayList   list;
	boolean     fill;
	String      source;
	int         limit;
	int         last;
	int         next;

	//
	// NOTE - old implementation included an undocumented kludge that
	// was explicitly for the JTable outputfilter field. Added a long
	// time ago, undoubtedly to temporarily support an old application
	// that assumed that reading JTable's outputfilter field returned
	// a string rather than an array of two strings. Code supporting
	// the kludge was removed on 12/19/06 because we support an array
	// of dictionaries as the second argument.
	//

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg[1].isInteger())
		    arg[1] = YoixObject.newString(new String(new char[] {(char)(arg[1].intValue()&0xFF)}));
		if (arg[1].isString() || arg[1].isDictionary() || arg[1].isArray() || arg[1].isNull()) {
		    if (arg.length == 2 || arg[2].isInteger()) {
			limit = (arg.length == 3) ? arg[2].intValue() : 0;
			if (fill = (limit < 0)) {
			    limit = -limit;
			    list = new ArrayList(limit);
			} else list = new ArrayList();
			last = limit - 1;
			source = YoixMake.CString(arg[0], 0);
			if (arg[1].notNull()) {
			    if (arg[1].isString())
				YoixMisc.split(source, YoixMake.CString(arg[1], 0), last, list);
			    else YoixMisc.split(source, arg[1], list);
			} else list.add(YoixObject.newString(source));
			if (fill) {
			    next = list.size();
			    while (next <= last)
				list.add(next++, YoixObject.newString());
			}
			obj = YoixMisc.copyIntoArray(list);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else if (arg[0].isNull())
		obj = YoixObject.newString();
	    else VM.badArgument(0);
	} else VM.badCall();

	return(obj);
    }


    public static YoixObject
    strspn(YoixObject arg[]) {

	String  str1;
	String  str2;
	int     value = 0;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		str1 = YoixMake.CString(arg[0], 0);
		str2 = YoixMake.CString(arg[1], 0);
		for (; value < str1.length(); value++) {
		    if (str2.indexOf(str1.charAt(value)) == -1)
			break;
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    strstr(YoixObject arg[]) {

	YoixObject  obj = null;
	String      str1;
	String      str2;
	int         index;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		str1 = YoixMake.CString(arg[0], 0);
		str2 = YoixMake.CString(arg[1], 0);
		if ((index = str1.indexOf(str2)) != -1)
		    obj = YoixObject.newLvalue(arg[0], index);
		else obj = YoixObject.newString();
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(obj);
    }


    public static YoixObject
    strtod(YoixObject arg[]) {

	String  str;
	double  value = 0;
	int     length;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isNull() || arg[1].isStringPointer()) {
		str = YoixMake.CString(arg[0], 0).trim();
		for (length = str.length(); length > 0; length--) {
		    try {
			value = Double.valueOf(str).doubleValue();
			break;
		    }
		    catch(NumberFormatException e) {
			str = str.substring(0, length - 1);
		    }
		}
		if (arg[1].notNull()) {
		    //
		    // Taking substrings can uncover trailing whitespace
		    // that Double.valueOf() accepts, so adjust length.
		    //
		    if (length > 0) {
			while (Character.isWhitespace(str.charAt(length-1)))
			    length--;
		    }
		    if (length > 0)
			length += arg[0].stringValue().indexOf(str);
		    arg[1].put(YoixObject.newLvalue(arg[0], arg[0].offset() + length));
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newDouble(value));
    }


    public static YoixObject
    strtok(YoixObject arg[]) {

	YoixObject  obj = null;
	boolean     growable;
	String      delim;
	String      source;
	int         end;
	int         growto;
	int         index;
	int         length;
	int         market;
	int         n;
	int         nullet;
	int         offset;
	int         start;

	//
	// Works by progressively trashing its first argument and by
	// initially appending a marker after the first null. Lack of
	// a marker lets the function know it is the first call. Done
	// when the marker is in offset 0. This version of strtok()
	// allows empty strings to be delimited.
	//

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() || arg[1].isNull()) {
		delim = YoixMake.CString(arg[1], 0);
		nullet = arg[0].indexOf('\0');
		market = arg[0].indexOf(MARKER);
		if (market < 0) {
		    // first time through
		    source = YoixMake.CString(arg[0], 0);
		    end = length = source.length();
		    // cheating by accessing directly
		    growable = ((YoixBodyString)arg[0].body()).growable;
		    growto = ((YoixBodyString)arg[0].body()).growto;
		    ((YoixBodyString)arg[0].body()).growable = true;
		    ((YoixBodyString)arg[0].body()).growto = -1;
		    if (length > 0 && delim.length() > 0) {
			for (n = 0; n < delim.length(); n++) {
			    if ((index = source.indexOf(delim.charAt(n))) != -1) {
				if (index < end)
				    end = index;
			    }
			}
		    }
		    obj = YoixObject.newString(source.substring(0, end));
		    if (end < length) {
			arg[0].overlay(source.substring(end+1), 0, length - end - 1);
			arg[0].put(length - end - 1, YoixObject.newInt('\0'), false);
		    }
		    arg[0].put(length - end, YoixObject.newInt(MARKER), false);
		    ((YoixBodyString)arg[0].body()).growable = growable;
		    ((YoixBodyString)arg[0].body()).growto = growto;
		} else if (nullet < 0 || nullet > market) {
		    obj = YoixObject.newString();
		} else {
		    // deja vu
		    source = YoixMake.CString(arg[0], 0);
		    end = length = source.length();
		    if (length > 0 && delim.length() > 0) {
			for (n = 0; n < delim.length(); n++) {
			    if ((index = source.indexOf(delim.charAt(n))) != -1) {
				if (index < end)
				    end = index;
			    }
			}
		    }
		    obj = YoixObject.newString(source.substring(0, end));
		    if (end < length) {
			arg[0].overlay(source.substring(end+1), 0, length - end - 1);
			arg[0].put(length - end - 1, YoixObject.newInt('\0'), false);
		    }
		    arg[0].put(length - end, YoixObject.newInt(MARKER), false);
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(obj);
    }


    public static YoixObject
    strton(YoixObject arg[]) {

	String  source;
	String  str;
	int     consumed[];
	int     value = 0;

	if (arg.length >= 2 && arg.length <= 4) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg[1].isNull() || arg[1].isStringPointer()) {
		    if (arg.length <= 2 || arg[2].isNumber()) {
			if (arg.length <= 3 || arg[3].isNumber()) {
			    source = YoixMake.CString(arg[0], 0);
			    str = YoixMisc.trimWhiteSpace(source, true, false);
			    consumed = new int[] {source.length() - str.length()};
			    value = YoixMisc.atoi(
				str,
				(arg.length > 2) ? arg[2].intValue() : 10,
				(arg.length > 3) ? arg[3].intValue() : 0,
				consumed
			    );
			    if (arg[1].notNull())
				arg[1].put(YoixObject.newLvalue(arg[0], arg[0].offset() + consumed[0]));
			} else VM.badArgument(3);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    substring(YoixObject arg[]) {

	String  value = null;
	String  str;
	int     end;
	int     start;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg[1].isInteger()) {
		    str = arg[0].stringValue();
		    start = arg[1].intValue();
		    if (start >= 0 && start <= str.length()) {
			if (arg.length == 3) {
			    if (arg[2].isInteger()) {
				end = arg[2].intValue();
				if (end >= 0 && end <= str.length())
				    value = str.substring(start, end);
				else VM.badArgumentValue(2);
			    } else VM.badArgument(2);
			} else value = str.substring(start);
		    } else VM.badArgumentValue(1);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(value));
    }


    public static YoixObject
    toLowerCase(YoixObject arg[]) {

	String  value = null;

	if (arg[0].isString() && arg[0].notNull())
	    value = arg[0].stringValue().toLowerCase();
	else VM.badArgument(0);

	return(YoixObject.newString(value));
    }


    public static YoixObject
    toUpperCase(YoixObject arg[]) {

	String  value = null;

	if (arg[0].isString() && arg[0].notNull())
	    value = arg[0].stringValue().toUpperCase();
	else VM.badArgument(0);

	return(YoixObject.newString(value));
    }


    public static YoixObject
    trim(YoixObject arg[]) {

	String  delim;
	String  str;
	String  value = null;

	if (arg.length >= 1 && arg.length <= 3) {
	    if (arg[0].isString() && arg[0].notNull()) {
		str = arg[0].stringValue();
		if (arg.length > 1) {
		    if (arg[1].isString() || arg[1].isNull()) {
			delim = arg[1].stringValue();
			if (arg.length == 3) {
			    if (arg[2].isString() || arg[2].isNull())
				value = YoixMisc.trim(str, delim, arg[2].stringValue());
			    else VM.badArgument(2);
			} else value = YoixMisc.trim(str, delim);
		    } else VM.badArgument(1);
		} else value = str.trim();
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(value));
    }


    public static YoixObject
    urlDecode(YoixObject arg[]) {

	boolean  ietf = false;
	String   value = null;
	char     buf[];

	//
	// The third argument is new (and undocumented) and controls
	// whether we stop at a null byte or not. In addition, when
	// there's a third argument we also try to grab the internal
	// buffer used by the string, which should improve perfomance
	// when the grab succeeds.
	//

	if (arg.length >= 1 && arg.length <= 3) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg.length <= 1 || arg[1].isNumber()) {
		    ietf = (arg.length > 1) ? arg[1].booleanValue() : false;
		    if (arg.length <= 2 || arg[2].isNumber()) {
			if (arg.length == 3) {
			    if (arg[0].offset() == 0) {
				if ((buf = arg[0].toCharArray(true, null)) == null)
				    buf = arg[0].toCharArray();
			    } else buf = arg[0].toCharArray();
			    value = YoixMisc.urlToAscii(buf, ietf, arg[2].booleanValue());
			} else value = YoixMisc.urlToAscii(arg[0].toCharArray(), ietf, true);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(value));
    }


    public static YoixObject
    urlEncode(YoixObject arg[]) {

	String  value = null;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg.length <= 1 || arg[1].isNumber()) {
		    value = YoixMisc.urlFromAscii(
			arg[0].stringValue(),
			arg.length > 1 ? arg[1].booleanValue() : false
		    );
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(value));
    }


    public static YoixObject
    utf8len(YoixObject arg[]) {

	int  value = -1;

	if (arg[0].isString() && arg[0].notNull())
	    value = YoixConverter.utf8Length(arg[0].stringValue());
	else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }
}

