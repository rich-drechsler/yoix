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

public abstract
class JVMDisassembler

    implements JVMConstants

{

    //
    // A simple JVM bytecode disassembler that's mostly for debugging and
    // development.
    //

    ///////////////////////////////////
    //
    // JVMDisassembler Methods
    //
    ///////////////////////////////////

    public static String
    disassemble(byte bytecode[]) {

	StringBuffer  sbuf = new StringBuffer();

	disassembleInto(bytecode, 0, bytecode != null ? bytecode.length : 0, "", sbuf, null);
	return(sbuf.length() > 0 ? sbuf.toString() : null);
    }


    public static String
    disassemble(byte bytecode[], JVMClassFile classfile) {

	StringBuffer  sbuf = new StringBuffer();

	disassembleInto(bytecode, 0, bytecode != null ? bytecode.length : 0, "", sbuf, classfile);
	return(sbuf.length() > 0 ? sbuf.toString() : null);
    }


    public static void
    disassembleInto(byte bytecode[], StringBuffer sbuf) {

	disassembleInto(bytecode, 0, bytecode != null ? bytecode.length : 0, "", sbuf, null);
    }


    public static void
    disassembleInto(byte bytecode[], String indent, StringBuffer sbuf) {

	disassembleInto(bytecode, 0, bytecode != null ? bytecode.length : 0, indent, sbuf, null);
    }


    public static void
    disassembleInto(byte bytecode[], int offset, int length, String indent, StringBuffer sbuf) {

	disassembleInto(bytecode, offset, length, indent, sbuf, null);
    }


    public static void
    disassembleInto(byte bytecode[], int offset, int length, String indent, StringBuffer sbuf, JVMClassFile classfile) {

	String  constant;
	String  typename;
	int     pc;
	int     argc;
	int     opcode;
	int     wideop;
	int     pad;
	int     count;
	int     low;
	int     high;
	int     index;
	int     pool_index;
	int     n;

	if (bytecode != null && length > 0) {
	    for (pc = 0, index = offset; pc < length; pc += argc + 1, index += argc + 1) {
		pool_index = 0;
		sbuf.append(indent);
		JVMMisc.appendRightAlignedInt(sbuf, pc, 6, ": ");
		switch (opcode = bytecode[index]&0xFF) {
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
			argc = 0;
			sbuf.append(OPCODE_MNEMONICS[opcode]);
			break;

		    //
		    // These opcodes use the byte argument to index into the local
		    // variable array.
		    //
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
			argc = 1;
			sbuf.append(OPCODE_MNEMONICS[opcode]);
			sbuf.append(" ");
			sbuf.append(bytecode[index + 1]&0xFF);
			break;

		    //
		    // This opcode uses the byte argument as immediate data that's
		    // sign extended to an int and pushed onto the operand stack.
		    //
		    case OP_BIPUSH:
			argc = 1;
			sbuf.append(OPCODE_MNEMONICS[opcode]);
			sbuf.append(" ");
			sbuf.append(bytecode[index + 1]);
			break;

		    //
		    // This opcode uses the byte argument to determine the type of
		    // the new array. Its size is determined by the int on top of
		    // the operand stack.
		    //
		    case OP_NEWARRAY:
			argc = 1;
			sbuf.append(OPCODE_MNEMONICS[opcode]);
			sbuf.append(" ");
			if ((typename = JVMScanner.getNewArrayTypeName(bytecode[index + 1] & 0xFF)) != null)
			    sbuf.append(typename);
			else sbuf.append(bytecode[index + 1]);
			break;

		    //
		    // This opcode uses the unsigned byte argument as an index into
		    // the constant pool.
		    //
		    case OP_LDC:
			argc = 1;
			pool_index = bytecode[index + 1]&0xFF;
			sbuf.append(OPCODE_MNEMONICS[opcode]);
			sbuf.append(" #");
			sbuf.append(pool_index);
			break;

		    //
		    // These opcodes combine the two unsigned byte arguments into an
		    // int that's used as a constant pool index.
		    //
		    case OP_ANEWARRAY:
		    case OP_CHECKCAST:
		    case OP_GETFIELD:
		    case OP_GETSTATIC:
		    case OP_INVOKESPECIAL:
		    case OP_INVOKESTATIC:
		    case OP_INVOKEVIRTUAL:
		    case OP_INSTANCEOF:
		    case OP_LDC2_W:
		    case OP_LDC_W:
		    case OP_NEW:
		    case OP_PUTFIELD:
		    case OP_PUTSTATIC:
			argc = 2;
			pool_index = JVMMisc.getUnsignedShort(bytecode, index + 1);
			sbuf.append(OPCODE_MNEMONICS[opcode]);
			sbuf.append(" #");
			sbuf.append(pool_index);
			break;

		    //
		    // These opcodes combine the two byte arguments into a signed 16
		    // bit number that's added to the address of this opcode to get
		    // the address of the next instruction.
		    //
		    case OP_GOTO:
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
			argc = 2;
			sbuf.append(OPCODE_MNEMONICS[opcode]);
			sbuf.append(" ");
			sbuf.append(pc + JVMMisc.getShort(bytecode, index + 1));
			break;

		    //
		    // The first byte is an index into the local variable array. The
		    // second byte is immediate data that's sign extended to an int
		    // and used to increment the local variable.
		    //
		    case OP_IINC:
			argc = 2;
			sbuf.append(OPCODE_MNEMONICS[opcode]);
			sbuf.append(" ");
			sbuf.append(bytecode[index + 1]&0xFF);
			sbuf.append(" ");
			sbuf.append(bytecode[index + 2]);
			break;

		    //
		    // This opcode combines the two bytes into a short which is sign
		    // extended to an int and the result is pushed onto the operand
		    // stack.
		    //
		    case OP_SIPUSH:
			argc = 2;
			sbuf.append(OPCODE_MNEMONICS[opcode]);
			sbuf.append(" ");
			sbuf.append(JVMMisc.getShort(bytecode, index + 1));
			break;

		    //
		    // The first two bytes are combined into a constant pool index.
		    // The third byte is an unsigned byte (greater that zero) that
		    // specifies the number of dimensions in the new array.
		    //
		    case OP_MULTIANEWARRAY:
			argc = 3;
			pool_index = JVMMisc.getUnsignedShort(bytecode, index + 1);
			sbuf.append(OPCODE_MNEMONICS[opcode]);
			sbuf.append(" #");
			sbuf.append(pool_index);
			sbuf.append(" ");
			sbuf.append(bytecode[index + 3]&0xFF);
			break;

		    //
		    // The four bytes are combined into a 32 bit signed int that's
		    // added to the address of this opcode to get the address of
		    // the next instruction.
		    //
		    case OP_GOTO_W:
		    case OP_JSR_W:
			argc = 4;
			sbuf.append(OPCODE_MNEMONICS[opcode]);
			sbuf.append(" ");
			sbuf.append(pc + JVMMisc.getInt(bytecode, index + 1));
			break;

		    //
		    // The first two bytes are combined into a constant pool index.
		    // The third byte is an unsigned byte that (I think) records how
		    // much space (i.e., how many ints) are needed for the arguments,
		    // where each long or double argument adds 2 to count.
		    // 
		    case OP_INVOKEINTERFACE:
			argc = 4;
			pool_index = JVMMisc.getUnsignedShort(bytecode, index + 1);
			sbuf.append(OPCODE_MNEMONICS[opcode]);
			sbuf.append(" #");
			sbuf.append(pool_index);
			sbuf.append(" ");
			sbuf.append(bytecode[index + 3]&0xFF);
			break;

		    //
		    // Used to implement switch statements when there are gaps in
		    // the case expressions.
		    //
		    case OP_LOOKUPSWITCH:
			pad = (3 - pc%4);
			count = JVMMisc.getInt(bytecode, index + 1 + pad + 4);
			argc = 8*(count + 1) + pad;
			sbuf.append(OPCODE_MNEMONICS[opcode]);
			sbuf.append(" {\n");
			for (n = 0; n < count; n++) {
			    sbuf.append(indent);
			    sbuf.append("        ");
			    sbuf.append("    ");
			    sbuf.append(JVMMisc.getInt(bytecode, index + 1 + pad + 8*(n + 1)));
			    sbuf.append(": ");
			    sbuf.append(pc + JVMMisc.getInt(bytecode, index + 1 + pad + 8*(n + 1) + 4));
			    sbuf.append("\n");
			}
			sbuf.append(indent);
			sbuf.append("        ");
			sbuf.append("    ");
			sbuf.append("default: ");
			sbuf.append(pc + JVMMisc.getInt(bytecode, index + 1 + pad));
			sbuf.append("\n");
			sbuf.append(indent);
			sbuf.append("        ");
			sbuf.append("}");
			break;

		    //
		    // Used to implement switch statements when there are no gaps
		    // in the case expressions and there are more than 2 of them.
		    //
		    case OP_TABLESWITCH:
			pad = (3 - pc%4);
			low = JVMMisc.getInt(bytecode, index + 1 + pad + 4);
			high = JVMMisc.getInt(bytecode, index + 1 + pad + 8);
			count = high - low + 1;
			argc = 12 + pad + 4*count;
			sbuf.append(OPCODE_MNEMONICS[opcode]);
			sbuf.append(" {\n");
			for (n = 0; n < count; n++) {
			    sbuf.append(indent);
			    sbuf.append("        ");
			    sbuf.append("    ");
			    sbuf.append(low + n);
			    sbuf.append(": ");
			    sbuf.append(pc + JVMMisc.getInt(bytecode, index + 1 + pad + 12 + 4*n));
			    sbuf.append("\n");
			}
			sbuf.append(indent);
			sbuf.append("        ");
			sbuf.append("    ");
			sbuf.append("default: ");
			sbuf.append(pc + JVMMisc.getInt(bytecode, index + 1 + pad));
			sbuf.append("\n");
			sbuf.append(indent);
			sbuf.append("        ");
			sbuf.append("}");
			break;

		    //
		    // Variable length instruction that allows opcodes that access
		    // local variables to get at more than 256 of them.
		    //
		    case OP_WIDE:
			wideop = bytecode[index + 1]&0xFF;
			sbuf.append(OPCODE_MNEMONICS[opcode]);
			sbuf.append(" ");
			sbuf.append(OPCODE_MNEMONICS[wideop]);
			sbuf.append(" ");
			if (wideop == OP_IINC) {
			    argc = 5;
			    sbuf.append(JVMMisc.getUnsignedShort(bytecode, index + 2));
			    sbuf.append(" ");
			    sbuf.append(JVMMisc.getShort(bytecode, index + 4));
			} else {
			    argc = 3;
			    sbuf.append(JVMMisc.getUnsignedShort(bytecode, index + 2));
			}
			break;

		    default:
			argc = 0;
			break;
		}
		if (pool_index > 0 && classfile != null) {
		    if ((constant = classfile.dumpConstantPoolConstant(pool_index)) != null && constant.length() > 0) {
			sbuf.append("    // ");
			sbuf.append(constant);
		    }
		}
		sbuf.append("\n");
	    }
	}
    }
}

