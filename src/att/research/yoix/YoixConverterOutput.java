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
import java.util.*;

class YoixConverterOutput extends YoixConverter

    implements YoixConstants

{

    //
    // Protects Yoix from the fact that the ByteToCharConverter is not
    // part of java.io (usually in sun.io) and so is not guaranteed to
    // exist or be consistent between versions.
    //

    private Integer  lastpos = null;
    private int      mrkpos = -1;
    private int      mrksiz = 0;
    private int      bufsiz = 0;

    private static final Integer  ZERO = new Integer(0);

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixConverterOutput(String enc, int size) {

	super(enc);
	charBuf = new char[size];
	charStart = 0;
	nextCharIdx = 0;
	charEnd = charBuf.length;
	byteBuf = new byte[(size*maxBytesPerChar) + maxCharsPerByte - 1];
	byteStart = nextByteIdx = byteEnd = 0;
	lastpos = new Integer(byteBuf.length);
	this.bufsiz = size;
    }

    ///////////////////////////////////
    //
    // YoixConverterOutput Methods
    //
    ///////////////////////////////////

    final int
    convert() {

	byte  tmp_bytes[];
	int   lastchar = nextCharIdx;
	int   nb = 0;

	if (nextByteIdx != byteEnd)
	    VM.abort(INTERNALERROR);

	if (haveConverters() && !useKludge()) {
	    six_args[0] = charBuf;
	    six_args[1] = new Integer(charStart);
	    six_args[2] = new Integer(lastchar);
	    six_args[3] = byteBuf;
	    six_args[4] = ZERO;
	    six_args[5] = lastpos;
	    try {
		nb = ((Integer)(ctb_convert.invoke(ctb, six_args))).intValue();
		nextCharIdx = ((Integer)(ctb_nextCharIndex.invoke(ctb, null))).intValue();
		if (nextCharIdx != lastchar)
		    VM.abort(INTERNALERROR);
		nextByteIdx = ((Integer)(ctb_nextByteIndex.invoke(ctb, null))).intValue();
	    }
	    catch(IllegalArgumentException e) {
		VM.caughtException(e, false, true);
	    }
	    catch(IllegalAccessException e) {
		VM.caughtException(e, false, true);
	    }
	    catch(InvocationTargetException e) {
		Throwable  t = e.getTargetException();

		if (t instanceof CharConversionException) {
		    if (cbfe_handle.equals(t.getClass())) {
			try {
			    nextByteIdx = ((Integer)(ctb_nextByteIndex.invoke(ctb, null))).intValue();
			    nb = nextByteIdx - byteStart;
			}
			catch(Exception ex) {
			    VM.caughtException(ex, false, true);
			}
		    } else VM.abort(e.getTargetException());
		} else VM.abort(e.getTargetException());
	    }
	    byteEnd = nextByteIdx;
	    nextByteIdx = 0;
	} else {
	    try {
		tmp_bytes = (String.copyValueOf(charBuf, charStart, nextCharIdx - charStart)).getBytes(encoding);
		nb = tmp_bytes.length;
		System.arraycopy(tmp_bytes, 0, byteBuf, 0, nb);
		byteEnd = nb;
		nextByteIdx = 0;
	    }
	    catch(UnsupportedEncodingException e) {
		VM.abort(BADENCODING, encoding);
	    }
	}

	charStart = nextCharIdx = 0;
	return(nb);
    }


    final int
    flush() {

	int  nb = 0;

	if (nextByteIdx != byteEnd)
	    VM.abort(INTERNALERROR);

	if (haveConverters() && !useKludge()) {
	    three_args[0] = byteBuf;
	    three_args[1] = ZERO;
	    three_args[2] = lastpos;
	    try {
		nb = ((Integer)(ctb_flush.invoke(ctb, three_args))).intValue();
		nextByteIdx = ((Integer)(ctb_nextByteIndex.invoke(ctb, null))).intValue();
	    }
	    catch(IllegalArgumentException e) {
		VM.caughtException(e, false, true);
	    }
	    catch(IllegalAccessException e) {
		VM.caughtException(e, false, true);
	    }
	    catch(InvocationTargetException e) {
		Throwable  t = e.getTargetException();

		if (t instanceof CharConversionException) {
		    if (cbfe_handle.equals(t.getClass())) {
			try {
			    nextByteIdx = ((Integer)(ctb_nextByteIndex.invoke(ctb, null))).intValue();
			    nb = nextByteIdx - byteStart;
			}
			catch(Exception ex) {
			    VM.caughtException(ex, false, true);
			}
		    } else VM.abort(e.getTargetException());
		} else VM.abort(e.getTargetException());
	    }
	    byteEnd = nextByteIdx;
	    nextByteIdx = 0;
	} else byteEnd = nextByteIdx = 0;

	charStart = nextCharIdx = 0;
	return(nb);
    }


    final int
    getBufsize() {

	return(bufsiz);
    }
}

