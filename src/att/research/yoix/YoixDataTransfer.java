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
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;

abstract
class YoixDataTransfer

    implements YoixConstants

{

    //
    // This class supports Java's datatransfer package. This list probably
    // will grow as we experiment with more external applications.
    //

    private static final DataFlavor  FLAVOR_COLOR = newDataFlavor("application/x-color", InputStream.class);
    private static final DataFlavor  FLAVOR_IMAGE = DataFlavor.imageFlavor;
    private static final DataFlavor  FLAVOR_LOCAL_STRING = newDataFlavor(String.class);
    private static final DataFlavor  FLAVOR_PLAIN_TEXT = newDataFlavor("text/plain", String.class);
    private static final DataFlavor  FLAVOR_PLAIN_TEXT_UNICODE = DataFlavor.getTextPlainUnicodeFlavor();
    private static final DataFlavor  FLAVOR_YOIX = newDataFlavor(YoixObject.class);

    //
    // These are the flavors that represent objects that we can transfer out
    // of our application. FLAVOR_YOIX is our preferred flavor, so it should
    // be first. These flavors are used by the Transferable methods that are
    // defined in YoixObject, so additions to this list probably need to be
    // accompanied by code changes in YoixObject.getTransferData().
    //

    private static final DataFlavor  EXPORTFLAVORS[] = {
	FLAVOR_YOIX,
	//
	// This caused problems on a Mac in one of the clipboard tests.
	// Decided to comment it out for now and investigate later.
	//
	//	FLAVOR_IMAGE,		// this might be null
	//
	FLAVOR_LOCAL_STRING,
	DataFlavor.stringFlavor,
	FLAVOR_PLAIN_TEXT		// was DataFlavor.plainTextFlavor
    };

    //
    // These are the flavors that represent objects that we can transfer into
    // our application. FLAVOR_YOIX is our preferred flavor, so it should be
    // first. The others are ordered so the least likely matches come first.
    // This list is only used by the yoixTransferable() methods defined in
    // this class.
    //

    private static final DataFlavor  IMPORTFLAVORS[] = {
	FLAVOR_YOIX,
	FLAVOR_IMAGE,			// this might be null
	FLAVOR_COLOR,			// omitted from EXPORTFLAVORS
	FLAVOR_LOCAL_STRING,
	DataFlavor.stringFlavor,
	FLAVOR_PLAIN_TEXT,		// was: DataFlavor.plainTextFlavor
	FLAVOR_PLAIN_TEXT_UNICODE	// added 2/22/2007
    };

    private static final DataFlavor  IMPORTFLAVORS_TP[] = {	// text preferable order
	FLAVOR_YOIX,
	FLAVOR_LOCAL_STRING,
	DataFlavor.stringFlavor,
	FLAVOR_PLAIN_TEXT,         // was: DataFlavor.plainTextFlavor,
	FLAVOR_PLAIN_TEXT_UNICODE, // added: 2/22/2007
	FLAVOR_IMAGE,		   // this might be null
	FLAVOR_COLOR 		   // omitted from EXPORTFLAVORS
    };

    //
    // Some objects (e.g., a String) may be represented by more than one
    // DataFlavor, so we sometimes use a HashMap to help answer questions
    // like "does this flavor represent a String?".
    //

    private static HashMap  stringflavors = new HashMap(5);

    static {
	stringflavors.put(DataFlavor.stringFlavor, Boolean.TRUE);
	//stringflavors.put(DataFlavor.plainTextFlavor, Boolean.TRUE);
	stringflavors.put(FLAVOR_LOCAL_STRING, Boolean.TRUE);
	stringflavors.put(FLAVOR_PLAIN_TEXT, Boolean.TRUE);
	stringflavors.put(FLAVOR_PLAIN_TEXT_UNICODE, Boolean.TRUE);
    }

    ///////////////////////////////////
    //
    // YoixMiscDataTransfer Methods
    //
    ///////////////////////////////////

    static DataFlavor[]
    getExportFlavors() {

	DataFlavor  flavors[];
	DataFlavor  temp[];
	DataFlavor  flavor;
	int         m;
	int         n;

	//
	// Suspect it's not necessary, but we currently make sure there are
	// no null elements in the array that we return. Done because some
	// versions of Java might not recognize DataFlavor.imageFlavor, but
	// there probably could be others. Might be better if we did this
	// once in a static initialzation block, but this isn't used much
	// so it's not a high priority.
	//

	flavors = new DataFlavor[EXPORTFLAVORS.length];
	for (n = 0, m = 0; n < EXPORTFLAVORS.length; n++) {
	    if ((flavor = EXPORTFLAVORS[n]) != null)
		flavors[m++] = flavor;
	}
	if (m < flavors.length) {
	    temp = new DataFlavor[m];
	    System.arraycopy(flavors, 0, temp, 0, m);
	    flavors = temp;
	}
	return(flavors);
    }


    static boolean
    isColorFlavor(DataFlavor flavor) {

	return(flavor != null && flavor == FLAVOR_COLOR);
    }


    static boolean
    isFlavorExported(DataFlavor flavor) {

	boolean  supported = false;
	int      n;

	//
	// Currenty accepts any flavor mentioned in EXPORTFLAVORS, which may
	// not be what you want. If it's not right just get your own copy
	// of EXPORTFLAVORS (using getDataFlavors()) and examine the entries
	// in that array.
	//

	if (flavor != null) {
	    for (n = 0; n < EXPORTFLAVORS.length; n++) {
		if (EXPORTFLAVORS[n] == flavor) {
		    supported = true;
		    break;
		}
	    }
	}
	return(supported);
    }


    static boolean
    isImageFlavor(DataFlavor flavor) {

	return(flavor != null && flavor == FLAVOR_IMAGE);
    }


    static boolean
    isStringFlavor(DataFlavor flavor) {

	return(flavor != null && stringflavors.get(flavor) == Boolean.TRUE);
    }


    static boolean
    isYoixFlavor(DataFlavor flavor) {

	return(flavor != null && flavor == FLAVOR_YOIX);
    }


    static YoixObject
    yoixTransferable(Transferable transferable, boolean textpreferable) {

	YoixObject  obj = null;
	DataFlavor  flavors[];

	//
	// try IMPORTFLAVORS first
	//

	if (transferable != null) {
	    if (!(transferable instanceof YoixObject)) {
		if ((obj = yoixTransferable(transferable, textpreferable ? IMPORTFLAVORS_TP : IMPORTFLAVORS)) == null)
		    if ((obj = yoixReadTransferable(transferable)) == null)
			obj = yoixTransferable(transferable, transferable.getTransferDataFlavors());
	    } else obj = (YoixObject)transferable;
	}
	return(obj != null ? obj : YoixObject.newNull());
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    static DataFlavor
    newDataFlavor(Class source) {

	return(newDataFlavor(DataFlavor.javaJVMLocalObjectMimeType, source));
    }


    static DataFlavor
    newDataFlavor(String mimetype, Class source) {

	DataFlavor  flavor = null;

	try {
	    flavor = new DataFlavor(mimetype + "; class=" + source.getName(), null, source.getClassLoader());
	}
	catch(ClassNotFoundException e) {}
	return(flavor);
    }


    private static YoixObject
    yoixTransferable(Transferable transferable, DataFlavor flavors[]) {

	YoixObject  obj = null;
	DataFlavor  firstflavor = null;
	DataFlavor  flavor;
	Object      value;
	int         n;

	//
	// The "firstflavor" stuff is a small kludge that should let accept
	// colors from some external applications without requiring that we
	// write the code that builds the components of the color by reading
	// bytes from an InputStream.
	//

	if (transferable != null && flavors != null && flavors.length > 0) {
	    for (n = 0; n < flavors.length && obj == null; n++) {
		if ((flavor = flavors[n]) != null) {
		    try {
			if ((value = transferable.getTransferData(flavor)) != null) {
			    if ((obj = YoixMake.yoixObject(value)) != null) {
				if (firstflavor != null) {
				    if (isColorFlavor(firstflavor)) {
					if (obj.isString()) {
					    try {
						obj = YoixMake.yoixColor(Color.decode(obj.stringValue()));
					    }
					    catch(NumberFormatException e) {}
					}
				    }
				}
			    } else if (firstflavor == null)
				firstflavor = flavor;
			}
		    }
		    catch(UnsupportedFlavorException e) {}
		    catch(IOException e) {}
		}
	    }
	}
	return(obj);
    }


    private static YoixObject
    yoixReadTransferable(Transferable transferable) {

	StringBuffer  sbuf;
	YoixObject    obj = null;
	DataFlavor    flavor;
	DataFlavor    flavors[];
	String        value;
	Reader        reader;
	Object        input[];
	Object        tmp[];
	char          ctmp[];
	char          buffer[];
	int           count;
	int           size;
	int           m;
	int           n;

	if (transferable != null) {
	    size = 0;
	    if ((flavors = transferable.getTransferDataFlavors()) != null) {
		if ((flavor = DataFlavor.selectBestTextFlavor(flavors)) != null) {
		    n = 0;
		    input = null;
		    try {
			reader = flavor.getReaderForText(transferable);
			buffer = new char[BUFSIZ];
			input = new Object[BUFSIZ];
			while ((count = reader.read(buffer, 0, BUFSIZ)) >= 0) {
			    if (count != 0) {
				if (n == input.length) {
				    tmp = new Object[n + BUFSIZ];
				    System.arraycopy(input, 0, tmp, 0, n);
				    input = tmp;
				}
				input[n] = new char[count];
				System.arraycopy(buffer, 0, (char[])input[n], 0, count);
				n++;
				size += count;
			    }
			}
		    }
		    catch(Exception e) {
			VM.caughtException(e);
		    }
		    if (size > 0) {
			buffer = new char[size];
			count = 0;
			for (m = 0; m < n; m++) {
			    ctmp = (char[])input[m];
			    System.arraycopy(ctmp, 0, buffer, count, ctmp.length);
			    count += ctmp.length;
			}
			value = new String(buffer);
			obj = YoixObject.newString(value);
		    }
		}
	    }
	    if (size == 0) {
		//
		// We are desperate here, so we ask transferable
		// for its list supported flavors and try them.
		// We're not convinced about asking transferable.
		// We should continue to think about it some.
		//
		obj = yoixTransferable(transferable, flavors);
	    }
	}
	return(obj);
    }
}

