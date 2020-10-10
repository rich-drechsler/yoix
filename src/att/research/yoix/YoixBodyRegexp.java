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
class YoixBodyRegexp extends YoixPointerActive

{

    private YoixRERegexp  regexp;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(5);

    static {
	activefields.put(N_PATTERN, new Integer(V_PATTERN));
	activefields.put(N_TYPE, new Integer(V_TYPE));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyRegexp(YoixObject data) {

	super(data);
	buildRegexp();
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

	return(REGEXP);
    }

    ///////////////////////////////////
    //
    // YoixBodyRegexp Methods
    //
    ///////////////////////////////////

    protected final void
    finalize() {

	regexp = null;
	super.finalize();
    }


    protected final synchronized Object
    getManagedObject() {

	String  pat;
	int     flags;

	if (regexp == null) {
	    pat = data.getString(N_PATTERN, null);
	    flags = data.getInt(N_TYPE, SINGLE_BYTE);
	    if ((regexp = new YoixRERegexp(pat, flags)) == null)	// impossible??
		VM.abort(BADVALUE, N_PATTERN);
	}

	return(regexp);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_PATTERN:
		    setPattern(obj);
		    break;

		case V_TYPE:
		    setType(obj);
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
    buildRegexp() {

	regexp = null;
	setField(N_PATTERN);
	setField(N_TYPE);
    }


    private synchronized void
    setPattern(YoixObject obj) {

	if (obj.notNull()) {
	    obj = YoixObject.newString(obj.stringValue());
	    obj.setAccessBody(LR__);
	    data.put(N_PATTERN, obj);
	}

	regexp = null;
    }


    private synchronized void
    setType(YoixObject obj) {

	data.putInt(N_TYPE, obj.intValue()&REFLAGS_MASK);
	regexp = null;
    }

}

