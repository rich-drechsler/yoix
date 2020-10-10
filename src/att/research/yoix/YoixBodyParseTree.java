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

final
class YoixBodyParseTree extends YoixPointerActive

{

    private SimpleNode  tree;
    private HashMap     nodetable;
    private int         parser;

    //
    // Special stuff for tree walking. We were very concerned about
    // efficiency, particularly for big trees, so much of this is
    // magic.
    //

    private SimpleNode  base;
    private SimpleNode  last;
    private ArrayList   list;
    private int         mode;
    private int         offset;

    //
    // Flag tells us we're pretty much finished with initialization.
    //

    private boolean  initialized = false;

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(15);

    static {
	activefields.put(N_CHILD, new Integer(V_CHILD));
	activefields.put(N_DEPTH, new Integer(V_DEPTH));
	activefields.put(N_LENGTH, new Integer(V_LENGTH));
	activefields.put(N_MATCH, new Integer(V_MATCH));
	activefields.put(N_PARENT, new Integer(V_PARENT));
	activefields.put(N_PARSE, new Integer(V_PARSE));
	activefields.put(N_PARSER, new Integer(V_PARSER));
	activefields.put(N_POSITION, new Integer(V_POSITION));
	activefields.put(N_TREE, new Integer(V_TREE));
	activefields.put(N_TYPE, new Integer(V_TYPE));
	activefields.put(N_VALUE, new Integer(V_VALUE));
	activefields.put(N_WALK, new Integer(V_WALK));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyParseTree(YoixObject data) {

	super(data);
	buildTree();
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(PARSETREE);
    }

    ///////////////////////////////////
    //
    // YoixBodyParseTree Methods
    //
    ///////////////////////////////////

    final boolean
    ancestorOf(YoixBodyParseTree descendant) {

	return(descendant != null && ancestorOf(descendant.tree));
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_CHILD:
		if (tree != null)
		    obj = builtinChild(name, argv);
		else obj = YoixObject.newParseTree();
		break;

	    case V_MATCH:
		obj = builtinMatch(name, argv);
		break;

	    case V_WALK:
		if (tree != null)
		    obj = builtinWalk(name, argv);
		else obj = YoixObject.newParseTree();
		break;

	    default:
		obj = null;	// tells caller builtin isn't in this class
		break;
	}
	return(obj);
    }


    protected final void
    finalize() {

	clearTree();
	super.finalize();
    }


    protected final synchronized YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_DEPTH:
		obj = YoixObject.newInt(findDepth(tree));
		break;

	    case V_LENGTH:
		obj = YoixObject.newInt(tree != null ? tree.length() : 0);
		break;

	    case V_PARENT:
		obj = newParseTree(findParent(tree));
		break;

	    case V_POSITION:
		obj = YoixObject.newInt(findPosition(tree));
		break;

	    case V_TREE:
		if (tree != null)
		    obj = YoixObject.newString(tree.dump(parser));
		else obj = YoixObject.newString();
		break;

	    case V_TYPE:
		obj = YoixObject.newInt(tree != null ? tree.type() : -1);
		break;

