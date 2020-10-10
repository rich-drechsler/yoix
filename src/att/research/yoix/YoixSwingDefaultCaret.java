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
import javax.swing.text.*;

class YoixSwingDefaultCaret extends DefaultCaret

{

    //
    // This class helps provide consistent behavoir in components, like
    // JTextArea and JTextField, that extend JTextComponent but also may
    // implement their own low level drag and drop support. Our goal was
    // to reproduce the highlighting and mouse behavior that's avaliable
    // in text components that use the automatic drag and drop handling
    // that Swing can provide. Extending Swing's DefaultCaret class and
    // using an instance of this class as the caret accomplished what we
    // wanted, and we didn't really see another easy solution.
    //

    private boolean  dragpossible = false;
    private boolean  dropstarted = false;
    private boolean  caretvisible = false;
    private int      selectedends[] = {0, 0};
    private int      caretmodel = 0;

    ///////////////////////////////////
    //
    // YoixSwingDefaultCaret Methods
    //
    ///////////////////////////////////

    final boolean
    getDragPossible() {

	return(dragpossible);
    }

    public final void
    mouseDragged(MouseEvent e) {

	if (dragpossible)
	    e.consume();
	super.mouseDragged(e);
    }


    public final void
    mousePressed(MouseEvent e) {

	if (getDragStarted() == false) {
	    if (isDragPossible(e)) {
		dragpossible = true;
		e.consume();
	    } else dragpossible = false;
	} else dragpossible = false;
	super.mousePressed(e);
    }


    public final void
    mouseReleased(MouseEvent e) {

	if (dragpossible && getDragStarted())
	    e.consume();
	dragpossible = false;
	super.mouseReleased(e);
    }


    protected void
    positionCaret(MouseEvent e) {

	if (e.isConsumed() == false)
	    super.positionCaret(e);
    }


    final void
    setCaretModel(int model) {

	caretmodel = model;
    }


    final void
    updateDropTarget(boolean dragging, boolean accepted, Point p) {

	JTextComponent  comp;

	if (dragging) {
	    if (caretmodel == 0) {
		if (dropstarted == false) {
		    dropstarted = true;
		    caretvisible = isVisible();
		    selectedends[0] = getMark();
		    selectedends[1] = getDot();
		    super.setVisible(true);
		}
		if (p != null) {
		    if ((comp = getComponent()) != null)
			comp.setCaretPosition(comp.viewToModel(p));
		}
	    }
	} else {
	    if (caretmodel == 0) {
		if (accepted == false) {
		    setDot(selectedends[0]);
		    moveDot(selectedends[1]);
		}
	    }
	    if (dropstarted) {
		dropstarted = false;
		super.setVisible(caretvisible);
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private boolean
    getDragStarted() {

	JTextComponent  comp;
	boolean         result = false;

	if ((comp = getComponent()) != null) {
	    if (comp instanceof YoixInterfaceDragable)
		result = ((YoixInterfaceDragable)comp).getDragStarted();
	}
	return(result);
    }


    private boolean
    isDragPossible(MouseEvent e) {

	JTextComponent  comp;
	boolean         result = false;
	int             position;
	int             dot;
	int             mark;

	if ((comp = getComponent()) != null) {
	    if (comp instanceof YoixInterfaceDragable) {
		if (((YoixInterfaceDragable)comp).isDragPossible()) {
		    dot = getDot();
		    mark = getMark();
		    if (dot != mark) {
			position = comp.viewToModel(e.getPoint());
			if (dot <= position && position < mark)
			    result = true;
			else if (mark <= position && position < dot)
			    result = true;
		    }
		}
	    }
	}
	return(result);
    }
}

