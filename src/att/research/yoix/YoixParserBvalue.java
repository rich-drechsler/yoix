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

class YoixParserBvalue

{

    //
    // A simple class used to represent a variable in a parse tree that's
    // been allocated a slot in a block. The block is identified by level,
    // which is the "distance" from the current (i.e., top) block to the
    // one that contains the variable. The offset is the location of the
    // variable in that block. A level that's -1 means the variable was
    // found in the reserved dictionary.
    //
    // Parse trees are initially unbound and never contain BOUND_LVALUE
    // nodes, but the binding process, which is currently only applied to
    // parse trees that represent functions, tries to replace NAME nodes
    // that represent declared variables with BOUND_LVALUE nodes that make
    // lookups faster.
    //

    private int  level;
    private int  offset;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixParserBvalue(String name, int level, int offset) {

	//
	// We ignore name, which currently isn't needed when BOUND_LVALUE
	// nodes are processed, but that undoubtedly will change. Right now
	// the only reason to save name is because we want more information
	// in a dump of a parse tree that contains bound nodes.
	//

	this.level = level;
	this.offset = offset;
    }

    ///////////////////////////////////
    //
    // YoixParserBvalue Methods
    //
    ///////////////////////////////////

    public final String
    toString() {

	return(level + ":" + offset);
    }


    final int
    getLevel() {

	return(level);
    }


    final String
    getName() {

	return(null);
    }


    final int
    getOffset() {

	return(offset);
    }
}

