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
import javax.media.j3d.GeometryStripArray;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import att.research.yoix.*;

abstract
class BodyGeometryStripArray extends BodyGeometryArray

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
     // NAME                        CAPABILITY                                            VALUE
     // ----                        ----------                                            -----
    };

    static {
	loadCapabilities(BodyGeometryArray.class, BodyGeometryStripArray.class);
	loadCapabilities(capabilities, BodyGeometryStripArray.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyGeometryStripArray(J3DObject data, int groupcount) {

	super(data, groupcount);
    }


    BodyGeometryStripArray(GeometryArray geometryarray, J3DObject data, int groupcount) {

	super(geometryarray, data, groupcount);
    }

    ///////////////////////////////////
    //
    // BodyGeometryStripArray Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	super.finalize();
    }


    protected final YoixObject
    getField(int field, YoixObject obj) {

	switch (field) {
	    case VL_STRIPVERTEXCOUNTS:
		obj = getStripVertexCounts(obj);
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
		case VL_STRIPVERTEXCOUNTS:
		    setStripVertexCounts(obj);
		    break;

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

    private synchronized YoixObject
    getStripVertexCounts(YoixObject obj) {

	int  counts[];
	int  length;

	length = ((GeometryStripArray)geometryarray).getNumStrips();
	counts = new int[length];
	((GeometryStripArray)geometryarray).getStripVertexCounts(counts);

	return(YoixMisc.copyIntoArray(counts));
    }


    private synchronized void
    setStripVertexCounts(YoixObject obj) {

	YoixObject  entry;
	int         counts[];
	int         temp[];
	int         vertexcount;
	int         length;
	int         total;
	int         count;
	int         m;
	int         n;

	if (obj.notNull()) {
	    if ((vertexcount = geometryarray.getVertexCount()) > 0) {
		counts = new int[vertexcount/Math.max(groupcount, 1)];
		total = 0;
		length = obj.length();
		for (m = 0, n = obj.offset(); n < length && m < counts.length; n++) {
		    if ((entry = obj.getObject(n)) != null) {
			if (entry.isNumber()) {
			    if ((count = entry.intValue()) >= groupcount) {
				//
				// Should we issue an error or warning if we
				// exceed the vertexcount limit??
				//
				if ((total += count) <= vertexcount)
				    counts[m++] = count;
				else break;
			    } else VM.abort(BADVALUE, NL_STRIPVERTEXCOUNTS, n);
			} else VM.abort(BADVALUE, NL_STRIPVERTEXCOUNTS, n);
		    }
		}
		if (total > 0) {
		    if (m < counts.length) {
			temp = new int[m];
			System.arraycopy(counts, 0, temp, 0, temp.length);
			counts = temp;
		    }
		    ((GeometryStripArray)geometryarray).setStripVertexCounts(counts);
		}
	    }
	}
    }
}

