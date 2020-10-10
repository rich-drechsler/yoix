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
import java.io.*;

class YoixAWTFileDialog extends FileDialog

    implements YoixInterfaceWindow

{

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTFileDialog(Frame parent) {

	super(parent);
	setFilenameFilter(new YoixFilenameFilter("x"));
    }


    YoixAWTFileDialog(Frame parent, String title) {

	super(parent, title);
	setFilenameFilter(new YoixFilenameFilter("x"));
    }


    YoixAWTFileDialog(Frame parent, String title, int mode) {

	super(parent, title, mode);
	setFilenameFilter(new YoixFilenameFilter("x"));
    }

    ///////////////////////////////////
    //
    // YoixInterfaceWindow Methods
    //
    ///////////////////////////////////

    public final void
    setGlassPane(Component pane) {

    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixFilenameFilter implements FilenameFilter {

	String  ending;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixFilenameFilter(String ending) {

	    this.ending = ending;
	}

	///////////////////////////////////
	//
	// YoixAWTFileDialog Methods
	//
	///////////////////////////////////

	public boolean
	accept(File dir, String name) {

	    return(name != null && name.endsWith(ending));
	}
    }
}

