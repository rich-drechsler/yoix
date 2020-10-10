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
import java.awt.event.*;
import javax.swing.event.*;

public
interface YoixInterfaceListener

    extends ActionListener,
	    AdjustmentListener,
	    CaretListener,
	    ChangeListener,
	    ComponentListener,
	    FocusListener,
	    HyperlinkListener,
	    InternalFrameListener,
	    ItemListener,
	    KeyListener,
	    ListSelectionListener,
	    MouseListener,
	    MouseMotionListener,
	    MouseWheelListener,
	    TextListener,
	    TreeSelectionListener,
	    WindowListener

{

    //
    // Methods implemented by classes that claim they can be a listener
    // for any AWT event.
    //

}

