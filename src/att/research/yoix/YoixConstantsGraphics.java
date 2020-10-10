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

interface YoixConstantsGraphics

{

    static final int  YOIX_CAP_BUTT = BasicStroke.CAP_BUTT;
    static final int  YOIX_CAP_ROUND = BasicStroke.CAP_ROUND;
    static final int  YOIX_CAP_SQUARE = BasicStroke.CAP_SQUARE;
    static final int  YOIX_COMPOSITE_CLEAR = AlphaComposite.CLEAR;
    static final int  YOIX_COMPOSITE_DST_IN = AlphaComposite.DST_IN;
    static final int  YOIX_COMPOSITE_DST_OUT = AlphaComposite.DST_OUT;
    static final int  YOIX_COMPOSITE_DST_OVER = AlphaComposite.DST_OVER;
    static final int  YOIX_COMPOSITE_SRC = AlphaComposite.SRC;
    static final int  YOIX_COMPOSITE_SRC_IN = AlphaComposite.SRC_IN;
    static final int  YOIX_COMPOSITE_SRC_OUT = AlphaComposite.SRC_OUT;
    static final int  YOIX_COMPOSITE_SRC_OVER = AlphaComposite.SRC_OVER;
    static final int  YOIX_JOIN_BEVEL = BasicStroke.JOIN_BEVEL;
    static final int  YOIX_JOIN_ROUND = BasicStroke.JOIN_ROUND;
    static final int  YOIX_JOIN_MITER = BasicStroke.JOIN_MITER;
    static final int  YOIX_SEG_MOVETO = PathIterator.SEG_MOVETO;
    static final int  YOIX_SEG_LINETO = PathIterator.SEG_LINETO;
    static final int  YOIX_SEG_QUADTO = PathIterator.SEG_QUADTO;
    static final int  YOIX_SEG_CUBICTO = PathIterator.SEG_CUBICTO;	// Java's name
    static final int  YOIX_SEG_CURVETO = PathIterator.SEG_CUBICTO;	// Yoix synonym
    static final int  YOIX_SEG_CLOSE = PathIterator.SEG_CLOSE;
    static final int  YOIX_WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD;
    static final int  YOIX_WIND_NON_ZERO = PathIterator.WIND_NON_ZERO;
}

