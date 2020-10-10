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
import java.math.*;
import java.net.*;
import java.util.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.*;
import javax.crypto.*;

abstract
class YoixModuleSecure extends YoixModule

{

    public final static int YOIX_ADDPROVIDER = 1;
    public final static int YOIX_CHECKPROVIDER = 2;
    public final static int YOIX_REMOVEPROVIDER = 3;
    public final static int YOIX_SECURITYPROPERTY = 4;

    private static HashMap  paramspec;

    static String  provider_table[][];
    static Class   specclasses[];

    private final static String   ALIAS_PREFIX = "Alg.Alias.";
    private final static int      ALIAS_PREFIX_LENGTH = ALIAS_PREFIX.length();

    //
    // This paramspec stuff needs to be re-worked entirely to generalize
    // it for other providers
    //

    static {
	paramspec = new HashMap(9);

	paramspec.put("DHGen", new Object[] {
	    new ParamSpec[] {
		new ParamSpec("primesize", "int", int.class),
		new ParamSpec("exponentsize", "int", int.class),
	    },
        });
	paramspec.put("DH", new Object[] {
	    new ParamSpec[] {
		new ParamSpec("primemodulus", "String", BigInteger.class),
		new ParamSpec("basegenerator", "String", BigInteger.class),
		new ParamSpec("bits", "int", int.class),
	    },
	    new ParamSpec[] {
		new ParamSpec("primemodulus", "String", BigInteger.class),
		new ParamSpec("basegenerator", "String", BigInteger.class),
	    },
        });
	paramspec.put("DSA", new Object[] {
	    new ParamSpec[] {
		new ParamSpec("prime", "String", BigInteger.class),
		new ParamSpec("base", "String", BigInteger.class),
		new ParamSpec("subprime", "String", BigInteger.class),
	    },
        });
	paramspec.put("Iv", new Object[] {
	    new ParamSpec[] {
		new ParamSpec("bytes", "int", byte.class, true),
		new ParamSpec("length", "int", int.class),
	    },
	    new ParamSpec[] {
		new ParamSpec("bytes", "int", byte.class, true),
	    },
        });
	paramspec.put("PBE", new Object[] {
	    new ParamSpec[] {
		new ParamSpec("salt", "int", byte.class, true),
		new ParamSpec("iterations", "int", int.class),
	    },
        });
	paramspec.put("PSS", new Object[] {
	    new ParamSpec[] {
		new ParamSpec("saltlength", "int", int.class),
	    },
        });
	paramspec.put("RC2", new Object[] {
	    new ParamSpec[] {
		new ParamSpec("keybits", "int", int.class),
		new ParamSpec("initializer", "int", byte.class, true),
	    },
	    new ParamSpec[] {
		new ParamSpec("keybits", "int", int.class),
	    },
        });
	paramspec.put("RC5", new Object[] {
	    new ParamSpec[] {
		new ParamSpec("version", "int", int.class),
		new ParamSpec("rounds", "int", int.class),
		new ParamSpec("wordsize", "int", int.class),
		new ParamSpec("initializer", "int", byte.class, true),
	    },
	    new ParamSpec[] {
		new ParamSpec("version", "int", int.class),
		new ParamSpec("rounds", "int", int.class),
		new ParamSpec("wordsize", "int", int.class),
	    },
        });
	paramspec.put("RSAKeyGen", new Object[] {
	    new ParamSpec[] {
		new ParamSpec("keysize", "int", int.class),
		new ParamSpec("publicexponent", "String", BigInteger.class),
	    },
        });

	specclasses = new Class[paramspec.size()];
	Iterator iter = paramspec.keySet().iterator();
	int   n = 0;
	Class    cls;
	while(iter.hasNext()) {
	    try {
		cls = Class.forName(((String)(iter.next())) + "ParameterSpec");
		specclasses[n++] = cls;
	    }
	    catch(Exception e) {
		specclasses[n++] = null;
	    };
	}
    };

    static String  $MODULENAME = M_SECURE;

    static Integer  $ADDPROVIDER = new Integer(YOIX_ADDPROVIDER);
    static Integer  $CHECKPROVIDER = new Integer(YOIX_CHECKPROVIDER);
    static Integer  $REMOVEPROVIDER = new Integer(YOIX_REMOVEPROVIDER);
    static Integer  $SECURITYPROPERTY = new Integer(YOIX_SECURITYPROPERTY);

    static Integer  $DECRYPT_MODE = new Integer(Cipher.DECRYPT_MODE);
    static Integer  $ENCRYPT_MODE = new Integer(Cipher.ENCRYPT_MODE);
    static Integer  $PRIVATE_KEY = new Integer(Cipher.PRIVATE_KEY);
    static Integer  $PUBLIC_KEY = new Integer(Cipher.PUBLIC_KEY);
    static Integer  $SECRET_KEY = new Integer(Cipher.SECRET_KEY);
    static Integer  $UNWRAP_MODE = new Integer(Cipher.UNWRAP_MODE);
    static Integer  $WRAP_MODE = new Integer(Cipher.WRAP_MODE);

    static Integer  $SYMMETRIC_KEY = new Integer(YoixBodyKey.SYMMETRIC_KEY);
    static Integer  $ASYMMETRIC_KEY = new Integer(YoixBodyKey.ASYMMETRIC_KEY);

