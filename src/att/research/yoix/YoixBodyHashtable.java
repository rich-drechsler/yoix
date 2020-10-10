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
class YoixBodyHashtable extends YoixPointerActive

{

    private YoixUtilHashtable  hashtable;

    //
    // The activefields Hashtable translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(20);

    static {
	activefields.put(N_CLONE, new Integer(V_CLONE));
	activefields.put(N_CONTAINS, new Integer(V_CONTAINS));
	activefields.put(N_CONTAINSVALUE, new Integer(V_CONTAINSVALUE));
	activefields.put(N_FIND, new Integer(V_FIND));
	activefields.put(N_FINDALL, new Integer(V_FINDALL));
	activefields.put(N_GET, new Integer(V_GET));
	activefields.put(N_KEYS, new Integer(V_KEYS));
	activefields.put(N_PAIRS, new Integer(V_PAIRS));
	activefields.put(N_PUT, new Integer(V_PUT));
	activefields.put(N_PUTALL, new Integer(V_PUTALL));
	activefields.put(N_REMOVE, new Integer(V_REMOVE));
	activefields.put(N_REMOVEVALUE, new Integer(V_REMOVEVALUE));
	activefields.put(N_SIZE, new Integer(V_SIZE));
	activefields.put(N_VALUES, new Integer(V_VALUES));
    }

    //
    // A full lookup during a dump is skipped for all fields named in
    // the sideeffects Hashtable. Probably only needed if reading the
    // field would cause an unwanted side effect.
    //

    private static final Hashtable  sideeffects = new Hashtable(1);

    static {
	sideeffects.put(N_CLONE, Boolean.TRUE);
	sideeffects.put(N_PAIRS, Boolean.TRUE);
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyHashtable(YoixObject data) {

	super(data);
	buildHashtable();
	setFixedSize();
    }


    private
    YoixBodyHashtable(YoixObject data, YoixUtilHashtable hashtable) {

	super(data);
	this.hashtable = hashtable;
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(HASHTABLE);
    }

    ///////////////////////////////////
    //
    // YoixBodyHashtable Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_CONTAINS:
		obj = builtinContains(name, argv);
		break;

	    case V_CONTAINSVALUE:
		obj = builtinContainsValue(name, argv);
		break;

	    case V_FIND:
		obj = builtinFind(name, argv);
		break;

	    case V_FINDALL:
		obj = builtinFindAll(name, argv);
		break;

	    case V_GET:
		obj = builtinGet(name, argv);
		break;

	    case V_PUT:
		obj = builtinPut(name, argv);
		break;

	    case V_PUTALL:
		obj = builtinPutAll(name, argv);
		break;

	    case V_REMOVE:
		obj = builtinRemove(name, argv);
		break;

	    case V_REMOVEVALUE:
		obj = builtinRemoveValue(name, argv);
		break;

	    default:
		obj = null;
		break;
	}

