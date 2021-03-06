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
class YoixBodyMulticastSocket extends YoixPointerActive

    implements YoixInterfaceKillable

{

    //
    // We have had mixed results with MulticastSockets and really haven't
    // had the time to do a thorough job of testing. Linux systems behave
    // poorly, while Windows and Mac seem to do better. The problems that
    // we've run into could easily be mistakes in our code, however there
    // are some long standing MulticastSocket bugs that are summarized in
    // the 4701650 Java bug report that seem to describe the behavior that
    // we've seen (primarily on Linux). We eventually will spend more time
    // looking into the problems.
    //

    private MulticastSocket  multicastsocket;

    //
    // We maintain a list of the joined groups.
    //

    private ArrayList  joinedgroups = new ArrayList();

    //
    // Active servers - currently unused and probably unnecessary??
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
	N_LOCALADDRESS,     $LR__,       null,    null,        null,
	N_LOCALPORT,        $LR__,       null,    null,        null,
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
	activefields.put(N_BROADCAST, new Integer(V_BROADCAST));
	activefields.put(N_JOINGROUP, new Integer(V_JOINGROUP));
	activefields.put(N_JOINEDGROUPS, new Integer(V_JOINEDGROUPS));
	activefields.put(N_LEAVEGROUP, new Integer(V_LEAVEGROUP));
	activefields.put(N_LOCALADDRESS, new Integer(V_LOCALADDRESS));
	activefields.put(N_LOCALNAME, new Integer(V_LOCALNAME));
	activefields.put(N_LOCALPORT, new Integer(V_LOCALPORT));
	activefields.put(N_LOOPBACK, new Integer(V_LOOPBACK));
	activefields.put(N_NETWORKINTERFACE, new Integer(V_NETWORKINTERFACE));
	activefields.put(N_RECEIVE, new Integer(V_RECEIVE));
	activefields.put(N_RECEIVEBUFFERSIZE, new Integer(V_RECEIVEBUFFERSIZE));
	activefields.put(N_REMOTEADDRESS, new Integer(V_REMOTEADDRESS));
	activefields.put(N_REMOTENAME, new Integer(V_REMOTENAME));
	activefields.put(N_REMOTEPORT, new Integer(V_REMOTEPORT));
	activefields.put(N_REUSEADDRESS, new Integer(V_REUSEADDRESS));
	activefields.put(N_SEND, new Integer(V_SEND));
	activefields.put(N_SENDBUFFERSIZE, new Integer(V_SENDBUFFERSIZE));
	activefields.put(N_TIMEOUT, new Integer(V_TIMEOUT));
	activefields.put(N_TIMETOLIVE, new Integer(V_TIMETOLIVE));
	activefields.put(N_TRAFFICCLASS, new Integer(V_TRAFFICCLASS));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyMulticastSocket(YoixObject data) {

	super(data);
	buildMulticastSocket();
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(MULTICASTSOCKET);
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
    // YoixBodyMulticastSocket Methods
    //
    ///////////////////////////////////

    final YoixObject
    callReceive(YoixObject arg[], int offset) {

	return(builtinReceive(N_RECEIVE, arg, offset));
    }


    final YoixObject
    callSend(YoixObject arg[], int offset) {

	return(builtinSend(N_SEND, arg, offset));
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_JOINGROUP:
		obj = builtinJoinGroup(name, argv, 0);
		break;

	    case V_LEAVEGROUP:
		obj = builtinLeaveGroup(name, argv, 0);
		break;

	    case V_RECEIVE:
		obj = builtinReceive(name, argv, 0);
		break;

	    case V_SEND:
		obj = builtinSend(name, argv, 0);
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

	    case V_BROADCAST:
		obj = getBroadcast(obj);
		break;

	    case V_JOINEDGROUPS:
		obj = getJoinedGroups(obj);
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

	    case V_LOOPBACK:
		obj = getLoopback(obj);
		break;

	    case V_NETWORKINTERFACE:
		obj = getNetworkInterface(obj);
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

	    case V_TIMEOUT:
		obj = getTimeout(obj);
		break;

	    case V_TIMETOLIVE:
		obj = getTimeToLive(obj);
		break;

	    case V_TRAFFICCLASS:
		obj = getTrafficClass(obj);
		break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(multicastsocket);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_ALIVE:
		    setAlive(obj);
		    break;

		case V_BROADCAST:
		    setBroadcast(obj);
		    break;

		case V_LOOPBACK:
		    setLoopback(obj);
		    break;

		case V_NETWORKINTERFACE:
		    setNetworkInterface(obj);
		    break;

		case V_RECEIVEBUFFERSIZE:
		    setReceiveBufferSize(obj);
		    break;

		case V_REMOTEADDRESS:
		    setRemoteAddress(obj);
		    break;

		case V_REMOTEPORT:
		    setRemotePort(obj);
		    break;

		case V_REUSEADDRESS:
		    setReuseAddress(obj);
		    break;

		case V_SENDBUFFERSIZE:
		    setSendBufferSize(obj);
		    break;

		case V_TIMEOUT:
		    setTimeout(obj);
		    break;

		case V_TIMETOLIVE:
		    setTimeToLive(obj);
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
    buildMulticastSocket() {

	multicastsocket = null;
	setField(N_ALIVE);
    }


    private YoixObject
    builtinJoinGroup(String name, YoixObject arg[], int offset) {

	boolean  result = false;

	if (arg.length - offset == 1) {
	    if (arg[offset].isString() || arg[offset].isNull())
		result = socketJoin(arg[offset].stringValue());
	    else VM.badArgument(name, offset);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private YoixObject
    builtinLeaveGroup(String name, YoixObject arg[], int offset) {

	boolean  result = false;

	if (arg.length - offset == 1) {
	    if (arg[offset].isString() || arg[offset].isNull())
		result = socketLeave(arg[offset].stringValue());
	    else VM.badArgument(name, offset);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private YoixObject
    builtinReceive(String name, YoixObject arg[], int offset) {

	DatagramPacket  packet;
	String          address;
	byte            buf[];
	int             port;
	int             count = -1;

	if (arg.length - offset == 3) {
	    if (arg[offset].isString() && arg[offset].notNull()) {
		if (arg[offset+1].isStringPointer()) {
		    if (arg[offset+2].isIntegerPointer()) {
			try {
			    buf = new byte[arg[offset].sizeof()];
			    packet = new DatagramPacket(buf, buf.length);
			    count = socketReceive(packet);
			    address = packet.getAddress().getHostAddress();
			    port = packet.getPort();
			    arg[offset+1].put(YoixObject.newString(address));
			    arg[offset+2].put(YoixObject.newInt(packet.getPort()));
			    if (count > 0)
				arg[offset].overlay(YoixMake.javaString(packet.getData()));
			}
			catch(RuntimeException e) {}
		    } else VM.badArgument(name, offset+2);
		} else VM.badArgument(name, offset+1);
	    } else VM.badArgument(name, offset);
	} else VM.badCall(name);

	return(YoixObject.newInt(count));
    }


    private YoixObject
    builtinSend(String name, YoixObject arg[], int offset) {

	DatagramPacket  packet;
	InetAddress     inet;
	String          text;
	byte            buf[];
	int             argc;
	int             port;
	int             count = -1;

	argc = arg.length - offset;

	if (argc == 1 || argc == 3) {
	    if (arg[offset].isString()) {
		if (argc == 1 || arg[offset+1].isString() || arg[offset+1].isNull()) {
		    if (argc == 1 || arg[offset+2].isInteger()) {
			port = (argc > 2) ? arg[offset+2].intValue() : 0;
			if (port >= 0 && port <= 0xFFFF) {
			    text = arg[offset].stringValue();
			    if (text.length() > 0) {
				try {
				    buf = YoixMake.javaByteArray(text);
				    packet = new DatagramPacket(buf, buf.length);
				    if (argc == 3) {
					if (arg[offset+1].notNull())
					    inet = InetAddress.getByName(arg[offset+1].stringValue());
					else inet = InetAddress.getLocalHost();
					packet.setAddress(inet);
					packet.setPort(port);
				    }
				    count = socketSend(packet);
				}
				catch(UnknownHostException e) {}
			    } else count = 0;
			} else VM.badArgumentValue(name, offset+2);
		    } else VM.badArgument(name, offset+2);
		} else VM.badArgument(name, offset+1);
	    } else VM.badArgument(name, offset);
	} else VM.badCall(name);

	return(YoixObject.newInt(count));
    }


    private synchronized YoixObject
    getAlive(YoixObject obj) {

	return(YoixObject.newInt(multicastsocket != null));
    }


    private synchronized YoixObject
    getBroadcast(YoixObject obj) {

	if (multicastsocket != null) {
	    try {
		obj = YoixObject.newInt(multicastsocket.getBroadcast());
	    }
	    catch(SocketException e) {}
	}
	return(obj);
    }


    private synchronized YoixObject
    getJoinedGroups(YoixObject obj) {

	if (joinedgroups.size() > 0)
	    obj = YoixMisc.copyIntoArray(joinedgroups);
	else obj = YoixObject.newArray();
	return(obj);
    }


    private synchronized YoixObject
    getLocalAddress(YoixObject obj) {

	if (multicastsocket != null)
	    obj = YoixObject.newString(multicastsocket.getLocalAddress().getHostAddress());
	return(obj);
    }


    private synchronized YoixObject
    getLocalName(YoixObject obj) {

	if (multicastsocket != null)
	    obj = YoixObject.newString(multicastsocket.getLocalAddress().getHostName());
	return(obj);
    }


    private synchronized YoixObject
    getLocalPort(YoixObject obj) {

	if (multicastsocket != null)
	    obj = YoixObject.newInt(multicastsocket.getLocalPort());
	return(obj);
    }


    private synchronized YoixObject
    getLoopback(YoixObject obj) {

	if (multicastsocket != null) {
	    try {
		//
		// Java apparently returns true if loopback mode is
		// disabled and false if it is enabled, which is why
		// we negate their return value!!!
		//
		obj = YoixObject.newInt(!multicastsocket.getLoopbackMode());
	    }
	    catch(SocketException e) {}
	}
	return(obj);
    }


    private synchronized YoixObject
    getNetworkInterface(YoixObject obj) {

	NetworkInterface  ni = null;

	if (multicastsocket != null) {
	    try {
		ni = multicastsocket.getNetworkInterface();
	    }
	    catch(SocketException e) {}
	    obj = YoixObject.newString(ni != null ? ni.getName() : null);
	}
	return(obj);
    }


    private synchronized YoixObject
    getReceiveBufferSize(YoixObject obj) {

	if (multicastsocket != null) {
	    try {
		obj = YoixObject.newInt(multicastsocket.getReceiveBufferSize());
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

	if (multicastsocket != null) {
	    if ((addr = multicastsocket.getRemoteSocketAddress()) != null) {
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

	if (multicastsocket != null) {
	    if ((addr = multicastsocket.getRemoteSocketAddress()) != null) {
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

	if (multicastsocket != null) {
	    if ((addr = multicastsocket.getRemoteSocketAddress()) != null) {
		if (addr instanceof InetSocketAddress)
		    port = ((InetSocketAddress)addr).getPort();
	    }
	    obj = YoixObject.newInt(port);
	}
	return(obj);
    }


    private synchronized YoixObject
    getReuseAddress(YoixObject obj) {

	if (multicastsocket != null) {
	    try {
		obj = YoixObject.newInt(multicastsocket.getReuseAddress());
	    }
	    catch(SocketException e) {}
	}
	return(obj);
    }


    private synchronized YoixObject
    getSendBufferSize(YoixObject obj) {

	if (multicastsocket != null) {
	    try {
		obj = YoixObject.newInt(multicastsocket.getSendBufferSize());
	    }
	    catch(SocketException e) {}
	}
	return(obj);
    }


    private synchronized YoixObject
    getTimeout(YoixObject obj) {

	if (multicastsocket != null) {
	    try {
		obj = YoixObject.newDouble(multicastsocket.getSoTimeout()/1000.0);
	    }
	    catch(SocketException e) {}
	}
	return(obj);
    }


    private synchronized YoixObject
    getTimeToLive(YoixObject obj) {

	if (multicastsocket != null) {
	    try {
		obj = YoixObject.newInt(multicastsocket.getTimeToLive());
	    }
	    catch(IOException e) {}
	}
	return(obj);
    }


    private synchronized YoixObject
    getTrafficClass(YoixObject obj) {

	if (multicastsocket != null) {
	    try {
		obj = YoixObject.newInt(multicastsocket.getTrafficClass());
	    }
	    catch(SocketException e) {}
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
    setBroadcast(YoixObject obj) {

	int  state;

	if (multicastsocket != null) {
	    if ((state = obj.intValue()) >= 0) {
		try {
		    multicastsocket.setBroadcast(state != 0);
		}
		catch(SocketException e) {}
	    }
	}
    }


    private synchronized void
    setLoopback(YoixObject obj) {

	int  state;

	if (multicastsocket != null) {
	    if ((state = obj.intValue()) >= 0) {
		try {
		    //
		    // Java expects that the argument will be true
		    // if we want to disable loopback mode and false
		    // to enable it!!!!
		    //
		    multicastsocket.setLoopbackMode(state == 0);
		}
		catch(SocketException e) {}
	    }
	}
    }


    private synchronized void
    setNetworkInterface(YoixObject obj) {

	NetworkInterface  ni = null;

	if (multicastsocket != null) {
	    try {
		multicastsocket.setNetworkInterface(NetworkInterface.getByName(obj.stringValue()));
	    }
	    catch(SocketException e) {}
	}
    }


    private synchronized void
    setReceiveBufferSize(YoixObject obj) {

	int  size;

	if (multicastsocket != null) {
	    if ((size = obj.intValue()) > 0) {
		try {
		    multicastsocket.setReceiveBufferSize(size);
		}
		catch(SocketException e) {}
	    }
	}
    }


    private synchronized void
    setRemoteAddress(YoixObject obj) {

	String  remoteaddress;
	int     remoteport;

	if (multicastsocket != null) {
	    if (multicastsocket.isConnected())
		multicastsocket.disconnect();
	    if (obj.notNull()) {
		remoteaddress = obj.stringValue();
		remoteport = data.getInt(N_REMOTEPORT, -1);
		if (remoteaddress != null && remoteaddress.length() > 0) {
		    if (remoteport >= 0 && remoteport <= 0xFFFF)
			socketConnect(new InetSocketAddress(remoteaddress, remoteport));
		}
	    }
	}
    }


    private synchronized void
    setRemotePort(YoixObject obj) {

	String  remoteaddress;
	int     remoteport;

	if (multicastsocket != null) {
	    if (multicastsocket.isConnected())
		multicastsocket.disconnect();
	    remoteaddress = data.getString(N_REMOTEADDRESS);
	    remoteport = obj.intValue();
	    if (remoteaddress != null && remoteaddress.length() > 0) {
		if (remoteport >= 0 && remoteport <= 0xFFFF)
		    socketConnect(new InetSocketAddress(remoteaddress, remoteport));
	    }
	}
    }


    private synchronized void
    setReuseAddress(YoixObject obj) {

	int  state;

	if (multicastsocket != null) {
	    if ((state = obj.intValue()) >= 0) {
		try {
		    multicastsocket.setReuseAddress(state != 0);
		}
		catch(SocketException e) {}
	    }
	}
    }


    private synchronized void
    setSendBufferSize(YoixObject obj) {

	int  size;

	if (multicastsocket != null) {
	    if ((size = obj.intValue()) > 0) {
		try {
		    multicastsocket.setSendBufferSize(size);
		}
		catch(SocketException e) {}
	    }
	}
    }


    private synchronized void
    setTimeout(YoixObject obj) {

	double  timeout;

	if (multicastsocket != null) {
	    if ((timeout = obj.doubleValue()) >= 0) {
		timeout = Math.max(0, Math.min(timeout, Integer.MAX_VALUE/1000));
		try {
		    multicastsocket.setSoTimeout((int)(1000*timeout));
		}
		catch(SocketException e) {}
	    }
	}
    }


    private synchronized void
    setTimeToLive(YoixObject obj) {

	int  ttl;

	if (multicastsocket != null) {
	    if ((ttl = obj.intValue()) >= 0) {
		try {
		    multicastsocket.setTimeToLive(Math.min(ttl, 255));
		}
		catch(IOException e) {}
	    }
	}
    }


    private synchronized void
    setTrafficClass(YoixObject obj) {

	int  flags;

	if (multicastsocket != null) {
	    if ((flags = obj.intValue()) >= 0) {
		try {
		    multicastsocket.setTrafficClass(flags & 0xFF);
		}
		catch(SocketException e) {
		    try {
			//
			// Try again, but this time with no precedence.
			//
			multicastsocket.setTrafficClass(flags & 0x1F);
		    }
		    catch(SocketException ex) {}
		}
		catch(IllegalArgumentException e) {}
	    }
	}
    }


    private synchronized boolean
    socketActivated() {

	if (multicastsocket != null) {
	    setPermissions(permissions, 5);
	    activeservers.put(this, Boolean.TRUE);
	}
	return(multicastsocket != null);
    }


    private synchronized boolean
    socketBind(SocketAddress addr) {

	if (multicastsocket != null) {
	    if (!multicastsocket.isBound()) {
		try {
		    multicastsocket.bind(addr);
		}
		catch(IOException e) {}
		catch(IllegalArgumentException e) {}
	    }
	}
	return(multicastsocket != null && multicastsocket.isBound());
    }


    private synchronized boolean
    socketConnect(SocketAddress addr) {

	if (multicastsocket != null) {
	    if (!multicastsocket.isConnected()) {
		try {
		    multicastsocket.connect(addr);
		}
		catch(IOException e) {}
		catch(IllegalArgumentException e) {}
	    }
	}
	return(multicastsocket != null && multicastsocket.isConnected());
    }


    private synchronized boolean
    socketJoin(String address) {

	if (address != null && address.length() > 0) {
	    if (joinedgroups.contains(address) == false) {
		if (socketStart()) {
		    try {
			multicastsocket.joinGroup(InetAddress.getByName(address));
			joinedgroups.add(address);
		    }
		    catch(IOException e) {
			VM.caughtException(e);
		    }
		    catch(IllegalArgumentException e) {
			VM.caughtException(e);
		    }
		}
	    }
	}
	return(joinedgroups.contains(address));
    }


    private synchronized boolean
    socketLeave(String address) {

	int  index;

	if (address != null && address.length() > 0) {
	    if (joinedgroups.contains(address)) {
		if (socketStart()) {
		    try {
			multicastsocket.leaveGroup(InetAddress.getByName(address));
			while ((index = joinedgroups.indexOf(address)) >= 0)
			    joinedgroups.remove(index);
		    }
		    catch(IOException e) {
			VM.caughtException(e);
		    }
		    catch(IllegalArgumentException e) {
			VM.caughtException(e);
		    }
		}
	    }
	}
	return(!joinedgroups.contains(address));
    }


    private int
    socketReceive(DatagramPacket p) {

	int  count = -1;

	//
	// Intentionally not synchronized to avoid possible deadlocks.
	//

	if (socketStart()) {
	    try {
		(new YoixInterruptable(multicastsocket)).receive(p);
		count = p.getLength();
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


    private int
    socketSend(DatagramPacket p) {

	int  count = -1;

	//
	// Intentionally not synchronized to avoid possible deadlocks.
	//

	if (socketStart()) {
	    try {
		multicastsocket.send(p);
		count = p.getLength();
	    }
	    catch(IOException e) {
		VM.caughtException(e);
	    }
	    catch(NullPointerException e) {
		VM.caughtException(e);
	    }
	    catch(IllegalArgumentException e) {
		VM.caughtException(e);
	    }
	}
	return(count);
    }


    private synchronized boolean
    socketSetup() {

	if (multicastsocket != null) {
	    setField(N_BROADCAST);
	    setField(N_LOOPBACK);
	    setField(N_NETWORKINTERFACE);
	    setField(N_RECEIVEBUFFERSIZE);
	    setField(N_REUSEADDRESS);
	    setField(N_SENDBUFFERSIZE);
	    setField(N_TIMEOUT);
	    setField(N_TIMETOLIVE);
	    setField(N_TRAFFICCLASS);
	}
	return(multicastsocket != null);
    }


    private synchronized boolean
    socketStart() {

	SocketAddress  local;
	SocketAddress  remote;
	String         address;
	String         localaddress;
	String         remoteaddress;
	int            localport;
	int            remoteport;

	if (multicastsocket == null) {
	    try {
		localaddress = data.getString(N_LOCALADDRESS);
		remoteaddress = data.getString(N_REMOTEADDRESS);
		localport = data.getInt(N_LOCALPORT, 0);
		remoteport = data.getInt(N_REMOTEPORT, -1);
		if (localaddress != null && localaddress.length() > 0) {
		    if ((address = YoixMisc.getInterfaceAddress(localaddress)) != null)
			localaddress = address;
		    local = new InetSocketAddress(localaddress, localport);
		} else local = new InetSocketAddress(localport);
		if (remoteaddress != null && remoteaddress.length() > 0) {
		    if (remoteport >= 0 && remoteport <= 0xFFFF)
			remote = new InetSocketAddress(remoteaddress, remoteport);
		    else remote = null;
		} else remote = null;
		multicastsocket = new MulticastSocket((SocketAddress)null);
		joinedgroups.clear();
		if (socketSetup()) {
		    if (socketBind(local)) {
			socketConnect(remote);
			socketActivated();
		    } else socketStop();
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
	return(multicastsocket != null);
    }


    private synchronized boolean
    socketStop() {

	if (multicastsocket != null) {
	    resetPermissions(permissions, 5);
	    multicastsocket.close();
	    multicastsocket = null;
	    joinedgroups.clear();
	    activeservers.remove(this);
	}
	return(multicastsocket == null);
    }
}

