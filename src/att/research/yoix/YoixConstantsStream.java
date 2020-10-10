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

interface YoixConstantsStream {

    static final int  FLUSHDISABLED = 0;
    static final int  FLUSHLINES = 1;
    static final int  FLUSHWRITES = 2;

    static final int  GZIP          = 1<<0;
    static final int  ZIPPED        = 1<<1;
    static final int  CHECKSUM      = 1<<2;
    static final int  ADLER32       = CHECKSUM|1<<3; // see YoixChecksum
    static final int  BSD           = CHECKSUM|1<<4;
    static final int  CRC32         = CHECKSUM|1<<5;
    static final int  SYSV          = CHECKSUM|1<<6;
    static final int  CHECKSUM_MASK = ADLER32|BSD|CRC32|SYSV;
    static final int  HEXCODER      = 1<<7;
    static final int  IETFCODER     = 1<<8;
    static final int  LINEDHEXCODER = 1<<9;
    static final int  MIMECODER     = 1<<10;

    static final int  CR_EOL    = 1<<0;
    static final int  NL_EOL    = 1<<1;
    static final int  CR_NL_EOL = 1<<2;
    static final int  EOL_MASK  = CR_EOL|NL_EOL|CR_NL_EOL;

    static final String  USERAGENT = "User-Agent";
}

