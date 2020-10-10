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
import java.util.zip.*;
import java.lang.reflect.*;
import java.util.Vector;
import att.research.yoix.jvma.*;

class Yoix

    implements YoixAPI,
	       YoixConstants,
	       YoixCompilerConstants

{

    //
    // We always have JavaCC build static parsers, so there can only be
    // one instance of each kind of parser.
    //

    private static PatternParser  parser_pattern = null;
    private static YoixParser     parser_yoix = new YoixParser((CharStream)null);
    private static DOTParser      parser_dot = null;
    private static XMLParser      parser_xml = null;

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public static YoixObject
    evalStream(Reader stream, String name) {

	SecurityManager  sm;

	if ((sm = System.getSecurityManager()) instanceof YoixSecurityManager) {
	    ((YoixSecurityManager)sm).checkYoixEval(
		YoixObject.newString(name),
		YoixObject.newInt(true)
	    );
	}

	return(executeScript(stream, name, null, null, false, false));
    }


    public static YoixObject
    evalString(String source, String name) {

	SecurityManager  sm;

	if ((sm = System.getSecurityManager()) instanceof YoixSecurityManager) {
	    ((YoixSecurityManager)sm).checkYoixEval(
		YoixObject.newString(source),
		YoixObject.newInt(false)
	    );
	}

	return(executeScript(new StringReader(source), name, null, null, false, false));
    }


    public static YoixObject
    executeStream(InputStream stream, String name, YoixObject argv, Vector includes, boolean executed) {

	SecurityManager  sm;

	if ((sm = System.getSecurityManager()) instanceof YoixSecurityManager) {
	    ((YoixSecurityManager)sm).checkYoixExecute(
		YoixObject.newString(name),
		YoixObject.newInt(true),
		argv
	    );
	}

	return(executeInputStream(stream, name, argv, includes, true, executed));
    }


    public static YoixObject
    executeString(String source, String name, YoixObject argv, boolean executed) {

	SecurityManager  sm;

	if ((sm = System.getSecurityManager()) instanceof YoixSecurityManager) {
	    ((YoixSecurityManager)sm).checkYoixExecute(
		YoixObject.newString(source),
		YoixObject.newInt(false),
		argv
	    );
	}

	return(executeScript(new StringReader(source), name, argv, null, true, executed));
    }


    public static void
    includeStream(InputStream stream, String name) {

	SecurityManager  sm;

	if ((sm = System.getSecurityManager()) instanceof YoixSecurityManager)
	    ((YoixSecurityManager)sm).checkYoixInclude(YoixObject.newString(name));

	executeInputStream(stream, name, null, null, false, false);
    }

    ///////////////////////////////////
    //
    // Yoix Methods
    //
    ///////////////////////////////////

    static SimpleNode
    translateCharArray(char source[], boolean addtags, int parser) {

	return(translate(new CharArrayReader(source), "--string--", addtags, parser));
    }


    static SimpleNode
    translateStream(Reader stream, String name, boolean addtags, int parser) {

	return(translate(stream, name, addtags, parser));
    }


    static SimpleNode
    translateString(String source, boolean addtags, int parser) {

	return(translate(new StringReader(source), "--string--", addtags, parser));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static void
    bootDOT() {

	if (parser_dot == null) {
	    synchronized(Yoix.class) {
		if (parser_dot == null)
		    parser_dot = new DOTParser((CharStream)null);
	    }
	}
    }


    private static void
    bootPattern() {

	if (parser_pattern == null) {
	    synchronized(Yoix.class) {
		if (parser_pattern == null)
		    parser_pattern = new PatternParser((CharStream)null);
	    }
	}
    }


    private static void
    bootXML() {

	if (parser_xml == null) {
	    synchronized(Yoix.class) {
		if (parser_xml == null)
		    parser_xml = new XMLParser((CharStream)null);
	    }
	}
    }


    private static YoixObject
    executeInputStream(InputStream stream, String name, YoixObject argv, Vector includes, boolean catcherror, boolean executed) {

	GZIPInputStream  gstream;
	ZipInputStream   zstream;
	YoixObject       result = null;
	YoixObject       objects[];
	SimpleNode       nodes[];
	ZipEntry         ze;
	HashMap          entries;
	String           entryname;
	String           classname;
	Method           compiled_method;
	Class            compiled_class;
	byte             classfile[];
	byte             bytes[];

	//
	// Takes a look at stream before deciding how it should be handled.
	// We use methods like YoixMisc.isZipFile(), that return false if
	// stream doesn't support marks or doesn't begin with the right set
	// of bytes for the particular input stream. Marks are used so the
	// "magic" bytes can be put back after they're checked.
	//
	// NOTE - might be easier if ZipInputStream() threw an IOException
	// (the way GZIPInputStream() does) when we handed it a stream that
	// wasn't zipped, but unfortunately that's not the way it works.
	//

	if (YoixMisc.isZipFile(stream)) {
	    if ((VM.getZipped() & EXECUTE_ZIPPED_ARCHIVE) == EXECUTE_ZIPPED_ARCHIVE) {
		zstream = new ZipInputStream(stream);
		entries = new HashMap();
		try {
		    while ((ze = zstream.getNextEntry()) != null) {
			if ((entryname = ze.getName()) != null) {
			    bytes = YoixMisc.readStream(zstream);
			    if (entryname.equals(N_CLASS))
				entries.put(N_CLASS, bytes);
			    else if (entryname.equals(N_CLASSNAME))
				entries.put(N_CLASSNAME, (new String(bytes)).trim());
			    else if (entryname.equals(N_NODES))
				entries.put(N_NODES, YoixMisc.objectFromBytes(bytes));
			    else if (entryname.equals(N_OBJECTS))
				entries.put(N_OBJECTS, YoixMisc.objectFromBytes(bytes));
			}
		    }
		}
		catch(IOException e) {}

		if (entries.containsKey(N_CLASS) && entries.containsKey(N_CLASSNAME) && entries.containsKey(N_NODES) && entries.containsKey(N_OBJECTS)) {
		    classfile = (byte[])entries.get(N_CLASS);
		    classname = (String)entries.get(N_CLASSNAME);
		    nodes = (SimpleNode[])entries.get(N_NODES);
		    objects = (YoixObject[])entries.get(N_OBJECTS);
		    compiled_class = null;
		    compiled_method = null;
		    
		    //
		    // Trying loadClass() first is overkill, but means we only
		    // initialize the nodes and objects fields the first time
		    // we try to execute the compiled class. Probably could use
		    // some synchronization here to be absolutely sure, but as
		    // noted above, it's overkill as it currently stands!!
		    // 
		    try {
			compiled_class = YoixCompiler.getClassLoader().loadClass(classname);
		    }
		    catch (ClassNotFoundException e) {
			try {
			    compiled_class = YoixCompiler.getClassLoader().defineClass(classfile, classname);
			    compiled_class.getDeclaredField(NAMEOF_NODES_FIELD).set(null, nodes);
			    compiled_class.getDeclaredField(NAMEOF_OBJECTS_FIELD).set(null, objects);
			}
			catch(Throwable t) {
			    VM.abort(INVALIDSCRIPT, name);
			}
		    }

		    try {
			compiled_method = compiled_class.getDeclaredMethod(
			    COMPILED_SCRIPTNAME,
			    new Class[] {YoixObject.class, YoixStack.class, (new SimpleNode[0]).getClass(), (new YoixObject[0]).getClass()}
			);
		    }
		    catch(Throwable t) {
			VM.abort(INVALIDSCRIPT, name);
		    }

		    result = executeScript(compiled_method, nodes, objects, name, argv, includes, catcherror, executed);
		} else VM.abort(INVALIDSCRIPT, name);
	    } else VM.abort(INVALIDSCRIPT, name, new String[] {OFFENDINGINFO, "zipped archives are not enabled (see -z option)"});
	} else if (YoixMisc.isGzipFile(stream)) {
	    if ((VM.getZipped() & EXECUTE_ZIPPED_FILE) == EXECUTE_ZIPPED_FILE) {
		gstream = null;
		try {
		    gstream = new GZIPInputStream(stream);
		}
		catch(IOException e) {}
		if (gstream != null)
		    executeScript(YoixMisc.getParserReader(gstream), name, argv, includes, catcherror, executed);
		else VM.abort(INVALIDSCRIPT, name);
	    } else VM.abort(INVALIDSCRIPT, name, new String[] {OFFENDINGINFO, "gzipped scripts are not enabled (see -z option)"});
	} else result = executeScript(YoixMisc.getParserReader(stream), name, argv, includes, catcherror, executed);

	return(result);
    }


    private static YoixObject
    executeScript(Reader stream, String name, YoixObject argv, Vector includes, boolean catcherror, boolean executed) {

	YoixParserStream   parserstream;
	YoixObject         result;
	SimpleNode         tree;
	YoixStack          stack;
	YoixError          eof_point = null;
	YoixError          error_point = null;
	YoixError          return_point = null;
	boolean            addtags;
	boolean            debugging;
	String             path;

	VM.boot();
	stack = VM.getThreadStack();
	parserstream = new YoixParserStream(stream, name);
	debugging = VM.getBoolean(N_DEBUGGING);
	result = null;

	try {
	    eof_point = stack.pushEOF(argv, executed);
	    try {
		return_point = stack.pushReturn();
		while (true) {
		    try {
			error_point = stack.pushError();
			while (true) {
			    if (includes == null) {
				addtags = debugging || VM.getBoolean(N_ADDTAGS) || (VM.getInt(N_TRACE) != 0);
				synchronized(parser_yoix) {
				    tree = parser_yoix.parseStatement(parserstream, addtags);
				}
				Thread.yield();	// be nice, mostly for green_threads
				YoixInterpreter.statement(tree, stack);
			    } else if (includes.size() > 0) {
				path = (String)includes.elementAt(0);
				includes.removeElementAt(0);
				YoixInterpreter.handleInclude((String)path, stack);
			    } else includes = null;
			}
		    }
		    catch(YoixError e) {
			if (e == error_point) {
			    if (catcherror == false) {
				if (stack.inTry())
				    stack.jumpToError(e.getDetails());
				else VM.error(e);
				break;
			    } else VM.error(e);
			} else throw(e);
		    }
		    catch(SecurityException e) {
			if (stack.inTry()) {
			    stack.popError();
			    stack.jumpToError(YoixError.recordDetails(SECURITYCHECK, e));
			} else {
			    VM.error(e);
			    stack.popError();
			}
		    }
		    catch(UnsupportedOperationException e) {
			VM.error(e);
			stack.popError();
		    }
		}
	    }
	    catch(Error e) {
		if (e == return_point)
		    result = stack.popRvalue();
		else throw(e);
	    }
	    stack.popEOF();
	}
	catch(Error e) {
	    if (e != eof_point)
		throw(e);
	}
	catch(ParseException e) {}	// required, but it can't happen!!

	return(result);
    }


    private static YoixObject
    executeScript(Method method, SimpleNode nodes[], YoixObject objects[], String name, YoixObject argv, Vector includes, boolean catcherror, boolean executed) {

	YoixObject  result = null;
	YoixStack   stack;
	YoixError   eof_point = null;
	YoixError   error_point = null;
	YoixError   return_point = null;
	Throwable   t;
	boolean     addtags;
	boolean     debugging;
	String      path;

	VM.boot();
	stack = VM.getThreadStack();
	debugging = VM.getBoolean(N_DEBUGGING);
	result = null;

	try {
	    eof_point = stack.pushEOF(argv, executed);
	    try {
		return_point = stack.pushReturn();
		while (true) {
		    try {
			error_point = stack.pushError();
			if (includes != null) {
			    while (includes.size() > 0) {
				path = (String)includes.elementAt(0);
				includes.removeElementAt(0);
				YoixInterpreter.handleInclude((String)path, stack);
			    }
			    includes = null;
			}
			//
			// Invoke compiled_class.executeScript() - probably should
			// set catcherror to false!!
			//
			addtags = debugging || VM.getBoolean(N_ADDTAGS) || (VM.getInt(N_TRACE) != 0);
			catcherror = false;
			try {
			    method.invoke(
				null,
				new Object[] {
				    YoixBodyBlock.getGlobal(),
				    stack,
				    nodes,
				    objects
				}
			    );
			}
			catch (InvocationTargetException e) {
			    t = e.getTargetException();
			    if (t instanceof YoixError)
				throw((YoixError)t);
			}
			catch(IllegalAccessException e) {
			    //
			    // This should never happen - the compiler is supposed
			    // build a class that lets us access the method!!
			    //
			    VM.abort(COMPILERERROR);
			}
			break;
		    }
		    catch(YoixError e) {
			if (e == error_point) {
			    if (catcherror == false) {
				if (stack.inTry())
				    stack.jumpToError(e.getDetails());
				else VM.error(e);
				break;
			    } else VM.error(e);
			} else throw(e);
		    }
		    catch(SecurityException e) {
			if (stack.inTry()) {
			    stack.popError();
			    stack.jumpToError(YoixError.recordDetails(SECURITYCHECK, e));
			} else {
			    VM.error(e);
			    stack.popError();
			}
		    }
		    catch(UnsupportedOperationException e) {
			VM.error(e);
			stack.popError();
		    }
		}
	    }
	    catch(Error e) {
		if (e == return_point)
		    result = stack.popRvalue();
		else throw(e);
	    }
	    stack.popEOF();
	}
	catch(Error e) {
	    if (e != eof_point)
		throw(e);
	}

	return(result);
    }


    private static SimpleNode
    translate(Reader stream, String name, boolean addtags, int parser) {

	SimpleNode  node;

	switch (parser) {
	    case PARSER_DOT:
		node = translateDOT(stream, name, addtags);
		break;

	    case PARSER_DTD:
		node = translateXML(stream, name, addtags, true);
		break;

	    case PARSER_PATTERN:
	    case PARSER_PATTERN_AND:
	    case PARSER_PATTERN_OR:
		node = translatePattern(stream, name, addtags, parser);
		break;

	    case PARSER_XML:
		node = translateXML(stream, name, addtags, false);
		break;

	    case PARSER_YOIX:
		node = translateYoix(stream, name, addtags);
		break;

	    default:
		node = null;
		VM.abort(INTERNALERROR);
		break;
	}
	return(node);
    }


    private static SimpleNode
    translateDOT(Reader stream, String name, boolean addtags) {

	YoixParserStream   parserstream;
	SimpleNode         result;
	SimpleNode         tree;
	YoixStack          stack;
	YoixError          error_point = null;

	bootDOT();
	stack = VM.getThreadStack();
	parserstream = new YoixParserStream(stream, name);
	result = new SimpleNode(DOTParserConstants._FOLDER);

	try {
	    error_point = stack.pushError();
	    while (true) {
		synchronized(parser_dot) {
		    tree = parser_dot.parseGraph(parserstream, addtags);
		    if (tree.type() == YOIX_EOF)
			break;
		    else result.jjtAppendChild(tree);
		}
	    }
	    stack.popError();
	}
	catch(YoixError e) {
	    if (e == error_point)
		result = e.getDetails();
	    else throw(e);
	}
	catch(ParseException e) {}	// required, but it can't happen!!

	return(result);
    }


    private static SimpleNode
    translatePattern(Reader stream, String name, boolean addtags, int parser) {

	YoixParserStream   parserstream;
	SimpleNode         result = null;
	YoixStack          stack;
	YoixError          error_point = null;

	bootPattern();
	stack = VM.getThreadStack();
	parserstream = new YoixParserStream(stream, name);

	try {
	    error_point = stack.pushError();
	    synchronized(parser_pattern) {
		result = parser_pattern.parsePattern(parserstream, parser);
	    }
	    stack.popError();
	}
	catch(YoixError e) {
	    if (e == error_point)
		result = e.getDetails();
	    else throw(e);
	}
	catch(ParseException e) {}	// required, but it can't happen!!

	return(result);
    }


    private static SimpleNode
    translateXML(Reader stream, String name, boolean addtags, boolean dtd) {

	YoixParserStream   parserstream;
	SimpleNode         result;
	SimpleNode         tree;
	YoixStack          stack;
	YoixError          error_point = null;

	bootXML();
	stack = VM.getThreadStack();
	parserstream = new YoixParserStream(stream, name);
	result = new SimpleNode(XMLParserConstants._FOLDER);

	try {
	    error_point = stack.pushError();
	    while (true) {
		synchronized(parser_xml) {
		    if (dtd)
			tree = parser_xml.parseDTD(parserstream, addtags);
		    else tree = parser_xml.parseDocument(parserstream, addtags);
		    if (tree.type() == YOIX_EOF)
			break;
		    else result.jjtAppendChild(tree);
		}
	    }
	    stack.popError();
	}
	catch(YoixError e) {
	    if (e == error_point)
		result = e.getDetails();
	    else throw(e);
	}
	catch(ParseException e) {}	// required, but it can't happen!!

	return(result);
    }


    private static SimpleNode
    translateYoix(Reader stream, String name, boolean addtags) {

	YoixParserStream   parserstream;
	SimpleNode         result;
	SimpleNode         tree;
	YoixStack          stack;
	YoixError          error_point = null;

	VM.boot();
	stack = VM.getThreadStack();
	parserstream = new YoixParserStream(stream, name);
	result = new SimpleNode(STATEMENT);

	try {
	    error_point = stack.pushError();
	    do {
		synchronized(parser_yoix) {
		    tree = parser_yoix.parseStatement(parserstream, addtags);
		    result.jjtAppendChild(tree);
		}
	    } while (tree.type() != YOIX_EOF);
	    stack.popError();
	}
	catch(YoixError e) {
	    if (e == error_point)
		result = e.getDetails();
	    else throw(e);
	}
	catch(ParseException e) {}	// required, but it can't happen!!

	return(result);
    }
}

