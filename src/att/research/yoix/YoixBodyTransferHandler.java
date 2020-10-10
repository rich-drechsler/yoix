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
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

final
class YoixBodyTransferHandler extends YoixPointerActive

{

    //
    // This is an unusual extension of the YoixPointerActive class that's
    // only needed as a way to provide access two methods (exportAsDrag()
    // and exportToClipboard()) that are defined in Java's TransferHandler
    // class. The other functions are callbacks that Yoix scripts can add
    // to TransferHandlers. These callback functions are invoked through
    // a YoixSwingTransferHandler that's created in YoixBodyComponentSwing
    // (see setTransferHandler()). When that YoixSwingTransferHandler is
    // created it automatically assigns itself to a JComponent, which means
    // it handles the Java callbacks) and it saves a reference to our data
    // dictionary so it can forward those callbacks to Yoix functions and
    // send the appropriate answers back to Java.
    //
    // This class is also used to represent default TransferHandlers that
    // Java associates with many Swing components, which for the most part
    // are all you'll ever deal with. In that mode the callback functions
    // are implemented in special Java classes and we decided to make them
    // completely inaccessible from Yoix scripts. In other words, if this
    // class represents one of Java's default TransferHandlers, the only
    // functions you can access are exportAsDrag() and exportToClipboard().
    // There's a small chance we may add code that will let you call the
    // callback functions associated with a default TransferHandler, but
    // it's not a high priority and probably won't add anything useful.
    //
    // One final thing to notice is that this class doesn't function as
    // a "manager" for some other Java object. As we've already mentioned
    // custom applications will sometimes have to use exportAsDrag() and
    // exportToClipboard() but the fact that they can get the handler from
    // their JComponent argument means we could have defined these builtins
    // somewhere else (e.g., YoixModuleJFC). We decided on this approach,
    // for now anyway, because it means Yoix scripts just have to look in
    // one place for all the TransferHandler support.
    //

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
    };

    //
    // Permissions when we're supposed to represent a Java TransferHandler
    // that was created somewhere else. These are the callback functions
    // that are currently inaccessible when we represent one of Java's
    // default TransferHandlers.
    //

    private static final Object  permissions2[] = {
     //
     // FIELD                      OBJECT       BODY
     // -----                      ------       ----
	N_CANIMPORT,               $LR__,       null,
	N_CREATETRANSFERABLE,      $LR__,       null,
	N_EXPORTDONE,              $LR__,       null,
	N_GETSOURCEACTIONS,        $LR__,       null,
	N_GETVISUALREPRESENTATION, $LR__,       null,
	N_IMPORTDATA,              $LR__,       null,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(5);

    static {
	activefields.put(N_EXPORTASDRAG, new Integer(V_EXPORTASDRAG));
	activefields.put(N_EXPORTTOCLIPBOARD, new Integer(V_EXPORTTOCLIPBOARD));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyTransferHandler(YoixObject data) {

	super(data);
	setFixedSize();
	setPermissions(permissions);
    }


    YoixBodyTransferHandler(YoixObject data, TransferHandler handler) {

	super(data);
	setFixedSize();
	setPermissions(permissions2);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(TRANSFERHANDLER);
    }

    ///////////////////////////////////
    //
    // YoixBodyTransferHandler Methods
    //
    ///////////////////////////////////

    protected final void
    finalize() {

	super.finalize();
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_EXPORTASDRAG:
		obj = builtinExportAsDrag(name, argv);
		break;

	    case V_EXPORTTOCLIPBOARD:
		obj = builtinExportToClipboard(name, argv);
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
	    default:
		break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(null);
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

    private synchronized YoixObject
    builtinExportAsDrag(String name, YoixObject arg[]) {

	YoixBodyComponent  body;
	TransferHandler    handler;
	AWTEvent           event;
	Object             comp;
	int                action;

	if (arg.length == 3) {
	    if (arg[0].isJComponent()) {
		if ((event = YoixMakeEvent.javaAWTEvent(arg[1], arg[0], false)) != null) {
		    if (event instanceof MouseEvent) {
			if (arg[2].isInteger()) {
			    comp = arg[0].getManagedObject();
			    if (comp instanceof JComponent) {
				if ((handler = ((JComponent)comp).getTransferHandler()) != null) {
				    body = (YoixBodyComponent)arg[0].body();
				    action = YoixBodyComponent.jfcInt("TransferHandler", arg[2].intValue());
				    handler.exportAsDrag(
					(JComponent)comp,
					body.pickTransferTrigger((MouseEvent)event),
					pickExportAction((JComponent)comp, handler, action)
				    );
				}
			    }
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private synchronized YoixObject
    builtinExportToClipboard(String name, YoixObject arg[]) {

	TransferHandler  handler;
	AWTEvent         event;
	Object           comp;
	int              action;

	if (arg.length == 3) {
	    if (arg[0].isJComponent()) {
		if (arg[1].isClipboard()) {
		    if (arg[2].isInteger()) {
			comp = arg[0].getManagedObject();
			if (comp instanceof JComponent) {
			    if ((handler = ((JComponent)comp).getTransferHandler()) != null) {
				action = YoixBodyComponent.jfcInt("TransferHandler", arg[2].intValue());
				handler.exportToClipboard(
				    (JComponent)comp,
				    (Clipboard)arg[1].getManagedObject(),
				    pickExportAction((JComponent)comp, handler, action)
				);
			    }
			}
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newEmpty());
    }


    private int
    pickExportAction(JComponent comp, TransferHandler handler, int action) {

	Object  result;

	//
	// Just used to try and make TransferHandler.exportAsDrag() happy
	// if the corresponding Yoix builtin was called with COPY_OR_MOVE
	// as the action argument.
	//

	if (handler != null) {
	    switch (action) {
		case DnDConstants.ACTION_COPY:
		case DnDConstants.ACTION_MOVE:
		case DnDConstants.ACTION_LINK:
		case DnDConstants.ACTION_NONE:
		    break;

		case DnDConstants.ACTION_COPY_OR_MOVE:
		    action &= handler.getSourceActions(comp);
		    if (action == DnDConstants.ACTION_COPY_OR_MOVE)
			action = DnDConstants.ACTION_MOVE;
		    break;
	    }
	}

	return(action);
    }
}

