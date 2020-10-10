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
import java.util.ArrayList;
import java.util.HashMap;

class YoixAWTTableColumn extends YoixAWTTextComponent

{

    //
    // Currently just tables, but with a little work something like
    // this probably could substitute for an AWT List or Choice.
    //

    private YoixAWTTableManager  manager;

    private boolean  visited[];
    private String   lines[];
    private Color    visitcolor = null;
    private int      highlighted = -1;
    private int      selected = -1;

    //
    // Remembering callback functions helps reduce lookup and checking
    // overhead.
    //

    private YoixObject  afterload = null;

    //
    // Forces an exact width calculation based on the loaded text when
    // set to true. Overhead was quite noticeable when tables had lots
    // lines. None of our existing applications need exact widths, so
    // it's disabled by default. Also, there's currently no way users
    // can change this setting, but that should be easy to change.
    //

    private boolean  measurelines = false;

    //
    // A few properties that can be set for each row - we anticipate
    // supporting more in the near future.
    //

    private boolean  hasrowcolors = false;
    private Color    rowbackgrounds[] = null;
    private Color    rowforegrounds[] = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTTableColumn(YoixObject data, YoixBodyComponent parent) {

	super(data, parent);
    }

    ///////////////////////////////////
    //
    // YoixAWTTableColumn Methods
    //
    ///////////////////////////////////

