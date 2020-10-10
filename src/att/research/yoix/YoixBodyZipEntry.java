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
import java.io.*;
import java.util.*;
import java.util.zip.*;

final
class YoixBodyZipEntry extends YoixPointerActive

    implements YoixConstants

{

    private ZipEntry  zipentry;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD                OBJECT       BODY
     // -----                ------       ----
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(12);

    static {
	activefields.put(N_COMMENT, new Integer(V_COMMENT));
	activefields.put(N_COMPRESSEDSIZE, new Integer(V_COMPRESSEDSIZE));
	activefields.put(N_CRC, new Integer(V_CRC));
	activefields.put(N_EXTRA, new Integer(V_EXTRA));
	activefields.put(N_DEFLATED, new Integer(V_DEFLATED));
	activefields.put(N_NAME, new Integer(V_NAME));
	activefields.put(N_SIZE, new Integer(V_SIZE));
	activefields.put(N_TIMESTAMP, new Integer(V_TIMESTAMP));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyZipEntry(YoixObject data) {

	this(data, null);
    }


    YoixBodyZipEntry(YoixObject data, ZipEntry zipentry) {

	super(data);

	if (zipentry != null)
	    this.zipentry = zipentry;
	else buildZipEntry();

	setFixedSize();
	setPermissions(permissions);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(ZIPENTRY);
    }

    ///////////////////////////////////
    //
    // YoixBodyZipEntry Methods
    //
    ///////////////////////////////////

    protected final void
    finalize() {

	zipentry = null;
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	ZipEntry  zipentry;

	zipentry = this.zipentry;

	if (zipentry != null) {
	    switch (activeField(name, activefields)) {
		case V_COMMENT:
		    obj = YoixObject.newString(zipentry.getComment());
		    break;

		case V_COMPRESSEDSIZE:
		    obj = YoixObject.newDouble(zipentry.getCompressedSize());
		    break;

		case V_CRC:
		    obj = YoixObject.newDouble(zipentry.getCrc());
		    break;

		case V_DEFLATED:
		    obj = YoixObject.newInt(zipentry.getMethod() == ZipEntry.DEFLATED);
		    break;

		case V_EXTRA:
		    obj = YoixObject.newString(YoixMake.javaUTFString(zipentry.getExtra()));
		    break;

	        case V_NAME:
		    obj = YoixObject.newString(zipentry.getName());
		    break;

		case V_SIZE:
		    obj = YoixObject.newDouble(zipentry.getSize());
		    break;

		case V_TIMESTAMP:
		    obj = YoixObject.newDouble(zipentry.getTime()/1000.0);
		    break;
	    }
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(zipentry);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	ZipEntry  zipentry;
	ZipEntry  ze;
	String    sval;
	double    dval;
	byte      bval[];
	long      lval;
	int       ival;

	zipentry = this.zipentry;

	if (obj != null && obj.notNull()) {
	    if (zipentry == null) {
		data.put(name, obj, true);
		if (name.equals(N_NAME))
		    buildZipEntry();
	    } else {
		switch (activeField(name, activefields)) {
		    case V_COMMENT:
			sval = obj.stringValue();
			if (YoixConverter.utf8Length(sval) <= 0xFFFF)
			    zipentry.setComment(sval);
			else VM.abort(BADVALUE, N_COMMENT);
			break;

		    case V_CRC:
			dval = obj.doubleValue();
			if (dval >= 0 && (long)dval <= 0xFFFFFFFFL)
			    zipentry.setCrc((long)dval);
			else VM.abort(BADVALUE, N_CRC);
			break;

		    case V_DEFLATED:
			if (obj.intValue() != 0)
			    zipentry.setMethod(ZipEntry.DEFLATED);
			else zipentry.setMethod(ZipEntry.STORED);
			break;

		    case V_EXTRA:
			sval = obj.stringValue();
			bval = YoixMake.javaUTFByteArray(sval);
			if (bval.length <= 0xFFFF) {
			    if (bval.length > 0)
				zipentry.setExtra(bval);
			     else zipentry.setExtra(null);
			} else VM.abort(BADVALUE, N_EXTRA);
			break;

		    case V_NAME:
			sval = obj.stringValue();
			if (YoixConverter.utf8Length(sval) <= 0xFFFF) {
			    ze = new ZipEntry(sval);
			    ze.setComment(zipentry.getComment());
			    if ((lval = zipentry.getCrc()) >= 0)
				ze.setCrc(lval);
			    ze.setExtra(zipentry.getExtra());
			    if ((lval = zipentry.getMethod()) != -1)
				ze.setMethod((int)lval);
			    if ((lval = zipentry.getSize()) >= 0)
				ze.setSize(lval);
			    zipentry.setTime(System.currentTimeMillis());
			    this.zipentry = ze;
			} else VM.abort(BADVALUE, N_NAME);
			break;

		    case V_SIZE:
			dval = obj.doubleValue();
			if (dval >= 0 && (long)dval <= 0xFFFFFFFFL)
			    zipentry.setSize((long)dval);
			else VM.abort(BADVALUE, N_SIZE);
			break;

		    case V_TIMESTAMP:
			dval = obj.doubleValue() * 1000.0;
			zipentry.setTime((long)dval);
			break;
		}
	    }
	}

	return(obj);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildZipEntry() {

	ZipEntry  zipentry = null;
	String    comment = data.getString(N_COMMENT, "");
	String    extra = data.getString(N_EXTRA, "");
	String    name = data.getString(N_NAME, "");
	double    crc = data.getDouble(N_CRC, -1);
	double    size = data.getDouble(N_SIZE, -1);
	double    time = data.getDouble(N_TIMESTAMP, -1);
	byte      bval[];
	int       deflated = data.getInt(N_DEFLATED, 1);

	if (name.length() != 0) {
	    if (YoixConverter.utf8Length(name) <= 0xFFFF) {
		zipentry = new ZipEntry(name);
		if (comment.length() > 0) {
		    if (YoixConverter.utf8Length(comment) <= 0xFFFF)
			zipentry.setComment(comment);
		    else VM.abort(BADVALUE, N_COMMENT);
		}
		if (crc >= 0) {
		    if ((long)crc <= 0xFFFFFFFFL)
			zipentry.setCrc((long)crc);
		    else VM.abort(BADVALUE, N_CRC);
		}

		if (extra.length() > 0) {
		    bval = YoixMake.javaUTFByteArray(extra);
		    if (bval.length <= 0xFFFF) {
			if (bval.length > 0)
			    zipentry.setExtra(bval);
			else zipentry.setExtra(null);
		    } else VM.abort(BADVALUE, N_EXTRA);
		}

		zipentry.setMethod(deflated != 0 ? ZipEntry.DEFLATED : ZipEntry.STORED);

		if (size >= 0) {
		    if ((long)size <= 0xFFFFFFFFL)
			zipentry.setSize((long)size);
		    else VM.abort(BADVALUE, N_SIZE);
		}

		if (time == -1)
		    time = System.currentTimeMillis();
		else time *= 1000.0;

		zipentry.setTime((long)time);
		this.zipentry = zipentry;
	    } else VM.abort(BADVALUE, N_NAME);
	}
    }
}

