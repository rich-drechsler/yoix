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
class JVMFieldsTable

    implements JVMConstants

{

    //
    // A class that's used to build a Java class file fields table.
    //

    private JVMConstantPool  constant_pool;
    private JVMField         fields_table[];
    private int              fields_table_count;

    //
    // Among other things we expect that the owner will eventually be used
    // to make editing decisions, provided it's not null.
    //

    private JVMClassFile  owner;

    //
    // We use a HashMap to remember the names of fields that have already
    // been defined. Done so the store methods can reject any attempt to
    // redefine an existing name. If we eventually want to support general
    // class file editing then we may want delete methods or perhaps just
    // add an argument to the store methods that allows replacement.
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
    JVMFieldsTable(JVMClassFile owner, JVMConstantPool constant_pool) {

	buildFieldsTable(owner, 0, constant_pool);
    }


    public
    JVMFieldsTable(JVMClassFile owner, int count, JVMConstantPool constant_pool) {

	buildFieldsTable(owner, count, constant_pool);
    }


    public
    JVMFieldsTable(JVMClassFile owner, byte bytes[], int offset, int count, JVMConstantPool constant_pool) {

	buildFieldsTable(owner, bytes, offset, count, constant_pool);
    }

    ///////////////////////////////////
    //
    // JVMFieldsTable Methods
    //
    ///////////////////////////////////

    final boolean
    contains(String name) {

	return(directory.containsKey(name));
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

	int  index;

	if (header != null) {
	    if (header.trim().length() > 0) {
		sbuf.append(indent);
		sbuf.append(header);
		indent += "  ";
	    }
	    sbuf.append("\n");
	}

	for (index = 0; index < fields_table_count; index++) {
	    sbuf.append(indent);
	    JVMMisc.appendRightAlignedInt(sbuf, index, 5, ": ");
	    fields_table[index].dumpFieldInto(indent + "       ", sbuf);
	}
    }


    final synchronized byte[]
    getBytes() {

	byte  bytes[];
	byte  field[];
	int   nextbyte;
	int   index;

	bytes = new byte[getSize()];
	nextbyte = 0;

	for (index = 0; index < fields_table_count; index++) {
	    field = fields_table[index].getBytes();
	    System.arraycopy(field, 0, bytes, nextbyte, field.length);
	    nextbyte += field.length;
	}

	return(bytes);
    }


    final int
    getBytesConsumed() {

	return(bytes_consumed);
    }


    final int
    getCount() {

	return(fields_table_count);
    }


    final JVMField
    getField(int index) {

	return(index >= 0 && index < fields_table_count ? fields_table[index] : null);
    }


    final int
    getFieldAccessFlags(int index) {

	return(index >= 0 && index < fields_table_count ? fields_table[index].getAccessFlags() : 0);
    }


    final JVMAttributesTable
    getFieldAttributes(int index) {

	return(index >= 0 && index < fields_table_count ? fields_table[index].getAttributes() : null);
    }


    final String
    getFieldDescriptor(int index) {

	return(index >= 0 && index < fields_table_count ? fields_table[index].getDescriptor() : null);
    }


    final String
    getFieldName(int index) {

	return(index >= 0 && index < fields_table_count ? fields_table[index].getName() : null);
    }


    final int
    getSize() {

	int  index;
	int  size = 0;

	for (index = 0; index < fields_table_count; index++)
	    size += fields_table[index].getSize();
	return(size);
    }


    final int
    storeConstantValue(String name, double value) {

	Object  obj;
	int     index = -1;

	if ((obj = directory.get(name)) != null)
	    index = fields_table[((Integer)obj).intValue()].storeConstantValue(value);
	return(index);
    }


    final int
    storeConstantValue(String name, float value) {

	Object  obj;
	int     index = -1;

	if ((obj = directory.get(name)) != null)
	    index = fields_table[((Integer)obj).intValue()].storeConstantValue(value);
	return(index);
    }


    final int
    storeConstantValue(String name, int value) {

	Object  obj;
	int     index = -1;

	if ((obj = directory.get(name)) != null)
	    index = fields_table[((Integer)obj).intValue()].storeConstantValue(value);
	return(index);
    }


    final int
    storeConstantValue(String name, long value) {

	Object  obj;
	int     index = -1;

	if ((obj = directory.get(name)) != null)
	    index = fields_table[((Integer)obj).intValue()].storeConstantValue(value);
	return(index);
    }


    final int
    storeConstantValue(String name, String value) {

	Object  obj;
	int     index = -1;

	if ((obj = directory.get(name)) != null)
	    index = fields_table[((Integer)obj).intValue()].storeConstantValue(value);
	return(index);
    }


    final int
    storeDeprecated(String name) {

	Object  obj;
	int     index = -1;

	if ((obj = directory.get(name)) != null)
	    index = fields_table[((Integer)obj).intValue()].storeDeprecated();
	return(index);
    }


    final int
    storeField(JVMField field) {

	String  name;
	String  descriptor;
	Object  obj;
	int     index = -1;

	if (field != null) {
	    if (field.getOwner() == owner) {
		if ((name = field.getName()) != null) {
		    if ((descriptor = field.getDescriptor()) != null) {
			if ((obj = directory.get(name)) == null) {
			    index = fields_table_count++;
			    ensureCapacity(1);
			    fields_table[index] = field;
			    registerField(name, index);
			}
		    }
		}
	    }
	}

	return(index);
    }


    final int
    storeField(String name, String descriptor, int access_flags) {

	return(storeField(name, descriptor, access_flags, null));
    }


    final int
    storeField(String name, String descriptor, int access_flags, JVMAttributesTable attributes) {

	int  name_index;
	int  descriptor_index;
	int  index = -1;

	if (JVMMisc.isIdentifier(name)) {
	    if (JVMDescriptor.isFieldDescriptor(descriptor)) {
		if ((name_index = constant_pool.storeUTF(name)) > 0) {
		    if ((descriptor_index = constant_pool.storeUTF(descriptor)) > 0) {
			index = storeField(
			    name_index,
			    descriptor_index,
			    access_flags,
			    attributes
			);
		    }
		}
	    }
	}

	return(index);
    }


    final int
    storeSynthetic(String name) {

	Object  obj;
	int     index = -1;

	if ((obj = directory.get(name)) != null)
	    index = fields_table[((Integer)obj).intValue()].storeSynthetic();
	return(index);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildFieldsTable(JVMClassFile owner, int count, JVMConstantPool constants) {

	this.owner = owner;
	this.constant_pool = constants;
	fields_table = new JVMField[0];
	fields_table_count = 0;
	directory = new HashMap();
	ensureCapacity(count);
    }


    private void
    buildFieldsTable(JVMClassFile owner, byte bytes[], int offset, int count, JVMConstantPool constants) {

	JVMAttributesTable  attributes;
	int                 initial_offset;
	int                 access_flags;
	int                 name_index;
	int                 descriptor_index;
	int                 attributes_count;
	int                 length;
	int                 index;

	initial_offset = offset;
	buildFieldsTable(owner, count, constants);

	for (index = 0; index < count && offset < bytes.length; index++) {
	    length = 8;
	    access_flags = JVMMisc.getUnsignedShort(bytes, offset);
	    name_index = JVMMisc.getUnsignedShort(bytes, offset + 2);
	    descriptor_index = JVMMisc.getUnsignedShort(bytes, offset + 4);
	    attributes_count = JVMMisc.getUnsignedShort(bytes, offset + 6);
	    if (attributes_count > 0) {
		attributes = new JVMAttributesTable(owner, bytes, offset + 8, attributes_count, constants);
		length += attributes.getSize();
		storeField(name_index, descriptor_index, access_flags, attributes);
	    } else storeField(name_index, descriptor_index, access_flags, null);
	    offset += length;
	}

	bytes_consumed = offset - initial_offset;
	trimToCurrentSize();
    }


    private void
    ensureCapacity(int count) {

	JVMField  tmp[];
	int       length;

	if (fields_table_count + count > fields_table.length) {
	    length = fields_table.length + count;
	    tmp = new JVMField[length];
	    if (fields_table.length > 0)
		System.arraycopy(fields_table, 0, tmp, 0, fields_table.length);
	    fields_table = tmp;
	}
    }


    private void
    registerField(String key, int index) {

	directory.put(key, new Integer(index));
    }


    private int
    storeField(int name_index, int descriptor_index, int access_flags, JVMAttributesTable attributes) {

	String  name;
	String  descriptor;
	Object  obj;
	int     index = -1;

	if ((name = constant_pool.getStringFromUTF(name_index)) != null) {
	    if ((descriptor = constant_pool.getStringFromUTF(descriptor_index)) != null) {
		if ((obj = directory.get(name)) == null) {
		    index = fields_table_count++;
		    ensureCapacity(1);
		    fields_table[index] = new JVMField(
			owner,
			access_flags,
			name_index,
			descriptor_index,
			attributes,
			constant_pool
		    );
		    registerField(name, index);
		} else if (descriptor.equals(getFieldDescriptor(((Integer)obj).intValue())))
		    index = ((Integer)obj).intValue();
	    }
	}

	return(index);
    }


    private void
    trimToCurrentSize() {

	JVMField  tmp[];

	if (fields_table_count < fields_table.length) {
	    tmp = new JVMField[fields_table_count];
	    if (fields_table.length > 0)
		System.arraycopy(fields_table, 0, tmp, 0, fields_table_count);
	    fields_table = tmp;
	}
    }
}

