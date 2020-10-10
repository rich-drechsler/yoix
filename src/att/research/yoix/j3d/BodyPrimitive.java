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
import com.sun.j3d.utils.geometry.Primitive;
import att.research.yoix.*;

abstract
class BodyPrimitive extends BodyNode

    implements Constants

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
     // NAME                        CAPABILITY                                 VALUE
     // ----                        ----------                                 -----
    };

    static {
	loadCapabilities(BodyNode.class, BodyPrimitive.class);
	loadCapabilities(capabilities, BodyPrimitive.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyPrimitive(J3DObject data) {

	super(data);
    }


    BodyPrimitive(Primitive primitive, J3DObject data) {

	super(primitive, data);
    }


    BodyPrimitive(Primitive primitive, J3DObject data, String tag) {

	super(primitive, data, tag);
    }

    ///////////////////////////////////
    //
    // BodyPrimitive Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	super.finalize();
    }


    protected final YoixObject
    getField(int field, YoixObject obj) {

	switch (field) {
	    case VL_GENERATENORMALS:
		obj = getGenerateNormals(obj);
		break;

	    case VL_GENERATETEXTURECOORDS:
		obj = getGenerateTextureCoords(obj);
		break;

	    case VL_SHARED:
		obj = getShared(obj);
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

    private YoixObject
    getGenerateNormals(YoixObject obj) {

	int  flags;
	int  value;

	flags = ((Primitive)peer).getPrimitiveFlags();
	if ((flags & Primitive.GENERATE_NORMALS) != 0)
	    value = ((flags & Primitive.GENERATE_NORMALS_INWARD) != 0) ? -1 : 1;
	else value = 0;

	return(YoixObject.newInt(value));
    }


    private YoixObject
    getGenerateTextureCoords(YoixObject obj) {

	return(YoixObject.newInt((((Primitive)peer).getPrimitiveFlags() & Primitive.GENERATE_TEXTURE_COORDS) != 0));
    }


    private YoixObject
    getShared(YoixObject obj) {

	return(YoixObject.newInt((((Primitive)peer).getPrimitiveFlags() & Primitive.GEOMETRY_NOT_SHARED) == 0));
    }
}

