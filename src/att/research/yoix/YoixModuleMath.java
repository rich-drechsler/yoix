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
import java.math.*;

abstract
class YoixModuleMath extends YoixModule

{

    static String  $MODULENAME = M_MATH;

    static Double   $M_E = new Double(Math.E);
    static Integer  $MAX_CHARACTER = new Integer(Character.MAX_VALUE);
    static Double   $MAX_DOUBLE = new Double(Double.MAX_VALUE);
    static Integer  $MAX_INT = new Integer(Integer.MAX_VALUE);
    static Integer  $MIN_CHARACTER = new Integer(Character.MIN_VALUE);
    static Double   $MIN_DOUBLE = new Double(Double.MIN_VALUE);
    static Integer  $MIN_INT = new Integer(Integer.MIN_VALUE);
    static Double   $NEGATIVE_INFINITY = new Double(Double.NEGATIVE_INFINITY);
    static Double   $M_PI = new Double(Math.PI);
    static Double   $POSITIVE_INFINITY = new Double(Double.POSITIVE_INFINITY);
    static Integer  $ROUND_CEILING = new Integer(BigDecimal.ROUND_CEILING);
    static Integer  $ROUND_DOWN = new Integer(BigDecimal.ROUND_DOWN);
    static Integer  $ROUND_FLOOR = new Integer(BigDecimal.ROUND_FLOOR);
    static Integer  $ROUND_HALF_DOWN = new Integer(BigDecimal.ROUND_HALF_DOWN);
    static Integer  $ROUND_HALF_EVEN = new Integer(BigDecimal.ROUND_HALF_EVEN);
    static Integer  $ROUND_HALF_UP = new Integer(BigDecimal.ROUND_HALF_UP);
    static Integer  $ROUND_UNNECESSARY = new Integer(BigDecimal.ROUND_UNNECESSARY);
    static Integer  $ROUND_UP = new Integer(BigDecimal.ROUND_UP);

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       null,                 "66",                $LIST,      $RORO, $MODULENAME,
       "E",                  $M_E,                $DOUBLE,    $LR__, null,
       "MAX_CHARACTER",      $MAX_CHARACTER,      $INTEGER,   $LR__, null,
       "MAX_DOUBLE",         $MAX_DOUBLE,         $DOUBLE,    $LR__, null,
       "MAX_INT",            $MAX_INT,            $INTEGER,   $LR__, null,
       "MIN_CHARACTER",      $MIN_CHARACTER,      $INTEGER,   $LR__, null,
       "MIN_DOUBLE",         $MIN_DOUBLE,         $DOUBLE,    $LR__, null,
       "MIN_INT",            $MIN_INT,            $INTEGER,   $LR__, null,
       "NaN",                $NAN,                $DOUBLE,    $LR__, null,
       "NEGATIVE_INFINITY",  $NEGATIVE_INFINITY,  $DOUBLE,    $LR__, null,
       "PI",                 $M_PI,               $DOUBLE,    $LR__, null,
       "POSITIVE_INFINITY",  $POSITIVE_INFINITY,  $DOUBLE,    $LR__, null,
       "ROUND_CEILING",      $ROUND_CEILING,      $INTEGER,   $LR__, null,
       "ROUND_DOWN",         $ROUND_DOWN,         $INTEGER,   $LR__, null,
       "ROUND_FLOOR",        $ROUND_FLOOR,        $INTEGER,   $LR__, null,
       "ROUND_HALF_DOWN",    $ROUND_HALF_DOWN,    $INTEGER,   $LR__, null,
       "ROUND_HALF_EVEN",    $ROUND_HALF_EVEN,    $INTEGER,   $LR__, null,
       "ROUND_HALF_UP",      $ROUND_HALF_UP,      $INTEGER,   $LR__, null,
       "ROUND_UNNECESSARY",  $ROUND_UNNECESSARY,  $INTEGER,   $LR__, null,
       "ROUND_UP",           $ROUND_UP,           $INTEGER,   $LR__, null,