	return(obj);
    }


    protected final void
    finalize() {

	hashtable.clear();
	hashtable = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_CLONE:
		obj = getClone();
		break;

	    case V_KEYS:
		obj = getKeys();
		break;

	    case V_PAIRS:
		obj = getPairs();
		break;

	    case V_SIZE:
		obj = getSize();
		break;

	    case V_VALUES:
		obj = getValues();
		break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return((Object)hashtable);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	int  mode;

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_PAIRS:
		    setPairs(obj);
		    break;

		case V_SIZE:
		    setSize(obj);
		    break;
	    }
	}

	return(obj);
    }


    protected final boolean
    sideEffects(String name) {

	return(sideeffects != null && sideeffects.containsKey(name));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildHashtable() {

	hashtable = new YoixUtilHashtable(data.getInt(N_SIZE, 0));
	setField(N_PAIRS);
    }


    private YoixObject
    builtinContains(String name, YoixObject arg[]) {

	boolean  result = false;

	if (arg.length == 1)
	    result = hashtable.containsYoixObjectKey(arg[0]);
	else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private YoixObject
    builtinContainsValue(String name, YoixObject arg[]) {

	boolean  result = false;

	if (arg.length == 1)
	    result = hashtable.containsYoixObjectValue(arg[0]);
	else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private YoixObject
    builtinFind(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	Vector      results;

	//
	// Decided to temporarily disable the second argument at the top
	// level until we impose some kind of order on hashtables. Just
	// accept two arguments in the first test is all you should have
	// to do if you don't like this approach.
	//

	if (arg.length == 1) {	// eventually may accept arg.length == 2
	    if (arg.length == 1 || arg[1].isInteger()) {
		results = hashtable.findYoixObject(arg[0]);
		if (results.size() > 0) {
		    if (arg.length == 2 && arg[1].booleanValue())
			obj = (YoixObject)results.lastElement();
		    else obj = (YoixObject)results.firstElement();
		} else obj = YoixObject.newNull();
	    } else VM.badArgument(name, 1);
	} else VM.badCall(name);

	return(obj == null ? YoixObject.newNull() : obj);
    }


    private YoixObject
    builtinFindAll(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	Vector      results;

	if (arg.length == 1) {
	    results = hashtable.findYoixObject(arg[0]);
	    if (results.size() > 0)
		obj = YoixMisc.copyIntoArray(results, true);
	    else obj = YoixObject.newArray();
	} else VM.badCall(name);

	return(obj == null ? YoixObject.newNull() : obj);
    }


    private YoixObject
    builtinGet(String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg.length == 1)
	    obj = hashtable.getYoixObject(arg[0]);
	else VM.badCall(name);

	return(obj == null ? YoixObject.newNull() : obj);
    }


    private YoixObject
    builtinPut(String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg.length == 2) {
	    if ((obj = hashtable.putYoixObject(arg[0], arg[1])) == null)
		obj = YoixObject.newNull();
	} else VM.badCall(name);

	return(obj == null ? YoixObject.newNull() : obj);
    }


    private YoixObject
    builtinPutAll(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	Vector      results;
	int         n;

	if (arg.length > 0 && arg.length%2 == 0) {
	    results = hashtable.putYoixObject(arg);
            if (results.size() > 0)
                obj = YoixMisc.copyIntoArray(results, true);
            else obj = YoixObject.newArray();
	} else VM.badCall(name);

	return(obj == null ? YoixObject.newNull() : obj);
    }


    private YoixObject
    builtinRemove(String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg.length == 1)
	    obj = hashtable.removeYoixObjectKey(arg[0]);
	else VM.badCall(name);

	return(obj == null ? YoixObject.newNull() : obj);
    }


    private YoixObject
    builtinRemoveValue(String name, YoixObject arg[]) {

	int  result = 0;

	if (arg.length == 1)
	    result = hashtable.removeYoixObjectValue(arg[0]);
	else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    getClone() {

	YoixBodyHashtable  body;

	body = new YoixBodyHashtable(
	    (YoixObject)data.clone(),
	    (YoixUtilHashtable)hashtable.clone()
	);
	return(YoixObject.newPointer(body));
    }


    private YoixObject
    getKeys() {

	return(YoixMisc.copyIntoArray(hashtable.getYoixObjectKeys()));
    }


    private YoixObject
    getPairs() {

	YoixObject  obj;

	obj = YoixMisc.copyIntoArray(hashtable.getYoixObjectPairs());
	if (obj.notNull())
	    obj.setGrowable(true);
	return(obj);
    }


    private YoixObject
    getSize() {

	return(YoixObject.newInt(hashtable.getYoixObjectCount()));
    }


    private YoixObject
    getValues() {

	return(YoixMisc.copyIntoArray(hashtable.getYoixObjectValues()));
    }


    private synchronized void
    setPairs(YoixObject obj) {

	data.put(N_PAIRS, YoixObject.newArray(), false);
	hashtable.loadYoixObject(obj);
    }


    private void
    setSize(YoixObject obj) {

	hashtable.setSizeTo(obj.intValue());
    }
}

