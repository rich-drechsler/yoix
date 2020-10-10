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
import java.awt.print.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.*;

abstract
class YoixModuleJFC extends YoixModule

    implements YoixConstantsJFC

{

    static String  $MODULENAME = null;

    static String  $JAVAAWTCOLOR = "java.awt.Color";
    static String  $JAVAAWTCURSOR = "java.awt.Cursor";
    static String  $JAVAAWTSYSTEMCOLOR = "java.awt.SystemColor";
    static String  $JAVAAWTEVENTKEYEVENT = "java.awt.event.KeyEvent";

    static Integer  $STANDARDCURSOR = new Integer(V_STANDARD_CURSOR);
    static String   $NUM_COLORS = SystemColor.NUM_COLORS + "";

    static Object  $module[] = {
    //
    // NAME                   ARG                  COMMAND     MODE   REFERENCE
    // ----                   ---                  -------     ----   ---------
       null,                  "18",                $LIST,      $RORO, "Color",
       $JAVAAWTCOLOR,         Color.black,         $READCLASS, $LR__, null,
       null,                  null,                $GROWTO,    null,  null,

       null,                  "16",                $LIST,      $RORO, "Cursor",
       N_STANDARD_CURSOR,     $STANDARDCURSOR,     $INTEGER,   $LR__, null,
       $JAVAAWTCURSOR,        new Integer(0),      $READCLASS, $LR__, null,
       null,                  null,                $GROWTO,    null,  null,

       null,                  "185",               $LIST,      $RORO, "KeyCode",
       $JAVAAWTEVENTKEYEVENT, "VK_",               $READCLASS, $LR__, null,
       null,                  null,                $GROWTO,    null,  null,

       null,                  $NUM_COLORS,         $LIST,      $LR__, "SystemColor",
       $JAVAAWTSYSTEMCOLOR,   Color.black,         $READCLASS, $LR__, null,
       null,                  null,                $GROWTO,    null,  null,

       null,                  "",                  $BUILTIN,   $LR_X, "addColor",
       null,                  "2",                 $BUILTIN,   $LR_X, "addCursor",
       null,                  "3",                 $BUILTIN,   $LR_X, "addEventHandler",
       null,                  "3",                 $BUILTIN,   $LR_X, "addListener",
       null,                  "-2",                $BUILTIN,   $LR_X, "appendText",
       null,                  "0",                 $BUILTIN,   $LR_X, "beep",
       null,                  "-2",                $BUILTIN,   $LR_X, "deleteText",
       null,                  "2",                 $BUILTIN,   $LR_X, "distance",
       null,                  "-1",                $BUILTIN,   $LR_X, "getBestCursorSize",
       null,                  "-1",                $BUILTIN,   $LR_X, "getBrighterColor",
       null,                  "-3",                $BUILTIN,   $LR_X, "getCMYKColor",
       null,                  "1",                 $BUILTIN,   $LR_X, "getColorName",
       null,                  "-1",                $BUILTIN,   $LR_X, "getDarkerColor",
       null,                  "1",                 $BUILTIN,   $LR_X, "getFirstFocusComponent",
       null,                  "-1",                $BUILTIN,   $LR_X, "getFocusComponentAfter",
       null,                  "-1",                $BUILTIN,   $LR_X, "getFocusComponentBefore",
       null,                  "1",                 $BUILTIN,   $LR_X, "getFocusComponents",
       null,                  "0",                 $BUILTIN,   $LR_X, "getFocusOwner",
       null,                  "0",                 $BUILTIN,   $LR_X, "getFontList",
       null,                  "3",                 $BUILTIN,   $LR_X, "getHSBColor",
       null,                  "-1",                $BUILTIN,   $LR_X, "getHSBComponents",
       null,                  "1",                 $BUILTIN,   $LR_X, "getLastFocusComponent",
       null,                  "1",                 $BUILTIN,   $LR_X, "getLocationInRoot",
       null,                  "-1",                $BUILTIN,   $LR_X, "getLocationOnScreen",
       null,                  "0",                 $BUILTIN,   $LR_X, "getMaximumCursorColors",
       null,                  "-1",                $BUILTIN,   $LR_X, "getRGBColor",
       null,                  "-1",                $BUILTIN,   $LR_X, "getSaturationAdjustedColor",
       null,                  "0",                 $BUILTIN,   $LR_X, "getScreenInsets",
       null,                  "0",                 $BUILTIN,   $LR_X, "getScreenResolution",
       null,                  "0",                 $BUILTIN,   $LR_X, "getScreenSize",
       null,                  "-2",                $BUILTIN,   $LR_X, "insertText",
       null,                  "-1",                $BUILTIN,   $LR_X, "invokeLater",
       null,                  "0",                 $BUILTIN,   $LR_X, "isDispatchThread",
       null,                  "2",                 $BUILTIN,   $LR_X, "postEvent",
       null,                  "-1",                $BUILTIN,   $LR_X, "printAll",
       null,                  "2",                 $BUILTIN,   $LR_X, "removeEventHandler",
       null,                  "2",                 $BUILTIN,   $LR_X, "removeListener",
       null,                  "-4",                $BUILTIN,   $LR_X, "replaceText",
       null,                  "-1",                $BUILTIN,   $LR_X, "toBack",
       null,                  "-1",                $BUILTIN,   $LR_X, "toFront",
    };

    static Object  extracted[] = {
	// do not change this order without looking at loaded()
	"Color",
	"Cursor",
    };

    ///////////////////////////////////
    //
    // YoixModuleJFC Methods
    //
    ///////////////////////////////////

    public static YoixObject
    addColor(YoixObject arg[]) {

	YoixObject  retval = null;
	String      name = null;
	Color       color = null;
	int         comp[];
	int         n;

	if (arg.length == 0 || (arg.length == 1 && arg[0].isNull())) {
	    retval = YoixObject.newInt(YoixRegistryColor.addColor());
	} else if (arg.length == 1 && arg[0].isDictionary()) {
	    retval = YoixObject.newInt(YoixRegistryColor.addColor(arg[0]));
	} else if (arg.length == 2 || arg.length == 4) {
	    if (arg[0].isString() && arg[0].notNull()) {
		name = arg[0].stringValue();
		if (arg.length == 2 && arg[1].isString()) {
		    if (arg[1].notNull()) {
			try {
			    color = Color.decode(arg[1].stringValue());
			}
			catch(NumberFormatException e) {
			    VM.badArgument(1);
			}
		    } else VM.badArgument(1);
		} else if (arg.length == 2 && arg[1].isColor()) {
		    if (arg[1].notNull())
			color = YoixMake.javaColor(arg[1]);
		    else VM.badArgument(1);
		} else if (arg.length > 2 && arg[1].isNumber()) {
		    comp = new int[3];
		    for (n = 1; n < 4; n++) {
			if (arg[n].isNumber())
			    comp[n-1] = Math.max(Math.min(arg[n].isInteger() ? arg[n].intValue() : (int)(255*arg[n].doubleValue() + 0.5), 255), 0);
			else VM.badArgument(n);
		    }
		    color = new Color(comp[0], comp[1], comp[2]);
		} else VM.badArgument(1);
		retval = YoixObject.newInt(YoixRegistryColor.addColor(name, color));
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(retval);
    }


    public static YoixObject
    addCursor(YoixObject arg[]) {

	boolean  result = false;
	String   name;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].notNull() && (arg[1].isImage() || arg[1].isString())) {
		name = arg[0].stringValue();
		result = YoixRegistryCursor.addCursor(
		    name,
		    YoixMakeScreen.javaCursor(arg[1], name)
		);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    addEventHandler(YoixObject arg[]) {

	YoixBodyComponent  ybc;
	YoixObject         argv[];
	YoixObject         data;
	boolean            growable;
	boolean            result = false;
	String             name;
	int                bit;

	if (arg[0].isComponent() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		if (arg[2].isCallable() && arg[2].notNull()) {
		    name = arg[1].stringValue();
		    ybc = (YoixBodyComponent)(arg[0].body());
		    if ((bit = ybc.getListenerBit(name)) != 0) {
			if (bit == YoixBodyComponent.DROPTARGETLISTENER) {
			    argv = new YoixObject[] {YoixMake.yoixType(T_DROPTARGETEVENT)};
			    if (!arg[2].callable(argv))	// extra checking
				VM.badArgument(2);
			}
			data = ybc.getData();
			if (data.getObject(name) == null) {
			    growable = data.getGrowable();
			    try {
				VM.pushAccess(LRW_);
				data.setGrowable(true);
				data.putObject(name, arg[2]);
				result = ybc.activateListener(name);
			    }
			    finally {
				data.setGrowable(growable);
				VM.popAccess();
			    }
			} else data.putObject(name, arg[2]);
		    } else VM.badArgumentValue(1);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    addListener(YoixObject arg[]) {

	//
	// This was the original name, but addEventHandler() is a better
	// fit for the Yoix event model.
	//

	return(addEventHandler(arg));
    }


    public static YoixObject
    appendText(YoixObject arg[]) {

	YoixObject  src;
	YoixObject  dest;
	ArrayList   undo;
	Object      body;
	int         delta = 0;
	int         n;

	if (arg.length >= 2) {
	    if (arg[0].isComponent()) {
		if (arg[1].isString()) {
		    if (arg.length < 3 || arg[2].isNumber()) {
			if (arg.length < 4 || arg[3].isNumber()) {
			    if (arg.length < 5 || arg[4].isArrayPointer()) {
				body = arg[0].body();
				if (body instanceof YoixBodyComponent) {
				    undo = (arg.length < 5) ? null : new ArrayList(3);
				    delta = ((YoixBodyComponent)body).replaceText(
					arg.length < 3 ? Integer.MAX_VALUE : arg[2].intValue() + 1,
					0,
					arg[1].stringValue(),
					arg.length < 4 ? true : arg[3].booleanValue(),
					undo
				    );
				} else undo = null;
				if (arg.length >= 5) {
				    for (n = 5; n < arg.length; n++)
					undo.add(arg[n]);
				    src = YoixMisc.copyIntoArray(undo, undo);
				    dest = arg[4].get();
				    if (dest.notNull())
					YoixMisc.copyInto(src, dest);
				    else arg[4].put(src);
				}
			    } else VM.badArgument(4);
			} else VM.badArgument(3);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(delta));
    }


    public static YoixObject
    beep(YoixObject arg[]) {

	YoixAWTToolkit.beep();
	return(null);
    }


    public static YoixObject
    deleteText(YoixObject arg[]) {

	YoixObject  src;
	YoixObject  dest;
	ArrayList   undo;
	Object      body;
	int         delta = 0;
	int         n;

	if (arg.length >= 2) {
	    if (arg[0].isComponent()) {
		if (arg[1].isNumber()) {
		    if (arg.length < 3 || arg[2].isNumber()) {
			if (arg.length < 4 || arg[3].isNumber()) {
			    if (arg.length < 5 || arg[4].isArrayPointer()) {
				body = arg[0].body();
				if (body instanceof YoixBodyComponent) {
				    undo = (arg.length < 5) ? null : new ArrayList(3);
				    delta = ((YoixBodyComponent)body).replaceText(
					arg[1].intValue(),
					arg.length < 3 ? 1 : arg[2].intValue(),
					"",
					arg.length < 4 ? true : arg[3].booleanValue(),
					undo
				    );
				} else undo = null;
				if (arg.length >= 5) {
				    for (n = 5; n < arg.length; n++)
					undo.add(arg[n]);
				    src = YoixMisc.copyIntoArray(undo, undo);
				    dest = arg[4].get();
				    if (dest.notNull())
					YoixMisc.copyInto(src, dest);
				    else arg[4].put(src);
				}
			    } else VM.badArgument(4);
			} else VM.badArgument(3);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(delta));
    }


    public static YoixObject
    distance(YoixObject arg[]) {

	YoixObject  retval = null;

	if (arg[0].isPoint()) {
	    if (arg[0].notNull()) {
		if (arg[1].isPoint()) {
		    if (arg[1].notNull()) {
			retval = YoixObject.newDouble(YoixMakeScreen.yoixDistance(YoixMisc.distance(YoixMakeScreen.javaPoint(arg[0]), YoixMakeScreen.javaPoint(arg[1]))));
		    } else VM.badArgumentValue(1);
		} else VM.badArgument(1);
	    } else VM.badArgumentValue(0);
	} else VM.badArgument(0);

	return(retval);
    }


    public static YoixObject
    getBestCursorSize(YoixObject arg[]) {

	YoixObject  matrix = null;
	YoixObject  obj = null;
	Dimension   size = null;

	if (arg.length == 1 || arg.length == 2 || arg.length == 3) {
	    if (arg[0].isDimension()) {
		if (arg.length == 1 || arg.length == 2) {
		    if (arg.length == 1 || arg[1].isMatrix()) {
			size = YoixMakeScreen.javaDimension(arg[0]);
			if (arg.length == 2)
			    matrix = arg[1];
			else matrix = VM.getDefaultMatrix();
		    } else VM.badArgument(1);
		} else VM.badCall();
	    } else if (arg[0].isNumber()) {
		if (arg.length == 2 || arg.length == 3) {
		    if (arg[1].isNumber()) {
			if (arg.length == 2 || arg[2].isMatrix()) {
			    size = YoixMakeScreen.javaDimension(arg[0].doubleValue(), arg[1].doubleValue());
			    if (arg.length == 3)
				matrix = arg[2];
			    else matrix = VM.getDefaultMatrix();
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		} else VM.badCall();
	    } else VM.badArgument(0);
	    if (size != null) {
		if ((size = YoixAWTToolkit.getBestCursorSize(size.width, size.height)) != null) {
		    if (size.width != 0 || size.height != 0)
			obj = YoixObject.newDimension(matrix.idtransform(size.width, size.height));
		}
	    }
	} else VM.badCall();

	return(obj != null ? obj : YoixObject.newDimension());
    }


    public static YoixObject
    getBrighterColor(YoixObject arg[]) {

	Color  color = null;

	if (arg.length == 1 || arg.length == 3) {
	    if (arg.length == 3) {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    color = new Color(
				Math.max(Math.min(arg[0].isInteger() ? arg[0].intValue() : (int)(255*arg[0].doubleValue() + 0.5), 255), 0),
				Math.max(Math.min(arg[1].isInteger() ? arg[1].intValue() : (int)(255*arg[1].doubleValue() + 0.5), 255), 0),
				Math.max(Math.min(arg[2].isInteger() ? arg[2].intValue() : (int)(255*arg[2].doubleValue() + 0.5), 255), 0)
			    );
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    } else if (arg[0].isColor())
		color = YoixMake.javaColor(arg[0]);
	    else VM.badArgument(0);
	} else VM.badCall();

	return(YoixMake.yoixColor(color != null ? color.brighter() : color));
    }


    public static YoixObject
    getCMYKColor(YoixObject arg[]) {

	boolean use_adobe = true;
	Color   color = null;
	float   cyan;
	float   magenta;
	float   yellow;
	float   black;

	if (arg.length == 3 || arg.length == 4 || arg.length == 5) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			if (arg.length == 3 || arg[3].isNumber()) {
			    cyan = Math.max(Math.min(arg[0].isInteger() ? arg[0].floatValue()/255.0f : arg[0].floatValue(), 1.0f), 0.0f);
			    magenta = Math.max(Math.min(arg[1].isInteger() ? arg[1].floatValue()/255.0f : arg[1].floatValue(), 1.0f), 0.0f);
			    yellow = Math.max(Math.min(arg[2].isInteger() ? arg[2].floatValue()/255.0f : arg[2].floatValue(), 1.0f), 0.0f);
			    if (arg.length >= 4)
				black = Math.max(Math.min(arg[3].isInteger() ? arg[3].floatValue()/255.0f : arg[3].floatValue(), 1.0f), 0.0f);
			    else black = 0.0f;
			    if (arg.length > 4) {
				if (arg[4].isNumber())
				    use_adobe = arg[4].booleanValue();
				else VM.badArgument(4);
			    }
			    if (use_adobe) {	// from Adobe's PostScript Language Reference Manual
				color = new Color(
				    1.0f - Math.min(1.0f, cyan + black),
				    1.0f - Math.min(1.0f, magenta + black),
				    1.0f - Math.min(1.0f, yellow + black)
				);
			    } else {		// from wikipedia.org (CMYK entry) or comp.graphics FAQ
				color = new Color(
				    1.0f - Math.min(1.0f, cyan*(1.0f - black) + black),
				    1.0f - Math.min(1.0f, magenta*(1.0f - black) + black),
				    1.0f - Math.min(1.0f, yellow*(1.0f - black) + black)
				);
			    }
			} else VM.badArgument(3);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixMake.yoixColor(color));
    }


    public static YoixObject
    getColorName(YoixObject arg[]) {

	String  name = null;

	if (arg[0].notNull()) {
	    if (arg[0].isColor())
		name = YoixMisc.javaReverseColorLookup(YoixMake.javaColor(arg[0]));
	    else VM.badArgument(0);
	}

	return(YoixObject.newString(name));
    }


    public static YoixObject
    getDarkerColor(YoixObject arg[]) {

	Color  color = null;

	if (arg.length == 1 || arg.length == 3) {
	    if (arg.length == 3) {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    color = new Color(
				Math.max(Math.min(arg[0].isInteger() ? arg[0].intValue() : (int)(255*arg[0].doubleValue() + 0.5), 255), 0),
				Math.max(Math.min(arg[1].isInteger() ? arg[1].intValue() : (int)(255*arg[1].doubleValue() + 0.5), 255), 0),
				Math.max(Math.min(arg[2].isInteger() ? arg[2].intValue() : (int)(255*arg[2].doubleValue() + 0.5), 255), 0)
			    );
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    } else if (arg[0].isColor())
		color = YoixMake.javaColor(arg[0]);
	    else VM.badArgument(0);
	} else VM.badCall();

	return(YoixMake.yoixColor(color != null ? color.darker() : color));
    }


    public static YoixObject
    getFirstFocusComponent(YoixObject arg[]) {

	FocusTraversalPolicy  policy;
	YoixObject            obj = null;
	YoixObject            root;
	Container             container;
	Component             component;
	ArrayList             list;
	HashMap               map;
	Object                comp;

	if (arg[0].isComponent() || arg[0].isNull()) {
	    if ((root = arg[0].getObject(N_ROOT)) != null) {
		if (root.notNull()) {
		    if ((comp = arg[0].getManagedObject()) != null) {
			if (comp instanceof Component) {
			    if (comp instanceof Window)
				container = (Window)comp;
			    else container = SwingUtilities.getWindowAncestor((Component)comp);
			    if (container != null) {
				if ((policy = container.getFocusTraversalPolicy()) != null) {
				    list = new ArrayList();
				    map = new HashMap();
				    component = policy.getFirstComponent(container);
				    while (component != null && map.containsKey(component) == false) {
					map.put(component, Boolean.TRUE);
					list.add(component);
					component = policy.getComponentAfter(container, component);
				    }
				    if ((obj = getComponentManagers(list, arg[0].getObject(N_COMPONENTS))) != null)
					obj = obj.getObject(0);
				}
			    }
			}
		    }
		}
	    }
	} else VM.badArgument(0);

	return(obj != null ? obj : YoixObject.newNull());
    }


    public static YoixObject
    getFocusComponentAfter(YoixObject arg[]) {

	FocusTraversalPolicy  policy;
	YoixObject            obj = null;
	YoixObject            root;
	Container             container;
	Component             comp;
	Component             complement;
	HashMap               visited;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isComponent() || arg[0].isNull()) {
		if (arg[0].notNull()) {
		    if ((root = arg[0].getObject(N_ROOT)) != null) {	// unnecessary test
			if (arg.length == 1 || arg[1].isComponent()) {
			    if (root.notNull()) {
				if ((container = (Container)root.getManagedObject()) != null) {	// unnecessary test
				    if ((policy = container.getFocusTraversalPolicy()) != null) {
					complement = (arg.length > 1) ? (Component)arg[1].getManagedObject() : null;
					visited = new HashMap();
					for (comp = (Component)arg[0].getManagedObject(); comp != null; ) {
					    visited.put(comp, Boolean.TRUE);
					    if ((comp = policy.getComponentAfter(container, comp)) != null) {
						if (visited.containsKey(comp) == false) {
						    if (complement != null && SwingUtilities.isDescendingFrom(comp, complement))
							continue;
						} else comp = null;
						break;
					    }
					}
					obj = getComponentManager(comp, root.getObject(N_COMPONENTS));
				    }
				}
			    }
			} else VM.badArgument(1);
		    } else VM.badArgumentValue(0);
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj != null ? obj : YoixObject.newNull());
    }


    public static YoixObject
    getFocusComponentBefore(YoixObject arg[]) {

	FocusTraversalPolicy  policy;
	YoixObject            obj = null;
	YoixObject            root;
	Container             container;
	Component             comp;
	Component             complement;
	HashMap               visited;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isComponent() || arg[0].isNull()) {
		if (arg[0].notNull()) {
		    if ((root = arg[0].getObject(N_ROOT)) != null) {	// unnecessary test
			if (arg.length == 1 || arg[1].isComponent()) {
			    if (root.notNull()) {
				if ((container = (Container)root.getManagedObject()) != null) {	// unnecessary test
				    if ((policy = container.getFocusTraversalPolicy()) != null) {
					complement = (arg.length > 1) ? (Component)arg[1].getManagedObject() : null;
					visited = new HashMap();
					for (comp = (Component)arg[0].getManagedObject(); comp != null; ) {
					    visited.put(comp, Boolean.TRUE);
					    if ((comp = policy.getComponentBefore(container, comp)) != null) {
						if (visited.containsKey(comp) == false) {
						    if (complement != null && SwingUtilities.isDescendingFrom(comp, complement))
							continue;
						} else comp = null;
						break;
					    }
					}
					obj = getComponentManager(comp, root.getObject(N_COMPONENTS));
				    }
				}
			    }
			} else VM.badArgument(1);
		    } else VM.badArgumentValue(0);
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj != null ? obj : YoixObject.newNull());
    }


    public static YoixObject
    getFocusComponents(YoixObject arg[]) {

	FocusTraversalPolicy  policy;
	YoixObject            obj = null;
	YoixObject            root;
	Container             container;
	Component             component;
	ArrayList             list;
	HashMap               map;
	Object                comp;

	if (arg[0].isComponent() || arg[0].isNull()) {
	    if ((root = arg[0].getObject(N_ROOT)) != null) {
		if (root.notNull()) {
		    if ((comp = arg[0].getManagedObject()) != null) {
			if (comp instanceof Component) {
			    if (comp instanceof Window)
				container = (Window)comp;
			    else container = SwingUtilities.getWindowAncestor((Component)comp);
			    if (container != null) {
				if ((policy = container.getFocusTraversalPolicy()) != null) {
				    list = new ArrayList();
				    map = new HashMap();
				    component = policy.getFirstComponent(container);
				    while (component != null && map.containsKey(component) == false) {
					map.put(component, Boolean.TRUE);
					list.add(component);
					component = policy.getComponentAfter(container, component);
				    }
				    obj = getComponentManagers(list, root.getObject(N_COMPONENTS));
				}
			    }
			}
		    }
		}
	    }
	} else VM.badArgument(0);

	return(obj != null ? obj : YoixObject.newNull());
    }


    public static YoixObject
    getFocusOwner(YoixObject arg[]) {

	KeyboardFocusManager  manager;
	YoixBodyComponent     body;
	YoixObject            obj = null;
	Component             comp;

	if ((manager = KeyboardFocusManager.getCurrentKeyboardFocusManager()) != null) {
	    if ((comp = manager.getFocusOwner()) != null) {
		if ((body = YoixBodyComponent.findActiveWindowBody(comp)) != null)
		    obj = getComponentManager(comp, body.getData().getObject(N_COMPONENTS));
	    }
	}

	return(obj != null ? obj : YoixObject.newNull());
    }


    public static YoixObject
    getFontList(YoixObject arg[]) {

	GraphicsEnvironment  env;
	YoixObject           obj = null;

	try {
	    env = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    obj = YoixMisc.copyIntoArray(env.getAvailableFontFamilyNames());
	}
	catch(RuntimeException e) {
	    obj = YoixMisc.copyIntoArray(YoixAWTToolkit.getFontList());
	}

	return(obj == null ? YoixObject.newArray() : obj);
    }


    public static YoixObject
    getHSBColor(YoixObject arg[]) {

	Color  color = null;
	float  hue;

	if (arg[0].isNumber()) {
	    if (arg[1].isNumber()) {
		if (arg[2].isNumber()) {
		    hue = arg[0].isInteger() ? arg[0].intValue()%360 : arg[0].floatValue();
		    color = Color.getHSBColor(
			Math.max(Math.min(arg[0].isInteger() ? hue/360.0f : hue, 1.0f), 0.0f),
			Math.max(Math.min(arg[1].isInteger() ? arg[1].floatValue()/100.0f : arg[1].floatValue(), 1.0f), 0.0f),
			Math.max(Math.min(arg[2].isInteger() ? arg[2].floatValue()/100.0f : arg[2].floatValue(), 1.0f), 0.0f)
		    );
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixMake.yoixColor(color));
    }


    public static YoixObject
    getHSBComponents(YoixObject arg[]) {

	Color  color;
	float  values[] = null;

	if (arg.length == 1 || arg.length == 3) {
	    if (arg.length == 1) {
		color = YoixMake.javaColor(arg[0]);
		values = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3]);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    values = Color.RGBtoHSB(
				Math.max(Math.min(arg[0].isInteger() ? arg[0].intValue() : (int)(255*arg[0].doubleValue() + 0.5), 255), 0),
				Math.max(Math.min(arg[1].isInteger() ? arg[1].intValue() : (int)(255*arg[1].doubleValue() + 0.5), 255), 0),
				Math.max(Math.min(arg[2].isInteger() ? arg[2].intValue() : (int)(255*arg[2].doubleValue() + 0.5), 255), 0),
				new float[3]
			    );
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    }
	} else VM.badCall();

	return(YoixMisc.copyIntoArray(values));
    }


    public static YoixObject
    getLastFocusComponent(YoixObject arg[]) {

	FocusTraversalPolicy  policy;
	YoixObject            obj = null;
	Container             container;

	if (arg[0].isComponent()) {
	    if ((container = ((YoixBodyComponent)arg[0].body()).getContainer()) != null) {
		if ((policy = container.getFocusTraversalPolicy()) != null) {
		    obj = getComponentManager(
			policy.getLastComponent(container),
			arg[0].getObject(N_COMPONENTS)
		    );
		}
	    }
	} else VM.badArgument(0);

	return(obj != null ? obj : YoixObject.newNull());
    }


    public static YoixObject
    getLocationInRoot(YoixObject arg[]) {

	YoixObject  obj = null;
	YoixObject  root;
	Container   parent;
	Object      container;
	Object      comp;
	Object      body;
	Point       origin;
	Point       point;

	if (arg[0].isComponent()) {
	    if ((comp = arg[0].getManagedObject()) != null) {
		if (comp instanceof Component) {
		    if (!(comp instanceof YoixInterfaceWindow)) {
			body = arg[0].body();
			if (body instanceof YoixBodyComponent) {
			    if ((root = ((YoixBodyComponent)body).data.getObject(N_ROOT)) != null) {
				container = root.getManagedObject();
				if (container instanceof Container) {
				    if (((Container)container).isAncestorOf((Component)comp)) {
					point = ((Component)comp).getLocation();
					parent = ((Component)comp).getParent();
					while (parent != null && parent != container) {
					    origin = parent.getLocation();
					    point.translate(origin.x, origin.y);
					    parent = parent.getParent();
					}
					obj = YoixMakeScreen.yoixPoint(point);
				    }
				}
			    }
			}
		    }
		} else VM.badArgument(0);
	    }
	} else VM.badArgument(0);

	return(obj == null ? YoixObject.newPoint() : obj);
    }


    public static YoixObject
    getLocationOnScreen(YoixObject arg[]) {

	GraphicsConfiguration  gc;
	Rectangle              bounds;
	YoixObject             obj = null;
	Object                 comp;
	Point                  point;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isComponent()) {
		if (arg.length == 1 || arg[1].isScreen() || arg[1].isNull()) {
		    if ((comp = arg[0].getManagedObject()) != null) {
			if (comp instanceof Component) {
			    try {
				point = ((Component)comp).getLocationOnScreen();
				gc = (arg.length == 1 || arg[1].isNull()) ? arg[0].getGraphicsConfiguration() : arg[1].getGraphicsConfiguration();
				if (gc != null) {
				    bounds = gc.getBounds();
				    point.x -= bounds.x;
				    point.y -= bounds.y;
				}
				obj = YoixMakeScreen.yoixPoint(point);
			    }
			    catch(IllegalComponentStateException e) {}
			} else VM.badArgument(0);
		    }
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj == null ? YoixObject.newPoint() : obj);
    }


    public static YoixObject
    getMaximumCursorColors(YoixObject arg[]) {

	//
	// Not documented yet and we're not convinced that the return value
	// means much of anything (seen a 0 return on a system that supports
	// custom cursors). Eventually will investigate more - until then
	// this will remain undocumented.
	//

	return(YoixObject.newInt(YoixAWTToolkit.getMaximumCursorColors()));
    }


    public static YoixObject
    getRGBColor(YoixObject arg[]) {

	YoixObject  obj = null;
	Color       color = null;

	if (arg.length >= 1 && arg.length <= 3) {
	    if (arg.length == 3) {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    color = new Color(
				Math.max(Math.min(arg[0].isInteger() ? arg[0].intValue() : (int)(255*arg[0].doubleValue() + 0.5), 255), 0),
				Math.max(Math.min(arg[1].isInteger() ? arg[1].intValue() : (int)(255*arg[1].doubleValue() + 0.5), 255), 0),
				Math.max(Math.min(arg[2].isInteger() ? arg[2].intValue() : (int)(255*arg[2].doubleValue() + 0.5), 255), 0)
			    );
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    } else {
		if (arg[0].isString() || arg[0].isNumber() || arg[0].isColor() || arg[0].isNull()) {
		    if (arg.length == 1 || arg[1].isColor() || arg[1].isNull()) {
			if (arg[0].isString()) {
			    try {
				color = Color.decode(arg[0].stringValue());
			    }
			    catch(NumberFormatException e) {
				color = YoixRegistryColor.javaColor(arg[0].stringValue());
			    }
			} else if (arg[0].isNumber())
			    color = new Color(arg[0].intValue());
			else if (arg[0].isColor())
			    color = YoixMake.javaColor(arg[0]);
			if (color == null && arg.length == 2)
			    color = YoixMake.javaColor(arg[1]);
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    }
	} else VM.badCall();

	return(YoixMake.yoixColor(color));
    }


    public static YoixObject
    getSaturationAdjustedColor(YoixObject arg[]) {

	Color  color = null;

	if (arg.length == 2 || arg.length == 4) {
	    if (arg.length == 4) {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    if (arg[3].isNumber()) {
				color = new Color(
				    Math.max(Math.min(arg[0].isInteger() ? arg[0].intValue() : (int)(255*arg[0].doubleValue() + 0.5), 255), 0),
				    Math.max(Math.min(arg[1].isInteger() ? arg[1].intValue() : (int)(255*arg[1].doubleValue() + 0.5), 255), 0),
				    Math.max(Math.min(arg[2].isInteger() ? arg[2].intValue() : (int)(255*arg[2].doubleValue() + 0.5), 255), 0)
				);
				color = YoixMiscJFC.getSaturationAdjustedColor(color, arg[3].doubleValue());
			    } else VM.badArgument(3);
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    } else if (arg[0].isColor()) {
		if (arg[1].isNumber()) {
		    color = YoixMiscJFC.getSaturationAdjustedColor(
			YoixMake.javaColor(arg[0]),
			arg[1].doubleValue()
		    );
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixMake.yoixColor(color));
    }


    public static YoixObject
    getScreenInsets(YoixObject arg[]) {

	return(YoixObject.newInsets(YoixAWTToolkit.getScreenInsets()));
    }


    public static YoixObject
    getScreenResolution(YoixObject arg[]) {

	return(YoixObject.newInt(YoixAWTToolkit.getScreenResolution()));
    }


    public static YoixObject
    getScreenSize(YoixObject arg[]) {

	return(YoixObject.newDimension(YoixAWTToolkit.getScreenSize()));
    }


    public static YoixObject
    insertText(YoixObject arg[]) {

	YoixObject  src;
	YoixObject  dest;
	ArrayList   undo;
	Object      body;
	int         delta = 0;
	int         n;

	if (arg.length >= 2) {
	    if (arg[0].isComponent()) {
		if (arg[1].isString()) {
		    if (arg.length < 3 || arg[2].isNumber()) {
			if (arg.length < 4 || arg[3].isNumber()) {
			    if (arg.length < 5 || arg[4].isArrayPointer()) {
				body = arg[0].body();
				if (body instanceof YoixBodyComponent) {
				    undo = (arg.length < 5) ? null : new ArrayList(3);
				    delta = ((YoixBodyComponent)body).replaceText(
					arg.length < 3 ? 0 : arg[2].intValue(),
					0,
					arg[1].stringValue(),
					arg.length < 4 ? true : arg[3].booleanValue(),
					undo
				    );
				} else undo = null;
				if (arg.length >= 5) {
				    for (n = 5; n < arg.length; n++)
					undo.add(arg[n]);
				    src = YoixMisc.copyIntoArray(undo, undo);
				    dest = arg[4].get();
				    if (dest.notNull())
					YoixMisc.copyInto(src, dest);
				    else arg[4].put(src);
				}
			    } else VM.badArgument(4);
			} else VM.badArgument(3);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(delta));
    }


    public static YoixObject
    invokeLater(YoixObject arg[]) {

	YoixObject  funct;
	YoixObject  argv[];
	YoixObject  context;
	Runnable    event;
	int         argc;

	if (arg.length > 0) {
	    argc = arg.length - 1;
	    if (arg[0].isCallable() || arg[0].isCallablePointer()) {
		if (arg[0].isCallable()) {
		    funct = arg[0];
		    context = null;
		} else {
		    funct = arg[0].get();
		    context = arg[0].compound() ? arg[0] : null;
		}
		if (funct.callable(argc)) {
		    argv = new YoixObject[argc];
		    System.arraycopy(arg, 1, argv, 0, argc);
		    event = new YoixAWTInvocationEvent(funct, argv, context);
		    EventQueue.invokeLater(event);
		} else VM.badCall();
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(null);
    }


    public static YoixObject
    isDispatchThread(YoixObject arg[]) {

	return(YoixObject.newInt(EventQueue.isDispatchThread()));
    }


    public static void
    loaded() {

	//
	// A method that the Yoix loader calls after all the module
	// loading dirty work is finished. We use it here to register
	// extracted objects with the appropriate support class, but
	// it could occasionally be a useful low level debugging tool.
	//

	if (extracted[0] instanceof YoixObject)
	    YoixRegistryColor.setRegistry((YoixObject)extracted[0]);
	if (extracted[1] instanceof YoixObject)
	    YoixRegistryCursor.setRegistry((YoixObject)extracted[1]);
    }


    public static YoixObject
    postEvent(YoixObject arg[]) {

	EventQueue  queue;
	YoixObject  event;
	AWTEvent    e;
	boolean     result = false;

	//
	// Doing something like,
	//
	//	e = new YoixAWTInvocationEvent(event, arg[1])
	//
	// or maybe
	//
	//	e = new YoixAWTInvocationEvent(event.duplicate(), arg[1])
	//
	// instead of calling javaAWTEvent() might be worth investigating.
	// One reason this isn't completely correct right now is because a
	// few of the "offical" event listeners do some special things that
	// won't get done by YoixBodyComponent.handleYoixEvent(). Probably
	// not hard to change YoixBodyComponent listeners, so we might look
	// into this later.
	// 

	if (arg[0].notNull()) {
	    if (arg[1].isComponent() || arg[1].isNull()) {
		    if (arg[0].isInteger() || arg[0].isString()) {
			event = YoixObject.newDictionary(1);
			event.putObject(N_ID, arg[0]);
		    } else event = arg[0]; 
		    if ((e = YoixMakeEvent.javaAWTEvent(event, arg[1])) != null) {
			if ((queue = YoixAWTToolkit.getSystemEventQueue()) != null) {
			    queue.postEvent(e);
			    result = true;
			}
		    }
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    printAll(YoixObject arg[]) {

	Object  comp;
	double  margin;

	//
	// Revised to recognize Printable objects (probably only one
	// right now), but there's much more that could be done. The
	// best solution might be to add a print() Yoix function to
	// Graphics objects that would replace calls to this builtin.
	//

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isComponent()) {
		comp = arg[0].getManagedObject();
		if (comp instanceof Printable || comp instanceof Frame) {
		    if (arg.length == 1 || arg[1].isNumber()) {
			margin = (arg.length == 2) ? arg[1].doubleValue() : Double.NaN;
			if (comp instanceof Printable)
			    printPrintable((Printable)comp, margin);
			else printFrame((Frame)comp, margin);
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(null);
    }


    public static YoixObject
    removeEventHandler(YoixObject arg[]) {

	YoixBodyComponent  ybc;
	YoixObject         data;
	String             name;
	int                bit = 0;

	if (arg[0].isComponent() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		name = arg[1].stringValue();
		ybc = (YoixBodyComponent)(arg[0].body());
		if ((bit = ybc.getListenerBit(name)) != 0) {
		    data = ybc.getData();
		    if (data.getObject(name) != null)
			data.putObject(name, YoixObject.newNull());
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(bit != 0));
    }


    public static YoixObject
    removeListener(YoixObject arg[]) {

	//
	// This was the original name, but removeEventHandler() is a better
	// fit for the Yoix event model.
	//

	return(removeEventHandler(arg));
    }


    public static YoixObject
    replaceText(YoixObject arg[]) {

	YoixObject  src;
	YoixObject  dest;
	ArrayList   undo;
	Object      body;
	int         delta = 0;
	int         n;

	if (arg.length >= 4) {
	    if (arg[0].isComponent()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			if (arg[3].isString()) {
			    if (arg.length < 5 || arg[4].isNumber()) {
				if (arg.length < 6 || arg[5].isArrayPointer()) {
				    body = arg[0].body();
				    if (body instanceof YoixBodyComponent) {
					undo = (arg.length < 6) ? null : new ArrayList(3);
					delta = ((YoixBodyComponent)body).replaceText(
					    arg[1].intValue(),
					    arg[2].intValue(),
					    arg[3].stringValue(),
					    arg.length < 5 ? true : arg[4].booleanValue(),
					    undo
					);
				    } else undo = null;
				    if (arg.length >= 6) {
					for (n = 6; n < arg.length; n++)
					    undo.add(arg[n]);
					src = YoixMisc.copyIntoArray(undo, undo);
					dest = arg[5].get();
					if (dest.notNull())
					    YoixMisc.copyInto(src, dest);
					else arg[5].put(src);
				    }
				} else VM.badArgument(5);
			    } else VM.badArgument(4);
			} else VM.badArgument(3);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(delta));
    }


    public static YoixObject
    toBack(YoixObject arg[]) {

	Object  comp;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isComponent() || arg[0].isNull()) {
		if (arg[0].notNull()) {
		    if ((comp = arg[0].getManagedObject()) != null) {
			if (comp instanceof YoixInterfaceWindow) {
			    if (arg.length == 1 || arg[1].isNumber()) {
				((YoixInterfaceWindow)comp).toBack();
				if (arg.length == 2 && arg[1].booleanValue())
				    ((YoixBodyComponent)arg[0].body()).childrenStack(false);
			    } else VM.badArgument(1);
			} else VM.badArgument(0);
		    }
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(null);
    }


    public static YoixObject
    toFront(YoixObject arg[]) {

	Object  comp;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isComponent() || arg[0].isNull()) {
		if (arg[0].notNull()) {
		    if ((comp = arg[0].getManagedObject()) != null) {
			if (comp instanceof YoixInterfaceWindow) {
			    if (arg.length == 1 || arg[1].isNumber()) {
				((YoixInterfaceWindow)comp).toFront();
				if (arg.length == 2 && arg[1].booleanValue())
				    ((YoixBodyComponent)arg[0].body()).childrenStack(true);
			    } else VM.badArgument(1);
			} else VM.badArgument(0);
		    }
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(null);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static YoixObject
    getComponentManager(Component comp, YoixObject components) {

	YoixObject  manager = null;
	YoixObject  obj;
	int         n;

	if (comp != null && components != null) {
	    if (components.notNull()) {
		for (n = 0; n < components.sizeof(); n++) {
		    if ((obj = components.getObject(n)) != null) {
			if (obj.getManagedObject() == comp) {
			    manager = obj;
			    break;
			}
		    }
		}
	    }
	}
	return(manager);
    }


    private static YoixObject
    getComponentManagers(ArrayList list, YoixObject components) {

	YoixObject  array = null;
	YoixObject  obj;
	Iterator    iterator;
	HashMap     map;
	Object      comp;
	int         index;
	int         n;

	if (list.size() > 0 && components != null) {
	    map = new HashMap();
	    for (n = 0; n < components.sizeof(); n++) {
		if ((obj = components.getObject(n)) != null) {
		    if ((comp = obj.getManagedObject()) != null)
			map.put(comp, obj);
		}
	    }
	    if (map.size() > 0) {
		array = YoixObject.newArray(0, -1);
		index = 0;
		for (iterator = list.iterator(); iterator.hasNext(); ) {
		    comp = iterator.next();
		    if (map.containsKey(comp))
			array.putObject(index++, (YoixObject)map.get(comp));
		}
		if (index > 0)
		    array.setGrowable(false);
		else array = null;
	    }
	}
	return(array);
    }


    private static void
    printFrame(Frame frame, double margin) {

	PrintJob  p = null;
	Graphics  g = null;

	try {
	    if ((p = YoixAWTToolkit.getPrintJob(frame, null, null)) != null) {
		if ((g = p.getGraphics()) != null)
		    frame.printAll(g);
	    }
	}

	finally {
	    if (p != null) {
		if (g != null)
		    g.dispose();
		p.end();
	    }
	}
    }


    private static void
    printPrintable(Printable comp, double margin) {

	PrinterJob  pj;
	PageFormat  pf;
	double      x;
	double      y;
	double      ulx;
	double      uly;
	double      lrx;
	double      lry;
	Paper       paper;

	//
	// Java's 1 inch margin seems very conservative! Our experience,
	// although dated, says that a 1/4 or slightly less should work
	// on most printers, so that's what we use as our default.
	//

	if ((pj = PrinterJob.getPrinterJob()) != null) {
	    if (Double.isNaN(margin) == false) {
		pf = new PageFormat();
		paper = pf.getPaper();
		x = paper.getImageableX();
		y = paper.getImageableY();
		ulx = Math.min(x, margin);
		uly = Math.min(y, margin);
		lrx = Math.max(x + paper.getImageableWidth(), paper.getWidth() - margin);
		lry = Math.max(y + paper.getImageableHeight(), paper.getHeight() - margin);
		paper.setImageableArea(ulx, uly, lrx - ulx, lry - uly);
		pf.setPaper(paper);
		pj.setPrintable(comp, pf);
	    } else pj.setPrintable(comp);
	    if (pj.printDialog()) {
		try {
		    pj.print();
		}
		catch(PrinterException e) {}
	    }
	}
    }
}

