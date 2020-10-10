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
class JVMAttributesTable

    implements JVMConstants

{

    //
    // A class that's used to build a Java class file attributes table.
    //

    private JVMConstantPool  constant_pool;
    private JVMAttribute     attributes_table[];
    private int              attributes_table_count;

    //
    // Among other things we expect that the owner will eventually be used
    // to make editing decisions, provided it's not null.
    //

    private JVMClassFile  owner;

    //
    // We use a HashMap to remember the names of the attributes that should
    // only appear once in an attributes table. Done so the store methods can
    // reject any attempt to them. If we eventually want to support general
    // class file editing then we may want delete methods or perhaps just add
    // an argument to the store methods that allows replacement.
    //

    private HashMap  directory;

    //
    // If this object was initially constructed from an existing class file
    // then we record the number of bytes consumed, which in most cases will
    // match the number returned by getSize().
    //

    protected int  bytes_consumed = 0;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    JVMAttributesTable(JVMClassFile owner, JVMConstantPool constant_pool) {

	buildAttributesTable(owner, 0, constant_pool);
    }


    public
    JVMAttributesTable(JVMClassFile owner, int count, JVMConstantPool constant_pool) {

	buildAttributesTable(owner, count, constant_pool);
    }


    public
    JVMAttributesTable(JVMClassFile owner, byte bytes[], int offset, int count, JVMConstantPool constant_pool) {

	buildAttributesTable(owner, bytes, offset, count, constant_pool);
    }

    ///////////////////////////////////
    //
    // JVMAttributesTable Methods
    //
    ///////////////////////////////////

    final String
    dumpTable() {

	return(dumpTable(""));
    }


    final String
    dumpTable(String indent) {

	StringBuffer  sbuf = new StringBuffer();

	dumpTableInto(indent, sbuf);
	return(sbuf.toString());
    }


    final void
    dumpTableInto(String indent, StringBuffer sbuf) {

	dumpTableInto(indent, null, sbuf);
    }


    final void
    dumpTableInto(String indent, String header, StringBuffer sbuf) {

	String  sep = "";
	int     index;

	if (header != null) {
	    if (header.trim().length() > 0) {
		sbuf.append(indent);
		sbuf.append(header);
		indent += "    ";
	    }
	    sbuf.append("\n");
	}

	for (index = 0; index < attributes_table_count; index++)
	    attributes_table[index].dumpAttributeInto(indent, sbuf);
    }


    final byte[]
    getBytes() {

	byte  bytes[];
	byte  attribute[];
	int   nextbyte;
	int   index;

	bytes = new byte[getSize()];
	nextbyte = 0;

	for (index = 0; index < attributes_table_count; index++) {
	    attribute = attributes_table[index].getBytes();
	    System.arraycopy(attribute, 0, bytes, nextbyte, attribute.length);
	    nextbyte += attribute.length;
	}

	return(bytes);
    }


    final int
    getBytesConsumed() {

	return(bytes_consumed);
    }


    final int
    getCount() {

	return(attributes_table_count);
    }


    final int
    getSize() {

	int  index;
	int  size = 0;

	for (index = 0; index < attributes_table_count; index++)
	    size += attributes_table[index].getSize();
	return(size);
    }


    final int
    storeCode(byte code[], int max_stack, int max_locals, int exception_table[], JVMAttributesTable attributes) {

	String  key = ATTRIBUTE_CODE;
	int     attribute_index;

	if ((attribute_index = getAttributeIndex(key)) < 0) {
	    attribute_index = attributes_table_count++;
	    ensureCapacity(1);
	    attributes_table[attribute_index] = new JVMCode(
		owner,
		code,
		max_stack,
		max_locals,
		exception_table,
		attributes,
		constant_pool
	    );
	    registerAttribute(key, attribute_index);
	} else {
	    //
	    // We eventually may want to see if the attribute already stored
	    // in this table is an exact match, but for now we return -1 to
	    // tell the user that the constant value may not be stored.
	    //
	    attribute_index = -1;
	}

	return(attribute_index);
    }


    final int
    storeConstantValue(double value) {

	String  key = ATTRIBUTE_CONSTANT_VALUE;
	int     attribute_index;

	if ((attribute_index = getAttributeIndex(key)) < 0) {
	    attribute_index = attributes_table_count++;
	    ensureCapacity(1);
	    attributes_table[attribute_index] = new JVMConstantValue(owner, value, constant_pool);
	    registerAttribute(key, attribute_index);
	} else {
	    //
	    // We eventually may want to see if the attribute already stored
	    // in this table is an exact match, but for now we return -1 to
	    // tell the user that the constant value may not be stored.
	    //
	    attribute_index = -1;
	}

	return(attribute_index);
    }


    final int
    storeConstantValue(float value) {

	String  key = ATTRIBUTE_CONSTANT_VALUE;
	int     attribute_index;

	if ((attribute_index = getAttributeIndex(key)) < 0) {
	    attribute_index = attributes_table_count++;
	    ensureCapacity(1);
	    attributes_table[attribute_index] = new JVMConstantValue(owner, value, constant_pool);
	    registerAttribute(key, attribute_index);
	} else {
	    //
	    // We eventually may want to see if the attribute already stored
	    // in this table is an exact match, but for now we return -1 to
	    // tell the user that the constant value may not be stored.
	    //
	    attribute_index = -1;
	}

	return(attribute_index);
    }


    final int
    storeConstantValue(int value) {

	String  key = ATTRIBUTE_CONSTANT_VALUE;
	int     attribute_index;

	if ((attribute_index = getAttributeIndex(key)) < 0) {
	    attribute_index = attributes_table_count++;
	    ensureCapacity(1);
	    attributes_table[attribute_index] = new JVMConstantValue(owner, value, constant_pool);
	    registerAttribute(key, attribute_index);
	} else {
	    //
	    // We eventually may want to see if the attribute already stored
	    // in this table is an exact match, but for now we return -1 to
	    // tell the user that the constant value may not be stored.
	    //
	    attribute_index = -1;
	}

	return(attribute_index);
    }


    final int
    storeConstantValue(long value) {

	String  key = ATTRIBUTE_CONSTANT_VALUE;
	int     attribute_index;

	if ((attribute_index = getAttributeIndex(key)) < 0) {
	    attribute_index = attributes_table_count++;
	    ensureCapacity(1);
	    attributes_table[attribute_index] = new JVMConstantValue(owner, value, constant_pool);
	    registerAttribute(key, attribute_index);
	} else {
	    //
	    // We eventually may want to see if the attribute already stored
	    // in this table is an exact match, but for now we return -1 to
	    // tell the user that the constant value may not be stored.
	    //
	    attribute_index = -1;
	}

	return(attribute_index);
    }


    final int
    storeConstantValue(String value) {

	String  key = ATTRIBUTE_CONSTANT_VALUE;
	int     attribute_index;

	if ((attribute_index = getAttributeIndex(key)) < 0) {
	    attribute_index = attributes_table_count++;
	    ensureCapacity(1);
	    attributes_table[attribute_index] = new JVMConstantValue(owner, value, constant_pool);
	    registerAttribute(key, attribute_index);
	} else {
	    //
	    // We eventually may want to see if the attribute already stored
	    // in this table is an exact match, but for now we return -1 to
	    // tell the user that the constant value may not be stored.
	    //
	    attribute_index = -1;
	}

	return(attribute_index);
    }


    final int
    storeDeprecated() {

	String  key = ATTRIBUTE_DEPRECATED;
	int     attribute_index;

	if ((attribute_index = getAttributeIndex(key)) < 0) {
	    attribute_index = attributes_table_count++;
	    ensureCapacity(1);
	    attributes_table[attribute_index] = new JVMDeprecated(owner, constant_pool);
	    registerAttribute(key, attribute_index);
	}

	return(attribute_index);
    }


    final int
    storeInnerClass(String inner, String outer, String name, int access_flags) {

	String  key = ATTRIBUTE_INNER_CLASSES;
	int     attribute_index;
	int     inner_class_index = -1;

	if ((attribute_index = getAttributeIndex(key)) < 0) {
	    attribute_index = attributes_table_count++;
	    ensureCapacity(1);
	    attributes_table[attribute_index] = new JVMInnerClasses(owner, constant_pool);
	    registerAttribute(key, attribute_index);
	}

	if (attributes_table[attribute_index] instanceof JVMInnerClasses)
	    inner_class_index = ((JVMInnerClasses)attributes_table[attribute_index]).storeInnerClass(inner, outer, name, access_flags);
	return(inner_class_index);
    }


    final int
    storeLineNumber(int line_number, int start_pc) {

	String  key = ATTRIBUTE_LINE_NUMBER_TABLE;
	int     attribute_index;
	int     line_number_index = -1;

	if ((attribute_index = getAttributeIndex(key)) < 0) {
	    attribute_index = attributes_table_count++;
	    ensureCapacity(1);
	    attributes_table[attribute_index] = new JVMLineNumberTable(owner, constant_pool);
	    registerAttribute(key, attribute_index);
	}

	if (attributes_table[attribute_index] instanceof JVMLineNumberTable)
	    line_number_index = ((JVMLineNumberTable)attributes_table[attribute_index]).storeLineNumber(line_number, start_pc);
	return(line_number_index);
    }


    final int
    storeLineNumberInCode(int line_number, int start_pc) {

	String  key = ATTRIBUTE_CODE;
	int     attribute_index;

	if ((attribute_index = getAttributeIndex(ATTRIBUTE_CODE)) >= 0) {
	    if (attributes_table[attribute_index] instanceof JVMCode)
		attribute_index = ((JVMCode)attributes_table[attribute_index]).storeLineNumber(line_number, start_pc);
	    else attribute_index = -1;
	} else attribute_index = -1;

	return(attribute_index);
    }


    final int
    storeLocalVariable(String local_name, String local_descriptor, int local_index, int start_pc, int length) {

	String  key = ATTRIBUTE_LOCAL_VARIABLE_TABLE;
	int     attribute_index;
	int     local_variable_index = -1;

	if ((attribute_index = getAttributeIndex(key)) < 0) {
	    attribute_index = attributes_table_count++;
	    ensureCapacity(1);
	    attributes_table[attribute_index] = new JVMLocalVariableTable(owner, constant_pool);
	    registerAttribute(key, attribute_index);
	}

	if (attributes_table[attribute_index] instanceof JVMLocalVariableTable)
	    local_variable_index = ((JVMLocalVariableTable)attributes_table[attribute_index]).storeLocalVariable(local_name, local_descriptor, local_index, start_pc, length);
	return(local_variable_index);
    }


    final int
    storeLocalVariableInCode(String local_name, String local_descriptor, int local_index, int start_pc, int length) {

	String  key = ATTRIBUTE_CODE;
	int     attribute_index;

	if ((attribute_index = getAttributeIndex(ATTRIBUTE_CODE)) >= 0) {
	    if (attributes_table[attribute_index] instanceof JVMCode)
		attribute_index = ((JVMCode)attributes_table[attribute_index]).storeLocalVariable(local_name, local_descriptor, local_index, start_pc, length);
	    else attribute_index = -1;
	} else attribute_index = -1;

	return(attribute_index);
    }


    final int
    storeSourceFile(String value) {

	String  key = ATTRIBUTE_SOURCE_FILE;
	int     attribute_index;

	if ((attribute_index = getAttributeIndex(key)) < 0) {
	    attribute_index = attributes_table_count++;
	    ensureCapacity(1);
	    attributes_table[attribute_index] = new JVMSourceFile(owner, value, constant_pool);
	    registerAttribute(key, attribute_index);
	} else {
	    //
	    // We eventually may want to see if the attribute already stored
	    // in this table is an exact match, but for now we return -1 to
	    // tell the user that the constant value may not be stored.
	    //
	    attribute_index = -1;
	}

	return(attribute_index);
    }


    final int
    storeSynthetic() {

	String  key = ATTRIBUTE_SYNTHETIC;
	int     attribute_index;

	if ((attribute_index = getAttributeIndex(key)) < 0) {
	    attribute_index = attributes_table_count++;
	    ensureCapacity(1);
	    attributes_table[attribute_index] = new JVMSynthetic(owner, constant_pool);
	    registerAttribute(key, attribute_index);
	}

	return(attribute_index);
    }


    final int
    storeThrownException(String exception) {

	String  key = ATTRIBUTE_EXCEPTIONS;
	int     attribute_index;
	int     exception_index = -1;

	if ((attribute_index = getAttributeIndex(key)) < 0) {
	    attribute_index = attributes_table_count++;
	    ensureCapacity(1);
	    attributes_table[attribute_index] = new JVMExceptions(owner, constant_pool);
	    registerAttribute(key, attribute_index);
	}

	if (attributes_table[attribute_index] instanceof JVMExceptions)
	    exception_index = ((JVMExceptions)attributes_table[attribute_index]).storeException(exception);
	return(exception_index);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildAttributesTable(JVMClassFile owner, int count, JVMConstantPool constants) {

	this.owner = owner;
	this.constant_pool = constants;
	attributes_table = new JVMAttribute[count];
	attributes_table_count = count;
	directory = new HashMap();
    }


    private void
    buildAttributesTable(JVMClassFile owner, byte bytes[], int offset, int count, JVMConstantPool constants) {

	boolean  valid = true;
	String   name;
	int      initial_offset;
	int      length;
	int      index;

	initial_offset = offset;
	buildAttributesTable(owner, count, constants);

	for (index = 0; index < count && offset < bytes.length && valid; index++) {
	    name = constants.getStringFromUTF(JVMMisc.getUnsignedShort(bytes, offset));
	    length = JVMMisc.getUnsignedInt(bytes, offset + 2) + 6;
	    if (name.equals(ATTRIBUTE_CODE))
		attributes_table[index] = new JVMCode(owner, bytes, offset, constants);
	    else if (name.equals(ATTRIBUTE_CONSTANT_VALUE))
		attributes_table[index] = new JVMConstantValue(owner, bytes, offset, constants);
	    else if (name.equals(ATTRIBUTE_DEPRECATED))
		attributes_table[index] = new JVMDeprecated(owner, bytes, offset, constants);
	    else if (name.equals(ATTRIBUTE_EXCEPTIONS))
		attributes_table[index] = new JVMExceptions(owner, bytes, offset, constants);
	    else if (name.equals(ATTRIBUTE_INNER_CLASSES))
		attributes_table[index] = new JVMInnerClasses(owner, bytes, offset, constants);
	    else if (name.equals(ATTRIBUTE_LINE_NUMBER_TABLE))
		attributes_table[index] = new JVMLineNumberTable(owner, bytes, offset, constants);
	    else if (name.equals(ATTRIBUTE_LOCAL_VARIABLE_TABLE))
		attributes_table[index] = new JVMLocalVariableTable(owner, bytes, offset, constants);
	    else if (name.equals(ATTRIBUTE_SOURCE_FILE))
		attributes_table[index] = new JVMSourceFile(owner, bytes, offset, constants);
	    else if (name.equals(ATTRIBUTE_SYNTHETIC))
		attributes_table[index] = new JVMSynthetic(owner, bytes, offset, constants);
	    else attributes_table[index] = new JVMUnimplemented(owner, bytes, offset, constants);
	    registerAttribute(name, index);
	    offset += length;
	}

	bytes_consumed = offset - initial_offset;
	trimToCurrentSize();
    }


    private void
    ensureCapacity(int count) {

	JVMAttribute  tmp[];
	int           length;

	if (attributes_table_count + count > attributes_table.length) {
	    length = attributes_table.length + count;
	    tmp = new JVMAttribute[length];
	    if (attributes_table.length > 0)
		System.arraycopy(attributes_table, 0, tmp, 0, attributes_table.length);
	    attributes_table = tmp;
	}
    }


    private int
    getAttributeIndex(String key) {

	//
	// In this case the key should be one of the predefined attribute
	// names and the interpretation of this method should be that only
	// one of them is allowed per table.
	//

	return(getAttributeIndex(key, key));
    }


    private int
    getAttributeIndex(String key, String tag) {

	Object  obj;

	return((obj = directory.get(tag + ":" + key)) != null ? ((Integer)obj).intValue() : -1);
    }


    private void
    registerAttribute(String key, int index) {

	registerAttribute(key, key, index);
    }


    private void
    registerAttribute(String key, String tag, int index) {

	directory.put(tag + ":" + key, new Integer(index));
    }


    private void
    trimToCurrentSize() {

	JVMAttribute  tmp[];

	if (attributes_table_count < attributes_table.length) {
	    tmp = new JVMAttribute[attributes_table_count];
	    if (attributes_table.length > 0)
		System.arraycopy(attributes_table, 0, tmp, 0, attributes_table_count);
	    attributes_table = tmp;
	}
    }
}

