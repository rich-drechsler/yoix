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

public
class YoixAWTFontMetrics

    implements YoixAPI,
	       YoixConstants

{

    private FontMetrics  fm;

    private int  ascent;
    private int  descent;
    private int  height;
    private int  leading;
    private int  maxadvance;
    private int  maxascent;
    private int  maxdescent;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTFontMetrics(FontMetrics fm) {

	this.fm = fm;

	ascent = fm.getAscent();
	descent = fm.getDescent();
	height = fm.getHeight();
	leading = fm.getLeading();
	maxadvance = fm.getMaxAdvance();
	maxascent = fm.getMaxAscent();
	maxdescent = fm.getMaxDescent();

	if (VM.getBoolean(N_FIXFONTMETRICS)) {
	    ascent = ascent - descent - leading + 1;
	    maxascent = maxascent - maxdescent - leading + 1;
	}
    }

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public final int
    charWidth(char ch) {

	return(fm.charWidth(ch));
    }


    public final int
    getAscent() {

	return(ascent);
    }


    public final int
    getDescent() {

	return(descent);
    }


    public final int
    getHeight() {

	return(height);
    }


    public final int
    getLeading() {

	return(leading);
    }


    public final int
    getMaxAdvance() {

	return(maxadvance);
    }


    public final int
    getMaxAscent() {

	return(maxascent);
    }


    public final int
    getMaxDescent() {

	return(maxdescent);
    }


    public final int
    stringWidth(String str) {

	return(fm.stringWidth(str));
    }

    ///////////////////////////////////
    //
    // YoixAWTFontMetrics Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	fm = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }
}

