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
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Node;
import javax.media.j3d.PointLight;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Transform3D;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.geometry.Text2D;
import com.sun.j3d.utils.image.TextureLoader;
import att.research.yoix.*;

public abstract
class Make

    implements Constants

{

    //
    // There's lots missing in yoixSceneGraphObject(), which handles the
    // translation of objects in loaded scenes into their Yoix equivalents,
    // but it will be updated. Missing objects end up as a generic Yoix
    // ScenGraphObject, which is only supposed to be a placeholder that
    // marks objects that need to be handled in yoixSceneGraphObject().
    //

    ///////////////////////////////////
    //
    // Make Methods
    //
    ///////////////////////////////////

    public static AxisAngle4d
    javaAxisAngle4d(YoixObject obj) {

	AxisAngle4d  axisangle;

	if (obj != null && obj.notNull()) {
	    axisangle = new AxisAngle4d(
		obj.getDouble(NL_X, 0.0),
		obj.getDouble(NL_Y, 0.0),
		obj.getDouble(NL_Z, 0.0),
		obj.getDouble(NL_ANGLE, 0.0) * Math.PI/180.0
	    );
	} else axisangle = new AxisAngle4d();

	return(axisangle);
    }


    public static Color3f
    javaColor3f(YoixObject obj, Color3f color) {

	if (obj != null) {
	    color = new Color3f(
		(float)Math.max(0.0, Math.min(obj.getDouble(N_RED, color != null ? color.x : 0), 1.0)),
		(float)Math.max(0.0, Math.min(obj.getDouble(N_GREEN, color != null ? color.y : 0), 1.0)),
		(float)Math.max(0.0, Math.min(obj.getDouble(N_BLUE, color != null ? color.z : 0), 1.0))
	    );
	}
	return(color);
    }


    public static Color4f
    javaColor4f(YoixObject obj, Color4f color) {

	if (obj != null) {
	    color = new Color4f(
		(float)Math.max(0.0, Math.min(obj.getDouble(N_RED, color != null ? color.x : 0), 1.0)),
		(float)Math.max(0.0, Math.min(obj.getDouble(N_GREEN, color != null ? color.y : 0), 1.0)),
		(float)Math.max(0.0, Math.min(obj.getDouble(N_BLUE, color != null ? color.z : 0), 1.0)),
		(float)Math.max(0.0, Math.min(obj.getDouble(NL_ALPHA, color != null ? color.w : 0), 1.0))
	    );
	}
	return(color);
    }


    public static ImageComponent2D
    javaImageComponent2D(YoixObject obj) {

	ImageComponent2D  imagecomponent = null;
	Image             image;

	if ((image = YoixMake.javaImage(obj)) != null) {
	    if (image instanceof BufferedImage) {
		imagecomponent = new ImageComponent2D(
		    pickFormat(image),
		    (BufferedImage)image,
		    obj.getBoolean(NL_BYREFERENCE, false),
		    obj.getBoolean(NL_YUP, false)
		);
	    }
	}
	return(imagecomponent);
    }


    public static Point3d
    javaPoint3d(YoixObject obj) {

	Point3d  point;

	if (obj != null && obj.notNull()) {
	    point = new Point3d(
		obj.getDouble(NL_X, 0.0),
		obj.getDouble(NL_Y, 0.0),
		obj.getDouble(NL_Z, 0.0)
	    );
	} else point = new Point3d();

	return(point);
    }


    public static Quat4d
    javaQuat4d(YoixObject obj) {

	Quat4d  quat;

	if (obj != null && obj.notNull()) {
	    quat = new Quat4d(
		obj.getDouble(NL_X, 0.0),
		obj.getDouble(NL_Y, 0.0),
		obj.getDouble(NL_Z, 0.0),
		obj.getDouble(NL_W, 0.0)
	    );
	} else quat = new Quat4d();

	return(quat);
    }


    public static Quat4d
    javaQuat4dFromOrientation3D(YoixObject obj) {

	AxisAngle4d  axisangle;
	Transform3D  t;
	Vector3d     vector;
	Quat4d       q4d;
	double       wb;
	double       sin;
	double       cos;

	if (J3DObject.isAxisAngle(obj)) {
	    q4d = new Quat4d();
	    q4d.set(Make.javaAxisAngle4d(obj));
	} else if (J3DObject.isEulerAngle(obj)) {
	    t = new Transform3D();
	    t.setEuler(Make.javaVector3d(obj));
	    q4d = new Quat4d();
	    t.get(q4d);
	} else if (J3DObject.isQuat4D(obj)) {
	    q4d = Make.javaQuat4d(obj);
	} else if (obj.isNumber()) {
	    q4d = new Quat4d();
	    wb = YoixMisc.toRadians(obj.doubleValue());
	    sin = Math.sin(wb);
	    cos = Math.cos(wb);
	    wb = 0.5 * (1 + cos);
	    if (wb >= 1e-30) {
		q4d.w = Math.sqrt(wb);
		q4d.x = 0;
		q4d.z = 0;
		q4d.y = 0.5 * sin / q4d.w;
	    } else {
		q4d.x = 0;
		q4d.y = 0;
		q4d.z = 1;
		q4d.w = 0;
	    }
	} else q4d = new Quat4d(); // or null??

	return(q4d);
    }


    public static Texture
    javaTexture(YoixObject obj, String format, boolean mipmap) {

	TextureLoader  loader = null;
	Texture        texture;
	String         path;
	int            flags;

	//
	// Unfortunately TextureLoader issues its own error messages using
	// System.err when it can't open a file or URL. We could eliminate
	// the error messages (and issue our own) by redirecting System.err
	// to a ByteArrayOutputStream(), but that would affect all errors
	// issued by all threads while the redirection was in effect, so
	// we decided against it.
	//

	if (obj != null && obj.notNull()) {
	    flags = mipmap ? TextureLoader.GENERATE_MIPMAP : 0;
	    if (obj.getBoolean(NL_YUP, false))
		flags |= TextureLoader.Y_UP;
	    if (obj.getBoolean(NL_BYREFERENCE, false))
		flags |= TextureLoader.BY_REFERENCE;
	    if (obj.isDictionary())
		obj = obj.getObject(NL_SOURCE);
	    if (obj != null) {
		if (obj.isImage()) {
		    loader = new TextureLoader(
			(BufferedImage)YoixMake.javaImage(obj),
			format,
			flags
		    );
		} else if (obj.isString()) {
		    try {
			path = obj.stringValue();
			switch (YoixMisc.guessStreamType(path)) {
			    case FILE:
				loader = new TextureLoader(path, format, flags, null);
				break;

			    case URL:
				loader = new TextureLoader(new URL(path), format, flags, null);
				break;
			}
		    }
		    catch(IOException e) {}
		}
	    }
	    texture = (loader != null) ? loader.getTexture() : new Texture2D();
	} else texture = new Texture2D();

	return(texture);
    }


    public static Transform3D
    javaTransform3D(YoixObject obj) {

	Transform3D  transform;

	if (obj != null && obj.notNull()) {
	    if (J3DObject.isTransform3D(obj))
		transform = ((J3DObject)obj).getManagedTransform();
	    else transform = new Transform3D();
	} else transform = new Transform3D();

	return(transform);
    }


    public static Vector3d
    javaVector3d(YoixObject obj) {

	Vector3d  vector;

	if (obj != null && obj.notNull()) {
	    if (J3DObject.isEulerAngle(obj)) {
		vector = new Vector3d(
		    obj.getDouble(NL_ALPHA, 0.0) * Math.PI/180.0,
		    obj.getDouble(NL_BETA, 0.0) * Math.PI/180.0,
		    obj.getDouble(NL_GAMMA, 0.0) * Math.PI/180.0
		);
	    } else {
		vector = new Vector3d(
		    obj.getDouble(NL_X, 0.0),
		    obj.getDouble(NL_Y, 0.0),
		    obj.getDouble(NL_Z, 0.0)
		);
	    }
	} else vector = new Vector3d();

	return(vector);
    }


    public static Vector3f
    javaVector3f(YoixObject obj) {

	Vector3f  vector;

	if (obj != null && obj.notNull()) {
	    if (J3DObject.isEulerAngle(obj)) {
		vector = new Vector3f(
		    (float)(obj.getDouble(NL_ALPHA, 0.0) * Math.PI/180.0),
		    (float)(obj.getDouble(NL_BETA, 0.0) * Math.PI/180.0),
		    (float)(obj.getDouble(NL_GAMMA, 0.0) * Math.PI/180.0)
		);
	    } else {
		vector = new Vector3f(
		    obj.getFloat(NL_X, 0.0),
		    obj.getFloat(NL_Y, 0.0),
		    obj.getFloat(NL_Z, 0.0)
		);
	    }
	} else vector = new Vector3f();

	return(vector);
    }


    public static YoixObject
    yoixColor(Color3f color) {

	YoixObject  obj;

	obj = YoixMake.yoixType(T_COLOR);

	if (color != null) {
	    obj.putDouble(N_RED, color.x);
	    obj.putDouble(N_GREEN, color.y);
	    obj.putDouble(N_BLUE, color.z);
        } else obj = YoixObject.newNull(obj);

        return(obj);
    }


    public static YoixObject
    yoixColor(Color4f color) {

	YoixObject  obj;

	obj = YoixMake.yoixType(T_COLOR);

	if (color != null) {
	    obj.putDouble(N_RED, color.x);
	    obj.putDouble(N_GREEN, color.y);
	    obj.putDouble(N_BLUE, color.z);
        } else obj = YoixObject.newNull(obj);

        return(obj);
    }


    public static J3DObject
    yoixSceneGraphObject(Scene scene, SceneGraphObject sgo, String tag) {

	J3DObject  obj;

	//
	// This method will be updated to handle new SceneGraphObjects when
	// as they're supported. In addition we undoubtedly will have to map
	// some Java SceneGraphObjects (e.g., TransformGroup) into others
	// that are supported (e.g., BranchGroup).
	//

	if (sgo != null) {
	    if (sgo instanceof Node) {
		if (sgo instanceof BranchGroup)
		    obj = new J3DObject(new BodyBranchGroup((BranchGroup)sgo, tag));
		else if (sgo instanceof Shape3D)
		    obj = new J3DObject(new BodyShape3D((Shape3D)sgo, tag));
		else if (sgo instanceof Sphere)
		    obj = new J3DObject(new BodySphere((Sphere)sgo, tag));
		else if (sgo instanceof ColorCube)
		    obj = new J3DObject(new BodyColorCube((ColorCube)sgo, tag));
		else if (sgo instanceof Text2D)
		    obj = new J3DObject(new BodyText2D((Text2D)sgo, tag));
		else if (sgo instanceof AmbientLight)
		    obj = new J3DObject(new BodyAmbientLight((AmbientLight)sgo, tag));
		else if (sgo instanceof DirectionalLight)
		    obj = new J3DObject(new BodyDirectionalLight((DirectionalLight)sgo, tag));
		else if (sgo instanceof PointLight)
		    obj = new J3DObject(new BodyPointLight((PointLight)sgo, tag));
		else obj = yoixPlaceholder(T_SCENEGRAPHOBJECT, sgo.toString(), tag);
	    } else obj = yoixPlaceholder(T_SCENEGRAPHOBJECT, sgo.toString(), tag);
	} else obj = J3DObject.newJ3DNull(T_SCENEGRAPHOBJECT);
	return(obj);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static int
    pickFormat(Image image) {

	int  format = ImageComponent2D.FORMAT_RGB;

	//
	// Currently only used by javaImageComponent2D, but we undoubtedly
	// will need it again.
	//

	if (image instanceof BufferedImage) {
	    switch (((BufferedImage)image).getType()) {
		case BufferedImage.TYPE_INT_RGB:
		case BufferedImage.TYPE_INT_BGR:
		case BufferedImage.TYPE_3BYTE_BGR:
		    format = ImageComponent2D.FORMAT_RGB;
		    break;

		case BufferedImage.TYPE_USHORT_565_RGB:
		case BufferedImage.TYPE_USHORT_555_RGB:
		    format = ImageComponent2D.FORMAT_RGB5;
		    break;

		case BufferedImage.TYPE_INT_ARGB:
		case BufferedImage.TYPE_INT_ARGB_PRE:
		case BufferedImage.TYPE_4BYTE_ABGR:
		case BufferedImage.TYPE_4BYTE_ABGR_PRE:
		    format = ImageComponent2D.FORMAT_RGBA;
		    break;

		case BufferedImage.TYPE_BYTE_BINARY:
		case BufferedImage.TYPE_BYTE_GRAY:
		case BufferedImage.TYPE_USHORT_GRAY:
		    format = ImageComponent2D.FORMAT_CHANNEL8;
		    break;

		case BufferedImage.TYPE_BYTE_INDEXED:
		    format = ImageComponent2D.FORMAT_RGB;
		    break;
	    }
	}
	return(format);
    }


    private static J3DObject
    yoixPlaceholder(String typename, String info, String tag) {

	J3DObject  obj;

	obj = (J3DObject)YoixMake.yoixType(typename);
	obj.putString(NL_ETC, info);
	obj.putString(NL_TAG, tag);
	return(obj);
    }
}

