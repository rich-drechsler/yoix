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
import java.util.ArrayList;
import att.research.yoix.*;

public
class AxisModelDefault extends AxisModel

{


    //
    // This should duplicate the original axis behavoir. It's really ugly
    // code that should not be copied and the behavior should also not be
    // modified. Take a look at like AxisModelUnixTime.java if you want a
    // much better example.
    //

    private YoixObject  tickgenerator = null;
    private double      ticksettings[] = {0, 10, 5};	// a resonable default
    private double      tickscales[];
    private double      tickorigin;
    private double      tickstep;

    //
    // Label controls - our printf support code in YoixMiscPrintf.java
    // probably should provide simple access to some of the low level
    // routines.
    //

    private YoixObject  labelgenerator = null;
    private YoixObject  labelformat[] = null;
    private String      labelstrings[] = null;
    private double      labelstep;

    //
    // Stuff for tracking and resetting labels and ticks - a recent
    // addition mostly to improve axis repainting when we zoom out
    // after labels or ticks have been changed.
    //

    private YoixObject  currentlabels = null;
    private YoixObject  currentticks = null;
    private YoixObject  limitlabels = null;
    private YoixObject  limitticks = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    AxisModelDefault(DataAxis axis) {

	super(axis);
    }

    ///////////////////////////////////
    //
    // AxisModel Methods
    //
    ///////////////////////////////////

