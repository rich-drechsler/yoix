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
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

class YoixSwingTableColumn extends TableColumn

    implements YoixConstantsJTable,
               YoixConstantsSwing

{

    private boolean   lowered = false;
    private Color     headerbackgrounds[] = null;
    private Color     headerforegrounds[] = null;
    private Icon      headericons[] = null;
    private String    headertip = null;

    private Font      headerFont = null;

    private Color     cellbackgrounds[] = null;
    private Color     cellforegrounds[] = null;
    private Color     cellselectionforegrounds[] = null;
    private Color     cellselectionbackgrounds[] = null;
    private Object    editinfo = null;

    private Color     background = null;
    private Color     foreground = null;
    private Color     disabledbackground = null;
    private Color     disabledforeground = null;
    private Color     selectionbackground = null;
    private Color     selectionforeground = null;

    private Color     editbackground = null;
    private Color     editforeground = null;

    private boolean   visible = true;
    private boolean   adjusting = false;
    private int       width_requested = -1;
    private int       minwidth_requested = -1;
    private int       maxwidth_requested = -1;

    private Object    format = null;

    private Font      font = null;
    private Border    border = null;

    private String    tiptext[] = null;
    private String    tag;

    private int       alignment = -1;
    int               halignment = -1;

    private int       header_alignment = -1;
    int               header_halignment = -1;

    private YoixObject  editor = null;
    private YoixObject  etc = null;	// spare value, used by YOIX_HISTOGRAM_TYPE

    private YoixObject  picksortobject = null;
    private YoixObject  picktableobject = null;

    private HashMap     htmap = null;
    private int         type = YOIX_STRING_TYPE;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    YoixSwingTableColumn(int modelIndex, int type) {

	super(modelIndex);
	this.tag = "_" + modelIndex; // initial value

	// just default to string type if no match (might change someday)
	for (int n = 0; n < YoixSwingJTable.typeValues.length; n++) {
	    if (YoixSwingJTable.typeValues[n] == type) {
		this.type = type;
		break;
	    }
	}
    }

    ///////////////////////////////////
    //
    // YoixSwingTableColumn Methods
    //
    ///////////////////////////////////

    public final int
    getAlignment(int dflt) {

	return(alignment < 0 ? dflt : alignment);
    }


    public final Border
    getBorder(Border dflt) {

	Border  snapshot = border;

	return(snapshot == null ? dflt : snapshot);
    }


    public final Color[]
    getCellBackgrounds(Color dflt[]) {

	Color  snapshot[] = cellbackgrounds;

	if (snapshot == null && background != null)
	    snapshot = new Color[] { background };

	return(snapshot == null ? dflt : snapshot);
    }


    public final Color[]
    getCellForegrounds(Color dflt[]) {

	Color  snapshot[] = cellforegrounds;

	if (snapshot == null && foreground != null)
	    snapshot = new Color[] { foreground };

	return(snapshot == null ? dflt : snapshot);
    }


    final Color[]
    getCellSelectionBackgrounds() {

	return(cellselectionbackgrounds);
    }


    final Color[]
    getCellSelectionForegrounds() {

	return(cellselectionforegrounds);
    }


    final Color
    getBackground(Color dflt) {

	return(background == null ? dflt : background);
    }


    final Color
    getForeground(Color dflt) {

	return(foreground == null ? dflt : foreground);
    }


    final Color
    getEditBackground(Color dflt) {

	return(editbackground == null ? dflt : editbackground);
    }


    final Color
    getEditForeground(Color dflt) {

	return(editforeground == null ? dflt : editforeground);
    }

    final Object
    getFormat(Object dflt) {
	return(format == null ? dflt : format);
    }


    final Color
    getDisabledBackground(Color dflt) {

	return(disabledbackground == null ? dflt : disabledbackground);
    }


    final Color
    getDisabledForeground(Color dflt) {

	return(disabledforeground == null ? dflt : disabledforeground);
    }


    final YoixObject
    getEditor() {

	return(editor);
    }


    final YoixObject
    getEtc() {

	return(etc);
    }


    public final Font
    getFont(Font dflt) {

	Font  snapshot = font;

	return(snapshot == null ? dflt : snapshot);
    }


    public final int
    getHeaderAlignment(int dflt) {

	return(header_alignment < 0 ? dflt : header_alignment);
    }


    public final Color
    getHeaderBackground(int state, Color dflt) {

	Color  snapshot[] = headerbackgrounds;
	Color  bkgd;

	if (snapshot == null || snapshot.length == 0)
	    bkgd = dflt;
	else if (snapshot.length < 3)
	    bkgd = snapshot[0];
	else if (state < 0)
	    bkgd = snapshot[1];
	else if (state > 0)
	    bkgd = snapshot[2];
	else bkgd = snapshot[0];

	return(bkgd);
    }


    public final Color[]
    getHeaderBackgrounds(Color[] dflt) {

	return(headerbackgrounds == null ? dflt : headerbackgrounds);
    }


    public final Font
    getHeaderFont(Font dflt) {

	Font  snapshot = headerFont;

	return(snapshot == null ? dflt : snapshot);
    }


    public final Color
    getHeaderForeground(int state, Color dflt) {

	Color  snapshot[] = headerforegrounds;
	Color  frgd;

	    if (snapshot == null || snapshot.length == 0)
		frgd = dflt;
	    else if (snapshot.length < 3)
		frgd = snapshot[0];
	    else if (state < 0)
		frgd = snapshot[1];
	    else if (state > 0)
		frgd = snapshot[2];
	    else frgd = snapshot[0];

	    return(frgd);
    }

    public final Color[]
    getHeaderForegrounds(Color[] dflt) {

	return(headerforegrounds == null ? dflt : headerforegrounds);
    }


    public final int
    getHeaderHorizontalAlignment(int dflt) {

	return(header_halignment < 0 ? dflt : header_halignment);
    }


    public final Icon
    getHeaderIcon(int state, Icon dflt) {

	Icon   snapshot[] = headericons;
	Icon   hicon;

	    if (snapshot == null || snapshot.length == 0)
		hicon = dflt;
	    else if (snapshot.length < 3)
		hicon = snapshot[0];
	    else if (state < 0)
		hicon = snapshot[1];
	    else if (state > 0)
		hicon = snapshot[2];
	    else hicon = snapshot[0];

	    return(hicon);
    }

    public final Icon[]
    getHeaderIcons() {

	return(headericons);
    }


    public final String
    getHeaderTip(String dflt) {

	return(headertip == null ? dflt : headertip);
    }


    public final int
    getHorizontalAlignment(int dflt) {

	return(halignment < 0 ? dflt : halignment);
    }


    public final boolean
    getLowered() {

	return(lowered);
    }


    public final YoixObject
    getPickSortObject() {

	return(picksortobject);
    }


    public final YoixObject
    getPickTableObject() {

	return(picktableobject);
    }


    public final Boolean
    getRowEditableBoolean(int row) {

	Boolean  rowedit = null;
	Boolean  array[];
	Object   snapshot = editinfo;

	if (snapshot != null) {
	    if (snapshot instanceof Boolean)
		rowedit = ((Boolean)snapshot);
	    else {
		array = (Boolean[])snapshot;
		if (row < array.length && array[row] != null)
		    rowedit = array[row];
	    }
	}
	return(rowedit);
    }


    public final int
    getRowHeightInfo(int row) {

	return(getRowHeightInfo(new Integer(row)));
    }


    public final int
    getRowHeightInfo(Integer row) {

	return((htmap == null || (row = ((Integer)(htmap.get(row)))) == null) ? 0 : row.intValue());
    }


    public final Color
    getSelectionBackground(Color dflt) {

	Color  snapshot = selectionbackground;

	return(snapshot == null ? dflt : snapshot);
    }


    public final Color
    getSelectionForeground(Color dflt) {

	Color  snapshot = selectionforeground;

	return(snapshot == null ? dflt : snapshot);
    }


    public final String
    getTag() {

	return(tag);
    }


    public final String
    getTipText(int row, int column, String dflt) {

	String  snapshot[] = tiptext;
	String  text;
	int     len;

	text = dflt;

	if (snapshot != null && (len = snapshot.length) > 0) {
	    if (len == 1)
		text = snapshot[0];
	    else if (row >= 0 && row < len && snapshot[row] != null)
		text = snapshot[row];
	}
	return(text);
    }


    public final String[]
    getToolTips() {

	return(tiptext);
    }


    public final int
    getType() {

	return(type);
    }


    public final boolean
    isVisible() {

	return(visible);
    }


    public final void
    setAlignments(int align, int halign) {

	alignment = align;
	halignment = halign;
    }


    public final void
    setBorder(Border value) {

	border = value;
    }


    public final void
    setCellBackgrounds(Color values[]) {

	cellbackgrounds = values;
    }


    public final void
    setCellForegrounds(Color values[]) {

	cellforegrounds = values;
    }


    final void
    setCellSelectionBackgrounds(Color values[]) {

	cellselectionbackgrounds = values;
    }


    final void
    setCellSelectionForegrounds(Color values[]) {

	cellselectionforegrounds = values;
    }


    final void
    setDisabledBackground(Color value) {

	disabledbackground = value;
    }


    final void
    setDisabledForeground(Color value) {

	disabledforeground = value;
    }


    public final void
    setEditInfo(Object values) {

	editinfo = values;
    }


    final void
    setEditor(YoixSwingJTable jt, YoixObject obj) {

	TableCellEditor  tce = null;

	//
	// Eventually try to do a better job consolidating the combobx
	// and textfield editing code - confusing and error prone as it
	// currently stands (it's definitely my fault).
	//

	if (obj != null && obj.notNull()) {
	    if (obj.isJTextField())
		tce = jt.createCellEditor((YoixSwingJTextField)obj.getManagedObject());
	} else obj = null;

	setCellEditor(tce);
	editor = obj;
    }


    final void
    setEditor(YoixSwingJTable jt, YoixObject obj, int type) {

	if (obj != null && obj.notNull()) {
	    setCellEditor(jt.createComboBoxEditor(type, ((YoixSwingJComboBox)(obj.getManagedObject()))));
	    editor = obj;
	} else {
	    setCellEditor(null);
	    editor = null;
	}
    }


    final void
    setEditBackground(Color clr) {

	editbackground = clr;
    }


    final void
    setEditForeground(Color clr) {

	editforeground = clr;
    }


    public final void
    setEtc(YoixObject value) {

	etc = value;
    }


    public final void
    setFont(Font value) {

	font = value;
    }


    public final void
    setHeaderAlignments(int align, int halign) {

	header_alignment = align;
	header_halignment = halign;
    }


    public final void
    setHeaderBackgrounds(Color values[]) {

	headerbackgrounds = values;
    }


    public final void
    setHeaderFont(Font value) {

	headerFont = value;
    }


    public final void
    setHeaderForegrounds(Color values[]) {

	headerforegrounds = values;
    }


    public final void
    setHeaderIcons(Icon icons[]) {

	headericons = icons;
    }


    public final void
    setHeaderTip(String tip) {

	headertip = tip;
    }


    public final void
    setLowered(boolean value) {

	lowered = value;
    }


    public final void
    setBackground(Color value) {

	background = value;
    }


    public final void
    setForeground(Color value) {

	foreground = value;
    }


    public final void
    setFormat(Object value) {

	format = value;
    }


    public synchronized void
    setMaxWidth(int value) {

	super.setMaxWidth(adjusting ? value : (maxwidth_requested = value));
    }


    public synchronized void
    setMinWidth(int value) {

	super.setMinWidth(adjusting ? value : (minwidth_requested = value));
    }


    public final void
    setPickSortObject(YoixObject obj) {

	picksortobject = (obj != null && obj.callable(1)) ? obj : null;
    }


    public final void
    setPickTableObject(YoixObject obj) {

	picktableobject = (obj != null && obj.callable(1)) ? obj : null;
    }


    public synchronized void
    setPreferredWidth(int value) {

	super.setPreferredWidth(adjusting ? value : (width_requested = value));
    }

    public final void
    setRowHeightInfo(int row, int ht) {

	setRowHeightInfo(new Integer(row), new Integer(ht));
    }

    public final void
    setRowHeightInfo(Integer row, Integer ht) {

	if (htmap == null)
	    htmap = new HashMap();

	htmap.put(row, ht);
    }


    public final void
    setSelectionBackground(Color value) {

	selectionbackground = value;
    }


    public final void
    setSelectionForeground(Color value) {

	selectionforeground = value;
    }


    public final void
    setTag(String tag) {

	this.tag = tag;
    }


    public final void
    setTipText(String tips[]) {

	tiptext = tips;
    }


    public final void
    setType(int type) {

	this.type = type;
    }


    public synchronized void
    setVisible(boolean value) {
	if (value != visible) {
	    visible = value;
	    adjusting = true;
	    if (visible) {
		if (minwidth_requested >= 0)
		    super.setMinWidth(minwidth_requested);
		else super.setMinWidth(15);                // Java default
		if (maxwidth_requested >= 0)
		    super.setMaxWidth(maxwidth_requested);
		else super.setMaxWidth(Integer.MAX_VALUE); // Java default
		if (width_requested >= 0)
		    super.setPreferredWidth(width_requested);
		else super.setPreferredWidth(75);          // Java default
	    } else {
		if (minwidth_requested < 0)
		    minwidth_requested = getMinWidth();
		if (maxwidth_requested < 0)
		    maxwidth_requested = getMaxWidth();
		if (width_requested < 0)
		    width_requested = getPreferredWidth();
		super.setMinWidth(0);
		super.setMaxWidth(0);
	    }
	    adjusting = false;
	}
    }


    final YoixObject
    yoixEditInfo() {

	YoixObject  result = null;
	Object      snapshot = editinfo;

	if (snapshot != null) {
	    if (snapshot instanceof Boolean)
		result = YoixObject.newInt(((Boolean)snapshot).booleanValue());
	    else result = YoixSwingJTable.yoixBooleanArray((Boolean[])snapshot);
	}
	return(result);
    }
}
