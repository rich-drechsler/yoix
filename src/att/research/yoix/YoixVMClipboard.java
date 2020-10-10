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

public abstract
class YoixVMClipboard extends YoixVMError

    implements ClipboardOwner,
	       YoixConstants

{

    //
    // Yoix representations of all clipboards are managed by this class,
    // which should be the only one that implements the ClipboardOwner
    // interface.
    //
    // NOTE - we considered adding the Yoix representation of the system
    // clipboard to the VM dictionary, but decided against (at least for
    // now). We were mostly concerned about performance (dumping the VM
    // dictionary should be a quick operation), but there was also the
    // chance that we could raise documented or undocumented exceptions
    // that could be platform dependent. Anyway, it eventually might be
    // nice if scripts could use VM.clipboard but there's no good reason
    // to rush it in without thorough testing.
    //

    private static YoixObject  systemclipboard = null;
    private static Hashtable   clipboards = new Hashtable();

    ///////////////////////////////////
    //
    // ClipboardOwner Methods
    //
    ///////////////////////////////////

    public synchronized void
    lostOwnership(Clipboard clipboard, Transferable contents) {

	DataFlavor  flavors[];
	YoixObject  funct;
	YoixObject  argv[];
	YoixObject  owner;
	Runnable    event;
	Object      value;
	int         n;

	//
	// We have never seen a YoixObject hit in the flavor loop so we
	// could be doing something wrong or maybe we don't understand
	// the contents argument. Doesn't seem to cause problems, so we
	// may investigate later.
	//

	if (clipboard != null) {
	    if ((value = clipboards.get(clipboard)) != null) {
		clipboards.remove(clipboard);
		if (value instanceof YoixObject) {
		    owner = (YoixObject)value;
		    if ((funct = owner.getObject(N_LOSTCLIPBOARDOWNERSHIP)) != null) {
			if (funct.callable(2)) {
			    argv = new YoixObject[] {YoixObject.newClipboard(clipboard), null};
			    if (contents != null) {
				if ((flavors = contents.getTransferDataFlavors()) != null) {
				    for (n = 0; n < flavors.length; n++) {
					if (flavors[n] != null) {
					    try {
						value = contents.getTransferData(flavors[n]);
						if (value instanceof YoixObject) {
						    argv[1] = (YoixObject)value;
						    break;
						}
					    }
					    catch(UnsupportedFlavorException e) {}
					    catch(IOException e) {}
					}
				    }
				}
			    }
			    if (argv[1] == null)
				argv[1] = YoixObject.newNull();
			} else if (funct.callable(1))
			    argv = new YoixObject[] {YoixObject.newClipboard(clipboard)};
			else if (funct.callable(0))
			    argv = new YoixObject[0];
			else argv = null;
			if (argv != null) {
			    event = new YoixAWTInvocationEvent(
				funct,
				argv,
				owner.compound() ? owner : null
			    );
			    EventQueue.invokeLater(event);
			}
		    }
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // YoixVMClipboard Methods
    //
    ///////////////////////////////////

    final synchronized YoixObject
    getClipboardOwner(Clipboard clipboard) {

	Object  currentowner = null;

	if (clipboard != null)
	    currentowner = clipboards.get(clipboard);
	return(currentowner instanceof YoixObject ? (YoixObject)currentowner : YoixObject.newNull());
    }


    final synchronized YoixObject
    getSystemClipboard() {

	if (systemclipboard == null)
	    systemclipboard = YoixObject.newClipboard(YoixAWTToolkit.getSystemClipboard());
	return(systemclipboard);
    }


    final synchronized void
    removeClipboard(Clipboard clipboard) {

	if (clipboard != null) {
	    try {
		clipboards.remove(clipboard);
		clipboard.setContents(YoixObject.newNull(), null);
	    }
	    catch(IllegalStateException e) {}
	}
    }


    final synchronized void
    removeClipboard(Clipboard clipboard, YoixObject owner) {

	Object  currentowner;

	if (clipboard != null) {
	    if (owner != null && owner.compound()) {
		currentowner = clipboards.get(clipboard);
		if (currentowner instanceof YoixObject) {
		    if (owner.bodyEquals((YoixObject)currentowner))
			removeClipboard(clipboard);
		}
	    }
	}
    }

    final synchronized void
    removeClipboards(YoixObject owner) {

	Enumeration  enm;
	Clipboard    clipboard;
	Object       currentowner;

	if (owner != null && owner.compound()) {
	    for (enm = clipboards.keys(); enm.hasMoreElements(); ) {
		if ((clipboard = (Clipboard)enm.nextElement()) != null) {
		    currentowner = clipboards.get(clipboard);
		    if (currentowner instanceof YoixObject) {
			if (owner.bodyEquals((YoixObject)currentowner))
			    removeClipboard(clipboard);
		    }
		}
	    }
	}
    }


    final synchronized boolean
    setClipboardContents(Clipboard clipboard, YoixObject contents, YoixObject owner) {

	boolean  result = false;
	Object   currentowner;

	//
	// Harder than you might expect because we're only a proxy owner
	// for YoixObjects that can define their own callback functions
	// that are supposed to be called when the ownership of clipboard
	// ownership changes. When required we trigger the callback by
	// calling our own lostOwnership() method, which should be safe
	// because it's synchronized and uses invokeLater() to actually
	// call the Yoix function.
	//
	// First implementation tried to force Java to make the call by
	// calling setContents() with a null owner field. That worked on
	// 1.4.X, but didn't behave well on 1.5.0.
	//

	if (clipboard != null) {
	    try {
		currentowner = clipboards.get(clipboard);
		if (currentowner instanceof YoixObject) {
		    if (((YoixObject)currentowner).bodyEquals(owner) == false)
			lostOwnership(clipboard, clipboard.getContents(this));
		}
		clipboard.setContents(contents, this);
		if (owner != null && owner.compound())
		    clipboards.put(clipboard, owner);
		else clipboards.remove(clipboard);
		result = true;
	    }
	    catch(IllegalStateException e) {}
	}
	return(result);
    }
}

