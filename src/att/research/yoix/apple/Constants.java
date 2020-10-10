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

package att.research.yoix.apple;
import com.apple.eawt.*;
import att.research.yoix.*;

public
interface Constants

    extends YoixConstants

{
    static final String  APPLE_FILEMANAGER = "com.apple.eio.FileManager";

    //
    // Local type names - no reason to make a copy, because the Yoix
    // interpreter won't allow typedict collisions.
    // 

    //static final String  T_AXIS = "Axis";

    //
    // The numbers assigned to these constants are not important except
    // that they must be unique and shouldn't equal values assigned to
    // the automatically generated constants, like JCOMPONENT, that are
    // used in YoixModule.newPointer.
    //

    //static final int  JAXIS = LASTTOKEN + 1;

    //
    // A copy of some official Yoix field names.
    //

    static final String  NY_ADD = N_ADD;
    static final String  NY_ENABLED = N_ENABLED;
    static final String  NY_REMOVE = N_REMOVE;

    //
    // Local field names and associated values.
    //

    static final String  NL_ABOUT = "about";
    static final String  NL_FILENAME = "filename";
    static final String  NL_MOUSELOCATION = "mouselocation";
    static final String  NL_OPENAPPLICATION = "openapplication";
    static final String  NL_OPENFILE = "openfile";
    static final String  NL_HANDLED = "handled";
    static final String  NL_PREFERENCES = "preferences";
    static final String  NL_PRINTFILE = "printfile";
    static final String  NL_QUIT = "quit";
    static final String  NL_REOPENAPPLICATION = "reopenapplication";

    //
    // Unique value associated with each local field name.
    //

    static final int  VL_ABOUT = 1;
    static final int  VL_FILENAME = 2;
    static final int  VL_MOUSELOCATION = 3;
    static final int  VL_OPENAPPLICATION = 4;
    static final int  VL_OPENFILE = 5;
    static final int  VL_HANDLED = 6;
    static final int  VL_PREFERENCES = 7;
    static final int  VL_PRINTFILE = 8;
    static final int  VL_QUIT = 9;
    static final int  VL_REOPENAPPLICATION = 10;

}
