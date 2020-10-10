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

import att.research.yoix.*;	// see the NOTE below

//
// An example of a custom module that defines a few trivial builtins
// and is automatically loaded when it's referenced in standard dot
// notation that begins with "yoix.module." and ends with the full
// name of this class. In addition this class file must be put in
// directory that Java examines when it searches for a class using
// the CLASSPATH variable.
//
// For example, if you're in the Yoix source directory type,
//
//	make all ExampleModule.class
//
// to build the interpreter and ExampleModule.class, then type
//
//	CLASSPATH=../../..:. java  att.research.yoix.YoixMain -d1
//
// to run yoix (notice the :. at the end of CLASSPATH), and finally
// type
//
//	yoix.module.ExampleModule.version();
//
// and the interpreter should automatically load ExampleModule, run
// the version() builtin that's defined below, and print the answer
// on standard output (because of the -d1 flag).
//
// Although this works, user module source code is not usually kept
// with the Yoix source code. We've written at least one substantial
// user module, and in every case we kept the module source code in
// a completely separate directory.
//
// Custom module support first appeared in version 0.9.6 even though
// it all worked properly at that time there were some details that
// needed attention - we plan on tackling the missing pieces soon!!
//
//
// NOTE - even though the line
//
//	import att.research.yoix.*;
//
// works for this simple example, javac will sometimes get confused
// if your module source code is kept with the source code for the
// att.research.yoix package. If you run into strange javac errors
// move your source to a different directory and/or explicitly name
// the att.research.yoix classes that your module needs to import.
// For example, changing
//
//	import att.research.yoix.*;
//
// to
//
//	import att.research.yoix.YoixModule;
//	import att.research.yoix.YoixConstants;
//	import att.research.yoix.YoixObject;
//
// would be sufficient for our simple example.
//

public abstract
class ExampleModule extends YoixModule

    implements YoixConstants

{

    public static String  $MODULENAME = "ExampleModule";
    public static String  $MODULEVERSION = "2.0";

    public static Object  $module[] = {
    //
    // NAME                  ARG        COMMAND     MODE   REFERENCE
    // ----                  ---        -------     ----   ---------
       $MODULENAME,          "3",       $LIST,      $RORO, $MODULENAME,
       "DUMMY_CONSTANT",     "12",      $INTEGER,   $LR__, null,
       "property",           "1",       $BUILTIN,   $LR_X, null,
       "version",            "0",       $BUILTIN,   $LR_X, null,
    };

    ///////////////////////////////////
    //
    // ExampleModule Methods
    //
    ///////////////////////////////////

    public static YoixObject
    property(YoixObject arg[]) {

	String  value = null;

	if (arg[0].isString())
	    value = System.getProperty(arg[0].stringValue());
	else VM.badArgument(0);

	return(YoixObject.newString(value));
    }


    public static YoixObject
    version(YoixObject arg[]) {

	String  value = null;

	if (arg.length == 0)
	    value = System.getProperty("java.version");
	else VM.badCall();

	return(YoixObject.newString(value));
    }
}

