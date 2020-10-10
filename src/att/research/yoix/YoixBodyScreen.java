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
import java.util.*;

final
class YoixBodyScreen extends YoixPointerActive

{

    //
    // Screens we create are assigned a non-negative number that's supposed
    // to identify the GraphicsDevice associated with the screen. The number
    // 0 is reserved for the default GraphicsDevice. In a multiscreen setup
    // the indices usually should correspond to the order of the screens in
    // the array that's returned by YoixMisc.getScreenDevices(), which should
    // also be the order of the screens in VM.screens. The problem is there's
    // no guarantee low level Java methods can (or do) maintain the order
    // (e.g., someone changes the configuration of the screens while we're
    // running), but the order that scripts see in VM.screens is fixed when
    // the array is filled in during initialization.
    //
    // All screens (i.e., GraphicDevices) are assigned an id string that can
    // be used to uniquely identify a screen, but the id strings are vendor
    // (and maybe platform) specific, so the screenmap HashMap is used to map
    // the index that we assigned to a screen into the id string associated
    // with the GraphicsDevice. Once we have the id string we can locate the
    // GraphicsDevice whenever we need it.
    //
    // NOTE - early on we decided against sotring the GraphicsDevice that's
    // associated with a screen in this class. I'm not convinced it was the
    // best decision so we eventaully may want to revisit it. One reason it
    // was done is that much of this code was initially written and tested
    // on a system that didn't support multiple screens.
    //

    private int  screenindex = -1;

    private static HashMap  screenmap = new HashMap();
    private static HashMap  screendata = new HashMap();

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	N_ID,               $LR__,       $LR__,
	N_DIAGONAL,         $LR__,       null,
	N_HEIGHT,           $LR__,       null,
	N_ID,               $LR__,       $LR__,
	N_INDEX,            $LR__,       null,
	N_PIXELHEIGHT,      $LR__,       null,
	N_PIXELWIDTH,       $LR__,       null,
	N_RESOLUTION,       $LR__,       null,
	N_WIDTH,            $LR__,       null,
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
	activefields.put(N_BOUNDS, new Integer(V_BOUNDS));
	activefields.put(N_DEFAULTMATRIX, new Integer(V_DEFAULTMATRIX));
	activefields.put(N_DIAGONAL, new Integer(V_DIAGONAL));
	activefields.put(N_DOUBLEBUFFERED, new Integer(V_DOUBLEBUFFERED));
	activefields.put(N_FONT, new Integer(V_FONT));
	activefields.put(N_FOREGROUND, new Integer(V_FOREGROUND));
	activefields.put(N_FULLSCREENSUPPORTED, new Integer(V_FULLSCREENSUPPORTED));
	activefields.put(N_HEADLESS, new Integer(V_HEADLESS));
	activefields.put(N_HEIGHT, new Integer(V_HEIGHT));
	activefields.put(N_ID, new Integer(V_ID));
	activefields.put(N_INDEX, new Integer(V_INDEX));
	activefields.put(N_INSETS, new Integer(V_INSETS));
	activefields.put(N_PIXELHEIGHT, new Integer(V_PIXELHEIGHT));
	activefields.put(N_PIXELWIDTH, new Integer(V_PIXELWIDTH));
	activefields.put(N_RESOLUTION, new Integer(V_RESOLUTION));
	activefields.put(N_UIMANAGER, new Integer(V_UIMANAGER));
	activefields.put(N_VIRTUALBOUNDS, new Integer(V_VIRTUALBOUNDS));
	activefields.put(N_WIDTH, new Integer(V_WIDTH));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyScreen(YoixObject data) {

	super(data, true);
	buildScreen();
	setFixedSize();
	setPermissions(permissions);
    }


    YoixBodyScreen(YoixObject data, GraphicsDevice screen, YoixObject dict) {

	super(data, true);
	buildScreen(dict);
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

	return(SCREEN);
    }

    ///////////////////////////////////
    //
    // YoixBodyScreen Methods
    //
    ///////////////////////////////////

