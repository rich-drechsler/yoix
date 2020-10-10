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
import java.awt.event.*;
import java.util.Hashtable;
import java.util.Vector;
import att.research.yoix.*;

final
class BodyComponentAWT extends YoixBodyComponentAWT

    implements Constants

{

    //
    // Decided we should keep a reference to data (as a J3DObject) just to
    // make coding a little easier. The alternative is to use getData() and
    // casting.
    //

    protected J3DObject  data;

    //
    // An array used to set permissions on some of the fields that
    // users should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
    };

    //
    // The activefields Hashtable translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static Hashtable  activefields = new Hashtable(20);

    static {
	activefields.put(NL_ALIVE, new Integer(VL_ALIVE));
	activefields.put(NL_DOUBLEBUFFERED, new Integer(VL_DOUBLEBUFFERED));
	activefields.put(NL_OFFSCREENBUFFER, new Integer(VL_OFFSCREENBUFFER));
	activefields.put(NL_PRERENDER, new Integer(VL_PRERENDER));
	activefields.put(NL_POSTRENDER, new Integer(VL_POSTRENDER));
	activefields.put(NL_POSTSWAP, new Integer(VL_POSTSWAP));
	activefields.put(NL_PROPERTIES, new Integer(VL_PROPERTIES));
	activefields.put(NL_RENDERFIELD, new Integer(VL_RENDERFIELD));
	activefields.put(NL_UNIVERSE, new Integer(VL_UNIVERSE));
	activefields.put(NL_VIEW, new Integer(VL_VIEW));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    BodyComponentAWT(J3DObject data) {

	super(data);
	this.data = data;
	setPermissions(permissions);
    }

    ///////////////////////////////////
    //
    // BodyComponentAWT Methods
    //
    ///////////////////////////////////

    protected final Object
    buildPeer() {

	Object  comp = null;

	switch (getMinor()) {
	    case CANVAS3D:
		peer = comp = new J3DCanvas3D(getData(), this);
		setField(NL_POSTRENDER);
		setField(NL_POSTSWAP);
		setField(NL_PRERENDER);
		setField(NL_RENDERFIELD);
		setField(NL_VIEW);
		setField(NL_OFFSCREENBUFFER);
		setField(NL_DOUBLEBUFFERED);
		setField(NL_ALIVE);
		break;

	    default:
		VM.abort(UNIMPLEMENTED);
		break;
	}

	return(comp);
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;
	Object      comp;

	//
	// Requests should fo back to YoixBodyComponentAWT if they're
	// not handled here.
	//

	comp = this.peer;		// snapshot - just to be safe

	switch (activeField(name, activefields)) {
	    default:
		obj = super.executeField(name, argv);
		break;
	}

	return(obj);
    }


    protected final void
    finalize() {

	data = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	Object  comp;

	//
	// Requests should fo back to YoixBodyComponentAWT if they're
	// not handled here.
	//

	comp = this.peer;		// snapshot - just to be safe

	switch (activeField(name, activefields)) {
	    case VL_ALIVE:
		obj = getAlive(comp, obj);
		break;

	    case VL_DOUBLEBUFFERED:
		obj = getDoubleBuffered(comp, obj);
		break;

	    case VL_OFFSCREENBUFFER:
		obj = getOffScreenBuffer(comp, obj);
		break;

	    case VL_PROPERTIES:
		obj = getProperties(comp, obj);
		break;

	    case VL_UNIVERSE:
		obj = getUniverse(comp, obj);
		break;

	    default:
		obj = super.getField(name, obj);
		break;
	}

	return(obj);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	Object  comp;

	//
	// Requests should fo back to YoixBodyComponentAWT if they're
	// not handled here.
	//

	comp = this.peer;		// snapshot - just to be safe

	if (comp != null && obj != null) {
	    switch (activeField(name, activefields)) {
		case VL_ALIVE:
		    setAlive(comp, obj);
		    break;

		case VL_DOUBLEBUFFERED:
		    setDoubleBuffered(comp, obj);
		    break;

		case VL_OFFSCREENBUFFER:
		    setOffScreenBuffer(comp, obj);
		    break;

		case VL_POSTRENDER:
		    setPostRender(comp, obj);
		    break;

		case VL_POSTSWAP:
		    setPostSwap(comp, obj);
		    break;

		case VL_PRERENDER:
		    setPreRender(comp, obj);
		    break;

		case VL_RENDERFIELD:
		    setRenderField(comp, obj);
		    break;

		case VL_VIEW:
		    setView(comp, obj);
		    break;

		default:
		    super.setField(name, obj);
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

    private YoixObject
    getAlive(Object comp, YoixObject obj) {

	if (comp instanceof J3DCanvas3D)
	    obj = YoixObject.newInt(((J3DCanvas3D)comp).getAlive());
	return(obj);
    }


    private YoixObject
    getDoubleBuffered(Object comp, YoixObject obj) {

	if (comp instanceof J3DCanvas3D)
	    obj = YoixObject.newInt(((J3DCanvas3D)comp).getDoubleBuffered());
	return(obj);
    }


    private YoixObject
    getProperties(Object comp, YoixObject obj) {

	if (comp instanceof J3DCanvas3D)
	    obj = YoixMisc.copyIntoDictionary(((J3DCanvas3D)comp).queryProperties());
	return(obj);
    }


    private YoixObject
    getOffScreenBuffer(Object comp, YoixObject obj) {

	Image  image;

	if (comp instanceof J3DCanvas3D)
	    obj = YoixObject.newImage(((J3DCanvas3D)comp).getOffScreenImage());
	return(obj);
    }


    private YoixObject
    getUniverse(Object comp, YoixObject obj) {

	BodyVirtualUniverse  universe;

	if (comp instanceof J3DCanvas3D) {
	    if ((universe = ((J3DCanvas3D)comp).getUniverse()) != null)
		obj = universe.getContext();
	    else obj = J3DObject.newJ3DNull(T_VIRTUALUNIVERSE);
	}
	return(obj);
    }


    private void
    setAlive(Object comp, YoixObject obj) {

	if (comp instanceof J3DCanvas3D)
	    ((J3DCanvas3D)comp).setAlive(obj.booleanValue());
    }


    private void
    setDoubleBuffered(Object comp, YoixObject obj) {

	//
	// Decided to duplicate our standard Swing code, which makes no
	// changes when obj (or VM.getDoubleBuffered()) is NULL, which
	// means an J3DCanvas3D will usually get its preferred setting.
	// Seems like overkill, but leave it be for now.
	//

	if ((obj = VM.getDoubleBuffered(obj)) != null) {
	    if (obj.notNull()) {
		if (comp instanceof J3DCanvas3D)
		    ((J3DCanvas3D)comp).setDoubleBuffered(obj.booleanValue());
	    }
	}
    }


    private void
    setOffScreenBuffer(Object comp, YoixObject obj) {

	if (comp instanceof J3DCanvas3D) {
	    //
	    // More - maybe?
	    //
	    if (data == null) {		// means constructor hasn't finished
		if (getData().getObject(NL_OFFSCREENBUFFER).isNull())
		    setToConstant(NL_OFFSCREENBUFFER);
	    }
	    try {
	        ((J3DCanvas3D)comp).setOffScreenImage(obj);
	    }
	    catch(IllegalArgumentException e) {
		VM.recordException(e);
		VM.abort(BADVALUE, NL_OFFSCREENBUFFER);
	    }
	}
    }


    private void
    setPostRender(Object comp, YoixObject obj) {

	if (comp instanceof J3DCanvas3D)
	    ((J3DCanvas3D)comp).setPostRender(obj);
    }


    private void
    setPostSwap(Object comp, YoixObject obj) {

	if (comp instanceof J3DCanvas3D)
	    ((J3DCanvas3D)comp).setPostSwap(obj);
    }


    private void
    setPreRender(Object comp, YoixObject obj) {

	if (comp instanceof J3DCanvas3D)
	    ((J3DCanvas3D)comp).setPreRender(obj);
    }


    private void
    setRenderField(Object comp, YoixObject obj) {

	if (comp instanceof J3DCanvas3D)
	    ((J3DCanvas3D)comp).setRenderField(obj);
    }


    private synchronized void
    setView(Object comp, YoixObject obj) {

	if (comp instanceof J3DCanvas3D)
	    ((J3DCanvas3D)comp).setView(obj);
    }
}

