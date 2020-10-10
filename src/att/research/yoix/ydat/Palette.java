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

package att.research.yoix.ydat;
import java.awt.*;
import java.util.*;
import att.research.yoix.*;

class Palette extends YoixPointerActive

    implements Constants

{

    private HashMap  colormap;
    private Color    currentpalette[];
    private Color    currentcolors[];
    private int      currentends[];

    private boolean  currentinverted = false;
    private boolean  canrank = true;
    private float    currenthue = 0.0f;
    private float    currentsaturation = 1.0f;
    private float    currentbrightness = 1.0f;
    private int      currentmodel = 2;

    private boolean  sequence = false;

    //
    // A callback function that lets users override every color selection
    // made by this palette.
    //

    private YoixObject  afterselect = null;

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

    private static HashMap  activefields = new HashMap(12);

    static {
	activefields.put(NL_AFTERSELECT, new Integer(VL_AFTERSELECT));
	activefields.put(NL_BRIGHTNESS, new Integer(VL_BRIGHTNESS));
	activefields.put(NL_CANRANK, new Integer(VL_CANRANK));
	activefields.put(NL_COLORS, new Integer(VL_COLORS));
	activefields.put(NL_HUE, new Integer(VL_HUE));
	activefields.put(NL_INVERTED, new Integer(VL_INVERTED));
	activefields.put(NL_MODEL, new Integer(VL_MODEL));
	activefields.put(NL_SATURATION, new Integer(VL_SATURATION));
	activefields.put(NL_SELECT, new Integer(VL_SELECT));
	activefields.put(NL_SEQUENCE, new Integer(VL_SEQUENCE));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    Palette() {

	this(YoixObject.newDictionary());
    }


    Palette(YoixObject data) {

	super(data);
	buildPalette();
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

	return(PALETTE);
    }

    ///////////////////////////////////
    //
    // Palette Methods
    //
    ///////////////////////////////////

    final Color[]
    getCurrentPalette() {

	Color  palette[];

	if (currentpalette != null) {
	    palette = new Color[currentpalette.length];
	    System.arraycopy(currentpalette, 0, palette, 0, palette.length);
	} else palette = null;
	return(palette);
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case VL_SELECT:
		obj = builtinSelect(name, argv);
		break;

	    default:
		obj = null;
		break;
	}
	return(obj);
    }


    protected final void
    finalize() {

	currentpalette = null;
	currentcolors = null;
	colormap = null;
	afterselect = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case VL_COLORS:
		obj = getColors(obj);
		break;
	}
	return(obj);
    }


    final synchronized int
    getIndex(Color color) {

	Object value;
	int    index;

	if (colormap != null) {
	    if ((value = colormap.get(color)) != null)
		index = ((Integer)value).intValue();
	    else index = currentpalette.length;
	} else index = currentpalette.length;
	return(index);
    }


    final Object
    getPaletteControls(double values[], int count) {

	Object  controls;
	double  tmp[];

	if (values != null && count > 0) {
	    if (count < values.length) {
		tmp = new double[count];
		System.arraycopy(values, 0, tmp, 0, count);
		values = tmp;
	    }
	    YoixMiscQsort.sort(values, 1);
	}

	return(values);
    }


    protected final Object
    getManagedObject() {

	return(currentpalette);
    }


    final double
    getMaxValue(Object controls) {

	double  values[];
	double  value;

	if (controls instanceof double[]) {
	    values = (double[])controls;
	    if (values.length > 0)
		value = values[values.length - 1];
	    else value = 0;
	} else value = 0;

	return(value);
    }


    final Color
    selectColor(int value, Color color) {

	Color  palette[];
	int    limit;

	if ((palette = currentpalette) != null && (limit = palette.length) > 0)
	    color = selectColor(value, limit, color);
	else color = afterSelect(value, 0, color);
	return(color);
    }


    final Color
    selectColor(int value, int limit, Color color) {

	Color  palette[];
	int    ends[];
	int    total;
	int    n;

	if ((palette = currentpalette) != null && (total = palette.length) > 0) {
	    n = Math.max(0, value);
	    if (limit > 0 && sequence == false) {
	        if (limit > 1 && n > 0) {
		    if (n < limit) {
			//
			// Only new syntax will have currentends set,
			// so the test should make sure we don't break
			// any existing palettes (at least not any that
			// are being used in production).
			//
			if ((ends = currentends) != null) {
			    //
			    // Written very quickly and not thoroughly
			    // tested so don't be surprised by mistakes
			    // or confused by the tests - there's lots
			    // of room for improvement!!
			    // 
			    if (n >= ends[0]) {
				if ((limit - n) >= (total - ends[1] - 1)) {
				    n = (int)((double)(total - 1)*n/(limit - 1) + .5);
				    if (n < ends[0])
					n = ends[0];
				    if (n > ends[1])
					n = ends[1];
				} else n = total - 1 - (limit - n);
			    }
			} else n = (int)((double)(total - 1)*n/(limit - 1) + .5);
		    } else n = total - 1;
		    color = palette[n];
		} else color = palette[0];
	    } else color = palette[n%total];
	}
	return(afterSelect(value, limit, color));
    }


    final Color
    selectColor(double value, double limit, Object controls, Color color) {

	double  values[];
	double  scale;
	Color   palette[];
	int     ends[];
	int     total;
	int     samples;
	int     index;
	int     rank;
	int     n;

	//
	// NOTE - we eventually could use a binary search, but for now this
	// brute force approach is sufficient.
	//

	if (canrank && controls instanceof double[]) {
	    values = (double[])controls;
	    if ((samples = values.length) > 0) {
		if ((palette = currentpalette) != null && (total = palette.length) > 0) {
		    if (value != 0) {
			if (total > 1) {
			    if (samples > 1) {
				limit = Math.min(limit, values[samples-1]);
				if (value < limit) {
				    rank = samples - 1;
				    for (n = 0; n < samples; n++) {
					if (value <= values[n]) {
					    rank = n;
					    while (n < samples && values[n] == value)
						n++;
					    rank += (n - rank)/2;
					    break;
					}
				    }
				    scale = ((double)(rank))/(samples - 1);
				    index = (int)(1 + scale*(total - 2));
				} else index = total - 1;
				color = palette[index];
			    } else color = (value < values[0]) ? palette[1] : palette[total-1];
			}
		    } else color = palette[0];
		}
		color = afterSelect(value, limit, color);
	    } else color = selectColor((int)value, (int)limit, color);
	} else color = selectColor((int)value, (int)limit, color);

	return(color);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case VL_AFTERSELECT:
		    setAfterSelect(obj);
		    break;

		case VL_BRIGHTNESS:
		    setBrightness(obj);
		    break;

		case VL_CANRANK:
		    setCanRank(obj);
		    break;

		case VL_COLORS:
		    setColors(obj);
		    break;

		case VL_HUE:
		    setHue(obj);
		    break;

		case VL_INVERTED:
		    setInverted(obj.booleanValue());
		    break;

		case VL_MODEL:
		    setModel(obj);
		    break;

		case VL_SATURATION:
		    setSaturation(obj);
		    break;

		case VL_SEQUENCE:
		    sequence = obj.booleanValue();
		    break;
	    }
	}
	return(obj);
    }


    final synchronized void
    setInverted(boolean state) {

	if (state != currentinverted) {
	    currentinverted = state;
	    createPalette();
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private Color
    afterSelect(double value, double limit, Color color) {

	YoixObject  funct;
	YoixObject  argv[];
	YoixObject  obj;

	if ((funct = afterselect) != null) {
	    if (funct.callable(3)) {
		argv = new YoixObject[] {
		    YoixObject.newDouble(value),
		    YoixObject.newDouble(limit),
		    YoixMake.yoixColor(color)
		};
	    } else if (funct.callable(2)) {
		argv = new YoixObject[] {
		    YoixObject.newDouble(value),
		    YoixObject.newDouble(limit)
		};
	    } else argv = null;
	    if (argv != null) {
		if ((obj = call(funct, argv)) != null) {
		    if (obj.isColor() && obj.notNull())
			color = YoixMake.javaColor(obj);
		}
	    }
	}
	return(color);
    }


    private void
    buildPalette() {

	setField(NL_CANRANK);
	setField(NL_AFTERSELECT);
	setField(NL_SEQUENCE);
	setField(NL_HUE);
	setField(NL_SATURATION);
	setField(NL_BRIGHTNESS);
	setField(NL_MODEL);
	setField(NL_INVERTED);
	setField(NL_COLORS);
    }


    private synchronized YoixObject
    builtinSelect(String name, YoixObject arg[]) {

	Color  color = null;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isInteger()) {
		if (arg.length == 1 || arg[1].isInteger()) {
		    color = selectColor(
			arg[0].intValue(),
			(arg.length == 2) ? arg[1].intValue() : 0,
			null
		    );
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixMake.yoixColor(color));
    }


    private synchronized void
    createPalette() {

	HashMap  map = null;
	Color    palette[] = null;
	Color    colors[];
	Color    color;
	float    brightness;
	float    hue;
	float    saturation;
	int      index;
	int      incr;
	int      model;
	int      denominator;
	int      total;
	int      n;

	if ((colors = currentcolors) != null && (total = colors.length) > 0) {
	    palette = new Color[total];
	    map = new HashMap();
	    hue = currenthue;
	    saturation = currentsaturation;
	    brightness = currentbrightness;
	    model = Math.max(0, Math.min(currentmodel, 4));
	    denominator = (total > 1) ? total - 1 : total;
	    if (currentinverted) {
		index = palette.length - 1;
		incr = -1;
	    } else {
		index = 0;
		incr = 1;
	    }
	    for (n = 0; n < total; index += incr, n++) {
		if ((color = colors[n]) == null) {
		    switch (model) {
			case 0:
			    brightness = 1.0f/(2*total) + ((float)n)/total;
			    break;

			case 1:
			    hue = ((float)n)/(3*denominator);
			    break;

			case 2:
			    hue = ((float)(2*n))/(3*denominator);
			    break;

			case 3:
			    hue = ((float)n)/total;
			    break;

			case 4:
			    saturation = 1.0f/(2*total) + ((float)n)/total;
			    break;
		    }
		    palette[index] = Color.getHSBColor(hue, saturation, brightness);
		} else palette[index] = color;
		map.put(palette[index], new Integer(index));
	    }

	    currentpalette = palette;
	    colormap = map;
	}
    }


    private synchronized YoixObject
    getColors(YoixObject obj) {

	return(YoixMisc.copyIntoArray(currentpalette));
    }


    private synchronized void
    setAfterSelect(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(2) || obj.callable(3))
		afterselect = obj;
	    else VM.abort(TYPECHECK, NL_AFTERSELECT);
	} else afterselect = null;
    }


    private synchronized void
    setBrightness(YoixObject obj) {

	double  value;

	if (obj != null) {
	    value = Math.max(0.0, Math.min(obj.doubleValue(), 1.0));
	    if (value != currentbrightness) {
		currentbrightness = (float)value;
		createPalette();
	    }
	}
    }


    private synchronized void
    setCanRank(YoixObject obj) {

	if (obj != null)
	    canrank = obj.booleanValue();
    }


    private synchronized void
    setColors(YoixObject obj) {

	YoixObject  element;
	ArrayList   list;
	Color       colors[];
	int         ends[];
	int         length;
	int         index;
	int         last;
	int         n;

	colormap = null;
	currentcolors = null;
	currentpalette = null;
	currentends = null;

	if (obj.notNull()) {
	    ends = null;
	    list = new ArrayList();
	    length = obj.length();
	    for (n = obj.offset(); n < length; n++) {
		if ((element = obj.getObject(n)) != null) {
		    if (element.isInteger()) {
			last = list.size() - 1;
			if (ends == null)
			    ends = new int[] {last, last};
			if ((index = element.intValue()) > last) {
			    element = (last >= 0) ? (YoixObject)list.get(last) : null;
			    for (; last < index; last++)
				list.add(element);
			}
			ends[1] = list.size() - 1;
		    } else list.add(element);
		} else list.add(null);
	    }

	    if ((length = list.size()) > 0) {
		colors = new Color[length];
		for (n = 0; n < length; n++) {
		    if ((element = (YoixObject)list.get(n)) != null) {
			if (element.isColor())
			    colors[n] = YoixMake.javaColor(element);
		    }
		}
		currentends = ends;
		currentcolors = colors;
		createPalette();
	    }
	}
    }


    private synchronized void
    setHue(YoixObject obj) {

	double  value;

	if (obj != null) {
	    value = Math.max(0.0, Math.min(obj.doubleValue(), 1.0));
	    if (value != currenthue) {
		currenthue = (float)value;
		createPalette();
	    }
	}
    }


    private synchronized void
    setModel(YoixObject obj) {

	int  value;

	if (obj != null) {
	    value = Math.max(0, obj.intValue());
	    if (value != currentmodel) {
		currentmodel = value;
		createPalette();
	    }
	}
    }


    private synchronized void
    setSaturation(YoixObject obj) {

	double  value;

	if (obj != null) {
	    value = Math.max(0.0, Math.min(obj.doubleValue(), 1.0));
	    if (value != currentsaturation) {
		currentsaturation = (float)value;
		createPalette();
	    }
	}
    }
}

