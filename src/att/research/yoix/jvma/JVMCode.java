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
class JVMCode extends JVMAttribute

    implements JVMConstants

{

    //
    // A class that implements the Code attribute.
    //

    private JVMAttributesTable  attributes;
    private byte                bytecode[];
    private int                 exception_table[];
    private int                 max_stack;
    private int                 max_locals;
    private int                 bytecode_length;
    private int                 attributes_count;
    private int                 exception_table_count;

    //
    // Among other things we expect that the owner may eventually be used
    // to make editing decisions, provided it's not null.
    //

    private JVMClassFile  owner;

    ///////////////////////////////////
    //
    // Constructors Methods
    //
    ///////////////////////////////////

    public
    JVMCode(JVMClassFile owner, byte bytecode[], int max_stack, int max_locals, JVMConstantPool constant_pool) {

	this(owner, bytecode, max_stack, max_locals, null, null, constant_pool);
    }


    public
    JVMCode(JVMClassFile owner, byte bytecode[], int max_stack, int max_locals, int exception_table[], JVMConstantPool constant_pool) {

	buildAttribute(owner, bytecode, max_stack, max_locals, exception_table, null, constant_pool);
    }


    public
    JVMCode(JVMClassFile owner, byte bytecode[], int max_stack, int max_locals, int exception_table[], JVMAttributesTable attributes, JVMConstantPool constant_pool) {

	buildAttribute(owner, bytecode, max_stack, max_locals, exception_table, attributes, constant_pool);
    }


    public
    JVMCode(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	buildAttribute(owner, bytes, offset, constant_pool);
    }

    ///////////////////////////////////
    //
    // JVMCode Methods
    //
    ///////////////////////////////////

    final void
    dumpAttributeInto(String indent, StringBuffer sbuf) {

	int  count;
	int  n;

	sbuf.append(indent);
	sbuf.append(ATTRIBUTE_CODE);
	sbuf.append("[");
	sbuf.append("max_stack=");
	sbuf.append(max_stack);
	sbuf.append(", ");
	sbuf.append("max_locals=");
	sbuf.append(max_locals);
	sbuf.append("]:\n");

	JVMDisassembler.disassembleInto(bytecode, 0, bytecode_length, indent, sbuf, owner);

	if (exception_table_count > 0 || attributes_count > 0)
	    sbuf.append("\n");

	if ((count = 4*exception_table_count) > 0) {
	    sbuf.append(indent);
	    sbuf.append("    ");
	    sbuf.append("-----------------  --------  ------  ---------\n");
	    sbuf.append(indent);
	    sbuf.append("    ");
	    sbuf.append("EXCEPTION HANDLER  START PC  END PC  EXCEPTION\n");
	    sbuf.append(indent);
	    sbuf.append("    ");
	    sbuf.append("-----------------  --------  ------  ---------\n");

	    for (n = 0; n < count; n += 4) {
		sbuf.append(indent);
		sbuf.append("         ");
		JVMMisc.appendRightAlignedInt(sbuf, exception_table[n+2], 5);
		sbuf.append("          ");
		JVMMisc.appendRightAlignedInt(sbuf, exception_table[n], 5);
		sbuf.append("    ");
		JVMMisc.appendRightAlignedInt(sbuf, exception_table[n+1], 5);
		sbuf.append("   ");
		if (exception_table[n+3] != 0)
		    constant_pool.dumpConstantInto(exception_table[n+3], sbuf);
		else sbuf.append("any");
		sbuf.append("\n");
	    }
	}

	if (attributes_count > 0)
	    attributes.dumpTableInto(indent, sbuf);
    }


    final byte[]
    getBytes() {

	byte  bytes[];
	byte  table[];
	int   size;
	int   length;
	int   count;
	int   nextbyte;
	int   n;

	size = getSize();
	length = size - 6;
	bytes = new byte[size];
	nextbyte = 0;

	bytes[nextbyte++] = (byte)(name_index >> 8);
	bytes[nextbyte++] = (byte)name_index;
	bytes[nextbyte++] = (byte)(length >> 24);
	bytes[nextbyte++] = (byte)(length >> 16);
	bytes[nextbyte++] = (byte)(length >> 8);
	bytes[nextbyte++] = (byte)length;

	bytes[nextbyte++] = (byte)(max_stack >> 8);
	bytes[nextbyte++] = (byte)max_stack;
	bytes[nextbyte++] = (byte)(max_locals >> 8);
	bytes[nextbyte++] = (byte)max_locals;

	bytes[nextbyte++] = (byte)(bytecode_length >> 24);
	bytes[nextbyte++] = (byte)(bytecode_length >> 16);
	bytes[nextbyte++] = (byte)(bytecode_length >> 8);
	bytes[nextbyte++] = (byte)bytecode_length;

	if (bytecode_length > 0) {
	    System.arraycopy(bytecode, 0, bytes, nextbyte, bytecode_length);
	    nextbyte += bytecode_length;
	}

	bytes[nextbyte++] = (byte)(exception_table_count >> 8);
	bytes[nextbyte++] = (byte)exception_table_count;

	if ((count = 4*exception_table_count) > 0) {
	    for (n = 0; n < count; n++) {
		bytes[nextbyte++] = (byte)(exception_table[n] >> 8);
		bytes[nextbyte++] = (byte)exception_table[n];
	    }
	}

	bytes[nextbyte++] = (byte)(attributes_count >> 8);
	bytes[nextbyte++] = (byte)attributes_count;

	if (attributes_count > 0) {
	    table = attributes.getBytes();
	    System.arraycopy(table, 0, bytes, nextbyte, table.length);
	    nextbyte += table.length;
	}

	return(bytes);
    }


    final int
    getSize() {

	int  size;

	size = 14;
	size += bytecode_length;
	size += 2;
	size += 8*exception_table_count;
	size += 2;
	size += (attributes != null) ? attributes.getSize() : 0;

	return(size);
    }


    final int
    storeLineNumber(int line_number, int start_pc) {

	return(getAttributesTable().storeLineNumber(line_number, start_pc));
    }


    final int
    storeLocalVariable(String local_name, String local_descriptor, int local_index, int start_pc, int length) {

	return(getAttributesTable().storeLocalVariable(local_name, local_descriptor, local_index, start_pc, length));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildAttribute(JVMClassFile owner, byte bytecode[], int max_stack, int max_locals, int exception_table[], JVMAttributesTable attributes, JVMConstantPool constant_pool) {

	this.owner = owner;
	this.constant_pool = constant_pool;
	this.name_index = constant_pool.storeUTF(ATTRIBUTE_CODE);
	this.bytecode = bytecode;
	this.bytecode_length = (bytecode != null) ? bytecode.length : 0;
	this.max_stack = max_stack;
	this.max_locals = max_locals;
	this.exception_table = exception_table;
	this.exception_table_count = (exception_table != null) ? exception_table.length/4 : 0;
	this.attributes = attributes;
	this.attributes_count = (attributes != null) ? attributes.getCount() : 0;
    }


    private void
    buildAttribute(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	int  tablesize = 0;
	int  count;
	int  n;

	this.owner = owner;
	this.constant_pool = constant_pool;
	this.name_index = JVMMisc.getUnsignedShort(bytes, offset);
	max_stack = JVMMisc.getUnsignedShort(bytes, offset+6);
	max_locals = JVMMisc.getUnsignedShort(bytes, offset+8);

	bytecode_length = JVMMisc.getUnsignedInt(bytes, offset+10);
	if (bytecode_length > 0) {
	    bytecode = new byte[bytecode_length];
	    System.arraycopy(bytes, offset+14, bytecode, 0, bytecode_length);
	    tablesize += bytecode_length;
	} else bytecode = null;

	exception_table_count = JVMMisc.getUnsignedShort(bytes, offset + 14 + tablesize);
	if ((count = 4*exception_table_count) > 0) {
	    exception_table = new int[count];
	    for (n = 0; n < count; n++) {
		exception_table[n] = JVMMisc.getUnsignedShort(bytes, offset + 16 + tablesize);
		tablesize += 2;
	    }
	}

	attributes_count = JVMMisc.getUnsignedShort(bytes, offset + 16 + tablesize);
	if (attributes_count > 0)
	    attributes = new JVMAttributesTable(owner, bytes, offset + 18 + tablesize, attributes_count, constant_pool);
    }


    private synchronized JVMAttributesTable
    getAttributesTable() {

	if (attributes == null)
	    attributes = new JVMAttributesTable(owner, constant_pool);
	return(attributes);
    }
}

