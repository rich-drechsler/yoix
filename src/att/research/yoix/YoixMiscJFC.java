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
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;

public abstract
class YoixMiscJFC

    implements YoixAPI,
	       YoixConstants,
	       YoixConstantsImage,
	       YoixConstantsJFC

{

    //
    // Apparently this is the official way to indicate a second button on
    // Macs.
    //

    private static int  SECOND_BUTTON_MASK = YOIX_BUTTON1_MASK|YOIX_CTRL_MASK;

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public static boolean
    checkLookAndFeel(String lookfeelname) {

	return(checkLookAndFeel(lookfeelname, null));
    }


    public static boolean
    checkLookAndFeel(String lookfeelname, String themename) {

	LookAndFeel  lookfeel;
	boolean      result = false;
	Object       theme;
	String       name;

	if ((lookfeel = UIManager.getLookAndFeel()) != null) {
	    if ((name = lookfeel.getName()) != null) {
		if (lookfeelname == null || name.equalsIgnoreCase(lookfeelname)) {
		    if (themename != null) {
			theme = YoixReflect.invoke(
			    YoixReflect.invoke("javax.swing.plaf.metal.MetalLookAndFeel", "getCurrentTheme"),
			    "getName"
			);
			if (theme instanceof String)
			    result = themename.equalsIgnoreCase((String)theme);
		    } else result = true;
		}
	    }
	}
	return(result);
    }


    public static int
    cookModifiers(ActionEvent e) {

	return(cookModifiers(((ActionEvent)e).getModifiers()));
    }


    public static int
    cookModifiers(InputEvent e) {

	return(cookModifiers(((InputEvent)e).getModifiers()));
    }


    public static void
    dispose(Object window) {

	if (window instanceof Window || window instanceof YoixInterfaceWindow) {
	    new YoixVMDisposer(window);
	    Thread.yield();
	}
    }


    public static Rectangle
    getBoundsIntersectScreen(Component comp) {

	Rectangle  bounds;
	Dimension  size;
	Point      point;

	bounds = new Rectangle(comp.getLocationOnScreen(), comp.getSize());
	bounds = bounds.intersection(new Rectangle(YoixAWTToolkit.getScreenSize()));
	point = bounds.getLocation();
	SwingUtilities.convertPointFromScreen(point, comp);
	bounds.setLocation(point);
	return(bounds);
    }


    public static double
    getGray(Color color) {

	//
	// NTSC video standard conversion of RGB Color to a gray value.
	// Currently used by pickForeground() when it tries to pick the
	// foreground color that might be best when drawing text over a
	// particular background color.
	//

	return(0.3*color.getRed()/255.0 + 0.59*color.getGreen()/255.0 + 0.11*color.getBlue()/255.0);
    }


    public static Color
    getHSBAdjustedColor(Color color, double hue_factor, double saturation_factor, double brightness_factor) {

	float  hsb[];

	if (color != null) {
	    hue_factor = Math.max(0.0, hue_factor);
	    saturation_factor = Math.max(0.0, saturation_factor);
	    brightness_factor = Math.max(0.0, brightness_factor);
	    if (hue_factor != 1.0 || saturation_factor != 1.0 || brightness_factor != 1.0) {
		if ((hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)) != null) {
		    hsb[0] = (float)(hue_factor*hsb[0]);
		    hsb[1] = (float)Math.min(saturation_factor*hsb[1], 1.0);
		    hsb[2] = (float)Math.min(brightness_factor*hsb[2], 1.0);
		    color = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
		}
	    }
	}
	return(color);
    }


    public static JLayeredPane
    getJLayeredPane(Component comp) {

	JLayeredPane  layeredpane = null;
	Container     parent;

	//
	// Returns the JLayeredPane associated with the RootPaneContainer
	// that currently contains comp.
	//

	if (comp != null) {
	    for (parent = comp.getParent(); parent != null; parent = parent.getParent()) {
		if (parent instanceof RootPaneContainer)
		    layeredpane = ((RootPaneContainer)parent).getLayeredPane();
            }
	}
	return(layeredpane);
    }


    public static Container
    getJToolTipContainer(JLayeredPane layeredpane) {

	Component   layered[];
	Component   components[];
	Container   container = null;
	int         layer;
	int         m;
	int         n;

	if (layeredpane != null) {
	    layer = JLayeredPane.POPUP_LAYER.intValue();
	    if ((layered = layeredpane.getComponentsInLayer(layer)) != null) {
		for (n = 0; n < layered.length && container == null; n++) {
		    if (layered[n] instanceof Container && layered[n].isVisible()) {
			if ((components = ((Container)layered[n]).getComponents()) != null) {
			    for (m = 0; m < components.length; m++) {
				if (components[m] instanceof JToolTip) {
				    container = (Container)layered[n];
				    break;
				}
			    }
			}
		    }
		}
	    }
	}
	return(container);
    }


    public static Graphics
    getMaskedGraphics(Component comp) {

	return(getMaskedGraphics(comp, getJLayeredPane(comp), false));
    }


    public static Graphics
    getMaskedGraphics(Component comp, JLayeredPane layeredpane) {

	return(getMaskedGraphics(comp, layeredpane, false));
    }


    public static Graphics
    getMaskedGraphics(Component comp, JLayeredPane layeredpane, boolean force) {

	Component  components[];
	Rectangle  rect;
	Graphics   g = null;
	Point      origin;
	Shape      clip;
	Area       cliparea;
	Area       mask;
	int        layer;

	//
	// This really should only be called from the event thread because
	// getComponentsInLayer() grabs AWT component tree lock. Decided to
	// provide a way around the restriction, but it's not recommended
	// because you could end up in deadlock.
	//

	if (comp != null) {
	    if ((g = comp.getGraphics()) != null) {
		if (layeredpane != null) {
		    if (EventQueue.isDispatchThread() || force) {
			layer = JLayeredPane.POPUP_LAYER.intValue();
			if ((components = layeredpane.getComponentsInLayer(layer)) != null) {
			    origin = SwingUtilities.convertPoint(layeredpane, new Point(0, 0), comp);
			    mask = buildComponentMask(components, origin, false, new Area());
			    if (mask.isEmpty() == false) {
				if ((clip = g.getClip()) == null) {
				    rect = comp.getBounds();
				    rect.x = 0;
				    rect.y = 0;
				    clip = rect;
				}
				cliparea = new Area(clip);
				cliparea.subtract(mask);
				g.setClip(cliparea);
			    }
			}
		    }
		}
	    }
	}
	return(g);
    }


    public static Color
    getSaturationAdjustedColor(Color color, double factor) {

	return(getHSBAdjustedColor(color, 1.0, factor, 1.0));
    }


    public static void
    hideToolTip(Component comp) {

	ToolTipManager  manager;

	//
	// Setting the tooltip text to null doesn't automatically hide the
	// the tooltip, but disabling and enabling the manager does hide it.
	// Behavior likely also depends on what's done first, so make sure
	// you test carefully if you make changes here. There's currently
	// no technical reason why we need comp, but decided to require it
	// for the time being.
	//

	if (comp != null) {
	    if (comp instanceof JComponent)
		((JComponent)comp).setToolTipText(null);
	    manager = ToolTipManager.sharedInstance();
	    manager.setEnabled(false);
	    manager.setEnabled(true);
	}
    }


    public static Color
    pickForeground(Color background) {

	return(pickForeground(Color.black, Color.white, background));
    }


    public static Color
    pickForeground(Color color1, Color color2, Color background) {

	double  gray;
	Color   color;

	if (color1 != null && color2 != null) {
	    if (background != null) {
		gray = getGray(background);
		color = Math.abs(getGray(color1) - gray) >= Math.abs(getGray(color2) - gray) ? color1 : color2;
	    } else color = color1;
	} else if (color1 != null)
	    color = color1;
	else if (color2 != null)
	    color = color2;
	else color = Color.black;

	return(color);
    }


    public static void
    stopToolTip(Component comp) {

	ToolTipManager  manager;
	MouseEvent      event;

	//
	// Unfortunately it looks like mousePressed() in ToolTipManager is
	// the one method we can call to hide a tooltip and make sure the
	// other important ToolTipManager variables and timers are properly
	// reset. A kludge, but we don't think there's another mechanism,
	// at least not right now. Also, mousePressed() as implemented by
	// the ToolTipManager in Java 1.5 completely ignores its argument
	// (i.e., null would be a legitimate MouseEvent), but to be safe
	// we decided to try to build an appropriate MouseEvent.
	//

	if (comp != null) {
	    if (comp instanceof JComponent)
		((JComponent)comp).setToolTipText(null);
	    event = new MouseEvent(
		comp,
		MouseEvent.MOUSE_PRESSED,
		System.currentTimeMillis(),
		YOIX_BUTTON1_MASK,
		0,
		0,
		0,
		false
	    );
	    manager = ToolTipManager.sharedInstance();
	    manager.mousePressed(event);
	}
    }

    ///////////////////////////////////
    //
    // YoixMiscJFC Methods
    //
    ///////////////////////////////////

    static void
    copyArea(YoixObject obj, double x, double y, double width, double height, double dx, double dy, boolean repaint) {

	copyArea(obj, x, y, width, height, dx, dy, repaint, true);
    }


    static void
    copyArea(YoixObject obj, double x, double y, double width, double height, double dx, double dy, boolean repaint, boolean clip) {

	YoixBodyMatrix  currentmatrix;
	YoixBodyPath    sourcepath;
	YoixBodyPath    clippath = null;
	YoixObject      graphics;
	Rectangle       bounds;
	Rectangle       source;
	Rectangle       rect;
	Rectangle       hrect;
	Rectangle       vrect;
	Graphics        g;
	Object          drawable;
	double          shift[];
	Point           delta;

	//
	// Java's Graphics.copyArea() implementation currently complains if
	// if we use a Graphics with an AffineTransform that looks like it's
	// been scaled, rotated, or sheared, which is one reason why we have
	// to work much harder than you might expect. The problem exists in
	// 1.6 and earlier versions on Linux, Macs, and probably Windows.
	//
	// Another subtle change is that this implementation now works well
	// with rectangle builtins, like rectfill(), because we use the same
	// method, namely YoixBodyPath.pathAlignedRectangle(), to select the
	// device space points for rectangles. In other words, a rectangle
	// drawn with one of those builtins can be copied or moved around
	// using this method.
	//
	// NOTE - we use clipping to improve the behavior of copyArea() in
	// rotated coordinate systems. It's not perfect, but at least you
	// usually do get something quite reasonable, however the repaint
	// code probably still needs attention. Clipping support in Java's
	// copyArea() apparently appeared in version 1.6, so this approach
	// won't work if you're using 1.5 or older.
	//
	// NOTE - right now a width or height that's zero means we'll use
	// the bounds of the drawable to determine the rectangle that we
	// copy.
	//

	if (obj.isDrawable()) {
	    if ((drawable = obj.getManagedDrawable(null)) != null) {
		if ((graphics = obj.getObject(N_GRAPHICS)) != null) {
		    if (drawable instanceof Component)
			bounds = getBoundsIntersectScreen((Component)drawable);
		    else if (drawable instanceof Image)
			bounds = new Rectangle(0, 0, ((Image)drawable).getWidth(null), ((Image)drawable).getHeight(null));
		    else bounds = new Rectangle();
		    currentmatrix = (YoixBodyMatrix)graphics.getObject(N_CTM).body();
		    shift = currentmatrix.dtransform(dx, dy);
		    delta = new Point((int)Math.round(shift[0]), (int)Math.round(shift[1]));
		    if (delta.x != 0 || delta.y != 0) {
			if (width > 0 && height > 0) {		// somewhat questionable criterion
			    sourcepath = (YoixBodyPath)YoixMake.yoixType(T_PATH).body();
			    sourcepath.setOwner((YoixBodyGraphics)graphics.body());
			    sourcepath.pathAlignedRectangle(x, y, width, height);

			    if (clip) {
				shift = currentmatrix.idtransform(delta.x, delta.y);
				clippath = (YoixBodyPath)YoixMake.yoixType(T_PATH).body();
				clippath.setOwner((YoixBodyGraphics)graphics.body());
				clippath.pathAlignedRectangle(x + shift[0], y + shift[1], width, height);
			    }

			    source = ((GeneralPath)sourcepath.getManagedObject()).getBounds();
			    rect = bounds.intersection(source);
			} else {
			    source = bounds;
			    rect = bounds;
			}

			if (rect.isEmpty() == false) {
			    if ((g = ((YoixBodyGraphics)graphics.body()).getGraphics2D(false)) != null) {
				//
				// This should stop copyArea() complaints.
				//
				((Graphics2D)g).setTransform(new AffineTransform());
				if (clippath != null)
				    g.setClip((GeneralPath)clippath.getManagedObject());
				g.copyArea(rect.x, rect.y, rect.width, rect.height, delta.x, delta.y);
				g.dispose();
			    }
			}

			if (repaint) {
			    //
			    // Rectangles may currently paint a bit more than
			    // necessary when the area is completely contained
			    // in bounds, so there's room for improvement. Also
			    // eventually should implement the image repainting
			    // code (primarily in YoixBodyImage).
			    //
			    if (delta.x >= 0 && delta.y >= 0) {
				hrect = new Rectangle(
				    source.x, Math.max(source.y, bounds.y),
				    source.width + delta.x, delta.y
				).intersection(bounds);
				vrect = new Rectangle(
				    Math.max(source.x, bounds.x), source.y + delta.y,
				    delta.x, source.height
				).intersection(bounds);
			    } else if (delta.x >= 0 && delta.y < 0) {
				hrect = new Rectangle(
				    source.x, Math.min(source.y + source.height, bounds.y + bounds.height) + delta.y,
				    source.width + delta.x, -delta.y
				).intersection(bounds);
				vrect = new Rectangle(
				    Math.max(source.x, bounds.x), source.y + delta.y,
				    delta.x, source.height
				).intersection(bounds);
			    } else if (delta.x < 0 && delta.y >= 0) {
				hrect = new Rectangle(
				    source.x + delta.x, Math.max(source.y, bounds.y),
				    source.width, delta.y
				).intersection(bounds);
				vrect = new Rectangle(
				    Math.min(source.x + source.width, bounds.x + bounds.width) + delta.x, source.y,
				    -delta.x, source.height + delta.y
				).intersection(bounds);
			    } else if (delta.x < 0 && delta.y < 0) {
				hrect = new Rectangle(
				    source.x + delta.x, Math.min(source.y + source.height, bounds.y + bounds.height) + delta.y,
				    source.width - delta.x, -delta.y
				).intersection(bounds);
				vrect = new Rectangle(
				    Math.min(source.x + source.width, bounds.x + bounds.width) + delta.x, source.y + delta.y,
				    -delta.x, source.height
				).intersection(bounds);
			    } else {	// just makes the compiler happy
				hrect = new Rectangle();
				vrect = new Rectangle();
			    }

			    //
			    // No image repainting right now. Also didn't
			    // notice preformance change when we switched
			    // between repaint() and paintImmediately().
			    //

			    if (drawable instanceof JComponent) {
				if (hrect.isEmpty() == false)
				    ((JComponent)drawable).paintImmediately(hrect.x, hrect.y, hrect.width, hrect.height);
				if (vrect.isEmpty() == false)
				    ((JComponent)drawable).paintImmediately(vrect.x, vrect.y, vrect.width, vrect.height);
			    } else if (drawable instanceof Component) {
				if (hrect.isEmpty() == false)
				    ((Component)drawable).repaint(hrect.x, hrect.y, hrect.width, hrect.height);
				if (vrect.isEmpty() == false)
				    ((Component)drawable).repaint(vrect.x, vrect.y, vrect.width, vrect.height);
			    }
			}
		    }
		}
	    }
	}
    }


    static int
    getButtonsPressed(InputEvent event) {

	int  modifiers;
	int  pressed;

	modifiers = event.getModifiersEx();
	pressed = ((modifiers&YOIX_BUTTON1_DOWN_MASK) == YOIX_BUTTON1_DOWN_MASK ? 1 : 0)
	    + ((modifiers&YOIX_BUTTON2_DOWN_MASK) == YOIX_BUTTON2_DOWN_MASK ? 1 : 0)
	    + ((modifiers&YOIX_BUTTON3_DOWN_MASK) == YOIX_BUTTON3_DOWN_MASK ? 1 : 0);

	return(pressed);
    }


    static Area
    getComponentMask(Container container) {

	return(getComponentMask(container, new Point(0, 0)));
    }


    static Area
    getComponentMask(Container container, Point origin) {

	Component  components[];
	Area       mask = null;

	//
	// Returns a mask that covers the components that are currently
	// showing in container. The mask doesn't include components,
	// like a JPanel, that are transparent and mostly used to hold
	// components, but it does carefully look through them. Test
	// is a little harder than you might expect because JComponent
	// extends Container and it's doubtful we really want to look
	// through every JComponent.
	// 

	if (container != null) {
	    if (container.isShowing()) {
		if ((components = container.getComponents()) != null) {
		    mask = buildComponentMask(components, origin, false, new Area());
		    if (mask.isEmpty())
			mask = null;
		}
	    }
	}
	return(mask);
    }


    static Area
    getDrawableArea(Object drawable) {

	Container  contentpane;
	Rectangle  rect;
	Point      corner;
	Point      origin;
	Area       area = null;
	Area       mask;

	if (drawable instanceof Container) {
	    if (drawable instanceof RootPaneContainer) {
		origin = ((RootPaneContainer)drawable).getRootPane().getLocation();
		contentpane = ((RootPaneContainer)drawable).getContentPane();
		if (contentpane != null) {
		    corner = contentpane.getLocation();
		    origin.translate(corner.x, corner.y);
		}
	    } else {
		origin = new Point(0, 0);
		contentpane = (Container)drawable;
	    }
	    rect = new Rectangle(origin, contentpane.getSize());
	    rect.width = Math.max(rect.width - 1, 0);
	    rect.height = Math.max(rect.height - 1, 0);
	    area = new Area(rect);
	    if ((mask = getComponentMask(contentpane, origin)) != null)
		area.subtract(mask);
	} else if (drawable instanceof Component) {
	    rect = new Rectangle(((Component)drawable).getSize());
	    rect.width = Math.max(rect.width - 1, 0);
	    rect.height = Math.max(rect.height - 1, 0);
	    area = new Area(rect);
	} else if (drawable instanceof Image) {
	    rect = new Rectangle();
	    rect.width = Math.max(((Image)drawable).getWidth(null) - 1, 0);
	    rect.height = Math.max(((Image)drawable).getHeight(null) - 1, 0);
	    area = new Area(rect);
	}

	return(area);
    }


    static YoixObject
    getDrawableBBox(Object drawable, YoixBodyMatrix matrix) {

	return(YoixObject.newRectangle(getDrawableBounds2D(drawable, matrix)));
    }


    static Rectangle2D
    getDrawableBounds2D(Object drawable, YoixBodyMatrix matrix) {

	Rectangle2D  bounds = null;
	Area         area;

	if (matrix != null) {
	    if ((area = getDrawableArea(drawable)) != null)
		bounds = YoixMake.javaBBox(area.getBounds2D(), matrix);
	}
	return(bounds);
    }
    

    static Point
    getWindowOffset(Object drawable) {

	JRootPane  rootpane;
	Object     decorations = null;
	Point      offset = null;

	//
	// Decided that measuring the window decorations that are added
	// to an AWT Frame or Dialog using Swing versions is probably
	// OK. Not tested much, so if you disagree change the code.
	//

	if (drawable instanceof Component) {
	    if (drawable instanceof YoixInterfaceWindow) {
		if (drawable instanceof RootPaneContainer && ((Component)drawable).isDisplayable()) {
		    rootpane = ((RootPaneContainer)drawable).getRootPane();
		    offset = rootpane.getLocation();
		} else {
		    try {
			if (drawable instanceof JFrame)
			    decorations = new JFrame();
			else if (drawable instanceof JDialog)
			    decorations = new JDialog();
			else if (drawable instanceof Frame)
			    decorations = new JFrame();
			else if (drawable instanceof Dialog)
			    decorations = new JDialog();
			else if (drawable instanceof JInternalFrame)
			    decorations = new JInternalFrame();
			if (decorations instanceof RootPaneContainer) {
			    if (decorations instanceof Window)
				((Window)decorations).pack();
			    else if (decorations instanceof JInternalFrame)
				((JInternalFrame)decorations).pack();
			    rootpane = ((RootPaneContainer)decorations).getRootPane();
			    offset = rootpane.getLocation();
			}
		    }
		    finally {
			if (decorations != null) {
			    if (decorations instanceof Window)
				((Window)decorations).dispose();
			    else if (decorations instanceof JInternalFrame)
				((JInternalFrame)decorations).dispose();
			    decorations = null;
			}
		    }
		}
	    } else offset = ((Component)drawable).getLocation();
	}
	return(offset != null ? offset : new Point());
    }


    static int
    guessExtent(Font font, String charset) {

	YoixAWTFontMetrics  fm;
	BufferedImage       image;
	Graphics            g;
	int                 white;
	int                 top;
	int                 bottom;
	int                 height;
	int                 width;
	int                 x;
	int                 y;

	//
	// Draws a few characters into an image, grabs the pixels, and
	// uses them to estimate the extent of the font. The caller can
	// use the number to pick a point-size scaling factor. Original
	// version, which was written for Java 1.1.X, had trouble when
	// it tried overstriking characters. Suspect it would no longer
	// be a problem, but we didn't investigate because this method
	// probably isn't used anymore.
	//

	top = -1;
	bottom = -2;

	if ((fm = YoixAWTToolkit.getFontMetrics(font)) != null) {
	    charset = (charset != null) ? charset : "\324jOgyp";
	    height = 2*(fm.getMaxAscent() + fm.getMaxDescent());
	    width = (int)(1.25*charset.length()*fm.getMaxAdvance());

	    image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    g = image.getGraphics();
	    g.setFont(font);
	    g.setColor(Color.white);
	    g.fillRect(0, 0, width, height);
	    white = image.getRGB(0, 0);
	    g.setColor(Color.black);
	    g.drawString(charset, 1, 3*height/4);

	    for (y = 0; y < height; y++) {
		for (x = 0; x < width; x++) {
		    if (image.getRGB(x, y) != white) {
			if (top < 0)
			    top = y;
			bottom = y;
			break;
		    }
		}
	    }
	    g.dispose();
	    image = null;
	}
	return(bottom - top);		// was return(bottom - top + 1)
    }


    static void
    paintBackground(Component comp, Graphics g) {

	Rectangle  rect;
	Color      color;

	//
	// Even if it was possible we wouldn't want to use g.clearRect()
	// because we might be drawing in an image (e.g., comp is a double
	// buffered Swing component) and the background color of the image
	// probably is system dependent.
	//
	// NOTE - the isOpaque() test was added on 1/17/06. Seems pretty
	// reasonable, but dealing with YoixSwingJLayeredPane.java can be
	// a little tricky because it overrides isOpaque() and that means
	// YoixSwingJLayeredPane.java has to be carful about when it calls
	// this method. The current solution in YoixSwingJLayeredPane.java
	// is sufficent for now, but probably needs work.
	//
	// NOTE - the old version of this method assumed g could be cast to
	// to a Graphics2D, which isn't necessarily the case if we got here
	// through Java's print mechanism. Anyway the Graphics2D assumption
	// looks like it really wasn't necessary so we changed the code on
	// 3/5/08 and Java's printing should now be happy. Should also note
	// that the old method that did this work to an additional shape
	// argument, but it was always null so we tossed that method.
	// 

	if (comp != null) {
	    if (comp.isOpaque()) {		// test added on 1/17/06
		if ((rect = g.getClipBounds()) == null)
		    rect = new Rectangle(comp.getSize());
		if (rect != null) {
		    color = g.getColor();
		    g.setColor(comp.getBackground());
		    g.fillRect(rect.x, rect.y, rect.width, rect.height);
		    g.setColor(color);
		}
	    }
	}
    }


    static void
    paintImage(Component comp, Image image, int hints, Graphics g) {

	paintImage(comp, image, null, null, (hints & YOIX_SCALE_TILE) != 0, g);
    }


    static void
    paintImage(Component comp, Image image, boolean tile, Graphics g) {

	paintImage(comp, image, null, null, tile, g);
    }


    static void
    paintImage(Component comp, Image image, Point offset, int hints, Graphics g) {

	paintImage(comp, image, offset, null, (hints & YOIX_SCALE_TILE) != 0, g);
    }


    static void
    paintImage(Component comp, Image image, Point offset, boolean tile, Graphics g) {

	paintImage(comp, image, offset, null, tile, g);
    }


    static void
    paintImage(Component comp, Image image, Point offset, Rectangle rect, boolean tile, Graphics g) {

	int  height;
	int  width;
	int  x0 = 0;
	int  y0 = 0;
	int  x1;
	int  y1;
	int  x2;
	int  y2;
	int  x;
	int  y;

	if (image != null && g != null) {
	    height = image.getHeight(null);
	    width = image.getWidth(null);
	    if (width > 0 && height > 0) {
		if (offset != null) {
		    x0 = offset.x;
		    y0 = offset.y;
		}
		if (tile) {
		    if (rect == null) {
			if ((rect = g.getClipBounds()) == null) {
			    if (comp != null) {
				if (offset != null)
				    rect = new Rectangle(offset, comp.getSize());
				else rect = new Rectangle(comp.getSize());
			    }
			}
		    }
		    if (rect != null) {
			x1 = x0 + width*((rect.x - x0)/width);
			x2 = x0 + width*((rect.x - x0 + rect.width + width - 1)/width);
			y1 = y0 + height*((rect.y - y0)/height);
			y2 = y0 + height*((rect.y - y0 + rect.height + height - 1)/height);
			for (y = y1; y < y2; y += height) {
			    for (x = x1; x < x2; x += width)
				g.drawImage(image, x, y, null);
			}
		    }
		} else g.drawImage(image, x0, y0, null);
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static Area
    buildComponentMask(Component components[], Point origin, boolean maskborders, Area mask) {

	Component  comp;
	Rectangle  rect;
	Border     border;
	Insets     insets;
	int        n;

	if (components != null) {
	    for (n = 0; n < components.length; n++) {
		if ((comp = components[n]) != null) {
		    if (comp.isShowing()) {
			rect = comp.getBounds();
			rect.translate(origin.x, origin.y);
			if (isTransparentContainer(comp)) {
			    if (maskborders && comp instanceof JComponent) {
				if ((border = ((JComponent)comp).getBorder()) != null) {
				    insets = border.getBorderInsets(comp);
				    if (insets.equals(ZEROINSETS) == false) {
					mask.add(new Area(rect));
					mask.subtract(new Area(new Rectangle(
					    rect.x + insets.left,
					    rect.y + insets.top,
					    rect.width - (insets.left + insets.right),
					    rect.height - (insets.top + insets.bottom)
					)));
				    }
				}
			    }
			    mask = buildComponentMask(
				((Container)comp).getComponents(),
				rect.getLocation(),
				maskborders,
				mask
			    );
			} else mask.add(new Area(rect));
		    }
		}
	    }
	}

	return(mask);
    }


    private static int
    cookModifiers(int modifiers) {

	//
	// Recently (9/19/05) made some changes here and also changed the
	// interpreter's default buttonmodel (in file YoixModuleVM.java)
	// from 1 to 0, which means one button mice on Macs no longer get
	// automatic special treatement. You can use the -b1 command line
	// option or a "buttonmodel" property file entry (see comments in
	// YoixMain.java for more info) to force the interpreter to change
	// models.
	//
	// Also note that earlier versions took an id argument and would
	// only apply the mappings to certain id's. Seemed wrong, so that
	// has also changed - at least for now.
	//

	switch (VM.getInt(N_BUTTONMODEL)) {
	    case 0:		// raw model - for everyone
	    default:
		break;

	    case 1:		// raw model - except for Mac
		if (ISMAC && (modifiers & SECOND_BUTTON_MASK) == SECOND_BUTTON_MASK) {
		    modifiers &= ~SECOND_BUTTON_MASK;
		    modifiers |= YOIX_BUTTON3_MASK;
		}
		break;

	    case 2:		// map button 2 to button 3
		if ((modifiers & YOIX_BUTTON_MASK) == YOIX_BUTTON2_MASK) {
		    modifiers &= ~YOIX_BUTTON2_MASK;
		    modifiers |= YOIX_BUTTON3_MASK;
		}
		break;

	    case 3:		// combines models 1 and 2
		if (ISMAC && (modifiers & SECOND_BUTTON_MASK) == SECOND_BUTTON_MASK) {
		    modifiers &= ~SECOND_BUTTON_MASK;
		    modifiers |= YOIX_BUTTON3_MASK;
		}
		if ((modifiers & YOIX_BUTTON_MASK) == YOIX_BUTTON2_MASK) {
		    modifiers &= ~YOIX_BUTTON2_MASK;
		    modifiers |= YOIX_BUTTON3_MASK;
		}
		break;
	}
	return(modifiers);
    }


    private static boolean
    isTransparentContainer(Object arg) {

	boolean  result;

	//
	// Eventually may accept a few more Swing containers.
	//

	if (arg instanceof JComponent) {
	    if (((JComponent)arg).isOpaque() == false) {
		if ((arg instanceof YoixSwingJCanvas) == false) {
		    result = arg instanceof JPanel ||
			arg instanceof JSplitPane ||
			arg instanceof JFileChooser;
		} else result = false;
	    } else result = false;
	} else result = false;

	return(result);
    }
}

