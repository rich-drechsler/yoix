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
import java.math.*;
import java.util.*;
import java.security.*;

public final
class YoixBodyRandom extends YoixPointerActive

{

    //
    // We assume random won't be null after the constructor is finished.
    //

    private Random  random;
    private int     seedsize = 8;

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(10);

    static {
	activefields.put(N_BINARY, new Integer(V_BINARY));
	activefields.put(N_BYTES, new Integer(V_BYTES));
	activefields.put(N_DOUBLE, new Integer(V_DOUBLE));
	activefields.put(N_GAUSSIAN, new Integer(V_GAUSSIAN));
	activefields.put(N_INT, new Integer(V_INT));
	activefields.put(N_PROVIDER, new Integer(V_PROVIDER));
	activefields.put(N_RANGE, new Integer(V_RANGE));
	activefields.put(N_SECURE, new Integer(V_SECURE));
	activefields.put(N_SEED, new Integer(V_SEED));
	activefields.put(N_SPECIFICATION, new Integer(V_SPECIFICATION));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyRandom(YoixObject data) {

	super(data);
	buildRandom();
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(RANDOM);
    }

    ///////////////////////////////////
    //
    // YoixBodyRandom Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_BYTES:
		obj = builtinBytes(name, argv);
		break;

	    case V_RANGE:
		obj = builtinRange(name, argv);
		break;

	    default:
		obj = null;
		break;
	}

	return(obj);
    }


    protected final void
    finalize() {

	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_BINARY:
	        obj = YoixObject.newInt(random.nextBoolean());
	        break;

	    case V_DOUBLE:
	        obj = YoixObject.newDouble(random.nextDouble());
	        break;

	    case V_GAUSSIAN:
	        obj = YoixObject.newDouble(random.nextGaussian());
	        break;

	    case V_INT:
	        obj = YoixObject.newInt(random.nextInt());
	        break;

	    case V_PROVIDER:
	        obj = getProvider();
	        break;

	    case V_SECURE:
	        obj = YoixObject.newInt(random instanceof SecureRandom);
	        break;

	    case V_SEED:
	        obj = getSeed();
	        break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(random);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_SECURE:
		    setSecure(obj);
		    break;

		case V_SEED:
		    setSeed(obj);
		    break;

		case V_SPECIFICATION:
		    setSpecification(obj);
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
    buildRandom() {

	setField(N_SECURE);
	setField(N_SPECIFICATION);
	setField(N_SEED);
    }


    private YoixObject
    builtinBytes(String name, YoixObject arg[]) {

	YoixObject  yobj = null;
	byte        bytearray[];
	int         nbr;

	if (arg.length == 1) {
	    if (arg[0].isInteger()) {
		nbr = arg[0].intValue();
		if (nbr > 0) {
		    bytearray = new byte[nbr];
		    random.nextBytes(bytearray);
		    yobj = YoixMake.yoixByteArray(bytearray);
		} else VM.badArgumentValue(name, 0);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(yobj);
    }


    private YoixObject
    builtinRange(String name, YoixObject arg[]) {

	int  nbr = 0;

	if (arg.length == 1) {
	    if (arg[0].isInteger()) {
		if ((nbr = arg[0].intValue()) > 0)
		    nbr = random.nextInt(nbr);
		else VM.badArgumentValue(name, 0);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return (YoixObject.newInt(nbr));
    }


    private synchronized YoixObject
    getProvider() {

	Enumeration  enm;
	YoixObject   result;
	Provider     provider;
	String       name;
	String       pname;
	String       pinfo;
	int          sz;

	if (random instanceof SecureRandom) {
	    provider = ((SecureRandom)random).getProvider();
	    if (provider != null) {
		sz = provider.size() + 2;
		if ((pname = provider.getName()) != null)
		    sz++;
		if ((pinfo = provider.getInfo()) != null)
		    sz++;
		result = YoixObject.newDictionary(sz);
		enm = provider.propertyNames();
		while (enm.hasMoreElements()) {
		    name = (String)enm.nextElement();
		    result.putString(name, provider.getProperty(name));
		}
		if (pname != null)
		    result.putString("name", pname);
		if (pinfo != null)
		    result.putString("info", pinfo);
		result.putString("summary", provider.toString());
		result.putString("version", "" + provider.getVersion());
	    } else result = null;
	} else result = null;

	return(result == null ? YoixObject.newNull() : result);
    }


    private synchronized YoixObject
    getSeed() {

	YoixObject  result;
	byte        bytearray[];

	if (random instanceof SecureRandom) {
	    bytearray = ((SecureRandom)random).generateSeed(seedsize);
	    result = YoixMake.yoixByteArray(bytearray);
	} else result = YoixObject.newDouble((double)random.nextLong());

	return (result);
    }


    private synchronized void
    setSecure(YoixObject obj) {

	if (obj.booleanValue()) {
	    if (random == null || !(random instanceof SecureRandom))
		random = new SecureRandom();
	} else {
	    if (random == null || random instanceof SecureRandom)
		random = new Random();
	}
    }


    private synchronized void
    setSeed(YoixObject obj) {

	YoixObject  yobj;
	byte        bytearray[];
	long        seed;
	int         n;

	if (obj.notNull()) {
	    if (obj.isArray() || obj.isString()) {
		if (obj.isArray())
		    bytearray = YoixMake.javaByteArray(obj);
		else bytearray = YoixMisc.hexStringToBytes(obj.stringValue());
		if (random instanceof SecureRandom) {
		    ((SecureRandom)random).setSeed(bytearray);
		    seedsize = bytearray.length;
		} else {
		    seed = 0;
		    for (n = 0; n < 8 && n < bytearray.length; n++)
			seed = (seed << 8) | bytearray[n];
		    random.setSeed(seed);
		}
	    } else if (obj.isNumber())
		random.setSeed((long)obj.doubleValue());
	    else VM.abort(TYPECHECK, N_SEED);
	}
    }


    private synchronized void
    setSpecification(YoixObject obj) {

	Random  newrandom;
	String  name;
	String  provider;

	if (random instanceof SecureRandom) {
	    if (obj.notNull()) {
		newrandom = null;
		if (obj.isString()) {
		    try {
			newrandom = SecureRandom.getInstance(obj.stringValue());
		    }
		    catch(GeneralSecurityException e) {
			VM.abort(BADVALUE, N_SPECIFICATION);
		    }
		} else if (obj.isDictionary()) {
		    name = obj.getDefinedString(N_NAME);
		    if (name != null) {
			provider = obj.getDefinedString(N_PROVIDER);
			try {
			    if (provider == null)
				newrandom = SecureRandom.getInstance(name);
			    else newrandom = SecureRandom.getInstance(name, provider);
			}
			catch(GeneralSecurityException e) {
			    VM.abort(BADVALUE, N_SPECIFICATION);
			}
		    } else VM.abort(BADVALUE, N_SPECIFICATION, N_NAME);
		} else VM.abort(TYPECHECK, N_SPECIFICATION);
	    } else newrandom = new SecureRandom();

	    if (newrandom != null)
		random = newrandom;
	}
    }
}

