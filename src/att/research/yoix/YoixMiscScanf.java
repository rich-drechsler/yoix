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
import java.util.*;
import java.text.*;

class YoixMiscScanf

    implements YoixConstants

{

    private static final int  SF_WIDTH = 1;
    private static final int  SF_SUPPRESS = 2;
    private static final int  SF_GROUP = 4;

    private static final int  SF_UTF3BYTELEAD = 0xE0;
    private static final int  SF_UTF2BYTELEAD = 0xC0;
    private static final int  SF_UTFBODYBYTE  = 0x80;

    private static final int  SF_BYTEMASK = 0xFF; // also used to size array

    private static final DecimalFormatSymbols  dfs = new DecimalFormatSymbols();
    private static final int  SF_THOUSANDS = (int)dfs.getGroupingSeparator();
    private static final int  SF_DECIMAL = (int)dfs.getDecimalSeparator();
    private static final int  SF_MINUSSIGN = (int)dfs.getMinusSign();
    private static final int  SF_PLUSSIGN = '+';

    ///////////////////////////////////
    //
    // YoixMiscScanf Methods
    //
    ///////////////////////////////////

    static int
    scanf(YoixBodyStream stream, YoixObject args[], int argn) {

	CharArrayWriter  xltbuf;
	boolean          skipws;
	long             cntoff;
	char             cformat[];
	int              cp = 0;
	int              ci = YOIX_EOF;
	int              cf = YOIX_EOF;
	int              flags = 0;
	int              width = 0;
	int              pos = 0;
	int              firstarg;
	int              maxargs = -1;

	//
	// The format string is assumed to be iso8859_1, so that
	//
	//	digit - '0'
	//
	// works the way we expect.
	//
	// NOTE - this method can generate BADCALL and BADARGUMENT errors,
	// which means it assumes the caller is a builtin or function. If
	// it's going to be used elsewhere it should be rewritten in a way
	// that gives the caller control over error messages.  Something
	// similiar has already been done for the printf support methods,
	// so that might be a good place to look for some hints, but this
	// undoubtedly will be harder.
	//

	cformat = args[argn++].stringValue().toCharArray();
	firstarg = argn;
	cntoff = stream.getInputCount();
	skipws = false;
	xltbuf = new CharArrayWriter(8);

	// check that there is input
	if ((ci = stream.getChar()) >= 0) {
	    stream.ungetChar(ci);

	outer_loop:
	    while (cp < cformat.length) {
		cf = cformat[cp++];
		if (!YoixMiscCtype.isascii(cf)) {
		    for (;;) {
			if ((ci = stream.getChar()) < 0)
			    break outer_loop;
			else if (ci != cf) {
			    stream.ungetChar(ci);
			    break outer_loop;
			}
			if (!YoixMiscCtype.isascii(cf)) {
			    if (cp < cformat.length)
				cf = cformat[cp++];
			    else break outer_loop;
			} else continue outer_loop;
		    }
		}

		if (cf != '%') {
		    if (YoixMiscCtype.isspace(cf)) {
			skipws = true;
			continue;
		    }

		    if ((ci = stream.getChar()) < 0)
			break;

		    if (skipws) {
			while (YoixMiscCtype.isspace(ci))
			    ci = stream.getChar();
			skipws = false;
		    }

		    if (ci != cf) {
			stream.ungetChar(ci);
			break;
		    }
		    continue;
		}

		if (cp < cformat.length)
		    cf = cformat[cp++];
		else break;

		// start of conversion string

		flags = 0;
		pos = argn;
		xltbuf.reset();

		// check for assignment-suppression and/or number grouping flag

		while (cf == '*' || cf == '\'') {
		    switch (cf) {
			case '*':
			    flags |= SF_SUPPRESS;
			    break;
			case '\'':
			    flags |= SF_GROUP;
			    break;
		    }
		    if (cp < cformat.length)
			cf = cformat[cp++];
		    else break outer_loop;
		}

		width = 0;
		if (YoixMiscCtype.isdigit(cf)) {
		    flags |= SF_WIDTH;
		    while (YoixMiscCtype.isdigit(cf)) {
			width = width*10 + cf - '0';
			if (cp < cformat.length)
			    cf = cformat[cp++];
			else break outer_loop;
		    }
		}

		argn = pos;
		if (width == 0)
		    width = -1;

		// check for conversion specifier
		if (skipws || (cf != '[' && cf != 'c' && cf != 'n')) {
		    if ((ci = stream.getChar()) < 0)
			break outer_loop;

		    while (YoixMiscCtype.isspace(ci)) {
			if ((ci = stream.getChar()) < 0)
			    break outer_loop;
		    }
		    skipws = false;
		    stream.ungetChar(ci);
		}


		switch (cf) {
		case '%':
		    if ((ci = stream.getChar()) != cf) {
			stream.ungetChar(ci);
			break outer_loop;
		    }
		    break;

		case 'n':
		    if ((flags&SF_SUPPRESS) == 0) {
			argn = storeArg(argn, args, new Integer((int)(stream.getInputCount() - cntoff)));
			if (argn > maxargs)
			    maxargs = argn;
		    }
		    break;

		case 'c':
		    if ((ci = stream.getChar()) < 0) {
			if (argn == 0) argn = -1;
			break outer_loop;
		    }

		    if (width < 0)
			width = 1;

		    if ((flags&SF_SUPPRESS) == 0) {
			do {
			    xltbuf.write(ci);
			} while (--width > 0 && (ci = stream.getChar()) >= 0);
			argn = storeArg(argn, args, xltbuf.toString(), false);
			if (argn > maxargs)
			    maxargs = argn;
		    } else while (--width > 0 && (ci = stream.getChar()) >= 0);

		    if (ci < 0)
			break outer_loop;
		    break;

		case 's':
		    if ((ci = stream.getChar()) < 0)
			break outer_loop;

		    do {
			if (YoixMiscCtype.isspace(ci)) {
			    stream.ungetChar(ci);
			    break;
			}
			xltbuf.write(ci);
		    } while ((width <= 0 || --width > 0) && (ci = stream.getChar()) >= 0);
		    if ((flags&SF_SUPPRESS) == 0) {
			argn = storeArg(argn, args, xltbuf.toString());
			if (argn > maxargs)
			    maxargs = argn;
		    }
		    break;

		case 'x':
		case 'X':
		case 'o':
		case 'u':
		case 'd':
		case 'i':
		    {
			int base = 0;

			switch (cf) {
			case 'x':
			case 'X':
			    base = 16;
			    break;
			case 'o':
			    base = 8;
			    break;
			case 'u':
			    base = 10;
			    break;
			case 'd':
			    base = 10;
			    break;
			case 'i':
			    base = 0;
			    break;
			}

			if ((ci = stream.getChar()) < 0)
			    break outer_loop;

			if (ci == SF_MINUSSIGN || ci == SF_PLUSSIGN) {
			    xltbuf.write(ci);
			    if (width > 0)
				width--;
			    ci = stream.getChar();
			}

			if (width != 0 && ci == '0') {
			    if (width > 0)
				width--;
			    xltbuf.write(ci);
			    ci = stream.getChar();
			    if (width != 0 && YoixMiscCtype.tolower(ci) == 'x') {
				if (base == 0)
				    base = 16;
				if (base == 16) {
				    if (width > 0)
					width--;
				    ci = stream.getChar();
				}
			    } else if (base == 0)
				base = 8;
			}

			if (base == 0)
			    base = 10;

			while (ci != YOIX_EOF && width != 0) {
			    if (base == 16 ? !YoixMiscCtype.isxdigit(ci) : ((!YoixMiscCtype.isdigit(ci) || ci - '0' >= base) && !((flags&SF_GROUP) != 0 && base == 10 && ci == SF_THOUSANDS))) {
				break;
			    }
			    if (YoixMiscCtype.isxdigit(ci)) {
				xltbuf.write(ci);
				if (width > 0)
				    width--;
			    }
			    ci = stream.getChar();
			}

			stream.ungetChar(ci);

			if (xltbuf.size() == 0 || (xltbuf.size() == 1 && ((xltbuf.toCharArray())[0] == SF_PLUSSIGN || (xltbuf.toCharArray())[0] == SF_MINUSSIGN))) {
			    break outer_loop;
			}

			if ((flags&SF_SUPPRESS) == 0) {
			    try {
				argn = storeArg(argn, args, Integer.valueOf(xltbuf.toString(), base));
				if (argn > maxargs)
				    maxargs = argn;
			    }
			    catch(NumberFormatException nfe) {
				break outer_loop;
			    }
			}
		    }
		    break;

		case 'e':
		case 'E':
		case 'f':
		case 'g':
		case 'G':
		    {
			boolean has_dot = false;
			boolean has_pow = false;

			if ((ci = stream.getChar()) < 0)
			    break outer_loop;

			if (ci == SF_MINUSSIGN || ci == SF_PLUSSIGN) {
			    if (width > 0)
				width--;
			    if ((ci = stream.getChar()) < 0)
				break;
			}

			do {
			    if (YoixMiscCtype.isdigit(ci))
				xltbuf.write(ci);
			    else if (has_pow &&  (xltbuf.toCharArray())[xltbuf.size()-1] == 'e' && (ci == SF_MINUSSIGN || ci == SF_PLUSSIGN)) {
				xltbuf.write(ci);
			    } else if (xltbuf.size() > 0 && !has_pow && YoixMiscCtype.tolower(ci) == 'e') {
				xltbuf.write('e');
				has_pow = has_dot = true;
			    } else if (ci == SF_DECIMAL && !has_dot) {
				xltbuf.write(ci);
				has_dot = true;
			    } else if ((flags&SF_GROUP) != 0 && ci == SF_THOUSANDS && !has_dot) {
				if (width > 0)
				    width++;
			    } else {
				stream.ungetChar(ci);
				break;
			    }
			    if (width > 0)
				width--;
			} while (width != 0 && (ci = stream.getChar()) >= 0);

			if (xltbuf.size() == 0)
			    break;

			if ((flags&SF_SUPPRESS) == 0) {
			    try {
				argn = storeArg(argn, args, Double.valueOf(xltbuf.toString()));
				if (argn > maxargs)
				    maxargs = argn;
			    }
			    catch(NumberFormatException nfe) {
				break outer_loop;
			    }
			}
		    }
		    break;

		case '[':
		    {
			boolean negated = false;
			int previous;

			if ((ci = stream.getChar()) < 0)
			    break outer_loop;
			if (utfSizeFromLead(ci) < 0) {
			    stream.ungetChar(ci);
			    break;
			}
			if (cp < cformat.length)
			    cf = cformat[cp++];
			else break outer_loop;

			if (cf == '^') {
			    if (cp < cformat.length)
				cf = cformat[cp++];
			    else break outer_loop;
			    negated = true;
			}

			boolean  lookup_table[] = new boolean[SF_BYTEMASK];

			// accept first char even if it is ']' or '-'
			lookup_table[cf] = true;
			previous = cf;

			while (cp < cformat.length && (cf = cformat[cp]) != ']') {
			    cp++;
			    if (cf == '-') {
				if (cp < cformat.length)
				    cf = cformat[cp++];
				else break outer_loop;
				if (cf == ']') {
				    lookup_table['-'] = true;
				    break;
				} else {
				    for (int b = previous; b <= cf; b++)
					lookup_table[b] = true;
				}
			    } else lookup_table[cf] = true;
			    previous = cf;
			}

			if (cp < cformat.length) cp++;

			do {
			    if (ci >= SF_BYTEMASK || lookup_table[ci] == negated) {
				stream.ungetChar(ci);
				break;
			    }
			    xltbuf.write(ci);
			    if (width > 0)
				width--;
			} while (width != 0 && ((ci = stream.getChar()) >= 0));

			if ((flags&SF_SUPPRESS) == 0) {
			    argn = storeArg(argn, args, xltbuf.toString());
			    if (argn > maxargs)
				maxargs = argn;
			}
		    }
		    break;
		}
	    }

	    if (cformat.length == cp) {
		if (skipws && ci >= 0) {
		    while (YoixMiscCtype.isspace(ci = stream.getChar()));
		    stream.ungetChar(ci);
		}
		if (maxargs < 0)
		    maxargs = firstarg;
	    }

	}

	return(maxargs >= firstarg ? maxargs - firstarg : -1);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static int
    storeArg(int pos, YoixObject args[], Object value) {

	return(storeArg(pos, args, value, true));
    }


    private static int
    storeArg(int pos, YoixObject args[], Object value, boolean terminate) {

	YoixObject  lval;
	YoixObject  dest;
	int         length;
	int         num;
	int         n;

	if (args != null && args.length > pos) {
	    lval = args[pos];
	    if (lval.isPointer()) {
		if (value instanceof String) {
		    if (lval.isString() == false) {
			dest = lval.get();
			if (dest.isNumber()) {
			    length = ((String)value).length();
			    for (n = num = 0; n < length; n++)
				num = (num << 8) | ((String)value).charAt(n);
			    lval.put(YoixObject.newInt(num));
			} else if (dest.isString())
			    lval.put(YoixObject.newString((String)value));
			else if (dest.isArray())
			    YoixMisc.copyInto((String)value, dest);
			else VM.badArgument(pos);
		    } else {
			if (terminate && ((String)value).length() < lval.sizeof())
			    value = (String)value + '\0';
			lval.overlay((String)value);
		    }
		} else if (value instanceof Number)
		    lval.put(YoixObject.newNumber((Number)value));
		else VM.die(INTERNALERROR);
	    } else VM.badArgument(pos);
	} else VM.badCall();

	return(pos + 1);
    }


    private static boolean
    utfBodyByte(int ch) {

	return((SF_UTFBODYBYTE&ch) == SF_UTFBODYBYTE);
    }


    private static int
    utfSizeFromLead(int ch) {

	int  size;

	if ((ch & SF_UTF3BYTELEAD) == SF_UTF3BYTELEAD)
	    size = 3;
	else if ((ch & SF_UTF2BYTELEAD) == SF_UTF2BYTELEAD)
	    size = 2;
	else if (YoixMiscCtype.isascii(ch))
	    size = 1;
	else size = -1;

	return(size);
    }
}

