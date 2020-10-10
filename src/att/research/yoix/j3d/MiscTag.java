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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.SceneGraphObject;
import com.sun.j3d.loaders.Scene;
import att.research.yoix.*;

public abstract
class MiscTag

    implements Constants

{

    ///////////////////////////////////
    //
    // MiscTag Methods
    //
    ///////////////////////////////////

    static J3DObject
    collect(YoixObject src, J3DObject dest) {

	String  tag;

	if ((tag = src.getString(NL_TAG)) != null && tag.length() > 0) {
	    if (dest.defined(tag) == false)
		dest.put(tag, src);
	    else if (src.bodyEquals(dest.getObject(tag)) == false)
		VM.abort(NAMECLASH, NL_TAG, new String[] {tag + " is already defined"});
	}
	return(collectChildren(src, dest));
    }


    static J3DObject
    collectChildren(YoixObject src, J3DObject dest) {

	return(copyTagged(src.getObject(NL_TAGGED), dest));
    }


    static J3DObject
    collectScene(Scene scene, int model, String prefix) {

	return(copyTagged(scene, model, prefix, J3DObject.newJ3DDictionary(0, -1)));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static HashMap
    collectObjects(Scene scene, int model, String prefix) {

	BranchGroup  branch;
	HashMap      tagged = null;
	String       path;
	Map          named;
	int          counter[] = {0};

	if (scene != null) {
	    branch = scene.getSceneGroup();
	    path = MiscSceneGraphObject.getString(NL_PATH, branch);
	    if ((named = scene.getNamedObjects()) != null) {
		if (branch != null)		// otherwise Hashtable may complain
		    named.remove(branch);
	    }
	    switch (model) {
		case 1:
		    tagged = copyObjects(
			named,
			prefix,
			counter,
			path,
			new HashMap()
		    );
		    break;

		case 2:
		    tagged = collectObjects(
			scene.getSceneGroup().getAllChildren(),
			prefix,
			counter,
			invertMap(named),
			path,
			new HashMap()
		    );
		    break;

		case 3:
		    named = invertMap(named);
		    tagged = collectObjects(
			scene.getSceneGroup().getAllChildren(),
			prefix,
			counter,
			(HashMap)named,
			path,
			new HashMap()
		    );
		    copyObjects(invertMap(named), prefix, counter, path, tagged);
		    break;
	    }
	}
	return(tagged);
    }


    private static HashMap
    collectObjects(Enumeration enm, String prefix, int counter[], HashMap named, String path, HashMap dest) {

	String  name;
	String  tag;
	Node    child;

	if (dest != null) {
	    while (enm.hasMoreElements()) {
		if ((child = (Node)enm.nextElement()) != null) {
		    if (child instanceof Group) {
			collectObjects(
			    ((Group)child).getAllChildren(),
			    prefix,
			    counter,
			    named,
			    path,
			    dest
			);
		    }
		    if (named != null) {
			if ((name = (String)named.get(child)) == null) {
			    if (prefix == null)
				tag = generateTag(path, null, counter);
			    else tag = generateTag(prefix, null, counter);
			} else tag = generateTag(prefix, name, counter);
			named.remove(child);
		    } else {
			if (prefix == null)
			    tag = generateTag(path, null, counter);
			else tag = generateTag(prefix, null, counter);
		    }
		    dest.put(tag, child);
		    MiscSceneGraphObject.putString(NL_PATH, path, child);
		}
	    }
	}
	return(dest);
    }


    private static HashMap
    copyObjects(Map src, String prefix, int counter[], String path, HashMap dest) {

	Iterator  iterator;
	Object    key;
	Object    value;
	String    name;

	//
	// NOTE - this assumes the src map is not inverted!!
	//

	if (src != null && dest != null) {
	    for (iterator = src.keySet().iterator(); iterator.hasNext(); ) {
		if ((key = iterator.next()) != null) {
		    if (key instanceof String) {
			value = src.get(key);
			if (value instanceof SceneGraphObject) {
			    name = generateTag(prefix, (String)key, counter);
			    dest.put(name, value);
			    MiscSceneGraphObject.putString(NL_PATH, path, (SceneGraphObject)value);
			}
		    }
		}
	    }
	}
	return(dest);
    }


    private static J3DObject
    copyTagged(YoixObject src, J3DObject dest) {

	YoixObject  element;
	String      tag;
	int         length;
	int         n;

	if (src != null) {
	    length = src.length();
	    for (n = src.offset(); n < length; n++) {
		if ((element = src.getObject(n)) != null) {
		    tag = src.name(n);
		    if (dest.defined(tag) == false)
			dest.put(tag, element, false);
		    else if (element.bodyEquals(dest.getObject(tag)) == false)
			VM.abort(NAMECLASH, NL_TAG, new String[] {tag + " is already defined"});
		}
	    }
	}
	return(dest);
    }


    private static J3DObject
    copyTagged(Scene scene, int model, String prefix, J3DObject dest) {

	YoixObject  element;
	Iterator    iterator;
	HashMap     tagged;
	Object      key;
	Object      value;

	if ((tagged = collectObjects(scene, model, prefix)) != null) {
	    for (iterator = tagged.keySet().iterator(); iterator.hasNext(); ) {
		if ((key = iterator.next()) != null) {
		    if (key instanceof String) {
			value = tagged.get(key);
			if (value instanceof SceneGraphObject) {
			    value = Make.yoixSceneGraphObject(
				scene,
				(SceneGraphObject)value,
				(String)key
			    );
			    tagged.put(key, value);
			} else tagged.remove(key);
		    } else tagged.remove(key);
		}
	    }
	    dest = copyTagged(YoixMisc.copyIntoDictionary(tagged), dest);
	}
	return(dest);
    }


    private static String
    generateTag(String prefix, String name, int counter[]) {

	String  tag;

	if (name == null) {
	    if (prefix == null || prefix.length() == 0)
		tag = "$_" + counter[0]++;
	    else tag = prefix + "_" + counter[0]++;
	} else {
	    if (prefix != null && prefix.length() > 0)
		tag = prefix + "_" + name;
	    else tag = name;
	}
	return(tag);
    }


    private static HashMap
    invertMap(Map map) {

	ArrayList  list;
	Iterator   iterator;
	HashMap    inverted = null;
	HashMap    duplicates;
	Object     key;
	Object     value;

	//
	// Written exclusively for the Scene.getNamedObjects() Hashtable,
	// but it's general enough that it probably should be public and
	// moved to a different class - maybe later.
	//

	if (map != null) {
	    inverted = new HashMap(map.size());
	    duplicates = new HashMap();
	    for (iterator = map.keySet().iterator(); iterator.hasNext(); ) {
		if ((key = iterator.next()) != null) {
		    if ((value = map.get(key)) != null) {
			if (inverted.containsKey(value)) {
			    if ((list = (ArrayList)duplicates.get(value)) == null) {
				list = new ArrayList();
				list.add(inverted.get(value));
				inverted.put(value, list);
				duplicates.put(value, list);
			    }
			    list.add(key);
			} else inverted.put(value, key);
		    }
		}
	    }
	}
	return(inverted);
    }
}

