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
import att.research.yoix.jvma.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

class YoixCompiler

    implements YoixCompilerConstants

{

    //
    // The start of an experiment to see if compiling Yoix parse trees into
    // bytecode is worthwile. If so we probably will restructure things and
    // eventually move most of the compiler related classes somewhere else.
    //
    // NOTE - passing the thread's stack to the compiled method and to some
    // of the compiler's support functions can add instructions to compiled
    // code, but improves performance when those support methods are needed.
    // The reason is obvious - the single instruction used to add the stack
    // to a method call turns into the Java method call
    //
    //     VM.getThreadStack()
    //
    // that each of those support methods would have to do to get the stack.
    // Anyway, we ran performance tests with and without the stack and when
    // it's needed and used often we found that performance could improve by
    // about 5%.
    //
    // NOTE - it would make sense to try to keep track of the "sync state"
    // of variables when there's a local and block copy. We gave it a quick
    // try, but ran into low level issues that weren't easy to resolve. The
    // code was quickly getting more complicated than it should have, so we
    // decided to back it out and try again later (when we have more time).
    //

    private ArrayList  node_references;
    private ArrayList  object_references;
    private String     source;
    private String     classname;

    private StringBuffer  header;
    private StringBuffer  body;
    private StringBuffer  trailer;
    private YoixStack     yoixstack;

    //
    // The bits in debug are used to control the debugging support that's
    // available in this class and elsewhere.
    //

    private int  debug = 0;

    //
    // Setting indented and commented to true helps when you're debugging,
    // but undoubtedly shouldn't be true by default. When addtags is false
    // we don't bother generating code the pushes tags onto the stack even
    // if the tags are included in the parse tree.
    //

    private boolean  indented = false;
    private boolean  commented = false;
    private boolean  addtags = false;

    //
    // The value assigned to localcopymodel should be 0 (no local variable
    // copies), 1 (copy int and double), or 2 (copy all local variables).
    // Copying all local variables (model 2) may not improve performance.
    // We eventually will investigate.
    // 

    private static final int  LOCALCOPYMODEL_NONE = 0;
    private static final int  LOCALCOPYMODEL_NUMBERS = 1;
    private static final int  LOCALCOPYMODEL_ALL = 2;

    private int  default_localcopymodel = LOCALCOPYMODEL_NUMBERS;
    private int  current_localcopymodel;

    //
    // Classes that we create must have unique names, so we generate their
    // names using a static integer constant that's incremented each time
    // a new name is generated. Names assigned to local variables, labels,
    // blocks, and methods only need to be unique in each class.
    //
    // NOTE - think we eventually should make this stuff more robust which
    // could let us reuse names when existing ones go out of scope. Won't
    // be too hard, but JVM behavior means we'll have to manage variables
    // by type (e.g., int, double, etc.).
    //

    private static int  next_class = 1;
    private int         next_block = 1;
    private int         next_variable = 1;
    private int         next_label = 1;
    private int         next_method = 1;
    private int         next_args = 0;

    //
    // We use these to keep track of labels that should be used by break
    // and continue statements.
    //

    private ArrayList  jump_data;

    //
    // These are initialized and used when we generate code for a switch
    // statement.
    //

    private ArrayList  switch_data;

    //
    // We use a HashMap to recover the name of the local variable assigned
    // to a block's storage array from an lvalue.
    //

    private HashMap  block_directory;

    //
    // Local variables are represented by Yoix dictionaries that define a
    // a small set of fields the compiler uses while it's generating code. 
    // Everytime we create a new representation of a local variable we also
    // register it in a HashMap that the compiler can use to determine if
    // an arbitrary YoixObject represents a local variable or not.
    // 

    private HashMap  local_directory;

    //
    // We use a HashMap to keep track of external methods and fields used by
    // the compiler.
    //

    private HashMap  extern_directory;

    //
    // If we're maintaining our own copy of local variables (int and double
    // only) we have to update their official "block storage" before we ask
    // the interpreter to do some things for us. We use synclocals to walk
    // a parse tree node and figure out which variables need to by synced.
    //

    private YoixCompilerSyncLocals  synclocals;

    //
    // A HashMap that we use to record YoixObjects that represent classes or
    // primitive data types.
    //

    private static HashMap  ALL_TYPES = new HashMap();

    static {
	ALL_TYPES.put(DOUBLE_TYPE, Boolean.TRUE);
	ALL_TYPES.put(INT_TYPE, Boolean.TRUE);
	ALL_TYPES.put(STRING_TYPE, Boolean.TRUE);
	ALL_TYPES.put(JAVAOBJECT_TYPE, Boolean.TRUE);
	ALL_TYPES.put(YOIXOBJECT_TYPE, Boolean.TRUE);
	ALL_TYPES.put(YOIXLVALUE_TYPE, Boolean.TRUE);
    }

    //
    // A HashMap used to recover extern definitions for the methods that are
    // used by the compiler.
    //

    private static HashMap  ALL_EXTERNS = new HashMap();

    static {
	ALL_EXTERNS.put(ABORT_BADOPERAND, EXTERN_ABORT_BADOPERAND);
	ALL_EXTERNS.put(ABORT_ILLEGALJUMP, EXTERN_ABORT_ILLEGALJUMP);
	ALL_EXTERNS.put(ABORT_TYPECHECK, EXTERN_ABORT_TYPECHECK);
	ALL_EXTERNS.put(ASSIGNOBJECT_BYINDEX, EXTERN_ASSIGNOBJECT_BYINDEX);
	ALL_EXTERNS.put(ASSIGNOBJECT_BYINDEX_VOID, EXTERN_ASSIGNOBJECT_BYINDEX_VOID);
	ALL_EXTERNS.put(ASSIGNOBJECT_BYLVALUE, EXTERN_ASSIGNOBJECT_BYLVALUE);
	ALL_EXTERNS.put(ASSIGNOBJECT_BYLVALUE_VOID, EXTERN_ASSIGNOBJECT_BYLVALUE_VOID);
	ALL_EXTERNS.put(BOOLEANVALUE_0, EXTERN_BOOLEANVALUE_0);
	ALL_EXTERNS.put(BOOLEANVALUE_1, EXTERN_BOOLEANVALUE_1);
	ALL_EXTERNS.put(DECLAREVARIABLE, EXTERN_DECLAREVARIABLE);
	ALL_EXTERNS.put(DECLAREVARIABLE_VOID, EXTERN_DECLAREVARIABLE_VOID);
	ALL_EXTERNS.put(DOUBLEVALUE, EXTERN_DOUBLEVALUE);
	ALL_EXTERNS.put(EXPRESSION_ARITHMETIC, EXTERN_EXPRESSION_ARITHMETIC);
	ALL_EXTERNS.put(EXPRESSION_ATTRIBUTE, EXTERN_EXPRESSION_ATTRIBUTE);
	ALL_EXTERNS.put(EXPRESSION_BITWISE, EXTERN_EXPRESSION_BITWISE);
	ALL_EXTERNS.put(EXPRESSION_CAST, EXTERN_EXPRESSION_CAST);
	ALL_EXTERNS.put(EXPRESSION_INITIALIZER, EXTERN_EXPRESSION_INITIALIZER);
	ALL_EXTERNS.put(EXPRESSION_INSTANCEOF, EXTERN_EXPRESSION_INSTANCEOF);
	ALL_EXTERNS.put(EXPRESSION_NEW, EXTERN_EXPRESSION_NEW);
	ALL_EXTERNS.put(EXPRESSION_POSTDECREMENT_BYINDEX, EXTERN_EXPRESSION_POSTDECREMENT_BYINDEX);
	ALL_EXTERNS.put(EXPRESSION_POSTDECREMENT_BYINDEX_VOID, EXTERN_EXPRESSION_POSTDECREMENT_BYINDEX_VOID);
	ALL_EXTERNS.put(EXPRESSION_POSTDECREMENT_BYLVALUE, EXTERN_EXPRESSION_POSTDECREMENT_BYLVALUE);
	ALL_EXTERNS.put(EXPRESSION_POSTDECREMENT_BYLVALUE_VOID, EXTERN_EXPRESSION_POSTDECREMENT_BYLVALUE_VOID);
	ALL_EXTERNS.put(EXPRESSION_POSTINCREMENT_BYINDEX, EXTERN_EXPRESSION_POSTINCREMENT_BYINDEX);
	ALL_EXTERNS.put(EXPRESSION_POSTINCREMENT_BYINDEX_VOID, EXTERN_EXPRESSION_POSTINCREMENT_BYINDEX_VOID);
	ALL_EXTERNS.put(EXPRESSION_POSTINCREMENT_BYLVALUE, EXTERN_EXPRESSION_POSTINCREMENT_BYLVALUE);
	ALL_EXTERNS.put(EXPRESSION_POSTINCREMENT_BYLVALUE_VOID, EXTERN_EXPRESSION_POSTINCREMENT_BYLVALUE_VOID);
	ALL_EXTERNS.put(EXPRESSION_PREDECREMENT_BYINDEX, EXTERN_EXPRESSION_PREDECREMENT_BYINDEX);
	ALL_EXTERNS.put(EXPRESSION_PREDECREMENT_BYINDEX_VOID, EXTERN_EXPRESSION_PREDECREMENT_BYINDEX_VOID);
	ALL_EXTERNS.put(EXPRESSION_PREDECREMENT_BYLVALUE, EXTERN_EXPRESSION_PREDECREMENT_BYLVALUE);
	ALL_EXTERNS.put(EXPRESSION_PREDECREMENT_BYLVALUE_VOID, EXTERN_EXPRESSION_PREDECREMENT_BYLVALUE_VOID);
	ALL_EXTERNS.put(EXPRESSION_PREINCREMENT_BYINDEX, EXTERN_EXPRESSION_PREINCREMENT_BYINDEX);
	ALL_EXTERNS.put(EXPRESSION_PREINCREMENT_BYINDEX_VOID, EXTERN_EXPRESSION_PREINCREMENT_BYINDEX_VOID);
	ALL_EXTERNS.put(EXPRESSION_PREINCREMENT_BYLVALUE, EXTERN_EXPRESSION_PREINCREMENT_BYLVALUE);
	ALL_EXTERNS.put(EXPRESSION_PREINCREMENT_BYLVALUE_VOID, EXTERN_EXPRESSION_PREINCREMENT_BYLVALUE_VOID);
	ALL_EXTERNS.put(EXPRESSION_RELATIONAL, EXTERN_EXPRESSION_RELATIONAL);
	ALL_EXTERNS.put(EXPRESSION_RERELATIONAL, EXTERN_EXPRESSION_RERELATIONAL);
	ALL_EXTERNS.put(EXPRESSION_SHIFT, EXTERN_EXPRESSION_SHIFT);
	ALL_EXTERNS.put(EXPRESSION_UNARY, EXTERN_EXPRESSION_UNARY);
	ALL_EXTERNS.put(FUNCTION_CALL, EXTERN_FUNCTION_CALL);
	ALL_EXTERNS.put(GETDOUBLE_BYINDEX_1, EXTERN_GETDOUBLE_BYINDEX_1);
	ALL_EXTERNS.put(GETDOUBLE_BYINDEX_2, EXTERN_GETDOUBLE_BYINDEX_2);
	ALL_EXTERNS.put(GETDOUBLE_BYNAME_1, EXTERN_GETDOUBLE_BYNAME_1);
	ALL_EXTERNS.put(GETDOUBLE_BYNAME_2, EXTERN_GETDOUBLE_BYNAME_2);
	ALL_EXTERNS.put(GETINT_BYINDEX_1, EXTERN_GETINT_BYINDEX_1);
	ALL_EXTERNS.put(GETINT_BYINDEX_2, EXTERN_GETINT_BYINDEX_2);
	ALL_EXTERNS.put(GETINT_BYNAME_1, EXTERN_GETINT_BYNAME_1);
	ALL_EXTERNS.put(GETINT_BYNAME_2, EXTERN_GETINT_BYNAME_2);
	ALL_EXTERNS.put(GETOBJECT_BYINDEX, EXTERN_GETOBJECT_BYINDEX);
	ALL_EXTERNS.put(GETOBJECT_BYNAME, EXTERN_GETOBJECT_BYNAME);
	ALL_EXTERNS.put(INTVALUE, EXTERN_INTVALUE);
	ALL_EXTERNS.put(LVALUE_BYEXPRESSION, EXTERN_LVALUE_BYEXPRESSION);
	ALL_EXTERNS.put(LVALUE_BYNAME, EXTERN_LVALUE_BYNAME);
	ALL_EXTERNS.put(LVALUE_HANDLER, EXTERN_LVALUE_HANDLER);
	ALL_EXTERNS.put(LVALUE_INCREMENT, EXTERN_LVALUE_INCREMENT);
	ALL_EXTERNS.put(NEWARRAY, EXTERN_NEWARRAY);
	ALL_EXTERNS.put(NEWBLOCKLVALUE_BYLEVEL, EXTERN_NEWBLOCKLVALUE_BYLEVEL);
	ALL_EXTERNS.put(NEWBLOCKLVALUE_BYNAME, EXTERN_NEWBLOCKLVALUE_BYNAME);
	ALL_EXTERNS.put(NEWDOUBLE, EXTERN_NEWDOUBLE);
	ALL_EXTERNS.put(NEWEMPTY, EXTERN_NEWEMPTY);
	ALL_EXTERNS.put(NEWGLOBALLVALUE, EXTERN_NEWGLOBALLVALUE);
	ALL_EXTERNS.put(NEWINT, EXTERN_NEWINT);
	ALL_EXTERNS.put(NEWSTRING, EXTERN_NEWSTRING);
	ALL_EXTERNS.put(NEWTHISLVALUE, EXTERN_NEWTHISLVALUE);
	ALL_EXTERNS.put(PICKSWITCHSTATMENTINDEX, EXTERN_PICKSWITCHSTATMENTINDEX);
	ALL_EXTERNS.put(POPFOREACHBLOCK, EXTERN_POPFOREACHBLOCK);
	ALL_EXTERNS.put(POPTAG, EXTERN_POPTAG);
	ALL_EXTERNS.put(PUSHFOREACHBLOCK, EXTERN_PUSHFOREACHBLOCK);
	ALL_EXTERNS.put(PUSHTAG, EXTERN_PUSHTAG);
	ALL_EXTERNS.put(STATEMENT_BEGINCOMPOUND, EXTERN_STATEMENT_BEGINCOMPOUND);
	ALL_EXTERNS.put(STATEMENT_ENDCOMPOUND, EXTERN_STATEMENT_ENDCOMPOUND);
	ALL_EXTERNS.put(STATEMENT_EOF, EXTERN_STATEMENT_EOF);
	ALL_EXTERNS.put(STATEMENT_EXIT, EXTERN_STATEMENT_EXIT);
	ALL_EXTERNS.put(STATEMENT_FINALLY, EXTERN_STATEMENT_FINALLY);
	ALL_EXTERNS.put(STATEMENT_FUNCTION, EXTERN_STATEMENT_FUNCTION);
	ALL_EXTERNS.put(STATEMENT_IMPORT, EXTERN_STATEMENT_IMPORT);
	ALL_EXTERNS.put(STATEMENT_INCLUDE, EXTERN_STATEMENT_INCLUDE);
	ALL_EXTERNS.put(STATEMENT_NAMEDBLOCK, EXTERN_STATEMENT_NAMEDBLOCK);
	ALL_EXTERNS.put(STATEMENT_QUALIFIER, EXTERN_STATEMENT_QUALIFIER);
	ALL_EXTERNS.put(STATEMENT_SAVE, EXTERN_STATEMENT_SAVE);
	ALL_EXTERNS.put(STATEMENT_SYNCHRONIZED, EXTERN_STATEMENT_SYNCHRONIZED);
	ALL_EXTERNS.put(STATEMENT_TRY, EXTERN_STATEMENT_TRY);
	ALL_EXTERNS.put(STATEMENT_TYPEDEF, EXTERN_STATEMENT_TYPEDEF);
	ALL_EXTERNS.put(STOREDOUBLE, EXTERN_STOREDOUBLE);
	ALL_EXTERNS.put(STOREINT, EXTERN_STOREINT);
	ALL_EXTERNS.put(STOREOBJECT, EXTERN_STOREOBJECT);
	ALL_EXTERNS.put(TYPECHECK_IFNOTPOINTER, EXTERN_TYPECHECK_IFNOTPOINTER);

	ALL_EXTERNS.put(GETLENGTH, EXTERN_GETLENGTH);
	ALL_EXTERNS.put(GETOFFSET, EXTERN_GETOFFSET);
	ALL_EXTERNS.put(GETSIZEOF, EXTERN_GETSIZEOF);
	ALL_EXTERNS.put(RESOLVE, EXTERN_RESOLVE);
	ALL_EXTERNS.put(RESOLVECLONE, EXTERN_RESOLVECLONE);
    }

    //
    // We occasionally examine the last few assembly language instructions
    // generated by the compiler and when possible simplify them using the
    // following regular expressions.
    //
    // NOTE - we undoubtedly could reduce some of the compiler's overhead
    // by generating an intermediate format - maybe later.
    //
    // NOTE - we probably can eliminate the need for this one with a little
    // extra work in methods like expressionArithmetic(). Seems to happen
    // when compiling something like:
    //
    //     int poiuy = 1000;
    //
    //     External(int arg) {
    //         arg - poiuy;
    //     }
    //
    // We eventually will take a look, but it's not urgent.
    //

    private static final String  REGEX_EXCH2 = "^.*\\n(\\s*" + NAME_EXCH + "\\n\\s*" + NAME_EXCH + "\\n$)";

    private static final Pattern  PATTERN_EXCH2 = Pattern.compile(REGEX_EXCH2, Pattern.DOTALL);

    //
    // Operator precedences that we may need when we're asked whether an
    // expression used to control branching can use techniques that make
    // the generated code more efficient than it would be when we need to
    // fully evaluate the entire expression.
    //

    private static final int  PRECEDENCE_LOGICALAND = YoixParser.getPrecedence(LOGICALAND);

    //
    // A class loader used to define all compiled functions and scripts.
    //

    private static JVMClassLoader  compiled_class_loader = new JVMClassLoader();

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixCompiler() {

    }

    ///////////////////////////////////
    //
    // YoixCompiler Methods
    //
    ///////////////////////////////////

    final synchronized String
    compileFunction(String methodname, YoixObject names, YoixObject args, SimpleNode tree) {

	//
	// This method is called to compile a function into its own class
	// file. The string returned to the caller is an assembly language
	// description of that class file that still has to be processed by
	// the jvma assembler.
	//

	resetCompiler();

	if (tree != null) {
	    yoixstack.pushMark();
	    node_references = new ArrayList();
	    object_references = new ArrayList();
	    classname = getNextClassName();
	    jump_data = new ArrayList();
	    switch_data = new ArrayList();
	    block_directory = new HashMap();
	    local_directory = new HashMap();
	    extern_directory = new HashMap();
	    synclocals = new YoixCompilerSyncLocals(this, yoixstack);
	    header = new StringBuffer();
	    body = new StringBuffer();
	    trailer = new StringBuffer();
	    current_localcopymodel = default_localcopymodel;

	    header.append("public class ");
	    header.append(classname);
	    header.append(" {\n");

	    try {
		compileFunctionInto(methodname, names, args, tree, body);
		header.append("\n");
		trailer.append("}\n");
		source = header.toString() + body.toString() + trailer.toString();
		if (isDebugBitSet(DEBUG_COMPILER_OUTPUT))
		    System.err.println(source);
	    }
	    catch(Throwable t) {
		if (isDebugBitSet(DEBUG_COMPILER_ERRORS)) {
		    if (t instanceof YoixError) {
			System.err.println(((YoixError)t).getDetails().getString(N_MESSAGE));
			System.err.println(((YoixError)t).getDetails().getString(N_JAVATRACE));
		    } else {
			System.err.println(t.getMessage());
			if (t instanceof JVMAssemblerError)
			    System.err.println(((JVMAssemblerError)t).getDetails());
			System.err.println(YoixMisc.javaTrace(t));
		    }
		}
	    }
	    finally {
		header = null;
		body = null;
		trailer = null;
	    }
	    yoixstack.popMark();
	}

	return(source);
    }


    final synchronized String
    compileScript(String path) {

	SimpleNode  tree;

	resetCompiler();

	if ((classname = pickClassNameForPath(path)) != null) {
	    if ((tree = translateScript(path)) != null) {
		yoixstack.pushMark();
		node_references = new ArrayList();
		object_references = new ArrayList();
		jump_data = new ArrayList();
		switch_data = new ArrayList();
		block_directory = new HashMap();
		local_directory = new HashMap();
		extern_directory = new HashMap();
		synclocals = new YoixCompilerSyncLocals(this, yoixstack);
		header = new StringBuffer();
		body = new StringBuffer();
		trailer = new StringBuffer();
		current_localcopymodel = default_localcopymodel;

		header.append("public class ");
		header.append(classname);
		header.append(" {\n");

		//
		// Create the static copies of the nodes and objects arrays
		// that are filled in when the compiled script is executed.
		// They're only accessed by YoixBodyFunction.link(), which
		// is automatically called whenever a function is defined
		// and it looks like there's a method in the class that we
		// build that implements the function.
		//

		if (indented)
		    header.append("    ");
		header.append("public static ");
		header.append(SIMPLENODE_ARRAY);
		header.append(" ");
		header.append(NAMEOF_NODES_ARGUMENT);
		header.append("\n");

		if (indented)
		    header.append("    ");
		header.append("public static ");
		header.append(YOIXOBJECT_ARRAY);
		header.append(" ");
		header.append(NAMEOF_OBJECTS_ARGUMENT);
		header.append("\n");

		header.append("\n");

		try {
		    compileScriptInto(path, tree, body);
		    header.append("\n");
		    header.append("\n");
		    trailer.append("}\n");
		    source = header.toString() + body.toString() + trailer.toString();
		    if (isDebugBitSet(DEBUG_COMPILER_OUTPUT))
			System.err.println(source);
		}
		catch(Throwable t) {
		    if (isDebugBitSet(DEBUG_COMPILER_ERRORS)) {
			if (t instanceof YoixError) {
			    System.err.println(((YoixError)t).getDetails().getString(N_MESSAGE));
			    System.err.println(((YoixError)t).getDetails().getString(N_JAVATRACE));
			} else {
			    System.err.println(t.getMessage());
			    if (t instanceof JVMAssemblerError)
				System.err.println(((JVMAssemblerError)t).getDetails());
			    System.err.println(YoixMisc.javaTrace(t));
			}
		    }
		}
		finally {
		    header = null;
		    body = null;
		    trailer = null;
		}
		yoixstack.popMark();
	    }
	}

	return(source);
    }


    final void
    disableAllBlockLocalVariables(boolean permanent, StringBuffer sbuf) {

	YoixObject  names;
	String      blockname;
	int         level;

	//
	// Disables local variable copies in all current blocks and, when
	// permanent is true, in all future blocks.
	//

	for (level = 0; (names = YoixBodyBlock.getBlockNames(level)) != null; level++) {
	    if ((blockname = (String)block_directory.get(names)) != null)
		disableBlockLocalVariables(blockname, sbuf);
	}
	if (permanent)
	    current_localcopymodel = LOCALCOPYMODEL_NONE;
    }


    final void
    disableBlockLocalVariables(YoixObject lval, StringBuffer sbuf) {

	YoixObject  obj = lval.resolve();

	if (isLocalVariable(obj))
	    disableBlockLocalVariables(obj.getString(BLOCKNAME), sbuf);
    }


    static final JVMClassLoader
    getClassLoader() {

	return(compiled_class_loader);
    }


    final synchronized String
    getClassName() {

	return(classname);
    }


    final synchronized SimpleNode[]
    getNodeReferences() {

	SimpleNode  references[] = null;
	int         length;
	int         n;

	if ((length = node_references.size()) > 0) {
	    references = new SimpleNode[length];
	    for (n = 0; n < length; n++)
		references[n] = (SimpleNode)node_references.get(n);
	}

	return(references);
    }


    final synchronized YoixObject[]
    getObjectReferences() {

	YoixObject  references[] = null;
	int         length;
	int         n;

	if ((length = object_references.size()) > 0) {
	    references = new YoixObject[length];
	    for (n = 0; n < length; n++)
		references[n] = (YoixObject)object_references.get(n);
	}

	return(references);
    }


    final synchronized String
    getSource() {

	return(source);
    }


    final boolean
    isDebugBitSet(int mask) {

	return((debug & mask) == mask);
    }


    final boolean
    isDisableAllReservedName(String name) {

	//
	// Returns true if name references a reserved builtin that should
	// trigger a disabling of all local variable copies. Right now we
	// only recognize N_EVAL, but N_EXECUTE may also be a candidate.
	// 

	return(N_EVAL.equals(name));
    }


    final void
    setAddTags(boolean state) {

	addtags = state;
    }


    final void
    setDebug(int value) {

	debug = value;
    }


    final void
    setLocalCopyModel(int model) {

	switch (model) {
	    case LOCALCOPYMODEL_NONE:
	    case LOCALCOPYMODEL_NUMBERS:
	    case LOCALCOPYMODEL_ALL:
		default_localcopymodel = model;
		break;
	}
    }


    final void
    syncLocalVariable(YoixObject lval, boolean disable, HashMap synced, boolean modify, HashMap modified, StringBuffer sbuf) {

	YoixObject  obj;

	if (lval.defined()) {
	    obj = lval.resolve();
	    if (isCopiedLocalVariable(obj)) {
		if (synced == null || synced.containsKey(obj) == false) {
		    addSyncLocalVariableFromCopy(obj, sbuf);
		    if (disable)
			disableLocalVariableCopy(obj);
		    if (synced != null)
			synced.put(obj, Boolean.TRUE);
		}
		if (modify && modified != null)
		    modified.put(obj, Boolean.TRUE);
	    }
	}
    }


    public String
    toString() {

	StringBuffer  sbuf = new StringBuffer();

	sbuf.append(getClass().getName());
	sbuf.append("[");
	sbuf.append("version=").append(COMPILER_VERSION);
	sbuf.append(", timestamp=").append(COMPILER_TIMESTAMP);
	sbuf.append(", addtags=").append(addtags);
	sbuf.append(", debug=").append(debug);
	sbuf.append(", localcopymodel=").append(default_localcopymodel);
	sbuf.append("]");

	return(sbuf.toString());
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    addCastToStrictBoolean(StringBuffer sbuf) {

	YoixObject  obj;
	String      labels[];

	//
	// This method guarantees that the value that ends up on the stack
	// is 0 or 1. It's usually overkill because addCastToWeakBoolean(),
	// which generates less code, is all you really need.
	// 

	obj = yoixstack.peekYoixObject();
	if (obj.isNumber()) {
	    yoixstack.popYoixObject();
	    addPush(obj.booleanValue(), sbuf);
	} else if (obj == YOIXLVALUE_TYPE) {
	    yoixstack.popYoixObject();
	    addInvoke(RESOLVE, 1, YOIXOBJECT_TYPE, sbuf);
	    addInvoke(BOOLEANVALUE_1, 1, INT_TYPE, sbuf);
	} else if (obj == INT_TYPE || obj == DOUBLE_TYPE) {
	    if (obj == DOUBLE_TYPE) {
		addPush(0.0, sbuf);
		addInstruction(NAME_DCMPL, sbuf);
	    }
	    labels = getNextBranchLabelNames(2);
	    addIf(NAME_IFNE, labels, sbuf);
	    addPush(false, sbuf);
	    addIfBranch(labels, true, sbuf);
	    addPush(true, sbuf);
	    addIfJoin(labels, sbuf);
	} else {
	    addCastToYoixObject(sbuf);
	    addInvoke(BOOLEANVALUE_1, 1, INT_TYPE, sbuf);
	}
    }


    private void
    addCastToWeakBoolean(StringBuffer sbuf) {

	YoixObject  obj;

	//
	// This method guarantees that an integer will end up on the stack
	// that can be used by NAME_IFEQ or NAME_IFNE instructions to pick
	// the appropriate branch.
	//

	obj = yoixstack.peekYoixObject();
	if (obj.isNumber()) {
	    yoixstack.popYoixObject();
	    addPush(obj.booleanValue(), sbuf);
	} else if (obj == YOIXLVALUE_TYPE) {
	    yoixstack.popYoixObject();
	    addInvoke(RESOLVE, 1, YOIXOBJECT_TYPE, sbuf);
	    addInvoke(BOOLEANVALUE_1, 1, INT_TYPE, sbuf);
	} else if (obj == DOUBLE_TYPE) {
	    addPush(0.0, sbuf);
	    addInstruction(NAME_DCMPL, sbuf);
	} else if (obj != INT_TYPE) {
	    addCastToYoixObject(sbuf);
	    addInvoke(BOOLEANVALUE_1, 1, INT_TYPE, sbuf);
	}
    }


    private void
    addCastToYoixObject(StringBuffer sbuf) {

	YoixObject  obj;

	if ((obj = yoixstack.peekYoixObject()) != YOIXOBJECT_TYPE) {
	    if (obj.isNumber()) {
		yoixstack.popYoixObject();
		addReferenceToObject(obj, sbuf);
	    } else if (obj == YOIXLVALUE_TYPE) {
		yoixstack.popYoixObject();
		addInvoke(RESOLVE, 1, YOIXOBJECT_TYPE, sbuf);
	    } else if (obj == INT_TYPE)
		addInvoke(NEWINT, 1, YOIXOBJECT_TYPE, sbuf);
	    else if (obj == DOUBLE_TYPE)
		addInvoke(NEWDOUBLE, 1, YOIXOBJECT_TYPE, sbuf);
	    else if (obj == STRING_TYPE)
		addInvoke(NEWSTRING, 1, YOIXOBJECT_TYPE, sbuf);
	    else VM.abort(COMPILERERROR);
	}
    }


    private void
    addComment(String text, boolean force, StringBuffer sbuf) {

	String  lines[];
	int     index;
	int     n;

	//
	// Trailing newlines in text are not discarded because split() is
	// called with a negative second argument.
	//

	if (commented || force) {
	    if (text != null) {
		if ((lines = text.split("\\n", -1)) != null) {
		    if ((index = sbuf.length() - 1) >= 0) {
			if (sbuf.charAt(index) != '\n')
			    sbuf.append("\n");
		    }
		    for (n = 0; n < lines.length; n++) {
			if (indented)
			    sbuf.append("\t");
			sbuf.append("// ");
			sbuf.append(lines[n]);
			if (lines[n].endsWith("\n") == false)
			    sbuf.append("\n");
		    }
		}
	    }
	}
    }


    private void
    addCompiledFunction(SimpleNode node, StringBuffer sbuf) {

	YoixObject  names;
	YoixObject  values;
	SimpleNode  params;
	SimpleNode  param;
	SimpleNode  tree;
	String      name;
	String      methodname;
	int         argc;
	int         index;
	int         n;

	//
	// We'll eventually provide real control over the the decision to
	// compile functions.
	//

	if (true) {
	    methodname = getNextMethodName();
	    node.jjtAppendChild(YoixObject.newString(classname + "." + methodname));
	    index = (node.getChild0().type() != NAME) ? 1 : 0;
	    name = node.getChild(index++).stringValue();
	    params = node.getChild(index++);
	    tree = node.getChild(index++);
	    argc = params.length() + 1;

	    names = YoixObject.newDictionary(argc - 1);
	    values = YoixObject.newArray(argc);
	    values.put(0, YoixObject.newStringConstant(name), false);

	    for (n = 1; n < argc; n++) {
		param = params.getChild(n - 1);
		if (param.type() == DECLARATION) {
		    values.put(n, YoixMake.yoixInstance(param.getChild0().stringValue()), false);
		    names.put(param.getChild1().stringValue(), YoixObject.newInt(n), false);
		} else {
		    values.putNull(n);
		    names.put(param.stringValue(), YoixObject.newInt(n));
		}
	    }

	    sbuf.append("\n");
	    compileFunctionInto(methodname, names, values, tree, sbuf);
	}
    }


    private void
    addEvaluate(StringBuffer sbuf) {

	if (yoixstack.peekYoixObject() == YOIXLVALUE_TYPE)
	    addInvoke(RESOLVE, 1, YOIXOBJECT_TYPE, sbuf);
	else addPush(yoixstack.popRvalue(), sbuf);
    }


    private boolean
    addEvaluateBinaryOperands(int op, StringBuffer sbuf) {

	YoixObject  left;
	YoixObject  right;
	boolean     flipped = false;

	//
	// The final selection of some operators (e.g., LT, LE, GT, GE) is
	// handled by the caller based on the boolean we return, so they're
	// omitted from the switch statement that's supposed to address the
	// operators commutativity.
	//

	right = yoixstack.peekYoixObject(0);
	left = yoixstack.peekYoixObject(1);

	if (isConstantValue(right) == false) {
	    if (isResolvedTypeName(right) == false)
		addEvaluate(sbuf);

	    if (isResolvedTypeName(left) == false) {
		if (left != YOIXLVALUE_TYPE)
		    yoixstack.exchange();
		else addInstruction(NAME_EXCH, sbuf);
		addEvaluate(sbuf);
		switch (op) {
		    case DIV:
		    case DIVEQ:
		    case MINUS:
		    case MINUSEQ:
		    case MOD:
		    case MODEQ:
		    case LEFTSHIFT:
		    case LEFTSHIFTEQ:
		    case RIGHTSHIFT:
		    case RIGHTSHIFTEQ:
		    case UNSIGNEDSHIFT:
		    case UNSIGNEDSHIFTEQ:
		    case EQTILDA:
		    case NETILDA:
			//
			// Relational operators are intentionally omitted.
			//
			addInstruction(NAME_EXCH, sbuf);
			break;

		    default:
			flipped = true;
			break;
		}
	    }
	} else {
	    yoixstack.popYoixObject();
	    addEvaluate(sbuf);
	    addPush(right, sbuf);
	}

	return(flipped);
    }


    private void
    addEvaluateToWeakBoolean(StringBuffer sbuf) {

	addEvaluate(sbuf);
	addCastToWeakBoolean(sbuf);
    }


    private void
    addEvaluateToYoixObject(StringBuffer sbuf) {

	YoixObject  obj;

	//
	// A little trickier than you might expect.
	//

	if (yoixstack.peekYoixObject() != YOIXLVALUE_TYPE) {
	    obj = yoixstack.popRvalue();
	    if (isLocalVariable(obj)) {
		if (isCopiedLocalVariable(obj) == false) {
		    addPushLocalBlock(obj, sbuf);
		    addPushLocalIndex(obj, sbuf);
		    addInvoke(GETOBJECT_BYINDEX, 2, YOIXOBJECT_TYPE,  sbuf);
		} else {
		    addPush(obj, sbuf);
		    addCastToYoixObject(sbuf);
		}
	    } else {
		addPush(obj, sbuf);
		addCastToYoixObject(sbuf);
	    }
	} else addInvoke(RESOLVE, 1, YOIXOBJECT_TYPE, sbuf);
    }


    private void
    addLvalue(StringBuffer sbuf) {

	YoixObject  lval;
	YoixObject  obj;
	YoixObject  names;
	String      blockname;
	int         level;

	//
	// Should we try to hide more block level stuff in YoixBodyBlock??
	// If so then the code in several methods will need attention. Not
	// a big deal right now, and it's all probably OK. Perhaps the big
	// complaint is that using getBlockNames() the way we do here means
	// our algorithm is O(n^2) in the number of active blocks, but we
	// really don't think it's much of an issue because there usually
	// won't be many local blocks.
	//

	if (yoixstack.peekYoixObject() != YOIXLVALUE_TYPE) {
	    lval = yoixstack.popYoixObject();
	    obj = lval.resolve();
	    if (isLocalVariable(obj)) {
		blockname = obj.getString(BLOCKNAME);
		for (level = 0; (names = YoixBodyBlock.getBlockNames(level)) != null; level++) {
		    if (blockname.equals((String)block_directory.get(names))) {
			addPush(level, sbuf);
			addPushLocalIndex(obj, sbuf);
			addInvoke(NEWBLOCKLVALUE_BYLEVEL, 2, YOIXLVALUE_TYPE, sbuf);
			break;
		    }
		}
	    } else if (YoixBodyBlock.isReservedLvalue(lval)) {		// should we accept everything here??
		//
		// This could trigger serialization errors so might needs a
		// close look. Serialization, if we're really using it, will
		// only be needed when we're compiling a script, so function
		// compilation won't have any problems. We will investigate
		// in a little while.
		//
		addPush(lval.name(), sbuf);
		addInvoke(NEWBLOCKLVALUE_BYNAME, 1, YOIXLVALUE_TYPE, sbuf);
	    } else VM.abort(COMPILERERROR);	// is this really an error?
	}
    }


    private void
    addExchCastToStrictBoolean(boolean commutative, StringBuffer sbuf) {

	addInstruction(NAME_EXCH, sbuf);
	addCastToStrictBoolean(sbuf);
	if (commutative == false)
	    addInstruction(NAME_EXCH, sbuf);
    }


    private void
    addExchCastToWeakBoolean(boolean commutative, StringBuffer sbuf) {

	addInstruction(NAME_EXCH, sbuf);
	addCastToWeakBoolean(sbuf);
	if (commutative == false)
	    addInstruction(NAME_EXCH, sbuf);
    }


    private boolean
    addExchCastToYoixObject(boolean commutative, StringBuffer sbuf) {

	YoixObject  left;
	YoixObject  right;
	boolean     flipped = false;

	//
	// We assume that the object on top of the stack represents a type,
	// so this method must not be called until that assumption is true.
	//

	right = yoixstack.peekYoixObject(0);

	if (isTypeName(right)) {
	    if ((left = yoixstack.peekYoixObject(1)) != null) {
		if (left.isNumber()) {
		    yoixstack.collapse();
		    addReferenceToObject(left, sbuf);
		    if (commutative == false)
			addInstruction(NAME_EXCH, sbuf);
		    else flipped = true;
		} else if (left == YOIXLVALUE_TYPE) {
		    addInstruction(NAME_EXCH, sbuf);
		    addInvoke(RESOLVE, 1, YOIXOBJECT_TYPE, sbuf);
		    if (commutative == false)
			addInstruction(NAME_EXCH, sbuf);
		    else flipped = true;
		} else if (left == INT_TYPE) {
		    addInstruction(NAME_EXCH, sbuf);
		    addInvoke(NEWINT, 1, YOIXOBJECT_TYPE, sbuf);
		    if (commutative == false)
			addInstruction(NAME_EXCH, sbuf);
		    else flipped = true;
		} else if (left == DOUBLE_TYPE) {
		    addInstruction(NAME_EXCH, sbuf);
		    addInvoke(NEWDOUBLE, 1, YOIXOBJECT_TYPE, sbuf);
		    if (commutative == false)
			addInstruction(NAME_EXCH, sbuf);
		    else flipped = true;
		} else if (left == STRING_TYPE) {
		    addInstruction(NAME_EXCH, sbuf);
		    addInvoke(NEWSTRING, 1, YOIXOBJECT_TYPE, sbuf);
		    if (commutative == false)
			addInstruction(NAME_EXCH, sbuf);
		    else flipped = true;
		} else if (left != YOIXOBJECT_TYPE)
		    VM.abort(COMPILERERROR);
	    } else VM.abort(COMPILERERROR);
	} else VM.abort(COMPILERERROR);

	return(flipped);
    }


    private void
    addIf(String name, String labels[], StringBuffer sbuf) {

	addInstruction(name, labels[0], sbuf);
    }


    private void
    addIfBranch(String labels[], boolean expression, StringBuffer sbuf) {

	//
	// This can be used to start the "true" branch of an if statement.
	//
	// NOTE - if we're working on an expression we pop yoixstack because
	// the code that follows should leave the same result of the stack
	// (otherwise the JVM validation would eventually fail).
	//

	addInstruction(NAME_GOTO, labels[1], sbuf);
	addLabel(labels[0], sbuf);
	if (expression)
	    yoixstack.popYoixObject();
    }


    private void
    addIfJoin(String labels[], StringBuffer sbuf) {

	addLabel(labels[labels.length - 1], sbuf);
    }


    private void
    addIncrementLocalIntegerVariable(YoixObject obj, int incr, StringBuffer sbuf) {

	if (isCopiedLocalVariable(obj)) {
	    if (obj.getObject(LOCALTYPE) == INT_TYPE)
		addInstruction(NAME_IINC, obj.getString(LOCALNAME), incr, sbuf);
	    else VM.abort(COMPILERERROR);
	} else VM.abort(COMPILERERROR);
    }


    private void
    addIndexRegisterDeclaration(YoixObject obj, StringBuffer sbuf) {

	YoixObject  register;

	if (isLocalVariable(obj)) {
	    if ((register = obj.getObject(INDEXREGISTER)) != null && register.notNull()) {
		if (indented)
		    sbuf.append("\t");
		sbuf.append(JAVAINT);
		sbuf.append(" ");
		sbuf.append(register.stringValue());
		sbuf.append("\n");
	    }
	}
    }


    private void
    addInstruction(String name, StringBuffer sbuf) {

	YoixObject  right;
	YoixObject  left;
	Matcher     matcher;

	if (indented)
	    sbuf.append("\t");
	sbuf.append(name);
	sbuf.append("\n");

	switch (JVMMisc.getOpcodeFor(name)) {
	    case OP_ADD:
	    case OP_DIV:
	    case OP_MUL:
	    case OP_REM:
	    case OP_SUB:
		right = yoixstack.popYoixObject();
		left = yoixstack.popYoixObject();
		yoixstack.pushYoixObject(right == INT_TYPE && left == INT_TYPE ? INT_TYPE : DOUBLE_TYPE);
		break;

	    case OP_AND:
	    case OP_OR:
	    case OP_XOR:
		yoixstack.popYoixObject();
		yoixstack.popYoixObject();
		yoixstack.pushYoixObject(INT_TYPE);
		break;

	    case OP_ARRAYSTORE:
		yoixstack.popYoixObject();
		yoixstack.popYoixObject();
		yoixstack.popYoixObject();
		break;

	    case OP_CAST2D:
		yoixstack.popYoixObject();
		yoixstack.pushYoixObject(DOUBLE_TYPE);
		break;

	    case OP_CAST2I:
		yoixstack.popYoixObject();
		yoixstack.pushYoixObject(INT_TYPE);
		break;

	    case OP_DCMPG:
	    case OP_DCMPL:
		yoixstack.popYoixObject();
		yoixstack.popYoixObject();
		yoixstack.pushYoixObject(INT_TYPE);
		break;

	    case OP_DUP:
		yoixstack.pushYoixObject(yoixstack.peekYoixObject());
		break;

	    case OP_DUPX:
		//
		// YoixStack.duplicateExchange() clones, which will disrupt
		// our type checking, so don't use it!!
		//
		right = yoixstack.popYoixObject();
		left = yoixstack.popYoixObject();
		yoixstack.pushYoixObject(right);
		yoixstack.pushYoixObject(left);
		yoixstack.pushYoixObject(right);
		break;

	    case OP_EXCH:
		//
		// We remove consecutive exch instructions.
		//
		matcher = PATTERN_EXCH2.matcher(sbuf);
		if (matcher.find())
		    sbuf.replace(matcher.start(1), matcher.end(1), "");
		right = yoixstack.popYoixObject();
		left = yoixstack.popYoixObject();
		yoixstack.pushYoixObject(right);
		yoixstack.pushYoixObject(left);
		break;

	    case OP_NEG:
		break;

	    case OP_POP:
		yoixstack.popYoixObject();
		break;

	    case OP_RETURN:
		//
		// Not exactly certain what to do here??
		//
		yoixstack.popYoixObject();
		break;

	    case OP_SHL:
	    case OP_SHR:
	    case OP_USHR:
		//
		// Seems wrong - definitely needs a closer look!!
		//
		yoixstack.popYoixObject();
		break;

	    default:
		//
		// This is temporary and only for development.
		//
		VM.warn(COMPILERERROR, new String[] {UNIMPLEMENTED, name});
		break;
	}
    }


    private void
    addInstruction(String name, String arg, StringBuffer sbuf) {

	if (indented)
	    sbuf.append("\t");
	sbuf.append(name);
	if (arg != null) {
	    sbuf.append(" ");
	    sbuf.append(arg);
	}
	sbuf.append("\n");

	switch (JVMMisc.getOpcodeFor(name)) {
	    case OP_ANEWARRAY:
		yoixstack.popYoixObject();
		yoixstack.pushYoixObject(JAVAOBJECT_TYPE);
		break;

	    case OP_GOTO:
		break;

	    case OP_IFEQ:
	    case OP_IFNE:
	    case OP_IFGT:
	    case OP_IFGE:
	    case OP_IFLT:
	    case OP_IFLE:
	    case OP_STORE:
		yoixstack.popYoixObject();
		break;

	    case OP_IF_ICMPLT:
	    case OP_IF_ICMPLE:
	    case OP_IF_ICMPGT:
	    case OP_IF_ICMPGE:
	    case OP_IF_ICMPEQ:
	    case OP_IF_ICMPNE:
		yoixstack.popYoixObject();
		yoixstack.popYoixObject();
		break;

	    default:
		//
		// This is temporary and only for development.
		//
		VM.warn(COMPILERERROR, new String[] {UNIMPLEMENTED, name});
		break;
	}
    }


    private void
    addInstruction(String name, String arg, YoixObject type, StringBuffer sbuf) {

	if (indented)
	    sbuf.append("\t");
	sbuf.append(name);
	if (arg != null) {
	    sbuf.append(" ");
	    sbuf.append(arg);
	}
	sbuf.append("\n");

	switch (JVMMisc.getOpcodeFor(name)) {
	    case OP_AALOAD:
		yoixstack.popYoixObject();
		yoixstack.popYoixObject();
		yoixstack.pushYoixObject(type);
		break;

	    case OP_CHECKCAST:
		yoixstack.popYoixObject();
		yoixstack.pushYoixObject(type);
		break;

	    default:
		//
		// This is temporary and only for development.
		//
		VM.warn(COMPILERERROR, new String[] {UNIMPLEMENTED, name});
		break;
	}
    }


    private void
    addInstruction(String name, String arg1, int arg2, StringBuffer sbuf) {

	if (indented)
	    sbuf.append("\t");
	sbuf.append(name);
	sbuf.append(" ");
	sbuf.append(arg1);
	sbuf.append(" ");
	sbuf.append(arg2);
	sbuf.append("\n");

	switch (JVMMisc.getOpcodeFor(name)) {
	    case OP_IINC:
		break;

	    default:
		//
		// This is temporary and only for development.
		//
		VM.warn(COMPILERERROR, new String[] {UNIMPLEMENTED, name});
		break;
	}
    }


    private void
    addInvoke(String method, int argc, YoixObject type, StringBuffer sbuf) {

	int  n;

	if (indented)
	    sbuf.append("\t");
	sbuf.append(NAME_INVOKE);
	sbuf.append(" ");
	sbuf.append(method);
	sbuf.append("\n");

	for (n = 0; n < argc; n++)
	    yoixstack.popYoixObject();
	if (type != null)
	    yoixstack.pushYoixObject(type);
	registerMethod(method);
    }


    private void
    addLabel(String label, StringBuffer sbuf) {

	int  index;

	if ((index = sbuf.length() - 1) >= 0 && sbuf.charAt(index) != '\n')
	    sbuf.append("\n");
	sbuf.append(label);
	sbuf.append(":");
	if (indented == false)
	    sbuf.append("\n");
    }


    private void
    addLocalBlockDeclaration(String blockname, StringBuffer sbuf) {

	if (indented)
	    sbuf.append("\t");
	sbuf.append(YOIXOBJECT);
	sbuf.append(" ");
	sbuf.append(blockname);
	sbuf.append("\n");
    }


    private void
    addLocalBlockDeclaration(String blockname, YoixObject names, YoixObject values, StringBuffer sbuf) {

	if (names != null)
	    block_directory.put(names, blockname);
	if (values != null)
	    block_directory.put(values.body(), blockname);

	addLocalBlockDeclaration(blockname, sbuf);
    }


    private void
    addLocalVariableDeclaration(YoixObject obj, boolean initialize, StringBuffer sbuf) {

	if (isLocalVariable(obj)) {
	    if (isLocalVariableCopied(obj)) {
		if (indented)
		    sbuf.append("\t");
		sbuf.append(obj.getString(LOCALTYPE));
		sbuf.append(" ");
		sbuf.append(obj.getString(LOCALNAME));
		sbuf.append("\n");
		if (initialize)
		    addSyncLocalVariableFromBlock(obj, sbuf);
	    }
	} else VM.abort(COMPILERERROR);
    }


    private void
    addPush(boolean value, StringBuffer sbuf) {

	if (indented)
	    sbuf.append("\t");
	sbuf.append(NAME_PUSH);
	sbuf.append(" ");
	sbuf.append(value ? "1" : "0");
	sbuf.append("\n");

	yoixstack.pushYoixObject(INT_TYPE);
    }


    private void
    addPush(int value, StringBuffer sbuf) {

	if (indented)
	    sbuf.append("\t");
	sbuf.append(NAME_PUSH);
	sbuf.append(" ");
	sbuf.append(value);
	sbuf.append("\n");

	yoixstack.pushYoixObject(INT_TYPE);
    }


    private void
    addPush(long value, StringBuffer sbuf) {

	//
	// The interpreter doesn't support longs, but occasionally we need
	// to push constant long values (0l and -1L) that are used with a
	// a few operators that normally only work with integers but were
	// extended to mean something reasonable when applied to doubles.
	// In those cases doubles are cast to longs and operated on by the
	// long versions of those operators and the result is immediately
	// cast back to a double. Anyway, we're going to push INT_TYPE onto
	// the stack when this method is called even though it's technically
	// a long.
	//
	// NOTE - we could define a LONG_TYPE object for the compiler, but
	// right now that seems like overkill.
	// 

	if (indented)
	    sbuf.append("\t");
	sbuf.append(NAME_PUSH);
	sbuf.append(" ");
	sbuf.append(value);
	sbuf.append("L");
	sbuf.append("\n");

	yoixstack.pushYoixObject(INT_TYPE);
    }


    private void
    addPush(double value, StringBuffer sbuf) {

	//
	// We eventually may need to deal with NaN and Infinity, but the
	// assembler will have to accept them first. Not urgent, but will
	// have to be looked at before the compiler goes into production.
	//

	if (indented)
	    sbuf.append("\t");
	sbuf.append(NAME_PUSH);
	sbuf.append(" ");
	sbuf.append(value);
	sbuf.append("\n");

	yoixstack.pushYoixObject(DOUBLE_TYPE);
    }


    private void
    addPush(String value, StringBuffer sbuf) {

	if (indented)
	    sbuf.append("\t");
	sbuf.append(NAME_PUSH);
	sbuf.append(" ");
	sbuf.append("\"");
	sbuf.append(value);
	sbuf.append("\"");
	sbuf.append("\n");

	yoixstack.pushYoixObject(STRING_TYPE);
    }


    private void
    addPush(YoixObject obj, StringBuffer sbuf) {

	//
	// Not sure what to do when all explict checks of obj fail. Right
	// now we just call
	//
	//     addReferenceToObject(obj, sbuf);
	//
	// but if obj is a dictionary and later on we try to serialize it
	// we may end up having problems. At the very least we can expect
	// a serialization error because right now YoixBodyDictionary.java
	// Serializable and we should think hard before adding Serializable
	// to dictionaries!! At the very least it looks like we would have
	// make builtins and streams serializable if we expect to handle
	// the reserved dictionary - needs a bunch of thought and a careful
	// look at how the things in the object reference array are used!!
	//

	if (isTypeName(obj) == false) {
	    if (isLocalVariable(obj))
		addPushLocalVariable(obj, false, sbuf);
	    else if (obj.isInteger())
		addPush(obj.intValue(), sbuf);
	    else if (obj.isDouble())
		addPush(obj.doubleValue(), sbuf);
	    else if (obj.isString())
		addPush(obj.stringValue(), sbuf);
	    else if (obj.isNull())
		addReferenceToObject(obj, sbuf);
	    else VM.abort(COMPILERERROR);	// not convinced???
	} else yoixstack.pushYoixObject(obj);
    }


    private void
    addPushLocalBlock(YoixObject obj, StringBuffer sbuf) {

	if (indented)
	    sbuf.append("\t");
	sbuf.append(NAME_PUSH);
	sbuf.append(" ");
	sbuf.append(obj.getString(BLOCKNAME));
	sbuf.append("\n");

	yoixstack.pushYoixObject(YOIXOBJECT_TYPE);
    }


    private void
    addPushLocalIndex(YoixObject obj, StringBuffer sbuf) {

	YoixObject  register;
	int         index;

	if (isLocalVariable(obj)) {
	    if ((register = obj.getObject(INDEXREGISTER)) != null && register.notNull())
		addPushLocalVariable(register.stringValue(), INT_TYPE, sbuf);
	    else if ((index = obj.getInt(LOCALINDEX)) >= 0)
		addPush(index, sbuf);
	    else VM.abort(COMPILERERROR);
	} else VM.abort(COMPILERERROR);
    }


    private void
    addPushLocalVariable(YoixObject obj, StringBuffer sbuf) {

	addPushLocalVariable(obj, false, sbuf);
    }


    private void
    addPushLocalVariable(YoixObject obj, boolean byindex, StringBuffer sbuf) {

	YoixObject  localtype;

	if (isLocalVariable(obj)) {
	    localtype = obj.getObject(LOCALTYPE);
	    if (byindex || isLocalVariableCopied(obj) == false) {
		addPushLocalBlock(obj, sbuf);
		addPushLocalIndex(obj, sbuf);
		if (localtype == INT_TYPE)
		    addInvoke(GETINT_BYINDEX_1, 2, INT_TYPE, sbuf);
		else if (localtype == DOUBLE_TYPE)
		    addInvoke(GETDOUBLE_BYINDEX_1, 2, DOUBLE_TYPE, sbuf);
		else addInvoke(GETOBJECT_BYINDEX, 2, YOIXOBJECT_TYPE,  sbuf);
	    } else addPushLocalVariable(obj.getString(LOCALNAME), localtype, sbuf);
	} else VM.abort(COMPILERERROR);
    }


    private void
    addPushLocalVariable(String name, YoixObject type, StringBuffer sbuf) {

	if (indented)
	    sbuf.append("\t");
	sbuf.append(NAME_PUSH);
	sbuf.append(" ");
	sbuf.append(name);
	sbuf.append("\n");

	yoixstack.pushYoixObject(type);
    }


    private void
    addPushMethodArgument(String name, StringBuffer sbuf) {

	//
	// We intentionally omitted NAMEOF_ARGS_ARGUMENT even though it's
	// the name of one of the method arguments, because right now the
	// only way it's used is as the name that the compiler assigns to
	// the block that contains the function arguments.
	//

	if (name == NAMEOF_STACK_ARGUMENT || name == NAMEOF_NODES_ARGUMENT || name == NAMEOF_OBJECTS_ARGUMENT || name == NAMEOF_GLOBAL_ARGUMENT) {
	    if (indented)
		sbuf.append("\t");
	    sbuf.append(NAME_PUSH);
	    sbuf.append(" ");
	    sbuf.append(name);
	    sbuf.append("\n");

	    yoixstack.pushYoixObject(JAVAOBJECT_TYPE);
	} else VM.abort(COMPILERERROR);
    }


    private void
    addPushNull(StringBuffer sbuf) {

	//
	// Probably OK, but should take a closer look later on.
	//

	if (indented)
	    sbuf.append("\t");
	sbuf.append(NAME_PUSH);
	sbuf.append(" ");
	sbuf.append("null");
	sbuf.append("\n");

	yoixstack.pushYoixObject(JAVAOBJECT_TYPE);
    }


    private void
    addReferenceToNode(SimpleNode node, StringBuffer sbuf) {

	addReferenceToNode(getIndexOfNode(node), JAVAOBJECT_TYPE, sbuf);
    }


    private void
    addReferenceToNode(int index, YoixObject type, StringBuffer sbuf) {

	addPushMethodArgument(NAMEOF_NODES_ARGUMENT, sbuf);
	addPush(index, sbuf);
	addInstruction(NAME_AALOAD, null, type, sbuf);
    }


    private void
    addReferenceToObject(YoixObject obj, StringBuffer sbuf) {

	addReferenceToObject(getIndexOfObject(obj), YOIXOBJECT_TYPE, sbuf);
    }


    private void
    addReferenceToObject(int index, YoixObject type, StringBuffer sbuf) {

	addPushMethodArgument(NAMEOF_OBJECTS_ARGUMENT, sbuf);
	addPush(index, sbuf);
	addInstruction(NAME_AALOAD, null, type, sbuf);
    }


    private void
    addStack(StringBuffer sbuf) {

	addPushMethodArgument(NAMEOF_STACK_ARGUMENT, sbuf);
    }


    private void
    addStoreLocalBlock(YoixObject names, YoixObject values, StringBuffer sbuf) {

	String  blockname;

	if ((blockname = (String)block_directory.get(names)) != null)
	    addInstruction(NAME_STORE, blockname, sbuf);
	else if ((blockname = (String)block_directory.get(values)) != null)
	    addInstruction(NAME_STORE, blockname, sbuf);
	else VM.abort(COMPILERERROR);
    }


    private void
    addStoreIndexRegister(YoixObject obj, StringBuffer sbuf) {

	YoixObject  register;

	if (isLocalVariable(obj)) {
	    if ((register = obj.getObject(INDEXREGISTER)) != null && register.notNull())
		addInstruction(NAME_STORE, register.stringValue(), sbuf);
	} else VM.abort(COMPILERERROR);
    }


    private void
    addStoreLocalVariable(YoixObject obj, StringBuffer sbuf) {

	addStoreLocalVariable(obj, false, sbuf);
    }


    private void
    addStoreLocalVariable(YoixObject obj, boolean byindex, StringBuffer sbuf) {

	YoixObject  localname;
	YoixObject  localtype;

	//
	// Expect we'll eventually have to do more here if we're going to
	// keep track of the state of the local copy and block copy of a
	// local variable.
	//

	if (isLocalVariable(obj)) {
	    localname = obj.getObject(LOCALNAME);
	    localtype = obj.getObject(LOCALTYPE);
	    if (byindex || isLocalVariableCopied(obj) == false) {
		if (localtype == INT_TYPE) {
		    addPushLocalBlock(obj, sbuf);
		    addPushLocalIndex(obj, sbuf);
		    addInvoke(STOREINT, 3, null, sbuf);
		} else if (localtype == DOUBLE_TYPE) {
		    addPushLocalBlock(obj, sbuf);
		    addPushLocalIndex(obj, sbuf);
		    addInvoke(STOREDOUBLE, 3, null, sbuf);
		} else {
		    addCastToYoixObject(sbuf);
		    addPushLocalBlock(obj, sbuf);
		    addPushLocalIndex(obj, sbuf);
		    addInvoke(STOREOBJECT, 3, null,  sbuf);
		}
	    } else {
		addInstruction(NAME_STORE, localname.stringValue(), sbuf);
	    }
	} else VM.abort(COMPILERERROR);
    }


    private void
    addSwitchCase(String match, String label, StringBuffer sbuf) {

	if (indented)
	    sbuf.append("\t    ");
	sbuf.append(match);
	sbuf.append(" : ");
	sbuf.append(label);
	sbuf.append("\n");
    }

    private void
    addSwitchEnd(StringBuffer sbuf) {

	if (indented)
	    sbuf.append("\t");
	sbuf.append("}\n");

	yoixstack.popYoixObject();
    }


    private void
    addSwitchStart(StringBuffer sbuf) {

	if (indented)
	    sbuf.append("\t");
	sbuf.append(NAME_SWITCH);
	sbuf.append(" {\n");
    }


    private void
    addSyncLocalVariableFromBlock(YoixObject obj, StringBuffer sbuf) {

	if (isCopiedLocalVariable(obj)) {
	    addPushLocalVariable(obj, true, sbuf);
	    addStoreLocalVariable(obj, false, sbuf);
	}
    }


    private void
    addSyncLocalVariableFromCopy(YoixObject obj, StringBuffer sbuf) {

	if (isCopiedLocalVariable(obj)) {
	    addPushLocalVariable(obj, false, sbuf);
	    addStoreLocalVariable(obj, true, sbuf);
	}
    }


    private void
    beginFunctionScope(String blockname, YoixObject names, YoixObject args, StringBuffer sbuf) {

	YoixObject  values;
	YoixObject  obj;
	YoixObject  arg;
	int         index;
	int         n;

	//
	// NOTE - yoixstack must be coordinated with endFunctionScope().
	//

	values = YoixObject.newArray(args.length());
	registerBlock(blockname, names, values);
	for (n = 0; n < names.length(); n++) {
	    if ((index = names.getInt(n, -1)) >= 0) {
		if ((arg = args.getObject(index)) != null) {
		    obj = createLocalVariable(blockname, index, pickType(arg));
		    addLocalVariableDeclaration(obj, true, sbuf);
		    values.putObject(index, obj);
		}
	    }
	}

	yoixstack.pushGlobalBlock(true);	// an empty growable global block
	yoixstack.pushLocalBlock(names, values, false);
	YoixBodyBlock.setFunctionBlock();
    }


    private void
    beginScriptScope(String blockname, String name, StringBuffer sbuf) {

	YoixObject  names;

	//
	// Creating our own dictionary representations of some objects in a
	// global dictionary causes problems for the interpreter. Turns out
	// VM is the main culprit, although we actually haven't tracked down
	// exactly where things go wrong, but others could potentially cause
	// problems too. Anyway, just to be safe we decided that all objects
	// in default global dictionaries will be omitted from the dictionary
	// we use when we create our global scope.
	//
	// NOTE - yoixstack must be coordinated with endScriptScope().
	//

	names = YoixObject.newDictionary(0, -1);
	registerBlock(blockname, names, names);
	addLocalBlockDeclaration(blockname, sbuf);
	yoixstack.pushGlobalBlock(names);
    }


    private void
    branch(SimpleNode node, boolean condition, String label, StringBuffer sbuf) {

	SimpleNode  child;
	HashMap     map;
	Object      data[];
	int         length;
	int         op;
	int         n;

	//
	// NOTE - right now constant expressions can't really be simplified
	// using the various evaluate() methods because getBranchMap() didn't
	// account for them when it built the map HashMap. We eventually may
	// look into this, but it's probably not a big deal.
	//

	if ((map = getBranchMap(node, condition, label)) != null) {
	    if ((length = node.length()) > 0) {
		for (n = 0; n < length; n++) {
		    child = node.getChild(n);
		    data = (Object[])map.get(child);
		    switch (op = child.type()) {
			case ARRAY:
			case DICTIONARY:
			    expressionInitializer(child, sbuf);
			    break;

			case CAST:
			    expressionCast(sbuf);
			    break;

			case CONDITIONAL:
			    //
			    // We assume data isn't null and has three elements.
			    //
			    n++;	// skip LOGICALAND or LOGICALOR
			    branch(child, ((Boolean)data[0]).booleanValue(), (String)data[1], sbuf);
			    if (data.length > 2 && data[2] != null)
				addLabel((String)data[2], sbuf);
			    data = null;
			    break;

			case EXPRESSION:
			    if (data != null) {
				branch(child, ((Boolean)data[0]).booleanValue(), (String)data[1], sbuf);
				data = null;
			    } else expression(child, sbuf);
			    break;

			case LVALUE:
			    lvalue(child, sbuf);
			    break;

			case NAME:
			    //
			    // Think all we have to do here is save the name so it's
			    // available when method callExpressionCast() is called.
			    //
			    yoixstack.pushString(child.stringValue());
			    break;

			case NEW:
			    expressionNew(child, sbuf);
			    break;

			case NUMBER:
			    yoixstack.pushYoixObject((YoixObject)child);
			    break;

			case POINTER:
			    expressionPointer((YoixObject)child, sbuf);
			    break;

			case QUESTIONCOLON:
			    expressionQuestionColon(child, sbuf);
			    break;

			case ATTRIBUTE:
			    expressionAttribute(child, sbuf);
			    break;

			case UPLUS:
			case UMINUS:
			case COMPLEMENT:
			    expressionUnary(op, sbuf);
			    break;

			case NOT:
			    if (data != null) {
				branchNot(((Boolean)data[0]).booleanValue(), (String)data[1], sbuf);
				data = null;
			    } else expressionUnary(op, sbuf);
			    break;

			case POSTDECREMENT:
			    expressionPostIncrement(-1, sbuf);
			    break;

			case POSTINCREMENT:
			    expressionPostIncrement(1, sbuf);
			    break;

			case PREDECREMENT:
			    expressionPreIncrement(-1, sbuf);
			    break;

			case PREINCREMENT:
			    expressionPreIncrement(1, sbuf);
			    break;

			case PLUS:
			case MINUS:
			case MUL:
			case DIV:
			case MOD:
			    expressionArithmetic(op, sbuf);
			    break;

			case PLUSEQ:
			case MINUSEQ:
			case MULEQ:
			case DIVEQ:
			case MODEQ:
			    expressionArithmeticEQ(op, sbuf);
			    break;

			case LEFTSHIFT:
			case RIGHTSHIFT:
			case UNSIGNEDSHIFT:
			    expressionShift(op, sbuf);
			    break;

			case LEFTSHIFTEQ:
			case RIGHTSHIFTEQ:
			case UNSIGNEDSHIFTEQ:
			    expressionShiftEQ(op, sbuf);
			    break;

			case AND:
			case OR:
			case XOR:
			    expressionBitwise(op, sbuf);
			    break;

			case ANDEQ:
			case OREQ:
			case XOREQ:
			    expressionBitwiseEQ(op, sbuf);
			    break;

			case LT:
			case GT:
			case LE:
			case GE:
			case EQ:
			case NE:
			case EQEQ:
			case NEEQ:
			    if (data != null) {
				branchRelational(op, ((Boolean)data[0]).booleanValue(), (String)data[1], sbuf);
				data = null;
			    } else expressionRelational(op, sbuf);
			    break;

			case EQTILDA:
			case NETILDA:
			    expressionRERelational(op, sbuf);
			    break;

			case INSTANCEOF:
			    if (data != null) {
				branchInstanceof(((Boolean)data[0]).booleanValue(), (String)data[1], sbuf);
				data = null;
			    } else expressionInstanceof(sbuf);
			    break;

			case LOGICALXOR:
			    expressionLogicalXor(sbuf);
			    break;

			case ASSIGN:
			    expressionAssign(sbuf);
			    break;

			case COMMA:
			    expressionComma(sbuf);
			    break;

			default:
			    VM.die(INTERNALERROR);
			    break;
		    }

		    if (data != null) {
			addEvaluateToWeakBoolean(sbuf);
			addInstruction(((Boolean)data[0]).booleanValue() ? NAME_IFNE : NAME_IFEQ, (String)data[1], sbuf);
			if (data.length > 2)
			    addLabel((String)data[2], sbuf);
		    }
		}
	    } else addInvoke(NEWEMPTY, 0, YOIXOBJECT_TYPE, sbuf);
	} else {
	    expression(node, sbuf);
	    addEvaluateToWeakBoolean(sbuf);
	    addInstruction(condition ? NAME_IFNE : NAME_IFEQ, label, sbuf);
	}
    }


    private void
    branchInstanceof(boolean condition, String label, StringBuffer sbuf) {

	YoixObject  left;
	String      right;

	right = popString();
	addEvaluate(sbuf);
	left = yoixstack.peekYoixObject(0);

	if ((left == INT_TYPE || left == DOUBLE_TYPE) && (right.equals(T_INT) || right.equals(T_DOUBLE) || right.equals(T_NUMBER))) {
	    addInstruction(NAME_POP, sbuf);
	    if (left == INT_TYPE && right.equals(T_DOUBLE))
		addPush(false, sbuf);
	    else if (left == DOUBLE_TYPE && right.equals(T_INT))
		addPush(false, sbuf);
	    else addPush(true, sbuf);
	} else {
	    addCastToYoixObject(sbuf);
	    addPush(right, sbuf);
	    addStack(sbuf);
	    addInvoke(EXPRESSION_INSTANCEOF, 3, INT_TYPE, sbuf);
	    addCastToWeakBoolean(sbuf);
	}
	addInstruction(condition ? NAME_IFNE : NAME_IFEQ, label, sbuf);
    }


    private void
    branchNot(boolean condition, String label, StringBuffer sbuf) {

	YoixObject  right;

	addEvaluate(sbuf);
	right = yoixstack.peekYoixObject(0);

	if (right == INT_TYPE || right == DOUBLE_TYPE) {
	    if (right == DOUBLE_TYPE) {
		addPush(0.0, sbuf);
		addInstruction(NAME_DCMPL, sbuf);
	    }
	    addInstruction(condition ? NAME_IFEQ : NAME_IFNE, label, sbuf);
	} else {
	    addCastToYoixObject(sbuf);
	    addPush(NOT, sbuf);
	    addStack(sbuf);
	    addInvoke(EXPRESSION_UNARY, 3, YOIXOBJECT_TYPE, sbuf);
	    addCastToWeakBoolean(sbuf);
	    addInstruction(condition ? NAME_IFNE : NAME_IFEQ, label, sbuf);
	}
    }


    private void
    branchRelational(int op, boolean condition, String label, StringBuffer sbuf) {

	YoixObject  left;
	YoixObject  right;
	boolean     flipped;
	String      instruction = null;

	flipped = addEvaluateBinaryOperands(op, sbuf);

	right = yoixstack.peekYoixObject(0);
	left = yoixstack.peekYoixObject(1);

	if ((right == INT_TYPE || right == DOUBLE_TYPE) && (left == INT_TYPE || left == DOUBLE_TYPE)) {
	    if (flipped) {
		switch (op) {
		    case LT: op = GT; break;
		    case LE: op = GE; break;
		    case GT: op = LT; break;
		    case GE: op = LE; break;
		}
	    }
	    if (left == DOUBLE_TYPE || right == DOUBLE_TYPE) {
		switch (op) {
		    case LT:
			addInstruction(NAME_DCMPG, sbuf);
			instruction = condition ? NAME_IFLT : NAME_IFGE;
			break;

		    case LE:
			addInstruction(NAME_DCMPG, sbuf);
			instruction = condition ? NAME_IFLE : NAME_IFGT;
			break;

		    case GT:
			addInstruction(NAME_DCMPL, sbuf);
			instruction = condition ? NAME_IFGT : NAME_IFLE;
			break;

		    case GE:
			addInstruction(NAME_DCMPL, sbuf);
			instruction = condition ? NAME_IFGE : NAME_IFLT;
			break;

		    case EQ:
		    case EQEQ:
			addInstruction(NAME_DCMPL, sbuf);
			instruction = condition ? NAME_IFEQ : NAME_IFNE;
			break;

		    case NE:
		    case NEEQ:
			addInstruction(NAME_DCMPL, sbuf);
			instruction = condition ? NAME_IFNE : NAME_IFEQ;
			break;
		}
	    } else {
		switch (op) {
		    case LT:
			instruction = condition ? NAME_IF_ICMPLT : NAME_IF_ICMPGE;
			break;

		    case LE:
			instruction = condition ? NAME_IF_ICMPLE : NAME_IF_ICMPGT;
			break;

		    case GT:
			instruction = condition ? NAME_IF_ICMPGT : NAME_IF_ICMPLE;
			break;

		    case GE:
		        instruction = condition ? NAME_IF_ICMPGE : NAME_IF_ICMPLT;
		        break;

		    case EQ:
		    case EQEQ:
			instruction = condition ? NAME_IF_ICMPEQ : NAME_IF_ICMPNE;
			break;

		    case NE:
		    case NEEQ:
			instruction = condition ? NAME_IF_ICMPNE : NAME_IF_ICMPEQ;
			break;
		}
	    }
	} else {
	    //
	    // Flipping the instruction choice (which is clearly wrong) and
	    // then running one of our early non-trivial examples yields an
	    // interesting result in the 1.6 JVM - we eventually should look
	    // into it.
	    //
	    addCastToYoixObject(sbuf);
	    flipped ^= addExchCastToYoixObject(false, sbuf);

	    if (flipped) {
		switch (op) {
		    case LT: op = GT; break;
		    case LE: op = GE; break;
		    case GT: op = LT; break;
		    case GE: op = LE; break;
		}
	    }

	    addPush(op, sbuf);
	    addStack(sbuf);
	    addInvoke(EXPRESSION_RELATIONAL, 4, INT_TYPE, sbuf);
	    addCastToWeakBoolean(sbuf);
	    instruction = condition ? NAME_IFNE : NAME_IFEQ;
	}
	addInstruction(instruction, label, sbuf);
    }


    private void
    compileFunctionInto(String methodname, YoixObject names, YoixObject args, SimpleNode tree, StringBuffer sbuf) {

	String  argsname;

	if (tree != null) {
	    argsname = getNextArgsName();

	    if (indented)
		sbuf.append("    ");

	    sbuf.append("public static ");
	    sbuf.append(YOIXOBJECT);
	    sbuf.append(" ");
	    sbuf.append(methodname);
	    sbuf.append("(");
	    sbuf.append(YOIXOBJECT);
	    sbuf.append(" ");
	    sbuf.append(argsname);
	    sbuf.append(", ");
	    sbuf.append(YOIXSTACK);
	    sbuf.append(" ");
	    sbuf.append(NAMEOF_STACK_ARGUMENT);
	    sbuf.append(", ");
	    sbuf.append(SIMPLENODE_ARRAY);
	    sbuf.append(" ");
	    sbuf.append(NAMEOF_NODES_ARGUMENT);
	    sbuf.append(", ");
	    sbuf.append(YOIXOBJECT_ARRAY);
	    sbuf.append(" ");
	    sbuf.append(NAMEOF_OBJECTS_ARGUMENT);
	    sbuf.append(") {\n");

	    beginFunctionScope(argsname, names, args, sbuf);

	    switch (tree.type()) {
		case EXPRESSION:
		    expression(tree, sbuf);
		    expressionReturn(sbuf);
		    break;

		case COMPOUND:
		case STATEMENT:
		    statement(tree, sbuf);
		    break;
	    }

	    endFunctionScope(sbuf);

	    if (indented)
		sbuf.append("    ");
	    sbuf.append("}\n");
	}
    }


    private void
    compileScriptInto(String name, SimpleNode tree, StringBuffer sbuf) {

	String  argsname;

	if (tree != null) {
	    argsname = getNextArgsName();

	    if (indented)
		sbuf.append("    ");

	    //
	    // Right now the compiler assumes that all functions have the
	    // same signature, so even though the first argument won't be
	    // used we have to include one anyway.
	    //

	    sbuf.append("public static void ");
	    sbuf.append(COMPILED_SCRIPTNAME);
	    sbuf.append("(");
	    sbuf.append(YOIXOBJECT);
	    sbuf.append(" ");
	    sbuf.append(argsname);
	    sbuf.append(", ");
	    sbuf.append(YOIXSTACK);
	    sbuf.append(" ");
	    sbuf.append(NAMEOF_STACK_ARGUMENT);
	    sbuf.append(", ");
	    sbuf.append(SIMPLENODE_ARRAY);
	    sbuf.append(" ");
	    sbuf.append(NAMEOF_NODES_ARGUMENT);
	    sbuf.append(", ");
	    sbuf.append(YOIXOBJECT_ARRAY);
	    sbuf.append(" ");
	    sbuf.append(NAMEOF_OBJECTS_ARGUMENT);
	    sbuf.append(") {\n");

	    beginScriptScope(argsname, name, sbuf);

	    switch (tree.type()) {
		case EXPRESSION:
		    expression(tree, sbuf);
		    expressionReturn(sbuf);
		    break;

		case COMPOUND:
		case STATEMENT:
		    statement(tree, sbuf);
		    break;
	    }

	    endScriptScope(sbuf);

	    if (indented)
		sbuf.append("    ");
	    sbuf.append("}\n");
	}
    }


    private YoixObject
    createLocalVariable(YoixObject lval, String typename) {

	YoixObject  obj = null;
	String      blockname;
	int         index;

	if ((blockname = (String)block_directory.get(lval.body())) != null) {
	    obj = createLocalVariable(blockname, YoixBodyBlock.isGlobalLvalue(lval) ? -1 : lval.offset(), pickType(typename));
	    lval.put(obj);
	} else VM.abort(COMPILERERROR);

	return(obj);
    }


    private YoixObject
    createLocalVariable(String blockname, int index, YoixObject type) {

	YoixObject  obj = null;
	ArrayList   block;
	Integer     model;

	//
	// Assigning a non-null value to LOCALNAME means the compiler should
	// create and manage a local copy of the variable, which is something
	// we should only do when the variable is a number.
	//
	// NOTE - suspect we can also use local copies of YoixObjects as long
	// they're not const or final. The change here is easy, but right now
	// there are places in the code (e.g., expressionPostIncrement()) that
	// enforce the fact that only INT_TYPE and DOUBLE_TYPE have LOCALNAME
	// entries that aren't null.
	//

	if (blockname != null) {
	    if ((block = (ArrayList)block_directory.get(blockname)) != null) {
		obj = YoixObject.newDictionary(5);
		obj.putString(LOCALNAME, null);
		obj.putString(BLOCKNAME, blockname);
		obj.putInt(LOCALINDEX, index);
		obj.putObject(LOCALTYPE, type);
		obj.putString(INDEXREGISTER, null);
		if ((model = (Integer)block.get(0)) != null) {
		    switch (model.intValue()) {
			case LOCALCOPYMODEL_NUMBERS:
			    if (type == INT_TYPE || type == DOUBLE_TYPE)
				obj.putString(LOCALNAME, getNextLocalVariableName());
			    else obj.putString(LOCALNAME, null);
			    break;

			case LOCALCOPYMODEL_ALL:
			    obj.putString(LOCALNAME, getNextLocalVariableName());
			    break;
		    }
		}
		if (index < 0)
		    obj.putString(INDEXREGISTER, getNextLocalVariableName());
		registerLocalVariable(obj);
	    } else VM.abort(COMPILERERROR);
	} else VM.abort(COMPILERERROR);

	return(obj);
    }


    private void
    disableBlockLocalVariables(String blockname, StringBuffer sbuf) {

	YoixObject  obj;
	ArrayList   block;
	int         n;

	if ((block = (ArrayList)block_directory.get(blockname)) != null) {
	    block.set(0, new Integer(0));	// disables future copies
	    for (n = 1; n < block.size(); n++) {
		if ((obj = (YoixObject)block.get(n)) != null) {
		    if (isCopiedLocalVariable(obj)) {
			addSyncLocalVariableFromCopy(obj, sbuf);
			disableLocalVariableCopy(obj);
		    }
		}
	    }
	}
    }


    private void
    disableLocalVariableCopies(HashMap map) {

	YoixObject  obj;
	Iterator    iterator;
	Object      key;

	if (map != null) {
	    if ((iterator = map.keySet().iterator()) != null) {
		while (iterator.hasNext()) {
		    key = iterator.next();
		    if (key instanceof YoixObject)
			disableLocalVariableCopy((YoixObject)key);
		}
	    }
	}
    }


    private void
    disableLocalVariableCopy(YoixObject obj) {

	if (isCopiedLocalVariable(obj))
	    obj.putString(LOCALNAME, null);
    }


    private void
    endFunctionScope(StringBuffer sbuf) {

	//
	// Removes the local and global blocks that beginFunctionScope()
	// started.
	//

	yoixstack.popBlock();
	yoixstack.popBlock();
    }


    private void
    endScriptScope(StringBuffer sbuf) {

	//
	// Removes the global block that beginScriptScope() started.
	//

	yoixstack.popBlock();
    }


    private int
    evaluateExpression(SimpleNode node, int index) {

	SimpleNode  child;
	YoixObject  obj;
	boolean     evaluated;
	int         lastindex;
	int         operands;
	int         consumed;
	int         length;
	int         op;
	int         n;

	//
	// We evaluate constant expressions using the stack the compiler is
	// using when it generates code, so as a precaution we push a mark
	// before starting and remove it before we return. The mark means
	// tests that check to see if a possible operand is a number will
	// fail when they encounter the mark, no matter what the compiler
	// has put on the stack.
	//
	// NOTE - missing cases (e.g., LT, GT) were intentionally omitted
	// because we doubt they're used much in constant expressions, but
	// if you disagree feel free to add the required code.
	//

	evaluated = true;
	operands = 0;
	lastindex = -1;
	length = node.length();
	yoixstack.pushMark();

	for (n = index; n < length && evaluated; n++) {
	    child = node.getChild(n);
	    switch (op = child.type()) {
		case EXPRESSION:
		    if (evaluateExpression(child, 0) == child.length()) {
			if (++operands == 1)
			    lastindex = n;
		    } else evaluated = false;
		    break;

		case NUMBER:
		    yoixstack.pushYoixObject((YoixObject)child);
		    if (++operands == 1)
			lastindex = n;
		    break;

		case LVALUE:
		    if (evaluated = evaluateLvalue(child)) {
			if (++operands == 1)
			    lastindex = n;
		    }
		    break;

		case QUESTIONCOLON:
		    if (evaluated = evaluateQuestionColon(child)) {
			if (++operands == 1)
			    lastindex = n;
		    }
		    break;

		case UPLUS:
		case UMINUS:
		case COMPLEMENT:
		case NOT:
		    if (evaluated = evaluateExpressionUnary(op)) {
			if (operands == 1)
			    lastindex = n;
		    }
		    break;

		case PLUS:
		case MINUS:
		case MUL:
		case DIV:
		case MOD:
		    if (evaluated = evaluateExpressionArithmetic(op)) {
			if (--operands == 1)
			    lastindex = n;
		    }
		    break;

		case LEFTSHIFT:
		case RIGHTSHIFT:
		case UNSIGNEDSHIFT:
		    if (evaluated = evaluateExpressionShift(op)) {
			if (--operands == 1)
			    lastindex = n;
		    }
		    break;

		case AND:
		case OR:
		case XOR:
		    if (evaluated = evaluateExpressionBitwise(op)) {
			if (--operands == 1)
			    lastindex = n;
		    }
		    break;

		default:
		    evaluated = false;
		    break;
	    }
	}

	if (operands > 0) {
	    obj = yoixstack.peekYoixObject(operands - 1);
	    yoixstack.popMark();
	    yoixstack.pushYoixObject(obj);
	    consumed = lastindex - index + 1;
	} else {
	    yoixstack.popMark();
	    consumed = 0;
	}

	return(consumed);
    }


    private boolean
    evaluateExpressionArithmetic(int op) {

	YoixObject  left;
	YoixObject  right;
	boolean     evaluated = false;

	if (yoixstack.peekYoixObject(0).isNumber() && yoixstack.peekYoixObject(1).isNumber()) {
	    evaluated = true;
	    right = yoixstack.popYoixObject();
	    left = yoixstack.popYoixObject();
	    if (left.isInteger() && right.isInteger()) {
		switch (op) {
		    case PLUS:
			yoixstack.pushInt(left.intValue() + right.intValue());
			break;

		    case MINUS:
			yoixstack.pushInt(left.intValue() - right.intValue());
			break;

		    case MUL:
			yoixstack.pushInt(left.intValue() * right.intValue());
			break;

		    case DIV:
			if (right.intValue() != 0)
			    yoixstack.pushInt(left.intValue() / right.intValue());
			else evaluated = false;
			break;

		    case MOD:
			if (right.intValue() != 0)
			    yoixstack.pushInt(left.intValue() % right.intValue());
			else evaluated = false;
			break;
		}
	    } else {
		switch (op) {
		    case PLUS:
			yoixstack.pushDouble(left.doubleValue() + right.doubleValue());
			break;

		    case MINUS:
			yoixstack.pushDouble(left.doubleValue() - right.doubleValue());
			break;

		    case MUL:
			yoixstack.pushDouble(left.doubleValue() * right.doubleValue());
			break;

		    case DIV:
			yoixstack.pushDouble(left.doubleValue() / right.doubleValue());
			break;

		    case MOD:
			yoixstack.pushDouble(left.doubleValue() % right.doubleValue());
			break;
		}
	    }
	}

	return(evaluated);
    }


    private boolean
    evaluateExpressionBitwise(int op) {

	YoixObject  left;
	YoixObject  right;
	boolean     evaluated = false;

	if (yoixstack.peekYoixObject(0).isNumber() && yoixstack.peekYoixObject(1).isNumber()) {
	    evaluated = true;
	    right = yoixstack.popYoixObject();
	    left = yoixstack.popYoixObject();
	    if (left.isInteger() && right.isInteger()) {
		switch (op) {
		    case AND:
			yoixstack.pushInt(left.intValue() & right.intValue());
			break;

		    case OR:
			yoixstack.pushInt(left.intValue() | right.intValue());
			break;

		    case XOR:
			yoixstack.pushInt(left.intValue() ^ right.intValue());
			break;
		}
	    } else {
		switch (op) {
		    case AND:
			yoixstack.pushDouble(left.longValue() & right.longValue());
			break;

		    case OR:
			yoixstack.pushDouble(left.longValue() | right.longValue());
			break;

		    case XOR:
			yoixstack.pushDouble(left.longValue() ^ right.longValue());
			break;
		}
	    }
	}

	return(evaluated);
    }


    private boolean
    evaluateQuestionColon(SimpleNode node) {

	SimpleNode  child;
	boolean     evaluated = false;

	child = node.getChild0();
	if (evaluateExpression(child, 0) == child.length()) {
	    child = yoixstack.popYoixObject().booleanValue() ? node.getChild1() : node.getChild2();
	    if (evaluateExpression(child, 0) == child.length())
		evaluated = true;
	}

	return(evaluated);
    }


    private boolean
    evaluateExpressionShift(int op) {

	YoixObject  left;
	YoixObject  right;
	boolean     evaluated = false;
	int         shift;

	if (yoixstack.peekYoixObject(0).isNumber() && yoixstack.peekYoixObject(1).isNumber()) {
	    evaluated = true;
	    right = yoixstack.popYoixObject();
	    left = yoixstack.popYoixObject();
	    shift = right.intValue();
	    if (left.isInteger()) {
		switch (op) {
		    case LEFTSHIFT:
			if (shift > 0) {
			    if (shift < BITSIZE_INT)
				yoixstack.pushInt(left.intValue() << shift);
			    else yoixstack.pushInt(0);
			} else yoixstack.pushInt(left.intValue() >> -shift);
			break;

		    case RIGHTSHIFT:
			if (shift < 0) {
			    shift = -shift;
			    if (shift < BITSIZE_INT)
				yoixstack.pushInt(left.intValue() << shift);
			    else yoixstack.pushInt(0);
			} else yoixstack.pushInt(left.intValue() >> shift);
			break;

		    case UNSIGNEDSHIFT:
			if (shift < 0) {
			    shift = -shift;
			    if (shift < BITSIZE_INT)
				yoixstack.pushInt(left.intValue() << shift);
			    else yoixstack.pushInt(0);
			} else yoixstack.pushInt(left.intValue() >>> shift);
			break;
		}
	    } else {
		switch (op) {
		    case LEFTSHIFT:
			if (shift > 0) {
			    if (shift < BITSIZE_LONG)
				yoixstack.pushDouble(left.longValue() << shift);
			    else yoixstack.pushDouble(0);
			} else yoixstack.pushDouble(left.longValue() >> -shift);
			break;

		    case RIGHTSHIFT:
			if (shift < 0) {
			    shift = -shift;
			    if (shift < BITSIZE_LONG)
				yoixstack.pushDouble(left.longValue() << shift);
			    else yoixstack.pushDouble(0);
			} else yoixstack.pushDouble(left.longValue() >> shift);
			break;

		    case UNSIGNEDSHIFT:
			if (shift < 0) {
			    shift = -shift;
			    if (shift < BITSIZE_LONG)
				yoixstack.pushDouble(left.longValue() << shift);
			    else yoixstack.pushDouble(0);
			} else yoixstack.pushDouble(left.longValue() >>> shift);
			break;
		}
	    }
	}

	return(evaluated);
    }


    private boolean
    evaluateExpressionUnary(int op) {

	YoixObject  right;
	boolean     evaluated = false;

	if (yoixstack.peekYoixObject(0).isNumber()) {
	    evaluated = true;
	    right = yoixstack.popYoixObject();
	    if (right.isInteger()) {
		switch (op) {
		    case COMPLEMENT:
			yoixstack.pushInt(~right.intValue());
			break;

		    case NOT:
			yoixstack.pushInt(right.intValue() == 0);
			break;

		    case UMINUS:
			yoixstack.pushInt(-right.intValue());
			break;

		    case UPLUS:
			yoixstack.pushInt(right.intValue());
			break;
		}
	    } else {
		switch (op) {
		    case COMPLEMENT:
			yoixstack.pushDouble(~right.longValue());
			break;

		    case NOT:
			yoixstack.pushInt(right.doubleValue() == 0.0);
			break;

		    case UMINUS:
			yoixstack.pushDouble(-right.doubleValue());
			break;

		    case UPLUS:
			yoixstack.pushDouble(right.doubleValue());
			break;
		}
	    }
	}

	return(evaluated);
    }


    private boolean
    evaluateLvalue(SimpleNode node) {

	SimpleNode  child;
	boolean     evaluated = false;
	String      name;

	//
	// Right now we only check for TRUE, FALSE, and EOF, but it's not
	// hard to imagine extending this to int or double variables that
	// have been declared const or final and that are initialized by
	// constant expressions, but would require some changes elsewhere
	// in the compiler (e.g, in the dictionary used to represent local
	// variables).
	//

	if (node.length() == 1) {
	    child = node.getChild0();
	    if (child.type() == NAME) {
		name = child.stringValue();
		if (name.equals(N_TRUE) || name.equals(N_TRUE2)) {
		    yoixstack.pushInt(1);
		    evaluated = true;
		} else if (name.equals(N_FALSE) || name.equals(N_FALSE2)) {
		    yoixstack.pushInt(0);
		    evaluated = true;
		} else if (name.equals(N_EOF)) {
		    yoixstack.pushInt(YOIX_EOF);
		    evaluated = true;
		}
	    }
	}

	return(evaluated);
    }


    private void
    expression(SimpleNode node, StringBuffer sbuf) {

	expression(node, false, sbuf);
    }


    private void
    expression(SimpleNode node, boolean toss, StringBuffer sbuf) {

	SimpleNode  child;
	boolean     tosschild;
	int         length;
	int         consumed;
	int         op;
	int         n;

	//
	// If toss is true when we get to the last child we know the caller
	// doesn't want the result to be left on the stack, so in some cases
	// (e.g., assignment expressions) we're able to generate code that's
	// more efficient, otherwise we need to make sure we pop the result
	// off the appropriate stack (or stacks).
	//

	if ((length = node.length()) > 0) {
	    for (n = 0; n < length; n++) {
		child = node.getChild(n);
		tosschild = toss && (n == length - 1);
		switch (op = child.type()) {
		    case ARRAY:
		    case DICTIONARY:
			expressionInitializer(child, sbuf);
			break;

		    case CAST:
			expressionCast(sbuf);
			break;

		    case CONDITIONAL:
			expressionConditional(node.getChild(++n).type(), child, sbuf);
			break;

		    case EXPRESSION:
			if ((consumed = evaluateExpression(node, n)) > 0)
			    n += consumed - 1;
			else {
			    expression(child, tosschild, sbuf);
			    if (tosschild)
				toss = false;
			}
			break;

		    case LVALUE:
			lvalue(child, sbuf);
			break;

		    case NAME:
			//
			// Think all we have to do here is save the name so it's
			// available when method callExpressionCast() is called.
			//
			yoixstack.pushString(child.stringValue());
			break;

		    case NEW:
			expressionNew(child, sbuf);
			break;

		    case NUMBER:
			if ((consumed = evaluateExpression(node, n)) > 0)
			    n += consumed - 1;
			else yoixstack.pushYoixObject((YoixObject)child);
			break;

		    case POINTER:
			expressionPointer((YoixObject)child, sbuf);
			break;

		    case QUESTIONCOLON:
			if ((consumed = evaluateExpression(node, n)) > 0)
			    n += consumed - 1;
			else expressionQuestionColon(child, sbuf);
			break;

		    case ATTRIBUTE:
			expressionAttribute(child, sbuf);
			break;

		    case UPLUS:
		    case UMINUS:
		    case COMPLEMENT:
		    case NOT:
			expressionUnary(op, sbuf);
			break;

		    case POSTDECREMENT:
			expressionPostIncrement(-1, tosschild, sbuf);
			if (tosschild)
			    toss = false;
			break;

		    case POSTINCREMENT:
			expressionPostIncrement(1, tosschild, sbuf);
			if (tosschild)
			    toss = false;
			break;

		    case PREDECREMENT:
			expressionPreIncrement(-1, tosschild, sbuf);
			if (tosschild)
			    toss = false;
			break;

		    case PREINCREMENT:
			expressionPreIncrement(1, tosschild, sbuf);
			if (tosschild)
			    toss = false;
			break;

		    case PLUS:
		    case MINUS:
		    case MUL:
		    case DIV:
		    case MOD:
			expressionArithmetic(op, sbuf);
			break;

		    case PLUSEQ:
		    case MINUSEQ:
		    case MULEQ:
		    case DIVEQ:
		    case MODEQ:
			expressionArithmeticEQ(op, tosschild, sbuf);
			if (tosschild)
			    toss = false;
			break;

		    case LEFTSHIFT:
		    case RIGHTSHIFT:
		    case UNSIGNEDSHIFT:
			expressionShift(op, sbuf);
			break;

		    case LEFTSHIFTEQ:
		    case RIGHTSHIFTEQ:
		    case UNSIGNEDSHIFTEQ:
			expressionShiftEQ(op, tosschild, sbuf);
			if (tosschild)
			    toss = false;
			break;

		    case AND:
		    case OR:
		    case XOR:
			expressionBitwise(op, sbuf);
			break;

		    case ANDEQ:
		    case OREQ:
		    case XOREQ:
			expressionBitwiseEQ(op, tosschild, sbuf);
			if (tosschild)
			    toss = false;
			break;

		    case LT:
		    case GT:
		    case LE:
		    case GE:
		    case EQ:
		    case NE:
		    case EQEQ:
		    case NEEQ:
			expressionRelational(op, sbuf);
			break;

		    case EQTILDA:
		    case NETILDA:
			expressionRERelational(op, sbuf);
			break;

		    case INSTANCEOF:
			expressionInstanceof(sbuf);
			break;

		    case LOGICALXOR:
			expressionLogicalXor(sbuf);
			break;

		    case ASSIGN:
			expressionAssign(tosschild, sbuf);
			if (tosschild)
			    toss = false;
			break;

		    case COMMA:
			expressionComma(sbuf);
			break;

		    default:
			VM.die(INTERNALERROR);
			break;
		}
	    }
	} else addInvoke(NEWEMPTY, 0, YOIXOBJECT_TYPE, sbuf);

	if (toss) {
	    if (isTypeName(yoixstack.peekYoixObject()))
		addInstruction(NAME_POP, sbuf);
	    else yoixstack.popYoixObject();
	}
    }


    private void
    expressionArithmetic(int op, StringBuffer sbuf) {

	YoixObject  left;
	YoixObject  right;
	YoixObject  top;
	boolean     flipped;
	String      label;

	//
	// Older versions of the Yoix interpreter automatically converted
	// an integer division by zero to a division operation on doubles.
	// It was a questionable approach that resulted in the generation
	// of inefficient code when expressions involved integer division. 
	// As a result we changed the interpreter so it complains with a
	// badoperand error if the divisor is zero in an integer division,
	// so that's all we have to duplicate here.
	// 

	top = yoixstack.peekYoixObject(0);
	flipped = addEvaluateBinaryOperands(op, sbuf);

	right = yoixstack.peekYoixObject(0);
	left = yoixstack.peekYoixObject(1);

	if ((right == INT_TYPE || right == DOUBLE_TYPE) && (left == INT_TYPE || left == DOUBLE_TYPE)) {
	    switch (op) {
		case PLUS:
		case PLUSEQ:
		    addInstruction(NAME_ADD, sbuf);
		    break;

		case MINUS:
		case MINUSEQ:
		    addInstruction(NAME_SUB, sbuf);
		    break;

		case MUL:
		case MULEQ:
		    addInstruction(NAME_MUL, sbuf);
		    break;

		case DIV:
		case DIVEQ:
		    if (left == INT_TYPE && right == INT_TYPE) {
			if (top.isInteger() == false) {
			    label = getNextBranchLabelName();
			    addInstruction(NAME_DUP, sbuf);
			    addInstruction(NAME_IFNE, label, sbuf);
			    addInvoke(ABORT_BADOPERAND, 0, null, sbuf);
			    addLabel(label, sbuf);
			} else if (top.intValue() == 0)
			    addInvoke(ABORT_BADOPERAND, 0, null, sbuf);
		    }
		    addInstruction(NAME_DIV, sbuf);
		    break;

		case MOD:
		case MODEQ:
		    if (left == INT_TYPE && right == INT_TYPE) {
			if (top.isInteger() == false) {
			    label = getNextBranchLabelName();
			    addInstruction(NAME_DUP, sbuf);
			    addInstruction(NAME_IFNE, label, sbuf);
			    addInvoke(ABORT_BADOPERAND, 0, null, sbuf);
			    addLabel(label, sbuf);
			} else if (top.intValue() == 0)
			    addInvoke(ABORT_BADOPERAND, 0, null, sbuf);
		    }
		    addInstruction(NAME_REM, sbuf);
		    break;
	    }
	} else {
	    addCastToYoixObject(sbuf);
	    switch (op) {
		case PLUS:
		case PLUSEQ:
		    //
		    // String addition isn't commutative and since we might
		    // be dealing with strings we decided, for now anyway,
		    // to always restore the order of the operands on the
		    // stack. Telling addExchCastToYoixObject() that we're
		    // working on a commutative operator should eliminate
		    // unnecessary exchanges.
		    // 
		    flipped ^= addExchCastToYoixObject(true, sbuf);
		    if (flipped)
			addInstruction(NAME_EXCH, sbuf);
		    break;

		case MUL:
		case MULEQ:
		    addExchCastToYoixObject(true, sbuf);
		    break;

		default:
		    addExchCastToYoixObject(false, sbuf);
		    break;
	    }
	    addPush(op, sbuf);
	    addStack(sbuf);
	    addInvoke(EXPRESSION_ARITHMETIC, 4, YOIXOBJECT_TYPE, sbuf);
	}
    }


    private void
    expressionArithmeticEQ(int op, StringBuffer sbuf) {

	expressionArithmeticEQ(op, false, sbuf);
    }


    private void
    expressionArithmeticEQ(int op, boolean toss, StringBuffer sbuf) {

	YoixObject  left;
	YoixObject  right;

	addEvaluate(sbuf);
	right = yoixstack.peekYoixObject(0);
	left = yoixstack.peekYoixObject(1);

	if (left == YOIXLVALUE_TYPE) {
	    addInstruction(NAME_EXCH, sbuf);
	    addInstruction(NAME_DUPX, sbuf);
	} else yoixstack.pushYoixObject(left);

	switch (op) {
	    case DIVEQ:
	    case MINUSEQ:
	    case MODEQ:
		if (left == YOIXLVALUE_TYPE)
		    addInstruction(NAME_EXCH, sbuf);
		else yoixstack.exchange();
		break;

	    case PLUSEQ:	// only needed for for string addition
		//
		// Addition of strings isn't commutative, so to be safe if
		// the right operand isn't a number we'll restore the order
		// of the operands on the stack. Probably could do better,
		// but this should be sufficient for now.
		//
		if (right != INT_TYPE && right != DOUBLE_TYPE) {
		    if (left == YOIXLVALUE_TYPE)
			addInstruction(NAME_EXCH, sbuf);
		    else yoixstack.exchange();
		}
		break;
	}

	expressionArithmetic(op, sbuf);
	expressionAssign(toss, sbuf);
    }


    private void
    expressionAssign(StringBuffer sbuf) {

	expressionAssign(false, sbuf);
    }


    private void
    expressionAssign(boolean toss, StringBuffer sbuf) {

	YoixObject  left;
	YoixObject  right;
	YoixObject  obj;
	YoixObject  localname;
	YoixObject  localtype;

	addEvaluate(sbuf);
	right = yoixstack.peekYoixObject(0);
	left = yoixstack.peekYoixObject(1);

	if (left.isPointer() && !right.isEmpty()) {
	    obj = left.resolve();
	    if (isLocalVariable(obj)) {
		yoixstack.collapse();
		if (isLocalVariableCopied(obj)) {
		    localname = obj.getObject(LOCALNAME);
		    localtype = obj.getObject(LOCALTYPE);
		    if (localtype == INT_TYPE || localtype == DOUBLE_TYPE) {
			if (right == INT_TYPE || right == DOUBLE_TYPE) {
			    if (localtype != right) {
				if (localtype == INT_TYPE)
				    addInstruction(NAME_CAST2I, sbuf);
				else addInstruction(NAME_CAST2D, sbuf);
			    }
			    if (toss == false)
				addInstruction(NAME_DUP, sbuf);
			    addStoreLocalVariable(obj, sbuf);
			} else {
			    addCastToYoixObject(sbuf);
			    addPushLocalBlock(obj, sbuf);
			    addPushLocalIndex(obj, sbuf);
			    addInvoke(ASSIGNOBJECT_BYINDEX, 3, YOIXOBJECT_TYPE,  sbuf);
			    if (localtype == INT_TYPE)
				addInvoke(INTVALUE, 1, INT_TYPE, sbuf);
			    else addInvoke(DOUBLEVALUE, 1, DOUBLE_TYPE, sbuf);
			    if (toss == false)
				addInstruction(NAME_DUP, sbuf);
			    addStoreLocalVariable(obj, sbuf);
			}
		    } else {
			addCastToYoixObject(sbuf);
			addPushLocalBlock(obj, sbuf);
			addPushLocalIndex(obj, sbuf);
			if (toss)
			    addInvoke(ASSIGNOBJECT_BYINDEX_VOID, 3, null, sbuf);
			else addInvoke(ASSIGNOBJECT_BYINDEX, 3, YOIXOBJECT_TYPE, sbuf);
			//
			// Sync our local copy with the block storage.
			//
			addSyncLocalVariableFromBlock(obj, sbuf);
		    }
		} else {
		    addCastToYoixObject(sbuf);
		    addPushLocalBlock(obj, sbuf);
		    addPushLocalIndex(obj, sbuf);
		    if (toss)
			addInvoke(ASSIGNOBJECT_BYINDEX_VOID, 3, null, sbuf);
		    else addInvoke(ASSIGNOBJECT_BYINDEX, 3, YOIXOBJECT_TYPE, sbuf);
		}
	    } else {
		addCastToYoixObject(sbuf);
		if (left != YOIXLVALUE_TYPE) {
		    yoixstack.collapse();
		    yoixstack.pushYoixObject(left);
		    addLvalue(sbuf);
		    addInstruction(NAME_EXCH, sbuf);
		}
		if (toss)
		    addInvoke(ASSIGNOBJECT_BYLVALUE_VOID, 2, null, sbuf);
		else addInvoke(ASSIGNOBJECT_BYLVALUE, 2, YOIXOBJECT_TYPE, sbuf);
	    }
	} else {
	    //
	    // Looks like something's wrong - call abort or the interpreter's
	    // expressionAssign().
	    // 
	    VM.abort(BADOPERAND);
	}
    }


    private void
    expressionAttribute(SimpleNode node, StringBuffer sbuf) {

	SimpleNode  child;
	String      name;
	int         attribute;

	switch (attribute = node.getChild1().intValue()) {
	    case ATTRIBUTE_ACCESS:
	    case ATTRIBUTE_GROWABLE:
	    case ATTRIBUTE_MAJOR:
	    case ATTRIBUTE_MINOR:
	    case ATTRIBUTE_NAMEOF:
	    case ATTRIBUTE_TYPENAME:
		addReferenceToNode(node, sbuf);
		addStack(sbuf);
		addInvoke(EXPRESSION_ATTRIBUTE, 2, YOIXOBJECT_TYPE, sbuf);
		break;

	    default:
		child = node.getChild0();
		if (child.length() == 1 && child.getChild0().type() == NAME) {
		    name = child.getChild0().stringValue();
		    if (YoixBodyBlock.isLocalName(name))
			yoixstack.pushYoixObject(YoixBodyBlock.newLvalue(name));
		    else lvalue(child, sbuf);
		} else lvalue(child, sbuf);
		addEvaluateToYoixObject(sbuf);
		switch (attribute) {
		    case ATTRIBUTE_LENGTH:
			addInvoke(GETLENGTH, 1, INT_TYPE, sbuf);
			break;

		    case ATTRIBUTE_OFFSET:
			addInvoke(GETOFFSET, 1, INT_TYPE, sbuf);
			break;

		    case ATTRIBUTE_SIZEOF:
			addInvoke(GETSIZEOF, 1, INT_TYPE, sbuf);
			break;
		}
		break;
	}
    }


    private void
    expressionBitwise(int op, StringBuffer sbuf) {

	YoixObject  left;
	YoixObject  right;

	addEvaluateBinaryOperands(op, sbuf);

	right = yoixstack.peekYoixObject(0);
	left = yoixstack.peekYoixObject(1);

	if ((right == INT_TYPE || right == DOUBLE_TYPE) && (left == INT_TYPE || left == DOUBLE_TYPE)) {
	    switch (op) {
		case AND:
		case ANDEQ:
		    addInstruction(NAME_AND, sbuf);
		    break;

		case OR:
		case OREQ:
		    addInstruction(NAME_OR, sbuf);
		    break;

		case XOR:
		case XOREQ:
		    addInstruction(NAME_XOR, sbuf);
		    break;
	    }
	    if (right == DOUBLE_TYPE || left == DOUBLE_TYPE)
		addInstruction(NAME_CAST2D, sbuf);
	} else {
	    addCastToYoixObject(sbuf);
	    addExchCastToYoixObject(true, sbuf);
	    addPush(op, sbuf);
	    addStack(sbuf);
	    addInvoke(EXPRESSION_BITWISE, 4, YOIXOBJECT_TYPE, sbuf);
	}
    }


    private void
    expressionBitwiseEQ(int op, StringBuffer sbuf) {

	expressionBitwiseEQ(op, false, sbuf);
    }


    private void
    expressionBitwiseEQ(int op, boolean toss, StringBuffer sbuf) {

	YoixObject  left;

	addEvaluate(sbuf);
	left = yoixstack.peekYoixObject(1);

	if (left == YOIXLVALUE_TYPE) {
	    addInstruction(NAME_EXCH, sbuf);
	    addInstruction(NAME_DUPX, sbuf);
	} else yoixstack.pushYoixObject(left);

	expressionBitwise(op, sbuf);
	expressionAssign(toss, sbuf);
    }


    private void
    expressionCast(StringBuffer sbuf) {

	YoixObject  right;
	String      left;

	addEvaluate(sbuf);
	right = yoixstack.peekYoixObject(0);
	left = yoixstack.peekYoixObject(1).stringValue();
	yoixstack.collapse();

	if ((left.equals(T_INT) || left.equals(T_DOUBLE)) && (right == INT_TYPE || right == DOUBLE_TYPE)) {
	    if (left.equals(T_INT) && right == DOUBLE_TYPE)
		addInstruction(NAME_CAST2I, sbuf);
	    else if (left.equals(T_DOUBLE) && right == INT_TYPE)
		addInstruction(NAME_CAST2D, sbuf);
	} else {
	    addCastToYoixObject(sbuf);
	    addPush(left, sbuf);
	    addStack(sbuf);
	    addInvoke(EXPRESSION_CAST, 3, YOIXOBJECT_TYPE, sbuf);
	}
    }


    private void
    expressionComma(StringBuffer sbuf) {

	YoixObject  left;
	YoixObject  right;

	right = yoixstack.peekYoixObject(0);
	left = yoixstack.peekYoixObject(1);

	if (isTypeName(left) && isTypeName(right)) {
	    addInstruction(NAME_EXCH, sbuf);
	    addInstruction(NAME_POP, sbuf);
	} else if (isTypeName(right))
	    addInstruction(NAME_POP, sbuf);
	else if (isTypeName(left))
	    yoixstack.popYoixObject();
	else yoixstack.collapse();
    }


    private void
    expressionConditional(int op, SimpleNode cond, StringBuffer sbuf) {

	String  label;

	//
	// Pushing the default answer before we start lets us generate code
	// that uses one label and no gotos.
	//

	addEvaluate(sbuf);

	addPush(op != LOGICALAND, sbuf);		// the default answer
	addInstruction(NAME_EXCH, sbuf);

	addCastToWeakBoolean(sbuf);
	label = getNextBranchLabelName();
	addInstruction(op == LOGICALAND ? NAME_IFEQ : NAME_IFNE, label, sbuf);
	expression(cond, sbuf);
	addEvaluateToWeakBoolean(sbuf);
	addInstruction(op == LOGICALAND ? NAME_IFEQ : NAME_IFNE, label, sbuf);
	addInstruction(NAME_POP, sbuf);
	addPush(op == LOGICALAND ? true : false, sbuf);
	addLabel(label, sbuf);
    }


    private void
    expressionInitializer(SimpleNode node, StringBuffer sbuf) {

	//
	// Initializers should all be handled by declareVariable(), so this
	// this method probably is never called, which also means it hasn't
	// been tested.
	//

	addReferenceToNode(node, sbuf);
	addStack(sbuf);
	addInvoke(EXPRESSION_INITIALIZER, 2, YOIXOBJECT_TYPE, sbuf);
    }


    private void
    expressionInstanceof(StringBuffer sbuf) {

	YoixObject  left;
	String      right;

	right = popString();
	addEvaluate(sbuf);
	left = yoixstack.peekYoixObject(0);

	if ((left == INT_TYPE || left == DOUBLE_TYPE) && (right.equals(T_INT) || right.equals(T_DOUBLE) || right.equals(T_NUMBER))) {
	    addInstruction(NAME_POP, sbuf);
	    if (left == INT_TYPE && right.equals(T_DOUBLE))
		addPush(false, sbuf);
	    else if (left == DOUBLE_TYPE && right.equals(T_INT))
		addPush(false, sbuf);
	    else addPush(true, sbuf);
	} else {
	    addCastToYoixObject(sbuf);
	    addPush(right, sbuf);
	    addStack(sbuf);
	    addInvoke(EXPRESSION_INSTANCEOF, 3, INT_TYPE, sbuf);
	}
    }


    private void
    expressionLogicalXor(StringBuffer sbuf) {

	addCastToStrictBoolean(sbuf);
	addExchCastToStrictBoolean(true, sbuf);
	addInstruction(NAME_XOR, sbuf);
    }


    private void
    expressionName(String name, StringBuffer sbuf) {

	//
	// Don't think this should ever generate code, but we do have to
	// save the name on the stack in case the value is needed later
	// later on (e.g., in handling a cast or instanceof expression).
	//

	yoixstack.pushString(name);
    }


    private void
    expressionNew(SimpleNode node, StringBuffer sbuf) {

	HashMap  modified;

	modified = synclocals.syncNew(node, sbuf);
	addReferenceToNode(node, sbuf);
	addStack(sbuf);
	addInvoke(EXPRESSION_NEW, 2, YOIXOBJECT_TYPE, sbuf);
	restoreModifiedVariables(modified, sbuf);
    }


    private void
    expressionPointer(YoixObject obj, StringBuffer sbuf) {

	//
	// The only pointer objects we'll encounter in expression trees are
	// strings (and maybe NULL), so that's all we have to deal with here.
	//

	if (obj.isString()) {
	    if (obj.notNull())
		addPush(((YoixBodyString)(obj.body())).cstringValue(obj.offset()), sbuf);
	    else addPushNull(sbuf);
	} else if (obj.isNull())
	    addPushNull(sbuf);
	else VM.abort(COMPILERERROR);
    }


    private void
    expressionPostIncrement(int incr, StringBuffer sbuf) {

	expressionPostIncrement(incr, false, sbuf);
    }


    private void
    expressionPostIncrement(int incr, boolean toss, StringBuffer sbuf) {

	YoixObject  lval;
	YoixObject  obj;
	YoixObject  localtype;

	lval = yoixstack.peekYoixObject();

	if (lval.defined()) {
	    obj = lval.resolve();
	    if (isLocalVariable(obj)) {
		if (isLocalVariableCopied(obj)) {
		    localtype = obj.getObject(LOCALTYPE);
		    if (localtype == INT_TYPE) {
			if (toss) {
			    if (yoixstack.peekYoixObject() == YOIXLVALUE_TYPE)
				addInstruction(NAME_POP, sbuf);
			    else yoixstack.popYoixObject();
			} else addEvaluate(sbuf);
			addIncrementLocalIntegerVariable(obj, incr, sbuf);
		    } else if (localtype == DOUBLE_TYPE) {
			addEvaluate(sbuf);
			if (toss == false)
			    addInstruction(NAME_DUP, sbuf);
			addPush((double)incr, sbuf);
			addInstruction(NAME_ADD, sbuf);
			addStoreLocalVariable(obj, sbuf);
		    } else {
			yoixstack.popYoixObject();
			//
			// First sync block storage with our local copy.
			//
			addSyncLocalVariableFromCopy(obj, sbuf);
			addPushLocalBlock(obj, sbuf);
			addPushLocalIndex(obj, sbuf);
			addStack(sbuf);
			if (toss) {
			    if (incr > 0)
				addInvoke(EXPRESSION_POSTINCREMENT_BYINDEX_VOID, 3, null, sbuf);
			    else addInvoke(EXPRESSION_POSTDECREMENT_BYINDEX_VOID, 3, null, sbuf);
			} else {
			    if (incr > 0)
				addInvoke(EXPRESSION_POSTINCREMENT_BYINDEX, 3, YOIXOBJECT_TYPE, sbuf);
			    else addInvoke(EXPRESSION_POSTDECREMENT_BYINDEX, 3, YOIXOBJECT_TYPE, sbuf);
			}
			//
			// Then sync our local copy with the block storage.
			//
			addSyncLocalVariableFromBlock(obj, sbuf);
		    }
		} else {
		    yoixstack.popYoixObject();
		    addPushLocalBlock(obj, sbuf);
		    addPushLocalIndex(obj, sbuf);
		    addStack(sbuf);
		    if (toss) {
			if (incr > 0)
			    addInvoke(EXPRESSION_POSTINCREMENT_BYINDEX_VOID, 3, null, sbuf);
			else addInvoke(EXPRESSION_POSTDECREMENT_BYINDEX_VOID, 3, null, sbuf);
		    } else {
			if (incr > 0)
			    addInvoke(EXPRESSION_POSTINCREMENT_BYINDEX, 3, YOIXOBJECT_TYPE, sbuf);
			else addInvoke(EXPRESSION_POSTDECREMENT_BYINDEX, 3, YOIXOBJECT_TYPE, sbuf);
		    }
		}
	    } else {
		addLvalue(sbuf);
		addStack(sbuf);
		if (toss) {
		    if (incr > 0)
			addInvoke(EXPRESSION_POSTINCREMENT_BYLVALUE_VOID, 2, YOIXOBJECT_TYPE, sbuf);
		    else addInvoke(EXPRESSION_POSTDECREMENT_BYLVALUE_VOID, 2, YOIXOBJECT_TYPE, sbuf);
		} else {
		    if (incr > 0)
			addInvoke(EXPRESSION_POSTINCREMENT_BYLVALUE, 2, YOIXOBJECT_TYPE, sbuf);
		    else addInvoke(EXPRESSION_POSTDECREMENT_BYLVALUE, 2, YOIXOBJECT_TYPE, sbuf);
		}
	    }
	} else VM.abort(COMPILERERROR);
    }


    private void
    expressionPreIncrement(int incr, StringBuffer sbuf) {

	expressionPreIncrement(incr, false, sbuf);
    }


    private void
    expressionPreIncrement(int incr, boolean toss, StringBuffer sbuf) {

	YoixObject  lval;
	YoixObject  obj;
	YoixObject  localtype;

	lval = yoixstack.peekYoixObject();

	if (lval.defined()) {
	    obj = lval.resolve();
	    if (isLocalVariable(obj)) {
		if (isLocalVariableCopied(obj)) {
		    yoixstack.popYoixObject();
		    localtype = obj.getObject(LOCALTYPE);
		    if (localtype == INT_TYPE) {
			addIncrementLocalIntegerVariable(obj, incr, sbuf);
			if (toss == false)
			    addPushLocalVariable(obj.getString(LOCALNAME), localtype, sbuf);
		    } else if (localtype == DOUBLE_TYPE) {
			addPushLocalVariable(obj.getString(LOCALNAME), localtype, sbuf);
			addPush((double)incr, sbuf);
			addInstruction(NAME_ADD, sbuf);
			if (toss == false)
			    addInstruction(NAME_DUP, sbuf);
			addStoreLocalVariable(obj, sbuf);
		    } else {
			//
			// First sync block storage with our local copy.
			//
			addSyncLocalVariableFromCopy(obj, sbuf);
			addPushLocalBlock(obj, sbuf);
			addPushLocalIndex(obj, sbuf);
			addStack(sbuf);
			if (toss) {
			    if (incr > 0)
				addInvoke(EXPRESSION_PREINCREMENT_BYINDEX_VOID, 3, null, sbuf);
			    else addInvoke(EXPRESSION_PREDECREMENT_BYINDEX_VOID, 3, null, sbuf);
			} else {
			    if (incr > 0)
				addInvoke(EXPRESSION_PREINCREMENT_BYINDEX, 3, YOIXOBJECT_TYPE, sbuf);
			    else addInvoke(EXPRESSION_PREDECREMENT_BYINDEX, 3, YOIXOBJECT_TYPE, sbuf);
			}
			//
			// Then sync our local copy with the block storage.
			//
			addSyncLocalVariableFromBlock(obj, sbuf);	// value is on the stack so we could use DUP
		    }
		} else {
		    yoixstack.popYoixObject();
		    addPushLocalBlock(obj, sbuf);
		    addPushLocalIndex(obj, sbuf);
		    addStack(sbuf);
		    if (toss) {
			if (incr > 0)
			    addInvoke(EXPRESSION_PREINCREMENT_BYINDEX_VOID, 3, null, sbuf);
			else addInvoke(EXPRESSION_PREDECREMENT_BYINDEX_VOID, 3, null, sbuf);
		    } else {
			if (incr > 0)
			    addInvoke(EXPRESSION_PREINCREMENT_BYINDEX, 3, YOIXOBJECT_TYPE, sbuf);
			else addInvoke(EXPRESSION_PREDECREMENT_BYINDEX, 3, YOIXOBJECT_TYPE, sbuf);
		    }
		}
	    } else {
		addLvalue(sbuf);
		addStack(sbuf);
		if (toss) {
		    if (incr > 0)
			addInvoke(EXPRESSION_PREINCREMENT_BYLVALUE_VOID, 2, null, sbuf);
		    else addInvoke(EXPRESSION_PREDECREMENT_BYLVALUE_VOID, 2, null, sbuf);
		} else {
		    if (incr > 0)
			addInvoke(EXPRESSION_PREINCREMENT_BYLVALUE, 2, YOIXOBJECT_TYPE, sbuf);
		    else addInvoke(EXPRESSION_PREDECREMENT_BYLVALUE, 2, YOIXOBJECT_TYPE, sbuf);
		}
	    }
	} else VM.abort(COMPILERERROR);
    }


    private void
    expressionQuestionColon(SimpleNode node, StringBuffer sbuf) {

	StringBuffer  truebuf;
	StringBuffer  falsebuf;
	YoixObject    truetype;
	YoixObject    falsetype;
	YoixObject    type;
	String        labels[];

	//
	// The extra work done here is required to make sure that the code
	// we generate leaves the same type of object on the JVM stack no
	// matter which expression is selected.
	//

	truebuf = new StringBuffer();
	falsebuf = new StringBuffer();

	expression(node.getChild1(), truebuf);
	addEvaluate(truebuf);
	truetype = yoixstack.popYoixObject();

	expression(node.getChild2(), falsebuf);
	addEvaluate(falsebuf);
	falsetype = yoixstack.popYoixObject();

	if (truetype != falsetype) {
	    if (falsetype != YOIXOBJECT_TYPE) {
		yoixstack.pushYoixObject(falsetype);
		addCastToYoixObject(falsebuf);
		yoixstack.popYoixObject();
	    }
	    if (truetype != YOIXOBJECT_TYPE) {
		yoixstack.pushYoixObject(truetype);
		addCastToYoixObject(truebuf);
		yoixstack.popYoixObject();
	    }
	    type = YOIXOBJECT_TYPE;
	} else type = truetype;

	expression(node.getChild0(), sbuf);
	addEvaluateToWeakBoolean(sbuf);

	labels = getNextBranchLabelNames(2);
	addIf(NAME_IFEQ, labels, sbuf);
	sbuf.append(truebuf);
	addIfBranch(labels, false, sbuf);
	sbuf.append(falsebuf);
	addIfJoin(labels, sbuf);

	yoixstack.pushYoixObject(type);
    }


    private void
    expressionRelational(int op, StringBuffer sbuf) {

	YoixObject  left;
	YoixObject  right;
	boolean     flipped;
	String      labels[];
	String      instruction = null;

	flipped = addEvaluateBinaryOperands(op, sbuf);

	right = yoixstack.peekYoixObject(0);
	left = yoixstack.peekYoixObject(1);

	if ((right == INT_TYPE || right == DOUBLE_TYPE) && (left == INT_TYPE || left == DOUBLE_TYPE)) {
	    if (left == DOUBLE_TYPE || right == DOUBLE_TYPE) {
		switch (op) {
		    case LT:
			addInstruction(NAME_DCMPG, sbuf);
			instruction = flipped ? NAME_IFGT : NAME_IFLT;
			break;

		    case LE:
			addInstruction(NAME_DCMPG, sbuf);
			instruction = flipped ? NAME_IFGE : NAME_IFLE;
			break;

		    case GT:
			addInstruction(NAME_DCMPL, sbuf);
			instruction = flipped ? NAME_IFLT : NAME_IFGT;
			break;

		    case GE:
			addInstruction(NAME_DCMPL, sbuf);
			instruction = flipped ? NAME_IFLE : NAME_IFGE;
			break;

		    case EQ:
		    case EQEQ:
			addInstruction(NAME_DCMPL, sbuf);
			instruction = NAME_IFEQ;
			break;

		    case NE:
		    case NEEQ:
			addInstruction(NAME_DCMPL, sbuf);
			instruction = NAME_IFNE;
			break;
		}
	    } else {
		switch (op) {
		    case LT:
			instruction = flipped ? NAME_IF_ICMPGT : NAME_IF_ICMPLT;
			break;

		    case LE:
			instruction = flipped ? NAME_IF_ICMPGE : NAME_IF_ICMPLE;
			break;

		    case GT:
			instruction = flipped ? NAME_IF_ICMPLT : NAME_IF_ICMPGT;
			break;

		    case GE:
		        instruction = flipped ? NAME_IF_ICMPLE : NAME_IF_ICMPGE;
		        break;

		    case EQ:
		    case EQEQ:
			instruction = NAME_IF_ICMPEQ;
			break;

		    case NE:
		    case NEEQ:
			instruction = NAME_IF_ICMPNE;
			break;
		}
	    }

	    labels = getNextBranchLabelNames(2);
	    addIf(instruction, labels, sbuf);
	    addPush(false, sbuf);
	    addIfBranch(labels, true, sbuf);
	    addPush(true, sbuf);
	    addIfJoin(labels, sbuf);
	} else {
	    addCastToYoixObject(sbuf);
	    flipped ^= addExchCastToYoixObject(false, sbuf);

	    if (flipped) {
		switch (op) {
		    case LT: op = GT; break;
		    case LE: op = GE; break;
		    case GT: op = LT; break;
		    case GE: op = LE; break;
		}
	    }

	    addPush(op, sbuf);
	    addStack(sbuf);
	    addInvoke(EXPRESSION_RELATIONAL, 4, INT_TYPE, sbuf);
	}
    }


    private void
    expressionRERelational(int op, StringBuffer sbuf) {

	addEvaluateBinaryOperands(op, sbuf);
	addCastToYoixObject(sbuf);
	addExchCastToYoixObject(false, sbuf);
	addPush(op, sbuf);
	addStack(sbuf);
	addInvoke(EXPRESSION_RERELATIONAL, 4, INT_TYPE, sbuf);
    }


    private void
    expressionReturn(StringBuffer sbuf) {

	addEvaluateToYoixObject(sbuf);
	addInstruction(NAME_RETURN, sbuf);
    }


    private void
    expressionShift(int op, StringBuffer sbuf) {

	YoixObject  left;
	YoixObject  right;
	String      labels[];
	int         bits;

	//
	// This duplicates the current implementation of shift operators by
	// the Yoix interpreter, however the special treatment afforded to
	// left shifts probably should have been extended to right shifts.
	// It's something we eventually should address - in YoixInterpreter
	// and here.
	//
	// NOTE - this generates quite a bit of code, but it probably would
	// not be that hard to generate the cases once and then use NAME_JSR
	// and NAME_RET to reuse code. Low priority, but we eventually may
	// investigate.
	//

	addEvaluateBinaryOperands(op, sbuf);

	right = yoixstack.peekYoixObject(0);
	left = yoixstack.peekYoixObject(1);

	if ((left == INT_TYPE || left == DOUBLE_TYPE) && (right == INT_TYPE || right == DOUBLE_TYPE)) {
	    bits = (left == INT_TYPE) ? BITSIZE_INT : BITSIZE_LONG;
	    labels = getNextBranchLabelNames(3);

	    addInstruction(NAME_CAST2I, sbuf);
	    addInstruction(NAME_DUP, sbuf);

	    switch (op) {
		case LEFTSHIFT:
		case LEFTSHIFTEQ:
		    addInstruction(NAME_IFLE, labels[0], sbuf);
		    break;

		case RIGHTSHIFT:
		case RIGHTSHIFTEQ:
		case UNSIGNEDSHIFT:
		case UNSIGNEDSHIFTEQ:
		    addInstruction(NAME_IFGE, labels[0], sbuf);
		    addInstruction(NAME_NEG, sbuf);
		    break;
	    }

	    addInstruction(NAME_DUP, sbuf);
	    addPush(bits, sbuf);
	    addInstruction(NAME_IF_ICMPGE, labels[1], sbuf);
	    addInstruction(NAME_SHL, sbuf);
	    addInstruction(NAME_GOTO, labels[2], sbuf);
	    //
	    // Prepare yoixstack for the labels[1] branch.
	    //
	    yoixstack.popYoixObject();
	    yoixstack.pushYoixObject(left);
	    yoixstack.pushYoixObject(INT_TYPE);
	    addLabel(labels[1], sbuf);
	    addInstruction(NAME_POP, sbuf);
	    addInstruction(NAME_POP, sbuf);
	    if (left == INT_TYPE)
		addPush(0, sbuf);
	    else addPush((long)0, sbuf);
	    addInstruction(NAME_GOTO, labels[2], sbuf);
	    //
	    // Prepare yoixstack for the labels[0] branch.
	    //
	    yoixstack.popYoixObject();
	    yoixstack.pushYoixObject(left);
	    yoixstack.pushYoixObject(INT_TYPE);
	    addLabel(labels[0], sbuf);

	    switch (op) {
		case LEFTSHIFT:
		case LEFTSHIFTEQ:
		    addInstruction(NAME_NEG, sbuf);
		    addInstruction(NAME_SHR, sbuf);
		    break;

		case RIGHTSHIFT:
		case RIGHTSHIFTEQ:
		    addInstruction(NAME_SHR, sbuf);
		    break;

		case UNSIGNEDSHIFT:
		case UNSIGNEDSHIFTEQ:
		    addInstruction(NAME_USHR, sbuf);
		    break;
	    }

	    addLabel(labels[2], sbuf);
	    if (left == DOUBLE_TYPE)
		addInstruction(NAME_CAST2D, sbuf);
	} else {
	    addCastToYoixObject(sbuf);
	    addExchCastToYoixObject(false, sbuf);
	    addPush(op, sbuf);
	    addStack(sbuf);
	    addInvoke(EXPRESSION_SHIFT, 4, YOIXOBJECT_TYPE, sbuf);
	}
    }


    private void
    expressionShiftEQ(int op, StringBuffer sbuf) {

	expressionShiftEQ(op, false, sbuf);
    }


    private void
    expressionShiftEQ(int op, boolean toss, StringBuffer sbuf) {

	YoixObject  left;

	addEvaluate(sbuf);
	left = yoixstack.peekYoixObject(1);

	if (left == YOIXLVALUE_TYPE) {
	    addInstruction(NAME_EXCH, sbuf);
	    addInstruction(NAME_DUPX, sbuf);
	    addInstruction(NAME_EXCH, sbuf);
	} else {
	    yoixstack.pushYoixObject(left);
	    yoixstack.exchange();
	}

	expressionShift(op, sbuf);
	expressionAssign(toss, sbuf);
    }


    private void
    expressionUnary(int op, StringBuffer sbuf) {

	YoixObject  right;
	String      labels[];

	addEvaluate(sbuf);
	right = yoixstack.peekYoixObject(0);

	if (right == INT_TYPE || right == DOUBLE_TYPE) {
	    switch (op) {
		case COMPLEMENT:
		    if (right == INT_TYPE) {
			addPush(-1, sbuf);
			addInstruction(NAME_XOR, sbuf);
		    } else {
			addPush((long)-1, sbuf);
			addInstruction(NAME_XOR, sbuf);
			addInstruction(NAME_CAST2D, sbuf);
		    }
		    break;

		case NOT:
		    labels = getNextBranchLabelNames(2);
		    if (right == INT_TYPE) {
			addIf(NAME_IFEQ, labels, sbuf);
			addPush(false, sbuf);
			addIfBranch(labels, true, sbuf);
			addPush(true, sbuf);
			addIfJoin(labels, sbuf);
		    } else {
			//
			// Interpreter currently returns an int so we do too,
			// but perhaps the interpreter should be changed?
			//
			addPush(0.0, sbuf);
			addInstruction(NAME_DCMPL, sbuf);
			addIf(NAME_IFEQ, labels, sbuf);
			addPush(false, sbuf);
			addIfBranch(labels, true, sbuf);
			addPush(true, sbuf);
			addIfJoin(labels, sbuf);
		    }
		    break;

		case UMINUS:
		    addInstruction(NAME_NEG, sbuf);
		    break;
	    }
	} else {
	    addCastToYoixObject(sbuf);
	    addPush(op, sbuf);
	    addStack(sbuf);
	    addInvoke(EXPRESSION_UNARY, 3, YOIXOBJECT_TYPE, sbuf);
	}
    }


    private void
    functionCall(SimpleNode args, StringBuffer sbuf) {

	int  argc;
	int  n;

	//
	// This undoubtedly isn't quite right!!! Code in the interpreter
	// calls lval.execute(), but we generate code that evaluates lval
	// when we call addEvaluateToYoixObject(). Suspect we may need an
	// addEvaluatetoLvalue() method that has to work harder when it's
	// handed a local variable.
	//
	// NOTE - there could be other methods in the complier that really
	// need the same kind of treatment, so we should carefully compare 
	// complier and interpreter methods when we're done with the first
	// pass through the compiler.
	//

	addLvalue(sbuf);

	argc = args.length();
	addPush(argc, sbuf);
	addInstruction(NAME_ANEWARRAY, YOIXOBJECT, sbuf);
	for (n = 0; n < argc; n++) {
	    addInstruction(NAME_DUP, sbuf);
	    addPush(n, sbuf);
	    expression(args.getChild(n), sbuf);
	    addEvaluateToYoixObject(sbuf);
	    addInstruction(NAME_ARRAYSTORE, sbuf);
	}
	addStack(sbuf);
	addInvoke(FUNCTION_CALL, 3, YOIXOBJECT_TYPE, sbuf);
    }


    private HashMap
    getBranchMap(SimpleNode node, boolean condition, String label) {

	SimpleNode  child;
	SimpleNode  last;
	SimpleNode  lastexpr;
	HashMap     map = null;
	Object      data[];
	String      lastlabel;
	String      lastexprlabel;
	int         length;
	int         n;

	//
	// The algorithm that we use makes use of the fact that nodes in
	// an expression are ordered by precedence. In addition it rejects
	// expressions that include operators that have a lower precedence
	// than LOGICALOR, except CONDITIONAL nodes are OK but a LOGICALXOR
	// will cause a rejection of the entire expression. The HashMap that
	// we return maps the nodes in the expression to arrays that have 2
	// or 3 elements. The first element in that array is a Boolean that
	// should be tested against the value that's left on the JVM stack
	// by the code that the compiler generates. The second element is
	// the label assigned to the code that should be executed when that
	// Boolean and the value on the JVM stack match. The third element
	// in the array, if there is one that's not null, is the label that
	// marks the end of the code the compiler generates for that node.
	// 
	// The following tables try to describe the decisions that we make
	// when we realize we have to deal with the CONDITIONAL nodes that
	// are used by LOGICALAND and LOGIGICALOR. You definitely need some
	// familiarly with the parse trees that are generated by the Yoix
	// parser when it encounters an expression if you expect to follow
	// this stuff.
	//
	//    ---------------------------------
	//    LOGICALAND - NOT CONDITIONAL NODE
	//    ---------------------------------
	//        VALUE     CONDITION    LAST==LASTEXPR     TARGET
	//        -----     ---------    --------------     ------
	//          F           F              F            lastlabel
	//          F           F              T            label
	//          F           T              F            lastlabel
	//          F           T              T            lastlabel
	//          T           F              F            next
	//          T           F              T            next
	//          T           T              F            next
	//          T           T              T            next
	//
	//    --------------------------------
	//    LOGICALOR - NOT CONDITIONAL NODE
	//    --------------------------------
	//        VALUE     CONDITION    LAST==LASTEXPR     TARGET
	//        -----     ---------    --------------     ------
	//          F           F              F            next
	//          F           F              T            next
	//          F           T              F            next
	//          F           T              T            next
	//          T           F              F            lastlabel
	//          T           F              T            lastlabel
	//          T           T              F            lastlabel
	//          T           T              T            label
	//
	//    -----------------------------
	//    LOGICALAND - CONDITIONAL NODE
	//    -----------------------------
	//        VALUE     CONDITION    LAST==LASTEXPR     CHILD==LAST   TARGET
	//        -----     ---------    --------------     -----------   ------
	//          F           F              F                 F        lastlabel
	//          T           F              F                 F        next
	//          F           F              F                 T        next
	//          T           F              F                 T        lastexprlabel
	//          F           F              T                 T        label
	//          T           F              T                 T        next
	//          F           F              T                 F        label
	//          T           F              T                 F        next
	//          F           T              F                 F        lastlabel
	//          T           T              F                 F        next
	//          F           T              F                 T        lastlabel
	//          T           T              F                 T        next
	//          F           T              T                 T        next
	//          T           T              T                 T        label
	//          F           T              T                 F        lastlabel
	//          T           T              T                 F        next
	//
	//    ----------------------------
	//    LOGICALOR - CONDITIONAL NODE
	//    ----------------------------
	//        VALUE     CONDITION    LAST==LASTEXPR     CHILD==LAST   TARGET
	//        -----     ---------    --------------     -----------   ------
	//          F           F              F                 F        next
	//          T           F              F                 F        lastlabel
	//          F           F              F                 T        impossible (by precedence)
	//          T           F              F                 T        impossible (by precedence)
	//          F           F              T                 T        label
	//          T           F              T                 T        next
	//          F           F              T                 F        next
	//          T           F              T                 F        lastlabel
	//          F           T              F                 F        next
	//          T           T              F                 F        label
	//          F           T              F                 T        next         
	//          T           T              F                 T        label
	//          F           T              T                 T        next
	//          T           T              T                 T        label
	//          F           T              T                 F        next
	//          T           T              T                 F        label
	//
	// You need some familiarly with the parse trees that are generated
	// by the Yoix parser when it encounters an expression before these
	// tables (and the code below) will make much sense. You can dump
	// parse trees by setting
	//
	//	VM.debug = 2;
	//
	// when you run the Yoix interpreter. In addition
	//
	//	VM.addtags = false;
	//
	// will eliminate the source code tags that clutter up parse trees.
	//
	// NOTE - caller currently assumes there's always a non-null entry
	// for CONDITIONAL nodes.
	// 

	if (isBranchExpression(node)) {
	    map = new HashMap();
	    if (label != null && node != null) {
		length = node.length();
		lastexpr = node.getChild(length - 1);
		if (lastexpr.type() == LOGICALAND || lastexpr.type() == LOGICALOR)		// part of a CONDITIONAL
		    lastexpr = node.getChild(length - 2);
		lastexprlabel = getNextBranchLabelName();
		map.put(lastexpr, lastexprlabel);
		for (n = 0; n < length; n++) {
		    child = node.getChild(n);
		    if (child.type() != CONDITIONAL) {
			if (n < length - 1) {
			    if (node.getChild(n+1).type() == CONDITIONAL) {
				last = node.getChild(getLastIndexForSubExpression(node, n+1));
				if (map.containsKey(last) == false) {
				    lastlabel = getNextBranchLabelName();
				    map.put(last, lastlabel);
				} else lastlabel = (String)map.get(last);

				switch (node.getChild(n+2).type()) {
				    case LOGICALAND:
					if (condition)
					    map.put(child, new Object[] {Boolean.FALSE, lastlabel});
					else map.put(child, new Object[] {Boolean.FALSE, (last == lastexpr) ? label : lastlabel});
					break;

				    case LOGICALOR:
					if (condition == false)
					    map.put(child, new Object[] {Boolean.TRUE, lastlabel});
					else map.put(child, new Object[] {Boolean.TRUE, (last == lastexpr) ? label : lastlabel});
					break;
				}
			    }
			} else map.put(child, new Object[] {new Boolean(condition), label});
		    } else {
			last = node.getChild(getLastIndexForSubExpression(node, n));
			if (map.containsKey(last) == false) {
			    lastlabel = getNextBranchLabelName();
			    map.put(last, lastlabel);
			} else lastlabel = (String)map.get(last);

			switch (node.getChild(++n).type()) {
			    case LOGICALAND:
				if (condition) {
				    if (last != lastexpr) {
					if (child == last) {
					    //
					    // Table in comments seems to say that technically this
					    // should be
					    //
					    //     new Object[] {Boolean.FALSE, lastlabel, null}
					    //
					    // which should be match what we're currently doing.
					    //
					    map.put(child, new Object[] {Boolean.FALSE, lastlabel, lastlabel});
					} else map.put(child, new Object[] {Boolean.FALSE, lastlabel, null});
				    } else {
					if (child == last) {
					    //
					    // Table in comments seems to say that technically this
					    // should be
					    //
					    //     new Object[] {Boolean.TRUE, label, null}
					    //
					    // which should be match what we're currently doing.
					    //
					    map.put(child, new Object[] {Boolean.TRUE, label, lastlabel});
					} else map.put(child, new Object[] {Boolean.FALSE, lastlabel, null});
				    }
				} else {
				    if (last != lastexpr) {
					if (child == last) {
					    //
					    // Table in comments seems to say that technically this
					    // should be
					    //
					    //     new Object[] {Boolean.TRUE, lastexprlabel, null}
					    //
					    // which should be match what we're currently doing.
					    //
					    map.put(child, new Object[] {Boolean.TRUE, lastexprlabel, lastlabel});
					} else map.put(child, new Object[] {Boolean.FALSE, lastlabel, null});
				    } else {
					if (child == last) {
					    //
					    // Table in comments seems to say that technically this
					    // should be
					    //
					    //     new Object[] {Boolean.FALSE, label, null}
					    //
					    // which should be match what we're currently doing.
					    //
					    map.put(child, new Object[] {Boolean.FALSE, label, lastlabel});
					} else map.put(child, new Object[] {Boolean.FALSE, label, null});
				    }
				}
				break;

			    case LOGICALOR:
				if (condition == false) {
				    if (last != lastexpr) {
					if (child == last)
					    VM.abort(COMPILERERROR);	// impossible - by precedence
					else map.put(child, new Object[] {Boolean.TRUE, lastlabel, null});
				    } else {
					if (child == last)
					    map.put(child, new Object[] {Boolean.FALSE, label, lastlabel});
					else map.put(child, new Object[] {Boolean.TRUE, lastlabel, null});
				    }
				} else {
				    //
				    // Table in comments seems to say that technically this
				    // should be
				    //
				    //     new Object[] {Boolean.TRUE, label, null}
				    //
				    // which should be match what we're currently doing.
				    //
				    map.put(child, new Object[] {Boolean.TRUE, label, child == last ? lastlabel : null});
				}
				break;
			}
		    }
		}
	    }
	}

	return(map == null || map.size() > 0 ? map : null);
    }


    private String
    getCurrentBreakLabel() {

	String  data[];
	String  label;
	int     index;

	if ((index = jump_data.size() - 1) >= 0) {
	    data = (String[])jump_data.get(index);
	    label = data[1];
	} else label = null;

	return(label);
    }


    private String
    getCurrentContinueLabel() {

	String  data[];
	String  label;
	int     index;

	if ((index = jump_data.size() - 1) >= 0) {
	    data = (String[])jump_data.get(index);
	    label = data[0];
	} else label = null;

	return(label);
    }


    private int
    getIndexOfNode(Object arg) {

	int  index;

	if ((index = node_references.indexOf(arg)) < 0) {
	    node_references.add(arg);
	    index = node_references.size() - 1;
	}
	return(index);
    }


    private int
    getIndexOfObject(YoixObject obj) {

	int  index;

	if ((index = object_references.indexOf(obj)) < 0) {
	    object_references.add(obj);
	    index = object_references.size() - 1;
	}
	return(index);
    }


    private int
    getLastIndexForSubExpression(SimpleNode expr, int index) {

	int  length;
	int  op;
	int  n;

	//
	// Returns an integer that references the last node that's part of
	// the "sub expression" that starts at index. Most of the time we
	// return index, but CONDITIONAL expression nodes will be grouped
	// by precedence (in expr) and in that case the integer we return
	// will be the last CONDITIONAL node in that grouping. The last
	// node in the grouping will eventually be responsible for adding
	// a label that other CONDITIONAL nodes in the group use to break
	// out once the answer is known.
	//

	if (expr.getChild(index).type() == CONDITIONAL) {
	    op = expr.getChild(index+1).type();
	    length = expr.length();
	    for (n = index + 2; n < length; n += 2) {
		if (expr.getChild(n).type() == CONDITIONAL) {
		    if (expr.getChild(n+1).type() == op)
			index = n;
		    else break;
		} else break;
	    }
	}

	return(index);
    }


    private String
    getNextArgsName() {

	int  next;

	//
	// Used to pick a unique name for the block that stores arguments
	// that were passed to the function. Needed because we currently
	// can't reuse block names but we may be asked to compile several
	// functions into the same class file (e.g., when we're compiling
	// a script).
	//

	return((next = next_args++) == 0 ? NAMEOF_ARGS_ARGUMENT : (NAMEOF_ARGS_ARGUMENT + next));
    }


    private String
    getNextBlockName() {

	String  name = COMPILED_BLOCKNAME + next_block++;

	registerBlock(name);
	return(name);
    }


    private String
    getNextBranchLabelName() {

	return(COMPILED_LABLENAME + next_label++);
    }


    private String[]
    getNextBranchLabelNames(int count) {

	String  labels[];
	int     n;

	labels = new String[count];
	for (n = 0; n < count; n++)
	    labels[n] = getNextBranchLabelName();
	return(labels);
    }


    private String
    getNextClassName() {

	return(COMPILED_CLASSNAME + next_class++);
    }


    private String
    getNextLocalVariableName() {

	return(COMPILED_VARIABLENAME + next_variable++);
    }


    private String
    getNextMethodName() {

	return(COMPILED_METHODNAME + next_method++);
    }


    private boolean
    isBranchExpression(SimpleNode expr) {

	boolean  result = false;
	boolean  done;
	int      index;
	int      op;
	int      n;

	//
	// Returns true if the expr parse tree represents an expression that
	// the compiler can handle efficiently when it contols the branching
	// decisions in variaous statements (e.g., if, while, for, and do).
	// A false return is OK and just means the generated code won't be
	// as efficient as it is when true is returned. Right now we reject
	// expressions that include operators with a lower precedence than
	// LOGICALOR (except for LOGICALXOR). The CONDITIONAL nodes used by
	// LOGICALAND and LOGICALOR are automatically accepted because they
	// immediately precede LOGICALAND and LOGICALOR in the parse tree.
	//

	if ((index = expr.length() - 1) >= 0) {
	    result = true;
	    done = false;
	    for (; index >= 0 && result && done == false; index--) {
		switch (op = expr.getChild(index).type()) {
		    case LOGICALOR:
		    case LOGICALAND:
			index--;		// skips the CONDITIONAL
			break;

		    default:
			if (YoixParser.getPrecedence(op) < PRECEDENCE_LOGICALAND)
			    result = false;
			else done = true;
			break;
		}
	    }
	}

	return(result);
    }


    private boolean
    isConstantValue(YoixObject obj) {

	return(obj.isNumber() || (obj.isString() && isTypeName(obj) == false));
    }


    private boolean
    isCopiedLocalVariable(YoixObject obj) {

	return(isLocalVariable(obj) && isLocalVariableCopied(obj));
    }


    private boolean
    isLocalVariable(YoixObject obj) {

	return(local_directory.containsKey(obj));
    }


    private boolean
    isLocalVariableCopied(YoixObject obj) {

	YoixObject  localname;

	return((localname = obj.getObject(LOCALNAME)) != null && localname.notNull());
    }


    private boolean
    isResolvedTypeName(YoixObject obj) {

	return(ALL_TYPES.containsKey(obj) && obj != YOIXLVALUE_TYPE);
    }


    private boolean
    isTypeName(YoixObject obj) {

	return(ALL_TYPES.containsKey(obj));
    }


    private void
    lvalue(SimpleNode node, StringBuffer sbuf) {

	SimpleNode  child;
	int         length;
	int         n;

	//
	// We can't evaluate expressions (here or in lvaluePrimary()) before
	// deciding whether to hand the job to the interpreter because those
	// expressions could involve side-effects.
	//
	// NOTE - we're implicitly assuming that synclocals.syncStatement()
	// also deals with operators like & (via another callback) that may
	// need to do things like disable caching of all locals in a block.
	// 

	length = node.length();
	for (n = lvaluePrimary(node, sbuf); n < length; n++) {
	    child = node.getChild(n);
	    switch (child.type()) {
		case EXPRESSION:
		    addEvaluateToYoixObject(sbuf);
		    expression(child, sbuf);
		    addEvaluateToYoixObject(sbuf);
		    addInvoke(LVALUE_BYEXPRESSION, 2, YOIXLVALUE_TYPE, sbuf);
		    break;

		case FUNCTION:
		    functionCall(child, sbuf);
		    break;

		case NAME:
		    addEvaluateToYoixObject(sbuf);
		    addPush(child.stringValue(), sbuf);
		    addInvoke(LVALUE_BYNAME, 2, YOIXLVALUE_TYPE, sbuf);
		    break;

		default:
		    VM.abort(COMPILERERROR);
		    break;
	    }
	}
    }


    private int
    lvaluePrimary(SimpleNode node, StringBuffer sbuf) {

	YoixParserBvalue  bvalue;
	YoixObject        lval;
	SimpleNode        child;
	HashMap           modified;
	String            name;
	int               count = 0;

	child = node.getChild(count++);

	switch (child.type()) {
	    case NAME:
		name = child.stringValue();
		if (YoixBodyBlock.isLocalName(name) == false) {
		    if (YoixBodyBlock.isReservedName(name)) {
			if (isDisableAllReservedName(name))
			    disableAllBlockLocalVariables(true, sbuf);
		    }
		    modified = synclocals.syncLvalue(node, sbuf);
		    addReferenceToNode(node, sbuf);
		    addStack(sbuf);
		    addInvoke(LVALUE_HANDLER, 2, YOIXLVALUE_TYPE, sbuf);
		    restoreModifiedVariables(modified, sbuf);
		    count = node.length();		// tell caller we're done
		} else yoixstack.pushYoixObject(YoixBodyBlock.newLvalue(name));
		break;

	    case BOUND_LVALUE:
		bvalue = (YoixParserBvalue)child.getBvalue();
		lval = YoixBodyBlock.newLvalue(bvalue);
		if ((name = YoixBodyBlock.getBlockName(lval)) == null)
		    name = lval.name();
		if (name != null) {
		    if (YoixBodyBlock.isLocalName(name) == false) {
			if (YoixBodyBlock.isReservedName(name)) {
			    if (isDisableAllReservedName(name))
				disableAllBlockLocalVariables(true, sbuf);
			}
			modified = synclocals.syncLvalue(node, sbuf);
			addReferenceToNode(node, sbuf);
			addStack(sbuf);
			addInvoke(LVALUE_HANDLER, 2, YOIXLVALUE_TYPE, sbuf);
			restoreModifiedVariables(modified, sbuf);
			count = node.length();		// tell caller we're done
		    } else yoixstack.pushYoixObject(lval);
		} else VM.abort(COMPILERERROR);
		break;

	    case GLOBAL:
		addInvoke(NEWGLOBALLVALUE, 0, YOIXLVALUE_TYPE, sbuf);
		break;

	    case THIS:
		addInvoke(NEWTHISLVALUE, 0, YOIXLVALUE_TYPE, sbuf);
		break;

	    case ADDRESS:
		modified = synclocals.syncLvalue(node, sbuf);
		addReferenceToNode(node, sbuf);
		addStack(sbuf);
		addInvoke(LVALUE_HANDLER, 2, YOIXLVALUE_TYPE, sbuf);
		//
		// If we take the address of a local variable then we disable
		// local copies of all variables in that block, even though it
		// might not always be required. Changes to this code probably
		// should also be made in YoixCompilerSyncLocals.java.
		//
		child = node.getChild1();
		if (child.length() == 1) {
		    child = child.getChild0();
		    switch (child.type()) {
			case NAME:
			    name = child.stringValue();
			    if (YoixBodyBlock.isLocalName(name)) {
				if ((lval = YoixBodyBlock.newLvalue(name)) != null)
				    disableBlockLocalVariables(lval, sbuf);
			    } else if (YoixBodyBlock.isReservedName(name))
				disableAllBlockLocalVariables(true, sbuf);
			    break;

			case BOUND_LVALUE:
			    bvalue = (YoixParserBvalue)child.getBvalue();
			    if (bvalue.getLevel() >= 0) {
				if ((lval = YoixBodyBlock.newLvalue(bvalue)) != null)
				    disableBlockLocalVariables(lval, sbuf);
			    } else disableAllBlockLocalVariables(true, sbuf);
			    break;
		    }
		}
		restoreModifiedVariables(modified, sbuf);
		count = node.length();		// tell caller we're done
		break;

	    case INDIRECTION:
		modified = synclocals.syncLvalue(node, sbuf);
		addReferenceToNode(node, sbuf);
		addStack(sbuf);
		addInvoke(LVALUE_HANDLER, 2, YOIXLVALUE_TYPE, sbuf);
		restoreModifiedVariables(modified, sbuf);
		count = node.length();		// tell caller we're done
		break;
	}

	return(count);
    }


    private String
    pickClassNameForPath(String path) {

	String  name = null;
	int     length;
	int     index;
	int     n;

	//
	// OK for now, but it probably needs work.
	//

	try {
	    path = (new URL(path)).getFile();
	}
	catch(IOException e) {}

	if (path != null) {
	    name = (new File(YoixMisc.toLocalPath(path))).getName();
	    if ((index = name.indexOf('.')) >= 0)
		name = name.substring(0, index);
	    if ((length = name.length()) > 0) {
		if (Character.isJavaIdentifierStart(name.charAt(0))) {
		    for (n = 1; n < length; n++) {
			if (Character.isJavaLetterOrDigit(name.charAt(n)) == false) {
			    name = null;
			    break;
			}
		    }
		} else name = null;
	    } else name = null;
	} else name = null;

	return(name);
    }


    private YoixObject
    pickType(String typename) {

	YoixObject  type;

	//
	// Returns the YoixObject the compiler uses internally to represent
	// the type of an object identified by the typename string.
	//

	if (typename.equals(T_INT))
	    type = INT_TYPE;
	else if (typename.equals(T_DOUBLE))
	    type = DOUBLE_TYPE;
	else type = YOIXOBJECT_TYPE;

	return(type);
    }


    private YoixObject
    pickType(YoixObject obj) {

	YoixObject  type;

	//
	// Returns the YoixObject the compiler uses internally to represent
	// the type of the object obj.
	//

	if (obj.isInteger())
	    type = INT_TYPE;
	else if (obj.isDouble())
	    type = DOUBLE_TYPE;
	else type = YOIXOBJECT_TYPE;

	return(type);
    }


    private void
    popJumpData() {

	int  index;

	if ((index = jump_data.size() - 1) >= 0)
	    jump_data.remove(index);
    }


    private YoixObject
    popRvalue(StringBuffer sbuf) {

	addEvaluate(sbuf);
	return(yoixstack.popYoixObject());
    }


    private String
    popString() {

	return(yoixstack.popYoixObject().stringValue());
    }


    private void
    popSwitchData() {

	int  index;

	if ((index = switch_data.size() - 1) >= 0)
	    switch_data.remove(index);
	popJumpData();
    }


    private void
    pushJumpData(String continue_label, String break_label) {

	jump_data.add(new String[] {continue_label, break_label});
    }


    private void
    pushSwitchData(ArrayList data, String break_label) {

	switch_data.add(data);
	pushJumpData(null, break_label);
    }


    private void
    registerBlock(String blockname) {

	registerBlock(blockname, null, null);
    }


    private void
    registerBlock(String blockname, YoixObject names, YoixObject values) {

	ArrayList  block;

	//
	// The first element in the ArrayList associated with the block's
	// name in block_directory is the localcopy model that's currently
	// associated with the block. Setting it to 0 disables local copies
	// for all future variables that are added to the block. Any other
	// elements in the ArrayList are the dictionaries that describe the
	// local variables defined in the block.
	//

	if (block_directory.containsKey(blockname) == false) {
	    block = new ArrayList();
	    block.add(new Integer(current_localcopymodel));
	    block_directory.put(blockname, block);
	    if (names != null)
		block_directory.put(names, blockname);
	    if (values != null)
		block_directory.put(values.body(), blockname);
	} else VM.abort(COMPILERERROR);
    }


    private void
    registerLocalVariable(YoixObject obj) {

	ArrayList  block;
	String     blockname;

	if (local_directory.containsKey(obj) == false) {
	    blockname = obj.getString(BLOCKNAME);
	    if ((block = (ArrayList)block_directory.get(blockname)) != null) {
		local_directory.put(obj, block);
		block.add(obj);
	    } else VM.abort(COMPILERERROR);
	} else VM.abort(COMPILERERROR);
    }


    private void
    registerMethod(String name) {

	String  value;

	if (extern_directory.containsKey(name) == false) {
	    extern_directory.put(name, Boolean.TRUE);
	    if ((value = (String)ALL_EXTERNS.get(name)) != null) {
		if (indented)
		    header.append("    ");
		header.append(value);
		header.append("\n");
	    } else {
		//
		// Is this an error???
		//
	    }
	}
    }


    private void
    resetCompiler() {

	boolean  debug_output = isDebugBitSet(DEBUG_COMPILER_OUTPUT);

	yoixstack = VM.getThreadStack();
	source = null;
	commented = debug_output;
	indented = debug_output;
	node_references = null;
	object_references = null;
	classname = null;
    }


    private void
    restoreModifiedVariables(HashMap modified, StringBuffer sbuf) {

	Iterator  iterator;
	Object    key;

	if (modified != null) {
	    if ((iterator = modified.keySet().iterator()) != null) {
		while (iterator.hasNext()) {
		    key = iterator.next();
		    if (key instanceof YoixObject)
			addSyncLocalVariableFromBlock((YoixObject)key, sbuf);
		}
	    }
	}
    }


    private void
    statement(SimpleNode node, StringBuffer sbuf) {

	if (node != null) {
	    switch (node.type()) {
		case BREAK:
		    statementBreak(sbuf);
		    break;

		case CASE:
		    statementCase(sbuf);
		    break;

		case DEFAULT_:
		    statementDefault(sbuf);
		    break;

		case EMPTY:
		    break;

		case COMPOUND:
		    statementCompound(node, 0, sbuf);
		    break;

		case CONTINUE:
		    statementContinue(sbuf);
		    break;

		case DECLARATION:
		    statementDeclaration(node, sbuf);
		    break;

		case DO:
		    statementDo(node, sbuf);
		    break;

		case EXIT:
		    statementExit(node, sbuf);
		    break;

		case EXPRESSION:
		    expression(node, true, sbuf);
		    break;

		case FINALLY:
		    statementFinally(node, sbuf);
		    break;

		case FOR:
		    statementFor(node, sbuf);
		    break;

		case FOREACH:
		    statementForEach(node, sbuf);
		    break;

		case FUNCTION:
		    statementFunction(node, sbuf);
		    break;

		case GLOBALBLOCK:
		    statementNamedBlock(node, GLOBALBLOCK, sbuf);
		    break;

		case IF:
		    statementIf(node, sbuf);
		    break;

		case IMPORT:
		    statementImport(node, sbuf);
		    break;

		case INCLUDE:
		    statementInclude(node, sbuf);
		    break;

		case NAMEDBLOCK:
		    statementNamedBlock(node, NAMEDBLOCK, sbuf);
		    break;

		case QUALIFIER:
		    statementQualifier(node, sbuf);
		    break;

		case RESTRICTEDBLOCK:
		    statementNamedBlock(node, RESTRICTEDBLOCK, sbuf);
		    break;

		case RETURN:
		    statementReturn(node, sbuf);
		    break;

		case SAVE:
		    statementSave(node, sbuf);
		    break;

		case STATEMENT:
		    statementList(node, 0, node.length(), sbuf);
		    break;

		case SWITCH:
		    statementSwitch(node, sbuf);
		    break;

		case SYNCHRONIZED:
		    statementSynchronized(node, sbuf);
		    break;

		case TAGGED:
		    statementTagged(node, sbuf);
		    break;

		case THISBLOCK:
		    statementNamedBlock(node, THISBLOCK, sbuf);
		    break;

		case TRY:
		    statementTry(node, sbuf);
		    break;

		case TYPEDEF:
		    statementTypedef(node, sbuf);
		    break;

		case WHILE:
		    statementWhile(node, sbuf);
		    break;

		case YOIX_EOF:
		    statementEOF(sbuf);
		    break;

		default:
		    VM.die(INTERNALERROR);
		    break;
	    }
	}
    }


    private void
    statementBreak(StringBuffer sbuf) {

	String  label;

	if ((label = getCurrentBreakLabel()) != null) {
	    if (label.length() > 0)
		addInstruction(NAME_GOTO, label, sbuf);
	} else addInvoke(ABORT_ILLEGALJUMP, 0, null, sbuf);
    }


    private void
    statementCase(StringBuffer sbuf) {

	ArrayList  data;
	String     label;
	int        index;

	if ((index = switch_data.size() - 1) >= 0) {
	    if ((data = (ArrayList)switch_data.get(index)) != null) {
		label = getNextBranchLabelName();
		addLabel(label, sbuf);
		data.add(data.get(0));
		data.add(label);
	    }
	}
    }


    private void
    statementCompound(SimpleNode node, int start, StringBuffer sbuf) {

	YoixObject  names;
	YoixObject  values;

	names = node.getLocalDict();
	values = YoixObject.newArray(names.length());

	addLocalBlockDeclaration(getNextBlockName(), names, values, sbuf);
	addReferenceToNode(node, sbuf);
	addStack(sbuf);
	addInvoke(STATEMENT_BEGINCOMPOUND, 2, YOIXOBJECT_TYPE, sbuf);
	addStoreLocalBlock(names, values, sbuf);

	yoixstack.pushLocalBlock(names, values, false);
	statementList(node, start, node.length() - 1, sbuf);
	yoixstack.popBlock();

	addStack(sbuf);
	addInvoke(STATEMENT_ENDCOMPOUND, 1, null, sbuf);
    }


    private void
    statementContinue(StringBuffer sbuf) {

	String  label;

	if ((label = getCurrentContinueLabel()) != null) {
	    if (label.length() > 0)
		addInstruction(NAME_GOTO, label, sbuf);
	} else addInvoke(ABORT_ILLEGALJUMP, 0, null, sbuf);
    }


    private void
    statementDeclaration(SimpleNode node, StringBuffer sbuf) {

	YoixObject  lval;
	YoixObject  obj;
	HashMap     modified;
	boolean     qualified;
	String      typename;
	int         length;
	int         index;
	int         n;

	//
	// NOTE - we're implicitly assuming that synclocals.syncStatement()
	// also deals with operators like & (via another callback) that may
	// need to do things like disable caching of all locals in a block.
	// It's code we still have to add!!
	//

	modified = syncStatement(node, true, sbuf);

	if (node.getChild0().type() == NAME) {
	    qualified = false;
	    n = 0;
	} else {
	    qualified = true;
	    n = 1;
	}

	typename = node.getChild(n++).stringValue();
	index = getIndexOfNode(node);

	for (length = node.length(); n < length; n++) {
	    lval = YoixBodyBlock.newDvalue(node.getChild(n).getChild0().getChild0().stringValue());
	    obj = createLocalVariable(lval, typename);

	    addReferenceToNode(index, JAVAOBJECT_TYPE, sbuf);
	    addPush(typename, sbuf);
	    addPush(n, sbuf);
	    addStack(sbuf);
	    if (YoixBodyBlock.isGlobalLvalue(lval)) {
		addIndexRegisterDeclaration(obj, sbuf);
		addInvoke(DECLAREVARIABLE, 4, INT_TYPE, sbuf);
		addStoreIndexRegister(obj, sbuf);
	    } else addInvoke(DECLAREVARIABLE_VOID, 4, null, sbuf);

	    //
	    // We don't use a local copy of the variable if the declaration
	    // included a qualifier, but instead we let the YoixObject used
	    // for block storage enforce the "qualifier".
	    //

	    if (qualified == false)
		addLocalVariableDeclaration(obj, true, sbuf);
	    else obj.putString(LOCALNAME, null);
	}

	restoreModifiedVariables(modified, sbuf);
    }


    private void
    statementDefault(StringBuffer sbuf) {

	ArrayList  data;
	String     label;
	int        index;

	if ((index = switch_data.size() - 1) >= 0) {
	    if ((data = (ArrayList)switch_data.get(index)) != null) {
		label = getNextBranchLabelName();
		addLabel(label, sbuf);
		data.set(2, label);
	    }
	}
    }


    private void
    statementDo(SimpleNode node, StringBuffer sbuf) {

	String  labels[];

	labels = getNextBranchLabelNames(3);
	pushJumpData(labels[1], labels[2]);

	syncBranches(node, sbuf);
	addLabel(labels[0], sbuf);
	statement(node.getChild0(), sbuf);
	addLabel(labels[1], sbuf);
	branch(node.getChild1(), true, labels[0], sbuf);
	addLabel(labels[2], sbuf);
	popJumpData();
    }


    private void
    statementEOF(StringBuffer sbuf) {

	addStack(sbuf);
	addInvoke(STATEMENT_EOF, 1, null, sbuf);
    }


    private void
    statementExit(SimpleNode node, StringBuffer sbuf) {

	expression(node.getChild0(), sbuf);
	addEvaluateToYoixObject(sbuf);
	addStack(sbuf);
	addInvoke(STATEMENT_EXIT, 2, null, sbuf);
    }


    private void
    statementFinally(SimpleNode node, StringBuffer sbuf) {

	//
	// We need to completely disable the use of local copies for every
	// local variable used by node because the finally block is handled
	// by the interpreter as the stack is popped, and we can't predict
	// exactly when that will happen.
	//
	// NOTE - we're implicitly assuming that synclocals.syncStatement()
	// also deals with operators like & (via another callback) that may
	// need to do things like disable caching of all locals in a block.
	// It's code we still have to add!!
	//

	disableLocalVariableCopies(syncStatement(node, false, sbuf));
	addReferenceToNode(node, sbuf);
	addStack(sbuf);
	addInvoke(STATEMENT_FINALLY, 2, null, sbuf);
    }


    private void
    statementFor(SimpleNode node, StringBuffer sbuf) {

	String  labels[];

	labels = getNextBranchLabelNames(3);
	pushJumpData(labels[1], labels[2]);

	syncBranches(node, sbuf);
	expression(node.getChild0(), true, sbuf);
	addLabel(labels[0], sbuf);
	branch(node.getChild1(), false, labels[2], sbuf);
	statement(node.getChild3(), sbuf);
	addLabel(labels[1], sbuf);
	expression(node.getChild2(), true, sbuf);
	addInstruction(NAME_GOTO, labels[0], sbuf);
	addLabel(labels[2], sbuf);

	popJumpData();
    }


    private void
    statementForEach(SimpleNode node, StringBuffer sbuf) {

	SimpleNode  name;
	SimpleNode  expression;
	SimpleNode  statement;
	SimpleNode  increment;
	YoixObject  names;
	YoixObject  values;
	YoixObject  lval;
	YoixObject  tmp1;
	YoixObject  tmp2;
	String      labels[];
	String      blockname;
	int         incr = 1;

	if (node.length() == 4) {
	    name = node.getChild0();
	    expression = node.getChild1();
	    increment = node.getChild2();
	    statement = node.getChild3();
	} else {
	    name = node.getChild0();
	    expression = node.getChild1();
	    increment = null;
	    statement = node.getChild2();
	}

	syncBranches(node, sbuf);
	expression(expression, sbuf);
	addEvaluate(sbuf);
	addInstruction(NAME_DUP, sbuf);
	addInvoke(TYPECHECK_IFNOTPOINTER, 1, null, sbuf);

	if (increment != null) {
	    expression(increment, sbuf);
	    addEvaluate(sbuf);
	    //
	    // Eventually need to do a better job here!!
	    //
	    addInstruction(NAME_CAST2I, sbuf);
	} else addPush(1, sbuf);

	addInstruction(NAME_EXCH, sbuf);

	//
	// At this point the lvalue and increment are the top two objects
	// on the stack.
	//

	blockname = getNextBlockName();
	lval = createLocalVariable(blockname, 0, YOIXOBJECT_TYPE);

	//
	// This is a bit ugly, but we need to push a new local block that's
	// used to store the foreach variable. We couldn't do it before we
	// evaluated the increment and expression, because the block could
	// disrupt the evaluation of those expressions. The results that we
	// got when we evaluated those expressions are represented by the
	// top two objects on yoixstack. In addition we've generated code
	// that's supposed to leave the results on the JVM stack, so right
	// now the two stacks should be in sync, but before we start the new
	// block we have to pop the two objects from yoixstack that represent
	// the state of the JVM stack and restore them after the new block is
	// started.
	// 

	tmp1 = yoixstack.popYoixObject();
	tmp2 = yoixstack.popYoixObject();
	yoixstack.pushForEachBlock(name.stringValue(), lval);
	yoixstack.pushYoixObject(tmp2);
	yoixstack.pushYoixObject(tmp1);

	names = YoixBodyBlock.getBlockNames(0);
	values = YoixBodyBlock.getBlockValues(0);

	addLocalBlockDeclaration(blockname, names, values, sbuf);
	addPush(name.stringValue(), sbuf);
	addStack(sbuf);
	addInvoke(PUSHFOREACHBLOCK, 3, YOIXOBJECT_TYPE, sbuf);
	addStoreLocalBlock(names, values, sbuf);

	addLocalVariableDeclaration(lval, true, sbuf);
	disableLocalVariableCopy(lval);

	//
	// At this point the increment is on top of the stack and it will
	// be there after statement() finishes. We should take a look at
	// LVALUE_INCREMENT because it calls a  public method that might
	// omit some permissions checks. It probably wouldn't be hard to
	// restrict it to lvalues used by "foreach" loops - later.
	//

	labels = getNextBranchLabelNames(3);
	pushJumpData(labels[1], labels[2]);

	addLabel(labels[0], sbuf);
	addPush(lval, sbuf);
	addInvoke(GETSIZEOF, 1, INT_TYPE, sbuf);
	addInstruction(NAME_IFLE, labels[2], sbuf);
	statement(statement, sbuf);
	addLabel(labels[1], sbuf);
	addInstruction(NAME_DUP, sbuf);
	addPush(lval, sbuf);
	addCastToYoixObject(sbuf);		// unnecessary
	addInvoke(LVALUE_INCREMENT, 2, null, sbuf);
	addInstruction(NAME_GOTO, labels[0], sbuf);
	addLabel(labels[2], sbuf);
	addInstruction(NAME_POP, sbuf);
	addStack(sbuf);
	addInvoke(POPFOREACHBLOCK, 1, null, sbuf);
	yoixstack.popBlock();

	popJumpData();
    }


    private void
    statementFunction(SimpleNode node, StringBuffer sbuf) {

	YoixObject  lval;
	YoixObject  obj;
	boolean     qualified;
	String      name;

	addReferenceToNode(node, sbuf);
	addStack(sbuf);
	addInvoke(STATEMENT_FUNCTION, 2, null, sbuf);

	if (node.getChild0().type() == NAME) {
	    qualified = false;
	    name = node.getChild0().stringValue();
	} else {
	    qualified = true;
	    name = node.getChild1().stringValue();
	}

	lval = YoixBodyBlock.newDvalue(name);
	obj = createLocalVariable(lval, T_FUNCTION);
	if (qualified == false)
	    addLocalVariableDeclaration(obj, true, sbuf);
	else obj.putString(LOCALNAME, null);

	//
	// Need a new buffer because there may be more function defintions
	// in node. Functions that we add here are only called by the low
	// level Yoix function code (see YoixBodyFunction.java).
	//

	sbuf = new StringBuffer();
	addCompiledFunction(node, sbuf);
	trailer.append(sbuf);
    }


    private void
    statementIf(SimpleNode node, StringBuffer sbuf) {

	String  labels[];
	int     length;

	//
	// Our assembler removes dead code, so we shouldn't have to worry
	// about it here. For example, the unused goto and its associated
	// label that we generate when compiling
	//
	//     if (expr)
	//         return(TRUE);
	//     else return(FALSE);
	//
	// are completely ignored by the assembler.
	//
	// Right now the assembler doesn't eliminate unnecessary code, so
	// nonsense like,
	//
	//         goto L1
        //     L1: ...
	//
	// which we occasionally generate, will end up in the bytecode that
	// the assembler creates. We eventually will fix the assembler, so
	// we're not going to try to eliminate it here.
	//

	length = node.length();
	labels = getNextBranchLabelNames(length - 1);

	syncBranches(node, sbuf);
	branch(node.getChild0(), false, labels[0], sbuf);
	statement(node.getChild1(), sbuf);
	if (length == 3) {
	    addIfBranch(labels, false, sbuf);
	    statement(node.getChild2(), sbuf);
	}
	addIfJoin(labels, sbuf);
    }


    private void
    statementImport(SimpleNode node, StringBuffer sbuf) {

	SimpleNode  child;
	HashMap     modified;

	//
	// NOTE - we're implicitly assuming that synclocals.syncStatement()
	// also deals with operators like & (via another callback) that may
	// need to do things like disable caching of all locals in a block.
	// It's code we still have to add!!
	//

	child = node.getChild0();
	switch (child.type()) {
	    case LVALUE:
		modified = syncStatement(node, true, sbuf);
		addReferenceToNode(node, sbuf);
		addStack(sbuf);
		addInvoke(STATEMENT_IMPORT, 2, null, sbuf);
		restoreModifiedVariables(modified, sbuf);
		break;

	    default:
		addReferenceToNode(node, sbuf);
		addStack(sbuf);
		addInvoke(STATEMENT_IMPORT, 2, null, sbuf);
		break;
	}
    }


    private void
    statementInclude(SimpleNode node, StringBuffer sbuf) {

	//
	// If the file to be included is supplied as a string constant we
	// might be able to do more here. Would take some careful thought
	// and would probably have to be an adjustable decision. Not a big
	// deal so for now we just punt and let the interpreter handle the
	// include.
	//

	disableAllBlockLocalVariables(true, sbuf);
	addReferenceToNode(node, sbuf);
	addStack(sbuf);
	addInvoke(STATEMENT_INCLUDE, 2, null, sbuf);
    }


    private void
    statementList(SimpleNode node, int start, int end, StringBuffer sbuf) {

	int  n;

	for (n = start; n < end; n++)
	    statement(node.getChild(n), sbuf);
    }


    private void
    statementNamedBlock(SimpleNode node, int type, StringBuffer sbuf) {

	//
	// Right now named block statements are handed to the interpreter,
	// but we probably could handle most of them here without too much
	// trouble. They're not used much, so it's not a high priority but
	// we probably will revisit this stuff before too long.
	//
	// Permanently disabling all local variables is a precaution that's
	// a protection against eval(), which is a builtin that can be used
	// to hand the contents of a file or string to the interpreter. If
	// eval() was part of the grammar (like include) rather than being
	// a builtin that's stored in the reserved dictionary we could be
	// more lenient here and in other places. Suspect we'll eventually
	// make the change, but until then we need to permanently disable
	// all local variable copies.
	//

	disableAllBlockLocalVariables(true, sbuf);
	addReferenceToNode(node, sbuf);
	addPush(type, sbuf);
	addStack(sbuf);
	addInvoke(STATEMENT_NAMEDBLOCK, 3, null, sbuf);
    }


    private void
    statementQualifier(SimpleNode node, StringBuffer sbuf) {

	HashMap  modified;

	modified = syncStatement(node, true, sbuf);
	addReferenceToNode(node, sbuf);
	addStack(sbuf);
	addInvoke(STATEMENT_QUALIFIER, 2, null, sbuf);
	restoreModifiedVariables(modified, sbuf);
    }


    private void
    statementReturn(SimpleNode node, StringBuffer sbuf) {

	expression(node.getChild0(), sbuf);
	expressionReturn(sbuf);
    }


    private void
    statementSave(SimpleNode node, StringBuffer sbuf) {

	//
	// We need to completely disable the use of local copies for every
	// local variable used by node because saved values are restored
	// when the stack is popped, and we can't predict exactly when that
	// will happen.
	//
	// NOTE - we're implicitly assuming that synclocals.syncStatement()
	// also deals with operators like & (via another callback) that may
	// need to do things like disable caching of all locals in a block.
	// It's code we still have to add!!
	//

	disableLocalVariableCopies(syncStatement(node, false, sbuf));
	addReferenceToNode(node, sbuf);
	addStack(sbuf);
	addInvoke(STATEMENT_SAVE, 2, null, sbuf);
    }


    private void
    statementSwitch(SimpleNode node, StringBuffer sbuf) {

	StringBuffer  tmpbuf;
	SimpleNode    stmt;
	SimpleNode    child;
	ArrayList     data;
	HashMap       modified;
	String        break_label;
	int           length;
	int           n;

	//
	// We undoubtedly can (and should) do more here.
	//

	addReferenceToNode(node, sbuf);
	expression(node.getChild0(), sbuf);
	addEvaluateToYoixObject(sbuf);
	modified = synclocals.syncCaseLabels(node, sbuf);
	addInvoke(PICKSWITCHSTATMENTINDEX, 2, INT_TYPE, sbuf);
	restoreModifiedVariables(modified, sbuf);

	stmt = node.getChild1();
	length = stmt.length();

	break_label = getNextBranchLabelName();
	data = new ArrayList();
	data.add(new Integer(-1));
	data.add("default");
	data.add(break_label);
	pushSwitchData(data, break_label);

	tmpbuf = new StringBuffer();

	for (n = 0; n < length; n++) {
	    data.set(0, new Integer(n));
	    child = stmt.getChild(n);
	    switch (child.type()) {
		case BREAK:
		    if (n < length - 1)
			statement(child, tmpbuf);
		    break;

		case TAGGED:
		    if (n < length - 1 || child.getTaggedNode().type() != BREAK)
			statement(child, tmpbuf);
		    break;

		default:
		    statement(child, tmpbuf);
		    break;
	    }
	}

	addSwitchStart(sbuf);
	for (n = 1; n < data.size(); n += 2)
	    addSwitchCase(data.get(n).toString(), data.get(n+1).toString(), sbuf);
	addSwitchEnd(sbuf);

	sbuf.append(tmpbuf);

	addLabel(break_label, sbuf);
	popSwitchData();
    }


    private void
    statementSynchronized(SimpleNode node, StringBuffer sbuf) {

	HashMap  modified;

	//
	// Right now synchronized statements are handed to the interpreter
	// because our assembler lacks the exception handling syntax needed
	// to completely handle monitors. It's something we're going to add
	// to the assembler and at that point we will revisit this stuff.
	//
	// NOTE - there's debugging support in the interpreter's version of
	// statementSynchronized(). Not convinced it's worth keeping, but
	// even if it is perhaps complied code can completely ignore it??
	// Need to think about it some. Maybe it can be a "one-time" check
	// of VM.debug at compile time - if the bit is on when the compiler
	// we let the interpreter handle everything, otherwise we ignore
	// changes to the bit and only generate code that's really needed
	// to implement statementSynchronized().
	//
	// NOTE - we're implicitly assuming that synclocals.syncStatement()
	// also deals with operators like & (via another callback) that may
	// need to do things like disable caching of all locals in a block.
	// It's code we still have to add!!
	// 

	modified = syncStatement(node, true, sbuf);
	addReferenceToNode(node, sbuf);
	addStack(sbuf);
	addInvoke(STATEMENT_SYNCHRONIZED, 2, null, sbuf);
	restoreModifiedVariables(modified, sbuf);
    }


    private void
    statementTagged(SimpleNode node, StringBuffer sbuf) {

	YoixObject  tag;

	tag = node.getTaggedLocation();
	addComment(tag.toString().trim(), false, sbuf);

	if (addtags) {
	    yoixstack.pushYoixObjectClone(node.getTaggedLocation());
	    addReferenceToNode(node, sbuf);
	    addStack(sbuf);
	    addInvoke(PUSHTAG, 2, null, sbuf);
	}

	statement(node.getTaggedNode(), sbuf);

	if (addtags) {
	    addStack(sbuf);
	    addInvoke(POPTAG, 1, null, sbuf);
	    yoixstack.popYoixObject();
	}
    }


    private void
    statementTry(SimpleNode node, StringBuffer sbuf) {

	HashMap  modified;

	//
	// Right now try statements are handed to the interpreter because
	// our assembler lacks the exception handling syntax that would be
	// needed to completely handle monitors. It's something we're going
	// to add to the assembler and at that point there's a small chance
	// we'll revisit this stuff.
	//
	// NOTE - we're implicitly assuming that synclocals.syncStatement()
	// also deals with operators like & (via another callback) that may
	// need to do things like disable caching of all locals in a block.
	// It's code we still have to add!!
	//

	modified = syncStatement(node, true, sbuf);
	addReferenceToNode(node, sbuf);
	addStack(sbuf);
	addInvoke(STATEMENT_TRY, 2, null, sbuf);
	restoreModifiedVariables(modified, sbuf);
    }


    private void
    statementTypedef(SimpleNode node, StringBuffer sbuf) {

	addReferenceToNode(node, sbuf);
	addStack(sbuf);
	addInvoke(STATEMENT_TYPEDEF, 2, null, sbuf);
    }


    private void
    statementWhile(SimpleNode node, StringBuffer sbuf) {

	String  labels[];


	labels = getNextBranchLabelNames(3);
	pushJumpData(labels[1], labels[2]);

	syncBranches(node, sbuf);
	addInstruction(NAME_GOTO, labels[1], sbuf);
	addLabel(labels[0], sbuf);
	statement(node.getChild1(), sbuf);
	addLabel(labels[1], sbuf);
	branch(node.getChild0(), true, labels[0], sbuf);
	addLabel(labels[2], sbuf);
	popJumpData();
    }


    private void
    syncBranches(SimpleNode node, StringBuffer sbuf) {

	if (current_localcopymodel != LOCALCOPYMODEL_NONE)
	    synclocals.syncBranches(node, sbuf);
    }


    private HashMap
    syncStatement(SimpleNode node, boolean wantmodified, StringBuffer sbuf) {

	return(synclocals.syncStatement(node, wantmodified, sbuf));
    }


    private SimpleNode
    translateScript(String path) {

	SimpleNode  tree;
	Reader      stream;

	if ((stream = YoixMisc.getParserReader(path)) != null) {
	    tree = Yoix.translateStream(stream, path, addtags, PARSER_YOIX);
	    try {
		stream.close();
	    }
	    catch(IOException e) {}
	} else tree = null;

	return(tree);
    }
}

