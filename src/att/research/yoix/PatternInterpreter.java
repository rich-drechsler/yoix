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
class PatternInterpreter

    implements YoixConstants

{

    //
    // A first cut at an interpreter for pattern matching operations that
    // are described in a parse tree that was built by PatternParser. Much
    // of this was borrowed from the yoix interpreter, so there's probably
    // room for simplification - later.
    //
    // NOTE - names of Yoix and pattern parser constants collide but their
    // values don't, so be careful!! That's also why we couldn't implement
    // both YoixParserConstants and PatternParserConstants.
    //
    // NOTE - the actual matching is handled in matchValues(), so that may
    // be where to start looking if you decide to change things.
    //

    ///////////////////////////////////
    //
    // PatternInterpreter Methods
    //
    ///////////////////////////////////

    static boolean
    match(SimpleNode pattern, String arg, int flags) { 

	YoixStack  stack = VM.getThreadStack();

	matchPattern(pattern, arg, flags|SINGLE_BYTE, stack);
	return(stack.popYoixObject().booleanValue());
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static int
    conditional(SimpleNode node, int n, String arg, int flags, YoixStack stack) {

	SimpleNode  cond;
	YoixObject  expr;

	cond = node.getChild(n);

	if (cond.type() == PatternParserConstants.CONDITIONAL) {
	    expr = stack.popRvalue();
	    switch (node.getChild(++n).type()) {
		case PatternParserConstants.AND:
		    stack.pushInt(expr.booleanValue() && match(cond.getChild0(), arg, flags));
		    break;

		case PatternParserConstants.OR:
		    stack.pushInt(expr.booleanValue() || match(cond.getChild0(), arg, flags));
		    break;

		default:
		    VM.abort(INTERNALERROR);
		    break;
	    }
	} else VM.abort(INTERNALERROR);

	return(n);
    }


    private static void
    matchPattern(SimpleNode node, String arg, int flags, YoixStack stack) {

	SimpleNode  child;
	int         length;
	int         op;
	int         n;

	if (arg != null && node != null) {
	    if (node.type() == PatternParserConstants.PATTERN) {
		if ((length = node.length()) > 0) {
		    for (n = 0; n < length; n++) {
			child = node.getChild(n);
			switch (op = child.type()) {
			    case PatternParserConstants.CONDITIONAL:
				n = conditional(node, n, arg, flags, stack);
				break;

			    case PatternParserConstants.NOT:
				stack.pushInt(!stack.popRvalue().booleanValue());
				break;

			    case PatternParserConstants.PATTERN:
				matchPattern(child, arg, flags, stack);
				break;

			    case PatternParserConstants.XOR:
				stack.pushInt(stack.popRvalue().booleanValue() ^ stack.popRvalue().booleanValue());
				break;

			    default:
				if (child instanceof YoixObject)
				    stack.pushInt(matchValues((YoixObject)child, arg, flags, stack));
				else VM.abort(INTERNALERROR);
				break;
			}
		    }
		} else stack.pushInt(false);
	    } else stack.pushInt(false);
	} else stack.pushInt(false);
    }


    private static boolean
    matchValues(YoixObject obj, String arg, int flags, YoixStack stack) {

	YoixRERegexp  regexp;
	boolean       result;
	String        pattern;
	byte          source[];
	byte          values[];
	int           length;
	int           n;

	//
	// Some of this, particularly the matching triggered when obj is
	// a string and DOT_PATTERN is set, may be convenient, but it was
	// added primarily to support an existing internal project. You
	// should be able to use regular expressions to accomplish the
	// same thing.
	//

	if (obj != null && arg != null) {
	    if (obj.isString()) {
		pattern = obj.stringValue();
		if ((flags & DOT_PATTERN) != 0) {
		    if ((length = arg.length()) == pattern.length()) {
			result = true;
			if ((flags & CASE_INSENSITIVE) != 0) {
			    arg = arg.toLowerCase();
			    pattern = pattern.toLowerCase();
			}
			source = YoixMake.javaByteArray(arg);
			values = YoixMake.javaByteArray(pattern);
			for (n = 0; n < length; n++) {
			    if (source[n] != values[n] && values[n] != '.') {
				result = false;
				break;
			    }
			}
		    } else result = false;
		} else if ((flags & CASE_INSENSITIVE) != 0)
		    result = arg.equalsIgnoreCase(pattern);
		else result = arg.equals(pattern);
	    } else if (obj.isRegexp()) {
		//
		// Important to check and set the type first. Also note
		// that flags currently always has SINGLE_BYTE set.
		//
		if (obj.getInt(N_TYPE, SINGLE_BYTE) != (flags&REFLAGS_MASK))
		    obj.putInt(N_TYPE, flags&REFLAGS_MASK);
		if ((regexp = (YoixRERegexp)obj.getManagedObject()) != null)
		    result = regexp.exec(arg, null);
		else result = false;
	    } else result = false;
	} else result = false;

	return(result);
    }
}

