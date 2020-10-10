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
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

class YoixAWTTextCanvas extends YoixAWTTextComponent

{

    private String   currenttext;
    private String   lines[];
    private int      textmode = YOIX_WORDMODE;
    private boolean  textwrap = true;

    //
    // We sometimes will want to recognize a few HTML tags. Scanning
    // is simple and only notices them if they're surrounded by white
    // space.
    //

    private static Hashtable  htmltags = new Hashtable();
    private static Hashtable  htmlwidths = new Hashtable();
    private static Integer    MAX_WIDTH = new Integer(Short.MAX_VALUE);

    static {
	htmltags.put("<p>", "");
	htmltags.put("<br>", "\r");
	htmlwidths.put("<p>", MAX_WIDTH);
	htmlwidths.put("<br>", MAX_WIDTH);
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTTextCanvas(YoixObject data, YoixBodyComponent parent) {

	super(data, parent);
    }

    ///////////////////////////////////
    //
    // YoixAWTTextCanvas Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	super.finalize();
    }


    protected final Dimension
    getLayoutSize(String name, Dimension size) {

	YoixObject  lval;
	YoixObject  obj;
	String      str;
	Vector      tokens;
	int         width;
	int         n;

	if (loadFont()) {
	    synchronized(FONTLOCK) {
		if ((obj = data.getObject(name)) != null && obj.notNull()) {
		    size = YoixMakeScreen.javaDimension(obj);
		    if (size.width <= 0 || size.height <= 0) {
			tokens = separate(currenttext);
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
				    if (size.width > 0 && textmode == YOIX_WORDMODE)
					tokens = wrapWords(tokens, size.width);
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


    final String
    getText() {

	return(currenttext);
    }


    public final void
    paint(Graphics g) {

	paintBackgroundImage(g);
	paintBorder(insets, g);

	if (lines != null || loadLines()) {
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


    final void
    reset() {

	setLines(null);
	repaint();
    }


    final void
    setAnchor() {

    }


    public final synchronized void
    setBounds(int x, int y, int width, int height) {

	if (totalsize.width != width || totalsize.height != height) {
	    setLines(null);
	    totalsize.width = width;
	    totalsize.height = height;
	    super.setBounds(x, y, width, height);
	    viewport.width = totalsize.width - insets.left - insets.right;
	    viewport.height = totalsize.height - insets.top - insets.bottom;
	} else super.setBounds(x, y, width, height);
    }


    final synchronized void
    setText(String text) {

	currenttext = text;
	reset();
    }


    final synchronized void
    setTextMode(int mode) {

	if (textmode != mode) {
	    textmode = mode;
	    reset();
	}
    }


    final synchronized void
    setTextWrap(boolean state) {

	if (textwrap != state) {
	    textwrap = state;
	    reset();
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private boolean
    loadLines() {

	Vector  tokens;

	if (loadFont()) {
	    if ((tokens = separate(currenttext)) != null) {
		if (totalsize.width > 0 && totalsize.height > 0) {
		    setLines(wrap(tokens));
		    syncViewport();
		}
	    }
	}

	return(lines != null);
    }


    private void
    paintLine(int n, Graphics g) {

	String  line;
	int     dx;
	int     x;
	int     y;

	if (lines != null && fm != null) {
	    if (n >= 0 && n < lines.length) {
		if ((line = lines[n]) != null) {
		    if ((dx = fm.stringWidth(line)) > 0) {
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
			g.drawString(line, x, n*cellsize.height + baseline);
		    }
		}
	    }
	}
    }


    private synchronized void
    setLines(String wrapped[]) {

	int  width;
	int  n;

	if (wrapped != null) {
	    extent.height = cellsize.height*wrapped.length;
	    if (textwrap == false) {
		extent.width = 0;
		for (n = 0; n < wrapped.length; n++) {
		    if ((width = fm.stringWidth(wrapped[n])) > extent.width)
			extent.width = width;
		}
	    } else extent.width = viewport.width;
	} else {
	    extent.height = 0;
	    extent.width = 0;
	}

	lines = wrapped;
    }


    private Vector
    separate(String text) {

	Vector  tokens;

	switch (textmode) {
	    case YOIX_LINEMODE:
		tokens = separateLines(text);
		break;

	    case YOIX_WORDMODE:
		tokens = separateWords(text);
		break;

	    default:
		tokens = new Vector(1);
		tokens.addElement(text);
		break;
	}

	return(tokens);
    }


    private Vector
    separateLines(String text) {

	StringBuffer  line;
	Vector        tokens;
	char          ch;
	int           length;
	int           col;
	int           n;

	if (text != null) {
	    tokens = new Vector();
	    length = text.length();
	    for (n = 0; n < length; n++) {
		line = new StringBuffer();
		for (col = 0; n < length; n++) {
		    if ((ch = text.charAt(n)) != '\n') {
			switch (ch) {
			    case '\t':
				do {
				    line.append(' ');
				    col++;
				} while ((col % 8) != 0);
				break;

			    default:
				line.append(ch);
				col++;
				break;
			}
		    } else break;
		}
		tokens.addElement(new String(line));
	    }
	} else tokens = null;

	return(tokens);
    }


    private Vector
    separateWords(String text) {

	StringTokenizer  tok;
	Vector           tokens;

	if (text != null) {
	    tokens = new Vector();
	    for (tok = new StringTokenizer(text, " \n\r\t"); tok.hasMoreTokens(); )
		tokens.addElement(tok.nextToken());
	} else tokens = null;

	return(tokens);
    }


    private String
    tokenValue(String token) {

	return(token.startsWith("<") && htmlwidths.containsKey(token.toLowerCase())
	    ? (String)htmltags.get(token.toLowerCase())
	    : token
	);
    }


    private int
    tokenWidth(String token) {

	return(token.startsWith("<") && htmlwidths.containsKey(token.toLowerCase())
	    ? ((Integer)htmlwidths.get(token.toLowerCase())).intValue()
	    : fm.stringWidth(token)
	);
    }


    private synchronized String[]
    wrap(Vector tokens) {

	String  wrapped[];
	int     n;

	if (textwrap && viewport.width > 0) {
	    switch (textmode) {
		case YOIX_LINEMODE:
		    tokens = wrapLines(tokens, viewport.width);
		    break;

		case YOIX_WORDMODE:
		    tokens = wrapWords(tokens, viewport.width);
		    break;
	    }
	}

	wrapped = new String[tokens.size()];
	for (n = 0; n < wrapped.length; n++)
	    wrapped[n] = (String)tokens.elementAt(n);

	return(wrapped);
    }


    private Vector
    wrapLines(Vector tokens, int maxwidth) {

	String  token;
	char    line[];
	int     width;
	int     m;
	int     n;

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

	return(tokens);
    }


    private Vector
    wrapWords(Vector tokens, int maxwidth) {

	String  token;
	String  line;
	String  word;
	Vector  buffer;
	int     width;
	int     n;

	buffer = new Vector();

	for (n = 0; n < tokens.size(); ) {
	    token = (String)tokens.elementAt(n++);
	    line = tokenValue(token);
	    width = tokenWidth(token);
	    for (; n < tokens.size(); n++) {
		token = (String)tokens.elementAt(n);
		if ((width += spacewidth + tokenWidth(token)) < maxwidth) {
		    word = tokenValue(token);
		    if (word.length() > 0)
			line += " " + word;
		} else break;
	    }
	    if (line.equals("\r") == false)
		buffer.addElement(line);
	}

	return(buffer);
    }
}

