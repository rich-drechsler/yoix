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
import java.io.File;
import java.text.*;
import java.util.*;
import java.util.regex.PatternSyntaxException;

abstract
class YoixModuleUtil extends YoixModule

    implements YoixConstantsJTable

{

    static String  $MODULENAME = M_UTIL;

    static Integer  $DOUBLE_TYPE  = new Integer(YOIX_DOUBLE_TYPE);
    static Integer  $INTEGER_TYPE = new Integer(YOIX_INTEGER_TYPE);
    static Integer  $MONEY_TYPE   = new Integer(YOIX_MONEY_TYPE);
    static Integer  $PERCENT_TYPE = new Integer(YOIX_PERCENT_TYPE);

    //
    // changed by YoixModuleUtil.setLocale, so not final
    //
    static char  DECIMAL_SEPARATOR = (new java.text.DecimalFormatSymbols()).getDecimalSeparator();

    //
    // Loading Locale can be expensive so postpone it until it's really
    // needed. Another reasonable approach would split yoix.util into a
    // few pieces.
    //

    static Object  $module[] = {
    //
    // NAME                      ARG                  COMMAND     MODE   REFERENCE
    // ----                      ---                  -------     ----   ---------
       null,                     "41",                $LIST,      $RORO, $MODULENAME,
       "DOUBLE_TYPE",            $DOUBLE_TYPE,        $INTEGER,   $LR__, null,
       "INTEGER_TYPE",           $INTEGER_TYPE,       $INTEGER,   $LR__, null,
       "MONEY_TYPE",             $MONEY_TYPE,         $INTEGER,   $LR__, null,
       "PERCENT_TYPE",           $PERCENT_TYPE,       $INTEGER,   $LR__, null,
       "Calendar",               "YoixModuleCalendar",$MODULE,    null,  null,
       "DateFormat",             "YoixModuleDate",    $MODULE,    null,  null,
       "Locale",                 "YoixModuleLocale",  $MODULE,    null,  null,
       "addCalendar",            "3",                 $BUILTIN,   $LR_X, null,
       "compareCalendar",        "2",                 $BUILTIN,   $LR_X, null,
       "compareJavaTo",          "1",                 $BUILTIN,   $LR_X, null,
       "date",                   "",                  $BUILTIN,   $LR_X, null,
       "findPaths",              "-2",                $BUILTIN,   $LR_X, null,
       "getArrayBands",          "-1",                $BUILTIN,   $LR_X, null,
       "getCalendarLeastMaximum","2",                 $BUILTIN,   $LR_X, null,
       "getCalendarLocales",     "0",                 $BUILTIN,   $LR_X, null,
       "getCalendarMaximum",     "2",                 $BUILTIN,   $LR_X, null,
       "getCalendarMinimum",     "2",                 $BUILTIN,   $LR_X, null,
       "getDateFormat",          "",                  $BUILTIN,   $LR_X, null,
       "getDictionaryNames",     "-1",                $BUILTIN,   $LR_X, null,
       "getLocale",              "",                  $BUILTIN,   $LR_X, null,
       "getLocales",             "0",                 $BUILTIN,   $LR_X, null,
       "getopt",                 "-3",                $BUILTIN,   $LR_X, null,
       "getTimeZone",            "",                  $BUILTIN,   $LR_X, null,
       "getTimeZoneIDs",         "",                  $BUILTIN,   $LR_X, null,
       "hashIndex",              "-2",                $BUILTIN,   $LR_X, null,
       "inDaylightTime",         "2",                 $BUILTIN,   $LR_X, null,
       "indexOfObject",          "-2",                $BUILTIN,   $LR_X, null,
       "lastIndexOfObject",      "-2",                $BUILTIN,   $LR_X, null,
       "numberFormat",           "-1",                $BUILTIN,   $LR_X, null,
       "parseDate",              "-1",                $BUILTIN,   $LR_X, null,
       "parseNumber",            "-1",                $BUILTIN,   $LR_X, null,
       "parseTimer",             "1",                 $BUILTIN,   $LR_X, null,
       "qsort",                  "-1",                $BUILTIN,   $LR_X, null,
       "replicate",              "-2",                $BUILTIN,   $LR_X, null,
       "rollCalendar",           "3",                 $BUILTIN,   $LR_X, null,
       "sequence",               "-1",                $BUILTIN,   $LR_X, null,
       "setCalendar",            "-3",                $BUILTIN,   $LR_X, null,
       "setLocale",              "1",                 $BUILTIN,   $LR_X, null,
       "setTimeZone",            "",                  $BUILTIN,   $LR_X, null,
       "timerFormat",            "-1",                $BUILTIN,   $LR_X, null,
       "uniq",                   "1",                 $BUILTIN,   $LR_X, null,

       T_OPTION,                 "9",                 $DICT,      $L___, T_OPTION,
       N_MAJOR,                  $OPTION,             $INTEGER,   $LR__, null,
       N_MINOR,                  "0",                 $INTEGER,   $LR__, null,
       N_GETOPT,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_OPTARG,                 T_OBJECT,            $NULL,      $RW_,  null,
       N_OPTERROR,               T_STRING,            $NULL,      $RW_,  null,
       N_OPTCHAR,                "0",                 $INTEGER,   $RW_,  null,
       N_OPTIND,                 "1",                 $INTEGER,   $RW_,  null,
       N_OPTSTR,                 "-+",                $STRING,    $RW_,  null,
       N_OPTWORD,                T_STRING,            $NULL,      $RW_,  null,

       T_LOCALE,                 "11",                $DICT,      $L___, T_LOCALE,
       N_MAJOR,                  $LOCALE,             $INTEGER,   $LR__, null,
       N_MINOR,                  "0",                 $INTEGER,   $LR__, null,
       N_COUNTRY,                T_STRING,            $NULL,      $RW_,  null,
       N_DISPLAYCOUNTRY,         T_STRING,            $NULL,      $R__,  null,
       N_DISPLAYLANGUAGE,        T_STRING,            $NULL,      $R__,  null,
       N_DISPLAYNAME,            T_STRING,            $NULL,      $R__,  null,
       N_DISPLAYVARIANT,         T_STRING,            $NULL,      $R__,  null,
       N_ISO3COUNTRY,            T_STRING,            $NULL,      $R__,  null,
       N_ISO3LANGUAGE,           T_STRING,            $NULL,      $R__,  null,
       N_LANGUAGE,               T_STRING,            $NULL,      $RW_,  null,
       N_VARIANT,                T_STRING,            $NULL,      $RW_,  null,

       T_TIMEZONE,               "9",                 $DICT,      $L___, T_TIMEZONE,
       N_MAJOR,                  $TIMEZONE,           $INTEGER,   $LR__, null,
       N_MINOR,                  "0",                 $INTEGER,   $LR__, null,
       N_DISPLAYNAME,            T_CALLABLE,          $NULL,      $L__X, null,
       N_DST,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_DSTOFFSET,              "0",                 $INTEGER,   $LR__, null,
       N_ID,                     T_STRING,            $NULL,      $RW_,  null,
       N_SAMERULES,              T_CALLABLE,          $NULL,      $L__X, null,
       N_USEDST,                 $FALSE,              $INTEGER,   $LR__, null,
       N_ZONEOFFSET,             "0",                 $INTEGER,   $RW_,  null,

       T_CALENDAR,               "34",                $DICT,      $L___, T_CALENDAR,
       N_MAJOR,                  $CALENDAR,           $INTEGER,   $LR__, null,
       N_MINOR,                  "0",                 $INTEGER,   $LR__, null,
       N_ADD,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_AMPM,                   "-1",                $INTEGER,   $LR__, null,
       N_DATE,                   "-1",                $INTEGER,   $RW_,  null,
       N_DAYOFMONTH,             "-1",                $INTEGER,   $RW_,  null,
       N_DAYOFWEEK,              "-1",                $INTEGER,   $LR__, null,
       N_DAYOFWEEKINMONTH,       "-1",                $INTEGER,   $LR__, null,
       N_DAYOFYEAR,              "-1",                $INTEGER,   $RW_,  null,
       N_DSTOFFSET,              "-1",                $INTEGER,   $LR__, null,
       N_ERA,                    "-1",                $INTEGER,   $RW_,  null,
       N_FIRSTDAYOFWEEK,         "-1",                $INTEGER,   $RW_,  null,
       N_GREGORIANCHANGE,        "0",                 $DOUBLE,    $RW_,  null,
       N_HOUR,                   "-1",                $INTEGER,   $LR__, null,
       N_HOUROFDAY,              "-1",                $INTEGER,   $RW_,  null,
       N_LEAPYEAR,               $FALSE,              $INTEGER,   $LR__, null,
       N_LEASTMAXIMUM,           T_CALLABLE,          $NULL,      $L__X, null,
       N_LENIENT,                $TRUE,               $INTEGER,   $RW_,  null,
       N_LOCALE,                 T_LOCALE,            $NULL,      $RW_,  null,
       N_MAXIMUM,                T_CALLABLE,          $NULL,      $L__X, null,
       N_MILLISECOND,            "-1",                $INTEGER,   $RW_,  null,
       N_MINDAYSINFIRSTWEEK,     "-1",                $INTEGER,   $RW_,  null,
       N_MINIMUM,                T_CALLABLE,          $NULL,      $L__X, null,
       N_MINUTE,                 "-1",                $INTEGER,   $RW_,  null,
       N_MONTH,                  "-1",                $INTEGER,   $RW_,  null,
       N_ROLL,                   T_CALLABLE,          $NULL,      $L__X, null,
       N_SECOND,                 "-1",                $INTEGER,   $RW_,  null,
       N_SET,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_WEEKOFMONTH,            "-1",                $INTEGER,   $LR__, null,
       N_WEEKOFYEAR,             "-1",                $INTEGER,   $LR__, null,
       N_TIMEZONE,               T_TIMEZONE,          $NULL,      $RW_,  null,
       N_UNIXTIME,               $NAN,                $DOUBLE,    $RW_,  null,
       N_YEAR,                   "0",                 $INTEGER,   $RW_,  null,
       N_ZONEOFFSET,             "-1",                $INTEGER,   $LR__, null,
    };

    ///////////////////////////////////
    //
    // YoixModuleUtil Methods
    //
    ///////////////////////////////////

    public static YoixObject
    addCalendar(YoixObject arg[]) {

	Calendar  calendar;
	int       amount;
	int       field;

	if (arg[0].isCalendar() && arg[0].notNull()) {
	    if (arg[1].isInteger()) {
		if (arg[2].isInteger()) {
		    amount = arg[2].intValue();
		    if (amount != 0) {
			calendar = (Calendar)(arg[0].getManagedObject());
			field = arg[1].intValue();
			try {
			    calendar.add(field, amount);
			}
			catch(IllegalArgumentException e) {
			    VM.badArgumentValue(1);
			}
		    }
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(arg[0]);
    }


    public static YoixObject
    compareCalendar(YoixObject arg[]) {

	Calendar  cal1;
	Calendar  cal2;
	int       value = 0;

	if (arg[0].isCalendar() && arg[0].notNull()) {
	    if (arg[1].isCalendar() && arg[1].notNull()) {
		cal1 = (Calendar)(arg[0].getManagedObject());
		cal2 = (Calendar)(arg[1].getManagedObject());
		value = cal1.after(cal2) ? 1 : (cal1.before(cal2) ? -1 : 0);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    compareJavaTo(YoixObject arg[]) {

	int  answer = 0;

	if (arg[0].isString() || arg[0].isNull())
	    answer = YoixMisc.jvmCompareTo(arg[0].stringValue());
	else VM.badArgument(0);

	return(YoixObject.newInt(answer < 0 ? -1 : (answer > 0 ? 1 : 0)));
    }


    public static YoixObject
    date(YoixObject arg[]) {

	YoixSimpleDateFormat  sdf = null;
	TimeZone              timezone;
	Locale                locale;
	String                str = null;
	String                format = UNIX_DATE_FORMAT;
	double                d;
	char                  array[];
	long                  l;
	int                   argn = 0;
	int                   i;

	if (arg.length > argn && arg[argn].isLocale() && arg[argn].notNull()) {
	    locale = (Locale)(arg[argn].getManagedObject());
	    argn++;
	} else locale = Locale.getDefault();

	if (arg.length > argn && arg[argn].isTimeZone() && arg[argn].notNull()) {
	    timezone = (TimeZone)(arg[argn].getManagedObject());
	    argn++;
	} else timezone = YoixMiscTime.getDefaultTimeZone();

	if (arg.length >= argn && arg.length <= argn+2) {
	    if (arg.length == argn) {
		sdf = new YoixSimpleDateFormat(format, locale);
		sdf.setTimeZone(timezone);
		str = sdf.format(new Date());
	    } else if (arg.length == argn+1) {
		if (arg[argn].isString() && arg[argn].notNull()) {
		    sdf = new YoixSimpleDateFormat(format = arg[argn].stringValue(), locale);
		    sdf.setTimeZone(timezone);
		    try {
			str = sdf.format(new Date());
		    }
		    catch(RuntimeException e) {
			VM.recordException(e);
			VM.badArgument(argn);
		    }
		} else if (arg[argn].isNumber()) {
		    sdf = new YoixSimpleDateFormat(format, locale);
		    sdf.setTimeZone(timezone);
		    d = arg[argn].doubleValue();
		    l = (long)(d * 1000.0);
		    str = sdf.format(new Date(l));
		} else VM.badArgument(argn);
	    } else if (arg.length == argn+2) {
		if (arg[argn].isString() && arg[argn].notNull()) {
		    sdf = new YoixSimpleDateFormat(format = arg[argn].stringValue(), locale);
		    sdf.setTimeZone(timezone);
		    if (arg[argn+1].isNumber()) {
			d = arg[argn+1].doubleValue();
			l = (long)(d * 1000.0);
			try {
			    str = sdf.format(new Date(l));
			}
			catch(RuntimeException e) {
			    VM.recordException(e);
			    VM.badArgument(argn);
			}
		    } else VM.badArgument(argn+1);
		} else VM.badArgument(argn);
	    }

	    if (format == UNIX_DATE_FORMAT && (i = str.indexOf(" 0")) >= 0) {
		array = str.toCharArray();
		do {
		    array[i+1] = ' ';
		} while ((i+2) < array.length && (i = str.indexOf(" 0", i+2)) >= 0);
		str = new String(array);
	    }
	} else VM.badCall();

	return(YoixObject.newString(str));
    }


    public static YoixObject
    findPaths(YoixObject arg[]) {

	ArrayList   results = null;
	String      executables[] = null;
	String      paths[] = null;
	YoixObject  result = null;
	YoixObject  yobj;
	boolean     bydir = true;
	String      str;
	int         cnt = 0;
	int         off;
	int         rule = 0; // <0 => stop at first one; 0 => stop at first set; >0 => get all
	int         m;
	int         n;

	if (arg.length <= 3) {
	    if (arg[0].notNull() && arg[0].isString()) {
		if ((str = arg[0].stringValue()).length() > 0) {
		    try {
			String p = PATHSEP;
			paths = str.split("[" + p + "]");
		    }
		    catch(PatternSyntaxException pse) {
			// should never happen
			VM.abort(BADVALUE, "path separator cannot be used to parse path!");
		    }
		} else VM.badArgumentValue(0);
		if (arg[1].notNull()) {
		    if (arg[1].isString()) {
			if ((str = arg[1].stringValue()).length() > 0) {
			    executables = new String[1];
			    executables[0] = str;
			} else VM.badArgumentValue(1);
		    } else if (arg[1].isArray()) {
			if (arg[1].sizeof() > 0) {
			    executables = new String[arg[1].sizeof()];
			    off = arg[1].offset();
			    for (n = 0; n < executables.length; n++) {
				yobj = arg[1].get(n, false);
				if (yobj.notNull() && yobj.isString() && (str = yobj.stringValue()).length() > 0) {
				    executables[n] = str;
				} else VM.badArgumentValue(1, n);
			    }
			} else VM.badArgumentValue(1);
		    } else VM.badArgument(1);
		} else VM.badArgument(1);
	    } else if (arg[0].notNull() && arg[0].isArray()) {
		bydir = false;
		if (arg[0].sizeof() > 0) {
		    executables = new String[arg[0].sizeof()];
		    off = arg[0].offset();
		    for (n = 0; n < executables.length; n++) {
			yobj = arg[0].get(n, false);
			if (yobj.notNull() && yobj.isString() && (str = yobj.stringValue()).length() > 0) {
			    executables[n] = str;
			} else VM.badArgumentValue(0, n);
		    }
		} else VM.badArgumentValue(0);
		if (arg[1].notNull() && arg[1].isString()) {
		    if ((str = arg[1].stringValue()).length() > 0) {
			try {
			    String p = PATHSEP;
			    paths = str.split("[" + p + "]");
			}
			catch(PatternSyntaxException pse) {
			// should never happen
			    VM.abort(BADVALUE, "path separator cannot be used to parse path!");
			}
		    } else VM.badArgumentValue(1);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	    if (arg.length == 3) {
		if (arg[2].isInteger()) {
		    rule = arg[2].intValue();
		} else VM.badArgument(2);
	    }
	    //
	    // At this point we have a set of paths and a set of executables, so now
	    // we just have to go though and check them
	    //
	    if (bydir) {
	    pathlist:
		for (m = 0; m < paths.length; m++) {
		    if (paths[m].length() > 0 && isDirectory(paths[m])) {
			for (n = 0; n < executables.length; n++) {
			    if (executables[n] != null) {
				str = paths[m] + FILESEP + executables[n];
				if (isFile(str)) {
				    if (rule < 0) {
					result = YoixObject.newString(str);
					break pathlist;
				    } else if (rule == 0) {
					if (result == null)
					    result = YoixObject.newArray(0, -1);
					result.putString(cnt++, str);
					if (cnt == executables.length)
					    break pathlist;
					executables[n] = null;
				    } else {
					if (results == null)
					    results = new ArrayList();
					results.add(YoixObject.newString(str));
				    }
				}
			    }
			}
		    }
		}
	    } else {
	    cmdlist:
		for (n = 0; n < executables.length; n++) {
		    if (executables[n] != null) {
			for (m = 0; m < paths.length; m++) {
			    if (paths[m].length() > 0 && isDirectory(paths[m])) {
				str = paths[m] + FILESEP + executables[n];
				if (isFile(str)) {
				    if (rule < 0) {
					result = YoixObject.newString(str);
					break cmdlist;
				    } else if (rule == 0) {
					if (result == null)
					    result = YoixObject.newArray(0, -1);
					result.putString(cnt++, str);
					if (cnt == executables.length)
					    break cmdlist;
					executables[n] = null;
				    } else {
					if (results == null)
					    results = new ArrayList();
					results.add(YoixObject.newString(str));
				    }
				}
			    }
			}
		    }
		}
	    }
	    if (rule > 0 && results != null) {
		result = YoixMake.yoixArray(results);
	    }
	} else VM.badCall();

	return(result == null ? YoixObject.newNull() : result);
    }


    public static YoixObject
    getArrayBands(YoixObject arg[]) {

	YoixObject  result = null;
	YoixObject  changes[];
	YoixObject  yobj;
	YoixObject  prev;
	boolean     reference[] = null;
	int         modulo[] = null;
	int         modoff[] = null;
	int         length;
	int         offset;
	int         chng = -1;
	int         m;
	int         n;

	if (arg[0].isArray() && arg[0].notNull()) {
	    if (arg.length > 1) {
		modulo = new int[arg.length - 1];
		modoff = new int[arg.length - 1];
		reference = new boolean[arg.length - 1];
		for (n = 1; n < arg.length; n++) {
		    m = n - 1;
		    if (arg[n].isInteger()) {
			modulo[m] = arg[n].intValue();
			modoff[m] = -1;
			reference[m] = false;
		    } else if (arg[n].isArray() && arg[n].notNull()) {
			modulo[m] = arg[n].sizeof();
			modoff[m] = arg[n].offset();
			reference[m] = (modulo[m] > 0);
		    } else {
			modulo[m] = -1;
			modoff[m] = -1;
			reference[m] = true;
		    }
		}
	    }
	    length = arg[0].sizeof();
	    offset = arg[0].offset();
	    if (modulo == null || modulo.length == 1) {
		result = YoixObject.newArray(length);
		changes = new YoixObject[1];
		changes[0] = result;
		if (modulo == null) {
		    reference = new boolean[1];
		    reference[0] = false;
		    modulo = new int[1];
		    modulo[0] = 0;
		    modoff = new int[1];
		    modoff[0] = 0;
		}
	    } else {
		result = YoixObject.newArray(modulo.length);
		changes = new YoixObject[modulo.length];
		for (m = 0; m < modulo.length; m++) {
		    if (modulo[m] < 0)
			changes[m] = (YoixObject)(arg[m+1].clone());
		    else changes[m] = YoixObject.newArray(length);
		    result.put(m, changes[m], false);
		}
	    }
	    prev = result; // initial value that is sure to fail equalsEQEQ test
	    for (n = 0; n < length; n++) {
		yobj = arg[0].getObject(n + offset);
		if (YoixInterpreter.equalsEQEQ(yobj, prev) == false) {
		    prev = yobj;
		    chng++;
		}
		for (m = 0; m < modulo.length; m++) {
		    if (reference[m]) {
			if (modulo[m] > 0)
			    changes[m].put(n, arg[m+1].getObject(modoff[m] + (chng%modulo[m])), true);
			else if (modulo[m] == 0)
			    changes[m].put(n, arg[m+1].getObject(modoff[m] + chng), true);
		    } else {
			if (modulo[m] > 0)
			    changes[m].putInt(n, (chng%modulo[m]));
			else changes[m].putInt(n, chng);
		    }
		}
	    }
	} else VM.badArgument(0);

	return(result == null ? YoixObject.newNull() : result);
    }


    public static YoixObject
    getCalendarLeastMaximum(YoixObject arg[]) {

	Calendar  calendar;
	int       field;
	int       value = -1;

	if (arg[0].isCalendar() && arg[0].notNull()) {
	    if (arg[1].isInteger()) {
		calendar = (Calendar)(arg[0].getManagedObject());
		field = arg[1].intValue();
		try {
		    value = calendar.getLeastMaximum(field);
		}
		catch(IllegalArgumentException e) {
		    VM.badArgument(1);
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    getCalendarLocales(YoixObject arg[]) {

	YoixObject  obj;
	Locale      locales[];
	int         n;

	locales = Calendar.getAvailableLocales();
	obj = YoixObject.newArray(locales.length);
	for (n = 0; n < locales.length; n++)
	    obj.put(n, YoixObject.newLocale(locales[n]), false);

	return(obj);
    }


    public static YoixObject
    getCalendarMaximum(YoixObject arg[]) {

	Calendar  calendar;
	int       field;
	int       value = -1;

	if (arg[0].isCalendar() && arg[0].notNull()) {
	    if (arg[1].isInteger()) {
		calendar = (Calendar)(arg[0].getManagedObject());
		field = arg[1].intValue();
		try {
		    value = calendar.getMaximum(field);
		}
		catch(IllegalArgumentException e) {
		    VM.badArgument(1);
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    getCalendarMinimum(YoixObject arg[]) {

	Calendar  calendar;
	int       field;
	int       value = -1;

	if (arg[0].isCalendar() && arg[0].notNull()) {
	    if (arg[1].isInteger()) {
		calendar = (Calendar)(arg[0].getManagedObject());
		field = arg[1].intValue();
		try {
		    value = calendar.getMinimum(field);
		}
		catch(IllegalArgumentException e) {
		    VM.badArgument(1);
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    getDateFormat(YoixObject arg[]) {

	SimpleDateFormat  sdf;
	boolean           localized = false;
	Locale            locale;
	String            format = UNIX_DATE_FORMAT;
	int               date_style = DateFormat.DEFAULT;
	int               time_style = DateFormat.DEFAULT;
	int               mode = 0;

	if (arg.length <= 5) {
	    if (arg.length > 0) {
		if (arg[0].isLocale() && arg[0].notNull()) {
		    locale = (Locale)(arg[0].getManagedObject());
		    if (arg.length > 1) {
			if (arg[1].isInteger())
			    localized = arg[1].booleanValue();
			else VM.badArgument(1);
			if (arg.length > 2) {
			    if (arg[2].isInteger())
				mode = arg[2].intValue();
			    else VM.badArgument(2);
			    if (arg.length > 3) {
				if (arg[3].isInteger()) {
				    if (mode >= 0 || arg.length > 4)
					date_style = arg[3].intValue();
				    else time_style = arg[3].intValue();
				} else VM.badArgument(3);
				if (arg.length > 4) {
				    if (arg[4].isInteger())
					time_style = arg[4].intValue();
				    else VM.badArgument(4);
				}
			    }
			}
		    }
		    if (mode == 0)
			sdf = (SimpleDateFormat)SimpleDateFormat.getDateTimeInstance(date_style, time_style, locale);
		    else if (mode > 0)
			sdf = (SimpleDateFormat)SimpleDateFormat.getDateInstance(date_style, locale);
		    else sdf = (SimpleDateFormat)SimpleDateFormat.getTimeInstance(time_style, locale);

		    if (localized)
			format = sdf.toLocalizedPattern();
		    else format = sdf.toPattern();
		} else VM.badArgument(0);
	    }
	} else VM.badCall();

	return(YoixObject.newString(format));
    }


    public static YoixObject
    getDictionaryNames(YoixObject arg[]) {

	YoixObject  names = null;
	boolean     sort = false;
	int         length;
	int         n;

	if (arg.length <= 2) {
	    if (arg[0].isPointer() && arg[0].notNull()) {	// was: isDictionary()
		if (arg.length == 2) {
		    if (arg[1].isInteger())
			sort = arg[1].booleanValue();
		    else VM.badArgument(1);
		}
		length = arg[0].length();
		names = YoixObject.newArray(length);
		for (n = 0; n < length; n++)
		    names.putString(n, arg[0].name(n));
		if (sort && length > 0)
		    YoixMiscQsort.qsort(names, 1, null);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(names);
    }


    public static YoixObject
    getLocale(YoixObject arg[]) {

	YoixObject  obj = null;
 	Locale      locale = null;
 	String      language = null;
 	String      country = null;
 	String      variant = null;

 	if (arg.length >= 0 && arg.length <=3) {
 	    if (arg.length > 0) {
 		if (arg[0].isNull() || arg[0].isString()) {
 		    language = arg[0].stringValue();
 		    if (arg.length > 1) {
 			if (arg[1].isNull() || arg[1].isString()) {
 			    country = arg[1].stringValue();
 			    if (arg.length > 2) {
 				if (arg[2].isNull() || arg[2].isString()) {
 				    variant = arg[2].stringValue();
 				} else VM.badArgument(2);
 			    }
 			} else VM.badArgument(1);
 		    }
 		} else VM.badArgument(0);
 		if (variant == null) {
 		    if (country == null) {
			if (language == null)
			    locale = Locale.getDefault();
 			else locale = new Locale(language);
 		    } else if (language != null)
 			locale = new Locale(language, country);
 		    else VM.badArgumentValue(1);
 		} else if (country != null) {
 		    if (language != null)
 			locale = new Locale(language, country, variant);
 		    else VM.badArgumentValue(1);
 		} else VM.badArgumentValue(2);
 	    } else locale = Locale.getDefault();
	    obj = YoixObject.newLocale(locale);
 	} else VM.badCall();

	return(obj);
    }


    public static YoixObject
    getLocales(YoixObject arg[]) {

	YoixObject  obj = null;
	Locale      locales[];
	int         len;
	int         n;

	locales = Locale.getAvailableLocales();
	obj = YoixObject.newArray(len = locales.length);
	for (n = 0; n < len; n++)
	    obj.putObject(n, YoixObject.newLocale(locales[n]));

	return(obj);
    }


    public static YoixObject
    getopt(YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg.length == 3 || arg.length == 4) {
	    if (arg[0].isOption() && arg[0].notNull())
		obj = ((YoixBodyOption)arg[0].body()).callGetopt(arg, 1);
	    else VM.badArgument(0);
	} else VM.badCall();

	return(obj != null ? obj : YoixObject.newInt(-1));
    }


    public static YoixObject
    getTimeZone(YoixObject arg[]) {

	YoixObject  obj = null;
	TimeZone    timezone;

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 0) {
		timezone = YoixMiscTime.getDefaultTimeZone();
		obj = YoixObject.newTimeZone(timezone);
	    } else if (arg[0].isString() && arg[0].notNull()) {
		timezone = TimeZone.getTimeZone(arg[0].stringValue());
		obj = YoixObject.newTimeZone(timezone);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj);
    }


    public static YoixObject
    getTimeZoneIDs(YoixObject arg[]) {

	YoixObject  obj = null;
	String      ids[] = null;
	int         n;

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 0) {
		ids = TimeZone.getAvailableIDs();
	    } else {
		if (arg[0].isInteger())
		    ids = TimeZone.getAvailableIDs(arg[0].intValue());
		else VM.badArgument(0);
	    }
	} else VM.badCall();

	obj = YoixObject.newArray(ids.length);
	for (n = 0; n < ids.length; n++)
	    obj.putString(n, ids[n]);

	return(obj);
    }


    public static YoixObject
    hashIndex(YoixObject arg[]) {

	int  index = -1;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isString() || arg[0].notNull()) {
		if (arg[1].isPointer()) {
		    if (arg.length == 2 || arg[2].isInteger()) {
			index = arg[1].hash(arg[0].stringValue());
			if (arg.length > 2 && arg[2].booleanValue()) {
			    if (arg[1].defined(index) == false)
				index = -1;
			}
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(index));
    }


    public static YoixObject
    inDaylightTime(YoixObject arg[]) {

	TimeZone  timezone;
	boolean   dst = false;
	double    dt;

	if (arg[0].isTimeZone() && arg[0].notNull()) {
	    if (arg[1].isNumber()) {
		timezone = (TimeZone)(arg[0].getManagedObject());
		dt = arg[1].doubleValue();
		dst = timezone.inDaylightTime(new Date((long)(1000.0*dt)));
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(dst ? 1 : 0));
    }


    public static YoixObject
    indexOfObject(YoixObject arg[]) {

	YoixObject  source;
	YoixObject  target;
	YoixObject  obj;
	Object      body;
	int         length;
	int         index = -1;
	int         n;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isPointer()) {
		if (arg.length == 2 || arg[2].isNumber()) {
		    source = arg[0];
		    target = arg[1];
		    length = source.length();
		    if (arg.length == 2 || arg[2].booleanValue() || target.notPointer()) {
			for (n = arg[0].offset(); n < length; n++) {
			    if (source.defined(n)) {
				if (YoixInterpreter.equalsEQEQ(source.get(n, false), target)) {
				    index = n;
				    break;
				}
			    }
			}
		    } else {
			body = target.body();
			for (n = arg[0].offset(); n < length; n++) {
			    if ((obj = source.getObject(n)) != null) {
				if ((obj.isPointer() && obj.body() == body) || YoixInterpreter.equalsEQEQ(obj, target)) {
				    index = n;
				    break;
				}
			    }
			}
		    }
		} else VM.badArgument(2);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(index));
    }


    public static YoixObject
    lastIndexOfObject(YoixObject arg[]) {

	YoixObject  source;
	YoixObject  target;
	YoixObject  obj;
	Object      body;
	int         offset;
	int         index = -1;
	int         n;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isPointer()) {
		if (arg.length == 2 || arg[2].isNumber()) {
		    source = arg[0];
		    target = arg[1];
		    offset = source.offset();
		    if (arg.length == 2 || arg[2].booleanValue() || target.notPointer()) {
			for (n = arg[0].length() - 1; n >= offset; n--) {
			    if (source.defined(n)) {
				if (YoixInterpreter.equalsEQEQ(source.get(n, false), target)) {
				    index = n;
				    break;
				}
			    }
			}
		    } else {
			body = target.body();
			for (n = arg[0].length() - 1; n >= offset; n--) {
			    if ((obj = source.getObject(n)) != null) {
				if ((obj.isPointer() && obj.body() == body) || YoixInterpreter.equalsEQEQ(obj, target)) {
				    index = n;
				    break;
				}
			    }
			}
		    }
		} else VM.badArgument(2);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(index));
    }


    public static YoixObject
    numberFormat(YoixObject arg[]) {
	HashMap             hash;
	Locale              locale;
	NumberFormat        nf = null;
	String              output = null;
	YoixObject          dict = null;
	YoixObject          yobj = null;
	YoixObject          yval;
	int                 argn = 0;
	int                 len = 0;
	int                 type = YOIX_DOUBLE_TYPE;

	if (arg.length > argn && arg[argn].isLocale() && arg[argn].notNull()) {
	    locale = (Locale)(arg[argn].getManagedObject());
	    argn++;
	} else locale = Locale.getDefault();

	if (arg.length == argn+1 || arg.length == argn+2) {
	    if (arg[argn].isNumber()) {
		yobj = arg[argn];
		if (arg.length == argn+2) {
		    if (arg[argn+1].isInteger()) {
			switch(type = arg[argn+1].intValue()) {
			case YOIX_DOUBLE_TYPE:
			    nf = NumberFormat.getInstance(locale);
			    break;
			case YOIX_INTEGER_TYPE:
			    nf = NumberFormat.getIntegerInstance(locale);
			    break;
			case YOIX_MONEY_TYPE:
			    nf = NumberFormat.getCurrencyInstance(locale);
			    break;
			case YOIX_PERCENT_TYPE:
			    nf = NumberFormat.getPercentInstance(locale);
			    break;
			default:
			    VM.badArgumentValue(2);
			    break;
			}
		    } else if (arg[argn+1].isDictionary() && arg[argn+1].notNull()) {
			dict = arg[argn+1];
			len = dict.length();
			for (int n = 0; n < len; n++) {
			    if ("type".equals(dict.name(n))) {
				yval = dict.get(n, false);
				if (yval.isInteger()) {
				    switch(type = yval.intValue()) {
				    case YOIX_DOUBLE_TYPE:
					nf = NumberFormat.getInstance(locale);
					break;
				    case YOIX_INTEGER_TYPE:
					nf = NumberFormat.getIntegerInstance(locale);
					break;
				    case YOIX_MONEY_TYPE:
					nf = NumberFormat.getCurrencyInstance(locale);
					break;
				    case YOIX_PERCENT_TYPE:
					nf = NumberFormat.getPercentInstance(locale);
					break;
				    default:
					VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
					break;
				    }
				} else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
				break;
			    }
			}
		    } else VM.badArgument(argn+1);
		}
	    } else VM.badArgument(argn);
	} else VM.badCall();

	if (nf == null)
	    nf = NumberFormat.getInstance(locale);

	if (dict != null) {
	    for (int n = 0; n < len; n++) {
		if ("type".equals(dict.name(n))) {
		    continue;
		} else if ("groupingUsed".equals(dict.name(n))) {
		    yval = dict.get(n, false);
		    if (yval.isInteger())
			nf.setGroupingUsed(yval.booleanValue());
		    else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		} else if ("maximumFractionDigits".equals(dict.name(n))) {
		    yval = dict.get(n, false);
		    if (yval.isInteger())
			nf.setMaximumFractionDigits(yval.intValue());
		    else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		} else if ("maximumIntegerDigits".equals(dict.name(n))) {
		    yval = dict.get(n, false);
		    if (yval.isInteger())
			nf.setMaximumIntegerDigits(yval.intValue());
		    else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		} else if ("minimumFractionDigits".equals(dict.name(n))) {
		    yval = dict.get(n, false);
		    if (yval.isInteger())
			nf.setMinimumFractionDigits(yval.intValue());
		    else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		} else if ("minimumIntegerDigits".equals(dict.name(n))) {
		    yval = dict.get(n, false);
		    if (yval.isInteger())
			nf.setMinimumIntegerDigits(yval.intValue());
		    else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		} else if ("parseIntegerOnly".equals(dict.name(n))) {
		    yval = dict.get(n, false);
		    if (yval.isInteger())
			nf.setParseIntegerOnly(yval.booleanValue());
		    else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		} else if (type != YOIX_INTEGER_TYPE) {
		    if ("decimalSeparatorAlwaysShown".equals(dict.name(n))) {
			yval = dict.get(n, false);
			if (yval.isInteger())
			    ((DecimalFormat)nf).setDecimalSeparatorAlwaysShown(yval.booleanValue());
			else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		    } else if ("groupingSize".equals(dict.name(n))) {
			yval = dict.get(n, false);
			if (yval.isInteger())
			    ((DecimalFormat)nf).setGroupingSize(yval.intValue());
			else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		    } else if ("multiplier".equals(dict.name(n))) {
			yval = dict.get(n, false);
			if (yval.isInteger())
			    ((DecimalFormat)nf).setMultiplier(yval.intValue());
			else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		    } else if ("negativePrefix".equals(dict.name(n))) {
			yval = dict.get(n, false);
			if (yval.isString() && yval.notNull())
			    ((DecimalFormat)nf).setNegativePrefix(yval.stringValue());
			else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		    } else if ("negativeSuffix".equals(dict.name(n))) {
			yval = dict.get(n, false);
			if (yval.isString() && yval.notNull())
			    ((DecimalFormat)nf).setNegativeSuffix(yval.stringValue());
			else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		    } else if ("positivePrefix".equals(dict.name(n))) {
			yval = dict.get(n, false);
			if (yval.isString() && yval.notNull())
			    ((DecimalFormat)nf).setPositivePrefix(yval.stringValue());
			else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		    } else if ("positiveSuffix".equals(dict.name(n))) {
			yval = dict.get(n, false);
			if (yval.isString() && yval.notNull())
			    ((DecimalFormat)nf).setPositiveSuffix(yval.stringValue());
			else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		    } else VM.badArgumentValue(dict.name(n), 2);
		} else VM.badArgumentValue(dict.name(n), 2);
	    }
	}

	if (yobj != null) {
	    if (yobj.isInteger())
		output = nf.format((long)(yobj.intValue()));
	    else output = nf.format(yobj.doubleValue());
	}

	return(output == null ? YoixObject.newString() : YoixObject.newString(output));
    }


    public static YoixObject
    parseDate(YoixObject arg[]) {

	YoixSimpleDateFormat  sdf = null;
	ParsePosition         pos = null;
	TimeZone              timezone;
	boolean               lenient = true;
	Locale                locale;
	String                date = null;
	String                format = UNIX_DATE_FORMAT;
	long                  time = 0;
	int                   argn = 0;

	if (arg.length > argn && arg[argn].isLocale() && arg[argn].notNull()) {
	    locale = (Locale)(arg[argn].getManagedObject());
	    argn++;
	} else locale = Locale.getDefault();

	if (arg.length > argn && arg[argn].isTimeZone() && arg[argn].notNull()) {
	    timezone = (TimeZone)(arg[argn].getManagedObject());
	    argn++;
	} else timezone = YoixMiscTime.getDefaultTimeZone();

	if (arg.length == argn+1 || arg.length == argn+2 || arg.length == argn+3) {
	    if (arg[argn].isString() && arg[argn].notNull()) {
		date = arg[argn].stringValue();
		if (arg.length == argn+2) {
		    if (arg[argn+1].isString() && arg[argn+1].notNull()) {
			format = arg[argn+1].stringValue();
		    } else if (arg[argn+1].isInteger()) {
			lenient = arg[argn+1].booleanValue();
		    } else VM.badArgument(argn+1);
		} else if (arg.length == argn+3) {
		    if (arg[argn+1].isString() && arg[argn+1].notNull()) {
			if (arg[argn+2].isInteger()) {
			    format = arg[argn+1].stringValue();
			    lenient = arg[argn+2].booleanValue();
			} else VM.badArgument(argn+2);
		    } else VM.badArgument(argn+1);
		}
	    } else VM.badArgument(argn);
	    sdf = new YoixSimpleDateFormat(format, locale);
	    sdf.setTimeZone(timezone);
	    sdf.setLenient(lenient);
	    pos = new ParsePosition(0);
	    try {
		time = sdf.parse(date, pos).getTime();
	    }
	    catch(RuntimeException e) {
		VM.recordException(e);
		VM.abort(BADVALUE, new String[] {"arguments are incompatible"});
	    }
	} else VM.badCall();

	return(YoixObject.newDouble(((double)time)/1000.0));
    }


    public static YoixObject
    parseNumber(YoixObject arg[]) {
	HashMap             hash;
	Locale              locale;
	Number              nbr = null;
	NumberFormat        nf = null;
	YoixObject          output = null;
	YoixObject          dict = null;
	YoixObject          yobj = null;
	YoixObject          yval;
	int                 argn = 0;
	int                 len = 0;
	int                 type = YOIX_DOUBLE_TYPE;

	if (arg.length > argn && arg[argn].isLocale() && arg[argn].notNull()) {
	    locale = (Locale)(arg[argn].getManagedObject());
	    argn++;
	} else locale = Locale.getDefault();

	if (arg.length == argn+1 || arg.length == argn+2) {
	    if (arg[argn].notNull() && arg[argn].isString()) {
		yobj = arg[argn];
		if (arg.length == argn+2) {
		    if (arg[argn+1].isInteger()) {
			switch(type = arg[argn+1].intValue()) {
			case YOIX_DOUBLE_TYPE:
			    nf = NumberFormat.getInstance(locale);
			    break;
			case YOIX_INTEGER_TYPE:
			    nf = NumberFormat.getIntegerInstance(locale);
			    break;
			case YOIX_MONEY_TYPE:
			    nf = NumberFormat.getCurrencyInstance(locale);
			    break;
			case YOIX_PERCENT_TYPE:
			    nf = NumberFormat.getPercentInstance(locale);
			    break;
			default:
			    VM.badArgumentValue(2);
			    break;
			}
		    } else if (arg[argn+1].isDictionary() && arg[argn+1].notNull()) {
			dict = arg[argn+1];
			len = dict.length();
			for (int n = 0; n < len; n++) {
			    if ("type".equals(dict.name(n))) {
				yval = dict.get(n, false);
				if (yval.isInteger()) {
				    switch(type = yval.intValue()) {
				    case YOIX_DOUBLE_TYPE:
					nf = NumberFormat.getInstance(locale);
					break;
				    case YOIX_INTEGER_TYPE:
					nf = NumberFormat.getIntegerInstance(locale);
					break;
				    case YOIX_MONEY_TYPE:
					nf = NumberFormat.getCurrencyInstance(locale);
					break;
				    case YOIX_PERCENT_TYPE:
					nf = NumberFormat.getPercentInstance(locale);
					break;
				    default:
					VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
					break;
				    }
				} else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
				break;
			    }
			}
		    } else VM.badArgument(argn+1);
		}
	    } else VM.badArgument(argn);
	} else VM.badCall();

	if (nf == null)
	    nf = NumberFormat.getInstance(locale);

	if (dict != null) {
	    for (int n = 0; n < len; n++) {
		if ("type".equals(dict.name(n))) {
		    continue;
		} else if ("groupingUsed".equals(dict.name(n))) {
		    yval = dict.get(n, false);
		    if (yval.isInteger())
			nf.setGroupingUsed(yval.booleanValue());
		    else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		} else if ("maximumFractionDigits".equals(dict.name(n))) {
		    yval = dict.get(n, false);
		    if (yval.isInteger())
			nf.setMaximumFractionDigits(yval.intValue());
		    else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		} else if ("maximumIntegerDigits".equals(dict.name(n))) {
		    yval = dict.get(n, false);
		    if (yval.isInteger())
			nf.setMaximumIntegerDigits(yval.intValue());
		    else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		} else if ("minimumFractionDigits".equals(dict.name(n))) {
		    yval = dict.get(n, false);
		    if (yval.isInteger())
			nf.setMinimumFractionDigits(yval.intValue());
		    else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		} else if ("minimumIntegerDigits".equals(dict.name(n))) {
		    yval = dict.get(n, false);
		    if (yval.isInteger())
			nf.setMinimumIntegerDigits(yval.intValue());
		    else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		} else if ("parseIntegerOnly".equals(dict.name(n))) {
		    yval = dict.get(n, false);
		    if (yval.isInteger())
			nf.setParseIntegerOnly(yval.booleanValue());
		    else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		} else if (type != YOIX_INTEGER_TYPE) {
		    if ("decimalSeparatorAlwaysShown".equals(dict.name(n))) {
			yval = dict.get(n, false);
			if (yval.isInteger())
			    ((DecimalFormat)nf).setDecimalSeparatorAlwaysShown(yval.booleanValue());
			else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		    } else if ("groupingSize".equals(dict.name(n))) {
			yval = dict.get(n, false);
			if (yval.isInteger())
			    ((DecimalFormat)nf).setGroupingSize(yval.intValue());
			else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		    } else if ("multiplier".equals(dict.name(n))) {
			yval = dict.get(n, false);
			if (yval.isInteger())
			    ((DecimalFormat)nf).setMultiplier(yval.intValue());
			else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		    } else if ("negativePrefix".equals(dict.name(n))) {
			yval = dict.get(n, false);
			if (yval.isString() && yval.notNull())
			    ((DecimalFormat)nf).setNegativePrefix(yval.stringValue());
			else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		    } else if ("negativeSuffix".equals(dict.name(n))) {
			yval = dict.get(n, false);
			if (yval.isString() && yval.notNull())
			    ((DecimalFormat)nf).setNegativeSuffix(yval.stringValue());
			else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		    } else if ("positivePrefix".equals(dict.name(n))) {
			yval = dict.get(n, false);
			if (yval.isString() && yval.notNull())
			    ((DecimalFormat)nf).setPositivePrefix(yval.stringValue());
			else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		    } else if ("positiveSuffix".equals(dict.name(n))) {
			yval = dict.get(n, false);
			if (yval.isString() && yval.notNull())
			    ((DecimalFormat)nf).setPositiveSuffix(yval.stringValue());
			else VM.badArgumentValue(dict.name(n) + yval.dump("/"), 2);
		    } else VM.badArgumentValue(dict.name(n), 2);
		} else VM.badArgumentValue(dict.name(n), 2);
	    }
	}

	try {
	    nbr = nf.parse(yobj.stringValue());
	    output = YoixObject.newNumber(nbr);
	}
	catch(java.text.ParseException ex) {
	    VM.badArgumentValue(yobj.stringValue(), argn+1);
	}

	return(output == null ? YoixObject.newNull() : output);
    }


    public static YoixObject
    parseTimer(YoixObject arg[]) {

	double  value = 0;

	if (arg.length == 1) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if (arg[0].notNull())
		    value = YoixMiscTime.parseTimer(arg[0].stringValue());
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newDouble(value));
    }


    public static YoixObject
    qsort(YoixObject arg[]) {

	YoixObject  compare;
	YoixObject  funct;
	boolean     inverted;
	Object      keys;
	int         incr;
	int         indices[];
	int         elements;
	int         n;
	int         m;

	if (arg.length >= 1) {
	    if (arg[0].isArray()) {
		keys = arg[0];
		elements = arg[0].sizeof();
		n = 1;
		incr = 1;
		compare = null;
		indices = null;
		inverted = false;
		if (arg.length > 1) {
		    if (arg[n].isCallable() || arg[n].isCallablePointer() || arg[n].isDictionary() || arg[n].isNull()) {
			if (arg[n].isDictionary() && arg[n].isCallablePointer() == false) {
			    keys = buildKeys((YoixObject)keys, arg[n]);
			    inverted = arg[n].getBoolean(N_INVERTED);
			} else compare = arg[n];
			if (compare != null && compare.notNull()) {
			    funct = compare.isCallable() ? compare : compare.get();
			    if (funct.callable(2) == false)
				VM.badArgument(n);
			} else compare = null;
			n++;
		    }
		    if (arg.length > n) {
			if (arg[n].isNumber()) {
			    if ((incr = arg[n].intValue()) <= 0)
				VM.badArgument(n);
			    n++;
			}
		    }
		    if (arg.length > n) {
			for (m = n; m < arg.length; m++) {
			    if (arg[m].isArray() == false || arg[m].sizeof() < elements)
				VM.badArgument(m);
			}
		    }
		}
		if (keys instanceof YoixObject)
		    indices = YoixMiscQsort.qsort((YoixObject)keys, incr, compare);
		else if (keys instanceof String[])
		    indices = YoixMiscQsort.qsort((String[])keys, incr);
		else if (keys instanceof double[])
		    indices = YoixMiscQsort.qsort((double[])keys, incr);
		else VM.abort(UNIMPLEMENTED);
		if (indices != null) {
		    if (keys != arg[0])
			rearrange(arg[0], indices, inverted);
		    for (; n < arg.length; n++)
			rearrange(arg[n], indices, inverted);
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(arg[0]);
    }


    public static YoixObject
    replicate(YoixObject arg[]) {

	YoixObject  result = null;
	int         elems = arg.length - 1;
	int         reps;
	int         size;
	int         i;
	int         j;

	if (arg[elems].isInteger()) {
	    if ((reps = arg[elems].intValue()) >= 0) {
		size = elems * reps;
		result = YoixObject.newArray(size);
		size = 0;
		for (i = 0; i < reps; i++) {
		    for (j = 0; j < elems; j++)
			result.put(size++, arg[j], true);
		}
	    } else VM.badArgumentValue(arg.length - 1);
	} else VM.badArgument(arg.length - 1);

	return(result);
    }


    public static YoixObject
    rollCalendar(YoixObject arg[]) {

	Calendar  calendar;
	boolean   up;
	int       field;

	if (arg[0].isCalendar() && arg[0].notNull()) {
	    if (arg[1].isInteger()) {
		if (arg[2].isInteger()) {
		    calendar = (Calendar)(arg[0].getManagedObject());
		    field = arg[1].intValue();
		    up = arg[2].booleanValue();
		    try {
			calendar.roll(field, up);
		    }
		    catch(IllegalArgumentException e) {
			VM.badArgument(1);
		    }
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(arg[0]);
    }


    public static YoixObject
    sequence(YoixObject arg[]) {

	YoixObject  result = null;
	int         start;
	int         incr;
	int         size;
	int         i;
	int         j;

	if (arg.length <= 3) {
	    if (arg[0].isInteger()) {
		start = 0;
		incr = 1;
		size = arg[0].intValue();
		if (size < 0)
		    VM.badArgumentValue(0);
		if (arg.length > 1) {
		    if (arg[1].isInteger())
			start = arg[1].intValue();
		    else VM.badArgument(1);
		    if (arg.length > 2) {
			if (arg[2].isInteger()) {
			    incr = arg[2].intValue();
			    if (incr == 0)
				VM.badArgumentValue(2);
			} else VM.badArgument(2);
		    }
		}
		result = YoixObject.newArray(size);
		for (i = start, j = 0; j < size; i += incr, j++)
		    result.putInt(j, i);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(result);
    }


    public static YoixObject
    setCalendar(YoixObject arg[]) {

	Calendar  calendar;
	int       ivals[];
	int       n;

	if (arg.length == 4 || arg.length == 6 || arg.length == 7) {
	    if (arg[0].isCalendar() && arg[0].notNull()) {
		calendar = (Calendar)(arg[0].getManagedObject());
		ivals = new int[arg.length - 1];
		for (n = 0; n < ivals.length; n++) {
		    if (arg[n+1].isInteger())
			ivals[n] = arg[n+1].intValue();
		    else VM.badArgument(n+1);
		}
		if (ivals.length == 3)
		    calendar.set(ivals[0], ivals[1], ivals[2]);
		else if (ivals.length == 5)
		    calendar.set(ivals[0], ivals[1], ivals[2], ivals[3], ivals[4]);
		else if (ivals.length == 6)
		    calendar.set(ivals[0], ivals[1], ivals[2], ivals[3], ivals[4], ivals[5]);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(arg[0]);
    }


    public static YoixObject
    setLocale(YoixObject arg[]) {

	YoixObject  obj = null;
	Locale      locale;

	if (arg[0].isLocale() && arg[0].notNull()) {
	    locale = (Locale)(arg[0].getManagedObject());
	    Locale.setDefault(locale);
	    DECIMAL_SEPARATOR = (new java.text.DecimalFormatSymbols()).getDecimalSeparator();
	    obj = arg[0];
	} else VM.badArgument(0);

	return(obj);
    }


    public static YoixObject
    setTimeZone(YoixObject arg[]) {

	TimeZone    timezone;

	// new behavior: allow null (or zero args) to reset to original system default
	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 0 || arg[0].isTimeZone() || arg[0].isNull()) {
		if (arg.length == 0 || arg[0].isNull())
		    timezone = null;
		else timezone = (TimeZone)(arg[0].getManagedObject());
		YoixMiscTime.setDefaultTimeZone(timezone);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(null);
    }


    public static YoixObject
    timerFormat(YoixObject arg[]) {

	String  format;
	String  value = null;
	double  timevalue;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 2) {
		if (arg[0].isString() || arg[0].isNull()) {
		    if (arg[1].isNumber()) {
			format = arg[0].notNull() ? arg[0].stringValue() : TIMER_FORMAT;
			timevalue = arg[1].doubleValue();
			if ((value = YoixMiscTime.timerFormat(format, timevalue)) == null)
			    VM.badArgumentValue(0);
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    } else {
		if (arg[0].isNumber())
		    value = YoixMiscTime.timerFormat(TIMER_FORMAT, arg[0].doubleValue());
		else VM.badArgument(0);
	    }
	} else VM.badCall();

	return(YoixObject.newString(value));
    }


    public static YoixObject
    uniq(YoixObject arg[]) {

	YoixObject  uniqarr = null;
	YoixObject  previous;
	YoixObject  current;
	YoixObject  undefined = YoixObject.newDictionary(0);
	int         off;
	int         sz;
	int         m;
	int         n;

	if (arg[0].isArray() || arg[0].isNull()) {
	    if (arg[0].notNull()) {
		uniqarr = YoixObject.newArray(0);
		uniqarr.setGrowable(true);
		if ((sz = arg[0].sizeof()) > 0) {
		    off = arg[0].offset();
		    if (arg[0].defined(off))
			previous = arg[0].getObject(off);
		    else previous = undefined;
		    for (m = 0, n = 1; n < sz; n++)  {
			if (arg[0].defined(off+n)) {
			    if ((current = arg[0].getObject(off+n)) == null) {
				if (previous == null)
				    continue;
				if (previous == undefined)
				    ((YoixBodyArray)(uniqarr.body())).growTo(++m);
				else uniqarr.putObject(m++, previous);
				previous = current;
			    } else if (current.equals(previous)) {
				continue;
			    } else {
				if (previous == undefined)
				    ((YoixBodyArray)(uniqarr.body())).growTo(++m);
				else uniqarr.putObject(m++, previous);
				previous = current;
			    }
			} else {
			    current = undefined;
			    if (current == previous)
				continue;
			    else {
				uniqarr.putObject(m++, previous);
				previous = current;
			    }
			}
		    }
		    if (previous == undefined)
			((YoixBodyArray)(uniqarr.body())).growTo(++m);
		    else uniqarr.putObject(m++, previous);
		}
	    }
	} else VM.badArgument(0);

	return(uniqarr == null ? YoixObject.newArray() : uniqarr);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static Object
    buildKeys(YoixObject src, YoixObject dict) {

	YoixObject  obj;
	YoixObject  range;
	boolean     autotrim;
	boolean     ignorecase;
	boolean     numeric;
	double      values[];
	String      keys[];
	String      key;
	Object      result;
	int         modifiers[];
	int         length;
	int         offset;
	int         start;
	int         end;
	int         count;
	int         ch;
	int         m;
	int         n;

	result = src;

	if (src.sizeof() > 0) {
	    length = src.length();
	    offset = src.offset();
	    autotrim = dict.getBoolean(N_AUTOTRIM, false);
	    ignorecase = dict.getBoolean(N_IGNORECASE, false);
	    numeric = dict.getBoolean(N_NUMERICSORT);
	    start = 0;
	    end = Integer.MAX_VALUE;
	    if ((range = dict.getObject(N_RANGE)) != null && range.notNull()) {
		if (range.isArray()) {
		    start = Math.max(range.getInt(0, -1), 0);
		    end = Math.max(start, range.getInt(1, Integer.MAX_VALUE - 1)) + 1;
		} else if (range.isNumber())
		    start = Math.max(range.intValue(), 0);
		else range = null;
		if (start == 0 && end == Integer.MAX_VALUE)
		    range = null;
	    }
	    keys = new String[src.sizeof()];
	    for (n = offset, m = 0; n < length; n++, m++) {
		if ((obj = src.getObject(n)) != null) {
		    if (obj.isString() && obj.notNull()) {
			key = obj.stringValue();
			if (range != null) {
			    count = key.length();
			    if (start < count) {
				if (end < count)
				    key = key.substring(start, end);
				else key = key.substring(start);
			    }
			}
			if (autotrim)
			    key = key.trim();
			if (ignorecase)
			    key = key.toLowerCase();
			keys[m] = key;
		    } else keys[m] = null;
		} else keys[m] = null;
	    }
	    if (numeric) {
		if ((obj = dict.getObject(N_MODIFIERS)) != null) {
		    if (obj.isArray()) {
			modifiers = new int[128];	// assume 7bit ascii for now
			for (n = 0; n < modifiers.length; n++)
			    modifiers[n] = 1;
			length = obj.length();
			for (n = obj.offset(); n < length - 1; n += 2) {
			    ch = obj.getInt(n, -1);
			    if (ch >= 0 && ch < modifiers.length)
				modifiers[ch] = obj.getInt(n+1, 1);
			}
		    } else modifiers = null;
		} else modifiers = null;
		length = keys.length;
		values = new double[length];
		for (n = 0; n < length; n++) {
		    if ((key = keys[n]) != null)
			values[n] = parseNumber(key, modifiers);
		    else values[n] = Double.POSITIVE_INFINITY;
		}
		result = values;
	    } else result = keys;
	}
	return(result);
    }


    private static boolean
    isDirectory(String path) {

	boolean check = false;

	//
	// Really only needed to catch SecurityExceptions that can sometimes
	// be triggered in one of our builtins.
	//

	try {
	    check = (new File(path)).isDirectory();
	}
	catch(Exception e) {
	    VM.recordException(e);
	}

	return(check);
    }


    private static boolean
    isFile(String path) {

	boolean check = false;

	//
	// Really only needed to catch SecurityExceptions that can sometimes
	// be triggered in one of our builtins.
	//

	try {
	    check = (new File(path)).isFile();
	}
	catch(Exception e) {
	    VM.recordException(e);
	}

	return(check);
    }


    private static double
    parseNumber(String str, int modifiers[]) {

	double  value = 0;
	int     lastch = -1;
	int     length;

	//
	// Using try/catch to set lastch works with Integer.parseInt()
	// but it won't always work with Double.valueOf(). We decided
	// to ignore the issues and restrict this to integers for now,
	// because that's sufficient for the application that currently
	// this stuff.
	//

	for (length = str.length(); length > 0; length--) {
	    try {
		value = Integer.parseInt(str, 10);
		break;
	    }
	    catch(NumberFormatException e) {
		lastch = str.charAt(length - 1);
		str = str.substring(0, length - 1);
	    }
	}

	if (modifiers != null) {
	    if (lastch >= 0 && lastch < modifiers.length)
		value *= modifiers[lastch];
	}
	return(value);
    }


    private static void
    rearrange(YoixObject obj, int indices[], boolean inverted) {

	YoixObject  old[];
	int         offset;
	int         length;
	int	    n;
	int         m;

	offset = obj.offset();
	length = obj.length();
	old = new YoixObject[length];
	for (n = offset; n < length; n++)
	    old[n] = obj.get(n, false);
	if (inverted) {
	    for (n = offset, m = length - 1; n < length; n++, m--)
		obj.put(m, old[indices[n]], false);
	} else {
	    for (n = offset; n < length; n++)
		obj.put(n, old[indices[n]], false);
	}
    }
}

