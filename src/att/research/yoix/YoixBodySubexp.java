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
class YoixBodySubexp extends YoixPointerActive

    implements YoixConstants

{

    private YoixRESubexp  subexp;

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
	activefields.put(N_TARGET, new Integer(V_TARGET));
	activefields.put(N_RANGES, new Integer(V_RANGES));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodySubexp(YoixObject data) {

	super(data);
	buildSubexp();
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

	return(SUBEXP);
    }

    ///////////////////////////////////
    //
    // YoixBodySubexp Methods
    //
    ///////////////////////////////////

    protected final void
    finalize() {

	subexp = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	YoixRESubexp  subexp;
	YoixObject    dict;
	int           n;

	subexp = this.subexp;

	switch (activeField(name, activefields)) {
	    case V_TARGET:
		obj = YoixObject.newString(subexp.getSource());
		break;

	    case V_RANGES:
		obj = YoixObject.newArray(subexp.size());
		for (n = 0; n < subexp.size(); n++) {
		    dict = YoixObject.newDictionary(2);
		    dict.put(N_SP, YoixObject.newInt(subexp.getSpAt(n)), false);
		    dict.put(N_EP, YoixObject.newInt(subexp.getEpAt(n)), false);
		    dict.setAccessBody(LR__);
		    obj.put(n, dict, false);
		}
		obj.setAccessBody(LR__);
		break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(subexp);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildSubexp() {

	subexp = new YoixRESubexp();
    }
}

