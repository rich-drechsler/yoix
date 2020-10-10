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
import java.util.*;
import java.lang.reflect.*;
import att.research.yoix.jvma.*;

final
class YoixBodyFunction

    implements YoixConstants,
	       YoixCompilerConstants,
	       YoixInterfaceBody,
	       YoixInterfaceCallable,
	       YoixInterfaceCloneable,
	       YoixInterfacePointer

{

    //
    // Implementing YoixInterfacePointer is a recent addition that lets us
    // expose some of the internal data to Yoix scripts using syntax that
    // works like references to fields in a dictionary (e.g., f.linked).
    // There's a small chance it's just a temporary change, so we probably
    // won't document it for a while.
    //

    private YoixObject  names;
    private YoixObject  values;
    private YoixObject  global;
    private SimpleNode  tree;
    private boolean     bound = false;
    private boolean     varargs;
    private boolean     executed;
    private boolean     evaluate;
    private int         argc;
    private int         counter;

    //
    // New additions for the function compiler. We only save the compiled
    // class because there's a chance we'll add a field that will let the
    // user dump it.
    //

    private boolean     compilable = true;
    private boolean     linked = false;
    private Class       compiled_class = null;
    private Method      compiled_method = null;
    private SimpleNode  compiled_node_references[] = null;
    private YoixObject  compiled_object_references[] = null;

    //
    // Functions now implement YoixInterfacePointer and can expose data to
    // Yoix scripts via the fields that are maintained by fieldindices and
    // fieldcodes objects. We use fieldindices to map a field name into an
    // index in the fieldcodes ArrayList where the actual code that's used
    // in switch statements is stored. Wouldn't be a big deal if we stored
    // the code in fieldindices and tossed fieldcodes, but it seemed like
    // using small consecutive integers for pointer offsets was reasonable.
    //

    private static HashMap    fieldindices = new HashMap();
    private static ArrayList  fieldcodes = new ArrayList();

    static {
	int  index = 0;

	fieldindices.put(N_BOUND, new Integer(index++));
	fieldcodes.add(new Integer(V_BOUND));

	fieldindices.put(N_COMPILABLE, new Integer(index++));
	fieldcodes.add(new Integer(V_COMPILABLE));

	fieldindices.put(N_LINKED, new Integer(index++));
	fieldcodes.add(new Integer(V_LINKED));

	fieldindices.put(N_COUNTER, new Integer(index++));
	fieldcodes.add(new Integer(V_COUNTER));

	fieldindices.put(N_TREE, new Integer(index++));
	fieldcodes.add(new Integer(V_TREE));

	fieldindices.put(N_VARARGS, new Integer(index++));
	fieldcodes.add(new Integer(V_VARARGS));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyFunction(YoixObject names, YoixObject values, SimpleNode tree, boolean varargs) {

	this.names = names;
	this.values = values;
	this.tree = tree;
	this.varargs = varargs;

	global = YoixBodyBlock.getGlobal();
	executed = YoixBodyBlock.isExecuted();
	argc = names.length();
	bound = !VM.getBoolean(N_BIND);
	evaluate = (tree.type() == EXPRESSION);
	counter = 0;
    }

    ///////////////////////////////////
    //
    // YoixInterfaceCloneable Methods
    //
    ///////////////////////////////////

    public final synchronized Object
    clone() {

	Object  obj;

	try {
	    obj = super.clone();
	}
	catch(CloneNotSupportedException e) {
	    obj = VM.die(INTERNALERROR);
	}

	return(obj);
    }


    public final Object
    copy(HashMap copied) {

	return(clone());
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final String
    dump() {

	SimpleNode  node;
	String      name;
	String      str;
	int         off[];
	int         pos[];
	int         m;
	int         n;
	int         o;

	if ((name = getName()) != null)
	    str = name + "(";
	else str = "function(";

	if (argc > 0) {
	    pos = new int[argc];
	    off = new int[argc];
	    for (n = 0; n < argc; n++) {
		pos[n] = names.get(n, false).intValue();
		off[n] = n;
	    }
	    for (n = 0; n < argc; n++) {
		for (m = n + 1; m < argc; m++) {
		    if (pos[off[n]] > pos[off[m]]) {
			o = off[n];
			off[n] = off[m];
			off[m] = o;
		    }
		}
	    }

	    for (n = 0; n < argc; n++)
		str += (n == 0 ? "" : ", ") + names.name(off[n]);

	} else n = 0;

	if (varargs)
	    str += (n == 0 ? "..." : ", ...");
	str += ")" + NL;

	if (VM.bitCheck(N_DEBUG, DEBUG_PARSETREE)) {
	    if ((node = tree) != null)
		str += node.dump(PARSER_YOIX);
	}
	return(str);
    }


    public final int
    length() {

	return(argc);
    }


    public final String
    toString() {

	return(dump().trim());
    }


    public final int
    type() {

	return(FUNCTION);
    }

    ///////////////////////////////////
    //
    // YoixInterfacePointer Methods
    //
    ///////////////////////////////////

    public final YoixObject
    cast(YoixObject obj, int index, boolean clone) {

	if (inRange(index)) {
	    switch (getFieldCode(index)) {
		case V_BOUND:
		case V_COMPILABLE:
		case V_COUNTER:
		case V_LINKED:
		case V_VARARGS:
		    if (obj.isNumber()) {
			if (clone)
			    obj = (YoixObject)obj.clone();
		    } else VM.abort(TYPECHECK, index);
		    break;

		case V_TREE:
		    //
		    // The only thing exposed here is the dump of the tree,
		    // so scripts should consider this a String field.
		    //
		    if (obj.isString()) {
			if (clone)
			    obj = (YoixObject)obj.clone();
		    } else VM.abort(TYPECHECK, index);
		    break;

		default:
		    VM.abort(UNDEFINED, index);
		    break;
	    }
	} else VM.abort(RANGECHECK, index);

	return(obj);
    }


    public final YoixObject
    cast(YoixObject obj, String name, boolean clone) {

	int  index;

	if ((index = hash(name)) != -1)
	    obj = cast(obj, index, clone);
	else VM.abort(UNDEFINED, name);

	return(obj);
    }


    public final boolean
    compound() {

	//
	// Even though we associate names with values, other things, like
	// the number reported by the length attribute, have nothing to do
	// with the "fields" that this interface exposes to scripts, so we
	// decided to return false here.
	//

	return(false);
    }


    public final void
    declare(int index, YoixObject obj, int mode) {

	VM.abort(INVALIDACCESS);
    }


    public final void
    declare(String name, YoixObject obj, int mode) {

	VM.abort(INVALIDACCESS);
    }


    public final boolean
    defined(int index) {

	return(inRange(index));
    }


    public final boolean
    defined(String name) {

	return(defined(hash(name)));
    }


    public final int
    definedAt(String name) {

	int  index;

	return(((index = hash(name)) >= 0 && defined(index)) ? index : -1);
    }


    public final String
    dump(int index, String indent, String typename) {

	return(dump());
    }


    public final boolean
    executable(int index) {

	return(false);
    }


    public final boolean
    executable(String name) {

	return(false);
    }


    public final YoixObject
    execute(int index, YoixObject argv[], YoixObject context) {

	VM.abort(INVALIDACCESS);
	return(null);
    }


    public final YoixObject
    execute(String name, YoixObject argv[], YoixObject context) {

	return(execute(hash(name), argv, context));
    }


    public final YoixObject
    get(int index, boolean clone) {

	YoixObject  obj = null;
	SimpleNode  node;

	if (inRange(index)) {
	    switch (getFieldCode(index)) {
		case V_BOUND:
		    obj = YoixObject.newInt(bound);
		    break;

		case V_COUNTER:
		    obj = YoixObject.newInt(counter);
		    break;

		case V_COMPILABLE:
		    obj = YoixObject.newInt(compilable);
		    break;

		case V_LINKED:
		    obj = YoixObject.newInt(linked);
		    break;

		case V_TREE:
		    if ((node = tree) != null)
			obj = YoixObject.newString(node.dump(PARSER_YOIX));
		    else obj = YoixObject.newString();
		    break;

		case V_VARARGS:
		    obj = YoixObject.newInt(varargs);
		    break;

		default:
		    VM.abort(UNDEFINED, index);
		    break;
	    }
	} else VM.abort(RANGECHECK, index);

	return(obj);
    }


    public final YoixObject
    get(String name, boolean clone) {

	YoixObject  obj = null;
	int         index;

	if ((index = hash(name)) != -1)
	    obj = get(index, clone);
	else VM.abort(UNDEFINED, name);

	return(obj);
    }


    public final int
    hash(String name) {

	Integer  value;

	return((value = (Integer)fieldindices.get(name)) != null ? value.intValue() : -1);
    }


    public final String
    name(int index) {

	Iterator  iterator;
	String    name = null;
	String    key;

	if (defined(index)) {
	    iterator = fieldindices.keySet().iterator();
	    while (iterator.hasNext()) {
		key = (String)iterator.next();
		if (((Integer)fieldindices.get(key)).intValue() == index) {
		    name = key;
		    break;
		}
	    }
	}

	return(name);
    }


    public final YoixObject
    put(int index, YoixObject obj, boolean clone) {

	if (inRange(index)) {
	    switch (getFieldCode(index)) {
		case V_BOUND:
		    VM.abort(INVALIDACCESS, N_BOUND);
		    break;

		case V_COUNTER:
		    VM.abort(INVALIDACCESS, N_COUNTER);
		    break;

		case V_COMPILABLE:
		    if (obj.isNumber())
			compilable = obj.booleanValue();
		    else VM.abort(TYPECHECK, N_COMPILABLE);
		    break;

		case V_LINKED:
		    VM.abort(INVALIDACCESS, N_LINKED);
		    break;

		case V_TREE:
		    VM.abort(INVALIDACCESS, N_TREE);
		    break;

		case V_VARARGS:
		    VM.abort(INVALIDACCESS, N_VARARGS);
		    break;

		default:
		    VM.abort(UNDEFINED, index);
		    break;
	    }
	} else VM.abort(RANGECHECK, index);

	return(obj);
    }


    public final YoixObject
    put(String name, YoixObject obj, boolean clone) {

	int  index;

	if ((index = hash(name)) != -1)
	    obj = put(index, obj, clone);
	else VM.abort(UNDEFINED, name);

	return(obj);
    }


    public final boolean
    readable(int index) {

	return(defined(index));
    }


    public final boolean
    readable(String name) {

	return(readable(hash(name)));
    }


    public final int
    reserve(String name) {

	return(hash(name));
    }

    ///////////////////////////////////
    //
    // YoixInterfaceCallable Methods
    //
    ///////////////////////////////////

    public final YoixObject
    call(YoixObject argv[], YoixObject context) {

	YoixBodyArray  array;
	SimpleNode     node;
	YoixObject     args;
	YoixObject     result = null;
	YoixStack      stack;
	YoixError      return_point;
	Throwable      t;
	int            len;
	int            n;

	//
	// The way this code is currently written assumes that linked is
	// set to true before tree is set to null. If that's not the case
	// you could occasionally run into thread dependent problems.
	//

	node = tree;		// snapshot - just to be safe;
	counter++;		// number of times we were called

	if (argv.length == argc || (varargs && argv.length > argc)) {
	    stack = VM.getThreadStack();
	    if (linked || bound || bind(stack)) {
		len = argv.length + 1;
		if (values != null) {
		    args = values.duplicate();
		    array = (YoixBodyArray)args.body();
		    if (argv.length > argc) {
			args.setGrowable(true);
			args.setGrowto(len);
			array.growTo(len);
		    }
		} else {
		    args = YoixObject.newArrayBlock(len);
		    args.put(0, YoixObject.newString(), false);
		    array = (YoixBodyArray)args.body();
		}
		for (n = 1; n < len; n++) {
		    if (array.storeArgument(argv[n - 1], n) == null)
			VM.badArgument(OFFENDINGFUNCTION, getName(), n);
		}

		if (linked) {
		    //
		    // Suspect some compiled functions could omit much of the
		    // scope overhead. Best approach might be if the compiler
		    // would set a static flags field for each method that it
		    // compiles and we could use that field to decide what we
		    // can omit - maybe later.
		    //

		    stack.pushMark();
		    beginScope(context, args, stack);
		    return_point = evaluate ? null : stack.pushReturn();

		    try {
			result = (YoixObject)compiled_method.invoke(
			    null,
			    new Object[] {
				args,
				stack,
				compiled_node_references,
				compiled_object_references
			    }
			);
		    }
		    catch(InvocationTargetException e) {
			t = e.getTargetException();
			if (t instanceof YoixError) {
			    if (t == return_point)
				result = stack.popRvalue();
			    else throw((YoixError)t);
			} else {
			    if (t instanceof SecurityException)
				throw((SecurityException)t);
			    else if (t instanceof ThreadDeath)
				throw((ThreadDeath)t);
			    VM.abort(t);
			}
		    }
		    catch(IllegalAccessException e) {
			//
			// This should never happen - the compiler is supposed
			// build a class that lets us access the method!!
			//
			VM.abort(COMPILERERROR);
		    }
		    stack.popMark();
		} else {
		    stack.pushMark();
		    beginScope(context, args, stack);
		    result = evaluate ? YoixInterpreter.evaluate(node, stack) : YoixInterpreter.call(node, stack);
		    stack.popMark();
		}
	    } else VM.die(INTERNALERROR);	// should be impossible
	} else VM.badCall(OFFENDINGFUNCTION, getName());

	return(result != null ? result : YoixObject.newEmpty());
    }


    public final boolean
    callable(int argc) {

	return(argc == this.argc || (varargs && argc > this.argc));
    }


    public final boolean
    callable(YoixObject argv[]) {

	boolean  result;
	int      count;
	int      n;

	if (result = callable(argv.length)) {
	    if (values != null) {
		count = Math.min(argv.length, values.length() - 1);
		for (n = 0; n < count; n++) {
		    if (argv[n] != null && argv[n].cast(values.getObject(n+1), false) == null) {
			result = false;
			break;
		    }
		}
	    }
	}
	return(result);
    }

    ///////////////////////////////////
    //
    // YoixBodyFunction Methods
    //
    ///////////////////////////////////

    final boolean
    bind() {

	return(bind(VM.getThreadStack()));
    }


    final void
    clearGlobal() {

	//
	// Kludge added while implementing the Yoix version of catch.
	// Means we can use a YoixBodyFunction as the error handler,
	// but we can force it to run in the same context as the try
	// block. Need a general solution if it's used for anything
	// else.
	//

	global = null;
    }


    final boolean
    compile() {

	return(compile(new YoixCompiler()));
    }


    final boolean
    compile(YoixCompiler compiler) {

	YoixObject  args;
	YoixObject  obj;
	String      source;
	int         length;
	int         n;

	//
	// We probably could use more control to help us debug compiler or
	// assembler errors.
	//

	if (compilable && linked == false) {
	    synchronized(this) {
		if (compilable && linked == false) {
		    source = null;
		    length = argc + 1;
		    args = YoixObject.newArray(length);
		    for (n = 0; n < length; n++) {
			if ((obj = values.getObject(n)) != null)
			    args.putObject(n, obj.duplicate());
			else args.putNull(n);
		    }

		    if ((source = compiler.compileFunction(COMPILED_METHODNAME, names, args, tree)) != null) {
			try {
			    compiled_class = YoixCompiler.getClassLoader().defineClass(source);
			    compiled_method = compiled_class.getDeclaredMethod(
				COMPILED_METHODNAME,
				new Class[] {YoixObject.class, YoixStack.class, (new SimpleNode[0]).getClass(), (new YoixObject[0]).getClass()}
			    );
			    compiled_node_references = compiler.getNodeReferences();
			    compiled_object_references = compiler.getObjectReferences();

			    //
			    // Order of the next two statements matters - call()
			    // implicitly assumes linked is set to true before
			    // tree is set to null.
			    //
			    linked = true;
			    tree = null;

			    if (compiler.isDebugBitSet(DEBUG_ASSEMBLER_OUTPUT))
				System.err.println((new JVMAssembler()).assembleClass(source));
			}
			catch(Throwable t) {
			    compilable = false;
			    compiled_node_references = null;
			    compiled_object_references = null;
			    compiled_class = null;
			    compiled_method = null;

			    if (compiler.isDebugBitSet(DEBUG_COMPILER_ERRORS)) {
				System.err.println(t.getMessage());
				if (t instanceof JVMAssemblerError)
				    System.err.println(((JVMAssemblerError)t).getDetails());
				System.err.println(YoixMisc.javaTrace(t));
			    }
			}
		    } else compilable = false;
		}
	    }
	}

	return(linked);
    }


    final boolean
    link(String classname, String methodname) {

	if (compilable && linked == false) {
	    if (classname != null && methodname != null) {
		try {
		    compiled_class = YoixCompiler.getClassLoader().loadClass(classname);
		    compiled_method = compiled_class.getDeclaredMethod(
			methodname,
			new Class[] {YoixObject.class, YoixStack.class, (new SimpleNode[0]).getClass(), (new YoixObject[0]).getClass()}
		    );

		    if (compiled_node_references == null)
			compiled_node_references = (SimpleNode[])compiled_class.getDeclaredField(NAMEOF_NODES_FIELD).get(null);
		    if (compiled_object_references == null)
			compiled_object_references = (YoixObject[])compiled_class.getDeclaredField(NAMEOF_OBJECTS_FIELD).get(null);

		    linked = true;
		    tree = null;
		}
		catch(Throwable t) {
		    compiled_node_references = null;
		    compiled_object_references = null;
		    compiled_class = null;
		    compiled_method = null;
		}
	    }
	}

	return(linked);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    beginScope(YoixObject context, YoixObject args, YoixStack stack) {

	YoixObject  contextnames;

	//
	// The conditional use of context is for efficiency, but may
	// cause problems later on when we implement binding because
	// I expect we will bind using the block level and index into
	// values. Actually binding will be hard because context can
	// change (it's where the function is currently defined), so
	// binding may only work for arguments and anything above that
	// scope. A way to increment block level (so it's consistent)
	// may be sufficient.
	//

	if (global != null) {
	    if (context != null && context.bodyEquals(global) == false) {
		if (context.compound())
		    contextnames = context;
		else contextnames = YoixBodyBlock.getBlockNames(context);
	    } else contextnames = null;
	    stack.pushGlobalBlock(global);
	    if (executed)
		YoixBodyBlock.setExecuted(true);
	    if (contextnames != null)
		stack.pushLocalBlock(contextnames, context, true);
	}

	stack.pushLocalBlock(names, args, false);
	YoixBodyBlock.setFunctionBlock();
    }


    private boolean
    bind(YoixStack stack) {

	YoixObject  args;

	//
	// The return value is just for convenience.
	//

	if (bound == false) {
	    synchronized(this) {
		if (bound == false) {
		    stack.pushMark();
		    stack.pushGlobalBlock(false);
		    stack.pushLocalBlock(names, YoixObject.newArrayBlock(argc), false);
		    YoixBodyBlock.setFunctionBlock();
		    YoixBinder.bind(tree, stack);
		    bound = true;
		    stack.popMark();
		}
	    }
	}
	return(bound);
    }


    private int
    getFieldCode(int index) {

	Integer  value;

	return((value = (Integer)fieldcodes.get(index)) != null ? value.intValue() : -1);
    }


    private int
    getFieldCode(String name) {

	Integer  value;
	
	return((value = (Integer)fieldindices.get(name)) != null ? getFieldCode(value.intValue()) : -1);
    }


    private String
    getName() {

	YoixObject  obj;
	String      name;

	if (values != null && (obj = values.getObject(0)) != null && obj.notNull())
	    name = obj.stringValue();
	else name = null;

	return(name);
    }


    private boolean
    inRange(int index) {

	return(index >= 0 && index < fieldcodes.size());
    }
}

