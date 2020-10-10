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
import java.io.IOException;
import java.net.*;
import java.util.*;

abstract
class YoixModuleNet extends YoixModule

{

    //
    // Decided to omit the multicast tests provided by InetAddress, for now
    // anyway. May change if we decide to implement MulticastSocket. We've
    // also had mixed results with MulticastSockets that may be related to
    // the 4701650 Java bug report, so one of the things we did in response
    // to that bug report was remove write permission from N_LOCALADDRESS,
    // which means MulticastSockets only bind to the wildcard address.
    //

    static String  $MODULENAME = M_NET;

    static Integer  $IPV4 = new Integer(IPV4);
    static Integer  $IPV6 = new Integer(IPV6);

    //
    // Flags for setting the traffic class (type of service).
    //

    static Integer  $IPTOS_NORMAL = new Integer(0x00);
    static Integer  $IPTOS_LOWCOST = new Integer(0x02);
    static Integer  $IPTOS_RELIABILITY = new Integer(0x04);
    static Integer  $IPTOS_THROUGHPUT = new Integer(0x08);
    static Integer  $IPTOS_LOWDELAY = new Integer(0x10);
    static Integer  $IPTOS_MASK = new Integer(0x1F);

    static Integer  $IPPREC_0 = new Integer(0x00);
    static Integer  $IPPREC_1 = new Integer(0x20);
    static Integer  $IPPREC_2 = new Integer(0x40);
    static Integer  $IPPREC_3 = new Integer(0x60);
    static Integer  $IPPREC_4 = new Integer(0x80);
    static Integer  $IPPREC_5 = new Integer(0xA0);
    static Integer  $IPPREC_6 = new Integer(0xC0);
    static Integer  $IPPREC_7 = new Integer(0xE0);
    static Integer  $IPPREC_MASK = new Integer(0xE0);

    //
    // CookieManager constants.
    //

    static Integer  $ACCEPT_NONE = new Integer(YoixBodyCookieManager.YOIX_ACCEPT_NONE);
    static Integer  $ACCEPT_ORIGINAL_SERVER = new Integer(YoixBodyCookieManager.YOIX_ACCEPT_ORIGINAL_SERVER);
    static Integer  $ACCEPT_ALL = new Integer(YoixBodyCookieManager.YOIX_ACCEPT_ALL);

