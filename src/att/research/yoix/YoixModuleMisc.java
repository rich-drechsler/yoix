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
class YoixModuleMisc extends YoixModule

{

    //
    // Miscellaneous stuff, mostly typedefs, that we don't always want
    // to load.
    //

    static String  $MODULENAME = null;

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       T_HASHTABLE,          "16",                $DICT,      $L___, T_HASHTABLE,
       N_MAJOR,              $HASHTABLE,          $INTEGER,   $LR__, null,
       N_MINOR,              "0",                 $INTEGER,   $LR__, null,
       N_CLONE,              T_OBJECT,            $NULL,      $LR__, null,
       N_CONTAINS,           T_CALLABLE,          $NULL,      $L__X, null,
       N_CONTAINSVALUE,      T_CALLABLE,          $NULL,      $L__X, null,
       N_FIND,               T_CALLABLE,          $NULL,      $L__X, null,
       N_FINDALL,            T_CALLABLE,          $NULL,      $L__X, null,
       N_GET,                T_CALLABLE,          $NULL,      $L__X, null,
       N_KEYS,               T_ARRAY,             $NULL,      $LR__, null,
       N_PAIRS,              T_ARRAY,             $NULL,      $LRW_, null,
       N_PUT,                T_CALLABLE,          $NULL,      $L__X, null,
       N_PUTALL,             T_CALLABLE,          $NULL,      $L__X, null,
       N_REMOVE,             T_CALLABLE,          $NULL,      $L__X, null,
       N_REMOVEVALUE,        T_CALLABLE,          $NULL,      $L__X, null,
       N_SIZE,               "-1",                $INTEGER,   $RW_,  null,
       N_VALUES,             T_ARRAY,             $NULL,      $LR__, null,

       T_VECTOR,             "19",                $DICT,      $L___, T_VECTOR,
       N_MAJOR,              $VECTOR,             $INTEGER,   $LR__, null,
       N_MINOR,              "0",                 $INTEGER,   $LR__, null,
       N_CLONE,              T_OBJECT,            $NULL,      $LR__, null,
       N_CONTAINS,           T_CALLABLE,          $NULL,      $L__X, null,
       N_CONTAINSVALUE,      T_CALLABLE,          $NULL,      $L__X, null,
       N_ELEMENTCOUNT,       "0",                 $INTEGER,   $LR__, null,
       N_ELEMENTS,           T_ARRAY,             $NULL,      $LR__, null,
       N_FIRSTVALUE,         T_OBJECT,            $NULL,      $LRW_, null,
       N_FIND,               T_CALLABLE,          $NULL,      $L__X, null,
       N_FINDALL,            T_CALLABLE,          $NULL,      $L__X, null,
       N_GET,                T_CALLABLE,          $NULL,      $L__X, null,
       N_INSERT,             T_CALLABLE,          $NULL,      $L__X, null,
       N_LASTVALUE,          T_OBJECT,            $NULL,      $LRW_, null,
       N_PUT,                T_CALLABLE,          $NULL,      $L__X, null,
       N_PUTALL,             T_CALLABLE,          $NULL,      $L__X, null,
       N_REMOVE,             T_CALLABLE,          $NULL,      $L__X, null,
       N_REMOVEVALUE,        T_CALLABLE,          $NULL,      $L__X, null,
       N_SIZE,               "-1",                $INTEGER,   $RW_,  null,
       N_VALUES,             T_ARRAY,             $NULL,      $LRW_, null,
    };
}

