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
import javax.swing.*;
import javax.swing.text.*;

class YoixSwingHighlighter extends DefaultHighlighter

    implements YoixConstantsSwing

{

    private JTextComponent  owner = null;

    //
    // This class provides some control over the text highlighting that
    // comes with all Swing JTextComponents. Clearing flags means the
    // installed JTextComponent behaves normally. Setting IGNORE_REMOVES
    // means the current highlights, if there are any, won't be cleared
    // when the JTextComponent loses the focus. Setting IGNORE_CHANGES
    // means highlights will not be added or changed when the user drags
    // the caret across the displayed text. Setting the ENABLE_MULTIMODE
    // flag means accept more than one highlighted region. All bits off
    // or all bits on are currently the most common settings, but other
    // settings could be useful.
    //

    private int  flags = 0;

    private static final int  IGNORE_REMOVES = 0x01;
    private static final int  IGNORE_CHANGES = 0x02;
    private static final int  ENABLE_MULTIMODE = 0x04;
    private static final int  ENABLE_EMPTYACTION = 0x08;
    private static final int  ENABLE_CARET = 0x10;
    private static final int  ENABLE_CARET_OWNER = 0x20;
    private static final int  ENABLE_CARET_OPENRIGHT = 0x40;
    private static final int  ENABLE_CARET_OPENLEFT = 0x80;	// unused

    private static final int  DISABLE_CARET_POSITIONING = 0x100;

    //
    // Remember the highlight that's supposed to represent the caret,
    // if there is one. caret_position is a recent addition that was
    // really only added so getCaretPosition() could return something
    // reasonable when the DISABLE_CARET_POSITIONING flag is set. We
    // may expand it's use in the near future.
    //

    private Object  caret = null;
    private Object  caret_owner = null;
    private int     caret_width = 1;
    private int     caret_position = -1;

    ///////////////////////////////////
    //
    // YoixSwingHighlighter Methods
    //
    ///////////////////////////////////

    public final Object
    addHighlight(int p0, int p1, HighlightPainter p) 

	throws BadLocationException

    {

	Object  tag = null;

	if ((flags&IGNORE_CHANGES) == 0) {
	    if ((flags&ENABLE_MULTIMODE) == 0)
		reset();
	    tag = super.addHighlight(p0, p1, p);
	}

	return(tag);
    }


    public final void
    changeHighlight(Object tag, int p0, int p1)

	throws BadLocationException

    {

	if ((flags&IGNORE_CHANGES) == 0) {
	    if (tag != null)
		super.changeHighlight(tag, p0, p1);
	}
    }


    public final synchronized void
    deinstall(JTextComponent c) {

	reset();
	owner = null;
	super.deinstall(c);
    }


    final synchronized Color
    getCaretColor() {

	Object  painter;
	Color   color = null;

	if (hasCaret()) {
	    if (caret instanceof Highlighter.Highlight) {	// always true?
		painter = ((Highlighter.Highlight)caret).getPainter();
		if (painter instanceof DefaultHighlightPainter)
		    color = ((DefaultHighlightPainter)painter).getColor();
	    }
	} else if (owner != null)
	    color = owner.getCaretColor();

	return(color != null ? color : getDefaultCaretColor());
    }


    final synchronized Color
    getCaretOwnerColor() {

	Object  painter;
	Color   color = null;

	if (hasCaret()) {
	    if (caret_owner instanceof Highlighter.Highlight) {	// always true?
		painter = ((Highlighter.Highlight)caret_owner).getPainter();
		if (painter instanceof DefaultHighlightPainter)
		    color = ((DefaultHighlightPainter)painter).getColor();
	    }
	    //
	    // A null return, for now, will mean no caret owner.
	    //
	    if (color == null) {
		if (owner != null)
		    color = owner.getSelectionColor();
		if (color == null)
		    color = owner.getBackground();
	    }
	}

	return(color);
    }


    final synchronized int
    getCaretPosition() {

	int  pos;

	if ((flags&DISABLE_CARET_POSITIONING) == 0) {
	    if (hasCaret() == false) {
		if (owner != null)
		    pos = owner.getCaretPosition();
		else pos = -1;
	    } else pos = ((Highlighter.Highlight)caret).getStartOffset();
	} else pos = caret_position;

	return(pos);
    }


    final synchronized Color
    getDefaultCaretColor() {

	Object  highlights[];
	Color   color = null;

	//
	// Probably don't want to return null to the caller, so this
	// is harder than it should be. There's undoubtedly a better
	// way to pick the color when Java says it's null.
	// 

	if (hasCaret()) {
	    if (owner != null)
		color = owner.getSelectionColor();
	    if (color == null)
		color = Color.gray;		// what??
	} else {
	    if (owner != null)
		color = owner.getCaretColor();
	    if (color == null)
		color = Color.black;		// what??
	}

	return(color);
    }


    final int
    getFlags() {

	return(flags);
    }


    final synchronized Object[]
    getHighlights(int fields) {

	return(getHighlights(fields, 0));
    }


    final synchronized Object[]
    getHighlights(int fields, int flags) {

	Highlight  highlights[];
	Highlight  highlight;
	Object     ends[] = null;
	Object     tmp[];
	Object     painter;
	int        skipped = 0;
	int        count;
	int        type;
	int        m;
	int        n;

	if (fields > 1) {
	    if ((highlights = super.getHighlights()) != null) {
		if ((count = highlights.length) > 0) {
		    ends = new Object[fields*count];
		    skipped = 0;
		    for (n = 0; n < count; n++) {
			m = n*fields;
			highlight = highlights[n];
			if (highlight == caret)
			    type = 1;
			else if (highlight == caret_owner)
			    type = 2;
			else type = 4;
			if (flags == 0 || (flags&type) != 0) {
			    ends[m] = new Integer(highlight.getStartOffset());
			    ends[m+1] = new Integer(highlight.getEndOffset());
			    if (fields > 2) {
				painter = highlight.getPainter();
				if (painter instanceof DefaultHighlightPainter)
				    ends[m+2] = ((DefaultHighlightPainter)painter).getColor();
				else ends[m+2] = null;
				if (fields > 3)
				    ends[m+3] = new Integer(type);
			    }
			} else skipped += fields;
		    }
		    if (skipped > 0) {
			tmp = new Object[ends.length - skipped];
			for (m = 0, n = 0; n < ends.length; n++) {
			    if (ends[n] != null)
				tmp[m++] = ends[n];
			}
			ends = tmp;
		    }
		}
	    }
	}

	return(ends);
    }


    public final synchronized void
    install(JTextComponent c) {

	owner = c;
	super.install(c);
    }


    public final void
    removeAllHighlights() {

	if ((flags&IGNORE_REMOVES) == 0)
	    super.removeAllHighlights();
    }


    public final void
    removeHighlight(Object tag) {

	if ((flags&IGNORE_REMOVES) == 0) {
	    if ((flags&ENABLE_MULTIMODE) == 0) {
		if (tag != null)
		    super.removeHighlight(tag);
	    }
	}
    }


    final synchronized void
    repaintHighlights() {

	Object  ends[];
	int     fields = 4;

	if ((ends = getHighlights(fields, 0)) != null)
	    setHighlights(ends, fields);
    }


    final synchronized int
    replaceText(int offset, int length, String str, boolean adjust, ArrayList undo) {

	Document  doc;
	int       start;
	int       end;
	int       delta = 0;

	if (owner != null && str != null) {
	    if ((doc = owner.getDocument()) != null) {
		if (caret_owner == null) {
		    start = 0;
		    end = doc.getLength();
		} else {
		    start = ((Highlighter.Highlight)caret_owner).getStartOffset();
		    end = ((Highlighter.Highlight)caret_owner).getEndOffset();
		}
		try {
		    str = (str != null) ? str : "";
		    offset = Math.max(start, Math.min(offset, end));
		    length = Math.max(0, Math.min(length, end - offset));
		    if (undo != null) {
			undo.add(new Integer(offset));
			undo.add(new Integer(str.length()));
			undo.add(doc.getText(offset, length));
		    }
		    if (length > 0)
			doc.remove(offset, length);
		    doc.insertString(offset, str, null);
		    delta = str.length() - length;
		    //
		    // Apparently Java needs some help, but only if we
		    // inserted text?? Kludge that was added quickly and
		    // needs to be checked again. First try required a
		    // positive delta, which we don't think was correct.
		    //
		    if (offset == start) {
			if (caret_owner != null)
			    super.changeHighlight(caret_owner, offset, end + delta);
		    }
		    if (adjust)
			setCaretPosition(offset + str.length());
		}
		catch(BadLocationException e) {}
		catch(NullPointerException e) {
		    //
		    // This can happen when owner is visible but has zero
		    // size, which isn't as unusual as you might expect.
		    // For example, put owner in a JSplitPane and it will
		    // be easy to get here. Problem seems to happen in
		    //
		    //    javax.swing.plaf.basic.BasicTextUI.damageRange()
		    //
		    // after getVisibleEditorRect() returns a null value
		    // (because owner has zero size) but damageRange()
		    // uses the return value without checking for null.
		    //
		    // Looks like Sun fixed the problem in 1.5.0, but we
		    // still need this because we support older versions.
		    //
		}
	    }
	}

	return(delta);
    }


    final synchronized void
    setCaretColor(Color color) {

	if (setHighlightColor(caret, color) == false) {
	    if (owner != null)
		owner.setCaretColor(color);
	}
    }


    final synchronized void
    setCaretOwnerColor(Color color) {

	setHighlightColor(caret_owner, color);
    }


    final synchronized void
    setCaretPosition(int offset) {

	Document  doc;
	int       end;
	int       start;

	if ((flags&DISABLE_CARET_POSITIONING) == 0) {
	    if (hasCaret()) {
		if (caret_owner == null) {
		    start = 0;
		    if (owner != null && (doc = owner.getDocument()) != null)
			end = doc.getLength();
		    else end = start;
		} else {
		    start = ((Highlighter.Highlight)caret_owner).getStartOffset();
		    end = ((Highlighter.Highlight)caret_owner).getEndOffset();
		    if (start <= end) {
			if ((flags & ENABLE_CARET_OPENRIGHT) != 0)
			    end += 1;
		    }
		} 
		if (start <= end) {		// not 100% sure about this
		    start = Math.min(Math.max(offset, start), end - caret_width);
		    end = Math.min(start + caret_width, end);
		    try {
			super.changeHighlight(caret, start, end);
		    }
		    catch(BadLocationException e) {}
		    catch(NullPointerException e) {
			//
			// This can happen when owner is visible but has zero
			// size, which isn't as unusual as you might expect.
			// For example, put owner in a JSplitPane and it will
			// be easy to get here. Problem seems to happen in
			//
			//    javax.swing.plaf.basic.BasicTextUI.damageRange()
			//
			// after getVisibleEditorRect() returns a null value
			// (because owner has zero size) but damageRange()
			// uses the return value without checking for null.
			//
			// Looks like Sun fixed the problem in 1.5.0, but we
			// still need this because we support older versions.
			//
		    }
		}
	    } else if (owner != null) {
		try {
		    owner.setCaretPosition(offset < 0 ? 0 : offset);
		}
		catch(RuntimeException e) {
		    try {
			offset = owner.getText().length();
			owner.setCaretPosition(offset);
		    }
		    catch(RuntimeException ee) {}
		}
	    }
	}
    }


    final synchronized void
    setFlags(int value) {

	boolean  empty;

	if (flags != value) {
	    if (((flags ^ value) & ENABLE_EMPTYACTION) != 0) {
		empty = ((value & ENABLE_EMPTYACTION) != 0);
		if (owner instanceof YoixSwingJTextArea)
		    ((YoixSwingJTextArea)owner).setEmptyAction(empty);
		else if (owner instanceof YoixSwingJTextField)
		    ((YoixSwingJTextField)owner).setEmptyAction(empty);
	    }
	    flags = value;
	    reset();
	}
    }


    final synchronized void
    setHighlights(Object ends[], int fields) {

	boolean  accumulate;
	Object   tag;
	Color    color;
	int      type;
	int      count;
	int      start;
	int      end;
	int      left = -1;
	int      right;
	int      width;
	int      m;
	int      n;

	reset();
	if (ends != null && fields > 1) {
	    accumulate = (flags&ENABLE_MULTIMODE) != 0;
	    count = ends.length/fields;
	    for (n = 0; n < count; n++) {
		m = fields*n;
		if (ends[m] instanceof Number && ends[m+1] instanceof Number) {
		    start = ((Number)ends[m]).intValue();
		    end = ((Number)ends[m+1]).intValue();
		    color = (fields > 2) ? (Color)ends[m+2] : null;
		    type = (fields > 3) ? ((Number)ends[m+3]).intValue() : 0;
		    if (start <= end) {
			if (accumulate || n == count - 1) {
			    tag = null;
			    try {
				if (color != null) {
				    tag = super.addHighlight(
					start,
					end,
					new DefaultHighlightPainter(color)
				    );
				} else tag = super.addHighlight(start, end, DefaultPainter);
			    }
			    catch(BadLocationException e) {}
			    catch(NullPointerException e) {
				Object highlights[] = super.getHighlights();

				//
				// This can happen when owner is visible but has zero
				// size, which isn't as unusual as you might expect.
				// For example, put owner in a JSplitPane and it will
				// be easy to get here. Problem seems to happen in
				//
				//    javax.swing.plaf.basic.BasicTextUI.damageRange()
				//
				// after getVisibleEditorRect() returns a null value
				// (because owner has zero size) but damageRange()
				// uses the return value without checking for null.
				//
				// Turns out just catching NullPointerException isn't
				// a complete solution, because super.addHighlight()
				// doesn't get a chance to return the tag, so we try
				// to find the tag by assuming it's the last one in
				// the array that super.getHighlights() returns.
				//
				// Looks like Sun fixed the problem in 1.5.0, but we
				// still need this because we support older versions.
				//

				if (highlights != null && highlights.length > 0)
				    tag = highlights[highlights.length - 1];
			    }
			    if (tag != null && (flags&ENABLE_CARET) != 0) {
				if (type == 1) {
				    left = ((Highlighter.Highlight)tag).getStartOffset();
				    right = ((Highlighter.Highlight)tag).getEndOffset();
				    caret = tag;
				    caret_width = right - left;
				    caret_position = left;
				} else if (type == 2)
				    caret_owner = tag;
			    }
			}
		    }
		}
	    }

	    //
	    // A recent addition
	    //

	    if ((flags&ENABLE_CARET_OWNER) != 0) {
		if (caret_owner == null) {
		    caret = null;
		    caret_width = 0;
		}
	    } else caret_owner = null;
	}

	//
	// This probably is unnecessary...
	//
	if (owner != null && caret_position >= 0) {
	    try {
		owner.setCaretPosition(caret_position);
	    }
	    catch(RuntimeException e) {}
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private final boolean
    hasCaret() {

	return(caret != null);
    }


    private synchronized void
    reset() {

	super.removeAllHighlights();
	caret = null;
	caret_owner = null;
	caret_width = 0;
	caret_position = -1;
    }


    private synchronized boolean
    setHighlightColor(Object tag, Color color) {

	boolean  result = false;
	Object   highlights[];
	int      fields = 4;
	int      n;

	if (tag != null) {
	    if ((highlights = super.getHighlights()) != null) {
		for (n = 0; n < highlights.length; n++) {
		    if (highlights[n] == tag) {
			if ((highlights = getHighlights(fields, 0)) != null) {
			    n = fields*n + 2;		// color location
			    if (n < highlights.length) {
				highlights[n] = color;
				setHighlights(highlights, fields);
				result = true;
			    }
			}
			break;
		    }
		}
	    }
	}

	return(result);
    }
}

