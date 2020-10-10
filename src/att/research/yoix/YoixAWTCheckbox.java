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
import java.util.ArrayList;

class YoixAWTCheckbox extends Checkbox

    implements YoixConstants

{

    //
    // Currently only needed so that reading the selected field in a
    // CheckboxGroup can map an AWT Checkbox to the Yoix Checkbox.
    //

    private YoixBodyComponent  parent;
    private YoixObject         data;

    private String  command = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTCheckbox(YoixObject data, YoixBodyComponent parent) {

	super();
	this.parent = parent;
	this.data = data;
    }

    ///////////////////////////////////
    //
    // YoixAWTCheckbox Methods
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


    final String
    getActionCommand() {

	return(command != null ? command : getLabel());
    }


    final YoixBodyComponent
    getBody() {

	return(parent);
    }


    final void
    setActionCommand(String str) {

	command = (str != null && str.length() > 0) ? str : null;
    }
}

