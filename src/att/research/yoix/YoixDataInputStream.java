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

final
class YoixDataInputStream

    implements DataInput,
	       YoixConstants

{

    //
    // Class that hides the discrepancies between Java's InputStream
    // and RandomAccessFile, so we don't have to worry about what's
    // being used as the underlying input source.
    //

    private DataInputStream       distream = null;
    private RandomAccessFile      rafile = null;
    private YoixDataOutputStream  lostream = null;
    private boolean               lastUsed = false;

    private long                  markpos = -1;
    private long                  marklimit = -1;

    private static final String   UNREADABLE = "stream not opened for reading";
    private static final String   UNSUPPORTED = "not supported by this stream";

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixDataInputStream(InputStream input) {

	if (input != null) {
	    if (input instanceof DataInputStream)
		distream = (DataInputStream)input;
	    else distream = new DataInputStream(input);
	} else VM.abort(INTERNALERROR);
    }


    YoixDataInputStream(YoixObject input, String encoding) {

	byte  bytes[];

	if (input != null) {
	    try {
		bytes = input.stringValue().getBytes(encoding);
		distream = new DataInputStream(new ByteArrayInputStream(bytes));
	    }
	    catch(UnsupportedEncodingException e) {
		VM.abort(BADENCODING, encoding);
	    }
	} else VM.abort(INTERNALERROR);
    }


    YoixDataInputStream(RandomAccessFile input) {

	this(input, null);
    }


    YoixDataInputStream(RandomAccessFile input, YoixDataOutputStream output) {

	if (input != null) {
	    rafile = input;
	    if (output != null) {
		if (output.isRandomAccess()) {
		    if (output.usesThisRandomAccessFile(rafile)) {
			lostream = output;
			lostream.linkTo(this);
		    } else VM.abort(INTERNALERROR);
		} else VM.abort(INTERNALERROR);
	    }
	} else VM.abort(INTERNALERROR);
    }

    ///////////////////////////////////
    //
    // DataInput Methods
    //
    ///////////////////////////////////

    public final boolean
    readBoolean()

	throws EOFException, IOException

    {

	if (distream != null)
	    return(distream.readBoolean());
	else if (rafile != null)
	    return(rafile.readBoolean());
	else throw(new IOException(UNREADABLE));
    }


    public final byte
    readByte()

	throws EOFException, IOException

    {

	if (distream != null)
	    return(distream.readByte());
	else if (rafile != null)
	    return(rafile.readByte());
	else throw(new IOException(UNREADABLE));
    }


    public final char
    readChar()

	throws EOFException, IOException

    {

	if (distream != null)
	    return(distream.readChar());
	else if (rafile != null)
	    return(rafile.readChar());
	else throw(new IOException(UNREADABLE));
    }


    public final double
    readDouble()

	throws EOFException, IOException

    {

	if (distream != null)
	    return(distream.readDouble());
	else if (rafile != null)
	    return(rafile.readDouble());
	else throw(new IOException(UNREADABLE));
    }


    public final float
    readFloat()

	throws EOFException, IOException

    {

	if (distream != null)
	    return(distream.readFloat());
	else if (rafile != null)
	    return(rafile.readFloat());
	else throw(new IOException(UNREADABLE));
    }


    public final void
    readFully(byte b[])

	throws EOFException, IOException

    {

	if (distream != null)
	    distream.readFully(b);
	else if (rafile != null)
	    rafile.readFully(b);
	else throw(new IOException(UNREADABLE));
    }


    public final void
    readFully(byte b[], int off, int len)

	throws EOFException, IOException

    {

	if (distream != null)
	    distream.readFully(b, off, len);
	else if (rafile != null)
	    rafile.readFully(b, off, len);
	else throw(new IOException(UNREADABLE));
    }


    public final int
    readInt()

	throws EOFException, IOException

    {

	if (distream != null)
	    return(distream.readInt());
	else if (rafile != null)
	    return(rafile.readInt());
	else throw(new IOException(UNREADABLE));
    }


    public final String
    readLine()

	throws IOException

    {

	if (distream != null) {
	    // want to avoid deprecation compilation message, so bag it and
	    // be less than helpful
	    //return(distream.readLine());
	    throw(new IOException("readLine is deprecated for DataInputStreams"));
	} else if (rafile != null)
	    return(rafile.readLine());
	else throw(new IOException(UNREADABLE));
    }


    public final long
    readLong()

	throws EOFException, IOException

    {

	if (distream != null)
	    return(distream.readLong());
	else if (rafile != null)
	    return(rafile.readLong());
	else throw(new IOException(UNREADABLE));
    }


    public final short
    readShort()

	throws EOFException, IOException

    {

	if (distream != null)
	    return(distream.readShort());
	else if (rafile != null)
	    return(rafile.readShort());
	else throw(new IOException(UNREADABLE));
    }


    public final int
    readUnsignedByte()

	throws EOFException, IOException

    {

	if (distream != null)
	    return(distream.readUnsignedByte());
	else if (rafile != null)
	    return(rafile.readUnsignedByte());
	else throw(new IOException(UNREADABLE));
    }


    public final int
    readUnsignedShort()

	throws EOFException, IOException

    {

	if (distream != null)
	    return(distream.readUnsignedShort());
	else if (rafile != null)
	    return(rafile.readUnsignedShort());
	else throw(new IOException(UNREADABLE));
    }


    public final String
    readUTF()

	throws EOFException, IOException, UTFDataFormatException

    {

	if (distream != null)
	    return(distream.readUTF());
	else if (rafile != null)
	    return(rafile.readUTF());
	else throw(new IOException(UNREADABLE));
    }


    public final int
    skipBytes(int n)

	throws EOFException, IOException

    {

	if (distream != null)
	    return(distream.skipBytes(n));
	else if (rafile != null)
	    return(rafile.skipBytes(n));
	else throw(new IOException(UNREADABLE));
    }

    ///////////////////////////////////
    //
    // YoixDataInputStream Methods
    //
    ///////////////////////////////////

    final DataInputStream
    accessDataInputStream() {

	return(distream);
    }


    final int
    available()

	throws IOException

    {

	if (distream != null)
	    return(distream.available());
	else if (rafile != null)
	    return((int)(rafile.length() - rafile.getFilePointer()));
	else throw(new IOException(UNREADABLE));
    }


    final boolean
    bufferResetNeeded() {

	return(rafile != null ? !lastUsed : false);
    }


    final void
    close()

	throws IOException

    {

	if (distream != null) {
	    distream.close();
	    distream = null;
	} else if (rafile != null) {
	    if (lostream == null)
		rafile.close();
	    else lostream.unlinkFrom(this);
	    rafile = null;
	} else throw(new IOException(UNREADABLE));
    }


    final long
    getOffset()

	throws IOException

    {

	if (distream != null)
	    throw(new IOException("getOffset " + UNSUPPORTED));
	else if (rafile != null)
	    return(rafile.getFilePointer());
	else throw(new IOException(UNREADABLE));
    }


    final boolean
    isRandomAccess() {

	return(rafile != null);
    }


    final void
    linkTo(YoixDataOutputStream output) {

	if (rafile == null)
	    VM.abort(INTERNALERROR);
	else if (lostream == null) {
	    if (output.usesThisRandomAccessFile(rafile))
		lostream = output;
	    else VM.abort(INTERNALERROR);
	}

	if (lostream != output)
	    VM.abort(INTERNALERROR);
    }


    final synchronized void
    mark(int readlimit) {

	if (distream != null)
	    distream.mark(readlimit);
	else if (rafile != null) {
	    marklimit = readlimit; // marklimit is not really used
	    try {
		markpos = rafile.getFilePointer();
	    }
	    catch(IOException e) {
		markpos = -1;
		VM.caughtException(e);
	    }
	}
    }


    final boolean
    markSupported() {

	if (distream != null)
	    return(distream.markSupported());
	else if (rafile != null)
	    return(true);
	else return(false);
    }


    final int
    read()

	throws IOException

    {

	if (distream != null)
	    return(distream.read());
	else if (rafile != null)
	    return(rafile.read());
	else throw(new IOException(UNREADABLE));
    }


    final int
    read(byte b[])

	throws IOException

    {

	if (distream != null)
	    return(distream.read(b));
	else if (rafile != null)
	    return(rafile.read(b));
	else throw(new IOException(UNREADABLE));
    }


    final int
    read(byte b[], int off, int len)

	throws IOException

    {

	if (distream != null)
	    return(distream.read(b, off, len));
	else if (rafile != null)
	    return(rafile.read(b, off, len));
	else throw(new IOException(UNREADABLE));
    }


    final boolean
    ready() {

	try {
	    return(available() > 0);
	}
	catch(IOException e) {
	    VM.caughtException(e);
	    return(false);
	}
    }


    final synchronized void
    reset()

	throws IOException

    {

	if (distream != null)
	    distream.reset();
	else if (rafile != null) {
	    if (markpos >= 0)
		rafile.seek(markpos);
	    else throw(new IOException("Resetting to invalid mark"));
	} else throw(new IOException(UNREADABLE));
    }


    final void
    seek(long pos)

	throws IOException

    {

	if (distream != null)
	    throw(new IOException("seek " + UNSUPPORTED));
	else if (rafile != null) {
	    rafile.seek(pos);
	    // indicate that a buffer reset is definitely needed
	    lastUsed = false;
	    if (lostream != null)
		lostream.toggleUse(false);
	} else throw(new IOException(UNREADABLE));
    }


    final boolean
    seekSupported() {

	return(rafile != null);
    }


    final long
    skip(long n)

	throws IOException

    {

	if (distream != null)
	    return(distream.skip(n));
	else if (rafile != null) {
	    long pos = rafile.getFilePointer();
	    long len = rafile.length();
	    if (pos + n > len) {
		rafile.seek(len);
		n = len - pos;
	    } else rafile.seek(pos + n);
	    return(n);
	} else throw(new IOException(UNREADABLE));
    }


    final void
    toggleUse(boolean local) {

	if (local) {
	    lastUsed = true;
	    if (lostream != null)
		lostream.toggleUse(false);
	} else lastUsed = false;
    }


    final long
    truncate(long len)

	throws IOException

    {

	if (distream != null)
	    throw(new IOException("truncate " + UNSUPPORTED));
	else if (rafile != null) {
	    rafile.setLength(len);
	    // indicate that a buffer reset is definitely needed
	    lastUsed = false;
	    if (lostream != null)
		lostream.toggleUse(false);
	} else throw(new IOException(UNREADABLE));

	return(rafile.length());
    }


    final void
    unlinkFrom(YoixDataOutputStream output) {

	if (rafile != null) {
	    if (lostream == output)
		lostream = null;
	    else if (lostream != null)
		VM.abort(INTERNALERROR);
	} else VM.abort(INTERNALERROR);
    }


    final boolean
    usesThisRandomAccessFile(RandomAccessFile input) {

	return(rafile == input);
    }
}

