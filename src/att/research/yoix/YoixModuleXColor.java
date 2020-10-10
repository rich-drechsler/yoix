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
import java.awt.Color;

abstract
class YoixModuleXColor extends YoixModule

{

    static String  $MODULENAME = M_XCOLOR;

    static String  $YOIXCONSTANTSXCOLOR = YOIXPACKAGE + ".YoixConstantsXColor";

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       null,                 "762",               $LIST,      $RORO, $MODULENAME,
       $YOIXCONSTANTSXCOLOR, Color.black,         $READCLASS, $LR__, null,
    };

    static Object  extracted[] = {
	$MODULENAME,
    };

    ///////////////////////////////////
    //
    // YoixModuleXColor Methods
    //
    ///////////////////////////////////

    static void
    addColor() {

	//
	// This method was public and named loaded() prior to 3/10/2010,
	// which meant it was called by the Yoix loader after the module
	// loading dirty work was finished. Not sure why it was done, but
	// it meant an expansion of the Color dictionary whenever this
	// module was loaded, which was behavior that was really hard to
	// explain. With the change the only way to update Color is for
	// the script to call the addColor() builtin with no arguments.
	// 

	if (extracted[0] instanceof YoixObject) {
	    YoixRegistryColor.setRegistry(null); // initializes
	    YoixRegistryColor.addColor((YoixObject)extracted[0]);
	    YoixRegistryColor.setXLoaded();
	}
    }
}

