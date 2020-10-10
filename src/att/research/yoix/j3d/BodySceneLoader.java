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
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.Loader;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.SceneBase;
import att.research.yoix.*;

class BodySceneLoader extends J3DPointerActive

    implements Constants

{

    private Class  loaderclass = null;
    private Scene  errorscene = null;

    //
    // Custom callback functions.
    //

    private YoixObject  preload = null;
    private YoixObject  postload = null;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(5);

    static {
	activefields.put(NL_ERRORSCENE, new Integer(VL_ERRORSCENE));
	activefields.put(NL_JAVACLASS, new Integer(VL_JAVACLASS));
	activefields.put(NL_LOAD, new Integer(VL_LOAD));
	activefields.put(NL_PRELOAD, new Integer(VL_PRELOAD));
	activefields.put(NL_POSTLOAD, new Integer(VL_POSTLOAD));
    }

    //
    // This is a reference to the default scene loaders, which is set
    // by Module.loaded() which is called by the Yoix module loading
    // code. Doing things this way means the module definition table
    // controls the default loaders.
    //

    private static YoixObject  defaulterrorscene = null;
    private static YoixObject  defaultsceneloaders = null;
    private static YoixObject  defaultpostload = null;
    private static YoixObject  defaultpreload = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodySceneLoader(J3DObject data) {

	super(data);
	buildSceneLoader();
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

	return(SCENELOADER);
    }

    ///////////////////////////////////
    //
    // BodySceneLoader Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case VL_LOAD:
		obj = builtinLoad(name, argv);
		break;

	    default:
		obj = null;
		break;
	}
	return(obj);
    }


    protected void
    finalize() {

	loaderclass = null;
	errorscene = null;
	postload = null;
	preload = null;
	super.finalize();
    }


    static YoixObject
    getDefaultErrorScene() {

	return(defaulterrorscene);
    }


    static YoixObject
    getDefaultSceneLoaders() {

	return(defaultsceneloaders);
    }


    static YoixObject
    getDefaultPostLoad() {

	return(defaultpostload);
    }


    static YoixObject
    getDefaultPreLoad() {

	return(defaultpreload);
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case VL_JAVACLASS:
		obj = getJavaClass(obj);
		break;

	    default:
		break;
	}
	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(loaderclass);
    }


    static Scene
    loadScene(String path, YoixObject loaders) {

	J3DObject  loader;
	Scene      scene = null;

	if ((loader = pickLoader(path, loaders)) != null) {
	    scene = sceneLoader(
		loader.getBodySceneLoader(),
		path,
		loader.getInt(NL_FLAGS, 0),
		loader.getInt(NL_ERRORMODEL, 0)
	    );
	}
	return(scene);
    }


    static YoixObject
    pickLoaders(YoixObject obj) {

	return(obj == null || obj.isNull() ? defaultsceneloaders : obj);
    }


    static void
    setDefaultErrorScene(YoixObject obj) {

	defaulterrorscene = obj.notNull() ? obj : null;
    }


    static void
    setDefaultSceneLoaders(YoixObject obj) {

	defaultsceneloaders = obj.notNull() ? obj : null;
    }


    static void
    setDefaultPostLoad(YoixObject obj) {

	defaultpostload = obj.notNull() ? obj : null;
    }


    static void
    setDefaultPreLoad(YoixObject obj) {

	defaultpreload = obj.notNull() ? obj : null;
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case VL_ERRORSCENE:
		    setErrorScene(obj);
		    break;

		case VL_JAVACLASS:
		    setJavaClass(obj);
		    break;

		case VL_POSTLOAD:
		    setPostLoad(obj);
		    break;

		case VL_PRELOAD:
		    setPreLoad(obj);
		    break;

		default:
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
    buildSceneLoader() {

	setField(NL_JAVACLASS);
	setField(NL_POSTLOAD);
	setField(NL_PRELOAD);
    }


    private YoixObject
    builtinLoad(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	Scene       scene;

	if (arg.length >= 1 || arg.length <= 5) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg.length <= 1 || arg[1].isNumber()) {
		    if (arg.length <= 2 || arg[2].isString() || arg[2].isNull()) {
			if (arg.length <= 3 || arg[3].isNumber()) {
			    if (arg.length <= 4 || arg[4].isNumber()) {
				scene = sceneLoader(
				    this,
				    arg[0].stringValue(),
				    arg.length > 3 ? arg[3].intValue() : getInt(NL_FLAGS, 0),
				    arg.length > 4 ? arg[4].intValue() : getInt(NL_ERRORMODEL, 0)
				);
				if (scene != null) {
				    obj = J3DObject.newBranchGroup(
					scene,
					arg.length > 1 ? arg[1].intValue() : 0,
					arg.length > 2 ? arg[2].stringValue() : null
				    );
				} else obj = J3DObject.newBranchGroup(scene);
			    } else VM.badArgument(name, 4);
			} else VM.badArgument(name, 3);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(obj);
    }


    private static void
    clearError(BodySceneLoader owner) {

	owner.putObject(NL_ERRORDICT, YoixObject.newDictionary());
    }


    private synchronized Scene
    getErrorScene() {

	YoixObject  obj;
	J3DObject   loader;
	String      path;

	if (errorscene == null) {
	    if ((obj = getObject(NL_ERRORSCENE)) != null || (obj = defaulterrorscene) != null) {
		if (obj.isString() && obj.notNull()) {
		    path = obj.stringValue();
		    if ((loader = pickLoader(path, defaultsceneloaders)) != null) {
			errorscene = sceneLoader(
			    loader.getBodySceneLoader(),
			    path,
			    loader.getInt(NL_FLAGS, 0),
			    -1
			);
		    }
		}
	    }
	    if (errorscene == null)
		errorscene = new SceneBase();
	}
	return(errorscene);
    }


    private YoixObject
    getJavaClass(YoixObject obj) {

	String  name;
	Class   source;

	if ((source = loaderclass) != null)
	    name = source.getName();
	else name = null;
	return(YoixObject.newString(name));
    }


    private static void
    handleClassError(BodySceneLoader owner, Throwable t, String name) {

	int  errormodel;

	if ((errormodel = owner.getInt(NL_ERRORMODEL, 0)) >= 0) {
	    owner.putObject(
		NL_ERRORDICT,
		YoixError.recordDetails(INVALIDCLASS, new String[] {OFFENDINGNAME, name}, t)
	    );
	    switch (errormodel) {
		case 0:
		    VM.abort(INVALIDCLASS, new String[] {OFFENDINGNAME, name}, t);
		    break;

		case 1:
		    VM.warn(INVALIDCLASS, new String[] {OFFENDINGNAME, name}, t);
		    break;
	    }
	}
    }


    private static Scene
    handleSceneError(BodySceneLoader owner, Throwable t, String error, String offender, String path, int errormodel) {

	Scene  scene = null;

	if (errormodel >= 0) {
	    owner.putObject(
		NL_ERRORDICT,
		YoixError.recordDetails(error, new String[] {offender, path}, t)
	    );
	    switch (errormodel) {
		case 0:
		    VM.abort(error, new String[] {offender, path}, t);
		    break;

		case 1:
		    VM.warn(error, new String[] {offender, path}, t);
		    break;
	    }
	    scene = owner.getErrorScene();
	}
	return(scene);
    }


    private static J3DObject
    pickLoader(String path, YoixObject loaders) {

	YoixObject  entry;
	J3DObject   obj = null;
	String      suffix;
	int         index;
	int         length;
	int         n;

	if (loaders != null) {
	    if (path != null && path.length() > 0) {
		if ((index = path.lastIndexOf(".")) >= 0) {
		    suffix = path.substring(index+1).toLowerCase();
		    if ((entry = loaders.getObject(suffix)) != null && entry.notNull()) {
			if (J3DObject.isSceneLoader(entry) == false) {
			    obj = J3DObject.newSceneLoader(entry);
			    length = loaders.length();
			    for (n = 0; n < length; n++) {
				if (entry.bodyEquals(loaders.getObject(n)))
				    loaders.putObject(n, obj);
			    }
			} else obj = (J3DObject)entry;
		    }
		}
	    }
	}
	return(obj);
    }


    private void
    postLoad(String path, boolean loaded) {

	YoixObject  funct;
	YoixObject  args[];

	if ((funct = postload) != null || (funct = defaultpostload) != null) {
	    args = new YoixObject[] {
		YoixObject.newString(path),
		YoixObject.newInt(loaded)
	    };
	    call(funct, args);
	}
    }


    private void
    preLoad(String path) {

	YoixObject  funct;
	YoixObject  args[];

	if ((funct = preload) != null || (funct = defaultpreload) != null) {
	    args = new YoixObject[] {YoixObject.newString(path)};
	    call(funct, args);
	}
    }


    private static Scene
    sceneLoader(BodySceneLoader owner, String path, int flags, int errormodel) {

	BufferedReader  reader;
	BranchGroup     branch;
	Loader          loader;
	Class           source;
	Scene           scene = null;

	if (owner != null && path != null && path.length() > 0) {
	    if ((reader = YoixMisc.getReader(path)) != null) {
		if ((source = (Class)owner.getManagedObject()) != null) {
		    try {
			if ((loader = (Loader)YoixReflect.javaInstance(source, Loader.class, owner.data)) != null) {
			    try {
				owner.preLoad(path);
				loader.setFlags(flags);
				scene = loader.load(reader);
				//
				// This is recent addition that may not be used.
				//
				MiscSceneGraphObject.putString(NL_PATH, path, scene.getSceneGroup());
			    }
			    finally {
				owner.postLoad(path, scene != null);
			    }
			}
		    }
		    catch(InvocationTargetException e) {
			handleClassError(owner, e.getTargetException(), source.getName());
		    }
		    catch(FileNotFoundException e) {
			scene = handleSceneError(owner, e, UNREADABLEFILE, OFFENDINGNAME, path, errormodel);
		    }
		    catch(IncorrectFormatException e) {
			scene = handleSceneError(owner, e, BADFILEFORMAT, OFFENDINGFILE, path, errormodel);
		    }
		    catch(ParsingErrorException e) {
			scene = handleSceneError(owner, e, BADFILECONTENT, OFFENDINGFILE, path, errormodel);
		    }
		}
	    } else scene = handleSceneError(owner, null, UNREADABLEFILE, OFFENDINGNAME, path, errormodel);
	}

	return(scene);
    }


    private synchronized void
    setErrorScene(YoixObject obj) {

	errorscene = null;
    }


    private void
    setJavaClass(YoixObject obj) {

	String  name;
	Object  instance;

	loaderclass = null;
	clearError(this);

	if (obj.notNull()) {
	    name = obj.stringValue();
	    try {
		if ((instance = YoixReflect.javaInstance(name, Loader.class, data)) != null)
		    loaderclass = instance.getClass();
	    }
	    catch(InvocationTargetException e) {
		handleClassError(this, e.getTargetException(), name);
	    }
	}
    }


    private void
    setPostLoad(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(2))
		postload = obj;
	    else VM.abort(TYPECHECK, NL_POSTLOAD);
	} else postload = null;
    }


    private void
    setPreLoad(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(1))
		preload = obj;
	    else VM.abort(TYPECHECK, NL_PRELOAD);
	} else preload = null;
    }
}

