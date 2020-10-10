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
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.UIManager.*;

final
class YoixBodyUIManager extends YoixPointerActive

{

    //
    // The activefields Hashtable translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(20);

    static {
	activefields.put(N_CONTAINS, new Integer(V_CONTAINS));
	activefields.put(N_CROSSPLATFORMNAME, new Integer(V_CROSSPLATFORMNAME));
	activefields.put(N_GET, new Integer(V_GET));
	activefields.put(N_LOOKANDFEEL, new Integer(V_LOOKANDFEEL));
	activefields.put(N_LOOKANDFEELNAMES, new Integer(V_LOOKANDFEELNAMES));
	activefields.put(N_NATIVENAME, new Integer(V_NATIVENAME));
	activefields.put(N_PROPERTIES, new Integer(V_PROPERTIES));
	activefields.put(N_PUT, new Integer(V_PUT));
	activefields.put(N_RESET, new Integer(V_RESET));
	activefields.put(N_THEME, new Integer(V_THEME));
    }

    //
    // Far as we can tell themes, which were introduced in Java 1.5, are
    // only supported by the metal look and feel, and unfortunately Java
    // currently doesn't provide a way to set the theme using its name.
    // As a temporary solution we provide a small HashMap that maps some
    // standard (and maybe custom) theme names to class names that can be
    // used when we try to call setCurrentTheme(). Users can always store
    // the Java class name of the theme (if they know it) in the "theme"
    // field, so the HashMap is really just a convenience.
    //

    private static HashMap  metalthemes = new HashMap();

    static {
	metalthemes.put("Steel", "javax.swing.plaf.metal.DefaultMetalTheme");
	metalthemes.put("Ocean", "javax.swing.plaf.metal.OceanTheme");
    }

    //
    // We use a HashMap to remember the LookAndFeels that we've updated,
    // which currently means their fonts have been adjusted based on the
    // command line magnification and platform dependent scaling that's
    // calculated by VM.buildScreen(). The font adjustment is done once
    // per LookAndFeel, so changing VM.fontmagnification in Yoix scripts,
    // rather than on the command line via the -m option, doesn't affect
    // UIManager fonts. In other words supported LookAndFeels all behave
    // as if they were created with adjusted fonts when the interpreter
    // first started.
    //

    private static HashMap  updated = new HashMap();

    //
    // Establish initial custom UIMananger settings - there should not be
    // many of them!!!
    //