    static YoixObject
    buildScreenDescription(GraphicsDevice screen, int index) {

	return(buildScreenDescription(screen, index, YoixObject.newDictionary(0, -1)));
    }


    static YoixObject
    buildScreenDescription(GraphicsDevice screen, int index, YoixObject dict) {

	GraphicsConfiguration  gc;
	YoixObject             matrix;
	Rectangle              bounds;
	String                 id;
	double                 diagonal;
	double                 resolution;
	double                 scale;
	double                 delta;
	double                 width;
	double                 height;

	//
	// This usually is an expensive operation that requires access to
	// your display, which could cause problems (e.g., a process on a
	// Unix system), so it's something we want to postpone until it's
	// really needed!! In other words, don't do this stuff until were
	// pretty sure the user is running a Yoix script that really does
	// want to do stuff on a display.
	//

	if (index >= 0) {
	    if (screen != null) {
		gc = screen.getDefaultConfiguration();
		bounds = gc.getBounds();
		id = screen.getIDstring();
	    } else {
		bounds = new Rectangle(0, 0, 0, 0);
		id = null;
	    }

	    width = bounds.width;
	    height = bounds.height;
	    diagonal = (index == 0) ? dict.getDouble(N_DIAGONAL, 17.0) : 0;

	    //
	    // This still needs work when width or height are zero!!!!
	    //

	    if (diagonal <= 0) {
		//
		// No diagonal means screenresolution probably is the best
		// we can do, so we use it to calculate the diagonal value
		// that's stored in dict.
		//
		// NOTE - we experimented with getGraphicsConfiguration()
		// and getNormalizingTransform() and it didn't seem like we
		// got any useful information. Done quickly, so this should
		// be revisited.
		//
		if ((resolution = YoixAWTToolkit.getScreenResolution()) <= 0)
		    resolution = 72.0;
		diagonal = Math.sqrt(width*width + height*height)/resolution;
	    } else resolution = Math.sqrt(width*width + height*height)/diagonal;

	    scale = resolution/72.0;
	    width /= scale;
	    height /= scale;

	    dict.put(N_WIDTH, YoixObject.newDouble(width));
	    dict.put(N_HEIGHT, YoixObject.newDouble(height));
	    dict.put(N_DIAGONAL, YoixObject.newDouble(diagonal));

	    //
	    // This should only happen when we're building a description of
	    // the default screen, but even if other screens try to define
	    // a default matrix Yoix scripts won't see them. Bottom line is
	    // there's only one defaultmatrix that gets used and it's the
	    // one associated with the default screen.
	    //

	    if ((matrix = dict.getObject(N_DEFAULTMATRIX)) != null)
		((YoixBodyMatrix)matrix.body()).scale(scale, scale);

	    //
	    // Make delta compensate for double precision arithmetic errors
	    // so dtransform of pixelwidth or pixelheight end up greater than
	    // or equal to 1.0. Probably no reason why we need to try to get
	    // so close, and I'm not even convinced this is always correct.
	    // Actually decided to start with a little slop that we probably
	    // can take away later on, which is why I use 1.000000000000001
	    // instead of 1.0.
	    //

	    delta = 1.000000000000001 - (1/scale)*scale;	// may be too tight??
	    dict.put(N_PIXELWIDTH, YoixObject.newDouble(1/scale + delta));
	    dict.put(N_PIXELHEIGHT, YoixObject.newDouble(1/scale + delta));

	    //
	    // These are some recent additions. The id string probably is
	    // unique, but it's in an unspecified (perhaps vendor specific)
	    // format and can't be changed. Alternatively the index of the
	    // GraphicsDevice in the array of all graphics devices should
	    // be more useful, but there's no guarantee the index is fixed
	    // or that the array returned by ge.getScreenDevices() won't
	    // won't change. For example, what happens if the user changes
	    // configuration of the monitors while the program is running??
	    // Trying to maintain perfect behavior when the configuration
	    // changes might not be necessary, so we're probably going to
	    // use the index.
	    //

	    dict.putString(N_ID, id);
	    dict.putInt(N_INDEX, index);
	}

	return(dict);
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    default:
		obj = null;
		break;
	}

