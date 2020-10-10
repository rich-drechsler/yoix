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

class YoixSwingJSlider extends JSlider

    implements YoixConstants,
	       YoixConstantsJFC,
	       YoixConstantsSwing

{

    private boolean  ticksnap = false;
    private int      unitincrement = -1;
    private int      tickgap = 0;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJSlider() {

	// better than super()?
	super(JSlider.HORIZONTAL, 0, 100, 50);
    }


    public
    YoixSwingJSlider(int orientation) {

	// better than super(orientation)?
	super(orientation, 0, 100, 50);
    }


    public
    YoixSwingJSlider(int min, int max) {

	// better than super(min, max)?
	super(JSlider.HORIZONTAL, min, max, (min + max)/2);
    }


    public
    YoixSwingJSlider(int min, int max, int value) {

	// better than super(min, max, value)?
	super(JSlider.HORIZONTAL, min, max, value);
    }


    public
    YoixSwingJSlider(int orientation, int min, int max, int value) {

	super(orientation, min, max, value);
    }


    public
    YoixSwingJSlider(BoundedRangeModel brm) {

	super(brm);
    }

    ///////////////////////////////////
    //
    // YoixSwingJSlider Methods
    //
    ///////////////////////////////////

    public final boolean
    getSnapToTicks() {

	return(ticksnap);
    }


    public final int
    getUnitIncrement() {

	return(unitincrement);
    }


    public final void
    setMajorTickSpacing(int spacing) {

	int oldValue = majorTickSpacing;

	if (oldValue != spacing) {
	    synchronized(this) {
		if (ticksnap) {
		    tickgap = 1;
		    if (minorTickSpacing > 0) {
			tickgap = minorTickSpacing;
		    } else if (spacing > 0) {
			tickgap = spacing;
		    }
		}
		super.setMajorTickSpacing(spacing);
	    }
	}
    }


    public final void
    setMinorTickSpacing(int spacing) {

	int oldValue = minorTickSpacing;

	if (oldValue != spacing) {
	    synchronized(this) {
		if (ticksnap) {
		    tickgap = 1;
		    if (spacing > 0) {
			tickgap = spacing;
		    } else if (majorTickSpacing > 0) {
			tickgap = majorTickSpacing;
		    }
		}
		super.setMinorTickSpacing(spacing);
	    }
	}
    }


    public final void
    setSnapToTicks(boolean value) {

	boolean  oldval = ticksnap;

	ticksnap = value;
	if (oldval != ticksnap) {
	    if (ticksnap) {
		synchronized(this) {
		    tickgap = 1;
		    if (minorTickSpacing > 0) {
			tickgap = minorTickSpacing;
		    } else if (majorTickSpacing > 0) {
			tickgap = majorTickSpacing;
		    }
		}
	    } else tickgap = 0;
	}
    }


    public final void
    setUnitIncrement(int increment) {

	unitincrement = increment;
    }


    public final void
    setValue(int n) {

	BoundedRangeModel  m = getModel();
	int                gap;
	int                direction;
	int                incr = unitincrement;	// snapshot
	int                tgap = tickgap;		// snapshot
	int                oldValue;

	//
	// Want more control, but Java does not provide it. In particular,
	// there is no unitincrement and blockincrement options and the
	// SliderUI cannot be extended in a general way because there is no
	// way to do an extension using reflection and without that some
	// unexpected UI, say like Apple's, might turn up. The correct
	// solution would be for Java to include unitincrement and
	// blockincrement variables that can be set and then use them in
	// scrollByUnit and scrollByBlock in the UI. Also, turning on
	// snap-to-tick currently makes clicking in the track useless,
	// but - once again - that is handled in the UI with flexibility.
	// So, we try to handle these things in a kludgey manner here
	// in setValue, though there is no way to handle the blockincrement
	// problem, since we cannot tell at this level whether the user
	// happened to slide the equivalent of the UI's block increment or
	// whether it was the result of, say, a page-up key stroke.
	// Nonetheless, we can handle unitincrement passably and snap-to-tick
	// fairly well.
	//

	if ((oldValue = m.getValue()) == n)
	    return;

	if (oldValue > n) {
	    direction = -1;
	    gap = oldValue - n;
	} else {
	    direction = 1;
	    gap = n - oldValue;
	}

	// we know gap > 0, so use incr > 1 rather than incr > 0
	if (incr > 1 && gap < incr) {
	    gap = incr;
	}

	if (tickgap > 0) {
	    if (gap < tickgap) {
		gap = tickgap;
	    } else if (gap%tickgap != 0) {
		gap = Math.round(gap/tickgap) * tickgap;
	    }
	}

	n = oldValue + direction * gap;
	if (oldValue == n) {
	    return;
	}

	m.setValue(n);

	if (accessibleContext != null) {
	    accessibleContext.firePropertyChange(
		AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
		new Integer(oldValue),
		new Integer(m.getValue())
	    );
	}
    }
}

