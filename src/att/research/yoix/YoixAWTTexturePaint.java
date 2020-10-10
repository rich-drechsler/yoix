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
import java.awt.geom.*;
import java.awt.image.*;

class YoixAWTTexturePaint extends TexturePaint

{

    //
    // This, or something like is, is needed because we want to disable
    // any magic scaling of the image when its used as a texture. When
    // scaling is needed Yoix programs should scale the image before
    // using it as a texture.
    // 

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    YoixAWTTexturePaint(BufferedImage image, Rectangle2D anchor) {

	super(image, anchor);
    }

    ///////////////////////////////////
    //
    // YoixAWTTexturePaint Methods
    //
    ///////////////////////////////////

    public final PaintContext
    createContext(ColorModel cm, Rectangle device, Rectangle2D user, AffineTransform xform, RenderingHints hints) {

	xform.scale(1/xform.getScaleX(), 1/xform.getScaleY());
	return(super.createContext(cm, device, user, xform, hints));
    }
}

