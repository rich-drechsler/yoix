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
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

public
class YoixImageObserver

    implements ImageObserver,
	       YoixAPI,
	       YoixConstants,
	       YoixConstantsImage

{

    //
    // A single instance of this class handles all images. Data about
    // each image is stored in Hashtables and updated by imageUpdate()
    // whenever we wait for an image.
    //
    // NOTE - we currently stop when we get YOIX_IMAGE_FRAMEBITS, which
    // probably is not always right. Need to investigate - see comments
    // below. There appears to be a good explanation of the behavior in
    // Java bug reports 4154196 and 4152395.
    //
    // NOTE - older versions created a static Frame that was no longer
    // used. We removed (1/13/06) it because it caused loading problems
    // when the interpreter was headless.
    //

    private Hashtable  imageflags;
    private Hashtable  imagedoneflags;
    private Hashtable  imagedata;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixImageObserver() {

	imageflags = new Hashtable();
	imagedoneflags = new Hashtable();
	imagedata = new Hashtable();
    }

    ///////////////////////////////////
    //
    // ImageObserver Methods
    //
    ///////////////////////////////////

    public final boolean
    imageUpdate(Image image, int flags, int x, int y, int width, int height) {

	YoixObject  data;
	boolean     done;
	int         allflags;

        synchronized(image) {
	    allflags = getFlags(image) | flags;
	    setFlags(image, allflags);
	    if ((data = getData(image)) != null) {
		VM.pushAccess(LRW_);
		data.putInt(N_FLAGS, allflags);
		VM.popAccess();
	    }
            if (done = isDone(image))
                image.notifyAll();
	}

	return(!done);
    }

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public final Image
    getImage(String path) {

	Image  image = null;

	if (path != null) {
	    switch (YoixMisc.guessStreamType(path)) {
		case FILE:
		    image = YoixAWTToolkit.getImage(path);
		    break;

		case URL:
		    try {
			image = YoixAWTToolkit.getImage(new URL(path));
		    }
		    catch(IOException e) {
			VM.caughtException(e);
			VM.abort(BADURL, N_SOURCE);
		    }
		    break;
	    }
	}

	return(image);
    }


    public final Image
    scaleImage(Image source, int width, int height, int hints, YoixObject data) {

	Image  image;

	if (height != source.getHeight(null) || width != source.getWidth(null)) {
	    if ((hints & (YOIX_SCALE_TILE|YOIX_SCALE_NONE)) == 0) {
		image = source.getScaledInstance(width, height, hints);
		image = waitForImage(image, data);
	    } else image = source;
	} else image = source;

	return(image);
    }


    public final Image
    waitForImage(Image image, YoixObject data) {

	int  flags = 0;

	if (image != null) {
	    synchronized(image) {
		if (imagedata.get(image) == null) {	// should always be true
		    if (YoixAWTToolkit.prepareImage(image, -1, -1, this) == false) {
			setData(image, data);
			setFlags(image, YoixAWTToolkit.checkImage(image, -1, -1, null));
			setDoneFlags(image);
			while (isDone(image) == false) {
			    try {
				image.wait();
			    }
			    catch(InterruptedException e) {}
			}
			flags = getFlags(image);
			tossImage(image);
		    }
		}
	    }
	}

	return((flags & (YOIX_IMAGE_ABORT | YOIX_IMAGE_ERROR)) == 0 ? image : null);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private YoixObject
    getData(Image image) {

	return((YoixObject)imagedata.get(image));
    }


    private int
    getDoneFlags(Image image) {

	Object  value;
	int     flags;

	if ((value = imagedoneflags.get(image)) != null)
	    flags = ((Integer)value).intValue();
	else flags = YOIX_IMAGE_ALLBITS | YOIX_IMAGE_ABORT | YOIX_IMAGE_ERROR;

	return(flags);
    }


    private int
    getFlags(Image image) {

	Object  value;
	int     flags;

	if ((value = imageflags.get(image)) != null)
	    flags = ((Integer)value).intValue();
	else flags = 0;

	return(flags);
    }


    private boolean
    isDone(Image image) {

	return((getFlags(image) & getDoneFlags(image)) != 0);
    }


    private void
    setData(Image image, YoixObject value) {

	if (value != null)
	    imagedata.put(image, value);
    }


    private void
    setDoneFlags(Image image) {

	//
	// Stopping when we get FRAMEBITS is definitely questionable, but
	// sometimes seems to be needed. Eventually see if it's only needed
	// when we scale an image - in that case we probably could just add
	// YOIX_IMAGE_FRAMEBITS to the done flags rather than do it for all
	// images. We will look into it later - initially thought it was
	// needed when we worked with an off-screen image.
	//
	// NOTE - There appears to be a good explanation of the behavior in
	// Java bug reports 4154196 and 4152395.
	//

	setDoneFlags(image, YOIX_IMAGE_ALLBITS | YOIX_IMAGE_FRAMEBITS | YOIX_IMAGE_ABORT | YOIX_IMAGE_ERROR);
    }


    private void
    setDoneFlags(Image image, int value) {

	imagedoneflags.put(image, new Integer(value));
    }


    private void
    setFlags(Image image, int value) {

	imageflags.put(image, new Integer(value));
    }


    private void
    tossImage(Image image) {

	imageflags.remove(image);
	imagedata.remove(image);
	imagedoneflags.remove(image);
    }
}

