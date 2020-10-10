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

class DataPartition

    implements YoixConstants

{

    //
    // A simple support class closely linked to a DataRecord, but using
    // an inner class is not the best solution.
    //
    // Recently (4/24/05) made some changes that mean translators should
    // now work with partitions.
    //

    DataGenerator  generator;
    Object         data;
    int            id;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    DataPartition(Object data, int id, DataGenerator generator) {

	this.data = data;
	this.id = id;
	this.generator = generator;
    }

    ///////////////////////////////////
    //
    // DataPartition Methods
    //
    ///////////////////////////////////

    final Object
    changeField(Object value) {

	Object  ovalue = data;

	data = value;
	return(ovalue);
    }


    final String
    getField() {

	String  value;

	if (data instanceof String) {
	    if (generator != null)
		translate();
	    value = (String)data;
	} else if (data instanceof Number)
	    value = data.toString();
	else value = null;

	return(value);
    }


    final String
    getField(int partition) {

	return(partition == id ? getField() : null);
    }


    final double
    getValue() {

	return(data instanceof Number ? ((Number)data).doubleValue() : Double.NaN);
    }


    final double
    getValue(int partition) {

	return(partition == id ? getValue() : Double.NaN);
    }


    public String
    toString() {

	return(data != null ? data.toString() : "null");
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private synchronized void
    translate() {

	if (generator != null) {
	    data = generator.translate((String)data);
	    generator = null;
	}
    }
}

