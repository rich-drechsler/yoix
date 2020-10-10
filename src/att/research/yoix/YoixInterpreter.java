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

public abstract
class YoixInterpreter

    implements YoixAPI,
	       YoixConstants

{

    //
    // The parse tree interpreter - each thread gets it's own stack,
    // so we don't have to worry much about synchronization here.
    //

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public static boolean
    booleanValue(YoixObject obj) {

	boolean  bool;

	//
	// Really belongs in YoixObject, but the current implementation
	// is a bit too restrictive for our purposes. Eventually try to
	// move it, but it will require careful testing. Best guess is
	// this version, even with the isEmpty() test, can replace the
	// existing implementation - much later!!
	//

	switch (obj.major()) {
	    case CALLABLE:
		bool = !obj.isNull();
		break;

	    case NUMBER:
		bool = (obj.doubleValue() != 0);
		break;

	    case POINTER:
		bool = !obj.isNull();
		break;

	    default:
		bool = obj.isEmpty();		// used by for statements
		break;
	}

	return(bool);
    }


    public static boolean
    getBoolean(String name) {

	return(getBoolean(name, false));
    }


    public static boolean
    getBoolean(String name, boolean fail) {

	YoixObject  obj;

	return((obj = getObject(name)) != null ? obj.booleanValue() : fail);
    }


    public static double
    getDouble(String name, double fail) {

	YoixObject  obj;

	return((obj = getObject(name)) != null ? obj.doubleValue() : fail);
    }


    public static int
    getInt(String name, int fail) {

	YoixObject  obj;

	return((obj = getObject(name)) != null ? obj.intValue() : fail);
    }


    public static YoixObject
    getLvalue(String name) {

	return(YoixBodyBlock.getLvalue(name));
    }


    public static YoixObject
    getObject(String name) {

	YoixObject  lval;

	//
	// Another method so Java code written for custom modules can look
	// through the active blocks the exactly way a Yoix script does.
	//

	return((lval = YoixBodyBlock.getLvalue(name)) != null ? lval.getObject() : null);
    }


    public static String
    getString(String name) {

	YoixObject  obj;

	return((obj = getObject(name)) != null ? obj.stringValue() : null);
    }


    public static boolean
    isEqualEQ(YoixObject left, YoixObject right) {

	return(left != null && right != null && equalsEQ(left, right));
    }


    public static boolean
    isEqualEQEQ(YoixObject left, YoixObject right) {

	return(left != null && right != null && equalsEQEQ(left, right));
    }


    public static void
    putDouble(String name, double value) {

	YoixBodyBlock.getLvalue(name).put(YoixObject.newDouble(value));
    }


    public static void
    putInt(String name, int value) {

	YoixBodyBlock.getLvalue(name).put(YoixObject.newInt(value));
    }


    public static void
    putInt(String name, boolean value) {

	YoixBodyBlock.getLvalue(name).put(YoixObject.newInt(value));
    }


    public static void
    putObject(String name, YoixObject value) {

	if (value != null)
	    YoixBodyBlock.getLvalue(name).put(value);
    }


    public static void
    putString(String name, String value) {

	YoixBodyBlock.getLvalue(name).put(YoixObject.newString(value));
    }

    ///////////////////////////////////
    //
    // YoixInterpreter Methods
    //
    ///////////////////////////////////

    static YoixObject
    call(SimpleNode node, YoixStack stack) {

	YoixObject  result = null;
	YoixError   return_point = null;

	try {
	    return_point = stack.pushReturn();
	    statement(node, stack);
	}
	catch(Error e) {
	    if (e == return_point)
		result = stack.popRvalue();
	    else throw(e);
	}
	return(result);
    }


    static String
    caseLabel(YoixObject obj) {

	String  str;

	if (obj != null) {
	    if (obj.isNumber())
		str = "A" + obj.doubleValue();
	    else if (obj.isNull())
		str = "Bnull";
	    else if (obj.isString())
		str = "C" + obj.stringValue();
	    else str = null;
	} else str = "D";

	return(str);
    }


    static String
    caseLabelValue(YoixObject obj) {

	String  str;

	if (obj != null) {
	    if (obj.isNumber())
		str = obj.doubleValue() + "";
	    else if (obj.isNull())
		str = "null";
	    else if (obj.isString())
		str = obj.stringValue();
	    else str = null;
	} else str = "default";

	return(str);
    }


    static void
    declareVariable(YoixObject lval, String typename, SimpleNode dnode, SimpleNode inode, YoixStack stack) {

	YoixObject  ival;
	YoixObject  tags;
	YoixObject  expr;
	SimpleNode  child;
	boolean     adjust = false;
	int         length;
	int         limits[] = {-1, -2};
	int         n;

	ivalue(inode, stack);
	tags = stack.popYoixObject();
	ival = stack.popRvalueClone();

	if ((child = dnode.getChild1()) != null) {
	    switch (child.type()) {
		case EXPRESSION:
		    expr = evaluate(child, stack);
		    if (expr.isInteger() && expr.intValue() >= 0)
			limits[0] = expr.intValue();
		    else VM.abort(BADDECLARATION);
		    break;

		case RANGE:
		    if ((length = child.length()) > 0) {
			limits[0] = 0;
			limits[1] = -1;
			for (n = 0; n < length; n++) {
			    expr = evaluate(child.getChild(n), stack);
			    if (expr.isInteger() && expr.intValue() >= 0) {
				if (n < limits.length)
				    limits[n] = expr.intValue();
				else VM.abort(UNIMPLEMENTED);
			    } else VM.abort(BADDECLARATION);
			}
		    } else if (ival != null) {
			if (ival.notEmpty()) {
			    //
			    // This is a recent change (6/27/01) and undoubtedly
			    // needs a closer look. Old version used ival.length()
			    // for everything, but ival.sizeof() seems approparite
			    // for strings etc. and maybe for everything. We may
			    // decide to pick one (or the other), but copyInto()
			    // methods in YoixMisc also need close look.
			    //
			    limits[0] = ival.compound() ? ival.length() : ival.sizeof();
			    adjust = true;
			} else ival = YoixObject.newNull();
		    } else VM.abort(BADDECLARATION);
		    break;

		default:
		    VM.die(INTERNALERROR);
		    break;
	    }
	}

	lval.declare(YoixMake.yoixType(typename, limits[0], limits[1], ival, tags, adjust));
    }


    static boolean
    equals(YoixObject left, YoixObject right, int operator) {

	YoixStack  stack = VM.getThreadStack();

	stack.pushYoixObjectClone(left);
	stack.pushYoixObjectClone(right);
	expressionRelational(operator, stack);
	return(booleanValue(stack.popYoixObject()));
    }


    static boolean
    equalsEQ(YoixObject left, YoixObject right) {

	YoixStack  stack = VM.getThreadStack();

	stack.pushYoixObjectClone(left);
	stack.pushYoixObjectClone(right);
	expressionRelational(EQ, stack);
	return(booleanValue(stack.popYoixObject()));
    }


    static boolean
    equalsEQEQ(YoixObject left, YoixObject right) {

	YoixStack  stack = VM.getThreadStack();

	stack.pushYoixObjectClone(left);
	stack.pushYoixObjectClone(right);
	expressionRelational(EQEQ, stack);
	return(booleanValue(stack.popYoixObject()));
    }


    static boolean
    equalsEQEQ(YoixObject left[], YoixObject right[]) {

	YoixStack  stack = VM.getThreadStack();
	boolean    result;
	int        length;
	int        n;

	if (left != right) {
	    if (left != null && right != null) {
		if (left.length == right.length) {
		    result = true;
		    for (n = 0; n < left.length && result; n++) {
			if (left[n] != right[n]) {
			    if (left[n] != null && right[n] != null) {
				stack.pushYoixObjectClone(left[n]);
				stack.pushYoixObjectClone(right[n]);
				expressionRelational(EQEQ, stack);
				result = booleanValue(stack.popYoixObject());
			    } else result = false;
			}
		    }
		} else result = false;
	    } else result = false;
	} else result = true;

	return(result);
    }


    static YoixObject
    evaluate(SimpleNode node, YoixStack stack) {

	expression(node, stack);
	return(stack.popRvalue());
    }


    static boolean
    evaluateBoolean(SimpleNode node, YoixStack stack) {

	return(booleanValue(evaluate(node, stack)));
    }


    static YoixObject
    evaluateDecrement(YoixObject obj) {

	if (obj.isInteger())
	    obj = YoixObject.newInt(obj.intValue() - 1);
	else if (obj.isNumber())
	    obj = YoixObject.newDouble(obj.doubleValue() - 1);
	else if (obj.isPointer())
	    obj = YoixObject.newLvalue(obj, obj.offset() - 1);
	else VM.abort(BADOPERAND);
	return(obj);
    }


    static YoixObject
    evaluateIncrement(YoixObject obj) {

	if (obj.isInteger())
	    obj = YoixObject.newInt(obj.intValue() + 1);
	else if (obj.isNumber())
	    obj = YoixObject.newDouble(obj.doubleValue() + 1);
	else if (obj.isPointer())
	    obj = YoixObject.newLvalue(obj, obj.offset() + 1); 
	else VM.abort(BADOPERAND);
	return(obj);
    }


    static void
    expression(SimpleNode node, YoixStack stack) {

	SimpleNode  child;
	int         length;
	int         op;
	int         n;

	if ((length = node.length()) > 0) {
	    for (n = 0; n < length; n++) {
		child = node.getChild(n);
		switch (op = child.type()) {
		    case ARRAY:
		    case DICTIONARY:
			expressionInitializer(child, stack);
			break;

		    case CAST:
			expressionCast(stack);
			break;

		    case CONDITIONAL:
			expressionConditional(node.getChild(++n).type(), child, stack);
			break;

		    case EXPRESSION:
			expression(child, stack);
			break;

		    case LVALUE:
			lvalue(child, stack);
			break;

		    case NAME:
			stack.pushString(child.stringValue());
			break;

		    case NEW:
			expressionNew(child, stack);
			break;

		    case NUMBER:
			stack.pushYoixObject((YoixObject)child);
			break;

		    case POINTER:
			stack.pushYoixObjectClone((YoixObject)child);
			break;

		    case QUESTIONCOLON:
			if (evaluateBoolean(child.getChild0(), stack))
			    expression(child.getChild1(), stack);
			else expression(child.getChild2(), stack);
			break;

		    case ATTRIBUTE:
			expressionAttribute(child, stack);
			break;

		    case UPLUS:
		    case UMINUS:
		    case COMPLEMENT:
		    case NOT:
			expressionUnary(op, stack);
			break;

		    case POSTDECREMENT:
			expressionPostDecrement(stack);
			break;

		    case POSTINCREMENT:
			expressionPostIncrement(stack);
			break;

		    case PREDECREMENT:
			expressionPreDecrement(stack);
			break;

		    case PREINCREMENT:
			expressionPreIncrement(stack);
			break;

		    case PLUS:
		    case MINUS:
		    case MUL:
		    case DIV:
		    case MOD:
			expressionArithmetic(op, stack);
			break;

		    case PLUSEQ:
		    case MINUSEQ:
		    case MULEQ:
		    case DIVEQ:
		    case MODEQ:
			stack.duplicateExchange(1);
			expressionArithmetic(op, stack);
			expressionAssign(stack);
			break;

		    case LEFTSHIFT:
		    case RIGHTSHIFT:
		    case UNSIGNEDSHIFT:
			expressionShift(op, stack);
			break;

		    case LEFTSHIFTEQ:
		    case RIGHTSHIFTEQ:
		    case UNSIGNEDSHIFTEQ:
			stack.duplicateExchange(1);
			expressionShift(op, stack);
			expressionAssign(stack);
			break;

		    case AND:
		    case OR:
		    case XOR:
			expressionBitwise(op, stack);
			break;

		    case ANDEQ:
		    case OREQ:
		    case XOREQ:
			stack.duplicateExchange(1);
			expressionBitwise(op, stack);
			expressionAssign(stack);
			break;

		    case LT:
		    case GT:
		    case LE:
		    case GE:
		    case EQ:
		    case NE:
		    case EQEQ:
		    case NEEQ:
			expressionRelational(op, stack);
			break;

		    case EQTILDA:
		    case NETILDA:
			expressionRERelational(op, stack);
			break;

		    case INSTANCEOF:
			expressionInstanceof(stack);
			break;

		    case LOGICALXOR:
			stack.pushInt(stack.popRvalue().booleanValue() ^ stack.popRvalue().booleanValue());
			break;

		    case ASSIGN:
			expressionAssign(stack);
			break;

		    case COMMA:
			stack.collapse();
			break;

		    default:
			VM.die(INTERNALERROR);
			break;
		}
	    }
	} else stack.pushEmpty();
    }


    static void
    expressionArithmetic(int op, YoixStack stack) {

	YoixObject  left;
	YoixObject  right;

	right = stack.popRvalue();
	left = stack.popRvalue();

	if (right.isNumber() && left.isNumber()) {
	    if (left.isInteger() && right.isInteger()) {
		switch (op) {
		    case PLUS:
		    case PLUSEQ:
			stack.pushInt(left.intValue() + right.intValue());
			break;

		    case MINUS:
		    case MINUSEQ:
			stack.pushInt(left.intValue() - right.intValue());
			break;

		    case MUL:
		    case MULEQ:
			stack.pushInt(left.intValue() * right.intValue());
			break;

		    case DIV:
		    case DIVEQ:
			if (right.intValue() != 0)
			    stack.pushInt(left.intValue() / right.intValue());
			else VM.abort(BADOPERAND);
			break;

		    case MOD:
		    case MODEQ:
			if (right.intValue() != 0)
			    stack.pushInt(left.intValue() % right.intValue());
			else VM.abort(BADOPERAND);
			break;
		}
	    } else {
		switch (op) {
		    case PLUS:
		    case PLUSEQ:
			stack.pushDouble(left.doubleValue() + right.doubleValue());
			break;

		    case MINUS:
		    case MINUSEQ:
			stack.pushDouble(left.doubleValue() - right.doubleValue());
			break;

		    case MUL:
		    case MULEQ:
			stack.pushDouble(left.doubleValue() * right.doubleValue());
			break;

		    case DIV:
		    case DIVEQ:
			stack.pushDouble(left.doubleValue() / right.doubleValue());
			break;

		    case MOD:
		    case MODEQ:
			stack.pushDouble(left.doubleValue() % right.doubleValue());
			break;
		}
	    }
	} else if (left.isPointer() && right.isInteger()) {
	    switch (op) {
		case PLUS:
		case PLUSEQ:
		    stack.pushLvalue(left, left.offset() + right.intValue());
		    break;

		case MINUS:
		case MINUSEQ:
		    stack.pushLvalue(left, left.offset() - right.intValue());
		    break;

		default:
		    VM.abort(BADOPERAND);
		    break;
	    }
	} else if (left.isInteger() && right.isPointer()) {
	    switch (op) {
		case PLUS:
		case PLUSEQ:
		    stack.pushLvalue(right, right.offset() + left.intValue());
		    break;

		case MINUS:
		case MINUSEQ:
		    stack.pushLvalue(right, right.offset() - left.intValue());
		    break;

		default:
		    VM.abort(BADOPERAND);
		    break;
	    }
	} else if (left.isPointer() && right.isPointer()) {
	    switch (op) {
		case PLUS:
		case PLUSEQ:
		    if (left.isString() && right.isString())
			stack.pushString(left.stringValue() + right.stringValue());
		    else if (left.isString() && right.isNull())
			stack.pushString(left.stringValue());
		    else if (left.isNull() && right.isString())
			stack.pushString(right.stringValue());
		    else VM.abort(BADOPERAND);
		    break;

		case MINUS:
		case MINUSEQ:
		    if (left.body() == right.body())
			stack.pushInt(left.offset() - right.offset());
		    else VM.abort(BADOPERAND);
		    break;

		default:
		    VM.abort(BADOPERAND);
		    break;
	    }
	} else VM.abort(BADOPERAND);
    }


    static void
    expressionAssign(YoixStack stack) {

	YoixObject  left;
	YoixObject  right;

	//
	// Result matches C, so it's the value stored in the left hand
	// side after the assignment.
	//

	right = stack.popRvalue();
	left = stack.popYoixObject();

	if (left.isPointer() && !right.isEmpty())
	    stack.pushYoixObjectClone(left.put(left.offset(), right, true));
	else VM.abort(BADOPERAND);
    }


    static void
    expressionAttribute(SimpleNode node, YoixStack stack) {

	SimpleNode  child;
	YoixObject  left;
	String      name;
	int         mode;

	child = node.getChild0();
	if (child.length() != 1 || child.getChild0().type() != NAME) {
	    lvalue(child, stack);
	    left = stack.popYoixObject();
	} else left = YoixBodyBlock.newLvalue(child.getChild0().stringValue());

	switch (node.getChild1().intValue()) {
	    case ATTRIBUTE_ACCESS:
		VM.pushAccess(R__);
		left = left.resolve();
		VM.popAccess();
		stack.pushInt((left.getAccessBody() << 4) | left.getAccess());
		break;

	    case ATTRIBUTE_GROWABLE:
		left = left.resolve();
		stack.pushInt(left.canGrowTo(left.length() + 1));
		break;

	    case ATTRIBUTE_LENGTH:
		stack.pushInt(left.resolve().length());
		break;

	    case ATTRIBUTE_MAJOR:
		stack.pushInt(left.resolve().major());
		break;

	    case ATTRIBUTE_MINOR:
		stack.pushInt(left.resolve().minor());
		break;

	    case ATTRIBUTE_NAMEOF:
		if (left.compound() == false) {
		    if ((name = YoixBodyBlock.getBlockName(left)) == null)
			name = left.name();
		} else name = left.name();
		stack.pushString(name);
		break;

	    case ATTRIBUTE_OFFSET:
		stack.pushInt(left.resolve().offset());
		break;

	    case ATTRIBUTE_SIZEOF:
		stack.pushInt(left.resolve().sizeof());
		break;

	    case ATTRIBUTE_TYPENAME:
		VM.pushAccess(R__);
		left = left.resolve();
		VM.popAccess();
		stack.pushString(VM.getTypename(left));
		break;

	    default:
		VM.die(INTERNALERROR);
		break;
	}
    }


    static void
    expressionBitwise(int op, YoixStack stack) {

	YoixObject  left;
	YoixObject  right;

	right = stack.popRvalue();
	left = stack.popRvalue();

	if (right.isNumber() && left.isNumber()) {
	    if (left.isInteger() && right.isInteger()) {
		switch (op) {
		    case AND:
		    case ANDEQ:
			stack.pushInt(left.intValue() & right.intValue());
			break;

		    case OR:
		    case OREQ:
			stack.pushInt(left.intValue() | right.intValue());
			break;

		    case XOR:
		    case XOREQ:
			stack.pushInt(left.intValue() ^ right.intValue());
			break;
		}
	    } else {
		switch (op) {
		    case AND:
		    case ANDEQ:
			stack.pushDouble(left.longValue() & right.longValue());
			break;

		    case OR:
		    case OREQ:
			stack.pushDouble(left.longValue() | right.longValue());
			break;

		    case XOR:
		    case XOREQ:
			stack.pushDouble(left.longValue() ^ right.longValue());
			break;
		}
	    }
	} else VM.abort(BADOPERAND);
    }


    static void
    expressionCast(YoixStack stack) {

	YoixObject  left;
	YoixObject  right;
	YoixObject  dest;
	YoixObject  result;

	right = stack.popRvalue();
	left = stack.popYoixObject();

	if ((dest = YoixMake.yoixInstance(left.stringValue())) != null) {
	    if ((result = right.cast(dest, false)) != null)
		stack.pushYoixObjectClone(result);
	    else VM.abort(TYPECHECK);
	} else VM.abort(TYPECHECK);
    }


    static void
    expressionConditional(int op, SimpleNode cond, YoixStack stack) {

	switch (op) {
	    case LOGICALAND:
		stack.pushInt(booleanValue(stack.popRvalue()) && evaluateBoolean(cond.getChild0(), stack));
		break;

	    case LOGICALOR:
		stack.pushInt(booleanValue(stack.popRvalue()) || evaluateBoolean(cond.getChild0(), stack));
		break;

	    default:
		VM.die(INTERNALERROR);
		break;
	}
    }


    static void
    expressionInitializer(SimpleNode node, YoixStack stack) {

	YoixObject  ival;
	YoixObject  tags;

	//
	// Initializers are should all be handled by declareVariable(), so
	// this method probably will never be called.
	//

	ivalue(node, stack);
	tags = stack.popYoixObject();
	ival = stack.popRvalueClone();
	stack.pushYoixObjectClone(ival);
    }


    static void
    expressionInstanceof(YoixStack stack) {

	YoixObject  left;
	YoixObject  right;
	YoixObject  dest;
	boolean     result;

	//
	// We get dest first, even when left is null, so the appearance
	// of BADTYPENAME error messages is consistent.
	//

	result = false;
	right = stack.popYoixObject();
	left = stack.popRvalue();

	if ((dest = YoixMake.yoixInstance(right.stringValue())) != null) {
	    if (left.notNull())
		result = (left.cast(dest, false) == left);
	}

	stack.pushInt(result);
    }


    static void
    expressionNew(SimpleNode node, YoixStack stack) {

	YoixObject  lval;
	SimpleNode  dnode;
	SimpleNode  inode;

	lval = YoixObject.newArray(1);
	dnode = node.getChild0();
	inode = node.getChild1();
	declareVariable(lval, dnode.getChild0().stringValue(), dnode, inode, stack);
	stack.pushYoixObject(lval.getObject(0));
    }


    static void
    expressionPostDecrement(YoixStack stack) {

	YoixObject  lval;
	YoixObject  result;

	lval = stack.popYoixObject();
	result = lval.resolveClone();
	lval.put(lval.offset(), evaluateDecrement(result), false);
	stack.pushYoixObject(result);
    }


    static void
    expressionPostIncrement(YoixStack stack) {

	YoixObject  lval;
	YoixObject  result;

	lval = stack.popYoixObject();
	result = lval.resolveClone();
	lval.put(lval.offset(), evaluateIncrement(result), false);
	stack.pushYoixObject(result);
    }


    static void
    expressionPreDecrement(YoixStack stack) {

	YoixObject  lval;
	YoixObject  result;

	lval = stack.popYoixObject();
	result = evaluateDecrement(lval.resolveClone());
	lval.put(lval.offset(), result, false);
	stack.pushYoixObject(result);
    }


    static void
    expressionPreIncrement(YoixStack stack) {

	YoixObject  lval;
	YoixObject  result;

	lval = stack.popYoixObject();
	result = evaluateIncrement(lval.resolveClone());
	lval.put(lval.offset(), result, false);
	stack.pushYoixObject(result);
    }


    static void
    expressionRelational(int op, YoixStack stack) {

	YoixBodyParseTree  ltree;
	YoixBodyParseTree  rtree;
	YoixObject         left;
	YoixObject         right;
	boolean            match;

	right = stack.popRvalue();
	left = stack.popRvalue();

	if (left.isNumber() && right.isNumber()) {
	    if (left.isInteger() && right.isInteger()) {
		switch (op) {
		    case LT:
			stack.pushInt(left.intValue() < right.intValue());
			break;

		    case GT:
			stack.pushInt(left.intValue() > right.intValue());
			break;

		    case LE:
			stack.pushInt(left.intValue() <= right.intValue());
			break;

		    case GE:
			stack.pushInt(left.intValue() >= right.intValue());
			break;

		    case EQ:
		    case EQEQ:
			stack.pushInt(left.intValue() == right.intValue());
			break;

		    case NE:
		    case NEEQ:
			stack.pushInt(left.intValue() != right.intValue());
			break;
		}
	    } else {
		switch (op) {
		    case LT:
			stack.pushInt(left.doubleValue() < right.doubleValue());
			break;

		    case GT:
			stack.pushInt(left.doubleValue() > right.doubleValue());
			break;

		    case LE:
			stack.pushInt(left.doubleValue() <= right.doubleValue());
			break;

		    case GE:
			stack.pushInt(left.doubleValue() >= right.doubleValue());
			break;

		    case EQ:
			stack.pushInt(left.doubleValue() == right.doubleValue());
			break;

		    case EQEQ:
			if (left.isNaN() && right.isNaN())
			    stack.pushInt(true);
			else stack.pushInt(left.doubleValue() == right.doubleValue());
			break;

		    case NE:
			stack.pushInt(left.doubleValue() != right.doubleValue());
			break;

		    case NEEQ:
			if (left.isNaN() && right.isNaN())
			    stack.pushInt(false);
			else stack.pushInt(left.doubleValue() != right.doubleValue());
			break;
		}
	    }
	} else if (left.isPointer() && right.isPointer()) {
	    if (left.isNull() || right.isNull()) {
		switch (op) {
		    case LT:
			stack.pushInt(left.isNull() && !right.isNull());
			break;

		    case GT:
			stack.pushInt(!left.isNull() && right.isNull());
			break;

		    case LE:
			stack.pushInt(left.isNull());
			break;

		    case GE:
			stack.pushInt(right.isNull());
			break;

		    case EQ:
		    case EQEQ:
			stack.pushInt(left.isNull() && right.isNull());
			break;

		    case NE:
		    case NEEQ:
			stack.pushInt(!left.isNull() || !right.isNull());
			break;
		}
	    } else if (left.body() == right.body()) {
		switch (op) {
		    case LT:
			stack.pushInt(left.offset() < right.offset());
			break;

		    case GT:
			stack.pushInt(left.offset() > right.offset());
			break;

		    case LE:
			stack.pushInt(left.offset() <= right.offset());
			break;

		    case GE:
			stack.pushInt(left.offset() >= right.offset());
			break;

		    case EQ:
		    case EQEQ:
			stack.pushInt(left.offset() == right.offset());
			break;

		    case NE:
		    case NEEQ:
			stack.pushInt(left.offset() != right.offset());
			break;
		}
	    } else if (left.isString() && right.isString()) {
		switch (op) {
		    case EQ:
			stack.pushInt(0);
			break;

		    case EQEQ:
			stack.pushInt(YoixMake.CString(left).compareTo(YoixMake.CString(right)) == 0);
			break;

		    case NE:
			stack.pushInt(1);
			break;

		    case NEEQ:
			stack.pushInt(YoixMake.CString(left).compareTo(YoixMake.CString(right)) != 0);
			break;

		    default:
			VM.abort(BADOPERAND);
			break;
		}
	    } else if (left.isString() && right.isRegexp()) {
		match = ((YoixRERegexp)(right.getManagedObject())).exec(left.stringValue(), null);
		switch (op) {
		    case EQ:
		    case EQEQ:
			stack.pushInt(match);
			break;

		    case NE:
		    case NEEQ:
			stack.pushInt(!match);
			break;

		    default:
			VM.abort(BADOPERAND);
			break;
		}
	    } else if (left.isRegexp() && right.isString()) {
		match = ((YoixRERegexp)(left.getManagedObject())).exec(right.stringValue(), null);
		switch (op) {
		    case EQ:
		    case EQEQ:
			stack.pushInt(match);
			break;

		    case NE:
		    case NEEQ:
			stack.pushInt(!match);
			break;

		    default:
			VM.abort(BADOPERAND);
			break;
		}
	    } else if (left.isColor() && right.isColor()) {
		switch (op) {
		    case EQ:
			stack.pushInt(0);
			break;

		    case EQEQ:
			stack.pushInt(YoixMake.javaColor(left).equals(YoixMake.javaColor(right)));
			break;

		    case NE:
			stack.pushInt(1);
			break;

		    case NEEQ:
			stack.pushInt(!YoixMake.javaColor(left).equals(YoixMake.javaColor(right)));
			break;

		    default:
			VM.abort(BADOPERAND);
			break;
		}
	    } else if (left.isParseTree() && right.isParseTree()) {
		rtree = (YoixBodyParseTree)right.body();
		ltree = (YoixBodyParseTree)left.body();
		switch (op) {
		    case LT:
			if (rtree != null && ltree != null)
			    stack.pushInt(rtree.ancestorOf(ltree));
			else stack.pushInt(0);
			break;

		    case GT:
			if (rtree != null && ltree != null)
			    stack.pushInt(ltree.ancestorOf(rtree));
			else stack.pushInt(0);
			break;

		    case LE:
			if (rtree != null && ltree != null)
			    stack.pushInt(ltree == rtree || rtree.ancestorOf(ltree));
			else if (rtree == null && ltree == null)
			    stack.pushInt(1);
			else stack.pushInt(0);
			break;

		    case GE:
			if (rtree != null && ltree != null)
			    stack.pushInt(ltree == rtree || ltree.ancestorOf(rtree));
			else if (rtree == null && ltree == null)
			    stack.pushInt(1);
			else stack.pushInt(0);
			break;

		    case EQ:
		    case EQEQ:
			stack.pushInt(rtree == ltree);
			break;

		    case NE:
		    case NEEQ:
			stack.pushInt(rtree != ltree);
			break;
		}
	    } else {
		switch (op) {
		    case EQ:
		    case EQEQ:
			stack.pushInt(0);
			break;

		    case NE:
		    case NEEQ:
			stack.pushInt(1);
			break;

		    default:
			VM.abort(BADOPERAND);
			break;
		}
	    }
	} else if (left.isCallable() && right.isCallable()) {
	    if (left.isNull() || right.isNull()) {
		switch (op) {
		    case LT:
			stack.pushInt(left.isNull() && !right.isNull());
			break;

		    case GT:
			stack.pushInt(!left.isNull() && right.isNull());
			break;

		    case LE:
			stack.pushInt(left.isNull());
			break;

		    case GE:
			stack.pushInt(right.isNull());
			break;

		    case EQ:
		    case EQEQ:
			stack.pushInt(left.isNull() && right.isNull());
			break;

		    case NE:
		    case NEEQ:
			stack.pushInt(!left.isNull() || !right.isNull());
			break;
		}
	    } else {
		switch (op) {
		    case EQ:
		    case EQEQ:
			if (left.isBuiltin() && right.isBuiltin())
			    stack.pushInt(left.toString().equals(right.toString()));
			else stack.pushInt(left.body() == right.body());
			break;

		    case NE:
		    case NEEQ:
			if (left.isBuiltin() && right.isBuiltin())
			    stack.pushInt(!left.toString().equals(right.toString()));
			else stack.pushInt(left.body() != right.body());
			break;

		    default:
			VM.abort(BADOPERAND);
			break;
		}
	    }
	} else {
	    switch (op) {
		case EQ:
		case EQEQ:
		    stack.pushInt(left.isNull() && right.isNull());
		    break;

		case NE:
		case NEEQ:
		    stack.pushInt(!(left.isNull() && right.isNull()));
		    break;

		default:
		    VM.abort(BADOPERAND);
		    break;
	    }
	}
    }


    static void
    expressionRERelational(int op, YoixStack stack) {

	YoixRERegexp  re = null;
	YoixObject    left;
	YoixObject    right;
	boolean       match = false;

	right = stack.popRvalue();
	left = stack.popRvalue();

	if (right.isPointer()) {
	    if (right.isNull()) {
		match = (left.isPointer() && left.isNull());
		re = null;
	    } else if (right.isString())
		re = new YoixRERegexp(right.stringValue());
	    else if (right.isRegexp())
		re = (YoixRERegexp)(right.getManagedObject());
	    else VM.abort(BADOPERAND);

	    if (re != null) {
		if (left.isNumber()) {
		    if (left.isInteger())
			match = re.exec("" + left.intValue(), null);
		    else match = re.exec("" + left.doubleValue(), null);
		} else if (left.isPointer()) {
		    if (left.isNull() || !left.isString())
			match = false;
		    else match = re.exec(left.stringValue(), null);
		} else match = false;
	    }

	    switch (op) {
		case EQTILDA:
		    stack.pushInt(match);
		    break;

		case NETILDA:
		    stack.pushInt(!match);
		    break;

		default:
		    VM.abort(BADOPERAND);
		    break;
	    }
	} else VM.abort(BADOPERAND);
    }


    static void
    expressionShift(int op, YoixStack stack) {

	YoixObject  left;
	YoixObject  right;
	int         shift;

	//
	// Decided we didn't like everything about Java's shift operators,
	// so we changed some of the behavior. Our main complaint was with
	// left shifting and the fact that only the low order 4 or 5 bits
	// of the right operand were ever used. We think that lead to some
	// confusing results, particularly to old C programmers.
	//

	right = stack.popRvalue();
	left = stack.popRvalue();

	if (left.isNumber() && right.isNumber()) {
	    shift = right.intValue();
	    if (left.isInteger()) {
		switch (op) {
		    case LEFTSHIFT:
		    case LEFTSHIFTEQ:
			if (shift > 0) {
			    if (shift < BITSIZE_INT)
				stack.pushInt(left.intValue() << shift);
			    else stack.pushInt(0);
			} else stack.pushInt(left.intValue() >> -shift);
			break;

		    case RIGHTSHIFT:
		    case RIGHTSHIFTEQ:
			if (shift < 0) {
			    shift = -shift;
			    if (shift < BITSIZE_INT)
				stack.pushInt(left.intValue() << shift);
			    else stack.pushInt(0);
			} else stack.pushInt(left.intValue() >> shift);
			break;

		    case UNSIGNEDSHIFT:
		    case UNSIGNEDSHIFTEQ:
			if (shift < 0) {
			    shift = -shift;
			    if (shift < BITSIZE_INT)
				stack.pushInt(left.intValue() << shift);
			    else stack.pushInt(0);
			} else stack.pushInt(left.intValue() >>> shift);
			break;
		}
	    } else {
		switch (op) {
		    case LEFTSHIFT:
		    case LEFTSHIFTEQ:
			if (shift > 0) {
			    if (shift < BITSIZE_LONG)
				stack.pushDouble(left.longValue() << shift);
			    else stack.pushDouble(0);
			} else stack.pushDouble(left.longValue() >> -shift);
			break;

		    case RIGHTSHIFT:
		    case RIGHTSHIFTEQ:
			if (shift < 0) {
			    shift = -shift;
			    if (shift < BITSIZE_LONG)
				stack.pushDouble(left.longValue() << shift);
			    else stack.pushDouble(0);
			} else stack.pushDouble(left.longValue() >> shift);
			break;

		    case UNSIGNEDSHIFT:
		    case UNSIGNEDSHIFTEQ:
			if (shift < 0) {
			    shift = -shift;
			    if (shift < BITSIZE_LONG)
				stack.pushDouble(left.longValue() << shift);
			    else stack.pushDouble(0);
			} else stack.pushDouble(left.longValue() >>> shift);
			break;
		}
	    }
	} else VM.abort(BADOPERAND);
    }


    static void
    expressionUnary(int op, YoixStack stack) {

	YoixObject  right;

	right = stack.popRvalue();

	if (right.isNumber()) {
	    if (right.isInteger()) {
		switch (op) {
		    case COMPLEMENT:
			stack.pushInt(~right.intValue());
			break;

		    case NOT:
			stack.pushInt(right.intValue() == 0);
			break;

		    case UMINUS:
			stack.pushInt(-right.intValue());
			break;

		    case UPLUS:
			stack.pushInt(right.intValue());
			break;
		}
	    } else {
		switch (op) {
		    case COMPLEMENT:
			////stack.pushDouble(-(right.doubleValue() + 1));
			stack.pushDouble(~right.longValue());
			break;

		    case NOT:
			stack.pushInt(right.doubleValue() == 0.0);
			break;

		    case UMINUS:
			stack.pushDouble(-right.doubleValue());
			break;

		    case UPLUS:
			stack.pushDouble(right.doubleValue());
			break;
		}
	    }
	} else if (right.isPointer()) {
	    switch (op) {
		case NOT:
		    stack.pushInt(right.isNull());
		    break;

		default:
		    VM.abort(BADOPERAND);
		    break;
	    }
	} else VM.abort(BADOPERAND);
    }


    static void
    functionCall(SimpleNode args, YoixStack stack) {

	YoixObject  argv[];
	YoixObject  lval;
	YoixObject  tag;
	boolean     unroll;
	int         argc;
	int         n;

	//
	// Tried hard to limit the impact of unroll checking.
	//

	lval = stack.popYoixObject();
	argc = args.length();
	argv = new YoixObject[argc];
	unroll = false;

	for (n = 0; n < argc; n++) {
	    expression(args.getChild(n), stack);
	    argv[n] = stack.popRvalueClone();
	    if (unroll == false && argv[n].canUnroll())
		unroll = true;
	}

	if ((tag = stack.peekYoixObject(null)) != null) {
	    if (tag.isTag() && lval.isFunctionPointer())
		((YoixBodyTag)tag.body()).setFunctionName(lval.name());
	}

	stack.pushYoixObjectClone(lval.execute(unroll ? YoixMisc.unrollArray(argv) : argv));
    }


    static void
    functionDefine(SimpleNode node, int access, YoixStack stack) {

	YoixObject  lval;
	YoixObject  names;
	YoixObject  values;
	YoixObject  funct;
	SimpleNode  qualifier;
	SimpleNode  params;
	SimpleNode  param;
	SimpleNode  tree;
	SimpleNode  extra;
	boolean     varargs;
	String      name;
	String      classname;
	String      methodname;
	int         argc;
	int         index;
	int         n;

	index = 0;
	qualifier = (node.getChild0().type() != NAME) ? node.getChild(index++) : null;
	name = node.getChild(index++).stringValue();
	lval = YoixBodyBlock.newDvalue(name);
	params = node.getChild(index++);
	tree = node.getChild(index++);
	argc = params.length() + 1;	// add one for name

	names = YoixObject.newDictionary(argc - 1);
	values = YoixObject.newArray(argc);
	varargs = (params.type() == VARARGLIST);
	values.put(0, YoixObject.newStringConstant(name), false);

	for (n = 1; n < argc; n++) {
	    param = params.getChild(n - 1);
	    if (param.type() == DECLARATION) {
		values.put(n, YoixMake.yoixInstance(param.getChild0().stringValue()), false);
		names.put(param.getChild1().stringValue(), YoixObject.newInt(n), false);
	    } else names.put(param.stringValue(), YoixObject.newInt(n));
	}

	funct = YoixObject.newFunction(names, values, tree, varargs);
	lval.declare(funct);
	if (qualifier != null)
	    qualifier(qualifier, lval.get(), stack);
	/////lval.resolve().setAccess(R_X);

	//
	// The compiler sometimes appends a child that contains the qualified
	// name of a method that implements the function, so we check to see
	// if there's another child that looks right and if so we try to link
	// the function to that method.
	//

	if ((extra = node.getChild(index++)) != null) {
	    if ((name = extra.stringValue()) != null) {
		if ((index = name.indexOf('.')) >= 0) {
		    classname = name.substring(0, index);
		    methodname = name.substring(index+1);
		    ((YoixBodyFunction)funct.body()).link(classname, methodname);
		}
	    }
	}
    }


    static YoixObject
    getJumpTable(SimpleNode node) {

	SimpleNode  stmt;
	YoixObject  expr;
	YoixObject  array;
	YoixObject  dict;
	YoixObject  table;
	YoixStack   stack = VM.getThreadStack();
	String      name;
	int         ends[];
	int         tmp[];
	int         integers;
	int         ival;
	int         length;
	int         total;
	int         count;
	int         holes;
	int         index;
	int         n;

	//
	// Builds a jump table (either an array or dictionary) for the
	// case and default labels in a switch statement list. Must be
	// coordinated with the code in this file that handles a switch
	// statement, so that's why it's here.
	//
	// Although it can't currently happen, there's a tiny chance of
	// deadlock because the caller probably is synchronized and case
	// labels are expressions, including function calls, that need
	// to be evaluated (one time). Impossible today because threads
	// can't execute the same tree, but that could change.
	//

	length = node.length();
	array = YoixObject.newArray(2);
	array.putInt(1, length);
	array.setGrowable(true);
	index = 2;

	dict = YoixObject.newDictionary(1);
	dict.putInt(caseLabel(null), length);
	dict.setGrowable(true);

	ends = null;
	integers = 0;

	for (n = 0; n < length; n++) {
	    stack.pushMark();		// unnecessary - investigate later??
	    stmt = node.getChild(n).getTaggedNode();
	    switch (stmt.type()) {
		case CASE:
		    expr = evaluate(stmt.getChild0(), stack);
		    if (expr.isInteger()) {
			ival = expr.intValue();
			if (ends != null) {
			    if (ends[0] > ival)
				ends[0] = ival;
			    else if (ends[1] < ival)
				ends[1] = ival;
			} else ends = new int[] {ival, ival};
			integers++;
		    }
		    array.put(index++, expr, false);
		    array.putInt(index++, n);
		    if ((name = caseLabel(expr)) != null) {
			if (dict.defined(name) == false)
			    dict.putInt(name, n);
			else VM.warn(DUPLICATECASE, new String[] {OFFENDINGVALUE, caseLabelValue(expr)});
		    }
		    break;

		case DEFAULT_:
		    array.putInt(1, n);
		    dict.putInt(caseLabel(null), n);
		    break;
	    }
	    stack.popMark();
	}

	length = array.length();
	count = length/2 - 1;
	if (integers == count) {
	    total = ends[1] - ends[0] + 1;
	    holes = total - count;
	    if (holes < 32) {		// probably should be tunable
		//
		// Can't eliminate storing indices in the new table that
		// we build because code that uses this table might have
		// to do a sequential search when it looks for something
		// other than an integer (e.g., a double). We may be able
		// to address this, but not right now.
		// 
		tmp = new int[2*(total + 1)];
		tmp[0] = ends[0];
		tmp[1] = array.getInt(1, length);
		for (n = 2; n < tmp.length; n += 2) {
		    tmp[n] = tmp[0] + n/2 - 1;
		    tmp[n + 1] = tmp[1];
		}
		for (n = 2; n < length; n += 2) {
		    index = array.getInt(n);
		    tmp[2*(index - ends[0] + 1)] = index;
		    tmp[2*(index - ends[0] + 1)  + 1] = array.getInt(n + 1);
		}
		table = YoixMisc.copyIntoArray(tmp);
	    } else table = (array.length()/2 == dict.length()) ? dict : array;
	} else table = (array.length()/2 == dict.length()) ? dict : array;

	table.setGrowable(false);
	return(table);
    }


    static void
    handleFinally(SimpleNode node, YoixStack stack) {

	YoixError  error_point = null;

	try {
	    error_point = stack.pushError();
	    statement(node, stack);
	    stack.popError();
	}
	catch(YoixError e) {
	    if (e != error_point)
		throw(e);
	    else VM.error(error_point);
	}
    }


    static void
    handleInclude(String path, YoixStack stack) {

	handleInclude(YoixObject.newString(path), stack);
    }


    static void
    handleInclude(YoixObject path, YoixStack stack) {

	InputStream  input;
	YoixObject   stream;

	if (VM.notRestricted()) {
	    if (path.notNull()) {
		stream = YoixObject.newStream(path, READ);
		if (stream.open()) {
		    try {
			if ((input = stream.streamValue().accessDataInputStream()) != null) {
			    if (input.markSupported() == false)
				input = new BufferedInputStream(input);
			    Yoix.includeStream(input, path.stringValue());
			} else VM.abort(UNREADABLEFILE, path.stringValue());
		    }
		    finally {
			stream.close();
		    }
		} else VM.abort(UNREADABLEFILE, path.stringValue());
	    }
	} else VM.abort(RESTRICTEDACCESS);
    }


    static void
    ivalue(SimpleNode node, YoixStack stack) {

	YoixObject  dest;
	YoixObject  tags = null;
	YoixObject  obj;
	boolean     unroll;
	int         count;
	int         n;

	if (node != null) {
	    switch (node.type()) {
		case ARRAY:
		    unroll = false;
		    dest = YoixObject.newArray(node.length());
		    for (n = 0; n < node.length(); n++) {
			obj = evaluate(node.getChild(n), stack);
			if (obj.isEmpty() == false) {
			    if (obj.canUnroll()) {
				unroll = true;
				dest.put(n, obj, false);
				obj.setUnroll(true);
			    } else dest.put(n, obj, false);
			}
		    }
		    stack.pushYoixObjectClone(unroll ? YoixMisc.unrollObject(dest) : dest);
		    break;

		case DICTIONARY:
		    node = node.getChild0();
		    if (node.type() == COMPOUND) {
			count = node.getChildLast().intValue();
			dest = YoixObject.newDictionary(count);
			if (VM.getBoolean(N_ADDTAGS))
			    tags = YoixObject.newArray(count);
			stack.pushLocalBlock(dest, dest, tags, false);
			statementList(node, 0, node.length() - 1, stack);
			stack.popBlock();
			stack.pushYoixObjectClone(dest);
		    } else {
			statement(node, stack);
			stack.pushEmpty();
		    }
		    break;

		case EXPRESSION:
		    expression(node.getChild0(), stack);
		    break;

		default:
		    VM.die(INTERNALERROR);
		    break;
	    }
	} else stack.pushEmpty();

	stack.pushYoixObject(tags != null ? tags : YoixObject.newNull());
    }


    static void
    lvalue(SimpleNode node, YoixStack stack) {

	SimpleNode  child;
	YoixObject  left;
	YoixObject  right;
	int         length;
	int         n;

	length = node.length();

	for (n = lvaluePrimary(node, stack); n < length; n++) {
	    child = node.getChild(n);
	    switch (child.type()) {
		case EXPRESSION:
		    right = evaluate(child, stack);
		    left = stack.popRvalue();
		    if (right.isInteger())
			stack.pushLvalue(left, left.offset() + right.intValue());
		    else if (right.isString())
			stack.pushLvalue(left, right.stringValue());
		    else VM.abort(TYPECHECK);
		    stack.peekYoixObject().setResolve(true);
		    break;

		case FUNCTION:
		    functionCall(child, stack);
		    break;

		case NAME:
		    stack.pushLvalue(stack.popRvalue(), child.stringValue());
		    stack.peekYoixObject().setResolve(true);
		    break;

		default:
		    VM.die(INTERNALERROR);
		    break;
	    }
	}
    }


    static int
    lvaluePrimary(SimpleNode node, YoixStack stack) {

	SimpleNode  child;
	int         count = 0;

	child = node.getChild(count++);

	switch (child.type()) {
	    case NAME:
		stack.pushYoixObject(YoixBodyBlock.newLvalue(child.stringValue()));
		break;

	    case BOUND_LVALUE:
		stack.pushYoixObject(YoixBodyBlock.newLvalue((YoixParserBvalue)child.getBvalue()));
		break;

	    case GLOBAL:
		stack.pushLvalue(YoixBodyBlock.getGlobal(), 0);
		stack.peekYoixObject().setResolve(false);
		break;

	    case THIS:
		stack.pushLvalue(YoixBodyBlock.getThis(), 0);
		stack.peekYoixObject().setResolve(false);
		break;

	    case ADDRESS:
		lvalue(node.getChild(count++), stack);
		if (stack.peekYoixObject().canResolve())
		    stack.peekYoixObject().setResolve(false);
		else VM.abort(UNDEFINEDRESULT);		// parser should catch this??
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

		    default:
			VM.die(INTERNALERROR);
			break;
		}
		stack.pushYoixObjectClone(stack.popRvalue());
		stack.peekYoixObject().setResolve(true);
		break;
	}

	return(count);
    }


    static int
    pickSwitchStatementIndex(SimpleNode node, YoixObject expr) {

	YoixObject  table;
	YoixObject  value;
	String      name;
	int         minval;
	int         val;
	int         length;
	int         index;
	int         n;

	table = node.getJumpTable();
	length = node.getChild1().length();
	index = length;

	if (table.isDictionary()) {
	    index = table.getInt(caseLabel(null), length);
	    if ((name = caseLabel(expr)) != null) {
		if ((n = table.getInt(name, length)) != length)
		    index = n;
	    }
	} else {
	    index = table.getInt(1, length);
	    if ((value = table.getObject(0)) == null || value.isInteger() == false || expr.isInteger() == false) {
		for (n = 2; n < table.length(); n += 2) {
		    value = table.get(n, false);
		    if (value.isString() && expr.isString()) {
			if (expr.stringValue().equals(value.stringValue())) {
			    index = table.getInt(n + 1, length);
			    break;
			}
		    } else if (equalsEQEQ(expr, value)) {
			index = table.getInt(n + 1, length);
			break;
		    }
		}
	    } else {
		val = expr.intValue();
		minval = value.intValue();
		if (val >= minval) {
		    if (val < minval + table.length()/2 - 1)
			index = table.getInt(2*(val - minval + 1) + 1);
		}
	    }
	}

	return(index);
    }


    static void
    qualifier(SimpleNode node, YoixObject dest, YoixStack stack) {

	int  destmode;
	int  bodymode;

	if (node.type() != NAME) {
	    switch (node.type()) {
		case CONST:
		    destmode = L___ | (dest.getAccess() & ~_W_);
		    bodymode = dest.getAccessBody();
		    break;

		case FINAL:
		    destmode = L___ | (dest.getAccess() & ~_W_);
		    bodymode = L___ | (dest.getAccessBody() & ~_W_);
		    break;

		case LOCKED:		//  parser may be ignoring this one
		    destmode = L___ | dest.getAccess();
		    bodymode = L___ | dest.getAccessBody();
		    break;

		default:
		    VM.abort(UNIMPLEMENTED);
		    destmode = dest.getAccess();	// for the compiler
		    bodymode = dest.getAccessBody();
		    break;
	    }
	    if (dest.getAccess() != destmode)
		dest.setAccess(destmode);
	    if (dest.isPointer() && dest.getAccessBody() != bodymode)
		dest.setAccessBody(bodymode);
	}
    }


    static void
    statement(SimpleNode node, YoixStack stack) {

	if (node != null) {
	    if (Thread.currentThread().isInterrupted() == false) {
		switch (node.type()) {
		    case BREAK:
			stack.jumpToBreak();
			break;

		    case CASE:
		    case DEFAULT_:
		    case EMPTY:
			break;

		    case COMPOUND:
			statementCompound(node, 0, stack);
			break;

		    case CONTINUE:
			stack.jumpToContinue();
			break;

		    case DECLARATION:
			statementDeclaration(node, stack);
			break;

		    case DO:
			statementDo(node, stack);
			break;

		    case EXIT:
			statementExit(node, stack);
			break;

		    case EXPRESSION:
			expression(node.getChild0(), stack);
			if (VM.bitCheck(N_DEBUG, DEBUG_EXPRESSION))
			    VM.print(N_STDOUT, stack.popRvalue());
			else stack.removeRvalue();		// force error message??
			break;

		    case FINALLY:
			statementFinally(node, stack);
			break;

		    case FOR:
			statementFor(node, stack);
			break;

		    case FOREACH:
			statementForEach(node, stack);
			break;

		    case FUNCTION:
			statementFunction(node, stack);
			break;

		    case GLOBALBLOCK:
			statementNamedBlock(node, GLOBALBLOCK, stack);
			break;

		    case IF:
			statementIf(node, stack);
			break;

		    case IMPORT:
			statementImport(node, stack);
			break;

		    case INCLUDE:
			statementInclude(node, stack);
			break;

		    case NAMEDBLOCK:
			statementNamedBlock(node, NAMEDBLOCK, stack);
			break;

		    case QUALIFIER:
			statementQualifier(node, stack);
			break;

		    case RESTRICTEDBLOCK:
			statementNamedBlock(node, RESTRICTEDBLOCK, stack);
			break;

		    case RETURN:
			statementReturn(node, stack);
			break;

		    case SAVE:
			statementSave(node, stack);
			break;

		    case STATEMENT:
			statementList(node, 0, node.length(), stack);
			break;

		    case SWITCH:
			statementSwitch(node, stack);
			break;

		    case SYNCHRONIZED:
			statementSynchronized(node, stack);
			break;

		    case TAGGED:
			//
			// Old version used marks to restore the stack, but that
			// sure seems like overkill. All we should have to do to
			// restore the stack (after statement() returns) is pop
			// one object (the tag we pushed) off the stack.
			//
			stack.pushYoixObjectClone(node.getTaggedLocation());
			if (VM.bitCheck(N_DEBUG, DEBUG_TAGGEDSTATEMENT))
			    VM.print(N_STDOUT, stack.peekYoixObject());
			statement(node.getTaggedNode(), stack);
			stack.popYoixObject();
			break;

		    case THISBLOCK:
			statementNamedBlock(node, THISBLOCK, stack);
			break;

		    case TRY:
			statementTry(node, stack);
			break;

		    case TYPEDEF:
			statementTypedef(node, stack);
			break;

		    case WHILE:
			statementWhile(node, stack);
			break;

		    case YOIX_EOF:
			stack.jumpToEOF();
			break;

		    default:
			VM.die(INTERNALERROR);
			break;
		}
	    } else VM.interrupt();
	}
    }


    static void
    statementCompound(SimpleNode node, int start, YoixStack stack) {

	stack.pushLocalBlock(node.getLocalDict(), false);
	statementList(node, start, node.length() - 1, stack);
	stack.popBlock();
    }


    static void
    statementDeclaration(SimpleNode node, YoixStack stack) {

	YoixObject  lval;
	SimpleNode  dnode;
	SimpleNode  inode;
	String      typename;
	int         length;
	int         n;

	n = (node.getChild0().type() == NAME) ? 0 : 1;
	typename = node.getChild(n++).stringValue();

	if (VM.isTypename(typename)) {
	    for (length = node.length(); n < length; n++) {
		dnode = node.getChild(n).getChild0();
		inode = node.getChild(n).getChild1();
		lval = YoixBodyBlock.newDvalue(dnode.getChild0().stringValue());
		declareVariable(lval, typename, dnode, inode, stack);
		qualifier(node.getChild0(), lval.get(), stack);
	    }
	} else VM.abort(BADTYPENAME, typename);
    }


    static void
    statementDo(SimpleNode node, YoixStack stack) {

	SimpleNode  condition;
	SimpleNode  statement;
	YoixError   break_point = null;
	YoixError   continue_point = null;
	boolean     continuing;

	//
	// The do-while helps performance because the inner loop doesn't
	// have to push and pop CONTINUE objects.
	//

	statement = node.getChild0();
	condition = node.getChild1();

	try {
	    break_point = stack.pushBreak();
	    do {
		continuing = false;
		try {
		    continue_point = stack.pushContinue();
		    do
			statement(statement, stack);
		    while (evaluateBoolean(condition, stack));
		}
		catch(YoixError e) {
		    if (continue_point != e)
			throw(e);
		    else continuing = true;
		}
	    } while (continuing);
	    stack.popBreak();		// should also clear CONTINUE
	}
	catch(YoixError e) {
	    if (break_point != e)
		throw(e);
	}
    }


    static void
    statementExit(SimpleNode node, YoixStack stack) {

	expression(node.getChild0(), stack);
	stack.jumpToExit();
    }


    static void
    statementFinally(SimpleNode node, YoixStack stack) {

	stack.pushFinally(node.getChild0());
    }


    static void
    statementFor(SimpleNode node, YoixStack stack) {

	SimpleNode  condition;
	SimpleNode  increment;
	SimpleNode  statement;
	YoixError   break_point = null;
	YoixError   continue_point = null;
	boolean     continuing;

	//
	// The do-while helps performance because the inner loop doesn't
	// have to push and pop CONTINUE objects.
	//

	condition = node.getChild1();
	increment = node.getChild2();
	statement = node.getChild3();

	try {
	    break_point = stack.pushBreak();
	    evaluate(node.getChild0(), stack);
	    do {
		continuing = false;
		try {
		    continue_point = stack.pushContinue();
		    while (evaluateBoolean(condition, stack)) {
			statement(statement, stack);
			expression(increment, stack);
			stack.removeObject();
		    }
		}
		catch(YoixError e) {
		    if (continue_point != e)
			throw(e);
		    evaluate(increment, stack);
		    continuing = true;
		}
	    } while (continuing);
	    stack.popBreak();		// should also clear CONTINUE
	}
	catch(YoixError e) {
	    if (break_point != e)
		throw(e);
	}
    }


    static void
    statementForEach(SimpleNode node, YoixStack stack) {

	SimpleNode  name;
	SimpleNode  expression;
	SimpleNode  statement;
	SimpleNode  increment;
	YoixObject  lval;
	YoixObject  obj;
	YoixError   break_point = null;
	YoixError   continue_point = null;
	boolean     continuing;
	int         incr = 1;

	//
	// The do-while helps performance because the inner loop doesn't
	// have to push and pop CONTINUE objects.
	//

	if (node.length() == 4) {
	    name = node.getChild0();
	    expression = node.getChild1();
	    increment = node.getChild2();
	    statement = node.getChild3();
	} else {
	    name = node.getChild0();
	    expression = node.getChild1();
	    increment = null;
	    statement = node.getChild2();
	}

	expression(expression, stack);
	lval = stack.popRvalueClone();

	if (lval.isPointer()) {
	    if (increment != null) {
		obj = evaluate(increment, stack);
		if (obj.notEmpty()) {
		    if (obj.isNumber())
			incr = obj.intValue();
		    else VM.abort(TYPECHECK);
		}
	    }
	    try {
		break_point = stack.pushBreak();
		stack.pushForEachBlock(name.stringValue(), lval);
		do {
		    continuing = false;
		    try {
			continue_point = stack.pushContinue();
			while (lval.sizeof() > 0) {
			    statement(statement, stack);
			    lval.incrementLvalue(incr);
			}
		    }
		    catch(YoixError e) {
			if (continue_point != e)
			    throw(e);
			continuing = true;
			lval.incrementLvalue(incr);
		    }
		} while (continuing);
		stack.popBreak();		// should also clear CONTINUE
	    }
	    catch(YoixError e) {
		if (break_point != e)
		    throw(e);
	    }
	} else VM.abort(TYPECHECK);
    }


    static void
    statementFunction(SimpleNode node, YoixStack stack) {

	//
	// The grammar may eventually allow a qualifier and return type in
	// function definitions (just like declarations). Currently not a
	// high priority.
	//

	functionDefine(node, EMPTY, stack);
    }


    static void
    statementIf(SimpleNode node, YoixStack stack) {

	do {
	    if (evaluateBoolean(node.getChild0(), stack))
		node = node.getChild1();
	    else node = node.getChild2();
	} while (node != null && node.type() == IF);

	statement(node, stack);
    }


    static void
    statementImport(SimpleNode node, YoixStack stack) {

	YoixObject  lvalues[];
	YoixObject  lval;
	SimpleNode  child;
	int         n;

	//
	// We decided to disable import when we're in a restricted block,
	// even though the yoix dictionary will be NULL (or empty) in a
	// restricted block. Probably overkill that eventually may change.
	//

	if (VM.notRestricted()) {
	    child = node.getChild0();
	    switch (child.type()) {
		case LVALUE:
		    lvalue(child, stack);
		    lval = stack.popYoixObject();
		    if (node.getChild1() == null) {
			if (lval.isDictionary())
			    YoixBodyBlock.importValue(lval.resolveClone(), lval.name());
			else VM.abort(TYPECHECK);
		    } else {
			if (lval.resolve().isDictionary())
			    YoixBodyBlock.importLvalue(lval, true);
			else VM.abort(TYPECHECK);
		    }
		    break;

		case NAME:
		    if ((lvalues = VM.getModuleLvalues()) != null) {
			for (n = 0; n < lvalues.length; n++) {
			    if ((lval = lvalues[n]) != null)
				YoixBodyBlock.importLvalue(lval, false);
			}
		    }
		    break;
	    }
	} else VM.abort(RESTRICTEDACCESS);
    }


    static void
    statementInclude(SimpleNode node, YoixStack stack) {

	YoixObject  path;

	expression(node.getChild0(), stack);
	path = stack.popRvalue();

	if (path.isString() || path.isNull())
	    handleInclude(path, stack);
	else VM.abort(TYPECHECK);
    }


    static void
    statementList(SimpleNode node, int start, int end, YoixStack stack) {

	int  n;

	for (n = start; n < end; n++)
	    statement(node.getChild(n), stack);
    }


    static void
    statementNamedBlock(SimpleNode node, int type, YoixStack stack) {

	SimpleNode  child;
	YoixObject  lval;
	YoixError   error_point = null;
	YoixError   return_point = null;
	int         index;
	int         length;

	//
	// Checking readablity here is questionable - the only potential
	// problem (we think) is that declarations in a named block would
	// let programs guess names and determine, at least from the error
	// message, that a name was defined in an unreadable dictionary.
	// Probably not a big deal, so the check that's done here should
	// be sufficient, particularly because there's currently no way a
	// Yoix program can remove read access from an object.
	//

	if ((child = node.getChild0()) != null && child.type() == LVALUE) {
	    lvalue(child, stack);
	    lval = stack.popRvalue();
	    if (lval.isNull())
		lval = YoixObject.newDictionary(0, -1);
	    index = 1;
	} else {
	    lval = YoixObject.newDictionary(0, -1);
	    index = 0;
	}

	if (lval.compound()) {
	    if (lval.canReadBody()) {		// belongs somewhere else
		if ((length = node.length()) > 0) {
		    switch (type) {
			case GLOBALBLOCK:
			    if (VM.notRestricted()) {
				stack.pushGlobalBlock(YoixMake.yoixGlobal(lval));
				statementList(node, index, length, stack);
				stack.popBlock();
			    } else VM.abort(RESTRICTEDACCESS);
			    break;

			case NAMEDBLOCK:
			case THISBLOCK:
			    stack.pushLocalBlock(lval, lval, type == THISBLOCK);
			    statementList(node, index, length, stack);
			    stack.popBlock();
			    break;

			case RESTRICTEDBLOCK:
			    try {
				error_point = stack.pushError();
				try {
				    return_point = stack.pushReturn();
				    stack.pushRestrictedBlock(YoixMake.yoixRestricted(lval));
				    statementList(node, index, length, stack);
				    stack.popReturn();
				}
				catch(YoixError e) {
				    if (e == return_point)
					stack.popRvalue();		// unused return value
				    else throw(e);
				}
				stack.popError();
			    }
			    catch(YoixError e) {
				if (e != error_point)
				    throw(e);
				else VM.error(error_point);
			    }
			    break;
		    }
		}
	    } else VM.abort(INVALIDACCESS);
	} else VM.abort(TYPECHECK);
    }


    static void
    statementQualifier(SimpleNode node, YoixStack stack) {

	YoixObject  lval;
	SimpleNode  expr;

	//
	// Recently (7/8/09) changed the qualifier() call to make sure lval
	// is resolved. Old version only did lval.get(), which was often OK
	// but it wasn't completely correct.
	//

	lvalue(node.getChild1(), stack);
	lval = stack.popYoixObject();
	if ((expr = node.getChild2()) != null)
	    lval.put(lval.offset(), evaluate(expr, stack), false);
	qualifier(node.getChild0(), lval.resolve(), stack);
    }


    static void
    statementReturn(SimpleNode node, YoixStack stack) {

	expression(node.getChild0(), stack);
	stack.jumpToReturn();
    }


    static void
    statementSave(SimpleNode node, YoixStack stack) {

	YoixObject  lval;
	SimpleNode  expr;

	lvalue(node.getChild0(), stack);
	lval = stack.popYoixObject();
	YoixBodyBlock.saveLvalue(lval);
	if ((expr = node.getChild1()) != null)
	    lval.put(lval.offset(), evaluate(expr, stack), false);
    }


    static void
    statementSwitch(SimpleNode node, YoixStack stack) {

	SimpleNode  stmt;
	YoixObject  expr;
	YoixError   break_point = null;
	int         index;

	stmt = node.getChild1();
	expr = evaluate(node.getChild0(), stack);
	index = pickSwitchStatementIndex(node, expr);

	try {
	    break_point = stack.pushBreak();
	    if (stmt.type() == COMPOUND)
		statementCompound(stmt, index, stack);
	    else statementList(stmt, index, stmt.length(), stack);
	    stack.popBreak();
	}
	catch(YoixError e) {
	    if (break_point != e)
		throw(e);
	}
    }


    static void
    statementSynchronized(SimpleNode node, YoixStack stack) {

	YoixObject  expr;
	boolean	    debugging;
	String      info;

	//
	// Decided not to allow synchronization on NULL even though it
	// should work. Thought it might lead to some confusion - easy
	// to undo, but will be hard to restore once undone.
	//
	// NOTE - old versions always generated the info string and also
	// used expr.toString() when expr wasn't a string, but building
	// a string for object like dictionaries isn't cheap and ends up
	// calling qsort to sort the names. Changed on 4/14/08 so info
	// string is only built when debugging is true and it no longer
	// calls expr.toString() when expr isn't a string. The overhead
	// generated the old code is usually not a big deal, but could
	// be important in applications that do lots of synchronization
	// using compound objects.
	//

	expr = evaluate(node.getChild0(), stack);

	if (expr.isPointer()) {
	    if (expr.notNull()) {
		debugging = VM.bitCheck(N_DEBUG, DEBUG_SYNCHRONIZED);
		if (debugging) {
		    info = "by " + Thread.currentThread().getName();
		    if (expr.isString())
			info += " for " + expr.stringValue();
		    info += "\n";
		} else info = "\n";
		try {
		    if (debugging) {
			VM.print(
			    N_STDOUT,
			    "requesting lock(" + System.currentTimeMillis() + "): " +
			    info
			);
		    }
		    synchronized(expr.getLock()) {
			try {
			    if (debugging) {
				VM.print(
				    N_STDOUT,
				    "       acquired(" + System.currentTimeMillis() + "): " +
				    info
				);
			    }
			    statement(node.getChild1(), stack);
			}
			finally {
			    if (debugging) {
				VM.print(
				    N_STDOUT,
				    "       released(" + System.currentTimeMillis() + "): " +
				    info
				);
			    }
			}
		    }
		}
		finally {
		    if (debugging) {
			VM.print(
			    N_STDOUT,
			    "      returning(" + System.currentTimeMillis() + "): " +
			    info
			);
		    }
		}
	    } else VM.abort(NULLPOINTER);
	} else VM.abort(TYPECHECK);
    }


    static void
    statementTry(SimpleNode node, YoixStack stack) {

	YoixObject  details;
	YoixObject  names;
	YoixObject  handler;
	YoixObject  handled;
	YoixError   catch_point = null;
	YoixError   error_point = null;
	String      message;

	//
	// Decided to disable try/catch when we're in a restricted block,
	// even though there isn't a compelling explanation. Other places,
	// like the code that handles include and global blocks, make more
	// sense, so there's a chance we'll eventually change our mind on
	// this one and at that point it will be painless to back out.
	//
	// Recently (9/10/05) added code to deal with SecurityExceptions,
	// however we suspect it might be useful to catch all Throwables.
	// Not an urgent enchancement so we decided to wait until we have
	// more time for testing.
	//
	// Decided to change how our implementation catch() behaves when
	// there's no return value. Old version assumed it was the same
	// as a false or zero return, but we now treat it just like true.
	// Change was made on 3/26/08. We could use a bit in VM.flags to
	// restore the old behavior if necessary, but this seems like a
	// much more natural implementation and matches Java better. We
	// Checked examples and several large applications and only found
	// one place where catch() did something other than always return
	// true and in that case it could return TRUE or nothing.
	//
	// NOTE - the stack.pushBreakableError() means break and continue
	// will work in the try block, but unfortunately the catch block
	// still needs work. This was fixed rather late (on 8/8/08) and we
	// didn't want to hold the 2.2.0 release up, so we'll look into it
	// before the next release.
	//

	if (VM.notRestricted()) {
	    try {
		stack.beginTry();
		catch_point = stack.pushBreakableError();
		statement(node.getChild0(), stack);
		stack.popError();
	    }
	    catch(YoixError e) {
		if (catch_point == e) {
		    details = e.getDetails();
		    names = YoixObject.newDictionary(1);
		    names.put(node.getChild1().stringValue(), YoixObject.newInt(1));
		    handler = YoixObject.newFunction(names, null, node.getChild2(), false);
		    ((YoixBodyFunction)handler.body()).clearGlobal();
		    handled = YoixObject.newEmpty();

		    try {
			error_point = stack.pushError();
			handled = handler.call(new YoixObject[] {details}, null);
			stack.popError();
		    }
		    catch(Error any) {
			if (any != error_point)
			    stack.popError();
			else VM.error(error_point);
		    }
		    finally {
			if (handled.isEmpty() == false) {
			    if (handled.isNumber() == false || handled.booleanValue() == false)
				stack.jumpToError(details);
			}
		    }
		} else throw(e);		// can this happen??
	    }
	    catch(SecurityException e) {
		stack.popError();
		if ((message = e.getMessage()) != null)
		    details = YoixError.recordDetails(SECURITYCHECK, message.split(": "), e);
		else details = YoixError.recordDetails(SECURITYCHECK, e);
		names = YoixObject.newDictionary(1);
		names.put(node.getChild1().stringValue(), YoixObject.newInt(1));
		handler = YoixObject.newFunction(names, null, node.getChild2(), false);
		((YoixBodyFunction)handler.body()).clearGlobal();
		handled = YoixObject.newEmpty();

		try {
		    error_point = stack.pushError();
		    handled = handler.call(new YoixObject[] {details}, null);
		    stack.popError();
		}
		catch(Error any) {
		    if (any != error_point)
			stack.popError();
		    else VM.error(error_point);
		}
		finally {
		    if (handled.isEmpty() == false) {
			if (handled.isNumber() == false || handled.booleanValue() == false)
			    VM.error(e);
		    }
		}
	    }
	    finally {
		stack.endTry();
	    }
	} else VM.abort(RESTRICTEDACCESS);
    }


    static void
    statementTypedef(SimpleNode node, YoixStack stack) {

	String      name;
	YoixObject  names;
	YoixObject  values;
	SimpleNode  param;
	SimpleNode  params;
	int         argc;
	int         n;

	name = node.getChild0().stringValue();
	params = node.getChild1();
	argc = params.length() + 1;	// add one for name
	names = YoixObject.newDictionary(argc - 1);
	values = YoixObject.newArray(argc);
	values.put(0, YoixObject.newStringConstant(name), false);

	for (n = 1; n < argc; n++) {
	    param = params.getChild(n - 1);
	    if (param.type() == DECLARATION) {
		values.put(n, YoixMake.yoixInstance(param.getChild0().stringValue()), false);
		names.put(param.getChild1().stringValue(), YoixObject.newInt(n), false);
	    } else names.put(param.stringValue(), YoixObject.newInt(n));
	}

	VM.putTypeDefinition(
	    YoixObject.newFunction(names, values, node.getChild2(), true),
	    node.getChild0().stringValue()
	);
    }


    static void
    statementWhile(SimpleNode node, YoixStack stack) {

	SimpleNode  condition;
	SimpleNode  statement;
	YoixError   break_point = null;
	YoixError   continue_point = null;
	boolean     continuing;

	//
	// The do-while helps performance because the inner loop doesn't
	// have to push and pop CONTINUE objects.
	//

	condition = node.getChild0();
	statement = node.getChild1();

	try {
	    break_point = stack.pushBreak();
	    do {
		continuing = false;
		try {
		    continue_point = stack.pushContinue();
		    while (evaluateBoolean(condition, stack))
			statement(statement, stack);
		}
		catch(YoixError e) {
		    if (continue_point != e)
			throw(e);
		    else continuing = true;
		}
	    } while (continuing);
	    stack.popBreak();		// should also clear CONTINUE
	}
	catch(YoixError e) {
	    if (break_point != e)
		throw(e);
	}
    }
}

