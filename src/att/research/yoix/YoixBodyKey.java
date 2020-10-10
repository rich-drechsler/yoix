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
import java.security.spec.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public final
class YoixBodyKey extends YoixPointerActive

{

    private AlgorithmParameters  parameters = null;
    private KeyPairGenerator     keypairgenerator;
    private KeyGenerator         keygenerator;
    private SecretKey            key;
    private KeyPair              keypair;
    private int                  gentype = 0;

    //
    // gentype starts out as 0, which means other values are uninitialized,
    // but eventually should be set to one of these nonzero constants.
    //

    final static int  SYMMETRIC_KEY = 1;
    final static int  ASYMMETRIC_KEY = 2;

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(5);

    static {
	activefields.put(N_ALGORITHM, new Integer(V_ALGORITHM));
	activefields.put(N_KEY, new Integer(V_KEY));
	activefields.put(N_KEYSTRING, new Integer(V_KEYSTRING));
	activefields.put(N_PARAMETERS, new Integer(V_PARAMETERS));
	activefields.put(N_PROVIDER, new Integer(V_PROVIDER));
	activefields.put(N_SPECIFICATION, new Integer(V_SPECIFICATION));
    }

    private static AlgorithmParameters  cipherparams = null;
    private static Cipher               keycipher = null;
    private static Key                  cipherkey = null;

    private final static String  CIPHERALGORITHM = "DES/CBC/PKCS5Padding";
    private final static String  CIPHERPROVIDER = "SunJCE";
    private final static byte[]  CIPHERPARAMS = {
	(byte)0x04, (byte)0x08, (byte)0x21, (byte)0x88,
	(byte)0xDD, (byte)0xB5, (byte)0x57, (byte)0x94,
	(byte)0xC1, (byte)0xEE,
    };

    private final static String  KEYALGORITHM = "DES";
    private final static int     KEYTYPE = Cipher.SECRET_KEY;
    private final static byte[]  KEYBYTES = {
	(byte)0xE3, (byte)0x46, (byte)0xD6, (byte)0x6E,
	(byte)0x62, (byte)0x26, (byte)0x29, (byte)0xFE,
    };

    private final static String  KEYSTRINGDELIM = "\t";

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyKey(YoixObject data) {

	super(data);
	buildKey();
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(KEY);
    }

    ///////////////////////////////////
    //
    // YoixBodyKey Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_KEYSTRING:
		obj = builtinKeyString(name, argv);
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

	    case V_KEY:
	        obj = getKey();
	        break;

	    case V_PARAMETERS:
	        obj = getParameters();
	        break;

	    case V_PROVIDER:
	        obj = getProvider();
	        break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(gentype == SYMMETRIC_KEY ? (Object)key : gentype == ASYMMETRIC_KEY ? (Object)keypair : null);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_KEY:
		    setKey(obj);
		    break;

