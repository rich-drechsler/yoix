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
import java.text.*;
import java.util.*;

public final
class YoixSimpleDateFormat extends SimpleDateFormat

    implements YoixConstants

{

    //
    // This class attempts to allow timezones the follow the XML standard:
    //
    //     The lexical representation of a timezone is a string of the form:
    //
    //           (('+' | '-') hh ':' mm) | 'Z'
    //
    //     where hh is a two-digit numeral (with leading zeros as required)
    //     that represents the hours, mm is a two-digit numeral that represents
    //     the minutes, '+' indicates a nonnegative duration, '-' indicates a
    //     nonpositive duration.
    //
    // The mapping so defined is one-to-one, except that '+00:00', '-00:00',
    // and 'Z' all represent the same zero-length duration timezone, UTC; 'Z'
    // is its canonical representation.
    //

    private SimpleDateFormat  xml_tz_processors[] = null;
    private Locale            xml_locale = null;

    final private String  XML_TZ_STR = "X";
    final private char    XML_TZ_CHAR = 'X';

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    YoixSimpleDateFormat() {

	super();
    }


    public
    YoixSimpleDateFormat(String pattern) {

	super("");
	if (pattern != null && pattern.contains(XML_TZ_STR))
	    pattern = processXMLPattern(pattern);
	super.applyLocalizedPattern(pattern);
    }


    public
    YoixSimpleDateFormat(String pattern, Locale locale) {

	super("", locale);
	xml_locale = locale;
	if (pattern != null && pattern.contains(XML_TZ_STR))
	    pattern = processXMLPattern(pattern);
	super.applyLocalizedPattern(pattern);
    }


    public
    YoixSimpleDateFormat(String pattern, DateFormatSymbols formatSymbols) {

	super("", formatSymbols);
	if (pattern != null && pattern.contains(XML_TZ_STR))
	    pattern = processXMLPattern(pattern);
	super.applyLocalizedPattern(pattern);
    }

    ///////////////////////////////////
    //
    // YoixSimpleDateFormat Methods
    //
    ///////////////////////////////////

    public void
    applyLocalizedPattern(String pattern) {

	if (pattern != null && pattern.contains(XML_TZ_STR))
	    pattern = processXMLPattern(pattern);
	else resetXMLPattern();
	super.applyLocalizedPattern(pattern);
    }


    public void
    applyPattern(String pattern) {

	if (pattern != null && pattern.contains(XML_TZ_STR))
	    pattern = processXMLPattern(pattern);
	else resetXMLPattern();
	super.applyPattern(pattern);
    }


    public StringBuffer
    format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {

	StringBuffer  sb;
	int           len;
	int           n;

	if (xml_tz_processors != null) {
	    super.format(date, toAppendTo, fieldPosition);
	    for (n = 0; n < xml_tz_processors.length; n++) {
		if (xml_tz_processors[n] == null) {
		    len = 0;
		} else {
		    sb = xml_tz_processors[n].format(date, new StringBuffer(), fieldPosition);
		    len = sb.length() + n; // add n to account for earlier colons, if any
		}
		addColon(toAppendTo, len);
	    }
	} else super.format(date, toAppendTo, fieldPosition);
	
	return(toAppendTo);
    }


    public Date
    parse(String text, ParsePosition pos) {

	ParsePosition   pp;
	String          newtext;
	Date            result = null;
	int             newstart;
	int             start;
	int             n;

	if (xml_tz_processors != null) {
	    //
	    // Date can only have one value, so go with the last value, which
	    // appears to be consistent with what SimpleDateFormat does in 
	    // similar situations (i.e., multiple TZs specified)
	    //
	    for (n = 0; n < xml_tz_processors.length; n++) {
		if (xml_tz_processors[n] == null) {
		    text = removeColon(text, 0);
		} else {
		    start = pos.getIndex();
		    pp = new ParsePosition(start);
		    result = xml_tz_processors[n].parse(text, pp);
		    newstart = pp.getIndex();
		    if (start < newstart) {
			text = removeColon(text, newstart);
		    } // else let things take their course
		}
	    }
	}
	result = super.parse(text, pos);

	return(result);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    addColon(StringBuffer sb, int offset) {

	int  len;

	len = sb.length();
	while (offset < len && Character.isWhitespace(sb.charAt(offset)))
	    offset++;
	if (offset+3 < len)
	    sb.insert(offset+3, ':');
    }


    private String
    processXMLPattern(String pattern) {

	String   newpat = null;
	boolean  in_quote;
	char     ch;
	char     chars[];
	int      replacements[];
	int      fmtcnt = 0;
	int      n;

	//
	// We assume pattern isn't null.
	//

	chars = pattern.toCharArray();
	in_quote = false;
	for (n = 0; n < chars.length; n++) {
	    switch ((int)chars[n]) {
		case '\'':
		    if (++n < chars.length) {
			if (chars[n] != '\'') {
			    in_quote = !in_quote;
			    n--;
			}
		    } else in_quote = !in_quote;
		    break;

		case XML_TZ_CHAR:
		    if (!in_quote) {
			if (xml_tz_processors == null) {
			    xml_tz_processors = new SimpleDateFormat[1];
			} else {
			    SimpleDateFormat[] tsdf;

			    tsdf = new SimpleDateFormat[fmtcnt+1];
			    System.arraycopy(xml_tz_processors, 0, tsdf, 0, fmtcnt);
			    xml_tz_processors = tsdf;
			}
			chars[n] = 'Z';
			newpat = new String(chars);
			if (n > 0) {
			    //
			    // Create with a Format with a pattern that stops
			    // just short of the TZ indicator.
			    //
			    xml_tz_processors[fmtcnt++] = new SimpleDateFormat(new String(chars, 0, n), xml_locale);
			} else xml_tz_processors[fmtcnt++] = null;
		    }
		    break;
	    }
	}

	return(newpat == null ? "" : newpat);
    }


    private String
    removeColon(String text, int offset) {

	StringBuffer  sbuf;
	char          chars[];
	int           textlen;

	//
	// If the colon isn't where we expect it to be, return the original
	// text and let things fail naturally.
	//

	chars = text.toCharArray();
	textlen = chars.length;
	while (offset < textlen && Character.isWhitespace(chars[offset]))
	    offset++;
	offset += 3;

	if (offset < textlen && chars[offset] == ':') {
	    sbuf = new StringBuffer(textlen - 1);
	    sbuf.append(chars, 0, offset);
	    offset++;
	    sbuf.append(chars, offset, textlen - offset);
	    text = sbuf.toString();
	}

	return(text);
    }


    private void
    resetXMLPattern() {

	xml_tz_processors = null;
    }
}
