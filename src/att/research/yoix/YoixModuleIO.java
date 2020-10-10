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
import java.nio.charset.*;
import java.util.*;
import java.util.zip.*;

abstract
class YoixModuleIO extends YoixModule

    implements YoixConstantsStream

{

    static String  $MODULENAME = M_IO;

    static Integer  $FLUSHDISABLED = new Integer(FLUSHDISABLED);
    static Integer  $FLUSHLINES = new Integer(FLUSHLINES);
    static Integer  $FLUSHWRITES = new Integer(FLUSHWRITES);

    static Integer  $ADLER32 = new Integer(ADLER32);
    static Integer  $BEST_COMPRESSION = new Integer(Deflater.BEST_COMPRESSION);
    static Integer  $BEST_SPEED = new Integer(Deflater.BEST_SPEED);
    static Integer  $BSD = new Integer(BSD);
    static Integer  $CRC32 = new Integer(CRC32);
    static Integer  $CHECKSUM = new Integer(CHECKSUM);
    static Integer  $DEFLATED = new Integer(ZipOutputStream.DEFLATED);
    static Integer  $DFLT_COMPRESSION = new Integer(Deflater.DEFAULT_COMPRESSION);
    static Integer  $GZIP = new Integer(GZIP);
    static Integer  $NO_COMPRESSION = new Integer(Deflater.NO_COMPRESSION);
    static Integer  $STORED = new Integer(ZipOutputStream.STORED);
    static Integer  $SYSV = new Integer(SYSV);
    static Integer  $ZIPPED = new Integer(ZIPPED);

    static Integer  $HEXCODER = new Integer(HEXCODER);
    static Integer  $IETFCODER = new Integer(IETFCODER);
    static Integer  $LINEDHEXCODER = new Integer(LINEDHEXCODER);
    static Integer  $MIMECODER = new Integer(MIMECODER);

    static Integer  $EXECUTE = new Integer(EXECUTE);
    static Integer  $LOCK = new Integer(LOCK);
    static Integer  $READ = new Integer(READ);
    static Integer  $WRITE = new Integer(WRITE);
    static Integer  $APPEND = new Integer(APPEND);
    static Integer  $UPDATE = new Integer(UPDATE);

    static String   $ISO8859_1 = YoixConverter.getISO88591Encoding();
    static String   $UTF8 = YoixConverter.getUTF8Encoding();

    static Object  $module[] = {
    //
    // NAME                    ARG                  COMMAND     MODE   REFERENCE
    // ----                    ---                  -------     ----   ---------
       null,                   "59",                $LIST,      $RORO, $MODULENAME,
       "ADLER32",              $ADLER32,            $INTEGER,   $LR__, null,
       "APPEND",               $APPEND,             $INTEGER,   $LR__, null,
       "BEST_COMPRESSION",     $BEST_COMPRESSION,   $INTEGER,   $LR__, null,
       "BEST_SPEED",           $BEST_SPEED,         $INTEGER,   $LR__, null,
       "BSD",                  $BSD,                $INTEGER,   $LR__, null,
       "BUFSIZ",               $BUFSIZ,             $INTEGER,   $LR__, null,
       "CHECKSUM",             $CHECKSUM,           $INTEGER,   $LR__, null,
       "CRC32",                $CRC32,              $INTEGER,   $LR__, null,
       "DEFAULT_COMPRESSION",  $DFLT_COMPRESSION,   $INTEGER,   $LR__, null,
       "DEFLATED",             $DEFLATED,           $INTEGER,   $LR__, null,
       "EXECUTE",              $EXECUTE,            $INTEGER,   $LR__, null,
       "FILE",                 $FILE,               $INTEGER,   $LR__, null,
       "FLUSHDISABLED",        $FLUSHDISABLED,      $INTEGER,   $LR__, null,
       "FLUSHLINES",           $FLUSHLINES,         $INTEGER,   $LR__, null,
       "FLUSHWRITES",          $FLUSHWRITES,        $INTEGER,   $LR__, null,
       "GZIP",                 $GZIP,               $INTEGER,   $LR__, null,
       "HEXCODER",             $HEXCODER,           $INTEGER,   $LR__, null,
       "IETFCODER",            $IETFCODER,          $INTEGER,   $LR__, null,
       "ISO8859_1",            $ISO8859_1,          $STRING,    $RORO, null,
       "LINEDHEXCODER",        $LINEDHEXCODER,      $INTEGER,   $LR__, null,
       "LOCK",                 $LOCK,               $INTEGER,   $LR__, null,
       "MIMECODER",            $MIMECODER,          $INTEGER,   $LR__, null,
       "NO_COMPRESSION",       $NO_COMPRESSION,     $INTEGER,   $LR__, null,
       "READ",                 $READ,               $INTEGER,   $LR__, null,
       "STORED",               $STORED,             $INTEGER,   $LR__, null,
       "STRINGSTREAM",         $STRINGSTREAM,       $INTEGER,   $LR__, null,
       "SYSV",                 $SYSV,               $INTEGER,   $LR__, null,
       "UPDATE",               $UPDATE,             $INTEGER,   $LR__, null,
       "URL",                  $URL,                $INTEGER,   $LR__, null,
       "UTF8",                 $UTF8,               $STRING,    $RORO, null,
       "WRITE",                $WRITE,              $INTEGER,   $LR__, null,
       "ZIPPED",               $ZIPPED,             $INTEGER,   $LR__, null,
       "available",            "1",                 $BUILTIN,   $LR_X, null,
       "chkstr",               "1",                 $BUILTIN,   $LR_X, null,
       "close",                "-1",                $BUILTIN,   $LR_X, null,
       "closeEntry",           "1",                 $BUILTIN,   $LR_X, null,
       "getAvailableCharsets", "",                  $BUILTIN,   $LR_X, null,
       "getZipEntries",        "1",                 $BUILTIN,   $LR_X, null,
       "getZipMember",         "2",                 $BUILTIN,   $LR_X, null,
       "mark",                 "2",                 $BUILTIN,   $LR_X, null,
       "offsetBytes",          "-1",                $BUILTIN,   $LR_X, null,
       "offsetSupported",      "1",                 $BUILTIN,   $LR_X, null,
       "open",                 "-2",                $BUILTIN,   $LR_X, null,
       "read",                 "3",                 $BUILTIN,   $LR_X, null,
       "readChar",             "1",                 $BUILTIN,   $LR_X, null,
       "readInto",             "-2",                $BUILTIN,   $LR_X, null,
       "readLine",             "1",                 $BUILTIN,   $LR_X, null,
       "readStream",           "-1",                $BUILTIN,   $LR_X, null,
       "ready",                "1",                 $BUILTIN,   $LR_X, null,
       "reopen",               "2",                 $BUILTIN,   $LR_X, null,
       "reset",                "1",                 $BUILTIN,   $LR_X, null,
       "setZipComment",        "2",                 $BUILTIN,   $LR_X, null,
       "setZipLevel",          "2",                 $BUILTIN,   $LR_X, null,
       "tellCount",            "-1",                $BUILTIN,   $LR_X, null,
       "truncateBytes",        "2",                 $BUILTIN,   $LR_X, null,
       "write",                "-2",                $BUILTIN,   $LR_X, null,
       "writeChar",            "2",                 $BUILTIN,   $LR_X, null,
       "writeFrom",            "-2",                $BUILTIN,   $LR_X, null,
       "writeLine",            "2",                 $BUILTIN,   $LR_X, null,
    };

    ///////////////////////////////////
    //
    // YoixModuleIO Methods
    //
    ///////////////////////////////////

    public static YoixObject
    available(YoixObject arg[]) {

	int  available = 0;

	if (arg[0].isStream() && arg[0].notNull())
	    available = arg[0].streamValue().available();
	else VM.badArgument(0);

	return(YoixObject.newInt(available));
    }


    public static YoixObject
    chkstr(YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg[0].isDouble() && arg[0].notNull())
	    obj = YoixObject.newString(YoixChecksum.chkstr(arg[0].doubleValue()));
	else VM.badArgument(0);

	return(obj);
    }


    public static YoixObject
    close(YoixObject arg[]) {

	boolean  result = false;

	//
	// Decided to initially only accept a stream argument, but left
	// most of the code that supports the mode argument in for now.
	//

	if (arg.length == 1) {		// currently rejects second argument
	    if (arg[0].isStream() && arg[0].notNull()) {
		if (arg.length == 2) {
		    if (arg[1].isInteger())
			result = arg[0].streamValue().close(arg[1].intValue());
		    else if (arg[1].isString() && arg[1].notNull())
			result = arg[0].streamValue().close(arg[1].stringValue());
		    else VM.badArgument(1);
		} else result = arg[0].streamValue().close();
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(result ? 0 : -1));
    }


    public static YoixObject
    closeEntry(YoixObject arg[]) {

	boolean result = false;

	if (arg[0].isStream() && arg[0].notNull()) {
	    result = arg[0].streamValue().closeEntry();
	} else VM.badArgument(0);

	return(YoixObject.newInt(result ? 0 : -1));
    }


    public static YoixObject
    getAvailableCharsets(YoixObject arg[]) {

	YoixObject  obj = null;
	SortedMap   sm;
	Charset     charset;
	String      names[];
	String      name;
	Set         keyset;
	int         n;

	if (arg.length == 0 || (arg.length == 1 && arg[0].isNull())) {
	    if ((sm = Charset.availableCharsets()) != null) {
		if ((keyset = sm.keySet()) != null) {
		    if ((names = (String[])keyset.toArray(new String[0])) != null) {
			obj = YoixObject.newArray(names.length);
			for (n = 0; n < names.length; n++)
			    obj.putString(n, names[n]);
		    }
		}
	    }
	} else if (arg.length == 1) {
	    if (arg[0].isString()) {
		name = arg[0].stringValue();
		try {
		    if (Charset.isSupported(name)) {
			if ((charset = Charset.forName(name)) != null) {
			    keyset = charset.aliases();
			    if ((names = (String[])keyset.toArray(new String[0])) != null) {
				obj = YoixObject.newArray(names.length + 1);
				obj.putString(0, charset.name());
				for (n = 0; n < names.length; n++)
				    obj.putString(n+1, names[n]);
			    }
			}
		    }
		}
		catch(Exception e) {
		    obj = null;
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj != null ? obj : YoixObject.newArray());
    }


    public static YoixObject
    getZipEntries(YoixObject arg[]) {

	Enumeration  enm;
	YoixObject   dict = null;
	ZipEntry     ze = null;
	ZipFile      zf = null;

	if (arg[0].isString() && arg[0].notNull()) {
	    try {
		zf = new ZipFile(YoixMisc.toYoixPath(arg[0].stringValue()));
	    }
	    catch(IOException e) {
		VM.caughtException(e);
	    }
	    if (zf != null) {
		dict = YoixObject.newDictionary(0);
		dict.setGrowable(true);
		enm = zf.entries();
		while (enm.hasMoreElements()) {
		    ze = (ZipEntry)enm.nextElement();
		    dict.put(ze.getName(), YoixObject.newZipEntry(ze), false);
		}
		dict.setGrowable(false);
	    }
	} else VM.badArgument(0);

	return(dict == null ? YoixObject.newDictionary() : dict);
    }


    public static YoixObject
    getZipMember(YoixObject arg[]) {

	InputStream  istream = null;
	YoixObject   stream = null;
	YoixObject   data;
	ZipEntry     ze;
	ZipFile      zf = null;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		try {
		    zf = new ZipFile(YoixMisc.toYoixPath(arg[0].stringValue()));
		}
		catch(IOException e) {
		    VM.caughtException(e);
		}
		if (zf != null) {
		    if ((ze = zf.getEntry(arg[1].stringValue())) != null) {
			try {
			    istream = zf.getInputStream(ze);
			}
			catch(IOException e) {
			    VM.caughtException(e, true, true);
			}
			data = VM.getTypeTemplate(T_FILE);
			data.putString(N_NAME, zf.getName());
			data.putInt(N_MODE, READ);
			data.putInt(N_OPEN, true);
			stream = YoixObject.newStream(data, istream);
		    } else VM.badArgument(1);
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(stream == null ? YoixObject.newStream() : stream);
    }


    public static YoixObject
    mark(YoixObject arg[]) {

	boolean  result = false;

	if (arg[0].isStream() && arg[0].notNull()) {
	    if (arg[1].isInteger())
		result = arg[0].streamValue().mark(arg[1].intValue());
	    else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    offsetBytes(YoixObject arg[]) {

	long  offset = -1;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isStream() && arg[0].notNull()) {
		if (arg[0].streamValue().offsetSupported()) {
		    if (arg.length == 2) {
			if (arg[1].isNumber())
			    offset = arg[0].streamValue().offsetBytes((long)arg[1].doubleValue());
			else VM.badArgument(1);
		    } else offset = arg[0].streamValue().offsetBytes();
		} else VM.badArgument(0);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newDouble((double)offset));
    }


    public static YoixObject
    offsetSupported(YoixObject arg[]) {

	boolean  result = false;

	if (arg[0].isStream() && arg[0].notNull())
	    result = arg[0].streamValue().offsetSupported();
	else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    open(YoixObject arg[]) {

	YoixObject  obj = null;
	YoixObject  data = null;
	YoixObject  path;
	YoixObject  mode;
	Object      body;
	int         type;
	int         n;

	if (arg.length >= 2) {
	    if (arg[0].isNull() || arg[0].isString()) {
		if ((arg[1].isString() && arg[1].notNull()) || arg[1].isInteger()) {
		    path = arg[0];
		    mode = arg[1];
		    n = 2;
		    if (arg.length > n && arg[n].isStream()) {
			obj = arg[n++];
			obj.putInt(N_OPEN, false);
			if (n < arg.length) {
			    if (n < arg.length - 1) {
				for (; n < arg.length - 1; n += 2) {
				    if (arg[n].isString() && arg[n].notNull())
					obj.putObject(arg[n].stringValue(), arg[n+1]);
				    else VM.badArgument(n);
				}
			    } else if (arg[n].notNull()) {
				if (arg[n].isDictionary())
				    YoixMisc.copyInto(arg[n], obj);
				else VM.badArgument(n);
			    }
			}
			obj.put(N_NAME, path);
			obj.put(N_MODE, mode);
			obj.putInt(N_OPEN, true);
		    } else {
			if (arg.length > n && arg[n].isInteger())
			    type = arg[n++].intValue();
			else if (path.notNull())
			    type = YoixMisc.guessStreamType(path.stringValue());
			else type = STRINGSTREAM;

			switch (type) {
			    case FILE:
				if (path.notNull())
				    data = VM.getTypeTemplate(T_FILE);
				else VM.badArgumentValue(0);
				break;

			    case STRINGSTREAM:
				data = VM.getTypeTemplate(T_STRINGSTREAM);
				break;

			    case URL:
				if (path.notNull())
				    data = VM.getTypeTemplate(T_URL);
				else VM.badArgumentValue(0);
				break;

			    default:
				data = VM.badArgument(2);
				break;
			}
			//
			// Now accepts key/value pairs or a single dictionary
			// argument - changed on 6/24/07.
			//
			if (n < arg.length) {
			    if (n < arg.length - 1) {
				for (; n < arg.length - 1; n += 2) {
				    if (arg[n].isString() && arg[n].notNull())
					data.putObject(arg[n].stringValue(), arg[n+1]);
				    else VM.badArgument(n);
				}
			    } else if (arg[n].notNull()) {
				if (arg[n].isDictionary())
				    YoixMisc.copyInto(arg[n], data);
				else VM.badArgument(n);
			    }
			}
			data.put(N_NAME, path);
			data.put(N_MODE, mode);
			data.putInt(N_OPEN, true);
			obj = YoixObject.newStream(data);
		    }
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return((obj != null && obj.getBoolean(N_OPEN)) ? obj : YoixObject.newStream());
    }


    public static YoixObject
    read(YoixObject arg[]) {

	String  buf;
	int     value = -1;

	if (arg[0].isStream() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		if (arg[2].isInteger() && arg[2].intValue() >= 0) {
		    buf = arg[0].streamValue().read(arg[2].intValue());
		    if (buf != null) {
			if ((value = buf.length()) > 0)
			    arg[1].overlay(buf);
		    } else value = -1;
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    readChar(YoixObject arg[]) {

	int  value = -1;

	if (arg[0].isStream() && arg[0].notNull())
	    value = arg[0].streamValue().read();
	else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    readInto(YoixObject arg[]) {

	YoixBodyStream  stream;
	char            buf[];
	int             count = -1;
	int             offset;
	int             length;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isStream() && arg[0].notNull()) {
		if (arg[1].isString() && arg[1].notNull()) {
		    if (arg.length <= 2 || arg[2].isInteger()) {
			if ((buf = arg[1].toCharArray(true, null)) != null) {
			    offset = arg[1].offset();
			    length = buf.length - offset;
			    if (arg.length > 2)
				length = Math.min(arg[2].intValue(), length);
			    if (length > 0) {
				stream = arg[0].streamValue();
				count = stream.readInto(buf, offset, length, -1, null);
			    } else count = (length == 0) ? 0 : -1;
			} else VM.abort(INVALIDACCESS);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(count));
    }


    public static YoixObject
    readLine(YoixObject arg[]) {

	String  str = null;

	if (arg[0].isStream() && arg[0].notNull())
	    str = arg[0].streamValue().readLine(EOL_MASK);
	else VM.badArgument(0);

	return(YoixObject.newString(str));
    }


    public static YoixObject
    readStream(YoixObject arg[]) {

	YoixBodyStream  stream;
	char            buf[] = null;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isStream() && arg[0].notNull()) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    stream = arg[0].streamValue();
		    buf = stream.readStream(arg.length == 1 ? -1 : arg[1].intValue());
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(buf));
    }


    public static YoixObject
    ready(YoixObject arg[]) {

	boolean  result = false;

	if (arg[0].isStream() && arg[0].notNull())
	    result = arg[0].streamValue().ready();
	else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    reopen(YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg[0].isStream() && arg[0].notNull()) {
	    if (arg[1].isStream() && arg[1].notNull()) {
		if (arg[1].streamValue().reopen(arg[0].streamValue()))
		    obj = arg[1];
		else obj = YoixObject.newStream();
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(obj);
    }


    public static YoixObject
    reset(YoixObject arg[]) {

	boolean  result = false;

	if (arg[0].isStream() && arg[0].notNull())
	    result = arg[0].streamValue().reset();
	else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    setZipComment(YoixObject arg[]) {

	int  result = 0;

	if (arg[0].isStream() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		result = arg[0].streamValue().setZipComment(arg[1].stringValue());
		if (result != 0)
		    VM.badArgument(0);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    setZipLevel(YoixObject arg[]) {

	int  result = 0;

	if (arg[0].isStream() && arg[0].notNull()) {
	    if (arg[1].isInteger()) {
		result = arg[0].streamValue().setZipLevel(arg[1].intValue());
		if (result != 0)
		    VM.badArgument(0);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    tellCount(YoixObject arg[]) {

	YoixBodyStream  stream;
	double          count = -1;
	int             mode = -1;

	if (arg[0].isStream()) {
	    stream = arg[0].streamValue();
	    if (arg.length > 1) {
		if (arg[1].isInteger()) {
		    mode = arg[1].intValue();
		    if (mode != READ && mode != WRITE)
			VM.badArgumentValue(1);
		} else VM.badArgument(1);
	    } else {
		if (stream.checkMode(READ))
		    mode = READ;
		else if (stream.checkMode(WRITE))
		    mode = WRITE;
	    }
	    if (mode == READ)
		count = (double)arg[0].streamValue().getInputCount();
	    else if (mode == WRITE)
		count = (double)arg[0].streamValue().getOutputCount();
	} else VM.badArgument(0);

	return(YoixObject.newInt((int)count));
    }


    public static YoixObject
    truncateBytes(YoixObject arg[]) {

	long  length = -1;

	if (arg.length == 2) {
	    if (arg[0].isStream() && arg[0].notNull()) {
		// only works on a offsetSupported stream
		if (arg[0].streamValue().offsetSupported()) {
		    if (arg[1].isNumber()) {
			length = arg[0].streamValue().setLength((long)arg[1].doubleValue());
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newDouble((double)length));
    }


    public static YoixObject
    write(YoixObject arg[]) {

	char  buf[];
	int   count = -1;
	int   len;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isStream() && arg[0].notNull()) {
		if (arg[1].isString() && arg[1].notNull()) {
		    if (arg.length == 3) {
			if (arg[2].isInteger()) {
			    buf = arg[1].toCharArray();
			    len = Math.min(Math.max(arg[2].intValue(), 0), buf.length);
			    count = arg[0].streamValue().write(buf, 0, len);
			} else VM.badArgument(2);
		    } else count = arg[0].streamValue().write(arg[1].stringValue());
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(count));
    }


    public static YoixObject
    writeChar(YoixObject arg[]) {

	int  count = -1;

	if (arg[0].isStream() && arg[0].notNull()) {
	    if (arg[1].isInteger())
		count = arg[0].streamValue().write(arg[1].intValue());
	    else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(count));
    }


    public static YoixObject
    writeFrom(YoixObject arg[]) {

	char  buf[];
	int   count = -1;
	int   len;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isStream() && arg[0].notNull()) {
		if (arg[1].isString() && arg[1].notNull()) {
		    if (arg.length == 2 || arg[2].isInteger()) {
			if ((buf = arg[1].toCharArray(true, null)) != null) {
			    if (arg.length > 2)
				len = Math.min(Math.max(arg[2].intValue(), 0), buf.length);
			    else len = buf.length;
			    count = arg[0].streamValue().write(buf, 0, len);
			} else VM.abort(INVALIDACCESS);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(count));
    }


    public static YoixObject
    writeLine(YoixObject arg[]) {

	int  count = -1;

	if (arg[0].isStream() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull())
		count = arg[0].streamValue().writeLine(arg[1].stringValue());
	    else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(count));
    }
}

