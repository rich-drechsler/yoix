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

class YoixAWTTextArea extends TextArea

    implements YoixConstants

{

    //
    // First few tries at fixing TextArea size, much like what is done
    // in YoixAWTTextField didn't work everywhere so we backed them out.
    // Plan on trying again in the near future.
    //

    private YoixBodyComponent  parent;
    private YoixObject         data;

    private Integer lastcaret = null;
    private Color   background_set;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTTextArea(YoixObject data, YoixBodyComponent parent, int rows, int columns, int scroll) {

	super("", rows, columns, scroll);
	this.parent = parent;
	this.data = data;
    }

    ///////////////////////////////////
    //
    // YoixAWTTextArea Methods
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


    final synchronized int
    replaceText(int offset, int length, String str, boolean trim, boolean adjust, ArrayList undo) {

	String  dest;
	String  text;
	int     delta;

	dest = getText();
	offset = Math.max(0, Math.min(offset, dest.length()));
	length = Math.max(0, Math.min(length, dest.length() - offset));
	str = (str != null) ? str : "";

	if (length > 0 || offset < dest.length()) {
	    text = YoixMisc.replaceString(dest, offset, length, str, trim, undo);
	    setText(text);
	} else {
	    if (undo != null) {
		undo.add(new Integer(offset));
		undo.add(new Integer(str.length()));
		undo.add(dest.substring(offset, offset+length));
	    }
	    append(trim ? YoixMisc.trimWhiteSpace(str, false, true) : str);
	    text = getText();
	}

	delta = text.length() - dest.length();

	if (adjust) {
	    //
	    // A very recent change (10/25/04) from
	    //
	    //    setCaretPosition(Math.max(0, offset + delta));
	    //
	    // that imporves caret positioning for replaceText and
	    // deleteText builtins. Be suspicious if older related
	    // builtins, like appendText, misbehave. Change matches
	    // Swing implementation in YoixSwingHighlighter.java, so
	    // the behavior is consistent.
	    //
	    setCaretPosition(offset + str.length());
	}
	return(delta);
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
}

