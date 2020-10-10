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
import java.io.*;
import java.lang.reflect.*;
import java.math.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.spec.*;
import java.util.*;
import javax.crypto.*;

public final
class YoixBodyCipher extends YoixPointerActive

{

    private Cipher  cipher;
    private Vector  cryptbytes = new Vector();
    private int     opmode = -1;

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(10);

    static {
	activefields.put(N_ALGORITHM, new Integer(V_ALGORITHM));
	activefields.put(N_INITIALIZER, new Integer(V_INITIALIZER));
	activefields.put(N_OPMODE, new Integer(V_OPMODE));
	activefields.put(N_PARAMETERS, new Integer(V_PARAMETERS));
	activefields.put(N_PROVIDER, new Integer(V_PROVIDER));
	activefields.put(N_SPECIFICATION, new Integer(V_SPECIFICATION));
	activefields.put(N_TEXT, new Integer(V_TEXT));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyCipher(YoixObject data) {

	super(data);
	buildCipher();
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(CIPHER);
    }

    ///////////////////////////////////
    //
    // YoixBodyCipher Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_UNWRAP:
		obj = builtinUnwrap(name, argv);
		break;

	    case V_WRAP:
		obj = builtinWrap(name, argv);
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
	    case V_ALGORITHM:
	        obj = getAlgorithm();
	        break;

	    case V_OPMODE:
	        obj = getOpmode();
	        break;

	    case V_PARAMETERS:
	        obj = getParameters();
	        break;

	    case V_PROVIDER:
	        obj = getProvider();
	        break;

	    case V_TEXT:
	        obj = getText();
	        break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(cipher);
    }


