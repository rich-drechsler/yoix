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
import javax.media.j3d.NodeComponent;
import att.research.yoix.*;

abstract
class BodyNodeComponent extends BodySceneGraphObject

    implements Constants

{

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyNodeComponent(J3DObject data) {

	super(data);
    }


    BodyNodeComponent(NodeComponent component, J3DObject data) {

	super(component, data);
    }

    ///////////////////////////////////
    //
    // BodyNodeComponent Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	super.finalize();
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

}

