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

public
interface JVMOpcodes {

    //
    // This class contains the definitions of opcodes and the corresponding
    // mnemonics that are recognized in assembly language source files. The
    // string definitions that start with NAME_ are opcode mnemonics that
    // correspond to the integer constants that begin with the OP_ prefix.
    //

    public static final String  NAME_NOP = "nop";
    public static final String  NAME_ACONST_NULL = "aconst_null";
    public static final String  NAME_ICONST_M1 = "iconst_m1";
    public static final String  NAME_ICONST_0 = "iconst_0";
    public static final String  NAME_ICONST_1 = "iconst_1";
    public static final String  NAME_ICONST_2 = "iconst_2";
    public static final String  NAME_ICONST_3 = "iconst_3";
    public static final String  NAME_ICONST_4 = "iconst_4";
    public static final String  NAME_ICONST_5 = "iconst_5";
    public static final String  NAME_LCONST_0 = "lconst_0";
    public static final String  NAME_LCONST_1 = "lconst_1";
    public static final String  NAME_FCONST_0 = "fconst_0";
    public static final String  NAME_FCONST_1 = "fconst_1";
    public static final String  NAME_FCONST_2 = "fconst_2";
    public static final String  NAME_DCONST_0 = "dconst_0";
    public static final String  NAME_DCONST_1 = "dconst_1";
    public static final String  NAME_BIPUSH = "bipush";
    public static final String  NAME_SIPUSH = "sipush";
    public static final String  NAME_LDC = "ldc";
    public static final String  NAME_LDC_W = "ldc_w";
    public static final String  NAME_LDC2_W = "ldc2_w";
    public static final String  NAME_ILOAD = "iload";
    public static final String  NAME_LLOAD = "lload";
    public static final String  NAME_FLOAD = "fload";
    public static final String  NAME_DLOAD = "dload";
    public static final String  NAME_ALOAD = "aload";
    public static final String  NAME_ILOAD_0 = "iload_0";
    public static final String  NAME_ILOAD_1 = "iload_1";
    public static final String  NAME_ILOAD_2 = "iload_2";
    public static final String  NAME_ILOAD_3 = "iload_3";
    public static final String  NAME_LLOAD_0 = "lload_0";
    public static final String  NAME_LLOAD_1 = "lload_1";
    public static final String  NAME_LLOAD_2 = "lload_2";
    public static final String  NAME_LLOAD_3 = "lload_3";
    public static final String  NAME_FLOAD_0 = "fload_0";
    public static final String  NAME_FLOAD_1 = "fload_1";
    public static final String  NAME_FLOAD_2 = "fload_2";
    public static final String  NAME_FLOAD_3 = "fload_3";
    public static final String  NAME_DLOAD_0 = "dload_0";
    public static final String  NAME_DLOAD_1 = "dload_1";
    public static final String  NAME_DLOAD_2 = "dload_2";
    public static final String  NAME_DLOAD_3 = "dload_3";
    public static final String  NAME_ALOAD_0 = "aload_0";
    public static final String  NAME_ALOAD_1 = "aload_1";
    public static final String  NAME_ALOAD_2 = "aload_2";
    public static final String  NAME_ALOAD_3 = "aload_3";
    public static final String  NAME_IALOAD = "iaload";
    public static final String  NAME_LALOAD = "laload";
    public static final String  NAME_FALOAD = "faload";
    public static final String  NAME_DALOAD = "daload";
    public static final String  NAME_AALOAD = "aaload";
    public static final String  NAME_BALOAD = "baload";
    public static final String  NAME_CALOAD = "caload";
    public static final String  NAME_SALOAD = "saload";
    public static final String  NAME_ISTORE = "istore";
    public static final String  NAME_LSTORE = "lstore";
    public static final String  NAME_FSTORE = "fstore";
    public static final String  NAME_DSTORE = "dstore";
    public static final String  NAME_ASTORE = "astore";
    public static final String  NAME_ISTORE_0 = "istore_0";
    public static final String  NAME_ISTORE_1 = "istore_1";
    public static final String  NAME_ISTORE_2 = "istore_2";
    public static final String  NAME_ISTORE_3 = "istore_3";
    public static final String  NAME_LSTORE_0 = "lstore_0";
    public static final String  NAME_LSTORE_1 = "lstore_1";
    public static final String  NAME_LSTORE_2 = "lstore_2";
    public static final String  NAME_LSTORE_3 = "lstore_3";
    public static final String  NAME_FSTORE_0 = "fstore_0";
    public static final String  NAME_FSTORE_1 = "fstore_1";
    public static final String  NAME_FSTORE_2 = "fstore_2";
    public static final String  NAME_FSTORE_3 = "fstore_3";
    public static final String  NAME_DSTORE_0 = "dstore_0";
    public static final String  NAME_DSTORE_1 = "dstore_1";
    public static final String  NAME_DSTORE_2 = "dstore_2";
    public static final String  NAME_DSTORE_3 = "dstore_3";
    public static final String  NAME_ASTORE_0 = "astore_0";
    public static final String  NAME_ASTORE_1 = "astore_1";
    public static final String  NAME_ASTORE_2 = "astore_2";
    public static final String  NAME_ASTORE_3 = "astore_3";
    public static final String  NAME_IASTORE = "iastore";
    public static final String  NAME_LASTORE = "lastore";
    public static final String  NAME_FASTORE = "fastore";
    public static final String  NAME_DASTORE = "dastore";
    public static final String  NAME_AASTORE = "aastore";
    public static final String  NAME_BASTORE = "bastore";
    public static final String  NAME_CASTORE = "castore";
    public static final String  NAME_SASTORE = "sastore";
    public static final String  NAME_POP = "pop";
    public static final String  NAME_POP2 = "pop2";
    public static final String  NAME_DUP = "dup";
    public static final String  NAME_DUP_X1 = "dup_x1";
    public static final String  NAME_DUP_X2 = "dup_x2";
    public static final String  NAME_DUP2 = "dup2";
    public static final String  NAME_DUP2_X1 = "dup2_x1";
    public static final String  NAME_DUP2_X2 = "dup2_x2";
    public static final String  NAME_SWAP = "swap";
    public static final String  NAME_IADD = "iadd";
    public static final String  NAME_LADD = "ladd";
    public static final String  NAME_FADD = "fadd";
    public static final String  NAME_DADD = "dadd";
    public static final String  NAME_ISUB = "isub";
    public static final String  NAME_LSUB = "lsub";
    public static final String  NAME_FSUB = "fsub";
    public static final String  NAME_DSUB = "dsub";
    public static final String  NAME_IMUL = "imul";
    public static final String  NAME_LMUL = "lmul";
    public static final String  NAME_FMUL = "fmul";
    public static final String  NAME_DMUL = "dmul";
    public static final String  NAME_IDIV = "idiv";
    public static final String  NAME_LDIV = "ldiv";
    public static final String  NAME_FDIV = "fdiv";
    public static final String  NAME_DDIV = "ddiv";
    public static final String  NAME_IREM = "irem";
    public static final String  NAME_LREM = "lrem";
    public static final String  NAME_FREM = "frem";
    public static final String  NAME_DREM = "drem";
    public static final String  NAME_INEG = "ineg";
    public static final String  NAME_LNEG = "lneg";
    public static final String  NAME_FNEG = "fneg";
    public static final String  NAME_DNEG = "dneg";
    public static final String  NAME_ISHL = "ishl";
    public static final String  NAME_LSHL = "lshl";
    public static final String  NAME_ISHR = "ishr";
    public static final String  NAME_LSHR = "lshr";
    public static final String  NAME_IUSHR = "iushr";
    public static final String  NAME_LUSHR = "lushr";
    public static final String  NAME_IAND = "iand";
    public static final String  NAME_LAND = "land";
    public static final String  NAME_IOR = "ior";
    public static final String  NAME_LOR = "lor";
    public static final String  NAME_IXOR = "ixor";
    public static final String  NAME_LXOR = "lxor";
    public static final String  NAME_IINC = "iinc";
    public static final String  NAME_I2L = "i2l";
    public static final String  NAME_I2F = "i2f";
    public static final String  NAME_I2D = "i2d";
    public static final String  NAME_L2I = "l2i";
    public static final String  NAME_L2F = "l2f";
    public static final String  NAME_L2D = "l2d";
    public static final String  NAME_F2I = "f2i";
    public static final String  NAME_F2L = "f2l";
    public static final String  NAME_F2D = "f2d";
    public static final String  NAME_D2I = "d2i";
    public static final String  NAME_D2L = "d2l";
    public static final String  NAME_D2F = "d2f";
    public static final String  NAME_I2B = "i2b";
    public static final String  NAME_I2C = "i2c";
    public static final String  NAME_I2S = "i2s";
    public static final String  NAME_LCMP = "lcmp";
    public static final String  NAME_FCMPL = "fcmpl";
    public static final String  NAME_FCMPG = "fcmpg";
    public static final String  NAME_DCMPL = "dcmpl";
    public static final String  NAME_DCMPG = "dcmpg";
    public static final String  NAME_IFEQ = "ifeq";
    public static final String  NAME_IFNE = "ifne";
    public static final String  NAME_IFLT = "iflt";
    public static final String  NAME_IFGE = "ifge";
    public static final String  NAME_IFGT = "ifgt";
    public static final String  NAME_IFLE = "ifle";
    public static final String  NAME_IF_ICMPEQ = "if_icmpeq";
    public static final String  NAME_IF_ICMPNE = "if_icmpne";
    public static final String  NAME_IF_ICMPLT = "if_icmplt";
    public static final String  NAME_IF_ICMPGE = "if_icmpge";
    public static final String  NAME_IF_ICMPGT = "if_icmpgt";
    public static final String  NAME_IF_ICMPLE = "if_icmple";
    public static final String  NAME_IF_ACMPEQ = "if_acmpeq";
    public static final String  NAME_IF_ACMPNE = "if_acmpne";
    public static final String  NAME_GOTO = "goto";
    public static final String  NAME_JSR = "jsr";
    public static final String  NAME_RET = "ret";
    public static final String  NAME_TABLESWITCH = "tableswitch";
    public static final String  NAME_LOOKUPSWITCH = "lookupswitch";
    public static final String  NAME_IRETURN = "ireturn";
    public static final String  NAME_LRETURN = "lreturn";
    public static final String  NAME_FRETURN = "freturn";
    public static final String  NAME_DRETURN = "dreturn";
    public static final String  NAME_ARETURN = "areturn";
    public static final String  NAME_RETURN = "return";
    public static final String  NAME_GETSTATIC = "getstatic";
    public static final String  NAME_PUTSTATIC = "putstatic";
    public static final String  NAME_GETFIELD = "getfield";
    public static final String  NAME_PUTFIELD = "putfield";
    public static final String  NAME_INVOKEVIRTUAL = "invokevirtual";
    public static final String  NAME_INVOKESPECIAL = "invokespecial";
    public static final String  NAME_INVOKESTATIC = "invokestatic";
    public static final String  NAME_INVOKEINTERFACE = "invokeinterface";
    public static final String  NAME_NEW = "new";
    public static final String  NAME_NEWARRAY = "newarray";
    public static final String  NAME_ANEWARRAY = "anewarray";
    public static final String  NAME_ARRAYLENGTH = "arraylength";
    public static final String  NAME_ATHROW = "athrow";
    public static final String  NAME_CHECKCAST = "checkcast";
    public static final String  NAME_INSTANCEOF = "instanceof";
    public static final String  NAME_MONITORENTER = "monitorenter";
    public static final String  NAME_MONITOREXIT = "monitorexit";
    public static final String  NAME_WIDE = "wide";
    public static final String  NAME_MULTIANEWARRAY = "multianewarray";
    public static final String  NAME_IFNULL = "ifnull";
    public static final String  NAME_IFNONNULL = "ifnonnull";
    public static final String  NAME_GOTO_W = "goto_w";
    public static final String  NAME_JSR_W = "jsr_w";

