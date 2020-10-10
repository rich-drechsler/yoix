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
import java.util.*;
import java.util.regex.*;

public abstract
class JVMDescriptor

    implements JVMConstants,
	       JVMPatterns

{

    //
    // A miscellaneous collection of methods.
    //

    private static final HashMap  BASE_TYPE_TO_DESCRIPTOR_MAP = new HashMap();

    static {
	BASE_TYPE_TO_DESCRIPTOR_MAP.put(BASE_TYPE_NAME_BOOLEAN, BOOLEAN_DESCRIPTOR);
	BASE_TYPE_TO_DESCRIPTOR_MAP.put(BASE_TYPE_NAME_CHAR, CHAR_DESCRIPTOR);
	BASE_TYPE_TO_DESCRIPTOR_MAP.put(BASE_TYPE_NAME_FLOAT, FLOAT_DESCRIPTOR);
	BASE_TYPE_TO_DESCRIPTOR_MAP.put(BASE_TYPE_NAME_DOUBLE, DOUBLE_DESCRIPTOR);
	BASE_TYPE_TO_DESCRIPTOR_MAP.put(BASE_TYPE_NAME_BYTE, BYTE_DESCRIPTOR);
	BASE_TYPE_TO_DESCRIPTOR_MAP.put(BASE_TYPE_NAME_SHORT, SHORT_DESCRIPTOR);
	BASE_TYPE_TO_DESCRIPTOR_MAP.put(BASE_TYPE_NAME_INT, INT_DESCRIPTOR);
	BASE_TYPE_TO_DESCRIPTOR_MAP.put(BASE_TYPE_NAME_LONG, LONG_DESCRIPTOR);
	BASE_TYPE_TO_DESCRIPTOR_MAP.put(BASE_TYPE_NAME_RETURNADDRESS, RETURNADDRESS_DESCRIPTOR);
    }

    private static final HashMap  DESCRIPTOR_TO_BASE_TYPE = new HashMap();

    static {
	DESCRIPTOR_TO_BASE_TYPE.put(BOOLEAN_DESCRIPTOR, BASE_TYPE_NAME_BOOLEAN);
	DESCRIPTOR_TO_BASE_TYPE.put(CHAR_DESCRIPTOR, BASE_TYPE_NAME_CHAR);
	DESCRIPTOR_TO_BASE_TYPE.put(FLOAT_DESCRIPTOR, BASE_TYPE_NAME_FLOAT);
	DESCRIPTOR_TO_BASE_TYPE.put(DOUBLE_DESCRIPTOR, BASE_TYPE_NAME_DOUBLE);
	DESCRIPTOR_TO_BASE_TYPE.put(BYTE_DESCRIPTOR, BASE_TYPE_NAME_BYTE);
	DESCRIPTOR_TO_BASE_TYPE.put(SHORT_DESCRIPTOR, BASE_TYPE_NAME_SHORT);
	DESCRIPTOR_TO_BASE_TYPE.put(INT_DESCRIPTOR, BASE_TYPE_NAME_INT);
	DESCRIPTOR_TO_BASE_TYPE.put(LONG_DESCRIPTOR, BASE_TYPE_NAME_LONG);
    }

    private static final HashMap  TYPE_ID_TO_DESCRIPTOR = new HashMap();

    static {
	TYPE_ID_TO_DESCRIPTOR.put(new Integer(BOOLEAN_ARRAY_TYPE_ID), BOOLEAN_DESCRIPTOR);
	TYPE_ID_TO_DESCRIPTOR.put(new Integer(CHAR_ARRAY_TYPE_ID), CHAR_DESCRIPTOR);
	TYPE_ID_TO_DESCRIPTOR.put(new Integer(FLOAT_ARRAY_TYPE_ID), FLOAT_DESCRIPTOR);
	TYPE_ID_TO_DESCRIPTOR.put(new Integer(DOUBLE_ARRAY_TYPE_ID), DOUBLE_DESCRIPTOR);
	TYPE_ID_TO_DESCRIPTOR.put(new Integer(BYTE_ARRAY_TYPE_ID), BYTE_DESCRIPTOR);
	TYPE_ID_TO_DESCRIPTOR.put(new Integer(SHORT_ARRAY_TYPE_ID), SHORT_DESCRIPTOR);
	TYPE_ID_TO_DESCRIPTOR.put(new Integer(INT_ARRAY_TYPE_ID), INT_DESCRIPTOR);
	TYPE_ID_TO_DESCRIPTOR.put(new Integer(LONG_ARRAY_TYPE_ID), LONG_DESCRIPTOR);
    }

    private static final HashMap  DESCRIPTOR_TO_TYPE_ID = new HashMap();

    static {
	DESCRIPTOR_TO_TYPE_ID.put(BOOLEAN_DESCRIPTOR, new Integer(BOOLEAN_ARRAY_TYPE_ID));
	DESCRIPTOR_TO_TYPE_ID.put(CHAR_DESCRIPTOR, new Integer(CHAR_ARRAY_TYPE_ID));
	DESCRIPTOR_TO_TYPE_ID.put(FLOAT_DESCRIPTOR, new Integer(FLOAT_ARRAY_TYPE_ID));
	DESCRIPTOR_TO_TYPE_ID.put(DOUBLE_DESCRIPTOR, new Integer(DOUBLE_ARRAY_TYPE_ID));
	DESCRIPTOR_TO_TYPE_ID.put(BYTE_DESCRIPTOR, new Integer(BYTE_ARRAY_TYPE_ID));
	DESCRIPTOR_TO_TYPE_ID.put(SHORT_DESCRIPTOR, new Integer(SHORT_ARRAY_TYPE_ID));
	DESCRIPTOR_TO_TYPE_ID.put(INT_DESCRIPTOR, new Integer(INT_ARRAY_TYPE_ID));
	DESCRIPTOR_TO_TYPE_ID.put(LONG_DESCRIPTOR, new Integer(LONG_ARRAY_TYPE_ID));
    }

    ///////////////////////////////////
    //
    // JVMDescriptor Methods
    //
    ///////////////////////////////////

    public static int
    getArrayTypeIdForDescriptor(String descriptor) {

	int  typeid = -1;

	if (DESCRIPTOR_TO_TYPE_ID.containsKey(descriptor))
	    typeid = ((Integer)(DESCRIPTOR_TO_TYPE_ID.get(descriptor))).intValue();
	return(typeid);
    }


    public static String
    getDeclarationFromDescriptor(String name, String descriptor) {

	StringBuffer  sbuf = null;
	boolean       acceptvoid;
	boolean       returntype;
	String        str;
	int           counter;
	int           parameters;
	int           dimensions;
	int           length;
	int           index;
	int           end;
	int           ch;

	if (descriptor != null && (length = descriptor.length()) > 0) {
	    parameters = -1;
	    dimensions = 0;
	    returntype = false;
	    sbuf = new StringBuffer();
	    for (index = 0, counter = 1; index < length && counter > 0 && sbuf != null; index++, counter--) {
		str = null;
		switch (ch = descriptor.charAt(index)) {
		    case DESCRIPTOR_ARRAY:
			dimensions++;
			counter++;
			break;

		    case DESCRIPTOR_BOOLEAN:
			str = BASE_TYPE_NAME_BOOLEAN;
			break;

		    case DESCRIPTOR_BYTE:
			str = BASE_TYPE_NAME_BYTE;
			break;

		    case DESCRIPTOR_CHAR:
			str = BASE_TYPE_NAME_CHAR;
			break;

		    case DESCRIPTOR_CLASS:
			if ((end = descriptor.indexOf(';', index)) > index) {
			    str = descriptor.substring(index + 1, end).replace('/', '.');
			    index = end;
			} else sbuf = null;
			break;

		    case DESCRIPTOR_DOUBLE:
			str = BASE_TYPE_NAME_DOUBLE;
			break;

		    case DESCRIPTOR_FLOAT:
			str = BASE_TYPE_NAME_FLOAT;
			break;

		    case DESCRIPTOR_INT:
			str = BASE_TYPE_NAME_INT;
			break;

		    case DESCRIPTOR_LONG:
			str = BASE_TYPE_NAME_LONG;
			break;

		    case DESCRIPTOR_SHORT:
			str = BASE_TYPE_NAME_SHORT;
			break;

		    case DESCRIPTOR_VOID:
			if (returntype) {
			    str = NAME_VOID;
			    returntype = false;
			} else sbuf = null;
			break;

		    default:
			if (ch == '(' && parameters < 0) {
			    if (name != null) {
				str = name + "(";
				name = null;
			    } else str = "(";
			    parameters = 0;
			    counter = 255;
			} else if (ch == ')' && parameters >= 0) {
			    sbuf.append(")");
			    parameters = -1;
			    counter = 2;
			    returntype = true;
			} else sbuf = null;
			break;
		}
		if (sbuf != null) {
		    if (str != null) {
			if (parameters >= 0) {
			    if (parameters++ > 1)
				sbuf.append(", ");
			}
			if (returntype) {
			    sbuf.insert(0, " ");
			    sbuf.insert(0, str);
			} else sbuf.append(str);
			for (; dimensions > 0; dimensions--)
			    sbuf.append("[]");
		    }
		}
	    }
	    if (index != length)
		sbuf = null;
	}

	if (sbuf != null && name != null) {
	    sbuf.append(" ");
	    sbuf.append(name);
	}

	return(sbuf != null && sbuf.length() > 0 ? sbuf.toString() : null);
    }


    public static int
    getDescriptorCategory(String descriptor) {

	int  category = 0;

	if (descriptor != null && descriptor.length() > 0) {
	    switch (descriptor.charAt(0)) {
		case DESCRIPTOR_BYTE:
		case DESCRIPTOR_CHAR:
		case DESCRIPTOR_FLOAT:
		case DESCRIPTOR_INT:
		case DESCRIPTOR_CLASS:
		case DESCRIPTOR_SHORT:
		case DESCRIPTOR_BOOLEAN:
		case DESCRIPTOR_ARRAY:
		case DESCRIPTOR_RETURNADDRESS:
		    category = 1;
		    break;

		case DESCRIPTOR_DOUBLE:
		case DESCRIPTOR_LONG:
		    category = 2;
		    break;
	    }
	}

	return(category);
    }


    public static int
    getDescriptorCode(String descriptor) {

	int  code = -1;

	if (descriptor != null && descriptor.length() > 0)
	    code = descriptor.charAt(0);
	return(code);
    }


    public static int
    getDescriptorCodeForArrayElement(String text) {

	return(getDescriptorCode(getDescriptorForArrayElement(text)));
    }


    public static int
    getDescriptorCodeForMethodReturn(String text) {

	return(getDescriptorCode(getDescriptorForMethodReturn(text)));
    }


    public static String
    getDescriptorForArray(int typeid) {

	return(ARRAY_DESCRIPTOR + (String)TYPE_ID_TO_DESCRIPTOR.get(new Integer(typeid)));
    }


    public static String
    getDescriptorForArray(String text) {

	Matcher  matcher;
	String   descriptor = null;
	String   type;
	int      index;

	if (isArrayDescriptor(text) == false && text != null) {
	    if ((index = text.indexOf(DESCRIPTOR_ARRAY)) >= 0) {
		type = text.substring(0, index);
		matcher = PATTERN_CLASS_TYPE.matcher(type);
		if (matcher.find()) {
		    if ((descriptor = getDescriptorForField(matcher.group(1), false)) != null) {
			for (; index < text.length(); index++) {
			    if (text.charAt(index) == DESCRIPTOR_ARRAY)
				descriptor = DESCRIPTOR_ARRAY + descriptor;
			}
		    }
		}
	    }
	} else descriptor = text;

	return(descriptor);
    }


    public static String
    getDescriptorForArrayByElement(String text) {

	String  descriptor;

	if ((descriptor = getDescriptorForField(text)) != null)
	    descriptor = ARRAY_DESCRIPTOR + descriptor;
	return(descriptor);
    }


    public static String
    getDescriptorForArrayElement(String text) {

	String  descriptor = null;
	int     index;

	if ((descriptor = getDescriptorForArray(text)) != null)
	    descriptor = descriptor.substring(descriptor.lastIndexOf(DESCRIPTOR_ARRAY) + 1);
	return(descriptor);
    }


    public static String
    getDescriptorForField(String text) {

	return(getDescriptorForField(text, false));
    }


    public static String
    getDescriptorForMethod(String text) {

	Matcher  matcher;
	String   returntype;
	String   methodname;
	String   parameters;
	String   formatted = null;

	//
	// Translates a text string that's supposed to represent a method in
	// a friendly format to a stricter format using descriptors for the
	// parameters and (optional) return type.
	//

	matcher = PATTERN_EXTERNAL_METHOD_TYPE.matcher(text);
	if (matcher.find()) {
	    returntype = getDescriptorForField(matcher.group(1));
	    text = matcher.replaceFirst("");
	} else returntype = null;

	matcher = PATTERN_EXTERNAL_METHOD_NAME.matcher(text);
	if (matcher.find()) {
	    methodname = matcher.group(1);
	    text = matcher.replaceFirst("");
	} else methodname = "";

	matcher = PATTERN_OPEN_PAREN.matcher(text);
	if (matcher.find()) {
	    text = matcher.replaceFirst("");
	    matcher = PATTERN_EXTERNAL_METHOD_PARAMETER.matcher(text);
	    parameters = "(";
	    while (matcher.find()) {
		text = matcher.replaceFirst("");
		parameters += getDescriptorForField(matcher.group(1));
		matcher = PATTERN_COMMA.matcher(text);
		if (matcher.find()) {
		    text = matcher.replaceFirst("");
		    matcher = PATTERN_EXTERNAL_METHOD_PARAMETER.matcher(text);
		} else break;
	    }
	    matcher = PATTERN_CLOSE_PAREN.matcher(text);
	    if (matcher.find()) {
		parameters += ")";
		formatted = methodname + parameters;
		if (returntype != null)
		    formatted += returntype;
	    }
	}

	return(formatted);
    }


    public static String
    getDescriptorForMethod(String returntype, ArrayList parametertypes) {

	String  descriptor = null;
	String  parameters;
	String  type;
	int     length;
	int     n;

	if (returntype != null) {
	    if ((returntype = getDescriptorForField(returntype, true)) != null) {
		parameters = "";
		if (parametertypes != null && (length = parametertypes.size()) > 0) {
		    for (n = 0; n < length; n++) {
			if ((type = getDescriptorForField((String)parametertypes.get(n), false)) == null) {
			    parameters = null;
			    break;
			} else parameters += type;
		    }
		}
		if (parameters != null)
		    descriptor = "(" + parameters + ")" + returntype;
	    }
	}

	return(descriptor);
    }


    public static String
    getDescriptorForMethodParameters(String descriptor) {

	int  start;
	int  end;

	if (isMethodDescriptor(descriptor)) {
	    if ((start = descriptor.indexOf('(')) >= 0) {
		if ((end = descriptor.indexOf(')')) >= 0)
		    descriptor = descriptor.substring(start + 1, end);
		else descriptor = null;
	    } else descriptor = null;
	} else descriptor = null;

	return(descriptor);
    }


    public static String
    getDescriptorForMethodReturn(String descriptor) {

	if (isMethodDescriptor(descriptor))
	    descriptor = descriptor.substring(descriptor.indexOf(')') + 1);
	else descriptor = null;

	return(descriptor);
    }


    public static int
    getDescriptorSize(String descriptor) {

	return(getDescriptorCategory(descriptor));
    }


    public static int
    getDescriptorSize(String descriptors[]) {

	return(getDescriptorSize(descriptors, descriptors != null ? descriptors.length : 0));
    }


    public static int
    getDescriptorSize(String descriptors[], int length) {

	int  size = 0;
	int  n;

	for (n = 0; n < length; n++)
	    size += getDescriptorSize(descriptors[n]);
	return(size);
    }


    public static String
    getNameFromDescriptor(String descriptor) {

	return(getDeclarationFromDescriptor(null, descriptor));
    }


    public static int
    getNextDescriptorIndex(String descriptor) {

	return(descriptor != null ? skipFieldDescriptor(descriptor, 0, descriptor.length()) : -1);
    }


    public static int
    getStackCountChange(String descriptor) {

	int  size;

	if ((size = measureDescriptor(descriptor, false, false)) >= 0) {
	    if (isMethodDescriptor(descriptor))
		size -= measureDescriptor(descriptor, false, true);
	}

	return(size);
    }


    public static int
    getStackSizeChange(String descriptor) {

	int  count;

	if ((count = measureDescriptor(descriptor, true, false)) >= 0) {
	    if (isMethodDescriptor(descriptor))
		count -= measureDescriptor(descriptor, true, true);
	}

	return(count);
    }


    public static int
    getStorageCount(String descriptor) {

	return(measureDescriptor(descriptor, false, false));
    }


    public static int
    getStorageSize(String descriptor) {

	return(measureDescriptor(descriptor, true, false));
    }


    public static String
    getTypeNameFromDescriptor(String descriptor) {

	return(getDeclarationFromDescriptor(null, descriptor));
    }


    public static boolean
    isArrayDescriptor(String descriptor) {

	return(isArrayDescriptor(descriptor, 0, (descriptor != null) ? descriptor.length() : -1));
    }


    public static boolean
    isArrayDescriptor(String descriptor, int offset, int length) {

	boolean  result;

	if (descriptor != null && offset >= 0 && offset < length && length <= descriptor.length()) {
	    if (descriptor.charAt(offset++) == DESCRIPTOR_ARRAY) {
		while (offset < length && descriptor.charAt(offset) == DESCRIPTOR_ARRAY)
		    offset++;
		result = (skipFieldDescriptor(descriptor, offset, length) == length);
	    } else result = false;
	} else result = false;

	return(result);
    }


    public static boolean
    isDescriptor(String descriptor) {

	return(isFieldDescriptor(descriptor) || isMethodDescriptor(descriptor));
    }


    public static boolean
    isDescriptor(String descriptor, int offset, int length) {

	return(isFieldDescriptor(descriptor, offset, length) || isMethodDescriptor(descriptor, offset, length));
    }


    public static boolean
    isFieldDescriptor(String descriptor) {

	return(isFieldDescriptor(descriptor, 0, (descriptor != null) ? descriptor.length() : -1));
    }


    public static boolean
    isFieldDescriptor(String descriptor, int offset, int length) {

	boolean  result;

	if (descriptor != null && offset >= 0 && offset < length && length <= descriptor.length())
	    result = (skipFieldDescriptor(descriptor, offset, length) == length);
	else result = false;

	return(result);
    }


    public static boolean
    isMethodDescriptor(String descriptor) {

	return(isMethodDescriptor(descriptor, 0, (descriptor != null) ? descriptor.length() : -1));
    }


    public static boolean
    isMethodDescriptor(String descriptor, int offset, int length) {

	boolean  result;

	if (descriptor != null && offset >= 0 && offset < length && length <= descriptor.length())
	    result = (skipMethodDescriptor(descriptor, offset, length) == length);
	else result = false;

	return(result);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static String
    getDescriptorForField(String text, boolean allowvoid) {

	Matcher  matcher;
	String   descriptor;

	if (isFieldDescriptor(text) == false && text != null) {
	    if ((descriptor = (String)BASE_TYPE_TO_DESCRIPTOR_MAP.get(text)) == null) {
		if (allowvoid == false || text.equals(NAME_VOID) == false) {
		    if (JVMMisc.isClassName(text))
			descriptor = DESCRIPTOR_CLASS + text.replace('.', '/') + ";";
		    else descriptor = getDescriptorForArray(text);
		} else descriptor = DESCRIPTOR_VOID + "";
	    }
	} else descriptor = text;

	return(descriptor);
    }


    private static int
    measureDescriptor(String descriptor, boolean storagesize, boolean skipparameters) {

	boolean  acceptvoid;
	boolean  inparameters;
	int      sizes[];
	int      size = 0;
	int      counter;
	int      length;
	int      index;
	int      ch;

	//
	// Returns the amount of storage needed by the JVM for the object
	// (or objects) are described by descriptor. The descriptor usually
	// represents a single object, however if it's a method descriptor
	// we calculate the storage used by its parameters (the caller still
	// has to add 1 to our result if "this" is included as an implicit
	// argument). A non-positive return means there was an error, but
	// we don't check as well as we probably should. For example, 'L'
	// followed by anything is accepted as long as we find the trailing
	// semicolon. 
	//

	if (descriptor != null && (length = descriptor.length()) > 0) {
	    sizes = new int[] {1, storagesize ? 2 : 1};
	    if (descriptor.charAt(0) == '(') {
		if (skipparameters) {
		    if ((index = descriptor.indexOf(')') + 1) <= 0) {
			index = length;
			size = -1;
		    }
		    counter = 1;
		    inparameters = false;
		    acceptvoid = true;
		} else {
		    index = 1;
		    counter = 255;
		    inparameters = true;
		    acceptvoid = false;
		}
	    } else {
		index = 0;
		counter = 1;
		inparameters = false;
		acceptvoid = false;
	    }
	    for (; index < length && counter > 0; index++, counter--) {
		switch (ch = descriptor.charAt(index)) {
		    case DESCRIPTOR_BYTE:
		    case DESCRIPTOR_CHAR:
		    case DESCRIPTOR_FLOAT:
		    case DESCRIPTOR_INT:
		    case DESCRIPTOR_SHORT:
		    case DESCRIPTOR_BOOLEAN:
		    case DESCRIPTOR_RETURNADDRESS:
			size += sizes[0];
			break;

		    case DESCRIPTOR_DOUBLE:
		    case DESCRIPTOR_LONG:
			size += sizes[1];
			break;

		    case DESCRIPTOR_CLASS:
			if ((index = descriptor.indexOf(';', index)) < 0) {
			    size = -1;
			    counter = -1;
			} else size += sizes[0];
			break;

		    case DESCRIPTOR_VOID:
			if (acceptvoid == false) {
			    size = -1;
			    counter = -1;
			}
			break;

		    case DESCRIPTOR_ARRAY:
			counter++;
			break;

		    default:
			if (inparameters && ch == ')') {
			    inparameters = false;
			    acceptvoid = true;
			    counter = 2;
			    sizes[0] = 0;
			    sizes[1] = 0;
			} else {
			    size = -1;
			    counter = -1;
			}
			break;
		}
	    }
	    if (index != length)
		size = -1;
	}

	return(size);
    }


    private static int
    skipFieldDescriptor(String descriptor, int offset, int length) {

	int  next;

	if (offset >= 0 && offset < length) {
	    switch (descriptor.charAt(offset++)) {
		case DESCRIPTOR_BYTE:
		case DESCRIPTOR_CHAR:
		case DESCRIPTOR_DOUBLE:
		case DESCRIPTOR_FLOAT:
		case DESCRIPTOR_INT:
		case DESCRIPTOR_LONG:
		case DESCRIPTOR_SHORT:
		case DESCRIPTOR_BOOLEAN:
		    break;

		case DESCRIPTOR_CLASS:
		    if ((next = JVMMisc.skipClassName(descriptor, offset, length, "/")) > offset) {
			offset = next;
			if (offset < length && descriptor.charAt(offset) == ';')
			    offset++;
			else offset = -1;
		    } else offset = -1;
		    break;

		case DESCRIPTOR_ARRAY:
		    offset = skipFieldDescriptor(descriptor, offset, length);
		    break;

		default:
		    offset = -1;
		    break;
	    }
	} else offset = -1;

	return(offset);
    }


    private static int
    skipMethodDescriptor(String descriptor, int offset, int length) {

	int  next;

	if (descriptor.charAt(offset++) == '(') {
	    offset = skipParameterDescriptors(descriptor, offset, length);
	    if (descriptor.charAt(offset++) == ')') {
		if (descriptor.charAt(offset) != DESCRIPTOR_VOID) {
		    if ((next = skipFieldDescriptor(descriptor, offset, length)) > offset)
			offset = next;
		    else offset = -1;
		} else offset++;
	    } else offset = -1;
	} else offset = -1;
	    
	return(offset);
    }


    private static int
    skipParameterDescriptors(String descriptor, int offset, int length) {

	int  next;

	while ((next = skipFieldDescriptor(descriptor, offset, length)) > offset)
	    offset = next;
	return(offset);
    }
}

