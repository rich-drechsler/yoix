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
import java.util.*;
import java.util.regex.*;

public
class JVMAssembler

    implements JVMConstants,
	       JVMPatterns

{

    //
    // An assembler that builds Java class files from instructions read from
    // specially formatted "source code" (to be described later). Right now
    // scanning is handled by using the patterns defined in JVMPatterns.java.
    // The instructions recognized when we assemble a method are defined in
    // JVMOpcodes.java. The collection includes the complete JVM instruction
    // set plus some "virtual instructions" that the assembler automatically
    // translates into one or more real JVM opcodes. The JVMInstruction.java
    // class is used to represent every instruction that's recognized in the
    // source code and the construction of an array of JVMInstructions that
    // accurately represent the source code is what's done in the first pass
    // when we're asked to assemble a method.
    //
    // We eventually will try to provide some real documentation, but until
    // then you'll have to read the source code. Unfortunately you may have
    // trouble following much of the code without a solid understanding of
    // the Java Virtual Machine, so you might need to consult
    //
    //     http://java.sun.com/docs/books/jvms/second_edition/html/VMSpecTOC.doc.html
    //
    // while you're looking through our source code.
    //
    // NOTE - even though we build successor and predecessor lists for each
    // instruction we currently don't use them to validate branches, so you
    // can still write code that we'll assemble but the JVM may reject when
    // it validates our work. We'll eventually try to be more complete, but
    // until then don't be surpised if a successfully assembled class file
    // isn't accepted by the JVM.
    //
    // NOTE - we decided to store some of the important data in class fields
    // rather than local method variables, mostly to simplify method calls,
    // but it means using synchronization to protect them. In practice most
    // applications probably will create a new JVMAssembler when they want
    // to build a new class file from its "source code" description, so the
    // synchronization usually isn't required.
    //

    private JVMClassFile  classfile;
    private JVMTypeStack  typestack;
    private JVMMethod     method;
    private int           codemodel;

    //
    // The code assembler's first pass builds a new instructions[] array for
    // for each method in the class. After that the original source code is
    // discarded and most of the remaining of the work needed to assemble the
    // method uses data stored in the instructions[] array.
    //

    private JVMInstruction  instructions[];
    private int             nextinstruction;

    //
    // The bytecode[] array is where the actual JVM instructions are built,
    // but it's a temporary workspace used by lots of methods in this class,
    // so each JVMInstruction instruction is responsible for storing it's own
    // copy of the bytecode.
    //

    private byte  bytecode[];
    private int   nextbyte;

    //
    // We use these when we're processing a try/catch block.
    //

    private ArrayList  trystack;
    private ArrayList  exception_table;
    private boolean    intry;

    //
    // Error related fields.
    //

    private HashMap  errormessages;
    private int      errorlimit;
    private int      errorcount;

    //
    // Miscellaneous stuff.
    //

    private static final int  ALOAD_OPCODES[] = {OP_ALOAD_0, OP_ALOAD_1, OP_ALOAD_2, OP_ALOAD_3, OP_ALOAD};
    private static final int  DLOAD_OPCODES[] = {OP_DLOAD_0, OP_DLOAD_1, OP_DLOAD_2, OP_DLOAD_3, OP_DLOAD};
    private static final int  FLOAD_OPCODES[] = {OP_FLOAD_0, OP_FLOAD_1, OP_FLOAD_2, OP_FLOAD_3, OP_FLOAD};
    private static final int  ILOAD_OPCODES[] = {OP_ILOAD_0, OP_ILOAD_1, OP_ILOAD_2, OP_ILOAD_3, OP_ILOAD};
    private static final int  LLOAD_OPCODES[] = {OP_LLOAD_0, OP_LLOAD_1, OP_LLOAD_2, OP_LLOAD_3, OP_LLOAD};

    private static final int  ASTORE_OPCODES[] = {OP_ASTORE_0, OP_ASTORE_1, OP_ASTORE_2, OP_ASTORE_3, OP_ASTORE};
    private static final int  DSTORE_OPCODES[] = {OP_DSTORE_0, OP_DSTORE_1, OP_DSTORE_2, OP_DSTORE_3, OP_DSTORE};
    private static final int  FSTORE_OPCODES[] = {OP_FSTORE_0, OP_FSTORE_1, OP_FSTORE_2, OP_FSTORE_3, OP_FSTORE};
    private static final int  ISTORE_OPCODES[] = {OP_ISTORE_0, OP_ISTORE_1, OP_ISTORE_2, OP_ISTORE_3, OP_ISTORE};
    private static final int  LSTORE_OPCODES[] = {OP_LSTORE_0, OP_LSTORE_1, OP_LSTORE_2, OP_LSTORE_3, OP_LSTORE};

    private static final boolean  IS_COMMUTATIVE[] = new boolean[OPCODE_MNEMONICS.length];

    static {
	IS_COMMUTATIVE[OP_ADD] = true;
	IS_COMMUTATIVE[OP_MUL] = true;
	IS_COMMUTATIVE[OP_AND] = true;
	IS_COMMUTATIVE[OP_OR] = true;
	IS_COMMUTATIVE[OP_XOR] = true;

	IS_COMMUTATIVE[OP_DADD] = true;
	IS_COMMUTATIVE[OP_DMUL] = true;
	IS_COMMUTATIVE[OP_FADD] = true;
	IS_COMMUTATIVE[OP_FMUL] = true;
	IS_COMMUTATIVE[OP_IADD] = true;
	IS_COMMUTATIVE[OP_IMUL] = true;
	IS_COMMUTATIVE[OP_IAND] = true;
	IS_COMMUTATIVE[OP_IOR] = true;
	IS_COMMUTATIVE[OP_IXOR] = true;
	IS_COMMUTATIVE[OP_LADD] = true;
	IS_COMMUTATIVE[OP_LMUL] = true;
	IS_COMMUTATIVE[OP_LAND] = true;
	IS_COMMUTATIVE[OP_LOR] = true;
	IS_COMMUTATIVE[OP_LXOR] = true;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    JVMAssembler() {

	this(DEFAULT_CODE_MODEL, -1);
    }


    public
    JVMAssembler(int codemodel, int errorlimit) {

	this.codemodel = codemodel;
	this.errorlimit = (errorlimit <= 0) ? Integer.MAX_VALUE : errorlimit;
    }

    ///////////////////////////////////
    //
    // JVMAssembler Methods
    //
    ///////////////////////////////////

    public synchronized JVMClassFile
    assembleClass(String source)

	throws JVMAssemblerError

    {

	return(assembleClass(source, 1));
    }


    public synchronized JVMClassFile
    assembleClass(String source, int linenumber)

	throws JVMAssemblerError

    {

	return(source != null
	    ? assembleClass(source.split("\\n"), 0, linenumber)
	    : null
	);
    }


    public synchronized JVMClassFile
    assembleClass(String lines[], int index, int linenumber)

	throws JVMAssemblerError

    {

	JVMScanner  scanner;
	Pattern     pattern;
	Matcher     matcher;
	String      line;
	String      qualifiers;
	String      classname;
	String      superclass;
	int         length;

	//
	// Builds a JVM class file from the "assembly language" source code
	// that's stored in lines[].
	//
	// NOTE - there's lots of room for improvement here, particularly in
	// the way the scanner works. A JavaCC replacement might be overkill,
	// but probably deserves some consideration - later.
	//

	resetClassAssembler();

	if (lines != null) {
	    if ((length = lines.length) > 0 && index < length) {
		scanner = new JVMScanner(this);
		pattern = PATTERN_CLASS_DECLARATION;
		for (; index < length; index++, linenumber++) {
		    if ((line = lines[index]) != null) {
			if (scanner.consumeLine(lines, index) == false) {
			    //
			    // Right now pattern is set to null after we find
			    // the start of a class declaration, which means
			    // only blank lines can follow the end of the first
			    // class definition.
			    //
			    if (pattern != null) {
				matcher = pattern.matcher(lines[index]);
				if (matcher.find()) {
				    if (pattern == PATTERN_CLASS_DECLARATION) {
					if ((qualifiers = scanner.consumeToken(lines, index, PATTERN_CLASS_QUALIFIERS)) != null) {
					    scanner.consumeToken(lines, index, PATTERN_CLASS);
					    if ((classname = scanner.consumeToken(lines, index, PATTERN_QUALIFIED_NAME)) != null) {
						if (scanner.consumeToken(lines, index, PATTERN_EXTENDS) != null) {
						    if ((superclass = scanner.consumeToken(lines, index, PATTERN_QUALIFIED_NAME)) == null)	// should be an error??
							superclass = OBJECT_CLASS;
						} else superclass = OBJECT_CLASS;
						classfile = new JVMClassFile();
						classfile.storeThis(classname);
						classfile.storeSuper(superclass);
						classfile.storeAccessFlags(qualifiers);
						if (qualifiers.indexOf(ATTRIBUTE_DEPRECATED.toLowerCase()) >= 0)
						    classfile.storeDeprecatedClass();
						classfile.registerExtension(classname, superclass);

						//
						// This reads the body of the class definition
						// and nulls out every entry in lines[] that it
						// reads, which means our loop will skip them.
						// Good enough for now, but it probably should
						// return a count that we can use to increment
						// index and linenumber.
						//
						classScanner(lines, index, linenumber, PATTERN_CLOSE_BRACE);
					    }
					}
					pattern = null;
				    } else recordError("class file syntax error", lines[index], linenumber, line);
				} else recordError("class file syntax error", lines[index], linenumber, line);
			    } else recordError("class file syntax error", lines[index], linenumber, line);
			}
		    }
		}
	    }
	}

	handleErrors();
	return(classfile);
    }


    synchronized boolean
    assembleMethod(String source, JVMMethod method)

	throws JVMAssemblerError

    {

	return(assembleMethod(source.split("\\n"), 0, 1, null, method));
    }


    synchronized boolean
    assembleMethod(String source, int linenumber, JVMMethod method)

	throws JVMAssemblerError

    {

	return(assembleMethod(source.split("\\n"), 0, linenumber, null, method));
    }


    synchronized boolean
    assembleMethod(String lines[], int index, int linenumber, Pattern endpattern, JVMMethod method)

	throws JVMAssemblerError

    {

	boolean  result = false;

	//
	// Builds the instructions[] array from the assembly language stored
	// in lines[] and then uses instructions[] in several passes to build
 	// the method, which includes generating the bytecode, resolving all
	// branch labels, and calculating max_stack and max_locals for the
	// method.
	//

	resetMethodAssembler(method);

	if (methodScanner(lines, index, linenumber, endpattern)) {
	    if (methodAnalyzer()) {
		if (methodAssembler()) {
		    if (methodBuilder())
			result = true;
		}
	    }
	}

	return(result);
    }


    public int
    getCodeModel() {

	return(codemodel);
    }


    public int
    getErrorCount() {

	return(errorcount);
    }


    synchronized void
    handleErrors()

	throws JVMAssemblerError

    {

	handleErrors(null);
    }


    synchronized void
    handleErrors(String message)

	throws JVMAssemblerError

    {

	if (errorcount > 0)
	    throw(new JVMAssemblerError(message, getSortedErrors()));
    }


    synchronized void
    recordError(String message)

	throws JVMAssemblerError

    {

	recordError(message, null, -1, null);
    }


    synchronized void
    recordError(String message, JVMInstruction instruction)

	throws JVMAssemblerError

    {

	recordError(message, instruction.getLine(), instruction.getLineNumber(), instruction.getLine());
    }


    synchronized void
    recordError(String message, String line, int linenumber)

	throws JVMAssemblerError

    {

	recordError(message, line, linenumber, line);
    }


    synchronized void
    recordError(String message, String line, int linenumber, String original_line)

	throws JVMAssemblerError

    {

	StringBuffer  sbuf;
	Integer       key;
	String        indent;
	char          ch;
	int           length;
	int           n;

	//
	// Records information about an error, but only if the error happens
	// at a line that hasn't already triggered and error, and quits when
	// there are too many errors.
	//
	// NOTE - we're implicilty assuming here that there aren't any errors
	// in any generated code (e.g., for <init> and <clinit> methods). It
	// might be better if we checked method name and drop all line number
	// info if it's <clinit>. Not quite sure how to handle code added to
	// the start of <init> methods.
	//

	key = new Integer(linenumber);

	if (errormessages.containsKey(key) == false) {
	    sbuf = new StringBuffer();
	    sbuf.append("Error: ");
	    sbuf.append(message != null ? message : "no information");
	    sbuf.append("; Line: ");
	    if (linenumber > 0)
		sbuf.append(linenumber);
	    else sbuf.append("none");
	    if (original_line != null && original_line.length() > 0) {
		indent = "        ";
		sbuf.append("\n");
		if (linenumber > 0)
		    JVMMisc.appendRightAlignedInt(sbuf, linenumber, indent.length() - 2, ": ");
		else sbuf.append(indent);
		sbuf.append(original_line);
		sbuf.append("\n");
		sbuf.append(indent);
		n = 0;
		if (line != null) {
		    for (length = original_line.length() - line.length(); n < length; n++) {
			if (original_line.charAt(n) == '\t')		// ignoring stuff like backspace
			    sbuf.append("\t");
			else sbuf.append(" ");
		    }
		}
		for (length = original_line.length(); n < length; n++) {
		    if ((ch = original_line.charAt(n)) == ' ' || ch == '\t')
			sbuf.append(ch);
		    else break;
		}
		sbuf.append("^");
	    }
	    sbuf.append("\n");

	    message = sbuf.toString();
	    errormessages.put(key, message);
	    if (++errorcount >= errorlimit)
		handleErrors("The assembler error limit of " + errorlimit + " has been exceeded");
	}
    }


    synchronized void
    recordOpcodeError(int opcode, String lines[], int index, int linenumber, String original_line)

	throws JVMAssemblerError

    {

	Matcher  matcher = PATTERN_WHITESPACE_OR_COMMENT.matcher(lines[index]);

	switch (opcode) {
	    case OP_AALOAD:
	    case OP_AASTORE:
	    case OP_ACONST_NULL:
	    case OP_ALOAD_0:
	    case OP_ALOAD_1:
	    case OP_ALOAD_2:
	    case OP_ALOAD_3:
	    case OP_ARETURN:
	    case OP_ARRAYLENGTH:
	    case OP_ASTORE_0:
	    case OP_ASTORE_1:
	    case OP_ASTORE_2:
	    case OP_ASTORE_3:
	    case OP_ATHROW:
	    case OP_BALOAD:
	    case OP_BASTORE:
	    case OP_CALOAD:
	    case OP_CASTORE:
	    case OP_D2F:
	    case OP_D2I:
	    case OP_D2L:
	    case OP_DALOAD:
	    case OP_DASTORE:
	    case OP_DCMPG:
	    case OP_DCMPL:
	    case OP_DCONST_0:
	    case OP_DCONST_1:
	    case OP_DLOAD_0:
	    case OP_DLOAD_1:
	    case OP_DLOAD_2:
	    case OP_DLOAD_3:
	    case OP_DUP2:
	    case OP_DUP2_X1:
	    case OP_DUP2_X2:
	    case OP_DUP_X1:
	    case OP_DUP_X2:
	    case OP_F2D:
	    case OP_F2I:
	    case OP_F2L:
	    case OP_FALOAD:
	    case OP_FASTORE:
	    case OP_FCMPG:
	    case OP_FCMPL:
	    case OP_FCONST_0:
	    case OP_FCONST_1:
	    case OP_FCONST_2:
	    case OP_FLOAD_0:
	    case OP_FLOAD_1:
	    case OP_FLOAD_2:
	    case OP_FLOAD_3:
	    case OP_I2B:
	    case OP_I2C:
	    case OP_I2D:
	    case OP_I2F:
	    case OP_I2L:
	    case OP_I2S:
	    case OP_IALOAD:
	    case OP_IASTORE:
	    case OP_ICONST_0:
	    case OP_ICONST_1:
	    case OP_ICONST_2:
	    case OP_ICONST_3:
	    case OP_ICONST_4:
	    case OP_ICONST_5:
	    case OP_ICONST_M1:
	    case OP_ILOAD_0:
	    case OP_ILOAD_1:
	    case OP_ILOAD_2:
	    case OP_ILOAD_3:
	    case OP_L2D:
	    case OP_L2F:
	    case OP_L2I:
	    case OP_LALOAD:
	    case OP_LASTORE:
	    case OP_LCMP:
	    case OP_LCONST_0:
	    case OP_LCONST_1:
	    case OP_LLOAD_0:
	    case OP_LLOAD_1:
	    case OP_LLOAD_2:
	    case OP_LLOAD_3:
	    case OP_MONITORENTER:
	    case OP_MONITOREXIT:
	    case OP_NOP:
	    case OP_POP2:
	    case OP_SALOAD:
	    case OP_SASTORE:
	    case OP_SWAP:
	    case OP_DADD:
	    case OP_FADD:
	    case OP_IADD:
	    case OP_LADD:
	    case OP_DDIV:
	    case OP_FDIV:
	    case OP_IDIV:
	    case OP_LDIV:
	    case OP_DMUL:
	    case OP_FMUL:
	    case OP_IMUL:
	    case OP_LMUL:
	    case OP_DREM:
	    case OP_FREM:
	    case OP_IREM:
	    case OP_LREM:
	    case OP_DSUB:
	    case OP_FSUB:
	    case OP_ISUB:
	    case OP_LSUB:
	    case OP_IAND:
	    case OP_IOR:
	    case OP_IXOR:
	    case OP_ISHL:
	    case OP_ISHR:
	    case OP_IUSHR:
	    case OP_LAND:
	    case OP_LOR:
	    case OP_LXOR:
	    case OP_LSHL:
	    case OP_LSHR:
	    case OP_LUSHR:
	    case OP_DNEG:
	    case OP_DSTORE_0:
	    case OP_DSTORE_1:
	    case OP_DSTORE_2:
	    case OP_DSTORE_3:
	    case OP_DRETURN:
	    case OP_FNEG:
	    case OP_FSTORE_0:
	    case OP_FSTORE_1:
	    case OP_FSTORE_2:
	    case OP_FSTORE_3:
	    case OP_FRETURN:
	    case OP_INEG:
	    case OP_ISTORE_0:
	    case OP_ISTORE_1:
	    case OP_ISTORE_2:
	    case OP_ISTORE_3:
	    case OP_IRETURN:
	    case OP_LNEG:
	    case OP_LSTORE_0:
	    case OP_LSTORE_1:
	    case OP_LSTORE_2:
	    case OP_LSTORE_3:
	    case OP_LRETURN:
	    case OP_DUP:
	    case OP_POP:
	    case OP_RETURN:
	    case OP_ADD:
	    case OP_SUB:
	    case OP_MUL:
	    case OP_DIV:
	    case OP_REM:
	    case OP_AND:
	    case OP_OR:
	    case OP_XOR:
	    case OP_ARRAYLOAD:
	    case OP_ARRAYSTORE:
	    case OP_EXCH:
	    case OP_NEG:
	    case OP_SHL:
	    case OP_SHR:
	    case OP_USHR:
	    case OP_CAST2D:
	    case OP_CAST2F:
	    case OP_CAST2I:
	    case OP_CAST2L:
	    case OP_DUPX:
		if (matcher.find() == false)
		    recordError("instruction doesn't take any arguments", lines[index], linenumber, original_line);
		break;

	    case OP_LOOKUPSWITCH:
	    case OP_TABLESWITCH:
	    case OP_SWITCH:
		//
		// Probably don't want to handle these here
		//
		break;

	    case OP_ALOAD:
	    case OP_DLOAD:
	    case OP_FLOAD:
	    case OP_ILOAD:
	    case OP_LLOAD:
	    case OP_RET:
	    case OP_ASTORE:
	    case OP_DSTORE:
	    case OP_FSTORE:
	    case OP_ISTORE:
	    case OP_LSTORE:
	    case OP_BIPUSH:
	    case OP_NEWARRAY:
	    case OP_LDC:
	    case OP_ANEWARRAY:
	    case OP_CHECKCAST:
	    case OP_INSTANCEOF:
	    case OP_NEW:
	    case OP_GETFIELD:
	    case OP_GETSTATIC:
	    case OP_PUTFIELD:
	    case OP_PUTSTATIC:
	    case OP_INVOKESPECIAL:
	    case OP_INVOKESTATIC:
	    case OP_INVOKEVIRTUAL:
	    case OP_LDC2_W:
	    case OP_LDC_W:
	    case OP_IFEQ:
	    case OP_IFGE:
	    case OP_IFGT:
	    case OP_IFLE:
	    case OP_IFLT:
	    case OP_IFNE:
	    case OP_IF_ICMPEQ:
	    case OP_IF_ICMPGE:
	    case OP_IF_ICMPGT:
	    case OP_IF_ICMPLE:
	    case OP_IF_ICMPLT:
	    case OP_IF_ICMPNE:
	    case OP_IFNONNULL:
	    case OP_IFNULL:
	    case OP_IF_ACMPEQ:
	    case OP_IF_ACMPNE:
	    case OP_GOTO:
	    case OP_JSR:
	    case OP_SIPUSH:
	    case OP_GOTO_W:
	    case OP_JSR_W:
	    case OP_INVOKE:
	    case OP_PUSH:
	    case OP_STORE:
		if (matcher.find())
		    recordError("instruction requires an argument", original_line, linenumber);
		else recordError("invalid argument", lines[index], linenumber, original_line);
		break;

	    case OP_IINC:
	    case OP_MULTIANEWARRAY:
	    case OP_INVOKEINTERFACE:
		if (matcher.find())
		    recordError("instruction requires two arguments", original_line, linenumber);
		else recordError("invalid argument", lines[index], linenumber, original_line);
		break;

	    case OP_WIDE:
		if (matcher.find())
		    recordError("instruction requires two or three arguments", original_line, linenumber);
		else recordError("invalid argument", lines[index], linenumber, original_line);
		break;

	    case OP_CALL:
		if (matcher.find())
		    recordError("instruction requires at least one argument", original_line, linenumber);
		else recordError("invalid argument", lines[index], linenumber, original_line);
		break;

	    default:
		//
		// Looks like we forgot this opcode - handle it here with a
		// general message.
		//
		recordError("instruction syntax error", lines[index], linenumber, original_line);
		break;
	}
    }


    synchronized void
    recordInternalError(String message)

	throws JVMAssemblerError

    {

	throw(new JVMAssemblerError("Internal Error: " + message, getSortedErrors()));
    }


    public void
    setCodeModel(int model) {

	if (model >= MIN_CODE_MODEL && model <= MAX_CODE_MODEL)
	    codemodel = model;
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    addCastTo(int target) {

	//
	// If necessary this method adds instructions to the code array that
	// casts the object on top of the operand stack to the type specified
	// by target.
	//

	switch (typestack.peekAtDescriptorCode(0)) {
	    case DESCRIPTOR_DOUBLE:
		switch (target) {
		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_SHORT:
			addCode(OP_D2I);
			break;

		    case DESCRIPTOR_FLOAT:
			addCode(OP_D2F);
			break;

		    case DESCRIPTOR_LONG:
			addCode(OP_D2L);
			break;
		}
		break;

	    case DESCRIPTOR_INT:
	    case DESCRIPTOR_BOOLEAN:
	    case DESCRIPTOR_BYTE:
	    case DESCRIPTOR_CHAR:
	    case DESCRIPTOR_SHORT:
		switch (target) {
		    case DESCRIPTOR_DOUBLE:
			addCode(OP_I2D);
			break;

		    case DESCRIPTOR_FLOAT:
			addCode(OP_I2F);
			break;

		    case DESCRIPTOR_LONG:
			addCode(OP_I2L);
			break;
		}
		break;

	    case DESCRIPTOR_FLOAT:
		switch (target) {
		    case DESCRIPTOR_DOUBLE:
			addCode(OP_F2D);
			break;

		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_SHORT:
			addCode(OP_F2I);
			break;

		    case DESCRIPTOR_LONG:
			addCode(OP_F2L);
			break;
		}
		break;

	    case DESCRIPTOR_LONG:
		switch (target) {
		    case DESCRIPTOR_DOUBLE:
			addCode(OP_L2D);
			break;

		    case DESCRIPTOR_FLOAT:
			addCode(OP_L2F);
			break;

		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_SHORT:
			addCode(OP_L2I);
			break;
		}
		break;
	}
    }


    private void
    addCode(int opcode) {

	//
	// This must only be used by opcodes that don't take arguments and
	// consequently don't mind a null args array.
	//

	addCode(opcode, null);
    }


    private void
    addCode(int opcode, int args[]) {

	//
	// Every instruction added to bytecode[] should use this method, which
	// means typestack changes can be centralized here.
	//

	switch (opcode) {
	    case OP_AALOAD:
	    case OP_AASTORE:
	    case OP_ACONST_NULL:
	    case OP_ALOAD_0:
	    case OP_ALOAD_1:
	    case OP_ALOAD_2:
	    case OP_ALOAD_3:
	    case OP_ARETURN:
	    case OP_ARRAYLENGTH:
	    case OP_ASTORE_0:
	    case OP_ASTORE_1:
	    case OP_ASTORE_2:
	    case OP_ASTORE_3:
	    case OP_ATHROW:
	    case OP_BALOAD:
	    case OP_BASTORE:
	    case OP_CALOAD:
	    case OP_CASTORE:
	    case OP_D2F:
	    case OP_D2I:
	    case OP_D2L:
	    case OP_DADD:
	    case OP_DALOAD:
	    case OP_DASTORE:
	    case OP_DCMPG:
	    case OP_DCMPL:
	    case OP_DCONST_0:
	    case OP_DCONST_1:
	    case OP_DDIV:
	    case OP_DLOAD_0:
	    case OP_DLOAD_1:
	    case OP_DLOAD_2:
	    case OP_DLOAD_3:
	    case OP_DMUL:
	    case OP_DNEG:
	    case OP_DREM:
	    case OP_DRETURN:
	    case OP_DSTORE_0:
	    case OP_DSTORE_1:
	    case OP_DSTORE_2:
	    case OP_DSTORE_3:
	    case OP_DSUB:
	    case OP_DUP:
	    case OP_DUP2:
	    case OP_DUP2_X1:
	    case OP_DUP2_X2:
	    case OP_DUP_X1:
	    case OP_DUP_X2:
	    case OP_F2D:
	    case OP_F2I:
	    case OP_F2L:
	    case OP_FADD:
	    case OP_FALOAD:
	    case OP_FASTORE:
	    case OP_FCMPG:
	    case OP_FCMPL:
	    case OP_FCONST_0:
	    case OP_FCONST_1:
	    case OP_FCONST_2:
	    case OP_FDIV:
	    case OP_FLOAD_0:
	    case OP_FLOAD_1:
	    case OP_FLOAD_2:
	    case OP_FLOAD_3:
	    case OP_FMUL:
	    case OP_FNEG:
	    case OP_FREM:
	    case OP_FRETURN:
	    case OP_FSTORE_0:
	    case OP_FSTORE_1:
	    case OP_FSTORE_2:
	    case OP_FSTORE_3:
	    case OP_FSUB:
	    case OP_I2B:
	    case OP_I2C:
	    case OP_I2D:
	    case OP_I2F:
	    case OP_I2L:
	    case OP_I2S:
	    case OP_IADD:
	    case OP_IALOAD:
	    case OP_IAND:
	    case OP_IASTORE:
	    case OP_ICONST_0:
	    case OP_ICONST_1:
	    case OP_ICONST_2:
	    case OP_ICONST_3:
	    case OP_ICONST_4:
	    case OP_ICONST_5:
	    case OP_ICONST_M1:
	    case OP_IDIV:
	    case OP_ILOAD_0:
	    case OP_ILOAD_1:
	    case OP_ILOAD_2:
	    case OP_ILOAD_3:
	    case OP_IMUL:
	    case OP_INEG:
	    case OP_IOR:
	    case OP_IREM:
	    case OP_IRETURN:
	    case OP_ISHL:
	    case OP_ISHR:
	    case OP_ISTORE_0:
	    case OP_ISTORE_1:
	    case OP_ISTORE_2:
	    case OP_ISTORE_3:
	    case OP_ISUB:
	    case OP_IUSHR:
	    case OP_IXOR:
	    case OP_L2D:
	    case OP_L2F:
	    case OP_L2I:
	    case OP_LADD:
	    case OP_LALOAD:
	    case OP_LAND:
	    case OP_LASTORE:
	    case OP_LCMP:
	    case OP_LCONST_0:
	    case OP_LCONST_1:
	    case OP_LDIV:
	    case OP_LLOAD_0:
	    case OP_LLOAD_1:
	    case OP_LLOAD_2:
	    case OP_LLOAD_3:
	    case OP_LMUL:
	    case OP_LNEG:
	    case OP_LOR:
	    case OP_LREM:
	    case OP_LRETURN:
	    case OP_LSHL:
	    case OP_LSHR:
	    case OP_LSTORE_0:
	    case OP_LSTORE_1:
	    case OP_LSTORE_2:
	    case OP_LSTORE_3:
	    case OP_LSUB:
	    case OP_LUSHR:
	    case OP_LXOR:
	    case OP_MONITORENTER:
	    case OP_MONITOREXIT:
	    case OP_NOP:
	    case OP_POP:
	    case OP_POP2:
	    case OP_RETURN:
	    case OP_SALOAD:
	    case OP_SASTORE:
	    case OP_SWAP:
		ensureCodeCapacity(1);
		bytecode[nextbyte++] = (byte)opcode;
		break;

	    case OP_ALOAD:
	    case OP_ASTORE:
	    case OP_DLOAD:
	    case OP_DSTORE:
	    case OP_FLOAD:
	    case OP_FSTORE:
	    case OP_ILOAD:
	    case OP_ISTORE:
	    case OP_LLOAD:
	    case OP_LSTORE:
	    case OP_RET:
	    case OP_BIPUSH:
	    case OP_NEWARRAY:
	    case OP_LDC:
		ensureCodeCapacity(2);
		bytecode[nextbyte++] = (byte)opcode;
		bytecode[nextbyte++] = (byte)args[0];
		break;

	    case OP_ANEWARRAY:
	    case OP_CHECKCAST:
	    case OP_INSTANCEOF:
	    case OP_NEW:
	    case OP_GETFIELD:
	    case OP_GETSTATIC:
	    case OP_PUTFIELD:
	    case OP_PUTSTATIC:
	    case OP_INVOKESPECIAL:
	    case OP_INVOKESTATIC:
	    case OP_INVOKEVIRTUAL:
	    case OP_LDC_W:
	    case OP_LDC2_W:
	    case OP_IFEQ:
	    case OP_IFGE:
	    case OP_IFGT:
	    case OP_IFLE:
	    case OP_IFLT:
	    case OP_IFNE:
	    case OP_IFNONNULL:
	    case OP_IFNULL:
	    case OP_IF_ACMPEQ:
	    case OP_IF_ACMPNE:
	    case OP_IF_ICMPEQ:
	    case OP_IF_ICMPGE:
	    case OP_IF_ICMPGT:
	    case OP_IF_ICMPLE:
	    case OP_IF_ICMPLT:
	    case OP_IF_ICMPNE:
	    case OP_GOTO:
	    case OP_JSR:
	    case OP_SIPUSH:
		ensureCodeCapacity(3);
		bytecode[nextbyte++] = (byte)opcode;
		bytecode[nextbyte++] = (byte)(args[0] >> 8);
		bytecode[nextbyte++] = (byte)args[0];
		break;

	    case OP_IINC:
		ensureCodeCapacity(3);
		bytecode[nextbyte++] = (byte)opcode;
		bytecode[nextbyte++] = (byte)args[0];
		bytecode[nextbyte++] = (byte)args[1];
		break;

	    case OP_MULTIANEWARRAY:
		ensureCodeCapacity(4);
		bytecode[nextbyte++] = (byte)opcode;
		bytecode[nextbyte++] = (byte)(args[0] >> 8);
		bytecode[nextbyte++] = (byte)args[0];
		bytecode[nextbyte++] = (byte)args[1];
		break;

	    case OP_GOTO_W:
	    case OP_JSR_W:
		ensureCodeCapacity(5);
		bytecode[nextbyte++] = (byte)opcode;
		bytecode[nextbyte++] = (byte)(args[0] >> 24);
		bytecode[nextbyte++] = (byte)(args[0] >> 16);
		bytecode[nextbyte++] = (byte)(args[0] >> 8);
		bytecode[nextbyte++] = (byte)args[0];
		break;

	    case OP_INVOKEINTERFACE:
		ensureCodeCapacity(5);
		bytecode[nextbyte++] = (byte)opcode;
		bytecode[nextbyte++] = (byte)(args[0] >> 8);
		bytecode[nextbyte++] = (byte)args[0];
		bytecode[nextbyte++] = (byte)args[1];
		bytecode[nextbyte++] = 0;
		break;

	    case OP_LOOKUPSWITCH:
	    case OP_TABLESWITCH:
		//
		// Padding and the rest of the instruction must be added later,
		// since at this point we're probably only assembling a single
		// instruction. The alternative would be to use OP_NOP opcodes
		// to handle alignment, and in that case we could add the rest
		// of the instruction.
		//
		ensureCodeCapacity(1);
		bytecode[nextbyte++] = (byte)opcode;
		break;

	    case OP_WIDE:
		if (args[0] == OP_IINC) {
		    ensureCodeCapacity(6);
		    bytecode[nextbyte++] = (byte)opcode;
		    bytecode[nextbyte++] = (byte)args[0];
		    bytecode[nextbyte++] = (byte)(args[1] >> 8);
		    bytecode[nextbyte++] = (byte)args[1];
		    bytecode[nextbyte++] = (byte)(args[2] >> 8);
		    bytecode[nextbyte++] = (byte)args[2];
		} else {
		    ensureCodeCapacity(4);
		    bytecode[nextbyte++] = (byte)opcode;
		    bytecode[nextbyte++] = (byte)args[0];
		    bytecode[nextbyte++] = (byte)(args[1] >> 8);
		    bytecode[nextbyte++] = (byte)args[1];
		}
		break;
	}

	typestack.execute(opcode, args);
    }


    private void
    addDupExch() {

	switch (typestack.peekAtCategory(1)) {
	    case 1:
		switch (typestack.peekAtCategory(0)) {
		    case 1:
			addCode(OP_DUP_X1);
			break;

		    case 2:
			addCode(OP_DUP2_X1);
			break;
		}
		break;

	    case 2:
		switch (typestack.peekAtCategory(0)) {
		    case 1:
			addCode(OP_DUP_X2);
			break;

		    case 2:
			addCode(OP_DUP2_X2);
			break;
		}
		break;
	}
    }


    private void
    addExch() {

	switch (typestack.peekAtCategory(1)) {
	    case 1:
		switch (typestack.peekAtCategory(0)) {
		    case 1:
			addCode(OP_SWAP);
			break;

		    case 2:
			addCode(OP_DUP2_X1);
			addCode(OP_POP2);
			break;
		}
		break;

	    case 2:
		switch (typestack.peekAtCategory(0)) {
		    case 1:
			addCode(OP_DUP_X2);
			addCode(OP_POP);
			break;

		    case 2:
			addCode(OP_DUP2_X2);
			addCode(OP_POP2);
			break;
		}
		break;
	}
    }


    private void
    addExchCastTo(int target, boolean commutative) {

	//
	// If necessary this method adds instructions to the code array that
	// we're building that casts the second object on the operand stack to
	// the type specified by target. If commutative is false it also adds
	// the intructions needed to move the cast object back to its original
	// position on the operand stack.
	//

	switch (target) {
	    case DESCRIPTOR_DOUBLE:
		switch (typestack.peekAtDescriptorCode(1)) {
		    case DESCRIPTOR_FLOAT:
			addExch();
			addCode(OP_F2D);
			if (commutative == false)
			    addExch();
			break;

		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_SHORT:
			addExch();
			addCode(OP_I2D);
			if (commutative == false)
			    addExch();
			break;

		    case DESCRIPTOR_LONG:
			addExch();
			addCode(OP_L2D);
			if (commutative == false)
			    addExch();
			break;
		}
		break;

	    case DESCRIPTOR_INT:
	    case DESCRIPTOR_BOOLEAN:
	    case DESCRIPTOR_BYTE:
	    case DESCRIPTOR_CHAR:
	    case DESCRIPTOR_SHORT:
		switch (typestack.peekAtDescriptorCode(1)) {
		    case DESCRIPTOR_DOUBLE:
			addExch();
			addCode(OP_D2I);
			if (commutative == false)
			    addExch();
			break;

		    case DESCRIPTOR_FLOAT:
			addExch();
			addCode(OP_F2I);
			if (commutative == false)
			    addExch();
			break;

		    case DESCRIPTOR_LONG:
			addExch();
			addCode(OP_L2I);
			if (commutative == false)
			    addExch();
			break;
		}
		break;

	    case DESCRIPTOR_FLOAT:
		switch (typestack.peekAtDescriptorCode(1)) {
		    case DESCRIPTOR_DOUBLE:
			addExch();
			addCode(OP_D2F);
			if (commutative == false)
			    addExch();
			break;

		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_SHORT:
			addExch();
			addCode(OP_I2F);
			if (commutative == false)
			    addExch();
			break;

		    case DESCRIPTOR_LONG:
			addExch();
			addCode(OP_L2F);
			if (commutative == false)
			    addExch();
			break;
		}
		break;

	    case DESCRIPTOR_LONG:
		switch (typestack.peekAtDescriptorCode(1)) {
		    case DESCRIPTOR_DOUBLE:
			addExch();
			addCode(OP_D2L);
			if (commutative == false)
			    addExch();
			break;

		    case DESCRIPTOR_FLOAT:
			addExch();
			addCode(OP_F2L);
			if (commutative == false)
			    addExch();
			break;

		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_SHORT:
			addExch();
			addCode(OP_I2L);
			if (commutative == false)
			    addExch();
			break;
		}
		break;
	}
    }


    private void
    addInstruction(String line, int linenumber, int opcode, int model) {

	ensureInstructionsCapacity(1);
	instructions[nextinstruction++] = new JVMInstruction(line, linenumber, opcode, model);
    }


    private void
    addInstruction(String line, int linenumber, int opcode, int model, int arg) {

	ensureInstructionsCapacity(1);
	instructions[nextinstruction++] = new JVMInstruction(line, linenumber, opcode, model, new int[] {arg});
    }


    private void
    addInstruction(String line, int linenumber, int opcode, int model, Object extra) {

	ensureInstructionsCapacity(1);
	instructions[nextinstruction++] = new JVMInstruction(line, linenumber, opcode, model, null, extra);
    }


    private void
    addInstruction(String line, int linenumber, int opcode, int model, int arg, Object extra) {

	ensureInstructionsCapacity(1);
	instructions[nextinstruction++] = new JVMInstruction(line, linenumber, opcode, model, new int[] {arg}, extra);
    }


    private void
    addInstruction(String line, int linenumber, int opcode, int model, int arg1, int arg2) {

	ensureInstructionsCapacity(1);
	instructions[nextinstruction++] = new JVMInstruction(line, linenumber, opcode, model, new int[] {arg1, arg2});
    }


    private void
    addInstruction(String line, int linenumber, int opcode, int model, int arg1, int arg2, int arg3) {

	ensureInstructionsCapacity(1);
	instructions[nextinstruction++] = new JVMInstruction(line, linenumber, opcode, model, new int[] {arg1, arg2, arg3});
    }


    private boolean
    addReturn(JVMInstruction instruction) {

	int  target;

	switch (target = method.getDescriptorCodeForReturnValue()) {
	    case DESCRIPTOR_ARRAY:
	    case DESCRIPTOR_CLASS:
		//
		// Recent change pushes null if object on top of the stack
		// doesn't match the methods return type is a recent change
		// (5/12/09) that was added quickly, mostly for a compiler
		// but it's probably OK.
		//
		instruction.setReturnOffset(nextbyte);
		if (method.getDescriptorForReturnValue().equals(typestack.peekAtDescriptor(0)) == false)
		    addCode(OP_ACONST_NULL);
		addCode(OP_ARETURN);
		break;

	    case DESCRIPTOR_BOOLEAN:
	    case DESCRIPTOR_BYTE:
	    case DESCRIPTOR_CHAR:
	    case DESCRIPTOR_DOUBLE:
	    case DESCRIPTOR_FLOAT:
	    case DESCRIPTOR_INT:
	    case DESCRIPTOR_LONG:
	    case DESCRIPTOR_SHORT:
		switch (typestack.peekAtDescriptorCode(0)) {
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_DOUBLE:
		    case DESCRIPTOR_FLOAT:
		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_LONG:
		    case DESCRIPTOR_SHORT:
			instruction.setReturnOffset(nextbyte);
			addCastTo(target);
			addCode(replaceOpcode(OP_RETURN, null));
			break;
		}
		break;

	    case DESCRIPTOR_VOID:
		instruction.setReturnOffset(nextbyte);
		addCode(OP_RETURN);
		break;

	    default:
		instruction.setReturnOffset(-1);
		break;
	}

	return(instruction.getReturnOffset() >= 0);
    }


    private boolean
    assembleInstruction(JVMInstruction instruction)

	throws JVMAssemblerError

    {

	boolean  returned;
	Object   call[];
	int      args[];
	int      model;
	int      opcode;
	int      errors;
	int      n;

	//
	// Initializes the typestack, generates the instruction's bytecode,
	// and then stores that bytecode, max_stack, and the final typestack
	// image back in the instruction (provided there weren't any errors).
	//
	// NOTE - selection of replacement opcodes for OP_GOTO and OP_JSR is
	// now handled after all instructions are assembled. That way we can
	// get a pretty accurate (padding for switch opcodes causes errors)
	// look at the required branch offset.
	//

	nextbyte = 0;
	errors = errorcount;
	returned = false;
	model = instruction.getCodeModel();
	args = instruction.getArgs();
	opcode = instruction.getOpcode();

	typestack.setStack(instruction.getStartStack());

	switch (opcode) {
	    case OP_AALOAD:
	    case OP_AASTORE:
	    case OP_ACONST_NULL:
	    case OP_ALOAD_0:
	    case OP_ALOAD_1:
	    case OP_ALOAD_2:
	    case OP_ALOAD_3:
	    case OP_ARRAYLENGTH:
	    case OP_ASTORE_0:
	    case OP_ASTORE_1:
	    case OP_ASTORE_2:
	    case OP_ASTORE_3:
	    case OP_BALOAD:
	    case OP_BASTORE:
	    case OP_CALOAD:
	    case OP_CASTORE:
	    case OP_D2F:
	    case OP_D2I:
	    case OP_D2L:
	    case OP_DALOAD:
	    case OP_DASTORE:
	    case OP_DCONST_0:
	    case OP_DCONST_1:
	    case OP_DLOAD_0:
	    case OP_DLOAD_1:
	    case OP_DLOAD_2:
	    case OP_DLOAD_3:
	    case OP_DUP2:
	    case OP_DUP2_X1:
	    case OP_DUP2_X2:
	    case OP_DUP_X1:
	    case OP_DUP_X2:
	    case OP_F2D:
	    case OP_F2I:
	    case OP_F2L:
	    case OP_FALOAD:
	    case OP_FASTORE:
	    case OP_FCONST_0:
	    case OP_FCONST_1:
	    case OP_FCONST_2:
	    case OP_FLOAD_0:
	    case OP_FLOAD_1:
	    case OP_FLOAD_2:
	    case OP_FLOAD_3:
	    case OP_I2B:
	    case OP_I2C:
	    case OP_I2D:
	    case OP_I2F:
	    case OP_I2L:
	    case OP_I2S:
	    case OP_IALOAD:
	    case OP_IASTORE:
	    case OP_ICONST_0:
	    case OP_ICONST_1:
	    case OP_ICONST_2:
	    case OP_ICONST_3:
	    case OP_ICONST_4:
	    case OP_ICONST_5:
	    case OP_ICONST_M1:
	    case OP_ILOAD_0:
	    case OP_ILOAD_1:
	    case OP_ILOAD_2:
	    case OP_ILOAD_3:
	    case OP_L2D:
	    case OP_L2F:
	    case OP_L2I:
	    case OP_LALOAD:
	    case OP_LASTORE:
	    case OP_LCONST_0:
	    case OP_LCONST_1:
	    case OP_LLOAD_0:
	    case OP_LLOAD_1:
	    case OP_LLOAD_2:
	    case OP_LLOAD_3:
	    case OP_MONITORENTER:
	    case OP_MONITOREXIT:
	    case OP_NOP:
	    case OP_POP2:
	    case OP_SALOAD:
	    case OP_SASTORE:
	    case OP_SWAP:
	    case OP_BIPUSH:
	    case OP_NEWARRAY:
	    case OP_ANEWARRAY:
	    case OP_CHECKCAST:
	    case OP_INSTANCEOF:
	    case OP_NEW:
	    case OP_GETFIELD:
	    case OP_GETSTATIC:
	    case OP_INVOKESPECIAL:
	    case OP_INVOKESTATIC:
	    case OP_INVOKEVIRTUAL:
	    case OP_LDC2_W:
	    case OP_LDC_W:
	    case OP_IFNONNULL:
	    case OP_IFNULL:
	    case OP_IF_ACMPEQ:
	    case OP_IF_ACMPNE:
	    case OP_SIPUSH:
	    case OP_MULTIANEWARRAY:
	    case OP_GOTO:
	    case OP_JSR:
	    case OP_GOTO_W:
	    case OP_JSR_W:
	    case OP_INVOKEINTERFACE:
	    case OP_WIDE:
		addCode(opcode, args);
		break;

	    case OP_ARETURN:
	    case OP_ATHROW:
	    case OP_RET:
		returned = true;
		addCode(opcode, args);
		break;

	    case OP_DADD:
	    case OP_FADD:
	    case OP_IADD:
	    case OP_LADD:
	    case OP_DDIV:
	    case OP_FDIV:
	    case OP_IDIV:
	    case OP_LDIV:
	    case OP_DMUL:
	    case OP_FMUL:
	    case OP_IMUL:
	    case OP_LMUL:
	    case OP_DREM:
	    case OP_FREM:
	    case OP_IREM:
	    case OP_LREM:
	    case OP_DSUB:
	    case OP_FSUB:
	    case OP_ISUB:
	    case OP_LSUB:
	    case OP_IAND:
	    case OP_IOR:
	    case OP_IXOR:
	    case OP_ISHL:
	    case OP_ISHR:
	    case OP_IUSHR:
	    case OP_LAND:
	    case OP_LOR:
	    case OP_LXOR:
	    case OP_LSHL:
	    case OP_LSHR:
	    case OP_LUSHR:
	    case OP_DNEG:
	    case OP_DSTORE_0:
	    case OP_DSTORE_1:
	    case OP_DSTORE_2:
	    case OP_DSTORE_3:
	    case OP_FNEG:
	    case OP_FSTORE_0:
	    case OP_FSTORE_1:
	    case OP_FSTORE_2:
	    case OP_FSTORE_3:
	    case OP_INEG:
	    case OP_ISTORE_0:
	    case OP_ISTORE_1:
	    case OP_ISTORE_2:
	    case OP_ISTORE_3:
	    case OP_LNEG:
	    case OP_LSTORE_0:
	    case OP_LSTORE_1:
	    case OP_LSTORE_2:
	    case OP_LSTORE_3:
	    case OP_PUTFIELD:
	    case OP_PUTSTATIC:
	    case OP_DCMPG:
	    case OP_DCMPL:
	    case OP_FCMPG:
	    case OP_FCMPL:
	    case OP_LCMP:
		if (model != STRICT_CODE_MODEL)
		    castOperands(opcode, args);
		addCode(opcode, args);
		break;

	    case OP_DRETURN:
	    case OP_FRETURN:
	    case OP_IRETURN:
	    case OP_LRETURN:
		returned = true;
		if (model != STRICT_CODE_MODEL)
		    castOperands(opcode, args);
		addCode(opcode, args);
		break;

	    case OP_IFEQ:
	    case OP_IFGE:
	    case OP_IFGT:
	    case OP_IFLE:
	    case OP_IFLT:
	    case OP_IFNE:
	    case OP_IF_ICMPEQ:
	    case OP_IF_ICMPGE:
	    case OP_IF_ICMPGT:
	    case OP_IF_ICMPLE:
	    case OP_IF_ICMPLT:
	    case OP_IF_ICMPNE:
	    case OP_LOOKUPSWITCH:
	    case OP_TABLESWITCH:
		if (model != STRICT_CODE_MODEL)
		    castOperands(opcode, args);
		instruction.setOffset(nextbyte);
		addCode(opcode, args);
		break;

	    case OP_DUP:
	    case OP_POP:
	    case OP_ALOAD:
	    case OP_DLOAD:
	    case OP_FLOAD:
	    case OP_ILOAD:
	    case OP_LLOAD:
	    case OP_LDC:
	    case OP_IINC:
		if (model != STRICT_CODE_MODEL)
		    opcode = replaceOpcode(opcode, args);
		addCode(opcode, args);
		break;

	    case OP_ASTORE:
	    case OP_DSTORE:
	    case OP_FSTORE:
	    case OP_ISTORE:
	    case OP_LSTORE:
		if (model != STRICT_CODE_MODEL) {
		    castOperands(opcode, args);
		    opcode = replaceOpcode(opcode, args);
		}
		addCode(opcode, args);
		break;

	    case OP_RETURN:
		returned = true;
		if (model != STRICT_CODE_MODEL) {
		    castOperands(opcode, args);
		    opcode = replaceOpcode(opcode, args);
		}
		addCode(opcode, args);
		break;

	    //
	    // Opcodes handled by the default case are "virtual instructions"
	    // that have to be translated into real JVM opcodes (and arguments)
	    // before being added to the bytecode that we're generating.
	    //

	    default:
		if (model != STRICT_CODE_MODEL) {
		    switch (opcode) {
			case OP_ADD:
			case OP_SUB:
			case OP_MUL:
			case OP_DIV:
			case OP_REM:
			case OP_AND:
			case OP_OR:
			case OP_XOR:
			case OP_ARRAYLOAD:
			case OP_ARRAYSTORE:
			case OP_SHL:
			case OP_SHR:
			case OP_USHR:
			case OP_STORE:
			case OP_PUT:
			    castOperands(opcode, args);
			    addCode(replaceOpcode(opcode, args), args);
			    break;

			case OP_CAST2D:
			case OP_CAST2F:
			case OP_CAST2I:
			case OP_CAST2L:
			    castOperands(opcode, args);
			    break;

			case OP_DUPX:
			    addDupExch();
			    break;

			case OP_EXCH:
			    addExch();
			    break;

			case OP_INVOKE:
			case OP_NEG:
			case OP_LOAD:
			case OP_GET:
			    addCode(replaceOpcode(opcode, args), args);
			    break;

			case OP_ICONST:
			case OP_LCONST:
			case OP_FCONST:
			case OP_DCONST:
			    addCode(replaceOpcode(opcode, args, (Number)instruction.getExtra()), args);
			    break;

			case OP_SWITCH:
			    castOperands(opcode, args);
			    instruction.setOffset(nextbyte);
			    opcode = replaceOpcode(opcode, null, instruction.getExtra());
			    instruction.setOpcode(opcode);
			    addCode(opcode);
			    break;

			case OP_CALL:
			    //
			    // This was implemented very quickly so there's lots of
			    // room for improvement and testing. Error handling, here
			    // and other OP_CALL cases, undoubtedly needs plenty of
			    // work. Anyway, don't waste time trying to figure this
			    // out - we undoubtedly will revisit the OP_CALL code in
			    // the very near future.
			    //
			    call = (Object[])instruction.getExtra();
			    args[0] = ((Integer)call[1]).intValue();
			    opcode = replaceOpcode(opcode, args);
			    switch (opcode) {
				case OP_INVOKEINTERFACE:
				    recordInternalError("invokeinterface (via call) has not been implemented properly");
				    break;

				case OP_INVOKESPECIAL:
				case OP_INVOKEVIRTUAL:
				    args[0] = classfile.storeConstantPoolFieldRef((String)call[2]);
				    addCode(replaceOpcode(OP_GET, args), args);
				    break;

				case OP_INVOKESTATIC:
				    break;
			    }
			    for (n = 3; n < call.length; n += 3) {
				if (call[n+1] instanceof Number)
				    args[0] = ((Number)call[n+1]).intValue();
				addCode(replaceOpcode(((Integer)call[n]).intValue(), args, call[n+1]), args);
				addCastTo(((Integer)call[n+2]).intValue());
			    }
			    args[0] = ((Integer)call[1]).intValue();
			    addCode(opcode, args);
			    break;

			default:
			    recordInternalError("code for assembling instruction \"" + OPCODE_MNEMONICS[opcode] + "\" is missing");
			    break;
		    }
		} else recordError("instruction is not allowed in strict mode", instruction);
		break;
	}

	if (instruction.isLeaf() && returned == false) {
	    if (model != STRICT_CODE_MODEL) {
		if (addReturn(instruction) == false)
		    recordError("can't add required return statement", instruction);
	    } else recordError("required return statement is missing", instruction);
	}

	if (errorcount == errors) {
	    instruction.setByteCode(bytecode, nextbyte);
	    instruction.setStack(typestack.getStack());
	    instruction.setMaxStack(typestack.getMaxStack());
	} else {
	    instruction.setByteCode(null);
	    instruction.invalidate();
	}

	return(errorcount == errors);
    }


    private boolean
    assembleInstructionsReachableFrom(JVMInstruction instruction)

	throws JVMAssemblerError

    {

	Iterator  successors;
	boolean   result = true;

	//
	// Still need to deal with error checking - think about it some.
	//

	if (instruction != null) {
	    if (instruction.getVisited() == false) {
		instruction.setVisited(true);
		if (result = assembleInstruction(instruction)) {
		    if ((successors = instruction.getSuccessors()) != null) {
			while (successors.hasNext())
			    assembleInstructionsReachableFrom((JVMInstruction)successors.next());
		    }
		}
	    }
	}

	return(result);
    }


    private int[]
    buildExceptionTable() {

	ArrayList  data;
	String     exception;
	int        table[] = null;
	int        tmp[];
	int        length;
	int        next;
	int        start;
	int        end;
	int        m;
	int        n;

	if (exception_table.size() > 0) {
	    for (n = 0; n < exception_table.size(); n++) {
		if ((data = (ArrayList)exception_table.get(n)) != null) {
		    if ((length = 4*((data.size() - 2)/2)) > 0) {
			if (table != null) {
			    next = table.length;
			    tmp = new int[next + length];
			    System.arraycopy(table, 0, tmp, 0, next);
			    table = tmp;
			} else {
			    table = new int[length];
			    next = 0;
			}
			start = instructions[((Integer)data.get(0)).intValue()].getExceptionHandlerStart();
			end = instructions[((Integer)data.get(1)).intValue()].getExceptionHandlerStart();
			for (m = 2; m < data.size(); m += 2) {
			    table[next++] = start;
			    table[next++] = end;
			    table[next++] = instructions[((Integer)data.get(m)).intValue()].getPC();
			    if ((exception = (String)data.get(m+1)) != null)
				table[next++] = classfile.storeConstantPoolClass(exception);
			    else table[next++] = 0;
			}
		    }
		}
	    }
	}

	return(table);
    }


    private void
    castOperands(int opcode, int args[])

	throws JVMAssemblerError

    {

	switch (opcode) {
	    case OP_DNEG:
	    case OP_DSTORE:
	    case OP_DSTORE_0:
	    case OP_DSTORE_1:
	    case OP_DSTORE_2:
	    case OP_DSTORE_3:
	    case OP_DRETURN:
	    case OP_DASTORE:
		addCastTo(DESCRIPTOR_DOUBLE);
		break;

	    case OP_FNEG:
	    case OP_FSTORE:
	    case OP_FSTORE_0:
	    case OP_FSTORE_1:
	    case OP_FSTORE_2:
	    case OP_FSTORE_3:
	    case OP_FRETURN:
	    case OP_FASTORE:
		addCastTo(DESCRIPTOR_FLOAT);
		break;

	    case OP_INEG:
	    case OP_ISTORE:
	    case OP_ISTORE_0:
	    case OP_ISTORE_1:
	    case OP_ISTORE_2:
	    case OP_ISTORE_3:
	    case OP_IRETURN:
	    case OP_BASTORE:
	    case OP_CASTORE:
	    case OP_IASTORE:
	    case OP_SASTORE:
		addCastTo(DESCRIPTOR_INT);
		break;

	    case OP_LNEG:
	    case OP_LSTORE:
	    case OP_LSTORE_0:
	    case OP_LSTORE_1:
	    case OP_LSTORE_2:
	    case OP_LSTORE_3:
	    case OP_LRETURN:
	    case OP_LASTORE:
		addCastTo(DESCRIPTOR_LONG);
		break;

	    case OP_PUTFIELD:
	    case OP_PUTSTATIC:
		addCastTo(classfile.getFieldDescriptorCode(args[0]));
		break;

	    case OP_IAND:
	    case OP_IOR:
	    case OP_IXOR:
		addCastTo(DESCRIPTOR_INT);
		addExchCastTo(DESCRIPTOR_INT, true);
		break;

	    case OP_ISHL:
	    case OP_ISHR:
	    case OP_IUSHR:
		addCastTo(DESCRIPTOR_INT);
		addExchCastTo(DESCRIPTOR_INT, false);
		break;

	    case OP_LSHL:
	    case OP_LSHR:
	    case OP_LUSHR:
		addCastTo(DESCRIPTOR_INT);
		addExchCastTo(DESCRIPTOR_LONG, false);
		break;

	    case OP_DADD:
	    case OP_DDIV:
	    case OP_DMUL:
	    case OP_DREM:
	    case OP_DSUB:
	    case OP_DCMPG:
	    case OP_DCMPL:
		addCastTo(DESCRIPTOR_DOUBLE);
		addExchCastTo(DESCRIPTOR_DOUBLE, IS_COMMUTATIVE[opcode]);
		break;

	    case OP_FADD:
	    case OP_FDIV:
	    case OP_FMUL:
	    case OP_FREM:
	    case OP_FSUB:
	    case OP_FCMPG:
	    case OP_FCMPL:
		addCastTo(DESCRIPTOR_FLOAT);
		addExchCastTo(DESCRIPTOR_FLOAT, IS_COMMUTATIVE[opcode]);
		break;

	    case OP_IADD:
	    case OP_IDIV:
	    case OP_IMUL:
	    case OP_IREM:
	    case OP_ISUB:
		addCastTo(DESCRIPTOR_INT);
		addExchCastTo(DESCRIPTOR_INT, IS_COMMUTATIVE[opcode]);
		break;

	    case OP_LADD:
	    case OP_LDIV:
	    case OP_LMUL:
	    case OP_LREM:
	    case OP_LSUB:
	    case OP_LCMP:
		addCastTo(DESCRIPTOR_LONG);
		addExchCastTo(DESCRIPTOR_LONG, IS_COMMUTATIVE[opcode]);
		break;

	    case OP_LOOKUPSWITCH:
	    case OP_TABLESWITCH:
		addCastTo(DESCRIPTOR_INT);
		break;

	    case OP_WIDE:
		if (args[0] == OP_IINC)
		    castOperands(args[0], new int[] {args[1], args[2]});
		else castOperands(args[0], new int[] {args[1]});
		break;

	    case OP_ADD:
	    case OP_DIV:
	    case OP_MUL:
	    case OP_REM:
	    case OP_SUB:
		switch (typestack.peekAtDescriptorCode(1)) {
		    case DESCRIPTOR_DOUBLE:
			addCastTo(DESCRIPTOR_DOUBLE);
			break;

		    case DESCRIPTOR_FLOAT:
			switch (typestack.peekAtDescriptorCode(0)) {
			    case DESCRIPTOR_DOUBLE:
				addExchCastTo(DESCRIPTOR_DOUBLE, IS_COMMUTATIVE[opcode]);
				break;

			    default:
				addCastTo(DESCRIPTOR_FLOAT);
				break;
			}
			break;

		    case DESCRIPTOR_LONG:
			switch (typestack.peekAtDescriptorCode(0)) {
			    case DESCRIPTOR_DOUBLE:
				addExchCastTo(DESCRIPTOR_DOUBLE, IS_COMMUTATIVE[opcode]);
				break;

			    case DESCRIPTOR_FLOAT:
				addExchCastTo(DESCRIPTOR_FLOAT, IS_COMMUTATIVE[opcode]);
				break;

			    default:
				addCastTo(DESCRIPTOR_LONG);
				break;
			}
			break;

		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_SHORT:
			addExchCastTo(typestack.peekAtDescriptorCode(0), IS_COMMUTATIVE[opcode]);
			break;
		}
		break;

	    case OP_AND:
	    case OP_OR:
	    case OP_XOR:
		switch (typestack.peekAtDescriptorCode(1)) {
		    case DESCRIPTOR_DOUBLE:
		    case DESCRIPTOR_FLOAT:
		    case DESCRIPTOR_LONG:
			addCastTo(DESCRIPTOR_LONG);
			addExchCastTo(DESCRIPTOR_LONG, IS_COMMUTATIVE[opcode]);
			break;

		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_SHORT:
			switch (typestack.peekAtDescriptorCode(0)) {
			    case DESCRIPTOR_DOUBLE:
			    case DESCRIPTOR_FLOAT:
			    case DESCRIPTOR_LONG:
				addCastTo(DESCRIPTOR_LONG);
				addExchCastTo(DESCRIPTOR_LONG, IS_COMMUTATIVE[opcode]);
				break;
			}
			break;
		}
		break;

	    case OP_CAST2D:
		addCastTo(DESCRIPTOR_DOUBLE);
		break;

	    case OP_CAST2F:
		addCastTo(DESCRIPTOR_FLOAT);
		break;

	    case OP_CAST2I:
		addCastTo(DESCRIPTOR_INT);
		break;

	    case OP_CAST2L:
		addCastTo(DESCRIPTOR_LONG);
		break;

	    case OP_IFEQ:
	    case OP_IFGE:
	    case OP_IFGT:
	    case OP_IFLE:
	    case OP_IFLT:
	    case OP_IFNE:
		addCastTo(DESCRIPTOR_INT);
		break;

	    case OP_IF_ICMPEQ:
	    case OP_IF_ICMPGE:
	    case OP_IF_ICMPGT:
	    case OP_IF_ICMPLE:
	    case OP_IF_ICMPLT:
	    case OP_IF_ICMPNE:
		addCastTo(DESCRIPTOR_INT);
		addExchCastTo(DESCRIPTOR_INT, false);
		break;

	    case OP_ARRAYLOAD:
		addCastTo(DESCRIPTOR_INT);
		break;

	    case OP_ARRAYSTORE:
		addCastTo(JVMDescriptor.getDescriptorCodeForArrayElement(typestack.peekAtDescriptor(2)));
		addExchCastTo(DESCRIPTOR_INT, false);
		break;

	    case OP_PUT:
		addCastTo(classfile.getFieldDescriptorCode(args[0]));
		break;

	    case OP_RETURN:
		addCastTo(method.getDescriptorCodeForReturnValue());
		break;

	    case OP_SHL:
	    case OP_SHR:
	    case OP_USHR:
		addCastTo(DESCRIPTOR_INT);
		switch (typestack.peekAtDescriptorCode(1)) {
		    case DESCRIPTOR_DOUBLE:
		    case DESCRIPTOR_FLOAT:
			addExchCastTo(DESCRIPTOR_LONG, false);
			break;
		}
		break;

	    case OP_STORE:
		addCastTo(method.getLocalVariableDescriptorCode(args[0]));
		break;

	    case OP_SWITCH:
		addCastTo(DESCRIPTOR_INT);
		break;
	}
    }


    private boolean
    classScanner(String lines[], int index, int linenumber, Pattern endpattern)

	throws JVMAssemblerError

    {

	JVMScanner  scanner;
	JVMMethod   method;
	JVMField    field;
	Matcher     matcher;
	String      line;
	String      classname;
	String      superclass;
	String      token;
	Number      number;
	int         errors;
	int         length;

	//
	// Reads the body of an "assembly language" class definition. Rather
	// ugly code (particularly if you look at JVMPatterns) that probably
	// could benefit from an formal JavaCC approach, which is something
	// that we eventually may investigate.
	//

	errors = errorcount;
	length = lines.length;
	scanner = new JVMScanner(this, classfile);

	for (index++, linenumber++; index < length; index++, linenumber++) {
	    if ((line = lines[index]) != null) {
		if (scanner.consumeLine(lines, index) == false) {
		    if (scanner.consumeToken(lines, index, endpattern) == null) {
			//
			// Right now extern definitions must be handled before
			// field and method declarations.
			//
			if (scanner.consumeToken(lines, index, PATTERN_EXTERN) != null) {
			    if (scanner.consumeExternalMethod(lines, index) == false) {
				if (scanner.consumeExternalConstructor(lines, index) == false) {
				    if (scanner.consumeExternalField(lines, index) == false)
					recordError("invalid extern statement", lines[index], linenumber, line);
				}
			    }
			} else if (scanner.consumeToken(lines, index, PATTERN_PRAGMA) != null) {
			    //
			    // Mostly obscure commands, but interface undoubtedly
			    // deserves better treatment - later.
			    //
			    if ((token = scanner.consumeToken(lines, index, PATTERN_CLASS_PRAGMAS)) != null) {
				if (token.equalsIgnoreCase("interface")) {
				    if ((token = scanner.consumeToken(lines, index, PATTERN_CLASS_TYPE)) != null)
					classfile.storeInterface(token);
				    else recordError("invalid interface argument", lines[index], linenumber, line);
				} else if (token.equalsIgnoreCase("major_version")) {
				    if ((number = scanner.consumeInteger(lines, index, MINIMUM_MAJOR_VERSION, MAXIMUM_MAJOR_VERSION)) != null)
					classfile.storeMajor(number.intValue());
				    else recordError("invalid major version argument", lines[index], linenumber, line);
				} else if (token.equalsIgnoreCase("minor_version")) {
				    if ((number = scanner.consumeInteger(lines, index, MINIMUM_MINOR_VERSION, MAXIMUM_MINOR_VERSION)) != null)
					classfile.storeMinor(number.intValue());
				    else recordError("invalid minor version argument", lines[index], linenumber, line);
				} else if (token.equalsIgnoreCase("sourcefile")) {
				    if ((token = scanner.consumeToken(lines, index, PATTERN_CLASS_TYPE)) != null)
					classfile.storeSourceFile(token);
				    else recordError("invalid source file argument", lines[index], linenumber, line);
				} else if (token.equalsIgnoreCase("deprecated"))
				    classfile.storeDeprecatedClass();
				else recordError("unimplemented class pragma", lines[index], linenumber, line);
			    } else recordError("unrecognized class pragma", lines[index], linenumber, line);
			} else if (scanner.consumeToken(lines, index, PATTERN_CLASS) != null) {
			    if ((classname = scanner.consumeToken(lines, index, PATTERN_CLASS_NAME)) != null) {
				if (scanner.consumeToken(lines, index, PATTERN_EXTENDS) != null) {
				    if ((superclass = scanner.consumeToken(lines, index, PATTERN_CLASS_NAME)) != null)
					classfile.registerExtension(classname, superclass);
				    else recordError("invalid class directive", lines[index], linenumber, line);
				} else recordError("invalid class directive", lines[index], linenumber, line);
			    } else recordError("invalid class directive", lines[index], linenumber, line);
			} else if ((field = scanner.consumeField(lines, index, linenumber)) != null) {
			    if (field.isValid()) {
				if (classfile.storeField(field) < 0)
				    recordError("invalid field definition", lines[index], linenumber, line);
			    } else recordError("syntax error in field definition", lines[index], linenumber, line);
			} else if ((method = scanner.consumeConstructor(lines, index, linenumber)) != null) {
			    if (method.isValid()) {
				if (classfile.storeMethod(method) < 0)
				    recordError("invalid constructor definition", lines[index], linenumber, line);
			    }
			} else if ((method = scanner.consumeMethod(lines, index, linenumber)) != null) {
			    if (method.isValid()) {
				if (classfile.storeMethod(method) < 0)
				    recordError("invalid method definition", lines[index], linenumber, line);
			    }
			} else recordError("class file syntax error", lines[index], linenumber, line);
		    } else {
			scanner.consumeLine(lines, index);
			classfile.completeClassFile(this);
			break;
		    }
		}
	    }
	}

	return(errors == errorcount);
    }


    private void
    endCatch() {

	JVMInstruction  instruction;
	ArrayList       data;
	int             start;
	int             end;
	int             index;
	int             n;

	data = (ArrayList)trystack.get(trystack.size() - 1);
	if (data.size() > 2) {
	    start = ((Integer)data.get(0)).intValue();
	    end = ((Integer)data.get(1)).intValue();
	    index = ((Integer)data.get(data.size() - 2)).intValue();
	    instruction = instructions[index];
	    for (n = start; n < end; n++) {
		instructions[n].addSuccessor(instruction);
		instruction.addPredecessor(instructions[n]);
	    }
	}
    }


    private void
    endTry() {

	ArrayList  data;
	int        index;

	if ((index = trystack.size() - 1) >= 0) {
	    intry = (index > 0);
	    data = (ArrayList)trystack.remove(index);
	    if (data.size() >= 4)
		exception_table.add(data);
	}
    }


    private void
    ensureCodeCapacity(int count) {

	byte  tmp[];
	int   length;

	if (bytecode != null) {
	    if (nextbyte + count > bytecode.length) {
		length = bytecode.length + count;
		tmp = new byte[length];
		if (bytecode.length > 0)
		    System.arraycopy(bytecode, 0, tmp, 0, bytecode.length);
		bytecode = tmp;
	    }
	} else {
	    bytecode = new byte[count];
	    nextbyte = 0;
	}
    }


    private void
    ensureInstructionsCapacity(int count) {

	JVMInstruction  tmp[];
	int             length;

	if (instructions != null) {
	    if (nextinstruction + count > instructions.length) {
		length = instructions.length + count;
		tmp = new JVMInstruction[length];
		if (instructions.length > 0)
		    System.arraycopy(instructions, 0, tmp, 0, instructions.length);
		instructions = tmp;
	    }
	} else {
	    instructions = new JVMInstruction[count];
	    nextinstruction = 0;
	}
    }


    private String
    getSortedErrors() {

	StringBuffer  sbuf;
	String        errors = null;
	Object        keys[];
	Set           keyset;
	int           n;

	if (errorcount > 0) {
	    if ((keyset = errormessages.keySet()) != null) {
		sbuf = new StringBuffer();
		keys = keyset.toArray();
		Arrays.sort(keys);
		for (n = 0; n < keys.length; n++)
		    sbuf.append(errormessages.get(keys[n]));
		errors = sbuf.toString();
	    }
	}

	return(errors);
    }


    private boolean
    methodAnalyzer()

	throws JVMAssemblerError

    {

	JVMInstruction  instruction;
	Object          extra;
	Object          table[];
	int             target;
	int             errors;
	int             m;
	int             n;

	//
	// This pass examines each instruction and builds the successor and
	// and precedessor lists that are stored in each instruction. Doubt
	// we really need a complete list of predecessors, since we probably
	// will only use one predecessor to initialize the typestack when we
	// actually assemble an instruction. The low level implementation of
	// JVMInstruction can decide how to handle multiple predecessors, but
	// there's a small chance that someday we'll compare the predecessor
	// stacks just to validate the bytecode.
	//

	errors = errorcount;

	for (n = 0; n < nextinstruction; n++) {
	    instruction = instructions[n];
	    switch (instruction.getOpcode()) {
		case OP_IFEQ:
		case OP_IFGE:
		case OP_IFGT:
		case OP_IFLE:
		case OP_IFLT:
		case OP_IFNE:
		case OP_IFNONNULL:
		case OP_IFNULL:
		case OP_IF_ACMPEQ:
		case OP_IF_ACMPNE:
		case OP_IF_ICMPEQ:
		case OP_IF_ICMPGE:
		case OP_IF_ICMPGT:
		case OP_IF_ICMPLE:
		case OP_IF_ICMPLT:
		case OP_IF_ICMPNE:
		case OP_JSR:
		case OP_JSR_W:
		    if (n < nextinstruction - 1 || instructions[n].getCodeModel() != STRICT_CODE_MODEL) {
			if (n < nextinstruction - 1) {
			    instruction.addSuccessor(instructions[n + 1]);
			    instructions[n + 1].addPredecessor(instruction);
			}
			if ((extra = instruction.getExtra()) != null) {
			    if (extra instanceof String)
				target = method.getBranchTarget((String)extra);
			    else target = ((Number)extra).intValue();
			    if (target >= 0) {
				if (target < nextinstruction) {
				    instruction.addSuccessor(instructions[target]);
				    instructions[target].addPredecessor(instruction);
				}
			    } else recordError("invalid branch target", instruction);
			} else recordInternalError("instruction is missing required extra data");
		    } else recordError("invalid final instruction", instruction);
		    break;

		case OP_GOTO:
		case OP_GOTO_W:
		    if ((extra = instruction.getExtra()) != null) {
			if (extra instanceof String)
			    target = method.getBranchTarget((String)extra);
			else target = ((Number)extra).intValue();
			if (target >= 0) {
			    if (target < nextinstruction) {
				instruction.addSuccessor(instructions[target]);
				instructions[target].addPredecessor(instruction);
			    }
			} else recordError("invalid branch target", instruction);
		    } else recordInternalError("instruction is missing required extra data");
		    break;

		case OP_SWITCH:
		case OP_LOOKUPSWITCH:
		case OP_TABLESWITCH:
		    if ((extra = instruction.getExtra()) != null) {
			if (extra instanceof Object[]) {
			    table = (Object[])extra;
			    for (m = 1; m < table.length; m += 2) {
				if (table[m] instanceof String)
				    target = method.getBranchTarget((String)table[m]);
				else target = ((Number)table[m]).intValue();
				if (target >= 0) {
				    if (target < nextinstruction) {
					instruction.addSuccessor(instructions[target]);
					instructions[target].addPredecessor(instruction);
				    }
				} else recordError("invalid branch target", instruction);
			    }
			} else recordInternalError("extra data for switch statement is invalid");
		    } else recordInternalError("instruction is missing required extra data");
		    break;

		case OP_ARETURN:
		case OP_DRETURN:
		case OP_FRETURN:
		case OP_IRETURN:
		case OP_LRETURN:
		case OP_RETURN:
		    break;

		case OP_RET:
		    break;

		default:
		    if (n < nextinstruction - 1) {
			instruction.addSuccessor(instructions[n + 1]);
			instructions[n + 1].addPredecessor(instruction);
		    }
		    break;
	    }
	}

	return(errors == errorcount);
    }


    private boolean
    methodAssembler()

	throws JVMAssemblerError

    {

	JVMInstruction  instruction;
	boolean         result = true;
	int             n;

	//
	// This pass assembles the individual instructions. The loop that
	// clears visited should be unnecessary as long as we don't set it
	// anywhere else. Better safe than sorry, so we'll leave things be
	// for now.
	//

	if (instructions != null && nextinstruction > 0) {
	    typestack = new JVMTypeStack(classfile, method, this);
	    for (n = 0; n < nextinstruction; n++)
		instructions[n].setVisited(false);
	    //
	    // This assembles the instructions that are reachable from
	    // the method's entry point. Exception handlers eventually
	    // could also be assembled by this method using exactly the
	    // same technique.
	    //

	    result = assembleInstructionsReachableFrom(instructions[0]);

	    //
	    // Makes final adjustments, if any, to the bytecode that we
	    // generated for each instruction.
	    //

	    if (result)
		replaceOpcodes();
	}

	return(result);
    }


    private boolean
    methodBuilder()

	throws JVMAssemblerError

    {

	JVMInstruction  instruction;
	Object          table[];
	byte            code[];
	int             max_stack;
	int             size;
	int             errors;
	int             length;
	int             opcode;
	int             value;
	int             count;
	int             low;
	int             high;
	int             pad;
	int             m;
	int             n;

	//
	// This pass collects the code that's been assembled and stored in
	// each instruction.
	//
	// NOTE - right now we don't throw JVMAssemblerError, but we may if
	// we find dead code or an invalid instruction??
	//

	errors = errorcount;
	nextbyte = 0;
	max_stack = 0;

	for (n = 0; n < nextinstruction; n++) {
	    instruction = instructions[n];
	    opcode = instruction.getOpcode();
	    if (instruction.getVisited()) {
		if (instruction.isValid()) {
		    if ((code = instruction.getByteCode()) != null) {
			if ((length = code.length) > 0) {
			    ensureCodeCapacity(length);
			    System.arraycopy(code, 0, bytecode, nextbyte, length);
			    instruction.setPC(nextbyte);
			    nextbyte += length;
			    if ((size = instructions[n].getMaxStack()) > max_stack)
				max_stack = size;
			    if (opcode == OP_LOOKUPSWITCH || opcode == OP_TABLESWITCH) {
				table = (Object[])instruction.getExtra();
				pad = 3 - (nextbyte-1)%4;
				ensureCodeCapacity(pad);
				for (; pad > 0; pad--)
				    bytecode[nextbyte++] = 0;
				switch (opcode) {
				    case OP_TABLESWITCH:
					ensureCodeCapacity(4*(3 + (table.length - 2)/2));
					value = (table[1] instanceof Integer) ? ((Integer)table[1]).intValue() : 0;
					bytecode[nextbyte++] = (byte)(value >> 24);
					bytecode[nextbyte++] = (byte)(value >> 16);
					bytecode[nextbyte++] = (byte)(value >> 8);
					bytecode[nextbyte++] = (byte)value;
					low = ((Integer)table[2]).intValue();
					bytecode[nextbyte++] = (byte)(low >> 24);
					bytecode[nextbyte++] = (byte)(low >> 16);
					bytecode[nextbyte++] = (byte)(low >> 8);
					bytecode[nextbyte++] = (byte)low;
					high = ((Integer)table[table.length - 2]).intValue();
					bytecode[nextbyte++] = (byte)(high >> 24);
					bytecode[nextbyte++] = (byte)(high >> 16);
					bytecode[nextbyte++] = (byte)(high >> 8);
					bytecode[nextbyte++] = (byte)high;

					for (m = 3; m < table.length; m += 2) {
					    value = (table[m] instanceof Integer) ? ((Integer)table[m]).intValue() : 0;
					    bytecode[nextbyte++] = (byte)(value >> 24);
					    bytecode[nextbyte++] = (byte)(value >> 16);
					    bytecode[nextbyte++] = (byte)(value >> 8);
					    bytecode[nextbyte++] = (byte)value;
					}
					break;

				    case OP_LOOKUPSWITCH:
					ensureCodeCapacity(4*table.length);
					value = (table[1] instanceof Integer) ? ((Integer)table[1]).intValue() : 0;
					bytecode[nextbyte++] = (byte)(value >> 24);
					bytecode[nextbyte++] = (byte)(value >> 16);
					bytecode[nextbyte++] = (byte)(value >> 8);
					bytecode[nextbyte++] = (byte)value;
					count = (table.length - 2)/2;
					bytecode[nextbyte++] = (byte)(count >> 24);
					bytecode[nextbyte++] = (byte)(count >> 16);
					bytecode[nextbyte++] = (byte)(count >> 8);
					bytecode[nextbyte++] = (byte)count;

					for (m = 2; m < table.length; m++) {
					    value = (table[m] instanceof Integer) ? ((Integer)table[m]).intValue() : 0;
					    bytecode[nextbyte++] = (byte)(value >> 24);
					    bytecode[nextbyte++] = (byte)(value >> 16);
					    bytecode[nextbyte++] = (byte)(value >> 8);
					    bytecode[nextbyte++] = (byte)value;
					}
					break;
				}
			    }
			}
		    }
		} else {
		    //
		    // Instruction was marked invalid - what should we do??
		    //
		}
	    } else {
		//
		// Dead code - what should we do??
		//
	    }
	}

	if (errors == errorcount) {
	    resolveBranchOffsets();
	    method.storeMaxStack(max_stack);		// right now this must be first
	    method.storeCode(bytecode, nextbyte, buildExceptionTable());
	}

	return(errors == errorcount);
    }


    private boolean
    methodScanner(String lines[], int index, int linenumber, Pattern endpattern)

	throws JVMAssemblerError

    {

	JVMScanner  scanner;
	String      label;
	String      line;
	String      token;
	Number      number;
	Object      table[];
	Object      call[];
	int         model;
	int         errors;
	int         args[];
	int         opcode;
	int         length;

	//
	// The first pass for the code assembler translates input lines into
	// the array of JVMInstructions that's used by the remaining passes.
	//

	instructions = null;
	model = codemodel;
	errors = errorcount;

	if (lines != null && method != null && classfile != null) {
	    if ((length = lines.length) > 0 && index < length) {
		scanner = new JVMScanner(this, classfile, method);
		args = new int[] {0, 0, 0};
		nextinstruction = 0;
		for (; index < length; index++, linenumber++) {
		    if ((line = lines[index]) != null) {
			if ((label = scanner.consumeToken(lines, index, PATTERN_BRANCH_LABEL)) != null) {
			    if (method.registerBranchLabel(label, nextinstruction) == false)
				recordError("label is already defined", line, linenumber);
			}
			if ((number = scanner.consumeReservedWord(lines, index, JVMMisc.MNEMONIC_TO_OPCODE)) != null) {
			    switch (opcode = number.intValue()) {
				//
				// These opcodes take no arguments.
				//
				case OP_AALOAD:
				case OP_AASTORE:
				case OP_ACONST_NULL:
				case OP_ALOAD_0:
				case OP_ALOAD_1:
				case OP_ALOAD_2:
				case OP_ALOAD_3:
				case OP_ARETURN:
				case OP_ARRAYLENGTH:
				case OP_ASTORE_0:
				case OP_ASTORE_1:
				case OP_ASTORE_2:
				case OP_ASTORE_3:
				case OP_ATHROW:
				case OP_BALOAD:
				case OP_BASTORE:
				case OP_CALOAD:
				case OP_CASTORE:
				case OP_D2F:
				case OP_D2I:
				case OP_D2L:
				case OP_DALOAD:
				case OP_DASTORE:
				case OP_DCMPG:
				case OP_DCMPL:
				case OP_DCONST_0:
				case OP_DCONST_1:
				case OP_DLOAD_0:
				case OP_DLOAD_1:
				case OP_DLOAD_2:
				case OP_DLOAD_3:
				case OP_DUP2:
				case OP_DUP2_X1:
				case OP_DUP2_X2:
				case OP_DUP_X1:
				case OP_DUP_X2:
				case OP_F2D:
				case OP_F2I:
				case OP_F2L:
				case OP_FALOAD:
				case OP_FASTORE:
				case OP_FCMPG:
				case OP_FCMPL:
				case OP_FCONST_0:
				case OP_FCONST_1:
				case OP_FCONST_2:
				case OP_FLOAD_0:
				case OP_FLOAD_1:
				case OP_FLOAD_2:
				case OP_FLOAD_3:
				case OP_I2B:
				case OP_I2C:
				case OP_I2D:
				case OP_I2F:
				case OP_I2L:
				case OP_I2S:
				case OP_IALOAD:
				case OP_IASTORE:
				case OP_ICONST_0:
				case OP_ICONST_1:
				case OP_ICONST_2:
				case OP_ICONST_3:
				case OP_ICONST_4:
				case OP_ICONST_5:
				case OP_ICONST_M1:
				case OP_ILOAD_0:
				case OP_ILOAD_1:
				case OP_ILOAD_2:
				case OP_ILOAD_3:
				case OP_L2D:
				case OP_L2F:
				case OP_L2I:
				case OP_LALOAD:
				case OP_LASTORE:
				case OP_LCMP:
				case OP_LCONST_0:
				case OP_LCONST_1:
				case OP_LLOAD_0:
				case OP_LLOAD_1:
				case OP_LLOAD_2:
				case OP_LLOAD_3:
				case OP_MONITORENTER:
				case OP_MONITOREXIT:
				case OP_NOP:
				case OP_POP2:
				case OP_SALOAD:
				case OP_SASTORE:
				case OP_SWAP:
				case OP_DADD:
				case OP_FADD:
				case OP_IADD:
				case OP_LADD:
				case OP_DDIV:
				case OP_FDIV:
				case OP_IDIV:
				case OP_LDIV:
				case OP_DMUL:
				case OP_FMUL:
				case OP_IMUL:
				case OP_LMUL:
				case OP_DREM:
				case OP_FREM:
				case OP_IREM:
				case OP_LREM:
				case OP_DSUB:
				case OP_FSUB:
				case OP_ISUB:
				case OP_LSUB:
				case OP_IAND:
				case OP_IOR:
				case OP_IXOR:
				case OP_ISHL:
				case OP_ISHR:
				case OP_IUSHR:
				case OP_LAND:
				case OP_LOR:
				case OP_LXOR:
				case OP_LSHL:
				case OP_LSHR:
				case OP_LUSHR:
				case OP_DNEG:
				case OP_DSTORE_0:
				case OP_DSTORE_1:
				case OP_DSTORE_2:
				case OP_DSTORE_3:
				case OP_DRETURN:
				case OP_FNEG:
				case OP_FSTORE_0:
				case OP_FSTORE_1:
				case OP_FSTORE_2:
				case OP_FSTORE_3:
				case OP_FRETURN:
				case OP_INEG:
				case OP_ISTORE_0:
				case OP_ISTORE_1:
				case OP_ISTORE_2:
				case OP_ISTORE_3:
				case OP_IRETURN:
				case OP_LNEG:
				case OP_LSTORE_0:
				case OP_LSTORE_1:
				case OP_LSTORE_2:
				case OP_LSTORE_3:
				case OP_LRETURN:
				case OP_DUP:
				case OP_POP:
				case OP_RETURN:
				    addInstruction(line, linenumber, opcode, model);
				    break;

				//
				// These opcodes use the first argument as an index into the
				// local variable array.
				//
				case OP_ALOAD:
				case OP_DLOAD:
				case OP_FLOAD:
				case OP_ILOAD:
				case OP_LLOAD:
				case OP_RET:
				case OP_ASTORE:
				case OP_DSTORE:
				case OP_FSTORE:
				case OP_ISTORE:
				case OP_LSTORE:
				    if ((number = scanner.consumeLocalVariable(lines, index)) != null) {
					args[0] = number.intValue();
					addInstruction(line, linenumber, opcode, model, args[0]);
				    } else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				//
				// This opcode uses the byte argument as immediate data that's
				// sign extended to an int and pushed onto the operand stack.
				//
				case OP_BIPUSH:
				    if ((number = scanner.consumeInteger(lines, index, Byte.MIN_VALUE, Byte.MAX_VALUE)) != null) {
					args[0] = number.intValue();
					addInstruction(line, linenumber, opcode, model, args[0]);
				    } else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				//
				// This opcode uses the byte argument to determine the type of
				// the new array. Its size is determined by the int on top of
				// the operand stack.
				//
				case OP_NEWARRAY:
				    if ((number = scanner.consumeArrayType(lines, index)) != null) {
					args[0] = number.intValue();
					addInstruction(line, linenumber, opcode, model, args[0]);
				    } else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				//
				// This opcode uses its argument as an unsigned int that's used
				// as a constant pool index.
				//
				case OP_LDC:
				    if ((number = scanner.consumeConstantPoolLiteral(lines, index, false)) != null) {
					args[0] = number.intValue();
					addInstruction(line, linenumber, opcode, model, args[0]);
				    } else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				//
				// These opcodes combine the two unsigned byte arguments into
				// an int that's used as a constant pool index.
				//
				case OP_ANEWARRAY:
				case OP_CHECKCAST:
				case OP_INSTANCEOF:
				case OP_NEW:
				    if ((number = scanner.consumeConstantPoolClass(lines, index)) != null) {
					args[0] = number.intValue();
					addInstruction(line, linenumber, opcode, model, args[0]);
				    } else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				case OP_GETFIELD:
				case OP_GETSTATIC:
				case OP_PUTFIELD:
				case OP_PUTSTATIC:
				    if ((number = scanner.consumeConstantPoolField(lines, index)) != null) {
					args[0] = number.intValue();
					addInstruction(line, linenumber, opcode, model, args[0]);
				    } else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				case OP_INVOKESPECIAL:
				case OP_INVOKESTATIC:
				case OP_INVOKEVIRTUAL:
				    if ((number = scanner.consumeConstantPoolMethod(lines, index)) != null) {
					args[0] = number.intValue();
					addInstruction(line, linenumber, opcode, model, args[0]);
				    } else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				case OP_LDC2_W:
				case OP_LDC_W:
				    if ((number = scanner.consumeConstantPoolLiteral(lines, index, true)) != null) {
					args[0] = number.intValue();
					addInstruction(line, linenumber, opcode, model, args[0]);
				    } else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				//
				// These opcodes combine the two byte arguments into a signed 16
				// bit number that's added to the address of this opcode to get
				// the address of the next instruction.
				//
				case OP_IFEQ:
				case OP_IFGE:
				case OP_IFGT:
				case OP_IFLE:
				case OP_IFLT:
				case OP_IFNE:
				case OP_IF_ICMPEQ:
				case OP_IF_ICMPGE:
				case OP_IF_ICMPGT:
				case OP_IF_ICMPLE:
				case OP_IF_ICMPLT:
				case OP_IF_ICMPNE:
				case OP_IFNONNULL:
				case OP_IFNULL:
				case OP_IF_ACMPEQ:
				case OP_IF_ACMPNE:
				    if ((label = scanner.consumeBranchLabel(lines, index, nextinstruction)) != null)
					addInstruction(line, linenumber, opcode, model, -1, label);
				    else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				//
				// The argument is translated into a signed offset that's added
				// to the address of this opcode to get the address of the next
				// instruction.
				//
				case OP_GOTO:
				case OP_JSR:
				    if ((label = scanner.consumeBranchLabel(lines, index, nextinstruction)) != null)
					addInstruction(line, linenumber, opcode, model, -1, label);
				    else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				//
				// The first argument is an index into the local variable array.
				// The second argument is immediate data that's sign extended to
				// an int and used to increment the local variable.
				//
				// NOTE - replaceOpcode() code that handles OP_IINC assumes
				// the instruction's args[] array contains three slots when it
				// decides it needs to use the wide version, which should explain
				// why we hand an extra argument to addInstruction().
				//
				case OP_IINC:
				    if ((number = scanner.consumeLocalVariable(lines, index)) != null) {
					args[0] = number.intValue();
					if ((number = scanner.consumeInteger(lines, index, Short.MIN_VALUE, Short.MAX_VALUE)) != null) {
					    args[1] = number.intValue();
					    addInstruction(line, linenumber, opcode, model, args[0], args[1], 0);
					} else recordOpcodeError(opcode, lines, index, linenumber, line);
				    } else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				//
				// This opcode combines the two bytes into a short which is sign
				// extended to an int and the result is pushed onto the operand
				// stack.
				//
				case OP_SIPUSH:
				    if ((number = scanner.consumeInteger(lines, index, Short.MIN_VALUE, Short.MAX_VALUE)) != null) {
					args[0] = number.intValue();
					addInstruction(line, linenumber, opcode, model, args[0]);
				    } else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				//
				// The first two bytes are combined into a constant pool index.
				// The third byte is an unsigned byte (greater that zero) that
				// specifies the number of dimensions in the new array.
				//
				case OP_MULTIANEWARRAY:
				    if ((number = scanner.consumeConstantPoolClass(lines, index)) != null) {
					args[0] = number.intValue();
					if ((number = scanner.consumeInteger(lines, index, MINIMUM_ARRAY_DIMENSIONS, MAXIMUM_ARRAY_DIMENSIONS)) != null) {
					    args[1] = number.intValue();
					    addInstruction(line, linenumber, opcode, model, args[0], args[1]);
					} else recordOpcodeError(opcode, lines, index, linenumber, line);
				    } else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				//
				// The argument is translated into a signed integer offset that's
				// added to the address of this opcode to get the address of the
				// next instruction.
				//
				case OP_GOTO_W:
				case OP_JSR_W:
				    if ((label = scanner.consumeBranchLabel(lines, index, nextinstruction)) != null)
					addInstruction(line, linenumber, opcode, model, -1, label);
				    else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				//
				// The first two bytes are combined into a constant pool index.
				// The third byte is an unsigned byte that (I think) records how
				// much space (i.e., how many ints) are needed for the arguments,
				// where each long or double argument adds 2 to count.
				//
				case OP_INVOKEINTERFACE:
				    if ((number = scanner.consumeConstantPoolMethod(lines, index)) != null) {
					args[0] = number.intValue();
					if ((number = scanner.consumeInteger(lines, index, MINIMUM_ARRAY_DIMENSIONS, MAXIMUM_ARRAY_DIMENSIONS)) != null) {
					    args[1] = number.intValue();
					    addInstruction(line, linenumber, opcode, model, args[0], args[1]);
					} else recordOpcodeError(opcode, lines, index, linenumber, line);
				    } else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				//
				// These are used to implement switch statements. OP_TABLESWITCH
				// is appropriate when there are no gaps in the case expressions,
				// otherwise OP_LOOKUPSWITCH should be used. The OP_SWITCH virtual
				// opcode (handled later) tries to pick the one that's appropriate
				// for the supplied switch table.
				//
				case OP_LOOKUPSWITCH:
				case OP_TABLESWITCH:
				    if ((table = scanner.consumeSwitchTable(lines, index, nextinstruction, linenumber)) != null)
					addInstruction(line, linenumber, opcode, model, 0, table);
				    else recordOpcodeError(opcode, lines, index, linenumber, line);
				    break;

				//
				// Variable length instruction that allows opcodes that access
				// local variables to get at more than 256 of them.
				//
				case OP_WIDE:
				    if ((number = scanner.consumeReservedWord(lines, index, JVMMisc.MNEMONIC_TO_OPCODE)) != null) {
					args[0] = number.intValue();
					switch (args[0]) {
					    case OP_IINC:
						if ((number = scanner.consumeLocalVariable(lines, index)) != null) {
						    args[1] = number.intValue();
						    if ((number = scanner.consumeInteger(lines, index, Short.MIN_VALUE, Short.MAX_VALUE)) != null) {
							args[2] = number.intValue();
							addInstruction(line, linenumber, opcode, model, args[0], args[1], args[2]);
						    } else recordOpcodeError(args[0], lines, index, linenumber, line);
						} else recordOpcodeError(args[0], lines, index, linenumber, line);
						break;

					    case OP_ALOAD:
					    case OP_ASTORE:
					    case OP_DLOAD:
					    case OP_DSTORE:
					    case OP_FLOAD:
					    case OP_FSTORE:
					    case OP_ILOAD:
					    case OP_ISTORE:
					    case OP_LLOAD:
					    case OP_LSTORE:
					    case OP_RET:
						if ((number = scanner.consumeLocalVariable(lines, index)) != null) {
						    args[1] = number.intValue();
						    addInstruction(line, linenumber, opcode, model, args[0], args[1]);
						} else recordOpcodeError(args[0], lines, index, linenumber, line);
						break;

					    default:
						recordError("invalid use of the wide opcode", lines[index], linenumber, line);
						break;
					}
				} else recordOpcodeError(opcode, lines, index, linenumber, line);
				break;

				//
				// The remaining opcodes, which are divided into two groups, are
				// the "virtual instructions" that are supported by the assembler.
				//
				default:
				    if (model != STRICT_CODE_MODEL) {
					switch (opcode) {
					    case OP_ADD:
					    case OP_SUB:
					    case OP_MUL:
					    case OP_DIV:
					    case OP_REM:
					    case OP_AND:
					    case OP_OR:
					    case OP_XOR:
					    case OP_ARRAYLOAD:
					    case OP_ARRAYSTORE:
					    case OP_EXCH:
					    case OP_NEG:
					    case OP_SHL:
					    case OP_SHR:
					    case OP_USHR:
					    case OP_CAST2D:
					    case OP_CAST2F:
					    case OP_CAST2I:
					    case OP_CAST2L:
					    case OP_DUPX:
						addInstruction(line, linenumber, opcode, model);
						break;

					    case OP_CALL:
						if ((call = scanner.consumeCall(lines, index, nextinstruction, linenumber)) != null)
						    addInstruction(line, linenumber, opcode, model, 0, call);
						else recordOpcodeError(opcode, lines, index, linenumber, line);
						break;

					    case OP_INVOKE:
						if ((number = scanner.consumeConstantPoolMethod(lines, index)) != null) {
						    args[0] = number.intValue();
						    addInstruction(line, linenumber, opcode, model, args[0]);
						} else recordOpcodeError(opcode, lines, index, linenumber, line);
						break;

					    case OP_SWITCH:
						if ((table = scanner.consumeSwitchTable(lines, index, nextinstruction, linenumber)) != null)
						    addInstruction(line, linenumber, opcode, model, 0, table);
						else recordOpcodeError(opcode, lines, index, linenumber, line);
						break;

					    //
					    // The last two are a little more complicated because they
					    // accept more than one kind of argument and it's the type
					    // of that argument that determines the instruction that we
					    // add.
					    //

					    case OP_PUSH:
						if ((number = scanner.consumeQualifiedLiteralNumber(lines, index)) != null) {
						    if (number instanceof Integer)
							addInstruction(line, linenumber, OP_ICONST, model, 0, number);
						    else if (number instanceof Long)
							addInstruction(line, linenumber, OP_LCONST, model, 0, number);
						    else if (number instanceof Float)
							addInstruction(line, linenumber, OP_FCONST, model, 0, number);
						    else addInstruction(line, linenumber, OP_DCONST, model, 0, number);
						} else if ((number = scanner.consumeConstantPoolString(lines, index)) != null) {
						    addInstruction(line, linenumber, OP_LDC, model, number.intValue());
						} else if (scanner.consumeToken(lines, index, PATTERN_NULL) != null) {
						    addInstruction(line, linenumber, OP_ACONST_NULL, model);
						} else if (scanner.consumeToken(lines, index, PATTERN_THIS) != null) {
						    if (method.isStatic() == false)
							addInstruction(line, linenumber, OP_ALOAD_0, model);
						    else recordError("can't use this in a static method", line, linenumber);
						} else if ((number = scanner.consumeLocalVariable(lines, index)) != null) {
						    addInstruction(line, linenumber, OP_LOAD, model, number.intValue());
						} else if ((number = scanner.consumeConstantPoolField(lines, index)) != null) {
						    addInstruction(line, linenumber, OP_GET, model, number.intValue());
						} else recordOpcodeError(opcode, lines, index, linenumber, line);
						break;

					    case OP_STORE:
						if ((number = scanner.consumeLocalVariable(lines, index)) != null)
						    addInstruction(line, linenumber, OP_STORE, model, number.intValue());
						else if ((number = scanner.consumeConstantPoolField(lines, index)) != null)
						    addInstruction(line, linenumber, OP_PUT, model, number.intValue());
						else recordOpcodeError(opcode, lines, index, linenumber, line);
						break;

					    default:
						recordError("code for scanning instruction \"" + OPCODE_MNEMONICS[opcode] + "\" is missing", line, linenumber);
						break;
					}
				    } else recordError("instruction is not allowed in strict mode", line, linenumber);
				    break;
			    }
			    if (scanner.consumeLine(lines, index) == false)
				recordOpcodeError(opcode, lines, index, linenumber, line);
			} else {
			    if (scanner.consumeToken(lines, index, PATTERN_EXTERN) != null) {
				if (scanner.consumeExternalMethod(lines, index) == false)
				    scanner.consumeExternalField(lines, index);
			    } else if (scanner.consumeToken(lines, index, PATTERN_PRAGMA) != null) {
				if ((token = scanner.consumeToken(lines, index, PATTERN_CODE_PRAGMAS)) != null) {
				    if (token.equalsIgnoreCase("model")) {
					if ((number = scanner.consumeInteger(lines, index, MIN_CODE_MODEL, MAX_CODE_MODEL)) != null)
					    model = number.intValue();
					else recordError("invalid code model argument", lines[index], linenumber, line);
				    } else recordError("unimplemented code pragma", lines[index], linenumber, line);
				} else recordError("unrecognized code pragma", lines[index], linenumber, line);
			    } else if (scanner.consumeToken(lines, index, PATTERN_START_TRY) != null) {
				startTry();
			    } else if (intry && scanner.consumeToken(lines, index, PATTERN_START_CATCH) != null) {
				token = scanner.consumeToken(lines, index, PATTERN_CLASS_NAME);
				if (scanner.consumeToken(lines, index, PATTERN_OPEN_BRACE) != null) {
				    endCatch();
				    startCatch(token);
				} else recordError("catch block syntax error", lines[index], linenumber, line);
			    } else if (intry && scanner.consumeToken(lines, index, PATTERN_CLOSE_BRACE) != null) {
				endCatch();
				endTry();
			    } else if (scanner.consumeToken(lines, index, endpattern) != null)
				break;
			    else scanner.consumeLocalVariableDeclaration(lines, index);
			    if (scanner.consumeLine(lines, index) == false)
				recordError("invalid opcode or assembler directive", lines[index], linenumber, line);
			}
			lines[index] = null;
		    }
		}
	    }
	}

	return(errorcount == errors && nextinstruction > 0);
    }


    private int
    replaceOpcode(int opcode) {

	return(replaceOpcode(opcode, null, null));
    }


    private int
    replaceOpcode(int opcode, int args[]) {

	return(replaceOpcode(opcode, args, null));
    }


    private int
    replaceOpcode(int opcode, int args[], Object extra) {

	String  descriptor;
	Object  table[];
	double  dvalue;
	float   fvalue;
	long    lvalue;
	int     opcodes[];
	int     ivalue;
	int     index;

	//
	// Called, after any required operand casting, to pick the real JVM
	// opcode that's needed to implement a virtual (or extended) opcode.
	// Several opcodes modify args and we assume that the modifications
	// will not go unnoticed, which currently means that in those cases
	// args[] must be the one that's stored in the opcode's instruction.
	//

	switch (opcode) {
	    case OP_ADD:
	    case OP_DIV:
	    case OP_MUL:
	    case OP_REM:
	    case OP_SUB:
		switch (typestack.peekAtDescriptorCode(0)) {
		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_SHORT:
			switch (opcode) {
			    case OP_ADD:
				opcode = OP_IADD;
				break;

			    case OP_DIV:
				opcode = OP_IDIV;
				break;

			    case OP_MUL:
				opcode = OP_IMUL;
				break;

			    case OP_REM:
				opcode = OP_IREM;
				break;

			    case OP_SUB:
				opcode = OP_ISUB;
				break;
			}
			break;

		    case DESCRIPTOR_DOUBLE:
			switch (opcode) {
			    case OP_ADD:
				opcode = OP_DADD;
				break;

			    case OP_DIV:
				opcode = OP_DDIV;
				break;

			    case OP_MUL:
				opcode = OP_DMUL;
				break;

			    case OP_REM:
				opcode = OP_DREM;
				break;

			    case OP_SUB:
				opcode = OP_DSUB;
				break;
			}
			break;

		    case DESCRIPTOR_FLOAT:
			switch (opcode) {
			    case OP_ADD:
				opcode = OP_FADD;
				break;

			    case OP_DIV:
				opcode = OP_FDIV;
				break;

			    case OP_MUL:
				opcode = OP_FMUL;
				break;

			    case OP_REM:
				opcode = OP_FREM;
				break;

			    case OP_SUB:
				opcode = OP_FSUB;
				break;
			}
			break;

		    case DESCRIPTOR_LONG:
			switch (opcode) {
			    case OP_ADD:
				opcode = OP_LADD;
				break;

			    case OP_DIV:
				opcode = OP_LDIV;
				break;

			    case OP_MUL:
				opcode = OP_LMUL;
				break;

			    case OP_REM:
				opcode = OP_LREM;
				break;

			    case OP_SUB:
				opcode = OP_LSUB;
				break;
			}
			break;
		}
		break;

	    case OP_AND:
	    case OP_OR:
	    case OP_XOR:
		switch (typestack.peekAtDescriptorCode(0)) {
		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_SHORT:
			switch (opcode) {
			    case OP_AND:
				opcode = OP_IAND;
				break;

			    case OP_OR:
				opcode = OP_IOR;
				break;

			    case OP_XOR:
				opcode = OP_IXOR;
				break;
			}
			break;

		    default:
			switch (opcode) {
			    case OP_AND:
				opcode = OP_LAND;
				break;

			    case OP_OR:
				opcode = OP_LOR;
				break;

			    case OP_XOR:
				opcode = OP_LXOR;
				break;
			}
			break;
		}
		break;

	    case OP_ALOAD:
	    case OP_DLOAD:
	    case OP_FLOAD:
	    case OP_ILOAD:
	    case OP_LLOAD:
	    case OP_ASTORE:
	    case OP_DSTORE:
	    case OP_FSTORE:
	    case OP_ISTORE:
	    case OP_LSTORE:
	    case OP_RET:
		if (args[0] > 255) {
		    args[1] = args[0];
		    args[0] = opcode;
		    opcode = OP_WIDE;
		}
		break;

	    case OP_ARRAYLOAD:
	    case OP_ARRAYSTORE:
		index = (opcode == OP_ARRAYLOAD) ? 1 : 2;
		switch (JVMDescriptor.getDescriptorCodeForArrayElement(typestack.peekAtDescriptor(index))) {
		    case DESCRIPTOR_ARRAY:
		    case DESCRIPTOR_CLASS:
			opcode = (opcode == OP_ARRAYLOAD) ? OP_AALOAD : OP_AASTORE;
			break;

		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
			opcode = (opcode == OP_ARRAYLOAD) ? OP_BALOAD : OP_BASTORE;
			break;

		    case DESCRIPTOR_CHAR:
			opcode = (opcode == OP_ARRAYLOAD) ? OP_CALOAD : OP_CASTORE;
			break;

		    case DESCRIPTOR_DOUBLE:
			opcode = (opcode == OP_ARRAYLOAD) ? OP_DALOAD : OP_DASTORE;
			break;

		    case DESCRIPTOR_FLOAT:
			opcode = (opcode == OP_ARRAYLOAD) ? OP_FALOAD : OP_FASTORE;
			break;

		    case DESCRIPTOR_INT:
			opcode = (opcode == OP_ARRAYLOAD) ? OP_IALOAD : OP_IASTORE;
			break;

		    case DESCRIPTOR_LONG:
			opcode = (opcode == OP_ARRAYLOAD) ? OP_LALOAD : OP_LASTORE;
			break;

		    case DESCRIPTOR_SHORT:
			opcode = (opcode == OP_ARRAYLOAD) ? OP_SALOAD : OP_SASTORE;
			break;
		}
		break;

	    case OP_DCONST:
		if ((dvalue = ((Number)extra).doubleValue()) == 0.0)
		    opcode = OP_DCONST_0;
		else if (dvalue == 1.0)
		    opcode = OP_DCONST_1;
		else {
		    args[0] = classfile.storeConstantPoolDouble(dvalue);
		    opcode = OP_LDC2_W;
		}
		break;

	    case OP_DUP:
		opcode = (typestack.peekAtCategory() == 2) ? OP_DUP2 : OP_DUP;
		break;

	    case OP_FCONST:
		if ((fvalue = ((Number)extra).floatValue()) == 0.0)
		    opcode = OP_FCONST_0;
		else if (fvalue == 1.0)
		    opcode = OP_FCONST_1;
		else if (fvalue == 2.0)
		    opcode = OP_FCONST_2;
		else {
		    if ((args[0] = classfile.storeConstantPoolFloat(fvalue)) <= 255)
			opcode = OP_LDC;
		    else opcode = OP_LDC_W;
		}
		break;

	    case OP_GET:
		opcode = classfile.isStaticField(args[0]) ? OP_GETSTATIC : OP_GETFIELD;
		break;

	    case OP_ICONST:
		switch (ivalue = ((Number)extra).intValue()) {
		    case -1:
			opcode = OP_ICONST_M1;
			break;

		    case 0:
			opcode = OP_ICONST_0;
			break;

		    case 1:
			opcode = OP_ICONST_1;
			break;

		    case 2:
			opcode = OP_ICONST_2;
			break;

		    case 3:
			opcode = OP_ICONST_3;
			break;

		    case 4:
			opcode = OP_ICONST_4;
			break;

		    case 5:
			opcode = OP_ICONST_5;
			break;

		    default:
			if (ivalue < Short.MIN_VALUE || ivalue > Short.MAX_VALUE) {
			    if ((args[0] = classfile.storeConstantPoolInt(ivalue)) <= 255)
				opcode = OP_LDC;
			    else opcode = OP_LDC_W;
			} else {
			    args[0] = ivalue;
			    if (ivalue >= Byte.MIN_VALUE && ivalue <= Byte.MAX_VALUE)
				opcode = OP_BIPUSH;
			    else opcode = OP_SIPUSH;
			}
			break;
		}
		break;

	    case OP_IINC:
		if (args[0] > 255 || args[1] > 255) {
		    args[2] = args[1];
		    args[1] = args[0];
		    args[0] = opcode;
		    opcode = OP_WIDE;
		}
		break;

	    case OP_INVOKE:
	    case OP_CALL:
		if (classfile.isSpecialMethod(args[0]) == false) {
		     if (classfile.isInterfaceMethodRef(args[0]) == false) {
			if (classfile.isStaticMethod(args[0]) == false) {
			    if (classfile.isPrivateClassMethod(args[0]))
				opcode = OP_INVOKESPECIAL;
			    else if (classfile.isSuperClassMethod(args[0]))
				opcode = OP_INVOKESPECIAL;
			    else opcode = OP_INVOKEVIRTUAL;
			} else opcode = OP_INVOKESTATIC;
		    } else opcode = OP_INVOKEINTERFACE;
		} else opcode = OP_INVOKESPECIAL;
		break;

	    case OP_LCONST:
		if ((lvalue = ((Number)extra).longValue()) == 0.0)
		    opcode = OP_LCONST_0;
		else if (lvalue == 1.0)
		    opcode = OP_LCONST_1;
		else {
		    args[0] = classfile.storeConstantPoolLong(lvalue);
		    opcode = OP_LDC2_W;
		}
		break;

	    case OP_LDC:
		if (JVMDescriptor.getDescriptorCategory(classfile.getDescriptor(args[0])) == 1) {
		    if (args[0] > 255)
			opcode = OP_LDC_W;
		} else opcode = OP_LDC2_W;
		break;

	    case OP_LOAD:
	    case OP_STORE:
		switch (method.getLocalVariableDescriptorCode(args[0])) {
		    case DESCRIPTOR_ARRAY:
		    case DESCRIPTOR_CLASS:
		    case DESCRIPTOR_RETURNADDRESS:
			//
			// Technically shouldn't allow OP_LOAD when a return address
			// is stored in the variable, but we'll assume someone else
			// is (or eventually will) handling error checking. Suspect
			// it belongs in typestack.execute().
			//
			opcodes = (opcode == OP_LOAD) ? ALOAD_OPCODES : ASTORE_OPCODES;
			break;

		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_SHORT:
			opcodes = (opcode == OP_LOAD) ? ILOAD_OPCODES : ISTORE_OPCODES;
			break;

		    case DESCRIPTOR_DOUBLE:
			opcodes = (opcode == OP_LOAD) ? DLOAD_OPCODES : DSTORE_OPCODES;
			break;

		    case DESCRIPTOR_FLOAT:
			opcodes = (opcode == OP_LOAD) ? FLOAD_OPCODES : FSTORE_OPCODES;
			break;

		    case DESCRIPTOR_LONG:
			opcodes = (opcode == OP_LOAD) ? LLOAD_OPCODES : LSTORE_OPCODES;
			break;

		    default:
			//
			// Should not happen.
			//
			opcodes = null;
			break;
		}
		if (opcodes != null) {
		    opcode = opcodes[Math.max(0, Math.min(args[0], opcodes.length - 1))];
		    if (args[0] > 255) {
			    args[1] = args[0];
			args[0] = opcode;
			opcode = OP_WIDE;
		    }
		}
		break;

	    case OP_NEG:
		switch (typestack.peekAtDescriptorCode(0)) {
		    case DESCRIPTOR_DOUBLE:
			opcode = OP_DNEG;
			break;

		    case DESCRIPTOR_FLOAT:
			opcode = OP_FNEG;
			break;

		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_SHORT:
			opcode = OP_INEG;
			break;

		    case DESCRIPTOR_LONG:
			opcode = OP_LNEG;
			break;
		}
		break;

	    case OP_POP:
		opcode = (typestack.peekAtCategory() == 2) ? OP_POP2 : OP_POP;
		break;

	    case OP_PUT:
		opcode = classfile.isStaticField(args[0]) ? OP_PUTSTATIC : OP_PUTFIELD;
		break;

	    case OP_RETURN:
		switch (method.getDescriptorCodeForReturnValue()) {
		    case DESCRIPTOR_ARRAY:
		    case DESCRIPTOR_CLASS:
			opcode = OP_ARETURN;
			break;

		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_SHORT:
			opcode = OP_IRETURN;
			break;

		    case DESCRIPTOR_DOUBLE:
			opcode = OP_DRETURN;
			break;

		    case DESCRIPTOR_FLOAT:
			opcode = OP_FRETURN;
			break;

		    case DESCRIPTOR_LONG:
			opcode = OP_LRETURN;
			break;

		    case DESCRIPTOR_VOID:
			opcode = OP_RETURN;
			break;
		}
		break;

	    case OP_SHL:
	    case OP_SHR:
	    case OP_USHR:
		switch (typestack.peekAtDescriptorCode(1)) {
		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_SHORT:
			switch (opcode) {
			    case OP_SHL:
				opcode = OP_ISHL;
				break;

			    case OP_SHR:
				opcode = OP_ISHR;
				break;

			    case OP_USHR:
				opcode = OP_IUSHR;
				break;
			}
			break;

		    default:
			switch (opcode) {
			    case OP_SHL:
				opcode = OP_LSHL;
				break;

			    case OP_SHR:
				opcode = OP_LSHR;
				break;

			    case OP_USHR:
				opcode = OP_LUSHR;
				break;
			}
			break;
		}
		break;

	    case OP_SWITCH:
		//
		// In this case the extra argument must be the table data
		// that was collected by the scanner, so a non-zero first
		// element means there aren't any gaps in the pairs.
		//
		table = (Object[])extra;
		opcode = (((Number)table[0]).intValue() != 0 && table.length > 2) ? OP_TABLESWITCH : OP_LOOKUPSWITCH;
		break;
	}

	return(opcode);
    }


    private void
    replaceOpcodes() {

	JVMInstruction  instruction;
	ArrayList       list;
	boolean         done;
	int             totalsize;
	int             distance;
	int             size;
	int             count;
	int             target;
	int             index;
	int             next;
	int             stop;
	int             sign;
	int             n;

	//
	// This method takes a close look at all goto and jsr instructions
	// and tries to make sure that branch offsets will fit in the two
	// bytes that are allowed. If a label is too far away the opcode is
	// changed to wide version and the instruction is reassembled. The
	// distance checks only work after all instructions are assembled.
	//

	list = new ArrayList();
	totalsize = 0;

	for (n = 0; n < nextinstruction; n++) {
	    instruction = instructions[n];
	    if (instruction.getVisited()) {
		if ((size = instruction.getByteCodeSize()) > 0) {
		    totalsize += size;
		    if (instruction.getCodeModel() != STRICT_CODE_MODEL) {
			switch (instruction.getOpcode()) {
			    case OP_GOTO:
			    case OP_JSR:
				list.add(new Integer(n));
				break;
			}
		    }
		}
	    }
	}

	if (totalsize > Short.MAX_VALUE) {
	    do {
		done = true;
		for (n = 0; n < list.size(); n++) {
		    index = ((Integer)list.get(n)).intValue();
		    instruction = instructions[index];
		    target = method.getBranchTarget((String)instruction.getExtra());
		    if (index != target) {
			if (index < target) {
			    next = index;
			    stop = target;
			    sign = 1;
			} else {
			    next = target;
			    stop = index;
			    sign = -1;
			}
			for (distance = 0; next < stop; next++)
			    distance += sign*instructions[next].getByteCodeSize();
			if (distance < Short.MIN_VALUE || distance > Short.MAX_VALUE) {
			    try {
				switch (instruction.getOpcode()) {
				    case OP_GOTO:
					instruction.setOpcode(OP_GOTO_W);
					assembleInstruction(instruction);
					break;

				    case OP_JSR:
					instruction.setOpcode(OP_JSR_W);
					assembleInstruction(instruction);
					break;
				}
			    }
			    catch(JVMAssemblerError e) {}
			    list.remove(n);
			    done = false;
			    break;
			}
		    }
		}
	    } while (done == false);
	}
    }


    private void
    resetClassAssembler() {

	classfile = null;
	errorcount = 0;
	errormessages = new HashMap();
	resetMethodAssembler(null);
    }


    private void
    resetMethodAssembler(JVMMethod method) {

	this.method = method;
	typestack = null;
	bytecode = null;
	instructions = null;
	nextbyte = 0;
	nextinstruction = 0;
	trystack = new ArrayList();
	exception_table = new ArrayList();
    }


    private void
    resolveBranchOffsets()

	throws JVMAssemblerError

    {

	JVMInstruction  instruction;
	ArrayList       list;
	Iterator        iterator;
	HashMap         branchlabels;
	boolean         usereturn;
	String          label;
	Object          value[];
	int             address;
	int             target;
	int             offset;
	int             index;
	int             npairs;
	int             low;
	int             pc;
	int             n;

	if ((branchlabels = method.getBranchLabels()) != null) {
	    if ((iterator = branchlabels.keySet().iterator()) != null) {
		while (iterator.hasNext()) {
		    if ((label = (String)iterator.next()) != null) {
			if ((list = (ArrayList)branchlabels.get(label)) != null) {
			    usereturn = false;
			    if ((index = ((Number)list.get(0)).intValue()) >= nextinstruction) {
				if (instructions[nextinstruction - 1].getReturnOffset() >= 0) {
				    index = nextinstruction - 1;
				    usereturn = true;
				}
			    }
			    if (index >= 0 && index < nextinstruction) {
				if (instructions[index].getVisited()) {
				    address = instructions[index].getPC();
				    if (usereturn)
					address += instructions[index].getReturnOffset();
				    for (n = 1; n < list.size(); n++) {
					if ((value = (Object[])list.get(n)) != null) {
					    instruction = instructions[((Number)value[0]).intValue()];
					    if (instruction.getVisited()) {
						pc = instruction.getAddress();
						offset = address - pc;
						index = pc;
						switch (bytecode[index]&0xFF) {
						    case OP_IFEQ:
						    case OP_IFGE:
						    case OP_IFGT:
						    case OP_IFLE:
						    case OP_IFLT:
						    case OP_IFNE:
						    case OP_IFNONNULL:
						    case OP_IFNULL:
						    case OP_IF_ACMPEQ:
						    case OP_IF_ACMPNE:
						    case OP_IF_ICMPEQ:
						    case OP_IF_ICMPGE:
						    case OP_IF_ICMPGT:
						    case OP_IF_ICMPLE:
						    case OP_IF_ICMPLT:
						    case OP_IF_ICMPNE:
							if (offset >= Short.MIN_VALUE && offset <= Short.MAX_VALUE) {
							    index += 1;
							    bytecode[index++] = (byte)(offset >> 8);
							    bytecode[index++] = (byte)offset;
							} else recordError("branch offset won't fit in two bytes", instruction);
							break;

						    case OP_GOTO:
						    case OP_JSR:
							if (offset >= Short.MIN_VALUE && offset <= Short.MAX_VALUE) {
							    index += 1;
							    bytecode[index++] = (byte)(offset >> 8);
							    bytecode[index++] = (byte)offset;
							} else recordError("branch offset won't fit in two bytes", instruction);
							break;

						    case OP_GOTO_W:
						    case OP_JSR_W:
							index += 1;
							bytecode[index++] = (byte)(offset >> 24);
							bytecode[index++] = (byte)(offset >> 16);
							bytecode[index++] = (byte)(offset >> 8);
							bytecode[index++] = (byte)offset;
							break;

						    case OP_LOOKUPSWITCH:
							index += (3 - pc%4) + 1;
							if (value[1] != null) {
							    target = ((Number)value[1]).intValue();
							    npairs = JVMMisc.getInt(bytecode, index + 4);
							    for (index += 8; npairs > 0; index += 8, npairs--) {
								if (JVMMisc.getInt(bytecode, index) == target) {
								    index += 4;
								    break;
								}
							    }
							}
							bytecode[index++] = (byte)(offset >> 24);
							bytecode[index++] = (byte)(offset >> 16);
							bytecode[index++] = (byte)(offset >> 8);
							bytecode[index++] = (byte)offset;
							break;

						    case OP_TABLESWITCH:
							index += (3 - pc%4) + 1;
							if (value[1] != null) {
							    target = ((Number)value[1]).intValue();
							    low = JVMMisc.getInt(bytecode, index + 4);
							    index += 12 + 4*(int)(target - low);
							}
							bytecode[index++] = (byte)(offset >> 24);
							bytecode[index++] = (byte)(offset >> 16);
							bytecode[index++] = (byte)(offset >> 8);
							bytecode[index++] = (byte)offset;
							break;
						}
					    }
					}
				    }
				}
			    } else {
				if (index >= 0) {
				    for (n = 1; n < list.size(); n++) {
					if ((value = (Object[])list.get(n)) != null) {
					    index = ((Number)value[0]).intValue();
					    if (index >= 0 && index < nextinstruction) {
						instruction = instructions[index];
						if (instruction.getVisited()) {
						    recordError("referenced label " + label + " is not associated with a real instruction", instruction);
						    break;
						}
					    }
					}
				    }
				} else recordError("referenced label " + label + " was never defined");
			    }
			}
		    }
		}
	    }
	}
    }


    private void
    startCatch(String exception) {

	ArrayList  data;

	//
	// A null value for exception means we're working on an exception
	// handler that's called for all exceptions.
	//

	data = (ArrayList)trystack.get(trystack.size() - 1);
	if (data.size() == 2)
	    data.set(1, new Integer(nextinstruction));
	data.add(new Integer(nextinstruction));
	data.add(exception);
    }


    private void
    startTry() {

	ArrayList  data;

	data = new ArrayList();
	data.add(new Integer(nextinstruction));
	data.add(new Integer(-1));
	trystack.add(data);
	intry = true;
    }
}

