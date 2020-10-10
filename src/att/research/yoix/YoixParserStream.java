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

//
// This was derived from the ASCII_CharStream.java class built by Java
// Compiler Compiler Version 1.1 (same for CharStream.java).
//
// This is a modified version of the ASCII_CharStream.java class that
// unfortunately seems to be needed if we're concerned about reliably
// switching input files. The canned CharStream classes that we looked
// at didn't provide the control over position information (line and
// column numbers) and the input buffer that we needed.
//

package att.research.yoix;
import java.io.*;

class YoixParserStream

    implements CharStream,
	       YoixConstants

{

    //
    // NOTE - bunch of recent error handling additions that also triggered
    // some cleanup (e.g., we remove ReInit() because if was unused). Also
    // added code that tries to store the "current line" as its read so the
    // error handling can get use it to show exactly where on the line the
    // syntax error was triggered. Ideally we'd prefer to use buffer[], but
    // there's no guarantee we can get our hands on the entire line using
    // buffer[]. We might be able to change fillBuffer() and expandBuffer()
    // enough so the entire line would be avaiable, but at this point that
    // seemed too dangerous, so we'll investiagte that one later on.
    //

    private Reader  stream;
    private String  name;

    private int  bufsize;
    private int  available;
    private int  tokenBegin;
    private int  bufpos = -1;
    private int  bufline[];
    private int  bufcolumn[];
    private int  column = 0;
    private int  line = 1;

    private char  buffer[] = null;
    private int   maxNextCharInd = 0;
    private int   inBuf = 0;
    private int   auxRemaining = 0;

    private boolean  prevCharIsCR = false;
    private boolean  prevCharIsLF = false;

    private YoixParserStream  parent;

    //
    // These are new and are only used to try to store the current line for
    // error messages. Character collection can be permanently disabled by
    // setting CURRENTLINELENGTH to 0 or temporarily disabled when nextchar
    // is negative (which sometimes can happen in backup()).
    //

    private char  currentline[] = null;
    private int   currentlinenumber;
    private int   nextchar;

    //
    // Added these defined constants so we could easily fiddle buffer sizes
    // while we were experimenting with some new code.
    //

    private static final int  BUFSIZE = 4096;
    private static final int  BUFSIZE_INCREMENT = 2048;
    private static final int  CURRENTLINELENGTH = 512;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixParserStream(Reader stream, String name) {

	this.stream = stream;
	this.name = (name != null) ? name : "--unknown--";

	line = 1;
	column = 0;
	bufsize = BUFSIZE;

	available = bufsize;
	buffer = new char[bufsize];
	bufline = new int[bufsize];
	bufcolumn = new int[bufsize];

	currentline = new char[CURRENTLINELENGTH];
	currentlinenumber = line;
	nextchar = 0;
    }

    ///////////////////////////////////
    //
    // CharStream Methods
    //
    ///////////////////////////////////

    public final void
    backup(int amount) {

	inBuf += amount;
	if ((bufpos -= amount) < 0)
	    bufpos += bufsize;
	nextchar -= amount;
    }


    public final char
    BeginToken()

	throws IOException

    {
	char  c;

	tokenBegin = -1;
	c = readChar();
	tokenBegin = bufpos;
	return(c);
    }


    public final void
    Done() {

	buffer = null;
	bufline = null;
	bufcolumn = null;
	currentline = null;
    }


    public final int
    getBeginColumn() {

	 return(bufcolumn[tokenBegin]);
    }


    public final int
    getBeginLine() {

	return(bufline[tokenBegin]);
    }


    public final int
    getColumn() {

	return(bufcolumn[bufpos]);
    }


    public final int
    getEndColumn() {

	return(bufcolumn[bufpos]);
    }


    public final int
    getEndLine() {

	return(bufline[bufpos]);
    }


    public final String
    GetImage() {

	if (bufpos >= tokenBegin)
	    return(new String(buffer, tokenBegin, bufpos - tokenBegin + 1));
	else return(new String(buffer, tokenBegin, bufsize - tokenBegin) + new String(buffer, 0, bufpos + 1));
    }


    public final int
    getLine() {

	return(bufline[bufpos]);
    }


    public final char[]
    GetSuffix(int len) {

	char  ret[] = new char[len];

	if ((bufpos + 1) >= len) {
	    System.arraycopy(buffer, bufpos - len + 1, ret, 0, len);
	} else {
	    System.arraycopy(buffer, bufsize - (len - bufpos - 1), ret, 0, len - bufpos - 1);
	    System.arraycopy(buffer, 0, ret, len - bufpos - 1, bufpos + 1);
	}

	return(ret);
    }


    public final char
    readChar()

	throws IOException

    {

	char  c;

	if (inBuf > 0) {
	    --inBuf;
	    c = ((char)((char)0xff & buffer[(bufpos == bufsize - 1) ? (bufpos = 0) : ++bufpos]));
	    if (nextchar < CURRENTLINELENGTH && nextchar >= 0)
		currentline[nextchar++] = c;
	} else {
	    if (++bufpos >= maxNextCharInd)
		fillBuffer();
	    c = (char)((char)0xff & buffer[bufpos]);
	    updatePosition(c);
	}

	return(c);
    }

    ///////////////////////////////////
    //
    // YoixParserStream Methods
    //
    ///////////////////////////////////

    final String
    getBufferedLine(int number) {

	String  line = null;
	char    ch;
	int     count;
	int     index;

	//
	// Right now we only guarantee one buffered line and don't bother
	// looking through buffer because the way it's implemented doesn't
	// even guarantee that the current token's entire line is saved.
	// This approach should be good enough since we're only using it
	// for error information, but if it's not we probably could modify
	// fillBuffer() and expandBuffer() so they try to keep the entire
	// line around.
	//

	if (nextchar > 0 && currentlinenumber == number) {
	    count = 0;
	    try {
		try {
		    while (bufline[bufpos] <= number) {
			if ((ch = readChar()) == '\n' || ch == '\r') {
			    backup(1);
			    break;
			}
			count++;
		    }
		}
		catch(IOException e) {}

		//
		// At this point, if we really wanted, we could look for
		// the line in buffer if we don't already have it - maybe
		// later.
		//

		if (nextchar > 0 && currentlinenumber == number) {
		    //
		    // Could have newlines at the end of currentline, so we
		    // trim them before making the string.
		    //
		    for (index = nextchar - 1; index >= 0; index--) {
			if ((ch = currentline[index]) != '\n' && ch != '\r')
			    break;
		    }
		    if (index >= 0)
			line = new String(currentline, 0, index + 1);
		}
	    }
	    finally {
		//
		// Make sure we backup to where we were when we started.
		// Using count should mean we don't have to worry if the
		// buffer was expanded as we tried to collect the entire
		// line.
		//
		if (count > 0)
		    backup(count);
	    }
	}

	return(line);
    }


    final String
    getEncounteredToken(ParseException e) {

	String  token;
	String  sep;
	Token   tok;

	//
	// Error handling code that came directly from the old parser that
	// returns a string representation of the token (the tokens) that
	// triggered the error.
	//

	if (e != null && e.currentToken != null) {
	    token = "";
	    sep = "";
	    for (tok = e.currentToken.next; tok != null; tok = tok.next) {
		if (tok.kind == 0) {		// it's EOF
		    token += sep + tokenImage[0];
		    break;
		} else token += sep + e.add_escapes(tok.image);
		sep = " ";
	    }
	} else token = null;

	return(getQuotedToken(token));
    }


    final String
    getExpectedToken(ParseException e) {

	String  expected;
	String  token = null;

	//
	// We only acknowledge a rather limited collection of tokens, but
	// it's a collection that likely will expand some.
	//

	if (e != null && e.expectedTokenSequences != null) {
	    if ((token = tokenImage[e.expectedTokenSequences[0][0]]) != null) {
		if (token.equals("\";\""))
		    expected = token;
		else if (token.equals("\")\"") || token.equals("\"}\"") || token.equals("\"]\""))
		    expected = token;
		else if (token.equals("\":\""))
		    expected = token;
		else expected = null;
	    } else expected = null;
	} else expected = null;

	return(expected);
    }


    final String
    getExpectedTokens(ParseException e) {

	String  expected;
	String  token;
	String  sep;
	int     count;
	int     m;
	int     n;

	//
	// This dumps the entire list of expected tokens, which really isn't
	// particularly useful to programmers.
	//

	if (e != null && e.expectedTokenSequences != null) {
	    expected = "";
	    for (n = 0; n < e.expectedTokenSequences.length; n++) {
		sep = "\t";
		for (m = 0; m < e.expectedTokenSequences[n].length; m++) {
		    if (e.expectedTokenSequences[n][m] != 0) {
			expected += sep + tokenImage[e.expectedTokenSequences[n][m]];
			sep = " ";
		    }
		}
		if (m > 0 && expected.length() > 0)
		    expected += NL;
	    }
	    if (expected.length() > 0)
		expected = "Parser was looking for one of the following tokens:" + NL + expected;
	    else expected = null;
	} else expected = null;

	return(expected);
    }


    final String
    getMarkedLine(int line, int column, String prefix, boolean expand) {

	StringBuffer  sbuf;
	String        text = null;
	char          ch;
	int           indent;
	int           length;
	int           count;
	int           col;
	int           n;

	//
	// Seriously doubt there's anything that we need to catch here, but
	// this was a late addition, so we're going to be cautious for now.
	//

	if (line > 0 && column > 0) {
	    try {
		if ((text = getBufferedLine(line)) != null) {
		    if ((length = text.length()) > 0) {
			sbuf = new StringBuffer(length);
			if (prefix != null && prefix.length() > 0)
			    sbuf.append(prefix);
			indent = sbuf.length();
			if (expand) {
			    for (n = 0, col = 1; n < length; col++, n++) {
				if ((ch = text.charAt(n)) == '\t') {
				    col--;
				    for (count = 8 - (col & 0x7); count > 0; count--, col++)
					sbuf.append(' ');
				} else sbuf.append(ch);
			    }
			} else sbuf.append(text);
			sbuf.append('\n');
			for (n = 0; n < indent; n++)
			    sbuf.append(' ');
			for (col = 1; col < column; col++)
			    sbuf.append(' ');
			sbuf.append("^");
			text = sbuf.toString();
		    }
		}
	    }
	    catch(Throwable t) {}
	}
	return(text);
    }


    final String
    getName() {

	return(name);
    }


    final YoixParserStream
    getParent() {

	return(parent);
    }


    final boolean
    haveLine(int line) {

	return(haveBufferedLine(line));
    }


    final boolean
    loadAux(String val) {

	if (val == null)
	    return(false);

	// assume always called just after a token was found (as it
	// is the token that caused the need to insert auxillary text)

	char[] newdata = val.toCharArray();

	// first determine if buffer is big enough

	int  occupied = maxNextCharInd - tokenBegin;

	while ((newdata.length + occupied) > bufsize)
	    expandBuffer(false);

	// next make sure tokenBegin is at position 0

	if (tokenBegin > 0) {
	    System.arraycopy(buffer, tokenBegin, buffer, 0, occupied);
	    System.arraycopy(bufline, tokenBegin, bufline, 0, occupied);
	    System.arraycopy(bufcolumn, tokenBegin, bufcolumn, 0, occupied);
	    bufpos -= tokenBegin;
	    tokenBegin = 0;
	}

	// then fix maxNextCharInd after all the moving around

	maxNextCharInd = tokenBegin + occupied;

	// now slip in the auxillary text and set col/line info

	int  unread = maxNextCharInd - bufpos;

	if (unread > 0) {
	    System.arraycopy(buffer, bufpos+1, buffer, bufpos+1+newdata.length, unread);
	    System.arraycopy(bufline, bufpos+1, bufline, bufpos+1+newdata.length, unread);
	    System.arraycopy(bufcolumn, bufpos+1, bufcolumn, bufpos+1+newdata.length, unread);
	}

	System.arraycopy(newdata, 0, buffer, bufpos+1, newdata.length);
	for (int i=bufpos+1; i<=bufpos+newdata.length; i++) {
	    bufline[i] = bufline[0];
	    bufcolumn[i] = bufcolumn[0];
	}

	maxNextCharInd += newdata.length;
	auxRemaining += newdata.length;
	return(true);
    }


    final void
    setParent(YoixParserStream parent) {

	this.parent = parent;
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    expandBuffer(boolean wrap) {

	char  newbuffer[] = new char[bufsize + BUFSIZE_INCREMENT];
	int   newbufline[] = new int[bufsize + BUFSIZE_INCREMENT];
	int   newbufcolumn[] = new int[bufsize + BUFSIZE_INCREMENT];

	try {
	    if (wrap) {
		System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
		System.arraycopy(buffer, 0, newbuffer, bufsize - tokenBegin, bufpos);
		buffer = newbuffer;

		System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
		System.arraycopy(bufline, 0, newbufline, bufsize - tokenBegin, bufpos);
		bufline = newbufline;

		System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
		System.arraycopy(bufcolumn, 0, newbufcolumn, bufsize - tokenBegin, bufpos);
		bufcolumn = newbufcolumn;

		maxNextCharInd = (bufpos += (bufsize - tokenBegin));
	    } else {
		System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
		buffer = newbuffer;
		System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
		bufline = newbufline;
		System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
		bufcolumn = newbufcolumn;
		maxNextCharInd = (bufpos -= tokenBegin);
	    }
	}

	catch(Throwable t) {
	    throw(new Error(t.getMessage()));
	}

	bufsize += BUFSIZE_INCREMENT;
	available = bufsize;
	tokenBegin = 0;
    }


    private void
    fillBuffer()

	throws IOException

    {

	int  i;

	if (maxNextCharInd == available) {
	    if (available == bufsize) {
		if (tokenBegin > BUFSIZE_INCREMENT) {
		    bufpos = maxNextCharInd = 0;
		    available = tokenBegin;
		} else if (tokenBegin < 0)
		    bufpos = maxNextCharInd = 0;
		else expandBuffer(false);
	    } else if (available > tokenBegin)
		available = bufsize;
	    else if ((tokenBegin - available) < BUFSIZE_INCREMENT)
		expandBuffer(true);
	    else available = tokenBegin;
	}

	try {
	    if ((i = stream.read(buffer, maxNextCharInd, available - maxNextCharInd)) == -1) {
		stream.close();
		throw(new IOException());
	    } else maxNextCharInd += i;

	    return;
	}

	catch(IOException e) {
	    --bufpos;
	    backup(0);
	    if (tokenBegin == -1)
		tokenBegin = bufpos;
	    throw(e);
	}

    }


    private String
    getQuotedToken(String token) {

	if (token != null) {
	    if (token.length() > 0)
		token = "\"" + token + "\"";
	    else token = null;
	}
	return(token);
    }


    private boolean
    haveBufferedLine(int number) {

	return(nextchar > 0 && currentlinenumber == number);
    }


    private void
    updatePosition(char c) {

	if (auxRemaining <= 0) {
	    column++;
	    if (prevCharIsLF) {
		prevCharIsLF = false;
		column = 1;
		line++;
		currentlinenumber = line;
		nextchar = 0;
	    } else if (prevCharIsCR) {
		prevCharIsCR = false;
		if (c != '\n') {
		    column = 1;
		    line++;
		    currentlinenumber = line;
		    nextchar = 0;
		} else prevCharIsLF = true;
	    }

	    switch (c) {
		case '\r' :
		    prevCharIsCR = true;
		    break;

		case '\n' :
		    prevCharIsLF = true;
		    break;

		case '\t' :
		    column--;
		    column += (8 - (column & 07));
		    break;
	    }

	    bufline[bufpos] = line;
	    bufcolumn[bufpos] = column;
	    if (nextchar < CURRENTLINELENGTH && nextchar >= 0)
		currentline[nextchar++] = c;
	} else auxRemaining--;
    }
}

