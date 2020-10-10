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

interface YoixConstantsGraph

{

    //
    // Flags
    //

    static final int  GRAPH_GRAPH          = 0x0001;
    static final int  GRAPH_NODE           = 0x0002;
    static final int  GRAPH_EDGE           = 0x0004;
    static final int  GRAPH_STRICT         = 0x0008;
    static final int  GRAPH_DIRECTED       = 0x0010;
    static final int  GRAPH_FORWARD        = 0x0020;
    static final int  GRAPH_REVERSE        = 0x0040;
    static final int  GRAPH_UNLABELLED     = 0x0080;
    static final int  GRAPH_INVISIBLE      = 0x0100;
    static final int  GRAPH_PLACEHOLDER    = 0x0200;
    static final int  GRAPH_CLUSTER        = 0x0400;
    static final int  GRAPH_EDGEMADE       = 0x0800;

    static final int  TYPE_MASK      = GRAPH_GRAPH|GRAPH_NODE|GRAPH_EDGE;
    static final int  DIRECTION_MASK = GRAPH_FORWARD|GRAPH_REVERSE;

    //
    // Walk types
    //

    static final int  XML_TEXTUAL        = 0;
    static final int  DOT_TEXTUAL        = 1;
    static final int  SETROOT            = 2;
    static final int  WALKER             = 3;
    static final int  NAMECHECK          = 4;
    static final int  NEWDATA            = 5;
    static final int  GLOBAL_ATTR        = 6;
    static final int  GLOBAL_ATTRS       = 7;
    static final int  DRAW_LAYOUT        = 8;
    static final int  DRAW_XDOT          = 9;

    //
    // For attribute() callable
    //

    static final int  CREATE      = 1;
    static final int  REPLACE     = CREATE << 1;
    static final int  DELETE      = CREATE << 2;
    static final int  SCOPED      = CREATE << 3;
    static final int  NODEDFLT    = CREATE << 4;
    static final int  EDGEDFLT    = CREATE << 5;
    static final int  GRAPHDFLT   = CREATE << 6;

    static final int  BFS  = 1;
    static final int  DFS  = 0;
    static final int  WALK = -1;

    //
    // Keywords
    //

    static final String  COORD_KYWD      = "coord";
    static final String  DIGRAPH_KYWD    = "digraph";
    static final String  DIRECTED_KYWD   = "directed";
    static final String  EDGE_ATTR_KYWD  = "edge_attributes";
    static final String  EDGE_KYWD       = "edge";
    static final String  FLAGS_KYWD      = "flags";
    static final String  FORWARD_KYWD    = "forward";
    static final String  GRAPH_ATTR_KYWD = "graph_attributes";
    static final String  GRAPH_KYWD      = "graph";
    static final String  HEAD_KYWD       = "head";
    static final String  NAME_KYWD       = "name";
    static final String  NODE_ATTR_KYWD  = "node_attributes";
    static final String  NODE_KYWD       = "node";
    static final String  PARENT_KYWD     = "parent";
    static final String  REVERSE_KYWD    = "reverse";
    static final String  STRICT_KYWD     = "strict";
    static final String  SUBGRAPH_KYWD   = "subgraph";
    static final String  TAIL_KYWD       = "tail";
    static final String  TYPE_KYWD       = "type";

    // notify conditions
    static final Integer  CREATED        = new Integer(1);
    static final Integer  DELETED        = new Integer(2);
    static final Integer  MODIFIED       = new Integer(3); // attribute change
    static final Integer  RELINKED       = new Integer(4); // head/tail change
    static final Integer  RENAMED        = new Integer(5);
    static final Integer  RESHAPED       = new Integer(6);
    static final Integer  RECOMPUTE      = new Integer(7);
    static final Integer  REDRAW         = new Integer(8);

    static final Integer  MOD_LOCAL      = new Integer(1);
    static final Integer  MOD_NODE       = new Integer(2);
    static final Integer  MOD_EDGE       = new Integer(3);
    static final Integer  MOD_GRAPH      = new Integer(4);
    static final Integer  MOD_GLBL_NODE  = new Integer(5);
    static final Integer  MOD_GLBL_EDGE  = new Integer(6);
    static final Integer  MOD_GLBL_GRAPH = new Integer(7);

    static final Boolean  MARK_TRUE      = new Boolean(true);
    static final Boolean  MARK_FALSE     = new Boolean(false);

    // for layout
    static final int    LAYOUT_FORCE       = 0;
    static final int    LAYOUT_FORCE_BOUND = 1;

    static final double PIover2 = Math.PI / 2.0;
    static final double PItimes2 = Math.PI * 2.0;
    static final double toDegrees = 180. / Math.PI;
    static final double toRadians = Math.PI / 180.;

    static final String  ANON_PREFIX     = "_anon_";
    static final String  KEY_PREFIX      = "_key_";
}

