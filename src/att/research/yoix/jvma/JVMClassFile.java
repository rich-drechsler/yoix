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

public
class JVMClassFile

    implements JVMConstants

{

    private JVMAttributesTable  attributes = null;
    private JVMInterfacesTable  interfaces = null;
    private JVMConstantPool     constant_pool = null;
    private JVMMethodsTable     methods_table = null;
    private JVMFieldsTable      fields_table = null;

    //
    // The values assigned here are used to indicate uninitialized items,
    // so don't change them unless you know what you're doing.
    //

    private int  major_version = -1;
    private int  minor_version = -1;
    private int  access_flags = -1;
    private int  this_class = -1;
    private int  super_class = -1;

    //
    // Class and instance initialization assembler instructions are added
    // to the appropriate StringBuffer as we build a new class file and at
    // the end of the process we assemble them into methods and add them
    // to the class file.
    // 

    private StringBuffer  class_init;
    private StringBuffer  instance_init;

    //
    // These are easy to get from constant_pool, but keeping our own copy
    // eliminates the overhead and simplifies the code.
    //

    private String  this_classname = null;
    private String  super_classname = null;

    //
    // We save the path and file size info here if we successsfully loaded
    // the data from a file.
    //

    private String  classfile_source = null;
    private int     classfile_size = -1;

    //
    // We use these HashMaps to save information about fields and methods
    // that we'll need when we construct constant_pool references. It's
    // really only needed when we reference fields or methods in other
    // classes, but right now we use them for everything.
    //

    private ArrayList  constructor_list = new ArrayList();
    private HashMap    field_directory = new HashMap();
    private HashMap    method_directory = new HashMap();

    //
    // We save information about class extensions in the following HashMap,
    // which is usually filled in by assembleClass().
    //

    private HashMap  extensions = new HashMap();

    //
    // Other JVM classes can check this (via isEditable()) when they need to
    // decide whether to allow changes to already defined values.
    //

    private boolean  editable = false;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    JVMClassFile() {

	buildClassFile(null, null, -1, -1, -1, new JVMConstantPool(this));
    }


    public
    JVMClassFile(String this_name, String super_name, int major_version, int minor_version, int access_flags, JVMConstantPool constant_pool) {

	buildClassFile(this_name, super_name, major_version, minor_version, access_flags, constant_pool);
    }


    public
    JVMClassFile(InputStream stream) {

	buildClassFile(JVMMisc.readStream(stream));
    }


    public
    JVMClassFile(String path) {

	buildClassFile(JVMMisc.readFile(path), path);
    }


    public
    JVMClassFile(byte bytes[]) {

	buildClassFile(bytes);
    }

    ///////////////////////////////////
    //
    // JVMClassFile Methods
    //
    ///////////////////////////////////

    final synchronized void
    completeClassFile(JVMAssembler assembler)

	throws JVMAssemblerError

    {

	createConstructors(assembler);
	createClassInit(assembler);
    }


    public final boolean
    containsField(String name) {

	return(fields_table.contains(name));
    }


    public final boolean
    containsMethod(String name) {

	return(methods_table.contains(name));
    }


    public final boolean
    containsMethod(String name, String descriptor) {

	return(methods_table.contains(name, descriptor));
    }


    final String
    dumpClassFile() {

	return(dumpClassFile(""));
    }


    final synchronized String
    dumpClassFile(String indent) {

	StringBuffer  sbuf = new StringBuffer();
	int           byte_offset;
	int           pool_index;

	sbuf.append(indent);
	if (access_flags < 0 || (access_flags & ACC_INTERFACE) == 0)
	    sbuf.append("Class File Dump\n");
	else sbuf.append("Interface File Dump\n");
	indent += "    ";

	sbuf.append(indent);
	sbuf.append(" Major:");
	sbuf.append(" ");
	sbuf.append(major_version >= MINIMUM_MAJOR_VERSION ? major_version : DEFAULT_MAJOR_VERSION);
	sbuf.append("\n");

	sbuf.append(indent);
	sbuf.append(" Minor:");
	sbuf.append(" ");
	sbuf.append(minor_version >= MINIMUM_MINOR_VERSION ? minor_version : DEFAULT_MINOR_VERSION);
	sbuf.append("\n");

	sbuf.append(indent);
	sbuf.append("Access:");
	sbuf.append(" ");
	sbuf.append(JVMMisc.dumpAccessFlags(access_flags >= 0 ? access_flags : ACC_SUPER, true));
	sbuf.append("\n");

	sbuf.append(indent);
	sbuf.append("  This:");
	if (this_class > 0) {
	    sbuf.append(" ");
	    sbuf.append(constant_pool.getClass(this_class));
	}
	sbuf.append("\n");

	sbuf.append(indent);
	sbuf.append(" Super:");
	if (super_class > 0) {
	    sbuf.append(" ");
	    sbuf.append(constant_pool.getClass(super_class));
	}
	sbuf.append("\n");
	sbuf.append("\n");

	if (interfaces.getCount() > 0) {
	    interfaces.dumpTableInto(indent, "Interfaces:", sbuf);
	    sbuf.append("\n");
	}
	if (attributes.getCount() > 0) {
	    attributes.dumpTableInto(indent, "Attributes:", sbuf);
	    sbuf.append("\n");
	}
	if (constant_pool.getCount() > 0) {
	    constant_pool.dumpTableInto(indent, "Constant Pool:", sbuf);
	    sbuf.append("\n");
	}
	if (fields_table.getCount() > 0) {
	    fields_table.dumpTableInto(indent, "Fields Table:", sbuf);
	    sbuf.append("\n");
	}
	if (methods_table.getCount() > 0) {
	    methods_table.dumpTableInto(indent, "Methods Table:", sbuf);
	    sbuf.append("\n");
	}

	return(new String(sbuf));
    }


    final String
    dumpConstantPoolConstant(int pool_index) {

	return(constant_pool.dumpConstant(pool_index));
    }


    public final synchronized byte[]
    getBytes() {

	byte  bytes[];
	byte  table[];
	int   nextbyte;
	int   count;

	bytes = new byte[getSize()];
	nextbyte = 0;

	bytes[nextbyte++] = (byte)(MAGIC_NUMBER >> 24);
	bytes[nextbyte++] = (byte)(MAGIC_NUMBER >> 16);
	bytes[nextbyte++] = (byte)(MAGIC_NUMBER >> 8);
	bytes[nextbyte++] = (byte)MAGIC_NUMBER;

	bytes[nextbyte++] = (byte)((minor_version >= MINIMUM_MINOR_VERSION ? minor_version : DEFAULT_MINOR_VERSION) >> 8);
	bytes[nextbyte++] = (byte)(minor_version >= MINIMUM_MINOR_VERSION ? minor_version : DEFAULT_MINOR_VERSION);
	bytes[nextbyte++] = (byte)((major_version >= MINIMUM_MAJOR_VERSION ? major_version : DEFAULT_MAJOR_VERSION) >> 8);
	bytes[nextbyte++] = (byte)(major_version >= MINIMUM_MAJOR_VERSION ? major_version : DEFAULT_MAJOR_VERSION);

	count = constant_pool.getCount();
	bytes[nextbyte++] = (byte)(count >> 8);
	bytes[nextbyte++] = (byte)count;
	if (count > 0) {
	    table = constant_pool.getBytes();
	    System.arraycopy(table, 0, bytes, nextbyte, table.length);
	    nextbyte += table.length;
	}

	bytes[nextbyte++] = (byte)((access_flags >= 0 ? access_flags : ACC_SUPER) >> 8);
	bytes[nextbyte++] = (byte)(access_flags >= 0 ? access_flags : ACC_SUPER);
	bytes[nextbyte++] = (byte)(this_class >> 8);
	bytes[nextbyte++] = (byte)this_class;
	bytes[nextbyte++] = (byte)(super_class >> 8);
	bytes[nextbyte++] = (byte)super_class;

	count = interfaces.getCount();
	bytes[nextbyte++] = (byte)(count >> 8);
	bytes[nextbyte++] = (byte)count;
	if (count > 0) {
	    table = interfaces.getBytes();
	    System.arraycopy(table, 0, bytes, nextbyte, table.length);
	    nextbyte += table.length;
	}

	count = fields_table.getCount();
	bytes[nextbyte++] = (byte)(count >> 8);
	bytes[nextbyte++] = (byte)count;
	if (count > 0) {
	    table = fields_table.getBytes();
	    System.arraycopy(table, 0, bytes, nextbyte, table.length);
	    nextbyte += table.length;
	}

	count = methods_table.getCount();
	bytes[nextbyte++] = (byte)(count >> 8);
	bytes[nextbyte++] = (byte)count;
	if (count > 0) {
	    table = methods_table.getBytes();
	    System.arraycopy(table, 0, bytes, nextbyte, table.length);
	    nextbyte += table.length;
	}

	count = attributes.getCount();
	bytes[nextbyte++] = (byte)(count >> 8);
	bytes[nextbyte++] = (byte)count;
	if (count > 0) {
	    table = attributes.getBytes();
	    System.arraycopy(table, 0, bytes, nextbyte, table.length);
	    nextbyte += table.length;
	}

	return(bytes);
    }


    public final String
    getClassName() {

	return(this_classname);
    }


    final JVMConstantPool
    getConstantPool() {

	return(constant_pool);
    }


    final String
    getDescriptor() {

	return(getDescriptor(this_class));
    }


    final int
    getDescriptorCode() {

	return(JVMDescriptor.getDescriptorCode(getDescriptor()));
    }


    final String
    getDescriptor(int pool_index) {

	return(constant_pool.getDescriptor(pool_index));
    }


    final int
    getDescriptorCode(int pool_index) {

	return(JVMDescriptor.getDescriptorCode(getDescriptor(pool_index)));
    }


    final String
    getDescriptorFor(int pool_index, int tag) {

	return(constant_pool.getDescriptorFor(pool_index, tag));
    }


    final String
    getFieldDescriptor(int pool_index) {

	FieldData  data;
	String     descriptor = null;
	String     key;

	if ((key = constant_pool.getFieldKey(pool_index)) != null) {
	    if ((data = (FieldData)field_directory.get(key)) != null)
		descriptor = data.descriptor;
	}
	return(descriptor);
    }


    final String
    getFieldClassName(int pool_index) {

	FieldData  data;
	String     classname = null;
	String     key;

	if ((key = constant_pool.getFieldKey(pool_index)) != null) {
	    if ((data = (FieldData)field_directory.get(key)) != null)
		classname = data.classname;
	}
	return(classname);
    }


    final int
    getFieldDescriptorCode(int pool_index) {

	return(JVMDescriptor.getDescriptorCode(getFieldDescriptor(pool_index)));
    }


    final String
    getMethodClassName(int pool_index) {

	MethodData  data;
	String      classname = null;
	String      key;

	if ((key = constant_pool.getMethodKey(pool_index)) != null) {
	    if ((data = (MethodData)method_directory.get(key)) != null)
		classname = data.classname;
	}
	return(classname);
    }


    final String
    getMethodDescriptor(int pool_index) {

	MethodData  data;
	String      descriptor = null;
	String      key;

	if ((key = constant_pool.getMethodKey(pool_index)) != null) {
	    if ((data = (MethodData)method_directory.get(key)) != null)
		descriptor = data.descriptor;
	}
	return(descriptor);
    }


    final int
    getMethodDescriptorCode(int pool_index) {

	return(JVMDescriptor.getDescriptorCode(getMethodDescriptor(pool_index)));
    }


    final synchronized int
    getSize() {

	int  size;

	size = 10;
	size += constant_pool.getSize();
	size += 8;
	size += interfaces.getSize();
	size += 2;
	size += fields_table.getSize();
	size += 2;
	size += methods_table.getSize();
	size += 2;
	size += attributes.getSize();

	return(size);
    }


    final String
    getSuperClassName() {

	return(super_classname);
    }


    final boolean
    isEditable() {

	return(editable);
    }


    final boolean
    isFieldRef(int pool_index) {

	return(constant_pool.getTag(pool_index) == CONSTANT_FIELDREF);
    }


    final boolean
    isInterfaceMethodRef(int pool_index) {

	return(constant_pool.getTag(pool_index) == CONSTANT_INTERFACEMETHODREF);
    }


    final boolean
    isMethodRef(int pool_index) {

	int  tag = constant_pool.getTag(pool_index);

	return((tag = constant_pool.getTag(pool_index)) == CONSTANT_METHODREF || tag == CONSTANT_INTERFACEMETHODREF);
    }


    final boolean
    isPrivateClassMethod(int pool_index) {

	boolean  result = false;
	String   name;
	String   descriptor;
	int      flags;

	if (this_class == constant_pool.getClassIndexFromReference(pool_index)) {
	    if ((name = constant_pool.getMethodName(pool_index)) != null) {
		if ((descriptor = constant_pool.getDescriptor(pool_index)) != null) {
		    if ((flags = methods_table.getMethodAccessFlags(name, descriptor)) >= 0)
			result = (flags & ACC_PRIVATE) == ACC_PRIVATE;
		}
	    }
	}
	return(result);
    }


    final boolean
    isSpecialMethod(int pool_index) {

	return(isSpecialMethod(constant_pool.getMethodName(pool_index)));
    }


    private boolean
    isSpecialMethod(String name) {

	return(NAME_INIT.equals(name) || NAME_CLASS_INIT.equals(name));
    }


    final boolean
    isStaticField(int pool_index) {

	FieldData  data;
	boolean    result = false;
	String     key;

	if ((key = constant_pool.getFieldKey(pool_index)) != null) {
	    if ((data = (FieldData)field_directory.get(key)) != null)
		result = (data.access_flags & ACC_STATIC) == ACC_STATIC;
	}
	return(result);
    }


    final boolean
    isStaticMethod(int pool_index) {

	MethodData  data;
	boolean     result = false;
	String      key;

	if ((key = constant_pool.getMethodKey(pool_index)) != null) {
	    if ((data = (MethodData)method_directory.get(key)) != null)
		result = (data.access_flags & ACC_STATIC) == ACC_STATIC;
	}
	return(result);
    }


    final boolean
    isSuperClassMethod(int pool_index) {

	return(JVMMisc.isSubClass(this_classname, constant_pool.getClassNameFromReference(pool_index), extensions));
    }


    final boolean
    registerConstructor(JVMMethod method) {

	boolean  registered = false;
	String   methodname;
	String   descriptor;

	if (method != null) {
	    if ((methodname = method.getName()) != null && methodname.equals(NAME_INIT)) {
		if ((descriptor = method.getDescriptor()) != null) {
		    registered = registerConstructor(
			this_classname,
			descriptor,
			method.getAccessFlags(),
			true
		    );
		    if (registered)
			constructor_list.add(method);
		}
	    }
	}

	return(registered);
    }


    final boolean
    registerConstructor(String name, String descriptor, String qualifiers) {

	FieldData  data;
	boolean    registered = false;
	String     classname;

	//
	// This is only for external constructors.
	//

	if (name != null && descriptor != null) {
	    registered = registerConstructor(
		name,
		descriptor,
		JVMMisc.getAccessFlags(qualifiers),
		false
	    );
	}

	return(registered);
    }


    final boolean
    registerExtension(String classname, String superclass) {

	boolean  result = false;
	String   value;

	if (superclass != null) {
	    if ((value = (String)extensions.get(classname)) == null) {
		extensions.put(classname, superclass);
		result = true;
	    } else result = value.equals(superclass);
	}
	return(result);
    }


    final boolean
    registerField(JVMField field) {

	boolean  registered = false;
	String   fieldname;
	String   descriptor;

	if (field != null) {
	    if ((fieldname = field.getName()) != null) {
		if ((descriptor = field.getDescriptor()) != null) {
		    registered = registerField(
			this_classname,
			fieldname,
			descriptor,
			field.getAccessFlags(),
			true
		    );
		}
	    }
	}

	return(registered);
    }


    final boolean
    registerField(String name, String type, String qualifiers) {

	boolean  registered = false;
	String   classname;
	String   fieldname;
	String   descriptor;

	if (name != null) {
	    if ((descriptor = JVMDescriptor.getDescriptorForField(type)) != null) {
		if ((fieldname = JVMMisc.getFieldName(name)) != null) {
		    if ((classname = JVMMisc.getClassName(name, this_classname)) != null) {
			registered = registerField(
			    classname,
			    fieldname,
			    descriptor,
			    JVMMisc.getAccessFlags(qualifiers),
			    false
			);
		    }
		}
	    }
	}

	return(registered);
    }


    final boolean
    registerMethod(JVMMethod method) {

	boolean  registered = false;
	String   methodname;
	String   descriptor;

	if (method != null) {
	    if ((methodname = method.getName()) != null) {
		if ((descriptor = method.getDescriptor()) != null) {
		    registered = registerMethod(
			this_classname,
			methodname,
			descriptor,
			method.getAccessFlags(),
			true
		    );
		}
	    }
	}

	return(registered);
    }


    final boolean
    registerMethod(String name, String descriptor, String qualifiers) {

	FieldData  data;
	boolean    registered = false;
	String     classname;
	String     methodname;

	if (name != null && descriptor != null) {
	    if ((methodname = JVMMisc.getFieldName(name)) != null) {
		if ((classname = JVMMisc.getClassName(name, this_classname)) != null) {
		    registered = registerMethod(
			classname,
			methodname,
			descriptor,
			JVMMisc.getAccessFlags(qualifiers),
			false
		    );
		}
	    }
	}

	return(registered);
    }


    protected final void
    setEditable(boolean state) {

	editable = state;
    }


    final synchronized int
    storeAccessFlags(int flags) {

	if (access_flags < 0)
	    access_flags = ((flags | ACC_SUPER) & CLASS_ACCESS_MASK);
	return(access_flags);
    }


    final synchronized int
    storeAccessFlags(String list) {

	int  flags;

	if ((flags = JVMMisc.getAccessFlags(list)) > 0)
	    flags = storeAccessFlags(flags);
	return(flags);
    }


    final synchronized int
    storeClassInit(JVMAssembler assembler)

	throws JVMAssemblerError

    {

	JVMMethod  method;
	String     source;
	int        index = -1;

	if (class_init != null) {
	    source = class_init.toString();
	    method = new JVMMethod(this);
	    method.storeName(NAME_CLASS_INIT);
	    method.storeDescriptor(JVMDescriptor.getDescriptorForMethod(NAME_VOID, null));
	    method.storeAccessFlags(ACC_STATIC);
	    if (assembler.assembleMethod(source, Integer.MIN_VALUE, method)) {
		registerMethod(method);
		storeMethod(method);
	    }
	}

	return(index);
    }


    final synchronized int
    storeCode(String name, String descriptor, byte code[], int max_stack, int max_locals, int exception_table[], JVMAttributesTable attributes) {

	return(methods_table.storeCode(name, descriptor, code, max_stack, max_locals, exception_table, attributes));
    }


    final synchronized int
    storeConstantPoolArrayClass(String name) {

	return(constant_pool.storeArrayClass(name));
    }


    final synchronized int
    storeConstantPoolClass(String name) {

	return(constant_pool.storeClass(name));
    }


    final synchronized int
    storeConstantPoolDescriptor(String descriptor) {

	return(constant_pool.storeDescriptor(descriptor));
    }


    final synchronized int
    storeConstantPoolDouble(double value) {

	return(constant_pool.storeDouble(value));
    }


    final synchronized int
    storeConstantPoolFieldRef(String name) {

	FieldData  data;
	int        index = -1;

	if ((data = (FieldData)field_directory.get(name)) != null) {
	    if ((index = data.pool_index) <= 0) {
		if ((index = storeConstantPoolFieldRef(data.classname, data.fieldname, data.descriptor)) > 0)
		    data.pool_index = index;
	    }
	}
	return(index);
    }


    final synchronized int
    storeConstantPoolFieldRef(String classname, String fieldname, String descriptor) {

	return(constant_pool.storeFieldRef(classname, fieldname, descriptor));
    }


    final synchronized int
    storeConstantPoolFloat(float value) {

	return(constant_pool.storeFloat(value));
    }


    final synchronized int
    storeConstantPoolInt(int value) {

	return(constant_pool.storeInt(value));
    }


    final synchronized int
    storeConstantPoolInterfaceMethodRef(String classname, String methodname, String descriptor) {

	return(constant_pool.storeInterfaceMethodRef(classname, methodname, descriptor));
    }


    final synchronized int
    storeConstantPoolLong(long value) {

	return(constant_pool.storeLong(value));
    }


    final synchronized int
    storeConstantPoolMethodRef(String name) {

	MethodData  methoddata;
	FieldData   fielddata;
	String      classname;
	int         index = -1;

	if ((methoddata = (MethodData)method_directory.get(name)) != null) {
	    if ((index = methoddata.pool_index) <= 0) {
		if ((fielddata = (FieldData)field_directory.get(methoddata.classname)) != null)
		    classname = JVMDescriptor.getNameFromDescriptor(fielddata.descriptor);
		else classname = methoddata.classname;
		if ((index = storeConstantPoolMethodRef(classname, methoddata.methodname, methoddata.descriptor)) > 0)
		    methoddata.pool_index = index;
	    }
	}
	return(index);
    }


    final synchronized int
    storeConstantPoolMethodRef(String classname, String methodname, String descriptor) {

	return(constant_pool.storeMethodRef(classname, methodname, descriptor));
    }


    final synchronized int
    storeConstantPoolNameAndType(String name, String descriptor) {

	return(constant_pool.storeNameAndType(name, descriptor));
    }


    final synchronized int
    storeConstantPoolString(String value) {

	return(constant_pool.storeString(value));
    }


    final synchronized int
    storeConstantPoolUTF(String value) {

	return(constant_pool.storeUTF(value));
    }


    final synchronized int
    storeConstantValue(String name, double value) {

	return(fields_table.storeConstantValue(name, value));
    }


    final synchronized int
    storeConstantValue(String name, float value) {

	return(fields_table.storeConstantValue(name, value));
    }


    final synchronized int
    storeConstantValue(String name, int value) {

	return(fields_table.storeConstantValue(name, value));
    }


    final synchronized int
    storeConstantValue(String name, long value) {

	return(fields_table.storeConstantValue(name, value));
    }


    final synchronized int
    storeConstantValue(String name, String value) {

	return(fields_table.storeConstantValue(name, value));
    }


    final synchronized int
    storeDeprecatedClass() {

	return(attributes.storeDeprecated());
    }


    final synchronized int
    storeDeprecatedField(String name) {

	return(fields_table.storeDeprecated(name));
    }


    final synchronized int
    storeDeprecatedMethod(String name, String descriptor) {

	return(methods_table.storeDeprecated(name, descriptor));
    }


    final synchronized int
    storeField(JVMField field) {

	Object  value;
	int     pool_index;
	int     index;

	if ((index = fields_table.storeField(field)) >= 0) {
	    if ((value = field.getInitialValue()) != null) {
		if (field.isStatic()) {
		    if (field.isFinal() == false) {
			if (value instanceof Number) {
			    addToClassInit(NAME_PUSH + " " + value);
			    addToClassInit(NAME_STORE + " " + field.getName());
			} else if (value instanceof String) {
			    if ((pool_index = storeConstantPoolString((String)value)) > 0) {
				addToClassInit(NAME_PUSH + " #" + pool_index);
				addToClassInit(NAME_STORE + " " + field.getName());
			    }
			} else if (value instanceof ArrayList)
			    addToClassInit(getArrayInitialization(field, ((ArrayList)value).toArray()));
		    } else field.storeConstantValue(value);
		} else {
		    if (value instanceof Number) {
			addToInstanceInit(NAME_ALOAD_0);
			addToInstanceInit(NAME_PUSH + " " + value);
			addToInstanceInit(NAME_STORE + " "  + field.getName());
		    } else if (value instanceof String) {
			if ((pool_index = storeConstantPoolString((String)value)) > 0) {
			    addToInstanceInit(NAME_ALOAD_0);
			    addToInstanceInit(NAME_PUSH + " #" + pool_index);
			    addToInstanceInit(NAME_STORE + " "  + field.getName());
			}
		    } else if (value instanceof ArrayList)
			addToInstanceInit(getArrayInitialization(field, ((ArrayList)value).toArray()));
		}
	    }
	}
	return(index);
    }


    final synchronized int
    storeField(String name, String descriptor) {

	return(fields_table.storeField(name, descriptor, 0, null));
    }


    final synchronized int
    storeField(String name, String descriptor, int access_flags) {

	return(fields_table.storeField(name, descriptor, access_flags, null));
    }


    final synchronized int
    storeField(String name, String descriptor, int access_flags, JVMAttributesTable attributes) {

	return(fields_table.storeField(name, descriptor, access_flags, attributes));
    }


    final synchronized int
    storeInnerClass(String inner, String outer, String name, int access_flags) {

	return(attributes.storeInnerClass(inner, outer, name, access_flags));
    }


    final synchronized int
    storeInterface(String name) {

	return(interfaces.storeInterface(name));
    }


    final synchronized int
    storeLineNumber(String name, String descriptor, int line_number, int start_pc) {

	return(methods_table.storeLineNumber(name, descriptor, line_number, start_pc));
    }


    final synchronized int
    storeLocalVariable(String name, String descriptor, String local_name, String local_descriptor, int local_index, int start_pc, int length) {

	return(methods_table.storeLocalVariable(name, descriptor, local_name, local_descriptor, local_index, start_pc, length));
    }


    final synchronized int
    storeMajor(int major) {

	if (major_version < 0) {
	    if (major >= MINIMUM_MAJOR_VERSION)
		major_version = major;
	}
	return(major_version);
    }


    final synchronized int
    storeMethod(JVMMethod method) {

	return(methods_table.storeMethod(method));
    }


    final synchronized int
    storeMethod(String name, String descriptor) {

	return(methods_table.storeMethod(name, descriptor, 0, null));
    }


    final synchronized int
    storeMethod(String name, String descriptor, int access_flags) {

	return(methods_table.storeMethod(name, descriptor, access_flags, null));
    }


    final synchronized int
    storeMethod(String name, String descriptor, int access_flags, JVMAttributesTable attributes) {

	return(methods_table.storeMethod(name, descriptor, access_flags, attributes));
    }


    final synchronized int
    storeMinor(int minor) {

	if (minor_version < 0) {
	    if (minor >= MINIMUM_MINOR_VERSION)
		minor_version = minor;
	}
	return(minor_version);
    }


    final synchronized int
    storeSourceFile(String value) {

	return(attributes.storeSourceFile(value));
    }


    final synchronized int
    storeSuper(String name) {

	int  index;

	if (super_class <= 0) {
	    if (name != null && name.length() > 0) {
		if ((super_class = storeConstantPoolClass(name)) > 0) {
		    super_classname = constant_pool.getClassName(super_class);
		    if ((index = storeConstantPoolMethodRef(super_classname, NAME_INIT, SUPER_DESCRIPTOR)) > 0) {
			addToInstanceInit(NAME_ALOAD_0);
			addToInstanceInit(NAME_INVOKESPECIAL + " #" + index);
		    }
		}
	    }
	}
	return(super_class);
    }


    final synchronized int
    storeSyntheticClass() {

	return(attributes.storeSynthetic());
    }


    final synchronized int
    storeSyntheticField(String name) {

	return(fields_table.storeSynthetic(name));
    }


    final synchronized int
    storeSyntheticMethod(String name, String descriptor) {

	return(methods_table.storeSynthetic(name, descriptor));
    }


    final synchronized int
    storeThis(String name) {

	if (this_class <= 0) {
	    if (name != null && name.length() > 0) {
		if ((this_class = storeConstantPoolClass(name)) > 0)
		    this_classname = constant_pool.getClassName(this_class);
	    }
	}
	return(this_class);
    }


    final synchronized int
    storeThrownException(String name, String descriptor, String exception) {

	return(methods_table.storeThrownException(name, descriptor, exception));
    }


    public String
    toString() {

	return(dumpClassFile());
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private synchronized void
    addToClassInit(String instructions) {

	if (instructions != null) {
	    if (class_init == null) {
		class_init = new StringBuffer();
		class_init.append("pragma model " + EXPANDED_CODE_MODEL + "\n");
	    }
	    class_init.append(instructions);
	    if (instructions.endsWith("\n") == false)
		class_init.append("\n");
	}
    }


    private synchronized void
    addToInstanceInit(String instructions) {

	if (instructions != null) {
	    if (instance_init == null)
		instance_init = new StringBuffer();
	    instance_init.append(instructions);
	    if (instructions.endsWith("\n") == false)
		instance_init.append("\n");
	}
    }


    private void
    buildClassFile() {

	constant_pool = new JVMConstantPool(this);
	fields_table = new JVMFieldsTable(this, constant_pool);
	methods_table = new JVMMethodsTable(this, constant_pool);
	interfaces = new JVMInterfacesTable(this, constant_pool);
	attributes = new JVMAttributesTable(this, constant_pool);
	minor_version = -1;
	major_version = -1;
	access_flags = -1;
	this_class = 0;
	super_class = 0;
    }


    private void
    buildClassFile(String this_name, String super_name, int major_version, int minor_version, int access_flags, JVMConstantPool constant_pool) {

	this.constant_pool = constant_pool;
	this.major_version = storeMajor(major_version);
	this.minor_version = storeMinor(minor_version);
	this.access_flags = storeAccessFlags(access_flags);
	this.this_class = storeThis(this_name);
	this.super_class = storeSuper(super_name);

	fields_table = new JVMFieldsTable(this, constant_pool);
	methods_table = new JVMMethodsTable(this, constant_pool);
	interfaces = new JVMInterfacesTable(this, constant_pool);
	attributes = new JVMAttributesTable(this, constant_pool);
    }


    private void
    buildClassFile(byte bytes[]) {

	buildClassFile(bytes, null);
    }


    private void
    buildClassFile(byte bytes[], String source) {

	int  tablesize;
	int  count;

	if (bytes != null && bytes.length >= CLASSFILE_MINIMUM_SIZE) {
	    if (JVMMisc.getInt(bytes, CLASSFILE_MAGIC) == MAGIC_NUMBER) {
		tablesize = 0;
		minor_version = JVMMisc.getUnsignedShort(bytes, CLASSFILE_MINOR_VERSION);
		major_version = JVMMisc.getUnsignedShort(bytes, CLASSFILE_MAJOR_VERSION);
		constant_pool = new JVMConstantPool(this, bytes);
		tablesize += constant_pool.getBytesConsumed();
		access_flags = JVMMisc.getUnsignedShort(bytes, CLASSFILE_ACCESS_FLAGS + tablesize);
		this_class = JVMMisc.getUnsignedShort(bytes, CLASSFILE_THIS_CLASS + tablesize);
		super_class = JVMMisc.getUnsignedShort(bytes, CLASSFILE_SUPER_CLASS + tablesize);

		count = JVMMisc.getUnsignedShort(bytes, CLASSFILE_INTERFACES_COUNT + tablesize);
		interfaces = new JVMInterfacesTable(this, bytes, CLASSFILE_INTERFACES_TABLE + tablesize, count, constant_pool);
		tablesize += interfaces.getBytesConsumed();

		count = JVMMisc.getUnsignedShort(bytes, CLASSFILE_FIELDS_COUNT + tablesize);
		fields_table = new JVMFieldsTable(this, bytes, CLASSFILE_FIELDS_TABLE + tablesize, count, constant_pool);
		tablesize += fields_table.getBytesConsumed();

		count = JVMMisc.getUnsignedShort(bytes, CLASSFILE_METHODS_COUNT + tablesize);
		methods_table = new JVMMethodsTable(this, bytes, CLASSFILE_METHODS_TABLE + tablesize, count, constant_pool);
		tablesize += methods_table.getBytesConsumed();

		count = JVMMisc.getUnsignedShort(bytes, CLASSFILE_ATTRIBUTES_COUNT + tablesize);
		attributes = new JVMAttributesTable(this, bytes, CLASSFILE_ATTRIBUTES_TABLE + tablesize, count, constant_pool);

		this_classname = constant_pool.getClassName(this_class);
		super_classname = constant_pool.getClassName(super_class);
		classfile_source = source;
		classfile_size = bytes.length;
	    }
	}
    }


    private void
    createClassInit(JVMAssembler assembler)

	throws JVMAssemblerError

    {

	JVMMethod  method;
	String     source;

	if (class_init != null) {
	    addToClassInit(NAME_RETURN);
	    source = class_init.toString();
	    method = new JVMMethod(this);
	    method.storeName(NAME_CLASS_INIT);
	    method.storeDescriptor(JVMDescriptor.getDescriptorForMethod(NAME_VOID, null));
	    method.storeAccessFlags(ACC_STATIC);
	    if (assembler.assembleMethod(source, Integer.MIN_VALUE, method)) {
		registerMethod(method);
		storeMethod(method);
	    }
	}
    }


    private void
    createConstructors(JVMAssembler assembler)

	throws JVMAssemblerError

    {

	JVMMethod  method;
	String     source;
	String     init;
	int        linenumber;
	int        initlines;
	int        model;
	int        index;
	int        n;

	if (instance_init != null) {
	    init = instance_init.toString();
	    if ((model = assembler.getCodeModel()) != EXPANDED_CODE_MODEL) {
		init = "pragma model " + EXPANDED_CODE_MODEL + "\n" + init;
		init += "pragma model " + model;
	    }
	    for (initlines = 0, index = 0; (index = init.indexOf("\n", index) + 1) > 0; initlines++) ;
	} else {
	    init = "";
	    initlines = 0;
	}

	if (constructor_list.size() == 0) {
	    method = new JVMMethod(this);
	    method.storeName(NAME_INIT);
	    method.storeDescriptor(SUPER_DESCRIPTOR);
	    method.storeAccessFlags(ACC_PUBLIC);
	    method.registerLocalVariable(NAME_THIS, getDescriptor());
	    constructor_list.add(method);
	}

	for (n = 0; n < constructor_list.size(); n++) {
	    if ((method = (JVMMethod)constructor_list.get(n)) != null) {
		if ((source = method.getSourceCode()) == null)
		    source = "";
		linenumber = method.getSourceCodeLineNumber() - initlines;
		if (assembler.assembleMethod(init + source, linenumber, method))
		    storeMethod(method);
	    }
	}
    }


    private String
    getArrayInitialization(JVMField field, Object initializers[]) {

	StringBuffer  sbuf = new StringBuffer();
	String        descriptor;
	String        store = null;
	int           typeid;
	int           length;
	int           n;

	//
	// Does this belong somewhere else, like in JVMAssembler.java?? We
	// eventually should take another look.
	//

	if ((length = initializers.length) > 0) {
	    if ((descriptor = JVMDescriptor.getDescriptorForArrayElement(field.getDescriptor())) != null) {
		if ((typeid = JVMDescriptor.getArrayTypeIdForDescriptor(descriptor)) > 0) {
		    switch (typeid = JVMDescriptor.getArrayTypeIdForDescriptor(descriptor)) {
			case BOOLEAN_ARRAY_TYPE_ID:
			case BYTE_ARRAY_TYPE_ID:
			    store = NAME_BASTORE;
			    break;

			case CHAR_ARRAY_TYPE_ID:
			    store = NAME_CASTORE;
			    break;

			case FLOAT_ARRAY_TYPE_ID:
			    store = NAME_FASTORE;
			    break;

			case DOUBLE_ARRAY_TYPE_ID:
			    store = NAME_DASTORE;
			    break;

			case SHORT_ARRAY_TYPE_ID:
			    store = NAME_SASTORE;
			    break;

			case INT_ARRAY_TYPE_ID:
			    store = NAME_IASTORE;
			    break;

			case LONG_ARRAY_TYPE_ID:
			    store = NAME_LASTORE;
			    break;
		    }
		    sbuf.append(NAME_PUSH);
		    sbuf.append(" ");
		    sbuf.append(length);
		    sbuf.append("\n");
		    sbuf.append(NAME_NEWARRAY);
		    sbuf.append(" ");
		    sbuf.append(typeid);
		    sbuf.append("\n");
		} else {
		    //
		    // Probably only called to handle to arrays of strings, but
		    // we consiously don't make any assumptions here.
		    // 
		    store = NAME_AASTORE;
		    sbuf.append(NAME_PUSH);
		    sbuf.append(" ");
		    sbuf.append(length);
		    sbuf.append("\n");
		    sbuf.append(NAME_ANEWARRAY);
		    sbuf.append(" ");
		    sbuf.append(JVMDescriptor.getTypeNameFromDescriptor(descriptor));
		    sbuf.append("\n");
		}

		if (store != null) {
		    for (n = 0; n < length; n++) {
			sbuf.append(NAME_DUP);
			sbuf.append("\n");
			sbuf.append(NAME_PUSH);
			sbuf.append(" ");
			sbuf.append(n);
			sbuf.append("\n");
			sbuf.append(NAME_LDC);
			sbuf.append(" #");
			sbuf.append(initializers[n]);
			sbuf.append("\n");
			sbuf.append(store);
			sbuf.append("\n");
		    }
		    sbuf.append(NAME_STORE);
		    sbuf.append(" ");
		    sbuf.append(field.getName());
		    sbuf.append("\n");
		}
	    }
	}

	return(sbuf.length() > 0 ? sbuf.toString() : null);
    }


    private synchronized boolean
    registerConstructor(String classname, String descriptor, int flags, boolean resolved) {

	MethodData  data;
	boolean     registered = false;
	String      methodname;
	String      parameters;
	String      key;
	int         index;

	if ((index = descriptor.indexOf(')')) > 0) {
	    methodname = NAME_INIT;
	    parameters = descriptor.substring(0, index+1);
	    key = classname + descriptor;
	    if ((data = (MethodData)method_directory.get(key)) == null) {
		data = new MethodData(classname, methodname, descriptor, flags, resolved);
		registerData(key, data, false);
		registerData(classname + parameters, data, false);
		registerData(classname, data, false);
		registered = true;
	    } else registered = data.matches(classname, methodname, descriptor, flags, resolved);
	}

	return(registered);
    }


    private synchronized boolean
    registerData(String key, FieldData data, boolean removecollision) {

	HashMap  directory;
	boolean  registered = false;

	if (key != null) {
	    if (field_directory.containsKey(key) == false) {
		field_directory.put(key, data);
		registered = true;
	    } else if (removecollision)
		field_directory.remove(key);
	}

	return(registered);
    }


    private synchronized boolean
    registerData(String key, MethodData data, boolean removecollision) {

	HashMap  directory;
	boolean  registered = false;

	if (key != null) {
	    if (method_directory.containsKey(key) == false) {
		method_directory.put(key, data);
		registered = true;
	    } else if (removecollision)
		method_directory.remove(key);
	}

	return(registered);
    }


    private synchronized boolean
    registerField(String classname, String fieldname, String descriptor, int flags, boolean resolved) {

	FieldData  data;
	boolean    registered = false;
	String     key;

	key = classname + "." + fieldname;
	if ((data = (FieldData)field_directory.get(key)) == null) {
	    data = new FieldData(classname, fieldname, descriptor, flags, resolved);
	    registerData(key, data, false);
	    if (classname.equals(this_classname))
		registerData(fieldname, data, false);
	    registered = true;
	} else registered = data.matches(classname, fieldname, descriptor, flags, resolved);

	return(registered);
    }


    private synchronized boolean
    registerMethod(String classname, String methodname, String descriptor, int flags, boolean resolved) {

	MethodData  data;
	FieldData   fielddata;
	boolean     registered = false;
	String      parameters;
	String      key;
	int         index;

	//
	// Caller probably can assume that a false return means there was
	// a mismatch between a method's extern and actual definitions.
	//

	if ((index = descriptor.indexOf(')')) > 0) {
	    parameters = descriptor.substring(0, index+1);
	    key = classname + "." + methodname + descriptor;
	    if ((data = (MethodData)method_directory.get(key)) == null) {
		data = new MethodData(classname, methodname, descriptor, flags, resolved);
		registerData(key, data, false);
		registerData(classname + "." + methodname + parameters, data, false);
		registerData(classname + "." + methodname, data, false);
		if (classname.equals(this_classname)) {
		    registerData(methodname + descriptor, data, false);
		    registerData(methodname + parameters, data, false);
		    registerData(methodname, data, false);
		}
		if ((fielddata = (FieldData)field_directory.get(classname)) != null) {
		    key = JVMDescriptor.getNameFromDescriptor(fielddata.descriptor) + "." + methodname;
		    registerData(key + descriptor, data, false);
		    registerData(key + parameters, data, false);
		    registerData(key, data, false);
		}
		registered = true;
	    } else registered = data.matches(classname, methodname, descriptor, flags, resolved);
	}

	return(registered);
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class FieldData {

	//
	// A trivial class that's just used to record some convenient data
	// about a field. Right now there's no reason why we can't use this
	// class for methods, but that might eventually change.
	//

	boolean  resolved = false;
	boolean  external = false;
	String   classname = null;
	String   fieldname = null;
	String   descriptor = null;
	int      access_flags = 0;
	int      pool_index = -1;

	//
	// In this context these are the only bits we care about.
	//

	static final int  ACCESS_MASK = ACC_PUBLIC|ACC_PRIVATE|ACC_PROTECTED|ACC_STATIC;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	FieldData(String classname, String fieldname, String descriptor, int access_flags, boolean resolved) {

	    this.classname = classname;
	    this.fieldname = fieldname;
	    this.descriptor = descriptor;
	    this.access_flags = access_flags;
	    this.resolved = resolved;
	    this.pool_index = -1;
	    this.external = classname.equals(this_classname);
	}

	///////////////////////////////////
	//
	// FieldData Methods
	//
	///////////////////////////////////

	boolean
	matches(String classname, String fieldname, String descriptor, int access_flags, boolean resolved) {

	    boolean  matched = false;

	    if (this.classname.equals(classname)) {
		if (this.fieldname.equals(fieldname)) {
		    if (this.descriptor.equals(descriptor)) {
			if ((this.access_flags & ACCESS_MASK) == (access_flags & ACCESS_MASK)) {
			    if (resolved) {
				this.resolved = true;
				this.access_flags = access_flags;
			    }
			    matched = true;
			}
		    }
		}
	    }

	    return(matched);
	}


	public String
	toString() {

	    return("[class=" + classname + ", field=" + fieldname + ", descriptor=" + descriptor + ", flags=" + access_flags + " (" + JVMMisc.dumpAccessFlags(access_flags, false) + ")]");
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class MethodData {

	//
	// A trivial class that's just used to record some convenient data
	// about a method. Right now there's no reason why we can't use this
	// class for fields, but that might eventually change.
	//

	boolean  resolved = false;
	boolean  external = false;
	boolean  constructor = false;
	String   classname = null;
	String   methodname = null;
	String   descriptor = null;
	int      access_flags = 0;
	int      pool_index = -1;

	//
	// In this context these are the only bits we care about.
	//

	static final int  ACCESS_MASK = ACC_PUBLIC|ACC_PRIVATE|ACC_PROTECTED|ACC_STATIC;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	MethodData(String classname, String methodname, String descriptor, int access_flags, boolean resolved) {

	    this.classname = classname;
	    this.methodname = methodname;
	    this.descriptor = descriptor;
	    this.access_flags = access_flags;
	    this.resolved = resolved;
	    this.pool_index = -1;
	    this.external = classname.equals(this_classname);
	    this.constructor = methodname.equals(NAME_INIT);
	}

	///////////////////////////////////
	//
	// MethodData Methods
	//
	///////////////////////////////////

	boolean
	matches(String classname, String methodname, String descriptor, int access_flags, boolean resolved) {

	    boolean  matched = false;

	    if (this.classname.equals(classname)) {
		if (this.methodname.equals(methodname)) {
		    if (this.descriptor.equals(descriptor)) {
			if ((this.access_flags & ACCESS_MASK) == (access_flags & ACCESS_MASK)) {
			    if (resolved) {
				this.resolved = true;
				this.access_flags = access_flags;
			    }
			    matched = true;
			}
		    }
		}
	    }

	    return(matched);
	}


	public String
	toString() {

	    return("[class=" + classname + ", method=" + methodname + ", descriptor=" + descriptor + ", flags=" + JVMMisc.dumpAccessFlags(access_flags, false) + "]");
	}
    }
}

