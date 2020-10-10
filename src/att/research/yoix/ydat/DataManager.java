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
import java.io.*;
import java.text.*;
import java.util.*;
import att.research.yoix.*;

class DataManager extends YoixPointerActive

    implements Constants

{

    //
    // We're now using the event thread, rather than a private thread, to
    // handle all requests that can be queued. Change was made on 4/23/09
    // because we noticed a potential deadlock issue that cropped up when
    // the interpreter was running in threadsafe mode. The change itself
    // was straightforward but it did affect quite a few methods, however
    // it was something that we first considered quite a while ago (long
    // before any possible deadlock issues came up).
    //

    private DataGenerator  datafields[];
    private DataColorer    coloredby;
    private SweepFilter    sweepfilters[];
    private DataViewer     datafilters[];
    private DataViewer     dataviewers[];
    private DataRecord     datarecords[];
    private DataTable      datatables[];
    private Hashtable      datatags;
    private Hashtable      datapartitions;
    private Hashtable      recolored;
    private DataPlot       dataplots[];
    private DataPlot       sortedby;
    private boolean        activated = false;
    private boolean        disposed = false;
    private int            selectedcount;
    private int            totalcount;

    private double  countervalues[];
    private double  countertotals[];
    private String  countertags[];
    private int     counterindices[];

    //
    // This field is here rather than in JGraphPlot objects because
    // the structure is associated with the data, not with the visual
    // representation, of which there may be many variations.
    //

    private YoixGraphElement  graphdata;

    //
    // This is here to support a temporary kludge that's needed when
    // this is used with old Yoix scripts. Really only needed by one
    // important internal application, so all of this probably can
    // disappear when that application is updated. Problems happen
    // because we changed the NL_PLOTFILTERS name to NL_SWEEPFILTERS
    // when we added SwingJDataTable.java, but in this case we simply
    // forgot (or perhaps ignored) portability issues. Anyway, all of
    // the nonsense needed to support NL_PLOTFILTERS can be removed
    // from this file and from Module.java!!!
    //

    private String  sweepfiltersname = null;	// temporary kludge!!!

    //
    // Stuff for managing selection masks. Harder than it needs to
    // be, but data and value fields must use separate bits despite
    // the fact that they have the same names and indices.
    //

    private Hashtable  maskmap;		// may be unnecessary
    private Vector     maskstack;	// avaliable masks

    //
    // A buffer that has room for all loaded records and can be used to
    // collect the records that were touched when an active filter or
    // plot changed. The buffer is allocated and released by two new
    // methods.
    //

    private final Object  HITBUFFERLOCK = new Object();
    private HitBuffer     hitbuffer;

    //
    // Stuff used to extract fields from the input string.
    //

    private String  inputcomment = null;
    private Object  inputseparators = "|";
    private BitSet  inputfields = null;
    private int     fieldcount = 0;

    //
    // Remembering callback functions helps reduce lookup and checking
    // overhead.
    //

    private YoixObject  aftercoloredby = null;
    private YoixObject  afterload = null;
    private YoixObject  afterupdate = null;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(35);

    static {
	activefields.put(NL_AFTERCOLOREDBY, new Integer(VL_AFTERCOLOREDBY));
	activefields.put(NL_AFTERLOAD, new Integer(VL_AFTERLOAD));
	activefields.put(NL_AFTERUPDATE, new Integer(VL_AFTERUPDATE));
	activefields.put(NL_APPENDTEXT, new Integer(VL_APPENDTEXT));
	activefields.put(NL_COLOREDBY, new Integer(VL_COLOREDBY));
	activefields.put(NL_COUNTERS, new Integer(VL_COUNTERS));
	activefields.put(NL_DATAFIELDS, new Integer(VL_DATAFIELDS));
	activefields.put(NL_DATAFILTERS, new Integer(VL_DATAFILTERS));
	activefields.put(NL_DATAPLOTS, new Integer(VL_DATAPLOTS));
	activefields.put(NL_DATATABLES, new Integer(VL_DATATABLES));
	activefields.put(NL_DATAVIEWERS, new Integer(VL_DATAVIEWERS));
	activefields.put(NL_DISPOSE, new Integer(VL_DISPOSE));
	activefields.put(NL_GETFIELDS, new Integer(VL_GETFIELDS));
	activefields.put(NL_GETINDEX, new Integer(VL_GETINDEX));
	activefields.put(NL_GETINDICES, new Integer(VL_GETINDICES));
	activefields.put(NL_GETSTATE, new Integer(VL_GETSTATE));
	activefields.put(NL_GETVALUES, new Integer(VL_GETVALUES));
	activefields.put(NL_GRAPHDATA, new Integer(VL_GRAPHDATA));
	activefields.put(NL_INPUTCOMMENT, new Integer(VL_INPUTCOMMENT));
	activefields.put(NL_INPUTFILTER, new Integer(VL_INPUTFILTER));
	activefields.put(NL_LOADEDCOUNT, new Integer(VL_LOADEDCOUNT));
	activefields.put(NL_PLOTFILTERS, new Integer(VL_PLOTFILTERS));
	activefields.put(NL_SELECTED, new Integer(VL_SELECTED));
	activefields.put(NL_SELECTEDCOUNT, new Integer(VL_SELECTEDCOUNT));
	activefields.put(NL_SORTEDBY, new Integer(VL_SORTEDBY));
	activefields.put(NL_SWEEPFILTERS, new Integer(VL_SWEEPFILTERS));
	activefields.put(NL_TEXT, new Integer(VL_TEXT));
	activefields.put(NL_TOTALS, new Integer(VL_TOTALS));
	activefields.put(NL_UNSELECTED, new Integer(VL_UNSELECTED));
    }

    //
    // Queued command identifiers.
    //

    private static final int  COMMAND_DATARECOLOR = 1;
    private static final int  COMMAND_PLOTLOAD = 2;
    private static final int  COMMAND_PLOTUPDATE = 3;
    private static final int  COMMAND_SORTRECORDS = 4;
    private static final int  COMMAND_SWEEPFILTERUPDATE = 5;
    private static final int  COMMAND_TABLECLEAR = 6;
    private static final int  COMMAND_TABLELOAD = 7;
    private static final int  COMMAND_TABLEUPDATE = 8;
    private static final int  COMMAND_VIEWERCLEAR = 9;
    private static final int  COMMAND_VIEWERLOAD = 10;
    private static final int  COMMAND_VIEWERRECOLOR = 11;
    private static final int  COMMAND_VIEWERUNCOLOR = 12;
    private static final int  COMMAND_VIEWERUPDATE = 13;

    //
    // Constants and a few arrays that can be used to monitor activity.
    // Changed names and removed STATE_SORTING on 6/6/07.
    //

    private int  currentstate = STATE_IDLE;

    private static final int  STATE_IDLE = 0;
    private static final int  STATE_RUNNING = 1;
    private static final int  STATE_LOADING_DATA = 2;
    private static final int  STATE_BUILDING_RECORDS = 3;
    private static final int  STATE_LOADING_COMPONENTS = 4;

    private static final String  STATENAME[] = {
	"Idle",
	"Running",
	"Loading Data",
	"Building Records",
	"Loading Components",
    };

    private String  stateargument[] = {null, null, null, null, null};
    private long    statetimer[] = {0, 0, 0, 0, 0};
    private int     stateposition[] = {0, 0, 0, 0, 0};
    private int     statetotal[] = {0, 0, 0, 0, 0};

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    DataManager(YoixObject data) {

	super(data);
	buildDataManager();
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

	return(DATAMANAGER);
    }

    ///////////////////////////////////
    //
    // DataManager Methods
    //
    ///////////////////////////////////

    final void
    clearTable(DataTable table) {

	Object  args[];

	args = new Object[] {
	    new Integer(COMMAND_TABLECLEAR),
	    table
	};

	if (EventQueue.isDispatchThread() == false)
	    EventQueue.invokeLater(new YoixAWTInvocationEvent(this, args));
	else handleRun(args);
    }


    final void
    clearViewer(DataViewer viewer) {

	Object  args[];

	args = new Object[] {
	    new Integer(COMMAND_VIEWERCLEAR),
	    viewer
	};

	if (EventQueue.isDispatchThread() == false)
	    EventQueue.invokeLater(new YoixAWTInvocationEvent(this, args));
	else handleRun(args);
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case VL_APPENDTEXT:
		obj = builtinAppendText(name, argv);
		break;

	    case VL_GETFIELDS:
		obj = builtinGetFields(name, argv);
		break;

	    case VL_GETINDEX:
		obj = builtinGetIndex(name, argv);
		break;

	    case VL_GETINDICES:
		obj = builtinGetIndices(name, argv);
		break;

	    case VL_GETSTATE:
		obj = builtinGetState(name, argv);
		break;

	    case VL_GETVALUES:
		obj = builtinGetValues(name, argv);
		break;

	    default:
		obj = null;
		break;
	}

	return(obj);
    }


    protected final void
    finalize() {

	datafilters = null;
	dataplots = null;
	dataviewers = null;
	datatables = null;
	datarecords = null;
	datafields = null;
	datatags = null;
	datapartitions = null;
	counterindices = null;
	countertags = null;
	countertotals = null;
	countervalues = null;
	sweepfilters = null;
	hitbuffer = null;
	coloredby = null;
	sortedby = null;
	recolored = null;
	super.finalize();
    }


    final void
    forceDataLoad(DataPlot plot) {

	handlePlotLoad(plot, true);
    }


    final void
    forceDataLoad(DataTable table) {

	handleTableLoad(table, true);
    }


    final void
    forceDataLoad(DataViewer viewer) {

	handleViewerLoad(viewer, true);
    }


    final DataColorer
    getColoredBy() {

	return(coloredby);
    }


    final Palette
    getCurrentPalette() {

	return(coloredby != null ? coloredby.getCurrentPalette() : null);
    }


    final DataRecord[]
    getDataRecords() {

	return(datarecords);
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case VL_COUNTERS:
		obj = getCounters(obj);
		break;

	    case VL_DISPOSE:
		obj = YoixObject.newInt(disposed);
		break;

	    case VL_LOADEDCOUNT:
		obj = YoixObject.newInt(totalcount);
		break;

	    case VL_SELECTED:
		obj = YoixObject.newString(getSelected());
		break;

	    case VL_SELECTEDCOUNT:
		obj = YoixObject.newInt(selectedcount);
		break;

	    case VL_TOTALS:
		obj = getTotals(obj);
		break;

	    case VL_UNSELECTED:
		obj = YoixObject.newString(getUnselected());
		break;
	}

	return(obj);
    }


    final YoixGraphElement
    getGraphElement() {

	return(graphdata);
    }


    final HitBuffer
    getHitBuffer(DataRecord loaded[]) {

	HitBuffer  hits = null;

	//
	// Need some synchronization, but this can't be a synchronized
	// method without risking deadlock.
	//

	synchronized(HITBUFFERLOCK) {
	    if (loaded != null && datarecords == loaded) {
		if (hitbuffer != null && loaded.length <= hitbuffer.getLength()) {
		    hits = hitbuffer;
		    hitbuffer = null;
		} else hits = new HitBuffer(loaded.length);
	    }
	}
	return(hits);
    }


    protected final Object
    getManagedObject() {

	return(null);
    }


    final DataPlot[]
    getPartitionedPlots(int index, int partition) {

	ArrayList  buffer;
	DataPlot   plots[];
	DataPlot   plot;
	int        length;
	int        n;

	if (partition >= 0) {
	    if ((plots = dataplots) != null) {
		buffer = new ArrayList();
		for (n = 0; n < plots.length; n++) {
		    if ((plot = plots[n]) != null) {
			if (plot.getXIndex() == index) {
			    if (plot.getPartitionIndex() != partition)
				buffer.add(plot);
			}
		    }
		}
		plots = null;		// don't accidentally return dataplots!!!
		if ((length = buffer.size()) > 0) {
		    plots = new DataPlot[length];
		    for (n = 0; n < plots.length; n++)
			plots[n] = (DataPlot)buffer.get(n);
		}
	    }
	} else plots = null;

	return(plots);
    }


    final synchronized int
    getPlotMask(int index, int partition) {

	return(getMask("P", index, -1));
    }


    final synchronized String
    getSelected(DataRecord records[], int count, BitSet omit) {

	StringBuffer  buf;
	DataRecord    buffer[];
	DataRecord    record;
	ArrayList     lines;
	String        text;
	int           length;
	int           index;
	int           n;

	if ((text = getData().getString(NL_TEXT)) != null) {
	    if (records != null && count > 0) {
		lines = separateTextLines(text);
		if ((length = lines.size()) > 0) {
		    if (records == datarecords) {
			buffer = new DataRecord[count];
			System.arraycopy(records, 0, buffer, 0, count);
		    } else buffer = records;
		    YoixMiscQsort.sort(buffer, 0);
		    buf = new StringBuffer();
		    for (n = 0; n < buffer.length; n++) {
			if ((record = buffer[n]) != null) {
			    if (record.isSelected() || record.isRequired()) {
				index = record.getID();
				if (index >= 0 && index < length) {
				    if (omit == null || omit.get(index) == false) {
					buf.append((String)lines.get(index));
					buf.append("\n");
				    }
				}
			    }
			}
		    }
		    text = new String(buf);
		} else text = null;
	    } else text = null;
	}

	return(text);
    }


    final int
    getSelectedCount() {

	return(selectedcount);
    }


    final synchronized ArrayList
    getSubordinatePlots(int index) {

	ArrayList  result;
	ArrayList  xaxis;
	ArrayList  yaxis;
	DataPlot   plot;
	int        n;

	xaxis = new ArrayList();
	yaxis = new ArrayList();
	result = new ArrayList();
	result.add(xaxis);
	result.add(yaxis);

	if (dataplots != null) {
	    for (n = 0; n < dataplots.length; n++) {
		if ((plot = dataplots[n]) != null) {
		    if (index == plot.getXIndex())
			xaxis.add(plot);
		    else if (index == plot.getYIndex())
			yaxis.add(plot);
		}
	    }
	}

	return(result);
    }


    final int
    getTagIndex(String name) {

	Object  value;
	int     index;

	if (datatags != null && name != null) {
	    if ((value = datatags.get(name)) != null)
		index = ((Integer)value).intValue();
	    else index = -1;
	} else index = -1;

	return(index);
    }


    final int
    getTagIndex(YoixObject dict, String name, int index) {

	YoixObject  obj;
	Object      value;

	if (datatags != null) {
	    if ((obj = dict.getObject(name)) != null) {
		if (obj.notNull()) {
		    if (obj.isString()) {
			if ((value = datatags.get(obj.stringValue())) != null)
			    index = ((Integer)value).intValue();
			else index = -3;
		    } else index = -2;
		}
	    }
	}

	return(index);
    }


    final int
    getTagPartition(String name) {

	Object  value;
	int     partition = -1;

	if (datapartitions != null && name != null) {
	    if ((value = datapartitions.get(name)) != null)
		partition = ((Integer)value).intValue();
	    else partition = -1;
	} else partition = -1;

	return(partition);
    }


    public final void
    handleRun(Object args[]) {

	if (args != null && args.length > 0) {
	    switch (((Integer)args[0]).intValue()) {
		case COMMAND_DATARECOLOR:
		    handleDataRecolor(
			(DataColorer)args[1],
			false
		    );
		    break;

		case COMMAND_PLOTLOAD:
		    handlePlotLoad((DataPlot)args[1], false);
		    break;

		case COMMAND_PLOTUPDATE:
		    handlePlotUpdate(
			(DataRecord[])args[1],
			(HitBuffer)args[2],
			((Integer)args[3]).intValue(),
			(DataPlot)args[4]
		    );
		    break;

		case COMMAND_SORTRECORDS:
		    handleSortRecords();
		    break;

		case COMMAND_SWEEPFILTERUPDATE:
		    handleSweepFilterUpdate(
			(DataRecord[])args[1],
			(HitBuffer)args[2],
			((Integer)args[3]).intValue(),
			(SweepFilter)args[4]
		    );
		    break;

		case COMMAND_TABLECLEAR:
		    handleTableClear((DataTable)args[1]);
		    break;

		case COMMAND_TABLELOAD:
		    handleTableLoad((DataTable)args[1], false);
		    break;

		case COMMAND_TABLEUPDATE:
		    handleTableUpdate(
			(DataRecord[])args[1],
			(HitBuffer)args[2],
			((Integer)args[3]).intValue(),
			(DataTable)args[4]
		    );
		    break;

		case COMMAND_VIEWERCLEAR:
		    handleViewerClear((DataViewer)args[1]);
		    break;

		case COMMAND_VIEWERLOAD:
		    handleViewerLoad((DataViewer)args[1], false);
		    break;

		case COMMAND_VIEWERRECOLOR:
		    handleViewerRecolor(
			(DataViewer)args[1],
			((Integer)args[2]).intValue(),
			(String)args[3]
		    );
		    break;

		case COMMAND_VIEWERUNCOLOR:
		    handleViewerUncolor(
			(DataViewer)args[1],
			((Integer)args[2]).intValue()
		    );
		    break;

		case COMMAND_VIEWERUPDATE:
		    handleViewerUpdate(
			(DataRecord[])args[1],
			(HitBuffer)args[2],
			((Integer)args[3]).intValue(),
			(DataViewer)args[4]
		    );
		    break;
	    }
	}
    }


    final void
    loadPlot(DataPlot plot) {

	Object  args[];

	args = new Object[] {
	    new Integer(COMMAND_PLOTLOAD),
	    plot
	};

	if (EventQueue.isDispatchThread() == false)
	    EventQueue.invokeLater(new YoixAWTInvocationEvent(this, args));
	else handleRun(args);
    }


    final void
    loadTable(DataTable table) {

	Object  args[];

	args = new Object[] {
	    new Integer(COMMAND_TABLELOAD),
	    table
	};

	if (EventQueue.isDispatchThread() == false)
	    EventQueue.invokeLater(new YoixAWTInvocationEvent(this, args));
	else handleRun(args);
    }


    final void
    loadViewer(DataViewer viewer) {

	Object  args[];

	args = new Object[] {
	    new Integer(COMMAND_VIEWERLOAD),
	    viewer
	};

	if (EventQueue.isDispatchThread() == false)
	    EventQueue.invokeLater(new YoixAWTInvocationEvent(this, args));
	else handleRun(args);
    }


    final void
    recolorData(DataColorer colorer) {

	Object  args[];

	args = new Object[] {
	    new Integer(COMMAND_DATARECOLOR),
	    colorer
	};

	if (EventQueue.isDispatchThread() == false)
	    EventQueue.invokeLater(new YoixAWTInvocationEvent(this, args));
	else handleRun(args);
    }


    final void
    recolorViewer(DataViewer viewer, int primaryindex, String othername) {

	Object  args[];

	args = new Object[] {
	    new Integer(COMMAND_VIEWERRECOLOR),
	    viewer,
	    new Integer(primaryindex),
	    othername
	};

	if (EventQueue.isDispatchThread() == false)
	    EventQueue.invokeLater(new YoixAWTInvocationEvent(this, args));
	else handleRun(args);
    }


    final void
    releaseHitBuffer(HitBuffer hits) {

	DataRecord  records[];

	synchronized(HITBUFFERLOCK) {
	    records = datarecords;		// snapshot - just to be safe
	    if (records != null) {
		if (hits != null) {
		    if (hits.getLength() == records.length) {
			if (hitbuffer != null)
			    hitbuffer.releaseBuffers();
			hitbuffer = hits;
		    }
		}
	    } else if (hitbuffer != null) {
		hitbuffer.releaseBuffers();
		hitbuffer = null;
	    }
	}
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case VL_AFTERCOLOREDBY:
		    setAfterColoredBy(obj);
		    break;

		case VL_AFTERLOAD:
		    setAfterLoad(obj);
		    break;

		case VL_AFTERUPDATE:
		    setAfterUpdate(obj);
		    break;

		case VL_COLOREDBY:
		    setColoredBy(obj);
		    break;

		case VL_DATAFIELDS:
		    setDataFields(obj);
		    break;

		case VL_DATAFILTERS:
		    setDataFilters(obj);
		    break;

		case VL_DATAPLOTS:
		    setDataPlots(obj);
		    break;

		case VL_DATAVIEWERS:
		    setDataViewers(obj);
		    break;

		case VL_DATATABLES:
		    setDataTables(obj);
		    break;

		case VL_DISPOSE:
		    setDispose(obj);
		    break;

		case VL_GRAPHDATA:
		    setGraphData(obj);
		    break;

		case VL_INPUTCOMMENT:
		    setInputComment(obj);
		    break;

		case VL_INPUTFILTER:
		    setInputFilter(obj);
		    break;

		case VL_PLOTFILTERS:		// kludge for an old application
		    setPlotFilters(obj);
		    break;

		case VL_SORTEDBY:
		    setSortedBy(obj);
		    break;

		case VL_SWEEPFILTERS:
		    setSweepFilters(obj);
		    break;

		case VL_TEXT:
		    setText(obj);
		    break;
	    }
	}

	return(obj);
    }


    final void
    setTagIndex(String name, int value) {

	if (datatags != null)
	    datatags.put(name, new Integer(value));
    }


    final void
    setTagPartition(String name, int value) {

	if (datapartitions != null)
	    datapartitions.put(name, new Integer(value));
    }


    final void
    sortRecords(DataPlot sorter) {

	Object  args[];

	if (sorter == sortedby) {
	    args = new Object[] {
		new Integer(COMMAND_SORTRECORDS)
	    };

	    if (EventQueue.isDispatchThread() == false)
		EventQueue.invokeLater(new YoixAWTInvocationEvent(this, args));
	    else handleRun(args);
	}
    }


    final void
    uncolorViewer(DataViewer viewer, int index) {

	Object  args[];

	args = new Object[] {
	    new Integer(COMMAND_VIEWERUNCOLOR),
	    viewer,
	    new Integer(index)
	};

	if (EventQueue.isDispatchThread() == false)
	    EventQueue.invokeLater(new YoixAWTInvocationEvent(this, args));
	else handleRun(args);
    }


    final void
    updateData(DataRecord loaded[], HitBuffer hits, int count, DataPlot source) {

	Object  args[];

	args = new Object[] {
	    new Integer(COMMAND_PLOTUPDATE),
	    loaded,
	    hits,
	    new Integer(count),
	    source
	};

	if (EventQueue.isDispatchThread() == false)
	    EventQueue.invokeLater(new YoixAWTInvocationEvent(this, args));
	else handleRun(args);
    }


    final void
    updateData(DataRecord loaded[], HitBuffer hits, int count, DataTable source) {

	Object  args[];

	args = new Object[] {
	    new Integer(COMMAND_TABLEUPDATE),
	    loaded,
	    hits,
	    new Integer(count),
	    source
	};

	if (EventQueue.isDispatchThread() == false)
	    EventQueue.invokeLater(new YoixAWTInvocationEvent(this, args));
	else handleRun(args);
    }


    final void
    updateData(DataRecord loaded[], HitBuffer hits, int count, DataViewer source) {

	Object  args[];

	args = new Object[] {
	    new Integer(COMMAND_VIEWERUPDATE),
	    loaded,
	    hits,
	    new Integer(count),
	    source
	};

	if (EventQueue.isDispatchThread() == false)
	    EventQueue.invokeLater(new YoixAWTInvocationEvent(this, args));
	else handleRun(args);
    }


    final void
    updateData(DataRecord loaded[], HitBuffer hits, int count, SweepFilter source) {

	Object  args[];

	args = new Object[] {
	    new Integer(COMMAND_SWEEPFILTERUPDATE),
	    loaded,
	    hits,
	    new Integer(count),
	    source
	};

	if (EventQueue.isDispatchThread() == false)
	    EventQueue.invokeLater(new YoixAWTInvocationEvent(this, args));
	else handleRun(args);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    abort(YoixObject dict, String array, int index, String field) {

	VM.abort(dict.defined(field) ? BADVALUE : UNDEFINED, array, index, field);
    }


    private synchronized void
    afterColoredBy() {

	YoixObject  funct;
	YoixObject  argv[];
	Runnable    event;

	if ((funct = aftercoloredby) != null) {
	    if (funct.callable(1))
		argv = new YoixObject[] {coloredby != null ? coloredby.getContext() : YoixObject.newNull()};
	    else argv = new YoixObject[0];
	    event = new YoixAWTInvocationEvent(funct, argv, getContext());
	    EventQueue.invokeLater(event);
	}
    }


    private synchronized void
    afterLoad() {

	YoixObject  funct;
	YoixObject  argv[];
	Runnable    event;

	if ((funct = afterload) != null) {
	    if (funct.callable(1))
		argv = new YoixObject[] {YoixObject.newInt(totalcount)};
	    else argv = new YoixObject[0];
	    event = new YoixAWTInvocationEvent(funct, argv, getContext());
	    EventQueue.invokeLater(event);
	}
    }


    private synchronized void
    afterUpdate() {

	YoixObject  funct;
	YoixObject  argv[];
	Runnable    event;

	if ((funct = afterupdate) != null) {
	    if (funct.callable(1))
		argv = new YoixObject[] {YoixObject.newInt(selectedcount)};
	    else argv = new YoixObject[0];
	    event = new YoixAWTInvocationEvent(funct, argv, getContext());
	    EventQueue.invokeLater(event);
	}
    }


    private synchronized void
    appendData(ArrayList records) {

	DataRecord  array[];
	Object      fields[];
	int         count;
	int         offset;
	int         m;
	int         n;

	//
	// Older version did
	//
	//     if (sortedby != null)
	//         sortedby.sortRecords(datarecords, sortedby, this);
	//
	// but EventPlots are now responsible and should call
	//
	//    datamanager.sortRecords(this);
	//
	// when necessary. Request can be queued up because results will
	// still be correct, but just a bit slower, if the EvenPlot hasn't
	// taken control of datarecords[]. Changed on 6/27/06.
	// 

	if (datarecords != null) {
	    if (disposed == false && records != null && records.size() > 0) {
		count = records.size();
		offset = datarecords.length;
		array = new DataRecord[offset + count];
		System.arraycopy(datarecords, 0, array, 0, offset);
		datarecords = array;

		totalcount = datarecords.length;
		selectedcount += count;

		for (m = 0, n = offset; n < totalcount; m++, n++) {
		    fields = (Object [])records.get(m);
		    datarecords[n] = new DataRecord(fields, n);
		}

		if (coloredby != null) {
		    coloredby.appendRecords(datarecords, offset);
		    handleDataRecolor(coloredby, true);
		}
		if (datafilters != null) {
		    for (n = 0; n < datafilters.length; n++) {
			if (datafilters[n] != coloredby)
			    datafilters[n].appendRecords(datarecords, offset);
		    }
		}
		if (dataplots != null) {
		    for (n = 0; n < dataplots.length; n++)
			dataplots[n].appendRecords(datarecords, offset);
		}
		if (dataviewers != null) {
		    for (n = 0; n < dataviewers.length; n++) {
			if (dataviewers[n] != coloredby)
			    dataviewers[n].appendRecords(datarecords, offset);
		    }
		}
		if (datatables != null) {
		    for (n = 0; n < datatables.length; n++)
			datatables[n].appendRecords(datarecords, offset);
		}
		if (sweepfilters != null) {
		    for (n = 0; n < sweepfilters.length; n++)
			sweepfilters[n].appendRecords(datarecords, offset);
		}
		loadCounters();
		afterLoad();
	    }
	} else loadData(records);
    }


    private void
    buildDataManager() {

	buildMaskStack();

	setField(NL_AFTERCOLOREDBY);
	setField(NL_AFTERLOAD);
	setField(NL_AFTERUPDATE);
	setField(NL_DATAFIELDS);
	setField(NL_DISPOSE);
	setField(NL_INPUTCOMMENT);
	setField(NL_INPUTFILTER);
	setField(NL_OUTPUTFILTER);
	setField(NL_DATAFILTERS);
	setField(NL_DATAPLOTS);
	setField(NL_DATAVIEWERS);
	setField(NL_DATATABLES);
	setField(NL_PLOTFILTERS);
	setField(NL_SWEEPFILTERS);
	setField(NL_GRAPHDATA);
	setField(NL_TEXT);

	startState(STATE_IDLE);
    }


    private synchronized void
    buildMaskStack() {

	int  masks[];
	int  n;

	masks = DataRecord.getMasks();
	maskmap = new Hashtable();
	maskstack = new Vector(masks.length);
	for (n = 0; n < masks.length; n++)
	    maskstack.addElement(new int[] {0, masks[n]});
    }


    private synchronized YoixObject
    builtinAppendText(String name, YoixObject arg[]) {

	ArrayList   records;
	String      text;
	char        buf[];

	if (arg.length == 1) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if (arg[0].notNull()) {
		    if ((buf = arg[0].toCharArray(true, null)) != null)
			records = separateRecords(buf, new ArrayList());
		    else records = separateRecords(arg[0].stringValue(), new ArrayList());
		    if (records.size() > 0) {
			appendData(records);
			if ((text = getData().getString(NL_TEXT)) != null) {
			    if (text.endsWith("\n") == false)
				text += "\n";
			    if (buf != null)
				text += new String(buf);
			    else text += arg[0].stringValue();
			    getData().putString(NL_TEXT, text);
			}
		    }
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinGetFields(String name, YoixObject arg[]) {

	DataRecord    record;
	YoixObject    result = null;
	ArrayList     matches;
	boolean       selected;
	boolean       any;
	String        key;
	String        field;
	int           length;
	int           index;
	int           target;
	int           count;
	int           n;

	//
	// Decided to use getID() so the current datarecords sorting has
	// no influence on the results. Starting from target instead of 0
	// when we're looking for one record is probably also reasonable.
	//

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 0 || arg[0].isInteger()) {
		if (datarecords != null && (length = datarecords.length) > 0) {
		    target = (arg.length == 1) ? arg[0].intValue() : 0;
		    if (target >= 0 && target < length) {
			if (arg.length == 0)
			    result = YoixObject.newArray(length);
			else result = null;
			for (count = 0, n = target; count < length; count++, n++) {
			    record = datarecords[n%length];
			    if (result != null || record.getID() == target) {
				if (result == null) {
				    result = record.getFields();
				    break;
				} else result.put(record.getID(), record.getFields(), false);
			    }
			}
		    }
		}
	    } else VM.badArgument(name, 0);
	} else if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isString()) {
		if (arg[1].isInteger()) {
		    if (arg.length == 2 || arg[2].isInteger()) {
			if (datarecords != null && (length = datarecords.length) > 0) {
			    key = arg[0].stringValue();
			    index = arg[1].intValue();
			    if (arg.length == 3) {
				any = false;
				selected = arg[2].booleanValue();
			    } else {
				any = true;
				selected = true;		// unused
			    }
			    if (key.length() >= 0 && index >= 0) {
				matches = new ArrayList();
				for (n = 0; n < length; n++) {
				    record = datarecords[n];
				    if ((field = record.getField(index)) != null) {
					if (key.equals(field)) {
					    if (any || record.isSelected() == selected)
						matches.add(record.getFields());
					}
				    }
				}
				result = YoixMisc.copyIntoArray(matches, true, matches);
			    }
			}
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(result == null ? YoixObject.newArray() : result);
    }


    private synchronized YoixObject
    builtinGetIndex(String name, YoixObject arg[]) {

	DataRecord    record;
	String        key;
	String        field;
	int           position = -1;
	int           length;
	int           count;
	int           start;
	int           index;
	int           incr;
	int           n;

	if (arg.length == 2 || arg.length == 3 || arg.length == 4) {
	    if (arg[0].isString()) {
		if (arg[1].isInteger()) {
		    if (arg.length <= 2 || arg[2].isInteger()) {
			if (arg.length <= 3 || arg[3].isInteger()) {
			    if (datarecords != null && (length = datarecords.length) > 0) {
				key = arg[0].stringValue();
				index = arg[1].intValue();
				start = (arg.length > 2) ? arg[2].intValue() : 0;
				if (arg.length > 3)
				    incr = arg[3].intValue() >= 0 ? 1 : -1;
				else incr = 1;
				if (key.length() >= 0 && index >= 0) {
				    for (count = 0, n = start; count < length; count++, n += incr) {
					if (n < 0)
					    n = (incr < 0) ? length - 1 : 0;
					record = datarecords[n%length];
					if ((field = record.getField(index)) != null) {
					    if (key.equals(field)) {
						position = n%length;
						break;
					    }
					}
				    }
				}
			    }
			} else VM.badArgument(name, 3);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newInt(position));
    }


    private synchronized YoixObject
    builtinGetIndices(String name, YoixObject arg[]) {

	DataRecord    record;
	YoixObject    result = null;
	ArrayList     matches;
	boolean       selected;
	boolean       any;
	String        key;
	String        field;
	int           length;
	int           index;
	int           n;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isString()) {
		if (arg[1].isInteger()) {
		    if (arg.length == 2 || arg[2].isInteger()) {
			if (datarecords != null && (length = datarecords.length) > 0) {
			    key = arg[0].stringValue();
			    index = arg[1].intValue();
			    if (arg.length == 3) {
				any = false;
				selected = arg[2].booleanValue();
			    } else {
				any = true;
				selected = true;		// unused
			    }
			    if (key.length() >= 0 && index >= 0) {
				matches = new ArrayList();
				for (n = 0; n < length; n++) {
				    record = datarecords[n];
				    if ((field = record.getField(index)) != null) {
					if (key.equals(field)) {
					    if (any || record.isSelected() == selected)
						matches.add(new Integer(record.getID()));
					}
				    }
				}
				result = YoixMisc.copyIntoArray(matches, true, matches);
			    }
			}
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(result == null ? YoixObject.newArray() : result);
    }


    private YoixObject
    builtinGetState(String name, YoixObject arg[]) {

	YoixObject  obj;
	long        time = System.currentTimeMillis();

	obj = YoixObject.newArray(6);
	obj.putInt(0, currentstate);
	obj.putString(1, STATENAME[currentstate]);
	obj.putDouble(2, (time - statetimer[currentstate])/1000.0);
	obj.putString(3, stateargument[currentstate]);
	obj.putDouble(4, stateposition[currentstate]);
	obj.putDouble(5, statetotal[currentstate]);

	return(obj);
    }


    private synchronized YoixObject
    builtinGetValues(String name, YoixObject arg[]) {

	DataRecord    record;
	YoixObject    result = null;
	ArrayList     matches;
	HashMap       included;
	boolean       selected;
	boolean       any;
	String        key;
	String        field;
	String        value;
	int           length;
	int           index;
	int           target;
	int           count;
	int           n;

	//
	// Decided to use getID() so the current datarecords sorting has
	// no influence on the results. Starting from target instead of 0
	// when we're looking for one record is probably also reasonable.
	//

	if (arg.length == 3 || arg.length == 4) {
	    if (arg[0].isString()) {
		if (arg[1].isInteger()) {
		    if (arg[2].isInteger()) {
			if (arg.length == 3 || arg[3].isInteger()) {
			    if (datarecords != null && (length = datarecords.length) > 0) {
				key = arg[0].stringValue();
				index = arg[1].intValue();
				target = arg[2].intValue();
				if (arg.length == 4) {
				    any = false;
				    selected = arg[3].booleanValue();
				} else {
				    any = true;
				    selected = true;		// unused
				}
				if (key.length() >= 0 && index >= 0) {
				    matches = new ArrayList();
				    included = new HashMap();
				    for (n = 0; n < length; n++) {
					record = datarecords[n];
					if ((field = record.getField(index)) != null) {
					    if (key.equals(field)) {
						if (any || record.isSelected() == selected) {
						    if ((value = record.getField(target)) != null) {
							if (included.containsKey(value) == false) {
							    matches.add(value);
							    included.put(value, Boolean.TRUE);
							}
						    }
						}
					    }
					}
				    }
				    result = YoixMisc.copyIntoArray(matches, true, matches);
				}
			    }
			} else VM.badArgument(name, 3);
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(result == null ? YoixObject.newArray() : result);
    }


    private synchronized void
    clearData() {

	int  n;

	if (datarecords != null || totalcount > 0) {
	    datarecords = null;
	    totalcount = 0;
	    selectedcount = 0;
	    countervalues = null;
	    countertotals = null;

	    if (datafilters != null) {
		for (n = 0; n < datafilters.length; n++)
		    datafilters[n].loadRecords(null, null, true);
	    }
	    if (dataplots != null) {
		for (n = 0; n < dataplots.length; n++)
		    dataplots[n].loadRecords(null, null, true);
	    }
	    if (dataviewers != null) {
		for (n = 0; n < dataviewers.length; n++)
		    dataviewers[n].loadRecords(null, null, true);
	    }
	    if (datatables != null) {
		for (n = 0; n < datatables.length; n++)
		    datatables[n].loadRecords(null, null, true);
	    }
	    if (sweepfilters != null) {
		for (n = 0; n < sweepfilters.length; n++)
		    sweepfilters[n].loadRecords(null, null, true);
	    }
	}
    }


    private synchronized int
    collectRecords(HitBuffer hits, int masks[], boolean selected) {

	DataRecord  record;
	int         length;
	int         count;
	int         n;

	//
	// Old code used selected and when it was false every record
	// that was not initially selected by masks was added to hits.
	// Seemed questionable, but we could easily be wrong. In any
	// case normal applications don't get here with selected set
	// to false, so the change probably won't affect any existing
	// applications. Change was made when we added the HitBuffer
	// class, which happened around 9/8/04.
	//

	length = datarecords.length;
	count = 0;

	for (n = 0; n < length; n++) {
	    record = datarecords[n];
	    if (record.notSelected(masks)) {
		record.setSelected(masks);
		if (record.isSelected())
		    hits.setRecord(count++, record, true);
	    }
	}
	return(count);
    }


    private void
    dispose() {

	if (disposed == false) {
	    clearData();
	    removeDataFilters();
	    removeDataPlots();
	    removeDataViewers();
	    removeDataTables();
	    removeSweepFilters();
	    datarecords = null;
	    disposed = true;
	}
    }


    private synchronized YoixObject
    getCounters(YoixObject obj) {

	int  length;
	int  n;

	if (countervalues != null && countertags != null) {
	    length = Math.min(countervalues.length, countertags.length);
	    obj = YoixObject.newDictionary(length);
	    for (n = 0; n < length; n++)
		obj.putDouble(countertags[n], countervalues[n]);
	} else obj = YoixObject.newDictionary();

	return(obj);
    }


    private synchronized int
    getDataMask(int index, int partition) {

	return(getMask("D", index, partition));
    }


    private synchronized int[]
    getDataMasks(int indices[], int count, int partitions[]) {

	int  masks[] = null;
	int  mask;
	int  n;

	if (indices != null) {
	    masks = new int[Math.min(indices.length, count)];
	    for (n = 0; n < masks.length; n++) {
		if ((mask = getMask("D", indices[n], partitions[n])) == 0) {
		    //
		    // Old code set masks to null here, which could result
		    // in NullPointerExceptions later on. We added a quick
		    // fix that eliminates the exception, however the real
		    // answer is to allow reuse of an index (probably in
		    // getMask()) when there's no partition. Change could
		    // affect existing applications, so we're going with
		    // the preventative change until we have time to make
		    // and test better fixes.
		    //
		    masks = new int[0];		// prevents NullPointerException
		    break;
		} else masks[n] = mask;
	    }
	}

	return(masks);
    }


    private ArrayList
    getFieldData(YoixObject dict, String name) {

	YoixObject  obj;
	YoixObject  element;
	ArrayList   data = null;
	boolean     valid;
	int         indices[];
	int         partitions[];
	int         tmp[];
	int         active;
	int         index;
	int         incr;
	int         length;
	int         m;
	int         n;

	if ((obj = dict.getObject(name)) != null) {
	    if (obj.isArray() && obj.sizeof() > 0) {
		length = obj.length();
		indices = new int[obj.sizeof()];
		partitions = new int[indices.length];
		active = 0;
		incr = 1;
		valid = true;
		for (n = obj.offset(), m = 0; valid && n < length; n++) {
		    if ((element = obj.getObject(n)) != null) {
			if (element.isString()) {
			    name = element.stringValue();
			    if ((index = getTagIndex(name)) >= 0) {
				partitions[m] = getTagPartition(name);
				indices[m++] = index;
				active += incr;
			    } else valid = false;
			} else if (element.isNull() && incr != 0) {
			    incr = 0;
			    tmp = new int[indices.length - 1];
			    System.arraycopy(indices, 0, tmp, 0, m);
			    indices = tmp;
			    tmp = new int[indices.length];
			    System.arraycopy(partitions, 0, tmp, 0, m);
			    partitions = tmp;
			} else {
			    //
			    // Changed on 5/12/07 - at this point the old
			    // version just set valid to false. Change was
			    // made to accommodate sweepfiltering graphs,
			    // but it needs to be carefully checked!!
			    //
			    partitions[m] = getTagPartition(null);
			    indices[m++] = -1;
			}
		    } else valid = false;
		}
		if (valid) {
		    data = new ArrayList();
		    data.add(indices);
		    data.add(partitions);
		    data.add(new Integer(active));
		}
	    } else if (obj.isString()) {
		name = obj.stringValue();
		if ((index = getTagIndex(name)) >= 0) {
		    data = new ArrayList();
		    data.add(new int[] {index});
		    data.add(new int[] {getTagPartition(name)});
		    data.add(new Integer(1));
		}
	    }
	}

	return(data);
    }


    private int[]
    getIndexArray(YoixObject dict, String name) {

	YoixObject  obj;
	YoixObject  element;
	int         indices[] = null;
	int         index;
	int         length;
	int         m;
	int         n;

	if ((obj = dict.getObject(name)) != null) {
	    if (obj.isArray() && obj.sizeof() > 0) {
		length = obj.length();
		indices = new int[obj.sizeof()];
		for (n = obj.offset(), m = 0; n < length && indices != null; n++) {
		    if ((element = obj.getObject(n)) != null) {
			if (element.isString()) {
			    name = element.stringValue();
			    if ((index = getTagIndex(name)) >= 0)
				indices[m++] = index;
			    else indices = null;
			} else if (element.isNull())
			    indices[m++] = -1;
			else indices = null;
		    } else indices = null;
		}
	    } else if (obj.isString() && obj.notNull()) {
		name = obj.stringValue();
		if ((index = getTagIndex(name)) >= 0)
		    indices = new int[] {index};
	    } else if (obj.isNull())
		indices = new int[] {-1};
	}

	return(indices);
    }


    private synchronized int
    getMask(String prefix, int index, int partition) {

	String  name;
	int     value[];
	int     mask = 0;

	//
	// Checking to make sure index is nonnegative was added on 5/12/07
	// as a precaution because the graph sweepfiltering changes mean
	// negative indices can end up in the indices array that's built
	// by getFieldData().
	//

	if (index >= 0) {
	    name = prefix + (partition >= 0 ? "P:" : ":") + index;
	    if ((value = (int[])maskmap.get(name)) == null) {
		if (maskstack.size() > 0) {
		    value = (int[])maskstack.elementAt(0);
		    maskstack.removeElementAt(0);
		    maskmap.put(name, value);
		    value[0] = 1;
		    mask = value[1];
		}
	    } else if (partition >= 0) {
		value[0]++;
		mask = value[1];
	    }
	}

	return(mask);
    }


    private synchronized String
    getSelected() {

	return(datarecords != null
	    ? getSelected(datarecords, datarecords.length, null)
	    : null
	);
    }


    private synchronized YoixObject
    getTotals(YoixObject obj) {

	int  length;
	int  n;

	if (countertotals != null && countertags != null) {
	    length = Math.min(countertotals.length, countertags.length);
	    obj = YoixObject.newDictionary(length);
	    for (n = 0; n < length; n++)
		obj.putDouble(countertags[n], countertotals[n]);
	} else obj = YoixObject.newDictionary();

	return(obj);
    }


    private synchronized String
    getUnselected() {

	StringBuffer  buf;
	DataRecord    buffer[];
	DataRecord    record;
	ArrayList     lines;
	String        text;
	int           length;
	int           index;
	int           n;

	if ((text = getData().getString(NL_TEXT)) != null) {
	    if (datarecords != null && datarecords.length > 0) {
		lines = separateTextLines(text);
		if ((length = lines.size()) > 0) {
		    buffer = new DataRecord[datarecords.length];
		    System.arraycopy(datarecords, 0, buffer, 0, datarecords.length);
		    YoixMiscQsort.sort(buffer, 0);
		    buf = new StringBuffer();
		    for (n = 0; n < buffer.length; n++) {
			if ((record = buffer[n]) != null) {
			    if (record.notSelected() || record.isRequired()) {
				index = record.getID();
				if (index >= 0 && index < length) {
				    buf.append((String)lines.get(index));
				    buf.append("\n");
				}
			    }
			}
		    }
		    text = new String(buf);
		} else text = null;
	    } else text = null;
	}

	return(text);
    }


    private synchronized void
    handleDataRecolor(DataColorer colorer, boolean loading) {

	int  length;
	int  n;

	if (colorer == coloredby) {
	    if (datarecords != null) {
		length = datarecords.length;
		if (colorer != null) {
		    colorer.loadRecords(datarecords, datarecords);
		    colorer.loadColors();
		    for (n = 0; n < length; n++)
			datarecords[n].setColor(colorer.getColor(datarecords[n]));
		} else {
		    for (n = 0; n < length; n++)
			datarecords[n].setColor(null);
		}
		if (loading == false) {
		    if (dataplots != null) {
			for (n = 0; n < dataplots.length; n++)
			    dataplots[n].recolorData();
		    }
		    if (dataviewers != null) {
			for (n = 0; n < dataviewers.length; n++) {
			    if (dataviewers[n] != colorer)
				dataviewers[n].recolorViewer();
			}
		    }
		    if (datafilters != null) {
			for (n = 0; n < datafilters.length; n++) {
			    if (datafilters[n] != colorer || datafilters[n].getStackMode())
				datafilters[n].recolorViewer();
			}
		    }
		    if (datatables != null) {
			for (n = 0; n < datatables.length; n++)
			    datatables[n].recolorTable();
		    }
		    if (sweepfilters != null) {
			for (n = 0; n < sweepfilters.length; n++) {
			    if (sweepfilters[n] != colorer)
				sweepfilters[n].recolorSweepFilter();
			}
		    }
		}
		afterColoredBy();
	    }
	}
    }


    private synchronized void
    handlePlotLoad(DataPlot plot, boolean force) {

	//
	// Old version called
	//
	//     plot.sortRecords(datarecords, sortedby, this);
	//
	// after the records were loaded, but plot is now responsible and
	// should calls
	//
	//     datamanager.sortRecords();
	//
	// whenever datarecords need to be sorted.
	//

	if (plot != null && datarecords != null) {
	    if (plot.isManagedBy(this))
		plot.loadRecords(datarecords, datarecords, force);
	}
    }


    private synchronized void
    handlePlotUpdate(DataRecord loaded[], HitBuffer hits, int count, DataPlot source) {

	int  length;
	int  n;

	if (hits != null && datarecords != null && loaded == datarecords) {
	    if (source == null || source.isManagedBy(this)) {
		if (count > 0) {
		    if (source != null)
			source.updatePlot(loaded, hits, count, sortedby);
		    if (dataplots != null) {
			for (n = 0; n < dataplots.length; n++) {
			    if (dataplots[n] != source)
				dataplots[n].updatePlot(loaded, hits, count, sortedby);
			}
		    }
		    if (dataviewers != null) {
			length = dataviewers.length;
			for (n = 0; n < length; n++)
			    dataviewers[n].updateViewer(loaded, hits, count);
			for (n = 0; n < length; n++)
			    dataviewers[n].repaintViewer(count);
		    }
		    if (datafilters != null) {
			length = datafilters.length;
			for (n = 0; n < length; n++)
			    datafilters[n].updateViewer(loaded, hits, count);
			for (n = 0; n < length; n++)
			    datafilters[n].repaintViewer(count);
		    }
		    if (datatables != null) {
			length = datatables.length;
			for (n = 0; n < length; n++)
			    datatables[n].updateTable(loaded, hits, count);
			for (n = 0; n < length; n++)
			    datatables[n].repaintTable(count);
		    }
		    if (sweepfilters != null) {
			length = sweepfilters.length;
			for (n = 0; n < length; n++)
			    sweepfilters[n].updateSweepFilter(loaded, hits, count);
			for (n = 0; n < length; n++)
			    sweepfilters[n].repaintSweepFilter(count);
		    }
		    updateCounters(hits, count);
		    afterUpdate();
		}
	    }
	}

	releaseHitBuffer(hits);
    }


    final synchronized void
    handleSortRecords() {

	int  n;

	//
	// First tells everyone to invalidate internal data structures
	// that may depend on the sorting of datarecords[] and then lets
	// sortedby sort datarecords[] as it wants. EventPlots currently
	// are the only objects that are affected, but we tell everyone
	// anyway because we don't want to make any assumptions here.
	//

	if (dataplots != null) {
	    for (n = 0; n < dataplots.length; n++) {
		if (dataplots[n] != sortedby)
		    dataplots[n].sortRecords(datarecords, sortedby, this);
	    }
	}
	if (dataviewers != null) {
	    for (n = 0; n < dataviewers.length; n++)
		dataviewers[n].recordsSorted(datarecords);
	}
	if (datatables != null) {
	    for (n = 0; n < datatables.length; n++)
		datatables[n].recordsSorted(datarecords);
	}
	if (datafilters != null) {
	    for (n = 0; n < datafilters.length; n++)
		datafilters[n].recordsSorted(datarecords);
	}
	if (sweepfilters != null) {
	    for (n = 0; n < sweepfilters.length; n++)
		sweepfilters[n].recordsSorted(null);
	}
	if (sortedby != null)
	    sortedby.sortRecords(datarecords, sortedby, this);
    }


    private synchronized void
    handleSweepFilterUpdate(DataRecord loaded[], HitBuffer hits, int count, SweepFilter source) {

	int  length;
	int  n;

	if (hits != null && datarecords != null && loaded == datarecords) {
	    if (source == null || source.isManagedBy(this)) {
		if (count > 0) {
		    if (source != null) {
			source.updateSweepFilter(loaded, hits, count);
			source.repaintSweepFilter(count);
		    }
		    if (dataplots != null) {
			for (n = 0; n < dataplots.length; n++)
			    dataplots[n].updatePlot(loaded, hits, count, sortedby);
		    }
		    if (dataviewers != null) {
			length = dataviewers.length;
			for (n = 0; n < length; n++)
			    dataviewers[n].updateViewer(loaded, hits, count);
			for (n = 0; n < length; n++)
			    dataviewers[n].repaintViewer(count);
		    }
		    if (datafilters != null) {
			length = datafilters.length;
			for (n = 0; n < length; n++)
			    datafilters[n].updateViewer(loaded, hits, count);
			for (n = 0; n < length; n++)
			    datafilters[n].repaintViewer(count);
		    }
		    if (datatables != null) {
			length = datatables.length;
			for (n = 0; n < length; n++)
			    datatables[n].updateTable(loaded, hits, count);
			for (n = 0; n < length; n++)
			    datatables[n].repaintTable(count);
		    }
		    if (sweepfilters != null) {
			length = sweepfilters.length;
			for (n = 0; n < length; n++) {
			    if (sweepfilters[n] != source)
				sweepfilters[n].updateSweepFilter(loaded, hits, count);
			}
			for (n = 0; n < length; n++) {
			    if (sweepfilters[n] != source)
				sweepfilters[n].repaintSweepFilter(count);
			}
		    }
		    updateCounters(hits, count);
		    afterUpdate();
		} else if (source != null)
		    source.repaintSweepFilter();	// questionable - check later??
	    }
	}

	releaseHitBuffer(hits);
    }


    private synchronized void
    handleTableClear(DataTable table) {

	HitBuffer  hits;
	int        masks[];
	int        count;

	if (table != null && datarecords != null) {
	    if (table.isManagedBy(this)) {
		if ((masks = table.getSelectMasks()) != null) {
		    if ((hits = getHitBuffer(datarecords)) != null) {
			if ((count = collectRecords(hits, masks, true)) > 0) {
			    updateData(datarecords, hits, count, table);
			    Thread.yield();
			}
			releaseHitBuffer(hits);
		    }
		    table.loadRecords(null, null, false);
		}
	    }
	}
    }


    private synchronized void
    handleTableLoad(DataTable table, boolean force) {

	if (table != null && datarecords != null) {
	    if (table.isManagedBy(this))
		table.loadRecords(datarecords, datarecords, force);
	}
    }


    private synchronized void
    handleTableUpdate(DataRecord loaded[], HitBuffer hits, int count, DataTable source) {

	int  length;
	int  n;

	if (hits != null && datarecords != null && loaded == datarecords) {
	    if (source == null || source.isManagedBy(this)) {
		if (count > 0) {
		    if (source != null) {
			source.updateTable(loaded, hits, count);
			source.repaintTable(count);
		    }
		    if (dataplots != null) {
			for (n = 0; n < dataplots.length; n++)
			    dataplots[n].updatePlot(loaded, hits, count, sortedby);
		    }
		    if (dataviewers != null) {
			length = dataviewers.length;
			for (n = 0; n < length; n++)
			    dataviewers[n].updateViewer(loaded, hits, count);
			for (n = 0; n < length; n++)
			    dataviewers[n].repaintViewer(count);
		    }
		    if (datafilters != null) {
			length = datafilters.length;
			for (n = 0; n < length; n++)
			    datafilters[n].updateViewer(loaded, hits, count);
			for (n = 0; n < length; n++)
			    datafilters[n].repaintViewer(count);
		    }
		    if (datatables != null) {
			length = datatables.length;
			for (n = 0; n < length; n++) {
			    if (datatables[n] != source)
				datatables[n].updateTable(loaded, hits, count);
			}
			for (n = 0; n < length; n++) {
			    if (datatables[n] != source)
				datatables[n].repaintTable(count);
			}
		    }
		    if (sweepfilters != null) {
			length = sweepfilters.length;
			for (n = 0; n < length; n++) {
			    if (sweepfilters[n] != source)
				sweepfilters[n].updateSweepFilter(loaded, hits, count);
			}
			for (n = 0; n < length; n++) {
			    if (sweepfilters[n] != source)
				sweepfilters[n].repaintSweepFilter(count);
			}
		    }
		    updateCounters(hits, count);
		    afterUpdate();
		} else if (source != null)
		    source.repaintTable();	// questionable - check later??
	    }
	}

	releaseHitBuffer(hits);
    }


    private synchronized void
    handleViewerClear(DataViewer viewer) {

	HitBuffer  hits;
	int        masks[];
	int        count;

	if (viewer != null && datarecords != null) {
	    if (viewer.isManagedBy(this)) {
		if ((masks = viewer.getSelectMasks()) != null) {
		    if ((hits = getHitBuffer(datarecords)) != null) {
			if ((count = collectRecords(hits, masks, true)) > 0) {
			    updateData(datarecords, hits, count, viewer);
			    Thread.yield();
			}
			releaseHitBuffer(hits);
		    }
		    viewer.loadRecords(null, null, false);
		    if (viewer == coloredby)
			handleDataRecolor(coloredby, false);
		}
	    }
	}
    }


    private synchronized void
    handleViewerLoad(DataViewer viewer, boolean force) {

	if (viewer != null && datarecords != null) {
	    if (viewer.isManagedBy(this)) {
		viewer.loadRecords(datarecords, datarecords, force);
		if (viewer == coloredby)
		    handleDataRecolor(coloredby, false);
	    }
	}
    }


    private synchronized void
    handleViewerRecolor(DataViewer viewer, int index, String othername) {

	DataRecord  record;
	boolean     reload;
	Object      value;
	String      name;
	String      key;
	int         length;
	int         n;

	//
	// The isRequired() test was added on 5/15/07 when recoloring was
	// extended to GraphPlots.
	//

	if (viewer != null && datarecords != null) {
	    reload = false;
	    length = datarecords.length;
	    for (n = 0; n < length; n++) {
		record = datarecords[n];
		if (record.isRequired() == false) {		// added for GraphPlots
		    if ((name = record.getField(index)) != null) {
			if (viewer.isSelected(name) == false) {
			    key = record.getKey(index);
			    if (recolored.get(key) == null) {
				recolored.put(key, record.changeField(index, othername));
				reload = true;
			    }
			} else if (name == othername) {
			    if (record.isSelected()) {
				key = record.getKey(index);
				if ((value = recolored.get(key)) != null) {
				    record.changeField(index, value);
				    recolored.remove(key);
				    reload = true;
				}
			    }
			}
		    }
		}
	    }
	    if (reload) {
		viewer.loadRecords(datarecords, datarecords, false);
		if (coloredby != null)
		    handleDataRecolor(coloredby, false);
	    }
	}
    }


    private synchronized void
    handleViewerUncolor(DataViewer viewer, int index) {

	DataRecord  record;
	boolean     reload;
	String      key;
	Object      value;
	int         length;
	int         n;

	if (viewer != null && datarecords != null) {
	    reload = false;
	    length = datarecords.length;
	    for (n = 0; n < length; n++) {
		record = datarecords[n];
		key = record.getKey(index);
		if ((value = recolored.get(key)) != null) {
		    record.changeField(index, value);
		    recolored.remove(key);
		    reload = true;
		}
	    }
	    if (reload) {
		viewer.loadRecords(datarecords, datarecords, false);
		if (coloredby != null)
		    handleDataRecolor(coloredby, false);
	    }
	}
    }


    private synchronized void
    handleViewerUpdate(DataRecord loaded[], HitBuffer hits, int count, DataViewer source) {

	int  length;
	int  n;

	if (hits != null && datarecords != null && loaded == datarecords) {
	    if (source == null || source.isManagedBy(this)) {
		if (count > 0) {
		    if (source != null) {
			source.updateViewer(loaded, hits, count);
			source.repaintViewer(count);
		    }
		    if (dataplots != null) {
			for (n = 0; n < dataplots.length; n++)
			    dataplots[n].updatePlot(loaded, hits, count, sortedby);
		    }
		    if (dataviewers != null) {
			length = dataviewers.length;
			for (n = 0; n < length; n++) {
			    if (dataviewers[n] != source)
				dataviewers[n].updateViewer(loaded, hits, count);
			}
			for (n = 0; n < length; n++) {
			    if (dataviewers[n] != source)
				dataviewers[n].repaintViewer(count);
			}
		    }
		    if (datafilters != null) {
			length = datafilters.length;
			for (n = 0; n < length; n++) {
			    if (datafilters[n] != source)
				datafilters[n].updateViewer(loaded, hits, count);
			}
			for (n = 0; n < length; n++) {
			    if (datafilters[n] != source)
				datafilters[n].repaintViewer(count);
			}
		    }
		    if (datatables != null) {
			length = datatables.length;
			for (n = 0; n < length; n++)
			    datatables[n].updateTable(loaded, hits, count);
			for (n = 0; n < length; n++)
			    datatables[n].repaintTable(count);
		    }
		    if (sweepfilters != null) {
			length = sweepfilters.length;
			for (n = 0; n < length; n++) {
			    if (sweepfilters[n] != source)
				sweepfilters[n].updateSweepFilter(loaded, hits, count);
			}
			for (n = 0; n < length; n++) {
			    if (sweepfilters[n] != source)
				sweepfilters[n].repaintSweepFilter(count);
			}
		    }
		    updateCounters(hits, count);
		    afterUpdate();
		} else if (source != null)
		    source.repaintViewer();	// questionable - check later??
	    }
	}

	releaseHitBuffer(hits);
    }


    private synchronized void
    loadCounters() {

	DataRecord  record;
	double      value;
	int         count;
	int         length;
	int         m;
	int         n;

	countervalues = null;
	countertotals = null;

	if (counterindices != null && datarecords != null) {
	    if (counterindices.length > 0 && datarecords.length > 0) {
		count = counterindices.length;
		length = datarecords.length;
		countertotals = new double[count];
		countervalues = new double[count];
		for (n = 0; n < length; n++) {
		    record = datarecords[n];
		    for (m = 0; m < count; m++) {
			value = record.getValue(counterindices[m]);
			if (Double.isNaN(value) == false)
			    countertotals[m] += value;
		    }
		}
		for (m = 0; m < count; m++)
		    countervalues[m] = countertotals[m];
	    }
	}
    }


    private synchronized void
    loadData(ArrayList records) {

	Object  fields[];
	int     loadingstates;
	int     n;

	//
	// Older version did
	//
	//     if (sortedby != null) {
	//         startState(STATE_SORTING);
	//         sortedby.sortRecords(datarecords, sortedby, this);
	//     }
	//
	// but EventPlots are now responsible and should call
	//
	//    datamanager.sortRecords(this);
	//
	// when necessary. Request can be queued up because results will
	// still be correct, but just a bit slower, if the EvenPlot hasn't
	// taken control of datarecords[]. Changed on 6/27/06.
	// 

	datarecords = null;
	totalcount = 0;
	selectedcount = 0;

	if (disposed == false && records != null) {
	    datarecords = new DataRecord[records.size()];
	    recolored = new Hashtable();
	    totalcount = datarecords.length;
	    selectedcount = totalcount;
	    startState(STATE_BUILDING_RECORDS, totalcount);
	    for (n = 0; n < totalcount; n++) {
		fields = (Object [])records.get(n);
		datarecords[n] = new DataRecord(fields, n);
		stateposition[STATE_BUILDING_RECORDS] = n;
	    }

	    loadingstates = (datafilters != null) ? datafilters.length : 0;
	    loadingstates += (dataplots != null) ? dataplots.length : 0;
	    loadingstates += (dataviewers != null) ? dataviewers.length : 0;
	    loadingstates += (datatables != null) ? datatables.length : 0;
	    loadingstates += (sweepfilters != null) ? sweepfilters.length : 0;
	    startState(STATE_LOADING_COMPONENTS, loadingstates);

	    if (coloredby != null) {
		stateargument[STATE_LOADING_COMPONENTS] = "coloredby";
		coloredby.loadRecords(datarecords, datarecords, false);
		handleDataRecolor(coloredby, true);
		stateposition[STATE_LOADING_COMPONENTS]++;
	    }
	    if (datafilters != null) {
		stateargument[STATE_LOADING_COMPONENTS] = "datafilters";
		for (n = 0; n < datafilters.length; n++) {
		    if (datafilters[n] != coloredby) {
			datafilters[n].loadRecords(datarecords, datarecords, false);
			stateposition[STATE_LOADING_COMPONENTS]++;
		    }
		}
	    }
	    if (dataplots != null) {
		stateargument[STATE_LOADING_COMPONENTS] = "dataplots";
		for (n = 0; n < dataplots.length; n++) {
		    dataplots[n].loadRecords(datarecords, datarecords, false);
		    stateposition[STATE_LOADING_COMPONENTS]++;
		}
	    }
	    if (dataviewers != null) {
		stateargument[STATE_LOADING_COMPONENTS] = "dataviewers";
		for (n = 0; n < dataviewers.length; n++) {
		    if (dataviewers[n] != coloredby) {
			dataviewers[n].loadRecords(datarecords, datarecords, false);
			stateposition[STATE_LOADING_COMPONENTS]++;
		    }
		}
	    }
	    if (datatables != null) {
		stateargument[STATE_LOADING_COMPONENTS] = "datatables";
		for (n = 0; n < datatables.length; n++) {
		    datatables[n].loadRecords(datarecords, datarecords, false);
		    stateposition[STATE_LOADING_COMPONENTS]++;
		}
	    }
	    if (sweepfilters != null) {
		stateargument[STATE_LOADING_COMPONENTS] = "sweepfilters";
		for (n = 0; n < sweepfilters.length; n++) {
		    sweepfilters[n].loadRecords(null, null, false);
		    stateposition[STATE_LOADING_COMPONENTS]++;
		}
	    }
	    loadCounters();
	}

	afterLoad();
    }


    private synchronized void
    releaseMask(int mask) {

	Enumeration  enm;
	Object       key;
	int          value[];

	if (mask != 0) {
	    for (enm = maskmap.keys(); enm.hasMoreElements(); ) {
		key = enm.nextElement();
		if ((value = (int[])maskmap.get(key)) != null) {
		    if (value[1] == mask) {
			if (--value[0] <= 0) {
			    maskmap.remove(key);
			    value[0] = 0;
			    maskstack.insertElementAt(value, 0);
			}
			break;
		    }
		}
	    }
	}
    }


    private synchronized void
    releaseMasks(int masks[]) {

	int  n;

	if (masks != null) {
	    for (n = 0; n < masks.length; n++)
		releaseMask(masks[n]);
	}
    }


    private synchronized void
    removeDataFilters() {

	DataViewer  filter;
	HitBuffer   hits;
	int         fieldmasks[];
	int         selectmasks[];
	int         count;
	int         n;

	if (datafilters != null) {
	    for (n = 0; n < datafilters.length; n++) {
		if ((filter = datafilters[n]) != null) {
		    fieldmasks = filter.getFieldMasks();
		    selectmasks = filter.getSelectMasks();
		    filter.setDataManager(null, -1, 0);
		    filter = null;
		    if (selectmasks != null) {
			if ((hits = getHitBuffer(datarecords)) != null) {
			    if ((count = collectRecords(hits, selectmasks, false)) > 0) {
				updateData(datarecords, hits, count, filter);
				Thread.yield();
			    } else releaseHitBuffer(hits);
			}
		    }
		    releaseMasks(fieldmasks);
		}
	    }
	}

	if (coloredby != null) {
	    if (coloredby.getDataManager() == null)
		coloredby = null;
	}
	datafilters = null;
    }


    private synchronized void
    removeDataPlots() {

	HitBuffer  hits;
	DataPlot   plot;
	int        selectmasks[];
	int        xmask;
	int        ymask;
	int        count;
	int        n;

	if (dataplots != null) {
	    for (n = 0; n < dataplots.length; n++) {
		if ((plot = dataplots[n]) != null) {
		    xmask = plot.getXMask();
		    ymask = plot.getYMask();
		    selectmasks = plot.getSelectMasks();
		    plot.setDataManager(null);
		    plot.setXIndex(-1);
		    plot.setYIndex(-1);
		    plot.setSweepFilter(null);
		    plot.setUnixTime(null);
		    plot = null;
		    if (selectmasks != null) {
			if ((hits = getHitBuffer(datarecords)) != null) {
			    if ((count = collectRecords(hits, selectmasks, false)) > 0) {
				updateData(datarecords, hits, count, plot);
				Thread.yield();
			    } else releaseHitBuffer(hits);
			}
		    }
		    releaseMask(xmask);
		    releaseMask(ymask);
		}
	    }
	}

	sortedby = null;
	dataplots = null;
    }


    private synchronized void
    removeDataTables() {

	DataTable  table;
	HitBuffer   hits;
	int         fieldmasks[];
	int         selectmasks[];
	int         count;
	int         n;
	int         m;

	//
	// Pretty much just a copy of removeDataFilters(), but we will
	// definitely have to do more. There usually will be more than
	// one mask and field associated with a DataViewer.
	//

	if (datatables != null) {
	    for (n = 0; n < datatables.length; n++) {
		if ((table = datatables[n]) != null) {
		    fieldmasks = table.getFieldMasks();
		    selectmasks = table.getSelectMasks();
		    table.setDataManager(null, null, null, null, null);
		    table = null;
		    if (selectmasks != null) {
			if ((hits = getHitBuffer(datarecords)) != null) {
			    if ((count = collectRecords(hits, selectmasks, false)) > 0) {
				updateData(datarecords, hits, count, table);
				Thread.yield();
			    } else releaseHitBuffer(hits);
			}
		    }
		    releaseMasks(fieldmasks);
		}
	    }
	}

	datatables = null;
    }


    private synchronized void
    removeDataViewers() {

	DataViewer  viewer;
	HitBuffer   hits;
	int         fieldmasks[];
	int         selectmasks[];
	int         count;
	int         n;
	int         m;

	//
	// Pretty much just a copy of removeDataFilters(), but we will
	// definitely have to do more. There usually will be more than
	// one mask and field associated with a DataViewer.
	//

	if (dataviewers != null) {
	    for (n = 0; n < dataviewers.length; n++) {
		if ((viewer = dataviewers[n]) != null) {
		    fieldmasks = viewer.getFieldMasks();
		    selectmasks = viewer.getSelectMasks();
		    viewer.setDataManager(null, null, null, null, null);
		    viewer = null;
		    if (selectmasks != null) {
			if ((hits = getHitBuffer(datarecords)) != null) {
			    if ((count = collectRecords(hits, selectmasks, false)) > 0) {
				updateData(datarecords, hits, count, viewer);
				Thread.yield();
			    } else releaseHitBuffer(hits);
			}
		    }
		    releaseMasks(fieldmasks);
		}
	    }
	}

	if (coloredby != null) {
	    if (coloredby.getDataManager() == null)
		coloredby = null;
	}
	dataviewers = null;
    }


    private synchronized void
    removeSweepFilters() {

	SweepFilter  filter;
	HitBuffer    hits;
	DataPlot     plot;
	int          fieldmasks[];
	int          selectmasks[];
	int          count;
	int          n;

	if (sweepfilters != null) {
	    for (n = 0; n < sweepfilters.length; n++) {
		if ((filter = sweepfilters[n]) != null) {
		    fieldmasks = filter.getFieldMasks();
		    selectmasks = filter.getSelectMasks();
		    filter.setDataManager(null, -1, 0);
		    filter.setSweepFiltering(false);
		    filter = null;
		    if (selectmasks != null) {
			if ((hits = getHitBuffer(datarecords)) != null) {
			    if ((count = collectRecords(hits, selectmasks, false)) > 0) {
				updateData(datarecords, hits, count, filter);
				Thread.yield();
			    } else releaseHitBuffer(hits);
			}
		    }
		    releaseMasks(fieldmasks);
		}
	    }
	}

	sweepfilters = null;
    }


    private ArrayList
    separateRecords(char data[], ArrayList records) {

	BufferedReader  reader = null;
	String          separators;

	try {
	    reader = new BufferedReader(new CharArrayReader(data));
	    startState(STATE_LOADING_DATA, data.length);
	    if (inputseparators instanceof String) {
		separators = (String)inputseparators;
		if (separators.length() == 1)
		    separateText(reader, separators.charAt(0), records);
		else separateText(reader, separators, records);
	    } else separateText(reader, (YoixObject)inputseparators, records);
	}
	catch(IOException e) {
	    VM.caughtException(e);
	}
	catch(RuntimeException e) {
	    VM.caughtException(e);
	}
	finally {
	    try {
		reader.close();
	    }
	    catch(IOException e) {
		VM.caughtException(e);
	    }
	}

	return(records);
    }


    private ArrayList
    separateRecords(String text, ArrayList records) {

	BufferedReader  reader = null;
	String          separators;

	try {
	    reader = new BufferedReader(new StringReader(text));
	    startState(STATE_LOADING_DATA, text.length());
	    if (inputseparators instanceof String) {
		separators = (String)inputseparators;
		if (separators.length() == 1)
		    separateText(reader, separators.charAt(0), records);
		else separateText(reader, separators, records);
	    } else separateText(reader, (YoixObject)inputseparators, records);
	}
	catch(IOException e) {
	    VM.caughtException(e);
	}
	catch(RuntimeException e) {
	    VM.caughtException(e);
	}
	finally {
	    try {
		reader.close();
	    }
	    catch(IOException e) {
		VM.caughtException(e);
	    }
	}

	return(records);
    }


    private void
    separateText(BufferedReader reader, char separator, ArrayList records)

	throws IOException

    {

	boolean  intern;
	boolean  trim;
	Object   fields[];
	String   buffer[];
	String   line;
	int      length;
	int      total;
	int      first;
	int      next;
	int      count;
	int      n;

	//
	// Separates each input line, assuming fields are delimited by
	// the same character. Each separated row is saved as an array
	// of Strings in the records ArrayList. This probably is the
	// most common way to load data.
	//

	if (fieldcount > 0 && datafields != null) {
	    buffer = new String[fieldcount];
	    intern = getData().getBoolean(NL_INTERN);
	    trim = getData().getBoolean(NL_TRIMFIELDS);
	    total = datafields.length;
	    while ((line = reader.readLine()) != null) {
		if ((length = line.length()) > 0) {
		    stateposition[STATE_LOADING_DATA] += length + 1;
		    if (inputcomment == null || line.startsWith(inputcomment) == false) {
			for (count = 0, next = 0; next < length && count < buffer.length; count++, next++) {
			    first = next;
			    if (inputfields.get(count)) {
			        if ((next = line.indexOf(separator, first)) < 0) {
				    buffer[count] = line.substring(first);
				    next = length;
				} else buffer[count] = line.substring(first, next);
				if (trim)
				    buffer[count] = buffer[count].trim();
				if (intern)
				    buffer[count] = buffer[count].intern();
			    } else {
				if ((next = line.indexOf(separator, first)) < 0)
				    next = length;
				buffer[count] = null;
			    }
			}
			fields = new Object[total];
			for (n = 0; n < total; n++)
			    datafields[n].loadField(fields, buffer, count);
			records.add(fields);
		    }
		}
	    }
	}
    }


    private void
    separateText(BufferedReader reader, String separators, ArrayList records)

	throws IOException

    {

	boolean  intern;
	boolean  trim;
	Object   fields[];
	String   buffer[];
	String   line;
	int      length;
	int      total;
	int      first;
	int      next;
	int      count;
	int      n;

	//
	// Separates each input line, assuming fields are delimited by
	// the same character. Each separated row is saved as an array
	// of Strings in the records ArrayList.
	//

	if (fieldcount > 0 && datafields != null) {
	    buffer = new String[fieldcount];
	    intern = getData().getBoolean(NL_INTERN);
	    trim = getData().getBoolean(NL_TRIMFIELDS);
	    total = datafields.length;
	    while ((line = reader.readLine()) != null) {
		if ((length = line.length()) > 0) {
		    stateposition[STATE_LOADING_DATA] += length + 1;
		    if (inputcomment == null || line.startsWith(inputcomment) == false) {
			for (count = 0, next = 0; next < length && count < buffer.length; count++) {
			    for (first = next; next < length; next++) {
				if (separators.indexOf(line.charAt(next)) >= 0)
				    break;
			    }
			    if (first < length) {
				if (next < length)
				    buffer[count] = line.substring(first, next++);
				else buffer[count] = line.substring(first);
				if (trim)
				    buffer[count] = buffer[count].trim();
				if (intern)
				    buffer[count] = buffer[count].intern();
			    } else buffer[count] = null;
			}
			fields = new Object[total];
			for (n = 0; n < total; n++)
			    datafields[n].loadField(fields, buffer, count);
			records.add(fields);
		    }
		}
	    }
	}
    }


    private void
    separateText(BufferedReader reader, YoixObject separators, ArrayList records)

	throws IOException

    {

	ArrayList  list;
	boolean    intern;
	boolean    trim;
	Object     fields[];
	String     buffer[];
	String     line;
	int        length;
	int        total;
	int        count;
	int        n;

	//
	// Separates each input line, assuming fields are delimited by
	// the same character. Each separated row is saved as an array
	// of Strings in the records ArrayList.
	//

	if (fieldcount > 0 && datafields != null) {
	    buffer = new String[fieldcount];
	    list = new ArrayList(fieldcount);
	    intern = getData().getBoolean(NL_INTERN);
	    trim = getData().getBoolean(NL_TRIMFIELDS);
	    total = datafields.length;
	    while ((line = reader.readLine()) != null) {
		if ((length = line.length()) > 0) {
		    stateposition[STATE_LOADING_DATA] += length + 1;
		    if (inputcomment == null || line.startsWith(inputcomment) == false) {
			list.clear();
			YoixMisc.split(line, separators, list);
			for (count = 0; count < list.size() && count < buffer.length; count++) {
			    buffer[count] = (String)list.get(count);
			    if (trim)
				buffer[count] = buffer[count].trim();
			    if (intern)
				buffer[count] = buffer[count].intern();
			}
			fields = new Object[total];
			for (n = 0; n < total; n++)
			    datafields[n].loadField(fields, buffer, count);
			records.add(fields);
		    }
		}
	    }
	}
    }


    private ArrayList
    separateTextLines(String text) {

	BufferedReader  reader = null;
	ArrayList       lines;
	String          line;

	//
	// A simple method that breaks the text string into lines that
	// must duplicate the line separation in separateText().
	//

	lines = new ArrayList();

	try {
	    reader = new BufferedReader(new StringReader(text));
	    while ((line = reader.readLine()) != null) {
		if (line.length() > 0) {
		    if (inputcomment == null || line.startsWith(inputcomment) == false)
			lines.add(line);
		}
	    }
	}
	catch(IOException e) {
	    VM.caughtException(e);
	}
	catch(RuntimeException e) {
	    VM.caughtException(e);
	}
	finally {
	    try {
		reader.close();
	    }
	    catch(IOException e) {
		VM.caughtException(e);
	    }
	}

	return(lines);
    }


    private synchronized void
    setAfterColoredBy(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0) || obj.callable(1))
		aftercoloredby = obj;
	    else VM.abort(TYPECHECK, NL_AFTERCOLOREDBY);
	} else aftercoloredby = null;
    }


    private synchronized void
    setAfterLoad(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0) || obj.callable(1))
		afterload = obj;
	    else VM.abort(TYPECHECK, NL_AFTERLOAD);
	} else afterload = null;
    }


    private synchronized void
    setAfterUpdate(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0) || obj.callable(1))
		afterupdate = obj;
	    else VM.abort(TYPECHECK, NL_AFTERUPDATE);
	} else afterupdate = null;
    }


    private synchronized void
    setColoredBy(YoixObject obj) {

	Object  colorer;

	colorer = getManagedObject(obj);

	if ((colorer instanceof DataColorer) || obj.isNull()) {
	    if (colorer != coloredby) {
		if (colorer == null || ((DataColorer)colorer).isManagedBy(this)) {
		    if ((coloredby = (DataColorer)colorer) != null)
			recolorData(coloredby);
		    else recolorData(null);
		} else recolorData(null);
	    }
	} else VM.abort(TYPECHECK, NL_COLOREDBY);
    }


    private synchronized void
    setDataFields(YoixObject obj) {

	DataGenerator  generator;
	YoixObject     dict;
	ArrayList      counters;
	ArrayList      fields;
	String         badfield;
	int            size;
	int            length;
	int            count;
	int            index;
	int            n;

	datafields = null;
	datatags = null;
	datapartitions = null;
	countertags = null;
	counterindices = null;
	countervalues = null;
	countertotals = null;
	inputfields = null;
	fieldcount = 0;

	if (obj.notNull()) {
	    size = obj.sizeof();
	    length = obj.length();
	    inputfields = new BitSet(size);
	    datatags = new Hashtable(size);
	    datapartitions = new Hashtable();
	    fields = new ArrayList(size);
	    counters = new ArrayList();
	    count = 0;
	    for (n = obj.offset(); n < length; n++) {
		if ((dict = obj.getObject(n)) != null) {
		    if (dict.notNull()) {
			if (dict.isDictionary()) {
			    generator = new DataGenerator(this, this, count);
			    if ((badfield = generator.construct(dict)) == null) {
				if (generator.getEnabled()) {
				    count++;
				    fields.add(generator);
				    if (generator.getAccumulate())
					counters.add(generator);
				    setTagIndex(generator.getTag(), generator.getID());
				    if ((index = generator.getIndex()) >= 0) {
					inputfields.set(index);
					fieldcount = Math.max(index + 1, fieldcount);
				    }
				}
			    } else abort(dict, NL_DATAFIELDS, n, badfield);
			} else VM.abort(TYPECHECK, NL_DATAFIELDS, n);
		    }
		}
	    }
	    if (fields.size() > 0) {
		datafields = new DataGenerator[fields.size()];
		for (n = 0; n < datafields.length; n++) {
		    datafields[n] = (DataGenerator)fields.get(n);
		    datafields[n].resolve();
		}
		if (counters.size() > 0) {
		    counterindices =  new int[counters.size()];
		    countertags =  new String[counters.size()];
		    for (n = 0; n < counterindices.length; n++) {
			generator = (DataGenerator)counters.get(n);
			counterindices[n] = generator.getID();
			countertags[n] = generator.getTag();
		    }
		}
	    }
	}
    }


    private synchronized void
    setDataFilters(YoixObject obj) {

	YoixObject  dict;
	YoixObject  element;
	ArrayList   fielddata;
	ArrayList   filters;
	Object      filter;
	int         masks[];
	int         indices[];
	int         partitions[];
	int         values[];
	int         active;
	int         diversity;
	int         length;
	int         m;
	int         n;

	//
	// Much of this code duplicates setDataViewers() so it's not hard
	// to imagine handling everything in a single method. Differences,
	// if there are any, are historic and probably confusing it this
	// is the first time you've looked at this code. A long time ago
	// we only supported histograms, which we called datafilters, and
	// there were abstract classes, like AWTDataFilter, that histograms
	// extended. When we added graphs we needed to make sure we didn't
	// break histograms so we introduced new abstract classes, like
	// AWTDataViewer, that graphs could extend and we added support
	// for a field named dataviewers. We kept the old DataFilter code
	// around until recently, when we were asked to add new features
	// to histograms, and at that point we decided to paritially clean
	// things up. We're definitely not done yet (in fact we like the
	// DataFilter name more than DataViewer), but things have improved
	// quite a bit so we'll probably stop until we're sure existing
	// applications are all working properly.
	//

	removeDataFilters();

	if (obj.notNull()) {
	    length = obj.length();
	    filters = new ArrayList();
	    for (n = obj.offset(); n < length; n++) {
		if ((dict = obj.getObject(n)) != null) {
		    if (dict.notNull()) {
			if (dict.isDictionary()) {
			    if ((fielddata = getFieldData(dict, NL_FIELD)) == null)
				abort(dict, NL_DATAFILTERS, n, NL_FIELD);
			    if ((values = getIndexArray(dict, NL_VALUE)) == null)
				abort(dict, NL_DATAFILTERS, n, NL_VALUE);
			    if ((diversity = getTagIndex(dict, NL_DIVERSITY, -1)) < -1)
				abort(dict, NL_DATAFILTERS, n, NL_DIVERSITY);
			    if ((element = dict.getObject(NL_FILTER)) != null) {
				filter = getManagedObject(element);
				if (filter instanceof DataViewer) {
				    filters.add(filter);
				    filters.add(fielddata);
				    filters.add(values);
				    filters.add(new Integer(diversity));
				} else VM.abort(TYPECHECK, NL_DATAFILTERS, n, NL_FILTER);
			    } else VM.abort(UNDEFINED, NL_DATAFILTERS, n, NL_FILTER);
			} else VM.abort(TYPECHECK, NL_DATAFILTERS, n);
		    }
		}
	    }
	    if ((length = filters.size()) > 0) {
		datafilters = new DataViewer[length/4];
		for (n = 0, m = 0; n < length; n += 4, m++) {
		    datafilters[m] = (DataViewer)filters.get(n);
		    fielddata = (ArrayList)filters.get(n+1);
		    values = (int[])filters.get(n+2);
		    diversity = ((Integer)filters.get(n+3)).intValue();
		    indices = (int[])fielddata.get(0);
		    partitions = (int[])fielddata.get(1);
		    active = ((Integer)fielddata.get(2)).intValue();
		    masks = getDataMasks(indices, active, partitions);
		    datafilters[m].setDataManager(this, indices, masks, values, partitions);
		    datafilters[m].setDiversityIndex(diversity);
		    if (datarecords != null)
			datafilters[m].loadRecords(datarecords, datarecords, false);
		}
	    }
	}

	setField(NL_COLOREDBY);
    }


    private synchronized void
    setDataPlots(YoixObject obj) {

	YoixObject  dict;
	YoixObject  element;
	YoixObject  array;
	ArrayList   plots;
	Object      plot;
	Object      filter;
	Object      filters[];
	Object      generator[];
	int         index;
	int         xindex;
	int         yindex;
	int         partition;
	int         length;
	int         k;
	int         m;
	int         n;

	removeDataPlots();

	if (obj.notNull()) {
	    length = obj.length();
	    plots = new ArrayList();
	    for (n = obj.offset(); n < length; n++) {
		if ((dict = obj.getObject(n)) != null) {
		    if (dict.notNull()) {
			if (dict.isDictionary()) {
			    partition = getTagPartition(dict.getString(NL_XAXIS));
			    if ((xindex = getTagIndex(dict, NL_XAXIS, -1)) < 0)
				abort(dict, NL_DATAPLOTS, n, NL_XAXIS);
			    if ((yindex = getTagIndex(dict, NL_YAXIS, -1)) < 0)
				abort(dict, NL_DATAPLOTS, n, NL_YAXIS);
			    if ((element = dict.getObject(NL_FILTER)) != null) {
				if (element.notNull()) {
				    if (element.isArray()) {
					array = element;
					filters = new SweepFilter[array.sizeof()];
					for (k = 0, m = array.offset(); k < filters.length; k++, m++) {
					    if ((element = array.getObject(m)) != null) {
						if (element.notNull()) {
						    filter = getManagedObject(element);
						    if (filter instanceof SweepFilter)
							filters[k] = filter;
						    else VM.abort(TYPECHECK, NL_DATAPLOTS, n, NL_FILTER);
						}
					    }
					}
					filter = filters;
				    } else {
					filter = getManagedObject(element);
					if (!(filter instanceof SweepFilter))
					    VM.abort(TYPECHECK, NL_DATAPLOTS, n, NL_FILTER);
				    }
				} else filter = null;
			    } else filter = null;
			    if ((element = dict.getObject(NL_GENERATOR)) != null) {
				generator = Misc.makeGenerator(element, this);
				if (generator == null && element.notNull())
				    VM.abort(BADVALUE, NL_DATAPLOTS, n, NL_GENERATOR);
			    } else generator = null;
			    if ((element = dict.getObject(NL_PLOT)) != null) {
				plot = getManagedObject(element);
				if (plot instanceof DataPlot) {
				    plots.add(plot);
				    plots.add(new Integer(xindex));
				    plots.add(new Integer(yindex));
				    plots.add(filter);
				    plots.add(generator);
				    plots.add(new Integer(partition));
				} else VM.abort(TYPECHECK, NL_DATAPLOTS, n, NL_PLOT);
			    } else VM.abort(UNDEFINED, NL_DATAPLOTS, n, NL_PLOT);
			} else VM.abort(TYPECHECK, NL_DATAPLOTS, n);
		    }
		}
	    }
	    if ((length = plots.size()) > 0) {
		dataplots = new DataPlot[length/6];
		for (n = 0, m = 0; n < length; n += 6, m++) {
		    dataplots[m] = (DataPlot)plots.get(n);
		    xindex = ((Integer)plots.get(n+1)).intValue();
		    yindex = ((Integer)plots.get(n+2)).intValue();
		    filter = plots.get(n+3);
		    generator = (Object[])plots.get(n+4);
		    partition = ((Integer)plots.get(n+5)).intValue();
		    dataplots[m].setXIndex(xindex);
		    dataplots[m].setYIndex(yindex);
		    dataplots[m].setPartitionIndex(partition);
		    dataplots[m].setDataManager(this);
		    if (filter instanceof SweepFilter[])
			dataplots[m].setSweepFilters((SweepFilter[])filter);
		    else dataplots[m].setSweepFilter((SweepFilter)filter);
		    dataplots[m].setGenerator(generator);
		    if (datarecords != null)
			dataplots[m].loadRecords(datarecords, datarecords, false);
		}
	    }
	}

	setField(NL_SORTEDBY);
    }


    private synchronized void
    setDataTables(YoixObject obj) {

	YoixObject  dict;
	YoixObject  element;
	ArrayList   fielddata;
	ArrayList   tables;
	Object      table;
	int         masks[];
	int         indices[];
	int         partitions[];
	int         values[];
	int         active;
	int         length;
	int         m;
	int         n;

	removeDataTables();

	if (obj.notNull()) {
	    length = obj.length();
	    tables = new ArrayList();
	    for (n = obj.offset(); n < length; n++) {
		if ((dict = obj.getObject(n)) != null) {
		    if (dict.notNull()) {
			if (dict.isDictionary()) {
			    if ((fielddata = getFieldData(dict, NL_FIELD)) == null)
				abort(dict, NL_DATATABLES, n, NL_FIELD);
			    if ((values = getIndexArray(dict, NL_VALUE)) == null)
				abort(dict, NL_DATATABLES, n, NL_VALUE);
			    if ((element = dict.getObject(NL_FILTER)) != null) {
				table = getManagedObject(element);
				if (table instanceof DataTable) {
				    tables.add(table);
				    tables.add(fielddata);
				    tables.add(values);
				} else VM.abort(TYPECHECK, NL_DATATABLES, n, NL_FILTER);
			    } else VM.abort(UNDEFINED, NL_DATATABLES, n, NL_FILTER);
			} else VM.abort(TYPECHECK, NL_DATATABLES, n);
		    }
		}
	    }
	    if ((length = tables.size()) > 0) {
		datatables = new DataTable[length/3];
		for (n = 0, m = 0; n < length; n += 3, m++) {
		    datatables[m] = (DataTable)tables.get(n);
		    fielddata = (ArrayList)tables.get(n+1);
		    values = (int[])tables.get(n+2);
		    indices = (int[])fielddata.get(0);
		    partitions = (int[])fielddata.get(1);
		    active = ((Integer)fielddata.get(2)).intValue();
		    masks = getDataMasks(indices, active, partitions);
		    datatables[m].setDataManager(this, indices, masks, values, partitions);
		    if (datarecords != null)
			datatables[m].loadRecords(datarecords, datarecords, false);
		}
	    }
	}
    }


    private synchronized void
    setDataViewers(YoixObject obj) {

	YoixObject  dict;
	YoixObject  element;
	ArrayList   fielddata;
	ArrayList   viewers;
	Object      viewer;
	int         masks[];
	int         indices[];
	int         partitions[];
	int         values[];
	int         active;
	int         length;
	int         m;
	int         n;

	//
	// The first check is only required while we support the backward
	// compatibility kludge that lets graphs in through datafilters.
	// The kludge assumes that we're using old code if NL_DATAVIEWERS
	// is defined, which means it can't be included in the DataManager
	// template that's defined in Module.java. This test, and the code
	// in setDataFilters() that supports viewers, can be removed when
	// we no longer need to worry about backward compatibility. In that
	// case we should also add NL_DATAVIEWERS to DataManager template.
	//

	if (obj.isArray() || obj.isNull()) {
	    removeDataViewers();
	    if (obj.notNull()) {
		length = obj.length();
		viewers = new ArrayList();
		for (n = obj.offset(); n < length; n++) {
		    if ((dict = obj.getObject(n)) != null) {
			if (dict.notNull()) {
			    if (dict.isDictionary()) {
				if ((fielddata = getFieldData(dict, NL_FIELDINDICES)) == null)
				    abort(dict, NL_DATAVIEWERS, n, NL_FIELDINDICES);
				if ((values = getIndexArray(dict, NL_VALUE)) == null)
				    abort(dict, NL_DATAVIEWERS, n, NL_VALUE);
				if ((element = dict.getObject(NL_FILTER)) != null) {
				    viewer = getManagedObject(element);
				    if (viewer instanceof DataViewer) {
					viewers.add(viewer);
					viewers.add(fielddata);
					viewers.add(values);
				    } else VM.abort(TYPECHECK, NL_DATAVIEWERS, n, NL_FILTER);
				} else VM.abort(UNDEFINED, NL_DATAVIEWERS, n, NL_FILTER);
			    } else VM.abort(TYPECHECK, NL_DATAVIEWERS, n);
			}
		    }
		}
		if ((length = viewers.size()) > 0) {
		    dataviewers = new DataViewer[length/3];
		    for (n = 0, m = 0; n < length; n += 3, m++) {
			dataviewers[m] = (DataViewer)viewers.get(n);
			fielddata = (ArrayList)viewers.get(n+1);
			values = (int[])viewers.get(n+2);
			indices = (int[])fielddata.get(0);
			partitions = (int[])fielddata.get(1);
			active = ((Integer)fielddata.get(2)).intValue();
			masks = getDataMasks(indices, active, partitions);
			dataviewers[m].setDataManager(this, indices, masks, values, partitions);
			if (datarecords != null)
			    dataviewers[m].loadRecords(datarecords, datarecords, false);
		    }
		}
	    }
	} else VM.abort(TYPECHECK, NL_DATAVIEWERS);
    }


    private synchronized void
    setDispose(YoixObject obj) {

	if (obj.booleanValue())
	    dispose();
    }


    private synchronized void
    setGraphData(YoixObject obj) {

	if (obj.isNull())
	    graphdata = null;
	else graphdata = obj.getGraphElement();
    }


    private synchronized void
    setInputComment(YoixObject obj) {

	if (obj.notNull()) {
	    inputcomment = obj.stringValue();
	    if (inputcomment.length() == 0)
		inputcomment = null;
	} else inputcomment = null;
    }


    private synchronized void
    setInputFilter(YoixObject obj) {

	YoixObject  separators;

	inputseparators = "|";

	if (obj.notNull()) {
	    if (obj.isArray() || obj.isDictionary())
		inputseparators = obj;
	    else if (obj.isString())
		inputseparators = obj.stringValue();
	    else VM.abort(TYPECHECK, NL_INPUTFILTER);
	}
    }


    private synchronized void
    setPlotFilters(YoixObject obj) {

	if (obj.notNull() && sweepfiltersname == null)
	    sweepfiltersname = NL_PLOTFILTERS;
	setSweepFilters(obj, NL_PLOTFILTERS);
    }


    private synchronized void
    setSortedBy(YoixObject obj) {

	DataPlot  sorter;
	int       n;

	sorter = (DataPlot)getManagedObject(obj);

	if (sorter != sortedby) {
	    if (sorter == null || sorter.isManagedBy(this))
		sortedby = sorter;
	    else sortedby = null;
	    sortRecords(sortedby);
	}
    }


    private synchronized void
    setSweepFilters(YoixObject obj) {

	if (obj.notNull() && sweepfiltersname == null)
	    sweepfiltersname = NL_SWEEPFILTERS;
	setSweepFilters(obj, NL_SWEEPFILTERS);
    }


    private synchronized void
    setSweepFilters(YoixObject obj, String name) {

	YoixObject  dict;
	YoixObject  element;
	ArrayList   fielddata;
	ArrayList   filters;
	Object      filter;
	int         field;
	int         length;
	int         masks[];
	int         indices[];
	int         partitions[];
	int         active;
	int         m;
	int         n;

	//
	// The initial check of name is a kludge that lets us support old
	// and new versions of low level Yoix scripts. Old versions used
	// NL_PLOTFILTERS to associate a special sweep filter with plot.
	// Our current version changed the name to NL_SWEEPFILTERS, but
	// that introduced protability problems with on old but important
	// internal application. The kludge probably can be removed when
	// we update that application.
	//

	if (sweepfiltersname == null || sweepfiltersname.equals(name)) {
	    removeSweepFilters();
	    if (obj.notNull()) {
		length = obj.length();
		filters = new ArrayList();
		for (n = obj.offset(); n < length; n++) {
		    if ((dict = obj.getObject(n)) != null) {
			if (dict.notNull()) {
			    if (dict.isDictionary()) {
				if ((element = dict.getObject(NL_FILTER)) != null) {
				    filter = getManagedObject(element);
				    if (filter instanceof DataTable) {
					if ((fielddata = getFieldData(dict, NL_FIELD)) == null)
					    abort(dict, NL_DATATABLES, n, NL_FIELD);
					if ((field = getTagIndex(dict, NL_VALUE, -1)) < 0)
					    abort(dict, NL_SWEEPFILTERS, n, NL_FIELD);
					filters.add(filter);
					filters.add(new Integer(field));
					filters.add(fielddata);
				    } else if (filter instanceof SweepFilter) {
					fielddata = null;
					if ((field = getTagIndex(dict, NL_FIELD, -1)) < 0) {
					    if ((fielddata = getFieldData(dict, NL_FIELD)) == null)
						abort(dict, NL_SWEEPFILTERS, n, NL_FIELD);
					    if ((field = getTagIndex(dict, NL_VALUE, -1)) < 0)
						abort(dict, NL_SWEEPFILTERS, n, NL_FIELD);
					}
					filters.add(filter);
					filters.add(new Integer(field));
					filters.add(fielddata);
				    } else VM.abort(TYPECHECK, NL_SWEEPFILTERS, n, NL_FILTER);
				} else VM.abort(UNDEFINED, NL_SWEEPFILTERS, n, NL_FILTER);
			    } else VM.abort(TYPECHECK, NL_SWEEPFILTERS, n);
			}
		    }
		}
	        if ((length = filters.size()) > 0) {
		    sweepfilters = new SweepFilter[length/3];
		    for (n = 0, m = 0; n < length; n += 3, m++) {
			fielddata = (ArrayList)filters.get(n+2);
			if (fielddata == null) {
			    sweepfilters[m] = (SweepFilter)filters.get(n);
			    field = ((Integer)filters.get(n+1)).intValue();
			    sweepfilters[m].setDataManager(this, field, getDataMask(field, -1));
			    sweepfilters[m].setSweepFiltering(true);
			} else {
			    filter = filters.get(n);
			    sweepfilters[m] = (SweepFilter)filter;
			    field = ((Integer)filters.get(n+1)).intValue();
			    fielddata = (ArrayList)filters.get(n+2);
			    indices = (int[])fielddata.get(0);
			    partitions = (int[])fielddata.get(1);
			    active = ((Integer)fielddata.get(2)).intValue();
			    masks = getDataMasks(indices, active, partitions);
			    ((SweepFilter)filter).setDataManager(this, indices, masks, new int[] {field}, partitions);
			    sweepfilters[m].setSweepFiltering(true);
			}
		    }
		}
	    }
	}
    }


    private synchronized void
    setText(YoixObject obj) {

	YoixObject  monitor = null;
	ArrayList   records;
	char        buf[];
	int         capacity;

	try {
	    startState(STATE_RUNNING);
	    monitor = startMonitor();
	    clearData();
	    if (obj != null && obj.notNull()) {
		//
		// A simple-minded estimate - it would not be hard to
		// do a much better job, probably in separateText().
		//
		capacity = Math.max(1000, obj.sizeof()/500);	// a simpleminded guess
		if ((buf = obj.toCharArray(true, null)) != null)
		    records = separateRecords(buf, new ArrayList(capacity));
		else records = separateRecords(obj.stringValue(), new ArrayList(capacity));
		if (records.size() > 0)
		    loadData(records);
	    }
	}
	finally {
	    stopMonitor(monitor);
	    startState(STATE_IDLE);
	}
    }


    private YoixObject
    startMonitor() {

	YoixObject  monitor;
	YoixObject  run;
	YoixObject  args[];

	if ((monitor = getData().getObject(NL_MONITOR)) != null) {
	    if (monitor.notNull()) {
		if ((run = monitor.getObject(NY_RUN)) != null) {
		    if (run.notNull()) {
			if (run.callable(1))
			    args = new YoixObject[] {getContext()};
			else args = new YoixObject[] {};
			monitor.execute(NY_RUN, args, null);
		    } else monitor = null;
		} else monitor = null;
	    } else monitor = null;
	}

	return(monitor);
    }


    private void
    startState(int state) {

	startState(state, 0, null);
    }


    private void
    startState(int state, int total) {

	startState(state, total, null);
    }


    private void
    startState(int state, String argument) {

	startState(state, 0, argument);
    }


    private synchronized void
    startState(int state, int total, String argument) {

	currentstate = state;
	stateargument[state] = argument;
	stateposition[state] = 0;
	statetotal[state] = total;
	statetimer[state] = System.currentTimeMillis();
    }


    private void
    stopMonitor(YoixObject monitor) {

	if (monitor != null)
	    monitor.put(NY_INTERRUPTED, YoixObject.newInt(true), false);
    }


    private synchronized void
    updateCounters(HitBuffer hits, int count) {

	DataRecord  record;
	double      value;
	int         length;
	int         sign;
	int         m;
	int         n;

	if (count > 0) {
	    length = (counterindices != null) ? counterindices.length : 0;
	    for (n = 0; n < count; n++) {
		record = hits.getRecord(n);
		sign = hits.isSelected(n) ? 1 : -1;
		selectedcount += sign;
		for (m = 0; m < length; m++) {
		    value = record.getValue(counterindices[m]);
		    if (Double.isNaN(value) == false)
			countervalues[m] += sign*value;
		}
	    }
	}
    }
}

