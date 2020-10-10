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

class YoixAWTGridLayout extends GridLayout

    implements YoixConstants

{

    //
    // We've made some changes to Java's GridLayout and at least one
    // may affect existing applications, but setting the model to -1,
    // which can be done in Yoix applications should restore the old
    // behavior. The three models are described below. We also added
    // useall and orientation fields. When useall is true, which is
    // the default, all components, whether they're visible or not,
    // are used in sizing and layout calculations. Setting useall to
    // false omits components that aren't visible, which corresponds
    // to the behavior you get with most other layout managers. The
    // orientation determines how components are added to the grid
    // and also controls some important model based calculations.
    //
    // Model 0, which is the default model, uses numbers assigned
    // to rows and columns as starting values that never decrease.
    // Actual values are selected based on the value of orientation
    // and the number of components in the grid. An orientation of
    // YOIX_HORIZONTAL means components are placed in the grid in
    // rows that have the specified number of columns and the number
    // of rows will be adjusted up to accomodate all the components.
    // Setting orientation to YOIX_VERTICAL exchanges the roles of
    // rows and columns - components are placed in columns that have
    // the specified number of rows and the number of columns will
    // be adjusted up to accomodate all the components. Setting rows
    // or columns to 0 means pick a value based on the orientation
    // and number of components. For example, when rows is 0 use a
    // value of 1 in the horizonal orientation and a value equal to
    // the number of components in the vertical orientation.
    //
    // Model 1 is a slight variation of model 0 that probably won't
    // be used much. This model behaves exactly like model 0 except
    // that rows or columns are only adjusted up to accomodate all
    // the components when they're assigned a value of 0. Rows an
    // columns should be carefully selected in this model because
    // there's no guarantee all components will be displayed when
    // rows and columns are both non-zero.
    //
    // Finally, setting model to -1 means use Java's model in which
    // the number assigned to columns is ignored (unless rows is 0)
    // and instead columns is calculated as the smallest number that
    // is needed to hold all the components in the specified number
    // of rows. The fact that columns is usually ignored means this
    // model can lead to confusing behavior when you try to control
    // a GridLayout by adjusting rows and columns, and that's the
    // main reason why we decided to use a different default.
    //

    private boolean  useall = true;
    private int      model = 0;
    private int      orientation = YOIX_HORIZONTAL;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTGridLayout(int model, int rows, int columns, int hgap, int vgap, int orientation, boolean useall) {

	super();
	setModel(model);
	setOrientation(orientation);
	setUseAll(useall);
	setGrid(rows, columns);
	setHgap(hgap);
	setVgap(vgap);
    }

    ///////////////////////////////////
    //
    // YoixAWTGridLayout Methods
    //
    ///////////////////////////////////

    public final Dimension
    preferredLayoutSize(Container parent) {

	return(getLayoutSize(parent, V_PREFERREDSIZE));
    }


    public final Dimension
    maximumLayoutSize(Container parent) {

	return(getLayoutSize(parent, V_MAXIMUMSIZE));
    }


    public final Dimension
    minimumLayoutSize(Container parent) {

	return(getLayoutSize(parent, V_MINIMUMSIZE));
    }


    public final void
    layoutContainer(Container parent) {

	Component  components[];
	Dimension  size;
	Insets     insets;
	int        cells;
	int        row;
	int        rows;
	int        column;
	int        columns;
	int        hgap;
	int        vgap;
	int        width;
	int        height;
	int        dx;
	int        dy;
	int        x;
	int        y;
	int        n;

	synchronized (parent.getTreeLock()) {
	    if ((components = getComponents(parent)) != null) {
		cells = components.length;
		rows = getRowCount(cells);
		columns = getColumnCount(cells);
		hgap = getHgap();
		vgap = getVgap();
		size = parent.getSize();
		insets = parent.getInsets();

		width = (size.width - (insets.left + insets.right) - (columns - 1)*hgap)/columns;
		height = (size.height - (insets.top + insets.bottom) - (rows - 1)*vgap)/rows;

		dx = width + hgap;
		dy = height + vgap;
		x = insets.left;
		for (column = 0; column < columns; column++) {
		    y = insets.top;
		    for (row = 0; row < rows; row++) {
			if (orientation == YOIX_VERTICAL)
			    n = column*rows + row;
			else n = row*columns + column;
			if (n < cells)
			    components[n].setBounds(x, y, width, height);
			y += dy;
		    }
		    x += dx;
		}
	    }
	}
    }


    public final void
    setColumns(int columns) {

	super.setColumns(Math.max(columns, 0));
    }


    final void
    setGrid(int rows, int columns) {

	//
	// Setting rows and columns to legitimate values is harder than
	// it should be - see a 1.1.X version of java.awt.GridLayout if
	// you want more details. A try/catch is not sufficient because
	// GridLayout doesn't check for negative numbers.
	//

	if (rows > 0) {
	    super.setRows(rows);
	    super.setColumns(Math.max(columns, 0));
	} else if (columns > 0) {
	    super.setColumns(columns);
	    super.setRows(Math.max(rows, 0));
	} else {
	    if (orientation == YOIX_HORIZONTAL) {
		super.setRows(1);
		super.setColumns(0);
	    } else {
		super.setColumns(1);
		super.setRows(0);
	    }
	}
    }


    final void
    setModel(int value) {

	model = (value != 0) ? (value < 0 ? -1 : 1) : 0;
    }


    final void
    setOrientation(int value) {

	orientation = (value == YOIX_VERTICAL) ? value : YOIX_HORIZONTAL;
    }


    public final void
    setRows(int rows) {

	super.setRows(Math.max(rows, 0));
    }


    final void
    setUseAll(boolean state) {

	useall = state;
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private int
    getColumnCount(int cells) {

	int  columns;
	int  rows;

	columns = getColumns();
	rows = getRows();

	switch (model) {
	    case -1:
		if (rows > 0)
		    columns = (cells + rows - 1)/rows;
		break;

	    case 0:
		if (columns == 0) {
		    if (rows > 0)
			columns = (cells + rows - 1)/rows;
		    else columns = (orientation == YOIX_HORIZONTAL) ? cells : 1;
		} else if (orientation == YOIX_VERTICAL) {
		    if (rows > 0 && rows*columns < cells)
			columns = (cells + rows - 1)/rows;
		}
		break;

	    case 1:
		if (columns == 0) {
		    if (rows > 0)
			columns = (cells + rows - 1)/rows;
		    else columns = (orientation == YOIX_HORIZONTAL) ? cells : 1;
		}
		break;
	}

	return(Math.max(columns, 1));
    }


    private Component[]
    getComponents(Container parent) {

	Component  components[];
	Component  temp[];
	int        count;
	int        n;

	if ((components = parent.getComponents()) != null) {
	    if (components.length > 0) {
		if (useall == false) {
		    for (n = 0, count = 0; n < components.length; n++) {
			if (components[n].isVisible())
			    components[count++] = components[n];
		    }
		    temp = new Component[count];
		    System.arraycopy(components, 0, temp, 0, count);
		    components = temp;
		}
	    }
	}

	return((components != null && components.length > 0) ? components : null);
    }


    private Dimension
    getLayoutSize(Container parent, int type) {

	Component  components[];
	Dimension  size;
	Insets     insets;
	int        cells;
	int        rows;
	int        columns;
	int        width;
	int        height;
	int        n;

	synchronized(parent.getTreeLock()){
	    insets = parent.getInsets();
	    if ((components = getComponents(parent)) != null) {
		cells = components.length;
		rows = getRowCount(cells);
		columns = getColumnCount(cells);
		width = 0;
		height = 0;
		for (n = 0; n < components.length; n++) {
		    switch (type) {
			case V_MAXIMUMSIZE:
			    size = components[n].getMaximumSize();
			    break;

			case V_MINIMUMSIZE:
			    size = components[n].getMinimumSize();
			    break;

			case V_PREFERREDSIZE:
			default:
			    size = components[n].getPreferredSize();
			    break;
		    }
		    if (size.width > width)
			width = size.width;
		    if (size.height > height)
			height = size.height;
		}
		size = new Dimension(
		    columns*width + (columns - 1)*getHgap(),
		    rows*height + (rows - 1)*getVgap()
		);
	    } else size = new Dimension(0, 0);

	    size.width += insets.left + insets.right;
	    size.height += insets.top + insets.bottom;
	}

	return(size);
    }


    private int
    getRowCount(int cells) {

	int  columns;
	int  rows;

	columns = getColumns();
	rows = getRows();

	switch (model) {
	    case -1:
		if (rows == 0) {
		    if (columns > 0)
			rows = (cells + columns - 1)/columns;
		    else rows = cells;
		}
		break;

	    case 0:
		if (rows == 0) {
		    if (columns > 0)
			rows = (cells + columns - 1)/columns;
		    else rows = (orientation == YOIX_HORIZONTAL) ? 1 : cells;
		} else if (orientation == YOIX_HORIZONTAL) {
		    if (columns > 0  && rows*columns < cells)
			rows = (cells + columns - 1)/columns;
		}
		break;

	    case 1:
		if (rows == 0) {
		    if (columns > 0)
			rows = (cells + columns - 1)/columns;
		    else rows = (orientation == YOIX_HORIZONTAL) ? 1 : cells;
		}
		break;
	}

	return(Math.max(rows, 1));
    }
}

