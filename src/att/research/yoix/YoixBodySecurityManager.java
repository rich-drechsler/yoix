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
import java.util.*;

final
class YoixBodySecurityManager extends YoixPointerActive

{

    private YoixSecurityManager  manager;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD                        OBJECT       BODY
     // -----                        ------       ----
	N_CHECKACCEPT,               $LR_X,       null,
	N_CHECKCONNECT,              $LR_X,       null,
	N_CHECKCREATEROBOT,          $LR_X,       null,
	N_CHECKDELETE,               $LR_X,       null,
	N_CHECKEXEC,                 $LR_X,       null,
	N_CHECKEXIT,                 $LR_X,       null,
	N_CHECKLISTEN,               $LR_X,       null,
	N_CHECKMULTICAST,            $LR_X,       null,
	N_CHECKPROPERTIESACCESS,     $LR_X,       null,
	N_CHECKREAD,                 $LR_X,       null,
	N_CHECKREADDISPLAYPIXELS,    $LR_X,       null,
	N_CHECKREADENVIRONMENT,      $LR_X,       null,
	N_CHECKREADPROPERTY,         $LR_X,       null,
	N_CHECKSYSTEMCLIPBOARDACCESS,$LR_X,       null,
	N_CHECKWRITE,                $LR_X,       null,
	N_CHECKWRITEPROPERTY,        $LR_X,       null,
	N_CHECKYOIXADDPROVIDER,      $LR_X,       null,
	N_CHECKYOIXEVAL,             $LR_X,       null,
	N_CHECKYOIXEXECUTE,          $LR_X,       null,
	N_CHECKYOIXINCLUDE,          $LR_X,       null,
	N_CHECKYOIXMODULE,           $LR_X,       null,
	N_CHECKYOIXOPEN,             $LR_X,       null,
	N_CHECKYOIXREMOVEPROVIDER,   $LR_X,       null,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(3);

    static {
	activefields.put(N_INCHECK, new Integer(V_INCHECK));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodySecurityManager(YoixObject data) {

	super(data);
	buildSecurityManager();
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

	return(SECURITYMANAGER);
    }

    ///////////////////////////////////
    //
    // YoixBodySecurityManager Methods
    //
    ///////////////////////////////////

    protected final void
    finalize() {

	manager = null;
	super.finalize();
    }


    protected final synchronized YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_INCHECK:
		if (manager != null)
		    obj = YoixObject.newInt(manager.getInCheck());
		break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(manager);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildSecurityManager() {

	//
	// YoixSecurityManager extends SecurityManager, but Java won't
	// create a new SecurityManager when one is already installed.
	// We don't care if Yoix programs create SecurityManagers, so
	// we trap the exception and set manager to null.
	//

	try {
	    manager = new YoixSecurityManager(data, this);
	}
	catch(SecurityException e) {
	    manager = null;
	}
    }
}

