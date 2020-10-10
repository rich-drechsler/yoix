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
import java.awt.*;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.text.*;

abstract
class YoixRegistryCursor

    implements YoixConstants

{

    private static YoixObject  registry = null;
    private static Hashtable   cursors = new Hashtable();

    ///////////////////////////////////
    //
    // YoixRegistryCursor Methods
    //
    ///////////////////////////////////

    static boolean
    addCursor(String name, Cursor cursor) {

	if (name != null && cursor != null && registry != null) {
	    synchronized(registry) {
		if (registry.defined(name) == false) {
		    VM.pushAccess(LRW_);
		    registry.setGrowable(true);
		    registry.putString(name, name);
		    VM.popAccess();
		    cursors.put(name, cursor);
		}
	    }
	} else cursor = null;

	return(cursor != null);
    }


    static Cursor
    getCursor(YoixObject obj) {

	return(getCursor(obj, null));
    }


    static Cursor
    getCursor(YoixObject obj, Object comp) {

	Cursor  cursor = null;

	if (obj != null) {
	    if (obj.notNull()) {
		if (obj.isInteger())
		    cursor = getCursor(obj.intValue(), comp);
		else if (obj.isString())
		    cursor = getCursor(obj.stringValue(), comp);
	    } else cursor = getStandardCursor(comp);
	} 
	return(cursor != null ? cursor : Cursor.getDefaultCursor());
    }


    static Cursor
    getStandardCursor(Object comp) {

	Cursor  cursor;

	if (comp instanceof TextComponent || comp instanceof JTextComponent)
	    cursor = getPredefinedCursor(Cursor.TEXT_CURSOR);
	else cursor = Cursor.getDefaultCursor();
	return(cursor);
    }


    static boolean
    isRegisteredCursor(YoixObject obj) {

	boolean  result;

	//
	// Decided to return false when obj is null or isn't a string. Not
	// completely convinced, but it's probably not a big deal because
	// right now this method is only called when obj is a string.
	//

	if (obj != null) {
	    if (obj.isString()) {
		if ((result = isStandardCursor(obj)) == false) {
		    if (registry != null && registry.defined(obj.stringValue()))
			result = true;
		}
	    } else result = false;
	} else result = false;

	return(result);
    }


    static boolean
    isStandardCursor(YoixObject obj) {

	boolean  result;

	if (obj != null) {
	    if (obj.isInteger())
		result = (obj.intValue() == V_STANDARD_CURSOR);
	    else if (obj.isNull())
		result = true;
	    else if (obj.isString())
		result = obj.stringValue().equals(N_STANDARD_CURSOR);
	    else result = false;
	} else result = false;
	return(result);
    }


    static boolean
    notStandardCursor(YoixObject obj) {

	return(!isStandardCursor(obj));
    }


    static synchronized void
    setRegistry(YoixObject dict) {

	if (registry == null && dict != null)
	    registry = dict;
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static Cursor
    getCursor(int type, Object comp) {

	return(type != V_STANDARD_CURSOR ? getPredefinedCursor(type) : getStandardCursor(comp));
    }


    private static Cursor
    getCursor(String name, Object comp) {

	YoixObject  obj;
	Cursor      cursor = null;

	if (name.equals(N_STANDARD_CURSOR) == false) {
	    if ((cursor = (Cursor)cursors.get(name)) == null) {
		if (registry != null) {
		    if ((obj = registry.getObject(name)) != null)
			cursor = getCursor(obj.intValue(), comp);
		}
	    }
	} else cursor = getStandardCursor(comp);
	return(cursor != null ? cursor : Cursor.getDefaultCursor());
    }


    private static Cursor
    getPredefinedCursor(int type) {

	Cursor  cursor;

	try {
	    cursor = Cursor.getPredefinedCursor(type);
	}
	catch(RuntimeException e) {
	    cursor = Cursor.getDefaultCursor();
	}
	return(cursor);
    }
}

