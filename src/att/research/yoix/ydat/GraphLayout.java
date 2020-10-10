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
import java.awt.geom.*;
import att.research.yoix.*;

public abstract
class GraphLayout

    implements Constants

{

    //
    // Suspect we'll eventually want a more general approach that likely
    // will represent nodes (and maybe edges too) by Java Shapes, like a
    // Rectangle2D or Ellipse2D. The node and edge shapes could be built
    // using simple "constructors", like newNodeRectangle(), and we then
    // could hand this shapes to other methods, like getEdgeEnds(), when
    // we need to figure things out about the nodes or edges. When we're
    // ready to "output" the node or edge we could hand the Shape to a
    // method that adds the appropriate commands to a StringBuffer, much
    // like we currently do.
    //

    private static double  ARROWTIP_ANGLE = Math.PI/6;
    private static double  ARROWTIP_RADIUS = 72.0/8;
    private static double  ARROWEDGE_COS = Math.cos(ARROWTIP_ANGLE/2);
    private static double  ARROWEDGE_SIN = Math.sin(ARROWTIP_ANGLE/2);

    //
    // Node width or height increases by twice the appropriate padding,
    // which is mostly to accommodate a centered label.
    //

    private static double  NODEPADDING_HORIZONTAL = 72.0/8;
    private static double  NODEPADDING_VERTICAL = 0;

    ///////////////////////////////////
    //
    // GraphLayout Methods
    //
    ///////////////////////////////////

    static GraphRecord[]
    buildGraphRecords(SwingJGraphPlot owner, DataRecord records[], Object model, YoixObject arg) {

	GraphRecord  graphrecords[] = null;
	int          length;
	int          n;

	if (records != null && (length = records.length) > 0) {
	    graphrecords = new GraphRecord[length];
	    for (n = 0; n < length; n++)
		graphrecords[n] = new GraphRecord(owner, records[n], n);
	    YoixMiscQsort.sort(graphrecords, 0);
	    if (model instanceof Integer) {
		switch (((Integer)model).intValue()) {
		    case 0:
		    default:
			buildSweepLayoutDefault(graphrecords, arg);
			break;
		}
	    } else {
		//
		// Eventually will do more...
		//
		buildSweepLayoutDefault(graphrecords, arg);
	    }
	}
	return(graphrecords);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static void
    addEdgeDecorations(StringBuffer sbuf, int coords[], boolean tip) {

	int  length;
	int  n;

	if (coords != null) {
	    if ((length = coords.length) > 0) {
		if (sbuf.length() > 0)
		    sbuf.append(' ');
		sbuf.append("w ");
		sbuf.append(tip ? 1 : 2);
		sbuf.append(" P ");
		sbuf.append(length/2);
		for (n = 0; n < length; n++) {
		    sbuf.append(' ');
		    sbuf.append(coords[n]);
		}
	    }
	}
    }


    private static void
    addHexColor(StringBuffer sbuf, String text, boolean fill) {

	if (text != null && text.length() > 0) {
	    if (sbuf.length() > 0)
		sbuf.append(' ');
	    sbuf.append(fill ? "C " : "c ");
	    sbuf.append(text.length()+1);
	    sbuf.append(" -#");
	    sbuf.append(text);
	}
    }


    private static void
    addLabel(StringBuffer sbuf, String text, int x, int y, int justification, Rectangle2D bounds) {

	if (text != null && text.length() > 0) {
	    if (sbuf.length() > 0)
		sbuf.append(' ');
	    sbuf.append("T ");
	    sbuf.append(x);
	    sbuf.append(' ');
	    sbuf.append(y + (int)(Math.round(Math.abs(bounds.getY()) - bounds.getHeight()/2)));
	    sbuf.append(' ');
	    sbuf.append(justification);
	    sbuf.append(' ');
	    sbuf.append(0);
	    sbuf.append(' ');
	    sbuf.append(text.length());
	    sbuf.append(" -");
	    sbuf.append(text);
	}
    }


    private static void
    addTooltip(StringBuffer sbuf, String text) {

	if (text != null && text.length() > 0) {
	    if (sbuf.length() > 0)
		sbuf.append(' ');
	    sbuf.append("t ");
	    sbuf.append(text.length());
	    sbuf.append(" -");
	    sbuf.append(text);
	}
    }


    private static void
    buildSweepLayoutDefault(GraphRecord graphrecords[], YoixObject arg) {

	StringBuffer  sbuf;
	Rectangle2D   bounds;
	Rectangle2D   emptybounds;
	GraphRecord   neighbor;
	Dimension     screensize;
	boolean       hasarg = false;
	boolean       newline;
	Object        distpair[];
	Object        primary_key;
	Object        secondary_key;
	String        name;
	String        text;
	String        tooltip;
	double        darg;
	double        progress[] = null;
	double        width_max;
	double        height_max;
	double        distance;
	double        distance_min;
	double        distance_max;
	double        distance_spread;
	double        period = -1;
	double        widthratio = 0.95;
	int           iarg;
	int           edge_coords[];
	int           tip_coords[];
	int           tail_coords[];
	int           line_coords[] = null;
	int           nodecount;
	int           direction;
	int           delta;
	int           hgap;
	int           vgap;
	int           width;
	int           height;
	int           lastwidth;
	int           lastheight;
	int           leftmargin;
	int           rightmargin;
	int           x;
	int           y;
	int           lastx;
	int           lasty;
	int           nextx;
	int           length;
	int           type = 0;
	int           n;

	if (arg != null) {
	    //
	    // Start very restrictive, we can generalize later. For now
	    // we also ignore arg when not right.
	    //
	    if (arg.isArray() && arg.sizeof()%2 == 0) {
		for (n = 0; n < arg.sizeof(); n += 2) {
		    if (arg.getObject(n).isInteger() && arg.getObject(n+1).isNumber()) {
			iarg = arg.getInt(n, 0);
			darg = arg.getDouble(n+1, -1);
			switch (iarg) {
			    case SWEEPGRAPH_MARKERS:
				if (darg > 0) {
				    type = iarg;
				    period = darg;
				    hasarg = true;
				    progress = new double[] {0, 0};
				    line_coords = new int[4];
				}
				break;

			    case SWEEPGRAPH_WIDTHRATIO:
				if (darg > 0 && darg < 1)
				    widthratio = darg;
				break;
			}
		    }
		}
	    }
	}

	if ((length = graphrecords.length) > 0) {
	    emptybounds = new Rectangle2D.Double(0, 0, 2, 2);
	    width_max = 0;
	    height_max = 0;
	    distance_min = Double.POSITIVE_INFINITY;
	    distance_max = 0;
	    neighbor = null;
	    for (n = 0; n < length; n++) {
		bounds = graphrecords[n].getActiveLabelBounds(emptybounds);
		if (bounds.getWidth() > width_max)
		    width_max = bounds.getWidth();
		if (bounds.getHeight() > height_max)
		    height_max = bounds.getHeight();
		if ((distance = graphrecords[n].getDistance(neighbor, 0, 0)) > 0) {
		    if (distance < distance_min)
			distance_min = distance;
		    if (distance > distance_max)
			distance_max = distance;
		}
		neighbor = graphrecords[n];
	    }

	    width_max = Math.ceil(width_max + 2*NODEPADDING_HORIZONTAL);
	    height_max = Math.ceil(height_max + 2*NODEPADDING_VERTICAL);
	    distance_spread = (distance_min < Double.POSITIVE_INFINITY) ? (distance_max - distance_min) : 0;
	    hgap = (int)Math.ceil(2*height_max);
	    vgap = (int)Math.ceil(2*height_max);
	    x = 0;
	    y = 0;

	    screensize = YoixAWTToolkit.getScreenSize();
	    leftmargin = 0;
	    rightmargin = (int)Math.max(widthratio*screensize.width - 2*hgap, width_max + hgap);

	    sbuf = new StringBuffer();
	    edge_coords = new int[4];

	    for (n = 0; n < length; ) {
		lastwidth = 0;
		lastheight = 0;
		lastx = 0;
		lasty = 0;
		direction = 1;
		newline = false;
		neighbor = null;
		nodecount = 0;

		if ((primary_key = graphrecords[n].getPrimaryKey()) != null) {
		    text = primary_key.toString();
		    bounds = graphrecords[n].getLabelBounds(text);
		    width = (int)Math.ceil(bounds.getWidth()/2 + NODEPADDING_HORIZONTAL);
		    height = (int)height_max;
		    x = leftmargin - (width + hgap);
		    name = "S" + n;			// dummy start node name
		    startNodeEllipse(sbuf, name, x, y, width, height, false);
		    addLabel(sbuf, text, x, y, 0, bounds);
		    addTooltip(sbuf, graphrecords[n].getPrimaryTooltip());
		    graphrecords[n].savePassiveElement(name, sbuf);
		    lastx = x;
		    lasty = y;
		    lastwidth = width;
		    lastheight = height;
		    nodecount++;
		}

		while (n < length && compareKeys(primary_key, graphrecords[n].getPrimaryKey())) {
		    if (neighbor != null) {
			if ((distance = graphrecords[n].getDistance(neighbor, 0, 0)) > 0) {
			    if (distance_spread > 0)
				delta = (int)Math.ceil(2*hgap*(1.0 - (distance_max - distance)/distance_max));
			    else delta = 0;
			} else delta = -hgap/2;
		    } else delta = 0;

		    bounds = graphrecords[n].getActiveLabelBounds(emptybounds);
		    width = (int)Math.ceil(bounds.getWidth()/2 + NODEPADDING_HORIZONTAL);
		    height = (int)height_max;
		    nextx = newline ? x : x + direction*(width + lastwidth + hgap + delta);
		    if (newline || (direction > 0 && nextx + width <= rightmargin) || (direction < 0 && nextx - width >= leftmargin)) {
			x = nextx;
			if (nodecount > 0) {
			    if (newline) {
				edge_coords[0] = x;
				edge_coords[1] = lasty + lastheight;
				edge_coords[2] = x;
				edge_coords[3] = y - height;
			    } else {
				if (direction > 0) {
				    edge_coords[0] = lastx + lastwidth;
				    edge_coords[1] = lasty;
				    edge_coords[2] = x - width;
				    edge_coords[3] = y;
				} else {
				    edge_coords[0] = lastx - lastwidth;
				    edge_coords[1] = lasty;
				    edge_coords[2] = x + width;
				    edge_coords[3] = y;
				}
			    }
			    if (hasarg)
				System.arraycopy(edge_coords, 0, line_coords, 0, edge_coords.length);

			    //
			    // Fill tip_coords and tail_coords before the
			    // the edge is drawn because endpoints of the
			    // edge may be moved slightly when decorations
			    // information is calculated.
			    //

			    tooltip = null;
			    tip_coords = null;
			    tail_coords = null;

			    distpair = graphrecords[n].getDistancePair(neighbor, Double.POSITIVE_INFINITY);
			    distance = ((Double)distpair[0]).doubleValue();
			    if (distance > 0) {
				tip_coords = getArrowCoords(edge_coords, true);
				if (distance != Double.POSITIVE_INFINITY)
				    tooltip = (String)distpair[1];
			    } else if (distance == 0)
				tooltip = (String)distpair[1];

			    name = "E" + n;		// dummy edge name
			    startEdgeLine(sbuf, name, edge_coords);
			    addEdgeDecorations(sbuf, tip_coords, true);
			    addEdgeDecorations(sbuf, tail_coords, false);
			    addTooltip(sbuf, tooltip);

			    if (hasarg)
				name = processLayoutArgDefault(graphrecords, n, sbuf, name, type, period, progress, distance, line_coords, x, y, width, height, newline);

			    graphrecords[n].savePassiveElement(name, sbuf);
			}


			name = "N" + n;			// dummy node name
			startNodeRectangle(sbuf, name, x, y, width, height, true);
			addLabel(sbuf, graphrecords[n].getActiveLabel(), x, y, 0, bounds);
			addHexColor(sbuf, graphrecords[n].getRecordHexColor(), true);
			addTooltip(sbuf, graphrecords[n].getSecondaryTooltip());
			graphrecords[n].saveActiveElement(name, sbuf);

			lastx = x;
			lasty = y;
			lastwidth = width;
			lastheight = height;
			newline = false;
			neighbor = graphrecords[n];
			nodecount++;
			n++;
		    } else {
			y += 2*height_max + vgap + delta;
			direction *= -1;
			newline = true;
		    }
		}
		y += 2*height_max + vgap;
	    }
	}
    }


    private static boolean
    compareKeys(Object key1, Object key2) {

	boolean  result;

	if (key1 != key2) {
	    if (key1 != null)
		result = key1.equals(key2);
	    else result = key2.equals(key1);
	} else result = true;

	return(result);
    }


    private static int[]
    getArrowCoords(int edge_coords[], boolean adjust) {

	double  ds;
	double  sin;
	double  cos;
	int     arrow_coords[];
	int     dx;
	int     dy;

	dx = edge_coords[0] - edge_coords[2];
	dy = edge_coords[1] - edge_coords[3];
	if ((ds = Math.sqrt(dx*dx + dy*dy)) > 0) {
	    sin = dy/ds;
	    cos = dx/ds;
	    arrow_coords = new int[] {
		edge_coords[2],
		edge_coords[3],
		edge_coords[2] + (int)Math.round(ARROWTIP_RADIUS*(cos*ARROWEDGE_COS - sin*ARROWEDGE_SIN)),
		edge_coords[3] + (int)Math.round(ARROWTIP_RADIUS*(cos*ARROWEDGE_SIN + sin*ARROWEDGE_COS)),
		edge_coords[2] + (int)Math.round(ARROWTIP_RADIUS*(cos*ARROWEDGE_COS + sin*ARROWEDGE_SIN)),
		edge_coords[3] + (int)Math.round(ARROWTIP_RADIUS*(sin*ARROWEDGE_COS - cos*ARROWEDGE_SIN))
	    };

	    if (adjust) {
		edge_coords[2] = (int)Math.round((arrow_coords[0] + arrow_coords[2] + arrow_coords[4])/3.0);
		edge_coords[3] = (int)Math.round((arrow_coords[1] + arrow_coords[3] + arrow_coords[5])/3.0);
	    }
	} else arrow_coords = null;

	return(arrow_coords);
    }


    private static int[]
    getEdgeEnds(Shape node1, Shape node2) {

	Rectangle2D  bounds1;
	Rectangle2D  bounds2;
	Point2D      center;
	Point2D      point1;
	Point2D      point2;
	int          coords[] = null;

	//
	// Returns the endpoints of the intersection of the line connecting
	// the centers of the two nodes with the boundaries of those nodes.
	// Uses the center of each node's bounding box as the center of the
	// node, so we'll need an another approach if the nodes that we can
	// draw get complicated.
	//

	if (node1 != null && node2 != null) {
	    if ((bounds1 = node1.getBounds()) != null) {
		if ((bounds2 = node2.getBounds()) != null) {
		    center = new Point2D.Double(bounds2.getCenterX(), bounds2.getCenterY());
		    if ((point1 = YoixMiscGeom.getFirstIntersection(center, node1)) != null) {
			center = new Point2D.Double(bounds1.getCenterX(), bounds1.getCenterY());
			if ((point2 = YoixMiscGeom.getFirstIntersection(center, node2)) != null) {
			    coords = new int[] {
				(int)Math.round(point1.getX()),
				(int)Math.round(point1.getY()),
				(int)Math.round(point2.getX()),
				(int)Math.round(point2.getY())
			    };
			}
		    }
		}
	    }
	}
	return(coords);
    }


    private static String
    processLayoutArgDefault(GraphRecord graphrecords[], int ridx, StringBuffer sbuf, String name, int type, double period, double progress[], double distance, int line_coords[], int x, int y, int width, int height, boolean newline) {

	Rectangle2D  bounds;
	String       label;
	double       periods0;
	double       periods1;
	int          justification;
	int          bb;
	int          periods;
	int          cnt;
	int          cnt2;
	int          cx;
	int          cy;

	//
	// distance < 0  means beginning of graph for a primary key
	// distance == 0 means several events occurring at "same time"
	//
	// Note: in the following we add 0.1 here and there to (generously)
	// correct for the possibility that double precision integer values
	// might be epsilon below their true integer value -- in the end,
	// we work with integer values so adding 0.1 cannot hurt and only
	// help
	//

	if (distance > 0) {
	    periods0 = progress[1] / period;
	    progress[1] += distance;
	    periods1 = progress[1] / period;
	    periods = (int)(Math.floor(periods1) - Math.floor(periods0) + 0.1);
	    if (periods > 0) {
		cnt = (int)Math.floor(progress[0] + 1.1);
		if (periods > 1) {
		    cnt2 = (int)Math.floor(progress[0] + periods + 0.1);
		    label = "" + cnt + "-" + cnt2;
		    cnt = cnt2;
		} else label = "" + cnt;
		progress[0] = cnt;

		bounds = graphrecords[ridx].getLabelBounds(label);

		bb = (int)Math.round(0.2 * (bounds.getHeight() - bounds.getY()));

		if (newline) {
		    cx = line_coords[0];
		    cy = (line_coords[1] + line_coords[3]) / 2;
		    cx -= bb;
		    justification = 1;
		} else {
		    cx = (line_coords[0] + line_coords[2]) / 2;
		    cy = line_coords[1];
		    cy += (int)((bounds.getHeight() - bounds.getY())/2.0);
		    justification = 0;
		}
		sbuf.append(" <");
		addLabel(sbuf, label, cx, cy, justification, bounds);
		sbuf.append(" >");
	    }
	} else if (distance < 0)
	    progress[0] = progress[1] = 0;	// reset
	return(name);
    }


    private static void
    startEdgeLine(StringBuffer sbuf, String name, int coords[]) {

	int  length;
	int  n;

	sbuf.setLength(0);
	if (name != null && coords != null) {
	    if ((length = coords.length) > 0) {
		sbuf.append(name);
		sbuf.append(' ');
		sbuf.append(2);			// edge type
		sbuf.append(" L ");
		sbuf.append(length/2);
		for (n = 0; n < length; n++) {
		    sbuf.append(' ');
		    sbuf.append(coords[n]);
		}
	    }
	}
    }


    private static void
    startNodeEllipse(StringBuffer sbuf, String name, int x, int y, int width, int height, boolean filled) {

	sbuf.setLength(0);
	if (name != null) {
	    sbuf.append(name);
	    sbuf.append(' ');
	    sbuf.append(1);			// node type
	    sbuf.append(filled ? " E " : " e ");
	    sbuf.append(x);
	    sbuf.append(' ');
	    sbuf.append(y);
	    sbuf.append(' ');
	    sbuf.append(width);
	    sbuf.append(' ');
	    sbuf.append(height);
	}
    }


    private static void
    startNodeRectangle(StringBuffer sbuf, String name, int x, int y, int width, int height, boolean filled) {

	sbuf.setLength(0);
	if (name != null) {
	    sbuf.append(name);
	    sbuf.append(' ');
	    sbuf.append(1);			// node type
	    sbuf.append(filled ? " P 2 " : " p 2 ");
	    sbuf.append(x);
	    sbuf.append(' ');
	    sbuf.append(y);
	    sbuf.append(' ');
	    sbuf.append(width);
	    sbuf.append(' ');
	    sbuf.append(height);
	}
    }
}

