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
class YoixModuleFactorial extends YoixModule

{

    static String  $MODULENAME = M_FACTORIAL;

    private static final double     MAX_COUNTABLE = 9007199254740992.0;
    private static final String     MAX_COUNTABLE_AS_STRING = "9007199254740992";
    private static final BigInteger MAX_COUNTABLE_AS_BIGINTEGER = new BigInteger(MAX_COUNTABLE_AS_STRING);
    private static final String     MAX_INT_AS_STRING = Integer.toString(Integer.MAX_VALUE);
    private static final BigInteger MAX_INT_AS_BIGINTEGER = new BigInteger(MAX_INT_AS_STRING);

    private static final double MAX_COUNTABLE_SQUARE_ROOT = 94906265.0;
    private static final double MAX_COUNTABLE_CUBE_ROOT = 208063.0;
    private static final double MAX_COUNTABLE_FOURTH_ROOT = 9741.0;
    private static final double MAX_COUNTABLE_FIFTH_ROOT = 1552.0;

    private static final int MAX_INT_SQUARE_ROOT = 46340;
    private static final int MAX_INT_CUBE_ROOT = 1290;
    private static final int MAX_INT_FOURTH_ROOT = 215;
    private static final int MAX_INT_FIFTH_ROOT = 73;

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       null,                 "1",                 $LIST,      $RORO, $MODULENAME,

        //
        // Constants go here...
        //
       
        //
        // Builtins go here...
        //

       "factorial",          "1",                 $BUILTIN,   $LR_X, null,

