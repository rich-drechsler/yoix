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
import java.awt.event.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Hashtable;
import javax.swing.*;
import att.research.yoix.*;

public abstract
class SwingDataViewer extends SwingDataColorer

    implements DataViewer,
	       SweepFilter

{

    SwingDataColorer  coloredby = null;
    DataManager       datamanager = null;
    DataRecord        datarecords[];
    DataRecord        loadeddata[];
    Hashtable         colormap;
    Hashtable         diversitymap;
    HashMap           viewermap;
    Palette           currentpalette = null;

    boolean  accumulate = false;
    boolean  autoready = false;
    boolean  autoshow = false;
    boolean  sweepfiltering = false;
    boolean  reversepalette = false;
    boolean  stacked = false;
    boolean  transientmode = false;

    boolean  fastlookups[] = null;
    int      fieldmasks[] = null;
    int      selectmasks[] = null;
    int      fieldindices[] = null;
    int      valueindices[] = null;
    int      partitionindices[] = null;
    int      primaryfield = 0;
    int      diversityindex = -1;
    int      activefieldcount = 0;
    int      active = 0;

    //
    // We use andmodel to decide how selectmasks[] is built, and that also
    // will control how records are selected there's more than one mask in
    // filtermasks[]. Currently no way Yoix scripts can change this because
    // a general implementation is probably trickier than you might expect.
    // We eventually may let the constructor adjust the value, but for now
    // that's not even allowed.
    // 

    private boolean  andmodel = false;

    //
    // Remembering the JLayeredPane can sometimes be useful because it can
    // be used to get a Graphics object with a clip that protects components
    // that are being displayed in the JLayeredPane.
    // 

    protected JLayeredPane  layeredpane = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    SwingDataViewer(YoixObject data, YoixBodyComponent parent) {

	super(data, parent);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceShowing Methods
    //
    ///////////////////////////////////

    public final synchronized void
    handleShowingChange(boolean state) {

	if (datamanager != null) {
	    if (state) {
		if (isLoadable() && isLoaded() == false)
		    datamanager.loadViewer(this);
		layeredpane = YoixMiscJFC.getJLayeredPane(this);
	    } else {
		if (autoready == false && sweepfiltering == false && isActiveDataColorer() == false) {
		    loadeddata = null;
		    datarecords = null;
		}
		layeredpane = null;
		painted = false;
	    }
	}
    }

    ///////////////////////////////////
    //
    // DataColorer Methods
    //
    ///////////////////////////////////

    public final Palette
    getCurrentPalette() {

	return(currentpalette);
    }


    public final DataManager
    getDataManager() {

	return(datamanager);
    }


    public boolean
    isActiveDataColorer() {

	return(false);
    }


    public final boolean
    isDataColorer() {

	DataManager  manager;

	return((manager = datamanager) != null && manager.getColoredBy() == this);
    }


    public final boolean
    isManagedBy(DataManager manager) {

	return(this.datamanager == manager);
    }


    public void
    loadColors() {

    }

    ///////////////////////////////////
    //
    // DataViewer Methods
    //
    ///////////////////////////////////

    public ArrayList
    collectRecordsAt(Point point, boolean selected) {

	return(null);
    }


    public final synchronized int
    getActiveFieldCount() {

	return(datamanager != null ? activefieldcount : 0);
    }


    public final synchronized YoixObject
    getHighlighted() {

	YoixObject  obj;
	ArrayList   buffer;
	int         length;
	int         n;

	if (totalcount > 0) {
	    buffer = new ArrayList();
	    for (n = 0; n < totalcount; n++) {
		if (isHighlighted(n))
		    buffer.add(YoixObject.newString(getKey(n)));
	    }
	    obj = YoixMisc.copyIntoArray(buffer, true, buffer);
	} else obj = null;

	return(obj != null ? obj : YoixObject.newArray());
    }


    public final synchronized YoixObject
    getPressed() {

	YoixObject  obj;
	ArrayList   buffer;
	int         length;
	int         n;

	if (totalcount > 0) {
	    buffer = new ArrayList();
	    for (n = 0; n < totalcount; n++) {
		if (isPressed(n))
		    buffer.add(YoixObject.newString(getKey(n)));
	    }
	    obj = YoixMisc.copyIntoArray(buffer, true, buffer);
	} else obj = null;

	return(obj != null ? obj : YoixObject.newArray());
    }


    public final boolean
    getStackMode() {

	return(stacked);
    }


    public final synchronized void
    setDiversityIndex(int index) {

	diversityindex = Math.max(index, -1);
    }

    ///////////////////////////////////
    //
    // SweepFilter Methods
    //
    ///////////////////////////////////

    public final synchronized void
    clear() {

	if (sweepfiltering && datamanager != null) {
	    if (isLoaded())
		datamanager.clearViewer(this);
	}
    }


    public final synchronized void
    clear(boolean selected) {

	DataRecord  loaded[];
	DataRecord  record;
	HitBuffer   hits[];
	int         counts[];
	int         masks[];
	int         length;
	int         count;
	int         n;

	//
	// Harder than you might expect when clearing unselected records
	// because when we're done none of those records can be deselected
	// by the masks associated with this filter.
	//

	if (sweepfiltering && datamanager != null) {
	    if (isLoaded()) {
		if ((masks = getSelectMasks()) != null) {
		    hits = new HitBuffer[2];
		    if ((hits[0] = datamanager.getHitBuffer(loadeddata)) != null) {
			if (selected || (hits[1] = datamanager.getHitBuffer(loadeddata)) != null) {
			    counts = new int[] {0, 0};
			    length = datarecords.length;
			    for (n = 0; n < length; n++) {
				record = datarecords[n];
				if (selected == false) {
				    if (record.notSelected(masks)) {
					record.setSelected(masks);
					if (record.isSelected())
					    hits[1].setRecord(counts[1]++, record, true);
				    } else if (record.isSelected())
					hits[0].setRecord(counts[0]++, record);
				} else if (record.notSelected())
				    hits[0].setRecord(counts[0]++, record);
			    }
			    //
			    // Remember loadeddata because it may end up null
			    // in loadRecords() but datamanager.updateData()
			    // checks value before doing anything. Moving the
			    // loadRecords() call also works, but doing the
			    // load first seems safer.
			    //
			    loaded = loadeddata;	// set to null in clearTable()
			    loadRecords(loaded, hits[0].copyRecords(counts[0]), false);
			    if (hits[1] != null) {
				if (counts[1] > 0) {
				    datamanager.updateData(loaded, hits[1], counts[1], this);
				    Thread.yield();
				} else datamanager.releaseHitBuffer(hits[1]);
			    }
			}
			datamanager.releaseHitBuffer(hits[0]);
		    }
		}
	    }
	}
    }


    public final synchronized boolean
    getAccumulate() {

	return(accumulate && sweepfiltering);
    }


    public final int
    getAccumulatedRecords(HitBuffer hits, BitSet accumulated) {

	return(getAccumulatedRecords(hits, accumulated, getAccumulate()));
    }


    public final synchronized int[]
    getFieldIndices() {

	return(datamanager != null ? fieldindices : null);
    }


    public final synchronized int[]
    getFieldMasks() {

	return(datamanager != null ? fieldmasks : null);
    }


    public final synchronized int[]
    getSelectMasks() {

	return(datamanager != null ? selectmasks : null);
    }


    public final synchronized boolean
    isSweepFilter(DataPlot plot) {

	return(sweepfiltering && plot.isManagedBy(datamanager));
    }


    public final synchronized void
    loadRecords(YoixObject records, boolean accumulating) {

	DataRecord  loaded[];
	DataRecord  record;
	YoixObject  element;
	HitBuffer   hits;
	BitSet      accumulated;
	int         length;
	int         index;
	int         count;
	int         start;
	int         n;

	//
	// Loads the DataRecords records identified by the integers indices
	// stored in the records array. This is only designed to be called
	// by the loadRecords() builtin that's supposed to handle actions,
	// like drag and drop, that may be used to load sweep filters.
	//

	if (sweepfiltering && datamanager != null) {
	    if ((loaded = datamanager.getDataRecords()) != null) {
		if ((hits = datamanager.getHitBuffer(loaded)) != null) {
		    if (records.isArray() && records.sizeof() > 0) {
			accumulated = new BitSet(0);
			length = records.length();
			start = getAccumulatedRecords(hits, accumulated, accumulating);
			count = start;
			for (n = records.offset(); n < length; n++) {
			    if ((element = records.getObject(n)) != null) {
				if (element.isInteger()) {
				    index = element.intValue();
				    if (index >= 0 && index < loaded.length) {
					record = loaded[index];
					if (accumulated.get(record.getID()) == false)
					    hits.setRecord(count++, record);
				    }
				}
			    }
			}
			if (count > start)
			    loadRecords(loaded, hits.copyRecords(count), false);
		    }
		}
		datamanager.releaseHitBuffer(hits);
	    }
	}
    }


    public final void
    recolorSweepFilter() {

	recolorViewer();
    }


    public void
    recordsSorted(DataRecord records[]) {

    }


    public final void
    repaintSweepFilter() {

	repaintViewer();
    }


    public final void
    repaintSweepFilter(int count) {

	repaintViewer(count);
    }


    public final synchronized void
    setDataManager(DataManager manager, int index, int mask) {

	setDataManager(manager, new int[] {index}, new int[] {mask}, new int[] {-1}, null);
    }


    public final synchronized void
    setDataManager(DataManager manager, int indices[], int masks[], int values[], int partitions[]) {

	//
	// Eventually add more checking. For example, make sure masks
	// is smaller than indices and that partitions and indices are
	// the same size? For now we assume the caller is disciplined,
	// which is OK because calls only come from DataManager.java.
	//
	// NOTE - the call to syncActive() is a recent addition (2/7/07)
	// that is supposed to make sure active stays in bounds. Doubt
	// it's really needed but if it did change active there could be
	// some inconsistency between our display and the checkbox menu
	// items that users can access.
	// 

	if (datamanager != manager || manager == null) {
	    datamanager = manager;
	    fieldindices = indices;
	    fieldmasks = masks;
	    valueindices = values;
	    selectmasks = DataRecord.getSelectMasks(fieldmasks, andmodel);
	    partitionindices = partitions;
	    setFastLookups();
	    setActiveFieldCount();
	    syncActive();
	    if (isLoaded())
		loadRecords(loadeddata, datarecords, false);
	}
    }


    public final synchronized void
    setSweepFiltering(boolean value) {

	if (sweepfiltering != value) {
	    sweepfiltering = value;
	    if (loadeddata != null)
		clear();
	}
	setFastLookups();
    }


    public final void
    updateSweepFilter(DataRecord loaded[], HitBuffer hits, int count) {

	updateViewer(loaded, hits, count);
    }

    ///////////////////////////////////
    //
    // SwingDataViewer Methods
    //
    ///////////////////////////////////

    final synchronized int
    collectRecords(HitBuffer hits, HashMap select, HashMap deselect) {

	DataRecord  record;
	String      name;
	int         masks[];
	int         length;
	int         count = 0;
	int         index;
	int         m;
	int         n;

	if ((masks = getSelectMasks()) != null) {
	    if (activefieldcount > 0) {
		length = datarecords.length;
		for (n = 0; n < length; n++) {
		    record = datarecords[n];
		    for (m = 0; m < activefieldcount; m++) {
			if ((name = getRecordName(record, m)) != null) {
			    if (select != null && select.containsKey(name)) {
				if (record.notSelected(masks)) {
				    record.setSelected(masks);
				    if (record.isSelected())
					hits.setRecord(count++, record, true);
				}
			    } else if (deselect != null && deselect.containsKey(name)) {
				if (record.isSelected(masks)) {
				    if (record.isSelected())
					hits.setRecord(count++, record, false);
				    record.clearSelected(masks);
				}
			    }
			}
		    }
		}
	    }
	}
	return(count);
    }


    final void
    colorViewer() {

	colorViewerWith(currentpalette);
    }


    protected YoixObject
    eventCoordinates(AWTEvent e) {

	return(null);
    }

    
    protected void
    finalize() {

	datamanager = null;
	datarecords = null;
	loadeddata = null;
        viewermap = null;
        colormap = null;
	currentpalette = null;
	coloredby = null;
	super.finalize();
    }


    final void
    forceDataLoad() {

	if (isLoaded() == false && sweepfiltering == false) {
	    if (datamanager != null)
		datamanager.forceDataLoad(this);
	}
    }


    final int
    getActive() {

	return(active);
    }


    final double
    getActiveValue(DataRecord record) {

	return(active > 0 ? record.getValue(valueindices[active-1]) : 1.0);
    }


    final SwingDataColorer
    getColoredBy() {

	return(coloredby);
    }


    final synchronized int
    getFieldIndex() {

	return(getFieldIndex(primaryfield));
    }


    final synchronized int
    getFieldIndex(int n) {

	return(datamanager != null && fieldindices != null && (n >= 0 && n < fieldindices.length) ? fieldindices[n] : -1);
    }


    final synchronized int
    getFieldMask() {

	return(getFieldMask(primaryfield));
    }


    final String
    getRecordName(DataRecord record) {

	return(getRecordName(record, primaryfield));
    }


    final String
    getRecordName(DataRecord record, int n) {

	String  name;

	if (fieldindices != null && n >= 0 && n < fieldindices.length) {
	    if (fastlookups != null && fastlookups[n])
		name = record.getField(fieldindices[n]);
	    else if (partitionindices != null && partitionindices[n] >= 0)
		name = record.getField(fieldindices[n], partitionindices[n]);
	    else name = record.getID() + "";		// new string each time??
	} else name = null;

	return(name);
    }


    final double[]
    getRecordValues(DataRecord record, int sign) {

	return(getRecordValues(record, valueindices, sign));
    }


    final double[]
    getRecordValues(DataRecord record, int indices[], int sign) {

	double  values[];
	int     length;
	int     n;

	if (indices != null) {
	    length = indices.length;
	    values = new double[length];
	    for (n = 0; n < length; n++)
		values[n] = sign*record.getValue(indices[n]);
	} else values = null;
	return(values);
    }


    final synchronized YoixObject
    getSelected() {

	YoixObject  obj;
	ArrayList   buffer;
	int         n;

	if (totalcount > 0) {
	    buffer = new ArrayList();
	    for (n = 0; n < totalcount; n++) {
		if (isSelected(n))
		    buffer.add(YoixObject.newString(getKey(n)));
	    }
	    obj = YoixMisc.copyIntoArray(buffer, true, buffer);
	} else obj = null;

	return(obj != null ? obj : YoixObject.newArray());
    }


    final synchronized String
    getText() {

	String  text = null;
	int     count;
	int     n;

	if (totalcount > 0) {
	    text = "";
	    for (n = 0; n < totalcount; n++) {
		for (count = getCountTotal(n); count > 0; count--)
		    text += getKey(n) + "\n";
	    }
	} else text = null;

	return(text);
    }


    final synchronized boolean
    isClear() {

	return(loadeddata == null || datarecords == null);
    }


    final synchronized boolean
    isLoaded() {

	return(loadeddata != null && datarecords != null);
    }


    final synchronized boolean
    isLoadable() {

	return(sweepfiltering == false && (autoready || isShowing() || isDataColorer()));
    }


    final synchronized boolean
    isReady() {

	return(autoready || sweepfiltering || isShowing() || isDataColorer());
    }


    final synchronized void
    setAccumulate(boolean accumulate) {

	this.accumulate = accumulate;
    }


    final synchronized void
    setActive(int active) {

	if (active >= 0) {
	    if (this.active != active) {
		this.active = active;
		loadRecords(loadeddata, datarecords, false);
	    }
	}
    }


    final synchronized void
    setAll(boolean state) {

	HashMap  select;
	HashMap  deselect;

	if (viewermap != null) {
	    if (state) {
		deselect = new HashMap();
		select = (HashMap)viewermap.clone();
	    } else {
		deselect = (HashMap)viewermap.clone();
		select = new HashMap();
	    }
	    setSelected(select, deselect);
	}
    }


    final synchronized void
    setAll(boolean state, YoixObject obj) {

	YoixObject  element;
	HashMap     select;
	HashMap     deselect;
	HashMap     target;
	String      key;
	Object      value;
	int         n;

	//
	// A recent addition (9/9/05) that only changes the state of the
	// elements that are named in obj, which currently should be an
	// array of strings that identify the target elements. Looks much
	// than it probably should because setSelected() currently seems
	// to want HashMap arguments that cover the data that's loaded.
	//

	if (viewermap != null && obj != null) {
	    target = new HashMap();
	    if (obj.isArray()) {
		target = new HashMap();
		for (n = obj.offset(); n < obj.length(); n++) {
		    if ((element = obj.getObject(n)) != null) {
			if (element.isString()) {
			    key = element.stringValue();
			    if (viewermap.containsKey(key))
				target.put(key, Boolean.TRUE);
			}
		    }
		}
	    }

	    if (target.size() > 0) {
		deselect = new HashMap();
		select = new HashMap();
		for (n = 0; n < totalcount; n++) {
		    key = getKey(n);
		    if ((value = viewermap.get(key)) != null) {
			if (state) {
			    if (isSelected(n) || target.containsKey(key))
				select.put(key, value);
			    else deselect.put(key, value);
			} else {
			    if (isSelected(n) == false || target.containsKey(key))
				deselect.put(key, value);
			    else select.put(key, value);
			}
		    }
		}
		setSelected(select, deselect);
	    }
	}
    }


    final synchronized void
    setAutoReady(boolean value) {

	if (autoready != value) {
	    autoready = value;
	    //
	    // Is there more to do??
	    //
	}
    }


    final void
    setAutoShow(boolean state) {

	autoshow = state;
    }


    final synchronized void
    setColoredBy(SwingDataColorer colorer) {

	if (colorer != coloredby) {
	    coloredby = colorer;
	    recolorViewer();
	    if (datamanager != null) {
		if (datamanager.getColoredBy() == this)
		    datamanager.recolorData(this);
	    }
	}
    }


    final synchronized void
    setDataManager(int index) {

	//
	// Recent addition (10/9/05) that hides some of the ugly details
	// from the setText() method that's now defined in histograms.
	//

	setDataManager(null, new int[] {index}, new int[] {1}, new int[] {-1}, new int[] {-1});
    }


    final synchronized void
    setOtherColor(Color color) {

	color = (color != null) ? color : OTHERCOLOR;

	if (othercolor == null || othercolor.equals(color) == false) {
	    othercolor = color;
	    colorViewer();
	    reset();
	    if (datamanager != null) {
		if (datamanager.getColoredBy() == this)
		    datamanager.recolorData(this);
	    }
	}
    }


    final synchronized void
    setPalette(YoixObject obj) {

	Palette  palette;

	palette = (obj != null && obj.notNull()) ? (Palette)getBody(obj) : null;

	if (palette != currentpalette) {
	    currentpalette = palette;
	    colorViewer();
	    reset();
	}
    }


    final synchronized void
    setPrimaryField(int field) {

	if (primaryfield != field) {
	    primaryfield = field;
	    reset();			// unnecessary???
	}
    }


    final synchronized void
    setReversePalette(boolean state) {

	//
	// Not a good implementation even though it works, because setting
	// the palette's inverted flag affects everyone using the palette.
	// Implementation really should be changed before Yoix scripts start
	// using this again!!
	//

	if (reversepalette != state) {
	    reversepalette = state;
	    if (currentpalette != null)
		currentpalette.setInverted(reversepalette);
	    colorViewer();
	    reset();
	    if (datamanager != null) {
		if (datamanager.getColoredBy() == this)
		    datamanager.recolorData(this);
	    }
	}
    }


    final synchronized void
    setTransientMode(boolean state) {

	transientmode = state;
    }


    final synchronized void
    updateData(int number, boolean selected) {

	HitBuffer  hits;
	int        count;

	if (totalcount > 0) {
	    if (datamanager != null) {
		if (datarecords != null && loadeddata != null) {
		    if (activefieldcount > 0) {
			if ((hits = datamanager.getHitBuffer(loadeddata)) != null) {
			    count = collectRecords(hits, number, selected);
			    if (count > 0) {
				datamanager.updateData(loadeddata, hits, count, this);
				Thread.yield();
			    } else {
				datamanager.releaseHitBuffer(hits);
				repaintViewer();
			    }
			}
		    }
		}
	    }
	}
    }


    final synchronized void
    updateData(ArrayList touched, boolean selected) {

	HitBuffer  hits;
	int        count;
	int        size;

	if (touched != null) {
	    size = touched.size();
	    if (totalcount > 0 && size > 0) {
		if (datamanager != null) {
		    if (size > 1) {
			if (datarecords != null && loadeddata != null) {
			    if (activefieldcount > 0) {
				if ((hits = datamanager.getHitBuffer(loadeddata)) != null) {
				    count = collectRecords(hits, touched, selected);
				    if (count > 0) {
					datamanager.updateData(loadeddata, hits, count, this);
					Thread.yield();
				    } else {
					datamanager.releaseHitBuffer(hits);
					repaintViewer();
				    }
				}
			    }
			}
		    } else updateData(((Integer)touched.get(0)).intValue(), selected);
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private synchronized int
    collectRecords(HitBuffer hits, int number, boolean selected) {

	DataRecord  record;
	String      key;
	String      name;
	int         masks[];
	int         length;
	int         total;
	int         count = 0;
	int         m;
	int         n;

	//
	// This is the original brute force algorithm that should always
	// work. It's currently private, but we eventually may want to let
	// subclasses implement their own (faster) version. In fact that's
	// just what our histogram classes did in older versions, but when
	// we tossed that version (and made this private) when we decided
	// to implement Guy Jacobson's "set-valued" histograms (5/1/05).
	// There's no reason why the custom histogram version couldn't be
	// restored if this method wasn't private - maybe later.
	//

	if ((masks = getSelectMasks()) != null) {
	    if (activefieldcount > 0) {
		length = datarecords.length;
		key = getKey(number);
		total = (key != null) ? getCountTotal(number) : length;
		for (n = 0; n < length && total > 0; n++) {
		    record = datarecords[n];
		    for (m = 0; m < activefieldcount; m++) {
			if ((name = getRecordName(record, m)) != null) {
			    if (name.equals(key) || key == null) {
				total--;
				if (selected) {
				    if (record.notSelected(masks)) {
					record.setSelected(masks);
					if (record.isSelected())
					    hits.setRecord(count++, record, true);
				    }
				} else {
				    if (record.isSelected(masks)) {
					if (record.isSelected())
					    hits.setRecord(count++, record, false);
					record.clearSelected(masks);
				    }
				}
			    }
			}
		    }
		}
	    }
	}
	return(count);
    }


    private synchronized int
    collectRecords(HitBuffer hits, ArrayList touched, boolean selected) {

	DataRecord  record;
	HashMap     keys;
	Object      value;
	String      name;
	int         masks[];
	int         length;
	int         total;
	int         count = 0;
	int         size;
	int         m;
	int         n;

	//
	// Collects records when more than one bin is touched, so we only
	// have to request one update from datamanager. Initially done to
	// conserve hit buffers, but we noticed a speed improvement too.
	// There's still room for improvement - maybe later.
	//

	if ((masks = getSelectMasks()) != null) {
	    if (activefieldcount > 0) {
		length = datarecords.length;
		size = touched.size();
		keys = new HashMap((int)(1.1*size));
		total = 0;
		for (n = 0; n < size; n++) {
		    if ((value = touched.get(n)) != null) {
			if ((m = ((Integer)value).intValue()) >= 0) {
			    keys.put(getKey(m), Boolean.TRUE);
			    total += getCountTotal(m);
			}
		    }
		}
		for (n = 0; n < length && total > 0; n++) {
		    record = datarecords[n];
		    for (m = 0; m < activefieldcount; m++) {
			if ((name = getRecordName(record, m)) != null) {
			    if (keys.containsKey(name)) {
				total--;
				if (selected) {
				    if (record.notSelected(masks)) {
					record.setSelected(masks);
					if (record.isSelected())
					    hits.setRecord(count++, record, true);
				    }
				} else {
				    if (record.isSelected(masks)) {
					if (record.isSelected())
					    hits.setRecord(count++, record, false);
					record.clearSelected(masks);
				    }
				}
			    }

			}
		    }
		}
	    }
	}
	return(count);
    }


    private synchronized int
    getAccumulatedRecords(HitBuffer hits, BitSet accumulated, boolean accumulating) {

	int  masks[];
	int  count;
	int  n;

	//
	// Using HitBuffer is just a convenience - no records have their
	// state changed and collected records aren't sent back to the
	// DataManager.
	//

	count = 0;

	if (datarecords != null && loadeddata != null) {
	    masks = getSelectMasks();
	    for (n = 0; n < datarecords.length; n++) {
		if (accumulating || datarecords[n].notSelected(masks)) {
		    hits.setRecord(count++, datarecords[n]);
		    if (accumulated != null)
			accumulated.set(datarecords[n].getID());
		}
	    }
	}
	return(count);
    }


    private synchronized int
    getFieldMask(int n) {

	return(datamanager != null && fieldmasks != null && (n >= 0 && n < fieldmasks.length) ? fieldmasks[n] : -1);
    }


    private synchronized void
    setActiveFieldCount() {

	if (fieldindices != null) {
	    if (fieldmasks != null)
		activefieldcount = Math.min(fieldindices.length, fieldmasks.length);
	    else activefieldcount = 0;
	} else activefieldcount = 0;
    }


    private synchronized void
    setFastLookups() {

	int  n;

	//
	// We changed the initial test from
	//
	//	if (partitionindices != null && sweepfiltering == false)
	//
	// on 6/19/07 because it stopped the new sweepgraphs from looking
	// for real field names when partitions weren't involved. The bad
	// behavior and the tests that made things work for partitions can
	// be found in getRecordName().
	//

	if (partitionindices != null) {
	    fastlookups = new boolean[partitionindices.length];
	    for (n = 0; n < fastlookups.length; n++)
		fastlookups[n] = (partitionindices[n] < 0);
	} else fastlookups = null;
    }


    private synchronized void
    syncActive() {

	if (valueindices == null || active < 0 || active > valueindices.length)
	    active = 0;
    }
}