	    case V_VALUE:
		obj = getValue(obj);
		break;
	}
	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(tree);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_PARSE:
		    setParse(obj);
		    break;

		case V_PARSER:
		    setParser(obj);
		    break;
	    }
	}
	return(obj);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private synchronized boolean
    ancestorOf(SimpleNode descendant) {

	boolean  result = false;

	if (tree != null && descendant != null && tree != descendant) {
	    while ((descendant = findParent(descendant)) != null) {
		if (descendant == tree) {
		    result = true;
		    break;
		}
	    }
	}
	return(result);
    }


    private SimpleNode
    buildNodeList(SimpleNode parent, SimpleNode ref, boolean all, ArrayList list) {

	SimpleNode  crnt;
	int         i;

	for (i = 0; i < parent.length(); i++) {
	    if (parent.value[i] instanceof SimpleNode) {
		crnt = (SimpleNode)(parent.value[i]);
		if (ref == null) {
		    list.add(crnt);
		    if (all)
			ref = buildNodeList(crnt, ref, all, list);
		} else if (crnt == ref) {
		    list.add(crnt);
		    ref = null;
		    if (all)
			ref = buildNodeList(crnt, ref, all, list);
		} else if (all)
		    ref = buildNodeList(crnt, ref, all, list);
	    } else break;
	}
	return(ref);
    }


    private HashMap
    buildNodeTable(SimpleNode parent, Integer depth, HashMap map) {

	Object  child;
	int     length;
	int     n;

	if (parent != null) {
	    length = parent.value.length;
	    for (n = 0; n < length; n++) {
		child = parent.value[n];
		if (child instanceof SimpleNode) {
		    map.put(child, new Object[] {parent, depth, new Integer(n)});
		    if (!(child instanceof YoixObject)) {
			buildNodeTable(
			    (SimpleNode)child,
			    new Integer(depth.intValue() + 1),
			    map
			);
		    }
		} else break;
	    }
	}
	return(map);
    }


    private void
    buildTree() {

	initialized = false;
	clearTree();
	setField(N_PARSER);
	setField(N_PARSE);
	initialized = true;
    }


    private synchronized YoixObject
    builtinChild(String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg.length == 1) {
	    if (arg[0].isInteger()) {
		if (tree.length() > 0 && tree.value[0] instanceof SimpleNode) 
		    obj = newParseTree(tree.getChild(arg[0].intValue()));
		else obj = YoixObject.newNull();
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(obj);
    }


    private synchronized YoixObject
    builtinMatch(String name, YoixObject arg[]) {

	boolean  result = false;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if (arg.length == 1 || arg[1].isInteger()) {
		    if (tree != null) {
			switch (parser) {
			    case PARSER_PATTERN:
			    case PARSER_PATTERN_AND:
			    case PARSER_PATTERN_OR:
			    case PARSER_PATTERN_XOR:
				result = PatternInterpreter.match(
				    tree,
				    arg[0].stringValue(),
				    arg.length > 1 ? arg[1].intValue() : 0
				);
				break;
			}
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newInt(result));
    }


    private synchronized YoixObject
    builtinWalk(String name, YoixObject arg[]) {

	YoixBodyParseTree  body;
	YoixObject         obj = null;
	SimpleNode         node = null;
	int                types[] = null;
	int                parser;
	int                mode;
	int                offset;
	int                i;
	int                n;

	mode = ROOT_TREE;
	parser = data.getInt(N_PARSER, PARSER_YOIX);

	if (arg.length >= 1 && arg.length <= 3) {
	    if (arg[0].isParseTree() && arg[0].notNull()) {
		if (arg.length > 1) {
		    if (arg[1].isNull()) {
			types = null;
		    } else if (arg[1].isInteger()) {
			n = arg[1].intValue();
			if (YoixMisc.tokenImage(n, parser) != null)
			    types = new int[] {n};
			else VM.badArgument(name, 1);
		    } else if (arg[1].isString()) {
			if ((n = YoixMisc.tokenValue(arg[1].stringValue(), parser)) >= 0)
			    types = new int[] {n};
			else VM.badArgument(name, 1);
		    } else if (arg[1].isArray()) {
			types = new int[arg[1].sizeof()];
			offset = arg[1].offset();
			for (i = 0; i < types.length; i++) {
			    if ((obj = arg[1].getObject(i + offset)) != null) {
				if (obj.isInteger()) {
				    n = obj.intValue();
				    if (YoixMisc.tokenImage(n, parser) != null)
					types[i] = n;
				    else VM.badArgument(name, 1);
				} else if (obj.isString()) {
				    if ((n = YoixMisc.tokenValue(obj.stringValue(), parser)) >= 0)
					types[i] = n;
				    else VM.badArgument(name, 1);
				} else VM.badArgument(name, 1);
			    }
			}
		    } else VM.badArgument(name, 1);
		    if (arg.length > 2) {
			if (arg[2].isNull())
			    mode = ROOT_TREE;
			else if (arg[2].isInteger())
			    mode = arg[2].intValue();
			else VM.badArgument(name, 2);
		    }
		}
		body = (YoixBodyParseTree)(arg[0].body());
		node = body.getNodeFromList(tree, mode, types);
		if (node != null) {
		    body.tree = node;
		    body.nodetable = nodetable;
		    body.parser = parser;
		    body.data.putInt(N_ADDTAGS, data.getInt(N_ADDTAGS, 0));
		    body.data.putInt(N_PARSER, parser);
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(YoixObject.newInt(node != null));
    }


    private void
    clearTree() {

	tree = null;
	nodetable = null;
	list = null;
	base = null;
	last = null;
	mode = -1;
	offset = -1;
    }


    private int
    findDepth(SimpleNode node) {

	HashMap  table;
	Object   info[];
	int      depth = 0;

	if (node != null && (table = nodetable) != null) {
	    if ((info = (Object[])table.get(node)) != null)
		depth = ((Integer)info[1]).intValue();
	}
	return(depth);
    }


    private SimpleNode
    findParent(SimpleNode node) {

	SimpleNode  parent = null;
	HashMap     table;
	Object      info[];

	if (node != null && (table = nodetable) != null) {
	    if ((info = (Object[])table.get(node)) != null)
		parent = (SimpleNode)info[0];
	}
	return(parent);
    }


    private int
    findPosition(SimpleNode node) {

	HashMap table;
	Object  info[];
	int     position = 0;

	if (node != null && (table = nodetable) != null) {
	    if ((info = (Object[])table.get(node)) != null)
		position = ((Integer)info[2]).intValue();
	}
	return(position);
    }


    private SimpleNode
    getNodeFromList(SimpleNode base, int mode, int types[]) {

	SimpleNode  node;
	SimpleNode  ref;
	SimpleNode  tmp;
	ArrayList   list;
	int         len;
	int         n;

	//
	// Modified low level code in SimpleNode.java but probably haven't
	// tested this yet. The findParent() call is where things changed.
	//

	ref = (SimpleNode)(this.tree);
	if (ref == null || (this.base != base || this.last != ref || this.mode != mode)) {
	    this.base = base;
	    this.mode = mode;
	    node = base;
	    if (mode != DESCENDANTS && mode != CHILDREN) {
		while ((tmp = findParent(node)) != null)
		    node = tmp;
	    }
	    list = new ArrayList();
	    list.add(node);
	    buildNodeList(node, null, (mode == CHILDREN ? false :true), list);
	    this.offset = 0;
	    this.list = list;
	}

	ref = null;
	len = this.list.size();

	while (ref == null && this.offset < len) {
	    node = (SimpleNode)(this.list.get(this.offset++));
	    if (types != null && types.length > 0) {
		for (n = 0; n < types.length; n++) {
		    if (types[n] == node.type()) {
			ref = node;
			break;
		    }
		}
	    } else ref = node;
	}
	this.last = ref;
	this.tree = ref;
	return(ref);
    }


    private synchronized YoixObject
    getValue(YoixObject obj) {

	if (tree != null) {
	    switch (parser) {
		case PARSER_DOT:
		case PARSER_DTD:
		case PARSER_PATTERN:
		case PARSER_PATTERN_AND:
		case PARSER_PATTERN_OR:
		case PARSER_PATTERN_XOR:
		case PARSER_XML:
		    if (tree.value.length > 0) {
			if (!(tree.value[0] instanceof SimpleNode))
			    obj = YoixObject.newString(tree.value[0].toString());
		    }
		    break;

		default:	 // PARSER_YOIX
		    switch (tree.type()) {
			case NAME:
			    obj = YoixObject.newString(tree.stringValue());
			    break;

			case NUMBER:
			case POINTER:
			    obj = (YoixObject)tree.clone();
			    break;
		    }
		    break;
	    }
	}
	return(obj);
    }


    private synchronized YoixObject
    newParseTree(SimpleNode node) {

	YoixBodyParseTree  body;
	YoixObject         obj;

	if (node != null) {
	    obj = YoixObject.newParseTree(VM.getTypeTemplate(T_PARSETREE));
	    body = (YoixBodyParseTree)obj.body();
	    body.tree = node;
	    body.nodetable = nodetable;
	    body.parser = parser;
	    body.data.putInt(N_ADDTAGS, data.getInt(N_ADDTAGS, 0));
	    body.data.putInt(N_PARSER, parser);
	} else obj = YoixObject.newParseTree();

	return(obj);
    }


    private synchronized void
    setParse(YoixObject obj) {

	SimpleNode  result;
	boolean     addtags;
	String      parse;
	int         parser;

	if (obj.notNull()) {
	    if (obj.isString()) {
		clearTree();
		parse = obj.stringValue();
		addtags = data.getBoolean(N_ADDTAGS);
		parser = data.getInt(N_PARSER, PARSER_YOIX);
		data.put(N_ERRORDICT, YoixObject.newDictionary());
		result = Yoix.translateString(parse, addtags, parser);
		if (!(result instanceof YoixObject)) {
		    this.tree = result;
		    this.parser = parser;
		    this.nodetable = buildNodeTable(result, new Integer(1), new HashMap());
		} else data.put(N_ERRORDICT, (YoixObject)result);
	    } else VM.abort(TYPECHECK, N_PARSE);
	} else clearTree();
    }


    private synchronized void
    setParser(YoixObject obj) {

	int  value;

	if (obj != null) {
	    switch (value = obj.intValue()) {
		case PARSER_DOT:
		case PARSER_DTD:
		case PARSER_PATTERN:
		case PARSER_PATTERN_AND:
		case PARSER_PATTERN_OR:
		case PARSER_PATTERN_XOR:
		case PARSER_XML:
		case PARSER_YOIX:
		    break;

		default:
		    value = PARSER_YOIX;
		    break;
	    }
	    data.putInt(N_PARSER, value);
	    if (initialized) {
		data.put(N_PARSE, YoixObject.newString(), false);
		clearTree();
	    }
	}
    }
}

