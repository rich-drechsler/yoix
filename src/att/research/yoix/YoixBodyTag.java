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

final
class YoixBodyTag

    implements YoixConstants,
	       YoixInterfaceBody,
	       YoixInterfaceCloneable,
	       Serializable

{

    //
    // Only for saving line, column, and source file information in
    // a parse tree.
    //

    private String  source;
    private int     line;
    private int     column;

    private String  function_name = null;

    private static String  COLUMN_PREFIX = "Column: ";
    private static String  FUNCTION_PREFIX = "Called: ";
    private static String  LINE_PREFIX = "Line: ";
    private static String  SOURCE_PREFIX = "Source: ";

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyTag(int line) {

	this.line = line;
	this.column = -1;
	this.source = "--unknown--";
    }


    YoixBodyTag(int line, String source) {

	this.line = line;
	this.column = -1;
	this.source = source;
    }


    YoixBodyTag(int line, int column, String source) {

	this.line = line;
	this.column = column;
	this.source = source;
    }

    ///////////////////////////////////
    //
    // YoixInterfaceCloneable Methods
    //
    ///////////////////////////////////

    public final synchronized Object
    clone() {

	Object  obj;

	try {
	    obj = super.clone();
	}
	catch(CloneNotSupportedException e) {
	    obj = VM.die(INTERNALERROR);
	}

	return(obj);
    }


    public final Object
    copy(HashMap copied) {

	return(clone());
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final String
    dump() {

	String  str = "";
	String  sep = "";

	//
	// Eventually could collect and include more information about the
	// function, but this will be sufficient for now. For example, we
	// could store the YoixBodyFunction instead of its name and ask it
	// for more info here.
	//

	if (function_name != null) {
	    str += sep + FUNCTION_PREFIX + function_name;
	    sep = "; ";
	}
	if (line >= 0) {
	    str += sep + LINE_PREFIX + line;
	    sep = "; ";
	}

	if (column >= 0) {
	    str += sep + COLUMN_PREFIX + column;
	    sep = "; ";
	}
	if (source != null) {
	    str += sep + SOURCE_PREFIX + source;
	    sep = "; ";
	}

	return(str + NL);
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

	return(TAG);
    }

    ///////////////////////////////////
    //
    // YoixBodyTag Methods
    //
    ///////////////////////////////////

    final int
    getColumn() {

	return(column);
    }


    static String
    getColumnPrefix() {

	return(COLUMN_PREFIX);
    }


    final String
    getFunctionName() {

	return(function_name);
    }


    final int
    getLine() {

	return(line);
    }


    static String
    getLinePrefix() {

	return(LINE_PREFIX);
    }


    final String
    getSource() {

	return(source);
    }


    static String
    getSourcePrefix() {

	return(SOURCE_PREFIX);
    }


    final boolean
    isFunctionTag() {

	return(function_name != null);
    }


    final void
    setFunctionName(String name) {

	this.function_name = name;
    }
}

