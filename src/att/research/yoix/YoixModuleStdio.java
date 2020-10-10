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
import java.io.*;

abstract
class YoixModuleStdio extends YoixModule

    implements YoixConstantsStream

{

    static String  $MODULENAME = M_STDIO;

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       null,                 "29",                $LIST,      $RORO, $MODULENAME,
       "fclose",             "1",                 $BUILTIN,   $LR_X, null,
       "fflush",             "1",                 $BUILTIN,   $LR_X, null,
       "fgetc",              "1",                 $BUILTIN,   $LR_X, null,
       "fgets",              "3",                 $BUILTIN,   $LR_X, null,
       "fopen",              "-2",                $BUILTIN,   $LR_X, null,
       "fprintf",            "-2",                $BUILTIN,   $LR_X, null,
       "fputc",              "2",                 $BUILTIN,   $LR_X, null,
       "fputs",              "2",                 $BUILTIN,   $LR_X, null,
       "freopen",            "3",                 $BUILTIN,   $LR_X, null,
       "fscanf",             "-2",                $BUILTIN,   $LR_X, null,
       "fseek",              "3",                 $BUILTIN,   $LR_X, null,
       "ftell",              "1",                 $BUILTIN,   $LR_X, null,
       "getc",               "1",                 $BUILTIN,   $LR_X, null,
       "getchar",            "0",                 $BUILTIN,   $LR_X, null,
       "gets",               "1",                 $BUILTIN,   $LR_X, null,
       "mktemp",             "1",                 $BUILTIN,   $LR_X, null,
       "printf",             "-1",                $BUILTIN,   $LR_X, null,
       "putc",               "2",                 $BUILTIN,   $LR_X, null,
       "putchar",            "1",                 $BUILTIN,   $LR_X, null,
       "puts",               "1",                 $BUILTIN,   $LR_X, null,
       "rewind",             "1",                 $BUILTIN,   $LR_X, null,
       "scanf",              "-1",                $BUILTIN,   $LR_X, null,
       "setbuf",             "2",                 $BUILTIN,   $LR_X, null,
       "sprintf",            "-2",                $BUILTIN,   $LR_X, null,
       "sscanf",             "-2",                $BUILTIN,   $LR_X, null,
       "tempnam",            "-2",                $BUILTIN,   $LR_X, null,
       "tmpnam",             "1",                 $BUILTIN,   $LR_X, null,
       "tmpfile",            "0",                 $BUILTIN,   $LR_X, null,
       "ungetc",             "2",                 $BUILTIN,   $LR_X, null,
    };

    //
    // Temp file suport.
    //

    private static String  middle = "aaaa";

    private static final String  ALNUM = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789._";
    private static final long    timestamp = (long)(YOIXSTARTTIME.doubleValue()*1000.0);

    ///////////////////////////////////
    //
    // YoixModuleStdio Methods
    //
    ///////////////////////////////////

    public static YoixObject
    fclose(YoixObject arg[]) {

	boolean  result = false;

	if (arg[0].isStream())
	    result = arg[0].streamValue().close();
	else VM.badArgument(0);

	return(YoixObject.newInt(result ? 0 : -1));
    }


    public static YoixObject
    fflush(YoixObject arg[]) {

	boolean  result = false;

	if (arg[0].isStream())
	    result = arg[0].streamValue().flush();
	else VM.badArgument(0);

	return(YoixObject.newInt(result ? 0 : -1));
    }


    public static YoixObject
    fgetc(YoixObject arg[]) {

	return(getc(arg));
    }


    public static YoixObject
    fgets(YoixObject arg[]) {

	String  str = null;
	int     len;

	if (arg[0].isString()) {
	    if (arg[1].isInteger()) {
		if (arg[2].isStream()) {
		    len = arg[1].intValue() - 1;
		    if (len >= 0 && len <= arg[0].length()) {
			if ((str = arg[2].streamValue().readLine(len, NL_EOL)) != null) {
			    if (str.length() < arg[0].sizeof())
				str += '\0';
			    arg[0].overlay(str);
			}
		    } else VM.badArgument(2);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(str == null ? YoixObject.newString() : arg[0]);
    }


    public static YoixObject
    fopen(YoixObject arg[]) {

	YoixObject  obj = null;
	YoixObject  data;
	int         n;

	if (arg.length >= 2) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg[1].isString() && arg[1].notNull()) {
		    data = VM.getTypeTemplate(T_FILE);
		    if (arg.length > 2) {
			if (arg.length > 3) {
			    for (n = 2; n < arg.length - 1; n += 2) {
				if (arg[n].isString() && arg[n].notNull())
				    data.putObject(arg[n].stringValue(), arg[n+1]);
				else VM.badArgument(n);
			    }
			} else if (arg[2].notNull()) {
			    if (arg[2].isDictionary())
				YoixMisc.copyInto(arg[2], data);
			    else VM.badArgument(2);
			}
		    }
		    data.put(N_NAME, arg[0]);
		    data.put(N_MODE, arg[1]);
		    data.putInt(N_OPEN, true);
		    obj = YoixObject.newStream(data);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return((obj != null && obj.getBoolean(N_OPEN)) ? obj : YoixObject.newStream());
    }


    public static YoixObject
    fprintf(YoixObject arg[]) {

	int  count = -1;

	if (arg[0].isStream()) {
	    if (arg[1].isString() && arg[1].notNull())
		count = arg[0].streamValue().write(YoixMiscPrintf.print(arg, 1));
	    else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(count));
    }


    public static YoixObject
    fputc(YoixObject arg[]) {

	return(putc(arg));
    }


    public static YoixObject
    fputs(YoixObject arg[]) {

	int  count = -1;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isStream())
		count = arg[1].streamValue().write(arg[0].stringValue(true));
	    else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(count));
    }


    public static YoixObject
    freopen(YoixObject arg[]) {

	YoixObject  obj = null;
	String      path;
	String      mode;

	if (arg[0].isString()) {
	    if (arg[1].isString()) {
		if (arg[2].isStream()) {
		    path = arg[0].stringValue();
		    mode = arg[1].stringValue();
		    if (arg[2].streamValue().reopen(path, mode))
			obj = arg[2];
		    else obj = YoixObject.newStream();
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(obj);
    }


    public static YoixObject
    fscanf(YoixObject arg[]) {

	int  count = -1;

	if (arg[0].isStream()) {
	    if (arg[1].isString() && arg[1].notNull())
		count = YoixMiscScanf.scanf(arg[0].streamValue(), arg, 1);
	    else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(count));
    }


    public static YoixObject
    fseek(YoixObject arg[]) {

	return(VM.abort(UNIMPLEMENTED));
    }


    public static YoixObject
    ftell(YoixObject arg[]) {

	int  offset = -1;

	if (arg[0].isStream())
	    offset = (int)arg[0].streamValue().offsetBytes();
	else VM.badArgument(0);

	return(YoixObject.newInt(offset));
    }


    public static YoixObject
    getc(YoixObject arg[]) {

	int  ch = -1;

	if (arg[0].isStream())
	    ch = arg[0].streamValue().read();
	else VM.badArgument(0);

	return(YoixObject.newInt(ch));
    }


    public static YoixObject
    getchar(YoixObject arg[]) {

	return(YoixObject.newInt(VM.getStream(N_STDIN).read()));
    }


    public static YoixObject
    gets(YoixObject arg[]) {

	String  str = null;

	if (arg[0].isString()) {
	    if ((str = VM.getStream(N_STDIN).readLine(NL_EOL)) != null) {
		if (str.length() < arg[0].sizeof())
		    str += '\0';
		arg[0].overlay(str);
	    }
	} else VM.badArgument(0);

	return(str == null ? YoixObject.newString() : arg[0]);
    }



    public static YoixObject
    mktemp(YoixObject arg[]) {

	String  name;
	String  prefix;
	String  suffix;
	int     middle[] = {0};

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[0].stringValue().endsWith("XXXXXX")) {
		name = arg[0].stringValue();
		arg[0].putInt(0, '\0');
		prefix = name.substring(0, name.length() - 6);
		suffix = makeTempSuffix(timestamp, 5);
		if ((name = makeTempName(prefix, middle, suffix)) != null)
		    arg[0].overlay(name);
	    } else VM.badArgument(0);
	} else VM.badArgument(0);

	return(arg[0]);
    }


    public static YoixObject
    printf(YoixObject arg[]) {

	int  count = -1;

	if (arg[0].isString() && arg[0].notNull())
	    count = VM.getStream(N_STDOUT).write(YoixMiscPrintf.print(arg, 0));
	else VM.badArgument(0);

	return(YoixObject.newInt(count));
    }


    public static YoixObject
    putc(YoixObject arg[]) {

	int  ch = -1;

	if (arg[0].isInteger()) {
	    if (arg[1].isStream()) {
		ch = arg[0].intValue();
		if (arg[1].streamValue().write(ch) != 1)
		    ch = -1;
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(ch));
    }


    public static YoixObject
    putchar(YoixObject arg[]) {

	int  ch = -1;

	if (arg[0].isInteger()) {
	    ch = arg[0].intValue();
	    if (VM.getStream(N_STDOUT).write(ch) != 1)
		ch = -1;
	} else VM.badArgument(0);

	return(YoixObject.newInt(ch));
    }


    public static YoixObject
    puts(YoixObject arg[]) {

	int  count = -1;

	if (arg[0].isString() && arg[0].notNull()) {
	    count = VM.getStream(N_STDOUT).write(arg[0].stringValue(true) + '\n');
	    ////count = VM.getStream(N_STDOUT).writeLine(arg[0].stringValue(true));
	} else VM.badArgument(0);

	return(YoixObject.newInt(count));
    }


    public static YoixObject
    rewind(YoixObject arg[]) {

	return(VM.abort(UNIMPLEMENTED));
    }


    public static YoixObject
    scanf(YoixObject arg[]) {

	int  count = -1;

	if (arg[0].isString() && arg[0].notNull())
	    count = YoixMiscScanf.scanf(VM.getStream(N_STDIN), arg, 0);
	else VM.badArgument(0);

	return(YoixObject.newInt(count));
    }


    public static YoixObject
    setbuf(YoixObject arg[]) {

	return(VM.abort(UNIMPLEMENTED));
    }


    public static YoixObject
    sprintf(YoixObject arg[]) {

	String  str;
	int     count = -1;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		str = YoixMiscPrintf.print(arg, 1);
		count = str.length();
		if (count < arg[0].sizeof())
		    str += '\0';
		arg[0].overlay(str);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(count));
    }


    public static YoixObject
    sscanf(YoixObject arg[]) {

	YoixObject  data;
	YoixObject  obj = null;
	YoixObject  args[] = null;
	int         count = -1;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		if (arg[0].sizeof() > 0) {
		    args = new YoixObject[arg.length - 1];
		    System.arraycopy(arg, 1, args, 0, args.length);
		    data = VM.getTypeTemplate(T_STRINGSTREAM);
		    data.put(N_NAME, arg[0]);
		    data.put(N_MODE, YoixObject.newString("r"));
		    data.putInt(N_OPEN, true);
		    obj = YoixObject.newStream(data);
		    count = YoixMiscScanf.scanf(obj.streamValue(), args, 0);
		    obj.streamValue().close();
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(count));
    }


    public static YoixObject
    tempnam(YoixObject arg[]) {

	YoixObject  obj = null;
	String      name;
	String      prefix;
	String      suffix;
	int         offsets[];
	int         length;

	if (arg.length <= 3) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if (arg[1].isString() || arg[1].isNull()) {
		    if (arg[0].notNull())
			prefix = YoixMake.CString(arg[0], 0) + FILESEP;
		    else prefix = VM.getString(N_TMPDIR) + FILESEP;
		    if (arg[1].notNull())
			prefix += YoixMake.CString(arg[1], 0);
		    suffix = makeTempSuffix(timestamp, 5);
		    if (arg.length == 3) {
			if (arg[2].isString() || arg[2].isNull()) {
			    if (arg[2].notNull())
				suffix += YoixMake.CString(arg[2], 0);
			} else VM.badArgument(2);
		    }
		    synchronized(middle) {
			offsets = getMiddleOffsets();
			name = makeTempName(prefix, offsets, suffix);
			obj = YoixObject.newString(name);
			length = prefix.length();
			setNextMiddle(name.substring(length, length + offsets.length));
		    }
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj != null ? obj : YoixObject.newString());
    }


    public static YoixObject
    tmpfile(YoixObject arg[]) {

	return(VM.abort(UNIMPLEMENTED));
    }


    public static YoixObject
    tmpnam(YoixObject arg[]) {

	YoixObject  obj = null;
	String      name;
	String      prefix;
	String      suffix;
	int         offsets[];
	int         length;

	if (arg[0].isString() || arg[0].isNull()) {
	    if (arg[0].notNull())
		arg[0].putInt(0, '\0');
	    obj = arg[0];
	    prefix = VM.getString(N_TMPDIR) + FILESEP;
	    suffix = makeTempSuffix(timestamp, 5);
	    synchronized(middle) {
		offsets = getMiddleOffsets();
		if ((name = makeTempName(prefix, offsets, suffix)) != null) {
		    if (arg[0].notNull()) {
			obj = arg[0];
			obj.overlay(name);
			if (obj.length() > name.length())
			    obj.putInt(name.length(), '\0');
		    } else obj = YoixObject.newString(name);
		    length = prefix.length();
		    setNextMiddle(name.substring(length, length + offsets.length));
		} else obj = arg[0];
	    }
	} else VM.badArgument(0);

	return(obj);
    }


    public static YoixObject
    ungetc(YoixObject arg[]) {

	int  ch = -1;

	if (arg[0].isInteger()) {
	    if (arg[1].isStream())
		ch = arg[1].streamValue().ungetChar(arg[0].intValue());
	    else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(ch));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static int[]
    getMiddleOffsets() {

	int  offsets[];
	int  n;

	//
	// Only called from a synchronized block, so there's no need to
	// synchronize again.
	//

	offsets = new int[middle.length()];
	for (n = 0; n < offsets.length; n++)
	    offsets[n] = middle.charAt(n) - 'a';

	return(offsets);
    }


    private static String
    makeTempName(String prefix, int offsets[], String suffix) {

	String  name = null;
	int     total;
	int     m;
	int     n;

	total = (int)Math.pow(26, offsets.length);

	for (n = 0; n < total && name == null; n++) {
	    name = prefix;
	    for (m = 0; m < offsets.length; m++)
		name += (char)('a' + offsets[m]);
	    name += suffix;
	    if ((new File(name)).exists()) {
		name = null;
		for (m = 0; m < offsets.length; m++) {
		    if (++offsets[m] < 26)
			m = offsets.length;
		    else offsets[m] = 0;
		}
	    }
	}

	return(name);
    }


    private static String
    makeTempSuffix(long id, int length) {

	char  name[];
	int   n;

	name = new char[length];

	for (n = 0; n < length; n++) {
	    name[n] = ALNUM.charAt((int)(id&0x3F));
	    id = id >> 6;
	}
	return(new String(name));
    }


    private static void
    setNextMiddle(String str) {

	char  chars[];
	int   n;

	//
	// Only called from a synchronized block, so there's no need to
	// synchronize again.
	//

	chars = str.toCharArray();

	for (n = 0; n < chars.length; n++) {
	    if (++chars[n] <= 'z')
		n = chars.length;
	    else chars[n] = 'a';
	}

	middle = new String(chars);
    }
}

