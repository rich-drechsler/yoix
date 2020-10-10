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
import java.awt.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.security.*;
import java.util.*;

class YoixSecurityManager extends SecurityManager

    implements YoixConstants

{

    //
    // This class tries to support application and applet security, but
    // in the future we may use separate subclasses to support applets
    // and applications.
    //
    // NOTE - in most cases we tried to follow Java's suggestions about
    // how individual methods should handle security failures, but only
    // when we're running as an applet. Take a look at the comments in
    // Java's SecurityManager class for more details. In several methods
    // (e.g., checkExit and checkTopLevelWindow) we ignored their advice
    // and another (i.e., checkMemberAccess) we were pretty much forced
    // to reproduce the code in super class method.
    //
    // NOTE - most (if not all) methods that just do
    //
    //        if (isApplet())
    //            super.checkXXX();
    //
    // could be eliminated because the super class methods should end up
    // calling our implementation of checkPermission() which also uses
    // isApplet() to decide what to do. Needs careful testing to be sure,
    // so we decided to leave most of them in for now.
    //
    // NOTE - code to support MACFRAMEWORKS is a recent addition and will
    // obviously only for Macs, despite the fact that in places the code
    // looks more general. In fact, just to emphasize the point there may
    // be an unnecessary ISMAC test in allowSystemRead() that will make
    // that test fail on other platforms.
    //

    YoixBodySecurityManager  parent;
    YoixObject               data;

    //
    // Set to true after we finish any one-time initialization that may
    // be needed before a SecurityManager is actually installed.
    //

    private static boolean  initialized = false;

    //
    // These can only be changed in the code controlled by initialized.
    //

    private static boolean  applet = false;
    private static int      appletflags = 0;

    //
    // We use a Hashtable to keep track of the threads that are currently
    // running security checks.
    //

    private Hashtable  checking = new Hashtable();

    //
    // We're also responsible for remembering when the command line script
    // came from and we may use that information to make decisions about
    // network connection or file read requests.
    //

    private static CodeSource  yoixcodesource = null;
    private static boolean     yoixset = false;
    private static Object      yoixsource = null;

    //
    // Right now this is only used when we're running on an Mac and it's
    // supposed to let us read files in the MACFRAMWORKS directory.
    //

    private static CodeSource  systemcodesource = null;

    //
    // Trailing characters that are used when we build yoixcodesource. The
    // meaning is set by CodeSource.implies(), so don't change them. Also
    // notice that we currently don't use yoixcodesource when the startup
    // script was loaded over the network.
    //

    private static final String  MATCH_EXACT = "";
    private static final String  MATCH_DIRECTORY = "*";
    private static final String  MATCH_SUBDIRETORIES = "-";

    //
    // Return value used when we called a function but something went wrong.
    // The values assigned to these constants are for internal use only and
    // don't have to agree with the numbers that functions actually return.
    // The mapping in callChecker() and comments in YoixSecurityOptions.java
    // should be sufficient to show you what's happening.
    //

    private static final int CHECK_REJECTED = -1;
    private static final int CHECK_FAILED = 0;
    private static final int CHECK_PASSED = 1;
    private static final int CHECK_SKIPPED = 2;

    //
    // Some things we use to help us decide if a class that we find in the
    // list returned by getClassContext() represents the execution of some
    // privileged code. All of this would be unnecessary if there was a way
    // to get the subset of getClassContext() that represent classes on the
    // stack since the last AccessController.doPrivileged() call.
    //

    private static final CodeSource  CODESOURCE = YoixMisc.getCodeSource();

    private static final Permission  ALLPERMISSION = new AllPermission();
    private static final Permission  ACCESSEVENTQUEUE = new AWTPermission("accessEventQueue");
    private static final Permission  MODIFYTHREAD = new RuntimePermission("modifyThread");
    private static final Permission  MODIFYTHREADGROUP = new RuntimePermission("modifyThreadGroup");

    private static final Class PRIVILEGEDACTION = PrivilegedAction.class;
    private static final Class PRIVILEGEDEXCEPTION = PrivilegedExceptionAction.class;

    private static final String  PRIVILEGEDPREFIXES[] = {
	"java.",
	"javax.",
	"sun.",
	"com.sun.",
    };

    //
    // We need read access in one unusual directory when we're running on
    // a Mac as an applet or application with restricted read permissions.
    //

    private static final String  MACFRAMWORKS = "/System/Library/Frameworks/JavaVM.framework/Versions";

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSecurityManager(YoixObject data, YoixBodySecurityManager parent) {

	this.data = data;
	this.parent = parent;
    }

    ///////////////////////////////////
    //
    // YoixSecurityManager Methods
    //
    ///////////////////////////////////

    public final void
    checkAccept(String host, int port) {

	YoixObject  argv[] = new YoixObject[] {YoixObject.newString(host), YoixObject.newInt(port)};
	int         status;

	if ((status = check(N_CHECKACCEPT, argv, isApplication())) != CHECK_PASSED) {
	    if (isApplet())
		super.checkAccept(host, port);
	}
    }


    public final void
    checkAwtEventQueueAccess() {

	if (isApplet())
	    super.checkAwtEventQueueAccess();
    }


    public final void
    checkConnect(String host, int port) {

	YoixObject  argv[] = new YoixObject[] {YoixObject.newString(host), YoixObject.newInt(port)};
	int         status;

	if ((status = check(N_CHECKCONNECT, argv, isApplication())) != CHECK_PASSED) {
	    if (isApplet()) {
		if (allowConnect(host, port) == false)
		    super.checkConnect(host, port);
	    }
	}
    }


    public final void
    checkConnect(String host, int port, Object context) {

	YoixObject  argv[] = new YoixObject[] {YoixObject.newString(host), YoixObject.newInt(port)};
	int         status;

	if ((status = check(N_CHECKCONNECT, argv, isApplication())) != CHECK_PASSED) {
	    if (isApplet()) {
		if (allowConnect(host, port) == false)
		    super.checkConnect(host, port, context);
	    }
	}
    }


    public final void
    checkCreateClassLoader() {

	if (isApplet())
	    super.checkCreateClassLoader();
    }


    public final void
    checkDelete(String file) {

	YoixObject  argv[] = new YoixObject[] {YoixObject.newString(file)};
	int         status;

	if ((status = check(N_CHECKDELETE, argv, isApplication())) != CHECK_PASSED) {
	    if (isApplet())
		super.checkDelete(file);
	}
    }


    public final void
    checkExec(String cmd) {

	YoixObject  argv[] = new YoixObject[] {YoixObject.newString(cmd)};
	int         status;

	if ((status = check(N_CHECKEXEC, argv, isApplication())) != CHECK_PASSED) {
	    if (isApplet())
		super.checkExec(cmd);
	}
    }


    public final void
    checkExit(int status) {

	YoixObject  argv[] = new YoixObject[] {YoixObject.newInt(status)};

	if (VM.notShutdown())
	    check(N_CHECKEXIT, argv);
    }


    public final void
    checkLink(String lib) {

	if (isApplet())
	    super.checkLink(lib);
    }


    public final void
    checkListen(int port) {

	YoixObject  argv[] = new YoixObject[] {YoixObject.newInt(port)};
	int         status;

	if ((status = check(N_CHECKLISTEN, argv, isApplication())) != CHECK_PASSED) {
	    if (isApplet())
		super.checkListen(port);
	}
    }


    public final void
    checkMemberAccess(Class clazz, int which) {

	Class  stack[];
	Class  caller;

	//
	// This is trickier than most of the other methods because we can't
	// call super.checkMemberAccess(). Instead we pretty much duplicate
	// superclass tests after adding a few of our own tests. Comments in
	// super.checkMemberAccess() describe the organization of the stack
	// as represented in the array that getClassContext() returns.
	//

	if (isApplet()) {
	    //
	    // Basically what super.checkMemberAccess() does, but we let a
	    // few special Yoix classes through.
	    //
	    if (which != Member.PUBLIC) {
		stack = getClassContext();
		caller = (stack.length >= 4) ? stack[3] : null;
		if (caller != YoixModule.class && caller != YoixVMCleaner.class) {
		    if (caller == null || caller.getClassLoader() != clazz.getClassLoader())
			checkPermission(new RuntimePermission("accessDeclaredMembers"));
		}
	    }
	}
    }


    public final void
    checkMulticast(InetAddress maddr) {

	YoixObject  argv[] = new YoixObject[] {YoixObject.newString(maddr.getHostAddress())};
	int         status;

	if ((status = check(N_CHECKMULTICAST, argv, isApplication())) != CHECK_PASSED) {
	    if (isApplet())
		super.checkMulticast(maddr);
	}
    }


    public final void
    checkPackageAccess(String pkg) {

	if (isApplet())
	    super.checkPackageAccess(pkg);
    }


    public final void
    checkPackageDefinition(String pkg) {

	if (isApplet())
	    super.checkPackageDefinition(pkg);
    }


    public final void
    checkPermission(Permission perm) {

	ProtectionDomain  domain;
	String            message;
	String            actions;
	Thread            thread;
	Class             stack[];
	int               n;

	//
	// This is where security checks often end up but we only do real
	// work if we're running in applet mode. Applications get a free
	// pass and are allowed to do anything that wasn't restricted by
	// the command line security options.
	//
	// Supplying our own version of this method should raise questions
	// because it means we're no longer using Java's AccessController
	// for security checking. It's not quite as drastic as you might
	// think because older versions of the interpreter didn't support
	// an "applet" mode that attempted to enforce your system's Java
	// security policy. In other words you're not losing any security
	// protection that was available in previous releases.
	//
	// So why did we override this method? Security managers that run
	// the usual way need AllPermission to do their work because they
	// end up in privileged blocks of code that are supposed to check
	// things that the JVM really needs. If they don't have permission
	// the JVM will have all sorts of problems. Assigning AllPermssion
	// to our security manager isn't unreasonable, but doing it for the
	// entire Yoix package seems questionable and requiring changes to
	// system or user policy files to enable "applet" mode protection
	// seemed excessive. Anyway, this approach appears to work without
	// requiring any policy file changes, so our new security checking
	// can be enabled using the "--applet" command line option. In the
	// future we may decide we have to put this class somewhere else
	// and resort to the AllPermission policy file approach, but that
	// will only happen if it looks like there are serious problems
	// with this approach.
	//
	// The checking that we do here definitely isn't as fast as Java's
	// because we have to check individual classes on the stack and we
	// also have to try to recognize, using a non-trivial guess, when
	// we happen to run into a doPrivileged() that the JVM needs. It's
	// unfortunate that Java doesn't currently supply a way to get the
	// the classes that are running in privileged block and that's why
	// we need our elaborate guess. Anyway, it seems to work, but you
	// should convince yourself by taking a careful look at the tests
	// in our inDoPrivileged() method. Remember though that applet mode
	// is completely new and so we're not sacrificing any security that
	// was available in previous releases.
	//
	// NOTE - we don't support a Yoix checkPermission() function, but
	// there's a small chance that may change in a future release. Also
	// notice that applications, as expected, get a free pass here.
	//

	if (redirectCheckPermission(perm) == false) {
	    if (isApplet()) {
		thread = Thread.currentThread();
		if (checking.containsKey(thread) == false) {
		    try {
			checking.put(thread, perm);
			stack = getClassContext();
			for (n = 0; n < stack.length && inDoPrivileged(stack[n]) == false; n++) {
			    if (stack[n] != this.getClass()) {
				if ((domain = stack[n].getProtectionDomain()) != null) {
				    if (domain.implies(perm) == false) {
					if (MODIFYTHREAD.implies(perm)) {
					    //
					    // This is how we interpreted comments
					    // in SecurityMananger.checkAccess(). The
					    // break if the test passes should be OK
					    // because the same test will pass again
					    // no matter which classes we check.
					    //
					    if (thread.getClass().getProtectionDomain().implies(MODIFYTHREAD))
						break;
					} else if (MODIFYTHREADGROUP.implies(perm)) {
					    //
					    // This is how we interpreted comments
					    // in SecurityMananger.checkAccess(). The
					    // break if the test passes should be OK
					    // because the same test will pass again
					    // no matter which classes we check.
					    //
					    if (thread.getClass().getProtectionDomain().implies(MODIFYTHREADGROUP))
						break;
					} else if (ACCESSEVENTQUEUE.implies(perm)) {
					    //
					    // Allow event queue access for classes
					    // loaded from our codebase. Eventually
					    // could use appletflags to restrict the
					    // access some?? Obviously need to check
					    // the other classes on the stack, so we
					    // can't break here.
					    //
					    if (allowEventQueueAccess(stack[n]))
						continue;
					} else {
					    //
					    // Real kludges, if any are needed, should
					    // go here.
					    //
					    if (perm instanceof AWTPermission && perm.getName().equals("setWindowAlwaysOnTop")) {
						//
						// Looks like Java 1.6 introduced a popup
						// menu problem when a security manager is
						// installed and a heavyweight window is
						// needed. They call setAlwaysOnTop(true),
						// but it needs to run as priveledged code
						// or Swing's Popup.java class should catch
						// security exceptions. We carefully try to
						// allow the call when the context looks OK.
						// 
						if (n > 2) {
						    if (stack[n] == YoixBodyComponentSwing.class) {
							if (stack[1] == java.awt.Window.class) {
							    if (stack[2].getName().equals("javax.swing.Popup$HeavyWeightWindow")) {
								break;
							    }
							}
						    }
						}
					    }
					}
					message = "Permission Denied: " + perm.getName();
					if ((actions = perm.getActions()) != null && actions.length() > 0)
					    message += " " + actions;
					throw(new SecurityException(message));
				    }
				}
			    }
			}
		    }
		    finally {
			checking.remove(thread);
		    }
		}
	    }
	}
    }


    public final void
    checkPrintJobAccess() {

	//
	// This can no longer be controlled via command line options, but
	// instead should be handled by Java policy files. Main reason is
	// that it results in lots of other permissions requests that may
	// or may not need to be handled (e.g., connect to localhost:631).
	//

	if (isApplet())
	    super.checkPrintJobAccess();
    }


    public final void
    checkPropertiesAccess() {

	YoixObject  argv[] = null;
	int         status;

	if ((status = check(N_CHECKPROPERTIESACCESS, argv, isApplication())) != CHECK_PASSED) {
	    if (isApplet())
		super.checkPropertiesAccess();
	}
    }


    public final void
    checkPropertyAccess(String key) {

	YoixObject  argv[] = new YoixObject[] {YoixObject.newString(key)};
	int         status;

	if ((status = check(N_CHECKREADPROPERTY, argv, isApplication())) != CHECK_PASSED) {
	    if (isApplet())
		super.checkPropertyAccess(key);
	}
    }


    public final void
    checkRead(FileDescriptor fd) {

	if (isApplet())
	    super.checkRead(fd);
    }


    public final void
    checkRead(String file) {

	YoixObject  argv[] = new YoixObject[] {YoixObject.newString(file)};
	int         status;

	if ((status = check(N_CHECKREAD, argv, isApplication())) != CHECK_PASSED) {
	    if (allowSystemRead(file) == false) {
		if (isApplet()) {
		    if (allowRead(file) == false) 
			super.checkRead(file);
		}
	    }
	}
    }


    public final void
    checkRead(String file, Object context) {

	YoixObject  argv[] = new YoixObject[] {YoixObject.newString(file)};
	int         status;

	if ((status = check(N_CHECKREAD, argv, isApplication())) != CHECK_PASSED) {
	    if (isApplet()) {
		if (allowRead(file) == false)
		    super.checkRead(file, context);
	    }
	}
    }


    public final void
    checkSecurityAccess(String target) {

	if (isApplet())
	    super.checkSecurityAccess(target);
    }


    public final void
    checkSetFactory() {

	if (isApplet())
	    super.checkSetFactory();
    }


    public final void
    checkSystemClipboardAccess() {

	YoixObject  argv[] = null;
	int         status;

	if ((status = check(N_CHECKSYSTEMCLIPBOARDACCESS, argv, isApplication())) != CHECK_PASSED) {
	    if (isApplet())
		super.checkSystemClipboardAccess();
	}
    }


    public final boolean
    checkTopLevelWindow(Object window) {

	YoixObject  argv[];

	return((appletflags&0x01) == 0);
    }


    public final void
    checkWrite(FileDescriptor fd) {

	if (isApplet())
	    super.checkWrite(fd);
    }


    public final void
    checkWrite(String file) {

	YoixObject  argv[] = new YoixObject[] {YoixObject.newString(file)};
	int         status;

	if ((status = check(N_CHECKWRITE, argv, isApplication())) != CHECK_PASSED) {
	    if (isApplet())
		super.checkWrite(file);
	}
    }


    final void
    checkYoixAddProvider(YoixObject provider) {

	YoixObject  argv[] = new YoixObject[] {provider};

	check(N_CHECKYOIXADDPROVIDER, argv);
    }


    final void
    checkYoixEval(YoixObject source, YoixObject ispath) {

	YoixObject  argv[] = new YoixObject[] {source, ispath};

	check(N_CHECKYOIXEVAL, argv);
    }


    final void
    checkYoixExecute(YoixObject source, YoixObject ispath, YoixObject args) {

	YoixObject  argv[] = new YoixObject[] {source, ispath, args != null ? args : YoixObject.newArray(0)};

	check(N_CHECKYOIXEXECUTE, argv);
    }


    final void
    checkYoixInclude(YoixObject source) {

	YoixObject  argv[] = new YoixObject[] {source};

	check(N_CHECKYOIXINCLUDE, argv);
    }


    final void
    checkYoixModule(YoixObject classname) {

	YoixObject  argv[] = new YoixObject[] {classname};

	check(N_CHECKYOIXMODULE, argv);
    }


    final void
    checkYoixOpen(YoixObject source, YoixObject type, YoixObject mode) {

	YoixObject  argv[] = new YoixObject[] {source, type, mode};

	check(N_CHECKYOIXOPEN, argv);
    }


    final void
    checkYoixRemoveProvider(YoixObject provider) {

	YoixObject  argv[] = new YoixObject[] {provider};

	check(N_CHECKYOIXREMOVEPROVIDER, argv);
    }


    protected final void
    finalize() {

	data = null;
	parent = null;
	checking.clear();
	checking = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    public final boolean
    getInCheck() {

	return(checking.containsKey(Thread.currentThread()));
    }


    static synchronized boolean
    setSecurityManager(YoixObject obj) {

	YoixSecurityManager  manager;
	YoixObject           element;
	boolean              result = false;
	int                  n;

	//
	// This method installs a security manager after making sure any
	// interpreter initialization that must preceed the installation
	// is finished.
	//

	if (System.getSecurityManager() == null) {
	    if (obj != null && obj.isSecurityManager()) {
		if ((manager = (YoixSecurityManager)obj.getManagedObject()) != null) {
		    if (initialized == false) {
			//
			// Currently force loading and initialization of
			// the YoixConverterOutput and YoixConverterInput
			// classes because they need access to the sun.io
			// package and checkPackageAccess() won't approve
			// without some work. Anyway, decided it was easy
			// enough to handle right here even though it's a
			// bit of a kludge.
			//

			new YoixConverterOutput(null, 100);
			new YoixConverterInput(null, 100);

			//
			// The captureScreen() builtin uses a static Robot
			// variable to read pixels from the display, so we
			// make sure the class is loaded.
			//
			// NOTE - just referencing YoixModuleImage.class was
			// sufficient when the javac target was 1.2, but for
			// 1.5 we need to do more.
			//

			YoixReflect.getDeclaredField(YoixModuleImage.class, YoixModule.MODULETABLE);

			//
			// The AppleApplicationEvent handlers trigger a
			// canAccessApplicationEvent security check
			// exception. By loading the module class here,
			// we avoid that problem.
			//
			// NOTE - we reference the class by name just so we
			// trigger a compile that will fail if the class is
			// missing and we're not on a Mac.
			//

			try {
			    YoixReflect.getDeclaredField(Class.forName("att.research.yoix.apple.Module"), YoixModule.MODULETABLE);
			}
			catch(ClassNotFoundException e) {}
			catch(UnsupportedClassVersionError e) {}	// just in case

			//
			// Probably unnecessary now, but our old versions
			// needed to make this happen before the security
			// manager was installed. Decided to leave it in
			// for the time being.
			//

			YoixAWTToolkit.getDefaultToolkit();

			//
			// Macs need to read files from a directory that
			// normally might not be readable. Not 100% sure
			// about this, but it seems to be needed when we
			// run as an applet and probably also if we're an
			// application with restricted read permission.
			//

			setSystemSource();

			applet = VM.isApplet();
			appletflags = VM.getAppletFlags();
			initialized = true;
		    }
		    VM.pushAccess(LRW_);
		    for (n = 0; n < obj.length(); n++) {
			if ((element = obj.getObject(n)) != null) {
			    if (element.isCallable())
				element.setAccess(LR_X);
			}
		    }
		    VM.popAccess();
		    System.setSecurityManager(manager);
		    result = true;
		}
	    }
	}
	return(result);
    }


    static synchronized boolean
    setYoixSource(String path) {

	boolean  result = false;
	String   dir;
	String   match;
	URL      url;
	int      index;

	//
	// We only get one chance to do this and we're not completely sure
	// what should happen if we're trying for the second time. Really
	// doubt it's all that important, so right now we return a boolean
	// that indicates whether or not we were allowed to initialize the
	// source related variables and let the caller handle it.
	//

	if (yoixset == false && initialized == false) {
	    result = true;
	    yoixset = true;
	    yoixsource = null;
	    yoixcodesource = null;
	    if (path != null) {
		try {
		    switch (YoixMisc.guessStreamType(path)) {
			case FILE:
			    if (path != NAME_STDIN && path != NAME_STDOUT && path != NAME_STDERR) {
	    			match = MATCH_SUBDIRETORIES;
				path = (new File(YoixMisc.toYoixPath(path))).getAbsolutePath();
				yoixsource = new File(path);
				if ((dir = ((File)yoixsource).getParent()) != null && dir.length() > 0) {
				    dir = YoixMisc.toYoixPath(dir);
				    if (dir.endsWith("/") == false)
					dir += "/";
				    url = new URL("file:" + path);
				    yoixcodesource = new CodeSource(
					new URL(url.getProtocol(), url.getHost(), url.getPort(), dir + match),
					(java.security.cert.Certificate[])null
				    );
				}
			    }
			    break;

			case URL:
	    		    match = MATCH_DIRECTORY;
			    path = YoixMisc.toYoixURL(path);
			    yoixsource = new URL(path);
			    url = (URL)yoixsource;
			    //
			    // We initialize yoixcodesource even though in
			    // this case it won't currently be used.
			    // 
			    if ((dir = url.getPath()) != null) {
				if ((index = dir.lastIndexOf('/')) >= 0) {
				    dir = dir.substring(0, index+1);
				    yoixcodesource = new CodeSource(
					new URL(url.getProtocol(), url.getHost(), url.getPort(), dir + match),
					(java.security.cert.Certificate[])null
				    );
				}
			    }
			    break;
		    }
		}
		catch(Exception e) {
		    yoixsource = null;
		    yoixcodesource = null;
		}
	    }
	}
	return(result);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private boolean
    allowConnect(String host, int port) {

	boolean  result = false;
	URL      url;

	//
	// If the original command line script was loaded over the network
	// then we allow DNS lookups (i.e., port is -1) and connections to
	// the original host using the original port.
	//
	// NOTE - this is somewhat analogous to the network permission that
	// Java grants to untrusted applets.
	//

	if (yoixsource instanceof URL) {
	    if (port != -1) {
		if ((appletflags&0x04) == 0) {
		    try {
			url = (URL)yoixsource;
			result = url.equals(new URL(url.getProtocol(), host, port, url.getFile()));
		    }
		    catch(Throwable t) {}
		}
	    } else result = (appletflags&0x08) == 0;
	}
	return(result);
    }


    private boolean
    allowEventQueueAccess(Class source) {

	//
	// Eventually could do more, like maybe use appletflags to help us
	// decide which classes should get access to the event queue.
	// 

	return(inCodeSource(source));
    }


    private boolean
    allowRead(String file) {

	boolean  result = false;
	String   path;

	//
	// If the original command line Yoix script was loaded from a local
	// file then we allow limited access to other local files.
	//
	// NOTE - this is somewhat analogous to the read permission granted
	// by Java to applets that are loaded from the local file system.
	//

	if (yoixsource instanceof File) {
	    if (yoixcodesource != null) {
		if ((appletflags&0x02) == 0) {
		    try {
			path = YoixMisc.toYoixPath((new File(YoixMisc.toYoixPath(file))).getAbsolutePath());
			result = yoixcodesource.implies(
			    new CodeSource(
				new URL("file:" + path),
				(java.security.cert.Certificate[])null
			    )
			);
		    }
		    catch(Throwable t) {}
		}
	    }
	}
	return(result);
    }


    private boolean
    allowSystemRead(String file) {

	boolean  result = false;
	String   path;

	//
	// The ISMAC test should be unnecessary and means we always return
	// false when we're not on a Mac. You will have to remove this test
	// before this can be extended to other platforms.
	//

	if (ISMAC && systemcodesource != null) {
	    try {
		path = (new File(YoixMisc.toYoixPath(file))).getAbsolutePath();
		result = systemcodesource.implies(
		    new CodeSource(
			new URL("file:" + path),
			(java.security.cert.Certificate[])null
		    )
		);
	    }
	    catch(Throwable t) {}
	}
	return(result);
    }
 

    private int
    callChecker(String name, YoixObject argv[]) {

	YoixObject  function;
	YoixObject  obj;
	Thread      thread;
	int         result;

	//
	// We return CHECK_PASSED if it looks like we're already doing a
	// security check for this thread. We eventually may take another
	// look at this approach, but it's sufficient enough for now. The
	// alternative is to use AccessController.doPrivileged() instead
	// of the checking Hashtable.
	// 
	// NOTE - adding this thread to the checking Hashtable before we
	// call the function means any secondary checkPermission() calls
	// that might be triggered will pass, which obviously means their
	// completely trusted functions or builtins.
	//

	if ((function = getChecker(name)) != null) {
	    thread = Thread.currentThread();
	    if (checking.containsKey(thread) == false) {
		try {
		    checking.put(thread, name);
		    if (inSystemCode() == false) {
			if (argv == null)
			    argv = new YoixObject[0];
			if ((obj = parent.call(function, argv)) != null) {
			    if (obj.isNumber()) {
				switch (obj.intValue()) {
				    case -1:
					result = CHECK_REJECTED;
					break;

				    case 0:
					result = CHECK_FAILED;
					break;

				    case 1:
					result = CHECK_PASSED;
					break;

				    case 2:
					result = CHECK_SKIPPED;
					break;

				    default:
					result = CHECK_FAILED;
					break;
				}
			    } else result = CHECK_FAILED;
			} else result = CHECK_FAILED;
		    } else result = CHECK_PASSED;
		}
		catch(Throwable t) {
		    //
		    // If anything unexpected happens make sure we
		    // notice and tell the caller too. Decided not
		    // not to record any info about the error, at
		    // least for now.
		    //
		    result = CHECK_FAILED;
		}
		finally {
		    checking.remove(thread);
		}
	    } else result = CHECK_PASSED;	// might be able to do better
	} else result = CHECK_SKIPPED;

	return(result);
    }


    private int
    check(String name, YoixObject argv[]) {

	return(check(name, argv, true));
    }


    private int
    check(String name, YoixObject argv[], boolean fatal) {

	String  args;
	String  sep;
	int     result;
	int     n;

	//
	// NOTE - if we build an error message we currently ignore any in
	// argv[] that's not a string, number, or NULL.
	//

	if ((result = callChecker(name, argv)) == CHECK_FAILED || result == CHECK_REJECTED) {
	    if (fatal || result == CHECK_REJECTED) {
		args = "";
		try {
		    if (argv != null) {
			sep = "";
			for (n = 0; n < argv.length; n++) {
			    if (argv[n].isString())
				args += sep + argv[n].stringValue();
			    else if (argv[n].isInteger())
				args += sep + argv[n].intValue();
			    else if (argv[n].isNumber())
				args += sep + argv[n].doubleValue();
			    else if (argv[n].isNull())
				args += sep + "NULL";
			    if (args.length() > 0)
				sep = ", ";
			}
		    }
		}
		finally {
		    throw(new SecurityException("Access Denied: " + name + "(" + args + ")"));
		}
	    }
	}
	return(result);
    }


    private YoixObject
    getChecker(String name) {

	YoixObject  function = null;

	if (data != null && name != null) {
	    if ((function = data.getObject(name)) != null && function.isNull())
		function = null;
	}
	return(function);
    }


    private boolean
    inCodeSource(Class arg) {

	ProtectionDomain  domain;
	boolean           result = false;

	if (CODESOURCE != null) {
	    if ((domain = arg.getProtectionDomain()) != null)
		result = CODESOURCE.implies(domain.getCodeSource());
	}
	return(result);
    }


    private boolean
    inDoPrivileged(Class arg) {

	boolean  result = false;

	//
	// Unfortunately it doesn't look there's a good way to determine
	// if we're executing privileged code, so we're stuck with this
	// guess. Without it we wouldn't be able to use Java classes that
	// use AccessController.doPrivileged() to do things that otherwise
	// might not be allowed. This would be unnecessary if we could ask
	// Java for a call stack snapshot that was pruned to only include
	// the classes the since the last doPrivileged() call.
	//

	if (PRIVILEGEDACTION.isAssignableFrom(arg) || PRIVILEGEDEXCEPTION.isAssignableFrom(arg))
	    result = isSystemClass(arg);
	return(result);
    }


    private boolean
    inSystemCode() {

	boolean  result = true;
	Class    stack[];
	int      n;

	//
	// This is only used to decide if we should call a handler that
	// usually was installed using a command line option. We return
	// true if it looks like we only found official Java classes or
	// this class by the time we bump into a doPrivileged() call or
	// check all the classes on the stack.
	//

	stack = getClassContext();
	for (n = 0; n < stack.length; n++) {
	    if (stack[n] != this.getClass()) {
		if (inDoPrivileged(stack[n]) == false) {
		    if (isSystemClass(stack[n]) == false) {
			result = false;
			break;
		    }
		} else break;
	    }
	}
	return(result);
    }


    private boolean
    isApplet() {

	return(applet);
    }


    private boolean
    isApplication() {

	return(!applet);
    }


    private boolean
    isSystemClass(Class arg) {

	ProtectionDomain  domain;
	boolean           result = false;
	String            name;
	int               n;

	//
	// We consider system classes the ones that have ALLPERMISSION
	// and look like they belong to Java or Sun because they happen
	// to begin with strings that are listed in PRIVILEGEDPREFIXES.
	// Didn't really investigate, so there probably is a better way
	// to make the identification - we will revisit this in a future
	// release.
	//
	// NOTE - classes that we think might be running privileged code
	// (i.e., classes that belong to Java and Sun) aren't signed and
	// have no certificates when you download them from the official
	// site.
	//

	if ((domain = arg.getProtectionDomain()) != null) {
	    if (domain.implies(ALLPERMISSION)) {
		if ((name = arg.getName()) != null) {
		    for (n = 0; n < PRIVILEGEDPREFIXES.length; n++) {
			if (name.startsWith(PRIVILEGEDPREFIXES[n])) {
			    result = true;
			    break;
			}
		    }
		}
	    }
	}
	return(result);
    }


    private boolean
    redirectCheckPermission(Permission perm) {

	YoixObject  argv[];
	boolean     result = false;
	String      name;
	String      actions;
	int         index;
	int         status;

	//
	// This can be used to redirect an official checkPermission() call
	// to an installed Yoix security checking function or builtin, but
	// it's only for limited checking that hasn't already been handled
	// by a SecurityManager method. Property "write" permission checks
	// are the perfect example and currently are the only case handled
	// by this method. Be careful if you make additions here.
	//

	if (perm instanceof AWTPermission) {
	    if ((name = perm.getName()) != null) {
		if (name.equals("readDisplayPixels")) {
		    argv = new YoixObject[0];
		    if ((status = check(N_CHECKREADDISPLAYPIXELS, argv, isApplication())) == CHECK_PASSED)
			result = true;
		} else if (name.equals("createRobot")) {
		    argv = new YoixObject[0];
		    if ((status = check(N_CHECKCREATEROBOT, argv, isApplication())) == CHECK_PASSED)
			result = true;
		}
	    }
	} else if (perm instanceof PropertyPermission) {
	    if ((actions = perm.getActions()) != null) {
		if (actions.indexOf("write") >= 0 && actions.indexOf("read") < 0) {
		    argv = new YoixObject[] {YoixObject.newString(perm.getName())};
		    if ((status = check(N_CHECKWRITEPROPERTY, argv, isApplication())) == CHECK_PASSED)
			result = true;
		}
	    }
	} else if (perm instanceof RuntimePermission) {
	    if ((name = perm.getName()) != null) {
		if (name.startsWith("getenv.")) {
		    if ((index = name.indexOf('.') + 1) < name.length()) {
			argv = new YoixObject[] {YoixObject.newString(name.substring(index))};
			if ((status = check(N_CHECKREADENVIRONMENT, argv, isApplication())) == CHECK_PASSED)
			    result = true;
		    }
		}
	    }
	}
	return(result);
    }


    private static synchronized void
    setSystemSource() {

	String  path;
	String  dir;
	String  match;
	File    source;
	URL     url;

	//
	// Follows what was done in setYoixSource(), but it only applies to
	// Macs right now. Doubt it will be needed on other platforms, but
	// if it is remove the unnecessary ISMAC test in allowSystemRead().
	//

	if (initialized == false) {
	    systemcodesource = null;
	    if (ISMAC) {
		try {
		    if (MACFRAMWORKS != null) {
			match = MATCH_SUBDIRETORIES;
			path = (new File(MACFRAMWORKS)).getAbsolutePath();
			source = new File(path);
			if (source.isDirectory()) {
			    dir = path.endsWith("/") ? path : (path + "/");
			    url = new URL("file:" + path);
			    systemcodesource = new CodeSource(
				new URL(url.getProtocol(), url.getHost(), url.getPort(), dir + match),
				(java.security.cert.Certificate[])null
			    );
			}
		    }
		}
		catch(IOException e) {
		    systemcodesource = null;
		}
	    }
	}
    }
}

