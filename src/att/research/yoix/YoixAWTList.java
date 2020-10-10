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

class YoixAWTList extends List

    implements YoixConstants

{

    private YoixBodyComponent  parent;
    private YoixObject         data;

    private YoixAWTFontMetrics  fm;

    private Dimension  cellsize = new Dimension(0, 0);
    private Dimension  slopsize = new Dimension(0, 0);
    private Font       currentfont;
    private int        spacewidth;
    private int        visibleindex = -1;

    //
    // This is here because we occasionally observed some very obscure
    // hangs when we used JRE 1.3.1 (actually 1.3.1-rc2-b23) on Windows
    // NT (probably happens on any Windows platform) and we were able
    // to get a thread dump by hitting ctrl-BREAK. We could tell that
    // the deadlock happened because Java's event thread and one of our
    // threads were trying to lock the same YoixAWTTextCanvas and grab
    // the AWT treelock associated with a YoixAWTTextCanvas. Turns out
    // we had some luck reproducing the deadlock, but it was complicated
    // and involved horizontal scrolling, modal dialogs, and posting a
    // query to a server. Although we don't understand exactly how the
    // deadlock happened the dumps did make it clear that removing the
    // synchronization from getLayoutSize() and loadFont() would prevent
    // this particular deadlock. We may investigate some more - later.
    //

    private final Object  FONTLOCK = new Object();

    //
    // An arbitrary sample string used to guess an average character
    // width. Unfortunately using fm.getMaxAdvance() wasn't the right
    // solution, at least for constant width fonts on all platforms.
    //

    private static String  samplestring = "MNmmnnoopp";

    //
    // Measure the thickness of horizontal and vertical scrollbars.
    //

    private static Dimension  scrollbarsize = new Dimension(0, 0);

    static {
	Scrollbar  vbar = new Scrollbar(Scrollbar.VERTICAL);
	Scrollbar  hbar = new Scrollbar(Scrollbar.HORIZONTAL);
	Frame      f = new Frame();

	f.add(hbar, BorderLayout.SOUTH);
	f.add(vbar, BorderLayout.EAST);
	f.pack();
	scrollbarsize.height = hbar.getBounds().height;
	scrollbarsize.width = vbar.getBounds().width;
	YoixMiscJFC.dispose(f);
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTList(YoixObject data, YoixBodyComponent parent, int rows) {

	super(rows);
	this.parent = parent;
	this.data = data;
    }

    ///////////////////////////////////
    //
    // YoixAWTList Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	data = null;
	parent = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    public final Dimension
    getMaximumSize() {

	return(getLayoutSize(N_MAXIMUMSIZE, super.getMaximumSize()));
    }


    public final Dimension
    getMinimumSize() {

	return(getLayoutSize(N_MINIMUMSIZE, super.getMinimumSize()));
    }


    public final Dimension
    getPreferredSize() {

	return(getLayoutSize(N_PREFERREDSIZE, super.getPreferredSize()));
    }


    public final int
    getVisibleIndex() {

	return(visibleindex);
    }


    public final synchronized void
    makeVisible(int index) {

	visibleindex = Math.min(Math.max(index, -1), getItemCount() - 1);
	if (visibleindex >= 0)
	    super.makeVisible(visibleindex);
    }


    public final synchronized void
    removeAll() {

	super.removeAll();
	visibleindex = -1;
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private Dimension
    getLayoutSize(String name, Dimension size) {

	String  str;
	String  items[];
	int     scroll;
	int     width;
	int     dx;
	int     dy;
	int     n;

	if (loadFont()) {
	    if ((items = getItems()) != null && items.length > 0) {
		if ((scroll = data.getInt(N_SCROLL, YOIX_BOTH)) != YOIX_BOTH) {
		    synchronized(FONTLOCK) {
			size = (size == null) ? new Dimension(0, 0) : size;
			switch (scroll) {
			    case YOIX_NONE:
			    case YOIX_VERTICAL:
				for (n = 0; n < items.length; n++) {
				    str = items[n];
				    if ((width = fm.stringWidth(str)) > size.width)
					size.width = width;
				}
				size.width += slopsize.width;
				if (scroll == YOIX_VERTICAL)
				    size.width += scrollbarsize.width;
				break;

			    default:
				break;
			}

			switch (scroll) {
			    case YOIX_NONE:
			    case YOIX_HORIZONTAL:
				size.height = items.length*cellsize.height;
				size.height += slopsize.height;
				if (scroll == YOIX_HORIZONTAL)
				    size.height += scrollbarsize.height;
				break;

			    default:
				break;
			}
		    }
		}
	    }
	}

	return(size);
    }


    private boolean
    loadFont() {

	YoixAWTFontMetrics  metrics;
	Font                font;

	if ((font = getFont()) != null) {
	    if (font.equals(currentfont) == false) {
		if ((metrics = YoixAWTToolkit.getFontMetrics(font)) != null) {
		    synchronized(FONTLOCK) {
			fm = metrics;
			currentfont = font;
			spacewidth = fm.stringWidth(" ");
			cellsize.width = fm.stringWidth(samplestring)/samplestring.length();
			cellsize.height = fm.getMaxAscent() + fm.getMaxDescent() + fm.getLeading();
			//
			// Kludges that currently try to match GFMS adjustments. We
			// eventually will experiment and try to improve things.
			//
			slopsize.width = 4*spacewidth;
			slopsize.height = 0;
			if (ISUNIX) {
			    if (OSNAME.startsWith("Irix"))
				slopsize.height += fm.getAscent();
			}
		    }
		} else font = null;
	    }
	}

	return(font != null);
    }
}

