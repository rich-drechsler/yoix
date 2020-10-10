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
import javax.swing.*;

class YoixSwingJSplitPane extends JSplitPane

    implements ComponentListener

{

    //
    // Much this nonsense is so we can adjust the divider's location more
    // gracefully than what's done in Java's JSplitPane class.
    //
    // The keephidden related code was added on 1/21/11 and helps prevent
    // the divider from jumping around when it looks like it's supposed
    // to be pinned to an end of the JSplitPane. Since we use getInsets()
    // to make the keephidden decision there's probably a bit more that
    // should be done to support setDividerLocation() calls that originate
    // directly from Yoix scripts. It doesn't seem like an urgent fix, so
    // we're going to wait even though the fix is probably easy.
    //

    private boolean  componentsized = false;
    private boolean  isfraction = false;
    private boolean  keephidden = true;
    private boolean  dividerlocked = false;
    private double   dividerlocation = 0.0;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJSplitPane() {

	super();
	addComponentListener(this);
    }

    ///////////////////////////////////
    //
    // ComponentListener Methods
    //
    ///////////////////////////////////

    public void
    componentHidden(ComponentEvent e) {

    }


    public void
    componentMoved(ComponentEvent e) {

    }


    public void
    componentResized(ComponentEvent e) {

	if (componentsized == false) {
	    removeComponentListener(this);
	    componentsized = true;
	    if (isfraction)
	        setDividerLocation(dividerlocation);
	    else setDividerLocation((int)dividerlocation);
	    validate();
	}
    }


    public void
    componentShown(ComponentEvent e) {

    }

    ///////////////////////////////////
    //
    // YoixSwingJSplitPane Methods
    //
    ///////////////////////////////////

    final int
    getDividerLocation(boolean ui) {

	return(ui ? getUI().getDividerLocation(this) : getDividerLocation());
    }


    final boolean
    getDividerLocked() {

	return(dividerlocked);
    }


    public final void
    setDividerLocation(int location) {

	Component  comp;
	Insets     insets;
	int        orientation;

	if (dividerlocked)
	    location = super.getDividerLocation();

	if (componentsized == false) {
	    isfraction = false;
	    dividerlocation = location;
	} else {
	    super.setDividerLocation(location);
	    if (keephidden) {
		orientation = getOrientation();
		if ((insets = getInsets()) == null)
		    insets = new Insets(0, 0, 0, 0);
		if ((comp = getLeftComponent()) != null) {
		    if (orientation == HORIZONTAL_SPLIT)
			comp.setVisible(location < 0 || location > insets.left);
		    else comp.setVisible(location < 0 || location > insets.top);
		}
		if ((comp = getRightComponent()) != null) {
		    if (orientation == HORIZONTAL_SPLIT)
			comp.setVisible(location < getWidth() - getDividerSize() - insets.left);
		    else comp.setVisible(location < getHeight() - getDividerSize() - insets.top);
		}
	    }
	}
    }


    public final void
    setDividerLocation(double value) {

	if (dividerlocked == false) {
	    if (componentsized == false) {
		isfraction = true;
		dividerlocation = value;
	    } else super.setDividerLocation(Math.max(0.0, Math.min(1.0, value)));
	} else {
	    if (componentsized == false) {
		isfraction = true;
		dividerlocation = value;
	    } else super.setDividerLocation(super.getDividerLocation());
	}
    }


    final void
    setDividerLocked(boolean state) {

	dividerlocked = state;
    }


    final void
    setKeepHidden(boolean state) {

	if (state) {
	    keephidden = state;
	    //
	    // Eventually sync visibility of the components based on the
	    // divider's current location.
	    //
	} else {
	    keephidden = state;
	    //
	    // Eventually both components should be made visible, but if
	    // possible it should only happen if it looks like they were
	    // hidden because of the divider code.
	    //
	}
    }
}

