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
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Locale;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import att.research.yoix.*;

abstract
class BodyNode extends BodySceneGraphObject

    implements Constants

{

    //
    // An array used to manage the TransformGroups that are associated with
    // this node.
    //

    private TransformGroup  transformgroups[] = {null, null, null, null};

    private static final int  INTERPOLATORGROUP = 0;
    private static final int  TRANSFORMGROUP = 1;
    private static final int  ORIENTATIONGROUP = 2;
    private static final int  POSITIONGROUP = 3;

    //
    // Node specific compile control - currently only set by Yoix versions
    // of BranchGroup and ViewPlatform. All others use the VirtualUniverse
    // setting of compile.
    //

    private boolean  compile = false;

    //
    // Special table used to control capabilities...
    //

    private static Object  capabilities[] = {
     //
     // NAME                           CAPABILITY                                      VALUE
     // ----                           ----------                                      -----
	"ALLOW_COLLIDABLE_READ ",      new Integer(Node.ALLOW_COLLIDABLE_READ),        null,
	"ALLOW_COLLIDABLE_WRITE ",     new Integer(Node.ALLOW_COLLIDABLE_WRITE),       null,
	"ALLOW_PICKABLE_READ",         new Integer(Node.ALLOW_PICKABLE_READ),          null,
	"ALLOW_PICKABLE_WRITE",        new Integer(Node.ALLOW_PICKABLE_WRITE),         null,
	"ALLOW_LOCAL_TO_VWORLD_READ",  new Integer(Node.ALLOW_LOCAL_TO_VWORLD_READ),   null,
    };

    static {
	loadCapabilities(capabilities, BodyNode.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyNode(J3DObject data) {

	super(data);
    }


    BodyNode(Node node, J3DObject data) {

	super(node, data);
    }


    BodyNode(Node node, J3DObject data, String tag) {

	super(node, data);
	buildNode(node, tag);
    }

    ///////////////////////////////////
    //
    // BodyNode Methods
    //
    ///////////////////////////////////

    final synchronized boolean
    addTo(Locale locale, boolean compile) {

	BranchGroup  branch;
	boolean      added = false;
	Node         top;

	//
	// Handling Locale additions and BranchGroup compilation here in a
	// synchronized method should give us the control that we need to
	// guarantee consistent answers from isLive() and isCompiled() that
	// are also synchronized. Probably not a big issue, but this was
	// pretty easy and should prevent suprises.
	//

	if (locale != null) {
	    if ((top = getTopNode()) != null) {
		if (!(top instanceof BranchGroup)) {
		    branch = new BranchGroup();
		    branch.addChild(top);
		} else branch = (BranchGroup)top;
		if (compile || this.compile) {
		    if (branch.isCompiled() == false)
			branch.compile();
		}
		locale.addBranchGraph(branch);
		added = true;
	    }
	}
	return(added);
    }


    protected void
    finalize() {

	transformgroups = null;
	super.finalize();
    }


    protected YoixObject
    getField(int field, YoixObject obj) {

	switch (field) {
	    case VL_COLLIDABLE:
		obj = getCollidable(obj);
		break;

	    case VL_COMPILE:
		obj = getCompile(obj);
		break;

	    case VL_LOCATION:
		obj = getLocation(obj);
		break;

	    case VL_ORIENTATION:
		obj = getOrientation(obj);
		break;

	    case VL_PICKABLE:
		obj = getPickable(obj);
		break;

	    case VL_POSITION:
		obj = getPosition(obj);
		break;

	    case VL_TAG:
		obj = getTag(obj);
		break;

	    case VL_TRANSFORM:
		obj = getTransform(obj);
		break;

	    default:
		obj = super.getField(field, obj);
		break;
	}
	return(obj);
    }


    final Material
    getMaterial() {

	Appearance  appearance;
	J3DObject   obj;
	Material    material;

	if ((obj = getJ3DObject(NL_APPEARANCE)) != null) {
	    if ((appearance = obj.getManagedAppearance()) != null)
		material = appearance.getMaterial();
	    else material = null;
	} else material = null;

	return(material);
    }


    final synchronized Node
    getTopNode() {

	Node  top;
	int   n;

	top = (Node)peer;
	for (n = transformgroups.length - 1; n >= 0; n--) {
	    if (transformgroups[n] != null) {
		top = transformgroups[n];
		break;
	    }
	}
	return(top);
    }


    final synchronized TransformGroup
    getInterpolatorGroup() {

	return(getTransformGroup(INTERPOLATORGROUP));
    }


    final String
    getTag() {

	return(MiscSceneGraphObject.getString(NL_TAG, peer));
    }


    protected YoixObject
    setField(int field, YoixObject obj) {

	if (obj != null) {
	    switch (field) {
		case VL_COLLIDABLE:
		    setCollidable(obj);
		    break;

		case VL_COMPILE:
		    setCompile(obj);
		    break;

		case VL_INTERPOLATOR:
		    setInterpolator(obj);
		    break;

		case VL_ORIENTATION:
		    setOrientation(obj);
		    break;

		case VL_PICKABLE:
		    setPickable(obj);
		    break;

		case VL_POSITION:
		    setPosition(obj);
		    break;

		case VL_TAG:
		    setTag(obj);
		    break;

		case VL_TRANSFORM:
		    setTransform(obj);
		    break;

		default:
		    super.setField(field, obj);
		    break;
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
    buildNode(Node node, String tag) {

	int  n;

	//
	// The tag string should only be non-null when node was created by
	// a SceneLoader that read an external model file. In that case we
	// decided to force the creation of all associated TransformGroups,
	// just to be sure they'll be there if we try to use them. Expect
	// we'll eventually try to tune the behavior.
	//

	if (tag != null) {
	    MiscSceneGraphObject.putString(NL_TAG, tag, node);
	    for (n = 0; n < transformgroups.length; n++)
		getTransformGroup(n);
	}
    }


    private synchronized YoixObject
    getCollidable(YoixObject obj) {

	return(YoixObject.newInt(((Node)peer).getCollidable()));
    }


    private YoixObject
    getCompile(YoixObject obj) {

	return(YoixObject.newInt(compile));
    }


    private synchronized YoixObject
    getLocation(YoixObject obj) {

	Transform3D  t;
	Point3d      point;

	if (peer.isLive()) {
	    t = new Transform3D();
	    point = new Point3d();
	    ((Node)peer).getLocalToVworld(t);
	    t.transform(point);
	} else point = null;
	return(J3DObject.newPoint3D(point));
    }


    private synchronized YoixObject
    getOrientation(YoixObject obj) {

	TransformGroup  tg;

	//
	// This is good enough for now, but we eventually will extract
	// the location directly from the TransformGroup - later.
	//

	if ((tg = getTransformGroup(ORIENTATIONGROUP)) != null) {
	    if ((obj = getObject(NL_ORIENTATION)) == null || obj.isNull())
		obj = J3DObject.newEulerAngle(0, 0, 0);
	} else obj = J3DObject.newEulerAngle(null);

	return(obj);
    }


    private synchronized YoixObject
    getPickable(YoixObject obj) {

	return(YoixObject.newInt(((Node)peer).getPickable()));
    }


    private synchronized YoixObject
    getPosition(YoixObject obj) {

	TransformGroup  tg;

	//
	// This is good enough for now, but we eventually will extract
	// the location directly from the TransformGroup - later.
	//

	if ((tg = getTransformGroup(POSITIONGROUP)) != null) {
	    if ((obj = getObject(NL_POSITION)) == null || obj.isNull())
		obj = J3DObject.newPoint3D(0, 0, 0);
	} else obj = J3DObject.newPoint3D(null);

	return(obj);
    }


    private YoixObject
    getTag(YoixObject obj) {

	return(YoixObject.newString(getTag()));
    }


    private synchronized YoixObject
    getTransform(YoixObject obj) {

	TransformGroup  tg;
	Transform3D     t;

	if ((tg = getTransformGroup(TRANSFORMGROUP)) != null) {
	    t = new Transform3D();
	    tg.getTransform(t);
	} else t = null;

	return(J3DObject.newTransform3D(t));
    }


    private synchronized TransformGroup
    getTransformGroup(int index) {

	TransformGroup  tg = null;
	Group           parent;
	Node            child;
	int             n;

	if (index >= 0 && index < transformgroups.length) {
	    if ((tg = (TransformGroup)transformgroups[index]) == null) {
		if (peer.isLive() == false && peer.isCompiled() == false) {
		    child = (Node)peer;
		    for (n = index - 1; n >= 0; n--) {
			if (transformgroups[n] != null) {
			    child = transformgroups[n];
			    break;
			}
		    }
		    tg = new TransformGroup();
		    transformgroups[index] = tg;
		    setTransformGroupCapabilities(index);
		    if ((parent = (Group)child.getParent()) != null) {
			if ((index = parent.indexOfChild(child)) >= 0)
			    parent.setChild(tg, index);
		    }
		    tg.addChild(child);
		}
	    }
	}
	return(tg);
    }


    private synchronized void
    setCollidable(YoixObject obj) {

	((Node)peer).setCollidable(obj.booleanValue());
    }


    private synchronized void
    setCompile(YoixObject obj) {

	if (((Node)peer).isLive() == false)
	    compile = obj.booleanValue();
    }


    private synchronized void
    setInterpolator(YoixObject obj) {

	TransformGroup  tg;
	YoixObject      element;
	J3DObject       interpolator;
	int             off;
	int             len;
	int             n;

	//
	// We're currently silent if the TransformGroup can't be updated,
	// but we could abort or issue a warning that could also be tuned
	// by an errormodel setting??
	//

	if (obj.isArray() || obj.isNull() || J3DObject.isInterpolator(obj)) {
	    if (obj.notNull()) {
		if ((tg = getTransformGroup(INTERPOLATORGROUP)) != null) {
		    if (obj.isArray()) {
			off = obj.offset();
			len = obj.length();
			for (n = off; n < len; n++) {
			    element = obj.get(n, false);
			    if (J3DObject.isInterpolator(element))
				((J3DObject)element).getBodyInterpolator().setTarget(new J3DObject(this));
			    else VM.abort(BADVALUE, NL_INTERPOLATOR, n - off);
			}
		    } else ((J3DObject)obj).getBodyInterpolator().setTarget(new J3DObject(this));
		}
	    } else {
		if ((interpolator = getJ3DObject(NL_INTERPOLATOR)) != null) {
		    if (interpolator.isArray()) {
			off = interpolator.offset();
			len = interpolator.length();
			for (n = off; n < len; n++) {
			    element = interpolator.get(n, false);
			    ((J3DObject)element).getBodyInterpolator().setTarget(null);
			}
		    } else interpolator.getBodyInterpolator().setTarget(null);
		}
	    }
	} else VM.abort(TYPECHECK, NL_INTERPOLATOR);
    }


    private synchronized void
    setOrientation(YoixObject obj) {

	TransformGroup  tg;
	Transform3D     t;
	AxisAngle4d     axisangle;
	Vector3d        vector;
	Quat4d          q4d;
	double          dbl;
	double          sin;
	double          cos;

	//
	// We're currently silent if the TransformGroup can't be updated,
	// but we could abort or issue a warning that could also be tuned
	// by an errormodel setting??
	//

	if (J3DObject.isOrientation3D(obj) || obj.isNull()) {
	    if (obj.notNull()) {
		if ((tg = getTransformGroup(ORIENTATIONGROUP)) != null) {
		    if (J3DObject.isAxisAngle(obj)) { // can it ever be null?
			if ((axisangle = Make.javaAxisAngle4d(obj)) != null) {
			    t = new Transform3D();
			    t.setRotation(axisangle);
			    tg.setTransform(t);
			}
		    } else if (J3DObject.isEulerAngle(obj)) {
			if ((vector = Make.javaVector3d(obj)) != null) { // can it ever be null?
			    t = new Transform3D();
			    t.setEuler(vector);
			    tg.setTransform(t);
			}
		    } else if (J3DObject.isQuat4D(obj)) {
			if ((q4d = Make.javaQuat4d(obj)) != null) { // can it ever be null?
			    t = new Transform3D();
			    t.set(q4d);
			    tg.setTransform(t);
			}
		    } else if (obj.isNumber()) { // rotation about Y-axis
			dbl = YoixMisc.toRadians(obj.doubleValue());
			sin = Math.sin(dbl);
			cos = Math.cos(dbl);
			t = new Transform3D(
			    new double[] {
				cos,  0.0, sin, 0.0,
				0.0,  1.0, 0.0, 0.0,
				-sin, 0.0, cos, 0.0,
				0.0,  0.0, 0.0, 1.0,
			    }
			);
			tg.setTransform(t);
		    }
		}
	    }
	} else VM.abort(TYPECHECK, NL_ORIENTATION);
    }


    private synchronized void
    setPickable(YoixObject obj) {

	((Node)peer).setCollidable(obj.booleanValue());
    }


    private synchronized void
    setPosition(YoixObject obj) {

	TransformGroup  tg;
	Transform3D     t;
	Vector3d        vector;

	//
	// We're currently silent if the TransformGroup can't be updated,
	// but we could abort or issue a warning that could also be tuned
	// by an errormodel setting??
	//

	if (obj.notNull()) {
	    if ((tg = getTransformGroup(POSITIONGROUP)) != null) {
		if ((vector = Make.javaVector3d(obj)) != null) {
		    t = new Transform3D();
		    t.setTranslation(vector);
		    tg.setTransform(t);
		}
	    }
	}
    }


    private void
    setTag(YoixObject obj) {

	//
	// Subclasses should always explicitly remove write permission from
	// this field, so we can assume this only happens once.
	//

	if (obj.notNull())
	    MiscSceneGraphObject.putString(NL_TAG, obj.stringValue(), peer);
    }


    private synchronized void
    setTransform(YoixObject obj) {

	TransformGroup  tg;
	Transform3D     t;

	//
	// We're currently silent if the TransformGroup can't be updated,
	// but we could abort or issue a warning that could also be tuned
	// by an errormodel setting??
	//

	if (obj.notNull()) {
	    if ((tg = getTransformGroup(TRANSFORMGROUP)) != null) {
		if ((t = Make.javaTransform3D(obj)) != null)
		    tg.setTransform(t);
	    }
	}
    }


    private void
    setTransformGroupCapabilities(int index) {

	TransformGroup  tg;
	int             setting;

	if (index >= 0 && index < transformgroups.length) {
	    if ((tg = transformgroups[index]) != null) {
		switch (index) {
		    case INTERPOLATORGROUP:
			setting = 2;
			break;

		    default:
			setting = getCapabilitySetting();
			break;
		}
		changeCapabilitySetting(tg, TransformGroup.ALLOW_TRANSFORM_READ, setting);
		changeCapabilitySetting(tg, TransformGroup.ALLOW_TRANSFORM_WRITE, setting);
	    }
	}
    }
}

