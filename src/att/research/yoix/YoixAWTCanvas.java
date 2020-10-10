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

public
class YoixAWTCanvas extends Canvas

    implements ItemSelectable,
	       MouseListener,
	       YoixAPI,
	       YoixAPIProtected,
	       YoixConstants,
	       YoixConstantsAWT,
	       YoixConstantsImage,
	       YoixInterfaceDrawable

{

    protected YoixBodyComponent  parent;
    protected YoixObject         data;

    private Insets  borderinsets = null;
    private Color   bordercolor = null;
    private int     state = 0;

    //
    // Background image support.
    //

    private Image  backgroundimage = null;
    private Image  filteredimage = null;
    private int    backgroundhints = 0;

    //
    // A Yoix paint function.
    //

    private YoixObject  paint = null;

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

    private int  mouse = AVAILABLE;
    private int  pressedstate;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    protected
    YoixAWTCanvas(YoixObject data, YoixBodyComponent parent) {

	this.data = data;
	this.parent = parent;
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

	int  button;

	if (mouse == AVAILABLE) {
	    switch (button = YoixMiscJFC.cookModifiers(e) & YOIX_BUTTON_MASK) {
		case YOIX_BUTTON1_MASK:
		case YOIX_BUTTON2_MASK:
		case YOIX_BUTTON3_MASK:
		    if ((button & data.getInt(N_BUTTONMASK, YOIX_BUTTON1_MASK)) != 0) {
			if (state == 0 || data.getBoolean(N_STICKY)) {
			    mouse = PRESSED;
			    pressedstate = (state == 0 ? 1 : 0);
			    setState(pressedstate);
			}
		    }
		    break;
	    }
	}
    }


    public synchronized void
    mouseReleased(MouseEvent e) {

	if (mouse != AVAILABLE) {
	    switch (YoixMiscJFC.cookModifiers(e) & YOIX_BUTTON_MASK) {
		case YOIX_BUTTON1_MASK:
		case YOIX_BUTTON2_MASK:
		case YOIX_BUTTON3_MASK:
		    if (mouse == PRESSED) {
			if (state == pressedstate) {
			    if (data.getBoolean(N_STICKY) == false)
				setState(state == 0 ? 1 : 0);
			    else postItemEvent(YoixObject.newInt(state), state != 0);
			    postActionEvent(null, e.getModifiers());
			}
		    }
		    mouse = AVAILABLE;
		    break;
	    }
	}
    }

    ///////////////////////////////////
    //
    // YoixInterfaceDrawable Methods
    //
    ///////////////////////////////////

    public Graphics
    getPaintGraphics() {

	return(super.getGraphics());
    }


    public boolean
    isDrawable() {

	return(true);
    }


    public boolean
    isPaintable() {

	return(true);
    }


    public boolean
    isTileable() {

	return(true);
    }


    public final void
    paintBackground(Graphics g) {

	YoixMiscJFC.paintBackground(this, g);
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


    public final synchronized void
    setBackgroundHints(int hints) {

	if (isTileable()) {
	    if (this.backgroundhints != hints) {
		this.backgroundhints = hints;
		this.filteredimage = null;
		repaint();
	    }
	}
    }


    public final synchronized void
    setBackgroundImage(Image image) {

	if (isTileable()) {
	    if (this.backgroundimage != image) {
		this.backgroundimage = image;
		this.filteredimage = null;
		repaint();
	    }
	}
    }


    public synchronized void
    setPaint(YoixObject obj) {

	if (isPaintable())
	    paint = obj.notNull() ? obj : null;
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


    protected Dimension
    getLayoutSize(String name, Dimension size) {

	YoixObject  obj;
	Dimension   defaultsize;

	//
	// Classes that extend this one can override this method when
	// they need more control over layout sizing.
	//

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
    // YoixAWTCanvas Methods
    //
    ///////////////////////////////////

    public synchronized void
    addActionListener(ActionListener listener) {

	actionlistener = AWTEventMulticaster.add(actionlistener, listener);
	if (actionlistener != null)
	    addMouseListener(this);
    }


    protected void
    finalize() {

	data = null;
	parent = null;
	paint = null;
	backgroundimage = null;
	filteredimage = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    final Color
    getBorderColor() {

	return(bordercolor);
    }


    final synchronized Insets
    getBorderInsets() {

	return((Insets)(borderinsets != null ? borderinsets : ZEROINSETS).clone());
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


    final int
    getState() {

	return(state);
    }


    public void
    paint(Graphics g) {

	paintBackgroundImage(g);
	paintCallback(g);
	paintBorder(borderinsets, g);
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
	if (actionlistener == null)
	    removeMouseListener(this);
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
    setState(int state) {

	if (this.state != state) {
	    if (this.state*state <= 0)
		repaint();
	    this.state = state;
	    if (mouse == AVAILABLE)
		postItemEvent(YoixObject.newInt(state), state != 0);
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private YoixBodyMatrix
    getCTMBody() {

	YoixObject  graphics;
	YoixObject  mtx;

	if ((graphics = data.getObject(N_GRAPHICS)) != null) {
	    if ((mtx = graphics.getObject(N_CTM)) == null)
		mtx = VM.getDefaultMatrix();
	} else mtx = VM.getDefaultMatrix();
	return((YoixBodyMatrix)mtx.body());
    }


    private Image
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
    paintCallback(Graphics g) {

	YoixObject  funct;
	YoixObject  clip;
	Rectangle   bounds;

	if ((funct = paint) != null && funct.notNull()) {
	    if (funct.callable(1)) {
		bounds = g.getClipBounds();
		if (bounds == null || getSize().equals(bounds.getSize()))
		    clip = YoixObject.newNull();
		else clip = YoixMake.yoixBBox(bounds, getCTMBody());
		parent.call(funct, new YoixObject[] {clip});
	    } else parent.call(funct, new YoixObject[0]);
	}
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
}

