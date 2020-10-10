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
import java.applet.*;
import java.io.*;
import java.net.*;
import java.util.*;

final
class YoixBodyAudioClip extends YoixPointerActive

{

    private AudioClip  audioclip;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	N_DISABLED,         $LR__,       null,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(7);

    static {
	activefields.put(N_DISABLED, new Integer(V_DISABLED));
	activefields.put(N_LOOP, new Integer(V_LOOP));
	activefields.put(N_NAME, new Integer(V_NAME));
	activefields.put(N_PLAY, new Integer(V_PLAY));
	activefields.put(N_STOP, new Integer(V_STOP));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyAudioClip(YoixObject data) {

	super(data);
	buildAudioClip();
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

	return(AUDIOCLIP);
    }

    ///////////////////////////////////
    //
    // YoixBodyAudioClip Methods
    //
    ///////////////////////////////////

    protected final void
    finalize() {

	audioclip = null;
	super.finalize();
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_LOOP:
		obj = builtinLoop(name, argv);
		break;

	    case V_PLAY:
		obj = builtinPlay(name, argv);
		break;

	    case V_STOP:
		obj = builtinStop(name, argv);
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
	    case V_DISABLED:
		obj = getDisabled();
		break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(audioclip);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_NAME:
		    setName(obj);
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
    buildAudioClip() {

	audioclip = null;
	setField(N_NAME);
    }


    private synchronized YoixObject
    builtinLoop(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	boolean     result = false;

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 1) {
		if (arg[0].isString())
		    setName(arg[0]);
		else VM.badArgument(name, 0);
	    }
	    if (audioclip != null) {
		audioclip.loop();
		result = true;
	    }
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    builtinPlay(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	boolean     result = false;

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 1) {
		if (arg[0].isString())
		    setName(arg[0]);
		else VM.badArgument(name, 0);
	    }
	    if (audioclip != null) {
		audioclip.play();
		result = true;
	    }
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    builtinStop(String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg.length == 0) {
	    if (audioclip != null) {
		audioclip.stop();
		audioclip = null;
	    }
	    obj = YoixObject.newEmpty();
	} else VM.badCall(name);

	return(obj);
    }


    private synchronized YoixObject
    getDisabled() {

	return(YoixObject.newInt(audioclip == null));
    }


    private synchronized void
    setName(YoixObject obj) {

	InputStream  stream = null;
	String       name;
	URL          url;

	if (obj != null) {
	    name = obj.stringValue();
	    if (audioclip != null) {
		audioclip.stop();
	        audioclip = null;
	    }
	    if (name.length() > 0) {
		if (YoixMisc.guessStreamType(name) == FILE) {
		    name = YoixMisc.toYoixPath(name);
		    name = "file:" + YoixMisc.toLocalPath(name);
		}
		try {
		    url = new URL(name);
		    stream = YoixMisc.getInputStream(url);	// only for existence check
		    audioclip = Applet.newAudioClip(url);
		}
		catch(IOException e) {
		    VM.caughtException(e, true);
		}
		finally {
		    if (stream != null) {
			try {
			    stream.close();
			}
			catch(IOException e) {}
		    }
		}
	    }
	}
    }
}

