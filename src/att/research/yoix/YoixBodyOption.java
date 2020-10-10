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
class YoixBodyOption extends YoixPointerActive

{

    //
    // Option parser support.
    //

    private YoixOption  option;

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(7);

    static {
	activefields.put(N_GETOPT, new Integer(V_GETOPT));
	activefields.put(N_OPTARG, new Integer(V_OPTARG));
	activefields.put(N_OPTCHAR, new Integer(V_OPTCHAR));
	activefields.put(N_OPTERROR, new Integer(V_OPTERROR));
	activefields.put(N_OPTIND, new Integer(V_OPTIND));
	activefields.put(N_OPTSTR, new Integer(V_OPTSTR));
	activefields.put(N_OPTWORD, new Integer(V_OPTWORD));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyOption(YoixObject data) {

	super(data);
	buildOption();
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(OPTION);
    }

    ///////////////////////////////////
    //
    // YoixBodyOption Methods
    //
    ///////////////////////////////////

    final YoixObject
    callGetopt(YoixObject arg[], int start) {

	return(builtinGetopt(N_GETOPT, arg, start));
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_GETOPT:
		obj = builtinGetopt(name, argv, 0);
		break;

	    default:
		obj = null;
		break;
	}

	return(obj);
    }


    protected final void
    finalize() {

	option = null;
	super.finalize();
    }


    protected final synchronized YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_OPTARG:
		obj = YoixMake.yoixObject(option.optarg);
		break;

	    case V_OPTCHAR:
		obj = YoixObject.newInt(option.optchar);
		break;

	    case V_OPTERROR:
		obj = YoixObject.newString(option.opterror);
		break;

	    case V_OPTIND:
		obj = YoixObject.newInt(option.optind);
		break;

	    case V_OPTSTR:
		obj = YoixObject.newString(option.optstr);
		break;

	    case V_OPTWORD:
		obj = YoixObject.newString(option.optword);
		break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(option);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_OPTARG:
		    option.optarg = obj.notNull() ? obj.clone() : null;
		    break;

		case V_OPTCHAR:
		    option.optchar = (char)obj.intValue();
		    break;

		case V_OPTERROR:
		    option.opterror = obj.notNull() ? obj.stringValue() : null;
		    break;

		case V_OPTIND:
		    option.optind = obj.intValue();
		    break;

		case V_OPTSTR:
		    option.optstr = obj.notNull() ? obj.stringValue() : null;
		    break;

		case V_OPTWORD:
		    option.optword = obj.notNull() ? obj.stringValue() : null;
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
    buildOption() {

	option = new YoixOption();
	setField(N_OPTIND);
	setField(N_OPTSTR);
    }


    private YoixObject
    builtinGetopt(String name, YoixObject arg[], int start) {

	YoixObject  obj;
	Object      argv[];
	String      letters = null;
	String      words[] = null;
	int         argc;
	int         letter = -1;
	int         offset;
	int         n;

	argc = arg.length - start;

	if (argc == 2 || argc == 3) {
	    if (arg[start].isArray() || arg[start].isNull()) {
		if (argc == 2) {
		    if (arg[1+start].isString() || arg[1+start].isArray() || arg[1+start].isNull()) {
			if (arg[1+start].isString()) {
			    letters = arg[1+start].notNull() ? arg[1+start].stringValue() : null;
			    words = null;
			} else {
			    letters = null;
			    words = arg[1+start].notNull() ? YoixMake.javaStringArray(arg[1+start]) : null;
			}
		    } else VM.badArgument(1+start);
		} else {
		    if (arg[1+start].isString() || arg[1+start].isNull()) {
			if (arg[2+start].isArray() || arg[2+start].isNull()) {
			    letters = arg[1+start].notNull() ? arg[1+start].stringValue() : null;
			    words = arg[2+start].notNull() ? YoixMake.javaStringArray(arg[2+start]) : null;
			} else VM.badArgument(2+start);
		    } else VM.badArgument(1+start);
		}
		if (letters != null || words != null) {
		    argv = new Object[arg[start].sizeof()];
		    offset = arg[start].offset();
		    for (n = 0; n < argv.length; n++)
			argv[n] = arg[start].getObject(n + offset);
		    letter = option.getopt(argv, letters, words);
		}
	    } else VM.badArgument(start);
	} else VM.badCall(name);

	return(YoixObject.newInt(letter));
    }
}

