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

package att.research.yoix.jvma;
import att.research.yoix.*;
import java.io.*;
import java.lang.reflect.*;

public abstract
class JVMTestModule extends YoixModule

    implements YoixConstants

{

    //
    // Simple test module that was during development work on the assembler.
    // It's not currently used, but probably still works if you follow the
    // instructions in one of the README files (probably Tests/README).
    //

    public static String  $MODULENAME = "JVMTestModule";

    public static String  $JVMCONSTANTS = "att.research.yoix.jvma.JVMConstants";

    public static Object  $module[] = {
    //
    // NAME                                   ARG                  COMMAND     MODE   REFERENCE
    // ----                                   ---                  -------     ----   ---------
       null,                                  "20",                $LIST,      $RORO, $MODULENAME,
       "assembleClass",                       "-1",                $BUILTIN,   $LR_X, null,
       "defineClass",                         "-1",                $BUILTIN,   $LR_X, null,

       "dumpClassFile",                       "0",                 $BUILTIN,   $LR_X, null,
       "getDeclarationFromDescriptor",        "2",                 $BUILTIN,   $LR_X, null,
       "getDescriptorForMethod",              "1",                 $BUILTIN,   $LR_X, null,
       "getNameFromDescriptor",               "1",                 $BUILTIN,   $LR_X, null,
       "isFieldDescriptor",                   "1",                 $BUILTIN,   $LR_X, null,
       "isMethodDescriptor",                  "1",                 $BUILTIN,   $LR_X, null,
       "loadClassFile",                       "1",                 $BUILTIN,   $LR_X, null,
       "storeConstantValue",                  "2",                 $BUILTIN,   $LR_X, null,
       "storeDeprecatedClass",                "0",                 $BUILTIN,   $LR_X, null,
       "storeDeprecatedField",                "1",                 $BUILTIN,   $LR_X, null,
       "storeDeprecatedMethod",               "2",                 $BUILTIN,   $LR_X, null,
       "storeFieldInfo",                      "-2",                $BUILTIN,   $LR_X, null,
       "storeMethodInfo",                     "-2",                $BUILTIN,   $LR_X, null,
       "storeInterface",                      "-1",                $BUILTIN,   $LR_X, null,
       "storeThrownException",                "3",                 $BUILTIN,   $LR_X, null,
       "storeInnerClass",                     "4",                 $BUILTIN,   $LR_X, null,
       "writeClassFile",                      "1",                 $BUILTIN,   $LR_X, null,

       "OpCode",                              "200",               $DICT,      $RORO, $_THIS,
       null,                                  "-1",                $GROWTO,    null,  null,
       $JVMCONSTANTS,                         "OP_\tOP_",          $READCLASS, $LR__, null,
       null,                                  null,                $GROWTO,    null,  null,
       null,                                  $MODULENAME,         $PUT,       null,  null,
    };

    //
    // Internal data structures that are only for testing and debugging.
    // Expect we'll eventually define Yoix types for many of them.
    //

    private static JVMClassFile  classfile = null;

    ///////////////////////////////////
    //
    // JVMTestModule Methods
    //
    ///////////////////////////////////

    public static YoixObject
    assembleClass(YoixObject arg[]) {

	String  details;

	if (arg[0].isString()) {
	    try {
		classfile = (new JVMAssembler()).assembleClass(arg[0].stringValue());
	    }
	    catch(JVMAssemblerError e) {
		classfile = null;
		if (e.getMessage() != null)
		    System.err.println(e.getMessage());
		if ((details = e.getDetails()) != null)
		    System.err.println(details);
	    }
	} else VM.badArgument(0);

	return(YoixObject.newInt(1));
    }


    public static YoixObject
    defineClass(YoixObject arg[]) {

	String  details;
	Method  method;
	Class   definedclass = null;
	Class   types[];
	Object  args[];
	int     length;
	int     m;
	int     n;

	//
	// Code that takes over after the class is defined only handles a
	// trivial test call to a static method that's in the successfully
	// defined class.
	//

	if (arg[0].isString()) {
	    try {
		definedclass = (new JVMClassLoader()).defineClass(arg[0].stringValue());
	    }
	    catch(JVMAssemblerError e) {
		if (e.getMessage() != null)
		    System.err.println(e.getMessage());
		if ((details = e.getDetails()) != null)
		    System.err.println(details);
	    }

	    if (definedclass != null) {
		if (arg.length > 1) {
		    if (arg[1].isString()) {
			try {
			    length = arg.length - 2;
			    args = new Object[length];
			    types = new Class[length];
			    for (n = 2, m = 0; n < arg.length; n++, m++) {
				if (arg[n].isDouble()) {
				    types[m] = Double.TYPE;
				    args[m] = new Double(arg[n].doubleValue());
				} else if (arg[n].isInteger()) {
				    types[m] = Integer.TYPE;
				    args[m] = new Integer(arg[n].intValue());
				} else if (arg[n].isString()) {
				    types[m] = String.class;
				    args[m] = arg[n].stringValue();
				} else {
				    types[m] = Object.class;
				    args[m] = null;
				}
			    }
			    method = definedclass.getMethod(arg[1].stringValue(), types);
			    method.invoke(null, args);
			}
			catch(Exception e) {
			    System.err.println("caught e=" + e);
			}
		    } else VM.badArgument(1);
		}
	    }
	} else VM.badArgument(0);

	return(YoixObject.newInt(definedclass != null));
    }


    public static YoixObject
    dumpClassFile(YoixObject arg[]) {

	String  str = null;

	if (arg.length == 0) {
	    if (classfile != null)
		str = classfile.dumpClassFile();
	} else VM.badCall();

	return(YoixObject.newString(str));
    }


    public static YoixObject
    loadClassFile(YoixObject arg[]) {

	if (arg[0].isString() || arg[0].isNull()) {
	    if (arg[0].notNull())
		classfile = new JVMClassFile(arg[0].stringValue());
	}
	return(YoixObject.newEmpty());
    }


    public static YoixObject
    storeConstantValue(YoixObject arg[]) {

	String  name;
	int     index = -1;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (classfile != null) {
		name = arg[0].stringValue();
		if (arg[1].isInteger())
		    index = classfile.storeConstantValue(name, arg[1].intValue());
		else if (arg[1].isDouble())
		    index = classfile.storeConstantValue(name, arg[1].doubleValue());
		else if (arg[1].isString())
		    index = classfile.storeConstantValue(name, arg[1].stringValue());
	    }
	} else VM.badArgument(1);

	return(YoixObject.newInt(index));
    }


    public static YoixObject
    storeDeprecatedClass(YoixObject arg[]) {

	int  index = -1;

	if (classfile != null)
	    index = classfile.storeDeprecatedClass();

	return(YoixObject.newInt(index >= 0));
    }


    public static YoixObject
    storeDeprecatedField(YoixObject arg[]) {

	int  index = -1;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (classfile != null)
		 index = classfile.storeDeprecatedField(arg[0].stringValue());
	} else VM.badArgument(0);

	return(YoixObject.newInt(index >= 0));
    }


    public static YoixObject
    storeDeprecatedMethod(YoixObject arg[]) {

	int  index = -1;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		if (classfile != null)
		    index = classfile.storeDeprecatedMethod(arg[0].stringValue(), arg[1].stringValue());
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(index >= 0));
    }


    public static YoixObject
    storeFieldInfo(YoixObject arg[]) {

	int  index = -1;

	if (arg.length >= 2 || arg.length <= 4) {
	    if (arg[0].isString()) {
		if (arg[1].isString()) {
		    if (arg.length <= 2 || arg[2].isInteger()) {
			if (arg.length <= 3) {	// no attributes - for now
			    if (classfile != null) {
				index = classfile.storeField(
				    arg[0].stringValue(),
				    arg[1].stringValue(),
				    arg.length > 2 ? arg[2].intValue() : 0,
				    null
				 );
			    }
			}
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(index >= 0));
    }


    public static YoixObject
    storeMethodInfo(YoixObject arg[]) {

	int  index = -1;

	if (arg.length >= 2 || arg.length <= 4) {
	    if (arg[0].isString()) {
		if (arg[1].isString()) {
		    if (arg.length <= 2 || arg[2].isInteger()) {
			if (arg.length <= 3) {	// no attributes - for now
			    if (classfile != null) {
				index = classfile.storeMethod(
				    arg[0].stringValue(),
				    arg[1].stringValue(),
				    arg.length > 2 ? arg[2].intValue() : 0,
				    null
				);
			    }
			}
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(index >= 0));
    }


    public static YoixObject
    storeInterface(YoixObject arg[]) {

	int  index = -1;

	if (arg.length == 1) {
	    if (arg[0].isString()) {
		if (classfile != null)
		    index = classfile.storeInterface(arg[0].stringValue());
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(index >= 0));
    }


    public static YoixObject
    storeThrownException(YoixObject arg[]) {

	int  index = -1;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		if (arg[2].isString() && arg[2].notNull()) {
		    if (classfile != null) {
			index = classfile.storeThrownException(
			    arg[0].stringValue(),
			    arg[1].stringValue(),
			    arg[2].stringValue()
			);
		    }
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(index >= 0));
    }


    public static YoixObject
    storeInnerClass(YoixObject arg[]) {

	int  index = -1;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		if (arg[2].isString() && arg[2].notNull()) {
		    if (arg[3].isInteger()) {
			if (classfile != null) {
			    index = classfile.storeInnerClass(
				arg[0].stringValue(),
			 	arg[1].stringValue(),
				arg[2].stringValue(),
				arg[3].intValue()
			    );
			}
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(index >= 0));
    }


    public static YoixObject
    writeClassFile(YoixObject arg[]) {

	FileOutputStream  stream;
	String            path;
	File              file;
	byte              bytes[];
	int               total = -1;

	if (arg[0].isString() || arg[0].isNull()) {
	    if (arg[0].notNull()) {
		path = arg[0].stringValue();
		if (classfile != null) {
		    if ((bytes = classfile.getBytes()) != null) {
			try {
			    stream = new FileOutputStream(path);
			    stream.write(bytes);
			    stream.close();
			    total = bytes.length;
			}
			catch(IOException e) {}
		    }
		}
	    }
	}

	return(YoixObject.newInt(total));
    }


    public static YoixObject
    getDeclarationFromDescriptor(YoixObject arg[]) {

	String  str = null;

	if (arg[0].isString() || arg[0].isNull()) {
	    if (arg[1].isString()) {
		str = JVMDescriptor.getDeclarationFromDescriptor(
		    arg[0].notNull() ? arg[0].stringValue() : null,
		    arg[1].stringValue()
		);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newString(str));
    }


    public static YoixObject
    getDescriptorForMethod(YoixObject arg[]) {

	String  str = null;

	if (arg[0].isString())
	    str = JVMDescriptor.getDescriptorForMethod(arg[0].stringValue());
	else VM.badArgument(0);

	return(YoixObject.newString(str));
    }


    public static YoixObject
    getNameFromDescriptor(YoixObject arg[]) {

	String  str = null;

	if (arg[0].isString())
	    str = JVMDescriptor.getDeclarationFromDescriptor(null, arg[0].stringValue());
	else VM.badArgument(0);

	return(YoixObject.newString(str));
    }


    public static YoixObject
    isFieldDescriptor(YoixObject arg[]) {

	boolean  result = false;

	if (arg[0].isString())
	    result = JVMDescriptor.isFieldDescriptor(arg[0].stringValue());
	else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isMethodDescriptor(YoixObject arg[]) {

	boolean  result = false;

	if (arg[0].isString())
	    result = JVMDescriptor.isMethodDescriptor(arg[0].stringValue());
	else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }
}

