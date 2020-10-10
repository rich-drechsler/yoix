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
import java.util.zip.*;

class YoixChecksum

    implements Checksum,
	       YoixConstants,
	       YoixConstantsStream

{

    private Checksum  java_checksum = null;
    private long      checksum;
    private long      bytes;
    private int       type = 0;

    private static final long  CHECKMASK = 0x1FFFFFFFFFFFFFFL;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixChecksum(int t) {

	type = t&CHECKSUM_MASK;

	switch (type) {
	    // Adler32 seems buggy, so skip it
	    //case ADLER32:
	    //java_checksum = new Adler32();
	    //break;

	    case BSD:
		break;

	    case CRC32:
		java_checksum = new CRC32();
		break;

	    default:
		type = SYSV;
		break;
	}

	reset();
    }

    ///////////////////////////////////
    //
    // Checksum Methods
    //
    ///////////////////////////////////

    public final long
    getValue() {

	long  value = 0;

	if (java_checksum == null) {
	    if (type == BSD) {
		value = ((checksum & 0xFFFF) + (bytes > 0 ? (((1L + (long)((bytes-1)/1024)) & 0xFFFF) << 16) : 0)) & CHECKMASK;
	    } else {
		value = ((((checksum >> 16) + checksum & 0xFFFF) & 0xFFFF) + (bytes > 0 ? (((1L + (long)((bytes-1)/512)) & 0xFFFF) << 16) : 0)) & CHECKMASK;
	    }
	} else value = java_checksum.getValue();

	return(value);
    }


    public final void
    reset() {

	if (java_checksum == null) {
	    bytes = 0;
	    checksum = 0;
	} else java_checksum.reset();
    }


    public final void
    update(int b) {

	if (java_checksum == null) {
	    bytes++;
	    if (type == BSD) {
		if ((checksum & 1) != 0)
		    checksum = 0xFFFF & ((checksum>>1) + b + 0x8000);
		else checksum = 0xFFFF & ((checksum>>1) + b);
	    } else checksum += 0xFFFF & b;
	} else java_checksum.update(b);
    }


    public final void
    update(byte b[], int off, int len) {

	int  i;
	int  l;

	if (java_checksum == null) {
	    l = off + len;
	    if (l > b.length)
		l = b.length;
	    bytes += l - off;

	    if (type == BSD) {
		// assume off is in range? off >= 0? len > 0?
		for (i = off; i < l; i++)
		    if ((checksum & 1) != 0)
			checksum = 0xFFFF & ((checksum>>1) + b[i] + 0x8000);
		    else checksum = 0xFFFF & ((checksum>>1) + b[i]);
	    } else {
		// assume off is in range? off >= 0? len > 0?
		for (i = off; i < l; i++)
		    checksum += 0xFFFF & b[i];
	    }
	} else java_checksum.update(b, off, len);
    }

    ///////////////////////////////////
    //
    // YoixChecksum Methods
    //
    ///////////////////////////////////

    static String
    chkstr(double chksum) {

	long value = ((long)(chksum + 0.001)) & CHECKMASK;
	long sum = value & 0xFFFFL;
	long cnt = value >> 16;

	return(sum + " " + cnt);
    }
}

