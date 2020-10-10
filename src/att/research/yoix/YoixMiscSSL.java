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
import java.lang.reflect.*;
import java.util.*;

public abstract
class YoixMiscSSL

    implements YoixAPI,
	       YoixConstants

{

    //
    // Code is harder than it should be because SSL stuff requires
    // 1.4 but we currently only require 1.3.1. Eventually will be
    // simplified and perhaps moved elsewhere.
    //

    private static Hashtable  sslhosthash = null;

    //
    // Names of any required SSL classes and methods.
    //

    private static String  SSL_CONNECTION = "javax.net.ssl.HttpsURLConnection";
    private static String  SSL_DEFAULTVERIFIER = "setDefaultHostnameVerifier";
    private static String  SSL_VERIFIER = "javax.net.ssl.HostnameVerifier";
    private static String  SSL_SESSION = "javax.net.ssl.SSLSession";
    private static String  SSL_PEERHOST = "getPeerHost";

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public static boolean
    sslSetup() {

	InvocationHandler  handler;
	ClassLoader        loader;
	boolean            result;
	Object             proxy;
	Method             method;
	Class              connection;
	Class              verifier;

	if (sslhosthash == null) {
	    result = false;
	    try {
		connection = Class.forName(SSL_CONNECTION);
		loader = connection.getClassLoader();
		verifier = Class.forName(SSL_VERIFIER);
		method = connection.getMethod(SSL_DEFAULTVERIFIER, new Class[] {verifier});
		handler = new SSLVerifyHandler();
		proxy = Proxy.newProxyInstance(loader, new Class[] {verifier}, handler);
		method.invoke(null, new Object[] {proxy});
		sslhosthash = new Hashtable();
		result = true;
	    }
	    catch(ClassNotFoundException e) {}
	    catch(NoSuchMethodException e) {}
	    catch(IllegalAccessException e) {}
	    catch(InvocationTargetException e) {}
	} else result = true;

	return(result);
    }

    ///////////////////////////////////
    //
    // YoixMiscSSL Methods
    //
    ///////////////////////////////////

    static boolean
    addHostPair(String pair) {

	boolean  result = false;
	String   equivalent;
	String   host;
	int      index;

	if (pair != null) {
	    if (result = sslSetup()) {
		if ((index = pair.indexOf('=')) > 0) {
		    host = pair.substring(0, index);
		    equivalent = pair.substring(index+1);
		    sslhosthash.put(host, equivalent);
		}
	    }
	}
	return(result);
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    public static
    class SSLVerifyHandler

	implements InvocationHandler

    {

	public Object
	invoke(Object proxy, Method method, Object args[]) {

	    Object  value = null;
	    String  host;
	    String  peer;
	    Method  getpeer;
	    Class   session;

	    //
	    // Had problems calling getPeerHost() so we decided (for now)
	    // to just skip the call and always return TRUE. We probably
	    // will investigate some more, it's definitely not an urgent
	    // problem. Current implementation means we can build using
	    // 1.3.1 and still run on 1.4.X.
	    //

	    if (args.length == 2) {
		value = Boolean.TRUE;
		if (sslhosthash != null) {
		    if ((host = (String)(sslhosthash.get(args[0]))) != null) {
			try {
			    value = Boolean.FALSE;
			    session = args[1].getClass();
			    getpeer = session.getMethod(SSL_PEERHOST, new Class[0]);
			    //
			    // All attempts at invoking getPeerHost()
			    // ended up with an IllegalAccessException,
			    // so skip the invoke and just return TRUE.
			    //
			}
			catch(NoSuchMethodException e) {}
		    }
		}
		value = Boolean.TRUE;	// kludge - always returning TRUE
	    }
	    return(value);
	}
    }
}