    synchronized boolean
    setCipherMode(int mode) {

	boolean  valid = true;

	switch (mode) {
	    case Cipher.DECRYPT_MODE:
	    case Cipher.ENCRYPT_MODE:
	    case Cipher.UNWRAP_MODE:
	    case Cipher.WRAP_MODE:
		if (opmode != mode) {
		    opmode = mode;
		    setField(N_INITIALIZER);
		}
		break;

	    default:
		valid = false;
		break;
	}
	return(valid);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_INITIALIZER:
		    setInitializer(obj);
		    break;

		case V_OPMODE:
		    setOpmode(obj);
		    break;

		case V_SPECIFICATION:
		    setSpecification(obj);
		    break;

		case V_TEXT:
		    setText(obj);
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
    buildCipher() {

	setField(N_SPECIFICATION);
    }


    private YoixObject
    builtinUnwrap(String name, YoixObject arg[]) {

	boolean  result = false;

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    builtinWrap(String name, YoixObject arg[]) {

	boolean  result = false;

	if (arg.length == 1) {
	    if (cipher != null) {
		if (opmode != Cipher.WRAP_MODE) {
		    opmode = Cipher.WRAP_MODE;
		    setField(N_INITIALIZER);
		}
	    } else VM.abort(UNSETVALUE, N_SPECIFICATION);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    getAlgorithm() {

	return(YoixObject.newString(cipher != null ? cipher.getAlgorithm() : null));
    }


    private String
    getCipherAlgorithmName() {

	return(getCipherAlgorithmPart(0));
    }


    private String
    getCipherAlgorithmMode() {

	return(getCipherAlgorithmPart(1));
    }


    private String
    getCipherAlgorithmPadding() {

	return(getCipherAlgorithmPart(2));
    }


    private synchronized String
    getCipherAlgorithmPart(int nbr) {

	StringTokenizer  st;
	String           part;
	String           algorithm;

	if (cipher != null && nbr >= 0) {
	    if ((algorithm = cipher.getAlgorithm()) != null) {
		st = new StringTokenizer(algorithm, "/");
		part = null;
		while (st.hasMoreTokens()) {
		    if (nbr-- == 0) {
			part = st.nextToken();
			break;
		    } else st.nextToken();
		}
	    } else part = null;
	} else part = null;

	return part;
    }


    private YoixObject
    getOpmode() {

	return(YoixObject.newInt(opmode));
    }


    private synchronized YoixObject
    getParameters() {

	AlgorithmParameters  parameters;
	YoixObject           result;

	if (cipher != null) {
	    parameters = cipher.getParameters();
	    if ((result = getParameterValues(parameters)) == null)
		result = YoixObject.newDictionary(1);
	    result.putInt("blocksize", cipher.getBlockSize());
	    result.setGrowable(false);
	} else result = null;

	return(result == null ? YoixObject.newDictionary() : result);
    }


    private YoixObject
    getParameterValues(Object repository) {

	YoixObject  result = null;
	YoixObject  yobj;
	Method      methods[];
	Object      obj;
	String      name;
	Class       cls;
	int         m;

	if (repository != null) {
	    cls = repository.getClass();
	    try {
		methods = cls.getMethods();
	    }
	    catch(SecurityException se) {
		methods = new Method[0];
	    }
	    result = YoixObject.newDictionary(2); // will be 2 at least
	    result.setGrowable(true);
	    result.setGrowto(-1);
	    result.putString("summary", repository.toString());
	    for (m = 0; m < methods.length; m++) {
		name = methods[m].getName();
		try {
		    if (name.startsWith("get") && methods[m].getParameterTypes().length == 0) {
			obj = methods[m].invoke(repository, null);
			name = name.substring(3).toLowerCase();
			yobj = processParameter(obj);
			if (yobj != null)
			    result.putObject(name, yobj);
		    }
		}
		catch(IllegalAccessException e) {}
		catch(InvocationTargetException e) {}
	    }
	} else result = null;

	return(result);
    }


    private synchronized YoixObject
    getProvider() {

	YoixObject  result;
	Provider    provider;

	if (cipher != null) {
	    if ((provider = cipher.getProvider()) != null)
		result = YoixModuleSecure.getProviderInfo(provider);
	    else result = null;
	} else result = null;

	return(result == null ? YoixObject.newDictionary() : result);
    }


    private synchronized YoixObject
    getText() {

	StringBuffer  sb;
	YoixObject    result = null;
	String        encoding;
	byte          bytearray[] = null;
	byte          bytes[][];
	int           len;
	int           off;
	int           n;

	if (cipher != null) {
	    switch (opmode) {
		case Cipher.ENCRYPT_MODE:
		case Cipher.DECRYPT_MODE:
		    try {
			bytearray = cipher.doFinal();
			setField(N_INITIALIZER);
		    }
		    catch(Exception e) {
			VM.abort(EXCEPTION, new String[] {e.getMessage()});
		    }
		    if (bytearray != null && bytearray.length > 0)
			cryptbytes.addElement(bytearray);
		    break;

		default:
		    VM.abort(BADVALUE, N_OPMODE);
		    break;
	    }

	    if (cryptbytes.size() > 0) {
		bytes = (byte[][])cryptbytes.toArray(new byte[0][0]);
		if (opmode == Cipher.ENCRYPT_MODE) {
		    for(n = 0, len = 0; n < bytes.length; n++)
			len += bytes[n].length;
		    bytearray = new byte[len];
		    for(n = 0, off = 0; n < bytes.length; n++, off += len)
			System.arraycopy(bytes[n], 0, bytearray, off, len = bytes[n].length);
		    result = YoixMake.yoixByteArray(bytearray);
		} else {
		    sb = new StringBuffer();
		    encoding = YoixConverter.getISO88591Encoding();
		    try {
			for(n = 0; n < bytes.length; n++)
			    sb.append(new String(bytes[n], encoding));
		    }
		    catch(UnsupportedEncodingException e) {}
		    result = YoixObject.newString(sb.toString());
		}
		cryptbytes.clear();
	    }
	}

	return(result == null ? YoixObject.newNull() : result);
    }


    private YoixObject
    processParameter(Object obj) {

	YoixObject  yobj = null;
	Object      objs[];
	String      str;
	byte        bytearray[];
	long        tm;
	int         m;

	if (obj instanceof BigInteger) {
	    yobj = YoixObject.newString(((BigInteger)obj).toString(16));
	} else if (obj instanceof BigDecimal) { // unused so far
	    yobj = YoixObject.newString(obj.toString());
	} else if (obj instanceof Integer) {
	    yobj = YoixObject.newInt(((Integer)obj).intValue());
	} else if (obj instanceof Double) {
	    yobj = YoixObject.newDouble(((Double)obj).doubleValue());
	} else if (obj instanceof Float) {
	    yobj = YoixObject.newDouble(((Float)obj).doubleValue());
	} else if (obj instanceof byte[]) {
	    bytearray = (byte[])obj;
	    yobj = YoixMake.yoixByteArray(bytearray);
	} else if (obj instanceof String) {
	    yobj = YoixObject.newString((String)obj);
	} else if (obj instanceof Class) {
	    yobj = YoixObject.newString(((Class)obj).getName());
	} else if (obj instanceof Provider) {
	    str = ((Provider)obj).getName();
	    yobj = YoixObject.newString(str);
	} else if (obj instanceof Date) {
	    tm = ((Date)obj).getTime();
	    yobj = YoixObject.newDouble(((double)(tm))/1000.0);
	} else if (obj instanceof List) {	// ends up not being used (so far)
	    objs = ((List)obj).toArray();
	    if (objs != null) {
		yobj = YoixObject.newArray(objs.length);
		for (m = 0; m < objs.length; m++)
		    yobj.putObject(m, processParameter(objs[m]));
	    }
	} else if (obj != null) {
	    str = obj.getClass().getName();
	    if (str.indexOf('$') < 0)
		yobj = YoixObject.newString(obj.toString());
	}

	return(yobj);
    }


    private synchronized void
    setInitializer(YoixObject obj) {

	AlgorithmParameterSpec  newparameterspec;
	AlgorithmParameters     newparameters = null;
	SecureRandom            newrandom = null;
	YoixObject              yobj;
	Certificate             certificate;
	Boolean                 custom;
	Integer                 iobj;
	Object                  managed;
	String                  algname = null;
	String                  sobj;
	Key                     newkey = null;
	int                     newkeytype = -1;

	if (obj.notNull() && cipher != null) {
	    if (opmode < 0)
		VM.abort(BADVALUE, N_OPMODE);
	    if (obj.isString()) {
		if ((newkey = YoixBodyKey.yoixStringKey(obj.stringValue())) != null) {
		    try {
			cipher.init(opmode, newkey);
		    }
		    catch(Exception e) {
			VM.abort(BADVALUE, N_INITIALIZER, new String[] {e.getMessage()});
		    }
		} else VM.abort(BADVALUE, N_INITIALIZER);
	    } else if (obj.isCertificate()) {
		certificate = (Certificate)obj.getManagedObject();
		if (certificate != null) {
		    try {
			cipher.init(opmode, certificate);
		    }
		    catch(Exception e) {
			VM.abort(BADVALUE, N_INITIALIZER, new String[] {e.getMessage()});
		    }
		} else VM.abort(BADVALUE, N_INITIALIZER);
	    } else if (obj.isDictionary()) {
		if ((sobj = obj.getDefinedString(N_ALGORITHM)) != null) {
		    try {
			if (sobj.indexOf('/') > 0)
			    sobj = sobj.substring(0, sobj.indexOf('/'));
			newparameters = AlgorithmParameters.getInstance(sobj);
			algname = sobj;
		    }
		    catch(Exception e) {
			VM.abort(BADVALUE, N_INITIALIZER, N_ALGORITHM, new String[] {e.getMessage()});
		    }
		}
		if ((yobj = obj.getObject(N_ALGORITHMSPEC)) != null) {
		    // fairly bogus; needs to be more generalized for non-standard providers;
		    // leave undocumented for now
		    if (algname == null)
			algname = getCipherAlgorithmName();
		    if ((newparameterspec = YoixModuleSecure.algorithmParameterSpec(yobj, algname, N_INITIALIZER)) == null)
			VM.abort(BADVALUE, N_INITIALIZER, N_ALGORITHMSPEC);
		    if (newparameters == null)
			newparameters = cipher.getParameters();
		    try {
			newparameters.init(newparameterspec);
		    }
		    catch(Exception e) {
			VM.abort(BADVALUE, N_INITIALIZER, N_ALGORITHMSPEC, new String[] {e.getMessage()});
		    }
		}

		if ((certificate = obj.getDefinedCertificate(N_CERTIFICATE)) == null) {
		    if ((managed = obj.getDefinedManagedObject(N_KEY)) != null) {
			if (managed instanceof KeyPair) {
			    if ((iobj = obj.getDefinedInteger(N_KEYTYPE)) != null) {
				switch (iobj.intValue()) {
				    case Cipher.PRIVATE_KEY:
					newkey = ((KeyPair)managed).getPrivate();
					break;

				    case Cipher.PUBLIC_KEY:
					newkey = ((KeyPair)managed).getPublic();
					break;

				    default:
					VM.abort(BADVALUE, N_INITIALIZER, N_KEYTYPE);
					break;
				}
			    } else VM.abort(MISSINGVALUE, N_INITIALIZER, N_KEYTYPE);
			} else if (managed instanceof SecretKey)
			    newkey = (SecretKey)managed;
			else VM.abort(BADVALUE, N_INITIALIZER, N_KEY);
		    } else if ((newkey = YoixBodyKey.yoixStringKey(obj.getDefinedString(N_KEY))) == null)
			VM.abort(MISSINGVALUE, N_INITIALIZER, N_KEY + "|" + N_CERTIFICATE); // eventually either/or with certificate
		}

		newrandom = obj.getDefinedSecureRandom(N_RANDOM);

		if (certificate != null) {
		    try {
			if (newrandom == null)
			    cipher.init(opmode, certificate);
			else cipher.init(opmode, certificate, newrandom);
		    }
		    catch(Exception e) {
			VM.abort(BADVALUE, N_INITIALIZER, new String[] {e.getMessage()});
		    }
		} else if (newkey != null) {
		    try {
			if (newrandom == null) {
			    if (newparameters == null)
				cipher.init(opmode, newkey);
			    else cipher.init(opmode, newkey, newparameters);
			} else {
			    if (newparameters == null)
				cipher.init(opmode, newkey, newrandom);
			    else cipher.init(opmode, newkey, newparameters, newrandom);
			}
		    }
		    catch(Exception e) {
			VM.abort(BADVALUE, N_INITIALIZER, new String[] {e.getMessage()});
		    }
		} else VM.abort(MISSINGVALUE, N_INITIALIZER, N_KEY); // eventually check for Certificate
	    } else VM.abort(TYPECHECK, N_INITIALIZER);
	}
    }


    private synchronized void
    setOpmode(YoixObject obj) {

	if (setCipherMode(obj.intValue()) == false)
	    VM.abort(BADVALUE, N_OPMODE);
    }


    private synchronized void
    setSpecification(YoixObject obj) {

	YoixObject  yobj;
	Cipher      newcipher = null;
	String      trans;
	String      mode;
	String      padding;
	String      provider;
	String      str;

	if (obj.notNull()) {
	    if (obj.isString()) {
		try {
		    newcipher = Cipher.getInstance(obj.stringValue());
		}
		catch(Exception e) {
		    VM.abort(BADVALUE, N_SPECIFICATION);
		}
	    } else if (obj.isDictionary()) {
		trans = obj.getDefinedString(N_TRANSFORMATION);
		mode = null;
		padding = null;
		provider = obj.getDefinedString(N_PROVIDER);

		if (trans.indexOf('/') >= 0) {
		    StringTokenizer st = new StringTokenizer(trans, "/", false);
		    if (st.hasMoreTokens()) {
			trans = st.nextToken();
			if (trans.length() == 0)
			    trans = null;
			if (st.hasMoreTokens()) {
			    mode = st.nextToken();
			    if (mode.length() == 0)
				mode = null;
			    if (st.hasMoreTokens()) {
				padding = st.nextToken();
				if (padding.length() == 0)
				    padding = null;
			    }
			}
		    }
		}

		mode = (str = obj.getDefinedString(N_MODE)) == null ? mode : str;
		padding = (str = obj.getDefinedString(N_PADDING)) == null ? padding : str;

		if (trans == null)
		    VM.abort(BADVALUE, N_SPECIFICATION, N_TRANSFORMATION);

		if (mode != null) {
		    // seems to me that if mode is present, padding must
		    // be present, too
		    if (padding != null)
			trans += "/" + mode;
		    else VM.abort(BADVALUE, N_SPECIFICATION, N_PADDING);
		}

		if (padding != null)
		    trans += "/" + padding;

		try {
		    if (provider == null)
			newcipher = Cipher.getInstance(trans);
		    else newcipher = Cipher.getInstance(trans, provider);
		}
		catch(Exception e) {
		    VM.abort(BADVALUE, N_SPECIFICATION);
		}
	    } else VM.abort(TYPECHECK, N_SPECIFICATION);
	}

	cipher = newcipher;
	if (cipher != null)
	    setField(N_OPMODE);
    }


    private synchronized void
    setText(YoixObject obj) {

	byte  bytearray[] = null;

	if (cipher != null && obj.notNull()) {
	    switch (opmode) {
		case Cipher.ENCRYPT_MODE:
		    if (obj.isString()) {
			bytearray = YoixMake.javaByteArray(obj.stringValue());
			if (bytearray.length > 0)
			    bytearray = cipher.update(bytearray);
			else bytearray = null;
			if (bytearray != null)
			    cryptbytes.addElement(bytearray);
		    } else VM.abort(TYPECHECK, N_TEXT + "/" + N_OPMODE);
		    break;

		case Cipher.DECRYPT_MODE:
		    if (obj.notNull()) {
			if (obj.isArray())
			    bytearray = YoixMake.javaByteArray(obj);
			else if (obj.isString())
			    bytearray = YoixMisc.hexStringToBytes(obj.stringValue());
			else VM.abort(TYPECHECK, N_TEXT + "/" + N_OPMODE);
			if (bytearray != null) {
			    bytearray = cipher.update(bytearray);
			    if (bytearray != null)
				cryptbytes.addElement(bytearray);
			}
		    }
		    break;

		default:
		    VM.abort(BADVALUE, N_OPMODE);
		    break;
	    }
	}
    }
}

