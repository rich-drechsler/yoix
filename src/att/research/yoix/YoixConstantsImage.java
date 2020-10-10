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
import java.awt.color.*;
import java.awt.image.*;

public
interface YoixConstantsImage

{

    static final YoixImageObserver  IMAGEOBSERVER = new YoixImageObserver();

    //
    // Most of the SCALE constants are no longer used.
    //

    static final int  YOIX_EDGE_NO_OP = ConvolveOp.EDGE_NO_OP;
    static final int  YOIX_EDGE_ZERO_FILL = ConvolveOp.EDGE_ZERO_FILL;
    static final int  YOIX_IMAGE_ABORT = ImageObserver.ABORT;
    static final int  YOIX_IMAGE_ALLBITS = ImageObserver.ALLBITS;
    static final int  YOIX_IMAGE_ERROR = ImageObserver.ERROR;
    static final int  YOIX_IMAGE_FRAMEBITS = ImageObserver.FRAMEBITS;
    static final int  YOIX_IMAGE_HEIGHT = ImageObserver.HEIGHT;
    static final int  YOIX_IMAGE_PROPERTIES =  ImageObserver.PROPERTIES;
    static final int  YOIX_IMAGE_SOMEBITS = ImageObserver.SOMEBITS;
    static final int  YOIX_IMAGE_WIDTH = ImageObserver.WIDTH;
    static final int  YOIX_SCALE_AREA_AVERAGING = Image.SCALE_AREA_AVERAGING;
    static final int  YOIX_SCALE_DEFAULT = Image.SCALE_DEFAULT;
    static final int  YOIX_SCALE_FAST = Image.SCALE_FAST;
    static final int  YOIX_SCALE_NONE = 32;
    static final int  YOIX_SCALE_REPLICATE = Image.SCALE_REPLICATE;
    static final int  YOIX_SCALE_SMOOTH = Image.SCALE_SMOOTH;
    static final int  YOIX_SCALE_TILE = 64;

    //
    // The full collection of Java image type names and values. We
    // have not tested every type in all possible situations, but
    // we have stumbled into a few annoying problems that probably
    // are platform dependent. For example, TYPE_USHORT_GRAY didn't
    // work well on our SGI using Java version 1.3.1, so it may be
    // mapped to TYPE_BYTE_GRAY in YoixBodyImage.setType().
    //

    static final int  YOIX_TYPE_CUSTOM = BufferedImage.TYPE_CUSTOM;
    static final int  YOIX_TYPE_INT_RGB = BufferedImage.TYPE_INT_RGB;
    static final int  YOIX_TYPE_INT_ARGB = BufferedImage.TYPE_INT_ARGB;
    static final int  YOIX_TYPE_INT_ARGB_PRE = BufferedImage.TYPE_INT_ARGB_PRE;
    static final int  YOIX_TYPE_INT_BGR = BufferedImage.TYPE_INT_BGR;
    static final int  YOIX_TYPE_3BYTE_BGR = BufferedImage.TYPE_3BYTE_BGR;
    static final int  YOIX_TYPE_4BYTE_ABGR = BufferedImage.TYPE_4BYTE_ABGR;
    static final int  YOIX_TYPE_4BYTE_ABGR_PRE = BufferedImage.TYPE_4BYTE_ABGR_PRE;
    static final int  YOIX_TYPE_USHORT_565_RGB = BufferedImage.TYPE_USHORT_565_RGB;
    static final int  YOIX_TYPE_USHORT_555_RGB = BufferedImage.TYPE_USHORT_555_RGB;
    static final int  YOIX_TYPE_BYTE_GRAY = BufferedImage.TYPE_BYTE_GRAY;
    static final int  YOIX_TYPE_USHORT_GRAY = BufferedImage.TYPE_USHORT_GRAY;
    static final int  YOIX_TYPE_BYTE_BINARY = BufferedImage.TYPE_BYTE_BINARY;
    static final int  YOIX_TYPE_BYTE_INDEXED = BufferedImage.TYPE_BYTE_INDEXED;

    //
    // Our own smaller collection of type names that tries to cover
    // the most important types without mentioning how pixels are
    // actually stored.
    //
    // NOTE - we had trouble using TYPE_USHORT_GRAY on our SGI using
    // 1.3.1, so we currently map YOIX_TYPE_GRAY to TYPE_BYTE_GRAY.
    // This should be invesitgated - we suspect it's another platform
    // dependent problem on our SGIs.
    //

    static final int  YOIX_TYPE_RGB = BufferedImage.TYPE_INT_RGB;
    static final int  YOIX_TYPE_RGB_ALPHA = BufferedImage.TYPE_INT_ARGB;
    static final int  YOIX_TYPE_RGBA = BufferedImage.TYPE_INT_ARGB;
    static final int  YOIX_TYPE_ARGB = BufferedImage.TYPE_INT_ARGB;
    static final int  YOIX_TYPE_RGB_COMPACT = BufferedImage.TYPE_USHORT_565_RGB;
    static final int  YOIX_TYPE_GRAY = BufferedImage.TYPE_BYTE_GRAY;	// temporary
    static final int  YOIX_TYPE_GRAY_COMPACT = BufferedImage.TYPE_BYTE_GRAY;
    static final int  YOIX_TYPE_BINARY = BufferedImage.TYPE_BYTE_BINARY;
    static final int  YOIX_TYPE_INDEXED = BufferedImage.TYPE_BYTE_INDEXED;
}

