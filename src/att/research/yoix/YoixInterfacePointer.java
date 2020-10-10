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

public
interface YoixInterfacePointer 

    extends YoixInterfaceBody

{

    //
    // Methods that must be implemented by the classes that represent
    // pointers, which are objects that can be dereferenced using dot,
    // subscript, or pointer notation (from C).
    //

    YoixObject  cast(YoixObject obj, int index, boolean clone);
    YoixObject  cast(YoixObject obj, String name, boolean clone);
    boolean     compound();
    void        declare(int index, YoixObject obj, int mode);
    void        declare(String name, YoixObject obj, int mode);
    boolean     defined(int index);
    boolean     defined(String name);
    int         definedAt(String name);
    String      dump(int index, String indent, String typename);
    boolean     executable(int index);
    boolean     executable(String name);
    YoixObject  execute(int index, YoixObject argv[], YoixObject context);
    YoixObject  execute(String name, YoixObject argv[], YoixObject context);
    YoixObject  get(int index, boolean clone);
    YoixObject  get(String name, boolean clone);
    int         hash(String name);
    String      name(int index);
    YoixObject  put(int index, YoixObject obj, boolean clone);
    YoixObject  put(String name, YoixObject obj, boolean clone);
    boolean     readable(int index);
    boolean     readable(String name);
    int         reserve(String name);
}

