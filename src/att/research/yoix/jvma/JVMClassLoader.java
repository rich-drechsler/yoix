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

public final
class JVMClassLoader extends ClassLoader

{

    //
    // A trivial class loader that builds a Java class file from its assembly
    // language source and then loads it so it will be available for use by
    // the JVM.
    //

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    ///////////////////////////////////
    //
    // JVMClassLoader Methods
    //
    ///////////////////////////////////

    public Class
    defineClass(String source)

	throws ClassFormatError,
	       IllegalArgumentException,
	       JVMAssemblerError

    {

	return(defineClass(source, 1));
    }


    public Class
    defineClass(String source, int linenumber)

	throws ClassFormatError,
	       IllegalArgumentException,
	       JVMAssemblerError

    {

	JVMClassFile  classfile;
	Class         defined_class = null;

	if (source != null) {
	    if ((classfile = (new JVMAssembler()).assembleClass(source, linenumber)) != null)
		defined_class = defineClass(classfile);
	}

	return(defined_class);
    }


    public Class
    defineClass(JVMClassFile classfile)

	throws ClassFormatError,
	       IllegalArgumentException

    {

	String  classname;
	String  packagename;
	Class   defined_class = null;
	byte    bytes[];
	int     index;

	if (classfile != null) {
	    if ((bytes = classfile.getBytes()) != null) {
		classname = classfile.getClassName();
		if ((index = classname.lastIndexOf('.')) >= 0) {
		    packagename = classname.substring(0, index);
		    if (getPackage(packagename) == null)
			definePackage(packagename, null, null, null, null, null, null, null);
		}
		defined_class = defineClass(classname, bytes, 0, bytes.length, null);
	    }
	}

	return(defined_class);
    }


    public Class
    defineClass(byte bytes[], String classname)

	throws ClassFormatError,
	       IllegalArgumentException

    {

	String  packagename;
	Class   defined_class = null;
	int     index;

	if (bytes != null && classname != null) {
	    if ((index = classname.lastIndexOf('.')) >= 0) {
		packagename = classname.substring(0, index);
		if (getPackage(packagename) == null)
		    definePackage(packagename, null, null, null, null, null, null, null);
	    }
	    defined_class = defineClass(classname, bytes, 0, bytes.length, null);
	}

	return(defined_class);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

}

