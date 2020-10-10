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

package att.research.yoix.ydat;
import java.io.*;
import java.text.*;
import java.util.*;
import att.research.yoix.*;

class DataGenerator

    implements Constants

{

    //
    // NOTE - the default return values from some builtins deserve a close
    // look. Did a few, but didn't have time to change and test some of the
    // others.
    //

    private YoixPointerActive  caller;
    private DataManager        datamanager;

    private boolean  accumulate;
    private boolean  enabled;
    private boolean  immediate;
    private boolean  resolved;
    private Object   datagenerator[];
    private Object   translator;
    private String   tag;
    private char     padding[];
    private int      id;
    private int      fieldindex;
    private int      type;
    private int      minwidth;
    private int      maxpad;
    private int      counter = 1;

    private YoixObject  args[];
    private double      stack[];

    //
    // These are used to remember the results of operations, like the ones
    // done by builtinMatch() and builtinStartsWith(), regular expressions
    // that were buit to handle the matching, and the objects that must be
    // used to decide if the results and/or regular expressions can be used
    // by the next operation.
    //

    private HashMap  matchvalues = null;
    private HashMap  matchexpressions = null;
    private Object   matchpatterns = null;
    private Object   matchlock = new Object();
    private Object   matchflags = null;

    //
    // An array used to help field validation.
    //

    private Object  validfields[] = {
	NL_ACCUMULATE, new Integer(VL_ACCUMULATE),
	NL_ENABLED, new Integer(VL_ENABLED),
	NL_GENERATOR, new Integer(VL_GENERATOR),
	NL_INDEX, new Integer(VL_INDEX),
	NL_TAG, new Integer(VL_TAG),
	NL_TRANSLATOR, new Integer(VL_TRANSLATOR),
	NL_TYPE, new Integer(VL_TYPE),
	NL_WIDTH, new Integer(VL_WIDTH),
    };

    //
    // Type values for data field descriptions.
    //

    private static final int  DATA_ARRAY = Module.DATA_ARRAY;
    private static final int  DATA_CALL = Module.DATA_CALL;
    private static final int  DATA_COUNTER = Module.DATA_COUNTER;
    private static final int  DATA_DICTIONARY = Module.DATA_DICTIONARY;
    private static final int  DATA_DOUBLE = Module.DATA_DOUBLE;
    private static final int  DATA_INTEGER = Module.DATA_INTEGER;
    private static final int  DATA_ONE = Module.DATA_ONE;
    private static final int  DATA_PARTITION = Module.DATA_PARTITION;
    private static final int  DATA_STRING = Module.DATA_STRING;
    private static final int  DATA_TABLE = Module.DATA_TABLE;
    private static final int  DATA_TABLE_NEW = Module.DATA_TABLE_NEW;
    private static final int  DATA_TABLECOLUMN = Module.DATA_TABLECOLUMN;
    private static final int  DATA_TABLECOLUMN_NEW = Module.DATA_TABLECOLUMN_NEW;
    private static final int  DATA_UID = Module.DATA_UID;
    private static final int  DATA_ZERO = Module.DATA_ZERO;

    //
    // Internal call constants.
    //

    private static final int  BUILTIN_ADJUSTTIME = Module.BUILTIN_ADJUSTTIME;
    private static final int  BUILTIN_ATOH = Module.BUILTIN_ATOH;
    private static final int  BUILTIN_BTOA = Module.BUILTIN_BTOA;
    private static final int  BUILTIN_BTOI = Module.BUILTIN_BTOI;
    private static final int  BUILTIN_CONSTANT = Module.BUILTIN_CONSTANT;
    private static final int  BUILTIN_CONTAINS = Module.BUILTIN_CONTAINS;
    private static final int  BUILTIN_DATE = Module.BUILTIN_DATE;
    private static final int  BUILTIN_ENDSWITH = Module.BUILTIN_ENDSWITH;
    private static final int  BUILTIN_EQUALS = Module.BUILTIN_EQUALS;
    private static final int  BUILTIN_GETFILEEXTENSION = Module.BUILTIN_GETFILEEXTENSION;
    private static final int  BUILTIN_GETINDEX = Module.BUILTIN_GETINDEX;
    private static final int  BUILTIN_GETKEY = Module.BUILTIN_GETKEY;
    private static final int  BUILTIN_GETQUERYSTRING = Module.BUILTIN_GETQUERYSTRING;
    private static final int  BUILTIN_GETSEARCHQUERY = Module.BUILTIN_GETSEARCHQUERY;
    private static final int  BUILTIN_GETVALUE = Module.BUILTIN_GETVALUE;
    private static final int  BUILTIN_HTOA = Module.BUILTIN_HTOA;
    private static final int  BUILTIN_HTOI = Module.BUILTIN_HTOI;
    private static final int  BUILTIN_LENGTH = Module.BUILTIN_LENGTH;
    private static final int  BUILTIN_MATCH = Module.BUILTIN_MATCH;
    private static final int  BUILTIN_MERCATORTOYDAT = Module.BUILTIN_MERCATORTOYDAT;
    private static final int  BUILTIN_PARSEDATE = Module.BUILTIN_PARSEDATE;
    private static final int  BUILTIN_PARSETIMER = Module.BUILTIN_PARSETIMER;
    private static final int  BUILTIN_PRINTF = Module.BUILTIN_PRINTF;
    private static final int  BUILTIN_RANDOM = Module.BUILTIN_RANDOM;
    private static final int  BUILTIN_REPLACE = Module.BUILTIN_REPLACE;
    private static final int  BUILTIN_SELECT = Module.BUILTIN_SELECT;
    private static final int  BUILTIN_SIZEOF = Module.BUILTIN_SIZEOF;
    private static final int  BUILTIN_STARTSWITH = Module.BUILTIN_STARTSWITH;
    private static final int  BUILTIN_STRFMT = Module.BUILTIN_STRFMT;
    private static final int  BUILTIN_SUBSTRING = Module.BUILTIN_SUBSTRING;
    private static final int  BUILTIN_TIMERFORMAT = Module.BUILTIN_TIMERFORMAT;
    private static final int  BUILTIN_TOLOWERCASE = Module.BUILTIN_TOLOWERCASE;
    private static final int  BUILTIN_TOUPPERCASE = Module.BUILTIN_TOUPPERCASE;
    private static final int  BUILTIN_TRIMQUERYSTRING = Module.BUILTIN_TRIMQUERYSTRING;
    private static final int  BUILTIN_URLDECODE = Module.BUILTIN_URLDECODE;
    private static final int  BUILTIN_URLENCODE = Module.BUILTIN_URLENCODE;

    //
    // Recognized and implemented boolean operators.
    //

    private static final HashMap  booleanoperators = new HashMap();

    static {
	booleanoperators.put("==", new Integer(EQ));
	booleanoperators.put("!=", new Integer(NE));
	booleanoperators.put("<", new Integer(LT));
	booleanoperators.put("<=", new Integer(LE));
	booleanoperators.put(">", new Integer(GT));
	booleanoperators.put(">=", new Integer(GE));
	booleanoperators.put("&", new Integer(AND));
	booleanoperators.put("&&", new Integer(AND));
	booleanoperators.put("|", new Integer(OR));
	booleanoperators.put("||", new Integer(OR));
    }

    //
    // A counter used to implement DATA_UID. We put it in an array so we
    // easily synchronize on something. Alternative, obviously, is a lock
    // Object.
    //

    private static int  nextid[] = new int[] {1};

    //
    // Constants used internally to implement DATA_ONE and DATA_ZERO.
    //

    private static final Integer  INTEGER_ONE = new Integer(1);
    private static final Integer  INTEGER_ZERO = new Integer(0);
    private static final Double   DOUBLE_ONE = new Double(1);
    private static final Double   DOUBLE_ZERO = new Double(0);
    private static final String   STRING_ONE = "1";
    private static final String   STRING_ZERO = "0";

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    DataGenerator(DataManager datamanager, YoixPointerActive caller, int id) {

	this.datamanager = datamanager;
	this.caller = caller;
	this.id = id;
	enabled = true;
	immediate = false;
	resolved = false;
	maxpad = 0;
    }

    ///////////////////////////////////
    //
    // DataGenerator Methods
    //
    ///////////////////////////////////

    final String
    construct(YoixObject dict) {

	YoixObject  obj;
	String      error;
	int         n;

	if ((error = validate(dict)) == null) {
	    enabled = dict.getBoolean(NL_ENABLED, true);
	    accumulate = dict.getBoolean(NL_ACCUMULATE, false);
	    fieldindex = dict.getInt(NL_INDEX, -1);
	    tag = dict.getString(NL_TAG, "$_" + id);
	    type = dict.getInt(NL_TYPE, DATA_STRING);
	    minwidth = dict.getInt(NL_WIDTH, 0);
	    constructDataGenerator(dict.getObject(NL_GENERATOR));
	    constructTranslator(dict.getObject(NL_TRANSLATOR));

	    padding = new char[Math.max(maxpad, minwidth)];
	    for (n = 0; n < maxpad; n++)
		padding[n] = ' ';
	}

	return(error);
    }


    static Object
    generateData(Object args[]) {

	DataGenerator  generator;
	Object         context[];
	Object         value = null;
	int            id;

	if (args.length == 2) {
	    if (args[0] instanceof DataGenerator) {
		if (args[1] instanceof Object[]) {
		    generator = (DataGenerator)args[0];
		    context = (Object[])args[1];
		    //if ((id = generator.getID()) >= 0 && id < context.length)
		    context[id = generator.getID()] = null;
		    value = context[id] = generator.generate(context);
		}
	    }
	}

	return(value);
    }


    final boolean
    getAccumulate() {

	return(accumulate);
    }


    final boolean
    getEnabled() {

	return(enabled);
    }


    final int
    getID() {

	return(id);
    }


    final int
    getIndex() {

	return(fieldindex);
    }


    final String
    getTag() {

	return(tag);
    }


    final int
    getType() {

	return(type);
    }


    final void
    loadField(Object fields[], String buffer[], int entries) {

	String  source;

	if (fields[id] == null) {
	    if ((fields[id] = translate(buffer, entries)) == null) {
		if (immediate)
		    fields[id] = generate(fields);
		else fields[id] = new Object[] {this, fields};
	    } else fields[id] = generate(fields);
	}
    }


    final Object
    generate(Object context[]) {

	return(generate(context, datagenerator));
    }


    private final Object
    generate(Object context[], Object generator[]) {

	Object  value = null;

	//
	// We eventually may include an argument (e.g., a BitMask) that
	// will let us recognize infinite loops. Actually there may be
	// an alternative - generateData() might be able to replace the
	// entry in context[] by null before calling generate().
	//
	// Added code (4/24/05) at the end of this method that tries to
	// apply translators to generated fields. Partitions are harder
	// and needed additions in generatePartition().
	//
	// Recently (8/26/05) changed DATA_DOUBLE and DATA_INTEGER cases
	// to return null value when generator is null and value starts
	// out as the empty string (i.e., it looks like it came from an
	// empty field). Seems reasonable, but needs to thoroughly tested.
	//

	if (generator != null) {
	    switch (type) {
		case DATA_ARRAY:
		    value = generateArray(context, generator);
		    break;

		case DATA_CALL:
		    value = generateCall(context, generator);
		    break;

		case DATA_COUNTER:
		    value = new Integer(counter++);
		    break;

		case DATA_DICTIONARY:
		    value = generateDictionary(context, generator);
		    break;

		case DATA_DOUBLE:
		    value = new Double(generateNumber(context, generator, 0));
		    break;

		case DATA_INTEGER:
		    value = new Integer((int)generateNumber(context, generator, 0));
		    break;

		case DATA_ONE:
		    value = INTEGER_ONE;
		    break;

		case DATA_PARTITION:
		    value = generatePartition(context, generator);
		    break;

		case DATA_STRING:
		    value = generateString(context, generator);
		    break;

		case DATA_TABLE:
		case DATA_TABLE_NEW:
		case DATA_TABLECOLUMN:
		case DATA_TABLECOLUMN_NEW:
		    value = generateTable(context, generator);
		    break;

		case DATA_UID:
		    value = new Integer(getNextUID());
		    break;

		case DATA_ZERO:
		    value = INTEGER_ZERO;
		    break;
	    }
	} else {
	    value = context[id];
	    if (value instanceof DataPartition)
		value = ((DataPartition)value).data;
	    if (value != null) {
		switch (type) {
		    case DATA_COUNTER:
			value = new Integer(counter++);
			break;

		    case DATA_DOUBLE:
			if (!(value instanceof Double)) {
			    if (value instanceof String) {
				if (((String)value).length() > 0)
				    value = new Double(YoixMake.javaDouble((String)value, 0));
			    } else if (value instanceof Number)
				value = new Double(((Number)value).doubleValue());
			    else value = new Double(YoixMake.javaDouble(value));
			}
			break;

		    case DATA_INTEGER:
			if (!(value instanceof Integer)) {
			    if (value instanceof String) {
				if (((String)value).length() > 0)
				    value = new Integer((int)YoixMake.javaDouble((String)value, 0));
			    } else if (value instanceof Number)
				value = new Integer(((Number)value).intValue());
			    else value = new Integer((int)YoixMake.javaDouble(value));
			}
			break;

		    case DATA_ONE:
			value = INTEGER_ONE;
			break;

		    case DATA_STRING:
			if (!(value instanceof String))
			    value = value.toString();
			break;

		    case DATA_UID:
			value = new Integer(getNextUID());
			break;

		    case DATA_ZERO:
			value = INTEGER_ZERO;
			break;
		}
	    } else {
		switch (type) {
		    case DATA_COUNTER:
			value = new Integer(counter++);
			break;

		    case DATA_ONE:
			value = INTEGER_ONE;
			break;

		    case DATA_UID:
			value = new Integer(getNextUID());
			break;

		    case DATA_ZERO:
			value = INTEGER_ZERO;
			break;
		}
	    }
	}

	if (value instanceof String) {
	    //
	    // Recently (4/24/05) decided to apply translators if we're sure
	    // value didn't come directly from the input data. The previous
	    // version essentially only applied translators in loadField(),
	    // which means they never worked with generated fields.
	    //
	    if (translator != null) {
		if (fieldindex < 0 || fieldindex >= context.length)
		    value = translate((String)value);
	    }
	    if (minwidth > 0)
		value = padString((String)value, minwidth, 0, YOIX_RIGHT);
	} else if (value instanceof Number) {
	    if (translator != null) {
		if (fieldindex < 0 || fieldindex >= context.length)
		    value = translate(value.toString());
	    }
	}
	return(value);
    }


    final void
    resolve() {

	if (datagenerator != null && datagenerator.length > 0) {
	    switch (type) {
		case DATA_ARRAY:
		    resolveTags(0, -1, false);
		    break;

		case DATA_CALL:
		    if (datagenerator[0] instanceof YoixObject)
			resolveTags(1, 1, false);
		    break;

		case DATA_DICTIONARY:
		    resolveTags(0, 1, false);
		    break;

		case DATA_DOUBLE:
		    resolveTags(0, 1, false);
		    break;

		case DATA_INTEGER:
		    resolveTags(0, 1, false);
		    break;

		case DATA_PARTITION:
		    resolveTags(0, 1, false);
		    if (datagenerator[0] instanceof Integer) {
			datamanager.setTagIndex(tag, ((Integer)datagenerator[0]).intValue());
			datamanager.setTagPartition(tag, this.id);
		    }
		    break;

		case DATA_STRING:
		    resolveTags(0, 1, false);
		    break;

		case DATA_TABLE:
		case DATA_TABLE_NEW:
		case DATA_TABLECOLUMN:
		case DATA_TABLECOLUMN_NEW:
		    resolveTags(0, 6, true);
		    break;
	    }
	}
    }


    final String
    translate(String value) {

	if (translator != null) {
	    if (translator instanceof YoixObject) {
		if (((YoixObject)translator).isDictionary())
		    value = lookup(value, (YoixObject)translator);
		else value = call((YoixObject)translator, value);
	    } else if (translator instanceof HashMap)
		value = lookup(value, (HashMap)translator);
	}
	return(value);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private Object
    builtinAdjustTime(Object context[], Object generator[]) {

	TimeZone  timezone;
	Object    value = null;
	Object    arg;
	Object    zone;
	Date      date;
	long      time;

	if (resolved == false)
	    resolveTags(1, 1, false);

	if (generator.length == 2 || generator.length == 3) {
	    if ((arg = getArgument(generator, 1, context)) != null) {
		if (arg instanceof Number) {
		    time = ((Number)arg).longValue();
		    timezone = null;
		    if (generator.length == 3) {
			zone = getArgument(generator, 2, context);
			if (zone instanceof YoixObject) {
			    if ((timezone = ((YoixObject)zone).timeZoneValue()) != null)
				generator[1] = timezone;
			} else if (zone instanceof TimeZone)
			    timezone = (TimeZone)zone;
		    }
		    if (timezone != null)
			time += timezone.getOffset(1000*time)/1000;
		    value = new Double(time);
		}
	    }
	}

	return(value);
    }


    private String
    builtinAtoh(Object context[], Object generator[]) {

	String  value = null;
	Object  arg;

	if (resolved == false)
	    resolveTags(1, 1, false);

	if (generator.length == 2) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof String)
		value = YoixMisc.hexFromAscii((String)arg);
	}

	return(value);
    }


    private String
    builtinBtoa(Object context[], Object generator[]) {

	String  value = null;
	Object  arg;

	if (resolved == false)
	    resolveTags(1, 1, false);

	if (generator.length == 2) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof String) {
		try {
		    value = Integer.toString(Integer.parseInt((String)arg, 2));
		}
		catch(NumberFormatException e) {}
	    }
	}

	return(value);
    }


    private Integer
    builtinBtoi(Object context[], Object generator[]) {

	Integer  value = null;
	Object   arg;

	if (resolved == false)
	    resolveTags(1, 1, false);

	if (generator.length == 2) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof String) {
		try {
		    value = new Integer(Integer.parseInt((String)arg, 2));
		}
		catch(NumberFormatException e) {}
	    }
	}

	return(value);
    }


    private Object
    builtinConstant(Object context[], Object generator[]) {

	Object  value = null;

	if (generator.length == 2) {
	    if (generator[1] instanceof Object[]) {
		value = generate(context, (Object[])generator[1]);
		generator[1] = value;
	    } else value = generator[1];
	}

	return(value);
    }


    private Integer
    builtinContains(Object context[], Object generator[]) {

	boolean  result = false;
	Object   source;
	Object   key;

	if (resolved == false)
	    resolveTags(1, 1, false);

	if (generator.length == 3) {
	    source = getArgument(generator, 1, context);
	    key = getArgument(generator, 2, context);
	    if (key instanceof String) {
		if (source instanceof String)
		    result = (((String)source).indexOf((String)key) >= 0);
		else if (source instanceof List)
		    result = ((List)source).contains(key);
		else if (source instanceof Map)
		    result = ((Map)source).containsKey(key);
		else if (source instanceof YoixObject)
		    result = ((YoixObject)source).defined((String)key);
	    }
	}

	return(result ? INTEGER_ONE : INTEGER_ZERO);
    }


    private String
    builtinDate(Object context[], Object generator[]) {

	YoixSimpleDateFormat  sdf;
	TimeZone              timezone;
	String                result = null;
	double                time;
	int                   n;

	if (resolved == false)
	    resolveTags(2, 1, false);

	if (generator.length > 2) {
	    n = 1;
	    timezone = null;
	    if (generator.length > 3) {
		if (generator[1] instanceof YoixObject) {
		    if ((timezone = ((YoixObject)generator[1]).timeZoneValue()) != null)
			generator[1] = null;
		}
		n++;
	    }
	    if (generator[n] instanceof String) {
		if (timezone == null)
		    timezone = YoixMiscTime.getDefaultTimeZone();
		sdf = new YoixSimpleDateFormat((String)generator[n]);
		sdf.setTimeZone(timezone);
		sdf.setLenient(true);
		generator[n] = sdf;
	    }
	    if (generator[n] instanceof YoixSimpleDateFormat) {
		time = generateNumber(context, generator, n + 1);
		if (Double.isNaN(time) == false)
		    result = MiscTime.getDate((YoixSimpleDateFormat)generator[n], time);
		else result = "";
	    }
	}

	return(result);
    }


    private Object
    builtinEndsWith(Object context[], Object generator[]) {

	boolean  sorted;
	Object   arg;
	Object   value = "";
	Object   patterns;
	Object   key;
	Object   flags;

	//
	// We ignore the sorted argument, but don't consider it an error
	// if it's supplied.
	//

	if (resolved == false)
	    resolveTags(new int[] {1, 2}, false);

	if (generator.length == 3 || generator.length == 4) {
	    if ((key = getArgument(generator, 1, context)) != null) {
		if ((patterns = getArgument(generator, 2, context)) != null) {
		    sorted = false;
		    flags = BUILTIN_ENDSWITH + "";
		    if (key instanceof String)
			value = matchString(BUILTIN_ENDSWITH, (String)key, patterns, flags, sorted);
		}
	    }
	}
	return(value);
    }


    private Object
    builtinEquals(Object context[], Object generator[]) {

	boolean  sorted;
	Object   arg;
	Object   value = "";
	Object   patterns;
	Object   key;
	Object   flags;

	if (resolved == false)
	    resolveTags(new int[] {1, 2}, false);

	if (generator.length == 3 || generator.length == 4) {
	    if ((key = getArgument(generator, 1, context)) != null) {
		if ((patterns = getArgument(generator, 2, context)) != null) {
		    if (generator.length == 4) {
			arg = getArgument(generator, 3, context);
			if (arg instanceof Number)
			    sorted = ((Number)arg).intValue() != 0;
			else sorted = false;
		    } else sorted = false;
		    flags = BUILTIN_EQUALS + "";
		    if (key instanceof String)
			value = matchString(BUILTIN_EQUALS, (String)key, patterns, flags, sorted);
		}
	    }
	}
	return(value);
    }


    private Object
    builtinGetFileExtension(Object context[], Object generator[]) {

	boolean  skipquery;
	Object   arg;
	String   value = "";
	String   path;
	String   delimiters;
	int      changecase;
	int      length;
	int      index;
	int      argn;
	int      n;

	if (resolved == false)
	    resolveTags(new int[] {1}, false);

	if (generator.length == 2 || generator.length == 3 || generator.length == 4) {
	    argn = 1;
	    arg = getArgument(generator, argn++, context);
	    if (arg instanceof String) {
		path = (String)arg;
		changecase = 0;
		skipquery = true;
		if (argn < generator.length)
		    skipquery = getBooleanValue(generator[argn++], skipquery);
		if (argn < generator.length) {
		    if (generator[argn] instanceof Number)
			changecase = ((Number)generator[argn++]).intValue();
		}
		if (skipquery) {
		    if ((index = path.indexOf('#')) >= 0)
			path = path.substring(0, index);
		    if ((index = path.lastIndexOf('?')) >= 0)
			path = path.substring(0, index);
		}
		if ((index = path.lastIndexOf('.')) >= 0) {
		    if (index > path.lastIndexOf('/')) {
			value = path.substring(index + 1);
			if (changecase > 0)
			    value = value.toUpperCase();
			else if (changecase < 0)
			    value = value.toLowerCase();
		    }
		}
	    }
	}
	return(value);
    }


    private Object
    builtinGetIndex(Object context[], Object generator[]) {

	YoixObject  obj;
	ArrayList   list;
	Object      value = "";
	Object      arg;
	int         index;

	if (resolved == false)
	    resolveTags(1, -1, false);

	if (generator.length >= 3) {
	    if ((arg = getArgument(generator, 1, context)) != null) {
		if (generator[2] instanceof Number) {
		    try {
			index = ((Number)generator[2]).intValue();
			if (arg instanceof YoixObject) {
			    if ((obj = ((YoixObject)arg).getObject(index)) != null) {
				if (obj.notNull()) {
				    if (obj.isString())
					value = new String(obj.stringValue());
				    else if (obj.isNumber())
					value = new Double(obj.doubleValue());
				    else value = obj;
				}
			    }
			} else if (arg instanceof String) {
			    if (generator.length == 3)
				value = new Integer(((String)arg).charAt(index));
			    else if ((list = splitIntoList((String)arg, generator, 3, true)) != null)
				value = list.get(index);
			} else if (arg instanceof ArrayList)
			    value = ((ArrayList)arg).get(index);
		    }
		    catch(IndexOutOfBoundsException e) {}
		}
	    }
	}
	return(value);
    }


    private Object
    builtinGetKey(Object context[], Object generator[]) {

	Object  value = "";
	Object  arg;
	String  key;

	if (resolved == false)
	    resolveTags(1, -1, false);

	if (generator.length == 3) {
	    if ((arg = getArgument(generator, 1, context)) != null) {
		if (generator[2] instanceof String) {
		    key = (String)generator[2];
		    if (arg instanceof HashMap) {
			if (((HashMap)arg).containsKey(key))
			    value = key;
		    } else if (arg instanceof YoixObject) {
			if (((YoixObject)arg).defined(key))
			    value = key;
		    }
		}
	    }
	}
	return(value);
    }


    private Object
    builtinGetQueryString(Object context[], Object generator[]) {

	boolean  decode;
	boolean  ietf;
	Object   arg;
	String   value = "";
	String   path;
	char     separator;
	int      index;

	//
	// We currently don't support an argument that changes ietf, which
	// among other things means spaces aren't represented by '+'. This
	// seems to be the version of the encoding used in httpd log files.
	//

	if (resolved == false)
	    resolveTags(new int[] {1}, false);

	if (generator.length == 2 || generator.length == 3 || generator.length == 4) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof String) {
		path = (String)arg;
		decode = true;
		ietf = true;		// currently can't be changed
		separator = '\0';
		if (generator.length > 2) {
		    decode = getBooleanValue(generator[2], decode);
		    if (generator.length > 3) {
			arg = generator[3];
			if (arg instanceof String)
			    separator = (((String)arg).length() > 0) ? ((String)arg).charAt(0) : '\0';
			else if (arg instanceof Number)
			    separator = (char)((Number)arg).intValue();
		    }
		}
		if ((index = path.indexOf('#')) >= 0)
		    path = path.substring(0, index);
		if ((index = path.lastIndexOf('?')) >= 0) {
		    value = path.substring(index + 1);
		    if (separator != '\0' && separator != '&')
			value = value.replace('&', separator);
		    if (decode && value.length() > 0)
			value = YoixMisc.urlToAscii(((String)value).toCharArray(), ietf, true);
		}
	    }
	}
	return(value);
    }


    private Object
    builtinGetSearchQuery(Object context[], Object generator[]) {

	boolean  decode;
	boolean  ietf;
	Object   arg;
	String   value = "";
	String   path;
	String   prefix;
	int      length;
	int      start;
	int      end;
	int      index;

	//
	// Search engine query strings found in httpd log files represent
	// spaces using '+', so in this case the ietf argument handed to
	// YoixMisc.urlToAscii() needs to be false and there's currently
	// no way to change it.
	//
	// NOTE - eventually might want a way to specify several prefixes
	// or maybe handle regular expression matching, but right now it
	// looks like the important search engines all use "q=".
	//

	if (resolved == false)
	    resolveTags(new int[] {1}, false);

	if (generator.length == 2 || generator.length == 3 || generator.length == 4) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof String) {
		path = (String)arg;
		decode = true;
		ietf = false;		// currently can't be changed
		prefix = "q=";
		if (generator.length > 2) {
		    decode = getBooleanValue(generator[2], decode);
		    if (generator.length > 3) {
			arg = generator[3];
			if (arg instanceof String)
			    prefix = (String)arg;
		    }
		}
		if ((index = path.indexOf('#')) >= 0)
		    path = path.substring(0, index);
		if ((index = path.lastIndexOf('?')) >= 0) {
		    if ((length = prefix.length()) > 0) {
			path = path.substring(index + 1);
			if ((index = path.indexOf(prefix)) >= 0) {
			    if (index == 0 || path.charAt(index-1) == '&') {
				start = index + length;
				if ((end = path.indexOf('&', start)) >= 0)
				    value = path.substring(start, end);
				else value = path.substring(start);
				if (decode && value.length() > 0)
				    value = YoixMisc.urlToAscii(((String)value).toCharArray(), ietf, true);
			    }
			}
		    }
		}
	    }
	}
	return(value);
    }


    private Object
    builtinGetValue(Object context[], Object generator[]) {

	YoixObject  obj;
	Object      value = "";
	Object      arg;
	Object      key;

	//
	// Original implementation didn't resolve the key string, which
	// normally is the right thing to do, however there are occasions
	// when we do want to resolve it, so we added an optional argument
	// that controls the resolveTags() call.
	//

	if (resolved == false) {
	    if (generator.length == 4) {
		if (generator[3] instanceof Number && ((Number)generator[3]).intValue() != 0)
		    resolveTags(1, 1, false);
		else resolveTags(1, -1, false);
	    } else resolveTags(1, -1, false);
	}

	if (generator.length == 3 || generator.length == 4) {
	    if ((arg = getArgument(generator, 1, context)) != null) {
		key = getArgument(generator, 2, context);
		if (key instanceof String) {
		    if (arg instanceof YoixObject) {
			if ((obj = ((YoixObject)arg).getObject((String)key)) != null) {
			    if (obj.notNull()) {
				if (obj.isString())
				    value = new String(obj.stringValue());
				else if (obj.isNumber())
				    value = new Double(obj.doubleValue());
				else value = obj;
			    }
			}
		    } else if (arg instanceof HashMap)
			value = ((HashMap)arg).get(key);
		}
	    }
	}
	return(value);
    }


    private String
    builtinHtoa(Object context[], Object generator[]) {

	String  value = null;
	Object  arg;

	if (resolved == false)
	    resolveTags(1, 1, false);

	if (generator.length == 2) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof String)
		value = YoixMisc.hexToAscii((String)arg);
	}

	return(value);
    }


    private Integer
    builtinHtoi(Object context[], Object generator[]) {

	Integer  value = null;
	Object   arg;

	if (resolved == false)
	    resolveTags(1, 1, false);

	if (generator.length == 2) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof String) {
		try {
		    value = new Integer(Integer.parseInt((String)arg, 16));
		}
		catch(NumberFormatException e) {}
	    }
	}

	return(value);
    }


    private Integer
    builtinLength(Object context[], Object generator[]) {

	Object  arg;
	int     length = -1;

	if (resolved == false)
	    resolveTags(1, 1, false);

	if (generator.length == 2) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof String)
		length = ((String)arg).length();
	    else if (arg instanceof List)
		length = ((List)arg).size();
	    else if (arg instanceof Map)
		length = ((Map)arg).size();
	    else if (arg instanceof YoixObject)
		length = ((YoixObject)arg).length();
	    else if (arg == null)
		length = 0;
	}

	return(length >= 0 ? new Integer(length) : null);
    }


    private Object
    builtinMatch(Object context[], Object generator[]) {

	Object  value = "";
	Object  patterns;
	Object  key;
	Object  flags;

	//
	// This builtin is designed for flexibility rather than matching
	// against big lists, so expect a performance hit if you get too
	// greedy. Sorted lists won't work here because every pattern in
	// the list can be an arbitrary regular expressions so we always
	// pass false as the sorted argument.
	//

	if (resolved == false)
	    resolveTags(new int[] {1, 2}, false);

	if (generator.length == 3 || generator.length == 4) {
	    if ((key = getArgument(generator, 1, context)) != null) {
		if ((patterns = getArgument(generator, 2, context)) != null) {
		    if (generator.length == 4 && generator[3] instanceof Number)
			flags = generator[3];
		    else flags = new Integer(SHELL_PATTERN);
		    if (key instanceof String)
			value = matchString(BUILTIN_MATCH, (String)key, patterns, flags, false);
		}
	    }
	}
	return(value);
    }


    private String
    builtinMercatorToYDAT(Object context[], Object generator[]) {

	boolean  ismark;
	String   value = null;
	Object   source;
	Object   arg;
	Object   matrix;
	String   command;
	double   width;
	double   height;
	int      fields;
	int      left;
	int      right;
	int      delim;

	//
	// Translates the latitude and longitude coordinates extracted from
	// a source string, which is the first argument, to coordinates in
	// the mercator projection and then builds a description of a path
	// in the format that can be handled by the separateText() "parser"
	// in SwingJGraphPlot. The second, third, and fourth arguments are
	// the special characters that are used to extract the latitude and
	// longitude from the source string. The fifth argument, which is
	// optional, is the number of fields to skip to get to the start of
	// the next latitude and longitude pair (the default is 2 and means
	// source only contains latitude and longitude data). The remaining
	// arguments, which are also all optional, describe the path that's
	// supposed to be built in format returned by Misc.mercatorToYDAT().
	// See he Misc.parseCommand() method for a complete description of
	// the allowed commands (i.e., the sixth argument).
	//
	// The commands currently recognized are 'E' or 'e' (ellipse), 'R'
	// or 'r' (rectangle), 'V' or 'v' (down arrow), '^' (up arrow), '<'
	// (left arrow), and '>' (right arrow). Two other commands, namely
	// 'P' and 'S', let you build a polygon or shape outlines by Bezier
	// curves that is drawn in the "unit square" centered at the point
	// specified by the latitude and longitude. For example,
	//
	//	"P 3 0 0 -0.5 -1 0.5 -1"
	//
	// draws the same shape as the 'V' or 'v' commands, while
	//
	//	"S 13 0 0 -0.1 -0.8 -0.4 -0.9 -0.5 -1.0 -0.5 -1.0 -0.5 -1.0 0.5 -1.0 0.4 -0.9 0.1 -0.8 0 0.0"
	//
	// draws a little fancier down arrow that's outline by curves. In
	// both cases the integer following the command letter (i.e., 'P'
	// or 'B') is the number of points in the polygon or shape. For
	// shapes the number of points minus 1 must be divisible by three.
	// Drawing outside the "unit square" is allowed. If no command is
	// supplied or it's not recognized a default shape (currently an
	// ellipse) is selected. The optional width and height arguments
	// adjust the size and scaling of the "unit square", however the
	// units depend on whether the path is a "fixed size" mark or a
	// node whose size changes as the user zooms in and out. In the
	// case of a mark with and height are in default Yoix coordinates,
	// namely 72 dots per inch, but scalable nodes should have their
	// width and height suppied in "degress", which means the values
	// probably should be less than one. Reasonable defaults are used
	// if width or height aren't supplied.
	//
	// NOTE - the origin (i.e., 0 0) should be the first point in any
	// mark that you construct using 'P' or 'S' directives. There's a
	// mistake somewhere in SwingJGraphPlot that affects the placement
	// of the mark during zooming if the origin isn't the first point.
	// We eventually will track it down, but this is all very new code
	// that right now is mostly experimental.
	//

	if (resolved == false)
	    resolveTags(1, 1, false);

	if (generator.length > 4) {
	    if ((source = getArgument(generator, 1, context)) != null) {
		if (source instanceof String && ((String)source).length() > 0) {
		    arg = getArgument(generator, 2, context);
		    left = (arg instanceof Number) ? ((Number)arg).intValue() : -1;
		    arg = getArgument(generator, 3, context);
		    delim = (arg instanceof Number) ? ((Number)arg).intValue() : -1;
		    arg = getArgument(generator, 4, context);
		    right = (arg instanceof Number) ? ((Number)arg).intValue() : -1;

		    if (generator.length > 5) {
			arg = getArgument(generator, 5, context);
			fields = (arg instanceof Number) ? ((Number)arg).intValue() : 2;
		    } else fields = 2;

		    command = null;
		    width = 0;
		    height = 0;
		    ismark = true;
		    matrix = null;

		    if (generator.length > 6) {
			arg = getArgument(generator, 6, context);
			if (arg instanceof String)
			    command = (String)arg;
			else if (arg instanceof Number)
			    command = String.valueOf((char)((Number)arg).intValue());
			if (generator.length > 7) {
			    arg = getArgument(generator, 7, context);
			    width = (arg instanceof Number) ? ((Double)arg).doubleValue() : 0;
			    if (generator.length > 8) {
				arg = getArgument(generator, 8, context);
				height = (arg instanceof Number) ? ((Double)arg).doubleValue() : 0;
				if (generator.length > 9) {
				    arg = getArgument(generator, 9, context);
				    ismark = (arg instanceof Number) ? (((Number)arg).intValue() != 0) : true;
				    if (generator.length > 10)
					matrix = getArgument(generator, 10, context);
				}
			    }
			}
		    }
		    value = Misc.mercatorToYDAT((String)source, left, delim, right, fields, command, width, height, ismark, matrix);
		}
	    }
	}
	return(value);
    }


    private Double
    builtinParseDate(Object context[], Object generator[]) {

	YoixSimpleDateFormat  sdf;
	TimeZone              timezone = null;
	Double                value = null;
	Object                format = null;
	Object                date = null;
	Object                arg;
	int                   length;
	int                   n;

	if (resolved == false)
	    resolveTags(1, 1, false);

	switch (length = generator.length) {
	    case 1:
		date = getArgument(context);
		format = UNIX_DATE_FORMAT;
		break;

	    case 2:
		date = getArgument(generator, 1, context);
		format = UNIX_DATE_FORMAT;
		break;

	    case 3:
		date = getArgument(generator, 1, context);
		format = getArgument(generator, 2, context);
		break;

	    default:
		if (length > 3) {
		    n = 1;
		    if (generator[1] instanceof YoixObject) {
			if ((timezone = ((YoixObject)generator[1]).timeZoneValue()) != null)
			    generator[1] = null;
			n++;
		    }
		    for (; n < length - 1; n++) {
			arg = getArgument(generator, n, context);
			if (arg instanceof String) {
			    if (date != null)
				date = (String)date + (String)arg;
			    else date = arg;
			}
		    }
		    format = getArgument(generator, n, context);
		}
		break;
	}

	if (!(format instanceof YoixSimpleDateFormat)) {
	    if (format instanceof String) {
		if (timezone == null)
		    timezone = YoixMiscTime.getDefaultTimeZone();
		sdf = new YoixSimpleDateFormat((String)format);
		sdf.setTimeZone(timezone);
		sdf.setLenient(true);
		if (length > 2)
		    generator[length - 1] = sdf;
	    } else sdf = null;
	} else sdf = (YoixSimpleDateFormat)format;

	if (date instanceof String) {
	    if (sdf != null)
		value = new Double(YoixMiscTime.parseDate((String)date, sdf, 0.0));
	    else value = null;
	} else value = null;

	return(value);
    }


    private Double
    builtinParseTime(Object context[], Object generator[]) {

	Object  duration;

	if (resolved == false)
	    resolveTags(1, 1, false);

	if (generator.length > 1)
	    duration = getArgument(generator, 1, context);
	else duration = getArgument(context);

	return((duration instanceof String) 
	    ? new Double(YoixMiscTime.parseTimer((String)duration))
	    : null
	);
    }


    private String
    builtinPrintf(Object context[], Object generator[]) {

	String  result;

	result = builtinStrfmt(context, generator);
	VM.print(N_STDOUT, result);
	return(result);
    }


    private Number
    builtinRandom(Object context[], Object generator[]) {

	Number  value;
	Object  arg;
	double  random = Math.random();
	double  left;
	double  right;

	if (resolved == false)
	    resolveTags(1, 1, false);

	if (generator.length > 2) {
	    arg = getArgument(generator, 1, context);
	    left = (arg instanceof Number) ? ((Number)arg).doubleValue() : 0;
	    arg = getArgument(generator, 2, context);
	    right = (arg instanceof Number) ? ((Number)arg).doubleValue() : 1;
	    random = left + random*(right - left);
	    if (generator.length > 3) {
		arg = getArgument(generator, 3, context);
		if (arg instanceof String) {
		    try {
			random += Double.parseDouble((String)arg);
		    }
		    catch(NumberFormatException e) {}
		} else if (arg instanceof Number)
		    random += ((Number)arg).doubleValue();
	    }
	    value = new Integer((int)(random + .5));
	} else value = new Double(random);

	return(value);
    }


    private String
    builtinReplace(Object context[], Object generator[]) {

	Object  arg;
	Object  oldchar;
	Object  newchar;
	String  result = null;
	char    chars[];
	char    ch;
	int     length;
	int     m;
	int     n;

	if (resolved == false)
	    resolveTags(1, 1, false);

	if (generator.length == 3 || generator.length == 4) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof String) {
		result = (String)arg;
		oldchar = getArgument(generator, 2, context);
		if (generator.length == 3) {
		    ch = (char)(((Number)oldchar).intValue());
		    chars = result.toCharArray();
		    length = chars.length;
		    for (n = 0, m = 0; n < length; n++) {
			if (chars[n] != ch)
			    chars[m++] = chars[n];
		    }
		    result = new String(chars, 0, m);
		} else {
		    newchar = getArgument(generator, 3, context);
		    if (oldchar instanceof Number && newchar instanceof Number) {
			result = result.replace(
			    (char)(((Number)oldchar).intValue()),
			    (char)(((Number)newchar).intValue())
			);
		    }
		}
	    }
	}

	return(result);
    }


    private Object
    builtinSelect(Object context[], Object generator[]) {

	Object  value = null;
	Object  arg;
	int     index;

	if (resolved == false)
	    resolveTags(1, 1, false);

	if (generator.length > 1) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof Number) {
		index = ((Number)arg).intValue();
		if (index >= 0 && index < generator.length - 2)
		    value = getArgument(generator, index+2, context);
	    }
	}
	return(value);
    }


    private Object
    builtinStartsWith(Object context[], Object generator[]) {

	boolean  sorted;
	Object   arg;
	Object   value = "";
	Object   patterns;
	Object   key;
	Object   flags;

	if (resolved == false)
	    resolveTags(new int[] {1, 2}, false);

	if (generator.length == 3 || generator.length == 4) {
	    if ((key = getArgument(generator, 1, context)) != null) {
		if ((patterns = getArgument(generator, 2, context)) != null) {
		    if (generator.length == 4) {
			arg = getArgument(generator, 3, context);
			if (arg instanceof Number)
			    sorted = ((Number)arg).intValue() != 0;
			else sorted = false;
		    } else sorted = false;
		    flags = BUILTIN_STARTSWITH + "";
		    if (key instanceof String)
			value = matchString(BUILTIN_STARTSWITH, (String)key, patterns, flags, sorted);
		}
	    }
	}
	return(value);
    }


    private String
    builtinStrfmt(Object context[], Object generator[]) {

	Object  arg;
	Object  formatted[];
	String  format;
	String  result = null;
	int     n;
	int     m;

	if (resolved == false)
	    resolveTags(2, 1, false);

	if (generator.length > 2) {
	    if (generator[1] instanceof String) {
		format = (String)generator[1];
		if (format != null && format.length() > 0) {
		    if (args == null)
			args = new YoixObject[generator.length - 1];
		    args[0] = YoixObject.newString(format);
		    for (n = 2, m = 1; n < generator.length; n++, m++) {
			arg = getArgument(generator, n, context);
			if (arg instanceof String)
			    args[m] = YoixObject.newString((String)(arg));
			else if (arg instanceof Number)
			    args[m] = YoixObject.newNumber((Number)arg);
			else args[m] = YoixObject.newString("");
		    }
		    formatted = YoixMiscPrintf.format(args, 0);
		    result = (String)formatted[0];
		}
	    }
	}

	return(result);
    }


    private String
    builtinSubstring(Object context[], Object generator[]) {

	Object  arg;
	String  result = null;
	String  source;
	int     length;
	int     from;
	int     skip;
	int     begin;
	int     end;

	//
	// Added code that accepts a string as either offset, and in that
	// case we look for either string in the source and use the index
	// where the string was found as the corresponding argument in the
	// substring() call. If we find a match for the first string but 
	// not the second we return the matched first string, which suits
	// our current needs but we eventually may want another argument
	// to control the behavior. Added on 5/26/07 and used to extract
	// the top-level directory (the one after root if it exists) for
	// files mentioned in httpd logs.
	//

	if (resolved == false)
	    resolveTags(new int[] {1}, false);

	if (generator.length > 1) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof String) {
		source = (String)arg;
		length = source.length();
		if (generator.length > 2) {
		    arg = generator[2];
		    skip = 0;
		    if (arg instanceof String) {
			begin = source.indexOf((String)arg);
			skip = ((String)arg).length();
		    } else if (arg instanceof Number)
			begin = ((Number)arg).intValue();
		    else begin = -1;
		    if (begin >= 0) {
			if (generator.length > 3) {
			    arg = generator[3];
			    if (arg instanceof String) {
				if ((end = source.indexOf((String)arg, begin + skip)) < 0)
				    end = begin + skip;
			    } else if (arg instanceof Number)
				end = ((Number)arg).intValue();
			    else end = begin;
			} else end = length;
			try {
			    result = source.substring(begin, end);
			}
			catch(RuntimeException e) {
			    result = "";
			}
		    } else result = null;
		} else result = source;
	    }
	}

	return(result);
    }


    private String
    builtinTimeFormat(Object context[], Object generator[]) {

	String  result = null;

	if (resolved == false)
	    resolveTags(2, 1, false);

	if (generator.length > 2) {
	    if (generator[1] instanceof String) {
		result = YoixMiscTime.timerFormat(
		    (String)generator[1],
		    generateNumber(context, generator, 2)
		);
	    }
	}

	return(result);
    }


    private String
    builtinToLowerCase(Object context[], Object generator[]) {

	String  value = null;
	Object  arg;

	if (resolved == false)
	    resolveTags(1, 1, false);

	if (generator.length == 2) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof String)
		value = ((String)arg).toLowerCase();
	}

	return(value);
    }


    private String
    builtinToUpperCase(Object context[], Object generator[]) {

	String  value = null;
	Object  arg;

	if (resolved == false)
	    resolveTags(1, 1, false);

	if (generator.length == 2) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof String)
		value = ((String)arg).toUpperCase();
	}

	return(value);
    }


    private Object
    builtinTrimQueryString(Object context[], Object generator[]) {

	boolean  decode;
	boolean  ietf;
	Object   arg;
	String   value = "";
	String   path;
	int      index;

	//
	// We currently don't support an argument that changes ietf, which
	// among other things means spaces aren't represented by '+'. This
	// seems to be the version of the encoding used in httpd log files.
	//

	if (resolved == false)
	    resolveTags(new int[] {1}, false);

	if (generator.length == 2 || generator.length == 3) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof String) {
		path = (String)arg;
		decode = true;
		ietf = true;		// currently can't be changed
		if (generator.length > 2)
		    decode = getBooleanValue(generator[2], decode);
		if ((index = path.indexOf('#')) >= 0)
		    path = path.substring(0, index);
		if ((index = path.lastIndexOf('?')) >= 0)
		   path = path.substring(0, index);
		if (decode && path.length() > 0)
		    path = YoixMisc.urlToAscii(path.toCharArray(), ietf, true);
		value = path;
	    }
	}
	return(value);
    }


    private Object
    builtinURLDecode(Object context[], Object generator[]) {

	boolean  ietf;
	String   value = null;
	Object   arg;

	//
	// Changed the default ietf value from false to true in 5/26/07.
	//

	if (resolved == false)
	    resolveTags(new int[] {1}, false);

	if (generator.length == 2 || generator.length == 3) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof String) {
		ietf = true;
		if (generator.length > 2)
		    ietf = getBooleanValue(generator[2], ietf);
		value = YoixMisc.urlToAscii(((String)arg).toCharArray(), ietf, true);
	    }
	}
	return(value);
    }


    private Object
    builtinURLEncode(Object context[], Object generator[]) {

	boolean  ietf;
	String   value = null;
	Object   arg;

	//
	// Changed the default ietf value from false to true in 5/26/07.
	//

	if (resolved == false)
	    resolveTags(new int[] {1}, false);

	if (generator.length == 2 || generator.length == 3) {
	    arg = getArgument(generator, 1, context);
	    if (arg instanceof String) {
		ietf = true;
		if (generator.length > 2)
		    ietf = getBooleanValue(generator[2], ietf);
		value = YoixMisc.urlFromAscii((String)arg, ietf);
	    }
	}
	return(value);
    }


    private String
    call(YoixObject funct, String value) {

	YoixObject  obj;

	if (funct.callable(1)) {
	    if ((obj = caller.call(funct, YoixObject.newString(value))) != null) {
		if (obj.isString())
		    value = obj.stringValue();
	    }
	}
	return(value);
    }


    private void
    constructDataGenerator(YoixObject obj) {

	Object  generator[] = null;

	immediate = false;

	if (obj != null && obj.notNull()) {
	    if (obj.sizeof() > 0) {
		switch (type) {
		    case DATA_TABLE:
		    case DATA_TABLE_NEW:
		    case DATA_TABLECOLUMN:
		    case DATA_TABLECOLUMN_NEW:
			generator = constructTableGenerator(obj);
			break;

		    default:
			generator = constructStandardGenerator(obj);
			break;
		}
	    } else if (obj.isString() || obj.isNumber()) {
		immediate = true;
		generator = new Object[1];
		switch (type) {
		    case DATA_DOUBLE:
		    case DATA_INTEGER:
			if (obj.isNumber())
			    generator[0] = new Double(obj.doubleValue());
			else generator[0] = new Double(YoixMake.javaDouble(obj.stringValue()));
			break;

		    case DATA_ONE:
			generator[0] = DOUBLE_ONE;
			break;

		    case DATA_ZERO:
			generator[0] = DOUBLE_ZERO;
			break;

		    default:
			if (obj.isNumber())
			    generator[0] = obj.doubleValue() + "";
			else generator[0] = obj.stringValue();
			break;
		}
	    } else if (obj.callable(0)) {
		immediate = true;
		generator = new Object[] {obj};
	    }
	}

	if (generator == null || generator.length == 0 || generator[0] == null)
	    datagenerator = null;
	else datagenerator = generator;

	stack = null;
	args = null;

	//
	// This is defensive, but there's a small chance it may be
	// unnecessary. Don't take it out without thorough testing.
	//

	if (type == DATA_PARTITION)
	    immediate = true;
    }


    private Object[]
    constructStandardGenerator(YoixObject obj) {

	YoixObject  element;
	Object      generator[];
	int         m;
	int         n;

	generator = new Object[obj.sizeof()];

	for (n = obj.offset(), m = 0; n < obj.length(); n++, m++) {
	    if ((element = obj.getObject(n)) != null) {
		if (element.isString()) {
		    generator[m] = element.stringValue();
		    if (generator[m].equals("this"))
			generator[m] = tag;
		} else if (element.isNull())
		    generator[m] = tag;
		else if (element.isArray())
		    generator[m] = constructStandardGenerator(element);
		else if (element.isNumber() && m >= 0)
		    generator[m] = new Double(element.doubleValue());
		else if (element.isCallable() && m == 0)
		    generator[m] = element;
		else if (element.isInteger() && type == DATA_CALL)
		    generator[m] = new Double(element.doubleValue());
		else if (type == DATA_CALL && m > 0)	// recent change
		    generator[m] = element;
		else generator[m] = "";
	    }
	}

	return(generator);
    }


    private Object[]
    constructTableGenerator(YoixObject obj) {

	YoixObject  element;
	Object      generator[];
	boolean     nolimit;
	int         width;
	int         m;
	int         n;

	generator = new Object[6*((obj.sizeof() + 5)/5)];
	nolimit = (type == DATA_TABLE || type == DATA_TABLECOLUMN);

	for (n = obj.offset(), m = 0; n < obj.length(); n++, m++) {
	    switch (m%6) {
		case 0:			// datafield
		    generator[m] = obj.getString(n, "");
		    break;

		case 1:			// width plus generated limit
		    width = obj.getInt(n, 0);
		    if (width >= 0) {
			generator[m++] = new Double(width);
			generator[m] = new Double(nolimit ? 0 : width);
			maxpad = Math.max(width, maxpad);
		    } else {
			generator[m++] = new Double(-width);
			generator[m] = new Double(0);
			maxpad = Math.max(-width, maxpad);
		    }
		    break;

		case 3:			// justify
		    generator[m] = new Double(obj.getInt(n, YOIX_LEFT));
		    break;

		case 4:			// separator
		    generator[m] = obj.getString(n, "");
		    break;

		case 5:			// formatter - eventually may accept functions
		    if ((element = obj.getObject(n)) != null) {
			if (element.notNull()) {
			    if (element.isString())
				generator[m] = element;
			    else if (element.isDictionary())
				generator[m] = element;
			    else if (element.callable(1) || element.callable(3))
				generator[m] = element;
			    else generator[m] = null;
			} else generator[m] = null;
		    } else generator[m] = null;
		    break;
	    }
	}

	return(generator);
    }


    private void
    constructTranslator(YoixObject obj) {

	YoixObject  element;
	HashMap     map;
	String      name;
	int         length;
	int         n;

	if (obj != null && obj.notNull()) {
	    if (obj.isDictionary()) {
		length = obj.length();
		map = new HashMap(length);
		for (n = 0; n < length; n++) {
		    if ((element = obj.getObject(n)) != null && element.notNull()) {
			if (element.isString())
			    map.put(obj.name(n), element.stringValue());
			else if (element.callable(1))
			    map.put(obj.name(n), element);
		    }
		}
		translator = map.size() > 0 ? map : null;
	    } else if (obj.callable(1))
		translator = obj;
	    else translator = null;
	} else translator = null;
    }


    private Object
    generateArray(Object context[], Object generator[]) {

	YoixObject  obj;
	Object      array = null;
	Object      arg;

	//
	// This method may end up replacing the generator after it's used
	// once, which can be big performance improvement when a function
	// call is involved in the process. The final decision is made in
	// the replaceGeneratorWith() method.
	//

	if (generator != null && generator.length > 0) {
	    if ((arg = getArgument(generator, 0, context)) != null) {
		if (arg instanceof YoixObject) {
		    obj = (YoixObject)arg;
		    if (obj.isCallable()) {
			if (obj.callable(0)) {
			    if ((obj = caller.call(obj, new YoixObject[0])) == null)
				obj = (YoixObject)arg;
			}
		    }
		    if (obj.isString()) {
			array = splitIntoList(obj.stringValue(), generator, 1, true);
			replaceGeneratorWith(generator, array, true);
		    } else if (obj.isArray()) {
			array = obj;
			if (obj != arg)
			    replaceGeneratorWith(generator, array, true);
		    } else if (obj.isNull()) {
			if (obj != arg)
			    replaceGeneratorWith(generator, obj, true);
		    }
		} else if (arg instanceof String) {
		    array = splitIntoList((String)arg, generator, 1, true);
		    replaceGeneratorWith(generator, array, true);
		} else if (arg instanceof ArrayList)
		    array = (ArrayList)arg;
	    }
	}
	return(array);
    }


    private boolean
    generateBoolean(Object context[], Object generator[], int n) {

	boolean  result;
	Object   arg;
	Object   ostack[];
	Object   operator;
	Object   left;
	Object   right;
	int      op;
	int      top;
	int      index;

	ostack = new Object[generator.length - 1];
	top = 0;

	for (; n < generator.length; n++) {
	    if (generator[n] instanceof String) {
		if ((operator = booleanoperators.get(generator[n])) != null && top > 1) {
		    left = ostack[top-2];
		    right = ostack[top-1];
		    switch (op = ((Integer)operator).intValue()) {
			case EQ:
			case NE:
			    result = (op == NE);
			    if (left instanceof Number && right instanceof Number)
				result ^= ((Number)left).doubleValue() == ((Number)right) .doubleValue();
			    else result ^= left.equals(right);
			    top -= 1;
			    break;

			case LT:
			case GT:
			    result = (op == GT);
			    if (left instanceof Number && right instanceof Number)
				result ^= ((Number)left).doubleValue() < ((Number)right).doubleValue();
			    else if (left instanceof String && right instanceof String)
				result ^= ((String)left).compareTo((String)right) < 0;
			    else if (left instanceof Boolean && right instanceof Boolean)
				result ^= (left == Boolean.FALSE && right == Boolean.TRUE);
			    top -= 1;
			    break;

			case LE:
			case GE:
			    result = (op == GE);
			    if (left instanceof Number && right instanceof Number)
				result ^= ((Number)left).doubleValue() <= ((Number)right).doubleValue();
			    else if (left instanceof String && right instanceof String)
				result ^= ((String)left).compareTo((String)right) <= 0;
			    else if (left instanceof Boolean && right instanceof Boolean)
				result ^= (left == Boolean.FALSE || right == Boolean.TRUE);
			    top -= 1;
			    break;

			case AND:
			    if (left instanceof Boolean && right instanceof Boolean)
				result = ((Boolean)left).booleanValue() && ((Boolean)right).booleanValue();
			    else result = false;
			    top -= 1;
			    break;

			case OR:
			    if (left instanceof Boolean && right instanceof Boolean)
				result = ((Boolean)left).booleanValue() || ((Boolean)right).booleanValue();
			    else result = false;
			    top -= 1;
			    break;

			default:
			    result = false;
			    break;
		    }
		    ostack[top-1] = result ? Boolean.TRUE : Boolean.FALSE;
		} else ostack[top++] = generator[n];
	    } else {
		arg = null;
		if (generator[n] instanceof Integer) {
		    index = ((Integer)generator[n]).intValue();
		    if (index >= 0 && index < context.length) {
			arg = context[index];
			if (arg instanceof Object[])
			    arg = generateData((Object[])arg);
			if (arg instanceof DataPartition)
			    arg = ((DataPartition)arg).data;
		    }
		} else arg = generator[n];
		if (arg != null)
		    ostack[top++] = arg;
	    }
	}

	if (top > 0) {
	    if (ostack[top-1] instanceof Boolean)
		result = ((Boolean)ostack[top-1]).booleanValue();
	    else if (ostack[top-1] instanceof Number)
		result = (((Number)ostack[top-1]).doubleValue() != 0);
	    else result = false;
	} else result = false;

	return(result);
    }


    private Object
    generateCall(Object context[], Object generator[]) {

	Object  value = null;

	if (generator != null && generator.length > 0) {
	    if (generator[0] instanceof Number) {
		switch (((Number)generator[0]).intValue()) {
		    case BUILTIN_ADJUSTTIME:
			value = builtinAdjustTime(context, generator);
			break;

		    case BUILTIN_ATOH:
			value = builtinAtoh(context, generator);
			break;

		    case BUILTIN_BTOA:
			value = builtinBtoa(context, generator);
			break;

		    case BUILTIN_BTOI:
			value = builtinBtoi(context, generator);
			break;

		    case BUILTIN_CONSTANT:
			value = builtinConstant(context, generator);
			break;

		    case BUILTIN_CONTAINS:
			value = builtinContains(context, generator);
			break;

		    case BUILTIN_DATE:
			value = builtinDate(context, generator);
			break;

		    case BUILTIN_ENDSWITH:
			value = builtinEndsWith(context, generator);
			break;

		    case BUILTIN_EQUALS:
			value = builtinEquals(context, generator);
			break;

		    case BUILTIN_GETFILEEXTENSION:
			value = builtinGetFileExtension(context, generator);
			break;

		    case BUILTIN_GETINDEX:
			value = builtinGetIndex(context, generator);
			break;

		    case BUILTIN_GETKEY:
			value = builtinGetKey(context, generator);
			break;

		    case BUILTIN_GETQUERYSTRING:
			value = builtinGetQueryString(context, generator);
			break;

		    case BUILTIN_GETSEARCHQUERY:
			value = builtinGetSearchQuery(context, generator);
			break;

		    case BUILTIN_GETVALUE:
			value = builtinGetValue(context, generator);
			break;

		    case BUILTIN_HTOA:
			value = builtinHtoa(context, generator);
			break;

		    case BUILTIN_HTOI:
			value = builtinHtoi(context, generator);
			break;

		    case BUILTIN_LENGTH:
		    case BUILTIN_SIZEOF:
			value = builtinLength(context, generator);
			break;

		    case BUILTIN_MATCH:
			value = builtinMatch(context, generator);
			break;

		    case BUILTIN_MERCATORTOYDAT:
			value = builtinMercatorToYDAT(context, generator);
			break;

		    case BUILTIN_PARSEDATE:
			value = builtinParseDate(context, generator);
			break;

		    case BUILTIN_PARSETIMER:
			value = builtinParseTime(context, generator);
			break;

		    case BUILTIN_PRINTF:
			value = builtinPrintf(context, generator);
			break;

		    case BUILTIN_RANDOM:
			value = builtinRandom(context, generator);
			break;

		    case BUILTIN_REPLACE:
			value = builtinReplace(context, generator);
			break;

		    case BUILTIN_SELECT:
			value = builtinSelect(context, generator);
			break;

		    case BUILTIN_STARTSWITH:
			value = builtinStartsWith(context, generator);
			break;

		    case BUILTIN_STRFMT:
			value = builtinStrfmt(context, generator);
			break;

		    case BUILTIN_SUBSTRING:
			value = builtinSubstring(context, generator);
			break;

		    case BUILTIN_TIMERFORMAT:
			value = builtinTimeFormat(context, generator);
			break;

		    case BUILTIN_TOLOWERCASE:
			value = builtinToLowerCase(context, generator);
			break;

		    case BUILTIN_TOUPPERCASE:
			value = builtinToUpperCase(context, generator);
			break;

		    case BUILTIN_TRIMQUERYSTRING:
			value = builtinTrimQueryString(context, generator);
			break;

		    case BUILTIN_URLDECODE:
			value = builtinURLDecode(context, generator);
			break;

		    case BUILTIN_URLENCODE:
			value = builtinURLEncode(context, generator);
			break;
		}
	    } else if (generator[0] instanceof YoixObject)
		value = generateYoixCall(context, generator);
	}

	return(value);
    }


    private Object
    generateDictionary(Object context[], Object generator[]) {

	YoixObject  obj;
	ArrayList   keys;
	ArrayList   values;
	HashMap     map;
	Object      dict = null;
	Object      arg;
	int         count;
	int         n;

	//
	// This method may end up replacing the generator after it's used
	// once, which can be big performance improvement when a function
	// call is involved in the process. The final decision is made in
	// the replaceGeneratorWith() method.
	//

	if (generator != null && generator.length > 0) {
	    if ((arg = getArgument(generator, 0, context)) != null) {
		if (arg instanceof YoixObject) {
		    obj = (YoixObject)arg;
		    if (obj.isCallable()) {
			if (obj.callable(0)) {
			    if ((obj = caller.call(obj, new YoixObject[0])) == null)
				obj = (YoixObject)arg;
			}
		    }
		    if (obj.isString()) {
			if (generator.length > 1) {
			    if (generator[1] instanceof String) {
				dict = splitIntoMap(obj.stringValue(), generator, 2, (String)generator[1], true);
				replaceGeneratorWith(generator, dict, true);
			    }
			}
		    } else if (obj.isDictionary()) {
			dict = obj;
			if (obj != arg)
			    replaceGeneratorWith(generator, dict, true);
		    } else if (obj.isNull()) {
			if (obj != arg)
			    replaceGeneratorWith(generator, obj, true);
		    }
		} else if (arg instanceof String) {
		    if (generator.length > 1) {
			if (generator[1] instanceof String) {
			    dict = splitIntoMap((String)arg, generator, 2, (String)generator[1], true);
			    replaceGeneratorWith(generator, dict, true);
			}
		    }
		} else if (arg instanceof ArrayList) {
		    keys = (ArrayList)arg;
		    if ((arg = getArgument(generator, 1, context)) != null) {
			if (arg instanceof ArrayList) {
			    values = (ArrayList)arg;
			    count = Math.min(keys.size(), values.size());
			    map = new HashMap(count);
			    for (n = 0; n < count; n++)
				map.put(keys.get(n), values.get(n));
			    replaceGeneratorWith(generator, map, true);
			    dict = map;
			}
		    }
		} else if (arg instanceof HashMap)
		    dict = (HashMap)arg;
	    }
	}
	return(dict);
    }


    private double
    generateNumber(Object context[], Object generator[], int n) {

	Object  arg;
	String  operator;
	int     radix;
	int     top;
	int     index;

	//
	// We use a stack to generate numbers so expressions can be
	// grouped without the parentheses and recursion that would
	// required to handle infix notation.
	//

	if (stack == null)
	    stack = new double[generator.length];
	top = 0;

	for (; n < generator.length; n++) {
	    if (generator[n] instanceof String) {
		operator = (String)generator[n];
		if (operator.length() == 1 && top > 1) {
		    switch (operator.charAt(0)) {
			case '+':
			    stack[top-2] += stack[top-1];
			    top -= 1;
			    break;

			case '-':
			    stack[top-2] -= stack[top-1];
			    top -= 1;
			    break;

			case '*':
			    stack[top-2] *= stack[top-1];
			    top -= 1;
			    break;

			case '/':
			    stack[top-2] /= stack[top-1];
			    top -= 1;
			    break;

			case '%':
			    stack[top-2] %= stack[top-1];
			    top -= 1;
			    break;

			case '^':
			    stack[top-2] = Math.pow(stack[top-2], stack[top-1]);
			    top -= 1;
			    break;

			case '|':
			    stack[top-2] = (stack[top-2] != 0 || stack[top-1] != 0) ? 1 : 0;
			    top -= 1;
			    break;

			case '&':
			    stack[top-2] = (stack[top-2] != 0 && stack[top-1] != 0) ? 1 : 0;
			    top -= 1;
			    break;

			case '<':
			    stack[top-2] = (stack[top-2] < stack[top-1]) ? 1 : 0;
			    top -= 1;
			    break;

			case '>':
			    stack[top-2] = (stack[top-2] > stack[top-1]) ? 1 : 0;
			    top -= 1;
			    break;

			case '=':
			    stack[top-2] = (stack[top-2] == stack[top-1]) ? 1 : 0;
			    top -= 1;
			    break;

			case '?':
			    if (top > 2) {
				stack[top-3] = (stack[top-3] != 0) ? stack[top-2] : stack[top-1];
				top -= 2;
			    }
			    break;

			case '#':	// look a little like / and *
			    stack[top-2] = Math.floor(stack[top-2]/stack[top-1]) * stack[top-1];
			    top -= 1;
			    break;
		    }
		} else if (top > 0) {
		    //
		    // Eventually could use a HashMap lookup and a switch.
		    //
		    if (operator.equals("&abs"))
			stack[top-1] = Math.abs(stack[top-1]);
		    else if (operator.equals("&ceil"))
			stack[top-1] = Math.ceil(stack[top-1]);
		    else if (operator.equals("&floor"))
			stack[top-1] = Math.floor(stack[top-1]);
		    else if (operator.equals("&round"))
			stack[top-1] = Math.round(stack[top-1]);
		    else if (top > 1) {
			if (operator.equals("&max")) {
			    stack[top-2] = Math.max(stack[top-1], stack[top-2]);
			    top -= 1;
			} else if (operator.equals("&min")) {
			    stack[top-2] = Math.min(stack[top-1], stack[top-2]);
			    top -= 1;
			}
		    }
		}
	    } else {
		arg = null;
		if (generator[n] instanceof Integer) {
		    index = ((Integer)generator[n]).intValue();
		    if (index >= 0 && index < context.length) {
			arg = context[index];
			if (arg instanceof Object[])
			    arg = generateData((Object[])arg);
			if (arg instanceof DataPartition)
			    arg = ((DataPartition)arg).data;
		    }
		} else if (generator[n] instanceof Double)
		    arg = generator[n];
		if (arg != null) {
		    if (arg instanceof String)
		        stack[top++] = YoixMake.javaDouble((String)arg, Double.NaN);
		    else stack[top++] = YoixMake.javaDouble(arg);
		}
	    }
	}

	return(top > 0 ? stack[top-1] : 0);
    }


    private Object
    generatePartition(Object context[], Object generator[]) {

	boolean  selected;
	Object   value = null;
	Object   left;
	Object   right;
	int      index;

	//
	// A set of changes (4/24/05) here and in DataPartition.java means
	// we now hand this DataGenerator to the DataPartition constructor
	// when there's a translator. Means translators will now work with
	// partitions and also means the translator won't be applied until
	// the we really need the value. Changes seem OK but probably need
	// some more testing.
	//
	// A change on 7/9/04 returns the DataPartition whenever we create
	// one. Old version never changed value, so the return was always
	// null.
	// 

	if (generator != null && generator.length > 1) {
	    if (generator[0] instanceof Integer) {
		index = ((Integer)generator[0]).intValue();
		if (index >= 0 && index < context.length) {
		    if (context[index] instanceof Object[])
			generateData((Object[])context[index]);
		    if ((context[index] instanceof DataPartition) == false) {
			if (generateBoolean(context, generator, 1)) {
			    value = new DataPartition(
				context[index],
				id,
				translator != null ? this : null
			    );
			    context[index] = value;
			}
		    }
		}
	    }
	}

	return(value);
    }


    private String
    generateString(Object context[], Object generator[]) {

	Object  arg;
	String  result = null;
	int     index;
	int     n;

	if (immediate == false) {
	    if (generator != null && generator.length > 0) {
		result = "";
		for (n = 0; n < generator.length; n++) {
		    if ((arg = getArgument(generator, n, context)) != null) {
			if (arg instanceof String)
			    result += (String)arg;
			else if (arg instanceof Number)
			    result += arg.toString();
		    }
		}
	    }
	} else result = (String)generator[0];

	return(result);
    }


    private String
    generateTable(Object context[], Object generator[]) {

	YoixObject  argv[];
	YoixObject  formatter;
	String      result = "";
	String      sep;
	String      str;
	Object      formatted[];
	Object      arg;
	int         justify;
	int         width;
	int         limit;
	int         length;
	int         index;
	int         n;


	if (immediate == false) {
	    length = generator.length;
	    result = (length > 0) ? "" : null;
	    for (n = 0; n < length - 6; n += 6) {
		arg = null;
		if (generator[n] instanceof Integer) {
		    index = ((Integer)generator[n]).intValue();
		    if (index >= 0 && index < context.length) {
			arg = context[index];
			if (arg instanceof Object[])
			    arg = generateData((Object[])arg);
			if (arg instanceof DataPartition)
			    arg = ((DataPartition)arg).data;
		    }
		}
		if (arg instanceof String || arg instanceof Number) {
		    if (generator[n+1] instanceof Number)
			width = ((Number)generator[n+1]).intValue();
		    else width = 0;
		    if (generator[n+2] instanceof Number)
			limit = ((Number)generator[n+2]).intValue();
		    else limit = 0;
		    if (generator[n+3] instanceof Number)
			justify = ((Number)generator[n+3]).intValue();
		    else justify = YOIX_LEFT;
		    if (generator[n+4] instanceof String)
			sep = (String)generator[n+4];
		    else sep = "";
		    if (generator[n+5] instanceof YoixObject) {
			formatter = (YoixObject)generator[n+5];
			if (formatter.isString()) {
			    argv = new YoixObject[2];
			    argv[0] = formatter;
			    if (arg instanceof String)
				argv[1] = YoixObject.newString((String)arg);
			    else argv[1] = YoixObject.newNumber((Number)arg);
			    formatted = YoixMiscPrintf.format(argv, 0);
			    arg = (String)formatted[0];
			} else if (formatter.isCallable()) {
			    if (formatter.callable(3)) {
				argv = new YoixObject[3];
				argv[1] = YoixObject.newInt(width);
				argv[2] = YoixObject.newInt(limit);
			    } else argv = argv = new YoixObject[1];
			    if (arg instanceof String)
				argv[0] = YoixObject.newString((String)arg);
			    else argv[0] = YoixObject.newNumber((Number)arg);
			    if ((arg = caller.call(formatter, argv)) != null) {
				if (((YoixObject)arg).isString())
				    arg = ((YoixObject)arg).stringValue();
				else arg = null;
			    }
			} else if (formatter.isDictionary())
			    arg = lookup(arg + "", formatter);
			else arg = null;
		    }
		    if (arg != null) {
			str = arg + "";
			if (str.length() > 0 || width > 0)
			    result = result + sep + padString(str, width, limit, justify);
		    }
		}
	    }
	} else result = (String)generator[0];

	return(result);
    }


    private Object
    generateYoixCall(Object context[], Object generator[]) {

	YoixObject  function;
	YoixObject  result;
	Object      value;
	Object      arg;
	int         index;
	int         m;
	int         n;

	function = (YoixObject)generator[0];
	if (args == null)
	    args = new YoixObject[generator.length - 1];

	for (n = 1, m = 0; n < generator.length; n++, m++) {
	    arg = getArgument(generator, n, context);
	    if (arg instanceof String)
		args[m] = YoixObject.newString((String)(arg));
	    else if (arg instanceof Number)
		args[m] = YoixObject.newNumber((Number)arg);
	    else if (arg instanceof YoixObject)
		args[m] = (YoixObject)arg;
	    else args[m] = YoixObject.newString("");
	}

	if ((result = caller.call(function, args)) != null) {
	    if (result.isString())
		value = result.stringValue();
	    else if (result.isInteger())
		value = new Integer(result.intValue());
	    else if (result.isNumber())
		value = new Double(result.doubleValue());
	    else if (result.isTimeZone())
		value = result.timeZoneValue();
	    else if (result.isNull())
		value = "";
	    else value = null;
	} else value = null;

	return(value);
    }


    private Object
    getArgument(Object context[]) {

	Object  arg;

	if (fieldindex >= 0 && fieldindex < context.length) {
	    arg = context[fieldindex];
	    if (arg instanceof Object[])
		arg = generateData((Object[])arg);
	    if (arg instanceof DataPartition)
		arg = ((DataPartition)arg).data;
	} else arg = null;

	return(arg);
    }


    private Object
    getArgument(Object generator[], int n, Object context[]) {

	Object  arg;
	int     index;

	if (generator[n] instanceof Integer) {
	    index = ((Integer)generator[n]).intValue();
	    if (index >= 0 && index < context.length) {
		arg = context[index];
		if (arg instanceof Object[])
		    arg = generateData((Object[])arg);
		if (arg instanceof DataPartition)
		    arg = ((DataPartition)arg).data;
	    } else arg = null;
	} else if (generator[n] instanceof Object[])
	    arg = generateCall(context, (Object[])generator[n]);
	else arg = generator[n];

	return(arg);
    }


    private boolean
    getBooleanValue(Object arg, boolean value) {

	//
	// Currently only used to parse generator[] arguments that are
	// supposed to represent booleans.
	//

	if (arg instanceof String)
	    value = Boolean.valueOf((String)arg).booleanValue();
	else if (arg instanceof Number)
	    value = (((Number)arg).intValue() != 0);
	return(value);
    }


    private Object
    getMatchedValue(String key, Object patterns, Object flags) {

	Object  value = null;

	if (key != null && patterns != null && flags != null) {
	    synchronized(matchlock) {
		if (matchvalues != null) {
		    if (flags.equals(matchflags) && patterns.equals(matchpatterns))
			value = matchvalues.get(key);
		}
	    }
	}
	return(value);
    }


    private int
    getNextUID() {

	int  uid;

	synchronized(nextid) {
	    uid = nextid[0]++;
	}
	return(uid);
    }


    private String
    lookup(String key, YoixObject dict) {

	Object  value;

	if (key != null && dict != null) {
	    if (key.length() > 0) {
		if ((value = dict.getString(key)) == null) {
		    if ((value = dict.getString("")) == null)
			value = key;
		}
	    } else if ((value = dict.getString("\0")) == null)		// 2/25/03
		value = dict.getString("");
	    if (value instanceof YoixObject)
		value = call((YoixObject)value, key);
	} else value = key;

	return((String)value);
    }


    private String
    lookup(String key, HashMap dict) {

	Object  value;

	if (key != null && dict != null) {
	    if (key.length() > 0) {
		if ((value = dict.get(key)) == null) {
		    if ((value = dict.get("")) == null)
			value = key;
		}
	    } else if ((value = dict.get("\0")) == null)
		value = dict.get("");
	    if (value instanceof YoixObject)
		value = call((YoixObject)value, key);
	} else value = key;

	return((String)value);
    }


    private boolean
    match(int builtin, String text, String pattern, Object flags) {

	return(match(builtin, text, pattern, pattern, flags));
    }


    private boolean
    match(int builtin, String text, String pattern, Object patterns, Object flags) {

	YoixRERegexp  re = null;
	boolean       result = false;

	if (text != null && pattern != null) {
	    switch (builtin) {
		case BUILTIN_ENDSWITH:
		    result = text.endsWith(pattern);
		    break;

		case BUILTIN_EQUALS:
		    result = text.equals(pattern);
		    break;

		case BUILTIN_MATCH:
		    if (patterns != null && flags instanceof Number) {
			synchronized(matchlock) {
			    if (matchexpressions == null)
				matchexpressions = new HashMap();
			    if (flags.equals(matchflags) == false || (re = (YoixRERegexp)matchexpressions.get(pattern)) == null) {
				if (flags.equals(matchflags) == false || patterns.equals(matchpatterns) == false) {
				    matchexpressions.clear();
				    matchflags = flags;
				    matchpatterns = patterns;
				}
				re = new YoixRERegexp(pattern, ((Number)flags).intValue());
				matchexpressions.put(pattern, re);
			    }
			}
		    }
		    result = (re != null) ? re.exec(text, null) : false;
		    break;

		case BUILTIN_STARTSWITH:
		    result = text.startsWith(pattern);
		    break;
	    }
	}
	return(result);
    }


    private Object
    matchString(int builtin, String text, Object patterns, Object flags, boolean sorted) {

	YoixObject  obj;
	YoixObject  matches;
	Collection  keyset;
	Iterator    iterator;  
	Object      arg;
	Object      value;
	Object      element;
	int         length;
	int         index;
	int         n;

	//
	// Handles the low level text string matching for the builtins that
	// support it.
	//

	if ((value = getMatchedValue(text, patterns, flags)) == null) {
	    value = "";
	    if (patterns instanceof YoixObject) {
		matches = (YoixObject)patterns;
		if (matches.isDictionary()) {
		    if ((value = matches.getString("")) == null)
			value = INTEGER_ZERO;
		    length = matches.length();
		    for (n = matches.offset(); n < length; n++) {
			if (match(builtin, text, matches.name(n), matches, flags)) {
			    if ((obj = matches.getObject(n)) != null) {
				if (obj.notNull()) {
				    if (obj.isString())
					value = new String(obj.stringValue());
				    else if (obj.isNumber())
					value = new Double(obj.doubleValue());
				    else value = obj;
				} else value = INTEGER_ONE;
			    } else value = INTEGER_ONE;
			    break;
			}
		    }
		} else if (matches.isString()) {
		    if (match(builtin, text, matches.stringValue(), flags))
			value = INTEGER_ONE;
		    else value = INTEGER_ZERO;
		}
	    } else if (patterns instanceof HashMap) {
		if ((keyset = ((HashMap)patterns).keySet()) != null) {
		    if ((value = ((HashMap)patterns).get("")) == null)
			value = INTEGER_ZERO;
		    for (iterator = keyset.iterator(); iterator.hasNext(); ) {
			element = iterator.next();
			if (element instanceof String) {
			    if (match(builtin, text, (String)element, patterns, flags)) {
				if ((value = ((HashMap)patterns).get(element)) == null)
				    value = INTEGER_ONE;
				break;
			    }
			}
		    }
		}
	    } else if (patterns instanceof ArrayList) {
		value = INTEGER_ZERO;
		if (sorted) {
		    if ((index = Collections.binarySearch(((ArrayList)patterns), text)) < 0) {
			if ((index = -(index + 2)) >= 0) {
			    element = ((ArrayList)patterns).get(index);
			    if (element instanceof String) {
				if (match(builtin, text, (String)element, patterns, flags))
				    value = INTEGER_ONE;
			    }
			}
		    } else value = INTEGER_ONE;
		} else {
		    for (iterator = ((ArrayList)patterns).iterator(); iterator.hasNext(); ) {
			element = iterator.next();
			if (element instanceof String) {
			    if (match(builtin, text, (String)element, patterns, flags)) {
				value = INTEGER_ONE;
				break;
			    }
			}
		    }
		}
	    } else if (patterns instanceof String) {
		if (match(builtin, text, (String)patterns, patterns, flags))
		    value = INTEGER_ONE;
		else value = INTEGER_ZERO;
	    }
	    saveMatchedValue(text, value, patterns, flags);
	}
	return(value);
    }


    private String
    padString(String str, int width, int limit, int justify) {

	int  length;

	if ((length = str.length()) > limit && limit > 0) {
	    str = str.substring(0, limit);
	    length = limit;
	}
	if (length < width) {
	    if (justify == YOIX_RIGHT)
		str = new String(padding, 0, width - length) + str;
	    else str += new String(padding, 0, width - length);
	}

	return(str);
    }


    private void
    replaceGeneratorWith(Object generator[], Object arg, boolean any) {

	boolean  replaceable = true;
	int      n;

	if (generator != null && arg != null) {
	    if (generator == datagenerator) {
		for (n = 0; n < generator.length && replaceable; n++) {
		    if (generator[n] instanceof YoixObject) {
			if (((YoixObject)generator[n]).isCallable())
			    replaceable = any;
		    } else if (generator[n] instanceof Integer)
			replaceable = false;
		    else if (generator[n] instanceof Object[])
			replaceable = false;
		}
		if (replaceable) {
		    if (arg instanceof Object[])
			datagenerator = (Object[])arg;
		    else datagenerator = new Object[] {arg};
		}
	    }
	}
    }


    private void
    resolveGeneratorTags(Object generator[], int n, int incr, boolean force) {

	int  index;
	int  length;

	length = generator.length;
	for (; n < length; n += incr) {
	    if (generator[n] instanceof String) {
		index = datamanager.getTagIndex((String)generator[n]);
		if (force || index >= 0)
		    generator[n] = new Integer(index);
	    } else if (generator[n] instanceof Object[])
		resolveGeneratorTags((Object[])generator[n], 0, 1, force);
	}
    }


    private void
    resolveTags(int index, boolean force) {

	if (resolved == false) {
	    if (datagenerator != null)
		resolveGeneratorTags(datagenerator, index, datagenerator.length, force);
	    resolved = true;
	}
    }


    private void
    resolveTags(int indices[], boolean force) {

	int  incr;
	int  n;

	if (resolved == false) {
	    if (datagenerator != null) {
		incr = datagenerator.length;
		for (n = 0; n < indices.length; n++)
		    resolveGeneratorTags(datagenerator, indices[n], incr, force);
	    }
	    resolved = true;
	}
    }


    private void
    resolveTags(int n, int incr, boolean force) {

	if (resolved == false) {
	    if (datagenerator != null) {
		if (incr <= 0)
		    incr = datagenerator.length;
		resolveGeneratorTags(datagenerator, n, incr, force);
	    }
	    resolved = true;
	}
    }


    private void
    saveMatchedValue(String key, Object value, Object patterns, Object flags) {

	if (key != null && patterns != null) {
	    synchronized(matchlock) {
		if (matchvalues == null)
		    matchvalues = new HashMap();
		if (flags.equals(matchflags) == false || patterns.equals(matchpatterns) == false) {
		    matchvalues.clear();
		    matchflags = flags;
		    matchpatterns = patterns;
		}
		matchvalues.put(key, value);
	    }
	}
    }


    private ArrayList
    splitIntoList(String source, Object generator[], int n, boolean translate) {

	ArrayList  list = null;
	String     sep;
	String     left;
	String     right;

	if (source != null) {
	    if (generator.length > n) {
		if (generator[n] instanceof String)
		    sep = (String)generator[n];
		else sep = null;
		if (generator.length > ++n) {
		    if (generator[n] instanceof String)
			left = (String)generator[n];
		    else left = "";
		    if (generator.length > ++n) {
			if (generator[n] instanceof String)
			    right = (String)generator[n];
			else right = left;
		    } else right = left;
		    source = YoixMisc.trim(source, left, right);
		}
		if (source.length() > 0) {
		    if (sep == null) {
			list = new ArrayList(1);
			list.add(source);
		    } else list = YoixMisc.split(source, sep);
		    if (list.size() > 0) {
			if (translate && translator != null) {
			    for (n = 0; n < list.size(); n++)
				list.set(n, translate((String)list.get(n)));
			}
		    } else list = null;
		}
	    }
	}
	return(list);
    }


    private HashMap
    splitIntoMap(String source, Object generator[], int n, String sep, boolean translate) {

	ArrayList  list;
	Iterator   iterator;
	HashMap    map = null;
	Object     element;
	Object     key;
	Object     value;
	String     text;
	int        skip;
	int        index;

	if ((list = splitIntoList(source, generator, n, false)) != null) {
	    if (sep != null && (skip = sep.length()) > 0) {
		map = new HashMap();
		for (iterator = list.iterator(); iterator.hasNext(); ) {
		    element = iterator.next();
		    if (element instanceof String) {
			text = (String)element;
			if ((index = text.indexOf(sep)) >= 0)
			    map.put(text.substring(0, index), text.substring(index + skip));
			else map.put(text, null);
		    }
		}
		if (map.size() > 0) {
		    if (translate && translator != null) {
			for (iterator = map.keySet().iterator(); iterator.hasNext(); ) {
			    key = iterator.next();
			    value = map.get(key);
			    if (value instanceof String)
				map.put(key, translate((String)value));
			}
		    }
		} else map = null;
	    }
	}
	return(map);
    }


    private String
    translate(String buffer[], int entries) {

	String  value;

	if (fieldindex >= 0 && fieldindex < entries) {
	    if ((value = buffer[fieldindex]) != null) {
		if (translator != null)
		    value = translate(value);
	    }
	} else value = null;

	return(value);
    }


    private String
    validate(YoixObject obj) {

	YoixObject  value;
	boolean     valid;
	String      name;
	int         n;

	//
	// Storing the type, rather than a number that identifies the
	// field, would simplifiy coding, but probabaly can't handle
	// extra checking or fields that aren't restricted to a single
	// type.
	//

	name = null;
	valid = true;

	for (n = 0; n < validfields.length && valid; n += 2) {
	    name = (String)validfields[n];
	    if ((value = obj.getObject(name)) != null) {
		switch (((Integer)validfields[n+1]).intValue()) {
		    case VL_ACCUMULATE:
		    case VL_ENABLED:
		    case VL_INDEX:
		    case VL_TYPE:
		    case VL_WIDTH:
			valid = value.isInteger();
			break;

		    case VL_GENERATOR:
			if ((valid = value.isArray()) == false) {
			    if (value.isNumber() || value.isString())
				valid = true;
			    else valid = value.callable(0);
			}
			break;

		    case VL_TAG:
			valid = value.isString();
			break;

		    case VL_TRANSLATOR:
			valid = value.isDictionary() || value.callable(1) || value.isNull();
			break;
		}
	    }
	}

	return(valid ? null : name);
    }
}

