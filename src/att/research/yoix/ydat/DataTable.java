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
import java.util.BitSet;
import java.util.HashMap;
import att.research.yoix.*;

public
interface DataTable

    extends SweepFilter

{

    int         colorTableWith(Palette palette);
    int         getActiveFieldCount();
    YoixObject  getContext();
    int         getCountTotal(int n);
    DataManager getDataManager();
    YoixObject  getHighlighted();
    String      getKey(int n);
    boolean     isHighlighted(int n);
    boolean     isPressed(int n);
    boolean     isSelected(int n);
    boolean     isSelected(String name);
    void        loadRecords(DataRecord loaded[], DataRecord records[]);
    void        recolorTable();
    void        repaintTable();
    void        repaintTable(int count);
    void        setHighlighted(YoixObject obj);
    void        setPressed(YoixObject obj);
    void        setSelected(YoixObject obj);
    void        setSelected(HashMap select, HashMap deselect);
    void        updateTable(DataRecord loaded[], HitBuffer hits, int count);
}

