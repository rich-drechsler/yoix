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

package att.research.yoix.j3d;
import java.util.HashMap;
import java.util.Map;
import javax.media.j3d.SceneGraphObject;
import att.research.yoix.*;

public abstract
class MiscSceneGraphObject

    implements Constants

{

    ///////////////////////////////////
    //
    // MiscSceneGraphObject Methods
    //
    ///////////////////////////////////

    public static boolean
    getBoolean(String name, SceneGraphObject target) {

	return(getBoolean(name, false, target));
    }


    public static boolean
    getBoolean(String name, boolean fail, SceneGraphObject target) {

	Object  data;
	Object  value = null;

	if (target != null && name != null) {
	    if ((data = getUserData(target)) != null) {
		if (data instanceof Map)
		    value = ((Map)data).get(name);
	    }
	}
	return(value instanceof Boolean ? ((Boolean)value).booleanValue() : fail);
    }


    public static int
    getInt(String name, int fail, SceneGraphObject target) {

	Object  data;
	Object  value = null;

	if (target != null && name != null) {
	    if ((data = getUserData(target)) != null) {
		if (data instanceof Map)
		    value = ((Map)data).get(name);
	    }
	}
	return(value instanceof Number ? ((Number)value).intValue() : fail);
    }


    public static String
    getString(String name, SceneGraphObject target) {

	Object  data;
	Object  value = null;

	if (target != null && name != null) {
	    if ((data = getUserData(target)) != null) {
		if (data instanceof Map)
		    value = ((Map)data).get(name);
	    }
	}
	return(value instanceof String ? (String)value : null);
    }


    public static YoixObject
    getYoixObject(String name, SceneGraphObject target) {

	Object  data;
	Object  value = null;

	if (target != null && name != null) {
	    if ((data = getUserData(target)) != null) {
		if (data instanceof Map)
		    value = ((Map)data).get(name);
	    }
	}
	return(value instanceof YoixObject ? (YoixObject)value : null);
    }


    public static void
    putBoolean(String name, boolean value, SceneGraphObject target) {

	Object  data;

	if (target != null && name != null) {
	    synchronized(target) {
		if ((data = getUserData(target)) != null) {
		    if (data instanceof Map)
			((Map)data).put(name, new Boolean(value));
		}
	    }
	}
    }


    public static void
    putInt(String name, int value, SceneGraphObject target) {

	Object  data;

	if (target != null && name != null) {
	    synchronized(target) {
		if ((data = getUserData(target)) != null) {
		    if (data instanceof Map)
			((Map)data).put(name, new Integer(value));
		}
	    }
	}
    }


    public static void
    putString(String name, String value, SceneGraphObject target) {

	Object  data;

	if (target != null && name != null) {
	    synchronized(target) {
		if ((data = getUserData(target)) != null) {
		    if (data instanceof Map)
			((Map)data).put(name, value);
		}
	    }
	}
    }


    public static void
    putYoixObject(String name, YoixObject value, SceneGraphObject target) {

	Object  data;

	if (target != null && name != null) {
	    synchronized(target) {
		if ((data = getUserData(target)) != null) {
		    if (data instanceof Map)
			((Map)data).put(name, value);
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static Object
    getUserData(SceneGraphObject target) {

	Object  data = null;

	if (target != null) {
	    if ((data = target.getUserData()) == null) {
		data = new HashMap();
		target.setUserData(data);
	    }
	}
	return(data);
    }
}

