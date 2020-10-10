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

class YoixDataOutputStream

    implements DataOutput,
	       YoixConstants

{

    //
    // Class that hides the discrepancies between Java's OutputStream
    // and RandomAccessFile, so we don't have to worry about what's
    // being used as the underlying output sink.
    //

    private DataOutputStream     dostream = null;
    private RandomAccessFile     rafile = null;
    private YoixDataInputStream	 listream = null;
    private boolean              lastUsed = false;

    private static final String  UNWRITABLE = "stream not opened for writing";
    private static final String  UNSUPPORTED = "not supported by this stream";

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixDataOutputStream(OutputStream output) {

	if (output != null) {
	    if (output instanceof DataOutputStream)
		dostream = (DataOutputStream)output;
	    else dostream = new DataOutputStream(output);
	} else VM.abort(INTERNALERROR);
    }


    YoixDataOutputStream(RandomAccessFile output) {

	this(output, null);
    }


    YoixDataOutputStream(RandomAccessFile output, YoixDataInputStream input) {

	if (output != null) {
	    rafile = output;
	    if (input != null) {
		if (input.isRandomAccess()) {
		    if (input.usesThisRandomAccessFile(rafile)) {
			listream = input;
			listream.linkTo(this);
		    } else VM.abort(INTERNALERROR);
		} else VM.abort(INTERNALERROR);
	    }
	} else VM.abort(INTERNALERROR);
    }

    ///////////////////////////////////
    //
    // DataOutput Methods
    //
    ///////////////////////////////////

    public final void
    write(byte b[])

	throws IOException

    {

	if (dostream != null)
	    dostream.write(b);
	else if (rafile != null)
	    rafile.write(b);
	else throw(new IOException(UNWRITABLE));
    }


    public final void
    write(byte b[], int off, int len)

	throws IOException

    {

	if (dostream != null)
	    dostream.write(b, off, len);
	else if (rafile != null)
	    rafile.write(b, off, len);
	else throw(new IOException(UNWRITABLE));
    }


    public final void
    write(int b)

	throws IOException

    {

	if (dostream != null)
	    dostream.write(b);
	else if (rafile != null)
	    rafile.write(b);
	else throw(new IOException(UNWRITABLE));
    }


    public final void
    writeBoolean(boolean v)

	throws IOException

    {

	if (dostream != null)
	    dostream.writeBoolean(v);
	else if (rafile != null)
	    rafile.writeBoolean(v);
	else throw(new IOException(UNWRITABLE));
    }


    public final void
    writeByte(int v)

	throws IOException

    {

	if (dostream != null)
	    dostream.writeByte(v);
	else if (rafile != null)
	    rafile.writeByte(v);
	else throw(new IOException(UNWRITABLE));
    }


    public final void
    writeBytes(String s)

	throws IOException

    {

	if (dostream != null)
	    dostream.writeBytes(s);
	else if (rafile != null)
	    rafile.writeBytes(s);
	else throw(new IOException(UNWRITABLE));
    }


    public final void
    writeChar(int v)

	throws IOException

    {

	if (dostream != null)
	    dostream.writeChar(v);
	else if (rafile != null)
	    rafile.writeChar(v);
	else throw(new IOException(UNWRITABLE));
    }


    public final void
    writeChars(String s)

	throws IOException

    {

	if (dostream != null)
	    dostream.writeChars(s);
	else if (rafile != null)
	    rafile.writeChars(s);
	else throw(new IOException(UNWRITABLE));
    }


    public final void
    writeDouble(double v)

	throws IOException

    {

	if (dostream != null)
	    dostream.writeDouble(v);
	else if (rafile != null)
	    rafile.writeDouble(v);
	else throw(new IOException(UNWRITABLE));
    }


    public final void
    writeFloat(float v)

	throws IOException

    {

	if (dostream != null)
	    dostream.writeFloat(v);
	else if (rafile != null)
	    rafile.writeFloat(v);
	else throw(new IOException(UNWRITABLE));
    }


    public final void
    writeInt(int v)

	throws IOException

    {

	if (dostream != null)
	    dostream.writeInt(v);
	else if (rafile != null)
	    rafile.writeInt(v);
	else throw(new IOException(UNWRITABLE));
    }


    public final void
    writeLong(long v)

	throws IOException

    {

	if (dostream != null)
	    dostream.writeLong(v);
	else if (rafile != null)
	    rafile.writeLong(v);
	else throw(new IOException(UNWRITABLE));
    }


    public final void
    writeShort(int v)

	throws IOException

    {

	if (dostream != null)
	    dostream.writeShort(v);
	else if (rafile != null)
	    rafile.writeShort(v);
	else throw(new IOException(UNWRITABLE));
    }


    public final void
    writeUTF(String s)

	throws IOException

    {

	if (dostream != null)
	    dostream.writeUTF(s);
	else if (rafile != null)
	    rafile.writeUTF(s);
	else throw(new IOException(UNWRITABLE));
    }

    ///////////////////////////////////
    //
    // YoixDataOutputStream Methods
    //
    ///////////////////////////////////

    final boolean
    bufferResetNeeded() {

	return(rafile != null ? !lastUsed : false);
    }


    final void
    close()

	throws IOException

    {

	if (dostream != null) {
	    dostream.close();
	    dostream = null;
	} else if (rafile != null) {
	    if (listream == null)
		rafile.close();
	    else listream.unlinkFrom(this);
	    rafile = null;
	} else throw(new IOException(UNWRITABLE));
    }


    final void
    flush()

	throws IOException

    {

	if (dostream != null)
	    dostream.flush();
	else if (rafile == null)
	    throw(new IOException(UNWRITABLE));
    }


    final long
    getOffset()

	throws IOException

    {

	if (dostream != null)
	    throw(new IOException("getOffset " + UNSUPPORTED));
	else if (rafile != null)
	    return(rafile.getFilePointer());
	else throw(new IOException(UNWRITABLE));
    }


    final boolean
    isRandomAccess() {

	return(rafile != null);
    }


    final void
    linkTo(YoixDataInputStream input) {

	if (rafile == null)
	    VM.abort(INTERNALERROR);
	else if (listream == null) {
	    if (input.usesThisRandomAccessFile(rafile))
		listream = input;
	    else VM.abort(INTERNALERROR);
	}
	if (listream != input)
	    VM.abort(INTERNALERROR);
    }


    final void
    seek(long pos)

	throws IOException

    {

	if (dostream != null)
	    throw(new IOException("seek " + UNSUPPORTED));
	else if (rafile != null) {
	    rafile.seek(pos);
	    // indicate that a buffer reset is definitely needed
	    lastUsed = false;
	    if (listream != null)
		listream.toggleUse(false);
	} else throw(new IOException(UNWRITABLE));
    }


    final boolean
    seekSupported() {

	return(rafile != null);		// recent change - check it
    }


    final void
    toggleUse(boolean local) {

	if (local) {
	    lastUsed = true;
	    if (listream != null)
		listream.toggleUse(false);
	} else lastUsed = false;
    }


    final long
    truncate(long len)

	throws IOException

    {

	if (dostream != null)
	    throw(new IOException("seek " + UNSUPPORTED));
	else if (rafile != null) {
	    rafile.setLength(len);
	    // indicate that a buffer reset is definitely needed
	    lastUsed = false;
	    if (listream != null)
		listream.toggleUse(false);
	} else throw(new IOException(UNWRITABLE));

	return(rafile.length());
    }


    final void
    unlinkFrom(YoixDataInputStream input) {

	if (rafile != null) {
	    if (listream == input)
		listream = null;
	    else if (listream != null)
		VM.abort(INTERNALERROR);
	} else VM.abort(INTERNALERROR);
    }


    final boolean
    usesThisRandomAccessFile(RandomAccessFile input) {

	return(rafile == input);
    }

}

