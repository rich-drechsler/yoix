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
import javax.swing.border.*;

public
class YoixTipManager

    implements MouseListener,
	       MouseMotionListener,
	       YoixConstants

{

    //
    // A fairly simple class that provides us with a few capabilities that
    // didn't work particularly well when we tried to implement them using
    // Java's ToolTipManager. There's a good chance we'll provide a way for
    // Yoix scripts to access this class, but right now it's pretty much a
    // standalone Java class.
    //
    // NOTE - menu accelerators that control things, like enabled, can be
    // troubling, particularly when they're used to toggle values, because
    // we can get swamped if the user can accidentally hold the key down
    // too long. Something we eventually may want to address, but it's an
    // issue that really should be addressed elsewhere. We did experiment
    // with accelerators that were triggered on release, but it obviously
    // didn't help.
    //

    private JComponent  source = null;

    //
    // These records the current state of the tip.
    //

    private boolean  enabled = false;
    private boolean  dropped = false;
    private boolean  shifting = false;

    //
    // Components used to display the tip.
    //

    private Container  tipcontainer = null;
    private JWindow    tipwindow = null;
    private JLabel     tiplabel = null;
    private JPanel     tippanel = null;

    //
    // The text that's displayed when the tip is shown. A null value or an
    // empty string automatically hides the tip.
    //

    private String  tiptext = null;
    private String  lasttiptext = "          ";
    private Point   lastmovepoint = null;

    //
    // These are mostly used to position the tip properly relative to the
    // cursor's current location.
    //

    private Point  tippoint = null;
    private Point  tipcursorpoint = null;
    private Point  tiplockpoint = null;
    private Point  tipoffset = null;
    private int    tiplockmodel = YOIX_NONE;
    private int    tipcounter = 0;
    private int    tipshiftmodel = 0;
    private int    tipshiftmodifiers = YOIX_CTRL_MASK|YOIX_SHIFT_MASK;

    //
    // When the tip has been dropped we flip tiplabel's foreground color
    // between these two values rather than hiding the tip when it's not
    // able to display valid information. In other words, the text that's
    // displayed when it's disabled by this mechanism is the last valid
    // text.
    //

    private Color  foreground = null;
    private Color  foreground_disabled = null;

    //
    // We remember the JLayeredPane associated with source and use it when
    // we pick to tip container. Not completely correct, because source can
    // be moved, but this will be good enough for now.
    //

    private JLayeredPane  layeredpane = null;

    //
    // Should be unnecessary, at least right now, but just in case we try
    // to make sure we're only listening once.
    //

    private int  listeners = 0;

    //
    // This is only used when we're dragging the tip window around. It's
    // the location of the last event, which means we can use it to move
    // tipwindow.
    //

    private Point  mousepoint = null;

    //
    // These are used to pick the tipwindow's cursor based on whether it's
    // dropped or not.
    //

    private static final Cursor  DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    private static final Cursor  DROPPED_CURSOR = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    private static final Cursor  MOVE_CURSOR = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);

    //
    // Constants that identify actions that need to be carried out in the
    // event thread (via invokeLater() and handleRun()).
    //

    private static final int  RUN_SETDROPPED = 1;
    private static final int  RUN_SETENABLED = 2;
    private static final int  RUN_SETTEXT = 3;
    private static final int  RUN_SETTIPLOCKMODEL = 4;
    private static final int  RUN_SETTIPLOCKPOINT = 5;
    private static final int  RUN_SETTIPOFFSET = 6;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    YoixTipManager(JComponent source) {

	this(source, null, null, null);
    }


    public
    YoixTipManager(JComponent source, Color background) {

	this(source, background, null, null);
    }


    public
    YoixTipManager(JComponent source, Color background, Color foreground) {

	this(source, background, foreground, null);
    }


    public
    YoixTipManager(JComponent source, Color background, Color foreground, Font font) {

	this.source = source;
	buildTipManager(background, foreground, font);
    }

    ///////////////////////////////////
    //
    // MouseListener Methods
    //
    ///////////////////////////////////

    public final void
    mouseClicked(MouseEvent e) {

	if (e.getSource() == source)
	    setCursorLocation(e.getPoint());
    }


    public final void
    mouseEntered(MouseEvent e) {

	if (e.getSource() == source) {
	    if (shifting == false)
		setCursorLocation(e.getPoint());
	}
    }


    public final void
    mouseExited(MouseEvent e) {

	if (e.getSource() == source) {
	    if (shifting == false)
		setCursorLocation(null);
	}
    }


    public final void
    mousePressed(MouseEvent e) {

	mousepoint = e.getPoint();

	if (e.getSource() == tipwindow) {
	    if (dropped) {
		SwingUtilities.convertPointToScreen(mousepoint, tipwindow);
		tipwindow.setCursor(MOVE_CURSOR);
	    }
	} else if (e.getSource() == source) {
	    if (tipshiftmodel == 1) {
		if ((e.getModifiers()&tipshiftmodifiers) == tipshiftmodifiers)
		    shifting = !dropped;
	    }
	    setCursorLocation(mousepoint);
	}
    }


    public final void
    mouseReleased(MouseEvent e) {

	Point  point = e.getPoint();

	if (e.getSource() == tipwindow) {
	    if (dropped)
		tipwindow.setCursor(DROPPED_CURSOR);
	    else tipwindow.setCursor(DEFAULT_CURSOR);
	} else if (e.getSource() == source) {
	    if (shifting) {
		tipoffset.x += (mousepoint.x - point.x);
		tipoffset.y += (mousepoint.y - point.y);
	    }
	    shifting = false;
	    setCursorLocation(point);
	    showTip();
	}
    }

    ///////////////////////////////////
    //
    // MouseMotionListener Methods
    //
    ///////////////////////////////////

    public final void
    mouseDragged(MouseEvent e) {

	Point  point = e.getPoint();
	Point  location;

	if (e.getSource() == tipwindow) {
	    SwingUtilities.convertPointToScreen(point, tipwindow);
	    location = tipwindow.getLocation();
	    location.x += (point.x - mousepoint.x);
	    location.y += (point.y - mousepoint.y);
	    tipwindow.setLocation(location);
	    mousepoint = point;
	} else if (e.getSource() == source)
	    setCursorLocation(point);
    }


    public final void
    mouseMoved(MouseEvent e) {

	if (e.getSource() == source) {
	    setCursorLocation(e.getPoint());
	    showTip();
	    lastmovepoint = e.getPoint();
	}
    }

    ///////////////////////////////////
    //
    // YoixTipManager Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	hideTip();
	source = null;
	layeredpane = null;
	tipcontainer = null;
	tiplabel = null;
	tippanel = null;
	if (tipwindow != null) {
	    new YoixVMDisposer(tipwindow);
	    tipwindow = null;
	}
    }


    public final Point
    getCursorLocation() {

	Point  point;

	return((point = tipcursorpoint) != null ? new Point(point) : null);
    }


    public final String
    getText() {

	return(tiptext);
    }


    public final synchronized Rectangle
    getTipBounds() {

	Dimension  size;
	Rectangle  bounds;
	Point      point;

	if (isShowing()) {
	    size = tipcontainer.getSize();
	    point = tipcontainer.getLocationOnScreen();
	    SwingUtilities.convertPointFromScreen(point, source);
	    bounds = new Rectangle(point, size);
	} else bounds = null;

	return(bounds);
    }


    public final synchronized Point
    getTipOffset() {

	return(tipoffset != null ? new Point(tipoffset) : pickTipOffset());
    }


    public final int
    getTipShiftModel() {

	return(tipshiftmodel);
    }


    public final int
    getTipShiftModifiers() {

	return(tipshiftmodifiers);
    }


    public final void
    handleRun(Object args[]) {

	if (args != null && args.length > 0) {
	    switch (((Integer)args[0]).intValue()) {
		case RUN_SETDROPPED:
		    handleSetDropped(((Boolean)args[1]).booleanValue());
		    break;

		case RUN_SETENABLED:
		    handleSetEnabled(((Boolean)args[1]).booleanValue());
		    break;

		case RUN_SETTEXT:
		    handleSetText((String)args[1]);
		    break;

		case RUN_SETTIPLOCKMODEL:
		    handleSetTipLockModel(((Integer)args[1]).intValue());
		    break;

		case RUN_SETTIPLOCKPOINT:
		    handleSetTipLockPoint((Point)args[1]);
		    break;

		case RUN_SETTIPOFFSET:
		    handleSetTipOffset((Point)args[1]);
		    break;
	    }
	}
    }


    public final boolean
    isDropped() {

	return(dropped);
    }


    public final boolean
    isEnabled() {

	return(enabled);
    }


    public final boolean
    isShifting() {

	return(shifting);
    }


    public final boolean
    isShowing() {

	JPanel  panel;

	return((panel = tippanel) != null && panel.isShowing());
    }


    public final synchronized void
    setDropped(boolean state) {

	if (EventQueue.isDispatchThread() == false) {
	    EventQueue.invokeLater(
		new YoixAWTInvocationEvent(
		    this,
		    new Object[] {new Integer(RUN_SETDROPPED), new Boolean(state)}
		)
	    );
	} else handleSetDropped(state);
    }


    public final synchronized void
    setEnabled(boolean state) {

	if (EventQueue.isDispatchThread() == false) {
	    EventQueue.invokeLater(
		new YoixAWTInvocationEvent(
		    this,
		    new Object[] {new Integer(RUN_SETENABLED), new Boolean(state)}
		)
	    );
	} else handleSetEnabled(state);
    }


    public final synchronized void
    setText(String text) {

	if (EventQueue.isDispatchThread() == false) {
	    EventQueue.invokeLater(
		new YoixAWTInvocationEvent(
		    this,
		    new Object[] {new Integer(RUN_SETTEXT), text}
		)
	    );
	} else handleSetText(text);
    }


    public final synchronized void
    setTipLockModel(int model) {

	if (EventQueue.isDispatchThread() == false) {
	    EventQueue.invokeLater(
		new YoixAWTInvocationEvent(
		    this,
		    new Object[] {new Integer(RUN_SETTIPLOCKMODEL), new Integer(model)}
		)
	    );
	} else handleSetTipLockModel(model);
    }


    public final synchronized void
    setTipLockPoint(int x, int y) {

	Point  point = new Point(x, y);

	if (EventQueue.isDispatchThread() == false) {
	    EventQueue.invokeLater(
		new YoixAWTInvocationEvent(
		    this,
		    new Object[] {new Integer(RUN_SETTIPLOCKPOINT), point}
		)
	    );
	} else handleSetTipLockPoint(point);
    }


    public final synchronized void
    setTipOffset(Point point) {

	point = (point != null) ? new Point(point) : pickTipOffset();

	if (EventQueue.isDispatchThread() == false) {
	    EventQueue.invokeLater(
		new YoixAWTInvocationEvent(
		    this,
		    new Object[] {new Integer(RUN_SETTIPOFFSET), point}
		)
	    );
	} else handleSetTipOffset(point);
    }


    public final synchronized void
    setTipShiftModel(int model) {

	//
	// Currently just 0, which means shifting must be started with a
	// startShifting() call, and 1, which leaves the decision to our
	// our mousePressed() method.
	//

	switch (model) {
	    case 0:
	    case 1:
		tipshiftmodel = model;
		break;

	    default:
		tipshiftmodel = 0;
		break;
	}
    }


    public final synchronized void
    setTipShiftModifiers(int modifiers) {

	tipshiftmodifiers = modifiers;
    }


    public final synchronized boolean
    startShifting() {

	boolean  result = false;

	if (tipshiftmodel == 0)  {
	    shifting = !dropped;
	    result = shifting;
	}
	return(result);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private synchronized void
    addAllListeners() {

	if (listeners == 0) {
	    listeners++;
	    source.addMouseListener(this);
	    source.addMouseMotionListener(this);
	}
    }


    private synchronized void
    buildTipManager(Color background, Color foreground, Font font) {

	Dimension  size;

	if (tippanel == null) {
	    tiplabel = new JLabel(tiptext);
	    tiplabel.setBorder(new EmptyBorder(1, 1, 1, 1));
	    tippanel = new JPanel();
	    tippanel.setLayout(new BorderLayout());
	    tippanel.setBorder(new BevelBorder(BevelBorder.RAISED));
	    tippanel.add(tiplabel, BorderLayout.CENTER);
	    tipoffset = pickTipOffset();
	    setBackground(background);
	    setForeground(foreground);
	    setFont(font);
	}
    }


    private synchronized JLayeredPane
    getLayeredPane() {

	if (layeredpane == null) {
	    if (source != null)
		layeredpane = YoixMiscJFC.getJLayeredPane(source);
	}
	return(layeredpane);
    }


    private synchronized JWindow
    getTipWindow() {

	if (tipwindow == null) {
	    tipwindow = new JWindow(SwingUtilities.getWindowAncestor(source));
	    tipwindow.addMouseListener(this);
	    tipwindow.addMouseMotionListener(this);
	}
	return(tipwindow);
    }


    private synchronized void
    handleSetDropped(boolean state) {

	boolean  forced = false;
	Point    point;

	if (state != dropped) {
	    if (tiptext == null && state) {
		if (tipcursorpoint != null)
		    tippoint = new Point(tipcursorpoint);
		else if (lastmovepoint != null)
		    tippoint = new Point(lastmovepoint);
		else tippoint = new Point();
		handleSetText(lasttiptext);
		forced = true;
	    }
	    dropped = state;
	    if (dropped) {
		if (getTipWindow() != null) {
		    if (tippoint != null) {
			point = new Point(tippoint.x + tipoffset.x, tippoint.y + tipoffset.y);
			SwingUtilities.convertPointToScreen(point, source);
			tipwindow.setLocation(point);
		    }
		}
		showTip();
		if (forced)
		    handleSetText(null);
	    } else {
		if (tiptext != null) {
		    tipcontainer = null;
		    showTip();
		} else hideTip();
	    }
	}
    }


    private synchronized void
    handleSetEnabled(boolean state) {

	if (state != enabled) {
	    enabled = state;
	    if (enabled) {
		addAllListeners();
		showTip();
	    } else {
		hideTip();
		tipcontainer = null;
		layeredpane = null;
		tiptext = null;
	    }
	}
    }


    private synchronized void
    handleSetText(String text) {

	if (text != tiptext && (text == null || text.equals(tiptext) == false)) {
	    if (tiptext != null)
		lasttiptext = tiptext;
	    tiptext = (text != null && text.length() > 0) ? text : null;
	    if (dropped == false) {
		if (tiptext == null)
		    hideTip();
		tiplabel.setForeground(foreground);
		tiplabel.setText(tiptext);
		tipcontainer = null;		// force reset in showTipAt()
		if (tiptext != null)
		    showTip();
	    } else {
		if (tiptext != null) {
		    tiplabel.setForeground(foreground);
		    tiplabel.setText(tiptext);
		    tipcontainer = null;	// force reset in showTipAt()
		    showTip();
		} else tiplabel.setForeground(foreground_disabled);
	    }
	}
    }


    private synchronized void
    handleSetTipLockModel(int model) {

	if (model != tiplockmodel) {
	    switch (model) {
		case YOIX_BOTH:
		case YOIX_HORIZONTAL:
		case YOIX_NONE:
		case YOIX_VERTICAL:
		    tiplockmodel = model;
		    break;

		default:
		    tiplockmodel = YOIX_NONE;
		    break;
	    }
	    setTipPoint(tipcursorpoint);
	    showTip();
	}
    }


    private synchronized void
    handleSetTipLockPoint(Point point) {

	if (point != tiplockpoint) {
	    if (point == null || point.equals(tiplockpoint) == false) {
		tiplockpoint = point;
		setTipPoint(tipcursorpoint);
	    }
	}
    }


    private synchronized void
    handleSetTipOffset(Point point) {

	//
	// It's safe to assume point isn't null.
	//

	if (point.equals(tipoffset) == false) {
	    tipoffset = point;
	    showTip();
	}
    }


    private synchronized void
    hideTip() {

	if (tipcontainer != null) {
	    if (tipcontainer == tippanel) {
		tippanel.setVisible(false);
		if (layeredpane != null)
		    layeredpane.remove(tippanel);
	    } else tipcontainer.setVisible(false);
	}
    }


    private synchronized Container
    pickTipContainer(Point point) {

	Rectangle  layerrect;
	Rectangle  tiprect;
	Dimension  size;

	//
	// Decided to show the tip as the bottom component in layeredpane's
	// POPUP_LAYER. Done so it doesn't obscure things like menus if the
	// owner decides to always show the tip when the mouse moves, even
	// it doesn't have the focus. Means a pulldown menu and our tip can
	// end up in the layer and it seems better to keep the menu on top.
	//
	// NOTE - it's important to position tippanel before adding it to
	// layeredpane, otherwise we could be asked to repaint much more
	// than is really necessary. Most noticeable repainting problems
	// only happen when tippanel is removed from tipwindow, because in
	// that case it's located at (0, 0). If it's added to layeredpane
	// and then properly positioned we get a repaint for (0, 0), but
	// we also get one when tippanel is moved to the correct location.
	// The two rectangles will usually be small, however they can be
	// combined into a single large rectangle by the time we're asked
	// to paint.
	//
	// NOTE - the moveToBack(tippanel) call seems to be required, even
	// if we try to add tippanel at the bottom. Seems like a Java bug,
	// but we didn't investigate.
	//

	if (getLayeredPane() != null && tiptext != null) {
	    if ((layerrect = layeredpane.getBounds()) != null) {
		size = tippanel.getPreferredSize();
		tiprect = new Rectangle(size);
		point = SwingUtilities.convertPoint(source, point, layeredpane);
		tiprect.x = point.x + tipoffset.x;
		tiprect.y = point.y + tipoffset.y;
		if (tiplockmodel != YOIX_HORIZONTAL && tiplockmodel != YOIX_BOTH) {
		    if (tiprect.x + tiprect.width > layerrect.width)
			tiprect.x = layerrect.width - tiprect.width;
		    if (tiprect.x < 0)
			tiprect.x = 0;
		}
		point.x = tiprect.x;
		point.y = tiprect.y;
		if (dropped == false && SwingUtilities.isRectangleContainingRectangle(layerrect, tiprect)) {
		    if (tipcontainer != tippanel) {
			if (tipwindow != null) {
			    tipwindow.setVisible(false);
			    tipwindow.remove(tippanel);
			}
			tippanel.setVisible(false);
			tippanel.setLocation(point.x, point.y);
			tippanel.setSize(size);
			layeredpane.add(tippanel, JLayeredPane.POPUP_LAYER);
			tipcontainer = tippanel;
		    }
		    layeredpane.moveToBack(tippanel);		// seems necessary
		} else {
		    if (tipcontainer != getTipWindow()) {
			tippanel.setVisible(false);
			layeredpane.remove(tippanel);
			tippanel.setSize(size);
			tippanel.setLocation(0, 0);
			tippanel.setVisible(true);
			tipwindow.getContentPane().add(tippanel);
			tipwindow.pack();
			tipcontainer = tipwindow;
		    }
		    SwingUtilities.convertPointToScreen(point, layeredpane);
		}
		if (dropped == false)
		    tipcontainer.setLocation(point.x, point.y);
	    }
	}

	return(tipcontainer);
    }


    private Point
    pickTipOffset() {

	Dimension  size;

	//
	// Picks a default offset, assuming a 16x16 cursor, which seems
	// reasonable.
	//

	size = YoixAWTToolkit.getBestCursorSize(16, 16);
	if (size.height <= 16) {
	    if (size.height == 0)
		size.height = YoixMakeScreen.javaDistance(3*72.0/16);
	    size.height += YoixMakeScreen.javaDistance(72.0/16);
	}
	return(new Point(0, size.height));
    }


    private synchronized void
    removeAllListeners() {

	if (listeners > 0) {
	    listeners--;
	    source.removeMouseListener(this);
	    source.removeMouseMotionListener(this);
	}
    }


    private void
    setBackground(Color color) {

	if (color != null || (color = UIManager.getColor("ToolTip.background")) != null)
	    tippanel.setBackground(color);
    }


    private synchronized void
    setCursorLocation(Point point) {

	tipcursorpoint = point;
	setTipPoint(point);
    }


    private void
    setFont(Font font) {

	if (font != null || (font = UIManager.getFont("ToolTip.font")) != null)
	    tiplabel.setFont(font);
    }


    private void
    setForeground(Color color) {

	if (color != null || (color = UIManager.getColor("ToolTip.foreground")) != null) {
	    foreground = color;
	    if ((foreground_disabled = UIManager.getColor("Label.disabledForeground")) == null)
		foreground_disabled = Color.gray;
	    tiplabel.setForeground(color);
	}
    }


    private synchronized void
    setTipPoint(Point point) {

	if (point != null) {
	    tippoint = new Point(point);
	    if (tiplockpoint != null) {
		if (tiplockmodel == YOIX_HORIZONTAL || tiplockmodel == YOIX_BOTH)
		    tippoint.x = tiplockpoint.x;
		if (tiplockmodel == YOIX_VERTICAL || tiplockmodel == YOIX_BOTH)
		    tippoint.y = tiplockpoint.y;
	    }
	} else tippoint = null;
    }


    private void
    showTip() {

	showTipAt(tippoint);
    }


    private synchronized void
    showTipAt(Point point) {

	if (enabled) {
	    if (point != null && tiptext != null) {
		if (pickTipContainer(point) != null) {
		    if (isShowing() == false)
			tipcontainer.setVisible(true);
		    if (tipcontainer instanceof Window) {
			((Window)tipcontainer).toFront();	// unnecessary??
			if ((++tipcounter%5) == 0)
			    Thread.yield();
		    }
		}
	    }
	}
    }
}

