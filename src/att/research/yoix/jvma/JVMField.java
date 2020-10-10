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
class JVMField

    implements JVMConstants

{

    //
    // A simple class that's used to represent a single entry in the fields
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
    // If the field is supposed to be assigned an initial value we'll store
    // it here temporarily.
    //

    private Object  initial_value = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    JVMField(JVMClassFile owner) {

	buildField(owner, -1, -1, -1, null, owner.getConstantPool());
    }

    public
    JVMField(JVMClassFile owner, int access_flags, int name_index, int descriptor_index, JVMConstantPool constant_pool) {

	buildField(owner, access_flags, name_index, descriptor_index, null, constant_pool);
    }


    public
    JVMField(JVMClassFile owner, int access_flags, int name_index, int descriptor_index, JVMAttributesTable attributes, JVMConstantPool constant_pool) {

	buildField(owner, access_flags, name_index, descriptor_index, attributes, constant_pool);
    }

    ///////////////////////////////////
    //
    // JVMField Methods
    //
    ///////////////////////////////////

    final String
    dumpField() {

	return(dumpField(""));
    }


    final String
    dumpField(String indent) {

	StringBuffer  sbuf = new StringBuffer();

	dumpFieldInto(indent, sbuf);
	return(sbuf.toString());
    }


    final void
    dumpFieldInto(String indent, StringBuffer sbuf) {

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


    final int
    getDescriptorIndex() {

	return(descriptor_index);
    }


    final Object
    getInitialValue() {

	return(initial_value);
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


    final void
    invalidate() {

	invalidated = true;
    }


    final boolean
    isFinal() {

	return((access_flags & ACC_FINAL) == ACC_FINAL ? true : false);
    }


    final boolean
    isStatic() {

	return((access_flags & ACC_STATIC) == ACC_STATIC ? true : false);
    }


    final boolean
    isValid() {

	boolean  result = false;

	if (invalidated == false) {
	    if (owner != null) {
		if (owner.getConstantPool() == constant_pool)
		    result = (name_index > 0 && descriptor_index > 0);
	    }
	}

	return(result);
    }


    final void
    setInitialValue(Object value) {

	initial_value = value;
    }


    final int
    storeAccessFlags(int flags) {

	if (access_flags < 0)
	    access_flags = flags & FIELDS_ACCESS_MASK;
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
    storeConstantValue(double value) {

	return(getAttributesTable().storeConstantValue(value));
    }


    final int
    storeConstantValue(float value) {

	return(getAttributesTable().storeConstantValue(value));
    }


    final int
    storeConstantValue(int value) {

	return(getAttributesTable().storeConstantValue(value));
    }


    final int
    storeConstantValue(long value) {

	return(getAttributesTable().storeConstantValue(value));
    }


    final int
    storeConstantValue(String value) {

	return(getAttributesTable().storeConstantValue(value));
    }


    final int
    storeConstantValue(Object value) {

	int  index = -1;

	if (value != null) {
	    if (value instanceof String)
		index = storeConstantValue((String)value);
	    else if (value instanceof Integer)
		index = storeConstantValue(((Integer)value).intValue());
	    else if (value instanceof Double)
		index = storeConstantValue(((Double)value).doubleValue());
	    else if (value instanceof Long)
		 index = storeConstantValue(((Long)value).longValue());
	    else if (value instanceof Float)
		index = storeConstantValue(((Float)value).floatValue());
	    else if (value instanceof Number)
		index = storeConstantValue(((Number)value).intValue());
	} else index = -1;

	return(index);
    }


    final int
    storeDeprecated() {

	return(getAttributesTable().storeDeprecated());
    }


    final int
    storeDescriptor(String descriptor) {

	int  index = -1;

	if (JVMDescriptor.isFieldDescriptor(descriptor)) {
	    if (descriptor_index <= 0)
		descriptor_index = constant_pool.storeUTF(descriptor);
	    if (descriptor.equals(constant_pool.getStringFromUTF(descriptor_index)))
		index = descriptor_index;
	}

	return(index);
    }


    final int
    storeName(String name) {

	int  index = -1;

	if (JVMMisc.isIdentifier(name)) {
	    if (name_index <= 0)
		name_index = constant_pool.storeUTF(name);
	    if (name.equals(constant_pool.getStringFromUTF(name_index)))
		index = name_index;
	}

	return(index);
    }


    final int
    storeSynthetic() {

	return(getAttributesTable().storeSynthetic());
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildField(JVMClassFile owner, int access_flags, int name_index, int descriptor_index, JVMAttributesTable attributes, JVMConstantPool constant_pool) {

	this.owner = owner;
	this.constant_pool = constant_pool;
	this.access_flags = (access_flags >= 0) ? (access_flags & FIELDS_ACCESS_MASK) : -1;
	this.name_index = name_index;
	this.descriptor_index = descriptor_index;
	this.attributes = attributes;
    }


    private synchronized JVMAttributesTable
    getAttributesTable() {

	if (attributes == null)
	    attributes = new JVMAttributesTable(owner, constant_pool);
	return(attributes);
    }
}

