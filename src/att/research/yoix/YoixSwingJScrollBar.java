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
import javax.accessibility.*;
import javax.swing.*;

class YoixSwingJScrollBar extends JScrollBar

    implements YoixConstants,
	       YoixConstantsJFC,
	       YoixConstantsSwing

{

    private YoixObject  data;

    //
    // This was added (on 1/15/11) to help performance when Yoix scrollbars
    // are attached to a JScrollPane. 
    //

    private JScrollPane  scrollpane = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJScrollBar(YoixObject data) {

	super();
	this.data = data;
    }

    ///////////////////////////////////
    //
    // YoixSwingJSlider Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	data = null;
	scrollpane = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    public final void
    setBlockIncrement(int value) {

	JScrollPane owner;
	JViewport   vp;
	Rectangle   rect;

	if (value <= 0) {
	    if ((owner = scrollpane) != null) {
		if ((vp = owner.getViewport()) != null) {
		    rect = vp.getViewRect();
		    if (getOrientation() == VERTICAL)
			value = rect.height;
		    else value = rect.width;
		}
	    }
	}

	super.setBlockIncrement(Math.max(value, 1));
    }


    final void
    setScrollPane(JScrollPane owner, int orientation) {

	if ((scrollpane = owner) != null) {
	    if (orientation == JScrollBar.VERTICAL) {
		setOrientation(orientation);
		syncBlockIncrement();
		syncUnitIncrement();
		owner.setVerticalScrollBar(this);
	    } else if (orientation == JScrollBar.HORIZONTAL) {
		setOrientation(orientation);
		syncBlockIncrement();
		syncUnitIncrement();
		owner.setHorizontalScrollBar(this);
	    }
	}
    }


    public final void
    setUnitIncrement(int value) {

	JScrollPane owner;
	JViewport   vp;
	Rectangle   rect;

	if (value <= 0) {
	    if ((owner = scrollpane) != null) {
		if ((vp = owner.getViewport()) != null) {
		    rect = vp.getViewRect();
		    if (getOrientation() == VERTICAL)
			value = rect.height/10;
		    else value = rect.width/10;
		}
	    }
	}

	super.setUnitIncrement(Math.max(value, 1));
    }


    public void
    setValues(int value, int extent, int min, int max) {

	super.setValues(value, extent, min, max);
	syncBlockIncrement();
	syncUnitIncrement();
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    syncBlockIncrement() {

	JScrollPane owner;
	JViewport   vp;
	Rectangle   rect;

	//
	// If this scrollbar is controlling a scrollpane and if the value
	// assigned to the blockincrement field is less than or equal to
	// zero then this method tries to sync the blockincrement to the
	// the appropriate dimensions of the scrollpane (actually to its
	// viewport). Behavior is reasonable, but it's not quite as good
	// as you get with the JScrollPane's default scrollbars.
	//

	if ((owner = scrollpane) != null) {
	    if ((vp = owner.getViewport()) != null) {
		rect = vp.getViewRect();
		if (getOrientation() == VERTICAL) {
		    if (data.getInt(N_BLOCKINCREMENT, -1) <= 0)
			super.setBlockIncrement(Math.max(rect.height, 1));
		} else {
		    if (data.getInt(N_BLOCKINCREMENT, -1) <= 0)
			super.setBlockIncrement(Math.max(rect.width, 1));
		}
	    }
	}
    }


    private void
    syncUnitIncrement() {

	JScrollPane owner;
	JViewport   vp;
	Rectangle   rect;

	//
	// If this scrollbar is controlling a scrollpane and if the value
	// assigned to the unitincrement field is less than or equal to
	// zero then this method tries to sync the unitincrement to the
	// the appropriate dimensions of the scrollpane (actually to its
	// viewport). Behavior is reasonable, but it's not quite as good
	// as you get with the JScrollPane's default scrollbars.
	//

	if ((owner = scrollpane) != null) {
	    if ((vp = owner.getViewport()) != null) {
		rect = vp.getViewRect();
		if (getOrientation() == VERTICAL) {
		    if (data.getInt(N_UNITINCREMENT, -1) <= 0)
			super.setUnitIncrement(Math.max(rect.height/10, 1));
		} else {
		    if (data.getInt(N_UNITINCREMENT, -1) <= 0)
			super.setUnitIncrement(Math.max(rect.width/10, 1));
		}
	    }
	}
    }
}

