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

package att.research.yoix.ydat;
import java.text.*;
import java.util.*;
import att.research.yoix.*;

public
class MiscTime extends GregorianCalendar

    implements YoixConstants

{

    //
    // This class extends GregorianCalendar so it can access protected
    // methods setTimeInMillis() and getTimeInMillis(). In addition we
    // decided that a single GregorianCalendar would be sufficient, as
    // long it's protected by a synchronized statement.
    //

    private static final MiscTime  calendar = new MiscTime();

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    private
    MiscTime() {

	super(YoixMiscTime.getDefaultTimeZone(), Locale.getDefault());
    }

    ///////////////////////////////////
    //
    // MiscTime Methods
    //
    ///////////////////////////////////

    static String
    getDate(String format, double utime) {

	return(getDate(new YoixSimpleDateFormat(format), utime));
    }


    static String
    getDate(YoixSimpleDateFormat sdf, double utime) {

	String  str;

	synchronized(calendar) {
	    if (utime != calendar.getUTime())
		calendar.setUTime(utime);
	    str = sdf.format(calendar.getTime());
	}
	return(str);
    }


    static boolean
    isPeakTime(double utime, int peakdays[], double start, double stop, double holidays[]) {

	boolean  result;
	double   hour;
	int      day;
	int      n;

	//
	// A stop hour that's less than the start hour means the endpoints
	// are in different days. When that happens we also assume that an
	// interval is only active when it starts in a day that peakdays[]
	// accepts.
	// 

	if (result = (start != stop)) {
	    if (holidays != null) {
		for (n = 0; n < holidays.length && utime >= holidays[n]; n += 2) {
		    if (utime < holidays[n+1]) {
			result = false;
			break;
		    }
		}
	    }
	    if (result) {
		synchronized(calendar) {
		    hour = getHour(utime);
		    day = getWeekday(utime);
		    if (start > stop) {
			if (hour >= start)
			    result = (peakdays[day] == 0);
			else if (hour < stop)
			    result = (peakdays[(day+6)%7] == 0);
			else result = false;
		    } else {
			if (peakdays[day] == 0)
			    result = (hour >= start && hour < stop);
			else result = false;
		    }
		}
	    }
	}
	return(result);
    }


    static double[]
    setHolidays(double holidays[]) {

	double  days[];
	int     len;
	int     i;
	int     j;
	int     k;
	int     l;

	synchronized(calendar) {
	    if (holidays != null && holidays.length > 0) {
		if (holidays.length%2 == 1) {
		    days = new double[holidays.length+1];
		    System.arraycopy(holidays, 0, days, 0, holidays.length);
		    //
		    // Leave some slack at the end in case we add days to
		    // get to a peakday.
		    //
		    days[holidays.length] = Long.MAX_VALUE - 864000;
		} else days = (double[])(holidays.clone());
		YoixMiscQsort.sort(days, 2);
		len = days.length;
		for (i = 2, j = 0; i < len;) {
		    while (i < len && days[j+1] >= days[i]) {
			days[j+1] = days[i+1];
			i += 2;
		    }
		    j += 2;
		    if (i != j) {
			// shift up (i-j) slots
			for (k = i, l = j; k < len; k += 2, l += 2) {
			    days[l] = days[k];
			    days[l+1] = days[k+1];
			}
			len -= (i - j);
			i = j;
			j -= 2;
		    } else i += 2;
		}
		if (len < days.length) {
		    holidays = new double[len];
		    System.arraycopy(days, 0, holidays, 0, len);
		    days = holidays;
		}
	    } else days = null;
	}
	return(days);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static double
    getHour(double utime) {

	double  hour;

	synchronized(calendar) {
	    if (utime != calendar.getUTime())
		calendar.setUTime(utime);
	    hour = (double)(calendar.get(HOUR_OF_DAY));
	    hour += ((double)(calendar.get(MINUTE)))/60.0;
	}
	return(hour);
    }


    private static double
    getUTime() {

	return(((double)calendar.getTimeInMillis())/1000.0);
    }


    private static int
    getWeekday(double utime) {

	int  day;

	synchronized(calendar) {
	    if (utime != calendar.getUTime())
		calendar.setUTime(utime);
	    day = calendar.get(DAY_OF_WEEK) - 1;
	}
	return(day);
    }


    private static void
    setUTime(double utime) {

	calendar.setTimeInMillis((long)(utime*1000.0));
    }
}