    public synchronized void
    drawLabels(int low, int high, Graphics g) {

	YoixObject  obj;
	YoixObject  args[];
	Rectangle   bbox;
	Rectangle   clip;
	Rectangle   rect;
	Rectangle   viewport;
	Dimension   cellsize;
	boolean     inverted;
	Insets      insets;
	Object      formatted[];
	String      str;
	double      axisends[];
	double      axisscale;
	double      incr;
	double      tick;
	double      delta;
	double      step;
	double      halftick;
	double      maxwidth;
	double      minchars;
	int         anchor;
	int         baseline;
	int         leading;
	int         ticklength;
	int         tickwidth;
	int         fontadjust;
	int         width;
	int         first;
	int         last;
	int         index;
	int         length;
	int         dx = 0;
	int         dy = 0;
	int         x;
	int         y;
	int         n;

	//
	// Bit more to do here. Decided to grow clip rectangle so labels
	// fit better, but probably should restore original clip path at
	// then end and use border insets in the adjustment. Also need to
	// check and adjust label positioning when it doesn't quite fit,
	// primarily for end labels. Generating labels once may be worth
	// the effort, but you probably won't notice performance improve
	// much because the label strings aren't drawn often compared to
	// the slider and ticks.
	//
	// NOTE - Recently (9/15/04) added code that tries to stop labels
	// from printing on top of each other. We probably should use the
	// TextLayout class, but it doesn't behave well on at least one
	// system (an SGI running Java 1.3.1) that really needs the axis
	// code so we estimate the bounding box on our own. Several things
	// misbehaved, including TextLayout.getBounds(), so be careful if
	// you decide to improve our implementation!!
	//
	// NOTE - made some change to first and last calculation to make
	// sure we repaint labels that really need it (7/12/06). Changed
	// calculation of first and last to extend the interval by one on
	// both sides. In addition we make sure the labels that we draw
	// all correspond to real ticks by verifying the viewport really
	// contains the tick. Calculations done here are messy (this is
	// really old code) and very hard to follow - sorry!!
	//

	if ((axisends = axis.getAxisEnds()) != null && tickscales != null && labelstep > 0) {
	    anchor = axis.getAnchor();
	    axisscale = axis.getAxisScale();
	    baseline = axis.getBaseline();
	    cellsize = axis.getCellSize();
	    insets = axis.getInsets();
	    inverted = axis.getInverted();
	    leading = axis.getLeading();
	    ticklength = axis.getTickLength();
	    tickwidth = axis.getLineWidth();
	    viewport = axis.getViewport();
	    clip = g.getClipBounds();
	    clip.x -= insets.left;
	    clip.y -= insets.top;
	    clip.width += insets.left + insets.right;
	    clip.height += insets.top + insets.bottom;
	    halftick = .5*tickwidth;
	    fontadjust = (cellsize.height - baseline);		// includes leading
	    delta = (tickorigin - axisends[0])*axisscale;
	    if (axis.getOrientation() == YOIX_HORIZONTAL) {
		if (inverted) {
		    first = (int)Math.floor((viewport.width - high - delta)/labelstep);
		    last = (int)Math.ceil(((viewport.width - low - delta)/labelstep));
		} else {
		    first = (int)Math.floor(((low - delta)/labelstep));
		    last = (int)Math.ceil((high + tickwidth - delta)/labelstep);
		}
		maxwidth = YoixMakeScreen.yoixDistance(labelstep);
	    } else {
		if (inverted) {
		    first = (int)Math.floor(((low - delta)/labelstep));
		    last = (int)Math.ceil((high + tickwidth - delta)/labelstep);
		} else {
		    first = (int)Math.floor((viewport.height - high - delta)/labelstep);
		    last = (int)Math.ceil(((viewport.height - low - delta)/labelstep));
		}
		maxwidth = YoixMakeScreen.yoixDistance((viewport.width - ticklength - leading));
	    }

	    //
	    // We now adjust first and last to make sure we repaint labels
	    // that really need it, but we also added a check to make sure
	    // that the corresponds to a real tick.
	    //

	    first = Math.max(first - 1, 0);
	    last = last + 1;

	    minchars = maxwidth/YoixMakeScreen.yoixDistance(cellsize.width);
	    index = 0;
	    if (labelstrings != null) {
		if ((length = labelstrings.length) > 0)
		    index += ((first >= 0) ? first : length - ((-first)%length));
	    } else length = 0;
	    g.setClip(clip);
	    incr = pickTickIncrement(ticksettings[0]);

	    //
	    // Recent additions that we use to dectect when a label prints
	    // on that labels we've already printed. If so we can skip it.
	    //
	    bbox = null;

	    for (n = first; n < last; n++) {
		if (labelstrings == null || length == 0) {
		    tick = tickorigin + n*incr;
		    if (labelgenerator != null) {
			if (labelgenerator.callable(3)) {
			    args = new YoixObject[] {
				YoixObject.newDouble(tick),
				YoixObject.newDouble(maxwidth),
				YoixObject.newDouble(minchars)
			    };
			} else if (labelgenerator.callable(2)) {
			    args = new YoixObject[] {
				YoixObject.newDouble(tick),
				YoixObject.newDouble(maxwidth)
			    };
			} else args = new YoixObject[] {YoixObject.newDouble(tick)};
			obj = axis.callGenerator(labelgenerator, args);
			if (obj.isString())
			    str = obj.stringValue();
			else str = (int)tick + "";
		    } else if (labelformat != null && labelformat.length == 2) {
			labelformat[1] = YoixObject.newDouble(tick);
			formatted = YoixMiscPrintf.format(labelformat, 0);
			if ((str = (String)formatted[0]) == null)
			    str = (int)tick + "";
		    } else str = (int)tick + "";
		} else str = labelstrings[index++%length];
		width = axis.getFontMetrics().stringWidth(str);
		step = n*labelstep + delta;
		switch (anchor) {
		    default:			// YOIX_NORTH
			if (inverted) {
			    x = (int)(viewport.width - step - halftick);
			    dx = -width/2;
			} else {
			    x = (int)(step + halftick);
			    dx = -width/2;
			}
			y = ticklength + baseline + leading;
			break;

		    case YOIX_SOUTH:
			if (inverted) {
			    x = (int)(viewport.width - step - halftick);
			    dx = -width/2;
			} else {
			    x = (int)(step + halftick);
			    dx = -width/2;
			}
			y = viewport.height - ticklength - fontadjust;
			break;

		    case YOIX_EAST:
			x = viewport.width - ticklength - leading - width;
			if (inverted) {
			    y = (int)(step + halftick);
			    dy = fontadjust + 1;
			} else {
			    y = (int)(viewport.height - step - halftick);
			    dy = fontadjust;
			}
			break;

		    case YOIX_WEST:
			x = ticklength + leading;
			if (inverted) {
			    y = (int)(step + halftick);
			    dy = fontadjust + 1;
			} else {
			    y = (int)(viewport.height - step - halftick);
			    dy = fontadjust;
			}
			break;
		}
		if (viewport.contains(x, y)) {	// make sure it's a real tick
		    x += dx;
		    y += dy;
		    rect = new Rectangle(x, y - baseline, width, cellsize.height);
		    if (bbox == null || bbox.intersects(rect) == false) {
			g.drawString(str, x, y);
			if (bbox == null)
			    bbox = rect;
			else bbox.add(rect);
		    }
		}
	    }
	}
    }


