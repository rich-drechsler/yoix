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
class JVMExceptions extends JVMAttribute

    implements JVMConstants

{

    //
    // A class that implements the Exceptions attribute.
    //

    private int  exceptions_table[];
    private int  exceptions_table_count;

    //
    // The owner field probably won't ever be used, but it's included for
    // consistency with the other JVM classes (at least for now).
    // 

    private JVMClassFile  owner;

    //
    // We use a HashMap to remember the exception names that have already
    // been added to this table.
    //

    private HashMap  directory;

    ///////////////////////////////////
    //
    // Constructors Methods
    //
    ///////////////////////////////////

    public
    JVMExceptions(JVMClassFile owner, JVMConstantPool constant_pool) {

	buildAttribute(owner, 0, constant_pool);
    }


    public
    JVMExceptions(JVMClassFile owner, int count, JVMConstantPool constant_pool) {

	buildAttribute(owner, count, constant_pool);
    }


    public
    JVMExceptions(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	buildAttribute(owner, bytes, offset, constant_pool);
    }

    ///////////////////////////////////
    //
    // JVMExceptions Methods
    //
    ///////////////////////////////////

    final void
    dumpAttributeInto(String indent, StringBuffer sbuf) {

	int  n;

	if (exceptions_table_count > 0) {
	    sbuf.append(indent);
	    sbuf.append(ATTRIBUTE_EXCEPTIONS);
	    sbuf.append(":\n");
	    for (n = 0; n < exceptions_table_count; n++) {
		sbuf.append(indent);
		sbuf.append("    ");
		constant_pool.dumpConstantInto(exceptions_table[n], sbuf);
		sbuf.append("\n");
	    }
	}
    }


    final byte[]
    getBytes() {

	byte  bytes[];
	int   size;
	int   length;
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
	bytes[nextbyte++] = (byte)(exceptions_table_count >> 8);
	bytes[nextbyte++] = (byte)exceptions_table_count;

	for (n = 0; n < exceptions_table_count; n++) {
	    bytes[nextbyte++] = (byte)(exceptions_table[n] >> 8);
	    bytes[nextbyte++] = (byte)exceptions_table[n];
	}

	return(bytes);
    }


    final int
    getSize() {

	return(8 + 2*exceptions_table_count);
    }


    final int
    storeException(String name) {

	Object  obj;
	int     name_index;
	int     exception_index = -1;

	name = name.replace('.', '/');

	if (JVMMisc.isClassName(name)) {
	    if ((obj = directory.get(name)) == null) {
		if ((name_index = constant_pool.storeClass(name)) > 0) {
		    exception_index = exceptions_table_count++;
		    ensureCapacity(1);
		    exceptions_table[exception_index] = name_index;
		    registerException(name, exception_index);
		}
	    } else exception_index = ((Integer)obj).intValue();
	}

	return(exception_index);
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
	this.name_index = constant_pool.storeUTF(ATTRIBUTE_EXCEPTIONS);
	exceptions_table = new int[0];
	exceptions_table_count = 0;
	directory = new HashMap();
	ensureCapacity(count);
    }


    private void
    buildAttribute(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	String  name;
	int     index;
	int     count;

	count = JVMMisc.getUnsignedShort(bytes, offset + 6);
	buildAttribute(owner, count, constant_pool);

	for (index = 0, offset += 8; index < count; index++, offset += 2) {
	    name = constant_pool.getClass(JVMMisc.getUnsignedShort(bytes, offset));
	    storeException(name);
	}

	exceptions_table = JVMMisc.trimToCurrentSize(exceptions_table, exceptions_table_count);
    }


    private void
    ensureCapacity(int count) {

	int  tmp[];
	int  length;

	if (exceptions_table_count + count > exceptions_table.length) {
	    length = exceptions_table.length + count;
	    tmp = new int[length];
	    if (exceptions_table.length > 0)
		System.arraycopy(exceptions_table, 0, tmp, 0, exceptions_table.length);
	    exceptions_table = tmp;
	}
    }


    private void
    registerException(String key, int exception_index) {

	directory.put(key, new Integer(exception_index));
    }
}

