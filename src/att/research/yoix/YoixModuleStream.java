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
class YoixModuleStream extends YoixModule

    implements YoixConstantsStream

{

    static String  $MODULENAME = null;

    static Integer  $FLUSHWRITES = new Integer(FLUSHWRITES);

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       T_STRINGSTREAM,       "22",                $DICT,      $L___, T_STRINGSTREAM,
       null,                 T_STREAM,            $TYPENAME,  null,  null,
       N_MAJOR,              $STREAM,             $INTEGER,   $LR__, null,
       N_MINOR,              $STRINGSTREAM,       $INTEGER,   $LR__, null,
       N_MODE,               "r",                 $OBJECT,    $RW_,  null,
       N_NAME,               T_STRING,            $NULL,      $RW_,  null,
       N_AUTOREADY,          $FALSE,              $INTEGER,   $RW_,  null,
       N_BUFSIZE,            $BUFSIZ,             $INTEGER,   $RW_,  null,
       N_CALLBACK,           T_CALLABLE,          $NULL,      $L__X, null,
       N_CHECKSUM,           "0",                 $DOUBLE,    $RW_,  null,
       N_CIPHER,             T_OBJECT,            $NULL,      $RW_,  null,
       N_ENCODING,           T_STRING,            $NULL,      $RW_,  null,
       N_FILTERS,            "0",                 $INTEGER,   $RW_,  null,
       N_FLUSHMODE,          $FLUSHWRITES,        $INTEGER,   $RW_,  null,
       N_FULLNAME,           T_STRING,            $NULL,      $LR__,  null,
       N_INTERRUPTED,        "0",                 $INTEGER,   $RW_,  null,
       N_MARKSUPPORTED,      $FALSE,              $INTEGER,   $LR__, null,
       N_NEXTBUF,            "",                  $STRING,    $RW_,  null,
       N_NEXTCHAR,           "-1",                $INTEGER,   $RW_,  null,
       N_NEXTENTRY,          T_ZIPENTRY,          $NULL,      $RW_,  null,
       N_NEXTLINE,           "",                  $STRING,    $RW_,  null,
       N_OPEN,               $FALSE,              $INTEGER,   $RW_,  null,
       N_READY,              "-1",                $INTEGER,   $LR__, null,
       N_SIZE,               "-1",                $DOUBLE,    $LR__, null,

       T_URL,                "38",                $DICT,      $L___, T_URL,
       null,                 T_STREAM,            $TYPENAME,  null,  null,
       N_MAJOR,              $STREAM,             $INTEGER,   $LR__, null,
       N_MINOR,              $URL,                $INTEGER,   $LR__, null,
       N_MODE,               "r",                 $OBJECT,    $RW_,  null,
       N_NAME,               T_STRING,            $NULL,      $RW_,  null,
       N_AUTOREADY,          $FALSE,              $INTEGER,   $RW_,  null,
       N_BUFSIZE,            $BUFSIZ,             $INTEGER,   $RW_,  null,
       N_CALLBACK,           T_CALLABLE,          $NULL,      $L__X, null,
       N_CHECKSUM,           "0",                 $DOUBLE,    $RW_,  null,
       N_CIPHER,             T_OBJECT,            $NULL,      $RW_,  null,
       N_CONNECTTIMEOUT,     $NAN,                $DOUBLE,    $RW_,  null,
       N_ENCODING,           T_STRING,            $NULL,      $RW_,  null,
       N_FILE,               T_STRING,            $NULL,      $LR__, null,
       N_FILTERS,            "0",                 $INTEGER,   $RW_,  null,
       N_FLUSHMODE,          $FLUSHWRITES,        $INTEGER,   $RW_,  null,
       N_FULLNAME,           T_STRING,            $NULL,      $LR__, null,
       N_HOST,               T_STRING,            $NULL,      $LR__, null,
       N_IFMODIFIEDSINCE,    "0",                 $OBJECT,    $RW_,  null,
       N_INTERRUPTED,        "0",                 $INTEGER,   $RW_,  null,
       N_MARKSUPPORTED,      $FALSE,              $INTEGER,   $LR__, null,
       N_NEXTBUF,            "",                  $STRING,    $RW_,  null,
       N_NEXTCHAR,           "-1",                $INTEGER,   $RW_,  null,
       N_NEXTENTRY,          T_ZIPENTRY,          $NULL,      $RW_,  null,
       N_NEXTLINE,           "",                  $STRING,    $RW_,  null,
       N_OPEN,               $FALSE,              $INTEGER,   $RW_,  null,
       N_PORT,               "-1",                $INTEGER,   $LR__, null,
       N_PROTOCOL,           T_STRING,            $NULL,      $LR__, null,
       N_PROXY,              T_OBJECT,            $NULL,      $RW_,  null,
       N_READTIMEOUT,        $NAN,                $DOUBLE,    $RW_,  null,
       N_READY,              "-1",                $INTEGER,   $LR__, null,
       N_REQUESTHEADER,      T_OBJECT,            $NULL,      $RW_,  null,
       N_REQUESTMETHOD,      T_STRING,            $NULL,      $RW_,  null,
       N_RESPONSECODE,       "-1",                $INTEGER,   $LR__, null,
       N_RESPONSEERROR,      T_STRING,            $NULL,      $LR__, null,
       N_RESPONSEHEADER,     T_DICT,              $NULL,      $LR__, null,
       N_RESPONSEKEYCASE,    "0",                 $INTEGER,   $RW_,  null,
       N_SIZE,               "-1",                $DOUBLE,    $LR__, null,
       N_USECACHES,          $FALSE,              $INTEGER,   $RW_,  null,
       N_USINGPROXY,         $FALSE,              $INTEGER,   $LR__, null,

       T_ZIPENTRY,           "10",                $DICT,      $L___, T_ZIPENTRY,
       N_MAJOR,              $ZIPENTRY,           $INTEGER,   $LR__, null,
       N_MINOR,              "0",                 $INTEGER,   $LR__, null,
       N_COMMENT,            "",                  $STRING,    $RW_,  null,
       N_COMPRESSEDSIZE,     "-1",                $DOUBLE,    $LR__, null,
       N_CRC,                "-1",                $DOUBLE,    $RW_,  null,
       N_EXTRA,              "",                  $STRING,    $RW_,  null,
       N_DEFLATED,           $TRUE,               $INTEGER,   $RW_,  null,
       N_NAME,               "",                  $STRING,    $RW_,  null,
       N_SIZE,               "-1",                $DOUBLE,    $RW_,  null,
       N_TIMESTAMP,          "-1",                $DOUBLE,    $RW_,  null,
    };
}

