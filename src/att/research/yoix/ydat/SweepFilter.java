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
import att.research.yoix.*;

public
interface SweepFilter {
    //
    // Many of these are also mentioned in interfaces like DataViewer or
    // DataColorer. The duplication is convenient and doesn't hurt.
    //

    void    appendRecords(DataRecord records[], int offset);
    void    clear();
    void    clear(boolean selected);
    boolean getAccumulate();
    int     getAccumulatedRecords(HitBuffer hits, BitSet accumulated);
    int[]   getFieldIndices();
    int[]   getFieldMasks();
    int[]   getSelectMasks();
    boolean isManagedBy(DataManager manager);
    boolean isSweepFilter(DataPlot plot);
    void    loadRecords(YoixObject records, boolean accumulating);
    void    loadRecords(DataRecord loaded[], DataRecord records[], boolean force);
    void    recolorSweepFilter();
    void    recordsSorted(DataRecord records[]);
    void    repaintSweepFilter();
    void    repaintSweepFilter(int count);
    void    setDataManager(DataManager manager, int index, int mask);
    void    setDataManager(DataManager manager, int indices[], int masks[], int values[], int partitions[]);
    void    setSweepFiltering(boolean value);
    void    updateSweepFilter(DataRecord loaded[], HitBuffer hits, int count);
}

