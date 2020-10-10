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
class YoixOption

    implements YoixConstantsError

{

    //
    // This class is designed for the YoixBodyOption class or to create a
    // local variable that can be used by a method that processes command
    // line options. The public fields provide easy access to the results,
    // which we think is appropriate for command line option parsing, but
    // they obviously also mean this isn't a thread-safe class.
    // 
    // NOTE - all versions of getopt() now take an argv[] argument that's
    // an array of Objects, so optarg has also been changed to an Object.
    // That means the caller will often have to cast optarg to a String.
    //

    public Object  optarg = null;
    public String  optstr = "-+";
    public String  optword = null;
    public String  opterror = null;
    public String  optstatus[] = null;		// error info
    public char    optchar = '-';
    public int     optind = 0;

    private String  optstring = "";		// abbreviation - for errors
    private int     optletter = -1;
    private int     optoffset = -1;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    YoixOption() {

	this(0);
    }


    public
    YoixOption(int start) {

	optind = start;
    }

    ///////////////////////////////////
    //
    // YoixOption Methods
    //
    ///////////////////////////////////

    public final int
    getopt(Object argv[], String letters) {

	return(getopt(argv.length, argv, letters, null));
    }


    public final int
    getopt(Object argv[], String words[]) {

	return(getopt(argv.length, argv, null, words));
    }


    public final int
    getopt(Object argv[], String letters, String words[]) {

	return(getopt(argv.length, argv, letters, words));
    }


    public final int
    getopt(int argc, Object argv[], String letters) {

	return(getopt(argc, argv, letters, null));
    }


    public final int
    getopt(int argc, Object argv[], String words[]) {

	return(getopt(argc, argv, null, words));
    }


    public final synchronized int
    getopt(int argc, Object argv[], String letters, String words[]) {

	String  arg;
	String  pattern;

	optchar = '-';
	optarg = null;
	opterror = null;
	optstatus = null;
	optword = null;
	optletter = -1;
	optstring = "";

	if ((arg = getCurrentArgument(argv)) != null && arg.length() > 0) {
	    if (optstr.indexOf(arg.charAt(0)) >= 0) {
		optchar = arg.charAt(0);
		if (arg.equals("--") == false) {
		    pattern = new String(new char[] {optchar, optchar});
		    if (arg.startsWith(pattern) == false) {
			optstring += optchar;
			optoffset = Math.max(optoffset, 1);
			if (letters == null)
			    handleLongOption(argc, argv, words, arg);
			else handleLetterOption(argc, argv, letters, arg);
		    } else {
			optstring += pattern;
			optoffset = Math.max(optoffset, 2);
			if (words == null)
			    handleLetterOption(argc, argv, letters, arg);
			else handleLongOption(argc, argv, words, arg);
		    }
		} else setNextArgument();
	    }
	}
	return(optletter);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    error(String mesg) {

	opterror = mesg;
	optstatus = new String[] {OFFENDINGINFO, mesg};
	optletter = '?';
	setNextArgument();
    }


    private String
    getCurrentArgument(Object argv[]) {

	Object  arg;
	String  value = null;
	int     index = optind;

	if (index >= 0 && index < argv.length) {
	    if ((arg = argv[index]) != null) {
		if (arg instanceof YoixObject) {
		    if (((YoixObject)arg).isString() && ((YoixObject)arg).notNull())
			value = ((YoixObject)arg).stringValue();
		} else if (arg instanceof String)
		    value = (String)arg;
	    }
	}
	return(value);
    }


    private String
    getLongOptionInfo(String name, String words[]) {

	String  info = null;
	String  word;
	char    ch;
	int     length;
	int     n;

	//
	// Currently requires an exact match, which means the option info
	// selected from words must have name entry that's terminated by
	// a ' ', ':', or '=' character. Not hard to accept abbreviations,
	// but we decided against implementing them right now.
	//

	if (name != null && words != null) {
	    if ((length = name.length()) > 0 && words.length > 0) {
		for (n = 0; info == null && n < words.length; n++) {
		    if ((word = words[n]) != null) {
			if (word.startsWith(name)) {
			    if (word.length() > length) {
				ch = word.charAt(length);
				if (ch == ':' || ch == '=')
				    info = word;
			    } else info = word;
			}
		    }
		}
	    }
	}
	return(info);
    }


    private void
    handleLetterOption(int argc, Object argv[], String letters, String arg) {

	char  letter;
	int   length;
	int   index;

	if (optoffset < arg.length()) {
	    letter = arg.charAt(optoffset++);
	    arg = arg.substring(optoffset);
	    optstring += letter;
	    if (letters != null && (length = letters.length()) > 0) {
		if ((index = letters.indexOf(letter)) >= 0) {
		    optletter = letter;
		    if (index < length - 1 && letters.charAt(index + 1) == ':') {
			if (arg.length() == 0) {
			    if (index >= length - 2 || letters.charAt(index + 2) != ':') {
				if (optind < argc - 1) {
				    setNextArgument();
				    optarg = argv[optind];
				    setNextArgument();
				} else error(optstring + " option needs an argument");
			    } else setNextArgument();
			} else {
			    optarg = arg;
			    setNextArgument();
			}
		    } else {
			if (arg.length() == 0)
			    setNextArgument();
		    }
		} else error(optstring + " is not a recognized option");
	    } else error(optstring + " is not a recognized option");
	} else error(optstring + " should be followed by an option letter");
    }


    private void
    handleLongOption(int argc, Object argv[], String words[], String arg) {

	String  word;
	String  info;
	char    letter;
	int     index;

	if (optoffset < arg.length()) {
	    letter = '-';
	    if ((index = arg.indexOf("=", optoffset)) > 0) {
		word = arg.substring(optoffset, index);
		arg = arg.substring(index + 1);
	    } else {
		word = arg.substring(optoffset);
		arg = "";
	    }
	    optstring += word;
	    if (words != null && words.length > 0) {
		if ((info = getLongOptionInfo(word, words)) != null) {
		    optword = word;
		    optletter = letter;
		    if ((index = info.indexOf('=')) > 0) {
			if (index < info.length() - 1)
			    optletter = info.charAt(index + 1);
		    }
		    if ((index = info.indexOf(':')) > 0) {
			if (arg.length() == 0) {
			    if (index >= info.length() - 1 || info.charAt(index + 1) != ':') {
				if (optind < argc - 1) {
				    setNextArgument();
				    optarg = argv[optind];
				    setNextArgument();
				} else error(optstring + " option needs an argument");
			    } else setNextArgument();
			} else {
			    optarg = arg;
			    setNextArgument();
			}
		    } else {
			if (arg.length() == 0)
			    setNextArgument();
			else error(optstring + " option doesn't take an argument");
		    }
		} else error(optstring + " is not a recognized option");
	    } else error(optstring + " is not a recognized option");
	} else error(optstring + " should be followed by an option letter");
    }


    private void
    setNextArgument() {

	optind++;
	optoffset = -1;
    }
}

