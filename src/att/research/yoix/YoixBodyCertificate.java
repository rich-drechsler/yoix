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
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public final
class YoixBodyCertificate extends YoixPointerActive

{

    private CertificateFactory  certfactory = null;
    private Certificate         certificate = null;

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(5);

    static {
	activefields.put(N_KEYSTRING, new Integer(V_KEYSTRING));
	activefields.put(N_PARAMETERS, new Integer(V_PARAMETERS));
	activefields.put(N_SOURCE, new Integer(V_SOURCE));
	activefields.put(N_SPECIFICATION, new Integer(V_SPECIFICATION));
	activefields.put(N_VERIFY, new Integer(V_VERIFY));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyCertificate(YoixObject data) {

	this(data, null);
    }


    YoixBodyCertificate(YoixObject data, Certificate cert) {

	super(data);
	buildCertificate(cert);
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(CERTIFICATE);
    }

    ///////////////////////////////////
    //
    // YoixBodyCertificate Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_VERIFY:
		obj = builtinVerify(name, argv);
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
	    case V_KEYSTRING:
	        obj = getKeyString();
	        break;

	    case V_PARAMETERS:
	        obj = getParameters();
	        break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(certificate);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_SOURCE:
		    obj = setSource(obj);
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

    private synchronized YoixObject
    builtinVerify(String name, YoixObject arg[]) {

	boolean  verified = false;
	Key      key;

	if (certificate != null) {
	    if (arg.length == 1) {
		if (arg[0].notNull()) {
		    if (arg[0].isString()) {
			if ((key = YoixBodyKey.yoixStringKey(arg[0].stringValue())) != null) {
			    if (key instanceof PublicKey) {
				try {
				    certificate.verify((PublicKey)key);
				    verified = true;
				}
				catch(GeneralSecurityException e) {}
			    }
			}
		    } else VM.badArgument(name, 0);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	}

	return(YoixObject.newInt(verified));
    }


    private void
    buildCertificate(Certificate cert) {

	if (cert != null) {
	    setField(N_SPECIFICATION, YoixObject.newString(cert.getType()));
	    certificate = cert;
	    data.putObject(N_SOURCE, setSource(YoixObject.newNull(), false));
	} else setField(N_SPECIFICATION);
    }


    private synchronized YoixObject
    getKeyString() {

	PublicKey  key;
	String     keystring;

	if (certificate != null) {
	    if ((key = certificate.getPublicKey()) != null) {
		keystring = YoixBodyKey.yoixKeyString(
		    key,
		    key.getAlgorithm(),
		    Cipher.PUBLIC_KEY
		);
	    } else keystring = null;
	} else keystring = null;

	return(YoixObject.newString(keystring));
    }


    private synchronized YoixObject
    getParameters() {

	YoixObject  result = null;
	YoixObject  yobj;
	Method      methods[];
	Object      obj;
	String      name;
	Class       cls;
	int         m;

	if (certificate != null) {
	    cls = certificate.getClass();
	    try {
		methods = cls.getMethods();
	    }
	    catch(SecurityException se) {
		methods = new Method[0];
	    }
	    result = YoixObject.newDictionary(4); // will be 4 at least
	    result.setGrowable(true);
	    result.setGrowto(-1);
	    result.putString("summary", certificate.toString());
	    for (m = 0; m < methods.length; m++) {
		name = methods[m].getName();
		try {
		    if (name.startsWith("get") && methods[m].getParameterTypes().length == 0) {
			obj = methods[m].invoke(certificate, null);
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

	return(result == null ? YoixObject.newDictionary() : result);
    }


    private YoixObject
    processParameter(Object obj) {

	YoixObject  yobj = null;
	YoixObject  yo;
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
	} else if (obj instanceof PublicKey) {
	    bytearray = ((PublicKey)obj).getEncoded();
	    yobj = YoixMake.yoixByteArray(bytearray);
	} else if (obj instanceof Principal) {
	    str = ((Principal)obj).getName();
	    yobj = YoixObject.newString(str);
	} else if (obj instanceof Date) {
	    tm = ((Date)obj).getTime();
	    yobj = YoixObject.newDouble(((double)(tm))/1000.0);
	} else if (obj instanceof List) { // ends up not being used (so far)
	    objs = ((List)obj).toArray();
	    if (objs != null) {
		yobj = YoixObject.newArray(objs.length);
		for (m = 0; m < objs.length; m++) {
		    yo = processParameter(objs[m]);
		    if (yo != null)
			yobj.putObject(m, yo);
		    else yobj.putObject(m, YoixObject.newNull());
		}
	    }
	} else if (obj != null) {
	    str = obj.getClass().getName();
	    if (str.indexOf('$') < 0)
		yobj = YoixObject.newString(obj.toString());
	}

	return(yobj);
    }


    private synchronized YoixObject
    setSource(YoixObject obj) {

	return(setSource(obj, true));
    }


    private synchronized YoixObject
    setSource(YoixObject obj, boolean setnull) {

	ByteArrayInputStream  bais;
	String                strchars;
	byte                  bytearray[];
	char                  inputchars[];

	if (certfactory != null && obj.notNull()) {
	    if (obj.isStream()) {
		if (((YoixBodyStream)(obj.body())).checkMode(READ)) {
		    inputchars = ((YoixBodyStream)(obj.body())).readStream(-1);
		    if (inputchars != null) {
			strchars = new String(inputchars);
			try {
			    bytearray = strchars.getBytes(YoixConverter.getISO88591Encoding());
			    bais = new ByteArrayInputStream(bytearray);
			    certificate = certfactory.generateCertificate(bais);
			}
			catch(CertificateException ce) {
			    certificate = null;
			    VM.abort(BADVALUE);
			}
			catch(Exception e) {
			    VM.abort(INTERNALERROR);
			}
		    }
		} else VM.abort(BADVALUE);
	    } else VM.abort(TYPECHECK);
	} else if (setnull)
	    certificate = null;

	if (certificate != null) {
	    try {
		obj = YoixMake.yoixByteArray(certificate.getEncoded());
	    }
	    catch(CertificateEncodingException cee) {
		// let's be brutal?
		certificate = null;
		VM.abort(BADVALUE);
	    }
	} else obj = YoixObject.newNull();

	return(obj);
    }


    private synchronized void
    setSpecification(YoixObject obj) {

	CertificateFactory  newcertfactory = null;
	YoixObject          yobj;
	String              type;
	String              provider;

	if (obj.notNull()) {
	    if (obj.isString()) {
		try {
		    newcertfactory = CertificateFactory.getInstance(obj.stringValue());
		}
		catch(Exception e) {
		    VM.abort(BADVALUE, N_SPECIFICATION);
		}
	    } else if (obj.isDictionary()) {
		type = obj.getDefinedString(N_TYPE);
		provider = obj.getDefinedString(N_PROVIDER);

		if (type == null)
		    VM.abort(BADVALUE, N_SPECIFICATION, N_TYPE);

		try {
		    if (provider == null)
			newcertfactory = CertificateFactory.getInstance(type);
		    else newcertfactory = CertificateFactory.getInstance(type, provider);
		}
		catch(Exception e) {
		    VM.abort(BADVALUE, N_SPECIFICATION);
		}
	    } else VM.abort(TYPECHECK, N_SPECIFICATION);
	}

	certfactory = newcertfactory;

	if (certfactory != null)
	    setField(N_SOURCE);
    }
}