    public synchronized void
    drawTicks(int low, int high, Graphics g) {

	Rectangle  viewport;
	boolean    inverted;
	double     axisends[];
	double     axisscale;
	double     scale;
	double     delta;
	int        anchor;
	int        ticklength;
	int        tickwidth;
	int        width;
	int        height;
	int        length;
	int        first;
	int        last;
	int        index;
	int        x;
	int        y;
	int        n;

	if ((axisends = axis.getAxisEnds()) != null && tickscales != null) {
	    anchor = axis.getAnchor();
	    axisscale = axis.getAxisScale();
	    inverted = axis.getInverted();
	    ticklength = axis.getTickLength();
	    tickwidth = axis.getLineWidth();
	    viewport = axis.getViewport();
	    length = tickscales.length;		// guaranteed nonzero
	    delta = (tickorigin - axisends[0])*axisscale;
	    if (axis.getOrientation() == YOIX_HORIZONTAL) {
		if (inverted) {
		    first = (int)Math.floor((viewport.width - high - delta)/tickstep);
		    last = (int)Math.ceil(((viewport.width - low - delta)/tickstep));
		} else {
		    first = (int)Math.floor(((low - delta)/tickstep));
		    last = (int)Math.ceil((high + tickwidth - delta)/tickstep);
		}
	    } else {
		if (inverted) {
		    first = (int)Math.floor(((low - delta)/tickstep));
		    last = (int)Math.ceil((high + tickwidth - delta)/tickstep);
		} else {
		    first = (int)Math.floor((viewport.height - high - delta)/tickstep);
		    last = (int)Math.ceil(((viewport.height - low - delta)/tickstep));
		}
	    }
	    index = (first >= 0) ? first : length - ((-first)%length);
	    for (n = first; n < last; n++) {
		scale = tickscales[index++%length];
		switch (anchor) {
		    default:			// YOIX_NORTH
			height = (int)(scale*ticklength);
			width = tickwidth;
			if (inverted)
			    x = (int)(viewport.width - (n*tickstep + delta) - width);
			else x = (int)(n*tickstep + delta + .5);
			y = 0;
			break;

		    case YOIX_SOUTH:
			height = (int)(scale*ticklength);
			width = tickwidth;
			if (inverted)
			    x = (int)(viewport.width - (n*tickstep + delta) - width);
			else x = (int)(n*tickstep + delta + .5);
			y = viewport.height - height;
			break;

		    case YOIX_EAST:
			height = tickwidth;
			width = (int)(scale*ticklength);
			x = viewport.width - width;
			if (inverted)
			    y = (int)(n*tickstep + delta + .5);
			else y = (int)(viewport.height - (n*tickstep + delta + height));
			break;

		    case YOIX_WEST:
			height = tickwidth;
			width = (int)(scale*ticklength);
			x = 0;
			if (inverted)
			    y = (int)(n*tickstep + delta + .5);
			else y = (int)(viewport.height - (n*tickstep + delta + height));
			break;
		}
		g.fillRect(x, y, width, height);
	    }
	}
    }


    protected void
    finalize() {

	currentlabels = null;
	currentticks = null;
	limitlabels = null;
	limitticks = null;
	super.finalize();
    }


    public synchronized String[]
    generateLabels() {

	YoixAWTFontMetrics  fm;
	ArrayList           labels;
	double              axislimits[];
	Object              formatted[];
	String              strings[];
	String              longest;
	String              str;
	double              incr;
	double              value;
	int                 width;
	int                 n;

	//
	// Used to generate sample label strings that can we examine when
	// a layout manager asks for a size recommendation. Actual labels
	// usually won't match the strings that we generate, at least not
	// right now, but that eventually may change. We try to return an
	// array that contains the longest label, although there currently
	// is no guarantee, and that also contains the maximum number of
	// different labels that we would ever want. Both calculations
	// are estimates still need much work!! Should this method also
	// know what "size" the layout manager is looking for??
	//

	if ((axislimits = axis.getAxisLimits()) != null && ticksettings != null) {
	    labels = new ArrayList();
	    if ((incr = pickTickIncrement(ticksettings[0])) > 0) {
		longest = null;
		if (labelstrings != null && labelstrings.length > 0) {
		    fm = axis.getFontMetrics();
		    width = 0;
		    for (n = 0; n < labelstrings.length; n++) {
			if (fm.stringWidth(labelstrings[n]) > width)
			    longest = labelstrings[n];
		    }
		}
		for (value = axislimits[0]; value <= axislimits[1]; value += incr) {
		    if (longest == null) {
			if (labelformat != null && labelformat.length == 2) {
			    labelformat[1] = YoixObject.newDouble(value);
			    formatted = YoixMiscPrintf.format(labelformat, 0);
			    if ((str = (String)formatted[0]) == null)
				str = (int)value + "";
			} else str = (int)value + "";
		     } else str = longest;
		     labels.add(str);
		}
	    }
	    strings = new String[labels.size()];
	    for (n = 0; n < strings.length; n++)
		strings[n] = (String)labels.get(n);
	} else strings = null;

	return(strings);
    }