    final void
    actionPerformed(int row) {

	YoixObject  event = null;

	//
	// Probably don't want to call the user's event handler while
	// we're synchronized, but we need a some protection while we
	// build the  Yoix represention of the event (there are other
	// solutions). Also, don't be confused by the name - this is
	// not an AWT event handler.
	//

	synchronized(this) {
	    if (lines != null && visited != null) {
		if (row >= 0 && row < lines.length) {
		    if (visited[row] == false) {
			visited[row] = true;
			paintLine(row);
		    }
		    event = YoixMake.yoixType(T_ACTIONEVENT);
		    event.put(N_ID, YoixObject.newInt(V_ACTIONPERFORMED));
		    event.put(N_MODIFIERS, YoixObject.newInt(YOIX_BUTTON1_MASK));
		    event.put(N_COMMAND, YoixObject.newString(lines[row]));
		}
	    }
	}

	if (event != null)
	    parent.call(N_ACTIONPERFORMED, event);
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


    final void
    addMouseListener(YoixAWTTableManager listener) {

	super.addMouseListener(listener);
    }

    final void
    addMouseMotionListener(YoixAWTTableManager listener) {

	super.addMouseMotionListener(listener);
    }


    final void
    afterLoad() {

	YoixObject  funct = afterload;		// snapshot - just to be safe
	YoixObject  argv[];
	int         n;

	if (funct != null) {
	    synchronized(this) {
		argv = new YoixObject[funct.callable(2) ? 2 : 1];
		if (manager != null)
		    argv[0] = manager.getContext();
		else argv[0] = YoixObject.newNull();
		if (argv.length == 2) {
		    if (lines != null) {
			argv[1] = YoixObject.newArray(lines.length);
			for (n = 0; n < lines.length; n++)
			    argv[1].putString(n, lines[n]);
		    } else argv[1] = YoixObject.newArray(0);
		}
	    }
	    parent.call(funct, argv);
	}
    }


    final synchronized int
    countColumns() {

	int  count = 0;
	int  n;

	if (lines != null) {
	    for (n = 0; n < lines.length; n++)
		count = Math.max(count, lines[n].length());
	}

	return(count);
    }


    final synchronized int
    countRows() {

	return(lines != null ? lines.length : 0);
    }


    protected void
    finalize() {

	manager = null;
	super.finalize();
    }


    final synchronized String
    getCell(int n) {

	return((n >= 0 && lines != null && n < lines.length) ? lines[n] : null);
    }


    final synchronized int
    getFirstRow() {

	return(cellsize.height > 0 ? viewport.y/cellsize.height : 0);
    }


    final synchronized String
    getHighlightedItem() {

	return((highlighted >= 0 && highlighted < lines.length)
	    ? lines[highlighted]
	    : null
	);
    }


    final synchronized int
    getIndexItem() {

	return((selected >= 0 && selected < lines.length) ? selected : -1);
    }


    final synchronized int
    getLastRow() {

	return(cellsize.height > 0 ? (viewport.y + viewport.height)/cellsize.height : -1);
    }


    protected final Dimension
    getLayoutSize(String name, Dimension size) {

	YoixObject  lval;
	YoixObject  obj;
	String      str;
	String      tokens[];
	int         width;
	int         n;

	if (loadFont()) {
	    synchronized(FONTLOCK) {
		if ((obj = data.getObject(name)) != null && obj.notNull()) {
		    size = YoixMakeScreen.javaDimension(obj);
		    if (size.width <= 0 || size.height <= 0) {
			tokens = lines;
			if (size.width <= 0) {
			    if (columns <= 0) {
				if (tokens != null) {
				    for (n = 0; n < tokens.length; n++) {
					str = tokens[n];
					if ((width = fm.stringWidth(str)) > size.width)
					    size.width = width;
				    }
				    size.width += insets.left + insets.right;
				}
			    } else size.width = columns*cellsize.width + insets.left + insets.right;
			}
			if (size.height <= 0) {
			    if (rows <= 0) {
				if (tokens != null) {
				    size.height = tokens.length*cellsize.height;
				    size.height += insets.top + insets.bottom;
				}
			    } else size.height = rows*cellsize.height + insets.top + insets.bottom;
			}
			if (size.width > 0 && size.height > 0) {
			    lval = YoixObject.newLvalue(data, name);
			    if (lval.canWrite())
				lval.put(YoixMakeScreen.yoixDimension(size));
			}
		    }
		} else {
		    if (columns > 0)
			size.width = columns*cellsize.width + insets.left + insets.right;
		    if (rows > 0)
			size.height = rows*cellsize.height + insets.top + insets.bottom;
		}
	    }
	}

	return(size);
    }


    final synchronized int
    getRow(int y) {

	int  num;

	return(cellsize.height > 0 && (num = (viewport.y - insets.top + y)) >= 0
	    ? num/cellsize.height
	    : -1
	);
    }


    final synchronized String
    getSelectedItem() {

	return((selected >= 0 && selected < lines.length) ? lines[selected] : null);
    }


    final synchronized String
    getText() {

	return(getText("\n"));
    }


    final synchronized String
    getText(String sep) {

	String  str = null;
	int     n;

	if (lines != null) {
	    for (n = 0; n < lines.length; n++) {
		if (n > 0)
		    str += sep + lines[n];
		else str = lines[n];
	    }
	}
	return(str);
    }


    final synchronized String
    getTextAt(int n) {

	return((n >= 0 && n < lines.length) ? lines[n] : null);
    }


    final Color
    getVisitColor() {

	return(visitcolor);
    }


    final boolean
    isActive() {

	YoixObject  obj;

	return(((obj = data.getObject(N_ACTIONPERFORMED)) != null)
	    ? obj.isFunction() && obj.notNull()
	    : false
	);
    }


    final synchronized void
    loadColumn(ArrayList records, int index) {

	String  row[];
	int     length;
	int     n;

	lines = null;
	visited = null;
	selected = -1;

	if (records != null && index >= 0) {
	    length = records.size();
	    lines = new String[length];
	    visited = new boolean[length];
	    for (n = 0; n < length; n++) {
		if ((row = (String[])records.get(n)) != null) {
		    if (index < row.length && row[index] != null)
			lines[n] = row[index];
		    else lines[n] = "";
		} else lines[n] = "";
	    }
	}

	setExtent();
	//
	// Eventually suspect this should be unsynchronized.
	//
	if (manager == null && lines != null)
	    afterLoad();
    }


    public final void
    paint(Graphics g) {

	paintBackgroundImage(g);
	paintBorder(insets, g);

	if (lines != null) {
	    g.translate(insets.left, insets.top);
	    paintRect(viewport.x, viewport.y, viewport.width, viewport.height, g);
	    g.translate(-insets.left, -insets.top);
	}
    }


    protected final synchronized void
    paintRect(int x, int y, int width, int height, Graphics g) {

	Shape  clip;
	Font   font;
	int    last;
	int    n;

	//
	// Setting clip path turns out to be important in some obscure
	// cases, so be careful making changes. For example, partially
	// cover the canvas with another window and then rapidly scroll
	// (in both directions) using keys and you may notice problems.
	// I think drawString() probably decides nothing needs to be
	// done when the text string doesn't look like it intersects
	// the current clipping path and maybe those calcuations aren't
	// quite right???
	//

	if (lines != null) {
	    clip = g.getClip();
	    font = g.getFont();
	    g.translate(-viewport.x, -viewport.y);
	    g.clipRect(x, y, width, height);		// recent change
	    g.setFont(getFont());
	    last = Math.min((y + height)/cellsize.height, lines.length - 1);
	    for (n = Math.max(y/cellsize.height, 0); n <= last; n++)
		paintLine(n, g);
	    g.translate(viewport.x, viewport.y);
	    g.setFont(font);
	    g.setClip(clip);
	}
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


    final void
    removeMouseListener(YoixAWTTableManager listener) {

	super.removeMouseListener(listener);
    }

    final void
    removeMouseMotionListener(YoixAWTTableManager listener) {

	super.removeMouseMotionListener(listener);
    }


    final void
    reset() {

	reset(false);
    }


    final void
    reset(boolean sync) {

	setViewportSize();
	if (sync && manager != null)
	    manager.reset();
	else repaint();
    }


    final synchronized void
    setAfterLoad(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(1) || obj.callable(2))
		afterload = obj;
	    else VM.abort(TYPECHECK, N_AFTERLOAD);
	} else afterload = null;
    }


