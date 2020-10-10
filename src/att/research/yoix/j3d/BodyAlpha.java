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

package att.research.yoix.j3d;
import java.util.HashMap;
import javax.media.j3d.Alpha;
import javax.media.j3d.RestrictedAccessException;
import att.research.yoix.*;

class BodyAlpha extends BodyNodeComponent

    implements Constants

{

    //
    // Our own reference to peer, which eliminates some casting.
    //

    private J3DAlpha  alpha = null;

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
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(20);

    static {
	activefields.put(NL_ALIVE, new Integer(VL_ALIVE));
	activefields.put(NL_CAPABILITIES, new Integer(VL_CAPABILITIES));
	activefields.put(NL_COMPILED, new Integer(VL_COMPILED));
	activefields.put(NL_COMPILED, new Integer(VL_COMPILED));
	activefields.put(NL_DEFAULTCAPABILITY, new Integer(VL_DEFAULTCAPABILITY));
	activefields.put(NL_LIVE, new Integer(VL_LIVE));
	activefields.put(NL_LOOP, new Integer(VL_LOOP));
	activefields.put(NL_MODE, new Integer(VL_MODE));
	activefields.put(NL_PATH, new Integer(VL_PATH));
	activefields.put(NL_PAUSETIME, new Integer(VL_PAUSETIME));
	activefields.put(NL_PHASEDELAY, new Integer(VL_PHASEDELAY));
	activefields.put(NL_RESUMETIME, new Integer(VL_RESUMETIME));
	activefields.put(NL_RUN, new Integer(VL_RUN));
	activefields.put(NL_STARTTIME, new Integer(VL_STARTTIME));
	activefields.put(NL_TRIGGERTIME, new Integer(VL_TRIGGERTIME));
	activefields.put(NL_VALUE, new Integer(VL_VALUE)); // execute
	activefields.put(NL_WAVEFORM, new Integer(VL_WAVEFORM));
    }

    //
    // A table that's used to control capabilities - low level setup
    // happens once when the loadCapabilities() methods are called in
    // the static initialization block that follows the table. Current
    // implementation seems error prone because we're required to pass
    // the correct classes to loadCapabilities(), so be careful if you
    // copy this stuff to different classes!!
    //

    private static Object  capabilities[] = {
     //
     // NAME                        CAPABILITY                                     VALUE
     // ----                        ----------                                     -----
    };

    static {
	loadCapabilities(capabilities, BodyAlpha.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyAlpha(J3DObject data) {

	this(null, data);
    }


    BodyAlpha(J3DAlpha alpha) {

	this(alpha, (J3DObject)VM.getTypeTemplate(T_ALPHA));
    }


    private
    BodyAlpha(J3DAlpha alpha, J3DObject data) {

	super(alpha, data);
	buildAlpha(alpha);
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

	return(ALPHA);
    }

    ///////////////////////////////////
    //
    // BodyAlpha Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj = null;
	int         field;

	try {
	    switch (field = activeField(name, activefields)) {
		case VL_VALUE:
		    obj =  builtinValue(name, argv);
		    break;

		default:
		    obj = executeField(field, name, argv);
		    break;
	    }
	}
	catch(RestrictedAccessException e) {
	    abort(e, name);
	}
	return(obj);
    }


    protected void
    finalize() {

	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	int  field;

	try {
	    switch (field = activeField(name, activefields)) {
		case VL_ALIVE:
		    obj = YoixObject.newInt(alpha.isAlive());
		    break;

		case VL_LOOP:
		    obj = YoixObject.newInt(alpha.getLoopCount());
		    break;

		case VL_MODE:
		    obj = getMode(obj);
		    break;

		case VL_PAUSETIME:
		    obj = getPauseTime(obj);
		    break;

		case VL_PHASEDELAY:
		    obj = getPhaseDelay(obj);
		    break;

		case VL_RESUMETIME:
		    obj = YoixObject.newDouble(((double)(alpha.getResumeTime()))/1000.0);
		    break;

		case VL_RUN:
		    obj = YoixObject.newInt(!alpha.isPaused());
		    break;

		case VL_STARTTIME:
		    obj = getStartTime(obj);
		    break;

		case VL_TRIGGERTIME:
		    obj = YoixObject.newDouble(((double)(alpha.getTriggerTime()))/1000.0);
		    break;

		case VL_WAVEFORM:
		    obj = getWaveform(obj);
		    break;

		default:
		    obj = getField(field, obj);
		    break;
	    }
	}
	catch(RestrictedAccessException e) {
	    abort(e, name);
	}
	return(obj);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	int  field;

	if (obj != null) {
	    try {
	        switch (field = activeField(name, activefields)) {
		    case VL_ALIVE:
			alpha.setAlive(obj.booleanValue());
			break;

		    case VL_LOOP:
			setLoop(obj);
			break;

		    case VL_MODE:
			setMode(obj);
			break;

		    case VL_PAUSETIME:
			alpha.pause((long)(obj.doubleValue()*1000.0));
			break;

		    case VL_PHASEDELAY:
			setPhaseDelay(obj);
			break;

		    case VL_RESUMETIME:
			alpha.resume((long)(obj.doubleValue() * 1000.0));
			break;

		    case VL_RUN:
			setRun(obj);
			break;

		    case VL_STARTTIME:
			setStartTime(obj);
			break;

		    case VL_TRIGGERTIME:
			setTriggerTime(obj);
			break;

		    case VL_WAVEFORM:
			setWaveform(obj);
			break;

		    default:
			setField(field, obj);
			break;
		}
	    }
	    catch(RestrictedAccessException e) {
		abort(e, name);
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
    buildAlpha(J3DAlpha alpha) {

	if ((this.alpha = alpha) == null) {
	    this.alpha = new J3DAlpha();
	    peer = this.alpha;
	    setField(NL_DEFAULTCAPABILITY);
	    setField(NL_TRIGGERTIME);
	    setField(NL_PHASEDELAY);
	    setField(NL_STARTTIME);
	    setField(NL_LOOP);
	    setField(NL_MODE);
	    setField(NL_PAUSETIME);
	    setField(NL_RESUMETIME);
	    setField(NL_WAVEFORM);
	    setField(NL_RUN);
	    setField(NL_ALIVE);
	}
	setField(NL_CAPABILITIES);
    }


    private YoixObject
    builtinValue(String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 0)
		obj = YoixObject.newDouble(alpha.value());
	    else if (arg[0].isNumber())
		obj = YoixObject.newDouble(alpha.value((long)(1000.0 * arg[0].doubleValue())));
	    else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(obj);
    }


    private YoixObject
    getMode(YoixObject obj) {

	int  mode;

	synchronized(alpha) {
	    mode = alpha.getMode();
	    if ((mode&(Alpha.INCREASING_ENABLE|Alpha.DECREASING_ENABLE)) == (Alpha.INCREASING_ENABLE|Alpha.DECREASING_ENABLE))
		mode = J3D_COMPLETE;
	    else if ((mode&Alpha.DECREASING_ENABLE) == Alpha.DECREASING_ENABLE)
		mode = J3D_DECREASING;
	    else mode = J3D_INCREASING;
	}

	return(YoixObject.newInt(mode));
    }


    private YoixObject
    getPauseTime(YoixObject obj) {

	synchronized(alpha) {
	    if (alpha.isPaused())
		obj = YoixObject.newDouble(((double)(alpha.getPauseTime()))/1000.0);
	    else obj = YoixObject.newInt(0);
	}

	return(obj);
    }


    private YoixObject
    getPhaseDelay(YoixObject obj) {

	obj = YoixObject.newDouble(((double)(alpha.getPhaseDelayDuration()))/1000.0);

	return(obj);
    }


    private YoixObject
    getStartTime(YoixObject obj) {

	obj = YoixObject.newDouble(((double)(alpha.getStartTime()))/1000.0);

	return(obj);
    }


    private YoixObject
    getWaveform(YoixObject obj) {

	YoixObject  wf = YoixObject.newArray(6);
	long        duration;

	synchronized(alpha) {
	    wf.putDouble(0, ((double)(duration = alpha.getIncreasingAlphaDuration()))/1000.0);
	    if (duration == 0)
		wf.putDouble(1, 0);
	    else wf.putDouble(1, ((double)(alpha.getIncreasingAlphaRampDuration()))/((double)(duration)));
	    wf.putDouble(2, ((double)(alpha.getAlphaAtOneDuration()))/1000.0);
	    wf.putDouble(3, ((double)(duration = alpha.getDecreasingAlphaDuration()))/1000.0);
	    if (duration == 0)
		wf.putDouble(4, 0);
	    else wf.putDouble(4, ((double)(alpha.getDecreasingAlphaRampDuration()))/((double)(duration)));
	    wf.putDouble(5, ((double)(alpha.getAlphaAtZeroDuration()))/1000.0);
	}

	return(wf);
    }


    private void
    setLoop(YoixObject obj) {

	int  loop = obj.intValue();

	if (loop < 0)
	    loop = -1;
	alpha.setLoopCount(loop);
    }


    private void
    setMode(YoixObject obj) {

	alpha.setMode(J3DMake.javaInt("Alpha", obj));
    }


    private void
    setPhaseDelay(YoixObject obj) {

	alpha.setPhaseDelayDuration((long)(obj.doubleValue()*1000.0));
    }


    private void
    setRun(YoixObject obj) {

        synchronized(alpha) {
	    if (obj.booleanValue()) {
		if (alpha.isPaused())
		    alpha.resume();
	    } else {
		if (!alpha.isPaused())
		    alpha.pause();
	    }
	}
    }


    private void
    setStartTime(YoixObject obj) {

	long  stime = (long)(obj.doubleValue() * 1000.0);

	if (stime > 0)
	    alpha.setStartTime(stime);
    }


    private void
    setTriggerTime(YoixObject obj) {

	alpha.setTriggerTime((long)(obj.doubleValue()*1000.0));
    }


    private void
    setWaveform(YoixObject obj) {

	YoixObject  yobj;
	double      dval = 0;
	double      duration;
	int         off;
	int         cnt;

	synchronized(alpha) {
	    if (obj.isNull()) {
		alpha.setIncreasingAlphaDuration(1000); // default also used by Java
		alpha.setIncreasingAlphaRampDuration(0);
		alpha.setAlphaAtOneDuration(0);
		alpha.setDecreasingAlphaDuration(0);
		alpha.setDecreasingAlphaRampDuration(0);
		alpha.setAlphaAtZeroDuration(0);
	    } else if (obj.isNumber()) {
		alpha.setIncreasingAlphaDuration((long)(obj.doubleValue() * 1000.0));
		alpha.setIncreasingAlphaRampDuration(0);
		alpha.setAlphaAtOneDuration(0);
		alpha.setDecreasingAlphaDuration(0);
		alpha.setDecreasingAlphaRampDuration(0);
		alpha.setAlphaAtZeroDuration(0);
	    } else if (obj.isArray()) {
		for (cnt = 0, off=obj.offset(); cnt < 6; cnt++, off++) {
		    if (off < obj.length()) {
			yobj = obj.getObject(off);
			if (yobj.isNumber())
			    dval = yobj.doubleValue();
			else VM.badArgument(NL_WAVEFORM, cnt);
		    } else dval = 0;

		    switch (cnt) {
			case 0:
			    alpha.setIncreasingAlphaDuration((long)(dval * 1000.0));
			    break;

			case 1:
			    if (dval < 0 || dval > 0.5)
				VM.abort(BADVALUE, NL_WAVEFORM, cnt);
			    duration = (double)alpha.getIncreasingAlphaDuration();
			    alpha.setIncreasingAlphaRampDuration((long)(dval * duration));
			    break;

			case 2:
			    alpha.setAlphaAtOneDuration((long)(dval * 1000.0));
			    break;

			case 3:
			    alpha.setDecreasingAlphaDuration((long)(dval * 1000.0));
			    break;

			case 4:
			    if (dval < 0 || dval > 0.5)
				VM.abort(BADVALUE, NL_WAVEFORM, cnt);
			    duration = (double)alpha.getDecreasingAlphaDuration();
			    alpha.setDecreasingAlphaRampDuration((long)(dval * duration));
			    break;

			case 5:
			    alpha.setAlphaAtZeroDuration((long)(dval * 1000.0));
			    break;
		    }
		}
	    } else VM.abort(BADVALUE, NL_WAVEFORM);
	}
    }
}

