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
import java.util.*;

abstract
class YoixGraphBase

    implements YoixInterfaceCloneable

{

    YoixBodyGraphObserver  obs; 	// the observer
    YoixGraphElement       grf; 	// the observed

    //
    // Members are also kept in a doubly linked list that's used
    // to keep printing consistent.
    //

    YoixGraphBase  next = null;
    YoixGraphBase  prev = null;

    double  x = 0;
    double  y = 0;

    boolean  selected = true;
    boolean  repaint = false;
    boolean  pressed = false;
    boolean  highlight = false;
    boolean  loaded = false;
    Color    color = Color.white;
    Font     font = null;
    int      marker = -1;		// used to avoid double visits

    //
    // Low level stuff filled when loading
    //

    GeneralPath      shape = null;
    GeneralPath      label_shape = null;
    Rectangle2D      bounds = null;
    Rectangle2D      label_bounds = null;
    boolean          filled = true;
    float            alpha = 1; // not used in a meaningful way yet
    boolean          invis = false;
    Point2D          label_pts[] = null;
    String           labels[] = null;
    String           name = null;
    double           dasharray[] = null;
    Shape            draw_shape = null;
    Color            assigned_color = null;
    Color            font_color = null;
    int              label_just[] = null;
    int              type = 0;
    int              linewidth = 1;

    //
    // This shape, when not null and properly initialized, will be
    // transformed and then intersected with the clipping shape that
    // is used to draw this element.
    //

    Area  clippedarea = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixGraphBase() {

	this.obs = null;
	this.next = this;
	this.prev = this;
    }

    ///////////////////////////////////
    //
    // YoixInterfaceCloneable Methods
    //
    ///////////////////////////////////

    public Object
    clone() {

	YoixGraphBase obj = null;

	try {
	    obj = (YoixGraphBase)(super.clone());
	}
	catch(CloneNotSupportedException e) {
	    // cannot happen
	    YoixConstants.VM.abort(YoixConstants.INTERNALERROR);
	}

	if (this.next == this)
	    obj.next = obj;

	if (this.prev == this)
	    obj.prev = obj;

	return(obj);
    }


    public Object
    copy(HashMap copied) {

	return(clone());
    }

    ///////////////////////////////////
    //
    // YoixGraphBase Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	obs = null;
	next = null;
    }


    void
    initElem(YoixGraphElement elem) {

    }


    void
    layout() {

    }
}

