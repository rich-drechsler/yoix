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
class JVMInterfacesTable

    implements JVMConstants

{

    //
    // A class that's used to build a Java class file interfaces table. In
    // this case a separate class is definitely overkill because the table
    // is just a collection of contant_pool indices, but it's an approach
    // that we used for most of the other pieces of a class file.
    //

    private JVMConstantPool  constant_pool;
    private int              interfaces_table[];
    private int              interfaces_table_count;

    //
    // Among other things we expect that the owner will eventually be used
    // to make editing decisions, provided it's not null.
    //

    private JVMClassFile  owner;

    //
    // We use a HashMap to remember the interface names that have already
    // been added to this table.
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
    JVMInterfacesTable(JVMClassFile owner, JVMConstantPool constant_pool) {

	buildInterfacesTable(owner, 0, constant_pool);
    }


    public
    JVMInterfacesTable(JVMClassFile owner, int count, JVMConstantPool constant_pool) {

	buildInterfacesTable(owner, count, constant_pool);
    }


    public
    JVMInterfacesTable(JVMClassFile owner, byte classfile[], JVMConstantPool constant_pool) {

	buildInterfacesTable(
	    owner,
	    classfile,
	    CLASSFILE_CONSTANT_POOL_TABLE,
	    JVMMisc.getUnsignedShort(classfile, CLASSFILE_CONSTANT_POOL_COUNT),
	    constant_pool
	);
    }


    public
    JVMInterfacesTable(JVMClassFile owner, byte bytes[], int offset, int count, JVMConstantPool constant_pool) {

	buildInterfacesTable(owner, bytes, offset, count, constant_pool);
    }

    ///////////////////////////////////
    //
    // JVMInterfacesTable Methods
    //
    ///////////////////////////////////

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

	String  name;
	int     interface_index;

	if (header != null) {
	    if (header.trim().length() > 0) {
		sbuf.append(indent);
		sbuf.append(header);
		indent += "    ";
	    }
	    sbuf.append("\n");
	}

	for (interface_index = 0; interface_index < interfaces_table_count; interface_index++) {
	    if ((name = getInterfaceName(interface_index)) != null) {
		sbuf.append(indent);
		sbuf.append(name);
		sbuf.append("\n");
	    }
	}
    }


    final byte[]
    getBytes() {

	byte  bytes[];
	int   nextbyte;
	int   n;

	bytes = new byte[getSize()];
	nextbyte = 0;

	for (n = 0; n < interfaces_table_count; n++) {
	    bytes[nextbyte++] = (byte)(interfaces_table[n] >> 8);
	    bytes[nextbyte++] = (byte)interfaces_table[n];
	}

	return(bytes);
    }


    final int
    getBytesConsumed() {

	return(bytes_consumed);
    }


    final int
    getCount() {

	return(interfaces_table_count);
    }


    final String
    getInterfaceName(int index) {

	return(index >= 0 && index < interfaces_table_count ? constant_pool.getClass(interfaces_table[index]) : null);
    }


    final int
    getSize() {

	return(2*interfaces_table_count);
    }


    final int
    storeInterface(String name) {

	Object  obj;
	int     name_index;
	int     interface_index = -1;

	name = name.replace('.', '/');

	if (JVMMisc.isClassName(name)) {
	    if ((obj = directory.get(name)) == null) {
		if ((name_index = constant_pool.storeClass(name)) > 0) {
		    interface_index = interfaces_table_count++;
		    ensureCapacity(1);
		    interfaces_table[interface_index] = name_index;
		    registerInterface(name, interface_index);
		}
	    } else interface_index = ((Integer)obj).intValue();
	}

	return(interface_index);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildInterfacesTable(JVMClassFile owner, int count, JVMConstantPool constants) {

	this.owner = owner;
	this.constant_pool = constants;
	interfaces_table = new int[0];
	interfaces_table_count = 0;
	directory = new HashMap();
	ensureCapacity(count);
    }


    private void
    buildInterfacesTable(JVMClassFile owner, byte bytes[], int offset, int count, JVMConstantPool constants) {

	String  name;
	int     initial_offset;
	int     index;

	initial_offset = offset;
	buildInterfacesTable(owner, count, constants);

	for (index = 0; index < count && offset < bytes.length; index++, offset += 2) {
	    name = constants.getClass(JVMMisc.getUnsignedShort(bytes, offset));
	    storeInterface(name);
	}

	bytes_consumed = offset - initial_offset;
	interfaces_table = JVMMisc.trimToCurrentSize(interfaces_table, interfaces_table_count);
    }


    private void
    ensureCapacity(int count) {

	int  tmp[];
	int  length;

	if (interfaces_table_count + count > interfaces_table.length) {
	    length = interfaces_table.length + count;
	    tmp = new int[length];
	    if (interfaces_table.length > 0)
		System.arraycopy(interfaces_table, 0, tmp, 0, interfaces_table.length);
	    interfaces_table = tmp;
	}
    }


    private void
    registerInterface(String key, int interface_index) {

	directory.put(key, new Integer(interface_index));
    }
}

