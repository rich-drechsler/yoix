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
import java.util.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public final
class YoixBodyKeyStore extends YoixPointerActive

{

    private String  filename;
    private String  password;
    private String  provider;
    private String  type;

    private static KeyStore  keystore;

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(9);

    static {
	activefields.put(N_ALIASES, new Integer(V_ALIASES));
	activefields.put(N_CERTIFICATE, new Integer(V_CERTIFICATE));
	activefields.put(N_FILE, new Integer(V_FILE));
	activefields.put(N_KEY, new Integer(V_KEY));
	activefields.put(N_OUTPUT, new Integer(V_OUTPUT));
	activefields.put(N_PASSWORD, new Integer(V_PASSWORD));
	activefields.put(N_PROVIDER, new Integer(V_PROVIDER));
	activefields.put(N_SIZE, new Integer(V_SIZE));
	activefields.put(N_TYPE, new Integer(V_TYPE));
    }

    private static final String  CERTIFICATE_ENTRY = "certificate";
    private static final String  KEY_ENTRY = "key";

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyKeyStore(YoixObject data) {

	super(data);
	buildKeyStore();
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(KEYSTORE);
    }

    ///////////////////////////////////
    //
    // YoixBodyKeyStore Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_ALIASES:
		obj = builtinAliases(name, argv);
		break;

	    case V_CERTIFICATE:
		obj = builtinCertificate(name, argv);
		break;

	    case V_KEY:
		obj = builtinKey(name, argv);
		break;

	    case V_OUTPUT:
		obj = builtinOutput(name, argv);
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
	    case V_PROVIDER:
	        obj = getProvider(obj);
	        break;

	    case V_SIZE:
		obj = getSize(obj);
	        break;

	    case V_TYPE:
	        obj = getType(obj);
	        break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(keystore);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_FILE:
		    obj = setFile(obj);
		    break;

		case V_PASSWORD:
		    obj = setPassword(obj);
		    break;

		case V_PROVIDER:
		    setProvider(obj);
		    break;

		case V_TYPE:
		    obj = setType(obj);
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
    buildKeyStore() {

	keystore = null;
	setField(N_PROVIDER);
	setField(N_TYPE);
	setField(N_PASSWORD);
	setField(N_FILE);
	reload();
    }


