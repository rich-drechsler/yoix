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

abstract
class YoixBinder

    implements YoixConstants

{

    //
    // Parse tree binder that's currently only used by functions. This is
    // a first cut that was written quickly and doesn't pretend to handle
    // everything, but it definitely will be improved in future releases.
    // In fact, it's not hard to imagine lots of other optimizations that
    // may or may not be related to binding, so expect improvements and
    // changes in future releases.
    //
    // We currently don't bind when blocks use dictionaries for storage,
    // which means many initializers aren't bound. Some constructs, like
    // named blocks, probably can't get a thorough treatement, but there
    // should be no reason why initializers can't be handled.
    //
    // Main missing pieces are NEW case in expression() and DICTIONARY in
    // ivalue(). declareVariable() also needs work and and it seems like
    // using YoixBodyBlock.newDvalue() isn't always the right choice (we
    // probably just want a method that reserves a slot for a declaration.
    //
    // The binding code can be officially enabled or disabled using
    //
    //    VM.bind
    //
    // which probably is initialized to TRUE in YoixModuleVM.java. It may
    // also be the case that it's automatically enabled by the -O command
    // line option. As noted above this is a recent addition that probably
    // hasn't been as thoroughly tested as we would like, so we may decide
    // that the interperpter should disable it by default until we've had
    // more time for testing.
    //

    ///////////////////////////////////
    //
    // YoixBinder Methods
    //
    ///////////////////////////////////

    static void
    bind(SimpleNode node, YoixStack stack) {

	//
	// Currently only called to handle function bodies and the caller
	// is responsible for establishing the appropriate block structure
	// and for tearing it down when we return. Probably don't have to
	// synchronize, but don't think it will cause problems.
	//

	if (node != null) {
	    synchronized(node) {
		switch(node.type()) {
		    case EXPRESSION:
			expression(node, stack);
			break;

		    case COMPOUND:
		    case STATEMENT:
			statement(node, stack);
			break;
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static void
    declareVariable(SimpleNode dnode, SimpleNode inode, YoixStack stack) {

	SimpleNode  child;
	int         length;
	int         n;

	//
	// Suspect YoixBodyBlock.newDvalue() was causing some of problems
	// that we had with dictionaries. Also, ivalue() doesn't currently
	// handle dictionaries, but that's something we'll address before
	// too long.
	//

	YoixBodyBlock.newDvalue(dnode.getChild0().stringValue());
	ivalue(inode, stack);

	if ((child = dnode.getChild1()) != null) {
	    switch (child.type()) {
		case EXPRESSION:
		    expression(child, stack);
		    break;

		case RANGE:
		    if ((length = child.length()) > 0) {
			for (n = 0; n < length; n++)
			    expression(child.getChild(n), stack);
		    }
		    break;
	    }
	}
    }


    private static void
    expression(SimpleNode node, YoixStack stack) {

	SimpleNode  child;
	int         length;
	int         op;
	int         n;

	//
	// The CONSTRUCTOR case eventually needs to be filled in - see the
	// corresponding code in YoixInterpreter.java for more details. We
	// skipped it because this code was written quickly and we had some
	// trouble when a dictionary was used for block storage.
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
			ivalue(child, stack);
			break;

		    case CONDITIONAL:
			expression(child.getChild0(), stack);
			n++;
			break;

		    case EXPRESSION:
			expression(child, stack);
			break;

		    case LVALUE:
			lvalue(child, stack);
			break;

		    case QUESTIONCOLON:
			expression(child.getChild0(), stack);
			expression(child.getChild1(), stack);
			expression(child.getChild2(), stack);
			break;

		    case ATTRIBUTE:
			lvalue(child.getChild0(), stack);
			break;

		    case NEW:
			//
			// This eventually should be filled in!!
			//
			break;
		}
	    }
	}
    }


    private static void
    functionCall(SimpleNode args, YoixStack stack) {

	int  argc;
	int  n;

	argc = args.length();

	for (n = 0; n < argc; n++)
	    expression(args.getChild(n), stack);
    }


    private static void
    ivalue(SimpleNode node, YoixStack stack) {

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
			expression(node.getChild(n), stack);
		    break;

		case DICTIONARY:
		    //
		    // This eventually should be filled in!!
		    //
		    break;

		case EXPRESSION:
		    expression(node.getChild0(), stack);
		    break;
	    }
	}
    }


    private static void
    lvalue(SimpleNode node, YoixStack stack) {

	SimpleNode  child;
	int         length;
	int         n;

	if (node != null && node.type() == LVALUE) {
	    length = node.length();
	    for (n = lvaluePrimary(node, stack); n < length; n++) {
		child = node.getChild(n);
		switch (child.type()) {
		    case EXPRESSION:
			expression(child, stack);
			break;

		    case FUNCTION:
			functionCall(child, stack);
			break;
		}
	    }
	}
    }


    private static int
    lvaluePrimary(SimpleNode node, YoixStack stack) {

	SimpleNode  child;
	SimpleNode  bvalue;
	int         count = 0;

	child = node.getChild(count++);

	switch (child.type()) {
	    case NAME:
		if ((bvalue = YoixBodyBlock.newBoundLvalue(child.stringValue())) != null)
		    node.jjtAddChild(bvalue, 0);
		break;

	    case BOUND_LVALUE:
		//
		// Nothing to do here, but it means someone else has been
		// here. Leave it in for now in case we want to add some
		// debugging code.
		//
		break;

	    case ADDRESS:
		lvalue(node.getChild(count++), stack);
		break;

	    case INDIRECTION:
		child = node.getChild(count++);
		switch (child.type()) {
		    case EXPRESSION:
			expression(child, stack);
			break;

		    case LVALUE:
			lvalue(child, stack);
			break;
		}
		break;
	}
	return(count);
    }


    private static void
    statement(SimpleNode node, YoixStack stack) {

	//
	// We eliminated cases that really have nothing to do, which means
	// there's no default case. Not completely convinced, so some or all
	// of them may reappear in a future release.
	//

	if (node != null) {
	    switch (node.type()) {
		case COMPOUND:
		    statementCompound(node, 0, stack);
		    break;

		case DECLARATION:
		    statementDeclaration(node, stack);
		    break;

		case DO:
		    statement(node.getChild0(), stack);
		    expression(node.getChild1(), stack);
		    break;

		case EXIT:
		    expression(node.getChild0(), stack);
		    break;

		case EXPRESSION:
		    expression(node.getChild0(), stack);
		    break;

		case FINALLY:
		    //
		    // Handling this probably would be complicated so we'll
		    // skip it for now.
		    //
		    break;

		case FOR:
		    expression(node.getChild0(), stack);
		    expression(node.getChild1(), stack);
		    statement(node.getChild3(), stack);
		    expression(node.getChild2(), stack);
		    break;

		case FOREACH:
		    stack.pushForEachBlock(node.getChild0().stringValue(), null);
		    statement(node.getChild2(), stack);
		    stack.popBlock();
		    break;

		case FUNCTION:
		    YoixBodyBlock.newDvalue(node.getChild0().stringValue());
		    break;

		case GLOBALBLOCK:
		case NAMEDBLOCK:
		case RESTRICTEDBLOCK:
		case THISBLOCK:
		    //
		    // Could do more here - start a new global block for
		    // binding, then bind statements in the named block.
		    //
		    lvalue(node.getChild0(), stack);
		    break;

		case IF:
		    expression(node.getChild0(), stack);
		    statement(node.getChild1(), stack);
		    statement(node.getChild2(), stack);
		    break;

		case IMPORT:
		    //
		    // Technically should call lvalue() here.
		    //
		    break;

		case INCLUDE:
		    expression(node.getChild0(), stack);
		    break;

		case QUALIFIER:
		    lvalue(node.getChild1(), stack);
		    expression(node.getChild2(), stack);
		    break;

		case RETURN:
		    expression(node.getChild0(), stack);
		    break;

		case SAVE:
		    lvalue(node.getChild0(), stack);
		    expression(node.getChild1(), stack);
		    break;

		case STATEMENT:
		    statementList(node, 0, node.length(), stack);
		    break;

		case SWITCH:
		    expression(node.getChild0(), stack);
		    statement(node.getChild1(), stack);
		    break;

		case SYNCHRONIZED:
		    expression(node.getChild0(), stack);
		    statement(node.getChild1(), stack);
		    break;

		case TAGGED:
		    statement(node.getTaggedNode(), stack);
		    break;

		case TRY:
		    statement(node.getChild0(), stack);
		    break;

		case WHILE:
		    expression(node.getChild0(), stack);
		    statement(node.getChild1(), stack);
		    break;
	    }
	}
    }


    private static void
    statementCompound(SimpleNode node, int start, YoixStack stack) {

	stack.pushLocalBlock(node.getLocalDict(), false);
	statementList(node, start, node.length() - 1, stack);
	stack.popBlock();
    }


    private static void
    statementDeclaration(SimpleNode node, YoixStack stack) {

	int  length;
	int  n;

	n = (node.getChild0().type() == NAME) ? 1 : 2;

	for (length = node.length(); n < length; n++) {
	    declareVariable(
		node.getChild(n).getChild0(),
		node.getChild(n).getChild1(),
		stack
	    );
	}
    }


    private static void
    statementList(SimpleNode node, int start, int end, YoixStack stack) {

	int  n;

	for (n = start; n < end; n++)
	    statement(node.getChild(n), stack);
    }
}

