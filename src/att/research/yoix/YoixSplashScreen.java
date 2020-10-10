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

public abstract
class YoixSplashScreen

    implements YoixConstants

{

    //
    // A simple class that manages the official system splash screen.
    //

    private static final String  SPLASHSCREEN_SCRIPT = "att/research/yoix/resources/system/splashscreen.yx";

    private static YoixObject  splashscreen = null;

    ///////////////////////////////////
    //
    // YoixSplashScreen Methods
    //
    ///////////////////////////////////

    static void
    hideSystemSplashScreen() {

	synchronized(SPLASHSCREEN_SCRIPT) {
	    if (splashscreen != null) {
		splashscreen.putInt(N_DISPOSE, true);
		splashscreen = null;
	    }
	}
    }


    static void
    showSystemSplashScreen(String title, String background, String foreground, boolean canexit) {

	//
	// This is the method that's called to process when a command line
	// option was used to request a system splash screen. Since we're
	// not completely booted yet (that only happens when execute the
	// Yoix script) we can't create Yoix representations of Colors (or
	// any other object that requires module loading), so the background
	// and forground colors are represented as Yoix strings. The code in
	// in the splash screen can use getRGBColor(), which we modified in
	// the 2.2.1 release, to translate the strings into colors.
	//

	showSystemSplashScreen(
	    YoixObject.newString(title),
	    YoixObject.newString(background),
	    YoixObject.newString(foreground),
	    canexit
	);
    }


    static void
    showSystemSplashScreen(YoixObject title, YoixObject background, YoixObject foreground, boolean canexit) {

	YoixObject  argv;
	String      resource;
	InputStream      stream = null;

	//
	// We assume we know how the spashscreen script uses arguments that
	// we pass to it via Yoix.executeStream().
	//

	synchronized(SPLASHSCREEN_SCRIPT) {
	    hideSystemSplashScreen();
	    if ((resource = YoixMisc.getResource(SPLASHSCREEN_SCRIPT)) != null) {
		try {
		    if ((stream = YoixMisc.getInputStream(resource)) != null) {
			argv = YoixObject.newArray(5);
			argv.putString(0, SPLASHSCREEN_SCRIPT);
			argv.putInt(1, true);
			argv.putObject(2, title);
			argv.putObject(3, background);
			argv.putObject(4, foreground);
			splashscreen = Yoix.executeStream(stream, SPLASHSCREEN_SCRIPT, argv, null, !canexit);
		    }
		}
		finally {
		    try {
			if (stream != null)
			    stream.close();
		    }
		    catch(IOException e) {}
		}
	    }
	}
    }
}

