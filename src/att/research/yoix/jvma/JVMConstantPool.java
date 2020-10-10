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
class JVMConstantPool

    implements JVMConstants

{

    //
    // A class that's used to build a Java class file constant pool, which
    // is where strings, class and interface names, field names, and other
    // constants referenced in the class file are stored.
    //
    // NOTE - testing long code via Yoix builtins can be tricky because Yoix
    // doesn't support the long type. You may want to use something like
    //
    //     storeConstantPoolLong(4294967295.0);
    //     dumpConstantPool();
    //
    // to test sign extension when you're using the yoix.jvm module.
    //

    private byte  constant_pool[];
    private int   constant_pool_count;
    private int   nextbyte;

    //
    // Among other things we expect that the owner will eventually be used
    // to make editing decisions, provided it's not null.
    //

    private JVMClassFile  owner;

    //
    // We use a HashMap to map the "names" of items stored in the constant
    // pool to their index. Right now its only purpose is to make sure we
    // don't store the exact same constant more than once.
    //
 
    private HashMap  directory;

    //
    // We use this HashMap whenever we want to map a constant pool index to
    // the byte in attributes_table where it starts.
    //

    private HashMap  bytemap;

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
    JVMConstantPool(JVMClassFile owner) {

	buildConstantPool(owner, 0);
    }


    public
    JVMConstantPool(JVMClassFile owner, int size) {

	buildConstantPool(owner, size);
    }


    public
    JVMConstantPool(JVMClassFile owner, byte classfile[]) {

	buildConstantPool(
	    owner,
	    classfile,
	    CLASSFILE_CONSTANT_POOL_TABLE,
	    JVMMisc.getUnsignedShort(classfile, CLASSFILE_CONSTANT_POOL_COUNT)
	);
    }


    public
    JVMConstantPool(JVMClassFile owner, byte bytes[], int offset, int count) {

	buildConstantPool(owner, bytes, offset, count);
    }

    ///////////////////////////////////
    //
    // JVMConstantPool Methods
    //
    ///////////////////////////////////

    final String
    dumpConstant(int pool_index) {

	StringBuffer  sbuf = new StringBuffer();

	dumpConstantInto(pool_index, sbuf);
	return(sbuf.toString());
    }


    final void
    dumpConstantInto(int pool_index, StringBuffer sbuf) {

	int  tag;

	if ((tag = getTag(pool_index)) >= 0) {
	    switch (tag) {
		case CONSTANT_CLASS:
		    sbuf.append(getClass(pool_index));
		    break;

		case CONSTANT_DOUBLE:
		    sbuf.append(getDouble(pool_index));
		    break;

		case CONSTANT_FIELDREF:
		case CONSTANT_INTERFACEMETHODREF:
		case CONSTANT_METHODREF:
		    sbuf.append(getClassFromReference(pool_index));
		    sbuf.append(" ");
		    sbuf.append(getNameFromReference(pool_index));
		    sbuf.append(" ");
		    sbuf.append(getTypeFromReference(pool_index));
		    break;

		case CONSTANT_FLOAT:
		    sbuf.append(getFloat(pool_index));
		    break;

		case CONSTANT_INTEGER:
		    sbuf.append(getInt(pool_index));
		    break;

		case CONSTANT_LONG:
		    sbuf.append(getLong(pool_index));
		    break;

		case CONSTANT_NAMEANDTYPE:
		    sbuf.append(getNameFromNameAndType(pool_index));
		    sbuf.append(" ");
		    sbuf.append(getTypeFromNameAndType(pool_index));
		    break;

		case CONSTANT_STRING:
		    sbuf.append(getString(pool_index));
		    break;

		case CONSTANT_UTF8:
		    sbuf.append(getStringFromUTF(pool_index));
		    break;
	    }
	}
    }


    final String
    dumpTable() {

	return(dumpTable(""));
    }


    final String
    dumpTable(String indent) {

	StringBuffer  sbuf = new StringBuffer();

	dumpTableInto(indent, null, sbuf);
	return(sbuf.toString());
    }


    final void
    dumpTableInto(String indent, String header, StringBuffer sbuf) {

	int  pool_index;
	int  tag;

	if (header != null) {
	    if (header.trim().length() > 0) {
		sbuf.append(indent);
		sbuf.append(header);
		indent += "  ";
	    }
	    sbuf.append("\n");
	}

	for (pool_index = 1; pool_index < constant_pool_count; pool_index++) {
	    if ((tag = getTag(pool_index)) >= 0) {
		sbuf.append(indent);
		JVMMisc.appendRightAlignedInt(sbuf, pool_index, 5, ": ");
		sbuf.append("[");
		sbuf.append(getTagAbbreviation(tag));
		sbuf.append("] ");
		dumpConstantInto(pool_index, sbuf);
		sbuf.append("\n");
	    }
	}
    }


    final byte[]
    getBytes() {

	byte  tmp[];
	int   length = nextbyte;

	tmp = new byte[length];
	if (length > 0)
	    System.arraycopy(constant_pool, 0, tmp, 0, length);
	return(tmp);
    }


    final int
    getBytesConsumed() {

	return(bytes_consumed);
    }


    final String
    getClass(int pool_index) {

	int  byte_offset;

	return((byte_offset = getByteOffset(pool_index)) >= 0
	    ? getStringFromUTF(JVMMisc.getUnsignedShort(constant_pool, byte_offset + 1))
	    : null
	);
    }


    final String
    getClassFromReference(int pool_index) {

	int  byte_offset;

	return((byte_offset = getByteOffset(pool_index)) >= 0
	    ? getClass(JVMMisc.getUnsignedShort(constant_pool, byte_offset + 1))
	    : null
	);
    }


    final int
    getClassIndexFromReference(int pool_index) {

	int  byte_offset;

	return((byte_offset = getByteOffset(pool_index)) >= 0
	    ? JVMMisc.getUnsignedShort(constant_pool, byte_offset + 1)
	    : -1
	);
    }


    final String
    getClassNameFromReference(int pool_index) {

	int  byte_offset;

	return((byte_offset = getByteOffset(pool_index)) >= 0
	    ? getClassName(JVMMisc.getUnsignedShort(constant_pool, byte_offset + 1))
	    : null
	);
    }


    final String
    getClassName(int pool_index) {

	String  name;

	if ((name = getClass(pool_index)) != null)
	    name = name.replace('/', '.');
	return(name);
    }


    final int
    getCount() {

	return(constant_pool_count);
    }


    final String
    getDescriptor(int pool_index) {

	String  descriptor = null;

	switch (getTag(pool_index)) {
	    case CONSTANT_CLASS:
		descriptor = getClass(pool_index);
		if (JVMDescriptor.isArrayDescriptor(descriptor) == false)
		    descriptor = CLASS_DESCRIPTOR + descriptor + ";";
		break;

	    case CONSTANT_DOUBLE:
		descriptor = DOUBLE_DESCRIPTOR;
		break;

	    case CONSTANT_FIELDREF:
	    case CONSTANT_INTERFACEMETHODREF:
	    case CONSTANT_METHODREF:
		descriptor = getTypeFromReference(pool_index);
		break;

	    case CONSTANT_FLOAT:
		descriptor = FLOAT_DESCRIPTOR;
		break;

	    case CONSTANT_INTEGER:
		descriptor = INT_DESCRIPTOR;
		break;

	    case CONSTANT_LONG:
		descriptor = LONG_DESCRIPTOR;
		break;

	    case CONSTANT_NAMEANDTYPE:
		descriptor = getTypeFromNameAndType(pool_index);
		break;

	    case CONSTANT_STRING:
		descriptor = STRING_DESCRIPTOR;
		break;

	    default:
		descriptor = null;
		break;
	}

	return(descriptor);
    }


    final String
    getDescriptorFor(int pool_index, int tag) {

	return(getTag(pool_index) == tag ? getDescriptor(pool_index) : null);
    }


    final double
    getDouble(int pool_index) {

	int  byte_offset;

	return((byte_offset = getByteOffset(pool_index)) >= 0
	    ? JVMMisc.getDouble(constant_pool, byte_offset + 1)
	    : 0
	);
    }


    final String
    getFieldKey(int pool_index) {

	String  key = null;
	String  classname;
	String  fieldname;

	if (getTag(pool_index) == CONSTANT_FIELDREF) {
	    if ((classname = getClassNameFromReference(pool_index)) != null) {
		if ((fieldname = getFieldName(pool_index)) != null)
		    key = classname + "." + fieldname;
	    }
	}

	return(key);
    }


    final String
    getFieldName(int pool_index) {

	return(getTag(pool_index) == CONSTANT_FIELDREF ? getNameFromReference(pool_index) : null);
    }


    final float
    getFloat(int pool_index) {

	int  byte_offset;

	return((byte_offset = getByteOffset(pool_index)) >= 0
	    ? JVMMisc.getFloat(constant_pool, byte_offset + 1)
	    : 0
	);
    }


    final int
    getInt(int pool_index) {

	int  byte_offset;

	return((byte_offset = getByteOffset(pool_index)) >= 0
	    ? JVMMisc.getInt(constant_pool, byte_offset + 1)
	    : 0
	);
    }


    final long
    getLong(int pool_index) {

	int  byte_offset;

	return((byte_offset = getByteOffset(pool_index)) >= 0
	    ? JVMMisc.getLong(constant_pool, byte_offset + 1)
	    : 0
	);
    }


    final String
    getMethodKey(int pool_index) {

	String  key = null;
	String  classname;
	String  descriptor;
	String  methodname;
	int     tag;

	if ((tag = getTag(pool_index)) == CONSTANT_METHODREF || tag == CONSTANT_INTERFACEMETHODREF) {
	    if ((classname = getClassNameFromReference(pool_index)) != null) {
		if ((descriptor = getTypeFromReference(pool_index)) != null) {
		    if ((methodname = getMethodName(pool_index)) != null)
			key = classname + "." + methodname + descriptor;
		}
	    }
	}

	return(key);
    }


    final String
    getMethodName(int pool_index) {

	int  tag = getTag(pool_index);

	return(tag == CONSTANT_METHODREF || tag == CONSTANT_INTERFACEMETHODREF ? getNameFromReference(pool_index) : null);
    }


    final String
    getNameFromNameAndType(int pool_index) {

	int  byte_offset;

	return((byte_offset = getByteOffset(pool_index)) >= 0
	    ? getStringFromUTF(JVMMisc.getUnsignedShort(constant_pool, byte_offset + 1))
	    : null
	);
    }


    final String
    getNameFromReference(int pool_index) {

	int  byte_offset;

	return((byte_offset = getByteOffset(pool_index)) >= 0
	    ? getNameFromNameAndType(JVMMisc.getUnsignedShort(constant_pool, byte_offset + 3))
	    : null
	);
    }


    final int
    getSize() {

	return(nextbyte);
    }


    final String
    getString(int pool_index) {

	int  byte_offset;

	return((byte_offset = getByteOffset(pool_index)) >= 0
	    ? getStringFromUTF(JVMMisc.getUnsignedShort(constant_pool, byte_offset + 1))
	    : null
	);
    }


    final String
    getStringFromUTF(int pool_index) {

	int  byte_offset;

	return((byte_offset = getByteOffset(pool_index)) >= 0
	    ? JVMMisc.getStringFromUTF(constant_pool, byte_offset + 3, JVMMisc.getUnsignedShort(constant_pool, byte_offset + 1))
	    : null
	);
    }


    final int
    getTag(int pool_index) {

	int  byte_offset;
	int  tag;

	if ((byte_offset = getByteOffset(pool_index)) >= 0)
	    tag = constant_pool[byte_offset];
	else tag = -1;

	return(tag);
    }


    final String
    getTypeFromNameAndType(int pool_index) {

	int  byte_offset;

	return((byte_offset = getByteOffset(pool_index)) >= 0
	    ? getStringFromUTF(JVMMisc.getUnsignedShort(constant_pool, byte_offset + 3))
	    : null
	);
    }


    final String
    getTypeFromReference(int pool_index) {

	int  byte_offset;

	return((byte_offset = getByteOffset(pool_index)) >= 0
	    ? getTypeFromNameAndType(JVMMisc.getUnsignedShort(constant_pool, byte_offset + 3))
	    : null
	);
    }


    final int
    storeArrayClass(String value) {

	return(storeArrayClass(value.replace('.', '/'), value));
    }


    final int
    storeArrayClass(String key, String name) {

	int  byte_offset;
	int  pool_index;
	int  name_index;

	if ((pool_index = getConstantPoolIndex(key, CONSTANT_CLASS)) < 0) {
	    name = name.replace('.', '/');
	    if (JVMDescriptor.isArrayDescriptor(name)) {
		if ((name_index = storeUTF(name)) >= 0) {
		    pool_index = constant_pool_count++;
		    byte_offset = nextbyte;
		    ensureCapacityFor(CONSTANT_CLASS);
		    constant_pool[nextbyte++] = CONSTANT_CLASS;
		    constant_pool[nextbyte++] = (byte)(name_index >> 8);
		    constant_pool[nextbyte++] = (byte)name_index;
		    registerConstant(key, pool_index, byte_offset);
		}
	    }
	}

	return(pool_index);
    }


    final int
    storeClass(String value) {

	return(storeClass(value.replace('.', '/'), value));
    }


    final int
    storeClass(String key, String name) {

	int  byte_offset;
	int  pool_index;
	int  name_index;

	if ((pool_index = getConstantPoolIndex(key, CONSTANT_CLASS)) < 0) {
	    name = name.replace('.', '/');
	    if (JVMMisc.isClassName(name)) {
		if ((name_index = storeUTF(name)) >= 0) {
		    pool_index = constant_pool_count++;
		    byte_offset = nextbyte;
		    ensureCapacityFor(CONSTANT_CLASS);
		    constant_pool[nextbyte++] = CONSTANT_CLASS;
		    constant_pool[nextbyte++] = (byte)(name_index >> 8);
		    constant_pool[nextbyte++] = (byte)name_index;
		    registerConstant(key, pool_index, byte_offset);
		}
	    }
	}

	return(pool_index);
    }


    final int
    storeDescriptor(String descriptor) {

	return(storeDescriptor(descriptor, descriptor));
    }


    final int
    storeDescriptor(String key, String descriptor) {

	return(JVMDescriptor.isDescriptor(descriptor) ? storeUTF(key, descriptor) : -1);
    }


    final int
    storeDouble(double value) {

	return(storeDouble(Double.toString(value), value));
    }


    final int
    storeDouble(String key, double value) {

	long  bits;
	int   byte_offset;
	int   pool_index;

	if ((pool_index = getConstantPoolIndex(key, CONSTANT_DOUBLE)) < 0) {
	    pool_index = constant_pool_count++;
	    byte_offset = nextbyte;
	    bits = Double.doubleToRawLongBits(value);
	    ensureCapacityFor(CONSTANT_DOUBLE);
	    constant_pool[nextbyte++] = CONSTANT_DOUBLE;
	    constant_pool[nextbyte++] = (byte)(bits >> 56);
	    constant_pool[nextbyte++] = (byte)(bits >> 48);
	    constant_pool[nextbyte++] = (byte)(bits >> 40);
	    constant_pool[nextbyte++] = (byte)(bits >> 32);
	    constant_pool[nextbyte++] = (byte)(bits >> 24);
	    constant_pool[nextbyte++] = (byte)(bits >> 16);
	    constant_pool[nextbyte++] = (byte)(bits >> 8);
	    constant_pool[nextbyte++] = (byte)bits;
	    registerConstant(key, pool_index, byte_offset);
	    //
	    // Think all we have to do here to account for the "phantom" slot
	    // is increment constant_pool_count. See the section on longs and
	    // doubles in the constant pool section of the JVM documentation.
	    //
	    constant_pool_count++;
	}

	return(pool_index);
    }


    final int
    storeFieldRef(String classname, String name, String descriptor) {

	return(storeFieldRef(classname + "+" + name + "@" + descriptor, classname, name, descriptor));
    }


    final int
    storeFieldRef(String key, String classname, String name, String descriptor) {

	return(storeReference(CONSTANT_FIELDREF, key, classname, name, descriptor));
    }


    final int
    storeFloat(float value) {

	return(storeFloat(Float.toString(value), value));
    }


    final int
    storeFloat(String key, float value) {

	int  bits;
	int  byte_offset;
	int  pool_index;

	if ((pool_index = getConstantPoolIndex(key, CONSTANT_FLOAT)) < 0) {
	    pool_index = constant_pool_count++;
	    byte_offset = nextbyte;
	    bits = Float.floatToRawIntBits(value);
	    ensureCapacityFor(CONSTANT_FLOAT);
	    constant_pool[nextbyte++] = CONSTANT_FLOAT;
	    constant_pool[nextbyte++] = (byte)(bits >> 24);
	    constant_pool[nextbyte++] = (byte)(bits >> 16);
	    constant_pool[nextbyte++] = (byte)(bits >> 8);
	    constant_pool[nextbyte++] = (byte)bits;
	    registerConstant(key, pool_index, byte_offset);
	}

	return(pool_index);
    }


    final int
    storeInt(int value) {

	return(storeInt(Integer.toString(value), value));
    }


    final int
    storeInt(String key, int value) {

	int  bits;
	int  byte_offset;
	int  pool_index;

	if ((pool_index = getConstantPoolIndex(key, CONSTANT_INTEGER)) < 0) {
	    pool_index = constant_pool_count++;
	    byte_offset = nextbyte;
	    bits = value;
	    ensureCapacityFor(CONSTANT_INTEGER);
	    constant_pool[nextbyte++] = CONSTANT_INTEGER;
	    constant_pool[nextbyte++] = (byte)(bits >> 24);
	    constant_pool[nextbyte++] = (byte)(bits >> 16);
	    constant_pool[nextbyte++] = (byte)(bits >> 8);
	    constant_pool[nextbyte++] = (byte)bits;
	    registerConstant(key, pool_index, byte_offset);
	}

	return(pool_index);
    }


    final int
    storeInterfaceMethodRef(String classname, String name, String descriptor) {

	return(storeInterfaceMethodRef(classname + "+" + name + "@" + descriptor, classname, name, descriptor));
    }


    final int
    storeInterfaceMethodRef(String key, String classname, String name, String descriptor) {

	return(storeReference(CONSTANT_INTERFACEMETHODREF, key, classname, name, descriptor));
    }


    final int
    storeLong(long value) {

	return(storeLong(Long.toString(value), value));
    }


    final int
    storeLong(String key, long value) {

	long  bits;
	int   byte_offset;
	int   pool_index;

	if ((pool_index = getConstantPoolIndex(key, CONSTANT_LONG)) < 0) {
	    pool_index = constant_pool_count++;
	    byte_offset = nextbyte;
	    bits = value;
	    ensureCapacityFor(CONSTANT_LONG);
	    constant_pool[nextbyte++] = CONSTANT_LONG;
	    constant_pool[nextbyte++] = (byte)(bits >> 56);
	    constant_pool[nextbyte++] = (byte)(bits >> 48);
	    constant_pool[nextbyte++] = (byte)(bits >> 40);
	    constant_pool[nextbyte++] = (byte)(bits >> 32);
	    constant_pool[nextbyte++] = (byte)(bits >> 24);
	    constant_pool[nextbyte++] = (byte)(bits >> 16);
	    constant_pool[nextbyte++] = (byte)(bits >> 8);
	    constant_pool[nextbyte++] = (byte)bits;
	    registerConstant(key, pool_index, byte_offset);
	    //
	    // Think all we have to do here to account for the "phantom" slot
	    // is increment constant_pool_count. See the section on longs and
	    // doubles in the constant pool section of the JVM documentation.
	    //
	    constant_pool_count++;
	}

	return(pool_index);
    }


    final int
    storeMethodRef(String classname, String name, String descriptor) {

	return(storeMethodRef(classname + "+" + name + "@" + descriptor, classname, name, descriptor));
    }


    final int
    storeMethodRef(String key, String classname, String name, String descriptor) {

	return(storeReference(CONSTANT_METHODREF, key, classname, name, descriptor));
    }


    final int
    storeName(String name) {

	return(storeName(name, name));
    }


    final int
    storeName(String key, String name) {

	return(JVMMisc.isIdentifier(name) || JVMMisc.isSpecialConstructorName(name) ? storeUTF(key, name) : -1);
    }


    final int
    storeNameAndType(String name, String descriptor) {

	return(storeNameAndType(name + "@" + descriptor, name, descriptor));
    }


    final int
    storeNameAndType(String key, String name, String descriptor) {

	int  byte_offset;
	int  pool_index;
	int  name_index;
	int  descriptor_index;
	
	if ((pool_index = getConstantPoolIndex(key, CONSTANT_NAMEANDTYPE)) < 0) {
	    if (JVMMisc.isIdentifier(name) || JVMMisc.isSpecialConstructorName(name)) {
		if (JVMDescriptor.isDescriptor(descriptor)) {
		    if ((name_index = storeName(name)) >= 0) {
			if ((descriptor_index = storeDescriptor(descriptor)) >= 0) {
			    pool_index = constant_pool_count++;
			    byte_offset = nextbyte;
			    ensureCapacityFor(CONSTANT_NAMEANDTYPE);
			    constant_pool[nextbyte++] = CONSTANT_NAMEANDTYPE;
			    constant_pool[nextbyte++] = (byte)(name_index >> 8);
			    constant_pool[nextbyte++] = (byte)name_index;
			    constant_pool[nextbyte++] = (byte)(descriptor_index >> 8);
			    constant_pool[nextbyte++] = (byte)descriptor_index;
			    registerConstant(key, pool_index, byte_offset);
			}
		    }
		}
	    }
	}

	return(pool_index);
    }


    final int
    storeString(String value) {

	return(storeString(value, value));
    }


    final int
    storeString(String key, String value) {

	int  byte_offset;
	int  pool_index;
	int  utf_index;

	if ((pool_index = getConstantPoolIndex(key, CONSTANT_STRING)) < 0) {
	    if ((utf_index = storeUTF(value)) >= 0) {
		pool_index = constant_pool_count++;
		byte_offset = nextbyte;
		ensureCapacityFor(CONSTANT_STRING);
		constant_pool[nextbyte++] = CONSTANT_STRING;
		constant_pool[nextbyte++] = (byte)(utf_index >> 8);
		constant_pool[nextbyte++] = (byte)utf_index;
		registerConstant(key, pool_index, byte_offset);
	    }
	}

	return(pool_index);
    }


    final int
    storeUTF(String value) {

	return(storeUTF(value, value));
    }


    final int
    storeUTF(String key, String value) {

	int  byte_offset;
	int  pool_index;
	int  length;

	if ((pool_index = getConstantPoolIndex(key, CONSTANT_UTF8)) < 0) {
	    pool_index = constant_pool_count++;
	    byte_offset = nextbyte;
	    ensureCapacityFor(CONSTANT_UTF8, 3*value.length());
	    length = JVMMisc.stringToModifiedUTF(value, constant_pool, nextbyte + 3);
	    constant_pool[nextbyte++] = CONSTANT_UTF8;
	    constant_pool[nextbyte++] = (byte)(length >> 8);
	    constant_pool[nextbyte++] = (byte)length;
	    nextbyte += length;
	    registerConstant(key, pool_index, byte_offset);
	}

	return(pool_index);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildConstantPool(JVMClassFile owner, int size) {

	this.owner = owner;
	constant_pool = new byte[0];
	constant_pool_count = 1;
	nextbyte = 0;
	directory = new HashMap();
	bytemap = new HashMap();
	ensureCapacity(size);
    }


    private void
    buildConstantPool(JVMClassFile owner, byte bytes[], int offset, int count) {

	boolean  valid = true;
	int      initial_offset;
	int      length;
	int      tag;
	int      pool_index;

	//
	// This will let us load constant pools from existing class files,
	// which should be a useful debugging and development tool. After
	// all constants are loaded we go back and rebuild the directory
	// map so the names that are used by the methods that store items
	// in the constant pool will appear in directory map. This has to
	// be done in a second pass because constants are variable length
	// and they're referenced by their index rather than their byte
	// offset.
	//

	initial_offset = offset;
	buildConstantPool(owner, 25*count);	// arbitrary initial size estimate

	for (pool_index = 1; pool_index < count && offset < bytes.length && valid; pool_index++) {
	    tag = bytes[offset];
	    length = getCapacityFor(tag);
	    switch (tag) {
		case CONSTANT_CLASS:
		case CONSTANT_STRING:
		case CONSTANT_FLOAT:
		case CONSTANT_INTEGER:
		case CONSTANT_NAMEANDTYPE:
		case CONSTANT_FIELDREF:
		case CONSTANT_INTERFACEMETHODREF:
		case CONSTANT_METHODREF:
		    valid = (storeBytes(bytes, offset, length, tag) > 0);
		    offset += length;
		    break;

		case CONSTANT_DOUBLE:
		case CONSTANT_LONG:
		    valid = (storeBytes(bytes, offset, length, tag) > 0);
		    offset += length;
		    pool_index++;
		    break;

		case CONSTANT_UTF8:
		    length += JVMMisc.getUnsignedShort(bytes, offset+1);
		    valid = (storeBytes(bytes, offset, length, tag) > 0);
		    offset += length;
		    break;

		default:
		    valid = false;
		    break;
	    }
	}

	bytes_consumed = offset - initial_offset;
	constant_pool = JVMMisc.trimToCurrentSize(constant_pool, nextbyte);
	buildIndex();
    }


    private void
    buildIndex() {

	String  name;
	int     tag;
	int     byte_offset;
	int     pool_index;

	//
	// The only time this is needed is right after we load the constant
	// pool that was read from an existing class file.
	//

	directory = new HashMap();

	for (pool_index = 1; pool_index < constant_pool_count; pool_index++) {
	    if ((tag = getTag(pool_index)) >= 0) {
		byte_offset = getByteOffset(pool_index);
		switch (tag) {
		    case CONSTANT_CLASS:
			registerConstant(getClass(pool_index), pool_index, byte_offset);
			break;

		    case CONSTANT_STRING:
			registerConstant(getString(pool_index), pool_index, byte_offset);
			break;

		    case CONSTANT_FLOAT:
			registerConstant(Float.toString(getFloat(pool_index)), pool_index, byte_offset);
			break;

		    case CONSTANT_INTEGER:
			registerConstant(Integer.toString(getInt(pool_index)), pool_index, byte_offset);
			break;

		    case CONSTANT_NAMEANDTYPE:
			registerConstant(
			    getNameFromNameAndType(pool_index) + "@" + getTypeFromNameAndType(pool_index),
			    pool_index,
			    byte_offset
			);
			break;

		    case CONSTANT_FIELDREF:
		    case CONSTANT_INTERFACEMETHODREF:
		    case CONSTANT_METHODREF:
			registerConstant(
			    getClassFromReference(pool_index) + "+" + getNameFromReference(pool_index) + "@" + getTypeFromReference(pool_index),
			    pool_index,
			    byte_offset
			);
			break;

		    case CONSTANT_DOUBLE:
			registerConstant(Double.toString(getDouble(pool_index)), pool_index, byte_offset);
			break;

		    case CONSTANT_LONG:
			registerConstant(Long.toString(getLong(pool_index)), pool_index, byte_offset);
			break;

		    case CONSTANT_UTF8:
			registerConstant(getStringFromUTF(pool_index), pool_index, byte_offset);
			break;
		}
	    }
	}
    }


    private void
    ensureCapacity(int size) {

	byte  tmp[];
	int   length;

	if (nextbyte + size > constant_pool.length) {
	    length = constant_pool.length + size;
	    tmp = new byte[length];
	    if (constant_pool.length > 0)
		System.arraycopy(constant_pool, 0, tmp, 0, constant_pool.length);
	    constant_pool = tmp;
	}
    }


    private void
    ensureCapacityFor(int tag) {

	ensureCapacity(getCapacityFor(tag));
    }


    private void
    ensureCapacityFor(int tag, int extra) {

	ensureCapacity(getCapacityFor(tag) + extra);
    }


    private int
    getByteOffset(int pool_index) {

	Object  obj;
	int     byte_offset;

	if (pool_index > 0 && (obj = bytemap.get(new Integer(pool_index))) != null) 
	    byte_offset = ((Integer)obj).intValue();
	else byte_offset = -1;

	return(byte_offset);
    }


    private int
    getCapacityFor(int tag) {

	int  size = 0;

	switch (tag) {
	    case CONSTANT_CLASS:
	    case CONSTANT_STRING:
		size = 3;
		break;

	    case CONSTANT_DOUBLE:
	    case CONSTANT_LONG:
		size = 9;
		break;

	    case CONSTANT_FLOAT:
	    case CONSTANT_INTEGER:
	    case CONSTANT_NAMEANDTYPE:
	    case CONSTANT_FIELDREF:
	    case CONSTANT_INTERFACEMETHODREF:
	    case CONSTANT_METHODREF:
		size = 5;
		break;

	    case CONSTANT_UTF8:
		//
		// The number of bytes stored in the constant still must be
		// added to this answer!!!
		//
		size = 3;
		break;
	}

	return(size);
    }


    private int
    getConstantPoolIndex(String key, int tag) {

	Object  obj;

	return((obj = directory.get(tag + ":" + key)) != null ? ((Integer)obj).intValue() : -1);
    }


    private String
    getTagAbbreviation(int tag) {

	String  abbreviation = null;

	switch (tag) {
	    case CONSTANT_CLASS:
		abbreviation = "CLASS";
		break;
	     
	    case CONSTANT_STRING:
		abbreviation = "STRING";
		break;

	    case CONSTANT_DOUBLE:
		abbreviation = "DOUBLE";
		break;

	    case CONSTANT_LONG:
		abbreviation = "LONG";
		break;

	    case CONSTANT_FLOAT:
		abbreviation = "FLOAT";
		break;

	    case CONSTANT_INTEGER:
		abbreviation = "INT";
		break;

	    case CONSTANT_NAMEANDTYPE:
		abbreviation = "TYPE";
		break;

	    case CONSTANT_FIELDREF:
		abbreviation = "FREF";
		break;

	    case CONSTANT_INTERFACEMETHODREF:
		abbreviation = "IREF";
		break;

	    case CONSTANT_METHODREF:
		abbreviation = "MREF";
		break;

	    case CONSTANT_UTF8:
		abbreviation = "UTF8";
		break;
	}

	return(abbreviation);
    }


    private void
    registerConstant(String key, int pool_index, int byte_offset) {

	bytemap.put(new Integer(pool_index), new Integer(byte_offset));
	directory.put(constant_pool[byte_offset] + ":" + key, new Integer(pool_index));
    }


    private int
    storeBytes(byte bytes[], int offset, int length, int tag) {

	return(storeBytes(constant_pool_count + "", bytes, offset, length, tag));
    }


    private int
    storeBytes(String key, byte bytes[], int offset, int length, int tag) {

	int  byte_offset;
	int  pool_index;

	if ((pool_index = getConstantPoolIndex(key, tag)) < 0) {
	    if (offset + length <= bytes.length) {
		pool_index = constant_pool_count++;
		byte_offset = nextbyte;
		ensureCapacity(length);
		System.arraycopy(bytes, offset, constant_pool, nextbyte, length);
		nextbyte += length;
		registerConstant(key, pool_index, byte_offset);
		if (tag == CONSTANT_DOUBLE || tag == CONSTANT_LONG)
		    constant_pool_count++;
	    }
	}

	return(pool_index);
    }


    private int
    storeReference(int tag, String key, String classname, String name, String descriptor) {

	int  byte_offset;
	int  pool_index;
	int  class_index;
	int  name_index;
	
	if ((pool_index = getConstantPoolIndex(key, tag)) < 0) {
	    if ((class_index = storeClass(classname)) >= 0) {
		if ((name_index = storeNameAndType(name, descriptor)) >= 0) {
		    pool_index = constant_pool_count++;
		    byte_offset = nextbyte;
		    ensureCapacityFor(tag);
		    constant_pool[nextbyte++] = (byte)tag;
		    constant_pool[nextbyte++] = (byte)(class_index >> 8);
		    constant_pool[nextbyte++] = (byte)class_index;
		    constant_pool[nextbyte++] = (byte)(name_index >> 8);
		    constant_pool[nextbyte++] = (byte)name_index;
		    registerConstant(key, pool_index, byte_offset);
		}
	    }
	}

	return(pool_index);
    }
}

