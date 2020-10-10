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
class JVMMethodsTable

    implements JVMConstants

{

    //
    // A class that's used to build a Java class file methods table.
    //

    private JVMConstantPool  constant_pool;
    private JVMMethod        methods_table[];
    private int              methods_table_count;

    //
    // Among other things we expect that the owner will eventually be used
    // to make editing decisions, provided it's not null.
    //

    private JVMClassFile  owner;

    //
    // We use a HashMap to remember the names of methods that have already
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
    JVMMethodsTable(JVMClassFile owner, JVMConstantPool constant_pool) {

	buildMethodsTable(owner, 0, constant_pool);
    }


    public
    JVMMethodsTable(JVMClassFile owner, int count, JVMConstantPool constant_pool) {

	buildMethodsTable(owner, count, constant_pool);
    }


    public
    JVMMethodsTable(JVMClassFile owner, byte bytes[], int offset, int count, JVMConstantPool constant_pool) {

	buildMethodsTable(owner, bytes, offset, count, constant_pool);
    }

    ///////////////////////////////////
    //
    // JVMMethodsTable Methods
    //
    ///////////////////////////////////

    final boolean
    contains(String name) {

	Iterator  iterator;
	boolean   result = false;
	String    key;

	if (name != null) {
	    if (name.indexOf('(') < 0) {
		name += "(";
		if ((iterator = directory.keySet().iterator()) != null) {
		    while (iterator.hasNext()) {
			if ((key = (String)iterator.next()) != null) {
			    if (key.startsWith(name)) {
				result = true;
				break;
			    }
			}
		    }
		}
	    } else result = directory.containsKey(name);
	}

	return(result);
    }


    final boolean
    contains(String name, String descriptor) {

	return(directory.containsKey(makeKey(name, descriptor)));
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

	for (index = 0; index < methods_table_count; index++) {
	    if (index > 0)
		sbuf.append("\n");
	    sbuf.append(indent);
	    JVMMisc.appendRightAlignedInt(sbuf, index, 5, ": ");
	    methods_table[index].dumpMethodInto(indent + "       ", sbuf);
	}
    }


    final synchronized byte[]
    getBytes() {

	byte  bytes[];
	byte  method[];
	int   nextbyte;
	int   index;

	bytes = new byte[getSize()];
	nextbyte = 0;

	for (index = 0; index < methods_table_count; index++) {
	    method = methods_table[index].getBytes();
	    System.arraycopy(method, 0, bytes, nextbyte, method.length);
	    nextbyte += method.length;
	}

	return(bytes);
    }


    final int
    getBytesConsumed() {

	return(bytes_consumed);
    }


    final int
    getCount() {

	return(methods_table_count);
    }


    final JVMMethod
    getMethod(int index) {

	return(index >= 0 && index < methods_table_count ? methods_table[index] : null);
    }


    final int
    getMethodAccessFlags(int index) {

	return(index >= 0 && index < methods_table_count ? methods_table[index].getAccessFlags() : 0);
    }


    final int
    getMethodAccessFlags(String name, String descriptor) {

	Object  obj;
	int     flags = 0;
	int     index;

	if ((obj = directory.get(makeKey(name, descriptor))) != null)
	    flags = getMethodAccessFlags(((Integer)obj).intValue());
	return(flags);
    }


    final JVMAttributesTable
    getMethodAttributes(int index) {

	return(index >= 0 && index < methods_table_count ? methods_table[index].getAttributes() : null);
    }


    final String
    getMethodDescriptor(int index) {

	return(index >= 0 && index < methods_table_count ? methods_table[index].getDescriptor() : null);
    }


    final String
    getMethodName(int index) {

	return(index >= 0 && index < methods_table_count ? methods_table[index].getName() : null);
    }


    final int
    getSize() {

	int  index;
	int  size = 0;

	for (index = 0; index < methods_table_count; index++)
	    size += methods_table[index].getSize();
	return(size);
    }


    final int
    storeCode(String name, String descriptor, byte code[], int max_stack, int max_locals, int exception_table[], JVMAttributesTable attributes) {

	Object  obj;
	int     index = -1;

	if ((obj = directory.get(makeKey(name, descriptor))) != null)
	    index = methods_table[((Integer)obj).intValue()].storeCode(code, max_stack, max_locals, exception_table, attributes);
	return(index);
    }


    final int
    storeDeprecated(String name, String descriptor) {

	Object  obj;
	int     index = -1;

	if ((obj = directory.get(makeKey(name, descriptor))) != null)
	    index = methods_table[((Integer)obj).intValue()].storeDeprecated();
	return(index);
    }


    final int
    storeLineNumber(String name, String descriptor, int line_number, int start_pc) {

	Object  obj;
	int     index = -1;

	if ((obj = directory.get(makeKey(name, descriptor))) != null)
	    index = methods_table[((Integer)obj).intValue()].storeLineNumber(line_number, start_pc);
	return(index);
    }


    final int
    storeLocalVariable(String name, String descriptor, String local_name, String local_descriptor, int local_index, int start_pc, int length) {

	Object  obj;
	int     index = -1;

	if ((obj = directory.get(makeKey(name, descriptor))) != null)
	    index = methods_table[((Integer)obj).intValue()].storeLocalVariable(local_name, local_descriptor, local_index, start_pc, length);
	return(index);
    }


    final int
    storeMethod(JVMMethod method) {

	String  name;
	String  descriptor;
	String  key;
	Object  obj;
	int     index = -1;

	if (method != null) {
	    if (method.getOwner() == owner) {
		if ((name = method.getName()) != null) {
		    if ((descriptor = method.getDescriptor()) != null) {
			key = makeKey(name, descriptor);
			if ((obj = directory.get(key)) == null) {
			    index = methods_table_count++;
			    ensureCapacity(1);
			    methods_table[index] = method;
			    registerMethod(key, index);
			}
		    }
		}
	    }
	}

	return(index);
    }


    final int
    storeMethod(String name, String descriptor, int access_flags) {

	return(storeMethod(name, descriptor, access_flags, null));
    }


    final int
    storeMethod(String name, String descriptor, int access_flags, JVMAttributesTable attributes) {

	int  name_index;
	int  descriptor_index;
	int  index = -1;

	if (JVMMisc.isIdentifier(name)) {
	    if (JVMDescriptor.isMethodDescriptor(descriptor)) {
		if ((name_index = constant_pool.storeUTF(name)) > 0) {
		    if ((descriptor_index = constant_pool.storeUTF(descriptor)) > 0) {
			index = storeMethod(
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
    storeSynthetic(String name, String descriptor) {

	Object  obj;
	int     index = -1;

	if ((obj = directory.get(makeKey(name, descriptor))) != null)
	    index = methods_table[((Integer)obj).intValue()].storeSynthetic();
	return(index);
    }


    final int
    storeThrownException(String name, String descriptor, String exception) {

	Object  obj;
	int     index = -1;

	if ((obj = directory.get(makeKey(name, descriptor))) != null)
	    index = methods_table[((Integer)obj).intValue()].storeThrownException(exception);
	return(index);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildMethodsTable(JVMClassFile owner, int count, JVMConstantPool constants) {

	this.owner = owner;
	this.constant_pool = constants;
	methods_table = new JVMMethod[0];
	methods_table_count = 0;
	directory = new HashMap();
	ensureCapacity(count);
    }


    private void
    buildMethodsTable(JVMClassFile owner, byte bytes[], int offset, int count, JVMConstantPool constants) {

	JVMAttributesTable  attributes;
	int                 initial_offset;
	int                 access_flags;
	int                 name_index;
	int                 descriptor_index;
	int                 attributes_count;
	int                 length;
	int                 index;

	initial_offset = offset;
	buildMethodsTable(owner, count, constants);

	for (index = 0; index < count && offset < bytes.length; index++) {
	    length = 8;
	    access_flags = JVMMisc.getUnsignedShort(bytes, offset);
	    name_index = JVMMisc.getUnsignedShort(bytes, offset + 2);
	    descriptor_index = JVMMisc.getUnsignedShort(bytes, offset + 4);
	    attributes_count = JVMMisc.getUnsignedShort(bytes, offset + 6);
	    if (attributes_count > 0) {
		attributes = new JVMAttributesTable(owner, bytes, offset + 8, attributes_count, constants);
		length += attributes.getSize();
		storeMethod(name_index, descriptor_index, access_flags, attributes);
	    } else storeMethod(name_index, descriptor_index, access_flags, null);
	    offset += length;
	}

	bytes_consumed = offset - initial_offset;
	trimToCurrentSize();
    }


    private void
    ensureCapacity(int count) {

	JVMMethod  tmp[];
	int        length;

	if (methods_table_count + count > methods_table.length) {
	    length = methods_table.length + count;
	    tmp = new JVMMethod[length];
	    if (methods_table.length > 0)
		System.arraycopy(methods_table, 0, tmp, 0, methods_table.length);
	    methods_table = tmp;
	}
    }


    private String
    makeKey(String name, String descriptor) {

	return(name + descriptor);
    }

    private void
    registerMethod(String key, int index) {

	directory.put(key, new Integer(index));
    }


    private int
    storeMethod(int name_index, int descriptor_index, int access_flags, JVMAttributesTable attributes) {

	String  name;
	String  descriptor;
	String  key;
	Object  obj;
	int     index = -1;

	if ((name = constant_pool.getStringFromUTF(name_index)) != null) {
	    if ((descriptor = constant_pool.getStringFromUTF(descriptor_index)) != null) {
		key = makeKey(name, descriptor);
		if ((obj = directory.get(key)) == null) {
		    index = methods_table_count++;
		    ensureCapacity(1);
		    methods_table[index] = new JVMMethod(
			owner,
			access_flags,
			name_index,
			descriptor_index,
			attributes,
			constant_pool
		    );
		    registerMethod(key, index);
		} else index = ((Integer)obj).intValue();
	    }
	}

	return(index);
    }


    private void
    trimToCurrentSize() {

	JVMMethod  tmp[];

	if (methods_table_count < methods_table.length) {
	    tmp = new JVMMethod[methods_table_count];
	    if (methods_table.length > 0)
		System.arraycopy(methods_table, 0, tmp, 0, methods_table_count);
	    methods_table = tmp;
	}
    }
}

