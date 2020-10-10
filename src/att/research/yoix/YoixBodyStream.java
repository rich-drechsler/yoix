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
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.*;
import javax.crypto.*;

public final
class YoixBodyStream extends YoixPointerActive

    implements YoixAPI,
	       YoixConstantsStream,
	       YoixInterfaceKillable

{

    //
    // The actual value assigned to stream depends on what kind of object
    // we're supposed to represent. For example, it will be a URLConnection
    // when we're a URL and a File when we're representing a local file.
    //
    // NOTE - there now are several fields that need Java 1.5 methods and
    // we're not currently using reflection to access them, so that means
    // Java 1.4.x is no longer supported. The proxy field is the only hard
    // one and that's because it uses an enum to set the type of the proxy
    // and accessing the enum via reflection probably needs Class methods
    // that were also added in Java 1.5.
    //

    private Object  stream;

    //
    // Low level stream fields.
    //

    private YoixDataInputStream   istream = null;
    private YoixDataOutputStream  ostream = null;
    private YoixConverterInput    btc = null;
    private YoixConverterOutput   ctb = null;

    private Checksum  iChecksum = null;
    private Checksum  oChecksum = null;

    private ZipInputStream   izip = null;
    private ZipOutputStream  ozip = null;
    private boolean          inEntry = false;

    private byte     buffer[] = null;
    private int      flushmode = FLUSHLINES;
    private long     chars_read = 0;
    private long     chars_written = 0;
    private boolean  autoready = false;

    private Object   inputinit = null;
    private Object   outputinit = null;

    private boolean  forceReset = false;
    private boolean  enforceReset = false;

    private String  fullname = null;

    private YoixStreamTrigger  readtrigger = null;
    private YoixStreamTrigger  writetrigger = null;
    private YoixStreamTrigger  eoftrigger = null;

    //
    // A counter for InterruptedIOExceptions (not the ones we throw). Was
    // added to help user scripts handle socket timeouts, which generate
    // InterruptedIOExceptions. We only increment interrupted, but scripts
    // can read or write it via the N_INTERRUPTED field that's defined by
    // all streams. We currently only handle interrupted reads, but writes
    // might also deserve attention - later.
    //

    private int  interrupted = 0;	// reads only right now

    //
    // URL specific fields that are used to record information obtained
    // from the connection. The response related fields are cleared when
    // open a new connection and should be explicitly set when we close
    // the connection or if we catch an IOException that's thrown by the
    // getInputStream() method. The request header identifies Yoix as the
    // user agent, unless there's an explict User-Agent set in the URL's
    // requestheader dictionary. In either case requestheader, it it's
    // not null, is the value used to make the current connection.
    //

    private String  responseerror = null;
    private Map     responseheader = null;
    private int     responsecode = -1;

    private long  requestifmodifiedsince = 0;
    private Map   requestheader = null;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private Object  permissions_init[] = {
     //
     // FIELD               OBJECT       BODY     PREV_OBJ     PREV_BODY
     // -----               ------       ----     --------     ---------
	N_MODE,             $LR__,       $LR__,   null,        null,
	N_NAME,             $LR__,       $LR__,   null,        null,
	N_PORT,             $LR__,       $LR__,   null,        null,
    };

    private Object  permissions_usage[] = {
     //
     // FIELD               OBJECT       BODY     PREV_OBJ     PREV_BODY
     // -----               ------       ----     --------     ---------
	N_BUFSIZE,          $LR__,       $LR__,   null,        null,
	N_CIPHER,           $LR__,       $LR__,   null,        null,
	N_ENCODING,         $LR__,       $LR__,   null,        null,
	N_FILTERS,          $LR__,       $LR__,   null,        null,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static final HashMap  activefields = new HashMap(35);

    static {
	activefields.put(N_AUTOREADY, new Integer(V_AUTOREADY));
	activefields.put(N_BUFSIZE, new Integer(V_BUFSIZE));
	activefields.put(N_CHECKSUM, new Integer(V_CHECKSUM));
	activefields.put(N_ENCODING, new Integer(V_ENCODING));
	activefields.put(N_CALLBACK, new Integer(V_CALLBACK));
	activefields.put(N_CONNECTTIMEOUT, new Integer(V_CONNECTTIMEOUT));
	activefields.put(N_FILE, new Integer(V_FILE));
	activefields.put(N_FLUSHMODE, new Integer(V_FLUSHMODE));
	activefields.put(N_FULLNAME, new Integer(V_FULLNAME));
	activefields.put(N_HOST, new Integer(V_HOST));
	activefields.put(N_IFMODIFIEDSINCE, new Integer(V_IFMODIFIEDSINCE));
	activefields.put(N_INTERRUPTED, new Integer(V_INTERRUPTED));
	activefields.put(N_MARKSUPPORTED, new Integer(V_MARKSUPPORTED));
	activefields.put(N_MODE, new Integer(V_MODE));
	activefields.put(N_NAME, new Integer(V_NAME));
	activefields.put(N_NEXTBUF, new Integer(V_NEXTBUF));
	activefields.put(N_NEXTCHAR, new Integer(V_NEXTCHAR));
	activefields.put(N_NEXTENTRY, new Integer(V_NEXTENTRY));
	activefields.put(N_NEXTLINE, new Integer(V_NEXTLINE));
	activefields.put(N_OPEN, new Integer(V_OPEN));
	activefields.put(N_PORT, new Integer(V_PORT));
	activefields.put(N_PROTOCOL, new Integer(V_PROTOCOL));
	activefields.put(N_READTIMEOUT, new Integer(V_READTIMEOUT));
	activefields.put(N_READY, new Integer(V_READY));
	activefields.put(N_REQUESTHEADER, new Integer(V_REQUESTHEADER));
	activefields.put(N_REQUESTMETHOD, new Integer(V_REQUESTMETHOD));
	activefields.put(N_RESPONSECODE, new Integer(V_RESPONSECODE));
	activefields.put(N_RESPONSEERROR, new Integer(V_RESPONSEERROR));
	activefields.put(N_RESPONSEHEADER, new Integer(V_RESPONSEHEADER));
	activefields.put(N_SIZE, new Integer(V_SIZE));
	activefields.put(N_USECACHES, new Integer(V_USECACHES));
	activefields.put(N_USINGPROXY, new Integer(V_USINGPROXY));
    }

    //
    // A full lookup during a dump is skipped for all fields named in
    // the sideeffects Hashtable. Probably only needed if reading the
    // field would cause an unwanted side effect. Best examples of the
    // problem are active stream fields (e.g., nextchar) that trigger
    // a read when we get() the value.
    //

    private static final Hashtable  sideeffects = new Hashtable(10);

    static {
	sideeffects.put(N_RESPONSECODE, Boolean.TRUE);
	sideeffects.put(N_RESPONSEHEADER, Boolean.TRUE);
	sideeffects.put(N_NEXTBUF, Boolean.TRUE);
	sideeffects.put(N_NEXTCHAR, Boolean.TRUE);
	sideeffects.put(N_NEXTENTRY, Boolean.TRUE);
	sideeffects.put(N_NEXTLINE, Boolean.TRUE);
	sideeffects.put(N_READY, Boolean.TRUE);
	sideeffects.put(N_SIZE, Boolean.TRUE);
    }

    //
    // Collection of all currently open streams, so they can be flushed
    // and closed on a graceful exit.
    //

    private static final Hashtable  openstreams = new Hashtable();
    private static final Hashtable  blockedthreads = new Hashtable();

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyStream(YoixObject data) {

	super(data);
	buildStream();
	setFixedSize();
    }


    YoixBodyStream(YoixObject data, InputStream stream) {

	super(data);
	this.stream = stream;
	buildStream();
	setFixedSize();
    }


    YoixBodyStream(YoixObject data, OutputStream stream) {

	super(data);
	this.stream = stream;
	buildStream();
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(STREAM);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceKillable Methods
    //
    ///////////////////////////////////

    public final void
    kill() {

	close();
    }

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public static void
    atInterrupt(Thread t) {

	Object  s;

	if (t != null) {
	    s = blockedthreads.remove(t);
	    if (t.isAlive()) {
		try {
		    if (s instanceof YoixDataInputStream)
			((YoixDataInputStream)s).close();
		    else if (s instanceof YoixDataOutputStream)
			((YoixDataOutputStream)s).close();
		    else if (s instanceof HttpURLConnection)
			((HttpURLConnection)s).disconnect();	// probably untested
		}
		catch(Exception e) {}
	    }
	}
    }

    ///////////////////////////////////
    //
    // YoixBodyStream Methods
    //
    ///////////////////////////////////

    final DataInputStream
    accessDataInputStream() {

	DataInputStream  rawStream = null;

	if (inputinit != null)
	    doInputInit();

	if (istream != null) {
	    if ((rawStream = istream.accessDataInputStream()) != null) {
		forceReset = true;
		enforceReset = true;
	    }
	}

	return(rawStream);
    }


    protected static void
    atExit() {

	YoixDataOutputStream  ldos;
	YoixConverterOutput   lctb;
	Enumeration           keys;
	Hashtable             opened;
	int                   count;

	opened = (Hashtable)openstreams.clone();

	for (keys = opened.keys(); keys.hasMoreElements(); ) {
	    ldos = (YoixDataOutputStream)keys.nextElement();
	    if ((lctb = (YoixConverterOutput)opened.get(ldos)) != null) {
		if (lctb.nextCharIdx > lctb.charStart) {
		    if ((count = lctb.convert()) > 0) {
			try {
			    ldos.write(lctb.byteBuf, lctb.nextByteIdx, count);
			}
			catch(IOException e) {
			    VM.caughtException(e);	// exiting - it will return
			}
			lctb.nextByteIdx += count;
		    }
		}
		try {
		    //
		    // A close() should be OK, but it's not required
		    // and sometimes seemed to hang on Linux systems.
		    //
		    ldos.flush();
		}
		catch(IOException e) {
		    VM.caughtException(e);	// exiting - it will return
		}
	    }
	}
    }


    final int
    available() {

	int  avail = 0;

	if (inputinit != null)
	    doInputInit();

	try {
	    if (stream instanceof ByteArrayOutputStream)
		avail = 0;
	    else if (stream instanceof URLConnection) {
		if (istream != null)
		    avail = istream.available();
	    } else if (stream instanceof OutputStream)
		avail = 0;
	    else if (istream != null)
		avail = istream.available();
	}
	catch(Exception e) {
	    avail = 0;
	    VM.caughtException(e, true);
	}

	return(avail);
    }


    final boolean
    checkMode(int mode) {

	if (inputinit != null && (mode&READ) != 0)
	    doInputInit();
	if (outputinit != null && (mode&WRITE) != 0)
	    doOutputInit();

	return(
	    (istream != null || (mode & READ) == 0) &&
	    (ostream != null || (mode & WRITE) == 0)
	);
    }


    final boolean
    close() {

	return(close(READ|WRITE));
    }


    final boolean
    close(String str) {

	return(close(pickStreamMode(str)));
    }


    final synchronized boolean
    close(int mode) {

	boolean  result = true;

	//
	// This probably needs a bit more work - an InterruptedIOException
	// can now make us quit (via VM.caughtException) before both parts
	// of the stream are properly closed. Ignoring the problem for now
	// because it probably won't happen often. Also not certain where
	// the fix belongs (here or in our low level interrupt code). One
	// easy fix would be to catch and remember InterruptedIOExceptions
	// and only call caughtException() at the end. Probably not a great
	// solution (e.g., YoixBodySocket and others) may have to deal with
	// the same issues, but they want to call this method twice.
	//

	if (outputinit != null && (mode&WRITE) != 0)
	    cancelOutputInit();
	if (inputinit != null && (mode&READ) != 0)
	    cancelInputInit();

	if (istream != null || ostream != null) {
	    if ((mode&READ) != 0) {
		if (istream != null) {
		    if (iChecksum != null) {
			VM.pushAccess(RW_);
			data.putDouble(N_CHECKSUM, (double)iChecksum.getValue());
			VM.popAccess();
		    }
		    try {
			istream.close();
		    }
		    catch(IOException e) {
			result = false;
			VM.caughtException(e, true);	// might not close ostream
		    }
		    finally {
			istream = null;
			iChecksum = null;
			izip = null;
			inEntry = false;
		    }
		}
	    }

	    if ((mode&WRITE) != 0) {
		if (ostream != null) {
		    flush();
		    try {
			if (oChecksum != null) {
			    VM.pushAccess(RW_);
			    data.putDouble(N_CHECKSUM, (double)oChecksum.getValue());
			    VM.popAccess();
			}
			ostream.close();
			if (stream != null && stream instanceof ByteArrayOutputStream) {
			    //
			    // We close first to make sure everything gets
			    // to the byte stream as it should, but since
			    // closing doesn't affect a byte stream we can
			    // still use "toString" after the close.
			    //
			    VM.pushAccess(RW_);
			    try {
				data.putString(N_NAME, ((ByteArrayOutputStream)stream).toString(data.getString(N_ENCODING)));
			    }
			    catch(UnsupportedEncodingException e) {
				data.putString(N_NAME, ((ByteArrayOutputStream)stream).toString());
				VM.caughtException(e, true);
			    }
			    VM.popAccess();
			}
		    }
		    catch(IOException e) {
			result = false;
			VM.caughtException(e, true);
		    }
		    finally {
		        openstreams.remove(ostream);
			ostream = null;
			oChecksum = null;
			ozip = null;
			inEntry = false;
		    }
		}
	    }

	    if (istream == null && ostream == null && inputinit == null && outputinit == null) {
		recordResponse(stream);
		stream = null;
		requestheader = null;
		resetPermissions(permissions_init, 5);
		resetPermissions(permissions_usage, 5);
	    }
	}

	return(result);
    }


    final synchronized boolean
    closeEntry() {

	boolean  success = false;

	if (inputinit != null)
	    doInputInit();

	if (izip != null) {
	    try {
		izip.closeEntry();
		success = true;
	    }
	    catch(Exception e) {
		VM.caughtException(e);
	    }
	} else if (ozip != null) {
	    try {
		flush();
		ozip.closeEntry();
		success = true;
	    }
	    catch(Exception e) {
		VM.caughtException(e);
	    }
	}

	return(success);
    }


    protected YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_CALLBACK:
		obj =  builtinCallback(name, argv);
		break;

	    default:
		obj = null;
		break;
	}
	return(obj);
    }


    protected final void
    finalize() {

	close();		// cleans openstreams
	stream = null;
	super.finalize();
    }


    final boolean
    flush() {

	boolean  result = false;
	int      count;

	if (outputinit != null)
	    doOutputInit();

	if (ostream != null) { // if null, nothing to flush
	    try {
		if (ctb.nextCharIdx > ctb.charStart) {
		    if ((count = ctb.convert()) > 0) {
			ostream.write(ctb.byteBuf, ctb.nextByteIdx, count);
			ctb.nextByteIdx += count;
		    }
		}
		ostream.flush();
		result = true;
	    }
	    catch(Exception e) {
		result = false;
		VM.caughtException(e, true);
	    }
	}
	return(result);
    }


    final int
    getChar() {

	return(getChar(true));
    }


    final int
    getChar(boolean use_eof_trigger) {

	Thread  thread = Thread.currentThread();
	int     ch = YOIX_EOF;
	int     count;

	if (inputinit != null)
	    doInputInit();

	try {
	    outer_context: if (istream != null) {
		if (forceReset || istream.bufferResetNeeded()) {
		    flush();
		    btc.charStart = btc.charEnd;
		    forceReset = enforceReset;
		}

		while (btc.charStart >= btc.charEnd) {
		    if (thread.isInterrupted() == false) {
			try {
			    beginBlockedOperation(thread, istream);
			    count = istream.read(buffer);
			}
			catch(InterruptedIOException e) {
			    //
			    // Without this timeouts during socket reads
			    // end up calling caughtException() which can
			    // end the current thread's run function.
			    //
			    interrupted++;
			    ch = YOIX_EOF;
			    break;
			}
			finally {
			    endBlockedOperation(thread);
			}
			if (count < 0) {
			    if (btc.flush() > 0)
				break;
			    break outer_context;
			}
			btc.convert(buffer, 0, count);
			if (readtrigger != null)
			    readtrigger.update(count);
		    } else throw(new InterruptedIOException());
		}
		if (thread.isInterrupted() == false) {
		    ch = btc.charBuf[btc.charStart++];
		    chars_read++;
		} else throw(new InterruptedIOException());
	    }
	}
	catch(IOException e) {
	    ch = YOIX_EOF;
	    VM.caughtException(e, true);
	}

	if (use_eof_trigger && eoftrigger != null && ch == YOIX_EOF)
	    eoftrigger.wrapup(chars_read);

	return(ch);
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_AUTOREADY:
		obj = YoixObject.newInt(autoready);
		break;

	    case V_CHECKSUM://no need for inputinit/outputinit checks
		if (istream != null && iChecksum != null)
		    obj = YoixObject.newDouble((double)iChecksum.getValue());
		else if (ostream != null && oChecksum != null)
		    obj = YoixObject.newDouble((double)oChecksum.getValue());
		break;

	    case V_CONNECTTIMEOUT:
		obj = getConnectTimeout(obj);
		break;

	    case V_ENCODING:
		obj = YoixObject.newString(data.getString(N_ENCODING));
		break;

	    case V_FILE:
		obj = getURLField(V_FILE, obj);
		break;

	    case V_FLUSHMODE:
		obj = YoixObject.newInt(flushmode);
		break;

	    case V_FULLNAME:
		obj = YoixObject.newString(fullname);
		break;

	    case V_HOST:
		obj = getURLField(V_HOST, obj);
		break;

	    case V_IFMODIFIEDSINCE:
		obj = getIfModifiedSince(obj);
		break;

	    case V_INTERRUPTED:
		obj = YoixObject.newInt(interrupted);
		break;

	    case V_MARKSUPPORTED:
		obj = YoixObject.newInt(markSupported());
		break;

	    case V_NAME:
		obj = getName(obj);
		break;

	    case V_NEXTBUF:
		if (inputinit != null)
		    doInputInit();
		obj = YoixObject.newString(read(btc == null ? 0 : btc.getBufsize()));
		break;

	    case V_NEXTCHAR:
		obj = YoixObject.newInt(read());
		break;

	    case V_NEXTENTRY:
		obj = YoixObject.newZipEntry(getZipEntry());
		break;

	    case V_NEXTLINE:
		obj = YoixObject.newString(readLine(EOL_MASK));
		break;

	    case V_OPEN:
		obj = YoixObject.newInt(isOpen());
		break;

	    case V_PORT:
		obj = getURLField(V_PORT, obj);
		break;

	    case V_PROTOCOL:
		obj = getURLField(V_PROTOCOL, obj);
		break;

	    case V_READTIMEOUT:
		obj = getReadTimeout(obj);
		break;

	    case V_READY:
		obj = YoixObject.newInt(ready());
		break;

	    case V_REQUESTHEADER:
		obj = getRequestHeader(obj);
		break;

	    case V_REQUESTMETHOD:
		obj = getRequestMethod(obj);
		break;

	    case V_RESPONSECODE:
		obj = getResponseCode(obj);
		break;

	    case V_RESPONSEERROR:
		obj = getResponseError(obj);
		break;

	    case V_RESPONSEHEADER:
		obj = getResponseHeader(obj);
		break;

	    case V_SIZE:
		obj = YoixObject.newDouble((double)getSize());
		break;

	    case V_USECACHES:
		obj = getUseCaches(obj);
		break;

	    case V_USINGPROXY:
		obj = getUsingProxy(obj);
		break;
	}

	return(obj);
    }


    final long
    getInputCount() {

	return(chars_read);
    }


    protected final Object
    getManagedObject() {

	return(stream);
    }


    final long
    getOutputCount() {

	return(chars_written);
    }


    final long
    getSize() {

	long  size = -1;

	try {
	    if (stream instanceof File) {
		flush();
		size = (new File(((File)stream).getAbsolutePath())).length();	// note: bytes, not chars
	    } else if (stream instanceof RandomAccessFile) {
		flush();
		size = ((RandomAccessFile)stream).length();
	    } else if (stream instanceof ByteArrayOutputStream) {
		flush();
		try {
		    size = ((ByteArrayOutputStream)stream).toString(data.getString(N_ENCODING)).length();	// note: chars, not bytes
		}
		catch(UnsupportedEncodingException e) {
		    size = ((ByteArrayOutputStream)stream).toString().length();	// note: chars, not bytes
		}
	    } else if (stream instanceof URLConnection)
		size = ((URLConnection)stream).getContentLength();	// note: bytes, not chars
	    else if (stream instanceof InputStream)
		size = chars_read;	// note: chars, not bytes
	    else if (stream instanceof OutputStream)
		size = chars_written;	// note: chars, not bytes
	    else if (stream instanceof YoixBodyString)
		size = ((YoixBodyString)stream).length();	// note: chars, not bytes
	    else size = -1;
	}
	catch(Exception e) {
	    size = -1;
	    VM.caughtException(e, true);
	}

	return(size);
    }


    final synchronized boolean
    isFile() {

	return(getMinor() == FILE);
    }


    final synchronized boolean
    isOpen() {

	return(istream != null || ostream != null || inputinit != null || outputinit != null);
    }


    final synchronized boolean
    isStringStream() {

	return(getMinor() == STRINGSTREAM);
    }


    final synchronized boolean
    isURL() {

	return(getMinor() == URL);
    }


    final boolean
    mark(int size) {

	boolean  result;

	//
	// NOTE - the inputinit test is done by markSupported().
	//

	if (markSupported()) {
	    btc.mark(size);
	    result = true;
	} else result = false;

	return(result);
    }


    final boolean
    markSupported() {

	if (inputinit != null)
	    doInputInit();

	return(istream != null);
    }


    final long
    offsetBytes() {

	return(offsetBytes(-1));
    }


    final long
    offsetBytes(long newOffset) {

	long  offset = -1;

	if (inputinit != null)
	    doInputInit();

	try {
	    if (istream != null) {
		if (ostream != null) {
		    flush();
		}
		if (newOffset >= 0) {
		    istream.seek(newOffset);
		} else {
		    if (btc.charStart >= btc.charEnd) {
			offset = istream.getOffset();
		    } else {
			offset = istream.getOffset() - btc.charEnd + btc.charStart;
		    }
		    istream.seek(offset);
		}
		btc.charStart = btc.charEnd;
		offset = istream.getOffset();
	    } else if (ostream != null) {
		flush();
		if (newOffset >= 0)
		    ostream.seek(newOffset);
		offset = ostream.getOffset();
	    } else offset = -1;
	}
	catch(IOException e) {
	    offset = -1;
	    VM.caughtException(e, true);
	}

	return(offset);
    }


    final boolean
    offsetSupported() {

	boolean supported = false;

	if (inputinit != null || istream != null) {
	    if (inputinit != null)
		doInputInit();
	    if (istream != null)
		supported = istream.seekSupported();
	} else if (outputinit != null || ostream != null) {
	    if (outputinit != null)
		doOutputInit();
	    if (ostream != null)
		supported = ostream.seekSupported();
	}

	return(supported);
    }


    final boolean
    open() {

	return(open(pickStreamMode()));
    }


    final boolean
    open(String str) {

	return(open(pickStreamMode(str)));
    }


    final synchronized boolean
    open(int mode) {

	SecurityManager  sm;
	YoixObject       name;
	boolean          result = false;
	int              minor;

	if ((mode&(READ|WRITE)) != 0) {
	    minor = getMinor();
	    name = data.getObject(N_NAME, null);
	    if (name != null && (name.notNull() || (minor == STRINGSTREAM))) {
		if ((sm = System.getSecurityManager()) instanceof YoixSecurityManager) {
		    ((YoixSecurityManager)sm).checkYoixOpen(
			YoixObject.newString(name.stringValue()),
			YoixObject.newInt(minor),
			YoixObject.newInt(mode)
		    );
		}

		fullname = null;
		responsecode = -1;
		responseerror = null;
		responseheader = null;

		switch (minor) {
		    case FILE:
			result = openFile(name, mode);
			break;

		    case STRINGSTREAM:
			result = openString(name, mode);
			break;

		    case URL:
			result = openURL(name, mode);
			break;

		    default:
			VM.abort(UNIMPLEMENTED);
			break;
		}
	    } else VM.abort(BADVALUE, N_NAME);
	} else VM.abort(BADVALUE, N_MODE);

	if (result)
	    setPermissions(permissions_init, 5);

	return(result);
    }


    final synchronized int
    read() {

	return(autoready && !ready() ? YOIX_EOF : getChar());
    }


    final synchronized String
    read(int length) {

	return(autoready && !ready() ? null : (String)getChars(length, true));
    }


    final synchronized int
    read(char buf[]) {

	return(read(buf, 0, buf.length));
    }


    final synchronized int
    read(char buf[], int offset, int length) {

	return(read(buf, offset, length, -1, null));
    }


    final synchronized int
    read(char buf[], int offset, int length, int eol_flag, boolean eol_info[]) {

	char  tmpbuf[];
	int   total;

	if (autoready == false || ready()) {
	    if (length > 0) {
		tmpbuf = new char[length];
		if ((total = getChars(tmpbuf, 0, length, eol_flag, eol_info)) != YOIX_EOF) {
		    if (total > 0)
			System.arraycopy(tmpbuf, 0, buf, offset, total);
		}
	    } else total = 0;
	} else total = YOIX_EOF;

	return(total);
    }


    final synchronized int
    readInto(char buf[], int offset, int length, int eol_flag, boolean eol_info[]) {

	int  total;

	//
	// Undoubtedly should be doing some bounds checking here!! Same
	// thing probably applies to some of the other read methods.
	//

	if (autoready == false || ready()) {
	    if (length > 0)
		total = getChars(buf, offset, length, eol_flag, eol_info);
	    else total = 0;
	} else total = YOIX_EOF;

	return(total);
    }


    final synchronized String
    readLine(int eol_flag) {

	boolean  eol_info[] = new boolean[2];
	String   line = null;
	char     chars[];

	//
	// This readLine (with no limit) strips EOL.
	//

	eol_info[0] = false;	// T/F slot for was there a new line
	eol_info[1] = false;	// T/F slot for is it a 2-char EOL

	if (autoready && !ready())
	    return(null);

	if ((chars = readStream(-1, eol_flag, eol_info)) != null) {
	    if (eol_info[0]) {
		if (eol_info[1])
		    line = new String(chars, 0, chars.length - 2);
		else line = new String(chars, 0, chars.length - 1);
	    } else line = new String(chars);
	}

	return(line);
    }


    final synchronized String
    readLine(int maxch, int eol_flag) {

	boolean  eol_info[] = new boolean[2];
	String   line = null;
	char     chars[];

	//
	// This readLine (with limit) does not strip EOL.
	//

	eol_info[0] = false; // T/F slot for was there a new line
	eol_info[1] = false; // T/F slot for is it a 2-char EOL

	if (autoready && !ready())
	    return(null);

	if (maxch == 0)
	    line = "";
	else if ((chars = readStream(maxch, eol_flag, eol_info)) != null)
	    line = new String(chars);

	return(line);
    }


    final synchronized char[]
    readStream(int requested) {

	return(readStream(requested, -1, null));
    }


    final synchronized char[]
    readStream(int requested, int eol_flag, boolean eol_info[]) {

	ArrayList  blocks;
	boolean    eof = true;
	char       result[] = null;
	char       block[];
	int        offset;
	int        count;
	int        total;

	if (requested <= 0) {
	    blocks = new ArrayList();
	    block = new char[4*BUFSIZ];
	    total = 0;
	    offset = 0;
	    while ((count = readInto(block, offset, block.length - offset, eol_flag, eol_info)) >= 0) {
		eof = false;
		total += count;
		offset += count;
		if (eol_info != null && eol_info[0])
		    break;
		if (offset == block.length) {
		    blocks.add(block);
		    block = new char[Math.min(2*block.length/BUFSIZ, 64)*BUFSIZ];
		    offset = 0;
		}
	    }
	    if (eof)
		result = null;
	    else {
		blocks.add(block);
		result = new char[total];
		offset = 0;
		while (offset < total) {
		    block = (char [])blocks.remove(0);
		    count = Math.min(total - offset, block.length);
		    System.arraycopy(block, 0, result, offset, count);
		    offset += count;
		    block = null;
		}
	    }
	} else {
	    block = new char[requested];
	    if ((count = read(block, 0, requested, eol_flag, eol_info)) >= 0) {
		if (count < requested) {
		    result = new char[count];
		    System.arraycopy(block, 0, result, 0, count);
		} else result = block;
	    } else result = null;
	}

	return(result);
    }


    final synchronized boolean
    ready() {

	if (inputinit != null)
	    doInputInit();

	return(istream != null && ((btc.charStart < btc.charEnd) || istream.ready()));
    }


    final void
    releaseDataInputStream() {

	enforceReset = false;
    }


    final boolean
    reopen(YoixBodyStream lstream) {

	String  buffer = null;

	if (inputinit != null)
	    doInputInit();

	flush(); // does an outputinit check, too
	close();

	if (lstream.isOpen()) {
	    if (lstream.getMinor() == STRINGSTREAM) {
		buffer = new String(lstream.readStream(-1));
	    }
	    lstream.close();
	}

	chars_read = 0;
	chars_written = 0;
	changeData(lstream.data);
	if (buffer != null)
	    data.putString(N_NAME, buffer);
	return(open());
    }


    final boolean
    reopen(String path, String mode) {

	boolean  isopen = false;
	int      minor = YoixMisc.guessStreamType(path);

	if (inputinit != null)
	    doInputInit();

	flush(); // does an outputinit check, too
	close();

	chars_read = 0;
	chars_written = 0;

	if (minor != getMinor()) {
	    YoixObject oldData = data;
	    switch (minor) {
		case URL:
		    data = VM.getTypeTemplate(T_URL);
		    break;

		case FILE:
		default:
		    data = VM.getTypeTemplate(T_FILE);
		    break;
	    }
	    // common elements that should probably be carried over
    	    data.put(N_ENCODING, oldData.get(N_ENCODING, true), false);
    	    data.put(N_FLUSHMODE, oldData.get(N_FLUSHMODE, true), false);
    	    data.put(N_BUFSIZE, oldData.get(N_BUFSIZE, true), false);
	}
	VM.pushAccess(LRW_);
	data.putString(N_NAME, path);
	isopen = open(mode);
	VM.popAccess();
	return(isopen);
    }


    final boolean
    reset() {

	boolean  result = false;

	//
	// NOTE - the inputinit test is done by markSupported().
	//

	try {
	    if (markSupported()) {
		btc.reset();
		result = true;
	    }
	}
	catch(IOException e) {
	    result = false;
	    VM.caughtException(e, true);
	}
	return(result);
    }


    final void
    resetDataInputStream() {

	forceReset = true;
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	int  mode;

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_AUTOREADY:
		    autoready = obj.booleanValue();
		    break;

		case V_BUFSIZE:
		    data.putInt(N_BUFSIZE, Math.max(obj.intValue(), 1));
		    break;

	        case V_CHECKSUM:
		    // writing any value, resets it
		    if (istream != null && iChecksum != null)
		        iChecksum.reset();
		    else if (ostream != null && oChecksum != null)
		        oChecksum.reset();
		    break;

		case V_ENCODING:
		    setEncoding(obj);
		    break;

		case V_FLUSHMODE:
		    flushmode = obj.intValue();
		    break;

		case V_INTERRUPTED:
		    interrupted = obj.intValue();
		    break;

		case V_IFMODIFIEDSINCE:
		    setIfModifiedSince(obj);
		    break;

		case V_MODE:
		    setMode(obj);
		    break;

		case V_NEXTBUF:
		    write(obj.stringValue());
		    break;

		case V_NEXTCHAR:
		    write(obj.intValue());
		    break;

		case V_NEXTENTRY:
		    putZipEntry(obj);
		    break;

		case V_NEXTLINE:
		    writeLine(obj.stringValue());
		    break;

		case V_OPEN:
		    if (obj.intValue() == 0) {
			if (isOpen())
			    close();
		    } else {
			if (!isOpen())
			    open();
		    }
		    break;
	    }
	}
	return(obj);
    }


    final void
    setEncoding(YoixObject obj) {

	String  dflt;

	dflt = YoixConverter.getSupportedEncoding(YoixMisc.getDefaultEncoding(), YoixConverter.getISO88591Encoding());

	if (obj.isNull())
	    data.putString(N_ENCODING, dflt);
	else data.putString(N_ENCODING, YoixConverter.getSupportedEncoding(obj, dflt));
	// double check -- should not be necessary, but let's be double sure
	dflt = data.getString(N_ENCODING);
	if (!Charset.isSupported(dflt))
	    VM.abort(BADENCODING, dflt);
    }


    final long
    setLength(long newLength) {

	long  length = -1;

	if (inputinit != null)
	    doInputInit();

	try {
	    if (istream != null) {
		if (ostream != null) {
		    flush();
		}
		length = istream.truncate(newLength);
		btc.charStart = btc.charEnd;
	    } else if (ostream != null) {
		flush();
		length = ostream.truncate(newLength);
	    } else length = -1;
	}
	catch(IOException e) {
	    length = -1;
	    VM.caughtException(e, true);
	}

	return(length);
    }


    final int
    setZipComment(String comment) {

	int  result = 0;

	if (outputinit != null)
	    doOutputInit();

	if (ozip != null) {
	    if (comment != null) {
		try {
		    flush();
		    ozip.setComment(comment);
		}
		catch(RuntimeException e) {
		    result = 2;
		    VM.caughtException(e);
		}
	    }
	} else result = 1;

	return(result);
    }


    final int
    setZipLevel(int level) {

	int  result = 0;

	if (outputinit != null)
	    doOutputInit();

	if (ozip != null) {
	    flush();
	    try {
		ozip.setLevel(level);
	    }
	    catch(IllegalArgumentException e) {
		ozip.setLevel(Deflater.DEFAULT_COMPRESSION);
		VM.caughtException(e);
	    }
	} else result = 1;

	return(result);
    }


    final int
    setZipMethod(int method) {

	int  result = 0;

	if (outputinit != null)
	    doOutputInit();

	if (ozip != null) {
	    flush();
	    try {
		ozip.setMethod(method);
	    }
	    catch(IllegalArgumentException e) {
		ozip.setMethod(ZipOutputStream.DEFLATED);
		VM.caughtException(e);
	    }
	} else result = 1;

	return(result);
    }


    protected final boolean
    sideEffects(String name) {

	return(sideeffects != null && sideeffects.containsKey(name));
    }


    final int
    ungetChar(int ich) {

	char  ch;
	char  newbuf[];
	int   unget_size;
	int   crnt_size;

	if (inputinit != null)
	    doInputInit();

	if (ich == YOIX_EOF)
	    return(ich);
	else ch = (char)ich;

	unget_size = 1;
	crnt_size = btc.charEnd - btc.charStart;

	if (unget_size <= btc.charStart) {
	    btc.charStart -= unget_size;
	    btc.charBuf[btc.charStart] = ch;
	} else if ((crnt_size + unget_size) <= btc.charBuf.length) {
	    System.arraycopy(btc.charBuf, btc.charStart, btc.charBuf, btc.charBuf.length-crnt_size, crnt_size);
	    btc.charStart = btc.charBuf.length - crnt_size - unget_size;
	    btc.nextCharIdx = btc.charEnd = btc.charBuf.length;
	    btc.charBuf[btc.charStart] = ch;
	} else {
	    newbuf = new char[crnt_size+unget_size];
	    newbuf[0] = ch;
	    System.arraycopy(btc.charBuf, btc.nextCharIdx, newbuf, unget_size, crnt_size);
	    btc.charStart = 0;
	    btc.nextCharIdx = btc.charEnd = newbuf.length;
	    btc.charBuf = newbuf;
	}

	chars_read -= unget_size;
	return(ch);
    }


    final synchronized int
    write(int ch) {

	int  nc;

	nc = putChar(ch);
	if (nc > 0 && flushmode >= FLUSHWRITES)
	    flush();
	return(nc);
    }


    final synchronized int
    write(String str) {

	char  tmpbuf[];
	int   len = str.length();
	int   nc;

	tmpbuf = new char[len];
	str.getChars(0, len, tmpbuf, 0);

	nc = putChars(tmpbuf, 0, len);
	if (nc > 0 && flushmode >= FLUSHWRITES)
	    flush();
	return(nc);
    }


    final synchronized int
    write(char buf[], int off, int len) {

	int  nc;

	nc = putChars(buf, off, len);
	if (nc > 0 && flushmode >= FLUSHWRITES)
	    flush();
	return(nc);
    }


    final synchronized int
    writeLine(String str) {

	char  tmpbuf[];
	int   len = str.length();
	int   nc;

	tmpbuf = new char[len + NLCHARS.length];
	str.getChars(0, len, tmpbuf, 0);
	System.arraycopy(NLCHARS, 0, tmpbuf, len, NLCHARS.length);
	nc = putChars(tmpbuf, 0, len + NLCHARS.length);
	if (nc > 0 && flushmode >= FLUSHLINES)
	    flush();
	return(nc);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static void
    beginBlockedOperation(Object s) {

	beginBlockedOperation(Thread.currentThread(), s);
    }


    private static void
    beginBlockedOperation(Thread t, Object s) {

	if (t != null && s != null)	// null s is possible but very unlikely
	    blockedthreads.put(t, s);
    }


    private void
    buildStream() {

	setField(N_BUFSIZE);
	setField(N_ENCODING);
	setField(N_FLUSHMODE);
	setField(N_IFMODIFIEDSINCE);
	setField(N_MODE);
	setField(N_OPEN);
    }


    private synchronized YoixObject
    builtinCallback(String name, YoixObject arg[]) {

	YoixObject  result = null;
	boolean     status = false;
	int         mode;
	int         trigger = 0;

	if (arg.length > 0) {
	    if (arg[0].isInteger()) {
		mode = arg[0].intValue();
		if (mode == READ || mode == WRITE || mode == YOIX_EOF) {
		    if (arg.length == 1) {
			if (mode == READ) {
			    if (readtrigger != null)
				result = readtrigger.getFunction();
			} else if (mode == WRITE) {
			    if (writetrigger != null)
				result = writetrigger.getFunction();
			} else { // mode == YOIX_EOF
			    if (eoftrigger != null)
				result = eoftrigger.getFunction();
			}
		    } else if (arg[1].notNull() && arg[1].isFunction()) {
			if (arg[1].callable(1)) {
			    if (mode == READ) {
				if (arg.length == 2) {
				    if (readtrigger == null || readtrigger.getFunction().body() != arg[1].body())
					VM.badCall(name);
				    else result = YoixObject.newInt(readtrigger.getTriggerSize());
				} else if (arg.length == 3) {
				    if (arg[2].isInteger()) {
					if ((trigger = arg[2].intValue()) > 0)
					    readtrigger = new YoixStreamTrigger(mode, trigger, arg[1]);
					else VM.abort(BADVALUE, name, 2);
				    } else VM.badArgument(name, 2);
				} else VM.badCall(name);
			    } else if (mode == WRITE) {
				if (arg.length == 2) {
				    if (writetrigger == null || writetrigger.getFunction() != arg[1])
					VM.badCall(name);
				    else result = YoixObject.newInt(writetrigger.getTriggerSize());
				} else if (arg.length == 3) {
				    if (arg[2].isInteger()) {
					if ((trigger = arg[2].intValue()) > 0)
					    writetrigger = new YoixStreamTrigger(mode, trigger, arg[1]);
					else VM.abort(BADVALUE, name, 2);
				    } else VM.badArgument(name, 2);
				} else VM.badCall(name);
			    } else { // mode == YOIX_EOF
				if (arg.length == 2) {
				    eoftrigger = new YoixStreamTrigger(mode, chars_read, arg[1]);
				} else VM.badCall(name);
			    }
			} else VM.abort(BADVALUE, name, 1);
		    } else VM.badArgument(name, 1);
		} else VM.abort(BADVALUE, name, 0); // must be arg 0
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(result == null ? YoixObject.newNull() : result);
    }


    private synchronized void
    cancelInputInit() {

	if (inputinit != null) {
	    try {
		if (inputinit instanceof InputStream)
		    ((InputStream)inputinit).close();
	    }
	    catch(IOException e) {}
	    finally {
		inputinit = null;
	    }
	}
    }


    private synchronized void
    cancelOutputInit() {

	if (outputinit != null) {
	    try {
		if (outputinit instanceof OutputStream)
		    ((OutputStream)outputinit).close();
	    }
	    catch(IOException e) {}
	    finally {
		outputinit = null;
	    }
	}
    }


    private synchronized void
    doInputInit() {

	InputStream  inStream = null;
	Cipher       cipher;
	String       encoding;
	byte         bytes[];
	int          bufsize;
	int          filters;

	if (inputinit != null && istream == null) {
	    // force output stream set-up
	    if (outputinit != null)
		doOutputInit();
	    encoding = data.getString(N_ENCODING);
	    bufsize = data.getInt(N_BUFSIZE, BUFSIZ);
	    try {
		switch (getMinor()) {
		case URL:
		    if (ostream != null) {
			close(WRITE);
		    }
		    if (inputinit instanceof URLConnection)
			inStream = (new YoixInterruptable(inputinit)).getInputStream();
		    else if (inputinit instanceof InputStream)
			inStream = (InputStream)inputinit;
		    else throw new RuntimeException("bad inputinit URL value, class=" + (inputinit == null ? "null" : inputinit.getClass().getName()));
		    break;

		case FILE:
		    if (inputinit instanceof InputStream)
			inStream = (InputStream)inputinit;
		    else throw new RuntimeException("bad inputinit FILE value, class=" + (inputinit == null ? "null" : inputinit.getClass().getName()));
		    break;

	        case STRINGSTREAM:
		    if (inputinit instanceof String) {
			try {
			    bytes = ((String)inputinit).getBytes(encoding);
			    inStream = new ByteArrayInputStream(bytes);
			}
			catch(UnsupportedEncodingException e) {
			    // encoding is validated when set, so we messed up
			    VM.abort(INTERNALERROR);
			}
		    } else throw new RuntimeException("bad inputinit STRING value, class=" + (inputinit == null ? "null" : inputinit.getClass().getName()));
		    break;

		default:
		    VM.abort(INTERNALERROR); // should not happen
		    break;
		}

		filters = data.getInt(N_FILTERS, 0);
		cipher = getCipher(READ);
		if ((filters&LINEDHEXCODER) == LINEDHEXCODER)
		    inStream = new YoixCoderInputStream(inStream, LINEDHEXCODER);
		if ((filters&HEXCODER) == HEXCODER)
		    inStream = new YoixCoderInputStream(inStream, HEXCODER);
		if ((filters&MIMECODER) == MIMECODER)
		    inStream = new YoixCoderInputStream(inStream, MIMECODER);
		if ((filters&IETFCODER) == IETFCODER)
		    inStream = new YoixCoderInputStream(inStream, IETFCODER);
		if (cipher != null)
		    inStream = new CipherInputStream(inStream, cipher);
		if ((filters&GZIP) == GZIP)
		    inStream = new GZIPInputStream(inStream);
		if ((filters&ZIPPED) == ZIPPED)
		    inStream = izip = new ZipInputStream(inStream);
		if ((filters&CHECKSUM) == CHECKSUM)
		    inStream = new CheckedInputStream(inStream, iChecksum = new YoixChecksum(filters));
		istream = new YoixDataInputStream(inStream);
		setPermissions(permissions_usage, 5);
	    }
	    catch(Exception e) {
		stream = null;
		istream = null;
		VM.caughtException(e, true);
	    }
	    finally {
		inputinit = null;
		btc = new YoixConverterInput(encoding, bufsize);
		buffer = new byte[bufsize];
	    }
	}
    }


    private synchronized void
    doOutputInit() {

	OutputStream  outStream = null;
	Cipher        cipher;
	int           filters;
	String        encoding;
	int           bufsize;
	byte          bytes[];

	if (outputinit != null && ostream == null) {
	    encoding = data.getString(N_ENCODING);
	    bufsize = data.getInt(N_BUFSIZE, BUFSIZ);
	    try {
		switch (getMinor()) {
		case URL:
		    if (outputinit instanceof OutputStream)
			outStream = (OutputStream)outputinit;
		    else throw new RuntimeException("bad outputinit URL value, class=" + (outputinit == null ? "null" : outputinit.getClass().getName()));
		    break;

		case FILE:
		    if (outputinit instanceof OutputStream)
			outStream = (OutputStream)outputinit;
		    else throw new RuntimeException("bad outputinit FILE value, class=" + (outputinit == null ? "null" : outputinit.getClass().getName()));
		    break;

	        case STRINGSTREAM:
		    if (outputinit instanceof ByteArrayOutputStream)
			outStream = (ByteArrayOutputStream)outputinit;
		    else throw new RuntimeException("bad outputinit STRING value, class=" + (outputinit == null ? "null" : outputinit.getClass().getName()));
		    break;

		default:
		    VM.abort(INTERNALERROR); // should not happen
		    break;
		}

		filters = data.getInt(N_FILTERS, 0);
		cipher = getCipher(WRITE);
		if ((filters&LINEDHEXCODER) == LINEDHEXCODER)
		    outStream = new YoixCoderOutputStream(outStream, LINEDHEXCODER, encoding);
		if ((filters&HEXCODER) == HEXCODER)
		    outStream = new YoixCoderOutputStream(outStream, HEXCODER);
		if ((filters&MIMECODER) == MIMECODER)
		    outStream = new YoixCoderOutputStream(outStream, MIMECODER);
		if ((filters&IETFCODER) == IETFCODER)
		    outStream = new YoixCoderOutputStream(outStream, IETFCODER);
		if (cipher != null)
		    outStream = new CipherOutputStream(outStream, cipher);
		if ((filters&GZIP) == GZIP)
		    outStream = new GZIPOutputStream(outStream);
		if ((filters&ZIPPED) == ZIPPED)
		    outStream = ozip = new ZipOutputStream(outStream);
		if ((filters&CHECKSUM) == CHECKSUM)
		    outStream = new CheckedOutputStream(outStream, oChecksum = new YoixChecksum(filters));
		ostream = new YoixDataOutputStream(outStream);
		setPermissions(permissions_usage, 5);
	    }
	    catch(Exception e) {
		stream = null;
		ostream = null;
		VM.caughtException(e, true);
	    }
	    finally {
		outputinit = null;
		ctb = new YoixConverterOutput(encoding, bufsize);
		buffer = new byte[bufsize];
		if (ostream != null)
		    openstreams.put(ostream, ctb);
	    }
	}
    }


    private static void
    endBlockedOperation() {

	endBlockedOperation(Thread.currentThread());
    }


    private static void
    endBlockedOperation(Thread t) {

	blockedthreads.remove(t);
    }


    private Object
    getChars(int requested, boolean asString) {

	return(getChars(requested, asString, -1, null));
    }


    private Object
    getChars(int requested, boolean asString, int eol_flag, boolean eol_info[]) {

	Object  result = null;
	char    retbuf[];
	char    tmpbuf[];
	int     actual;

	if (inputinit != null)
	    doInputInit();

	tmpbuf = new char[requested];
	if ((actual = getChars(tmpbuf, 0, requested, eol_flag, eol_info)) == YOIX_EOF)
	    return(result);

	if (asString) {
	    if (actual > 0)
		result = new String(tmpbuf, 0, actual);
	    else result = "";
	} else {
	    retbuf = new char[actual];
	    if (actual > 0)
		System.arraycopy(tmpbuf, 0, retbuf, 0, actual);
	    result = retbuf;
	}

	return(result);
    }


    private int
    getChars(char buf[], int offset, int requested) {

	return(getChars(buf, offset, requested, -1, null));
    }


    private int
    getChars(char buf[], int offset, int requested, int eol_flag, boolean eol_info[]) {

	boolean  done = false;
	boolean  check_eol = false;
	boolean  found_eol = false;
	boolean  nlinit = false;
	boolean  crinit = false;
	boolean  check_crnl = false;
	boolean  check_cr = false;
	Thread   thread = Thread.currentThread();
	int      total = 0;
	int      increment;
	int      count = 0;
	int      attempts = 0;
	int      ch;
	int      n;
	int      avail;

	if (inputinit != null)
	    doInputInit();

	if (eol_flag >= 0) {
	    eol_flag &= EOL_MASK;
	    if (eol_flag == 0)
		eol_flag = CR_EOL|NL_EOL|CR_NL_EOL;
	    nlinit = ((eol_flag&NL_EOL) != 0);
	    crinit = ((eol_flag&(CR_EOL|CR_NL_EOL)) != 0);
	    check_eol = nlinit || crinit;
	    check_crnl = ((eol_flag&CR_NL_EOL) != 0);
	    check_cr = ((eol_flag&CR_EOL) != 0);
	    eol_info[0] = false;
	    eol_info[1] = false;
	}

	if (istream != null) {
	    if (forceReset || istream.bufferResetNeeded()) {
		flush();
		btc.charStart = btc.charEnd;
		forceReset = enforceReset;
	    }
	    while (total < requested && !done) {
		if (btc.charStart < btc.charEnd) {
		    if ((btc.charStart + (requested-total)) <= btc.charEnd)
			increment = requested - total;
		    else increment = btc.charEnd - btc.charStart;
		    if (check_eol) {
			for (n=0; n<increment; n++) {
			    if (nlinit && btc.charBuf[btc.charStart+n] == '\n') {
				eol_info[0] = found_eol = true;
				increment = n + 1;
				break;
			    } else if (crinit && btc.charBuf[btc.charStart+n] == '\r') {
				if (check_crnl && n < (increment-1) && btc.charBuf[btc.charStart+n+1] == '\n') {
				    eol_info[0] = found_eol = true;
				    eol_info[1] = true;
				    increment = n + 2;
				    break;
				} else if (check_cr) {
				    eol_info[0] = found_eol = true;
				    increment = n + 1;
				    break;
				}
			    }
			}
		    }
		    System.arraycopy(btc.charBuf, btc.charStart, buf, offset + total, increment);
		    total += increment;
		    btc.charStart += increment;
		    done = (total == requested) || found_eol;
		}
		while (btc.charStart >= btc.charEnd && !done && count >= 0) {
		    try {
			avail = buffer.length;
			if (avail > 0 || (avail = istream.available()) > 0) {
			    if (thread.isInterrupted() == false) {
				try {
				    beginBlockedOperation(thread, istream);
				    count = istream.read(buffer, 0, Math.min(buffer.length, avail));
				    avail -= count;
				}
				catch(InterruptedIOException e) {
				    //
				    // Without this timeouts during socket reads
				    // end up calling caughtException() which can
				    // end the current thread's run function.
				    //
				    interrupted++;
				    total = YOIX_EOF;
				    done = true;
				    break;
				}
				finally {
				    endBlockedOperation(thread);
				}
				if (count < 0) {
				    if (btc.flush() <= 0) {
					if (total == 0) {
					    total = YOIX_EOF;
					}
					done = true;
				    }
				    break;
				} else btc.convert(buffer, 0, count);
				if (thread.isInterrupted())
				    throw(new InterruptedIOException());
				if (readtrigger != null)
				    readtrigger.update(count);
			    } else throw(new InterruptedIOException());
			} else {
			    if ((ch = getChar(false)) == YOIX_EOF) {
				if (btc.flush() <= 0)
				    done = true;
				count = -1;
			    } else ungetChar(ch);
			}
		    }
		    catch(IOException e) {
			VM.caughtException(e, true);
			total = YOIX_EOF;
			break;
		    }
		}
	    }
	    if (total > 0)
		chars_read += (long)total;
	} else total = YOIX_EOF;

	if (eoftrigger != null && (total == YOIX_EOF || count < 0))
	    eoftrigger.wrapup(chars_read);

	return(total);
    }


    private Cipher
    getCipher(int mode) {

	YoixObject  yobj;
	Cipher      cipher = null;

	//
	// Mode should only be READ or WRITE.
	//

	yobj = data.getObject(N_CIPHER);
	if (yobj != null && yobj.notNull()) {
	    if (yobj.isArray() || yobj.isDictionary()) {
		// use makeCipher built-in to construct cipher
		if ((yobj = YoixModuleSecure.makeCipher(new YoixObject[] { yobj })) != null && yobj.notNull()) {
		    ((YoixBodyCipher)(yobj.body())).setCipherMode(mode == READ ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE);
		    cipher = (Cipher)yobj.getManagedObject();
		} else VM.abort(BADVALUE, N_CIPHER);
	    } else if (yobj.isCipher()) {
		((YoixBodyCipher)(yobj.body())).setCipherMode(mode == READ ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE);
		cipher = (Cipher)yobj.getManagedObject();
	    } else VM.abort(BADVALUE, N_CIPHER);
	}

	return(cipher);
    }


    private synchronized YoixObject
    getConnectTimeout(YoixObject obj) {

	if (stream instanceof URLConnection)
	    obj = YoixObject.newDouble(((URLConnection)stream).getConnectTimeout()/1000);
	return(obj);
    }


    private synchronized YoixObject
    getIfModifiedSince(YoixObject obj) {

	if (stream instanceof URLConnection)
	    obj = YoixObject.newDouble(((URLConnection)stream).getIfModifiedSince()/1000);
	else obj = YoixObject.newDouble(Math.max(0, requestifmodifiedsince/1000));
	return(obj);
    }


    private synchronized YoixObject
    getName(YoixObject obj) {

	if (stream instanceof ByteArrayOutputStream) {
	    flush();
	    try {
		obj = YoixObject.newString(((ByteArrayOutputStream)stream).toString(data.getString(N_ENCODING)));
	    }
	    catch(UnsupportedEncodingException e) {
		obj = YoixObject.newString(((ByteArrayOutputStream)stream).toString());
	    }
	}
	return(obj);
    }


    private synchronized YoixObject
    getReadTimeout(YoixObject obj) {

	if (stream instanceof URLConnection)
	    obj = YoixObject.newDouble(((URLConnection)stream).getReadTimeout()/1000);
	return(obj);
    }


    private YoixObject
    getRequestHeader(YoixObject obj) {

	//
	// Intentionally returns the value defined in the data dictionary
	// when requestheader is null.
	//

	if (requestheader != null)
	    obj = YoixMisc.copyIntoDictionary(requestheader);
	return(obj);
    }


    private synchronized YoixObject
    getRequestMethod(YoixObject obj) {

	if (stream instanceof HttpURLConnection)
	    obj = YoixObject.newString(((HttpURLConnection)stream).getRequestMethod());
	return(obj);
    }


    private YoixObject
    getResponseCode(YoixObject obj) {

	if (inputinit != null)
	    doInputInit();

	if (istream != null && stream instanceof HttpURLConnection)
	    recordResponseCode((HttpURLConnection)stream);
	return(YoixObject.newInt(responsecode));
    }


    private YoixObject
    getResponseError(YoixObject obj) {

	if (stream instanceof HttpURLConnection)
	    recordResponseError((HttpURLConnection)stream);
	return(YoixObject.newString(responseerror));
    }


    private YoixObject
    getResponseHeader(YoixObject obj) {

	Iterator  keys;
	HashMap   copy;
	String    key;
	Object    value;
	Map       map = null;
	int       keycase;

	if (inputinit != null)
	    doInputInit();

	if (istream != null && stream instanceof URLConnection) {
	    recordResponseHeader(stream);
	    if ((map = responseheader) != null) {
		keycase = data.getInt(N_RESPONSEKEYCASE, 0);
		copy = new HashMap();
		for (keys = map.keySet().iterator(); keys.hasNext(); ) {
		    key = (String)keys.next();
		    value = map.get(key);
		    if (key != null) {
			if (keycase != 0)
			    key = (keycase < 0) ? key.toLowerCase() : key.toUpperCase();
		    } else key = "";
		    copy.put(key, value);
		}
		map = copy;
	    }
	}
	return(map != null ? YoixMisc.copyIntoDictionary(map) : YoixObject.newDictionary());
    }


    private YoixObject
    getURLField(int field, YoixObject obj) {

	YoixObject  name;
	int         port;
	URL         url;

	if (isURL()) {
	    try {
		if ((name = data.getObject(N_FULLNAME, null)) == null || name.isNull())
		    name = data.getObject(N_NAME, null);
		url = new URL(name.stringValue());
		switch (field) {
		    case V_FILE:
			obj = YoixObject.newString(YoixMisc.toYoixURL(url.getFile()));
			break;

		    case V_HOST:
			obj = YoixObject.newString(url.getHost());
			break;

		    case V_PORT:
			if ((port = url.getPort()) == -1)
			    port = url.getDefaultPort();
			obj = YoixObject.newInt(port);
			break;

		    case V_PROTOCOL:
			obj = YoixObject.newString(url.getProtocol());
			break;
		}
	    }
	    catch(Exception e) {
		VM.caughtException(e);
	    }
	}

	return(obj);
    }


    private synchronized YoixObject
    getUseCaches(YoixObject obj) {

	if (stream instanceof URLConnection)
	    obj = YoixObject.newInt(((URLConnection)stream).getUseCaches());
	return(obj);
    }


    private YoixObject
    getUsingProxy(YoixObject obj) {

	if (stream instanceof HttpURLConnection)
	    obj = YoixObject.newInt(((HttpURLConnection)stream).usingProxy());
	return(obj);
    }


    private ZipEntry
    getZipEntry() {

	ZipEntry  ze = null;

	if (inputinit != null)
	    doInputInit();

	if (izip != null) {
	    accessDataInputStream();
	    try {
		if (inEntry)
		    izip.closeEntry();
		ze = izip.getNextEntry();
		inEntry = true;
	    }
	    catch(ZipException e) {
		ze = null;
		VM.caughtException(e);
	    }
	    catch(IOException e) {
		ze = null;
		VM.caughtException(e);
	    }
	    finally {
		releaseDataInputStream();
	    }
	}

	return(ze);
    }


    private boolean
    openFile(YoixObject name, int mode) {

	RandomAccessFile  rafile;
	OutputStream      outputStream;
	InputStream       inputStream;
	Object            file;
	Cipher            cipher;
	int               filters;
	String            encoding;
	int               bufsize;

	if (stream == null) {
	    try {
		file = new File(fullname = name.stringValue());
		fullname = YoixMisc.toYoixPath(fullname);
		switch (mode&(READ|WRITE)) {
		    case READ|WRITE:
			encoding = data.getString(N_ENCODING);
			bufsize = data.getInt(N_BUFSIZE, BUFSIZ);
			if (data.getInt(N_FILTERS, 0) != 0)
			    VM.abort(BADVALUE, N_FILTERS);
			if (data.getObject(N_CIPHER).notNull())
			    VM.abort(BADVALUE, N_CIPHER);
			if ((mode&(APPEND|UPDATE)) == 0)	// truncate or create
			    (new FileOutputStream(fullname)).close();
			rafile = new RandomAccessFile(fullname, "rw");
			rafile.seek((mode&APPEND) != 0 ? rafile.length() : 0);
			istream = new YoixDataInputStream(rafile);
			ostream = new YoixDataOutputStream(rafile, istream);
			btc = new YoixConverterInput(encoding, bufsize);
			ctb = new YoixConverterOutput(encoding, bufsize);
			file = rafile;
			inputinit = outputinit = null; // for readability
			setPermissions(permissions_usage, 5);
			openstreams.put(ostream, ctb);
			buffer = new byte[data.getInt(N_BUFSIZE, 1)];
			break;

		    case READ:
			inputinit = new FileInputStream(fullname);
			break;

		    case WRITE:
			outputinit = new FileOutputStream(fullname, ((mode&APPEND) != 0));
			break;

		    default:
			file = null;
			break;
		}
		stream = file;
	    }
	    catch(FileNotFoundException e) {
		stream = null;		// should be unnecessary
		fullname = null;
		VM.caughtException(e, true);
	    }
	    catch(IOException e) {
		stream = null;		// should be unnecessary
		fullname = null;
		VM.caughtException(e, true);
	    }
	} else if (stream instanceof InputStream) {
	    fullname = name.stringValue();
	    inputinit = stream;
	    // special case for installer
	    if (stream instanceof ZipInputStream) {
		if (data.getInt(N_FILTERS, 0) != 0)
		    VM.abort(INTERNALERROR);
		izip = (ZipInputStream)stream;
		doInputInit();
		data.putInt(N_FILTERS, ZIPPED);
	    }
	} else if (stream instanceof OutputStream) {
	    fullname = name.stringValue();
	    outputinit = stream;
	}

	if (stream != null)
	    data.forcePutObject(N_FULLNAME, YoixObject.newString(fullname));
	else fullname = null;

	return(stream != null);
    }


    private boolean
    openString(YoixObject name, int mode) {

	OutputStream  outputStream;
	InputStream   inputStream;
	Cipher        cipher;
	byte          bytes[];
	int           filters;
	int           bufsize;

	if (stream == null) {
	    try {
		fullname = "--string--";
		switch (mode&(READ|WRITE)) {
		    case READ|WRITE:
			// perhaps TODO someday?
			stream = null;
			break;

		    case READ:
			data.put(N_NAME, YoixObject.newString("--string--"));
			stream = name.body();
			if (stream != null)
			    inputinit = name.stringValue();
			else VM.abort(INTERNALERROR);
			break;

		    case WRITE:
			data.put(N_NAME, YoixObject.newString("--string--"));
			bufsize = data.getInt(N_BUFSIZE, BUFSIZ);
			stream = outputinit = new ByteArrayOutputStream(bufsize);
			break;

		    default:
			stream = null;
			break;
		}
	    }
	    catch(Exception e) {
		stream = null;
		fullname = null;
		VM.caughtException(e, true);
	    }
	}

	if (stream != null)
	    data.forcePutObject(N_FULLNAME, YoixObject.newString(fullname));
	else fullname = null;

	return(stream != null);
    }


    private boolean
    openURL(YoixObject name, int mode) {

	URLConnection  connection = null;
	YoixObject     proxy;

	if (stream == null) {
	    try {
		if ((proxy = data.getObject(N_PROXY)) != null && proxy.notNull())
		    connection = YoixMisc.getURLConnection(new URL(name.stringValue()), proxy);
		else connection = YoixMisc.getURLConnection(new URL(name.stringValue()));
		connection.setAllowUserInteraction((mode&UPDATE) == UPDATE);
		connection.setDoInput((mode&READ) == READ);
		connection.setDoOutput((mode&WRITE) == WRITE);

		syncConnectTimeout(connection);
		syncReadTimeout(connection);
		syncIfModifiedSince(connection);
		syncUseCaches(connection);
		syncRequestMethod(connection);
		syncRequestHeader(connection);

		fullname = YoixMisc.toYoixURL(connection.getURL().toExternalForm());

		if (connection.getDoInput()) {
		    // cannot open input until output is done, so set
		    // flag so it can be done when actually needed
		    inputinit = connection;
		    //istream = new YoixDataInputStream(connection.getInputStream());
		    //btc = new YoixConverterInput(encoding, bufsize);
		}

		if (connection.getDoOutput()) {
		    outputinit = (new YoixInterruptable(connection)).getOutputStream();
		    flushmode = FLUSHWRITES; // not necessary, I think
		}
		stream = connection;
		//
		// Recently (8/18/01) added so the opening of a non-existent
		// URL for reading or writing behaves the same way. Not well
		// tested and needs a careful look - later.
		//
		if (inputinit != null && outputinit == null)
		    inputinit = (new YoixInterruptable(connection)).getInputStream();
	    }
	    catch(Exception e) {
		recordResponse(connection);
		inputinit = outputinit = null;
		stream = null;
		fullname = null;
		VM.caughtException(e, true);
	    }
	}

	if (stream != null)
	    data.forcePutObject(N_FULLNAME, YoixObject.newString(fullname));
	else fullname = null;

	return(stream != null);
    }


    private int
    pickStreamMode() {

	YoixObject  obj;
	int         mode = 0;

	if ((obj = data.getObject(N_MODE)) != null) {
	    if (obj.isString())
		mode = pickStreamMode(obj.stringValue());
	    else if (obj.isInteger())
		mode = obj.intValue()&MODEMASK;
	}
	return(mode);
    }


    private int
    pickStreamMode(String str) {

	String  canonized;
	int     mode = 0;

	if (str != null) {
	    canonized = str.toLowerCase().trim();
	    if (canonized.length() > 0) {
		if (canonized.equals("r"))
		    mode = READ;
		else if (canonized.equals("w"))
		    mode = WRITE;
		else if (canonized.equals("a"))
		    mode = (WRITE|APPEND);
		else if (canonized.equals("r+"))
		    mode = (READ|WRITE|UPDATE);
		else if (canonized.equals("w+"))
		    mode = (READ|WRITE);
		else if (canonized.equals("a+"))
		    mode = (READ|WRITE|APPEND);
	    }
	}
	return(mode);
    }


    private int
    putChar(int ch) {

	int  nc = YOIX_EOF;
	int  nb;

	if (outputinit != null)
	    doOutputInit();

	if (ostream != null) {
	    if (ozip != null && !inEntry)
		VM.abort(EXCEPTION, T_ZIPENTRY);
	    nc = 0;
	    // state is such that we can always fit one char into buffer
	    ctb.charBuf[ctb.nextCharIdx++] = (char)ch;
	    nc++;
	    if (ctb.nextCharIdx == ctb.charEnd) {
		if ((nb = ctb.convert()) > 0) {
		    try {
			ostream.write(ctb.byteBuf, ctb.nextByteIdx, nb);
			ctb.nextByteIdx += nb;
		    }
		    catch(IOException e) {
			nc = YOIX_EOF;
			VM.caughtException(e, true);
		    }
		    if (writetrigger != null)
			writetrigger.update(nb);
		}
	    }
	}

	if (nc > 0)			// what if nc == YOIX_EOF
	    chars_written += nc;
	return(nc);
    }


    private int
    putChars(char output[], int off, int requested) {

	int     nc = YOIX_EOF;
	int     available;
	int     nb;
	int     end;

	if (outputinit != null)
	    doOutputInit();

	if (ostream != null) {
	    nc = 0;
	    end = off + requested;
	    if (ozip != null && !inEntry)
		VM.abort(EXCEPTION, T_ZIPENTRY);
	    while (off < end) {
		available = ctb.charEnd - ctb.nextCharIdx;
		if (requested <= available)
		    available = requested;
		System.arraycopy(output, off, ctb.charBuf, ctb.nextCharIdx, available);
		ctb.nextCharIdx += available;
		off += available;
		nc += available;
		requested -= available;
		if (ctb.nextCharIdx == ctb.charEnd) {
		    if ((nb = ctb.convert()) > 0) {
			try {
			    ostream.write(ctb.byteBuf, ctb.nextByteIdx, nb);
			    ctb.nextByteIdx += nb;
			}
			catch(IOException e) {
			    VM.caughtException(e, true);
			    return(YOIX_EOF);
			}
			if (writetrigger != null)
			    writetrigger.update(nb);
		    }
		}
	    }
	}

	chars_written += nc;		// can nc be negative??
	return(nc);
    }


    private void
    putZipEntry(YoixObject obj) {

	ZipEntry  ze;

	if (outputinit != null)
	    doOutputInit();

	if (ozip != null) {
	    if ((ze = (ZipEntry)obj.getManagedObject()) != null) {
		try {
		    flush();
		    if (inEntry)
			ozip.closeEntry();
		    ozip.putNextEntry(ze);
		    inEntry = true;
		}
		catch(Exception e) {
		    VM.caughtException(e);
		    // message is quite informative, we should use it
		    VM.abort(EXCEPTION, T_ZIPENTRY + ": " + e.getMessage());
		}
	    } else VM.abort(BADVALUE, new String[] {T_ZIPENTRY + ".name"});
	} else VM.abort(INVALIDACCESS);		// don't like this error!!!
    }


    private void
    recordResponse(Object connection) {

	//
	// Used to record response information when we close a URL or hit
	// an IOException when we try to get the input stream associated
	// with a URL.
	//

	if (connection instanceof URLConnection) {
	    recordResponseError(connection);
	    recordResponseHeader(connection);
	    recordResponseCode(connection);
	}
    }


    private synchronized void
    recordResponseCode(Object connection) {

	if (connection instanceof HttpURLConnection) {
	    if (responsecode == -1) {
		try {
		    responsecode = ((HttpURLConnection)connection).getResponseCode();
		}
		catch(IOException e) {}
	    }
	}
    }


    private synchronized void
    recordResponseError(Object connection) {

	InputStreamReader  reader;
	StringBuffer       sbuf;
	InputStream        errorstream;
	char               buf[];
	int                limit;
	int                total;
	int                count;

	//
	// Reads the error stream, if there is one, and saves the response
	// in responseerror, where it can be accessed when a Yoix script
	// asks for it. According to Sun, clearing the error stream also
	// helps with persistent connections, although apparently Java 1.6
	// does read a large chunk from the error stream in an effort to
	// automatically clear the connection so it can be reused. Take a
	// look at
	//
	//    http://java.sun.com/javase/6/docs/technotes/guides/net/http-keepalive.html
	//
	// for more information.
	//
	// NOTE - we're not using the "fancy" block buffering to read data
	// from the error stream because we don't expect there's much to be
	// read. As a precaution we stop appending bytes to the error string
	// once we reach a limit that seems reasonable, but we continue to
	// read the error stream to clear the connection. It's a small kludge
	// that we could address without too much trouble if it ever causes
	// problems.
	//

	if (connection instanceof HttpURLConnection) {
	    if (responseerror == null) {
		if ((errorstream = ((HttpURLConnection)connection).getErrorStream()) != null) {
		    try {
			reader = new InputStreamReader(errorstream);
			buf = new char[BUFSIZ];
			sbuf = new StringBuffer(4*buf.length);
			limit = Math.min(32*buf.length, 256*1024);
			for (total = 0; (count = reader.read(buf)) > 0; total += count) {
			    if (total < limit)
				sbuf.append(buf, 0, count);
			}
			responseerror = sbuf.toString();
		    }
		    catch(IOException e) {}
		}
	    }
	}
    }


    private synchronized void
    recordResponseHeader(Object connection) {

	if (connection instanceof URLConnection) {
	    if (responseheader == null)
		responseheader = ((URLConnection)connection).getHeaderFields();
	}
    }


    private void
    setIfModifiedSince(YoixObject obj) {

	long  millis = 0;

	if (obj.isString() || obj.isNumber() || obj.isNull()) {
	    if (obj.isString()) {
		try {
		    //
		    // We realize this is deprecated, but it's still used
		    // to parse date strings in Java's URLConnection and
		    // and HttpURLConnection classes, so we don't feel too
		    // bad about using it here (even though the date may
		    // not be in GMT).
		    //
		    millis = Date.parse(obj.stringValue());
		}
		catch(Exception e) {}
	    } else if (obj.isNumber())
		millis = (long)(1000*obj.doubleValue());
	    requestifmodifiedsince = Math.max(millis, 0);
	} else VM.abort(TYPECHECK, N_IFMODIFIEDSINCE);
    }


    private void
    setMode(YoixObject obj) {

	if (obj.isString() == false && obj.isInteger() == false)
	    VM.abort(TYPECHECK, N_MODE);
    }


    private void
    syncConnectTimeout(URLConnection connection) {

	YoixObject  obj;
	double      timeout;

	if (connection != null) {
	    if ((obj = data.getObject(N_CONNECTTIMEOUT)) != null) {
		if ((timeout = obj.doubleValue()) >= 0)
		    connection.setConnectTimeout((int)Math.max(1000*timeout, 0));
	    }
	}
    }


    private void
    syncIfModifiedSince(URLConnection connection) {

	long  millis;

	if (connection != null) {
	    if ((millis = requestifmodifiedsince) > 0)
		connection.setIfModifiedSince(millis);
	}
    }


    private void
    syncReadTimeout(URLConnection connection) {

	YoixObject  obj;
	double      timeout;

	if (connection instanceof URLConnection) {
	    if ((obj = data.getObject(N_READTIMEOUT)) != null) {
		if ((timeout = obj.doubleValue()) >= 0)
		    connection.setReadTimeout((int)Math.max(1000*timeout, 0));
	    }
	}
    }


    private void
    syncRequestHeader(URLConnection connection) {

	YoixObject  obj;
	YoixObject  dict;
	YoixObject  properties;
	YoixObject  key;
	YoixObject  value;
	String      useragent;
	String      name;
	int         n;

	if (connection != null) {
	    useragent = "Yoix/" + YOIXVERSION;
	    if ((obj = data.getObject(N_REQUESTHEADER)) != null && obj.notNull()) {
		if (obj.isDictionary()) {
		    for (n = 0; n < obj.length(); n++) {
			if ((name = obj.name(n)) != null) {
			    if ((value = obj.getObject(n)) != null && value.isString() && value.notNull()) {
				if (name.equals(USERAGENT) == false)
				    ((URLConnection)connection).setRequestProperty(name, value.stringValue());
				else useragent = value.stringValue();
			    }
			}
		    }
		} else if (obj.isArray()) {
		    for (n = obj.offset(); n < obj.sizeof() - 1; n += 2) {
			if ((key = obj.getObject(n)) != null && key.isString() && key.notNull()) {
			    if ((value = obj.getObject(n+1)) != null && value.isString() && value.notNull()) {
				name = key.stringValue();
				if (name.length() > 0) {
				    if (name.equals(USERAGENT) == false)
					((URLConnection)connection).setRequestProperty(name, value.stringValue());
				    else useragent = value.stringValue();
				}
			    }
			}
		    }
		}
	    }
	    ((URLConnection)connection).setRequestProperty(USERAGENT, useragent);
	    requestheader = ((URLConnection)connection).getRequestProperties();
	}
    }


    private void
    syncRequestMethod(URLConnection connection) {

	YoixObject  obj;

	if (connection instanceof HttpURLConnection) {
	    if ((obj = data.getObject(N_REQUESTMETHOD)) != null && obj.notNull()) {
		try {
		    ((HttpURLConnection)connection).setRequestMethod(obj.stringValue().toUpperCase());
		}
		catch(ProtocolException e) {}
	    }
	}
    }


    private void
    syncUseCaches(URLConnection connection) {

	if (connection instanceof URLConnection)
	    connection.setUseCaches(data.getBoolean(N_USECACHES, false));
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixStreamTrigger {

	YoixObject  args[] = null;
	long        counter = 0;
	int         mode;
	int         size = 0;
	int         trigger = 0;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixStreamTrigger(int mode, int size, YoixObject funct) {

	    this.mode = mode;
	    this.size = size;
	    this.args = new YoixObject[2];
	    this.args[0] = funct;
	    this.args[1] = null;
	}


	YoixStreamTrigger(int mode, long init, YoixObject funct) {

	    this.mode = mode;
	    this.counter = init;
	    this.args = new YoixObject[2];
	    this.args[0] = funct;
	    this.args[1] = null;
	}

	///////////////////////////////////
	//
	// YoixStreamTrigger Methods
	//
	///////////////////////////////////

	final YoixObject
	getFunction() {

	    return(args == null ? YoixObject.newNull() : args[0]);
	}


	final int
	getTriggerSize() {

	    return(size);
	}


	final void
	update(int count) {

	    if (size > 0) {
		counter += count;
		if ((trigger += count) > size) {
		    trigger = trigger % size;
		    args[1] = YoixObject.newDouble(counter);
		    call(args);
		}
		//
		// Putting this here is a brute force approach that seems
		// to get better performance when size if big. Tried it
		// right after the call(), but wasn't all that happy with
		// update performance for big sizes. Not hard to imagine
		// better solutions.
		//
		Thread.yield();
	    }
	}


	final void
	wrapup(long chars_read) {

	    args[1] = YoixObject.newDouble(chars_read - counter);
	    call(args);
	    Thread.yield();
	}
    }
}

