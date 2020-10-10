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
import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Leaf;
import javax.media.j3d.Locale;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.ViewPlatform;
import javax.media.j3d.VirtualUniverse;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import com.sun.j3d.loaders.Scene;
import att.research.yoix.*;

public
class BodyVirtualUniverse extends J3DPointerActive

    implements Constants

{

    private VirtualUniverse  virtualuniverse = null;
    private Locale           locale = null;

    private Background  background = null;
    private Color3f     backgroundcolor = null;
    private Image       backgroundimage = null;

    //
    // This is only created and used when a VirtualUniverse is assigned to
    // the "owner" field (name probably be changed to "view") in a Canvas3D.
    // Work is done in setCanvas() below - notice that we add a reference
    // in the NL_TAGGED dictionary to the newly created ViewPlatform under
    // the name TAG_VIEWPLATFORM, but we currently don't make sure it's
    // not already defined - need to think about the right approach.
    //

    private BodyViewPlatform  defaultview = null;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	NL_COMPILE,         $LR__,       null,
	NL_LAYOUT,          $LR__,       $LR__,		// hopefully temporary??
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(10);

    static {
	activefields.put(NL_BACKGROUND, new Integer(VL_BACKGROUND));
	activefields.put(NL_BACKGROUNDHINTS, new Integer(VL_BACKGROUNDHINTS));
	activefields.put(NL_BACKGROUNDIMAGE, new Integer(VL_BACKGROUNDIMAGE));
	activefields.put(NL_LAYOUT, new Integer(VL_LAYOUT));
	activefields.put(NL_PRIORITY, new Integer(VL_PRIORITY));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    BodyVirtualUniverse(J3DObject data) {

	super(data);
	buildVirtualUniverse();
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

	return(VIRTUALUNIVERSE);
    }

    ///////////////////////////////////
    //
    // BodyVirtualUniverse Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    default:
		obj = null;
		break;
	}
	return(obj);
    }


    protected void
    finalize() {

	cleanup();
	defaultview = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case VL_PRIORITY:
		obj = YoixObject.newInt(VirtualUniverse.getJ3DThreadPriority());
		break;

	    default:
		break;
	}
	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(virtualuniverse);
    }


    final synchronized void
    setCanvas(J3DCanvas3D canvas) {

	J3DObject  tagged;
	J3DObject  obj;

	if (canvas != null) {
	    if (defaultview == null) {
		obj = J3DObject.newViewPlatform(virtualuniverse);
		defaultview = obj.getBodyViewPlatform();
		defaultview.addTo(locale, getBoolean(NL_COMPILE, false));
		tagged = (J3DObject)data.getObject(NL_TAGGED);
		tagged.setGrowable(true);
		tagged.forcePutObject(TAG_VIEWPLATFORM, obj);
		tagged.setGrowable(false);
	    }
	    defaultview.setCanvas(canvas);
	    canvas.setUniverse(this);
	}
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case VL_BACKGROUND:
		    setBackground(obj);
		    break;

		case VL_BACKGROUNDHINTS:
		    setBackgroundHints(obj);
		    break;

		case VL_BACKGROUNDIMAGE:
		    setBackgroundImage(obj);
		    break;

		case VL_LAYOUT:
		    setLayout(obj);
		    break;

		case VL_PRIORITY:
		    setPriority(obj);
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

    private synchronized boolean
    addBranch(YoixObject obj, J3DObject tagged) {

	BodyNode  body;
	boolean   added = false;

	if (obj instanceof J3DObject) {
	    if ((body = ((J3DObject)obj).getBodyNode()) != null) {
		body.addTo(locale, getBoolean(NL_COMPILE, false));
		if (body instanceof BodyViewPlatform)
		    ((BodyViewPlatform)body).setUniverse(this);
		MiscTag.collect(obj, tagged);
		added = true;
	    }
	}
	return(added);
    }


    private void
    buildVirtualUniverse() {

	BoundingSphere  bs;
	BranchGroup     branch;

	//
	// Wouldn't be surprised if the background stuff has to change. It
	// was added very early and we still haven't taken another look at
	// it - later.
	//

	virtualuniverse = new VirtualUniverse();
	locale = new Locale(virtualuniverse);

	bs = new BoundingSphere();
	bs.setRadius(Double.POSITIVE_INFINITY);

	background = new Background();
	background.setApplicationBounds(bs);
	background.setCapability(Background.ALLOW_COLOR_READ);
	background.setCapability(Background.ALLOW_COLOR_WRITE);
	background.setCapability(Background.ALLOW_IMAGE_READ);
	background.setCapability(Background.ALLOW_IMAGE_WRITE);
	background.setCapability(Background.ALLOW_IMAGE_SCALE_MODE_READ);
	background.setCapability(Background.ALLOW_IMAGE_SCALE_MODE_WRITE);

	backgroundcolor = new Color3f();
	background.getColor(backgroundcolor);

	branch = new BranchGroup();
	branch.addChild(background);
	locale.addBranchGraph(branch);

	setField(NL_BACKGROUND);
	setField(NL_BACKGROUNDHINTS);
	setField(NL_BACKGROUNDIMAGE);
	setField(NL_LAYOUT);
	setField(NL_PRIORITY);
    }


    private void
    cleanup() {

	VirtualUniverse  universe;

	//
	// Is there more we need to do??
	//

	if ((universe = virtualuniverse) != null) {
	    universe.removeAllLocales();
	    locale = null;
	    virtualuniverse = null;
	}
    }


    private synchronized void
    setBackground(YoixObject obj) {

	background.setColor(Make.javaColor3f(obj, backgroundcolor));
    }


    private  synchronized void
    setBackgroundHints(YoixObject obj) {

	int  mode;

	switch (obj.intValue()) {
	    case YOIX_SCALE_DEFAULT:
		mode = Background.SCALE_FIT_ALL;
		break;

	    case YOIX_SCALE_NONE:
		mode = Background.SCALE_NONE;
		break;

	    case YOIX_SCALE_TILE:
		mode = Background.SCALE_REPEAT;
		break;

	    default:
		mode = Background.SCALE_FIT_ALL;
		break;
	}
	background.setImageScaleMode(mode);
    }


    private synchronized void
    setBackgroundImage(YoixObject obj) {

	background.setImage(Make.javaImageComponent2D(obj));
    }


    private synchronized void
    setLayout(YoixObject obj) {

	YoixObject  element;
	YoixObject  loaders;
	J3DObject   tagged;
	Scene       scene;
	int         length;
	int         n;

	//
	// We briefly considered accepting more than just arrays, but it
	// seemed confusing so the experiment was tossed. Should be easy
	// to restore if you want.
	//

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
			    //
			    // Eventually may accept more - for example
			    // a dictionary would be an easy way to pass 
			    // extra info (e.g., prefix or model) to the
			    // newBranchGroup() method. Alternative right
			    // now, which isn't unreasonable, is to put
			    // an appropriate BranchGroup in layout.
			    //
			    if (element.isString()) {
				//
				// Decided to ignore errors here because
				// BodySceneLoader.loadScene() supports
				// a bunch of tunable error handling.
				// 
				if ((scene = BodySceneLoader.loadScene(element.stringValue(), loaders)) != null) {
				    element = J3DObject.newBranchGroup(scene);
				    addBranch(element, tagged);
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


    private synchronized void
    setPriority(YoixObject obj) {

	Thread  currentthread;
	int     currentpriority;
	int     priority;

	//
	// Documentation claims new priority can't be higher than that of
	// the calling thread, otherwise setJ3DThreadPriority() throws a
	// SecurityException. Decided, at least for now, to try to remove
	// that restriction by temporarily raising the priority of this
	// thread when necessary - not completely convinced??
	//

        priority = Math.max(Math.min(obj.intValue(), Thread.MAX_PRIORITY), Thread.MIN_PRIORITY);

	if (priority != VirtualUniverse.getJ3DThreadPriority()) {
	    currentthread = Thread.currentThread();
	    currentpriority = currentthread.getPriority();
	    if (priority > currentpriority) {
		try {
		    currentthread.setPriority(priority);
		    VirtualUniverse.setJ3DThreadPriority(priority);
		}
		finally {
		    currentthread.setPriority(currentpriority);
		}
	    } else VirtualUniverse.setJ3DThreadPriority(priority);
	}
    }
}

