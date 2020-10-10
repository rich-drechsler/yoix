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
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

public abstract
class YoixMiscGraph

    implements YoixAPI,
	       YoixConstants,
	       YoixConstantsGraph

{

    private static HashMap  colors = null;

    // following three lines taken from SwingJGraphPlot
    static final int  DATA_GRAPH_NODE = 0x01;
    static final int  DATA_GRAPH_EDGE = 0x02;
    static final int  DATA_GRAPH_REFERENCE = 0x04;
    static final int  DATA_GRAPH_REQUIRED = 0x08;

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public static String
    graphdata(YoixObject yobj, char delim, Object attributes) {

	return(graphdata(yobj, delim, attributes, false, false));
    }


    public static String
    graphdata(YoixObject yobj, char delim, Object attributes, boolean terminate) {

	return(graphdata(yobj, delim, attributes, terminate, false));
    }


    public static String
    graphdata(YoixObject yobj, char delim, Object attributes, boolean terminate, boolean referenced) {

	YoixGraphElement  elem;
	YoixObject        obj;
	YoixObject        entry;
	String            value[];
	String            key;
	String            star = "*";
	Object            managed;
	char              buf[];
	int               length;
	int               n;

	//
	// Now accepts attributes in a few different forms, which means
	// we can supply partition dependent attribute lists. Changes
	// were added carefully in a way that should maintain backward
	// compatibility, which probably isn't particularly important.
	// Most of the new code kicks in when the attributes argument
	// is a dictionary that is supposed to map partition names to
	// attribute arrays.
	//

	if (yobj.notNull()) {
	    if (yobj.isString()) {
		if ((buf = yobj.toCharArray(true, null)) != null)
		    elem = YoixGraphElement.loadDOTGraph(buf);
		else elem = YoixGraphElement.loadDOTGraph(yobj.stringValue());
	    } else if (yobj.isGraphElement() && (managed = yobj.getManagedObject()) != null) {
		if (managed instanceof YoixGraphElement) {
		    elem = (YoixGraphElement)managed;
		} else elem = null;
	    } else elem = null;
	} else elem = null;

	if (attributes instanceof YoixObject) {
	    obj = (YoixObject)attributes;
	    if (obj.notNull()) {
		if (obj.isDictionary()) {
		    attributes = new HashMap();
		    length = obj.length();
		    for (n = obj.offset(); n < length; n++) {
			if ((key = obj.name(n)) != null) {
			    if ((entry = obj.getObject(n)) != null) {
				if ((value = YoixMake.javaStringArray(entry, true, "")) != null) {
				    ((HashMap)attributes).put(key, value);
				    if (star != null) {
					((HashMap)attributes).put(star, value);
					star = null;
				    }
				}
			    }
			}
		    }
		} else attributes = YoixMake.javaStringArray(obj, true, "");
	    } else attributes = null;
	}

	return(referenced ? grefdata(elem, delim, attributes, terminate) : graphdata(elem, delim, attributes, terminate));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static void
    appendAdditionals(StringBuffer buf, YoixGraphElement elem) {

	appendAdditionals(buf, elem, true);
    }


    private static void
    appendAdditionals(StringBuffer buf, YoixGraphElement elem, boolean puttip) {

	String  attr;
	String  val;
	String  styles[];
	char    ch;
	int     len;
	int     ln;
	int     i;
	int     j;
	int     k;

	//
	// The tooltip case is a recent addition (2/4/05) that's supposed
	// to define a string that will be used as a tooltip. The label is
	// used when there's no tooltip entry, so tooltip provides a way
	// to override the default or it provide tooltip text when there's
	// no label.
	//

	if (puttip && (attr = atget(elem, "tooltip")) != null) {
	    buf.append(" t ");
	    buf.append(attr.length());
	    buf.append(" -");
	    buf.append(attr);
	}
 	if ((attr = atget(elem, "color")) != null) {
 	    buf.append(" A 2 color s ");
 	    buf.append(attr.length());
 	    buf.append(" -");
 	    buf.append(attr);
	}
	if ((attr = atget(elem, "fontcolor")) != null) {
	    buf.append(" A 2 fontcolor s ");
 	    buf.append(attr.length());
 	    buf.append(" -");
 	    buf.append(attr);
	}
	if ((attr = atget(elem, "fontsize")) != null) {
	    buf.append(" A 2 fontsize N 1 ");
	    buf.append(attr);
	}
	if ((attr = atget(elem, "fontstyle")) != null) {
	    buf.append(" A 2 fontstyle S ");
	    buf.append(attr.length());
	    buf.append(" -");
	    buf.append(attr);
	}
	if ((attr = atget(elem, "pos")) != null) {
	    if ((val = getPosition(attr)) != null) {
		buf.append(" x ");
		buf.append(val);
	    } 
	}
	if ((attr = atget(elem, "style")) != null) {
	    if ((styles = parse_style(attr)) != null && (len = styles.length) > 0) {
		for (i = 0; i < len; i = j) {
		    j = i + 1;
		    if (j < len && "(".equals(styles[j])) {
			ch = styles[++j].charAt(0);
			ln = styles[j].length();
			if (Character.isDigit(ch)) {
			    k = 1;
			    while (k<ln && Character.isDigit(ch = styles[j].charAt(k))) k++;
			    if (k == ln) {
				buf.append(" A 2 ");
				buf.append(underscore(styles[i]));
				buf.append(" N 1 ");
				buf.append(styles[j]);
			    } else {
				buf.append(" A 2 ");
				buf.append(underscore(styles[i]));
				buf.append(" S ");
				buf.append(ln);
				buf.append(" -");
				buf.append(styles[j]);
			    }
			} else {
			    buf.append(" A 2 ");
			    buf.append(underscore(styles[i]));
			    buf.append(" S ");
			    buf.append(ln);
			    buf.append(" -");
			    buf.append(styles[j]);
			}
			j += 2;
		    } else {
			buf.append(" A 1 ");
			buf.append(underscore(styles[i]));
		    }
 		}
	    }
	}
    }


    private static void
    appendPartition(StringBuffer buf, YoixGraphElement elem, char delim, Object attributes) {

	appendPartition(buf, elem, delim, attributes, true);
    }


    private static void
    appendPartition(StringBuffer buf, YoixGraphElement elem, char delim, Object attributes, boolean putpart) {

	StringTokenizer  tok;
	String           attrs[] = null;
	String           attr;
	String           partition;
	String           token;
	int              count;
	int              n;

	if ((partition = atget(elem, "_partition_")) == null) {
	    if ((partition = atget(elem, "_type_")) == null) {
		if (elem.ofType(GRAPH_NODE))
		    partition = "" + DATA_GRAPH_NODE;
		else if (elem.ofType(GRAPH_EDGE))
		    partition = "" + DATA_GRAPH_EDGE;
		else partition = "0";
	    }
	}

	if (putpart) {
	    buf.append(delim);
	    buf.append(partition);
	}

	if (attributes != null) {
	    if (attributes instanceof HashMap) {
		if ((attrs = (String[])((HashMap)attributes).get(partition)) == null)
		    attrs = (String[])((HashMap)attributes).get("*");
	    } else if (attributes instanceof String[])
		attrs = (String[])attributes;
	    if (attrs != null) {
		for (n = 0; n < attrs.length; n++) {
		    buf.append(delim);
		    if ((attr = atget(elem, attrs[n])) == null) {
			//
			// The "__label__" and "__name__" strings trigger
			// special code. "__label__" tries to generate a
			// string by combining an entire "_ldraw_" label,
			// if there is one, otherwise it returns getName(),
			// while "__name__" always returns getName(). We
			// recently (8/21/05) made changes to setName(),
			// and we think automatically generated edge names
			// have improved, so "__name__" now may be a better
			// choice than "__label__".
			//
			if (attrs[n].equals("__label__")) {
			    if ((attr = atget(elem, "_ldraw_")) != null) {
				tok = new StringTokenizer(attr);
				for (count = 1; tok.hasMoreTokens(); count++) {
				    token = tok.nextToken();
				    if ((count%7) == 0)
					buf.append(token.substring(1));
				}
			    } else buf.append(elem.getName());
			} else if (attrs[n].equals("__name__"))
			    buf.append(elem.getName());
		    } else buf.append(attr);
		}
	    }
	}
    }


    private static String
    atget(YoixGraphElement obj, String name) {

	YoixObject  yobj;
	String      value;
	int         length;
	int         n;

	//
	// Old version initially trimmed whitespace from both ends, but if
	// the result ended in "-" it assumed an explicit whitespace text
	// string and in that case only trimmed the left end. Usually OK,
	// but not if an explicit text string (i.e., T n -text) ended with
	// whitspace but also included non-whitespace characters. Decided
	// trimming from the right end should be skipped, at least for now.
	// 

	if ((yobj = obj.getAttribute(name)) != null && yobj.isString()) {
	    value = yobj.stringValue();
	    if ((length = value.length()) > 0) {
		for (n = 0; n < length; n++) {
		    if (Character.isWhitespace(value.charAt(n)) == false)
			break;
		}
		if (n > 0) {
		    if (n < length)
			value = value.substring(n);
		    else value = null;
		}
	    } else value = null;
	} else value = null;

	return(value);
    }


    private static String
    canonize(String str) {

	char  array[];
	int   pos;
	int   n;

	if (str != null) {
	    array = str.toCharArray();
	    for (n = 0, pos = 0; n < array.length; n++) {
		if (Character.isLetterOrDigit(array[n]))
		    array[pos++] = Character.toLowerCase(array[n]);
	    }
	    str = (pos != 0) ? new String(array, 0, pos) : "";
	}
	return(str);
    }


    private static String
    getPosition(String str) {

	StringTokenizer  tok;
	String           token;
	int              count;
	int              x;
	int              y;

	//
	// Looks like edge positions somtimes start with "s," or "e,", so
	// we need to skip over anything that doesn't look like a number.
	// We still assume second token is legit if first token was.
	//

	if (str != null) {
	    x = 0;
	    y = 0;
	    tok = new StringTokenizer(str, " ,");
	    for (count = 0; tok.countTokens() >= 2;) {
		token = tok.nextToken();
		if (token.length() > 0 && Character.isDigit(token.charAt(0))) {
		    count++;
		    x += YoixMake.javaInt(token, 0);
		    y += YoixMake.javaInt(tok.nextToken(), 0);
		}
	    }
	    if (count > 1) {
		x /= count;
		y /= count;
	    }
	    str = "2 " + x + " " + y;
	}
	return(str);
    }


    private static String
    graphdata(YoixGraphElement graph, char delim, Object attributes, boolean terminate) {

	YoixGraphElement  elem;
	StringBuffer      buf;
	String            ret;
	String            attr;
	String            h_attr;
	String            t_attr;
	Vector            vec;
	int               firstnode = -1;
	int               lastnode = -2;
	int               sz;
	int               i;

	//
	// Added firstnode and lastnode to help with the second pass
	// through the graph.
	//

	ret = null;

	if (graph != null) {
	    buf = new StringBuffer();
	    vec = graph.traverse(true, GRAPH_EDGE|GRAPH_NODE, -1, null, null, 0);
	    sz = vec.size();
	    for (i = 0; i < sz; i++) {
		elem = (YoixGraphElement)(vec.elementAt(i));
		if (elem.ofType(GRAPH_EDGE)) {
		    buf.append(quoteText(elem.getName(), ' '));
		    buf.append(' ');
		    if ((attr = atget(elem, "_type_")) != null)
			buf.append(attr);
		    else buf.append('2');
		    if ((attr = atget(elem, "_tdraw_")) != null) {
			buf.append(" w 2 ");	// new - tail subpath index
			buf.append(attr);
		    }
		    if ((attr = atget(elem, "_draw_")) != null) {
			buf.append(" w 0 ");	// new - edge subpath index
			buf.append(attr);
		    }
		    if ((attr = atget(elem, "_hdraw_")) != null) {
			buf.append(" w 1 ");	// new - head subpath index
			buf.append(attr);
		    }
		    attr = atget(elem, "_ldraw_");
		    h_attr = atget(elem, "_hldraw_");
		    t_attr = atget(elem, "_tldraw_");
		    if (attr != null || h_attr != null || t_attr != null)
			buf.append(" <");
		    if (attr != null) {
			buf.append(' ');
			buf.append(attr);
		    }
		    if (h_attr != null) {
			buf.append(' ');
			buf.append(h_attr);
		    }
		    if (t_attr != null) {
			buf.append(' ');
			buf.append(t_attr);
		    }
		    if (attr != null || h_attr != null || t_attr != null)
			buf.append(" >");
		    appendAdditionals(buf, elem);
		    appendPartition(buf, elem, delim, attributes);
		    if (terminate)
			buf.append(delim);
		    buf.append('\n');
		} else {
		    if (firstnode < 0)
			firstnode = i;
		    lastnode = i;
		}
	    }
	    for (i = firstnode; i <= lastnode; i++) {
		elem = (YoixGraphElement)(vec.elementAt(i));
		if (elem.ofType(GRAPH_NODE)) {
		    buf.append(quoteText(elem.getName(), ' '));
		    buf.append(' ');
		    if ((attr = atget(elem, "_type_")) != null)
			buf.append(attr);
		    else buf.append('1');
		    if ((attr = atget(elem, "_draw_")) != null) {
			buf.append(' ');
			buf.append(attr);
		    }
		    if ((attr = atget(elem, "_ldraw_")) != null) {
			buf.append(" < ");
			buf.append(attr);
			buf.append(" >");
		    }
		    appendAdditionals(buf, elem);
		    appendPartition(buf, elem, delim, attributes);
		    if (terminate)
			buf.append(delim);
		    buf.append('\n');
		}
	    }
	    ret = buf.toString();
	}
	return(ret);
    }


    private static String
    grefdata(YoixGraphElement graph, char delim, Object attributes, boolean terminate) {

	YoixGraphElement  elem;
	Vector            vec;
	StringBuffer      buf;
	StringBuffer      attrs;
	String            name;
	String            ret;
	String            attr;
	String            h_attr;
	String            t_attr;
	String            tips;
	Iterator          keys;
	Integer           nbr;
	Integer           count;
	HashMap           drawinfo;
	HashMap           attrinfo;
	HashMap           tipsinfo;
	HashMap           ecountinfo;
	HashMap           ncountinfo;
	HashMap           countinfo;
	ArrayList         list;
	int               firstnode = -1;
	int               lastnode = -2;
	int               sz;
	int               i;
	int               idx;
	int               type;

	//
	// Added firstnode and lastnode to help with the second pass
	// through the graph.
	//

	ret = null;

	if (graph != null) {
	    drawinfo = new HashMap();
	    attrinfo = new HashMap();
	    tipsinfo = new HashMap();
	    ecountinfo = new HashMap();
	    ncountinfo = new HashMap();
	    vec = graph.traverse(true, GRAPH_EDGE|GRAPH_NODE, -1, null, null, 0);
	    buf = new StringBuffer(32 * vec.size()); // a total guess on needed buffer size
	    sz = vec.size();
	    for (i = 0; i < sz; i++) {
		buf.setLength(0);
		elem = (YoixGraphElement)(vec.elementAt(i));
		name = elem.getName();
		if ((idx = name.indexOf('[')) >= 0)
		    name = name.substring(0, idx);
		if (elem.ofType(GRAPH_EDGE))
		    countinfo = ecountinfo;
		else countinfo = ncountinfo;
		if (countinfo.containsKey(name)) {
		    nbr = (Integer)countinfo.get(name);
		    countinfo.put(name, new Integer(nbr.intValue() + 1));

		    appendPartition(buf, elem, delim, attributes, false);
		    list = (ArrayList)(attrinfo.get(name));
		    if (buf.length() > 0)
			list.add(buf.toString());
		    else list.add(null);
		} else {
		    countinfo.put(name, new Integer(1));
		    if (elem.ofType(GRAPH_EDGE)) {
			if ((attr = atget(elem, "_tdraw_")) != null) {
			    buf.append(" w 2 ");	// new - tail subpath index
			    buf.append(attr);
			}
			if ((attr = atget(elem, "_draw_")) != null) {
			    buf.append(" w 0 ");	// new - edge subpath index
			    buf.append(attr);
			}
			if ((attr = atget(elem, "_hdraw_")) != null) {
			    buf.append(" w 1 ");	// new - head subpath index
			    buf.append(attr);
			}
			attr = atget(elem, "_ldraw_");
			h_attr = atget(elem, "_hldraw_");
			t_attr = atget(elem, "_tldraw_");
			if (attr != null || h_attr != null || t_attr != null)
			    buf.append(" <");
			if (attr != null) {
			    buf.append(' ');
			    buf.append(attr);
			}
			if (h_attr != null) {
			    buf.append(' ');
			    buf.append(h_attr);
			}
			if (t_attr != null) {
			    buf.append(' ');
			    buf.append(t_attr);
			}
			if (attr != null || h_attr != null || t_attr != null)
			    buf.append(" >");
		    } else {
			if ((attr = atget(elem, "_draw_")) != null) {
			    buf.append(' ');
			    buf.append(attr);
			}
			if ((attr = atget(elem, "_ldraw_")) != null) {
			    buf.append(" < ");
			    buf.append(attr);
			    buf.append(" >");
			}
		    }
		    appendAdditionals(buf, elem, false);
		    drawinfo.put(name, buf.toString());

		    buf.setLength(0);
		    appendPartition(buf, elem, delim, attributes, false);
		    list = new ArrayList(1);
		    if (buf.length() > 0)
			list.add(buf.toString());
		    else list.add(null);
		    attrinfo.put(name, list);
		}

		if ((attr = atget(elem, "tooltip")) != null) {
		    if (tipsinfo.containsKey(name)) {
			tips = (String)(tipsinfo.get(name));
			tipsinfo.put(name, tips + "<br>" + attr);
		    } else tipsinfo.put(name, attr);
		}
	    }

	    buf.setLength(0);
	    keys = ecountinfo.keySet().iterator();
	    while(keys.hasNext()) {
		name = (String)(keys.next());
		buf.append(quoteText(name, ' '));
		buf.append(' ');
		buf.append(DATA_GRAPH_EDGE|DATA_GRAPH_REFERENCE|DATA_GRAPH_REQUIRED);
		buf.append((String)(drawinfo.get(name)));
		if (tipsinfo.containsKey(name)) {
		    tips = (String)(tipsinfo.get(name));
		    buf.append(" t ");
		    buf.append(tips.length());
		    buf.append(" -");
		    buf.append(tips);
		}
		buf.append(delim);
		buf.append('0');
		buf.append(NL);
	    }
	    keys = ncountinfo.keySet().iterator();
	    while(keys.hasNext()) {
		name = (String)(keys.next());
		buf.append(quoteText(name, ' '));
		buf.append(' ');
		buf.append(DATA_GRAPH_NODE|DATA_GRAPH_REFERENCE|DATA_GRAPH_REQUIRED);
		buf.append((String)(drawinfo.get(name)));
		if (tipsinfo.containsKey(name)) {
		    tips = (String)(tipsinfo.get(name));
		    buf.append(" t ");
		    buf.append(tips.length());
		    buf.append(" -");
		    buf.append(tips);
		}
		buf.append(delim);
		buf.append('0');
		buf.append(NL);
	    }
	    buf.append(NL);
	    keys = attrinfo.keySet().iterator();
	    while(keys.hasNext()) {
		name = (String)(keys.next());
		if (ecountinfo.containsKey(name))
		    type = DATA_GRAPH_EDGE;
		else type = DATA_GRAPH_NODE;
		list = (ArrayList)(attrinfo.get(name));
		if (list == null || (sz = list.size()) == 0) {
		    buf.append(name);
		    buf.append(delim);
		    buf.append(type);
		    buf.append(delim);
		    buf.append(1);
		    buf.append(NL);
		} else {
		    for (i = 0; i < sz; i++) {
			buf.append(name);
			buf.append(delim);
			buf.append(type);
			buf.append(delim);
			buf.append(sz);
			if ((attr = (String)(list.get(i))) != null)
			    buf.append(attr);
			buf.append(NL);
		    }
		}
	    }
	    ret = buf.toString();
	}
	return(ret);
    }


    private static String[]
    parse_style(String style) {

	StringBuffer  buf = new StringBuffer(128);
	ArrayList     parse = new ArrayList(64);
	boolean       in_parens = false;
	String        result[] = null;
	int           position[];
	int           len;
	int           i;

	position = new int[1];

	position[0] = 0;
	len = style.length();
	while (style_token(style, len, position, buf)) {
	    if (buf.length() > 0) {
		switch (buf.charAt(0)) {
		case '(':
		    if (in_parens) {
			VM.abort(SYNTAXERROR, "nesting not allowed in style: " + style);
			return(null);
		    }
		    parse.add(buf.toString());
		    buf.setLength(0);
		    in_parens = true;
		    break;

		case ')':
		    if (in_parens == false) {
			VM.abort(SYNTAXERROR, "unmatched ')' in style: " + style);
			return(null);
		    }
		    in_parens = false;
		    parse.add(buf.toString());
		    buf.setLength(0);
		    break;

		case ',':
		    if (in_parens == true) {
			VM.abort(SYNTAXERROR, "multiple entries in parentheses in style: " + style);
			return(null);
			//parse.add(buf.toString());
			//buf.setLength(0);
		    }
		    break;

		default:
		    parse.add(buf.toString());
		    buf.setLength(0);
		    break;
		}
	    }
	}

	if (in_parens) {
	    VM.abort(SYNTAXERROR, "unmatched '(' in style: " + style);
	    return(null);
	}

	if ((len = parse.size()) > 0) {
	    result = new String[len];
	    for (i = 0; i < len; i++)
		result[i] = (String)(parse.get(i));
	}

	return(result);
    }


    private static String
    quoteText(String text, char delim) {

	StringBuffer  buffer;
	String        quoted;
	char          textchars[];
	int           n;

	//
	// Performs CSV-style quoting (i.e., use "" to represent " inside
	// double quotes), if needed.
	//

	if (text != null && text.length() > 0 && (text.charAt(0) == '"' || text.indexOf(delim) >= 0)) {
	    buffer = new StringBuffer(text.length() + 2);
	    textchars = text.toCharArray();
	    buffer.append('"');
	    for (n = 0; n < textchars.length; n++) {
		if (textchars[n] == '"')
		    buffer.append('"');
		buffer.append(textchars[n]);
	    }
	    buffer.append('"');
	    quoted = buffer.toString();
	} else quoted = text;

	return(quoted);
    }


    private static boolean
    style_delim(char c) {

	return(c == '(' || c == ')' || c == ',');
    }


    private static boolean
    style_token(String style, int len, int position[], StringBuffer out) {

	char  c;
	int   i;

	c = 0;
	i = position[0];
	while (i<len && (Character.isWhitespace(c = style.charAt(i))))
	    i++;
	if (i < len) {
	    switch (c) {
		case '(':
		case ')':
		case ',':
		    out.append(c);
		    i++;
		    break;
		default:
		    while (!style_delim(c)) {
			out.append(c);
			if (++i < len)
			    c = style.charAt(i);
			else break;
		    }
		    break;
	    }
	} else i++;
	position[0] = i;

	return(i <= len);
    }


    private static String
    underscore(String str) {

	char  array[];
	int   pos;

	if (str != null) {
	    array = str.toCharArray();
	    for (pos = 0; pos < array.length; pos++) {
		if (Character.isWhitespace(array[pos]))
		    array[pos] = '_';
	    }
	    str = (pos != 0) ? new String(array) : "";
	}
	return(str);
    }
}

