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
import javax.media.j3d.Node;
import javax.media.j3d.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import com.sun.j3d.loaders.Scene;
import att.research.yoix.*;

public
class J3DObject extends YoixObject

    implements Constants

{

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    protected
    J3DObject(YoixObject template) {

	super(template);
    }


    protected
    J3DObject(YoixPointerActive body) {

	super(body);
    }


    protected
    J3DObject(String typename) {

	//
	// The corresponding YoixObject constructor was a very recent
	// addition that gives us a way to build a null object that
	// the interpreter will approve of and will be an instance of
	// this class. Not completely convinced and it's definitely not
	// needed, but may be convenient. The details in YoixObject are
	// a bit kludgy but they seem to work - we eventually need to
	// take another look.
	//

	super(typename);
    }


    protected
    J3DObject(int minor, int length) {

	super(minor, length);
    }

    ///////////////////////////////////
    //
    // YoixAPIProtected Methods
    //
    ///////////////////////////////////

    protected final Object
    body() {

	//
	// This gives access to other classes in this package.
	//

	return(super.body());
    }


    protected final void
    forcePutObject(String name, YoixObject obj) {

	//
	// This gives access to other classes in this package.
	//

	super.forcePutObject(name, obj);
    }


    protected final void
    forceSetAccessElement(String name, int perm) {

	//
	// This gives access to other classes in this package.
	//

	super.forceSetAccessElement(name, perm);
    }


    protected final Object
    getManagedObject() {

	//
	// This gives access to other classes in this package.
	//

	return(super.getManagedObject());
    }


    protected final void
    setAccessBody(int perm) {

	//
	// This gives access to other classes in this package.
	//

	super.setAccessBody(perm);
    }


    protected final void
    setAccessElement(int index, int perm) {

	//
	// This gives access to other classes in this package.
	//

	super.setAccessElement(index, perm);
    }


    protected final void
    setAccessElement(String name, int perm) {

	//
	// This gives access to other classes in this package.
	//

	super.setAccessElement(name, perm);
    }


    protected final void
    setGrowable(boolean state) {

	//
	// This gives access to other classes in this package.
	//

	super.setGrowable(state);
    }

    ///////////////////////////////////
    //
    // J3DObject Methods
    //
    ///////////////////////////////////

    final BodyInterpolator
    getBodyInterpolator() {

	Object  body;

	return((body = body()) instanceof BodyInterpolator ? (BodyInterpolator)body : null);
    }


    final BodyNode
    getBodyNode() {

	Object  body;

	return((body = body()) instanceof BodyNode ? (BodyNode)body : null);
    }


    final BodyNodeComponent
    getBodyNodeComponent() {

	Object  body;

	return((body = body()) instanceof BodyNodeComponent ? (BodyNodeComponent)body : null);
    }


    final BodySceneLoader
    getBodySceneLoader() {

	Object  body;

	return((body = body()) instanceof BodySceneLoader ? (BodySceneLoader)body : null);
    }


    final BodyViewPlatform
    getBodyViewPlatform() {

	Object  body;

	return((body = body()) instanceof BodyViewPlatform ? (BodyViewPlatform)body : null);
    }


    final TransformGroup
    getInterpolatorGroup() {

	return(body() instanceof BodyNode ? ((BodyNode)body()).getInterpolatorGroup() : null);
    }


    final Alpha
    getManagedAlpha() {

	Object  managed;

	return((managed = getManagedObject()) instanceof Alpha ? (Alpha)managed : null);
    }


    final Appearance
    getManagedAppearance() {

	Object  managed;

	return((managed = getManagedObject()) instanceof Appearance ? (Appearance)managed : null);
    }


    final Bounds
    getManagedBounds() {

	Object  managed;

	return((managed = getManagedObject()) instanceof Bounds ? (Bounds)managed : null);
    }


    final BranchGroup
    getManagedBranchGroup() {

	Object  managed;

	return((managed = getManagedObject()) instanceof BranchGroup ? (BranchGroup)managed : null);
    }


    final ColoringAttributes
    getManagedColoringAttributes() {

	Object  managed;

	return((managed = getManagedObject()) instanceof ColoringAttributes ? (ColoringAttributes)managed : null);
    }


    final Geometry
    getManagedGeometry() {

	Object  managed;

	return((managed = getManagedObject()) instanceof Geometry ? (Geometry)managed : null);
    }


    final Group
    getManagedGroup() {

	Object  managed;

	return((managed = getManagedObject()) instanceof Group ? (Group)managed : null);
    }


    final Interpolator
    getManagedInterpolator() {

	Object  managed;

	return((managed = getManagedObject()) instanceof Interpolator ? (Interpolator)managed : null);
    }


    final LineArray
    getManagedLineArray() {

	Object  managed;

	return((managed = getManagedObject()) instanceof LineArray ? (LineArray)managed : null);
    }


    final LineAttributes
    getManagedLineAttributes() {

	Object  managed;

	return((managed = getManagedObject()) instanceof LineAttributes ? (LineAttributes)managed : null);
    }


    final LineStripArray
    getManagedLineStripArray() {

	Object  managed;

	return((managed = getManagedObject()) instanceof LineStripArray ? (LineStripArray)managed : null);
    }


    final Material
    getManagedMaterial() {

	Object  managed;

	return((managed = getManagedObject()) instanceof Material ? (Material)managed : null);
    }


    final Node
    getManagedNode() {

	Object  managed;

	return((managed = getManagedObject()) instanceof Node ? (Node)managed : null);
    }


    final NodeComponent
    getManagedNodeComponent() {

	Object  managed;

	return((managed = getManagedObject()) instanceof NodeComponent ? (NodeComponent)managed : null);
    }


    final PointArray
    getManagedPointArray() {

	Object  managed;

	return((managed = getManagedObject()) instanceof PointArray ? (PointArray)managed : null);
    }


    final PointAttributes
    getManagedPointAttributes() {

	Object  managed;

	return((managed = getManagedObject()) instanceof PointAttributes ? (PointAttributes)managed : null);
    }


    final PolygonAttributes
    getManagedPolygonAttributes() {

	Object  managed;

	return((managed = getManagedObject()) instanceof PolygonAttributes ? (PolygonAttributes)managed : null);
    }


    final QuadArray
    getManagedQuadArray() {

	Object  managed;

	return((managed = getManagedObject()) instanceof QuadArray ? (QuadArray)managed : null);
    }


    final RenderingAttributes
    getManagedRenderingAttributes() {

	Object  managed;

	return((managed = getManagedObject()) instanceof RenderingAttributes ? (RenderingAttributes)managed : null);
    }


    final Texture
    getManagedTexture() {

	Object  managed;

	return((managed = getManagedObject()) instanceof Texture ? (Texture)managed : null);
    }


    final TextureUnitState
    getManagedTextureUnit() {

	Object  managed;

	return((managed = getManagedObject()) instanceof TextureUnitState ? (TextureUnitState)managed : null);
    }


    final Transform3D
    getManagedTransform() {

	Object  managed;

	return((managed = getManagedObject()) instanceof Transform3D ? (Transform3D)managed : null);
    }


    final TransparencyAttributes
    getManagedTransparencyAttributes() {

	Object  managed;

	return((managed = getManagedObject()) instanceof TransparencyAttributes ? (TransparencyAttributes)managed : null);
    }


    final TriangleArray
    getManagedTriangleArray() {

	Object  managed;

	return((managed = getManagedObject()) instanceof TriangleArray ? (TriangleArray)managed : null);
    }


    final TriangleFanArray
    getManagedTriangleFanArray() {

	Object  managed;

	return((managed = getManagedObject()) instanceof TriangleFanArray ? (TriangleFanArray)managed : null);
    }


    final TriangleStripArray
    getManagedTriangleStripArray() {

	Object  managed;

	return((managed = getManagedObject()) instanceof TriangleStripArray ? (TriangleStripArray)managed : null);
    }


    final ViewPlatform
    getManagedViewPlatform() {

	Object  managed;

	return((managed = getManagedObject()) instanceof ViewPlatform ? (ViewPlatform)managed : null);
    }


    final Material
    getTargetMaterial() {

	Material mat = null;

	return(body() instanceof BodyNode ? ((BodyNode)body()).getMaterial() : null);
    }


    final TransformGroup
    getTargetTransform() {

	return(getInterpolatorGroup());
    }


    final boolean
    isAppearance() {

	return(body() instanceof BodyAppearance);
    }


    static boolean
    isAppearance(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isAppearance());
    }


    final boolean
    isAxisAngle() {

	return(notNull() && isType(T_AXISANGLE));
    }


    static boolean
    isAxisAngle(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isAxisAngle());
    }


    final boolean
    isBounds() {

	return(body() instanceof BodyBounds);
    }


    static boolean
    isBounds(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isBounds());
    }


    final boolean
    isBranchGroup() {

	return(body() instanceof BodyBranchGroup);
    }


    static boolean
    isBranchGroup(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isBranchGroup());
    }


    final boolean
    isCanvas3D() {

	return(getManagedObject() instanceof Canvas3D);
    }


    static boolean
    isCanvas3D(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isCanvas3D());
    }


    final boolean
    isColoringAttributes() {

	return(body() instanceof BodyColoringAttributes);
    }


    static boolean
    isColoringAttributes(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isColoringAttributes());
    }


    final boolean
    isConfigolator() {

	return(notNull() && isType(T_CONFIGOLATOR));
    }


    static boolean
    isConfigolator(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isConfigolator());
    }


    final boolean
    isEulerAngle() {

	return(notNull() && isType(T_EULERANGLE));
    }


    static boolean
    isEulerAngle(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isEulerAngle());
    }


    final boolean
    isGeometry() {

	return(notNull() && getManagedObject() instanceof Geometry);
    }


    static boolean
    isGeometry(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isGeometry());
    }


    final boolean
    isInterpolator() {

	return(body() instanceof BodyInterpolator);
    }


    static boolean
    isInterpolator(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isInterpolator());
    }


    final boolean
    isLineArray() {

	return(notNull() && isType(T_LINEARRAY));
    }


    static boolean
    isLineArray(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isLineArray());
    }


    final boolean
    isLineAttributes() {

	return(body() instanceof BodyLineAttributes);
    }


    static boolean
    isLineAttributes(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isLineAttributes());
    }


    final boolean
    isLineStripArray() {

	return(notNull() && isType(T_LINESTRIPARRAY));
    }


    static boolean
    isLineStripArray(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isLineStripArray());
    }


    final boolean
    isMaterial() {

	return(body() instanceof BodyMaterial);
    }


    static boolean
    isMaterial(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isMaterial());
    }


    final boolean
    isOrientation3D() {

	return(isEulerAngle() || isAxisAngle() || isQuat4D() || isNumber());
    }


    static boolean
    isOrientation3D(YoixObject obj) {

	return((obj instanceof J3DObject && ((J3DObject)obj).isOrientation3D()) || obj.isNumber());
    }


    final boolean
    isPoint3D() {

	return(notNull() && isType(T_POINT3D));
    }


    static boolean
    isPoint3D(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isPoint3D());
    }


    final boolean
    isPointArray() {

	return(notNull() && isType(T_POINTARRAY));
    }


    static boolean
    isPointArray(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isPointArray());
    }


    final boolean
    isPointAttributes() {

	return(body() instanceof BodyPointAttributes);
    }


    static boolean
    isPointAttributes(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isPointAttributes());
    }


    final boolean
    isPolygonAttributes() {

	return(body() instanceof BodyPolygonAttributes);
    }


    static boolean
    isPolygonAttributes(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isPolygonAttributes());
    }


    final boolean
    isQuadArray() {

	return(notNull() && isType(T_QUADARRAY));
    }


    static boolean
    isQuadArray(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isQuadArray());
    }


    final boolean
    isQuat4D() {

	return(notNull() && isType(T_QUAT4D));
    }


    static boolean
    isQuat4D(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isQuat4D());
    }


    final boolean
    isRenderingAttributes() {

	return(body() instanceof BodyRenderingAttributes);
    }


    static boolean
    isRenderingAttributes(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isRenderingAttributes());
    }


    final boolean
    isSceneLoader() {

	return(body() instanceof BodySceneLoader);
    }


    static boolean
    isSceneLoader(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isSceneLoader());
    }


    final boolean
    isTexture() {

	return(body() instanceof BodyTexture);
    }


    static boolean
    isTexture(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isTexture());
    }


    static boolean
    isTextureSource(YoixObject obj) {

	return(obj.isImage() || obj.isString() || obj.isDictionary() || obj.isNull());
    }


    final boolean
    isTextureUnit() {

	return(body() instanceof BodyTextureUnit);
    }


    static boolean
    isTextureUnit(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isTextureUnit());
    }


    final boolean
    isTransform3D() {

	return(body() instanceof BodyTransform3D);
    }


    static boolean
    isTransform3D(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isTransform3D());
    }


    final boolean
    isTransparencyAttributes() {

	return(body() instanceof BodyTransparencyAttributes);
    }


    static boolean
    isTransparencyAttributes(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isTransparencyAttributes());
    }


    final boolean
    isTriangleArray() {

	return(notNull() && isType(T_TRIANGLEARRAY));
    }


    static boolean
    isTriangleArray(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isTriangleArray());
    }


    final boolean
    isTriangleFanArray() {

	return(notNull() && isType(T_TRIANGLEFANARRAY));
    }


    static boolean
    isTriangleFanArray(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isTriangleFanArray());
    }


    final boolean
    isTriangleStripArray() {

	return(notNull() && isType(T_TRIANGLESTRIPARRAY));
    }


    static boolean
    isTriangleStripArray(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isTriangleStripArray());
    }


    final boolean
    isVector3D() {

	return(notNull() && isType(T_VECTOR3D));
    }


    static boolean
    isVector3D(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isVector3D());
    }


    final boolean
    isViewPlatform() {

	return(body() instanceof BodyViewPlatform);
    }


    static boolean
    isViewPlatform(YoixObject obj) {

	return(obj instanceof J3DObject && ((J3DObject)obj).isViewPlatform());
    }


    static J3DObject
    newAppearance(Appearance appearance) {

	return(appearance != null
	    ? new J3DObject(new BodyAppearance(appearance))
	    : newJ3DNull(T_APPEARANCE)
	);
    }


    static J3DObject
    newBranchGroup(BranchGroup branchgroup) {

	return(branchgroup != null
	    ? new J3DObject(new BodyBranchGroup(branchgroup))
	    : newJ3DNull(T_BRANCHGROUP)
	);
    }


    static J3DObject
    newBranchGroup(Scene scene) {

	//
	// Setting model to 0 means tagged objects won't be collected.
	//

	return(newBranchGroup(scene, 0, null));
    }


    static J3DObject
    newBranchGroup(Scene scene, int model, String prefix) {

	return(scene != null && scene.getSceneGroup() != null
	    ? new J3DObject(new BodyBranchGroup(scene, model, prefix))
	    : newJ3DNull(T_BRANCHGROUP)
	);
    }


    static J3DObject
    newColoringAttributes(ColoringAttributes attributes) {

	return(attributes != null
	    ? new J3DObject(new BodyColoringAttributes(attributes))
	    : newJ3DNull(T_COLORINGATTRIBUTES)
	);
    }


    static J3DObject
    newEulerAngle(double alpha, double beta, double gamma) {

	J3DObject  obj;

	//
	// The comments about NY_CLASSNAME in newPoint3D() also apply
	// here.
	//

	obj = (J3DObject)YoixMake.yoixType(T_EULERANGLE);
	obj.putDouble(NL_ALPHA, alpha);
	obj.putDouble(NL_BETA, beta);
	obj.putDouble(NL_GAMMA, gamma);
	return(obj);
    }


    static J3DObject
    newEulerAngle(Vector3d vector) {

	J3DObject  obj;

	//
	// The comments about NY_CLASSNAME in newPoint3D() also apply
	// here.
	//

	if (vector != null) {
	    obj = (J3DObject)YoixMake.yoixType(T_EULERANGLE);
	    obj.putDouble(NL_ALPHA, vector.x);
	    obj.putDouble(NL_BETA, vector.y);
	    obj.putDouble(NL_GAMMA, vector.z);
	} else obj = newJ3DNull(T_EULERANGLE);

	return(obj);
    }


    static J3DObject
    newGeometry(Geometry geometry) {

	J3DObject  obj;

	if (geometry instanceof LineArray)
	    obj = newLineArray((LineArray)geometry);
	else if (geometry instanceof LineStripArray)
	    obj = newLineStripArray((LineStripArray)geometry);
	else if (geometry instanceof PointArray)
	    obj = newPointArray((PointArray)geometry);
	else if (geometry instanceof QuadArray)
	    obj = newQuadArray((QuadArray)geometry);
	else if (geometry instanceof TriangleArray)
	    obj = newTriangleArray((TriangleArray)geometry);
	else if (geometry instanceof TriangleFanArray)
	    obj = newTriangleFanArray((TriangleFanArray)geometry);
	else if (geometry instanceof TriangleStripArray)
	    obj = newTriangleStripArray((TriangleStripArray)geometry);
	else obj = null;
	return(obj);
    }


    static J3DObject
    newJ3DDictionary(int length) {

	return(new J3DObject(DICTIONARY, length));
    }


    static J3DObject
    newJ3DDictionary(int length, int limit) {

	J3DObject  obj;

	//
	// YoixObject.newDictionary() eventually gets called and by the
	// constructor, which means negative lengths are automatically
	// interpreted as a request for a null object, so we don't have
	// to check the length if we don't want.
	//

	obj = new J3DObject(DICTIONARY, length);
	obj.setGrowable(true);
	obj.setGrowto(limit);
	return(obj);
    }


    static J3DObject
    newJ3DNull(String typename) {

	//
	// Not convinced this is needed or even a good idea - remember to
	// toss the YoixObject support code if we decide it shouldn't ever
	// be allowed.
	//

	return(new J3DObject(typename));
    }


    static J3DObject
    newJ3DPointerActive(YoixPointerActive body) {

	return(new J3DObject(body));
    }


    static J3DObject
    newLineArray(LineArray array) {

	return(array != null
	    ? new J3DObject(new BodyLineArray(array))
	    : newJ3DNull(T_LINEARRAY)
	);
    }


    static J3DObject
    newLineAttributes(LineAttributes attributes) {

	return(attributes != null
	    ? new J3DObject(new BodyLineAttributes(attributes))
	    : newJ3DNull(T_LINEATTRIBUTES)
	);
    }


    static J3DObject
    newLineStripArray(LineStripArray array) {

	return(array != null
	    ? new J3DObject(new BodyLineStripArray(array))
	    : newJ3DNull(T_LINESTRIPARRAY)
	);
    }


    static J3DObject
    newMaterial(Material material) {

	return(material != null
	    ? new J3DObject(new BodyMaterial(material))
	    : newJ3DNull(T_MATERIAL)
	);
    }


    public static J3DObject
    newObject(int id, int length, YoixObject data) {

	J3DObject  obj = null;

	switch (id) {
	    case ALPHA:
		obj = new J3DObject(new BodyAlpha((J3DObject)data));
		break;

	    case AMBIENTLIGHT:
		obj = new J3DObject(new BodyAmbientLight((J3DObject)data));
		break;

	    case APPEARANCE:
		obj = new J3DObject(new BodyAppearance((J3DObject)data));
		break;

	    case BOUNDS:
		obj = new J3DObject(new BodyBounds((J3DObject)data));
		break;

	    case BRANCHGROUP:
		obj = new J3DObject(new BodyBranchGroup((J3DObject)data));
		break;

	    case COLORCUBE:
		obj = new J3DObject(new BodyColorCube((J3DObject)data));
		break;

	    case COLORINGATTRIBUTES:
		obj = new J3DObject(new BodyColoringAttributes((J3DObject)data));
		break;

	    case COMPONENT:
		obj = new J3DObject(new BodyComponentAWT((J3DObject)data));
		break;

	    case CONE:
		obj = new J3DObject(new BodyCone((J3DObject)data));
		break;

	    case CYLINDER:
		obj = new J3DObject(new BodyCylinder((J3DObject)data));
		break;

	    case DIRECTIONALLIGHT:
		obj = new J3DObject(new BodyDirectionalLight((J3DObject)data));
		break;

	    case INTERPOLATOR:
		obj = new J3DObject(new BodyInterpolator((J3DObject)data));
		break;

	    case LINEARRAY:
		obj = new J3DObject(new BodyLineArray((J3DObject)data));
		break;

	    case LINEATTRIBUTES:
		obj = new J3DObject(new BodyLineAttributes((J3DObject)data));
		break;

	    case LINESTRIPARRAY:
		obj = new J3DObject(new BodyLineStripArray((J3DObject)data));
		break;

	    case LOCALE3D:
		obj = new J3DObject(new BodyLocale3D((J3DObject)data));
		break;

	    case MATERIAL:
		obj = new J3DObject(new BodyMaterial((J3DObject)data));
		break;

	    case POINTARRAY:
		obj = new J3DObject(new BodyPointArray((J3DObject)data));
		break;

	    case POINTATTRIBUTES:
		obj = new J3DObject(new BodyPointAttributes((J3DObject)data));
		break;

	    case POINTLIGHT:
		obj = new J3DObject(new BodyPointLight((J3DObject)data));
		break;

	    case POLYGONATTRIBUTES:
		obj = new J3DObject(new BodyPolygonAttributes((J3DObject)data));
		break;

	    case QUADARRAY:
		obj = new J3DObject(new BodyQuadArray((J3DObject)data));
		break;

	    case RENDERINGATTRIBUTES:
		obj = new J3DObject(new BodyRenderingAttributes((J3DObject)data));
		break;

	    case SCENELOADER:
		obj = new J3DObject(new BodySceneLoader((J3DObject)data));
		break;

	    case SHAPE3D:
		obj = new J3DObject(new BodyShape3D((J3DObject)data));
		break;

	    case SPHERE:
		obj = new J3DObject(new BodySphere((J3DObject)data));
		break;

	    case TEXT2D:
		obj = new J3DObject(new BodyText2D((J3DObject)data));
		break;

	    case TEXTURE2D:
		obj = new J3DObject(new BodyTexture2D((J3DObject)data));
		break;

	    case TEXTUREUNIT:
		obj = new J3DObject(new BodyTextureUnit((J3DObject)data));
		break;

	    case TRANSFORM3D:
		obj = new J3DObject(new BodyTransform3D((J3DObject)data));
		break;

	    case TRANSPARENCYATTRIBUTES:
		obj = new J3DObject(new BodyTransparencyAttributes((J3DObject)data));
		break;

	    case TRIANGLEARRAY:
		obj = new J3DObject(new BodyTriangleArray((J3DObject)data));
		break;

	    case TRIANGLEFANARRAY:
		obj = new J3DObject(new BodyTriangleFanArray((J3DObject)data));
		break;

	    case TRIANGLESTRIPARRAY:
		obj = new J3DObject(new BodyTriangleStripArray((J3DObject)data));
		break;

	    case VIEWPLATFORM:
		obj = new J3DObject(new BodyViewPlatform((J3DObject)data));
		break;

	    case VIRTUALUNIVERSE:
		obj = new J3DObject(new BodyVirtualUniverse((J3DObject)data));
		break;

	    default:
		VM.abort(INTERNALERROR);
		break;
	}
	return(obj);
    }


    static J3DObject
    newPoint3D(Point3d point) {

	J3DObject  obj;

	//
	// We can safely cast the return from yoixType() to a J3DObject
	// because the type definition in Module defines a NY_CLASSNAME
	// field and some ugly code in YoixVM.buildTypeTemplate() will
	// remove the NY_CLASSNAME field for the template that's put in
	// typedict if it won't be needed again. In other words, the
	// definition of T_POINT3D in Module.java includes NY_CLASSNAME
	// but that we create are J3DObjects but the NY_CLASSNAME field
	// is gone.
	//
	// NOTE - all this magic isn't absolutely required but it could
	// help eliminate casting by other classes in this package that
	// use newPoint3D(). There's also a chance things could end up
	// being more confusing - not sure yet, but we can always remove
	// NY_CLASSNAME in the Module.java definition and just work with
	// YoixObjects here.
	//

	if (point != null) {
	    obj = (J3DObject)YoixMake.yoixType(T_POINT3D);
	    obj.putDouble(NL_X, point.x);
	    obj.putDouble(NL_Y, point.y);
	    obj.putDouble(NL_Z, point.z);
	} else obj = newJ3DNull(T_POINT3D);

	return(obj);
    }


    static J3DObject
    newPoint3D(double x, double y, double z) {

	J3DObject  obj;

	obj = (J3DObject)YoixMake.yoixType(T_POINT3D);
	obj.putDouble(NL_X, x);
	obj.putDouble(NL_Y, y);
	obj.putDouble(NL_Z, z);
	return(obj);
    }


    static J3DObject
    newPointArray(PointArray array) {

	return(array != null
	    ? new J3DObject(new BodyPointArray(array))
	    : newJ3DNull(T_POINTARRAY)
	);
    }


    static J3DObject
    newPointAttributes(PointAttributes attributes) {

	return(attributes != null
	    ? new J3DObject(new BodyPointAttributes(attributes))
	    : newJ3DNull(T_POINTATTRIBUTES)
	);
    }


    static J3DObject
    newPolygonAttributes(PolygonAttributes attributes) {

	return(attributes != null
	    ? new J3DObject(new BodyPolygonAttributes(attributes))
	    : newJ3DNull(T_POLYGONATTRIBUTES)
	);
    }


    static J3DObject
    newQuadArray(QuadArray array) {

	return(array != null
	    ? new J3DObject(new BodyQuadArray(array))
	    : newJ3DNull(T_QUADARRAY)
	);
    }


    static J3DObject
    newRenderingAttributes(RenderingAttributes attributes) {

	return(attributes != null
	    ? new J3DObject(new BodyRenderingAttributes(attributes))
	    : newJ3DNull(T_RENDERINGATTRIBUTES)
	);
    }


    static J3DObject
    newSceneLoader(YoixObject ival) {

	YoixObject  dict;

	if (ival != null) {
	    if (ival.notNull()) {
		if (ival.isString() || ival.isDictionary()) {
		    if (ival.isString()) {
			dict = YoixObject.newDictionary(1);
			dict.put(NL_JAVACLASS, ival);
			ival = dict;
		    }
		} else ival = null;
	    } else ival = null;
	}
	return(ival != null
	    ? (J3DObject)YoixMake.yoixType(T_SCENELOADER, ival)
	    : newJ3DNull(T_SCENELOADER)
	);
    }


    static J3DObject
    newShape3D(Shape3D shape) {

	return(shape != null
	    ? new J3DObject(new BodyShape3D(shape))
	    : newJ3DNull(T_SHAPE3D)
	);
    }


    static J3DObject
    newTexture(Texture texture) {

	J3DObject  obj;

	//
	// Eventually expect we'll support Texture3D.
	//

	if (texture instanceof Texture2D)
	    obj = new J3DObject(new BodyTexture2D((Texture2D)texture));
	else obj = newJ3DNull(T_TEXTURE2D);
	return(obj);
    }


    static J3DObject
    newTextureUnit(TextureUnitState textureunit) {

	return(textureunit != null
	    ? new J3DObject(new BodyTextureUnit(textureunit))
	    : newJ3DNull(T_TEXTUREUNIT)
	);
    }


    static J3DObject
    newTransform3D(Transform3D transform) {

	return(transform != null
	    ? new J3DObject(new BodyTransform3D(transform))
	    : newJ3DNull(T_TRANSFORM3D)
	);
    }


    static J3DObject
    newTransparencyAttributes(TransparencyAttributes attributes) {

	return(attributes != null
	    ? new J3DObject(new BodyTransparencyAttributes(attributes))
	    : newJ3DNull(T_TRANSPARENCYATTRIBUTES)
	);
    }


    static J3DObject
    newTriangleArray(TriangleArray array) {

	return(array != null
	    ? new J3DObject(new BodyTriangleArray(array))
	    : newJ3DNull(T_TRIANGLEARRAY)
	);
    }


    static J3DObject
    newTriangleFanArray(TriangleFanArray array) {

	return(array != null
	    ? new J3DObject(new BodyTriangleFanArray(array))
	    : newJ3DNull(T_TRIANGLEFANARRAY)
	);
    }


    static J3DObject
    newTriangleStripArray(TriangleStripArray array) {

	return(array != null
	    ? new J3DObject(new BodyTriangleStripArray(array))
	    : newJ3DNull(T_TRIANGLESTRIPARRAY)
	);
    }


    static J3DObject
    newVector3D(Vector3d vector) {

	J3DObject  obj;

	if (vector != null) {
	    obj = (J3DObject)YoixMake.yoixType(T_VECTOR3D);
	    obj.putDouble(NL_X, vector.x);
	    obj.putDouble(NL_Y, vector.y);
	    obj.putDouble(NL_Z, vector.z);
	} else obj = newJ3DNull(T_VECTOR3D);

	return(obj);
    }


    static J3DObject
    newVector3D(Vector3f vector) {

	J3DObject  obj;

	if (vector != null) {
	    obj = (J3DObject)YoixMake.yoixType(T_VECTOR3D);
	    obj.putDouble(NL_X, vector.x);
	    obj.putDouble(NL_Y, vector.y);
	    obj.putDouble(NL_Z, vector.z);
	} else obj = newJ3DNull(T_VECTOR3D);

	return(obj);
    }


    static J3DObject
    newViewPlatform(VirtualUniverse universe) {

	return(universe != null
	    ? new J3DObject(new BodyViewPlatform(universe))
	    : newJ3DNull(T_VIEWPLATFORM)
	);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

}

