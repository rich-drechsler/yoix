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
import java.util.*;

public abstract
class YoixModule

    implements YoixConstants,
	       YoixConstantsSwing

{

    //
    // Special constants used to control module loading in readTable().
    // Several, like CLASS, MODULE, and PACKAGE are hard to understand,
    // and a few others (e.g., INCLUDE and REMOVE) are not currently
    // used by any module that we know about.
    //
    // Here's an incomplete description of the special constants:
    //
    //      CLASS The arg should be a String (or null) that's the
    //            name of a class that implements a newPointer()
    //            method that YoixObject.newPointer() calls when
    //            it needs to create ... (see YoixMake.yoixType()).
    //
    // Definitely needs documentation, but it can wait. Until then the
    // code itself (i.e., readTable()) is the only real documentation.
    //

    public static final int  CLASS = LASTTOKEN + 1;
    public static final int  COLOR = LASTTOKEN + 2;
    public static final int  DECLARE = LASTTOKEN + 3;
    public static final int  DIMENSION = LASTTOKEN + 4;
    public static final int  DUP = LASTTOKEN + 5;
    public static final int  GET = LASTTOKEN + 6;
    public static final int  GROWTO = LASTTOKEN + 7;
    public static final int  INCLUDE = LASTTOKEN + 8;
    public static final int  INSETS = LASTTOKEN + 9;
    public static final int  MODULE = LASTTOKEN + 10;
    public static final int  PACKAGE = LASTTOKEN + 11;
    public static final int  POINT = LASTTOKEN + 12;
    public static final int  PUT = LASTTOKEN + 13;
    public static final int  READCLASS = LASTTOKEN + 14;
    public static final int  REMOVE = LASTTOKEN + 15;
    public static final int  RESTART = LASTTOKEN + 16;
    public static final int  TYPEDEF = LASTTOKEN + 17;
    public static final int  TYPENAME = LASTTOKEN + 18;

    public static final Integer  $CLASS = new Integer(CLASS);
    public static final Integer  $COLOR = new Integer(COLOR);
    public static final Integer  $DECLARE = new Integer(DECLARE);
    public static final Integer  $DIMENSION = new Integer(DIMENSION);
    public static final Integer  $DUP = new Integer(DUP);
    public static final Integer  $GET = new Integer(GET);
    public static final Integer  $GROWTO = new Integer(GROWTO);
    public static final Integer  $INCLUDE = new Integer(INCLUDE);
    public static final Integer  $INSETS = new Integer(INSETS);
    public static final Integer  $MODULE = new Integer(MODULE);
    public static final Integer  $PACKAGE = new Integer(PACKAGE);
    public static final Integer  $POINT = new Integer(POINT);
    public static final Integer  $PUT = new Integer(PUT);
    public static final Integer  $READCLASS = new Integer(READCLASS);
    public static final Integer  $REMOVE = new Integer(REMOVE);
    public static final Integer  $RESTART = new Integer(RESTART);
    public static final Integer  $TYPEDEF = new Integer(TYPEDEF);
    public static final Integer  $TYPENAME = new Integer(TYPENAME);

    //
    // Current collection of module names - convenient because they need
    // to be mentioned in at least two other class files.
    //

    public static final String  M_AWT = "awt";
    public static final String  M_CTYPE = "ctype";
    public static final String  M_ERROR = "error";
    public static final String  M_EVENT = "event";
    public static final String  M_FACTORIAL = "factorial";
    public static final String  M_GRAPH = "graph";
    public static final String  M_GRAPHICS = "graphics";
    public static final String  M_IMAGE = "image";
    public static final String  M_IO = "io";
    public static final String  M_MATH = "math";
    public static final String  M_MODULE = "module";
    public static final String  M_NET = "net";
    public static final String  M_PARSER = "parser";
    public static final String  M_RE = "re";
    public static final String  M_ROBOT = "robot";
    public static final String  M_SECURE = "secure";
    public static final String  M_SOUND = "sound";
    public static final String  M_STDIO = "stdio";
    public static final String  M_STRING = "string";
    public static final String  M_SWING = "swing";
    public static final String  M_SWING_EXTENSION = "swing_extension";
    public static final String  M_SYSTEM = "system";
    public static final String  M_THREAD = "thread";
    public static final String  M_TYPE = "type";
    public static final String  M_UTIL = "util";
    public static final String  M_WINDOWS = "windows";
    public static final String  M_XCOLOR = "xcolor";

    //
    // An object that represents where the loader is putting things. The
    // string itself is insignificant - it is never used!!!
    //

    public static final String  $_THIS = "";

    //
    // Objects that represent some common constants.
    //

    public static final Integer  $BUFSIZ = new Integer(BUFSIZ);
    public static final Integer  $FALSE = new Integer(0);
    public static final Integer  $YOIX_EOF = new Integer(YOIX_EOF);
    public static final Integer  $TRUE = new Integer(1);
    public static final Double   $NAN = new Double(Double.NaN);

    //
    // Objects that represent permissions - the complicated ones at the end
    // refer to the body and object itself (refer to the readTable() method
    // if you need more details). The ones that start with "$A" let you set
    // the ANYMAJOR flag, which means the initial type of the entry doesn't
    // control what can be stored there later on.
    //

    public static final Integer  $___ = new Integer(___);
    public static final Integer  $R__ = new Integer(R__);
    public static final Integer  $_W_ = new Integer(_W_);
    public static final Integer  $__X = new Integer(__X);
    public static final Integer  $RW_ = new Integer(RW_);
    public static final Integer  $R_X = new Integer(R_X);
    public static final Integer  $_WX = new Integer(_WX);
    public static final Integer  $RWX = new Integer(RWX);

    public static final Integer  $L___ = new Integer(L___);
    public static final Integer  $LR__ = new Integer(LR__);
    public static final Integer  $L_W_ = new Integer(L_W_);
    public static final Integer  $L__X = new Integer(L__X);
    public static final Integer  $LRW_ = new Integer(LRW_);
    public static final Integer  $LR_X = new Integer(LR_X);
    public static final Integer  $L_WX = new Integer(L_WX);
    public static final Integer  $LRWX = new Integer(LRWX);

    public static final Integer  $A_W_ = new Integer(ANYMAJOR | _W_);
    public static final Integer  $A_WX = new Integer(ANYMAJOR | _WX);
    public static final Integer  $ARW_ = new Integer(ANYMAJOR | RW_);
    public static final Integer  $ARWX = new Integer(ANYMAJOR | RWX);

    public static final Integer  $ROR = new Integer(ROR);
    public static final Integer  $RORO = new Integer(RORO);
    public static final Integer  $RORW = new Integer(RORW);
    public static final Integer  $RWR = new Integer(RWR);
    public static final Integer  $RWRO = new Integer(RWRO);
    public static final Integer  $RWRW = new Integer(RWRW);

    //
    // Objects that correspond to some parser constants, so they're public,
    // built once, and easy to use in tables.
    //

    public static final Integer  $ARRAY = new Integer(ARRAY);
    public static final Integer  $AUDIOCLIP = new Integer(AUDIOCLIP);
    public static final Integer  $BUILTIN = new Integer(BUILTIN);
    public static final Integer  $BUTTON = new Integer(BUTTON);
    public static final Integer  $BUTTONGROUP = new Integer(BUTTONGROUP);
    public static final Integer  $CALENDAR = new Integer(CALENDAR);
    public static final Integer  $CALLABLE = new Integer(CALLABLE);
    public static final Integer  $CANVAS = new Integer(CANVAS);
    public static final Integer  $CERTIFICATE = new Integer(CERTIFICATE);
    public static final Integer  $CHECKBOX = new Integer(CHECKBOX);
    public static final Integer  $CHECKBOXGROUP = new Integer(CHECKBOXGROUP);
    public static final Integer  $CHOICE = new Integer(CHOICE);
    public static final Integer  $CIPHER = new Integer(CIPHER);
    public static final Integer  $CLIPBOARD = new Integer(CLIPBOARD);
    public static final Integer  $COMPILER = new Integer(COMPILER);
    public static final Integer  $COMPONENT = new Integer(COMPONENT);
    public static final Integer  $COOKIEMANAGER = new Integer(COOKIEMANAGER);
    public static final Integer  $DATAGRAMSOCKET = new Integer(DATAGRAMSOCKET);
    public static final Integer  $DATETIME = new Integer(DATETIME);
    public static final Integer  $DIALOG = new Integer(DIALOG);
    public static final Integer  $DICT = new Integer(DICTIONARY);
    public static final Integer  $DOUBLE = new Integer(DOUBLE);
    public static final Integer  $EDGE = new Integer(EDGE);
    public static final Integer  $ELEMENT = new Integer(ELEMENT);
    public static final Integer  $FILE = new Integer(FILE);
    public static final Integer  $FILEDIALOG = new Integer(FILEDIALOG);
    public static final Integer  $FONT = new Integer(FONT);
    public static final Integer  $FRAME = new Integer(FRAME);
    public static final Integer  $FUNCTION = new Integer(FUNCTION);
    public static final Integer  $GRAPH = new Integer(GRAPH);
    public static final Integer  $GRAPHICS = new Integer(GRAPHICS);
    public static final Integer  $GRAPHOBSERVER = new Integer(GRAPHOBSERVER);
    public static final Integer  $HASHTABLE = new Integer(HASHTABLE);
    public static final Integer  $IMAGE = new Integer(IMAGE);
    public static final Integer  $INTEGER = new Integer(INTEGER);
    public static final Integer  $JBUTTON = new Integer(JBUTTON);
    public static final Integer  $JCANVAS = new Integer(JCANVAS);
    public static final Integer  $JCOLORCHOOSER = new Integer(JCOLORCHOOSER);
    public static final Integer  $JCOMBOBOX = new Integer(JCOMBOBOX);
    public static final Integer  $JCOMPONENT = new Integer(JCOMPONENT);
    public static final Integer  $JDESKTOPPANE = new Integer(JDESKTOPPANE);
    public static final Integer  $JDIALOG = new Integer(JDIALOG);
    public static final Integer  $JFILECHOOSER = new Integer(JFILECHOOSER);
    public static final Integer  $JFILEDIALOG = new Integer(JFILEDIALOG);
    public static final Integer  $JFRAME = new Integer(JFRAME);
    public static final Integer  $JINTERNALFRAME = new Integer(JINTERNALFRAME);
    public static final Integer  $JLABEL = new Integer(JLABEL);
    public static final Integer  $JLAYEREDPANE = new Integer(JLAYEREDPANE);
    public static final Integer  $JLIST = new Integer(JLIST);
    public static final Integer  $JMENU = new Integer(JMENU);
    public static final Integer  $JMENUBAR = new Integer(JMENUBAR);
    public static final Integer  $JMENUITEM = new Integer(JMENUITEM);
    public static final Integer  $JPANEL = new Integer(JPANEL);
    public static final Integer  $JPOPUPMENU = new Integer(JPOPUPMENU);
    public static final Integer  $JPROGRESSBAR = new Integer(JPROGRESSBAR);
    public static final Integer  $JSCROLLBAR = new Integer(JSCROLLBAR);
    public static final Integer  $JSCROLLPANE = new Integer(JSCROLLPANE);
    public static final Integer  $JSEPARATOR = new Integer(JSEPARATOR);
    public static final Integer  $JSLIDER = new Integer(JSLIDER);
    public static final Integer  $JSPLITPANE = new Integer(JSPLITPANE);
    public static final Integer  $JTABBEDPANE = new Integer(JTABBEDPANE);
    public static final Integer  $JTABLE = new Integer(JTABLE);
    public static final Integer  $JTEXTAREA = new Integer(JTEXTAREA);
    public static final Integer  $JTEXTCANVAS = new Integer(JTEXTCANVAS);
    public static final Integer  $JTEXTFIELD = new Integer(JTEXTFIELD);
    public static final Integer  $JTEXTPANE = new Integer(JTEXTPANE);
    public static final Integer  $JTEXTTERM = new Integer(JTEXTTERM);
    public static final Integer  $JTOOLBAR = new Integer(JTOOLBAR);
    public static final Integer  $JTREE = new Integer(JTREE);
    public static final Integer  $JWINDOW = new Integer(JWINDOW);
    public static final Integer  $KEY = new Integer(KEY);
    public static final Integer  $KEYSTORE = new Integer(KEYSTORE);
    public static final Integer  $LABEL = new Integer(LABEL);
    public static final Integer  $LIST = new Integer(LIST);
    public static final Integer  $LOCALE = new Integer(LOCALE);
    public static final Integer  $MATRIX = new Integer(MATRIX);
    public static final Integer  $MENUBAR = new Integer(MENUBAR);
    public static final Integer  $MULTICASTSOCKET = new Integer(MULTICASTSOCKET);
    public static final Integer  $NODE = new Integer(NODE);
    public static final Integer  $NULL = new Integer(NULL);
    public static final Integer  $NUMBER = new Integer(NUMBER);
    public static final Integer  $OBJECT = new Integer(OBJECT);
    public static final Integer  $OPTION = new Integer(OPTION);
    public static final Integer  $PANEL = new Integer(PANEL);
    public static final Integer  $PARSETREE = new Integer(PARSETREE);
    public static final Integer  $PATH = new Integer(PATH);
    public static final Integer  $POINTER = new Integer(POINTER);
    public static final Integer  $POPUPMENU = new Integer(POPUPMENU);
    public static final Integer  $PROCESS = new Integer(PROCESS);
    public static final Integer  $RANDOM = new Integer(RANDOM);
    public static final Integer  $REGEXP = new Integer(REGEXP);
    public static final Integer  $SCREEN = new Integer(SCREEN);
    public static final Integer  $SCROLLBAR = new Integer(SCROLLBAR);
    public static final Integer  $SCROLLPANE = new Integer(SCROLLPANE);
    public static final Integer  $SECURITYMANAGER = new Integer(SECURITYMANAGER);
    public static final Integer  $SERVERSOCKET = new Integer(SERVERSOCKET);
    public static final Integer  $SOCKET = new Integer(SOCKET);
    public static final Integer  $STATEMENT = new Integer(STATEMENT);
    public static final Integer  $STREAM = new Integer(STREAM);
    public static final Integer  $STRING = new Integer(STRING);
    public static final Integer  $STRINGSTREAM = new Integer(STRINGSTREAM);
    public static final Integer  $SUBEXP = new Integer(SUBEXP);
    public static final Integer  $TABLECOLUMN = new Integer(TABLECOLUMN);
    public static final Integer  $TABLEMANAGER = new Integer(TABLEMANAGER);
    public static final Integer  $TEXTAREA = new Integer(TEXTAREA);
    public static final Integer  $TEXTCANVAS = new Integer(TEXTCANVAS);
    public static final Integer  $TEXTFIELD = new Integer(TEXTFIELD);
    public static final Integer  $TEXTTERM = new Integer(TEXTTERM);
    public static final Integer  $THREAD = new Integer(THREAD);
    public static final Integer  $TIME = new Integer(TIME);
    public static final Integer  $TIMEZONE = new Integer(TIMEZONE);
    public static final Integer  $TRANSFERHANDLER = new Integer(TRANSFERHANDLER);
    public static final Integer  $UIMANAGER = new Integer(UIMANAGER);
    public static final Integer  $URL = new Integer(URL);
    public static final Integer  $VECTOR = new Integer(VECTOR);
    public static final Integer  $WINDOW = new Integer(WINDOW);
    public static final Integer  $ZIPENTRY = new Integer(ZIPENTRY);

    //
    // Objects that represent the constants that we have tried to assign
    // a single fixed value. Important so different modules don't step on
    // each other when imported.
    //

    public static final Integer  $YOIX_ALWAYS = new Integer(YOIX_ALWAYS);
    public static final Integer  $YOIX_AS_NEEDED = new Integer(YOIX_AS_NEEDED);
    public static final Integer  $YOIX_BOTH = new Integer(YOIX_BOTH);
    public static final Integer  $YOIX_BOTTOM = new Integer(YOIX_BOTTOM);
    public static final Integer  $YOIX_BOTTOMLEFT = new Integer(YOIX_BOTTOMLEFT);
    public static final Integer  $YOIX_BOTTOMRIGHT = new Integer(YOIX_BOTTOMRIGHT);
    public static final Integer  $YOIX_CENTER = new Integer(YOIX_CENTER);
    public static final Integer  $YOIX_EAST = new Integer(YOIX_EAST);
    public static final Integer  $YOIX_HORIZONTAL = new Integer(YOIX_HORIZONTAL);
    public static final Integer  $YOIX_LEADING = new Integer(YOIX_LEADING);
    public static final Integer  $YOIX_LEFT = new Integer(YOIX_LEFT);
    public static final Integer  $YOIX_LINEMODE = new Integer(YOIX_LINEMODE);
    public static final Integer  $YOIX_LOWER_LEFT_CORNER = new Integer(YOIX_LOWER_LEFT_CORNER);
    public static final Integer  $YOIX_LOWER_RIGHT_CORNER = new Integer(YOIX_LOWER_RIGHT_CORNER);
    public static final Integer  $YOIX_LOAD = new Integer(YOIX_LOAD);
    public static final Integer  $YOIX_NEVER = new Integer(YOIX_NEVER);
    public static final Integer  $YOIX_NONE = new Integer(YOIX_NONE);
    public static final Integer  $YOIX_NORTH = new Integer(YOIX_NORTH);
    public static final Integer  $YOIX_NORTHEAST = new Integer(YOIX_NORTHEAST);
    public static final Integer  $YOIX_NORTHWEST = new Integer(YOIX_NORTHWEST);
    public static final Integer  $YOIX_RELATIVE = new Integer(YOIX_RELATIVE);
    public static final Integer  $YOIX_REMAINDER = new Integer(YOIX_REMAINDER);
    public static final Integer  $YOIX_RIGHT = new Integer(YOIX_RIGHT);
    public static final Integer  $YOIX_SAVE = new Integer(YOIX_SAVE);
    public static final Integer  $YOIX_SOUTH = new Integer(YOIX_SOUTH);
    public static final Integer  $YOIX_SOUTHEAST = new Integer(YOIX_SOUTHEAST);
    public static final Integer  $YOIX_SOUTHWEST = new Integer(YOIX_SOUTHWEST);
    public static final Integer  $YOIX_TOP = new Integer(YOIX_TOP);
    public static final Integer  $YOIX_TOPLEFT = new Integer(YOIX_TOPLEFT);
    public static final Integer  $YOIX_TOPRIGHT = new Integer(YOIX_TOPRIGHT);
    public static final Integer  $YOIX_TRAILING = new Integer(YOIX_TRAILING);
    public static final Integer  $YOIX_UPPER_LEFT_CORNER = new Integer(YOIX_UPPER_LEFT_CORNER);
    public static final Integer  $YOIX_UPPER_RIGHT_CORNER = new Integer(YOIX_UPPER_RIGHT_CORNER);
    public static final Integer  $YOIX_VERTICAL = new Integer(YOIX_VERTICAL);
    public static final Integer  $YOIX_WEST = new Integer(YOIX_WEST);
    public static final Integer  $YOIX_WORDMODE = new Integer(YOIX_WORDMODE);

    //
    // The results obtained when modules are officially loaded are saved
    // in modulecache. This was a Hashtable, but we think a HashMap will
    // work. This was a recent change (8/18/04) that can be undone if you
    // notice strange behavior that seem to be thread related.
    //

    private static HashMap  modulecache = new HashMap();
    private static boolean  booted = false;

    //
    // A few loader definitions.
    //

    public static final String  MODULECHECKER = "checker";
    public static final String  MODULECREATED = "$MODULECREATED";
    public static final String  MODULEINIT = "$init";
    public static final String  MODULETABLE = "$module";
    public static final String  MODULEEXTRACTED = "extracted";
    public static final String  MODULELOADED = "loaded";
    public static final String  MODULELOADER = "loader";
    public static final String  MODULENAME = "$MODULENAME";
    public static final String  MODULENOTICE = "$MODULENOTICE";
    public static final String  MODULETUNER = "tuner";
    public static final String  MODULEVERSION = "$MODULEVERSION";

    //
    // Some classes that we need for reflection.
    //

    private static final Class  MAPCLASS = Map.class;
    private static final Class  OBJECTCLASS = Object.class;
    private static final Class  STRINGCLASS = String.class;
    private static final Class  TABLECLASS = (new Object[0]).getClass();

    ///////////////////////////////////
    //
    // YoixModule Methods
    //
    ///////////////////////////////////

    static synchronized void
    autoload(ArrayList list) {

	String  failed = null;
	String  classname;
	Object  array[];
	int     n;

	if (list != null) {
	    array = list.toArray();
	    for (n = 0; n < array.length; n++) {
		if (array[n] instanceof String) {
		    classname = (String)array[n];
		    invokeLoader(classname, new HashMap(), true);
		}
	    }
	}
    }


    static synchronized void
    boot(Map cache) {

	String  classname;

	if (booted == false) {
	    classname = YoixModuleVM.class.getName();
	    invokeLoader(classname, cache, false);
	    modulecache.remove(classname);
	    booted = true;
	} else VM.die(INTERNALERROR);
    }


    public static String
    created(String classname) {

	String  value = null;
	Class   source;

	try {
	    source = Class.forName(classname);
	    try {
		value = (String)source.getDeclaredField(MODULECREATED).get(null);
	    }
	    catch(NoSuchFieldException e) {
		if (YoixMisc.inPackage(classname, YOIXPACKAGE))
		    value = YOIXCREATED;
	    }
	    catch(Exception e) {}
	}
	catch(ClassNotFoundException e) {}

	return(value);
    }


    static synchronized YoixObject
    get(String classname, String fieldname) {

	Map  cache;

	if ((cache = (Map)modulecache.get(classname)) == null) {
	    cache = new HashMap();
	    invokeLoader(classname, cache, true);
	}
	return((YoixObject)cache.get(fieldname));
    }


    static synchronized String
    load(Class source) {

	return(source != null ? load(source.getName()) : null);
    }


    static synchronized String
    load(String name) {

	return(load(new String[] {name}));
    }


    static synchronized String
    load(String list[]) {

	String  loaded = null;
	String  classname;
	int     n;

	for (n = 0; n < list.length && loaded == null; n++) {
	    if ((classname = list[n]) != null) {
		if (modulecache.get(classname) == null) {
		    invokeLoader(classname, new HashMap(), true);
		    if (modulecache.get(classname) != null)
			loaded = classname;
		} else loaded = classname;
	    }
	}
	return(loaded);
    }


    public static Map
    loader(String classname, String modulename, Object init[], Object module[], Object extracted[], Map cache) {

	YoixObject  obj;
	HashMap     templates;
	int         n;

	//
	// This method is public so reflection works easily, but we should
	// only get here from invokeLoader()!!!
	//

	if (module != null && module.length > 0) {
	    templates = new HashMap();
	    if (cache == null)
		cache = new HashMap();
	    if (init != null)
		readTable(classname, modulename, init, cache, templates);
	    readTable(classname, modulename, module, cache, templates);
	    if (extracted != null) {
		for (n = 0; n < extracted.length; n++)
		    extracted[n] = cache.get(extracted[n]);
	    }
	    if (modulename != null) {
		if ((obj = (YoixObject)cache.get(modulename)) != null)
		    obj.setGrowable(false);
	    }
	    invokeLoaded(classname);
	    VM.cleanup(classname);
	}
	return(cache);
    }


    public static String
    notice(String classname) {

	String  value = null;
	Class   source;

	try {
	    source = Class.forName(classname);
	    try {
		value = (String)source.getDeclaredField(MODULENOTICE).get(null);
	    }
	    catch(NoSuchFieldException e) {
		if (YoixMisc.inPackage(classname, YOIXPACKAGE))
		    value = YOIXNOTICE;
	    }
	    catch(Exception e) {}
	}
	catch(ClassNotFoundException e) {}

	return(value);
    }


    static void
    tune(String name, Object value) {

	invokeTuner(YoixModuleVM.class.getName(), name, value);
    }


    public static void
    tuner(Object table[], String name, Object value) {

	int  n;

	//
	// This method is public so reflection works easily, but we should
	// only get here from invokeTuner()!!!
	//

	if (table != null) {
	    for (n = 0; n <= table.length - 5; n += 5) {
		if (name.equals((String)table[n+4])) {
		    if (table[n+3] != null) {	// null means no changes
			if (table[n+2] == $NULL) {
			    if (table[n+1].equals(T_STRING))
				table[n+2] = $STRING;
			    else VM.die(INTERNALERROR);
			}
			table[n+1] = value;
		    }
		    break;
		}
	    }
	}
    }


    public static String
    version(String classname) {

	String  value = null;
	Class   source;

	try {
	    source = Class.forName(classname);
	    try {
		value = (String)source.getDeclaredField(MODULEVERSION).get(null);
	    }
	    catch(NoSuchFieldException e) {
		if (YoixMisc.inPackage(classname, YOIXPACKAGE))
		    value = YOIXVERSION;
	    }
	    catch(Exception e) {}
	}
	catch(ClassNotFoundException e) {}

	return(value);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static void
    checkLoad(String classname) {

	SecurityManager  sm;

	//
	// Eventually call invokeChecker() for all classes??
	//

	if (YoixMisc.inPackage(classname, YOIXPACKAGE) == false) {
	    if (VM.getUserModules()) {
		if ((sm = System.getSecurityManager()) instanceof YoixSecurityManager) {
		    ((YoixSecurityManager)sm).checkYoixModule(
			YoixObject.newString(classname)
		    );
		}
		invokeChecker(classname);
	    } else throw(new SecurityException());
	}
    }


    private static synchronized void
    invokeChecker(String classname) {

	Method  method;
	Class   source;

	try {
	    source = Class.forName(classname);
	    method = source.getMethod(MODULECHECKER, new Class[] {OBJECTCLASS});
	    method.invoke(null, new Object[] {VM.newVM()});
	}
	catch(ClassNotFoundException e) {}	// should be impossible
	catch(NoSuchMethodException e) {}	// it's optional
	catch(IllegalAccessException e) {}	// not our responsibility
	catch(InvocationTargetException e) {
	    throw(new SecurityException());
	}
    }


    private static synchronized void
    invokeLoaded(String classname) {

	Method  method;
	Class   source;

	try {
	    source = Class.forName(classname);
	    method = source.getMethod(MODULELOADED, new Class[] {});
	    method.invoke(null, new Object[] {});
	}
	catch(ClassNotFoundException e) {}	// should be impossible
	catch(NoSuchMethodException e) {}	// it's optional
	catch(IllegalAccessException e) {}	// not our responsibility
	catch(InvocationTargetException e) {
	    throw(new SecurityException());
	}
    }


    private static synchronized void
    invokeLoader(String classname, Map cache, boolean silent) {

	Method  method;
	Object  init[];
	Object  module[];
	Object  extracted;
	String  badfield;
	String  modulename;
	Field   modulefield;
	Class   source;

	badfield = null;

	try {
	    if (booted && VM.bitCheck(N_DEBUG, DEBUG_LOADMODULE))
		VM.println(N_STDOUT, "load module " + classname);
	    source = Class.forName(classname);
	    if (isYoixModule(source)) {
		checkLoad(classname);
		module = null;
		modulename = null;
		init = null;
		extracted = null;
		method = source.getMethod(
		    MODULELOADER,
		    new Class[] {
			STRINGCLASS,
			STRINGCLASS,
			TABLECLASS,
			TABLECLASS,
			TABLECLASS,
			MAPCLASS
		    }
		);
		try {
		    modulefield = source.getDeclaredField(MODULETABLE);
		    module = (Object [])modulefield.get(null);
		}
		catch(Exception e) {
		    badfield = MODULETABLE;
		    throw(e);
		}
		try {
		    init = (Object [])source.getDeclaredField(MODULEINIT).get(null);
		}
		catch(NoSuchFieldException e) {}
		catch(Exception e) {
		    badfield = MODULEINIT;
		    throw(e);
		}
		try {
		    extracted = (Object)source.getDeclaredField(MODULEEXTRACTED).get(null);
		}
		catch(NoSuchFieldException e) {}
		catch(Exception e) {
		    badfield = MODULEEXTRACTED;
		    throw(e);
		}
		try {
		    modulename = (String)source.getDeclaredField(MODULENAME).get(null);
		}
		catch(NoSuchFieldException e) {}
		modulecache.put(classname, cache);	// must preceed invoke()
		method.invoke(
		    null,
		    new Object[] {classname, modulename, init, module, extracted, cache}
		);
		modulefield.set(source, null);
	    } else VM.warn(INVALIDSUPERCLASS, classname);
	}
	catch(ClassNotFoundException e) {
	    if (silent == false || VM.bitCheck(N_DEBUG, DEBUG_LOADMODULE)) {
		VM.caughtException(e, true);
		VM.warn(UNDEFINEDCLASS, classname);
	    }
	}
	catch(UnsupportedClassVersionError e) {
	    if (silent == false || VM.bitCheck(N_DEBUG, DEBUG_LOADMODULE)) {
		VM.warn(UNDEFINEDCLASS, classname);
	    }
	}
	catch(SecurityException e) {
	    throw(e);
	}
	catch(Exception e) {
	    VM.caughtException(e, true);
	    if (badfield != null)
		VM.warn(INVALIDFIELD, classname + "." + badfield);
	    else VM.warn(INVALIDCLASS, classname);
	}
    }


    private static synchronized void
    invokeTuner(String classname, String name, Object value) {

	YoixObject  obj = null;
	Method      method;
	Object      table[];
	Class       source;

	try {
	    source = Class.forName(classname);
	    if (source.getSuperclass().equals(YoixModule.class)) {
		method = source.getMethod(
		    MODULETUNER,
		    new Class[] {
			TABLECLASS,
			STRINGCLASS,
			OBJECTCLASS
		    }
		);
		table = (Object [])source.getDeclaredField(MODULEINIT).get(null);
		method.invoke(
		    null,
		    new Object[] {table, name, value}
		);
	    } else throw(new Exception());
	}
	catch(ClassNotFoundException e) {
	    VM.caughtException(e);
	    VM.warn(UNDEFINEDCLASS, classname);
	}
	catch(Exception e) {
	    VM.caughtException(e);
	    VM.warn(INVALIDCLASS, classname);
	}
    }


    private static boolean
    isYoixModule(Class source) {

	boolean  result = false;

	while (source != null) {
	    if (source.equals(YoixModule.class)) {
		result = true;
		break;
	    } else source = source.getSuperclass();
	}
	return(result);
    }


    private static int
    mapMinor(YoixObject obj) {

	int  major;
	int  minor;

	//
	// Maps the N_MAJOR numbers defined in obj into numbers that should
	// be used when creating we create null objects. Confusing, but it's
	// very important.
	//

	switch (major = obj.getInt(N_MAJOR, -1)) {
	    case JCOMPONENT:
		minor = COMPONENT;
		break;

	    default:
		minor = (major == -1) ? obj.minor() : major;
		break;
	}
	return(minor);
    }


    private static void
    readClass(String classname, YoixObject dest, String prefix, Class type)

	throws Exception

    {

	YoixObject  current;
	YoixObject  obj;
	Object      value;
	String      name;
	Class       source;
	Field       fields[];
	int         n;
	int         offset = -1;

	if (VM.bitCheck(N_DEBUG, DEBUG_READCLASS))
	    VM.println(N_STDOUT, "read class " + classname);

	if (prefix != null && (n = prefix.indexOf('\t')) >= 0) {
	    offset = prefix.length() - (n + 1);
	    prefix = prefix.substring(0, n);
	}

	source = Class.forName(classname);
	try {
	    //
	    // Catching SecurityException prevents problems when we're running
	    // as an untrusted application under javaws, however there's a good
	    // chance that always using source.getFields() would work. We will
	    // eventually look into it, but this is a safe approach won't cause
	    // any new problems.
	    //

	    fields = source.getDeclaredFields();
	}
	catch(SecurityException e) {
	    //
	    // Probably can dispense with source.getDeclaredFields() and just
	    // use source.getFields(). We eventually will experiment.
	    //
	    fields = source.getFields();
	}
	for (n = 0; n < fields.length; n++) {
	    try {
		name = fields[n].getName();
		if (name.charAt(0) != '$' && (prefix == null || name.startsWith(prefix))) {
		    value = fields[n].get(source);
		    if (type == null || type.isInstance(value)) {
			if (value instanceof Number)
			    obj = YoixObject.newNumber((Number)value);
			else if (value instanceof String)
			    obj = YoixObject.newString((String)value);
			else if (value instanceof Color)
			    obj = YoixObject.newColor((Color)value);
			else if (value instanceof Locale)
			    obj = YoixObject.newLocale((Locale)value);
			else obj = null;
			if (obj != null) {
			    if (offset > 0)
				name = name.substring(offset);
			    if (dest.defined(name) == false) {
				//
				// Eventually do better setting permissions.
				//
				obj.setAccessBody(LR__);
				dest.declare(name, obj, LR__);
			    } else {
				current = dest.get(name, false);
				if (YoixInterpreter.equalsEQ(current, obj) == false)
				    VM.abort(BADDECLARATION, name);
			    }
			}
		    }
		}
	    }
	    catch(Exception e) {}
	}
    }


    private static void
    readTable(String classname, String modulename, Object table[], Map cache, Map templates) {

	YoixObject  obj;
	YoixObject  temp;
	YoixObject  target = null;
	String      targetname = null;
	String      name = null;
	String      ref = null;
	String      builtin;
	Object      arg = null;
	int         modulesize = -1;
	int         command;
	int         mode;
	int         ival;
	int         n = 0;

	VM.pushMark();
	VM.pushAccess(LRWX);
	VM.pushError();

	try {
	    for (n = 0; n < table.length; n += 5) {
		obj = null;
		name = (String)table[n];
		arg = table[n+1];
		command = ((Integer)table[n+2]).intValue();
		mode = (table[n+3] != null) ? ((Integer)table[n+3]).intValue() : 0;
		ref = (String)table[n+4];
		switch (command) {
		    case ARRAY:
			if (arg != null)
			    obj = YoixObject.newArray(YoixMake.javaInt(arg));
			else obj = YoixObject.newArray();
			break;

		    case BUILTIN:
			if (name != null || ref != null) {
			    builtin = classname + "." + (name != null ? name : ref);
			    if (arg != null && arg.equals("") == false) {
				ival = YoixMake.javaInt(arg);
				obj = YoixObject.newBuiltin(builtin, Math.abs(ival), ival < 0);
			    } else obj = YoixObject.newBuiltin(builtin, 0, true);
			} else throw(new Exception());
			break;

		    case CLASS:
			if (arg instanceof String)
			    obj = YoixObject.newString((String)arg);
			else if (arg == null)
			    obj = YoixObject.newString(classname);
		 	else throw(new Exception());
			break;

		    case COLOR:
			if (arg instanceof Color)
			    obj = YoixObject.newColor((Color)arg);
			else if (arg instanceof String)
			    obj = YoixObject.newColor((String)arg);
			else if (arg == null)
			    obj = YoixObject.newColor();
			else throw(new Exception());
			break;

		    case DECLARE:
			if (arg instanceof String) {
			    if ((temp = (YoixObject)templates.get(arg)) == null) {
				temp = YoixMake.yoixType((String)arg);
				templates.put(arg, temp);
			    }
			    obj = temp.duplicate();
			} else throw(new Exception());
			break;

		    case DICTIONARY:
			if (arg != null)
			    obj = YoixObject.newDictionary(YoixMake.javaInt(arg));
			else obj = YoixObject.newDictionary();
			break;

		    case DIMENSION:
			if (arg instanceof Dimension)
			    obj = YoixObject.newDimension((Dimension)arg);
			else if (arg == null)
			    obj = YoixObject.newDimension();
			else throw(new Exception());
			break;

		    case DOUBLE:
			if (arg instanceof Double)
			    obj = YoixObject.newDouble((Double)arg);
			else obj = YoixObject.newDouble(YoixMake.javaDouble(arg));
			break;

		    case DUP:
		    case GET:
			if (arg != null) {
			    if ((temp = (YoixObject)cache.get(arg)) != null)
				obj = (command == DUP) ? temp.duplicate() : temp;
			    else throw(new Exception());
			} else throw(new Exception());
			break;

		    case GROWTO:
			if (target != null) {
			    if (arg != null) {
				ival = YoixMake.javaInt(arg);
				target.setGrowable(true);
				target.setGrowto(ival != 0 ? ival : target.length());
			    } else target.setGrowable(false);
			} else throw(new Exception());
			break;

		    case INCLUDE:
			if (name != null)
			    invokeLoader(name, cache, false);
			else throw(new Exception());
			break;

		    case INSETS:
			if (arg instanceof Insets)
			    obj = YoixObject.newInsets((Insets)arg);
			else if (arg == null)
			    obj = YoixObject.newInsets();
			else throw(new Exception());
			break;

		    case INTEGER:
			if (arg instanceof Integer)
			    obj = YoixObject.newInt((Integer)arg);
			else obj = YoixObject.newInt(YoixMake.javaInt(arg));
			break;

		    case LIST:
			obj = YoixObject.newDictionary(YoixMake.javaInt(arg), -1, false);
			break;

		    case MATRIX:
			obj = YoixMake.yoixType(T_MATRIX);
			break;

		    case MODULE:
			if (target != null && target.isDictionary()) {
			    if (name != null && name.length() > 0) {
				if (arg instanceof String) {
				    if (((String)arg).length() > 0) {
					target.reserve(name);
					((YoixBodyDictionaryObject)target.body()).setModuleClass(
					    name,
					    YOIXPACKAGE + "." + arg
					);
				    } else throw(new Exception());
				} else throw(new Exception());
			    } else throw(new Exception());
			} else throw(new Exception());
			break;

		    case NULL:
			if (arg instanceof String) {
			    if ((temp = (YoixObject)cache.get(arg)) == null) {
				obj = YoixObject.newNull((String)arg);
				mode |= (obj.mode() & ANYMASK);
			    } else obj = YoixObject.newNull(temp.major(), mapMinor(temp), (String)arg);
			} else throw(new Exception());
			break;

		    case NUMBER:
			if (arg instanceof Number)
			    obj = YoixObject.newNumber((Number)arg);
			else obj = YoixObject.newDouble(YoixMake.javaDouble(arg));
			mode |= ANYMINOR;
			break;

		    case OBJECT:
			if (arg instanceof String)
			    obj = YoixObject.newString((String)arg);
			else if (arg instanceof Number)
			    obj = YoixObject.newNumber((Number)arg);
			else if (arg instanceof Dimension)
			    obj = YoixObject.newDimension((Dimension)arg);
			else if (arg instanceof Insets)
			    obj = YoixObject.newInsets((Insets)arg);
			else if (arg instanceof Point)
			    obj = YoixObject.newPoint((Point)arg);
			else obj = YoixObject.newInt(YoixMake.javaInt(arg));
			mode |= ANYMAJOR;
			break;

		    case PACKAGE:
			if (target != null && target.isDictionary()) {
			    if (arg instanceof String)
				((YoixBodyDictionaryObject)target.body()).setModulePackage((String)arg);
			    else throw(new Exception());
			} else throw(new Exception());
			break;

		    case POINT:
			if (arg instanceof Point)
			    obj = YoixObject.newPoint((Point)arg);
			else if (arg == null)
			    obj = YoixObject.newPoint();
			else throw(new Exception());
			break;

		    case PUT:
			if (name != null || targetname != null) {
			    if (arg instanceof String) {
				if (ref == null) {
				    if (target != null) {
					if ((temp = (YoixObject)cache.get(arg)) != null) {
					    if (name == null)
						name = targetname;
					    if (temp.defined(name) == false)
						temp.declare(name, target, target.mode());
					    else temp.put(name, target, false);
					} else throw(new Exception());
				    } else throw(new Exception());
				} else throw(new Exception());
			    } else throw(new Exception());
			} else throw(new Exception());
			break;

		    case READCLASS:
			if (target != null && name != null) {
			    if (arg != null) {
				if (arg instanceof String && ((String)arg).length() > 0)
				    readClass(name, target, (String)arg, null);
				else readClass(name, target, null, arg.getClass());
			    } else readClass(name, target, null, null);
			} else throw(new Exception());
			break;

		    case REMOVE:
			if (name != null)
			    cache.remove(name);
			else throw(new Exception());
			break;

		    case RESTART:
			if (arg != null && ref == null) {
			    if ((temp = (YoixObject)cache.get(arg)) != null) {
				target = temp;
				targetname = name;
			    } else throw(new Exception());
			} else throw(new Exception());
			break;

		    case STRING:
			if (arg instanceof String)
			    obj = YoixObject.newString((String)arg);
			else if (arg instanceof Integer)
			    obj = YoixObject.newString(YoixMake.javaInt(arg));
			else if (arg == null)
			    obj = YoixObject.newString();
			else throw(new Exception());
			break;

		    case TYPEDEF:
			if (name != null || targetname != null) {
			    if (arg == null || arg instanceof String) {
				if (ref == null) {
				    if (target != null) {
					if (name == null)
					    name = targetname;
					if (target.isDictionary())	// more later - maybe
					    VM.loadTypeDefinition(target, name, (String)arg);
					else if (target.isBuiltin() && target.callable(2))
					    VM.loadTypeDefinition(target, name, (String)arg);
					else throw(new Exception());
				    } else throw(new Exception());
				} else throw(new Exception());
			    } else throw(new Exception());
			} else throw(new Exception());
			break;

		    case TYPENAME:
			if (target != null) {
			    if (arg instanceof String)
				target.setTypename((String)arg);
			    else throw(new Exception());
			} else throw(new Exception());
			break;

		    default:
			throw(new Exception());
		}

		if (obj != null) {
		    if (mode > 0xFF)
			obj.setAccessBody(mode >>> 8);
		    if (ref != null) {
			if (ref != $_THIS) {
			    if (ref.equals(modulename)) {
				if (modulesize == -1)
				    modulesize = obj.sizeof();
			    }
			    cache.put(ref, obj);
			}
			obj.setMode(mode);
			target = obj;
			targetname = name;
		    }
		    if (name != null && ref == null) {
			if (target != null) {
			    if (target.defined(name)) {
				target.put(name, obj, false);
				obj.setAccess(mode);	// added on 8/29/04
			    } else target.declare(name, obj, mode&MODEMASK);
			} else throw(new Exception());
		    }
		}
	    }
	}
	catch(YoixError e) {
	    VM.die(
		LOADERERROR,
		tableError(classname, modulename, targetname, name, arg, ref, n, e),
		null
	    );
	}
	catch(Throwable t) {
	    VM.die(
		LOADERERROR,
		tableError(classname, modulename, targetname, name, arg, ref, n, t),
		t
	    );
	}

	if (modulename != null && modulesize >= 0) {
	    if ((obj = (YoixObject)cache.get(modulename)) != null) {
		if (obj.sizeof() != modulesize) {
		    VM.warn(
			MODULESIZEERROR,
			new String[] {
			    OFFENDINGCLASS, classname,
			    "InitialSize", modulesize + "",
			    "FinalSize", obj.sizeof() + ""
			}
		    );
		}
	    }
	}

	VM.popMark();
    }


    private static String[]
    tableError(String classname, String modulename, String targetname, String name, Object arg, String ref, int index, Throwable t) {

	String  args[];
	String  tmp[];

	//
	// Quick try at providing a little more information when an error
	// occurs in readTable().
	//

	args = new String[] {
	    "Class", classname,
	    "Module", (modulename != null ? modulename : "null"),
	    "Name", (name != null ? name : "null"),
	    "Arg", (arg == null ? "null" : ((arg instanceof String) ? (String)arg : "???")),
	    "Reference", (ref != null ? ref : "null"),
	    "Index", index + "",
	    "Target", (targetname != null ? targetname : "null"),
	};

	if (t instanceof YoixError) {
	    tmp = new String[args.length + 1];
	    System.arraycopy(args, 0, tmp, 0, args.length);
	    tmp[tmp.length - 1] = ((YoixError)t).getDetails().getString(N_MESSAGE, "");
	    args = tmp;
	} else {
	    tmp = new String[args.length + 2];
	    System.arraycopy(args, 0, tmp, 0, args.length);
	    tmp[tmp.length - 2] = "Exception";
	    tmp[tmp.length - 1] =  t.getClass().getName();
	    args = tmp;
	}
	return(args);
    }
}

