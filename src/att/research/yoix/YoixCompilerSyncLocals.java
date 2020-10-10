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
import java.io.*;
import java.util.*;

class YoixCompilerSyncLocals

    implements YoixCompilerConstants

{

    //
    // A simple class designed to walk a parse tree and recognize the local
    // variables that are referenced in that parse tree. Whenever we find a
    // variable we call compiler.syncLocalVariable() method with arguments
    // that let the compiler know exactly what we found.
    //
    // NOTE - this started as a copy of YoixBinder.java, so there's probably
    // a bunch that's still missing. It's good enough for now, but will need
    // lots of work before its ready for production.
    //

    private YoixCompiler  compiler;
    private YoixStack     yoixstack;

    //
    // Set in syncLocals() and passed back to compiler.syncLocalVariable(),
    // whenever we call it.
    //

    private StringBuffer  sbuf;
    private HashMap       synced;
    private HashMap       modified;
    private int           mode;

    //
    // Right now all we do for branch nodes is let the complier know that it
    // shouldn't be keeping local variable copies whenever we detect certain
    // constructs in the parse tree. We use a HashMap to remember the branch
    // nodes that we've checked so we don't do it again.
    //

    private HashMap  branch_directory = new HashMap();

    //
    // These are used to set mode that we're operating in.
    //

    private static final int  BRANCH_MODE = 1;
    private static final int  SYNC_MODE = 2;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixCompilerSyncLocals(YoixCompiler compiler, YoixStack yoixstack) {

	this.compiler = compiler;
	this.yoixstack = yoixstack;
    }

    ///////////////////////////////////
    //
    // YoixCompilerSyncLocals Methods
    //
    ///////////////////////////////////

    final synchronized void
    syncBranches(SimpleNode node, StringBuffer sbuf) {

	if (isBranchRegistered(node) == false) {
	    this.sbuf = sbuf;
	    mode = BRANCH_MODE;
	    switch (node.type()) {
		case DO:
		    statementDo(node);
		    break;

		case FOR:
		    statementFor(node);
		    break;

		case FOREACH:
		    statementForEach(node);
		    break;

		case IF:
		    statementIf(node);
		    break;

		case SWITCH:
		    statementSwitch(node);
		    break;

		case WHILE:
		    statementWhile(node);
		    break;
	    }
	}
    }


    final synchronized HashMap
    syncCaseLabels(SimpleNode node, StringBuffer sbuf) {

	SimpleNode  stmt;
	SimpleNode  child;
	int         length;
	int         n;

	//
	// We're only interested in syncing expressions that are assoiciated
	// with case statements.
	//

	if (node != null) {
	    this.sbuf = sbuf;
	    synced = new HashMap();
	    modified = new HashMap();
	    mode = SYNC_MODE;
	    stmt = node.getChild1();
	    length = stmt.length();

	    for (n = 0; n < length; n++) {
		child = stmt.getChild(n);
		switch (child.type()) {
		    case CASE:
			expression(child.getChild0());
			break;

		    case TAGGED:
			child = child.getTaggedNode();
			if (child.type() == CASE)
			    expression(child.getChild0());
			break;
		}
	    }
	} else modified = null;

	return(modified);
    }


    final synchronized HashMap
    syncLvalue(SimpleNode node, StringBuffer sbuf) {

	if (node != null) {
	    this.sbuf = sbuf;
	    synced = new HashMap();
	    modified = new HashMap();
	    mode = SYNC_MODE;
	    lvalue(node);
	} else modified = null;

	return(modified);
    }


    final synchronized HashMap
    syncNew(SimpleNode node, StringBuffer sbuf) {

	if (node != null) {
	    this.sbuf = sbuf;
	    synced = new HashMap();
	    modified = new HashMap();
	    mode = SYNC_MODE;
	    expressionNew(node);
	} else modified = null;

	return(modified);
    }


    final synchronized HashMap
    syncStatement(SimpleNode node, boolean wantmodified, StringBuffer sbuf) {

	if (node != null) {
	    this.sbuf = sbuf;
	    synced = new HashMap();
	    modified = new HashMap();
	    mode = SYNC_MODE;
	    statement(node);
	} else {
	    modified = null;
	    synced = null;
	}

	return(wantmodified ? modified : synced);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    declareVariable(String typename, SimpleNode dnode, SimpleNode inode, boolean create) {

	YoixObject  lval;
	YoixError   error_point = null;
	SimpleNode  child;
	int         length;
	int         n;

	//
	// Suspect YoixBodyBlock.newDvalue() was causing some of problems
	// that we had with dictionaries. Also, ivalue() doesn't currently
	// handle dictionaries, but that's something we'll address before
	// too long.
	//
	// NOTE - we ignore declaration errors because some are expected,
	// but even if they're not we really not interested in detecting
	// error in this class.
	//
	// NOTE - we completely ignore typename and instead use T_OBJECT
	// because types can be created dynamically or defined by modules.
	// This is a change that we may want to port to YoixBinder - later.
	//

	if (create) {
	    try {
		error_point = yoixstack.pushError();
		lval = YoixBodyBlock.newDvalue(dnode.getChild0().stringValue());
		lval.declare(YoixMake.yoixType(T_OBJECT));
		yoixstack.popError();
	    }
	    catch(YoixError e) {
		if (e != error_point)
		    yoixstack.popError();
	    }
	}

	ivalue(inode);

	if ((child = dnode.getChild1()) != null) {
	    switch (child.type()) {
		case EXPRESSION:
		    expression(child);
		    break;

		case RANGE:
		    if ((length = child.length()) > 0) {
			for (n = 0; n < length; n++)
			    expression(child.getChild(n));
		    }
		    break;
	    }
	}
    }


    private void
    expression(SimpleNode node) {

	SimpleNode  child;
	boolean     modify;
	int         length;
	int         op;
	int         n;

	//
	// We eliminated cases that really have nothing to do, which means
	// there's no default case. Not completely convinced, so some or all
	// of them may reappear in a future release.
	//

	if (node != null && (length = node.length()) > 0) {
	    for (n = 0; n < length; n++) {
		child = node.getChild(n);
		switch (op = child.type()) {
		    case ARRAY:
		    case DICTIONARY:
			ivalue(child);
			break;

		    case CONDITIONAL:
			expression(child.getChild0());
			n++;
			break;

		    case EXPRESSION:
			expression(child);
			break;

		    case LVALUE:
			modify = false;
			if (mode == SYNC_MODE) {
			    if (n < length - 1) {
				switch (node.getChild(n+1).type()) {
				    case POSTDECREMENT:
				    case POSTINCREMENT:
				    case PREDECREMENT:
				    case PREINCREMENT:
					modify = true;
					break;

				    default:
					if (n < length - 2) {
					    switch (node.getChild(n+2).type()) {
						case ASSIGN:
						case PLUSEQ:
						case MINUSEQ:
						case MULEQ:
						case DIVEQ:
						case MODEQ:
						case LEFTSHIFTEQ:
						case RIGHTSHIFTEQ:
						case UNSIGNEDSHIFTEQ:
						case ANDEQ:
						case OREQ:
						case XOREQ:
						    modify = true;
						    break;
					    }
					}
					break;
				}
			    }
			}
			lvalue(child, false, modify);
			break;

		    case QUESTIONCOLON:
			expression(child.getChild0());
			expression(child.getChild1());
			expression(child.getChild2());
			break;

		    case ATTRIBUTE:
			lvalue(child.getChild0());
			break;

		    case NEW:
			expressionNew(child);
			break;
		}
	    }
	}
    }


    private void
    expressionNew(SimpleNode node) {

	SimpleNode  dnode;

	dnode = node.getChild0();
	declareVariable(dnode.getChild0().stringValue(), dnode, node.getChild1(), false);
    }


    private void
    functionCall(SimpleNode args) {

	int  argc;
	int  n;

	argc = args.length();

	for (n = 0; n < argc; n++)
	    expression(args.getChild(n));
    }


    private boolean
    isBranchRegistered(SimpleNode node) {

	return(branch_directory.containsKey(node));
    }


    private void
    ivalue(SimpleNode node) {

	YoixObject  dest;
	int         count;
	int         n;

	//
	// The DICTIONARY case eventually needs to be filled in - see the
	// corresponding code in YoixInterpreter.java for more details. We
	// skipped it because this code was written quickly and we had some
	// trouble when a dictionary was used for block storage.
	//

	if (node != null) {
	    switch (node.type()) {
		case ARRAY:
		    for (n = 0; n < node.length(); n++)
			expression(node.getChild(n));
		    break;

		case DICTIONARY:
		    //
		    // This eventually should be filled in!!
		    //
		    break;

		case EXPRESSION:
		    expression(node.getChild0());
		    break;
	    }
	}
    }


    private void
    lvalue(SimpleNode node) {

	lvalue(node, false, false);
    }


    private void
    lvalue(SimpleNode node, boolean disable, boolean modify) {

	SimpleNode  child;
	int         length;
	int         n;

	if (node != null && node.type() == LVALUE) {
	    length = node.length();
	    for (n = lvaluePrimary(node, disable, modify); n < length; n++) {
		child = node.getChild(n);
		switch (child.type()) {
		    case EXPRESSION:
			expression(child);
			break;

		    case FUNCTION:
			functionCall(child);
			break;
		}
	    }
	}
    }


    private int
    lvaluePrimary(SimpleNode node, boolean disable, boolean modify) {

	YoixParserBvalue  bvalue;
	YoixObject        lval;
	SimpleNode        child;
	String            name;
	int               count = 0;

	child = node.getChild(count++);

	switch (child.type()) {
	    case NAME:
		name = child.stringValue();
		switch (mode) {
		    case SYNC_MODE:
			compiler.syncLocalVariable(YoixBodyBlock.newLvalue(name), disable, synced, modify, modified, sbuf);
			break;

		    case BRANCH_MODE:
			if (compiler.isDisableAllReservedName(name))
			    compiler.disableAllBlockLocalVariables(true, sbuf);
			break;
		}
		break;

	    case BOUND_LVALUE:
		lval = YoixBodyBlock.newLvalue((YoixParserBvalue)child.getBvalue());
		switch (mode) {
		    case SYNC_MODE:
			compiler.syncLocalVariable(lval, disable, synced, modify, modified, sbuf);
			break;

		    case BRANCH_MODE:
			if ((name = YoixBodyBlock.getBlockName(lval)) == null)
			    name = lval.name();
			if (compiler.isDisableAllReservedName(name))
			    compiler.disableAllBlockLocalVariables(true, sbuf);
			break;

		}
		break;

	    case ADDRESS:
		lvalue(node.getChild(count++), disable, modify);
		if (mode == BRANCH_MODE) {
		    //
		    // If we take the address of a local variable then we disable
		    // local copies of all variables in that block, even though it
		    // might not always be required. This probably should duplicate
		    // what's current done in the compiler.
		    //
		    child = node.getChild1();
		    if (child.length() == 1) {
			child = child.getChild0();
			switch (child.type()) {
			    case NAME:
				name = child.stringValue();
				if (YoixBodyBlock.isLocalName(name)) {
				    if ((lval = YoixBodyBlock.newLvalue(name)) != null)
					compiler.disableBlockLocalVariables(lval, sbuf);
				} else if (YoixBodyBlock.isReservedName(name))
				    compiler.disableAllBlockLocalVariables(true, sbuf);
				break;

			    case BOUND_LVALUE:
				bvalue = (YoixParserBvalue)child.getBvalue();
				if (bvalue.getLevel() >= 0) {
				    if ((lval = YoixBodyBlock.newLvalue(bvalue)) != null)
					compiler.disableBlockLocalVariables(lval, sbuf);
				} else compiler.disableAllBlockLocalVariables(true, sbuf);
				break;
			}
		    }
		}
		break;

	    case INDIRECTION:
		child = node.getChild(count++);
		switch (child.type()) {
		    case EXPRESSION:
			expression(child);
			break;

		    case LVALUE:
			lvalue(child, disable, modify);
			break;
		}
		break;
	}
	return(count);
    }


    private void
    registerBranchNode(SimpleNode node) {

	if (mode == BRANCH_MODE)
	    branch_directory.put(node, Boolean.TRUE);
    }


    private void
    statement(SimpleNode node) {

	//
	// We eliminated cases that really have nothing to do, which means
	// there's no default case. Not completely convinced, so some or all
	// of them may reappear in a future release.
	//

	if (node != null) {
	    switch (node.type()) {
		case CASE:
		    expression(node.getChild0());
		    break;

		case COMPOUND:
		    statementCompound(node, 0);
		    break;

		case DECLARATION:
		    statementDeclaration(node);
		    break;

		case DO:
		    statementDo(node);
		    break;

		case EXIT:
		    expression(node.getChild0());
		    break;

		case EXPRESSION:
		    expression(node.getChild0());
		    break;

		case FINALLY:
		    statement(node.getChild0());
		    break;

		case FOR:
		    statementFor(node);
		    break;

		case FOREACH:
		    statementForEach(node);
		    break;

		case FUNCTION:
		    YoixBodyBlock.newDvalue(node.getChild0().stringValue());
		    break;

		case GLOBALBLOCK:
		case NAMEDBLOCK:
		case RESTRICTEDBLOCK:
		case THISBLOCK:
		    statementNamedBlock(node);
		    break;

		case IF:
		    statementIf(node);
		    break;

		case IMPORT:
		    statementImport(node);
		    break;

		case INCLUDE:
		    expression(node.getChild0());
		    break;

		case QUALIFIER:
		    statementQualifier(node);
		    break;

		case RETURN:
		    expression(node.getChild0());
		    break;

		case SAVE:
		    lvalue(node.getChild0());
		    expression(node.getChild1());
		    break;

		case STATEMENT:
		    statementList(node, 0, node.length());
		    break;

		case SWITCH:
		    statementSwitch(node);
		    break;

		case SYNCHRONIZED:
		    expression(node.getChild0());
		    statement(node.getChild1());
		    break;

		case TAGGED:
		    statement(node.getTaggedNode());
		    break;

		case TRY:
		    statement(node.getChild0());
		    break;

		case WHILE:
		    statementWhile(node);
		    break;
	    }
	}
    }


    private void
    statementCompound(SimpleNode node, int start) {

	yoixstack.pushLocalBlock(node.getLocalDict(), false);
	statementList(node, start, node.length() - 1);
	yoixstack.popBlock();
    }


    private void
    statementDeclaration(SimpleNode node) {

	String  typename;
	int     length;
	int     n;

	n = (node.getChild0().type() == NAME) ? 0 : 1;
	typename = node.getChild(n++).stringValue();

	for (length = node.length(); n < length; n++) {
	    declareVariable(
		typename,
		node.getChild(n).getChild0(),
		node.getChild(n).getChild1(),
		true
	    );
	}
    }


    private void
    statementDo(SimpleNode node) {

	registerBranchNode(node);
	statement(node.getChild0());
	expression(node.getChild1());
    }


    private void
    statementFor(SimpleNode node) {

	registerBranchNode(node);
	expression(node.getChild0());
	expression(node.getChild1());
	statement(node.getChild3());
	expression(node.getChild2());
    }


    private void
    statementForEach(SimpleNode node) {

	registerBranchNode(node);
	yoixstack.pushForEachBlock(node.getChild0().stringValue(), null);
	statement(node.getChild2());
	yoixstack.popBlock();
    }


    private void
    statementIf(SimpleNode node) {

	registerBranchNode(node);
	expression(node.getChild0());
	statement(node.getChild1());
	statement(node.getChild2());
    }


    private void
    statementImport(SimpleNode node) {

	if (node.getChild0().type() == LVALUE)
	    lvalue(node.getChild0());
    }


    private void
    statementInclude(SimpleNode node) {

	if (mode == BRANCH_MODE)
	    compiler.disableAllBlockLocalVariables(true, sbuf);
	expression(node.getChild0());
    }


    private void
    statementList(SimpleNode node, int start, int end) {

	int  n;

	for (n = start; n < end; n++)
	    statement(node.getChild(n));
    }


    private void
    statementNamedBlock(SimpleNode node) {

	SimpleNode  child;
	int         index;

	//
	// Eventually should do a bit more, like start a global block when
	// we're handling a GLOBALBLOCK. Not particularly important right
	// now, because I think the only bad thing that can happen is that
	// we sync more variables than are really needed.
	//

	if (mode == BRANCH_MODE)
	    compiler.disableAllBlockLocalVariables(true, sbuf);

	if ((child = node.getChild0()) != null && child.type() == LVALUE) {
	    lvalue(child);
	    index = 1;
	} else index = 0;

	statementList(node, index, node.length());
    }


    private void
    statementQualifier(SimpleNode node) {

	SimpleNode  expr;

	if ((expr = node.getChild2()) != null) {
	    lvalue(node.getChild1(), true, true);
	    expression(expr);
	} else lvalue(node.getChild1(), true, false);
    }


    private void
    statementSwitch(SimpleNode node) {

	registerBranchNode(node);
	expression(node.getChild0());
	statement(node.getChild1());
    }


    private void
    statementWhile(SimpleNode node) {

	registerBranchNode(node);
	expression(node.getChild0());
	statement(node.getChild1());
    }
}

