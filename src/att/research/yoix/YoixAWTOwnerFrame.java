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

class YoixAWTOwnerFrame extends Frame

    implements YoixConstants
{

    //
    // A special Frame that we use when we want to create a JWindow or
    // Window that can also get the focus. The magic is in isShowing().
    //

    private YoixObject  data;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTOwnerFrame(YoixObject data) {

	super();
	this.data = data;
    }

    ///////////////////////////////////
    //
    // YoixWindowOwner Methods
    //
    ///////////////////////////////////

    protected final void
    finalize() {

	data = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    public final boolean
    isShowing() {

	YoixObject  obj;
	YoixObject  focusable;
	boolean     result;

	//
	// The old version did
	//
	//     return((obj = data) != null
	//         ? obj.getBoolean(N_FOCUSABLE, false) || super.isShowing()
	//         : super.isShowing()
	//     );
	//
	// and windows had a focusable field that was initialized to true.
	// We recently added a focusable field to all yoix components and
	// made NULL the default to avoid calling Component.setFocusable()
	// unless absolutely necessary. The changes here try to compensate 
	// compensate for the new initial value assigned to the focusable
	// field in a window. Changes were made on 6/7/10.
	//
	// NOTE - not convinced this is needed, but decided to leave it in
	// for now.
	// 

	if ((result = super.isShowing()) == false) {
	    if ((obj = data) != null) {
		if ((focusable = obj.getObject(N_FOCUSABLE)) != null) {
		    if (focusable.isNull() || focusable.booleanValue())
			result = true;
		}
	    }
	}
	return(result);
    }


    public final void
    show() {

    }
}