    final void
    setAnchor() {

    }


    final void
    setBaseline(int baseline) {

	synchronized(FONTLOCK) {
	    this.baseline = baseline;
	}
    }


    public final synchronized void
    setBounds(int x, int y, int width, int height) {

	if (totalsize.width != width || totalsize.height != height) {
	    totalsize.width = width;
	    totalsize.height = height;
	}

	super.setBounds(x, y, width, height);
	setViewportSize();
    }


    final void
    setCellHeight(int height) {

	synchronized(FONTLOCK) {
	    cellsize.height = height;
	}
    }


    final void
    setCellWidth(int width) {

	synchronized(FONTLOCK) {
	    cellsize.width = width;
	}
    }


    final synchronized void
    setHighlighted(int next) {

	int  current;

	if ((current = highlighted) != next) {
	    highlighted = next;
	    repaintLine(current);
	    paintLine(highlighted);
	}
    }


    final synchronized void
    setIndexItem(int idx) {

	if (lines != null && idx >= 0 && idx < lines.length) {
	    selected = idx;
	    if (visited != null && visited[idx] == false) {
		visited[idx] = true;
		paintLine(idx);
	    }
	} else selected = -1;
    }


    final synchronized void
    setManager(YoixAWTTableManager manager) {

	//
	// The disposeSavedGraphics() call should be safe and seems to
	// be needed in a few obscure test cases.
	//

	this.manager = manager;
	disposeSavedGraphics();		// required??
    }


    final synchronized void
    setRowProperties(HashMap properties) {

	YoixObject  obj;
	Object      prop;

	if ((obj = data.getObject(N_ROWPROPERTIES)) == null || obj.isNull()) {
	    hasrowcolors = false;
	    rowbackgrounds = null;
	    rowforegrounds = null;
	    if (properties != null) {
		if ((prop = properties.get(N_BACKGROUND)) != null) {
		    if (prop instanceof Color[]) {
			hasrowcolors = true;
			rowbackgrounds = (Color[])prop;
		    }
		}
		if ((prop = properties.get(N_FOREGROUND)) != null) {
		    if (prop instanceof Color[]) {
			hasrowcolors = true;
			rowbackgrounds = (Color[])prop;
		    }
		}
	    }
	}
    }


    final synchronized void
    setRowProperties(YoixObject obj) {

	YoixObject  prop;
	HashMap     javacolors;
	Color       color;
	int         length;
	int         value;
	int         n;

	hasrowcolors = false;
	rowbackgrounds = null;
	rowforegrounds = null;

	if (obj != null && obj.notNull()) {
	    javacolors = new HashMap();
	    if ((prop = obj.getObject(N_BACKGROUND, null)) != null) {
		if (prop.notNull() && (length = prop.sizeof()) > 0) {
		    hasrowcolors = true;
		    rowbackgrounds = new Color[length];
		    for (n = prop.offset(); n < length; n++) {
			value = YoixMake.javaColorValue(prop.getObject(n));
			if ((color = (Color)javacolors.get(value+"")) == null) {
			    color = new Color(value);
			    javacolors.put(value+"", color);
			}
			rowbackgrounds[n] = color;
		    }
		}
	    }
	    if ((prop = obj.getObject(N_FOREGROUND, null)) != null) {
		if (prop.notNull() && (length = prop.sizeof()) > 0) {
		    hasrowcolors = true;
		    rowforegrounds = new Color[length];
		    for (n = prop.offset(); n < length; n++) {
			value = YoixMake.javaColorValue(prop.getObject(n));
			if ((color = (Color)javacolors.get(value+"")) == null) {
			    color = new Color(value);
			    javacolors.put(value+"", color);
			}
			rowforegrounds[n] = color;
		    }
		}
	    }
	}
	reset(true);
    }


