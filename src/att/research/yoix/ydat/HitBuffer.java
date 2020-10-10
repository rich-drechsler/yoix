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
import att.research.yoix.*;

class HitBuffer

{

    //
    // The older versions used an array of DataRecords and checked each
    // record's current state when the hits were processed to determine
    // the intention of the hit (i.e., the record was removed or added).
    // Turns out we really need the state at the time of the hit rather
    // than the time it's processed in objects (e.g., Histograms) that
    // increment or decrement counters. Anyway this class addresses the
    // the Histogram "counter" issue by saving the state of each record
    // at the time the hit is recorded.
    //
    // Lots of room for improvement, but we'll wait a bit for things to
    // settle down before making more changes. One obvious change would
    // be to implement addRecord() and use it in place of setRecord().
    // Also could use a BitSet instead of a bollean array to keep track
    // of selected state - might save some memory but could be slower??
    //

    private DataRecord  records[];
    private boolean     selected[];
    private int         recordindices[];
    private int         length;
 
    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    HitBuffer(int length) {

	this.length = length;
	this.records = new DataRecord[length];
	this.selected = new boolean[length];
	this.recordindices = new int[length];		// recent addition
    }

    ///////////////////////////////////
    //
    // HitBuffer Methods
    //
    ///////////////////////////////////

    final DataRecord[]
    copyRecords(int count) {

	DataRecord  copy[];

	copy = new DataRecord[count];
	System.arraycopy(records, 0, copy, 0, count);
	return(copy);
    }


    final int[]
    copyRecordIndices(int count) {

	int  indices[];

	indices = new int[count];
	System.arraycopy(recordindices, 0, indices, 0, count);
	return(indices);
    }


    final int
    getLength() {

	return(length);
    }


    final DataRecord
    getRecord(int index) {

	return(records[index]);
    }


    final int
    getRecordIndex(int index) {

	return(recordindices[index]);
    }


    final int[]
    getRecordIndices() {

	return(recordindices);
    }


    final int[]
    getSortedIndices(int count, int mapping[]) {

	int  indices[];
	int  n;

	//
	// When mapping is null we assume the caller has determined that
	// any additional record sorting is unnecessary.
	// 

	indices = new int[count];
	if (mapping != null) {
	    for (n = 0; n < count; n++)
		indices[n] = mapping[recordindices[n]];
	    YoixMiscQsort.sort(indices);
	} else System.arraycopy(recordindices, 0, indices, 0, count);

	return(indices);
    }


    final boolean
    isSelected(int index) {

	return(selected[index]);
    }


    final void
    releaseBuffers() {

	//
	// Should only be called when we're certain the HitBuffer will
	// not be used again. We've skipped bounds and existence checks
	// in other methods to help efficiency, but that means users of
	// HitBuffers must be careful.
	//

	length = 0;
	records = null;
	selected = null;
	recordindices = null;
    }


    final void
    setRecord(int index, DataRecord record) {

	//
	// Calling this essentially means the stuff saved selected is 
	// useless. Means callers need to be consistent and make sure
	// a single use of a HitBuffer always calls the same version
	// of setRecord(). Also means hits collected by this method
	// shouldn't be passed to the DataManager's updateDate method.
	// Not hard to imagine being more careful, but once again we
	// chose not to for efficieny reasons (at least for now).

	records[index] = record;
	recordindices[index] = record.getIndex();
    }


    final void
    setRecord(int index, DataRecord record, boolean state) {

	records[index] = record;
	recordindices[index] = record.getIndex();
	selected[index] = state;
    }
}

