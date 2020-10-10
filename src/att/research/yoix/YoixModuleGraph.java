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
import java.util.*;

abstract
class YoixModuleGraph extends YoixModule

    implements YoixConstantsGraph

{

    static String  $MODULENAME = M_GRAPH;

    static Integer  $BFS = new Integer(BFS);
    static Integer  $CREATE = new Integer(CREATE);
    static Integer  $DELETE = new Integer(DELETE);
    static Integer  $DFS = new Integer(DFS);
    static Integer  $DIRECTED = new Integer(GRAPH_DIRECTED);
    static Integer  $DOT = new Integer(DOT_TEXTUAL);
    static Integer  $DRAW_LAYOUT = new Integer(DRAW_LAYOUT);
    static Integer  $DRAW_XDOT = new Integer(DRAW_XDOT);
    static Integer  $GRAPH_EDGE = new Integer(GRAPH_EDGE);
    static Integer  $LAYOUT_FORCE = new Integer(LAYOUT_FORCE);
    static Integer  $LAYOUT_FORCE_BOUND = new Integer(LAYOUT_FORCE_BOUND);
    static Integer  $EDGEDFLT = new Integer(EDGEDFLT);
    static Integer  $FORWARD = new Integer(GRAPH_FORWARD);
    static Integer  $GRAPH_GRAPH = new Integer(GRAPH_GRAPH);
    static Integer  $GRAPHDFLT = new Integer(GRAPHDFLT);
    static Integer  $GRAPH_NODE = new Integer(GRAPH_NODE);
    static Integer  $NODEDFLT = new Integer(NODEDFLT);
    static Integer  $REPLACE = new Integer(REPLACE);
    static Integer  $REVERSE = new Integer(GRAPH_REVERSE);
    static Integer  $SCOPED = new Integer(SCOPED);
    static Integer  $STRICT = new Integer(GRAPH_STRICT);
    static Integer  $WALK = new Integer(WALK);
    static Integer  $XML = new Integer(XML_TEXTUAL);

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       null,                 "29",                $LIST,      $RORO, $MODULENAME,
       "BFS",                $BFS,                $INTEGER,   $LR__, null,
       "CREATE",             $CREATE,             $INTEGER,   $LR__, null,
       "DELETE",             $DELETE,             $INTEGER,   $LR__, null,
       "DFS",                $DFS,                $INTEGER,   $LR__, null,
       "DIRECTED",           $DIRECTED,           $INTEGER,   $LR__, null,
       "DOT",                $DOT,                $INTEGER,   $LR__, null,
       "DRAW_LAYOUT",        $DRAW_LAYOUT,        $INTEGER,   $LR__, null,
       "DRAW_XDOT",          $DRAW_XDOT,          $INTEGER,   $LR__, null,
       "EDGE",               $GRAPH_EDGE,         $INTEGER,   $LR__, null,
       "EDGEDFLT",           $EDGEDFLT,           $INTEGER,   $LR__, null,
       "FORWARD",            $FORWARD,            $INTEGER,   $LR__, null,
       "GRAPH",              $GRAPH_GRAPH,        $INTEGER,   $LR__, null,
       "GRAPHDFLT",          $GRAPHDFLT,          $INTEGER,   $LR__, null,
       "LAYOUT_FORCE",       $LAYOUT_FORCE,       $INTEGER,   $LR__, null,
       "LAYOUT_FORCE_BOUND", $LAYOUT_FORCE_BOUND, $INTEGER,   $LR__, null,
       "NODE",               $GRAPH_NODE,         $INTEGER,   $LR__, null,
       "NODEDFLT",           $NODEDFLT,           $INTEGER,   $LR__, null,
       "REPLACE",            $REPLACE,            $INTEGER,   $LR__, null,
       "REVERSE",            $REVERSE,            $INTEGER,   $LR__, null,
       "SCOPED",             $SCOPED,             $INTEGER,   $LR__, null,
       "STRICT",             $STRICT,             $INTEGER,   $LR__, null,
       "WALK",               $WALK,               $INTEGER,   $LR__, null,
       "XML",                $XML,                $INTEGER,   $LR__, null,
       "countElements",      "-1",                $BUILTIN,   $LR_X, null,
       "dotGraph",           "1",                 $BUILTIN,   $LR_X, null,
       "dotGraphToText",     "-1",                $BUILTIN,   $LR_X, null,
       "dotGraphToYDAT",     "-1",                $BUILTIN,   $LR_X, null,
       "listElements",       "-1",                $BUILTIN,   $LR_X, null,
       "xmlGraph",           "1",                 $BUILTIN,   $LR_X, null,

       T_EDGE,               "14",                $DICT,      $L___, T_EDGE,
       null,                 "-1",                $GROWTO,    null,  null,
       null,                 T_ELEMENT,           $TYPENAME,  null,  null,
       N_MAJOR,              $ELEMENT,            $INTEGER,   $LR__, null,
       N_MINOR,              $EDGE,               $INTEGER,   $LR__, null,
       N_ATTRIBUTE,          T_CALLABLE,          $NULL,      $L__X, null,
       N_ATTRIBUTES,         T_DICT,              $NULL,      $RW_,  null,
       N_BFS,                T_CALLABLE,          $NULL,      $L__X, null,
       N_DFS,                T_CALLABLE,          $NULL,      $L__X, null,
       N_FLAGS,              "0",                 $INTEGER,   $RW_,  null,
       N_HEAD,               T_ELEMENT,           $NULL,      $RW_,  null,
       N_NAME,               T_STRING,            $NULL,      $RW_,  null,
       N_PARENT,             T_ELEMENT,           $NULL,      $RW_,  null,
       N_ROOT,               T_ELEMENT,           $NULL,      $LR__, null,
       N_TAIL,               T_ELEMENT,           $NULL,      $RW_,  null,
       N_TEXT,               T_CALLABLE,          $NULL,      $L__X, null,
       N_WALK,               T_CALLABLE,          $NULL,      $L__X, null,

       T_GRAPH,              "17",                $DICT,      $L___, T_GRAPH,
       null,                 "-1",                $GROWTO,    null,  null,
       null,                 T_ELEMENT,           $TYPENAME,  null,  null,
       N_MAJOR,              $ELEMENT,            $INTEGER,   $LR__, null,
       N_MINOR,              $GRAPH,              $INTEGER,   $LR__, null,
       N_ATTRIBUTE,          T_CALLABLE,          $NULL,      $L__X, null,
       N_ATTRIBUTES,         T_DICT,              $NULL,      $RW_,  null,
       N_BFS,                T_CALLABLE,          $NULL,      $L__X, null,
       N_DFS,                T_CALLABLE,          $NULL,      $L__X, null,
       N_EDGEDEFAULTS,       T_DICT,              $NULL,      $RW_,  null,
       N_ELEMENT,            T_CALLABLE,          $NULL,      $L__X, null,
       N_FLAGS,              "0",                 $INTEGER,   $RW_,  null,
       N_GRAPHDEFAULTS,      T_DICT,              $NULL,      $RW_,  null,
       N_NAME,               T_STRING,            $NULL,      $RW_,  null,
       N_NODEDEFAULTS,       T_DICT,              $NULL,      $RW_,  null,
       N_PARENT,             T_ELEMENT,           $NULL,      $RW_,  null,
       N_ROOT,               T_ELEMENT,           $NULL,      $LR__, null,
       N_TEXT,               T_CALLABLE,          $NULL,      $L__X, null,
       N_WALK,               T_CALLABLE,          $NULL,      $L__X, null,

       T_NODE,               "13",                $DICT,      $L___, T_NODE,
       null,                 "-1",                $GROWTO,    null,  null,
       null,                 T_ELEMENT,           $TYPENAME,  null,  null,
       N_MAJOR,              $ELEMENT,            $INTEGER,   $LR__, null,
       N_MINOR,              $NODE,               $INTEGER,   $LR__, null,
       N_ATTRIBUTE,          T_CALLABLE,          $NULL,      $L__X, null,
       N_ATTRIBUTES,         T_DICT,              $NULL,      $RW_,  null,
       N_BFS,                T_CALLABLE,          $NULL,      $L__X, null,
       N_DFS,                T_CALLABLE,          $NULL,      $L__X, null,
       N_FLAGS,              "0",                 $INTEGER,   $RW_,  null,
       N_NAME,               T_STRING,            $NULL,      $RW_,  null,
       N_PARENT,             T_ELEMENT,           $NULL,      $RW_,  null,
       N_ROOT,               T_ELEMENT,           $NULL,      $LR__, null,
       N_TEXT,               T_CALLABLE,          $NULL,      $L__X, null,
       N_WALK,               T_CALLABLE,          $NULL,      $L__X, null,

       T_GRAPHOBSERVER,      "9",                 $DICT,      $L___, T_GRAPHOBSERVER,
       N_MAJOR,              $GRAPHOBSERVER,      $INTEGER,   $LR__, null,
       N_MINOR,              "0",                 $INTEGER,   $LR__, null,
       N_GRAPH,              T_ELEMENT,           $NULL,      $RW_,  null,
       N_INTERRUPTED,        $FALSE,              $INTEGER,   $RW_,  null,
       N_LAYOUTMANAGER,      T_CALLABLE,          $NULL,      $L__X, null,
       N_TEXT,               T_CALLABLE,          $NULL,      $L__X, null,
       N_UPDATE,             T_CALLABLE,          $NULL,      $L__X, null,
       N_WAIT,               T_CALLABLE,          $NULL,      $L__X, null,
       N_WALK,               T_CALLABLE,          $NULL,      $L__X, null,
    };

    ///////////////////////////////////
    //
    // YoixModuleGraph Methods
    //
    ///////////////////////////////////

    public static YoixObject
    countElements(YoixObject arg[]) {

	YoixGraphElement  elem = null;
	int               types = GRAPH_NODE|GRAPH_EDGE|GRAPH_GRAPH;
	int               count = 0;

	if (arg.length <= 2) {
	    if (arg[0].isGraph()) {
		if (arg[0].notNull()) {
		    elem = ((YoixBodyElement)arg[0].body()).getElement();
		    if (elem != null) {
			if (arg.length == 2) {
			    if (arg[1].isInteger())
				types &= arg[1].intValue();
			    else if (arg[1].notNull() && arg[1].isString())
				types &= YoixBodyElement.parseGraphTypes(arg[1].stringValue());
			    else VM.badArgument(1);
			}
			count = elem.countElements(types);
		    }
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(count));
    }


    public static YoixObject
    dotGraph(YoixObject arg[]) {

	YoixGraphElement  elem = null;

	if (arg[0].notNull() && arg[0].isString())
	    elem = YoixGraphElement.loadDOTGraph(arg[0].stringValue());
	else VM.badArgument(0);

	return(elem == null ? YoixObject.newElement() : YoixObject.newElement(elem.getWrapper()));
    }


    public static YoixObject
    dotGraphToText(YoixObject arg[]) {

	//
	// The name of this builtin has been changed to dotGraphToYDAT and
	// all of our references to it have been changed, but there could
	// be someold config files lying around need this.
	// 

	return(dotGraphToYDAT(arg));
    }


    public static YoixObject
    dotGraphToYDAT(YoixObject arg[]) {

	String  text = null;
	char    delim = '|';

	//
	// Pretty much duplicates the graphPlotText() builtin that was
	// included in a custom module, but means we don't need that
	// module.
	//

	if (arg.length <= 5) {
	    if (arg[0].isGraphElement() || arg[0].isString()) {
		if (arg[0].notNull()) {
		    if (arg.length <= 1 || arg[1].isString() || arg[1].isNull()) {
			if (arg.length <= 2 || arg[2].isArray() || arg[2].isDictionary() || arg[2].isNull()) {
			    if (arg.length <= 3 || arg[3].isInteger()) {
				if (arg.length <= 4 || arg[4].isInteger()) {
				    if (arg.length > 1 && arg[1].sizeof() > 0)
					delim = arg[1].stringValue().charAt(0);
				    text = YoixMiscGraph.graphdata(
					arg[0],
					delim,
					arg.length > 2 ? arg[2] : null,
					arg.length > 3 ? arg[3].booleanValue() : false,
					arg.length > 4 ? arg[4].booleanValue() : false
				    );
				} else VM.badArgument(4);
			    } else VM.badArgument(3);
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(text == null ? YoixObject.newString() : YoixObject.newString(text));
    }


    public static YoixObject
    listElements(YoixObject arg[]) {

	YoixGraphElement  elem = null;
	YoixObject        result = null;
	YoixObject        args[] = null;
	Vector            list = null;
	int               types = GRAPH_NODE|GRAPH_EDGE|GRAPH_GRAPH;
	int               sz;
	int               i;

	if (arg[0].isGraph()) {
	    if (arg[0].notNull()) {
		elem = ((YoixBodyElement)arg[0].body()).getElement();
		if (elem != null) {
		    if (arg.length > 1) {
			if (arg[1].notNull() && (arg[1].isInteger() || arg[1].isString())) {
			    if (arg[1].isInteger())
				types &= arg[1].intValue();
			    else types &= YoixBodyElement.parseGraphTypes(arg[1].stringValue());
			    if (arg.length > 2) {
				if (arg[2].notNull() && arg[2].isFunction()) {
				    args = new YoixObject[arg.length - 2];
				    System.arraycopy(arg,2,args,0,args.length);
				} else VM.badArgument(2);
			    }
			} else if (arg[1].notNull() && arg[1].isFunction()) {
			    args = new YoixObject[arg.length - 1];
			    System.arraycopy(arg,1,args,0,args.length);
			} else VM.badArgument(1);
		    }
		    list = elem.listElements(types, args);
		    result = YoixObject.newArray(sz = list.size());
		    for (i = 0; i < sz; i++) {
			result.put(i, YoixObject.newElement((YoixBodyElement)list.elementAt(i)), false);
		    }
		}
	    }
	} else VM.badArgument(0);

	return(result == null ? YoixObject.newArray(0) : result);
    }


    public static YoixObject
    xmlGraph(YoixObject arg[]) {

	YoixGraphElement  elem = null;

	if (arg[0].notNull() && arg[0].isString())
	    elem = YoixGraphElement.loadXMLGraph(arg[0].stringValue());
	else VM.badArgument(0);

	return(elem == null ? YoixObject.newElement() : YoixObject.newElement(elem.getWrapper()));
    }
}

