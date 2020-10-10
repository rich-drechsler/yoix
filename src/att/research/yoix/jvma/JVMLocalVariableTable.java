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
class JVMLocalVariableTable extends JVMAttribute

    implements JVMConstants

{

    //
    // A class that implements the LocalVariableTable attribute.
    //

    private int  local_variable_table[];
    private int  local_variable_table_count;

    //
    // The owner field probably won't ever be used, but it's included for
    // consistency with the other JVM classes (at least for now).
    // 

    private JVMClassFile  owner;

    //
    // We use a HashMap to remember the local variables that have already
    // been added to this table.
    //

    private HashMap  directory;

    ///////////////////////////////////
    //
    // Constructors Methods
    //
    ///////////////////////////////////

    public
    JVMLocalVariableTable(JVMClassFile owner, JVMConstantPool constant_pool) {

	buildAttribute(owner, 0, constant_pool);
    }


    public
    JVMLocalVariableTable(JVMClassFile owner, int count, JVMConstantPool constant_pool) {

	buildAttribute(owner, count, constant_pool);
    }


    public
    JVMLocalVariableTable(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	buildAttribute(owner, bytes, offset, constant_pool);
    }

    ///////////////////////////////////
    //
    // JVMLocalVariableTable Methods
    //
    ///////////////////////////////////

    final void
    dumpAttributeInto(String indent, StringBuffer sbuf) {

	int  count;
	int  n;

	if ((count = 5*local_variable_table_count) > 0) {
	    sbuf.append(indent);
	    sbuf.append("    ");
	    sbuf.append("--------------  --------  ------  -------------------\n");
	    sbuf.append(indent);
	    sbuf.append("    ");
	    sbuf.append("VARIABLE INDEX  START PC  END PC  NAME AND DESCRIPTOR\n");
	    sbuf.append(indent);
	    sbuf.append("    ");
	    sbuf.append("--------------  --------  ------  -------------------\n");
	    for (n = 0; n < count; n += 5) {
		sbuf.append(indent);
		sbuf.append("        ");
		JVMMisc.appendRightAlignedInt(sbuf, local_variable_table[n+4], 5);
		sbuf.append("        ");
		JVMMisc.appendRightAlignedInt(sbuf, local_variable_table[n], 5);
		sbuf.append("    ");
		JVMMisc.appendRightAlignedInt(sbuf, local_variable_table[n+1], 5);
		sbuf.append("   ");
		sbuf.append(constant_pool.getStringFromUTF(local_variable_table[n+2]));
		sbuf.append(" ");
		sbuf.append(constant_pool.getStringFromUTF(local_variable_table[n+3]));
		sbuf.append("\n");
	    }
	}
    }


    final byte[]
    getBytes() {

	byte  bytes[];
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
	bytes[nextbyte++] = (byte)(local_variable_table_count >> 8);
	bytes[nextbyte++] = (byte)local_variable_table_count;

	if ((count = 5*local_variable_table_count) > 0) {
	    for (n = 0; n < count; n++) {
		bytes[nextbyte++] = (byte)(local_variable_table[n] >> 8);
		bytes[nextbyte++] = (byte)local_variable_table[n];
	    }
	}

	return(bytes);
    }


    final int
    getSize() {

	return(8 + 10*local_variable_table_count);
    }


    final int
    storeLocalVariable(String local_name, String local_descriptor, int local_index, int start_pc, int length) {

	Object  obj;
	int     local_variable_index = -1;

	//
	// NOTE - using a key built from local_index and start_pc isn't the
	// best approach because what we really want to know is whether any
	// of the existing pc "intervals" intersect this one and if they do
	// then there's probably something wrong. Definitely overkill for
	// debugging attribute that (I don't think) is used by the JVM.
	//

	if (JVMMisc.isIdentifier(local_name)) {
	    if (JVMDescriptor.isFieldDescriptor(local_descriptor)) {
		if (directory.get(makeKey(local_index, start_pc)) == null)
		    local_variable_index = newLocalVariable(local_name, local_descriptor, local_index, start_pc, length);
	    }
	}

	return(local_variable_index);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildAttribute(JVMClassFile owner, int count, JVMConstantPool constant_pool) {

	this.owner = owner;
	this.constant_pool = constant_pool;
	this.name_index = constant_pool.storeUTF(ATTRIBUTE_LOCAL_VARIABLE_TABLE);
	local_variable_table = new int[0];
	local_variable_table_count = 0;
	directory = new HashMap();
	ensureCapacity(count);
    }


    private void
    buildAttribute(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	String  local_name;
	String  local_descriptor;
	int     local_index;
	int     start_pc;
	int     length;
	int     count;
	int     index;

	count = JVMMisc.getUnsignedShort(bytes, offset + 6);
	buildAttribute(owner, count, constant_pool);

	for (index = 0, offset += 8; index < count; index++, offset += 10) {
	    start_pc = JVMMisc.getUnsignedShort(bytes, offset);
	    length = JVMMisc.getUnsignedShort(bytes, offset+2);
	    local_name = constant_pool.getStringFromUTF(JVMMisc.getUnsignedShort(bytes, offset+4));
	    local_descriptor = constant_pool.getStringFromUTF(JVMMisc.getUnsignedShort(bytes, offset+6));
	    local_index = JVMMisc.getUnsignedShort(bytes, offset+8);
	    newLocalVariable(local_name, local_descriptor, local_index, start_pc, length);
	}

	local_variable_table = JVMMisc.trimToCurrentSize(local_variable_table, 5*local_variable_table_count);
    }


    private void
    ensureCapacity(int count) {

	int  tmp[];
	int  length;

	if (5*(local_variable_table_count + count) > local_variable_table.length) {
	    length = local_variable_table.length + 5*count;
	    tmp = new int[length];
	    if (local_variable_table.length > 0)
		System.arraycopy(local_variable_table, 0, tmp, 0, local_variable_table.length);
	    local_variable_table = tmp;
	}
    }


    private String
    makeKey(int local_index, int start_pc) {

	return(local_index + ":" + start_pc);
    }


    private int
    newLocalVariable(String local_name, String local_descriptor, int local_index, int start_pc, int length) {

	int  local_variable_index = -1;
	int  offset;

	//
	// This is now used to load line number tables from existing class
	// files, so everything's accepted. If we rejected an entry there
	// could be problems later on during the class file load because
	// our getSize(), which is indirectly called from the method that's
	// reading the class, won't return a size that matches the size of
	// the attribute stored in the class file. As a result the offsets
	// calculated in that method will be off and the loading will fail
	// an unpredictable way.
	//
	// NOTE - we eventually may want a way to mark bad entries that we
	// accept anyway.
	//

	local_variable_index = local_variable_table_count++;
	ensureCapacity(1);
	offset = 5*local_variable_index;
	local_variable_table[offset] = start_pc;
	local_variable_table[offset + 1] = length;
	local_variable_table[offset + 2] = constant_pool.storeUTF(local_name);
	local_variable_table[offset + 3] = constant_pool.storeUTF(local_descriptor);
	local_variable_table[offset + 4] = local_index;
	registerLineNumber(makeKey(local_index, start_pc), local_variable_index);

	return(local_variable_index);
    }


    private void
    registerLineNumber(String key, int line_number_index) {

	directory.put(key, new Integer(line_number_index));
    }
}

