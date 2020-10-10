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
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Texture;
import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;
import att.research.yoix.*;

abstract
class BodyTexture extends BodyNodeComponent

    implements Constants

{

    //
    // Decided to keep our own copy of peer to eliminate some casting.
    //

    protected Texture  texture = null;

    //
    // A table that's used to control capabilities - low level setup
    // happens once when the loadCapabilities() methods are called in
    // the static initialization block that follows the table. Current
    // implementation seems error prone because we're required to pass
    // the correct classes to loadCapabilities(), so be careful if you
    // copy this stuff to different classes!!
    //
    // Some of these capabilities apply to several fields rather than
    // just one, so our capability support code probably should accept
    // an array of field names. When that happens we should be able to
    // complete the table. Actually not 100% convinced by the way we
    // currently use the third column - would an extra READ|WRITE arg
    // give us better control? If not can we safely look for a _READ
    // or _WRITE suffix in the name? No9t sure if adding stuff would
    // really help matters?
    //

    private static Object  capabilities[] = {
     //
     // NAME                              CAPABILITY                                           VALUE
     // ----                              ----------                                           -----
	"ALLOW_ANISOTROPIC_FILTER_READ",  new Integer(Texture.ALLOW_ANISOTROPIC_FILTER_READ),  null,
	"ALLOW_BOUNDARY_COLOR_READ",      new Integer(Texture.ALLOW_BOUNDARY_COLOR_READ),      null,
	"ALLOW_BOUNDARY_MODE_READ",       new Integer(Texture.ALLOW_BOUNDARY_MODE_READ),       null,
	"ALLOW_ENABLE_READ",              new Integer(Texture.ALLOW_ENABLE_READ),              null,
	"ALLOW_ENABLE_WRITE",             new Integer(Texture.ALLOW_ENABLE_WRITE),             null,
	"ALLOW_FILTER_READ",              new Integer(Texture.ALLOW_FILTER_READ),              null,
	"ALLOW_FILTER4_READ",             new Integer(Texture.ALLOW_FILTER4_READ),             null,
	"ALLOW_FORMAT_READ",              new Integer(Texture.ALLOW_FORMAT_READ),              null,
	"ALLOW_IMAGE_READ",               new Integer(Texture.ALLOW_IMAGE_READ),               null,
	"ALLOW_IMAGE_WRITE",              new Integer(Texture.ALLOW_IMAGE_WRITE),              null,
	"ALLOW_LOD_RANGE_READ",           new Integer(Texture.ALLOW_LOD_RANGE_READ),           null,
	"ALLOW_LOD_RANGE_WRITE",          new Integer(Texture.ALLOW_LOD_RANGE_WRITE),          null,
	"ALLOW_MIPMAP_MODE_READ",         new Integer(Texture.ALLOW_MIPMAP_MODE_READ),         null,
	"ALLOW_SHARPEN_TEXTURE_READ",     new Integer(Texture.ALLOW_SHARPEN_TEXTURE_READ),     null,
	"ALLOW_SIZE_READ",                new Integer(Texture.ALLOW_SIZE_READ),                null,
    };

    static {
	loadCapabilities(capabilities, BodyTexture.class);
	capabilities = null;
    }

    private static final double  LOG2 = Math.log(2);

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyTexture(J3DObject data) {

	super(data);
    }


    BodyTexture(Texture texture, J3DObject data) {

	super(texture, data);
    }

    ///////////////////////////////////
    //
    // BodyTexture Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	texture = null;
	super.finalize();
    }


    protected final YoixObject
    getField(int field, YoixObject obj) {

	switch (field) {
	    case VL_ANISOTROPICDEGREE:
		obj = getAnisotropicDegree(obj);
		break;

	    case VL_BASELEVEL:
		obj = getBaseLevel(obj);
		break;

	    case VL_BOUNDARY:
		obj = getBoundary(obj);
		break;

	    case VL_ENABLED:
		obj = getEnabled(obj);
		break;

	    case VL_FILTER4:
		obj = getFilter4(obj);
		break;

	    case VL_FORMAT:
		obj = getFormat(obj);
		break;

	    case VL_HEIGHT:
		obj = getHeight(obj);
		break;

	    case VL_IMAGE:
		obj = getImage(obj);
		break;

	    case VL_LODOFFSET:
		obj = getLODOffset(obj);
		break;

	    case VL_LODRANGE:
		obj = getLODRange(obj);
		break;

	    case VL_MAGFILTER:
		obj = getMagFilter(obj);
		break;

	    case VL_MAXIMUMLEVEL:
		obj = getMaximumLevel(obj);
		break;

	    case VL_MINFILTER:
		obj = getMinFilter(obj);
		break;

	    case VL_MIPMAP:
		obj = getMipMap(obj);
		break;

	    case VL_MIPMAPLEVELS:
		obj = getMipMapLevels(obj);
		break;

	    case VL_SHARPEN:
		obj = getSharpen(obj);
		break;

	    case VL_WIDTH:
		obj = getWidth(obj);
		break;

	    default:
		obj = super.getField(field, obj);
		break;
	}
	return(obj);
    }


    protected final YoixObject
    setField(int field, YoixObject obj) {

	if (obj != null) {
	    switch (field) {
		case VL_ANISOTROPICDEGREE:
		    setAnisotropicDegree(obj);
		    break;

		case VL_BASELEVEL:
		    setBaseLevel(obj);
		    break;

		case VL_BOUNDARY:
		    setBoundary(obj);
		    break;

		case VL_ENABLED:
		    setEnabled(obj);
		    break;

		case VL_FILTER4:
		    setFilter4(obj);
		    break;

		case VL_LODOFFSET:
		    setLODOffset(obj);
		    break;

		case VL_LODRANGE:
		    setLODRange(obj);
		    break;

		case VL_MAGFILTER:
		    setMagFilter(obj);
		    break;

		case VL_MAXIMUMLEVEL:
		    setMaximumLevel(obj);
		    break;

		case VL_MINFILTER:
		    setMinFilter(obj);
		    break;

		case VL_SHARPEN:
		    setSharpen(obj);
		    break;

		default:
		    super.setField(field, obj);
		    break;
	    }
	}
	return(obj);
    }


    protected final void
    updateCapabilities() {

	ImageComponent  images[];
	boolean         readable;
	boolean         writable;
	int             n;

	if ((images = texture.getImages()) != null) {
	    readable = texture.getCapability(Texture.ALLOW_IMAGE_READ);
	    writable = texture.getCapability(Texture.ALLOW_IMAGE_WRITE);
	    for (n = 0; n < images.length; n++) {
		changeCapability(images[n], ImageComponent.ALLOW_FORMAT_READ, readable);
		changeCapability(images[n], ImageComponent.ALLOW_IMAGE_READ, readable);
		changeCapability(images[n], ImageComponent.ALLOW_SIZE_READ, readable);
		changeCapability(images[n], ImageComponent.ALLOW_IMAGE_WRITE, writable);
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private YoixObject
    getAnisotropicDegree(YoixObject obj) {

	return(YoixObject.newDouble(texture.getAnisotropicFilterDegree()));
    }


    private YoixObject
    getBaseLevel(YoixObject obj) {

	return(YoixObject.newInt(texture.getBaseLevel()));
    }


    private YoixObject
    getBoundary(YoixObject obj) {

	Color4f  color;

	//
	// We're omitting width because we currently use TextureLoader to
	// build testure and there's no way to specify a boundary width.
	//

	color = new Color4f();
	texture.getBoundaryColor(color);
	obj = YoixObject.newDictionary(4);
	obj.put(NL_COLOR, Make.yoixColor(color));
	obj.putDouble(NL_ALPHA, Math.max(0.0, Math.min(color.w, 1.0)));
	obj.put(NL_S, J3DMake.yoixConstant("TextureBoundaryMode", texture.getBoundaryModeS()));
	obj.put(NL_T, J3DMake.yoixConstant("TextureBoundaryMode", texture.getBoundaryModeT()));

	return(obj);
    }


    private YoixObject
    getEnabled(YoixObject obj) {

	return(YoixObject.newInt(texture.getEnable()));
    }

    private YoixObject
    getFilter4(YoixObject obj) {

	float  weights[];
	int    count;
	int    n;

	if ((count = texture.getFilter4FuncPointsCount()) > 0) {
	    obj = YoixObject.newArray(count);
	    weights = new float[count];
	    texture.getFilter4Func(weights);
	    for (n = 0; n < count; n++)
		obj.putDouble(n, weights[n]);
	} else obj = YoixObject.newArray();
	return(obj);
    }


    private YoixObject
    getFormat(YoixObject obj) {

	return(J3DMake.yoixConstant("TextureFormat", texture.getFormat()));
    }


    private YoixObject
    getHeight(YoixObject obj) {

	return(YoixObject.newInt(texture.getHeight()));
    }


    private YoixObject
    getImage(YoixObject obj) {

	ImageComponent  component;
	Image           image = null;

	if ((component = texture.getImage(0)) != null) {
	    if (component instanceof ImageComponent2D)
		image = ((ImageComponent2D)component).getImage();
	}
	return(YoixObject.newImage(image));
    }


    private YoixObject
    getLODOffset(YoixObject obj) {

	Vector3f  offset;

	offset = new Vector3f();
	texture.getLodOffset(offset);
	obj = YoixObject.newArray(3);
	obj.putDouble(0, offset.x);
	obj.putDouble(1, offset.y);
	obj.putDouble(2, offset.z);
	return(obj);
    }


    private YoixObject
    getLODRange(YoixObject obj) {

	obj = YoixObject.newArray(2);
	obj.putDouble(0, texture.getMinimumLOD());
	obj.putDouble(1, texture.getMaximumLOD());
	return(obj);
    }


    private YoixObject
    getMagFilter(YoixObject obj) {

	return(J3DMake.yoixConstant("TextureMagFilter", texture.getMagFilter()));
    }


    private YoixObject
    getMaximumLevel(YoixObject obj) {

	return(YoixObject.newInt(texture.getMaximumLevel()));
    }


    private YoixObject
    getMinFilter(YoixObject obj) {

	return(J3DMake.yoixConstant("TextureMinFilter", texture.getMinFilter()));
    }


    private YoixObject
    getMipMap(YoixObject obj) {

	return(YoixObject.newInt(texture.getMipMapMode() != Texture.BASE_LEVEL));
    }


    private YoixObject
    getMipMapLevels(YoixObject obj) {

	return(YoixObject.newInt(texture.numMipMapLevels()));
    }


    private YoixObject
    getSharpen(YoixObject obj) {

	float  lod[];
	float  pts[];
	int    count;
	int    m;
	int    n;

	if ((count = texture.getSharpenTextureFuncPointsCount()) > 0) {
	    lod = new float[count];
	    pts = new float[count];
	    texture.getSharpenTextureFunc(lod, pts);
	    obj = YoixObject.newArray(2*count);
	    for (n = 0, m = 0; n < count; n++) {
		obj.putDouble(m++, lod[n]);
		obj.putDouble(m++, pts[n]);
	    }
	} else obj = YoixObject.newArray();
	return(obj);
    }


    private YoixObject
    getWidth(YoixObject obj) {

	return(YoixObject.newInt(texture.getWidth()));
    }


    private void
    setAnisotropicDegree(YoixObject obj) {

	float  degree;

	//
	// Documentation claims an exception is thrown when the degree
	// exceeds a maximum, but that seems to be a mistake particularly
	// because maximum values seem to associated with Canvas3Ds not
	// Texture so that part of the documentation doesn't make much
	// sense!!!
	//
	// Documentation also claims this can't be set if we're part of
	// a live or compiled scene - there's no capability that lets us
	// write!!
	//

	degree = Math.max(1.0f, obj.floatValue());
	texture.setAnisotropicFilterMode(degree > 1.0 ? Texture.ANISOTROPIC_SINGLE_VALUE : Texture.ANISOTROPIC_NONE);
	texture.setAnisotropicFilterDegree(degree);
    }


    private void
    setBaseLevel(YoixObject obj) {

	texture.setBaseLevel(Math.max(0, Math.min(obj.intValue(), texture.getMaximumLevel())));
    }


    private void
    setBoundary(YoixObject obj) {

	Color4f  color;
	int      s;
	int      t;

	//
	// The boundary width can only be set through a constructor, so we
	// have to ignore it here. Also turns out there's currently no way
	// to use it when we first build texture because TextureLoader is
	// being used. Bottom line is the width field probably shouldn't
	// even be filled in by getBoundary().
	//

	if (obj.notNull()) {
	    color = Make.javaColor4f(obj.getObject(NL_COLOR), new Color4f());
	    s = J3DMake.javaInt("TextureBoundaryMode", obj.getObject(NL_S));
	    t = J3DMake.javaInt("TextureBoundaryMode", obj.getObject(NL_T));
	} else {
	    color = new Color4f();
	    s = J3DMake.javaInt("TextureBoundaryMode", YoixObject.newNull());
	    t = s;
	}
	texture.setBoundaryColor(color);
	texture.setBoundaryModeS(s);
	texture.setBoundaryModeT(t);
    }


    private void
    setEnabled(YoixObject obj) {

	texture.setEnable(obj.booleanValue());
    }


    private void
    setFilter4(YoixObject obj) {

	float  weights[];
	int    count;
	int    m;
	int    n;

	count = obj.sizeof();
	weights = new float[Math.max(count, 4)];
	for (n = 0, m = obj.offset(); n < count; n++, m++)
	    weights[n] = obj.getFloat(m, 0);
	texture.setFilter4Func(weights);
    }


    private void
    setLODOffset(YoixObject obj) {

	YoixObject  element;
	Vector3f    offset;

	if (obj.isArray() || obj.isNull()) {
	    offset = new Vector3f();
	    if (obj.notNull()) {
		if ((element = obj.getObject(0)) != null) {
		    if (element.isNumber()) {
			offset.x = element.floatValue();
			if ((element = obj.getObject(1)) != null) {
			    if (element.isNumber()) {
				offset.y = element.floatValue();
				if ((element = obj.getObject(2)) != null) {
				    if (element.isNumber())
					offset.z = element.floatValue();
				    else VM.abort(BADVALUE, NL_LODOFFSET, 2);
				} else VM.abort(BADVALUE, NL_LODOFFSET);
			    } else VM.abort(BADVALUE, NL_LODOFFSET, 1);
			} else VM.abort(BADVALUE, NL_LODOFFSET);
		    } else VM.abort(BADVALUE, NL_LODOFFSET, 0);
		} else VM.abort(BADVALUE, NL_LODOFFSET);
	    }
	    texture.setLodOffset(offset);
	} else VM.abort(TYPECHECK, NL_LODOFFSET);
    }


    private void
    setLODRange(YoixObject obj) {

	YoixObject  element;
	float       maximum;
	float       minimum;

	if (obj.isArray() || obj.isNull()) {
	    minimum = -1000.0f;
	    maximum = 1000.0f;
	    if (obj.notNull()) {
		if ((element = obj.getObject(0)) != null) {
		    if (element.isNumber()) {
			minimum = element.floatValue();
			if ((element = obj.getObject(1)) != null) {
			    if (element.isNumber())
				maximum = Math.max(element.floatValue(), minimum);
			    else VM.abort(BADVALUE, NL_LODRANGE, 1);
			} else VM.abort(BADVALUE, NL_LODRANGE);
		    } else VM.abort(BADVALUE, NL_LODRANGE, 0);
		} else VM.abort(BADVALUE, NL_LODRANGE);
	    }
	    texture.setMinimumLOD(minimum);
	    texture.setMaximumLOD(maximum);
	} else VM.abort(TYPECHECK, NL_LODRANGE);
    }


    private void
    setMagFilter(YoixObject obj) {

	texture.setMagFilter(J3DMake.javaInt("TextureMagFilter", obj));
    }


    private void
    setMaximumLevel(YoixObject obj) {

	int  width;
	int  height;
	int  maximum;
	int  minimum;

	if (texture.getMipMapMode() == Texture.MULTI_LEVEL_MIPMAP) {
	    if ((width = texture.getWidth()) > 1 && (height = texture.getWidth()) > 1) {
		minimum = texture.getBaseLevel();
		maximum = (int)(Math.log(Math.max(width, height))/LOG2);
	        texture.setMaximumLevel(Math.max(minimum, Math.min(obj.intValue(), maximum)));
	    }
	}
    }


    private void
    setMinFilter(YoixObject obj) {

	texture.setMinFilter(J3DMake.javaInt("TextureMinFilter", obj));
    }


    private void
    setSharpen(YoixObject obj) {

	float  lod[];
	float  pts[];
	int    count;
	int    m;
	int    n;

	count = obj.sizeof()/2;
	lod = new float[count];
	pts = new float[count];
	for (n = 0, m = obj.offset(); n < count; n++) {
	    lod[n] = obj.getFloat(m++, 0);
	    pts[n] = obj.getFloat(m++, 0);
	}
	texture.setSharpenTextureFunc(lod, pts);
    }
}

