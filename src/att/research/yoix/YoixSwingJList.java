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

class YoixSwingJList extends JList

    implements ComponentListener

{

    //
    // The original reason for this class was so we can set the visibility
    // of an index after the component is shown as we noticed occasional
    // weirdness otherwise. We also now adjust the answers returned by the
    // getMinimumSize() and getPreferredSize() when the number of visible
    // rows exceeds the total number of rows in the JList (8/3/07). Other
    // approaches, like returning getPreferredSize() when getMinimumSize()
    // is called handle many cases, but there are examples that don't work
    // quite right, so be careful and test thoroughly if you make changes
    // here. For example, make sure your tests include lists that display
    // fewer rows of text than their visible row count and also test them
    // with and without an enclosing JScrollPane.
    //

    private boolean  componentresized = false;
    private int      idx = -1;

    private double  columnwidth = 0;
    private int     visiblecolumns = 0;
    private int     rowheight = 0;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJList(ListModel datamodel) {

	super(datamodel);
	setCellRenderer(new YoixDefaultListCellRenderer());
	addComponentListener(this);
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

	if (componentresized == false) {
	    componentresized = true;
	    if (idx >= 0) {
		super.ensureIndexIsVisible(idx);
	    }
	}
    }


    public void
    componentShown(ComponentEvent e) {

    }

    ///////////////////////////////////
    //
    // YoixSwingJList Methods
    //
    ///////////////////////////////////

    public void
    ensureIndexIsVisible(int index) {

	if (componentresized == false)
	    idx = index;
	else super.ensureIndexIsVisible(index);
    }


    public Dimension
    getMinimumSize() {

	Dimension  size;
	Rectangle  rect;
	Insets     insets;
	int        visiblerows;
	int        modelrows;

	//
	// We want our row setting, which ends up as getVisibleRowCount(),
	// to really control Jlist behavior, so we have to override this
	// method and getPreferredSize() because Java's default answer will
	// be based on whatever's currently loaded in the JList.
	//
	// NOTE - seems a little strange that the minimum height can end up
	// larger than the preferred height, but things seems to work and a
	// few obscure examples behave better this way.
	//

	size = super.getMinimumSize();
	if (isMinimumSizeSet() == false) {
	    if (getLayoutOrientation() == VERTICAL) {
		visiblerows = getVisibleRowCount();
		if ((modelrows = getModel().getSize()) > 0) {
		    if (visiblerows > modelrows) {
			if ((rect = getCellBounds(0, 0)) != null)
			    size.height += (visiblerows - modelrows)*rect.height;
		    }
		    size.width = pickWidth(size.width);
		} else {
		    if (size.height == 0)
			size.height = visiblerows*getRowHeight();
		    if (size.width == 0)
			size.width = pickWidth(size.width);
		}
	    }
	}
	return(size);
    }


    public Dimension
    getPreferredSize() {

	Dimension  size;
	Rectangle  rect;
	Insets     insets;
	int        visiblerows;
	int        modelrows;

	//
	// We want our row setting, which ends up as getVisibleRowCount(),
	// to really control Jlist behavior, so we have to override this
	// method and getMinimumSize() because Java's default answer will
	// be based on whatever's currently loaded in the JList.
	//
	// NOTE - only modifying the answer when we're not in a JViewport
	// means the preferred height can be less than the minimum height.
	// Doesn't seem to cause problems anywhere and means any vertical
	// scroll adjustments won't be added until absolutely necessary.
	//

	size = super.getPreferredSize();
	if (isPreferredSizeSet() == false) {
	    if (!(getParent() instanceof JViewport)) {
		if (getLayoutOrientation() == VERTICAL) {
		    visiblerows = getVisibleRowCount();
		    if ((modelrows = getModel().getSize()) > 0) {
			if (visiblerows > modelrows) {
			    if ((rect = getCellBounds(0, 0)) != null)
				size.height += (visiblerows - modelrows)*rect.height;
			}
			size.width = pickWidth(size.width);
		    } else {
			if (size.height == 0)
			    size.height = visiblerows*getRowHeight();
			if (size.width == 0)
			    size.width = pickWidth(size.width);
		    }
		}
	    } else size.width = pickWidth(size.width);
	}
	return(size);
    }


    public int
    getVisibleColumnCount() {

	return(visiblecolumns);
    }


    public void
    setFont(Font font) {

	super.setFont(font);
	columnwidth = 0;
	rowheight = 0;
    }


    public final void
    setPrototypeCellValue(Object value) {

	super.setPrototypeCellValue(pickPrototypeValue(value));
    }


    final void
    setVisibleColumnCount(int columns) {

	columns = Math.max(0, columns);
	if (columns != visiblecolumns) {
	    visiblecolumns = columns;
	    invalidate();
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private double
    getColumnWidth() {

	Rectangle2D  rect;
	Font         font;

	//
	// There is no exact answer, so we don't care that this approach,
	// namely multiplying visiblecolumns by the width of one character,
	// isn't completely correct. We do try to get an answer that, when
	// multiplied by visiblecolumns, will be a reasonably tight upper
	// bound, so that's why we don't use FontMetrics.charWidth().
	//

	if (columnwidth == 0) {
	    font = getFont();
	    rect = font.getStringBounds("m", new FontRenderContext(new AffineTransform(), false, true));
	    columnwidth = rect.getWidth();
	}
	return(columnwidth);
    }


    private int
    getRowHeight() {

	Rectangle  rect;
	JList      list;

	if (rowheight == 0) {
	    list = new JList(new String[] {"M"});
	    list.setFont(getFont());
	    if ((rect = list.getCellBounds(0, 0)) != null)
		rowheight = rect.height;
	}
	return(rowheight);
    }


    private Object
    pickPrototypeValue(Object value) {

	YoixSwingLabelItem  labelitem;
	FontRenderContext   frc;
	Rectangle2D         bounds;
	ListModel           model;
	double              width;
	String              text;
	Font                font;
	int                 index;

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


    private int
    pickWidth(int width) {

	if (visiblecolumns > 0)
	    width = (int)Math.ceil(visiblecolumns*getColumnWidth());
	return(width);
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixDefaultListCellRenderer extends DefaultListCellRenderer {

	public Component
	getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

	    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    if (value instanceof YoixSwingLabelItem)
		setIcon(((YoixSwingLabelItem)value).getIcon());
	    return(this);
	}
    }
}

