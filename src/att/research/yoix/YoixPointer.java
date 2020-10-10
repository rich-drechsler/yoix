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

public abstract
class YoixPointer

    implements YoixAPI,
               YoixConstants,
	       YoixInterfacePointer

{

    //
    // Objects that represent pointers always extend this class.
    //

    boolean  growable = false;
    short    flags = RWX;
    int      growto = -1;
    int      length = -1;

    //
    // A synchronization lock that should only be used by the interpreter
    // to implement synchronized statements (or functions).
    //

    private Object  lock = null;

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public final boolean
    canAccess(short perm) {

	return((flags&perm) == (perm&ACCESSMASK) || VM.canAccess(perm));
    }


    public final boolean
    canExecute() {

	return(canAccess(__X));
    }


    public final boolean
    canGrowTo(int size) {

	return(growable && (growto < 0 || size <= growto) && canWrite());
    }


    public final boolean
    canRead() {

	return(canAccess(R__));
    }


    public final boolean
    canUnlock() {

	return((flags & L___) == 0 || VM.canAccess(L___));
    }


    public final boolean
    canWrite() {

	return(canAccess(_W_));
    }


    public final int
    definedAt(String name) {

	int  index;

	return(((index = hash(name)) >= 0 && defined(index)) ? index : -1);
    }


    public final int
    getAccess() {

	return(flags & ACCESSMASK);
    }


    public final int
    getCapacity(int limit) {

	if (limit > length) {
	    if (growable) {
		if (growto >= 0 && growto < limit)
		    limit = growto;
	    } else limit = length;
	}

	return(limit);
    }


    public final boolean
    inRange(int index) {

	return(index >= 0 && (index < length || ((YoixInterfaceBody)this).length() < length));
    }

    ///////////////////////////////////
    //
    // YoixPointer Methods
    //
    ///////////////////////////////////

    final boolean
    getGrowable() {

	return(growable);
    }


    final int
    getGrowto() {

	return(growto);
    }


    final synchronized Object
    getLock() {

	if (lock == null)
	    lock = new Object();
	return(lock);
    }


    final synchronized void
    setAccess(int perm) {

	if (canUnlock())
	    flags = (short)((flags & (~ACCESSMASK)) | (perm & ACCESSMASK));
	else VM.abort(INVALIDACCESS);
    }


    final void
    setGrowable(boolean state) {

	growable = state;
    }


    final void
    setGrowto(int limit) {

	growto = limit;
    }
}

