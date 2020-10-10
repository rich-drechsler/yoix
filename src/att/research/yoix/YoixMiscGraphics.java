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
class YoixMiscGraphics

    implements YoixAPI,
	       YoixConstants,
	       YoixConstantsGraphics

{

    //
    // This class was added to provide an interface to some of the methods
    // in YoixBodyGraphics that might be useful to custom module code. We
    // haven't tried to be complete and expect there will be additions and
    // changes in future releases.
    //

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public static void
    drawString(YoixObject obj, String str, double x, double y, Graphics2D g) {

	YoixInterfaceFont  font;
	YoixBodyGraphics   graphics;

	if (obj != null && obj.isGraphics()) {
	    graphics = (YoixBodyGraphics)obj.body();
	    if ((font = graphics.getCurrentInterfaceFont()) != null) {
		if (g != null)
		    font.fontDrawString(str, x, y, 1.0, g, graphics);
		else font.fontDrawString(str, x, y, 1.0, graphics);
	    }
	}
    }


    public static Graphics2D
    getGraphics2D(YoixObject obj) {

	return(obj != null && obj.isGraphics() ? ((YoixBodyGraphics)obj.body()).getGraphics2D() : null);
    }


    public static Graphics2D
    getGraphics2D(YoixObject obj, boolean erase) {

	return(obj != null && obj.isGraphics() ? ((YoixBodyGraphics)obj.body()).getGraphics2D(erase) : null);
    }


    public static void
    rectButton(YoixObject obj, double x, double y, double width, double height, double border, int state, Graphics2D g) {

	if (obj != null && obj.isGraphics())
	    ((YoixBodyGraphics)obj.body()).rectButton(x, y, width, height, border, state, g);
    }


    public static void
    rectFill(YoixObject obj, double x, double y, double width, double height, Graphics2D g) {

	if (obj != null && obj.isGraphics())
	    ((YoixBodyGraphics)obj.body()).rectFill(x, y, width, height, g);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////
}

