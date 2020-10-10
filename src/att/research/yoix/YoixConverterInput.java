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

class YoixConverterInput extends YoixConverter

    implements YoixConstants

{

    //
    // Protects Yoix from the fact that the ByteToCharConverter is not
    // part of java.io (usually in sun.io) and so is not guaranteed to
    // exist or be consistent between versions.
    //

    private int  mrkpos = -1;
    private int  mrksiz = 0;
    private int  bufsiz = 0;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixConverterInput(String enc, int size) {

	super(enc);
	byteBuf = new byte[(size*maxCharsPerByte) + maxBytesPerChar - 1];
	charBuf = new char[size];
	this.bufsiz = size;
    }

    ///////////////////////////////////
    //
    // YoixConverterInput Methods
    //
    ///////////////////////////////////

    final synchronized void
    mark(int size) {

	char  newbuf[];

	if (charBuf.length < size) {
	    newbuf = new char[size];
	    System.arraycopy(charBuf, nextCharIdx, newbuf, 0, charEnd - nextCharIdx);
	} else System.arraycopy(charBuf, nextCharIdx, charBuf, 0, charEnd - nextCharIdx);

	charEnd = charEnd - nextCharIdx;
	nextCharIdx = 0;
	mrkpos = 0;
	mrksiz = size;
    }


    final synchronized void
    reset()

	throws IOException

    {

	if (mrkpos >= 0)
	    nextCharIdx = mrkpos;
	else throw(new IOException("mark not set or expired"));
    }


    final int
    convert(byte input[], int start, int end) {

	String  encoded;
	byte    final_bytes[];
	byte    tmp_bytes[];
	int     bytes;
	int     nc = 0;
	int	j;
	int	k;

	bytes = end - start;
	byteEnd = nextByteIdx + bytes;

	if (charStart != charEnd)
	    VM.abort(INTERNALERROR);

	if (byteEnd > byteBuf.length) {
	    // cannot fit into remaining space, so
	    // shift unused bytes (if any) to the front
	    if (byteStart < byteEnd) {
		System.arraycopy(byteBuf, byteStart, byteBuf, 0, nextByteIdx - byteStart);
		nextByteIdx -= byteStart;
		byteEnd = nextByteIdx + bytes;
		byteStart = 0;
	    } else {
		nextByteIdx = 0;
		byteEnd = bytes;
		byteStart = 0;
	    }
	}

	if (byteEnd > byteBuf.length)
	    VM.abort(INTERNALERROR);

	System.arraycopy(input, start, byteBuf, nextByteIdx, bytes);

	// reset offsets except for case where mrksiz is bigger than bufsiz
	if (!(mrkpos >= 0 && mrksiz > bufsiz && (charEnd - mrkpos) < mrksiz)) {
	    charStart = 0;
	    mrkpos = -1;
	}

	if (haveConverters() && !useKludge()) {
	    six_args[0] = byteBuf;
	    six_args[1] = new Integer(byteStart);
	    six_args[2] = new Integer(byteEnd);
	    six_args[3] = charBuf;
	    six_args[4] = new Integer(charStart);
	    six_args[5] = new Integer(charBuf.length);
	    try {
		nc = ((Integer)(btc_convert.invoke(btc, six_args))).intValue();
		byteStart = nextByteIdx = ((Integer)(btc_nextByteIndex.invoke(btc, null))).intValue();
		if (nextByteIdx != byteEnd)
		    VM.abort(INTERNALERROR);
		nextCharIdx = ((Integer)(btc_nextCharIndex.invoke(btc, null))).intValue();
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
			    byteStart = nextByteIdx = ((Integer)(btc_nextByteIndex.invoke(btc, null))).intValue();
			    nextCharIdx = ((Integer)(btc_nextCharIndex.invoke(btc, null))).intValue();
			    nc = nextCharIdx - charStart;
			}
			catch(Exception ex) {
			    VM.caughtException(ex, false, true);
			}
		    } else VM.abort(e.getTargetException());
		} else VM.abort(e.getTargetException());
	    }
	    charEnd = nextCharIdx;
	} else {
	    try {
		encoded = new String(byteBuf, byteStart, byteEnd - byteStart, encoding);
	    }
	    catch(UnsupportedEncodingException e) {
		encoded = null;
		VM.abort(BADENCODING, encoding);
	    }
	    if ((nc = encoded.length()) > 0) {
		if ((charStart + nc) > charBuf.length) {
		    // only take what buffer can handle
		    if ((nc = charBuf.length - charStart) == 0)
			VM.abort(INTERNALERROR);
		}
		encoded.getChars(0, nc, charBuf, charStart);
		try {
		    final_bytes = encoded.substring(nc - 1).getBytes(encoding);
		}
		catch(UnsupportedEncodingException e) {
		    final_bytes = null;
		    VM.abort(BADENCODING, encoding);
		}
		if (
		   final_bytes.length == 4 // might be Unicode with BOM
		   &&
		   (
		    (final_bytes[0] == BE_BOM[0] && final_bytes[1] == BE_BOM[1]) // big endian BOM
		    ||
		    (final_bytes[0] == BE_BOM[1] && final_bytes[1] == BE_BOM[0]) // little endian BOM
		    )
		   ) {
		    // get rid of BOM
		    tmp_bytes = new byte[2];
		    tmp_bytes[0] = final_bytes[2];
		    tmp_bytes[1] = final_bytes[3];
		    final_bytes = tmp_bytes;
		}
		nextByteIdx = -1;
		for (j = 0; j < 16; j++) {
		    if (byteBuf[byteEnd - final_bytes.length - j] == final_bytes[0]) {
			for (k = 1; k < final_bytes.length; k++) {
			    if (input[byteEnd - final_bytes.length - j + k] != final_bytes[k])
				break;
			}
			if (k == final_bytes.length) {
			    byteStart = nextByteIdx = byteEnd - j;
			    break;
			}
		    }
		}
		if (nextByteIdx < 0)
		    VM.abort(INTERNALERROR);
	    }
	    charEnd = nextCharIdx = charStart + nc;
	}

	return(nc);
    }


    final int
    flush() {

	String  encoded;
	int	nc = 0;

	if (nextCharIdx != charEnd)
	    VM.abort(INTERNALERROR);

	// reset offsets except for case where mrksiz is bigger than bufsiz
	if (!(mrkpos >= 0 && mrksiz > bufsiz && (nextCharIdx - mrkpos) < mrksiz)) {
	    nextCharIdx = 0;
	    mrkpos = -1;
	}

	if (haveConverters() && !useKludge()) {
	    three_args[0] = charBuf;
	    three_args[1] = new Integer(nextCharIdx);
	    three_args[2] = new Integer(charBuf.length);
	    try {
		nc = ((Integer)(btc_flush.invoke(btc, three_args))).intValue();
		nextCharIdx = ((Integer)(btc_nextCharIndex.invoke(btc, null))).intValue();
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
			    nextCharIdx = ((Integer)(btc_nextCharIndex.invoke(btc, null))).intValue();
			    nc = nextCharIdx - charStart;
			}
			catch(Exception ex) {
			    VM.caughtException(ex, false, true);
			}
		    } else VM.abort(e.getTargetException());
		} else VM.abort(e.getTargetException());
	    }
	    charEnd = nextCharIdx;
	    if (nc == 0)
		charStart = charEnd;
	} else {
	    nc = 0;
	    if (byteStart < nextByteIdx) {
		try {
		    encoded = new String(byteBuf, byteStart, nextByteIdx, encoding);
		    if ((nc = encoded.length()) > 0)
			encoded.getChars(0, nc, charBuf, charStart);
		}
		catch(UnsupportedEncodingException e) {
		    VM.abort(BADENCODING, encoding);
		}
	    }
	    charEnd = nextCharIdx = charStart + nc;
	}

	return(nc);
    }


    final int
    getBufsize() {

	return(bufsiz);
    }
}

