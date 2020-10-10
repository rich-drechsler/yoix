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
class JVMInnerClasses extends JVMAttribute

    implements JVMConstants

{

    //
    // A class that implements the InnerClasses attribute.
    //

    private int  classes[];
    private int  classes_count;

    //
    // The owner field probably won't ever be used, but it's included for
    // consistency with the other JVM classes (at least for now).
    // 

    private JVMClassFile  owner;

    //
    // We use a HashMap to remember the inner classes that have already been
    // added to this table.
    //

    private HashMap  directory;

    ///////////////////////////////////
    //
    // Constructors Methods
    //
    ///////////////////////////////////

    public
    JVMInnerClasses(JVMClassFile owner, JVMConstantPool constant_pool) {

	buildAttribute(owner, 0, constant_pool);
    }


    public
    JVMInnerClasses(JVMClassFile owner, int count, JVMConstantPool constant_pool) {

	buildAttribute(owner, count, constant_pool);
    }


    public
    JVMInnerClasses(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	buildAttribute(owner, bytes, offset, constant_pool);
    }

    ///////////////////////////////////
    //
    // JVMInnerClasses Methods
    //
    ///////////////////////////////////

    final void
    dumpAttributeInto(String indent, StringBuffer sbuf) {

	int  index;
	int  flags;
	int  count;
	int  n;

	if ((count = 4*classes_count) > 0) {
	    sbuf.append(indent);
	    sbuf.append(ATTRIBUTE_INNER_CLASSES);
	    sbuf.append(":\n");
	    for (n = 0; n < count; n += 4) {
		sbuf.append(indent);
		sbuf.append("    ");
		if (classes[n+2] != 0)
		    constant_pool.dumpConstantInto(classes[n+2], sbuf);
		else sbuf.append("Anonymous Class");
		if ((index = classes[n]) > 0) {
		    sbuf.append("\n");
		    sbuf.append(indent);
		    sbuf.append("        ");
		    sbuf.append("Inner: ");
		    constant_pool.dumpConstantInto(index, sbuf);
		}
		if ((index = classes[n+1]) > 0) {
		    sbuf.append("\n");
		    sbuf.append(indent);
		    sbuf.append("        ");
		    sbuf.append("Outer: ");
		    constant_pool.dumpConstantInto(index, sbuf);
		}
		if ((flags = classes[n+3]) != 0) {
		    sbuf.append("\n");
		    sbuf.append(indent);
		    sbuf.append("        ");
		    sbuf.append("Access: ");
		    sbuf.append(JVMMisc.dumpAccessFlags(flags, false));
		}
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
	bytes[nextbyte++] = (byte)(classes_count >> 8);
	bytes[nextbyte++] = (byte)classes_count;

	if ((count = 4*classes_count) > 0) {
	    for (n = 0; n < count; n++) {
		bytes[nextbyte++] = (byte)(classes[n] >> 8);
		bytes[nextbyte++] = (byte)classes[n];
	    }
	}

	return(bytes);
    }


    final int
    getSize() {

	return(8 + 4*2*classes_count);
    }


    final int
    storeInnerClass(String inner, String outer, String name, int access_flags) {

	Object  obj;
	int     inner_index;
	int     outer_index;
	int     name_index;
	int     inner_class_index = -1;

	//
	// The JVM documentation says inner_index can be 0, which means it's
	// not a valid constant pool entry, but we're really not sure how it
	// would be interpreted? Perhaps it's a mistake in the documentation,
	// but either way this method probably still needs some work to make
	// sure everything's handled gracefully.
	//

	if (inner != null)
	    inner = inner.replace('.', '/');
	if (outer != null)
	    outer = outer.replace('.', '/');

	if (JVMMisc.isClassName(inner) || inner == null) {
	    if (JVMMisc.isClassName(outer) || outer == null) {
		if (JVMMisc.isIdentifier(name) || name == null) {
		    if ((obj = directory.get(makeKey(inner, outer, name))) == null) {
			inner_class_index = classes_count++;
			ensureCapacity(1);
			classes[4*inner_class_index] = (inner != null) ? constant_pool.storeClass(inner) : 0;
			classes[4*inner_class_index + 1] = (outer != null) ? constant_pool.storeClass(outer) : 0;
			classes[4*inner_class_index + 2] = name_index = (name != null) ? constant_pool.storeUTF(name) : 0;
			classes[4*inner_class_index + 3] = access_flags & INNER_CLASS_ACCESS_MASK;
			registerInnerClass(makeKey(inner, outer, name), inner_class_index);
		    } else inner_class_index = ((Integer)obj).intValue();
		}
	    }
	}

	return(inner_class_index);
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
	this.name_index = constant_pool.storeUTF(ATTRIBUTE_INNER_CLASSES);
	classes = new int[0];
	classes_count = 0;
	directory = new HashMap();
	ensureCapacity(count);
    }


    private void
    buildAttribute(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	String  name;
	String  inner_class;
	String  outer_class;
	int     access_flags;
	int     count;
	int     index;

	count = JVMMisc.getUnsignedShort(bytes, offset + 6);
	buildAttribute(owner, count, constant_pool);

	for (index = 0, offset += 8; index < count; index++, offset += 8) {
	    inner_class = constant_pool.getClass(JVMMisc.getUnsignedShort(bytes, offset));
	    outer_class = constant_pool.getClass(JVMMisc.getUnsignedShort(bytes, offset+2));
	    name = constant_pool.getStringFromUTF(JVMMisc.getUnsignedShort(bytes, offset+4));
	    access_flags = JVMMisc.getUnsignedShort(bytes, offset+6);
	    storeInnerClass(inner_class, outer_class, name, access_flags);
	}

	classes = JVMMisc.trimToCurrentSize(classes, 4*classes_count);
    }


    private void
    ensureCapacity(int count) {

	int  tmp[];
	int  length;

	if (4*(classes_count + count) > classes.length) {
	    length = classes.length + 4*count;
	    tmp = new int[length];
	    if (classes.length > 0)
		System.arraycopy(classes, 0, tmp, 0, classes.length);
	    classes = tmp;
	}
    }


    private String
    makeKey(String inner, String outer, String name) {

	String  key;

	key = (inner != null) ? inner : "";
	key += ":";
	key += (outer != null) ? outer : "";
	key += ":";
	key += (name != null) ? name : "";

	return(key);
    }


    private void
    registerInnerClass(String key, int class_index) {

	directory.put(key, new Integer(class_index));
    }
}

