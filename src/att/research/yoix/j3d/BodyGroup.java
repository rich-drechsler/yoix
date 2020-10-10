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
import java.util.Hashtable;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.ViewPlatform;
import com.sun.j3d.loaders.Scene;
import att.research.yoix.*;

public abstract
class BodyGroup extends BodyNode

{

    //
    // A table that's used to control capabilities - low level setup
    // happens once when the loadCapabilities() methods are called in
    // the static initialization block that follows the table. Current
    // implementation seems error prone because we're required to pass
    // the correct classes to loadCapabilities(), so be careful if you
    // copy this stuff to different classes!!
    //

    private static Object  capabilities[] = {
     //
     // NAME                        CAPABILITY                                             VALUE
     // ----                        ----------                                             -----
    };

    static {
	loadCapabilities(BodyNode.class, BodyGroup.class);
	loadCapabilities(capabilities, BodyGroup.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    BodyGroup(J3DObject data) {

	super(data);
    }


    BodyGroup(Group group, J3DObject data) {

	super(group, data);
    }


    BodyGroup(Group group, J3DObject data, String tag) {

	super(group, data, tag);
    }

    ///////////////////////////////////
    //
    // BodyGroup Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	super.finalize();
    }


    final synchronized void
    setLayout(YoixObject obj) {

	YoixObject  element;
	YoixObject  loaders;
	J3DObject   tagged;
	String      tag;
	Scene       scene;
	int         length;
	int         n;

	if (obj.isArray()) {
	    //
	    // Eventually may need a way to gracefully remove everything,
	    // but until then setting NL_LAYOUT more once will definitely
	    // not behave well. In fact we probably will prevent it using
	    // the permissions array, just to enforce things!!!
	    //
	    if (obj.notNull()) {
		loaders = BodySceneLoader.pickLoaders(getObject(NL_LOADERS));
		tagged = J3DObject.newJ3DDictionary(0, -1);
		length = obj.length();
		for (n = obj.offset(); n < length; n++) {
		    if ((element = obj.getObject(n)) != null) {
			if (element.notNull()) {
			    if (element.isString()) {
				//
				// Decided to ignore errors here because
				// BodySceneLoader.loadScene() supports
				// a bunch of tunable error handling.
				// 
				if ((scene = BodySceneLoader.loadScene(element.stringValue(), loaders)) != null) {
				    if ((tag = getTag()) != null)
				        element = J3DObject.newBranchGroup(scene, 1, tag);
				    else element = J3DObject.newBranchGroup(scene);
				    addChildren(element, tagged);
				}
			    } else if (addBranch(element, tagged) == false)
				VM.abort(BADVALUE, N_LAYOUT, n);
			}
		    }
		}
		tagged.setGrowable(false);
		tagged.setAccessBody(LR__);
		data.forcePutObject(NL_TAGGED, tagged);
		data.forceSetAccessElement(NL_TAGGED, LR__);
	    }
	} else VM.abort(TYPECHECK, NL_LAYOUT);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private boolean
    addBranch(YoixObject obj, J3DObject tagged) {

	BodyNode  body;
	boolean   added = false;
	Node      node;

	if (obj instanceof J3DObject) {
	    if ((node = ((J3DObject)obj).getManagedNode()) != null) {
		if ((body = ((J3DObject)obj).getBodyNode()) != null) {
		    if (!(node instanceof ViewPlatform)) {	// not allowed??
			((Group)peer).addChild(body.getTopNode());
			MiscTag.collect(obj, tagged);
			added = true;
		    }
		}
	    }
	}
	return(added);
    }


    private boolean
    addChildren(YoixObject obj, J3DObject tagged) {

	Enumeration  enm;
	boolean      added = false;
	Group        group;
	Node         child;

	if (obj instanceof J3DObject) {
	    if ((group = ((J3DObject)obj).getManagedGroup()) != null) {
		for (enm = group.getAllChildren(); enm.hasMoreElements(); ) {
		    child = (Node)enm.nextElement();
		    group.removeChild(child);
		    ((Group)peer).addChild(child);
		}
		MiscTag.collectChildren(obj, tagged);
		added = true;
	    }
	}
	return(added);
    }
}

