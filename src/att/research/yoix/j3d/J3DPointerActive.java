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

package att.research.yoix.j3d;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.SceneGraphObject;
import com.sun.j3d.loaders.Scene;
import att.research.yoix.*;

public abstract
class J3DPointerActive extends YoixPointerActive

    implements Constants

{

    //
    // Decided we should keep a reference to data (as a J3DObject) just to
    // make coding a little easier. The alternative is to use getData() and
    // casting.
    //

    protected J3DObject  data;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    protected
    J3DPointerActive(J3DObject data) {

        super(data);
	this.data = data;
    }

    ///////////////////////////////////
    //
    // J3DPointerActive Methods
    //
    ///////////////////////////////////

    protected YoixObject
    call(YoixObject function, YoixObject argv[], YoixObject context) {

	return(super.call(function, argv, context));
    }


    protected void
    finalize() {

	data = null;
	super.finalize();
    }


    final boolean
    getBoolean(String name) {

	return(data.getBoolean(name));
    }


    final boolean
    getBoolean(String name, boolean fail) {

	return(data.getBoolean(name, fail));
    }


    public YoixObject
    getContext() {

	if (context == null)
	    context = J3DObject.newJ3DPointerActive(this);
	return(context);
    }


    final double
    getDouble(String name, double fail) {

	return(data.getDouble(name, fail));
    }


    final float
    getFloat(String name, double fail) {

	return(data.getFloat(name, fail));
    }


    final int
    getInt(String name, int fail) {

	return(data.getInt(name, fail));
    }


    final J3DObject
    getJ3DObject(String name) {

	YoixObject  obj;

	return((obj = data.getObject(name)) instanceof J3DObject ? (J3DObject)obj : null);
    }


    final YoixObject
    getObject(String name) {

	return(data.getObject(name));
    }


    final String
    getString(String name) {

	return(data.getString(name));
    }


    final void
    putObject(String name, YoixObject obj) {

	data.putObject(name, obj);
    }
}

