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
import java.util.*;

final
class YoixBodySocket extends YoixPointerActive

    implements YoixInterfaceKillable

{

    private Socket      socket = null;
    private YoixObject  socketinput = null;
    private YoixObject  socketoutput = null;

    //
    // Remember sockets we started, so we can clean up on a graceful
    // exit. May be unnecessary - check all systems first!!
    //

    private static WeakHashMap  activesockets = new WeakHashMap();

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY     PREV_OBJ     PREV_BODY
     // -----               ------       ----     --------     ---------
	N_LOCALADDRESS,     $LR__,       $LR__,   null,        null,
	N_LOCALPORT,        $LR__,       null,    null,        null,
	N_REMOTEADDRESS,    $LR__,       $LR__,   null,        null,
	N_REMOTEPORT,       $LR__,       null,    null,        null,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(25);

    static {
	activefields.put(N_ALIVE, new Integer(V_ALIVE));
	activefields.put(N_INPUT, new Integer(V_INPUT));
	activefields.put(N_INPUTSHUTDOWN, new Integer(V_INPUTSHUTDOWN));
	activefields.put(N_KEEPALIVE, new Integer(V_KEEPALIVE));
	activefields.put(N_LINGER, new Integer(V_LINGER));
	activefields.put(N_LOCALADDRESS, new Integer(V_LOCALADDRESS));
	activefields.put(N_LOCALNAME, new Integer(V_LOCALNAME));
	activefields.put(N_LOCALPORT, new Integer(V_LOCALPORT));
	activefields.put(N_OOBINLINE, new Integer(V_OOBINLINE));
	activefields.put(N_OUTPUT, new Integer(V_OUTPUT));
	activefields.put(N_OUTPUTSHUTDOWN, new Integer(V_OUTPUTSHUTDOWN));
	activefields.put(N_RECEIVEBUFFERSIZE, new Integer(V_RECEIVEBUFFERSIZE));
	activefields.put(N_REMOTEADDRESS, new Integer(V_REMOTEADDRESS));
	activefields.put(N_REMOTENAME, new Integer(V_REMOTENAME));
	activefields.put(N_REMOTEPORT, new Integer(V_REMOTEPORT));
	activefields.put(N_REUSEADDRESS, new Integer(V_REUSEADDRESS));
	activefields.put(N_SENDBUFFERSIZE, new Integer(V_SENDBUFFERSIZE));
	activefields.put(N_SENDURGENTDATA, new Integer(V_SENDURGENTDATA));
	activefields.put(N_TIMEOUT, new Integer(V_TIMEOUT));
	activefields.put(N_TCPNODELAY, new Integer(V_TCPNODELAY));
	activefields.put(N_TRAFFICCLASS, new Integer(V_TRAFFICCLASS));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodySocket(YoixObject data) {

	this(data, null);
    }


    YoixBodySocket(YoixObject data, Socket socket) {

	super(data);

	if (socket != null) {
	    this.socket = socket;
	    if (socketSetup())
		socketActivated();
	    else socketStop();
	} else buildSocket();

	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(SOCKET);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceKillable Methods
    //
    ///////////////////////////////////

    public final void
    kill() {

	socketStop();
    }

    ///////////////////////////////////
    //
    // YoixBodySocket Methods
    //
    ///////////////////////////////////

    protected static void
    atExit() {

	YoixBodySocket  body;
	Iterator        iterator;
	HashMap         active;
	Socket          socket;

	//
	// Decided to close sockets here instead of using socketStop(),
	// so we shouldn't have to worry about deadlock.
	//

	active = new HashMap();
	active.putAll(activesockets);		// clone doesn't work

	for (iterator = active.keySet().iterator(); iterator.hasNext(); ) {
	    try {
		body = (YoixBodySocket)iterator.next();
		if (body.data.getBoolean(N_PERSISTENT) == false) {
		    if ((socket = (Socket)body.getManagedObject()) != null)
			socket.close();
		}
	    }
	    catch(IOException e) {}
	}
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_SENDURGENTDATA:
		obj = builtinSendUrgentData(name, argv, 0);
		break;

	    default:
		obj = null;
		break;
	}

	return(obj);
    }


    protected final void
    finalize() {

	socketStop();
	super.finalize();
    }


    protected final synchronized YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_ALIVE:
		obj = getAlive(obj);
		break;

	    case V_INPUT:
		obj = getInput(obj);
		break;

	    case V_INPUTSHUTDOWN:
		obj = getInputShutdown(obj);
		break;

	    case V_KEEPALIVE:
		obj = getKeepAlive(obj);
		break;

	    case V_LINGER:
		obj = getLinger(obj);
		break;

	    case V_LOCALADDRESS:
		obj = getLocalAddress(obj);
		break;

	    case V_LOCALNAME:
		obj = getLocalName(obj);
		break;

	    case V_LOCALPORT:
		obj = getLocalPort(obj);
		break;

	    case V_OOBINLINE:
		obj = getOOBInline(obj);
		break;

	    case V_OUTPUT:
		obj = getOutput(obj);
		break;

	    case V_OUTPUTSHUTDOWN:
		obj = getOutputShutdown(obj);
		break;

	    case V_RECEIVEBUFFERSIZE:
		obj = getReceiveBufferSize(obj);
		break;

	    case V_REMOTEADDRESS:
		obj = getRemoteAddress(obj);
		break;

	    case V_REMOTENAME:
		obj = getRemoteName(obj);
		break;

	    case V_REMOTEPORT:
		obj = getRemotePort(obj);
		break;

	    case V_REUSEADDRESS:
		obj = getReuseAddress(obj);
		break;

	    case V_SENDBUFFERSIZE:
		obj = getSendBufferSize(obj);
		break;

	    case V_TCPNODELAY:
		obj = getTcpNoDelay(obj);
		break;

	    case V_TIMEOUT:
		obj = getTimeout(obj);
		break;

	    case V_TRAFFICCLASS:
		obj = getTrafficClass(obj);
		break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(socket);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_ALIVE:
		    setAlive(obj);
		    break;

		case V_INPUTSHUTDOWN:
		    setInputShutdown(obj);
		    break;

		case V_KEEPALIVE:
		    setKeepAlive(obj);
		    break;

		case V_LINGER:
		    setLinger(obj);
		    break;

		case V_OOBINLINE:
		    setOOBInline(obj);
		    break;

		case V_OUTPUTSHUTDOWN:
		    setOutputShutdown(obj);
		    break;

		case V_RECEIVEBUFFERSIZE:
		    setReceiveBufferSize(obj);
		    break;

		case V_REUSEADDRESS:
		    setReuseAddress(obj);
		    break;

		case V_SENDBUFFERSIZE:
		    setSendBufferSize(obj);
		    break;

		case V_TCPNODELAY:
		    setTcpNoDelay(obj);
		    break;

		case V_TIMEOUT:
		    setTimeout(obj);
		    break;

		case V_TRAFFICCLASS:
		    setTrafficClass(obj);
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
    buildSocket() {

	socket = null;
	setField(N_ALIVE);
    }


    private YoixObject
    builtinSendUrgentData(String name, YoixObject arg[], int offset) {

	String  text;
	byte    buf[];
	int     count = -1;
	int     argc;
	int     n;

	argc = arg.length - offset;

	if (argc == 1) {
	    if (arg[offset].isInteger() || arg[offset].isString()) {
		if (arg[offset].isString()) {
		    text = arg[offset].stringValue();
		    if (text.length() > 0) {
			buf = YoixMake.javaByteArray(text);
			for (n = 0; n < buf.length; n++) {
			    if (socketSendUrgentData(buf[n]) == 1) {
				if (count == -1)
				    count = 1;
				else count++;
		 	    } else break;
			}
		    } else count = 0;
		} else count = socketSendUrgentData(arg[offset].intValue());
	    } else VM.badArgument(name, offset);
	} else VM.badCall(name);

	return(YoixObject.newInt(count));
    }


    private synchronized YoixObject
    getAlive(YoixObject obj) {

	return(YoixObject.newInt(socket != null));
    }


    private synchronized YoixObject
    getInput(YoixObject obj) {

	if (socket != null) {
	    if (socketinput == null) {
		try {
		    socketinput = newSocketStream(socket.getInputStream());
		}
		catch(IOException e) {}
	    }
	    obj = (socketinput != null) ? socketinput : YoixObject.newStream();
	} else obj = YoixObject.newStream();
	return(obj);
    }


    private synchronized YoixObject
    getInputShutdown(YoixObject obj) {

	return(YoixObject.newInt(socket != null ? socket.isInputShutdown() : true));
    }


    private synchronized YoixObject
    getKeepAlive(YoixObject obj) {

	if (socket != null) {
	    try {
		obj = YoixObject.newInt(socket.getKeepAlive());
	    }
	    catch(SocketException e) {}
	}
	return(obj);
    }


    private synchronized YoixObject
    getLinger(YoixObject obj) {

	int  linger;

	//
	// In this case our time and Java's agree and both are in seconds.
	//

	if (socket != null) {
	    try {
		obj = YoixObject.newDouble(socket.getSoLinger());
	    }
	    catch(SocketException e) {}
	}
	return(obj);
    }


    private synchronized YoixObject
    getLocalAddress(YoixObject obj) {

	if (socket != null)
	    obj = YoixObject.newString(socket.getLocalAddress().getHostAddress());
	return(obj);
    }


    private synchronized YoixObject
    getLocalName(YoixObject obj) {

	if (socket != null)
	    obj = YoixObject.newString(socket.getLocalAddress().getHostName());
	return(obj);
    }


    private synchronized YoixObject
    getLocalPort(YoixObject obj) {

	if (socket != null)
	    obj = YoixObject.newInt(socket.getLocalPort());
	return(obj);
    }


    private synchronized YoixObject
    getOOBInline(YoixObject obj) {

	if (socket != null) {
	    try {
		obj = YoixObject.newInt(socket.getOOBInline());
	    }
	    catch(SocketException e) {}
	}
	return(obj);
    }
 

    private synchronized YoixObject
    getOutput(YoixObject obj) {

	if (socket != null) {
	    if (socketoutput == null) {
		try {
		    socketoutput = newSocketStream(socket.getOutputStream());
		}
		catch(IOException e) {}
	    }
	    obj = (socketoutput != null) ? socketoutput : YoixObject.newStream();
	} else obj = YoixObject.newStream();
	return(obj);
    }


    private synchronized YoixObject
    getOutputShutdown(YoixObject obj) {

	return(YoixObject.newInt(socket != null ? socket.isOutputShutdown() : true));
    }


    private synchronized YoixObject
    getReceiveBufferSize(YoixObject obj) {

	if (socket != null) {
	    try {
		obj = YoixObject.newInt(socket.getReceiveBufferSize());
	    }
	    catch(SocketException e) {}
	}
	return(obj);
    }


    private synchronized YoixObject
    getRemoteAddress(YoixObject obj) {

	SocketAddress  addr;
	InetAddress    inet;
	String         host = null;

	if (socket != null) {
	    if ((addr = socket.getRemoteSocketAddress()) != null) {
		if (addr instanceof InetSocketAddress) {
		    if ((inet = ((InetSocketAddress)addr).getAddress()) != null)
			host = inet.getHostAddress();
		}
	    }
	    obj = YoixObject.newString(host);
	}
	return(obj);
    }


    private synchronized YoixObject
    getRemoteName(YoixObject obj) {

	SocketAddress  addr;
	InetAddress    inet;
	String         name = null;

	if (socket != null) {
	    if ((addr = socket.getRemoteSocketAddress()) != null) {
		if (addr instanceof InetSocketAddress) {
		    if ((inet = ((InetSocketAddress)addr).getAddress()) != null)
			name = inet.getHostName();
		}
	    }
	    obj = YoixObject.newString(name);
	}
	return(obj);
    }


    private synchronized YoixObject
    getRemotePort(YoixObject obj) {

	SocketAddress  addr;
	InetAddress    inet;
	int            port = -1;

	if (socket != null) {
	    if ((addr = socket.getRemoteSocketAddress()) != null) {
		if (addr instanceof InetSocketAddress)
		    port = ((InetSocketAddress)addr).getPort();
	    }
	    obj = YoixObject.newInt(port);
	}
	return(obj);
    }


    private synchronized YoixObject
    getReuseAddress(YoixObject obj) {

	if (socket != null)
	    try {
		obj = YoixObject.newInt(socket.getReuseAddress());
	    }
	    catch(SocketException e) {}
	return(obj);
    }


    private synchronized YoixObject
    getSendBufferSize(YoixObject obj) {

	if (socket != null) {
	    try {
		obj = YoixObject.newInt(socket.getSendBufferSize());
	    }
	    catch(SocketException e) {}
	}
	return(obj);
    }


    private synchronized YoixObject
    getTcpNoDelay(YoixObject obj) {

	if (socket != null) {
	    try {
		obj = YoixObject.newInt(socket.getTcpNoDelay());
	    }
	    catch(SocketException e) {}
	}
	return(obj);
    }


    private synchronized YoixObject
    getTimeout(YoixObject obj) {

	if (socket != null) {
	    try {
		obj = YoixObject.newDouble(socket.getSoTimeout()/1000.0);
	    }
	    catch(SocketException e) {}
	}
	return(obj);
    }


    private synchronized YoixObject
    getTrafficClass(YoixObject obj) {

	if (socket != null) {
	    try {
		obj = YoixObject.newInt(socket.getTrafficClass());
	    }
	    catch(SocketException e) {}
	}
	return(obj);
    }


    private YoixObject
    newSocketStream(Object stream) {

	YoixObject  dict;
	YoixObject  obj;

	//
	// Looks like N_FULLNAME in streams is no longer writable, and as
	// a result this stuff was broken. We deleted the two lines that
	// tried to store values in the N_FULLNAME field on 1/23/07.
	//

	dict = VM.getTypeTemplate(T_FILE);

	if (stream instanceof InputStream) {
	    dict.putString(N_NAME, "--socket:" + N_INPUT + "--");
	    dict.putString(N_MODE, "r");
	    dict.putInt(N_OPEN, true);
	    obj = YoixObject.newStream(dict, (InputStream)stream);
	} else {
	    dict.putString(N_NAME, "--socket:" + N_OUTPUT + "--");
	    dict.putString(N_MODE, "w");
	    dict.putInt(N_OPEN, true);
	    obj = YoixObject.newStream(dict, (OutputStream)stream);
	}
	return(obj);
    }


    private synchronized void
    setAlive(YoixObject obj) {

	if (obj.booleanValue())
	    socketStart();
	else socketStop();
    }


    private synchronized void
    setInputShutdown(YoixObject obj) {

	if (socket != null) {
	    if (obj.booleanValue()) {
		try {
		    socket.shutdownInput();
		}
		catch(IOException e) {}
	    }
	}
    }


    private synchronized void
    setKeepAlive(YoixObject obj) {

	int  state;

	if (socket != null) {
	    if ((state = obj.intValue()) >= 0) {
		try {
		    socket.setKeepAlive(state != 0);
		}
		catch(SocketException e) {}
	    }
	}
    }


    private synchronized void
    setLinger(YoixObject obj) {

	double  seconds;

	if (socket != null) {
	    try {
		seconds = obj.doubleValue();
		if (seconds >= 0)
		    socket.setSoLinger(true, (int)seconds);
		else socket.setSoLinger(false, 0);
	    }
	    catch(SocketException e) {}
	}
    }


    private synchronized void
    setOOBInline(YoixObject obj) {

	int  state;

	if (socket != null) {
	    if ((state = obj.intValue()) >= 0) {
		try {
		    socket.setOOBInline(state != 0);
		}
		catch(SocketException e) {}
	    }
	}
    }


    private synchronized void
    setOutputShutdown(YoixObject obj) {

	if (socket != null) {
	    if (obj.booleanValue()) {
		try {
		    socket.shutdownOutput();
		}
		catch(IOException e) {}
	    }
	}
    }


    private synchronized void
    setReceiveBufferSize(YoixObject obj) {

	int  size;

	if (socket != null) {
	    if ((size = obj.intValue()) > 0) {
		try {
		    socket.setReceiveBufferSize(size);
		}
		catch(SocketException e) {}
	    }
	}
    }


    private synchronized void
    setReuseAddress(YoixObject obj) {

	int  state;

	if (socket != null) {
	    if ((state = obj.intValue()) >= 0) {
		try {
		    socket.setReuseAddress(state != 0);
		}
		catch(SocketException e) {}
	    }
	}
    }


    private synchronized void
    setSendBufferSize(YoixObject obj) {

	int  size;

	if (socket != null) {
	    if ((size = obj.intValue()) > 0) {
		try {
		    socket.setSendBufferSize(size);
		}
		catch(SocketException e) {}
	    }
	}
    }


    private synchronized void
    setTcpNoDelay(YoixObject obj) {

	if (socket != null) {
	    try {
		socket.setTcpNoDelay(obj.booleanValue());
	    }
	    catch(SocketException e) {}
	}
    }


    private synchronized void
    setTimeout(YoixObject obj) {

	double  timeout;

	if (socket != null) {
	    if ((timeout = obj.doubleValue()) >= 0) {
		timeout = Math.max(0, Math.min(timeout, Integer.MAX_VALUE/1000));
		try {
		    socket.setSoTimeout((int)(1000*timeout));
		}
		catch(SocketException e) {}
	    }
	}
    }


    private synchronized void
    setTrafficClass(YoixObject obj) {

	int  flags;

	if (socket != null) {
	    if ((flags = obj.intValue()) >= 0) {
		try {
		    socket.setTrafficClass(flags & 0xFF);
		}
		catch(SocketException e) {
		    try {
			//
			// Try again, but this time with no precedence.
			//
			socket.setTrafficClass(flags & 0x1F);
		    }
		    catch(SocketException ex) {}
		}
		catch(IllegalArgumentException e) {}
	    }
	}
    }


    private synchronized boolean
    socketActivated() {

	if (socket != null) {
	    setPermissions(permissions, 5);
	    activesockets.put(this, Boolean.TRUE);
	}
	return(socket != null);
    }


    private synchronized boolean
    socketBind(SocketAddress addr) {

	if (socket != null) {
	    if (!socket.isBound()) {
		try {
		    socket.bind(addr);
		}
		catch(IOException e) {}
		catch(IllegalArgumentException e) {}
	    }
	}
	return(socket != null && socket.isBound());
    }


    private synchronized boolean
    socketConnect(SocketAddress addr) {

	if (socket != null) {
	    if (!socket.isConnected()) {
		try {
		    socket.connect(addr);
		}
		catch(IOException e) {}
		catch(IllegalArgumentException e) {}
	    }
	}
	return(socket != null && socket.isConnected());
    }


    private int
    socketSendUrgentData(int value) {

	int  count = -1;

	//
	// Intentionally not synchronized to avoid possible deadlocks.
	//

	if (socketStart()) {
	    try {
		socket.sendUrgentData(value);
		count = 1;
	    }
	    catch(IOException e) {
		VM.caughtException(e);
	    }
	    catch(NullPointerException e) {
		VM.caughtException(e);
	    }
	}
	return(count);
    }


    private synchronized boolean
    socketSetup() {

	if (socket != null) {
	    setField(N_KEEPALIVE);
	    setField(N_LINGER);
	    setField(N_OOBINLINE);
	    setField(N_RECEIVEBUFFERSIZE);
	    setField(N_REUSEADDRESS);
	    setField(N_SENDBUFFERSIZE);
	    setField(N_TCPNODELAY);
	    setField(N_TRAFFICCLASS);
	    setField(N_TIMEOUT);
	}
	return(socket != null);
    }


    private synchronized boolean
    socketStart() {

	SocketAddress  local;
	SocketAddress  remote;
	String         address;
	String         localaddress;
	String         remoteaddress;
	int            remoteport;
	int            localport;

	if (socket == null) {
	    localaddress = data.getString(N_LOCALADDRESS);
	    remoteaddress = data.getString(N_REMOTEADDRESS);
	    localport = data.getInt(N_LOCALPORT, 0);
	    remoteport = data.getInt(N_REMOTEPORT, -1);
	    if (remoteaddress != null && remoteaddress.length() > 0) {
		if (remoteport >= 0 && remoteport <= 0xFFFF) {
		    try {
		 	if (localaddress != null && localaddress.length() > 0) {
			    if ((address = YoixMisc.getInterfaceAddress(localaddress)) != null)
				localaddress = address;
			    local = new InetSocketAddress(localaddress, localport);
			} else local = new InetSocketAddress(localport);
			remote = new InetSocketAddress(remoteaddress, remoteport);
			socket = new Socket();
			if (socketSetup()) {
			    if (socketBind(local)) {
				if (socketConnect(remote))
				    socketActivated();
				else socketStop();
			    } else socketStop();
			} else socketStop();
		    }
		    catch(IllegalArgumentException e) {
			socketStop();
			VM.caughtException(e, true);
		    }
		}
	    }
	}
	return(socket != null);
    }


    private synchronized boolean
    socketStop() {

	YoixObject  input;
	YoixObject  output;

	if (socket != null) {
	    resetPermissions(permissions, 5);
	    try {
		socket.close();
	    }
	    catch(IOException e) {}
	    input = socketinput;
	    output = socketoutput;
	    socket = null;
	    socketinput = null;
	    socketoutput = null;
	    activesockets.remove(this);
	    if (input != null)
		input.close();
	    if (output != null)
		output.close();
	}
	return(socket == null);
    }
}

