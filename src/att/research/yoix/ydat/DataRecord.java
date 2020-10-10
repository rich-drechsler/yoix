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
import java.awt.Color;
import att.research.yoix.*;

class DataRecord

    implements YoixConstants,
	       YoixInterfaceSortable

{

    //
    // The code that expands masks beyond a single long is new and not
    // thoroughly tested. It seems to work properly, but be suspicious
    // if you notice problems. First place to check is the comments in
    // isSelected() and notSelected(). Changes were made around 7/18/05.
    //

    private Object  data[];
    private int     id;
    private int     index;

    private boolean  required = false;
    private Color    color = null;
    private int      selected[] = {SELECT_MASK};

    //
    // Low order bits in the masks that getMasks() returns to DataManager
    // are used for control purposes. The bottom INDEX_BITS are used when
    // we need to located the appropriate element in selected[]. The next
    // bit is a magic flag that controls isSelected() and notSelected()
    // when they're handed a masks[] array with more than one element.
    // Increasing INDEX_BITS means more mask bits will be available. The
    // current setting allows 224 masks, which should be plenty!!
    //
    // We decided that calculating, building, and storing mask info based
    // on a DataManager request was too expensive, at least right now, so
    // that's why we're using the hardcoded values. Shouldn't be too hard
    // to change, if you decide flexibilty is important.
    //

    private static final int  INDEX_BITS = 3;

    private static final int  INDEX_MASK = (1<<INDEX_BITS) - 1;
    private static final int  MODEL_MASK = (1<<INDEX_BITS);
    private static final int  CONTROL_MASK = MODEL_MASK|INDEX_MASK;
    private static final int  SELECT_MASK = ~(MODEL_MASK|INDEX_MASK);

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    DataRecord() {

	this(null, 0);
    }


    DataRecord(Object data[], int id) {

	this.data = data;
	this.id = id;
	this.index = id;
    }

    ///////////////////////////////////
    //
    // YoixInterfaceSortable Methods
    //
    ///////////////////////////////////

    public final int
    compare(YoixInterfaceSortable element, int flag) {

	return((id > ((DataRecord)element).id) ? 1 : -1);
    } 

    ///////////////////////////////////
    //
    // DataRecord Methods
    //
    ///////////////////////////////////

    final Object
    changeField(int field, Object value) {

	Object  ovalue;

	if (field >= 0 && field < data.length) {
	    if (data[field] != null) {
		if (!(data[field] instanceof DataPartition)) {
		    ovalue = data[field];
		    data[field] = value;
		} else ovalue = ((DataPartition)data[field]).changeField(value);
	    } else ovalue = null;
	} else ovalue = null;

	return(ovalue);
    }


    final void
    clearSelected(int mask) {

	int  index = mask&INDEX_MASK;

	if (index >= selected.length)
	    growTo(index);
	selected[index] &= ~mask|CONTROL_MASK;
    }


    final void
    clearSelected(int masks[]) {

	int  n;

	if (masks != null) {
	    for (n = 0; n < masks.length; n++)
		clearSelected(masks[n]);
	}
    }


    final Color
    getColor() {

	return(color);
    }


    final String
    getField(int field) {

	String  value = null;

	if (field >= 0 && field < data.length) {
	    if (!(data[field] instanceof String)) {
		if (data[field] instanceof Object[]) {
		    DataGenerator.generateData((Object[])data[field]);
		    if (data[field] instanceof String)
			value = (String)data[field];
		    else if (data[field] instanceof Number)
			value = data[field].toString();
		    else if (data[field] instanceof DataPartition)
			value = ((DataPartition)data[field]).getField();
		} else if (data[field] instanceof Number)
		    value = data[field].toString();
		else if (data[field] instanceof DataPartition)
		    value = ((DataPartition)data[field]).getField();
	    } else value = (String)data[field];
	}

	return(value);
    }


    final String
    getField(int field, int partition) {

	String  value = null;

	if (partition >= 0) {
	    if (field >= 0 && field < data.length) {
		if (data[field] instanceof Object[])
		    DataGenerator.generateData((Object[])data[field]);
		if (data[field] instanceof DataPartition)
		    value = ((DataPartition)data[field]).getField(partition);
	    }
	} else value = getField(field);

	return(value);
    }


    final YoixObject
    getFields() {

	YoixObject  fields;
	String      field;
	double      value;
	int         length;
	int         n;

	length = getLength();
	fields = YoixObject.newArray(length);

	for (n = 0; n < length; n++) {
	    if ((field = getField(n)) == null) {
		value = getValue(n);
		if (Double.isNaN(value))
		    fields.put(n, YoixObject.newNull(), false);
		else fields.put(n, YoixObject.newDouble(value), false);
	    } else fields.put(n, YoixObject.newString(field), false);
	}

	return(fields);
    }


    final int
    getID() {

	return(id);
    }


    final int
    getIndex() {

	return(index);
    }


    final String
    getKey(int field) {

	//
	// Exactly duplicates some old code, but there's a chance we
	// can use field[n] - invesitgate later.
	//

	return(id + ":" + field);
    }


    final int
    getLength() {

	return(data != null ? data.length : 0);
    }


    static int[]
    getMasks() {

	int  masks[];
	int  mask;
	int  start;
	int  count;
	int  index;
	int  n;

	count = (1 << INDEX_BITS);
	masks = new int[count*(32 - (INDEX_BITS + 1))];

	for (n = 0, index = 0; n < count; n++) {
	    for (mask = 1 << (INDEX_BITS + 1); mask != 0; mask <<= 1)
		masks[index++] = mask | n;
	}
	return(masks);
    }


    final int
    getPartition(int field) {

	int  partition;

	if (field >= 0 && field < data.length) {
	    if (data[field] instanceof DataPartition)
		partition = ((DataPartition)data[field]).id;
	    else partition = -1;
	} else partition = -1;

	return(partition);
    }


    static int[]
    getSelectMasks(int masks[], boolean andmodel) {

	int  selectmasks[];
	int  length;
	int  n;

	if (masks != null) {
	    if ((length = masks.length) > 0) {
		selectmasks = new int[length];
		for (n = 0; n < length; n++) {
		    selectmasks[n] = masks[n] & (SELECT_MASK|INDEX_MASK);
		    if (andmodel)
			selectmasks[n] |= MODEL_MASK;	// marks it as AND model
		}
	    } else selectmasks = null;
	} else selectmasks = null;
	return(selectmasks);
    }


    static int[]
    getSelectMasks(int xmask, int ymask) {

	int  selectmasks[];
	int  masks[];

	if (xmask != 0 || ymask != 0) {
	    if (xmask != 0 && ymask != 0)
		masks = new int[] {xmask, ymask};
	    else if (xmask != 0)
		masks = new int[] {xmask};
	    else masks = new int[] {ymask};
	    selectmasks = getSelectMasks(masks, true);
	} else selectmasks = null;
	return(selectmasks);
    }


    final double
    getValue(int field) {

	double  value = Double.NaN;

	if (field >= 0 && field < data.length) {
	    if (!(data[field] instanceof Number)) {
		if (data[field] instanceof Object[]) {
		    DataGenerator.generateData((Object[])data[field]);
		    if (data[field] instanceof Number)
			value = ((Number)data[field]).doubleValue();
		    else if (data[field] instanceof DataPartition)
			value = ((DataPartition)data[field]).getValue();
		} else if (data[field] instanceof DataPartition)
		    value = ((DataPartition)data[field]).getValue();
	    } else value = ((Number)data[field]).doubleValue();
	}

	return(value);
    }


    final double
    getValue(int field, int partition) {

	double  value = Double.NaN;

	if (partition >= 0) {
	    if (field >= 0 && field < data.length) {
		if (data[field] instanceof Object[])
		    DataGenerator.generateData((Object[])data[field]);
		if (data[field] instanceof DataPartition)
		    value = ((DataPartition)data[field]).getValue(partition);
	    }
	} else value = getValue(field);

	return(value);
    }


    final boolean
    isRequired() {

	return(required);
    }


    final boolean
    isSelected() {

	int  n;

	for (n = 0; n < selected.length; n++) {
	    if ((selected[n]&SELECT_MASK) != SELECT_MASK)
		return(false);
	}
	return(true);
    }


    final boolean
    isSelected(int mask) {

	int  index = mask&INDEX_MASK;

	//
	// The original version called growTo() when index was too big, but
	// it seems like we should be able to assume the mask bit is set if
	// the entry hasn't been allocated yet. May be a very small chance
	// threads could cause problems, but we suspect not because masks
	// now are single select bits that belong to a component that gets
	// here from a synchronized method. There are lots of things to try,
	// including calling growTo(), if we stumble into into problems.
	//

	return((mask &= SELECT_MASK) != 0 && ((index >= selected.length) || (selected[index]&mask) == mask));
    }


    final boolean
    isSelected(int masks[]) {

	boolean  result;
	int      length;
	int      n;

	if (masks != null && (length = masks.length) > 0) {
	    if (length > 1) {
		result = ((masks[0]&MODEL_MASK) != 0);
		for (n = 0; n < length; n++) {
		    if (isSelected(masks[n]) != result) {
			result = !result;
			break;
		    }
		}
	    } else result = isSelected(masks[0]);
	} else result = false;
	return(result);
    }


    final boolean
    notSelected() {

	int  n;

	for (n = 0; n < selected.length; n++) {
	    if ((selected[n]&SELECT_MASK) != SELECT_MASK)
		return(true);
	}
	return(false);
    }


    final boolean
    notSelected(int mask) {

	int  index = mask&INDEX_MASK;

	//
	// The original version called growTo() when index was too big, but
	// it seems like we should be able to assume the mask bit is set if
	// the entry hasn't been allocated yet. May be a very small chance
	// threads could cause problems, but we suspect not because masks
	// now are single select bits that belong to a component that gets
	// here from a synchronized method. There are lots of things to try,
	// including calling growTo(), if we stumble into into problems.
	//

	return((mask &= SELECT_MASK) == 0 || ((index < selected.length) && (selected[index]&mask) != mask));
    }


    final boolean
    notSelected(int masks[]) {

	boolean  result;
	int      length;
	int      n;

	if (masks != null && (length = masks.length) > 0) {
	    if (length > 1) {
		result = ((masks[0]&MODEL_MASK) == 0);
		for (n = 0; n < length; n++) {
		    if (notSelected(masks[n]) != result) {
			result = !result;
			break;
		    }
		}
	    } else result = notSelected(masks[0]);
	} else result = true;
	return(result);
    }


    final void
    setColor(Color color) {

	this.color = color;
    }


    final void
    setIndex(int index) {

	this.index = index;
    }


    final void
    setRequired(boolean state) {

	required = state;
    }


    final void
    setSelected(int mask) {

	int  index = mask&INDEX_MASK;

	if (index >= selected.length)
	    growTo(index);
	selected[index] |= (mask&SELECT_MASK);
    }


    final void
    setSelected(int masks[]) {

	int  n;

	if (masks != null) {
	    for (n = 0; n < masks.length; n++)
		setSelected(masks[n]);
	}
    }


    public String
    toString() {

	String  str;
	int     n;

	str = "<" + id + ":" + index + ":" + selected[0] + "> ";

	for (n = 0; n < data.length; n++) {
	    if (n == 0)
		str += data[n];
	    else str += "|" + data[n];
	}
	return(str);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private synchronized void
    growTo(int index) {

	int  temp[];
	int  length;
	int  n;

	if ((length = selected.length) <= index) {
	    temp = new int[index + 1];
	    System.arraycopy(selected, 0, temp, 0, length);
	    for (n = length; n <= index; n++)
		temp[n] = SELECT_MASK;
	    selected = temp;
	}
    }
}

