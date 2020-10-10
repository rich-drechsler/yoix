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

package att.research.yoix.apple;
import com.apple.eawt.*;
import att.research.yoix.*;

public
class AppleApplicationAdapter extends ApplicationAdapter

    implements Constants

{

    YoixObject  aboutHandler = null;
    YoixObject  openApplicationHandler = null;
    YoixObject  openFileHandler = null;
    YoixObject  preferencesHandler = null;
    YoixObject  printFileHandler = null;
    YoixObject  quitHandler = null;
    YoixObject  reOpenApplicationHandler = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    AppleApplicationAdapter() {

	super();
    }

    ///////////////////////////////////
    //
    // AppleApplicationAdapter Methods
    //
    ///////////////////////////////////

    public void
    handleAbout(ApplicationEvent event) {

	YoixObject  yev;

	if (aboutHandler != null) {
	    yev = newEvent(event);
	    YoixMisc.call(aboutHandler, new YoixObject[] { yev }, null);
	    event.setHandled(yev.getBoolean(NL_HANDLED));
	}
	super.handleAbout(event);
    }


    public void
    handleOpenApplication(ApplicationEvent event) {

	YoixObject  yev;

	if (openApplicationHandler != null) {
	    yev = newEvent(event);
	    YoixMisc.call(openApplicationHandler, new YoixObject[] { yev }, null);
	    event.setHandled(yev.getBoolean(NL_HANDLED));
	}
	super.handleOpenApplication(event);
    }


    public void
    handleOpenFile(ApplicationEvent event) {

	YoixObject  yev;

	if (openFileHandler != null) {
	    yev = newEvent(event);
	    YoixMisc.call(openFileHandler, new YoixObject[] { yev }, null);
	    event.setHandled(yev.getBoolean(NL_HANDLED));
	}
	super.handleOpenFile(event);
    }


    public void
    handlePreferences(ApplicationEvent event) {

	YoixObject  yev;

	if (preferencesHandler != null) {
	    yev = newEvent(event);
	    YoixMisc.call(preferencesHandler, new YoixObject[] { yev }, null);
	    event.setHandled(yev.getBoolean(NL_HANDLED));
	}
	super.handlePreferences(event);
    }


    public void
    handlePrintFile(ApplicationEvent event) {

	YoixObject  yev;

	if (printFileHandler != null) {
	    yev = newEvent(event);
	    YoixMisc.call(printFileHandler, new YoixObject[] { yev }, null);
	    event.setHandled(yev.getBoolean(NL_HANDLED));
	}
	super.handlePrintFile(event);
    }


    public void
    handleQuit(ApplicationEvent event) {

	YoixObject  yev;

	if (quitHandler != null) {
	    yev = newEvent(event);
	    YoixMisc.call(quitHandler, new YoixObject[] { yev }, null);
	    event.setHandled(yev.getBoolean(NL_HANDLED) == false);
	}
	super.handleQuit(event);
    }


    public void
    handleReOpenApplication(ApplicationEvent event) {

	YoixObject  yev;

	if (reOpenApplicationHandler != null) {
	    yev = newEvent(event);
	    YoixMisc.call(reOpenApplicationHandler, new YoixObject[] { yev }, null);
	    event.setHandled(yev.getBoolean(NL_HANDLED));
	}
	super.handleReOpenApplication(event);
    }


    YoixObject
    newEvent(ApplicationEvent e) {

	YoixObject  yev;

	yev = YoixMakeEvent.yoixEvent(e, V_INVOCATIONACTION, null);
	yev.putInt(NL_HANDLED, e.isHandled());
	yev.putString(NL_FILENAME, e.getFilename());

	return(yev);
    }


    void
    setAboutHandler(YoixObject handler) {

	aboutHandler = handler;
    }


    void
    setOpenApplicationHandler(YoixObject handler) {

	openApplicationHandler = handler;
    }


    void
    setOpenFileHandler(YoixObject handler) {

	openFileHandler = handler;
    }


    void
    setPreferencesHandler(YoixObject handler) {

	preferencesHandler = handler;
    }


    void
    setPrintFileHandler(YoixObject handler) {

	printFileHandler = handler;
    }


    void
    setQuitHandler(YoixObject handler) {

	quitHandler = handler;
    }


    void
    setReOpenApplicationHandler(YoixObject handler) {

	reOpenApplicationHandler = handler;
    }
}
