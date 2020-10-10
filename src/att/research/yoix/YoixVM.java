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
import java.awt.geom.AffineTransform;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public final
class YoixVM extends YoixVMClipboard

    implements YoixAPI,
	       YoixConstants

{

    //
    // Our virtual machine - there can only be one instance, and it's
    // created and defined in YoixConstants.java. There's plenty of
    // room for confusion and problems if you try to get too fancy in
    // initializations or the constructor, so be very careful if you
    // start making changes here.
    //
    // NOTE - not certain all these variables need to be static, but
    // it's not really an issue because we do only want one instance
    // of this class. Test carefully if you decide to change any of
    // them!! In fact, everything probably could be static and all
    // we would have to do would be change the name of this file to
    // VM.java or change all uses of VM to YoixVM. We like the short
    // name in our Java code but prefer the longer class name, so
    // we'll leave thing be for now.
    //

    private static boolean  applet = false;
    private static boolean  buildscreen = true;
    private static boolean  buildscreens = true;
    private static boolean  checkedsuffix = false;
    private static boolean  usermodules = true;
    private static Integer  exitstatus = null;
    private static double   fontscale = 1.0;
    private static String   lookandfeel = null;
    private static int      vmstate = -1;
    private static int      appletflags = 0;
    private static int      zipped = EXECUTE_ZIPPED_DEFAULT;

    //
    // These are set when we boot.
    //

    private static HashMap  loaded;

    private static YoixObject  errordict = null;
    private static YoixObject  reserved = null;
    private static YoixObject  defaultscreen = null;
    private static YoixObject  allscreens = null;
    private static YoixObject  typedict = null;
    private static YoixObject  vm = null;
    private static YoixObject  yoixdict = null;
    private static YoixObject  mainvm = null;

    private static ArrayList   modulenames = null;
    private static ArrayList   autoload = null;
    private static HashMap     typenames = null;

    //
    // These are built from the startup tables during initialization.
    //

    private static YoixObject  defaultmatrix = null;
    private static YoixObject  securitymanager = null;
    private static YoixObject  iso88591encoding = null;
    private static YoixObject  jvmencoding = null;

    private static YoixObject  prebootparserencoding = null;
    private static YoixObject  prebootstreamencoding = null;
    private static boolean     prebootacceptcertificates = false;

    //
    // This is only needed while we're handling command line options.
    //

    private static String  securityoptions = null;

    //
    // This is set in saveException(), which should only be called when
    // the DEBUG_CAUGHTEXCEPTION flag is set.
    //

    private static Vector  savedexceptions = null;
    private static int     savedexceptionlimit = 10;

    static {
	//
	// The try/catch should only be needed if we're being run by a
	// version of Java that's too old (Charset was added to Java 1.4
	// and it's now used by YoixConverter). Catching the exception
	// gives YoixMain a chance to run validate(), which should notice
	// the old Java version and gracefully quit. Something else will
	// break if validate() somehow doesn't quit, so the interpreter
	// won't get far and there's no point in trying to be graceful
	// here.
	//
	try {
	    jvmencoding = YoixObject.newString(YoixConverter.getJVMEncoding());
	    jvmencoding.setAccessBody(LR__);

	    iso88591encoding = YoixObject.newString(YoixConverter.getISO88591Encoding());
	    iso88591encoding.setAccessBody(LR__);
	}
	catch(NoClassDefFoundError e) {}
    }

    //
    // Constants that describe our state. Actual state values are only
    // used in this file and some of our tests assume they're ordered
    // this way, so don't change the numbers without also changing any
    // vmstate tests that assume a particular order.
    //

    private static final int  INITIALIZING = 0;
    private static final int  BOOTING = 1;
    private static final int  BOOTED = 2;
    private static final int  RUNNING = 3;
    private static final int  EXITING = 4;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixVM() {

	//
	// There's only one instance of this class and it's created by
	// the VM definition in YoixConstants.java. Notice that we also
	// need YoixConstants, so the real initialization, which happens
	// when boot() is called, is intentionally postponed.
	//

	if (vmstate == -1)
	    vmstate = INITIALIZING;
	else die(INTERNALERROR);
    }

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public final void
    caughtException(Exception e) {

	caughtException(e, false, false);
    }


    public final void
    caughtException(Exception e, boolean record) {

	caughtException(e, record, false);
    }


    public final void
    caughtException(Exception e, boolean record, boolean abort) {

	//
	// We eventually may funnel all caught exceptions though this method,
	// which would then let us add more debugging support - later.
	//

	if (record)
	    recordException(e);
	else saveException(e, false);

	if (vmstate > BOOTING && vmstate < EXITING) {
	    if (e instanceof InterruptedIOException) {
		//
		// Previous version always called interrupt here, but this
		// seems more correct. If possible interrupt() will try to
		// stop the thread's run function, which uaully will be more
		// than we really want. The bad behavior surfaced when we
		// set timeouts on a socket, and even though we addressed
		// that mistake elsewhere calling interrput() here really
		// was overkill.
		//
		// NOTE - this branch probably could handle all excetions,
		// or maybe everything except InterruptedException - later.
		// 
		if (Thread.currentThread().isInterrupted())
		    interrupt();
	    } else if (e instanceof InterruptedException)
		interrupt();
	}

	if (abort)
	    abort(e);
    }


    public final synchronized void
    clearErrordict() {

	YoixObject  dict;
	YoixObject  details;

	if ((dict = YoixBodyBlock.getErrordict()) != null) {
	    details = YoixObject.newDictionary(7);
	    details.putString(N_EXCEPTION, null);
	    details.putString(N_JAVATRACE, null);
	    details.putString(N_MESSAGE, null);
	    details.putString(N_NAME, null);
	    details.putString(N_STACKTRACE, null);
	    details.putDouble(N_TIMESTAMP, 0.0);
	    details.putString(N_TYPE, null);
	    YoixMisc.copyInto(details, dict);
	}
    }


    public final void
    exit(int status) {

	//
	// There's now a single thread that's responsible for handling the
	// shutdown of the interpreter and the Java Virtual Machine. That
	// thread is also responsible for calling Yoix functions that were
	// added to a queue of "shutdown hooks" using the addShutdownHook()
	// builtin. The new mechanism means we must not call System.exit()
	// from our "shutdown thread" because the JVM makes System.exit()
	// wait if it's running "shutdown hooks", which means we'll hang!!
	// The new mechanism also means this probably should be the only
	// place that System.exit() is called. In practice the restriction
	// could be eased slightly, but only when we can be sure that the
	// call could not possibly be made while we're executing one of
	// the shutdown hooks.
	//

	try {
	    status = setShutdown(status);
	    if (Thread.currentThread() instanceof YoixVMShutdownThread) {	// prevents deadlock
		YoixBodyProcess.atExit();
		YoixBodyStream.atExit();
		YoixBodySocket.atExit();
	    } else System.exit(status);
	}
	finally {
	    RUNTIME.halt(status);
	}
    }


    public final boolean
    getAcceptCertificates() {

	return(isBooted() ? getBoolean(N_ACCEPTCERTIFICATES) : prebootacceptcertificates);
    }


    public final YoixObject
    getDefaultEncoding() {

	YoixObject  obj;

	if (isBooted())
	    obj = vmGet(N_ENCODING).getObject(N_STREAM);
	else obj = prebootstreamencoding != null ? prebootstreamencoding : iso88591encoding;

	return(obj.notNull() ? obj : iso88591encoding);
    }


    public final YoixObject
    getDefaultMatrix() {

	//
	// This triggers buildScreen(), which we usually don't want to
	// do unless absolutely necessary.
	//

	if (defaultmatrix == null)
	    defaultmatrix = vmGet(N_DEFAULTMATRIX);	// triggers buildScreen()
	return(defaultmatrix);
    }


    public final AffineTransform
    getDefaultTransform() {

	AffineTransform  transform = null;
	YoixObject       matrix;

	if ((matrix = getDefaultMatrix()) != null)
	    transform = ((YoixBodyMatrix)matrix.body()).getCurrentAffineTransform();
	return(transform);
    }


    public final YoixObject
    getDoubleBuffered(YoixObject obj) {

	return(obj == null || obj.isNull() ? getObject(N_DOUBLEBUFFERED) : obj);
    }


    public final double
    getFontMagnification() {

	if (buildscreen)
	    buildScreen();
	return(fontscale*vmGet(N_FONTMAGNIFICATION).doubleValue());
    }


    public final double
    getFontScale() {

	if (buildscreen)
	    buildScreen();
	return(fontscale);
    }


    public final double
    getInitialFontMagnification() {

	if (buildscreen)
	    buildScreen();
	return(fontscale*vm.getDouble(N_FONTMAGNIFICATION, 1.0));
    }


    public final YoixObject
    getParserEncoding() {

	YoixObject  obj;

	if (isBooted())
	    obj = vmGet(N_ENCODING).getObject(N_PARSER);
	else obj = prebootparserencoding != null ? prebootparserencoding : jvmencoding;

	return(obj.notNull() ? obj : jvmencoding);
    }


    public final String
    getTypename(YoixObject obj) {

	String  name;

	if ((name = obj.getTypename()) == null) {
	    if ((name = (String)typenames.get(obj.minor() + "")) == null)
		die(INTERNALERROR);
	}
	return(name);
    }


    public final YoixObject
    getTypeTemplate(String name) {

	YoixObject  obj;

	//
	// Duplicates the official type definition, so the return value
	// can be safely modified by the caller.
	//

	if ((obj = getTypeDefinition(name)) != null) {
	    if (obj.isPointer()) {
		obj = obj.duplicate();
		obj.setMode(RW_);
	    }
	}
	return(obj);
    }


    public final YoixObject
    getTypeTemplate(String name, YoixObject ival) {

	return(YoixMake.initialize(getTypeTemplate(name), ival));
    }


    public final YoixObject
    getVMEncoding() {

	YoixObject  obj;

	if (isBooted())
	    obj = vmGet(N_ENCODING).getObject(N_VM);
	else obj = prebootparserencoding != null ? prebootparserencoding : jvmencoding;

	return(obj.notNull() ? obj : jvmencoding);
    }


    public final void
    print(String name, String str) {

	Object  stream;

	if ((stream = vmStream(name)) != null) {
	    if (stream instanceof YoixBodyStream) {
		((YoixBodyStream)stream).write(str);
		((YoixBodyStream)stream).flush();
	    } else if (stream instanceof PrintStream) {
		((PrintStream)stream).print(str);
		((PrintStream)stream).flush();
	    } else {
		System.err.print(str);
		System.err.flush();
		die(INTERNALERROR, name);
	    }
	} else {
	    System.err.print(str);
	    System.err.flush();
	    die(INTERNALERROR, name);
	}
    }


    public final void
    println(String name, String str) {

	Object  stream;

	if ((stream = vmStream(name)) != null) {
	    if (stream instanceof YoixBodyStream) {
		((YoixBodyStream)stream).writeLine(str);
		((YoixBodyStream)stream).flush();
	    } else if (stream instanceof PrintStream) {
		((PrintStream)stream).println(str);
		((PrintStream)stream).flush();
	    } else {
		System.err.println(str);
		die(INTERNALERROR, name);
	    }
	} else {
	    System.err.println(str);
	    die(INTERNALERROR, name);
	}
    }


    public final synchronized void
    recordException(Throwable t) {

	YoixObject  dict;
	YoixObject  details;

	saveException(t, false);
	if ((dict = YoixBodyBlock.getErrordict()) != null) {
	    details = YoixError.recordDetails(JAVAERROR, EXCEPTION, null, t);
	    YoixMisc.copyInto(details, dict);
	}
    }

    ///////////////////////////////////
    //
    // YoixVM Methods
    //
    ///////////////////////////////////

    final void
    addSecurityOption(Object arg) {

	String  argument;
	String  error;

	//
	// Calling abort() if there's an error should kill us since we're
	// not booted yet. Seems like the behavior we want if something's
	// wrong with a security option. Could continue by using warn(),
	// but calling die() probably isn't appropriate because the user
	// would get a stack trace.
	//

	if (notBooted()) {
	    if (isApplet() == false) {
		if (arg instanceof String) {
		    argument = (String)arg;
		    if ((error = YoixSecurityOptions.addOption(argument)) == null) {
			if (securityoptions != null)
			    securityoptions += " " + argument;
			else securityoptions = argument;
			YoixModule.tune(N_SECURITYOPTIONS, securityoptions);
		    } else abort(error, new String[] {OFFENDINGARGUMENT, argument});
		}
	    } else VM.abort(INVALIDACCESS, new String[] {N_APPLET, "can't modify security settings"});
	}
    }


    final synchronized YoixObject
    autoImport(String name) {

	YoixObject  importdict;
	YoixObject  value = null;
	YoixObject  lval;
	YoixObject  obj;
	int         length;
	int         n;

	if ((importdict = YoixBodyBlock.getImportdict()) != null) {
	    length = importdict.length();
	    for (n = 0; n < length; n++) {
		if ((lval = importdict.getObject(n)) != null) {
		    if ((obj = lval.get()) != null) {
			if ((value = obj.getObject(name)) != null) {
			    if (bitCheck(N_DEBUG, DEBUG_AUTOIMPORT))
				println(N_STDOUT, "importing " + name + " from " + lval.name());
			    break;
			}
		    }
		}
	    }
	}
	return(value);
    }


    final void
    autoLoad(String classname) {

	if (vmstate == INITIALIZING) {
	    if (classname != null) {
		if (autoload == null)
		    autoload = new ArrayList();
		autoload.add(classname);
	    }
	}
    }


    final boolean
    bitCheck(String name, int mask) {

	return((getInt(name)&mask) != 0);
    }


    final void
    boot() {

	if (vmstate == INITIALIZING) {
	    vmstate = BOOTING;
	    buildVM();
	    vmstate = BOOTED;
	    prepareVM();
	    vmstate = RUNNING;
	}
    }


    final boolean
    canAccess(short perm) {

	return((YoixVMThread.getThreadAccess()&perm) == perm);
    }


    final boolean
    canExit() {

	boolean  result = false;

	//
	// At one point this method was synchronized and for a short time
	// after adding threadsafe support it synchronized on its own lock.
	// We recently removed all synchronization, but windowCount() still
	// should let queued window visibility requests be handled if it's
	// not called from the event thread. Anyway, this was a last minute
	// change that we believe is OK.
	// 

	if (YoixBodyThread.threadCount(false) == 0) {
	    if (YoixBodyComponent.windowCount() == 0)
		result = YoixMain.isFinished();
	}
	return(result);
    }


    final void
    checkSecuritySuffix(String path) {

	boolean  secure = false;
	int      n;

	//
	// Called to check whether the script specified on the command line
	// should be run as an applet. Right now the answer is yes if there
	// are any control characters in path, the YOIXSECURESUFFIX pattern
	// (currently '.yxs') is found anywhere in path, or an exception is
	// thrown while we're running any of these checks. In other words if
	// path ends in '.yxs' it's run as an applet, as are other paths that
	// look a little suspicious.
	//

	try {
	    for (n = 0; n < path.length(); n++) {
		if (Character.isISOControl(path.charAt(n))) {
		    secure = true;
		    break;
		}
	    }
	    if (secure == false) {
		if (path.indexOf(YOIXSECURESUFFIX) >= 0)
		    secure = true;
	    }
	}
	catch(Exception e) {
	    secure = true;
	}
	finally {
	    if (secure) {
		setApplet(null);
		if (isApplet() == false)
		    die(INTERNALERROR);
	    }
	    checkedsuffix = true;
	}
    }


    final void
    cleanup(String classname) {

	new YoixVMCleaner(classname);
    }


    final int
    getAccess() {

	return(YoixVMThread.getThreadAccess());
    }


    final int
    getAppletFlags() {

	return(appletflags);
    }


    final boolean
    getBoolean(String name) {

	return(getInt(name) != 0);
    }


    final double
    getDouble(String name) {

	return(vmGet(name).doubleValue());
    }


    final int
    getExitModel() {

	int  exitmodel = getInt(N_EXITMODEL);

	if (mainvm != null) {
	    if (mainvm.defined(N_EXITMODEL))	// should be unnecessary
		exitmodel = mainvm.getInt(N_EXITMODEL, exitmodel);
	}
	return(exitmodel);
    }


    final int
    getInt(String name) {

	return(vmGet(name).intValue());
    }


    final YoixObject
    getLvalue(String name) {

	return(vmLvalue(name));
    }


    final YoixObject[]
    getModuleLvalues() {

	YoixObject  lvalues[];
	Object      name;
	int         length;
	int         n;

	if (yoixdict != null && modulenames != null) {
	    length = modulenames.size();
	    lvalues = new YoixObject[length];
	    for (n = 0; n < length; n++) {
		name = modulenames.get(n);
		if (name instanceof String)
		    lvalues[n] = YoixObject.newLvalue(yoixdict, (String)name, true);
		else lvalues[n] = null;
	    }
	} else lvalues = null;

	return(lvalues);
    }


    final YoixObject
    getObject(String name) {

	return((YoixObject)vmGet(name).clone());
    }


    final synchronized int
    getSavedExceptionCount() {

	return(savedexceptions != null ? savedexceptions.size() : 0);
    }


    final synchronized Vector
    getSavedExceptions() {

	Vector  exceptions = savedexceptions;

	savedexceptions = null;
	return(exceptions);
    }


    final YoixBodyStream
    getStream(String name) {

	return(vmGet(name).streamValue());
    }


    final String
    getString(String name) {

	return(vmGet(name).stringValue());
    }


    final YoixStack
    getThreadStack() {

	return(YoixVMThread.getThreadStack());
    }


    final YoixObject
    getTypeDefinition(String name) {

	YoixObject  dict;
	YoixObject  obj;

	//
	// The return value should never be modified!! If you do all
	// name objects created after the change will be affected!!!
	// Use getTypeTemplate() whenever you're not sure.
	//

	if ((dict = YoixBodyBlock.getTypedict()) == null) {
	    if ((dict = typedict) == null)	// only if we're still booting
		dict = (YoixObject)loaded.get(N_TYPEDICT);
	}

	obj = ((YoixBodyDictionaryObject)dict.body()).peekAt(name);
	if (obj != null && obj.getTypename() == null && obj.isInteger() == false)
	    obj.setTypename(dict.name(dict.hash(name)));
	return(obj);
    }


    final boolean
    getUserModules() {

	return(usermodules);
    }


    final int
    getZipped() {

	return(zipped);
    }


    final void
    interrupt() {

	if (YoixVMThread.isInterruptable())
	    getThreadStack().jumpToInterrupt();
    }


    final boolean
    isApplet() {

	return(applet);
    }


    final boolean
    isApplication() {

	return(!isApplet());
    }


    final boolean
    isBooted() {

	return(vmstate >= BOOTED);
    }


    final boolean
    isBooting() {

	return(vmstate == BOOTING);
    }


    final boolean
    isRunning() {

	return(vmstate == RUNNING);
    }


    final boolean
    isShutdown() {

	return(vmstate >= EXITING);
    }


    final boolean
    isRestricted() {

	return(YoixBodyBlock.isRestricted());
    }


    final boolean
    isTypename(String name) {

	YoixObject  dict;

	return((dict = YoixBodyBlock.getTypedict()) != null && dict.defined(name));
    }


    final boolean
    isYoixTypename(String name) {

	//
	// The isBooting() test is a recent addition (12/7/05) that lets our
	// low level module loading code build more null objects.
	//

	return(typedict != null && typedict.defined(name) || isBooting());
    }


    final void
    jumpToError(YoixObject details) {

	getThreadStack().jumpToError(details);
    }


    final YoixObject
    load(String name) {

	YoixObject  obj = null;

	if (name.equals(N_SCREEN)) {
	    if (buildscreen)
		buildScreen();
	    obj = defaultscreen.duplicate();
	} else if (name.equals(N_SCREENS)) {
	    if (buildscreens)
		buildScreens();
	    obj = allscreens.duplicate();
	}

	return(obj);
    }


    final synchronized void
    loadTypeDefinition(YoixObject value, String name, String typename) {

	YoixObject  dict;

	//
	// Only called by the module loader, so access to typedict will
	// not be a problem.
	//

	if ((dict = typedict) != null) {
	    if (dict.defined(name) == false) {
		try {
		    YoixVMThread.setLoadingType(true);
		    value.setTypename(typename != null ? typename : name);
		    value = buildTypeTemplate(value, typename);
		    dict.declare(name, value, (value.canExecute() ? L__X : L___));
		    if ((dict = YoixBodyBlock.getTypedict()) != null) {
			if (dict.defined(name) == false)
			    dict.declare(name, value, (value.canExecute() ? L__X : L___));
			else abort(INVALIDACCESS, name);
		    }
		}
		finally {
		    YoixVMThread.setLoadingType(false);
		}
	    } else abort(INVALIDACCESS, name);
	}
    }


    final YoixObject
    newErrordict() {

	return(errordict.duplicate());
    }


    final YoixObject
    newGlobalDict(YoixObject argv) {

	YoixObject  dict;

	//
	// Builds a new dictionary suitable for use as the global context,
	// but only when argv isn't null.
	//

	if (argv != null) {
	    dict = YoixObject.newDictionary(7, -1);
	    dict.declare(N_ARGC, YoixObject.newInt(argv.sizeof()), RW_);
	    dict.declare(N_ARGV, argv, RW_);
	    dict.declare(N_ENVP, YoixObject.newArray(), RW_);
	    dict.declare(N_TYPEDICT, newTypedict(), LR__);
	    dict.declare(N_ERRORDICT, newErrordict(), LR__);
	    dict.declare(N_VM, newVM(), LR__);
	    dict.declare(N_IMPORTDICT, newImports(), LR__);
	} else dict = null;

	return(dict);
    }


    final YoixObject
    newImports() {

	YoixObject  obj;

	obj = YoixObject.newArray(0, -1);
	obj.setAccessBody(LR__);
	return(obj);
    }


    final YoixObject
    newTypedict() {

	return(typedict.duplicate());
    }


    final YoixObject
    newTypedict(YoixObject obj) {

	YoixObject  dict;
	YoixObject  element;
	String      names[];
	String      name;
	int         length;
	int         n;

	//
	// Builds a typedict for a restricted block, which currently ends
	// up being a fixed size dictionary. The dictionary automatically
	// includes types listed in YoixModuleVM.RESTRICTED_TYPES[], plus
	// any listed in obj, which should be an array of typename strings.
	// 

	if (typedict != null) {
	    pushAccess(LR__);
	    names = YoixModuleVM.RESTRICTED_TYPES;
	    length = names.length;
	    dict = YoixObject.newDictionary(length, -1);
	    for (n = 0; n < length; n++) {
		if ((name = names[n]) != null) {
		    if (typedict.defined(name)) {
			if ((element = typedict.getObject(name)) != null)
			    dict.declare(name, element, L___);
		    }
		}
	    }

	    if (obj != null) {
		if (obj.isArray() && obj.notNull()) {
		    length = obj.length();
		    for (n = obj.offset(); n < length; n++) {
			if ((element = obj.getObject(n)) != null) {
			    if (element.isString()) {
				name = element.stringValue();
				if (dict.defined(name) == false) {
				    if (typedict.defined(name)) {
					if ((element = typedict.getObject(name)) != null)
					    dict.declare(name, element, L___);
				    }
				}
			    }
			}
		    }
		}
	    }
	    popAccess();
	} else dict = YoixObject.newDictionary(0);

	if (dict != null) {
	    dict.setAccessBody(RORO >>> 8);
	    dict.setMode(RORO);
	    dict.setGrowable(false);		// overkill??
	}
	return(dict);
    }


    final YoixObject
    newVM() {

	YoixObject  obj;

	//
	// Assumes first call is from the main thread, which is currently
	// reasonable, but removing that assumption would not be hard.
	//

	if ((obj = YoixBodyBlock.getVM()) == null) {
	    obj = vm.duplicate();
	    if (mainvm == null)		// first call and we're main thread
		mainvm = obj;
	} else obj = obj.duplicate();

	return(obj);
    }


    final YoixObject
    newVM(boolean restricted) {

	YoixObject  obj;
	YoixObject  screen;

	if ((obj = newVM()) != null) {
	    if (restricted) {
		if ((screen = obj.getObject(N_SCREEN)) != null) {
		    pushAccess(LRWX);
		    screen.putObject(N_UIMANAGER, null);
		    popAccess();
		}
	    }
	}
	return(obj);
    }
 

    final boolean
    notBooted() {

	return(!isBooted());
    }


    final boolean
    notRestricted() {

	return(!isRestricted());
    }


    final boolean
    notShutdown() {

	return(!isShutdown());
    }


    final void
    popAccess() {

	getThreadStack().popAccess();
    }


    final void
    popError() {

	getThreadStack().popError();
    }


    final void
    popMark() {

	getThreadStack().popMark();
    }


    final void
    print(String name, YoixObject obj) {

	print(name, obj.dump());
    }


    final void
    pushAccess(int perm) {

	//
	// No idea why, but moving this into YoixStack slowed one of our
	// our timing tests down, even though it was hardly used?? We'll
	// leave it here for the time being, but we eventually should do
	// a better investigation.
	//

	getThreadStack().pushYoixObject(YoixObject.newAccess(YoixVMThread.getThreadAccess()));
	YoixVMThread.setThreadAccess(perm);
    }


    final YoixError
    pushError() {

	return(getThreadStack().pushError());
    }


    final YoixError
    pushError(String tag, String arg) {

	return(getThreadStack().pushError(tag, arg));
    }


    final YoixError
    pushInterrupt() {

	return(getThreadStack().pushInterrupt());
    }


    final void
    pushMark() {

	getThreadStack().pushMark();
    }


    final void
    pushRestore(String name, YoixObject value) {

	getThreadStack().pushRestore(getLvalue(name), value);
    }


    final void
    pushTag(int line, int column, String source) {

	getThreadStack().pushTag(line, column, source);
    }


    final void
    putTypeDefinition(YoixObject value, String name) {

	YoixObject  dict;

	if ((dict = YoixBodyBlock.getTypedict()) != null) {
	    if (dict.defined(name) == false) {
		pushAccess(LRWX);
		value.setTypename(name);
		dict.declare(name, value, (value.canExecute() ? L__X : L___));
		popAccess();
	    } else abort(INVALIDACCESS, name);
	} else die(INTERNALERROR, name);
    }


    final synchronized void
    saveException(String message) {

	if (message != null)
	    saveException(new Throwable("Saved Message:\n" + message), true);
    }


    final synchronized void
    saveException(Throwable t, boolean silent) {

	int  limit;
	int  trace;

	//
	// Eventually suspect we'll want to verify that Throwable t really
	// is an exception that we haven't already saved and/or dumped. We
	// probably can do it by looking at last Throwable that we saved.
	// Would be useful if we decide it's reasonable to try to capture
	// more information and as a result we make extra calls that could
	// already have been handled.
	//

	if (isBooted() && bitCheck(N_DEBUG, DEBUG_CAUGHTEXCEPTION)) {
	    if (silent == false) {
		if ((trace = getInt(N_TRACE)) != 0)
		    print(N_STDERR, YoixVMThread.dump(null, trace));
		if (bitCheck(N_DEBUG, DEBUG_JAVATRACE))
		    print(N_STDERR, YoixMisc.javaTrace(t));
	    }

	    if ((limit = savedexceptionlimit) > 0) {
		 if (savedexceptions == null)
		    savedexceptions = new Vector();
		savedexceptions.add(
		    new Object[] {
			new Long(System.currentTimeMillis()),
			Thread.currentThread(),
			t != null ? t : new Throwable(),
			YoixVMThread.dump(null, MODEL_YOIXCALLSTACKTRACE)
		    }
		);
		while (savedexceptions.size() > limit)
		    savedexceptions.removeElementAt(0);
	    }
	}
    }


    final void
    setAccess(int perm) {

	YoixVMThread.setThreadAccess(perm);
    }


    final void
    setApplet(String arg) {

	//
	// No way to disable applet mode or change the flags once set, and
	// we it currently can only happen at startup (i.e., via a command
	// line option).
	//

	if (vmstate == INITIALIZING) {
	    if (applet == false) {
		YoixModule.tune(N_APPLET, Boolean.TRUE);
		applet = true;
		appletflags = YoixMake.javaInt(arg, 0);
	    }
	}
    }


    final void
    setDebugging(String arg) {

	boolean  debugging = true;
	boolean  addtags = true;
	int      trace = MODEL_NONE;

	//
	// Currently only allowed at startup via command line options. The
	// way -g:none is handled right now means it's exactly the same as
	// the -O option, but there may be a difference in future releases.
	//

	if (vmstate == INITIALIZING) {
	    if (arg != null && arg.length() > 0) {
		if (arg.charAt(0) == ':' || arg.charAt(0) == '=')
		    arg = arg.substring(1);
		if (arg.equals("none")) {
		    debugging = false;
		    addtags = false;
		    trace = MODEL_NONE;
		} else if (arg.equals("trace"))
		    trace = MODEL_YOIXCALLSTACKTRACE;
	    }
	    YoixModule.tune(N_DEBUGGING, new Boolean(debugging));
	    YoixModule.tune(N_ADDTAGS, new Boolean(addtags));
	    YoixModule.tune(N_TRACE, new Integer(trace));
	}
    }


    final void
    setLookAndFeel(String arg) {

	if (vmstate == INITIALIZING) {
	    if (arg != null && arg.length() > 0)
		lookandfeel = arg;
	    else lookandfeel = null;
	}
    }


    final void
    setOptimized(String arg) {

	//
	// Currently only allowed at startup via command line options and
	// right it's equivalent to -g:none, but we eventually expect to
	// process the argument and perhaps adjust settings that aren't
	// touched by the -g option.
	//

	if (vmstate == INITIALIZING) {
	    YoixModule.tune(N_DEBUGGING, Boolean.FALSE);
	    YoixModule.tune(N_ADDTAGS, Boolean.FALSE);
	    YoixModule.tune(N_TRACE, new Integer(0));
	}
    }


    final synchronized void
    setSavedExceptionLimit(int limit) {

	if ((savedexceptionlimit = limit) > 0) {
	    if (savedexceptions != null) {
		while (savedexceptions.size() > savedexceptionlimit)
		    savedexceptions.removeElementAt(0);
	    }
	} else savedexceptions = null;
    }


    final synchronized int
    setShutdown(int status) {

	vmstate = EXITING;
	if (exitstatus == null)
	    exitstatus = new Integer(status);
	return(exitstatus.intValue());
    }


    final void
    setPreBootAcceptCertificates(String name, Object value) {

	if (notBooted()) {
	    if (value instanceof Boolean) {
		prebootacceptcertificates = ((Boolean)value).booleanValue();
		YoixModule.tune(name, value);
	    }
	}
    }


    final void
    setPreBootDefaultEncoding(String value) {

	if (notBooted())
	    prebootstreamencoding = YoixObject.newString(value);
    }


    final void
    setPreBootParserEncoding(String value) {


	if (notBooted())
	    prebootparserencoding = YoixObject.newString(value);
    }


    final synchronized boolean
    setSecurityChecker(String name, YoixObject obj) {

	YoixObject  element;
	boolean     result = false;

	//
	// The RUNNING test lets -S options accumulated before the --applet
	// through. It's not a particularly important "feature" that we may
	// eventually decie to eliminate. Code elsewhere already rejects -S
	// options that appear after the --applet option.
	// 

	if (isApplet() == false || vmstate < RUNNING) {
	    if (obj != null && obj.isCallable() && obj.notNull()) {
		if (securitymanager == null)
		    setSecurityManager(YoixMake.yoixType(T_SECURITYMANAGER));
		if (securitymanager != null) {
		    if ((element = securitymanager.getObject(name)) != null) {
			if (element.isNull()) {
			    pushAccess(LRW_);
			    securitymanager.put(name, obj, true);
			    result = true;
			    popAccess();
			}
		    }
		}
	    }
	}
	return(result);
    }


    final synchronized boolean
    setSecurityManager(YoixObject obj) {

	boolean  result = false;

	if (securitymanager == null) {
	    if (YoixSecurityManager.setSecurityManager(obj)) {
		securitymanager = obj;
		result = true;
	    }
	}
	return(result);
    }


    final synchronized void
    setUserModules(boolean state) {

	//
	// Currently only allowed at startup (i.e., via a command line
	// option) and never turns user modules back on.
	//

	if (vmstate == INITIALIZING)
	    usermodules &= state;	// never on once off
    }


    final synchronized void
    setZipped(int flags) {

	if (vmstate == INITIALIZING) {
	    zipped = (flags & EXECUTE_ZIPPED_MASK);
	    YoixModule.tune(N_ZIPPED, new Integer(zipped));
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildModuleNames() {

	YoixBodyDictionaryObject  body;
	int                       length;
	int                       n;

	if (modulenames == null && yoixdict != null) {
	    modulenames = new ArrayList();
	    body = (YoixBodyDictionaryObject)yoixdict.body();
	    length = yoixdict.sizeof();
	    for (n = 0; n < length; n++) {
		if (body.moduleNameAt(n) != null)
		    modulenames.add(body.nameAt(n));
	    }
	}
    }


    private synchronized void
    buildScreen() {

	YoixObject  uimanager;
	double      screenresolution;
	double      magnification;
	Font        font;
	int         extent;

	//
	// This usually is an expensive operation that requires access to
	// your display, which could cause problems (e.g., a process on a
	// Unix system), so it's something we want to postpone until it's
	// really needed!! In other words, don't do this stuff until were
	// pretty sure the user is running a Yoix script that really does
	// want to do stuff on a display.
	//

	if (buildscreen && defaultscreen != null) {
	    pushAccess(LRW_);
	    buildscreen = false;
	    YoixBodyScreen.buildScreenDescription(YoixMisc.getDefaultScreenDevice(), 0, defaultscreen);

	    //
	    // These are explictily set here to help prevent the annoying
	    // "focus grab" behavior that happened whenever the interpreter
	    // was started on a Mac.
	    //

	    defaultscreen.putColor(N_BACKGROUND, DEFAULT_BACKGROUND);
	    defaultscreen.putColor(N_FOREGROUND, DEFAULT_FOREGROUND);

	    //
	    // Setting defaultmatrix isn't necessarily required because it
	    // happens anyway in getDefaultMatrix(), but it's probably best
	    // to take care of it now. Turns out that the code that clones
	    // our matrix explicitly changes permissions on the body - it's
	    // a kludge that we don't want to address right now.
	    //

	    defaultmatrix = defaultscreen.get(N_DEFAULTMATRIX);

	    //
	    // Decided default behavior should enable the font scaling code,
	    // so the N_FIXFONTS in YoixModuleVM.java is now initialized to
	    // true. Use the -f command line option to disable font scaling.
	    // Macs seem to handle fonts quite well, so we also decided they
	    // should skip the font scaling calculations. Actually omitting
	    // '\324' from the charset used by guessExtent() made our guess
	    // just about right on a Mac, but the Unix and Windows systems
	    // that we tried seemed get better answers with it in.
	    //
	    // NOTE - comment about the initial value being set to true no
	    // longer appears to be the case.
	    //

	    if (getBoolean(N_FIXFONTS)) {
		if ((magnification = getDouble(N_FONTMAGNIFICATION)) > 0) {
		    //
		    // Macs always seem to handle fonts well, so we decided
		    // to skip the adjustments if we're running on a Mac.
		    // 
		    if (ISMAC == false) {
			font = Font.decode(DEFAULT_JAVA_FONTNAME);
			//
			// It's probably size 12 but the pixel counting done
			// in guessExtent() might benefit from a bigger font.
			// Scaling up by 1.5 or so might also be reasonable,
			// but need to test thoroughly and didn't have time.
			//
			if (font.getSize() < 18)
			    font = new Font(font.getName(), font.getStyle(), 18);
			if ((extent = YoixMiscJFC.guessExtent(font, null)) > 0) {
			    screenresolution = YoixAWTToolkit.getScreenResolution();
			    fontscale = (font.getSize()*screenresolution)/(72.0*extent);

			    //
			    // This is a late addition - don't decrease the
			    // font size by default (i.e., fixfonts is 1).
			    //
			    if (getInt(N_FIXFONTS) == 1)
				fontscale = Math.max(1.0, fontscale);
			}
		    }
		}
	    }

	    //
	    // This must follow the fontscale calculation because there's
	    // an initialization method in YoixBodyUIMananger that might
	    // trigger the scaling of the fonts managed by UIMananger, and
	    // that in turn assumes fontscale is properly initialized. If
	    // you remove N_UIMANAGER from defaultscreen then you should add
	    //
	    //		YoixBodyUIManager.updateUIManager();
	    //
	    // to trigger the UIMananger font scaling, which is important
	    // on Linux or whenever font scaling has been requested using
	    // the -m command line option.
	    //

	    defaultscreen.put(N_UIMANAGER, YoixMake.yoixType(T_UIMANAGER));
	    if (lookandfeel != null) {
		uimanager = defaultscreen.getObject(N_UIMANAGER);
		uimanager.putString(N_LOOKANDFEEL, lookandfeel);
	    }

	    defaultscreen = YoixObject.newScreen(YoixMisc.getDefaultScreenDevice(), defaultscreen);
	    popAccess();
	}
    }


    private synchronized void
    buildScreens() {

	GraphicsDevice  screens[];
	YoixObject      description;
	int             n;

	if (buildscreens && allscreens != null) {
	    buildscreens = false;
	    if (buildscreen)
		buildScreen();
	    pushAccess(LRW_);
	    allscreens.putObject(0, defaultscreen);
	    if (YoixMisc.getScreenDeviceCount() > 1) {
		if ((screens = YoixMisc.getScreenDevices()) != null) {
		    allscreens.setGrowable(true);
		    allscreens.setGrowto(-1);
		    for (n = 1; n < screens.length; n++) {
			if ((description = YoixBodyScreen.buildScreenDescription(screens[n], n)) != null)
			    allscreens.putObject(n, YoixObject.newScreen(screens[n], description));
		    }
		    allscreens.setGrowable(false);
		}
	    }
	    popAccess();
	}
    }


    private void
    buildTypeNames() {

	YoixBodyDictionaryObject  body;
	YoixObject                obj;
	int                       n;

	if (typenames == null && typedict != null) {
	    typenames = new HashMap();
	    body = (YoixBodyDictionaryObject)typedict.body();
	    for (n = 0; n < typedict.sizeof(); n++) {
		if (body.existsAt(n)) {
		    obj = body.peekAt(n);
		    if (obj.isInteger())
			typenames.put(obj.intValue() + "", body.name(n));
		}
	    }
	}
    }


    private YoixObject
    buildTypeTemplate(YoixObject template, String typename) {

	Constructor  constructor;
	YoixObject   copy;
	String       classname;
	String       name;
	Class        source;
	int          length;
	int          growto;
	int          n;

	if (template.isDictionary()) {
	    if ((classname = template.getString(N_CLASSNAME)) != null) {
		if (YoixMisc.inPackage(classname, YOIXPACKAGE) == false) {
		    if (template.isDictionary()) {
			if (!(template.defined(N_MAJOR) && template.defined(N_MINOR))) {
			    //
			    // This was added very quickly - all we basically
			    // trying to do is create a new template that looks
			    // like the original but omits the N_CLASSNAME field.
			    // We eventually must have a better way to do this!!
			    //
			    length = template.length();
			    copy = YoixObject.newDictionary(length - 1);
			    for (n = 0; n < length; n++) {
				if (template.defined(n)) {
				    name = template.name(n);
				    if (name.equals(N_CLASSNAME) == false)
					copy.put(name, template.get(n, false), false);
				}
			    }
			    if ((growto = template.getGrowto()) >= length)
				growto -= 1;
			    copy.setGrowable(template.getGrowable());
			    copy.setAccessBody(template.getAccessBody());
			    copy.setMode(template.mode());
			    copy.setTypename((String)template.value[1]);
			    template = copy;
			}
		    }
		    try {
			source = Class.forName(classname);
			constructor = source.getDeclaredConstructor(new Class[] {YoixObject.class});
			constructor.setAccessible(true);
			template = (YoixObject)constructor.newInstance(new Object[] {template});
		    }
		    catch(ClassNotFoundException e) {}
		    catch(NoSuchMethodException e) {}
		    catch(InstantiationException e) {}
		    catch(IllegalAccessException e) {}
		    catch(InvocationTargetException e) {}
		}
	    }
	}
	return(template);
    }


    private void
    buildVM() {

	String  path;

	if (vmstate == BOOTING) {
	    if (checkedsuffix) {
		setAccess(LRWX);
		loaded = new HashMap();
		YoixModule.boot(loaded);
		errordict = (YoixObject)loaded.get(N_ERRORDICT);
		reserved = (YoixObject)loaded.get(N_RESERVED);
		typedict = (YoixObject)loaded.get(N_TYPEDICT);
		vm = (YoixObject)loaded.get(N_VM);
		defaultscreen = ((YoixBodyDictionaryObject)vm.body()).activateField(N_SCREEN);
		allscreens = ((YoixBodyDictionaryObject)vm.body()).activateField(N_SCREENS);
		yoixdict = (YoixObject)loaded.get(N_YOIX);

		vmPut(N_STDIN, YoixObject.newStream((YoixObject)loaded.get(N_STDIN), System.in));
		vmPut(N_STDOUT, YoixObject.newStream((YoixObject)loaded.get(N_STDOUT), System.out));
		vmPut(N_STDERR, YoixObject.newStream((YoixObject)loaded.get(N_STDERR), System.err));

		YoixBodyBlock.setReserved(reserved);

		if ((path = getString(N_TMPDIR)) != null) {
		    if (path.equals(TMPDIR) == false)
			(new File(new File(path).getAbsolutePath())).mkdir();
		}

		buildTypeNames();
		buildModuleNames();
		setAccess(0);
	    }
	}
    }


    private void
    prepareVM() {

	String  codebase;
	String  jarfile;

	//
	// Finishes the booting process, but at this point everything the
	// interpreter needs to run has been loaded and installed, which
	// means methods, like YoixMake.yoixType(), will work properly.
	// 

	if (vmstate == BOOTED) {
	    if (checkedsuffix) {
		YoixModule.autoload(autoload);
		YoixVMShutdownThread.installShutdownThread();

		if ((codebase = YoixMisc.getCodeBase(this.getClass())) != null)
		    System.setProperty("yoix.codebase", codebase);
		if ((jarfile = YoixMisc.getLocalJarPath(this.getClass())) != null)
		    System.setProperty("yoix.jarfile", jarfile);

		if (isApplication() || setSecurityManager(YoixMake.yoixType(T_SECURITYMANAGER)))
		    YoixSecurityOptions.installOptions();
		else VM.abort(INVALIDACCESS, new String[] {N_APPLET, "can't install a security manager"});
	    }
	}
    }


    private YoixObject
    vmGet(String name) {

	YoixObject  lval;
	YoixObject  obj;
	Object      body;

	lval = vmLvalue(name);
	body = lval.body();
	if (body instanceof YoixBodyDictionaryObject)
	    obj = ((YoixBodyDictionaryObject)lval.body()).peekAt(lval.offset());
	else obj = lval.get();

	return(obj);
    }


    private YoixObject
    vmLvalue(String name) {

	YoixObject  lval;
	YoixObject  source;
	YoixObject  dest;
	Hashtable   lvalues;

	lvalues = YoixBodyBlock.getLvalues();
	lval = null;

	if (lvalues == null || (lval = (YoixObject)lvalues.get(name)) == null) {
	    if ((source = reserved) == null)
		source = (YoixObject)loaded.get(N_RESERVED);
	    if (source == null || source.defined(name) == false) {
		if ((source = YoixBodyBlock.getVM()) == null) {
		    if ((source = vm) == null)
			source = (YoixObject)loaded.get(N_VM);
		}
		if (source.defined(name) == false) {
		    source = source.get(N_SCREEN);
		    if (source.defined(name))
			dest = source;
		    else dest = null;
		} else dest = source;
	    } else dest = source;
	    if (dest != null) {
		lval = YoixObject.newLvalue(dest, name);
		lval.setResolve(true);		// only for expression()
		if (lvalues != null)
		    lvalues.put(name, lval);
	    } else die(INTERNALERROR, name);
	}

	return(lval);
    }


    private void
    vmPut(String name, YoixObject value) {

	vmLvalue(name).put(value);
    }


    private Object
    vmStream(String name) {

	Object  stream;

	if (vmstate <= BOOTING) {	// still initializing or booting
	    if (name == N_STDIN)
		stream = System.in;
	    else if (name == N_STDOUT)
		stream = System.out;
	    else if (name == N_STDERR)
		stream = System.err;
	    else stream = null;
	} else stream = vmGet(name).streamValue();

	return(stream);
    }
}

