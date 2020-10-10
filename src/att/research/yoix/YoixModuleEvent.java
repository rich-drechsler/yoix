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
class YoixModuleEvent extends YoixModule

    implements YoixConstantsJFC

{

    static String  $MODULENAME = M_EVENT;

    //
    // These are for the dictionary that maps Yoix event handler names to
    // their internal id number. Not very important or used much, but it
    // is a convenient list of the existing Yoix event handler names.
    //
    // NOTE - this had all been done in YoixModuleJFC.java and the entry
    // was called Event, but that name was already claimed by this module,
    // so we decided to change the dictionary name to Handler. Definitely
    // an imcompatible change, but we really doubt the dictionary is used
    // by real applications.
    //

    static Integer  $V_ACTIONPERFORMED = new Integer(V_ACTIONPERFORMED);
    static Integer  $V_ADJUSTCHANGED = new Integer(V_ADJUSTCHANGED);
    static Integer  $V_CARETUPDATE = new Integer(V_CARETUPDATE);
    static Integer  $V_COMPONENTHIDDEN = new Integer(V_COMPONENTHIDDEN);
    static Integer  $V_COMPONENTMOVED = new Integer(V_COMPONENTMOVED);
    static Integer  $V_COMPONENTRESIZED = new Integer(V_COMPONENTRESIZED);
    static Integer  $V_COMPONENTSHOWN = new Integer(V_COMPONENTSHOWN);
    static Integer  $V_DRAGGESTURERECOGNIZED = new Integer(V_DRAGGESTURERECOGNIZED);
    static Integer  $V_DRAGDROPEND = new Integer(V_DRAGDROPEND);
    static Integer  $V_DRAGENTER = new Integer(V_DRAGENTER);
    static Integer  $V_DRAGEXIT = new Integer(V_DRAGEXIT);
    static Integer  $V_DRAGMOUSEMOVED = new Integer(V_DRAGMOUSEMOVED);
    static Integer  $V_DRAGOVER = new Integer(V_DRAGOVER);
    static Integer  $V_DROP = new Integer(V_DROP);
    static Integer  $V_DROPACTIONCHANGED = new Integer(V_DROPACTIONCHANGED);
    static Integer  $V_FOCUSGAINED = new Integer(V_FOCUSGAINED);
    static Integer  $V_FOCUSLOST = new Integer(V_FOCUSLOST);
    static Integer  $V_HYPERLINKACTIVATED = new Integer(V_HYPERLINKACTIVATED);
    static Integer  $V_HYPERLINKENTERED = new Integer(V_HYPERLINKENTERED);
    static Integer  $V_HYPERLINKEXITED = new Integer(V_HYPERLINKEXITED);
    static Integer  $V_INVOCATIONACTION = new Integer(V_INVOCATIONACTION);
    static Integer  $V_INVOCATIONBROWSE = new Integer(V_INVOCATIONBROWSE);
    static Integer  $V_INVOCATIONEDIT = new Integer(V_INVOCATIONEDIT);
    static Integer  $V_INVOCATIONEDITIMPORT = new Integer(V_INVOCATIONEDITIMPORT);
    static Integer  $V_INVOCATIONEDITKEY = new Integer(V_INVOCATIONEDITKEY);
    static Integer  $V_INVOCATIONRUN = new Integer(V_INVOCATIONRUN);
    static Integer  $V_INVOCATIONSELECTION = new Integer(V_INVOCATIONSELECTION);
    static Integer  $V_ITEMCHANGED = new Integer(V_ITEMCHANGED);
    static Integer  $V_KEYPRESSED = new Integer(V_KEYPRESSED);
    static Integer  $V_KEYRELEASED = new Integer(V_KEYRELEASED);
    static Integer  $V_KEYTYPED = new Integer(V_KEYTYPED);
    static Integer  $V_MOUSECLICKED = new Integer(V_MOUSECLICKED);
    static Integer  $V_MOUSEDRAGGED = new Integer(V_MOUSEDRAGGED);
    static Integer  $V_MOUSEENTERED = new Integer(V_MOUSEENTERED);
    static Integer  $V_MOUSEEXITED = new Integer(V_MOUSEEXITED);
    static Integer  $V_MOUSEMOVED = new Integer(V_MOUSEMOVED);
    static Integer  $V_MOUSEPRESSED = new Integer(V_MOUSEPRESSED);
    static Integer  $V_MOUSERELEASED = new Integer(V_MOUSERELEASED);
    static Integer  $V_MOUSEWHEELMOVED = new Integer(V_MOUSEWHEELMOVED);
    static Integer  $V_STATECHANGED = new Integer(V_STATECHANGED);
    static Integer  $V_TEXTCHANGED = new Integer(V_TEXTCHANGED);
    static Integer  $V_VALUECHANGED = new Integer(V_VALUECHANGED);
    static Integer  $V_WINDOWACTIVATED = new Integer(V_WINDOWACTIVATED);
    static Integer  $V_WINDOWCLOSED = new Integer(V_WINDOWCLOSED);
    static Integer  $V_WINDOWCLOSING = new Integer(V_WINDOWCLOSING);
    static Integer  $V_WINDOWDEACTIVATED = new Integer(V_WINDOWDEACTIVATED);
    static Integer  $V_WINDOWDEICONIFIED = new Integer(V_WINDOWDEICONIFIED);
    static Integer  $V_WINDOWICONIFIED = new Integer(V_WINDOWICONIFIED);
    static Integer  $V_WINDOWOPENED = new Integer(V_WINDOWOPENED);

    //
    // The three uppercase KeyEvent constants defined in the module were
    // only added for convenience for the invocationEditKey event. Values
    // are also easy to get using HandlerID, so the new constants aren't
    // absolutely necessary.
    //

    static String  $N_KEYPRESSED_UPPER = N_KEYPRESSED.toUpperCase();
    static String  $N_KEYRELEASED_UPPER = N_KEYRELEASED.toUpperCase();
    static String  $N_KEYTYPED_UPPER = N_KEYTYPED.toUpperCase();

    //
    // Miscellaneous constants.
    //

    static Integer  $INVALIDEVENT = new Integer(V_INVALIDEVENT);
    static Integer  $TEXTCHANGE = new Integer(YOIX_TEXTCHANGE);
    static Double   $DEFAULTPADDING = new Double(72.0/12);

    static Object  $module[] = {
    //
    // NAME                    ARG                      COMMAND     MODE   REFERENCE
    // ----                    ---                      -------     ----   ---------
       null,                   "5",                     $LIST,      $RORO, $MODULENAME,

       "listEventHandlers",    "",                      $BUILTIN,   $LR_X, null,

       $N_KEYPRESSED_UPPER,    $V_KEYPRESSED,           $INTEGER,   $LR__, null,
       $N_KEYRELEASED_UPPER,   $V_KEYRELEASED,          $INTEGER,   $LR__, null,
       $N_KEYTYPED_UPPER,      $V_KEYTYPED,             $INTEGER,   $LR__, null,

       "HandlerID",            "49",                    $DICT,      $RORO, "Handlers",
       N_ACTIONPERFORMED,      $V_ACTIONPERFORMED,      $INTEGER,   $LR__, null,
       N_ADJUSTCHANGED,        $V_ADJUSTCHANGED,        $INTEGER,   $LR__, null,
       N_CARETUPDATE,          $V_CARETUPDATE,          $INTEGER,   $LR__, null,
       N_COMPONENTHIDDEN,      $V_COMPONENTHIDDEN,      $INTEGER,   $LR__, null,
       N_COMPONENTMOVED,       $V_COMPONENTMOVED,       $INTEGER,   $LR__, null,
       N_COMPONENTRESIZED,     $V_COMPONENTRESIZED,     $INTEGER,   $LR__, null,
       N_COMPONENTSHOWN,       $V_COMPONENTSHOWN,       $INTEGER,   $LR__, null,
       N_DRAGGESTURERECOGNIZED,$V_DRAGGESTURERECOGNIZED,$INTEGER,   $LR__, null,
       N_DRAGDROPEND,          $V_DRAGDROPEND,          $INTEGER,   $LR__, null,
       N_DRAGENTER,            $V_DRAGENTER,            $INTEGER,   $LR__, null,
       N_DRAGEXIT,             $V_DRAGEXIT,             $INTEGER,   $LR__, null,
       N_DRAGMOUSEMOVED,       $V_DRAGMOUSEMOVED,       $INTEGER,   $LR__, null,
       N_DRAGOVER,             $V_DRAGOVER,             $INTEGER,   $LR__, null,
       N_DROP,                 $V_DROP,                 $INTEGER,   $LR__, null,
       N_DROPACTIONCHANGED,    $V_DROPACTIONCHANGED,    $INTEGER,   $LR__, null,
       N_FOCUSGAINED,          $V_FOCUSGAINED,          $INTEGER,   $LR__, null,
       N_FOCUSLOST,            $V_FOCUSLOST,            $INTEGER,   $LR__, null,
       N_HYPERLINKACTIVATED,   $V_HYPERLINKACTIVATED,   $INTEGER,   $LR__, null,
       N_HYPERLINKENTERED,     $V_HYPERLINKENTERED,     $INTEGER,   $LR__, null,
       N_HYPERLINKEXITED,      $V_HYPERLINKEXITED,      $INTEGER,   $LR__, null,
       N_INVOCATIONACTION,     $V_INVOCATIONACTION,     $INTEGER,   $LR__, null,
       N_INVOCATIONBROWSE,     $V_INVOCATIONBROWSE,     $INTEGER,   $LR__, null,
       N_INVOCATIONEDIT,       $V_INVOCATIONEDIT,       $INTEGER,   $LR__, null,
       N_INVOCATIONEDITIMPORT, $V_INVOCATIONEDITIMPORT, $INTEGER,   $LR__, null,
       N_INVOCATIONEDITKEY,    $V_INVOCATIONEDITKEY,    $INTEGER,   $LR__, null,
       N_INVOCATIONRUN,        $V_INVOCATIONRUN,        $INTEGER,   $LR__, null,
       N_INVOCATIONSELECTION,  $V_INVOCATIONSELECTION,  $INTEGER,   $LR__, null,
       N_ITEMCHANGED,          $V_ITEMCHANGED,          $INTEGER,   $LR__, null,
       N_KEYPRESSED,           $V_KEYPRESSED,           $INTEGER,   $LR__, null,
       N_KEYRELEASED,          $V_KEYRELEASED,          $INTEGER,   $LR__, null,
       N_KEYTYPED,             $V_KEYTYPED,             $INTEGER,   $LR__, null,
       N_MOUSECLICKED,         $V_MOUSECLICKED,         $INTEGER,   $LR__, null,
       N_MOUSEDRAGGED,         $V_MOUSEDRAGGED,         $INTEGER,   $LR__, null,
       N_MOUSEENTERED,         $V_MOUSEENTERED,         $INTEGER,   $LR__, null,
       N_MOUSEEXITED,          $V_MOUSEEXITED,          $INTEGER,   $LR__, null,
       N_MOUSEMOVED,           $V_MOUSEMOVED,           $INTEGER,   $LR__, null,
       N_MOUSEPRESSED,         $V_MOUSEPRESSED,         $INTEGER,   $LR__, null,
       N_MOUSERELEASED,        $V_MOUSERELEASED,        $INTEGER,   $LR__, null,
       N_MOUSEWHEELMOVED,      $V_MOUSEWHEELMOVED,      $INTEGER,   $LR__, null,
       N_STATECHANGED,         $V_STATECHANGED,         $INTEGER,   $LR__, null,
       N_TEXTCHANGED,          $V_TEXTCHANGED,          $INTEGER,   $LR__, null,
       N_VALUECHANGED,         $V_VALUECHANGED,         $INTEGER,   $LR__, null,
       N_WINDOWACTIVATED,      $V_WINDOWACTIVATED,      $INTEGER,   $LR__, null,
       N_WINDOWCLOSED,         $V_WINDOWCLOSED,         $INTEGER,   $LR__, null,
       N_WINDOWCLOSING,        $V_WINDOWCLOSING,        $INTEGER,   $LR__, null,
       N_WINDOWDEACTIVATED,    $V_WINDOWDEACTIVATED,    $INTEGER,   $LR__, null,
       N_WINDOWDEICONIFIED,    $V_WINDOWDEICONIFIED,    $INTEGER,   $LR__, null,
       N_WINDOWICONIFIED,      $V_WINDOWICONIFIED,      $INTEGER,   $LR__, null,
       N_WINDOWOPENED,         $V_WINDOWOPENED,         $INTEGER,   $LR__, null,
       null,                   $MODULENAME,             $PUT,       null,  null,

    //
    // Event type definitions.
    //

       T_EVENT,                T_DICT,                  $NULL,      $L___, T_EVENT,

       T_ACTIONEVENT,          "3",                     $DICT,      $L___, T_ACTIONEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_COMMAND,              T_STRING,                $NULL,      $RW_,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,
       N_MODIFIERS,            "0",                     $INTEGER,   $RW_,  null,

       T_ADJUSTMENTEVENT,      "4",                     $DICT,      $L___, T_ADJUSTMENTEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,
       N_ORIENTATION,          "0",                     $INTEGER,   $RW_,  null,
       N_TYPE,                 "0",                     $INTEGER,   $RW_,  null,
       N_VALUE,                "0",                     $INTEGER,   $RW_,  null,

       T_CARETEVENT,           "3",                     $DICT,      $L___, T_CARETEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_DOT,                  "0",                     $INTEGER,   $RW_,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,
       N_MARK,                 "0",                     $INTEGER,   $RW_,  null,

       T_CHANGEEVENT,          "1",                     $DICT,      $L___, T_CHANGEEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,

       T_COMPONENTEVENT,       "1",                     $DICT,      $L___, T_COMPONENTEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,

       T_FOCUSEVENT,           "2",                     $DICT,      $L___, T_FOCUSEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,
       N_TEMPORARY,            $FALSE,                  $INTEGER,   $RW_,  null,

       T_HYPERLINKEVENT,       "2",                     $DICT,      $L___, T_HYPERLINKEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_HREF,                 T_STRING,                $NULL,      $RW_,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,

       T_INVOCATIONEVENT,      "1",                     $DICT,      $L___, T_INVOCATIONEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       null,                   "-1",                    $GROWTO,    null,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,

       T_ITEMEVENT,            "3",                     $DICT,      $L___, T_ITEMEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,
       N_ITEM,                 T_OBJECT,                $NULL,      $RW_,  null,
       N_STATE,                $FALSE,                  $INTEGER,   $RW_,  null,

       T_KEYEVENT,             "8",                     $DICT,      $L___, T_KEYEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_CONSUMED,             $FALSE,                  $INTEGER,   $RW_,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,
       N_KEYCHAR,              "0",                     $INTEGER,   $RW_,  null,
       N_KEYCODE,              "0",                     $INTEGER,   $RW_,  null,
       N_KEYSTRING,            T_STRING,                $NULL,      $RW_,  null,
       N_MODIFIERS,            "0",                     $INTEGER,   $RW_,  null,
       N_MODIFIERSDOWN,        "0",                     $INTEGER,   $RW_,  null,
       N_WHEN,                 "0",                     $DOUBLE,    $RW_,  null,

       T_LISTSELECTIONEVENT,   "4",                     $DICT,      $L___, T_LISTSELECTIONEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_FIRSTINDEX,           "-1",                    $INTEGER,   $RW_,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,
       N_LASTINDEX,            "-1",                    $INTEGER,   $RW_,  null,
       N_SEQUENCE,             "0",                     $INTEGER,   $RW_,  null,

       T_MOUSEEVENT,           "11",                    $DICT,      $L___, T_MOUSEEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_BUTTON,               "0",                     $INTEGER,   $RW_,  null,
       N_CLICKCOUNT,           "0",                     $INTEGER,   $RW_,  null,
       N_CONSUMED,             $FALSE,                  $INTEGER,   $RW_,  null,
       N_COORDINATES,          T_POINT,                 $NULL,      $RW_,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,
       N_LOCATION,             T_POINT,                 $DECLARE,   $RW_,  null,
       N_MODIFIERS,            "0",                     $INTEGER,   $RW_,  null,
       N_MODIFIERSDOWN,        "0",                     $INTEGER,   $RW_,  null,
       N_POPUPTRIGGER,         $FALSE,                  $INTEGER,   $RW_,  null,
       N_PRESSED,              "0",                     $INTEGER,   $RW_,  null,
       N_WHEN,                 "0",                     $DOUBLE,    $RW_,  null,

       T_MOUSEWHEELEVENT,      "15",                    $DICT,      $L___, T_MOUSEWHEELEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_CLICKCOUNT,           "0",                     $INTEGER,   $RW_,  null,
       N_CONSUMED,             $FALSE,                  $INTEGER,   $RW_,  null,
       N_COORDINATES,          T_POINT,                 $NULL,      $RW_,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,
       N_LOCATION,             T_POINT,                 $DECLARE,   $RW_,  null,
       N_MODIFIERS,            "0",                     $INTEGER,   $RW_,  null,
       N_MODIFIERSDOWN,        "0",                     $INTEGER,   $RW_,  null,
       N_POPUPTRIGGER,         $FALSE,                  $INTEGER,   $RW_,  null,
       N_PRESSED,              "0",                     $INTEGER,   $RW_,  null,
       N_SCROLLAMOUNT,         "0",                     $INTEGER,   $RW_,  null,
       N_SCROLLTYPE,           "0",                     $INTEGER,   $RW_,  null,
       N_UNITSTOSCROLL,        "0",                     $INTEGER,   $RW_,  null,
       N_WHEELROTATION,        "0",                     $INTEGER,   $RW_,  null,
       N_WHEN,                 "0",                     $DOUBLE,    $RW_,  null,
       N_WHENNEXT,             "0",                     $DOUBLE,    $RW_,  null,

       T_PAINTEVENT,           "2",                     $DICT,      $L___, T_PAINTEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,
       N_UPDATERECT,           T_RECTANGLE,             $NULL,      $RW_,  null,

       T_TEXTEVENT,            "5",                     $DICT,      $L___, T_TEXTEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,
       N_LENGTH,               "-1",                    $INTEGER,   $RW_,  null,
       N_OFFSET,               "-1",                    $INTEGER,   $RW_,  null,
       N_SIZE,                 "-1",                    $INTEGER,   $RW_,  null,
       N_TYPE,                 $TEXTCHANGE,             $INTEGER,   $RW_,  null,

       T_TREESELECTIONEVENT,   "2",                     $DICT,      $L___, T_TREESELECTIONEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,
       N_ITEMS,                T_ARRAY,                 $NULL,      $RW_,  null,

       T_WINDOWEVENT,          "1",                     $DICT,      $L___, T_WINDOWEVENT,
       null,                   T_EVENT,                 $TYPENAME,  null,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,

    //
    // These are a little different and currently don't set their type to
    // T_EVENT. Done so our low level Java code can distinguish between
    // DragSourceEvent and DropTargetEvent event handlers that share names
    // by using the callable(argv[]) method. Our DragGestureEvent doesn't
    // suffer from the problem, but we decided that it also wouldn't claim
    // to be a T_EVENT. There's a small chance we'll eventually make some
    // changes to our Java code that will let these use the T_EVENT type.
    //
    // We decided to make T_DRAGSOURCEEVENT and T_DROPTARGETEVENT growable
    // so the three overloaded event handlers could skip some easy checks
    // if the want. For example, dragEnter() could store a cursor in its
    // event argument, do some other work, and then return TRUE or FALSE,
    // it might never need to check its argument type.
    //

       T_DRAGGESTUREEVENT,     "14",                    $DICT,      $L___, T_DRAGGESTUREEVENT,
       null,                   "-1",                    $GROWTO,    null,  null,
       N_ANCHOR,               $YOIX_SOUTH,             $INTEGER,   $RW_,  null,
       N_COORDINATES,          T_POINT,                 $NULL,      $RW_,  null,
       N_CURSOR,               T_OBJECT,                $NULL,      $RW_,  null,
       N_DRAGIMAGESUPPORTED,   $FALSE,                  $INTEGER,   $RW_,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,
       N_LOCATION,             T_POINT,                 $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,          T_DIMENSION,             $NULL,      $RW_,  null,
       N_MODIFIERS,            "0",                     $INTEGER,   $RW_,  null,
       N_MODIFIERSDOWN,        "0",                     $INTEGER,   $RW_,  null,
       N_OFFSET,               T_POINT,                 $NULL,      $RW_,  null,
       N_PADDING,              $DEFAULTPADDING,         $DOUBLE,    $RW_,  null,
       N_SCREENLOCATION,       T_POINT,                 $NULL,      $RW_,  null,
       N_VISUAL,               T_OBJECT,                $NULL,      $RW_,  null,
       N_WHEN,                 "0",                     $DOUBLE,    $RW_,  null,

       T_DRAGSOURCEEVENT,      "7",                     $DICT,      $L___, T_DRAGSOURCEEVENT,
       null,                   "-1",                    $GROWTO,    null,  null,
       N_ACTION,               "0",                     $INTEGER,   $RW_,  null,
       N_COORDINATES,          T_POINT,                 $NULL,      $RW_,  null,
       N_CURSOR,               T_OBJECT,                $NULL,      $RW_,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,
       N_LOCATION,             T_POINT,                 $NULL,      $RW_,  null,
       N_SCREENLOCATION,       T_POINT,                 $NULL,      $RW_,  null,
       N_SUCCEEDED,            $FALSE,                  $INTEGER,   $RW_,  null,

       T_DROPTARGETEVENT,      "8",                     $DICT,      $L___, T_DROPTARGETEVENT,
       null,                   "-1",                    $GROWTO,    null,  null,
       N_ACTION,               "0",                     $INTEGER,   $RW_,  null,
       N_COORDINATES,          T_POINT,                 $NULL,      $RW_,  null,
       N_DRAGOWNER,            $FALSE,                  $INTEGER,   $RW_,  null,
       N_ID,                   $INVALIDEVENT,           $OBJECT,    $RW_,  null,
       N_LOCATION,             T_POINT,                 $NULL,      $RW_,  null,
       N_MIMETYPES,            T_ARRAY,                 $NULL,      $RW_,  null,
       N_SCREENLOCATION,       T_POINT,                 $NULL,      $RW_,  null,
       N_TRANSFERABLE,         T_OBJECT,                $NULL,      $RW_,  null,
    };

    static Object  extracted[] = {
	"Handlers",
    };

    ///////////////////////////////////
    //
    // YoixModuleEvent Methods
    //
    ///////////////////////////////////

    public static YoixObject
    listEventHandlers(YoixObject arg[]) {

	YoixObject  dict;
	YoixObject  result = null;
	YoixObject  yobj;
	Object      body;
	String      keys[];
	String      list[];
	String      newkeys[];
	int         bit;
	int         bits;
	int         len;
	int         n;

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 0 || arg[0].isNull()) {
		dict = (YoixObject)extracted[0];
		len = dict.length(); 
		keys = new String[len];
		for (n = 0; n < len; n++)
		    keys[n] = dict.name(n);
	    } else if (arg[0].isComponent() || arg[0].isString()) {
		if (arg[0].isString())
		    yobj = YoixMake.yoixType(arg[0].stringValue());
		else yobj = YoixMake.yoixType(arg[0].typename());
		if (yobj != null && yobj.notNull() && yobj.isComponent()) {
		    body = yobj.body();
		    if (body instanceof YoixBodyComponentSwing) {
			bits = ((YoixBodyComponentSwing)body).addListeners(YoixBodyComponent.NEXTLISTENER - 1);
		    } else if (body instanceof YoixBodyComponentAWT) {
			bits = ((YoixBodyComponentAWT)body).addListeners(YoixBodyComponent.NEXTLISTENER - 1);
		    } else {
			VM.abort(INTERNALERROR);
			bits = 0; // for compiler
		    }
		    bits = (YoixBodyComponent.NEXTLISTENER - 1) & ~bits;
		    keys = new String[0];
		    if (bits != 0) {
			if (body instanceof YoixBodyComponentSwing)
			    n = ((YoixBodyComponentSwing)body).removeListeners(bits);
			else // can just use else by this time (error detected above)
			    n = ((YoixBodyComponentAWT)body).removeListeners(bits);
			if (n != 0)
			    VM.abort(INTERNALERROR, "Mask", n); // removeListeners didn't remove them all
			for (bit = 1; bit != YoixBodyComponent.NEXTLISTENER; bit <<= 1) {
			    if ((bit&bits) != 0) {
				if ((list = YoixBodyComponent.listenerList(bit)) != null) {
				    newkeys = new String[keys.length + list.length];
				    System.arraycopy(keys, 0, newkeys, 0, keys.length);
				    System.arraycopy(list, 0, newkeys, keys.length, list.length);
				    keys = newkeys;
				}
			    }
			}
		    }
		    len = keys.length;
		} else if (yobj == null || yobj.notNull()) {
		    VM.abort(BADVALUE, new String[] { "typename" }); // yoixType should have complained already
		    // for compiler
		    keys = null;
		    len = 0;
		} else {
		    // not a component
		    keys = null;
		    len = 0;
		}
	    } else {
		VM.badArgument(0);
		// for compiler
		keys = null;
		len = 0;
	    }
	    if (keys != null) {
		if (len > 1)
		    YoixMiscQsort.sort(keys, 1);
		result = YoixObject.newArray(len);
		for (n = 0; n < len; n++)
		    result.putString(n, keys[n]);
	    }
	} else VM.badCall();

	return(result == null ? YoixObject.newNull() : result);
    }
}

