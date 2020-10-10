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

class YoixStack

    implements YoixConstants

{

    //
    // Every thread gets a stack that's used to interpret parse trees and
    // to establish control and error handling.
    //
    // NOTE - added popToError() that tries not to die() if it looks like
    // the error came from a VM.abort() call. Seems to work well, but the
    // code was added quickly (on 12/17/06), so there's probably lots of
    // room for improvement.
    //
    // NOTE - eventually should use the UncaughtExceptionHandler interface
    // defined in Java 1.5 to eliminate the stack track and other unwanted
    // noise that comes from ThreadGroup.uncaughtException().
    //

    private YoixObject  orig_stack[];
    private YoixObject  stack[];
    private int         next = 0;
    private int         tries = 0;

    //
    // These constants control how the stacks grow. It's not hard to imagine
    // smarter code, but so far it hasn't been an issue that we have noticed.
    //

    private static final int  INITIAL_CAPACITY = 50;
    private static final int  CAPACITY_INCREMENT = 100;

    //
    // An empty stack (all nulls) that's used for initialization.
    //

    private static final YoixObject  EMPTY_STACK[] = new YoixObject[INITIAL_CAPACITY];

    //
    // Several fixed objects that we put on the stack.
    //

    private static final YoixObject  EMPTY_OBJECT = YoixObject.newEmpty();
    private static final YoixObject  MARK_OBJECT = YoixObject.newMark();
    private static final YoixObject  TRUE_OBJECT = YoixObject.newInt(true);
    private static final YoixObject  FALSE_OBJECT = YoixObject.newInt(false);

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixStack() {

	orig_stack = (YoixObject[])EMPTY_STACK.clone();
	stack = orig_stack;
	next = 0;
	tries = 0;
    }

    ///////////////////////////////////
    //
    // YoixStack Methods
    //
    ///////////////////////////////////

    final void
    beginTry() {

	if (tries < 0)		// just in case
	    tries = 0;
	tries++;
    }


    final void
    collapse() {

	if (next > 1) {
	    stack[next - 2] = stack[next - 1];
	    stack[next - 1] = null;
	    next--;
	} else VM.die(STACKUNDERFLOW);
    }


    final String
    dump(int model) {

	YoixBodyTag  tag;
	YoixObject   obj;
	String       str = "";
	String       indent = "\t";
	int          n;

	for (n = next - 1; n >= 0; n--) {
	    if (stack[n] != null) {
		switch (model) {
		    case MODEL_YOIXSTACKDUMP:
			str += indent + stack[n].dump(indent);
			break;

		    case MODEL_YOIXSTACKTRACE:
			if (stack[n].isTag())
			    str += indent + stack[n].dump(indent);
			break;

		    case MODEL_YOIXCALLSTACKTRACE:
			if (stack[n].isTag()) {
			    tag = (YoixBodyTag)stack[n].body();
			    if (tag.isFunctionTag())
				str += indent + stack[n].dump(indent);
			}
			break;
		}
	    } else str += indent + "--null--" + NL;	// should never happen!!
	}

	return(str);
    }


    static String
    dumpPrefix(int model) {

	String  prefix = null;

	switch (model) {
	    case MODEL_YOIXSTACKDUMP:
		prefix = PREFIX_YOIXSTACKDUMP;
		break;

	    case MODEL_YOIXSTACKTRACE:
		prefix = PREFIX_YOIXSTACKTRACE;
		break;

	    case MODEL_YOIXCALLSTACKTRACE:
		prefix = PREFIX_YOIXCALLSTACKTRACE;
		break;
	}
	return(prefix);
    }


    final void
    duplicateExchange(int n) {

	YoixObject  obj;

	if (next > n) {
	    obj = stack[next - 1];
	    stack[next - 1] = (YoixObject)stack[next - 1 - n].clone();
	    pushObject(obj);
	} else VM.die(STACKUNDERFLOW);
    }


    final void
    endTry() {

	if (--tries < 0)
	    tries = 0;
    }


    final void
    exchange() {

	YoixObject  obj;

	if (next > 1) {
	    obj = stack[next - 1];
	    stack[next - 1] = stack[next - 2];
	    stack[next - 2] = obj;
	} else VM.die(STACKUNDERFLOW);
    }


    final int
    getHeight() {

	return(next);
    }


    final boolean
    inTry() {

	return(tries > 0);
    }


    final void
    jumpToBreak() {

	popToJump(BREAK).jump();
    }


    final void
    jumpToContinue() {

	popToJump(CONTINUE).jump();
    }


    final void
    jumpToEOF() {

	popToJump(YOIX_EOF).jump();
    }


    final void
    jumpToError(YoixObject details) {

	popToError(details).jump(details);
    }


    final void
    jumpToExit() {

	SecurityManager sm;
	YoixObject      result;
	YoixObject      jump;
	int             status;

	result = popYoixObject();

	if (YoixBodyBlock.exit()) {		// check flag first - later
	    jump = popToJump(RETURN);
	    pushYoixObject(result);
	    jump.jump();
	} else {
	    status = result.isNumber() ? result.intValue() : 0;
	    if ((sm = System.getSecurityManager()) instanceof YoixSecurityManager)
		((YoixSecurityManager)sm).checkExit(status);
	    VM.exit(status);
	}
    }


    final void
    jumpToInterrupt() {

	popToJump(INTERRUPT).jump();
    }


    final void
    jumpToReturn() {

	YoixObject  result;
	YoixObject  jump;

	result = popYoixObject();
	jump = popToJump(RETURN);
	pushYoixObject(result);
	jump.jump();
    }


    final YoixObject
    peekTag() {

	YoixObject  tag = null;
	int         n;

	for (n = next - 1; n >= 0; n--) {
	    if (stack[n].isTag()) {
		tag = stack[n];
		break;
	    }
	}
	return(tag);
    }


    final YoixObject
    peekYoixObject() {

	//
	// Explict testing seems to perform better here than try/catch, at
	// least on one platform.
	//

	return(next > 0 ? stack[next - 1] : VM.die(STACKUNDERFLOW));
    }


    final YoixObject
    peekYoixObject(int index) {

	return(next > index ? stack[next - 1 - index] : null);
    }


    final YoixObject
    peekYoixObject(int index, YoixObject fail) {

	return(next > index ? stack[next - 1 - index] : fail);
    }


    final YoixObject
    peekYoixObject(YoixObject fail) {

	return(next > 0 ? stack[next - 1] : fail);
    }


    final void
    popAccess() {

	popToControl(ACCESS);
    }


    final void
    popBlock() {

	popToControl(BLOCK);
    }


    final void
    popBreak() {

	popToJump(BREAK);
    }


    final void
    popContinue() {

	popToJump(CONTINUE);
    }


    final void
    popEOF() {

	popToJump(YOIX_EOF);
    }


    final void
    popError() {

	popToError(null);
    }


    final void
    popMark() {

	popToControl(MARK);
    }


    final void
    popReturn() {

	popToJump(RETURN);
    }


    final YoixObject
    popRvalue() {

	return(popYoixObject().resolve());
    }


    final YoixObject
    popRvalueClone() {

	return(popYoixObject().resolveClone());
    }


    final YoixObject
    popYoixObject() {

	YoixObject  obj;

	try {
	    obj = stack[--next];
	    stack[next] = null;
	}
	catch(ArrayIndexOutOfBoundsException e) {
	    obj = VM.die(STACKUNDERFLOW);
	}
	return(obj);
    }


    final void
    pushBoolean(boolean value) {

	pushObject(value ? TRUE_OBJECT : FALSE_OBJECT);
    }


    final YoixError
    pushBreak() {

	return(pushJumpObject(BREAK));
    }


    final YoixError
    pushBreakableError() {

	YoixError  error = new YoixError();

	pushObject(YoixObject.newBreakableError(error));
	return(error);
    }


    final YoixError
    pushContinue() {

	return(pushJumpObject(CONTINUE));
    }


    final void
    pushDouble(double value) {

	pushObject(YoixObject.newDouble(value));
    }


    final void
    pushEmpty() {

	pushObject(EMPTY_OBJECT);
    }


    final YoixError
    pushEOF(YoixObject argv, boolean executed) {

	YoixObject  dict;
	YoixError   eof;

	//
	// The argv check is required - it will be null when we're getting
	// ready to handle an include statement, and in that case we don't
	// want to start a new global block.
	//

	eof = pushJumpObject(YOIX_EOF);
	if ((dict = VM.newGlobalDict(argv)) != null) {
	    pushGlobalBlock(dict);
	    YoixBodyBlock.setExecuted(executed);
	}
	return(eof);
    }


    final YoixError
    pushError() {

	return(pushJumpObject(ERROR));
    }


    final YoixError
    pushError(String tag, String arg) {

	YoixError  error = pushError();

	error.setPrefix(tag, arg);
	return(error);
    }


    final void
    pushFinally(SimpleNode stmt) {

	int  n;

	for (n = next - 1; n >= 0; n--) {
	    if (stack[n] != null && stack[n].isBlock()) {
		if (n < next - 1 && stack[n+1].isFinally())
		    stack[n+1] = YoixObject.newFinally(stmt);
		else insertAfter(n, YoixObject.newFinally(stmt));
		break;
	    }
	}
    }


    final void
    pushForEachBlock(String name, YoixObject value) {

	pushObject(YoixObject.newForEachBlock(name, value));
    }


    final void
    pushGlobalBlock(YoixObject names) {

	pushObject(YoixObject.newGlobalBlock(names));
    }


    final void
    pushGlobalBlock(boolean autocreate) {

	pushObject(YoixObject.newGlobalBlock(autocreate));
    }


    final void
    pushInt(boolean value) {

	pushObject(value ? TRUE_OBJECT : FALSE_OBJECT);
    }


    final void
    pushInt(int value) {

	pushObject(YoixObject.newInt(value));
    }


    final YoixError
    pushInterrupt() {

	YoixError  interrupt;

	//
	// There's no popInterrupt() because we don't push an INTERRUPT if
	// it looks like one's already on the stack!!
	//

	if (YoixVMThread.isInterruptable() == false) {
	    interrupt = pushJumpObject(INTERRUPT);
	    YoixVMThread.setInterruptable(true);
	} else interrupt = null;
	return(interrupt);
    }


    final void
    pushLocalBlock(YoixObject names, boolean isthis) {

	pushObject(YoixObject.newLocalBlock(names, isthis));
    }


    final void
    pushLocalBlock(YoixObject names, YoixObject values, boolean isthis) {

	pushObject(YoixObject.newLocalBlock(names, values, null, isthis));
    }


    final void
    pushLocalBlock(YoixObject names, YoixObject values, YoixObject tags, boolean isthis) {

	pushObject(YoixObject.newLocalBlock(names, values, tags, isthis));
    }


    final void
    pushLvalue(YoixObject obj, int offset) {

	pushObject(YoixObject.newLvalue(obj, offset));
    }


    final void
    pushLvalue(YoixObject obj, String name) {

	pushObject(YoixObject.newLvalue(obj, name));
    }


    final void
    pushMark() {

	pushObject(MARK_OBJECT);
    }


    final void
    pushYoixObject(YoixObject obj) {

	pushObject(obj);
    }


    final void
    pushYoixObjectClone(YoixObject obj) {

	pushObject((YoixObject)obj.clone());
    }


    final void
    pushRestore(YoixObject lval, YoixObject value) {

	int  access = VM.getAccess();

	try {
	    VM.setAccess(LRWX);
	    pushYoixObjectClone(lval.get());
	    pushYoixObjectClone(lval);
	    pushObject(YoixObject.newRestore());
	}
	finally {
	    VM.setAccess(access);
	}
	if (value != null)
	    lval.put(value);
    }


    final void
    pushRestrictedBlock(YoixObject dict) {

	pushObject(YoixObject.newRestrictedBlock(dict));
    }


    final YoixError
    pushReturn() {

	return(pushJumpObject(RETURN));
    }


    final void
    pushString(String value) {

	pushObject(YoixObject.newString(value));
    }


    final void
    pushTag(int line, int column, String source) {

	pushObject(YoixObject.newTag(line, column, source));
    }


    final void
    removeObject() {

	if (next > 0)
	    stack[--next] = null;
	else VM.die(STACKUNDERFLOW);
    }


    final void
    removeRvalue() {

	if (next > 0) {
	    stack[--next].resolve();
	    stack[next] = null;
	} else VM.die(STACKUNDERFLOW);
    }


    final void
    reset() {

	stack = orig_stack;
	System.arraycopy(EMPTY_STACK, 0, stack, 0, EMPTY_STACK.length);
	next = 0;
	tries = 0;
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    growStack(YoixObject obj) {

	YoixObject  newstack[];
	int         length;

	if ((length = stack.length) <= next) {
	    newstack = new YoixObject[length + CAPACITY_INCREMENT];
	    System.arraycopy(stack, 0, newstack, 0, length);
	    if (stack == orig_stack)
		System.arraycopy(EMPTY_STACK, 0, stack, 0, EMPTY_STACK.length);
	    stack = newstack;
	    next = length;
	}
	stack[next++] = obj;
    }


    private void
    handleRestore() {

	YoixObject  lval;
	YoixObject  value;
	int         access;

	lval = popYoixObject();
	value = popYoixObject();
	access = VM.getAccess();

	try {
	    VM.setAccess(LRWX);
	    lval.put(value);
	}
	finally {
	    VM.setAccess(access);
	}
    }


    private void
    insertAfter(int index, YoixObject obj) {

	YoixObject  newstack[];

	newstack = new YoixObject[stack.length + 1];
	System.arraycopy(stack, 0, newstack, 0, index + 1);
	newstack[index + 1] = obj;
	System.arraycopy(stack, index + 1, newstack, index + 2, next - (index + 1));
	if (stack == orig_stack)
	    System.arraycopy(EMPTY_STACK, 0, stack, 0, EMPTY_STACK.length);
	stack = newstack;
	next += 1;
    }


    private void
    popToControl(int minor) {

	YoixObject  obj;
	int         maj;
	int         min;

	do {
	    obj = popYoixObject();
	    if ((maj = obj.major()) == CONTROL) {
		switch (min = obj.minor()) {
		    case ACCESS:
			VM.setAccess(obj.intValue());
			break;

		    case BLOCK:
			((YoixObject)obj).end();
			break;

		    case FINALLY:
			YoixInterpreter.handleFinally(
			    (SimpleNode)obj.getManagedObject(),
			    this
			);
			break;

		    case RESTORE:
			handleRestore();
			break;
		}
		if (min == minor)
		    break;
	    } else if (maj == JUMP) {
		if (obj.minor() == INTERRUPT)
		    YoixVMThread.setInterruptable(false);
	    }
	} while (true);
    }


    private YoixObject
    popToError(YoixObject details) {

	YoixObject  obj;
	int         maj;
	int         min;

	//
	// A version of popToJump() that only deals with errors and relies
	// on a private popYoixObject() that tries not to die if it clears
	// the stack without finding an error object. Duplicating the code
	// was mostly a performance based decision made because we didn't
	// have the time to run timing test needed to check a popToJump()
	// the handled errors and jumps. We may revisit this in the near
	// future.
	//

	do {
	    obj = popYoixObject(details);
	    if ((maj = obj.major()) == JUMP) {
		switch (min = obj.minor()) {
		    case INTERRUPT:
			YoixVMThread.setInterruptable(false);
			break;
		}
		if (min == ERROR)
		    break;
	    } else if (maj == CONTROL) {
		switch (obj.minor()) {
		    case ACCESS:
			VM.setAccess(obj.intValue());
			break;

		    case BLOCK:
			((YoixObject)obj).end();
			break;

		    case FINALLY:
			YoixInterpreter.handleFinally(
			    (SimpleNode)obj.getManagedObject(),
			    this
			);
			break;

		    case RESTORE:
			handleRestore();
			break;
		}
	    }
	} while (true);
	return(obj);
    }


    private YoixObject
    popToJump(int minor) {

	YoixObject  obj;
	int         maj;
	int         min;

	do {
	    obj = popYoixObject();
	    if ((maj = obj.major()) == JUMP) {
		switch (min = obj.minor()) {
		    case ERROR:
			if (obj.isBreakableError() == false) {
			    if (minor == BREAK || minor == CONTINUE) {
				pushObject(obj);
				VM.abort(ILLEGALJUMP);
			    }
			}
			break;

		    case INTERRUPT:
			YoixVMThread.setInterruptable(false);
			break;
		}
		if (min == minor)
		    break;
	    } else if (maj == CONTROL) {
		switch (obj.minor()) {
		    case ACCESS:
			VM.setAccess(obj.intValue());
			break;

		    case BLOCK:
			((YoixObject)obj).end();
			break;

		    case FINALLY:
			YoixInterpreter.handleFinally(
			    (SimpleNode)obj.getManagedObject(),
			    this
			);
			break;

		    case RESTORE:
			handleRestore();
			break;
		}
	    }
	} while (true);
	return(obj);
    }


    private YoixObject
    popYoixObject(YoixObject details) {

	YoixObject  obj = null;
	YoixError   error;

	//
	// Special version that currently is only used by popToError() that
	// tries not to die if it looks like the we're handling an abort()
	// (i.e., details is not null). Throwing a YoixError object that we
	// create should unwind Java's stack because any methods that catch
	// YoixErrors should only consume them if they created them.
	//
	// NOTE - the UncaughtExceptionHandler interface defined in Java 1.5
	// (it's in the Thread class) will help clean up the error message
	// that that comes from ThreadGroup.uncaughtException(). We probably
	// will let YoixVM implement the UncaughtExceptionHandler interface,
	// whicn would let us remove the stack trace and any other unwanted
	// noise that comes from ThreadGroup.uncaughtException(). Think we
	// could force it in using relection if we really had to, but it's
	// not urgent and probably not worth the complications right now.
	// 

	try {
	    obj = stack[--next];
	    stack[next] = null;
	}
	catch(ArrayIndexOutOfBoundsException e) {
	    if (details != null) {
		if (details.isDictionary()) {
		    next = 0;
		    error = new YoixError();
		    error.setDetails(details);
		    VM.error(error);
		    throw(error);
		}
	    }
	    obj = VM.die(STACKUNDERFLOW);
	}
	return(obj);
    }


    private YoixError
    pushJumpObject(int type) {

	YoixError  error = new YoixError();

	pushObject(YoixObject.newJump(type, error));
	return(error);
    }


    private void
    pushObject(YoixObject obj) {

	if (next == stack.length)
	    growStack(obj);
	else stack[next++] = obj;
    }
}

