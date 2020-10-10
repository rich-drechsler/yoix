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
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import javax.swing.JScrollPane;
import att.research.yoix.*;

final
class BodyComponentSwing extends YoixBodyComponentSwing

    implements Constants

{
    //
    // NOTE - a fix in YoixMakeScreen.javaDistance() that happened on
    // 10/30/06 resulted in slightly thinner lines, bars, and points,
    // at least on some systems. The YoixMakeScreen.java changes seem
    // correct, so we now compensate here by adding 0.5 to distances
    // that we send through YoixMakeScreen.javaDistance(). Not 100%
    // certain about this approach, but it's sufficient for now and
    // should be easy to adjust in the future - if necessary.
    //
    // An array used to set permissions on some of the fields that
    // users should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	NL_AXISMODEL,       $LR__,       null,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(140);

    static {
	activefields.put(NL_ACCUMULATE, new Integer(VL_ACCUMULATE));
	activefields.put(NL_ACTIVE, new Integer(VL_ACTIVE));
	activefields.put(NL_ACTIVEFIELDCOUNT, new Integer(VL_ACTIVEFIELDCOUNT));
	activefields.put(NL_AFTERAPPEND, new Integer(VL_AFTERAPPEND));
	activefields.put(NL_AFTERLOAD, new Integer(VL_AFTERLOAD));
	activefields.put(NL_AFTERPRESSED, new Integer(VL_AFTERPRESSED));
	activefields.put(NL_AFTERSWEEP, new Integer(VL_AFTERSWEEP));
	activefields.put(NL_AFTERUPDATE, new Integer(VL_AFTERUPDATE));
	activefields.put(NL_ALIVE, new Integer(VL_ALIVE));
	activefields.put(NL_ATTACHEDEDGESELECTION, new Integer(VL_ATTACHEDEDGESELECTION));
	activefields.put(NL_AUTOREADY, new Integer(VL_AUTOREADY));
	activefields.put(NL_AUTOSCROLL, new Integer(VL_AUTOSCROLL));
	activefields.put(NL_AUTOSHOW, new Integer(VL_AUTOSHOW));
	activefields.put(NL_AXISENDS, new Integer(VL_AXISENDS));
	activefields.put(NL_AXISLIMITS, new Integer(VL_AXISLIMITS));
	activefields.put(NL_AXISMODEL, new Integer(VL_AXISMODEL));
	activefields.put(NL_AXISWIDTH, new Integer(VL_AXISWIDTH));
	activefields.put(NL_BARSPACE, new Integer(VL_BARSPACE));
	activefields.put(NL_CLEAR, new Integer(VL_CLEAR));
	activefields.put(NL_CLICKRADIUS, new Integer(VL_CLICKRADIUS));
	activefields.put(NL_COLLECTRECORDSAT, new Integer(VL_COLLECTRECORDSAT));
	activefields.put(NL_COLOREDBY, new Integer(VL_COLOREDBY));
	activefields.put(NL_CONNECT, new Integer(VL_CONNECT));
	activefields.put(NL_CONNECTCOLOR, new Integer(VL_CONNECTCOLOR));
	activefields.put(NL_CONNECTWIDTH, new Integer(VL_CONNECTWIDTH));
	activefields.put(NL_DATAENDS, new Integer(VL_DATAENDS));
	activefields.put(NL_DATAFIELDS, new Integer(VL_DATAFIELDS));
	activefields.put(NL_DATAMANAGER, new Integer(VL_DATAMANAGER));
	activefields.put(NL_DRAGCOLOR, new Integer(VL_DRAGCOLOR));
	activefields.put(NL_EDGECOUNT, new Integer(VL_EDGECOUNT));
	activefields.put(NL_EDGEFLAGS, new Integer(VL_EDGEFLAGS));
	activefields.put(NL_EDGES, new Integer(VL_EDGES));
	activefields.put(NL_EDGESCALE, new Integer(VL_EDGESCALE));
	activefields.put(NL_EMPTYCOLOR, new Integer(VL_EMPTYCOLOR));
	activefields.put(NL_FIELDINDEX, new Integer(VL_FIELDINDEX));
	activefields.put(NL_FIELDINDICES, new Integer(VL_FIELDINDICES));
	activefields.put(NL_FILLMODEL, new Integer(VL_FILLMODEL));
	activefields.put(NY_FINDNEXTMATCH, new Integer(VL_FINDNEXTMATCH)); // NY_ -> VL_ ??
	activefields.put(NL_FONTSCALE, new Integer(VL_FONTSCALE));
	activefields.put(NL_FROZEN, new Integer(VL_FROZEN));
	activefields.put(NL_GETPOSITION, new Integer(VL_GETPOSITION));
	activefields.put(NL_GETTIPTEXTAT, new Integer(VL_GETTIPTEXTAT));
	activefields.put(NL_GETZOOMENDS, new Integer(VL_GETZOOMENDS));
	activefields.put(NL_GRAPHLAYOUTARG, new Integer(VL_GRAPHLAYOUTARG));
	activefields.put(NL_GRAPHLAYOUTMODEL, new Integer(VL_GRAPHLAYOUTMODEL));
	activefields.put(NL_GRAPHLAYOUTSORT, new Integer(VL_GRAPHLAYOUTSORT));
	activefields.put(NL_GRAPHMATRIX, new Integer(VL_GRAPHMATRIX));
	activefields.put(NL_HIDEPOINTS, new Integer(VL_HIDEPOINTS));
	activefields.put(NL_HIDEUNLABELED, new Integer(VL_HIDEUNLABELED));
	activefields.put(NL_HIGHLIGHTCOLOR, new Integer(VL_HIGHLIGHTCOLOR));
	activefields.put(NL_HIGHLIGHTED, new Integer(VL_HIGHLIGHTED));
	activefields.put(NL_IGNOREZERO, new Integer(VL_IGNOREZERO));
	activefields.put(NL_INVERTED, new Integer(VL_INVERTED));
	activefields.put(NL_KEEPTALL, new Integer(VL_KEEPTALL));
	activefields.put(NL_KEYS, new Integer(VL_KEYS));
	activefields.put(NL_LABELFLAGS, new Integer(VL_LABELFLAGS));
	activefields.put(NL_LABELS, new Integer(VL_LABELS));
	activefields.put(NL_LINEWIDTH, new Integer(VL_LINEWIDTH));
	activefields.put(NL_LOADEDENDS, new Integer(VL_LOADEDENDS));
	activefields.put(NL_LOADRECORDS, new Integer(VL_LOADRECORDS));
	activefields.put(NL_MODEL, new Integer(VL_MODEL));
	activefields.put(NL_MOVED, new Integer(VL_MOVED));
	activefields.put(NL_NODECOUNT, new Integer(VL_NODECOUNT));
	activefields.put(NL_NODEFLAGS, new Integer(VL_NODEFLAGS));
	activefields.put(NL_NODEOUTLINE, new Integer(VL_NODEOUTLINE));
	activefields.put(NL_NODES, new Integer(VL_NODES));
	activefields.put(NL_NODESCALE, new Integer(VL_NODESCALE));
	activefields.put(NL_OFFPEAKCOLOR, new Integer(VL_OFFPEAKCOLOR));
	activefields.put(NL_OPERATIONS, new Integer(VL_OPERATIONS));
	activefields.put(NL_ORIENTATION, new Integer(VL_ORIENTATION));
	activefields.put(NL_OTHERCOLOR, new Integer(VL_OTHERCOLOR));
	activefields.put(NL_OUTLINECACHESIZE, new Integer(VL_OUTLINECACHESIZE));
	activefields.put(NL_PAINTORDER, new Integer(VL_PAINTORDER));
	activefields.put(NL_PALETTE, new Integer(VL_PALETTE));
	activefields.put(NL_PLOT, new Integer(VL_PLOT));
	activefields.put(NL_PLOTENDS, new Integer(VL_PLOTENDS));
	activefields.put(NL_PLOTSTYLE, new Integer(VL_PLOTSTYLE));
	activefields.put(NL_PLOTSTYLEFLAGS, new Integer(VL_PLOTSTYLEFLAGS));
	activefields.put(NL_POINTSIZE, new Integer(VL_POINTSIZE));
	activefields.put(NL_PRESSED, new Integer(VL_PRESSED));
	activefields.put(NL_PRESSEDCOLOR, new Integer(VL_PRESSEDCOLOR));
	activefields.put(NL_PRESSINGCOLOR, new Integer(VL_PRESSINGCOLOR));
	activefields.put(NL_PRIMARYFIELD, new Integer(VL_PRIMARYFIELD));
	activefields.put(NL_RANKPREFIX, new Integer(VL_RANKPREFIX));
	activefields.put(NL_RANKSUFFIX, new Integer(VL_RANKSUFFIX));
	activefields.put(NL_RECOLORED, new Integer(VL_RECOLORED));
	activefields.put(NL_REVERSEPALETTE, new Integer(VL_REVERSEPALETTE));
	activefields.put(NL_SELECTED, new Integer(VL_SELECTED));
	activefields.put(NL_SELECTFLAGS, new Integer(VL_SELECTFLAGS));
	activefields.put(NL_SELECTWIDTH, new Integer(VL_SELECTWIDTH));
	activefields.put(NL_SEPARATOR, new Integer(VL_SEPARATOR));
	activefields.put(NL_SETALL, new Integer(VL_SETALL));
	activefields.put(NL_SETPLOTENDS, new Integer(VL_SETPLOTENDS));
	activefields.put(NL_SHADETIMES, new Integer(VL_SHADETIMES));
	activefields.put(NL_SLIDERCOLOR, new Integer(VL_SLIDERCOLOR));
	activefields.put(NL_SLIDERENABLED, new Integer(VL_SLIDERENABLED));
	activefields.put(NL_SLIDERENDS, new Integer(VL_SLIDERENDS));
	activefields.put(NL_SORTBY, new Integer(VL_SORTBY));
	activefields.put(NL_SORTDEFAULT, new Integer(VL_SORTDEFAULT));
	activefields.put(NL_SPREAD, new Integer(VL_SPREAD));
	activefields.put(NL_STACKED, new Integer(VL_STACKED));
	activefields.put(NL_SUBORDINATEPLOTS, new Integer(VL_SUBORDINATEPLOTS));
	activefields.put(NL_SWEEPCOLOR, new Integer(VL_SWEEPCOLOR));
	activefields.put(NL_SWEEPFLAGS, new Integer(VL_SWEEPFLAGS));
	activefields.put(NL_SYMMETRIC, new Integer(VL_SYMMETRIC));
	activefields.put(NL_TEXT, new Integer(VL_TEXT));
	activefields.put(NL_TICKS, new Integer(VL_TICKS));
	activefields.put(NL_TIMESHADING, new Integer(VL_TIMESHADING));
	activefields.put(NL_TIPDROPPED, new Integer(VL_TIPDROPPED));
	activefields.put(NL_TIPENABLED, new Integer(VL_TIPENABLED));
	activefields.put(NL_TIPFLAGS, new Integer(VL_TIPFLAGS));
	activefields.put(NL_TIPLOCKMODEL, new Integer(VL_TIPLOCKMODEL));
	activefields.put(NL_TIPOFFSET, new Integer(VL_TIPOFFSET));
	activefields.put(NL_TIPPREFIX, new Integer(VL_TIPPREFIX));
	activefields.put(NL_TIPSUFFIX, new Integer(VL_TIPSUFFIX));
	activefields.put(NL_TRANSIENTMODE, new Integer(VL_TRANSIENTMODE));
	activefields.put(NL_TRANSLATOR, new Integer(VL_TRANSLATOR));
	activefields.put(NL_UNIXTIME, new Integer(VL_UNIXTIME));
	activefields.put(NL_XAXIS, new Integer(VL_XAXIS));
	activefields.put(NL_YAXIS, new Integer(VL_YAXIS));
	activefields.put(NL_ZOOM, new Integer(VL_ZOOM));
	activefields.put(NL_ZOOMDIRECTION, new Integer(VL_ZOOMDIRECTION));
	activefields.put(NL_ZOOMINCOLOR, new Integer(VL_ZOOMINCOLOR));
	activefields.put(NL_ZOOMLIMIT, new Integer(VL_ZOOMLIMIT));
	activefields.put(NL_ZOOMOUTCOLOR, new Integer(VL_ZOOMOUTCOLOR));
	activefields.put(NL_ZOOMSCALE, new Integer(VL_ZOOMSCALE));

	//
	// These were added to accomodate our DataTable development. Some of
	// all of them may disappear, but if they do make sure you also clean
	// Constants.java and Module.java up.
	//

	activefields.put(NL_ANCHOR, new Integer(VL_ANCHOR));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    BodyComponentSwing(YoixObject data) {

	super(data);
    }

    ///////////////////////////////////
    //
    // BodyComponentSwing Methods
    //
    ///////////////////////////////////

    protected final Object
    buildPeer() {

	Object  comp = null;

	switch (getMinor()) {
	    case JAXIS:
		peer = comp = new SwingJAxis(getData(), this);
		setField(NL_AXISMODEL);
		setField(NY_ANCHOR);
		setField(NY_INSETS);
		setField(NY_STATE);
		setField(NY_SAVEGRAPHICS);
		setField(NY_BORDERCOLOR);
		setField(NY_BORDER);
		setField(NY_COLUMNS);
		setField(NY_ROWS);
		setField(NL_AXISWIDTH);
		setField(NL_LINEWIDTH);
		setField(NL_AXISENDS);
		setField(NL_SLIDERENABLED);
		setField(NL_SLIDERENDS);
		setField(NL_SLIDERCOLOR);
		setField(NL_INVERTED);
		setField(NL_LABELS);
		setField(NL_TICKS);
		setToConstant(NY_SIZE);
		break;

	    case JDATATABLE:
		peer = comp = new SwingJDataTable(getData(), this);
		peerscroller = new YoixSwingJScrollPane((SwingJDataTable)peer);
		setField(NY_INPUTFILTER);
		setField(NL_ACCUMULATE);
		setField(NL_AUTOREADY);
		setField(NL_AUTOSCROLL);
		setField(NL_AUTOSHOW);
		setField(NL_AFTERLOAD);
		setField(NL_AFTERPRESSED);
		setField(NL_AFTERSWEEP);
		setField(NL_AFTERUPDATE);
		setField(NL_ANCHOR);
		setField(NL_EMPTYCOLOR);
		setField(NL_HIGHLIGHTCOLOR);
		setField(NL_PRESSEDCOLOR);
		setField(NL_PRESSINGCOLOR);
		setField(NL_OTHERCOLOR);
		setField(NY_BORDERCOLOR);
		setField(NY_BORDER);
		setField(NY_COLUMNS);
		setField(NY_ROWS);
		setField(NL_TRANSIENTMODE);
		setField(NL_PALETTE);
		setField(NL_OPERATIONS);
		setField(NL_ACTIVE);
		setField(NL_ANCHOR);
		setField(NY_SCROLL);
		setField(NY_COLUMNS);
		setField(NY_RESIZE);
		setField(NY_RESIZEMODE);
		setToConstant(NY_SIZE);
		break;

	    case JEVENTPLOT:
		peer = comp = new SwingJEventPlot(getData(), this);
		setField(NL_AUTOREADY);
		setField(NL_AFTERAPPEND);
		setField(NL_AFTERLOAD);
		setField(NL_AFTERSWEEP);
		setField(NL_AFTERUPDATE);
		setField(NL_UNIXTIME);
		setField(NY_ANCHOR);
		setField(NY_INSETS);
		setField(NY_IPAD);
		setField(NY_STATE);
		setField(NY_SAVEGRAPHICS);
		setField(NL_OFFPEAKCOLOR);
		setField(NY_BORDERCOLOR);
		setField(NY_BORDER);
		setField(NL_SWEEPCOLOR);
		setField(NL_SWEEPFLAGS);
		setField(NL_AXISWIDTH);
		setField(NL_LINEWIDTH);
		setField(NL_POINTSIZE);
		setField(NL_KEEPTALL);
		setField(NL_IGNOREZERO);
		setField(NL_PLOTSTYLEFLAGS);
		setField(NL_PLOTSTYLE);
		setField(NL_HIDEPOINTS);
		setField(NL_CONNECT);
		setField(NL_CONNECTCOLOR);
		setField(NL_CONNECTWIDTH);
		setField(NL_XAXIS);
		setField(NL_YAXIS);
		setField(NL_SHADETIMES);
		setField(NL_TIMESHADING);
		setField(NL_REVERSEPALETTE);
		setField(NL_PALETTE);
		setField(NL_MODEL);
		setField(NL_STACKED);
		setField(NL_SPREAD);
		setField(NL_SYMMETRIC);
		setField(NL_RANKPREFIX);
		setField(NL_RANKSUFFIX);
		setField(NL_TIPFLAGS);
		setField(NL_TIPLOCKMODEL);
		setField(NL_TIPOFFSET);
		setField(NL_TIPPREFIX);
		setField(NL_TIPSUFFIX);
		setField(NL_TIPDROPPED);
		setField(NL_TIPENABLED);
		setToConstant(NY_SIZE);
		break;

	    case JGRAPHPLOT:
		peer = comp = new SwingJGraphPlot(getData(), this);
		setField(NL_ACCUMULATE);
		setField(NL_AUTOREADY);
		setField(NL_AUTOSHOW);
		setField(NL_AFTERLOAD);
		setField(NL_AFTERPRESSED);
		setField(NL_AFTERSWEEP);
		setField(NL_AFTERUPDATE);
		setField(NY_ANCHOR);
		setField(NY_INSETS);
		setField(NY_IPAD);
		setField(NY_STATE);
		setField(NY_SAVEGRAPHICS);
		setField(NL_CLICKRADIUS);
		setField(NL_DRAGCOLOR);
		setField(NL_EMPTYCOLOR);
		setField(NL_HIGHLIGHTCOLOR);
		setField(NL_PRESSEDCOLOR);
		setField(NL_PRESSINGCOLOR);
		setField(NL_OTHERCOLOR);
		setField(NY_BORDERCOLOR);
		setField(NY_BORDER);
		setField(NL_SWEEPCOLOR);
		setField(NL_ZOOMINCOLOR);
		setField(NL_ZOOMOUTCOLOR);
		setField(NL_LABELFLAGS);
		setField(NL_LINEWIDTH);
		setField(NL_SELECTWIDTH);
		setField(NL_EDGEFLAGS);
		setField(NL_NODEFLAGS);
		setField(NL_SELECTFLAGS);
		setField(NL_EDGESCALE);
		setField(NL_FONTSCALE);
		setField(NL_NODESCALE);
		setField(NL_ZOOMSCALE);
		setField(NL_FILLMODEL);
		setField(NL_SEPARATOR);
		setField(NL_PAINTORDER);
		setField(NL_REVERSEPALETTE);
		setField(NL_PALETTE);
		setField(NL_PRIMARYFIELD);
		setField(NL_COLOREDBY);
		setField(NL_OPERATIONS);
		setField(NL_ZOOMDIRECTION);
		setField(NL_ACTIVE);
		setField(NL_ATTACHEDEDGESELECTION);
		setField(NL_TIPFLAGS);
		setField(NL_TIPLOCKMODEL);
		setField(NL_TIPOFFSET);
		setField(NL_TIPPREFIX);
		setField(NL_TIPSUFFIX);
		setField(NL_TIPDROPPED);
		setField(NL_TIPENABLED);
		setField(NL_OUTLINECACHESIZE);
		setField(NL_GRAPHLAYOUTMODEL);
		setField(NL_GRAPHLAYOUTARG);
		setField(NL_GRAPHLAYOUTSORT);
		setToConstant(NY_SIZE);
		break;

	    case JHISTOGRAM:
		peer = comp = new SwingJHistogram(getData(), this);
		setField(NL_ACCUMULATE);
		setField(NL_ALIVE);
		setField(NL_AUTOREADY);
		setField(NL_AUTOSCROLL);
		setField(NL_AUTOSHOW);
		setField(NL_AFTERLOAD);
		setField(NL_AFTERPRESSED);
		setField(NL_AFTERSWEEP);
		setField(NL_AFTERUPDATE);
		setField(NY_ALIGNMENT);
		setField(NY_ANCHOR);
		setField(NL_BARSPACE);
		setField(NY_INSETS);
		setField(NY_IPAD);
		setField(NY_STATE);
		setField(NY_SAVEGRAPHICS);
		setField(NL_EMPTYCOLOR);
		setField(NL_HIGHLIGHTCOLOR);
		setField(NL_PRESSEDCOLOR);
		setField(NL_PRESSINGCOLOR);
		setField(NL_OTHERCOLOR);
		setField(NY_BORDERCOLOR);
		setField(NY_BORDER);
		setField(NY_COLUMNS);
		setField(NY_ROWS);
		setField(NL_LABELFLAGS);
		setField(NL_SORTBY);
		setField(NL_SORTDEFAULT);
		setField(NL_TRANSIENTMODE);
		setField(NL_TRANSLATOR);
		setField(NL_REVERSEPALETTE);
		setField(NL_PALETTE);
		setField(NL_OPERATIONS);
		setField(NL_ACTIVE);
		setField(NL_HIDEUNLABELED);
		setField(NL_STACKED);
		setField(NL_TEXT);
		setToConstant(NY_SIZE);
		break;

	    default:
		VM.abort(UNIMPLEMENTED);
		break;
	}

	return(comp);
    }


    protected final YoixObject
    eventCoordinates(AWTEvent e) {

	YoixObject  coordinates;
	Object      comp;

	comp = this.peer;		// snapshot - just to be safe

	if (comp instanceof SwingJAxis)
	    coordinates = ((SwingJAxis)comp).eventCoordinates(e);
	else if (comp instanceof SwingJEventPlot)
	    coordinates = ((SwingJEventPlot)comp).eventCoordinates(e);
	else if (comp instanceof SwingDataViewer)
	    coordinates = ((SwingDataViewer)comp).eventCoordinates(e);
	else coordinates = null;

	return(coordinates);
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;
	Object      comp;

	comp = this.peer;		// snapshot - just to be safe

	switch (activeField(name, activefields)) {
	    case VL_CLEAR:
		obj = builtinClear(comp, name, argv);
		break;

	    case VL_COLLECTRECORDSAT:
		obj = builtinCollectRecordsAt(comp, name, argv);
		break;

	    case VL_FINDNEXTMATCH:
		obj = builtinFindNextMatch(comp, name, argv);
		break;

	    case VL_GETPOSITION:
		obj = builtinGetPosition(comp, name, argv);
		break;

	    case VL_GETTIPTEXTAT:
		obj = builtinGetTipTextAt(comp, name, argv);
		break;

	    case VL_GETZOOMENDS:
		obj = builtinGetZoomEnds(comp, name, argv);
		break;

	    case VL_LOADRECORDS:
		obj = builtinLoadRecords(comp, name, argv);
		break;

	    case VL_SETALL:
		obj = builtinSetAll(comp, name, argv);
		break;

	    case VL_SETPLOTENDS:
		obj = builtinSetPlotEnds(comp, name, argv);
		break;

	    case VL_SUBORDINATEPLOTS:
		obj = builtinSubordinatePlots(comp, name, argv);
		break;

	    case VL_ZOOM:
		obj = builtinZoom(comp, name, argv);
		break;

	    default:
		obj = super.executeField(name, argv);
		break;
	}

	return(obj);
    }


    protected final void
    finalize() {

	super.finalize();
    }


    protected YoixBodyMatrix
    getCTMBody() {

	return(super.getCTMBody());
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	Object  comp;

	comp = this.peer;		// snapshot - just to be safe

	switch (activeField(name, activefields)) {
	    case VL_ACCUMULATE:
		obj = getAccumulate(comp, obj);
		break;

	    case VL_ACTIVE:
		obj = getActive(comp, obj);
		break;

	    case VL_ACTIVEFIELDCOUNT:
		obj = getActiveFieldCount(comp, obj);
		break;

	    case VL_ALIVE:
		obj = getAlive(comp, obj);
		break;

	    case VL_AUTOSCROLL:
		obj = getAutoScroll(comp, obj);
		break;

	    case VL_AXISENDS:
		obj = getAxisEnds(comp, obj);
		break;

	    case VL_AXISLIMITS:
		obj = getAxisLimits(comp, obj);
		break;

	    case VL_CLEAR:
		obj = getClear(comp, obj);
		break;

	    case VL_COLOREDBY:
		obj = getColoredBy(comp, obj);
		break;

	    case VL_CONNECT:
		obj = getConnect(comp, obj);
		break;

	    case VL_CONNECTCOLOR:
		obj = getConnectColor(comp, obj);
		break;

	    case VL_CONNECTWIDTH:
		obj = getConnectWidth(comp, obj);
		break;

	    case VL_DATAENDS:
		obj = getDataEnds(comp, obj);
		break;

	    case VL_DATAMANAGER:
		obj = getDataManager(comp, obj);
		break;

	    case VL_DRAGCOLOR:
		obj = getDragColor(comp, obj);
		break;

	    case VL_EDGECOUNT:
		obj = getEdgeCount(comp, obj);
		break;

	    case VL_EDGES:
		obj = getEdges(comp, obj);
		break;

	    case VL_EMPTYCOLOR:
		obj = getEmptyColor(comp, obj);
		break;

	    case VL_FIELDINDEX:
		obj = getFieldIndex(comp, obj);
		break;

	    case VL_FIELDINDICES:
		obj = getFieldIndices(comp, obj);
		break;

	    case VL_FROZEN:
		obj = getFrozen(comp, obj);
		break;


	    case VL_GRAPHLAYOUTSORT:
		obj = getGraphLayoutSort(comp, obj);
		break;

	    case VL_GRAPHMATRIX:
		obj = getGraphMatrix(comp, obj);
		break;

	    case VL_HIGHLIGHTCOLOR:
		obj = getHighlightColor(comp, obj);
		break;

	    case VL_HIGHLIGHTED:
		obj = getHighlighted(comp, obj);
		break;

	    case VL_KEYS:
		obj = getKeys(comp, obj);
		break;

	    case VL_LABELS:
		obj = getLabels(comp, obj);
		break;

	    case VL_LINEWIDTH:
		obj = getLineWidth(comp, obj);
		break;

	    case VL_LOADEDENDS:
		obj = getLoadedEnds(comp, obj);
		break;

	    case VL_MOVED:
		obj = getMoved(comp, obj);
		break;

	    case VL_NODECOUNT:
		obj = getNodeCount(comp, obj);
		break;

	    case VL_NODES:
		obj = getNodes(comp, obj);
		break;

	    case VL_OFFPEAKCOLOR:
		obj = getOffPeakColor(comp, obj);
		break;

	    case VL_OPERATIONS:
		obj = getOperations(comp, obj);
		break;

	    case VL_ORIENTATION:
		obj = getOrientation(comp, obj);
		break;

	    case VL_OTHERCOLOR:
		obj = getOtherColor(comp, obj);
		break;

	    case VL_OUTLINECACHESIZE:
		obj = getOutlineCacheSize(comp, obj);
		break;

	    case VL_PLOT:
		obj = getPlot(comp, obj);
		break;

	    case VL_PLOTENDS:
		obj = getPlotEnds(comp, obj);
		break;

	    case VL_PLOTSTYLE:
		obj = getPlotStyle(comp, obj);
		break;

	    case VL_PLOTSTYLEFLAGS:
		obj = getPlotStyleFlags(comp, obj);
		break;

	    case VL_POINTSIZE:
		obj = getPointSize(comp, obj);
		break;

	    case VL_PRESSED:
		obj = getPressed(comp, obj);
		break;

	    case VL_PRESSEDCOLOR:
		obj = getPressedColor(comp, obj);
		break;

	    case VL_PRESSINGCOLOR:
		obj = getPressingColor(comp, obj);
		break;

	    case VL_SELECTED:
		obj = getSelected(comp, obj);
		break;

	    case VL_SHADETIMES:
		obj = getShadeTimes(comp, obj);
		break;

	    case VL_SLIDERCOLOR:
		obj = getSliderColor(comp, obj);
		break;

	    case VL_SLIDERENDS:
		obj = getSliderEnds(comp, obj);
		break;

	    case VL_SPREAD:
		obj = getSpread(comp, obj);
		break;

	    case VL_STACKED:
		obj = getStacked(comp, obj);
		break;

	    case VL_SWEEPCOLOR:
		obj = getSweepColor(comp, obj);
		break;

	    case VL_SWEEPFLAGS:
		obj = getSweepFlags(comp, obj);
		break;

	    case VL_TEXT:
		obj = getText(comp, obj);
		break;

	    case VL_TIPDROPPED:
		obj = getTipDropped(comp, obj);
		break;

	    case VL_TIPENABLED:
		obj = getTipEnabled(comp, obj);
		break;

	    case VL_TIPOFFSET:
		obj = getTipOffset(comp, obj);
		break;

	    case VL_ZOOMINCOLOR:
		obj = getZoomInColor(comp, obj);
		break;

	    case VL_ZOOMLIMIT:
		obj = getZoomLimit(comp, obj);
		break;

	    case VL_ZOOMOUTCOLOR:
		obj = getZoomOutColor(comp, obj);
		break;

	    case VL_ZOOMSCALE:
		obj = getZoomScale(comp, obj);
		break;

	    default:
		obj = super.getField(name, obj);
		break;
	}

	return(obj);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	Object  comp;

	comp = this.peer;		// snapshot - just to be safe

	if (comp != null && obj != null) {
	    switch (activeField(name, activefields)) {
		case VL_ACCUMULATE:
		    setAccumulate(comp, obj);
		    break;

		case VL_ACTIVE:
		    setActive(comp, obj);
		    break;

		case VL_AFTERAPPEND:
		    setAfterAppend(comp, obj);
		    break;

		case VL_AFTERLOAD:
		    setAfterLoad(comp, obj);
		    break;

		case VL_AFTERPRESSED:
		    setAfterPressed(comp, obj);
		    break;

		case VL_AFTERSWEEP:
		    setAfterSweep(comp, obj);
		    break;

		case VL_AFTERUPDATE:
		    setAfterUpdate(comp, obj);
		    break;

		case VL_ALIVE:
		    setAlive(comp, obj);
		    break;

		case VL_ANCHOR:
		    setAnchor(comp, obj);
		    break;

		case VL_ATTACHEDEDGESELECTION:
		    setAttachedEdgeSelection(comp, obj);
		    break;

		case VL_AUTOREADY:
		    setAutoReady(comp, obj);
		    break;

		case VL_AUTOSCROLL:
		    setAutoScroll(comp, obj);
		    break;

		case VL_AUTOSHOW:
		    setAutoShow(comp, obj);
		    break;

		case VL_AXISENDS:
		    setAxisEnds(comp, obj);
		    break;

		case VL_AXISMODEL:
		    setAxisModel(comp, obj);
		    break;

		case VL_AXISWIDTH:
		    setAxisWidth(comp, obj);
		    break;

		case VL_BARSPACE:
		    setBarSpace(comp, obj);
		    break;

		case VL_CLICKRADIUS:
		    setClickRadius(comp, obj);
		    break;

		case VL_COLOREDBY:
		    setColoredBy(comp, obj);
		    break;

		case VL_CONNECT:
		    setConnect(comp, obj);
		    break;

		case VL_CONNECTCOLOR:
		    setConnectColor(comp, obj);
		    break;

		case VL_CONNECTWIDTH:
		    setConnectWidth(comp, obj);
		    break;

		case VL_DRAGCOLOR:
		    setDragColor(comp, obj);
		    break;

		case VL_EDGEFLAGS:
		    setEdgeFlags(comp, obj);
		    break;

		case VL_EDGESCALE:
		    setEdgeScale(comp, obj);
		    break;

		case VL_EMPTYCOLOR:
		    setEmptyColor(comp, obj);
		    break;

		case VL_FILLMODEL:
		    setFillModel(comp, obj);
		    break;

		case VL_FONTSCALE:
		    setFontScale(comp, obj);
		    break;

		case VL_FROZEN:
		    setFrozen(comp, obj);
		    break;

		case VL_GRAPHLAYOUTARG:
		    setGraphLayoutArg(comp, obj);
		    break;

		case VL_GRAPHLAYOUTMODEL:
		    setGraphLayoutModel(comp, obj);
		    break;

		case VL_GRAPHLAYOUTSORT:
		    setGraphLayoutSort(comp, obj);
		    break;

		case VL_HIDEPOINTS:
		    setHidePoints(comp, obj);
		    break;

		case VL_HIDEUNLABELED:
		    setHideUnlabeled(comp, obj);
		    break;

		case VL_HIGHLIGHTCOLOR:
		    setHighlightColor(comp, obj);
		    break;

		case VL_HIGHLIGHTED:
		    setHighlighted(comp, obj);
		    break;

		case VL_IGNOREZERO:
		    setIgnoreZero(comp, obj);
		    break;

		case VL_INVERTED:
		    setInverted(comp, obj);
		    break;

		case VL_KEEPTALL:
		    setKeepTall(comp, obj);
		    break;

		case VL_LABELFLAGS:
		    setLabelFlags(comp, obj);
		    break;

		case VL_LABELS:
		    setLabels(comp, obj);
		    break;

		case VL_LINEWIDTH:
		    setLineWidth(comp, obj);
		    break;

		case VL_MODEL:
		    setModel(comp, obj);
		    break;

		case VL_MOVED:
		    setMoved(comp, obj);
		    break;

		case VL_NODEFLAGS:
		    setNodeFlags(comp, obj);
		    break;

		case VL_NODEOUTLINE:
		    setNodeOutline(comp, obj);
		    break;

		case VL_NODESCALE:
		    setNodeScale(comp, obj);
		    break;

		case VL_OFFPEAKCOLOR:
		    setOffPeakColor(comp, obj);
		    break;

		case VL_OPERATIONS:
		    setOperations(comp, obj);
		    break;

		case VL_OTHERCOLOR:
		    setOtherColor(comp, obj);
		    break;

		case VL_OUTLINECACHESIZE:
		    setOutlineCacheSize(comp, obj);
		    break;

		case VL_PAINTORDER:
		    setPaintOrder(comp, obj);
		    break;

		case VL_PALETTE:
		    setPalette(comp, obj);
		    break;

		case VL_PLOTENDS:
		    setPlotEnds(comp, obj);
		    break;

		case VL_PLOTSTYLE:
		    setPlotStyle(comp, obj);
		    break;

		case VL_PLOTSTYLEFLAGS:
		    setPlotStyleFlags(comp, obj);
		    break;

		case VL_POINTSIZE:
		    setPointSize(comp, obj);
		    break;

		case VL_PRESSED:
		    setPressed(comp, obj);
		    break;

		case VL_PRESSEDCOLOR:
		    setPressedColor(comp, obj);
		    break;

		case VL_PRESSINGCOLOR:
		    setPressingColor(comp, obj);
		    break;

		case VL_PRIMARYFIELD:
		    setPrimaryField(comp, obj);
		    break;

		case VL_RANKPREFIX:
		    setRankPrefix(comp, obj);
		    break;

		case VL_RANKSUFFIX:
		    setRankSuffix(comp, obj);
		    break;

		case VL_RECOLORED:
		    setRecolored(comp, obj);
		    break;

		case VL_REVERSEPALETTE:
		    setReversePalette(comp, obj);
		    break;

		case VL_SELECTED:
		    setSelected(comp, obj);
		    break;

		case VL_SELECTFLAGS:
		    setSelectFlags(comp, obj);
		    break;

		case VL_SELECTWIDTH:
		    setSelectWidth(comp, obj);
		    break;

		case VL_SEPARATOR:
		    setSeparator(comp, obj);
		    break;

		case VL_SHADETIMES:
		    setShadeTimes(comp, obj);
		    break;

		case VL_SLIDERCOLOR:
		    setSliderColor(comp, obj);
		    break;

		case VL_SLIDERENABLED:
		    setSliderEnabled(comp, obj);
		    break;

		case VL_SLIDERENDS:
		    setSliderEnds(comp, obj);
		    break;

		case VL_SORTBY:
		    setSortBy(comp, obj);
		    break;

		case VL_SORTDEFAULT:
		    setSortDefault(comp, obj);
		    break;

		case VL_SPREAD:
		    setSpread(comp, obj);
		    break;

		case VL_STACKED:
		    setStacked(comp, obj);
		    break;

		case VL_SWEEPCOLOR:
		    setSweepColor(comp, obj);
		    break;

		case VL_SWEEPFLAGS:
		    setSweepFlags(comp, obj);
		    break;

		case VL_SYMMETRIC:
		    setSymmetric(comp, obj);
		    break;

		case VL_TEXT:
		    setText(comp, obj);
		    break;

		case VL_TICKS:
		    setTicks(comp, obj);
		    break;

		case VL_TIMESHADING:
		    setTimeShading(comp, obj);
		    break;

		case VL_TIPDROPPED:
		    setTipDropped(comp, obj);
		    break;

		case VL_TIPENABLED:
		    setTipEnabled(comp, obj);
		    break;

		case VL_TIPFLAGS:
		    setTipFlags(comp, obj);
		    break;

		case VL_TIPLOCKMODEL:
		    setTipLockModel(comp, obj);
		    break;

		case VL_TIPOFFSET:
		    setTipOffset(comp, obj);
		    break;

		case VL_TIPPREFIX:
		    setTipPrefix(comp, obj);
		    break;

		case VL_TIPSUFFIX:
		    setTipSuffix(comp, obj);
		    break;

		case VL_TRANSIENTMODE:
		    setTransientMode(comp, obj);
		    break;

		case VL_TRANSLATOR:
		    setTranslator(comp, obj);
		    break;

		case VL_UNIXTIME:
		    setUnixTime(comp, obj);
		    break;

		case VL_XAXIS:
		    setXAxis(comp, obj);
		    break;

		case VL_YAXIS:
		    setYAxis(comp, obj);
		    break;

		case VL_ZOOMDIRECTION:
		    setZoomDirection(comp, obj);
		    break;

		case VL_ZOOMINCOLOR:
		    setZoomInColor(comp, obj);
		    break;

		case VL_ZOOMOUTCOLOR:
		    setZoomOutColor(comp, obj);
		    break;

		case VL_ZOOMSCALE:
		    setZoomScale(comp, obj);
		    break;

		default:
		    super.setField(name, obj);
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
    builtinClear(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	ArrayList   list;
	boolean     selected;
	Point       point;

	if (comp instanceof SwingDataViewer || comp instanceof SwingJDataTable) {
	    if (arg.length == 0 || arg.length == 1) {
		if (arg.length == 0) {
		    if (comp instanceof SwingDataViewer)
			((SwingDataViewer)comp).clear();
		    else if (comp instanceof SwingJDataTable)
			((SwingJDataTable)comp).clear();
		} else {
		    if (arg[0].isNumber()) {
			if (comp instanceof SwingDataViewer)
			    ((SwingDataViewer)comp).clear(arg[0].booleanValue());
			else ((SwingJDataTable)comp).clear(arg[0].booleanValue());
		    } else VM.badArgument(name, 0);
		}
		obj = YoixObject.newEmpty();
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinCollectRecordsAt(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	ArrayList   list;
	boolean     selected;
	Point       point;

	if (comp instanceof DataViewer || comp instanceof DataPlot) {
	    if (arg.length == 1 || arg.length == 2) {
		if (arg[0].isPoint()) {
		    if (arg.length == 1 || arg[1].isNumber()) {
			point = YoixMakeScreen.javaPoint(arg[0]);
			selected = (arg.length == 1) ? true : arg[1].booleanValue();
			if (comp instanceof DataViewer)
			    list = ((DataViewer)comp).collectRecordsAt(point, selected);
			else list = ((DataPlot)comp).collectRecordsAt(point, selected);
			obj = YoixMisc.copyIntoArray(list);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinFindNextMatch(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	Point2D     point;
	boolean     bpattern;
	boolean     ignorecase;
	boolean     bycols;
	boolean     forward;
	String      key;
	int         ipattern;
	int         direction;

	if (comp instanceof SwingJHistogram) {
	    if (arg.length >= 1 && arg.length <= 4) {
		if (arg[0].isString()) {
		    if (arg.length < 2 || arg[1].isInteger()) {
			if (arg.length < 3 || arg[2].isInteger()) {
			    if (arg.length < 4 || arg[3].isInteger()) {
				bpattern = (arg.length > 1) ? arg[1].booleanValue() : false;
				ignorecase = (arg.length > 2) ? arg[2].booleanValue() : true;
				direction = (arg.length > 3) ? arg[3].intValue() : 1;
				key = ((SwingJHistogram)comp).findNextMatch(
				    arg[0].stringValue(),
				    bpattern,
				    ignorecase,
				    direction
				);
				obj = YoixObject.newString(key);
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	} else if (comp instanceof SwingJDataTable) {
	    if (arg.length >= 1 && arg.length <= 5) {
		if (arg[0].isString()) {
		    if (arg.length < 2 || arg[1].isInteger()) {
			if (arg.length < 3 || arg[2].isInteger()) {
			    if (arg.length < 4 || arg[3].isInteger()) {
				if (arg.length < 5 || arg[4].isInteger()) {
				    ipattern = (arg.length > 1) ? arg[1].intValue() : 0;
				    ignorecase = (arg.length > 2) ? arg[2].booleanValue() : true;
				    bycols = (arg.length > 2) ? arg[3].booleanValue() : false;
				    forward = (arg.length > 3) ? arg[4].booleanValue() : true;
				    key = ((SwingJDataTable)comp).localFindNextMatch(
					arg[0].stringValue(),
					ipattern,
					ignorecase,
					bycols,
					forward
				    );
				    obj = YoixObject.newString(key);
				} else VM.badArgument(name, 4);
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else if (arg.length == 0)
		((SwingJDataTable)comp).localFindClearSelection();
	    else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinGetPosition(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	Point2D     point;

	if (comp instanceof SwingJGraphPlot) {
	    if (arg.length == 1) {
		if (arg[0].isString() || arg[0].isNull()) {
		    if (arg[0].notNull()) {
			point = ((SwingJGraphPlot)comp).getPosition(arg[0].stringValue());
			obj = YoixObject.newPoint(point);
		    } else obj = YoixObject.newPoint();
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinGetTipTextAt(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	boolean     html;
	String      text = null;
	Point       point;
	int         flags;

	if (comp instanceof DataColorer || comp instanceof DataPlot) {
	    if (arg.length == 1 || arg.length == 2 || arg.length == 3) {
		if (arg[0].isPoint()) {
		    if (arg.length <= 1 || arg[1].isNumber()) {
			if (arg.length <= 2 || arg[2].isNumber()) {
			    point = YoixMakeScreen.javaPoint(arg[0]);
			    flags = (arg.length <= 1) ? 0 : arg[1].intValue();
			    html = (arg.length <= 2) ? true : arg[2].booleanValue();
			    if (comp instanceof DataColorer)
				text = ((DataColorer)comp).getTipTextAt(point, flags, html);
			    else text = ((DataPlot)comp).getTipTextAt(point, flags, html);
			    obj = YoixObject.newString(text);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinGetZoomEnds(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	double      ends[];

	if (comp instanceof SwingJAxis) {
	    if (arg.length == 2) {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			ends = ((SwingJAxis)comp).getZoomEnds(
			    arg[0].intValue(),
			    arg[1].doubleValue()
			);
			obj = YoixMisc.copyIntoArray(ends);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinLoadRecords(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	boolean     accumulating;

	if (comp instanceof SweepFilter) {
	    if (arg.length == 1 || arg.length == 2) {
		if (arg[0].isArray()) {
		    if (arg.length == 1 || arg[1].isNumber()) {
			if (arg.length > 1)
			    accumulating = arg[1].booleanValue();
			else accumulating = ((SweepFilter)comp).getAccumulate();
			((SweepFilter)comp).loadRecords(arg[0], accumulating);
			obj = YoixObject.newEmpty();
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinSetAll(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (comp instanceof SwingDataViewer || comp instanceof SwingJDataTable) {
	    if (arg.length == 1 || arg.length == 2) {
		if (arg[0].isNumber()) {
		    if (arg.length == 2) {
			if (arg[1].isArray() || arg[1].isNull()) {
			    if (comp instanceof SwingDataViewer)
				((SwingDataViewer)comp).setAll(arg[0].booleanValue(), arg[1]);
			    else ((SwingJDataTable)comp).setAll(arg[0].booleanValue(), arg[1]);
			} else VM.badArgument(name, 1);
		    } else {
			if (comp instanceof SwingDataViewer)
			    ((SwingDataViewer)comp).setAll(arg[0].booleanValue());
			else ((SwingJDataTable)comp).setAll(arg[0].booleanValue());
		    }
		    obj = YoixObject.newEmpty();
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinSetPlotEnds(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (comp instanceof SwingJEventPlot) {
	    if (arg.length == 1 || arg.length == 2) {
		if (arg[0].isDictionary()) {
		    if (arg.length == 1 || arg[1].isNumber()) {
			if (arg[0].notNull()) {
			    ((SwingJEventPlot)comp).setPlotEnds(
				arg[0],
				arg.length == 1 ? 0 : arg[1].intValue()
			    );
			}
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	    obj = YoixObject.newEmpty();
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinSubordinatePlots(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	ArrayList   result;
	ArrayList   xaxis;
	ArrayList   yaxis;

	if (comp instanceof SwingJAxis) {
	    if (arg.length == 0) {
		if ((result = ((SwingJAxis)comp).getSubordinatePlots()) != null) {
		    obj = YoixObject.newDictionary(2);
		    xaxis = (ArrayList)result.get(0);
		    yaxis = (ArrayList)result.get(1);
		    obj.put(NL_XAXIS, YoixMisc.copyIntoArray(xaxis, xaxis), false);
		    obj.put(NL_YAXIS, YoixMisc.copyIntoArray(yaxis, yaxis), false);
		} else obj = YoixObject.newArray();
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinZoom(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	double      scale;

	if (comp instanceof SwingJGraphPlot) {
	    if (arg.length == 1 || arg.length == 2) {
		if (arg.length == 1) {
		    if (arg[0].isNumber())
			((SwingJGraphPlot)comp).zoom(arg[0].doubleValue());
		    else VM.badArgument(name, 0);
		} else {
		    if (arg[0].isNumber()) {
			scale = arg[0].doubleValue();
			if (arg[1].isPoint() || arg[1].isNull()) {
			    if (arg[1].notNull())
				((SwingJGraphPlot)comp).zoom(scale, YoixMakeScreen.javaPoint(arg[1]));
			    else ((SwingJGraphPlot)comp).zoom(scale);
			} else VM.badArgument(name, 1);
		    } else VM.badArgument(name, 0);
		}
		obj = YoixObject.newEmpty();
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private YoixObject
    getAccumulate(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = YoixObject.newInt(((SwingDataViewer)comp).getAccumulate());
	else if (comp instanceof SwingJDataTable)
	    obj = YoixObject.newInt(((SwingJDataTable)comp).getAccumulate());
	return(obj);
    }


    private YoixObject
    getActive(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = YoixObject.newInt(((SwingDataViewer)comp).getActive());
	else if (comp instanceof SwingJDataTable)
	    obj = YoixObject.newInt(((SwingJDataTable)comp).getActive());
	return(obj);
    }


    private YoixObject
    getActiveFieldCount(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = YoixObject.newInt(((SwingDataViewer)comp).getActiveFieldCount());
	else if (comp instanceof SwingJDataTable)
	    obj = YoixObject.newInt(((SwingJDataTable)comp).getActiveFieldCount());
	return(obj);
    }


    private YoixObject
    getAlive(Object comp, YoixObject obj) {

	if (comp instanceof SwingJHistogram)
	    obj = YoixObject.newInt(((SwingJHistogram)comp).getAlive());
	else if (comp instanceof SwingJEventPlot)
	    obj = YoixObject.newInt(((SwingJEventPlot)comp).getAlive());
	else if (comp instanceof SwingJGraphPlot)
	    obj = YoixObject.newInt(((SwingJGraphPlot)comp).getAlive());
	else if (comp instanceof SwingJDataTable)
	    obj = YoixObject.newInt(((SwingJDataTable)comp).getAlive());
	return(obj);
    }


    private YoixObject
    getAutoScroll(Object comp, YoixObject obj) {

	if (comp instanceof SwingJHistogram)
	    obj = YoixObject.newInt(((SwingJHistogram)comp).getAutoScroll());
	else if (comp instanceof SwingJDataTable)
	    obj = YoixObject.newInt(((SwingJDataTable)comp).getAutoScroll());
	return(obj);
    }


    private YoixObject
    getAxisEnds(Object comp, YoixObject obj) {

	double  ends[];

	if (comp instanceof SwingJAxis) {
	    if ((ends = ((SwingJAxis)comp).getAxisEnds()) != null) {
		obj = YoixObject.newArray(2);
		obj.putDouble(0, ends[0]);
		obj.putDouble(1, ends[1]);
	    } else obj = YoixObject.newArray();
	}
	return(obj);
    }


    private YoixObject
    getAxisLimits(Object comp, YoixObject obj) {

	double  ends[];

	if (comp instanceof SwingJAxis) {
	    if ((ends = ((SwingJAxis)comp).getAxisLimits()) != null) {
		obj = YoixObject.newArray(2);
		obj.putDouble(0, ends[0]);
		obj.putDouble(1, ends[1]);
	    } else obj = YoixObject.newArray();
	}
	return(obj);
    }


    private YoixObject
    getClear(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = YoixObject.newInt(((SwingDataViewer)comp).isClear());
	else if (comp instanceof SwingJDataTable)
	    obj = YoixObject.newInt(((SwingJDataTable)comp).isClear());
	return(obj);
    }


    private YoixObject
    getColoredBy(Object comp, YoixObject obj) {

	SwingDataColorer  coloredby;

	if (comp instanceof SwingDataViewer) {
	    if ((coloredby = ((SwingDataViewer)comp).getColoredBy()) != null)
		obj = coloredby.getContext();
	    else obj = YoixObject.newNull();
	}

	return(obj);
    }


    private YoixObject
    getConnect(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = YoixObject.newInt(((SwingJEventPlot)comp).getConnect());
	return(obj);
    }


    private YoixObject
    getConnectColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = YoixMake.yoixColor(((SwingJEventPlot)comp).getConnectColor());
	return(obj);
    }


    private YoixObject
    getConnectWidth(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = YoixObject.newDouble(YoixMakeScreen.yoixDistance(((SwingJEventPlot)comp).getConnectWidth()));
	return(obj);
    }


    private YoixObject
    getDataEnds(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = ((SwingJEventPlot)comp).getDataEnds();
	return(obj);
    }


    private YoixObject
    getDataManager(Object comp, YoixObject obj) {

	DataManager  manager;

	if (comp instanceof SwingJEventPlot) {
	    manager = ((SwingJEventPlot)comp).getDataManager();
	    obj = (manager != null) ? manager.getContext() : YoixObject.newNull();
	} else if (comp instanceof SwingDataViewer) {
	    manager = ((SwingDataViewer)comp).getDataManager();
	    obj = (manager != null) ? manager.getContext() : YoixObject.newNull();
	} else if (comp instanceof SwingJDataTable) {
	    manager = ((SwingJDataTable)comp).getDataManager();
	    obj = (manager != null) ? manager.getContext() : YoixObject.newNull();
	}

	return(obj);
    }


    private YoixObject
    getDragColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    obj = YoixMake.yoixColor(((SwingJGraphPlot)comp).getDragColor());
	return(obj);
    }


    private YoixObject
    getEdgeCount(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    obj = YoixObject.newInt(((SwingJGraphPlot)comp).getEdgeCount());
	return(obj);
    }


    private YoixObject
    getEdges(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    obj = ((SwingJGraphPlot)comp).getEdges();
	return(obj);
    }


    private YoixObject
    getEmptyColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = YoixMake.yoixColor(((SwingDataViewer)comp).getEmptyColor());
	else if (comp instanceof SwingJDataTable)
	    obj = YoixMake.yoixColor(((SwingJDataTable)comp).getEmptyColor());
	return(obj);
    }


    private YoixObject
    getFieldIndex(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = YoixObject.newInt(((SwingDataViewer)comp).getFieldIndex());
	else if (comp instanceof SwingJDataTable)
	    obj = YoixObject.newInt(((SwingJDataTable)comp).getFieldIndex());
	return(obj);
    }


    private YoixObject
    getFieldIndices(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = YoixMisc.copyIntoArray(((SwingDataViewer)comp).getFieldIndices());
	else if (comp instanceof SwingJDataTable)
	    obj = YoixMisc.copyIntoArray(((SwingJDataTable)comp).getFieldIndices());
	return(obj);
    }


    private YoixObject
    getFrozen(Object comp, YoixObject obj) {

	if (comp instanceof SwingJAxis)
	    obj = YoixObject.newInt(((SwingJAxis)comp).getFrozen());
	else if (comp instanceof SwingDataColorer)
	    obj = YoixObject.newInt(((SwingDataColorer)comp).getFrozen());
	else if (comp instanceof SwingDataPlot)
	    obj = YoixObject.newInt(((SwingDataPlot)comp).getFrozen());
	return(obj);
    }


    private YoixObject
    getGraphLayoutSort(Object comp, YoixObject obj) {


	if (comp instanceof SwingJGraphPlot)
	    obj = YoixMisc.copyIntoArray(((SwingJGraphPlot)comp).getGraphLayoutSort());
	return(obj);
    }


    private YoixObject
    getGraphMatrix(Object comp, YoixObject obj) {

	AffineTransform  transform;

	//
	// Returns a matrix that transforms points in the graph to points
	// in the default Yoix coordinate system.
	//

	if (comp instanceof SwingJGraphPlot) {
	    if ((transform = ((SwingJGraphPlot)comp).getGraphMatrix()) != null) {
		try {
		    transform.preConcatenate(VM.getDefaultTransform().createInverse());
		}
		catch(NoninvertibleTransformException e) {}
	    }
	    obj = YoixObject.newMatrix(transform);
	}
	return(obj);
    }


    private YoixObject
    getHighlightColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = YoixMake.yoixColor(((SwingDataViewer)comp).getHighlightColor());
	else if (comp instanceof SwingJDataTable)
	    obj = YoixMake.yoixColor(((SwingJDataTable)comp).getHighlightColor());
	return(obj);
    }


    private YoixObject
    getHighlighted(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = ((SwingDataViewer)comp).getHighlighted();
	else if (comp instanceof SwingJDataTable)
	    obj = ((SwingJDataTable)comp).getHighlighted();
	return(obj);
    }


    private synchronized YoixObject
    getKeys(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = ((SwingDataViewer)comp).getKeys();
	else if (comp instanceof SwingJDataTable)
	    obj = ((SwingJDataTable)comp).getKeys();
	return(obj);
    }


    private synchronized YoixObject
    getLabels(Object comp, YoixObject obj) {

	if (comp instanceof SwingJHistogram)
	    obj = ((SwingJHistogram)comp).getLabels();
	return(obj);
    }


    private YoixObject
    getLineWidth(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = YoixObject.newDouble(YoixMakeScreen.yoixDistance(((SwingJEventPlot)comp).getLineWidth()));
	else if (comp instanceof SwingJAxis)
	    obj = YoixObject.newDouble(YoixMakeScreen.yoixDistance(((SwingJAxis)comp).getLineWidth()));
	else if (comp instanceof SwingJGraphPlot)
	    obj = YoixObject.newDouble(YoixMakeScreen.yoixDistance(((SwingJGraphPlot)comp).getLineWidth()));
	return(obj);
    }


    private YoixObject
    getLoadedEnds(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = ((SwingJEventPlot)comp).getLoadedEnds();
	return(obj);
    }


    private YoixObject
    getMoved(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    obj = ((SwingJGraphPlot)comp).getMoved();
	return(obj);
    }


    private YoixObject
    getNodeCount(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    obj = YoixObject.newInt(((SwingJGraphPlot)comp).getNodeCount());
	return(obj);
    }


    private YoixObject
    getNodes(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    obj = ((SwingJGraphPlot)comp).getNodes();
	return(obj);
    }


    private YoixObject
    getOffPeakColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = YoixMake.yoixColor(((SwingJEventPlot)comp).getOffPeakColor());
	return(obj);
    }


    private YoixObject
    getOperations(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = ((SwingDataViewer)comp).getOperations();
	else if (comp instanceof SwingJDataTable)
	    obj = ((SwingJDataTable)comp).getOperations();
	return(obj);
    }


    private YoixObject
    getOrientation(Object comp, YoixObject obj) {

	if (comp instanceof SwingJAxis)
	    obj = YoixObject.newInt(((SwingJAxis)comp).getOrientation());
	return(obj);
    }


    private YoixObject
    getOtherColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataColorer)
	    obj = YoixMake.yoixColor(((SwingDataColorer)comp).getOtherColor());
	return(obj);
    }


    private YoixObject
    getOutlineCacheSize(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    obj = YoixObject.newInt(((SwingJGraphPlot)comp).getOutlineCacheSize());
	return(obj);
    }


    private YoixObject
    getPlot(Object comp, YoixObject obj) {

	if (comp instanceof SwingJAxis)
	    obj = ((SwingJAxis)comp).getPlot();
	return(obj);
    }


    private YoixObject
    getPlotEnds(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = ((SwingJEventPlot)comp).getPlotEnds();
	return(obj);
    }


    private YoixObject
    getPlotStyle(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = ((SwingJEventPlot)comp).getPlotStyle();
	return(obj);
    }


    private YoixObject
    getPlotStyleFlags(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = YoixObject.newInt(((SwingJEventPlot)comp).getPlotStyleFlags());
	return(obj);
    }


    private YoixObject
    getPointSize(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = YoixObject.newDouble(YoixMakeScreen.yoixDistance(((SwingJEventPlot)comp).getPointSize()));
	return(obj);
    }


    private YoixObject
    getPressed(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = ((SwingDataViewer)comp).getPressed();
	else if (comp instanceof SwingJDataTable)
	    obj = ((SwingJDataTable)comp).getPressed();
	return(obj);
    }


    private YoixObject
    getPressedColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = YoixMake.yoixColor(((SwingDataViewer)comp).getPressedColor());
	else if (comp instanceof SwingJDataTable)
	    obj = YoixMake.yoixColor(((SwingJDataTable)comp).getPressedColor());
	return(obj);
    }


    private YoixObject
    getPressingColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = YoixMake.yoixColor(((SwingDataViewer)comp).getPressingColor());
	else if (comp instanceof SwingJDataTable)
	    obj = YoixMake.yoixColor(((SwingJDataTable)comp).getPressingColor());
	return(obj);
    }


    private YoixObject
    getSelected(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = ((SwingDataViewer)comp).getSelected();
	else if (comp instanceof SwingJDataTable)
	    obj = ((SwingJDataTable)comp).getSelected();
	return(obj);
    }


    private YoixObject
    getShadeTimes(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = ((SwingJEventPlot)comp).getShadeTimes();
	return(obj);
    }


    private YoixObject
    getSliderColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingJAxis)
	    obj = YoixMake.yoixColor(((SwingJAxis)comp).getSliderColor());
	return(obj);
    }


    private YoixObject
    getSliderEnds(Object comp, YoixObject obj) {

	double  ends[];

	if (comp instanceof SwingJAxis) {
	    if ((ends = ((SwingJAxis)comp).getSliderEnds()) != null) {
		obj = YoixObject.newArray(2);
		obj.putDouble(0, ends[0]);
		obj.putDouble(1, ends[1]);
	    } else obj = YoixObject.newArray();
	}

	return(obj);
    }


    private YoixObject
    getSpread(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = YoixObject.newInt(((SwingJEventPlot)comp).getSpread());
	return(obj);
    }


    private YoixObject
    getStacked(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = YoixObject.newInt(((SwingJEventPlot)comp).getStacked());
	else if (comp instanceof SwingJHistogram)
	    obj = YoixObject.newInt(((SwingJHistogram)comp).getStacked());
	return(obj);
    }


    private YoixObject
    getSweepColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = YoixMake.yoixColor(((SwingJEventPlot)comp).getSweepColor());
	else if (comp instanceof SwingDataViewer)
	    obj = YoixMake.yoixColor(((SwingDataViewer)comp).getSweepColor());
	else if (comp instanceof SwingJDataTable)
	    obj = YoixMake.yoixColor(((SwingJDataTable)comp).getSweepColor());
	return(obj);
    }


    private YoixObject
    getSweepFlags(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    obj = YoixObject.newInt(((SwingJEventPlot)comp).getSweepFlags());
	return(obj);
    }


    private YoixObject
    getText(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    obj = YoixObject.newString(((SwingDataViewer)comp).getText());
	return(obj);
    }


    private YoixObject
    getTipDropped(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    obj = YoixObject.newInt(((SwingJGraphPlot)comp).getTipDropped());
	else if (comp instanceof SwingJEventPlot)
	    obj = YoixObject.newInt(((SwingJEventPlot)comp).getTipDropped());
	return(obj);
    }


    private YoixObject
    getTipEnabled(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    obj = YoixObject.newInt(((SwingJGraphPlot)comp).getTipEnabled());
	else if (comp instanceof SwingJEventPlot)
	    obj = YoixObject.newInt(((SwingJEventPlot)comp).getTipEnabled());
	return(obj);
    }


    private YoixObject
    getTipOffset(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    obj = YoixMakeScreen.yoixPoint(((SwingJGraphPlot)comp).getTipOffset());
	else if (comp instanceof SwingJEventPlot)
	    obj = YoixMakeScreen.yoixPoint(((SwingJEventPlot)comp).getTipOffset());
	return(obj);
    }


    private YoixObject
    getZoomInColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    obj = YoixMake.yoixColor(((SwingJGraphPlot)comp).getZoomInColor());
	return(obj);
    }


    private YoixObject
    getZoomLimit(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    obj = YoixObject.newDouble(((SwingJGraphPlot)comp).getZoomLimit());
	return(obj);
    }


    private YoixObject
    getZoomOutColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    obj = YoixMake.yoixColor(((SwingJGraphPlot)comp).getZoomOutColor());
	return(obj);
    }


    private YoixObject
    getZoomScale(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    obj = YoixObject.newDouble(((SwingJGraphPlot)comp).getZoomScale());
	return(obj);
    }


    private void
    setAccumulate(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setAccumulate(obj.booleanValue());
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setAccumulate(obj.booleanValue());
    }


    private void
    setActive(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setActive(obj.intValue());
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setActive(obj.intValue());
    }


    private void
    setAfterAppend(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setAfterAppend(obj);
    }


    private void
    setAfterLoad(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setAfterLoad(obj);
	else if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setAfterLoad(obj);
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setAfterLoad(obj);
    }


    private void
    setAfterPressed(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setAfterPressed(obj);
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setAfterPressed(obj);
    }


    private void
    setAfterSweep(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataPlot)
	    ((SwingDataPlot)comp).setAfterSweep(obj);
	else if (comp instanceof SwingDataColorer)
	    ((SwingDataViewer)comp).setAfterSweep(obj);
    }


    private void
    setAfterUpdate(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setAfterUpdate(obj);
	else if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setAfterUpdate(obj);
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setAfterUpdate(obj);
    }


    private void
    setAlive(Object comp, YoixObject obj) {

	if (comp instanceof SwingJHistogram)
	    ((SwingJHistogram)comp).setAlive(obj.booleanValue());
	else if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setAlive(obj.booleanValue());
	else if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setAlive(obj.booleanValue());
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setAlive(obj.booleanValue());
    }


    private void
    setAnchor(Object comp, YoixObject obj) {

	if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setAnchor(obj.intValue());
	else super.setField(N_ANCHOR, obj);
    }


    private void
    setAttachedEdgeSelection(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setAttachedEdgeSelection(obj);
    }


    private void
    setAutoReady(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setAutoReady(obj.booleanValue());
	else if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setAutoReady(obj.booleanValue());
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setAutoReady(obj.booleanValue());
    }


    private void
    setAutoScroll(Object comp, YoixObject obj) {

	if (comp instanceof SwingJHistogram)
	    ((SwingJHistogram)comp).setAutoScroll(obj.booleanValue());
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setAutoScroll(obj.booleanValue());
    }


    private void
    setAutoShow(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setAutoShow(obj.booleanValue());
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setAutoShow(obj.booleanValue());
    }


    private void
    setAxisEnds(Object comp, YoixObject obj) {

	double  ends[];
	int     offset;

	if (comp instanceof SwingJAxis) {
	    if (obj.notNull()) {
		offset = obj.offset();
		if (obj.sizeof() >= 2) {
		    ends = new double[] {
			obj.get(offset, false).doubleValue(),
			obj.get(offset+1, false).doubleValue()
		    };
		} else if (obj.sizeof() == 0)
		    ends = ((SwingJAxis)comp).getAxisLimits();
		else ends = null;
		if (ends != null)
		    ((SwingJAxis)comp).setAxisEnds(ends[0], ends[1]);
	    } else ((SwingJAxis)comp).setAxisEnds();
	}
    }


    private void
    setAxisModel(Object comp, YoixObject obj) {

	if (comp instanceof SwingJAxis)
	    ((SwingJAxis)comp).setAxisModel(obj.intValue());
    }


    private void
    setAxisWidth(Object comp, YoixObject obj) {

	double  width;

	if (comp instanceof SwingJEventPlot) {
	    if ((width = obj.doubleValue()) > 0)
		((SwingJEventPlot)comp).setAxisWidth(YoixMakeScreen.javaDistance(width + 0.5));
	    else ((SwingJEventPlot)comp).setAxisWidth(0);
	} else if (comp instanceof SwingJAxis) {
	    if ((width = obj.doubleValue()) > 0)
		((SwingJAxis)comp).setAxisWidth(YoixMakeScreen.javaDistance(width + 0.5));
	    else ((SwingJAxis)comp).setAxisWidth(0);
	}
    }


    private void
    setBarSpace(Object comp, YoixObject obj) {

	if (comp instanceof SwingJHistogram)
	    ((SwingJHistogram)comp).setBarSpace(obj.doubleValue());
    }


    private void
    setClickRadius(Object comp, YoixObject obj) {

	double  radius;

	if (comp instanceof SwingJGraphPlot) {
	    if ((radius = obj.doubleValue()) > 0)
		((SwingJGraphPlot)comp).setClickRadius(YoixMakeScreen.javaDistance(radius + 0.5));
	    else ((SwingJGraphPlot)comp).setClickRadius(0);
	}
    }


    private synchronized void
    setColoredBy(Object comp, YoixObject obj) {

	Object  colorer;

	if (comp instanceof SwingDataViewer) {
	    colorer = getManagedObject(obj);
	    if (colorer instanceof SwingDataColorer)
		((SwingDataViewer)comp).setColoredBy((SwingDataColorer)colorer);
	    else if (obj.isNull())
		((SwingDataViewer)comp).setColoredBy(null);
	    else VM.abort(TYPECHECK, NL_COLOREDBY);
	}
    }


    private void
    setConnect(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setConnect(obj.intValue());
    }


    private void
    setConnectColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setConnectColor(YoixMake.javaColor(obj));
    }


    private void
    setConnectWidth(Object comp, YoixObject obj) {

	double  width;

	if (comp instanceof SwingJEventPlot) {
	    if ((width = obj.doubleValue()) > 0)
		((SwingJEventPlot)comp).setConnectWidth(YoixMakeScreen.javaDistance(width + 0.5));
	    else ((SwingJEventPlot)comp).setConnectWidth(0);
	}
    }


    private void
    setDragColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setDragColor(YoixMake.javaColor(obj));
    }


    private void
    setEdgeFlags(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setEdgeFlags(obj.intValue());
    }


    private void
    setEdgeScale(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setEdgeScale(obj.doubleValue());
    }


    private void
    setEmptyColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setEmptyColor(YoixMake.javaColor(obj));
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setEmptyColor(YoixMake.javaColor(obj));
    }


    private void
    setFillModel(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setFillModel(obj.intValue());
    }


    private void
    setFontScale(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setFontScale(obj.doubleValue());
    }


    private void
    setFrozen(Object comp, YoixObject obj) {

	if (comp instanceof SwingJAxis)
	    ((SwingJAxis)comp).setFrozen(obj.booleanValue());
	else if (comp instanceof SwingDataColorer)
	    ((SwingDataColorer)comp).setFrozen(obj.booleanValue());
	else if (comp instanceof SwingDataPlot)
	    ((SwingDataPlot)comp).setFrozen(obj.booleanValue());
    }


    private void
    setGraphLayoutArg(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setGraphLayoutArg(obj);
    }


    private void
    setGraphLayoutModel(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot) {
	    if (obj.isNumber())
		((SwingJGraphPlot)comp).setGraphLayoutModel(obj.intValue());
	    else if (obj.isString())
		((SwingJGraphPlot)comp).setGraphLayoutModel(obj.stringValue());
	    else if (obj.isNull())
		((SwingJGraphPlot)comp).setGraphLayoutModel(null);
	    else VM.abort(TYPECHECK, NL_GRAPHLAYOUTMODEL);
	}
    }


    private void
    setGraphLayoutSort(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setGraphLayoutSort(obj);
    }


    private void
    setHidePoints(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setHidePoints(obj.booleanValue());
    }


    private void
    setHideUnlabeled(Object comp, YoixObject obj) {

	if (comp instanceof SwingJHistogram)
	    ((SwingJHistogram)comp).setHideUnlabeled(obj.booleanValue());
    }


    private void
    setHighlightColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setHighlightColor(YoixMake.javaColor(obj));
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setHighlightColor(YoixMake.javaColor(obj));
    }


    private void
    setHighlighted(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setHighlighted(obj);
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setHighlighted(obj);
    }


    private void
    setIgnoreZero(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setIgnoreZero(obj.booleanValue());
    }


    private void
    setInverted(Object comp, YoixObject obj) {

	if (comp instanceof SwingJAxis)
	    ((SwingJAxis)comp).setInverted(obj.booleanValue());
    }


    private void
    setKeepTall(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setKeepTall(obj.booleanValue());
    }


    private void
    setLabelFlags(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setLabelFlags(obj.intValue());
    }


    private void
    setLabels(Object comp, YoixObject obj) {

	if (comp instanceof SwingJAxis)
	    ((SwingJAxis)comp).setLabels(obj);
    }


    private void
    setLineWidth(Object comp, YoixObject obj) {

	double  width;

	if (comp instanceof SwingJEventPlot) {
	    if ((width = obj.doubleValue()) > 0)
		((SwingJEventPlot)comp).setLineWidth(YoixMakeScreen.javaDistance(width + 0.5));
	    else ((SwingJEventPlot)comp).setLineWidth(0);
	} else if (comp instanceof SwingJAxis) {
	    if ((width = obj.doubleValue()) > 0)
		((SwingJAxis)comp).setLineWidth(YoixMakeScreen.javaDistance(width + 0.5));
	    else ((SwingJAxis)comp).setLineWidth(0);
	} else if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setLineWidth(obj.doubleValue());
    }


    private void
    setModel(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setModel(obj.intValue());
    }


    private void
    setMoved(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot) {
	    if (obj.isNull())
		((SwingJGraphPlot)comp).setMoved(obj);
	    else VM.abort(BADVALUE, NL_MOVED);
	}
    }


    private void
    setNodeFlags(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setNodeFlags(obj.intValue());
    }


    private void
    setNodeOutline(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setNodeOutline(obj.doubleValue());
    }


    private void
    setNodeScale(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setNodeScale(obj.doubleValue());
    }


    private void
    setOffPeakColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setOffPeakColor(YoixMake.javaColor(obj));
    }


    private void
    setOperations(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setOperations(obj);
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setOperations(obj);
    }


    private void
    setOtherColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataColorer)
	    ((SwingDataColorer)comp).setOtherColor(YoixMake.javaColor(obj));
    }


    private void
    setOutlineCacheSize(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setOutlineCacheSize(obj.intValue());
    }


    private void
    setPaintOrder(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setPaintOrder(obj);
    }


    private void
    setPalette(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setPalette(obj);
	else if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setPalette(obj);
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setPalette(obj);
    }


    private void
    setPlotEnds(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setPlotEnds(obj);
    }


    private void
    setPlotStyle(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setPlotStyle(obj);
    }


    private void
    setPlotStyleFlags(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setPlotStyleFlags(obj.intValue());
    }


    private void
    setPointSize(Object comp, YoixObject obj) {

	double  size;

	if (comp instanceof SwingJEventPlot) {
	    if ((size = obj.doubleValue()) > 0)
		((SwingJEventPlot)comp).setPointSize(YoixMakeScreen.javaDistance(size + 0.5));
	    else ((SwingJEventPlot)comp).setPointSize(0);
	}
    }


    private void
    setPressed(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setPressed(obj);
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setPressed(obj);
    }


    private void
    setPressedColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setPressedColor(YoixMake.javaColor(obj));
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setPressedColor(YoixMake.javaColor(obj));
    }


    private void
    setPressingColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setPressingColor(YoixMake.javaColor(obj));
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setPressingColor(YoixMake.javaColor(obj));
    }


    private void
    setPrimaryField(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setPrimaryField(obj.intValue());
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setPrimaryField(obj.intValue());
    }


    private void
    setRankPrefix(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot) {
	    if (obj.isString() || obj.isCallable() || obj.isNull())
		((SwingJEventPlot)comp).setRankPrefix(obj);
	    else VM.abort(TYPECHECK, NL_RANKPREFIX);
	}
    }


    private void
    setRankSuffix(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot) {
	    if (obj.isString() || obj.isCallable() || obj.isNull())
		((SwingJEventPlot)comp).setRankSuffix(obj);
	    else VM.abort(TYPECHECK, NL_RANKSUFFIX);
	}
    }


    private void
    setRecolored(Object comp, YoixObject obj) {

	if (comp instanceof SwingJHistogram)
	    ((SwingJHistogram)comp).setRecolored(obj.booleanValue());
	else if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setRecolored(obj.booleanValue());
    }


    private void
    setReversePalette(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setReversePalette(obj.booleanValue());
	else if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setReversePalette(obj.booleanValue());
    }


    private void
    setSelected(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer) {
	    if (obj.isNull() || obj.isArray() || obj.isString() || obj.isNumber())
		((SwingDataViewer)comp).setSelected(obj);
	    else VM.abort(TYPECHECK, NL_SELECTED);
	} else if (comp instanceof SwingJDataTable) {
	    if (obj.isNull() || obj.isArray() || obj.isString() || obj.isNumber())
		((SwingJDataTable)comp).setSelected(obj);
	    else VM.abort(TYPECHECK, NL_SELECTED);
	}
    }


    private void
    setSelectFlags(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setSelectFlags(obj.intValue());
    }


    private void
    setSelectWidth(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setSelectWidth(obj.doubleValue());
    }


    private void
    setSeparator(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setSeparator(obj.stringValue());
    }


    private void
    setShadeTimes(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setShadeTimes(obj);
    }


    private void
    setSliderColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingJAxis)
	    ((SwingJAxis)comp).setSliderColor(YoixMake.javaColor(obj));
    }


    private void
    setSliderEnabled(Object comp, YoixObject obj) {

	if (comp instanceof SwingJAxis)
	    ((SwingJAxis)comp).setSliderEnabled(obj.booleanValue());
    }


    private void
    setSliderEnds(Object comp, YoixObject obj) {

	double  ends[];
	int     m;
	int     n;

	if (comp instanceof SwingJAxis) {
	    if (obj.notNull()) {
		ends = new double[obj.sizeof()];
		for (m = 0, n = obj.offset(); m < ends.length; m++, n++)
		    ends[m] = obj.get(n, false).doubleValue();
	    } else ends = null;
	    ((SwingJAxis)comp).setSliderEnds(ends);
	}
    }


    private void
    setSortBy(Object comp, YoixObject obj) {

	if (comp instanceof SwingJHistogram)
	    ((SwingJHistogram)comp).setSortBy(obj.intValue());
    }


    private void
    setSortDefault(Object comp, YoixObject obj) {

	if (comp instanceof SwingJHistogram)
	    ((SwingJHistogram)comp).setSortDefault(obj.intValue());
    }


    private void
    setSpread(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setSpread(obj.booleanValue());
    }


    private void
    setStacked(Object comp, YoixObject obj) {

	//
	// EventPlots no longer use this field, but we decided to call the
	// method anyway.
	//

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setStacked(obj.booleanValue());
	else if (comp instanceof SwingJHistogram)
	    ((SwingJHistogram)comp).setStacked(obj.booleanValue());
    }


    private void
    setSweepColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setSweepColor(YoixMake.javaColor(obj));
	else if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setSweepColor(YoixMake.javaColor(obj));
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setSweepColor(YoixMake.javaColor(obj));
    }


    private void
    setSweepFlags(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setSweepFlags(obj.intValue());
    }


    private void
    setSymmetric(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setSymmetric(obj.booleanValue());
    }


    private void
    setText(Object comp, YoixObject obj) {

	if (comp instanceof SwingJHistogram)
	    ((SwingJHistogram)comp).setText(obj.stringValue());
    }


    private void
    setTicks(Object comp, YoixObject obj) {

	if (comp instanceof SwingJAxis)
	    ((SwingJAxis)comp).setTicks(obj);
    }


    private void
    setTimeShading(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setTimeShading(obj.intValue());
    }


    private void
    setTipDropped(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setTipDropped(obj.booleanValue());
	else if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setTipDropped(obj.booleanValue());
    }


    private void
    setTipEnabled(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setTipEnabled(obj.booleanValue());
	else if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setTipEnabled(obj.booleanValue());
    }


    private void
    setTipFlags(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setTipFlags(obj.intValue());
	else if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setTipFlags(obj.intValue());
    }


    private void
    setTipLockModel(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setTipLockModel(obj.intValue());
	else if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setTipLockModel(obj.intValue());
    }


    private void
    setTipOffset(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setTipOffset(obj.notNull() ? YoixMakeScreen.javaPoint(obj) : null);
	else if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setTipOffset(obj.notNull() ? YoixMakeScreen.javaPoint(obj) : null);
    }


    private void
    setTipPrefix(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot || comp instanceof SwingJGraphPlot) {
	    if (obj.isString() || obj.isCallable() || obj.isNull()) {
		if (comp instanceof SwingJEventPlot)
		    ((SwingJEventPlot)comp).setTipPrefix(obj);
		else ((SwingJGraphPlot)comp).setTipPrefix(obj);
	    } else VM.abort(TYPECHECK, NL_TIPPREFIX);
	}
    }


    private void
    setTipSuffix(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot || comp instanceof SwingJGraphPlot) {
	    if (obj.isString() || obj.isCallable() || obj.isNull()) {
		if (comp instanceof SwingJEventPlot)
		    ((SwingJEventPlot)comp).setTipSuffix(obj);
		else ((SwingJGraphPlot)comp).setTipSuffix(obj);
	    } else VM.abort(TYPECHECK, NL_TIPSUFFIX);
	}
    }


    private void
    setTransientMode(Object comp, YoixObject obj) {

	if (comp instanceof SwingDataViewer)
	    ((SwingDataViewer)comp).setTransientMode(obj.booleanValue());
	else if (comp instanceof SwingJDataTable)
	    ((SwingJDataTable)comp).setTransientMode(obj.booleanValue());
    }


    private void
    setTranslator(Object comp, YoixObject obj) {

	if (comp instanceof SwingJHistogram)
	    ((SwingJHistogram)comp).setTranslator(obj.notNull() ? obj : null);
    }


    private void
    setUnixTime(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setUnixTime(obj);
    }


    private void
    setXAxis(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setXAxis((SwingJAxis)getManagedObject(obj));
    }


    private void
    setYAxis(Object comp, YoixObject obj) {

	if (comp instanceof SwingJEventPlot)
	    ((SwingJEventPlot)comp).setYAxis((SwingJAxis)getManagedObject(obj));
    }


    private void
    setZoomDirection(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setZoomDirection(obj.intValue());
    }


    private void
    setZoomInColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setZoomInColor(YoixMake.javaColor(obj));
    }


    private void
    setZoomOutColor(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setZoomOutColor(YoixMake.javaColor(obj));
    }


    private void
    setZoomScale(Object comp, YoixObject obj) {

	if (comp instanceof SwingJGraphPlot)
	    ((SwingJGraphPlot)comp).setZoomScale(obj.doubleValue());
    }
}