        //
        // New types go here
        //

    };

    //
    // Hard coded factorials
    //

    private static final Object FACTORIALS[] = {
        //
        // Results through 12! can be stored by a 32 bit signed integer.
        //
    
        new Integer(1),                          // 0!
        new Integer(1),                          // 1!
        new Integer(2),                          // 2!
        new Integer(6),                          // 3!
        new Integer(24),                         // 4!
        new Integer(120),                        // 5!
        new Integer(720),                        // 6!
        new Integer(5040),                       // 7!
        new Integer(40320),                      // 8!
        new Integer(362880),                     // 9!
        new Integer(3628800),                    // 10!
        new Integer(39916800),                   // 11!
        new Integer(479001600),                  // 12!
    
        //
        // Results for 13! through 18! are all less than MAX_COUNTABLE, so they fit
        // in a double without any loss of precision. Even though 19! and 20! can be
        // accurately represented using a double, we decided not to include them in
        // this group because they're bigger than MAX_COUNTABLE.
        //
    
        new Double(6227020800.0),                       // 13!
        new Double(87178291200.0),                      // 14!
        new Double(1307674368000.0),                    // 15!
        new Double(20922789888000.0),                   // 16!
        new Double(355687428096000.0),                  // 17!
        new Double(6402373705728000.0),                 // 18!
    
        //
        // A few more factorials that are represented as strings because they're all
        // bigger than MAX_COUNTABLE. All of them end in lots of zeros (base 10 or 2)
        // so many still fit in a double, but the integers close to them don't.
        //
    
        "121645100408832000",                                                       // 19!
        "2432902008176640000",                                                      // 20!
        "51090942171709440000",                                                     // 21!
        "1124000727777607680000",                                                   // 22!
        "25852016738884976640000",                                                  // 23!
        "620448401733239439360000",                                                 // 24!
        "15511210043330985984000000",                                               // 25!
        "403291461126605635584000000",                                              // 26!
        "10888869450418352160768000000",                                            // 27!
        "304888344611713860501504000000",                                           // 28!
        "8841761993739701954543616000000",                                          // 29!
        "265252859812191058636308480000000",                                        // 30!
        "8222838654177922817725562880000000",                                       // 31!
        "263130836933693530167218012160000000",                                     // 32!
        "8683317618811886495518194401280000000",                                    // 33!
        "295232799039604140847618609643520000000",                                  // 34!
        "10333147966386144929666651337523200000000",                                // 35!
        "371993326789901217467999448150835200000000",                               // 36!
        "13763753091226345046315979581580902400000000",                             // 37!
        "523022617466601111760007224100074291200000000",                            // 38!
        "20397882081197443358640281739902897356800000000",                          // 39!
        "815915283247897734345611269596115894272000000000",                         // 40!
        "33452526613163807108170062053440751665152000000000",                       // 41!
        "1405006117752879898543142606244511569936384000000000",                     // 42!
        "60415263063373835637355132068513997507264512000000000",                    // 43!
        "2658271574788448768043625811014615890319638528000000000",                  // 44!
        "119622220865480194561963161495657715064383733760000000000",                // 45!
        "5502622159812088949850305428800254892961651752960000000000",               // 46!
        "258623241511168180642964355153611979969197632389120000000000",             // 47!
        "12413915592536072670862289047373375038521486354677760000000000",           // 48!
        "608281864034267560872252163321295376887552831379210240000000000",          // 49!
        "30414093201713378043612608166064768844377641568960512000000000000",        // 50!
        "1551118753287382280224243016469303211063259720016986112000000000000",      // 51!
        "80658175170943878571660636856403766975289505440883277824000000000000",     // 52!
    };

    //
    // A transposed version of Pascal's Triangle. It obviously wastes some space, but
    // this was copied directly from a Yoix script implementation where being able to
    // pull the appropriate row vector out of the array once and use it in a loop was
    // particularly useful in several important functions in that script.
    //

    private static final int PASCALS_TRIANGLE_TRANSPOSED[][] = {
        {1, 1, 1, 1, 1,  1,  1,  1,  1,   1,   1,   1,   1,    1,    1,    1,     1,     1,     1,     1,      1,      1,      1,       1,       1,       1,        1},
        {0, 1, 2, 3, 4,  5,  6,  7,  8,   9,  10,  11,  12,   13,   14,   15,    16,    17,    18,    19,     20,     21,     22,      23,      24,      25,       26},
        {0, 0, 1, 3, 6, 10, 15, 21, 28,  36,  45,  55,  66,   78,   91,  105,   120,   136,   153,   171,    190,    210,    231,     253,     276,     300,      325},
        {0, 0, 0, 1, 4, 10, 20, 35, 56,  84, 120, 165, 220,  286,  364,  455,   560,   680,   816,   969,   1140,   1330,   1540,    1771,    2024,    2300,     2600},
        {0, 0, 0, 0, 1,  5, 15, 35, 70, 126, 210, 330, 495,  715, 1001, 1365,  1820,  2380,  3060,  3876,   4845,   5985,   7315,    8855,   10626,   12650,    14950},
        {0, 0, 0, 0, 0,  1,  6, 21, 56, 126, 252, 462, 792, 1287, 2002, 3003,  4368,  6188,  8568, 11628,  15504,  20349,  26334,   33649,   42504,   53130,    65780},
        {0, 0, 0, 0, 0,  0,  1,  7, 28,  84, 210, 462, 924, 1716, 3003, 5005,  8008, 12376, 18564, 27132,  38760,  54264,  74613,  100947,  134596,  177100,   230230},
        {0, 0, 0, 0, 0,  0,  0,  1,  8,  36, 120, 330, 792, 1716, 3432, 6435, 11440, 19448, 31824, 50388,  77520, 116280, 170544,  245157,  346104,  480700,   657800},
        {0, 0, 0, 0, 0,  0,  0,  0,  1,   9,  45, 165, 495, 1287, 3003, 6435, 12870, 24310, 43758, 75582, 125970, 203490, 319770,  490314,  735471, 1081575,  1562275},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   1,  10,  55, 220,  715, 2002, 5005, 11440, 24310, 48620, 92378, 167960, 293930, 497420,  817190, 1307504, 2042975,  3124550},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   1,  11,  66,  286, 1001, 3003,  8008, 19448, 43758, 92378, 184756, 352716, 646646, 1144066, 1961256, 3268760,  5311735},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   1,  12,   78,  364, 1365,  4368, 12376, 31824, 75582, 167960, 352716, 705432, 1352078, 2496144, 4457400,  7726160},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   0,   1,   13,   91,  455,  1820,  6188, 18564, 50388, 125970, 293930, 646646, 1352078, 2704156, 5200300,  9657700},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   0,   0,    1,   14,  105,   560,  2380,  8568, 27132,  77520, 203490, 497420, 1144066, 2496144, 5200300, 10400600},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   0,   0,    0,    1,   15,   120,   680,  3060, 11628,  38760, 116280, 319770,  817190, 1961256, 4457400,  9657700},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   0,   0,    0,    0,    1,    16,   136,   816,  3876,  15504,  54264, 170544,  490314, 1307504, 3268760,  7726160},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   0,   0,    0,    0,    0,     1,    17,   153,   969,   4845,  20349,  74613,  245157,  735471, 2042975,  5311735},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   0,   0,    0,    0,    0,     0,     1,    18,   171,   1140,   5985,  26334,  100947,  346104, 1081575,  3124550},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   0,   0,    0,    0,    0,     0,     0,     1,    19,    190,   1330,   7315,   33649,  134596,  480700,  1562275},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   0,   0,    0,    0,    0,     0,     0,     0,     1,     20,    210,   1540,    8855,   42504,  177100,   657800},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   0,   0,    0,    0,    0,     0,     0,     0,     0,      1,     21,    231,    1771,   10626,   53130,   230230},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   0,   0,    0,    0,    0,     0,     0,     0,     0,      0,      1,     22,     253,    2024,   12650,    65780},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   0,   0,    0,    0,    0,     0,     0,     0,     0,      0,      0,      1,      23,     276,    2300,    14950},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   0,   0,    0,    0,    0,     0,     0,     0,     0,      0,      0,      0,       1,      24,     300,     2600},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   0,   0,    0,    0,    0,     0,     0,     0,     0,      0,      0,      0,       0,       1,      25,      325},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   0,   0,    0,    0,    0,     0,     0,     0,     0,      0,      0,      0,       0,       0,       1,       26},
        {0, 0, 0, 0, 0,  0,  0,  0,  0,   0,   0,   0,   0,    0,    0,    0,     0,     0,     0,     0,      0,      0,      0,       0,       0,       0,        1},
    };

    ///////////////////////////////////
    //
    // YoixModuleFactorial Methods
    //
    ///////////////////////////////////

    public static YoixObject
    factorial(YoixObject arg[]) {

        YoixObject result = null;
        Object     value;
        int        num;

        if (arg[0].isNumber()) {
            if ((value = computeFactorial(arg[0].intValue())) != null) {
                if (value instanceof Number)
                    result = YoixObject.newInt(((Number)value).intValue());
                else if (value instanceof String)
                    result = YoixObject.newString((String)value);
            }
        } else VM.badArgument(0);

        return(result != null ? result : YoixObject.newNull());
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static Object
    computeFactorial(int n) {

        BigInteger value;
        BigInteger next;
        Object     result = null;
        int        m;

        if (n > 0) {
            if (n >= FACTORIALS.length) {
                try {
                    value = new BigInteger(FACTORIALS[FACTORIALS.length - 1].toString(), 10);
                    next = new BigInteger(FACTORIALS.length + "");
                    for (m = next.intValue(); m <= n; m++) {
                        value = value.multiply(next);
                        next.add(BigInteger.ONE);
                    }
                    result = value.toString();
                }
                catch(NumberFormatException e) {
                    result = null;
                }
            } else result = FACTORIALS[n];
        } else result = new Integer((n == 0) ? 1 : 0);

        return(result);
    }
}

