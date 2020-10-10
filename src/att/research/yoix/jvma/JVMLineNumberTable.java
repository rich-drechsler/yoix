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
class JVMLineNumberTable extends JVMAttribute

    implements JVMConstants

{

    //
    // A class that implements the LineNumberTable attribute.
    //

    private int  line_number_table[];
    private int  line_number_table_count;

    //
    // The owner field probably won't ever be used, but it's included for
    // consistency with the other JVM classes (at least for now).
    // 

    private JVMClassFile  owner;

    //
    // We use a HashMap to remember the starting pc and line number pairs
    // that have already been added to this table.
    //

    private HashMap  directory;

    ///////////////////////////////////
    //
    // Constructors Methods
    //
    ///////////////////////////////////

    public
    JVMLineNumberTable(JVMClassFile owner, JVMConstantPool constant_pool) {

	buildAttribute(owner, 0, constant_pool);
    }


    public
    JVMLineNumberTable(JVMClassFile owner, int count, JVMConstantPool constant_pool) {

	buildAttribute(owner, count, constant_pool);
    }


    public
    JVMLineNumberTable(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	buildAttribute(owner, bytes, offset, constant_pool);
    }

    ///////////////////////////////////
    //
    // JVMLineNumberTable Methods
    //
    ///////////////////////////////////

    final void
    dumpAttributeInto(String indent, StringBuffer sbuf) {

	int  count;
	int  n;

	if ((count = 2*line_number_table_count) > 0) {
	    sbuf.append(indent);
	    sbuf.append("    ");
	    sbuf.append("-----------  --------\n");
	    sbuf.append(indent);
	    sbuf.append("    ");
	    sbuf.append("SOURCE LINE  START PC\n");
	    sbuf.append(indent);
	    sbuf.append("    ");
	    sbuf.append("-----------  --------\n");
	    for (n = 0; n < count; n += 2) {
		sbuf.append(indent);
		sbuf.append("      ");
		JVMMisc.appendRightAlignedInt(sbuf, line_number_table[n+1], 5);
		sbuf.append("       ");
		JVMMisc.appendRightAlignedInt(sbuf, line_number_table[n], 5);
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
	bytes[nextbyte++] = (byte)(line_number_table_count >> 8);
	bytes[nextbyte++] = (byte)line_number_table_count;

	if ((count = 2*line_number_table_count) > 0) {
	    for (n = 0; n < count; n++) {
		bytes[nextbyte++] = (byte)(line_number_table[n] >> 8);
		bytes[nextbyte++] = (byte)line_number_table[n];
	    }
	}

	return(bytes);
    }


    final int
    getSize() {

	return(8 + 2*2*line_number_table_count);
    }


    final int
    storeLineNumber(int line_number, int start_pc) {

	Object  obj;
	int     line_number_index = -1;

	if ((obj = directory.get(makeKey(start_pc, line_number))) == null) {
	    line_number_index = line_number_table_count++;
	    ensureCapacity(1);
	    line_number_table[2*line_number_index] = start_pc;
	    line_number_table[2*line_number_index + 1] = line_number;
	    registerLineNumber(makeKey(start_pc, line_number), line_number_index);
	} else line_number_index = ((Integer)obj).intValue();

	return(line_number_index);
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
	this.name_index = constant_pool.storeUTF(ATTRIBUTE_LINE_NUMBER_TABLE);
	line_number_table = new int[0];
	line_number_table_count = 0;
	directory = new HashMap();
	ensureCapacity(count);
    }


    private void
    buildAttribute(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	int  start_pc;
	int  line_number;
	int  count;
	int  index;

	count = JVMMisc.getUnsignedShort(bytes, offset + 6);
	buildAttribute(owner, count, constant_pool);

	for (index = 0, offset += 8; index < count; index++, offset += 4) {
	    start_pc = JVMMisc.getUnsignedShort(bytes, offset);
	    line_number = JVMMisc.getUnsignedShort(bytes, offset+2);
	    storeLineNumber(line_number, start_pc);
	}

	line_number_table = JVMMisc.trimToCurrentSize(line_number_table, 2*line_number_table_count);
    }


    private void
    ensureCapacity(int count) {

	int  tmp[];
	int  length;

	if (2*(line_number_table_count + count) > line_number_table.length) {
	    length = line_number_table.length + 2*count;
	    tmp = new int[length];
	    if (line_number_table.length > 0)
		System.arraycopy(line_number_table, 0, tmp, 0, line_number_table.length);
	    line_number_table = tmp;
	}
    }


    private String
    makeKey(int start_pc, int line_number) {

	return(start_pc + ":" + line_number);
    }


    private void
    registerLineNumber(String key, int line_number_index) {

	directory.put(key, new Integer(line_number_index));
    }
}

