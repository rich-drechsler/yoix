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
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.plaf.ColorUIResource;

class YoixSwingJColorChooser extends JColorChooser

    implements YoixConstants,
               PropertyChangeListener

{

    //
    // Spent some time trying to make setPreviewPanel() behave, but
    // didn't have much luck, so most of the support code has been
    // removed from this file and from YoixBodyComponentSwing.java.
    //
    // Unfortunately, we also had trouble making a transparent color
    // chooser, despite the fact that we tried really hard (see our
    // implementation of setOpaque()). Everything worked nicely with
    // Java 1.3.1, but apparently JTabbedPane in 1.4.2 doesn't pay
    // attention when we set opaque to false. Didn't track it down,
    // but we decided not to delete setOpaque() because it did work
    // with 1.3.1.
    //

    private YoixBodyComponent  parent;
    private YoixObject         data;
    private Color              reset = null;

    private ChangeListener  listener = null;
    private ChangeEvent     changeevent = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJColorChooser(YoixObject data, YoixBodyComponent parent) {

	this.parent = parent;
	this.data = data;
	getPreviewPanel().addPropertyChangeListener(this);
    }

    ///////////////////////////////////
    //
    // PropertyChangeListener Methods
    //
    ///////////////////////////////////

    public void
    propertyChange(PropertyChangeEvent evt) {

	if (evt.getPropertyName().equals("foreground")) {
	    if (!evt.getOldValue().equals(evt.getNewValue()))
		fireStateChanged();
	}
    }

    ///////////////////////////////////
    //
    // YoixSwingJColorChooser Methods
    //
    ///////////////////////////////////

    public final void
    addChangeListener(ChangeListener l) {

	listenerList.add(ChangeListener.class, l);
    }


    public final ChangeListener[]
    getChangeListeners() {

	return((ChangeListener[])listenerList.getListeners(ChangeListener.class));
    }


    protected void
    finalize() {

	data = null;
	parent = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    final void
    fireStateChanged() {

	Object  listeners[];
	int     last;
	int     n;

	listeners = listenerList.getListenerList();
	last = listeners.length - 2;

	for (n = last; n >= 0; n -= 2) {
	    if (listeners[n] == ChangeListener.class) {
		//
		// The event is essentially content-free, so we can use
		// the same one over and over.
		//
		if (changeevent == null)
		    changeevent = new ChangeEvent(this);
		((ChangeListener)listeners[n+1]).stateChanged(changeevent);
	    }
	}
    }


    public final Color
    getReset() {

	if (reset == null)
	    reset = getColor();
	return(reset);
    }


    public final void
    removeChangeListener(ChangeListener l) {

	listenerList.remove(ChangeListener.class, l);
    }


    public final void
    setBackground(Color color) {

	setBackground(this, new ColorUIResource(color));
    }


    public final void
    setColor(Color color) {

	if (reset == null)
	    reset = color;
	super.setColor(color);
    }


    public final void
    setEnabled(boolean state) {

	setEnabled(this, state);
    }


    public final void
    setFont(Font font) {

	setFont(this, font);
    }


    public final void
    setForeground(Color color) {

	Color  current;

	current = getColor();
	setForeground(this, color);
	setColor(current);
    }


    public final void
    setOpaque(boolean state) {

	if (data != null) {
	    setOpaque(this, state, data.getInt(N_OPAQUEFLAGS, 0));
	    repaint();
	}
    }


    public final void
    setReset(Color color) {

	Color  base = getReset();
	Font   font;

	if (color == null)
	    color = base;
	else reset = color;

	super.setColor(color);
	if (!base.equals(color)) {
	    font = getPreviewPanel().getFont();
	    getPreviewPanel().removePropertyChangeListener(this);
	    setPreviewPanel(null);
	    // need to restore same font since new panel could have a different one
	    getPreviewPanel().addPropertyChangeListener(this);
	    getPreviewPanel().setFont(font);
	    getPreviewPanel().setBackground(getBackground());
	    revalidate();
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    setBackground(Container container, Color color) {

	Component  components[];
	Component  component;
	Component  preview;
	int        n;

	if (container != null) {
	    components = container.getComponents();
	    for (n = 0; n < components.length; n++) {
		if ((component = components[n]) != null) {
		    if (!(component instanceof JTextComponent)) {
			component.setBackground(color);
			if (component instanceof Container) {
			    if (component instanceof JTabbedPane) {
				if (color instanceof ColorUIResource)
				    setBackground((Container)component, new Color(color.getRGB()));
				else setBackground((Container)component, color);
			    } else setBackground((Container)component, color);
			}
		    }
		}
	    }
	    if (container == this)
		super.setBackground(color);
	    else container.setBackground(color);
	}
    }


    private void
    setEnabled(Container container, boolean state) {

	Component  components[];
	Component  component;
	int        n;

	if (container != null) {
	    components = container.getComponents();
	    for (n = 0; n < components.length; n++) {
		if ((component = components[n]) != null) {
		    component.setEnabled(state);
		    if (component instanceof Container)
			setEnabled((Container)component, state);
		}
	    }
	    if (container == this)
		super.setEnabled(state);
	    else container.setEnabled(state);
	}
    }


    private void
    setFont(Container container, Font font) {

	Component  components[];
	Component  component;
	int        n;

	if (container != null) {
	    components = container.getComponents();
	    for (n = 0; n < components.length; n++) {
		if ((component = components[n]) != null) {
		    component.setFont(font);
		    if (component instanceof Container)
			setFont((Container)component, font);
		}
	    }
	    if (container == this)
		super.setFont(font);
	    else container.setFont(font);
	}
    }


    private void
    setForeground(Container container, Color color) {

	Component  components[];
	Component  component;
	int        n;

	if (container != null) {
	    components = container.getComponents();
	    for (n = 0; n < components.length; n++) {
		if ((component = components[n]) != null) {
		    if (component != getPreviewPanel()) {	// recent addition
			component.setForeground(color);
			if (component instanceof Container)
			    setForeground((Container)component, color);
		    }
		}
	    }
	    if (container == this)
		super.setForeground(color);
	    else container.setForeground(color);
	}
    }


    private void
    setOpaque(Container container, boolean state, int flags) {

	Component  components[];
	Component  component;
	int        n;

	//
	// Small chance this may no longer be needed, but things also seem
	// to behave properly now that the JTabbedPane gets extra attention.
	//

	if (container != null) {
	    components = container.getComponents();
	    for (n = 0; n < components.length; n++) {
		component = components[n];
		if (component instanceof JTextComponent) {
		    if ((flags&0x01) != 0)
			component = null;
		}
		if (component instanceof JScrollBar) {
		    if ((flags&0x02) != 0)
			component = null;
		}
		if (component instanceof JComponent) {
		    if (component instanceof JTabbedPane)
			((JComponent)component).setOpaque(false);
		    else ((JComponent)component).setOpaque(state);
		}
		if (component instanceof Container)
		    setOpaque((Container)component, state, flags);
	    }
	    if (container instanceof JComponent) {
		if (container == this)
		    super.setOpaque(state);
		else if (!(container instanceof JTabbedPane))
		    ((JComponent)container).setOpaque(state);
	    }
	}
    }
}

