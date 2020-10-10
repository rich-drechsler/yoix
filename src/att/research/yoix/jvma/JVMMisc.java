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
import java.util.regex.*;

public abstract
class JVMMisc

    implements JVMConstants,
	       JVMPatterns

{

    //
    // A miscellaneous collection of methods.
    //

    static final HashMap  ACCESS_NAME_TO_FLAG_MAP = new HashMap();

    static {
	ACCESS_NAME_TO_FLAG_MAP.put(NAME_PUBLIC, new Integer(ACC_PUBLIC));
	ACCESS_NAME_TO_FLAG_MAP.put(NAME_PRIVATE, new Integer(ACC_PRIVATE));
	ACCESS_NAME_TO_FLAG_MAP.put(NAME_PROTECTED, new Integer(ACC_PROTECTED));
	ACCESS_NAME_TO_FLAG_MAP.put(NAME_STATIC, new Integer(ACC_STATIC));
	ACCESS_NAME_TO_FLAG_MAP.put(NAME_FINAL, new Integer(ACC_FINAL));
	ACCESS_NAME_TO_FLAG_MAP.put(NAME_SUPER, new Integer(ACC_SUPER));
	ACCESS_NAME_TO_FLAG_MAP.put(NAME_SYNCHRONIZED, new Integer(ACC_SYNCHRONIZED));
	ACCESS_NAME_TO_FLAG_MAP.put(NAME_VOLATILE, new Integer(ACC_VOLATILE));
	ACCESS_NAME_TO_FLAG_MAP.put(NAME_TRANSIENT, new Integer(ACC_TRANSIENT));
	ACCESS_NAME_TO_FLAG_MAP.put(NAME_NATIVE, new Integer(ACC_NATIVE));
	ACCESS_NAME_TO_FLAG_MAP.put(NAME_INTERFACE, new Integer(ACC_INTERFACE));
	ACCESS_NAME_TO_FLAG_MAP.put(NAME_ABSTRACT, new Integer(ACC_ABSTRACT));
	ACCESS_NAME_TO_FLAG_MAP.put(NAME_STRICT, new Integer(ACC_STRICT));
    }

    //
    // MNEMONIC_TO_OPCODE is used by the assembler to translate the mnemonic
    // presentation of an opcode the appropriate byte.
    //

    static final HashMap  MNEMONIC_TO_OPCODE = new HashMap(256);

    static {
	for (int n = 0; n < OPCODE_MNEMONICS.length; n++) {
	    if (OPCODE_MNEMONICS[n] != null)
		MNEMONIC_TO_OPCODE.put(OPCODE_MNEMONICS[n], new Integer(n));
	}
    }

    ///////////////////////////////////
    //
    // JVMMisc Methods
    //
    ///////////////////////////////////

    public static void
    appendRightAlignedInt(StringBuffer sbuf, int number, int width) {

	appendRightAlignedInt(sbuf, number, width, null);
    }


    public static void
    appendRightAlignedInt(StringBuffer sbuf, int number, int width, String suffix) {

	String  str = number + "";
	int     count;

	for (count = width - str.length(); count > 0; count--)
	    sbuf.append(" ");
	sbuf.append(str);
	if (suffix != null)
	    sbuf.append(suffix);
    }


    public static String
    dumpAccessFlags(int flags, boolean classfile) {

	String  str = "";
	String  sep = " ";

	//
	// The classfile argument is used to decide how to handle 0x0020,
	// which is defined as ACC_SYNCHRONIZED and ACC_SUPER. We're also
	// not enforcing exclusivity or any other rules that apply access
	// flags that are defined for various items in a class file.
	//

	if ((flags & ACC_PUBLIC) != 0)
	    str += ((str.length() > 0) ? sep : "") + NAME_PUBLIC;
	if ((flags & ACC_PRIVATE) != 0)
	    str += ((str.length() > 0) ? sep : "") + NAME_PRIVATE;
	if ((flags & ACC_PROTECTED) != 0)
	    str += ((str.length() > 0) ? sep : "") + NAME_PROTECTED;
	if ((flags & ACC_STATIC) != 0)
	    str += ((str.length() > 0) ? sep : "") + NAME_STATIC;
	if ((flags & ACC_FINAL) != 0)
	    str += ((str.length() > 0) ? sep : "") + NAME_FINAL;
	if ((flags & ACC_INTERFACE) != 0)
	    str += ((str.length() > 0) ? sep : "") + NAME_INTERFACE;
	if ((flags & ACC_SYNCHRONIZED) != 0)
	    str += ((str.length() > 0) ? sep : "") + (classfile ? NAME_SUPER : NAME_SYNCHRONIZED);
	if ((flags & ACC_NATIVE) != 0)
	    str += ((str.length() > 0) ? sep : "") + NAME_NATIVE;
	if ((flags & ACC_ABSTRACT) != 0)
	    str += ((str.length() > 0) ? sep : "") + NAME_ABSTRACT;
	if ((flags & ACC_STRICT) != 0)
	    str += ((str.length() > 0) ? sep : "") + NAME_STRICT;
	if ((flags & ACC_VOLATILE) != 0)
	    str += ((str.length() > 0) ? sep : "") + NAME_VOLATILE;
	if ((flags & ACC_TRANSIENT) != 0)
	    str += ((str.length() > 0) ? sep : "") + NAME_TRANSIENT;

	return(str);
    }


    public static int
    getAccessFlags(String list) {

	Integer  flag;
	String   fields[];
	int      flags = 0;
	int      n;

	if (list != null) {
	    fields = list.split("\\s+");
	    for (n = 0; n < fields.length; n++) {
		if ((flag = (Integer)ACCESS_NAME_TO_FLAG_MAP.get(fields[n])) != null)
		    flags |= flag.intValue();
	    }
	}
	return(flags);
    }


    public static String
    getClassName(String name) {

	return(getClassName(name, null));
    }


    public static String
    getClassName(String name, String value) {

	int  index;

	if (name != null) {
	    if ((index = name.lastIndexOf('.')) >= 0)
		value = name.substring(0, index);
	}
	return(value);
    }


    public static double
    getDouble(byte bytes[], int offset) {

	int  high;
	int  low;

	high = (bytes[offset] << 24) |
	    (bytes[offset+1] & 0xFF) << 16 |
	    (bytes[offset+2] & 0xFF) << 8 |
	    (bytes[offset+3] & 0xFF);

	low = (bytes[offset+4] << 24) |
	    (bytes[offset+5] & 0xFF) << 16 |
	    (bytes[offset+6] & 0xFF) << 8 |
	    (bytes[offset+7] & 0xFF);

	return(Double.longBitsToDouble((high & 0xFFFFFFFFL) << 32 | (low & 0xFFFFFFFFL)));
    }


    public static String
    getFieldName(String name) {

	return(getFieldName(name, name));
    }


    public static String
    getFieldName(String name, String value) {

	int  index;

	if (name != null) {
	    if ((index = name.lastIndexOf('.')) >= 0) {
		if (index < name.length() - 1)
		    value = name.substring(index+1);
	    }
	}
	return(value);
    }


    public static float
    getFloat(byte bytes[], int offset) {

	return(
	    Float.intBitsToFloat(
		(bytes[offset] << 24) |
		((bytes[offset+1] & 0xFF) << 16) |
		((bytes[offset+2] & 0xFF) << 8) |
		(bytes[offset+3] & 0xFF)
	    )
	);
    }


    public static int
    getInt(byte bytes[], int offset) {

	return(
	    (bytes[offset] << 24) |
	    (bytes[offset+1] & 0xFF) << 16 |
	    (bytes[offset+2] & 0xFF) << 8 |
	    (bytes[offset+3] & 0xFF)
	);
    }


    public static long
    getLong(byte bytes[], int offset) {

	int  high;
	int  low;

	high = (bytes[offset] << 24) |
	    (bytes[offset+1] & 0xFF) << 16 |
	    (bytes[offset+2] & 0xFF) << 8 |
	    (bytes[offset+3] & 0xFF);

	low = (bytes[offset+4] << 24) |
	    (bytes[offset+5] & 0xFF) << 16 |
	    (bytes[offset+6] & 0xFF) << 8 |
	    (bytes[offset+7] & 0xFF);

	return((high & 0xFFFFFFFFL) << 32 | (low & 0xFFFFFFFFL));
    }


    public static String
    getMethodName(String name) {

	return(getFieldName(name, name));
    }


    public static String
    getMethodName(String name, String value) {

	int  index;

	if (name != null) {
	    if ((index = name.indexOf('(')) >= 0)
		name = name.substring(0, index);
	    if ((index = name.lastIndexOf('.')) >= 0) {
		if (index < name.length() - 1)
		    value = name.substring(index+1);
	    }
	}
	return(value);
    }


    public static int
    getOpcodeFor(String name) {

	Object  value;

	return((value = MNEMONIC_TO_OPCODE.get(name)) != null ? ((Integer)value).intValue() : -1);
    }


    public static int
    getShort(byte bytes[], int offset) {

	return((bytes[offset] << 8) | (bytes[offset+1] & 0xFF));
    }


    public static String
    getStringFromUTF(byte bytes[], int offset) {

	//
	// This method assumes that the length of the UTF data is stored at
	// bytes[offset] and bytes[offset+1] and that the string data starts
	// at offset+2.
	//

	return(getStringFromUTF(bytes, offset + 2, getUnsignedShort(bytes, offset)));
    }


    public static String
    getStringFromUTF(byte bytes[], int offset, int length) {

	String  value = null;

	//
	// This method assumes that the string data starts at offset, which
	// means the caller has discovered the length data, which usually is
	// stored in the preceeding two bytes, so we don't have to account
	// for it here.
	//

	try {
	    value = stringFromModifiedUTF(bytes, offset, length);
	}
	catch(UTFDataFormatException e) {}

	return(value);
    }


    public static int
    getUnsignedInt(byte bytes[], int offset) {

	return(getInt(bytes, offset) & 0x7FFFFFFF);
    }


    public static int
    getUnsignedShort(byte bytes[], int offset) {

	return(((bytes[offset] & 0xFF) << 8) | (bytes[offset+1] & 0xFF));
    }


    public static boolean
    isClassName(String name) {

	return(isClassName(name, 0, (name != null) ? name.length() : -1));
    }


    public static boolean
    isClassName(String name, int offset, int length) {

	boolean  result;

	if (name != null && offset >= 0 && offset < length && length <= name.length())
	    result = (skipClassName(name, offset, length, "./") == length);
	else result = false;

	return(result);
    }


    public static boolean
    isConstuctorNameFor(String name, JVMClassFile classfile) {

	boolean  result;

	if ((result = isSpecialConstructorName(name)) == false) {
	    if (classfile != null && name != null)
		result = name.equals(classfile.getClassName());
	}

	return(result);
    }


    public static boolean
    isIdentifier(String name) {

	return(isIdentifier(name, 0, (name != null) ? name.length() : -1));
    }


    public static boolean
    isIdentifier(String name, int offset, int length) {

	boolean  result;

	if (name != null && offset >= 0 && offset < length && length <= name.length())
	    result = (skipIdentifier(name, offset, length) == length);
	else result = false;

	return(result);
    }


    public static boolean
    isMethodName(String name) {

	return(isIdentifier(name) || isSpecialName(name));
    }


    public static boolean
    isSpecialClassInitName(String name) {

	return(name != null && name.equals(NAME_CLASS_INIT));
    }


    public static boolean
    isSpecialConstructorName(String name) {

	return(name != null && name.equals(NAME_INIT));
    }


    public static boolean
    isSpecialName(String name) {

	return(isSpecialConstructorName(name) || isSpecialClassInitName(name));
    }


    public static boolean
    isSubClass(String classname, String superclass, HashMap extensions) {

	boolean  result = false;

	while (classname != null) {
	    if ((classname = (String)extensions.get(classname)) != null) {
		if (classname.equals(superclass)) {
		    result = true;
		    break;
		}
	    }
	}
	return(result);
    }


    public static byte[]
    readFile(String path) {

	FileInputStream  stream = null;
	byte             bytes[] = null;

	if (path != null && path.length() > 0) {
	    try {
		stream = new FileInputStream(path);
		bytes = readStream(stream);
	    }
	    catch(IOException e) {}
	    finally {
		if (stream != null) {
		    try {
			stream.close();
		    }
		    catch(IOException e) {}
		}
	    }
	}

	return(bytes);
    }


    public static byte[]
    readStream(InputStream stream) {

	byte  bytes[] = null;
	byte  buf[];
	byte  tmp[];
	int   count;

	try {
	    buf = new byte[4096];
	    bytes = new byte[0];
	    while ((count = stream.read(buf)) > 0) {
		tmp = new byte[bytes.length + count];
		System.arraycopy(bytes, 0, tmp, 0, bytes.length);
		System.arraycopy(buf, 0, tmp, bytes.length, count);
		bytes = tmp;
	    }
	    stream.close();
	}
	catch(IOException e) {
	    bytes = null;
	}

	return(bytes != null && bytes.length > 0 ? bytes : null);
    }


    public static int
    skipClassName(String name, int offset, int length, String sep) {

	boolean  firstchar;
	int      ch;

	for (firstchar = true; offset < length; offset++) {
	    ch = name.charAt(offset);
	    if (firstchar) {
		if (Character.isJavaIdentifierStart(ch) == false) {
		    offset = -1;
		    break;
		} else firstchar = false;
	    } else {
		if (Character.isJavaIdentifierPart(ch) == false) {
		    if (sep.indexOf(ch) < 0) {
			if (ch != ';')
			    offset = -1;
			break;
		    } else firstchar = true;
		}
	    }
	}

	return(offset);
    }


    public static int
    skipIdentifier(String name, int offset, int length) {

	//
	// We currently assume all characters in name belong to the "Basic
	// Mulitlingual Plane".
	//

	if (Character.isJavaIdentifierStart(name.charAt(offset++))) {
	    for (; offset < length; offset++) {
		if (Character.isJavaIdentifierPart(name.charAt(offset)) == false) {
		    offset = -1;
		    break;
		}
	    }
	} else offset = -1;

	return(offset);
    }


    public static String
    stringFromModifiedUTF(byte bytes[], int offset, int count)

	throws UTFDataFormatException

    {

	char  chars[];
	int   length;
	int   ch1;
	int   ch2;
	int   ch3;
	int   m;
	int   n;

	chars = new char[3*count];
	length = offset + count;

	for (n = offset, m = 0; n < length; ) {
	    ch1 = bytes[n++];
	    if (ch1 <= 0) {				// multibyte character (including 0)
		if ((ch1 & 0xC0) == 0xC0) {
		    ch2 = bytes[n++];
		    if ((ch2 & 0xC0) == 0x80)
			chars[m++] = (char)(((ch1 & 0x1F) << 6) | (ch2 & 0x3F));
		    else throw(new UTFDataFormatException("bad UTF character near byte " + (n - 1)));
		} else if ((ch1 & 0xE0) == 0xE0) {
		    ch2 = bytes[n++];
		    if ((ch2 & 0xC0) == 0x80) {
			ch3 = bytes[n++];
			if ((ch3 & 0xC0) == 0x80)
			    chars[m++] = (char)(((ch1 & 0x0F) << 12) | ((ch2 & 0x3F) << 6) | (ch3 & 0x3F));
			else throw(new UTFDataFormatException("bad UTF character near byte " + (n - 1)));
		    } else throw(new UTFDataFormatException("bad UTF character near byte " + (n - 1)));
		} else throw(new UTFDataFormatException("bad UTF character near byte " + (n - 1)));
	    } else chars[m++] = (char)ch1;
	}

	return(new String(chars, 0, m));
    }


    public static int
    stringToModifiedUTF(String str, byte bytes[], int offset) {

	int  length;
	int  ch;
	int  m;
	int  n;

	//
	// We assume the caller has made sure there's enough room in bytes[]
	// so we don't bother checking.
	//

	length = str.length();

	for (n = 0, m = offset; n < length; n++) {
	    ch = str.charAt(n);
	    if (ch == 0x0000 || ch > 0x007F) {
		if (ch > 0x07FF) {
		    bytes[m++] = (byte)(0xE0 | ((ch >> 12) & 0x0F));
		    bytes[m++] = (byte)(0x80 | ((ch >> 6) & 0x3F));
		    bytes[m++] = (byte)(0x80 | (ch & 0x3F));
		} else {
		    bytes[m++] = (byte)(0xC0 | ((ch >> 6) & 0x1F));
		    bytes[m++] = (byte)(0x80 | (ch & 0x3F));
		}
	    } else bytes[m++] = (byte)ch;
	}

	return(m - offset);
    }


    public static byte[]
    trimToCurrentSize(byte table[], int next) {

	byte  tmp[];

	if (table != null && next < table.length) {
	    tmp = new byte[next];
	    if (table.length > 0)
		System.arraycopy(table, 0, tmp, 0, next);
	    table = tmp;
	}
	return(table);
    }


    public static int[]
    trimToCurrentSize(int table[], int next) {

	int  tmp[];

	if (table != null && next < table.length) {
	    tmp = new int[next];
	    if (table.length > 0)
		System.arraycopy(table, 0, tmp, 0, next);
	    table = tmp;
	}
	return(table);
    }


    public static JVMInstruction[]
    trimToCurrentSize(JVMInstruction table[], int next) {

	JVMInstruction  tmp[];

	if (table != null && next < table.length) {
	    tmp = new JVMInstruction[next];
	    if (table.length > 0)
		System.arraycopy(table, 0, tmp, 0, next);
	    table = tmp;
	}
	return(table);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

}

