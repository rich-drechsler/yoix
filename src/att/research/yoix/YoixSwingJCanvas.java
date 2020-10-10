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
import java.awt.geom.*;
import javax.swing.*;
import java.util.Hashtable;

public
class YoixSwingJCanvas extends YoixSwingJPanel

    implements ItemSelectable,
	       MouseListener,
	       MouseMotionListener,
	       MouseWheelListener,
	       YoixAPI,
	       YoixAPIProtected,
	       YoixConstantsSwing

{

    //
    // A JPanel masquerading as something that doesn't exist in Swing,
    // namely a JCanvas, but is provided by the Yoix interpreter (for
    // now anyway). Stumbled into at least very subtle issue related
    // to painting, so be careful making changes here.
    //

    private Insets  borderinsets = null;
    private Color   bordercolor = null;
    private int     panandzoomflags = 0;
    private int     state = 0;

    //
    // Coordinates for drawing top and bottom shadows.
    //

    private int  bottomx[] = {0, 0, 0, 0, 0, 0};
    private int  bottomy[] = {0, 0, 0, 0, 0, 0};
    private int  topx[] = {0, 0, 0, 0, 0, 0};
    private int  topy[] = {0, 0, 0, 0, 0, 0};

    //
    // Listeners for some custom events - not private so subclasses can
    // easily check them before deciding to do any extra work.
    //

    protected ActionListener actionlistener = null;
    protected ItemListener   itemlistener = null;

    //
    // Mouse event related stuff - some states may not be implemented.
    //

    private static final int  AVAILABLE = 0;
    private static final int  PRESSED = 1;
    private static final int  PANNING = 2;
    private static final int  RESETTING = 3;

    private Point  mousepoint = null;
    private int    mousebutton = 0;
    private int    pressedstate;
    private int    mouse = AVAILABLE;

    //
    // Pan and zoom support, which right now can only be happen via three
    // eight bit fields that are stored in a single in. We eventually may
    // accept a dictionary, but not for a while.
    //

    private boolean  pancopyarea = false;
    private int      panbuttonmask = 0;
    private int      panmodifiers = 0;
    private int      zoomwheel = 0;
    private int      zoommodifiers = 0;
    private int      resetbuttonmask = 0;
    private int      resetmodifiers = 0;

    private static final int  PAN_STARTBIT = 0;
    private static final int  ZOOM_STARTBIT = 8;
    private static final int  RESET_STARTBIT = 16;

    private static final int  PAN_MASK = (0xFF << PAN_STARTBIT);
    private static final int  ZOOM_MASK = (0xFF << ZOOM_STARTBIT);
    private static final int  RESET_MASK = (0xFF << RESET_STARTBIT);
    private static final int  PANANDZOOM_MASK = PAN_MASK|ZOOM_MASK|RESET_MASK;

    //
    // Callback functions that are occasionally useful.
    //

    private YoixObject  afterpan = null;
    private YoixObject  afterzoom = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    protected
    YoixSwingJCanvas(YoixObject data, YoixBodyComponent parent) {

	super(data, parent);
    }

    ///////////////////////////////////
    //
    // ItemSelectable Methods
    //
    ///////////////////////////////////

    public final synchronized void
    addItemListener(ItemListener listener) {

        itemlistener = AWTEventMulticaster.add(itemlistener, listener);
    }


    public synchronized Object[]
    getSelectedObjects() {

	return(null);
    }


    public final synchronized void
    removeItemListener(ItemListener listener) {

	itemlistener = AWTEventMulticaster.remove(itemlistener, listener);
    }

    ///////////////////////////////////
    //
    // MouseListener Methods
    //
    ///////////////////////////////////

    public void
    mouseClicked(MouseEvent e) {

    }


    public synchronized void
    mouseEntered(MouseEvent e) {

	if (mouse == PRESSED)
	    setState(pressedstate);
    }


    public synchronized void
    mouseExited(MouseEvent e) {

	if (mouse == PRESSED)
	    setState(pressedstate == 0 ? 1 : 0);
    }


    public synchronized void
    mousePressed(MouseEvent e) {

	int  modifiers;
	int  button;

	if (mouse == AVAILABLE) {
	    button = YoixMiscJFC.cookModifiers(e) & YOIX_BUTTON_MASK;
	    modifiers = e.getModifiersEx();
	    switch (button) {
		case YOIX_BUTTON1_MASK:
		case YOIX_BUTTON2_MASK:
		case YOIX_BUTTON3_MASK:
		    if ((button & panbuttonmask) == button && (modifiers & YOIX_KEY_DOWN_MASK) == panmodifiers) {
			mouse = PANNING;
			panBegin(e.getPoint());
		    } else if ((button & resetbuttonmask) == button && (modifiers & YOIX_KEY_DOWN_MASK) == resetmodifiers) {
			mouse = RESETTING;
			resetBegin(e.getPoint());
		    } else if ((button & data.getInt(N_BUTTONMASK, YOIX_BUTTON1_MASK)) != 0) {
			if (state == 0 || data.getBoolean(N_STICKY)) {
			    mouse = PRESSED;
			    pressedstate = (state == 0 ? 1 : 0);
			    setState(pressedstate);
			}
		    }
		    break;
	    }
	    mousebutton = (mouse != AVAILABLE) ? button : 0;
	}
    }


    public synchronized void
    mouseReleased(MouseEvent e) {

	int  buttons;

	if (mouse != AVAILABLE) {
	    buttons = YoixMiscJFC.cookModifiers(e) & YOIX_BUTTON_MASK;
	    if ((buttons & mousebutton) != 0) {		// test is for Java 1.3.1
		switch (mouse) {
		    case PRESSED:
			if (state == pressedstate) {
			    if (data.getBoolean(N_STICKY) == false)
				setState(state == 0 ? 1 : 0);
			    else postItemEvent(YoixObject.newInt(state), state != 0);
			    postActionEvent(null, e.getModifiers());
			}
			break;

		    case PANNING:
			panEnd(e.getPoint());
			break;

		    case RESETTING:
			resetEnd(e.getPoint());
			break;
		}
		mousepoint = null;
		mousebutton = 0;
		mouse = AVAILABLE;
	    }
	}
    }

    ///////////////////////////////////
    //
    // MouseMotionListener Methods
    //
    ///////////////////////////////////

    public synchronized void
    mouseDragged(MouseEvent e) {

	if (mouse != AVAILABLE) {
	    switch (mouse) {
		case PANNING:
		    panDragged(e.getPoint());
		    break;
	    }
	}
    }


    public synchronized void
    mouseMoved(MouseEvent e) {

    }

    ///////////////////////////////////
    //
    // MouseWheelListener Methods
    //
    ///////////////////////////////////

    public synchronized void
    mouseWheelMoved(MouseWheelEvent e) {

	if ((e.getModifiersEx() & YOIX_KEY_DOWN_MASK) == zoommodifiers)
	    zoom(e.getWheelRotation() < 0 ? 1.05 : 1/1.05, e.getPoint());
    }

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public final YoixObject
    getContext() {

	//
	// Why is this method public? Investigate later and if possible
	// make it protected.
	//

	return(parent != null ? parent.getContext() : null);
    }


    public final void
    paintBackgroundImage(Graphics g) {

	Rectangle  bounds;
	Image      image;

	if ((image = getFilteredImage(backgroundhints)) != null) {
	    if ((bounds = getInteriorBounds()) != null)
		YoixMiscJFC.paintImage(this, image, bounds.getLocation(), backgroundhints, g);
	}
    }

    ///////////////////////////////////
    //
    // YoixAPIProtected Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    call(YoixObject function, YoixObject arg) {

	return(parent.call(function, arg));
    }


    protected final YoixObject
    call(YoixObject function, YoixObject argv[]) {

	return(parent.call(function, argv));
    }


    protected final boolean
    hasActionListener() {

	YoixObject  obj;
	boolean     result;

	if (actionlistener != null) {
	    obj = data.getObject(N_ACTIONPERFORMED);
	    result = (obj != null && obj.notNull() && obj.isCallable());
	} else result = false;

	return(result);
    }


    protected final boolean
    hasItemListener() {

	YoixObject  obj;
	boolean     result;

	if (itemlistener != null) {
	    obj = data.getObject(N_ITEMCHANGED);
	    result = (obj != null && obj.notNull() && obj.isCallable());
	} else result = false;

	return(result);
    }


    protected final Object
    getBody(YoixObject obj) {

	return(parent != null ? parent.getBody(obj) : null);
    }


    protected final YoixObject
    getData() {

	return(data);
    }


    protected final void
    paintBorder(Insets insets, Graphics g) {

	Dimension  d;
	Shape      clip;
	int        bottom;
	int        left;
	int        right;
	int        top;

	if (borderinsets != null && borderinsets.equals(ZEROINSETS) == false) {
	    d = getSize();
	    if (d.width != 0 && d.height != 0) {
		synchronized(this) {
		    if (d.width != bottomx[0] || d.height != bottomy[0]) {
			left = Math.min(borderinsets.left, insets.left);
			top = Math.min(borderinsets.top, insets.top);
			right = Math.min(borderinsets.right, insets.right);
			bottom = Math.min(borderinsets.bottom, insets.bottom);

			bottomx[0] = d.width;
			bottomy[0] = d.height;
			bottomx[1] = d.width;
			bottomy[1] = 0;
			bottomx[2] = d.width - right;
			bottomy[2] = top;
			bottomx[3] = bottomx[2];
			bottomy[3] = d.height - bottom;
			bottomx[4] = left;
			bottomy[4] = bottomy[3];
			bottomx[5] = 0;
			bottomy[5] = d.height;

			topx[0] = 0;
			topy[0] = 0;
			topx[1] = d.width;
			topy[1] = 0;
			topx[2] = d.width - right;
			topy[2] = top;
			topx[3] = left;
			topy[3] = top;
			topx[4] = left;
			topy[4] = d.height - bottom;
			topx[5] = 0;
			topy[5] = d.height;
		    }
		    clip = g.getClip();
		    g.setClip(null);
		    g.setColor(pickBorderColor(bordercolor, false));
		    g.fillPolygon(bottomx, bottomy, 6);
		    g.setColor(pickBorderColor(bordercolor, true));
		    g.fillPolygon(topx, topy, 6);
		    g.setColor(getForeground());
		    g.setClip(clip);
		}
	    }
	}
    }


    protected final void
    postActionEvent(String command, int modifiers) {

	postActionEvent(command, modifiers, false);
    }


    protected final void
    postActionEvent(String command, int modifiers, boolean checked) {

	EventQueue  queue;

	if (checked || hasActionListener()) {
	    if ((queue = YoixAWTToolkit.getSystemEventQueue()) != null) {
		queue.postEvent(
		    new ActionEvent(
			this,
			AWTEvent.RESERVED_ID_MAX + 1,
			command,
			modifiers
		    )
		);
	    }
	}
    }


    protected final void
    postItemEvent(YoixObject item, boolean selected) {

	postItemEvent(item, selected, false);
    }


    protected final void
    postItemEvent(YoixObject item, boolean selected, boolean checked) {

	EventQueue  queue;

	if (checked || hasItemListener()) {
	    if ((queue = YoixAWTToolkit.getSystemEventQueue()) != null) {
		queue.postEvent(
		    new ItemEvent(
			this,
			AWTEvent.RESERVED_ID_MAX + 1,
			item,
			selected ? ItemEvent.SELECTED : ItemEvent.DESELECTED
		    )
		);
	    }
	}
    }

    ///////////////////////////////////
    //
    // YoixSwingJCanvas Methods
    //
    ///////////////////////////////////

    public synchronized void
    addActionListener(ActionListener listener) {

	actionlistener = AWTEventMulticaster.add(actionlistener, listener);
	if (actionlistener != null) {
	    if (panbuttonmask == 0)
		addMouseListener(this);
	}
    }


    final Color
    getBorderColor() {

	return(bordercolor);
    }


    final synchronized Insets
    getBorderInsets() {

	return((Insets)(borderinsets != null ? borderinsets : ZEROINSETS).clone());
    }


    protected final Image
    getFilteredImage(int hints) {

	Rectangle  bounds;
	Image      image;
	int        height;
	int        width;

	//
	// Old versions always synchronized, but as a precaution we decided
	// the most common case (no background image) shouldn't bother. We
	// wanted to avoid any chance of deadlock, despite the fact that we
	// had no evidence the old implementation caused problems.
	//

	if (backgroundimage != null) {
	    synchronized(this) {
		image = (filteredimage != null) ? filteredimage : backgroundimage;
		if (image != null) {
		    bounds = getInteriorBounds();
		    height = image.getHeight(null);
		    width = image.getWidth(null);
		    if ((hints & YOIX_SCALE_TILE) == 0) {
			if ((hints & YOIX_SCALE_NONE) == 0) {
			    if (bounds.height != height || bounds.width != width) {
				image = IMAGEOBSERVER.scaleImage(
				    image,
				    bounds.width,
				    bounds.height,
				    hints,
				    null
				);
			    }
			}
		    }
		    filteredimage = image;
		}
	    }
	} else image = null;

	return(image);
    }


    final int
    getState() {

	return(state);
    }


    synchronized Point
    getOrigin() {

	YoixBodyMatrix  matrix;
	Point           point = new Point();
	double          coords[];

	if ((matrix = parent.getCTMBody()) != null) {
	    coords = matrix.itransform(0, 0);
	    ((Point2D)point).setLocation(coords[0], coords[1]);
	}
	return(point);
    }


    synchronized Point2D
    getOrigin2D() {

	YoixBodyMatrix  matrix;
	Point2D         point = new Point();
	double          coords[];

	if ((matrix = parent.getCTMBody()) != null) {
	    coords = matrix.itransform(0, 0);
	    point = new Point2D.Double(coords[0], coords[1]);
	}
	return(point);
    }


    public void
    paint(Graphics g) {

	paintBackground(g);
	paintBackgroundImage(g);
	paintCallback(g);
	paintBorder(borderinsets, g);
	paintBorder(g);
    }


    protected final void
    processEvent(AWTEvent e) {

	ActionListener  actionlistener;
	ItemListener    itemlistener;

	if (e instanceof ItemEvent) {
	    itemlistener = this.itemlistener;		// snapshot - just to be safe
	    if (itemlistener != null) {
		itemlistener.itemStateChanged(
		    new ItemEvent(
			(ItemSelectable)e.getSource(),
			ItemEvent.ITEM_STATE_CHANGED,
			((ItemEvent)e).getItem(),
			((ItemEvent)e).getStateChange()
		    )
		);
	    }
	} else if (e instanceof ActionEvent) {
	    actionlistener = this.actionlistener;	// snapshot - just to be safe
	    if (actionlistener != null) {
		actionlistener.actionPerformed(
		    new ActionEvent(
			e.getSource(),
			ActionEvent.ACTION_PERFORMED,
			((ActionEvent)e).getActionCommand(),
			((ActionEvent)e).getModifiers()
		    )
		);
	    }
	} else super.processEvent(e);
    }


    public synchronized void
    removeActionListener(ActionListener listener) {

	actionlistener = AWTEventMulticaster.remove(actionlistener, listener);
	if (actionlistener == null) {
	    if (panbuttonmask == 0)
		removeMouseListener(this);
	}
    }


    final synchronized void
    setAfterPan(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0) || obj.callable(1) || obj.callable(2))
		afterpan = obj;
	} else afterpan = null;
    }


    final synchronized void
    setAfterZoom(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0) || obj.callable(1) || obj.callable(2))
		afterzoom = obj;
	} else afterzoom = null;
    }


    final synchronized void
    setBorderColor(Color color) {

	bordercolor = color;
	repaint();
    }


    final synchronized void
    setBorderInsets(Insets insets) {

	this.borderinsets = insets;
	bottomx[0] = 0;
	bottomy[0] = 0;
	repaint();
    }


    public synchronized void
    setForeground(Color color) {

	Color  fg;

	if ((fg = getForeground()) != color) {
	    if (fg == null || fg.equals(color) == false) {
		super.setForeground(color);
		repaint();
	    }
	}
    }


    final synchronized void
    setOrigin(YoixObject obj) {

	YoixBodyMatrix  matrix;
	double          coords[];

	if (obj.notNull()) {
	    if ((matrix = parent.getCTMBody()) != null) {
		coords = matrix.itransform(0, 0);
		panBy(coords[0] - obj.getDouble(N_X, 0.0), coords[1] - obj.getDouble(N_Y, 0.0));
	    }
	}
    }


    final synchronized void
    setPanAndZoom(YoixObject obj) {

	//
	// Eventually might accept a dictionary, but an int is sufficient
	// for now.
	//

	if (obj.isInteger())
	    setPanAndZoom(obj.intValue());
    }


    final synchronized void
    setState(int state) {

	//
	// The border test was added on 5/14/08 as a way to stop unnecessary
	// repaint() calls. Should be OK because documentation says the state
	// field affects the appearance of the border. This change guarantees
	// there's no appearance change when there's no border, which seems
	// reasonable. Very small chance existing applications could notice,
	// but only if they had a paint() function that used state to decide
	// what to paint and also had no border. We think it's unlikely any
	// existing applications will notice the change.
	//

	if (this.state != state) {
	    if (this.state*state <= 0) {
		if (borderinsets != null && borderinsets.equals(ZEROINSETS) == false)
		    repaint();
	    }
	    this.state = state;
	    if (mouse == AVAILABLE)
		postItemEvent(YoixObject.newInt(state), state != 0);
	}
    }


    final synchronized void
    zoom(double scaling, Point lock) {

	YoixBodyMatrix  matrix;

	if ((matrix = parent.getCTMBody()) != null) {
	    handleZoom(
		scaling,
		matrix.itransform(lock.x, lock.y, null),
		matrix
	    );
	}
    }


    final synchronized void
    zoom(double scaling, YoixObject obj) {

	double  loc[];

	//
	// This is called from the builtin, and unfortunately in that case
	// it looks like we need to parent.getGraphics() to force the CTM
	// that we want to use here to be properly initialized. Definitely
	// a kludge that we should investigate, particularly because it's
	// not needed when the zoom request is triggered by an event an in
	// that case goes through the other method. Ideally all we should
	// have to do is call parent.getCTMBody() and not worry about it,
	// but at this point I wasn't prepared to make the change in that
	// method without completely understanding the behavoir.
	// 

	if (obj == null || obj.isPoint() || obj.isNull()) {
	    if (parent.getGraphics(this, null) != null) {	// initialization kludge
		if (obj != null)
		    loc = new double[] {obj.getDouble(N_X, 0), obj.getDouble(N_Y, 0)};
		else loc = new double[] {0.0, 0.0};
		handleZoom(scaling, loc, parent.getCTMBody());
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    afterPan(YoixObject bbox_new, YoixObject bbox_clean) {

	YoixObject  funct = afterpan;		// snapshot - just to be safe
	YoixObject  argv[];

	if (funct != null) {
	    if (funct.callable(2))
		argv = new YoixObject[] {bbox_new, bbox_clean};
	    else if (funct.callable(1))
		argv = new YoixObject[] {bbox_new};
	    else argv = new YoixObject[0];
	    call(funct, argv);
	}
    }


    private void
    afterZoom(double scaling, double loc[]) {

	YoixObject  funct = afterzoom;		// snapshot - just to be safe
	YoixObject  argv[];

	if (funct != null) {
	    if (funct.callable(2))
		argv = new YoixObject[] {YoixObject.newDouble(scaling), YoixObject.newPoint(loc)};
	    else if (funct.callable(1))
		argv = new YoixObject[] {YoixObject.newDouble(scaling)};
	    else argv = new YoixObject[0];
	    call(funct, argv);
	}
    }


    private Rectangle
    getInteriorBounds() {

	Rectangle  r = getBounds();
	Insets     b;

	if ((b = borderinsets) != null) {
	    r.x = b.left;
	    r.y = b.top;
	    r.width = r.width - b.left - b.right;
	    r.height = r.height - b.top - b.bottom;
	}

	return(r);
    }


    private void
    handleZoom(double scaling, double loc[], YoixBodyMatrix matrix) {

	if (loc != null && matrix != null) {
	    if (scaling != 1.0) {
		matrix.translate(loc[0], loc[1]);
		matrix.scale(scaling, scaling);
		matrix.translate(-loc[0], -loc[1]);
	    }
	    repaint();
	    if (afterzoom != null)
		afterZoom(scaling, loc);
	}
    }


    private void
    panBegin(Point point) {

	mousepoint = point;
    }


    private void
    panBy(double dx, double dy) {

	YoixBodyMatrix  matrix;
	YoixObject      funct;
	YoixObject      bbox_clean = null;
	Rectangle       bounds;
	Graphics        g;
	double          delta[];
	double          pixels[];

	//
	// We update the canvas, either by a full repaint() or by using
	// copyArea() and just repainting the two small rectangles that
	// need to be updated. The choice really depends on how smart the
	// application's Yoix paint() function is, so the decision should
	// controlled by the application. Using copyArea() generates two
	// paint() calls, which is OK if the paint() functon is smart and
	// uses its rectangle argument to reduce the amount of work it has
	// to do, but otherwise it might be the slower approach.
	//
	// NOTE - we do an unnecessary transform if we get here because
	// the user was dragging the mouse around, but it's probably not
	// worth eliminating right now.
	//

	if (dx != 0 || dy != 0) {
	    if ((matrix = parent.getCTMBody()) != null) {
		if ((funct = afterpan) != null)
		    bbox_clean = YoixMiscJFC.getDrawableBBox(this, matrix);
		pixels = matrix.dtransform(dx, dy);
		pixels[0] = Math.round(pixels[0]);
		pixels[1] = Math.round(pixels[1]);
		delta = matrix.idtransform(pixels[0], pixels[1]);
		matrix.translate(delta[0], delta[1]);
		if (pancopyarea) {
		    bounds = new Rectangle(getLocationOnScreen(), getSize());
		    //
		    // An arbitrary test that's probably not unreasonable.
		    // Do we want a way to control this decision??
		    //
		    if (Math.abs(pixels[0]) < bounds.width/2 && Math.abs(pixels[1]) < bounds.height/2)
			YoixMiscJFC.copyArea(parent.getContext(), 0, 0, 0, 0, delta[0], delta[1], true, false);
		    else paintImmediately(0, 0, bounds.width, bounds.height);
		} else repaint();
		if (funct != null)
		    afterPan(YoixMiscJFC.getDrawableBBox(this, matrix), bbox_clean);
	    }
	}
    }


    private void
    panDragged(Point point) {

	YoixBodyMatrix  matrix;
	double          coords[];

	if (mousepoint != null) {
	    if ((matrix = parent.getCTMBody()) != null) {
		coords = matrix.idtransform(point.x - mousepoint.x, point.y - mousepoint.y);
		panBy(coords[0], coords[1]);
		mousepoint = point;
	    }
	}
    }


    private void
    panEnd(Point point) {

    }


    private Color
    pickBorderColor(Color color, boolean top) {

	if (state >= 0) {
	    if (color == null)
		color = getBackground();
	    color = ((state > 0) ^ top) ? color.brighter() : color.darker();
	}
	return(color != null ? color : getForeground());
    }


    private int
    pickButtonMask(int bits) {

	int  mask = 0;

	if ((bits & 0x01) != 0)
	    mask |= YOIX_BUTTON1_MASK;
	if ((bits & 0x02) != 0)
	    mask |= YOIX_BUTTON2_MASK;
	if ((bits & 0x04) != 0)
	    mask |= YOIX_BUTTON3_MASK;
	return(mask);
    }


    private int
    pickModifierMask(int bits) {

	int  mask = 0;

	if ((bits & 0x01) != 0)
	    mask |= YOIX_SHIFT_DOWN_MASK;
	if ((bits & 0x02) != 0)
	    mask |= YOIX_CTRL_DOWN_MASK;
	if ((bits & 0x04) != 0)
	    mask |= YOIX_ALT_DOWN_MASK;
	return(mask);
    }


    private void
    resetBegin(Point point) {

	mousepoint = point;
    }


    private void
    resetEnd(Point point) {

	YoixBodyMatrix  matrix;

	if ((matrix = parent.getCTMBody()) != null) {
	    matrix.matrixReset();
	    repaint();
	}
    }


    final synchronized void
    setPanAndZoom(int flags) {

	int  bits;
	int  buttonmask;
	int  wheel;
	int  modifiers;
	int  extra;

	flags &= PANANDZOOM_MASK;
	if (flags != panandzoomflags) {
	    if ((flags & PAN_MASK) != (panandzoomflags & PAN_MASK)) {
		bits = (flags>>PAN_STARTBIT)&0xFF;
		buttonmask = pickButtonMask(bits&0x7);
		modifiers = pickModifierMask((bits >> 4)&0x7);
		extra = (bits>>7)&0x1;
		if (buttonmask == 0 && panbuttonmask != 0) {
		    if (actionlistener == null)
			removeMouseListener(this);
		    removeMouseMotionListener(this);
		} else if (buttonmask != 0 && panbuttonmask == 0) {
		    if (actionlistener == null)
			addMouseListener(this);
		    addMouseMotionListener(this);
		}
		panbuttonmask = buttonmask;
		panmodifiers = modifiers;
		pancopyarea = (extra != 0);
	    }

	    if ((flags & ZOOM_MASK) != (panandzoomflags & ZOOM_MASK)) {
		bits = (flags>>ZOOM_STARTBIT)&0xFF;
		wheel = (bits >> 3) & 0x01;
		modifiers = pickModifierMask((bits >> 4)&0x7);
		if (wheel == 0 && zoomwheel != 0)
		    removeMouseWheelListener(this);
		else addMouseWheelListener(this);
		zoomwheel = wheel;
		zoommodifiers = modifiers;
	    }

	    if ((flags & ZOOM_MASK) != (panandzoomflags & ZOOM_MASK)) {
		bits = (flags>>RESET_STARTBIT)&0xFF;
		resetbuttonmask = pickButtonMask(bits&0x7);
		resetmodifiers = pickModifierMask((bits >> 4)&0x7);
	    }
	    panandzoomflags = flags;
	}
    }
}