	return(obj);
    }


    protected final void
    finalize() {

	super.finalize();
    }


    protected final synchronized YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_BACKGROUND:
	    case V_DEFAULTMATRIX:
	    case V_DOUBLEBUFFERED:
	    case V_FONT:
	    case V_FOREGROUND:
	    case V_RESOLUTION:
	    case V_UIMANAGER:
		//
		// Decided to duplicate all, but some really don't need it.
		//
		obj = getDefaultField(name, obj, true);
		break;

	    case V_BOUNDS:
		obj = getBounds(obj);
		break;

	    case V_FULLSCREENSUPPORTED:
		obj = getFullScreenSupported(obj);
		break;

	    case V_HEADLESS:
		obj = getHeadless(obj);
		break;

	    case V_INDEX:
		obj = getIndex(obj);
		break;

	    case V_INSETS:
		obj = getInsets(obj);
		break;

	    case V_VIRTUALBOUNDS:
		obj = getVirtualBounds(obj);
		break;
	}

	return(obj);
    }


    final GraphicsConfiguration
    getGraphicsConfiguration() {

	GraphicsConfiguration  gc = null;
	GraphicsDevice         gd;

	if ((gd = getScreenDevice()) != null)
	    gc = gd.getDefaultConfiguration();
	return(gc);
    }


    protected final Object
    getManagedObject() {

	return(getScreenDevice());
    }


    final GraphicsDevice
    getScreenDevice() {

	return(getScreenDevice(screenindex));
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_BACKGROUND:
		case V_DEFAULTMATRIX:
		case V_FONT:
		case V_FOREGROUND:
		case V_RESOLUTION:
		    setDefaultField(name, obj);
		    break;

		case V_DOUBLEBUFFERED:
		    if (obj.isNull() || obj.isNumber())
			setDefaultField(name, obj);
		    else VM.abort(TYPECHECK, N_DOUBLEBUFFERED);
		    break;

		case V_INDEX:
		    setIndex(obj);
		    break;
	    }
	}

	return(obj);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildScreen() {

	YoixObject  dict;
	YoixObject  diagonal;
	Integer     key;
	int         index;

	dict = YoixObject.newDictionary(0, -1);
	index = pickScreenIndex(data.getInt(N_INDEX));		// force index into bounds
	key = new Integer(index);

	if (screendata.containsKey(key))
	    diagonal = ((YoixObject)screendata.get(key)).getObject(N_DIAGONAL);
	else diagonal = data.getObject(N_DIAGONAL);

	if (diagonal != null)
	    dict.putObject(N_DIAGONAL, diagonal);
	buildScreen(buildScreenDescription(getScreenDevice(index), index, dict));
    }


    private void
    buildScreen(YoixObject dict) {

	YoixMisc.copyInto(dict, data);
	setField(N_INDEX);
    }


    private YoixObject
    getBounds(YoixObject obj) {

	GraphicsConfiguration  gc;
	GraphicsDevice         gd;
	Rectangle              bounds = null;

	if ((gd = getScreenDevice()) != null) {
	    if ((gc = gd.getDefaultConfiguration()) != null)
		bounds = gc.getBounds();
	}

	return(bounds != null ? YoixMakeScreen.yoixRectangle(bounds) : YoixObject.newRectangle());
    }


    private YoixBodyScreen
    getDefaultBlockScreen() {

	YoixBodyScreen  screen = null;
	YoixObject      obj;
	Object          body;

	if ((obj = YoixBodyBlock.getVM()) != null) {
	    if ((obj = obj.getObject(N_SCREEN)) != null) {
		body = obj.body();
		if (body instanceof YoixBodyScreen)
		    screen = (YoixBodyScreen)body;
	    }
	}
	return(screen);
    }


    private YoixObject
    getDefaultField(String name, YoixObject obj, boolean duplicate) {

	YoixBodyScreen  screen;

	if ((screen = getDefaultBlockScreen()) != null) {
	    if (screen != this)
		obj = screen.getField(name, obj);
	    else obj = screen.data.getObject(name);
	}

	return(obj != null && duplicate ? obj.duplicate() : obj);
    }


    private YoixObject
    getFullScreenSupported(YoixObject obj) {

	GraphicsDevice  gd;
	boolean         result = false;

	if ((gd = getScreenDevice()) != null)
	    result = gd.isFullScreenSupported();

	return(YoixObject.newInt(result));
    }


    private YoixObject
    getHeadless(YoixObject obj) {

	return(YoixObject.newInt(GraphicsEnvironment.isHeadless()));
    }


    private YoixObject
    getIndex(YoixObject obj) {

	return(YoixObject.newInt(screenindex));
    }


    private YoixObject
    getInsets(YoixObject obj) {

	GraphicsConfiguration  gc;
	GraphicsDevice         gd;
	Insets                 insets = null;

	if ((gd = getScreenDevice()) != null) {
	    if ((gc = gd.getDefaultConfiguration()) != null)
		insets = YoixAWTToolkit.getScreenInsets(gc);
	}

	return(insets != null ? YoixMakeScreen.yoixInsets(insets) : YoixObject.newInsets());
    }


    private static GraphicsDevice
    getScreenDevice(int index) {

	return(index >= 0 ? YoixMisc.getScreenDeviceByID((String)screenmap.get(new Integer(index))) : null);
    }


    private YoixObject
    getVirtualBounds(YoixObject obj) {

	GraphicsDevice  gds[];
	GraphicsDevice  gd;
	Rectangle       bounds = null;
	Rectangle       rect;
	int             n;

	//
	// Technically not quite correct (according to Java documentation)
	// but should be sufficient for our purposes - at least for now.
	//

	if ((gds = YoixMisc.getScreenDevices()) != null) {
	    bounds = new Rectangle();
	    for (n = 0; n < gds.length; n++)
		bounds = bounds.union(gds[n].getDefaultConfiguration().getBounds());
	    if ((gd = getScreenDevice()) != null) {
		if ((rect = gd.getDefaultConfiguration().getBounds()) != null) {
		    bounds.x = -rect.x;
		    bounds.y = -rect.y;
		}
	    }
	}

	return(bounds != null ? YoixMakeScreen.yoixRectangle(bounds) : YoixObject.newRectangle());
    }


    private int
    pickScreenIndex(int index) {

	int  count;

	//
	// Right now this forces the index into bounds for the current set
	// of screens, however there's probably no absolute guarantee it
	// will be a valid index at some future time (e.g., the user could
	// reconfigure screens) so bounds checks before using the returned
	// index aren't necessarily overkill.
	//

	if ((count = YoixMisc.getScreenDeviceCount()) >= 0) {
	    if (index < 0)
		index = 0;
	    else if (index >= count)
		index = count - 1;
	} else index = -1;

	return(index);
    }


    private void
    setDefaultField(String name, YoixObject obj) {

	YoixBodyScreen  screen;

	if ((screen = getDefaultBlockScreen()) != null) {
	    if (screen != this)
		screen.setField(name, obj);
	    else data.putObject(name, obj);
	}
    }


    private void
    setIndex(YoixObject obj) {

	GraphicsDevice  gds[];
	Integer         key;
	int             index;
	int             n;

	//
	// We assume here that gds[0] is the default screen, which is
	// currently enforced by YoixMisc.getScreenDevices().
	//

	if (screenindex < 0) {
	    if ((index = pickScreenIndex(obj.intValue())) >= 0) {
		if ((gds = YoixMisc.getScreenDevices()) != null) {
		    if (index < gds.length) {	// tiny chance this is needed
			key = new Integer(index);
			if (screenmap.containsKey(key) == false) {
			    if (index > 0) {
				for (n = 1; n < gds.length; n++) {
				    if (screenmap.containsValue(gds[n].getIDstring()) == false) {
					screenmap.put(key, gds[n].getIDstring());
					screendata.put(key, data);
					break;
				    }
				}
			    } else {
				screenmap.put(key, gds[0].getIDstring());	// we guarantee this
				screendata.put(key, data);
			    }
			}
		    } else index = -1;
		} else index = -1;
	    }
	    screenindex = index;
	}
    }
}

