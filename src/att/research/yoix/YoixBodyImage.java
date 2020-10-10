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
import java.awt.color.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

final
class YoixBodyImage extends YoixPointerActive

    implements YoixConstantsImage,
	       Runnable

{

    //
    // Much of this, particularly the internal thread that handles the
    // graphics related requests, undoubtedly looks like overkill, but
    // getting consistent results without deadlock or restricting the
    // threads that can draw in an image was challenging.
    //
    // NOTE - the internal thread that handles graphics request usually
    // stops after the queue is emptied, which is important for garbage
    // collection, but setting image's N_PERSISTENT to non-zero changes
    // that behavior. I suspect we'll eventually provide a little more
    // control and maybe even let images (like processes) pick a parent
    // window that cleans up - later.
    //

    private BufferedImage  currentimage = null;
    private String         currentpath = null;
    private double         currentmetrics[] = null;
    private Image          currentsource = null;
    private Color          currentbackground = null;
    private int            currenttransparency = -1;
    private int            currentmodel = 0;
    private int            currenttype = -1;
    private int            currentwidth = 0;
    private int            currentheight = 0;

    //
    // A Yoix paint function.
    //

    private YoixObject  paint;

    //
    // We use a separate thread to help impose some order on graphics
    // operations, which can be initiated by any thread, and prevent
    // (or at least try to prevent) mysterious deadlocks.
    //

    private YoixThread  thread = null;
    private boolean     threadenabled = true;
    private Vector      queue = new Vector();
    private int         priority = Thread.NORM_PRIORITY;

    //
    // Flag tells us we're pretty much finished with initialization.
    //

    private boolean  initialized = false;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(25);

    static {
	activefields.put(N_BACKGROUND, new Integer(V_BACKGROUND));
	activefields.put(N_COLUMNS, new Integer(V_COLUMNS));
	activefields.put(N_CONVERT, new Integer(V_CONVERT));
	activefields.put(N_CONVOLVE, new Integer(V_CONVOLVE));
	activefields.put(N_GETPIXEL, new Integer(V_GETPIXEL));
	activefields.put(N_GRAPHICS, new Integer(V_GRAPHICS));
	activefields.put(N_HOTSPOT, new Integer(V_HOTSPOT));
	activefields.put(N_METRICS, new Integer(V_METRICS));
	activefields.put(N_MODEL, new Integer(V_MODEL));
	activefields.put(N_OPAQUE, new Integer(V_OPAQUE));
	activefields.put(N_PAINT, new Integer(V_PAINT));
	activefields.put(N_PERSISTENT, new Integer(V_PERSISTENT));
	activefields.put(N_PREFERREDSIZE, new Integer(V_PREFERREDSIZE));
	activefields.put(N_REPAINT, new Integer(V_REPAINT));
	activefields.put(N_REPLACE, new Integer(V_REPLACE));
	activefields.put(N_RESCALE, new Integer(V_RESCALE));
	activefields.put(N_ROWS, new Integer(V_ROWS));
	activefields.put(N_SETPIXEL, new Integer(V_SETPIXEL));
	activefields.put(N_SIZE, new Integer(V_SIZE));
	activefields.put(N_SOURCE, new Integer(V_SOURCE));
	activefields.put(N_TRANSFORM, new Integer(V_TRANSFORM));
	activefields.put(N_TYPE, new Integer(V_TYPE));
    }

    //
    // Queued command identifiers.
    //

    private static final int  COMMAND_BUILDCURRENTIMAGE = 1;
    private static final int  COMMAND_BUILDIMAGE = 2;
    private static final int  COMMAND_CONVERT = 3;
    private static final int  COMMAND_CONVOLVE = 4;
    private static final int  COMMAND_NOTIFY = 5;
    private static final int  COMMAND_REPLACE = 6;
    private static final int  COMMAND_RESCALE = 7;
    private static final int  COMMAND_SETCURRENTIMAGE = 8;
    private static final int  COMMAND_TRANSFORM = 9;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyImage(YoixObject data) {

	this(data, null);
    }


    YoixBodyImage(YoixObject data, Image image) {

	super(data);
	currentsource = image;
	buildImage();
	setFixedSize();
	setPermissions(permissions);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(IMAGE);
    }

    ///////////////////////////////////
    //
    // Runnable Methods
    //
    ///////////////////////////////////

    public final void
    run() {

	Object  args[] = null;

	try {
	    while (threadenabled) {
		args = null;
		try {
		    synchronized(queue) {
			if (queue.size() > 0) {
			    args = (Object[])queue.elementAt(0);
			    queue.removeElementAt(0);
			} else {
			    if (data.getBoolean(N_PERSISTENT)) {
				if (threadenabled)
				    queue.wait();
			    } else break;
			}
		    }
		    if (args != null)
			handleCommand(args);
		}
		catch(InterruptedException e) {}
	    }
	}
	finally {
	    stopThread();
	}
    }

    ///////////////////////////////////
    //
    // YoixBodyImage Methods
    //
    ///////////////////////////////////

    final BufferedImage
    copyCurrentImage() {

	return(copyCurrentImage(null));
    }


    final BufferedImage
    copyCurrentImage(Object lock) {

	BufferedImage  current;
	BufferedImage  image;
	Graphics       g;
	int            width;
	int            height;

	if ((image = buildCurrentImage(lock)) != null) {
	    current = image;
	    width = current.getWidth();
	    height = current.getHeight();
	    image = new BufferedImage(width, height, current.getType());
	    if ((g = image.getGraphics()) != null)  {
		g.drawImage(current, 0, 0, null);
		g.dispose();
	    }
	}
	return(image);
    }


    final void
    eraseCurrentImage(double alpha) {

	BufferedImage  image;
	Graphics       g;
	int            width;
	int            height;

	if ((image = getCurrentImage()) != null) {
	    width = image.getWidth();
	    height = image.getHeight();
	    if ((g = image.getGraphics()) != null)  {
		paintBackground(0, 0, width, height, (float)alpha, g);
		g.dispose();
	    }
	}
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_CONVERT:
		obj = builtinConvert(name, argv);
		break;

	    case V_CONVOLVE:
		obj = builtinConvolve(name, argv);
		break;

	    case V_GETPIXEL:
		obj = builtinGetPixel(name, argv);
		break;

	    case V_REPAINT:
		obj = builtinRepaint(name, argv);
		break;

	    case V_REPLACE:
		obj = builtinReplace(name, argv);
		break;

	    case V_RESCALE:
		obj = builtinRescale(name, argv);
		break;

	    case V_SETPIXEL:
		obj = builtinSetPixel(name, argv);
		break;

	    case V_TRANSFORM:
		obj = builtinTransform(name, argv);
		break;

	    default:
		obj = null;
		break;
	}

	return(obj);
    }


    protected final void
    finalize() {

	currentpath = null;
	currentimage = null;
	currentsource = null;
	currentmetrics = null;
	paint = null;
	super.finalize();
    }


    final BufferedImage
    getConvertedImage(BufferedImage src, int type) {

	BufferedImage  image = null;
	Graphics       g;
	int            width;
	int            height;

	if (src != null) {
	    if (type != src.getType()) {
		width = src.getWidth();
		height = src.getHeight();
		if ((image = new BufferedImage(width, height, type)) != null) {
		    if ((g = image.getGraphics()) != null) {
			g.drawImage(src, 0, 0, null);
			g.dispose();
		    }
		}
	    }
	}

	return(image);
    }


    final BufferedImage
    getCurrentImage() {

	return(buildCurrentImage(null));
    }


    final BufferedImage
    getCurrentImage(Object lock) {

	return(buildCurrentImage(lock));
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

        switch (activeField(name, activefields)) {
	    case V_BACKGROUND:
		obj = getBackground(obj);
		break;

	    case V_COLUMNS:
		obj = getColumns(obj);
		break;

	    case V_GRAPHICS:
		obj = getGraphics(obj);
		break;

	    case V_HOTSPOT:
		obj = getHotspot(obj);
		break;

	    case V_PREFERREDSIZE:
		obj = getPreferredSize(obj);
		break;

	    case V_OPAQUE:
		obj = getOpaque(obj);
		break;

	    case V_ROWS:
		obj = getRows(obj);
		break;

	    case V_SIZE:
		obj = getSize(obj);
		break;
	}
	return(obj);
    }


    final double[]
    getMetrics(BufferedImage image) {

	return(getMetrics(image, null));
    }


    final double[]
    getMetrics(BufferedImage image, YoixBodyMatrix matrix) {

	double  metrics[];
	double  delta[];
	int     width;
	int     height;

	if (image != null) {
	    width = image.getWidth();
	    height = image.getHeight();
	    metrics = new double[] {
		currentmetrics[0]*width,
		currentmetrics[1]*height,
		currentmetrics[2]*width,
		currentmetrics[3]*height,
	    };
	    if (matrix != null) {
		delta = matrix.idtransform(metrics[0], metrics[1]);
		metrics[0] = delta[0];
		metrics[1] = delta[1];
		delta = matrix.idtransform(metrics[2], metrics[3]);
		metrics[2] = delta[0];
		metrics[3] = delta[1];
	    }
	} else metrics = null;

	return(metrics);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_BACKGROUND:
		    setBackground(obj);
		    break;

		case V_GRAPHICS:
		    setGraphics(obj);
		    break;

		case V_METRICS:
		    setMetrics(obj);
		    break;

		case V_MODEL:
		    setModel(obj);
		    break;

		case V_PAINT:
		    setPaint(obj);
		    break;

		case V_PERSISTENT:
		    setPersistent(obj);
		    break;

		case V_SIZE:
		    setSize(obj);
		    break;

		case V_SOURCE:
		    setSource(obj);
		    break;

		case V_TYPE:
		    setType(obj);
		    break;
	    }
	}
	return(obj);
    }


    final void
    tossCurrentImage() {

	if (initialized) {
	    synchronized(queue) {
		queue.addElement(
		    new Object[] {
			new Integer(COMMAND_SETCURRENTIMAGE),
			null
		    }
		);
		startThread();
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private BufferedImage
    buildCurrentImage(Object lock) {

	BufferedImage  result[] = {null};

	if (thread != Thread.currentThread()) {
	    if (lock == null)
		lock = new Object();
	    synchronized(lock) {
		synchronized(queue) {
		    queue.addElement(
			new Object[] {
			    new Integer(COMMAND_BUILDCURRENTIMAGE),
			    result
			}
		    );
		    queue.addElement(
			new Object[] {
			    new Integer(COMMAND_NOTIFY),
			    lock
			}
		    );
		    startThread();
		}
		try {
		    lock.wait();
		}
		catch(InterruptedException e) {}
	    }
	} else result[0] = currentimage;

	return(result[0]);
    }


    private void
    buildImage() {

	currentpath = null;
	currentimage = null;
	initialized = false;
	setField(N_GRAPHICS);
	setField(N_BACKGROUND);
	setField(N_SIZE);
	setField(N_TYPE);
	setField(N_PAINT);
	setField(N_MODEL);
	setField(N_METRICS);
	initialized = true;
	if (currentsource == null || data.getObject(N_SOURCE).notNull())
	    setField(N_SOURCE);
    }


    private YoixObject
    builtinConvert(String name, YoixObject arg[]) {

	if (arg.length == 2 || (arg.length == 3 && arg[0].isColor())) {
	    if (arg[0].isColor() || arg[0].isImage()) {
		if (arg[0].isColor()) {
		    if (arg[1].isColor() || arg[1].isNull() || arg[1].isImage()) {
			if (arg.length == 2 || (arg[2].isArray() && arg[2].sizeof() >= 4))
			    callBuiltin(COMMAND_CONVERT, arg);
			else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else {
		    if (arg[1].isArray() && arg[1].sizeof() >= 4)
			callBuiltin(COMMAND_CONVERT, arg);
		    else VM.badArgument(name, 1);
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private YoixObject
    builtinConvolve(String name, YoixObject arg[]) {

	int  n;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isArray() || arg[0].isNull()) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    if (arg[0].sizeof() > 0)
			callBuiltin(COMMAND_CONVOLVE, arg);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private YoixObject
    builtinGetPixel(String name, YoixObject arg[]) {

	BufferedImage  image;
	Color          color = null;
	int            width;
	int            height;
	int            column;
	int            row;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg.length == 2 || arg[2].isColor() || arg[2].isNull()) {
			color = YoixMake.javaColor(arg.length == 3 ? arg[2] : null);
			if ((image = getCurrentImage()) != null) {
			    width = image.getWidth();
			    height = image.getHeight();
			    column = arg[0].intValue();
			    row = arg[1].intValue();
			    if (column >= 0 && column < width) {
				if (row >= 0 && row < height) {
				    try {
					color = new Color(image.getRGB(column, row));
				    }
				    catch(ArrayIndexOutOfBoundsException e) {}
				}
			    }
			}
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(color != null ? YoixMake.yoixColor(color) : YoixObject.newNull());
    }


    private YoixObject
    builtinRepaint(String name, YoixObject arg[]) {

	int  n;

	//
	// Older versions tried to queue a repaint command, but that
	// occasionally lead to inconsistent behavoir when the image
	// was also changed by its own paint routine. Decided that
	// the best approach, at least for now, is to just toss the
	// image which means the arguments are also unused. We may
	// revisit this, but the current behavior seems good.
	//

	if (arg.length == 0 || arg.length == 1 || arg.length == 4) {
	    if (arg.length == 4) {
		for (n = 0; n < arg.length; n++) {
		    if (arg[n].isNumber() == false)
			VM.badArgument(name, n);
		}
	    } else if (arg.length == 1) {
		if (!(arg[0].isRectangle() || arg[0].isNull()))
		    VM.badArgument(name, 0);
	    }
	    tossCurrentImage();
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private YoixObject
    builtinReplace(String name, YoixObject arg[]) {

	int  n;

	if (arg.length > 0) {
	    for (n = 0; n < arg.length; n++) {
		if (!(arg[n].isArray() || arg[n].isNull()))
		    VM.badArgument(name, n);
	    }
	    callBuiltin(COMMAND_REPLACE, arg);
	} else VM.badCall(name);

	return(getContext());
    }


    private YoixObject
    builtinRescale(String name, YoixObject arg[]) {

	int  n;

	if (arg.length > 0) {
	    for (n = 0; n < arg.length; n++) {
		if (arg[n].isNumber() == false)
		    VM.badArgument(name, n);
	    }
	    callBuiltin(COMMAND_RESCALE, arg);
	} else VM.badCall(name);

	return(getContext());
    }


    private YoixObject
    builtinSetPixel(String name, YoixObject arg[]) {

	BufferedImage  image;
	YoixObject     obj = null;
	Color          color;
	int            value;
	int            oldvalue;
	int            width;
	int            height;
	int            column;
	int            row;

	//
	// Eventually may let arg[2] be a color or number. If it's a
	// number then assumes it's the alpha value, which means read
	// current pixel's color, add alpha, and set the pixel to the
	// new value. Suspect we may even want to do something similar
	// when arg[2] is a color (i.e., preserve alpha if present).
	// Because of these plans we decided to return the old color
	// (or NULL) since we'll eventually need to read the pixel to
	// implement the new stuff.
	//

	if (arg.length == 3) {
	    if (arg[0].isNumber()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isColor()) {
			if ((color = YoixMake.javaColor(arg[2])) != null)
			    value = color.getRGB();
			else value = 0;
			if ((image = getCurrentImage()) != null) {
			    width = image.getWidth();
			    height = image.getHeight();
			    column = arg[0].intValue();
			    row = arg[1].intValue();
			    if (column >= 0 && column < width) {
				if (row >= 0 && row < height) {
				    try {
					oldvalue = image.getRGB(column, row);
					image.setRGB(column, row, value);
					obj = YoixMake.yoixColor(new Color(oldvalue));
				    }
				    catch(ArrayIndexOutOfBoundsException e) {}
				}
			    }
			}
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(obj != null ? obj : YoixObject.newNull());
    }


    private YoixObject
    builtinTransform(String name, YoixObject arg[]) {

	if (arg.length == 1) {
	    if (arg[0].isMatrix())
		callBuiltin(COMMAND_TRANSFORM, arg);
	    else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(getContext());
    }


    private void
    callBuiltin(int command, YoixObject arg[]) {

	if (thread != Thread.currentThread()) {
	    synchronized(queue) {
		queue.addElement(new Object[] {new Integer(command), arg});
		startThread();
	    }
	} else handleCommand(new Object[] {new Integer(command), arg});
    }


    private YoixObject
    getBackground(YoixObject obj) {

	return(YoixMake.yoixColor(currentbackground));
    }


    private YoixObject
    getColumns(YoixObject obj) {

	BufferedImage  image;
	int            columns;

	if ((image = getCurrentImage()) != null)
	    columns = image.getWidth();
	else columns = 0;

	return(YoixObject.newInt(columns));
    }


    private BufferedImage
    getCompatibleImage(BufferedImage src) {

	return(getCompatibleImage(src, 0, 0, null));
    }


    private BufferedImage
    getCompatibleImage(BufferedImage src, int width, int height, Color background) {

	BufferedImage  image = null;
	Graphics       g;

	if (src != null) {
	    if (width <= 0)
		width = src.getWidth();
	    if (height <= 0)
		height = src.getHeight();
	    if ((image = new BufferedImage(width, height, src.getType())) != null) {
		if (background != null && isOpaque(image)) {
		    if ((g = image.getGraphics()) != null) {
			g.setColor(background);
			g.fillRect(0, 0, width, height);
			g.dispose();
		    }
		}
	    }
	}

	return(image);
    }


    private YoixObject
    getGraphics(YoixObject obj) {

	if ((obj = data.getObject(N_GRAPHICS)) != null) {
	    if (obj.isNull()) {
		obj = YoixMake.yoixType(T_GRAPHICS);
		setGraphics(obj);
	    }
	}
	return(obj);
    }


    private YoixObject
    getHotspot(YoixObject obj) {

	if (getCurrentImage() != null) {
	    if ((obj = data.getObject(N_HOTSPOT)) != null) {
		if (obj.isNull())
		    obj = YoixObject.newPoint(new Point(0, 0));
	    }
	} else obj = YoixObject.newPoint();
	return(obj);
    }


    private YoixObject
    getOpaque(YoixObject obj) {

	BufferedImage  image;
	boolean        result;

	if ((image = getCurrentImage()) != null)
	    result = isOpaque(image);
	else result = (currenttransparency == Transparency.OPAQUE);

	return(YoixObject.newInt(result));
    }


    private YoixObject
    getPreferredSize(YoixObject obj) {

	Image  image;
	int    height;
	int    width;

	image = currentsource;		// snapshot - just to be safe

	if (image != null) {
	    height = image.getHeight(null);
	    width = image.getWidth(null);
	    obj = YoixMakeScreen.yoixDimension(width, height);
	} else obj = getSize(obj);

	return(obj);
    }


    private YoixObject
    getRows(YoixObject obj) {

	BufferedImage  image;
	int            rows;

	if ((image = getCurrentImage()) != null)
	    rows = image.getHeight();
	else rows = 0;

	return(YoixObject.newInt(rows));
    }


    private BufferedImage
    getScaledImage(BufferedImage src, int width, int height) {

	AffineTransformOp  operator;
	AffineTransform    transform;
	BufferedImage      image;
	double             sx;
	double             sy;

	if (src != null) {
	    sx = width/((double)src.getWidth());
	    sy = height/((double)src.getHeight());
	    if (sx != 1.0 || sy != 1.0) {
		image = new BufferedImage(width, height, src.getType());
		transform = new AffineTransform();
		transform.scale(sx, sy);
		operator = new AffineTransformOp(transform, null);
		operator.filter(src, (BufferedImage)image);
	    } else image = src;
	} else image = null;

	return(image);
    }


    private YoixObject
    getSize(YoixObject obj) {

	BufferedImage  image;
	int            height;
	int            width;

	if ((obj = data.getObject(N_SIZE)) == null || obj.isNull()) {
	    if ((image = getCurrentImage()) != null) {
		height = image.getHeight();
		width = image.getWidth();
		obj = YoixMakeScreen.yoixDimension(width, height);
	    }
	}

	return(obj != null ? obj : YoixObject.newNull());
    }


    private void
    handleCommand(Object args[]) {

	if (args != null && args.length > 0) {
	    switch (((Integer)args[0]).intValue()) {
		case COMMAND_BUILDCURRENTIMAGE:
		    handleBuildCurrentImage((BufferedImage[])args[1]);
		    break;

		case COMMAND_CONVERT:
		    handleConvert((YoixObject[])args[1]);
		    break;

		case COMMAND_CONVOLVE:
		    handleConvolveOp((YoixObject[])args[1]);
		    break;

		case COMMAND_NOTIFY:
		    handleNotifyAll(args[1]);
		    break;

		case COMMAND_REPLACE:
		    handleReplaceOp((YoixObject[])args[1]);
		    break;

		case COMMAND_RESCALE:
		    handleRescaleOp((YoixObject[])args[1]);
		    break;

		case COMMAND_SETCURRENTIMAGE:
		    handleSetCurrentImage((BufferedImage)args[1]);
		    break;

		case COMMAND_TRANSFORM:
		    handleTransformOp((YoixObject[])args[1]);
		    break;
	    }
	}
    }


    private BufferedImage
    handleBuildCurrentImage(BufferedImage result[]) {

	BufferedImage  image;
	YoixObject     funct;
	Graphics       g;
	Color          color;
	int            width;
	int            height;

	if ((image = currentimage) == null) {
	    if (currentsource != null) {
		width = currentsource.getWidth(null);
		height = currentsource.getHeight(null);
	    } else if (currentpath == null) {
		width = currentwidth;
		height = currentheight;
	    } else {
		width = 0;
		height = 0;
	    }
	    if (width > 0 && height > 0) {
		image = new BufferedImage(width, height, currenttype);
		if ((g = image.getGraphics()) != null) {
		    if (isOpaque(image))
			paintBackground(0, 0, width, height, 1.0f, g);
		    if (currentsource != null)
			g.drawImage(currentsource, 0, 0, null);
		    g.dispose();
		}
		if (currentwidth > 0 && currentheight > 0) {
		    if (width != currentwidth || height != currentheight)
			image = getScaledImage(image, currentwidth, currentheight);
		}
	    }
	    if ((currentimage = image) != null) {
		if ((funct = paint) != null && funct.notNull()) {
		    if (funct.callable(1))
			call(funct, new YoixObject[] {YoixObject.newNull()});
		    else call(funct, new YoixObject[0]);
		}
	    }
	}

	if (result != null)
	    result[0] = currentimage;
	return(currentimage);
    }


    private void
    handleConvert(YoixObject arg[]) {

	BufferedImage  src;
	BufferedImage  sample;
	YoixObject     array;
	boolean        automatch;
	Color          color;
	int            grid[];
	int            target;
	int            value;
	int            height;
	int            width;
	int            columns;
	int            column;
	int            rows;
	int            row;
	int            x;
	int            y;

	if ((src = handleBuildCurrentImage(null)) != null) {
	    if (arg[arg.length - 1].isArray()) {
		automatch = true;
		grid = new int[4];
		array = arg[arg.length - 1];
		grid[1] = Math.max(array.getInt(0, 1), 1);
		grid[3] = Math.max(array.getInt(1, 1), 1);
		grid[0] = Math.max(array.getInt(2, 0), 0)%grid[1];
		grid[2] = Math.max(array.getInt(3, 0), 0)%grid[3];
	    } else {
		automatch = false;
		grid = new int[] {0, 1, 0, 1};
	    }

	    if (arg[0].isColor()) {
		target = YoixMake.javaColor(arg[0]).getRGB();
		if (arg[1].isImage()) {
		    sample = ((YoixBodyImage)arg[1].body()).copyCurrentImage();
		    value = 0;
		} else {
		    if (arg[1].isNull()) {
			color = YoixMake.javaColor(arg[0]);
			color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
		    } else color = YoixMake.javaColor(arg[1]);
		    sample = null;
		    value = color.getRGB();
		}
	    } else {
		sample = ((YoixBodyImage)arg[0].body()).copyCurrentImage();
		target = 0;
		value = 0;
	    }

	    rows = src.getHeight();
	    columns = src.getWidth();
	    width = (sample != null) ? sample.getWidth() : 0;
	    height = (sample != null) ? sample.getHeight() : 0;
	    for (row = grid[2]; row < rows; row += grid[3]) {
		for (column = grid[0]; column < columns; column += grid[1]) {
		    if (automatch || src.getRGB(column, row) == target) {
			if (sample != null) {
			    x = (column*width)/columns;
			    y = (row*height)/rows;
			    value = sample.getRGB(x, y);
			}
			src.setRGB(column, row, value);
		    }
		}
	    }
	}
    }


    private void
    handleConvolveOp(YoixObject arg[]) {

	BufferedImage  src;
	BufferedImage  dest;
	ConvolveOp     operator;
	YoixObject     element;
	boolean        valid;
	float          values[];
	int            edge;
	int            width;
	int            height;
	int            m;
	int            n;

	//
	// Should be realtively easy to add width and height arguments,
	// although there are alternatives. For example, trim rows and
	// columns of all zeros from all edges of the array before the
	// ConvolveOp operator is built.
	//

	if ((src = handleBuildCurrentImage(null)) != null) {
	    width = 0;
	    height = 0;
	    values = new float[arg[0].sizeof()];
	    for (n = arg[0].offset(), m = 0; m < values.length; n++, m++) {
		if ((element = arg[0].getObject(n)) != null && element.isNumber())
		    values[m] = (float)element.doubleValue();
		else values[m] = 0;
	    }
	    if (arg.length > 1) {
		switch (edge = arg[1].intValue()) {
		    case YOIX_EDGE_NO_OP:
		    case YOIX_EDGE_ZERO_FILL:
			break;

		    default:
			edge = YOIX_EDGE_ZERO_FILL;
			break;
		}
	    } else edge = YOIX_EDGE_ZERO_FILL;
	    if (values.length > 0) {		// test should be unnecessary
		for (n = 0; n < values.length && values[n] == 0; n++) ;
		if (n == values.length)
		    values[n/2] = Float.MIN_VALUE;
		dest = getCompatibleImage(src);
		if (width <= 0)
		    width = (int)Math.sqrt(values.length);
		else if (width > values.length)
		    width = values.length;
		if (height <= 0 || height*width > values.length)
		    height = values.length/width;
		operator = new ConvolveOp(
		    new Kernel(width, height, values),
		    edge,
		    null
		);
		operator.filter(src, dest);
		setCurrentImage(dest);
	    }
	}
    }


    private void
    handleNotifyAll(Object lock) {

	if (lock != null) {
	    synchronized(lock) {
		lock.notifyAll();
	    }
	}
    }


    private void
    handleReplaceOp(YoixObject arg[]) {

	BufferedImage  src;
	BufferedImage  dest;
	BufferedImage  copy;
	ColorModel     model;
	YoixObject     array;
	YoixObject     value;
	Hashtable      tablemap;
	LookupOp       operator;
	boolean        useidentity;
	short          tables[][];
	short          table[];
	short          identity[];
	int            type;
	int            offset;
	int            limit;
	int            length;
	int            count;
	int            bits;
	int            i;
	int            j;
	int            m;
	int            n;

	//
	// We use an offset of zero and full size short arrays when
	// we create the LookupOp operator in an attempt to prevent
	// unnecessary Java exceptions.
	//

	if ((src = handleBuildCurrentImage(null)) != null) {
	    type = src.getType();
	    n = 0;
	    offset = (arg[0].isNumber()) ? arg[n++].intValue() : 0;
	    model = src.getColorModel();
	    count = model.getNumComponents();
	    bits = 0;
	    for (m = 0; m < count; m++)
		bits = Math.max(bits, model.getComponentSize(m));
	    length = (bits > 0) ? (1 << bits) : 256;
	    offset = Math.min(Math.max(offset, 0), length - 1);
	    tables = new short[count][];
	    tablemap = new Hashtable();
	    identity = null;
	    useidentity = ((arg.length - n) > 1);
	    for (m = 0; m < count; n++, m++) {
		if (n < arg.length && arg[n].notNull()) {
		    array = arg[n];
		    if ((table = (short[])tablemap.get(array.body())) == null) {
			table = new short[length];
			limit = Math.min(array.length(), table.length - offset);
			for (i = array.offset(), j = offset; i < limit; i++, j++) {
			    if ((value = array.getObject(i)) != null) {
				if (value.isNumber())
				    table[j] = (short)value.intValue();
			    }
			}
			//
			// For now we skip the tablemap optimization when
			// arrays have offsets - a simple check that could
			// easily be improved.
			//
			if (array.offset() == 0)
			    tablemap.put(array.body(), table);
		    }
		    tables[m] = (short[])table;
		} else {
		    if (n < arg.length || useidentity) {
			if (identity == null) {
			    identity = new short[length];
			    for (i = 0; i < identity.length; i++)
				identity[i] = (short)i;
			}
			tables[m] = identity;
		    } else tables[m] = tables[m-1];
		}
	    }
	    tablemap.clear();
	    operator = new LookupOp(new ShortLookupTable(0, tables), null);
	    if (isIndexed(src)) {
		if ((copy = getConvertedImage(src, YOIX_TYPE_RGB)) != src) {
		    dest = getCompatibleImage(copy);
		    operator.filter(copy, dest);
		    dest = getConvertedImage(dest, type);
		    setCurrentImage(dest);
		}
	    } else {
		dest = getCompatibleImage(src);
		operator.filter(src, dest);
		setCurrentImage(dest);
	    }
	}
    }


    private void
    handleRescaleOp(YoixObject arg[]) {

	BufferedImage  src;
	BufferedImage  dest;
	BufferedImage  copy;
	RescaleOp      operator;
	float          scale[];
	float          offset[];
	int            type;
	int            count;
	int            m;
	int            n;

	if ((src = handleBuildCurrentImage(null)) != null) {
	    count = src.getColorModel().getNumComponents();
	    type = src.getType();
	    scale = new float[count];
	    offset = new float[count];
	    for (n = 0, m = 0; m < count; m++) {
		if (n < arg.length) {
		    scale[m] = (float)arg[n++].doubleValue();
		    if (n < arg.length)
			offset[m] = (float)arg[n++].doubleValue();
		    else offset[m] = 0.0f;
		} else {
		    if (arg.length <= 2) {
			scale[m] = scale[m-1];
			offset[m] = offset[m-1];
		    } else {
			scale[m] = 1.0f;
			offset[m] = 0.0f;
		    }
		}
	    }
	    operator = new RescaleOp(scale, offset, null);
	    if (isIndexed(src)) {
		if ((copy = getConvertedImage(src, YOIX_TYPE_RGB)) != src) {
		    dest = getCompatibleImage(copy);
		    operator.filter(copy, dest);
		    dest = getConvertedImage(dest, type);
		    setCurrentImage(dest);
		}
	    } else {
		dest = getCompatibleImage(src);
		operator.filter(src, dest);
		setCurrentImage(dest);
	    }
	}
    }


    private void
    handleSetCurrentImage(BufferedImage image) {

	currentimage = image;
    }


    private void
    handleTransformOp(YoixObject arg[]) {

	AffineTransformOp  operator;
	AffineTransform    transform;
	YoixBodyMatrix     matrix;
	BufferedImage      src;
	BufferedImage      dest;
	double             width;
	double             height;
	double             coords[];
	double             pt[];
	double             x0;
	double             y0;
	double             x1;
	double             y1;
	int                n;

	if ((src = handleBuildCurrentImage(null)) != null) {
	    matrix = (YoixBodyMatrix)arg[0].body();
	    width = src.getWidth();
	    height = src.getHeight();
	    coords = matrix.transform(
	        new double[] {
		    0, 0,
		    width, 0,
		    width, height,
		    0, height
		}
	    );
	    x0 = x1 = coords[0];
	    y0 = y1 = coords[1];
	    for (n = 2; n < coords.length; n += 2) {
		if (coords[n] < x0)
		    x0 = coords[n];
		else if (coords[n] > x1)
		    x1 = coords[n];
	    }
	    for (n = 3; n < coords.length; n += 2) {
		if (coords[n] < y0)
		    y0 = coords[n];
		else if (coords[n] > y1)
		    y1 = coords[n];
	    }
	    pt = matrix.itransform(x0, y0);
	    transform = matrix.getCurrentAffineTransform();
	    transform.translate(-pt[0], -pt[1]);
	    dest = getCompatibleImage(
		src,
		(int)(x1 - x0 + 0.5),
		(int)(y1 - y0 + 0.5),
		currentbackground
	    );
	    operator = new AffineTransformOp(transform, null);
	    operator.filter(src, dest);
	    if (currentwidth > 0 && currentheight > 0) {
		if (dest.getWidth() != currentwidth || dest.getHeight() != currentheight)
		    dest = getScaledImage(dest, currentwidth, currentheight);
	    }
	    setCurrentImage(dest);
	}
    }


    private boolean
    isIndexed(BufferedImage src) {

	boolean  result = false;

	if (src != null) {
	    switch(src.getType()) {
		case YOIX_TYPE_BYTE_INDEXED:
		case YOIX_TYPE_BYTE_BINARY:
		    result = true;
		    break;
	    }
	}

	return(result);
    }


    private boolean
    isOpaque(BufferedImage image) {

	return(image.getColorModel().getTransparency() == Transparency.OPAQUE);
    }


    private void
    paintBackground(int x, int y, int width, int height, float alpha, Graphics g) {

	Color  color;
	Color  original;
	float  rgba[];

	if (width > 0 && height > 0) {
	    if (g != null) {
		if ((color = currentbackground) != null) {
		    if (alpha < 1.0) {
			rgba = color.getRGBComponents(new float[4]);
			rgba[3] = Math.max(0.0f, alpha);
			color = new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
		    }
		    original = g.getColor();
		    g.setColor(color);
		    g.fillRect(x, y, width, height);
		    g.setColor(original);
		}
	    }
	}
    }


    private void
    setBackground(YoixObject obj) {

	Color  color;

	color = YoixMakeScreen.javaBackground(obj);
	if (color.equals(currentbackground) == false) {
	    currentbackground = color;
	    tossCurrentImage();
	}
    }


    private void
    setCurrentImage(BufferedImage image) {

	if (thread != Thread.currentThread()) {
	    synchronized(queue) {
		queue.addElement(
		    new Object[] {
			new Integer(COMMAND_SETCURRENTIMAGE),
			image
		    }
		);
		startThread();
	    }
	} else currentimage = image;
    }


    private void
    setGraphics(YoixObject obj) {

	YoixBodyGraphics  body;

	if (obj.notNull()) {
	    body = (YoixBodyGraphics)obj.body();
	    body.setOwner(getContext());
	    data.put(N_GRAPHICS, obj, false);
	    data.get(N_GRAPHICS).setAccess(LR__);
	}
    }


    private void
    setMetrics(YoixObject obj) {

	double  metrics[] = null;
	int     m;
	int     n;

	if (obj.isNumber() || obj.isArray() || obj.isNull()) {
	    metrics = new double[] {0.0, 0.0, 0.0, 0.0};
	    if (obj.notNull()) {
		if (obj.isArray()) {
		    n = obj.offset();
		    if (obj.sizeof() >= 4) {
			for (m = 0; m < 4; n++, m++)
			    metrics[m] = obj.getDouble(m, 0.0);
		    } else if (obj.sizeof() == 2) {
			metrics[0] = obj.getDouble(n++, 0.0);
			metrics[2] = obj.getDouble(n++, 0.0);
		    } else if (obj.sizeof() == 1)
			metrics[2] = obj.getDouble(n++, 0.0);
		} else metrics[2] = obj.doubleValue();
	    }
	    currentmetrics = metrics;
	} else VM.abort(TYPECHECK, N_METRICS);
    }


    private void
    setModel(YoixObject obj) {

	int  value;

	value = Math.min(Math.max(obj.intValue(), -1), 1);
	if (currentmodel != value) {
	    currentmodel = value;
	    tossCurrentImage();
	}
    }


    private void
    setPaint(YoixObject obj) {

	if (obj.isNull() || obj.callable(1) || obj.callable(0)) {
	    paint = obj.notNull() ? obj : null;
	    ////tossCurrentImage();
	} else VM.abort(TYPECHECK, N_PAINT);
    }


    private void
    setPersistent(YoixObject obj) {

	if (obj.booleanValue() == false) {
	    synchronized(queue) {
		if (thread != null && thread.isAlive())
		    queue.notifyAll();
	    }
	}
    }


    private void
    setSize(YoixObject obj) {

	Dimension  dimen;

	dimen = YoixMakeScreen.javaDimension(obj);
	if (dimen.width != currentwidth || dimen.height != currentheight) {
	    currentwidth = dimen.width;
	    currentheight = dimen.height;
	    tossCurrentImage();
	}
    }


    private synchronized void
    setSource(YoixObject obj) {

	YoixBodyImage  body;
	Image          image;

	if (obj.isString() || obj.isImage() || obj.isNull()) {
	    if (obj.isString() || obj.isNull()) {
		VM.pushAccess(LRW_);
		data.putInt(N_FLAGS, 0);
		VM.popAccess();
		currentsource = null;
		currentpath = null;
		tossCurrentImage();
		if (obj.notNull()) {
		    currentpath = obj.stringValue();
		    if (currentmodel >= 0 || currentpath.length() > 0) {
			image = IMAGEOBSERVER.getImage(currentpath);
			currentsource = IMAGEOBSERVER.waitForImage(image, data);
			if (currentsource == null) {
			    VM.pushAccess(LRW_);
			    data.putInt(N_FLAGS, YOIX_IMAGE_ERROR|YOIX_IMAGE_ABORT);
			    VM.popAccess();
			}
		    } else currentpath = null;
		}
	    } else {
		//
		// Should we set data.source to NULL after making the
		// copy?? Think about it.
		//
		body = (YoixBodyImage)obj.body();
		if (body != this) {
		    currentsource = body.copyCurrentImage();
		    currentpath = null;
		    tossCurrentImage();
		    if (currentsource == null) {
			VM.pushAccess(LRW_);
			data.putInt(N_FLAGS, YOIX_IMAGE_ERROR|YOIX_IMAGE_ABORT);
			VM.popAccess();
		    }
		}
	    }
	} else VM.abort(TYPECHECK, N_SOURCE);
    }


    private void
    setType(YoixObject obj) {

	int  transparency;
	int  value;

	switch (value = obj.intValue()) {
	    case YOIX_TYPE_CUSTOM:
	    case YOIX_TYPE_INT_RGB:
	    case YOIX_TYPE_INT_BGR:
	    case YOIX_TYPE_3BYTE_BGR:
	    case YOIX_TYPE_USHORT_565_RGB:
	    case YOIX_TYPE_USHORT_555_RGB:
	    case YOIX_TYPE_BYTE_GRAY:
	    case YOIX_TYPE_BYTE_INDEXED:
	    case YOIX_TYPE_BYTE_BINARY:
		transparency = Transparency.OPAQUE;
		break;

	    case YOIX_TYPE_INT_ARGB:
	    case YOIX_TYPE_INT_ARGB_PRE:
	    case YOIX_TYPE_4BYTE_ABGR:
	    case YOIX_TYPE_4BYTE_ABGR_PRE:
		transparency = Transparency.TRANSLUCENT;
		break;

	    case YOIX_TYPE_USHORT_GRAY:		// Java 1.3.1 SGI had trouble
		value = YOIX_TYPE_BYTE_GRAY;
		transparency = Transparency.OPAQUE;
		break;

	    default:
		value = YOIX_TYPE_INT_RGB;
		transparency = Transparency.OPAQUE;
		break;
	}

	if (value != currenttype) {
	    currenttype = value;
	    currenttransparency = transparency;
	    tossCurrentImage();
	}
    }


    private void
    startThread() {

	synchronized(queue) {
	    if (thread == null) {
		try {
		    threadenabled = true;
		    thread = new YoixThread(this);
		    thread.setPriority(Math.min(priority, Thread.MAX_PRIORITY));
		    thread.start();
		}
		catch(IllegalThreadStateException e) {
		    stopThread();
		}
	    } else queue.notifyAll();
	}
    }


    private void
    stopThread() {

	synchronized(queue) {
	    threadenabled = false;
	    thread = null;
	    if (queue.size() > 0)
		startThread();
	    queue.notifyAll();
	}
    }
}

