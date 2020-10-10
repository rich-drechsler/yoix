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

abstract
class YoixModuleRE extends YoixModule

{

    static String  $MODULENAME = M_RE;

    static Integer  $CASE_INSENSITIVE = new Integer(CASE_INSENSITIVE);
    static Integer  $SHELL_PATTERN = new Integer(SHELL_PATTERN);
    static Integer  $SINGLE_BYTE = new Integer(SINGLE_BYTE);
    static Integer  $TEXT_PATTERN = new Integer(TEXT_PATTERN);
    static Integer  $RAWSHELL_PATTERN = new Integer(RAWSHELL_PATTERN);

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       null,                 "12",                $LIST,      $RORO, $MODULENAME,
       "CASE_INSENSITIVE",   $CASE_INSENSITIVE,   $INTEGER,   $LR__, null,
       "SHELL_PATTERN",      $SHELL_PATTERN,      $INTEGER,   $LR__, null,
       "SINGLE_BYTE",        $SINGLE_BYTE,        $INTEGER,   $LR__, null,
       "TEXT_PATTERN",       $TEXT_PATTERN,       $INTEGER,   $LR__, null,
       "RAWSHELL_PATTERN",   $RAWSHELL_PATTERN,   $INTEGER,   $LR__, null,

       "gsubsti",            "3",                 $BUILTIN,   $LR_X, null,
       "gvsubsti",           "3",                 $BUILTIN,   $LR_X, null,
       "regexec",            "-2",                $BUILTIN,   $LR_X, null,
       "regexp",             "-1",                $BUILTIN,   $LR_X, null,
       "regsub",             "2",                 $BUILTIN,   $LR_X, null,
       "substi",             "-2",                $BUILTIN,   $LR_X, null,
       "vsubsti",            "-2",                $BUILTIN,   $LR_X, null,

       T_REGEXP,             "4",                 $DICT,      $L___, T_REGEXP,
       null,                 "-1",                $GROWTO,    null,  null,
       N_MAJOR,              $REGEXP,             $INTEGER,   $LR__, null,
       N_MINOR,              "0",                 $INTEGER,   $LR__, null,
       N_PATTERN,            T_STRING,            $NULL,      $RW_,  null,
       N_TYPE,               $SINGLE_BYTE,        $INTEGER,   $RW_,  null,

