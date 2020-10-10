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
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

class YoixSwingJComboBox extends JComboBox

    implements YoixConstants,
	       YoixConstantsJFC,
	       YoixConstantsSwing

{

    //
    // An extension of JComboBox that tries to deal with several issues,
    // but there may be more to do. For example, adding synchronization
    // to a few methods (e.g., to protect the model) might be worthwhile
    // but we decided to be conservative for now.
    //
    // Perfomance can be a concern when a JComboBox displays lots of items.
    // The culprit turns out to be buried in JList text measuring UI code,
    // but it's code that can be short-circuited by asking the JList to use
    // a prototype cell value instead of measuring each label. Unfortunately
    // Java currently (1.6.0) doesn't do it when a prototype is assigned to
    // a JComboBox and there's no direct access to the JList that the UI is
    // using. Access to the JList was achieved with our own ListCellRenderer
    // class, which is an extension of Swing's DefaultListCellRenderer, and
    // when a Yoix script assigns a new value to our prototypevalue field we
    // make sure the prototype is passed to the JList in a timely fashion.
    // These changes were made on 5/24/08.
    //

    private YoixBodyComponentSwing  parent;
    private YoixObject              data;

    private String editaction = null;

    //
    // Stuff used to try to get our hands on the JList that's used by the
    // look and feel.
    //

    private boolean  updateprototype = false;
    private Object   prototype = null;

    //
    // Default action commands.
    //

    private static final String  COMBOBOX_SELECTED = "comboBoxSelected";
    private static final String  COMBOBOX_EDITED = "comboBoxEdited";

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJComboBox(YoixObject data, YoixBodyComponentSwing parent) {

	super();

	this.parent = parent;
	this.data = data;
	setModel(new DefaultComboBoxModel());
	setRenderer(new YoixDefaultListCellRenderer());
	setActionCommands(null, null);
    }

    ///////////////////////////////////
    //
    // YoixSwingJComboBox Methods
    //
    ///////////////////////////////////

    public final void
    actionPerformed(ActionEvent e) {

        Object  newitem;
	String  oldcommand;

	//
	// When this is triggered by a loss of focus, ID will be 0 and
	// command will "".
	//

	if (e.getID() != 0 || e.getActionCommand().length() > 0 || data.getBoolean(N_FOCUSACTION, true)) {
	    newitem= getEditor().getItem();
	    setPopupVisible(false);
	    getModel().setSelectedItem(newitem);
	    oldcommand = getActionCommand();
	    setActionCommand(editaction);
	    fireActionEvent();
	    setActionCommand(oldcommand);
	}
    }


    final void
    clearCachedSizes() {

	//
	// The BasicComboBoxUI uses two booleans to decide whether or not
	// the values stored in two "cache" Dimensions are valid. One is
	// protected and the other is private and there's apparently no
	// official way to clear the caches, but the booleans are reset
	// when certain PropertyChangeEvents are handled. One of them is
	// the "renderer" change, which otherwise looks pretty harmless,
	// so that's what we use.
	//
	// NOTE - changing fonts only ends up clearing one of the caches,
	// so inconsistent or bad cached values can be a problem when the
	// font is changed. Really looks like a BasicComboBoxUI mistake,
	// so we call this method whenever the font changes. There could
	// be other cases where this is needed.
	//
	// NOTE - setting updateprototype to true is supposed to force the
	// prototype code in our ListCellRenderer to run again. This only
	// matters when we're supposed to measure all labels and the new
	// font means we pick a different label as the widest.
	//

	updateprototype = true;
	firePropertyChange("renderer", null, null);
    }


    protected void
    finalize() {

	data = null;
	parent = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }

    //
    //  You can use this approach if you want the ItemEvent to return
    //  the mapping value. Left it out for now since we don't know if
    //  we want to change all Swing List behavior to be the same and
    //  ditto for AWT. We need to revisit this train of thought.
    //
    //
    //  protected void
    //	fireItemStateChanged(ItemEvent e) {
    //      if (e.getItem() instanceof YoixSwingLabelItem) {
    //          e = new ItemEvent(e.getItemSelectable(), e.getID(), ((YoixSwingLabelItem)(e.getItem())).getValue(), e.getStateChange());
    //      }
    //      super.fireItemStateChanged(e);
    //  }
    //

    final String[]
    getActionCommands() {

	return(new String[] {getActionCommand(), editaction});
    }


    final int
    getRows() {

	return(getMaximumRowCount());
    }


    public final void
    setActionCommand(String cmd) {

	super.setActionCommand(cmd != null ? cmd : COMBOBOX_SELECTED);
    }


    final void
    setActionCommands(String cmds[]) {

	setActionCommand(cmds[0]);
	setEditActionCommand(cmds[1]);
    }


    final void
    setActionCommands(String cmd, String alt) {

	setActionCommand(cmd);
	setEditActionCommand(alt);
    }


    public final void
    setBackground(Color color) {

	Component  comps[];
	Color      childcolor;
	int        n;

	//
	// With Aqua, setting JComboBox background affects the area around
	// the combobox "button" and the popupmenu portion too. So setting
	// the color should set the popupmenu color, but the area around
	// the button should match the prevailing background.
	//

	super.setBackground(color);
	if (parent != null) {
	    if (UIManager.getLookAndFeel().getClass().getName().indexOf("Aqua") >= 0) {
		if ((comps = getComponents()) != null) {
		    childcolor = YoixMakeScreen.javaBackground(parent.findClosestValue(N_BACKGROUND));
		    for (n = 0; n < comps.length; n++) {
			if (comps[n].getClass().getName().indexOf("Button") >= 0)
			    comps[n].setBackground(childcolor);
			else comps[n].setBackground(color);
		    }
		}
	    }
	}
    }


    final void
    setEditActionCommand(String cmd) {

	editaction = (cmd != null) ? cmd : COMBOBOX_EDITED;
    }


    final void
    setEditorForeground(Color color) {

	getEditor().getEditorComponent().setForeground(color != null ? color : Color.black);
    }


    public final void
    setFont(Font font) {

	//
	// The PropertyChangeEvent generated when the font changes only
	// resets one cache (the display cache isn't touched) so if the
	// layout manager calls getPreferredSize() or getMinimumSize()
	// it will usually get the old answer and won't notice that the
	// JComboBox's size requirements have changed. Calling our own
	// clearCachedSizes() is a kludge but should clear both caches.
	//

	super.setFont(font);
	clearCachedSizes();
    }


    final void
    setHorizontalAlignment(int alignment) {

	((DefaultListCellRenderer)getRenderer()).setHorizontalAlignment(alignment);
    }


    final void
    setListData(Object values[]) {

	setModel(new DefaultComboBoxModel(values));
	updateprototype = true;
    }


    public final void
    setPrototypeDisplayValue(Object value) {

	//
	// The responsibility for the super.setPrototypeDisplayValue() call
	// has been moved to our ListCellRenderer class, which is also where
	// we update the UI list, because the most likely non-null value is
	// one that asks pickPrototypeValue() to choose the widest label but
	// the labels aren't necessarily loaded yet.
	// 

	if (prototype != value && (prototype == null || prototype.equals(value) == false)) {
	    prototype = value;
	    updateprototype = true;
	}
    }


    final void
    setRows(int count) {

	if (count > 0)
	    super.setMaximumRowCount(count);
    }


    public final void
    setSelectedItem(Object item) {

	YoixSwingLabelItem  labelitem;
	ComboBoxModel       model;
	Object              reminder;
	String              target;
	int                 index;

	//
	// We extend this method because we need to check the value field
	// of the YoixSwingLabelItem object rather than the object itself.
	//
	// NOTE - all calls from the YoixBodyComponentSwing class pass an
	// item that's a String or null, but that may not be the case when
	// this JComboBox is used as a table cell editor. The original code
	// tested
	//
	//     if (item != null && item instanceof String && !isEditable())
	//
	// before the code that checked individual the YoixSwingLabelItems,
	// be we decided to convert item to a String using toString() when
	// item wasn't a String and then search for the converted value in
	// the YoixSwingLabelItems. Could have done the conversion in the
	//
	//     delegate.setValue(value);
	//
	// call that's made in YoixSwingJTable.java, but that wouldn't take
	// care of any other calls (e.g., perhaps in YoixSwingJTree). This
	// change was made on 10/25/10 and needs to be thoroughly tested!!
	// If there's a problem just change the JTable call to
	//
	//     delegate.setValue(value.toString());
	//
	// and the initialization issue when a JComboBox is used as a cell
	// editor in a JTable should disappear.
	//

	reminder = selectedItemReminder;

	if (reminder == null || !reminder.equals(item)) {
	    if (item != null && !isEditable()) {	// old code made sure item was a String
		target = (item instanceof String) ? (String)item : item.toString();
		if ((model = getModel()) != null) {
		    for (index = 0; index < model.getSize(); index++) {
			if ((labelitem = (YoixSwingLabelItem)(model.getElementAt(index))) != null) {
			    if (target.equals(labelitem.getValue())) {
				item = labelitem;
				break;
			    }
			}
		    }
		}
	    }
	    super.setSelectedItem(item);
	} else {
	    //
	    // An editable JComboBox() would sometimes not display the
	    // the selected item. Actually only seemed to happen if the
	    // item was the first one loaded. Forcing the issue for an
	    // "invalid" JComboBox seemed to fix the behavior, but we
	    // didn't try to really track it down. The old version of
	    // this method just called fireActionEvent(), which is all
	    // super.setSelectedItem() will do in this case, but that
	    // seemed like a version dependent assumption (that wasn't
	    // the cause of the bad behavior).
	    //
	    if (isEditable() && isValid() == false)
		super.setSelectedItem(null);
	    super.setSelectedItem(item);	// was fireActionEvent();
	}
	//
	// Occasionally helps make sure the label is properly painted.
	//
	repaint();
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private Object
    pickPrototypeValue() {

	YoixSwingLabelItem  labelitem;
	FontRenderContext   frc;
	ComboBoxModel       model;
	Rectangle2D         bounds;
	Object              value;
	double              width;
	String              text;
	Font                font;
	int                 index;

	value = prototype;

	if (value instanceof String) {
	    if (((String)value).length() == 0) {
		value = null;
		if ((model = getModel()) != null) {
		    font = getFont();
		    frc = new FontRenderContext(new AffineTransform(), false, false);
		    width = 0;
		    for (index = 0; index < model.getSize(); index++) {
			if ((labelitem = (YoixSwingLabelItem)(model.getElementAt(index))) != null) {
			    if ((text = labelitem.getText()) != null && text != "") {
				if ((bounds = font.getStringBounds(text, frc)) != null) {
				    if (bounds.getWidth() > width) {
					width = bounds.getWidth();
					value = text;
				    }
				}
			    }
			}
		    }
		}
	    }
	}
	return(value);
    }


    private void
    updatePrototypeValue(JList list) {

	Object  value;

	if (list != null) {
	    if (updateprototype) {
		updateprototype = false;
		value = pickPrototypeValue();
		super.setPrototypeDisplayValue(value);
		list.setPrototypeCellValue(value);
		//
		// Make sure the fixed cell width and height are reset when
		// prototype is null - list.setPrototypeCellValue() may not
		// do it.
		//
		if (value == null) {
		    list.setFixedCellWidth(-1);
		    list.setFixedCellHeight(-1);
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixDefaultListCellRenderer extends DefaultListCellRenderer {

	//
	// We use this class to propagate the proptotype values assigned to
	// the JComboBox to the JList used by the UI. It's not something that
	// Java (1.6.0 and older) currently does, but we obviously think it
	// should even though you'll probably only notice it when you start
	// working with really big lists. The changes were added on 5/24/08.
	//

	public Component
	getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean hasfocus) {

	    Component  comp;

	    comp = super.getListCellRendererComponent(list, value, index, selected, hasfocus);
	    if (value instanceof YoixSwingLabelItem)
		setIcon(((YoixSwingLabelItem)value).getIcon());
	    updatePrototypeValue(list);
	    return(comp);
	}
    }
}

