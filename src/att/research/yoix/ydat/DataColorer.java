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
interface DataColorer {
    void        appendRecords(DataRecord records[], int offset);
    int         compare(DataRecord record1, DataRecord record2);
    Color       getColor(DataRecord record);
    Color       getColor(String name);
    YoixObject  getContext();
    Palette     getCurrentPalette();
    DataManager getDataManager();
    String      getKey(int n);
    String      getName(DataRecord record);
    String[]    getNames();
    Object[]    getNamesAndColors();
    String      getTipText(String name, int flags, boolean html);
    String      getTipTextAt(Point point, int flags, boolean html);
    boolean     isActiveDataColorer();
    boolean     isDataColorer();
    boolean     isManagedBy(DataManager manager);
    void        loadColors();
    void        loadRecords(DataRecord loaded[], DataRecord records[]);
    void        loadRecords(DataRecord loaded[], DataRecord records[], boolean force);
    void        setExtent();
    void        tossLabels();
}

