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
import javax.swing.*;
import javax.swing.border.*;

interface YoixConstantsSwing

    extends YoixConstantsJFC

{

    //
    // Icon type names
    //

    static final String  N_DEFAULTICON = "defaulticon";
    static final String  N_DISABLEDICON = "disabledicon";
    static final String  N_DISABLEDSELECTEDICON = "disabledselectedicon";
    static final String  N_PRESSEDICON = "pressedicon";
    static final String  N_ROLLOVERICON = "rollovericon";
    static final String  N_ROLLOVERSELECTEDICON = "rolloverselectedicon";
    static final String  N_SELECTEDICON = "selectedicon";

    //
    // Button types
    //

    static final int  YOIX_STANDARD_BUTTON = 0;
    static final int  YOIX_CHECKBOX_BUTTON = 1;
    static final int  YOIX_RADIO_BUTTON = 2;
    static final int  YOIX_TOGGLE_BUTTON = 3;

    //
    // Additional scrollbar constants that are currently only used with
    // a JScrollPane. They require bit-wise combinability, and must not
    // interfere with existing constants, like YOIX_NONE. Take a look
    // at the "ScrollPolicy" definitions in YoixBodyComponentSwing.java
    // and the constants defined in YoixConstantsJFC.java if you need
    // more info.
    //

    static final int  YOIX_HORIZONTAL_ALWAYS = 1<<20;
    static final int  YOIX_HORIZONTAL_AS_NEEDED = 1<<21;
    static final int  YOIX_HORIZONTAL_NEVER = 1<<22;
    static final int  YOIX_VERTICAL_ALWAYS = 1<<23;
    static final int  YOIX_VERTICAL_AS_NEEDED = 1<<24;
    static final int  YOIX_VERTICAL_NEVER = 1<<25;

    static final int  YOIX_DEFAULT_ICON = 0;

    //
    // Selection modes - carefully chosen so that YOIX_SINGLE_SELECTION
    // maps to 0 (i.e., FALSE) and YOIX_MULTIPLE_INTERVAL_SELECTION maps
    // to 1 (i.e., TRUE). Done primarily so the multiplemode field in a
    // JList can be initialized using TRUE and FALSE, which is the way
    // the field is set in an AWT List. Unfortunately we're still not
    // 100% compatible because users have to hold CTRL to make multiple
    // selections. Also recently deleted YOIX_NO_SELECTION, which was
    // not used but ended up defined in yoix.swing.
    //

    static final int  YOIX_SINGLE_SELECTION = 0;		// == FALSE
    static final int  YOIX_MULTIPLE_INTERVAL_SELECTION = 1;	// == TRUE
    static final int  YOIX_SINGLE_INTERVAL_SELECTION = 2;	// not important

    //
    // Border definitions.
    //

    static final int  YOIX_BEVELED = 0x0001;
    static final int  YOIX_EMPTY = 0x0002;
    static final int  YOIX_ETCHED = 0x0004;
    static final int  YOIX_LINED = 0x0008;
    static final int  YOIX_MATTE = 0x0010;
    static final int  YOIX_LOWERED = 0x0020;
    static final int  YOIX_RAISED = 0x0040;
    static final int  YOIX_ROUNDED = 0x0080;
    static final int  YOIX_SOFTBEVELED = 0x0100;

    static final int  YOIX_TITLED = 0x0200;	// obsolete - for backward compatibility

    //
    // Several convenient combinations that are only used internally.
    //

    static final int  BORDER_TYPE_MASK = YOIX_BEVELED|YOIX_EMPTY|YOIX_ETCHED|YOIX_LINED|YOIX_MATTE|YOIX_SOFTBEVELED;
    static final int  BORDER_BEVEL_MASK = YOIX_LOWERED|YOIX_RAISED;
}

