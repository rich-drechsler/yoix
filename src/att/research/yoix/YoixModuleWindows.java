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
import java.util.*;

abstract
class YoixModuleWindows extends YoixModule

{

    static String  $MODULENAME = M_WINDOWS;

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       null,                 "4",                 $LIST,      $RORO, $MODULENAME,
       "loadINI",            "-1",                $BUILTIN,   $LR_X, null,
       "readINI",            "3",                 $BUILTIN,   $LR_X, null,
       "remINI",             "3",                 $BUILTIN,   $LR_X, null,
       "writeINI",           "4",                 $BUILTIN,   $LR_X, null,
    };

    ///////////////////////////////////
    //
    // YoixModuleWindows Methods
    //
    ///////////////////////////////////

    public static YoixObject
    loadINI(YoixObject arg[]) {

	Enumeration  enm;
	YoixObject   obj = null;
	Hashtable    topics;
	boolean      tolower;
	String       topic;
	Object       key;
	Object       value;

	if (arg.length >= 1 || arg.length <= 3) {
	    if (arg[0].isString() && arg[0].notNull()) {
		if (arg.length == 1 || (arg[1].isString() || arg[1].isNull() || arg[1].isInteger())) {
		    if (arg.length < 3 || arg[2].isInteger()) {
			tolower = false;
			topic = null;
			if (arg.length > 1) {
			    if (arg[1].isString() && arg[1].notNull())
				topic = arg[1].stringValue();
			    if (arg.length == 2 && arg[1].isInteger())
				tolower = arg[1].booleanValue();
			    else if (arg.length == 3)
				tolower = arg[2].booleanValue();
			}

			if ((topics = iniLoader(arg[0].stringValue(), tolower)) != null) {
			    for (enm = topics.keys(); enm.hasMoreElements(); ) {
				key = enm.nextElement();
				if ((value = topics.get(key)) != null)
				    topics.put(key, YoixMisc.copyIntoDictionary((Hashtable)value));
				if (topic != null && key instanceof String)
				    if (topic.equalsIgnoreCase((String)key))
					topic = (String)key;
			    }
			    if (topic == null)
				obj = YoixMisc.copyIntoDictionary(topics);
			    else obj = (YoixObject)topics.get(topic);
			}
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj == null ? YoixObject.newDictionary() : obj);
    }


    public static YoixObject
    readINI(YoixObject arg[]) {

	Hashtable  topics;
	Hashtable  topic;
	String     key;
	String     value = null;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		if (arg[2].isString() && arg[2].notNull()) {
		    if ((topics = iniLoader(arg[0].stringValue(), true)) != null) {
			key = arg[1].stringValue().toLowerCase();
			if ((topic = (Hashtable)topics.get(key)) != null) {
			    key = arg[2].stringValue().toLowerCase();
			    value = (String)topic.get(key);
			}
		    }
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newString(value));
    }


    public static YoixObject
    remINI(YoixObject arg[]) {

	FileWriter  writer = null;
	String      topic;
	String      option;
	String      path;
	String      buf;
	boolean     result = false;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		if (arg[2].isString() && arg[2].notNull()) {
		    path = arg[0].stringValue();
		    topic = arg[1].stringValue();
		    option = arg[2].stringValue();
		    if ((buf = iniReader(path, topic, option, null)) != null) {
			try {
			    writer = new FileWriter(path);
			    writer.write(buf, 0, buf.length());
			    result = true;
			}
			catch(IOException e) {}
			finally {
			    if (writer != null) {
				try {
				    writer.close();
				}
				catch(IOException e) {}
			    }
			}
		    }
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    writeINI(YoixObject arg[]) {

	FileWriter  writer = null;
	String      topic;
	String      option;
	String      value;
	String      path;
	String      buf;
	boolean     result = false;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		if (arg[2].isString() && arg[2].notNull()) {
		    if (arg[3].isString() && arg[3].notNull()) {
			path = arg[0].stringValue();
			topic = arg[1].stringValue();
			option = arg[2].stringValue();
			value = arg[3].stringValue();
			if ((buf = iniReader(path, topic, option, value)) != null) {
			    try {
				writer = new FileWriter(path);
				writer.write(buf, 0, buf.length());
				result = true;
			    }
			    catch(IOException e) {}
			    finally {
				if (writer != null) {
				    try {
					writer.close();
				    }
				    catch(IOException e) {}
				}
			    }
			}
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static Hashtable
    iniLoader(String path, boolean tolower) {

	BufferedReader  reader = null;
	Hashtable       topics = null;
	Hashtable       topic;
	String          name;
	String          line;
	String          trim;
	String          key;
	String          value;
	int             index;

	try {
	    reader = new BufferedReader(new FileReader(YoixMisc.toYoixPath(path)));
	    topics = new Hashtable();
	    while ((line = reader.readLine()) != null) {
		trim = line.trim();
		if (trim.length() > 0 && trim.charAt(0) != ';') {
		    if (line.startsWith("[") && line.endsWith("]")) {
			name = line.substring(1, line.length() - 1);
			if (tolower)
			    name = name.toLowerCase();
			if ((topic = (Hashtable)topics.get(name)) == null)
			    topic = new Hashtable(20);
			while ((line = reader.readLine()) != null) {
			    trim = line.trim();
			    if (trim.length() > 0) {
				if (trim.charAt(0) != ';') {
				    if ((index = line.indexOf('=')) > 0) {
					key = line.substring(0, index);
					if (tolower)
					    key = key.toLowerCase();
					value = line.substring(index+1);
					topic.put(key, value);
				    }
				}
			    } else break;
			}
			topics.put(name, topic);
		    }
		}
	    }
	}
	catch(IOException e) {
	    VM.caughtException(e);
	    topics = null;
	}
	finally {
	    if (reader != null) {
		try {
		    reader.close();
		}
		catch(IOException e) {}
	    }
	}
	return(topics);
    }


    private static String
    iniReader(String path, String topic, String option, String value) {

	BufferedReader  reader = null;
	StringBuffer    buf = null;
	boolean         found;
	String          name;
	String          line;
	String          trim;
	String          key;
	String          newline;
	int             index;

	try {
	    reader = new BufferedReader(new FileReader(YoixMisc.toYoixPath(path)));
	    buf = new StringBuffer();
	    newline = "\r\n";
	    while ((line = reader.readLine()) != null) {
		buf.append(line + newline);
		trim = line.trim();
		if (trim.length() > 0 && trim.charAt(0) != ';') {
		    if (line.startsWith("[") && line.endsWith("]")) {
			name = line.substring(1, line.length() - 1);
			if (name.equalsIgnoreCase(topic)) {
			    found = false;
			    while ((line = reader.readLine()) != null) {
				trim = line.trim();
				if (trim.length() > 0) {
				    if (trim.charAt(0) != ';') {
					if ((index = line.indexOf('=')) > 0) {
					    key = line.substring(0, index);
					    if (key.equalsIgnoreCase(option)) {
						found = true;
						if (value != null)
						    line = key + "=" + value;
						else line = null;
					    }
					}
				    }
				    if (line != null)
					buf.append(line + newline);
				} else {
				    if (found == false) {
					if (option != null && value != null)
					    buf.append(option + "=" + value + newline);
				    }
				    buf.append(line + newline);
				    break;
				}
			    }
			}
		    }
		}
	    }
	}
	catch(IOException e) {
	    VM.caughtException(e);
	    buf = null;
	}
	finally {
	    if (reader != null) {
		try {
		    reader.close();
		}
		catch(IOException e) {}
	    }
	}
	return(buf != null ? new String(buf) : null);
    }
}

