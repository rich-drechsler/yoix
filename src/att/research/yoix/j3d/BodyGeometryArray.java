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
import javax.media.j3d.GeometryArray;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import att.research.yoix.*;

abstract
class BodyGeometryArray extends BodyNodeComponent

    implements Constants

{

    //
    // Decided to keep our own copy of peer to eliminate some casting.
    //

    protected GeometryArray  geometryarray = null;
    protected int            groupcount = 0;

    //
    // We may build the coordinates[] array and use it to determine the
    // vertex count, but only when NL_VERTEXCOUNT isn't properly set. If
    // we do we save the array until the first setCoordinates() call, so
    // we don't have to do it again. First setCoordinates() call will use
    // coordinates[] and then set it to null and after that it should stay
    // null.
    //

    private float  coordinates[] = null;

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
	"ALLOW_COLOR_READ",         new Integer(GeometryArray.ALLOW_COLOR_READ),          null,
	"ALLOW_COLOR_WRITE",        new Integer(GeometryArray.ALLOW_COLOR_WRITE),         null,
	"ALLOW_COORDINATE_READ",    new Integer(GeometryArray.ALLOW_COORDINATE_READ),     null,
	"ALLOW_COORDINATE_WRITE",   new Integer(GeometryArray.ALLOW_COORDINATE_WRITE),    null,
	"ALLOW_COUNT_READ",         new Integer(GeometryArray.ALLOW_COUNT_READ),          null,
	"ALLOW_COUNT_WRITE",        new Integer(GeometryArray.ALLOW_COUNT_WRITE),         null,
	"ALLOW_FORMAT_READ",        new Integer(GeometryArray.ALLOW_FORMAT_READ),         null,
	"ALLOW_NORMAL_READ",        new Integer(GeometryArray.ALLOW_NORMAL_READ),         null,
	"ALLOW_NORMAL_WRITE",       new Integer(GeometryArray.ALLOW_NORMAL_WRITE),        null,
	"ALLOW_TEXCOORD_READ",      new Integer(GeometryArray.ALLOW_TEXCOORD_READ),       null,
	"ALLOW_TEXCOORD_WRITE",     new Integer(GeometryArray.ALLOW_TEXCOORD_WRITE),      null,
    };

    static {
	loadCapabilities(capabilities, BodyGeometryArray.class);
	capabilities = null;
    }

    //
    // These are the names of the fields that we accept when we load floats
    // from a compound YoixObject.
    //

    private static String  COORDINATE_FIELDS[] = {NL_X, NL_Y, NL_Z, NL_W};
    private static String  COLOR_FIELDS[] = {NL_RED, NL_GREEN, NL_BLUE, NL_ALPHA};

    //
    // These may occasionally be conventient, but be warned that COLOR_3
    // and COLOR_4 probably share a bit so don't make assumptions when
    // you check for colors!!!
    //

    private static final int  COLOR_MASK = GeometryArray.COLOR_3 | GeometryArray.COLOR_4;

    private static final int  TEXTURE_MASK = GeometryArray.TEXTURE_COORDINATE_2
					   | GeometryArray.TEXTURE_COORDINATE_3
					   | GeometryArray.TEXTURE_COORDINATE_4;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyGeometryArray(J3DObject data, int groupcount) {

	super(data);
	this.groupcount = groupcount;
    }


    BodyGeometryArray(GeometryArray geometryarray, J3DObject data, int groupcount) {

	super(geometryarray, data);
	this.groupcount = groupcount;
    }

    ///////////////////////////////////
    //
    // BodyGeometryArray Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	geometryarray = null;
	super.finalize();
    }


    protected YoixObject
    getField(int field, YoixObject obj) {

	switch (field) {
	    case VL_COLORFORMAT:
		obj = getColorFormat(obj);
		break;

	    case VL_COLORS:
		obj = getColors(obj);
		break;

	    case VL_COORDINATES:
		obj = getCoordinates(obj);
		break;

	    case VL_NORMALS:
		obj = getNormals(obj);
		break;

	    case VL_TEXTURECOORDINATES:
		obj = getTextureCoordinates(obj);
		break;

	    case VL_TEXTUREFORMAT:
		obj = getTextureFormat(obj);
		break;

	    case VL_VERTEXCOUNT:
		obj = getVertexCount(obj);
		break;

	    default:
		obj = super.getField(field, obj);
		break;
	}
	return(obj);
    }


    protected YoixObject
    setField(int field, YoixObject obj) {

	if (obj != null) {
	    switch (field) {
		case VL_COLORS:
		    setColors(obj);
		    break;

		case VL_COORDINATES:
		    setCoordinates(obj);
		    break;

		case VL_NORMALS:
		    setNormals(obj);
		    break;

		case VL_TEXTURECOORDINATES:
		    setTextureCoordinates(obj);
		    break;

		default:
		    super.setField(field, obj);
		    break;
	    }
	}
	return(obj);
    }


    final int
    vertexCount() {

	int  count = -1;

	//
	// This is only supposed to be used once when the constructor in
	// the subclass decides it really needs to build geometryarray, so
	// we currently abort if geometryarray isn't null.
	//

	if (geometryarray == null) {
	    if ((count = getInt(NL_VERTEXCOUNT, -1)) <= 0) {
		if (coordinates == null)	// in case we're called more than once
		    coordinates = loadFloatArray(getObject(NL_COORDINATES), 3, COORDINATE_FIELDS);
		if (coordinates != null)
		    count = coordinates.length/3;
	    }
	    count = Math.max(count, Math.max(groupcount, 1));
	    if (groupcount > 1)
		count = ((count + groupcount - 1)/groupcount)*groupcount;
	} else VM.abort(INTERNALERROR);

	return(count);
    }


    final int
    vertexFormat(boolean allownormals, boolean allowtextures) {

	YoixObject  obj;
	int         format = 0;

	//
	// This is only supposed to be used once when the constructor in
	// the subclass decides it really needs to build geometryarray, so
	// we currently abort if geometryarray isn't null.
	//

	if (geometryarray == null) {
	    format = GeometryArray.COORDINATES;
	    format |= J3DMake.javaInt("GeometryArrayColor", getObject(NL_COLORFORMAT));
	    if (allowtextures)
	        format |= J3DMake.javaInt("GeometryArrayTexture", getObject(NL_TEXTUREFORMAT));
	    if (allownormals) {
		if (getBoolean(NL_GENERATENORMALS) == false) {
		    if ((obj = getObject(NL_NORMALS)) != null && obj.notNull())
			format |= GeometryArray.NORMALS;
		} else format |= GeometryArray.NORMALS;
	    }
	} else VM.abort(INTERNALERROR);

	return(format);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private int
    countColorComponents() {

	int  components;
	int  format;

	format = geometryarray.getVertexFormat();
	if ((format & COLOR_MASK) != 0) {
	    if ((format & GeometryArray.COLOR_4) == GeometryArray.COLOR_4)
		components = 4;
	    else components = 3;
	} else components = 0;
	return(components);
    }


    private int
    countNormalComponents() {

	int  components;

	if ((geometryarray.getVertexFormat() & GeometryArray.NORMALS) != 0)
	    components = 3;
	else components = 0;
	return(components);
    }


    private int
    countTextureComponents() {

	int  components;
	int  format;

	format = geometryarray.getVertexFormat();
	if ((format & TEXTURE_MASK) != 0) {
	    if ((format & GeometryArray.TEXTURE_COORDINATE_2) != 0)
		components = 2;
	    else if ((format & GeometryArray.TEXTURE_COORDINATE_3) != 0)
		components = 3;
	    else components = 4;
	} else components = 0;

	return(components);
    }


    private void
    generateNormals() {

	NormalGenerator  generator;
	GeometryArray    array;
	GeometryInfo     info;
	float            normals[];
	int              components;

	if ((components = countNormalComponents()) > 0) {
	    if (getBoolean(NL_GENERATENORMALS)) {
		info = new GeometryInfo(geometryarray);
		generator = new NormalGenerator();
		generator.generateNormals(info);
		array = info.getGeometryArray();
		normals = new float[components*array.getVertexCount()];
		array.getNormals(0, normals);
		geometryarray.setNormals(0, normals);
	    }
	}
    }


    private YoixObject
    getColorFormat(YoixObject obj) {

	return(J3DMake.yoixConstant("GeometryArrayColor", geometryarray.getVertexFormat() & COLOR_MASK));
    }


    private synchronized YoixObject
    getColors(YoixObject obj) {

	float  colors[];
	int    components;

	if ((components = countColorComponents()) > 0) {
	    colors = new float[components*geometryarray.getVertexCount()];
	    geometryarray.getColors(0, colors);
	    obj = YoixMisc.copyIntoArray(colors);
	} else obj = YoixObject.newArray();
	return(obj);
    }


    private synchronized YoixObject
    getCoordinates(YoixObject obj) {

	float  coords[];

	if ((geometryarray.getVertexFormat() & GeometryArray.COORDINATES) != 0) {
	    coords = new float[3*geometryarray.getVertexCount()];
	    geometryarray.getCoordinates(0, coords);
	    obj = YoixMisc.copyIntoArray(coords);
	} else obj = YoixObject.newArray();
	return(obj);
    }


    private synchronized YoixObject
    getNormals(YoixObject obj) {

	float  normals[];
	int    components;

	if ((components = countNormalComponents()) > 0) {
	    normals = new float[components*geometryarray.getVertexCount()];
	    geometryarray.getNormals(0, normals);
	    obj = YoixMisc.copyIntoArray(normals);
	} else obj = YoixObject.newArray();
	return(obj);
    }


    private synchronized YoixObject
    getTextureCoordinates(YoixObject obj) {

	float  coords[];
	int    components;

	if ((components = countTextureComponents()) > 0) {
	    coords = new float[components*geometryarray.getVertexCount()];
	    geometryarray.getTextureCoordinates(0, 0, coords);
	    obj = YoixMisc.copyIntoArray(coords);
	} else obj = YoixObject.newArray();
	return(obj);
    }


    private YoixObject
    getVertexCount(YoixObject obj) {

	return(YoixObject.newInt(geometryarray.getVertexCount()));
    }


    private YoixObject
    getTextureFormat(YoixObject obj) {

	return(J3DMake.yoixConstant("GeometryArrayTexture", geometryarray.getVertexFormat() & TEXTURE_MASK));
    }


    private float[]
    loadFloatArray(YoixObject obj, int components, String fields[]) {

	YoixObject  entry;
	float       coords[];
	float       temp[];
	int         count;
	int         length;
	int         l;
	int         m;
	int         n;

	if (obj != null && obj.notNull()) {
	    coords = new float[components*obj.sizeof()];	// overkill
	    length = obj.length();
	    for (m = 0, n = obj.offset(); n < length; n++) {
		if ((entry = obj.getObject(n)) != null) {
		    if (entry.isNumber() == false) {
			if (entry.compound()) {
			    while (m%components != 0)
				coords[m++] = 0.0f;
			    for (l = 0; l < components && l < fields.length; l++)
				coords[m++] = entry.getFloat(fields[l], 0);
			}
		    } else coords[m++] = entry.floatValue();
		}
	    }
	    if ((count = ((m + components - 1)/components)*components) < coords.length) {
		temp = new float[count];
		System.arraycopy(coords, 0, temp, 0, count);
		coords = temp;
	    }
	} else coords = null;

	return(coords);
    }


    private void
    setColors(YoixObject obj) {

	float  colors[];
	int    components;
	int    count;
	int    n;

	if (obj.notNull()) {
	    if ((count = geometryarray.getVertexCount()) > 0) {
		if ((components = countColorComponents()) > 0) {
		    colors = loadFloatArray(obj, components, COLOR_FIELDS);
		    data.putNull(NL_COLORS);
		    for (n = 0; n < colors.length; n++) {
			if (colors[n] < 0)
			    colors[n] = 0;
			else if (colors[n] > 1)
			    colors[n] = 1;
		    }
		    //
		    // We're careful here so we don't get suprise exceptions.
		    //
		    colors = setMinimumSize(colors, components*count);
		    geometryarray.setColors(0, colors, 0, count);
		}
	    }
	}
    }


    private synchronized void
    setCoordinates(YoixObject obj) {

	float  coords[];
	int    count;

	if (obj.notNull()) {
	    if ((count = geometryarray.getVertexCount()) > 0) {
		if ((coords = coordinates) == null)
		    coords = loadFloatArray(obj, 3, COORDINATE_FIELDS);
		else coordinates = null;
		data.putNull(NL_COORDINATES);
		//
		// We're careful here so we don't get suprise exceptions.
		//
		coords = setMinimumSize(coords, 3*count);
		geometryarray.setCoordinates(0, coords, 0, count);
		generateNormals();
	    }
	}
    }


    private void
    setNormals(YoixObject obj) {

	float   normals[];
	double  distance2;
	double  coordinate;
	float   scale;
	int     components;
	int     count;
	int     m;
	int     n;

	//
	// Using a depracated version of setTextureCoordinates() until we
	// get around to implementing the texture coordinate sets that are
	// supported by the other GeometryArray constructor. Important, so
	// we will get around to it, but it can easily wait for a while.
	//

	if (obj.notNull()) {
	    if ((count = geometryarray.getVertexCount()) > 0) {
		if ((components = countNormalComponents()) > 0) {
		    normals = loadFloatArray(obj, components, COORDINATE_FIELDS);
		    data.putNull(NL_NORMALS);
		    for (n = 0; n < normals.length; n += components) {
			distance2 = 0;
			for (m = n; m < n + components; m++) {
			    if ((coordinate = normals[m]) != 0)
				distance2 += coordinate*coordinate;
			}
			if (distance2 > 0) {
			    scale = (float)(1.0/Math.sqrt(distance2));
			    for (m = n; m < n + components; m++)
				normals[m] *= scale;
			}
		    }
		    //
		    // We're careful here so we don't get suprise exceptions.
		    //
		    normals = setMinimumSize(normals, components*count);
		    geometryarray.setNormals(0, normals, 0, count);
		}
	    }
	}
    }


    private void
    setTextureCoordinates(YoixObject obj) {

	float  coords[];
	int    components;
	int    count;

	//
	// Using a depracated version of setTextureCoordinates() until we
	// get around to implementing the texture coordinate sets that are
	// supported by the other GeometryArray constructor. Important, so
	// we will get around to it, but it can easily wait for a while.
	//

	if (obj.notNull()) {
	    if ((count = geometryarray.getVertexCount()) > 0) {
		if ((components = countTextureComponents()) > 0) {
		    coords = loadFloatArray(obj, components, COORDINATE_FIELDS);
		    data.putNull(NL_TEXTURECOORDINATES);
		    //
		    // We're careful here so we don't get suprise exceptions.
		    //
		    coords = setMinimumSize(coords, components*count);
		    geometryarray.setTextureCoordinates(0, coords, 0, count);
		}
	    }
	}
    }


    private static float[]
    setMinimumSize(float values[], int length) {

	float  temp[];

	if (values.length < length) {
	    temp = new float[length];
	    System.arraycopy(values, 0, temp, 0, values.length);
	    values = temp;
	}
	return(values);
    }
}

