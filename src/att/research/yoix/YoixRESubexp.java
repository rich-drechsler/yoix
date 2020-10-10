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

public
class YoixRESubexp

    implements YoixAPI

{

    //
    // This class is not currently thread safe.
    //

    private Regsubexp  se[];
    private String     source;
    private int        used;

    private static final int  NSUBEXP = 10;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    YoixRESubexp() {

	this(NSUBEXP);
    }


    public
    YoixRESubexp(int sz) {

	se = new Regsubexp[sz];
	used = 0;
	source = null;
    }

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public final int
    getEpAt(int exp) {

	int  pos = -1;

	if (exp < se.length) {
	    if (exp < used)
		pos = se[exp].ep;
	}
	return(pos);
    }


    public final int[]
    getMatches() {

	return(getMatches(0));
    }


    public final int[]
    getMatches(int offset) {

	int  matches[] = new int[2*used];
	int  n;

	for (n = 0; n < used; n++) {
	    matches[2*n] = se[n].sp + offset;
	    matches[2*n+1] = se[n].ep + offset;
	}
	return(matches);
    }


    public final int
    getSpAt(int exp) {

	int  pos = -1;

	if (exp < se.length) {
	    if (exp < used)
		pos = se[exp].sp;
	}
	return(pos);
    }


    public final void
    reset() {

	used = 0;
	source = null;
    }

    ///////////////////////////////////
    //
    // YoixRESubexp Methods
    //
    ///////////////////////////////////

    final YoixRESubexp
    copy() {

	YoixRESubexp  ns = new YoixRESubexp(used);
	int           i;

	for (i = 0; i < used; i++)
	    ns.se[i] = new Regsubexp(se[i].sp, se[i].ep);
	ns.used = used;

	return(ns);
    }


    final Regsubexp
    getAt(int exp) {

	if (exp >= used)
	    throw(new ArrayIndexOutOfBoundsException(exp));
	return(se[exp]);
    }


    final String
    getSource() {

	return(source);
    }


    final void
    putAt(Regsubexp rse, int exp) {

	Regsubexp  nse[];

	if (exp > used) {
	    throw(new ArrayIndexOutOfBoundsException(exp));
	}
	if (exp >= se.length) {
	    nse = new Regsubexp[exp+NSUBEXP];
	    System.arraycopy(se, 0, nse, 0, se.length);
	    se = nse;
	}
	if (exp == used) {
	    used++;
	    if (se[exp] == null)
		se[exp] = new Regsubexp();
	}
	se[exp].sp = rse.sp;
	se[exp].ep = rse.ep;
    }


    final void
    putEpAt(int pos, int exp) {

	if (exp >= used)
	    setSize(exp+1);
	se[exp].ep = pos;
    }


    final void
    putSpAt(int pos, int exp) {

	if (exp >= used)
	    setSize(exp+1);
	se[exp].sp = pos;
    }


    final void
    setSize(int sz) {

	Regsubexp  nse[];
	int        i;

	if (sz <= used) {
	    if (sz <= 0)
		used = 0;
	    else used = sz;
	} else {
	    if (sz >= se.length) {
		nse = new Regsubexp[sz+NSUBEXP];
		System.arraycopy(se, 0, nse, 0, se.length);
		se = nse;
	    }
	    for (i = used; i < sz; i++) {
		if (se[i] == null)
		    se[i] = new Regsubexp();
		else {
		    se[i].sp = -1;
		    se[i].ep = -1;
		}
	    }
	    used = sz;
	}
    }


    final void
    setSource(String src) {

	source = src;
    }


    final int
    size() {

	return(used);
    }


    public final String
    toString() {

	StringBuffer  sb = new StringBuffer("YoixRESubexp[");
	int           i;

	sb.append(used);
	sb.append("]=(");
	for (i = 0; i < used; i++) {
	    if (i != 0)
		sb.append(';');
	    sb.append(se[i].sp);
	    sb.append(',');
	    sb.append(se[i].ep);
	}
	sb.append(')');
	return(sb.toString());
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class Regsubexp {

	int  sp;
	int  ep;

	Regsubexp() {
	    sp = -1;
	    ep = -1;
	}

	Regsubexp(int sp, int ep) {
	    this.sp = sp;
	    this.ep = ep;
	}
    }
}