    static Object  $module[] = {
    //
    // NAME                      ARG                  COMMAND     MODE   REFERENCE
    // ----                      ---                  -------     ----   ---------
       null,                     "17",                $LIST,      $RORO, $MODULENAME,

       "ADDPROVIDER",            $ADDPROVIDER,        $INTEGER,   $LR__, null,
       "CHECKPROVIDER",          $CHECKPROVIDER,      $INTEGER,   $LR__, null,
       "REMOVEPROVIDER",         $REMOVEPROVIDER,     $INTEGER,   $LR__, null,
       "SECURITYPROPERTY",       $SECURITYPROPERTY,   $INTEGER,   $LR__, null,

       "DECRYPT_MODE",           $DECRYPT_MODE,       $INTEGER,   $LR__, null,
       "ENCRYPT_MODE",           $ENCRYPT_MODE,       $INTEGER,   $LR__, null,
       "PUBLIC_KEY",             $PUBLIC_KEY,         $INTEGER,   $LR__, null,
       "PRIVATE_KEY",            $PRIVATE_KEY,        $INTEGER,   $LR__, null,
       "SECRET_KEY",             $SECRET_KEY,         $INTEGER,   $LR__, null,
       "UNWRAP_MODE",            $UNWRAP_MODE,        $INTEGER,   $LR__, null,
       "WRAP_MODE",              $WRAP_MODE,          $INTEGER,   $LR__, null,

       "SYMMETRIC_KEY",          $SYMMETRIC_KEY,      $INTEGER,   $LR__, null,
       "ASYMMETRIC_KEY",         $ASYMMETRIC_KEY,     $INTEGER,   $LR__, null,

       "adjustSecurity",         "-2",                $BUILTIN,   $LR_X, null,
       //"algorithmSpec",          "",                  $BUILTIN,   $LR_X, null,
       "getCertificates",        "-2",                $BUILTIN,   $LR_X, null,
       "getProviderInfo",        "",                  $BUILTIN,   $LR_X, null,
       "getProviders",           "",                  $BUILTIN,   $LR_X, null,
       //"makeCipher",             "-1",                $BUILTIN,   $LR_X, null,

       T_CIPHER,                 "9",                 $DICT,      $L___, T_CIPHER,
       N_MAJOR,                  $CIPHER,             $INTEGER,   $LR__, null,
       N_MINOR,                  "0",                 $INTEGER,   $LR__, null,
       N_ALGORITHM,              T_STRING,            $NULL,      $LR__, null,
       N_INITIALIZER,            T_OBJECT,            $NULL,      $RW_,  null,
       N_OPMODE,                 "-1",                $INTEGER,   $RW_,  null,
       N_PARAMETERS,             T_DICT,              $NULL,      $LR__, null,
       N_PROVIDER,               T_DICT,              $NULL,      $LR__, null,
       N_SPECIFICATION,          T_OBJECT,            $NULL,      $RW_,  null,
       N_TEXT,                   T_OBJECT,            $NULL,      $RW_,  null,

       T_KEY,                    "8",                 $DICT,      $L___, T_KEY,
       N_MAJOR,                  $KEY,                $INTEGER,   $LR__, null,
       N_MINOR,                  "0",                 $INTEGER,   $LR__, null,
       N_ALGORITHM,              T_STRING,            $NULL,      $LR__, null,
       N_KEY,                    T_DICT,              $NULL,      $RW_,  null,
       N_KEYSTRING,              T_CALLABLE,          $NULL,      $L__X, null,
       N_PARAMETERS,             T_DICT,              $NULL,      $LR__, null,
       N_PROVIDER,               T_DICT,              $NULL,      $LR__, null,
       N_SPECIFICATION,          T_DICT,              $NULL,      $RW_,  null,

       T_KEYSTORE,               "11",                $DICT,      $L___, T_KEYSTORE,
       N_MAJOR,                  $KEYSTORE,           $INTEGER,   $LR__, null,
       N_MINOR,                  "0",                 $INTEGER,   $LR__, null,
       N_ALIASES,                T_CALLABLE,          $NULL,      $L__X, null,
       N_CERTIFICATE,            T_CALLABLE,          $NULL,      $L__X, null,
       N_FILE,                   T_STRING,            $NULL,      $RW_,  null,
       N_KEY,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_OUTPUT,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_PASSWORD,               T_STRING,            $NULL,      $RW_,  null,
       N_PROVIDER,               T_OBJECT,            $NULL,      $RW_,  null,
       N_SIZE,                   "0",                 $INTEGER,   $LR__, null,
       N_TYPE,                   T_STRING,            $NULL,      $RW_,  null,

       T_CERTIFICATE,            "7",                 $DICT,      $L___, T_CERTIFICATE,
       N_MAJOR,                  $CERTIFICATE,        $INTEGER,   $LR__, null,
       N_MINOR,                  "0",                 $INTEGER,   $LR__, null,
       N_KEYSTRING,              T_STRING,            $NULL,      $LR__, null,
       N_PARAMETERS,             T_DICT,              $NULL,      $LR__, null,
       N_SOURCE,                 T_OBJECT,            $NULL,      $RW_,  null,
       N_SPECIFICATION,          T_OBJECT,            $NULL,      $RW_,  null,
       N_VERIFY,                 T_CALLABLE,          $NULL,      $L__X, null,
    };

    ///////////////////////////////////
    //
    // YoixModuleSecure Methods
    //
    ///////////////////////////////////

