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

public
class YoixError extends Error

    implements YoixAPI,
	       YoixConstants

{

    private YoixObject  details;
    private String      prefix;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    YoixError() {

	super();
    }

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public static YoixObject
    recordDetails(String name) {

	return(recordDetails(ABORTERROR, name, null, null));
    }


    public static YoixObject
    recordDetails(String name, Throwable t) {

	return(recordDetails(ABORTERROR, name, null, t));
    }


    public static YoixObject
    recordDetails(String name, String args[], Throwable t) {

	return(recordDetails(ABORTERROR, name, args, t));
    }

    ///////////////////////////////////
    //
    // YoixError Methods
    //
    ///////////////////////////////////

    final YoixObject
    getDetails() {

	return(details);
    }


    static YoixObject
    recordDetails(String type, String name, String args[], Throwable t) {

	YoixObject  dict;

	dict = YoixObject.newDictionary(8);
	dict.putString(N_TYPE, type);
	dict.putString(N_NAME, name);
	dict.putObject(N_ARGS, YoixMisc.copyIntoArray(args));
	dict.putString(N_STACKTRACE, YoixVMThread.dump(null));
	dict.putDouble(N_TIMESTAMP, System.currentTimeMillis());
	dict.putString(N_MESSAGE, formatMessage(dict, args));
	if (t == null) {
	    t = new Throwable(PREFIX_JAVASTACKTRACE);
	    dict.putString(N_EXCEPTION, null);
	} else dict.putString(N_EXCEPTION, t.toString());
	dict.putString(N_JAVATRACE, YoixMisc.javaTrace(t));
	insertStartLine(dict, t);

	return(dict);
    }


    final void
    setDetails(YoixObject obj) {

	String  mesg;
	String  str;
	int     index;

	details = obj;

	if (prefix != null && details != null) {
	    if ((mesg = details.getString(N_MESSAGE)) != null) {
		if ((index = mesg.indexOf(';')) > 0) {
		    str = mesg.substring(0, index) + "; " + prefix + ";";
		    str += mesg.substring(index + 1);
		} else str = mesg + "; " + prefix;
		details.putString(N_MESSAGE, str);
		prefix = null;
	    }
	}
    }


    final void
    setPrefix(String tag, String arg) {

	if (tag != null && arg != null)
	    prefix = tag + ": " + arg;
	else prefix = null;
    }


    final void
    setLocation(YoixObject obj) {

	YoixBodyTag  tag;

	if (obj != null && obj.isTag()) {
	    tag = (YoixBodyTag)obj.body();
	    setLocation(tag.getLine(), tag.getColumn(), tag.getSource());
	}
    }


    final void
    setLocation(int line, int column, String source) {

	String  mesg;
	String  sep;
	int     location_index = -1;
	int     line_index;
	int     column_index;
	int     source_index;
	int     index;

	//
	// Currently assumes location information, if it's present, comes
	// last in the existing message. Good enough for now, but probably
	// not hard to make completely general.
	//

	if (details != null) {
	    if ((mesg = details.getString(N_MESSAGE)) != null) {
		if ((index = mesg.indexOf(YoixBodyTag.getLinePrefix())) >= 0)
		    location_index = index;
		if ((index = mesg.indexOf(YoixBodyTag.getColumnPrefix())) >= 0)
		    location_index = (location_index < 0) ? index : Math.min(index, location_index);
		if ((index = mesg.indexOf(YoixBodyTag.getSourcePrefix())) >= 0 && index < location_index)
		    location_index = (location_index < 0) ? index : Math.min(index, location_index);
		if (location_index >= 0) {
		    mesg = mesg.substring(0, location_index);
		    sep = "";
		    if (line >= 0) {
			mesg += sep + YoixBodyTag.getLinePrefix() + line;
			sep = "; ";
		    }
		    if (column >= 0) {
			mesg += sep + YoixBodyTag.getColumnPrefix() + column;
			sep = "; ";
		    }
		    if (source != null) {
			mesg += sep + YoixBodyTag.getSourcePrefix() + source;
			sep = "; ";
		    }
		    details.putString(N_MESSAGE, mesg);
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static String
    formatMessage(YoixObject dict, String args[]) {

	String  type;
	String  name;
	String  suffix;
	String  mesg;
	String  tag;
	String  sep;
	int     index;
	int     n;

	type = dict.getString(N_TYPE, ABORTERROR);
	name = dict.getString(N_NAME, "");
	suffix = YoixVMThread.dump(null, MODEL_YOIXSTACKTRACE, false);

	if ((index = suffix.indexOf(NL)) >= 0)
	    suffix = suffix.substring(0, index).trim();

	if (name != null && name.length() > 0) {
	    mesg = type + ": " + name;
	    if (args != null) {
		for (n = 0; n < args.length; n++) {
		    if (args[n] != null) {
			if (n < args.length - 1) {
			    tag = args[n++];
			    if (tag.length() == 0 || Character.isLetter(tag.charAt(0)) == false) {
				if (suffix != null && tag.indexOf(NL) == 0) {
				    mesg += "; " + suffix;
				    suffix = null;
				}
				mesg += tag;
				sep = "";
			    } else {
				mesg += "; " + tag;
				sep = ": ";
			    }
			} else sep = "; ";
			if (args[n] != null && args[n].length() > 0)
			    mesg += sep + args[n];
		    }
		}
	    }
	    if (suffix != null && suffix.length() > 0)
		mesg += "; " + suffix;
	} else mesg = null;

	return(mesg);
    }


    final static void
    insertStartLine(YoixObject dict, Throwable t) {

	boolean  valid;
	String   mesg;
	String   source;
	String   startline;
	int      index;
	int      n;

	//
	// Inserts start line information into the error message when t is
	// a ParseException and the line number is avaiable in t's message.
	// Right now it's put there by YoixParser.abort() whenever there's
	// a syntax error and the line where the error happened isn't the
	// same as the line where the statement the parser is working on
	// started.
	//

	if ((mesg = dict.getString(N_MESSAGE)) != null) {
	    if (t instanceof ParseException) {
		if ((startline = t.getMessage()) != null && startline.length() > 0) {
		    valid = true;
		    for (n = 0; n < startline.length(); n++) {
			if (Character.isDigit(startline.charAt(n)) == false) {
			    valid = false;
			    break;
			}
		    }
		    if (valid) {
			if ((index = mesg.lastIndexOf(YoixBodyTag.getSourcePrefix())) >= 0) {
			    //
			    // The next two tests should be unnecessary,
			    // but they probably can't hurt.
			    //
			    if (mesg.lastIndexOf(YoixBodyTag.getLinePrefix()) < index) {
				if (mesg.lastIndexOf(YoixBodyTag.getColumnPrefix()) < index) {
				    source = mesg.substring(index);
				    mesg = mesg.substring(0, index);
				    mesg += "Start Line: " + startline + "; ";
				    mesg += source;
				    dict.putString(N_MESSAGE, mesg);
				}
			    }
			}
		    }
		}
	    }
	}
    }
}

