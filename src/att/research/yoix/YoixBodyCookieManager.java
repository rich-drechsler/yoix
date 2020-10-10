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
import java.net.*;
import java.util.*;

final
class YoixBodyCookieManager extends YoixPointerActive

{

    //
    // Right now this class is mostly a placeholder that was added quickly
    // to support command line cookie options. The CookieManager type can
    // be used, but it doesn't yet provide any significant capabilites, so 
    // you shouldn't use it. Eventually expect the default cookie manager
    // that's created and installed when the --cookie and --cookiepolicy
    // command line options are used will be available as a CookieManager
    // Yoix object in scripts. Anyway there's still a bunch to do!!
    //
    // This class relies on cookie support that was added in Java 1.6, so
    // so we use lots of reflection to make sure Java 1.5, which is still
    // supported by the interpreter, can build and run without significant
    // problems. Obviously the reflection code can be removed if Java 1.5
    // is no longer supported.
    //

    private Object cookiemanager = null;

    //
    // The interpreter's default cookie manager.
    //

    private static Object  cookiemanager_default = null;

    //
    // This was added quickly, mostly to give the interpreter some control
    // over cookie handling. Right now it's very limited and not all that
    // convenient because we're still supporting Java 1.5, which means we
    // really should be using lots of reflection!! It will be expanded in
    // the near future, but right now there's a project that needs minimal
    // support for the default cookie manager, so that's our initial goal.
    //

