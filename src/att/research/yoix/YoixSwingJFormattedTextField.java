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
import javax.swing.*;
import javax.swing.text.*;

//
// very incomplete - work in progress
//
class YoixSwingJFormattedTextField extends JFormattedTextField

    implements FocusListener,
	       YoixConstants,
	       YoixInterfaceDragable

{

    private YoixBodyComponent  parent;
    private YoixObject         data;

    private boolean  consumetabs = false;
    private boolean  modifytabs = false;
    private boolean  havefocus = false;
    private boolean  installempty = false;
    private Action   currentaction = null;
    private int      caretmodel = 0;

    //
    // The keystroke and action bindings that are installed right after
    // the JFormattedTextField is created. Used when we're asked to
    // restore the default actions.
    //

    private KeyStroke  defaultkeystrokes[] = null;
    private Action     defaultaction = null;

    //
    // An Action with an actionPerformed() that does nothing that's used
    // when we want to take complete control of the JTextComponent. Any
    // characters that need to be bound to emptyaction should be listed
    // in the emptykeystrokes array.
    //

    private static Action  emptyaction = new EmptyAction();

    private static KeyStroke  emptykeystrokes[] = {
	KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
	KeyStroke.getKeyStroke('\b'),
	KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
	KeyStroke.getKeyStroke('\t'),
	KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),

	//
	// What about function keys? F6, F8, and F10 appear to do stuff
	// on Windows and maybe elsewhere too - look into it later.
	//
    };

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJFormattedTextField(YoixObject data, YoixBodyComponent parent) {

	this.data = data;
	this.parent = parent;
	defaultaction = getKeymap().getDefaultAction();
	defaultkeystrokes = getKeymap().getBoundKeyStrokes();
	currentaction = defaultaction;
	addFocusListener(this);
    }

    ///////////////////////////////////
    //
    // FocusListener Methods
    //
    ///////////////////////////////////

    public void
    focusGained(FocusEvent e) {

	havefocus = true;
	if (installempty)
	    installAction(true);
    }


    public void
    focusLost(FocusEvent e) {

	havefocus = false;
	if (installempty)
	    installAction(false);
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
    // YoixSwingJTextField Methods
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


    public final Dimension
    getMinimumSize() {

	//
	// We sometimes run into lousy behavoir when layout managers
	// decide a JTextField might be too big for the cell that's
	// supposed contain it. In that case the layout manager may
	// decide to use the number returned by this method, but in
	// that case we still think the preferred size is the best
	// answer, which is why things are done this way.
	//

	return(super.getPreferredSize());
    }


    protected void
    processKeyEvent(KeyEvent e) {

	//
	// Using setModifiers() to clear CTRL_MASK despite the fact
	// that it's deprecated. An alternative, if this causes any
	// problems, is to build a copy that has CTRL_MASK cleared,
	// consume() the original KeyEvent, and hand the copy that
	// modifiers properly set to super.processKeyEvent(). Seems
	// like work that we probably can skip for now.
	//

	if (modifytabs) {
	    if (e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyChar() == '\t')
		e.setModifiers(~InputEvent.CTRL_MASK & e.getModifiers());
	}
	super.processKeyEvent(e);
	if (consumetabs) {
	    if (e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyChar() == '\t')
		e.consume();
	}
    }


    final synchronized void
    setAttributes(YoixObject dict) {
	if (arg[3].isDictionary()) {
	    if (arg[3].notNull()) {

		for (n = 0; n < boolAttrs.length; n++) {
		    if ((yobj2 = arg[3].getObject(boolAttrs[n])) != null || (yobj2 = arg[3].getObject(boolAttrs[n].toLowerCase())) != null) {
			if (yobj2.isInteger()) {
			    // these must be coordinated with boolAttrs
			    switch (n) {
			    case 0:
				renderer.setDecimalSeparatorAlwaysShown(yobj2.booleanValue());
				break;
			    case 1:
				renderer.setGroupingUsed(yobj2.booleanValue());
				break;
			    case 2:
				renderer.setParseIntegerOnly(yobj2.booleanValue());
				break;
			    case 3:
				renderer.setZeroNotShown(yobj2.booleanValue());
				break;
			    default:
				VM.abort(INTERNALERROR, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": unrecognized boolean attribute:" + boolAttrs[n]});
			    }
			} else {
			    VM.abort(BADVALUE, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": non-boolean value for boolean attribute:" + boolAttrs[n]});
			}
		    }
		}

		for (n = 0; n < intAttrs.length; n++) {
		    if ((yobj2 = arg[3].getObject(intAttrs[n])) != null || (yobj2 = arg[3].getObject(intAttrs[n].toLowerCase())) != null) {
			if (yobj2.isInteger()) {
			    // these must be coordinated with intAttrs
			    switch (n) {
			    case 0:
				renderer.setGroupingSize(yobj2.intValue());
				break;
			    case 1:
				renderer.setMaximumFractionDigits(yobj2.intValue());
				break;
			    case 2:
				renderer.setMaximumIntegerDigits(yobj2.intValue());
				break;
			    case 3:
				renderer.setMinimumFractionDigits(yobj2.intValue());
				break;
			    case 4:
				renderer.setMinimumIntegerDigits(yobj2.intValue());
				break;
			    case 5:
				renderer.setMultiplier(yobj2.intValue());
				break;
			    default:
				VM.abort(INTERNALERROR, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": unrecognized integer attribute:" + intAttrs[n]});
			    }
			} else {
			    VM.abort(BADVALUE, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": non-integer value for integer attribute:" + intAttrs[n]});
			}
		    }
		}

		for (n = 0; n < nbrAttrs.length; n++) {
		    if ((yobj2 = arg[3].getObject(nbrAttrs[n])) != null || (yobj2 = arg[3].getObject(nbrAttrs[n].toLowerCase())) != null) {
			if (yobj2.isNumber()) {
			    // these must be coordinated with nbrAttrs
			    switch (n) {
			    case 0:
				renderer.setOverflow(yobj2.doubleValue());
				break;
			    case 1:
				renderer.setUnderflow(yobj2.doubleValue());
				break;
			    default:
				VM.abort(INTERNALERROR, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": unrecognized number attribute:" + nbrAttrs[n]});
			    }
			} else {
			    VM.abort(BADVALUE, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": non-number value for number attribute:" + nbrAttrs[n]});
			}
		    }
		}

		for (n = 0; n < strAttrs.length; n++) {
		    if ((yobj2 = arg[3].getObject(strAttrs[n])) != null || (yobj2 = arg[3].getObject(strAttrs[n].toLowerCase())) != null) {
			if (yobj2.notNull() && yobj2.isString()) {
			    // these must be coordinated with strAttrs
			    switch (n) {
			    case 0:
				renderer.setFormat(yobj2.stringValue());
				break;
			    case 1:
				renderer.setNegativePrefix(yobj2.stringValue());
				break;
			    case 2:
				renderer.setNegativeSuffix(yobj2.stringValue());
				break;
			    case 3:
				renderer.setPositivePrefix(yobj2.stringValue());
				break;
			    case 4:
				renderer.setPositiveSuffix(yobj2.stringValue());
				break;
			    case 5:
				renderer.setLowSubstitute(new String[] { yobj2.stringValue() });
				break;
			    case 6:
				renderer.setHighSubstitute(new String[] { yobj2.stringValue() });
				break;
			    case 7:
				renderer.setInputFormat(yobj2.stringValue());
				break;
			    case 8:
				if (renderer instanceof YoixJTableDateRenderer)
				    ((YoixJTableDateRenderer)renderer).setTimeZone(yobj2.stringValue());
				break;
			    case 9:
				if (renderer instanceof YoixJTableDateRenderer)
				    ((YoixJTableDateRenderer)renderer).setInputTimeZone(yobj2.stringValue());
				break;
			    case 10:
				renderer.setRendererLocale(yobj2.stringValue());
				break;
			    case 11:
				renderer.setInputLocale(yobj2.stringValue());
				break;
			    default:
				VM.abort(INTERNALERROR, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": unrecognized string attribute:" + strAttrs[n]});
			    }
			} else if (yobj2.notNull() && yobj2.isArray()) {
			    switch (n) {
			    case 5:
				renderer.setLowSubstitute(YoixMake.javaStringArray(yobj2));
				break;
			    case 6:
				renderer.setHighSubstitute(YoixMake.javaStringArray(yobj2));
				break;
			    default:
				VM.abort(INTERNALERROR, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": unrecognized string array attribute:" + strAttrs[n]});
			    }
			} else {
			    VM.abort(BADVALUE, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": null or non-string value for string attribute:" + strAttrs[n]});
			}
		    }
		}
	    }
	} else VM.badArgument(name, 3);
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


    final synchronized void
    setEmptyAction(boolean empty) {

	installempty = empty;
	installAction(havefocus && empty);
    }


    final synchronized void
    setType(int newtype, boolean autotrim) {

	String oldtext = getText();
	int    oldtype = type;

	switch (newtype) {
	case YOIX_BOOLEAN_TYPE:
	case YOIX_DATE_TYPE:
	case YOIX_DOUBLE_TYPE:
	case YOIX_INTEGER_TYPE:
	case YOIX_MONEY_TYPE:
	case YOIX_PERCENT_TYPE:
	case YOIX_TIMER_TYPE:
	    autotrim = true;
	    // fall through to...
	case YOIX_STRING_TYPE:
	case YOIX_TEXT_TYPE:
	    type = newtype;
	    if (type != oldtype && oldtext != null) {
		if (autotrim)
		    oldtext = oldtext.trim();
		if (oldtext.length() > 0)
		    setText(); // TODO and think about
	    }
	    break;
	default:
	    VM.abort(BADVALUE, N_TYPE);
	    break;
	}
    }


    final synchronized void
    setValue(YoixObject newvalue) {
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

    private synchronized void
    installAction(boolean empty) {

	if (empty)
	    installAction(emptyaction, emptykeystrokes, false);
	else installAction(defaultaction, defaultkeystrokes, true);
    }


    private synchronized void
    installAction(Action action, KeyStroke keystrokes[], boolean focuskeys) {

	Keymap  map;
	int     n;

	if (action != null && keystrokes != null) {
	    if (action != currentaction) {
		if ((map = getKeymap()) != null) {
		    map.removeBindings();		// unnecessary??
		    map.setDefaultAction(action);
		    for (n = 0; n < keystrokes.length; n++)
			map.addActionForKeyStroke(keystrokes[n], action);
		}
		setFocusTraversalKeysEnabled(focuskeys);
		consumetabs = false;
		modifytabs = !focuskeys;
		currentaction = action;
	    }
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    static class EmptyAction extends TextAction {

	EmptyAction() {
	    super("yoix-empty-action");
	}

	public void
	actionPerformed(ActionEvent e) {

	}
    }
}

