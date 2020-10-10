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

package att.research.yoix.ydat;
import java.awt.*;
import att.research.yoix.*;

public
interface DataAxis {
    YoixObject         callGenerator(YoixObject funct, YoixObject argv[]);
    int                getAnchor();
    double[]           getAxisEnds();
    double[]           getAxisLimits();
    double             getAxisScale();
    int                getAxisSpan();
    int                getBaseline();
    int                getCellHeight();
    Dimension          getCellSize();
    int                getCellWidth();
    SwingJEventPlot    getEventPlot();
    YoixAWTFontMetrics getFontMetrics();
    Insets             getInsets();
    boolean            getInverted();
    int                getLeading();
    int                getLineWidth();
    int                getOrientation();
    int                getTickLength();
    Rectangle          getViewport();
    boolean            hitAxisLimits();
    void               resetAxis();
}

