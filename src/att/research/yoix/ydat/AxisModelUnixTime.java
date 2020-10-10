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

public
class AxisModelUnixTime extends AxisModel

{

    //
    // This is for Rick, so ask him before changing things. If you need
    // a modified version extend this class, make changes in that class,
    // and use your new version in SwingJAxis.setAxisModel(). You also
    // should update AWTAxis.setAxisModel(), even though AWT versions
    // probably aren't used much anymore.
    //
    // Some of the code appears to support horizontal or vertical axes,
    // however there are places where we currently assume the axis is
    // horizontal - we eventually may look into this, but it's not all
    // that important for our applications.
    //

    private TickInfo  tickinfo[] = null;

    //
    // Miscellaneous constants...
    //

    private static int  MINUTE = 60;
    private static int  QUARTER_HOUR = 15*MINUTE;
    private static int  HALF_HOUR = 30*MINUTE;
    private static int  HOUR = 60*MINUTE;
    private static int  DAY = 24*HOUR;
    private static int  WEEK = 7*DAY;

    //
    // Weekday names.
    //

    private static String  WEEKDAYS[] = {
	"Thursday",
	"Friday",
	"Saturday",
	"Sunday",
	"Monday",
	"Tuesday",
	"Wednesday",
    };

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    AxisModelUnixTime(DataAxis axis) {

	super(axis);
    }

    ///////////////////////////////////
    //
    // AxisModel Methods
    //
    ///////////////////////////////////