    static {
	UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);
    }

    //
    // Turns out several things had to be forced into the event thread to
    // avoid a possible deadlock that was introduced in our implementation
    // of YoixBodyComponent.updateUI(). Before threadsafe support was added
    // the old version grabbed the AWT tree lock in an attempt to prevent an
    // an occasional NullPointerException and that could cause problems if
    // we called updateUI() from outside the event thread. We eliminated the
    // use of the AWT tree lock in updateUI(), since we now only call it from
    // the event thread.
    //
    // NOTE - our implementation should cover the problem areas, but it's not
    // hard to imagine better solutions. For example, maybe updateUI() itself
    // should do this or perhaps it should only be done by the setLookAndFeel()
    // that calls updateUI(). Anyway, we're going to leave things be for now.
    //

    private static final int  RUN_SETLOOKANDFEEL = 1;
    private static final int  RUN_SETTHEME = 2;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyUIManager(YoixObject data) {

	super(data, true);	// tells super we're cloneable!!
	updateUIManager();
	buildUIManager();
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(UIMANAGER);
    }

    ///////////////////////////////////
    //
    // YoixBodyUIManager Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_CONTAINS:
		obj = builtinContains(name, argv);
		break;

	    case V_GET:
		obj = builtinGet(name, argv);
		break;

	    case V_PUT:
		obj = builtinPut(name, argv);
		break;

	    case V_RESET:
		obj = builtinReset(name, argv);
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
	    case V_CROSSPLATFORMNAME:
		obj = getCrossPlatformName();
		break;

	    case V_LOOKANDFEEL:
		obj = getLookAndFeel();
		break;

	    case V_LOOKANDFEELNAMES:
		obj = getLookAndFeelNames();
		break;

	    case V_NATIVENAME:
		obj = getNativeName();
		break;

	    case V_PROPERTIES:
		obj = getProperties();
		break;

	    case V_THEME:
		obj = getTheme();
		break;

	    default:
		break;
	}
	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(null);
    }


    public final void
    handleRun(Object args[]) {

	if (args != null && args.length > 0) {
	    switch (((Integer)args[0]).intValue()) {
		case RUN_SETLOOKANDFEEL:
		    handleSetLookAndFeel((YoixObject)args[1]);
		    break;

		case RUN_SETTHEME:
		    handleSetTheme((YoixObject)args[1]);
		    break;
	    }
	}
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	int  mode;

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_LOOKANDFEEL:
		    setLookAndFeel(obj);
		    break;

		case V_PROPERTIES:
		    setProperties(obj);
		    break;

		case V_THEME:
		    setTheme(obj);
		    break;

		default:
		    break;
	    }
	}
	return(obj);
    }


    static void
    updateUIManager() {

	LookAndFeel  lookfeel;
	double       factor;

	//
	// The second try is a kludge that probably is only needed by the
	// GTK look and feel on Linux? The initialize() call seems to help
	// eliminate the NullPointerException mentioned in the comments in
	// updateLookAndFeel(), so test the GTK+ model before tossing the
	// kludge.
	//

	if ((lookfeel = UIManager.getLookAndFeel()) != null) {
	    factor = getScaleFactor(lookfeel);
	    if (updateLookAndFeel(lookfeel, factor) == false) {
		lookfeel.initialize();
		updateLookAndFeel(lookfeel, factor);
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildUIManager() {

	setField(N_LOOKANDFEEL);
	setField(N_THEME);
	setField(N_PROPERTIES);
    }


    private YoixObject
    builtinContains(String name, YoixObject arg[]) {

	boolean  result = false;

	if (arg.length == 1) {
	    if (arg[0].isString())
		result = (getYoixObject(arg[0].stringValue()) != null);
	    else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private YoixObject
    builtinGet(String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isString()) {
		obj = getYoixObject(
		    arg[0].stringValue(),
		    arg.length > 1 ? arg[1] : null
		);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(obj == null ? YoixObject.newNull() : obj);
    }


    private YoixObject
    builtinPut(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	String      key;

	if (arg.length == 2) {
	    if (arg[0].isString()) {
		key = arg[0].stringValue();
		obj = getYoixObject(key);
		UIManager.put(key, YoixMake.javaObject(arg[1]));
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(obj == null ? YoixObject.newNull() : obj);
    }


    private YoixObject
    builtinReset(String name, YoixObject arg[]) {

	Enumeration  enm;
	UIDefaults   defaults;
	Object       key;
	Object       value;
	double       scale;
	int          count = 0;
	int          n;

	//
	// Could easily accept several string arguments, which would let
	// users reset several fields (rather than just one or all) in a
	// single call - maybe later.
	//

	if ((defaults = UIManager.getLookAndFeelDefaults()) != null) {
	    scale = VM.getInitialFontMagnification();
	    if (arg.length == 0) {
		for (enm = defaults.keys(); enm.hasMoreElements(); ) {
		    key = enm.nextElement();
		    value = defaults.get(key);
		    if (UIManager.get(key) != value) {
			if (value instanceof Font) {
			    value = scaleFont((Font)value, scale);
			    if (value.equals(UIManager.get(key)) == false) {
				UIManager.put(key, value);
				count++;
			    }
			} else {
			    UIManager.put(key, value);
			    count++;
			}
		    }
		}
	    } else {
		for (n = 0; n < arg.length; n++) {
		    if (arg[n].notString() && arg[n].notNull())
			VM.badArgument(name, n);
		}
		for (n = 0; n < arg.length; n++) {
		    if (arg[n].notNull()) {
			key = arg[n].stringValue();
			if (defaults.contains(key)) {
			    value = defaults.get(key);
			    if (UIManager.get(key) != value) {
				if (value instanceof Font) {
				    value = scaleFont((Font)value, scale);
				    if (value.equals(UIManager.get(key)) == false) {
					UIManager.put(key, value);
					count++;
				    }
				} else {
				    UIManager.put(key, value);
				    count++;
				}
			    }
			}
		    }
		}
	    }
	}

	return(YoixObject.newInt(count));
    }


    private YoixObject
    getCrossPlatformName() {

	return(YoixObject.newString(mapClassName(UIManager.getCrossPlatformLookAndFeelClassName())));
    }


    private YoixObject
    getLookAndFeel() {

	LookAndFeel  lookfeel;
	String       name = null;

	if ((lookfeel = UIManager.getLookAndFeel()) != null)
	    name = lookfeel.getName();
	return(YoixObject.newString(name));
    }


    private YoixObject
    getLookAndFeelNames() {

	LookAndFeelInfo  info[];
	LookAndFeel      lookfeel;
	YoixObject       obj = null;
	int              count;
	int              n;
	int              m;

	if ((info = UIManager.getInstalledLookAndFeels()) != null) {
	    for (n = 0, count = 0; n < info.length; n++) {
		if ((lookfeel = (LookAndFeel)YoixReflect.newInstance(info[n].getClassName())) != null) {
		    if (lookfeel.isSupportedLookAndFeel())
			count++;
		    else info[n] = null;
		} else info[n] = null;
	    }
	    if (count > 0) {
		obj = YoixObject.newArray(count);
		for (n = 0, m = 0; n < info.length; n++) {
		    if (info[n] != null)
			obj.putString(m++, info[n].getName());
		}
	    }
	}
	return(obj != null ? obj : YoixObject.newArray());
    }


    private YoixObject
    getNativeName() {

	String  name;
	String  javaname;

	if ((name = mapClassName(UIManager.getSystemLookAndFeelClassName())) != null) {
	    if ((javaname = UIManager.getCrossPlatformLookAndFeelClassName()) != null) {
		if (name.equals(javaname))
		    name = null;
	    }
	}
	return(YoixObject.newString(name));
    }


    private YoixObject
    getProperties() {

	Enumeration  enm;
	YoixObject   obj;
	Hashtable    src;
	Hashtable    dest;
	Object       key;

	if ((src = UIManager.getDefaults()) != null) {
	    dest = new Hashtable(src.size());
	    for (enm = src.keys(); enm.hasMoreElements(); ) {
		key = enm.nextElement();
		if (key instanceof String) {
		    if ((obj = getYoixObject(key)) != null)
			dest.put(key, obj);
		}
	    }
	} else dest = null;

	return(dest != null ? YoixMisc.copyIntoDictionary(dest) : YoixObject.newDictionary());
    }


    private static double
    getScaleFactor(LookAndFeel lookfeel) {

	String  name;
	double  scale;
	double  factor = 1.0;

	//
	// Another kludge for the GTK theme, probably just on Linux. Our
	// testing seemed to show that font sizes were correct and didn't
	// need any special scaling when the GTK theme was used on Linux.
	// Anyway, we return the inverse of the interpreter's fontscale,
	// which can be used to recover the font magnification that was
	// set on the command line. Incidentally, we also noticed that
	// the GTK theme didn't seem to use UIMananger listed fonts, so
	// this kludge probably won't affect GUI components but it should
	// make fonts stroed by the UIManager reasonably consistent.
	//

	if (lookfeel != null) {
	    if ((name = lookfeel.getName()) != null) {
		if (name.startsWith("GTK")) {
		    if ((scale = VM.getFontScale()) != 0.0)
			factor = 1.0/scale;
		}
	    }
	}
	return(factor);
    }


    private YoixObject
    getTheme() {

	LookAndFeel  lookfeel;
	Object       theme = null;

	if ((lookfeel = UIManager.getLookAndFeel()) != null) {
	    theme = YoixReflect.invoke(
		YoixReflect.invoke(lookfeel, "getCurrentTheme"),
		"getName"
	    );
	}
	return(theme instanceof String ? YoixObject.newString((String)theme) : YoixObject.newString());
    }


    private YoixObject
    getYoixObject(Object key) {

	return(getYoixObject(key, null));
    }


    private YoixObject
    getYoixObject(Object key, YoixObject result) {

	YoixObject  obj;
	Object      value;

	if ((value = UIManager.get(key)) != null) {
	    if ((obj = YoixMake.yoixObject(value)) == null) {
		if (result == null) {
		    if (value instanceof InputMap)
			result = YoixObject.newNull(T_DICT);
		    else result = YoixObject.newNull();
		}
	    } else result = obj;
	}
	return(result);
    }


    private void
    handleSetLookAndFeel(YoixObject obj) {

	LookAndFeelInfo  info[];
	LookAndFeel      lookfeel;
	String           name;
	int              n;

	if (obj.notNull()) {
	    if ((lookfeel = UIManager.getLookAndFeel()) != null) {
		name = obj.stringValue();
		if (name.equalsIgnoreCase(lookfeel.getName()) == false) {
		    if ((info = UIManager.getInstalledLookAndFeels()) != null) {
			for (n = 0; n < info.length; n++) {
			    if (name.equalsIgnoreCase(info[n].getName())) {
				setLookAndFeel(info[n].getClassName());
				break;
			    }
			}
		    }
		}
	    }
	}
    }


    private void
    handleSetTheme(YoixObject obj) {

	LookAndFeel  lookfeel;
	UIDefaults   defaults;
	String       classname;
	String       themename;
	Object       theme;
	Object       result;
	Insets       insets;
	Class        type;

	if ((lookfeel = UIManager.getLookAndFeel()) != null) {
	    themename = obj.stringValue();
	    if ((classname = (String)metalthemes.get(themename)) == null)
		classname = themename;
	    if ((theme = YoixReflect.newInstance(classname)) != null) {
		for (type = theme.getClass(); type != null; type = type.getSuperclass()) {
		    result = YoixReflect.invoke(
			lookfeel,
			"setCurrentTheme",
			new Object[] {theme},
			new Class[] {type},
			YoixReflect.REFLECTION_ERROR
		    );
		    if (result == null) {
			setLookAndFeel(lookfeel);
			break;
		    }
		}
	    }
	}
    }


    private String
    mapClassName(String classname) {

	LookAndFeelInfo  info[];
	String           name = null;
	int              n;

	if (classname != null) {
	    if ((info = UIManager.getInstalledLookAndFeels()) != null) {
		for (n = 0; n < info.length; n++) {
		    if (classname.equals(info[n].getClassName())) {
			name = info[n].getName();
			break;
		    }
		}
	    }
	}
	return(name);
    }


    private static Font
    scaleFont(Font font, double scale) {

	int  size;

	if (scale != 1.0 && scale > 0.0) {
	    size = (int)(font.getSize()*scale + 0.5);
	    font = new Font(font.getName(), font.getStyle(), size);
	}
	return(font);
    }


    private void
    setLookAndFeel(YoixObject obj) {

	//
	// Seems like the event queue stuff might just belong in the method
	// that calls YoixBodyComponent.updateUI(), since that's currently
	// where problems can happen. We may addess this later on.
	//

	if (EventQueue.isDispatchThread() == false) {
	    EventQueue.invokeLater(
		new YoixAWTInvocationEvent(
		    this,
		    new Object[] {new Integer(RUN_SETLOOKANDFEEL), obj}
		)
	    );
	} else handleSetLookAndFeel(obj);
    }


    private void
    setLookAndFeel(String classname) {

	Object  lookfeel;

	if ((lookfeel = YoixReflect.newInstance(classname)) != null) {
	    if (lookfeel instanceof LookAndFeel)
		setLookAndFeel((LookAndFeel)lookfeel);
	}
    }


    private void
    setLookAndFeel(LookAndFeel lookfeel) {

	double  factor;

	synchronized(updated) {
	    factor = getScaleFactor(lookfeel);
	    if (updateLookAndFeel(lookfeel, factor) == false) {
		lookfeel.initialize();
		updateLookAndFeel(lookfeel, factor);
	    }
	    try {
		UIManager.setLookAndFeel(lookfeel);
		YoixBodyComponent.updateUI();
	    }
	    catch(UnsupportedLookAndFeelException e) {}
	}
    }


    private void
    setProperties(YoixObject obj) {

	String  key;
	int     length;
	int     n;

	if (obj.isDictionary()) {
	    length = obj.length();
	    for (n = obj.offset(); n < length; n++) {
		if ((key = obj.name(n)) != null)
		    UIManager.put(key, YoixMake.javaObject(obj.getObject(n)));
	    }
	}
    }


    private void
    setTheme(YoixObject obj) {

	//
	// Seems like the event queue stuff might just belong in the method
	// that calls YoixBodyComponent.updateUI(), since that's currently
	// where problems can happen. We may addess this later on.
	//

	if (EventQueue.isDispatchThread() == false) {
	    EventQueue.invokeLater(
		new YoixAWTInvocationEvent(
		    this,
		    new Object[] {new Integer(RUN_SETTHEME), obj}
		)
	    );
	} else handleSetTheme(obj);
    }


    private static boolean
    updateLookAndFeel(LookAndFeel lookfeel, double factor) {

	Enumeration  enm;
	UIDefaults   defaults;
	Object       key;
	double       scale;
	Font         font;
	int          size;

	if (lookfeel != null && updated.containsKey(lookfeel) == false) {
	    synchronized(updated) {
		if (updated.containsKey(lookfeel) == false) {	// just in case
		    try {
			//
			// The GTK look and feel, which appeared in Java 1.5,
			// misbehaved when getDefaults() was called too early.
			// Calling
			//
			//	lookfeel.initialize()
			//
			// eliminated the exception, however there are other
			// issues with GTK that are troubling. For example,
			// Linux fonts in the other models need scaling, but
			// that doesn't appear to be the case for GTK?? Also
			// seemed like the fonts, or at least the font sizes,
			// were correct and didn't need scaling, however it
			// also seemed like the GTK look and feel completely
			// ignored UIManager fonts - definitely confusing!!
			//
			if ((scale = VM.getInitialFontMagnification()) != 1.0 && scale > 0.0) {
			    if ((defaults = lookfeel.getDefaults()) != null) {
				//
				// We're only interested in fonts and really
				// don't want to the force evaluation of all
				// LazyValues, because some of them could take
				// a while. Decided to just look for "font" in
				// any case at the end of key.
				//
				scale *= factor;
				for (enm = defaults.keys(); enm.hasMoreElements(); ) {
				    key = enm.nextElement();
				    if (key instanceof String) {
					if (((String)key).toLowerCase().endsWith("font")) {
					    if ((font = defaults.getFont(key)) != null)
						UIManager.put(key, scaleFont(font, scale));
					}
				    }
				}
			    }
			}
			updated.put(lookfeel, Boolean.TRUE);
		    }
		    catch(NullPointerException e) {}
		}
	    }
	}
	return(updated.containsKey(lookfeel));
    }
}

