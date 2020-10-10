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
import java.net.*;
import java.io.*;
import java.util.*;

final
class YoixBodyServerSocket extends YoixPointerActive

    implements YoixInterfaceKillable

{

    private ServerSocket  serversocket;

    //
    // Running servers - not really used, so it's probably unnecessary?
    //

    private static WeakHashMap  activeservers = new WeakHashMap();

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY     PREV_OBJ     PREV_BODY
     // -----               ------       ----     --------     ---------
	N_BACKLOG,          $LR__,       null,    null,        null,
	N_LOCALADDRESS,     $LR__,       null,    null,        null,
	N_LOCALPORT,        $LR__,       null,    null,        null,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(10);

    static {
	activefields.put(N_ACCEPT, new Integer(V_ACCEPT));
	activefields.put(N_ALIVE, new Integer(V_ALIVE));
	activefields.put(N_BACKLOG, new Integer(V_BACKLOG));
	activefields.put(N_LOCALADDRESS, new Integer(V_LOCALADDRESS));
	activefields.put(N_LOCALNAME, new Integer(V_LOCALNAME));
	activefields.put(N_LOCALPORT, new Integer(V_LOCALPORT));
	activefields.put(N_RECEIVEBUFFERSIZE, new Integer(V_RECEIVEBUFFERSIZE));
	activefields.put(N_REUSEADDRESS, new Integer(V_REUSEADDRESS));
	activefields.put(N_TIMEOUT, new Integer(V_TIMEOUT));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyServerSocket(YoixObject data) {

	super(data);
	buildServerSocket();
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(SERVERSOCKET);
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
    // YoixBodyServerSocket Methods
    //
    ///////////////////////////////////

    final YoixObject
    callAccept(YoixObject arg[], int offset) {

	return(builtinAccept(N_ACCEPT, arg, offset));
    }


    final boolean
    close() {

	return(socketStop());
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_ACCEPT:
		obj = builtinAccept(name, argv, 0);
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


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_ALIVE:
		obj = getAlive(obj);
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

	    case V_RECEIVEBUFFERSIZE:
		obj = getReceiveBufferSize(obj);
		break;

	    case V_REUSEADDRESS:
		obj = getReuseAddress(obj);
		break;

	    case V_TIMEOUT:
		obj = getTimeout(obj);
		break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(serversocket);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_ALIVE:
		    setAlive(obj);
		    break;

		case V_RECEIVEBUFFERSIZE:
		    setReceiveBufferSize(obj);
		    break;

		case V_REUSEADDRESS:
		    setReuseAddress(obj);
		    break;

		case V_TIMEOUT:
		    setTimeout(obj);
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
    buildServerSocket() {

	serversocket = null;
	setField(N_ALIVE);
    }


    private YoixObject
    builtinAccept(String name, YoixObject arg[], int offset) {

	YoixObject  obj = null;
	YoixObject  dict;
	Socket      client;
	int         argc;

	argc = arg.length - offset;

	if (argc == 0 || argc == 1) {
	    if (argc == 0 || arg[offset].isDictionary()) {
		client = socketAccept();
		if (client != null) {
		    dict = VM.getTypeTemplate(T_SOCKET);
		    if (argc == 1)
			YoixMisc.copyInto(arg[offset], dict);
		    obj = YoixObject.newSocket(dict, client);
		}
	    } else VM.badArgument(name, offset);
	} else VM.badCall(name);

	return(obj != null ? obj : YoixObject.newSocket());
    }


    private synchronized YoixObject
    getAlive(YoixObject obj) {

	return(YoixObject.newInt(serversocket != null));
    }


    private synchronized YoixObject
    getLocalAddress(YoixObject obj) {

	if (serversocket != null)
	    obj = YoixObject.newString(serversocket.getInetAddress().getHostAddress());
	return(obj);
    }


    private synchronized YoixObject
    getLocalName(YoixObject obj) {

	if (serversocket != null)
	    obj = YoixObject.newString(serversocket.getInetAddress().getHostName());
	return(obj);
    }


    private synchronized YoixObject
    getLocalPort(YoixObject obj) {

	if (serversocket != null)
	    obj = YoixObject.newInt(serversocket.getLocalPort());
	return(obj);
    }


    private synchronized YoixObject
    getReceiveBufferSize(YoixObject obj) {

	if (serversocket != null) {
	    try {
		obj = YoixObject.newInt(serversocket.getReceiveBufferSize());
	    }
	    catch(SocketException e) {}
	}
	return(obj);
    }


    private synchronized YoixObject
    getReuseAddress(YoixObject obj) {

	if (serversocket != null)
	    try {
		obj = YoixObject.newInt(serversocket.getReuseAddress());
	    }
	    catch(SocketException e) {}
	return(obj);
    }


    private synchronized YoixObject
    getTimeout(YoixObject obj) {

	if (serversocket != null) {
	    try {
		obj = YoixObject.newDouble(serversocket.getSoTimeout()/1000.0);
	    }
	    catch(IOException e) {}
	}
	return(obj);
    }


    private void
    setAlive(YoixObject obj) {

	if (obj.booleanValue())
	    socketStart();
	else socketStop();
    }


    private synchronized void
    setReceiveBufferSize(YoixObject obj) {

	int  size;

	if (serversocket != null) {
	    if ((size = obj.intValue()) > 0) {
		try {
		    serversocket.setReceiveBufferSize(size);
		}
		catch(SocketException e) {}
	    }
	}
    }


    private synchronized void
    setReuseAddress(YoixObject obj) {

	int  state;

	if (serversocket != null) {
	    if ((state = obj.intValue()) >= 0) {
		try {
		    serversocket.setReuseAddress(state != 0);
		}
		catch(SocketException e) {}
	    }
	}
    }


    private synchronized void
    setTimeout(YoixObject obj) {

	double  timeout;

	if (serversocket != null) {
	    if ((timeout = obj.doubleValue()) >= 0) {
		timeout = Math.max(0, Math.min(timeout, Integer.MAX_VALUE/1000));
		try {
		    serversocket.setSoTimeout((int)(1000*timeout));
		}
		catch(SocketException e) {}
	    }
	}
    }


    private Socket
    socketAccept() {

	Socket  client = null;
	double  timeout;

	//
	// Not synchronized because holding the lock while waiting for
	// a new connection means the server could not be closed. The
	// solution, for now, is to catch NullPointerException, which
	// could conceivably happen if another thread closed the server
	// right before we called serversocket.accept().
	//

	if (socketStart()) {
	    try {
		client = (new YoixInterruptable(serversocket)).accept();
	    }
	    catch(IOException e) {
		VM.caughtException(e);
	    }
	    catch(NullPointerException e) {
		VM.caughtException(e);
	    }
	}
	return(client);
    }


    private synchronized boolean
    socketActivated() {

	if (serversocket != null) {
	    setPermissions(permissions, 5);
	    activeservers.put(this, Boolean.TRUE);
	}
	return(serversocket != null);
    }


    private synchronized boolean
    socketBind(SocketAddress addr, int backlog) {

	if (serversocket != null) {
	    if (!serversocket.isBound()) {
		try {
		    serversocket.bind(addr, backlog);
		}
		catch(IOException e) {}
		catch(IllegalArgumentException e) {}
	    }
	}
	return(serversocket != null && serversocket.isBound());
    }


    private synchronized boolean
    socketSetup() {

	if (serversocket != null) {
	    setField(N_RECEIVEBUFFERSIZE);
	    setField(N_REUSEADDRESS);
	    setField(N_TIMEOUT);
	}
	return(serversocket != null);
    }


    private synchronized boolean
    socketStart() {

	SocketAddress  local;
	String         address;
	String         localaddress;
	int            localport;
	int            backlog;

	if (serversocket == null) {
	    try {
		localaddress = data.getString(N_LOCALADDRESS);
		localport = data.getInt(N_LOCALPORT, 0);
		backlog = data.getInt(N_BACKLOG, 50);
		if (localaddress != null && localaddress.length() > 0) {
		    if ((address = YoixMisc.getInterfaceAddress(localaddress)) != null)
			localaddress = address;
		    local = new InetSocketAddress(localaddress, localport);
		} else local = new InetSocketAddress(localport);
		serversocket = new ServerSocket();
		if (socketSetup()) {
		    if (socketBind(local, backlog))
			socketActivated();
		    else socketStop();
		} else socketStop();
	    }
	    catch(IllegalArgumentException e) {
		socketStop();
		VM.caughtException(e, true);
	    }
	    catch(IOException e) {
		socketStop();
		VM.caughtException(e, true);
	    }
	}
	return(serversocket != null);
    }


    private synchronized boolean
    socketStop() {

	if (serversocket != null) {
	    resetPermissions(permissions, 5);
	    try {
		serversocket.close();
	    }
	    catch(IOException e) {}
	    serversocket = null;
	    activeservers.remove(this);
	}
	return(serversocket == null);
    }
}