    static final int  YOIX_ACCEPT_NONE = 0;
    static final int  YOIX_ACCEPT_ORIGINAL_SERVER = 1;
    static final int  YOIX_ACCEPT_ALL = 2;

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
    };

    //
    // The activefields Hashtable translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(20);

    static {
	activefields.put(N_INSTALLED, new Integer(V_INSTALLED));
	activefields.put(N_POLICY, new Integer(V_POLICY));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyCookieManager(YoixObject data) {

	super(data);
	buildCookieManager();
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

	return(COOKIEMANAGER);
    }

    ///////////////////////////////////
    //
    // YoixBodyCookieManager Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
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


    static synchronized Object
    getDefaultCookieManager() {

	Object  manager;

	//
	// Initial policy is set to "none", which I think menas the manager
	// doesn't accept any cookies from servers. The policy can be changed
	// using the --cookiepolicy command line option.
	//

	if ((manager = cookiemanager_default) == null) {
	    if ((manager = cookieManagerNew()) != null) {
		cookieManagerSetCookiePolicy(manager, pickCookiePolicy("none"));
		cookieManagerSetDefault(manager);
		cookiemanager_default = manager;
	    }
	}
	return(manager);
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_INSTALLED:
		obj = getInstalled();
		break;

	    case V_POLICY:
		obj = getPolicy();
		break;

	    default:
		break;
	}
	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(cookiemanager);
    }


    static void
    setDefaultCookies(String source) {

	Object  manager;
	Object  store;
	Object  cookie;
	List    cookies;
	URI     uri;
	int     index;

	//
	// Right now cookies without a domain are silently ignored.
	// 

	if (source != null) {
	    if ((cookies = httpCookieParse(source)) != null) {
		if ((manager = getDefaultCookieManager()) != null) {
		    //
		    // This should be a separate method that adds cookies to
		    // any cookie manager - later.
		    //
		    if ((store = cookieManagerGetCookieStore(manager)) != null) {
			try {
			    uri = new URI("");		// sufficient when there's a domain
			    for (index = 0; index < cookies.size(); index++) {
				if ((cookie = cookies.get(index)) != null) {
				    if (httpCookieGetDomain(cookie) != null) {
					if (httpCookieGetPath(cookie) == null)
					    httpCookieSetPath(cookie, "/");
					cookieStoreAdd(store, uri, cookie);
				    }
				}
			    }
			}
			catch(URISyntaxException e) {}		// won't happen
		    }
		}
	    }
	}
    }


    static void
    setDefaultCookiePolicy(String name) {

	Object  manager;

	if ((manager = getDefaultCookieManager()) != null)
	    cookieManagerSetCookiePolicy(manager, pickCookiePolicy(name));
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	int  mode;

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_INSTALLED:
		    setInstalled(obj);
		    break;

		case V_POLICY:
		    setPolicy(obj);
		    break;

		default:
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
    buildCookieManager() {

	//
	// If we're using Java 1.5 (or lower) cookiemanager will be null!!
	//

	cookiemanager = cookieManagerNew();
	setField(N_POLICY);
    }


    private static Object
    cookieManagerGetCookieStore(Object manager) {

	return(YoixReflect.invoke(manager, "getCookieStore"));
    }


    private static Object
    cookieManagerGetDefault() {

	return(YoixReflect.invoke("java.net.CookieManager", "getDefault"));
    }


    private static Object
    cookieManagerNew() {

	return(YoixReflect.newInstance("java.net.CookieManager"));
    }


    private static void
    cookieManagerSetCookiePolicy(Object manager, Object policy) {

	YoixReflect.invoke(
	    manager,
	    "setCookiePolicy",
	    new Object[] {policy},
	    new Class[] {YoixReflect.getClassForName("java.net.CookiePolicy")}
	);
    }


    private static void
    cookieManagerSetDefault(Object manager) {

	YoixReflect.invoke(
	    "java.net.CookieManager",
	    "setDefault",
	    new Object[] {manager},
	    new Class[] {YoixReflect.getClassForName("java.net.CookieHandler")}
	);
    }


    private static Object
    cookiePolicyGetPolicy(String name) {

	return(YoixReflect.getDeclaredField(
	    YoixReflect.getClassForName("java.net.CookiePolicy"),
	    name
	));
    }


    private static void
    cookieStoreAdd(Object store, URI uri, Object cookie) {

	YoixReflect.invoke(store, "add", new Object[] {uri, cookie});
    }


    private YoixObject
    getInstalled() {

	return(YoixObject.newInt(cookiemanager != null && cookieManagerGetDefault() == cookiemanager));
    }


    private YoixObject
    getPolicy() {

	YoixObject  obj;

	switch (data.getInt(N_POLICY)) {
	    case YOIX_ACCEPT_ORIGINAL_SERVER:
		obj = YoixObject.newInt(YOIX_ACCEPT_ORIGINAL_SERVER);
		break;

	    case YOIX_ACCEPT_ALL:
		obj = YoixObject.newInt(YOIX_ACCEPT_ALL);
		break;

	    default:
		obj = YoixObject.newInt(YOIX_ACCEPT_NONE);
		break;
	}
	return(obj);
    }


    private static List
    httpCookieParse(String source) {

	return((List)YoixReflect.invoke("java.net.HttpCookie", "parse", source));
    }


    private static String
    httpCookieGetDomain(Object cookie) {

	return((String)YoixReflect.invoke(cookie, "getDomain"));
    }


    private static String
    httpCookieGetPath(Object cookie) {

	return((String)YoixReflect.invoke(cookie, "getPath"));
    }


    private static void
    httpCookieSetPath(Object cookie, String path) {

	YoixReflect.invoke(cookie, "setPath", path);
    }


    private static Object
    pickCookiePolicy(int id) {

	Object  policy;

	//
	// This eventually will use reflection.
	//

	switch (id) {
	    case YOIX_ACCEPT_ORIGINAL_SERVER:
		policy = cookiePolicyGetPolicy("ACCEPT_ORIGINAL_SERVER");
		break;

	    case YOIX_ACCEPT_ALL:
		policy = cookiePolicyGetPolicy("ACCEPT_ALL");
		break;

	    default:
		policy = cookiePolicyGetPolicy("ACCEPT_NONE");
		break;
	}
	return(policy);
    }


    private static Object
    pickCookiePolicy(String name) {

	Object  policy;

	if (name != null) {
	    name = name.trim();
	    if (name.equalsIgnoreCase("server") || name.equalsIgnoreCase("original server") || name.equalsIgnoreCase("original_server"))
		policy = cookiePolicyGetPolicy("ACCEPT_ORIGINAL_SERVER");
	    else if (name.equalsIgnoreCase("all") || name.equalsIgnoreCase("any"))
		policy = cookiePolicyGetPolicy("ACCEPT_ALL");
	    else policy = cookiePolicyGetPolicy("ACCEPT_NONE");
	} else policy = cookiePolicyGetPolicy("ACCEPT_NONE");

	return(policy);
    }


    private void
    setInstalled(YoixObject obj) {

	if (cookiemanager != null) {
	    if (obj.booleanValue() == false) {
		if (cookieManagerGetDefault() == cookiemanager)
		    cookieManagerSetDefault(cookiemanager_default);
	    } else cookieManagerSetDefault(cookiemanager);
	}
    }


    private void
    setPolicy(YoixObject obj) {

	Object  policy;

	if (cookiemanager != null) {
	    if ((policy = pickCookiePolicy(obj.intValue())) != null)
		cookieManagerSetCookiePolicy(cookiemanager, policy);
	}
    }
}

