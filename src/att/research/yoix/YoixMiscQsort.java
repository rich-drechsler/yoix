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

package att.research.yoix;

public abstract
class YoixMiscQsort

    implements YoixConstants,
	       YoixConstantsJTable

{

    //
    // Recently added several variations named sort() that give the
    // caller an easy way to avoid some of the overhead that comes
    // with the qsort() methods (primarily the index array).
    //
    // All low level implementations of qsort now include code that
    // helps when the object that's being sorted includes duplicates
    // and doesn't impose much of a penalty otherwise. There's still
    // room for improvement in our implementation - for example, we
    // probably can and should be more aggressive in equality tests
    // and shifts. We will get back to it later.
    //

    ///////////////////////////////////
    //
    // YoixMiscQsort Methods
    //
    ///////////////////////////////////

    public static int[]
    qsort(YoixInterfaceSortable keys[], int flag) {

	int  indices[];
	int  n;

	if (keys != null) {
	    indices = new int[keys.length];
	    for (n = 0; n < indices.length; n++)
		indices[n] = n;
	    quickSort(keys, indices, 0, keys.length - 1, flag);
	} else indices = null;

	return(indices);
    }


    public static int[]
    qsort(double keys[], int incr) {

	int  indices[];
	int  groups;
	int  n;

	if (keys != null) {
	    indices = new int[keys.length];
	    for (n = 0; n < indices.length; n++)
		indices[n] = n;
	    if (incr > 0 && keys.length > incr) {
		if ((groups = incr*((keys.length - incr)/incr)) > 0)
		    quickSort(keys, indices, 0, groups, incr);
	    }
	} else indices = null;

	return(indices);
    }


    public static int[]
    qsort(int keys[], int incr) {

	int  indices[];
	int  groups;
	int  n;

	if (keys != null) {
	    indices = new int[keys.length];
	    for (n = 0; n < indices.length; n++)
		indices[n] = n;
	    if (incr > 0 && keys.length > incr) {
		if ((groups = incr*((keys.length - incr)/incr)) > 0)
		    quickSort(keys, indices, 0, groups, incr);
	    }
	} else indices = null;

	return(indices);
    }


    public static int[]
    qsort(String keys[], int incr) {

	int  indices[];
	int  n;

	//
	// Currently ignoring incr - guess we just didn't get around
	// to the low level quickSort() implementation.
	//

	if (keys != null) {
	    indices = new int[keys.length];
	    for (n = 0; n < indices.length; n++)
		indices[n] = n;
	    if (incr > 0 && keys.length > incr)
		quickSort(keys, indices, 0, keys.length - 1);
	} else indices = null;

	return(indices);
    }


    public static int[]
    qsort(YoixObject keys, int incr, YoixObject compare) {

	int  indices[];
	int  elements;
	int  groups;
	int  offset;
	int  n;

	if (keys != null) {
	    indices = new int[keys.length()];
	    for (n = 0; n < indices.length; n++)
		indices[n] = n;
	    if (incr > 0 && (elements = keys.sizeof()) > incr) {
		offset = keys.offset();
		if ((groups = incr*((elements - incr)/incr)) > 0) {
		    if (compare != null)
			quickSort(keys, indices, compare, offset, offset + groups, incr);
		    else quickSort(keys, indices, offset, offset + groups, incr);
		}
	    }
	} else indices = null;

	return(indices);
    }


    public static int[]
    qsort(Object data[][], int types[], int columns[], int incr) {

	int  indices[];
	int  groups;
	int  n;

	if (data != null && columns != null) {
	    indices = new int[data.length];
	    for (n = 0; n < indices.length; n++)
		indices[n] = n;
	    if (incr > 0 && data.length > incr) {
		if ((groups = incr*((data.length - incr)/incr)) > 0)
		    quickSort(data, indices, types, columns, 0, groups, incr);
	    }
	} else indices = null;

	return(indices);
    }


    public static void
    sort(YoixInterfaceSortable keys[], int flag) {

	if (keys != null)
	    quickSort(keys, 0, keys.length - 1, flag);
    }


    public static void
    sort(int keys[]) {

	if (keys != null)
	    quickSort(keys, 0, keys.length - 1);
    }


    public static void
    sort(int keys[], int incr) {

	int  groups;

	if (keys != null) {
	    if (incr > 0 && keys.length > incr) {
		if ((groups = incr*((keys.length - incr)/incr)) > 0)
		    quickSort(keys, 0, groups, incr);
	    }
	}
    }


    public static void
    sort(double keys[], int incr) {

	int  groups;

	if (keys != null) {
	    if (incr > 0 && keys.length > incr) {
		if ((groups = incr*((keys.length - incr)/incr)) > 0)
		    quickSort(keys, 0, groups, incr);
	    }
	}
    }


    public static void
    sort(String keys[], int incr) {

	//
	// Currently ignoring incr - guess we just didn't get around
	// to the low level quickSort() implementation.
	//

	if (keys != null) {
	    if (incr > 0 && keys.length > incr)
		quickSort(keys, 0, keys.length - 1);
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static String
    getString(YoixObject keys, int index, String key) {

	YoixObject  obj;

	obj = keys.get(index, false);
	if (obj.isString())
	    key = obj.notNull() ? obj.stringValue() : null;
	else if (obj.isNull())
	    key = null;
	else VM.abort(TYPECHECK, index);

	return(key);
    }


    private static void
    quickSort(YoixInterfaceSortable keys[], int left, int right, int flag) {

	YoixInterfaceSortable  leftkey;
	int                    result;
	int                    pivot;
	int                    top;
	int                    n;

	if (left < right) {
	    swap(keys, left, (left + right)/2);
	    if ((leftkey = keys[left]) != null) {
		pivot = left;
		top = right;
		for (n = left + 1; n <= top; n++) {
		    if (keys[n] != null) {
			if ((result = keys[n].compare(leftkey, flag)) < 0) {
			    if (++pivot != n)
				swap(keys, pivot, n);
			} else if (result == 0 && top > n)
			    swap(keys, top--, n--);
		    }
		}
		swap(keys, left, pivot);
		quickSort(keys, left, pivot - 1, flag);
		if (top < right) {
		    for (top++, pivot++; top < right; top++, pivot++)
			swap(keys, pivot, top);
		    quickSort(keys, pivot, right, flag);
		} else quickSort(keys, pivot + 1, right, flag);
	    } else {
		swap(keys, left, right);
		quickSort(keys, left, right - 1, flag);
	    }
	}
    }


    private static void
    quickSort(YoixInterfaceSortable keys[], int indices[], int left, int right, int flag) {

	YoixInterfaceSortable  leftkey;
	int                    result;
	int                    pivot;
	int                    top;
	int                    n;

	if (left < right) {
	    swap(keys, indices, left, (left + right)/2);
	    if ((leftkey = keys[left]) != null) {
		pivot = left;
		top = right;
		for (n = left + 1; n <= top; n++) {
		    if (keys[n] != null) {
			if ((result = keys[n].compare(leftkey, flag)) < 0) {
			    if (++pivot != n)
				swap(keys, indices, pivot, n);
			} else if (result == 0 && top > n)
			    swap(keys, indices, top--, n--);
		    }
		}
		swap(keys, indices, left, pivot);
		quickSort(keys, indices, left, pivot - 1, flag);
		if (top < right) {
		    for (top++, pivot++; top < right; top++, pivot++)
			swap(keys, indices, pivot, top);
		    quickSort(keys, indices, pivot, right, flag);
		} else quickSort(keys, indices, pivot + 1, right, flag);
	    } else {
		swap(keys, indices, left, right);
		quickSort(keys, indices, left, right - 1, flag);
	    }
	}
    }


    private static void
    quickSort(double keys[], int left, int right, int incr) {

	double  leftkey;
	int     pivot;
	int     top;
	int     n;

	if (left < right) {
	    swap(keys, left, ((left + right)/(2*incr))*incr, incr);
	    leftkey = keys[left];
	    pivot = left;
	    top = right;
	    for (n = left + incr; n <= top; n += incr) {
		if (keys[n] < leftkey) {
		    if ((pivot += incr) != n)
			swap(keys, pivot, n, incr);
		} else if (keys[n] == leftkey && top > n) {
		    swap(keys, top, n, incr);
		    top -= incr;
		    n -= incr;
		}
	    }
	    swap(keys, left, pivot, incr);
	    quickSort(keys, left, pivot - incr, incr);
	    if (top < right) {
		for (top += incr, pivot += incr; top < right; top += incr, pivot += incr)
		    swap(keys, pivot, top, incr);
		quickSort(keys, pivot, right, incr);
	    } else quickSort(keys, pivot + incr, right, incr);
	}
    }


    private static void
    quickSort(double keys[], int indices[], int left, int right, int incr) {

	double  leftkey;
	int     pivot;
	int     top;
	int     n;

	if (left < right) {
	    swap(keys, indices, left, ((left + right)/(2*incr))*incr, incr);
	    leftkey = keys[left];
	    pivot = left;
	    top = right;
	    for (n = left + incr; n <= top; n += incr) {
		if (keys[n] < leftkey) {
		    if ((pivot += incr) != n)
			swap(keys, indices, pivot, n, incr);
		} else if (keys[n] == leftkey && top > n) {
		    swap(keys, indices, top, n, incr);
		    top -= incr;
		    n -= incr;
		}
	    }
	    swap(keys, indices, left, pivot, incr);
	    quickSort(keys, indices, left, pivot - incr, incr);
	    if (top < right) {
		for (top += incr, pivot += incr; top < right; top += incr, pivot += incr)
		    swap(keys, indices, pivot, top, incr);
		quickSort(keys, indices, pivot, right, incr);
	    } else quickSort(keys, indices, pivot + incr, right, incr);
	}
    }


    private static void
    quickSort(int keys[], int left, int right) {

	int  leftkey;
	int  pivot;
	int  top;
	int  n;

	if (left < right) {
	    swap(keys, left, (left + right)/2);
	    leftkey = keys[left];
	    pivot = left;
	    top = right;
	    for (n = left + 1; n <= top; n++) {
		if (keys[n] < leftkey) {
		    if (++pivot != n)
			swap(keys, pivot, n);
		} else if (keys[n] == leftkey && top > n)
		    swap(keys, top--, n--);
	    }
	    swap(keys, left, pivot);
	    quickSort(keys, left, pivot - 1);
	    if (top < right) {
		for (top++, pivot++; top < right; top++, pivot++)
		    swap(keys, pivot, top);
		quickSort(keys, pivot, right);
	    } else quickSort(keys, pivot + 1, right);
	}
    }


    private static void
    quickSort(int keys[], int left, int right, int incr) {

	int  leftkey;
	int  pivot;
	int  top;
	int  n;

	if (left < right) {
	    swap(keys, left, ((left + right)/(2*incr))*incr, incr);
	    leftkey = keys[left];
	    pivot = left;
	    top = right;
	    for (n = left + incr; n <= top; n += incr) {
		if (keys[n] < leftkey) {
		    if ((pivot += incr) != n)
			swap(keys, pivot, n, incr);
		} else if (keys[n] == leftkey && top > n) {
		    swap(keys, top, n, incr);
		    top -= incr;
		    n -= incr;
		}
	    }
	    swap(keys, left, pivot, incr);
	    quickSort(keys, left, pivot - incr, incr);
	    if (top < right) {
		for (top += incr, pivot += incr; top < right; top += incr, pivot += incr)
		    swap(keys, pivot, top, incr);
		quickSort(keys, pivot, right, incr);
	    } else quickSort(keys, pivot + incr, right, incr);
	}
    }


    private static void
    quickSort(int keys[], int indices[], int left, int right, int incr) {

	int  leftkey;
	int  pivot;
	int  top;
	int  n;

	if (left < right) {
	    swap(keys, indices, left, ((left + right)/(2*incr))*incr, incr);
	    leftkey = keys[left];
	    pivot = left;
	    top = right;
	    for (n = left + incr; n <= top; n += incr) {
		if (keys[n] < leftkey) {
		    if ((pivot += incr) != n)
			swap(keys, indices, pivot, n, incr);
		} else if (keys[n] == leftkey && top > n) {
		    swap(keys, indices, top, n, incr);
		    top -= incr;
		    n -= incr;
		}
	    }
	    swap(keys, indices, left, pivot, incr);
	    quickSort(keys, indices, left, pivot - incr, incr);
	    if (top < right) {
		for (top += incr, pivot += incr; top < right; top += incr, pivot += incr)
		    swap(keys, indices, pivot, top, incr);
		quickSort(keys, indices, pivot, right, incr);
	    } else quickSort(keys, indices, pivot + incr, right, incr);
	}
    }


    private static void
    quickSort(String keys[], int left, int right) {

	String  leftkey;
	int     result;
	int     pivot;
	int     top;
	int     n;

	if (left < right) {
	    swap(keys, left, (left + right)/2);
	    if ((leftkey = keys[left]) != null) {
		pivot = left;
		top = right;
		for (n = left + 1; n <= top; n++) {
		    if (keys[n] != null) {
			if ((result = keys[n].compareTo(leftkey)) < 0) {
			    if (++pivot != n)
				swap(keys, pivot, n);
			} else if (result == 0 && top > n)
			    swap(keys, top--, n--);
		    }
		}
		swap(keys, left, pivot);
		quickSort(keys, left, pivot - 1);
		if (top < right) {
		    for (top++, pivot++; top < right; top++, pivot++)
			swap(keys, pivot, top);
		    quickSort(keys, pivot, right);
		} else quickSort(keys, pivot + 1, right);
	    } else {
		swap(keys, left, right);
		quickSort(keys, left, right - 1);
	    }
	}
    }


    private static void
    quickSort(String keys[], int indices[], int left, int right) {

	String  leftkey;
	int     result;
	int     pivot;
	int     top;
	int     n;

	if (left < right) {
	    swap(keys, indices, left, (left + right)/2);
	    if ((leftkey = keys[left]) != null) {
		pivot = left;
		top = right;
		for (n = left + 1; n <= top; n++) {
		    if (keys[n] != null) {
			if ((result = keys[n].compareTo(leftkey)) < 0) {
			    if (++pivot != n)
				swap(keys, indices, pivot, n);
			} else if (result == 0 && top > n)
			    swap(keys, indices, top--, n--);
		    }
		}
		swap(keys, indices, left, pivot);
		quickSort(keys, indices, left, pivot - 1);
		if (top < right) {
		    for (top++, pivot++; top < right; top++, pivot++)
			swap(keys, indices, pivot, top);
		    quickSort(keys, indices, pivot, right);
		} else quickSort(keys, indices, pivot + 1, right);
	    } else {
		swap(keys, indices, left, right);
		quickSort(keys, indices, left, right - 1);
	    }
	}
    }


    private static void
    quickSort(YoixObject keys, int indices[], int left, int right, int incr) {

	String  leftkey;
	String  key;
	int     result;
	int     pivot;
	int     top;
	int     n;

	if (left < right) {
	    swap(keys, indices, left, ((left + right)/(2*incr))*incr, incr);
	    if ((leftkey = getString(keys, left, null)) != null) {
		pivot = left;
		top = right;
		for (n = left + incr; n <= top; n += incr) {
		    if ((key = getString(keys, n, null)) != null) {
		        if ((result = key.compareTo(leftkey)) < 0) {
			    if ((pivot += incr) != n)
				swap(keys, indices, pivot, n, incr);
			} else if (result == 0 && top > n) {
			    swap(keys, indices, top, n, incr);
			    top -= incr;
			    n -= incr;
			}
		    }
		}
		swap(keys, indices, left, pivot, incr);
		quickSort(keys, indices, left, pivot - incr, incr);
		if (top < right) {
		    for (top += incr, pivot += incr; top < right; top += incr, pivot += incr)
			swap(keys, indices, pivot, top, incr);
		    quickSort(keys, indices, pivot, right, incr);
		} else quickSort(keys, indices, pivot + incr, right, incr);
	    } else {
		swap(keys, indices, left, right, incr);
		quickSort(keys, indices, left, right - incr, incr);
	    }
	}
    }


    private static void
    quickSort(YoixObject keys, int indices[], YoixObject compare, int left, int right, int incr) {

	YoixObject  argv[];
	int         result;
	int         pivot;
	int         top;
	int         n;

	if (left < right) {
	    swap(keys, indices, left, ((left + right)/(2*incr))*incr, incr);
	    argv = new YoixObject[] {null, keys.get(left, false)};
	    pivot = left;
	    top = right;
	    for (n = left + incr; n <= top; n += incr) {
		argv[0] = keys.get(n, false);
		if ((result = compare.call(argv, null).intValue()) < 0) {
		    if ((pivot += incr) != n)
			swap(keys, indices, pivot, n, incr);
		} else if (result == 0 && top > n) {
		    swap(keys, indices, top, n, incr);
		    top -= incr;
		    n -= incr;
		}
	    }
	    swap(keys, indices, left, pivot, incr);
	    quickSort(keys, indices, compare, left, pivot - incr, incr);
	    if (top < right) {
		for (top += incr, pivot += incr; top < right; top += incr, pivot += incr)
		    swap(keys, indices, pivot, top, incr);
		quickSort(keys, indices, compare, pivot, right, incr);
	    } else quickSort(keys, indices, compare, pivot + incr, right, incr);
	}
    }


    private static void
    quickSort(Object data[][], int indices[], int types[], int columns[], int left, int right, int incr) {

	int  result;
	int  pivot;
	int  top;
	int  n;

	if (left < right) {
	    swap(indices, left, ((left + right)/(2*incr))*incr, incr);
	    pivot = left;
	    top = right;
	    for (n = left + incr; n <= top; n += incr) {
		if ((result = table_compare(data, indices, n, left, columns, types)) < 0) {
		    if ((pivot += incr) != n)
			swap(indices, pivot, n, incr);
		} else if (result == 0 && top > n) {
		    swap(indices, top, n, incr);
		    top -= incr;
		    n -= incr;
		}
	    }
	    swap(indices, left, pivot, incr);
	    quickSort(data, indices, types, columns, left, pivot - incr, incr);
	    if (top < right) {
		for (top += incr, pivot += incr; top < right; top += incr, pivot += incr)
		    swap(indices, pivot, top, incr);
		quickSort(data, indices, types, columns, pivot, right, incr);
	    } else quickSort(data, indices, types, columns, pivot + incr, right, incr);
	}
    }


    private static void
    swap(double keys[], int n, int m, int incr) {

	double  key;

	for (; incr > 0; incr--, m++, n++) {
	    key = keys[n];
	    keys[n] = keys[m];
	    keys[m] = key;
	}
    }


    private static void
    swap(double keys[], int indices[], int n, int m, int incr) {

	double  key;
	int     position;

	for (; incr > 0; incr--, m++, n++) {
	    key = keys[n];
	    position = indices[n];
	    keys[n] = keys[m];
	    indices[n] = indices[m];
	    keys[m] = key;
	    indices[m] = position;
	}
    }


    private static void
    swap(int keys[], int n, int m) {

	int  key;

	key = keys[n];
	keys[n] = keys[m];
	keys[m] = key;
    }


    private static void
    swap(int keys[], int n, int m, int incr) {

	int  key;

	for (; incr > 0; incr--, m++, n++) {
	    key = keys[n];
	    keys[n] = keys[m];
	    keys[m] = key;
	}
    }


    private static void
    swap(int keys[], int indices[], int n, int m, int incr) {

	int  key;
	int  position;

	for (; incr > 0; incr--, m++, n++) {
	    key = keys[n];
	    position = indices[n];
	    keys[n] = keys[m];
	    indices[n] = indices[m];
	    keys[m] = key;
	    indices[m] = position;
	}
    }


    private static void
    swap(Object keys[], int n, int m) {

	Object  key;

	key = keys[n];
	keys[n] = keys[m];
	keys[m] = key;
    }


    private static void
    swap(Object keys[], int n, int m, int incr) {

	Object  key;

	for (; incr > 0; incr--, m++, n++) {
	    key = keys[n];
	    keys[n] = keys[m];
	    keys[m] = key;
	}
    }


    private static void
    swap(Object keys[], int indices[], int n, int m) {

	Object  key;
	int     position;

	key = keys[n];
	position = indices[n];
	keys[n] = keys[m];
	indices[n] = indices[m];
	keys[m] = key;
	indices[m] = position;
    }


    private static void
    swap(Object keys[], int indices[], int n, int m, int incr) {

	Object  key;
	int     position;

	for (; incr > 0; incr--, m++, n++) {
	    key = keys[n];
	    position = indices[n];
	    keys[n] = keys[m];
	    indices[n] = indices[m];
	    keys[m] = key;
	    indices[m] = position;
	}
    }


    private static void
    swap(YoixObject keys, int indices[], int n, int m, int incr) {

	YoixObject  key;
	int         position;

	for (; incr > 0; incr--, m++, n++) {
	    key = keys.get(n, false);
	    position = indices[n];
	    keys.put(n, keys.get(m, false), false);
	    indices[n] = indices[m];
	    keys.put(m, key, false);
	    indices[m] = position;
	}
    }


    private static int
    table_compare(Object data[][], int indices[], int idx1, int idx2, int columns[], int types[]) {

	boolean  ascending;
	Object   obj1;
	Object   obj2;
	double   d1;
	double   d2;
	int      result = 0;
	int      idx;
	int      col;

	for (idx = 0; idx < columns.length; idx++) {
	    // columns start at 1 instead of zero so that
	    // sign can indicate sort order, so need to subtract
	    // 1 (as well as changed negative to positive value)
	    if ((col = columns[idx]) < 0) {
		ascending = false;
		col = -1 - col;
	    } else if (col > 0) {
		ascending = true;
		col--;
	    } else continue; // 0 is not a valid value

	    if (types != null && col < types.length) {
		obj1 = YoixSwingJTable.pickSortObject(data[indices[idx1]][col]);
		obj2 = YoixSwingJTable.pickSortObject(data[indices[idx2]][col]);

		if (obj1 == null && obj2 == null) {
		    result = 0;
		} else if (obj1 == null) {
		    return(ascending ? -1 : 1);
		} else if (obj2 == null) {
		    return(ascending ? 1 : -1);
		} else {
		    switch (types[col]) {
			case YOIX_BOOLEAN_TYPE:
			    {
				boolean  b1 = ((Boolean)obj1).booleanValue();
				boolean  b2 = ((Boolean)obj2).booleanValue();
				result = (b1 == b2) ? 0 : b1 ? 1 : -1;
			    }
			    break;

			case YOIX_DATE_TYPE:
			    {
				long  t1 = ((java.util.Date)obj1).getTime();
				long  t2 = ((java.util.Date)obj2).getTime();
				result = (t1 < t2) ? -1 : (t1 > t2) ? 1 : 0;
			    }
			    break;

			case YOIX_DOUBLE_TYPE:
			    if (((Double)obj1).isNaN()) {
				if (((Double)obj2).isNaN()) {
				    result = 0;
				} else result = -1;
			    } else if (((Double)obj2).isNaN()) {
				result = 1;
			    } else {
				d1 = ((Double)(obj1)).doubleValue();
				d2 = ((Double)(obj2)).doubleValue();
				result = (d1 < d2) ? -1 : (d1 > d2) ? 1 : 0;
			    }
			    break;

			case YOIX_INTEGER_TYPE:
			    {
				int  n1 = ((Integer)(obj1)).intValue();
				int  n2 = ((Integer)(obj2)).intValue();
				result = (n1 < n2) ? -1 : (n1 > n2) ? 1 : 0;
			    }
			    break;

			case YOIX_HISTOGRAM_TYPE:
			case YOIX_MONEY_TYPE:
			case YOIX_PERCENT_TYPE:
			case YOIX_TIMER_TYPE:
			    if (((YoixSwingJTable.NaN)obj1).isNaN()) {
				if (((YoixSwingJTable.NaN)obj2).isNaN()) {
				    result = 0;
				} else result = -1;
			    } else if (((YoixSwingJTable.NaN)obj2).isNaN()) {
				result = 1;
			    } else {
				d1 = ((Number)(obj1)).doubleValue();
				d2 = ((Number)(obj2)).doubleValue();
				result = (d1 < d2) ? -1 : (d1 > d2) ? 1 : 0;
			    }
			    break;

			case YOIX_OBJECT_TYPE:
			    if (obj1 instanceof Number && obj2 instanceof Number) {
				d1 = ((Number)(obj1)).doubleValue();
				d2 = ((Number)(obj2)).doubleValue();
				result = (d1 < d2) ? -1 : (d1 > d2) ? 1 : 0;
			    } else if (obj1 instanceof String && obj2 instanceof String)
				result = ((String)obj1).compareTo((String)obj2);
			    else result = obj1.toString().compareTo(obj2.toString());
			    break;

			case YOIX_STRING_TYPE:
			    result = ((String)obj1).compareTo((String)obj2);
			    break;

			case YOIX_TEXT_TYPE:
			    result = ((YoixSwingJTable.YoixJTableText)obj1).toSortString().compareTo(((YoixSwingJTable.YoixJTableText)obj2).toSortString());
			    break;

			default:
			    result = obj1.toString().compareTo(obj2.toString());
			    break;
		    }
		    if (result != 0)
			return(ascending ? result : -result);
		}
	    }
	}

	return(result);
    }
}

