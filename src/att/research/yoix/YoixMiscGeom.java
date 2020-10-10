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

public abstract
class YoixMiscGeom

    implements YoixAPI,
	       YoixConstants,
	       YoixConstantsGraphics

{

    private static final float  DEFAULTWIDTH = 0.1f;

    private static final BasicStroke DEFAULTSTROKE = new BasicStroke(
	DEFAULTWIDTH,
	BasicStroke.CAP_BUTT,
	BasicStroke.JOIN_MITER
    );

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public static String
    dumpPath(GeneralPath path) {

	return(dumpPath(path, null, null));
    }


    public static String
    dumpPath(GeneralPath path, String prefix) {

	return(dumpPath(path, prefix, null));
    }


    public static String
    dumpPath(GeneralPath path, String prefix, String suffix) {

	PathIterator  pi;
	StringBuffer  sb = new StringBuffer();
	double        coords[];
	int           type;

	if (prefix == null)
	    prefix = "";
	if (suffix == null)
	    suffix = NL;

	if (path != null) {
	    coords = new double[6];
	    pi = path.getPathIterator(null);
	    while(!pi.isDone()) {
		switch(type = pi.currentSegment(coords)) {
		    case PathIterator.SEG_CLOSE: 
			sb.append(prefix);
			sb.append("closepath();");
			sb.append(suffix);
			break;

		    case PathIterator.SEG_CUBICTO: 
			sb.append(prefix);
			sb.append("cubicto(");
			sb.append(coords[0]);
			sb.append(",");
			sb.append(coords[1]);
			sb.append(",");
			sb.append(coords[2]);
			sb.append(",");
			sb.append(coords[3]);
			sb.append(",");
			sb.append(coords[4]);
			sb.append(",");
			sb.append(coords[5]);
			sb.append(");");
			sb.append(suffix);
			break;

		    case PathIterator.SEG_LINETO: 
			sb.append(prefix);
			sb.append("lineto(");
			sb.append(coords[0]);
			sb.append(",");
			sb.append(coords[1]);
			sb.append(");");
			sb.append(suffix);
			break;

		    case PathIterator.SEG_MOVETO: 
			sb.append(prefix);
			sb.append("moveto(");
			sb.append(coords[0]);
			sb.append(",");
			sb.append(coords[1]);
			sb.append(");");
			sb.append(suffix);
			break;

		    case PathIterator.SEG_QUADTO: 
			sb.append(prefix);
			sb.append("quadto(");
			sb.append(coords[0]);
			sb.append(",");
			sb.append(coords[1]);
			sb.append(",");
			sb.append(coords[2]);
			sb.append(",");
			sb.append(coords[3]);
			sb.append(");");
			sb.append(suffix);
			break;
		}
		pi.next();
	    }
	}

	return(sb.toString());
    }


    public static Point2D
    getAveragePoint(GeneralPath path) {

	PathIterator  iterator;
	double        sum[] = null;
	double        coords[];
	int           count;
	int           total;
	int           n;

	//
	// Computes the average of the x an y coordinates stored in path
	// and returns the result in a Point2D (or null). It's easy to
	// calculate and may sometimes be more useful than the center
	// path's bounding box, particularly for the simple shapes that
	// are often used by the head and tail of edges in a graph. This
	// should not be confused with the centroid or center of gravity
	// of the shape represented by path!!
	//

	total = 0;
	sum = new double[] {0, 0};

	if (path != null) {
	    coords = new double[6];
	    iterator = path.getPathIterator(new AffineTransform());
	    for (; iterator.isDone() == false; iterator.next()) {
		switch (iterator.currentSegment(coords)) {
		    case PathIterator.SEG_LINETO:
		    case PathIterator.SEG_MOVETO:
			count = 2;
			break;

		    case PathIterator.SEG_QUADTO:
			count = 4;
			break;

		    case PathIterator.SEG_CUBICTO:
			count = 6;
			break;

		    case PathIterator.SEG_CLOSE:
			count = 0;
			break;

		    default:
			count = 0;
			break;
		}
		for (n = 0; n < count; ) {
		    sum[0] += coords[n++];
		    sum[1] += coords[n++];
		};
		total += count/2;
	    }
	}

	return(total > 0 ?  new Point2D.Double(sum[0]/total, sum[1]/total) : null);
    }


    public static Line2D
    getConnectingLine(GeneralPath path, int direction) {

	Line2D  line = null;
	double  tangents[];

	//
	// Returns a line that connects the first and last points in path.
	// A non-negative direction means the line goes from the first to
	// the last point in path, while a negative value means the line
	// goes from the last to the first point in path.
	//

	if ((tangents = getTangents(path)) != null) {
	    if (direction >= 0) {
		line = new Line2D.Double(
		    tangents[0], tangents[1],
		    tangents[6], tangents[7]
		);
	    } else {
		line = new Line2D.Double(
		    tangents[6], tangents[7],
		    tangents[0], tangents[1]
		);
	    }
	}
	return(line);
    }


    public static Point2D[]
    getEndPoints(GeneralPath path) {
 
	Point2D  points[] = null;
	double   tangents[];

	if ((tangents = getTangents(path)) != null) {
	    points = new Point2D[] {
		new Point2D.Double(tangents[0], tangents[1]),
		new Point2D.Double(tangents[6], tangents[7])
	    };
	}
	return(points);
    }


    public static Line2D[]
    getEndTangents(GeneralPath path, int direction) {

	Line2D  lines[] = null;
	double  tangents[];

	if ((tangents = getTangents(path)) != null) {
	    if (direction == 0) {
		lines = new Line2D[] {
		    new Line2D.Double(tangents[0], tangents[1], tangents[2], tangents[3]),
		    new Line2D.Double(tangents[4], tangents[5], tangents[6], tangents[7])
		};
	    } else if (direction > 0) {
		lines = new Line2D[] {
		    new Line2D.Double(tangents[0], tangents[1], tangents[2], tangents[3]),
		    new Line2D.Double(tangents[6], tangents[7], tangents[4], tangents[5])
		};
	    } else {
		lines = new Line2D[] {
		    new Line2D.Double(tangents[2], tangents[3], tangents[0], tangents[1]),
		    new Line2D.Double(tangents[4], tangents[5], tangents[6], tangents[7])
		};
	    }
	    if (lines[0].getP1().equals(lines[0].getP2()))
		lines[0] = null;
	    if (lines[1].getP1().equals(lines[1].getP2()))
		lines[1] = null;
	}
	return(lines);
    }


    public static Point2D
    getFirstIntersection(Point2D point, Shape shape) {

	return(getFirstIntersection(point, shape, null));
    }


    public static Point2D
    getFirstIntersection(Point2D point, Shape shape, Point2D intersection) {

	Rectangle2D  bounds;

	if (point != null && shape != null) {
	    if ((bounds = shape.getBounds()) != null) {
		intersection = getFirstIntersection(
		    new Line2D.Double(
			point.getX(), point.getY(),
			bounds.getCenterX(), bounds.getCenterY()
		    ),
		    new Area(shape),
		    intersection
		);
	    }
	}
	return(intersection);
    }


    public static Point2D
    getFirstIntersection(Line2D line, Shape shape) {

	return(getFirstIntersection(line, shape, null));
    }


    public static Point2D
    getFirstIntersection(Line2D line, Shape shape, Point2D intersection) {

	Rectangle2D  bounds;
	double       x1;
	double       x2;
	double       y1;
	double       y2;
	double       dx;
	double       dy;
	double       value;
	double       t;

	//
	// Extends the line, when necessary, by moving the second end point
	// far enough (conceptually to infinity) to guarantee that we will
	// find intersections of the line with shape (if they exist). The
	// line's first end point is never moved and the second end point
	// is never moved closer to the first end point. If the "extended"
	// line and shape intersect we return the point in the intersection
	// that's closest to the line's first end point.
	//

	if (line != null && shape != null) {
	    if ((bounds = shape.getBounds2D()) != null) {
		t = 1.0;
		x1 = line.getX1();
		y1 = line.getY1();
		x2 = line.getX2();
		y2 = line.getY2();
		dx = x2 - x1;
		dy = y2 - y1;
		if (dy != 0) {
		    if ((value = (bounds.getMinY() - y1)/dy) > t)
			t = value;
		    if ((value = (bounds.getMaxY() - y1)/dy) > t)
			t = value;
		}
		if (dx != 0) {
		    if ((value = (bounds.getMinX() - x1)/dx) > t)
			t = value;
		    if ((value = (bounds.getMaxX() - x1)/dx) > t)
			t = value;
		}
		x2 = dx*t + x1;
		y2 = dy*t + y1;
		intersection = getFirstIntersection(
		    new Line2D.Double(x1, y1, x2, y2),
		    new Area(shape)
		);
	    }
	}
	return(intersection);
    }


    public static int
    incode(Rectangle2D rect, Point2D point) {

	double  px;
	double  py;
	double  rmw;
	double  rmh;
	int     incode = 0;

	if (rect != null && point != null && rect.contains(point)) {
	    px = point.getX() - rect.getX();
	    py = point.getY() - rect.getY();

	    rmw = 0.5 * rect.getWidth();
	    rmh = 0.5 * rect.getHeight();

	    // use Rectangle2D outcode int values
	    // we won't worry about pure left, right,
	    // top, bottom or center (i.e., equality)
	    //
	    if (px <= rmw)
		incode |= Rectangle2D.OUT_LEFT;
	    else incode |= Rectangle2D.OUT_RIGHT;
	    if (py <= rmh)
		incode |= Rectangle2D.OUT_TOP;
	    else incode |= Rectangle2D.OUT_BOTTOM;
	}

	return(incode);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static Point2D
    getFirstIntersection(Line2D line, Area area) {

	PathIterator  iterator;
	GeneralPath   path;
	double        closest[] = null;
	double        coords[];
	double        minimum;
	double        value;
	double        x;
	double        y;
	int           index;

	//
	// Intersects line with area and returns the point closest to
	// the line's first end point in that intersection or null if
	// line and area don't intersect. The area is changed by this
	// method!!!
	//

	if (line != null && area != null) {
	    area.intersect(new Area(DEFAULTSTROKE.createStrokedShape(line)));
	    if (area.isEmpty() == false) {
		x = line.getX1();
		y = line.getY1();
		coords = new double[6];
		path = new GeneralPath(area);
		iterator = path.getPathIterator(new AffineTransform());
		minimum = Double.POSITIVE_INFINITY;
		for (; iterator.isDone() == false; iterator.next()) {
		    switch (iterator.currentSegment(coords)) {
			case PathIterator.SEG_LINETO:
			case PathIterator.SEG_MOVETO:
			    index = 0;
			    break;

			case PathIterator.SEG_QUADTO:	// can this happen?
			    index = 2;
			    break;

			case PathIterator.SEG_CUBICTO:	// can this happen?
			    index = 4;
			    break;

			default:
			    index = -1;
			    break;
		    }
		    if (index >= 0) {
			value = Point2D.distanceSq(x, y, coords[index], coords[index+1]);
			if (value < minimum) {
			    if (closest == null)
				closest = new double[2];
			    closest[0] = coords[index];
			    closest[1] = coords[index+1];
			    minimum = value;
			}
		    }
		}
	    }
	}
	return(closest != null ? new Point2D.Double(closest[0], closest[1]) : null);
    }


    private static double[]
    getTangents(GeneralPath path) {

	PathIterator  iterator;
	double        tangents[] = null;
	double        coords[];
	double        current[];
	double        moveto[];
	int           next;
	int           n;

	//
	// Returns an array of eight numbers (or null) that represent the
	// lines tangent to path at its start and end.
	//

	if (path != null) {
	    current = new double[2];
	    moveto = new double[2];
	    coords = new double[6];
	    iterator = path.getPathIterator(new AffineTransform());
	    for (; iterator.isDone() == false; iterator.next()) {
		switch (iterator.currentSegment(coords)) {
		    case PathIterator.SEG_LINETO:
			next = 0;
			break;

		    case PathIterator.SEG_MOVETO:
			next = -1;
			moveto[0] = coords[0];
			moveto[1] = coords[1];
			current[0] = moveto[0];
			current[1] = moveto[1];
			break;

		    case PathIterator.SEG_QUADTO:
			next = 2;
			break;

		    case PathIterator.SEG_CUBICTO:
			next = 4;
			break;

		    case PathIterator.SEG_CLOSE:
			next = 0;
			coords[0] = moveto[0];
			coords[1] = moveto[1];
			break;

		    default:
			next = -1;
			break;
		}
		if (next >= 0) {
		    if (tangents == null) {
			tangents = new double[8];
			tangents[0] = current[0];
			tangents[1] = current[1];
			tangents[2] = coords[0];
			tangents[3] = coords[1];
		    }
		    if (next > 1) {
			tangents[4] = coords[next-2];
			tangents[5] = coords[next-1];
		    } else {
			tangents[4] = current[0];
			tangents[5] = current[1];
		    }
		    tangents[6] = coords[next];
		    tangents[7] = coords[next+1];
		    current[0] = tangents[6];
		    current[1] = tangents[7];
		}
	    }
	}
	return(tangents);
    }
}