       "abs",                "1",                 $BUILTIN,   $LR_X, null,
       "acos",               "1",                 $BUILTIN,   $LR_X, null,
       "asin",               "1",                 $BUILTIN,   $LR_X, null,
       "atan",               "1",                 $BUILTIN,   $LR_X, null,
       "atan2",              "2",                 $BUILTIN,   $LR_X, null,
       "bigAbs",             "1",                 $BUILTIN,   $LR_X, null,
       "bigAdd",             "2",                 $BUILTIN,   $LR_X, null,
       "bigCompareTo",       "2",                 $BUILTIN,   $LR_X, null,
       "bigDivide",          "-2",                $BUILTIN,   $LR_X, null,
       "bigFromRadix",       "2",                 $BUILTIN,   $LR_X, null,
       "bigMax",             "2",                 $BUILTIN,   $LR_X, null,
       "bigMin",             "2",                 $BUILTIN,   $LR_X, null,
       "bigMultiply",        "2",                 $BUILTIN,   $LR_X, null,
       "bigNegate",          "1",                 $BUILTIN,   $LR_X, null,
       "bigSubtract",        "2",                 $BUILTIN,   $LR_X, null,
       "bigToRadix",         "2",                 $BUILTIN,   $LR_X, null,
       "cbrt",               "1",                 $BUILTIN,   $LR_X, null,
       "ceil",               "1",                 $BUILTIN,   $LR_X, null,
       "cos",                "1",                 $BUILTIN,   $LR_X, null,
       "cosh",               "1",                 $BUILTIN,   $LR_X, null,
       "exp",                "1",                 $BUILTIN,   $LR_X, null,
       "expm1",              "1",                 $BUILTIN,   $LR_X, null,
       "floor",              "1",                 $BUILTIN,   $LR_X, null,
       "hypot",              "2",                 $BUILTIN,   $LR_X, null,
       "ieeeRemainder",      "2",                 $BUILTIN,   $LR_X, null,
       "iceil",              "1",                 $BUILTIN,   $LR_X, null,
       "ifloor",             "1",                 $BUILTIN,   $LR_X, null,
       "irint",              "1",                 $BUILTIN,   $LR_X, null,
       "iround",             "1",                 $BUILTIN,   $LR_X, null,
       "log",                "1",                 $BUILTIN,   $LR_X, null,
       "log10",              "1",                 $BUILTIN,   $LR_X, null,
       "log1p",              "1",                 $BUILTIN,   $LR_X, null,
       "max",                "2",                 $BUILTIN,   $LR_X, null,
       "min",                "2",                 $BUILTIN,   $LR_X, null,
       "pow",                "2",                 $BUILTIN,   $LR_X, null,
       "random",             "0",                 $BUILTIN,   $LR_X, null,
       "rint",               "1",                 $BUILTIN,   $LR_X, null,
       "round",              "1",                 $BUILTIN,   $LR_X, null,
       "signum",             "1",                 $BUILTIN,   $LR_X, null,
       "sin",                "1",                 $BUILTIN,   $LR_X, null,
       "sinh",               "1",                 $BUILTIN,   $LR_X, null,
       "sqrt",               "1",                 $BUILTIN,   $LR_X, null,
       "tan",                "1",                 $BUILTIN,   $LR_X, null,
       "tanh",               "1",                 $BUILTIN,   $LR_X, null,
       "toDegrees",          "1",                 $BUILTIN,   $LR_X, null,
       "toRadians",          "1",                 $BUILTIN,   $LR_X, null,
       "ulp",                "1",                 $BUILTIN,   $LR_X, null,

