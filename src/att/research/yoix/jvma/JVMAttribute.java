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

public abstract
class JVMAttribute

    implements JVMConstants

{

    //
    // The abstract superclass for low level attribute classes.
    //

    protected JVMConstantPool  constant_pool;
    protected int              name_index;

    ///////////////////////////////////
    //
    // JVMAttribute Methods
    //
    ///////////////////////////////////

    final String
    dumpAttribute() {

	return(dumpAttribute(""));
    }


    final String
    dumpAttribute(String indent) {

	StringBuffer  sbuf = new StringBuffer();

	dumpAttributeInto(indent, sbuf);
	return(sbuf.toString());
    }


    final String
    getName() {

	return(constant_pool.getStringFromUTF(name_index));
    }


    final int
    getNameIndex() {

	return(name_index);
    }

    ///////////////////////////////////
    //
    // Abstract Methods
    //
    ///////////////////////////////////

    abstract void   dumpAttributeInto(String indent, StringBuffer sbuf);
    abstract byte[] getBytes();
    abstract int    getSize();
}

