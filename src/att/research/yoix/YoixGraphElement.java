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

import java.awt.*;
import java.awt.geom.*;

public
class YoixGraphElement

    implements YoixAPI,
	       YoixConstants,
	       YoixConstantsGraph

{
    private final static String EDGESEP = "\ufffd";

    private String     name;
    private int        flags;
    private boolean    verbose = false;
    private Hashtable  attributes;

    InnerObservable  notifier;
    YoixGraphBase    graphdata;

    private YoixGraphElement  root;
    private YoixGraphElement  parent;
    private YoixBodyElement   wrapper;

    //
    // linked list associating nodes and edges to a subgraph
    //
    private YoixGraphElement  left_sibling;
    private YoixGraphElement  right_sibling;

    //
    // ways to know we've been here, done that
    //

    private Hashtable  edgelist;
    private String     key;
    private String     tailport;
    private String     headport;
    private String     tailpass;
    private String     headpass;

    private Hashtable  elements;
    private Vector     children;
    private int        visastamp;

    private Vector     vestigialchildren = null;
    private Vector     vestigialparents = null;

    //
    // default attributes used by subgraphs
    //

    private Hashtable  subg_attributes;
    private Hashtable  node_attributes;
    private Hashtable  edge_attributes;

    private static Hashtable  subg_defaults = new Hashtable();
    private static Hashtable  node_defaults = new Hashtable();
    private static Hashtable  edge_defaults = new Hashtable();

    private Vector  inbound;
    private Vector  outbound;
    private YoixGraphElement  head;
    private YoixGraphElement  tail;

    private int        seqid;		// for cross-locking graphs
    private static int marker = 0;

    private static Hashtable  names = new Hashtable();

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixGraphElement() {

	//
	// Placeholder graph
	//

	parent = null;
	root = this;
	this.flags = GRAPH_GRAPH|GRAPH_PLACEHOLDER;
	elements = new Hashtable();
	attributes = new Hashtable();
	notifier = new InnerObservable();
	seqid = getMarker();

	subg_attributes = new Hashtable();
	node_attributes = new Hashtable();
	edge_attributes = new Hashtable();
	children = new Vector();

	left_sibling = this;
	right_sibling = this;

	setName("<placeholder>");
    }


    YoixGraphElement(String name, int flags) {

	//
	// Root graph
	//

	parent = null;
	root = this;
	this.flags = GRAPH_GRAPH|(flags & (GRAPH_STRICT|GRAPH_DIRECTED));
	elements = new Hashtable();
	attributes = new Hashtable();
	notifier = new InnerObservable();
	seqid = getMarker();

	subg_attributes = new Hashtable();
	node_attributes = new Hashtable();
	edge_attributes = new Hashtable();
	children = new Vector();

	left_sibling = this;
	right_sibling = this;

	setName(name);
    }


    YoixGraphElement(String name, YoixGraphElement parent, int flags) {

	//
	// node
	//

	if (parent == null)
	    parent = new YoixGraphElement();
	else if (!parent.isType(GRAPH_GRAPH) || (parent.isType(GRAPH_PLACEHOLDER)))
	    VM.abort(BADVALUE, new String[] {PARENT_KYWD});

	flags &= ~TYPE_MASK;

	root = parent.getRoot();

	synchronized(root) {
	    this.parent = parent;
	    attributes = new Hashtable();

	    this.flags = GRAPH_NODE|flags;
	    inbound = new Vector();
	    outbound = new Vector();
	    parent.addSibling(this);

	    setName(name);
	    parent.queueInfo(new Object[]{this,CREATED});
	}
    }


    YoixGraphElement(String name, YoixGraphElement parent, int flags, boolean cluster) {

	//
	// subgraph - use cluster as boolean instead of part of flags
	// to distinguish from node.
	//

	if (parent == null)
	    parent = new YoixGraphElement();
	else if (!parent.isType(GRAPH_GRAPH) || (parent.isType(GRAPH_PLACEHOLDER)))
	    VM.abort(BADVALUE, new String[] {PARENT_KYWD});

	flags &= ~TYPE_MASK;

	root = parent.getRoot();

	synchronized(root) {
	    this.parent = parent;
	    attributes = new Hashtable();


	    if ((flags&(GRAPH_STRICT|GRAPH_DIRECTED)) != (root.getFlags()&(GRAPH_STRICT|GRAPH_DIRECTED)))
		VM.abort(BADVALUE, new String[] {FLAGS_KYWD + "/" + STRICT_KYWD + "/" + DIRECTED_KYWD});

	    if (cluster)
		flags |= GRAPH_CLUSTER;
	    else flags &= ~GRAPH_CLUSTER;

	    this.flags = GRAPH_GRAPH|flags;
	    seqid = getMarker();

	    subg_attributes = new Hashtable();
	    node_attributes = new Hashtable();
	    edge_attributes = new Hashtable();
	    children = new Vector();
	    left_sibling = this;
	    right_sibling = this;
	    parent.addChild(this);
	    notifier = new InnerObservable();

	    setName(name);
	    parent.queueInfo(new Object[]{this,CREATED});
	}
    }


    YoixGraphElement(String name, YoixGraphElement parent, YoixGraphElement tail, YoixGraphElement head, String key, int flags) {

	this(name, parent, tail, null, null, head, null, null, key, flags);
    }


    YoixGraphElement(String name, YoixGraphElement parent, YoixGraphElement tail, String tailport, String tailpass, YoixGraphElement head, String headport, String headpass, String key, int flags) {

	Hashtable  keys;
        boolean    nokey = false;

	if (parent == null)
	    parent = new YoixGraphElement();

	flags &= ~TYPE_MASK;

	if (!parent.isType(GRAPH_GRAPH))
	    VM.abort(BADVALUE, new String[] {PARENT_KYWD});
	if (head == null || !head.isType(GRAPH_NODE))
	    VM.abort(BADVALUE, new String[] {HEAD_KYWD + "[" + head + "]"});
	if (tail == null || !tail.isType(GRAPH_NODE))
	    VM.abort(BADVALUE, new String[] {TAIL_KYWD});
	if ((flags&~(DIRECTION_MASK)) != 0)
	    VM.abort(BADVALUE, new String[] {FLAGS_KYWD});

	root = parent.getRoot();

	synchronized(root) {
	    if (tail.getRoot() != root)
		VM.abort(BADVALUE, new String[] {TAIL_KYWD});
	    if (head.getRoot() != root)
		VM.abort(BADVALUE, new String[] {HEAD_KYWD});

	    boolean directed = root.isType(GRAPH_DIRECTED);

	    if (directed) {
		if ((flags&(DIRECTION_MASK)) == 0)
		    flags |= GRAPH_FORWARD;
		else if ((flags&(DIRECTION_MASK)) == (DIRECTION_MASK))
		    VM.abort(BADVALUE, new String[] {FLAGS_KYWD + "/" + DIRECTED_KYWD});
	    }

	    if (key == null) {
		nokey = true;
		if (name != null)
		    key = name;
		else
		    key = "";
	    }

	    if (root.isType(GRAPH_STRICT)) {

		if (tail == head) {
		    VM.abort(BADVALUE, new String[] {TAIL_KYWD + "/" + HEAD_KYWD + "/" + STRICT_KYWD});
		} else if (root.edgelist != null && (root.edgelist.containsKey(tail.name+EDGESEP+head.name) || (!directed && root.edgelist.containsKey(head.name+EDGESEP+tail.name)))) {
		    VM.abort(BADVALUE, new String[] {TAIL_KYWD + "/" + HEAD_KYWD + "/" + STRICT_KYWD});
		}
	    } else if (root.edgelist == null) {
		root.edgelist = new Hashtable();
		keys = new Hashtable();
		keys.put(key, this);
		root.edgelist.put(tail.name+EDGESEP+head.name, keys);
	    } else if ((keys = ((Hashtable)(root.edgelist.get(tail.name+EDGESEP+head.name)))) != null || (!directed && (keys = ((Hashtable)(root.edgelist.get(head.name+EDGESEP+tail.name)))) != null)) {
		if (keys.containsKey(key)) {
                    if (nokey) {
                        do {
                            key = KEY_PREFIX + getMarker();
                        } while(keys.containsKey(key));
                    } else
                        VM.abort(NAMECLASH, tail.name + "/" + head.name + "/" + key);
		} else
		    keys.put(key, this);
	    } else {
		keys = new Hashtable();
		keys.put(key, this);
		root.edgelist.put(tail.name+EDGESEP+head.name, keys);
	    }

	    this.parent = parent;
	    this.flags = GRAPH_EDGE|flags;
	    this.key = key;
	    this.tailport = tailport;
	    this.headport = headport;
	    this.tailpass = tailpass;
	    this.headpass = headpass;
	    attributes = new Hashtable();

	    // these must be set before adding edge to its nodes
	    this.tail = tail;
	    this.head = head;

	    addEdge(this);
	    parent.addSibling(this);
	    setEdgeName(name, head, tail);
	    parent.queueInfo(new Object[]{this,CREATED});
	}
    }

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public final String
    getName() {

	return(name);
    }


    public final String[]
    listAttachedEdgeNames(String nodename) {

	return(listAttachedEdgeNames(nodename, 0));
    }


    public final String[]
    listAttachedEdgeNames(String nodename, int direction) {

	YoixGraphElement  elem;
	Enumeration       enm;
	HashSet           hset;
	String            list[] = null;

	hset = new HashSet();

	synchronized(root) {
	    if ((elem = findNode(nodename)) != null) {
		if (direction <= 0) {
		    enm = elem.getInbound();
		    while (enm.hasMoreElements())
			hset.add(((YoixGraphElement)(enm.nextElement())).getName());
		}
		if (direction >= 0) {
		    enm = elem.getOutbound();
		    while (enm.hasMoreElements())
			hset.add(((YoixGraphElement)(enm.nextElement())).getName());
		}
		list = (String[])hset.toArray(new String[hset.size()]);
	    }
	}
	return(list);
    }


    public final String[]
    listEdgeTailHead(String edgename) {

	YoixGraphElement  elem;
	String            nodenames[] = null;

	synchronized(root) {
	    if ((elem = findEdge(edgename)) != null) {
		nodenames = new String[2];
		nodenames[0] = elem.tail.getName();
		nodenames[1] = elem.head.getName();
	    }
	}
	return(nodenames);
    }

    ///////////////////////////////////
    //
    // YoixGraphElement Methods
    //
    ///////////////////////////////////

    final void
    addObserverData(YoixBodyGraphObserver obs, int dataclass) {

	Object  args[];

	if (!isType(GRAPH_GRAPH))
	    VM.abort(INTERNALERROR);

	switch(dataclass) {
	    case LAYOUT_FORCE_BOUND:
		args = new Object[] {obs, new ForceLayout()};
		break;

	    case LAYOUT_FORCE:
	    default:
		args = null; // for compiler
		VM.abort(INTERNALERROR);
		break;
	}
	traverse(false, GRAPH_NODE|GRAPH_EDGE|GRAPH_GRAPH, -1, this, args, NEWDATA);
    }


    final YoixBodyThread
    prepLayout(YoixBodyGraphObserver obs) {

	// called from synchronized observer method

	YoixGraphBase  gd;

	synchronized(root) {
	    gd = getGraphData(obs, this);
	}

	RunLayout rl = new RunLayout(gd);

	return(rl.prepThread(obs));
    }


    final void
    runLayout(YoixBodyThread bthrd, boolean waitForIt, YoixObject[] args) {

	// called from UNsynchronized observer method

	RunLayout rl = (RunLayout)(bthrd.getRunner());
	if (rl != null)
	    rl.runThread(bthrd, waitForIt, args);
    }


    final void
    waitForLayout(YoixBodyThread bthrd) {

	// called from UNsynchronized observer method

	RunLayout rl = (RunLayout)(bthrd.getRunner());
	if (rl != null)
	    rl.waitForThread();
    }


    final YoixGraphBase
    getGraphData(YoixBodyGraphObserver obs, YoixGraphElement grf) {

	// assumes synchronized on root so graphdata ring is constant

	YoixGraphBase    gd = null;

	if (obs != null) {
	    gd = graphdata;

	    if (gd.obs != obs || gd.grf != grf) {
		do {
		    gd = gd.next;
		} while ((gd.obs != obs || gd.grf != grf) && gd != graphdata);

		if (gd.obs != obs || gd.grf != grf)
		    gd = null;
		else
		    graphdata = gd;
	    }
	}

	return(gd);
    }


    final YoixObject
    addDefaultAttribute(int dflt, String name, YoixObject value) {

	YoixGraphElement  prnt;
	YoixGraphElement  grndprnt;
	Hashtable         attrs;
	YoixObject        oldValue;
	Integer           indicator;

	name = nameCache(name);

	synchronized(root) {
	    prnt = isType(GRAPH_GRAPH) ? this : parent;
	    if (dflt == 0) {
		dflt = getType();
		if (dflt == GRAPH_GRAPH) {
		    attrs = prnt.subg_attributes;
		    indicator = MOD_GRAPH;
		} else if (dflt == GRAPH_NODE) {
		    attrs = prnt.node_attributes;
		    indicator = MOD_NODE;
		} else {
		    attrs = prnt.edge_attributes;
		    indicator = MOD_EDGE;
		}
	    } else {
		switch (dflt) {
		    case EDGEDFLT:
		    case GRAPHDFLT:
		    case NODEDFLT:
			break;

		    default:
			VM.abort(INTERNALERROR);
			break;
		}

		if ((grndprnt = prnt.parent) == null) {
		    if (dflt == GRAPHDFLT) {
			attrs = YoixGraphElement.subg_defaults;
			indicator = MOD_GLBL_GRAPH;
		    } else if (dflt == NODEDFLT) {
			attrs = YoixGraphElement.node_defaults;
			indicator = MOD_GLBL_NODE;
		    } else {
			attrs = YoixGraphElement.edge_defaults;
			indicator = MOD_GLBL_EDGE;
		    }
		    prnt = null;
		} else {
		    if (dflt == GRAPHDFLT) {
			attrs = grndprnt.subg_attributes;
			indicator = MOD_GRAPH;
		    } else if (dflt == NODEDFLT) {
			attrs = grndprnt.node_attributes;
			indicator = MOD_NODE;
		    } else {
			attrs = grndprnt.edge_attributes;
			indicator = MOD_EDGE;
		    }
		    prnt = grndprnt;
		}
	    }

	    oldValue = (YoixObject)attrs.get(name);
	    if (value == null) {
		attrs.remove(name);
		if (prnt == null)
		    globalNotify(new Object[]{root, MODIFIED, indicator, name, null});
		else
		    prnt.queueInfo(new Object[]{prnt, MODIFIED, indicator, name, null});
	    } else if (!value.equals(oldValue)) {
		attrs.put(name, value);
		if (prnt == null)
		    globalNotify(new Object[]{root, MODIFIED, indicator, name, value});
		else
		    prnt.queueInfo(new Object[]{prnt, MODIFIED, indicator, name, value});
	    }
	}
	return(oldValue);
    }


    final YoixObject
    addLocalAttribute(int dflt, String name, YoixObject value) {

	YoixGraphElement  prnt;
	YoixObject        oldValue;
	Hashtable         attr;
	Integer           indicator;

	name = nameCache(name);

	synchronized(root) {
	    if (dflt == 0) {
		oldValue = (YoixObject)attributes.get(name);
		if (value == null) {
		    attributes.remove(name);
		    queueInfo(new Object[]{this, MODIFIED, MOD_LOCAL, name, null});
		} else if (!value.equals(oldValue)) {
		    attributes.put(name, value);
		    queueInfo(new Object[]{this, MODIFIED, MOD_LOCAL, name, value});
		}
	    } else {
		prnt = isType(GRAPH_GRAPH) ? this : parent;
		if (dflt == GRAPHDFLT) {
		    attr = prnt.subg_attributes;
		    indicator = MOD_GRAPH;
		} else if (dflt == NODEDFLT) {
		    attr = prnt.node_attributes;
		    indicator = MOD_NODE;
		} else if (dflt == EDGEDFLT) {
		    attr = prnt.edge_attributes;
		    indicator = MOD_EDGE;
		} else {
		    attr = null;
		    indicator = null;
		    VM.abort(INTERNALERROR);
		}

		oldValue = (YoixObject)attr.get(name);
		if (value == null) {
		    attr.remove(name);
		    prnt.queueInfo(new Object[]{prnt, MODIFIED, indicator, name, null});
		} else if (!value.equals(oldValue)) {
		    attr.put(name, value);
		    prnt.queueInfo(new Object[]{prnt, MODIFIED, indicator, name, value});
		}
	    }
	}
	return(oldValue);
    }


    final Vector
    bdfs(boolean dfirst, int depth, YoixGraphElement baggage, Object carryon, int indicators) {

	YoixGraphElement  elem;
	Vector            input;
	Vector            stack;
	int               size;

	stack = new Vector();
	input = new Vector(1);
	input.addElement(this);

	synchronized(root) {
	    doBDFS(getType(), depth, getMarker(), 0, input, stack);
	    if (baggage != null && (size = stack.size()) > 0) {
		if (dfirst) {
		    while (size-- > 0) {
			elem = (YoixGraphElement)(stack.elementAt(size));
			if (baggage.sightsee(elem, indicators, true, carryon)) {
			    while (--size >= 0)
				stack.removeElementAt(size);
			    break;
			}

			if (elem.isType(GRAPH_GRAPH)) { // should be sufficient to say type == GRAPH_GRAPH
			    if (baggage.sightsee(elem, indicators, false, carryon)) {
				while (--size >= 0)
				    stack.removeElementAt(size);
				break; // not really necessary
			    }
			}
		    }
		} else {
		    for (int i=0; i<size; i++) {
			elem = (YoixGraphElement)(stack.elementAt(i));
			if (baggage.sightsee(elem, indicators, true, carryon)) {
			    if (++i < size)
				stack.setSize(i);
			    break;
			}
			if (elem.isType(GRAPH_GRAPH)) { // should be sufficient to say type == GRAPH_GRAPH
			    if (baggage.sightsee(elem, indicators, false, carryon)) {
				if (++i < size)
				    stack.setSize(i);
				break;
			    }
			}
		    }
		}
	    }
	}
	return(stack);
    }


    final void
    changeEdge(YoixGraphElement node, boolean isHead) {
	// assume this is an edge and node is a node

	synchronized(root) {
	    if (isHead) {
		if (head != node) {
		    queueInfo(new Object[]{this, RELINKED, MARK_TRUE, head, node});
		    head.inbound.removeElement(this);
		    head = node;
		    if (!head.inbound.contains(this))
			head.inbound.addElement(this);
		}
	    } else {
		if (tail != node) {
		    queueInfo(new Object[]{this, RELINKED, MARK_FALSE, tail, node});
		    tail.outbound.removeElement(this);
		    tail = node;
		    if (!tail.outbound.contains(this))
			tail.outbound.addElement(this);
		}
	    }
	}
    }


    final void
    changeName(String newname) {

	synchronized(root) {
	    if (!name.equals(newname)) {
		if (!root.elements.containsKey(newname)) {
		    queueInfo(new Object[]{this, RENAMED, name, newname});
		    root.elements.put(newname, this);
		    root.elements.remove(name);
		    name = newname;
		} else VM.abort(NAMECLASH, newname);
	    }
	}
    }


    final void
    clearDefaultAttributes() {

	clearDefaultAttributes(0);
    }


    final void
    clearDefaultAttributes(int dflt) {

	YoixGraphElement  prnt;
	YoixGraphElement  grndprnt;
	Enumeration       enm;
	Hashtable         attrs;
	Integer           indicator;

	synchronized(root) {
	    prnt = isType(GRAPH_GRAPH) ? this : parent;
	    if (dflt == 0) {
		dflt = getType();
		if (dflt == GRAPH_GRAPH) {
		    attrs = prnt.subg_attributes;
		    indicator = MOD_GRAPH;
		} else if (dflt == GRAPH_NODE) {
		    attrs = prnt.node_attributes;
		    indicator = MOD_NODE;
		} else {
		    attrs = prnt.edge_attributes;
		    indicator = MOD_EDGE;
		}
	    } else {
		switch (dflt) {
		    case EDGEDFLT:
		    case GRAPHDFLT:
		    case NODEDFLT:
			break;

		    default:
			VM.abort(INTERNALERROR);
			break;
		}
		if ((grndprnt = prnt.parent) == null) {
		    if (dflt == GRAPHDFLT) {
			attrs = YoixGraphElement.subg_defaults;
			indicator = MOD_GLBL_GRAPH;
		    } else if (dflt == NODEDFLT) {
			attrs = YoixGraphElement.node_defaults;
			indicator = MOD_GLBL_NODE;
		    } else {
			attrs = YoixGraphElement.edge_defaults;
			indicator = MOD_GLBL_EDGE;
		    }
		    prnt = null;
		} else {
		    if (dflt == GRAPHDFLT) {
			attrs = grndprnt.subg_attributes;
			indicator = MOD_GRAPH;
		    } else if (dflt == NODEDFLT) {
			attrs = grndprnt.node_attributes;
			indicator = MOD_NODE;
		    } else {
			attrs = grndprnt.edge_attributes;
			indicator = MOD_EDGE;
		    }
		    prnt = grndprnt;
		}
	    }

	    enm = attrs.keys();
	    while(enm.hasMoreElements()) {
		if (prnt == null)
		    globalNotify(new Object[]{root, MODIFIED, indicator, enm.nextElement(), null});
		else
		    prnt.queueInfo(new Object[]{prnt, MODIFIED, indicator, enm.nextElement(), null});
	    }

	    attrs.clear();
	}
    }


    final void
    clearLocalAttributes() {

	clearLocalAttributes(0);
    }


    final void
    clearLocalAttributes(int dflt) {

	YoixGraphElement  prnt;
	Enumeration       enm;
	Hashtable         attrs;
	Integer           indicator;

	synchronized(root) {
	    if (dflt != 0) {
		prnt = isType(GRAPH_GRAPH) ? this : parent;
		if (dflt == GRAPHDFLT) {
		    attrs = prnt.subg_attributes;
		    indicator = MOD_GRAPH;
		} else if (dflt == NODEDFLT) {
		    attrs = prnt.node_attributes;
		    indicator = MOD_NODE;
		} else if (dflt == EDGEDFLT) {
		    attrs = prnt.edge_attributes;
		    indicator = MOD_EDGE;
		} else {
		    attrs = null;
		    indicator = null;
		    VM.abort(INTERNALERROR);
		}
	    } else {
		prnt = this; // not really a parent, but needed below
		attrs = attributes;
		indicator = MOD_LOCAL;
	    }

	    enm = attrs.keys();
	    while(enm.hasMoreElements()) {
		prnt.queueInfo(new Object[]{prnt, MODIFIED, indicator, enm.nextElement(), null});
	    }

	    attrs.clear();
	}
    }


    final int
    countElements(int types) {

	YoixGraphElement  elem;
	Enumeration       enm;
	int               count = 0;

	types &= TYPE_MASK;
	if (types != 0 && types != TYPE_MASK) {
	    synchronized(root) {
		for (enm = elements.elements(); enm.hasMoreElements(); ) {
		    elem = (YoixGraphElement)(enm.nextElement());
		    if (elem.ofType(types))
			count++;
		}
	    }
	} else count = elements.size();
	return(count);
    }


    public boolean
    equals(Object obj) {

	return(obj == this);
    }


    final YoixGraphElement
    findEdge(String tailname, String headname, String key) {

	YoixGraphElement  elem = null;

	if (key == null)
	    key = "";

	synchronized(root) {
	    if (root.edgelist != null) {
		elem = (YoixGraphElement)(root.edgelist.get(tailname+EDGESEP+headname+EDGESEP+key));
		if (elem == null && !root.isType(GRAPH_DIRECTED))
		    elem = (YoixGraphElement)(root.edgelist.get(headname+EDGESEP+tailname+EDGESEP+key));
	    }
	}
	return(elem);
    }


    final YoixGraphElement
    findEdge(String name) {

	YoixGraphElement  elem;

	synchronized(root) {
	    elem = (YoixGraphElement)(root.elements.get(name));
	    if (elem != null && !elem.ofType(GRAPH_EDGE))
		elem = null;
	}
	return(elem);
    }


    final YoixGraphElement
    findElement(String name) {

	YoixGraphElement  elem;

	synchronized(root) {
	    elem = (YoixGraphElement)(root.elements.get(name));
	}
	return(elem);
    }


    final YoixGraphElement
    findNode(String name) {

	YoixGraphElement  elem;

	synchronized(root) {
	    elem = (YoixGraphElement)(root.elements.get(name));
	    if (elem != null && !elem.ofType(GRAPH_NODE))
		elem = null;
	}
	return(elem);
    }


    final YoixGraphElement
    findSubgraph(String name) {

	YoixGraphElement  elem;

	synchronized(root) {
	    elem = (YoixGraphElement)(root.elements.get(name));
	    if (elem != null && (elem.parent == null || !elem.ofType(GRAPH_GRAPH)))
		elem = null;
	}
	return(elem);
    }


    final YoixObject
    getAttribute(String name) {

	return(getAttribute(0, name));
    }


    final YoixObject
    getAttribute(int dflt, String name) {

	YoixObject  value;

	if ((value = getLocalAttribute(dflt, name)) == null)
	    value = getGlobalAttribute(dflt, name);
	return(value);
    }


    final Hashtable
    getAttributes() {

	return(getAttributes(0));
    }


    final Hashtable
    getAttributes(int dflt) {

	YoixObject  value;
	Hashtable   attrs;
	int         type;

	if (dflt == 0) {
	    attrs = getLocalAttributes(0);
	    dflt = getType();
	    if (dflt == GRAPH_GRAPH)
		dflt = GRAPHDFLT;
	    else if (dflt == GRAPH_NODE)
		dflt = NODEDFLT;
	    else dflt = EDGEDFLT;
	} else {
	    switch (dflt) {
		case EDGEDFLT:
		case GRAPHDFLT:
		case NODEDFLT:
		    break;

		default:
		    VM.abort(INTERNALERROR);
		    break;
	    }
	    attrs = new Hashtable();
	}
	getGlobalAttributes(dflt, attrs);

	return(attrs);
    }


    final int
    getFlags() {

	return(flags&~TYPE_MASK);
    }


    final YoixObject
    getGlobalAttribute(int dflt, String name) {

	YoixGraphElement  prnt;
	YoixObject        value = null;
	Hashtable         attrs;
	Object            args[];

	if (dflt == 0) {
	    dflt = getType();
	    if (dflt == GRAPH_EDGE)
		dflt = EDGEDFLT;
	    else if (dflt == GRAPH_NODE)
		dflt = NODEDFLT;
	    else dflt = GRAPHDFLT;
	}

	synchronized(root) {
	    if (isType(GRAPH_GRAPH))
		prnt = this;
	    else prnt = parent;
	    args = new Object[] {new Integer(dflt), name, null};
	    if (prnt.doClimbGlobalScope(this, args, GLOBAL_ATTR, true))
		value = (YoixObject)args[2];
	}

	if (value == null) {
	    if (dflt == GRAPHDFLT)
		attrs = YoixGraphElement.subg_defaults;
	    else if (dflt == NODEDFLT)
		attrs = YoixGraphElement.node_defaults;
	    else if (dflt == EDGEDFLT)
		attrs = YoixGraphElement.edge_defaults;
	    else {
		attrs = null;
		VM.abort(INTERNALERROR);
	    }
	    value = (YoixObject)attrs.get(name);
	}

	return(value);
    }


    final void
    getGlobalAttributes(int dflt, Hashtable hash) {

	YoixGraphElement  prnt;
	Enumeration       enm;
	Hashtable         attrs;
	String            name;
	Object            args[];
	Object            value;

	if (dflt == 0) {
	    dflt = getType();
	    if (dflt == GRAPH_EDGE)
		dflt = EDGEDFLT;
	    else if (dflt == GRAPH_NODE)
		dflt = NODEDFLT;
	    else dflt = GRAPHDFLT;
	}

	synchronized(root) {
	    if (isType(GRAPH_GRAPH))
		prnt = this;
	    else
		prnt = parent;
	    args = new Object[]{ new Integer(dflt), hash };
	    prnt.doClimbGlobalScope(this, args, GLOBAL_ATTRS, true);
	}

	if (dflt == GRAPHDFLT)
	    attrs = YoixGraphElement.subg_defaults;
	else if (dflt == NODEDFLT)
	    attrs = YoixGraphElement.node_defaults;
	else if (dflt == EDGEDFLT)
	    attrs = YoixGraphElement.edge_defaults;
	else {
	    attrs = null;
	    VM.abort(INTERNALERROR);
	}
	for (enm = attrs.keys(); enm.hasMoreElements(); ) {
	    name = (String)enm.nextElement();
	    if (!hash.containsKey(name))
		if ((value = attrs.get(name)) != null)
		    hash.put(name, value);
	}
    }


    final YoixGraphElement
    getHead() {

	return(head);
    }


    final String
    getHeadport() {

	return(headport);
    }


    final String
    getHeadpass() {

	return(headpass);
    }


    final Enumeration
    getInbound() {

	Enumeration enm;

	if (!ofType(GRAPH_NODE))
	    enm = (new Vector()).elements();
	else enm = inbound.elements();
	return(enm);
    }


    final YoixObject
    getLocalAttribute(String name) {

	return(getLocalAttribute(0, name));
    }


    final YoixObject
    getLocalAttribute(int dflt, String name) {

	YoixGraphElement  prnt;
	YoixObject        result;
	Hashtable         attr;

	if (dflt != 0) {
	    synchronized(root) {
		prnt = isType(GRAPH_GRAPH) ? this : parent;
		if (dflt == EDGEDFLT)
		    attr = prnt.edge_attributes;
		else if (dflt == GRAPHDFLT)
		    attr = prnt.subg_attributes;
		else if (dflt == NODEDFLT)
		    attr = prnt.node_attributes;
		else {
		    attr = null;
		    VM.abort(INTERNALERROR);
		}
		result = (YoixObject)attr.get(name);
	    }
	} else result = (YoixObject)attributes.get(name);
	return(result);
    }


    final Hashtable
    getLocalAttributes() {

	return(getLocalAttributes(0));
    }


    final Hashtable
    getLocalAttributes(int dflt) {

	YoixGraphElement  prnt;
	Hashtable         attrs;

	if (dflt != 0) {
	    synchronized(root) {
		prnt = isType(GRAPH_GRAPH) ? this : parent;
		if (dflt == NODEDFLT)
		    attrs = (Hashtable)prnt.node_attributes.clone();
		else if (dflt == EDGEDFLT)
		    attrs = (Hashtable)prnt.edge_attributes.clone();
		else if (dflt == GRAPHDFLT)
		    attrs = (Hashtable)prnt.subg_attributes.clone();
		else {
		    attrs = null; // otherwise javac complains
		    VM.abort(INTERNALERROR);
		}
	    }
	} else attrs = (Hashtable)attributes.clone();
	return(attrs);
    }


    final Enumeration
    getOutbound() {

	Enumeration  enm;

	if (!ofType(GRAPH_NODE))
	    enm = (new Vector()).elements();
	else enm = outbound.elements();
	return(enm);
    }


    final YoixGraphElement
    getParent() {

	return(parent);
    }


    final YoixGraphElement
    getRoot() {

	return(root);
    }


    final YoixGraphElement
    getTail() {

	return(tail);
    }


    final String
    getTailport() {

	return(tailport);
    }


    final String
    getTailpass() {

	return(tailpass);
    }


    final YoixBodyElement
    getWrapper() {

	return(wrapper);
    }


    static final void
    globalNotify(Object[] info) {

	// System.err.println("global modify");
	// TODO
    }


    final private void
    descendentNotify(Object[] info) {

	// assumes info[1] == MODIFIED and we're synchronized on root

	YoixGraphElement  subg;
	int               len;

	if ((len = children.size()) > 0) {
	    switch(((Integer)(info[2])).intValue()) {
	    case 2: // MOD_NODE
		for (int i=0; i<len; i++) {
		    subg = (YoixGraphElement)(children.elementAt(i));
		    if (!subg.node_attributes.containsKey(info[3])) {
			if (subg.notifier != null)
			    subg.notifier.queueInfo(info);
			subg.descendentNotify(info);
		    }
		}
		break;
	    case 3: // MOD_EDGE
		for (int i=0; i<len; i++) {
		    subg = (YoixGraphElement)(children.elementAt(i));
		    if (!subg.edge_attributes.containsKey(info[3])) {
			if (subg.notifier != null)
			    subg.notifier.queueInfo(info);
			subg.descendentNotify(info);
		    }
		}
		break;
	    case 4: // MOD_GRAPH
		for (int i=0; i<len; i++) {
		    subg = (YoixGraphElement)(children.elementAt(i));
		    if (!subg.subg_attributes.containsKey(info[3])) {
			if (subg.notifier != null)
			    subg.notifier.queueInfo(info);
			subg.descendentNotify(info);
		    }
		}
		break;
	    default:
		// cannot happen
		VM.abort(INTERNALERROR);
		break;
	    }
	}
    }


    final static boolean
    isClusterName(String name) {
	return(name != null && name.startsWith("cluster"));
    }


    final boolean
    isDescendentOf(YoixGraphElement subg) {

	YoixGraphElement  prnt = parent;

	while (prnt != null) {
	    if (subg == prnt)
		return(true);
	    prnt = prnt.parent;
	}

	return(false);
    }


    final Vector
    listElements(int types, YoixObject args[]) {

	YoixGraphElement  elem;
	Enumeration       enm;
	YoixObject        ret;
	Vector            list;

	list = new Vector();
	types &= TYPE_MASK;

	synchronized(root) {
	    for (enm = elements.elements(); enm.hasMoreElements(); ) {
		elem = (YoixGraphElement)(enm.nextElement());
		if (types == 0 || elem.ofType(types)) {
		    if (args != null && (ret = elem.getWrapper().call(args)) != null) {
			if (ret.isInteger() && ret.intValue() != 0)
			    list.addElement(elem.getWrapper());
		    } else list.addElement(elem.getWrapper());
		}
	    }
	}
	return(list);
    }


    static YoixGraphElement
    loadDOTGraph(String source) {

	return(loadDOTGraph(Yoix.translateString(source, true, PARSER_DOT)));
    }


    static YoixGraphElement
    loadDOTGraph(char source[]) {

	return(loadDOTGraph(Yoix.translateCharArray(source, true, PARSER_DOT)));
    }


    static YoixGraphElement
    loadXMLGraph(String source) {

	return(loadXMLGraph(Yoix.translateString(source, true, PARSER_XML)));
    }


    static YoixGraphElement
    loadXMLGraph(char source[]) {

	return(loadXMLGraph(Yoix.translateCharArray(source, true, PARSER_XML)));
    }


    final boolean
    ofType(int flag) {

	return((flags&flag) != 0);
    }


    final void
    queueInfo(Object info[]) {

	YoixGraphElement  prnt;

	if (isType(GRAPH_GRAPH))
	    prnt = this;
	else prnt = parent;

	while (prnt != null) {
	    if (prnt.notifier != null)
		prnt.notifier.queueInfo(info);
	    prnt = prnt.parent;
	}

	if (info[1] == MODIFIED && info[2] != MOD_LOCAL && isType(GRAPH_GRAPH)) {
	    descendentNotify(info);
	}
    }


    final void
    setParent(YoixGraphElement prnt) {

	YoixGraphElement  base;
	YoixGraphElement  root1;
	YoixGraphElement  root2;

	if (prnt == null) {
	    if (isType(GRAPH_GRAPH))
		base = this;
	    else base = prnt = new YoixGraphElement();
	} else base = prnt.getRoot();

	if (base.seqid < root.seqid) {
	    root1 = base;
	    root2 = root;
	} else {
	    root1 = root;
	    root2 = base;
	}

	synchronized(root1) {
	    synchronized(root2) {
		if (prnt != parent) {
		    if (root != base) {
			if (isType(GRAPH_GRAPH)) {
			    if ((flags&(GRAPH_STRICT|GRAPH_DIRECTED)) != (base.getFlags()&(GRAPH_STRICT|GRAPH_DIRECTED)))
				VM.abort(BADVALUE, new String[] {FLAGS_KYWD + "/" + STRICT_KYWD + "/" + DIRECTED_KYWD});

			    if (this != base)
				bdfs(true, -1, this, base, NAMECHECK);
			    bdfs(true, -1, this, base, SETROOT);
			} else {
			    if (base.elements.containsKey(name))
				VM.abort(NAMECLASH, name);
			    root.elements.remove(name);
			    root = base;
			    root.elements.put(name, this);
			}
		    }
		    switchParent(prnt);
		}
	    }
	}
    }


    final void
    setWrapper(YoixBodyElement wrapper) {

	this.wrapper = wrapper;
    }


    public final String
    toString() {

	return(toString(XML_TEXTUAL, -1, -1, null, null));
    }


    final String
    toString(int format, int mode, int depth, String indent, YoixBodyGraphObserver obs) {

	StringBuffer  output;
	Object        parcel[];

	format = (format == DOT_TEXTUAL) ? DOT_TEXTUAL : XML_TEXTUAL;
	output = new StringBuffer();
	parcel = new Object[] {output, indent, null, null};

	synchronized(root) {
	    if (mode == 0)
		bdfs(true, depth, this, parcel, format);
	    else if (mode > 0)
		bdfs(false, depth, this, parcel, format);
	    else {
		parcel[2] = new Integer(getGeneration());
		parcel[3] = obs; // may or may not be null
		traverse(false, GRAPH_NODE|GRAPH_EDGE|GRAPH_GRAPH, depth, this, parcel, format);
	    }
	}
	return(output.toString());
    }


    final Vector
    traverse(boolean buildVec, int type, int depth, YoixGraphElement baggage, Object carryon, int indicators) {

	Vector  stack;
	int     level = 0;

	stack = buildVec ? new Vector() : null;
	synchronized(root) {
	    doTraversal(stack, type, depth, baggage, carryon, indicators, getMarker(), level);
	}
	return(stack);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    addChild(YoixGraphElement subgraph) {

	if (!children.contains(subgraph))
	    children.addElement(subgraph);
    }


    private static void
    addEdge(YoixGraphElement edge) {

	if (!edge.tail.outbound.contains(edge))
	    edge.tail.outbound.addElement(edge);
	if (!edge.head.inbound.contains(edge))
	    edge.head.inbound.addElement(edge);
    }


    private void
    addSibling(YoixGraphElement sib) {

	if (left_sibling == null || sib.parent != this) {
	    VM.abort(INTERNALERROR); // should never happen
	} else {
	    sib.left_sibling = left_sibling;
	    left_sibling = sib;
	    sib.right_sibling = this;
	    sib.left_sibling.right_sibling = sib;
	}
    }


    private void
    appendAttributes(Hashtable attributes, StringBuffer output, int format, boolean hasData) {

	appendAttributes(attributes, output, format, hasData, " ", null);
    }


    private void
    appendAttributes(Hashtable attributes, StringBuffer output, int format, boolean hasData, String prefix, String delim) {

	Enumeration  enm;
	YoixObject   value;
	String       name;
	int          loop = 0;

	if (hasData && appendData(output, prefix, delim, format)) {
	    loop++;
	}

	for (enm = attributes.keys(); enm.hasMoreElements(); ) {
	    loop++;
	    name = (String)enm.nextElement();
	    value = (YoixObject)attributes.get(name);
	    if (loop > 1 && delim != null)
		output.append(delim);
	    output.append(prefix);
	    output.append(name);
	    output.append('=');
	    if (value == null || value.isNull())
		output.append("\"\"");
	    else {
		if (value.isString()) {
		    String   str = value.stringValue();

		    if (str.startsWith("\000<") && str.endsWith(">\000"))
			output.append(str.substring(1, str.length() - 1));
		    else if (format == DOT_TEXTUAL)
			output.append(quoteText(str, false, true));
		    else {
			boolean  forceQuotes = false;
			try {
			    double  d = Double.valueOf(str).doubleValue();
			    if (Math.floor(d) != d)
				forceQuotes = true;
			}
			catch(NumberFormatException e) {}
			output.append(quoteText(str, forceQuotes, false));
		    }
		} else if (value.isNumber()) {
		    if (value.isInteger())
			output.append(value.intValue());
		    else {
			double  d = value.doubleValue();

			if (Math.floor(d) == d)
			    output.append((long)d);
			else {
			    output.append('"');
			    output.append(d);
			    output.append('"');
			}
		    }
		} else {
		    String type = value.getTypename();

		    // using == instead of equals() is OK here
		    if (type == T_COLOR || type == T_DIMENSION || type == T_INSETS || type == T_POINT || type == T_RECTANGLE) {
			int len = value.length();
			output.append('"');
			for (int i=0; i<len; i++) {
			    if (i > 0)
				output.append(',');
			    output.append(value.get(i,false).doubleValue());
			}
			output.append('"');
		    } else if (type == T_ARRAY) {
			boolean list = true;
			int len = value.length();
			for (int i=0; i<len; i++) {
			    if (!value.get(i,false).isNumber()) {
				list = false;
				break;
			    }
			}
			if (list) {
			    output.append('"');
			    for (int i=0; i<len; i++) {
				if (i > 0) {
				    output.append(',');
				    // treat as tuples (visually)
				    if (i%2 == 1)
					output.append(' ');
				}
				output.append(value.get(i,false).doubleValue());
			    }
			    output.append('"');
			} else {
			    output.append("\"data of type '" + type + "'\"");
			}
		    } else {
			// TODO
			output.append("\"data of type '" + type + "'\"");
		    }
		}
	    }
	}
    }


    private void
    appendFlags(StringBuffer output) {

	appendFlags(output, null, null, XML_TEXTUAL);
    }


    private void
    appendFlags(StringBuffer output, String prefix, String delim, int format) {

	if (isType(GRAPH_GRAPH) && parent == null) {
	    if (isType(GRAPH_DIRECTED)) {
		output.append(" ");
		output.append(DIRECTED_KYWD);
		output.append("=1");
	    } else if (root.verbose) {
		output.append(" ");
		output.append(DIRECTED_KYWD);
		output.append("=0");
	    }

	    if (isType(GRAPH_STRICT)) {
		output.append(" ");
		output.append(STRICT_KYWD);
		output.append("=1");
	    } else if (root.verbose) {
		output.append(" ");
		output.append(STRICT_KYWD);
		output.append("=0");
	    }
	}

	if (isType(GRAPH_EDGE)) {
	    if (isType(GRAPH_FORWARD) && (root.verbose || !root.isType(GRAPH_DIRECTED))) {
		output.append(" ");
		output.append(FORWARD_KYWD);
		output.append("=1");
	    } else if (root.verbose && !root.isType(GRAPH_DIRECTED)) {
		output.append(" ");
		output.append(FORWARD_KYWD);
		output.append("=0");
	    }

	    if (isType(GRAPH_REVERSE)) {
		output.append(" ");
		output.append(REVERSE_KYWD);
		output.append("=1");
	    } else if (root.verbose && !root.isType(GRAPH_DIRECTED)) {
		output.append(" ");
		output.append(REVERSE_KYWD);
		output.append("=0");
	    }

	    output.append(" ");
	    output.append(TAIL_KYWD);
	    output.append("=");
	    output.append(quoteText(tail.getName(),false));

	    output.append(" ");
	    output.append(HEAD_KYWD);
	    output.append("=");
	    output.append(quoteText(head.getName(),false));
	}
    }


    private boolean
    appendData(StringBuffer output, String prefix, String delim, int format) {

	int orig_len = output.length();

	if (ofType(GRAPH_NODE|GRAPH_GRAPH)) {
	    if (format == XML_TEXTUAL) {
		output.append(" ");
		output.append(COORD_KYWD);
		output.append("=\"");
		if (Math.floor(graphdata.x) == graphdata.x)
		    output.append((long)graphdata.x);
		else
		    output.append(graphdata.x);
		output.append(",");
		if (Math.floor(graphdata.y) == graphdata.y)
		    output.append((long)graphdata.y);
		else
		    output.append(graphdata.y);
		output.append("\"");
	    } else {
		output.append(prefix);
		output.append("pos=\"");
		if (Math.floor(graphdata.x) == graphdata.x)
		    output.append((long)graphdata.x);
		else
		    output.append(graphdata.x);
		output.append(",");
		if (Math.floor(graphdata.y) == graphdata.y)
		    output.append((long)graphdata.y);
		else
		    output.append(graphdata.y);
		output.append("\"");
	    }
	}

	return(orig_len != output.length());
    }


    private static YoixGraphElement
    buildDOTGraph(SimpleNode content, String gname, YoixGraphElement graph) {

	YoixGraphElement  elem;
	YoixGraphElement  head;
	YoixGraphElement  tail;
	YoixGraphElement  subg;
	YoixGraphElement  gelm;
	SimpleNode        tree;
	SimpleNode        node;
	SimpleNode        crnt;
	Hashtable         attrs;
	String            tag;
	String            subtag;
	String            type;
	String            name;
	String            value;
	String            strarr[];
	Object            headparts[];
	Object            tailparts[];
	Object            obj;
	String            headname;
	String            headport;
	String            headpass;
	String            tailname;
	String            tailport;
	String            tailpass;
	String            key;
	Vector            vec;
	Vector            edgeparts;
	Vector            edgeports;
	int               epcnt;
	int               flags;
	int               pos;
	int               ee, ii, jj, hh, tt;

	//
	// Incomplete, but we're working on it.
	//

	node = null;
	elem = tail = head = null;
	value = null;

	for (pos=0; pos<content.value.length; pos++) {
	    if (!(content.value[pos] instanceof SimpleNode)) {
		VM.abort(INTERNALERROR, "DOT Graph Tree: bad content: " + gname);
	    }

	    tree = (SimpleNode)(content.value[pos]);

	    attrs = null;
	    tag = null;

	    switch (tree.type()) {
	    case DOTParserConstants._GRAPH_ATTRIBUTES:
		attrs = graph.subg_attributes;
		// fall through to...
	    case DOTParserConstants._NODE_ATTRIBUTES:
		if (attrs == null)
		    attrs = graph.node_attributes;
		// fall through to...
	    case DOTParserConstants._EDGE_ATTRIBUTES:
		if (attrs == null)
		    attrs = graph.edge_attributes;

		node = null;

		for (ii=0; ii<tree.value.length; ii++) {
		    switch ((crnt = ((SimpleNode)(tree.value[ii]))).type()) {
		    case YoixParserConstants.TAG:
			tag = crnt.value[0].toString();
			break;
		    case DOTParserConstants._ATTRIBUTE:
			node = crnt;
			subtag = tag;
			name = value = null;
			for (jj=0; jj<node.value.length; jj++) {
			    switch ((crnt = ((SimpleNode)(node.value[jj]))).type()) {
			    case YoixParserConstants.TAG:
				subtag = crnt.value[0].toString();
				break;
			    case DOTParserConstants._NAME:
				name = crnt.value[0].toString();
				break;
			    case DOTParserConstants._VALUE:
				value = crnt.value[0].toString();
				break;
			    }
			}
			if (name == null || value == null) {
			    VM.abort(BADVALUE, new String[] {"DOT Graph Tree: missing name/value" + (subtag == null ? "" : " near " + subtag)});
			}
			attrs.put(name, YoixObject.newString(value));
			break;
		    default:
			VM.abort(BADVALUE, new String[] {"DOT Graph Tree: unexpected content" + (tag == null ? "" : " near " + tag)});
			break;
		    }
		}
		break;

	    case DOTParserConstants._EDGE:
		key = null;
		edgeparts = new Vector();
		edgeports = new Vector();
		for (ii=0; ii<tree.value.length; ii++) {
		    switch ((crnt = ((SimpleNode)(tree.value[ii]))).type()) {
		    case YoixParserConstants.TAG:
			tag = crnt.value[0].toString();
			break;
		    case DOTParserConstants._NAME:
			edgeparts.addElement(crnt.value[0].toString());
			edgeports.addElement(null);
			break;
		    case DOTParserConstants._SUBGRAPH:
			edgeparts.addElement(buildSubgraph(crnt, graph));
			edgeports.addElement(null);
			break;
		    case DOTParserConstants._ATTRIBUTE:
			if ((node = ((SimpleNode)(crnt.value[jj=0]))).type() == YoixParserConstants.TAG)
			    node = (SimpleNode)(crnt.value[jj=1]);
			if (node.value[0].toString().equals("key"))
			    key = ((SimpleNode)(crnt.value[jj+1])).value[0].toString();
			break;
		    case DOTParserConstants._PORT:
			if (crnt.value.length == 1) {
			    edgeports.setElementAt(new String[] { ((SimpleNode)crnt.value[0]).value[0].toString(), null }, edgeports.size() - 1);
			} else { // length == 2
			    edgeports.setElementAt(new String[] { ((SimpleNode)crnt.value[0]).value[0].toString(), ((SimpleNode)crnt.value[1]).value[0].toString() }, edgeports.size() - 1);
			}
			break;
		    }
		}
		if ((epcnt = edgeparts.size()) < 2)
		    VM.abort(BADVALUE, new String[] {"DOT Graph Tree: improper edge specification" + (tag == null ? "" : " near " + tag)});
		for (ee=1; ee<epcnt; ee++) {
		    obj = edgeparts.elementAt(ee-1);
		    if (obj instanceof String) {
			strarr = (String[])edgeports.elementAt(ee-1);
			if (strarr != null)
			    tailparts = new Object[] { new String[] { (String)obj, strarr[0], strarr[1] } };
			else tailparts = new Object[] { new String[] { (String)obj, null, null } };
		    } else {
			vec = ((YoixGraphElement)obj).traverse(true, GRAPH_NODE, -1, null, null, 0);
			tailparts = new Object[vec.size()];
			for (tt=0; tt<tailparts.length; tt++) {
			    gelm = (YoixGraphElement)(vec.elementAt(tt));
			    tailparts[tt] = new String[] {
				gelm.getName(),
				gelm.getTailport(), // null
				gelm.getTailpass()  // null
			    };
			}
		    }
		    obj = edgeparts.elementAt(ee);
		    if (obj instanceof String) {
			strarr = (String[])edgeports.elementAt(ee);
			if (strarr != null)
			    headparts = new Object[] { new String[] { (String)obj, strarr[0], strarr[1] } };
			else headparts = new Object[] { new String[] { (String)obj, null, null } };
		    } else {
			vec = ((YoixGraphElement)obj).traverse(true, GRAPH_NODE, -1, null, null, 0);
			headparts = new Object[vec.size()];
			for (hh=0; hh<headparts.length; hh++) {
			    gelm = (YoixGraphElement)(vec.elementAt(hh));
			    headparts[hh] = new String[] {
				gelm.getName(),
				gelm.getHeadport(), // null
				gelm.getHeadpass()  // null
			    };
			}
		    }
		    for (tt=0; tt<tailparts.length; tt++) {
			strarr = (String[])tailparts[tt];
			tailname = strarr[0];
			tailport = strarr[1];
			tailpass = strarr[2];
			for (hh=0; hh<headparts.length; hh++) {
			    strarr = (String[])headparts[hh];
			    headname = strarr[0];
			    headport = strarr[1];
			    headpass = strarr[2];
			    if ((elem = graph.findEdge(tailname,headname,key)) == null) {
				if ((head = graph.findNode(headname)) == null) {
				    head = new YoixGraphElement(headname, graph, 0);
				    YoixObject.newElement(head);
				}
				if ((tail = graph.findNode(tailname)) == null) {
				    tail = new YoixGraphElement(tailname, graph, 0);
				    YoixObject.newElement(tail);
				}
				elem = new YoixGraphElement(null, graph, tail, tailport, tailpass, head, headport, headpass, key, 0);
				YoixObject.newElement(elem);
			    }
			    attrs = elem.attributes;
			    for (ii=0; ii<tree.value.length; ii++) {
				if ((crnt = ((SimpleNode)(tree.value[ii]))).type() == DOTParserConstants._ATTRIBUTE) {
				    node = crnt;
				    subtag = tag;
				    name = value = null;
				    for (jj=0; jj<node.value.length; jj++) {
					switch ((crnt = ((SimpleNode)(node.value[jj]))).type()) {
					case YoixParserConstants.TAG:
					    subtag = crnt.value[0].toString();
					    break;
					case DOTParserConstants._NAME:
					    name = crnt.value[0].toString();
					    break;
					case DOTParserConstants._VALUE:
					    value = crnt.value[0].toString();
					    break;
					}
				    }
				    if (name == null || value == null)
					VM.abort(BADVALUE, new String[] {"DOT Graph Tree: missing name/value" + (subtag == null ? "" : " near " + subtag)});
				    attrs.put(name, YoixObject.newString(value));
				}
			    }
			}
		    }
		}
		break;

	    case DOTParserConstants._NODE:
		name = null;
		for (ii=0; ii<tree.value.length; ii++) {
		    switch ((crnt = ((SimpleNode)(tree.value[ii]))).type()) {
		    case YoixParserConstants.TAG:
			tag = crnt.value[0].toString();
			break;
		    case DOTParserConstants._NAME:
			name = crnt.value[0].toString();
			ii = tree.value.length;
			break;
		    }
		}
		if (name == null)
		    VM.abort(BADVALUE, new String[] {"DOT Graph Tree: missing node name" + (tag == null ? "" : " near " + tag)});
		if ((elem = graph.findNode(name)) == null) {
		    elem = new YoixGraphElement(name, graph, 0);
		    YoixObject.newElement(elem);
		} else {
		    if (graph != (subg = elem.parent)) {
			subg.makeChildVestigial(elem);
			elem.setParent(graph);
		    }
		}
		attrs = elem.attributes;
		for (ii=0; ii<tree.value.length; ii++) {
		    if ((crnt = ((SimpleNode)(tree.value[ii]))).type() == DOTParserConstants._ATTRIBUTE) {
			node = crnt;
			subtag = tag;
			name = value = null;
			for (jj=0; jj<node.value.length; jj++) {
			    switch ((crnt = ((SimpleNode)(node.value[jj]))).type()) {
			    case YoixParserConstants.TAG:
				subtag = crnt.value[0].toString();
				break;
			    case DOTParserConstants._NAME:
				name = crnt.value[0].toString();
				break;
			    case DOTParserConstants._VALUE:
				value = crnt.value[0].toString();
				break;
			    }
			}
			if (name == null || value == null) {
			    VM.abort(BADVALUE, new String[] {"DOT Graph Tree: missing name/value" + (subtag == null ? "" : " near " + subtag)});
			}
			attrs.put(name, YoixObject.newString(value));
		    }
		}
		break;

	    case DOTParserConstants._SUBGRAPH:
		subg = buildSubgraph(tree, graph);
		break;
	    default:
		VM.abort(BADVALUE, new String[] {"DOT Graph Tree: unexpected content" + (tag == null ? "" : " near " + tag), tree.dump(PARSER_DOT) });
		break;
	    }
	}

	return(graph);
    }


    private static YoixGraphElement
	buildSubgraph(SimpleNode tree, YoixGraphElement graph) {

	YoixGraphElement  elem;
	YoixGraphElement  subg;
	SimpleNode        crnt;
	SimpleNode        node;
	String            name;
	String            tag;
	int               ii;

	name = null;
	node = null;
	for (ii=0; ii<tree.value.length; ii++) {
	    switch ((crnt = ((SimpleNode)(tree.value[ii]))).type()) {
	    case DOTParserConstants._BODY:
		node = crnt;
		break;
	    case YoixParserConstants.TAG:
		tag = crnt.value[0].toString();
		break;
	    case DOTParserConstants._NAME:
		name = crnt.value[0].toString();
		break;
	    }
	}
	if (name == null || (elem = graph.findSubgraph(name)) == null) {
	    elem = new YoixGraphElement(name, graph, graph.flags&(GRAPH_DIRECTED|GRAPH_STRICT), isClusterName(name));
	    YoixObject.newElement(elem);
	} else {
	    if (graph != (subg = elem.parent)) {
		subg.makeChildVestigial(elem);
		elem.setParent(graph);
	    }
	}
	if (node != null) {
	    elem = YoixGraphElement.buildDOTGraph(node, elem.getName(), elem);
	}
	return(elem);
    }


    private static YoixGraphElement
    buildXMLGraph(SimpleNode content, String tag, YoixGraphElement graph) {

	YoixGraphElement  elem;
	YoixGraphElement  head;
	YoixGraphElement  tail;
	Enumeration       enm;
	SimpleNode        tree;
	SimpleNode        node;
	Hashtable         attrs;
	String            type;
	String            name;
	String            value;
	String            headname;
	String            tailname;
	int               flags;
	int               dflt;
	int               pos;

	node = null;
	elem = tail = head = null;
	value = null;

	for (pos=0; pos<content.value.length; pos++) {

	    if (!(content.value[pos] instanceof SimpleNode)) {
		VM.abort(INTERNALERROR, "XML Graph Tree: bad content: " + tag);
	    }

	    tree = (SimpleNode)(content.value[pos]);

	    switch (tree.type()) {
	    case XMLParserConstants._BLOCK:
	    case XMLParserConstants._NOBLOCK:
		if (
		    tree.value.length == 0 ||
		    !(tree.value[0] instanceof SimpleNode) ||
		    (node = (SimpleNode)(tree.value[0])).type() != YoixParserConstants.TAG ||
		    node.value[0] instanceof SimpleNode
		    ) {
		    VM.abort(INTERNALERROR, "Missing Tag: " + tag);
		}
		tag = node.value[0].toString();
		if (
		    tree.value.length < 2 ||
		    !(tree.value[1] instanceof SimpleNode) ||
		    (node = (SimpleNode)(tree.value[1])).type() != XMLParserConstants._NAME ||
		    node.value[0] instanceof SimpleNode
		    ) {
		    VM.abort(INTERNALERROR, "XML Graph Tree: missing identifier: " + tag);
		}
		type = node.value[0].toString().toLowerCase();
		node = processTree(tree, attrs = new Hashtable());
		flags = 0;
		dflt = 0;
		if (tree.type() == XMLParserConstants._BLOCK) {
		    if ((name = (String)attrs.get(NAME_KYWD)) == null)
			VM.abort(BADVALUE, new String[] {"XML Graph Tree: missing name: " + tag});
		    else attrs.remove(NAME_KYWD);
		    if (graph == null) {
			if (type.equals(GRAPH_KYWD)) {
			    if ((value = (String)attrs.get(DIRECTED_KYWD)) != null) {
				if (value.charAt(0) != '0')
				    flags |= GRAPH_DIRECTED;
				attrs.remove(DIRECTED_KYWD);
			    }
			    if ((value = (String)attrs.get(STRICT_KYWD)) != null) {
				if (value.charAt(0) != '0')
				    flags |= GRAPH_STRICT;
				attrs.remove(STRICT_KYWD);
			    }
			    elem = graph = new YoixGraphElement(name, flags);
			} else VM.abort(BADVALUE, new String[] {"XML Graph Tree: '" + type + "' " + tag});
		    } else {
			if (type.equals(SUBGRAPH_KYWD)) {
			    flags |= (graph.getFlags())&(GRAPH_STRICT|GRAPH_DIRECTED);
			    // TODO: cluster boolean
			    elem = new YoixGraphElement(name, graph, flags, false);
			} else VM.abort(BADVALUE, new String[] {"XML Graph Tree: '" + type + "' " + tag});
		    }
		    if (node != null)
			buildXMLGraph(node, tag, elem);
		} else {
		    if (graph == null)
			VM.abort(BADVALUE, new String[] {"XML Graph Tree: '" + type + "' " + tag});
		    if (type.equals(NODE_KYWD)) {
			if ((name = (String)attrs.get(NAME_KYWD)) == null) {
			    VM.abort(BADVALUE, new String[] {"XML Graph Tree: missing name: " + tag});
			} else attrs.remove(NAME_KYWD);
			if ((elem = (YoixGraphElement)graph.root.elements.get(name)) == null) {
			    elem = new YoixGraphElement(name, graph, 0);
			} else {
			    if (graph != elem.getParent()) {
				if (!elem.ofType(GRAPH_EDGEMADE)) {
				    VM.abort(BADVALUE, new String[] {"XML Graph Tree: '" + name + "' ambiguous parent: " + tag});
				}
				elem.setParent(graph);
			    }
			    elem.delFlag(GRAPH_EDGEMADE);
			}
		    } else if (type.equals(EDGE_KYWD)) {
			if ((name = (String)attrs.get(NAME_KYWD)) == null)
			    VM.abort(BADVALUE, new String[] {"XML Graph Tree: missing name: " + tag});
			else attrs.remove(NAME_KYWD);
			if ((headname = (String)attrs.get(HEAD_KYWD)) != null) {
			    if ((head = (YoixGraphElement)graph.root.elements.get(headname)) == null)
				head = new YoixGraphElement(headname, graph, GRAPH_EDGEMADE);
			    attrs.remove(HEAD_KYWD);
			} else VM.abort(SYNTAXERROR, "XML Graph Tree: '" + name + "' missing head: " + tag);
			if ((tailname = (String)attrs.get(TAIL_KYWD)) != null) {
			    if ((tail = (YoixGraphElement)graph.root.elements.get(tailname)) == null)
				tail = new YoixGraphElement(tailname, graph, GRAPH_EDGEMADE);
			    attrs.remove(TAIL_KYWD);
			} else VM.abort(SYNTAXERROR, "XML Graph Tree: '" + name + "' missing tail: " + tag);
			if ((value = (String)attrs.get(FORWARD_KYWD)) != null) {
			    if (value.charAt(0) != '0')
				flags |= GRAPH_FORWARD;
			    attrs.remove(FORWARD_KYWD);
			}
			if ((value = (String)attrs.get(REVERSE_KYWD)) != null) {
			    if (value.charAt(0) != '0')
				flags |= GRAPH_REVERSE;
			    attrs.remove(REVERSE_KYWD);
			}
			elem = new YoixGraphElement(name, graph, tail, head, null, flags);
		    } else if (type.equals(GRAPH_ATTR_KYWD)) {
			elem = graph;
			dflt = GRAPHDFLT;
		    } else if (type.equals(NODE_ATTR_KYWD)) {
			elem = graph;
			dflt = NODEDFLT;
		    } else if (type.equals(EDGE_ATTR_KYWD)) {
			elem = graph;
			dflt = EDGEDFLT;
		    } else VM.abort(BADVALUE, new String[] {"XML Graph Tree: '" + type + "' " + tag});
		}
		if (elem.getWrapper() == null)
		    YoixObject.newElement(elem);
		for (enm = attrs.keys(); enm.hasMoreElements(); ) {
		    name = ((String)(enm.nextElement())).toLowerCase();
		    value = (String)(attrs.get(name));
		    // TODO: try to finesse value a bit (rather than just newString)??
		    elem.addLocalAttribute(dflt, name, YoixObject.newString(value));
		}
		break;
	    case YoixParserConstants.TAG:
		tag = tree.value[0].toString();
		break;
	    case XMLParserConstants._CHARDATA:
	    case XMLParserConstants._COMMENT:
		// ignore spurious text and comments
		break;
	    default:
		VM.abort(BADVALUE, new String[] {"XML Graph Tree: bad type '" + SimpleNode.typeString(tree.type) + "' " + tag});
		break;
	    }
	}

	return(graph);
    }


    private void
    delFlag(int flag) {

	flag &= ~TYPE_MASK;
	flags &= ~flag;
    }


    private boolean
    direction(int dir) {

	int  flg = flags & DIRECTION_MASK;

	return(flg == 0 || (flg&dir) != 0);
    }


    private static void
    doBDFS(int type, int depth, int stamp, int level, Vector inbox, Vector stack) {

	YoixGraphElement  elem;
	YoixGraphElement  current;
	int               sz;
	int               szz;
	Vector            input;

	sz = inbox.size();
	level++;

	if (sz == 0 || (depth >= 0 && level > depth))
	    return;

	input = new Vector();

	for (int i=0; i<sz; i++) {
	    elem = (YoixGraphElement)inbox.elementAt(i);
	    if (elem.visastamp == stamp) {
		continue;
	    }

	    elem.visastamp = stamp;
	    if (type == GRAPH_GRAPH) {
		stack.addElement(elem);
		if (depth < 0 || level < depth) {
		    if ((szz = elem.children.size()) > 0) {
			for (int j=0; j<szz; j++) {
			    current = (YoixGraphElement)(elem.children.elementAt(j));
			    if (current.visastamp != stamp)
				input.addElement(current);
			}
		    }
		}
	    } else if (type == GRAPH_NODE) {
		stack.addElement(elem);
		if (depth < 0 || level < depth) {
		    if ((szz = elem.outbound.size()) > 0) {
			for (int j=0; j<szz; j++) {
			    current = (YoixGraphElement)(elem.outbound.elementAt(j));
			    if (current.direction(GRAPH_FORWARD)) {
				if (current.head.visastamp != stamp) {
				    input.addElement(current.head);
				}
			    }
			}
		    }
		    if ((szz = elem.inbound.size()) > 0) {
			for (int j=0; j<szz; j++) {
			    current = (YoixGraphElement)(elem.inbound.elementAt(j));
			    if (current.direction(GRAPH_REVERSE)) {
				if (current.tail.visastamp != stamp) {
				    input.addElement(current.tail);
				}
			    }
			}
		    }
		}
	    } else { // type == GRAPH_EDGE
		stack.addElement(elem);
		if (depth < 0 || level < depth) {
		    if (elem.direction(GRAPH_FORWARD)) {
			if ((szz = elem.head.outbound.size()) > 0) {
			    for (int j=0; j<szz; j++) {
				current = (YoixGraphElement)(elem.head.outbound.elementAt(j));
				if (current.direction(GRAPH_FORWARD)) {
				    if (current.visastamp != stamp) {
					input.addElement(current);
				    }
				}
			    }
			}
			if ((szz = elem.head.inbound.size()) > 0) {
			    for (int j=0; j<szz; j++) {
				current = (YoixGraphElement)(elem.head.inbound.elementAt(j));
				if (current.direction(GRAPH_REVERSE)) {
				    if (current.visastamp != stamp) {
					input.addElement(current);
				    }
				}
			    }
			}
		    }
		    if (elem.direction(GRAPH_REVERSE)) {
			if ((szz = elem.tail.outbound.size()) > 0) {
			    for (int j=0; j<szz; j++) {
				current = (YoixGraphElement)(elem.tail.outbound.elementAt(j));
				if (current.direction(GRAPH_FORWARD)) {
				    if (current.visastamp != stamp) {
					input.addElement(current);
				    }
				}
			    }
			}
			if ((szz = elem.tail.inbound.size()) > 0) {
			    for (int j=0; j<szz; j++) {
				current = (YoixGraphElement)(elem.tail.inbound.elementAt(j));
				if (current.direction(GRAPH_REVERSE)) {
				    if (current.visastamp != stamp) {
					input.addElement(current);
				    }
				}
			    }
			}
		    }
		}
	    }
	}

	if (input.size() > 0)
	    doBDFS(type, depth, stamp, level, input, stack);
    }


    private boolean
    doClimbGlobalScope(YoixGraphElement baggage, Object carryon, int indicators, boolean initial) {

	YoixGraphElement  prnt;
	Enumeration       enm;

	for (prnt=this; prnt != null && (initial || prnt.parent != null); prnt = prnt.parent) {
	    if (baggage.sightsee(this, indicators, true, carryon))
		return(true);
	}

	if (vestigialparents != null && (enm = vestigialparents.elements()) != null) {
	    while(enm.hasMoreElements()) {
		prnt = (YoixGraphElement)(enm.nextElement());
		if (prnt.doClimbGlobalScope(baggage, carryon, indicators, false))
		    return(true);
	    }
	}

	return(false);
    }


    private boolean
    doTraversal(Vector stack, int type, int depth, YoixGraphElement baggage, Object carryon, int indicators, int stamp, int level) {

	YoixGraphElement  current;
	int               sz;

	if (visastamp == stamp) {
	    return(false);
	}

	visastamp = stamp;

	level++;

	if (depth >= 0 && level > depth)
	    return(false);

	if (stack != null && ofType(type))
	    stack.addElement(this);

	if (baggage != null && ofType(type))
	    if (baggage.sightsee(this, indicators, true, carryon))
		return(true);

	if (isType(GRAPH_GRAPH)) {
	    if (depth < 0 || level < depth) {
		current = this;
		while ((current = current.right_sibling) != this) {
		    if (current.doTraversal(stack, type, depth, baggage, carryon, indicators, stamp, level))
			return(true);
		}

		if ((sz = children.size()) > 0) {
		    for (int i=0; i<sz; i++) {
			if (((YoixGraphElement)children.elementAt(i)).doTraversal(stack, type, depth, baggage, carryon, indicators, stamp, level))
			    return(true);
		    }
		}
	    }

	    if (baggage != null && ofType(type))
		if (baggage.sightsee(this, indicators, false, carryon))
		    return(true);
	}
	return(false);
    }


    private int
    getGeneration() {

	YoixGraphElement  prnt = parent;

	return((prnt == null || prnt.isType(GRAPH_PLACEHOLDER)) ? 0 : 1 + prnt.getGeneration());
    }


    private String
    getIndent(String initial, Integer depth) {

	StringBuffer  buf;
	int           cycles;

	cycles = getGeneration();

	if (depth != null) {
	    cycles -= depth.intValue();
	    if (cycles < 0)
		VM.abort(INTERNALERROR); // should not happen - just checking
	}

	buf = (initial == null) ? new StringBuffer() : new StringBuffer(initial);

	//
	// We synchronize here, but only because it makes StringBuffer's
	// append() run much faster!!
	//

	synchronized(buf) {
	    while (cycles-- > 0)
		buf.append("    ");
	}
	return(buf.toString());
    }


    private int
    getLevel() {

	YoixGraphElement  prnt = parent;

	return(prnt == null ? 0 : 1 + prnt.getLevel());
    }


    private synchronized static int
    getMarker() {

	return(marker == Integer.MAX_VALUE ? marker = 0 : marker++);
    }


    private int
    getType() {

	return(flags&TYPE_MASK);
    }


    private boolean
    isType(int flag) {

	return((flags&flag) == flag);
    }


    private static YoixGraphElement
    loadDOTGraph(SimpleNode tree) {

	YoixGraphElement  graph = null;
	SimpleNode        node;
	SimpleNode        crnt;
	String            name = null;
	String            tag = null;
	int               flags = 0;
	int               size;
	int               type;

	// basic validation
	if (tree instanceof YoixObject) {
	    VM.abort(((YoixObject)tree).getString(N_MESSAGE, ""));
	} else if (tree != null && tree.type() != YOIX_EOF) {
	    if (tree.type() != DOTParserConstants._FOLDER)
		VM.abort(INTERNALERROR);

	    if (((SimpleNode)(tree.value[0])).type() != DOTParserConstants._GRAPH)
		VM.abort(BADVALUE, new String[] {"DOT Graph Tree: wrong tree type (not a DOT graph)"});

	    if (tree.value.length > 1)
		VM.abort(BADVALUE, new String[] {"DOT Graph Tree: multiple graphs"});

	    tree = (SimpleNode)(tree.value[0]);

	    node = null;
	    tag = null;

	    for (int i=0; i<tree.value.length; i++) {
		switch ((crnt = ((SimpleNode)(tree.value[i]))).type()) {
		case DOTParserConstants._BODY:
		    node = crnt;
		    break;
		case DOTParserConstants._DIGRAPH:
		    flags |= crnt.value[0].toString().charAt(0) == '1' ? GRAPH_DIRECTED : 0;
		    break;
		case DOTParserConstants._NAME:
		    name = crnt.value[0].toString();
		    break;
		case DOTParserConstants._STRICT:
		    flags |= crnt.value[0].toString().charAt(0) == '1' ? GRAPH_STRICT : 0;
		    break;
		case YoixParserConstants.TAG:
		    tag = crnt.value[0].toString();
		    break;
		default:
		    VM.abort(BADVALUE, new String[] {"DOT Graph Tree: bad graph tree structure"});
		    break;
		}
	    }

	    if (node != null && name != null) {
		tree = node;
		if (
		    tree.type() != DOTParserConstants._BODY ||
		    tree.value.length < 1 ||
		    !(tree.value[0] instanceof SimpleNode)
		    ) {
		    VM.abort(BADVALUE, new String[] {"DOT Graph Tree: invalid parse tree" + (tag == null ? "" : " " + tag)});
		}

		// recursion
		graph = YoixGraphElement.buildDOTGraph(tree, name,
		    new YoixGraphElement(name, flags));
		YoixObject.newElement(graph);
	    } else VM.abort(BADVALUE, new String[] {"DOT Graph Tree: missing elements" + (tag == null ? "" : " " + tag)});

	}
	return(graph);
    }


    private static YoixGraphElement
    loadXMLGraph(SimpleNode tree) {

	YoixGraphElement  graph = null;
	SimpleNode        node;
	String            tag;
	int               size;
	int               type;

	// basic validation
	if (tree instanceof YoixObject) {
	    VM.abort(((YoixObject)tree).getString(N_MESSAGE, ""));
	} else if (tree != null && tree.type() != YOIX_EOF) {
	    if (tree.type() != XMLParserConstants._FOLDER)
		VM.abort(INTERNALERROR);

	    if (((SimpleNode)(tree.value[0])).type() != XMLParserConstants._XML)
		VM.abort(INTERNALERROR);

	    if (tree.value.length > 1)
		VM.abort(BADVALUE, new String[] {"multiple graphs"});

	    tree = (SimpleNode)(tree.value[0]);

	    node = null;

	    for (int i=0; i<tree.value.length; i++) {
		if ((node = ((SimpleNode)(tree.value[i]))).type() == XMLParserConstants._BODY) {
		    break;
		}
	    }

	    if (node != null) {
		tree = node;
		if (
		    tree.type() != XMLParserConstants._BODY ||
		    tree.value.length < 2 ||
		    !(tree.value[0] instanceof SimpleNode) ||
		    !(tree.value[1] instanceof SimpleNode)
		    ) {
		    VM.abort(INTERNALERROR);
		}

		node = (SimpleNode)(tree.value[0]);

		if (
		    node.type() != YoixParserConstants.TAG ||
		    node.value.length != 1 ||
		    node.value[0] instanceof SimpleNode
		    ) {
		    VM.abort(INTERNALERROR);
		}

		tag = node.value[0].toString();
		node = (SimpleNode)(tree.value[1]);

		if (
		    node.type() != XMLParserConstants._BLOCK ||
		    node.value.length == 0 ||
		    !(node.value[0] instanceof SimpleNode)
		    ) {
		    VM.abort(INTERNALERROR, SimpleNode.typeString(node.type) + ": " + tag);
		}
		// recursion
		graph = YoixGraphElement.buildXMLGraph(tree, tag, graph);
	    }

	}
	return(graph);
    }


    private void
    makeChildVestigial(YoixGraphElement child) {

	if (vestigialchildren == null) {
	    vestigialchildren = new Vector();
	    vestigialchildren.addElement(child);
	    if (child.vestigialparents == null)
		child.vestigialparents = new Vector();
	    child.vestigialparents.addElement(this);
	} else {
	    if (vestigialchildren.contains(child)) {
		// child order does not matter, but we want this
		// parent to be positioned as most recent
		child.vestigialparents.removeElement(this);
		child.vestigialparents.addElement(this);
	    } else {
		vestigialchildren.addElement(child);
		if (child.vestigialparents == null)
		    child.vestigialparents = new Vector();
		child.vestigialparents.addElement(this);
	    }
	}
    }


    private static String
    nameCache(String name) {

	String  ret;

	if ((ret = (String)(names.get(name))) == null) {
	    names.put(name, name);
	    ret = name;
	}

	return(ret);
    }


    private static SimpleNode
    processTree(SimpleNode tree, Hashtable attrs) {

	SimpleNode  content;
	SimpleNode  node;
	String      name;
	String      value;
	int         size;
	int         i;

	content = null;

	if ((size = tree.value.length) > 0) {
	    for (i=0; i<size; i++) {
		if (tree.value[i] instanceof SimpleNode) {
		    node = (SimpleNode)(tree.value[i]);
		    if (node.type() == XMLParserConstants._ATTRIBUTE) {
			// assumes ATTRIBUTE never has a TAG
			if (
			   node.value.length != 2 ||
			   !(node.value[0] instanceof SimpleNode) ||
			   ((SimpleNode)(node.value[0])).value.length != 1 ||
			   ((SimpleNode)(node.value[0])).value[0] instanceof SimpleNode ||
			   !(node.value[1] instanceof SimpleNode) ||
			   ((SimpleNode)(node.value[1])).value.length != 1 ||
			   ((SimpleNode)(node.value[1])).value[0] instanceof SimpleNode
			   ) {
			    VM.abort(INTERNALERROR, "bad attribute"); // should we be so thorough???
			}
			name = ((SimpleNode)(node.value[0])).value[0].toString().toLowerCase();
			value = ((SimpleNode)(node.value[1])).value[0].toString().toLowerCase();
			attrs.put(name, value);
		    } else if (node.type() == XMLParserConstants._CONTENT) {
			if (content == null) {
			    if (
			       node.value.length == 0 ||
			       !(node.value[0] instanceof SimpleNode)
			       ) {
				VM.abort(INTERNALERROR, "bad content"); // should we be so thorough???
			    }
			    content = node;
			} else VM.abort(INTERNALERROR, "multiple content");
		    }
		}
	    }
	}
	return(content);
    }


    private static String
    quoteText(String text, boolean needsQuotes) {
	return(quoteText(text,needsQuotes,false));
    }


    private static String
    quoteText(String text, boolean needsQuotes, boolean isDot) {

	StringBuffer  buf;
	char          chars[];
	int           i;

	if (text != null) {
	    buf = new StringBuffer(text.length() + 5);
	    chars = text.toCharArray();
	    for (i = 0; i < chars.length; i++) {
		if (chars[i] == '"') {
		    needsQuotes = true;
		    buf.append('\\');
		} else if (isDot && !Character.isLetterOrDigit(chars[i]))
		    needsQuotes = true;
		else if (!isDot && Character.isWhitespace(chars[i]))
		    needsQuotes = true;
		buf.append(chars[i]);
	    }
	    if (needsQuotes) {
		buf.insert(0,'"');
		buf.append('"');
		text = buf.toString();
	    }
	} else if (needsQuotes)
	    text = "\"\"";
	return(text);
    }


    private void
    removeChild(YoixGraphElement subgraph) {

	children.removeElement(subgraph);
    }


    private static void
    removeEdge(YoixGraphElement edge) {

	edge.tail.outbound.removeElement(edge);
	edge.head.inbound.removeElement(edge);
    }


    private void
    removeSibling(YoixGraphElement sib) {

	YoixGraphElement  current;

	if (sib == this || sib.parent != this || left_sibling == null) {
	    VM.abort(INTERNALERROR); // should never happen
	} else {
	    sib.left_sibling.right_sibling = sib.right_sibling;
	    sib.right_sibling.left_sibling = sib.left_sibling;
	}
    }


    private void
    setName(String name) {

	boolean  supplied = true;

	if (name == null) {
	    supplied = false;
	    name = ANON_PREFIX + getMarker();
	}

	while (root.elements.containsKey(name)) {
	    if (supplied)
		VM.abort(NAMECLASH, name);
	    name = ANON_PREFIX + getMarker();
	}

	root.elements.put(name, this);
	this.name = name;
    }


    private void
    setEdgeName(String name, YoixGraphElement tail, YoixGraphElement head) {

	String  prefix;
	String  headname;
	String  tailname;
	int     counter;

	if (name == null) {
	    if (head != null && tail != null) {
		if ((headname = head.getName()) != null) {
		    if ((tailname = tail.getName()) != null) {
			prefix = tailname + "--" + headname;
			name = prefix;
			for (counter = 2; root.elements.containsKey(name); counter++)
			    name = prefix + "[" + counter + "]";
		    }
		}
	    }
	}
	setName(name);
    }


    private void
    switchParent(YoixGraphElement prnt) {

	switch (getType()) {
	    case GRAPH_GRAPH:
		if (parent != null) {
		    parent.removeChild(this);
		    parent.queueInfo(new Object[]{this,DELETED});
		}
		if ((parent = prnt) != null) {
		    parent.addChild(this);
		    parent.queueInfo(new Object[]{this,CREATED});
		}
		break;

	    case GRAPH_NODE:
	    case GRAPH_EDGE:
		if (parent != null) {
		    parent.removeSibling(this);
		    parent.queueInfo(new Object[]{this,DELETED});
		}
		if ((parent = prnt) != null) {
		    parent.addSibling(this);
		    parent.queueInfo(new Object[]{this,CREATED});
		}
		break;
	}
    }


    private boolean
    sightsee(YoixGraphElement elem, int indicators, boolean initial, Object carryon) {

	switch (indicators) {
	case XML_TEXTUAL:
	    {
		Object[] parcel = (Object[])carryon;
		StringBuffer output = (StringBuffer)parcel[0];
		String indent = (String)parcel[1];
		Integer depth = (Integer)parcel[2];
		YoixBodyGraphObserver  obs = (YoixBodyGraphObserver)parcel[3];

		String offset;

		String elemtype;
		YoixGraphElement start, current;

		boolean  hasData = false;

		offset = elem.getIndent(indent, depth);

		synchronized(output) {
		    if (elem.getGraphData(obs, this) != null) {
			switch (obs.manager_type) {
			case LAYOUT_FORCE_BOUND:
			case LAYOUT_FORCE:
			    if (elem.isType(GRAPH_NODE))
				hasData = true;
			    else
				hasData = false;
			    break;
			default:
			    hasData = false;
			    break;
			}
		    }

		    if (elem.isType(GRAPH_GRAPH)) {
			if (elem.parent == null || elem.parent.isType(GRAPH_PLACEHOLDER))
			    elemtype = GRAPH_KYWD;
			else {
			    elemtype = SUBGRAPH_KYWD;
			}

			if (initial) {
			    output.append(offset);
			    output.append('<');
			    output.append(elemtype);
			    output.append(' ');
			    output.append(NAME_KYWD);
			    output.append('=');
			    output.append(quoteText(elem.getName(), false));

			    elem.appendFlags(output);
			    elem.appendAttributes(elem.attributes, output, XML_TEXTUAL, hasData);
			    output.append('>');
			    output.append(NL);

			    if (elem.subg_attributes.size() > 0) {
				output.append(offset);
				output.append("    <");
				output.append(GRAPH_ATTR_KYWD);
				elem.appendAttributes(elem.subg_attributes, output, XML_TEXTUAL, false);
				output.append(" />");
				output.append(NL);
			    }

			    if (elem.node_attributes.size() > 0) {
				output.append(offset);
				output.append("    <");
				output.append(NODE_ATTR_KYWD);
				elem.appendAttributes(elem.node_attributes, output, XML_TEXTUAL, false);
				output.append(" />");
				output.append(NL);
			    }

			    if (elem.edge_attributes.size() > 0) {
				output.append(offset);
				output.append("    <");
				output.append(EDGE_ATTR_KYWD);
				elem.appendAttributes(elem.edge_attributes, output, XML_TEXTUAL, false);
				output.append(" />");
				output.append(NL);
			    }

			} else {
			    output.append(offset);
			    output.append("</");
			    output.append(elemtype);
			    output.append('>');
			    output.append(NL);
			}
		    } else {
			if (elem.isType(GRAPH_NODE)) {
			    elemtype = NODE_KYWD;
			} else {
			    elemtype = EDGE_KYWD;
			}

			output.append(offset);
			output.append('<');
			output.append(elemtype);
			output.append(' ');
			output.append(NAME_KYWD);
			output.append('=');
			output.append(quoteText(elem.getName(), false));
			elem.appendFlags(output);
			elem.appendAttributes(elem.attributes, output, XML_TEXTUAL, hasData);
			output.append(" />");
			output.append(NL);

		    }
		}
		//TESTING
		//if(elem.notifier != null) {
		    //elem.notifier.notifyObservers();
		//}
	    }
	    break;
	case DOT_TEXTUAL:
	    {
		Object[] parcel = (Object[])carryon;
		StringBuffer output = (StringBuffer)parcel[0];
		String indent = (String)parcel[1];
		Integer depth = (Integer)parcel[2];
		YoixBodyGraphObserver  obs = (YoixBodyGraphObserver)parcel[3];

		String offset;
		String arrow;

		YoixGraphElement start, current;
		Hashtable merged;
		Enumeration enm;
		String key;

		int  sz = 0;
		boolean  hasData = false;

		offset = elem.getIndent(indent, depth);

		// synchronize once on buffer since
		// we are doing a lot of appends and each
		// of those wants to synchronize
		synchronized(output) {
		    sz = elem.attributes.size();

		    if (elem.getGraphData(obs, this) != null) {
			switch (obs.manager_type) {
			case LAYOUT_FORCE_BOUND:
			case LAYOUT_FORCE:
			    if (elem.isType(GRAPH_NODE))
				hasData = true;
			    else
				hasData = false;
			    break;
			default:
			    hasData = false;
			    break;
			}
		    }

		    if (elem.isType(GRAPH_GRAPH)) {
			if (initial) {
			    if (hasData)
				sz++;
			    output.append(offset);
			    if (elem.parent == null || elem.parent.isType(GRAPH_PLACEHOLDER)) {
				if ((elem.getFlags()&GRAPH_STRICT) == GRAPH_STRICT) {
				    output.append(STRICT_KYWD);
				    output.append(" ");
				}
				if ((elem.getFlags()&GRAPH_DIRECTED) == GRAPH_DIRECTED)
				    output.append(DIGRAPH_KYWD);
				else output.append(GRAPH_KYWD);
				output.append(' ');
				output.append(quoteText(elem.getName(), false, true));
				output.append(' ');
			    } else if (!elem.getName().startsWith(ANON_PREFIX)) {
				output.append(SUBGRAPH_KYWD);
				output.append(' ');
				output.append(quoteText(elem.getName(), false, true));
				output.append(' ');
			    }
			    output.append('{');
			    output.append(NL);

			    if (elem.subg_attributes.size() > 0 || sz > 0) {
				output.append(offset);
				output.append("    ");
				output.append(GRAPH_KYWD);
				output.append(" [");
				merged = (Hashtable)elem.subg_attributes.clone();
				for (enm = elem.attributes.keys(); enm.hasMoreElements(); ) {
				    key = (String)enm.nextElement();
				    merged.put(key, elem.attributes.get(key));
				}
				elem.appendAttributes(merged, output, DOT_TEXTUAL, hasData, NL + offset + "        ", ",");
				output.append(NL);
				output.append(offset);
				output.append("    ];");
				output.append(NL);
			    }

			    if (elem.node_attributes.size() > 0) {
				output.append(offset);
				output.append("    ");
				output.append(NODE_KYWD);
				output.append(" [");
				elem.appendAttributes(elem.node_attributes, output, DOT_TEXTUAL, false, NL + offset + "        ", ",");
				output.append(NL);
				output.append(offset);
				output.append("    ];");
				output.append(NL);
			    }

			    if (elem.edge_attributes.size() > 0) {
				output.append(offset);
				output.append("    ");
				output.append(EDGE_KYWD);
				output.append(" [");
				elem.appendAttributes(elem.edge_attributes, output, DOT_TEXTUAL, false, NL + offset + "        ", ",");
				output.append(NL);
				output.append(offset);
				output.append("    ];");
				output.append(NL);
			    }
			} else {
			    output.append(offset);
			    output.append('}');
			    output.append(NL);
			}
		    } else {
			output.append(offset);

			if (elem.isType(GRAPH_NODE)) {
			    if (hasData)
				sz++;
			    output.append(quoteText(elem.getName(), false, true));
			} else {
			    if (elem.isType(GRAPH_FORWARD|GRAPH_REVERSE)) {
				arrow = " <-> ";
			    } else if (!elem.isType(GRAPH_FORWARD) && !elem.isType(GRAPH_REVERSE)) {
				arrow = " -- ";
			    } else if (elem.isType(GRAPH_FORWARD)) {
				arrow = " -> ";
			    } else {
				arrow = null;
			    }
			    if (arrow != null) {
				output.append(quoteText(elem.tail.getName(), false, true));
				if (elem.tailport != null) {
				    output.append(":");
				    output.append(quoteText(elem.tailport, false, true));
				}
				if (elem.tailpass != null) {
				    output.append(":");
				    output.append(quoteText(elem.tailpass, false, true));
				}
				output.append(arrow);
				output.append(quoteText(elem.head.getName(), false, true));
				if (elem.headport != null) {
				    output.append(":");
				    output.append(quoteText(elem.headport, false, true));
				}
				if (elem.headpass != null) {
				    output.append(":");
				    output.append(quoteText(elem.headpass, false, true));
				}
			    } else {
				output.append(quoteText(elem.head.getName(), false, true));
				if (elem.headport != null) {
				    output.append(":");
				    output.append(quoteText(elem.headport, false, true));
				}
				if (elem.headpass != null) {
				    output.append(":");
				    output.append(quoteText(elem.headpass, false, true));
				}
				output.append(" -> ");
				output.append(quoteText(elem.tail.getName(), false, true));
				if (elem.tailport != null) {
				    output.append(":");
				    output.append(quoteText(elem.tailport, false, true));
				}
				if (elem.tailpass != null) {
				    output.append(":");
				    output.append(quoteText(elem.tailpass, false, true));
				}
			    }
			}

			if (sz > 0) {
			    output.append(" [");
			    elem.appendAttributes(elem.attributes, output, DOT_TEXTUAL, hasData, NL + offset + "    ", ",");
			    output.append(NL);
			    output.append(offset);
			    output.append(']');
			}
			output.append(';');
			output.append(NL);
		    }
		}
	    }
	    break;
	case DRAW_LAYOUT:
	    {
		Object[] parcel = (Object[])carryon;
		YoixBodyGraphObserver  obs = (YoixBodyGraphObserver)parcel[0];
		Color                  fg = (Color)parcel[1];
		Color                  bg = (Color)parcel[2];
		Rectangle              clip = (Rectangle)parcel[3];
		Graphics2D             gr = (Graphics2D)parcel[4];
		YoixGraphBase          graphdata;

// 		if ((graphdata = elem.getGraphData(obs, this)) != null) {
// 		    System.err.println("graphdata="+graphdata.x+","+graphdata.y);
// 		} else {
// 		    System.err.println("graphdata=null");
// 		}

	    }
	    break;
	case NAMECHECK:
	    if (initial) {
		YoixGraphElement chk_root = (YoixGraphElement)carryon;
		if (isType(GRAPH_GRAPH)) {
		    YoixGraphElement current = this;
		    if (current.root != chk_root)
			if (chk_root.elements.containsKey(current.getName()))
			    VM.abort(NAMECLASH, current.getName());
		    while (current.right_sibling != this) {
			current = current.right_sibling;
			if (current.root != chk_root)
			    if (chk_root.elements.containsKey(current.getName()))
				VM.abort(NAMECLASH, current.getName());
		    }
		}
	    }
	    break;
	case NEWDATA:
	    if (initial) {
		Object[] args = (Object[])carryon;
		YoixBodyGraphObserver obs = (YoixBodyGraphObserver)args[0];
		YoixGraphBase datatemplate = (YoixGraphBase)args[1];
		YoixGraphBase  crnt, prev, newdata = null;

		newdata = (YoixGraphBase)(datatemplate.clone());

		newdata.obs = obs;
		newdata.grf = this;

		if (elem.graphdata != null) {

		    prev = crnt = elem.graphdata;
		    while ((crnt.obs != obs || crnt.grf != this) && crnt.next != elem.graphdata) {
			prev = crnt;
			crnt = crnt.next;
		    }
		    if (crnt.obs == obs && crnt.grf == this) {

			if (crnt == elem.graphdata) {
			    if (crnt.next != crnt) {
				// haven't moved, so we have to find previous
				prev = crnt;
				while (prev.next != crnt) {
				    prev = prev.next;
				}

				// do replacement
				newdata.next = prev.next;
				prev.next = newdata;
			    }
			    // else only one element in ring, so just
			    // replace the whole ring (done below)
			} else {
			    // have previous, just do replacement
			    newdata.next = prev.next;
			    prev.next = newdata;
			}
		    } else {
			// insert new data
			newdata.next = crnt.next;
			crnt.next = newdata;
		    }
		}
		newdata.initElem(elem);
		// make sure graphdata now references most recently
		// accessed member (namely the new data)
		elem.graphdata = newdata;
	    }
	    break;
	case SETROOT:
	    if (initial) {
		root = (YoixGraphElement)carryon;
		if (isType(GRAPH_GRAPH)) {
		    YoixGraphElement current = this;
		    while (current.right_sibling != this) {
			current = current.right_sibling;
			if (current.root != root) {
			    current.root.elements.remove(current.getName());
			    current.root = root;
			    current.root.elements.put(current.getName(), current);
			}
		    }
		}
	    }
	    break;
	case WALKER:
	    if (initial) {
		YoixObject ret;
		YoixObject args[] = (YoixObject[])carryon;
		if (elem.getWrapper() != null) {
		    if ((ret = elem.getWrapper().call(args)) != null) {
			if (ret.isInteger() && ret.intValue() != 0) {
			    return(true);
			}
		    }
		}
	    }
	    break;
	case GLOBAL_ATTR:
	    if (initial) {
		Hashtable    attrs;
		Object args[] = (Object[])carryon;

		switch(((Integer)args[0]).intValue()) {
		case GRAPHDFLT:
		    attrs = elem.subg_attributes;
		    break;
		case NODEDFLT:
		    attrs = elem.node_attributes;
		    break;
		case EDGEDFLT:
		    attrs = elem.edge_attributes;
		    break;
		default:
		    VM.abort(INTERNALERROR);
		    attrs = null;
		}

		if((args[2] = attrs.get((String)args[1])) != null)
		    return(true);
	    }
	    break;
	case GLOBAL_ATTRS:
	    if (initial) {
		Object       args[] = (Object[])carryon;
		String       name;
		Object       value;
		Hashtable    attrs;
		Hashtable    hash;
		Enumeration  enm;

		switch(((Integer)args[0]).intValue()) {
		case GRAPHDFLT:
		    attrs = elem.subg_attributes;
		    break;
		case NODEDFLT:
		    attrs = elem.node_attributes;
		    break;
		case EDGEDFLT:
		    attrs = elem.edge_attributes;
		    break;
		default:
		    VM.abort(INTERNALERROR);
		    attrs = null;
		}

		hash = (Hashtable)args[1];
		for (enm = attrs.keys(); enm.hasMoreElements(); ) {
		    name = (String)enm.nextElement();
		    if (!hash.containsKey(name))
			if ((value = attrs.get(name)) != null)
			    hash.put(name, value);
		}
	    }
	    break;
	}
	return(false);
    }

    ///////////////////////////////////
    //
    // Customized Observable class
    //
    ///////////////////////////////////

    class InnerObservable {

	private Vector  notifications;
	private Vector  obs;

	public
	InnerObservable() {
	}


	public void
	addObserver(YoixBodyGraphObserver o) {

	    notifyObservers();

	    synchronized(this) {
		if (obs == null) {
		    obs = new Vector();
		}

		if (!obs.contains(o)) {
		    obs.addElement(o);
		}
	    }
	}


	public synchronized void
	deleteObserver(YoixBodyGraphObserver o) {

	    if (obs != null) {
		obs.removeElement(o);
		if (obs.size() == 0)
		    obs = null;
	    }
	}


	public synchronized void
	deleteObservers() {

	    if (obs != null) {
		obs.removeAllElements();
		obs = null;
	    }
	}


	public synchronized void
	queueInfo(Object info[]) {

	    if (info.length < 2)
		VM.abort(INTERNALERROR);

	    if (obs == null)
		return;

	    if (notifications == null) {
		notifications = new Vector();
	    }

	    notifications.addElement((Object[])(info.clone()));
	}

	public void
	notifyObservers() {

	    notifyObservers(null);
	}

	public void
	notifyObservers(Object info[]) {

	    YoixBodyGraphObserver  arrLocal[];
	    Vector                 queue;
	    int                    len;

	    synchronized(this) {
		if (obs == null)
		    return;

		if (info != null)
		    queueInfo(info);

		if (notifications == null || (len = notifications.size()) == 0)
		    return;

		compressNotifications();

		queue = new Vector();
		for(int i=0; i<len; i++) {
		    info = (Object[])(notifications.elementAt(i));
		    if (info[0] != null) {
			queue.addElement(info);
		    }
		}
		notifications.setSize(0);

		if (queue.size() == 0)
		    return;

		arrLocal = new YoixBodyGraphObserver[obs.size()];
		obs.copyInto(arrLocal);
	    }

	    for (int i = arrLocal.length-1; i>=0; i--)
		arrLocal[i].update(YoixGraphElement.this, (Vector)(queue.clone()));
	}

	private void compressNotifications() {

	    // we are synchronized when called

	    Object[]  ref, info;
	    int       len, i, k;

	    if (notifications == null || notifications.size() < 2)
		return;

	    len = notifications.size();

	    for(k=len-1; k > 0; k--) {
		info = (Object[])(notifications.elementAt(k));

		if (info[0] == null)
		    continue;

		switch(((Integer)(info[1])).intValue()) {
		case 1: // CREATED
		    for (i=k-1; i>=0; i--) {
			ref = (Object[])(notifications.elementAt(i));
			if (ref[0] == info[0]) {
			    if (ref[1] == DELETED) {
				ref[0] = null;
				info[0] = null;
				break;
			    }
			}
		    }
		    break;
		case 2: // DELETED
		    for (i=k-1; i>=0; i--) {
			ref = (Object[])(notifications.elementAt(i));
			if (ref[0] == info[0]) {
			    ref[0] = null;
			    if (ref[1] == CREATED) {
				info[0] = null;
				break;
			    }
			}
		    }
		    break;
		case 3: // MODIFIED
		    for (i=0; i<k; i++) {
			ref = (Object[])(notifications.elementAt(i));
			if (ref[0] == info[0] || (info[1] == MODIFIED && info[2] != MOD_LOCAL)) {
			    if (ref[1] == info[1]) {
				if (ref[2] == info[2] && ref[3] == info[3]) {
				    ref[0] = null;
				}
			    }
			}
		    }
		    break;
		case 4: // RELINKED
		    for (i=k-1; i>=0; i--) {
			ref = (Object[])(notifications.elementAt(i));
			if (ref[0] == info[0]) {
			    if (ref[1] == info[1] && ref[2] == info[2]) {
				if (ref[3] == info[4]) {
				    info[0] = null;
				    ref[0] = null;
				    break;
				} else if (ref[4] == info[3]) {
				    ref[0] = null;
				    info[3] = ref[3];
				} else {
				    // cannot happen
				    VM.abort(INTERNALERROR);
				}
			    }
			}
		    }
		    break;
		case 5: // RENAMED
		case 6: // RESHAPED (not used yet, but same behavior)
		    for (i=k-1; i>=0; i--) {
			ref = (Object[])(notifications.elementAt(i));
			if (ref[0] == info[0]) {
			    if (ref[1] == info[1]) {
				if (ref[2].equals(info[3])) {
				    info[0] = null;
				    ref[0] = null;
				    break;
				} else if (ref[3].equals(info[2])) {
				    ref[0] = null;
				    info[2] = ref[2];
				} else {
				    // cannot happen
				    VM.abort(INTERNALERROR);
				}
			    }
			}
		    }
		    break;
		}
	    }
	}
    }


    ///////////////////////////////////
    //
    // YoixGraphForceLayout class
    //
    // The layout method presented here is derived in large part from
    // methods and code developed by Emden Gansner working at AT&T Labs.
    // For more of Emden's extensive contributions to graph theory and
    // practice, visit:
    //   http://www.research.att.com/sw/tools/graphviz/
    //
    ///////////////////////////////////

    class ForceLayout extends YoixGraphBase

        implements Cloneable

    {

	double   ox = 0;
	double   oy = 0;
	double   dx = 0.0;
	double   dy = 0.0;

	int      shape = 0;

	boolean  port = false;

	ForceLayout() {
	}

	public final Object clone() {

	    ForceLayout obj = null;

	    obj = (ForceLayout)(super.clone());

	    return(obj);
	}

	public String toString() {
	    return(x+","+y);
	}

	void initElem(YoixGraphElement elem) {

	    if(elem.isType(GRAPH_NODE)) {

		double angle = PItimes2 * obs.rng.nextDouble();
		// for debugging: angle = PItimes2 * 0.5;

		YoixObject  yobj = elem.getAttribute("portal");

		if (yobj != null && yobj.isInteger() && yobj.booleanValue()) {
		    port = true;
		    x = obs.dvalues[2] * Math.cos(angle);
		    y = obs.dvalues[3] * Math.sin(angle);
		} else {
		    double  rad =  0.9 * obs.rng.nextDouble();
		    // for debugging: rad =  0.9 * 0.5;
		    x = rad * obs.dvalues[2] * Math.cos(angle);
		    y = rad * obs.dvalues[3] * Math.sin(angle);
		}
		ox = x;
		oy = y;
	    }
	}

	void layout() {

	    double            k;
	    Enumeration       enm;
	    YoixGraphElement  el;
	    Vector            vector;
	    int               sz;


	    vector = new Vector();
	    for (enm = grf.elements.elements(); enm.hasMoreElements(); ) {
		el = (YoixGraphElement)(enm.nextElement());
		if (el.ofType(GRAPH_NODE)) {
		    vector.addElement(el);
		}
	    }

	    if ((sz = vector.size()) > 0) {

		k = Math.sqrt((4.0*obs.dvalues[2]*obs.dvalues[3])/((double)sz));

		for (int i=0; i<obs.ivalues[0]; i++) {
		    // allow interruption
		    if(Thread.currentThread().isInterrupted())
			break;
		    adjust(vector, sz, k, temperature(obs.ivalues[0], i));
		}
	    }
	}

	private void adjust(Vector nds, int n_nodes,  double K, double temp) {

	    double            temp2 = temp*temp;
	    double            K2 = K*K*0.2;
	    YoixGraphElement  elem, telem, helem;
	    ForceLayout       data, tdata, hdata;
	    double            xdelta;
	    double            ydelta;
	    double            dist;
	    double            dist2;
	    double            rep;
	    double            repx;
	    double            repy;
	    int               i, j;
	    Enumeration       enm;

	    // initialize displacement
	    for (i=0; i<n_nodes; i++) {
		elem = (YoixGraphElement)nds.elementAt(i);

		data = null;
		synchronized(elem.root) {
		    if (grf.root == elem.root)
			data = (ForceLayout)(elem.getGraphData(obs, grf));
		}

		if (data != null) {

		//System.err.println("data0="+data);

		    if (data.port) {
			data.dx = 0.0;
			data.dy = 0.0;
		    } else {
			data.dx = -data.x;
			data.dy = -data.y;
		    }
		}
	    }

	    // compute forces on nodes
	    for (i=0; i<n_nodes; i++) {
		telem = (YoixGraphElement)nds.elementAt(i);

		tdata = null;
		synchronized(telem.root) {
		    if (grf.root == telem.root)
			tdata = (ForceLayout)(telem.getGraphData(obs, grf));
		}

		if (tdata != null) {

		    // repulsive forces
		    for (j=i+1; j<n_nodes; j++) {
			helem = (YoixGraphElement)nds.elementAt(j);

			hdata = null;
			synchronized(helem.root) {
			    if (grf.root == helem.root)
				hdata = (ForceLayout)(helem.getGraphData(obs, grf));
			}

			if (hdata != null) {

			    xdelta = tdata.x - hdata.x;
			    ydelta = tdata.y - hdata.y;

			    if ((xdelta == 0.0) && (ydelta == 0.0)) {
				xdelta = 1.0;
				ydelta = 1.0;
			    }

			    dist2 = xdelta*xdelta + ydelta*ydelta;

			    rep = K2/dist2;
			    if (tdata.port && hdata.port)
				rep = 10.0*rep;

			    repx = 3.0 * rep * xdelta;
			    repy = rep * ydelta;

			    tdata.dx = tdata.dx + repx;
			    tdata.dy = tdata.dy + repy;
			    hdata.dx = hdata.dx - repx;
			    hdata.dy = hdata.dy - repy;
			}
		    }

		    // attractive forces
		    for(enm = telem.outbound.elements(); enm.hasMoreElements();) {
			helem = ((YoixGraphElement)(enm.nextElement())).head;

			hdata = null;
			synchronized(helem.root) {
			    if (grf.root == helem.root)
				hdata = (ForceLayout)(helem.getGraphData(obs, grf));
			}

			if (hdata != null) {
			    xdelta = tdata.x - hdata.x;
			    ydelta = tdata.y - hdata.y;
			    dist = Math.sqrt(xdelta*xdelta + ydelta*ydelta);
			    rep = 3.0*dist/K;
			    repx = rep * xdelta;
			    repy = rep * ydelta;

			    tdata.dx = tdata.dx - repx;
			    tdata.dy = tdata.dy - repy;
			    hdata.dx = hdata.dx + repx;
			    hdata.dy = hdata.dy + repy;
			}
		    }
		}
	    }

	    // reposition nodes
	    for (i = 0; i < n_nodes; i++) {

		elem = (YoixGraphElement)nds.elementAt(i);

		data = null;
		synchronized(elem.root) {
		    if (grf.root == elem.root)
			data = (ForceLayout)(elem.getGraphData(obs, grf));
		}

		if (data != null)
		    update(data,temp,temp2);
	    }
	}

	private void update(ForceLayout data, double temp, double temp2)
	{
	    double dx = data.dx;
	    double dy = data.dy;
	    double x = 0.0;
	    double y = 0.0;
	    double len2 = dx*dx + dy*dy;
	    double fact, d;

	    if (len2 < temp2) {
		x = data.x + dx;
		y = data.y + dy;
	    } else {
		fact = temp / (Math.sqrt(len2));
		x = data.x + dx*fact;
		y = data.y + dy*fact;
	    }

	    d = Math.sqrt((x*x)/obs.dvalues[4] + (y*y)/obs.dvalues[5]);

	    if (data.port) {
		data.x = x/d;
		data.y = y/d;
	    } else if (d >= 1.0) {
		data.x = 0.95*x/d;
		data.y = 0.95*y/d;
	    } else {
		data.x = x;
		data.y = y;
	    }
	}

	private double
	temperature(double niters, double iter) {

	    return((obs.dvalues[6] * (niters - iter)) / niters);
	}
    }


    class RunLayout

	implements Runnable

    {

	private YoixGraphBase  gdata;
	private int            priority;
	private Thread         thread;
	private YoixObject     args[];

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	RunLayout(YoixGraphBase gdata) {

	    this(gdata, Thread.NORM_PRIORITY);
	}


	RunLayout(YoixGraphBase gdata, int priority) {

	    this.gdata = gdata;
	    this.priority = priority;
	}

	///////////////////////////////////
	//
	// Runnable Methods
	//
	///////////////////////////////////

	public final void
	run() {

	    try {
		gdata.layout();
	    }
	    finally {
		synchronized(gdata.obs) {
		    // YoixBodyThread cleanup
		    gdata.obs.thread.kill();
		    gdata.obs.thread = null;
		}

		gdata.obs.layoutCallback(gdata.grf, Thread.currentThread().isInterrupted(), args);

		try {
		    synchronized(this) {
			notifyAll();
		    }
		}
		catch(Exception e) {}
	    }
	}

	///////////////////////////////////
	//
	// RunLayout Methods
	//
	///////////////////////////////////

	protected final void
	finalize() {

	    try {
		super.finalize();
	    }
	    catch(Throwable t) {}
	}

	///////////////////////////////////
	//
	// Private Methods
	//
	///////////////////////////////////

	YoixBodyThread
	prepThread(YoixBodyGraphObserver obs) {

	    YoixBodyThread  bthrd = null;
	    YoixObject      data;

	    data = VM.getTypeTemplate(T_THREAD);
	    data.putInt(N_PRIORITY, priority);
	    bthrd = new YoixBodyThread(data, this);
	    thread = (Thread)(bthrd.getManagedObject());
	    if (thread != null) {
		obs.thread = bthrd;
		data.putInt(N_ALIVE, true);
	    } else bthrd = null;

	    return(bthrd);
	}


	synchronized void
	runThread(YoixBodyThread bthrd, boolean waitForIt, YoixObject[] args) {

	    this.args = args;
	    bthrd.setField(N_ALIVE, YoixObject.newInt(true)); // starts thread
	    if(waitForIt) {
		try {
		    wait();
		}
		catch(InterruptedException e) {}
	    }
	}


	synchronized void
	waitForThread() {

	    try {
		wait();
	    }
	    catch(InterruptedException e) {}
	}
    }
}

