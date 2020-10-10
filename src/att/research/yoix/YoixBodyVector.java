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
class YoixBodyVector extends YoixPointerActive

{

    private YoixUtilVector  vector;

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(25);

    static {
	activefields.put(N_CLONE, new Integer(V_CLONE));
	activefields.put(N_CONTAINS, new Integer(V_CONTAINS));
	activefields.put(N_CONTAINSVALUE, new Integer(V_CONTAINSVALUE));
	activefields.put(N_ELEMENTCOUNT, new Integer(V_ELEMENTCOUNT));
	activefields.put(N_ELEMENTS, new Integer(V_ELEMENTS));
	activefields.put(N_FIND, new Integer(V_FIND));
	activefields.put(N_FINDALL, new Integer(V_FINDALL));
	activefields.put(N_FIRSTVALUE, new Integer(V_FIRSTVALUE));
	activefields.put(N_GET, new Integer(V_GET));
	activefields.put(N_INSERT, new Integer(V_INSERT));
	activefields.put(N_LASTVALUE, new Integer(V_LASTVALUE));
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
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyVector(YoixObject data) {

	super(data);
	buildVector();
	setFixedSize();
    }


    private
    YoixBodyVector(YoixObject data, YoixUtilVector vector) {

	super(data);
	this.vector = vector;
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(VECTOR);
    }

    ///////////////////////////////////
    //
    // YoixBodyVector Methods
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

	    case V_INSERT:
		obj = builtinInsert(name, argv);
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

	vector.removeAllElements();
	vector = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_CLONE:
		obj = getClone();
		break;

	    case V_ELEMENTCOUNT:
		obj = getElementCount();
		break;

	    case V_ELEMENTS:
		obj = getElements();
		break;

	    case V_FIRSTVALUE:
		obj = getFirstValue();
		break;

	    case V_LASTVALUE:
		obj = getLastValue();
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

	return((Object)vector);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_FIRSTVALUE:
		    setFirstValue(obj);
		    break;

		case V_LASTVALUE:
		    setLastValue(obj);
		    break;

		case V_SIZE:
		    setSize(obj);
		    break;

		case V_VALUES:
		    setValues(obj);
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
    buildVector() {

	vector = new YoixUtilVector(0);
	setField(N_VALUES);
	setField(N_SIZE, YoixObject.newInt(Math.max(data.getInt(N_SIZE, 0), vector.size())));
    }


    private YoixObject
    builtinContains(String name, YoixObject arg[]) {

	boolean  result = false;
	int      index;

	if (arg.length == 1) {
	    if (arg[0].isInteger())
		result = vector.containsYoixObject(arg[0].intValue());
	    else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private YoixObject
    builtinContainsValue(String name, YoixObject arg[]) {

	boolean  result = false;

	if (arg.length == 1)
	    result = vector.containsYoixObject(arg[0]);
	else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private YoixObject
    builtinFind(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	Vector      results;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1 || arg[1].isInteger()) {
		results = vector.findYoixObject(arg[0]);
		if (results.size() > 0) {
		    if (arg.length == 2 && arg[1].booleanValue())
			obj = (YoixObject)results.lastElement();
		    else obj = (YoixObject)results.firstElement();
		} else obj = YoixObject.newInt(-1);
	    } else VM.badArgument(name, 1);
	} else VM.badCall(name);

	return(obj == null ? YoixObject.newInt(-1) : obj);
    }


    private YoixObject
    builtinFindAll(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	Vector      results;

	if (arg.length == 1) {
	    results = vector.findYoixObject(arg[0]);
	    if (results.size() > 0)
		obj = YoixMisc.copyIntoArray(results);
	    else obj = YoixObject.newArray();
	} else VM.badCall(name);

	return(obj == null ? YoixObject.newNull() : obj);
    }


    private YoixObject
    builtinGet(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	int         offset;

	if (arg.length == 1 ) {
	    if (arg[0].isInteger())
		obj = vector.getYoixObject(arg[0].intValue());
	    else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(obj == null ? YoixObject.newNull() : obj);
    }


    private YoixObject
    builtinInsert(String name, YoixObject arg[]) {

	boolean  result = false;

	if (arg.length == 2) {
	    if (arg[0].isInteger())
		result = vector.insertYoixObject(arg[0].intValue(), arg[1]);
	    else VM.badArgument(name, 1);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private YoixObject
    builtinInsertAll(String name, YoixObject arg[]) {

	int  result = 0;
	int  n;

	//
	// Currently unused - was mapped to insertall field which we
	// have now removed. Probably will extend insert() builtin so
	// it accepts a variable number of arguments - later. Leaving
	// this in for now, even though it probably should be tossed.
	//

	if (arg.length > 0 && arg.length%2 == 0) {
	    for (n = 0; n < arg.length; n += 2) {
		if (arg[n].isInteger() == false)
		    VM.badArgument(name, n);
	    }
	    result = vector.insertYoixObject(arg);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private YoixObject
    builtinPut(String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg.length == 2) {
	    if (arg[0].isInteger())
		obj = vector.putYoixObject(arg[0].intValue(), arg[1]);
	    else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(obj == null ? YoixObject.newNull() : obj);
    }


    private YoixObject
    builtinPutAll(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	Vector      results;
	int         n;

	if (arg.length > 0 && arg.length%2 == 0) {
	    for (n = 0; n < arg.length; n += 2) {
		if (arg[n].isInteger() == false)
		    VM.badArgument(name, n);
	    }
	    results = vector.putYoixObject(arg);
            if (results.size() > 0)
                obj = YoixMisc.copyIntoArray(results);
            else obj = YoixObject.newArray();
	} else VM.badCall(name);

	return(obj == null ? YoixObject.newNull() : obj);
    }


    private YoixObject
    builtinRemove(String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isInteger()) {
		if (arg.length == 1 || arg[1].isInteger()) {
		    obj = vector.removeYoixObject(
			arg[0].intValue(),
			arg.length == 2 ? arg[1].booleanValue() : false
		    );
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(obj == null ? YoixObject.newNull() : obj);
    }


    private YoixObject
    builtinRemoveValue(String name, YoixObject arg[]) {

	int  result = 0;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1 || arg[1].isInteger()) {
		result = vector.removeYoixObject(
		    arg[0],
		    arg.length == 2 ? arg[1].booleanValue() : false
		);
	    } else VM.badArgument(name, 1);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    getClone() {

	YoixBodyVector  body;

	body = new YoixBodyVector(
	    (YoixObject)data.clone(),
	    (YoixUtilVector)vector.clone()
	);
	return(YoixObject.newPointer(body));
    }

    private YoixObject
    getElementCount() {

	return(YoixObject.newInt(vector.getYoixObjectElementCount()));
    }


    private YoixObject
    getElements() {

	YoixObject  obj;

	obj = YoixMisc.copyIntoArray(vector.getYoixObjectElements());
	if (obj.notNull())
	    obj.setGrowable(true);
	return(obj);
    }


    private YoixObject
    getFirstValue() {

	YoixObject  obj;

	if ((obj = vector.firstYoixObject()) == null)
	    obj = YoixObject.newNull();
	return(obj);
    }


    private YoixObject
    getLastValue() {

	YoixObject  obj;

	if ((obj = vector.lastYoixObject()) == null)
	    obj = YoixObject.newNull();
	return(obj);
    }


    private YoixObject
    getSize() {

	return(YoixObject.newInt(vector.getYoixObjectSize()));
    }


    private YoixObject
    getValues() {

	YoixObject  obj;

	obj = YoixMisc.copyIntoArray(vector.getYoixObject());
	if (obj.notNull())
	    obj.setGrowable(true);
	return(obj);
    }


    private void
    setFirstValue(YoixObject obj) {

	vector.firstYoixObject(obj);
    }


    private void
    setLastValue(YoixObject obj) {

	vector.lastYoixObject(obj);
    }


    private synchronized void
    setValues(YoixObject obj) {

	data.put(N_VALUES, YoixObject.newArray(), false);
	vector.loadYoixObject(obj);
    }


    private void
    setSize(YoixObject obj) {

	vector.setSizeTo(obj.intValue());
    }
}

