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

class YoixAWTCheckboxMenuItemGroup {

    private YoixAWTCheckboxMenuItem  selected = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTCheckboxMenuItemGroup() {

    }

    ///////////////////////////////////
    //
    // YoixAWTCheckboxMenuItemGroup Methods
    //
    ///////////////////////////////////

    final YoixAWTCheckboxMenuItem
    getSelectedBox() {

	return(selected);
    }


    final synchronized void
    setSelectedBox(YoixAWTCheckboxMenuItem box) {

	YoixAWTCheckboxMenuItem  current;

	if (box == null || box.group == this) {
	    current = this.selected;
	    this.selected = box;
	    if ((current != null) && (current != box))
		current.setSuperState(false);
	}
    }
}

