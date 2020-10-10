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
import java.net.URL;
import javax.imageio.*;
import javax.swing.*;

abstract
class YoixModuleImage extends YoixModule

    implements YoixConstantsImage

{

    static String  $MODULENAME = M_IMAGE;

    static Integer  $RGBTYPE = new Integer(YOIX_TYPE_RGB);
    static String   $YOIXCONSTANTSIMAGE = YOIXPACKAGE + ".YoixConstantsImage";

    static Object  $module[] = {
    //
    // NAME                  ARG                    COMMAND     MODE   REFERENCE
    // ----                  ---                    -------     ----   ---------
       null,                 "45",                  $LIST,      $RORO, $MODULENAME,
       $YOIXCONSTANTSIMAGE,  "YOIX_\tYOIX_",        $READCLASS, $LR__, null,

       "captureScreen",      "",                    $BUILTIN,   $LR_X, null,
       "decodeImage",        "",                    $BUILTIN,   $LR_X, null,
       "decodeJPEG",         "-1",                  $BUILTIN,   $LR_X, null,
       "encodeImage",        "",                    $BUILTIN,   $LR_X, null,
       "encodeJPEG",         "-1",                  $BUILTIN,   $LR_X, null,

       T_IMAGE,              "26",                  $DICT,      $L___, T_IMAGE,
       null,                 "-1",                  $GROWTO,    null,  null,
       N_MAJOR,              $IMAGE,                $INTEGER,   $LR__, null,
       N_MINOR,              "0",                   $INTEGER,   $LR__, null,
       N_BACKGROUND,         T_COLOR,               $NULL,      $RW_,  null,
       N_COLUMNS,            "0",                   $INTEGER,   $LR__, null,
       N_CONVERT,            T_CALLABLE,            $NULL,      $L__X, null,
       N_CONVOLVE,           T_CALLABLE,            $NULL,      $L__X, null,
       N_DESCRIPTION,        T_STRING,              $NULL,      $RW_,  null,
       N_FLAGS,              "0",                   $INTEGER,   $LR__, null,
       N_GETPIXEL,           T_CALLABLE,            $NULL,      $L__X, null,
       N_GRAPHICS,           T_GRAPHICS,            $NULL,      $RW_,  null,
       N_HOTSPOT,            T_POINT,               $NULL,      $RW_,  null,
       N_METRICS,            T_OBJECT,              $NULL,      $RW_,  null,
       N_MODEL,              "-1",                  $INTEGER,   $RW_,  null,
       N_OPAQUE,             $TRUE,                 $INTEGER,   $LR__, null,
       N_PAINT,              T_CALLABLE,            $NULL,      $RWX,  null,
       N_PERSISTENT,         $FALSE,                $INTEGER,   $RW_,  null,
       N_PREFERREDSIZE,      T_DIMENSION,           $NULL,      $LR__, null,
       N_REPAINT,            T_CALLABLE,            $NULL,      $L__X, null,
       N_REPLACE,            T_CALLABLE,            $NULL,      $L__X, null,
       N_RESCALE,            T_CALLABLE,            $NULL,      $L__X, null,
       N_ROWS,               "0",                   $INTEGER,   $LR__, null,
       N_SETPIXEL,           T_CALLABLE,            $NULL,      $L__X, null,
       N_SIZE,               T_DIMENSION,           $NULL,      $RW_,  null,
       N_SOURCE,             T_OBJECT,              $NULL,      $RW_,  null,
       N_TRANSFORM,          T_CALLABLE,            $NULL,      $L__X, null,
       N_TYPE,               $RGBTYPE,              $INTEGER,   $RW_,  null,
    };

    //
    // Decided to keep a static reference to a Robot for captureImage() so
    // security checking, if there is any, can focus on "readDisplayPixels"
    // permission. Means we must load this class before a security manager
    // is installed, but without it every screen capture would also have to
    // allow robot creation. Decided it should be private and kept in this
    // class because there are other Robot methods that apparently aren't
    // checked, which means the "createRobot" permission check is the only
    // way they can be controlled.
    //

    private static Robot  screencapturerobot = null;

    static {
	//
	// Catching SecurityExceptions prevents problems if we're running as
	// an untrusted application under javaws.
	//
	try {
	    screencapturerobot = new Robot();
	}
	catch(AWTException e) {}
	catch(SecurityException e) {}
    }

    ///////////////////////////////////
    //
    // YoixModuleImage Methods
    //
    ///////////////////////////////////

    public static YoixObject
    captureScreen(YoixObject arg[]) {

	BufferedImage  bufimage = null;
	JScrollPane    scroller;
	Rectangle      rect = null;
	Component      comp;
	Dimension      size;
	Object         body;
	Object         managed;
	Point          location;
	Point          prntlctn;

	//
	// Old implementation created a new Robot for every screen capture,
	// but that meant security checking for "readDisplayPixels" would
	// also have to allow "createRobot". Seemed wrong to link the two
	// checks, so instead we create a static Robot instance and use it
	// here. Also means this class needs to be loaded before a security
	// manager is installed.
	//

	if (arg.length >= 0 && arg.length <= 2) {
	    if (arg.length == 0) {
		size = YoixAWTToolkit.getScreenSize();
		rect = new Rectangle(0, 0, size.width, size.height);
	    } else {
		if (arg[0].isNull()) {
		    if (arg.length == 1) {
			size = YoixAWTToolkit.getScreenSize();
			rect = new Rectangle(0, 0, size.width, size.height);
		    } else VM.badArgument(1);
		} else if (arg[0].isComponent()) {
		    if (arg.length == 1) {
			if ((managed = arg[0].getManagedObject()) != null && managed instanceof Component) {
			    //
			    // Think we should use the peerscroller, if
			    // there is one, because the component could
			    // end up being really big. Using peerscroller
			    // means we get the JTable's header, which is
			    // good, but it also means we get scrollbars,
			    // which may or may not be good.
			    // 
			    body = arg[0].body();
			    if (body instanceof YoixBodyComponentSwing) {
				if ((scroller = ((YoixBodyComponentSwing)body).getPeerScroller()) != null) {
				    //
				    // Could check scrollbars here and
				    // calculate appropriate adjustments
				    // to the width and height if they're
				    // visible.
				    //
				    comp = scroller;
				} else comp = (Component)managed;
			    } else comp = (Component)managed;
			    location = comp.getLocation();
			    size = comp.getSize();
			    rect = new Rectangle(0, 0, size.width, size.height);
			    while ((comp = comp.getParent()) != null && !(comp instanceof YoixAWTOwnerFrame)) {
				prntlctn = comp.getLocation();
				location.x += prntlctn.x;
				location.y += prntlctn.y;
				size = comp.getSize();
				rect = rect.intersection(new Rectangle(0, 0, size.width, size.height));
			    }
			    rect = new Rectangle(location.x, location.y, rect.width, rect.height);
			}
		    } else VM.badArgument(1);
		} else if (arg[0].isRectangle()) {
		    if (arg.length == 1) {
			rect = YoixMakeScreen.javaRectangle(arg[0]);
		    } else if (arg[1].isInteger()) {
			if (arg[1].booleanValue()) {
			    // leave it raw
			    rect = new Rectangle(
				arg[0].getInt(N_X, 0), arg[0].getInt(N_Y, 0),
				arg[0].getInt(N_WIDTH, 0), arg[0].getInt(N_HEIGHT, 0)
			    );
			} else rect = YoixMakeScreen.javaRectangle(arg[0]);
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    }
	    if (rect != null && screencapturerobot != null) {
		try {
		    bufimage = screencapturerobot.createScreenCapture(
			rect.intersection(YoixAWTToolkit.getScreenBounds())
		    );
		}
		catch(IllegalArgumentException e) {}
	    }
	} else VM.badCall();

	return(bufimage == null ? YoixObject.newImage() : YoixObject.newImage(bufimage));
    }


    public static YoixObject
    decodeImage(YoixObject arg[]) {

	YoixBodyStream  stream;
	BufferedImage   image;
	InputStream     input;
	YoixObject      obj = null;
	byte            bytes[] = null;
	char            chars[];

	//
	// We recently (1/20/07) changed how a string argument is handled
	// mostly so the output of encodeImage() could be supplied as an
	// to this builtin. The old interpretation assumed a string was
	// the file name or URL of an image file, but the fact that the
	// builtin takes a stream as an argument means it can still read
	// images from files or URLs. The documentation of this builtin,
	// which first appeared in version 2.0.0, didn't explain string
	// arguments, so we don't expect the change will affect any real
	// applications.
	//

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 1) {
		if (arg[0].isStream()) {
		    stream = (YoixBodyStream)arg[0].body();
		    if (((YoixBodyStream)stream).checkMode(READ)) {
			chars = ((YoixBodyStream)stream).readStream(-1);
			bytes = YoixMake.javaByteArray(new String(chars));
		    } else VM.badArgumentValue(0);
		} else if (arg[0].isString())
		    bytes = YoixMake.javaByteArray(arg[0].stringValue());
		else VM.badArgument(0);
		if (bytes != null) {
		    input = new ByteArrayInputStream(bytes);
		    try {
			image = ImageIO.read(input);
			obj = YoixObject.newImage(image);
		    }
		    catch(IOException e) {
			VM.caughtException(e);
		    }
		    finally {
			if (input != null) {
			    try {
				input.close();
			    }
			    catch(IOException ex) {}
			}
		    }
		}
	    } else obj = YoixMake.yoixObject(ImageIO.getReaderFormatNames());
	} else VM.badCall();

	return(obj != null ? obj : YoixObject.newNull());
    }


    public static YoixObject
    decodeJPEG(YoixObject arg[]) {

	return(decodeImage(arg));
    }


    public static YoixObject
    encodeImage(YoixObject arg[]) {

	ByteArrayOutputStream  outstream;
	BufferedImage          image;
	YoixBodyImage          body;
	YoixObject             obj = null;
	boolean                result = false;
	String                 path;
	String                 format = "jpg";
	Object                 stream;

	if (arg.length <= 3) {
	    if (arg.length > 0) {
		if (arg[0].isImage() || arg[0].isNull()) {
		    if (arg[0].notNull()) {
			body = (YoixBodyImage)arg[0].body();
			if ((image = body.copyCurrentImage()) != null) {
			    if (arg.length > 1) {
				if (arg[1].isString()) {
				    if (arg[1].notNull())
					format = arg[1].stringValue();
				} else VM.badArgument(1);
			    }
			    outstream = new ByteArrayOutputStream();
			    try {
				result = ImageIO.write(image, format, outstream);
			    }
			    catch(IllegalArgumentException e) {
				VM.caughtException(e, true);
				VM.badArgumentValue(0); // must be the image
			    }
			    catch(IOException e) {
				VM.caughtException(e, true);
				VM.badArgumentValue(0); // must be the image
			    }
			    if (result) {
				if (arg.length > 2 && arg[2].notNull()) {
				    result = false;
				    if (arg[2].isString()) {
					path = arg[2].stringValue();
					try {
					    switch (YoixMisc.guessStreamType(path)) {
						case FILE:
						    stream = new FileOutputStream(YoixMisc.toYoixPath(path));
						    break;

						case URL:
						    stream = YoixMisc.getOutputStream(new URL(path));
						    break;

						default:
						    stream = null; // for compiler
						    break;
					    }
					    outstream.writeTo((OutputStream)stream);
					}
					catch(IOException e) {
					    VM.caughtException(e);
					    VM.badArgumentValue(2);
					}
					result = true;
				    } else if (arg[2].isStream()) {
					stream = (YoixBodyStream)arg[2].body();
					if (((YoixBodyStream)stream).checkMode(WRITE))
					    ((YoixBodyStream)stream).write(YoixMake.javaString(outstream));
				        else VM.badArgumentValue(2);
					result = true;
				    } else VM.badArgument(2);
				    obj = YoixObject.newInt(result);
				} else obj = YoixObject.newString(YoixMake.javaString(outstream));
			    } else if (arg.length > 1)
				VM.badArgumentValue(1);
			    else VM.abort(INTERNALERROR); // apparently no "jpeg"
			}
		    } else obj = YoixObject.newNull();
		} else VM.badArgument(0);
	    } else obj = YoixMake.yoixObject(ImageIO.getWriterFormatNames());
	} else VM.badCall();

	return(obj != null ? obj : YoixObject.newNull());
    }


    public static YoixObject
    encodeJPEG(YoixObject arg[]) {

	YoixObject  obj = null;

	//
	// Decided to explicitly request the format rather that relying on
	// the default setting in encodeImage().
	//

	if (arg.length == 1) {
	    if (arg[0].isImage() || arg[0].isNull()) {
		if (arg[0].notNull()) {
		    obj = encodeImage(
			new YoixObject[] {
			    arg[0],
			    YoixObject.newString("jpg")
			}
		    );
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj != null ? obj : YoixObject.newString());
    }
}