    public static YoixObject
    adjustSecurity(YoixObject arg[]) {

	YoixObject       result = null;
	SecurityManager  sm;
	ClassLoader      cl;
	Provider         provider = null;
	Provider         providers[];
	Object           object;
	String           name;
	String           value;
	String           delimiters;
	String           providername;
	Class            providerclass;
	int              position;
	int              n;
	int              retval;

	if (arg[0].isInteger()) {
	    if (arg[1].notNull() && arg[1].isString()) {
		name = arg[1].stringValue();
		switch (arg[0].intValue()) {
		    case YOIX_ADDPROVIDER:
			position = -1;
			if (arg.length >= 3) {
			    if (arg[2].isNull() || arg[2].isString()) {
				if (arg[2].isNull())
				    value = null;
				else value = arg[2].stringValue();
				if (arg.length >= 4) {
				    if (arg[3].isInteger()) {
					position = arg[3].intValue();
					if (arg.length > 4)
					    VM.badCall();
					delimiters = null;
				    } else {
					if (arg[3].isNull())
					    delimiters = null;
					else if (arg[3].isString())
					    delimiters = arg[3].stringValue();
					else {
					    VM.badArgument(3);
					    delimiters = null; // for compiler
					}
					if (arg.length >= 5) {
					    if (arg[4].isInteger())
						position = arg[4].intValue();
					    else VM.badArgument(4);
					    if (arg.length > 5)
						VM.badCall();
					}
				    }
				} else delimiters = null;
			    } else if (arg[3].isInteger()) {
				position = arg[3].intValue();
				value = delimiters = null;
				if (arg.length > 4)
				    VM.badCall();
			    } else {
				VM.badArgument(2);
				value = delimiters = null; // for compiler
			    }
			    if (value == null)
				cl = ClassLoader.getSystemClassLoader();
			    else cl = YoixMisc.classLoader(value, delimiters);
			} else cl = ClassLoader.getSystemClassLoader();
			if (cl != null) {
			    if ((sm = System.getSecurityManager()) instanceof YoixSecurityManager)
				((YoixSecurityManager)sm).checkYoixAddProvider(arg[1]);
			    try {
				if (cl instanceof YoixClassLoader)
				    providerclass = ((YoixClassLoader)cl).findClass(name);
				else providerclass = Class.forName(name);
			    }
			    catch(Exception e) {
				VM.recordException(e);
				providerclass = null; // for compiler
			    }
			    if (providerclass != null) {
				try {
				    object = providerclass.newInstance();
				}
				catch(Exception e) {
				    VM.recordException(e);
				    object = null; // for compiler
				}
				if (object != null) {
				    if (object instanceof Provider)
					provider = (Provider)object;
				    else VM.badArgumentValue(1, new String[] { "not a true provider" });
				    providername = provider.getName();
				    if (Security.getProvider(providername) == null) {
					try {
					    // note: insertProviderAt is 1-based (i.e., starts at 1),
					    // but Yoix arg is 0-based so we add 1.
					    if (position < 0)
						Security.addProvider(provider);
					    else Security.insertProviderAt(provider, 1+position);
					}
					catch(Exception e) {
					    VM.recordException(e);
					    providername = null;
					}
				    }
				    if (providername != null)
					result = YoixObject.newString(providername);
				}
			    }
			}
			break;

		    case YOIX_CHECKPROVIDER:
			if (arg.length >= 3) {
			    if (arg[2].isNull())
				value = delimiters = null;
			    else if (arg[2].isString())
				value = arg[2].stringValue();
			    else {
				VM.badArgument(2);
				value = delimiters = null; // for compiler
			    }
			    if (arg.length >= 4) {
				if (arg[3].isNull())
				    delimiters = null;
				else if (arg[3].isString())
				    delimiters = arg[3].stringValue();
				else {
				    VM.badArgument(3);
				    delimiters = null; // for compiler
				}
				if (arg.length > 4)
				    VM.badCall();
			    } else delimiters = null;
			    try {
				if (value == null)
				    cl = ClassLoader.getSystemClassLoader();
				else cl = YoixMisc.classLoader(value, delimiters);
				if (cl instanceof YoixClassLoader)
				    providerclass = ((YoixClassLoader)cl).findClass(name);
				else providerclass = Class.forName(name);
				object = providerclass.newInstance();
				if (object instanceof Provider) {
				    provider = (Provider)object;
				    providername = provider.getName();
				    if (Security.getProvider(providername) != null)
					retval = 1;
				    else retval = 0;
				} else retval = 0;
			    }
			    catch(Exception e) {
				retval = 0;
			    }
			    result = YoixObject.newInt(retval);
			} else VM.badCall();
			break;

		    case YOIX_REMOVEPROVIDER:
			if (arg.length == 2) {
			    if ((sm = System.getSecurityManager()) instanceof YoixSecurityManager)
				((YoixSecurityManager)sm).checkYoixRemoveProvider(arg[1]);
			    if (Security.getProvider(name) != null) {
				try {
				    Security.removeProvider(name);
				}
				catch(Exception e) {
				    //VM.abort(EXCEPTION, new String[] { e.getMessage() });
				    VM.recordException(e);
				}
				if (Security.getProvider(name) == null)
				    retval = 1;
				else retval = 0;
			    } else retval = 1;
			    result = YoixObject.newInt(retval);
			} else VM.badCall();
			break;

		    case YOIX_SECURITYPROPERTY:
			if (arg.length > 2) {
			    if (arg[2].notNull() && arg[2].isString()) {
				value = arg[2].stringValue();
			    } else {
				VM.badArgument(2);
				value = null; // for compiler
			    }
			    if (arg.length > 3)
				VM.badCall();
			} else value = null;
			try {
			    result = YoixObject.newString(Security.getProperty(name));
			    if (value != null)
				Security.setProperty(name, value);
			}
			catch(Exception e) {
			    //VM.abort(EXCEPTION, new String[] { e.getMessage() });
			    VM.recordException(e);
			    result = null;
			}
			break;

		    default:
			VM.badArgumentValue(1);
			break;
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(result == null ? YoixObject.newNull() : result);
    }


    public static YoixObject
    algorithmSpec(YoixObject arg[]) {

	StringBuffer  sb;
	YoixObject    yobj = null;
	ParamSpec     spec[];
	Iterator      iter;
	boolean       sized;
	Object        specs[];
	String        name;
	int           most;
	int           len;
	int           l;
	int           m;
	int           n;

	//
	// Currently unused - pending reworking of paramspec stuff??
	//

	if (arg.length == 0) {
	    iter = paramspec.keySet().iterator();
	    sb = new StringBuffer();
	    while (iter.hasNext()) {
		name = (String)iter.next();
		sb.append("name=");
		sb.append(name);
		sb.append(" <==Yoix/Java==> ");
		sb.append(name);
		sb.append("ParameterSpec");
		sb.append(NL);
		specs = (Object[])paramspec.get(name);
		for (n = 0; n < specs.length; n++) {
		    sb.append("  ");
		    if (specs.length > 1) {
			sb.append("Custom Specification ");
			sb.append(n+1);
			sb.append(":");
		    } else sb.append("Custom Specification:");
		    sb.append(NL);
		    spec = (ParamSpec[])specs[n];
		    for (m = 0; m < spec.length; m++) {
			sb.append("    ");
			if (spec[m].array) {
			    sb.append("Array ");
			    sb.append(spec[m].name);
			    sb.append(" (");
			    sb.append(spec[m].yoixtype);
			    sb.append(" values)");
			    sb.append(" <==Yoix/Java==> ");
			    sb.append(spec[m].javaclass.getName());
			    sb.append("[] ");
			    sb.append(spec[m].name);
			} else {
			    sb.append(spec[m].yoixtype);
			    sb.append(" ");
			    sb.append(spec[m].name);
			    sb.append(" <==Yoix/Java==> ");
			    sb.append(spec[m].javaclass.getName());
			    sb.append(" ");
			    sb.append(spec[m].name);
			}
			sb.append(NL);
		    }
		}
		sb.append(NL);
	    }
	    yobj = YoixObject.newString(sb.toString());
	} else {
	    if (arg[0].notNull() && arg[0].isString()) {
		if ((specs = (Object[])paramspec.get(name = arg[0].stringValue())) != null) {
		    sized = false;
		    most = 0;
		    for (n = 0; n < specs.length; n++) {
			spec = (ParamSpec[])specs[n];
			if (spec.length == (arg.length - 1)) {
			    sized = true;
			    for (m = 0; m < spec.length; m++) {
				if (arg[1+m].isNull())
				    VM.badArgument(1+m);
				else if (spec[m].array) {
				    if (arg[1+m].isArray() && arg[1+m].sizeof() > 0) {
					len = arg[1+m].length();
					for (l=arg[1+m].offset(); l<len; l++) {
					    if (!spec[m].yoixtype.equals(arg[1+m].getObject(l).typename()))
						break;
					}
					if (l < len)
					    break;
				    } else break;
				} else if (!spec[m].yoixtype.equals(arg[1+m].typename()))
				    break;
				if ((m+1) > most)
				    most = m+1;
			    }
			    if (m == spec.length) {
				yobj = YoixObject.newDictionary(2+spec.length);
				yobj.putString(N_NAME, name);
				yobj.putInt(N_CUSTOM, true);
				for (m = 0; m < spec.length; m++)
				    yobj.putObject(spec[m].name, arg[1+m]);
				break;
			    }
			}
		    }
		    if (!sized)
			VM.badCall();
		    if (n == specs.length && arg.length > (most+1))
			VM.badArgument(most+1);
		} else VM.badArgumentValue(0,  new String[] { name });
	    } else VM.badArgument(0);
	}

	return(yobj);
    }


    public static YoixObject
    getCertificates(YoixObject arg[]) {

	BufferedInputStream  bis;
	CertificateFactory   cf;
	Certificate          certarray[];
	YoixObject           result = null;
	ArrayList            storage;
	String               type;
	String               source;
	String               provider;
	int                  n;

	if (arg.length <= 3) {
	    if (arg[0].notNull() && arg[0].isString()) {
		if (arg[1].notNull() && arg[1].isString()) {
		    type = arg[0].stringValue();
		    source = arg[1].stringValue();
		    if (arg.length > 2) {
			if (arg[2].isNull()) {
			    provider = null;
			} else if (arg[2].isString()) {
			    provider = arg[2].stringValue();
			} else {
			    VM.badArgument(2);
			    provider = null; // for compiler
			}
		    } else provider = null;
		    try {
			switch (YoixMisc.guessStreamType(source)) {
			    case FILE:
				bis = new BufferedInputStream(new FileInputStream(YoixMisc.toYoixPath(source)));
				break;

			    case URL:
				bis = new BufferedInputStream(YoixMisc.getInputStream(new URL(source)));
				break;

			    default:
				VM.abort(INTERNALERROR); // should never get here
				bis = null; // for compiler
				break;
			}
		    }
		    catch(Exception e) {
			VM.recordException(e);
			VM.badArgumentValue(1);
			bis = null; // for compiler
		    }
		    if (provider == null) {
			try {
			    cf = CertificateFactory.getInstance(type);
			}
			catch(Exception e) {
			    VM.recordException(e);
			    VM.badArgumentValue(0);
			    cf = null; // for compiler
			}
		    } else {
			try {
			    cf = CertificateFactory.getInstance(type, provider);
			}
			catch(Exception e) {
			    VM.recordException(e);
			    VM.abort(BADVALUE, new String[] { "type", "provider" });
			    cf = null; // for compiler
			}
		    }
		    storage = new ArrayList();
		    try {
			while (bis.available() > 0) {
			    storage.add(cf.generateCertificate(bis));
			}
		    }
		    catch(Exception e) {
			VM.recordException(e);
			VM.badArgumentValue(1);
		    }
		    certarray = (Certificate[])storage.toArray(new Certificate[0]);
		    result = YoixObject.newArray(certarray.length);
		    for (n = 0; n < certarray.length; n++)
			result.putObject(n, YoixObject.newCertificate(certarray[n]));
		} else VM.badArgument(0);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(result == null ? YoixObject.newNull() : result);
    }


    public static YoixObject
    makeCipher(YoixObject arg[]) {

	YoixObject  data;
	YoixObject  yobj;
	String      transformation = null;
	String      provider = null;
	String      keystring = null;
	int         oparg = -1;
	int         opmode = Cipher.ENCRYPT_MODE;
	int         off;

	//
	// Used internally, actually, in YoixBodyStream, so keep it, but
	// for now, let's not release it as an official built-in
	//

	if (arg[0].notNull()) {
	    if (arg.length == 1) {
		if (arg[0].isArray()) {
		    off = arg[0].offset();
		    if ((yobj = arg[0].getObject(off++, null)) != null && yobj.notNull() && yobj.isString()) {
			transformation = yobj.stringValue();
			if ((yobj = arg[0].getObject(off++, null)) != null && yobj.notNull() && yobj.isString()) {
			    if (arg[0].sizeof() == 2) {
				keystring = yobj.stringValue();
			    } else if (arg[0].sizeof() >= 3) {
				provider = yobj.stringValue();
				if ((yobj = arg[0].getObject(off++, null)) != null) {
				    if (yobj.isInteger()) {
					keystring = provider;
					provider = null;
					opmode = yobj.intValue();
					oparg = 0;
					if (arg[0].sizeof() > 3)
					    VM.badArgument(0);
				    } else if (yobj.notNull() && yobj.isString()) {
					keystring = yobj.stringValue();
					if (arg[0].sizeof() >= 4) {
					    if ((yobj = arg[0].getObject(off++, null)) != null && yobj.isInteger()) {
						opmode = yobj.intValue();
						oparg = 0;
						if (arg[0].sizeof() > 4)
						    VM.badArgument(0);
					    } else VM.badArgumentValue(0, --off);
					}
				    } else VM.badArgumentValue(0, --off);
				} else VM.badArgumentValue(0, --off);
			    } else VM.badArgumentValue(0, --off);
			} else VM.badArgumentValue(0, --off);
		    } else VM.badArgumentValue(0, --off);
		} else if (arg[0].isDictionary()) {
		    if ((yobj = arg[0].getObject(N_TRANSFORMATION, null)) != null && yobj.notNull() && yobj.isString()) {
			transformation = yobj.stringValue();
		    } else VM.badArgumentValue(0, N_TRANSFORMATION);
		    if ((yobj = arg[0].getObject(N_PROVIDER, null)) != null) {
			if (yobj.notNull() && yobj.isString()) {
			    provider = yobj.stringValue();
			} else VM.badArgumentValue(0, N_PROVIDER);
		    }
		    if ((yobj = arg[0].getObject(N_KEYSTRING, null)) != null && yobj.notNull() && yobj.isString()) {
			keystring = yobj.stringValue();
		    } else VM.badArgumentValue(0, N_KEYSTRING);
		    if ((yobj = arg[0].getObject(N_OPMODE, null)) != null) {
			if (yobj.isInteger()) {
			    opmode = yobj.intValue();
			    oparg = 0;
			} else VM.badArgument(0, new String[] { N_OPMODE });
		    }
		} else VM.badArgument(0);
	    } else if (arg.length == 2 || arg.length == 3 || arg.length == 4) {
		if (arg[0].isString()) {
		    transformation = arg[0].stringValue();
		    if (arg[1].notNull() && arg[1].isString()) {
			if (arg.length == 2) {
			    keystring = arg[1].stringValue();
			} else if (arg[2].isInteger()) {
			    keystring = arg[1].stringValue();
			    opmode = arg[2].intValue();
			    oparg = 2;
			} else if (arg[2].notNull() && arg[2].isString()) {
			    provider = arg[1].stringValue();
			    keystring = arg[2].stringValue();
			    if (arg.length == 4) {
				if (arg[3].isInteger()) {
				    opmode = arg[3].intValue();
				    oparg = 3;
				} else VM.badArgument(3);
			    }
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    } else VM.badCall();
	} else VM.badArgument(0);

	switch (opmode) {
	    case Cipher.ENCRYPT_MODE:
	    case Cipher.DECRYPT_MODE:
	    case Cipher.WRAP_MODE:
	    case Cipher.UNWRAP_MODE:
		break;

	    default:
		if (oparg == 0)
		    VM.badArgumentValue(oparg, new String[] { N_OPMODE });
		else VM.badArgumentValue(oparg);
		break;
	}

	data = VM.getTypeTemplate(T_CIPHER);
	data.putInt(N_OPMODE, opmode);

	if (provider != null) {
	    yobj = YoixObject.newDictionary(2);
	    yobj.putString(N_TRANSFORMATION, transformation);
	    yobj.putString(N_PROVIDER, provider);
	    data.putObject(N_SPECIFICATION, yobj);
	} else data.putString(N_SPECIFICATION, transformation);
	data.putString(N_INITIALIZER, keystring);

	return(YoixObject.newCipher(data));
    }


    public static YoixObject
    getProviderInfo(YoixObject arg[]) {

	YoixRERegexp  pats[];
	YoixObject    result = null;
	YoixObject    yobj;
	boolean       nodots = true;
	String        table[][];
	String        row[];
	String        value;
	int           m;
	int           n;

	if (arg == null || arg.length == 0)
	    pats = null;
	else {
	    pats = new YoixRERegexp[arg.length];
	    for (n = 0; n < arg.length; n++) {
		if (arg[n].isNull())
		    pats[n] = null;
		else if (arg[n].isString()) {
		    if ((value = arg[n].stringValue()) == null || (value = value.trim()).length() == 0  || value.equals("*"))
			pats[n] = null;
		    else pats[n] = new YoixRERegexp(value, SHELL_PATTERN|SINGLE_BYTE);
		} else if (arg[n].isRegexp()) {
		    if ((value = arg[n].getString(N_PATTERN)) == null || value.equals("*"))
			pats[n] = null;
		    else pats[n] = (YoixRERegexp)arg[n].getManagedObject();
		} else if (arg[n].isInteger() && n == (arg.length-1)) {
		    nodots = arg[n].booleanValue();
		} else VM.badArgument(n);
	    }
	}

	if ((table = providerSearch(pats, nodots)) != null && table.length > 0) {
	    result = YoixObject.newArray(table.length);
	    for (n = 0; n < table.length; n++) {
		row = table[n];
		yobj = YoixObject.newArray(row.length);
		for (m = 0; m < row.length; m++)
		    yobj.putString(m, row[m]);
		result.putObject(n, yobj);
	    }
	}

	return(result == null ? YoixObject.newArray() : result);
    }


    public static YoixObject
    getProviders(YoixObject arg[]) {

	YoixRERegexp  nmre;
	YoixRERegexp  valre;
	YoixObject    result = null;
	YoixObject    yobj;
	Provider      providers[];
	String        name;
	int           len;
	int           idx;
	int           slot;
	int           n;

	if (arg.length == 0) {
	    providers = Security.getProviders();
	    if (providers != null) {
		result = YoixObject.newDictionary(providers.length);
		for (n = 0; n < providers.length; n++)
		    result.putObject(providers[n].getName(), getProviderInfo(providers[n],n));
	    }
	} else if (arg.length <= 3) {
	    if (arg[0].notNull()) {
		if (arg[0].isString()) {
		    idx = 1;
		    name = arg[0].stringValue();
		    slot = -1;
		} else if (arg[0].isInteger()) {
		    idx = 1;
		    name = null;
		    slot = arg[0].intValue();
		} else {
		    idx = 0;
		    name = null;
		    slot = -1;
		}
		if (arg.length > idx) {
		    if (arg[idx].notNull() && arg[idx].isRegexp())
			nmre = (YoixRERegexp)(arg[idx].getManagedObject());
		    else {
			VM.badArgument(idx);
			nmre = null;
		    }
		    if (arg.length > idx+1) {
			if (arg[idx+1].notNull() && arg[idx+1].isRegexp())
			    valre = (YoixRERegexp)(arg[idx+1].getManagedObject());
			else {
			    VM.badArgument(idx+1);
			    valre = null;
			}
		    } else {
			valre = null;
		    }
		} else {
		    nmre = null;
		    valre = null;
		}
		providers = Security.getProviders();
		if (providers != null) {
		    for (n = 0; n < providers.length; n++) {
			if (
			    (name == null && slot < 0)
			    ||
			    (n == slot)
			    ||
			    (name != null && name.equals(providers[n].getName()))
			    ) {
			    if (nmre == null) {
				result = getProviderInfo(providers[n],n);
				break;
			    } else {
				result = scanProviderInfo(result, providers[n], name == null ? providers[n].getName() + "." : null, nmre, valre);
				if (name != null)
				    break;
			    }
			}
		    }
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(result == null ? YoixObject.newDictionary() : result);
    }

    ///////////////////////////////////
    //
    // YoixModuleSecure Methods for Yoix developers
    //
    ///////////////////////////////////

    static AlgorithmParameterSpec
    algorithmParameterSpec(YoixObject obj, String name, String context) {

	AlgorithmParameterSpec  newparameterspec = null;
	YoixObject              yobj;
	ParamSpec               spec[];
	Boolean                 custom;
	Object                  result;
	Object                  params[];
	Object                  specs[];
	String                  classname;
	Class                   types[];
	byte                    bytearray[];
	int                     len;
	int                     off;
	int                     n;
	int                     m;
	int                     l;

	//
	// handling of custom parameters needs to be kept consistent with the
	// specification in paramspec HashMap (i.e., currently need to handle
	// int, byte[], BigInteger; if new class added, need to add it here to)
	//
	// we can assume obj is notNull and isDictionary
	//

	if (paramspec.containsKey(name)) {
	    specs = (Object[])paramspec.get(name);
	    for (n = 0; n < specs.length; n++) {
		spec = (ParamSpec[])specs[n];
		for (m = 0; n < spec.length; m++) {
		    if ((spec[m].yobj = obj.getObject(spec[m].name)) == null)
			break;
		}
		if (m == spec.length) {
		    types = new Class[spec.length];
		    params = new Object[spec.length];
		    for (m = 0; n < spec.length; m++) {
			if (spec[m].yobj.notNull()) {
			    if (spec[m].array) {
				if (spec[m].yobj.isArray()) {
				    len = spec[m].yobj.sizeof();
				    off = spec[m].yobj.offset();
				    bytearray = new byte[len];
				    params[m] = bytearray;
				    types[m] = bytearray.getClass();
				    for (l=0; l<len; l++) {
					yobj = spec[m].yobj.get(off+l, false);
					if (yobj.notNull()) {
					    if (yobj.typename().equals(spec[m].yoixtype)) {
						if (spec[m].javaclass == byte.class) {
						    bytearray[l] = (byte)(0xFF & spec[m].yobj.intValue());
						} else VM.abort(INTERNALERROR);
					    } else VM.abort(TYPECHECK, context, spec[m].name, new String[] { ""+(l+off) });
					} else VM.abort(NULLPOINTER, context, spec[m].name, new String[] { ""+(l+off) });
				    }
				} else VM.abort(TYPECHECK, context, spec[m].name);
			    } else {
				types[m] =  spec[m].javaclass;
				if (spec[m].yobj.typename().equals(spec[m].yoixtype)) {
				    if (spec[m].javaclass == int.class) {
					params[m] = new Integer(spec[m].yobj.intValue());
				    } else if (spec[m].javaclass == BigInteger.class) {
					try {
					    params[m] = new BigInteger(spec[m].yobj.stringValue().trim());
					}
					catch(Exception e3) {
					    VM.badArgumentValue(context, spec[m].name);
					}
				    } else VM.abort(INTERNALERROR);
				} else VM.abort(TYPECHECK, context, spec[m].name);
			    }
			} else VM.abort(NULLPOINTER, context, spec[m].name);
		    }
		    classname = spec[m].name + "ParameterSpec";
		    result = YoixReflect.newInstance(classname, params, types);
		    if (result instanceof Exception) {
			if (result instanceof ClassNotFoundException)
			    VM.abort(INTERNALERROR);
			else VM.abort(EXCEPTION, context, new String[] { ((Exception)result).getMessage() });
		    } else if (result instanceof AlgorithmParameterSpec) {
			newparameterspec = (AlgorithmParameterSpec)result;
		    } else VM.abort(INTERNALERROR);
		    break;
		}
	    }
	    if (n == specs.length)
		VM.badArgumentValue(context, N_NAME,  new String[] { N_CUSTOM });
	} else VM.abort(TYPECHECK, context, N_NAME);

	return (newparameterspec);
    }


    static Key
    keyParameterSpec(YoixObject obj, String context) {

	SecureRandom  random;
	KeyGenerator  keygen;
	YoixObject    yobj;
	ParamSpec     spec[];
	Boolean       custom;
	Integer       keysizeobj;
	Object        result;
	Object        params[];
	Object        specs[];
	String        classname;
	String        name;
	String        provider;
	Class         types[];
	byte          bytearray[];
	Key           newkey = null;
	int           keysize;
	int           len;
	int           off;
	int           n;
	int           m;
	int           l;

	if (obj.notNull() && obj.isDictionary()) {
	    if ((name = obj.getDefinedString(N_NAME)) == null)
		VM.abort(MISSINGVALUE, context, N_NAME);

	    provider = obj.getDefinedString(N_PROVIDER);
	    try {
		if (provider == null)
		    keygen = KeyGenerator.getInstance(name);
		else keygen = KeyGenerator.getInstance(name, provider);
	    }
	    catch(NoSuchAlgorithmException nsae) {
		VM.badArgumentValue(context, N_NAME);
		keygen = null; // for compiler
	    }
	    catch(NoSuchProviderException nspe) {
		VM.badArgumentValue(context, N_PROVIDER);
		keygen = null; // for compiler
	    }

	    if ((keysizeobj = obj.getDefinedInteger("keysize")) == null)
		keysize = 0;
	    else keysize = keysizeobj.intValue();

	    if ((yobj = obj.getObject("random")) == null)
		random = null;
	    else {
		if (yobj.isRandom() && yobj.getBoolean(N_SECURE))
		    random = (SecureRandom)yobj.getManagedObject();
		else {
		    VM.badArgumentValue(context, "random");
		    random = null; // for compiler
		}
	    }

	    if ((custom = obj.getDefinedBoolean(N_CUSTOM)) != null && custom.booleanValue()) {
		if (paramspec.containsKey(name)) {
		    specs = (Object[])paramspec.get(name);
		    for (n = 0; n < specs.length; n++) {
			spec = (ParamSpec[])specs[n];
			for (m = 0; n < spec.length; m++) {
			    if ((spec[m].yobj = obj.getObject(spec[m].name)) == null)
				break;
			}
			if (m == spec.length) {
			    types = new Class[spec.length];
			    params = new Object[spec.length];
			    for (m = 0; n < spec.length; m++) {
				if (spec[m].yobj.notNull()) {
				    if (spec[m].array) {
					if (spec[m].yobj.isArray()) {
					    len = spec[m].yobj.sizeof();
					    off = spec[m].yobj.offset();
					    bytearray = new byte[len];
					    params[m] = bytearray;
					    types[m] = bytearray.getClass();
					    for (l=0; l<len; l++) {
						yobj = spec[m].yobj.get(off+l, false);
						if (yobj.notNull()) {
						    if (yobj.typename().equals(spec[m].yoixtype)) {
							if (spec[m].javaclass == byte.class) {
							    bytearray[l] = (byte)(0xFF & spec[m].yobj.intValue());
							} else VM.abort(INTERNALERROR);
						    } else VM.abort(TYPECHECK, context, spec[m].name, new String[] { ""+(l+off) });
						} else VM.abort(NULLPOINTER, context, spec[m].name, new String[] { ""+(l+off) });
					    }
					} else VM.abort(TYPECHECK, context, spec[m].name);
				    } else {
					types[m] =  spec[m].javaclass;
					if (spec[m].yobj.typename().equals(spec[m].yoixtype)) {
					    if (spec[m].javaclass == int.class) {
						params[m] = new Integer(spec[m].yobj.intValue());
					    } else if (spec[m].javaclass == BigInteger.class) {
						try {
						    params[m] = new BigInteger(spec[m].yobj.stringValue().trim());
						}
						catch(Exception e3) {
						    VM.badArgumentValue(context, spec[m].name);
						}
					    } else if (spec[m].javaclass == SecureRandom.class) {
						if (spec[m].yobj.getBoolean(N_SECURE))
						    params[m] = spec[m].yobj.getManagedObject();
						else VM.badArgumentValue(context, spec[m].name);
					    } else VM.abort(INTERNALERROR);
					} else VM.abort(TYPECHECK, context, spec[m].name);
				    }
				} else VM.abort(NULLPOINTER, context, spec[m].name);
			    }
			    classname = spec[m].name + "ParameterSpec";
			    result = YoixReflect.newInstance(classname, params, types);
			    if (result instanceof Exception) {
				if (result instanceof ClassNotFoundException)
				    VM.abort(INTERNALERROR);
				else VM.abort(EXCEPTION, context, new String[] { ((Exception)result).getMessage() });
			    } else if (result instanceof AlgorithmParameterSpec) {
				try {
				    if (random == null)
					keygen.init((AlgorithmParameterSpec)result);
				    else keygen.init((AlgorithmParameterSpec)result, random);
				}
				catch(Exception e) {
				    VM.abort(EXCEPTION, context, new String[] { e.getMessage() });
				}
			    } else VM.abort(INTERNALERROR);
			}
			break;
		    }
		    if (n == specs.length)
			VM.badArgumentValue(context, N_NAME,  new String[] { N_CUSTOM });
		} else VM.badArgumentValue(context, N_NAME,  new String[] { N_CUSTOM });
	    } else {
		if (keysize > 0 || random != null) {
		    try {
			if (keysize > 0) {
			    if (random != null)
				keygen.init(keysize, random);
			    else keygen.init(keysize);
			} else keygen.init(random);
		    }
		    catch(Exception e) {
			VM.abort(EXCEPTION, context, new String[] { e.getMessage() });
		    }
		} else VM.badArgumentValue(context, "keysize/random");
	    }
	} else {
	    VM.abort(TYPECHECK, context);
	    keygen = null; // for compiler
	}

	return (keygen == null ? null : keygen.generateKey());
    }


    static YoixObject
    getProviderInfo(Provider provider) {

	Provider  providers[];
	String    name;
	int       n;
	int       slot = -1;

	if (provider != null) {
	    name = provider.getName();
	    providers = Security.getProviders();
	    if (name != null && providers != null) {
		for (n = 0; n < providers.length; n++) {
		    if (name.equals(providers[n].getName())) {
			slot = n;
			break;
		    }
		}
	    }
	}
	return(provider == null ? YoixObject.newDictionary() : getProviderInfo(provider, slot));
    }


    static YoixObject
    getProviderInfo(Provider provider, int slot) {

	Enumeration  enm;
	YoixObject   result;
	String       name;
	String       pname;
	String       pinfo;
	int          sz;

	if (provider != null) {
	    sz = provider.size() + 5;
	    result = YoixObject.newDictionary(sz);
	    enm = provider.propertyNames();
	    while (enm.hasMoreElements()) {
		name = (String)enm.nextElement();
		result.putString(name, provider.getProperty(name));
	    }
	    pname = provider.getName();
	    pinfo = provider.getInfo();
	    if (pname != null) // will this ever be null?
		result.putString("name", pname);
	    else result.putObject("name", YoixObject.newString());
	    if (pinfo != null) // I think this one may be null sometimes
		result.putString("info", pinfo);
	    else result.putObject("info", YoixObject.newString());
	    result.putInt("slot", slot);
	    result.putString("summary", provider.toString());
	    result.putString("version", "" + provider.getVersion());
	} else result = null;

	return(result == null ? YoixObject.newDictionary() : result);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static YoixObject
    scanProviderInfo(YoixObject dict, Provider provider, String prefix, YoixRERegexp nmre, YoixRERegexp valre) {

	Enumeration  enm;
	String       extras[];
	String       name;
	String       value;
	String       pname;
	String       pinfo;
	int          n;

	// we assume dict is growable or null

	if (provider != null) {
	    enm = provider.propertyNames();
	    while (enm.hasMoreElements()) {
		name = (String)enm.nextElement();
		if (nmre.exec(name, null)) {
		    value = provider.getProperty(name);
		    if (valre == null || valre.exec(value, null)) {
			if (dict == null) {
			    dict = YoixObject.newDictionary(1);
			    dict.setGrowable(true);
			    dict.setGrowto(-1);
			}
			dict.putString(prefix == null ? name : prefix + name, value);
		    }
		}
	    }
	    extras = new String[] {
		"name", provider.getName(),
		"info", provider.getInfo(),
		"summary", provider.toString(),
		"version", "" + provider.getVersion(),
	    };
	    for (n = 0; n < extras.length; n += 2) {
		if (nmre.exec(extras[n], null)) {
		    if (valre == null || valre.exec(extras[n+1], null)) {
			if (dict == null) {
			    dict = YoixObject.newDictionary(1);
			    dict.setGrowable(true);
			    dict.setGrowto(-1);
			}
			name = (prefix == null ? extras[n] : prefix+extras[n]);
			if (extras[n+1] != null)
			    dict.putString(name, extras[n+1]);
			else dict.putObject(name, YoixObject.newString());
		    }
		}
	    }
	}
	return(dict);
    }


    private static String[][]
    providerTable() {

	StringTokenizer  st;
	Enumeration      enm;
	Provider         providers[];
	Provider         provider;
	String           name;
	String           value;
	String           pname;
	Vector           vec;
	int              n;

	vec = new Vector();
	providers = Security.getProviders();
	if (providers != null) {
	    for (n = 0; n < providers.length; n++) {
		provider = providers[n];

		if (provider != null) {
		    pname = provider.getName();
		    enm = provider.propertyNames();
		    while (enm.hasMoreElements()) {
			name = (String)enm.nextElement();
			if (name == null)
			    VM.abort(INTERNALERROR); //just wonder if it can happen
			value = provider.getProperty(name);
			if (name.startsWith(ALIAS_PREFIX))
			    name = name.substring(ALIAS_PREFIX_LENGTH);
			st = new StringTokenizer(name, ".", false);
			vec.addElement(new String[] { pname, st.nextToken(), st.nextToken("\n").substring(1), value });
		    }
		}
	    }
	}
	return((String[][])vec.toArray(new String[0][0]));
    }


    static String[][]
    providerSearch(YoixRERegexp[] pats, boolean nodots) {

	String  provider_table[][];
	String  row[];
	Vector  vec = null;
	int     plen;
	int     m;
	int     n;

	if ((provider_table = providerTable()) != null) {
	    vec = new Vector(provider_table.length);
	    plen = (pats == null ? 0 : pats.length);
	    for (n = 0; n < provider_table.length; n++) {
		row = provider_table[n];
		for (m = 0; m < row.length; m++) {
		    if (
			(m < plen && pats[m] != null && !pats[m].exec(row[m], null))
			||
			(nodots && (row[m].indexOf('.') >= 0))
			) break;
		}

		if (m == row.length)
		    vec.addElement(row);
	    }
	}
	return((vec == null || vec.size() == 0) ? null : (String[][])vec.toArray(new String[0][0]));
    }
}

class ParamSpec {
    boolean  array;
    String   name;
    String   yoixtype;
    Class    javaclass;

    YoixObject yobj; // used for storage

    ParamSpec(String name, String yoixtype, Class javaclass) {
	this(name, yoixtype, javaclass, false);
    }

    ParamSpec(String name, String yoixtype, Class javaclass, boolean array) {
	this.name = name;
	this.yoixtype = yoixtype;
	this.javaclass = javaclass;
	this.array = array;
    }
}

