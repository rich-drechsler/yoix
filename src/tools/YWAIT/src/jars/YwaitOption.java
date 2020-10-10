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

public
class YwaitOption {

    //
    // Option parsing class that we borrowed from the 2.2.1 Yoix source code
    // and modified for our purposes (e.g., we eliminted long option support
    // and turned optarg into a String).
    //

    public String  optarg = null;
    public String  optstr = "-+";
    public String  optword = null;
    public String  opterror = null;
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
    YwaitOption() {

	optind = 0;
    }

    ///////////////////////////////////
    //
    // YwaitOption Methods
    //
    ///////////////////////////////////

    public final int
    getopt(String argv[], String letters) {

	return(getopt(argv.length, argv, letters));
    }


    public final synchronized int
    getopt(int argc, String argv[], String letters) {

	String  arg;
	String  pattern;

	optchar = '-';
	optarg = null;
	opterror = null;
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
			handleLetterOption(argc, argv, letters, arg);
		    } else {
			optstring += pattern;
			optoffset = Math.max(optoffset, 2);
			handleLetterOption(argc, argv, letters, arg);
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
		if (arg instanceof String)
		    value = (String)arg;
	    }
	}
	return(value);
    }


    private void
    handleLetterOption(int argc, String argv[], String letters, String arg) {

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
    setNextArgument() {

	optind++;
	optoffset = -1;
    }
}

