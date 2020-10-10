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
import java.lang.reflect.*;
import java.text.*;
import java.util.*;

public
class YoixMiscTime

    implements YoixAPI,
	       YoixConstants

{

    //
    // JDK1.5 screwed up TimeZone.[gs]etDefault(), so we get it once and
    // maintain our own after that.
    //

    private static TimeZone  timezone_default = null;
    private static TimeZone  timezone_system = null;

    //
    // Yoix currently represents time using doubles, which means we should
    // have about 52 bits before we have to start worrying about overflow.
    // It's more than adequate to represent the time since Jan 1 1970 in
    // milliseconds because:
    //
    //    (2^52)/(1000*(365*24*60*60)) ~ 142808 years
    //
    // However we can get into trouble with nanoseconds because,
    //
    //    (2^52)/(1000000000*(24*60*60)) ~ 52 days
    //
    // so we can't accurately represent the number of nanoseconds since
    // Jan 1 1970 using the 52 available bits in a double. On the other
    // hand, 50 days seems more than adequate if we're only interested in
    // measuring nanosecond time intervals, so all we have to do is save
    // a start time, as measured by Java's System.nanoTime(), and use it
    // as the epoch whenever we deal with nanoseconds.
    //
    // Incidentally, System.nanoTime() requires JDK1.5 or greater, so we
    // use reflecation and use System.currentTimeMillis() when necessary.
    //

    private static Method  nanotime = null;
    private static long    nanoepoch = 0;

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public static TimeZone
    getDefaultTimeZone() {

	if (timezone_system == null)
	    initTimeZones();
	return((TimeZone)timezone_default.clone());
    }


    public static long
    getNanoTime() {

	Number  nanos;
	long    time;

	if (initNanoTime()) {
	    try {
		nanos = (Number)nanotime.invoke(System.class, null);
		time = nanos.longValue();
	    }
	    catch(Exception e) {
		time = System.currentTimeMillis() * 1000000L;
	    }
	} else time = System.currentTimeMillis() * 1000000L;

	return(time - nanoepoch);
    }


    public static double
    parseDate(String str, YoixSimpleDateFormat sdf) {

	return(parseDate(str, sdf, Double.NaN));
    }


    public static double
    parseDate(String str, YoixSimpleDateFormat sdf, double time) {

	if (str != null && sdf != null) {
	    try {
		time = ((double)sdf.parse(str, new ParsePosition(0)).getTime())/1000.0;
	    }
	    catch(RuntimeException e) {}
	}
	return(time);
    }


    public static double
    parseTimer(String str) {

	boolean  negative = false;
	String   letters = "yYtTdDhHmMsS";
	double   duration = 0;
	double   value;
	int      length;
	int      start;
	int      index;

	if (str != null) {
	    str = str.trim();
	    if (str.indexOf("-") == 0 && str.length() > 1) {
		negative = true;
		str = str.substring(1);
	    }
	    length = str.length();
	    for (start = 0; (index = nextLetter(str, letters, start+1, length)) < length; start = index) {
		value = YoixMake.javaDouble(str.substring(start, index), 0.0);
		switch (str.charAt(index)) {
		    case 'y':
		    case 'Y':
			duration += 31104000*value;
			break;

		    case 't':
		    case 'T':
			duration += 2592000*value;
			break;

		    case 'd':
		    case 'D':
			duration += 86400*value;
			break;

		    case 'h':
		    case 'H':
			duration += 3600*value;
			break;

		    case 'm':
		    case 'M':
			duration += 60*value;
			break;

		    case 's':
		    case 'S':
			duration += value;
			break;
		}
		index++;
	    }
	    if (negative)
		duration = -duration;
	}
	return(duration);
    }


    public static void
    setDefaultTimeZone(TimeZone tz) {

	if (timezone_system == null)
	    initTimeZones();

	synchronized(timezone_system) {
	    if (tz == null)
		timezone_default = (TimeZone)timezone_system.clone();
	    else timezone_default = (TimeZone)tz.clone();
	}
    }


    public static String
    timerFormat(String format, double timevalue) {

	StringBuffer  buf;
	YoixObject    args[];
	ArrayList     vargs;
	boolean       negative = false;
	boolean       suppress = false;
	boolean       suppressed = false;
	boolean       optional = false;
	boolean       zerofill = false;
	boolean       blankfill = false;
	boolean       valid = true;
	Object        formatted[];
	Object        info[];
	String        output = null;
	long          tmpval;
	long          value;
	long          ms_t;
	int           ms;
	int           ms_r;
	int           sc;
	int           sc_t;
	int           mn;
	int           mn_t;
	int           hr;
	int           hr_t;
	int           dy;
	int           dy_t;
	int           mt;
	int           mt_t;
	int           yr;
	int           yr_t;
	int           i;
	int           j;
	int           len;
	int           c;
	int           cn;
	int           fmt;
	int           first;
	int           leader;
	int           size;
	boolean       rounded;

	//
	// We intentionally round fractional seconds and total values, but
	// take the floor for others. In addition, we no longer abort when
	// something goes wrong, but instead we return a null value and let
	// the caller decide how to handle it. The change was made as part
	// of a general error reporting cleanup that tries to restrict the
	// use of BADCALL and BADARGUMENT errors to functions or builtins.
	//

	if (format != null) {
	    if (timevalue < 0) {
		timevalue = -timevalue;
		negative = true;
	    }
	    leader = first = 0;
	    value = (long)timevalue;
	    ms_t = Math.round(timevalue*1000.0);
	    ms = (int)Math.round((timevalue - ((double)value))*1000.0);
	    sc_t = (int)Math.round(timevalue);
	    sc = (int)(value%60L);
	    value /= 60L;
	    mn_t = (int)Math.round(timevalue/60.0);
	    mn = (int)(value%60L);
	    value /= 60L;
	    hr_t = (int)Math.round(timevalue/3600.0);
	    hr = (int)(value%24L);
	    dy_t = (int)Math.round(timevalue/86400.0);
	    value /= 24L;
	    dy = (int)(value%30L);
	    mt_t = (int)Math.round(timevalue/(2592000.0));
	    value /= 30L;
	    mt = (int)(value%12L);
	    yr_t = (int)Math.round(timevalue/(31104000.0));
	    value /= 12L;
	    yr = (int)value;
	    len = format.length();
	    if (len == 1 && format.equalsIgnoreCase("c")) {
		// special case (compact format)
		if (yr > 0)
		    output = yr + "y";
		else if (mt > 0)
		    output = mt + "t";
		else if (dy > 0)
		    output = dy + "d";
		else if (hr > 0)
		    output = hr + "h";
		else if (mn > 0)
		    output = mn + "m";
		else if (sc > 0)
		    output = sc + "s";
		else if (ms > 0)
		    output = ms + "ms";
		else output = "0s";
		if (negative)
		    output = "-" + output;
	    } else if (len > 0) {
		vargs = new ArrayList();
		buf = new StringBuffer();
		if (negative)
		    buf.append('-');
		suppress = false;
		fmt = 0;
		for (i = 0; i < len && valid; i++) {
		    c = format.charAt(i);
		    switch (c) {
			case '\'':
			    i++;
			    if (i < len) {
				c = format.charAt(i);
				if (!suppress) {
				    if (c == '%')
					buf.append('%');
				    buf.append((char)c);
				}
				if (c != '\'') {
				    i++;
				    for (; i < len; i++) {
					c = format.charAt(i);
					if (c == '\'')
					    break;
					if (!suppress) {
					    if (c == '%')
						buf.append('%');
					    buf.append((char)c);
					}
				    }
				}
			    }
			    suppress = false;
			    break;

			case 'N':
			case 'n':
			    if (first == 0)
				leader = first = YoixMiscCtype.tolower(c);
			    break;

			case 'Y':
			case 'y':
			case 'T':
			case 't':
			case 'D':
			case 'd':
			case 'F':
			case 'f':
			case 'H':
			case 'h':
			case 'M':
			case 'm':
			case 'S':
			case 's':
			    if (first == 0)
				leader = first = YoixMiscCtype.tolower(c);
			    // fall through to...
			case 'B':
			case 'b':
			case 'Z':
			case 'z':
			    blankfill = false;
			    optional = false;
			    suppress = false;
			    suppressed = false;
			    zerofill = false;
			    if (YoixMiscCtype.isupper(c)) {
				suppress = true;
				suppressed = true;
			    }
			    if (c == 'Z' || c == 'z')
				zerofill = true;
			    else if (c == 'B' || c == 'b')
				blankfill = true;
			    fmt = 1;
			    cn = c;
			    while ((i+1) < len && c == (cn = format.charAt(i+1))) {
				fmt++;
				i++;
			    }
			    if (blankfill || zerofill) {
				if ((i+1) < len) {
				    switch (cn) {
					case 'Z':
					case 'z':
					case 'B':
					case 'b':
					    valid = false;
					    break;

				        case 'Y':
				        case 'y':
				        case 'T':
				        case 't':
					case 'D':
					case 'd':
					case 'F':
					case 'f':
					case 'H':
					case 'h':
					case 'M':
					case 'm':
					case 'S':
					case 's':
					    if (first == 0)
						leader = first = YoixMiscCtype.tolower(cn);
					    break;

					default:
					    valid = false;
					    break;
				    }
				    c = cn;
				    if (YoixMiscCtype.islower(c))
					suppress = false;
				    else suppress = true;
				    fmt++;
				    i++;
				} else valid = false;
			    } else if ((i+1) < len && suppress) {
				if ((c+YoixMiscCtype.UPPERTOLOWER) != cn) {
				    switch (cn) {
					case 'Z':
					case 'z':
					    zerofill = true;
					    optional = true;
					    fmt++;
					    i++;
					    break;

					case 'B':
					case 'b':
					    blankfill = true;
					    optional = true;
					    fmt++;
					    i++;
					    break;

					default:
					    break;
				    }
				} else {
				    c = cn;
				    optional = true;
				    fmt++;
				    i++;
				}
			    }
			    value = -1L;
			    rounded = false;
			    switch (c) {
				case 'F':
				case 'f':
				    if (value < 0) {
					if (fmt > 3)
					    value = ms_t;
					else if (first == YoixMiscCtype.tolower(c)) {
					    if (fmt > 2)
						value = ms_t;
					    else if (fmt > 1)
						value = Math.round(timevalue*100.0);
					    else value = Math.round(timevalue*10.0);
					} else {
					    ms_r = 0;
					    if (fmt > 2)
						value = ms;
					    else if (fmt > 1) {
						value = Math.round(timevalue*100.0);
						tmpval = (long)(timevalue*100.0);
						if (value != tmpval) {
						    value %= 100;
						    if (value == 0)
							ms_r = 1;
						} else value %= 100;
					    } else {
						value = Math.round(timevalue*10.0);
						tmpval = (long)(timevalue*10.0);
						if (value != tmpval) {
						    value %= 10;
						    if (value == 0)
							ms_r = 1;
						} else value %= 10;
					    }
					    if (ms_r == 1) {
						int   vc;
						long  vv;
						// add an extra second as needed
						for (size = vargs.size() - 1; size >= 0; size--) {
						    info = (Object[])vargs.get(size);
						    vc = ((Integer)info[1]).intValue();
						    switch(vc) {
						    case 's':
							if (!((Boolean)info[2]).booleanValue()) {
							    vv = ((YoixObject)info[0]).longValue();
							    if (leader == vc || vv < 59)
								vv++;
							    else vv = 0;
							    info[0] = YoixObject.newNumber((double)vv);
							}
							break;
						    case 'm':
							if (sc == 59) {
							    if (!((Boolean)info[2]).booleanValue()) {
								vv = ((YoixObject)info[0]).longValue();
								if (leader == vc || vv < 59)
								    vv++;
								else vv = 0;
								info[0] = YoixObject.newNumber((double)vv);
							    }
							}
							break;
						    case 'h':
							if (sc == 59 && mn == 59) {
							    if (!((Boolean)info[2]).booleanValue()) {
								vv = ((YoixObject)info[0]).longValue();
								if (leader == vc || vv < 23)
								    vv++;
								else vv = 0;
								info[0] = YoixObject.newNumber((double)vv);
							    }
							}
							break;
						    case 'd':
							if (sc == 59 && mn == 59 && hr == 23) {
							    if (!((Boolean)info[2]).booleanValue()) {
								vv = ((YoixObject)info[0]).longValue();
								if (leader == vc || vv < 29)
								    vv++;
								info[0] = YoixObject.newNumber((double)vv);
							    }
							}
							break;
						    case 't':
							if (sc == 59 && mn == 59 && hr == 23 && dy == 29) {
							    if (!((Boolean)info[2]).booleanValue()) {
								vv = ((YoixObject)info[0]).longValue();
								if (leader == vc || vv < 12)
								    vv++;
								info[0] = YoixObject.newNumber((double)vv);
							    }
							}
							break;
						    case 'y':
							if (sc == 59 && mn == 59 && hr == 23 && dy == 29 && mt == 11) {
							    if (!((Boolean)info[2]).booleanValue()) {
								vv = ((YoixObject)info[0]).longValue();
								vv++;
								info[0] = YoixObject.newNumber((double)vv);
							    }
							}
							break;
						    }
						}
					    }
					}
				    }
				    if (value > 0)
					suppress = false;
				    if (sc > 0)
					suppressed = false;
				    // fall through to...
				case 'S':
				case 's':
				    if (value < 0) {
					if (fmt > 2) {
					    value = sc_t;
					    rounded = true;
					} else if (first == YoixMiscCtype.tolower(c))
					    value = sc + 60*mn + 3600*hr + 86400*dy + 2592000*mt + 31104000*yr;
					else value = sc;
				    }
				    if (value > 0)
					suppress = false;
				    if (mn > 0)
					suppressed = false;
				    // fall through to...
				case 'M':
				case 'm':
				    if (value < 0) {
					if (fmt > 2) {
					    value = mn_t;
					    rounded = true;
					} else if (first == YoixMiscCtype.tolower(c))
					    value = mn + 60*hr + 1440*dy + 43200*mt + 518400*yr;
					else value = mn;
				    }
				    if (value > 0)
					suppress = false;
				    if (hr > 0)
					suppressed = false;
				    // fall through to...
				case 'H':
				case 'h':
				    if (value < 0) {
					if (fmt > 2) {
					    value = hr_t;
					    rounded = true;
					} else if (first == YoixMiscCtype.tolower(c))
					    value = hr + 24*dy + 720*mt + 8640*yr;
					else value = hr;
				    }
				    if (value > 0)
					suppress = false;
				    if (dy > 0)
					suppressed = false;
				    // fall through to...
				case 'D':
				case 'd':
				    if (value < 0) {
					if (fmt > 2) {
					    value = dy_t;
					    rounded = true;
					} else if (first == YoixMiscCtype.tolower(c))
					    value = dy + 30*mt + 360*yr;
					else value = dy;
				    }
				    if (value > 0)
					suppress = false;
				    if (mt > 0)
					suppressed = false;
				    // fall through to...
				case 'T':
				case 't':
				    if (value < 0) {
					if (fmt > 2) {
					    value = mt_t;
					    rounded = true;
					} else if (first == YoixMiscCtype.tolower(c))
					    value = mt + 12*yr;
					else value = mt;
				    }
				    if (value > 0)
					suppress = false;
				    if (yr > 0)
					suppressed = false;
				    // fall through to...
				case 'Y':
				case 'y':
				    if (value < 0) {
					if (fmt > 2 && !blankfill && !zerofill) {
					    value = yr_t;
					    rounded = true;
					} else value = yr;
				    }
				    if (value > 0)
					suppress = false;
				    break;

				default:
				    valid = false;
				    break;
			    }
			    if (valid) {
				first = 'n';
				if (optional && value == 0L) {
				    suppress = true;
				    suppressed = true;
				}
				if (!suppress || !suppressed) {
				    suppress = false;
				    buf.append('%');
				    if (fmt > 1 && !suppressed && (zerofill || blankfill)) {
					if (zerofill)
					    buf.append('0');
					buf.append("" + fmt);
				    }
				    buf.append('d');
				    vargs.add(
					new Object[] {
					    YoixObject.newNumber((double)value),
					    new Integer(YoixMiscCtype.tolower(c)),
					    new Boolean(rounded)
					}
				    );
				}
			    }
			    break;

			default:
			    if (!YoixMiscCtype.isalpha(c)) {
				if (!suppress || !suppressed) {
				    if (c == '%')
					buf.append('%');
				    buf.append((char)c);
				}
				suppress = false;
			    } else valid = false;
			    break;
		    }
		}
		if (valid) {
		    size = vargs.size();
		    args = new YoixObject[size + 1];
		    args[0] = YoixObject.newString(buf.toString());
		    for (i = 0; i < size; i++) {
			info = (Object[])vargs.get(i);
			args[i+1] = (YoixObject)info[0];
		    }
		    formatted = YoixMiscPrintf.format(args, 0);
		    output = (String)formatted[0];
		} else output = null;
	    } else output = "";
	} else output = null;

	return(valid ? output : null);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static boolean
    initNanoTime() {

	Number  nanos;

	if (nanotime == null && nanoepoch <= 0) {
	    try {
		nanotime = System.class.getDeclaredMethod("nanoTime", null);
		try {
		    nanos = (Number)nanotime.invoke(System.class, null);
		    nanoepoch = nanos.longValue();
		}
		catch(Exception ee) {
		    nanotime = null;
		    nanoepoch = System.currentTimeMillis()*1000000L;
		}
	    }
	    catch(Exception e) {
		nanotime = null;
		nanoepoch = System.currentTimeMillis()*1000000L;
	    }
	}
	return(nanotime != null);
    }


    private static synchronized void
    initTimeZones() {

	if (timezone_system == null) {
	    timezone_system = TimeZone.getDefault();
	    timezone_default = TimeZone.getDefault();
	}
    }


    private static int
    nextLetter(String str, String letters, int index, int length) {

	for (; index < length; index++) {
	    if (letters.indexOf(str.charAt(index)) >= 0)
		break;
	}
	return(index);
    }
}

