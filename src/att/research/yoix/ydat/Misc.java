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
import java.util.*;
import att.research.yoix.*;

public abstract
class Misc

    implements Constants

{

    //
    // An arbitrary transformation that can be used to map latitude and
    // longitude values in radians to (x, y) coordinates appropriate for
    // display by GraphPlot. Flipping the y-axis is part of the job but
    // just as important is scaling radians to values that can be drawn
    // properly by Java2D's GeneralPath, which currently uses floats to
    // store path coordinates.
    //
    // If there's an underlying map then the transformation that's used
    // by mercatorToYDAT() has to be coordinated with that map. In that
    // case it's unlikely that our default transformation is the one to
    // use, however the appropriate transformation can be supplied as an
    // argument in the mercatorToYDAT() call. Incidentally, we pick this
    // transformation because it agreed with the one used in a world map
    // that we built several years ago and still occasionally use.
    //

    private static AffineTransform  MAP_TRANSFORM = new AffineTransform(18000.0/Math.PI, 0, 0, -18000.0/Math.PI, 18000, -18000);

    ///////////////////////////////////
    //
    // Misc Methods
    //
    ///////////////////////////////////

    static int
    compareOctets(String left, String right) {

	int  octet1;
	int  octet2;
	int  index;
	int  result = 0;

	//
	// Written very quickly - it definitely needs some work!!
	//

	while (result == 0 && left != null && right != null) {
	    if ((index = left.indexOf('.')) > 0) {
		octet1 = YoixMake.javaInt(left.substring(0, index), -1);
		left = left.substring(index+1);
	    } else {
		octet1 = YoixMake.javaInt(left, 0); 
		left = null;
	    }
	    if ((index = right.indexOf('.')) > 0) {
		octet2 = YoixMake.javaInt(right.substring(0, index), -2);
		right = right.substring(index+1);
	    } else {
		octet2 = YoixMake.javaInt(right, 0);
		right = null;
	    }
	    result = octet1 - octet2;
	}
	return(result);
    }


    static Object[]
    makeGenerator(YoixObject obj, DataManager manager) {

	YoixObject  element;
	Object      generator[];
	String      name;
	int         index;
	int         type;
	int         n;

	generator = null;

	if (obj.isArray() && obj.sizeof() > 0) {
	    n = obj.offset();
	    if ((element = obj.getObject(n++)) != null && element.isInteger()) {
		switch (type = element.intValue()) {
		    case COUNTER_GENERATOR:
		    case OVERLAP_GENERATOR:
			if (obj.sizeof() == 2) {
			    if ((name = obj.getString(n)) != null) {
				if ((index = manager.getTagIndex(name)) >= 0) {
				    generator = new Object[2];
				    generator[0] = new Integer(type);
				    generator[1] = new Integer(index);
				}
			    }
			}
			break;

		    default:
			break;
		}
	    }
	}
	return(generator);
    }


    static String
    mercatorToYDAT(String source, int left, int delim, int right, int fields, String command, double width, double height, boolean ismark, Object matrix) {

	AffineTransform  transform;
	ArrayList        tokens;
	ArrayList        paths;
	HashMap          map;
	String           path;
	String           result = null;
	String           body;
	String           delimiter;
	String           marker;
	String           name;
	double           origin[];
	double           sides[];
	int              type;
	int              n;

	//
	// Applies the mercator projection to latitude and longitude pairs
	// extracted from the source string and outputs the description of
	// a shape near that point in a format that can be processed by the
	// SwingJGraphPlot.separateText() method.
	//
	// NOTE - support for several marks in the source string was finally
	// implemented properly (on 4/23/09) and involved low level changes
	// in SwingJGraphPlot.java. Right now we use " \0 " as the hardcoded
	// string that separates the marks and SwingJGraphPlot extracts them
	// in separateText(). Don't think the hardcoded string will cause a
	// problem, but we could easily pass it in as an argument, but this
	// is more than sufficient for now. The old implementation pretended
	// to handle multiple marks, but it was a bogus implementation that
	// just appended them all to a single path, which obviously wouldn't
	// be able to accurately mark more than one point.
	//

	if (source != null && fields >= 2) {
	    body = YoixMisc.trim(source, left, right);
	    if (body.length() > 0) {
		name = "";
		transform = MAP_TRANSFORM;
		if (matrix != null) {
		    if (matrix instanceof YoixObject) {
			if (((YoixObject)matrix).isMatrix())
			    transform = ((YoixObject)matrix).getAffineTransform();
		    } else if (matrix instanceof AffineTransform)
			transform = (AffineTransform)matrix;
		}

		if (ismark == false) {
		    sides = new double[] {
			Math.toRadians(width > 0 ? width : 0.030),
			Math.toRadians(height > 0 ? height : 0.030)
		    };
		    transform.deltaTransform(sides, 0, sides, 0, 1);
		    sides[0] = Math.abs(sides[0]);
		    sides[1] = Math.abs(sides[1]);
		} else sides = VM.getDefaultMatrix().dtransform(width > 0 ? width : 72.0/5, height > 0 ? height : 72.0/5);

		type = SwingJGraphPlot.DATA_GRAPH_NODE | (ismark ? SwingJGraphPlot.DATA_MARK : 0);
		delimiter = (delim >= 0 ? String.valueOf((char)delim) : ":");
		command = (command != null && command.length() > 0) ? command : "v";
		paths = new ArrayList();
		map = new HashMap();
		tokens = YoixMisc.split(body, delimiter);
		for (n = 0; n < tokens.size() - 1; n += fields) {
		    origin = new double[] {
			Math.toRadians(YoixMake.javaDouble((String)tokens.get(n), Double.NaN)),
			Math.toRadians(YoixMake.javaDouble((String)tokens.get(n+1), Double.NaN))
		    };
		    if (Double.isNaN(origin[0]) == false && Double.isNaN(origin[1]) == false) {
			origin[1] = 0.5*Math.log(((1 + Math.sin(origin[1]))/(1 - Math.sin(origin[1]))));
			transform.transform(origin, 0, origin, 0, 1);
			if ((marker = parseCommand(command, origin, sides)) != null) {
			    name = tokens.get(n) + delimiter + tokens.get(n+1);
			    if (left >= 0)
				name = (char)left + name;
			    if (right >= 0)
				name = name + (char)right;
			    name.replaceAll("[ \t\n]", "_");
			    path = name + " " + type + " " + marker + " t 0 -";
			    if (map.containsKey(path) == false) {
				map.put(path, Boolean.TRUE);
				if (result == null)
				    result = path;
				else result += " \0 " + path;
			    }
			}
		    }
		}
	    }
	}
	return(result);
    }


    static Color
    pickAdjustedColor(Color color, double adjust, Color reserved[]) {

	double  hue;
	double  saturation;
	double  brightness;
	double  delta;
	float   hsb[];
	int     n;

	//
	// Most of this was arrived at by experimenting and assuming that
	// adjust is a relatively small brightness adjustment. Undoubtedly
	// lots of room for improvement and we probably should throw a few
	// additional colors (e.g., pressedcolor and highlightcolor) into
	// reserved.
	//

	hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
	hue = hsb[0];
	saturation = hsb[1];
	brightness = hsb[2] + adjust;

	if (brightness < 0 || brightness > 1)
	    brightness = hsb[2] - adjust;

	delta = Math.min(.25, 1.0/(reserved.length + 1));

	for (n = 0; n < reserved.length; n++) {
	    if ((color = reserved[n]) != null) {
		hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		if (Math.abs(saturation - hsb[1]) < .1) {
		    if (Math.abs(brightness - hsb[2]) < .1) {
			if (saturation < .5 || Math.abs(hue - hsb[0]) < .1) {
			    if (saturation > 0) {
				saturation = Math.max(1.0, saturation + .5);
				hue += delta;
				hue = hue - Math.floor(hue);
			    } else saturation = .25;
			    reserved[n] = null;		// prevent infinite loops
			    n = 0;			// then try again
			}
		    }
		}
	    }
	}

	return(new Color(Color.HSBtoRGB((float)hue, (float)saturation, (float)brightness)));
    }

    ///////////////////////////////////
    //
    // Misc Methods
    //
    ///////////////////////////////////

    private static AffineTransform
    getUnitSquareTransform(double origin[], double sides[]) {

	AffineTransform  transform;

	transform = new AffineTransform();
	transform.setToTranslation(origin[0], origin[1]);
	transform.scale(sides[0], sides[1]);
	return(transform);
    }


    private static String
    parseCommand(String command, double origin[], double sides[]) {

	AffineTransform  transform;
	boolean          filled;
	String           marker = null;
	String           args[];
	double           coords[];
	char             ch;
	int              count;
	int              n;

	//
	// Eventually expect we'll rewrite this so it's more flexible. One
	// obvious improvement would be to use a HashMap to map the primary
	// argument (the one used by the switch statement) into a constant
	// so we use more than the first character to make decisions. After
	// that we probably want a way to set "attributes" that get handed
	// to the SwingJGraphPlot parser. What's here should be sufficient
	// for now, so any enhancements stuff can wait.
	// 

	if (command != null) {
	    args = command.split(" ");
	    filled = true;
	    for (n = 0; n < args.length; n++) {
		marker = (marker == null) ? "" : marker + " ";
		switch (ch = args[n].charAt(0)) {
		    case 'E':
		    case 'e':
			marker += (filled ? "E " : "e ");
			marker += (float)origin[0] + " " + (float)origin[1] + " " + (float)(sides[0]/2) + " " + (float)(sides[1]/2);
			break;

		    case 'R':
		    case 'r':
			marker += (filled ? "P 2 " : "p 2 ");
			marker += (float)origin[0] + " " + (float)origin[1] + " " + (float)(sides[0]/2) + " " + (float)(sides[1]/2);
			break;

		    case 'V':
		    case 'v':
			marker += (filled ? "P 3 " : "p 3 ");
			marker += (float)origin[0] + " " + (float)origin[1] + " ";
			marker += (float)(origin[0] + sides[0]/2) + " " + (float)(origin[1] - sides[1]) + " ";
			marker += (float)(origin[0] - sides[0]/2) + " " + (float)(origin[1] - sides[1]);
			break;

		    case '^':
			marker += (filled ? "P 3 " : "p 3 ");
			marker += (float)origin[0] + " " + (float)origin[1] + " ";
			marker += (float)(origin[0] + sides[0]/2) + " " + (float)(origin[1] + sides[1]) + " ";
			marker += (float)(origin[0] - sides[0]/2) + " " + (float)(origin[1] + sides[1]);
			break;

		    case '>':
			marker += (filled ? "P 3 " : "p 3 ");
			marker += (float)origin[0] + " " + (float)origin[1] + " ";
			marker += (float)(origin[0] - sides[0]) + " " + (float)(origin[1] - sides[1]/2) + " ";
			marker += (float)(origin[0] - sides[0]) + " " + (float)(origin[1] + sides[1]/2);
			break;

		    case '<':
			marker += (filled ? "P 3 " : "p 3 ");
			marker += (float)origin[0] + " " + (float)origin[1] + " ";
			marker += (float)(origin[0] + sides[0]) + " " + (float)(origin[1] - sides[1]/2) + " ";
			marker += (float)(origin[0] + sides[0]) + " " + (float)(origin[1] + sides[1]/2);
			break;

		    case 'P':		// polygon
		    case 'S':		// shape (outlined by splines)
			if (n++ < args.length - 1) {
			    if ((count = YoixMake.javaInt(args[n++], -1)) > 0) {
				transform = getUnitSquareTransform(origin, sides);
				coords = new double[2];
				//
				// We force count to a value that won't upset the
				// SwingJGraphPlot parsing code and just use the
				// origin for missing points. Hopefully this will
				// eliminate obscure exceptions that can be thrown
				// by the SwingJGraphPlot parser when it encounters
				// something unexpected - remember that parser was
				// originally just designed to handle translated
				// xdot output, which we assumed was well behaved.
				// 
				if (ch == 'S') {
				    count = ((count + 1)/3)*3 + 1;
				    marker += 'B' + " " + count;
				} else {
				    count = Math.max(count, 3);
				    marker += (filled ? 'P' : 'p') + " " + count;
				}
				for (; count > 0; count--, n += 2) {
				    if (n < args.length - 1) {
					coords[0] = YoixMake.javaDouble(args[n], 0);
					coords[1] = YoixMake.javaDouble(args[n+1], 0);
				    } else {
					coords[0] = 0;
					coords[1] = 0;
				    }
				    transform.transform(coords, 0, coords, 0, 1);
				    marker += " " + (float)coords[0] + " " + (float)coords[1];
				}
				if (ch == 'S' && filled)
				    marker += " A 1 filled";
			    }
			}
			break;

		    default:
			marker += (filled ? "E " : "e ");
			marker += (float)origin[0] + " " + (float)origin[1] + " " + (float)(sides[0]/2) + " " + (float)(sides[1]/2);
			break;
		}
	    }
	}
	return(marker);
    }
}

