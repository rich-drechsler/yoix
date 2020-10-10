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
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

class YoixSwingJFileDialog extends YoixSwingJDialog

    implements ActionListener,
	       WindowListener,
	       YoixConstants,
	       YoixInterfaceFileChooser

{

    private YoixBodyComponent  parent;
    private YoixObject         data;

    //
    // We make our own file chooser.
    //

    private YoixSwingJFileChooser  filechooser;
    private YoixObject             chooser;

    //
    // String constants that let us figure out what the user did.
    //

    private static final String  APPROVE_SELECTION = JFileChooser.APPROVE_SELECTION;
    private static final String  CANCEL_SELECTION = JFileChooser.CANCEL_SELECTION;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJFileDialog(YoixObject data, YoixBodyComponent parent, Dialog owner, GraphicsConfiguration gc) {

	super(data, parent, owner, gc);
	buildDialog(data, parent);
    }


    YoixSwingJFileDialog(YoixObject data, YoixBodyComponent parent, Frame owner, GraphicsConfiguration gc) {

	super(data, parent, owner, gc);
	buildDialog(data, parent);
    }

    ///////////////////////////////////
    //
    // ActionListener Methods
    //
    ///////////////////////////////////

    public final void
    actionPerformed(ActionEvent e) {

	//
	// When autodispose is true we need to make sure the file field in
	// data is properly set because when we hide the dialog it will be
	// disposed. What that means is the peer defined in parent will be
	// set to null and in that case getFile() method defined in parent
	// will return the value that's defined in data. Done on 9/19/11 in
	// response to a problem that was reported by a user.
	//

	if (data.getBoolean(N_AUTODISPOSE, false)) {
	    if (CANCEL_SELECTION.equals(e.getActionCommand()))
		data.putObject(N_FILE, YoixObject.newString());
	    else data.putObject(N_FILE, parent.getField(N_FILE, YoixObject.newString()));
	}

	parent.setField(N_VISIBLE, YoixObject.newInt(false));
    }

    ///////////////////////////////////
    //
    // WindowListener Methods
    //
    ///////////////////////////////////

    public final void
    windowActivated(WindowEvent e) {

    }


    public final void
    windowClosed(WindowEvent e) {

    }


    public final void
    windowClosing(WindowEvent e) {

	if (data.writable(N_FILE))
	    data.put(N_FILE, YoixObject.newNull(), false);
	setSelectedFile(null);
    }


    public final void
    windowDeactivated(WindowEvent e) {

    }


    public final void
    windowDeiconified(WindowEvent e) {

    }


    public final void
    windowIconified(WindowEvent e) {

    }


    public final void
    windowOpened(WindowEvent e) {

    }

    ///////////////////////////////////
    //
    // YoixInterfaceFileChooser Methods
    //
    ///////////////////////////////////

    public final void
    addChoosableFileFilter(FileFilter filter) {

	filechooser.addChoosableFileFilter(filter);
    }


    public final int
    getApproveButtonMnemonic() {

	return(filechooser.getApproveButtonMnemonic());
    }


    public final String
    getApproveButtonText() {

	return(filechooser.getApproveButtonText());
    }


    public final String
    getApproveButtonToolTipText() {

	return(filechooser.getApproveButtonToolTipText());
    }


    public final FileFilter[]
    getChoosableFileFilters() {

	return(filechooser.getChoosableFileFilters());
    }


    public final File
    getCurrentDirectory() {

	return(filechooser.getCurrentDirectory());
    }


    public final FileFilter
    getFileFilter() {

	return(filechooser.getFileFilter());
    }


    public final File
    getSelectedFile() {

	return(filechooser.getSelectedFile());
    }


    public final File[]
    getSelectedFiles() {

	return(filechooser.getSelectedFiles());
    }


    public final boolean
    isDirectorySelectionEnabled() {

	return(filechooser.isDirectorySelectionEnabled());
    }


    public final boolean
    isFileHidingEnabled() {

	return(filechooser.isFileHidingEnabled());
    }


    public final boolean
    isFileSelectionEnabled() {

	return(filechooser.isFileSelectionEnabled());
    }


    public final boolean
    isMultiSelectionEnabled() {

	return(filechooser.isMultiSelectionEnabled());
    }


    public final void
    resetChoosableFileFilters() {

	filechooser.resetChoosableFileFilters();
    }


    public final void
    setAcceptAllFileFilterUsed(boolean state) {

	filechooser.setAcceptAllFileFilterUsed(state);
    }


    public final void
    setApproveButtonMnemonic(int value) {

	filechooser.setApproveButtonMnemonic(value);
    }


    public final void
    setApproveButtonText(String text) {

	filechooser.setApproveButtonText(text);
    }


    public final void
    setApproveButtonToolTipText(String text) {

	filechooser.setApproveButtonToolTipText(text);
    }


    public final void
    setCurrentDirectory(File dir) {

	filechooser.setCurrentDirectory(dir);
    }


    public final void
    setDialogType(int type) {

	filechooser.setDialogType(type);
    }


    public final void
    setFileSelectionMode(int mode) {

	filechooser.setFileSelectionMode(mode);
    }


    public final void
    setMultiSelectionEnabled(boolean mode) {

	filechooser.setMultiSelectionEnabled(mode);
    }


    public final void
    setFileFilter(FileFilter filter) {

	filechooser.setFileFilter(filter);
    }


    public final void
    setFileHidingEnabled(boolean mode) {

	filechooser.setFileHidingEnabled(mode);
    }


    public final void
    setSelectedFile(File file) {

	filechooser.setSelectedFile(file);
    }


    public final void
    setSelectedFiles(File files[]) {

	filechooser.setSelectedFiles(files);
    }

    ///////////////////////////////////
    //
    // YoixSwingJFileDialog Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	data = null;
	parent = null;
	chooser = null;
	filechooser = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    final YoixObject
    getFileChooser() {

	return(chooser);
    }


    public final void
    show() {

	YoixObject  obj;
	Window      owner;

	//
	// Tries to center over the owner, which should have been set from
	// N_PARENT, if it's showing and the N_LOCATION field is NULL.
	// 

	if ((owner = getOwner()) != null) {
	    if (owner.isShowing()) {
		if ((obj = data.getObject(N_LOCATION)) == null || obj.isNull())
		    setLocationRelativeTo(owner);
	    }
	}
	super.show();
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildDialog(YoixObject data, YoixBodyComponent parent) {

	YoixObject  ival;

	//
	// Using ival to pass N_OPAQUEFLAGS from our data dictionary to
	// to the filechooser is a quick fix that's good enough for now,
	// but we eventually may want a better solution. Implementation
	// means setting opaqueflags field in a JFileDialog only works
	// in declarations. Really not important because opaqueflags is
	// currently undocumented and may remain that way.
	//

	this.parent = parent;
	this.data = data;

	ival = YoixObject.newDictionary(1);
	ival.putInt(N_OPAQUEFLAGS, data.getInt(N_OPAQUEFLAGS, 0));
	chooser = YoixMake.yoixType(T_JFILECHOOSER, ival);
	filechooser = (YoixSwingJFileChooser)chooser.getManagedObject();
	filechooser.addActionListener(this);
	addWindowListener(this);
	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }
}

