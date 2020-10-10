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
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

class YoixSwingJTree extends JTree

    implements YoixConstants,
	       YoixConstantsJTree,
               YoixConstantsSwing,
               TreeExpansionListener,
               TreeWillExpandListener

{

    private YoixBodyComponent  parent;
    private YoixObject         data;

    //
    // Icon and Tree node support.
    //

    private Icon  defaultOpenIcon = null;
    private Icon  defaultClosedIcon = null;
    private Icon  defaultLeafIcon = null;

    private Color defaultBorderColor = null;
    private Color defaultSelectionBackground = null;
    private Color defaultSelectionForeground = null;

    private static Icon  javaDefaultOpenIcon;
    private static Icon  javaDefaultClosedIcon;
    private static Icon  javaDefaultLeafIcon;

    private static Color javaDefaultBorderColor;
    private static Color javaDefaultSelectionBackground;
    private static Color javaDefaultSelectionForeground;

    static {
	DefaultTreeCellRenderer dtcr = new DefaultTreeCellRenderer();

	javaDefaultOpenIcon = dtcr.getDefaultOpenIcon();
	javaDefaultClosedIcon = dtcr.getDefaultClosedIcon();
	javaDefaultLeafIcon = dtcr.getDefaultLeafIcon();

	javaDefaultBorderColor = UIManager.getColor("Tree.selectionBorderColor");
	javaDefaultSelectionBackground = UIManager.getColor("Tree.selectionBackground");
	javaDefaultSelectionForeground = UIManager.getColor("Tree.selectionForeground");
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJTree(YoixObject data, YoixBodyComponent parent) {

	super();
	this.parent = parent;
	this.data = data;

	setModel(
	    new YoixTreeModel(
		new DefaultMutableTreeNode(
		    new YoixJTreeNode(
			YoixObject.newString("--empty--"),
			parent.getContext()
		    ),
		    true
		)
	    )
	);
	setCellRenderer(new YoixTreeCellRenderer());
    }

    ///////////////////////////////////
    //
    // TreeExpansionListener Methods
    //
    ///////////////////////////////////

    public final void
    treeCollapsed(TreeExpansionEvent event) {

	treeCollapsedOrExpanded(event, YOIX_NODE_COLLAPSED);
    }


    public final void
    treeExpanded(TreeExpansionEvent event) {

	treeCollapsedOrExpanded(event, YOIX_NODE_EXPANDED);
    }

    ///////////////////////////////////
    //
    // TreeWillExpandListener Methods
    //
    ///////////////////////////////////

    public final void
    treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {

	treeWillCollapseOrExpand(event, YOIX_NODE_COLLAPSING);
    }


    public final void
    treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {

	treeWillCollapseOrExpand(event, YOIX_NODE_EXPANDING);
    }

    ///////////////////////////////////
    //
    // YoixSwingJTree Methods
    //
    ///////////////////////////////////

    final synchronized YoixObject
    builtinAction(String name, YoixObject arg[]) {

	DefaultMutableTreeNode  treenode = null;
	DefaultMutableTreeNode  targetnode = null;
	DefaultMutableTreeNode  parentnode;
	DefaultTreeModel        treemodel = null;
	Enumeration             enm;
	YoixObject              crntnode = null;
	YoixObject              prevnode = null;
	YoixObject              prntnode;
	YoixObject              added;
	YoixObject              result = null;
	YoixObject              content;
	YoixObject              yobj;
	YoixObject              yobj2;
	YoixError               error_point = null;
	ArrayList               list;
	TreePath                paths[] = null;
	TreePath                treepath;
	boolean                 children = false;
	Vector                  vec;
	String                  fieldname = null;
	int                     toss = 0;
	int                     action = -1;
	int                     row = -1;
	int                     m;
	int                     n;
	int                     off;
	int                     idx1 = -1;
	int                     idx2 = -1;

	if (arg.length >= 1 && arg.length <= 4) {
	    if (arg[0].isInteger()) {
		action = arg[0].intValue();
		switch(action) {
		    case YOIX_COLLAPSE_ALL:
		    case YOIX_EDIT_CANCEL:
		    case YOIX_EDIT_STOP:
		    case YOIX_EXPAND_ALL:
		    case YOIX_GET_ROW_COUNT:
		    case YOIX_GET_SELECTED_NODES:
		    case YOIX_GET_SELECTED_COUNT:
		    case YOIX_SELECT_ALL:
		    case YOIX_SELECT_NONE:
			if (arg.length != 1)
			    VM.badCall(name);
			break;

		    case YOIX_UPDATE_TREE:
			treemodel = (DefaultTreeModel)getModel();
			if (arg.length == 2) {
			    if (arg[1].notNull() && arg[1].isJTreeNode()) {
				crntnode = arg[1];
				targetnode = findTaggedTreeNode(this, crntnode.getObject(N_TAG).stringValue());
				if (targetnode == null)
				    VM.badArgumentValue(name, 1);
			    } else VM.badArgument(name, 1);
			} else VM.badCall(name);
			break;

		    case YOIX_COLLAPSE_NODE:
		    case YOIX_DELETE_NODE:
		    case YOIX_DESELECT_NODE:
		    case YOIX_EDIT_START:
		    case YOIX_EXPAND_NODE:
		    case YOIX_GET_EXPANDED_NODES:
		    case YOIX_GET_PARENT:
		    case YOIX_GET_ROW_FOR_NODE:
		    case YOIX_GET_SIBLING_ABOVE:
		    case YOIX_GET_SIBLING_BELOW:
		    case YOIX_MAKE_NODE_VISIBLE:
		    case YOIX_NODE_HAS_BEEN_EXPANDED:
		    case YOIX_NODE_IS_EXPANDED:
		    case YOIX_NODE_IS_SELECTED:
		    case YOIX_NODE_IS_VISIBLE:
		    case YOIX_SCROLL_NODE:
		    case YOIX_SELECT_NODE:
		    case YOIX_SELECT_TOGGLE:
		    case YOIX_TAGGED_COPY:
		    case YOIX_UNTAGGED_COPY:
		    case YOIX_UPDATE_COPY:
			if (arg.length == 2) {
			    if (arg[1].notNull() && arg[1].isJTreeNode()) {
				treenode = findTaggedTreeNode(this, arg[1].getObject(N_TAG).stringValue());
				crntnode = arg[1];
				if (treenode == null)
				    VM.badArgumentValue(name, 1);
			    } else VM.badArgument(name, 1);
			} else VM.badCall(name);
			treemodel = (DefaultTreeModel)(this.getModel());
			break;

		    case YOIX_BREADTH_FIRST:
		    case YOIX_DEPTH_FIRST:
		    case YOIX_POSTORDER_TRAVERSAL:
		    case YOIX_PREORDER_TRAVERSAL:
			if (arg.length == 2 || arg.length == 3 || arg.length == 4) {
			    if (arg[1].notNull() && arg[1].isJTreeNode()) {
				if (arg.length < 3 || arg[2].isInteger()) {
				    if (arg.length < 4 || arg[3].isString()) {
					crntnode = arg[1];
					toss = (arg.length > 2) ? arg[2].intValue() : 0;
					fieldname = (arg.length > 3) ? arg[3].stringValue() : null;
					treenode = findTaggedTreeNode(this, arg[1].getObject(N_TAG).stringValue());
					if (treenode == null)
					    VM.badArgumentValue(name, 1);
				    } else VM.badArgument(name, 3);
				} else VM.badArgument(name, 2);
			    } else VM.badArgument(name, 1);
			} else VM.badCall(name);
			treemodel = (DefaultTreeModel)getModel();
			break;

		    case YOIX_GET_NODE_FOR_ROW:
		    case YOIX_SCROLL_ROW:
			if (arg.length == 2) {
			    if (arg[1].isInteger())
				row = arg[1].intValue();
			    else VM.badArgument(name, 1);
			} else VM.badCall(name);
			break;

		    case YOIX_ELEMENT_COUNT:
			if (arg.length == 1 || arg.length == 2) {
			    if (arg.length == 2) {
				if (!arg[1].isNull() && !arg[1].isJTreeNode())
				    VM.badArgument(name, 1);
			    }
			} else VM.badCall(name);
			break;

		    case YOIX_ADD_CHILD:
		    case YOIX_ADD_SIBLING_ABOVE:
		    case YOIX_ADD_SIBLING_BELOW:
		    case YOIX_NEW_FOR_OLD:
			if (arg.length == 3) {
			    if (arg[1].notNull()) {
				if (arg[1].isJTreeNode() || arg[1].isString()) {
				    crntnode = arg[1];
				} else VM.badArgument(name, 1);

				if (arg[2].notNull() && arg[2].isJTreeNode()) {
				    prevnode = arg[2];
				    targetnode = findTaggedTreeNode(this, prevnode.getObject(N_TAG).stringValue());
				    if (targetnode == null)
					VM.badArgumentValue(name, 2);
				} else VM.badArgument(name, 2);
			    } else VM.badArgument(name, 1);
			} else VM.badCall(name);
			treemodel = (DefaultTreeModel)getModel();
			break;

		    case YOIX_DESELECT_INTERVAL:
		    case YOIX_SELECT_INTERVAL:
			if (arg.length == 3) {
			    if (arg[1].notNull() && arg[1].isInteger()) {
				idx1 = arg[1].intValue();
				if (arg[2].notNull() && arg[2].isInteger()) {
				    idx2 = arg[2].intValue();
				    if (idx1 > idx2) {
					m = idx1;
					idx1 = idx2;
					idx2 = m;
				    }
				    if (idx1 < 0)
					idx1 = 0;
				    if (idx2 >= (m = getRowCount()))
					idx2 = m - 1;
				} else VM.badArgument(name, 2);
			    } else VM.badArgument(name, 1);
			} else VM.badCall(name);
			break;

		    case YOIX_DESELECT_NODES:
		    case YOIX_SELECT_NODES:
			if (arg.length > 1) {
			    treemodel = (DefaultTreeModel)getModel();
			    if (arg[1].notNull()) {
				if (arg[1].isArray()) {
				    if (arg.length > 2)
					VM.badCall(name);
				    yobj = arg[1];
				} else if (arg[1].isJTreeNode()) {
				    m = arg.length - 1;
				    yobj = YoixObject.newArray(m);
				    for (n = 1; n <= m; n++)
					yobj.put(n-1, arg[n], false);
				} else {
				    VM.badArgument(name, 1);
				    yobj = null; // for compiler
				}
				m = yobj.sizeof();
				paths = new TreePath[m];
				off = yobj.offset();
				for (n = 0; n < m; n++) {
				    yobj2 = yobj.get(off+n,false);
				    if (yobj2.notNull() && yobj2.isJTreeNode()) {
					treenode = findTaggedTreeNode(this, yobj2.getObject(N_TAG).stringValue());
					if (treenode == null)
					    VM.badArgumentValue(name, n);
					paths[n] = new TreePath(treemodel.getPathToRoot(treenode));
				    } else VM.badArgument(name, n);
				}
			    } else VM.badArgument(name, 1);
			} else VM.badCall(name);
			break;

		    default:
			VM.badArgumentValue(name, 0);
			break;
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	switch(action) {
	    case YOIX_ADD_CHILD:
		if ((added = data.getObject(N_COMPONENTS)) == null)
		    added = YoixObject.newDictionary(getElementCount(crntnode), -1, false);
		try {
		    error_point = VM.pushError();
		    VM.pushAccess(LRW_);
		    added.setGrowable(true);
		    treenode = buildNode(crntnode, null, added, parent.getContext());
		    added.setGrowable(false);
		    added.setAccessBody(LR__);
		    data.put(N_COMPONENTS, added);
		    VM.popAccess();
		    VM.popError();
		    crntnode = ((YoixJTreeNode)(treenode.getUserObject())).getSelf();
		    ((YoixJTreeNode)(targetnode.getUserObject())).addYoixChild(crntnode);
		    treemodel.insertNodeInto(treenode, targetnode, targetnode.getChildCount());
		    expandPath(new TreePath(treemodel.getPathToRoot(targetnode)));
		    result = crntnode;
		}
		catch(Error e) {
		    if (e != error_point) {
			VM.popError();
			throw(e);
		    } else VM.error(error_point);
		}
		catch(RuntimeException e) {
		    VM.popError();
		    throw(e);
		}
		break;

	    case YOIX_ADD_SIBLING_ABOVE:
	    case YOIX_ADD_SIBLING_BELOW:
		if ((parentnode = (DefaultMutableTreeNode)(targetnode.getParent())) != null) {
		    n = treemodel.getIndexOfChild(parentnode, targetnode)
			+ ((action == YOIX_ADD_SIBLING_BELOW) ? 1 : 0);
		    if ((added = data.getObject(N_COMPONENTS)) == null)
			added = YoixObject.newDictionary(getElementCount(crntnode), -1, false);
		    try {
			error_point = VM.pushError();
			VM.pushAccess(LRW_);
			added.setGrowable(true);
			treenode = buildNode(crntnode, null, added, parent.getContext());
			added.setGrowable(false);
			added.setAccessBody(LR__);
			data.put(N_COMPONENTS, added);
			VM.popAccess();
			VM.popError();
			crntnode = ((YoixJTreeNode)(treenode.getUserObject())).getSelf();
			((YoixJTreeNode)(parentnode.getUserObject())).addYoixChild(crntnode, n);
			treemodel.insertNodeInto(treenode, parentnode, n);
			expandPath(new TreePath(treemodel.getPathToRoot(parentnode)));
			result = crntnode;
		    }
		    catch(Error e) {
			if (e != error_point) {
			    VM.popError();
			    throw(e);
			} else VM.error(error_point);
		    }
		    catch(RuntimeException e) {
			VM.popError();
			throw(e);
		    }
		} else VM.badArgumentValue(name, 2);
		break;

	    case YOIX_BREADTH_FIRST:
	    case YOIX_DEPTH_FIRST:
	    case YOIX_POSTORDER_TRAVERSAL:
	    case YOIX_PREORDER_TRAVERSAL:
		switch (action) {
		case YOIX_BREADTH_FIRST:
		    enm = treenode.breadthFirstEnumeration();
		    break;

		case YOIX_PREORDER_TRAVERSAL:
		    enm = treenode.preorderEnumeration();
		    break;

		default:
		    enm = treenode.depthFirstEnumeration();
		    break;
		}
		list = new ArrayList();
		while (enm.hasMoreElements()) {
		    treenode = (DefaultMutableTreeNode)enm.nextElement();
		    if (toss != 0) {
			if (treenode.isRoot())
			    treenode = (toss&0x01) == 0 ? treenode : null;
			else if (treenode.isLeaf())
			    treenode = (toss&0x04) == 0 ? treenode : null;
			else if ((toss&0x02) != 0)
			    treenode = null;
		    }
		    if (treenode != null) {
			crntnode = ((YoixJTreeNode)treenode.getUserObject()).getSelf();
			if (fieldname != null) {
			    if ((content = crntnode.getObject(N_CONTENT)) != null) {
				if (content.defined(fieldname))
				    content.putInt(fieldname, list.size());
			    }
			}
			list.add(crntnode);
		    }
		}
		result = YoixMisc.copyIntoArray(list, true, list);
		break;

	    case YOIX_COLLAPSE_ALL:
		treemodel = (DefaultTreeModel)getModel();
		treenode = (DefaultMutableTreeNode)(treemodel.getRoot());
		enm = getExpandedDescendants(new TreePath(treemodel.getPathToRoot(treenode)));
		if (enm != null) {
		    vec = new Vector();
		    while (enm.hasMoreElements())
			vec.addElement(enm.nextElement());
		    enm = vec.elements();
		    while (enm.hasMoreElements())
			collapsePath((TreePath)(enm.nextElement()));
		}
		break;

	    case YOIX_COLLAPSE_NODE:
		collapsePath(new TreePath(treemodel.getPathToRoot(treenode)));
		break;

	    case YOIX_DELETE_NODE:
		if ((parentnode = (DefaultMutableTreeNode)(treenode.getParent())) == null) {
		    data.put(N_TOP, YoixObject.newNull());
		    parent.setField(N_TOP);
		} else {
		    ((YoixJTreeNode)(parentnode.getUserObject())).removeYoixChild(crntnode);
		    treemodel.removeNodeFromParent(treenode);
		    treenode = (DefaultMutableTreeNode)(treemodel.getRoot());
		    added = YoixObject.newDictionary(getElementCount(treenode), -1, false);
		    enm = treenode.breadthFirstEnumeration();
		    while (enm.hasMoreElements()) {
			treenode = ((DefaultMutableTreeNode)(enm.nextElement()));
			((YoixJTreeNode)(treenode.getUserObject())).addSelf(added);
		    }
		    try {
			error_point = VM.pushError();
			VM.pushAccess(LRW_);
			added.setGrowable(false);
			data.put(N_COMPONENTS, added);
			VM.popAccess();
			VM.popError();
		    }
		    catch(Error e) {
			if (e != error_point) {
			    VM.popError();
			    throw(e);
			} else VM.error(error_point);
		    }
		    catch(RuntimeException e) {
			VM.popError();
			throw(e);
		    }
		}
		break;

	    case YOIX_DESELECT_INTERVAL:
		removeSelectionInterval(idx1, idx2);
		break;

	    case YOIX_DESELECT_NODE:
		removeSelectionPath(new TreePath(treemodel.getPathToRoot(treenode)));
		break;

	    case YOIX_DESELECT_NODES:
		removeSelectionPaths(paths);
		break;

	    case YOIX_EDIT_CANCEL:
		cancelEditing();
		break;

	    case YOIX_EDIT_START:
		startEditingAtPath(new TreePath(treemodel.getPathToRoot(treenode)));
		break;

	    case YOIX_EDIT_STOP:
		stopEditing();
		break;

	    case YOIX_ELEMENT_COUNT:
		n = 0;
		if (arg.length == 2 && arg[1].notNull())
		    n = getElementCount(arg[1]);
		else n = getElementCount();
		result = YoixObject.newInt(n);
		break;

	    case YOIX_EXPAND_ALL:
		treemodel = (DefaultTreeModel)getModel();
		treenode = (DefaultMutableTreeNode)(treemodel.getRoot());
		enm = treenode.breadthFirstEnumeration();
		while (enm.hasMoreElements()) {
		    targetnode = (DefaultMutableTreeNode)(enm.nextElement());
		    expandPath(new TreePath(treemodel.getPathToRoot(targetnode)));
		}
		break;

	    case YOIX_EXPAND_NODE:
		expandPath(new TreePath(treemodel.getPathToRoot(treenode)));
		break;

	    case YOIX_GET_EXPANDED_NODES:
		enm = getExpandedDescendants(new TreePath(treemodel.getPathToRoot(treenode)));
		if (enm != null) {
		    result = YoixObject.newArray(0);
		    result.setGrowable(true);
		    n = 0;
		    while (enm.hasMoreElements()) {
			result.put(n++, ((YoixJTreeNode)(((DefaultMutableTreeNode)(((TreePath)(enm.nextElement())).getLastPathComponent())).getUserObject())).getSelf(), false);
		    }
		}
		break;

	    case YOIX_GET_NODE_FOR_ROW:
		if ((treepath = getPathForRow(row)) != null) {
		    treenode = (DefaultMutableTreeNode)(treepath.getLastPathComponent());
		    result = ((YoixJTreeNode)(treenode.getUserObject())).getSelf();
		}
		break;

	    case YOIX_GET_PARENT:
		if ((parentnode = (DefaultMutableTreeNode)(treenode.getParent())) != null) {
		    result = ((YoixJTreeNode)(parentnode.getUserObject())).getSelf();
		}
		break;

	    case YOIX_GET_ROW_COUNT:
		result = YoixObject.newInt(getRowCount());
		break;

	    case YOIX_GET_ROW_FOR_NODE:
		result = YoixObject.newInt(getRowForPath(new TreePath(treemodel.getPathToRoot(treenode))));
		break;

	    case YOIX_GET_SELECTED_NODES:
		paths = getSelectionPaths();
		if (paths != null) {
		    result = YoixObject.newArray(0);
		    result.setGrowable(true);
		    for (n = 0; n < paths.length; n++) {
			result.put(n, ((YoixJTreeNode)(((DefaultMutableTreeNode)(paths[n].getLastPathComponent())).getUserObject())).getSelf(), false);
		    }
		}
		break;

	    case YOIX_GET_SELECTED_COUNT:
		result = YoixObject.newInt(getSelectionCount());
		break;

	    case YOIX_GET_SIBLING_ABOVE:
		if ((parentnode = (DefaultMutableTreeNode)(treenode.getParent())) != null) {
		    if (treemodel.getChildCount(parentnode) > 1) {
			if ((n = treemodel.getIndexOfChild(parentnode, treenode)) > 0) {
			    result = ((YoixJTreeNode)(((DefaultMutableTreeNode)(treemodel.getChild(parentnode, n-1))).getUserObject())).getSelf();
			}
		    }
		}
		break;

	    case YOIX_GET_SIBLING_BELOW:
		if ((parentnode = (DefaultMutableTreeNode)(treenode.getParent())) != null) {
		    if ((m = treemodel.getChildCount(parentnode)) > 1) {
			if ((n = treemodel.getIndexOfChild(parentnode, treenode)) < (m-1)) {
			    result = ((YoixJTreeNode)(((DefaultMutableTreeNode)(treemodel.getChild(parentnode, n+1))).getUserObject())).getSelf();
			}
		    }
		}
		break;

	    case YOIX_MAKE_NODE_VISIBLE:
		makeVisible(treepath = new TreePath(treemodel.getPathToRoot(treenode)));
		scrollPathToVisible(treepath);
		break;

	    case YOIX_NEW_FOR_OLD:
		if ((parentnode = (DefaultMutableTreeNode)(targetnode.getParent())) == null) {
		    data.put(N_TOP, crntnode);
		    parent.setField(N_TOP);
		} else {
		    n = treemodel.getIndexOfChild(parentnode, targetnode);
		    ((YoixJTreeNode)(parentnode.getUserObject())).removeYoixChild(prevnode);
		    treemodel.removeNodeFromParent(targetnode);
		    treenode = (DefaultMutableTreeNode)(treemodel.getRoot());
		    added = YoixObject.newDictionary(getElementCount(treenode) + getElementCount(crntnode), -1, false);
		    enm = treenode.breadthFirstEnumeration();
		    while (enm.hasMoreElements()) {
			treenode = ((DefaultMutableTreeNode)(enm.nextElement()));
			((YoixJTreeNode)(treenode.getUserObject())).addSelf(added);
		    }
		    try {
			error_point = VM.pushError();
			VM.pushAccess(LRW_);
			treenode = buildNode(crntnode, null, added, parent.getContext());
			added.setGrowable(false);
			added.setAccessBody(LR__);
			data.put(N_COMPONENTS, added);
			VM.popAccess();
			VM.popError();
			crntnode = ((YoixJTreeNode)(treenode.getUserObject())).getSelf();
			((YoixJTreeNode)(parentnode.getUserObject())).addYoixChild(crntnode, n);
			treemodel.insertNodeInto(treenode, parentnode, n);
			expandPath(new TreePath(treemodel.getPathToRoot(parentnode)));
			result = crntnode;
		    }
		    catch(Error e) {
			if (e != error_point) {
			    VM.popError();
			    throw(e);
			} else VM.error(error_point);
		    }
		    catch(RuntimeException e) {
			VM.popError();
			throw(e);
		    }
		}
		break;

	    case YOIX_NODE_HAS_BEEN_EXPANDED:
		result = YoixObject.newInt(hasBeenExpanded(new TreePath(treemodel.getPathToRoot(treenode))));
		break;

	    case YOIX_NODE_IS_EXPANDED:
		result = YoixObject.newInt(isExpanded(new TreePath(treemodel.getPathToRoot(treenode))));
		break;

	    case YOIX_NODE_IS_SELECTED:
		result = YoixObject.newInt(isPathSelected(new TreePath(treemodel.getPathToRoot(treenode))));
		break;

	    case YOIX_NODE_IS_VISIBLE:
		result = YoixObject.newInt(isVisible(new TreePath(treemodel.getPathToRoot(treenode))));
		break;

	    case YOIX_SCROLL_NODE:
		scrollPathToVisible(new TreePath(treemodel.getPathToRoot(treenode)));
		break;

	    case YOIX_SCROLL_ROW:
		scrollRowToVisible(row);
		break;

	    case YOIX_SELECT_ALL:
		treemodel = (DefaultTreeModel)getModel();
		treenode = (DefaultMutableTreeNode)(treemodel.getRoot());
		enm = treenode.breadthFirstEnumeration();
		while (enm.hasMoreElements()) {
		    targetnode = (DefaultMutableTreeNode)(enm.nextElement());
		    expandPath(new TreePath(treemodel.getPathToRoot(targetnode)));
		}
		setSelectionInterval(0, getRowCount() - 1);
		break;

	    case YOIX_SELECT_ALL_VISIBLE:
		setSelectionInterval(0, getRowCount() - 1);
		break;

	    case YOIX_SELECT_INTERVAL:
		addSelectionInterval(idx1, idx2);
		break;

	    case YOIX_SELECT_NODE:
		setSelectionPath(treepath = new TreePath(treemodel.getPathToRoot(treenode)));
		scrollPathToVisible(treepath);
		break;

	    case YOIX_SELECT_NODES:
		setSelectionPaths(paths);
		break;

	    case YOIX_SELECT_NONE:
		clearSelection();
		break;

	    case YOIX_SELECT_TOGGLE:
		treepath = new TreePath(treemodel.getPathToRoot(treenode));
		if (isPathSelected(treepath))
		    removeSelectionPath(treepath);
		else setSelectionPath(treepath);
		break;

	    case YOIX_TAGGED_COPY:
		result = yoixJTreeNode(treenode, true);
		break;

	    case YOIX_UNTAGGED_COPY:
		result = yoixJTreeNode(treenode);
		break;

	    case YOIX_UPDATE_COPY:
		result = yoixJTreeNode(treenode, true, true);
		break;

	    case YOIX_UPDATE_TREE:
		((YoixJTreeNode)(targetnode.getUserObject())).update(crntnode);
		treemodel.nodeChanged(targetnode);
		treeDidChange();
		break;

	    default:
		VM.abort(INTERNALERROR);
		break;
	}

	return(result == null ? YoixObject.newNull() : result);
    }


    final synchronized YoixObject
    builtinItem(String name, YoixObject arg[]) {

	YoixObject  result = null;
	YoixObject  top;
	TreePath    path;
	double      loc[];
	double      x;
	double      y;

	if (arg.length == 2) {
	    if (arg[0].notNull() && arg[0].isNumber()) {
		x = arg[0].doubleValue();
		if (arg[1].notNull() && arg[1].isNumber()) {
		    // so we don't select "--empty--"
		    if ((top = data.getObject(N_TOP)) != null && top.notNull()) {
			y = arg[1].doubleValue();
			if ((loc = VM.getDefaultMatrix().dtransform(x, y)) != null) {
			    if ((path = getPathForLocation((int)loc[0], (int)loc[1])) != null) {
				result = ((YoixJTreeNode)((DefaultMutableTreeNode)(path.getLastPathComponent())).getUserObject()).getSelf();
			    }
			}
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(result == null ? YoixObject.newNull() : result);
    }


    protected void
    finalize() {

	data = null;
	parent = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    final int
    getElementCount() {

	return getElementCount((DefaultMutableTreeNode)null);
    }


    final int
    getElementCount(DefaultMutableTreeNode node) {

	YoixTreeModel  model;
	Enumeration    preorder;
	int            count = 0;

	if (node == null) {
	    if ((model = (YoixTreeModel)getModel()) != null)
		node = (DefaultMutableTreeNode)(model.getRoot());
	}
	if (node != null) {
	    preorder = node.preorderEnumeration();
	    while (preorder.hasMoreElements()) {
		preorder.nextElement();
		count++;
	    }
	}

	return(count);
    }


    final static int
    getElementCount(YoixObject yjtreenode) {
	
	YoixObject  ykids;
	int         count;
	int         ccount;
	int         n;

	count = yjtreenode.isNull() ? 0 : 1; // self

	if (count > 0) {
	    ykids = yjtreenode.get(N_CHILDREN, false);
	    ccount = ykids.isNull() ? 0 : ykids.length();
	
	    for (n=0; n<ccount; n++)
		count += getElementCount(ykids.get(n, false));
	}

	return(count);
    }


    final Color
    getSelectionBackground() {

	return(defaultSelectionBackground);
    }


    final Color
    getSelectionForeground() {

	return(defaultSelectionForeground);
    }


    final void
    setBorderColor(YoixObject obj) {

	DefaultMutableTreeNode  jnode;
	YoixTreeModel           model;
	YoixJTreeNode           ynode;
	Enumeration             enm;
	Color                   previous;

	previous = defaultBorderColor;
	defaultBorderColor = YoixMake.javaColor(obj, javaDefaultBorderColor);
	if (!defaultBorderColor.equals(previous)) {
	    model = (YoixTreeModel)getModel();
	    jnode = (DefaultMutableTreeNode)(model.getRoot());
	    enm = jnode.breadthFirstEnumeration();
	    do {
		ynode = (YoixJTreeNode)(jnode.getUserObject());
		if (ynode.borderColorIsDefault)
		    ynode.borderColor = defaultBorderColor;
	    } while (enm.hasMoreElements() && (jnode = (DefaultMutableTreeNode)enm.nextElement()) != null);
	    model.reload(this);
	}
    }


    final void
    setClosedIcon(YoixObject obj) {

	DefaultMutableTreeNode  jnode;
	YoixJTreeNode           ynode;
	YoixTreeModel           model;
	Enumeration             enm;
	Icon                    previous;

	previous = defaultClosedIcon;
	if (obj.isNull() && (VM.getTypename(obj) == T_IMAGE || VM.getTypename(obj) == T_POINTER))
	    defaultClosedIcon = null;
	else if (obj.isImage())
	    defaultClosedIcon = YoixMake.javaIcon(obj);
	else if (obj.isInteger() && obj.intValue() == YOIX_DEFAULT_ICON)
	    defaultClosedIcon = javaDefaultClosedIcon;
	else VM.abort(TYPECHECK, N_CLOSEDICON);

	// direct non-equality comparison is OK in this situation
	if (defaultClosedIcon != previous) {
	    model = (YoixTreeModel)getModel();
	    jnode = (DefaultMutableTreeNode)model.getRoot();
	    enm = jnode.breadthFirstEnumeration();
	    do {
		ynode = (YoixJTreeNode)jnode.getUserObject();
		if (ynode.closedIsDefault)
		    ynode.closedIcon = defaultClosedIcon;
	    } while (enm.hasMoreElements() && (jnode = (DefaultMutableTreeNode)enm.nextElement()) != null);
	    model.reload();
	}
    }


    final void
    setLeafIcon(YoixObject obj) {

	DefaultMutableTreeNode  jnode;
	YoixJTreeNode           ynode;
	YoixTreeModel           model;
	Enumeration             enm;
	Icon                    previous;

	previous = defaultLeafIcon;
	if (obj.isNull() && (VM.getTypename(obj) == T_IMAGE || VM.getTypename(obj) == T_POINTER))
	    defaultLeafIcon = null;
	else if (obj.isImage())
	    defaultLeafIcon = YoixMake.javaIcon(obj);
	else if (obj.isInteger() && obj.intValue() == YOIX_DEFAULT_ICON)
	    defaultLeafIcon = javaDefaultLeafIcon;
	else VM.abort(TYPECHECK, N_LEAFICON);

	// direct non-equality comparison is OK in this situation
	if (defaultLeafIcon != previous) {
	    model = (YoixTreeModel)getModel();
	    jnode = (DefaultMutableTreeNode)model.getRoot();
	    enm = jnode.breadthFirstEnumeration();
	    do {
		ynode = (YoixJTreeNode)jnode.getUserObject();
		if (ynode.leafIsDefault)
		    ynode.leafIcon = defaultLeafIcon;
	    } while (enm.hasMoreElements() && (jnode = (DefaultMutableTreeNode)enm.nextElement()) != null);
	    model.reload();
	}
    }


    final void
    setOpenIcon(YoixObject obj) {

	DefaultMutableTreeNode  jnode;
	YoixJTreeNode           ynode;
	YoixTreeModel           model;
	Enumeration             enm;
	Icon                    previous;

	previous = defaultOpenIcon;
	if (obj.isNull() && (VM.getTypename(obj) == T_IMAGE || VM.getTypename(obj) == T_POINTER))
	    defaultOpenIcon = null;
	else if (obj.isImage())
	    defaultOpenIcon = YoixMake.javaIcon(obj);
	else if (obj.isInteger() && obj.intValue() == YOIX_DEFAULT_ICON)
	    defaultOpenIcon = javaDefaultOpenIcon;
	else VM.abort(TYPECHECK, N_OPENICON);

	if (defaultOpenIcon != previous) {
	    model = (YoixTreeModel)getModel();
	    jnode = (DefaultMutableTreeNode)model.getRoot();
	    enm = jnode.breadthFirstEnumeration();
	    do {
		ynode = (YoixJTreeNode)jnode.getUserObject();
		if (ynode.openIsDefault)
		    ynode.openIcon = defaultOpenIcon;
	    } while (enm.hasMoreElements() && (jnode = (DefaultMutableTreeNode)enm.nextElement()) != null);
	    model.reload();
	}
    }


    final void
    setSelectionBackground(YoixObject obj) {

	DefaultMutableTreeNode  jnode;
	YoixJTreeNode           ynode;
	YoixTreeModel           model;
	Enumeration             enm;
	Color                   color;

	color = defaultSelectionBackground;
	defaultSelectionBackground = YoixMake.javaColor(obj, javaDefaultSelectionBackground);
	if (!defaultSelectionBackground.equals(color)) {
	    model = (YoixTreeModel)getModel();
	    jnode = (DefaultMutableTreeNode)(model.getRoot());
	    enm = jnode.breadthFirstEnumeration();
	    do {
		ynode = (YoixJTreeNode)(jnode.getUserObject());
		if (ynode.selectionBackgroundIsDefault)
		    ynode.selectionBackground = defaultSelectionBackground;
	    } while (enm.hasMoreElements() && (jnode = (DefaultMutableTreeNode)(enm.nextElement())) != null);
	    model.reload(this);
	}
    }


    final void
    setSelectionForeground(YoixObject obj) {

	DefaultMutableTreeNode  jnode;
	YoixJTreeNode           ynode;
	YoixTreeModel           model;
	Enumeration             enm;
	Color                   color;

	color = defaultSelectionForeground;
	defaultSelectionForeground = YoixMake.javaColor(obj, javaDefaultSelectionForeground);
	if (!defaultSelectionForeground.equals(color)) {
	    model = (YoixTreeModel)getModel();
	    jnode = (DefaultMutableTreeNode)(model.getRoot());
	    enm = jnode.breadthFirstEnumeration();
	    do {
		ynode = (YoixJTreeNode)(jnode.getUserObject());
		if (ynode.selectionForegroundIsDefault)
		    ynode.selectionForeground = defaultSelectionForeground;
	    } while (enm.hasMoreElements() && (jnode = (DefaultMutableTreeNode)(enm.nextElement())) != null);
	    model.reload(this);
	}
    }


    final synchronized void
    setTop(YoixObject obj) {

	DefaultMutableTreeNode  root = null;
	YoixError               error_point = null;
	YoixObject              components;
	HashMap                 added;

	//
	// Explicitly setting N_TOP here seems to be required in some
	// very subtle cases because the setRoot(root) call can cause
	// a T_TREESELECTIONEVENT to be sent to a Yoix event handler
	// and that event could arrive before YoixPointerActive gets
	// a chance to update data.top. If the event handler decides
	// to check top (e.g., to see if it's NULL) it could get the
	// wrong answer if data.top hasn't been updated. This actually
	// happened in a Yoix application and it resulted in behavior
	// that occasionally was hard to explain.
	//
	// NOTE - are there other places where something similiar
	// could happen??
	//

	try {
	    error_point = VM.pushError();
	    VM.pushAccess(LRW_);
	    added = new HashMap();
	    if (obj.notNull())
		root = buildNode(obj, null, added, parent.getContext());
	    else root = new DefaultMutableTreeNode(new YoixJTreeNode(YoixObject.newString("--empty--"), parent.getContext()), true);
	    components = YoixMisc.copyIntoDictionary(added);
	    components.setGrowable(false);
	    components.setAccessBody(LR__);
	    data.put(N_TOP, obj);		// subtle change
	    data.put(N_COMPONENTS, components);
	    VM.popAccess();
	    VM.popError();
	}
	catch(Error e) {
	    if (e != error_point) {
		VM.popError();
		throw(e);
	    } else VM.error(error_point);
	}
	catch(RuntimeException e) {
	    VM.popError();
	    throw(e);
	}
	((YoixTreeModel)getModel()).setRoot(root);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private synchronized DefaultMutableTreeNode
    buildNode(YoixObject self, DefaultMutableTreeNode parent, YoixObject added, YoixObject root) {

	DefaultMutableTreeNode  thisnode = null;
	YoixJTreeNode           jtreenode;
	YoixObject              children;
	String                  tag;
	int                     len;
	int                     i;

	jtreenode = new YoixJTreeNode(self, root);
	tag = jtreenode.getTag();

	if (added.defined(tag) == false) {
	    added.put(tag, jtreenode.getSelf());
	    children = jtreenode.getChildren();
	    thisnode = new DefaultMutableTreeNode(jtreenode, true);
	    if (children != null) {
		len = children.length();
		for (i = children.offset(); i < len; i++)
		    buildNode(children.get(i, false), thisnode, added, root);
	    }

	    if (parent != null)
		parent.add(thisnode);
	    else lockdown(thisnode);
	} else VM.abort(DUPLICATETAG, tag);

	return(thisnode);
    }


    private synchronized DefaultMutableTreeNode
    buildNode(YoixObject self, DefaultMutableTreeNode parent, HashMap added, YoixObject root) {

	DefaultMutableTreeNode  thisnode = null;
	YoixJTreeNode           jtreenode;
	YoixObject              children;
	String                  tag;
	int                     len;
	int                     i;

	jtreenode = new YoixJTreeNode(self, root);
	tag = jtreenode.getTag();

	if (added.containsKey(tag) == false) {
	    added.put(tag, jtreenode.getSelf());
	    children = jtreenode.getChildren();
	    thisnode = new DefaultMutableTreeNode(jtreenode, true);
	    if (children != null) {
		len = children.length();
		for (i = children.offset(); i < len; i++)
		    buildNode(children.get(i, false), thisnode, added, root);
	    }

	    if (parent != null)
		parent.add(thisnode);
	    else lockdown(thisnode);
	} else VM.abort(DUPLICATETAG, tag);

	return(thisnode);
    }


    private DefaultMutableTreeNode
    findTaggedTreeNode(JTree tree, String tag) {

	DefaultMutableTreeNode  node;
	DefaultMutableTreeNode  elem;
	Enumeration             enm;

	node = null;

	if (tree != null && tag != null) {
	    if ((elem = ((DefaultMutableTreeNode)(tree.getModel().getRoot()))) != null) {
		if (tag.equals(((YoixJTreeNode)(elem.getUserObject())).getTag())) {
		    node = elem;
		} else {
		    enm = elem.breadthFirstEnumeration();
		    while (enm.hasMoreElements()) {
			elem = ((DefaultMutableTreeNode)(enm.nextElement()));
			if (tag.equals(((YoixJTreeNode)(elem.getUserObject())).getTag())) {
			    node = elem;
			    break;
			}
		    }
		}
	    }
	}

	return(node);
    }


    private void
    lockdown(DefaultMutableTreeNode base) {

	Enumeration             enm;
	DefaultMutableTreeNode  node;

	enm = base.depthFirstEnumeration();
	while (enm.hasMoreElements()) {
	    node = (DefaultMutableTreeNode)(enm.nextElement());
	    ((YoixJTreeNode)node.getUserObject()).lockdown();
	}
    }


    private void
    treeCollapsedOrExpanded(TreeExpansionEvent event, int type) {

	DefaultMutableTreeNode  jnode;
	YoixObject              ynode;
	YoixObject              evobj;

	jnode = (DefaultMutableTreeNode)(event.getPath()).getLastPathComponent();
	ynode = ((YoixJTreeNode)(jnode.getUserObject())).getSelf();
	evobj = YoixMake.yoixType(T_INVOCATIONEVENT);
	evobj.putInt(N_ID, V_INVOCATIONBROWSE);
	evobj.putInt(N_TYPE, type);
	Runnable runevent = new YoixAWTInvocationEvent(
	    parent.getData().getObject(N_INVOCATIONBROWSE),
	    new YoixObject[] {evobj},
	    ynode
	);
	EventQueue.invokeLater(runevent);
    }


    private void
    treeWillCollapseOrExpand(TreeExpansionEvent event, int type) throws ExpandVetoException {

	DefaultMutableTreeNode  jnode;
	YoixObject              ynode;
	YoixObject              yretv;
	YoixObject              evobj;

	jnode = (DefaultMutableTreeNode)(event.getPath()).getLastPathComponent();
	ynode = ((YoixJTreeNode)(jnode.getUserObject())).getSelf();
	evobj = YoixMake.yoixType(T_INVOCATIONEVENT);
	evobj.putInt(N_ID, V_INVOCATIONBROWSE);
	evobj.putInt(N_TYPE, type);
	yretv = parent.call(
	    parent.getData().getObject(N_INVOCATIONBROWSE),
	    new YoixObject[] {evobj},
	    ynode
	);
	if (yretv != null && yretv.notNull() && yretv.isInteger()) {
	    if (!yretv.booleanValue())
		throw new ExpandVetoException(event);
	}
    }


    private synchronized YoixObject
    yoixJTreeNode(DefaultMutableTreeNode treenode) {

	return(yoixJTreeNode(treenode, false, false));
    }


    private synchronized YoixObject
    yoixJTreeNode(DefaultMutableTreeNode treenode, boolean tagged) {

	return(yoixJTreeNode(treenode, tagged, false));
    }


    private synchronized YoixObject
    yoixJTreeNode(DefaultMutableTreeNode treenode, boolean tagged, boolean childless) {

	DefaultMutableTreeNode  child;
	YoixJTreeNode           node;
	YoixObject              obj;
	YoixObject              children;
	Object                  userobject;
	int                     ccnt;
	int                     n;

	obj = YoixMake.yoixType(T_JTREENODE);
	userobject = treenode.getUserObject();

	if (userobject != null && userobject instanceof YoixJTreeNode) {
	    node = (YoixJTreeNode)userobject;
	    if (node.background != null)
		obj.putColor(N_BACKGROUND, node.background);

	    if (node.borderColorIsDefault)
		obj.put(N_BORDERCOLOR, YoixObject.newNull(), false);
	    else obj.putColor(N_BORDERCOLOR, node.borderColor);

	    if (!childless && (ccnt = treenode.getChildCount()) > 0) {
		children = YoixObject.newArray(ccnt);
		for (n = 0; n < ccnt; n++) {
		    child = (DefaultMutableTreeNode)(treenode.getChildAt(n));
		    children.put(n, yoixJTreeNode(child), false);
		}
		obj.put(N_CHILDREN, children, false);
	    }

	    if (node.closedIsDefault)
		obj.putInt(N_CLOSEDICON, YOIX_DEFAULT_ICON);
	    else if (node.closedIcon == null)
		obj.put(N_CLOSEDICON, YoixObject.newNull(), false);
	    else obj.put(N_CLOSEDICON, YoixMake.yoixIcon(node.closedIcon), false);

	    obj.put(N_CONTENT, node.self.getObject(N_CONTENT), true);

	    if (node.font != null)
		obj.put(N_FONT, YoixMake.yoixFont(node.font), false);

	    if (node.foreground != null)
		obj.putColor(N_FOREGROUND, node.foreground);

	    if (node.leafIsDefault)
		obj.putInt(N_LEAFICON, YOIX_DEFAULT_ICON);
	    else if (node.leafIcon == null)
		obj.put(N_LEAFICON, YoixObject.newNull(), false);
	    else obj.put(N_LEAFICON, YoixMake.yoixIcon(node.leafIcon), false);

	    if (node.openIsDefault)
		obj.putInt(N_OPENICON, YOIX_DEFAULT_ICON);
	    else if (node.openIcon == null)
		obj.put(N_OPENICON, YoixObject.newNull(), false);
	    else obj.put(N_OPENICON, YoixMake.yoixIcon(node.openIcon), false);

	    // always skip ROOT

	    if (node.selectionBackgroundIsDefault)
		obj.put(N_SELECTIONBACKGROUND, YoixObject.newNull(), false);
	    else obj.putColor(N_SELECTIONBACKGROUND, node.selectionBackground);

	    if (node.selectionForegroundIsDefault)
		obj.put(N_SELECTIONFOREGROUND, YoixObject.newNull(), false);
	    else obj.putColor(N_SELECTIONFOREGROUND, node.selectionForeground);

	    if (tagged)
		obj.putString(N_TAG, node.tag);
	    obj.putString(N_TEXT, node.text);
	    obj.putString(N_TOOLTIPTEXT, node.toolTip);
	}

	return(obj);
    }


    static YoixObject
    yoixJTreeNode2(DefaultMutableTreeNode treenode) {

	YoixJTreeNode  node;
	YoixObject     obj = null;
	Object         userobject;

	//
	// Added quickly so JTreeNodes returned in TreeSelectionEvents
	// match (using == comparison) the nodes that are returned by
	// the action() builtin. Needs a closer look along with a better
	// name!!!
	//

	if ((userobject = treenode.getUserObject()) != null) {
	    if (userobject instanceof YoixJTreeNode) {
		node = (YoixJTreeNode)userobject;
		obj = node.getSelf();
	    }
	}

	return(obj != null ? obj : YoixObject.newNull());
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTreeNode {

	String      tag;
	String      text;
	YoixObject  self;
	YoixObject  children;

	Icon        closedIcon;
	Icon        openIcon;
	Icon        leafIcon;

	boolean     closedIsDefault;
	boolean     openIsDefault;
	boolean     leafIsDefault;

	Color       background;
	Color       borderColor;
	Color       foreground;
	Color       selectionBackground;
	Color       selectionForeground;

	boolean     borderColorIsDefault;
	boolean     selectionBackgroundIsDefault;
	boolean     selectionForegroundIsDefault;

	Font        font;

	String      toolTip;

        ///////////////////////////////////
        //
        // Constructors
        //
        ///////////////////////////////////

	YoixJTreeNode(YoixObject node, YoixObject root) {

	    setup(node, root);
	}

        ///////////////////////////////////
        //
        // YoixJTreeNode Methods
        //
        ///////////////////////////////////

	final void
	addSelf(YoixObject added) {

	    added.put(self.getString(N_TAG), self);
	}


	final void
	addYoixChild(YoixObject child) {

	    YoixError   error_point = null;
	    YoixObject  obj;

	    obj = self.get(N_CHILDREN);
	    if (obj.isNull()) {
		obj = YoixObject.newArray(0);
	    }
	    children = obj;

	    try {
		error_point = VM.pushError();
		VM.pushAccess(RW_);
		children.setGrowable(true);
		children.put(children.length(), child, false);
		self.put(N_CHILDREN, children);
		children.setGrowable(false);
		VM.popAccess();
		VM.popError();
	    }
	    catch(Error e) {
		if (e != error_point) {
		    VM.popError();
		    throw(e);
		} else VM.error(error_point);
	    }
	    catch(RuntimeException e) {
		VM.popError();
		throw(e);
	    }
	}


	final void
	addYoixChild(YoixObject child, int pos) {

	    YoixError   error_point = null;
	    YoixObject  obj;
	    YoixObject  newlist;
	    int         length;
	    int         offset;
	    int         m, n;

	    obj = self.get(N_CHILDREN);
	    if (obj.isNull()) {
		obj = YoixObject.newArray(0);
	    }
	    children = obj;

	    length = children.sizeof();
	    if (pos == length)
		addYoixChild(child);
	    else {
		newlist = YoixObject.newArray(length+1);
		offset = children.offset();
		for (m = 0, n = 0; n < length; n++) {
		    // at this point, this should be true
		    if ((obj = children.getObject(n + offset)) != null) {
			// at this point, these should be true
			if (obj.isJTreeNode() && obj.notNull()) {
			    if (n == pos)
				newlist.put(m++, child, false);
			    newlist.put(m++, obj, false);
			} else VM.abort(INTERNALERROR);
		    } else VM.abort(INTERNALERROR);
		}

		children = newlist;

		try {
		    error_point = VM.pushError();
		    VM.pushAccess(LRW_);
		    self.put(N_CHILDREN, newlist);
		    newlist.setGrowable(false);
		    newlist.setAccess(LR__);
		    newlist.setAccessBody(LR__);
		    VM.popAccess();
		    VM.popError();
		}
		catch(Error e) {
		    if (e != error_point) {
			VM.popError();
			throw(e);
		    } else VM.error(error_point);
		}
		catch(RuntimeException e) {
		    VM.popError();
		    throw(e);
		}
	    }

	}


	final YoixObject
	getChildren() {

	    return(children);
	}


	final YoixObject
	getSelf() {

	    return(self);
	}


	final String
	getTag() {

	    return(tag);
	}


	final void
	lockdown() {

	    YoixObject  obj;

	    synchronized(YoixSwingJTree.this) {

		obj = self.getObject(N_BACKGROUND);
		obj.setAccess(LR__);
		obj = self.getObject(N_BORDERCOLOR);
		obj.setAccess(LR__);

		if (children != null) {
		    children.setGrowable(false);
		    children.setAccess(LR__);
		    children.setAccessBody(LR__);
		} else {
		    obj = self.getObject(N_CHILDREN);
		    obj.setAccess(LR__);
		}

		obj = self.getObject(N_CLOSEDICON);
		obj.setAccess(LR__);

		obj = self.getObject(N_FONT);
		obj.setAccess(LR__);

		obj = self.getObject(N_FOREGROUND);
		obj.setAccess(LR__);

		obj = self.getObject(N_LEAFICON);
		obj.setAccess(LR__);

		obj = self.getObject(N_OPENICON);
		obj.setAccess(LR__);

		obj = self.getObject(N_SELECTIONBACKGROUND);
		obj.setAccess(LR__);

		obj = self.getObject(N_SELECTIONFOREGROUND);
		obj.setAccess(LR__);

		obj = self.getObject(N_TAG);
		obj.setAccess(LR__);

		obj = self.getObject(N_TEXT);
		obj.setAccess(LR__);

		obj = self.getObject(N_TOOLTIPTEXT);
		obj.setAccess(LR__);

	    }
	}


	final void
	removeYoixChild(YoixObject child) {

	    YoixObject  newlist;
	    YoixObject  obj;
	    YoixError   error_point = null;
	    int         length;
	    int         offset;
	    int         n;
	    int         m;

	    synchronized(YoixSwingJTree.this) {

		if (children != null) {
		    length = children.sizeof() - 1;
		    newlist = YoixObject.newArray(length);
		    offset = children.offset();
		    for (m = 0, n = 0; n <= length; n++) {
			// at this point, this should be true
			if ((obj = children.getObject(n + offset)) != null) {
			    // at this point, these should be true
			    if (obj.isJTreeNode() && obj.notNull()) {
				// direct (in)equality should suffice
				if (obj.body() != child.body()) {
				    if (m < length)
					newlist.put(m, obj, false);
				    m++;
				}
			    } else VM.abort(INTERNALERROR);
			} else VM.abort(INTERNALERROR);
		    }
		    if (m < n) {
			if (m == 0) {
			    children = null;
			    newlist = YoixObject.newNull();
			} else children = newlist;

			try {
			    error_point = VM.pushError();
			    VM.pushAccess(LRW_);
			    self.put(N_CHILDREN, newlist);
			    newlist.setGrowable(false);
			    newlist.setAccess(LR__);
			    newlist.setAccessBody(LR__);
			    VM.popAccess();
			    VM.popError();
			}
			catch(Error e) {
			    if (e != error_point) {
				VM.popError();
				throw(e);
			    } else VM.error(error_point);
			}
			catch(RuntimeException e) {
			    VM.popError();
			    throw(e);
			}
		    }
		}

	    }
	}


	public final String
	toString() {

	    return(text);
	}

        ///////////////////////////////////
        //
        // Private Methods
        //
        ///////////////////////////////////

	private void
	setup(YoixObject node, YoixObject root) {

	    YoixObject  obj;
	    YoixError   error_point = null;

	    if (node.isJTreeNode()) {
		self = node;
	    } else if (node.isString()) {
		self = YoixMake.yoixJTreeNode(node.stringValue());
		text = node.stringValue();
		self.put(N_TEXT, node);
	    } else {
		text = "-- bad value -- [" + node + "]";
		self = YoixMake.yoixJTreeNode(text);
		self.put(N_TEXT, YoixObject.newString(text));
	    }

	    try {
		error_point = VM.pushError();
		VM.pushAccess(RW_);
		self.put(N_ROOT, root, true);
		VM.popAccess();
		VM.popError();
	    }
	    catch(Error e) {
		if (e != error_point) {
		    VM.popError();
		    throw(e);
		} else VM.error(error_point);
	    }
	    catch(RuntimeException e) {
		VM.popError();
		throw(e);
	    }

	    update(self);

	    obj = self.get(N_CHILDREN);
	    if (obj.isNull() || obj.sizeof() == 0)
		children = null;
	    else children = obj;

	    obj = self.getObject(N_TAG);
	    if (obj.notNull()) {
		tag = obj.stringValue();
	    } else {
		obj = YoixObject.newString(tag = "_" + YoixMisc.nextID());
		self.put(N_TAG, obj);
	    }
	}


	void
	update(YoixObject newself) {

	    YoixError   error_point = null;
	    YoixObject  obj;

	    synchronized(YoixSwingJTree.this) {
		obj = newself.getObject(N_TEXT);
		if (obj.notNull())
		    text = obj.stringValue();
		else {
		    text = "-- no label --";
		    obj = YoixObject.newString(text);
		    newself.put(N_TEXT, obj);
		}

		obj = newself.getObject(N_BACKGROUND);
		if (obj.notNull())
		    background = YoixMake.javaColor(obj);
		else background = null;

		obj = newself.getObject(N_BORDERCOLOR);
		if (obj.notNull()) {
		    borderColor = YoixMake.javaColor(obj);
		    borderColorIsDefault = false;
		} else {
		    borderColor = defaultBorderColor;
		    borderColorIsDefault = true;
		}

		obj = newself.getObject(N_CLOSEDICON, null);
		if (obj.isNull()) {
		    closedIcon = null;
		    closedIsDefault = false;
		} else if (obj.isImage()) {
		    closedIcon = YoixMake.javaIcon(obj);
		    closedIsDefault = false;
		} else if (obj.isInteger() && obj.intValue() == YOIX_DEFAULT_ICON) {
		    closedIcon = defaultClosedIcon;
		    closedIsDefault = true;
		} else VM.abort(TYPECHECK, N_CLOSEDICON);

		obj = newself.getObject(N_FONT);
		if (obj.notNull())
		    font = YoixMakeScreen.javaFont(obj); // typecheck abort possible
		else font = null;

		obj = newself.getObject(N_FOREGROUND);
		if (obj.notNull())
		    foreground = YoixMake.javaColor(obj);
		else foreground = null;

		obj = newself.getObject(N_LEAFICON, null);
		if (obj.isNull()) {
		    leafIcon = null;
		    leafIsDefault = false;
		} else if (obj.isImage()) {
		    leafIcon = YoixMake.javaIcon(obj);
		    leafIsDefault = false;
		} else if (obj.isInteger() && obj.intValue() == YOIX_DEFAULT_ICON) {
		    leafIcon = defaultLeafIcon;
		    leafIsDefault = true;
		} else VM.abort(TYPECHECK, N_LEAFICON);

		obj = newself.getObject(N_OPENICON);
		if (obj.isNull()) {
		    openIcon = null;
		    openIsDefault = false;
		} else if (obj.isImage()) {
		    openIcon = YoixMake.javaIcon(obj);
		    openIsDefault = false;
		} else if (obj.isInteger() && obj.intValue() == YOIX_DEFAULT_ICON) {
		    openIcon = defaultOpenIcon;
		    openIsDefault = true;
		} else VM.abort(TYPECHECK, N_OPENICON);

		obj = newself.getObject(N_SELECTIONBACKGROUND);
		if (obj.notNull()) {
		    selectionBackground = YoixMake.javaColor(obj);
		    selectionBackgroundIsDefault = false;
		} else {
		    selectionBackground = defaultSelectionBackground;
		    selectionBackgroundIsDefault = true;
		}

		obj = newself.getObject(N_SELECTIONFOREGROUND);
		if (obj.notNull()) {
		    selectionForeground = YoixMake.javaColor(obj);
		    selectionForegroundIsDefault = false;
		} else {
		    selectionForeground = defaultSelectionForeground;
		    selectionForegroundIsDefault = true;
		}

		obj = newself.getObject(N_TOOLTIPTEXT);
		if (obj.notNull())
		    toolTip = obj.stringValue();
		else toolTip = null;

		if (self != newself) {
		    try {
			error_point = VM.pushError();
			VM.pushAccess(RW_);
			self.put(N_BACKGROUND, newself.getObject(N_BACKGROUND), true);
			self.put(N_BORDERCOLOR, newself.getObject(N_BORDERCOLOR), true);
			self.put(N_CLOSEDICON, newself.getObject(N_CLOSEDICON), true);
			self.put(N_CONTENT, newself.getObject(N_CONTENT), false);
			self.put(N_FONT, newself.getObject(N_FONT), true);
			self.put(N_FOREGROUND, newself.getObject(N_FOREGROUND), true);
			self.put(N_LEAFICON, newself.getObject(N_LEAFICON), true);
			self.put(N_OPENICON, newself.getObject(N_OPENICON), true);
			self.put(N_SELECTIONBACKGROUND, newself.getObject(N_SELECTIONBACKGROUND), true);
			self.put(N_SELECTIONFOREGROUND, newself.getObject(N_SELECTIONFOREGROUND), true);
			self.put(N_TEXT, newself.getObject(N_TEXT), true);
			self.put(N_TOOLTIPTEXT, newself.getObject(N_TOOLTIPTEXT), true);
			VM.popAccess();
			VM.popError();
		    }
		    catch(Error e) {
			if (e != error_point) {
			    VM.popError();
			    throw(e);
			} else VM.error(error_point);
		    }
		    catch(RuntimeException e) {
			VM.popError();
			throw(e);
		    }
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixTreeCellRenderer extends DefaultTreeCellRenderer {

        ///////////////////////////////////
        //
        // YoixTreeCellRenderer Methods
        //
        ///////////////////////////////////

	public Component
	getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

	    YoixJTreeNode  node;
	    Object         userobj;

	    userobj = ((DefaultMutableTreeNode)value).getUserObject();

	    if (userobj instanceof YoixJTreeNode) {
		node = (YoixJTreeNode)userobj;
		this.setOpenIcon(node.openIcon);	// some compilers complain
		this.setClosedIcon(node.closedIcon);	// some compilers complain
		this.setLeafIcon(node.leafIcon);	// some compilers complain

		if (node.foreground != null)
		    this.setTextNonSelectionColor(node.foreground);
		else this.setTextNonSelectionColor(tree.getForeground());

		this.setTextSelectionColor(node.selectionForeground);
		this.setFont(node.font);
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (node.toolTip != null && tree.getToolTipText() != null)
		    setToolTipText(node.toolTip);
		else setToolTipText(null);

		if (node.background != null)
		    this.setBackgroundNonSelectionColor(node.background);
		else this.setBackgroundNonSelectionColor(tree.getBackground());

		this.setBorderSelectionColor(node.borderColor);
		this.setBackgroundSelectionColor(node.selectionBackground);
	    } else super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

	    return(this);
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixTreeModel extends DefaultTreeModel {

        ///////////////////////////////////
        //
        // Constructors
        //
        ///////////////////////////////////

	public
	YoixTreeModel(TreeNode root) {

	    super(root, false);
	}

        ///////////////////////////////////
        //
        // YoixTreeModel Methods
        //
        ///////////////////////////////////

	public void
	reload(JTree jtree) {

	    Enumeration  enm;
	    TreePath     treepath;
	    TreePath     selections[];
	    int          rows[];
	    int          rtmp[];
	    int          rcnt;
	    int          n;

	    if ((rcnt = jtree.getRowCount()) > 0) {
		if ((treepath = jtree.getPathForRow(0)) != null) {
		    if ((enm = jtree.getExpandedDescendants(treepath)) != null) {
			rows = new int[rcnt];
			rcnt = 0;
			while (enm.hasMoreElements()) {
			    treepath = (TreePath)(enm.nextElement());
			    rows[rcnt++] = jtree.getRowForPath(treepath);
			}
			rtmp = rows;
			rows = new int[rcnt];
			System.arraycopy(rtmp, 0, rows, 0, rcnt);
			YoixMiscQsort.qsort(rows, 1);
		    } else rows = null;
		} else rows = null;

		selections = jtree.getSelectionPaths();
		reload();

		if (rows != null) {
		    for (n = 0; n < rcnt; n++)
			jtree.expandRow(rows[n]);
		} else jtree.collapseRow(0);

		if (selections != null)
		    jtree.setSelectionPaths(selections);
	    }
	}


	public void
	valueForPathChanged(TreePath path, Object newValue) {

	    DefaultMutableTreeNode  aNode;
	    YoixJTreeNode           ynode;
	    String                  text;

	    aNode = (DefaultMutableTreeNode)path.getLastPathComponent();

	    if (newValue instanceof String) {
		text = ((String)newValue).trim();
		if (text.length() > 0) {
		    ynode = (YoixJTreeNode)(aNode.getUserObject());
		    ynode.text = text;
		    ynode.getSelf().put(N_TEXT, YoixObject.newString(text));
		}
	    } else aNode.setUserObject(newValue);	// can this ever happen??

	    nodeChanged(aNode);
	}
    }

}