       T_RANDOM,                 "12",                $DICT,      $L___, T_RANDOM,
       N_MAJOR,                  $RANDOM,             $INTEGER,   $LR__, null,
       N_MINOR,                  "0",                 $INTEGER,   $LR__, null,
       N_BINARY,                 $FALSE,              $INTEGER,   $LR__, null,
       N_BYTES,                  "1",                 $BUILTIN,   $LR_X, null,
       N_DOUBLE,                 $NAN,                $DOUBLE,    $LR__, null,
       N_GAUSSIAN,               $NAN,                $DOUBLE,    $LR__, null,
       N_INT,                    $NAN,                $INTEGER,   $LR__, null,
       N_PROVIDER,               T_DICT,              $NULL,      $LR__, null,
       N_RANGE,                  "1",                 $BUILTIN,   $LR_X, null,
       N_SECURE,                 $FALSE,              $INTEGER,   $RW_,  null,
       N_SEED,                   T_OBJECT,            $NULL,      $RW_,  null,
       N_SPECIFICATION,          T_OBJECT,            $NULL,      $RW_,  null,
    };

    private static final double  LN10 = Math.log(10.0);

    ///////////////////////////////////
    //
    // YoixModuleMath Methods
    //
    ///////////////////////////////////

    public static YoixObject
    abs(YoixObject arg[]) {

	Number  value = null;

	if (arg[0].isNumber()) {
	    if (arg[0].isInteger()) {
		value = new Integer(Math.abs(arg[0].intValue()));
		if (value.intValue() < 0)
		    value = new Double(Math.abs(arg[0].doubleValue()));
	    } else value = new Double(Math.abs(arg[0].doubleValue()));
	} else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    acos(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.acos(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    asin(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.asin(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    atan(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.atan(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    atan2(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber()) {
	    if (arg[1].isNumber())
		value = Math.atan2(arg[0].doubleValue(), arg[1].doubleValue());
	    else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    bigAbs(YoixObject arg[]) {

	Number  num = null;

	if (arg[0].isString()) {
	    num = bigNumber(arg[0], 0);
	    if (num instanceof BigDecimal)
		num = ((BigDecimal)num).abs();
	    else num = ((BigInteger)num).abs();
	} else VM.badArgument(0);

	return(YoixObject.newString(num.toString()));
    }


    public static YoixObject
    bigAdd(YoixObject arg[]) {

	Number  left;
	Number  right;
	Number  num = null;

	if (arg[0].isString()) {
	    if (arg[1].isString()) {
		left = bigNumber(arg[0], 0);
		if (left instanceof BigInteger) {
		    right = bigNumber(arg[1], 1);
		    if (right instanceof BigDecimal)
			num = bigDecimal(arg[0], 0).add((BigDecimal)right);
		    else num = ((BigInteger)left).add((BigInteger)right);
		} else num = ((BigDecimal)left).add(bigDecimal(arg[1], 1));
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newString(num.toString()));
    }


    public static YoixObject
    bigCompareTo(YoixObject arg[]) {

	Number  left;
	Number  right;
	int     value = 0;

	if (arg[0].isString()) {
	    if (arg[1].isString()) {
		left = bigNumber(arg[0], 0);
		if (left instanceof BigInteger) {
		    right = bigNumber(arg[1], 1);
		    if (right instanceof BigDecimal)
			value = bigDecimal(arg[0], 0).compareTo((BigDecimal)right);
		    else value = ((BigInteger)left).compareTo((BigInteger)right);
		} else value = ((BigDecimal)left).compareTo(bigDecimal(arg[1], 1));
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    bigDivide(YoixObject arg[]) {

	Number  left;
	Number  right;
	Number  num = null;
	int     round;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isString()) {
		if (arg[1].isString()) {
		    if (arg.length == 2 || arg[2].isInteger()) {
			if (arg.length == 3)
			    round = arg[2].intValue();
			else round = BigDecimal.ROUND_HALF_UP;
			left = bigNumber(arg[0], 0);
			try {
			    if (left instanceof BigInteger) {
				right = bigNumber(arg[1], 1);
				if (right instanceof BigDecimal)
				    num = bigDecimal(arg[0], 0).divide((BigDecimal)right, round);
				else num = ((BigInteger)left).divide((BigInteger)right);
			    } else num = ((BigDecimal)left).divide(bigDecimal(arg[1], 1), round);
			}
			catch(ArithmeticException  e) {
			    VM.badArgument(1);
			}
			catch(IllegalArgumentException  e) {
			    VM.badArgument(2);
			}
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(num.toString()));
    }


    public static YoixObject
    bigFromRadix(YoixObject arg[]) {

	BigInteger  nbr = null;
	int         radix;

	if (arg[0].isString()) {
	    if (arg[1].isInteger()) {
		radix = arg[1].intValue();
		if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
		    VM.abort(BADVALUE, 1);
		try {
		    nbr = new BigInteger(arg[0].stringValue(), radix);
		}
		catch(Exception e) {
		    VM.abort(BADVALUE, 0);
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newString(nbr.toString()));
    }


    public static YoixObject
    bigMax(YoixObject arg[]) {

	Number  left;
	Number  right;
	Number  num = null;

	if (arg[0].isString()) {
	    if (arg[1].isString()) {
		left = bigNumber(arg[0], 0);
		if (left instanceof BigInteger) {
		    right = bigNumber(arg[1], 1);
		    if (right instanceof BigDecimal)
			num = bigDecimal(arg[0], 0).max((BigDecimal)right);
		    else num = ((BigInteger)left).max((BigInteger)right);
		} else num = ((BigDecimal)left).max(bigDecimal(arg[1], 1));
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newString(num.toString()));
    }


    public static YoixObject
    bigMin(YoixObject arg[]) {

	Number  left;
	Number  right;
	Number  num = null;

	if (arg[0].isString()) {
	    if (arg[1].isString()) {
		left = bigNumber(arg[0], 0);
		if (left instanceof BigInteger) {
		    right = bigNumber(arg[1], 1);
		    if (right instanceof BigDecimal)
			num = bigDecimal(arg[0], 0).min((BigDecimal)right);
		    else num = ((BigInteger)left).min((BigInteger)right);
		} else num = ((BigDecimal)left).min(bigDecimal(arg[1], 1));
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newString(num.toString()));
    }


    public static YoixObject
    bigMultiply(YoixObject arg[]) {

	Number  left;
	Number  right;
	Number  num = null;

	if (arg[0].isString()) {
	    if (arg[1].isString()) {
		left = bigNumber(arg[0], 0);
		if (left instanceof BigInteger) {
		    right = bigNumber(arg[1], 1);
		    if (right instanceof BigDecimal)
			num = bigDecimal(arg[0], 0).multiply((BigDecimal)right);
		    else num = ((BigInteger)left).multiply((BigInteger)right);
		} else num = ((BigDecimal)left).multiply(bigDecimal(arg[1], 1));
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newString(num.toString()));
    }


    public static YoixObject
    bigNegate(YoixObject arg[]) {

	Number  num = null;

	if (arg[0].isString()) {
	    num = bigNumber(arg[0], 0);
	    if (num instanceof BigDecimal)
		num = ((BigDecimal)num).negate();
	    else num = ((BigInteger)num).negate();
	} else VM.badArgument(0);

	return(YoixObject.newString(num.toString()));
    }


    public static YoixObject
    bigSubtract(YoixObject arg[]) {

	Number  left;
	Number  right;
	Number  num = null;

	if (arg[0].isString()) {
	    if (arg[1].isString()) {
		left = bigNumber(arg[0], 0);
		if (left instanceof BigInteger) {
		    right = bigNumber(arg[1], 1);
		    if (right instanceof BigDecimal)
			num = bigDecimal(arg[0], 0).subtract((BigDecimal)right);
		    else num = ((BigInteger)left).subtract((BigInteger)right);
		} else num = ((BigDecimal)left).subtract(bigDecimal(arg[1], 1));
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newString(num.toString()));
    }


    public static YoixObject
    bigToRadix(YoixObject arg[]) {

	Number  nbr = null;
	int     radix = 10;

	if (arg[0].isString()) {
	    if (arg[1].isInteger()) {
		nbr = bigNumber(arg[0], 0);
		if (nbr instanceof BigInteger) {
		    radix = arg[1].intValue();
		    if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
			VM.abort(BADVALUE, 1);
		} else VM.abort(BADVALUE, 0);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newString(((BigInteger)nbr).toString(radix)));
    }


    public static YoixObject
    cbrt(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.cbrt(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    ceil(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.ceil(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    cos(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.cos(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    cosh(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.cosh(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    exp(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.exp(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    expm1(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.expm1(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    floor(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.floor(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    hypot(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber()) {
	    if (arg[1].isNumber()) {
		value = Math.hypot(arg[0].doubleValue(), arg[1].doubleValue());
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    ieeeRemainder(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber()) {
	    if (arg[1].isNumber()) {
		value = Math.IEEEremainder(arg[0].doubleValue(), arg[1].doubleValue());
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    iceil(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.ceil(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newInt((int)(value + 0.5)));
    }


    public static YoixObject
    ifloor(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.floor(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newInt((int)(value + 0.5)));
    }


    public static YoixObject
    irint(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.rint(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newInt((int)(value + 0.5)));
    }


    public static YoixObject
    iround(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.floor(arg[0].doubleValue() + 0.5);
	else VM.badArgument(0);

	return(YoixObject.newInt((int)value));
    }


    public static YoixObject
    log(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.log(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    log10(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.log(arg[0].doubleValue())/LN10;
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    log1p(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.log1p(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    max(YoixObject arg[]) {

	Number  value = null;

	if (arg[0].isNumber()) {
	    if (arg[1].isNumber()) {
		if (arg[0].isDouble() || arg[1].isDouble())
		    value = new Double(Math.max(arg[0].doubleValue(), arg[1].doubleValue()));
		else value = new Integer(Math.max(arg[0].intValue(), arg[1].intValue()));
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    min(YoixObject arg[]) {

	Number  value = null;

	if (arg[0].isNumber()) {
	    if (arg[1].isNumber()) {
		if (arg[0].isDouble() || arg[1].isDouble())
		    value = new Double(Math.min(arg[0].doubleValue(), arg[1].doubleValue()));
		else value = new Integer(Math.min(arg[0].intValue(), arg[1].intValue()));
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    pow(YoixObject arg[]) {

	double  value = 0;

	//
	// Java documentation claims they throw an exception when base
	// is 0.0 and exponent is less than 0.0, or when base is less
	// than 0.0 and exponent is not a whole number. I haven't seen
	// the exceptions on our SGI (version 1.1.6), but we catch them
	// anyway and try to match the return values we normally get.
	//

	if (arg[0].isNumber()) {
	    if (arg[1].isNumber()) {
		try {
		    value = Math.pow(arg[0].doubleValue(), arg[1].doubleValue());
		}

		catch(ArithmeticException e) {
		    if (arg[0].doubleValue() == 0 && arg[1].doubleValue() < 0)
			value = Double.POSITIVE_INFINITY;
		    else value = Double.NaN;
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    random(YoixObject arg[]) {

	return(YoixObject.newNumber(Math.random()));
    }


    public static YoixObject
    rint(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.rint(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    round(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.floor(arg[0].doubleValue() + 0.5);
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    signum(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.signum(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    sin(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.sin(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    sinh(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.sinh(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    sqrt(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.sqrt(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    tan(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.tan(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    tanh(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.tanh(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    toDegrees(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.toDegrees(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    toRadians(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.toRadians(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    ulp(YoixObject arg[]) {

	double  value = 0;

	if (arg[0].isNumber())
	    value = Math.ulp(arg[0].doubleValue());
	else VM.badArgument(0);

	return(YoixObject.newNumber(value));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static BigDecimal
    bigDecimal(YoixObject obj, int argn) {

	BigDecimal  num = null;
	String      str;

	try {
	    if (obj.isString())
		str = obj.stringValue(true);
	    else str = obj.toString().trim();
	    num = new BigDecimal(str);
	}
	catch(NumberFormatException e) {
	    VM.badArgument(argn);
	}

	return(num);
    }


    private static BigInteger
    bigInteger(YoixObject obj, int argn) {

	BigInteger  num = null;
	String      str;

	try {
	    if (obj.isString())
		str = obj.stringValue(true);
	    else str = obj.toString().trim();
	    if (str.indexOf('.') >= 0)
		num = (new BigDecimal(str)).toBigInteger();
	    else num = new BigInteger(str);
	}
	catch(NumberFormatException e) {
	    VM.badArgument(argn);
	}

	return(num);
    }


    private static Number
    bigNumber(YoixObject obj, int argn) {

	Number  num = null;
	String  str;

	try {
	    if (obj.isString()) {
		str = obj.stringValue(true);
		if (str.indexOf('.') >= 0)
		    num = new BigDecimal(str);
		else num = new BigInteger(str);
	    } else if (obj.isInteger())
		num = new BigInteger(obj.stringValue(true).trim());
	    else if (obj.isDouble())
		num = new BigDecimal(obj.doubleValue());
	    else VM.badArgument(argn);
	}
	catch(NumberFormatException e) {
	    VM.badArgument(argn);
	}

	return(num);
    }


    private static BigDecimal
    bigDecimal(String str, int argn) {

	BigDecimal  num = null;

	try {
	    num = new BigDecimal(str);
	}
	catch(NumberFormatException e) {
	    VM.badArgument(argn);
	}

	return(num);
    }


    private static BigInteger
    bigInteger(String str, int argn) {

	BigInteger  num = null;

	try {
	    num = new BigInteger(str);
	}
	catch(NumberFormatException e) {
	    VM.badArgument(argn);
	}

	return(num);
    }
}

