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
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

abstract
class YoixMiscMenu

    implements YoixConstants,
	       YoixConstantsSwing

{

    //
    // Recently combined AWT and Swing menu support. It's definitely an
    // improvement, but there's still more that could be done.
    //
    // In another recent change we made setNewProperty() accessible from
    // other classes and now use it in YoixBodyComponentSwing.java when
    // we need to sync menu properties.
    //

    ///////////////////////////////////
    //
    // YoixMiscMenu Methods
    //
    ///////////////////////////////////

    static void
    addListener(Object comp, YoixInterfaceListener listener) {

	if (comp != null) {
	    synchronized(comp) {
		if (comp instanceof MenuBar)
		    addListener((MenuBar)comp, listener);
		else if (comp instanceof PopupMenu)
		    addListener((Menu)comp, listener);
		else if (comp instanceof JMenuBar)
		    addListener((JMenuBar)comp, listener);
		else if (comp instanceof JPopupMenu)
		    addListener((JPopupMenu)comp, listener);
	    }
	}
    }


    static MenuBar
    buildMenuBar(YoixObject layout) {

	return(buildMenuBar(layout, new MenuBar(), false));
    }


    static MenuBar
    buildMenuBar(YoixObject layout, MenuBar menubar, boolean reset) {

	YoixObject  name;
	YoixObject  arg;
	HashMap     groups;
	Menu        menu;
	int         length;
	int         incr;
	int         n;

	if (layout != null && menubar != null) {
	    if (layout.isArray() || layout.isMenu() || layout.isNull()) {
		synchronized(menubar) {
		    groups = new HashMap();
		    if (reset)
			dispose(menubar);
		    length = layout.length();
		    for (n = 0; n < length; n += incr) {
			incr = 2;
			arg = layout.get(n, false);
			if (arg.isString() && arg.notNull()) {
			    name = arg;
			    arg = layout.get(n + 1, false);
			    if (arg.isMenu() || arg.isArray() || arg.isNull()) {
				menu = buildAWTMenu(new Menu(name.stringValue()), arg, groups);
				menubar.add(menu);
				if (name.stringValue().equals("Help"))
				    menubar.setHelpMenu(menu);
			    } else VM.abort(BADMENUITEM, n + 1);
			} else if (arg.isArray() || arg.isMenu() || arg.isNull()) {
			    incr = 1;
			    if (arg.notNull())
				buildMenuBar(arg, menubar, false);
			} else VM.abort(BADMENUITEM, n);
		    }
		}
	    } else VM.abort(TYPECHECK);
	}
	return(menubar);
    }


    static PopupMenu
    buildPopupMenu(YoixObject layout) {

	return(buildPopupMenu(layout, new PopupMenu(null), false));
    }


    static PopupMenu
    buildPopupMenu(YoixObject layout, YoixObject label) {

	PopupMenu  popup;

	if (label == null || label.isNull())
	    popup = new PopupMenu(null);
	else popup = new PopupMenu(label.stringValue());

	return(buildPopupMenu(layout, popup, false));
    }


    static PopupMenu
    buildPopupMenu(YoixObject layout, PopupMenu popup, boolean reset) {

	if (layout != null && popup != null) {
	    if (layout.isArray() || layout.isMenu() || layout.isNull()) {
		synchronized(popup) {
		    if (reset)
			dispose(popup);
		    buildAWTMenu(popup, layout, new HashMap());
		}
	    } else VM.abort(TYPECHECK);
	}
	return(popup);
    }


    static YoixObject
    buildSwingMenuArray(YoixObject layout) {

	return(buildSwingMenuArray(layout, new HashMap()));
    }


    static void
    dispose(Object comp) {

	if (comp != null) {
	    synchronized(comp) {
		if (comp instanceof MenuBar)
		    dispose((MenuBar)comp);
		else if (comp instanceof PopupMenu)
		    dispose((Menu)comp);
		//
		// The rest currently aren't needed...
		//
	    }
	}
    }


    static int
    getMenuItemEnabled(Object comp, Object pattern) {

	int  result = -1;

	if (comp != null && pattern != null) {
	    synchronized(comp) {
		if (comp instanceof MenuBar)
		    result = getMenuItemEnabled((MenuBar)comp, pattern);
		else if (comp instanceof PopupMenu)
		    result = getMenuItemEnabled((Menu)comp, pattern);
		else if (comp instanceof JMenuBar)
		    result = getMenuItemEnabled((JMenuBar)comp, pattern);
		else if (comp instanceof JPopupMenu)
		    result = getMenuItemEnabled((JPopupMenu)comp, pattern);
		else if (comp instanceof JMenu)
		    result = getMenuItemEnabled((JMenu)comp, pattern);
	    }
	}
	return(result);
    }


    static int
    getMenuItemState(Object comp, Object pattern) {

	int  result = -1;

	if (comp != null && pattern != null) {
	    synchronized(comp) {
		if (comp instanceof MenuBar)
		    result = getMenuItemState((MenuBar)comp, pattern);
		else if (comp instanceof PopupMenu)
		    result = getMenuItemState((Menu)comp, pattern);
		else if (comp instanceof JMenuBar)
		    result = getMenuItemState((JMenuBar)comp, pattern);
		else if (comp instanceof JPopupMenu)
		    result = getMenuItemState((JPopupMenu)comp, pattern);
		else if (comp instanceof JMenu)
		    result = getMenuItemState((JMenu)comp, pattern);
	    }
	}
	return(result);
    }


    static void
    removeListener(Object comp, YoixInterfaceListener listener) {

	if (comp != null) {
	    synchronized(comp) {
		if (comp instanceof MenuBar)
		    removeListener((MenuBar)comp, listener);
		else if (comp instanceof PopupMenu)
		    removeListener((Menu)comp, listener);
		else if (comp instanceof JMenuBar)
		    removeListener((JMenuBar)comp, listener);
		else if (comp instanceof JPopupMenu)
		    removeListener((JPopupMenu)comp, listener);
	    }
	}
    }


    static int
    setMenuItemEnabled(Object comp, Object pattern, boolean state) {

	int  result = -1;

	if (comp != null && pattern != null) {
	    synchronized(comp) {
		if (comp instanceof MenuBar)
		    result = setMenuItemEnabled((MenuBar)comp, pattern, state);
		else if (comp instanceof PopupMenu)
		    result = setMenuItemEnabled((Menu)comp, pattern, state);
		else if (comp instanceof JMenuBar)
		    result = setMenuItemEnabled((JMenuBar)comp, pattern, state);
		else if (comp instanceof JPopupMenu)
		    result = setMenuItemEnabled((JPopupMenu)comp, pattern, state);
		else if (comp instanceof JMenu)
		    result = setMenuItemEnabled((JMenu)comp, pattern, state);
	    }
	}
	return(result);
    }


    static int
    setMenuItemState(Object comp, Object pattern, boolean state) {

	int  result = -1;

	if (comp != null && pattern != null) {
	    synchronized(comp) {
		if (comp instanceof MenuBar)
		    result = setMenuItemState((MenuBar)comp, pattern, state);
		else if (comp instanceof PopupMenu)
		    result = setMenuItemState((Menu)comp, pattern, state);
		else if (comp instanceof JMenuBar)
		    result = setMenuItemState((JMenuBar)comp, pattern, state);
		else if (comp instanceof JPopupMenu)
		    result = setMenuItemState((JPopupMenu)comp, pattern, state);
		else if (comp instanceof JMenu)
		    result = setMenuItemState((JMenu)comp, pattern, state);
	    }
	}
	return(result);
    }


    static void
    setMenuProperty(Object comp, String name, Object value) {

	if (comp != null && name != null && value != null) {
	    synchronized(comp) {
		if (comp instanceof MenuBar)
		    setMenuProperty((MenuBar)comp, name, value);
		else if (comp instanceof PopupMenu)
		    setMenuProperty((Menu)comp, name, value);
		else if (comp instanceof JMenuBar)
		    setMenuProperty((JMenuBar)comp, name, value);
		else if (comp instanceof JPopupMenu)
		    setMenuProperty((JPopupMenu)comp, name, value);
		else if (comp instanceof JMenu)
		    setMenuProperty((JMenu)comp, name, value);
	    }
	}
    }


    static void
    setNewProperty(Object item, String name, Object value) {

	if (item != null && name != null && value != null) {
	    if (item instanceof Component) {
		if (name.equals(N_OPAQUE) && value instanceof Boolean) {
		    if (item instanceof JComponent)
			((JComponent)item).setOpaque(((Boolean)value).booleanValue());
		} else if (name.equals(N_BACKGROUND) && value instanceof Color)
		    ((Component)item).setBackground((Color)value);
		else if (name.equals(N_FOREGROUND) && value instanceof Color)
		    ((Component)item).setForeground((Color)value);
		else if (name.equals(N_FONT) && value instanceof Font)
		    ((Component)item).setFont((Font)value);
	    } else if (item instanceof MenuComponent) {
		if (name.equals(N_FONT) && value instanceof Font)
		    ((MenuComponent)item).setFont((Font)value);
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static void
    addAccelerator(MenuItem menuitem, YoixObject accelerator) {

	MenuShortcut  shortcut = null;
	KeyStroke     keystroke;
	int           keycode;
	int           modifiers;

	//
	// A string value should fit the description provided in comments
	// that preceed Java's
	//
	//	KeyStroke.getKeyStroke(String s);
	//
	// method. Decided to punt, for now anyway, when accelerator is an
	// int because we had a few problems trying to make it behave the
	// like our Swing implementation.
	//

	if (menuitem != null && accelerator != null) {
	    if (accelerator.isString() || accelerator.isInteger()) {
		if (accelerator.isString() && accelerator.notNull()) {
		    try {
			keystroke = KeyStroke.getKeyStroke(accelerator.stringValue());
			keycode = keystroke.getKeyCode();
			modifiers = keystroke.getModifiers();
			shortcut = new MenuShortcut(keycode, (modifiers & Event.SHIFT_MASK) != 0);
		    }
		    catch(IllegalArgumentException e) {}
		}
		if (shortcut != null)
		    menuitem.setShortcut(shortcut);
	    }
	}
    }


    private static void
    addListener(MenuBar menubar, YoixInterfaceListener listener) {

	Menu  menu;
	int   count;
	int   n;

	count = menubar.getMenuCount();
	for (n = 0; n < count; n++) {
	    if ((menu = menubar.getMenu(n)) != null)
		addListener(menu, listener);
	}
    }


    private static void
    addListener(Menu menu, YoixInterfaceListener listener) {

	MenuItem  item;
	int       count;
	int       n;

	count = menu.getItemCount();
	for (n = 0; n < count; n++) {
	    if ((item = menu.getItem(n)) != null) {
		if (item instanceof Menu)
		    addListener((Menu)item, listener);
		else if (item instanceof CheckboxMenuItem)
		    ((CheckboxMenuItem)item).addItemListener((ItemListener)listener);
		else item.addActionListener((ActionListener)listener);
	    }
	}
    }


    private static void
    addListener(JMenuBar menubar, YoixInterfaceListener listener) {

	JMenu  menu;
	int    count;
	int    n;

	count = menubar.getMenuCount();
	for (n = 0; n < count; n++) {
	    if ((menu = menubar.getMenu(n)) != null)
		addListener(menu, listener);
	}
    }


    private static void
    addListener(JPopupMenu popup, YoixInterfaceListener listener) {

	Component  item;
	int        count;
	int        n;

	count = popup.getComponentCount();
	for (n = 0; n < count; n++) {
	    if ((item = popup.getComponent(n)) != null) {
		if (item instanceof JMenuItem) {
		    if (item instanceof JMenu)
			addListener((JMenu)item, listener);
		    else if (item instanceof JCheckBoxMenuItem)
			((JCheckBoxMenuItem)item).addItemListener((ItemListener)listener);
		    else if (item instanceof JRadioButtonMenuItem)
			((JRadioButtonMenuItem)item).addItemListener((ItemListener)listener);
		    else ((JMenuItem)item).addActionListener((ActionListener)listener);
		}
	    }
	}
    }


    private static void
    addListener(JMenu menu, YoixInterfaceListener listener) {

	JMenuItem  item;
	int        count;
	int        n;

	count = menu.getItemCount();
	for (n = 0; n < count; n++) {
	    if ((item = menu.getItem(n)) != null) {
		if (item instanceof JMenu)
		    addListener((JMenu)item, listener);
		else if (item instanceof JCheckBoxMenuItem)
		    item.addItemListener((ItemListener)listener);
		else if (item instanceof JRadioButtonMenuItem)
		    item.addItemListener((ItemListener)listener);
		else item.addActionListener((ActionListener)listener);
	    }
	}
    }


    private static Menu
    buildAWTMenu(Menu menu, YoixObject layout, HashMap groups) {

	YoixAWTCheckboxMenuItemGroup  mig;
	YoixAWTCheckboxMenuItem       checkbox;
	YoixObject                    name;
	YoixObject                    arg;
	YoixObject                    group;
	YoixObject                    element;
	MenuItem                      menuitem;
	boolean                       havestate;
	boolean                       state;
	String                        mig_tag;
	int                           length;
	int                           incr;
	int                           n;

	//
	// Added (12/5/04) a check that omits separators when the menu is
	// empty. Undoubtedly could do more - maybe later.
	//
	// Added (7/14/06) support for menu accelerators and an arbitrary
	// dictionary argument as the second entry in each menu description.
	// Probably wouldn't be hard to change things so the dictionary has
	// more control (e.g., set group and state) - maybe later.
	//
	// NOTE - changed limit test in for loop from "n < length - 1" to
	// "n < length" on 1/4/07. Also changed second arg definition to
	// make sure n is in bounds.
	//
	//

	length = layout.length();

	for (n = 0; n < length; n += incr) {
	    incr = 2;
	    arg = layout.get(n, false);
	    if (arg.isString() && arg.notNull()) {
		name = arg;
		arg = (n < length - 1) ? layout.get(n + 1, false) : YoixObject.newNull();
		if ((arg.isMenu() || arg.isArray()) && arg.notNull())
		    menu.add(buildAWTMenu(new Menu(name.stringValue()), arg, groups));
		else if (arg.isNull() && name.stringValue().equals("-")) {
		    if (menu.getItemCount() > 0)	// recent addition
			menu.addSeparator();
		} else if (arg.isString() || arg.isNull() || arg.isDictionary()) {
		    if (n + 2 < length && layout.get(n + 2, false).isNumber()) {
			incr = 3;
			checkbox = javaCheckboxMenuItem(name, arg, layout.getBoolean(n + 2));
			menu.add(checkbox);
		    } else if (n + 3 < length && layout.get(n + 3, false).isNumber()) {
			group = layout.get(n + 2, false);
			if (group.isString() || group.isNull()) {
			    incr = 4;
			    if (group.notNull()) {
				mig_tag = group.stringValue();
				if ((mig = (YoixAWTCheckboxMenuItemGroup)groups.get(mig_tag)) == null) {
				    mig = new YoixAWTCheckboxMenuItemGroup();
				    groups.put(mig_tag, mig);
				}
			    } else mig = null;
			    checkbox = javaCheckboxMenuItem(name, arg, layout.getBoolean(n + 3), mig);
			    menu.add(checkbox);
			} else VM.abort(BADMENUITEM, n + 2);
		    } else {
			mig = null;
			havestate = false;
			state = false;
			if (arg.isDictionary()) {
			    if ((group = arg.getObject(N_GROUP)) != null) {
				if (group.isString()) {
				    mig_tag = group.stringValue();
				    if ((mig = (YoixAWTCheckboxMenuItemGroup)groups.get(mig_tag)) == null) {
					mig = new YoixAWTCheckboxMenuItemGroup();
					groups.put(mig_tag, mig);
				    }
				}
			    }
			    if ((element = arg.getObject(N_STATE)) != null || (element = arg.getObject(N_SELECTED)) != null) {
				if (element.isNumber()) {
				    state = element.booleanValue();
				    havestate = true;
				}
			    }
			    if (mig != null) {
				checkbox = javaCheckboxMenuItem(name, arg, state, mig);
				menu.add(checkbox);
			    } else if (havestate) {
				checkbox = javaCheckboxMenuItem(name, arg, state);
				menu.add(checkbox);
			    } else {
				menuitem = javaMenuItem(name, arg);
				menu.add(menuitem);
			    }
			} else {
			    menuitem = javaMenuItem(name, arg);
			    menu.add(menuitem);
			}
		    }
		} else VM.abort(BADMENUITEM, n + 1);
	    } else if (arg.isArray() || arg.isMenu() || arg.isNull()) {
		incr = 1;
		if (arg.notNull())
		    buildAWTMenu(menu, arg, groups);
	    } else VM.abort(BADMENUITEM, n);
	}
	return(menu);
    }


    private static YoixObject
    buildSwingMenu(YoixObject array, YoixObject layout, HashMap groups) {

	JRadioButtonMenuItem  radiobutton;
	JCheckBoxMenuItem     checkbox;
	YoixObject            obj = null;
	YoixObject            mig;
	YoixObject            name;
	YoixObject            arg;
	YoixObject            group;
	JMenuItem             menuitem;
	String                mig_tag;
	int                   length;
	int                   incr;
	int                   n;

	//
	// Added (12/5/04) a check that omits separators when the menu is
	// empty. Undoubtedly could do more - maybe later.
	//
	// Added (7/14/06) support for menu accelerators and an arbitrary
	// dictionary argument as the second entry in each menu description.
	// Probably wouldn't be hard to change things so the dictionary has
	// more control (e.g., set group and state) - maybe later.
	//
	// NOTE - changed limit test in for loop from "n < length - 1" to
	// "n < length" on 1/4/07. Also changed second arg definition to
	// make sure n is in bounds.
	//

	length = layout.length();

	for (n = 0; n < length; n += incr) {
	    incr = 2;
	    obj = null;
	    arg = layout.get(n, false);
	    if (arg.isString() && arg.notNull()) {
		name = arg;
		arg = (n < length - 1) ? layout.get(n + 1, false) : YoixObject.newNull();
		if ((arg.isArray() || arg.isMenu()) && arg.notNull())
		    obj = yoixJMenu(name, buildSwingMenuArray(arg, groups));
		else if (arg.isNull() && name.stringValue().equals("-")) {
		    if (array.length() > 0)	// recent addition
			obj = YoixObject.newNull();
		    else obj = null;
		} else if (arg.isString() || arg.isNull() || arg.isDictionary()) {
		    if (n + 2 < length && layout.get(n + 2, false).isNumber()) {
			incr = 3;
			obj = yoixJMenuItem(YOIX_CHECKBOX_BUTTON, name, arg, null);
			checkbox = (JCheckBoxMenuItem)(obj.getManagedObject());
			checkbox.setState(layout.getBoolean(n + 2));
		    } else if (n + 3 < length && layout.get(n + 3, false).isNumber()) {
			group = layout.get(n + 2, false);
			if (group.isString() || group.isNull()) {
			    incr = 4;
			    if (group.notNull()) {
				mig_tag = group.stringValue();
				if ((mig = (YoixObject)groups.get(mig_tag)) == null) {
				    mig = YoixObject.newJComponent(VM.getTypeTemplate(T_BUTTONGROUP));
				    groups.put(mig_tag, mig);
				}
				obj = yoixJMenuItem(YOIX_RADIO_BUTTON, name, arg, mig);
				radiobutton = (JRadioButtonMenuItem)(obj.getManagedObject());
				radiobutton.setSelected(layout.getBoolean(n + 3));
			    } else {
				obj = yoixJMenuItem(YOIX_CHECKBOX_BUTTON, name, arg, null);
				checkbox = (JCheckBoxMenuItem)(obj.getManagedObject());
				checkbox.setState(layout.getBoolean(n + 2));
			    }
			} else VM.abort(BADMENUITEM, n + 2);
		    } else {
			obj = yoixJMenuItem(name, arg, groups);
			menuitem = (JMenuItem)(obj.getManagedObject());
		    }
		} else VM.abort(BADMENUITEM, n + 1);
		if (obj != null)
		    array.put(array.length(), obj, false);
	    } else if (arg.isArray() || arg.isMenu() || arg.isNull()) {
		incr = 1;
		if (arg.notNull())
		    buildSwingMenu(array, arg, groups);
	    } else VM.abort(BADMENUITEM, n);
	}
	return(array);
    }


    private static YoixObject
    buildSwingMenuArray(YoixObject layout, HashMap groups) {

	YoixObject  array = null;

	if (layout != null) {
	    array = YoixObject.newArray(0, -1);
	    buildSwingMenu(array, layout, groups);
	    array.setGrowable(false);
	}
	return(array);
    }


    private static void
    dispose(MenuBar menubar) {

	Menu  menu;
	int   n;

	for (n = menubar.getMenuCount() - 1; n >= 0; n--) {
	    if ((menu = menubar.getMenu(n)) != null) {
		try {
		    dispose(menu);
		    menubar.remove(menu);
		}
		catch(RuntimeException e) {}
	    }
	}
    }


    private static void
    dispose(Menu menu) {

	MenuItem  item;
	int       n;

	for (n = menu.getItemCount() - 1; n >= 0; n--) {
	    if ((item = menu.getItem(n)) != null) {
		try {
		    if (item instanceof Menu)
			dispose((Menu)item);
		    menu.remove(item);
		}
		catch(RuntimeException e) {}
	    }
	}
    }


    private static int
    getMenuItemEnabled(MenuBar menubar, Object pattern) {

	Menu  menu;
	int   result = -1;
	int   count;
	int   n;

	count = menubar.getMenuCount();
	for (n = 0; n < count && result < 0; n++) {
	    if ((menu = menubar.getMenu(n)) != null)
		result = getMenuItemEnabled(menu, pattern);
	}
	return(result);
    }


    private static int
    getMenuItemEnabled(Menu menu, Object pattern) {

	MenuItem  item;
	int       result = -1;
	int       count;
	int       n;

	count = menu.getItemCount();
	for (n = 0; n < count && result < 0; n++) {
	    if ((item = menu.getItem(n)) != null) {
		if (matchPattern(pattern, item.getActionCommand()))
		    result = item.isEnabled() ? 1 : 0;
		else if (item instanceof Menu)
		    result = getMenuItemEnabled((Menu)item, pattern);
	    }
	}
	return(result);
    }


    private static int
    getMenuItemEnabled(JMenuBar menubar, Object pattern) {

	JMenu  menu;
	int    result = -1;
	int    count;
	int    n;

	count = menubar.getMenuCount();
	for (n = 0; n < count && result < 0; n++) {
	    if ((menu = menubar.getMenu(n)) != null)
		result = getMenuItemEnabled(menu, pattern);
	}
	return(result);
    }


    private static int
    getMenuItemEnabled(JPopupMenu popup, Object pattern) {

	Component  item;
	int        result = -1;
	int        count;
	int        n;

	count = popup.getComponentCount();
	for (n = 0; n < count && result < 0; n++) {
	    if ((item = popup.getComponent(n)) != null) {
		if (item instanceof JMenuItem) {
		    if (matchPattern(pattern, ((JMenuItem)item).getActionCommand()))
			result = ((JMenuItem)item).isEnabled() ? 1 : 0;
		    else if (item instanceof JMenu)
			result = getMenuItemEnabled((JMenu)item, pattern);
		}
	    }
	}
	return(result);
    }


    private static int
    getMenuItemEnabled(JMenu menu, Object pattern) {

	JMenuItem  item;
	int        result = -1;
	int        count;
	int        n;

	count = menu.getItemCount();
	for (n = 0; n < count && result < 0; n++) {
	    if ((item = menu.getItem(n)) != null) {
		if (matchPattern(pattern, item.getActionCommand()))
		    result = item.isEnabled() ? 1 : 0;
		else if (item instanceof JMenu)
		    result = getMenuItemEnabled((JMenu)item, pattern);
	    }
	}
	return(result);
    }


    private static int
    getMenuItemState(MenuBar menubar, Object pattern) {

	Menu  menu;
	int   result = -1;
	int   count;
	int   n;

	count = menubar.getMenuCount();
	for (n = 0; n < count && result < 0; n++) {
	    if ((menu = menubar.getMenu(n)) != null)
		result = getMenuItemState(menu, pattern);
	}
	return(result);
    }


    private static int
    getMenuItemState(Menu menu, Object pattern) {

	MenuItem  item;
	int       result = -1;
	int       count;
	int       n;

	count = menu.getItemCount();
	for (n = 0; n < count && result < 0; n++) {
	    if ((item = menu.getItem(n)) != null) {
		if (item instanceof CheckboxMenuItem) {
		    if (matchPattern(pattern, item.getActionCommand()))
			result = ((CheckboxMenuItem)item).getState() ? 1 : 0;
		} else if (item instanceof Menu)
		    result = getMenuItemState((Menu)item, pattern);
	    }
	}
	return(result);
    }


    private static int
    getMenuItemState(JMenuBar menubar, Object pattern) {

	JMenu  menu;
	int    result = -1;
	int    count;
	int    n;

	count = menubar.getMenuCount();
	for (n = 0; n < count && result < 0; n++) {
	    if ((menu = menubar.getMenu(n)) != null)
		result = getMenuItemState(menu, pattern);
	}
	return(result);
    }


    private static int
    getMenuItemState(JPopupMenu popup, Object pattern) {

	Component  item;
	int        result = -1;
	int        count;
	int        n;

	count = popup.getComponentCount();
	for (n = 0; n < count && result < 0; n++) {
	    if ((item = popup.getComponent(n)) != null) {
		if (item instanceof JCheckBoxMenuItem) {
		    if (matchPattern(pattern, ((JCheckBoxMenuItem)item).getActionCommand()))
			result = ((JCheckBoxMenuItem)item).isSelected() ? 1 : 0;
		} else if (item instanceof JRadioButtonMenuItem) {
		    if (matchPattern(pattern, ((JRadioButtonMenuItem)item).getActionCommand()))
			result = ((JRadioButtonMenuItem)item).isSelected() ? 1 : 0;
		} else if (item instanceof JMenu)
		    result = getMenuItemState(pattern, (JMenu)item);
	    }
	}
	return(result);
    }


    private static int
    getMenuItemState(JMenu menu, Object pattern) {

	JMenuItem  item;
	int        result = -1;
	int        count;
	int        n;

	count = menu.getItemCount();
	for (n = 0; n < count && result < 0; n++) {
	    if ((item = menu.getItem(n)) != null) {
		if (item instanceof JCheckBoxMenuItem) {
		    if (matchPattern(pattern, item.getActionCommand()))
			result = item.isSelected() ? 1 : 0;
		} else if (item instanceof JRadioButtonMenuItem) {
		    if (matchPattern(pattern, item.getActionCommand()))
			result = item.isSelected() ? 1 : 0;
		} else if (item instanceof JMenu)
		    result = getMenuItemState((JMenu)item, pattern);
	    }
	}
	return(result);
    }


    private static YoixAWTCheckboxMenuItem
    javaCheckboxMenuItem(YoixObject text, YoixObject arg, boolean state) {

	YoixAWTCheckboxMenuItem  checkbox;
	YoixObject               obj;

	checkbox = new YoixAWTCheckboxMenuItem(text.stringValue());
	if (arg.isDictionary()) {
	    checkbox.setActionCommand(arg.getString(N_COMMAND));
	    if ((obj = arg.getObject(N_ENABLED)) != null)
		checkbox.setEnabled(obj.isInteger() && obj.booleanValue());
	    addAccelerator(checkbox, arg.getObject(N_ACCELERATOR));
	} else if (arg.isString() || arg.isNull()) {
	    checkbox.setActionCommand(arg.stringValue());
	    checkbox.setEnabled(arg.isNull() == false);
	}
	checkbox.setState(state);
	return(checkbox);
    }


    private static YoixAWTCheckboxMenuItem
    javaCheckboxMenuItem(YoixObject text, YoixObject arg, boolean state, YoixAWTCheckboxMenuItemGroup mig) {

	YoixAWTCheckboxMenuItem  checkbox;
	YoixObject               obj;

	checkbox = new YoixAWTCheckboxMenuItem(text.stringValue(), state, mig);
	if (arg.isDictionary()) {
	    checkbox.setActionCommand(arg.getString(N_COMMAND));
	    if ((obj = arg.getObject(N_ENABLED)) != null)
		checkbox.setEnabled(obj.isInteger() && obj.booleanValue());
	    addAccelerator(checkbox, arg.getObject(N_ACCELERATOR));
	} else if (arg.isString() || arg.isNull()) {
	    checkbox.setActionCommand(arg.stringValue());
	    checkbox.setEnabled(arg.isNull() == false);
	}
	return(checkbox);
    }


    private static MenuItem
    javaMenuItem(YoixObject text, YoixObject arg) {

	YoixObject  obj;
	MenuItem    menuitem;
 
	menuitem = new MenuItem(text.stringValue());
	if (arg.isDictionary()) {
	    menuitem.setActionCommand(arg.getString(N_COMMAND));
	    if ((obj = arg.getObject(N_ENABLED)) != null)
		menuitem.setEnabled(obj.isInteger() && obj.booleanValue());
	    addAccelerator(menuitem, arg.getObject(N_ACCELERATOR));
	} else if (arg.isString() || arg.isNull()) {
	    menuitem.setActionCommand(arg.stringValue());
	    menuitem.setEnabled(arg.isNull() == false);
	}
	return(menuitem);
    }


    private static boolean
    matchMore(Object pattern, int result) {

	//
	// We may want more tests (e.g., pattern could be a YoixObject so
	// we might need to take a closer look). 
	// 

	return(
	    (pattern instanceof String && result < 0) ||
	    (pattern instanceof YoixRERegexp)
	);
    }


    private static boolean
    matchPattern(Object pattern, String command) {

	boolean  result;

	if (pattern instanceof String)
	    result = pattern.equals(command);
	else if (pattern instanceof YoixRERegexp)
	    result = ((YoixRERegexp)pattern).exec(command, null);
	else result = false;

	return(result);
    }


    private static void
    removeListener(MenuBar menubar, YoixInterfaceListener listener) {

	Menu  menu;
	int   count;
	int   n;

	count = menubar.getMenuCount();
	for (n = 0; n < count; n++) {
	    if ((menu = menubar.getMenu(n)) != null)
		removeListener(menu, listener);
	}
    }


    private static void
    removeListener(Menu menu, YoixInterfaceListener listener) {

	MenuItem  item;
	int       count;
	int       n;

	count = menu.getItemCount();
	for (n = 0; n < count; n++) {
	    if ((item = menu.getItem(n)) != null) {
		if (item instanceof Menu)
		    removeListener((Menu)item, listener);
		else if (item instanceof CheckboxMenuItem)
		    ((CheckboxMenuItem)item).removeItemListener((ItemListener)listener);
		else item.removeActionListener((ActionListener)listener);
	    }
	}
    }


    private static void
    removeListener(JMenuBar menubar, YoixInterfaceListener listener) {

	JMenu  menu;
	int    count;
	int    n;

	count = menubar.getMenuCount();
	for (n = 0; n < count; n++) {
	    if ((menu = menubar.getMenu(n)) != null)
		removeListener(menu, listener);
	}
    }


    private static void
    removeListener(JPopupMenu popup, YoixInterfaceListener listener) {

	Component  item;
	int        count;
	int        n;

	count = popup.getComponentCount();
	for (n = 0; n < count; n++) {
	    if ((item = popup.getComponent(n)) != null) {
		if (item instanceof JMenuItem) {
		    if (item instanceof JMenu)
			removeListener((JMenu)item, listener);
		    else if (item instanceof JCheckBoxMenuItem)
			((JCheckBoxMenuItem)item).removeItemListener((ItemListener)listener);
		    else if (item instanceof JRadioButtonMenuItem)
			((JRadioButtonMenuItem)item).removeItemListener((ItemListener)listener);
		    else ((JMenuItem)item).removeActionListener((ActionListener)listener);
		}
	    }
	}
    }


    private static void
    removeListener(JMenu menu, YoixInterfaceListener listener) {

	JMenuItem  item;
	int        count;
	int        n;

	count = menu.getItemCount();
	for (n = 0; n < count; n++) {
	    if ((item = menu.getItem(n)) != null) {
		if (item instanceof JMenu)
		    removeListener((JMenu)item, listener);
		else if (item instanceof JCheckBoxMenuItem)
		    item.removeItemListener((ItemListener)listener);
		else if (item instanceof JRadioButtonMenuItem)
		    item.removeItemListener((ItemListener)listener);
		else item.removeActionListener((ActionListener)listener);
	    }
	}
    }


    private static int
    setMenuItemEnabled(MenuBar menubar, Object pattern, boolean state) {

	Menu  menu;
	int   result = -1;
	int   count;
	int   n;

	if (menubar != null && pattern != null) {
	    count = menubar.getMenuCount();
	    for (n = 0; n < count && matchMore(pattern, result); n++) {
		if ((menu = menubar.getMenu(n)) != null)
		    result = setMenuItemEnabled(menu, pattern, state);
	    }
	}
	return(result);
    }


    private static int
    setMenuItemEnabled(Menu menu, Object pattern, boolean state) {

	MenuItem  item;
	int       result = -1;
	int       count;
	int       n;

	count = menu.getItemCount();
	for (n = 0; n < count && matchMore(pattern, result); n++) {
	    if ((item = menu.getItem(n)) != null) {
		if (matchPattern(pattern, item.getActionCommand())) {
		    if (item.isEnabled() != state) {
			result = state ? 0 : 1;
			item.setEnabled(state);
		    } else result = state ? 1 : 0;
		} else if (item instanceof Menu)
		    result = setMenuItemEnabled((Menu)item, pattern, state);
	    }
	}
	return(result);
    }


    private static int
    setMenuItemEnabled(JMenuBar menubar, Object pattern, boolean state) {

	JMenu  menu;
	int    result = -1;
	int    count;
	int    n;

	if (menubar != null && pattern != null) {
	    count = menubar.getMenuCount();
	    for (n = 0; n < count && matchMore(pattern, result); n++) {
		if ((menu = menubar.getMenu(n)) != null)
		    result = setMenuItemEnabled(menu, pattern, state);
	    }
	}
	return(result);
    }


    private static int
    setMenuItemEnabled(JPopupMenu popup, Object pattern, boolean state) {

	Component  item;
	int        result = -1;
	int        count;
	int        n;

	if (popup != null && pattern != null) {
	    count = popup.getComponentCount();
	    for (n = 0; n < count && matchMore(pattern, result); n++) {
		if ((item = popup.getComponent(n)) != null) {
		    if (item instanceof JMenuItem) {
			if (matchPattern(pattern, ((JMenuItem)item).getActionCommand())) {
			    if (item.isEnabled() != state) {
				result = state ? 0 : 1;
				item.setEnabled(state);
			    } else result = state ? 1 : 0;
			} else if (item instanceof JMenu)
			    result = setMenuItemEnabled((JMenu)item, pattern, state);
		    }
		}
	    }
	}
	return(result);
    }


    private static int
    setMenuItemEnabled(JMenu menu, Object pattern, boolean state) {

	JMenuItem  item;
	int        result = -1;
	int        count;
	int        n;

	count = menu.getItemCount();
	for (n = 0; n < count && matchMore(pattern, result); n++) {
	    if ((item = menu.getItem(n)) != null) {
		if (matchPattern(pattern, item.getActionCommand())) {
		    if (item.isEnabled() != state) {
			result = state ? 0 : 1;
			item.setEnabled(state);
		    } else result = state ? 1 : 0;
		} else if (item instanceof JMenu)
		    result = setMenuItemEnabled((JMenu)item, pattern, state);
	    }
	}
	return(result);
    }


    private static int
    setMenuItemState(MenuBar menubar, Object pattern, boolean state) {

	Menu  menu;
	int   result = -1;
	int   count;
	int   n;

	if (menubar != null && pattern != null) {
	    count = menubar.getMenuCount();
	    for (n = 0; n < count && matchMore(pattern, result); n++) {
		if ((menu = menubar.getMenu(n)) != null)
		    result = setMenuItemState(menu, pattern, state);
	    }
	}
	return(result);
    }


    private static int
    setMenuItemState(Menu menu, Object pattern, boolean state) {

	MenuItem  item;
	int       result = -1;
	int       count;
	int       n;

	//
	// We try hard to avoid unnecessary setState() calls even though
	// the problems that we did see were restricted to Swing.
	//

	count = menu.getItemCount();
	for (n = 0; n < count && matchMore(pattern, result); n++) {
	    if ((item = menu.getItem(n)) != null) {
		if (item instanceof YoixAWTCheckboxMenuItem) {
		    if (matchPattern(pattern, item.getActionCommand())) {
			if (((YoixAWTCheckboxMenuItem)item).getState() != state) {
			    result = state ? 0 : 1;
			    ((YoixAWTCheckboxMenuItem)item).setState(state, false);
			} else result = state ? 1 : 0;
		    }
		} else if (item instanceof CheckboxMenuItem) {
		    if (matchPattern(pattern, item.getActionCommand())) {
			if (((CheckboxMenuItem)item).getState() != state) {
			    result = state ? 0 : 1;
			    ((CheckboxMenuItem)item).setState(state);
			} else result = state ? 1 : 0;
		    }
		} else if (item instanceof Menu)
		    result = setMenuItemState((Menu)item, pattern, state);
	    }
	}
	return(result);
    }


    private static int
    setMenuItemState(JMenuBar menubar, Object pattern, boolean state) {

	JMenu  menu;
	int    result = -1;
	int    count;
	int    n;

	if (menubar != null && pattern != null) {
	    count = menubar.getMenuCount();
	    for (n = 0; n < count && matchMore(pattern, result); n++) {
		if ((menu = menubar.getMenu(n)) != null)
		    result = setMenuItemState(menu, pattern, state);
	    }
	}
	return(result);
    }


    private static int
    setMenuItemState(JPopupMenu popup, Object pattern, boolean state) {

	ButtonModel  model;
	ButtonGroup  group;
	Component    item;
	int          result = -1;
	int          count;
	int          n;

	//
	// We try hard to avoid unnecessary setSelected() calls because
	// some versions of Java on some platforms (e.g., 1.3.1 on our
	// SGIs) will send an ItemEvent even if the state didn't change.
	// Java's mistake, but it's easy to avoid. Be careful and test
	// thoroughly if you change this code.
	//

	if (popup != null && pattern != null) {
	    count = popup.getComponentCount();
	    for (n = 0; n < count && matchMore(pattern, result); n++) {
		if ((item = popup.getComponent(n)) != null) {
		    if (item instanceof JCheckBoxMenuItem) {
			if (matchPattern(pattern, ((JCheckBoxMenuItem)item).getActionCommand())) {
			    if (((JCheckBoxMenuItem)item).isSelected() != state) {
				result = state ? 0 : 1;
				((JCheckBoxMenuItem)item).setSelected(state);
			    } else result = state ? 1 : 0;
			}
		    } else if (item instanceof JRadioButtonMenuItem) {
			if (matchPattern(pattern, ((JRadioButtonMenuItem)item).getActionCommand())) {
			    if (((JRadioButtonMenuItem)item).isSelected() != state) {
				result = state ? 0 : 1;
				if (state == false) {
				    model = ((JRadioButtonMenuItem)item).getModel();
				    if (model instanceof DefaultButtonModel)
					group = ((DefaultButtonModel)model).getGroup();
				    else group = null;
				    if (group != null) {
					group.remove((JRadioButtonMenuItem)item);
					((JRadioButtonMenuItem)item).setSelected(state);
					group.add((JRadioButtonMenuItem)item);
				    } else ((JRadioButtonMenuItem)item).setSelected(state);
				} else ((JRadioButtonMenuItem)item).setSelected(state);
			    } else result = state ? 1 : 0;
			}
		    } else if (item instanceof JMenu)
			result = setMenuItemState((JMenu)item, pattern, state);
		}
	    }
	}
	return(result);
    }


    private static int
    setMenuItemState(JMenu menu, Object pattern, boolean state) {

	ButtonModel  model;
	ButtonGroup  group;
	JMenuItem    item;
	int          result = -1;
	int          count;
	int          n;

	//
	// We try hard to avoid unnecessary setSelected() calls because
	// some versions of Java on some platforms (e.g., 1.3.1 on our
	// SGIs) will send an ItemEvent even if the state didn't change.
	// Java's mistake, but it's easy to avoid. Be careful and test
	// thoroughly if you change this code.
	//

	if (menu != null && pattern != null) {
	    count = menu.getItemCount();
	    for (n = 0; n < count && matchMore(pattern, result); n++) {
		if ((item = menu.getItem(n)) != null) {
		    if (item instanceof JCheckBoxMenuItem) {
			if (matchPattern(pattern, item.getActionCommand())) {
			    if (item.isSelected() != state) {
				result = state ? 0 : 1;
				item.setSelected(state);
			    } else result = state ? 1 : 0;
			}
		    } else if (item instanceof JRadioButtonMenuItem) {
			if (matchPattern(pattern, item.getActionCommand())) {
			    if (item.isSelected() != state) {
				result = state ? 0 : 1;
				if (state == false) {
				    model = item.getModel();
				    if (model instanceof DefaultButtonModel)
					group = ((DefaultButtonModel)model).getGroup();
				    else group = null;
				    if (group != null) {
					group.remove(item);
					item.setSelected(state);
					group.add(item);
				    } else item.setSelected(state);
				} else item.setSelected(state);
			    } else result = state ? 1 : 0;
			}
		    } else if (item instanceof JMenu)
			result = setMenuItemState((JMenu)item, pattern, state);
		}
	    }
	}
	return(result);
    }


    private static void
    setMenuProperty(MenuBar menubar, String name, Object value) {

	Menu  menu;
	int   count;
	int   n;

	if (menubar != null && name != null && value != null) {
	    setNewProperty(menubar, name, value);
	    count = menubar.getMenuCount();
	    for (n = 0; n < count; n++) {
		if ((menu = menubar.getMenu(n)) != null)
		    setMenuProperty(menu, name, value);
	    }
	}
    }


    private static void
    setMenuProperty(Menu menu, String name, Object value) {

	MenuItem  item;
	int       count;
	int       n;

	if (menu != null && name != null && value != null) {
	    setNewProperty(menu, name, value);
	    count = menu.getItemCount();
	    for (n = 0; n < count; n++) {
		if ((item = menu.getItem(n)) != null) {
		    if (item instanceof Menu)
			setMenuProperty((Menu)item, name, value);
		    else setNewProperty(item, name, value);
		}
	    }
	}
    }


    private static void
    setMenuProperty(JMenuBar menubar, String name, Object value) {

	JMenu  menu;
	int    count;
	int    n;

	if (menubar != null && name != null && value != null) {
	    setNewProperty(menubar, name, value);
	    count = menubar.getMenuCount();
	    for (n = 0; n < count; n++) {
		if ((menu = menubar.getMenu(n)) != null)
		    setMenuProperty(menu, name, value);
	    }
	}
    }


    private static void
    setMenuProperty(JPopupMenu popup, String name, Object value) {

	Component  item;
	int        count;
	int        n;

	if (popup != null && name != null && value != null) {
	    setNewProperty(popup, name, value);
	    count = popup.getComponentCount();
	    for (n = 0; n < count; n++) {
		if ((item = popup.getComponent(n)) != null) {
		    if (item instanceof JMenu)
			setMenuProperty((JMenu)item, name, value);
		    else setNewProperty(item, name, value);
		}
	    }
	}
    }


    private static void
    setMenuProperty(JMenu menu, String name, Object value) {

	JMenuItem  item;
	int        count;
	int        n;

	if (menu != null && name != null && value != null) {
	    setNewProperty(menu, name, value);
	    count = menu.getItemCount();
	    for (n = 0; n < count; n++) {
		if ((item = menu.getItem(n)) != null) {
		    if (item instanceof JMenu)
			setMenuProperty((JMenu)item, name, value);
		    else setNewProperty((Component)item, name, value);
		}
	    }
	}
    }


    private static YoixObject
    yoixJMenu(YoixObject text, YoixObject items) {

	YoixObject  data;

	data = VM.getTypeTemplate(T_JMENU);
	if (text != null)
	    data.put(N_TEXT, text, true);
	if (items != null)
	    data.put(N_ITEMS, items, true);
	return(YoixObject.newJComponent(data));
    }


    private static YoixObject
    yoixJMenuItem(YoixObject text, YoixObject arg, HashMap groups) {

	YoixObject  data;
	YoixObject  value;
	String      name;
	String      tag;
	int         type = -1;
	int         length;
	int         n;

	data = VM.getTypeTemplate(T_JMENUITEM);
	if (arg.isDictionary()) {
	    length = arg.length();
	    for (n = arg.offset(); n < length; n++) {
		if (arg.defined(n)) {
		    name = arg.name(n);
		    value = arg.get(n, true);
		    if (name.equals(N_GROUP)) {
			if (value.isString()) {
			    tag = value.stringValue();
			    if ((value = (YoixObject)groups.get(tag)) == null) {
				value = YoixObject.newJComponent(VM.getTypeTemplate(T_BUTTONGROUP));
				groups.put(tag, value);
			    }
			    if (type == -1)
				type = YOIX_RADIO_BUTTON;
			}
		    } else if (name.equals(N_STATE) || name.equals(N_SELECTED)) {
			if (type == -1 && value.isNumber()) {
			    if (arg.defined(N_GROUP) == false)
				type = YOIX_CHECKBOX_BUTTON;
			}
		    } else if (name.equals(N_TYPE)) {
			type = value.intValue();
			value = null;
		    }
		    if (value != null)
			data.putObject(name, value);
		}
	    }
	} else if (arg.isString() || arg.isNull()) {
	    data.put(N_COMMAND, arg, true);
	    data.putInt(N_ENABLED, arg.notNull());
	}

	if (type != -1)
	    data.putInt(N_TYPE, type);
	if (text != null)
	    data.put(N_TEXT, text, true);
	return(YoixObject.newJComponent(data));
    }


    private static YoixObject
    yoixJMenuItem(int type, YoixObject text, YoixObject arg, YoixObject group) {

	YoixObject  data;
	String      name;
	int         length;
	int         n;

	//
	// Ignores type and group fields when arg is a dictionary, which
	// means the caller is responsible for picking appropriate values
	// and passing them to us as arguments.
	//

	data = VM.getTypeTemplate(T_JMENUITEM);
	if (arg.isDictionary()) {
	    length = arg.length();
	    for (n = arg.offset(); n < length; n++) {
		if (arg.defined(n)) {
		    name = arg.name(n);
		    if (name.equals(N_GROUP) == false && name.equals(N_TYPE) == false)
			data.putObject(name, arg.get(n, true));
		}
	    }
	} else if (arg.isString() || arg.isNull()) {
	    data.put(N_COMMAND, arg, true);
	    data.putInt(N_ENABLED, arg.notNull());
	}

	data.putInt(N_TYPE, type);
	if (text != null)
	    data.put(N_TEXT, text, true);
	if (group != null)
	    data.put(N_GROUP, group, true);
	return(YoixObject.newJComponent(data));
    }
}

