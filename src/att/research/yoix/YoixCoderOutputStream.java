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

final
class YoixCoderOutputStream extends FilterOutputStream

    implements YoixConstants,
               YoixConstantsStream

{

    private OutputStream  out;
    private int           coder;

    private boolean  clearchars[];
    private boolean  lined;
    private boolean  ietf;
    private int      bytecnt;
    private byte     nlbytes[];

    private static final int MAXBYTES = 60;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixCoderOutputStream(OutputStream out) {

	this(out, MIMECODER, null);
    }


    YoixCoderOutputStream(OutputStream out, int coder) {

	this(out, coder, null);
    }


    YoixCoderOutputStream(OutputStream out, int coder, String encoding) {

	super(out);

	this.out = out;
	this.coder = coder;

	lined = false;
	ietf = false;
	clearchars = null;
	bytecnt = 0;
	nlbytes = null;

	switch (coder) {
	    case LINEDHEXCODER:
		lined = true;
		if (encoding == null)
		    nlbytes = NL.getBytes();
		else {
		    try {
			nlbytes = NL.getBytes(encoding);
		    }
		    catch(UnsupportedEncodingException uee) {
			nlbytes = NL.getBytes();
			VM.caughtException(uee, true);
		    }
		}
		break;

	    case HEXCODER:
		break;

	    case IETFCODER:
		ietf = true;
		clearchars = YoixMisc.IETF_CLEARCHARS;
		break;

	    case MIMECODER:
		clearchars = YoixMisc.MIME_CLEARCHARS;
		break;

	    default:
		VM.abort(INTERNALERROR);
		break;
	}
    }

    ///////////////////////////////////
    //
    // YoixCoderOutputStream Methods
    //
    ///////////////////////////////////

    public final void
    close()

	throws IOException

    {

	if (coder == LINEDHEXCODER) {
	    if (bytecnt > 0) {
		out.write(nlbytes);
		bytecnt = 0;
	    }
	}
	try {
	    flush();
	}
	catch(IOException io) {}
	out.close();
	clearchars = null;
    }


    public final void
    flush()

	throws IOException

    {

	out.flush();
    }


    public final void
    write(int ch)

	throws IOException

    {

	switch (coder) {
	    case LINEDHEXCODER:
		out.write(YoixMisc.HEXNIBBLES[(ch>>4) & 0xF]);
		out.write(YoixMisc.HEXNIBBLES[ch & 0xF]);
		bytecnt += 2;
		if (bytecnt >= MAXBYTES) {
		    out.write(nlbytes);
		    bytecnt = 0;
		}
		break;

	    case HEXCODER:
		out.write(YoixMisc.HEXNIBBLES[(ch>>4) & 0xF]);
		out.write(YoixMisc.HEXNIBBLES[ch & 0xF]);
		break;

	    case IETFCODER:
	    case MIMECODER:
		if (ch >= 128 || clearchars[ch] == false) {
		    out.write('%');
		    out.write(YoixMisc.HEXNIBBLES[(ch>>4) & 0xF]);
		    out.write(YoixMisc.HEXNIBBLES[ch & 0xF]);
		} else out.write((ietf || ch != ' ') ? ch : '+');
		break;
	}
    }


    public final void
    write(byte b[])

	throws IOException

    {

	if (b != null)
	    write(b, 0, b.length);
    }


    public final void
    write(byte b[], int off, int len)

	throws IOException

    {

	int  ch;
	int  rd = 0;

	if (b != null) {
	    while (len > 0 && off < b.length) {
		write(0xFF&b[off++]);
		len--;
	    }
	}
    }
}

