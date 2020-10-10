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

public
interface YoixInterfaceFont {
    void         fontAShow(String str, double ax, double ay, double alpha, YoixBodyGraphics graphics);
    void         fontAWidthShow(String str, double cx, double cy, int code, double ax, double ay, double alpha, YoixBodyGraphics graphics);
    void         fontCharPath(String str, boolean stroke, YoixBodyGraphics graphics);
    void         fontDrawString(String str, double x, double y, double alpha, YoixBodyGraphics graphics);
    void         fontDrawString(String str, double x, double y, double alpha, Graphics2D g, YoixBodyGraphics graphics);
    YoixObject   fontGetFontMatrix();
    Font         fontGetJavaFont();
    void         fontKShow(String str, YoixObject proc, double alpha, YoixBodyGraphics graphics);
    YoixObject   fontRotateFont(double angle);
    YoixObject   fontScaleFont(double sx, double sy);
    YoixObject   fontShearFont(double shx, double shy);
    void         fontShow(String str, double alpha, YoixBodyGraphics graphics);
    Point2D      fontStringAdvance(String str, Object arg);
    Rectangle2D  fontStringBounds(String str, boolean tight, Object arg);
    String       fontStringFit(String str, double width, String suffix, String substitute, Object arg);
    String       fontStringFit(String str, double width, String suffix, String substitute, boolean fractionalmetrics, boolean antialiasing, Object arg);
    double       fontStringWidth(String str, Object arg);
    YoixObject   fontTransformFont(YoixBodyMatrix matrix);
    YoixObject   fontTranslateFont(double tx, double ty);
    void         fontWidthShow(String str, double cx, double cy, int code, double alpha, YoixBodyGraphics graphics);
}