		case V_SPECIFICATION:
		    setSpecification(obj);
		    break;
	    }
	}

	return(obj);
    }


    static String
    yoixKeyString(Key key, String algorithm, int type) {

	StringBuffer  sb;
	String        str;
	byte          bytearray[] = null;

	if (keycipher != null || buildKeyCipher()) {
	    try {
		keycipher.init(Cipher.WRAP_MODE, cipherkey, cipherparams);
		bytearray = keycipher.wrap(key);
		sb = new StringBuffer(3 + algorithm.length() + 2 * bytearray.length);
		sb.append(algorithm);
		sb.append(KEYSTRINGDELIM);
		sb.append(type);
		sb.append(KEYSTRINGDELIM);
		sb.append(YoixMisc.hexBytesToString(bytearray));
		str = sb.toString();
		keycipher.init(Cipher.ENCRYPT_MODE, cipherkey, cipherparams);
		bytearray = keycipher.doFinal(str.getBytes(YoixConverter.getISO88591Encoding()));
	    }
	    catch(GeneralSecurityException e) {}
	    catch(UnsupportedEncodingException e) {}
	} else VM.abort(INTERNALERROR);

	return(YoixMisc.hexBytesToString(bytearray));
    }


    static Key
    yoixStringKey(String codedkey) {

	StringTokenizer  st;
	StringBuffer     sb;
	String           algorithm;
	String           str;
	byte             bytearray[];
	Key              key = null;
	int              type;

	if (keycipher != null || buildKeyCipher()) {
	    if ((bytearray = YoixMisc.hexStringToBytes(codedkey)) != null) {
		try {
		    keycipher.init(Cipher.DECRYPT_MODE, cipherkey, cipherparams);
		    bytearray = keycipher.doFinal(bytearray);
		    str = new String(bytearray, YoixConverter.getISO88591Encoding());
		    st = new StringTokenizer(str, KEYSTRINGDELIM, false);
		    if (st.hasMoreTokens()) {
			algorithm = st.nextToken();
			if (st.hasMoreTokens()) {
			    type = Integer.parseInt(st.nextToken());
			    if (st.hasMoreTokens()) {
				codedkey = st.nextToken();
				if ((bytearray = YoixMisc.hexStringToBytes(codedkey)) != null) {
				    keycipher.init(Cipher.UNWRAP_MODE, cipherkey, cipherparams);
				    key = keycipher.unwrap(bytearray, algorithm, type);
				}
			    }
			}
		    }
		}
		catch(GeneralSecurityException e) {}
		catch(UnsupportedEncodingException e) {}
	    }
	}

	return(key);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildKey() {

	key = null;
	keygenerator = null;
	keypairgenerator = null;
	gentype = 0;
	setField(N_SPECIFICATION);
    }


    private static synchronized boolean
    buildKeyCipher() {

	SecretKeyFactory  factory;
	DESKeySpec        keyspec;

	if (keycipher == null) {
	    try {
		keycipher = Cipher.getInstance(CIPHERALGORITHM, CIPHERPROVIDER);
		keyspec = new DESKeySpec(KEYBYTES);
		factory = SecretKeyFactory.getInstance(KEYALGORITHM, CIPHERPROVIDER);
		cipherkey = factory.generateSecret(keyspec);
		cipherparams = AlgorithmParameters.getInstance(KEYALGORITHM, CIPHERPROVIDER);
		cipherparams.init(CIPHERPARAMS);
	    }
	    catch(GeneralSecurityException e) {}
	    catch(IOException e) {}
	}

	return(keycipher != null);
    }


    private synchronized YoixObject
    builtinKeyString(String name, YoixObject arg[]) {

	String  keystring = null;
	Key     wrapkey = null;
	int     type = -1;

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 1) {
		if (arg[0].isInteger()) {
		    switch(type = arg[0].intValue()) {
			case Cipher.SECRET_KEY:
			    if (key != null)
				wrapkey = key;
			    else VM.badArgumentValue(name, 0);
			    break;

			case Cipher.PRIVATE_KEY:
			    if (keypair != null)
				wrapkey = keypair.getPrivate();
			    else VM.badArgumentValue(name, 0);
			    break;

			case Cipher.PUBLIC_KEY:
			    if (keypair != null)
				wrapkey = keypair.getPublic();
			    else VM.badArgumentValue(name, 0);
			    break;

			default:
			    VM.badArgumentValue(name, 0);
			    break;
		    }
		} else VM.badArgument(name, 0);
	    } else {
		if (key != null) {
		    wrapkey = key;
		    type = Cipher.SECRET_KEY;
		} else if (keypair != null) {
		    wrapkey = keypair.getPublic();
		    type = Cipher.PUBLIC_KEY;
		} else VM.badCall(name);
	    }
	    if (wrapkey != null)
		keystring = YoixBodyKey.yoixKeyString(wrapkey, wrapkey.getAlgorithm(), type);
	    else VM.abort(INTERNALERROR);	// impossible, I believe
	} else VM.badCall(name);

	return(YoixObject.newString(keystring));
    }


    private synchronized YoixObject
    getAlgorithm() {

	String  name = null;

	if (key == null) {
	    if (keypair != null) {
		name = keypair.getPrivate().getAlgorithm();
		if (!name.equals(keypair.getPublic().getAlgorithm()))
		    VM.abort(INTERNALERROR); // thought they would always be the same
	    }
	} else name = key.getAlgorithm();

	return(YoixObject.newString(name));
    }


    private synchronized YoixObject
    getKey() {

	YoixObject  result = null;
	PrivateKey  prikey;
	YoixObject  yobj;
	PublicKey   pubkey;
	byte        bytearray[];
	int         n;

	if (gentype == SYMMETRIC_KEY && key != null) {
	    result = YoixObject.newDictionary(2);

	    bytearray = key.getEncoded();
	    yobj = YoixMake.yoixByteArray(bytearray);
	    result.putObject("secret_bytes", yobj == null ? YoixObject.newArray() : yobj);
	    yobj = YoixMake.yoixByteArrayString(bytearray);
	    result.putObject("secret_text", yobj == null ? YoixObject.newString() : yobj);
	} else if (gentype == ASYMMETRIC_KEY && keypair != null) {
	    result = YoixObject.newDictionary(4);

	    pubkey = keypair.getPublic();
	    bytearray = pubkey.getEncoded();
	    yobj = YoixMake.yoixByteArray(bytearray);
	    result.putObject("public_bytes", yobj == null ? YoixObject.newArray() : yobj);
	    yobj = YoixMake.yoixByteArrayString(bytearray);
	    result.putObject("public_text", yobj == null ? YoixObject.newString() : yobj);

	    prikey = keypair.getPrivate();
	    bytearray = prikey.getEncoded();
	    yobj = YoixMake.yoixByteArray(bytearray);
	    result.putObject("private_bytes", yobj == null ? YoixObject.newArray() : yobj);
	    yobj = YoixMake.yoixByteArrayString(bytearray);
	    result.putObject("private_text", yobj == null ? YoixObject.newString() : yobj);
	}

	return (result == null ? YoixObject.newDictionary() : result);
    }


    private static YoixObject
    getKeyDictionary(String refname, Key key, int type) {

	YoixObject  dict;
	YoixObject  yobj;
	Method      methods[];
	Object      obj;
 	String      name = null;
	Class       cls;
	byte        bytearray[];
	int         m;

	// assumes key is not null

	cls = key.getClass();
	try {
	    methods = cls.getMethods();
	}
	catch(SecurityException se) {
	    methods = new Method[0];
	}
	dict = YoixObject.newDictionary(4);
	dict.setGrowable(true);
	dict.setGrowto(-1);
	dict.putInt(N_TYPE, type);
	dict.putString("algorithm", key.getAlgorithm());
	dict.putString("format", key.getFormat());
	dict.putObject("encoded", YoixMake.yoixByteArray(key.getEncoded()));
	for (m = 0; m < methods.length; m++) {
	    name = methods[m].getName();
	    try {
		if (name.startsWith("get") && methods[m].getParameterTypes().length == 0) {
		    obj = methods[m].invoke(key, null);
		    name = name.substring(3).toLowerCase();
		    if (obj instanceof BigInteger) {
			dict.putString(name, ((BigInteger)obj).toString(16));
		    } else if (obj instanceof BigDecimal) { // unused so far
			dict.putString(name, obj.toString());
		    } else if (obj instanceof Integer) {
			dict.putInt(name, ((Integer)obj).intValue());
		    } else if (obj instanceof Double) {
			dict.putDouble(name, ((Double)obj).doubleValue());
		    } else if (obj instanceof Float) {
			dict.putDouble(name, ((Float)obj).doubleValue());
		    } else if (obj instanceof byte[]) {
			bytearray = (byte[])obj;
			yobj = YoixMake.yoixByteArray(bytearray);
			dict.putObject(name, yobj);
		    } else if (obj instanceof String) {
			dict.putString(name, (String)obj);
		    } else if (obj instanceof Class) {
			dict.putString(name, ((Class)obj).getName());
		    } else {
			dict.putString(name, obj.toString());
		    }
		}
	    }
	    catch(IllegalAccessException e) {}
	    catch(InvocationTargetException e) {}
	}
	dict.setGrowable(false);

	return(dict);
    }


    private synchronized YoixObject
    getParameters() {

	YoixObject  result;

	if (key != null) {
	    result = YoixObject.newDictionary(1);
	    result.putObject("secretkey", getKeyDictionary(N_PARAMETERS, key, Cipher.SECRET_KEY));
	} else if (keypair != null) {
	    result = YoixObject.newDictionary(2);
	    result.putObject("publickey", getKeyDictionary(N_PARAMETERS, keypair.getPublic(), Cipher.PUBLIC_KEY));
	    result.putObject("privatekey", getKeyDictionary(N_PARAMETERS, keypair.getPrivate(), Cipher.PRIVATE_KEY));
	} else result = null;

	return(result == null ? YoixObject.newDictionary() : result);
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

	if (keygenerator != null || keypairgenerator != null) {
	    provider = (keygenerator != null) ? keygenerator.getProvider() : keypairgenerator.getProvider();
	    if (provider != null) {
		sz = provider.size() + 2;
		if (( pname = provider.getName()) != null)
		    sz++;
		if (( pinfo = provider.getInfo()) != null)
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


    private synchronized void
    setKey(YoixObject obj) {

	if (obj.isNull()) {
	    if (keygenerator != null) {
		try {
		    key = keygenerator.generateKey();
		}
		catch(Exception e) {
		    VM.abort(EXCEPTION, e.getMessage()); // should never happen
		}
	    } else if (keypairgenerator != null) {
		try {
		    keypair = keypairgenerator.generateKeyPair();
		}
		catch(Exception e) {
		    VM.abort(EXCEPTION, e.getMessage()); // should never happen
		}
	    }
	} else VM.abort(BADVALUE, N_KEY);
    }


    private synchronized void
    setSpecification(YoixObject obj) {

	AlgorithmParameterSpec  newparameterspec = null;
	AlgorithmParameters     newparameters = null;
	KeyPairGenerator        newkeypairgenerator = null;
	KeyGenerator            newkeygenerator = null;
	SecureRandom            random;
	YoixObject              yobj;
	SecretKey               newkey = null;
	Boolean                 custom;
	Integer                 typeobj;
	Integer                 keysize;
	KeyPair                 newkeypair = null;
	String                  trans;
	String                  provider;
	int                     newtype = 0;

	if (obj.notNull()) {
	    if (obj.isDictionary()) {
		if ((typeobj = obj.getDefinedInteger(N_TYPE)) != null) {
		    newtype = typeobj.intValue();
		    trans = obj.getDefinedString(N_TRANSFORMATION);
		    provider = obj.getDefinedString(N_PROVIDER);
		    random = obj.getDefinedSecureRandom(N_RANDOM);
		    keysize = obj.getDefinedInteger(N_KEYSIZE);
		    custom = obj.getDefinedBoolean(N_CUSTOM);

		    if (trans != null) {
			if (newtype == SYMMETRIC_KEY) {
			    try {
				if (provider == null)
				    newkeygenerator = KeyGenerator.getInstance(trans);
				else newkeygenerator = KeyGenerator.getInstance(trans, provider);
			    }
			    catch(Exception e) {
				VM.abort(BADVALUE, N_SPECIFICATION);
			    }

			    if (custom != null && custom.booleanValue()) {
				if ((newparameterspec = YoixModuleSecure.algorithmParameterSpec(obj, trans, N_SPECIFICATION)) == null)
				    VM.abort(BADVALUE, N_SPECIFICATION, N_CUSTOM);
				if (random != null) {
				    try {
					newkeygenerator.init(newparameterspec, random);
				    }
				    catch(Exception e) {
					VM.abort(BADVALUE, N_SPECIFICATION, "["+N_CUSTOM+"|"+N_RANDOM+"]");
				    }
				} else {
				    try {
					newkeygenerator.init(newparameterspec);
				    }
				    catch(Exception e) {
					VM.abort(BADVALUE, N_SPECIFICATION, N_CUSTOM);
				    }
				}
				try {
				    newparameters = AlgorithmParameters.getInstance(trans);
				    newparameters.init(newparameterspec);
				}
				catch(Exception e) {
				    VM.abort(BADVALUE, N_SPECIFICATION, new String[] { e.getMessage() });
				}
			    } else {
				if (keysize == null || keysize.intValue() <= 0) {
				    if (random != null) {
					try {
					    newkeygenerator.init(random);
					}
					catch(Exception e) {
					    VM.abort(BADVALUE, N_SPECIFICATION, N_RANDOM);
					}
				    }
				} else {
				    if (random != null) {
					try {
					    newkeygenerator.init(keysize.intValue(), random);
					}
					catch(Exception e) {
					    VM.abort(BADVALUE, N_SPECIFICATION, "["+N_KEYSIZE+"|"+N_RANDOM+"]");
					}
				    } else {
					try {
					    newkeygenerator.init(keysize.intValue());
					}
					catch(Exception e) {
					    VM.abort(BADVALUE, N_SPECIFICATION, N_KEYSIZE);
					}
				    }
				}
			    }

			    try {
				newkey = newkeygenerator.generateKey();
			    }
			    catch(Exception e) {
				VM.abort(BADVALUE, N_SPECIFICATION, new String[] { e.getMessage() });
			    }

			} else if (newtype == ASYMMETRIC_KEY) {
			    try {
				if (provider == null)
				    newkeypairgenerator = KeyPairGenerator.getInstance(trans);
				else newkeypairgenerator = KeyPairGenerator.getInstance(trans, provider);
			    }
			    catch(Exception e) {
				VM.abort(BADVALUE, N_SPECIFICATION, new String[] { e.getMessage() });
			    }

			    if (custom != null && custom.booleanValue()) {
				if ((newparameterspec = YoixModuleSecure.algorithmParameterSpec(obj, trans, N_SPECIFICATION)) == null)
				    VM.abort(BADVALUE, N_SPECIFICATION, N_CUSTOM);
				if (random != null) {
				    try {
					newkeypairgenerator.initialize(newparameterspec, random);
				    }
				    catch(Exception e) {
					VM.abort(BADVALUE, N_SPECIFICATION, "["+N_CUSTOM+"|"+N_RANDOM+"]");
				    }
				} else {
				    try {
					newkeypairgenerator.initialize(newparameterspec);
				    }
				    catch(Exception e) {
					VM.abort(BADVALUE, N_SPECIFICATION, N_CUSTOM);
				    }
				}
				try {
				    newparameters = AlgorithmParameters.getInstance(trans);
				    newparameters.init(newparameterspec);
				}
				catch(Exception e) {
				    VM.abort(BADVALUE, N_SPECIFICATION, new String[] { e.getMessage() });
				}
			    } else {
				if (keysize != null && keysize.intValue() > 0) {
				    if (random != null) {
					try {
					    newkeypairgenerator.initialize(keysize.intValue(), random);
					}
					catch(Exception e) {
					    VM.abort(BADVALUE, N_SPECIFICATION, "["+N_KEYSIZE+"|"+N_RANDOM+"]");
					}
				    } else {
					try {
					    newkeypairgenerator.initialize(keysize.intValue());
					}
					catch(Exception e) {
					    VM.abort(BADVALUE, N_SPECIFICATION, N_KEYSIZE);
					}
				    }
				}
			    }

			    try {
				newkeypair = newkeypairgenerator.generateKeyPair();
			    }
			    catch(Exception e) {
				VM.abort(BADVALUE, N_SPECIFICATION, new String[] { e.getMessage() });
			    }


			} else VM.abort(BADVALUE, N_SPECIFICATION, N_TYPE);
		    } else VM.abort(MISSINGVALUE, N_SPECIFICATION, N_TRANSFORMATION);
		} else VM.abort(MISSINGVALUE, N_SPECIFICATION, N_TYPE);
	    } else VM.abort(TYPECHECK, N_SPECIFICATION);
	}

	gentype = newtype;
	parameters = newparameters;
	key = newkey;
	keygenerator = newkeygenerator;
	keypair = newkeypair;
	keypairgenerator = newkeypairgenerator;
    }
}

