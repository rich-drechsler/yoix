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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import att.research.yoix.*;

public
interface DataViewer

    extends DataColorer,
	    SweepFilter

{
    ArrayList  collectRecordsAt(Point point, boolean selected);
    int        colorViewerWith(Palette palette);
    int        getActiveFieldCount();
    int        getCountTotal(int n);
    YoixObject getHighlighted();
    boolean    getStackMode();
    boolean    isHighlighted(int n);
    boolean    isPressed(int n);
    boolean    isSelected(int n);
    boolean    isSelected(String name);
    void       recolorViewer();
    void       repaintViewer();
    void       repaintViewer(int count);
    void       setDiversityIndex(int index);
    void       setHighlighted(YoixObject obj);
    void       setPressed(YoixObject obj);
    void       setSelected(YoixObject obj);
    void       setSelected(HashMap select, HashMap deselect);
    void       updateViewer(DataRecord loaded[], HitBuffer hits, int count);
}

