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
import java.awt.*;

public
interface YoixInterfaceDrawable {

    Graphics  getPaintGraphics();
    boolean   isDrawable();
    boolean   isPaintable();
    boolean   isTileable();
    void      paintBackground(Graphics g);
    void      paintBackgroundImage(Graphics g);
    void      setBackgroundHints(int hints);
    void      setBackgroundImage(Image image);
    void      setPaint(YoixObject obj);
}

