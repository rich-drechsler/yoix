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
class JVMScanner

    implements JVMConstants,
	       JVMPatterns

{

    //
    // Source files handed to assembleClass() create (or update) class files
    // using the following commands, which are extracted from the source and
    // then mapped to integer constants by various HashMaps that are defined
    // below.
    //
    // NOTE - several methods were borrowed from the Yoix interpreter source
    // because we wanted to make sure that the most important classes in this
    // package could stand on their own.
    //

    private JVMClassFile  classfile;
    private JVMAssembler  assembler;
    private JVMMethod     method;

    //
    // Strings and integer constants used to build special purpose HashMaps
    // that are only used by the scanner.
    //

    private static final String  NAME_ACCESS = "access";
    private static final String  NAME_CONSTANTVALUE = "constantvalue";
    private static final String  NAME_DEPRECATED = "deprecated";
    private static final String  NAME_DESCRIPTOR = "descriptor";
    private static final String  NAME_FIELD = "field";
    private static final String  NAME_MAXLOCAL = "maxlocal";
    private static final String  NAME_MAXSTACK = "maxstack";
    private static final String  NAME_METHOD = "method";
    private static final String  NAME_NAME = "name";
    private static final String  NAME_SUPER = "super";
    private static final String  NAME_SYNTHETIC = "synthetic";
    private static final String  NAME_THIS = "this";
    private static final String  NAME_TYPE = "type";

    private static final int  ID_ACCESS = 1;
    private static final int  ID_CONSTANTVALUE = 2;
    private static final int  ID_DEPRECATED = 3;
    private static final int  ID_DESCRIPTOR = 4;
    private static final int  ID_FIELD = 5;
    private static final int  ID_MAXLOCAL = 6;
    private static final int  ID_MAXSTACK = 7;
    private static final int  ID_METHOD = 8;
    private static final int  ID_NAME = 9;
    private static final int  ID_SUPER = 10;
    private static final int  ID_SYNTHETIC = 11;
    private static final int  ID_THIS = 12;
    private static final int  ID_TYPE = 13;

    //
    // These are the method-level commands.
    //

    private static final HashMap  METHOD_COMMANDS = new HashMap();

    static {
	METHOD_COMMANDS.put(NAME_ACCESS, new Integer(ID_ACCESS));
	METHOD_COMMANDS.put(NAME_DEPRECATED, new Integer(ID_DEPRECATED));
	METHOD_COMMANDS.put(NAME_NAME, new Integer(ID_NAME));
	METHOD_COMMANDS.put(NAME_SYNTHETIC, new Integer(ID_SYNTHETIC));
	METHOD_COMMANDS.put(NAME_TYPE, new Integer(ID_TYPE));
    }

    //
    // The next two HashMaps are used to convert between type name strings
    // that the assembler accepts in newarray instructions and the numbers
    // that the JVM expects.
    //

    private static final HashMap  BASE_TYPE_NAME_MAP = new HashMap();

    static {
	BASE_TYPE_NAME_MAP.put(BASE_TYPE_NAME_BOOLEAN, new Integer(BOOLEAN_ARRAY_TYPE_ID));
	BASE_TYPE_NAME_MAP.put(BASE_TYPE_NAME_CHAR, new Integer(CHAR_ARRAY_TYPE_ID));
	BASE_TYPE_NAME_MAP.put(BASE_TYPE_NAME_FLOAT, new Integer(FLOAT_ARRAY_TYPE_ID));
	BASE_TYPE_NAME_MAP.put(BASE_TYPE_NAME_DOUBLE, new Integer(DOUBLE_ARRAY_TYPE_ID));
	BASE_TYPE_NAME_MAP.put(BASE_TYPE_NAME_BYTE, new Integer(BYTE_ARRAY_TYPE_ID));
	BASE_TYPE_NAME_MAP.put(BASE_TYPE_NAME_SHORT, new Integer(SHORT_ARRAY_TYPE_ID));
	BASE_TYPE_NAME_MAP.put(BASE_TYPE_NAME_INT, new Integer(INT_ARRAY_TYPE_ID));
	BASE_TYPE_NAME_MAP.put(BASE_TYPE_NAME_LONG, new Integer(LONG_ARRAY_TYPE_ID));
    }

    private static final HashMap  BASE_TYPE_ID_MAP = new HashMap();

    static {
	BASE_TYPE_ID_MAP.put(new Integer(BOOLEAN_ARRAY_TYPE_ID), BASE_TYPE_NAME_BOOLEAN);
	BASE_TYPE_ID_MAP.put(new Integer(CHAR_ARRAY_TYPE_ID), BASE_TYPE_NAME_CHAR);
	BASE_TYPE_ID_MAP.put(new Integer(FLOAT_ARRAY_TYPE_ID), BASE_TYPE_NAME_FLOAT);
	BASE_TYPE_ID_MAP.put(new Integer(DOUBLE_ARRAY_TYPE_ID), BASE_TYPE_NAME_DOUBLE);
	BASE_TYPE_ID_MAP.put(new Integer(BYTE_ARRAY_TYPE_ID), BASE_TYPE_NAME_BYTE);
	BASE_TYPE_ID_MAP.put(new Integer(SHORT_ARRAY_TYPE_ID), BASE_TYPE_NAME_SHORT);
	BASE_TYPE_ID_MAP.put(new Integer(INT_ARRAY_TYPE_ID), BASE_TYPE_NAME_INT);
	BASE_TYPE_ID_MAP.put(new Integer(LONG_ARRAY_TYPE_ID), BASE_TYPE_NAME_LONG);
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    JVMScanner(JVMAssembler assembler) {

	buildScanner(assembler, null, null);
    }


    public
    JVMScanner(JVMAssembler assembler, JVMClassFile classfile) {

	buildScanner(assembler, classfile, null);
    }


    public
    JVMScanner(JVMAssembler assembler, JVMClassFile classfile, JVMMethod method) {

	buildScanner(assembler, classfile, method);
    }

    ///////////////////////////////////
    //
    // JVMScanner Methods
    //
    ///////////////////////////////////

    public Number
    consumeArrayType(String lines[], int index) {

	Matcher  matcher;
	Number   number = null;
	String   line;
	int      type;

	if ((line = lines[index]) != null) {
	    matcher = PATTERN_INTEGER.matcher(line);
	    if (matcher.find()) {
		try {
		    if ((type = Integer.parseInt(matcher.group(1))) >= 4 && type <= 11) {
			number = new Integer(type);
			lines[index] = matcher.replaceFirst("");
		    }
		}
		catch(NumberFormatException e) {}
	    } else {
		matcher = PATTERN_WORD.matcher(line);
		if (matcher.find()) {
		    if ((number = (Integer)BASE_TYPE_NAME_MAP.get(matcher.group(1))) != null)
			lines[index] = matcher.replaceFirst("");
		}
	    }
	}

	return(number);
    }


    public String
    consumeBranchLabel(String lines[], int index, int instruction_index) {

	return(consumeBranchLabel(lines, index, instruction_index, null));
    }


    public String
    consumeBranchLabel(String lines[], int index, int instruction_index, Integer match) {

	Matcher  matcher;
	String   label = null;

	//
	// Branching targets for all instructions must be supplied as labels
	// that are stored in method and resolved, by the assembler, as one
	// of the last steps involved in generating the method's bytecode.
	//

	if (lines[index] != null) {
	    matcher = PATTERN_NAME.matcher(lines[index]);
	    if (matcher.find()) {
		label = matcher.group(1);
		method.addBranchLabelReference(
		    label, 
		    new Object[] {new Integer(instruction_index), match}
		);
		lines[index] = matcher.replaceFirst("");
	    }
	}

	return(label);
    }


    public Object[]
    consumeCall(String lines[], int index, int instruction_index, int linenumber)

	throws JVMAssemblerError

    {

	ArrayList  list = null;
	Integer    type;
	Number     number;
	String     line;
	String     parameters;
	String     descriptor;
	int        code;
	int        next;
	int        method_index;
	int        opcode;

	//
	// Seems to work when arguments and parameters match up, but error
	// handling and type checking definitely need work!!! Anyway, this
	// was added quicklly without too much though and it really hasn't
	// been tested much either, so don't be surprised if you run into
	// problems - we will get back to it in the near future.
	//

	if ((line = lines[index]) != null) {
	    if ((number = consumeConstantPoolMethod(lines, index)) != null) {
		list = new ArrayList();
		method_index = number.intValue();
		parameters = JVMDescriptor.getDescriptorForMethodParameters(classfile.getDescriptor(method_index));
		while (consumeLine(lines, index) == false) {
		    if ((code = JVMDescriptor.getDescriptorCode(parameters)) > 0) {
			//
			// Logic should duplicate the way the assembler handles
			// the OP_PUSH opcode, which seems to suggest that there
			// should be a consumePush() method??
			//
			type = new Integer(code);
			if ((number = consumeQualifiedLiteralNumber(lines, index)) != null) {
			    if (number instanceof Integer) {
				list.add(new Integer(OP_ICONST));
				list.add(number);
				list.add(type);
			    } else if (number instanceof Long) {
				list.add(new Integer(OP_LCONST));
				list.add(number);
				list.add(type);
			    } else if (number instanceof Float) {
				list.add(new Integer(OP_FCONST));
				list.add(number);
				list.add(type);
			    } else {
				list.add(new Integer(OP_DCONST));
				list.add(number);
				list.add(type);
			    }
			} else if ((number = consumeConstantPoolString(lines, index)) != null) {
			    list.add(new Integer(OP_LDC));
			    list.add(number);
			    list.add(type);
			} else if (consumeToken(lines, index, PATTERN_NULL) != null) {
			    list.add(new Integer(OP_ACONST_NULL));
			    list.add(null);
			    list.add(type);
			} else if (consumeToken(lines, index, PATTERN_THIS) != null) {
			    if (method.isStatic() == false) {
				list.add(new Integer(OP_ALOAD_0));
				list.add(null);
				list.add(type);
			    } else assembler.recordError("can't use this in a static method", line, linenumber);
			} else if ((number = consumeLocalVariable(lines, index)) != null) {
			    list.add(new Integer(OP_LOAD));
			    list.add(number);
			    list.add(type);
			} else if ((number = consumeConstantPoolField(lines, index)) != null) {
			    list.add(new Integer(OP_GET));
			    list.add(number);
			    list.add(type);
			} else {
			    list = null;
			    break;
			}
		    } else {
			//
			// Ran out of parameter descriptors before we consumed
			// all the arguments.
			//
		    }
		    if ((next = JVMDescriptor.getNextDescriptorIndex(parameters)) >= 0)
			parameters = parameters.substring(next);
		    else parameters = null;
		}
		if (list != null) {
		    list.add(0, classfile.getMethodClassName(method_index));
		    list.add(0, new Integer(method_index));
		    list.add(0, new Integer(OP_INVOKE));
		}
	    } else lines[index] = line;
	}

	return(list != null ? list.toArray() : null);
    }


    public Number
    consumeConstantPoolClass(String lines[], int index) {

	Pattern  patterns[] = {PATTERN_CLASS_TYPE, PATTERN_ARRAY_TYPE, PATTERN_ARRAY_DESCRIPTOR, PATTERN_POOL_INDEX};
	Matcher  matcher;
	Number   number = null;
	String   line;
	String   descriptor;
	int      pool_index;
	int      n;

	if ((line = lines[index]) != null) {
	    for (n = 0; n < patterns.length && number == null; n++) {
		matcher = patterns[n].matcher(line);
		if (matcher.find()) {
		    if (patterns[n] == PATTERN_CLASS_TYPE) {
			if ((pool_index = classfile.storeConstantPoolClass(matcher.group(1))) > 0) {
			    number = new Integer(pool_index);
			    lines[index] = matcher.replaceFirst("");
			}
		    } else if (patterns[n] == PATTERN_ARRAY_TYPE) {
			if ((descriptor = JVMDescriptor.getDescriptorForArray(matcher.group(1))) != null) {
			    if ((pool_index = classfile.storeConstantPoolArrayClass(descriptor)) > 0) {
				number = new Integer(pool_index);
				lines[index] = matcher.replaceFirst("");
			    }
			}
		    } else if (patterns[n] == PATTERN_ARRAY_DESCRIPTOR) {
			if ((pool_index = classfile.storeConstantPoolArrayClass(matcher.group(1))) > 0) {
			    number = new Integer(pool_index);
			    lines[index] = matcher.replaceFirst("");
			}
		    } else if (patterns[n] == PATTERN_POOL_INDEX) {
			try {
			    if ((pool_index = Integer.parseInt(matcher.group(1))) >= 0 && pool_index <= 65535) {
				number = new Integer(pool_index);
				lines[index] = matcher.replaceFirst("");
			    }
			}
			catch(NumberFormatException e) {}
		    }
		}
	    }
	}

	return(number);
    }


    public Number
    consumeConstantPoolField(String lines[], int index) {

	Pattern  patterns[] = {PATTERN_QUALIFIED_NAME, PATTERN_POOL_INDEX};
	Matcher  matcher;
	Number   number = null;
	String   line;
	int      pool_index;
	int      n;

	if ((line = lines[index]) != null) {
	    for (n = 0; n < patterns.length && number == null; n++) {
		matcher = patterns[n].matcher(line);
		if (matcher.find()) {
		    if (patterns[n] == PATTERN_QUALIFIED_NAME) {
			if ((pool_index = classfile.storeConstantPoolFieldRef(matcher.group(1))) > 0) {
			    number = new Integer(pool_index);
			    lines[index] = matcher.replaceFirst("");
			}
		    } else if (patterns[n] == PATTERN_POOL_INDEX) {
			try {
			    if ((pool_index = Integer.parseInt(matcher.group(1))) >= 0 && pool_index <= 65535) {
				if (classfile.isFieldRef(pool_index)) {
				    number = new Integer(pool_index);
				    lines[index] = matcher.replaceFirst("");
				}
			    }
			}
			catch(NumberFormatException e) {}
		    }
		}
	    }
	}

	return(number);
    }


    public Number
    consumeConstantPoolLiteral(String lines[], int index, boolean wide) {

	Pattern  patterns[] = {PATTERN_HEX_STRING, PATTERN_INTEGER, PATTERN_FLOAT, PATTERN_STRING, PATTERN_CHAR, PATTERN_POOL_INDEX};

	//
	// PATTERN_HEX_STRING has to be matched first because the 0x that
	// introduces a hex string would match as an int (or float).
	//

	return(consumeConstantPoolLiteral(lines, index, wide, patterns));
    }


    public Number
    consumeConstantPoolLiteral(String lines[], int index, String descriptor) {

	Pattern  patterns[] = null;
	boolean  wide = false;

	switch (JVMDescriptor.getDescriptorCode(descriptor)) {
	    case DESCRIPTOR_BYTE:
	    case DESCRIPTOR_CHAR:
	    case DESCRIPTOR_FLOAT:
	    case DESCRIPTOR_INT:
	    case DESCRIPTOR_SHORT:
	    case DESCRIPTOR_BOOLEAN:
		patterns = new Pattern[] {PATTERN_INTEGER, PATTERN_FLOAT, PATTERN_CHAR};
		break;

	    case DESCRIPTOR_DOUBLE:
	    case DESCRIPTOR_LONG:
		patterns = new Pattern[] {PATTERN_INTEGER, PATTERN_FLOAT, PATTERN_CHAR};
		wide = true;
		break;

	    case DESCRIPTOR_CLASS:
		if (descriptor.equals(STRING_DESCRIPTOR))
		    patterns = new Pattern[] {PATTERN_HEX_STRING, PATTERN_STRING};
		break;
	}

	return(consumeConstantPoolLiteral(lines, index, wide, patterns));
    }


    public Number
    consumeConstantPoolMethod(String lines[], int index) {

	Pattern  patterns[] = {PATTERN_METHOD_REFERENCE, PATTERN_QUALIFIED_NAME, PATTERN_POOL_INDEX};
	Matcher  matcher;
	Number   number = null;
	String   line;
	int      pool_index;
	int      n;

	if ((line = lines[index]) != null) {
	    for (n = 0; n < patterns.length && number == null; n++) {
		matcher = patterns[n].matcher(line);
		if (matcher.find()) {
		    if (patterns[n] == PATTERN_METHOD_REFERENCE) {
			if ((pool_index = classfile.storeConstantPoolMethodRef(JVMDescriptor.getDescriptorForMethod(matcher.group(1)))) > 0) {
			    number = new Integer(pool_index);
			    lines[index] = matcher.replaceFirst("");
			}
		    } else if (patterns[n] == PATTERN_QUALIFIED_NAME) {
			if ((pool_index = classfile.storeConstantPoolMethodRef(matcher.group(1))) > 0) {
			    number = new Integer(pool_index);
			    lines[index] = matcher.replaceFirst("");
			}
		    } else if (patterns[n] == PATTERN_POOL_INDEX) {
			try {
			    if ((pool_index = Integer.parseInt(matcher.group(1))) >= 0 && pool_index <= 65535) {
				if (classfile.isMethodRef(pool_index)) {
				    number = new Integer(pool_index);
				    lines[index] = matcher.replaceFirst("");
				}
			    }
			}
			catch(NumberFormatException e) {}
		    }
		}
	    }
	}

	return(number);
    }


    public Number
    consumeConstantPoolString(String lines[], int index) {

	Pattern  patterns[] = {PATTERN_STRING, PATTERN_HEX_STRING, PATTERN_POOL_INDEX};
	Matcher  matcher;
	Number   number = null;
	String   line;
	int      pool_index;
	int      n;

	if ((line = lines[index]) != null) {
	    for (n = 0; n < patterns.length && number == null; n++) {
		matcher = patterns[n].matcher(line);
		if (matcher.find()) {
		    if (patterns[n] == PATTERN_STRING) {
			if ((pool_index = classfile.storeConstantPoolString(javaString(matcher.group(1), true))) > 0) {
			    number = new Integer(pool_index);
			    lines[index] = matcher.replaceFirst("");
			}
		    } else if (patterns[n] == PATTERN_HEX_STRING) {
			if ((pool_index = classfile.storeConstantPoolString(javaHexString(matcher.group(1)))) > 0) {
			    number = new Integer(pool_index);
			    lines[index] = matcher.replaceFirst("");
			}
		    } else if (patterns[n] == PATTERN_POOL_INDEX) {
			try {
			    if ((pool_index = Integer.parseInt(matcher.group(1))) >= 0 && pool_index <= 65535) {
				number = new Integer(pool_index);
				lines[index] = matcher.replaceFirst("");
			    }
			}
			catch(NumberFormatException e) {}
		    }
		}
	    }
	}

	return(number);
    }


    public JVMMethod
    consumeConstructor(String lines[], int index, int linenumber)

	throws JVMAssemblerError

    {

	JVMMethod  method = null;
	ArrayList  parametertypes;
	ArrayList  parameternames;
	Matcher    matcher;
	String     source;
	String     line;
	String     qualifiers;
	String     methodname;
	String     parameters;
	String     descriptor;
	String     name;
	int        access_flags;
	int        size;


	//
	// A null return means the first line didn't look like the start of
	// a constructor definition. Returning an invalid method means the
	// first line was good but something in the body of the constructor
	// caused problems.
	//
	// Constructors are assembled later because there's initialization
	// code that has to be added to each one, so we collect the source
	// code and store it and the starting line number in the method so
	// it's available when we're really ready to generate the bytecode.
	//

	if ((line = lines[index]) != null) {
	    if ((qualifiers = consumeToken(lines, index, PATTERN_CONSTRUCTOR_QUALIFIERS)) != null) {
		if ((methodname = consumeToken(lines, index, PATTERN_CONSTRUCTOR_NAME)) != null) {
		    if (JVMMisc.isConstuctorNameFor(methodname, classfile)) {
			method = new JVMMethod(classfile);
			methodname = NAME_INIT;
			consumeToken(lines, index, PATTERN_OPEN_PAREN);
			parameternames = new ArrayList();
			parametertypes = new ArrayList();
			access_flags = JVMMisc.getAccessFlags(qualifiers);
			if ((access_flags & ACC_STATIC) == 0)
			    method.registerLocalVariable(NAME_THIS, classfile.getDescriptor());
			matcher = PATTERN_METHOD_PARAMETER.matcher(lines[index]);
			while (matcher.find()) {
			    lines[index] = matcher.replaceFirst("");
			    descriptor = JVMDescriptor.getDescriptorForField(matcher.group(1));
			    name = matcher.group(2);
			    if ((size = JVMDescriptor.getStorageSize(descriptor)) > 0) {
				if (method.registerLocalVariable(name, descriptor, size) >= 0) {
				    parametertypes.add(descriptor);
				    parameternames.add(name);
				    if (consumeToken(lines, index, PATTERN_COMMA) != null)
					matcher = PATTERN_METHOD_PARAMETER.matcher(lines[index]);
				    else break;
				} else assembler.recordError("parameter " + name + " is already defined", line, linenumber);
			    } else assembler.recordError("can't parse type descriptor " + descriptor, line, linenumber);
			}
			if (consumeToken(lines, index, PATTERN_CLOSE_PAREN) != null) {
			    if (consumeToken(lines, index, PATTERN_OPEN_BRACE) != null) {
				if ((descriptor = JVMDescriptor.getDescriptorForMethod(NAME_VOID, parametertypes)) != null) {
				    if ((source = consumeMethodSource(lines, index)) != null) {
					method.storeName(methodname);
					method.storeDescriptor(descriptor);
					method.storeAccessFlags(qualifiers);
					if (qualifiers.indexOf(ATTRIBUTE_DEPRECATED.toLowerCase()) >= 0)
					    method.storeDeprecated();
					if (qualifiers.indexOf(ATTRIBUTE_SYNTHETIC.toLowerCase()) >= 0)
					    method.storeSynthetic();
					if (classfile.registerConstructor(method) == false) {
					    method.invalidate();
					    assembler.recordError("possible mismatch between constructor and its extern definition", line, linenumber);
					} else method.storeSourceCode(source, linenumber);
				    } else method.invalidate();
				} else method.invalidate();
			    } else lines[index] = line;
			} else lines[index] = line;
		    } else lines[index] = line;
		} else lines[index] = line;
	    } else lines[index] = line;
	}

	return(method);
    }


    public boolean
    consumeExternalConstructor(String lines[], int index) {

	ArrayList  parametertypes;
	Matcher    matcher;
	boolean    consumed = false;
	String     line;
	String     qualifiers;
	String     name;
	String     descriptor;

	if ((line = lines[index]) != null) {
	    if ((qualifiers = consumeToken(lines, index, PATTERN_EXTERNAL_CONSTRUCTOR_QUALIFIERS)) != null) {
		if ((name = consumeToken(lines, index, PATTERN_EXTERNAL_CONSTRUCTOR_NAME)) != null) {
		    if (consumeToken(lines, index, PATTERN_OPEN_PAREN) != null) {
			parametertypes = new ArrayList();
			matcher = PATTERN_EXTERNAL_METHOD_PARAMETER.matcher(lines[index]);
			while (matcher.find()) {
			    lines[index] = matcher.replaceFirst("");
			    descriptor = JVMDescriptor.getDescriptorForField(matcher.group(1));
			    parametertypes.add(descriptor);
			    if (consumeToken(lines, index, PATTERN_COMMA) != null)
				matcher = PATTERN_EXTERNAL_METHOD_PARAMETER.matcher(lines[index]);
			    else break;
			}
			if (consumeToken(lines, index, PATTERN_CLOSE_PAREN) != null) {
			    if (consumeLine(lines, index)) {
				descriptor = JVMDescriptor.getDescriptorForMethod(NAME_VOID, parametertypes);
				classfile.registerConstructor(name, descriptor, qualifiers);
				consumed = true;
			    } else lines[index] = line;
			} else lines[index] = line;
		    } else lines[index] = line;
		} else lines[index] = line;
	    } else lines[index] = line;
	}

	return(consumed);
    }


    public boolean
    consumeExternalField(String lines[], int index) {

	Matcher  matcher;
	boolean  consumed = false;
	String   line;
	String   qualifiers;
	String   type;
	String   name;

	if ((line = lines[index]) != null) {
	    if ((qualifiers = consumeToken(lines, index, PATTERN_EXTERNAL_FIELD_QUALIFIERS)) != null) {
		if ((type = consumeToken(lines, index, PATTERN_EXTERNAL_FIELD_TYPE)) != null) {
		    if ((name = consumeToken(lines, index, PATTERN_EXTERNAL_FIELD_NAME)) != null) {
			if (consumeLine(lines, index)) {
			    classfile.registerField(name, type, qualifiers);
			    consumed = true;
			} else lines[index] = line;
		    } else lines[index] = line;
		} else lines[index] = line;
	    } else lines[index] = line;
	}

	return(consumed);
    }


    public boolean
    consumeExternalMethod(String lines[], int index) {

	ArrayList  parametertypes;
	Matcher    matcher;
	boolean    consumed = false;
	String     line;
	String     qualifiers;
	String     type;
	String     name;
	String     descriptor;

	if ((line = lines[index]) != null) {
	    if ((qualifiers = consumeToken(lines, index, PATTERN_EXTERNAL_METHOD_QUALIFIERS)) != null) {
		if ((type = consumeToken(lines, index, PATTERN_EXTERNAL_METHOD_TYPE)) != null) {
		    if ((name = consumeToken(lines, index, PATTERN_EXTERNAL_METHOD_NAME)) != null) {
			if (consumeToken(lines, index, PATTERN_OPEN_PAREN) != null) {
			    parametertypes = new ArrayList();
			    matcher = PATTERN_EXTERNAL_METHOD_PARAMETER.matcher(lines[index]);
			    while (matcher.find()) {
				lines[index] = matcher.replaceFirst("");
				descriptor = JVMDescriptor.getDescriptorForField(matcher.group(1));
				parametertypes.add(descriptor);
				if (consumeToken(lines, index, PATTERN_COMMA) != null)
				    matcher = PATTERN_EXTERNAL_METHOD_PARAMETER.matcher(lines[index]);
				else break;
			    }
			    if (consumeToken(lines, index, PATTERN_CLOSE_PAREN) != null) {
				if (consumeLine(lines, index)) {
				    descriptor = JVMDescriptor.getDescriptorForMethod(type, parametertypes);
				    classfile.registerMethod(name, descriptor, qualifiers);
				    consumed = true;
				} else lines[index] = line;
			    } else lines[index] = line;
			} else lines[index] = line;
		    } else lines[index] = line;
		} else lines[index] = line;
	    } else lines[index] = line;
	}

	return(consumed);
    }


    public JVMField
    consumeField(String lines[], int index, int linenumber)

	throws JVMAssemblerError

    {

	ArrayList  initializers;
	JVMField   field = null;
	Object     value;
	String     line;
	String     qualifiers;
	String     type;
	String     name;
	String     descriptor;
	String     initializer;

	//
	// A null return means the line didn't look like the start of a field
	// definition. Returning an invalid field means the line looked good
	// but something else caused problems. 
	//

	if ((line = lines[index]) != null) {
	    if ((qualifiers = consumeToken(lines, index, PATTERN_FIELD_QUALIFIERS)) != null) {
		if ((type = consumeToken(lines, index, PATTERN_FIELD_TYPE)) != null) {
		    if ((name = consumeToken(lines, index, PATTERN_FIELD_NAME)) != null) {
			initializer = consumeToken(lines, index, PATTERN_FIELD_INITIALIZER);
			if (consumeLine(lines, index)) {
			    if ((descriptor = JVMDescriptor.getDescriptorForField(type)) != null) {
				field = new JVMField(classfile);
				field.storeName(name);
				field.storeDescriptor(descriptor);
				field.storeAccessFlags(qualifiers);
				if (qualifiers.indexOf(ATTRIBUTE_DEPRECATED.toLowerCase()) >= 0)
				    field.storeDeprecated();
				if (qualifiers.indexOf(ATTRIBUTE_SYNTHETIC.toLowerCase()) >= 0)
				    field.storeSynthetic();
				if (initializer != null) {
				    value = null;
				    if (descriptor.length() == 1) {
					try {
					    if (initializer.matches(REGEX_CHAR))
						initializer = javaCharacter(initializer) + "";
					    switch (descriptor.charAt(0)) {
						case DESCRIPTOR_BYTE:
						    value = new Byte(initializer);
						    break;

						case DESCRIPTOR_CHAR:
						    value = new Integer(Integer.parseInt(initializer) & 0xFFFF);
						    break;

						case DESCRIPTOR_INT:
						    value = new Integer(initializer);
						    break;

						case DESCRIPTOR_SHORT:
						    value = new Short(initializer);
						    break;

						case DESCRIPTOR_BOOLEAN:
						    value = new Integer(Integer.parseInt(initializer) != 0 ? 1 : 0);
						    break;

						case DESCRIPTOR_DOUBLE:
						    value = new Double(initializer);
						    break;

						case DESCRIPTOR_FLOAT:
						    value = new Float(initializer);
						    break;

						case DESCRIPTOR_LONG:
						    value = new Long(initializer);
						    break;

						default:
						    value = null;
						    break;
					    }
					}
					catch(NumberFormatException e) {
					    field.invalidate();
					}
				    } else if (descriptor.equals(STRING_DESCRIPTOR)) {
					if (initializer.matches(REGEX_STRING))
					    value = javaString(initializer, true);
					else if (initializer.matches(REGEX_HEX_STRING))
					    value = javaHexString(initializer);
					else field.invalidate();
				    } else if (JVMDescriptor.isArrayDescriptor(descriptor)) {
					//
					// Sufficient for now, but we eventually will want to
					// move this.
					//
					if (initializer.matches(REGEX_OPEN_BRACE)) {
					    value = consumeArrayInitializers(
						lines,
						index,
						JVMDescriptor.getDescriptorForArrayElement(descriptor),
						linenumber
					    );
					} else field.invalidate();
				    }
				    if (field.isValid())
					field.setInitialValue(value);
				}
				if (field.isValid())
				    classfile.registerField(field);
			    } else field.invalidate();
			} else lines[index] = line;
		    } else lines[index] = line;
		} else lines[index] = line;
	    } else lines[index] = line;
	}

	return(field);
    }


    public Number
    consumeInteger(String lines[], int index, long low, long high) {

	Matcher  matcher;
	Number   number = null;
	long     value;

	if (lines[index] != null) {
	    matcher = PATTERN_INTEGER.matcher(lines[index]);
	    if (matcher.find()) {
		try {
		    if ((value = Long.parseLong(matcher.group(1))) >= low && value <= high) {
			number = new Long(value);
			lines[index] = matcher.replaceFirst("");
		    }
		}
		catch(NumberFormatException e) {}
	    }
	}

	return(number);
    }


    public boolean
    consumeLine(String lines[], int index) {

	Matcher  matcher;
	boolean  consumed = false;

	if (lines[index] != null) {
	    matcher = PATTERN_WHITESPACE_OR_COMMENT.matcher(lines[index]);
	    if (matcher.find()) {
		lines[index] = null;
		consumed = true;
	    }
	} else consumed = true;

	return(consumed);
    }


    public Number
    consumeLiteralNumber(String lines[], int index) {

	Pattern  patterns[] = {PATTERN_INTEGER, PATTERN_FLOAT, PATTERN_CHAR};
	Matcher  matcher;
	Number   number = null;
	String   line;
	double   dvalue;
	long     lvalue;
	int      n;

	//
	// NOTE - PATTERN_HEX_STRING is a recent (10/3/09) addition to the
	// scanner and the check here that rejects lines that match it is
	// a small kludge that makes sure we don't think the leading 0x in
	// is an integer (or float). This method is not currently used, so
	// it could be removed and nothing would break, but we decided to
	// leave it in and add the PATTERN_HEX_STRING rejecting code just
	// in case.
	//

	if ((line = lines[index]) != null) {
	    matcher = PATTERN_HEX_STRING.matcher(line);
	    if (matcher.find() == false) {
		for (n = 0; n < patterns.length && number == null; n++) {
		    matcher = patterns[n].matcher(line);
		    if (matcher.find()) {
			if (patterns[n] == PATTERN_INTEGER) {
			    try {
				lvalue = Long.parseLong(matcher.group(1));
				if ((int)lvalue == lvalue)
				    number = new Integer((int)lvalue);
				else number = new Long(lvalue);
				lines[index] = matcher.replaceFirst("");
			    }
			    catch(NumberFormatException e) {}
			} else if (patterns[n] == PATTERN_FLOAT) {
			    try {
				dvalue = Double.parseDouble(matcher.group(1));
				if ((float)dvalue == dvalue)
				    number = new Float((float)dvalue);
				else number = new Double(dvalue);
				lines[index] = matcher.replaceFirst("");
			    }
			    catch(NumberFormatException e) {}
			} else if (patterns[n] == PATTERN_CHAR) {
			    number = new Integer(javaCharacter(matcher.group(1)));
			    lines[index] = matcher.replaceFirst("");
			} 
		    }
		}
	    }
	}

	return(number);
    }


    public Number
    consumeLocalVariable(String lines[], int index) {

	Pattern  patterns[] = {PATTERN_NAME, PATTERN_INTEGER};
	Matcher  matcher;
	Number   number = null;
	String   line;
	int      value;
	int      n;

	if ((line = lines[index]) != null) {
	    for (n = 0; n < patterns.length && number == null; n++) {
		matcher = patterns[n].matcher(line);
		if (matcher.find()) {
		    if (patterns[n] == PATTERN_INTEGER) {
			try {
			    if ((value = Integer.parseInt(matcher.group(1))) >= 0 && value <= 65535) {
				number = new Integer(value);
				lines[index] = matcher.replaceFirst("");
			    }
			}
			catch(NumberFormatException e) {}
		    } else if (patterns[n] == PATTERN_NAME) {
			if (method != null) {
			    if ((value = method.getLocalVariableIndex(matcher.group(1))) >= 0) {
				number = new Integer(value);
				lines[index] = matcher.replaceFirst("");
			    }
			}
		    }
		}
	    }
	}

	return(number);
    }


    public boolean
    consumeLocalVariableDeclaration(String lines[], int index) {

	Matcher  matcher;
	boolean  consumed = false;
	String   line;
	String   type;
	String   name;
	String   descriptor;

	if ((line = lines[index]) != null) {
	    if ((type = consumeToken(lines, index, PATTERN_LOCAL_VARIABLE_TYPE)) != null) {
		if ((name = consumeToken(lines, index, PATTERN_LOCAL_VARIABLE_NAME)) != null) {
		    if (consumeLine(lines, index)) {
			if ((descriptor = JVMDescriptor.getDescriptorForField(type)) != null) {
			    if (method.registerLocalVariable(name, descriptor) >= 0)
				consumed = true;
			} else lines[index] = line;
		    } else lines[index] = line;
		} else lines[index] = line;
	    } else lines[index] = line;
	}

	return(consumed);
    }


    public JVMMethod
    consumeMethod(String lines[], int index, int linenumber)

	throws JVMAssemblerError

    {

	JVMMethod  method = null;
	ArrayList  parametertypes;
	ArrayList  parameternames;
	Matcher    matcher;
	String     line;
	String     qualifiers;
	String     returntype;
	String     methodname;
	String     parameters;
	String     descriptor;
	String     name;
	int        access_flags;
	int        size;

	//
	// A null return means the first line didn't look like the start of
	// a method definition. Returning an invalid method means the first
	// line looked good but something in the body of the method caused
	// problems.
	//

	if ((line = lines[index]) != null) {
	    if ((qualifiers = consumeToken(lines, index, PATTERN_METHOD_QUALIFIERS)) != null) {
		if ((returntype = consumeToken(lines, index, PATTERN_METHOD_TYPE)) != null) {
		    if ((methodname = consumeToken(lines, index, PATTERN_METHOD_NAME)) != null) {
			if (consumeToken(lines, index, PATTERN_OPEN_PAREN) != null) {
			    method = new JVMMethod(classfile);
			    parameternames = new ArrayList();
			    parametertypes = new ArrayList();
			    access_flags = JVMMisc.getAccessFlags(qualifiers);
			    if ((access_flags & ACC_STATIC) == 0)
				method.registerLocalVariable(NAME_THIS, classfile.getDescriptor());
			    matcher = PATTERN_METHOD_PARAMETER.matcher(lines[index]);
			    while (matcher.find()) {
				lines[index] = matcher.replaceFirst("");
				descriptor = JVMDescriptor.getDescriptorForField(matcher.group(1));
				name = matcher.group(2);
				if ((size = JVMDescriptor.getStorageSize(descriptor)) > 0) {
				    if (method.registerLocalVariable(name, descriptor, size) >= 0) {
					parametertypes.add(descriptor);
					parameternames.add(name);
					if (consumeToken(lines, index, PATTERN_COMMA) != null)
					    matcher = PATTERN_METHOD_PARAMETER.matcher(lines[index]);
					else break;
				    } else assembler.recordError("parameter " + name + " is already defined", line, linenumber);
				} else assembler.recordError("can't parse type descriptor " + descriptor, line, linenumber);
			    }
			    if (consumeToken(lines, index, PATTERN_CLOSE_PAREN) != null) {
				if (consumeToken(lines, index, PATTERN_OPEN_BRACE) != null) {
				    if ((descriptor = JVMDescriptor.getDescriptorForMethod(returntype, parametertypes)) != null) {
					if (consumeLine(lines, index)) {
					    method.storeName(methodname);
					    method.storeDescriptor(descriptor);
					    method.storeAccessFlags(qualifiers);
					    if (qualifiers.indexOf(ATTRIBUTE_DEPRECATED.toLowerCase()) >= 0)
						method.storeDeprecated();
					    if (qualifiers.indexOf(ATTRIBUTE_SYNTHETIC.toLowerCase()) >= 0)
						method.storeSynthetic();
					    if (assembler.assembleMethod(lines, index + 1, index + 2, PATTERN_CLOSE_BRACE, method) == false)
						method.invalidate();
					} else lines[index] = line;
					if (classfile.registerMethod(method) == false) {
					    method.invalidate();
					    assembler.recordError("possible mismatch between method and its extern definition", line, linenumber);
					}
				    } else method.invalidate();
				} else lines[index] = line;
			    } else lines[index] = line;
			} else lines[index] = line;
		    } else lines[index] = line;
		} else lines[index] = line;
	    } else lines[index] = line;
	}

	return(method);
    }


    public String
    consumeMethodSource(String lines[], int index) {

	StringBuffer  sbuf;
	String        line;
	int           level;
	int           length;
	int           n;

	//
	// We collect all lines so the string that we return can accurately
	// reflect line numbers in the original source.
	//
	// NOTE - older versions looked for the opening brace, but we now
	// assume it's already been consumed.
	//

	sbuf = new StringBuffer();

	if (lines != null) {
	    if ((length = lines.length) > 0 && index < length) {
		level = 1;
		for (n = index; n < length && level > 0; n++) {
		    if ((line = lines[n]) != null) {
			if (line.indexOf("}") >= 0)
			    level--;
			else if (line.indexOf("}") >= 0)
			    level++;
			if (level > 0)
			    sbuf.append(line);
		    }
		    if (level > 0)
			sbuf.append("\n");
		    lines[n] = null;
		}
	    }
	}

	return(sbuf.toString());
    }


    public Number
    consumeQualifiedLiteralNumber(String lines[], int index) {

	Pattern  patterns[] = {PATTERN_INTEGER_QUALIFIED, PATTERN_FLOAT_QUALIFIED, PATTERN_CHAR};
	Matcher  matcher;
	Number   number = null;
	String   token;
	String   line;
	int      n;

	//
	// The caller can check the number that we return to determine the
	// kind of number that the source code requested.
	//
	// NOTE - PATTERN_HEX_STRING is a recent (10/3/09) addition to the
	// scanner and the check here that rejects lines that match it is
	// a small kludge that makes sure we don't think the leading 0x in
	// is an integer (or float). There probably are better approaches,
	// but this should be sufficient for now.
	//

	if ((line = lines[index]) != null) {
	    matcher = PATTERN_HEX_STRING.matcher(line);
	    if (matcher.find() == false) {
		for (n = 0; n < patterns.length && number == null; n++) {
		    matcher = patterns[n].matcher(line);
		    if (matcher.find()) {
			if (patterns[n] == PATTERN_INTEGER_QUALIFIED) {
			    try {
				token = matcher.group(1);
				if (token.endsWith("l") || token.endsWith("L")) {
				    token = token.substring(0, token.length() - 1);
				    number = new Long(token);
				} else if (token.endsWith("f") || token.endsWith("F")) {
				    token = token.substring(0, token.length() - 1);
				    number = new Float(token);
				} else if (token.endsWith("d") || token.endsWith("D")) {
				    token = token.substring(0, token.length() - 1);
				    number = new Double(token);
				} else number = new Integer(token);
				lines[index] = matcher.replaceFirst("");
			    }
			    catch(NumberFormatException e) {}
			} else if (patterns[n] == PATTERN_FLOAT_QUALIFIED) {
			    try {
				token = matcher.group(1);
				if (token.endsWith("f") || token.endsWith("F")) {
				    token = token.substring(0, token.length() - 1);
				    number = new Float(token);
				} else if (token.endsWith("d") || token.endsWith("D")) {
				    token = token.substring(0, token.length() - 1);
				    number = new Double(token);
				} else number = new Double(token);
				lines[index] = matcher.replaceFirst("");
			    }
			    catch(NumberFormatException e) {}
			} else if (patterns[n] == PATTERN_CHAR) {
			    number = new Integer(javaCharacter(matcher.group(1)));
			    lines[index] = matcher.replaceFirst("");
			} 
		    }
		}
	    }
	}

	return(number);
    }


    public Number
    consumeReservedWord(String lines[], int index, HashMap words) {

	Matcher  matcher;
	Number   id = null;
	String   key;

	if (lines[index] != null) {
	    matcher = PATTERN_WORD.matcher(lines[index]);
	    if (matcher.find()) {
		if ((key = matcher.group(1)) != null) {
		    if ((id = (Number)words.get(key)) != null)
			lines[index] = matcher.replaceFirst("");
		}
	    }
	}

	return(id);
    }


    public Object[]
    consumeSwitchTable(String lines[], int index, int instruction_index, int linenumber)

	throws JVMAssemblerError

    {

	ArrayList  list;
	boolean    nogaps;
	HashMap    directory;
	Integer    match;
	String     line;
	String     matchlabel;
	String     branchlabel;
	String     defaultlabel;
	Object     table[] = null;
	Object     sorted[];
	int        count;
	int        m;
	int        n;

	//
	// Reads the body of a switch marked by opening and closing braces
	// and collects the information about the match and branch labels
	// in an array that's returned to the called. Entries in the table
	// are sorted so they can be used later to build an OP_TABLESWITCH
	// or OP_LOOKUPSWITCH instruction.
	//
	// NOTE - plenty of room for improvement here, particularly in the
	// way gaps are handled. Don't think it would be hard to change our
	// gap handling code so OP_TABLESWITCH is be used when there aren't
	// many gaps.
	// 

	if ((line = lines[index]) != null) {
	    if (consumeToken(lines, index, PATTERN_OPEN_BRACE) != null) {
		if (consumeLine(lines, index)) {
		    list = new ArrayList();
		    directory = new HashMap();
		    defaultlabel = null;
		    for (index += 1; index < lines.length; index++) {
			if ((line = lines[index]) != null) {
			    if (consumeToken(lines, index, PATTERN_CLOSE_BRACE) == null) {
				if (consumeLine(lines, index) == false) {
				    if ((matchlabel = consumeToken(lines, index, PATTERN_SWITCH_LABEL)) != null) {
					try {
					    match = matchlabel.equals("default") ? null : new Integer(matchlabel);
					    if ((branchlabel = consumeBranchLabel(lines, index, instruction_index, match)) != null) {
						if (consumeLine(lines, index)) {
						    if (match != null) {
							list.add(match);
							directory.put(match, branchlabel);
						    } else {
							if (defaultlabel == null)
							    defaultlabel = branchlabel;
							else assembler.recordError("switch has multiple defaults", line, linenumber);
						    }
						} else {
						    assembler.recordError("switch syntax error", lines[index], linenumber, line);
						    lines[index] = null;
						}
					    } else {
						assembler.recordError("switch branch label is missing", lines[index], linenumber, line);
						lines[index] = null;
					    }
					}
					catch(NumberFormatException e) {}
				    } else {
					assembler.recordError("switch match label is missing", lines[index], linenumber, line);
					lines[index] = null;
				    }
				}
			    } else {
				if (consumeLine(lines, index)) {
				    if (defaultlabel != null) {
					if ((count = list.size()) > 0) {
					    sorted = list.toArray();
					    Arrays.sort(sorted);
					} else sorted = new Object[0];
					for (n = 1, nogaps = true; n < count && nogaps; n += 1)
					    nogaps = (((Integer)sorted[n]).intValue() - ((Integer)sorted[n-1]).intValue() == 1);
					table = new Object[2*count + 2];
					if (count > 0) {
					    for (n = 2, m = 0; n < table.length; n += 2, m++) {
						table[n] = sorted[m];
						table[n+1] = directory.get(sorted[m]);
					    }
					}
					table[0] = new Integer(nogaps ? 1 : 0);
					table[1] = defaultlabel;
				    } else assembler.recordError("switch without a default", line, linenumber);
				} else {
				    assembler.recordError("switch syntax error", lines[index], linenumber, line);
				    lines[index] = null;
				}
				break;
			    }
			}
		    }
		} else lines[index] = line;
	    } else lines[index] = line;
	}

	return(table);
    }


    public String
    consumeToken(String lines[], int index, Pattern pattern) {

	Matcher  matcher;
	String   token = null;

	//
	// Patterns aren't required to have groups, which is why we catch
	// IndexOutOfBoundsException and make sure we don't return null.
	//

	if (lines[index] != null) {
	    if (pattern != null) {
		matcher = pattern.matcher(lines[index]);
		if (matcher.find()) {
		    try {
			token = matcher.group(1);
		    }
		    catch(IndexOutOfBoundsException e) {
			token = "";
		    }
		    lines[index] = matcher.replaceFirst("");
		}
	    }
	}

	return(token);
    }


    public String
    consumeToken(String lines[], int index, Pattern patterns[]) {

	Matcher  matcher;
	String   token = null;
	String   line;
	int      n;

	//
	// Patterns aren't required to have groups, which is why we catch
	// IndexOutOfBoundsException and make sure we don't return null.
	//

	if ((line = lines[index]) != null) {
	    if (patterns != null) {
		for (n = 0; n < patterns.length; n++) {
		    if (patterns[n] != null) {
			matcher = patterns[n].matcher(line);
			if (matcher.find()) {
			    try {
				token = matcher.group(1);
			    }
			    catch(IndexOutOfBoundsException e) {
				token = "";
			    }
			    lines[index] = matcher.replaceFirst("");
			    break;
			}
		    }
		}
	    }
	}

	return(token);
    }


    public static String
    getNewArrayTypeName(int id) {

	return((String)BASE_TYPE_ID_MAP.get(new Integer(id)));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildScanner(JVMAssembler assembler, JVMClassFile classfile, JVMMethod method) {

	this.assembler = assembler;
	this.classfile = classfile;
	this.method = method;
    }


    private ArrayList
    consumeArrayInitializers(String lines[], int index, String descriptor, int linenumber)

	throws JVMAssemblerError

    {

	ArrayList  initializers = new ArrayList();
	Number     number;
	String     line;

	//
	// Right now we only accept one initializer per line and the values
	// are NOT separated by commas. A primitive syntax that eventually
	// may change, but at this point it was only added so the compiler
	// could easily build an array of strings that represent serialized
	// elements. Serializing the entire array and saving it as a string
	// in the class file sometimes resulted strings that exceeded the
	// limit imposed by the JVM. This approach definitely helps, but it
	// doesn't completely eliminate all string size problems, however
	// there are other things the compiler can do.
	//

	if (descriptor != null) {
	    for (index += 1; index < lines.length; index++, linenumber++) {
		if ((line = lines[index]) != null) {
		    if (consumeToken(lines, index, PATTERN_CLOSE_BRACE) == null) {
			if ((number = consumeConstantPoolLiteral(lines, index, descriptor)) != null) {
			    if (initializers != null)
				initializers.add(number);
			} else assembler.recordError("invalid initializer", lines[index], linenumber, line);
			if (consumeLine(lines, index) == false) {
			    assembler.recordError("initializer syntax error", lines[index], linenumber, line);
			    lines[index] = null;
			}
		    } else break;
		}
	    }
	}

	return(initializers != null && initializers.size() > 0 ? initializers : null);
    }


    private Number
    consumeConstantPoolLiteral(String lines[], int index, boolean wide, Pattern patterns[]) {

	Matcher  matcher;
	Number   number = null;
	String   line;
	int      pool_index;
	int      n;

	//
	// NOTE - PATTERN_HEX_STRING has to be matched first because the 0x
	// that introduces a hex string would match as an int (or float).
	//

	if (patterns != null) {
	    if ((line = lines[index]) != null) {
		for (n = 0; n < patterns.length && number == null; n++) {
		    matcher = patterns[n].matcher(line);
		    if (matcher.find()) {
			if (patterns[n] == PATTERN_INTEGER) {
			    try {
				if (wide)
				    pool_index = classfile.storeConstantPoolLong(Long.parseLong(matcher.group(1)));
				else pool_index = classfile.storeConstantPoolInt(Integer.parseInt(matcher.group(1)));
				if (pool_index > 0) {
				    number = new Integer(pool_index);
				    lines[index] = matcher.replaceFirst("");
				}
			    }
			    catch(NumberFormatException e) {}
			} else if (patterns[n] == PATTERN_FLOAT) {
			    try {
				if (wide)
				    pool_index = classfile.storeConstantPoolDouble(Double.parseDouble(matcher.group(1)));
				else pool_index = classfile.storeConstantPoolFloat(Float.parseFloat(matcher.group(1)));
				if (pool_index > 0) {
				    number = new Integer(pool_index);
				    lines[index] = matcher.replaceFirst("");
				}
			    }
			    catch(NumberFormatException e) {}
			} else if (patterns[n] == PATTERN_STRING) {
			    if ((pool_index = classfile.storeConstantPoolString(javaString(matcher.group(1), true))) > 0) {
				number = new Integer(pool_index);
				lines[index] = matcher.replaceFirst("");
			    }
			} else if (patterns[n] == PATTERN_HEX_STRING) {
			    if ((pool_index = classfile.storeConstantPoolString(javaHexString(matcher.group(1)))) > 0) {
				number = new Integer(pool_index);
				lines[index] = matcher.replaceFirst("");
			    }
			} else if (patterns[n] == PATTERN_CHAR) {
			    if ((pool_index = classfile.storeConstantPoolInt(javaCharacter(matcher.group(1)))) > 0) {
				number = new Integer(pool_index);
				lines[index] = matcher.replaceFirst("");
			    }
			} else if (patterns[n] == PATTERN_POOL_INDEX) {
			    try {
				if ((pool_index = Integer.parseInt(matcher.group(1))) >= 0 && pool_index <= 65535) {
				    number = new Integer(pool_index);
				    lines[index] = matcher.replaceFirst("");
				}
			    }
			    catch(NumberFormatException e) {}
			}
		    }
		}
	    }
	}

	return(number);
    }


    private int
    hexDigit(char ch, int value) {

	//
	// A method that was borrowed from Yoix.
	//

	if (ch > '9') {
	    if (ch >= 'A' && ch <= 'F')
		value = (ch - 'A') + 10;
	    else if (ch >= 'a' && ch <= 'f')
		value = (ch - 'a') + 10;
	} else if (ch >= '0')
	    value = ch - '0';

	return(value);
    }


    private int
    javaCharacter(String str) {

	int  value = 0;
	int  n;

	//
	// A method that was borrowed from Yoix.
	//

	str = javaString(str, true);
	for (n = 0; n < str.length(); n++)
	    value = (value << 16) | str.charAt(n);

	return(value);
    }


    private String
    javaHexString(String str) {

	byte  buf[];
	int   length;
	int   digit;
	int   m;
	int   n;

	length = str.length();
	buf = new byte[(length + 1)/2];

	for (m = 0, n = 3; n < length; n++) {
	    if ((digit = hexDigit(str.charAt(n), -1)) >= 0) {
		if (m%2 == 0)
		    buf[m/2] = (byte)(digit << 4);
		else buf[m/2] |= (byte)digit;
		m++;
	    }
	}

	return(javaString(buf, 0, (m + 1)/2));
    }


    private String
    javaString(String str, boolean delimiters) {

	char  buf[];
	char  ch;
	int   delim;
	int   digit;
	int   limit;
	int   m;
	int   n;

	//
	// A slightly modified method that was borrowed from Yoix.
	//

	if (str.indexOf('\\') >= 0) {
	    m = 0;
	    n = 0;
	    buf = str.toCharArray();
	    delim = (delimiters) ? buf[n++] : -1;
	    while (n < buf.length && (ch = buf[n++]) != delim) {
		if (ch == '\\' && n < buf.length) {
		    switch (ch = buf[n++]) {
			case 'b':
			    ch = '\b';
			    break;

			case 'f':
			    ch = '\f';
			    break;

			case 'n':
			    ch = '\n';
			    break;

			case 'r':
			    ch = '\r';
			    break;

			case 't':
			    ch = '\t';
			    break;

			case 'x':
			    ch = 0;
			    for (limit = n + 4; n < limit && n < buf.length; n++) {
				if ((digit = hexDigit(buf[n], -1)) >= 0)
				    ch = (char)((ch << 4) | digit);
				else break;
			    }
			    break;

			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			    ch = (char)octalDigit(ch, 0);
			    for (limit = n + 2; n < limit && n < buf.length; n++) {
				if ((digit = octalDigit(buf[n], -1)) >= 0)
				    ch = (char)((ch << 3) | digit);
				else break;
			    }
			    break;

			case '\r':
			    if (buf[n] == '\n')		// always true - check grammar??
				n++;
			    continue;

			case '\n':
			    continue;
		    }
		}
		buf[m++] = ch;
	    }
	    str = new String(buf, 0, m);
	} else if (delimiters) {
	    if ((n = str.lastIndexOf(str.charAt(0))) > 0)
		str = str.substring(1, n);
	    else str = str.substring(1);
	}

	return(str);
    }


    public static String
    javaString(byte bytes[], int offset, int length) {

	String  str = null;
	String  encoding = "ISO-8859-1";

	if (bytes != null) {
	    try {
		str = new String(bytes, offset, length, encoding);
	    }
	    catch(RuntimeException e) {
		try {
		    length = Math.min(bytes.length - offset, length);
		    str = new String(bytes, offset, length, encoding);
		}
		catch(UnsupportedEncodingException ee) {}
	    }
	    catch(UnsupportedEncodingException e) {}
	}

	return(str);
    }


    private int
    octalDigit(char ch, int fail) {

	//
	// A method that was borrowed from Yoix.
	//

	return((ch >= '0' && ch <= '7') ? ch - '0' : fail);
    }
}