    //
    // Virtual instructions that aren't official JVM opcodes, but they are
    // recognized by our assembler where they're translated into one or more
    // real opcodes.
    //
    // NOTE - any names that are assigned null strings are associated with
    // opcodes that the assembler uses but aren't recognized if they appear
    // in your assembly language source code.
    //

    public static final String  NAME_EXCH = "exch";
    public static final String  NAME_PUSH = "push";
    public static final String  NAME_STORE = "store";
    public static final String  NAME_NEG = "neg";
    public static final String  NAME_ADD = "add";
    public static final String  NAME_SUB = "sub";
    public static final String  NAME_MUL = "mul";
    public static final String  NAME_DIV = "div";
    public static final String  NAME_REM = "rem";
    public static final String  NAME_AND = "and";
    public static final String  NAME_OR = "or";
    public static final String  NAME_XOR = "xor";
    public static final String  NAME_SHL = "shl";
    public static final String  NAME_SHR = "shr";
    public static final String  NAME_USHR = "ushr";
    public static final String  NAME_INVOKE = "invoke";
    public static final String  NAME_SWITCH = "switch";
    public static final String  NAME_ARRAYLOAD = "arrayload";
    public static final String  NAME_ARRAYSTORE = "arraystore";

    //
    // These "virtual instructions" are only for internal use, so the slot
    // that they occupy in OPCODE_MNEMONICS[], which is defined below, has
    // been set to null.
    //

