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
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

class YoixAWTTableManager extends Panel

    implements YoixConstants,
	       YoixConstantsAWT,
	       ComponentListener,
	       ContainerListener,
	       MouseListener,
	       MouseMotionListener

{

    //
    // The extra container Panel seems to help horizontal scrolling,
    // but there's a chance everything could work without it.
    //
    // Could do a bit more cleaning up. For example, it looks like
    // a few methods (isActive() is just one) are really private.
    //

    private YoixBodyComponent  parent;
    private YoixObject         data;

    private YoixAWTTableColumn  children[] = new YoixAWTTableColumn[0];
    private boolean             isactive = false;
    private Panel               container;
    private Point               origin = new Point(0, 0);
    private Point               clicked = null;
    private int                 clickradius2 = 0;
    private int                 columncount = 0;
    private int                 currentcolumn = 0;
    private int                 currentrow = 0;
    private int                 firstrow = 0;
    private int                 lastrow = -1;

    //
    // Good enough for our purposes (at least right now), but our input
    // and output separator support should be more general.
    //

    private Object  outputfilter[] = null;
    private Object  inputfilter[] = null;
    private String  inputseparators[] = {"|", "\n"};
    private String  outputseparators[] = {"|", "\n"};

    //
    // Remembering callback functions helps reduce lookup and checking
    // overhead.
    //

    private YoixObject  afterload = null;
    private YoixObject  syncviewport = null;
    private int         synccount = 0;

    //
    // Variable that we can able to check to see if we're in the middle
    // of a load. Now also rememeber the size of the loaded text which
    // should make recovering it more efficient. The exact number isn't
    // needed - it's only used to pick a StringBuffer's starting size.
    //

    private int  loading = 0;
    private int  textlength = 0;

    //
    // Mouse status - SELECTING is currently unimplemented.
    //

    private static final int  UNAVAILABLE = -1;
    private static final int  AVAILABLE = 0;
    private static final int  HIGHLIGHTING = 1;
    private static final int  CLICKING = 2;
    private static final int  SELECTING = 3;	// cut+paste or drag+drop - later??

    private int  mouse = AVAILABLE;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTTableManager(YoixObject data, YoixBodyComponent parent) {

	this.data = data;
	this.parent = parent;

	container = new Panel();
	container.setLayout(new BorderLayout());
	container.add(this);
	addAllListeners();
    }

    ///////////////////////////////////
    //
    // ComponentListener Methods
    //
    ///////////////////////////////////

    public void
    componentHidden(ComponentEvent e) {

    }


    public void
    componentMoved(ComponentEvent e) {

    }


    public void
    componentResized(ComponentEvent e) {

	syncViewport();
    }


    public void
    componentShown(ComponentEvent e) {

    }

    ///////////////////////////////////
    //
    // ContainerListener Methods
    //
    ///////////////////////////////////

    public synchronized void
    componentAdded(ContainerEvent e) {

	YoixAWTTableColumn  column;
	YoixAWTTableColumn  added[];

	if (e.getChild() instanceof YoixAWTTableColumn) {
	    column = (YoixAWTTableColumn)e.getChild();
	    column.setManager(this);
	    column.addMouseListener(this);
	    added = new YoixAWTTableColumn[children.length + 1];
	    System.arraycopy(children, 0, added, 0, children.length);
	    added[children.length] = column;
	    children = added;
	    columncount = children.length;
	}
    }


    public synchronized void
    componentRemoved(ContainerEvent e) {

	YoixAWTTableColumn  added[];
	YoixAWTTableColumn  column;
	int                 m;
	int                 n;

	if (e.getChild() instanceof YoixAWTTableColumn) {
	    column = (YoixAWTTableColumn)e.getChild();
	    column.setManager(null);
	    column.removeMouseListener(this);
	    added = new YoixAWTTableColumn[children.length - 1];
	    for (n = m = 0; n < children.length; n++) {
		if (children[n] != column)
		    added[m++] = children[n];
	    }
	    children = added;
	    columncount = children.length;
	}
    }

    ///////////////////////////////////
    //
    // MouseListener Methods
    //
    ///////////////////////////////////

    public void
    mouseClicked(MouseEvent e) {

    }


    public void
    mouseEntered(MouseEvent e) {

    }


    public void
    mouseExited(MouseEvent e) {

    }


    public void
    mousePressed(MouseEvent e) {

	YoixAWTTableColumn  source;

	if (handleEvent(N_MOUSEPRESSED, e) == false) {
	    if (mouse == AVAILABLE) {
		source = (YoixAWTTableColumn)e.getSource();
		currentcolumn = getColumn(source);
		firstrow = source.getFirstRow();
		lastrow = source.getLastRow();
		isactive = isActive();
		switch (YoixMiscJFC.cookModifiers(e) & YOIX_BUTTON_MASK) {
		    case YOIX_BUTTON1_MASK:
			currentrow = source.getRow(e.getY());
			if (currentrow >= firstrow && currentrow <= lastrow) {
			    clicked = e.getPoint();
			    if (source.isActive()) {
				mouse = CLICKING;
				setHighlighted(currentrow);
			    } else mouse = SELECTING;
			} else mouse = UNAVAILABLE;
			break;

		    case YOIX_BUTTON2_MASK:
		    case YOIX_BUTTON3_MASK:
			mouse = HIGHLIGHTING;
			currentrow = source.getRow(e.getY());
			setHighlighted(currentrow);
			if (isactive) {
			    if (currentrow >= firstrow && currentrow <= lastrow)
				actionPerformed();
			}
			break;
		}
		if (mouse != AVAILABLE)
		    source.addMouseMotionListener(this);
	    }
	}
    }


    public void
    mouseReleased(MouseEvent e) {

	YoixAWTTableColumn  source;
	int                 button;

	if (handleEvent(N_MOUSERELEASED, e) == false) {
	    if (mouse != AVAILABLE) {
		source = (YoixAWTTableColumn)e.getSource();
		switch (button = YoixMiscJFC.cookModifiers(e) & YOIX_BUTTON_MASK) {
		    case YOIX_BUTTON1_MASK:
		    case YOIX_BUTTON2_MASK:
		    case YOIX_BUTTON3_MASK:
			setHighlighted(-1);
			if (mouse == CLICKING) {
			    if (button == YOIX_BUTTON1_MASK) {
				setSelected(currentrow);
				source.actionPerformed(currentrow);
			    }
			}
			firstrow = 0;
			lastrow = -1;
			mouse = AVAILABLE;
			break;
		}
		if (mouse == AVAILABLE)
		    source.removeMouseMotionListener(this);
	    }
	}
    }

    ///////////////////////////////////
    //
    // MouseMotionListener Methods
    //
    ///////////////////////////////////

    public void
    mouseDragged(MouseEvent e) {

	int  row;

	if (handleEvent(N_MOUSEDRAGGED, e) == false) {
	    switch (mouse) {
		case CLICKING:
		    if (YoixMisc.distance2(e.getPoint(), clicked) > clickradius2) {
			setHighlighted(-1);
			mouse = SELECTING;
		    }
		    break;

		case HIGHLIGHTING:
		    row = ((YoixAWTTableColumn)e.getSource()).getRow(e.getY());
		    if (row != currentrow) {
			currentrow = row;
			setHighlighted(currentrow);
			if (isactive) {
			    if (currentrow >= firstrow && currentrow <= lastrow)
				actionPerformed();
			}
		    }
		    break;

		case SELECTING:			// maybe later?
		    break;
	    }
	}
    }


    public void
    mouseMoved(MouseEvent e) {

    }

    ///////////////////////////////////
    //
    // YoixAWTTableManager Methods
    //
    ///////////////////////////////////

    final void
    actionPerformed() {

	YoixObject  event;

	if ((event = YoixMake.yoixType(T_ACTIONEVENT)) != null) {
	    event.put(N_ID, YoixObject.newInt(V_ACTIONPERFORMED));
	    event.put(N_MODIFIERS, YoixObject.newInt(YOIX_BUTTON2_MASK));
	    event.put(N_COMMAND, YoixObject.newString(getHighlightedItem()));
	    parent.call(N_ACTIONPERFORMED, event);
	} else VM.die(INTERNALERROR);
    }


    public synchronized void
    addActionListener(ActionListener listener) {

	//
	// Currently disabled because there's a mechanism that's old
	// but works well enough for now. We may remove the old code
	// in favor of the event queue approach, but probably not for
	// a while.
	//
    }


    final synchronized void
    addAllListeners() {

	int  n;

	addContainerListener(this);
	addComponentListener(this);
	for (n = 0; n < children.length; n++)
	    ((YoixAWTTableColumn)children[n]).addMouseListener(this);
    }


    final synchronized int
    countRows() {

	int  loadedrows;
	int  n;

	loadedrows = 0;

	for (n = 0; n < columncount; n++)
	    loadedrows = Math.max(loadedrows, children[n].countRows());
	return(loadedrows);
    }


    protected void
    finalize() {

	if (container != null) {
	    container.removeAll();
	    container = null;
	}

	data = null;
	parent = null;
	children = null;

	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    final synchronized Dimension
    getCellSize() {

	YoixAWTTableColumn  column;
	int                 ascent;
	int                 advance;
	int                 baseline;
	int                 descent;
	int                 n;

	ascent = 0;
	descent = 0;
	advance = 0;

	for (n = 0; n < columncount; n++) {
	    column = children[n];
	    if (column.isVisible()) {
		baseline = column.getBaseline();
		ascent = Math.max(ascent, baseline);
		descent = Math.max(descent, column.getCellHeight() - baseline);
		advance += column.getCellWidth();
	    }
	}

	advance = (columncount > 0) ? advance/columncount : 0;
	return(new Dimension(advance, ascent + descent));
    }


    final synchronized YoixObject
    getContext() {

	return(parent.getContext());
    }


    final synchronized String
    getDisplayedText() {

	return(recoverDisplayedText());
    }


    final synchronized Dimension
    getExtent() {

	int  width;
	int  height;
	int  n;

	width = getPreferredSize().width;
	height = 0;

	for (n = 0; n < columncount; n++) {
	    if (children[n].isVisible())
		height = Math.max(height, children[n].getExtent().height);
	}

	return(new Dimension(width, height));
    }


    final synchronized String
    getHighlightedItem() {

	String   item;
	String   separator;
	String   value;
	int      n;
	boolean  nonnull;

	item = null;
	separator = outputseparators[0];
	nonnull = false;

	for (n = currentcolumn; n < columncount; n++) {
	    item = (item != null) ? (item + separator) : "";
	    if ((value = children[n].getHighlightedItem()) != null) {
		item += value;
		nonnull = true;
	    }
	}

	return(nonnull ? item : null);
    }


    final synchronized String
    getHTML(Object objs[]) {

	StringBuffer  buf;
	boolean       rowlist[];
	boolean       collist[];
	boolean       first_time_row;
	boolean       first_time_col;
	String        alignments[];
	String        rowinfo[];
	String        colinfo[];
	String        text;
	String        str;
	int           tmplist[];
	int           colcount;
	int           rowcount;
	int           align;
	int           rows;
	int           i;
	int           m;
	int           n;

	if (objs == null || objs.length < 4)
	    VM.abort(INTERNALERROR);

	if ((rows = countRows()) > 0) {
	    // note: rows > 0 ==> columncount > 0
	    rowinfo = null;
	    if (objs[0] != null) {
		if (objs[0] instanceof String) {
		    str = (String)objs[0];
		    rowcount = rows;
		    rowinfo = new String[rowcount];
		    while (rowcount > 0)
			rowinfo[--rowcount] = str;
		} else if (objs[0] instanceof String[])
		    rowinfo = (String[])objs[0];
		else VM.abort(INTERNALERROR);
	    }

	    colinfo = null;
	    if (objs[1] != null) {
		if (objs[1] instanceof String) {
		    str = (String)objs[1];
		    colcount = columncount;
		    colinfo = new String[colcount];
		    while (colcount > 0)
			colinfo[--colcount] = str;
		} else if (objs[1] instanceof String[])
		    colinfo = (String[])objs[1];
		else VM.abort(INTERNALERROR);
	    }

	    rowlist = null;
	    if (objs[2] != null) {
		if (objs[2] instanceof Integer) {
		    n = ((Integer)objs[2]).intValue();
		    if (n > 0) {
			rowlist = new boolean[rows];
			for (m = 0; m < n && m < rows; m++)
			    rowlist[m] = true;
		    }
		} else if (objs[2] instanceof int[]) {
		    tmplist = (int[])objs[2];
		    for (i = 0; i < tmplist.length; i++) {
			n = tmplist[i];
			if (n >= 0 && n < rows) {
			    if (rowlist == null)
				rowlist = new boolean[rows];
			    rowlist[n] = true;
			}
		    }
		} else VM.abort(INTERNALERROR);
	    }

	    collist = null;
	    if (objs[3] != null) {
		if (objs[3] instanceof Integer) {
		    n = ((Integer)objs[3]).intValue();
		    if (n > 0) {
			collist = new boolean[columncount];
			for (m = 0; m < n && m < columncount; m++)
			    collist[m] = true;
		    }
		} else if (objs[3] instanceof int[]) {
		    tmplist = (int[])objs[3];
		    for (i = 0; i < tmplist.length; i++) {
			n = tmplist[i];
			if (n >= 0 && n < columncount) {
			    if (collist == null)
				collist = new boolean[columncount];
			    collist[n] = true;
			}
		    }
		} else VM.abort(INTERNALERROR);
	    }

	    buf = new StringBuffer();
	    alignments = new String[columncount];
	    first_time_row = true;
	    for (m = 0; m < rows; m++) {
		if (rowlist != null && !rowlist[m])
		    continue;
		if (rowinfo == null || rowinfo.length <= m || rowinfo[m] == null)
		    buf.append("\t<tr>");
		else {
		    buf.append("\t<tr ");
		    buf.append(rowinfo[m]);
		    buf.append('>');
		}
		buf.append(NL);
		first_time_col = true;
		for (n = 0; n < columncount; n++) {
		    if (collist != null && !collist[n])
			continue;
		    if (first_time_row) {
			align = children[n].alignment;
			if (align == YOIX_LEFT)
			    alignments[n] = "align=left";
			else if (align == YOIX_RIGHT)
			    alignments[n] = "align=right";
			else alignments[n] = "align=center";
		    }
		    if (first_time_col) {
			first_time_col = false;
			buf.append("\t\t<td ");
			buf.append(alignments[n]);
			if (colinfo != null && colinfo.length > n && colinfo[n] != null) {
			    buf.append(' ');
			    buf.append(colinfo[n]);
			}
			buf.append('>');
		    } else {
			buf.append("</td>");
			buf.append(NL);
			buf.append("\t\t<td ");
			buf.append(alignments[n]);
			if (colinfo != null && colinfo.length > n && colinfo[n] != null) {
			    buf.append(' ');
			    buf.append(colinfo[n]);
			}
			buf.append('>');
		    }
		    if ((str = children[n].getTextAt(m)) != null && (str = str.trim()).length() > 0)
			buf.append(str);
		    else buf.append("&nbsp;");
		}
		buf.append("</td>");
		buf.append(NL);
		buf.append("\t</tr>");
		buf.append(NL);

		if (first_time_row)
		    first_time_row = false;
	    }
	    text = buf.toString();
	} else text = null;

	return(text);
    }


    final synchronized int[]
    getIndexItem() {

	String  item;
	String  separator;
	String  value;
	int     idx[];
	int     n;

	item = null;
	separator = outputseparators[0];
	idx = new int[columncount];

	for (n = 0; n < columncount; n++)
	    idx[n] = children[n].getIndexItem();

	return(idx);
    }


    final YoixObject
    getInputFilter() {

	YoixObject  obj;
	int         n;

	if (inputfilter != null) {
	    obj = YoixObject.newArray(inputfilter.length);
	    for (n = 0; n < inputfilter.length; n += 3) {
		obj.putString(n, (String)inputfilter[n]);
		obj.putInt(n + 1, ((Integer)inputfilter[n + 1]).intValue());
		obj.putString(n + 2, (String)inputfilter[n + 2]);
	    }
	} else obj = YoixObject.newString(inputseparators[0]);

	return(obj);
    }


    final Point
    getOrigin() {

	return(origin);
    }


    final YoixObject
    getOutputFilter() {

	YoixObject  obj;
	int         n;

	if (outputfilter != null) {
	    obj = YoixObject.newArray(outputfilter.length);
	    for (n = 0; n < outputfilter.length; n += 3) {
		obj.putString(n, (String)outputfilter[n]);
		obj.put(n + 1, (YoixObject)outputfilter[n + 1], true);
		obj.putString(n + 2, (String)outputfilter[n + 2]);
	    }
	} else obj = YoixObject.newString(outputseparators[0]);

	return(obj);
    }


    final synchronized String
    getSelectedItem() {

	String  item;
	String  separator;
	String  value;
	int     n;

	item = null;
	separator = outputseparators[0];

	for (n = 0; n < columncount; n++) {
	    if ((value = children[n].getSelectedItem()) != null) {
		item = (item != null) ? (item + separator) : "";
		item += value;
	    }
	}

	return(item);
    }


    final synchronized int
    getSyncCount() {

	return(synccount);
    }


    final synchronized String
    getText() {

	return((inputfilter == null)
	    ? recoverLoadedText(new String(inputseparators[0]), "\n")
	    : recoverLoadedText(inputfilter)
	);
    }


    final synchronized Rectangle
    getViewport() {

	Component  owner;
	int        height;
	int        width;
	int        n;

	if ((owner = container.getParent()) == null)
	    owner = container;

	width = owner.getSize().width;
	height = Short.MAX_VALUE;

	for (n = 0; n < columncount; n++) {
	    if (children[n].isVisible())
		height = Math.min(height, children[n].getViewport().height);
	}

	return(new Rectangle(origin.x, origin.y, width, height));
    }


    public synchronized void
    removeActionListener(ActionListener listener) {

	//
	// Currently disabled because there's a mechanism that's old
	// but works well enough for now. We may remove the old code
	// in favor of the event queue approach, but probably not for
	// a while.
	//
    }


    final synchronized void
    removeAllListeners() {

	int  n;

	removeContainerListener(this);
	removeComponentListener(this);
	for (n = 0; n < children.length; n++)
	    ((YoixAWTTableColumn)children[n]).removeMouseListener(this);
    }


    final synchronized void
    reset() {

	int  ascent;
	int  baseline;
	int  descent;
	int  height;
	int  n;

	if (loading == 0) {
	    ascent = 0;
	    descent = 0;

	    for (n = 0; n < columncount; n++) {
		baseline = children[n].getBaseline();
		ascent = Math.max(ascent, baseline);
		descent = Math.max(descent, children[n].getCellHeight() - baseline);
	    }

	    height = ascent + descent;

	    for (n = 0; n < columncount; n++) {
		children[n].setBaseline(ascent);
		children[n].setCellHeight(height);
		children[n].repaint();
	    }
	    syncViewport();
	}
    }


    final synchronized void
    setAfterLoad(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0))
		afterload = obj;
	    else VM.abort(TYPECHECK, N_AFTERLOAD);
	} else afterload = null;
    }


    public final void
    setBounds(int x, int y, int width, int height) {

	super.setBounds(-origin.x, y, width, height);
    }


    final void
    setClickRadius(int radius) {

	clickradius2 = (radius > 0) ? radius*radius : 0;
    }


    final synchronized void
    setHighlighted(int row) {

	int  n;

	for (n = 0; n < columncount; n++)
	    children[n].setHighlighted(row);
    }


    final synchronized void
    setInputFilter(YoixObject obj) {

	Object  table[];
	String  str;
	int     length;
	int     n;

	inputfilter = null;
	inputseparators = new String[] {"|", "\n"};

	if (obj.notNull()) {
	    if (obj.isArray()) {
		if ((length = obj.length()) > 0 && length%3 == 0) {
		    table = new Object[length];
		    for (n = 0; n < length; n += 3) {
			table[n] = obj.get(n, false).stringValue();
			table[n + 1] = new Integer(obj.get(n + 1, false).intValue());
			table[n + 2] = obj.get(n + 2, false).stringValue();
		    }
		    inputfilter = table;
		} else VM.abort(TYPECHECK, N_INPUTFILTER);
	    } else if (obj.isString())
		inputseparators[0] = obj.stringValue();
	    else VM.abort(TYPECHECK, N_INPUTFILTER);
	}
    }


    final synchronized void
    setOrigin(Point point) {

	int  n;

	if (point != null && point.equals(origin) == false) {
	    if (origin.y != point.y) {
		origin.y = point.y;
		for (n = 0; n < columncount; n++)
		    children[n].verticalScrollTo(origin.y);
	    }
	    if (origin.x != point.x) {
		origin.x = point.x;
		setLocation(getLocation());
	    }
	}
    }


    final synchronized void
    setOutputFilter(YoixObject obj) {

	Object  table[];
	String  str;
	int     length;
	int     n;

	//
	// Hardly used, so we don't accept filters that are arrays. May
	// change, but not for a while.
	//

	outputfilter = null;
	outputseparators = new String[] {"|", "\n"};

	if (obj.notNull()) {
	    if (obj.isArray()) {
		VM.abort(TYPECHECK, N_OUTPUTFILTER);	// remove later??
		if ((length = obj.length()) > 0 && length%3 == 0) {
		    table = new Object[length];
		    for (n = 0; n < length; n += 3) {
			table[n] = obj.get(n, false).stringValue();
			table[n + 1] = new Integer(obj.get(n + 1, false).intValue());
			table[n + 2] = obj.get(n + 2, false).stringValue();
		    }
		    outputfilter = table;
		} else VM.abort(TYPECHECK, N_OUTPUTFILTER);
	    } else if (obj.isString())
		outputseparators[0] = obj.stringValue();
	    else VM.abort(TYPECHECK, N_OUTPUTFILTER);
	}
    }


    final synchronized void
    setRowProperties(YoixObject obj) {

	YoixObject  prop;
	HashMap     properties;
	HashMap     javacolors;
	Color       colors[];
	Color       color;
	int         length;
	int         value;
	int         n;

	properties = new HashMap();

	if (obj != null && obj.notNull()) {
	    javacolors = new HashMap();
	    if ((prop = obj.getObject(N_BACKGROUND, null)) != null) {
		if (prop.notNull() && (length = prop.sizeof()) > 0) {
		    colors = new Color[length];
		    for (n = prop.offset(); n < length; n++) {
			value = YoixMake.javaColorValue(prop.getObject(n));
			if ((color = (Color)javacolors.get(value+"")) == null) {
			    color = new Color(value);
			    javacolors.put(value+"", color);
			}
			colors[n] = color;
		    }
		    properties.put(N_BACKGROUND, colors);
		}
	    }
	    if ((prop = obj.getObject(N_FOREGROUND, null)) != null) {
		if (prop.notNull() && (length = prop.sizeof()) > 0) {
		    colors = new Color[length];
		    for (n = prop.offset(); n < length; n++) {
			value = YoixMake.javaColorValue(prop.getObject(n));
			if ((color = (Color)javacolors.get(value+"")) == null) {
			    color = new Color(value);
			    javacolors.put(value+"", color);
			}
			colors[n] = color;
		    }
		    properties.put(N_FOREGROUND, colors);
		}
	    }
	    javacolors = null;
	}

	for (n = 0; n < columncount; n++)
	    children[n].setRowProperties(properties);
	reset();
    }


    final synchronized void
    setSaveGraphics(boolean state) {

	int  n;

	for (n = 0; n < columncount; n++)
	    children[n].setSaveGraphics(state);
    }


    final synchronized void
    setSyncViewport(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(1) || obj.callable(3))
		syncviewport = obj;
	    else VM.abort(TYPECHECK, N_SYNCVIEWPORT);
	} else syncviewport = null;
    }


    final synchronized void
    setText(String text) {

	loadColumns(separateRecords(text, new ArrayList(10*(columncount + 1))));
	textlength = (text != null) ? text.length() : 0;
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    afterLoad() {

	YoixObject  funct = afterload;
	int         n;

	if (funct != null)
	    parent.call(funct, new YoixObject[0]);

	for (n = 0; n < columncount; n++)
	    children[n].afterLoad();
    }


    private synchronized int
    getColumn(Object source) {

	int  n;

	for (n = 0; n < columncount; n++) {
	    if (source == children[n])
		break;
	}

	return(n);
    }


    private boolean
    handleEvent(String name, AWTEvent e) {

	YoixObject  obj;
	boolean     handled;

	if (handled = data.defined(name)) {
	    if ((obj = parent.handleAWTEvent(e)) != null)
		handled = obj.isNumber() ? obj.booleanValue() : false;
	}

	return(handled);
    }


    private boolean
    isActive() {

	YoixObject  obj;

	return(((obj = data.getObject(N_ACTIONPERFORMED)) != null)
	    ? obj.isFunction() && obj.notNull()
	    : false
	);
    }


    private void
    loadColumns(ArrayList records) {

	int  n;

	try {
	    loading++;
	    for (n = 0; n < columncount; n++)
		children[n].loadColumn(records, n);
	    afterLoad();
	}
	finally {
	    loading--;
	}
	reset();
    }


    private synchronized String
    recoverDisplayedText() {

	YoixAWTTableColumn  column;
	StringBuffer        value;
	String              cell;
	String              linesep;
	String              sep;
	int                 lengths[];
	int                 loadedrows;
	int                 m;
	int                 n;
	int                 k;

	//
	// A very late addition designed to reproduce behavior that we used
	// in the original implementation of GFMS.
	//

	value = new StringBuffer("");
	linesep = System.getProperty("line.separator");		// questionable
	lengths = new int[children.length];
	loadedrows = 0;

	for (n = 0; n < children.length; n++) {
	    lengths[n] = children[n].countColumns();
	    loadedrows = Math.max(loadedrows, children[n].countRows());
	}

	for (n = 0; n < loadedrows; n++) {
	    sep = "";
	    for (m = 0; m < children.length; m++) {
		column = children[m];
		if (column.isVisible()) {
		    value.append(sep);
		    if (lengths[m] > 0) {
			if ((cell = column.getCell(n)) != null) {
			    value.append(cell);
			    for (k = lengths[m] - cell.length(); k > 0; k--)
				value.append(' ');
			}
		    } else value.append("*");
		    sep = " ";
		}
	    }
	    value.append(linesep);
	}

	return(new String(value));
    }


    private synchronized String
    recoverLoadedText(Object filter[]) {

	String  text;
	String  str;
	String  prefix;
	String  replace;
	int     column;
	int     rows;
	int     m;
	int     n;
	int     k;

	//
	// Doubt this is used much (if ever), so we didn't bother with
	// the StringBuffer changes.
	//

	if ((rows = countRows()) > 0) {
	    text = "";
	    for (m = 0; m < rows; m++) {
		for (n = 0; n < filter.length; n += 3) {
		    column = ((Integer)filter[n + 1]).intValue() - 1;
		    if (column >= 0 && column < columncount) {
			if ((str = children[column].getTextAt(m)) != null) {
			    if ((replace = (String)filter[n + 2]) != null) {
				for (k = 0; k < replace.length() - 1; k += 2) {
				    str = str.replace(
					replace.charAt(k+1),
					replace.charAt(k)
				    );
				}
			    }
			    if ((prefix = (String)filter[n]) != null)
				str = prefix + str;
			    text += str + "\n";
			}
		    }
		}
	    }
	} else text = null;

	return(text);
    }


    private synchronized String
    recoverLoadedText(String fieldsep, String linesep) {

	StringBuffer  sbuf;
	String        text;
	String        str;
	int           length;
	int           rows;
	int           m;
	int           n;

	if ((rows = countRows()) > 0) {
	    length = rows*(columncount*fieldsep.length() + linesep.length());
	    sbuf = new StringBuffer(Math.max(textlength, 100) + length);
	    for (m = 0; m < rows; m++) {
		if (m > 0)
		    sbuf.append(linesep);
		for (n = 0; n < columncount; n++) {
		    if (n > 0)
			sbuf.append(fieldsep);
		    if ((str = children[n].getTextAt(m)) != null)
			sbuf.append(str);
		}
	    }
	    text = sbuf.toString();
	} else text = null;

	return(text);
    }


    private ArrayList
    separateRecords(String text, ArrayList records) {

	BufferedReader  reader = null;
	String          separators;

	try {
	    reader = new BufferedReader(new StringReader(text));
	    if (inputfilter == null) {
		separators = inputseparators[0];
		if (separators.length() == 1)
		    separateText(reader, separators.charAt(0), records);
		else separateText(reader, separators, records);
	    } else separateText(reader, inputfilter, records);
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
    separateText(BufferedReader reader, Object inputfilter[], ArrayList records)

	throws IOException

    {

	String  fields[];
	String  line;
	String  prefix;
	String  replace;
	String  value;
	int     index;
	int     m;
	int     n;

	fields = null;

	while ((line = reader.readLine()) != null) {
	    for (n = 0; n < inputfilter.length; n += 3) {
		if ((prefix = (String)inputfilter[n]) != null) {
		    if (line.startsWith(prefix)) {
			index = ((Integer)inputfilter[n + 1]).intValue();
			if (index > 0) {
			    if (index <= columncount) {
				replace = (String)inputfilter[n + 2];
				value = line.substring(prefix.length());
				if (replace != null) {
				    for (m = 0; m < replace.length() - 1; m += 2) {
					value = value.replace(
					    replace.charAt(m),
					    replace.charAt(m+1)
					);
				    }
				}
				if (fields == null)
				    fields = new String[columncount];
				fields[index - 1] = value;
			    }
			} else if (fields != null) {
			    records.add(fields);
			    fields = null;
			}
		    }
		}
	    }
	}

	if (fields != null)
	    records.add(fields);
    }


    private void
    separateText(BufferedReader reader, char separator, ArrayList records)

	throws IOException

    {

	String  buffer[];
	String  fields[];
	String  line;
	int     length;
	int     first;
	int     next;
	int     count;

	//
	// Separates each input line, assuming fields are delimited by
	// the same character. Each separated row is saved as an array
	// of Strings in the records ArrayList. Probably the most common
	// way to load a table.
	//

	buffer = new String[columncount];

	while ((line = reader.readLine()) != null) {
	    length = line.length();

	    for (count = 0, next = 0; next < length && count < buffer.length; count++, next++) {
		first = next;
		if ((next = line.indexOf(separator, first)) < 0) {
		    buffer[count] = line.substring(first);
		    next = length;
		} else buffer[count] = line.substring(first, next);
	    }

	    fields = new String[count];
	    System.arraycopy(buffer, 0, fields, 0, count);
	    records.add(fields);
	}
    }


    private void
    separateText(BufferedReader reader, String separators, ArrayList records)

	throws IOException

    {

	String  buffer[];
	String  fields[];
	String  line;
	int     length;
	int     first;
	int     next;
	int     count;

	//
	// Loads rows and handles field separation when there's more
	// than one separator character. There's room for improvement,
	// but this code probably isn't used much.
	//

	buffer = new String[columncount];

	while ((line = reader.readLine()) != null) {
	    length = line.length();

	    for (count = 0, next = 0; next < length && count < buffer.length; count++) {
		for (first = next; next < length; next++) {
		    if (separators.indexOf(line.charAt(next)) >= 0)
			break;
		}
		if (first < length) {
		    if (next < length)
			buffer[count] = line.substring(first, next++);
		    else buffer[count] = line.substring(first);
		} else buffer[count] = null;
	    }

	    fields = new String[count];
	    System.arraycopy(buffer, 0, fields, 0, count);
	    records.add(fields);
	}
    }


    final synchronized void
    setIndexItem(int idx) {

	int  n;

	for (n = 0; n < columncount; n++)
	    children[n].setIndexItem(idx);
    }


    private synchronized void
    setSelected(int row) {

	int  n;

	for (n = 0; n < columncount; n++)
	    children[n].setSelected(row);
    }


    private synchronized void
    syncViewport() {

	YoixObject  funct;
	YoixObject  argv[];
	Runnable    event;

	//
	// Calls a user defined function, so to be safe we build an
	// InvocationEvent for the call and then post it on the AWT
	// event queue (to try to avoid deadlock).
	// 

	funct = syncviewport;		// snapshot - just to be safe

	if (funct != null) {
	    synccount++;
	    if (funct.callable(3)) {
		argv = new YoixObject[] {
		    YoixMakeScreen.yoixRectangle(getViewport()),
		    YoixMakeScreen.yoixDimension(getExtent()),
		    YoixMakeScreen.yoixDimension(getCellSize())
		};
	    } else argv = new YoixObject[] {YoixObject.newInt(synccount)};
	    event = new YoixAWTInvocationEvent(funct, argv, getContext());
	    EventQueue.invokeLater(event);
	}
    }
}

