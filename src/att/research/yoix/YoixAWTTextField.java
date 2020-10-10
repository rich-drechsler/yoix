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

class YoixAWTTextField extends TextField

    implements YoixConstants

{

    private YoixBodyComponent  parent;
    private YoixObject         data;

    private Integer  lastcaret = null;
    private Color    background_set;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTTextField(YoixObject data, YoixBodyComponent parent) {

	this.parent = parent;
	this.data = data;
    }

    ///////////////////////////////////
    //
    // YoixAWTTextField Methods
    //
    ///////////////////////////////////

    public final void
    addNotify() {

	super.addNotify();
	if (lastcaret != null) {
	    synchronized(this) {
		try {
		    super.setCaretPosition(lastcaret.intValue());
		}
		catch(RuntimeException e) {}
		finally {
		    lastcaret = null;
		}
	    }
	}
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


    public final Dimension
    getMaximumSize() {

	return(getLayoutSize(N_MAXIMUMSIZE, super.getMaximumSize()));
    }


    public final Dimension
    getMinimumSize() {

	return(getLayoutSize(N_MINIMUMSIZE, super.getMinimumSize()));
    }


    public final Dimension
    getPreferredSize() {

	return(getLayoutSize(N_PREFERREDSIZE, super.getPreferredSize()));
    }


    public final synchronized void
    setBackground(Color c) {

	background_set = c;

	if (c != null) {
	    if (YoixMisc.jvmCompareTo("1.3") < 0) {
		if (YoixMisc.jvmCompareTo("1.2") < 0 || ISWIN) {
		    if (isEditable())
			c = c.darker();
		}
	    }
	}

	super.setBackground(c);
    }


    public final synchronized void
    setCaretPosition(int position) {

	try {
	    super.setCaretPosition(position);
	}
	catch(RuntimeException e) {
	    lastcaret = new Integer(position);
	}
    }


    public final synchronized void
    setEditable(boolean state) {

	super.setEditable(state);
	setBackground(background_set);
    }


    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private Dimension
    getLayoutSize(String name, Dimension size) {

	YoixObject  obj;
	Dimension   defaultsize;

	if ((obj = data.getObject(name)) != null && obj.notNull()) {
	    defaultsize = size;
	    size = YoixMakeScreen.javaDimension(obj);
	    if (size.width <= 0 || size.height <= 0) {
		if (size.width < 0 || (size.width == 0 && name.equals(N_PREFERREDSIZE)))
		    size.width = (defaultsize != null) ? defaultsize.width : 0;
		if (size.height < 0 || (size.height == 0 && name.equals(N_PREFERREDSIZE)))
		    size.height = (defaultsize != null) ? defaultsize.height : 0;
	    }
	}

	return(size);
    }
}

