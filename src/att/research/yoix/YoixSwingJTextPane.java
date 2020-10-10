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
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.rtf.*;
import java.lang.reflect.*;

class YoixSwingJTextPane extends JTextPane

    implements YoixConstants,
               YoixInterfaceDragable

{

    private YoixBodyComponent  parent;
    private YoixObject         data;

    private HashMap  bodyattrs = null;
    private Color    lastfore = null;
    private Color    lastback = null;
    private Font     lastfont = null;
    private int      caretmodel = 0;
    private int      basealign = -1;
    private int      lastalign = -1;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJTextPane() {

	this(null, null);
    }


    YoixSwingJTextPane(YoixObject data, YoixBodyComponent parent) {

	this.data = data;
	this.parent = parent;
    }

    ///////////////////////////////////
    //
    // YoixInterfaceDragable Methods
    //
    ///////////////////////////////////

    public boolean
    getDragPossible() {

	boolean  result;
	Caret    caret;

	caret = getCaret();
	if (caret instanceof YoixSwingDefaultCaret)
	    result = ((YoixSwingDefaultCaret)caret).getDragPossible();
	else result = true;
	return(result);
    }


    public boolean
    getDragStarted() {

	return(parent.getDragStarted());
    }


    public boolean
    isDragGesturePossible() {

	 return(parent.isDragGesturePossible());
    }


    public boolean
    isDragPossible() {

	return(parent.isDragPossible() && getSelectedText() != null);
    }


    public void
    updateDropTarget(boolean dragging, boolean accepted, Point p) {

	Caret  caret;

	caret = getCaret();
	if (caret instanceof YoixSwingDefaultCaret)
	    ((YoixSwingDefaultCaret)caret).updateDropTarget(dragging, accepted, p);
    }

    ///////////////////////////////////
    //
    // YoixSwingJTextPane Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	data = null;
	parent = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    public Color
    getBackground() {

	StyledDocument  doc;
	AttributeSet    attr;
	Color           color;

	doc = getStyledDocument();
	color = null;

	if (doc != null && getCaret() != null) {
	    attr = doc.getParagraphElement(getCaretPosition()).getAttributes();
	    if (attr.isDefined(StyleConstants.Background))
		color = StyleConstants.getBackground(attr);
	}
	if (color == null)
	    color = getLastBackground();
	if (color == null)
	    color = super.getBackground();

	return(color);
    }


    public Font
    getFont() {

	StyledDocument  doc;
	AttributeSet    attr;
	boolean         is_bold;
	boolean         is_italic;
	String          font_family;
	Font            font;
	int             font_size;

	doc = getStyledDocument();
	font = null;

	if (doc != null && getCaret() != null) {
	    attr = doc.getParagraphElement(getCaretPosition()).getAttributes();
	    if (attr.isDefined(StyleConstants.FontFamily)) {
		font_family = StyleConstants.getFontFamily(attr);
		font_size = StyleConstants.getFontSize(attr);
		is_bold = StyleConstants.isBold(attr);
		is_italic = StyleConstants.isItalic(attr);
		font = new Font(font_family, is_bold ? Font.BOLD : is_italic ? Font.ITALIC : Font.PLAIN, font_size);
	    }
	}
	if (font == null)
	    font = getLastFont();
	if (font == null)
	    font = super.getFont();

	return(font);
    }


    public Color
    getForeground() {

	StyledDocument  doc;
	AttributeSet    attr;
	Color           color;

	doc = getStyledDocument();
	color = null;

	if (doc != null && getCaret() != null) {
	    attr = doc.getParagraphElement(getCaretPosition()).getAttributes();
	    if (attr.isDefined(StyleConstants.Foreground))
		color = StyleConstants.getForeground(attr);
	}
	if (color == null)
	    color = getLastForeground();
	if (color == null)
	    color = super.getForeground();

	return(color);
    }


    public int
    getHorizontalAlignment() {

	StyledDocument  doc;
	AttributeSet    attr;
	Element         elem;
	Element         kid;
	Integer         value;
	int             alignment;
	int             cnt;
	int             n;

	doc = getStyledDocument();

	if (doc != null && getCaret() != null) {
	    attr = doc.getParagraphElement(getCaretPosition()).getAttributes();
	    value = (Integer)(attr.getAttribute(StyleConstants.Alignment));
	    if (value == null)
		value = new Integer(swingToStyleAlignment(basealign < 0 ? getLastAlignment() : basealign, false));
	    alignment = value.intValue();

	    switch (alignment) {
		case StyleConstants.ALIGN_RIGHT:
		case YOIX_RIGHT:
		    alignment = SwingConstants.RIGHT;
		    break;

		case StyleConstants.ALIGN_LEFT:
		case YOIX_LEFT:
		    alignment = SwingConstants.LEFT;
		    break;

		case StyleConstants.ALIGN_CENTER:
		case YOIX_CENTER:
		    alignment = SwingConstants.CENTER;
		    break;

		default:
		    VM.abort(INTERNALERROR);
		    break;
	    }
	} else alignment = YOIX_LEFT;

	return(alignment);
    }


    public int
    getSelectionEnd() {

	return(getCaret() == null ? 0 : super.getSelectionEnd());
    }


    public int
    getSelectionStart() {

	return(getCaret() == null ? 0 : super.getSelectionStart());
    }


    public String
    getText() {

	ByteArrayOutputStream  stream;
	Document               doc;
	EditorKit              kit;
	String                 text;

	if ((kit = getEditorKit()) instanceof RTFEditorKit) {
	    doc = getDocument();
	    stream = new ByteArrayOutputStream();
	    try {
		kit.write(stream, doc, 0, doc.getLength());
		text = new String(stream.toByteArray(), YoixConverter.getISO88591Encoding());
	    }
	    catch(Exception e) {
		VM.recordException(e);
		text = null;
	    }
	    stream = null;
	} else text = super.getText();

	return(text);
    }


    final void
    setAnchor(String reference) {

	//
	// The scrollToReference() method was protected until 1.4.1, so
	// making the call from this class eliminates any problems.
	//

	super.scrollToReference(reference);
    }


    public void
    setBackground(Color color) {

	SimpleAttributeSet  attr;
	StyledDocument      doc;

	super.setBackground(lastback = color);

	doc = getStyledDocument();

	if (doc != null && color != null) {
	    attr = new SimpleAttributeSet();
	    StyleConstants.setBackground(attr, color);
	    if (getSelectionStart() >= doc.getLength())
		doc.setParagraphAttributes(getSelectionStart(), 0, attr, false);
	    else doc.setParagraphAttributes(getSelectionStart(), getSelectionEnd() - getSelectionStart(), attr, false);

	    saveAttrs("background", YoixMiscPrintf.strfmt("#%06X", (0xFFFFFF & color.getRGB())));
	}
    }


    final synchronized void
    setCaretModel(int model) {

	Caret  caret;

	if (caretmodel != model) {
	    caretmodel = model;
	    syncCaretToModel();
	    caret = getCaret();
	    if (caret instanceof YoixSwingDefaultCaret)
		((YoixSwingDefaultCaret)caret).setCaretModel(caretmodel);
	}
    }


    public void
    setFont(Font font) {

	SimpleAttributeSet  attr;
	StyledDocument      doc;
	boolean             font_bold = false;
	boolean             font_italic = false;
	String              font_family;
	int                 font_size;

	super.setFont(lastfont = font);

	doc = getStyledDocument();

	if (doc != null && font != null) {
	    font_family = font.getFamily();
	    font_size = font.getSize();
	    if (!font.isPlain()) {
		font_bold = font.isBold();
		font_italic = font.isItalic();
	    }

	    attr = new SimpleAttributeSet();
	    StyleConstants.setFontFamily(attr, font_family);
	    StyleConstants.setFontSize(attr, font_size);
	    StyleConstants.setBold(attr, font_bold);
	    StyleConstants.setItalic(attr, font_italic);
	    if (getSelectionStart() >= doc.getLength())
		doc.setParagraphAttributes(getSelectionStart(), 0, attr, false);
	    else doc.setParagraphAttributes(getSelectionStart(), getSelectionEnd() - getSelectionStart(), attr, false);

	    saveAttrs("font-family", font_family.indexOf(" ") >= 0 ? "'" + font_family + "'" : font_family);
	    saveAttrs("font-size", font_size + "pt");
	    saveAttrs("font-style", font_italic ? "italic" : "normal");
	    saveAttrs("font-weight", font_bold ? "bold" : "normal");
	}
    }


    public void
    setForeground(Color color) {

	SimpleAttributeSet  attr;
	StyledDocument      doc;

	super.setForeground(lastfore = color);

	doc = getStyledDocument();

	if (doc != null && color != null) {
	    attr = new SimpleAttributeSet();
	    StyleConstants.setForeground(attr, color);
	    if (getSelectionStart() >= doc.getLength())
		doc.setParagraphAttributes(getSelectionStart(), 0, attr, false);
	    else doc.setParagraphAttributes(getSelectionStart(), getSelectionEnd() - getSelectionStart(), attr, false);

	    saveAttrs("color", YoixMiscPrintf.strfmt("#%06X", (0xFFFFFF & color.getRGB())));
	}
    }


    public void
    setHorizontalAlignment(int alignment) {

	SimpleAttributeSet  attr;
	StyledDocument      doc;
	int                 start;
	int                 end;
	int                 len;

	doc = getStyledDocument();

	if (doc != null) {
	    alignment = swingToStyleAlignment(lastalign = alignment, true);
	    attr = new SimpleAttributeSet();
	    StyleConstants.setAlignment(attr, alignment);
	    start = getSelectionStart();
	    end = getSelectionEnd();
	    len = doc.getLength();
	    if (start == 0 && end == 0)
		doc.setParagraphAttributes(0, len, attr, false);
	    else if (start >= len)
		doc.setParagraphAttributes(start, 0, attr, false);
	    else doc.setParagraphAttributes(start, end - start, attr, false);
	}
    }


    public void
    setText(String text) {

	StringReader  sr;
	EditorKit     kit;
	Document      doc;
	int           len;

	doc = getDocument();

	if (doc instanceof HTMLDocument) {
	    setHorizontalAlignment(basealign = getLastAlignment());
	    setForeground(getLastForeground());
	    setBackground(getLastBackground());
	    setFont(getLastFont());
	    updateHTMLBodyStyle();
	    try {
		super.setText(text);
	    }
	    catch(RuntimeException e) {
		VM.abort(HTMLERROR, e);
	    }
	} else if ((kit = getEditorKit()) instanceof RTFEditorKit) {
	    super.setText("");
	    setHorizontalAlignment(basealign = getLastAlignment());
	    setForeground(getLastForeground());
	    setBackground(getLastBackground());
	    setFont(getLastFont());
	    if ((len = text.length()) > 0) {
		try {
		    sr = new StringReader(text);
		    kit.read(sr, doc, 0);
		}
		catch(UnsupportedEncodingException uce) {
		    VM.recordException(uce);
		}
		catch(Exception io) {
		    VM.recordException(io);
		}
		if (doc.getLength() == 0) {
		    try {
			doc.insertString(0, text, null);
		    }
		    catch(BadLocationException ble) {
			VM.recordException(ble);
			super.setText(text);
		    }
		}
	    }
	} else {
 	    try {
 		doc.remove(0, doc.getLength());
 		setHorizontalAlignment(basealign = getLastAlignment());
		setForeground(getLastForeground());
		setBackground(getLastBackground());
		setFont(getLastFont());
 		doc.insertString(0, text, null);
 	    }
 	    catch(BadLocationException ble) {
		VM.recordException(ble);
 		super.setText("");
 		setHorizontalAlignment(basealign = getLastAlignment());
		setForeground(getLastForeground());
		setBackground(getLastBackground());
		setFont(getLastFont());
 		super.setText(text);
 	    }
	}
    }


    final synchronized void
    syncCaretToModel() {

	Highlighter  highlighter;
	Caret        newcaret;
	Caret        oldcaret;
	int          dot;
	int          mark;

	oldcaret = getCaret();

	if (caretmodel == 0) {
	    if (isDragGesturePossible()) {
		if (oldcaret instanceof YoixSwingDefaultCaret)
		    newcaret = null;
		else newcaret = new YoixSwingDefaultCaret();
	    } else {
		if (oldcaret instanceof YoixSwingDefaultCaret)
		    newcaret = new DefaultCaret();
		else newcaret = null;
	    }
	} else {
	    if (oldcaret instanceof YoixSwingDefaultCaret)
		newcaret = null;
	    else newcaret = new YoixSwingDefaultCaret();
	}

	if (newcaret != null) {
	    if (newcaret instanceof YoixSwingDefaultCaret)
		((YoixSwingDefaultCaret)newcaret).setCaretModel(caretmodel);
	    //
	    // Not sure about removing highlights, but we encountered some
	    // inconsistent behavoir (maybe caused by YoixSwingHighlighter)
	    // if we didn't do this. Behavior wasn't a serious problem and
	    // we didn't track it all the way down, so we may revisit this.
	    //
	    highlighter = getHighlighter();
	    highlighter.removeAllHighlights();
	    if (oldcaret != null) {
		dot = oldcaret.getDot();	// just in case
		setCaret(newcaret);
		newcaret.setDot(dot);		// must follow setCaret()
	    } else setCaret(newcaret);
	    repaint();
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private int
    getLastAlignment() {

	return(lastalign < 0 ? SwingConstants.LEFT : lastalign);
    }


    private Color
    getLastBackground() {

	return(lastback == null ? Color.white : lastback);
    }


    private Color
    getLastForeground() {

	return(lastfore == null ? Color.black : lastfore);
    }


    private Font
    getLastFont() {

	return(lastfont == null ? lastfont = new Font("Monospaced", Font.PLAIN, 12) : lastfont);
    }


    private synchronized void
    mergeAttrs(HashMap target) {

	if (bodyattrs != null)
	    target.putAll(bodyattrs);
    }


    private synchronized void
    saveAttrs(String name, String value) {

	if (bodyattrs == null)
	    bodyattrs = new HashMap();
	bodyattrs.put(name, value);
    }


    private void
    updateHTMLBodyStyle() {

 	StyledEditorKit  kit;
 	StyledDocument   doc;
 	StringBuffer     sb;
	Enumeration      names;
	StyleSheet       ss;
	Iterator         keys;
	HashMap          map;
	Object           name;
	Style            st;
	int              n;

	if ((doc = getStyledDocument()) != null && doc instanceof HTMLDocument) {
	    ss = ((HTMLDocument)doc).getStyleSheet();
	    st = ss.getRule("body");
	    map = new HashMap();
	    names = st.getAttributeNames();
	    while (names.hasMoreElements()) {
		name = names.nextElement();
		map.put(name.toString(), st.getAttribute(name));
	    }
	    mergeAttrs(map);
	    sb = new StringBuffer("body {");
	    keys = map.keySet().iterator();
	    while (keys.hasNext()) {
		name = keys.next();
		if (!name.equals("name")) {
		    sb.append(' ');
		    sb.append(name);
		    sb.append(": ");
		    sb.append(map.get(name));
		    sb.append(";");
		}
	    }
	    sb.append(" }");
	    ss.addRule(sb.toString());
	}
    }


    private int
    swingToStyleAlignment(int alignment, boolean save) {

	switch (alignment) {
	    case SwingConstants.RIGHT:
	    case SwingConstants.TRAILING:
		alignment = StyleConstants.ALIGN_RIGHT;
		if (save)
		    saveAttrs("text-align", "right");
		break;

	    case SwingConstants.LEFT:
	    case SwingConstants.LEADING:
		alignment = StyleConstants.ALIGN_LEFT;
		if (save)
		    saveAttrs("text-align", "left");
		break;

	    case SwingConstants.CENTER:
		alignment = StyleConstants.ALIGN_CENTER;
		if (save) saveAttrs("text-align", "center");
		break;

	    default:
		VM.abort(INTERNALERROR);
		break;
	}
	return(alignment);
    }
}

