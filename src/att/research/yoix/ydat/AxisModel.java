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
import att.research.yoix.*;

public abstract
class AxisModel

    implements Constants

{

    protected DataAxis  axis = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    AxisModel(DataAxis axis) {

	this.axis = axis;
    }

    ///////////////////////////////////
    //
    // AxisModel Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	this.axis = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    public void
    setLabels(YoixObject obj) {

    }


    public void
    setTicks(YoixObject obj) {

    }


    public void
    syncToAxisLimits() {

    }


    public void
    syncToEnds(double ends[]) {

    }

    ///////////////////////////////////
    //
    // Abstract Methods
    //
    ///////////////////////////////////

    abstract void
    drawLabels(int low, int high, Graphics g);

    abstract void
    drawTicks(int low, int high, Graphics g);

    abstract String[]
    generateLabels();

    abstract void
    makeTicks();
}