    public static final String  NAME_DCONST = "dconst";
    public static final String  NAME_FCONST = "fconst";
    public static final String  NAME_GET = "get";
    public static final String  NAME_ICONST = "iconst";
    public static final String  NAME_LCONST = "lconst";
    public static final String  NAME_LOAD = "load";
    public static final String  NAME_PUT = "put";

    //
    // Recent additions - NAME_CALL really isn't ready for prime time.
    //

    public static final String  NAME_CALL = "call";
    public static final String  NAME_CAST2D = "cast2d";
    public static final String  NAME_CAST2F = "cast2f";
    public static final String  NAME_CAST2I = "cast2i";
    public static final String  NAME_CAST2L = "cast2l";
    public static final String  NAME_DUPX = "dupx";

    //
    // These are the corresponding opcode definitions that are used when we
    // generate bytecode or need a case label in a switch statement.
    //

    public static final int  OP_NOP = 0x00;
    public static final int  OP_ACONST_NULL = 0x01;
    public static final int  OP_ICONST_M1 = 0x02;
    public static final int  OP_ICONST_0 = 0x03;
    public static final int  OP_ICONST_1 = 0x04;
    public static final int  OP_ICONST_2 = 0x05;
    public static final int  OP_ICONST_3 = 0x06;
    public static final int  OP_ICONST_4 = 0x07;
    public static final int  OP_ICONST_5 = 0x08;
    public static final int  OP_LCONST_0 = 0x09;
    public static final int  OP_LCONST_1 = 0x0A;
    public static final int  OP_FCONST_0 = 0x0B;
    public static final int  OP_FCONST_1 = 0x0C;
    public static final int  OP_FCONST_2 = 0x0D;
    public static final int  OP_DCONST_0 = 0x0E;
    public static final int  OP_DCONST_1 = 0x0F;
    public static final int  OP_BIPUSH = 0x10;
    public static final int  OP_SIPUSH = 0x11;
    public static final int  OP_LDC = 0x12;
    public static final int  OP_LDC_W = 0x13;
    public static final int  OP_LDC2_W = 0x14;
    public static final int  OP_ILOAD = 0x15;
    public static final int  OP_LLOAD = 0x16;
    public static final int  OP_FLOAD = 0x17;
    public static final int  OP_DLOAD = 0x18;
    public static final int  OP_ALOAD = 0x19;
    public static final int  OP_ILOAD_0 = 0x1A;
    public static final int  OP_ILOAD_1 = 0x1B;
    public static final int  OP_ILOAD_2 = 0x1C;
    public static final int  OP_ILOAD_3 = 0x1D;
    public static final int  OP_LLOAD_0 = 0x1E;
    public static final int  OP_LLOAD_1 = 0x1F;
    public static final int  OP_LLOAD_2 = 0x20;
    public static final int  OP_LLOAD_3 = 0x21;
    public static final int  OP_FLOAD_0 = 0x22;
    public static final int  OP_FLOAD_1 = 0x23;
    public static final int  OP_FLOAD_2 = 0x24;
    public static final int  OP_FLOAD_3 = 0x25;
    public static final int  OP_DLOAD_0 = 0x26;
    public static final int  OP_DLOAD_1 = 0x27;
    public static final int  OP_DLOAD_2 = 0x28;
    public static final int  OP_DLOAD_3 = 0x29;
    public static final int  OP_ALOAD_0 = 0x2A;
    public static final int  OP_ALOAD_1 = 0x2B;
    public static final int  OP_ALOAD_2 = 0x2C;
    public static final int  OP_ALOAD_3 = 0x2D;
    public static final int  OP_IALOAD = 0x2E;
    public static final int  OP_LALOAD = 0x2F;
    public static final int  OP_FALOAD = 0x30;
    public static final int  OP_DALOAD = 0x31;
    public static final int  OP_AALOAD = 0x32;
    public static final int  OP_BALOAD = 0x33;
    public static final int  OP_CALOAD = 0x34;
    public static final int  OP_SALOAD = 0x35;
    public static final int  OP_ISTORE = 0x36;
    public static final int  OP_LSTORE = 0x37;
    public static final int  OP_FSTORE = 0x38;
    public static final int  OP_DSTORE = 0x39;
    public static final int  OP_ASTORE = 0x3A;
    public static final int  OP_ISTORE_0 = 0x3B;
    public static final int  OP_ISTORE_1 = 0x3C;
    public static final int  OP_ISTORE_2 = 0x3D;
    public static final int  OP_ISTORE_3 = 0x3E;
    public static final int  OP_LSTORE_0 = 0x3F;
    public static final int  OP_LSTORE_1 = 0x40;
    public static final int  OP_LSTORE_2 = 0x41;
    public static final int  OP_LSTORE_3 = 0x42;
    public static final int  OP_FSTORE_0 = 0x43;
    public static final int  OP_FSTORE_1 = 0x44;
    public static final int  OP_FSTORE_2 = 0x45;
    public static final int  OP_FSTORE_3 = 0x46;
    public static final int  OP_DSTORE_0 = 0x47;
    public static final int  OP_DSTORE_1 = 0x48;
    public static final int  OP_DSTORE_2 = 0x49;
    public static final int  OP_DSTORE_3 = 0x4A;
    public static final int  OP_ASTORE_0 = 0x4B;
    public static final int  OP_ASTORE_1 = 0x4C;
    public static final int  OP_ASTORE_2 = 0x4D;
    public static final int  OP_ASTORE_3 = 0x4E;
    public static final int  OP_IASTORE = 0x4F;
    public static final int  OP_LASTORE = 0x50;
    public static final int  OP_FASTORE = 0x51;
    public static final int  OP_DASTORE = 0x52;
    public static final int  OP_AASTORE = 0x53;
    public static final int  OP_BASTORE = 0x54;
    public static final int  OP_CASTORE = 0x55;
    public static final int  OP_SASTORE = 0x56;
    public static final int  OP_POP = 0x57;
    public static final int  OP_POP2 = 0x58;
    public static final int  OP_DUP = 0x59;
    public static final int  OP_DUP_X1 = 0x5A;
    public static final int  OP_DUP_X2 = 0x5B;
    public static final int  OP_DUP2 = 0x5C;
    public static final int  OP_DUP2_X1 = 0x5D;
    public static final int  OP_DUP2_X2 = 0x5E;
    public static final int  OP_SWAP = 0x5F;
    public static final int  OP_IADD = 0x60;
    public static final int  OP_LADD = 0x61;
    public static final int  OP_FADD = 0x62;
    public static final int  OP_DADD = 0x63;
    public static final int  OP_ISUB = 0x64;
    public static final int  OP_LSUB = 0x65;
    public static final int  OP_FSUB = 0x66;
    public static final int  OP_DSUB = 0x67;
    public static final int  OP_IMUL = 0x68;
    public static final int  OP_LMUL = 0x69;
    public static final int  OP_FMUL = 0x6A;
    public static final int  OP_DMUL = 0x6B;
    public static final int  OP_IDIV = 0x6C;
    public static final int  OP_LDIV = 0x6D;
    public static final int  OP_FDIV = 0x6E;
    public static final int  OP_DDIV = 0x6F;
    public static final int  OP_IREM = 0x70;
    public static final int  OP_LREM = 0x71;
    public static final int  OP_FREM = 0x72;
    public static final int  OP_DREM = 0x73;
    public static final int  OP_INEG = 0x74;
    public static final int  OP_LNEG = 0x75;
    public static final int  OP_FNEG = 0x76;
    public static final int  OP_DNEG = 0x77;
    public static final int  OP_ISHL = 0x78;
    public static final int  OP_LSHL = 0x79;
    public static final int  OP_ISHR = 0x7A;
    public static final int  OP_LSHR = 0x7B;
    public static final int  OP_IUSHR = 0x7C;
    public static final int  OP_LUSHR = 0x7D;
    public static final int  OP_IAND = 0x7E;
    public static final int  OP_LAND = 0x7F;
    public static final int  OP_IOR = 0x80;
    public static final int  OP_LOR = 0x81;
    public static final int  OP_IXOR = 0x82;
    public static final int  OP_LXOR = 0x83;
    public static final int  OP_IINC = 0x84;
    public static final int  OP_I2L = 0x85;
    public static final int  OP_I2F = 0x86;
    public static final int  OP_I2D = 0x87;
    public static final int  OP_L2I = 0x88;
    public static final int  OP_L2F = 0x89;
    public static final int  OP_L2D = 0x8A;
    public static final int  OP_F2I = 0x8B;
    public static final int  OP_F2L = 0x8C;
    public static final int  OP_F2D = 0x8D;
    public static final int  OP_D2I = 0x8E;
    public static final int  OP_D2L = 0x8F;
    public static final int  OP_D2F = 0x90;
    public static final int  OP_I2B = 0x91;
    public static final int  OP_I2C = 0x92;
    public static final int  OP_I2S = 0x93;
    public static final int  OP_LCMP = 0x94;
    public static final int  OP_FCMPL = 0x95;
    public static final int  OP_FCMPG = 0x96;
    public static final int  OP_DCMPL = 0x97;
    public static final int  OP_DCMPG = 0x98;
    public static final int  OP_IFEQ = 0x99;
    public static final int  OP_IFNE = 0x9A;
    public static final int  OP_IFLT = 0x9B;
    public static final int  OP_IFGE = 0x9C;
    public static final int  OP_IFGT = 0x9D;
    public static final int  OP_IFLE = 0x9E;
    public static final int  OP_IF_ICMPEQ = 0x9F;
    public static final int  OP_IF_ICMPNE = 0xA0;
    public static final int  OP_IF_ICMPLT = 0xA1;
    public static final int  OP_IF_ICMPGE = 0xA2;
    public static final int  OP_IF_ICMPGT = 0xA3;
    public static final int  OP_IF_ICMPLE = 0xA4;
    public static final int  OP_IF_ACMPEQ = 0xA5;
    public static final int  OP_IF_ACMPNE = 0xA6;
    public static final int  OP_GOTO = 0xA7;
    public static final int  OP_JSR = 0xA8;
    public static final int  OP_RET = 0xA9;
    public static final int  OP_TABLESWITCH = 0xAA;
    public static final int  OP_LOOKUPSWITCH = 0xAB;
    public static final int  OP_IRETURN = 0xAC;
    public static final int  OP_LRETURN = 0xAD;
    public static final int  OP_FRETURN = 0xAE;
    public static final int  OP_DRETURN = 0xAF;
    public static final int  OP_ARETURN = 0xB0;
    public static final int  OP_RETURN = 0xB1;
    public static final int  OP_GETSTATIC = 0xB2;
    public static final int  OP_PUTSTATIC = 0xB3;
    public static final int  OP_GETFIELD = 0xB4;
    public static final int  OP_PUTFIELD = 0xB5;
    public static final int  OP_INVOKEVIRTUAL = 0xB6;
    public static final int  OP_INVOKESPECIAL = 0xB7;
    public static final int  OP_INVOKESTATIC = 0xB8;
    public static final int  OP_INVOKEINTERFACE = 0xB9;
    public static final int  OP_NEW = 0xBB;
    public static final int  OP_NEWARRAY = 0xBC;
    public static final int  OP_ANEWARRAY = 0xBD;
    public static final int  OP_ARRAYLENGTH = 0xBE;
    public static final int  OP_ATHROW = 0xBF;
    public static final int  OP_CHECKCAST = 0xC0;
    public static final int  OP_INSTANCEOF = 0xC1;
    public static final int  OP_MONITORENTER = 0xC2;
    public static final int  OP_MONITOREXIT = 0xC3;
    public static final int  OP_WIDE = 0xC4;
    public static final int  OP_MULTIANEWARRAY = 0xC5;
    public static final int  OP_IFNULL = 0xC6;
    public static final int  OP_IFNONNULL = 0xC7;
    public static final int  OP_GOTO_W = 0xC8;
    public static final int  OP_JSR_W = 0xC9;

