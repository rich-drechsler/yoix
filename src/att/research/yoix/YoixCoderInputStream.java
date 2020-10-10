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
class YoixCoderInputStream extends FilterInputStream

    implements YoixConstants,
               YoixConstantsStream

{

    private InputStream  in;
    private int          coder;
    private boolean      ietf;

    final static private int  HDLENGTH = YoixMisc.HEXDIGITS.length;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixCoderInputStream(InputStream in) {

	this(in, MIMECODER);
    }


    YoixCoderInputStream(InputStream in, int  coder) {

	super(in);
	this.in = in;
	this.coder = coder;

	if (coder == IETFCODER)
	    ietf = true;
	else ietf = false;
    }

    ///////////////////////////////////
    //
    // YoixCoderInputStream Methods
    //
    ///////////////////////////////////

    public final int
    available()

	throws IOException

    {

	int  avail;

	avail = in.available();

	if (coder == HEXCODER || coder == LINEDHEXCODER)
	    avail /= 2;
	else avail /= 3;
    
	return(avail);
    }


    public final void
    close()

	throws IOException

    {

	in.close();
    }


    public final synchronized void
    mark(int readlimit) {

	in.mark(readlimit);
    }


    public final boolean
    markSupported() {

	return(in.markSupported());
    }


    public final int
    read()

	throws IOException

    {

	int  ch;
	int  ch0;
	int  ch1;
	int  d1;
	int  d2;

	//
	// All (coder) reads go through here -- may be a performance bottleneck,
	// but for now it will do.
	//

	switch (coder) {
	    case HEXCODER:
		if ((ch0 = in.read()) >= 0 && (ch1 = in.read()) >= 0) {
		    if (ch0 < HDLENGTH && ch1 < HDLENGTH) {
			if ((d1 = YoixMisc.HEXDIGITS[ch0]) >= 0 && (d2 = YoixMisc.HEXDIGITS[ch1]) >= 0) {
			    ch = (d1<<4)|d2;
			} else throw new IOException("input is not in HEX format");
		    } else throw new IOException("input is not in HEX format");
		} else ch = -1;
		break;

	    case LINEDHEXCODER:
		if ((ch0 = in.read()) >= 0 && (ch1 = in.read()) >= 0) {
		    for(;;) {
			//
			// Is it reasonable to assume that these are the universal
			// line break characters or should we look decode NL and
			// look for the bytes in there?
			//
			if (ch0 == '\r' || ch0 == '\n') {
			    ch0 = ch1;
			    if ((ch1 = in.read()) >= 0)
				continue;
			    else ch = -1;
			} else if (ch0 < HDLENGTH && ch1 < HDLENGTH) {
			    if ((d1 = YoixMisc.HEXDIGITS[ch0]) >= 0 && (d2 = YoixMisc.HEXDIGITS[ch1]) >= 0) {
				ch = (d1<<4)|d2;
			    } else throw new IOException("input is not in HEX format");
			} else throw new IOException("input is not in HEX format");
			break;
		    }
		} else ch = -1;
		break;

	    case IETFCODER:
	    case MIMECODER:
		if ((ch0 = in.read()) >= 0) {
		    if (ch0 <= 0x7F) {
			if (ch0 == '%') {
			    if ((ch0 = in.read()) >= 0 && (ch1 = in.read()) >= 0) {
				if (ch0 < HDLENGTH && ch1 < HDLENGTH) {
				    if ((d1 = YoixMisc.HEXDIGITS[ch0]) >= 0 && (d2 = YoixMisc.HEXDIGITS[ch1]) >= 0) {
					ch = (d1<<4)|d2;
				    } else throw new IOException("input is not in URL format");
				} else throw new IOException("input is not in URL format");
			    } else ch = -1;
			} else if (ietf || ch0 != '+')
			    ch = ch0;
			else ch = ' ';
		    } else throw new IOException("input is not in URL format");
		} else ch = -1;
		break;

	    default:
		VM.abort(INTERNALERROR);
		ch = -1; // for compiler
		break;
	}

	return(ch);
    }


    public final int
    read(byte b[])

	throws IOException

    {

	return(read(b, 0, b == null ? 0 : b.length));
    }


    public final int
    read(byte b[], int off, int len)

	throws IOException

    {

	int  ch;
	int  rd = 0;

	if (b != null && len > 0 && off < b.length) {
	    while (len > 0 && off < b.length && (ch = read()) >= 0) {
		rd++;
		len--;
		b[off++] = (byte)ch;
	    }
	    if (rd == 0)
		rd = -1;
	}
	return(rd);
    }


    public final synchronized void
    reset()

	throws IOException

    {

	in.reset();
    }


    public final long
    skip(long n)

	throws IOException

    {

	long  skipped = 0;

	while (n-- > 0L && read() >= 0)
	    skipped++;
	return(skipped);
    }
}

