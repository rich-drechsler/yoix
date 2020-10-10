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
import java.util.*;

interface YoixCompilerConstants

    extends YoixConstants,
	    JVMConstants,
	    JVMOpcodes

{

    static final String  COMPILER_VERSION = "0.9.1";
    static final String  COMPILER_TIMESTAMP = "Fri Oct 23 09:06:19 EDT 2009";

    //
    // The first few definitions are strings that the compiler uses when it
    // builds names that it assigns to various constructs.
    //

    static final String  COMPILED_METHODNAME = "compiledMethod";
    static final String  COMPILED_SCRIPTNAME = "compiledScript";
    static final String  COMPILED_CLASSNAME = YOIXPACKAGE + ".$_CompiledClass";
    static final String  COMPILED_VARIABLENAME = "$N";
    static final String  COMPILED_BLOCKNAME = "$B";
    static final String  COMPILED_LABLENAME = "L";

    static final String  EXECUTE_SCRIPTNAME = "executeScript";

    //
    // These are the names of the classes and primitive types the compiler
    // will need.
    //

    static final String  JAVADOUBLE = "double";
    static final String  JAVAINT = "int";
    static final String  JAVASTRING = "java.lang.String";
    static final String  JAVAOBJECT = "java.lang.Object";
    static final String  SIMPLENODE = YOIXPACKAGE + ".SimpleNode";
    static final String  YOIXBODYBLOCK = YOIXPACKAGE + ".YoixBodyBlock";
    static final String  YOIXCOMPILER = YOIXPACKAGE + ".YoixCompiler";
    static final String  YOIXCOMPILERSUPPORT = YOIXPACKAGE + ".YoixCompilerSupport";
    static final String  YOIXINTERPRETER = YOIXPACKAGE + ".YoixInterpreter";
    static final String  YOIXOBJECT = YOIXPACKAGE + ".YoixObject";
    static final String  YOIXSTACK = YOIXPACKAGE + ".YoixStack";

    static final String  JAVAOBJECT_ARRAY = JAVAOBJECT + "[]";
    static final String  JAVASTRING_ARRAY = JAVASTRING + "[]";
    static final String  SIMPLENODE_ARRAY = SIMPLENODE + "[]";
    static final String  YOIXOBJECT_ARRAY = YOIXOBJECT + "[]";

    //
    // These are the official YoixObjects the compiler uses to keep track
    // of objects on the JVM's operand stack. They're YoixObjects because
    // the compiler uses the Yoix interpreter's stack. The strings stored
    // in each one should be the string that would be used in an assembly
    // language declaration of a variable of that type. Right now the only
    // exception is YOIXLVALUE_TYPE, which is only used to indicate that
    // corresponding object on the JVM operand stack represents an lvalue
    // and we often will have to generate code to "resolve" it.
    //
    // NOTE - the compiler uses "==" and "!=" to compare type objects, so
    // it should never call methods that leave cloned or duplicated type
    // objects on the stack!!!
    //

    static final YoixObject  DOUBLE_TYPE = YoixObject.newString(JAVADOUBLE);
    static final YoixObject  INT_TYPE = YoixObject.newString(JAVAINT);
    static final YoixObject  STRING_TYPE = YoixObject.newString(JAVASTRING);
    static final YoixObject  JAVAOBJECT_TYPE = YoixObject.newString(JAVAOBJECT);
    static final YoixObject  YOIXOBJECT_TYPE = YoixObject.newString(YOIXOBJECT);
    static final YoixObject  YOIXLVALUE_TYPE = YoixObject.newString("lvalue");

    //
    // The arguments passed to a compiled method have the following names.
    //

    static final String  NAMEOF_ARGS_ARGUMENT = "args";
    static final String  NAMEOF_STACK_ARGUMENT = "stack";
    static final String  NAMEOF_NODES_ARGUMENT = "nodes";
    static final String  NAMEOF_OBJECTS_ARGUMENT = "objects";
    static final String  NAMEOF_GLOBAL_ARGUMENT = "global";

    //
    // These are the names of static fields that are created when a script
    // is compiled. They're initialized by reflection when the interpreter
    // first tries to execute the compiled script's class.
    //

    static final String  NAMEOF_NODES_FIELD = "nodes";
    static final String  NAMEOF_OBJECTS_FIELD = "objects";

    //
    // The compiler stores information about variables that it recognizes
    // in Yoix dictionaries that represent those variables and define the
    // the following fields.
    //

    static final String  BLOCKNAME = "block";
    static final String  LOCALINDEX = "index";
    static final String  LOCALNAME = "name";
    static final String  LOCALTYPE = "type";
    static final String  INDEXREGISTER = "register";

    //
    // Debugging various parts of the compiler can be controlled using the
    // following definitions.
    //

    static final int  DEBUG_COMPILER_OUTPUT = 0x01;
    static final int  DEBUG_ASSEMBLER_OUTPUT = 0x02;
    static final int  DEBUG_COMPILER_ERRORS = 0x04;

    //
    // String definitions of methods the compiler will need.
    //

    static final String  ABORT_BADOPERAND = YOIXCOMPILERSUPPORT + ".abortBadOperand()";
    static final String  ABORT_ILLEGALJUMP = YOIXCOMPILERSUPPORT + ".abortIllegalJump()";
    static final String  ABORT_TYPECHECK = YOIXCOMPILERSUPPORT + ".abortTypecheck()";
    static final String  ASSIGNOBJECT_BYINDEX = YOIXCOMPILERSUPPORT + ".assignObject(" + YOIXOBJECT + ", " + YOIXOBJECT + ", int)";
    static final String  ASSIGNOBJECT_BYINDEX_VOID = YOIXCOMPILERSUPPORT + ".assignObjectVoid(" + YOIXOBJECT + ", " + YOIXOBJECT + ", int)";
    static final String  ASSIGNOBJECT_BYLVALUE = YOIXCOMPILERSUPPORT + ".assignObject(" + YOIXOBJECT + ", " + YOIXOBJECT + ")";
    static final String  ASSIGNOBJECT_BYLVALUE_VOID = YOIXCOMPILERSUPPORT + ".assignObjectVoid(" + YOIXOBJECT + ", " + YOIXOBJECT + ")";
    static final String  BOOLEANVALUE_0 = YOIXOBJECT + ".booleanValue()";
    static final String  BOOLEANVALUE_1 = YOIXINTERPRETER + ".booleanValue(" + YOIXOBJECT + ")";
    static final String  DECLAREVARIABLE = YOIXCOMPILERSUPPORT + ".declareVariable(" + SIMPLENODE + ", " + JAVASTRING + ", int, " + YOIXSTACK + ")";
    static final String  DECLAREVARIABLE_VOID = YOIXCOMPILERSUPPORT + ".declareVariableVoid(" + SIMPLENODE + ", " + JAVASTRING + ", int, " + YOIXSTACK + ")";
    static final String  DOUBLEVALUE = YOIXOBJECT + ".doubleValue()";
    static final String  EXPRESSION_ARITHMETIC = YOIXCOMPILERSUPPORT + ".expressionArithmetic(" + YOIXOBJECT + ", " + YOIXOBJECT + ", int, " + YOIXSTACK + ")";
    static final String  EXPRESSION_ATTRIBUTE = YOIXCOMPILERSUPPORT + ".expressionAttribute(" + SIMPLENODE + ", " + YOIXSTACK + ")";
    static final String  EXPRESSION_BITWISE = YOIXCOMPILERSUPPORT + ".expressionBitwise(" + YOIXOBJECT + ", " + YOIXOBJECT + ", int, " + YOIXSTACK + ")";
    static final String  EXPRESSION_CAST = YOIXCOMPILERSUPPORT + ".expressionCast(" + YOIXOBJECT + ", " + JAVASTRING + ", " + YOIXSTACK + ")";
    static final String  EXPRESSION_INITIALIZER = YOIXCOMPILERSUPPORT + ".expressionInitializer(" + SIMPLENODE + ", " + YOIXSTACK + ")";
    static final String  EXPRESSION_INSTANCEOF = YOIXCOMPILERSUPPORT + ".expressionInstanceof(" + YOIXOBJECT + ", " + JAVASTRING + ", " + YOIXSTACK + ")";
    static final String  EXPRESSION_NEW = YOIXCOMPILERSUPPORT + ".expressionNew(" + SIMPLENODE + ", " + YOIXSTACK + ")";
    static final String  EXPRESSION_POSTDECREMENT_BYINDEX = YOIXCOMPILERSUPPORT + ".expressionPostDecrement(" + YOIXOBJECT + ", int, " + YOIXSTACK + ")";
    static final String  EXPRESSION_POSTDECREMENT_BYINDEX_VOID = YOIXCOMPILERSUPPORT + ".expressionPostDecrementVoid(" + YOIXOBJECT + ", int, " + YOIXSTACK + ")";
    static final String  EXPRESSION_POSTDECREMENT_BYLVALUE = YOIXCOMPILERSUPPORT + ".expressionPostDecrement(" + YOIXOBJECT + ", " + YOIXSTACK + ")";
    static final String  EXPRESSION_POSTDECREMENT_BYLVALUE_VOID = YOIXCOMPILERSUPPORT + ".expressionPostDecrementVoid(" + YOIXOBJECT + ", " + YOIXSTACK + ")";
    static final String  EXPRESSION_POSTINCREMENT_BYINDEX = YOIXCOMPILERSUPPORT + ".expressionPostIncrement(" + YOIXOBJECT + ", int, " + YOIXSTACK + ")";
    static final String  EXPRESSION_POSTINCREMENT_BYINDEX_VOID = YOIXCOMPILERSUPPORT + ".expressionPostIncrementVoid(" + YOIXOBJECT + ", int, " + YOIXSTACK + ")";
    static final String  EXPRESSION_POSTINCREMENT_BYLVALUE = YOIXCOMPILERSUPPORT + ".expressionPostIncrement(" + YOIXOBJECT + ", " + YOIXSTACK + ")";
    static final String  EXPRESSION_POSTINCREMENT_BYLVALUE_VOID = YOIXCOMPILERSUPPORT + ".expressionPostIncrementVoid(" + YOIXOBJECT + ", " + YOIXSTACK + ")";
    static final String  EXPRESSION_PREDECREMENT_BYINDEX = YOIXCOMPILERSUPPORT + ".expressionPreDecrement(" + YOIXOBJECT + ", int, " + YOIXSTACK + ")";
    static final String  EXPRESSION_PREDECREMENT_BYINDEX_VOID = YOIXCOMPILERSUPPORT + ".expressionPreDecrementVoid(" + YOIXOBJECT + ", int, " + YOIXSTACK + ")";
    static final String  EXPRESSION_PREDECREMENT_BYLVALUE = YOIXCOMPILERSUPPORT + ".expressionPreDecrement(" + YOIXOBJECT + ", " + YOIXSTACK + ")";
    static final String  EXPRESSION_PREDECREMENT_BYLVALUE_VOID = YOIXCOMPILERSUPPORT + ".expressionPreDecrementVoid(" + YOIXOBJECT + ", " + YOIXSTACK + ")";
    static final String  EXPRESSION_PREINCREMENT_BYINDEX = YOIXCOMPILERSUPPORT + ".expressionPreIncrement(" + YOIXOBJECT + ", int, " + YOIXSTACK + ")";
    static final String  EXPRESSION_PREINCREMENT_BYINDEX_VOID = YOIXCOMPILERSUPPORT + ".expressionPreIncrementVoid(" + YOIXOBJECT + ", int, " + YOIXSTACK + ")";
    static final String  EXPRESSION_PREINCREMENT_BYLVALUE = YOIXCOMPILERSUPPORT + ".expressionPreIncrement(" + YOIXOBJECT + ", " + YOIXSTACK + ")";
    static final String  EXPRESSION_PREINCREMENT_BYLVALUE_VOID = YOIXCOMPILERSUPPORT + ".expressionPreIncrementVoid(" + YOIXOBJECT + ", " + YOIXSTACK + ")";
    static final String  EXPRESSION_RELATIONAL = YOIXCOMPILERSUPPORT + ".expressionRelational(" + YOIXOBJECT + ", " + YOIXOBJECT + ", int, " + YOIXSTACK + ")";
    static final String  EXPRESSION_RERELATIONAL = YOIXCOMPILERSUPPORT + ".expressionRERelational(" + YOIXOBJECT + ", " + YOIXOBJECT + ", int, " + YOIXSTACK + ")";
    static final String  EXPRESSION_SHIFT = YOIXCOMPILERSUPPORT + ".expressionShift(" + YOIXOBJECT + ", " + YOIXOBJECT + ", int, " + YOIXSTACK + ")";
    static final String  EXPRESSION_UNARY = YOIXCOMPILERSUPPORT + ".expressionUnary(" + YOIXOBJECT + ", int, " + YOIXSTACK + ")";
    static final String  FUNCTION_CALL = YOIXCOMPILERSUPPORT + ".functionCall(" + YOIXOBJECT + ", " + YOIXOBJECT_ARRAY + ", " + YOIXSTACK + ")";
    static final String  GETDOUBLE_BYINDEX_1 = YOIXOBJECT + ".getDouble(int)";
    static final String  GETDOUBLE_BYINDEX_2 = YOIXOBJECT + ".getDouble(int, double)";
    static final String  GETDOUBLE_BYNAME_1 = YOIXOBJECT + ".getDouble(" + JAVASTRING + ")";
    static final String  GETDOUBLE_BYNAME_2 = YOIXOBJECT + ".getDouble(" + JAVASTRING + ", double)";
    static final String  GETINT_BYINDEX_1 = YOIXOBJECT + ".getInt(int)";
    static final String  GETINT_BYINDEX_2 = YOIXOBJECT + ".getInt(int, int)";
    static final String  GETINT_BYNAME_1 = YOIXOBJECT + ".getInt(" + JAVASTRING + ")";
    static final String  GETINT_BYNAME_2 = YOIXOBJECT + ".getInt(" + JAVASTRING + ", int)";
    static final String  GETOBJECT_BYINDEX = YOIXOBJECT + ".getObject(int)";
    static final String  GETOBJECT_BYNAME = YOIXOBJECT + ".getObject(" + JAVASTRING + ")";
    static final String  INTVALUE = YOIXOBJECT + ".intValue()";
    static final String  LVALUE_BYEXPRESSION = YOIXCOMPILERSUPPORT + ".lvalueByExpression(" + YOIXOBJECT + ", " + YOIXOBJECT + ")";
    static final String  LVALUE_BYNAME = YOIXCOMPILERSUPPORT + ".lvalueByName(" + YOIXOBJECT + ", " + JAVASTRING + ")";
    static final String  LVALUE_HANDLER = YOIXCOMPILERSUPPORT + ".lvalue(" + SIMPLENODE + ", " + YOIXSTACK + ")";
    static final String  LVALUE_INCREMENT = YOIXCOMPILERSUPPORT + ".lvalueIncrement(int, " + YOIXOBJECT + ")";
    static final String  NEWARRAY = YOIXOBJECT + ".newArray(int)";
    static final String  NEWBLOCKLVALUE_BYLEVEL = YOIXCOMPILERSUPPORT + ".newBlockLvalue(int, int)";
    static final String  NEWBLOCKLVALUE_BYNAME = YOIXCOMPILERSUPPORT + ".newBlockLvalue(" + JAVASTRING + ")";
    static final String  NEWEMPTY = YOIXOBJECT + ".newEmpty()";
    static final String  NEWDOUBLE = YOIXOBJECT + ".newDouble(double)";
    static final String  NEWGLOBALLVALUE = YOIXCOMPILERSUPPORT + ".newGlobalLvalue()";
    static final String  NEWINT = YOIXOBJECT + ".newInt(int)";
    static final String  NEWSTRING = YOIXOBJECT + ".newString(" + JAVASTRING + ")";
    static final String  NEWTHISLVALUE = YOIXCOMPILERSUPPORT + ".newThisLvalue()";
    static final String  PICKSWITCHSTATMENTINDEX = YOIXCOMPILERSUPPORT + ".pickSwitchStatementIndex(" + SIMPLENODE + ", " + YOIXOBJECT + ")";
    static final String  POPFOREACHBLOCK = YOIXCOMPILERSUPPORT + ".popForEachBlock(" + YOIXSTACK + ")";
    static final String  POPTAG = YOIXCOMPILERSUPPORT + ".popTag(" + YOIXSTACK + ")";
    static final String  PUSHFOREACHBLOCK = YOIXCOMPILERSUPPORT + ".pushForEachBlock(" + YOIXOBJECT + ", " + JAVASTRING + ", " + YOIXSTACK + ")";
    static final String  PUSHTAG = YOIXCOMPILERSUPPORT + ".pushTag(" + SIMPLENODE + ", " + YOIXSTACK + ")";
    static final String  STATEMENT_BEGINCOMPOUND = YOIXCOMPILERSUPPORT + ".statementBeginCompound(" + SIMPLENODE + ", " + YOIXSTACK + ")";
    static final String  STATEMENT_ENDCOMPOUND = YOIXCOMPILERSUPPORT + ".statementEndCompound(" + YOIXSTACK + ")";
    static final String  STATEMENT_EOF = YOIXCOMPILERSUPPORT + ".statementEOF(" + YOIXSTACK + ")";
    static final String  STATEMENT_EXIT = YOIXCOMPILERSUPPORT + ".statementExit(" + YOIXOBJECT + ", " + YOIXSTACK + ")";
    static final String  STATEMENT_FINALLY = YOIXCOMPILERSUPPORT + ".statementFinally(" + SIMPLENODE + ", " + YOIXSTACK + ")";
    static final String  STATEMENT_FUNCTION = YOIXCOMPILERSUPPORT + ".statementFunction(" + SIMPLENODE + ", " + YOIXSTACK + ")";
    static final String  STATEMENT_IMPORT = YOIXCOMPILERSUPPORT + ".statementImport(" + SIMPLENODE + ", " + YOIXSTACK + ")";
    static final String  STATEMENT_INCLUDE = YOIXCOMPILERSUPPORT + ".statementInclude(" + SIMPLENODE + ", " + YOIXSTACK + ")";
    static final String  STATEMENT_NAMEDBLOCK = YOIXCOMPILERSUPPORT + ".statementNamedBlock(" + SIMPLENODE + ", int, " + YOIXSTACK + ")";
    static final String  STATEMENT_QUALIFIER = YOIXCOMPILERSUPPORT + ".statementQualifier(" + SIMPLENODE + ", " + YOIXSTACK + ")";
    static final String  STATEMENT_SAVE = YOIXCOMPILERSUPPORT + ".statementSave(" + SIMPLENODE + ", " + YOIXSTACK + ")";
    static final String  STATEMENT_SYNCHRONIZED = YOIXCOMPILERSUPPORT + ".statementSynchronized(" + SIMPLENODE + ", " + YOIXSTACK + ")";
    static final String  STATEMENT_TRY = YOIXCOMPILERSUPPORT + ".statementTry(" + SIMPLENODE + ", " + YOIXSTACK + ")";
    static final String  STATEMENT_TYPEDEF = YOIXCOMPILERSUPPORT + ".statementTypedef(" + JAVAOBJECT + ", " + YOIXSTACK + ")";
    static final String  STOREDOUBLE = YOIXCOMPILERSUPPORT + ".storeDouble(double, " + YOIXOBJECT + ", int)";
    static final String  STOREINT = YOIXCOMPILERSUPPORT + ".storeInt(int, " + YOIXOBJECT + ", int)";
    static final String  STOREOBJECT = YOIXCOMPILERSUPPORT + ".storeObject(" + YOIXOBJECT + ", " + YOIXOBJECT + ", int)";
    static final String  TYPECHECK_IFNOTPOINTER = YOIXCOMPILERSUPPORT + ".typecheckIfNotPointer(" + YOIXOBJECT + ")";

    static final String  GETLENGTH = YOIXOBJECT + ".length()";
    static final String  GETOFFSET = YOIXOBJECT + ".offset()";
    static final String  GETSIZEOF = YOIXOBJECT + ".sizeof()";
    static final String  RESOLVE = YOIXOBJECT + ".resolve()";
    static final String  RESOLVECLONE = YOIXOBJECT + ".resolveClone()";

    //
    // These are the official external declarations for the methods we just
    // defined using the syntax that the assembler wants.
    //

    static final String  EXTERN_ABORT_BADOPERAND = "extern static void " + ABORT_BADOPERAND;
    static final String  EXTERN_ABORT_ILLEGALJUMP = "extern static void " + ABORT_ILLEGALJUMP;
    static final String  EXTERN_ABORT_TYPECHECK = "extern static void " + ABORT_TYPECHECK;
    static final String  EXTERN_ASSIGNOBJECT_BYINDEX = "extern static " + YOIXOBJECT + " " + ASSIGNOBJECT_BYINDEX;
    static final String  EXTERN_ASSIGNOBJECT_BYINDEX_VOID = "extern static void " + ASSIGNOBJECT_BYINDEX_VOID;
    static final String  EXTERN_ASSIGNOBJECT_BYLVALUE = "extern static " + YOIXOBJECT + " " + ASSIGNOBJECT_BYLVALUE;
    static final String  EXTERN_ASSIGNOBJECT_BYLVALUE_VOID = "extern static void " + ASSIGNOBJECT_BYLVALUE_VOID;
    static final String  EXTERN_BOOLEANVALUE_0 = "extern boolean " + BOOLEANVALUE_0;
    static final String  EXTERN_BOOLEANVALUE_1 = "extern static boolean " + BOOLEANVALUE_1;
    static final String  EXTERN_DECLAREVARIABLE = "extern static int " + DECLAREVARIABLE;
    static final String  EXTERN_DECLAREVARIABLE_VOID = "extern static void " + DECLAREVARIABLE_VOID;
    static final String  EXTERN_DOUBLEVALUE = "extern double " + DOUBLEVALUE;
    static final String  EXTERN_EXPRESSION_ARITHMETIC = "extern static " + YOIXOBJECT + " " + EXPRESSION_ARITHMETIC;
    static final String  EXTERN_EXPRESSION_ATTRIBUTE = "extern static " + YOIXOBJECT + " " + EXPRESSION_ATTRIBUTE;
    static final String  EXTERN_EXPRESSION_BITWISE = "extern static " + YOIXOBJECT + " " + EXPRESSION_BITWISE;
    static final String  EXTERN_EXPRESSION_CAST = "extern static " + YOIXOBJECT + " " + EXPRESSION_CAST;
    static final String  EXTERN_EXPRESSION_INITIALIZER = "extern static " + YOIXOBJECT + " " + EXPRESSION_INITIALIZER;
    static final String  EXTERN_EXPRESSION_INSTANCEOF = "extern static boolean " + EXPRESSION_INSTANCEOF;
    static final String  EXTERN_EXPRESSION_NEW = "extern static " + YOIXOBJECT + " " + EXPRESSION_NEW;
    static final String  EXTERN_EXPRESSION_POSTDECREMENT_BYINDEX = "extern static " + YOIXOBJECT + " " + EXPRESSION_POSTDECREMENT_BYINDEX;
    static final String  EXTERN_EXPRESSION_POSTDECREMENT_BYINDEX_VOID = "extern static void " + EXPRESSION_POSTDECREMENT_BYINDEX_VOID;
    static final String  EXTERN_EXPRESSION_POSTDECREMENT_BYLVALUE = "extern static " + YOIXOBJECT + " " + EXPRESSION_POSTDECREMENT_BYLVALUE;
    static final String  EXTERN_EXPRESSION_POSTDECREMENT_BYLVALUE_VOID = "extern static void " + EXPRESSION_POSTDECREMENT_BYLVALUE_VOID;
    static final String  EXTERN_EXPRESSION_POSTINCREMENT_BYINDEX = "extern static " + YOIXOBJECT + " " + EXPRESSION_POSTINCREMENT_BYINDEX;
    static final String  EXTERN_EXPRESSION_POSTINCREMENT_BYINDEX_VOID = "extern static void " + EXPRESSION_POSTINCREMENT_BYINDEX_VOID;
    static final String  EXTERN_EXPRESSION_POSTINCREMENT_BYLVALUE = "extern static " + YOIXOBJECT + " " + EXPRESSION_POSTINCREMENT_BYLVALUE;
    static final String  EXTERN_EXPRESSION_POSTINCREMENT_BYLVALUE_VOID = "extern static void " + EXPRESSION_POSTINCREMENT_BYLVALUE_VOID;
    static final String  EXTERN_EXPRESSION_PREDECREMENT_BYINDEX = "extern static " + YOIXOBJECT + " " + EXPRESSION_PREDECREMENT_BYINDEX;
    static final String  EXTERN_EXPRESSION_PREDECREMENT_BYINDEX_VOID = "extern static void " + EXPRESSION_PREDECREMENT_BYINDEX_VOID;
    static final String  EXTERN_EXPRESSION_PREDECREMENT_BYLVALUE = "extern static " + YOIXOBJECT + " " + EXPRESSION_PREDECREMENT_BYLVALUE;
    static final String  EXTERN_EXPRESSION_PREDECREMENT_BYLVALUE_VOID = "extern static void " + EXPRESSION_PREDECREMENT_BYLVALUE_VOID;
    static final String  EXTERN_EXPRESSION_PREINCREMENT_BYINDEX = "extern static " + YOIXOBJECT + " " + EXPRESSION_PREINCREMENT_BYINDEX;
    static final String  EXTERN_EXPRESSION_PREINCREMENT_BYINDEX_VOID = "extern static void " + EXPRESSION_PREINCREMENT_BYINDEX_VOID;
    static final String  EXTERN_EXPRESSION_PREINCREMENT_BYLVALUE = "extern static " + YOIXOBJECT + " " + EXPRESSION_PREINCREMENT_BYLVALUE;
    static final String  EXTERN_EXPRESSION_PREINCREMENT_BYLVALUE_VOID = "extern static void " + EXPRESSION_PREINCREMENT_BYLVALUE_VOID;
    static final String  EXTERN_EXPRESSION_RELATIONAL = "extern static boolean " + EXPRESSION_RELATIONAL;
    static final String  EXTERN_EXPRESSION_RERELATIONAL = "extern static boolean " + EXPRESSION_RERELATIONAL;
    static final String  EXTERN_EXPRESSION_SHIFT = "extern static " + YOIXOBJECT + " " + EXPRESSION_SHIFT;
    static final String  EXTERN_EXPRESSION_UNARY = "extern static " + YOIXOBJECT + " " + EXPRESSION_UNARY;
    static final String  EXTERN_FUNCTION_CALL = "extern static " + YOIXOBJECT + " " + FUNCTION_CALL;
    static final String  EXTERN_GETDOUBLE_BYINDEX_1 = "extern double " + GETDOUBLE_BYINDEX_1;
    static final String  EXTERN_GETDOUBLE_BYINDEX_2 = "extern double " + GETDOUBLE_BYINDEX_2;
    static final String  EXTERN_GETDOUBLE_BYNAME_1 = "extern double " + GETDOUBLE_BYNAME_1;
    static final String  EXTERN_GETDOUBLE_BYNAME_2 = "extern double " + GETDOUBLE_BYNAME_2;
    static final String  EXTERN_GETINT_BYINDEX_1 = "extern int " + GETINT_BYINDEX_1;
    static final String  EXTERN_GETINT_BYINDEX_2 = "extern int " + GETINT_BYINDEX_2;
    static final String  EXTERN_GETINT_BYNAME_1 = "extern int " + GETINT_BYNAME_1;
    static final String  EXTERN_GETINT_BYNAME_2 = "extern int " + GETINT_BYNAME_2;
    static final String  EXTERN_GETOBJECT_BYINDEX = "extern " + YOIXOBJECT + " "  + GETOBJECT_BYINDEX;
    static final String  EXTERN_GETOBJECT_BYNAME = "extern " + YOIXOBJECT + " "  + GETOBJECT_BYNAME;
    static final String  EXTERN_INTVALUE = "extern int " + INTVALUE;
    static final String  EXTERN_LVALUE_BYEXPRESSION = "extern static " + YOIXOBJECT + " " + LVALUE_BYEXPRESSION;
    static final String  EXTERN_LVALUE_BYNAME = "extern static " + YOIXOBJECT + " " + LVALUE_BYNAME;
    static final String  EXTERN_LVALUE_HANDLER = "extern static " + YOIXOBJECT + " " + LVALUE_HANDLER;
    static final String  EXTERN_LVALUE_INCREMENT = "extern static void " + LVALUE_INCREMENT;
    static final String  EXTERN_NEWARRAY = "extern static " + YOIXOBJECT + " " + NEWARRAY;
    static final String  EXTERN_NEWBLOCKLVALUE_BYLEVEL = "extern static " + YOIXOBJECT + " " + NEWBLOCKLVALUE_BYLEVEL;
    static final String  EXTERN_NEWBLOCKLVALUE_BYNAME = "extern static " + YOIXOBJECT + " " + NEWBLOCKLVALUE_BYNAME;
    static final String  EXTERN_NEWDOUBLE = "extern static " + YOIXOBJECT + " " + NEWDOUBLE;
    static final String  EXTERN_NEWEMPTY = "extern static " + YOIXOBJECT + " " + NEWEMPTY;
    static final String  EXTERN_NEWGLOBALLVALUE = "extern static " + YOIXOBJECT + " " + NEWGLOBALLVALUE;
    static final String  EXTERN_NEWINT = "extern static " + YOIXOBJECT + " " + NEWINT;
    static final String  EXTERN_NEWSTRING = "extern static " + YOIXOBJECT + " " + NEWSTRING;
    static final String  EXTERN_NEWTHISLVALUE = "extern static " + YOIXOBJECT + " " + NEWTHISLVALUE;
    static final String  EXTERN_PICKSWITCHSTATMENTINDEX = "extern static int " + PICKSWITCHSTATMENTINDEX;
    static final String  EXTERN_POPFOREACHBLOCK = "extern static void " + POPFOREACHBLOCK;
    static final String  EXTERN_POPTAG = "extern static void " + POPTAG;
    static final String  EXTERN_PUSHFOREACHBLOCK = "extern static " + YOIXOBJECT + " " + PUSHFOREACHBLOCK;
    static final String  EXTERN_PUSHTAG = "extern static void " + PUSHTAG;
    static final String  EXTERN_STATEMENT_BEGINCOMPOUND = "extern static " + YOIXOBJECT + " " + STATEMENT_BEGINCOMPOUND;
    static final String  EXTERN_STATEMENT_ENDCOMPOUND = "extern static void " + STATEMENT_ENDCOMPOUND;
    static final String  EXTERN_STATEMENT_EOF = "extern static void " + STATEMENT_EOF;
    static final String  EXTERN_STATEMENT_EXIT = "extern static void " + STATEMENT_EXIT;
    static final String  EXTERN_STATEMENT_FINALLY = "extern static void " + STATEMENT_FINALLY;
    static final String  EXTERN_STATEMENT_FUNCTION = "extern static void " + STATEMENT_FUNCTION;
    static final String  EXTERN_STATEMENT_IMPORT = "extern static void " + STATEMENT_IMPORT;
    static final String  EXTERN_STATEMENT_INCLUDE = "extern static void " + STATEMENT_INCLUDE;
    static final String  EXTERN_STATEMENT_NAMEDBLOCK = "extern static void " + STATEMENT_NAMEDBLOCK;
    static final String  EXTERN_STATEMENT_QUALIFIER = "extern static void " + STATEMENT_QUALIFIER;
    static final String  EXTERN_STATEMENT_SAVE = "extern static void " + STATEMENT_SAVE;
    static final String  EXTERN_STATEMENT_SYNCHRONIZED = "extern static void " + STATEMENT_SYNCHRONIZED;
    static final String  EXTERN_STATEMENT_TRY = "extern static void " + STATEMENT_TRY;
    static final String  EXTERN_STATEMENT_TYPEDEF = "extern static void " + STATEMENT_TYPEDEF;
    static final String  EXTERN_STOREDOUBLE = "extern static void " + STOREDOUBLE;
    static final String  EXTERN_STOREINT = "extern static void " + STOREINT;
    static final String  EXTERN_STOREOBJECT = "extern static void " + STOREOBJECT;
    static final String  EXTERN_TYPECHECK_IFNOTPOINTER = "extern static void " + TYPECHECK_IFNOTPOINTER;

    static final String  EXTERN_GETLENGTH = "extern int " + GETLENGTH;
    static final String  EXTERN_GETOFFSET = "extern int " + GETOFFSET;
    static final String  EXTERN_GETSIZEOF = "extern int " + GETSIZEOF;
    static final String  EXTERN_RESOLVE = "extern " + YOIXOBJECT + " " + RESOLVE;
    static final String  EXTERN_RESOLVECLONE = "extern " + YOIXOBJECT + " " + RESOLVECLONE;
}

