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
class YoixBodyTimeZone extends YoixPointerActive

    implements YoixConstants

{

    private TimeZone  timezone;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD                OBJECT       BODY
     // -----                ------       ----
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(5);

    static {
	activefields.put(N_DISPLAYNAME, new Integer(V_DISPLAYNAME));
	activefields.put(N_DST, new Integer(V_DST));
	activefields.put(N_DSTOFFSET, new Integer(V_DSTOFFSET));
	activefields.put(N_ID, new Integer(V_ID));
	activefields.put(N_SAMERULES, new Integer(V_SAMERULES));
	activefields.put(N_USEDST, new Integer(V_USEDST));
	activefields.put(N_ZONEOFFSET, new Integer(V_ZONEOFFSET));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyTimeZone(YoixObject data) {

	this(data, null);
    }


    YoixBodyTimeZone(YoixObject data, TimeZone timezone) {

	super(data);

	if (timezone == null)
	    buildTimeZone();
	else this.timezone = timezone;

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

	return(TIMEZONE);
    }

    ///////////////////////////////////
    //
    // YoixBodyTimeZone Methods
    //
    ///////////////////////////////////

    protected YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_DISPLAYNAME:
		obj = builtinDisplayName(name, argv);
		break;

	    case V_DST:
		obj = builtinDST(name, argv);
		break;

	    case V_SAMERULES:
		obj = builtinSameRules(name, argv);
		break;

	    default:
		obj = null;
		break;
	}
	return(obj);
    }


    protected final void
    finalize() {

	timezone = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_DSTOFFSET:
		obj = getDSTOffset(obj);
		break;

	    case V_ID:
		obj = getID(obj);
		break;

	    case V_USEDST:
		obj = getUseDST(obj);
		break;

	    case V_ZONEOFFSET:
		obj = getZoneOffset(obj);
		break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(timezone);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_ID:
		    setID(obj);
		    break;

	        case V_ZONEOFFSET:
		    setZoneOffset(obj);
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
    buildTimeZone() {

	setField(N_ID);
    }


    private synchronized YoixObject
    builtinDisplayName(String name, YoixObject arg[]) {

	boolean  dst = false;
	Locale   locale = null;
	String   retval = null;
	boolean  style = true;

	if (arg.length >= 1 && arg.length <= 3) {
	    if (arg[0].isNumber()) {
		style = arg[0].booleanValue();
		if (arg.length > 1) {
		    if (arg[1].isNumber())
			dst = arg[1].booleanValue();
		    else VM.badArgument(name, 1);
		}
		if (arg.length > 2) {
		    if (arg[2].isLocale())
			locale = (Locale)(arg[2].getManagedObject());
		    else VM.badArgument(name, 2);
		}
		if (timezone != null) {
		    if (locale == null)
			retval = timezone.getDisplayName(dst, style ? TimeZone.LONG : TimeZone.SHORT);
		    else retval = timezone.getDisplayName(dst, style ? TimeZone.LONG : TimeZone.SHORT, locale);
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newString(retval));
    }


    private synchronized YoixObject
    builtinDST(String name, YoixObject arg[]) {

	boolean  dst = false;
	double   dt;

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		if (timezone != null) {
		    dt = arg[0].doubleValue();
		    dst = timezone.inDaylightTime(new Date((long)(1000.0*dt)));
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newInt(dst));
    }


    private synchronized YoixObject
    builtinSameRules(String name, YoixObject arg[]) {

	boolean   same = false;
	TimeZone  zone;

	if (arg.length == 1) {
	    if (arg[0].isTimeZone()) {
		if (timezone != null) {
		    zone = (TimeZone)(arg[0].getManagedObject());
		    same = timezone.hasSameRules(zone);
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newInt(same));
    }


    private synchronized YoixObject
    builtinOffset(String name, YoixObject arg[]) {

	int      offset = 0;
	double   dt;

	if (arg.length == 1) {
	    if (arg[0].isNumber()) {
		if (timezone != null) {
		    dt = arg[0].doubleValue();
		    offset = timezone.getOffset((long)(1000.0*dt));
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newInt(offset));
    }


    private synchronized YoixObject
    getDSTOffset(YoixObject obj) {

	if (timezone != null)
	    obj = YoixObject.newInt(timezone.getDSTSavings());
	return(obj);
    }


    private synchronized YoixObject
    getID(YoixObject obj) {

	return(YoixObject.newString(timezone != null ? timezone.getID() : null));
    }


    private synchronized YoixObject
    getUseDST(YoixObject obj) {

	return(YoixObject.newInt(timezone != null ? timezone.useDaylightTime() : false));
    }


    private synchronized YoixObject
    getZoneOffset(YoixObject obj) {

	if (timezone != null)
	    obj = YoixObject.newInt(timezone.getRawOffset());
	return(obj);
    }


    private synchronized void
    setID(YoixObject obj) {

	timezone = TimeZone.getTimeZone(obj.stringValue());
    }


    private synchronized void
    setZoneOffset(YoixObject obj) {

	if (timezone != null)
	    timezone.setRawOffset(obj.intValue());
    }
}