    //
    // Virtual instructions that aren't official JVM opcodes, but they are
    // recognized by our assembler where they're translated into one or more
    // real instructions. They currently don't appear in disassembler output.
    //

    public static final int  OP_EXCH = 0x100;
    public static final int  OP_PUSH = 0x101;
    public static final int  OP_STORE = 0x102;
    public static final int  OP_NEG = 0x103;
    public static final int  OP_ADD = 0x104;
    public static final int  OP_SUB = 0x105;
    public static final int  OP_MUL = 0x106;
    public static final int  OP_DIV = 0x107;
    public static final int  OP_REM = 0x108;
    public static final int  OP_AND = 0x109;
    public static final int  OP_OR = 0x10A;
    public static final int  OP_XOR = 0x10B;
    public static final int  OP_SHL = 0x10C;
    public static final int  OP_SHR = 0x10D;
    public static final int  OP_USHR = 0x10E;
    public static final int  OP_INVOKE = 0x10F;
    public static final int  OP_SWITCH = 0x110;
    public static final int  OP_ARRAYLOAD = 0x111;
    public static final int  OP_ARRAYSTORE = 0x112;

    //
    // These "virtual instructions" are only for internal use, so the slot
    // that they occupy in OPCODE_MNEMONICS[], which is defined below, has
    // been set to null.
    //

