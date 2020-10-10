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
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;

final
class YoixBodyClipboard extends YoixPointerActive

{

    private Clipboard  clipboard;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	N_NAME,             $LR__,       null,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(5);

    static {
	activefields.put(N_CONTENTS, new Integer(V_CONTENTS));
	activefields.put(N_NAME, new Integer(V_NAME));
	activefields.put(N_OWNER, new Integer(V_OWNER));
	activefields.put(N_SETCONTENTS, new Integer(V_SETCONTENTS));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyClipboard(YoixObject data) {

	super(data);
	buildClipboard();
	setFixedSize();
	setPermissions(permissions);
    }


    YoixBodyClipboard(YoixObject data, Clipboard clipboard) {

	super(data);
	this.clipboard = clipboard;
	setFixedSize();
	setPermissions(permissions);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(CLIPBOARD);
    }

    ///////////////////////////////////
    //
    // YoixBodyClipboard Methods
    //
    ///////////////////////////////////

    protected final void
    finalize() {

	VM.removeClipboard(clipboard);
	clipboard = null;
	super.finalize();
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_SETCONTENTS:
		obj = builtinSetContents(name, argv);
		break;

	    default:
		obj = null;
		break;
	}
	return(obj);
    }



    protected final synchronized YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_CONTENTS:
		obj = getContents();
		break;

	    case V_NAME:
		obj = getName();
		break;

	    case V_OWNER:
		obj = getOwner();
		break;
	}
	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(clipboard);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		default:
		    break;
	    }
	}
	return(obj);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildClipboard() {

	clipboard = new Clipboard(data.getString(N_NAME));
    }


    private synchronized YoixObject
    builtinSetContents(String name, YoixObject arg[]) {

	boolean  result = false;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1 || arg[1].compound() || arg[1].isNull()) {
		result = VM.setClipboardContents(
		    clipboard,
		    arg[0],
		    arg.length == 2 ? arg[1] : null
		);
	    } else VM.badArgument(name, 1);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    getContents() {

	YoixObject  obj = null;

	if (clipboard != null) {
	    try {
		// in the absence of other information, we need to prefer text over
		// other transferable flavors as Microsoft Word places some kind of
		// image flavor mixed in with a straight text snarf!
		obj = YoixDataTransfer.yoixTransferable(clipboard.getContents(this), true);
	    }
	    catch(IllegalStateException e) {}
	}
	return(obj != null ? obj : YoixObject.newNull());
    }


    private synchronized YoixObject
    getName() {

	String  name = null;

	if (clipboard != null) {
	    try {
		if ((name = clipboard.getName()) != null) {
		    if (name.length() == 0)
			name = null;
		}
	    }
	    catch(IllegalStateException e) {}
	}
	return(YoixObject.newString(name));
    }


    private synchronized YoixObject
    getOwner() {

	return(VM.getClipboardOwner(clipboard));
    }
}

