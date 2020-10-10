//
// Simple bounding box support.
//

package att.research.yoix.ydat;

class BoundingBox

{

    double   ulx;
    double   uly;
    double   lrx;
    double   lry;
    double   totalx;
    double   totaly;
    int      count = 0;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BoundingBox() {

    }


    BoundingBox(BoundingBox bbox) {

	ulx = bbox.ulx;
	uly = bbox.uly;
	lrx = bbox.lrx;
	lry = bbox.lry;
	totalx = bbox.totalx;
	totaly = bbox.totaly;
	count = bbox.count;
    }

    ///////////////////////////////////
    //
    // BoundingBox Methods
    //
    ///////////////////////////////////

    final void
    add(double x, double y) {

	cover(x, y);
	count++;
	totalx += x;
	totaly += y;
    }


    final boolean
    contains(double x, double y) {

	return(contains(x, y, 0));
    }


    final boolean
    contains(double x, double y, int flags) {

	boolean  result;

	if (count > 0) {
	    switch (flags&0x3) {
		case 1:
		    result = (ulx < x && lrx >= x && uly < y && lry >= y);
		    break;

		case 2:
		    result = (ulx <= x && lrx > x && uly <= y && lry > y);
		    break;

		case 3:
		    result = (ulx < x && lrx > x && uly < y && lry > y);
		    break;

		default:
		    result = (ulx <= x && lrx >= x && uly <= y && lry >= y);
		    break;
	    }
	} else result = false;

	return(result);
    }


    final boolean
    containsX(double x) {

	return(containsX(x, 0));
    }


    final boolean
    containsX(double x, int flags) {

	boolean  result;

	if (count > 0) {
	    switch (flags&0x3) {
		case 1:
		    result = (ulx < x && lrx >= x);
		    break;

		case 2:
		    result = (ulx <= x && lrx > x);
		    break;

		case 3:
		    result = (ulx < x && lrx > x);
		    break;

		default:
		    result = (ulx <= x && lrx >= x);
		    break;
	    }
	} else result = false;

	return(result);
    }


    final boolean
    containsY(double y) {

	return(containsY(y, 0));
    }


    final boolean
    containsY(double y, int flags) {

	boolean  result;

	if (count > 0) {
	    switch (flags&0x3) {
		case 1:
		    result = (uly < y && lry >= y);
		    break;

		case 2:
		    result = (uly <= y && lry > y);
		    break;

		case 3:
		    result = (uly < y && lry > y);
		    break;

		default:
		    result = (uly <= y && lry >= y);
		    break;
	    }
	} else result = false;

	return(result);
    }


    final boolean
    covers(BoundingBox bbox) {

	boolean  result;

	if (bbox != null && bbox.count > 0)
	    result = covers(bbox.ulx, bbox.uly) && covers(bbox.lrx, bbox.lry);
	else result = true;
	return(result);
    }


    final boolean
    covers(double x, double y) {

	return(count > 0 && ulx <= x && lrx >= x && uly <= y && lry >= y);
    }


    public final boolean
    equals(Object obj) {

	BoundingBox  bbox;
	boolean      result;

	if (obj instanceof BoundingBox) {
	    bbox = (BoundingBox)obj;
	    if (count > 0 && bbox.count > 0)
		result = bbox.ulx == ulx && bbox.uly == uly && bbox.lrx == lrx && bbox.lry == lry;
	    else result = bbox.count == count;
	} else result = false;

	return(result);
    }


    final void
    reset() {

	count = 0;
	totalx = 0;
	totaly = 0;
    }


    final void
    reset(double x, double y) {

	reset();
	add(x, y);
    }


    final void
    reset(double x0, double y0, double x1, double y1) {

	reset();
	add(x0, y0);
	add(x1, y1);
    }


    public final String
    toString() {

	return(
	    "bbox[" + count + "]: (" +
	    ulx + ", " + uly +
	    ") (" +
	    lrx + ", " + lry +
	    ") (" +
	    totalx + ", " + totaly + ")"
	);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    cover(double x, double y) {

	if (count > 0) {
	    if (x < ulx)
		ulx = x;
	    else if (x > lrx)
		lrx = x;
	    if (y < uly)
		uly = y;
	    else if (y > lry)
		lry = y;
	} else {
	    ulx = lrx = x;
	    uly = lry = y;
	}
    }
}