    public static final int  OP_DCONST = 0x113;
    public static final int  OP_FCONST = 0x114;
    public static final int  OP_GET = 0x115;
    public static final int  OP_ICONST = 0x116;
    public static final int  OP_LCONST = 0x117;
    public static final int  OP_LOAD = 0x118;
    public static final int  OP_PUT = 0x119;

    //
    // Recent additions - OP_CALL really isn't ready for prime time.
    //

    public static final int  OP_CALL = 0x11A;
    public static final int  OP_CAST2D = 0x11B;
    public static final int  OP_CAST2F = 0x11C;
    public static final int  OP_CAST2I = 0x11D;
    public static final int  OP_CAST2L = 0x11E;
    public static final int  OP_DUPX = 0x11F;

    //
    // An array that maps opcodes to the strings that we're supposed to use
    // in error messages or disassembler output.
    //

    public static final String  OPCODE_MNEMONICS[] = {
	NAME_NOP,			// 0x00
	NAME_ACONST_NULL,		// 0x01
	NAME_ICONST_M1,			// 0x02
	NAME_ICONST_0,			// 0x03
	NAME_ICONST_1,			// 0x04
	NAME_ICONST_2,			// 0x05
	NAME_ICONST_3,			// 0x06
	NAME_ICONST_4,			// 0x07
	NAME_ICONST_5,			// 0x08
	NAME_LCONST_0,			// 0x09
	NAME_LCONST_1,			// 0x0A
	NAME_FCONST_0,			// 0x0B
	NAME_FCONST_1,			// 0x0C
	NAME_FCONST_2,			// 0x0D
	NAME_DCONST_0,			// 0x0E
	NAME_DCONST_1,			// 0x0F
	NAME_BIPUSH,			// 0x10
	NAME_SIPUSH,			// 0x11
	NAME_LDC,			// 0x12
	NAME_LDC_W,			// 0x13
	NAME_LDC2_W,			// 0x14
	NAME_ILOAD,			// 0x15
	NAME_LLOAD,			// 0x16
	NAME_FLOAD,			// 0x17
	NAME_DLOAD,			// 0x18
	NAME_ALOAD,			// 0x19
	NAME_ILOAD_0,			// 0x1A
	NAME_ILOAD_1,			// 0x1B
	NAME_ILOAD_2,			// 0x1C
	NAME_ILOAD_3,			// 0x1D
	NAME_LLOAD_0,			// 0x1E
	NAME_LLOAD_1,			// 0x1F
	NAME_LLOAD_2,			// 0x20
	NAME_LLOAD_3,			// 0x21
	NAME_FLOAD_0,			// 0x22
	NAME_FLOAD_1,			// 0x23
	NAME_FLOAD_2,			// 0x24
	NAME_FLOAD_3,			// 0x25
	NAME_DLOAD_0,			// 0x26
	NAME_DLOAD_1,			// 0x27
	NAME_DLOAD_2,			// 0x28
	NAME_DLOAD_3,			// 0x29
	NAME_ALOAD_0,			// 0x2A
	NAME_ALOAD_1,			// 0x2B
	NAME_ALOAD_2,			// 0x2C
	NAME_ALOAD_3,			// 0x2D
	NAME_IALOAD,			// 0x2E
	NAME_LALOAD,			// 0x2F
	NAME_FALOAD,			// 0x30
	NAME_DALOAD,			// 0x31
	NAME_AALOAD,			// 0x32
	NAME_BALOAD,			// 0x33
	NAME_CALOAD,			// 0x34
	NAME_SALOAD,			// 0x35
	NAME_ISTORE,			// 0x36
	NAME_LSTORE,			// 0x37
	NAME_FSTORE,			// 0x38
	NAME_DSTORE,			// 0x39
	NAME_ASTORE,			// 0x3A
	NAME_ISTORE_0,			// 0x3B
	NAME_ISTORE_1,			// 0x3C
	NAME_ISTORE_2,			// 0x3D
	NAME_ISTORE_3,			// 0x3E
	NAME_LSTORE_0,			// 0x3F
	NAME_LSTORE_1,			// 0x40
	NAME_LSTORE_2,			// 0x41
	NAME_LSTORE_3,			// 0x42
	NAME_FSTORE_0,			// 0x43
	NAME_FSTORE_1,			// 0x44
	NAME_FSTORE_2,			// 0x45
	NAME_FSTORE_3,			// 0x46
	NAME_DSTORE_0,			// 0x47
	NAME_DSTORE_1,			// 0x48
	NAME_DSTORE_2,			// 0x49
	NAME_DSTORE_3,			// 0x4A
	NAME_ASTORE_0,			// 0x4B
	NAME_ASTORE_1,			// 0x4C
	NAME_ASTORE_2,			// 0x4D
	NAME_ASTORE_3,			// 0x4E
	NAME_IASTORE,			// 0x4F
	NAME_LASTORE,			// 0x50
	NAME_FASTORE,			// 0x51
	NAME_DASTORE,			// 0x52
	NAME_AASTORE,			// 0x53
	NAME_BASTORE,			// 0x54
	NAME_CASTORE,			// 0x55
	NAME_SASTORE,			// 0x56
	NAME_POP,			// 0x57
	NAME_POP2,			// 0x58
	NAME_DUP,			// 0x59
	NAME_DUP_X1,			// 0x5A
	NAME_DUP_X2,			// 0x5B
	NAME_DUP2,			// 0x5C
	NAME_DUP2_X1,			// 0x5D
	NAME_DUP2_X2,			// 0x5E
	NAME_SWAP,			// 0x5F
	NAME_IADD,			// 0x60
	NAME_LADD,			// 0x61
	NAME_FADD,			// 0x62
	NAME_DADD,			// 0x63
	NAME_ISUB,			// 0x64
	NAME_LSUB,			// 0x65
	NAME_FSUB,			// 0x66
	NAME_DSUB,			// 0x67
	NAME_IMUL,			// 0x68
	NAME_LMUL,			// 0x69
	NAME_FMUL,			// 0x6A
	NAME_DMUL,			// 0x6B
	NAME_IDIV,			// 0x6C
	NAME_LDIV,			// 0x6D
	NAME_FDIV,			// 0x6E
	NAME_DDIV,			// 0x6F
	NAME_IREM,			// 0x70
	NAME_LREM,			// 0x71
	NAME_FREM,			// 0x72
	NAME_DREM,			// 0x73
	NAME_INEG,			// 0x74
	NAME_LNEG,			// 0x75
	NAME_FNEG,			// 0x76
	NAME_DNEG,			// 0x77
	NAME_ISHL,			// 0x78
	NAME_LSHL,			// 0x79
	NAME_ISHR,			// 0x7A
	NAME_LSHR,			// 0x7B
	NAME_IUSHR,			// 0x7C
	NAME_LUSHR,			// 0x7D
	NAME_IAND,			// 0x7E
	NAME_LAND,			// 0x7F
	NAME_IOR,			// 0x80
	NAME_LOR,			// 0x81
	NAME_IXOR,			// 0x82
	NAME_LXOR,			// 0x83
	NAME_IINC,			// 0x84
	NAME_I2L,			// 0x85
	NAME_I2F,			// 0x86
	NAME_I2D,			// 0x87
	NAME_L2I,			// 0x88
	NAME_L2F,			// 0x89
	NAME_L2D,			// 0x8A
	NAME_F2I,			// 0x8B
	NAME_F2L,			// 0x8C
	NAME_F2D,			// 0x8D
	NAME_D2I,			// 0x8E
	NAME_D2L,			// 0x8F
	NAME_D2F,			// 0x90
	NAME_I2B,			// 0x91
	NAME_I2C,			// 0x92
	NAME_I2S,			// 0x93
	NAME_LCMP,			// 0x94
	NAME_FCMPL,			// 0x95
	NAME_FCMPG,			// 0x96
	NAME_DCMPL,			// 0x97
	NAME_DCMPG,			// 0x98
	NAME_IFEQ,			// 0x99
	NAME_IFNE,			// 0x9A
	NAME_IFLT,			// 0x9B
	NAME_IFGE,			// 0x9C
	NAME_IFGT,			// 0x9D
	NAME_IFLE,			// 0x9E
	NAME_IF_ICMPEQ,			// 0x9F
	NAME_IF_ICMPNE,			// 0xA0
	NAME_IF_ICMPLT,			// 0xA1
	NAME_IF_ICMPGE,			// 0xA2
	NAME_IF_ICMPGT,			// 0xA3
	NAME_IF_ICMPLE,			// 0xA4
	NAME_IF_ACMPEQ,			// 0xA5
	NAME_IF_ACMPNE,			// 0xA6
	NAME_GOTO,			// 0xA7
	NAME_JSR,			// 0xA8
	NAME_RET,			// 0xA9
	NAME_TABLESWITCH,		// 0xAA
	NAME_LOOKUPSWITCH,		// 0xAB
	NAME_IRETURN,			// 0xAC
	NAME_LRETURN,			// 0xAD
	NAME_FRETURN,			// 0xAE
	NAME_DRETURN,			// 0xAF
	NAME_ARETURN,			// 0xB0
	NAME_RETURN,			// 0xB1
	NAME_GETSTATIC,			// 0xB2
	NAME_PUTSTATIC,			// 0xB3
	NAME_GETFIELD,			// 0xB4
	NAME_PUTFIELD,			// 0xB5
	NAME_INVOKEVIRTUAL,		// 0xB6
	NAME_INVOKESPECIAL,		// 0xB7
	NAME_INVOKESTATIC,		// 0xB8
	NAME_INVOKEINTERFACE,		// 0xB9
	null,				// 0xBA - unused
	NAME_NEW,			// 0xBB
	NAME_NEWARRAY,			// 0xBC
	NAME_ANEWARRAY,			// 0xBD
	NAME_ARRAYLENGTH,		// 0xBE
	NAME_ATHROW,			// 0xBF
	NAME_CHECKCAST,			// 0xC0
	NAME_INSTANCEOF,		// 0xC1
	NAME_MONITORENTER,		// 0xC2
	NAME_MONITOREXIT,		// 0xC3
	NAME_WIDE,			// 0xC4
	NAME_MULTIANEWARRAY,		// 0xC5
	NAME_IFNULL,			// 0xC6
	NAME_IFNONNULL,			// 0xC7
	NAME_GOTO_W,			// 0xC8
	NAME_JSR_W,			// 0xC9

	//
	// The remaining 8-bit opcodes are reserved or currently unused.
	//

	null,				// 0xCA
	null,				// 0xCB
	null,				// 0xCC
	null,				// 0xCD
	null,				// 0xCE
	null,				// 0xCF
	null,				// 0xD0
	null,				// 0xD1
	null,				// 0xD2
	null,				// 0xD3
	null,				// 0xD4
	null,				// 0xD5
	null,				// 0xD6
	null,				// 0xD7
	null,				// 0xD8
	null,				// 0xD9
	null,				// 0xDA
	null,				// 0xDB
	null,				// 0xDC
	null,				// 0xDD
	null,				// 0xDE
	null,				// 0xDF
	null,				// 0xE0
	null,				// 0xE1
	null,				// 0xE2
	null,				// 0xE3
	null,				// 0xE4
	null,				// 0xE5
	null,				// 0xE6
	null,				// 0xE7
	null,				// 0xE8
	null,				// 0xE9
	null,				// 0xEA
	null,				// 0xEB
	null,				// 0xEC
	null,				// 0xED
	null,				// 0xEE
	null,				// 0xEF
	null,				// 0xF0
	null,				// 0xF1
	null,				// 0xF2
	null,				// 0xF3
	null,				// 0xF4
	null,				// 0xF5
	null,				// 0xF6
	null,				// 0xF7
	null,				// 0xF8
	null,				// 0xF9
	null,				// 0xFA
	null,				// 0xFB
	null,				// 0xFC
	null,				// 0xFD
	null,				// 0xFE
	null,				// 0xFF

	//
	// Virtual instructions that aren't official JVM opcodes, but they
	// are recognized by our assembler where they're translated into one
	// or more real instructions.
	//

	NAME_EXCH,			// 0x100
	NAME_PUSH,			// 0x101
	NAME_STORE,			// 0x102
	NAME_NEG,			// 0x103
	NAME_ADD,			// 0x104
	NAME_SUB,			// 0x105
	NAME_MUL,			// 0x106
	NAME_DIV,			// 0x107
	NAME_REM,			// 0x108
	NAME_AND,			// 0x109
	NAME_OR,			// 0x10A
	NAME_XOR,			// 0x10B
	NAME_SHL,			// 0x10C
	NAME_SHR,			// 0x10D
	NAME_USHR,			// 0x10E
	NAME_INVOKE,			// 0x10F
	NAME_SWITCH,			// 0x110
	NAME_ARRAYLOAD,			// 0x111
	NAME_ARRAYSTORE,		// 0x112

	//
	// Virtual instructions that are for internal use only, so we set
	// their entries to null which means the assembler won't recognize
	// them when it parses assembly language source code.
	//

	null,				// 0x113 - NAME_DCONST
	null,				// 0x114 - NAME_FCONST
	null,				// 0x115 - NAME_GET
	null,				// 0x116 - NAME_ICONST
	null,				// 0x117 - NAME_LCONST
	null,				// 0x118 - NAME_LOAD
	null,				// 0x119 - NAME_PUT

	//
	// Recent additions - NAME_CALL really isn't ready for prime time.
	//

	NAME_CALL,			// 0x11A
	NAME_CAST2D,			// 0x11B
	NAME_CAST2F,			// 0x11C
	NAME_CAST2I,			// 0x11D
	NAME_CAST2L,			// 0x11E
	NAME_DUPX,			// 0x11F
    };
}

