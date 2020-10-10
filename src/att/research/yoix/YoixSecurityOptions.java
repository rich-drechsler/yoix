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
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

abstract
class YoixSecurityOptions

    implements YoixConstants

{

    //
    // Class that can build and install a security manager based on a
    // relatively simple description that usually comes from a command
    // option or property file entry.
    //

    private static Hashtable  handlers = new Hashtable(0);

    //
    // Strings used as the first line in various prompt dialogs.
    //

    private static String  SECURITY_CHECK = "Yoix Security Check";
    private static String  ALLOW_ACCEPT = "Accept Connection From";
    private static String  ALLOW_CONNECT = "Allow Connection To";
    private static String  ALLOW_DELETE = "Allow Deletion Of File";
    private static String  ALLOW_DISPLAY_READ = "Allow Read Of Display Pixels";
    private static String  ALLOW_ENVIRONMENT_READ = "Allow Reading Of Environment";
    private static String  ALLOW_EXEC = "Allow Execution Of File";
    private static String  ALLOW_EXEC_ANY_FILE = "Allow Execution Of Any File";
    private static String  ALLOW_EXIT = "Allow Exit With";
    private static String  ALLOW_FILE_READ = "Allow Read Of File";
    private static String  ALLOW_FILE_WRITE = "Allow Write To File";
    private static String  ALLOW_LISTEN = "Allow Listening To";
    private static String  ALLOW_MULTICAST = "Allow Use Of Multicast Address";
    private static String  ALLOW_PROPERTIES_ACCESS = "Allow Access To All Properties";
    private static String  ALLOW_PROPERTY_READ = "Allow Reading Of Property";
    private static String  ALLOW_PROPERTY_WRITE = "Allow Writing Of Property";
    private static String  ALLOW_RESOLVE = "Allow Resolve For Host";
    private static String  ALLOW_ROBOT_CREATION = "Allow Robot Creation";
    private static String  ALLOW_SYSTEM_CLIPBOARD_ACCESS = "Allow System Clipboard Access";
    private static String  ALLOW_YOIX_EVAL_FILE = "Allow Yoix Eval Of File";
    private static String  ALLOW_YOIX_EVAL_STRING = "Allow Yoix Eval Of String";
    private static String  ALLOW_YOIX_EXECUTE_FILE = "Allow Yoix Execute Of File";
    private static String  ALLOW_YOIX_EXECUTE_STRING = "Allow Yoix Execute Of String";
    private static String  ALLOW_YOIX_INCLUDE = "Allow Include Of Yoix File";
    private static String  ALLOW_YOIX_MODULE = "Allow Loading Of Yoix Module";
    private static String  ALLOW_YOIX_OPEN = "Allow Open Of";
    private static String  ALLOW_YOIX_OPEN_READ = "Allow Open For Read Of";
    private static String  ALLOW_YOIX_OPEN_READWRITE = "Allow Open For Read And Write Of";
    private static String  ALLOW_YOIX_OPEN_WRITE = "Allow Open For Write Of";
    private static String  ALLOW_YOIX_PROVIDER_ADD = "Allow Addition Of Provider";
    private static String  ALLOW_YOIX_PROVIDER_REMOVE = "Allow Removal Of Provider";

    //
    // Flags used to group security checking functions into categories.
    //

    private static final int  ACCEPT = 1<<0;
    private static final int  ADDPROVIDER = 1<<1;
    private static final int  CLIPBOARD = 1<<2;
    private static final int  CONNECT = 1<<3;
    private static final int  DELETE = 1<<4;
    private static final int  EVAL = 1<<5;
    private static final int  EXEC = 1<<6;
    private static final int  EXECUTE = 1<<7;
    private static final int  EXIT = 1<<8;
    private static final int  FILE = 1<<9;
    private static final int  INCLUDE = 1<<10;
    private static final int  LISTEN = 1<<11;
    private static final int  MODULE = 1<<12;
    private static final int  MULTICAST = 1<<13;
    private static final int  OPEN = 1<<14;
    private static final int  PROPERTIES = 1<<15;
    private static final int  READ = 1<<16;
    private static final int  READDISPLAY = 1<<17;
    private static final int  READENVIRONMENT = 1<<18;
    private static final int  READPROPERTY = 1<<19;
    private static final int  REMOVEPROVIDER = 1<<20;
    private static final int  ROBOT = 1<<21;
    private static final int  SOCKET = 1<<22;
    private static final int  WRITE = 1<<23;
    private static final int  WRITEPROPERTY = 1<<24;

    //
    // These are special flags and are interpreted in the private version
    // of addOption().
    //

    private static final int  TEMPFILE = 1<<25|FILE;
    private static final int  UPDATE = 1<<26|FILE;
    private static final int  CWD = 1<<27|FILE;

    //
    // The value assigned to these constants currently matters, so don't
    // change them unless you really know what's happening. An ArrayList
    // array contains three lists that represent DENY, PROMPT, and ALLOW
    // lists. The method that uses the lists walks through the array using
    // a loop that implicitly assumes the DENY list is first and the ALLOW
    // list is last.
    //

    private static final int  SKIPPED = -1;
    private static final int  DENY = 0;
    private static final int  PROMPT = 1;
    private static final int  ALLOW = 2;

    private static final Hashtable  accessmap = new Hashtable(10);

    static {
	accessmap.put("deny", new Integer(DENY));
	accessmap.put("refuse", new Integer(DENY));
	accessmap.put("prompt", new Integer(PROMPT));
	accessmap.put("allow", new Integer(ALLOW));
	accessmap.put("permit", new Integer(ALLOW));
    }

    //
    // A table that maps names, which should be lower case strings, to
    // to category flags or to the official name of the security checker.
    // Means users can say
    //
    //		prompt:file
    //
    // to apply the "prompt" policy to all security checkers that deal
    // with local files, or
    //
    //		prompt:write
    //
    // or
    //
    //		prompt:checkWrite
    //
    // to apply the "prompt" policy before local files are written.
    //

    private static final Hashtable  categorymap = new Hashtable(25);

    static {
	categorymap.put("accept", new Integer(ACCEPT));
	categorymap.put("addprovider", new Integer(ADDPROVIDER));
	categorymap.put("clipboard", new Integer(CLIPBOARD));
	categorymap.put("connect", new Integer(CONNECT));
	categorymap.put("delete", new Integer(DELETE));
	categorymap.put("eval", new Integer(EVAL));
	categorymap.put("exec", new Integer(EXEC));
	categorymap.put("execute", new Integer(EXECUTE));
	categorymap.put("exit", new Integer(EXIT));
	categorymap.put("file", new Integer(FILE));
	categorymap.put("include", new Integer(INCLUDE));
	categorymap.put("listen", new Integer(LISTEN));
	categorymap.put("module", new Integer(MODULE));
	categorymap.put("multicast", new Integer(MULTICAST));
	categorymap.put("open", new Integer(OPEN));
	categorymap.put("properties", new Integer(PROPERTIES));
	categorymap.put("read", new Integer(READ));
	categorymap.put("readdisplay", new Integer(READDISPLAY));
	categorymap.put("readenvironment", new Integer(READENVIRONMENT));
	categorymap.put("readproperty", new Integer(READPROPERTY));
	categorymap.put("removeprovider", new Integer(REMOVEPROVIDER));
	categorymap.put("robot", new Integer(ROBOT));
	categorymap.put("socket", new Integer(SOCKET));
	categorymap.put("write", new Integer(WRITE));
	categorymap.put("writeproperty", new Integer(WRITEPROPERTY));

	categorymap.put("tempfile", new Integer(TEMPFILE));
	categorymap.put("tmpfile", new Integer(TEMPFILE));
	categorymap.put("update", new Integer(UPDATE));
	categorymap.put("cwd", new Integer(CWD));
    }

    //
    // A table that associates builtins and categories with functions
    // defined in a SecurityManager. Overkill right now because the
    // ARGUMENT and TITLE columns aren't currently used.
    //

    private static Object  OPTIONSTABLE[] = {
    //
    // NAME                          ARGUMENT   TITLE   FLAGS
    // ----                          --------   -----   -----
       N_CHECKACCEPT,                null,      null,   new Integer(ACCEPT|SOCKET),
       N_CHECKCONNECT,               null,      null,   new Integer(CONNECT|SOCKET),
       N_CHECKDELETE,                null,      null,   new Integer(DELETE|FILE),
       N_CHECKEXEC,                  null,      null,   new Integer(EXEC),
       N_CHECKEXIT,                  null,      null,   new Integer(EXIT),
       N_CHECKLISTEN,                null,      null,   new Integer(LISTEN|SOCKET),
       N_CHECKMULTICAST,             null,      null,   new Integer(MULTICAST|SOCKET),
       N_CHECKPROPERTIESACCESS,      null,      null,   new Integer(PROPERTIES),
       N_CHECKREAD,                  null,      null,   new Integer(READ|FILE),
       N_CHECKREADDISPLAYPIXELS,     null,      null,   new Integer(READDISPLAY),
       N_CHECKREADENVIRONMENT,       null,      null,   new Integer(READENVIRONMENT),
       N_CHECKREADPROPERTY,          null,      null,   new Integer(READPROPERTY|PROPERTIES),
       N_CHECKCREATEROBOT,           null,      null,   new Integer(ROBOT),
       N_CHECKSYSTEMCLIPBOARDACCESS, null,      null,   new Integer(CLIPBOARD),
       N_CHECKWRITE,                 null,      null,   new Integer(WRITE|FILE),
       N_CHECKWRITEPROPERTY,         null,      null,   new Integer(WRITEPROPERTY),

       N_CHECKYOIXADDPROVIDER,       null,      null,   new Integer(ADDPROVIDER),
       N_CHECKYOIXEVAL,              null,      null,   new Integer(EVAL),
       N_CHECKYOIXEXECUTE,           null,      null,   new Integer(EXECUTE),
       N_CHECKYOIXINCLUDE,           null,      null,   new Integer(INCLUDE),
       N_CHECKYOIXMODULE,            null,      null,   new Integer(MODULE),
       N_CHECKYOIXOPEN,              null,      null,   new Integer(OPEN),
       N_CHECKYOIXREMOVEPROVIDER,    null,      null,   new Integer(REMOVEPROVIDER),
    };

    //
    // Definitions used to match arguments or build data structures that
    // are used during argument matching.
    //

    private static String  STAR = "*";
    private static String  DOTDOT = "..";
    private static int     MATCHERFLAGS = RAWSHELL_PATTERN;

    private static YoixRERegexp  DEFAULTMATCHER = new YoixRERegexp(STAR, MATCHERFLAGS);

    ///////////////////////////////////
    //
    // YoixSecurityOptions Methods
    //
    ///////////////////////////////////

    static final String
    addOption(String text) {

	ArrayList  commands;
	ArrayList  fields;
	String     command;
	String     pattern;
	String     error = null;
	int        count;
	int        m;
	int        n;

	if (handlers != null && VM.notBooted()) {
	    if ((commands = YoixMisc.split(text, " ")) != null) {
		for (n = 0; n < commands.size() && error == null; n++) {
		    if ((command = (String)commands.get(n)) != null) {
			command = command.trim();
			if (command.length() > 0) {
			    if ((fields = YoixMisc.split(command, ":")) != null) {
				if ((count = fields.size()) > 1) {
				    if (count > 2) {
					//
					// Argument could contain colons, so
					// glue things back together. Typical
					// case is a host:port pattern.
					//
					pattern = (String)fields.get(2);
					for (m = 3; m < count; m++)
					    pattern += ":" + (String)fields.get(m);
				    } else pattern = STAR;
				    error = addOption(
					(String)fields.get(0),
					(String)fields.get(1),
					pattern,
					OPTIONSTABLE
				    );
				} else error = SECURITYOPTION;
			    }
			}
		    }
		}
	    }
	}
	return(error);
    }


    public static YoixObject
    checkAccept(YoixObject arg[], Object extra[]) {

	ArrayList  lists[];
	String     host;
	int        port;
	int        index;
	int        result = 0;

	if (arg.length == 2) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg[1].isInteger()) {
		    host = arg[0].stringValue();
		    port = arg[1].intValue();
		    lists = (ArrayList[])extra[0];
		    if (!host.startsWith("[") && host.indexOf(':') != -1)
			host = "[" + host + "]";
		    if ((index = findAccessIndex(host + ":" + port, lists, null)) == SKIPPED)
			index = findAccessIndex(host, lists, null);
		    result = getAccessAnswer(index, ALLOW_ACCEPT, host + ":" + port);
		}
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkConnect(YoixObject arg[], Object extra[]) {

	ArrayList  lists[];
	ArrayList  list;
	String     host;
	int        port;
	int        index;
	int        result = 0;
	int        n;

	//
	// Decided that we should either allow or skip the check when port
	// is -1 (i.e., caller wants to determine an IP address) and our
	// answer should only be based on whether or not the lists array
	// contains at least one non-empty list. That would mean there was
	// at least one command line option that targeted this method.
	//

	if (arg.length == 2) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg[1].isInteger()) {
		    host = arg[0].stringValue();
		    port = arg[1].intValue();
		    lists = (ArrayList[])extra[0];
		    if (!host.startsWith("[") && host.indexOf(':') != -1)
			host = "[" + host + "]";
		    if (port != -1) {
			if ((index = findAccessIndex(host + ":" + port, lists, null)) == SKIPPED)
			    index = findAccessIndex(host, lists, null);
			result = getAccessAnswer(index, ALLOW_CONNECT, host + ":" + port);
		    } else {
			index = SKIPPED;
			for (n = 0; n < lists.length; n++) {
			    if ((list = lists[n]) != null && list.size() > 0) {
				index = ALLOW;
				break;
			    }
			}
			result = getAccessAnswer(index, ALLOW_RESOLVE, host);
		    }
		}
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkCreateRobot(YoixObject arg[], Object extra[]) {

	int  result = 0;

	if (arg.length == 0) {
	    result = getAccessAnswer(
		findAccessIndex(null, (ArrayList[])extra[0], null),
		ALLOW_ROBOT_CREATION, null
	    );
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkDelete(YoixObject arg[], Object extra[]) {

	String  path;
	int     result = 0;

	if (arg.length == 1) {
	    if (arg[0].isString() && arg[0].notNull()) {
		path = arg[0].stringValue();
		result = getAccessAnswer(
		    findAccessIndex(path, (ArrayList[])extra[0], DOTDOT),
		    ALLOW_DELETE, path
		);
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkExec(YoixObject arg[], Object extra[]) {

	String  cmd;
	File    file;
	int     result = 0;

	if (arg.length == 1) {
	    if (arg[0].isString() && arg[0].notNull()) {
		cmd = arg[0].stringValue();
		file = new File(cmd);
		if (file.isAbsolute()) {
		    result = getAccessAnswer(
			findAccessIndex(cmd, (ArrayList[])extra[0], DOTDOT),
			ALLOW_EXEC, cmd
		    );
		} else {
		    result = getAccessAnswer(
			findAccessIndex(null, (ArrayList[])extra[0], null),
			ALLOW_EXEC_ANY_FILE, null
		    );
		}
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkExit(YoixObject arg[], Object extra[]) {

	String  status;
	int     result = 0;

	if (arg.length == 1) {
	    if (arg[0].isNumber() && arg[0].notNull()) {
		status = arg[0].intValue() + "";
		result = getAccessAnswer(
		    findAccessIndex(null, (ArrayList[])extra[0], null),
		    ALLOW_EXIT, "status=" + status
		);
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkListen(YoixObject arg[], Object extra[]) {

	int  port;
	int  result = 0;

	if (arg.length == 1) {
	    if (arg[0].isInteger()) {
		port = arg[0].intValue();
		result = getAccessAnswer(
		    findAccessIndex(port + "", (ArrayList[])extra[0], null),
		    ALLOW_LISTEN, "port=" + port
		);
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkMulticast(YoixObject arg[], Object extra[]) {

	String  address;
	int     result = 0;

	if (arg.length == 1) {
	    if (arg[0].isString() && arg[0].notNull()) {
		address = arg[0].stringValue();
		result = getAccessAnswer(
		    findAccessIndex(address, (ArrayList[])extra[0], null),
		    ALLOW_MULTICAST, address
		);
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkPropertiesAccess(YoixObject arg[], Object extra[]) {

	int  result = 0;

	if (arg.length == 0) {
	    result = getAccessAnswer(
		findAccessIndex(null, (ArrayList[])extra[0], null),
		ALLOW_PROPERTIES_ACCESS, null
	    );
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkRead(YoixObject arg[], Object extra[]) {

	String  path;
	int     result = 0;

	if (arg.length == 1) {
	    if (arg[0].isString() && arg[0].notNull()) {
		path = arg[0].stringValue();
		result = getAccessAnswer(
		    findAccessIndex(path, (ArrayList[])extra[0], DOTDOT),
		    ALLOW_FILE_READ, path
		);
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkReadDisplayPixels(YoixObject arg[], Object extra[]) {

	int  result = 0;

	if (arg.length == 0) {
	    result = getAccessAnswer(
		findAccessIndex(null, (ArrayList[])extra[0], null),
		ALLOW_DISPLAY_READ, null
	    );
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkReadEnvironment(YoixObject arg[], Object extra[]) {

	String  name;
	int     result = 0;

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 0 || arg[0].isString() || arg[0].isNull()) {
		name = (arg.length == 0 || arg[0].isNull()) ? null : arg[0].stringValue();
		result = getAccessAnswer(
		    findAccessIndex(name, (ArrayList[])extra[0], null),
		    ALLOW_ENVIRONMENT_READ, name
		);
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkReadProperty(YoixObject arg[], Object extra[]) {

	String  property;
	int     result = 0;

	if (arg.length == 1) {
	    if (arg[0].isString() && arg[0].notNull()) {
		property = arg[0].stringValue();
		result = getAccessAnswer(
		    findAccessIndex(property, (ArrayList[])extra[0], null),
		    ALLOW_PROPERTY_READ, property
		);
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkSystemClipboardAccess(YoixObject arg[], Object extra[]) {

	int  result = 0;

	if (arg.length == 0) {
	    result = getAccessAnswer(
		findAccessIndex(null, (ArrayList[])extra[0], null),
		ALLOW_SYSTEM_CLIPBOARD_ACCESS, null
	    );
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkWrite(YoixObject arg[], Object extra[]) {

	String  path;
	int     result = 0;

	if (arg.length == 1) {
	    if (arg[0].isString() && arg[0].notNull()) {
		path = arg[0].stringValue();
		result = getAccessAnswer(
		    findAccessIndex(path, (ArrayList[])extra[0], DOTDOT),
		    ALLOW_FILE_WRITE, path
		);
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkWriteProperty(YoixObject arg[], Object extra[]) {

	String  property;
	int     result = 0;

	if (arg.length == 1) {
	    if (arg[0].isString() && arg[0].notNull()) {
		property = arg[0].stringValue();
		result = getAccessAnswer(
		    findAccessIndex(property, (ArrayList[])extra[0], null),
		    ALLOW_PROPERTY_WRITE, property
		);
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkYoixAddProvider(YoixObject arg[], Object extra[]) {

	String  provider;
	int     result = 0;

	if (arg.length == 1) {
	    if (arg[0].isString() && arg[0].notNull()) {
		provider = arg[0].stringValue();
		result = getAccessAnswer(
		    findAccessIndex(provider, (ArrayList[])extra[0], null),
		    ALLOW_YOIX_PROVIDER_ADD, provider
		);
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkYoixEval(YoixObject arg[], Object extra[]) {

	boolean  ispath;
	String   source;
	int      result = 0;

	if (arg.length == 2) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg[1].isInteger()) {
		    source = arg[0].stringValue();
		    ispath = arg[1].booleanValue();
		    if (ispath) {
			result = getAccessAnswer(
			    findAccessIndex(source, (ArrayList[])extra[0], DOTDOT),
			    ALLOW_YOIX_EVAL_FILE, source
			);
		    } else {
			if (source.length() > 40)
			    source = source.substring(0, 40) + "...";
			result = getAccessAnswer(
			    findAccessIndex(null, (ArrayList[])extra[0], null),
			    ALLOW_YOIX_EVAL_STRING, source
			);
		    }
		}
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkYoixExecute(YoixObject arg[], Object extra[]) {

	boolean  ispath;
	String   source;
	int      result = 0;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg[1].isInteger()) {
		    source = arg[0].stringValue();
		    ispath = arg[1].booleanValue();
		    if (ispath) {
			result = getAccessAnswer(
			    findAccessIndex(source, (ArrayList[])extra[0], DOTDOT),
			    ALLOW_YOIX_EXECUTE_FILE, source
			);
		    } else {
			if (source.length() > 40)
			    source = source.substring(0, 40) + "...";
			result = getAccessAnswer(
			    findAccessIndex(null, (ArrayList[])extra[0], null),
			    ALLOW_YOIX_EXECUTE_STRING, source
			);
		    }
		}
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkYoixInclude(YoixObject arg[], Object extra[]) {

	String  path;
	int     result = 0;

	if (arg.length == 1) {
	    if (arg[0].isString() && arg[0].notNull()) {
		path = arg[0].stringValue();
		result = getAccessAnswer(
		    findAccessIndex(path, (ArrayList[])extra[0], DOTDOT),
		    ALLOW_YOIX_INCLUDE, path
		);
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkYoixModule(YoixObject arg[], Object extra[]) {

	String  classname;
	int     result = 0;

	if (arg.length == 1) {
	    if (arg[0].isString() && arg[0].notNull()) {
		classname = arg[0].stringValue();
		result = getAccessAnswer(
		    findAccessIndex(classname, (ArrayList[])extra[0], null),
		    ALLOW_YOIX_MODULE, classname
		);
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkYoixOpen(YoixObject arg[], Object extra[]) {

	String  path;
	String  title;
	int     type;
	int     mode;
	int     result = 0;

	if (arg.length == 3) {
	    if (arg[0].isString() && arg[0].notNull()) {
		path = arg[0].stringValue();
		type = arg[1].intValue();
		mode = arg[2].intValue();
		if ((title = (String)extra[2]) == null) {
		    if ((mode&RW_) == RW_)
			title = ALLOW_YOIX_OPEN_READWRITE;
		    else if ((mode&R__) == R__)
			title = ALLOW_YOIX_OPEN_READ;
		    else if ((mode&_W_) == _W_)
			title = ALLOW_YOIX_OPEN_WRITE;
		    else title = ALLOW_YOIX_OPEN;
		}
		switch (type) {
		    case YoixConstants.FILE:
			//
			// Not completely convinced about using toYoixPath()
			// but it's only for the argument lookup and is what
			// YoixBodyStream() uses to get the file's fullname.
			//
			result = getAccessAnswer(
			    findAccessIndex(YoixMisc.toYoixPath(path), (ArrayList[])extra[0], DOTDOT),
			    title, path
			);
			break;

		    case YoixConstants.STRINGSTREAM:
			//
			// Don't see much point in checking a STRINGSTREAM
			// open, so for now we just say it's always OK.
			//
			result = 1;
			break;

		    case YoixConstants.URL:
			result = getAccessAnswer(
			    findAccessIndex(path, (ArrayList[])extra[0], null),
			    title, path
			);
			break;
		}
	    }
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkYoixRemoveProvider(YoixObject arg[], Object extra[]) {

	String  provider;
	int     result = 0;

	if (arg.length == 1) {
	    if (arg[0].isString() && arg[0].notNull()) {
		provider = arg[0].stringValue();
		result = getAccessAnswer(
		    findAccessIndex(provider, (ArrayList[])extra[0], null),
		    ALLOW_YOIX_PROVIDER_REMOVE, provider
		);
	    }
	}
	return(YoixObject.newInt(result));
    }


    static boolean
    installOptions() {

	Enumeration  keys;
	YoixObject   handler;
	boolean      result = false;
	String       classname;
	Object       key;

	if (handlers != null && handlers.size() > 0) {
	    classname = YoixSecurityOptions.class.getName();
	    for (keys = handlers.keys(); keys.hasMoreElements(); ) {
		key = keys.nextElement();
		if (key instanceof String) {
		    handler = YoixObject.newBuiltin(
			classname + "." + (String)key,
			0,
			true,
			(Object[])handlers.get(key)
		    );
		    if (VM.setSecurityChecker((String)key, handler) == false)
			VM.die(INVALIDACCESS, (String)key);
		    result = true;
		}
	    }
	    handlers = null;
	}
	return(result);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static void
    addAddresses(ArrayList arglist, String pattern) {

	InetAddress  addrs[];
	String       host;
	String       port;
	String       address;
	int          index;
	int          n;

	//
	// Takes a pattern that includes a host and port and tries to add
	// the same permissions for all IP addresses that getAllByName()
	// returns for that host. Omitting this step means the user also
	// has to include options that cover IP addresses for hosts that
	// are listed on the command line. There are other solutions, but
	// this seemed like the simplest approach.
	// 

	if (pattern != null && arglist != null) {
	    if (pattern != STAR && pattern.indexOf('*') < 0) {
		if ((index = pattern.indexOf(':')) > 0 && index < pattern.length() - 1) {
		    if (index == pattern.lastIndexOf(':')) {
			host = pattern.substring(0, index);
			port = pattern.substring(index+1);
			if (host.length() > 0 && port.length() > 0) {	// unnecessary test
			    try {
				if ((addrs = InetAddress.getAllByName(host)) != null) {
				    for (n = 0; n < addrs.length; n++) {
					if (addrs[n] != null) {
					    address = addrs[n].getHostAddress();
					    if (address != null && address.indexOf('/') < 0 && address.equals(host) == false)
						arglist.add(newMatcher(address + ":" + port));
					}
				    }
				}
			    }
			    catch(Exception e) {}
			}
		    }
		}
	    }
	}
    }


    private static String
    addOption(String access, String category, String pattern, Object table[]) {

	ArrayList  arglist;
	ArrayList  lists[];
	String     error = null;
	String     name;
	String     title;
	String     path;
	Object     value;
	Object     arg;
	int        flag;
	int        mask;
	int        columns = 4;
	int        matches = 0;
	int        index;
	int        n;

	if ((value = accessmap.get(access.toLowerCase())) != null) {
	    if ((index = ((Integer)value).intValue()) >= 0 && index < 3) {
		if ((value = categorymap.get(category.toLowerCase())) != null) {
		    if (value instanceof String) {
			category = (String)value;
			mask = 0;
		    } else mask = ((Integer)value).intValue();
		} else mask = 0;
		for (n = 0; n < table.length && error == null; n += columns) {
		    name = (String)table[n];
		    arg = table[n+1];
		    title = (String)table[n+2];
		    flag = ((Integer)table[n+3]).intValue();
		    if ((flag&mask) != 0 || category.equals(name)) {
			if ((value = handlers.get(name)) == null) {
			    lists = new ArrayList[] {null, null, null};
			    handlers.put(name, new Object[] {lists, arg, title});
			} else lists = (ArrayList[])((Object[])value)[0];
			if ((arglist = lists[index]) == null) {
			    arglist = new ArrayList();
			    lists[index] = arglist;
			}
			if (mask == UPDATE) {
			    if ((path = YoixMisc.getLocalJarPath()) != null) {
				arglist.add(newMatcher(YoixMisc.toYoixPath(path)));
				arglist.add(newMatcher(YoixMisc.toYoixPath(path + ".update")));
			    }
			} else if (mask == TEMPFILE) {
			    //
			    // In some cases directory gets checked, too, so include that.
			    // Also add path with symbolic links removed (toRealPath)
			    //
			    arglist.add(newMatcher(TMPDIR));
			    arglist.add(newMatcher(TMPDIR + FILESEP + "*"));
			    arglist.add(newMatcher(YoixMisc.toLocalPath(YoixMisc.toRealPath(TMPDIR))));
			    arglist.add(newMatcher(YoixMisc.toLocalPath(YoixMisc.toRealPath(TMPDIR) + "/*")));
			} else if (mask == CWD) {
			    // should be the initPWD computed in YoixMisc
			    arglist.add(newMatcher(YoixMisc.toLocalPath(".")));
			    arglist.add(newMatcher(YoixMisc.toLocalPath(".") + FILESEP + "*"));
			    arglist.add(newMatcher(YoixMisc.toLocalPath(YoixMisc.toRealPath("."))));
			    arglist.add(newMatcher(YoixMisc.toLocalPath(YoixMisc.toRealPath(".") + "/*")));
			} else arglist.add(newMatcher(pattern));

			if (mask == ACCEPT || mask == CONNECT || mask == LISTEN) {
			    addAddresses(arglist, pattern);
			}
			//
			// Recent addition that makes DENY the default choice
			// unless DEFAULTMATCHER is explicitly added to one
			// of the other lists. The code in findAccessIndex()
			// that looks for DEFAULTMATCHER was also changed to
			// let later a DEFAULTMATCHER win.
			//
			if (index != DENY) {
			    if ((arglist = lists[DENY]) == null) {
				arglist = new ArrayList();
				lists[DENY] = arglist;
			    }
			    if (arglist.contains(DEFAULTMATCHER) == false)
				arglist.add(DEFAULTMATCHER);
			}
			matches++;
		    }
		}
	    } else error = INTERNALERROR;
	} else error = SECURITYOPTION;

	return(error == null && matches == 0 ? SECURITYOPTION : error);
    }


    private static int
    findAccessIndex(String arg, ArrayList lists[], String reject) {

	YoixRERegexp  matcher;
	ArrayList     list;
	boolean       unmatched = true;
	int           index = SKIPPED;
	int           m;
	int           n;

	//
	// Be careful if you make changes here - we implicilty assume that
	// the DENY list is first, PROMPT list is second, and the ALLOW is
	// last.
	//

	if (arg == null || reject == null || arg.indexOf(reject) < 0) {
	    for (n = 0; n < lists.length && unmatched; n++) {
		if ((list = lists[n]) != null) {
		    if (list.contains(DEFAULTMATCHER))
			index = n;
		    if (arg != null && arg.length() > 0) {
			for (m = 0; m < list.size() && unmatched; m++) {
			    if ((matcher = (YoixRERegexp)list.get(m)) != null) {
				if (matcher != DEFAULTMATCHER) {
				    if (matcher.exec(arg, null)) {
					index = n;
					unmatched = false;
				    }
				}
			    }
			}
		    }
		}
	    }
	}
	return(index);
    }


    private static int
    getAccessAnswer(int index, String action, String text) {

	Object  message;
	int     response;
	int     answer = 0;

	//
	// Builtins called to check stuff should return an integer that is
	// 0 if the check failed, 1 if it succeeded, 2 if there wasn't an
	// answer, and -1 if the user was prompted and ended up rejecting
	// the request. In other words, a return of -1 means the failure
	// is definitive and the caller shouldn't try to get a different
	// answer (e.g., by calling Java's default security checker).
	//

	if (index == PROMPT) {
	    if (action != null || text != null) {
		if (action != null && text != null)
		    message = new String[] {SECURITY_CHECK, " ", action + ":", text};
		else message = new String[] {SECURITY_CHECK, " ", (action != null ? action + "?" : text)};
		try {
		    response = JOptionPane.showConfirmDialog(
			null,
			message,
			SECURITY_CHECK,
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE
		    );
		}
		catch(HeadlessException e) {
		    response = JOptionPane.YES_OPTION;
		}
		answer = (response == JOptionPane.YES_OPTION) ? 1 : -1;
	    } else answer = 1;
	} else if (index == ALLOW)
	    answer = 1;
	else if (index == SKIPPED)
	    answer = 2;
	else answer = 0;

	return(answer);
    }


    private static YoixRERegexp
    newMatcher(String pattern) {

	return(pattern != null && pattern != STAR
	    ? new YoixRERegexp(pattern, MATCHERFLAGS)
	    : DEFAULTMATCHER
	);
    }
}

