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
import att.research.yoix.*;

public
interface DataPlot {
    void       appendRecords(DataRecord records[], int offset);
    ArrayList  collectRecordsAt(Point point, boolean selected);
    YoixObject getContext();
    int        getPartitionIndex();
    int[]      getSelectMasks();
    int        getXIndex();
    int        getXMask();
    int        getYIndex();
    int        getYMask();
    String     getTipTextAt(Point point, int flags, boolean html);
    boolean    isManagedBy(DataManager manager);
    void       loadRecords(DataRecord loaded[], DataRecord records[], boolean force);
    void       paintRect(Graphics g);
    void       recolorData();
    void       setDataManager(DataManager manager);
    void       setGenerator(Object generator[]);		// questionable
    void       setPartitionIndex(int index);
    void       setSweepFilter(SweepFilter filter);
    void       setSweepFilters(SweepFilter filters[]);
    void       setUnixTime(YoixObject obj);		// questionable
    void       setXIndex(int index);
    void       setYIndex(int index);
    void       sortRecords(DataRecord records[], DataPlot sorter, DataManager manager);
    void       updatePlot(DataRecord loaded[], HitBuffer hits, int count, DataPlot sorter);
}