    public synchronized void
    makeTicks() {

	SwingJEventPlot  plot;
	double           ends[];

	if ((ends = axis.getAxisEnds()) != null) {
	    if (ends[1] > ends[0]) {
		if ((plot = axis.getEventPlot()) != null) {
		    if (axis.getOrientation() == YOIX_HORIZONTAL && plot.getSpread())
			makeUniformTicks(ends, plot);
		    else makeTicks(ends);
		} else makeTicks(ends);
	    }
	}
    }


    public synchronized void
    setLabels(YoixObject obj) {

	YoixObject  value;
	YoixObject  generator;
	YoixObject  format[];
	String      labels[];
	int         length;
	int         offset;
	int         n;

	//
	// The initial null check is needed because this is called from
	// several places.
	//

	if (obj != null) {
	    if (obj.isArray() || obj.isNull() || obj.isString() || obj.isCallable()) {
		if (compare(obj, currentlabels) == false) {
		    generator = null;
		    format = null;
		    labels = null;
		    offset = 0;
		    if (obj.isArray()) {
			if (obj.notNull()) {
			    length = obj.length();
			    if ((offset = obj.offset()) < 0)
				offset = length - (-offset)%length;
			    labels = new String[length];
			    for (n = 0; n < length; n++) {
				if ((value = obj.getObject((n+offset)%length)) != null) {
				    if (value.isString())
					labels[n] = value.stringValue();
				    else VM.abort(BADVALUE, n);
				} else labels[n] = "";
			    }
			}
		    } else if (obj.notNull()) {
			if (obj.isString()) {
			    format = new YoixObject[2];
			    format[0] = obj;
			} else if (obj.callable(1) || obj.callable(2) || obj.callable(3))
			    generator = obj;
			else VM.abort(TYPECHECK, NL_LABELS);
		    }
		    labelgenerator = generator;
		    labelformat = format;
		    labelstrings = labels;
		    currentlabels = obj;
		    if (axis.hitAxisLimits())
			limitlabels = obj;
		    axis.resetAxis();
		}
	    } else VM.abort(TYPECHECK, NL_LABELS);
	}
    }


    public synchronized void
    setTicks(YoixObject obj) {

	double  ticks[];
	int     length;
	int     offset;
	int     n;

	//
	// The initial null check is needed because this is called from
	// several places.
	//

	if (obj != null) {
	    if (obj.isArray() || obj.isNull() || obj.callable(2)) {
		if (compare(obj, currentticks) == false) {
		    ticksettings = null;
		    tickgenerator = null;
		    ticks = new double[] {0, 10, 5};
		    if (obj.isArray() || obj.isNull()) {
			if (obj.notNull()) {
			    length = Math.min(obj.sizeof(), 3);
			    offset = obj.offset();
			    for (n = offset; n < length; n++)
				ticks[n] = obj.getDouble(n, ticks[n]);
			}
		    } else tickgenerator = obj;
		    ticksettings = ticks;
		    currentticks = obj;
		    if (axis.hitAxisLimits())
			limitticks = obj;
		    axis.resetAxis();
		}
	    } else VM.abort(TYPECHECK, NL_LABELS);
	}
    }


    public synchronized void
    syncToAxisLimits() {

	setTicks(limitticks);
	setLabels(limitlabels);
    }