    static Object  $module[] = {
    //
    // NAME                      ARG                      COMMAND     MODE   REFERENCE
    // ----                      ---                      -------     ----   ---------
       null,                     "41",                    $LIST,      $RORO, $MODULENAME,
       "ACCEPT_NONE",            $ACCEPT_NONE,            $INTEGER,   $LR__, null,
       "ACCEPT_ORIGINAL_SERVER", $ACCEPT_ORIGINAL_SERVER, $INTEGER,   $LR__, null,
       "ACCEPT_ALL",             $ACCEPT_ALL,             $INTEGER,   $LR__, null,

       "IPPREC_0",               $IPPREC_0,               $INTEGER,   $LR__, null,
       "IPPREC_1",               $IPPREC_1,               $INTEGER,   $LR__, null,
       "IPPREC_2",               $IPPREC_2,               $INTEGER,   $LR__, null,
       "IPPREC_3",               $IPPREC_3,               $INTEGER,   $LR__, null,
       "IPPREC_4",               $IPPREC_4,               $INTEGER,   $LR__, null,
       "IPPREC_5",               $IPPREC_5,               $INTEGER,   $LR__, null,
       "IPPREC_6",               $IPPREC_6,               $INTEGER,   $LR__, null,
       "IPPREC_7",               $IPPREC_7,               $INTEGER,   $LR__, null,
       "IPPREC_MASK",            $IPPREC_MASK,            $INTEGER,   $LR__, null,
       "IPTOS_LOWCOST",          $IPTOS_LOWCOST,          $INTEGER,   $LR__, null,
       "IPTOS_LOWDELAY",         $IPTOS_LOWDELAY,         $INTEGER,   $LR__, null,
       "IPTOS_NORMAL",           $IPTOS_NORMAL,           $INTEGER,   $LR__, null,
       "IPTOS_RELIABILITY",      $IPTOS_RELIABILITY,      $INTEGER,   $LR__, null,
       "IPTOS_THROUGHPUT",       $IPTOS_THROUGHPUT,       $INTEGER,   $LR__, null,
       "IPTOS_MASK",             $IPTOS_MASK,             $INTEGER,   $LR__, null,
       "IPV4",                   $IPV4,                   $INTEGER,   $LR__, null,
       "IPV6",                   $IPV6,                   $INTEGER,   $LR__, null,

       "accept",                 "-1",                    $BUILTIN,   $LR_X, null,
       "getAddress",             "",                      $BUILTIN,   $LR_X, null,
       "getAllByName",           "",                      $BUILTIN,   $LR_X, null,
       "getHostAddress",         "",                      $BUILTIN,   $LR_X, null,
       "getHostName",            "",                      $BUILTIN,   $LR_X, null,
       "getInterfaceAddress",    "-1",                    $BUILTIN,   $LR_X, null,
       "getInterfaceAddresses",  "",                      $BUILTIN,   $LR_X, null,
       "isAnyLocalAddress",      "1",                     $BUILTIN,   $LR_X, null,
       "isLinkLocalAddress",     "1",                     $BUILTIN,   $LR_X, null,
       "isLoopbackAddress",      "1",                     $BUILTIN,   $LR_X, null,
       "isMCGlobal",             "1",                     $BUILTIN,   $LR_X, null,
       "isMCLinkLocal",          "1",                     $BUILTIN,   $LR_X, null,
       "isMCNodeLocal",          "1",                     $BUILTIN,   $LR_X, null,
       "isMCOrgLocal",           "1",                     $BUILTIN,   $LR_X, null,
       "isMCSiteLocal",          "1",                     $BUILTIN,   $LR_X, null,
       "isMulticastAddress",     "1",                     $BUILTIN,   $LR_X, null,
       "isReachable",            "-2",                    $BUILTIN,   $LR_X, null,
       "isSiteLocalAddress",     "1",                     $BUILTIN,   $LR_X, null,
       "parseURL",               "-1",                    $BUILTIN,   $LR_X, null,
       "receive",                "4",                     $BUILTIN,   $LR_X, null,
       "send",                   "4",                     $BUILTIN,   $LR_X, null,

       T_SOCKET,                 "24",                    $DICT,      $L___, T_SOCKET,
       N_MAJOR,                  $SOCKET,                 $INTEGER,   $LR__, null,
       N_MINOR,                  "0",                     $INTEGER,   $LR__, null,
       N_ALIVE,                  $FALSE,                  $INTEGER,   $RW_,  null,
       N_KEEPALIVE,              "-1",                    $INTEGER,   $RW_,  null,
       N_INPUT,                  T_STREAM,                $NULL,      $LR__, null,
       N_INPUTSHUTDOWN,          $FALSE,                  $INTEGER,   $RW_,  null,
       N_LINGER,                 "-1",                    $DOUBLE,    $RW_,  null,
       N_LOCALADDRESS,           T_STRING,                $NULL,      $RW_,  null,
       N_LOCALNAME,              T_STRING,                $NULL,      $LR__, null,
       N_LOCALPORT,              "0",                     $INTEGER,   $RW_,  null,
       N_OOBINLINE,              "-1",                    $INTEGER,   $RW_,  null,
       N_OUTPUT,                 T_STREAM,                $NULL,      $LR__, null,
       N_OUTPUTSHUTDOWN,         $FALSE,                  $INTEGER,   $RW_,  null,
       N_PERSISTENT,             $FALSE,                  $INTEGER,   $RW_,  null,
       N_RECEIVEBUFFERSIZE,      "-1",                    $INTEGER,   $RW_,  null,
       N_REMOTEADDRESS,          T_STRING,                $NULL,      $RW_,  null,
       N_REMOTENAME,             T_STRING,                $NULL,      $LR__, null,
       N_REMOTEPORT,             "-1",                    $INTEGER,   $RW_,  null,
       N_REUSEADDRESS,           "-1",                    $INTEGER,   $RW_,  null,
       N_SENDBUFFERSIZE,         "-1",                    $INTEGER,   $RW_,  null,
       N_SENDURGENTDATA,         T_CALLABLE,              $NULL,      $L__X, null,
       N_TCPNODELAY,             $FALSE,                  $INTEGER,   $RW_,  null,
       N_TIMEOUT,                "-1",                    $DOUBLE,    $RW_,  null,
       N_TRAFFICCLASS,           "-1",                    $INTEGER,   $RW_,  null,

       T_DATAGRAMSOCKET,         "17",                    $DICT,      $L___, T_DATAGRAMSOCKET,
       N_MAJOR,                  $DATAGRAMSOCKET,         $INTEGER,   $LR__, null,
       N_MINOR,                  "0",                     $INTEGER,   $LR__, null,
       N_ALIVE,                  $FALSE,                  $INTEGER,   $RW_,  null,
       N_BROADCAST,              "-1",                    $INTEGER,   $RW_,  null,
       N_LOCALADDRESS,           T_STRING,                $NULL,      $RW_,  null,
       N_LOCALNAME,              T_STRING,                $NULL,      $LR__, null,
       N_LOCALPORT,              "0",                     $INTEGER,   $RW_,  null,
       N_RECEIVE,                T_CALLABLE,              $NULL,      $L__X, null,
       N_RECEIVEBUFFERSIZE,      "-1",                    $INTEGER,   $RW_,  null,
       N_REMOTEADDRESS,          T_STRING,                $NULL,      $RW_,  null,
       N_REMOTENAME,             T_STRING,                $NULL,      $LR__, null,
       N_REMOTEPORT,             "-1",                    $INTEGER,   $RW_,  null,
       N_REUSEADDRESS,           "-1",                    $INTEGER,   $RW_,  null,
       N_SEND,                   T_CALLABLE,              $NULL,      $L__X, null,
       N_SENDBUFFERSIZE,         "-1",                    $INTEGER,   $RW_,  null,
       N_TIMEOUT,                "-1",                    $DOUBLE,    $RW_,  null,
       N_TRAFFICCLASS,           "-1",                    $INTEGER,   $RW_,  null,

       T_MULTICASTSOCKET,        "23",                    $DICT,      $L___, T_MULTICASTSOCKET,
       N_MAJOR,                  $MULTICASTSOCKET,        $INTEGER,   $LR__, null,
       N_MINOR,                  "0",                     $INTEGER,   $LR__, null,
       N_ALIVE,                  $FALSE,                  $INTEGER,   $RW_,  null,
       N_BROADCAST,              "-1",                    $INTEGER,   $RW_,  null,
       N_JOINGROUP,              T_CALLABLE,              $NULL,      $L__X, null,
       N_JOINEDGROUPS,           T_ARRAY,                 $NULL,      $LR__, null,
       N_LEAVEGROUP,             T_CALLABLE,              $NULL,      $L__X, null,
       N_LOCALADDRESS,           T_STRING,                $NULL,      $LR__, null,
       N_LOCALNAME,              T_STRING,                $NULL,      $LR__, null,
       N_LOCALPORT,              "0",                     $INTEGER,   $RW_,  null,
       N_LOOPBACK,               "$TRUE",                 $INTEGER,   $RW_,  null,
       N_NETWORKINTERFACE,       T_STRING,                $NULL,      $RW_,  null,
       N_RECEIVE,                T_CALLABLE,              $NULL,      $L__X, null,
       N_RECEIVEBUFFERSIZE,      "-1",                    $INTEGER,   $RW_,  null,
       N_REMOTEADDRESS,          T_STRING,                $NULL,      $RW_,  null,
       N_REMOTENAME,             T_STRING,                $NULL,      $LR__, null,
       N_REMOTEPORT,             "-1",                    $INTEGER,   $RW_,  null,
       N_REUSEADDRESS,           "-1",                    $INTEGER,   $RW_,  null,
       N_SEND,                   T_CALLABLE,              $NULL,      $L__X, null,
       N_SENDBUFFERSIZE,         "-1",                    $INTEGER,   $RW_,  null,
       N_TIMEOUT,                "-1",                    $DOUBLE,    $RW_,  null,
       N_TIMETOLIVE,             "-1",                    $INTEGER,   $RW_,  null,
       N_TRAFFICCLASS,           "-1",                    $INTEGER,   $RW_,  null,

       T_SERVERSOCKET,           "11",                    $DICT,      $L___, T_SERVERSOCKET,
       N_MAJOR,                  $SERVERSOCKET,           $INTEGER,   $LR__, null,
       N_MINOR,                  "0",                     $INTEGER,   $LR__, null,
       N_ACCEPT,                 T_CALLABLE,              $NULL,      $L__X, null,
       N_ALIVE,                  $FALSE,                  $INTEGER,   $RW_,  null,
       N_BACKLOG,                "50",                    $INTEGER,   $RW_,  null,
       N_LOCALADDRESS,           T_STRING,                $NULL,      $RW_,  null,
       N_LOCALNAME,              T_STRING,                $NULL,      $LR__, null,
       N_LOCALPORT,              "0",                     $INTEGER,   $RW_,  null,
       N_RECEIVEBUFFERSIZE,      "-1",                    $INTEGER,   $RW_,  null,
       N_REUSEADDRESS,           "-1",                    $INTEGER,   $RW_,  null,
       N_TIMEOUT,                "-1",                    $DOUBLE,    $RW_,  null,

       T_COOKIEMANAGER,          "7",                     $DICT,      $L___, T_COOKIEMANAGER,
       N_MAJOR,                  $COOKIEMANAGER,          $INTEGER,   $LR__, null,
       N_MINOR,                  "0",                     $INTEGER,   $LR__, null,
       N_ADD,                    T_CALLABLE,              $NULL,      $L__X, null,
       N_GET,                    T_CALLABLE,              $NULL,      $L__X, null,
       N_INSTALLED,              "$FALSE",                $INTEGER,   $RW_,  null,
       N_POLICY,                 $ACCEPT_NONE,            $INTEGER,   $RW_,  null,
       N_PUT,                    T_CALLABLE,              $NULL,      $L__X, null,
    };

