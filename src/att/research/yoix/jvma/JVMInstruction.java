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
import java.io.*;
import java.util.*;
import java.util.regex.*;

public
class JVMInstruction

    implements JVMConstants,
	       JVMPatterns

{

    //
    // An important class that's supposed to represent a single "instruction"
    // that was encountered in an "assembly language source file", so several
    // JVM instructions can be associated with each instance of this class.
    // 

    private String  line;
    private int     linenumber;
    private int     pc;
    private int     opcode;
    private String  stack[];
    private Object  extra;
    private int     args[];
    private int     codemodel;

    //
    // The bytecode for each assembled "instruction" is temporarily stored
    // in the bytecode array where it can be retrived later when we collect
    // the JVM instructions that implement a method. At any point time the
    // stored bytecode for an instruction may be incomplete (e.g., switch
    // opcodes) or may include branch offsets that still must be resolved.
    //

    private byte  bytecode[];

    //
    // The maximum operand stack size for each "instruction" is calculated
    // as the instruction is assembled and the result is saved in max_stack.
    //

    private int   max_stack;

    //
    // The code stored in bytecode[] can be preceeded by opcodes that were
    // added to handle things like casting, but branch offsets that have to
    // be resolved when the bytecode for a method is built must be relative
    // to the branching instruction address. The value stored in offset is
    // the index in bytecode where the actual JVM instruction represented
    // by this "instruction" can be found.
    //

    private int  offset;

    //
    // If this happens to be the last "official" instruction but we added a
    // return after it then a label that followed this instruction could be
    // considered valid if we point it at that return.
    //

    private int  return_offset;

    //
    // We use these booleans to mark the validity and reachability of this
    // "instruction".
    //

    private boolean  valid;
    private boolean  visited;

    //
    // A list of the possible successors of the current instruction.
    //

    private ArrayList  successors;
    private ArrayList  predecessors;

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    JVMInstruction(String line, int linenumber, int opcode, int codemodel) {

	buildInstruction(line, linenumber, -1, opcode, codemodel, null, null, null);
    }


    JVMInstruction(String line, int linenumber, int opcode, int codemodel, int args[]) {

	buildInstruction(line, linenumber, -1, opcode, codemodel, args, null, null);
    }


    JVMInstruction(String line, int linenumber, int opcode, int codemodel, int args[], Object extra) {

	buildInstruction(line, linenumber, -1, opcode, codemodel, args, extra, null);
    }

    ///////////////////////////////////
    //
    // JVMInstruction Methods
    //
    ///////////////////////////////////

    void
    addPredecessor(JVMInstruction instruction) {

	if (instruction != null) {
	    if (predecessors == null) {
		predecessors = new ArrayList();
		predecessors.add(instruction);
	    } else if (predecessors.contains(instruction) == false)
		predecessors.add(instruction);
	}
    }


    void
    addSuccessor(JVMInstruction instruction) {

	if (instruction != null) {
	    if (successors == null) {
		successors = new ArrayList();
		successors.add(instruction);
	    } else if (successors.contains(instruction) == false)
		successors.add(instruction);
	}
    }


    String
    dumpPredecessors() {

	JVMInstruction  instruction;
	StringBuffer    sbuf = null;
	String          sep = "";
	int             n;

	//
	// A debugging method.
	//

	if (predecessors != null) {
	    sbuf = new StringBuffer();
	    for (n = 0; n < predecessors.size(); n++) {
		if ((instruction = (JVMInstruction)predecessors.get(n)) != null) {
		    sbuf.append(sep);
		    sbuf.append(instruction.toString());
		    sep = ", ";
		}
	    }
	}

	return(sbuf != null ? sbuf.toString() : null);
    }


    String
    dumpSuccessors() {

	JVMInstruction  instruction;
	StringBuffer    sbuf = null;
	String          sep = "";
	int             n;

	//
	// A debugging method.
	//

	if (successors != null) {
	    sbuf = new StringBuffer();
	    for (n = 0; n < successors.size(); n++) {
		if ((instruction = (JVMInstruction)successors.get(n)) != null) {
		    sbuf.append(sep);
		    sbuf.append(instruction.toString());
		    sep = ", ";
		}
	    }
	}

	return(sbuf != null ? sbuf.toString() : null);
    }


    int
    getAddress() {

	return(pc + offset);
    }


    int[]
    getArgs() {

	return(args);
    }


    byte[]
    getByteCode() {

	return(bytecode);
    }


    int
    getByteCodeSize() {

	Object  table[];
	int     size = 0;
	int     address;
	int     pad;

	//
	// The padding that has to be added to switch statements means our
	// answer won't be exact (unless pc and offset have been been set),
	// but it should always be an upper bound.
	//

	if (valid && bytecode != null) {
	    switch (opcode) {
		case OP_LOOKUPSWITCH:
		case OP_TABLESWITCH:
		case OP_SWITCH:
		    if (extra instanceof Object[]) {
			table = (Object[])extra;
			pad = ((address = getAddress()) >= 0) ? 3 - address%4 : 3;
			switch (opcode) {
			    case OP_LOOKUPSWITCH:
				size = bytecode.length + 4*table.length + pad;
				break;

			    case OP_TABLESWITCH:
				size = bytecode.length + 4*(3 + (table.length - 2)/2) + pad;
				break;

			    case OP_SWITCH:
				if (((Number)table[0]).intValue() != 0 && table.length > 2)
				    size = bytecode.length + 4*(3 + (table.length - 2)/2) + pad;
				else size = bytecode.length + 4*table.length + pad;
				break;
			}
		    }
		    break;

		default:
		    size = bytecode.length;
		    break;
	    }
	}

	return(size);
    }


    int
    getCodeModel() {

	return(codemodel);
    }


    int
    getExceptionHandlerStart() {

	return(pc);
    }


    int
    getExceptionHandlerEnd() {

	return(pc + getByteCodeSize());
    }


    Object
    getExtra() {

	return(extra);
    }


    String
    getLine() {

	return(line);
    }


    int
    getLineNumber() {

	return(linenumber);
    }


    int
    getMaxStack() {

	return(max_stack);
    }


    int
    getOpcode() {

	return(opcode);
    }


    int
    getPC() {

	return(pc);
    }


    Iterator
    getPredecessors() {

	return(predecessors != null ? predecessors.iterator() : null);
    }


    int
    getOffset() {

	return(offset);
    }


    int
    getReturnOffset() {

	return(return_offset);
    }


    String[]
    getStack() {

	return(stack != null ? (String[])stack.clone() : null);
    }


    String[]
    getStartStack() {

	JVMInstruction  instruction;
	Iterator        iterator;
	String          startstack[] = null;

	if ((iterator = getPredecessors()) != null) {
	    while (iterator.hasNext()) {
		if ((instruction = (JVMInstruction)iterator.next()) != null) {
		    if (instruction.getVisited()) {
			startstack = instruction.getStack();
			break;
		    }
		}
	    }
	}

	return(startstack);
    }


    Iterator
    getSuccessors() {

	return(successors != null ? successors.iterator() : null);
    }


    boolean
    getVisited() {

	return(visited);
    }


    void
    invalidate() {

	valid = false;
    }


    boolean
    isLeaf() {

	boolean  result;

	if (successors != null) {
	    switch (opcode) {
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
		    result = (successors.size() <= 1);
		    break;

		default:
		    result = (successors.size() == 0);
		    break;
	    }
	} else result = true;

	return(result);
    }


    boolean
    isValid() {

	return(valid);
    }


    void
    setByteCode(byte bytecode[]) {

	this.bytecode = (bytecode != null) ? (byte[])bytecode.clone() : null;
    }


    void
    setByteCode(byte bytecode[], int length) {

	byte  tmp[];

	if (bytecode != null && bytecode.length > 0 && length > 0) {
	    tmp = new byte[length];
	    System.arraycopy(bytecode, 0, tmp, 0, length);
	    this.bytecode = tmp;
	} else this.bytecode = null;
    }


    void
    setCodeModel(int model) {

	codemodel = model;
    }


    void
    setMaxStack(int size) {

	max_stack = size;
    }


    void
    setOffset(int offset) {

	this.offset = offset;
    }


    void
    setOpcode(int opcode) {

	this.opcode = opcode;
    }


    void
    setPC(int pc) {

	this.pc = pc;
    }


    void
    setReturnOffset(int value) {

	return_offset = value;
    }


    void
    setStack(String stack[]) {

	if (stack != null)
	    this.stack = (String[])stack.clone();
	else this.stack = null;
    }


    void
    setVisited(boolean state) {

	visited = state;
    }


    public String
    toString() {

	StringBuffer  sbuf;
	String        nmemonic;
	int           n;

	sbuf = new StringBuffer();
	if (opcode >= 0 && opcode < OPCODE_MNEMONICS.length) {
	    if ((nmemonic = OPCODE_MNEMONICS[opcode]) != null)
		sbuf.append(OPCODE_MNEMONICS[opcode]);
	    else sbuf.append(opcode);
	} else sbuf.append(opcode);

	sbuf.append(", stack=|");
	if (stack != null) {
	    for (n = 0; n < stack.length; n++)
		sbuf.append(stack[n]);
	}
	sbuf.append("|");

	sbuf.append(", valid=");
	sbuf.append(valid);

	sbuf.append(", visited=");
	sbuf.append(visited);

	return(sbuf.toString());
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildInstruction(String line, int linenumber, int pc, int opcode, int codemodel, int args[], Object extra, String stack[]) {

	this.line = line;
	this.linenumber = linenumber;
	this.pc = pc;
	this.opcode = opcode;
	this.codemodel = codemodel;
	this.args = args;
	this.extra = extra;
	this.stack = stack;

	bytecode = null;
	offset = 0;
	visited = false;
	valid = true;
	return_offset = -1;
    }
}
