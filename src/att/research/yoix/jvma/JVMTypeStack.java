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
class JVMTypeStack

    implements JVMConstants,
	       JVMPatterns

{

    //
    // A class used to keep track of the type of the objects on the JVM's
    // operand stack while we're assembling a method.
    //
    // NOTE - there's currently no checking for inconsistencies that might
    // arise between a JVM opcode and the typestack, but we eventually may
    // add some checking and tell the assembler (via recordError()) when
    // we find a mistake. Until then assembler probably won't be used.
    //

    private JVMClassFile  classfile;
    private JVMAssembler  assembler;
    private JVMMethod     method;

    //
    // We remember the types of the objects on the operand stack by storing
    // their descriptor strings in typestack. Having type information means
    // the assembly language that we accept can be simplified because many
    // of the type related decisions can be left to the assembler. Equally
    // important is the fact that the maximum size of the operand stack for
    // each "instruction" can be determined here and used to calculate the
    // max_stack value for the method that we're working on. In case you're
    // wondering, one of our assembly language "instructions" can generate
    // more than one JVM opcode, so determining an instruction's max_stack
    // value means more work than you might initially think.
    //

    private String  typestack[];
    private int     nexttype;
    private int     stacksize;
    private int     max_stack;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    JVMTypeStack(JVMClassFile classfile, JVMMethod method, JVMAssembler assembler) {

	this.classfile = classfile;
	this.method = method;
	this.assembler = assembler;
	typestack = null;
	stacksize = 0;
	max_stack = 0;
    }

    ///////////////////////////////////
    //
    // TypeStack Methods
    //
    ///////////////////////////////////

    int
    getMaxStack() {

	return(max_stack);
    }


    String[]
    getStack() {

	String  stack[];

	if (typestack != null && nexttype > 0) {
	    stack = new String[nexttype];
	    System.arraycopy(typestack, 0, stack, 0, nexttype);
	} else stack = null;

	return(stack);
    }


    int
    getStackHeight() {

	return(nexttype > 0 ? nexttype - 1 : -1);
    }


    int
    peekAtCategory() {

	String  descriptor;
	int     category = 0;

	if (nexttype > 0) {
	    if ((descriptor = typestack[nexttype - 1]) != null)
		category = JVMDescriptor.getDescriptorCategory(descriptor);
	}
	return(category);
    }


    int
    peekAtCategory(int index) {

	String  descriptor;
	int     category = 0;

	if (nexttype > index) {
	    if ((descriptor = typestack[nexttype - 1 - index]) != null)
		category = JVMDescriptor.getDescriptorCategory(descriptor);
	}
	return(category);
    }


    String
    peekAtDescriptor() {

	return(nexttype > 0 ? typestack[nexttype - 1] : null);
    }


    String
    peekAtDescriptor(int index) {

	return(nexttype > index ? typestack[nexttype - 1 - index] : null);
    }


    int
    peekAtDescriptorCode() {

	String  descriptor;
	int     code = -1;

	if (nexttype > 0) {
	    if ((descriptor = typestack[nexttype - 1]) != null)
		code = descriptor.charAt(0);
	}
	return(code);
    }


    int
    peekAtDescriptorCode(int index) {

	String  descriptor;
	int     code = -1;

	if (nexttype > index) {
	    if ((descriptor = typestack[nexttype - 1 - index]) != null)
		code = descriptor.charAt(0);
	}
	return(code);
    }


    void
    setStack(String stack[]) {

	//
	// Explicitly setting the typestack using this method always resets
	// stacksize and max_stack. The assumption is that we'll call this
	// method before assembling every JVMInstruction.
	//

	if (stack != null) {
	    typestack = (String[])stack.clone();
	    nexttype = stack.length;
	    stacksize = JVMDescriptor.getDescriptorSize(typestack);
	    max_stack = stacksize;
	} else {
	    typestack = null;
	    nexttype = 0;
	    stacksize = 0;
	    max_stack = 0;
	}
    }


    public String
    toString() {

	StringBuffer  sbuf = new StringBuffer();
	int           n;

	if (nexttype > 0) {
	    for (n = nexttype - 1; n >= 0; n--) {
		JVMMisc.appendRightAlignedInt(sbuf, n, 5, ": ");
		sbuf.append(typestack[n]);
		sbuf.append("\n");
	    }
	}

	return(sbuf.length() > 0 ? sbuf.toString() : "Stack is empty");
    }


    void
    execute(int opcode, int args[]) {

	String  descriptor;
	String  value1;
	String  value2;
	String  value3;
	String  value4;

	//
	// Updates the type stack assuming an opcode instruction was just
	// successfully executed by the JVM. For now we use a brute force
	// approach, but there are lots of way to reduce overhead!!
	//
	// NOTE - we eventually could add code that makes sure everything
	// is OK for many of the opcodes.
	//

	switch (opcode) {
	    case OP_ACONST_NULL:
		push(NULL_DESCRIPTOR);
		break;

	    case OP_DCONST_0:
	    case OP_DCONST_1:
		push(DOUBLE_DESCRIPTOR);
		break;

	    case OP_FCONST_0:
	    case OP_FCONST_1:
	    case OP_FCONST_2:
		push(FLOAT_DESCRIPTOR);
		break;

	    case OP_ICONST_0:
	    case OP_ICONST_1:
	    case OP_ICONST_2:
	    case OP_ICONST_3:
	    case OP_ICONST_4:
	    case OP_ICONST_5:
	    case OP_ICONST_M1:
		push(INT_DESCRIPTOR);
		break;

	    case OP_LCONST_0:
	    case OP_LCONST_1:
		push(LONG_DESCRIPTOR);
		break;

	    case OP_AALOAD:
	    case OP_DALOAD:
	    case OP_FALOAD:
	    case OP_IALOAD:
	    case OP_LALOAD:
		descriptor = JVMDescriptor.getDescriptorForArrayElement(peekAtDescriptor(1));
		pop(2);
		push(descriptor);
		break;

	    case OP_BALOAD:
	    case OP_CALOAD:
	    case OP_SALOAD:
		pop(2);
		push(INT_DESCRIPTOR);
		break;

	    case OP_ALOAD:
	    case OP_DLOAD:
	    case OP_FLOAD:
	    case OP_ILOAD:
	    case OP_LLOAD:
		push(method.getLocalVariableDescriptor(args[0]));
		break;

	    case OP_ALOAD_0:
	    case OP_DLOAD_0:
	    case OP_FLOAD_0:
	    case OP_ILOAD_0:
	    case OP_LLOAD_0:
		push(method.getLocalVariableDescriptor(0));
		break;

	    case OP_ALOAD_1:
	    case OP_DLOAD_1:
	    case OP_FLOAD_1:
	    case OP_ILOAD_1:
	    case OP_LLOAD_1:
		push(method.getLocalVariableDescriptor(1));
		break;

	    case OP_ALOAD_2:
	    case OP_DLOAD_2:
	    case OP_FLOAD_2:
	    case OP_ILOAD_2:
	    case OP_LLOAD_2:
		push(method.getLocalVariableDescriptor(2));
		break;

	    case OP_ALOAD_3:
	    case OP_DLOAD_3:
	    case OP_FLOAD_3:
	    case OP_ILOAD_3:
	    case OP_LLOAD_3:
		push(method.getLocalVariableDescriptor(3));
		break;

	    case OP_BIPUSH:
	    case OP_SIPUSH:
		push(INT_DESCRIPTOR);
		break;

	    case OP_LDC:
	    case OP_LDC2_W:
	    case OP_LDC_W:
		push(classfile.getDescriptor(args[0]));
		break;

	    case OP_GETFIELD:
		pop();
		push(classfile.getFieldDescriptor(args[0]));
		break;

	    case OP_GETSTATIC:
		push(classfile.getFieldDescriptor(args[0]));
		break;

	    case OP_NEW:
		push(classfile.getDescriptor(args[0]));
		break;

	    case OP_NEWARRAY:
		pop();
		push(JVMDescriptor.getDescriptorForArray(args[0]));
		break;

	    case OP_ANEWARRAY:
		pop();
		push(JVMDescriptor.getDescriptorForArrayByElement(classfile.getDescriptor(args[0])));
		break;

	    case OP_MULTIANEWARRAY:
		pop(args[1]);
		push(classfile.getDescriptor(args[0]));
		break;

	    case OP_ARRAYLENGTH:
		pop();
		push(INT_DESCRIPTOR);
		break;

	    case OP_AASTORE:
	    case OP_BASTORE:
	    case OP_CASTORE:
	    case OP_DASTORE:
	    case OP_FASTORE:
	    case OP_IASTORE:
	    case OP_LASTORE:
	    case OP_SASTORE:
		pop(3);
		break;

	    case OP_ASTORE:
	    case OP_DSTORE:
	    case OP_FSTORE:
	    case OP_ISTORE:
	    case OP_LSTORE:
	    case OP_ASTORE_0:
	    case OP_ASTORE_1:
	    case OP_ASTORE_2:
	    case OP_ASTORE_3:
	    case OP_DSTORE_0:
	    case OP_DSTORE_1:
	    case OP_DSTORE_2:
	    case OP_DSTORE_3:
	    case OP_FSTORE_0:
	    case OP_FSTORE_1:
	    case OP_FSTORE_2:
	    case OP_FSTORE_3:
	    case OP_ISTORE_0:
	    case OP_ISTORE_1:
	    case OP_ISTORE_2:
	    case OP_ISTORE_3:
	    case OP_LSTORE_0:
	    case OP_LSTORE_1:
	    case OP_LSTORE_2:
	    case OP_LSTORE_3:
		pop();
		break;

	    case OP_PUTFIELD:
		pop(2);
		break;

	    case OP_PUTSTATIC:
		pop();
		break;

	    case OP_D2F:
	    case OP_I2F:
	    case OP_L2F:
		pop();
		push(FLOAT_DESCRIPTOR);
		break;

	    case OP_D2I:
	    case OP_F2I:
	    case OP_I2B:
	    case OP_I2C:
	    case OP_I2S:
	    case OP_L2I:
		pop();
		push(INT_DESCRIPTOR);
		break;

	    case OP_D2L:
	    case OP_F2L:
	    case OP_I2L:
		pop();
		push(LONG_DESCRIPTOR);
		break;

	    case OP_F2D:
	    case OP_I2D:
	    case OP_L2D:
		pop();
		push(DOUBLE_DESCRIPTOR);
		break;

	    case OP_FNEG:
	    case OP_INEG:
	    case OP_LNEG:
	    case OP_DNEG:
		break;

	    case OP_DADD:
	    case OP_DDIV:
	    case OP_DMUL:
	    case OP_DREM:
	    case OP_DSUB:
		pop(2);
		push(DOUBLE_DESCRIPTOR);
		break;

	    case OP_FADD:
	    case OP_FDIV:
	    case OP_FMUL:
	    case OP_FREM:
	    case OP_FSUB:
		pop(2);
		push(FLOAT_DESCRIPTOR);
		break;

	    case OP_IADD:
	    case OP_IAND:
	    case OP_IDIV:
	    case OP_IMUL:
	    case OP_IOR:
	    case OP_IREM:
	    case OP_ISHL:
	    case OP_ISHR:
	    case OP_ISUB:
	    case OP_IUSHR:
	    case OP_IXOR:
		pop(2);
		push(INT_DESCRIPTOR);
		break;

	    case OP_IINC:
		break;

	    case OP_LADD:
	    case OP_LAND:
	    case OP_LDIV:
	    case OP_LMUL:
	    case OP_LOR:
	    case OP_LREM:
	    case OP_LSHL:
	    case OP_LSHR:
	    case OP_LSUB:
	    case OP_LUSHR:
	    case OP_LXOR:
		pop(2);
		push(LONG_DESCRIPTOR);
		break;

	    case OP_DCMPG:
	    case OP_DCMPL:
	    case OP_FCMPG:
	    case OP_FCMPL:
	    case OP_LCMP:
		pop(2);
		push(INT_DESCRIPTOR);
		break;

	    case OP_IFEQ:
	    case OP_IFGE:
	    case OP_IFGT:
	    case OP_IFLE:
	    case OP_IFLT:
	    case OP_IFNE:
	    case OP_IFNONNULL:
	    case OP_IFNULL:
		pop();
		break;

	    case OP_IF_ACMPEQ:
	    case OP_IF_ACMPNE:
	    case OP_IF_ICMPEQ:
	    case OP_IF_ICMPGE:
	    case OP_IF_ICMPGT:
	    case OP_IF_ICMPLE:
	    case OP_IF_ICMPLT:
	    case OP_IF_ICMPNE:
		pop(2);
		break;

	    case OP_GOTO:
	    case OP_GOTO_W:
		break;

	    case OP_LOOKUPSWITCH:
	    case OP_TABLESWITCH:
	    case OP_MONITORENTER:
	    case OP_MONITOREXIT:
		pop();
		break;

	    case OP_INVOKESTATIC:
		descriptor = classfile.getMethodDescriptor(args[0]);
		pop(JVMDescriptor.getStorageCount(descriptor));
		descriptor = JVMDescriptor.getDescriptorForMethodReturn(descriptor);
		if (VOID_DESCRIPTOR.equals(descriptor) == false)
		    push(descriptor);
		break;

	    case OP_INVOKEINTERFACE:
	    case OP_INVOKESPECIAL:
	    case OP_INVOKEVIRTUAL:
		descriptor = classfile.getMethodDescriptor(args[0]);
		pop(JVMDescriptor.getStorageCount(descriptor) + 1);
		descriptor = JVMDescriptor.getDescriptorForMethodReturn(descriptor);
		if (VOID_DESCRIPTOR.equals(descriptor) == false)
		    push(descriptor);
		break;

	    case OP_ARETURN:
	    case OP_DRETURN:
	    case OP_FRETURN:
	    case OP_IRETURN:
	    case OP_LRETURN:
		//
		// Empty the typestack??
		//
		pop();
		break;

	    case OP_RETURN:
		//
		// Empty the typestack??
		//
		break;

	    case OP_JSR:
	    case OP_JSR_W:
		push(RETURNADDRESS_DESCRIPTOR);
		break;

	    case OP_RET:
		break;

	    case OP_ATHROW:
		//
		// Stack is cleared...
		//
		break;

	    case OP_INSTANCEOF:
		pop();
		push(INT_DESCRIPTOR);
		break;

	    case OP_CHECKCAST:
		//
		// We undoubtedly could do some error checking here.
		//
		descriptor = classfile.getDescriptorFor(args[0], CONSTANT_CLASS);
		pop();
		push(descriptor);
		break;

	    case OP_POP:
		pop();
		break;

	    case OP_POP2:
		pop(peekAtCategory() == 2 ? 1 : 2);
		break;

	    case OP_SWAP:
		swap();
		break;

	    case OP_DUP:
		push(peekAtDescriptor());
		break;

	    case OP_DUP2:
		if (peekAtCategory() != 2) {
		    push(peekAtDescriptor(1));
		    push(peekAtDescriptor(1));
		} else push(peekAtDescriptor());
		break;

	    case OP_DUP2_X1:
		if (peekAtCategory() == 2) {
		    value1 = pop();
		    value2 = pop();
		    push(value1);
		    push(value2);
		    push(value1);
		} else {
		    value1 = pop();
		    value2 = pop();
		    value3 = pop();
		    push(value2);
		    push(value1);
		    push(value3);
		    push(value2);
		    push(value1);
		}
		break;

	    case OP_DUP2_X2:
		if (peekAtCategory(0) == 2) {
		    if (peekAtCategory(1) == 2) {
			value1 = pop();
			value2 = pop();
			push(value1);
			push(value2);
			push(value1);
		    } else {
			value1 = pop();
			value2 = pop();
			value3 = pop();
			push(value1);
			push(value3);
			push(value2);
			push(value1);
		    }
		} else if (peekAtCategory(2) == 2) {
		    value1 = pop();
		    value2 = pop();
		    value3 = pop();
		    push(value2);
		    push(value1);
		    push(value3);
		    push(value2);
		    push(value1);
		} else {
		    value1 = pop();
		    value2 = pop();
		    value3 = pop();
		    value4 = pop();
		    push(value2);
		    push(value1);
		    push(value4);
		    push(value3);
		    push(value2);
		    push(value1);
		}
		break;

	    case OP_DUP_X1:
		value1 = pop();
		value2 = pop();
		push(value1);
		push(value2);
		push(value1);
		break;

	    case OP_DUP_X2:
		if (peekAtCategory(1) == 2) {
		    value1 = pop();
		    value2 = pop();
		    push(value1);
		    push(value2);
		    push(value1);
		} else {
		    value1 = pop();
		    value2 = pop();
		    value3 = pop();
		    push(value1);
		    push(value3);
		    push(value2);
		    push(value1);
		}
		break;

	    case OP_NOP:
		break;

	    case OP_WIDE:
		switch (args[0]) {
		    case OP_ALOAD:
		    case OP_DLOAD:
		    case OP_FLOAD:
		    case OP_ILOAD:
		    case OP_LLOAD:
			push(method.getLocalVariableDescriptor(args[1]));
			break;

		    case OP_ASTORE:
		    case OP_DSTORE:
		    case OP_FSTORE:
		    case OP_ISTORE:
		    case OP_LSTORE:
			pop();
			break;

		    case OP_RET:
		    case OP_IINC:
			break;
		}
		break;

	    default:
		//
		// Looks like we missed something.
		//
		break;
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    clear() {

	nexttype = 0;
	stacksize = 0;
    }


    private void
    ensureCapacity(int count) {

	String  tmp[];
	int     length;

	if (typestack != null) {
	    if (nexttype + count > typestack.length) {
		length = typestack.length + count;
		tmp = new String[length];
		System.arraycopy(typestack, 0, tmp, 0, typestack.length);
		typestack = tmp;
	    }
	} else {
	    typestack = new String[count];
	    nexttype = 0;
	}
    }


    private String
    pop() {

	String  descriptor;

	descriptor = (nexttype > 0) ? typestack[--nexttype] : null;
	updateOperandStack(descriptor, -1);
	return(descriptor);
    }


    private void
    pop(int count) {

	for (; count > 0; count--)
	    pop();
    }


    private void
    push(String descriptor) {

	int  size;

	ensureCapacity(1);
	typestack[nexttype++] = descriptor;
	updateOperandStack(descriptor, 1);
    }


    private void
    swap() {

	String  tmp;

	if (nexttype > 1) {
	    tmp = typestack[nexttype - 2];
	    typestack[nexttype - 2] = typestack[nexttype - 1];
	    typestack[nexttype - 1] = tmp;
	}
    }


    private void
    updateOperandStack(String descriptor, int direction) {

	int  size;

	if (descriptor != null) {
	    size = direction*JVMDescriptor.getDescriptorSize(descriptor);
	    if ((stacksize += size) > max_stack)
		max_stack = stacksize;
	}
    }
}

