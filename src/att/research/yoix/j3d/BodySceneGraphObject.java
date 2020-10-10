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
import java.util.Iterator;
import javax.media.j3d.CapabilityNotSetException;
import javax.media.j3d.Node;
import javax.media.j3d.RestrictedAccessException;
import javax.media.j3d.NodeComponent;
import javax.media.j3d.SceneGraphObject;
import att.research.yoix.*;

abstract
class BodySceneGraphObject extends J3DPointerActive

    implements Constants

{

    protected SceneGraphObject  peer = null;

    //
    // These map class names to the maps - implementation will undoubtedly
    // change, but that won't affect any other classes. Also suspect that
    // with a little effort we can simplify the entire process and maybe
    // even eliminate one of the loadXXX() methods (not sure if that's the
    // best idea right now). The demands that the current implementation
    // places on other classes is error prone, because loadXXX() methods
    // currently require that classes be passed as arguments but it would
    // be much better if the loadXXX() methods could generate the classes.
    // Can reflection help?? Not a big issue, at least not yet, so it's
    // currently not worth much of an effort!!
    //

    private static HashMap  namemaps = new HashMap();
    private static HashMap  bitmaps = new HashMap();

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodySceneGraphObject(J3DObject data) {

	this(null, data);
    }


    BodySceneGraphObject(SceneGraphObject object, J3DObject data) {

	super(data);
	buildSceneGraphObject(object);
    }

    ///////////////////////////////////
    //
    // BodySceneGraphObject Methods
    //
    ///////////////////////////////////

    final void
    abort(RestrictedAccessException e, String name) {

	String  error;

	if (e instanceof CapabilityNotSetException)
	    error = CAPABILITYNOTSET;
	else error = RESTRICTEDACCESS;
	VM.abort(error, name);
    }


    final void
    changeCapability(int capability, boolean state) {

	changeCapability(peer, capability, state);
    }


    static void
    changeCapability(SceneGraphObject target, int bit, boolean state) {

	if (target != null) {
	    if (state != target.getCapability(bit)) {
		if (state)
		    target.setCapability(bit);
		else target.clearCapability(bit);
	    }
	}
    }


    static void
    changeCapabilityIsFrequent(SceneGraphObject target, int bit, boolean state) {

	if (target != null) {
	    if (state != target.getCapabilityIsFrequent(bit)) {
		if (state)
		    target.setCapabilityIsFrequent(bit);
		else target.clearCapabilityIsFrequent(bit);
	    }
	}
    }


    static void
    changeCapabilitySetting(SceneGraphObject target, int bit, int setting) {

	if (setting >= 0) {
	    changeCapability(target, bit, setting > 0);
	    changeCapabilityIsFrequent(target, bit, setting > 1);
	}
    }


    protected YoixObject
    executeField(int field, String name, YoixObject argv[]) {

	return(null);
    }


    protected void
    finalize() {

	peer.setUserData(null);
	peer = null;
	super.finalize();
    }


    final int
    getCapabilitySetting() {

	int  setting;

	if ((setting = MiscSceneGraphObject.getInt(NL_DEFAULTCAPABILITY, -1, peer)) < 0) {
	    if (peer instanceof Node) {
		if (MiscSceneGraphObject.getString(NL_TAG, peer) != null)
		    setting = 2;
		else setting = 0;
	    } else setting = 0;
	}
	return(setting);
    }


    protected YoixObject
    getField(int field, YoixObject obj) {

	switch (field) {
	    case VL_CAPABILITIES:
		obj = getCapabilities(obj);
		break;

	    case VL_COMPILED:
		obj = getCompiled(obj);
		break;

	    case VL_DEFAULTCAPABILITY:
		obj = getDefaultCapability(obj);
		break;

	    case VL_LIVE:
		obj = getLive(obj);
		break;

	    case VL_PATH:
		obj = getPath(obj);
		break;

	    default:
		break;
	}
	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(peer);
    }


    static void
    loadCapabilities(Object table[], Class owner) {

	HashMap  names;
	HashMap  bits;
	int      length;
	int      n;

	//
	// Don't think synchronization is an issue here??
	//

	if (table != null) {
	    if ((length = table.length) >= 3) {
		if ((names = (HashMap)namemaps.get(owner)) == null)
		    names = new HashMap();
		if ((bits = (HashMap)bitmaps.get(owner)) == null)
		    bits = new HashMap();
		for (n = 0; n < length - 2; n += 3) {
		    if (table[n] != null && table[n+1] != null) {
			names.put(table[n], table[n+1]);
			bits.put(table[n+1], table[n+2]);
		    }
		}
		namemaps.put(owner, names);
		bitmaps.put(owner, bits);
	    }
	}
    }


    static void
    loadCapabilities(Class source, Class owner) {

	HashMap  sourcemap;
	HashMap  destmap;

	if (source != null && owner != null) {
	    if ((sourcemap = (HashMap)namemaps.get(source)) != null) {
		if ((destmap = (HashMap)namemaps.get(owner)) == null) {
		    destmap = new HashMap();
		    namemaps.put(owner, destmap);
		}
		destmap.putAll(sourcemap);
	    }
	    if ((sourcemap = (HashMap)bitmaps.get(source)) != null) {
		if ((destmap = (HashMap)bitmaps.get(owner)) == null) {
		    destmap = new HashMap();
		    bitmaps.put(owner, destmap);
		}
		destmap.putAll(sourcemap);
	    }
	}
    }


    protected YoixObject
    setField(int field, YoixObject obj) {

	if (obj != null) {
	    switch (field) {
		case VL_CAPABILITIES:
		    setCapabilities(obj);
		    break;

		case VL_DEFAULTCAPABILITY:
		    setDefaultCapability(obj);
		    break;

		default:
		    break;
	    }
	}
	return(obj);
    }


    protected void
    updateCapabilities() {

	//
	// A callback method that subclasses can override when they want
	// to update capabilities in associated classes (e.g., a Shape3D
	// might want to update it's Appearance capabilites). This often
	// ends up calling the updateDefaultCapabilities() method, which
	// is defined below, but the subclass sometimes has more to do.
	//
    }


    final void
    updateDefaultCapabilities(String name) {

	updateDefaultCapabilities(new String[] {name});
    }


    final void
    updateDefaultCapabilities(String names[]) {

	YoixObject  obj;
	int         setting;
	int         n;

	if (names != null) {
	    setting = getCapabilitySetting();
	    for (n = 0; n < names.length; n++) {
		if (defined(names[n])) {
		    if ((obj = get(names[n], false)) != null)	// go through getField()
			updateDefaultCapabilities(obj, setting);
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildSceneGraphObject(SceneGraphObject object) {

	peer = object;
    }


    private synchronized void
    changeCapabilities(YoixObject obj) {

	YoixObject  element;
	HashMap     namemap;
	Object      value;
	int         length;
	int         n;

	if (peer.isLive() == false && peer.isCompiled() == false) {
	    changedDefaultCapabilities();
	    if (obj.notNull()) {
		if ((namemap = (HashMap)namemaps.get(this.getClass())) != null) {
		    length = obj.length();
		    for (n = 0; n < length; n++) {
			if ((element = obj.getObject(n)) != null) {
			    if (element.isNumber()) {
				if ((value = namemap.get(obj.name(n))) != null) {
				    if (value instanceof Integer) {
					changeCapabilitySetting(
					    peer,
					    ((Integer)value).intValue(),
					    element.intValue()
					);
				    }
				}
			    }
			}
		    }
		}
	    }
	    MiscSceneGraphObject.putYoixObject(NL_CAPABILITIES, obj, peer);
	}
    }


    private synchronized void
    changedDefaultCapabilities() {

	YoixObject  obj;
	Iterator    iterator;
	HashMap     bits;
	Object      key;
	Object      value;
	String      name;
	int         defaultsetting;
	int         setting;
	int         bit;

	if (peer.isLive() == false && peer.isCompiled() == false) {
	    if ((bits = (HashMap)bitmaps.get(this.getClass())) != null) {
		defaultsetting = getCapabilitySetting();
		for (iterator = bits.keySet().iterator(); iterator.hasNext(); ) {
		    if ((key = iterator.next()) != null) {
			if (key instanceof Integer) {
			    bit = ((Integer)key).intValue();
			    setting = defaultsetting;
			    //
			    // Earlier version accepted a string and in
			    // that case assumed it was a field name in
			    // in data. If that field wasn't writable
			    // (e.g., it had been declared using const)
			    // then setting was set to 0. String support
			    // code has been removed, but it could be
			    // restored.
			    // 
			    value = bits.get(key);
			    if (value instanceof Boolean)
				setting = ((Boolean)value).booleanValue() ? 2 : 0;
			    else if (value instanceof Integer) 
				setting = ((Integer)value).intValue();
			    changeCapabilitySetting(peer, bit, setting);
			}
		    }
		}
	    }
	}
    }


    private YoixObject
    getCapabilities(YoixObject obj) {

	Iterator  iterator;
	HashMap   src;
	HashMap   dest;
	Object    key;
	Object    value;
	int       setting;
	int       bit;

	if ((src = (HashMap)namemaps.get(this.getClass())) != null) {
	    dest = new HashMap();
	    for (iterator = src.keySet().iterator(); iterator.hasNext(); ) {
		if ((key = iterator.next()) != null) {
		    if (key instanceof String) {
			if ((value = src.get(key)) != null) {
			    if (value instanceof Integer) {
				bit = ((Integer)value).intValue();
				if (peer.getCapability(bit))
				    setting = peer.getCapabilityIsFrequent(bit) ? 2 : 1;
				else setting = 0;
				dest.put(key, YoixObject.newInt(setting));
			    }
			}
		    }
		}
	    }
	    obj = YoixMisc.copyIntoDictionary(dest);
	} else obj = YoixObject.newDictionary();
	return(obj);
    }


    private YoixObject
    getCompiled(YoixObject obj) {

	return(YoixObject.newInt(peer.isCompiled()));
    }


    private YoixObject
    getDefaultCapability(YoixObject obj) {

	return(YoixObject.newInt(MiscSceneGraphObject.getInt(NL_DEFAULTCAPABILITY, -1, peer)));
    }


    private YoixObject
    getLive(YoixObject obj) {

	return(YoixObject.newInt(peer.isLive()));
    }


    private YoixObject
    getPath(YoixObject obj) {

	return(YoixObject.newString(MiscSceneGraphObject.getString(NL_PATH, peer)));
    }


    private void
    setCapabilities(YoixObject obj) {

	if (obj.equals(MiscSceneGraphObject.getYoixObject(NL_CAPABILITIES, peer)) == false) {
	    changeCapabilities(obj);
	    updateCapabilities();
	}
    }


    private void
    setDefaultCapability(YoixObject obj) {

	int  current;
	int  value;

	//
	// Decided, for now at least, to only process changes that increase
	// the default setting. Done because it may simplify the work that
	// other classes have to sync capabilites in NodeComponents. Not
	// convinced this is the place for that decision, but it should be
	// OK for now. Maybe NL_DEFAULTCAPABILITY should be locked via the
	// permissions array??
	//

	value = Math.max(-1, Math.min(obj.intValue(), 2));
	current = MiscSceneGraphObject.getInt(NL_DEFAULTCAPABILITY, -1, peer);
	if (value > current) {
	    MiscSceneGraphObject.putInt(NL_DEFAULTCAPABILITY, value, peer);
	    if ((obj = MiscSceneGraphObject.getYoixObject(NL_CAPABILITIES, peer)) != null) {
		changeCapabilities(obj);
		updateCapabilities();
	    }
	}
    }


    private void
    updateDefaultCapabilities(YoixObject obj, int setting) {

	YoixObject  entry;
	int         length;
	int         n;

	//
	// Does the minimum that we currently think is necessary, but it
	// probably could be smarter - maybe later!! Traversing one level
	// of an array was added to handle NL_GEOMETRY entries that may
	// be arrays. Don't like the special case code, but it should be
	// good enough for now. Recursion with a HashMap to keep track of
	// where we've been might be the general solution, but could also
	// lead to subtle complications.
	//

	if (obj != null) {
	    if (obj instanceof J3DObject) {
		if (obj.defined(NL_DEFAULTCAPABILITY))
		    obj.putInt(NL_DEFAULTCAPABILITY, setting);
	    } else if (obj.isArray()) {
		length = obj.length();
		for (n = obj.offset(); n < length; n++) {
		    if ((entry = obj.getObject(n)) != null) {
			if (entry instanceof J3DObject) {
			    if (entry.defined(NL_DEFAULTCAPABILITY))
				entry.putInt(NL_DEFAULTCAPABILITY, setting);
			}
		    }
		}
	    }
	}
    }
}

