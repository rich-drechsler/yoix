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

package att.research.yoix.ydat;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import att.research.yoix.*;

class GraphRecord

    implements Constants,
	       YoixInterfaceSortable

{

    //
    // There can be at most one active element and it's the element that's
    // associated with the active field in the GraphPlot that's passed to
    // the constructor as the owner argument. It's typically a node, but
    // that's not a requirement. Likewise, passive elements, if there are
    // any, are often edges, but once again there's nothing that will stop
    // you from adding nodes to the list of passive elements.
    // 
    // NOTE - we omitted synchronization because this class is currently
    // only used to create temporary data structures that are used by a
    // single thread. If the assumption changes this class will need some
    // work.
    //

    DataRecord  record;
    Font        font;
    int         index;		// currently unused

    Rectangle2D  active_labelbounds;
    Object       primary_key;
    Object       secondary_key;
    Object       primary_sortkey;
    Object       secondary_sortkey;
    String       active_element;
    String       active_name;
    String       active_label;
    String       passive_elements[];
    String       passive_names[];
    String       tooltips[];
    int          layoutsort[];

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    GraphRecord(SwingJGraphPlot owner, DataRecord record, int index) {

	//
	// At this point it's safe to assume that there's exactly one
	// active field that we don't care about here.
	//

	this.record = record;
	this.font = owner.getDefaultFont(true);
	this.layoutsort = owner.getGraphLayoutSort();
	this.index = index;
	buildGraphRecord(owner, record);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceSortable Methods
    //
    ///////////////////////////////////

    public final int
    compare(YoixInterfaceSortable element, int flag) {

	GraphRecord  data;
	double       val;

	//
	// We aren't really doing anything with flag right now, but that
	// probably will change.
	//

	data = (GraphRecord)element;

	switch (flag) {
	    case 0:
		if ((val = compareSortKeys(primary_key, data.primary_key, layoutsort[0])) == 0)
		    if ((val = compareSortKeys(primary_sortkey, data.primary_sortkey, layoutsort[0])) == 0)
			if ((val = compareSortKeys(secondary_key, data.secondary_key, layoutsort[1])) == 0)
			    val = compareSortKeys(secondary_sortkey, data.secondary_sortkey, layoutsort[1]);
		break;

	    default:
		val = 0;
		break;
	}
	return(val > 0 ? 1 : (val < 0 ? -1 : 0));
    }

    ///////////////////////////////////
    //
    // GraphRecord Methods
    //
    ///////////////////////////////////

    final String
    getActiveElement() {

	return(active_element);
    }


    final String
    getActiveLabel() {

	return(active_label);
    }


    final Rectangle2D
    getActiveLabelBounds(Rectangle2D bounds) {

	return(active_labelbounds != null ? active_labelbounds : bounds);
    }


    final String
    getActiveName() {

	return(active_name);
    }


    final double
    getDistance(GraphRecord neighbor) {

	return(getDistance(neighbor, Double.POSITIVE_INFINITY, -1));
    }


    final double
    getDistance(GraphRecord neighbor, double undefined) {

	return(getDistance(neighbor, undefined, -1));
    }


    final double
    getDistance(GraphRecord neighbor, double undefined, double fail) {

	double  distance;

	//
	// Usually returns a positive number that the caller can interpret
	// as the "distance" this record and neighbor, but the return will
	// be undefined (e.g., Double.POSITIVE_INFINITY) if the sort key
	// that didn't match is a string or if the primary sort key didn't
	// match when a secondary key is defined. If neighbor is null then
	// fail (usually -1) is returned.
	//

	if (neighbor != this && neighbor != null) {
	    if (secondary_key != null && neighbor.secondary_key != null) {
		if ((distance = compareSortKeys(primary_key, neighbor.primary_key, layoutsort[0])) == 0) {
		    if ((distance = compareSortKeys(secondary_key, neighbor.secondary_key, layoutsort[1])) != 0) {
			if (secondary_key instanceof String || neighbor.secondary_key instanceof String)
			    distance = undefined;
			else if (distance < 0)
			    distance = -distance;
		    }
		} else distance = undefined;
	    } else if ((distance = compareSortKeys(primary_key, neighbor.primary_key, layoutsort[0])) != 0) {
		if (primary_key instanceof String || neighbor.primary_key instanceof String)
		    distance = undefined;
		else if (distance < 0)
		    distance = -distance;
	    }
	} else distance = (neighbor == this) ? 0 : fail;

	return(distance);
    }

    final Object[]
    getDistancePair(GraphRecord neighbor) {

	return(getDistancePair(neighbor, Double.POSITIVE_INFINITY, -1));
    }


    final Object[]
    getDistancePair(GraphRecord neighbor, double undefined) {

	return(getDistancePair(neighbor, undefined, -1));
    }


    final Object[]
    getDistancePair(GraphRecord neighbor, double undefined, double fail) {

	double  distance;
	String  distancetip = null;

	//
	// Usually returns a positive number that the caller can interpret
	// as the "distance" this record and neighbor, but the return will
	// be undefined (e.g., Double.POSITIVE_INFINITY) if the sort key
	// that didn't match is a string or if the primary sort key didn't
	// match when a secondary key is defined. If neighbor is null then
	// fail (usually -1) is returned.
	//

	if (neighbor != this && neighbor != null) {
	    if (secondary_key != null && neighbor.secondary_key != null) {
		if ((distance = compareSortKeys(primary_key, neighbor.primary_key, layoutsort[0])) == 0) {
		    if ((distance = compareSortKeys(secondary_key, neighbor.secondary_key, layoutsort[1])) != 0) {
			if (secondary_key instanceof String || neighbor.secondary_key instanceof String)
			    distance = undefined;
			else if (distance < 0)
			    distance = -distance;
		    }
		    if (distance != undefined) {
			if (layoutsort[1] == VL_SORT_TIME)
			    distancetip = YoixMiscTime.timerFormat(TIMER_FORMAT, distance);
			else distancetip = "" + distance;
		    }
		} else distance = undefined;
	    } else if ((distance = compareSortKeys(primary_key, neighbor.primary_key, layoutsort[0])) != 0) {
		if (primary_key instanceof String || neighbor.primary_key instanceof String)
		    distance = undefined;
		else if (distance < 0)
		    distance = -distance;
		if (distance != undefined) {
		    if (layoutsort[0] == VL_SORT_TIME)
			distancetip = YoixMiscTime.timerFormat(TIMER_FORMAT, distance);
		    else distancetip = "" + distance;
		}
	    } else if (distance != undefined) {
		if (layoutsort[0] == VL_SORT_TIME)
		    distancetip = YoixMiscTime.timerFormat(TIMER_FORMAT, distance);
		else distancetip = "" + distance;
	    }
	} else {
	    distance = ((neighbor == this) ? 0 : fail);
	    distancetip = "" + distance;
	}

	return(new Object[] {new Double(distance), distancetip});
    }


    final Rectangle2D
    getLabelBounds(String text) {

	Rectangle2D  bounds;
	TextLayout   layout;

	if (text != null) {
	    layout = new TextLayout(text, font, FONTCONTEXT);
	    bounds = layout.getBounds();
	} else bounds = null;
	return(bounds);
    }


    final int
    getPassiveCount() {

	return(passive_elements != null ? passive_elements.length : 0);
    }


    final String
    getPassiveElement(int n) {

	return(passive_elements != null && n >= 0 && n < passive_elements.length ? passive_elements[n] : null);
    }


    final String
    getPassiveName(int n) {

	return(passive_names != null && n >= 0 && n < passive_names.length ? passive_names[n] : null);
    }


    final Object
    getPrimaryKey() {

	return(primary_key);
    }


    final String
    getPrimaryTooltip() {

	return(getTooltip(0));
    }


    final DataRecord
    getRecord() {

	return(record);
    }


    final Color
    getRecordColor() {

	return(record.getColor());
    }


    final String
    getRecordHexColor() {

	String  text;
	Color   color;

	if ((color = record.getColor()) != null)
	    text = Integer.toHexString(color.getRGB()).substring(2);
	else text = null;
	return(text);
    }


    final Object
    getSecondaryKey() {

	return(secondary_key);
    }


    final String
    getSecondaryTooltip() {

	return(getTooltip(1));
    }


    final Object[]
    getSortKeys() {

	return(new Object[] {primary_sortkey, secondary_sortkey});
    }


    final String
    getTooltip(int index) {

	String  tip = null;

	if (tooltips != null) {
	    if (index >= 0 && index < tooltips.length) {
		if (tooltips[index] != null)
		    tip = tooltips[index];
	    }
	}
	return(tip);
    }


    final void
    saveActiveElement(String name, StringBuffer sbuf) {

	active_name = name;
	active_element = sbuf.toString();
    }


    final void
    savePassiveElement(String name, StringBuffer sbuf) {

	String  tmp[];
	int     length;

	if (name != null && sbuf != null) {
	    if (passive_elements != null && (length = passive_elements.length) > 0) {
		tmp = new String[length + 1];
		System.arraycopy(passive_names, 0, tmp, 0, length);
		tmp[length] = name;
		passive_names = tmp;
		tmp = new String[length + 1];
		System.arraycopy(passive_elements, 0, tmp, 0, length);
		tmp[length] = sbuf.toString();
		passive_elements = tmp;
	    } else {
		passive_names = new String[] {name};
		passive_elements = new String[] {sbuf.toString()};
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildGraphRecord(SwingJGraphPlot owner, DataRecord record) {

	double  value;
	int     indices[];
	int     m;
	int     n;

	if ((indices = owner.getFieldIndices()) != null) {
	    //
	    // For now we require that indices have room for at least 4
	    // elements and the first one is for the active field, which
	    // we're not one we're interested in here. There are at least
	    // seven elements in indices. The seventh element and any others
	    // are used as the node's tooltip.
	    //
	    if (indices.length >= 7) {
		if (indices[1] >= 0) {
		    value = record.getValue(indices[1]);
		    if (Double.isNaN(value) == false) {
			if (value == (int)value)
			    primary_key = new Integer((int)value);
			else primary_key = new Double(value);
		    } else primary_key = owner.getRecordName(record, 1);
		} else primary_key = null;

		if (indices[2] >= 0) {
		    value = record.getValue(indices[2]);
		    if (Double.isNaN(value) == false) {
			if (value == (int)value)
			    secondary_key = new Integer((int)value);
			else secondary_key = new Double(value);
		    } else secondary_key = owner.getRecordName(record, 2);
		} else secondary_key = null;

		if (indices[3] >= 0) {
		    active_label = owner.getRecordName(record, 3);
		    active_labelbounds = getLabelBounds(active_label);
		}

		if (indices[4] >= 0) {
		    value = record.getValue(indices[4]);
		    if (Double.isNaN(value) == false) {
			if (value == (int)value)
			    primary_sortkey = new Integer((int)value);
			else primary_sortkey = new Double(value);
		    } else primary_sortkey = owner.getRecordName(record, 4);
		} else primary_sortkey = null;

		if (indices[5] >= 0) {
		    value = record.getValue(indices[5]);
		    if (Double.isNaN(value) == false) {
			if (value == (int)value)
			    secondary_sortkey = new Integer((int)value);
			else secondary_sortkey = new Double(value);
		    } else secondary_sortkey = owner.getRecordName(record, 5);
		} else secondary_sortkey = null;

		//
		// The indices array currently always has 7 or more elements,
		// but there's a small chance we'll drop the 7th and remaining
		// elements and just use the node label as the tooltip.
		//

		tooltips = new String[] {null, null};
		for (n = 0, m = 6; n < tooltips.length; m++, n++) {
		    if (m < indices.length) {
			if (indices[m] >= 0)
			    tooltips[n] = owner.getRecordName(record, m);
			else tooltips[n] = null;
		    } else if (n > 0)
			tooltips[n] = tooltips[n-1];
		}
	    }
	}
    }


    private double
    compareSortKeys(Object key1, Object key2, int order) {

	double  value1;
	double  value2;
	double  value;

	if (key1 != key2) {
	    if (key1 instanceof Number || key2 instanceof Number) {
		if (key1 instanceof Number && key2 instanceof Number)
		    value = ((Number)key1).doubleValue() - ((Number)key2).doubleValue();
		else if (key1 instanceof Number)
		    value = 1;
		else value = -1;
	    } else if (key1 instanceof String || key2 instanceof String) {
		if (key1 instanceof String && key2 instanceof String) {
		    switch (order) {
			case VL_SORT_OCTET:
			    value = Misc.compareOctets((String)key1, (String)key2);
			    break;

			case VL_SORT_NUMERIC:
			    value = YoixMake.javaDouble((String)key1, Double.NaN) - YoixMake.javaDouble((String)key2, Double.NaN);
			    if (Double.isNaN(value)) {
				if (Double.isNaN(YoixMake.javaDouble((String)key1, Double.NaN))) {
				    if (Double.isNaN(YoixMake.javaDouble((String)key2, Double.NaN)))
					value = 0;
				    else value = 1;
				} else value = -1;
			    }
			    break;

			default:
			    value = ((String)key1).compareTo((String)key2);
			    break;
		    }
		} else if (key1 instanceof String)
		    value = 1;
		else value = -1;
	    } else value = 0;
	} else value = 0;

	return(value);
    }
}