    private YoixObject
    builtinAliases(String name, YoixObject arg[]) {

	Enumeration  enm;
	YoixObject   result = null;
	YoixObject   yobj;
	KeyStore     keysnap = keystore;
	Object       obj;
	String       str;
	String       type;
	boolean      getdates = false;
	int          cnt;

	if (arg.length >= 0 && arg.length <= 2) {
	    obj = null;
	    if (arg.length == 0) {
		if (keysnap != null)
		    obj = keysnap;
	    } else if (arg.length == 1) {
		if (arg[0].isNull()) {
		    if (keysnap != null)
			obj = keysnap;
		} else if (arg[0].isString()) {
		    if (keysnap != null)
			obj = arg[0];
		} else VM.badArgument(name, 0);
	    } else { // arg.length == 2
		if (arg[0].isNull()) {
		    if (arg[1].isInteger()) {
			obj = keysnap;
			getdates = arg[1].booleanValue();
		    } else VM.badArgument(name, 1);
		} else if (arg[0].isString()) {
		    if (arg[1].isInteger()) {
			if (arg[1].booleanValue()) {
			    if (keysnap != null) {
				try {
				    obj = keysnap.getCreationDate(arg[0].stringValue());
				}
				catch(KeyStoreException kse) {
				    VM.abort(INTERNALERROR); // should not ever happen
				}
			    } else obj = arg[0];
			}
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    }

	    if (obj != null) {
		if (obj instanceof KeyStore) {
		    try {
			result = YoixObject.newArray(keysnap.size());
			enm = keysnap.aliases();
		    }
		    catch(KeyStoreException kse) {
			VM.abort(INTERNALERROR); // should not ever happen
			enm = null; // for compiler
		    }
		    cnt = 0;
		    while (enm.hasMoreElements()) {
			if (arg.length == 2) {
			    yobj = YoixObject.newArray(2);
			    str = (String)(enm.nextElement());
			    yobj.putString(0, str);
			    if (getdates) {
				try {
				    obj = keysnap.getCreationDate(str);
				}
				catch(KeyStoreException kse) {
				    VM.abort(INTERNALERROR); // should not ever happen
				    str = null; // for compiler
				}
				yobj.putDouble(1, ((double)(((Date)obj).getTime()))/1000.0);
			    } else {
				try {
				    if (keysnap.isKeyEntry(str))
					yobj.putString(1, KEY_ENTRY);
				    else if (keysnap.isCertificateEntry(str))
					yobj.putString(1, CERTIFICATE_ENTRY);
				    else {
					VM.abort(INTERNALERROR); // should not ever happen
				    }
				}
				catch(KeyStoreException kse) {
				    yobj = YoixObject.newNull();
				}
			    }
			    result.putObject(cnt++, yobj);
			} else result.putString(cnt++, (String)(enm.nextElement()));
		    }
		} else if (obj instanceof YoixObject) {
		    str = ((YoixObject)obj).stringValue();
		    try {
			if (keysnap.containsAlias(str)) {
			    if (keysnap.isKeyEntry(str))
				type = KEY_ENTRY;
			    else if (keysnap.isCertificateEntry(str))
				type = CERTIFICATE_ENTRY;
			    else {
				VM.abort(INTERNALERROR); // should not ever happen
				type = null; // for compiler
			    }
			} else type = null;
			result = type == null ? YoixObject.newNull() : YoixObject.newString(type);
		    }
		    catch(KeyStoreException kse) {
			VM.abort(INTERNALERROR); // should not ever happen
		    }
		} else { // obj instanceof Date
		    result = YoixObject.newDouble(((double)(((Date)obj).getTime()))/1000.0);
		}
	    }
	} else VM.badCall(name);

	return(result == null ? YoixObject.newNull() : result);
    }


    private YoixObject
    builtinCertificate(String name, YoixObject arg[]) {

	Certificate  certificate;
	Certificate  certchain[];
	YoixObject   result = null;
	KeyStore     keysnap = keystore;
	boolean      getchain;
	String       str;
	int          m;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].notNull() && arg[0].isString()) {
		if (arg.length == 1 || arg[1].isInteger()) {
		    if (arg.length == 1)
			getchain = false;
		    else getchain = arg[1].booleanValue();
		    if (keysnap != null) {
			try {
			    if (getchain) {
				certchain = keysnap.getCertificateChain(arg[0].stringValue());
				certificate = null;
			    } else {
				certificate = keysnap.getCertificate(arg[0].stringValue());
				certchain = null;
			    }
			}
			catch(KeyStoreException kse) {
			    VM.abort(INTERNALERROR);
			    certificate = null; // for compiler
			    certchain = null;
			}

			if (certchain != null) {
			    result = YoixObject.newArray(certchain.length);
			    for (m = 0; m < certchain.length; m++)
				result.putObject(m, YoixObject.newCertificate(certchain[m]));
			} else if (certificate != null)
			    result = YoixObject.newCertificate(certificate);
		    }
		} else if (arg[1].notNull() && arg[1].isCertificate()) {
		    certificate = (Certificate)arg[1].getManagedObject();
		    synchronized(this) {
			if (keystore != null) {
			    try {
				keystore.setCertificateEntry(arg[0].stringValue(), certificate);
				result = YoixObject.newInt(true);
			    }
			    catch(KeyStoreException kse) {
				VM.abort(INTERNALERROR);
			    }

			} else result = YoixObject.newInt(false);
		    }
		} else VM.badArgument(name, 1);
	    } else if (arg[0].notNull() && arg[0].isCertificate()) {
		if (arg.length == 1) {
		    if (keysnap != null) {
			certificate = (Certificate)arg[0].getManagedObject();
			try {
			    str = keysnap.getCertificateAlias((Certificate)arg[0].getManagedObject());
			    if (str != null)
				result = YoixObject.newString(str);
			}
			catch(KeyStoreException kse) {
			    VM.abort(INTERNALERROR);
			}
		    }
		} else VM.badCall(name);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(result == null ? YoixObject.newNull() : result);
    }


    private YoixObject
    builtinKey(String name, YoixObject arg[]) {

	Certificate  certchain[];
	YoixObject   result = null;
	YoixObject   yobj;
	KeyStore     keysnap = keystore;
	String       pswd;
	Key          key;
	int          value;
	int          m;
	int          n;

	if (arg.length >= 1 && arg.length <= 4) {
	    if (arg[0].notNull() && arg[0].isString()) {
		if (arg.length == 1 || arg[1].isNull() || (arg.length == 2 && arg[1].isString())) {
		    if (arg.length == 1 || arg[1].isNull())
			pswd = password;
		    else pswd = arg[1].stringValue();
		    if (pswd == null)
			VM.abort(MISSINGVALUE, N_PASSWORD);
		    if (keysnap != null) {
			try {
			    key = keysnap.getKey(arg[0].stringValue(), pswd.toCharArray());
			    if (key == null)
				value = 0;
			    else if (key instanceof SecretKey)
				value = Cipher.SECRET_KEY;
			    else if (key instanceof PublicKey)
				value = Cipher.PUBLIC_KEY;
			    else if (key instanceof PrivateKey)
				value = Cipher.PRIVATE_KEY;
			    else {
				VM.abort(INTERNALERROR);
				value = -1; // for compiler
			    }
			    if (key != null)
				result = YoixObject.newString(YoixBodyKey.yoixKeyString(key, key.getAlgorithm(), value));
			}
			catch(KeyStoreException kse) {
			    VM.abort(INTERNALERROR);
			}
			catch(NoSuchAlgorithmException nsae) {
			    VM.abort(INTERNALERROR);
			}
			catch(UnrecoverableKeyException uke) {
			    VM.abort(name, BADVALUE, new String[] { "password incorrect or keystore type inappropriate for key type" });
			}
		    }
		} else if (arg[1].isString()) {
		    if (arg[2].isNull() || arg[2].isString()) {
			if (arg[2].isNull())
			    pswd = password;
			else pswd = arg[2].stringValue();
			key = YoixBodyKey.yoixStringKey(arg[1].stringValue());
			if (arg.length == 4) {
			    if (arg[3].notNull() && arg[3].isArray()) {
				certchain = new Certificate[arg[3].sizeof()];
				for (m = 0, n = arg[3].offset(); m < certchain.length; m++, n++) {
				    yobj = arg[3].getObject(n);
				    if (yobj.notNull() && yobj.isCertificate())
					certchain[m] = (Certificate)yobj.getManagedObject();
				    else VM.badArgumentValue(name, 3, new String[] { "bad value in array at element offset " + n });
				}
			    } else {
				VM.badArgument(name, 3);
				certchain = null; // for compiler
			    }
			} else if (arg.length == 3 && !(key instanceof PrivateKey)) {
			    certchain = null;
			} else {
			    VM.badCall(name);
			    certchain = null; // for compiler
			}
			synchronized(this) {
			    if (keystore != null) {
				if (pswd == null)
				    VM.abort(MISSINGVALUE, N_PASSWORD);
				try {
				    keystore.setKeyEntry(arg[0].stringValue(), key, pswd.toCharArray(), certchain);
				    result = YoixObject.newInt(true);
				}
				catch(KeyStoreException kse) {
				    VM.abort(INTERNALERROR);
				}
			    } else result = YoixObject.newInt(false);
			}
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(result == null ? YoixObject.newNull() : result);
    }


    private synchronized YoixObject
    builtinOutput(String name, YoixObject arg[]) {

	YoixObject      result = null;
	YoixObject      yobj;
	OutputStream    ostream;
	String          fname = null;
	boolean         success = false;

	if (arg.length == 0) {
	    if (filename != null) {
		if (password != null) {
		    try {
			ostream = new FileOutputStream(fname = filename);
			keystore.store(ostream, password.toCharArray());
			success = true;
		    }
		    catch(FileNotFoundException fnfe) {
			VM.abort(BADVALUE, new String[] { "could not open file '" + filename + "' because: " + fnfe.getMessage() });
		    }
		    catch(SecurityException se) {
			VM.abort(BADVALUE, new String[] { "security restriction for '" + filename + "' because: " + se.getMessage() });
		    }
		    catch(KeyStoreException kse) {
			VM.abort(INTERNALERROR);
		    }
		    catch(IOException ioe) {
			VM.abort(BADVALUE, new String[] { "could not write data to '" + filename + "' because: " + ioe.getMessage() });
		    }
		    catch(NoSuchAlgorithmException nsae) {
			VM.abort(BADVALUE, new String[] { "could not protect keystore data because: " + nsae.getMessage() });
		    }
		    catch(CertificateException ce) {
			VM.abort(BADVALUE, new String[] { "could not store certificate info because: " + ce.getMessage() });
		    }
		} else VM.abort(MISSINGVALUE, N_PASSWORD);
	    } else VM.abort(MISSINGVALUE, N_FILE);
	} else if (arg.length == 1) {
	    if (arg[0].notNull() && arg[0].isString()) {
		if (password != null) {
		    try {
			ostream = new FileOutputStream(fname = arg[0].stringValue());
			keystore.store(ostream, password.toCharArray());
			success = true;
		    }
		    catch(FileNotFoundException fnfe) {
			VM.abort(BADVALUE, new String[] { "could not open file '" + arg[0].stringValue() + "' because: " + fnfe.getMessage() });
		    }
		    catch(SecurityException se) {
			VM.abort(BADVALUE, new String[] { "security restriction for '" + arg[0].stringValue() + "' because: " + se.getMessage() });
		    }
		    catch(KeyStoreException kse) {
			VM.abort(INTERNALERROR);
		    }
		    catch(IOException ioe) {
			VM.abort(BADVALUE, new String[] { "could not write data to '" + arg[0].stringValue() + "' because: " + ioe.getMessage() });
		    }
		    catch(NoSuchAlgorithmException nsae) {
			VM.abort(BADVALUE, new String[] { "could not protect keystore data because: " + nsae.getMessage() });
		    }
		    catch(CertificateException ce) {
			VM.abort(BADVALUE, new String[] { "could not store certificate info because: " + ce.getMessage() });
		    }
		} else VM.abort(MISSINGVALUE, N_PASSWORD);
	    } else VM.badArgument(name, 0);
	} else if (arg.length == 2) {
	    if (arg[0].notNull() && arg[0].isString()) {
		if (arg[1].notNull() && arg[1].isString()) {
		    try {
			ostream = new FileOutputStream(fname = arg[0].stringValue());
			keystore.store(ostream, arg[1].stringValue().toCharArray());
			success = true;
		    }
		    catch(FileNotFoundException fnfe) {
			VM.abort(BADVALUE, new String[] { "could not open file '" + arg[0].stringValue() + "' because: " + fnfe.getMessage() });
		    }
		    catch(SecurityException se) {
			VM.abort(BADVALUE, new String[] { "security restriction for '" + arg[0].stringValue() + "' because: " + se.getMessage() });
		    }
		    catch(KeyStoreException kse) {
			VM.abort(INTERNALERROR);
		    }
		    catch(IOException ioe) {
			VM.abort(BADVALUE, new String[] { "could not write data to '" + arg[0].stringValue() + "' because: " + ioe.getMessage() });
		    }
		    catch(NoSuchAlgorithmException nsae) {
			VM.abort(BADVALUE, new String[] { "could not protect keystore data because: " + nsae.getMessage() });
		    }
		    catch(CertificateException ce) {
			VM.abort(BADVALUE, new String[] { "could not store certificate info because: " + ce.getMessage() });
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newDouble(YoixMisc.fileSize(fname)));
    }


    private synchronized YoixObject
    getProvider(YoixObject obj) {

	YoixObject  result;
	Provider    provider;

	if (keystore != null) {
	    if ((provider = keystore.getProvider()) != null)
		result = YoixModuleSecure.getProviderInfo(provider);
	    else result = null;
	} else result = null;

	return(result == null ? YoixObject.newNull() : result);
    }


    private synchronized YoixObject
    getSize(YoixObject obj) {

	int  size = 0;

	if (keystore != null) {
	    try {
		size = keystore.size();
	    }
	    catch(KeyStoreException e) {}
	}

	return(YoixObject.newInt(size));
    }


    private synchronized YoixObject
    getType(YoixObject obj) {

	return(keystore != null ? YoixObject.newString(keystore.getType()) : null);
    }


    private synchronized void
    reload() {

	InputStream  inputstream;
	KeyStore     newkeystore = null;
	String       message = null;

	try {
	    if (provider == null)
		newkeystore = KeyStore.getInstance(type);
	    else newkeystore = KeyStore.getInstance(type, provider);
	    if (filename == null || !YoixMisc.isFile(filename)) {
		if (password == null)
		    newkeystore.load(null, null);
		else newkeystore.load(null, password.toCharArray());
	    } else {
		inputstream = new FileInputStream(filename);
		if (password == null)
		    newkeystore.load(inputstream, null);
		else newkeystore.load(inputstream, password.toCharArray());
	    }
	}
	catch(KeyStoreException kse) { // KeyStore.getInstance
	    message = kse.getMessage();
	}
	catch(NoSuchProviderException nspe) { // KeyStore.getInstance
	    message = nspe.getMessage();
	}
	catch(FileNotFoundException fnfe) { // new FileInputStream
	    message = fnfe.getMessage();
	}
	catch(IOException ioe) { // keystore.load()
	    message = ioe.getMessage();
	}
	catch(NoSuchAlgorithmException nsae) { // keystore.load()
	    message = nsae.getMessage();
	}
	catch(CertificateException ce) { // keystore.load()
	    message = ce.getMessage();
	}

	if (message != null)
	    VM.abort(BADVALUE, new String[] { "loading keystore (" + message + ")" });

	keystore = newkeystore;
    }


    private synchronized YoixObject
    setFile(YoixObject obj) {

	filename = (obj.sizeof() > 0) ? obj.stringValue() : null;
	return(YoixObject.newString(filename));
    }


    private synchronized YoixObject
    setPassword(YoixObject obj) {

	password = (obj.sizeof() > 0) ? obj.stringValue() : null;
	return(YoixObject.newString(password));
    }


    private synchronized void
    setProvider(YoixObject obj) {

	provider = (obj.sizeof() > 0) ? obj.stringValue() : null;
    }


    private synchronized YoixObject
    setType(YoixObject obj) {

	type = (obj.sizeof() > 0) ? obj.stringValue() : KeyStore.getDefaultType();
	return(YoixObject.newString(type));
    }
}