    public synchronized void
    syncToEnds(double ends[]) {

	//
	// Not sure if there's more to do here, at least for this model,
	// however other models probably will be different.
	//

	if (ends == null) {
	    currentlabels = null;
	    currentticks = null;
	    limitlabels = null;
	    limitticks = null;
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private boolean
    compare(YoixObject src, YoixObject dest) {

	YoixObject  left;
	YoixObject  right;
	boolean     result = false;
	int         length;
	int         m;
	int         n;

	if (src != null && dest != null) {
	    if (src.notNull() && dest.notNull()) {
	        if (src.isArray() && dest.isArray()) {
		    if (src.sizeof() == dest.sizeof()) {
			length = dest.length();
			n = dest.offset();
			m = src.offset();
			for (result = true; n < length && result; n++, m++) {
			    left = dest.getObject(n);
			    right = src.getObject(m);
			    if (left != right) {
				if (left != null && right != null)
				    result = left.equals(right);
				else result = false;
			    }
			}
		    } else result = false;
		} else result = false;
	    } else result = src.equals(dest);
	} else result = false;

	return(result);
    }


    private synchronized void
    makeTicks(double axisends[]) {

	YoixObject  obj;
	YoixObject  args[];
	double      ticks[];
	double      axisscale;
	double      step;
	int         tickwidth;
	int         intervals;
	int         regions;
	int         separation;
	int         raise;
	int         n;

	axisscale = axis.getAxisScale();
	tickwidth = axis.getLineWidth();
	if (tickgenerator != null) {
	    args = new YoixObject[] {
		YoixObject.newDouble(axisends[0]),
		YoixObject.newDouble(axisends[1])
	    };
	    if ((obj = axis.callGenerator(tickgenerator, args)) != null) {
		if (obj.isArray() && obj.notNull()) {
		    ticksettings = new double[] {
			obj.getDouble(0, 0),
			obj.getDouble(1, 10),
			obj.getDouble(2, 5)
		    };
		}
	    }
	}
	intervals = (int)ticksettings[1];
	raise = (int)ticksettings[2];
	regions = Math.max(intervals, 1);
	step = axisscale*pickTickIncrement(ticksettings[0])/regions;
	separation = Math.max(2*tickwidth + 1, 5);
	if (step < separation && regions > 1) {
	    step = step*intervals;
	    for (regions--; regions > 1; regions--) {
		if (intervals%regions == 0) {
		    if (step/regions >= separation) {
			step /= regions;
			break;
		    }
		}
	    }
	}
	tickscales = new double[Math.max(regions, 1)];
	tickscales[0] = 1.0;
	for (n = 1; n < regions; n++) {
	    if (raise > 0 && 2*raise <= regions && (n%raise) == 0)
		tickscales[n] = 0.8;
	    else tickscales[n] = 0.65;
	}
	tickstep = step;
	tickorigin = 0;
	labelstep = step*regions;
    }


    private synchronized void
    makeUniformTicks(double ends[], SwingJEventPlot plot) {

	double  ticks[];
	double  axisscale;
	double  step;
	int     tickwidth;
	int     intervals;
	int     regions;
	int     separation;
	int     n;

	ticksettings = new double[] {0, 10, 0};
	axisscale = axis.getAxisScale();
	tickwidth = axis.getLineWidth();

	intervals = (int)ticksettings[1];
	regions = Math.max(intervals, 1);
	step = axisscale*pickTickIncrement(ticksettings[0])/regions;
	separation = Math.max(2*tickwidth + 1, 5);

	if (step < separation && regions > 1) {
	    step = step*intervals;
	    for (regions--; regions > 1; regions--) {
		if (intervals%regions == 0) {
		    if (step/regions >= separation) {
			step /= regions;
			break;
		    }
		}
	    }
	}

	tickscales = new double[] {0.8};
	tickstep = step;
	tickorigin = ends[0];
	labelstep = 0;		// means labels are skipped
    }


    private double
    pickTickIncrement(double incr) {

	Rectangle  viewport;
	Dimension  cellsize;
	double     axislimits[];
	double     delta;
	int        count;

	if (incr <= 0) {
	    if ((axislimits = axis.getAxisLimits()) != null) {
		if ((delta = axislimits[1] - axislimits[0]) > 0) {
		    if (incr > -1) {
			cellsize = axis.getCellSize();
			viewport = axis.getViewport();
			switch (axis.getOrientation()) {
			    case YOIX_VERTICAL:
				if (viewport.height > 0 && cellsize.height > 0) {
				    count = (viewport.height/cellsize.height + 1)/2;
				    count = Math.max(1, Math.min(count, 10));
				} else count = 10;
				break;

			    case YOIX_HORIZONTAL:
				if (viewport.width > 0 && cellsize.width > 0) {
				    count = (viewport.width/cellsize.width + 1)/2;
				    count = Math.max(1, Math.min(count, 10));
				} else count = 10;
				break;

			    default:
				count = 10;
				break;
			}
		    } else count = -(int)incr;
		    incr = delta/count;
		} else incr = 10;
	    } else incr = 10;
	}
	return(incr);
    }
}

