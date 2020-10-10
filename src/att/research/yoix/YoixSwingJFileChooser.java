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
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.plaf.ColorUIResource;

class YoixSwingJFileChooser extends JFileChooser

    implements ActionListener,
	       YoixConstants,
	       YoixInterfaceFileChooser

{

    private YoixBodyComponent  parent;
    private YoixObject         data;

    private static final int  RUN_JVIEWPORT = 1;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJFileChooser(YoixObject data, YoixBodyComponent parent) {

	this.parent = parent;
	this.data = data;
	addActionListener(this);
	setDoubleBuffered(true);	// currently required
	doListListenerFix();
    }

    ///////////////////////////////////
    //
    // ActionListener Methods
    //
    ///////////////////////////////////

    public final void
    actionPerformed(ActionEvent e) {

	File  dir;

	if (CANCEL_SELECTION.equals(e.getActionCommand())) {
	    dir = getCurrentDirectory();
	    setSelectedFile(null);
	    setCurrentDirectory(dir);
	}
    }

    ///////////////////////////////////
    //
    // YoixInterfaceFileChooser Methods
    //
    ///////////////////////////////////

    public final void
    setCurrentDirectory(File dir) {

	if (dir == null) {
	    super.setSelectedFile(new File(""));
	    super.setSelectedFile(null);
	}
	super.setCurrentDirectory(dir);
    }


    public final void
    setSelectedFile(File file) {

	boolean  symlink;
	File     parent;
	File     dir;

	if (file != null) {
	    if (file.isAbsolute() && file.isDirectory()) {
		//
		// Symbolic link type situation - needed by Mac.
		//
		symlink = getFileSystemView().isParent(getCurrentDirectory(), file);
	    } else symlink = false;
	    if (symlink) {
		super.setSelectedFile(new File(""));
		super.setSelectedFile(null);
	    } else {
		super.setSelectedFile(file);
		if ((dir = getCurrentDirectory()) != null) {
		    if ((parent = file.getParentFile()) != null) {
			try {
			    parent = parent.getCanonicalFile();
			    dir = dir.getCanonicalFile();
			    if (dir.equals(parent) == false) {
				super.setSelectedFile(new File(""));
				super.setSelectedFile(null);
			    }
			}
			catch(IOException e) {
			    super.setSelectedFile(new File(""));
			    super.setSelectedFile(null);
			}
		    }
		}
	    }
	} else {
	    super.setSelectedFile(new File(""));
	    super.setSelectedFile(null);
	    //super.setCurrentDirectory(null); // breaks Mac behavior
	}
    }

    ///////////////////////////////////
    //
    // YoixSwingJFileChooser Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	data = null;
	parent = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    public final void
    handleRun(Object args[]) {

	if (args != null && args.length > 0) {
	    switch (((Integer)args[0]).intValue()) {
		case RUN_JVIEWPORT:
		    handleJViewport((JViewport)args[1], (Point)args[2]);
		    break;
	    }
	}
    }


    public final void
    setBackground(Color color) {

	setBackground(this, new ColorUIResource(color));
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

	setForeground(this, color);
    }


    public final void
    setOpaque(boolean state) {

	if (data != null) {
	    setOpaque(this, state, data.getInt(N_OPAQUEFLAGS, 0));
	    repaint();
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    doListListenerFix() {

	recurseDoListListenerFix(this, null, null);
    }


    private void
    handleJViewport(JViewport jvp, Point pt) {

	jvp.setViewPosition(pt);
    }


    private boolean
    recurseDoListListenerFix(Container cont, JViewport jvp, Component comp) {

	MouseListener  listeners[];
	Component      comps[];
	int            n;

	if (ISMAC) {
	    comps = cont.getComponents();
	    for (n = 0; n < comps.length; n++) {
		if (jvp == null && comps[n] instanceof JViewport) {
		    jvp = (JViewport)comps[n];
		    if (recurseDoListListenerFix((Container)comps[n], jvp, comp))
			return(true);
		} else if (comp == null && (comps[n] instanceof JList || comps[n] instanceof JTable))
		    comp = comps[n];
		else if ((jvp == null || comp == null) && comps[n] instanceof Container && ((Container)comps[n]).getComponentCount() > 0) {
		    if (recurseDoListListenerFix((Container)comps[n], jvp, comp))
			return(true);
		} else if (jvp != null && comp != null)
		    break;
	    }
	    if (jvp != null && comp != null) {
		listeners = comp.getMouseListeners();
		for (n = 0; n < listeners.length; n++) {
		    if (listeners[n].getClass().getName().endsWith("MouseInputHandler")) {
			comp.removeMouseListener(listeners[n]);
			comp.addMouseListener(new MouseFixer(comp, jvp, listeners[n]));
			break;
		    }
		}
		return(true);
	    }
	}
	return(false);
    }


    private void
    setBackground(Container container, Color color) {

	Component  components[];
	Component  component;
	int        n;

	//
	// The JViewport test is a quick way to skip over the file name
	// display - works, but it definitely could be improved.
	//

	if (container != null) {
	    components = container.getComponents();
	    for (n = 0; n < components.length; n++) {
		if ((component = components[n]) != null) {
		    if (!(component instanceof JTextComponent)) {
			component.setBackground(color);
			if (component instanceof Container) {
			    if (!(component instanceof JViewport))
				setBackground((Container)component, color);
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
		    component.setForeground(color);
		    if (component instanceof Container)
			setForeground((Container)component, color);
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
	// The JViewport test is a quick way to skip over the file name
	// display - works, but it definitely could be improved.
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
		if (component instanceof JComponent)
		    ((JComponent)component).setOpaque(state);
		if (component instanceof Container) {
		    if (!(component instanceof JViewport))
			setOpaque((Container)component, state, flags);
		}
	    }
	    if (container instanceof JComponent) {
		if (container == this)
		    super.setOpaque(state);
		else ((JComponent)container).setOpaque(state);
	    }
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class MouseFixer

        implements MouseListener

    {

	Component      comp;
	JViewport      jvp;
	MouseListener  listener;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	MouseFixer(Component comp, JViewport jvp, MouseListener listener) {

	    this.comp = comp;
	    this.jvp = jvp;
	    this.listener = listener;
	}

	///////////////////////////////////
	//
	// MouseListener Methods
	//
	///////////////////////////////////

	public void
	mouseClicked(MouseEvent e) {

	    listener.mouseClicked(e);
	}


	public void
	mouseEntered(MouseEvent e) {

	    listener.mouseEntered(e);
	}


	public void
	mouseExited(MouseEvent e) {

	    listener.mouseExited(e);
	}


	public void
	mousePressed(MouseEvent e) {

	    listener.mousePressed(e);
	}


	public void
	mouseReleased(MouseEvent e) {

	    Point  pt;

	    if (ISMAC) {
		pt = jvp.getViewPosition();
		listener.mouseReleased(e);
		EventQueue.invokeLater(
		    new YoixAWTInvocationEvent(
			YoixSwingJFileChooser.this,
			new Object[] { new Integer(RUN_JVIEWPORT), jvp, pt }
		    )
		);
	    } else listener.mouseReleased(e);
	}
    }
}