    ///////////////////////////////////
    //
    // YoixModuleNet Methods
    //
    ///////////////////////////////////

    public static YoixObject
    accept(YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isServerSocket())
		obj = ((YoixBodyServerSocket)arg[0].body()).callAccept(arg, 1);
	    else VM.badArgument(0);
	} else VM.badCall();

	return(obj != null ? obj : YoixObject.newSocket());
    }


    public static YoixObject
    getAddress(YoixObject arg[]) {

	InetAddress  inet;
	YoixObject   obj = null;
	byte         addr[];
	int          n;

	if (arg.length <= 1) {
	    if (arg.length == 0 || arg[0].isString()) {
		try {
		    if (arg.length == 1)
			inet = InetAddress.getByName(arg[0].stringValue());
		    else inet = InetAddress.getLocalHost();
		    addr = inet.getAddress();
		    obj = YoixObject.newArray(addr.length);
		    for (n = 0; n < addr.length; n++)
			obj.putInt(n, (int)(addr[n] & 0xFF));
		}
		catch(UnknownHostException e) {}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj != null ? obj : YoixObject.newArray());
    }


    public static YoixObject
    getAllByName(YoixObject arg[]) {

	InetAddress  addr[];
	InetAddress  inet;
	YoixObject   obj = null;
	YoixObject   obj2;
	String       name = null;
	boolean      bytes = false;
	byte         info[];
	int          m;
	int          n;

	if (arg.length <= 2) {
	    if (arg.length == 0 || arg[0].isString() || arg[0].isInteger()) {
		if (arg.length > 0) {
		    if (arg[0].isString()) {
			name = arg[0].stringValue();
			if (arg.length > 1 && arg[1].isInteger())
			    bytes = arg[1].booleanValue();
			else if (arg.length == 1)
			    bytes = false;
			else VM.badArgument(1);
		    } else {
			bytes = arg[0].booleanValue();
			if (arg.length > 1 && arg[1].isString())
			    name = arg[1].stringValue();
			else if (arg.length == 1)
			    name = null;
			else VM.badArgument(1);
		    }
		} else {
		    name = null;
		    bytes = false;
		}
		try {
		    if (name != null) {
			inet = InetAddress.getByName(name);
			name = inet.getHostName();
		    } else name = InetAddress.getLocalHost().getHostName();
		    addr = InetAddress.getAllByName(name);
		    obj = YoixObject.newArray(addr.length);
		    for (n = 0; n < addr.length; n++) {
			if (bytes) {
			    info = addr[n].getAddress();
			    obj2 = YoixObject.newArray(info.length);
			    for (m = 0; m < info.length; m++)
				obj2.putInt(m, (int)(info[m] & 0xFF));
			    obj.putObject(n, obj2);
			} else obj.putString(n, addr[n].getHostAddress());
		    }
		}
		catch(UnknownHostException e) {}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj != null ? obj : YoixObject.newArray());
    }


    public static YoixObject
    getHostAddress(YoixObject arg[]) {

	InetAddress  inet;
	String       value = null;

	if (arg.length <= 1) {
	    if (arg.length == 0 || arg[0].isString()) {
		try {
		    if (arg.length == 1)
			inet = InetAddress.getByName(arg[0].stringValue());
		    else inet = InetAddress.getLocalHost();
		    value = inet.getHostAddress();
		}
		catch(UnknownHostException e) {}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(value));
    }


    public static YoixObject
    getHostName(YoixObject arg[]) {

	InetAddress  inet;
	String       value = null;

	if (arg.length <= 1) {
	    if (arg.length == 0 || arg[0].isString()) {
		try {
		    if (arg.length == 1)
			inet = InetAddress.getByName(arg[0].stringValue());
		    else inet = InetAddress.getLocalHost();
		    value = inet.getCanonicalHostName();
		}
		catch(UnknownHostException e) {}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(value));
    }


    public static YoixObject
    getInterfaceAddress(YoixObject arg[]) {

	String  address = null;
	String  name;
	int     mask;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isString()) {
		if (arg.length == 1 || arg[1].isInteger()) {
		    name = arg[0].stringValue();
		    mask = (arg.length > 1) ? arg[1].intValue() : IPV4|IPV6;
		    address = YoixMisc.getInterfaceAddress(name, mask);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(address));
    }


    public static YoixObject
    getInterfaceAddresses(YoixObject arg[]) {

	HashMap  map = null;
	int      mask;

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 0 || arg[0].isInteger()) {
		mask = (arg.length > 0) ? arg[0].intValue() : IPV4|IPV6;
		map = YoixMisc.getInterfaceAddresses(mask);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(map != null && map.size() > 0 ? YoixMisc.copyIntoDictionary(map) : YoixObject.newDictionary());
    }


    public static YoixObject
    isAnyLocalAddress(YoixObject arg[]) {

	InetAddress  inet;
	boolean      result = false;

	if (arg[0].isString() || arg[0].isNull()) {
	    try {
		inet = InetAddress.getByName(arg[0].stringValue());
		result = inet.isAnyLocalAddress();
	    }
	    catch(UnknownHostException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isLinkLocalAddress(YoixObject arg[]) {

	InetAddress  inet;
	boolean      result = false;

	if (arg[0].isString() || arg[0].isNull()) {
	    try {
		inet = InetAddress.getByName(arg[0].stringValue());
		result = inet.isLinkLocalAddress();
	    }
	    catch(UnknownHostException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isLoopbackAddress(YoixObject arg[]) {

	InetAddress  inet;
	boolean      result = false;

	if (arg[0].isString() || arg[0].isNull()) {
	    try {
		inet = InetAddress.getByName(arg[0].stringValue());
		result = inet.isLoopbackAddress();
	    }
	    catch(UnknownHostException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isMCGlobal(YoixObject arg[]) {

	InetAddress  inet;
	boolean      result = false;

	if (arg[0].isString() || arg[0].isNull()) {
	    try {
		inet = InetAddress.getByName(arg[0].stringValue());
		result = inet.isMCGlobal();
	    }
	    catch(UnknownHostException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isMCLinkLocal(YoixObject arg[]) {

	InetAddress  inet;
	boolean      result = false;

	if (arg[0].isString() || arg[0].isNull()) {
	    try {
		inet = InetAddress.getByName(arg[0].stringValue());
		result = inet.isMCLinkLocal();
	    }
	    catch(UnknownHostException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isMCNodeLocal(YoixObject arg[]) {

	InetAddress  inet;
	boolean      result = false;

	if (arg[0].isString() || arg[0].isNull()) {
	    try {
		inet = InetAddress.getByName(arg[0].stringValue());
		result = inet.isMCNodeLocal();
	    }
	    catch(UnknownHostException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isMCOrgLocal(YoixObject arg[]) {

	InetAddress  inet;
	boolean      result = false;

	if (arg[0].isString() || arg[0].isNull()) {
	    try {
		inet = InetAddress.getByName(arg[0].stringValue());
		result = inet.isMCOrgLocal();
	    }
	    catch(UnknownHostException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isMCSiteLocal(YoixObject arg[]) {

	InetAddress  inet;
	boolean      result = false;

	if (arg[0].isString() || arg[0].isNull()) {
	    try {
		inet = InetAddress.getByName(arg[0].stringValue());
		result = inet.isMCSiteLocal();
	    }
	    catch(UnknownHostException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isMulticastAddress(YoixObject arg[]) {

	InetAddress  inet;
	boolean      result = false;

	if (arg[0].isString() || arg[0].isNull()) {
	    try {
		inet = InetAddress.getByName(arg[0].stringValue());
		result = inet.isMulticastAddress();
	    }
	    catch(UnknownHostException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isReachable(YoixObject arg[]) {

	InetAddress       inet;
	NetworkInterface  netif = null;
	String            netifstr = null;
	boolean           result = false;
	int               timeout = -1;
	int               ttl = 0;
	int               argc;

	if (arg.length <= 4) {
	    if (arg[0].isString() || arg[0].isNull()) {
		argc = 1;
		if (arg[argc].isString() || arg[argc].isNull()) {
		    netifstr = arg[argc++].stringValue();
		}
		if (argc < arg.length) {
		    if (arg[argc].isNumber()) {
			timeout = (int)(arg[argc].doubleValue()*1000.0);
			if (timeout < 0)
			    VM.badArgumentValue(argc);
			argc++;
			if (argc < arg.length) {
			    if (arg[argc].isInteger()) {
				ttl = arg[argc].intValue();
				if (ttl < 0)
				    VM.badArgumentValue(argc);
			    } else VM.badArgument(argc);
			}
			VM.clearErrordict();
			try {
			    inet = InetAddress.getByName(arg[0].stringValue());
			    if (netifstr != null)
				netif = NetworkInterface.getByName(netifstr);
			    result = inet.isReachable(netif, ttl, timeout);
			}
			catch(UnknownHostException e) { VM.recordException(e); }
			catch(IOException e) { VM.recordException(e); }
			catch(IllegalArgumentException e) {} // cannot happen
		    } else VM.badArgument(argc);
		} else VM.badCall();
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isSiteLocalAddress(YoixObject arg[]) {

	InetAddress  inet;
	boolean      result = false;

	if (arg[0].isString() || arg[0].isNull()) {
	    try {
		inet = InetAddress.getByName(arg[0].stringValue());
		result = inet.isSiteLocalAddress();
	    }
	    catch(UnknownHostException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    parseURL(YoixObject arg[]) {

	YoixObject  dict = null;
	boolean     valid = true;
	String      spec = null;
	URL         context = null;
	URL         url;

	if (arg.length == 1 || arg.length == 2) {
	    valid = true;
	    if (arg.length == 1) {
		if (arg[0].isString() || arg[0].isNull())
		    spec = arg[0].stringValue();
		else VM.badArgument(0);
	    } else {
		if (arg[0].isString() || arg[0].isURL() || arg[0].isNull()) {
		    if (arg[1].isString() || arg[1].isNull()) {
			spec = arg[1].stringValue();
			if (arg[0].notNull()) {
			    if (arg[0].isString()) {
				try {
				    context = new URL(arg[0].stringValue());
				}
				catch(MalformedURLException e) {
				    VM.recordException(e);
				    valid = false;
				}
			    } else context = (URL)arg[0].getManagedObject();
			}
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    }
	    if (valid) {
		if (spec != null && spec.length() > 0 || context != null) {
		    try {
			url = new URL(context, spec);
			dict = YoixObject.newDictionary(11);
			dict.putString("authority", url.getAuthority());
			dict.putInt("defaultport", url.getDefaultPort());
			dict.putString("file", url.getFile());
			dict.putString("host", url.getHost());
			dict.putString("path", url.getPath());
			dict.putInt("port", url.getPort());
			dict.putString("protocol", url.getProtocol());
			dict.putString("query", url.getQuery());
			dict.putString("reference", url.getRef());
			dict.putString("userinfo", url.getUserInfo());
			dict.putString("externalform", url.toExternalForm());
		    }
		    catch(MalformedURLException e) {
			VM.recordException(e);
		    }
		}
	    }
	} else VM.badCall();

	return (dict == null ? YoixObject.newDictionary() : dict);
    }


    public static YoixObject
    receive(YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg[0].isDatagramSocket())
	    obj = ((YoixBodyDatagramSocket)arg[0].body()).callReceive(arg, 1);
	if (arg[0].isMulticastSocket())
	    obj = ((YoixBodyMulticastSocket)arg[0].body()).callReceive(arg, 1);
	else VM.badArgument(0);

	return(obj != null ? obj : YoixObject.newInt(-1));
    }


    public static YoixObject
    send(YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg[0].isDatagramSocket())
	    obj = ((YoixBodyDatagramSocket)arg[0].body()).callSend(arg, 1);
	else if (arg[0].isMulticastSocket())
	    obj = ((YoixBodyMulticastSocket)arg[0].body()).callSend(arg, 1);
	else VM.badArgument(0);

	return(obj != null ? obj : YoixObject.newInt(-1));
    }
}