       T_SUBEXP,             "4",                 $DICT,      $L___, T_SUBEXP,
       N_MAJOR,              $SUBEXP,             $INTEGER,   $LR__, null,
       N_MINOR,              "0",                 $INTEGER,   $LR__, null,
       N_RANGES,             T_ARRAY,             $NULL,      $LR__, null,
       N_TARGET,             T_STRING,            $NULL,      $LR__, null,
    };

    ///////////////////////////////////
    //
    // YoixModuleRE Methods
    //
    ///////////////////////////////////

    public static YoixObject
    gsubsti(YoixObject arg[]) {

	YoixRERegexp  regexp;
	YoixRESubexp  subexp;
	YoixObject    obj = null;
	String        substitute;
	StringBuffer  result;
	String        remainder;
	boolean       initial = true;

	if (arg[0].isString() && arg[0].notNull()) {
	    substitute = arg[0].stringValue();
	    if (arg[1].notNull() && (arg[1].isRegexp() || arg[1].isString())) {
		if (arg[1].isRegexp()) {
		    regexp = (YoixRERegexp)(arg[1].getManagedObject());
		} else {
		    regexp = new YoixRERegexp(arg[1].stringValue());
		}
		if (arg[2].isString() && arg[2].notNull()) {
		    obj = arg[2];
		    remainder = arg[2].stringValue();
		    subexp = new YoixRESubexp();
		    result = new StringBuffer();

		    synchronized(result) {
			while (regexp.exec(remainder, subexp, initial)) {
			    initial = false;
			    obj = null;
			    result.append(subexp.getSource().substring(0, subexp.getSpAt(0)));
			    result.append(YoixRERegexp.regsub(substitute, subexp));
			    //
			    // If the start and end indices are equal we must
			    // have matched "^" or "$" and they'll either be
			    // 0 or remainder.length(). Either way we end up
			    // in an infinite loop if we don't break.
			    //
			    if (subexp.getSpAt(0) == subexp.getEpAt(0)) {
				if (subexp.getSpAt(0) != 0)	// it's already in result
				    remainder = "";
				break;
			    } else remainder = subexp.getSource().substring(subexp.getEpAt(0));
			}
			if (obj == null) {
			    if (remainder.length() > 0)
				result.append(remainder);
			    obj = YoixObject.newString(result.toString());
			}
		    }
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(obj);
    }


    public static YoixObject
    gvsubsti(YoixObject arg[]) {

	YoixRERegexp  regexp;
	YoixRESubexp  subexp;
	YoixObject    obj = null;
	String        substitute;
	StringBuffer  result;
	String        remainder;

	if (arg[0].isString() && arg[0].notNull()) {
	    substitute = arg[0].stringValue();
	    if (arg[1].notNull() && (arg[1].isRegexp() || arg[1].isString())) {
		if (arg[1].isRegexp()) {
		    regexp = (YoixRERegexp)(arg[1].getManagedObject());
		} else {
		    regexp = new YoixRERegexp(arg[1].stringValue());
		}
		if (arg[2].isString() && arg[2].notNull()) {
		    obj = arg[2];
		    remainder = arg[2].stringValue();
		    subexp = new YoixRESubexp();
		    result = new StringBuffer();

		    synchronized(result) {
			while (regexp.exec(remainder, subexp)) {
			    obj = null;
			    result.append(subexp.getSource().substring(0, subexp.getSpAt(0)));
			    result.append(substitute);
			    //
			    // If the start and end indices are equal we must
			    // have matched "^" or "$" and they'll either be
			    // 0 or remainder.length(). Either way we end up
			    // in an infinite loop if we don't break.
			    //
			    if (subexp.getSpAt(0) == subexp.getEpAt(0)) {
				if (subexp.getSpAt(0) != 0)	// it's already in result
				    remainder = "";
				break;
			    } else remainder = subexp.getSource().substring(subexp.getEpAt(0));
			}
			if (obj == null) {
			    if (remainder.length() > 0)
				result.append(remainder);
			    obj = YoixObject.newString(result.toString());
			}
		    }
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(obj);
    }


    public static YoixObject
    regexec(YoixObject arg[]) {

	YoixRERegexp  regexp;
	YoixRESubexp  subexp;
	YoixObject    obj;
	boolean       match = false;
	String        target;

	if (arg.length >= 2 && arg.length <= 3) {
	    if (arg[0].isRegexp() && arg[0].notNull()) {
		regexp = (YoixRERegexp)(arg[0].getManagedObject());
		if (arg[1].isString() && arg[1].notNull()) {
		    target = arg[1].stringValue();
		    if (arg.length == 3) {
			if (arg[2].isSubexp() && arg[2].notNull()) {
			    subexp = (YoixRESubexp)(arg[2].getManagedObject());
			    match = regexp.exec(target, subexp);
			} else VM.badArgument(2);
		    } else match = regexp.exec(target, null);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(match ? YoixObject.newInt(1) : YoixObject.newInt(0));
    }


    public static YoixObject
    regexp(YoixObject arg[]) {

	YoixObject  obj = null;
	YoixObject  data;
	String      pattern;

	if (arg.length >= 1 && arg.length <= 2) {
	    if (arg[0].isString() && arg[0].notNull()) {
		data = VM.getTypeTemplate(T_REGEXP);
		data.put(N_PATTERN, arg[0]);
		if (arg.length >= 2) {
		    if (arg[1].isInteger()) {
			data.putInt(N_TYPE, (REFLAGS_MASK&arg[1].intValue()));
		    } else VM.badArgument(1);
		}
		obj = YoixObject.newRegexp(data);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj == null ? YoixObject.newRegexp() : obj);
    }


    public static YoixObject
    regsub(YoixObject arg[]) {

	YoixRESubexp  subexp;
	YoixObject    obj;
	String        result = null;
	String        target;

	if (arg[0].isString() && arg[0].notNull()) {
	    target = arg[0].stringValue();
	    if (arg[1].isSubexp() && arg[1].notNull()) {
		subexp = (YoixRESubexp)(arg[1].getManagedObject());
		result = YoixRERegexp.regsub(target, subexp);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(result == null ? YoixObject.newString() : YoixObject.newString(result));
    }


    public static YoixObject
    substi(YoixObject arg[]) {

	YoixRERegexp  regexp;
	YoixRESubexp  subexp;
	YoixObject    obj = null;
	String        substitute;
	String        target;

	if (arg.length == 2) {
	    if (arg[0].isString() && arg[0].notNull()) {
		substitute = arg[0].stringValue();
		if (arg[1].isSubexp() && arg[1].notNull()) {
		    subexp = (YoixRESubexp)(arg[1].getManagedObject());
		    if (subexp != null && subexp.getSpAt(0) >= 0) {
			obj = YoixObject.newString(
			    subexp.getSource().substring(0, subexp.getSpAt(0)) +
			    YoixRERegexp.regsub(substitute, subexp) +
			    subexp.getSource().substring(subexp.getEpAt(0))
			);
		    }
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else if (arg.length == 3) {
	    if (arg[0].isString() && arg[0].notNull()) {
		substitute = arg[0].stringValue();
		if (arg[1].notNull() && (arg[1].isRegexp() || arg[1].isString())) {
		    if (arg[1].isRegexp())
			regexp = (YoixRERegexp)(arg[1].getManagedObject());
		    else regexp = new YoixRERegexp(arg[1].stringValue());
		    if (arg[2].isString() && arg[2].notNull()) {
			obj = arg[2];
			target = arg[2].stringValue();
			subexp = new YoixRESubexp();
			if (regexp.exec(target, subexp)) {
			    obj = YoixObject.newString(
				subexp.getSource().substring(0, subexp.getSpAt(0)) +
				YoixRERegexp.regsub(substitute, subexp) +
				subexp.getSource().substring(subexp.getEpAt(0))
			    );
			}
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj);
    }


    public static YoixObject
    vsubsti(YoixObject arg[]) {

	YoixRERegexp  regexp;
	YoixRESubexp  subexp;
	YoixObject    obj = null;
	String        substitute;
	String        target;

	if (arg.length == 2) {
	    if (arg[0].isString() && arg[0].notNull()) {
		substitute = arg[0].stringValue();
		if (arg[1].isSubexp() && arg[1].notNull()) {
		    subexp = (YoixRESubexp)(arg[1].getManagedObject());
		    if (subexp != null && subexp.getSpAt(0) >= 0) {
			obj = YoixObject.newString(
			    subexp.getSource().substring(0, subexp.getSpAt(0)) +
			    substitute +
			    subexp.getSource().substring(subexp.getEpAt(0))
			);
		    }
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else if (arg.length == 3) {
	    if (arg[0].isString() && arg[0].notNull()) {
		substitute = arg[0].stringValue();
		if (arg[1].notNull() && (arg[1].isRegexp() || arg[1].isString())) {
		    if (arg[1].isRegexp())
			regexp = (YoixRERegexp)(arg[1].getManagedObject());
		    else regexp = new YoixRERegexp(arg[1].stringValue());
		    if (arg[2].isString() && arg[2].notNull()) {
			obj = arg[2];
			target = arg[2].stringValue();
			subexp = new YoixRESubexp();
			if (regexp.exec(target, subexp)) {
			    obj = YoixObject.newString(
				subexp.getSource().substring(0, subexp.getSpAt(0)) +
				substitute +
				subexp.getSource().substring(subexp.getEpAt(0))
			    );
			}
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj);
    }
}

