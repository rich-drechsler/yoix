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
import java.util.*;

final
class YoixBodyCalendar extends YoixPointerActive

    implements YoixConstants

{

    private Calendar  calendar;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD                          OBJECT       BODY
     // -----                          ------       ----
	N_AMPM,                        $LR__,       null,
	N_DAYOFWEEK,                   $LR__,       null,
	N_DAYOFWEEKINMONTH,            $LR__,       null,
	N_DSTOFFSET,                   $LR__,       null,
	N_HOUR,                        $LR__,       null,
	N_LEAPYEAR,                    $LR__,       null,
	N_WEEKOFMONTH,                 $LR__,       null,
	N_WEEKOFYEAR,                  $LR__,       null,
	N_ZONEOFFSET,                  $LR__,       null,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(30);

    static {
	activefields.put(N_ADD, new Integer(V_ADD));
	activefields.put(N_AMPM, new Integer(V_AMPM));
	activefields.put(N_DATE, new Integer(V_DATE));
	activefields.put(N_DAYOFMONTH, new Integer(V_DAYOFMONTH));
	activefields.put(N_DAYOFWEEK, new Integer(V_DAYOFWEEK));
	activefields.put(N_DAYOFWEEKINMONTH, new Integer(V_DAYOFWEEKINMONTH));
	activefields.put(N_DAYOFYEAR, new Integer(V_DAYOFYEAR));
	activefields.put(N_DSTOFFSET, new Integer(V_DSTOFFSET));
	activefields.put(N_ERA, new Integer(V_ERA));
	activefields.put(N_FIRSTDAYOFWEEK, new Integer(V_FIRSTDAYOFWEEK));
	activefields.put(N_GREGORIANCHANGE, new Integer(V_GREGORIANCHANGE));
	activefields.put(N_HOUR, new Integer(V_HOUR));
	activefields.put(N_HOUROFDAY, new Integer(V_HOUROFDAY));
	activefields.put(N_LEAPYEAR, new Integer(V_LEAPYEAR));
	activefields.put(N_LEASTMAXIMUM, new Integer(V_LEASTMAXIMUM));
	activefields.put(N_LENIENT, new Integer(V_LENIENT));
	activefields.put(N_LOCALE, new Integer(V_LOCALE));
	activefields.put(N_MAXIMUM, new Integer(V_MAXIMUM));
	activefields.put(N_MILLISECOND, new Integer(V_MILLISECOND));
	activefields.put(N_MINDAYSINFIRSTWEEK, new Integer(V_MINDAYSINFIRSTWEEK));
	activefields.put(N_MINIMUM, new Integer(V_MINIMUM));
	activefields.put(N_MINUTE, new Integer(V_MINUTE));
	activefields.put(N_MONTH, new Integer(V_MONTH));
	activefields.put(N_ROLL, new Integer(V_ROLL));
	activefields.put(N_SECOND, new Integer(V_SECOND));
	activefields.put(N_SET, new Integer(V_SET));
	activefields.put(N_TIMEZONE, new Integer(V_TIMEZONE));
	activefields.put(N_UNIXTIME, new Integer(V_UNIXTIME));
	activefields.put(N_WEEKOFMONTH, new Integer(V_WEEKOFMONTH));
	activefields.put(N_WEEKOFYEAR, new Integer(V_WEEKOFYEAR));
	activefields.put(N_YEAR, new Integer(V_YEAR));
	activefields.put(N_ZONEOFFSET, new Integer(V_ZONEOFFSET));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyCalendar(YoixObject data) {

	this(data, null);
    }


    YoixBodyCalendar(YoixObject data, Calendar calendar) {

	super(data);

	if (calendar != null) {
	    this.calendar = calendar;
	    data.put(N_TIMEZONE, YoixObject.newTimeZone(calendar.getTimeZone()), false);
	    data.put(N_LOCALE, YoixObject.newLocale(), false);
	} else buildCalendar();

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

	return(CALENDAR);
    }

    ///////////////////////////////////
    //
    // YoixBodyCalendar Methods
    //
    ///////////////////////////////////

    protected YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_ADD:
		obj = builtinAdd(name, argv);
		break;

	    case V_LEASTMAXIMUM:
		obj = builtinLeastMaximum(name, argv);
		break;

	    case V_MAXIMUM:
		obj = builtinMaximum(name, argv);
		break;

	    case V_MINIMUM:
		obj = builtinMinimum(name, argv);
		break;

	    case V_ROLL:
		obj = builtinRoll(name, argv);
		break;

	    case V_SET:
		obj = builtinSet(name, argv);
		break;

	    default:
		obj = null;
		break;
	}
	return(obj);
    }


    protected final void
    finalize() {

	calendar = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	Calendar  calendar;

	//
	// Still looks like there's room for simplification. Could use
	// a static Hashtable to map our V_XXX constants to appropriate
	// Calendar constants. Would let us combine lots off cases, but
	// probably not worth the effort right now.
	//

	calendar = this.calendar;

	try {
	    switch (activeField(name, activefields)) {
		case V_AMPM:
		    obj = getCalendarField(calendar, Calendar.AM_PM, -1);
		    break;

		case V_DATE:
		    obj = getCalendarField(calendar, Calendar.DATE, -1);
		    break;

		case V_DAYOFMONTH:
		    obj = getCalendarField(calendar, Calendar.DAY_OF_MONTH, -1);
		    break;

		case V_DAYOFWEEK:
		    obj = getCalendarField(calendar, Calendar.DAY_OF_WEEK, -1);
		    break;

		case V_DAYOFWEEKINMONTH:
		    obj = getCalendarField(calendar, Calendar.DAY_OF_WEEK_IN_MONTH, -1);
		    break;

		case V_DAYOFYEAR:
		    obj = getCalendarField(calendar, Calendar.DAY_OF_YEAR, -1);
		    break;

		case V_DSTOFFSET:
		    obj = getCalendarField(calendar, Calendar.DST_OFFSET, -1);
		    break;

		case V_ERA:
		    obj = getCalendarField(calendar, Calendar.ERA, -1);
		    break;

		case V_FIRSTDAYOFWEEK:
		    obj = YoixObject.newInt(calendar.getFirstDayOfWeek());
		    break;

		case V_GREGORIANCHANGE:
		    if (calendar instanceof GregorianCalendar) {
			obj = YoixObject.newDouble(
			    ((GregorianCalendar)calendar).getGregorianChange().getTime()/1000.0
			);
		    } else obj = YoixObject.newInt(-1);
		    break;

		case V_HOUR:
		    obj = getCalendarField(calendar, Calendar.HOUR, -1);
		    break;

		case V_HOUROFDAY:
		    obj = getCalendarField(calendar, Calendar.HOUR_OF_DAY, -1);
		    break;

		case V_LEAPYEAR:
		    if (calendar instanceof GregorianCalendar) {
			if (calendar.isSet(Calendar.YEAR))
			    obj = YoixObject.newInt(
				((GregorianCalendar)calendar).isLeapYear(calendar.get(Calendar.YEAR))
			    );
			else obj = YoixObject.newInt(0);
		    } else obj = YoixObject.newInt(0);
		    break;

		case V_LENIENT:
		    obj = YoixObject.newInt(calendar.isLenient());
		    break;

		case V_LOCALE:
		    obj = YoixObject.newLocale(YoixMake.javaLocale(obj));
		    break;

		case V_MILLISECOND:
		    obj = getCalendarField(calendar, Calendar.MILLISECOND, -1);
		    break;

		case V_MINDAYSINFIRSTWEEK:
		    obj = YoixObject.newInt(calendar.getMinimalDaysInFirstWeek());
		    break;

		case V_MINUTE:
		    obj = getCalendarField(calendar, Calendar.MINUTE, -1);
		    break;

		case V_MONTH:
		    obj = getCalendarField(calendar, Calendar.MONTH, -1);
		    break;

		case V_SECOND:
		    obj = getCalendarField(calendar, Calendar.SECOND, -1);
		    break;

		case V_TIMEZONE:
		    obj = YoixObject.newTimeZone(calendar.getTimeZone());
		    break;

		case V_UNIXTIME:
		    obj = YoixObject.newDouble(((double)(calendar.getTime().getTime()))/1000.0);
		    break;

		case V_WEEKOFMONTH:
		    obj = getCalendarField(calendar, Calendar.WEEK_OF_MONTH, -1);
		    break;

		case V_WEEKOFYEAR:
		    obj = getCalendarField(calendar, Calendar.WEEK_OF_YEAR, -1);
		    break;

		case V_YEAR:
		    obj = getCalendarField(calendar, Calendar.YEAR, -1);
		    break;

		case V_ZONEOFFSET:
		    obj = getCalendarField(calendar, Calendar.ZONE_OFFSET, -1);
		    break;
	    }
	}
	catch(IllegalArgumentException e) {
	    if (calendar.isLenient())
		VM.abort(BADVALUE, name);
	    else VM.abort(BADVALUE, name, new String[] {"check " + N_LENIENT + " field"});
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(calendar);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	Calendar  calendar;
	Calendar  cal;
	Locale    locale;

	calendar = this.calendar;

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_DATE:
		case V_DAYOFMONTH:
		    calendar.set(Calendar.DAY_OF_MONTH, obj.intValue());
		    break;

		case V_DAYOFYEAR:
		    calendar.set(Calendar.DAY_OF_YEAR, obj.intValue());
		    break;

		case V_ERA:
		    calendar.set(Calendar.ERA, obj.intValue());
		    break;

	        case V_FIRSTDAYOFWEEK:
		    calendar.setFirstDayOfWeek(obj.intValue());
		    triggerRecompute(calendar);
		    break;

		case V_GREGORIANCHANGE:
		    if (calendar instanceof GregorianCalendar) {
			((GregorianCalendar)calendar).setGregorianChange(
			    new Date((long)(1000.0*obj.doubleValue()))
			);
			triggerRecompute(calendar);
		    }
		    break;

		case V_HOUROFDAY:
		    calendar.set(Calendar.HOUR_OF_DAY, obj.intValue());
		    break;

		case V_LENIENT:
		    calendar.setLenient(obj.booleanValue());
		    break;

		case V_LOCALE:
		    locale = YoixMake.javaLocale(obj);
		    data.put(N_LOCALE, YoixObject.newLocale(locale), false);
		    cal = Calendar.getInstance(locale);
		    calendar.setFirstDayOfWeek(cal.getFirstDayOfWeek());
		    calendar.setMinimalDaysInFirstWeek(cal.getMinimalDaysInFirstWeek());
		    triggerRecompute(calendar);
		    break;

		case V_MILLISECOND:
		    calendar.set(Calendar.MILLISECOND, obj.intValue());
		    break;

	        case V_MINDAYSINFIRSTWEEK:
		    calendar.setMinimalDaysInFirstWeek(obj.intValue());
		    triggerRecompute(calendar);
		    break;

		case V_MINUTE:
		    calendar.set(Calendar.MINUTE, obj.intValue());
		    break;

		case V_MONTH:
		    calendar.set(Calendar.MONTH, obj.intValue());
		    break;

		case V_SECOND:
		    calendar.set(Calendar.SECOND, obj.intValue());
		    break;

		case V_TIMEZONE:
		    calendar.setTimeZone(YoixMake.javaTimeZone(obj));
		    triggerRecompute(calendar);
		    break;

		case V_UNIXTIME:
		    calendar.setTime(new Date((long)(1000.0*obj.doubleValue())));
		    break;

		case V_YEAR:
		    calendar.set(Calendar.YEAR, obj.intValue());
		    break;
	    }
	}

	return(obj);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildCalendar() {

	YoixObject  obj;
	Calendar    calendar;
	TimeZone    timezone;
	Locale      locale;
	int         first_day_of_week;
	int         minimal_days_in_first_week;
	int         day;
	int         hour;
	int         minute;
	int         month;
	int         second;
	int         year;

	if ((obj = data.get(N_LOCALE, false)) == null || obj.isNull()) {
	    locale = Locale.getDefault();
	    data.put(N_LOCALE, YoixObject.newLocale(locale), false);
	} else locale = YoixMake.javaLocale(obj);

	if ((obj = data.get(N_TIMEZONE, false)) == null || obj.isNull()) {
	    timezone = YoixMiscTime.getDefaultTimeZone();
	    data.put(N_TIMEZONE, YoixObject.newTimeZone(timezone), false);
	} else timezone = YoixMake.javaTimeZone(obj);

	// NOTE: we will probably want to change references to
	// GregorianCalendar to YoixMiscTime once YoixMiscTime
	// is stabilized and accepted since YoixMiscTime extends
	// GregorianCalendar and provides access to setUTime/getUTime
	// which comes in handy (e.g., it could replace need for
	// new Date() needed by setTime just below here)
	calendar = new GregorianCalendar(timezone, locale);

	if ((obj = data.get(N_UNIXTIME, false)) != null && obj.notNull()) {
	    double utime = obj.doubleValue();
	    if (!Double.isNaN(utime))
		calendar.setTime(new Date((long)(1000.0*utime)));
	}

	first_day_of_week = data.getInt(N_FIRSTDAYOFWEEK, -1);
	minimal_days_in_first_week = data.getInt(N_MINDAYSINFIRSTWEEK, -1);
	day = data.getInt(N_DAYOFMONTH, -1);
	hour = data.getInt(N_HOUROFDAY, -1);
	minute = data.getInt(N_MINUTE, -1);
	month = data.getInt(N_MONTH, -1);
	second = data.getInt(N_SECOND, -1);
	year = data.getInt(N_YEAR, -1);

	if (first_day_of_week < 0 || first_day_of_week > calendar.getMaximum(Calendar.DAY_OF_WEEK))
	    first_day_of_week = calendar.getFirstDayOfWeek();

	if (minimal_days_in_first_week < 0 || minimal_days_in_first_week > (1+calendar.getMaximum(Calendar.DAY_OF_WEEK)))
	    minimal_days_in_first_week = calendar.getMinimalDaysInFirstWeek();

	// don't bother checking February, etc.
	if (day < 0 || day > calendar.getMaximum(Calendar.DAY_OF_MONTH))
	    day = calendar.get(Calendar.DAY_OF_MONTH);

	if (hour < 0 || hour > calendar.getMaximum(Calendar.HOUR_OF_DAY))
	    hour = calendar.get(Calendar.HOUR_OF_DAY);

	if (minute < 0 || minute > calendar.getMaximum(Calendar.MINUTE))
	    minute = calendar.get(Calendar.MINUTE);

	if (month < 0 || month > calendar.getMaximum(Calendar.MONTH))
	    month = calendar.get(Calendar.MONTH);

	if (second < 0 || second > calendar.getMaximum(Calendar.SECOND))
	    second = calendar.get(Calendar.SECOND);

	// fortunately, no year zero
	if (year == 0)
	    year = calendar.get(Calendar.YEAR);

	calendar.setLenient(data.getBoolean(N_LENIENT, true));
	calendar.setFirstDayOfWeek(first_day_of_week);
	calendar.setMinimalDaysInFirstWeek(minimal_days_in_first_week);
	calendar.set(year, month, day, hour, minute, second);

	this.calendar = calendar;
    }


    private synchronized YoixObject
    builtinAdd(String name, YoixObject arg[]) {

	int  amount;
	int  field;

	if (arg.length == 2) {
	    if (arg[0].isInteger()) {
		if (arg[1].isInteger()) {
		    amount = arg[1].intValue();
		    if (amount != 0) {
			field = arg[0].intValue();
			try {
			    calendar.add(field, amount);
			}
			catch(IllegalArgumentException e) {
			    VM.badArgumentValue(name, 0);
			}
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinLeastMaximum(String name, YoixObject arg[]) {

	int  field;
	int  value = -1;	// for compiler

	if (arg.length == 1) {
	    if (arg[0].isInteger()) {
		field = arg[0].intValue();
		try {
		    value = calendar.getLeastMaximum(field);
		}
		catch(IllegalArgumentException e) {
		    VM.badArgumentValue(name, 0);
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newInt(value));
    }


    private synchronized YoixObject
    builtinMaximum(String name, YoixObject arg[]) {

	int  field;
	int  value = -1;	// for compiler

	if (arg.length == 1) {
	    if (arg[0].isInteger()) {
		field = arg[0].intValue();
		try {
		    value = calendar.getMaximum(field);
		}
		catch(IllegalArgumentException e) {
		    VM.badArgumentValue(name, 0);
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newInt(value));
    }


    private synchronized YoixObject
    builtinMinimum(String name, YoixObject arg[]) {

	int  field;
	int  value = -1;	// for compiler

	if (arg.length == 1) {
	    if (arg[0].isInteger()) {
		field = arg[0].intValue();
		try {
		    value = calendar.getMinimum(field);
		}
		catch(IllegalArgumentException e) {
		    VM.badArgumentValue(name, 0);
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newInt(value));
    }


    private synchronized YoixObject
    builtinRoll(String name, YoixObject arg[]) {

	boolean  up;
	int      field;

	if (arg.length == 2) {
	    if (arg[0].isInteger()) {
		if (arg[1].isInteger()) {
		    up = arg[1].booleanValue();
		    field = arg[0].intValue();
		    try {
			calendar.roll(field, up);
		    }
		    catch(IllegalArgumentException e) {
			VM.badArgumentValue(name, 0);
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinSet(String name, YoixObject arg[]) {

	int  ivals[];
	int  n;

	if (arg.length == 3 || arg.length == 5 || arg.length == 6) {
	    ivals = new int[arg.length];
	    for (n = 0; n < ivals.length; n++) {
		if (arg[n].isInteger())
		    ivals[n] = arg[n].intValue();
		else VM.badArgument(name, n);
	    }
	    if (ivals.length == 3)
		calendar.set(ivals[0], ivals[1], ivals[2]);
	    else if (ivals.length == 5)
		calendar.set(ivals[0], ivals[1], ivals[2], ivals[3], ivals[4]);
	    else if (ivals.length == 6)
		calendar.set(ivals[0], ivals[1], ivals[2], ivals[3], ivals[4], ivals[5]);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private static YoixObject
    getCalendarField(Calendar calendar, int field, int fail) {

	YoixObject  obj;

	if (calendar.isSet(field))
	    obj = YoixObject.newInt(calendar.get(field));
	else obj = YoixObject.newInt(fail);

	return(obj);
    }


    private static void
    triggerRecompute(Calendar cal) {

	cal.setTime(cal.getTime());
    }
}

