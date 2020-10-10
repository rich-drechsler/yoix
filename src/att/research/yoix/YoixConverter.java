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

package att.research.yoix;

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.*;
import java.util.*;

abstract
class YoixConverter

    implements YoixConstants

{

    //
    // Protects Yoix from the fact that the ByteToCharConverter isn't
    // part of java.io (usually in sun.io) and so is not guaranteed to
    // exist or be consistent between versions.
    //

    static Hashtable  converters = new Hashtable();
    static Class      byte_array = (new byte[1]).getClass();
    static Class      char_array = (new char[1]).getClass();
    static byte       BE_BOM[] = {(byte)0xFE, (byte)0xFF};	// a big endian mark
    static Class      cbfe_handle = null;
    static Class      mie_handle = null;
    static Class      uce_handle = null;

    Object  btc = null;
    Method  btc_convert = null;
    Method  btc_flush = null;
    Method  btc_getCharacterEncoding = null;
    Method  btc_getMaxCharsPerByte = null;
    Method  btc_nextByteIndex = null;
    Method  btc_nextCharIndex = null;

    Object  ctb = null;
    Method  ctb_convert = null;
    Method  ctb_flush = null;
    Method  ctb_getCharacterEncoding = null;
    Method  ctb_getMaxBytesPerChar = null;
    Method  ctb_nextByteIndex = null;
    Method  ctb_nextCharIndex = null;

    byte  byteBuf[] = null;
    int   byteStart = 0;
    int   byteEnd = 0;
    int   nextByteIdx = 0;

    char  charBuf[] = null;
    int   charStart = 0;
    int   charEnd = 0;
    int   nextCharIdx = 0;

    int  maxBytesPerChar = -1;
    int  maxCharsPerByte = -1;

    Object  six_args[] = null;
    Object  three_args[] = null;

    private boolean  have_converters = false;
    private boolean  force_kludge = false;

    String   encoding = null;

    //
    // This should let us be more forgiving with encoding names users may
    // set on the command line or in property files. Lookups are done by
    // mapping to uppercase and converting all '_' characters to '-', but
    // only after Charset decides it doesn't like the original name. It
    // probably would be easy to do most of this without using  HashMap.
    // We're definitely not convinced either way.
    //

    private static HashMap  encodingmap = new HashMap();

    static {
	encodingmap.put("ISO-8859-1", "ISO-8859-1");	// for underscore mapping
	encodingmap.put("ISO8859-1", "ISO-8859-1");
	encodingmap.put("8859-1", "ISO-8859-1");
	encodingmap.put("ISO-LATIN-1", "ISO-8859-1");
	encodingmap.put("ISOLATIN-1", "ISO-8859-1");
	encodingmap.put("LATIN-1", "ISO-8859-1");
	encodingmap.put("LATIN1", "ISO-8859-1");

	encodingmap.put("UTF-8", "UTF-8");		// for underscore mapping
	encodingmap.put("UTF8", "UTF-8");

	encodingmap.put("UTF-16", "UTF-16");		// for underscore mapping
	encodingmap.put("UTF16", "UTF-16");

	encodingmap.put("UTF-16BE", "UTF-16BE");	// for underscore mapping
	encodingmap.put("UTF16BE", "UTF-16BE");

	encodingmap.put("UTF-16LE", "UTF-16LE");	// for underscore mapping
	encodingmap.put("UTF16LE", "UTF-16LE");

	encodingmap.put("US-ASCII", "US-ASCII");	// for underscore mapping
	encodingmap.put("USASCII", "US-ASCII");
	encodingmap.put("ASCII", "US-ASCII");
    }

    private static String  iso8859_1_encoding = null;
    private static String  utf_8_encoding = null;
    private static String  utf_16_encoding = null;
    private static String  utf_16be_encoding = null;
    private static String  utf_16le_encoding = null;
    private static String  usascii_encoding = null;
    private static String  fileEncodingPkg = "sun.io";		// may be reset

    static {
	//
	// Determine appropriate id strings for the iso8859_1, utf_8 and the
	// other standard Java encodings (see Charset javadoc). Since 1.4 this
	// job is a lot easier.
	//
	// These are the official (i.e., via javadoc) supported charsets, we
	// should make sure we can get this set...  DO NOT SHIFT ORDER WITHOUT
	// ADJUSTING CODE THAT FOLLOWS!!!
	//

	String  standards[] = {"ISO-8859-1", "UTF-8", "UTF-16", "UTF-16BE", "UTF-16LE", "US-ASCII"};
	int     n;

	for (n = 0; n < standards.length; n++) {
	    if (supportedCharset(standards[n])) {
		switch(n) {
		    case 0:
			iso8859_1_encoding = standards[n];
			break;

		    case 1:
			utf_8_encoding = standards[n];
			break;

		    case 2:
			utf_16_encoding = standards[n];
			break;

		    case 3:
			utf_16be_encoding = standards[n];
			break;

		    case 4:
			utf_16le_encoding = standards[n];
			break;

		    case 5:
			usascii_encoding = standards[n];
			break;
		}
	    }
	}

	//
	// We insist on these because we explicitly use them.
	//
	if (iso8859_1_encoding == null || utf_8_encoding == null)
	    VM.die(INTERNALERROR);

	//
	// Jan 6, 2006: it is about time we get rid of this junk and use Charset,
	// etc. stuff to handle encoding/decoding in a non-reflective manner. Don't
	// have the time this go-round, but will do in the next few months!
	//
	try {
	    fileEncodingPkg = System.getProperty("file.encoding.pkg", null);
	}
	catch(Exception e) {}
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixConverter(String enc) {

	setEncoding(enc);
	six_args = new Object[6];
	three_args = new Object[3];
    }

    ///////////////////////////////////
    //
    // YoixConverter Methods
    //
    ///////////////////////////////////

    final String
    getCharacterEncoding() {

	return(encoding);
    }


    static final String
    getDefaultEncoding() {

	return(VM.getDefaultEncoding().stringValue());
    }


    static final String
    getISO88591Encoding() {

	return(iso8859_1_encoding);
    }


    static final String
    getJVMEncoding() {

	String  dflt = (new InputStreamReader(System.in)).getEncoding();

	return(getSupportedEncoding(dflt, dflt));
    }


    static final String
    getUTF8Encoding() {

	return(utf_8_encoding);
    }


    static final String
    getSupportedEncoding(Object key, String value) {

	String  name = null;

	try {
	    if (key instanceof YoixObject) {
		if (((YoixObject)key).isString())
		    name = ((YoixObject)key).stringValue();
	    } else if (key instanceof String)
		name = (String)key;
	    if (name != null) {
		name = name.trim();
		if (!Charset.isSupported(name)) {
		    name = name.toUpperCase().replace('_', '-');
		    if ((name = (String)encodingmap.get(name)) != null) {
			if (Charset.isSupported(name))
			    value = name;
		    }
		} else value = (Charset.forName(name)).name();	// get canonical name
	    }
	}
	catch(Exception e) {}
	return(value);
    }


    final boolean
    haveConverters() {

	return(have_converters);
    }


    static final boolean
    setDefaultEncoding(String name) {

	boolean  settable = true;

	if (name == null)
	    YoixModule.tune(N_ENCODING, YoixObject.newString());
	else if ((name = name.trim()).length() == 0 || name.equalsIgnoreCase("null"))
	    YoixModule.tune(N_ENCODING, YoixObject.newString());
	else if (supportedCharset(name))
	    YoixModule.tune(N_ENCODING, YoixObject.newString(name));
	else settable = false;

	return(settable);
    }


    static final boolean
    supportedCharset(String name) {

	boolean  supported = false;

	try {
	    supported = Charset.isSupported(name);
	}
	catch(Exception e) {}
	return(supported);
    }


    final boolean
    useKludge() {

	return(force_kludge);
    }


    static int
    utf8Length(String str) {

	char  chrs[];
	int   l;
	int   len = 0;
	int   ch;
	int   i;

	chrs = str.toCharArray();
	l = chrs.length;

	for (i = 0; i < l; i++) {
	    if ((ch = chrs[i]) <= 0x7F)
		len++;
	    else if (ch <= 0x7FF)
		len += 2;
	    else len += 3;
	}

	return(len);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private synchronized void
    setEncoding(String encode) {

	// we cannot know these, so go with safe values
	// if we have a converter, they will be set accurately
	maxCharsPerByte = 1; // really cannot be otherwise
	maxBytesPerChar = 8; // the max observed (ISO2022JP)

	if (encode == null || encode.equals(""))
	    encode = YoixMisc.getDefaultEncoding();

	try {
	    // get canonical name and validate, too
	    encoding = (Charset.forName(encode)).name();
	}
	catch(IllegalCharsetNameException e) {
	    VM.abort(BADENCODING, encode);
	}
	catch(UnsupportedCharsetException e) {
	    VM.abort(BADENCODING, encode);
	}

	// TODO: use Charset stuff (1/6/2006)

	have_converters = false;
	ConverterSet cset = null;
	if ((cset = (ConverterSet)converters.get(encoding)) == null) {
	    cset = new ConverterSet();

	    // Need all this reflection indirection because we cannot
	    // assume sun.io is the correct package.
	    // If any of this stuff fails it means we cannot trust
	    // this approach, so we'll use an indirect approach
	    // and less efficient approach (possibly twice as slow)

	    try {
		if (cbfe_handle == null)
		    cbfe_handle = Class.forName(fileEncodingPkg + ".ConversionBufferFullException");
		// not really sure we need these other two
		// exceptions - knowing that we have an
		// instanceof java.io.CharConversionException
		// may be enough
		if (mie_handle == null)
		    mie_handle = Class.forName(fileEncodingPkg + ".MalformedInputException");
		if (uce_handle == null)
		    uce_handle = Class.forName(fileEncodingPkg + ".UnknownCharacterException");

		Class btc_handle = Class.forName(fileEncodingPkg + ".ByteToCharConverter");
		Method btc_getConverter = btc_handle.getMethod("getConverter", new Class[] { java.lang.String.class });
		cset.btc = btc_getConverter.invoke(null, new Object[] { encoding });
		Class btc_class = cset.btc.getClass();

		cset.btc_convert = btc_class.getMethod("convert", new Class[] { byte_array, Integer.TYPE, Integer.TYPE, char_array, Integer.TYPE, Integer.TYPE });
		cset.btc_flush = btc_class.getMethod("flush", new Class[] { char_array, Integer.TYPE, Integer.TYPE });
		// don't really need this one since we already
		// have the canonical name
		cset.btc_getCharacterEncoding = btc_class.getMethod("getCharacterEncoding", null);
		cset.btc_getMaxCharsPerByte = btc_class.getMethod("getMaxCharsPerByte", null);
		cset.btc_nextByteIndex = btc_class.getMethod("nextByteIndex", null);
		cset.btc_nextCharIndex = btc_class.getMethod("nextCharIndex", null);

		Class ctb_handle = Class.forName(fileEncodingPkg + ".CharToByteConverter");
		Method ctb_getConverter = ctb_handle.getMethod("getConverter", new Class[] { java.lang.String.class });
		cset.ctb = ctb_getConverter.invoke(null, new Object[] { encoding });
		Class ctb_class = cset.ctb.getClass();

		cset.ctb_convert = ctb_class.getMethod("convert", new Class[] { char_array, Integer.TYPE, Integer.TYPE, byte_array, Integer.TYPE, Integer.TYPE });
		cset.ctb_flush = ctb_class.getMethod("flush", new Class[] { byte_array, Integer.TYPE, Integer.TYPE });
		// don't really need this one since we already
		// have the canonical name
		cset.ctb_getCharacterEncoding = ctb_class.getMethod("getCharacterEncoding", null);
		cset.ctb_getMaxBytesPerChar = ctb_class.getMethod("getMaxBytesPerChar", null);
		cset.ctb_nextByteIndex = ctb_class.getMethod("nextByteIndex", null);
		cset.ctb_nextCharIndex = ctb_class.getMethod("nextCharIndex", null);

		Integer retObj = null;

		cset.maxCharsPerByte = maxCharsPerByte; // initialize
		try {
		    retObj = (Integer)(cset.btc_getMaxCharsPerByte.invoke(cset.btc, null));
		    cset.maxCharsPerByte = retObj.intValue();
		}
		catch(IllegalArgumentException e) {
		    VM.caughtException(e, false);
		}
		catch(IllegalAccessException e) {
		    VM.caughtException(e, false);
		}
		catch(InvocationTargetException e) {
		    VM.caughtException(e, false);	// was e.getTargetException()
		}

		cset.maxBytesPerChar = maxBytesPerChar; // initialize
		try {
		    retObj = (Integer)(cset.ctb_getMaxBytesPerChar.invoke(cset.ctb, null));
		    cset.maxBytesPerChar = retObj.intValue();
		}
		catch(IllegalArgumentException e) {
		    VM.caughtException(e, false);
		}
		catch(IllegalAccessException e) {
		    VM.caughtException(e, false);
		}
		catch(InvocationTargetException e) {
		    VM.caughtException(e, false);	// was e.getTargetException()
		}

		converters.put(encoding, cset);
		have_converters = true;
	    }
	    catch(Exception e) {
		VM.caughtException(e, false);
	    }
	} else have_converters = true;

	if (have_converters) {
	    btc = cset.btc;
	    btc_convert = cset.btc_convert;
	    btc_flush = cset.btc_flush;
	    btc_getCharacterEncoding = cset.btc_getCharacterEncoding;
	    btc_getMaxCharsPerByte = cset.btc_getMaxCharsPerByte;
	    btc_nextByteIndex = cset.btc_nextByteIndex;
	    btc_nextCharIndex = cset.btc_nextCharIndex;

	    ctb = cset.ctb;
	    ctb_convert = cset.ctb_convert;
	    ctb_flush = cset.ctb_flush;
	    ctb_getCharacterEncoding = cset.ctb_getCharacterEncoding;
	    ctb_getMaxBytesPerChar = cset.ctb_getMaxBytesPerChar;
	    ctb_nextByteIndex = cset.ctb_nextByteIndex;
	    ctb_nextCharIndex = cset.ctb_nextCharIndex;

	    maxCharsPerByte = cset.maxCharsPerByte;
	    maxBytesPerChar = cset.maxBytesPerChar;
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    private
    class ConverterSet {

	Object  btc = null;
	Method  btc_convert = null;
	Method  btc_flush = null;
	Method  btc_getCharacterEncoding = null;
	Method  btc_getMaxCharsPerByte = null;
	Method  btc_nextByteIndex = null;
	Method  btc_nextCharIndex = null;

	Object  ctb = null;
	Method  ctb_convert = null;
	Method  ctb_flush = null;
	Method  ctb_getCharacterEncoding = null;
	Method  ctb_getMaxBytesPerChar = null;
	Method  ctb_nextByteIndex = null;
	Method  ctb_nextCharIndex = null;

	int  maxCharsPerByte = 1;
	int  maxBytesPerChar = 8;
    }
}

