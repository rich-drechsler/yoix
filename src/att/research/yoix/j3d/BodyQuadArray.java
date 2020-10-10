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
import java.util.HashMap;
import javax.media.j3d.Geometry;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RestrictedAccessException;
import att.research.yoix.*;

class BodyQuadArray extends BodyGeometryArray

    implements Constants

{

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	NL_COLORFORMAT,     $LR__,       null,
	NL_GENERATENORMALS, $LR__,       null,
	NL_TEXTUREFORMAT,   $LR__,       null,
	NL_VERTEXCOUNT,     $LR__,       null,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(15);

    static {
	activefields.put(NL_CAPABILITIES, new Integer(VL_CAPABILITIES));
	activefields.put(NL_COLORFORMAT, new Integer(VL_COLORFORMAT));
	activefields.put(NL_COLORS, new Integer(VL_COLORS));
	activefields.put(NL_COMPILED, new Integer(VL_COMPILED));
	activefields.put(NL_COORDINATES, new Integer(VL_COORDINATES));
	activefields.put(NL_DEFAULTCAPABILITY, new Integer(VL_DEFAULTCAPABILITY));
	activefields.put(NL_LIVE, new Integer(VL_LIVE));
	activefields.put(NL_NORMALS, new Integer(VL_NORMALS));
	activefields.put(NL_PATH, new Integer(VL_PATH));
	activefields.put(NL_TEXTURECOORDINATES, new Integer(VL_TEXTURECOORDINATES));
	activefields.put(NL_TEXTUREFORMAT, new Integer(VL_TEXTUREFORMAT));
	activefields.put(NL_VERTEXCOUNT, new Integer(VL_VERTEXCOUNT));
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
     // NAME                        CAPABILITY                                            VALUE
     // ----                        ----------                                            -----
    };

    static {
	loadCapabilities(BodyGeometryArray.class, BodyQuadArray.class);
	loadCapabilities(capabilities, BodyQuadArray.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyQuadArray(J3DObject data) {

	this(null, data);
    }


    BodyQuadArray(QuadArray geometryarray) {

	this(geometryarray, (J3DObject)VM.getTypeTemplate(T_QUADARRAY));
    }


    private
    BodyQuadArray(QuadArray geometryarray, J3DObject data) {

	super(geometryarray, data, 4);
	buildQuadArray(geometryarray);
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

	return(QUADARRAY);
    }

    ///////////////////////////////////
    //
    // BodyQuadArray Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj = null;
	int         field;

	try {
	    switch (field = activeField(name, activefields)) {
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
		    default:
			obj = setField(field, obj);
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
    buildQuadArray(QuadArray geometryarray) {

	if ((this.geometryarray = geometryarray) == null) {
	    this.geometryarray = new QuadArray(
		vertexCount(),
		vertexFormat(true, true)
	    );
	    peer = this.geometryarray;

	    setField(NL_DEFAULTCAPABILITY);
	    setField(NL_COORDINATES);
	    setField(NL_NORMALS);
	    setField(NL_COLORS);
	    setField(NL_TEXTURECOORDINATES);
	}
	setField(NL_CAPABILITIES);
    }
}

