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
import java.awt.geom.*;
import java.awt.datatransfer.Clipboard;
import java.awt.image.*;
import java.net.*;
import java.util.*;

public abstract
class YoixAWTToolkit

    implements YoixConstants,
	       YoixConstantsImage

{

    //
    // We don't initialize toolkit until it's really needed because we want
    // to avoid the "focus grabbing" behavoir that apparently happens on Macs
    // whenever AWT code is triggered. Makes our job more difficult, but it's
    // probably important, at least until the Mac behavior changes.
    // 

    private static Toolkit  toolkit = null;
    private static boolean  initialized = false;

    ///////////////////////////////////
    //
    // YoixAWTToolkit Methods
    //
    ///////////////////////////////////

    public static void
    beep() {

	Toolkit  tk;

	if ((tk = getDefaultToolkit()) != null)
	    tk.beep();
    }


    public static int
    checkImage(Image image, int width, int height, ImageObserver observer) {

	Toolkit  tk;
	int      status = YOIX_IMAGE_ERROR;

	if ((tk = getDefaultToolkit()) != null)
	    status = tk.checkImage(image, width, height, observer);
	return(status);
    }


    public static Cursor
    createCustomCursor(Image image, Point hotspot, String name) {

	Rectangle  rect;
	Rectangle  interior;
	Point2D    intersection;
	Toolkit    tk;
	Cursor     cursor = null;
	int        width;
	int        height;

	//
	// We currently only handle BufferedImages, despite the fact that
	// the image parameter accepts more.
	//

	if ((tk = getDefaultToolkit()) != null) {
	    if (image instanceof BufferedImage) {
		if (hotspot != null) {
		    width = ((BufferedImage)image).getWidth();
		    height = ((BufferedImage)image).getHeight();
		    rect = new Rectangle(getBestCursorSize(width, height));
		    if (rect.contains(hotspot) == false) {
			interior = new Rectangle(0, 0, width - 1, height - 1);
			intersection = YoixMiscGeom.getFirstIntersection(hotspot, interior);
			if (intersection != null) {
			    hotspot = new Point(
				(int)intersection.getX(),
				(int)intersection.getY()
			    );
			}
			if (rect.contains(hotspot) == false)
			    hotspot = new Point(0, 0);
		    }
		} else hotspot = new Point(0, 0);
		try {
		    cursor = tk.createCustomCursor(image, hotspot, name);
		}
		catch(HeadlessException e) {}
	    }
	}

	return(cursor);
    }


    public static Dimension
    getBestCursorSize(int width, int height) {

	Dimension  size = null;
	Toolkit    tk;

	if ((tk = getDefaultToolkit()) != null) {
	    try {
		size = tk.getBestCursorSize(width, height);
	    }
	    catch(HeadlessException e) {}
	}
	return(size != null ? size : new Dimension(0, 0));
    }


    public static Toolkit
    getDefaultToolkit() {

	if (initialized == false) {
	    try {
	        toolkit = Toolkit.getDefaultToolkit();
	    }
	    catch(Throwable t) {
		VM.warn(t);
	    }
	    finally {
		initialized = true;
	    }
	}
	return(toolkit);
    }


    public static String[]
    getFontList() {

	Toolkit  tk;
	String   list[] = null;

	if ((tk = getDefaultToolkit()) != null)
	    list = tk.getFontList();
	return(list);
    }


    public static YoixAWTFontMetrics
    getFontMetrics(Font font) {

	FontMetrics  fm = null;
	Toolkit      tk;

	if ((tk = getDefaultToolkit()) != null)
	    fm = tk.getFontMetrics(font);
	return(fm != null ? new YoixAWTFontMetrics(fm) : null);
    }


    public static Image
    getImage(String filename) {

	Toolkit  tk;
	Image    image = null;

	if ((tk = getDefaultToolkit()) != null)
	    image = tk.getImage(filename);
	return(image);
    }


    public static Image
    getImage(URL url) {

	Toolkit  tk;
	Image    image = null;

	if ((tk = getDefaultToolkit()) != null)
	    image = tk.getImage(url);
	return(image);
    }


    public static int
    getMaximumCursorColors() {

	Toolkit  tk;
	int      colors = 0;

	if ((tk = getDefaultToolkit()) != null) {
	    try {
		colors = tk.getMaximumCursorColors();
	    }
	    catch(HeadlessException e) {}
	}
	return(colors);
    }


    public static PrintJob
    getPrintJob(Frame frame, String jobtitle, Properties props) {

	PrintJob  printjob = null;
	Toolkit   tk;

	if ((tk = getDefaultToolkit()) != null) {
	    try {
		printjob = tk.getPrintJob(frame, jobtitle, props);
	    }
	    catch(NullPointerException e) {}
	}
	return(printjob);
    }


    public static Rectangle
    getScreenBounds() {

	//
	// Could have a method that takes a boolean argument and uses it
	// to decide whether insets should be accounted for.
	//

	return(new Rectangle(0, 0, getScreenWidth(), getScreenHeight()));
    }


    public static int
    getScreenHeight() {

	Dimension  size;
	Toolkit    tk;
	int        height = 0;

	if ((tk = getDefaultToolkit()) != null) {
	    try {
		size = tk.getScreenSize();
		height = size.height;
	    }
	    catch(HeadlessException e) {}
	}
	return(height);
    }


    public static Insets
    getScreenInsets() {

	GraphicsConfiguration  gc = null;

	if (GraphicsEnvironment.isHeadless() == false)
	    gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	return(getScreenInsets(gc));
    }


    public static Insets
    getScreenInsets(GraphicsConfiguration gc) {

	Toolkit  tk;
	Insets   insets = null;

	//
	// Unfortunately this doesn't seem to work if you're using Gnome
	// on Linux. There are long-standing Java bug reports.
	//

	if (gc != null) {
	    if ((tk = getDefaultToolkit()) != null) {
		try {
		    insets = tk.getScreenInsets(gc);
		}
		catch(HeadlessException e) {}
	    }
	}
	return(insets != null ? insets : new Insets(0, 0, 0, 0));
    }


    public static int
    getScreenResolution() {

	Toolkit  tk;
	int      resolution = 72;

	if ((tk = getDefaultToolkit()) != null) {
	    try {
		resolution = tk.getScreenResolution();
	    }
	    catch(HeadlessException e) {}
	}
	return(resolution);
    }


    public static Dimension
    getScreenSize() {

	Dimension  size = null;
	Toolkit    tk;

	if ((tk = getDefaultToolkit()) != null) {
	    try {
		size = tk.getScreenSize();
	    }
	    catch(HeadlessException e) {}
	}
	return(size != null ? size : new Dimension(0, 0));
    }


    public static int
    getScreenWidth() {

	Dimension  size;
	Toolkit    tk;
	int        width = 0;

	if ((tk = getDefaultToolkit()) != null) {
	    try {
		size = tk.getScreenSize();
		width = size.width;
	    }
	    catch(HeadlessException e) {}
	}
	return(width);
    }


    public static Clipboard
    getSystemClipboard() {

	Clipboard  clipboard = null;
	Toolkit    tk;

	if ((tk = getDefaultToolkit()) != null) {
	    try {
		clipboard = tk.getSystemClipboard();
	    }
	    catch(HeadlessException e) {}
	}
	return(clipboard);
    }


    public static EventQueue
    getSystemEventQueue() {

	EventQueue  eventqueue = null;
	Toolkit     tk;

	if ((tk = getDefaultToolkit()) != null)
	    eventqueue = tk.getSystemEventQueue();
	return(eventqueue);
    }


    public static boolean
    prepareImage(Image image, int width, int height, ImageObserver observer) {

	boolean  result = false;
	Toolkit  tk;
	
	if ((tk = getDefaultToolkit()) != null)
	    result = tk.prepareImage(image, width, height, observer);
	return(result);
    }
}

