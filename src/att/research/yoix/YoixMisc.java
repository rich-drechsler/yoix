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
import java.awt.*;
import java.io.*;
import java.net.*;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.*;

public abstract
class YoixMisc

    implements YoixAPI,
	       YoixConstants

{

    //
    // Be very careful in static initializations that use YoixConstants
    // constants because several methods (e.g., getTempDirectory() and
    // tokenImage()) defined here are also used in that file. A confusing
    // and error prone implementation that we undoubtedly should address
    // in a future release!!
    //
    // NOTE - started a cleanup of the various copyIntoXXX() methods on
    // 2/8/05 but didn't finish because we needed to make a snapshot. We
    // should take a careful look at it all before the next release. The
    // main goal (I think) is to use List and Map in as many places as
    // possible and also eliminate unnecessary versions when possible.
    // Need to take a careful look at synchronization that's currently
    // used in many of the versions that deal with ArrayList and HashMap
    // because Hashtable and Vector may not need it??? Think about it
    // carefully and test all changes!!! Also need to go through bodies
    // of the various copyIntoXXX() methods and adjust, add, or delete
    // some of the instanceof testing.
    //
    // NOTE - finally finished the cleanup mentioned above, but probably
    // still need to take a careful look at synchronization. There could
    // theoretically be some deadlock issues (e.g., we're copying a List
    // Map or Object[] and we find another one of these objects), but we
    // seriously doubt it's ever an issue. Passing the existing lock, if
    // there is one, as the lock argument in the method that's called to
    // handle the object, would eliminate deadlock as a possibilty, even
    // though it doesn't do anything to protect the copy. Changes were
    // made on 6/23/07, but there's obviously more to do.
    //

    //
    // Several tables that help in the conversions between hex and ascii
    // (binary) strings.
    //

    static final byte  HEXNIBBLES[] = {
	(byte)'0', (byte)'1', (byte)'2', (byte)'3',
	(byte)'4', (byte)'5', (byte)'6', (byte)'7',
	(byte)'8', (byte)'9', (byte)'A', (byte)'B',
	(byte)'C', (byte)'D', (byte)'E', (byte)'F'
    };

    static final int  HEXDIGITS[] = {
	-1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1,
	 0,  1,  2,  3,  4,  5,  6,  7,
	 8,  9, -1, -1, -1, -1, -1, -1,
	-1, 10, 11, 12, 13, 14, 15, -1,
	-1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1,
	-1, 10, 11, 12, 13, 14, 15, -1,
	-1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1,
    };

    static final char  HEXCHARS[] = {
	'0', '1', '2', '3',
	'4', '5', '6', '7',
	'8', '9', 'A', 'B',
	'C', 'D', 'E', 'F'
    };

    //
    // Two tables used to identify characters that don't need special
    // attention during a URL encoding.
    //

    static final boolean  MIME_CLEARCHARS[] = {
	false, false, false, false, false, false, false, false,
	false, false, false, false, false, false, false, false,
	false, false, false, false, false, false, false, false,
	false, false, false, false, false, false, false, false,
	 true, false, false, false, false, false, false, false,
	false, false,  true, false, false,  true,  true, false,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true, false, false, false, false, false, false,
	false,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true, false, false, false, false,  true,
	false,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true, false, false, false, false, false,
    };

    static final boolean  IETF_CLEARCHARS[] = {
	false, false, false, false, false, false, false, false,
	false, false, false, false, false, false, false, false,
	false, false, false, false, false, false, false, false,
	false, false, false, false, false, false, false, false,
	false,  true, false, false,  true, false,  true, false,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true, false,  true, false,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true, false, false, false, false,  true,
	false,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true, false, false, false,  true, false,
    };

    static final boolean  HTML_CLEARCHARS[] = {
	false, false, false, false, false, false, false, false,
	false, false,  true, false, false,  true, false, false,
	false, false, false, false, false, false, false, false,
	false, false, false, false, false, false, false, false,
	 true,  true, false,  true,  true,  true, false,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true, false,  true, false,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true,  true,
	 true,  true,  true,  true,  true,  true,  true, false,
    };

    //
    // Some of this causes problems if a user starts the interpreter using
    // an old version of Java because Character.toString(char) was added
    // in 1.4. In general we don't care, but this class is loaded really
    // early so the static initialization of HTML_CHARMAP[] means YoixMain
    // won't get a chance to give the user a friendly warning. Definitely
    // not a big deal, but a message in a dialog seems much better that a
    // Java trace.
    //

    private static Object  HTML_CHARMAP[] = {
	      // from the HTML4.1 spec at w3c.org
	      "quot", String.valueOf((char)34),
	       "amp", String.valueOf((char)38),
	        "lt", String.valueOf((char)60),
	        "gt", String.valueOf((char)62),
	      "nbsp", String.valueOf((char)160),
	     "iexcl", String.valueOf((char)161),
	      "cent", String.valueOf((char)162),
	     "pound", String.valueOf((char)163),
	    "curren", String.valueOf((char)164),
	       "yen", String.valueOf((char)165),
	    "brvbar", String.valueOf((char)166),
	      "sect", String.valueOf((char)167),
	       "uml", String.valueOf((char)168),
	      "copy", String.valueOf((char)169),
	      "ordf", String.valueOf((char)170),
	     "laquo", String.valueOf((char)171),
	       "not", String.valueOf((char)172),
	       "shy", String.valueOf((char)173),
	       "reg", String.valueOf((char)174),
	      "macr", String.valueOf((char)175),
	       "deg", String.valueOf((char)176),
	    "plusmn", String.valueOf((char)177),
	      "sup2", String.valueOf((char)178),
	      "sup3", String.valueOf((char)179),
	     "acute", String.valueOf((char)180),
	     "micro", String.valueOf((char)181),
	      "para", String.valueOf((char)182),
	    "middot", String.valueOf((char)183),
	     "cedil", String.valueOf((char)184),
	      "sup1", String.valueOf((char)185),
	      "ordm", String.valueOf((char)186),
	     "raquo", String.valueOf((char)187),
	    "frac14", String.valueOf((char)188),
	    "frac12", String.valueOf((char)189),
	    "frac34", String.valueOf((char)190),
	    "iquest", String.valueOf((char)191),
	    "Agrave", String.valueOf((char)192),
	    "Aacute", String.valueOf((char)193),
	     "Acirc", String.valueOf((char)194),
	    "Atilde", String.valueOf((char)195),
	      "Auml", String.valueOf((char)196),
	     "Aring", String.valueOf((char)197),
	     "AElig", String.valueOf((char)198),
	    "Ccedil", String.valueOf((char)199),
	    "Egrave", String.valueOf((char)200),
	    "Eacute", String.valueOf((char)201),
	     "Ecirc", String.valueOf((char)202),
	      "Euml", String.valueOf((char)203),
	    "Igrave", String.valueOf((char)204),
	    "Iacute", String.valueOf((char)205),
	     "Icirc", String.valueOf((char)206),
	      "Iuml", String.valueOf((char)207),
	       "ETH", String.valueOf((char)208),
	    "Ntilde", String.valueOf((char)209),
	    "Ograve", String.valueOf((char)210),
	    "Oacute", String.valueOf((char)211),
	     "Ocirc", String.valueOf((char)212),
	    "Otilde", String.valueOf((char)213),
	      "Ouml", String.valueOf((char)214),
	     "times", String.valueOf((char)215),
	    "Oslash", String.valueOf((char)216),
	    "Ugrave", String.valueOf((char)217),
	    "Uacute", String.valueOf((char)218),
	     "Ucirc", String.valueOf((char)219),
	      "Uuml", String.valueOf((char)220),
	    "Yacute", String.valueOf((char)221),
	     "THORN", String.valueOf((char)222),
	     "szlig", String.valueOf((char)223),
	    "agrave", String.valueOf((char)224),
	    "aacute", String.valueOf((char)225),
	     "acirc", String.valueOf((char)226),
	    "atilde", String.valueOf((char)227),
	      "auml", String.valueOf((char)228),
	     "aring", String.valueOf((char)229),
	     "aelig", String.valueOf((char)230),
	    "ccedil", String.valueOf((char)231),
	    "egrave", String.valueOf((char)232),
	    "eacute", String.valueOf((char)233),
	     "ecirc", String.valueOf((char)234),
	      "euml", String.valueOf((char)235),
	    "igrave", String.valueOf((char)236),
	    "iacute", String.valueOf((char)237),
	     "icirc", String.valueOf((char)238),
	      "iuml", String.valueOf((char)239),
	       "eth", String.valueOf((char)240),
	    "ntilde", String.valueOf((char)241),
	    "ograve", String.valueOf((char)242),
	    "oacute", String.valueOf((char)243),
	     "ocirc", String.valueOf((char)244),
	    "otilde", String.valueOf((char)245),
	      "ouml", String.valueOf((char)246),
	    "divide", String.valueOf((char)247),
	    "oslash", String.valueOf((char)248),
	    "ugrave", String.valueOf((char)249),
	    "uacute", String.valueOf((char)250),
	     "ucirc", String.valueOf((char)251),
	      "uuml", String.valueOf((char)252),
	    "yacute", String.valueOf((char)253),
	     "thorn", String.valueOf((char)254),
	      "yuml", String.valueOf((char)255),
	     "OElig", String.valueOf((char)338),
	     "oelig", String.valueOf((char)339),
	    "Scaron", String.valueOf((char)352),
	    "scaron", String.valueOf((char)353),
	      "Yuml", String.valueOf((char)376),
	      "fnof", String.valueOf((char)402),
	      "circ", String.valueOf((char)710),
	     "tilde", String.valueOf((char)732),
	     "Alpha", String.valueOf((char)913),
	      "Beta", String.valueOf((char)914),
	     "Gamma", String.valueOf((char)915),
	     "Delta", String.valueOf((char)916),
	   "Epsilon", String.valueOf((char)917),
	      "Zeta", String.valueOf((char)918),
	       "Eta", String.valueOf((char)919),
	     "Theta", String.valueOf((char)920),
	      "Iota", String.valueOf((char)921),
	     "Kappa", String.valueOf((char)922),
	    "Lambda", String.valueOf((char)923),
	        "Mu", String.valueOf((char)924),
	        "Nu", String.valueOf((char)925),
	        "Xi", String.valueOf((char)926),
	   "Omicron", String.valueOf((char)927),
	        "Pi", String.valueOf((char)928),
	       "Rho", String.valueOf((char)929),
	     "Sigma", String.valueOf((char)931),
	       "Tau", String.valueOf((char)932),
	   "Upsilon", String.valueOf((char)933),
	       "Phi", String.valueOf((char)934),
	       "Chi", String.valueOf((char)935),
	       "Psi", String.valueOf((char)936),
	     "Omega", String.valueOf((char)937),
	     "alpha", String.valueOf((char)945),
	      "beta", String.valueOf((char)946),
	     "gamma", String.valueOf((char)947),
	     "delta", String.valueOf((char)948),
	   "epsilon", String.valueOf((char)949),
	      "zeta", String.valueOf((char)950),
	       "eta", String.valueOf((char)951),
	     "theta", String.valueOf((char)952),
	      "iota", String.valueOf((char)953),
	     "kappa", String.valueOf((char)954),
	    "lambda", String.valueOf((char)955),
	        "mu", String.valueOf((char)956),
	        "nu", String.valueOf((char)957),
	        "xi", String.valueOf((char)958),
	   "omicron", String.valueOf((char)959),
	        "pi", String.valueOf((char)960),
	       "rho", String.valueOf((char)961),
	    "sigmaf", String.valueOf((char)962),
	     "sigma", String.valueOf((char)963),
	       "tau", String.valueOf((char)964),
	   "upsilon", String.valueOf((char)965),
	       "phi", String.valueOf((char)966),
	       "chi", String.valueOf((char)967),
	       "psi", String.valueOf((char)968),
	     "omega", String.valueOf((char)969),
	  "thetasym", String.valueOf((char)977),
	     "upsih", String.valueOf((char)978),
	       "piv", String.valueOf((char)982),
	      "ensp", String.valueOf((char)8194),
	      "emsp", String.valueOf((char)8195),
	    "thinsp", String.valueOf((char)8201),
	      "zwnj", String.valueOf((char)8204),
	       "zwj", String.valueOf((char)8205),
	       "lrm", String.valueOf((char)8206),
	       "rlm", String.valueOf((char)8207),
	     "ndash", String.valueOf((char)8211),
	     "mdash", String.valueOf((char)8212),
	     "lsquo", String.valueOf((char)8216),
	     "rsquo", String.valueOf((char)8217),
	     "sbquo", String.valueOf((char)8218),
	     "ldquo", String.valueOf((char)8220),
	     "rdquo", String.valueOf((char)8221),
	     "bdquo", String.valueOf((char)8222),
	    "dagger", String.valueOf((char)8224),
	    "Dagger", String.valueOf((char)8225),
	      "bull", String.valueOf((char)8226),
	    "hellip", String.valueOf((char)8230),
	    "permil", String.valueOf((char)8240),
	     "prime", String.valueOf((char)8242),
	     "Prime", String.valueOf((char)8243),
	    "lsaquo", String.valueOf((char)8249),
	    "rsaquo", String.valueOf((char)8250),
	     "oline", String.valueOf((char)8254),
	     "frasl", String.valueOf((char)8260),
	      "euro", String.valueOf((char)8364),
	     "image", String.valueOf((char)8465),
	    "weierp", String.valueOf((char)8472),
	      "real", String.valueOf((char)8476),
	     "trade", String.valueOf((char)8482),
	   "alefsym", String.valueOf((char)8501),
	      "larr", String.valueOf((char)8592),
	      "uarr", String.valueOf((char)8593),
	      "rarr", String.valueOf((char)8594),
	      "darr", String.valueOf((char)8595),
	      "harr", String.valueOf((char)8596),
	     "crarr", String.valueOf((char)8629),
	      "lArr", String.valueOf((char)8656),
	      "uArr", String.valueOf((char)8657),
	      "rArr", String.valueOf((char)8658),
	      "dArr", String.valueOf((char)8659),
	      "hArr", String.valueOf((char)8660),
	    "forall", String.valueOf((char)8704),
	      "part", String.valueOf((char)8706),
	     "exist", String.valueOf((char)8707),
	     "empty", String.valueOf((char)8709),
	     "nabla", String.valueOf((char)8711),
	      "isin", String.valueOf((char)8712),
	     "notin", String.valueOf((char)8713),
	        "ni", String.valueOf((char)8715),
	      "prod", String.valueOf((char)8719),
	       "sum", String.valueOf((char)8721),
	     "minus", String.valueOf((char)8722),
	    "lowast", String.valueOf((char)8727),
	     "radic", String.valueOf((char)8730),
	      "prop", String.valueOf((char)8733),
	     "infin", String.valueOf((char)8734),
	       "ang", String.valueOf((char)8736),
	       "and", String.valueOf((char)8743),
	        "or", String.valueOf((char)8744),
	       "cap", String.valueOf((char)8745),
	       "cup", String.valueOf((char)8746),
	       "int", String.valueOf((char)8747),
	    "there4", String.valueOf((char)8756),
	       "sim", String.valueOf((char)8764),
	      "cong", String.valueOf((char)8773),
	     "asymp", String.valueOf((char)8776),
	        "ne", String.valueOf((char)8800),
	     "equiv", String.valueOf((char)8801),
	        "le", String.valueOf((char)8804),
	        "ge", String.valueOf((char)8805),
	       "sub", String.valueOf((char)8834),
	       "sup", String.valueOf((char)8835),
	      "nsub", String.valueOf((char)8836),
	      "sube", String.valueOf((char)8838),
	      "supe", String.valueOf((char)8839),
	     "oplus", String.valueOf((char)8853),
	    "otimes", String.valueOf((char)8855),
	      "perp", String.valueOf((char)8869),
	      "sdot", String.valueOf((char)8901),
	     "lceil", String.valueOf((char)8968),
	     "rceil", String.valueOf((char)8969),
	    "lfloor", String.valueOf((char)8970),
	    "rfloor", String.valueOf((char)8971),
	      "lang", String.valueOf((char)9001),
	      "rang", String.valueOf((char)9002),
	       "loz", String.valueOf((char)9674),
	    "spades", String.valueOf((char)9824),
	     "clubs", String.valueOf((char)9827),
	    "hearts", String.valueOf((char)9829),
	     "diams", String.valueOf((char)9830),
    };

    private static final HashMap  htmlcharmap = new HashMap((int)(HTML_CHARMAP.length/2));

    private static final int  MINIMUM_HTMLINFO = 2;	// between the '&' and the ';'
    private static final int  MAXIMUM_HTMLINFO = 8;	// between the '&' and the ';'

    static {
	for(int n = 0; n < HTML_CHARMAP.length; n += 2)
	    htmlcharmap.put(HTML_CHARMAP[n], HTML_CHARMAP[n+1]);
	HTML_CHARMAP = null;
    };

    private static final char  PWD[] = initPWD();
    private static int         nextid = 1;

    //
    // For reverse color lookups.
    //

    static final HashMap  reversecolor = new HashMap();

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public static void
    appendText(YoixObject dest, String text) {

	if (dest.isComponent())
	    ((YoixBodyComponent)dest.body()).appendText(text);
    }


    public static int
    atoi(String str) {

	return(atoi(str, 0, 0, null));
    }


    public static int
    atoi(String str, int value) {

	return(atoi(str, 0, value, null));
    }


    public static int
    atoi(String str, int radix, int value) {

	return(atoi(str, radix, value, null));
    }


    public static int
    atoi(String str, int radix, int value, int consumed[]) {

	int  length;
	int  skipped = 0;
	int  sign;

	if (radix == 10) {
	    if (str.startsWith("+")) {
		str = str.substring(1);
		skipped++;
	    }
	    for (length = str.length(); length > 0; length--) {
		try {
		    value = Integer.parseInt(str, 10);
		    break;
		}
		catch(NumberFormatException e) {
		    str = str.substring(0, length - 1);
		}
	    }
	} else {
	    if (str.startsWith("+")) {
		sign = 1;
		str = str.substring(1);
		skipped++;
	    } else if (str.startsWith("-")) {
		sign = -1;
		str = str.substring(1);
		skipped++;
	    } else sign = 1;
	    if (radix == 0 || radix == 16) {
		if (str.length() > 1 && str.charAt(0) == '0') {
		    if (str.charAt(1) == 'x' || str.charAt(1) == 'X') {
			radix = 16;
			str = str.substring(2);
			skipped += 2;
		    } else radix = (radix == 0) ? 8 : radix;
		} else radix = (radix == 0) ? 10 : radix;
	    }
	    for (length = str.length(); length > 0; length--) {
		try {
		    value = sign*Integer.parseInt(str, radix);
		    break;
		}
		catch(NumberFormatException e) {
		    str = str.substring(0, length - 1);
		}
	    }
	}

	if (consumed != null && consumed.length >= 1)
	    consumed[0] += skipped + str.length();

	return(value);
    }


    public static YoixObject
    call(YoixObject function, YoixObject arg) {

	return(call(function, new YoixObject[] {arg}, null));
    }


    public static YoixObject
    call(YoixObject function, YoixObject argv[]) {

	return(call(function, argv, null));
    }


    public static YoixObject
    call(YoixObject function, YoixObject arg, YoixObject context) {

	return(call(function, new YoixObject[] {arg}, context));
    }


    public static YoixObject
    call(YoixObject function, YoixObject argv[], YoixObject context) {

	YoixObject  obj = null;
	YoixError   interrupt_point = null;
	YoixError   error_point = null;

	//
	// Be careful, java.awt.EventDispatchThread.run() can sometimes
	// generates unwanted noise!!
	//
	// NOTE - This method appears in YoixPointerActive except this
	// one is public and static. We eventually should take a close
	// look at all custom call() methods and the ones that can use
	// this method should be replaced. Having a public static call()
	// is particularly useful for add-on modules.
	// 

	if (function != null) {
	    if (function.notNull()) {
		try {
		    error_point = VM.pushError();
		    try {
			interrupt_point = VM.pushInterrupt();
			obj = function.call(argv, context).resolve();
		    }
		    catch(YoixError e) {
			if (e != interrupt_point)
			    throw(e);
		    }
		    VM.popError();
		}
		catch(YoixError e) {
		    if (e != error_point)
			throw(e);
		    else VM.error(error_point);
		}
		catch(SecurityException e) {
		    VM.error(e);
		    VM.popError();
		}
	    }
	}

	return(obj);
    }


    public static YoixObject
    copyInto(YoixObject src, YoixObject dest) {

	int  length;
	int  m;
	int  n;

	if (src.isPointer() && dest.isPointer()) {
	    if (src.isString() == false) {
		switch (dest.minor()) {
		    case DICTIONARY:
			length = src.length();
			for (n = src.offset(); n < length; n++) {
			    if (src.defined(n))
				dest.put(src.name(n), src.get(n, true), false);
			}
			break;

		    default:
			length = src.length();
			for (n = src.offset(), m = dest.offset(); n < length; n++, m++) {
			    if (src.defined(n))
				dest.put(m, src.get(n, true), false);
			}
			break;
		}
	    } else copyInto(src.stringValue(), dest);
	} else VM.abort(TYPECHECK);

	return(dest);
    }


    public static YoixObject
    copyInto(String src, YoixObject dest) {

	int  length;
	int  m;
	int  n;

	if (dest.isPointer()) {
	    switch (dest.minor()) {
		case ARRAY:
		    length = src.length();
		    for (n = 0, m = dest.offset(); n < length; n++, m++)
			dest.putInt(m, src.charAt(n));
		    break;

		case DICTIONARY:
		    length = src.length();
		    for (n = 0; n < length; n++)
			dest.putInt(n + "", src.charAt(n));
		    break;

		case STRING:
		    dest.overlay(src);
		    break;

		default:
		    VM.abort(UNIMPLEMENTED);
		    break;
	    }
	} else VM.abort(TYPECHECK);

	return(dest);
    }


    public static YoixObject
    copyIntoArray(int src[]) {

	YoixObject  dest;
	int         length;
	int         n;

	if (src != null) {
	    length = src.length;
	    dest = YoixObject.newArray(length);
	    for (n = 0; n < length; n++)
		dest.put(n, YoixObject.newInt(src[n]), false);
	} else dest = YoixObject.newArray();

	return(dest);
    }


    public static YoixObject
    copyIntoArray(float src[]) {

	YoixObject  dest;
	int         length;
	int         n;

	if (src != null) {
	    length = src.length;
	    dest = YoixObject.newArray(length);
	    for (n = 0; n < length; n++)
		dest.put(n, YoixObject.newDouble(src[n]), false);
	} else dest = YoixObject.newArray();

	return(dest);
    }


    public static YoixObject
    copyIntoArray(double src[]) {

	YoixObject  dest;
	int         length;
	int         n;

	if (src != null) {
	    length = src.length;
	    dest = YoixObject.newArray(length);
	    for (n = 0; n < length; n++)
		dest.put(n, YoixObject.newDouble(src[n]), false);
	} else dest = YoixObject.newArray();

	return(dest);
    }


    public static YoixObject
    copyIntoArray(Color src[]) {

	YoixObject  dest;
	int         length;
	int         n;

	if (src != null) {
	    length = src.length;
	    dest = YoixObject.newArray(length);
	    for (n = 0; n < length; n++)
		dest.put(n, YoixObject.newColor(src[n]), false);
	} else dest = YoixObject.newArray();

	return(dest);
    }


    public static YoixObject
    copyIntoArray(String src[]) {

	YoixObject  dest;
	int         length;
	int         n;

	if (src != null) {
	    length = src.length;
	    dest = YoixObject.newArray(length);
	    for (n = 0; n < length; n++)
		dest.put(n, YoixObject.newString(src[n]), false);
	} else dest = YoixObject.newArray();

	return(dest);
    }


    public static YoixObject
    copyIntoArray(YoixObject src[]) {

	return(YoixObject.newArray(src));
    }


    public static YoixObject
    copyIntoArray(Object src[]) {

	return(copyIntoArray(src, false));
    }


    public static YoixObject
    copyIntoArray(Object src[], boolean nullpad) {

	YoixObject  dest;
	Object      value;
	int         length;
	int         n;

	if (src != null) {
	    length = src.length;
	    dest = YoixObject.newArray(length);
	    for (n = 0; n < length; n++) {
		value = src[n];
		if (!(value instanceof YoixObject)) {
		    if (value instanceof String)
			dest.put(n, YoixObject.newString((String)value), false);
		    else if (value instanceof Number)
			dest.put(n, YoixObject.newNumber((Number)value), false);
		    else if (value instanceof Boolean)
			dest.put(n, YoixObject.newInt((Boolean)value), false);
		    else if (value instanceof StringBuffer)
			dest.put(n, YoixObject.newString((StringBuffer)value), false);
		    else if (value instanceof Color)
			dest.put(n, YoixMake.yoixColor((Color)value), false);
		    else if (value instanceof Map)
			dest.put(n, copyIntoDictionary((Map)value), false);
		    else if (value instanceof List)
			dest.put(n, copyIntoArray((List)value, nullpad, value), false);
		    else if (value instanceof Object[])
			dest.put(n, copyIntoArray((Object[])value, nullpad), false);
		    else if ((value = YoixMake.yoixObject(value)) != null)
			dest.put(n, (YoixObject)value, false);
		    else if (nullpad)
			dest.put(n, YoixObject.newNull(), false);
		} else dest.put(n, (YoixObject)value, true);
	    }
	} else dest = YoixObject.newArray();

	return(dest);
    }


    public static YoixObject
    copyIntoArray(Object src[], boolean nullpad, boolean growable) {

	YoixObject  dest;
	Object      value;
	int         length;
	int         n;

	//
	// This was added very quickly (on 2/5/11) - there's undoubtedly lots
	// of room for improvement and simplification here and in many of the
	// reltated methods!!
	//

	if (src != null) {
	    length = src.length;
	    dest = YoixObject.newArray(length);
	    dest.setGrowable(growable);
	    for (n = 0; n < length; n++) {
		value = src[n];
		if (!(value instanceof YoixObject)) {
		    if (value instanceof String)
			dest.put(n, YoixObject.newString((String)value), false);
		    else if (value instanceof Number)
			dest.put(n, YoixObject.newNumber((Number)value), false);
		    else if (value instanceof Boolean)
			dest.put(n, YoixObject.newInt((Boolean)value), false);
		    else if (value instanceof StringBuffer)
			dest.put(n, YoixObject.newString((StringBuffer)value), false);
		    else if (value instanceof Color)
			dest.put(n, YoixMake.yoixColor((Color)value), false);
		    else if (value instanceof Map)
			dest.put(n, copyIntoDictionary((Map)value), false);
		    else if (value instanceof List)
			dest.put(n, copyIntoArray((List)value, nullpad, growable, value), false);
		    else if (value instanceof Object[])
			dest.put(n, copyIntoArray((Object[])value, nullpad, growable), false);
		    else if ((value = YoixMake.yoixObject(value)) != null)
			dest.put(n, (YoixObject)value, false);
		    else if (nullpad)
			dest.put(n, YoixObject.newNull(), false);
		} else dest.put(n, (YoixObject)value, true);
	    }
	} else {
	    dest = YoixObject.newArray();
	    dest.setGrowable(growable);
	}

	return(dest);
    }


    public static YoixObject
    copyIntoArray(List src) {

	return(copyIntoArray(src, false, src));
    }


    public static YoixObject
    copyIntoArray(List src, boolean nullpad) {

	return(copyIntoArray(src, nullpad, src));
    }


    public static YoixObject
    copyIntoArray(List src, boolean nullpad, boolean growable) {

	return(copyIntoArray(src, nullpad, growable, src));
    }


    public static YoixObject
    copyIntoArray(List src, Object lock) {

	return(copyIntoArray(src, false, lock));
    }


    public static YoixObject
    copyIntoArray(List src, boolean nullpad, Object lock) {

	YoixObject  dest;
	Object      value;
	int         length;
	int         n;

	if (src != null) {
	    synchronized(lock) {
		length = src.size();
		dest = YoixObject.newArray(length);
		for (n = 0; n < length; n++) {
		    value = src.get(n);
		    if (!(value instanceof YoixObject)) {
			if (value instanceof String)
			    dest.put(n, YoixObject.newString((String)value), false);
			else if (value instanceof Number)
			    dest.put(n, YoixObject.newNumber((Number)value), false);
			else if (value instanceof Boolean)
			    dest.put(n, YoixObject.newInt((Boolean)value), false);
			else if (value instanceof StringBuffer)
			    dest.put(n, YoixObject.newString((StringBuffer)value), false);
			else if (value instanceof Color)
			    dest.put(n, YoixMake.yoixColor((Color)value), false);
			else if (value instanceof Map)
			    dest.put(n, copyIntoDictionary((Map)value), false);
			else if (value instanceof List)
			    dest.put(n, copyIntoArray((List)value, nullpad, value), false);
			else if (value instanceof Object[])
			    dest.put(n, copyIntoArray((Object[])value, nullpad), false);
			else if ((value = YoixMake.yoixObject(value)) != null)
			    dest.put(n, (YoixObject)value, false);
			else if (nullpad)
			    dest.put(n, YoixObject.newNull(), false);
		    } else dest.put(n, (YoixObject)value, true);
		}
	    }
	} else dest = YoixObject.newArray();

	return(dest);
    }


    public static YoixObject
    copyIntoArray(List src, boolean nullpad, boolean growable, Object lock) {

	YoixObject  dest;
	Object      value;
	int         length;
	int         n;

	if (src != null) {
	    synchronized(lock) {
		length = src.size();
		dest = YoixObject.newArray(length);
		dest.setGrowable(growable);
		for (n = 0; n < length; n++) {
		    value = src.get(n);
		    if (!(value instanceof YoixObject)) {
			if (value instanceof String)
			    dest.put(n, YoixObject.newString((String)value), false);
			else if (value instanceof Number)
			    dest.put(n, YoixObject.newNumber((Number)value), false);
			else if (value instanceof Boolean)
			    dest.put(n, YoixObject.newInt((Boolean)value), false);
			else if (value instanceof StringBuffer)
			    dest.put(n, YoixObject.newString((StringBuffer)value), false);
			else if (value instanceof Color)
			    dest.put(n, YoixMake.yoixColor((Color)value), false);
			else if (value instanceof Map)
			    dest.put(n, copyIntoDictionary((Map)value), false);
			else if (value instanceof List)
			    dest.put(n, copyIntoArray((List)value, nullpad, growable, value), false);
			else if (value instanceof Object[])
			    dest.put(n, copyIntoArray((Object[])value, nullpad, growable), false);
			else if ((value = YoixMake.yoixObject(value)) != null)
			    dest.put(n, (YoixObject)value, false);
			else if (nullpad)
			    dest.put(n, YoixObject.newNull(), false);
		    } else dest.put(n, (YoixObject)value, true);
		}
	    }
	} else {
	    dest = YoixObject.newArray();
	    dest.setGrowable(growable);
	}

	return(dest);
    }


    public static ArrayList
    copyIntoArrayList(Object src[]) {

	ArrayList  dest;
	int        n;

	if (src != null) {
	    dest = new ArrayList(src.length);
	    for (n = 0; n < src.length; n++)
		dest.add(src[n]);
	} else dest = new ArrayList();

	return(dest);
    }


    public static YoixObject
    copyIntoDictionary(Map src) {

	return(copyIntoDictionary(src, YoixObject.newDictionary(src.size()), src));
    }


    public static YoixObject
    copyIntoDictionary(Map src, boolean growable) {

	YoixObject  dest;

	dest = YoixObject.newDictionary(src.size());
	dest.setGrowable(growable);
	return(copyIntoDictionary(src, dest, growable, src));
    }


    public static YoixObject
    copyIntoDictionary(Map src, YoixObject dest) {

	return(copyIntoDictionary(src, dest, src));
    }


    public static YoixObject
    copyIntoDictionary(Map src, YoixObject dest, Object lock) {

	Iterator  iterator;
	Object    key;
	Object    value;

	if (dest.isDictionary()) {
	    synchronized(lock) {
		for (iterator = src.keySet().iterator(); iterator.hasNext(); ) {
		    key = iterator.next();
		    if (key instanceof String || (key == null && src.get(key) != null)) {
			value = src.get(key);
			if (key == null)
			    key = "null";
			if (!(value instanceof YoixObject)) {
			    if (value instanceof String)
				dest.put((String)key, YoixObject.newString((String)value), false);
			    else if (value instanceof Number)
				dest.put((String)key, YoixObject.newNumber((Number)value), false);
			    else if (value instanceof Boolean)
				dest.put((String)key, YoixObject.newInt((Boolean)value), false);
			    else if (value instanceof StringBuffer)
				dest.put((String)key, YoixObject.newString((StringBuffer)value), false);
			    else if (value instanceof Color)
				dest.put((String)key, YoixMake.yoixColor((Color)value), false);
			    else if (value instanceof Map)
				dest.put((String)key, copyIntoDictionary((Map)value), false);
			    else if (value instanceof List)
				dest.put((String)key, copyIntoArray((List)value, false, value), false);
			    else if (value instanceof Object[])
				dest.put((String)key, copyIntoArray((Object[])value, false), false);
			    else if ((value = YoixMake.yoixObject(value)) != null)
				dest.put((String)key, (YoixObject)value, false);
			} else dest.put((String)key, (YoixObject)value, true);
		    }
		}
	    }
	} else VM.abort(TYPECHECK);

	return(dest);
    }


    public static YoixObject
    copyIntoDictionary(Map src, YoixObject dest, boolean growable, Object lock) {

	Iterator  iterator;
	Object    key;
	Object    value;

	if (dest.isDictionary()) {
	    synchronized(lock) {
		for (iterator = src.keySet().iterator(); iterator.hasNext(); ) {
		    key = iterator.next();
		    if (key instanceof String || (key == null && src.get(key) != null)) {
			value = src.get(key);
			if (key == null)
			    key = "null";
			if (!(value instanceof YoixObject)) {
			    if (value instanceof String)
				dest.put((String)key, YoixObject.newString((String)value), false);
			    else if (value instanceof Number)
				dest.put((String)key, YoixObject.newNumber((Number)value), false);
			    else if (value instanceof Boolean)
				dest.put((String)key, YoixObject.newInt((Boolean)value), false);
			    else if (value instanceof StringBuffer)
				dest.put((String)key, YoixObject.newString((StringBuffer)value), false);
			    else if (value instanceof Color)
				dest.put((String)key, YoixMake.yoixColor((Color)value), false);
			    else if (value instanceof Map)
				dest.put((String)key, copyIntoDictionary((Map)value, growable), false);
			    else if (value instanceof List)
				dest.put((String)key, copyIntoArray((List)value, false, growable, value), false);
			    else if (value instanceof Object[])
				dest.put((String)key, copyIntoArray((Object[])value, false, growable), false);
			    else if ((value = YoixMake.yoixObject(value)) != null)
				dest.put((String)key, (YoixObject)value, false);
			} else dest.put((String)key, (YoixObject)value, true);
		    }
		}
	    }
	} else VM.abort(TYPECHECK);

	return(dest);
    }


    public static YoixObject
    copyIntoDictionary(List src) {

	YoixObject  dest;
	Object      key;
	Object      value;
	String      name;
	int         length;
	int         n;

	if (src != null) {
	    synchronized(src) {
		length = src.size();
		dest = YoixObject.newDictionary(length/2);
		for (n = 0; n < length - 1; n += 2) {
		    key = src.get(n);
		    if (key instanceof YoixObject && ((YoixObject)key).isString())
			key = ((YoixObject)key).stringValue();
		    if (key instanceof String) {
			name = (String)key;
			value = src.get(n+1);
			if (!(value instanceof YoixObject)) {
			    if (value instanceof String)
				dest.put(name, YoixObject.newString((String)value), false);
			    else if (value instanceof Number)
				dest.put(name, YoixObject.newNumber((Number)value), false);
			    else if (value instanceof Boolean)
				dest.put(name, YoixObject.newInt((Boolean)value), false);
			    else if (value instanceof StringBuffer)
				dest.put(name, YoixObject.newString((StringBuffer)value), false);
			    else if (value instanceof Color)
				dest.put(name, YoixMake.yoixColor((Color)value), false);
			    else if (value instanceof Map)
				dest.put(name, copyIntoDictionary((Map)value), false);
			    else if (value instanceof List)
				dest.put(name, copyIntoArray((List)value, false, value), false);
			    else if (value instanceof Object[])
				dest.put(name, copyIntoArray((Object[])value, false), false);
			    else if ((value = YoixMake.yoixObject(value)) != null)
				dest.put(name, (YoixObject)value, false);
			} else dest.put(name, (YoixObject)value, true);
		    }
		}
	    }
	} else dest = YoixObject.newDictionary();

	return(dest);
    }


    public static YoixObject
    copyIntoGrowableArray(List src, boolean nullpad, Object lock) {

	YoixObject  obj;

	if ((obj = copyIntoArray(src, nullpad, lock)) != null) {
	    if (obj.notNull())
		obj.setGrowable(true);
	    else obj = YoixObject.newArray(0, -1);
	}
	return(obj);
    }


    public static double
    distance(Point p0, Point p1) {

	return(Math.sqrt(distance2(p0, p1)));
    }


    public static double
    distance2(Point p0, Point p1) {

	return((p1.x - p0.x)*(p1.x - p0.x) + (p1.y - p0.y)*(p1.y - p0.y));
    }


    public static double
    fileSize(String filename) {

	double  result = 0;

	if (filename != null) {
	    try {
		result = (double)((new File(filename)).length());
	    }
	    catch(RuntimeException e) {}
	}

	return(result);
    }


    public static String
    fmt(String str) {

	return(fmt(str.toCharArray(), 65, 75, false, false));
    }


    public static String
    fmt(String str, int goal) {

	return(fmt(str.toCharArray(), goal, goal+10, false, false));
    }


    public static String
    fmt(String str, int goal, int maximum) {

	return(fmt(str.toCharArray(), goal, maximum, false, false));
    }


    public static String
    fmt(String str, int goal, int maximum, boolean forced) {

	return(fmt(str.toCharArray(), goal, maximum, forced, false));
    }


    public static String
    fmt(String str, int goal, int maximum, boolean forced, boolean squeeze) {

	return(str != null ? fmt(str.toCharArray(), goal, maximum, forced, squeeze) : null);
    }


    public static String
    fmt(char chars[], int goal, int maximum, boolean forced, boolean squeeze) {

	StringBuffer  result = null;
	boolean       newline;
	char          previous;
	char          sp;
	int           idx;
	int           indent;
	int           jdx;
	int           line;
	int           spaces;
	int           type;
	int           word;

	//
	// Each line attempts to be about "goal" long, but no longer than
	// the maximum (goal+10 by default) unless there is no break point
	// in the string. Breaks only occur at spaces (' ') and newlines
	// ('\n'). Carriage returns are skipped and removed (and only put
	// back if NL contains one). A non-zero "squeeze" will squeeze
	// multiple spaces or newlines between words to one. Spaces at the
	// beginning of a line are taken as indentation and all following
	// lines are equally indented until two newlines in a row (or \r\n
	// combos in a row) are encountered.
	//
	// Think we currently append a newline if we're at the end of the
	// string but have exceeded our limits. Is that the right behavior
	// and if so should it be controllable??
	//

	if (chars != null) {
	    idx = 0;
	    line = 0;
	    indent = 0;
	    spaces = 0;
	    previous = '\n';
	    newline = false;
	    sp = ' ';
	    result = new StringBuffer();
	    while (idx < chars.length) {
		if (newline)
		    result.append(NL);
		newline = false;
		switch(chars[idx]) {
		    case ' ':
			sp = chars[idx];
			spaces = 0;
			if (previous == '\n') {
			    if (squeeze && result.length() > 0)
				newline = true;
			    line = 0;
			    indent = 0;
			    while (idx < chars.length && chars[idx] == ' ') {
				idx++;
				indent++;
				if (forced && indent == maximum) {
				    if (!squeeze) {
					while(indent-- > 0)
					    result.append(' ');
				    }
				    result.append(NL);
				    indent = 0;
				}
			    }
			    previous = ' ';
			} else {
			    while (idx < chars.length && chars[idx] == ' ') {
				idx++;
				spaces++;
				if (forced && !squeeze && spaces == maximum) {
				    while(spaces-- > 0)
					result.append(' ');
				    result.append(NL);
				    spaces = 0;
				}
			    }
			    if (squeeze)
				spaces = 1;
			}
			previous = sp;
			break;

		    case '\r':
			if (previous == '\n') {
			    previous = '\r';
			}
			// fall through to...

		    case '\n':
			if (previous == '\n') {
			    line = 0;
			    indent = 0;
			    spaces = 0;
			    while (idx < chars.length && (chars[idx] == '\n' || chars[idx] == '\r')) {
				if (chars[idx] == '\n')
				    spaces++;
				idx++;
			    }
			    if (squeeze && spaces > 0) {
				if (result.length() == 0)
				    spaces = 0;
				else spaces = 1;
			    }
			    if (idx < chars.length) {
				while (spaces-- > 0)
				    result.append(NL);
			    }
			} else {
			    previous = chars[idx];
			    idx++;
			    spaces = 1;
			}
			break;

		    default:
			if (previous == '\n') {
			    previous = chars[idx];
			    if (spaces == 0)
				spaces = 1;
			}
			word = idx;
			while (word < chars.length && chars[word] != ' ' && chars[word] != '\r' && chars[word] != '\n' && (!forced || (line == 0 && (indent + word - idx) < maximum) || (line != 0 && (line + spaces + word - idx) < maximum)))
			    word++;
			word -= idx;
			if (line == 0) {
			    if (indent > 0) {
				line = indent;
				for (jdx=0; jdx<indent; jdx++)
				    result.append(sp);
			    }
			    line += word;
			    result.append(chars, idx, word);
			    idx += word;
			    if (line > goal) {
				newline = true;
				line = 0;
			    }
			} else if ((line + spaces + word) < goal) {
			    line += spaces + word;
			    for (jdx=0; jdx<spaces; jdx++)
				result.append(sp);
			    result.append(chars, idx, word);
			    idx += word;
			} else if ((line + spaces + word) > maximum) {
			    newline = true;
			    line = 0;
			} else if ((line + spaces + word - goal) < (goal - line)) {
			    for (jdx=0; jdx<spaces; jdx++)
				result.append(sp);
			    result.append(chars, idx, word);
			    idx += word;
			    newline = true;
			    line = 0;
			} else {
			    newline = true;
			    line = 0;
			}
			spaces = 0;
			break;
		}
	    }
	}
	return(result != null ? result.toString() : null);
    }


    public static String
    getDefaultEncoding() {

	return(VM.getDefaultEncoding().stringValue());
    }


    public static InputStream
    getInputStream(String path) {

	return(getInputStream(path, true));
    }


    public static InputStream
    getInputStream(String path, boolean buffered) {

	InputStream  stream = null;

	try {
	    switch (guessStreamType(path)) {
		case FILE:
		    stream = new FileInputStream(toLocalPath(path));
		    break;

		case URL:
		    stream = getInputStream(new URL(path));
		    break;
	    }
	    if (buffered)
		stream = new BufferedInputStream(stream);
	}
	catch(IOException e) {
	    VM.caughtException(e);
	}

	return(stream);
    }


    public static InputStream
    getInputStream(URL url) 

	throws IOException

    {

	//
	// Unfortunately Java's URL class is final so we can't extend it,
	// but when we handle https we need to make sure we've initialized
	// the YoixTrustPolicy class. Anyway, this is now the recommended
	// way to open URLs because it tries to guarantee the Yoix https
	// setup has completed.
	//

	return(YoixTrustPolicy.setupURL(url).openConnection().getInputStream());
    }


    public static OutputStream
    getOutputStream(URL url)

	throws IOException

    {

	//
	// Unfortunately Java's URL class is final so we can't extend it,
	// but when we handle https we need to make sure we've initialized
	// the YoixTrustPolicy class. Anyway, this is now the recommended
	// way to open URLs because it tries to guarantee the Yoix https
	// setup has completed.
	//

	return(YoixTrustPolicy.setupURL(url).openConnection().getOutputStream());
    }


    public static String
    getInterfaceAddress(String name) {

	return(getInterfaceAddress(name, IPV4|IPV6));
    }


    public static String
    getInterfaceAddress(String name, int mask) {

	ArrayList  list;
	HashMap    map;
	String     address = null;

	if ((map = getInterfaceAddresses(mask)) != null) {
	    if ((list = (ArrayList)map.get(name)) != null)
		address = (String)list.get(0);
	}
	return(address);
    }


    public static HashMap
    getInterfaceAddresses(int mask) {

	NetworkInterface  network;
	Enumeration       networks;
	Enumeration       addresses;
	InetAddress       inet;
	ArrayList         list;
	HashMap           map;
	String            name;
	String            address;
	int               protocols[] = {IPV4, IPV6};
	int               protocol;
	int               index;
	int               n;

	//
	// We guarantee that IPV4 addresses precede IPV6 addresses in the
	// ArrayLists that are associated with each network interface.
	//

	map = new HashMap();

	for (n = 0; n < protocols.length; n++) {
	    if ((mask & protocols[n]) != 0) {
		try {
		    networks = NetworkInterface.getNetworkInterfaces();
		    while (networks.hasMoreElements()) {
			network = (NetworkInterface)networks.nextElement();
			if ((name = network.getName()) != null) {
			    addresses = network.getInetAddresses();
			    while (addresses.hasMoreElements()) {
				inet = (InetAddress)addresses.nextElement();
				protocol = (inet instanceof Inet4Address) ? IPV4 : IPV6;
				if ((protocol & protocols[n]) != 0) {
				    if ((address = inet.getHostAddress()) != null) {
					if (inet instanceof Inet6Address) {
					    //
					    // Decided to eliminate the scope id that
					    // version 1.5 added. Not certain, but it
					    // seemed OK and probably can be backed out
					    // or dealt with in some other way if we
					    // need to.
					    //
					    if ((index = address.lastIndexOf('%')) > 0)
						address = address.substring(0, index);
					}
					if ((list = (ArrayList)map.get(name)) == null) {
					    list = new ArrayList();
					    map.put(name, list);
					}
					list.add(address);
				    }
				}
			    }
			}
		    }
		}
		catch(SocketException e) {}
	    }
	}
	return(map);
    }


    public static String
    getParserEncoding() {

	return(VM.getParserEncoding().stringValue());
    }


    public static BufferedReader
    getReader(String path) {

	return(getReader(path, getDefaultEncoding()));
    }


    public static BufferedReader
    getReader(String path, String encoding) {

	BufferedReader  reader = null;

	try {
	    switch (guessStreamType(path)) {
		case FILE:
		    reader = new BufferedReader(
			new InputStreamReader(new FileInputStream(toLocalPath(path)), encoding)
		    );
		    break;

		case URL:
		    reader = new BufferedReader(
			new InputStreamReader(getInputStream(new URL(path)), encoding)
		    );
		    break;
	    }
	}
	catch(IOException e) {
	    VM.caughtException(e);
	}

	return(reader);
    }


    public static String
    getResource(String path) {

	ClassLoader  loader;
	String       resource = null;
	URL          url;

	if (path != null) {
	    loader = YoixMisc.class.getClassLoader();
	    if ((url = loader.getResource(path)) != null)
		resource = url.toExternalForm();
	}

	return(resource);
    }


    public static URLConnection
    getURLConnection(URL url) 

	throws IOException

    {

	//
	// Unfortunately Java's URL class is final so we can't extend it,
	// but when we handle https we need to make sure we've initialized
	// the YoixTrustPolicy class. Anyway, this is now the recommended
	// way to open URLs because it tries to guarantee the Yoix https
	// setup has completed.
	//

	return(YoixTrustPolicy.setupURL(url).openConnection());
    }


    public static URLConnection
    getURLConnection(URL url, YoixObject obj) 

	throws IOException

    {

	InetSocketAddress  address;
	URLConnection      connection = null;
	String             text;
	String             host;
	Proxy              proxy;
	int                port;
	int                index;

	//
	// Normally called to open a connection that uses a proxy server,
	// but as usual we have to initialized the YoixTrustPolicy class.
	// Currently returns null if we can't open the connection using
	// a proxy, which means the caller has to decide how to proceed.
	// 

	if (obj != null && obj.notNull()) {
	    if (obj.isString()) {
		text = obj.stringValue();
		if ((index = text.indexOf(':')) >= 0) {
		    host = text.substring(0, index);
		    port = YoixMake.javaInt(text.substring(index+1), 80);
		    if (host.length() > 0)
			address = new InetSocketAddress(host, port);
		    else address = new InetSocketAddress(port);
		    proxy = new Proxy(Proxy.Type.HTTP, address);
		    connection = YoixTrustPolicy.setupURL(url).openConnection(proxy);
		} else connection = null;
	    } else {
		//
		// Eventually accept a dictionary with host and port set.
		//
		connection = null;
	    }
	} else connection = getURLConnection(url);

	return(connection);
    }


    public static String
    getVersionInfo(boolean full) {

	String  info;

	if (full)
	    info = "Yoix Version \"" + YOIXVERSION + "\"" + " [" + YOIXCREATED + "]";
	else info = YOIXVERSION;
	return(info);
    }


    public static String
    getVMEncoding() {

	return(VM.getVMEncoding().stringValue());
    }


    public static int
    guessStreamType(String path) {

	int  type = FILE;

	try {
	    new URL(path);
	    type = URL;
	}
	catch(IOException e) {
	    VM.caughtException(e);
	}

	return(type);
    }


    public static String
    hexBytesToString(byte bytes[]) {

	StringBuffer  sb;
	String        str;
	byte          bite;
	int           len;
	int           m;
	int           n;

	if (bytes != null) {
	    len = bytes.length;
	    sb = new StringBuffer(2*len);
	    for (m = 0; m < len; m++) {
		sb.append(HEXCHARS[0x0F & (bytes[m]>>4)]);
		sb.append(HEXCHARS[0x0F & bytes[m]]);
	    }
	    str = sb.toString();
	} else str = null;

	return(str);
    }


    public static String
    hexFromAscii(String str) {

	String  value = null;
	byte    source[];
	byte    bytes[];
	int     length;
	int     ch;
	int     m;
	int     n;

	if (str != null) {
	    source = YoixMake.javaByteArray(str);
	    length = source.length;
	    bytes = new byte[2*length];
	    for (n = 0, m = 0; n < length; n++) {
		ch = source[n];
		bytes[m++] = HEXNIBBLES[(ch>>4) & 0xF];
		bytes[m++] = HEXNIBBLES[ch & 0xF];
	    }
	    value = YoixMake.javaString(bytes, 0, m);
	}
	return(value);
    }


    public static String
    hexFromAscii(char source[]) {

	String  value = null;
	byte    bytes[];
	char    ch;
	int     length;
	int     m;
	int     n;

	//
	// Be very careful using this method - it implicitly assumes
	// ISO8859-1 encoding of characters, so it's definitely not
	// general!!!
	//

	if (source != null) {
	    length = source.length;
	    bytes = new byte[2*length];
	    for (n = 0, m = 0; n < length; n++) {
		ch = source[n];
		bytes[m++] = HEXNIBBLES[(ch>>4) & 0xF];
		bytes[m++] = HEXNIBBLES[ch & 0xF];
	    }
	    value = YoixMake.javaString(bytes, 0, m);
	}
	return(value);
    }


    public static byte[]
    hexStringToBytes(String bytes) {

	byte  bytearray[];
	char  chararray[];
	int   m;
	int   n;

	if (bytes != null && bytes.length()%2 == 0) {
	    chararray = bytes.toCharArray();
	    bytearray = new byte[chararray.length/2];
	    for (m = 0, n = 0; n < chararray.length; m++, n += 2) {
		bytearray[m] = (byte)((0x0F & HEXDIGITS[(int)chararray[n]])<<4);
		bytearray[m] |= (byte)(0x0F & HEXDIGITS[(int)chararray[n+1]]);
	    }
	} else bytearray = null;

	return(bytearray);
    }


    public static String
    hexToAscii(String str) {

	return(hexToAscii(str, false));
    }


    public static String
    hexToAscii(String str, boolean tonull) {

	String  value = null;
	byte    bytes[];
	char    ch;
	int     length;
	int     digit;
	int     m;
	int     n;

	if (str != null) {
	    if ((length = str.length()) > 0) {
		bytes = new byte[(length + 1)/2];
		for (m = 0, n = 0; n < length; n++) {
		    if ((ch = str.charAt(n)) < HEXDIGITS.length) {
			if ((digit = HEXDIGITS[ch]) >= 0) {
			    if (m%2 == 0)
				bytes[m/2] = (byte)(digit << 4);
			    else bytes[m/2] |= (byte)digit;
			    m++;
			} else if (ch == '\0' && tonull)
			    break;
		    }
		}
		value = YoixMake.javaString(bytes, 0, (m + 1)/2);
	    } else value = "";
	}
	return(value);
    }


    public static String
    hexToAscii(char source[], boolean tonull) {

	String  value = null;
	byte    bytes[];
	char    ch;
	int     length;
	int     digit;
	int     m;
	int     n;

	if (source != null) {
	    if ((length = source.length) > 0) {
		bytes = new byte[(length + 1)/2];
		for (m = 0, n = 0; n < length; n++) {
		    if ((ch = source[n]) < HEXDIGITS.length) {
			if ((digit = HEXDIGITS[ch]) >= 0) {
			    if (m%2 == 0)
				bytes[m/2] = (byte)(digit << 4);
			    else bytes[m/2] |= (byte)digit;
			    m++;
			} else if (ch == '\0' && tonull)
			    break;
		    }
		}
		value = YoixMake.javaString(bytes, 0, (m + 1)/2);
	    }
	}
	return(value);
    }


    public static String
    htmlFromAscii(String str) {

	return(htmlFromAscii(str, null));
    }


    public static String
    htmlFromAscii(String str, String extra) {

	StringBuffer  buf;
	String        value = null;
	char          chars[];
	int           length;
	int           ch;
	int           n;
	int           m;

	if (str != null) {
	    chars = str.toCharArray();
	    length = chars.length;
	    buf = new StringBuffer(length);
	    synchronized(buf) {
		for (n = 0, m = 0; n < length; n++) {
		    if ((ch = chars[n]&0xFFFF) >= 128 || HTML_CLEARCHARS[ch] == false || (extra != null && extra.indexOf(ch) >= 0)) {
			buf.append('&');
			buf.append('#');
			buf.append(ch);
			buf.append(';');
		    } else buf.append((char)ch);
		}
		value = buf.toString();
	    }
	}
	return(value);
    }


    public static String
    htmlToAscii(String str) {

	StringTokenizer  st;
	StringBuffer     buf;
	boolean          inchar;
	String           tok;
	String           nexttok;
	String           value;
	int              len;

	if (str != null && str.length() > 3) {
	    buf = new StringBuffer(str.length());
	    st = new StringTokenizer(str, "&;", true);
	    inchar = false;
	    synchronized(buf) {
		while (st.hasMoreTokens()) {
		    tok = st.nextToken();
		    if ("&".equals(tok)) {
			if (st.hasMoreTokens()) {
			    tok = st.nextToken();
			    nexttok = null;
			    while(true) {
				if (
				    st.hasMoreTokens()
				    &&
				    ";".equals(nexttok = st.nextToken())
				    &&
				    (len = tok.length()) >= MINIMUM_HTMLINFO
				    &&
				    len <= MAXIMUM_HTMLINFO
				    &&
				    (value = htmlChar(tok)) != null
				) {
				    buf.append(value);
				} else {
				    buf.append("&");
				    if (nexttok == null) {
					buf.append(tok);
				    } else if ("&".equals(tok)) {
					tok = nexttok;
					nexttok = null;
					continue;
				    } else {
					buf.append(tok);
					if ("&".equals(nexttok)) {
					    if (st.hasMoreTokens()) {
						tok = st.nextToken();
						nexttok = null;
						continue;
					    } else buf.append("&");
					} else buf.append(nexttok);
				    }
				}
				break;
			    }
			}
		    } else buf.append(tok);
		}
		str = buf.toString();
	    }
	}
	return(str);
    }


    public static boolean
    isFile(String filename) {

	boolean  result = false;

	if (filename != null) {
	    try {
		result = (new File(filename).isFile());
	    }
	    catch(RuntimeException e) {}
	}

	return(result);
    }


    public static boolean
    isGzipFile(InputStream stream) {

	boolean  result = false;
	byte     magic[];

	//
	// Returns true if we can transparently read two bytes from stream
	// and those bytes match the ones used to mark GZIP file.
	//

	if (stream != null) {
	    if (stream.markSupported()) {
		magic = new byte[2];
		try {
		    stream.mark(magic.length);
		    stream.read(magic);
		    stream.reset();
		    result = ((magic[0]&0xFF) == 0x1F && (magic[1]&0xFF) == 0x8B);
		}
		catch(Exception e) {}
	    }
	}

	return(result);
    }


    public static boolean
    isZipFile(InputStream stream) {

	boolean  result = false;
	byte     magic[];

	//
	// Returns true if we can transparently read four bytes from stream 
	// and those bytes match the ones used to mark ZIP archive.
	//

	if (stream != null) {
	    if (stream.markSupported()) {
		magic = new byte[4];
		try {
		    stream.mark(magic.length);
		    stream.read(magic);
		    stream.reset();
		    result = ((magic[0]&0xFF) == 0x50 && (magic[1]&0xFF) == 0x4B && (magic[2]&0xFF) == 0x03 && (magic[3]&0xFF) == 0x04);
		}
		catch(Exception e) {}
	    }
	}

	return(result);
    }


    public static Color
    javaColorLookup(String name) {

	Color  color;

	if ((color = YoixRegistryColor.javaColor(name)) == null) {
	    YoixRegistryColor.addColor();
	    color = YoixRegistryColor.javaColor(name);
	}
	return(color);
    }


    public static String
    javaReverseColorLookup(Color color) {

	return(color != null ? (String)reversecolor.get(color) : null);
    }


    public static String
    javaTrace() {

	return(javaTrace(new Throwable(), -1));
    }


    public static String
    javaTrace(int lines) {

	return(javaTrace(new Throwable(), lines));
    }


    public static String
    javaTrace(Throwable t) {

	return(javaTrace(t, -1));
    }


    public static String
    javaTrace(Throwable t, int lines) {

	StringWriter  writer;
	String        trace;
	int           end = -1;
	int           index;

	writer = new StringWriter();
	t.printStackTrace(new PrintWriter(writer));
	trace = writer.toString();
	if ((index = trace.indexOf(PREFIX_JAVASTACKTRACE)) != -1 && index < trace.indexOf(NL))
	    trace = trace.substring(index);
	while (lines-- > 0) {
	    if ((index = trace.indexOf(NL, end+1)) > end)
		end = index;
	    else break;
	}
	return(end > 0 ? trace.substring(0, end) : trace);
    }


    public static int
    jvmCompareTo(String version) {

	return(getJVMVersion().compareTo(version));
    }


    public static boolean
    match(YoixObject obj, String arg, int flags) {

	return(obj.isParseTree()
	    ? PatternInterpreter.match((SimpleNode)obj.getManagedObject(), arg, flags)
	    : false
	);
    }


    public static synchronized int
    nextID() {

	return(nextid++);
    }


    public static ArrayList
    split(String source, String delim) {

	return(split(source, delim, -1, new ArrayList()));
    }


    public static ArrayList
    split(String source, YoixObject array) {

	return(split(source, array, new ArrayList()));
    }


    public static ArrayList
    split(String source, String delim, int last, ArrayList list) {

	int  length;
	int  skip;
	int  start;
	int  stop;
	int  end;
	int  next;

	//
	// This is the original string-splitting method that's also the
	// way the strsplit() builtin usually behaves.
	//

	if (source != null) {
	    if (delim != null) {
		skip = delim.length();
		length = source.length();
		stop = length + skip;
		for (start = end = next = 0; start < stop; start = end + skip) {
		    if (last < 0 || next < last) {
			if (skip > 0) {
			    if ((end = source.indexOf(delim, start)) < start)
				end = length;
			} else end = start + 1;
		    } else end = length;
		    list.add(next++, source.substring(start, end));
		}
	    } else list.add(source);
	} else list = null;

	return(list);
    }


    public static ArrayList
    split(String source, YoixObject separators, ArrayList list) {

	YoixObject  dict;
	String      left;
	String      right;
	String      skip;
	char        ch;
	int         length;
	int         incr;
	int         start;
	int         end;
	int         next;
	int         index;
	int         n;

	//
	// A new string-splitting method that's controlled by an array of
	// one or more dictionaries. Each dictionary can set fields named
	// "skip", "left", and "right" that control how individual fields
	// in the source string are identified.
	//
	// NOTE - performance is important, so be very careful if you add
	// capabilities.
	//

	if (source != null && list != null) {
	    if (separators != null) {
		if (separators.isDictionary()) {
		    skip = separators.getString(N_SKIP);
		    left = separators.getString(N_LEFT);
		    right = separators.getString(N_RIGHT);
		    separators = null;
		    index = 0;
		} else {
		    left = null;
		    right = null;
		    skip = null;
		    index = separators.offset();
		}
		length = source.length();
		for (start = 0; start < length; ) {
		    if (separators != null) {
			if ((dict = separators.getObject(index++)) != null && dict.isDictionary()) {
			    skip = dict.getString(N_SKIP, skip);
			    left = dict.getString(N_LEFT, left);
			    right = dict.getString(N_RIGHT, right);
			}
		    }
		    if (skip != null) {
			switch (skip.length()) {
			    case 0:
				break;

			    case 1:
				ch = skip.charAt(0);
				while (start < length && source.charAt(start) == ch)
				    start++;
				break;

			    default:
				while (start < length) {
				    ch = source.charAt(start);
				    if (skip.indexOf(ch) >= 0)
					start++;
				    else break;
				}
				break;
			}
		    }
		    if (start < length) {
			if (left != null && left.length() > 0) {
			    if (source.indexOf(left, start) == start)
				start += left.length();
			}
			if (start < length) {
			    if (right != null && right.length() > 0) {
				incr = right.length();
				if ((end = source.indexOf(right, start)) < start)
				    end = length;
			    } else {
				incr = 0;
				end = start + 1;
			    }
			    list.add(source.substring(start, end));
			    start = end + incr;
			}
		    }
		}
	    } else list.add(source);
	}

	return(list);
    }


    public static double
    toDegrees(double radians) {

	return(radians*180.0/Math.PI);
    }


    public static String
    toLocalPath(String path) {

	return(path != null ? path.replace('/', File.separatorChar) : null);
    }


    public static double
    toRadians(double degrees) {

	return(degrees*Math.PI/180.0);
    }


    public static String
    toRealPath(String path) {

	String realpath = null;

	if (path != null) {
	    try {
		realpath = (new File(path)).getCanonicalPath();
		if (File.separatorChar != '/')
		    realpath = realpath.replace(File.separatorChar, '/');
	    }
	    catch(IOException e) {
		VM.caughtException(e);
	    }
	}
	return(realpath);
    }


    public static String
    toYoixPath(String path) {

	boolean  nondot = false;
	boolean  unc = false;
	char     orig[] = null;
	char     array[] = null;
	int      dots = 0;
	int      i;
	int      j;

	if (path != null && (orig = path.toCharArray()).length > 0) {
	    if (orig[0] == File.separatorChar || orig[0] == '/') {
		array = orig;
	    } else if (ISWIN && orig.length > 1 && orig[1] == ':') {
		array = new char[orig.length + 1];
		array[0] = '/';
		array[1] = orig[0];
		array[2] = '/';
		System.arraycopy(orig, 2, array, 3, orig.length - 2);
	    } else {
		if (ISWIN && PWD.length > 1 && PWD[1] == ':') {
		    array = new char[PWD.length + orig.length + 2];
		    array[0] = '/';
		    array[1] = PWD[0];
		    array[2] = '/';
		    System.arraycopy(PWD, 2, array, 3, PWD.length - 2);
		    array[PWD.length + 1] = '/';
		    System.arraycopy(orig, 0, array, PWD.length + 2, orig.length);
		} else {
		    array = new char[PWD.length + orig.length + 1];
		    System.arraycopy(PWD, 0, array, 0, PWD.length);
		    array[PWD.length] = '/';
		    System.arraycopy(orig, 0, array, PWD.length + 1, orig.length);
		}
	    }

	    //
	    // Java's file classes recognize UNC pathnames, at least on Windows,
	    // but we change the leading slash or backslash pair to a single '/',
	    // so those Java classes won't recognize the path. This was added on
	    // 9/21/11 as an attempt to remember whether the original path looked
	    // like it was a UNC path. We check again at the end after most of the
	    // work is complete, but only if unc is true.
	    //

	    if (array.length >= 5) {
		if (array[0] == File.separatorChar || array[0] == '/') {
		    if (array[1] == File.separatorChar || array[1] == '/') {
			if (array[2] != File.separatorChar && array[2] != '/') {
			    if (array[2] != '.' || array[3] != '.') {
				//
				// Looks like a UNC path - we'll check the modified
				// path and really decide then.
				//
				unc = true;
			    }
			}
		    }
		}
	    }

	    // at this point we can assume that array[0] == '/' or
	    // array[0] == File.separatorChar

	    for (i = 0, j = 0; i < array.length; i++) {
		if (array[i] == File.separatorChar || array[i] == '/') {
		    if (dots > 0) {
			if (dots == 1) {
			    dots = 0;
			} else if (dots == 2) {
			    dots = 0;
			    if (j > 0) {
				do {
				    j--;
				} while (j > 0 && array[j - 1] != '/');
			    }
			} else {
			    do {
				array[j++] = '.';
			    } while (--dots > 0);
			}
		    }
		    if (j > 0 && array[j - 1] != '/')
			array[j++] = '/';
		    else if (j == 0)
			array[j++] = '/';
		    nondot = false;
		} else if (!nondot && array[i] == '.') {
		    dots++;
		} else if (dots > 0) {
		    do {
			array[j++] = '.';
		    } while (--dots > 0);
		    array[j++] = array[i];
		    nondot = true;
		} else {
		    array[j++] = array[i];
		    nondot = true;
		}
	    }

	    if (dots > 0) {
		if (dots == 1) {
		    dots = 0;
		} else if (dots == 2) {
		    dots = 0;
		    if (j > 0) {
			do {
			    j--;
			} while (j > 0 && array[j - 1] != '/');
		    }
		} else {
		    do {
			array[j++] = '.';
		    } while (--dots > 0);
		}
	    }

	    // removing trailing slash, if any (unless there is only a slash)
	    while (j > 1 && array[j - 1] == '/')
		j--;

	    if (ISWIN && array.length > 2 && array[0] == '/' && array[2] == '/' && !unc) {
		// assumes a single character directory at the lead is a
		// disk; normally a reasonable assumption, but can
		// construct examples where it is wrong - in those cases,
		// full-path could be supplied to fix things - i.e., supply
		// /c/c/file or c:/c/file instead of just /c/file
		//
		// Note: we skip the isDirectory check under the isApplet situation
		//       as it requires a read of the directory, which throws off
		//       excpetion cases like: -S allow:tmpfile --applet
		//       making the allow useless as it also would need a read of the
		//       top-level directory (e.g., C:\).
		//       Note: that -Sdeny:file -Sallow:tmpfile without --applet will
		//             not work, so best to use --applet and then allow what
		//             you want.
		//
		if (VM.isApplet() || (new File(array[1] + ":" + File.separatorChar)).isDirectory()) {
		    array[0] = array[1];
		    array[1] = ':';
		}
	    }
	    path = new String(array, 0, j);

	    if (unc) {
		if (path.indexOf('/') < path.lastIndexOf('/'))
		    path = "/" + path;
	    }
	}

	return(path);
    }


    public static String
    toYoixURL(String path) {

	boolean  nondot = false;
	char     array[] = null;
	int      slash = 0;
	int      dots = 0;
	int      i;
	int      j;

	if (path != null && (j = (array = path.toCharArray()).length) > 0) {
	    // first pass, fix up any backslash perversions and
	    // find last of initial slashes
	    for (i=0; i < array.length && array[i] != '?'; i++) {
		if (array[i] == '\\') {
		    array[i] = '/';
		    if (slash <= 0)
			slash = -1 - i;
		} else if (slash <= 0 && array[i] == '/')
		    slash = -1 - i;
		else if (slash < 0)
		    slash = -slash;
	    }

	    if (slash > 0) {
		slash--;

		for (i = slash, j = slash; i < array.length && array[i] != '?'; i++) {
		    if (array[i] == '/') {
			if (dots > 0) {
			    if (dots == 1) {
				dots = 0;
			    } else if (dots == 2) {
				dots = 0;
				if (j > slash) {
				    do {
					j--;
				    } while (j > slash && array[j - 1] != '/');
				}
			    } else {
				do {
				    array[j++] = '.';
				} while (--dots > 0);
			    }
			}
			if (j > slash && array[j - 1] != '/')
			    array[j++] = '/';
			else if (j == slash)
			    array[j++] = '/';
			nondot = false;
		    } else if (!nondot && array[i] == '.') {
			dots++;
		    } else if (dots > 0) {
			do {
			    array[j++] = '.';
			} while (--dots > 0);
			array[j++] = array[i];
			nondot = true;
		    } else {
			array[j++] = array[i];
			nondot = true;
		    }
		}
		if (dots > 0) {
		    if (dots == 1) {
			dots = 0;
		    } else if (dots == 2) {
			dots = 0;
			if (j > slash) {
			    do {
				j--;
			    } while (j > slash && array[j - 1] != '/');
			}
		    } else {
			do {
			    array[j++] = '.';
			} while (--dots > 0);
		    }
		}

		// removing trailing slash, if any (unless there is only a slash)
		while (j > slash+1 && array[j - 1] == '/')
		    j--;

		if (i < array.length && array[i] == '?') {
		    for (; i < array.length; i++)
			array[j++] = array[i];
		}

	    }
	    path = new String(array, 0, j);
	}
	return(path);
    }


    public static String
    trim(String str, int delim) {

	return(trim(str, delim >= 0 ? String.valueOf((char)delim) : null));
    }


    public static String
    trim(String str, int left, int right) {

	return(trim(str, left >= 0 ? String.valueOf((char)left) : null, right >= 0 ? String.valueOf((char)right) : null));
    }


    public static String
    trim(String str, String delim) {

	return(trim(str, delim, delim));
    }


    public static String
    trim(String str, String left, String right) {

	int  length;
	int  start;
	int  end;

	if (str != null) {
	    if ((length = str.length()) > 0) {
		start = 0;
		if (left != null) {
		    for (; start < length; start++) {
			if (left.indexOf(str.charAt(start)) == -1)
			    break;
		    }
		}

		end = length - 1;
		if (right != null) {
		    for (; end >= start; end--) {
			if (right.indexOf(str.charAt(end)) == -1)
			    break;
		    }
		} 

		if (start != 0 || end != length - 1)
		    str = str.substring(start, end + 1);
	    }
	}
	return(str);
    }


    public static String
    trimWhiteSpace(String str) {

	return(trimWhiteSpace(str, true, true));
    }


    public static String
    trimWhiteSpace(String str, boolean left, boolean right) {

	//
	// Java's trim() removes characters less than or equal to
	// 0x20 (space), so it's not quite the same as our version
	// of trim().
	//

	if (str != null) {
	    if (left || right) {
		if (left == false) {
		    str = ("X" + str).trim();
		    if (str.length() > 1)
			str = str.substring(1);
		    else str = "";
		} else if (right == false) {
		    str = (str + "X").trim();
		    if (str.length() > 1)
			str = str.substring(0, str.length() - 1);
		    else str = "";
		} else str = str.trim();
	    }
	}
	return(str);
    }


    public static String
    urlFromAscii(String str, boolean ietf) {

	boolean  clearchars[];
	String   value = null;
	byte     bytes[];
	byte     source[];
	int      length;
	int      ch;
	int      n;
	int      m;

	if (str != null) {
	    clearchars = ietf ? IETF_CLEARCHARS : MIME_CLEARCHARS;
	    source = YoixMake.javaByteArray(str);
	    length = source.length;
	    bytes = new byte[3*length];
	    for (n = 0, m = 0; n < length; n++) {
		if ((ch = source[n]&0xFF) >= 128 || clearchars[ch] == false) {
		    bytes[m++] = '%';
		    bytes[m++] = HEXNIBBLES[(ch>>4) & 0xF];
		    bytes[m++] = HEXNIBBLES[ch & 0xF];
		} else bytes[m++] = (byte)((ietf || ch != ' ') ? ch : '+');
	    }
	    value = YoixMake.javaString(bytes, 0, m);
	}
	return(value);
    }


    public static String
    urlToAscii(String str, boolean ietf) {

	//
	// Currently unused, but if that changes you may want to
	// take another look because str.toCharArray() essentially
	// creates another (temporary) copy of the string.
	//

	return(str != null ? urlToAscii(str.toCharArray(), ietf, false) : null);
    }


    public static String
    urlToAscii(char source[], boolean ietf) {

	return(urlToAscii(source, ietf, false));
    }


    public static String
    urlToAscii(char source[], boolean ietf, boolean tonull) {

	String  value = null;
	byte    bytes[];
	char    ch;
	int     length;
	int     digit;
	int     m;
	int     n;

	if (source != null) {
	    if ((length = source.length) > 0) {
		try {
		    bytes = new byte[length];
		    for (m = 0, n = 0; n < length; n++) {
			if ((ch = source[n]) <= 0x7F) {
			    if (ch == '%') {
				if ((digit = HEXDIGITS[source[++n]]) >= 0) {
				    bytes[m] = (byte)(digit << 4);
				    if ((digit = HEXDIGITS[source[++n]]) >= 0)
					bytes[m++] |= (byte)digit;
				   else throw(new NumberFormatException(null));
				} else throw(new NumberFormatException(null));
			    } else if (ch != '\0')
				bytes[m++] = (byte)((ietf || ch != '+') ? ch : ' ');
			    else if (tonull == false)
				throw(new RuntimeException());
			    else break;
			} else throw(new RuntimeException());
		    }
		    value = YoixMake.javaString(bytes, 0, m);
		}
		catch(RuntimeException e) {}
	    } else value = "";
	}
	return(value);
    }


    public static String
    urlToUnicode(char source[], boolean ietf, boolean tonull) {

	String  value = null;
	char    chars[];
	char    ch;
	int     d1;
	int     d2;
	int     length;
	int     m;
	int     n;

	//
	// A modification of urlToAscii() that's more forgiving and accepts
	// source characters that aren't in the US-ASCII character set. This
	// this doesn't agree with RFC 2396, which talks about URIs, but it's
	// an extension that may sometimes be convenient when we want to deal
	// with a text string that looks like it's been partially encoded.
	//
	// NOTE - this might not be used right now, but it probably shouldn't
	// be deleted for a while.
	//

	if (source != null) {
	    if ((length = source.length) > 0) {
		try {
		    chars = new char[length];
		    for (m = 0, n = 0; n < length; n++) {
			if ((ch = source[n]) == '%') {
			    if (n < length - 2) {
				if (source[n+1] < HEXDIGITS.length && source[n+2] < HEXDIGITS.length) {
				    if ((d1 = HEXDIGITS[source[n+1]]) >= 0 && (d2 = HEXDIGITS[source[n+2]]) >= 0) {
					chars[m++] = (char)((d1 << 4) | d2);
					n += 2;
				    } else chars[m++] = ch;
				} else chars[m++] = ch;
			    } else chars[m++] = ch;
			} else if (ch != '\0')
			    chars[m++] = (char)((ietf || ch != '+') ? ch : ' ');
			else if (tonull == false)
			    throw(new RuntimeException());
			else break;
		    }
		    value = new String(chars, 0, m);
		}
		catch(RuntimeException e) {}
	    } else value = "";
	}
	return(value);
    }

    ///////////////////////////////////
    //
    // YoixMisc Methods
    //
    ///////////////////////////////////

    static YoixClassLoader
    classLoader(String path) {

	return(classLoader(path, null));
    }


    static YoixClassLoader
    classLoader(String path, String delims) {

	StringTokenizer  st;
	YoixClassLoader  loader = null;
	ArrayList        alist;
	File             file;
	String           token;
	URL              url;

	if (path != null) {
	    if (delims == null || delims.length() == 0) {
		// since default PATHSEP on *nix systems is ":" and
		// that conflicts with http://..., then when no delim is
		// supplied, first try path as a single value and only if
		// that fails try parising with PATHSEP
		try {
		    file = new File(path);
		    if (file.isFile() || file.isDirectory())
			url = file.toURL();
		    else url = new URL(path);
		    loader = new YoixClassLoader(new URL[] { url });
		}
		catch(MalformedURLException e) {
		    delims = PATHSEP;
		}
	    }

	    if (loader == null) {
		st = new StringTokenizer(path, delims, false);
		alist = new ArrayList();
		while (st.hasMoreTokens()) {
		    token = st.nextToken();
		    if (token.length() > 0) {
			try {
			    file = new File(token);
			    if (file.isFile() || file.isDirectory())
				url = file.toURL();
			    else url = new URL(token);
			    alist.add(url);
			}
			catch(MalformedURLException e) {}
		    }
		}
		if (alist.size() > 0)
		    loader = new YoixClassLoader((URL[])alist.toArray(new URL[0]));
		else VM.recordException(new RuntimeException("path contained no valid entries using path separator '" + delims + "'"));
	    }
	}

	return(loader);
    }


    static String
    getCodeBase() {

	return(getCodeBase(YoixConstants.class));
    }


    static String
    getCodeBase(Class source) {

	CodeSource  codesource;
	String      codebase = null;
	URL         location;

	if ((codesource = getCodeSource(source)) != null) {
	    if ((location = codesource.getLocation()) != null)
		codebase = location.toExternalForm();
	}
	return(codebase);
    }


    static CodeSource
    getCodeSource() {

	return(getCodeSource(YoixConstants.class));
    }


    static CodeSource
    getCodeSource(Class source) {

	CodeSource  codesource = null;

	//
	// Catching SecurityException prevents problems when we're running as
	// an untrusted application under javaws.
	//

	try {
	    codesource = source.getProtectionDomain().getCodeSource();
	}
	catch(SecurityException e) {}
	return(codesource);
    }


    static GraphicsConfiguration
    getDefaultGraphicsConfiguration() {

	GraphicsConfiguration  gc = null;
	GraphicsDevice         gd;

	if ((gd = getDefaultScreenDevice()) != null)
	    gc = gd.getDefaultConfiguration();
	return(gc);
    }


    static GraphicsDevice
    getDefaultScreenDevice() {

	GraphicsDevice  gd = null;

	try {
	    gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	}
	catch(Exception e) {}
	return(gd);
    }


    static Color
    getForegroundColor(Color bkgd, Color dflt) {

	double  d0;
	double  d1;
	double  d2;
	double  val;
	double  sat1;
	double  bright1;
	double  sat2;
	double  bright2;
	int     red1;
	int     green1;
	int     blue1;
	int     red2;
	int     green2;
	int     blue2;
	int     mx;
	int     mn;

	red1 = bkgd.getRed();
	green1 = bkgd.getGreen();
	blue1 = bkgd.getBlue();

	red2 = dflt.getRed();
	green2 = dflt.getGreen();
	blue2 = dflt.getBlue();

	if (red1 > green1) {
	    mx = red1;
	    mn = green1;
	} else {
	    mx = green1;
	    mn = red1;
	}
	if (blue1 > mx)
	    mx = blue1;
	else if (blue1 < mn)
	    mn = blue1;

	bright1 = ((double)mx)/255.0;
	sat1 = (mx != 0) ? ((double)(mx - mn))/((double)mx) : 0;

	if (red2 > green2) {
	    mx = red2;
	    mn = green2;
	} else {
	    mx = green2;
	    mn = red2;
	}
	if (blue2 > mx)
	    mx = blue2;
	else if (blue2 < mn)
	    mn = blue2;

	bright2 = ((double)mx)/255.0;
	sat2 = (mx != 0) ? ((double)(mx - mn))/((double)mx) : 0;

	d0 = bright1 - bright2;
	d1 = sat1 - sat2;
	val = Math.sqrt((d0*d0+d1*d1)/2.0);

	if (val < 0.2) {
	    if (red1 < 128)
		red1 += 128;
	    else red1 -= 128;

	    if (green1 < 128)
		green1 += 128;
	    else green1 -= 128;

	    if (blue1 < 128)
		blue1 += 128;
	    else blue1 -= 128;

	    dflt = new Color(red1, green1, blue1);
	}

	return(dflt);
    }


    static String
    getLocalJarPath() {

	return(getLocalJarPath(YoixConstants.class));
    }


    static String
    getLocalJarPath(Class source) {

	CodeSource  codesource;
	String      path = null;
	File        file;
	URL         location;

	//
	// Returns null if the source class wasn't loaded from a jar file
	// that exits on the the local host.
	//

	if ((codesource = getCodeSource(source)) != null) {
	    if ((location = codesource.getLocation()) != null) {
		if (location.getProtocol().equals("file")) {
		    file = new File(location.getPath());
		    if (file.isFile() && file.exists()) {
			path = file.getAbsolutePath();
		    } else {
			file = new File(urlToAscii(location.getPath(), false));
			if (file.isFile() && file.exists())
			    path = file.getAbsolutePath();
		    }
		}
	    }
	}
	return(path);
    }


    static String
    getPackageName() {

	//
	// You may be tempted to write this as,
	//
	//	return(YoixConstants.class.getPackage().getName());
	//
	// but for some reason the Yoix installer didn't approve. We may
	// investigate, but it's definitely not a high priority.
	//

	return(getPackageName(YoixConstants.class.getName()));
    }


    static String
    getPackageName(String name) {

	int  index = name.lastIndexOf('.');

	return(index > 0 ? name.substring(0, index) : ".");
    }


    static BufferedReader
    getParserReader(String path) {

	return(getReader(path, getParserEncoding()));
    }


    static Reader
    getParserReader(InputStream stream) {

	StringBuffer  sbuf;
	ZipEntry      ze;
	Reader        reader;
	String        str;
	byte          buf[];
	int           count;

	if (stream instanceof ZipInputStream) {
	    str = "";
	    try {
		ze = ((ZipInputStream)stream).getNextEntry();
		buf = new byte[BUFSIZ];
		sbuf = new StringBuffer();
		synchronized(sbuf) {
		    while ((count = stream.read(buf)) >= 0)
			sbuf.append(YoixMake.javaString(buf, 0, count));
		    if (sbuf.length() > 0)
			str = sbuf.toString();
		}
	    }
	    catch(IOException e) {
		VM.caughtException(e);
	    }
	    reader = new StringReader(str);
	} else {
	    try {
		reader = new InputStreamReader(stream, YoixConverter.getSupportedEncoding(getParserEncoding(), getVMEncoding()));
	    }
	    catch(Exception e) {
		VM.caughtException(e);
		VM.abort(INTERNALERROR); // should never get here
		reader = null; // for compiler
	    }
	}
	return(reader);
    }


    static String
    getProperty(String name, String value) {

	try {
	    value = System.getProperty(name);
	}
	catch(SecurityException e) {}
	return(value);
    }


    static GraphicsDevice
    getScreenDeviceByID(String id) {

	GraphicsDevice  gds[];
	GraphicsDevice  gd = null;
	int             n;

	if (id != null) {
	    if ((gds = getScreenDevices()) != null) {
		for (n = 0; n < gds.length; n++) {
		    if (id.equals(gds[n].getIDstring())) {
			gd = gds[n];
			break;
		    }
		}
	    }
	}

	return(gd);
    }


    static int
    getScreenDeviceCount() {

	int  count = 0;

	try {
	    count = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;
	}
	catch(Exception e) {}

	return(count);
    }


    static GraphicsDevice[]
    getScreenDevices() {

	GraphicsDevice  gds[] = null;
	GraphicsDevice  gd;
	int             n;

	//
	// Decided to impose a little order on the array that we return and
	// guarantee that the first entry is the default screen. It's only
	// for convenience and might be the way it happens anyway, but we
	// didn't find any information about the array that's returned by
	// getScreenDevices(). Small chance we'll eventually want to sort
	// the remaining elements, perhaps by id string or their bounds.
	//

	try {
	    gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	    gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
	    if (gds[0] != gd) {
		for (n = 1; n < gds.length; n++) {
		    if (gd == gds[n]) {
			gds[n] = gds[0];
			gds[0] = gd;
			break;
		    }
		}
	    }
	}
	catch(Exception e) {}

	return(gds);
    }


    static Reader
    getStreamReader(InputStream stream) {

	StringBuffer  sbuf;
	ZipEntry      ze;
	Reader        reader;
	String        str;
	byte          buf[];
        int           count;

	//
	// Appears to be unused after getReader was split into getParserReader
	// and getStreamReader.
	//

	if (stream instanceof ZipInputStream) {
	    str = "";
	    try {
		ze = ((ZipInputStream)stream).getNextEntry();
		buf = new byte[BUFSIZ];
		sbuf = new StringBuffer();
		synchronized(sbuf) {
		    while ((count = stream.read(buf)) >= 0)
			sbuf.append(YoixMake.javaString(buf, 0, count));
		    if (sbuf.length() > 0)
			str = sbuf.toString();
		}
	    }
	    catch(IOException e) {
		VM.caughtException(e);
	    }
	    reader = new StringReader(str);
	} else {
	    try {
		reader = new InputStreamReader(stream, YoixConverter.getSupportedEncoding(getDefaultEncoding(), YoixConverter.getISO88591Encoding()));
	    }
	    catch(Exception e) {
		VM.caughtException(e);
		VM.abort(INTERNALERROR); // should never get here
		reader = null; // for compiler
	    }
	}

	return(reader);
    }


    static String
    getTempDirectory(boolean trim) {

	String  path;

	if ((path = getProperty("java.io.tmpdir", null)) != null) {
	    if (trim)
		path = trim(path, "", File.separator);
	} else path = (ISUNIX ? "/tmp" : "/temp") + (trim ? "" : "/");

	return(path);
    }


    static int
    hexDigit(char ch, int value) {

	if (ch > '9') {
	    if (ch >= 'A' && ch <= 'F')
		value = (ch - 'A') + 10;
	    else if (ch >= 'a' && ch <= 'f')
		value = (ch - 'a') + 10;
	} else if (ch >= '0')
	    value = ch - '0';

	return(value);
    }


    static boolean
    inPackage(String classname, String packagename) {

	return(packagename.equals(getPackageName(classname)));
    }


    static boolean
    isBright(Color color) {

	int  red;
	int  green;
	int  blue;
	int  mx;

	red = color.getRed();
	green = color.getGreen();
	blue = color.getBlue();

	mx = (red > green) ? red : green;
	if (blue > mx)
	    mx = blue;
	return(mx > 127);
    }


    static boolean
    isEqual(Object obj1, Object obj2) {

	return(obj1 == obj2 ? true : (obj1 != null && obj1.equals(obj2)));
    }


    static boolean
    jvmStartsWith(String version) {

	return(getJVMVersion().startsWith(version));
    }


    static boolean
    notEqual(Object obj1, Object obj2) {

	return(obj1 != obj2 ? (obj1 == null || obj1.equals(obj2) == false) : false);
    }


    static int
    octalDigit(char ch, int fail) {

	return((ch >= '0' && ch <= '7') ? ch - '0' : fail);
    }


    static byte[]
    objectToBytes(Object object) {

	ByteArrayOutputStream  bstream;
	ObjectOutputStream     stream = null;
	byte                   bytes[] = null;

	if (object != null) {
	    bstream = new ByteArrayOutputStream();
	    try {
		stream = new ObjectOutputStream(bstream);
		stream.writeObject(object);
		stream.close();
		bytes = bstream.toByteArray();
	    }
	    catch(IOException e) {
		VM.recordException(e);
	    }
	    finally {
		if (stream != null) {
		    try {
			stream.close();
		    }
		    catch(IOException e) {}
		} else if (bstream != null) {
		    try {
			bstream.close();
		    }
		    catch(IOException e) {}
		}
	    }
	}
	return(bytes);
    }


    static Object
    objectFromBytes(byte bytes[]) {

	ObjectInputStream  stream = null;
	Object             object = null;

	if (bytes != null) {
	    if (bytes.length > 0) {
		try {
		    stream = new ObjectInputStream(new ByteArrayInputStream(bytes));
		    object = stream.readObject();
		}
		catch(Exception e) {}
		finally {
		    if (stream != null) {
			try {
			    stream.close();
			}
			catch(IOException e) {}
		    }
		}
	    }
	}

	return(object);
    }


    static String
    pad(String str, String lpad[], String rpad[], int incr) {

	StringBuffer  sbuf;
	char          ch;
	int           length;
	int           n;

	if (str != null && (length = str.length()) > 0) {
	    if (lpad != null || rpad != null) {
		sbuf = new StringBuffer(length);
		lpad = (lpad == null || lpad.length == 0) ? new String[] {""} : lpad;
		rpad = (rpad == null || rpad.length == 0) ? new String[] {""} : rpad;
		incr = Math.max(incr, 1);
		for (n = 0; n < length; n++) {
		    if (n%incr == 0)
			sbuf.append(lpad[(n/incr)%lpad.length]);
		    sbuf.append(str.charAt(n));
		    if ((n+1)%incr == 0)
			sbuf.append(rpad[(n/incr)%rpad.length]);
		}
		str = sbuf.toString();
	    }
	}
	return(str);
    }


    static String
    pad(String str, YoixObject left, YoixObject right, int incr) {

	String  lpad[];
	String  rpad[];
	int     m;
	int     n;

	if ((left != null && left.notNull()) || (right != null && right.notNull())) {
	    if (left != null) {
		lpad = new String[left.sizeof()];
		for (m = 0, n = left.offset(); m < lpad.length;)
		    lpad[m++] = left.getString(n++, "");
	    } else lpad = null;
	    if (right != null) {
		rpad = new String[right.sizeof()];
		for (m = 0, n = right.offset(); m < rpad.length;)
		    rpad[m++] = right.getString(n++, "");
	    } else rpad = null;
	    str = pad(str, lpad, rpad, incr);
	}
	return(str);
    }


    static int
    pickCaretPosition(String current, String next) {

	int  caret = 0;
	int  delta = 0;
	int  current_length;
	int  next_length;
	int  length;
	int  index;
	int  n;

	//
	// Tries to figure out a reasonable place to put the caret when the
	// text displayed by a component (e.g., JTextField) is changed from
	// current to next. This is primarily intended to provide a decent
	// guess of where the caret should go when the current text that's
	// displayed by the component is completely replaced by next, which
	// can happen in undo/redo operations. The code was borrowed from a
	// Yoix application and probably could still use some work.
	//

	if (next != null) {
	    if (current != null) {
		next_length = next.length();
		current_length = current.length();
		length = Math.min(current_length, next_length);
		for (index = 0; index < length; index++) {
		    if (current.charAt(index) != next.charAt(index))
			break;
		}
		if ((delta = next_length - current_length) < 0) {
		    caret = index;
		    index += -delta;
		} else caret = index + delta;
		if (caret < next_length && index < current_length) {
		    if (next.regionMatches(caret, current, index, current_length - index) == false)
			caret = next_length;
		}
	    } else caret = next.length();
	} else caret = 0;

	return(caret);
    }

    static byte[]
    readFile(String path) {

	FileInputStream  stream = null;
	byte             bytes[] = null;

	if (path != null && path.length() > 0) {
	    try {
		stream = new FileInputStream(toLocalPath(path));
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


    static byte[]
    readStream(InputStream stream) {

	return(readStream(stream, 8*BUFSIZ));
    }


    static byte[]
    readStream(InputStream stream, int blocksize) {

	byte  bytes[] = null;
	byte  block[];
	byte  tmp[];
	int   count;

	if (blocksize > 0) {
	    try {
		block = new byte[blocksize];
		bytes = new byte[0];
		while ((count = stream.read(block, 0, block.length)) != -1) {
		    tmp = new byte[bytes.length + count];
		    System.arraycopy(bytes, 0, tmp, 0, bytes.length);
		    System.arraycopy(block, 0, tmp, bytes.length, count);
		    bytes = tmp;
		}
	    }
	    catch(IOException e) {
		bytes = null;
	    }
	}

	return(bytes == null || bytes.length > 0 ? bytes : null);
    }


    static String
    replaceString(String dest, int offset, int length, String str, boolean trim, ArrayList undo) {

	if (dest != null) {
	    str = (str != null) ? str : "";
	    offset = Math.max(0, Math.min(offset, dest.length()));
	    length = Math.max(0, length);
	    if (trim) {
		if (offset == 0)
		    str = trimWhiteSpace(str, true, false);
		else if (offset == dest.length())
		    str = trimWhiteSpace(str, false, true);
	    }
	    if (undo != null) {
		undo.add(new Integer(offset));
		undo.add(new Integer(str.length()));
		undo.add(dest.substring(offset, offset+length));
	    }
	    if (length == 0) {
		if (str.length() > 0)
		    dest = dest.substring(0, offset) + str + dest.substring(offset);
	    } else dest = new String(new StringBuffer(dest).replace(offset, offset+length, str));
	}

	return(dest);
    }


    static String
    tokenImage(int index) {

	return(tokenImage(index, PARSER_YOIX));
    }


    static String
    tokenImage(int index, int parser) {

	String  images[] = null;
	String  image;

	if (index != YOIX_EOF) {
	    switch (parser) {
		case PARSER_DOT:
		    if (index == TAG)
			images = YoixParserConstants.tokenImage;
		    else images = DOTParserConstants.tokenImage;
		    break;

		case PARSER_DTD:
		case PARSER_XML:
		    if (index == TAG)
			images = YoixParserConstants.tokenImage;
		    else images = XMLParserConstants.tokenImage;
		    break;

		case PARSER_PATTERN:
		case PARSER_PATTERN_AND:
		case PARSER_PATTERN_OR:
		case PARSER_PATTERN_XOR:
		    if (index == TAG)
			images = YoixParserConstants.tokenImage;
		    else images = PatternParserConstants.tokenImage;
		    break;

		case PARSER_YOIX:
		    images = YoixParserConstants.tokenImage;
		    switch (index) {
			case POSTINCREMENT:
			case PREINCREMENT:
			    index = INCREMENT;
			    break;

			case POSTDECREMENT:
			case PREDECREMENT:
			    index = DECREMENT;
			    break;
		    }
		    break;

		default:
		    break;
	    }
	} else {
	    images = YoixParserConstants.tokenImage;
	    index = EOF;
	}

	return((images != null && index >= 0 && index < images.length)
	    ? images[index].substring(1, images[index].length() - 1)
	    : null
	);
    }


    static int
    tokenValue(String tok, int parser) {

	String  images[] = null;
	int     value = -1;
	int     n;

	if (tok != null) {
	    switch (parser) {
		case PARSER_DOT:
		    if (tok.equals("TAG"))
			images = YoixParserConstants.tokenImage;
		    else images = DOTParserConstants.tokenImage;
		    break;

		case PARSER_DTD:
		case PARSER_XML:
		    if (tok.equals("TAG"))
			images = YoixParserConstants.tokenImage;
		    else images = XMLParserConstants.tokenImage;
		    break;

		case PARSER_PATTERN:
		    if (tok.equals("TAG"))
			images = YoixParserConstants.tokenImage;
		    else images = PatternParserConstants.tokenImage;
		    break;

		case PARSER_YOIX:
		    images = YoixParserConstants.tokenImage;
		    break;

		default:
		    break;
	    }
	    if (images != null) {
		tok = "\"" + tok + "\"";
		for (n = 0; n < images.length; n++) {
		    if (tok.equals(images[n])) {
			value = n;
			break;
		    }
		}
	    }
	}
	return(value);
    }


    static YoixObject[]
    unrollArray(YoixObject argv[]) {

	YoixObject  element;
	ArrayList   dest;
	int         length;
	int         n;

	dest = new ArrayList();
	length = argv.length;

	for (n = 0; n < length; n++) {
	    if ((element = argv[n]) != null) {
		if (element.canUnroll())
		    unrollInto(element, dest);
		else dest.add(element);
	    }
	}

	length = dest.size();
	argv = new YoixObject[length];
	for (n = 0; n < length; n++)
	    argv[n] = (YoixObject)dest.get(n);

	return(argv);
    }


    static void
    unrollInto(YoixObject src, YoixObject dest) {

	int  length;
	int  m;
	int  n;

	if ((length = src.length()) > 0) {
	    if (src.compound()) {
		if (dest.compound()) {
		    for (n = src.offset(); n < length; n++) {
			if (src.defined(n))
			    dest.put(src.name(n), src.get(n, true), false);
		    }
		} else {
		    for (m = dest.offset(), n = src.offset(); n < length; m += 2, n++) {
			if (src.defined(n)) {
			    dest.put(m, YoixObject.newString(src.name(n)), false);
			    dest.put(m + 1, src.get(n, true), false);
			}
		    }
		}
	    } else {
		if (dest.compound()) {
		    for (m = dest.offset(), n = src.offset(); n < length; m++, n++) {
			if (src.defined(n))
			    dest.put(src.name(n), src.get(n, true), false);
		    }
		} else {
		    for (m = dest.offset(), n = src.offset(); n < length; m++, n++) {
			if (src.defined(n))
			    dest.put(m, src.get(n, true), false);
		    }
		}
	    }
	}
    }


    static YoixObject
    unrollObject(YoixObject obj) {

	YoixObject  element;
	ArrayList   dest;
	int         length;
	int         n;

	dest = new ArrayList();
	length = obj.length();

	for (n = obj.offset(); n < length; n++) {
	    if ((element = obj.getObject(n)) != null) {
		if (element.canUnroll())
		    unrollInto(element, dest);
		else dest.add(element);
	    }
	}

	length = dest.size();
	obj = YoixObject.newArray(length);
	for (n = 0; n < length; n++)
	    obj.put(n, (YoixObject)dest.get(n), true);

	return(obj);
    }


    static boolean
    writeFile(String path, byte bytes[]) {

	FileOutputStream  stream = null;
	boolean           result = false;

	if (path != null && path.length() > 0) {
	    try {
		stream = new FileOutputStream(toLocalPath(path));
		stream.write(bytes);
		result = true;
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

	return(result);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static String
    getJVMVersion() {

	String  version = null;

	if (JAVARUNTIMEVERSION == null || JAVARUNTIMEVERSION.length() == 0) {
	    if (JAVAVMVERSION == null || JAVAVMVERSION.length() == 0) {
		if (JAVAVERSION != null && JAVAVERSION.length() > 0) {
		    if (JAVAVERSION.startsWith("1."))
			version = JAVAVERSION;
		    else version = JAVAMINVERSION;
		} else version = JAVAMINVERSION;
	    } else version = JAVAVMVERSION;
	} else version = JAVARUNTIMEVERSION;
	return(version);
    }


    private static String
    htmlChar(String str) {

	String  value;
	int     val;

	// we can assume str is not null and has length >= MINIMUM_HTMLINFO
	if ((value = (String)htmlcharmap.get(str)) == null) {
	    if (str.charAt(0) == '#') {
		try {
		    val = Integer.parseInt(str.substring(1), 10);
		    if (val >= 0 && val <= 0xFFFF)
			value = Character.toString((char)val);
		}
		catch(Exception e) {
		    value = null; // belt and suspenders
		}
	    }
	}

	return(value);
    }


    private static char[]
    initPWD() {

	String  upwd;
	String  udir;
	File    fdir;
	File    fpwd;

	try {
	    upwd = System.getProperty("user.pwd");
	    udir = System.getProperty("user.dir");

	    if (upwd != null && !upwd.equals(udir)) {
		if (ISWIN) {
		    // change U/WIN "/c" to "c:"
		    if (upwd.charAt(0) == '/' && upwd.charAt(2) == '/') {
			if ((new File(upwd.charAt(1) + ":" + File.separator)).isDirectory())
			    upwd = upwd.charAt(1) + ":" + upwd.substring(2);
		    }
		}

		if (File.separatorChar != '/')
		    upwd = upwd.replace(File.separatorChar, '/');
		if (upwd.indexOf('.') >= 0) {
		    // reject relative path for "user.pwd"
		    if (
			upwd.equals(".")
			|| upwd.equals("..")
			|| upwd.startsWith("./")
			|| upwd.startsWith("../")
			|| upwd.endsWith("/.")
			|| upwd.endsWith("/..")
			|| upwd.indexOf("/./") >= 0
			|| upwd.indexOf("/../") >= 0
		    ) {
			 upwd = null;
		    }
		}

		if (upwd != null) {
		    fdir = new File(udir);
		    fpwd = new File(upwd);
		    try {
			if (fpwd.isDirectory()) {
			    if (!fdir.getCanonicalPath().equals(fpwd.getCanonicalPath()))
				upwd = null;
			} else upwd = null;
		    }
		    catch(IOException e) {
			upwd = null;
			VM.caughtException(e);
		    }
		}
	    }
	}
	catch(SecurityException e) {
	    upwd = ".";
	    udir = null;
	}

	return((upwd != null ? upwd : udir).toCharArray());
    }


    private static void
    unrollInto(YoixObject src, ArrayList dest) {

	int  length;
	int  n;

	//
	// Now private and storing Yoix string instead of Java string when
	// src is a compound object, otherwise we get a ClassCastException
	// in unrollArray() (changed on 5/14/08).
	//

	if ((length = src.length()) > 0) {
	    if (src.compound()) {
		for (n = src.offset(); n < length; n++) {
		    if (src.defined(n)) {
			dest.add(YoixObject.newString(src.name(n)));	// changed on 5/14/08
			dest.add(src.get(n, true));
		    }
		}
	    } else {
		for (n = src.offset(); n < length; n++) {
		    if (src.defined(n))
			dest.add(src.get(n, true));
		}
	    }
	}
    }
}

