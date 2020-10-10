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

abstract
class YoixMiscCtype

    implements YoixConstants

{

    //
    // Familiar (but not international) C-style character testing.
    //

    private static final short  CN = 0x01;	// control
    private static final short  WS = 0x02;	// white space
    private static final short  SP = 0x04;	// space
    private static final short  PU = 0x08;	// punctuation
    private static final short  DG = 0x10;	// digit
    private static final short  OD = 0x20;	// octal digit
    private static final short  UC = 0x40;	// upper case
    private static final short  HD = 0x80;	// hex digit
    private static final short  LC = 0x100;	// lower case

    private static final short  ctype[] = {
	CN, CN, CN, CN, CN, CN, CN, CN,
	CN, CN|WS, CN|WS, CN|WS, CN|WS, CN|WS, CN, CN,
	CN, CN, CN, CN, CN, CN, CN, CN,
	CN, CN, CN, CN, CN, CN, CN, CN,
	WS|SP, PU, PU, PU, PU, PU, PU, PU,
	PU, PU, PU, PU, PU, PU, PU, PU,
	DG|OD, DG|OD, DG|OD, DG|OD, DG|OD, DG|OD, DG|OD, DG|OD,
	DG, DG, PU, PU, PU, PU, PU, PU,
	PU, UC|HD, UC|HD, UC|HD, UC|HD, UC|HD, UC|HD, UC,
	UC, UC, UC, UC, UC, UC, UC, UC,
	UC, UC, UC, UC, UC, UC, UC, UC,
	UC, UC, UC, PU, PU, PU, PU, PU,
	PU, LC|HD, LC|HD, LC|HD, LC|HD, LC|HD, LC|HD, LC,
	LC, LC, LC, LC, LC, LC, LC, LC,
	LC, LC, LC, LC, LC, LC, LC, LC,
	LC, LC, LC, PU, PU, PU, PU, CN,
    };

    private static final short  ALPHA = (UC|LC);
    private static final short  ALNUM = (UC|LC|DG);
    private static final short  GRAPH = (ALNUM|PU);
    private static final short  PRINT = (GRAPH|SP);
    private static final short  ODIGIT = (OD);
    private static final short  XDIGIT = (DG|HD);

    static final short  LOWERTOUPPER = 'A' - 'a';
    static final short  UPPERTOLOWER = 'a' - 'A';

    ///////////////////////////////////
    //
    // YoixMiscCtype Methods
    //
    ///////////////////////////////////

    static boolean
    isalnum(int ch) {

	return(isascii(ch) && (ctype[ch]&ALNUM) != 0);
    }


    static boolean
    isalpha(int ch) {

	return(isascii(ch) && (ctype[ch]&ALPHA) != 0);
    }


    static boolean
    isascii(int ch) {

	return(ch >= 0 && ch < 128);
    }


    static boolean
    iscntrl(int ch) {

	return(isascii(ch) && (ctype[ch]&CN) != 0);
    }


    static boolean
    isdigit(int ch) {

	return(isascii(ch) && (ctype[ch]&DG) != 0);
    }


    static boolean
    isgraph(int ch) {

	return(isascii(ch) && (ctype[ch]&DG) != 0);
    }


    static boolean
    islower(int ch) {

	return(isascii(ch) && (ctype[ch]&LC) != 0);
    }


    static boolean
    isoctal(int ch) {

	return(isascii(ch) && (ctype[ch]&ODIGIT) != 0);
    }


    static boolean
    isprint(int ch) {

	return(isascii(ch) && (ctype[ch]&PRINT) != 0);
    }


    static boolean
    ispunct(int ch) {

	return(isascii(ch) && (ctype[ch]&PU) != 0);
    }


    public static boolean
    isspace(int ch) {

	return(isascii(ch) && (ctype[ch]&WS) != 0);
    }


    static boolean
    isupper(int ch) {

	return(isascii(ch) && (ctype[ch]&UC) != 0);
    }


    static boolean
    isxdigit(int ch) {

	return(isascii(ch) && (ctype[ch]&XDIGIT) != 0);
    }


    static int
    tolower(int ch) {

	return(isupper(ch) ? ch + UPPERTOLOWER : ch);
    }


    static int
    toupper(int ch) {

	return(islower(ch) ? ch + LOWERTOUPPER : ch);
    }
}

