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
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

public
class YoixSwingJScrollPane extends JScrollPane

    implements MouseWheelListener

{

    //
    // This class tries to adjust behavior that we didn't like in Java's
    // JScrollPane class. Layout manager's for complicated screens that
    // contained one or more JScrollPanes sometimes behaved in ways that
    // confused users when they resized screens. The main culprit seemed
    // to be ScrollPaneLayout.preferredLayoutSize(), because it carefully
    // tries to account for scrollbars that may or may not be needed. The
    // result was that resizing a screen in one direction, say vertically,
    // could mean a vertical scrollbar would appear or disappear, which
    // changes the JScrollPane's preferred width and might eventually let
    // the layout manager make some surprising adjustments.
    //
    // Our approach is to provide low level control of the JScrollPane's
    // getMinimumSize() and getPreferredSize() using sizecontrol, which
    // can be changed by Yoix scripts. Bits 0 to 3 set the minimum size
    // model, bits 4 to 7 set the preferred size model, bits 8 to 12 are
    // special flags, while the remaining are currently undocumented and
    // subject to change so we recommend that they be set to zero. When
    // sizecontrol is zero Java's default answers are used for the minimum
    // and preferred sizes. Model 1 means we calculate the value which is
    // optionally adjusted by the appropriate scrollbar dimension based
    // on bits in the special flags. Model 2 means both dimensions will
    // be zero, which doesn't make much sense for the preferred size but
    // can occasionally be a useful as a minimum size.
    //
    // The idea behind our model 1 adjustments is that getPreferredSize()
    // should return an answer that depends on the scrollbar policies and
    // perhaps also on sizecontrol rather than on whether a scrollbar may
    // or may not be needed by the component that's being viewed in the
    // JScrollPane. Answers from getPreferredSize() and getMinimumSize()
    // that don't depend on the JScrollPane's current size should lead to
    // more consistent layout manager behavior when a screen containing a
    // JScrollPane is resized. Our algorithm, for the most part, can be
    // summarized by the following table
    //
    //             NEVER       MAYBE       ALWAYS
    //             -----       -----       ------
    //      NEVER:  No          No           No
    //      MAYBE:  Yes         ???          No
    //     ALWAYS:  Yes         Yes          Yes
    //
    // in which NEVER, MAYBE, and ALWAYS are abbreviations that stand for
    // the obvious horizontal or vertical scrollbar policies. If the rows
    // represent the vertical scrollbar policy and columns represent the
    // horizontal scrollbar policy then cells in the table tell you when
    // getPreferredSize() or getMinimumSize() add the scrollbar width to
    // the width field in their answer. What's done when both scrollbars
    // are optional is controlled by a bit in the special flags. In this
    // case it's bit 8 and when set the width of the scrollbar is added
    // to the width in the answer. That's a brief summary of sizecontrol
    // and the algorithm - the complete story can be found in the code.
    //

    private int  sizecontrol = 0;

    private int      model_minsize = 0;			// bits 0 to 3
    private int      model_preferredsize = 0;		// bits 4 to 7
    private boolean  adjustwidth_flag = false;		// bit 8
    private boolean  adjustheight_flag = false;		// bit 9
    private boolean  inheritsizecontrol_flag = false;	// bit 10
    private boolean  lockminsize_flag = false;		// bit 11
    private boolean  lockpreferredsize_flag = false;	// bit 12

    //
    // The other problem was with mouse wheel scrolling, which seemed to
    // always scroll by three lines on Linux and Windows because that was
    // the value stored in the scrollAmount field in the MouseWheelEvent
    // that was handed to Java's BasicScrollPaneUI class, which is where
    // the low level scolling code happens to be. We put ourselves in the
    // loop by overriding methods that add and remove MouseWheelListeners
    // from the JScrollPane. We currently do it no matter who tries to be
    // a MouseWheelListener, but there's a chance we may want to limit it
    // to the javax.swing.plaf.basic.BasicScrollPaneUI class - later.
    //
    // NOTE - the low level implementation of the code that handle mouse
    // wheel scrolling in plaf.basic.BasicScrollPaneUI.java has improved
    // in 1.6.0 (see BasicScrollPaneUI.mouseWheelMoved()), but platform
    // dependent scrolling behavior hasn't been addressed. In particular
    // you still can't use the mouse wheel to scroll up or down by one
    // unit on Linux or Windows.
    // 

    private Vector  listeners;

    //
    // We uses a small array (size is controlled by threshold value below)
    // to collect MouseWheelEvents that are used when we try to choose an
    // appropriate value for scrollAmount. Our decision is partly based on
    // how fast MouseWheelEvents arrive, but it turns out we also want to
    // be able to recognize when the scrolling direction changes. In other
    // words, just remembering event timestamps isn't quite good enough.
    //

    private MouseWheelEvent  events[] = null;
    private int              next = 0;

    //
    // These are low level variables that control the method that we use
    // to choose the scrollAmount for the new MouseWheelEvents. The value
    // assigned to threshold, if it's positive, controls the size of the
    // array that we use to save old MouseWheelEvents that are used in the
    // scrollAmount calculation. The clicktimes and rolltimes arrays are
    // used to pick values based on the direction of the wheel roatation.
    // A negative wheel rotation uses times at index 0, while a positive
    // wheel rotation uses times at index 1. Individual events separated
    // by clicktime always contribute to the calculation of scrollAmount,
    // as do groups of events separated by less than rolltime milliseconds.
    // A theshold of 0 always sets scrollAmount to 1 while a negative value
    // disables the scrollAmount adjustments.
    //

    private long  rolltimes[] = {525, 475};	// trial and error
    private long  clicktimes[] = {30, 30};	// trial and error
    private int   threshold;

    //
    // The default threshold was picked by experimenting on at least three
    // different platforms that were all equipped with different mice. The
    // actual threshold can be passed as an argument to the constructor, so
    // it could also specified by a field that was sent in each component.
    // 

    private static final int  THRESHOLD = 7;	// trial and error

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    YoixSwingJScrollPane() {

	this(null, THRESHOLD);
    }


    public
    YoixSwingJScrollPane(Component comp) {

	this(comp, THRESHOLD);
    }


    public
    YoixSwingJScrollPane(Component comp, int threshold) {

	super(comp);
	buildScrollPane(threshold);
    }

    ///////////////////////////////////
    //
    // MouseWheelListener Methods
    //
    ///////////////////////////////////

    public final void
    mouseWheelMoved(MouseWheelEvent e) {

	MouseWheelListener  listener;
	MouseWheelEvent     original;
	Enumeration         enm;
	int                 scrollamount;

	//
	// There's now similar compensation in YoixMakeEvent.yoixEvent(), which is
	// where Java events are converted to Yoix events, so we hand the original
	// event back to YoixBodyComponent listeners, otherwise the compensation
	// would be applied twice. Not a big deal because Yoix scripts won't want
	// to handle MouseWheelEvent in a JScrollPane very often.
	//

	if (listeners != null && isEnabled()) {
	    original = e;
	    if ((scrollamount = pickScrollAmount(e)) != e.getScrollAmount()) {
		e = new MouseWheelEvent(
		    (Component)e.getSource(),
		    e.getID(),
		    e.getWhen(),
		    e.getModifiers(),
		    e.getX(),
		    e.getY(),
		    e.getClickCount(),	
		    e.isPopupTrigger(),
		    e.getScrollType(),
		    scrollamount,
		    e.getWheelRotation()
		);
	    }
	    for (enm = listeners.elements(); enm.hasMoreElements(); ) {
		if ((listener = (MouseWheelListener)enm.nextElement()) != null) {
		    if (listener instanceof YoixBodyComponent)
			listener.mouseWheelMoved(original);
		    else listener.mouseWheelMoved(e);
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // YoixSwingJScrollPane Methods
    //
    ///////////////////////////////////

    public final synchronized void
    addMouseWheelListener(MouseWheelListener listener) {

	if (listener != null) {
	    if (listeners == null || listeners.contains(listener) == false) {
		if (listeners == null)
		    listeners = new Vector();
		if (listeners.isEmpty())
		    super.addMouseWheelListener(this);
		listeners.addElement(listener);
	    }
	}
    }


    protected void
    finalize() {

	listeners.clear();
	listeners = null;
	events = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    final Dimension
    getAdjustedViewportSize() {

	JScrollBar  sb;
	Dimension   size = null;
	Insets      insets;

	size = getSize();
	insets = getInsets();
	size.width -= (insets.left + insets.right);
	size.height -= (insets.top + insets.bottom);
	if ((sb = getVerticalScrollBar()) != null) {
	    if (getVerticalScrollBarPolicy() != VERTICAL_SCROLLBAR_NEVER)
		size.width -= sb.getPreferredSize().width;
	}
	if ((sb = getHorizontalScrollBar()) != null) {
	    if (getHorizontalScrollBarPolicy() != HORIZONTAL_SCROLLBAR_NEVER)
		size.height -= sb.getPreferredSize().height;
	}
	return(size);
    }


    public Dimension
    getMinimumSize() {

	JViewport  vp;
	Component  comp;
	Dimension  size = null;

	if ((vp = getViewport()) != null) {
	    comp = vp.getView();
	    switch (pickModel(vp, false)) {
		case 0:
		    size = super.getMinimumSize();
		    break;

		case 1:
		    size = (comp != null) ? comp.getMinimumSize() : new Dimension(0, 0);
		    //
		    // Do we need a similiar width adjustment? Symmetry
		    // might suggest it, but we were unable to come up
		    // an example that seems to need it.
		    //
		    size.height = Math.min(size.height, vp.getPreferredSize().height);
		    size.width = getAdjustedWidth(size.width, vp, false);
		    size.height = getAdjustedHeight(size.height, vp, false);
		    break;

		case 2:
		    size = new Dimension(0, 0);
		    break;
	    }
	}
	return(size != null ? size : new Dimension(0, 0));
    }


    public Dimension
    getPreferredSize() {

	JViewport  vp;
	Dimension  size = null;

	//
	// Doubt you'll ever use model 2 in this method, but we'll leave
	// it in just for consistency with getMinimumSize().
	//

	if ((vp = getViewport()) != null) {
	    switch (pickModel(vp, true)) {
		case 0:
		    size = super.getPreferredSize();
		    break;

		case 1:
		    size = vp.getPreferredSize();
		    size.width = getAdjustedWidth(size.width, vp, true);
		    size.height = getAdjustedHeight(size.height, vp, true);
		    break;

		case 2:
		    size = new Dimension(0, 0);
		    break;
	    }
	}
	return(size != null ? size : super.getPreferredSize());
    }


    final int
    getSizeControl() {

	return(sizecontrol);
    }


    final void
    inheritSizeControl(YoixObject obj) {

	if (inheritsizecontrol_flag) {
	    if (obj != null && obj.isNumber())
		setSizeControl(obj.intValue());
	}
    }


    public final synchronized void
    removeMouseWheelListener(MouseWheelListener listener) {

	if (listener != null) {
	    if (listeners.contains(listener)) {
		listeners.remove(listener);
		if (listeners.size() == 0)
		    super.removeMouseWheelListener(this);
	    }
	}
    }


    public final void
    setCursor(Cursor cursor) {

	Component  components[];
	Component  component;
	int        n;

	//
	// Didn't like the fact that the scrollbar cursors changed when we
	// assign a new cursor to the JScrollPane, so we walk through the
	// components contained in the JScrollPane looking for scrollbars.
	// Just skipping the setCursor() call for scrollbars didn't work,
	// so we explicitly set it to the value that YoixRegistryCursor
	// thinks is appropriate for a JScrollBar.
	//
	// NOTE - checking for a cursor change is precaution that's really
	// not necessary, but we're going to leave it in anyway. It's done
	// to make absolutely sure there's no chance of recursion (see the
	// YoixSwingJTable.setCursor() method), but the fact is we won't
	// find a component, like a YoixSwingJTable, that may have called
	// us because we only look at that the top-level components that
	// we manage. A YoixSwingJTable that's using us as a peerscroller
	// will be in the JViewport, and we don't look inside it. Anyway
	// the code and this note should serve as a reminder!!
	//

	if ((components = getComponents()) != null) {
	    for (n = 0; n < components.length; n++) {
		component = components[n];
		if (!(component instanceof JScrollBar)) {
		    if (cursor != component.getCursor())
			component.setCursor(cursor);
		} else component.setCursor(YoixRegistryCursor.getStandardCursor(component));
	    }
	}
    }


    public final void
    setEnabled(boolean state) {

	Component  components[];
	Component  component;
	int        n;

	super.setEnabled(state);
	if ((components = getComponents()) != null) {
	    for (n = 0; n < components.length; n++) {
		if ((component = components[n]) != null)	// unnecessary check
		    component.setEnabled(state);
	    }
	}
    }


    public final void
    setOpaque(boolean state) {

	JViewport  vp;

	//
	// Code that sync's the viewport is a recent addition (1/28/11) that
	// seems to behave properly, but undoubtedly needs more testing. We
	// should also test whether other components (e.g., the header) need
	// attention. Should YoixBodyComponentSwing.setOpaque() be setting
	// opaque in peerscroller when it's not null?? We eventually should
	// investigate.
	//

	super.setOpaque(state);
	if ((vp = getViewport()) != null)	// added on 1/28/11
	    vp.setOpaque(state);
    }


    final synchronized void
    setSizeControl(int flags) {

	if (flags != sizecontrol) {
	    sizecontrol = flags;
	    model_minsize = (sizecontrol >> 0) & 0x0F;
	    model_preferredsize = (sizecontrol >> 4) & 0x0F;
	    adjustwidth_flag = ((sizecontrol >> 8) & 0x01) != 0;
	    adjustheight_flag = ((sizecontrol >> 9) & 0x01) != 0;
	    inheritsizecontrol_flag = ((sizecontrol >> 10) & 0x01) != 0;
	    lockminsize_flag = ((sizecontrol >> 11) & 0x01) != 0;
	    lockpreferredsize_flag = ((sizecontrol >> 12) & 0x01) != 0;
	    revalidate();
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildScrollPane(int threshold) {

	//
	// Low level mouse wheel scrolling improved in 1.6.0, so we can be
	// more aggressive in how many events we track and consequently how
	// big our adjusted scrollAmount gets.
	//

	this.threshold = threshold;
	if (threshold >= 0) {
	    if (YoixMisc.jvmCompareTo("1.6.0") >= 0)
		events = new MouseWheelEvent[5*threshold + 1];
	    else events = new MouseWheelEvent[threshold + 5];
	} else events = null;
	next = 0;
    }


    private int
    getAdjustedHeight(int height, JViewport vp, boolean preferred) {

	JScrollBar  sb;
	JViewport   header;
	Insets      insets;
	int         policy;
	int         delta;

	insets = getInsets();
	height += insets.top + insets.bottom;

	if ((header = getColumnHeader()) != null && header.isVisible())
	    height += header.getPreferredSize().height;

	if ((sb = getHorizontalScrollBar()) != null) {
	    delta = preferred ? sb.getPreferredSize().height : sb.getMinimumSize().height;
	    if ((policy = getHorizontalScrollBarPolicy()) == HORIZONTAL_SCROLLBAR_AS_NEEDED) {
		if ((policy = getVerticalScrollBarPolicy()) == VERTICAL_SCROLLBAR_NEVER)
		    height += delta;
		else if (adjustheight_flag && policy == VERTICAL_SCROLLBAR_AS_NEEDED)
		    height += delta;
	    } else if (policy == HORIZONTAL_SCROLLBAR_ALWAYS)
		height += delta;
	}
	return(height);
    }


    private int
    getAdjustedWidth(int width, JViewport vp, boolean preferred) {

	JScrollBar  sb;
	JViewport   header;
	Insets      insets;
	int         policy;
	int         delta;

	insets = getInsets();
	width += insets.left + insets.right;

	if ((header = getRowHeader()) != null && header.isVisible())
	    width += preferred ? header.getPreferredSize().width : header.getMinimumSize().width;

	if ((sb = getVerticalScrollBar()) != null) {
	    delta = preferred ? sb.getPreferredSize().width : sb.getMinimumSize().width;
	    if ((policy = getVerticalScrollBarPolicy()) == VERTICAL_SCROLLBAR_AS_NEEDED) {
		if ((policy = getHorizontalScrollBarPolicy()) == HORIZONTAL_SCROLLBAR_NEVER)
		    width += delta;
		else if (adjustwidth_flag && policy == HORIZONTAL_SCROLLBAR_AS_NEEDED)
		    width += delta;
	    } else if (policy == VERTICAL_SCROLLBAR_ALWAYS)
		width += delta;
	}
	return(width);
    }


    private int
    pickModel(JViewport vp, boolean preferred) {

	Container  parent;
	int        model;

	//
	// This is where the final model selection is made, but only if the
	// appropriate locked flag isn't true. Picking the minsize model is
	// a little harder because we usually want to let JSplitPanes adjust 
	// our size down to zero, which is why we pick model 2 when we have
	// an ancestor that's a JSplitPane.
	//

	if (preferred == false) {
	    if (lockminsize_flag == false) {
		if (vp.isMinimumSizeSet() == false) {
		    model = model_minsize;
		    for (parent = getParent(); parent != null; parent = parent.getParent()) {
			if (parent instanceof JSplitPane) {
			    model = 2;
			    break;
			}
		    }
		} else model = 0;
	    } else model = model_minsize;
	} else {
	    if (lockpreferredsize_flag == false) {
		if (vp.isPreferredSizeSet() == false)
		    model = model_preferredsize;
		else model = 0;
	    } else model = model_preferredsize;
	}

	return(model);
    }


    private int
    pickScrollAmount(MouseWheelEvent e) {

	MouseWheelEvent  event;
	MouseWheelEvent  lastevent;
	long             clicktime;
	long             rolltime;
	long             delta;
	int              scrollamount;
	int              rotation;
	int              index;
	int              count;
	int              subtotal;

	scrollamount = e.getScrollAmount();
	if (events != null) {
	    if (events.length > 1) {
		index = next;
		events[index] = e;
		next = (next + 1)%events.length;
		rotation = e.getWheelRotation();
		//
		// This check means we don't mess with events that look
		// like they're encoding the amount to scroll using the
		// event's scrollamount and wheelrotation fields, which
		// right now is a behavior that we've only witnessed on
		// Macs. Small chance we may want to make all platforms
		// behave the same way by taking this test out and then
		// storing 1 as the wheelrotation field in a new event.
		//
		if (Math.abs(rotation) == 1) {
		    clicktime = clicktimes[rotation < 0 ? 0 : 1];
		    rolltime = rolltimes[rotation < 0 ? 0 : 1];
		    lastevent = e;
		    subtotal = 0;
		    for (count = 0; index != next; count++, subtotal++, index--) {
			if ((event = events[index]) != null) {
			    if (rotation*event.getWheelRotation() > 0) {
				delta = lastevent.getWhen() - events[index].getWhen();
				if (delta > clicktime) {
				    if (subtotal >= threshold/2 || count < threshold) {
					if (delta < rolltime)
					    subtotal = 0;
					else break;
				    } else break;
				}
				lastevent = event;
				if (index == 0)
				    index = events.length;
			    } else break;
			} else break;
		    }
		    if (count <= threshold)
			scrollamount = 1;
		    else scrollamount = count - (threshold - 1);
		}
	    } else scrollamount = 1;
	}
	return(scrollamount);
    }
}