    final synchronized void
    setSelected(int row) {

	selected = row;
    }


    final synchronized void
    setSelectedItem(String text) {

	if (selected >= 0 && selected < lines.length) {
	    lines[selected] = text;
	    repaintLine(selected);
	}
    }


    final synchronized void
    setVisitColor(Color color) {

	visitcolor = color;
	reset(false);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private Color
    getRowColor(int row, boolean wantbackground) {

	Color  color;

	if ((row == highlighted) == wantbackground) {
	    if (visited[row] == false || visitcolor == null) {
		if (rowforegrounds != null)
		    color = rowforegrounds[row%rowforegrounds.length];
		else color = getForeground();
	    } else color = visitcolor;
	} else {
	    if (rowbackgrounds != null)
		color = rowbackgrounds[row%rowbackgrounds.length];
	    else color = getBackground();
	}

	return(color);
    }


    private synchronized void
    paintLine(int n) {

	Graphics  g;
	int       tx;
	int       ty;

	if (n >= getFirstRow() && n <= getLastRow()) {
	    if ((g = getSavedGraphics()) != null) {
		tx = insets.left - viewport.x;
		ty = insets.top - viewport.y;
		g.translate(tx, ty);
		paintLine(n, g);
		g.translate(-tx, -ty);
		disposeSavedGraphics(g);
	    }
	}
    }


    private void
    paintLine(int n, Graphics g) {

	String  text;
	Color   color;
	Color   background;
	int     dx;
	int     x;
	int     y;

	if (lines != null) {
	    if (n >= 0 && n < lines.length) {
		color = g.getColor();
		y = n*cellsize.height;
		if (highlighted == n || hasrowcolors) {
		    background = getRowColor(n, true);
		    if (highlighted == n || color.equals(background) == false) {
			g.setColor(background);
			g.fillRect(0, y, viewport.width, cellsize.height);
		    }
		}
		g.setColor(getRowColor(n, false));
		if ((text = lines[n]) != null) {
		    if ((dx = fm.stringWidth(text)) > 0) {
			switch (alignment) {
			    case YOIX_LEFT:
				x = 0;
				break;

			    case YOIX_CENTER:
				x = (viewport.width - dx)/2;
				break;

			    case YOIX_RIGHT:
				x = viewport.width - dx;
				break;

			    default:
				x = 0;
				break;
			}
			g.drawString(text, x, y + baseline);
		    }
		}
		g.setColor(color);
	    }
	}
    }


    private synchronized void
    repaintLine(int n) {

	Graphics  g;
	int       tx;
	int       ty;

	if (n >= getFirstRow() && n <= getLastRow()) {
	    if ((g = getSavedGraphics()) != null) {
		tx = insets.left - viewport.x;
		ty = insets.top - viewport.y;
		g.translate(tx, ty);
		g.clearRect(0, n*cellsize.height, viewport.width, cellsize.height);
		paintLine(n, g);
		g.translate(-tx, -ty);
		disposeSavedGraphics(g);
	    }
	}
    }


    private void
    setExtent() {

	int  width;
	int  n;

	extent.width = 0;
	extent.height = 0;

	if (lines != null) {
	    if (loadFont()) {
		if (measurelines) {
		    synchronized(FONTLOCK) {
			for (n = 0; n < lines.length; n++) {
			    if ((width = fm.stringWidth(lines[n])) > extent.width)
				extent.width = width;
			}
		    }
		} else extent.width = totalsize.width;
		extent.height = cellsize.height*lines.length;
	    }
	}
    }


    private void
    setViewportSize() {

	viewport.width = totalsize.width - insets.left - insets.right;
	viewport.height = totalsize.height - insets.top - insets.bottom;
    }
}

