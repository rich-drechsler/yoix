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
class JVMUnimplemented extends JVMAttribute

    implements JVMConstants

{

    //
    // A simple class used to store attributes that don't recognize when we
    // read an existing class file.
    //

    private byte  attribute[];
    private int   attribute_length;

    //
    // The owner field probably won't ever be used, but it's included for
    // consistency with the other JVM classes (at least for now).
    // 

    private JVMClassFile  owner;

    ///////////////////////////////////
    //
    // Constructors Methods
    //
    ///////////////////////////////////

    public
    JVMUnimplemented(JVMClassFile owner, byte bytes[], JVMConstantPool constant_pool) {

	buildAttribute(owner, bytes, 0, constant_pool);
    }


    public
    JVMUnimplemented(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	buildAttribute(owner, bytes, offset, constant_pool);
    }

    ///////////////////////////////////
    //
    // JVMUnimplemented Methods
    //
    ///////////////////////////////////

    final void
    dumpAttributeInto(String indent, StringBuffer sbuf) {

	String  name;
	int     ch;
	int     n;

	if ((name = constant_pool.getStringFromUTF(name_index)) != null) {
	    sbuf.append(indent);
	    sbuf.append(name);
	    sbuf.append(" [unimplemented or unrecognized attribute]");
	    for (n = 0; n < attribute_length; n++) {
		if ((n % 16) == 0) {
		    sbuf.append("\n");
		    sbuf.append(indent);
		    sbuf.append("    ");
		}
		ch = attribute[n]&0xFF;
		if (ch < 16)
		    sbuf.append('0');
		sbuf.append(Integer.toString(ch, 16).toUpperCase());
	    }
	    sbuf.append("\n");
	}
    }


    final byte[]
    getBytes() {

	byte  bytes[];
	int   nextbyte;

	bytes = new byte[getSize()];
	nextbyte = 0;

	bytes[nextbyte++] = (byte)(name_index >> 8);
	bytes[nextbyte++] = (byte)name_index;
	bytes[nextbyte++] = (byte)(attribute_length >> 24);
	bytes[nextbyte++] = (byte)(attribute_length >> 16);
	bytes[nextbyte++] = (byte)(attribute_length >> 8);
	bytes[nextbyte++] = (byte)attribute_length;

	if (attribute.length > 0) {
	    System.arraycopy(attribute, 0, bytes, nextbyte, attribute.length);
	    nextbyte += attribute.length;	// unnecessary
	}

	return(bytes);
    }


    final int
    getSize() {

	return(6 + attribute_length);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildAttribute(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	this.owner = owner;
	this.constant_pool = constant_pool;
	this.name_index = JVMMisc.getUnsignedShort(bytes, offset);
	this.attribute_length = JVMMisc.getUnsignedInt(bytes, offset+2);
	attribute = new byte[attribute_length];
	System.arraycopy(bytes, offset+6, attribute, 0, attribute_length);
    }
}

