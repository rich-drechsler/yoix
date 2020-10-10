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
class JVMSourceFile extends JVMAttribute

    implements JVMConstants

{

    //
    // A class that implements the SourceFile attribute.
    //

    private int  sourcefile_index;

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
    JVMSourceFile(JVMClassFile owner, String value, JVMConstantPool constant_pool) {

	buildAttribute(owner, constant_pool.storeUTF(value), constant_pool);
    }


    public
    JVMSourceFile(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	buildAttribute(owner, bytes, offset, constant_pool);
    }

    ///////////////////////////////////
    //
    // JVMSourceFile Methods
    //
    ///////////////////////////////////

    final void
    dumpAttributeInto(String indent, StringBuffer sbuf) {

	String  name;

	if ((name = constant_pool.getStringFromUTF(name_index)) != null) {
	    sbuf.append(indent);
	    sbuf.append(name);
	    sbuf.append(": ");
	    constant_pool.dumpConstantInto(sourcefile_index, sbuf);
	    sbuf.append("\n");
	}
    }


    final byte[]
    getBytes() {

	byte  bytes[];
	int   nextbyte;

	bytes = new byte[8];
	nextbyte = 0;

	bytes[nextbyte++] = (byte)(name_index >> 8);
	bytes[nextbyte++] = (byte)name_index;
	bytes[nextbyte++] = 0;
	bytes[nextbyte++] = 0;
	bytes[nextbyte++] = 0;
	bytes[nextbyte++] = 2;
	bytes[nextbyte++] = (byte)(sourcefile_index >> 8);
	bytes[nextbyte++] = (byte)sourcefile_index;

	return(bytes);
    }


    final int
    getSize() {

	return(8);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildAttribute(JVMClassFile owner, int sourcefile_index, JVMConstantPool constant_pool) {

	this.owner = owner;
	this.constant_pool = constant_pool;
	this.name_index = constant_pool.storeUTF(ATTRIBUTE_SOURCE_FILE);
	this.sourcefile_index = sourcefile_index;
    }


    private void
    buildAttribute(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	this.owner = owner;
	this.constant_pool = constant_pool;
	this.name_index = JVMMisc.getUnsignedShort(bytes, offset);
	this.sourcefile_index = JVMMisc.getUnsignedShort(bytes, offset + 6);
    }
}

