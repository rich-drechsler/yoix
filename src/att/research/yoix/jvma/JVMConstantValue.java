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
class JVMConstantValue extends JVMAttribute

    implements JVMConstants

{

    //
    // A class that implements the ConstantValue attribute.
    //

    private int  constantvalue_index;

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
    JVMConstantValue(JVMClassFile owner, double value, JVMConstantPool constant_pool) {

	buildAttribute(owner, constant_pool.storeDouble(value), constant_pool);
    }


    public
    JVMConstantValue(JVMClassFile owner, float value, JVMConstantPool constant_pool) {

	buildAttribute(owner, constant_pool.storeFloat(value), constant_pool);
    }


    public
    JVMConstantValue(JVMClassFile owner, int value, JVMConstantPool constant_pool) {

	buildAttribute(owner, constant_pool.storeInt(value), constant_pool);
    }


    public
    JVMConstantValue(JVMClassFile owner, long value, JVMConstantPool constant_pool) {

	buildAttribute(owner, constant_pool.storeLong(value), constant_pool);
    }


    public
    JVMConstantValue(JVMClassFile owner, String value, JVMConstantPool constant_pool) {

	buildAttribute(owner, constant_pool.storeString(value), constant_pool);
    }


    public
    JVMConstantValue(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	buildAttribute(owner, bytes, offset, constant_pool);
    }

    ///////////////////////////////////
    //
    // JVMConstantValue Methods
    //
    ///////////////////////////////////

    final void
    dumpAttributeInto(String indent, StringBuffer sbuf) {

	String  name;

	if ((name = constant_pool.getStringFromUTF(name_index)) != null) {
	    sbuf.append(indent);
	    sbuf.append(name);
	    sbuf.append(": ");
	    constant_pool.dumpConstantInto(constantvalue_index, sbuf);
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
	bytes[nextbyte++] = (byte)(constantvalue_index >> 8);
	bytes[nextbyte++] = (byte)constantvalue_index;

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
    buildAttribute(JVMClassFile owner, int constantvalue_index, JVMConstantPool constant_pool) {

	this.owner = owner;
	this.constant_pool = constant_pool;
	this.name_index = constant_pool.storeUTF(ATTRIBUTE_CONSTANT_VALUE);
	this.constantvalue_index = constantvalue_index;
    }


    private void
    buildAttribute(JVMClassFile owner, byte bytes[], int offset, JVMConstantPool constant_pool) {

	this.owner = owner;
	this.constant_pool = constant_pool;
	this.name_index = JVMMisc.getUnsignedShort(bytes, offset);
	this.constantvalue_index = JVMMisc.getUnsignedShort(bytes, offset + 6);
    }
}

