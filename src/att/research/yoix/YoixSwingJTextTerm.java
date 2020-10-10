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
import java.util.Vector;

class YoixSwingJTextTerm extends YoixSwingJTextComponent

    implements FocusListener,
	       KeyListener,
	       MouseListener

{

    //
    // Written very quickly, so there's lots of room for improvement.
    // Implementation, particularly the way lines[] is handled, is
    // not good. lines[] should be static and we should remember the
    // current endpoints (i.e., first and last lines). Many obviously
    // useful features are also missing!
    //
    // This may not currently be thread-safe. Probably just needs a
    // very careful look - later.
    //

    private String  lines[] = {};
    private String  prompt = "";
    private int     firstcolumn = 0;
    private int     savelines = 0;

    private boolean  editable = true;
    private boolean  havefocus = false;
    private int      tabstops = 8;

    //
    // Substrings of this are used to expand tabs.
    //

    private static String  spaces = "        ";		// 8 spaces - for tabs!!!

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJTextTerm(YoixObject data, YoixBodyComponent parent) {

	super(data, parent);
	addAllListeners();
    }

    ///////////////////////////////////
    //
    // FocusListener Methods
    //
    ///////////////////////////////////

    public void
    focusGained(FocusEvent e) {

	havefocus = true;
	showCursor(true);
    }


    public void
    focusLost(FocusEvent e) {

	havefocus = false;
	showCursor(true);
    }

    ///////////////////////////////////
    //
    // KeyListener Methods
    //
    ///////////////////////////////////

    public void
    keyPressed(KeyEvent e) {

	int  pagerows;
	int  pagecolumns;

	//
	// Disabling focus traversal on tabs was tricky - looked at the
	// source for java.awt.Window to figure out one solution.
	//

	pagerows = Math.max(viewport.height/cellsize.height - 1, 1);
	pagecolumns = Math.max(viewport.width/cellsize.width - 1, 1);
	switch (e.getKeyCode()) {
	    case KeyEvent.VK_DOWN:
		scrollBy(0, e.isShiftDown() ? pagerows : 1);
		break;

	    case KeyEvent.VK_LEFT:
		scrollBy(e.isShiftDown() ? -pagecolumns : -1, 0);
		break;

	    case KeyEvent.VK_PAGE_DOWN:
		scrollBy(0, pagerows);
		break;

	    case KeyEvent.VK_PAGE_UP:
		scrollBy(0, -pagerows);
		break;

	    case KeyEvent.VK_RIGHT:
		scrollBy(e.isShiftDown() ? pagecolumns : 1, 0);
		break;

	    case KeyEvent.VK_TAB:
		if (e.isShiftDown() == false)
		    e.setKeyCode(0);	// so tab doesn't change focus!!
		break;

	    case KeyEvent.VK_UP:
		scrollBy(0, e.isShiftDown() ? -pagerows : -1);
		break;
	}
    }


    public void
    keyReleased(KeyEvent e) {

    }


    public void
    keyTyped(KeyEvent e) {

	if (editable) {
	    if (e.getKeyChar() != '\t' || e.isShiftDown() == false) {
		paintChar(e.getKeyChar());
		showLastLine();
	    }
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

	//
	// Eventually may implement cut and paste. It could start when
	// button 1 is pressed and we already have the focus??
	//

	switch (YoixMiscJFC.cookModifiers(e) & YOIX_BUTTON_MASK) {
	    case YOIX_BUTTON1_MASK:
		if (havefocus == false)	// otherwise start cut and paste?
		    requestFocus();
		break;
	}
    }


    public void
    mouseReleased(MouseEvent e) {

    }

    ///////////////////////////////////
    //
    // YoixSwingJTextTerm Methods
    //
    ///////////////////////////////////

    final synchronized void
    addAllListeners() {

	addFocusListener(this);
	addKeyListener(this);
	addMouseListener(this);
    }


    final synchronized void
    append(String text) {

	String  wrapped[];
	int     n;

	if ((wrapped = wrap(text)) != null) {
	    showCursor(false);
	    for (n = 0; n < wrapped.length; n++) {
		showString(wrapped[n]);
		showLine(null);
	    }
	    showLastLine();
	    showCursor(true);
	}
    }


    protected void
    finalize() {

	super.finalize();
    }


    final boolean
    getEditable() {

	return(editable);
    }


    protected final Dimension
    getLayoutSize(String name, Dimension size) {

	YoixObject  obj;
	YoixObject  lval;
	String      str;
	Vector      tokens;
	int         width;
	int         n;

	if (loadFont()) {
	    synchronized(FONTLOCK) {
		if ((obj = data.getObject(name)) != null && obj.notNull()) {
		    size = YoixMakeScreen.javaDimension(obj);
		    if (size.width <= 0 || size.height <= 0) {
			tokens = separate(getText());
			if (size.width <= 0) {
			    if (columns <= 0) {
				if (tokens != null) {
				    for (n = 0; n < tokens.size(); n++) {
					str = (String)tokens.elementAt(n);
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
				    size.height = tokens.size()*cellsize.height;
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


    final synchronized String
    getText() {

	String  text;
	int     n;

	text = "";

	for (n = 0; n < lines.length; n++) {
	    if (lines[n] != null)
		text += lines[n];
	}
	return(text);
    }


    public final boolean
    isFocusTraversable() {

	return(true);
    }


    public final void
    paint(Graphics g) {

	paintBackground(g);
	paintBackgroundImage(g);
	paintBorder(insets, g);
	g.translate(insets.left, insets.top);
	paintRect(viewport.x, viewport.y, viewport.width, viewport.height, g);
	showCursor(true);
	g.translate(-insets.left, -insets.top);
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


    final synchronized void
    removeAllListeners() {

	removeFocusListener(this);
	removeKeyListener(this);
	removeMouseListener(this);
    }


    final void
    reset() {

	setLines(wrap(getText()));
	repaint();
    }


    public final synchronized void
    setBounds(int x, int y, int width, int height) {

	if (totalsize.width != width || totalsize.height != height) {
	    totalsize.width = width;
	    totalsize.height = height;
	    super.setBounds(x, y, width, height);
	    viewport.width = totalsize.width - insets.left - insets.right;
	    viewport.height = totalsize.height - insets.top - insets.bottom;
	    reset();
	    showLastLine();
	} else super.setBounds(x, y, width, height);
    }


    final synchronized void
    setEditable(boolean state) {

	editable = state;
    }


    final synchronized void
    setPrompt(String str) {

	int  index;

	if (str != null) {
	    prompt = str;
	    if ((index = prompt.indexOf('\n')) >= 0)
		prompt = prompt.substring(0, index);
	} else prompt = "";
    }


    final synchronized void
    setSaveLines(int count) {

	savelines = count;		// need a reset??
    }


    final synchronized void
    setText(String text) {

	setLines(wrap(text));
	reset();
	showLastLine();
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    afterNewline(String line, int start) {

	YoixObject  handler;

	if ((handler = data.getObject(N_AFTERNEWLINE)) != null) {
	    if (handler.notNull() && handler.isCallable()) {
		if (start >= 0 && start < line.length())
		    line = line.substring(start);
		else line = "";
		parent.call(handler, new YoixObject[] {YoixObject.newString(line)});
	    }
	}
    }


    private void
    paintChar(char ch) {

	showCursor(false);

	switch (ch) {
	    case '\b':
		showBackspace();
		break;

	    case '\n':
		showLine("\n");
		break;

	    case '\r':			// make it configurable - later?
		showLine("\n");
		break;

	    case '\t':
		showString(spaces.substring(lines[lines.length - 1].length() % tabstops));
		break;

	    default:
		showString(ch + "");
		break;
	}

	showCursor(true);
    }


    private void
    paintLine(int n, Graphics g) {

	String  line;

	if (fm != null) {
	    if (n >= 0 && n < lines.length) {
		if ((line = lines[n]) != null) {
		    if (fm.stringWidth(line) > 0)
			g.drawString(line, 0, n*cellsize.height + baseline);
		}
	    }
	}
    }


    private void
    scrollBy(int dx, int dy) {

	int  x;
	int  y;

	if (dx != 0 ||  dy != 0) {
	    x = Math.max(viewport.x + dx*cellsize.width, 0);
	    y = Math.max(viewport.y + dy*cellsize.height, 0);
	    setOrigin(new Point(x, y));
	    showCursor(true);
	}
    }


    private synchronized void
    setLines(String wrapped[]) {

	int  n;

	if (wrapped != null) {
	    extent.height = cellsize.height*wrapped.length;
	    extent.width = viewport.width;
	    lines = wrapped;
	} else {
	    extent.height = 0;
	    extent.width = 0;
	    lines = new String[] {""};
	}

	if ((n = lines.length - 1) >= 0) {
	    if (prompt != null && prompt.length() > 0) {
		if (n == 0 || lines[n - 1].endsWith("\n")) {
		    if (lines[n].length() == 0)
			showString(prompt);
		}
	    }
	    firstcolumn = lines[n].length();
	} else firstcolumn = 0;
    }


    private Vector
    separate(String text) {

	StringBuffer  line;
	Vector        tokens;
	char          ch;
	int           length;
	int           col;
	int           n;

	if (text != null) {
	    tokens = new Vector();
	    length = text.length();
	    line = new StringBuffer();
	    for (n = 0; n < length; n++) {
		switch (ch = text.charAt(n)) {
		    case '\n':
			line.append(ch);
			if (n < length - 1) {
			    tokens.addElement(new String(line));
			    line = new StringBuffer();
			}
			break;

		    case '\t':
			col = line.length();
			do {
			    line.append(' ');
			    col++;
			} while ((col % tabstops) != 0);
			break;

		    default:
			line.append(ch);
			break;
		}
	    }
	    tokens.addElement(new String(line));
	} else tokens = null;

	return(tokens);
    }


    private void
    showBackspace() {

	Graphics  g;
	String    line;
	int       n;
	int       x;
	int       y;

	n = lines.length - 1;

	if ((line = lines[n]) != null && line.length() > firstcolumn) {
	    lines[n] = line.substring(0, line.length() - 1);
	    if ((g = getSavedGraphics()) != null) {
		g.translate(insets.left - viewport.x, insets.top - viewport.y);
		x = fm.stringWidth(lines[n]);
		y = n*cellsize.height;
		g.clearRect(x, y, 2*cellsize.width, cellsize.height);
		g.translate(-insets.left + viewport.x, -insets.top + viewport.y);
		disposeSavedGraphics(g);
	    }
	}
    }


    private void
    showCursor(boolean state) {

	Graphics  g;
	int       height;
	int       width;
	int       n;
	int       x;
	int       y;

	if (editable) {
	    n = lines.length - 1;
	    if (lines[n] != null) {
		if ((g = getSavedGraphics()) != null) {
		    g.translate(insets.left - viewport.x, insets.top - viewport.y);
		    x = fm.stringWidth(lines[n]);
		    y = n*cellsize.height;
		    height = cellsize.height - 1;
		    width = cellsize.width - 1;
		    if (state) {
			g.fillRect(x, y, width, height);
			if (havefocus == false)
			    g.clearRect(x + 1, y + 1, width - 2, height - 2);
		    } else g.clearRect(x, y, width, height);
		    g.translate(-insets.left + viewport.x, -insets.top + viewport.y);
		    disposeSavedGraphics(g);
		}
	    }
	}
    }


    private void
    showLastLine() {

	Point  point;

	if (extent.height > viewport.height) {
	    point = new Point(
		0,
		(lines.length - viewport.height/cellsize.height)*cellsize.height
	    );
	} else point = new Point(0, 0);

	setOrigin(point);
	showCursor(true);
    }


    private synchronized void
    showLine(String suffix) {

	String  line;
	String  nlines[];
	int     length;
	int     start;

	length = lines.length;
	nlines = new String[length + 1];
	System.arraycopy(lines, 0, nlines, 0, length);
	line = nlines[length - 1];
	start = firstcolumn;
	if (suffix != null)
	    nlines[length - 1] += suffix;
	nlines[length] = "";

	setLines(nlines);
	afterNewline(line, start);
    }


    private void
    showString(String str) {

	Graphics  g;
	String    line;
	int       n;
	int       x;
	int       y;

	n = lines.length - 1;

	if (n >= 0 && str != null) {
	    if ((line = lines[n]) != null) {
		if ((g = getSavedGraphics()) != null) {
		    g.translate(insets.left - viewport.x, insets.top - viewport.y);
		    x = fm.stringWidth(line);
		    y = n*cellsize.height + baseline;
		    g.drawString(str, x, y);
		    g.translate(-insets.left + viewport.x, -insets.top + viewport.y);
		    disposeSavedGraphics(g);
		}
		lines[n] = line + str;
	    }
	}
    }


    private synchronized String[]
    wrap(String text) {

	return(wrap(separate(text)));
    }


    private synchronized String[]
    wrap(Vector tokens) {

	String  wrapped[];
	String  token;
	char    line[];
	int     maxwidth;
	int     width;
	int     m;
	int     n;

	if (viewport.width > 0) {
	    loadFont();			// added on 8/31/04
	    maxwidth = viewport.width - fm.stringWidth(prompt);
	    for (n = 0; n < tokens.size(); n++) {
		token = (String)tokens.elementAt(n);
		if (fm.stringWidth(token) > maxwidth) {
		    line = token.toCharArray();
		    width = fm.charWidth(line[0]);
		    for (m = 1; m < line.length; m++) {
			if ((width += fm.charWidth(line[m])) > maxwidth) {
			    tokens.setElementAt(new String(line, 0, m), n);
			    tokens.insertElementAt(
				new String(line, m, line.length - m),
				n + 1
			    );
			    break;
			}
		    }
		}
	    }
	}

	wrapped = new String[tokens.size()];
	for (n = 0; n < wrapped.length; n++)
	    wrapped[n] = (String)tokens.elementAt(n);

	return(wrapped);
    }
}

