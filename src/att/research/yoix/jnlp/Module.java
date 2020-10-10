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

package att.research.yoix.jnlp;
import java.net.*;
import att.research.yoix.*;

public abstract
class Module extends YoixModule

    implements Constants

{

    //
    // This module probably shouldn't be used when we're not running under
    // javaws. Reflection isn't an option here because it probably isn't
    // allowed. Everything would be much simpler if Sun included javax.jlnp
    // classes in a jar file that's included in java's bootclasspath.
    //

    public static final String  $MODULENAME = "jnlp";
    public static final String  $MODULECREATED = "Tue Nov 25 13:02:20 EST 2008";
    public static final String  $MODULEVERSION = "1.0";

    public static Object  $module[] = {
    //
    // NAME                        ARG                 COMMAND     MODE   REFERENCE
    // ----                        ---                 -------     ----   ---------
       $MODULENAME,                "2",                $LIST,      $RORO, $MODULENAME,

       "isWebBrowserSupported",    "0",                $BUILTIN,   $LR_X, null,
       "showDocument",             "1",                $BUILTIN,   $LR_X, null,
    };

    ///////////////////////////////////
    //
    // Module Methods
    //
    ///////////////////////////////////

    public static YoixObject
    isWebBrowserSupported(YoixObject arg[]) {

	boolean  result = false;
	Object   bs;

	try {
	    if (checkJNLP()) {
		if ((bs = javax.jnlp.ServiceManager.lookup("javax.jnlp.BasicService")) != null)
		    result = ((javax.jnlp.BasicService)bs).isWebBrowserSupported();
	    }
	}
	catch(Exception e) {
	    result = false;
	}

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    showDocument(YoixObject arg[]) {

	boolean  result = false;
	Object   bs;
	URL      url;

	if (arg[0].isString() || arg[0].isNull()) {
	    if (arg[0].isString()) {
		try {
		    url = new URL(arg[0].stringValue());
		    //
		    // The check prevents a ClassNotFoundException when we try
		    // to call lookup() when javax.jnlp classes aren't available.
		    // Not sure why the exception wasn't caught by the existing
		    // catch - investigate later.
		    //
		    if (checkJNLP()) {
			if ((bs = javax.jnlp.ServiceManager.lookup("javax.jnlp.BasicService")) != null)
			    result = ((javax.jnlp.BasicService)bs).showDocument(url);
		    }
		}
		catch(Exception e) {
		    result = false;
		}
	    }
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static boolean
    checkJNLP() {

	boolean  result = false;

	try {
	    Class.forName("javax.jnlp.ServiceManager");
	    result = true;
	}
	catch(ClassNotFoundException e) {}

	return(result);
    }
}

