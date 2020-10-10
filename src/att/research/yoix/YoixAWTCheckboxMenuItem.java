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

class YoixAWTCheckboxMenuItem extends CheckboxMenuItem {

    YoixAWTCheckboxMenuItemGroup  group;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTCheckboxMenuItem() {

	super();
	group = null;
    }


    YoixAWTCheckboxMenuItem(String label) {

	super(label);
	group = null;
    }


    YoixAWTCheckboxMenuItem(String label, boolean state) {

	super(label, state);
	group = null;
    }


    YoixAWTCheckboxMenuItem(String label, boolean state, YoixAWTCheckboxMenuItemGroup group) {

	super(label, state);
	this.group = group;
	if (state && group != null)
	    group.setSelectedBox(this);
    }

    ///////////////////////////////////
    //
    // YoixAWTCheckboxMenuItem Methods
    //
    ///////////////////////////////////

    final YoixAWTCheckboxMenuItemGroup
    getGroup() {

	return(group);
    }


    public final void
    setState(boolean state) {

	setState(state, true);
    }


    final void
    setState(boolean state, boolean selectedstate) {

    	YoixAWTCheckboxMenuItemGroup  group = this.group;

	if (group != null) {
	    if (state) {
		group.setSelectedBox(this);
		super.setState(true);
	    } else if (group.getSelectedBox() == this)
		super.setState(selectedstate);
	    else super.setState(false);
	} else super.setState(state);
    }


    final void
    setSuperState(boolean state) {

	super.setState(state);
    }
}

