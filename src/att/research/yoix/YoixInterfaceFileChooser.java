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
import java.io.File;
import javax.swing.filechooser.FileFilter;

public
interface YoixInterfaceFileChooser {

    //
    // A few methods that we expect to find in classes that are supposed
    // to represent file choosers.
    //

    void          addChoosableFileFilter(FileFilter filter);
    int           getApproveButtonMnemonic();
    String        getApproveButtonText();
    String        getApproveButtonToolTipText();
    FileFilter[]  getChoosableFileFilters();
    File          getCurrentDirectory();
    FileFilter    getFileFilter();
    File          getSelectedFile();
    File[]        getSelectedFiles();
    boolean       isDirectorySelectionEnabled();
    boolean       isFileHidingEnabled();
    boolean       isFileSelectionEnabled();
    boolean       isMultiSelectionEnabled();
    void          resetChoosableFileFilters();
    void          setAcceptAllFileFilterUsed(boolean state);
    void          setApproveButtonMnemonic(int value);
    void          setApproveButtonText(String text);
    void          setApproveButtonToolTipText(String text);
    void          setCurrentDirectory(File dir);
    void          setDialogType(int type);
    void          setFileFilter(FileFilter filter);
    void          setFileHidingEnabled(boolean mode);
    void          setFileSelectionMode(int mode);
    void          setMultiSelectionEnabled(boolean mode);
    void          setSelectedFile(File file);
    void          setSelectedFiles(File files[]);

}

