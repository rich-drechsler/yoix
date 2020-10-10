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
import java.util.*;

///////////////////////////////////
//
// The regular expression implementation presented here is derived
// in large part from work done by M. Douglas McIlroy while at AT&T.
// He is currently an adjunct professor at Dartmouth. More information
// about Doug and his extensive constributions to computer science can
// be found at:
//  http://www.cs.dartmouth.edu/~doug/
//
///////////////////////////////////

public
class YoixRERegexp

    implements YoixAPI,
	       YoixConstants

{

    private Program  program;
    private String   pattern;

    private Stack  andstack = new Stack();
    private Stack  atorstack = new Stack();
    private Stack  subidstack = new Stack();

    private boolean  lastwasand = false;
    private boolean  shell = false;
    private boolean  rawshell = false;
    private boolean  case_insensitive = false;

    private char  exprp[] = null;
    private int   nbra = 0;
    private int   eoff = 0;
    private int   popped_subid = 0;

    private int   cursubid = 0;
    private int   errors = 0;

    private int   cclass_mask;
    private int   cclass_size;

    private static final int  LISTINCREMENT = 10;

    private static final int  OPERATOR = 0x200000;
    private static final int  START = 0x200000;
    private static final int  RBRA = 0x200001;
    private static final int  LBRA = 0x200002;
    private static final int  OR = 0x200003;
    private static final int  CAT = 0x200004;
    private static final int  STAR = 0x200005;
    private static final int  PLUS = 0x200006;
    private static final int  QUEST = 0x200007;

    private static final int  OPERAND = 0x300000;
    private static final int  ANY = 0x300000;
    private static final int  NOP = 0x300001;
    private static final int  BOL = 0x300002;
    private static final int  EOL = 0x300003;
    private static final int  CCLASS = 0x300004;
    private static final int  END = 0x3FFFFF;

    private static final int  ANY_STAR = 0x400000; // shell

    private static final int  ENDMARKER = -999;

    private static final int  CCLASS_MASK_1_BYTE = 0xFF;
    private static final int  CCLASS_SIZE_1_BYTE = 4;
    private static final int  CCLASS_MASK_2_BYTE = 0xFFFF;
    private static final int  CCLASS_SIZE_2_BYTE = 1024;

    private static final int   CCLASS_SHIFTSIZE = 63;
    private static final int   CCLASS_DIVISOR = 64;
    private static final long  CCLASS_NEGATE = 0xFFFFFFFF|(0xFFFFFFFF<<32);

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    YoixRERegexp(String str) {

	this(str, SINGLE_BYTE);
    }


    public
    YoixRERegexp(String str, int reflags) {

	int token = 0;

	if (str != null && str.length() > 0) {

	    if (((reflags&SINGLE_BYTE) == SINGLE_BYTE)) {
		cclass_mask = CCLASS_MASK_1_BYTE;
		cclass_size = CCLASS_SIZE_1_BYTE;
	    } else {
		cclass_mask = CCLASS_MASK_2_BYTE;
		cclass_size = CCLASS_SIZE_2_BYTE;
	    }

	    program = new Program();
	    pattern = str;
	    shell = ((reflags&SHELL_PATTERN) == SHELL_PATTERN);
	    rawshell = ((reflags&RAWSHELL_PATTERN) == RAWSHELL_PATTERN);
	    case_insensitive = ((reflags&CASE_INSENSITIVE) == CASE_INSENSITIVE);

	    errors = 0;
	    cursubid = 0;
	    lastwasand = false;

	    startlex(pattern);
	    pushator(START-1);

	    if ((reflags&TEXT_PATTERN) == TEXT_PATTERN) {
		while ((token = lexText()) != END)
		    operand(token);
	    } else if (shell) {
		operand(BOL);
		while ((token = lexShell()) != END) {
		    if (token == ANY_STAR) {
			operand(ANY);
			operator(STAR);
		    } else if ((token&OPERAND) == OPERATOR)
			operator(token);
		    else operand(token);
		}
		operand(EOL);
	    } else if (rawshell) {
		operand(BOL);
		while ((token = lexRawShell()) != END) {
		    if (token == ANY_STAR) {
			operand(ANY);
			operator(STAR);
		    } else if ((token&OPERAND) == OPERATOR)
			operator(token);
		    else operand(token);
		}
		operand(EOL);
	    } else {
		while ((token = lex()) != END) {
		    if ((token&OPERAND) == OPERATOR)
			operator(token);
		    else operand(token);
		}
	    }

	    evaluntil(START);
	    operand(END);
	    evaluntil(START);

	    if (nbra != 0)
		error(REGEXPSYNTAXERROR);	// unmatched left paren

	    program.startinst = ((Node)andstack.peek()).first;
	    optimize();

	} else program = null;

    }

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public final boolean
    exec(String str, YoixRESubexp mp) {

	return(exec(str, mp, true));
    }


    public final boolean
    exec(String str, YoixRESubexp mp, boolean initial) {

	YoixRESubexp  sempty;
	ListMgr       lmgr;
	boolean       checkstart;
	boolean       match = false;
	Inst          inst;
	char          text[];
	int           startchar;
	int           flag;
	int           tl;
	int           nl;
	int           ch;
	int           s;
	int           i;

	if (str == null || program == null)
	    return(false);

	startchar = program.startinst.type < OPERATOR ? program.startinst.type : 0;
	checkstart = (startchar != 0);
	text = str.toCharArray();
	lmgr = new ListMgr();
	sempty = new YoixRESubexp();

	if (mp != null)
	    mp.reset();

	inst = null;
	flag = 0;
	s = 0;

	do {
	    if (s < text.length)
		ch = text[s];
	    else ch = ENDMARKER;
	    if (checkstart && !(ch == startchar || (case_insensitive && lowercase(ch) == lowercase(startchar))))
		continue;
	    tl = flag;
	    nl = flag ^= 1;
	    lmgr.ilist[0] = 0;
	    lmgr.ilist[1] = 0;
	    sempty.putSpAt(s, 0);
	    newthread(lmgr.list[tl], 0, program.startinst, sempty);
	    lmgr.list[nl][0].inst = null;
	    for (; (inst = lmgr.list[tl][lmgr.ilist[tl]].inst) != null; lmgr.ilist[tl]++) {
	    loop1: for (;;)
		switchstmt: switch (inst.type) {
		default:
		    if (inst.type == ch || (case_insensitive && lowercase(inst.type) == lowercase(ch))) {
			if (newthread(lmgr.list[nl], lmgr.ilist[nl], inst.left, lmgr.list[tl][lmgr.ilist[tl]].se) == lmgr.listsize) {
			    lmgr.grow();
			}
		    }
		    break loop1;

		case LBRA:
		    lmgr.list[tl][lmgr.ilist[tl]].se.putSpAt(s, inst.subid);
		    inst = inst.left;
		    break switchstmt;

		case RBRA:
		    lmgr.list[tl][lmgr.ilist[tl]].se.putEpAt(s, inst.subid);
		    inst = inst.left;
		    break switchstmt;

		case ANY:
		    if (newthread(lmgr.list[nl], lmgr.ilist[nl], inst.left, lmgr.list[tl][lmgr.ilist[tl]].se) == lmgr.listsize) {
			lmgr.grow();
		    }
		    break loop1;

		case BOL:
		    if (s == 0 && initial) {
			inst = inst.left;
			break switchstmt;
		    }
		    break loop1;

		case EOL:
		    if (ch == ENDMARKER || ch == '\0') {
			inst = inst.left;
			break switchstmt;
		    }
		    break loop1;

		case CCLASS:
		    if (ch>=0 && inst.cclass.isMember(ch)) {
			if (newthread(lmgr.list[nl], lmgr.ilist[nl], inst.left, lmgr.list[tl][lmgr.ilist[tl]].se) == lmgr.listsize) {
			    lmgr.grow();
			}
		    }
		    break loop1;

		case OR:
		    if (newthread(lmgr.list[tl], lmgr.ilist[tl], inst.right, lmgr.list[tl][lmgr.ilist[tl]].se) == lmgr.listsize) {
			lmgr.grow();
		    }
		    inst = inst.left;
		    break switchstmt;

		case END:
		    match = true;
		    lmgr.list[tl][lmgr.ilist[tl]].se.putEpAt(s, 0);
		    if (mp != null)
			newmatch(mp, lmgr.list[tl][lmgr.ilist[tl]].se);
		    break loop1;
		}
	    }
	    checkstart = startchar != 0 && lmgr.list[nl][lmgr.ilist[nl]].inst == null;
	} while (ch != '\0' && s++ < text.length);

	if (match && mp != null)
	    mp.setSource(str);
	return(match);
    }


    public static String
    regsub(String in, YoixRESubexp se) {

	StringBuffer  sb = null;
	String        src = null;
	String        ret = null;
	char          in_chars[] = null;
	char          ch;
	int           pos;
	int           ep;
	int           sp;
	int           i;

	if (in != null) {
	    in_chars = in.toCharArray();
	    if (se != null)
		src = se.getSource();

	    sb = new StringBuffer(in_chars.length);

	    synchronized(sb) {
		for (i = 0; i < in_chars.length; i++) {
		    ch = in_chars[i];
		    if (ch == '\\') {
			if (++i < in_chars.length) {
			    switch (ch = in_chars[i]) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				    pos = ch - '0';
				    while (++i < in_chars.length) {
					ch = in_chars[i];
					if (ch >= '0' && ch <= '9')
					    pos = pos*10 + ch - '0';
					else break;
				    }
				    i--;
				    if (se != null && pos < se.size() && src != null) {
					sp = se.getSpAt(pos);
					ep = se.getEpAt(pos);
					if (sp >= 0)
					    sb.append(src.substring(sp, ep));
				    }
				    break;

				default:	// includes "case '\\':"
				    sb.append(ch);
				    break;
			    }
			}
		    } else if (ch == '&') {
			if (se != null && 0 < se.size() && src != null) {
			    sp = se.getSpAt(0);
			    ep = se.getEpAt(0);
			    sb.append(src.substring(sp, ep));
			}
		    } else sb.append(ch);
		}

		ret = sb.toString();

	    }
	}

	return(ret);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    bldcclass() {

	CharClass  cclass;
	boolean    negate = false;
	int        c1;
	int        c2;

	cclass = newcclass();
	if (
	   eoff < exprp.length &&
	   ((!shell && exprp[eoff] == '^') || (shell && exprp[eoff] == '!'))
	   ) {
	    negate = true;
	    eoff++;
	}

	while ((c1 = c2 = nextc()) != ']') {
	    if (eoff < exprp.length && exprp[eoff] == '-') {
		if (eoff < exprp.length - 1 && exprp[eoff+1] != ']') {
		    eoff++; // eat '-'
		    c2 = nextc();
		}
	    }
	    for (c1 &= cclass_mask, c2 &= cclass_mask; c1 <= c2; c1++) {
		cclass.setBit(c1);
		if (case_insensitive)
		    cclass.setBit(uppercase(c1));
	    }
	}

	if (negate)
	    cclass.negate();
    }


    private String
    dump() {

	StringBuffer  sb;
	String        str;
	Inst          inst;
	int           i;

	sb = new StringBuffer();
	synchronized(sb) {
	    for (i = 0; i < program.instvec.size(); i++) {
		inst = (Inst)(program.instvec.elementAt(i));
		sb.append(i);
		sb.append(":\t");
		sb.append(Integer.toHexString(inst.type));
		sb.append("\t");
		sb.append(inst.left==null?-1:program.instvec.indexOf(inst.left));
		sb.append("\t");
		sb.append(inst.right==null?-1:program.instvec.indexOf(inst.right));
		sb.append("\t");
		sb.append(inst.subid);
		sb.append(NL);
	    }

	    str = sb.toString();
	}

	return(str);
    }


    private void
    error(String error) {

	if (program != null) {
	    program.cleanup();
	    program = null;
	}
	if (error != null)
	    VM.abort(error);
    }


    private void
    evaluntil(int pri) {

	Node  op1 = null;
	Node  op2 = null;
	Inst  inst1 = null;
	Inst  inst2 = null;

	while (pri == RBRA || ((Integer)atorstack.peek()).intValue() >= pri) {
	    switch (popator()) {
		case LBRA:
		    op1 = popand('(');
		    inst2 = newinst(RBRA);
		    inst2.subid = popped_subid;
		    op1.last.left = inst2;
		    inst1 = newinst(LBRA);
		    inst1.subid = popped_subid;
		    inst1.left = op1.first;
		    pushand(inst1, inst2);
		    return;

		case OR:
		    op2 = popand('|');
		    op1 = popand('|');
		    inst2 = newinst(NOP);
		    op2.last.left = inst2;
		    op1.last.left = inst2;
		    inst1 = newinst(OR);
		    inst1.right = op1.first;
		    inst1.left = op2.first;
		    pushand(inst1, inst2);
		    break;

		case CAT:
		    op2 = popand(0);
		    op1 = popand(0);
		    op1.last.left = op2.first;
		    pushand(op1.first, op2.last);
		    break;

		case STAR:
		    op2 = popand('*');
		    inst1 = newinst(OR);
		    op2.last.left = inst1;
		    inst1.right = op2.first;
		    pushand(inst1, inst1);
		    break;

		case PLUS:
		    op2 = popand('+');
		    inst1 = newinst(OR);
		    op2.last.left = inst1;
		    inst1.right = op2.first;
		    pushand(op2.first, inst1);
		    break;

		case QUEST:
		    op2 = popand('?');
		    inst1 = newinst(OR);
		    inst2 = newinst(NOP);
		    inst1.left = inst2;
		    inst1.right = op2.first;
		    op2.last.left = inst2;
		    pushand(inst1, inst2);
		    break;

		default:
		    error(REGEXPUNDEFINED);	// unknown operator
		    break;
	    }
	}
    }


    final String
    getPattern() {

	return(pattern);
    }


    private int
    lex() {

	int  c;

	c = (eoff < exprp.length) ? exprp[eoff++] : -1;

	switch (c) {
	    case '\\':
		if (eoff < exprp.length)
		    c = exprp[eoff++];
		break;
	    case -1:
		c = END;
		break;
	    case '*':
		c = STAR;
		break;
	    case '?':
		c = QUEST;
		break;
	    case '+':
		c = PLUS;
		break;
	    case '|':
		c = OR;
		break;
	    case '.':
		c = ANY;
		break;
	    case '(':
		c = LBRA;
		break;
	    case ')':
		c = RBRA;
		break;
	    case '^':
		c = BOL;
		break;
	    case '$':
		c = EOL;
		break;
	    case '[':
		c = CCLASS;
		bldcclass();
		break;
	}

	return(c);
    }


    private int
    lexRawShell() {

	int  c;

	c = (eoff < exprp.length) ? exprp[eoff++] : -1;

	switch (c) {
	    case -1:
		c = END;
		break;
	    case '*':
		c = ANY_STAR;
		break;
	    case '?':
		c = ANY;
		break;
	    case '[':
		c = CCLASS;
		bldcclass();
		break;
	}

	return(c);
    }


    private int
    lexShell() {

	int  c;

	c = (eoff < exprp.length) ? exprp[eoff++] : -1;

	switch (c) {
	    case '\\':
		if (eoff < exprp.length)
		    c = exprp[eoff++];
		break;
	    case -1:
		c = END;
		break;
	    case '*':
		c = ANY_STAR;
		break;
	    case '?':
		c = ANY;
		break;
	    case '[':
		c = CCLASS;
		bldcclass();
		break;
	}

	return(c);
    }


    private int
    lexText() {

	return((eoff < exprp.length) ? exprp[eoff++] : END);
    }


    private int
    lowercase(int c) {

	return((int)Character.toLowerCase((char)c));
    }


    private CharClass
    newcclass() {

	CharClass cc = new CharClass();

	program.chclvec.addElement(cc);
	return(cc);
    }


    private Inst
    newinst(int type) {

	Inst  inst = new Inst(type);

	program.instvec.addElement(inst);
	return(inst);
    }


    private void
    newmatch(YoixRESubexp mp, YoixRESubexp sp) {

	int  i;

	if (mp == null)
	    return;
	if (sp == null || sp.size() <= 0)
	    return;
	if (
	   mp.size() == 0
	   || sp.getSpAt(0) < mp.getSpAt(0)
	   || (sp.getSpAt(0) == mp.getSpAt(0) && sp.getEpAt(0) > mp.getEpAt(0))
	   ) {
	    for (i = 0; i < sp.size(); i++)
		mp.putAt(sp.getAt(i), i);
	    if (mp.size() > sp.size())
		mp.setSize(sp.size());
	}
    }


    private int
    newthread(IList lp[], int off, Inst ip, YoixRESubexp sep) {

	IList  p = null;
	int    i = off;

	while (i < lp.length && (p = lp[i]).inst != null) {
	    if (p.inst == ip) { // == should be OK (no need for equals)
		if (sep.getSpAt(0) < p.se.getSpAt(0)) {
		    p.se = sep.copy();
		}
		return(-1);
	    }
	    i++;
	}
	if (p == null)
	    throw(new RuntimeException("newthread encountered null list"));
	p.inst = ip;
	p.se = sep.copy();
	i++;
	if (i >= lp.length)
	    throw(new RuntimeException("newthread encountered end of list"));
	lp[i].inst = null;
	return(i);
    }


    private int
    nextc() {

	int  c = -1;

	if (eoff == exprp.length || (exprp[eoff] == '\\' && (eoff+1) == exprp.length)) {
	    error(REGEXPSYNTAXERROR);	// malformed '[]'
	} else if (exprp[eoff] == '\\') {
	    eoff++;
	    c = exprp[eoff++]|OPERATOR;
	} else c = exprp[eoff++];

	if (case_insensitive)
	    c = lowercase(c);

	return(c);
    }


    private void
    operand(int type) {

	Inst  inst;

	if (lastwasand)
	    operator(CAT);
	inst = newinst(type);
	if (type == CCLASS)
	    inst.cclass = (CharClass)(program.chclvec.lastElement());
	pushand(inst, inst);
	lastwasand = true;
    }


    private void
    operator(int type) {

	if (type == RBRA && --nbra < 0)
	    error(REGEXPSYNTAXERROR);	// unmatched right paren
	if (type == LBRA) {
	    cursubid++;
	    nbra++;
	    if (lastwasand)
		operator(CAT);
	} else evaluntil(type);

	if (type != RBRA)
	    pushator(type);
	lastwasand = false;
	if (type == STAR || type == QUEST || type == PLUS || type == RBRA)
	    lastwasand = true;
    }


    private void
    optimize() {

	Inst  inst = null;
	Inst  target = null;
	int   i;

	for (i = 0; i < program.instvec.size(); i++) {
	    inst = (Inst)program.instvec.elementAt(i);
	    if (inst.type == END) {
		if (i+1 != program.instvec.size())	// sanity check
		    error(REGEXPBADINITIALIZER);	// can't happen
		break;
	    }
	    target = inst.left;
	    while (target.type == NOP) {
		program.instvec.removeElement(target);
		target = target.left;
	    }
	    inst.left = target;
	}

	program.instvec.trimToSize();
    }


    private Node
    popand(int op) {

	if (andstack.empty()) {
	    error(REGEXPSYNTAXERROR);	// missing operand
	    Inst inst = newinst(NOP);	// sure looks like we can't get here??
	    pushand(inst, inst);
	}
	return((Node)andstack.pop());
    }


    private int
    popator() {

	if (atorstack.empty())
	    error(REGEXPSTACKUNDERFLOW);	// can't happen
	popped_subid = (((Integer)subidstack.pop()).intValue());
	return(((Integer)atorstack.pop()).intValue());
    }


    private void
    pushand(Inst f, Inst l) {

	andstack.push(new Node(f, l));
    }


    private void
    pushator(int type) {

	atorstack.push(new Integer(type));
	subidstack.push(new Integer(cursubid));
    }


    private void
    startlex(String s) {

	exprp = s.toCharArray();
	eoff = 0;
	nbra = 0;
    }


    private int
    uppercase(int c) {

	return((int)Character.toUpperCase((char)c));
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class CharClass {

	long bits[];

	CharClass() {
	    bits = new long[cclass_size];
	}

	final void
	setBit(int c) {

	    if (c > 0) {
		c &= cclass_mask;
		bits[c/CCLASS_DIVISOR] |= 1L<<(c&CCLASS_SHIFTSIZE);
	    }
	}

	final void
	negate() {

	    int  i;

	    for (i = 0; i < bits.length; i++)
		bits[i] ^= CCLASS_NEGATE;
	}

	final boolean
	isMember(int c) {

	    boolean  ret = false;

	    if (c >= 0) {
		c &= cclass_mask;
		ret = (bits[c/CCLASS_DIVISOR]&(1L<<(c&CCLASS_SHIFTSIZE))) != 0;
	    }
	    return(ret);
	}

	public final String
	toString() {

	    char  array[] = new char[bits.length * CCLASS_DIVISOR];
	    long  j;
	    int   i;
	    int   k;
	    int   l;

	    l = 0;
	    for (i = 0; i < bits.length; i++) {
		j = 1;
		for (k = 0; k < CCLASS_DIVISOR; k++) {
		    array[l] = ((bits[i]&j)==j)?'1':'0';
		    j <<= 1;
		    l++;
		}
	    }
	    return(new String(array));
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class IList {

	YoixRESubexp  se;
	Inst          inst;

	IList() {
	    inst = null;
	    se = new YoixRESubexp();
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class Inst {

	CharClass  cclass;
	Inst       left;		// next;
	Inst       right;
	int        tloop = 0;
	int        type;
	int        subid;

	Inst(int type) {

	    this.type = type;
	    subid = 0;
	    right = null;
	    left = null;
	    cclass = null;
	}

	public final synchronized String
	toString() {

	    String  result = "(suppressed due to looping)";

	    tloop++;
	    if (tloop == 1) {
		result = "Inst: type("+Integer.toHexString(type)+"), subid("+subid+"), right("+right+"), left("+left+"), cclass("+cclass+")";
	    }
	    tloop--;
	    return(result);
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class ListMgr {

	IList  list[][];
	int    ilist[];
	int    listsize;

	ListMgr() {

	    int  i;

	    list = new IList[2][];
	    ilist = new int[2];
	    listsize = LISTINCREMENT - 1;

	    list[0] = new IList[LISTINCREMENT];
	    list[1] = new IList[LISTINCREMENT];
	    for (i = 0; i < list[0].length; i++) {
		list[0][i] = new IList();
		list[1][i] = new IList();
	    }
	}

	final void
	grow() {

	    IList  nlist[];
	    int    i;

	    nlist = new IList[list[0].length + LISTINCREMENT];
	    listsize++;
	    System.arraycopy(list[0], 0, nlist, 0, listsize);
	    list[0] = nlist;
	    nlist = new IList[list[1].length + LISTINCREMENT];
	    System.arraycopy(list[1], 0, nlist, 0, listsize);
	    list[1] = nlist;
	    for (i = listsize; i < list[0].length; i++) {
		list[0][i] = new IList();
		list[1][i] = new IList();
	    }
	    listsize = list[0].length - 1;
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class Node {

	Inst first;
	Inst last;

	Node(Inst f, Inst l) {
	    first = f;
	    last = l;
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class Program {

	Vector  chclvec;
	Vector  instvec;
	Inst    startinst;

	Program() {
	    startinst = null;
	    chclvec = new Vector();
	    instvec = new Vector();
	}

	final void
	cleanup() {

	    Inst  inst;
	    int   i;

	    chclvec.removeAllElements();
	    for (i = 0; i < instvec.size(); i++) {
		inst = (Inst)instvec.elementAt(i);
		inst.right = null;
		inst.left = null;
		inst.cclass = null;
	    }
	    instvec.removeAllElements();
	}
    }
}

