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
import javax.swing.event.*;
import javax.swing.plaf.ColorUIResource;

class YoixSwingJTabbedPane extends JTabbedPane

    implements ChangeListener,
	       ContainerListener,
	       FocusListener,
	       MouseListener,
	       YoixConstants

{

    //
    // Unfortunately we had trouble making a transparent tabbed pane
    // using Java 1.4.2. Didn't track it down, but it sure looks like
    // it's a Java bug that was introduced after 1.3.1. Probably will
    // investigate more - later.
    //
    // We recently added some code that tries to remember the focused
    // component in each tabbed pane. It seems to work properly, but it
    // does depend on the fact that our mouseEvent handlers are called
    // last (actually they just have to be called after the ones that
    // belong to the UI) so we also nave to override addMouseListener().
    // The focus tracking code is enabled using setTrackFocus().
    //

    private YoixBodyComponentSwing  parent;
    private YoixObject              data;

    private int  sizecontrol = 0;

    private int      model_minsize = 0;			// bits 0 to 3
    private int      model_preferredsize = 0;		// bits 4 to 7
    private boolean  lockminsize_flag = false;		// bit 8
    private boolean  lockpreferredsize_flag = false;	// bit 9
    private int      alternate_width = 0;		// bits 12 to 21
    private int      alternate_height = 0;		// bits 22 to 31

    //
    // These are used to remember the last component in each tab that
    // had the focus so it can be restored the next time that tab is
    // selected. Without this Swing always seems to give the focus to
    // the first component in the tabbed pane.
    //
    // NOTE - there's a small chance this approach could be delegated to
    // YoixSwingJPanel, but it's overhead that's really only needed when
    // a YoixSwingJPanel is displayed by a JTabbedPane or in a CardLayout.
    // The problem that approach would have to solve is deciding when to
    // "ignore" a focusGained() event. We do it here using mousePressed()
    // and mouseReleased() (and by making sure our MouseEvent handlers are
    // called last), but that probably doesn't work in a JPanel and relying
    // on componentShown() and componentHidden() probably also won't work.
    // 

    private Component  lastfocused = null;
    private boolean    trackfocus = false;
    private boolean    mousepressed = false;
    private HashMap    focusowners;		// for each tab
    private HashMap    tabmap;			// maps component to tab

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJTabbedPane(YoixObject data, YoixBodyComponentSwing parent) {

	super();

	this.parent = parent;
	this.data = data;
	addChangeListener(this);
    }

    ///////////////////////////////////
    //
    // ChangeListener Methods
    //
    ///////////////////////////////////

    public final synchronized void
    stateChanged(ChangeEvent e) {

	syncToolTips();
    }

    ///////////////////////////////////
    //
    // ContainerListener Methods
    //
    ///////////////////////////////////

    public final void
    componentAdded(ContainerEvent e) {

	startFocusTrackingFor(e.getChild());
    }


    public final void
    componentRemoved(ContainerEvent e) {

	stopFocusTrackingFor(e.getChild());
    }

    ///////////////////////////////////
    //
    // FocusListener Methods
    //
    ///////////////////////////////////

    public final synchronized void
    focusGained(FocusEvent e) {

	Component  owner;
	Component  comp;

	if (mousepressed == false) {
	    if (trackfocus) {
		comp = (Component)e.getSource();
		if ((owner = (Component)tabmap.get(comp)) != null)
		    focusowners.put(owner, comp);
	    }
	}
    }


    public final void
    focusLost(FocusEvent e) {

    }

    ///////////////////////////////////
    //
    // MouseListener Methods
    //
    ///////////////////////////////////

    public final void
    mouseClicked(MouseEvent e) {

    }


    public final void
    mouseEntered(MouseEvent e) {

    }


    public final void
    mouseExited(MouseEvent e) {

    }


    public final synchronized void
    mousePressed(MouseEvent e) {

	int  index;

	mousepressed = true;
	if (focusowners != null) {
	    if ((index = getSelectedIndex()) >= 0)
		lastfocused = (Component)focusowners.get(getComponentAt(index));
	    else lastfocused = null;
	} else lastfocused = null;
    }


    public final synchronized void
    mouseReleased(MouseEvent e) {

	if (lastfocused != null) {
	    lastfocused.requestFocus();
	    lastfocused = null;
	}
	mousepressed = false;
    }

    ///////////////////////////////////
    //
    // YoixSwingJTabbedPane Methods
    //
    ///////////////////////////////////

    public final synchronized void
    addMouseListener(MouseListener listener) {

	//
	// This should guarantee our MouseEvent handlers are called last.
	//

	if (trackfocus && listener != this) {
	    super.removeMouseListener(this);
	    super.addMouseListener(listener);
	    super.addMouseListener(this);
	} else super.addMouseListener(listener);
    }


    protected void
    finalize() {

	setTrackFocus(false);
	data = null;
	parent = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    public Dimension
    getMinimumSize() {

	Dimension  size = null;

	switch (pickModel(false)) {
	    case 0:
	    case 1:
		size = super.getMinimumSize();
		break;

	    case 2:
		size = new Dimension(alternate_width, alternate_height);
		break;
	}
	return(size != null ? size : new Dimension(0, 0));
    }


    public Dimension
    getPreferredSize() {

	Dimension  size = null;

	//
	// Doubt you'll ever use model 2 in this method, but we'll leave
	// it in just for consistency with getMinimumSize().
	//

	switch (pickModel(true)) {
	    case 0:
	    case 1:
		size = super.getPreferredSize();
		break;

	    case 2:
		size = new Dimension(alternate_width, alternate_height);
		break;
	}
	return(size != null ? size : super.getPreferredSize());
    }


    final int
    getSizeControl() {

	return(sizecontrol);
    }


    final boolean
    getTrackFocus() {

	return(trackfocus);
    }


    final synchronized void
    setAlignment(int alignment) {

	switch (alignment) {
	    case TOP:
	    case BOTTOM:
	    case LEFT:
	    case RIGHT:
		setTabPlacement(alignment);
		break;

	    default:
		setTabPlacement(TOP);
		break;
	}
    }


    final synchronized void
    setFirstFocus(Component tab, Component comp) {

	if (focusowners != null) {
	    if (tab != null) {
		if (focusowners.get(tab) == null)
		    focusowners.put(tab, comp);
	    }
	}
    }


    public final void
    setForegroundAt(int index, Color color) {

	if (UIManager.getLookAndFeel().getClass().getName().indexOf("Aqua") >= 0) {
	    if (color.equals(UIManager.getColor("TabbedPane.foreground")))
		color = new ColorUIResource(color);
	}
	super.setForegroundAt(index, color);
    }


    final synchronized void
    setScrollPolicy(int policy) {

	switch (policy) {
	    case YOIX_ALWAYS:
	    case YOIX_AS_NEEDED:
		policy = SCROLL_TAB_LAYOUT;
		break;

	    default:
		policy = WRAP_TAB_LAYOUT;
		break;
	}
	setTabLayoutPolicy(policy);
    }


    final synchronized void
    setSelected(YoixObject obj) {

	int  index;
	int  count;
	int  n;

	if (obj != null) {
	    if (obj.isNull()) {
		index = 0;
		count = getTabCount();
		for (n = 0; n < count; n++) {
		    if (isEnabledAt(n)) {
			index = n;
			break;
		    }
		}
	    } else if (obj.isComponent())
		index = indexOfComponent((Component)obj.getManagedObject());
	    else if (obj.isNumber())
		index = obj.intValue();
	    else index = -1;
	    if (index >= 0) {
		try {
		    setSelectedIndex(index);
		}
		catch(IllegalArgumentException e) {}
		catch(IndexOutOfBoundsException e) {}
	    }
	}
    }


    final synchronized void
    setSizeControl(int flags) {

	if (flags != sizecontrol) {
	    sizecontrol = flags;
	    model_minsize = (sizecontrol >> 0) & 0x0F;
	    model_preferredsize = (sizecontrol >> 4) & 0x0F;
	    lockminsize_flag = ((sizecontrol >> 8) & 0x01) != 0;
	    lockpreferredsize_flag = ((sizecontrol >> 9) & 0x01) != 0;
	    //
	    // Two unused flags - placement matches YoixJScrollPane.java
	    // even though it doesn't have to.
	    //
	    alternate_width = (sizecontrol >> 12) & 0x3FF;
	    alternate_height = (sizecontrol >> 22) & 0x3FF;
	    revalidate();
	}
    }


    public final void
    setTabLayoutPolicy(int policy) {

	super.setTabLayoutPolicy(policy);
    }


    public final void
    setToolTipText(String text) {

	super.setToolTipText(text);
	if (text == null)
	    syncToolTips();
    }


    final synchronized void
    setTrackFocus(boolean state) {

	Component  comp;
	Iterator   iterator;
	int        count;
	int        n;

	if (state != trackfocus) {
	    if (state) {
		tabmap = new HashMap();
		focusowners = new HashMap();
		count = getTabCount();
		for (n = 0; n < count; n++) {
		    if ((comp = getComponentAt(n)) != null) {
			focusowners.put(comp, null);
			addFocusListenersTo(comp, comp);
		    }
		}
		addMouseListener(this);
		addContainerListener(this);
		trackfocus = true;
	    } else {
		trackfocus = false;
		removeMouseListener(this);
		removeContainerListener(this);
		for (iterator = tabmap.keySet().iterator(); iterator.hasNext(); ) {
		    comp = (Component)iterator.next();
		    comp.removeFocusListener(this);
		}
		tabmap = null;
		focusowners = null;
	    }
	}
    }


    final synchronized void
    syncTabProperty(Component comp, String name, Object value) {

	Color  color;
	int    index;

	if ((index = indexOfComponent(comp)) >= 0) {
	    if (name.equals(N_BACKGROUND)) {
		if (value instanceof Color)
		    color = (Color)value;
		else color = getBackground();
		setBackgroundAt(index, color);
	    } else if (name.equals(N_ENABLED)) {
		if (value instanceof YoixObject)
		    setEnabledAt(index, ((YoixObject)value).booleanValue());
		else if (value instanceof Boolean)
		    setEnabledAt(index, ((Boolean)value).booleanValue());
	    } else if (name.equals(N_FOREGROUND)) {
		if (value instanceof Color)
		    color = (Color)value;
		else color = getForeground();
		setForegroundAt(index, color);
	    } else if (name.equals(N_ICON)) { 
		if (value instanceof YoixObject)
		    value = YoixMake.javaIcon((YoixObject)value);
		if (value instanceof Icon)
		    setIconAt(index, (Icon)value);
		else setIconAt(index, null);
	    } else if (name.equals(N_TITLE)) {
		if (value instanceof String)
		    setTitleAt(index, (String)value);
		else setTitleAt(index, null);
	    } else if (name.equals(N_TOOLTIPTEXT)) {
		if (value instanceof String) {
		    if (index != getSelectedIndex())
			setToolTipTextAt(index, (String)value);
		} else setToolTipTextAt(index, null);
	    }
	}
    }


    final synchronized void
    syncToolTips() {

	Component  comp;
	boolean    register;
	String     text;
	int        selected;
	int        count;
	int        n;

	selected = getSelectedIndex();
	count = getTabCount();
	register = false;

	for (n = 0; n < count; n++) {
	    text = null;
	    if (n != selected) {
		if ((comp = getComponentAt(n)) != null) {
		    if (comp instanceof JComponent)
			text = ((JComponent)comp).getToolTipText();
		}
	    }
	    if (text != null) {
		register = true;
		setToolTipTextAt(n, text);
	    } else setToolTipTextAt(n, null);
	}

	if (register)
	    ToolTipManager.sharedInstance().registerComponent(this);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    addFocusListenersTo(Component component, Component tab) {

	Component  components[];
	Component  comp;
	int        n;

	if (component instanceof Container) {
	    if ((components = ((Container)component).getComponents()) != null) {
		for (n = 0; n < components.length; n++) {
		    comp = components[n];
		    if (comp.isFocusable()) {
			tabmap.put(comp, tab);
			if (comp.hasFocus())
			    focusowners.put(tab, comp);
			comp.addFocusListener(this);
			if (comp instanceof Container)
			    addFocusListenersTo((Container)comp, tab);
		    }
		}
	    }
	}
    }


    private int
    pickModel(boolean preferred) {

	Container  parent;
	int        model;

	//
	// This is where the final model selection is made, but only if the
	// appropriate locked flag isn't true. Picking the minsize model is
	// a little harder because we usually want to let JSplitPanes adjust 
	// our size down to zero, which is why we pick model 2 when we have
	// an ancestor that's a JSplitPane.
	//

	if (preferred == false) {
	    if (lockminsize_flag == false) {
		if (isMinimumSizeSet() == false) {
		    model = model_minsize;
		    for (parent = getParent(); parent != null; parent = parent.getParent()) {
			if (parent instanceof JSplitPane) {
			    model = 2;
			    break;
			}
		    }
		} else model = 0;
	    } else model = model_minsize;
	} else {
	    if (lockpreferredsize_flag == false) {
		if (isPreferredSizeSet() == false)
		    model = model_preferredsize;
		else model = 0;
	    } else model = model_preferredsize;
	}

	return(model);
    }


    private void
    removeFocusListenersFrom(Component component, Component tab) {

	Component  components[];
	Component  comp;
	int        n;

	if (component instanceof Container) {
	    if ((components = ((Container)component).getComponents()) != null) {
		for (n = 0; n < components.length; n++) {
		    comp = components[n];
		    if (tabmap.containsKey(comp)) {
			tabmap.remove(comp);
			if (focusowners.get(tab) == comp)
			    focusowners.put(tab, null);
			comp.removeFocusListener(this);
			if (comp instanceof Container)
			    removeFocusListenersFrom((Container)comp, tab);
		    }
		}
	    }
	}
    }


    private synchronized void
    startFocusTrackingFor(Component comp) {

	Component  tab;
	int        count;
	int        n;

	if (trackfocus) {
	    count = getTabCount();
	    for (n = 0; n < count; n++) {
		if ((tab = getComponentAt(n)) != null) {
		    if (SwingUtilities.isDescendingFrom(comp, tab)) {
			if (focusowners.containsKey(tab) == false)	// unnecessary
			    focusowners.put(tab, null);
			addFocusListenersTo(tab, tab);
		    }
		}
	    }
	}
    }


    private synchronized void
    stopFocusTrackingFor(Component comp) {

	Component  tab;

	if (trackfocus) {
	    if ((tab = (Component)tabmap.get(comp)) != null)
		removeFocusListenersFrom(comp, tab);
	}
    }
}

