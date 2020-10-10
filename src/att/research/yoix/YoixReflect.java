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
import java.lang.reflect.*;

public abstract
class YoixReflect

    implements YoixAPI,
	       YoixConstants

{

    //
    // A class that's supposed to be the place where we put any reflection
    // related code that may be useful elsewhere (e.g., in custom modules).
    // The invoke() and newInstance() methods never throw exceptions, and
    // normally return null when there's an error. Use REFLECTION_ERROR as
    // the result argument when you call invoke() or newInstance() and any
    // exception that's caught will end up as the return value.
    //

    public static final Throwable  REFLECTION_ERROR = new Throwable();

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public static Class
    getClassForName(String name) {

	Class  cls = null;

	try {
	    cls = Class.forName(name);
	}
	catch(ClassNotFoundException e) {}

	return(cls);
    }


    public static Object
    getDeclaredField(Class source, String name) {

	return(getDeclaredField(source, name, null, null));
    }


    public static Object
    getDeclaredField(Class source, String name, Object instance) {

	return(getDeclaredField(source, name, instance, null));
    }


    public static Object
    getDeclaredField(Class source, String name, Object instance, Object result) {

	try {
	    result = source.getDeclaredField(name).get(instance);
	}
	catch(NoSuchFieldException e) {
	    if (result == REFLECTION_ERROR)
		result = e;
	}
	catch(IllegalAccessException e) {
	    if (result == REFLECTION_ERROR)
		result = e;
	}
	catch(RuntimeException e) {
	    if (result == REFLECTION_ERROR)
		result = e;
	}
	return(result);
    }


    public static Object
    invoke(Object owner, String name) {

	return(invoke(owner, name, null, null, null));
    }


    public static Object
    invoke(Object owner, String name, Object result) {

	return(invoke(owner, name, null, null, result));
    }


    public static Object
    invoke(Object owner, String name, boolean arg) {

	return(invoke(owner, name, new Object[] {new Boolean(arg)}));
    }


    public static Object
    invoke(Object owner, String name, int arg) {

	return(invoke(owner, name, new Object[] {new Integer(arg)}));
    }


    public static Object
    invoke(Object owner, String name, double arg) {

	return(invoke(owner, name, new Object[] {new Double(arg)}));
    }


    public static Object
    invoke(Object owner, String name, String arg) {

	return(invoke(owner, name, new Object[] {arg}));
    }


    public static Object
    invoke(Object owner, String name, Object args[]) {

	return(invoke(owner, name, args, getReflectionTypes(args), null));
    }


    public static Object
    invoke(Object owner, String name, Object args[], Object result) {

	return(invoke(owner, name, args, getReflectionTypes(args), result));
    }


    public static Object
    invoke(Object owner, String name, Object args[], Class types[]) {

	return(invoke(owner, name, args, types, null));
    }


    public static Object
    invoke(Object owner, String name, Object args[], Class types[], Object result) {

	Method  method;
	Class   source;

	try {
	    if (owner instanceof String) {
		source = Class.forName((String)owner);
		owner = null;
	    } else source = owner.getClass();
	    method = source.getMethod(name, types);
	    result = method.invoke(owner, args);
	}
	catch(ClassNotFoundException e) {
	    if (result == REFLECTION_ERROR)
		result = e;
	}
	catch(NoSuchMethodException e) {
	    if (result == REFLECTION_ERROR)
		result = e;
	}
	catch(InvocationTargetException e) {
	    if (result == REFLECTION_ERROR)
		result = e;
	}
	catch(IllegalAccessException e) {
	    if (result == REFLECTION_ERROR)
		result = e;
	}
	catch(RuntimeException e) {
	    if (result == REFLECTION_ERROR)
		result = e;
	}

	return(result);
    }


    public static Object
    javaInstance(String name, Class target, YoixObject args)

	throws InvocationTargetException

    {

	//
	// These are currently used by a custom module (that we undoubtedly
	// will release when it's all finished). They're a little different
	// the other methods in this class, so there's a pretty good chance
	// we'll eventually try to clean things up. Until then you probably
	// should avoid using these until these comments disappear.
	//

	return(invokeConstructor(name, target, args));
    }


    public static Object
    javaInstance(Class source, Class target, YoixObject args)

	throws InvocationTargetException

    {

	//
	// These are currently used by a custom module (that we undoubtedly
	// will release when it's all finished). They're a little different
	// the other methods in this class, so there's a pretty good chance
	// we'll eventually try to clean things up. Until then you probably
	// should avoid using these until these comments disappear.
	//

	return(invokeConstructor(source, target, args));
    }


    public static Object
    newInstance(String name) {

	return(newInstance(name, null, null, null));
    }


    public static Object
    newInstance(String name, boolean arg) {

	return(newInstance(name, new Object[] {new Boolean(arg)}));
    }


    public static Object
    newInstance(String name, int arg) {

	return(newInstance(name, new Object[] {new Integer(arg)}));
    }


    public static Object
    newInstance(String name, double arg) {

	return(newInstance(name, new Object[] {new Double(arg)}));
    }


    public static Object
    newInstance(String name, String arg) {

	return(newInstance(name, new Object[] {arg}));
    }


    public static Object
    newInstance(String name, Object args[]) {

	return(newInstance(name, args, getReflectionTypes(args), null));
    }


    public static Object
    newInstance(String name, Object args[], Class types[]) {

	return(newInstance(name, args, types, null));
    }


    public static Object
    newInstance(String name, Object args[], Class types[], Object result) {

	Constructor  constructor;
	Class        source;

	try {
	    source = Class.forName(name);
	    if (args != null && types != null) {
		constructor = source.getDeclaredConstructor(types);
		result = constructor.newInstance(args);
	    } else result = source.newInstance();
	}
	catch(NoClassDefFoundError e) {
	    if (result == REFLECTION_ERROR)
		result = e;
	}
	catch(ClassNotFoundException e) {
	    if (result == REFLECTION_ERROR)
		result = e;
	}
	catch(InvocationTargetException e) {
	    if (result == REFLECTION_ERROR)
		result = e;
	}
	catch(InstantiationException e) {
	    if (result == REFLECTION_ERROR)
		result = e;
	}
	catch(IllegalAccessException e) {
	    if (result == REFLECTION_ERROR)
		result = e;
	}
	catch(NoSuchMethodException e) {
	    if (result == REFLECTION_ERROR)
		result = e;
	}
	catch(RuntimeException e) {
	    if (result == REFLECTION_ERROR)
		result = e;
	}

	return(result);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static Object[]
    getReflectionArrays(YoixObject obj) {

	YoixObject  element;
	Object      args[] = null;
	Object      types[] = null;
	double      value;
	int         length;
	int         m;
	int         n;

	//
	// Leaves lots to be desired, but the very limited mapping that's
	// supported should be sufficient for now. There's currently no
	// support for some simple argument types (e.g., boolean or float)
	// but it's not hard to imagine conventions that could expand the
	// the supported types. For example, the name of the argument's
	// Java type (perhaps as a string) and the argument's value could
	// be stored in Yoix arrays that we could parse here - maybe later.
	//

	if (obj != null) {
	    if ((length = obj.sizeof()) > 0) {
		types = new Class[length];
		args = new Object[length];
		for (n = obj.offset(), m = 0; m < args.length; m++, n++) {
		    if ((element = obj.getObject(n)) != null) {
			if (element.isDouble()) {
			    types[m] = Double.TYPE;
			    args[m] = new Double(element.doubleValue());
			} else if (element.isInteger()) {
			    types[m] = Integer.TYPE;
			    args[m] = new Integer(element.intValue());
			} else if (element.isString()) {
			    types[m] = String.class;
			    args[m] = element.stringValue();
			} else {
			    types[m] = Object.class;
			    args[m] = null;
			}
		    } else {
			types[m] = Object.class;
			args[m] = null;
		    }
		}
	    }
	}
	return(new Object[] {types, args});
    }


    private static Class[]
    getReflectionTypes(Object args[]) {

	Class  types[];
	Field  field;
	int    n;

	//
	// Returns an array of Class objects that describes the Objects
	// in the args[] array. Does a decent job, but isn't always the
	// right way to go.
	//

	if (args != null) {
	    types = new Class[args.length];
	    for (n = 0; n < args.length; n++) {
		if (args[n] != null) {
		    types[n] = args[n].getClass();
		    try {
			//
			// Old version used getDeclaredField(), but there
			// really was no reason why. Changed to eliminate
			// YoixSecurityManager.checkMemberAccess() errors
			// when interpreter runs in applet mode.
			// 
			field = types[n].getField("TYPE");
			types[n] = (Class)field.get(null);
		    }
		    catch(IllegalAccessException e) {}
		    catch(NoSuchFieldException e) {}
		    catch(RuntimeException e) {}
		} else types[n] = Object.class;
	    }
	} else types = null;

	return(types);
    }


    private static Object
    invokeConstructor(String name, Class target, YoixObject args)

	throws InvocationTargetException

    {

	Object  instance = null;

	if (name != null) {
	    try {
		instance = invokeConstructor(Class.forName(name), target, args);
	    }
	    catch(ClassNotFoundException e) {
		throw(new InvocationTargetException(e));
	    }
	}
	return(instance);
    }


    private static Object
    invokeConstructor(Class source, Class target, YoixObject args)

	throws InvocationTargetException

    {

	YoixObject  constructor;
	YoixObject  setup;
	YoixObject  element;
	Method      method;
	String      name;
	Object      arrays[];
	Object      instance = null;
	int         length;
	int         n;

	if (source != null) {
	    try {
		if (args != null && (constructor = args.getObject(N_JAVACONSTRUCTOR)) != null) {
		    arrays = getReflectionArrays(constructor);
		    instance = source.getDeclaredConstructor((Class[])arrays[0]).newInstance((Object[])arrays[1]);
		} else instance = source.newInstance();
		if (target != null) {
		    if (target.isInstance(instance) == false)
			throw(new ClassCastException());
		}
		if (args != null && (setup = args.getObject(N_JAVASETUP)) != null) {
		    length = setup.sizeof();
		    for (n = 0; n < length - 1; n += 2) {
			name = setup.getString(n);
			arrays = getReflectionArrays(setup.getObject(n+1));
			method = source.getDeclaredMethod(name, (Class[])arrays[0]);
			method.invoke(instance, (Object[])arrays[1]);
		    }
		}
	    }
	    catch(InstantiationException e) {
		throw(new InvocationTargetException(e));
	    }
	    catch(IllegalAccessException e) {
		throw(new InvocationTargetException(e));
	    }
	    catch(ClassCastException e) {
		throw(new InvocationTargetException(e));
	    }
	    catch(NoSuchMethodException e) {
		throw(new InvocationTargetException(e));
	    }
	    catch(RuntimeException e) {
		throw(new InvocationTargetException(e));
	    }
	}
	return(instance);
    }
}

