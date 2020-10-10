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
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.*;
import javax.swing.text.*;

class YoixSwingTransferHandler extends TransferHandler

    implements YoixConstants

{

    //
    // This class overrides TransferHandler callback methods and directs
    // the real work to Yoix functions or other Java code that interacts
    // with the Yoix representation of the JComponent that's the target
    // of the TransferHandler. The Yoix error handling code may be a bit
    // confusing, but it's required. It probably can be simplified some,
    // but omit and an abort will result in a STACKUNDERFLOW error that
    // will kill the application.
    //
    // NOTE - we probably use reflection in YoixBodyComponentSwing.java
    // to call the constructor.
    // 

    private YoixObject  data = null;
    private YoixObject  owner = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingTransferHandler(YoixObject data, YoixObject owner) {

	super();
	this.data = data;
	this.owner = owner;
    }

    ///////////////////////////////////
    //
    // YoixSwingTransferHandler Methods
    //
    ///////////////////////////////////

    public boolean
    canImport(JComponent comp, DataFlavor flavors[]) {

	YoixObject  argv[];
	YoixObject  funct;
	YoixObject  obj;
	YoixError   error_point = null;
	boolean     result = false;

	if (owner.getManagedObject() == comp) {
	    try {
		error_point = VM.pushError();
		if ((funct = data.getObject(N_CANIMPORT)) != null) {
		    if (funct.notNull()) {
			argv = new YoixObject[] {
			    owner,
			    YoixMake.yoixMimeTypes(flavors)
			};
			if ((obj = call(funct, argv, data)) != null) {
			    if (obj.isNumber())
				result = obj.booleanValue();
			}
		    } else result = canImport(owner, flavors);
		} else result = canImport(owner, flavors);
		VM.popError();
	    }
	    catch(YoixError e) {
		if (e != error_point)
		    throw(e);
		else VM.error(error_point);
	    }
	} else result = super.canImport(comp, flavors);
	return(result);
    }


    protected Transferable
    createTransferable(JComponent comp) {

	Transferable  transferable = null;
	YoixObject    funct;
	YoixObject    obj;
	YoixError     error_point = null;

	if (owner.getManagedObject() == comp) {
	    try {
		error_point = VM.pushError();
		if ((funct = data.getObject(N_CREATETRANSFERABLE)) != null) {
		    if (funct.notNull()) {
			obj = call(funct, new YoixObject[] {owner}, data);
			if (obj != null && obj.notNull())
			    transferable = obj;
			else transferable = null;
		    } else transferable = createTransferable(owner);
		} else transferable = createTransferable(owner);
		VM.popError();
	    }
	    catch(YoixError e) {
		if (e != error_point)
		    throw(e);
		else VM.error(error_point);
	    }
	} else transferable = super.createTransferable(comp);
	return(transferable);
    }


    public void
    exportDone(JComponent comp, Transferable t, int action) {

	YoixObject  argv[];
	YoixObject  funct;
	YoixError   error_point = null;

	if (owner.getManagedObject() == comp) {
	    try {
		error_point = VM.pushError();
		if ((funct = data.getObject(N_EXPORTDONE)) != null) {
		    if (funct.notNull()) {
			argv = new YoixObject[] {
			    owner,
			    YoixDataTransfer.yoixTransferable(t, comp instanceof JTextComponent),
			    YoixObject.newInt(getYoixAction(action))
			};
			call(funct, argv, data);
		    } else exportDone(owner, action);
		} else exportDone(owner, action);
		VM.popError();
	    }
	    catch(YoixError e) {
		if (e != error_point)
		    throw(e);
		else VM.error(error_point);
	    }
	} else super.exportDone(comp, t, action);
    }


    protected final void
    finalize() {

	data = null;
	owner = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    public int
    getSourceActions(JComponent comp) {

	YoixObject  argv[];
	YoixObject  funct;
	YoixObject  obj;
	YoixError   error_point = null;
	int         action = NONE;

	if (owner.getManagedObject() == comp) {
	    try {
		error_point = VM.pushError();
		action = getJavaAction(data.getInt(N_ACTION, YOIX_COPY));
		if ((funct = data.getObject(N_GETSOURCEACTIONS)) != null) {
		    if (funct.notNull()) {
			argv = new YoixObject[] {owner}; 
			if ((obj = call(funct, argv, data)) != null) {
			    if (obj.isNumber())
				action = getJavaAction(obj.intValue());
			}
		    }
		}
		VM.popError();
	    }
	    catch(YoixError e) {
		if (e != error_point)
		    throw(e);
		else VM.error(error_point);
	    }
	} else action = super.getSourceActions(comp);

	return(action);
    }


    public Icon
    getVisualRepresentation(Transferable t) {

	YoixObject  argv[];
	YoixObject  funct;
	YoixObject  obj;
	YoixError   error_point = null;
	Icon        icon = null;

	//
	// Haven't ever seen this called yet, but most of our testing has
	// been on Linux.
	//

	if ((funct = data.getObject(N_GETVISUALREPRESENTATION)) != null) {
	    try {
		error_point = VM.pushError();
		if (funct.notNull()) {
		    argv = new YoixObject[] {YoixDataTransfer.yoixTransferable(t, false)};
		    if ((obj = call(funct, argv, data)) != null)
			icon = YoixMake.javaIcon(obj);
		}
		VM.popError();
	    }
	    catch(YoixError e) {
		if (e != error_point)
		    throw(e);
		else VM.error(error_point);
	    }
	}
	return(icon);
    }


    public boolean
    importData(JComponent comp, Transferable t) {

	YoixObject  argv[];
	YoixObject  funct;
	YoixObject  obj;
	YoixError   error_point = null;
	boolean     result = false;

	if (owner.getManagedObject() == comp) {
	    try {
		error_point = VM.pushError();
		if ((funct = data.getObject(N_IMPORTDATA)) != null) {
		    if (funct.notNull()) {
			argv = new YoixObject[] {
			    owner,
			    YoixDataTransfer.yoixTransferable(t, comp instanceof JTextComponent)
			};
			if ((obj = call(funct, argv, data)) != null) {
			    if (obj.isNumber())
				result = obj.booleanValue();
			}
		    } else result = importData(owner, t);
		} else result = importData(owner, t);
		VM.popError();
	    }
	    catch(YoixError e) {
		if (e != error_point)
		    throw(e);
		else VM.error(error_point);
	    }
	} else result = super.importData(comp, t);

	return(result);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private YoixObject
    call(YoixObject funct, YoixObject argv[], YoixObject context) {

	YoixObject  obj = null;
	YoixError   interrupt_point = null;

	//
	// Came from YoixPointerActive.call(), but it's different because
	// we assume that the caller has done the required null checking
	// and pushed an error object.
	//

	try {
	    interrupt_point = VM.pushInterrupt();
	    obj = funct.call(argv, context).resolve();
	}
	catch(YoixError e) {
	    if (e != interrupt_point)
		throw(e);
	}
	catch(SecurityException e) {
	    VM.error(e);
	}
	return(obj);
    }


    private boolean
    canImport(YoixObject owner, DataFlavor flavors[]) {

	boolean  result = false;
	int      n;

	if (owner != null && flavors != null) {
	    for (n = 0; n < flavors.length; n++) {
		if (owner.isDataFlavorSupported(flavors[n])) {
		    result = true;
		    break;
		}
	    }
	}
	return(result);
    }


    private Transferable
    createTransferable(YoixObject owner) {

	YoixObject  obj = null;
	String      property;

	if ((property = data.getString(N_PROPERTY)) != null)
	    obj = owner.getObject(property);
	return(obj);
    }


    private void
    exportDone(YoixObject owner, int action) {

	String  property;

	if (action == MOVE) {
	    if ((property = data.getString(N_PROPERTY)) != null)
		owner.putObject(property, YoixObject.newNull());
	}
    }


    private int
    getJavaAction(int action) {

	//
	// Maps an integer that came from a Yoix script to a constant that's
	// an official TransferHandler action. We use the mappings provided
	// by YoixBodyComponent, which currently don't let us go in the other
	// direction.
	//

	return(YoixBodyComponent.jfcInt("TransferHandler", action));
    }


    private int
    getYoixAction(int action) {

	//
	// Unfortunately there'e currently no way to use YoixBodyComponent
	// methods to go in the other direction. We have one custom module
	// that provides mapping in both directions, but we haven't had the
	// time to include that support in YoixBodyComponent yet.
	//

	switch (action) {
	    case COPY:
		action = YOIX_COPY;
		break;

	    case COPY_OR_MOVE:
		action = YOIX_COPY_OR_MOVE;
		break;

	    case DnDConstants.ACTION_LINK:
		action = YOIX_LINK;
		break;

	    case MOVE:
		action = YOIX_MOVE;
		break;

	    default:
		action = YOIX_NONE;
		break;
	}
	return(action);
    }


    private boolean
    importData(YoixObject owner, Transferable t) {

	boolean  result = false;
	String   property;

	if ((property = data.getString(N_PROPERTY)) != null) {
	    if (owner.defined(property)) {
		owner.putObject(property, YoixDataTransfer.yoixTransferable(t, true));
		result = true;
	    }
	}
	return(result);
    }
}

