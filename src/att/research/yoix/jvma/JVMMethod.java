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
class JVMMethod

    implements JVMConstants

{

    //
    // A simple class that's used to represent a single entry in the methods
    // table.
    //

    private JVMAttributesTable  attributes;
    private JVMConstantPool     constant_pool;
    private int                 access_flags;
    private int                 name_index;
    private int                 descriptor_index;

    //
    // Among other things we expect that the owner will eventually be used
    // to make editing decisions, provided it's not null.
    //

    private JVMClassFile  owner;

    //
    // When this is set to true then isValid() will always return false.
    //

    private boolean  invalidated = false;

    //
    // Source code for some methods can be stored here and assembled later
    // on. It currently only happens for constructors, and likely won't be
    // used for any other methods.
    //

    private String  sourcecode = null;
    private int     linenumber = -1;

    //
    // All labels are collected in the following HashMap. Each entry will be
    // an ArrayList that contains the index of the instruction at that label
    // as the first element. Objects that follow that index are one or more
    // Object arrays that individually identify every reference to the label.
    //


    //
    // Right now these are only needed while we're assembling code. Each entry
    // in label_directory will be an ArrayList that contains the index of the
    // instruction at that label as the first element. Objects that follow the
    // index are Object arrays that individually identify every reference to
    // the label.
    //

    private HashMap  label_directory = null;
    private HashMap  variable_directory = null;
    private int      max_locals = 0;
    private int      max_stack = -1;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    JVMMethod(JVMClassFile owner) {

	buildMethod(owner, -1, -1, -1, null, owner.getConstantPool());
    }


    public
    JVMMethod(JVMClassFile owner, int access_flags, int name_index, int descriptor_index, JVMConstantPool constant_pool) {

	buildMethod(owner, access_flags, name_index, descriptor_index, null, constant_pool);
    }


    public
    JVMMethod(JVMClassFile owner, int access_flags, int name_index, int descriptor_index, JVMAttributesTable attributes, JVMConstantPool constant_pool) {

	buildMethod(owner, access_flags, name_index, descriptor_index, attributes, constant_pool);
    }

    ///////////////////////////////////
    //
    // JVMMethod Methods
    //
    ///////////////////////////////////

    void
    addBranchLabelReference(String label, Object value[]) {

	ArrayList  list;

	if ((list = (ArrayList)label_directory.get(label)) == null) {
	    list = new ArrayList();
	    list.add(new Integer(-1));
	    label_directory.put(label, list);
	}
	list.add(value);
    }


    final String
    dumpMethod() {

	return(dumpMethod(""));
    }


    final String
    dumpMethod(String indent) {

	StringBuffer  sbuf = new StringBuffer();

	dumpMethodInto(indent, sbuf);
	return(sbuf.toString());
    }


    final void
    dumpMethodInto(String indent, StringBuffer sbuf) {

	sbuf.append(constant_pool.getStringFromUTF(name_index));
	sbuf.append(" ");
	sbuf.append(constant_pool.getStringFromUTF(descriptor_index));
	sbuf.append(" [");
	sbuf.append(JVMMisc.dumpAccessFlags(access_flags >= 0 ? access_flags : 0, false));
	sbuf.append("]\n");
	if (attributes != null)
	    attributes.dumpTableInto(indent + "  ", sbuf);
    }


    final int
    getAccessFlags() {

	return(access_flags);
    }


    final JVMAttributesTable
    getAttributes() {

	return(getAttributesTable());
    }


    final HashMap
    getBranchLabels() {

	return(label_directory != null ? (HashMap)label_directory.clone() : null);
    }


    final int
    getBranchTarget(String label) {

	ArrayList  list;
	Object     value;
	int        target = -1;

	if ((list = (ArrayList)label_directory.get(label)) != null) {
	    if ((value = list.get(0)) != null)
		target = ((Number)value).intValue();
	}
	return(target);
    }


    final byte[]
    getBytes() {

	byte  attr_bytes[];
	byte  bytes[];
	int   nextbyte;
	int   count;

	if (attributes != null) {
	    count = attributes.getCount();
	    attr_bytes = attributes.getBytes();
	} else {
	    count = 0;
	    attr_bytes = new byte[0];
	}

	bytes = new byte[8 + attr_bytes.length];
	nextbyte = 0;

	bytes[nextbyte++] = (byte)((access_flags >= 0 ? access_flags : 0) >> 8);
	bytes[nextbyte++] = (byte)(access_flags >= 0 ? access_flags : 0);
	bytes[nextbyte++] = (byte)(name_index >> 8);
	bytes[nextbyte++] = (byte)name_index;
	bytes[nextbyte++] = (byte)(descriptor_index >> 8);
	bytes[nextbyte++] = (byte)descriptor_index;
	bytes[nextbyte++] = (byte)(count >> 8);
	bytes[nextbyte++] = (byte)count;
	if (count > 0)
	    System.arraycopy(attr_bytes, 0, bytes, nextbyte, attr_bytes.length);

	return(bytes);
    }


    final String
    getDescriptor() {

	return(constant_pool.getStringFromUTF(descriptor_index));
    }


    final String
    getDescriptorForReturnValue() {

	return(JVMDescriptor.getDescriptorForMethodReturn(getDescriptor()));
    }


    final int
    getDescriptorCodeForReturnValue() {

	String  descriptor;
	int     code = -1;

	if ((descriptor = JVMDescriptor.getDescriptorForMethodReturn(getDescriptor())) != null)
	    code = descriptor.charAt(0);
	return(code);
    }


    final int
    getDescriptorIndex() {

	return(descriptor_index);
    }


    final String
    getLocalVariableDescriptor(int index) {

	LocalVariableData  data;
	String             descriptor = null;

	if ((data = (LocalVariableData)variable_directory.get(new Integer(index))) != null)
	    descriptor = data.descriptor;
	return(descriptor);
    }


    final int
    getLocalVariableDescriptorCode(int index) {

	LocalVariableData  data;
	int                code = -1;

	if ((data = (LocalVariableData)variable_directory.get(new Integer(index))) != null)
	    code = data.descriptor.charAt(0);
	return(code);
    }


    final String
    getLocalVariableDescriptor(String name) {

	LocalVariableData  data;
	String             descriptor = null;

	if ((data = (LocalVariableData)variable_directory.get(name)) != null)
	    descriptor = data.descriptor;
	return(descriptor);
    }


    final int
    getLocalVariableDescriptorCode(String name) {

	LocalVariableData  data;
	int                code = -1;

	if ((data = (LocalVariableData)variable_directory.get(name)) != null)
	    code = data.descriptor.charAt(0);
	return(code);
    }


    final int
    getLocalVariableIndex(String name) {

	LocalVariableData  data;
	int                index = -1;

	if ((data = (LocalVariableData)variable_directory.get(name)) != null)
	    index = data.index;
	return(index);
    }


    final String
    getLocalVariableName(int index) {

	LocalVariableData  data;
	String             name = null;

	if ((data = (LocalVariableData)variable_directory.get(new Integer(index))) != null)
	    name = data.descriptor;
	return(name);
    }


    final int
    getMaxLocals() {

	return(max_locals);
    }


    final int
    getMaxStack() {

	return(max_stack);
    }


    final String
    getName() {

	return(constant_pool.getStringFromUTF(name_index));
    }


    final int
    getNameIndex() {

	return(name_index);
    }


    final JVMClassFile
    getOwner() {

	return(owner);
    }


    final int
    getSize() {

	return(8 + (attributes != null ? attributes.getSize() : 0));
    }


    final String
    getSourceCode() {

	return(sourcecode);
    }


    final int
    getSourceCodeLineNumber() {

	return(linenumber);
    }


    final void
    invalidate() {

	invalidated = true;
    }


    final boolean
    isStatic() {

	return((access_flags & ACC_STATIC) == ACC_STATIC ? true : false);
    }


    final boolean
    isValid() {

	boolean  result = false;

	//
	// Temporary - there's much more to do here!!!
	//

	if (invalidated == false) {
	    if (owner != null) {
		if (owner.getConstantPool() == constant_pool)
		    result = (name_index > 0 && descriptor_index > 0);
	    }
	}

	return(result);
    }


    final boolean
    registerBranchLabel(String label, int target) {

	ArrayList  list;
	boolean    result = false;
	Object     value;

	if ((list = (ArrayList)label_directory.get(label)) == null) {
	    list = new ArrayList();
	    list.add(new Integer(target));
	    label_directory.put(label, list);
	    result = true;
	} else if ((value = list.get(0)) != null) {
	    if (value instanceof Number) {
		if (((Number)value).intValue() < 0) {
		    list.set(0, new Integer(target));
		    result = true;
		} else {
		    //
		    // Otherwise we're trying to redefine a label.
		    //
		}
	    } else {
		//
		// Otherwise it's an internal error....
		//
	    }
	}

	return(result);
    }


    final int
    registerLocalVariable(String name, String descriptor) {

	return(registerLocalVariable(name, descriptor, JVMDescriptor.getStorageSize(descriptor)));
    }



    final int
    registerLocalVariable(String name, String descriptor, int size) {

	LocalVariableData  data;
	int                index = -1;

	if (size > 0) {
	    if (variable_directory.containsKey(name) == false) {
		index = max_locals;
		data = new LocalVariableData(name, descriptor, index, size);
		variable_directory.put(name, data);
		variable_directory.put(new Integer(index), data);
		max_locals += size;
	    }
	}

	return(index);
    }


    final int
    storeAccessFlags(int flags) {

	if (access_flags < 0)
	    access_flags = flags & METHODS_ACCESS_MASK;
	return(access_flags);
    }


    final int
    storeAccessFlags(String list) {

	int  flags;

	if ((flags = JVMMisc.getAccessFlags(list)) >= 0)
	    flags = storeAccessFlags(flags);
	return(flags);
    }


    final int
    storeCode(byte bytecode[]) {

	//
	// Eventually will calculate max_stack etc...
	//

	return(storeCode(bytecode, max_stack, max_locals, null, null));
    }


    final int
    storeCode(byte bytecode[], int length) {

	return(storeCode(bytecode, length, null));
    }


    final int
    storeCode(byte bytecode[], int length, int exception_table[]) {

	byte  tmp[];
	int   index = -1;

	if (bytecode != null && bytecode.length > 0 && length > 0) {
	    tmp = new byte[length];
	    System.arraycopy(bytecode, 0, tmp, 0, length);
	    bytecode = tmp;
	    index = storeCode(bytecode, max_stack, max_locals, exception_table, null);
	}

	return(index);
    }


    final int
    storeCode(byte bytecode[], int max_stack, int max_locals, int exception_table[], JVMAttributesTable attributes) {

	int  index = -1;

	if (bytecode != null) {
	    if (max_stack >= 0 && max_locals >= 0)
		index = getAttributesTable().storeCode(bytecode, max_stack, max_locals, exception_table, attributes);
	}

	return(index);
    }


    final int
    storeDeprecated() {

	return(getAttributesTable().storeDeprecated());
    }


    final int
    storeDescriptor(String descriptor) {

	int  index = -1;

	if (JVMDescriptor.isMethodDescriptor(descriptor)) {
	    if (descriptor_index <= 0)
		descriptor_index = constant_pool.storeUTF(descriptor);
	    if (descriptor.equals(constant_pool.getStringFromUTF(descriptor_index)))
		index = descriptor_index;
	}

	return(index);
    }


    final int
    storeLineNumber(int line_number, int start_pc) {

	return(getAttributesTable().storeLineNumberInCode(line_number, start_pc));
    }


    final int
    storeLocalVariable(String local_name, String local_descriptor, int local_index, int start_pc, int length) {

	return(getAttributesTable().storeLocalVariableInCode(local_name, local_descriptor, local_index, start_pc, length));
    }


    final int
    storeMaxStack(int depth) {

	if (max_stack < 0)
	    max_stack = depth;
	return(max_stack);
    }


    final int
    storeName(String name) {

	int  index = -1;

	if (JVMMisc.isMethodName(name)) {
	    if (name_index <= 0)
		name_index = constant_pool.storeUTF(name);
	    if (name.equals(constant_pool.getStringFromUTF(name_index)))
		index = name_index;
	}

	return(index);
    }


    final void
    storeSourceCode(String sourcecode, int linenumber) {

	this.sourcecode = sourcecode;
	this.linenumber = linenumber;
    }


    final int
    storeSynthetic() {

	return(getAttributesTable().storeSynthetic());
    }


    final int
    storeThrownException(String exception) {

	return(getAttributesTable().storeThrownException(exception));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildMethod(JVMClassFile owner, int access_flags, int name_index, int descriptor_index, JVMAttributesTable attributes, JVMConstantPool constant_pool) {

	this.owner = owner;
	this.constant_pool = constant_pool;
	this.access_flags = (access_flags >= 0) ? (access_flags & METHODS_ACCESS_MASK) : -1;
	this.name_index = name_index;
	this.descriptor_index = descriptor_index;
	this.attributes = attributes;
	label_directory = new HashMap();
	variable_directory = new HashMap();
    }


    private synchronized JVMAttributesTable
    getAttributesTable() {

	if (attributes == null)
	    attributes = new JVMAttributesTable(owner, constant_pool);
	return(attributes);
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class LocalVariableData {

	//
	// A trivial class that's just used to record some convenient data
	// about a local variable.
	//

	String   name;
	String   descriptor;
	int      index;
	int      size;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	LocalVariableData(String name, String descriptor, int index, int size) {

	    this.name = name;
	    this.index = index;
	    this.descriptor = descriptor;
	    this.size = size;
	}

	///////////////////////////////////
	//
	// FieldData Methods
	//
	///////////////////////////////////

    }
}

