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
class YoixBodyElement extends YoixPointerActive

    implements YoixConstantsGraph

{

    private YoixGraphElement  element;

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(20);

    static {
	activefields.put(N_ATTRIBUTE, new Integer(V_ATTRIBUTE));
	activefields.put(N_ATTRIBUTES, new Integer(V_ATTRIBUTES));
	activefields.put(N_BFS, new Integer(V_BFS));
	activefields.put(N_DFS, new Integer(V_DFS));
	activefields.put(N_EDGEDEFAULTS, new Integer(V_EDGEDEFAULTS));
	activefields.put(N_ELEMENT, new Integer(V_ELEMENT));
	activefields.put(N_FLAGS, new Integer(V_FLAGS));
	activefields.put(N_GRAPHDEFAULTS, new Integer(V_GRAPHDEFAULTS));
	activefields.put(N_HEAD, new Integer(V_HEAD));
	activefields.put(N_NAME, new Integer(V_NAME));
	activefields.put(N_NODEDEFAULTS, new Integer(V_NODEDEFAULTS));
	activefields.put(N_PARENT, new Integer(V_PARENT));
	activefields.put(N_ROOT, new Integer(V_ROOT));
	activefields.put(N_TAIL, new Integer(V_TAIL));
	activefields.put(N_TEXT, new Integer(V_TEXT));
	activefields.put(N_WALK, new Integer(V_WALK));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyElement(YoixObject data) {

	super(data);

	YoixObject  name;
	YoixObject  parent;

	setFixedSize();

	name = data.getObject(N_NAME, null);
	parent = data.getObject(N_PARENT, null);

	// if a parent is supplied, make sure it is for real
	if (parent != null && parent.notNull() && (!parent.isGraph() || !((YoixBodyElement)parent.body()).hasElement()))
	    VM.abort(TYPECHECK, N_PARENT);

	if (name != null && name.notNull() && name.isString()) {
	    if (isGraph() || parent != null && parent.notNull() && parent.isGraph()) {
		buildElement();
	    }
	}
    }


    YoixBodyElement(YoixObject data, YoixGraphElement elem) {

	super(data);

	setFixedSize();
	element = elem;
	elem.setWrapper(this);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(ELEMENT);
    }

    ///////////////////////////////////
    //
    // YoixBodyElement Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	if (element != null) {
	    switch (activeField(name, activefields)) {
		case V_ATTRIBUTE:
		    obj = builtinAttribute(name, argv);
		    break;

		case V_BFS:
		    obj = executeWalks(name, BFS, argv);
		    break;

		case V_DFS:
		    obj = executeWalks(name, DFS, argv);
		    break;

		case V_ELEMENT:
		    obj = builtinElement(name, argv);
		    break;

		case V_TEXT:
		    obj = builtinText(name, argv);
		    break;

		case V_WALK:
		    obj = executeWalks(name, WALK, argv);
		    break;

		default:
		    obj = null;
		    break;
	    }
	} else obj = null;		// not quite!!

	return(obj);
    }


    protected final void
    finalize() {

	element.setWrapper(null);
	element = null;
	super.finalize();
    }


    final YoixGraphElement
    getElement() {

	return(element);
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	if (element != null) {
	    switch (activeField(name, activefields)) {
		case V_ATTRIBUTES:
		    obj = getLocalAttributes(0, obj);
		    break;

		case V_EDGEDEFAULTS:
		    obj = getLocalAttributes(EDGEDFLT, obj);
		    break;

		case V_FLAGS:
		    obj = YoixObject.newInt(element.getFlags());
		    break;

		case V_NAME:
		    obj = YoixObject.newString(element.getName());
		    break;

		case V_GRAPHDEFAULTS:
		    obj = getLocalAttributes(GRAPHDFLT, obj);
		    break;

		case V_HEAD:
		    if (element.getHead() != null)
			obj = YoixObject.newElement(element.getHead().getWrapper());
		    break;

		case V_NODEDEFAULTS:
		    obj = getLocalAttributes(NODEDFLT, obj);
		    break;

		case V_PARENT:
		    if (element.getParent() != null)
			obj = YoixObject.newElement(element.getParent().getWrapper());
		    break;

		case V_ROOT:
		    obj = YoixObject.newElement(element.getRoot().getWrapper());
		    break;

		case V_TAIL:
		    if (element.getTail() != null)
			obj = YoixObject.newElement(element.getTail().getWrapper());
		    break;

		default:
		    break;
	    }
	}

	return(obj);
    }


    final YoixObject
    getLocalAttributes(int dflt, YoixObject dict) {

	Enumeration  enm;
	Hashtable    attrs;
	String       key;

	if (element != null) {
	    attrs = element.getLocalAttributes(dflt);
	    dict = YoixObject.newDictionary(attrs.size());
	    if (attrs.size() > 0) {
		enm = attrs.keys();
		while (enm.hasMoreElements()) {
		    key = (String)enm.nextElement();
		    dict.put(key, (YoixObject)attrs.get(key));
		}
	    }
	}

	return(dict);
    }


    protected final Object
    getManagedObject() {

	return(element);
    }


    final boolean
    hasElement() {

	return(element != null);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	YoixObject  info;
	int         field;

	if (obj != null) {
	    field = activeField(name, activefields);
	    if (element == null) {
		if (field == V_NAME) {
		    if (obj.notNull() && obj.isString()) {
			data.put(N_NAME, obj);
			if (isGraph() || (info = data.getObject(N_PARENT, null)) != null && info.notNull()) {
			    if (!isEdge() || ((info = data.getObject(N_HEAD, null)) != null && info.notNull() && (info = data.getObject(N_TAIL, null)) != null && info.notNull())) {
				buildElement();
			    }
			}
		    } else VM.abort(TYPECHECK, N_NAME);
		} else if (field == V_PARENT) {
		    if (obj.notNull() && obj.isGraph()) {
			data.put(N_PARENT, obj);
			if ((info = data.getObject(N_NAME, null)) != null && info.notNull()) {
			    if (!isEdge() || ((info = data.getObject(N_HEAD, null)) != null && info.notNull() && (info = data.getObject(N_TAIL, null)) != null && info.notNull())) {
				buildElement();
			    }
			}
		    } else VM.abort(TYPECHECK, N_PARENT);
		} else if (field == V_HEAD && (info = data.getObject(N_TAIL, null)) != null && info.notNull() && (info = data.getObject(N_NAME, null)) != null && info.notNull() && (info = data.getObject(N_PARENT, null)) != null && info.notNull()) {
		    buildElement();
		} else if (field == V_TAIL && (info = data.getObject(N_HEAD, null)) != null && info.notNull() && (info = data.getObject(N_NAME, null)) != null && info.notNull() && (info = data.getObject(N_PARENT, null)) != null && info.notNull()) {
		    buildElement();
		}
	    } else if (element != null) {
		switch (field) {
		    case V_ATTRIBUTES:
			if (obj.isNull() || obj.isDictionary())
			    setLocalAttributes(0, obj);
			else VM.abort(TYPECHECK, N_ATTRIBUTES);
			break;

		    case V_EDGEDEFAULTS:
			if (obj.isNull() || obj.isDictionary())
			    setLocalAttributes(EDGEDFLT, obj);
			else VM.abort(TYPECHECK, N_EDGEDEFAULTS);
			break;

		    case V_GRAPHDEFAULTS:
			if (obj.isNull() || obj.isDictionary())
			    setLocalAttributes(GRAPHDFLT, obj);
			else VM.abort(TYPECHECK, N_GRAPHDEFAULTS);
			break;

		    case V_HEAD:
			if (obj.notNull() && obj.isNode())
			    element.changeEdge((YoixGraphElement)obj.getManagedObject(), true);
			else VM.abort(TYPECHECK, N_HEAD);
			break;

		    case V_NAME:
			if (obj.notNull() && obj.isString()) {
			    element.changeName(obj.stringValue());
			    data.put(N_NAME, obj);
			} else VM.abort(TYPECHECK, N_NAME);
			break;

		    case V_NODEDEFAULTS:
			if (obj.isNull() || obj.isDictionary())
			    setLocalAttributes(NODEDFLT, obj);
			else VM.abort(TYPECHECK, N_NODEDEFAULTS);
			break;

		    case V_PARENT:
			if (obj.isNull() || obj.isGraph())
			    element.setParent((YoixGraphElement)obj.getManagedObject());
			else VM.abort(TYPECHECK, N_PARENT);
			break;

		    case V_TAIL:
			if (obj.notNull() && obj.isNode())
			    element.changeEdge((YoixGraphElement)obj.getManagedObject(), false);
			else VM.abort(TYPECHECK, N_TAIL);
			break;

		    default:
			break;
		}
	    }
	}

	return(obj);
    }


    final boolean
    isEdge() {

	return(getMinor() == EDGE);
    }


    final boolean
    isGraph() {

	return(getMinor() == GRAPH);
    }


    final boolean
    isNode() {

	return(getMinor() == NODE);
    }


    static int
    parseGraphTypes(String str) {

	char  chs[];
	int   types = 0;
	int   i;

	if (str.length() > 0) {
	    chs = str.toCharArray();
	    i = 0;
	    while (i < chs.length) {
		if (Character.isLetter(chs[i])) {
		    if (chs[i] == 'g' || chs[i] == 'G')
			types |= GRAPH_GRAPH;
		    else if (chs[i] == 'n' || chs[i] == 'N')
			types |= GRAPH_NODE;
		    else if (chs[i] == 'e' || chs[i] == 'E')
			types |= GRAPH_EDGE;
		    while (i < chs.length && Character.isLetter(chs[i]))
			i++;
		}
		i++;
	    }
	}

	if (types == 0)
	    types = GRAPH_NODE|GRAPH_EDGE|GRAPH_GRAPH;
	return(types);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildElement() {

	YoixGraphElement  prnt;
	YoixGraphElement  hd;
	YoixGraphElement  tl;
	YoixObject        item;
	YoixObject        parent;
	YoixObject        head;
	YoixObject        tail;
	String            name;
	int               minor;
	int               flags;

	minor = getMinor();
	flags = data.getInt(N_FLAGS, 0);
	name = data.getString(N_NAME, null);
	parent = data.getObject(N_PARENT, null);
	prnt = null;

	switch (minor) {
	    case GRAPH:
		if (parent == null || parent.isNull()) {
		    element = new YoixGraphElement(name, flags);
		    element.setWrapper(this);
		} else if (parent.isGraph()) {
		    prnt = (YoixGraphElement)parent.getManagedObject();
		    element = new YoixGraphElement(name, prnt, flags, YoixGraphElement.isClusterName(name));
		    element.setWrapper(this);
		} else VM.abort(TYPECHECK, N_PARENT);
		break;

	    case NODE:
		if (parent != null && parent.notNull() && parent.isGraph()) {
		    prnt = (YoixGraphElement)parent.getManagedObject();
		    element = new YoixGraphElement(name, prnt, flags);
		    element.setWrapper(this);
		} else VM.abort(TYPECHECK, N_PARENT);
		break;

	    case EDGE:
		if (parent != null && parent.notNull() && parent.isGraph()) {
		    head = data.getObject(N_HEAD, null);
		    if (head != null && head.notNull() && head.isNode()) {
			tail = data.getObject(N_TAIL, null);
			if (tail != null && tail.notNull() && tail.isNode()) {
			    prnt = (YoixGraphElement)parent.getManagedObject();
			    hd = (YoixGraphElement)head.getManagedObject();
			    tl = (YoixGraphElement)tail.getManagedObject();
			    element = new YoixGraphElement(name, prnt, tl, hd, null, flags);
			    element.setWrapper(this);
			}
		    }
		} else VM.abort(TYPECHECK, N_PARENT);
		break;

	    default:
		VM.abort(UNIMPLEMENTED);
		break;
	}

	if (element != null) {
	    if ((item = data.getObject(N_ATTRIBUTES, null)) != null)
		setField(N_ATTRIBUTES, item);
	    if (minor == GRAPH) {
		if ((item = data.getObject(N_EDGEDEFAULTS, null)) != null)
		    setField(N_EDGEDEFAULTS, item);
		if ((item = data.getObject(N_GRAPHDEFAULTS, null)) != null)
		    setField(N_GRAPHDEFAULTS, item);
		if ((item = data.getObject(N_NODEDEFAULTS, null)) != null)
		    setField(N_NODEDEFAULTS, item);
	    }
	} else VM.abort(INTERNALERROR);
    }


    private YoixObject
    builtinAttribute(String name, YoixObject arg[]) {

	Enumeration  enm;
	YoixObject   result = null;
	YoixObject   elem;
	Hashtable    attrs;
	String       key;
	int          ivalue;
	int          offset;
	int          scope;
	int          mode;
	int          dflt;
	int          len;
	int          i;

	mode = 0;
	scope = 0;
	dflt = 0;

	for (offset = 0; arg.length > offset && arg[offset].isInteger(); offset++) {
	    ivalue = arg[offset].intValue();
	    if ((ivalue&~(CREATE|REPLACE|DELETE|SCOPED|NODEDFLT|EDGEDFLT|GRAPHDFLT)) == 0) {
		switch (ivalue&(CREATE|REPLACE|DELETE)) {
		    case CREATE:
		    case REPLACE:
		    case DELETE:
			mode = ivalue&(CREATE|REPLACE|DELETE);
			break;

		    case 0:
			break;

		    default:
			VM.badArgument(name, offset);
			break;
		}

		switch (ivalue&(SCOPED)) {
		    case SCOPED:
			scope = ivalue&(SCOPED);
			break;

		    case 0:
			break;

		    default:
			VM.badArgument(name, offset);
			break;
		}

		switch (ivalue&(NODEDFLT|EDGEDFLT|GRAPHDFLT)) {
		    case EDGEDFLT:
		    case GRAPHDFLT:
		    case NODEDFLT:
			dflt = ivalue&(NODEDFLT|EDGEDFLT|GRAPHDFLT);
			break;

		    case 0:
			break;

		    default:
			VM.badArgument(name, offset);
			break;
		}
	    } else VM.badArgument(name, offset);
	}

	if (arg.length == offset) {
	    if (mode == DELETE) {
		if (scope != SCOPED)
		    element.clearLocalAttributes(dflt);
		else element.clearDefaultAttributes(dflt);
	    } else if (mode == 0) {
		if (scope != SCOPED)
		    attrs = element.getLocalAttributes(dflt);
		else attrs = element.getAttributes(dflt);
		result = YoixObject.newDictionary(attrs.size());
		if (attrs.size() > 0) {
		    enm = attrs.keys();
		    while (enm.hasMoreElements()) {
			key = (String)enm.nextElement();
			result.put(key, (YoixObject)attrs.get(key));
		    }
		}
	    } else VM.badArgumentValue(name, -1, new String[] {"flags/action"});
	} else if (arg.length == (offset+1)) {
	    if (arg[offset].notNull() && arg[offset].isString()) {
		if (mode == DELETE) {
		    if (scope != SCOPED)
			result = element.addLocalAttribute(dflt, arg[offset].stringValue(), null);
		    else result = element.addDefaultAttribute(dflt, arg[offset].stringValue(), null);
		} else if (mode == 0) {
		    // get value
		    if (scope != SCOPED)
			result = element.getLocalAttribute(dflt, arg[offset].stringValue());
		    else result = element.getAttribute(dflt, arg[offset].stringValue());
		} else VM.badArgumentValue(name, offset, new String[] {"flags/action"});
	    } else if (arg[offset].isArray()) {
		if (mode == DELETE) {
		    if ((len = arg[offset].length()) > 0) {
			result = YoixObject.newArray(len);
			for (i = 0; i < len; i++) {
			    elem = arg[offset].get(i, false);
			    if (elem.notNull() && elem.isString()) {
				if (scope != SCOPED)
				    elem = element.addLocalAttribute(dflt, elem.stringValue(), null);
				else elem = element.addDefaultAttribute(dflt, elem.stringValue(), null);
				result.put(i, elem == null ? YoixObject.newNull() : elem, false);
			    } else VM.badArgument(name, offset);
			}
		    }
		} else if (mode == 0) {
		    // get values
		    if ((len = arg[offset].length()) > 0) {
			result = YoixObject.newArray(len);
			for (i = 0; i < len; i++) {
			    elem = arg[offset].get(i, false);
			    if (elem.notNull() && elem.isString()) {
				if (scope != SCOPED)
				    elem = element.getLocalAttribute(dflt, elem.stringValue());
				else elem = element.getAttribute(dflt, elem.stringValue());
				result.put(i, elem == null ? YoixObject.newNull() : elem, false);
			    } else VM.badArgument(name, offset);
			}
		    }
		} else VM.badArgumentValue(name, offset, new String[] {"flags/action"});
	    } else if (arg[offset].isDictionary()) {
		if (mode == CREATE) {
		    if ((len = arg[offset].length()) > 0) {
			for (i = 0; i < len; i++) {
			    elem = arg[offset].get(i, false);
			    if (scope != SCOPED) {
				if (element.getLocalAttribute(dflt, arg[offset].name(i)) == null)
				    element.addLocalAttribute(dflt, arg[offset].name(i), elem);
				else VM.badArgumentValue(name, offset, name(i));
			    } else {
				if (element.getAttribute(dflt, arg[offset].name(i)) == null)
				    elem = element.addDefaultAttribute(dflt, arg[offset].name(i), elem);
				else VM.badArgumentValue(name, offset, name(i));
			    }
			}
		    }
		} else if (mode == REPLACE || mode == 0) {
		    if ((len = arg[offset].length()) > 0) {
			result = YoixObject.newArray(len);
			for (i = 0; i < len; i++) {
			    elem = arg[offset].get(i, false);
			    if (scope != SCOPED)
				elem = element.addLocalAttribute(dflt, arg[offset].name(i), elem);
			    else elem = element.addDefaultAttribute(dflt, arg[offset].name(i), elem);
			    result.put(i, elem == null ? YoixObject.newNull() : elem, false);
			}
		    }
		} else VM.badArgumentValue(name, offset, new String[] {"flags/action"});
	    } else VM.badArgument(name, offset);
	} else if (arg.length == (offset+2)) {
	    if (arg[offset].notNull() && arg[offset].isString()) {
		if (mode == REPLACE || mode == 0) {
		    if (scope != SCOPED)
			result = element.addLocalAttribute(dflt, arg[offset].stringValue(), arg[offset+1]);
		    else result = element.addDefaultAttribute(dflt, arg[offset].stringValue(), arg[offset+1]);
		} else if (mode == CREATE) {
		    if (scope != SCOPED) {
			if (element.getLocalAttribute(dflt, arg[offset].stringValue()) == null)
			    element.addLocalAttribute(dflt, arg[offset].stringValue(), arg[offset+1]);
			else VM.badArgumentValue(name, offset);
		    } else {
			if (element.getAttribute(dflt, arg[offset].stringValue()) == null)
			    element.addDefaultAttribute(dflt, arg[offset].stringValue(), arg[offset+1]);
			else VM.badArgumentValue(name, offset);
		    }
		} else VM.badArgumentValue(name, offset, new String[] {"flags/action"});
	    } else VM.badArgument(name, offset);
	} else VM.badCall(name);

	return(result == null ? YoixObject.newNull() : result);
    }


    private YoixObject
    builtinElement(String name, YoixObject arg[]) {

	YoixGraphElement  elem;
	YoixObject        result = null;

	if (arg.length == 1) {
	    if (arg[0].notNull() && arg[0].isString()) {
		if ((elem = element.findElement(arg[0].stringValue())) != null)
		    result = YoixObject.newElement(elem.getWrapper());
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(result == null ? YoixObject.newNull() : result);
    }


    private YoixObject
    builtinText(String name, YoixObject arg[]) {

	YoixObject  result = null;
	String      indent = null;
	int         format;
	int         mode;
	int         depth;

	if (arg.length <= 4) {
	    format = XML_TEXTUAL;
	    mode = -1;
	    depth = -1;
	    indent = null;
	    if (arg.length > 0) {
		if (arg[0].isInteger()) {
		    if (arg[0].intValue() == DOT_TEXTUAL)
			format = DOT_TEXTUAL;
		    else format = XML_TEXTUAL;
		    if (arg.length > 1) {
			if (arg[1].isInteger())
			    mode = arg[1].intValue();
			else VM.badArgument(name, 1);
			if (arg.length > 2) {
			    if (arg[2].isInteger())
				depth = arg[2].intValue();
			    else VM.badArgument(name, 2);
			    if (arg.length > 3) {
				if (arg[3].notNull() && arg[3].isString())
				    indent = arg[3].stringValue();
				else VM.badArgument(name, 3);
			    }
			}
		    }
		} else VM.badArgument(name, 0);
	    }
	    if (element != null)
		result = YoixObject.newString(element.toString(format, mode, depth, indent, null));
	} else VM.badCall(name);

	return(result == null ? YoixObject.newString() : result);
    }


    private YoixObject
    executeWalks(String name, int mode, YoixObject argv[]) {

	YoixObject  args[] = null;
	YoixObject  result = null;
	Vector      list;
	int         depth = -1;
	int         types = GRAPH_NODE|GRAPH_EDGE|GRAPH_GRAPH;
	int         sz;
	int         i;

	if (element != null) {
	    if (argv.length > 0) {
		if (argv.length == 1) {
		    if (isGraph() && argv[0].isInteger()) {
			depth = argv[0].intValue();
		    } else if (isGraph() && mode < 0 && argv[0].notNull() && argv[0].isString()) {
			types = parseGraphTypes(argv[0].stringValue());
		    } else if (argv[0].notNull() && argv[0].isFunction()) {
			args = argv;
		    } else VM.badArgument(name, 0);
		} else if (argv.length == 2) {
		    if (isGraph() && argv[0].isInteger()) {
			depth = argv[0].intValue();
			if (argv[1].notNull() && argv[1].isFunction()) {
			    args = new YoixObject[argv.length - 1];
			    System.arraycopy(argv,1,args,0,args.length);
			} else if (isGraph() && mode < 0 && argv[1].notNull() && argv[1].isString()) {
			    types = parseGraphTypes(argv[1].stringValue());
			} else VM.badArgument(name, 1);
		    } else if (isGraph() && mode < 0 && argv[0].notNull() && argv[0].isString()) {
			types = parseGraphTypes(argv[0].stringValue());
			if (argv[1].notNull() && argv[1].isFunction()) {
			    args = new YoixObject[argv.length - 1];
			    System.arraycopy(argv,1,args,0,args.length);
			} else if (isGraph() && argv[1].isInteger()) {
			    depth = argv[1].intValue();
			} else VM.badArgument(name, 1);
		    } else if (argv[0].notNull() && argv[0].isFunction()) {
			args = argv;
		    } else VM.badArgument(name, 0);
		} else {
		    if (isGraph() && argv[0].isInteger()) {
			depth = argv[0].intValue();
			if (argv[1].notNull() && argv[1].isFunction()) {
			    args = new YoixObject[argv.length - 1];
			    System.arraycopy(argv,1,args,0,args.length);
			} else if (isGraph() && mode < 0 && argv[1].notNull() && argv[1].isString()) {
			    types = parseGraphTypes(argv[1].stringValue());
			    if (argv[2].notNull() && argv[2].isFunction()) {
				args = new YoixObject[argv.length - 2];
				System.arraycopy(argv,2,args,0,args.length);
			    } else VM.badArgument(name, 2);
			} else VM.badArgument(name, 1);
		    } else if (isGraph() && mode < 0 && argv[0].notNull() && argv[0].isString()) {
			types = parseGraphTypes(argv[0].stringValue());
			if (argv[1].notNull() && argv[1].isFunction()) {
			    args = new YoixObject[argv.length - 1];
			    System.arraycopy(argv,1,args,0,args.length);
			} else if (isGraph() && argv[1].isInteger()) {
			    depth = argv[1].intValue();
			    if (argv[2].notNull() && argv[2].isFunction()) {
				args = new YoixObject[argv.length - 2];
				System.arraycopy(argv,2,args,0,args.length);
			    } else VM.badArgument(name, 2);
			} else VM.badArgument(name, 1);
		    } else if (argv[0].notNull() && argv[0].isFunction()) {
			args = argv;
		    } else VM.badArgument(name, 0);
		}
	    }
	    if (mode == 0) {
		list = element.bdfs(true, depth, args == null ? null : element, args, WALKER);
	    } else if (mode > 0) {
		list = element.bdfs(false, depth, args == null ? null : element, args, WALKER);
	    } else {
		list = element.traverse(true, types, depth, args == null ? null : element, args, WALKER);
	    }
	    result = YoixObject.newArray(sz = list.size());
	    for (i = 0; i < sz; i++) {
		result.put(i, YoixObject.newElement(((YoixGraphElement)list.elementAt(i)).getWrapper()), false);
	    }
	} else VM.badCall();

	return(result == null ? YoixObject.newArray(0) : result);
    }


    private void
    setLocalAttributes(int dflt, YoixObject dict) {

	int  i;

	if (element != null) {
	    if (dict == null || dict.isNull() || dict.length() == 0) {
		synchronized(element.getRoot()) {
		    element.clearLocalAttributes(dflt);
		}
	    } else {
		synchronized(element.getRoot()) {
		    element.clearLocalAttributes(dflt);
		    for (i = dict.length() - 1; i >= 0; i--)
			element.addLocalAttribute(dflt, dict.name(i), dict.get(i, false));
		}
	    }
	}
    }
}

