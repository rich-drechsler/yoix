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
import javax.media.j3d.BranchGroup;
import javax.media.j3d.RestrictedAccessException;
import com.sun.j3d.loaders.Scene;
import att.research.yoix.*;

class BodyBranchGroup extends BodyGroup

    implements Constants

{

    //
    // Decided to keep our own copy of peer to eliminate some casting.
    //

    protected BranchGroup  branchgroup = null;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	NL_COMPILE,         $LR__,       null,
	NL_TAG,             $LR__,       $LR__,
	NL_TAGGED,          $LR__,       $LR__,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(20);

    static {
	activefields.put(NL_CAPABILITIES, new Integer(VL_CAPABILITIES));
	activefields.put(NL_COLLIDABLE, new Integer(VL_COLLIDABLE));
	activefields.put(NL_COMPILE, new Integer(VL_COMPILE));
	activefields.put(NL_COMPILED, new Integer(VL_COMPILED));
	activefields.put(NL_DEFAULTCAPABILITY, new Integer(VL_DEFAULTCAPABILITY));
	activefields.put(NL_DETACH, new Integer(VL_DETACH));
	activefields.put(NL_INTERPOLATOR, new Integer(VL_INTERPOLATOR));
	activefields.put(NL_LAYOUT, new Integer(VL_LAYOUT));
	activefields.put(NL_LIVE, new Integer(VL_LIVE));
	activefields.put(NL_LOCATION, new Integer(VL_LOCATION));
	activefields.put(NL_ORIENTATION, new Integer(VL_ORIENTATION));
	activefields.put(NL_PATH, new Integer(VL_PATH));
	activefields.put(NL_PICKABLE, new Integer(VL_PICKABLE));
	activefields.put(NL_POSITION, new Integer(VL_POSITION));
	activefields.put(NL_TAG, new Integer(VL_TAG));
	activefields.put(NL_TRANSFORM, new Integer(VL_TRANSFORM));
    }

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
     // NAME                      CAPABILITY                                  VALUE
     // ----                      ----------                                  -----
	"ALLOW_DETACH",           new Integer(BranchGroup.ALLOW_DETACH),      Boolean.TRUE,
    };

    static {
	loadCapabilities(BodyNode.class, BodyBranchGroup.class);
	loadCapabilities(BodyGroup.class, BodyBranchGroup.class);
	loadCapabilities(capabilities, BodyBranchGroup.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyBranchGroup(J3DObject data) {

	this(null, data, null);
    }


    BodyBranchGroup(BranchGroup branchgroup) {

	this(branchgroup, (J3DObject)VM.getTypeTemplate(T_BRANCHGROUP), null);
    }


    BodyBranchGroup(BranchGroup branchgroup, String tag) {

	//
	// This should only be used when the object is part of scene that
	// was created elsewhere and loaded by our SceneGraphLoader. The
	// only use probably should come from Make.yoixSceneGraphObject().
	//

	this(branchgroup, (J3DObject)VM.getTypeTemplate(T_BRANCHGROUP), tag);
    }


    BodyBranchGroup(Scene scene, int model, String prefix) {

	//
	// This should only be used when branchgroup is part of scene that
	// was created elsewhere and loaded by our SceneGraphLoader!!!
	//

	super(scene.getSceneGroup(), (J3DObject)VM.getTypeTemplate(T_BRANCHGROUP), prefix);
	buildBranchGroup(scene, model, prefix);
	setFixedSize();
	setPermissions(permissions);
    }


    private
    BodyBranchGroup(BranchGroup branchgroup, J3DObject data, String tag) {

	super(branchgroup, data, tag);
	buildBranchGroup(branchgroup);
	setFixedSize();
	setPermissions(permissions);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(BRANCHGROUP);
    }

    ///////////////////////////////////
    //
    // BodyBranchGroup Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj = null;
	int         field;

	try {
	    switch (field = activeField(name, activefields)) {
		case VL_DETACH:
		    obj = builtinDetach(name, argv);
		    break;

		default:
		    obj = executeField(field, name, argv);
		    break;
	    }
	}
	catch(RestrictedAccessException e) {
	    abort(e, name);
	}
	return(obj);
    }


    protected void
    finalize() {

	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	int  field;

	try {
	    switch (field = activeField(name, activefields)) {
		default:
		     obj = getField(field, obj);
		     break;
	    }
	}
	catch(RestrictedAccessException e) {
	    abort(e, name);
	}
	return(obj);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	int  field;

	if (obj != null) {
	    try {
	        switch (field = activeField(name, activefields)) {
		    case VL_LAYOUT:
			setLayout(obj);
			break;

		    default:
			setField(field, obj);
			break;
		}
	    }
	    catch(RestrictedAccessException e) {
		abort(e, name);
	    }
	}
	return(obj);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildBranchGroup(BranchGroup branchgroup) {

	if ((this.branchgroup = branchgroup) == null) {
	    this.branchgroup = new BranchGroup();
	    peer = this.branchgroup;
	    setField(NL_DEFAULTCAPABILITY);
	    setField(NL_TAG);
	    setField(NL_COLLIDABLE);
	    setField(NL_PICKABLE);
	    setField(NL_TRANSFORM);
	    setField(NL_ORIENTATION);
	    setField(NL_POSITION);
	    setField(NL_LAYOUT);
	}
	setField(NL_CAPABILITIES);
    }


    private void
    buildBranchGroup(Scene scene, int model, String prefix) {

	this.branchgroup = scene.getSceneGroup();
	data.forcePutObject(NL_TAGGED, MiscTag.collectScene(scene, model, prefix));
	setField(NL_CAPABILITIES);
    }


    private synchronized YoixObject
    builtinDetach(String name, YoixObject arg[]) {

	boolean  result = false;

	//
	// Definitely not right yet and it may never be. Still somehow have
	// to deal with tags and updating NL_TAGGED arrays where appropriate
	// (e.g., in the universe that contains us). Probably should remove
	// NL_DETACH field if we don't address the unresolved issues!! This
	// was added quickly and nobody currently needs it.
	//

	if (arg.length == 0) {
	    try {
		branchgroup.detach();
		result = (branchgroup.getParent() == null);
	    }
	    catch(RestrictedAccessException e) {}
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }
}