    public synchronized void
    drawLabels(int low, int high, Graphics g) {

	Rectangle  bbox;
	Rectangle  clip;
	Rectangle  rect;
	Rectangle  viewport;
	Dimension  cellsize;
	TickInfo   ticks[];
	TickInfo   info;
	boolean    inverted;
	Insets     insets;
	String     str;
	double     tick;
	double     halftick;
	int        anchor;
	int        baseline;
	int        leading;
	int        ticklength;
	int        tickwidth;
	int        fontadjust;
	int        width;
	int        x;
	int        y;
	int        n;

	//
	// Bit more to do here. Decided to grow clip rectangle so labels
	// fit better, but probably should restore original clip path at
	// then end and use border insets in the adjustment. Also need to
	// check and adjust label positioning when it doesn't quite fit,
	// primarily for end labels.
	//

	if ((ticks = tickinfo) != null) {
	    anchor = axis.getAnchor();
	    baseline = axis.getBaseline();
	    cellsize = axis.getCellSize();
	    insets = axis.getInsets();
	    inverted = axis.getInverted();
	    leading = axis.getLeading();
	    ticklength = axis.getTickLength();
	    tickwidth = axis.getLineWidth();
	    viewport = axis.getViewport();

	    halftick = .5*tickwidth;
	    fontadjust = (cellsize.height - baseline);		// includes leading

	    clip = g.getClipBounds();
	    clip.x -= insets.left;
	    clip.y -= insets.top;
	    clip.width += insets.left + insets.right;
	    clip.height += insets.top + insets.bottom;
	    g.setClip(clip);

	    //
	    // Recent additions that we use to detect when a label prints
	    // on labels we've already printed. If so we can skip it.
	    //
	    bbox = null;

	    for (n = 0; n < ticks.length; n++) {
		info = ticks[n];
		if ((str = info.label) != null) {
		    width = axis.getFontMetrics().stringWidth(str);
		    switch (anchor) {
			default:			// YOIX_NORTH
			    if (inverted)
				x = (int)(viewport.width - info.offset - halftick - width/2);
			    else x = (int)(info.offset + halftick - width/2);
			    y = ticklength + baseline + leading;
			    break;

			case YOIX_SOUTH:
			    if (inverted)
				x = (int)(viewport.width - info.offset - halftick - width/2);
			    else x = (int)(info.offset + halftick - width/2);
			    y = viewport.height - ticklength - fontadjust;
			    break;

			case YOIX_EAST:
			    x = viewport.width - ticklength - leading - width;
			    if (inverted)
				y = (int)(info.offset + halftick + fontadjust + 1);
			    else y = (int)(viewport.height - info.offset - halftick + fontadjust);
			    break;

			case YOIX_WEST:
			    x = ticklength + leading;
			    if (inverted)
				y = (int)(info.offset + halftick + fontadjust + 1);
			    else y = (int)(viewport.height - info.offset - halftick + fontadjust);
			    break;
		    }
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
	TickInfo   ticks[];
	TickInfo   info;
	boolean    inverted;
	double     scale;
	int        anchor;
	int        ticklength;
	int        tickwidth;
	int        width;
	int        height;
	int        length;
	int        x;
	int        y;
	int        n;

	if ((ticks = tickinfo) != null) {
	    anchor = axis.getAnchor();
	    inverted = axis.getInverted();
	    ticklength = axis.getTickLength();
	    tickwidth = axis.getLineWidth();
	    viewport = axis.getViewport();
	    for (n = 0; n < ticks.length; n++) {
		info = ticks[n];
		switch (anchor) {
		    default:			// YOIX_NORTH
			height = (int)(info.scale*ticklength);
			width = tickwidth;
			if (inverted)
			    x = (int)(viewport.width - info.offset - width);
			else x = (int)(info.offset + .5);
			y = 0;
			break;

		    case YOIX_SOUTH:
			height = (int)(info.scale*ticklength);
			width = tickwidth;
			if (inverted)
			    x = (int)(viewport.width - info.offset - width);
			else x = (int)(info.offset + .5);
			y = viewport.height - height;
			break;

		    case YOIX_EAST:
			height = tickwidth;
			width = (int)(info.scale*ticklength);
			x = viewport.width - width;
			if (inverted)
			    y = (int)(info.offset + .5);
			else y = (int)(viewport.height - (info.offset + height));
			break;

		    case YOIX_WEST:
			height = tickwidth;
			width = (int)(info.scale*ticklength);
			x = 0;
			if (inverted)
			    y = (int)(info.offset + .5);
			else y = (int)(viewport.height - (info.offset + height));
			break;
		}
		g.fillRect(x, y, width, height);
	    }
	}
    }


    protected void
    finalize() {

	super.finalize();
    }


    public synchronized String[]
    generateLabels() {

	ArrayList  labels;
	double     axislimits[];
	String     strings[];
	String     longest;
	double     value;
	double     limit;
	int        n;

	//
	// Only used to generate sample label strings that can we examine
	// when a layout manager asks for a size recommendation. We really
	// don't try hard to duplicate labels that really will be used, so
	// don't take this too seriously.
	//

	if ((axislimits = axis.getAxisLimits()) != null) {
	    if ((longest = getLongestLabel(WEEKDAYS, 3)) != null) {
		labels = new ArrayList(60);
		limit = Math.min(axislimits[0] + 60*DAY, axislimits[1]);
		for (value = axislimits[0]; value <= limit; value += DAY)
		    labels.add(longest);
		strings = new String[labels.size()];
		for (n = 0; n < strings.length; n++)
		    strings[n] = (String)labels.get(n);
	    } else strings = null;
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

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private String
    getLongestLabel(String labels[], int count) {

	return(getLongestLabel(labels, count, -1));
    }


    private String
    getLongestLabel(String labels[], int count, double limit) {

	YoixAWTFontMetrics  fm;
	String              label;
	String              longest = null;
	int                 length = -1;
	int                 width;
	int                 n;

	if (labels != null) {
	    if ((fm = axis.getFontMetrics()) != null) {
		for (n = 0; n < labels.length; n++) {
		    if ((label = labels[n]) != null) {
			if (count > 0 && label.length() > count)
			    label = label.substring(0, count);
			if ((width = fm.stringWidth(label)) > length) {
			    length = width;
			    longest = label;
			}
		    }
		}
	    }
	}
	return(limit < 0 || length <= limit ? longest : null);
    }


    private synchronized void
    makeTicks(double ends[]) {

	GregorianCalendar  calendar;
	ArrayList          list = null;
	TickInfo           temp[];
	TickInfo           info;
	TimeZone           timezone;
	String             labels[];
	String             label;
	double             separation;
	double             position;
	double             scale;
	double             days;
	double             span;
	double             delta;
	double             step;
	double             limit;
	long               end;
	int                prefixes[];
	int                index;
	int                offset;
	int                raise;
	int                n;

	if ((span = ends[1] - ends[0]) > 0) {
	    if ((scale = axis.getAxisScale()) > 0) {
		list = new ArrayList();
		separation = Math.max(2*(axis.getLineWidth() + 1), 5)/scale;
		limit = ends[0] + (axis.getAxisSpan() - axis.getLineWidth())/scale;
		prefixes = new int[] {3, 1};
		if ((days = span/DAY) < 365) {
		    if (days > 84) {
			span = WEEK;
			step = DAY;
			raise = 1;
		    } else if (days > 40) {
			span = DAY;
			step = 4*HOUR;
			raise = 2;
		    } else if (days > 7) {
			span = DAY;
			step = HOUR;
			raise = 6;
		    } else {
			span = DAY;
			step = QUARTER_HOUR;
			raise = 24;
		    }
		    if (separation < span) {
			if (step < separation) {
			    delta = step*Math.ceil(separation/step);
			    for (; delta < span; delta += step) {
				if (delta*((int)(span/delta)) == span)
				    break;
			    }
			    delta = Math.min(span, delta);
			} else delta = step;
		    } else delta = span*Math.ceil(separation/span);
		    if (delta > 0) {
			if ((labels = pickLongestLabels(WEEKDAYS, prefixes, span*scale)) == null)
			    labels = pickLongestLabels(WEEKDAYS, new int[] {1}, -1);
			position = delta*Math.ceil(ends[0]/delta);
			while (position < limit) {
			    offset = (int)(scale*(position - ends[0]));
			    info = new TickInfo(position, offset);
			    if ((position % span) == 0) {
				if (labels != null) {
				    index = (int)(position/span);
				    if (span > DAY)
					index *= (int)(span/DAY);
				    info.label = labels[index % labels.length];
				}
			    } else {
				if (((position%span)/step) % raise == 0)
				    info.scale = 0.8;
				else info.scale = 0.65;
			    }
			    list.add(info);
			    position += delta;
			}
		    }
		} else {
		    timezone = TimeZone.getTimeZone("UTC");
		    calendar = new GregorianCalendar(timezone);
		    calendar.setTimeInMillis((long)(1000*ends[0]));
		    calendar.clear();
		    calendar.set(calendar.get(Calendar.YEAR), 0, 1, 0, 0, 0);
		    while ((position = calendar.getTimeInMillis()/1000) < limit) {
			if (position >= ends[0]) {
			    offset = (int)(scale*(position - ends[0]));
			    info = new TickInfo(position, offset);
			    if (calendar.get(Calendar.MONTH) == 0)
				info.label = calendar.get(Calendar.YEAR) + "";
			    else info.scale = 0.65;
			    list.add(info);
			}
			calendar.add(Calendar.MONTH, 1);
		    }
		}
		if (list != null) {
		    temp = new TickInfo[list.size()];
		    for (n = 0; n < temp.length; n++)
			temp[n] = (TickInfo)list.get(n);
		    tickinfo = temp;
		} else tickinfo = null;
	    } else tickinfo = null;
	} else tickinfo = null;
    }


    private synchronized void
    makeUniformTicks(double ends[], SwingJEventPlot plot) {

	Rectangle  viewport;
	Dimension  cellsize;
	ArrayList  list = null;
	TickInfo   temp[];
	TickInfo   info;
	double     limits[];
	double     delta;
	double     position;
	double     scale;
	double     step;
	int        n;

	if ((limits = axis.getAxisLimits()) != null) {
	    if ((delta = limits[1] - limits[0]) > 0) {
		if ((scale = axis.getAxisScale()) > 0) {
		    cellsize = axis.getCellSize();
		    viewport = axis.getViewport();
		    if (viewport.width > 0 && cellsize.width > 0)
			step = delta/Math.max(1, (viewport.width/cellsize.width + 1)/2);
		    else step = delta/10;
		    list = new ArrayList();
		    for (position = ends[0]; position <= ends[1]; position += step) {
			info = new TickInfo(position, (int)(scale*(position - ends[0])));
			info.scale = 0.8;
			list.add(info);
		    }
		    temp = new TickInfo[list.size()];
		    for (n = 0; n < temp.length; n++)
			temp[n] = (TickInfo)list.get(n);
		    tickinfo = temp;
		} else tickinfo = null;
	    } else tickinfo = null;
	} else tickinfo = null;
    }


    private String[]
    pickLongestLabels(String labels[], int prefixes[], double limit) {

	String  longest[] = null;
	String  label;
	int     count;
	int     m;
	int     n;

	if (labels != null && prefixes != null) {
	    for (n = 0; n < prefixes.length; n++) {
		if (getLongestLabel(labels, prefixes[n], limit) != null) {
		    if ((count = prefixes[n]) > 0) {
			longest = new String[labels.length];
			for (m = 0; m < labels.length; m++) {
			    if ((label = labels[m]) != null) {
				if (label.length() > count)
				    longest[m] = label.substring(0, count);
				else longest[m] = label;
			    } else longest[m] = label;
			}
		    } else longest = labels;
		    break;
		}
	    }
	}
	return(longest);
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class TickInfo {

	String  label = null;
	double  value = 0;
	double  scale = 1.0;
	int     offset = 0;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	TickInfo(double value, int offset) {

	    this.value = value;
	    this.offset = offset;
	}

	///////////////////////////////////
	//
	// TickInfo Methods
	//
	///////////////////////////////////

	public String
	toString() {

	    return("[" + value + ", " + offset + ", " + scale + ", " + label + "]");
	}
    }
}

