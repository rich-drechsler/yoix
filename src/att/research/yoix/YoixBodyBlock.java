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
class YoixBodyBlock

    implements YoixConstants,
	       YoixInterfaceBody

{

    //
    // Used to manage global and local scopes. Should be thread-safe
    // because stacks are, and these only show up on a stack.
    //

    private YoixBodyBlock  previous = null;
    private YoixObject     names;
    private YoixObject     values;
    private YoixObject     tags = null;
    private YoixObject     argvalues;
    private YoixObject     globalvalues;
    private YoixObject     errordict;
    private YoixObject     importdict;
    private YoixObject     typedict;
    private YoixObject     vm;
    private Hashtable      lvalues;
    private boolean        executed;
    private boolean        isglobal;
    private boolean        isrestricted;
    private boolean        autocreate;

    private boolean        isfunctionblock;
    private boolean        isthis;
    private Object         thisstorage;
    private Stack          savestack;
    private int            next = 0;

    private static YoixObject  reserved = null;
    private static YoixObject  reserved_restricted = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyBlock(YoixObject dict) {

	//
	// Starts a restricted global block, so be careful about using it.
	//

	this(dict, dict, null, true, true);
	this.isrestricted = true;
    }


    YoixBodyBlock(YoixObject dict, boolean autocreate) {

	this(dict, dict, null, true, true);
	this.autocreate = autocreate;
    }


    YoixBodyBlock(YoixObject names, YoixObject values, YoixObject tags, boolean isglobal, boolean isthis) {

	YoixBodyBlock  current;

	this.names = names;
	this.values = values;
	this.tags = tags;
	this.isglobal = isglobal;
	this.isthis = isthis;
	this.autocreate = false;

	current = YoixVMThread.getCurrentBlock();
	previous = current;
	YoixVMThread.setCurrentBlock(this);
	savestack = null;
	executed = false;
	argvalues = null;
	isfunctionblock = false;

	if (isglobal) {
	    globalvalues = values;
	    thisstorage = values;
	    errordict = values.getObject(N_ERRORDICT);
	    importdict = values.getObject(N_IMPORTDICT);
	    typedict = values.getObject(N_TYPEDICT);
	    vm = values.getObject(N_VM);
	    lvalues = new Hashtable();
	    isrestricted = false;
	} else if (previous != null) {
	    globalvalues = previous.globalvalues;
	    thisstorage = isthis ? this : previous.thisstorage;
	    errordict = previous.errordict;
	    importdict = previous.importdict;
	    typedict = previous.typedict;
	    vm = previous.vm;
	    lvalues = previous.lvalues;
	    isrestricted = previous.isrestricted;
	}
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final String
    dump() {

	return("--block:" + getBlockLevel() + "--" + NL);
    }


    public final int
    length() {

	return(0);
    }


    public final String
    toString() {

	return(dump().trim());
    }


    public final int
    type() {

	return(BLOCK);
    }

    ///////////////////////////////////
    //
    // YoixBodyBlock Methods
    //
    ///////////////////////////////////

    void
    end() {

	if (names != null) {
	    restoreLvalues();
	    YoixVMThread.setCurrentBlock(previous);
	    previous = null;
	    names = null;
	    values = null;
	    tags = null;
	    argvalues = null;
	    globalvalues = null;
	    thisstorage = null;
	    errordict = null;
	    importdict = null;
	    typedict = null;
	    vm = null;
	    lvalues = null;
	    savestack = null;
	}
    }


    static boolean
    exit() {

	YoixBodyBlock  block;
	boolean        executed;
	int            length;
	int            n;

	if ((executed = (isExecuted() || isRestricted()))) {
	    for (block = YoixVMThread.getCurrentBlock(); ;block = block.previous) {
		if (block.values != null) {
		    VM.pushAccess(R__);
		    length = block.values.length();
		    for (n = 0; n < length; n++) {
			if (block.values.defined(n))
			    block.values.get(n, false).kill();
		    }
		    VM.popAccess();
		}
		if (block.isglobal)
		    break;
	    }
	}

	return(executed);
    }


    static String
    getBlockName(YoixObject values) {

	YoixBodyBlock  block;
	YoixObject     names;
	String         name = null;
	int            offset;
	int            length;
	int            n;

	if (values != null) {
	    if ((values.isArray() || values.isDictionary()) && values.inRange()) {
		if ((block = YoixVMThread.getCurrentBlock()) != null) {
		    do {
			if (values.bodyEquals(block.values)) {
			    if (values.isArray()) {
				names = block.names;
				length = names.length();
				offset = values.offset();
				for (n = 0; n < length; n++) {
				    if (names.defined(n)) {
					if (offset == names.getInt(n, -1)) {
					    name = names.name(n);
					    break;
					}
				    }
				}
			    } else name = values.name();
			} else if (block.isglobal)
			    break;
			block = block.previous;
		    } while (true);
		}
	    }
	}

	return(name);
    }


    static String
    getBlockName(YoixBodyArray body, int index) {

	YoixBodyBlock  block;
	YoixObject     names;
	YoixObject     obj;
	String         name = null;
	int            length;
	int            n;

	if (body != null) {
	    if ((block = YoixVMThread.getCurrentBlock()) != null) {
		do {
		    if ((obj = block.values) != null && body == obj.body()) {
			names = block.names;
			length = names.length();
			for (n = 0; n < length; n++) {
			    if (names.defined(n)) {
				if (index == names.getInt(n, -1)) {
				    name = names.name(n);
				    break;
				}
			    }
			}
		    } else if (block.isglobal)
			break;
		    block = block.previous;
		} while (true);
	    }
	}
	return(name);
    }


    static YoixObject
    getBlockNames(int level) {

	YoixBodyBlock  block;

	if ((block = YoixVMThread.getCurrentBlock()) != null) {
	    for (; level > 0; level--) {
		if (block.isglobal)
		    break;
		block = block.previous;
	    }
	}

	return(level == 0 && block != null ? block.names : null);
    }


    static YoixObject
    getBlockValues(int level) {

	YoixBodyBlock  block;

	if ((block = YoixVMThread.getCurrentBlock()) != null) {
	    for (; level > 0; level--) {
		if (block.isglobal)
		    break;
		block = block.previous;
	    }
	}

	return(level == 0 && block != null ? block.values : null);
    }


    static YoixObject
    getBlockNames(YoixObject values) {

	YoixBodyBlock  block;

	if (values != null) {
	    if ((block = YoixVMThread.getCurrentBlock()) != null) {
		do {
		    if (values.bodyEquals(block.values))
			return(block.names);
		    else if (block.isglobal)
			return(null);
		    block = block.previous;
		} while (true);
	    }
	}

	return(null);
    }


    static YoixObject
    getErrordict() {

	YoixBodyBlock  block;

	return(((block = YoixVMThread.getCurrentBlock()) != null) ? block.errordict : null);
    }


    static YoixObject
    getGlobal() {

	YoixBodyBlock  block;

	return(((block = YoixVMThread.getCurrentBlock()) != null) ? block.globalvalues : null);
    }


    static YoixObject
    getImportdict() {

	YoixBodyBlock  block;

	return(((block = YoixVMThread.getCurrentBlock()) != null) ? block.importdict : null);
    }


    static YoixObject
    getLvalue(String name) {

	YoixBodyBlock  block;
	YoixObject     lval = null;
	int            offset;

	//
	// This was as added so Java code written for custom modules can
	// look through the active blocks the exactly way a Yoix script
	// does, except for the autocreate code used is newLvalue(). We
	// also don't abort here, but instead we just return null when
	// name isn't found.
	//

	if (name != null) {
	    block = YoixVMThread.getCurrentBlock();
	    if (reserved == null || reserved.defined(name) == false) {
		do {
		    if ((offset = block.names.definedAt(name)) != -1) {
			if (block.values.isArray()) {
			    offset = block.names.get(offset, false).intValue();
			    if (block.values.defined(offset))
				lval = YoixObject.newBlockLvalue(block.values, offset);
			} else lval = YoixObject.newBlockLvalue(block.values, offset);
		    } else if (block.isglobal) {
			lval = block.autoImport(name);
			break;
		    } else if (block.isfunctionblock)
			lval = block.getFunctionArgs(name);
		} while (lval == null && (block = block.previous) != null);
	    } else if (block.isrestricted) {
		if (block.globalvalues != null) {
		    if ((offset = block.globalvalues.definedAt(name)) != -1)
			lval = YoixObject.newBlockLvalue(block.globalvalues, offset);
		}
	    } else if ((offset = reserved.definedAt(name)) != -1)
		lval = YoixObject.newBlockLvalue(reserved, offset);
	}
	return(lval);
    }


    static Hashtable
    getLvalues() {

	YoixBodyBlock  block;

	return(((block = YoixVMThread.getCurrentBlock()) != null) ? block.lvalues : null);
    }


    static YoixObject
    getReserved(boolean restricted) {

	YoixObject  dict;
	YoixObject  element;
	String      name;
	int         length;
	int         n;

	if (restricted) {
	    if ((dict = reserved_restricted) == null) {
		if (reserved != null) {
		    length = reserved.length();
		    dict = YoixObject.newDictionary(length);
		    for (n = 0; n < length; n++) {
			if ((name = reserved.name(n)) != null) {
			    if ((element = reserved.getObject(n)) != null) {
				if (element.isStream())
				    element = YoixObject.newStream();
				else if (name.equals(N_EXECUTE))
				    element = YoixObject.newBuiltin();
				else if (name.equals(N_YOIX))
				    element = YoixObject.newDictionary();
				dict.declare(name, element, element.mode());
			    }
			}
		    }
		    reserved_restricted = dict;
		}
	    }
	} else dict = reserved;

	return(dict);
    }


    static YoixObject
    getThis() {

	YoixBodyBlock  current;
	YoixBodyBlock  block;
	YoixObject     obj;

	if ((current = YoixVMThread.getCurrentBlock()) != null) {
	    if (current.thisstorage instanceof YoixBodyBlock) {
		block = (YoixBodyBlock)current.thisstorage;
		if (block.thisstorage instanceof YoixBodyBlock) {
		    if (block.values.isArray())
			block.thisstorage = YoixObject.newDictionary(block.names, block.values);
		    else block.thisstorage = block.values;
		}
		current.thisstorage = block.thisstorage;
	    }
	    obj = (YoixObject)current.thisstorage;
	} else obj = null;
	return(obj);
    }


    static YoixObject
    getTypedict() {

	YoixBodyBlock  block;

	return(((block = YoixVMThread.getCurrentBlock()) != null) ? block.typedict : null);
    }


    static YoixObject
    getVM() {

	YoixBodyBlock  block;

	return(((block = YoixVMThread.getCurrentBlock()) != null) ? block.vm : null);
    }


    static void
    importLvalue(YoixObject lval, boolean force) {

	YoixObject  dest;
	YoixObject  source;
	YoixObject  importdict;
	int         length;
	int         offset;
	int         n;

	if ((dest = getGlobal()) != null) {
	    //
	    // The defined() check is reasonable, I suppose, and is
	    // currently needed to force loading of custom modules.
	    // Without it side effects that are supposed to happen
	    // when a module is loaded (e.g., typedict definitions)
	    // may be skipped.
	    //
	    if (force == false || lval.defined()) {	// primarily to force load of custom modules
		if ((importdict = getImportdict()) != null) {
		    VM.pushAccess(LRWX);
		    importdict.put(importdict.sizeof(), lval, true);
		    VM.popAccess();
		}
		if (VM.getBoolean(N_AUTOIMPORT) == false) {
		    source = lval.resolveClone();
		    if (source.compound()) {
			length =  source.length();
			for (n = 0; n < length; n++) {
			    if (source.defined(n))
				importValue(source.get(n, true), source.name(n), dest);
			}
		    }
		}
	    } else VM.abort(BADIMPORT, lval.name());
	}
    }


    static void
    importValue(YoixObject value, String name) {

	YoixObject  dest;

	if ((dest = getGlobal()) != null)
	    importValue(value, name, dest);
    }


    static boolean
    isDefined(String name) {

	YoixBodyBlock  block;
	boolean        result = false;

	if (reserved == null || reserved.defined(name) == false) {
	    block = YoixVMThread.getCurrentBlock();
	    do {
		if ((result = block.names.defined(name)) || block.isglobal)
		    break;
	    } while ((block = block.previous) != null);
	} else result = true;

	return(result);
    }


    static boolean
    isExecuted() {

	YoixBodyBlock  block;

	if ((block = YoixVMThread.getCurrentBlock()) != null) {
	    do {
		if (block.isglobal)
		    return(block.executed);
		block = block.previous;
	    } while (true);
	}

	return(false);
    }


    static boolean
    isGlobal() {

	YoixBodyBlock  block;

	return(((block = YoixVMThread.getCurrentBlock()) != null) ? block.isglobal : true);
    }


    static boolean
    isGlobalLvalue(YoixObject lval) {

	return(lval != null && lval.bodyEquals(getGlobal()));
    }


    static boolean
    isLocalName(String name) {

	YoixBodyBlock  block;
	boolean        result = false;

	if (name != null) {
	    if (reserved == null || reserved.defined(name) == false) {
		block = YoixVMThread.getCurrentBlock();
		do {
		    if (block.isglobal == false && block.isthis == false) {
			if (block.names.definedAt(name) != -1) {
			    result = true;
			    break;
			}
		    } else break;
		} while ((block = block.previous) != null);
	    }
	}
	return(result);
    }


    static boolean
    isReservedLvalue(YoixObject lval) {

	return(reserved != null && reserved.bodyEquals(lval));
    }


    static boolean
    isReservedName(String name) {

	return(reserved != null && reserved.defined(name));
    }


    static boolean
    isRestricted() {

	YoixBodyBlock  block;

	return(((block = YoixVMThread.getCurrentBlock()) != null) ? block.isrestricted : false);
    }


    static SimpleNode
    newBoundLvalue(String name) {

	YoixBodyBlock  block;
	SimpleNode     bvalue = null;
	int            level = -1;
	int            offset;

	//
	// Looks through the active blocks, to the top "global" block, for
	// a block that defines name. Returns a bvalue that describes where
	// name was found in the current block structure. The level saved
	// in the bvalue is the "distance" from the current block, with a
	// -1 meaning the name was found in the reserved dictionary.
	//

	block = YoixVMThread.getCurrentBlock();

	if (reserved == null || reserved.defined(name) == false) {
	    do {
		level++;
		if ((offset = block.names.definedAt(name)) != -1) {
		    if (block.values.isArray())
			offset = block.names.get(offset, false).intValue();
		    bvalue = SimpleNode.newBoundLvalue(name, level, offset);
		}
		if (block.isglobal)
		    break;
	    } while (bvalue == null && (block = block.previous) != null);
	} else if (block.isrestricted == false) {
	    if ((offset = reserved.definedAt(name)) != -1)
		bvalue = SimpleNode.newBoundLvalue(name, level, offset);
	}

	return(bvalue);
    }


    static YoixObject
    newDvalue(String name) {

	YoixBodyBlock  block;
	YoixObject     names;
	YoixObject     dval;
	int            offset;
	int            slot;

	//
	// Builds an lvalue that references the declaration of variable
	// name in the current block. Must only be used by declarations
	// or function definitions.
	//
	// NOTE - this is used by the binding code, but there probably
	// should be a separate method that handles binding. We suspect
	// it may be part of the reason why we had trouble binding when
	// local block storage was a dictionary,
	//

	dval = null;
	block = YoixVMThread.getCurrentBlock();
	names = block.names;

	if ((offset = names.reserve(name)) != -1) {
	    if (block.values.isArray()) {
		if (names.defined(offset) == false) {
		    slot = block.nextOffset();
		    names.put(offset, YoixObject.newInt(slot), false);
		    offset = slot;
		} else offset = names.get(offset, false).intValue();
	    }
	    dval = YoixObject.newBlockDvalue(block.values, offset);
	    if (block.tags != null)
		block.tags.putObject(offset, YoixVMThread.getThreadStack().peekTag());
	} else VM.abort(BLOCKFULL, name);

	return(dval);
    }


    static YoixObject
    newLvalue(String name) {

	YoixBodyBlock  block;
	YoixObject     lval;
	int            offset;

	//
	// Looks through the active blocks, to the top "global" block,
	// for a definition of name. Returns an lvalue that references
	// the definition. Reserves slot for undefined names if we get
	// to the global block and the N_CREATE flags is true.
	//

	lval = null;
	block = YoixVMThread.getCurrentBlock();

	if (reserved == null || reserved.defined(name) == false) {
	    do {
		if ((offset = block.names.definedAt(name)) != -1) {
		    if (block.values.isArray()) {
			offset = block.names.get(offset, false).intValue();
			if (block.values.defined(offset))
			    lval = YoixObject.newBlockLvalue(block.values, offset);
		    } else lval = YoixObject.newBlockLvalue(block.values, offset);
		} else if (block.isglobal) {
		    if ((lval = block.autoImport(name)) == null) {
			if ((lval = block.autoCreate(name)) == null)
			    VM.abort(UNDEFINED, name);
		    }
		} else if (block.isfunctionblock)
		    lval = block.getFunctionArgs(name);
	    } while (lval == null && (block = block.previous) != null);
	} else if (block.isrestricted) {
	    if (block.globalvalues != null) {
		if ((offset = block.globalvalues.definedAt(name)) != -1)
		    lval = YoixObject.newBlockLvalue(block.globalvalues, offset);
	    }
	} else if ((offset = reserved.definedAt(name)) != -1)
	    lval = YoixObject.newBlockLvalue(reserved, offset);
	else VM.abort(UNDEFINED, name);

	return(lval);
    }


    static YoixObject
    newLvalue(int level, int offset) {

	YoixBodyBlock  block;
	YoixObject     lval = null;

	if (level >= 0) {
	    for (block = YoixVMThread.getCurrentBlock(); ; block = block.previous, level--) {
		if (level == 0) {
		    if (block.values.defined(offset))
			lval = YoixObject.newBlockLvalue(block.values, offset);
		    break;
		} else if (block.isglobal) {
		    //
		    // Don't pass a global block if requested level is too big.
		    //
		    break;
		}
	    }
	} else {
	    if (reserved != null && reserved.defined(offset))
		lval = YoixObject.newBlockLvalue(reserved, offset);
	}

	return(lval);
    }


    static YoixObject
    newLvalue(YoixParserBvalue bvalue) {

	YoixBodyBlock  block;
	YoixObject     lval;
	int            level;

	if ((level = bvalue.getLevel()) >= 0) {
	    for (block = YoixVMThread.getCurrentBlock(); ; block = block.previous, level--) {
		if (level == 0) {
		    lval = YoixObject.newBlockLvalue(block.values, bvalue.getOffset());
		    break;
		} else if (block.isglobal)
		    VM.abort(BINDINGERROR, bvalue.getName());
	    }
	} else lval = YoixObject.newBlockLvalue(reserved, bvalue.getOffset());
	return(lval);
    }


    static void
    saveLvalue(YoixObject lval) {

	YoixBodyBlock  current = YoixVMThread.getCurrentBlock();
	YoixObject     val;
	int            perm;

	if (lval.canWriteBody()) {
	    val = lval.get(true);
	    if (val.canWrite()) {
		if (current.savestack == null)
		    current.savestack = new Stack();
		current.savestack.push(val);
		current.savestack.push(lval);
	    } else VM.abort(INVALIDACCESS);
	} else VM.abort(INVALIDACCESS);
    }


    static void
    setExecuted(boolean state) {

	YoixBodyBlock  block;

	for (block = YoixVMThread.getCurrentBlock(); ;block = block.previous) {
	    if (block.isglobal) {
		block.executed = state;
		break;
	    }
	}
    }


    static void
    setFunctionBlock() {

	YoixBodyBlock  block;

	if ((block = YoixVMThread.getCurrentBlock()) != null)
	    block.isfunctionblock = true;
    }


    static void
    setReserved(YoixObject obj) {

	if (obj == null || obj.isDictionary())
	    reserved = obj;
	else VM.die(INTERNALERROR);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private YoixObject
    autoCreate(String name) {

	YoixObject  lval = null;
	int         offset;
	int         slot;

	//
	// Reserves slot for undefined names if the N_CREATE flag is
	// true.
	//

	if (VM.getBoolean(N_CREATE) || autocreate) {
	    if ((offset = names.reserve(name)) != -1) {
		if (values.isArray()) {		// can this happen??
		    if (names.defined(offset) == false) {
			slot = nextOffset();
			names.put(offset, YoixObject.newInt(slot), false);
			offset = slot;
		    } else offset = names.get(offset, false).intValue();
		}
		lval = YoixObject.newBlockLvalue(values, offset);
	    } else VM.abort(BLOCKFULL, name);
	}
	return(lval);
    }


    private YoixObject
    autoImport(String name) {

	YoixObject  lval = null;
	YoixObject  value;
	int         offset;
	int         slot;

	if (VM.getBoolean(N_AUTOIMPORT)) {
	    if ((value = VM.autoImport(name)) != null) {
		if ((offset = names.reserve(name)) != -1) {
		    if (values.isArray()) {		// can this happen??
			if (names.defined(offset) == false) {
			    slot = nextOffset();
			    names.put(offset, YoixObject.newInt(slot), false);
			    offset = slot;
			} else offset = names.get(offset, false).intValue();
		    }
		    values.put(offset, value, true);
		    lval = YoixObject.newBlockLvalue(values, offset);
		} else VM.abort(BLOCKFULL, name);
	    }
	}
	return(lval);
    }


    private int
    getBlockLevel() {

	YoixBodyBlock  block;
	int            level = 0;

	for (block = YoixVMThread.getCurrentBlock(); block != this; block = block.previous, level++) {
	    if (block.isglobal) {
		level = -1;
		break;
	    }
	}
	return(level);
    }


    private YoixObject
    getFunctionArgs(String name) {

	YoixObject  lval;
	int         offset;

	offset = (name.equals(N_ARGV) ? 1 : (name.equals(N_ARGC) ? 0 : -1));

	if (offset >= 0) {
	    if (argvalues == null) {
		argvalues = YoixObject.newArray(2);
		argvalues.put(0, YoixObject.newInt(values.length()), false);
		argvalues.put(1, values, false);
		//
		// Old versions did,
		//
		//	argvalues.setAccessBody(LR__);
		//
		// which meant function argv could not be incremented. The
		// line was removed on 4/17/02 and will stay out unless we
		// hear of problems.
		//
	    }
	    lval = YoixObject.newBlockLvalue(argvalues, offset);
	} else lval = null;

	return(lval);
    }


    private static void
    importValue(YoixObject value, String name, YoixObject dest) {

	YoixObject  current;
	int         mode;

	//
	// Not certain what to do if name is in reserved - currently
	// ignoring it, but is abort() or warning() better??
	//

	if (reserved == null || reserved.defined(name) == false) {
	    if (dest.defined(name)) {
		current = dest.get(name, false);
		if (value.bodyEquals(current) == false) {
		    if (YoixInterpreter.equalsEQ(current, value) == false)
			VM.abort(BADIMPORT, name);
		}
	    } else {
		mode = (value.mode() & ~_W_) | L___;
		dest.declare(name, value, mode);
	    }
	}
    }


    private int
    nextOffset() {

	int  offset;
	int  length;

	length = values.length();

	if ((offset = next++) >= 0 && offset < length) {
	    if (values.defined(offset) == true) {
		offset = -1;
		for (; next < length; next++) {
		    if (values.defined(next) == false) {
			offset = next++;
			break;
		    }
		}
	    }
	} else offset = -1;		// probably deserve to die here

	return(offset);
    }


    private void
    restoreLvalues() {

	YoixObject  lval;
	YoixObject  val;
	int         vmaccess;
	int         access;

	if (savestack != null) {
	    vmaccess = VM.getAccess();
	    try {
		VM.setAccess(LRWX);
		while (savestack.empty() == false) {
		    lval = (YoixObject)savestack.pop();
		    val = (YoixObject)savestack.pop();
		    access = val.getAccess();
		    lval.put(lval.offset(), val, false);
		    lval.get().setAccess(access);
		}
	    }
	    finally {
		savestack = null;
		VM.setAccess(vmaccess);
	    }
	}
    }
}

