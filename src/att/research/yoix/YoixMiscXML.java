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
import java.util.*;
import java.util.regex.*;

abstract
class YoixMiscXML

    implements YoixConstants

{

    //
    // NOTE - xmlAdd() and xmlGet() support was added very quickly, mostly
    // for an important new application. They're functional but incomplete
    // and there's undoubtedly lots of room for improvement in the existing
    // code. Needs to revisited, along with the YoixModuleParser builtins
    // that use the code, before the next official release!! Most of the
    // code was written on 1/30/11 and 1/31/11.
    // 

    //
    // Flags that control some of the low level behavior xmlToYoix(). Most
    // of them control whitespace handling (TOSS_ATTRIBUTES obviously is an
    // exception). The flags that target CDATA probably haven't tested much
    // and really seem like overkill!!
    //

    static int  TOSS_WS_CHARDATA = 0x001;
    static int  TRIM_FRONT_CHARDATA_WS = 0x002;
    static int  TRIM_BACK_CHARDATA_WS = 0x04;
    static int  TOSS_ATTRIBUTES = 0x08;
    static int  TOSS_WS_CDATA = 0x010;
    static int  TRIM_FRONT_CDATA_WS = 0x020;
    static int  TRIM_BACK_CDATA_WS = 0x040;
    static int  OMIT_TEXT_CONSOLIDATION = 0x080;

    //
    // Flags that control the low level behavior of yoixToXML(). Most were
    // added on 2/2/11.
    //

    static int  ADD_XML_DECLARATION = 0x01;
    static int  ACCEPT_ALL_NAMES = 0x02;
    static int  OMIT_NULL_ELEMENTS = 0x04;
    static int  USE_EMPTY_ELEMENT_TAG = 0x08;

    //
    // Formal definitions of the names that can be assigned to objects that
    // store the attributes and content associated with an XML block and the
    // names that can be assigned to text and whitespace. Using a character,
    // like $, that's not allowed in XML names means we don't have to worry
    // about collisions.
    //

    private static final String  NAMEOF_XML_ATTRIBUTES = "$attributes";
    private static final String  NAMEOF_XML_TEXT = "$text";
    private static final String  NAMEOF_XML_WS = "$ws";

    //
    // We sometimes use this to eliminate unnecessary whitespace when we
    // translate an XML parse tree to it's YoixObject representation.
    //

    private static final String  XML_WS = " \n\r\t";
    private static final Pattern PATTERN_WS_OR_EMPTY = Pattern.compile("^[" + XML_WS + "]*$");
    private static final Pattern PATTERN_NEED_CDATA = Pattern.compile("[<&]|]]>");
    private static final Pattern PATTERN_CDATA_END = Pattern.compile("]]>");
    private static final Pattern PATTERN_LEADING_OR_TRAILING_WS = Pattern.compile("^[" + XML_WS + "]|[" + XML_WS + "]$");

    //
    // Masks that are used to control processing done in storeText(). Small
    // chance we'll want to add one for attributes??
    //

    private static int  CHARDATA_MASKS[] = {
	TRIM_FRONT_CHARDATA_WS|TRIM_BACK_CHARDATA_WS,
	TRIM_FRONT_CHARDATA_WS,
	TRIM_BACK_CHARDATA_WS,
	TOSS_WS_CHARDATA,
    };

    private static int  CDATA_MASKS[] = {
	TRIM_FRONT_CDATA_WS|TRIM_BACK_CDATA_WS,
	TRIM_FRONT_CDATA_WS,
	TRIM_BACK_CDATA_WS,
	TOSS_WS_CDATA,
    };

    //
    // The value that we associate with NOBLOCK nodes. Using null probably
    // is the only other possible value, but that makes it impossible for
    // scripts to tell when xmlGet() didn't find anything so you probably
    // shouldn't change this.
    // 

    private static String NOBLOCK_VALUE = "";

    ///////////////////////////////////
    //
    // YoixMiscXML Methods
    //
    ///////////////////////////////////

    static YoixObject
    xmlAdd(String key, YoixObject values, YoixObject dest) {

	return(xmlAdd(key, "/", "//*", values, dest));
    }


    static YoixObject
    xmlAdd(String key, String sep, YoixObject values, YoixObject dest) {

	return(xmlAdd(key, sep, sep, values, dest));
    }


    static YoixObject
    xmlAdd(String key, String sep, String regex, YoixObject values, YoixObject dest) {

	if (key != null && sep != null)
	    dest = xmlAdd(YoixMisc.trim(key, sep, sep).split(regex != null ? regex : sep), values, dest);
	else dest = null;

	return(dest);
    }


    static YoixObject
    xmlGet(String key, YoixObject dest) {

	return(xmlGet(key, "/", "//*", dest, true));
    }


    static YoixObject
    xmlGet(String key, YoixObject dest, boolean extract) {

	return(xmlGet(key, "/", "//*", dest, extract));
    }


    static YoixObject
    xmlGet(String key, String sep, YoixObject dest, boolean extract) {

	return(xmlGet(key, sep, sep, dest, extract));
    }


    private static YoixObject
    xmlGet(String key, String sep, String regex, YoixObject dest, boolean extract) {

	if (key != null && sep != null)
	    dest = xmlGet(YoixMisc.trim(key, sep, sep).split(regex != null ? regex : sep), dest, extract);
	else dest = null;

	return(dest);
    }


    static YoixObject
    xmlToYoix(SimpleNode tree) {

	return(xmlToYoix(tree, 0, TOSS_WS_CHARDATA));
    }


    static YoixObject
    xmlToYoix(SimpleNode tree, int model, int flags) {

	YoixObject  yobj = null;
	Object      obj;

	if (tree != null) {
	    if ((obj = xmlToJava(tree, model, flags, 0, newStorage(model))) != null) {
		if (obj instanceof ArrayList)
		    yobj = YoixMisc.copyIntoArray((ArrayList)obj, false, true);
		else if (obj instanceof HashMap)
		    yobj = YoixMisc.copyIntoDictionary((HashMap)obj, true);
	    }
	}

	return(yobj);
    }


    static String
    yoixToXML(YoixObject src) {

	return(yoixToXML(src, 0, ""));
    }


    static String
    yoixToXML(YoixObject src, int flags, String indent) {

	String prefix;
	String xml;

	if ((xml = yoixToXML(src, flags, indent, 0)) != null) {	// always true right now
	    if ((flags&ADD_XML_DECLARATION) != 0) {
		prefix = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>";
		if (indent != null)
		    prefix += "\n";
		xml = prefix + xml;
	    }
	}

	return(xml);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static boolean
    acceptName(String key, int flags) {

	boolean  result;

	//
	// Really only designed to skip null, "", and the three control names
	// that xmlToYoix() sometimes generates. Undoubtedly could do more,
	// but we're not trying to reject all invalid XML names.
	//

	if (key != null || key.length() > 0) {
	    if ((flags & ACCEPT_ALL_NAMES) == 0) {
		if (key.equals(NAMEOF_XML_ATTRIBUTES) || key.equals(NAMEOF_XML_TEXT) || key.equals(NAMEOF_XML_WS))
		    result = false;
		else result = true;
	    } else result = true;
	} else result = false;

	return(result);
    }


    private static void
    addAttributes(YoixObject dest, StringBuffer sbuf) {

	YoixObject  key;
	YoixObject  value;
	YoixObject  storage;
	String      name;
	int         length;
	int         count;
	int         n;

	if ((storage = getYoixValue(NAMEOF_XML_ATTRIBUTES, dest)) != null) {
	    if ((length = storage.length()) > 0) {
		if (storage.isArray()) {
		    for (n = 0; n < length - 1; n += 2) {
			if ((key = storage.getObject(n)) != null && key.isString()) {
			    if ((value = storage.getObject(n+1)) != null && value.isString()) {
				sbuf.append(" ");
				sbuf.append(key.stringValue());
				sbuf.append("=");
				sbuf.append("\"");
				sbuf.append(value.stringValue());
				sbuf.append("\"");
			    }
			}
		    }
		} else if (storage.compound()) {
		    for (n = 0; n < length; n++) {
			if ((name = storage.name(n)) != null) {
			    if ((value = storage.getObject(n)) != null && value.isString()) {
				sbuf.append(" ");
				sbuf.append(name);
				sbuf.append("=");
				sbuf.append("\"");
				sbuf.append(value.stringValue());
				sbuf.append("\"");
			    }
			}
		    }
		}
	    }
	}
    }


    private static void
    addCDATA(String text, StringBuffer sbuf) {

	Matcher  matcher;

	if (text != null) {
	    matcher = PATTERN_CDATA_END.matcher(text);
	    if (matcher.find())
		text = matcher.replaceAll("]]]]><![CDATA[>");
	    sbuf.append("<![CDATA[");
	    sbuf.append(text);
	    sbuf.append("]]>");
	}
    }


    private static void
    addIndent(String indent, int level, StringBuffer sbuf) {

	int  length;

	if (indent != null) {
	    if ((length = sbuf.length()) > 0 && sbuf.charAt(length-1) != '\n')
		sbuf.append("\n");
	    for (; level > 0; level--)
		sbuf.append(indent);
	}
    }


    private static boolean
    canCopyInto(Object source, Object dest) {

	ArrayList  list;
	Iterator   iterator;
	boolean    result;
	HashMap    keys;
	Object     key;
	int        size;
	int        n;

	//
	// A brute force approach that undoubtedly should be rewritten or
	// simply moved into the method that copies objects into HashMaps.
	// It's really only needed to support model 1, which mixes arrays
	// and dictionaries.
	//
	// NOTE - tried doing everything with Set operations on keySet(),
	// but got an exception so I switched to this approach. Was done
	// very quickly and it definitely needs to be revisited!!!!!
	//

	if (dest instanceof HashMap) {
	    keys = new HashMap((HashMap)dest);
	    if (source instanceof ArrayList) {
		result = true;
		list = (ArrayList)source;
		size = list.size();
		for (n = 0; n < size; n += 2) {
		    key = list.get(n);
		    if (keys.containsKey(key)) {
			result = false;
			break;
		    } else keys.put(key, Boolean.TRUE);
		}
	    } else if (source instanceof HashMap) {
		result = true;
		iterator = ((HashMap)source).keySet().iterator();
		while (iterator.hasNext()) {
		    key = iterator.next();
		    if (keys.containsKey(key)) {
			result = false;
			break;
		    } else keys.put(key, Boolean.TRUE);
		}
	    } else if (source instanceof String) {
		if (keys.containsKey(NAMEOF_XML_TEXT))
		    result = false;
		else result = true;
	    } else result = true;
	} else if (dest instanceof ArrayList)
	    result = true;
	else result = false;

	return(result);
    }


    private static ArrayList
    copyInto(Object value, ArrayList dest) {

	Iterator iterator;
	HashMap  map;
	Object   key;

	if (value instanceof ArrayList) {
	    iterator = ((ArrayList)value).iterator();
	    while (iterator.hasNext())
		dest.add(iterator.next());
	} else if (value instanceof HashMap) {
	    map = (HashMap)value;
	    iterator = map.keySet().iterator();
	    while (iterator.hasNext()) {
		key = iterator.next();
		dest.add(key);
		dest.add(map.get(key));
	    }
	} else if (value instanceof String) {
	    dest.add(NAMEOF_XML_TEXT);
	    dest.add(value);
	}

	return(dest);
    }


    private static HashMap
    copyInto(Object value, HashMap dest) {

	Iterator iterator;
	HashMap  map;
	Object   key;

	if (value instanceof ArrayList) {
	    iterator = ((ArrayList)value).iterator();
	    while (iterator.hasNext()) {
		key = iterator.next();
		if (iterator.hasNext())
		    dest.put(key, iterator.next());
	    }
	} else if (value instanceof HashMap) {
	    map = (HashMap)value;
	    iterator = map.keySet().iterator();
	    while (iterator.hasNext()) {
		key = iterator.next();
		dest.put(key, map.get(key));
	    }
	} else if (value instanceof String)
	    dest.put(NAMEOF_XML_TEXT, value);

	return(dest);
    }


    private static YoixObject
    getYoixValue(String name, YoixObject dest) {

	YoixObject  key;
	YoixObject  value = null;
	int         length;
	int         n;

	if (dest != null) {
	    if ((length = dest.length()) > 0) {
		if (dest.isArray()) {
		    for (n = 0; n < length - 1; n += 2) {
			if ((key = dest.getObject(n)) != null && key.isString()) {
			    if (key.stringValue().equals(name)) {
				value = dest.getObject(n+1);
				break;
			    }
			}
		    }
		} else if (dest.compound())
		    value = dest.getObject(name);
	    }
	}
	return(value);
    }


    private static Object
    newStorage(int model) {

	return(model != 0 ? (Object)new LinkedHashMap() : (Object)new ArrayList());
    }


    private static Object
    newStorage(int model, boolean attributes) {

	Object  storage = newStorage(model);

	if (attributes) {
	    if (storage instanceof ArrayList) {
		((ArrayList)storage).add(NAMEOF_XML_ATTRIBUTES);
		((ArrayList)storage).add(newStorage(model));
	    } else ((HashMap)storage).put(NAMEOF_XML_ATTRIBUTES, newStorage(model));
	}

	return(storage);
    }


    private static Object
    store(Object value, int model, int flags, Object dest) {

	return(store(null, value, model, flags, dest));
    }


    private static Object
    store(String name, Object value, int model, int flags, Object dest) {

	ArrayList  list;
	Iterator   iterator;
	HashMap    map;
	Object     key;
	int        size;

	if (value instanceof ArrayList) {
	    list = (ArrayList)value;
	    switch (list.size()) {
		case 0:
		    value = null;
		    break;

		case 1:
		    value = list.get(0);
		    break;

		case 2:
		    if (NAMEOF_XML_TEXT.equals(list.get(0)))
			value = list.get(1);
		    break;
	    }
	} else if (value instanceof HashMap) {
	    map = (HashMap)value;
	    switch (map.size()) {
		case 0:
		    value = null;
		    break;

		case 1:
		    if (map.containsKey(NAMEOF_XML_TEXT))
			value = map.get(NAMEOF_XML_TEXT);
		    break;
	    }
	}

	if (dest instanceof HashMap) {
	    map = (HashMap)dest;
	    if (model != 2) {		// can switch to an ArrayList
		if (map.containsKey(name)) {
		    list = new ArrayList();
		    iterator = map.keySet().iterator();
		    while (iterator.hasNext()) {
			key = iterator.next();
			list.add(key);
			list.add(map.get(key));
		    }
		    if ((flags&OMIT_TEXT_CONSOLIDATION) == 0) {
			if ((size = list.size()) > 1) {
			    if (NAMEOF_XML_TEXT.equals(name) || NAMEOF_XML_WS.equals(name)) {
				if (name.equals(list.get(size-2))) {
				    map.put(name, (String)map.get(name) + (String)value);
				    list = null;
				}
			    }
			}
		    }
		    if (list != null)
			dest = store(name, value, model, flags, list);
		} else {
		    if (name == null) {
			if (canCopyInto(value, map) == false)
			    dest = copyInto(value, copyInto(map, new ArrayList()));
			else copyInto(value, map);
		    } else map.put(name, value);
		}
	    } else {
		if (name != null)
		    map.put(name, value);
		else copyInto(value, map);
	    }
	} else if (dest instanceof ArrayList) {
	    list = (ArrayList)dest;
	    if ((flags&OMIT_TEXT_CONSOLIDATION) == 0) {
		if ((size = list.size()) > 1) {
		    if (NAMEOF_XML_TEXT.equals(name) || NAMEOF_XML_WS.equals(name)) {
			if (name.equals(list.get(size-2))) {
			    list.set(size-1, (String)list.get(size-1) + (String)value);
			    list = null;
			}
		    }
		}
	    }
	    if (list != null) {
		if (name != null) {
		    list.add(name);
		    list.add(value);
		} else copyInto(value, list);
	    }
	}

	return(dest);
    }


    private static Object
    storeAttribute(String name, Object value, int model, int flags, Object dest) {

	ArrayList  list;
	HashMap    map;
	int        index;

	if (dest instanceof ArrayList) {
	    list = (ArrayList)dest;
	    if ((index = list.indexOf(NAMEOF_XML_ATTRIBUTES)) < 0) {
		list.add(NAMEOF_XML_ATTRIBUTES);
		list.add(newStorage(model));
		index = list.indexOf(NAMEOF_XML_ATTRIBUTES);
	    }
	    list.set(index+1, store(name, value, model, flags, list.get(index+1)));
	} else if (dest instanceof HashMap) {
	    map = (HashMap)dest;
	    if (map.containsKey(NAMEOF_XML_ATTRIBUTES) == false)
		map.put(NAMEOF_XML_ATTRIBUTES, newStorage(model));
	    map.put(NAMEOF_XML_ATTRIBUTES, store(name, value, model, flags, map.get(NAMEOF_XML_ATTRIBUTES)));
	}

	return(dest);
    }


    private static Object
    storeBlock(String name, Object value, boolean toss_attributes, int model, int flags, Object dest) {

	ArrayList  list;
	HashMap    map;
	int        size;
	int        index;

	if (value instanceof ArrayList) {
	    list = (ArrayList)value;
	    if (toss_attributes) {
		if ((index = list.indexOf(NAMEOF_XML_ATTRIBUTES)) >= 0) {
		    list.remove(index+1);
		    list.remove(index);
		}
	    }
	    if (list.size() == 0)
		value = NOBLOCK_VALUE;
	} else if (value instanceof HashMap) {
	    map = (HashMap)value;
	    if (toss_attributes) {
		if (map.containsKey(NAMEOF_XML_ATTRIBUTES))
		    map.remove(NAMEOF_XML_ATTRIBUTES);
	    }
	    if (map.size() == 0)
		value = NOBLOCK_VALUE;
	}

	return(store(name, value, model, flags, dest));
    }


    private static Object
    storeText(String text, int masks[], int model, int flags, Object dest) {

	Matcher  matcher;

	if ((flags & masks[0]) != 0) {
	    text = YoixMisc.trim(
		text,
		(flags & masks[1]) != 0 ? XML_WS : null,
		(flags & masks[2]) != 0 ? XML_WS : null
	    );
	}

	matcher = PATTERN_WS_OR_EMPTY.matcher(text);
	if (matcher.find()) {
	    if ((flags&masks[3]) == 0) {
		//
		// If we're not explicitly ignoring CDATA encoded whitespace
		// then we store it as regular text.
		//
		if (masks != CDATA_MASKS)
		    dest = store(NAMEOF_XML_WS, text, model, flags, dest);
		else dest = store(NAMEOF_XML_TEXT, text, model, flags, dest);
	    }
	} else dest = store(NAMEOF_XML_TEXT, text, model, flags, dest);

	return(dest);
    }


    private static YoixObject
    xmlAdd(String names[], YoixObject values, YoixObject dest) {

	YoixObject  key;
	YoixObject  value;
	YoixObject  storage;
	YoixObject  target;
	YoixObject  element;
	ArrayList   targets;
	ArrayList   hits;
	String      name;
	int         index;
	int         m;
	int         n;

	//
	// This was written very quickly for a new application so it's not
	// complete and there's lots of room for improvement in the parts
	// that have been implemented. The code is tricky and may be hard
	// to follow, but it seems to handle the kind of additions done by
	// the application that originally needed the code. We use lvalues
	// extensively in this code as a way to manage an existing storage
	// array and as a reference to a slot in that array, and when we
	// really only are interested in array we create an lvalue with an
	// offset of -1, which can't ever reference a slot in that array.
	//

	if (names != null && dest != null) {
	    if (dest.isNull() || dest.isArray()) {
		if (dest.isNull())
		    dest = YoixObject.newArray(0, -1);
		targets = new ArrayList();
		targets.add(YoixObject.newLvalue(dest, -1));
		for (index = 0; index < names.length; index++) {
		    if ((name = names[index]) != null) {
			if (name.length() > 0) {
			    hits = new ArrayList();
			    for (n = 0; n < targets.size(); n++) {
				target = (YoixObject)targets.get(n);
				if (target.isArray()) {
				    for (m = 0; m < target.length() - 1; m += 2) {
					if ((key = target.getObject(m)) != null && key.isString()) {
					    if (name.equals(key.stringValue())) {
						if ((value = target.getObject(m+1)) != null) {
						    if (index < names.length - 1) {
							if (value.isArray()) {
							    if (hits.contains(value) == false)
								hits.add(YoixObject.newLvalue(value, -1));
							} else if (value.isNull()) {
							    value = YoixObject.newArray(0, -1);
							    target.putObject(m+1, value);
							    hits.add(YoixObject.newLvalue(value, -1));
							} else {
							    //
							    // Eventually deal with dictionaries etc.
							    // Should it be an error until then??
							    //
							}
						    } else if (value.isNull()) {
							//
							// NULL is a placeholder in target for a
							// value that hasn't been officially set
							// yet. Right now it can turn into a string
							// or a storage array. In this case we're
							// looking for the last name, so create an
							// lvalue that references the slot.
							// 
							hits.add(YoixObject.newLvalue(target, m+1));
						    } else {
							if (value.isArray()) {
							    //
							    // Test is a recent addition, once again for
							    // a special project, but it seems reasonable.
							    // Take a closer look before the next release.
							    //
							    // NOTE - the isArray() test was added on 2/21/11
							    // and the isString() test was added on 11/3/11,
							    // but I'm still not completely convinced by all
							    // of this.
							    //
							    if (values.isArray() == false && values.isString() == false) {
								storage = YoixObject.newArray(0, -1);
								storage.putObject(0, value);
								hits.add(storage);
							    }
							} else {
							    //
							    // Eventually deal with dictionaries etc.
							    // Should it be an error until then??
							    //
							}
						    }
						}
					    }
					}
				    }
				} else if (target.compound()) {
				    //
				    // Eventually deal with dictionaries etc. Should it
				    // be an error until then.
				    //
				}
			    }
			    if (hits.size() == 0) {
				for (n = 0; n < targets.size(); n++) {
				    target = (YoixObject)targets.get(n);
				    if ((value = target.getObject(target.offset())) == null) {
					if (target.isArray()) {
					    target.putString(target.length(), name);
					    target.putNull(target.length());
					    hits.add(YoixObject.newLvalue(target, target.length() - 1));
					}
				    } else if (value.isNull()) {
					value = YoixObject.newArray(0, -1);
					value.putString(0, name);
					value.putNull(1);
					target.putObject(target.offset(), value);
					hits.add(YoixObject.newLvalue(value, value.length() - 1));
				    } else {
					//
					// Eventually deal with dictionaries etc. Should it
					// be an error until then.
					//
				    }
				}
			    }
			    targets = hits;
			}
		    }
		}
    
		if (targets.size() > 0) {
		    if (values != null) {
			for (n = 0; n < targets.size(); n++) {
			    target = (YoixObject)targets.get(n);
			    if (values.isArray()) {
				//
				// Would be nice if we could just store the array
				// in the structure, but if we do there are issues
				// (growability, dictionaries etc.) that we really
				// should address first. For now we'll look at the
				// elements in the array but only add the ones that
				// look like a key/value pair with both the key and
				// value being strings.
				//
				// NOTE - the string checks of the value have been
				// removed, so at this point any object can be put
				// in the structure, however the caveats mentioned
				// above are still valid. This probably should be
				// revisited in the near future!!!
				// 
    
				storage = YoixObject.newArray(0, -1);
				for (index = values.offset(); index < values.length() - 1; index += 2) {
				    if ((value = values.getObject(index+1)) != null) {
					if ((key = values.getObject(index)) != null) {
					    //
					    // The next test was added on 11/3/11. Seems
					    // reasonable but not thoroughly tested.
					    //
					    if (value.isString() || value.isArray() || value.isNull()) {
						if (key.isString()) {
						    storage.putObject(storage.length(), key);
						    storage.putObject(storage.length(), value);
						} else if (key.isNull()) {
						    //
						    // This is code that was added quickly for a
						    // special project. Also didn't put any real
						    // thought into what happens if an array has
						    // some null keys and some string keys. Right
						    // now mixing the different kinds of keys is
						    // allowed but it's almost certainly not a
						    // good idea!! All of this stuff needs to be
						    // revisited in the near future!!!!
						    //
						    if ((element = target.getObject(target.length() - 1)) != null) {
							if (element.notNull()) {
							    target.putString(target.length(), names[names.length - 1]);
							    target.putObject(target.length(), value);
							} else target.putObject(target.length() - 1, value);
						    }
						}
					    }
					}
				    }
				}
				if (storage.length() > 0) {
				    if ((value = target.getObject(target.offset())) != null) {
					if (value.isArray())
					    YoixMisc.unrollInto(storage, YoixObject.newLvalue(value, value.sizeof()));
					else if (value.isNull())
					    target.putObject(target.offset(), storage);
				    }
				}
			    } else if (values.isString()) {
				target.putString(target.offset(), values.stringValue());
			    }
			}
		    }
		}
	    }
	}

	return(dest);
    }


    private static YoixObject
    xmlGet(String names[], YoixObject dest, boolean extract) {

	YoixObject  key;
	YoixObject  value;
	YoixObject  hits;
	YoixObject  content;
	String      name;
	int         length;
	int         index;
	int         n;

	if (names != null) {
	    for (index = 0; index < names.length && dest != null; index++) {
		if ((name = names[index]) != null) {
		    if ((length = dest.length()) > 0) {
			hits = YoixObject.newArray(0, -1);
			if (dest.isArray()) {
			    for (n = 0; n < length - 1; n += 2) {
				if ((key = dest.getObject(n)) != null && key.isString()) {
				    if (name.equals(key.stringValue())) {
					if ((value = dest.getObject(n+1)) != null) {
					    if (value.isString()) {
						if (index < names.length - 1) {
						    //
						    // Storing value in consecutive slots here
						    // is intentional.
						    //
						    hits.putObject(hits.length(), value);
						    hits.putObject(hits.length(), value);
						} else hits.putObject(hits.length(), value);
					    } else if (value.isArray() || value.compound()) {
						if (index < names.length - 1) {
						    hits = YoixObject.newLvalue(hits, hits.length());
						    YoixMisc.unrollInto(value, hits);
						    hits = YoixObject.newLvalue(hits, 0);
						} else hits.putObject(hits.length(), value);
					    }
					}
				    }
				}
			    }
			    dest = hits;
			} else if (dest.isDictionary()) {
			    if ((value = dest.getObject(name)) != null) {
				if (value.isString()) {
				    if (index < names.length - 1) {
					hits.putObject(hits.length(), value);
					hits.putObject(hits.length(), value);
				    } else hits.putObject(hits.length(), value);
				} else if (value.isArray() || value.compound()) {
				    if (index < names.length - 1) {
					hits = YoixObject.newLvalue(hits, hits.length());
					YoixMisc.unrollInto(value, hits);
					hits = YoixObject.newLvalue(hits, 0);
				    } else hits.putObject(hits.length(), value);
				}
			    }
			    dest = hits;
			} else dest = null;
		    } else dest = null;
		} else dest = null;
	    }
	} else dest = null;

	if (dest != null) {
	    if (dest.length() == 1) {
		if (extract)
		    dest = dest.getObject(0);
	    } else if (dest.length() == 0)
		dest = null;
	}

	return(dest);
    }


    private static Object
    xmlToJava(SimpleNode tree, int model, int flags, int index, Object dest) {

	SimpleNode  node;
	Object      child;
	int         length;

	//
	// Simple method that's supposed to convert parse trees created by
	// the Yoix XML parser to a representation that uses Java ArrayLists
	// and HashMaps for the represention of the parse tree. Was written
	// quickly (on 8/22/10) for an important application, but probably
	// still needs lots of testing.
	//

	if (tree != null) {
	    length = tree.value.length;
	    for (; index < length; index++) {
		child = tree.value[index];
		if (child instanceof SimpleNode) {
		    node = (SimpleNode)child;
		    switch (node.type) {
			case XMLParserConstants._ATTRIBUTE:
			    dest = storeAttribute(
				((SimpleNode)node.value[0]).value[0].toString(),
				((SimpleNode)node.value[1]).value[0].toString(),
				model,
				flags,
				dest
			    );
			    break;

			case XMLParserConstants._BLOCK:
			    dest = storeBlock(
				((SimpleNode)node.value[0]).value[0].toString(),
				xmlToJava(node, model, flags, 1, newStorage(model, node.value.length > 2)),
				(flags & TOSS_ATTRIBUTES) != 0,
				model,
				flags,
				dest
			    );
			    break;

			case XMLParserConstants._BODY:
			case XMLParserConstants._XML:
			    xmlToJava(node, model, flags, 0, dest);
			    break;

			case XMLParserConstants._CDSECT:
			    dest = storeText(node.value[0].toString(), CDATA_MASKS, model, flags, dest);
			    break;

			case XMLParserConstants._CHARDATA:
			    dest = storeText(node.value[0].toString(), CHARDATA_MASKS, model, flags, dest);
			    break;

			case XMLParserConstants._COMMENT:
			case XMLParserConstants._DTD:
			case XMLParserConstants._PROC_INST:
			case XMLParserConstants._PROLOG:
			    //
			    // Ignoring these means there are lots of others that we
			    // shouldn't encounter. If they somehow get through they
			    // should end up in the default case.
			    //
			    break;

			case XMLParserConstants._CONTENT:
			    dest = store(xmlToJava(node, model, flags, 0, newStorage(model)), model, flags, dest);
			    break;

			case XMLParserConstants._NOBLOCK:
			    dest = store(
				((SimpleNode)node.value[0]).value[0].toString(),
				NOBLOCK_VALUE,
				model,
				flags,
				dest
			    );
			    break;

			default:
			    //
			    // We probably shouldn't get here because we're ignoring
			    // lots of types, but decided to leave it in just in case.
			    // Perhaps we should issue a warning message if we end up
			    // here, at least while this code is being tested - maybe
			    // later.
			    //
			    dest = store(xmlToJava(node, model, flags, 0, newStorage(model)), model, flags, dest);
			    break;
		    }
		} else dest = store(child.toString(), model, flags, dest);	// probably shouldn't get here
	    }
	}
	return(dest);
    }


    private static String
    yoixToXML(YoixObject src, int flags, String indent, int level) {

	StringBuffer  sbuf;
	Matcher       matcher;
	String        text;
	int           length;
	int           n;

	//
	// Elements in an array are assumed to be organized in name/value
	// pairs, so obviously this method doesn't pretend to generate an
	// XML representation for any Yoix object. Instead it's primarily
	// designed to invert the structures produced by xmlToYoix(), but
	// it does try to do something sensible with compound objects.
	//

	sbuf = new StringBuffer();
	if (src.notNull()) {
	    length = src.length();
	    if (src.isArray()) {
		for (n = src.offset(); n < length - 1; n += 2) {
		    if (src.defined(n) && src.readable(n) && src.defined(n+1)) {
			yoixToXML(
			    src.getObject(n).stringValue(),
			    src.readable(n+1) ? src.getObject(n+1) : null,
			    flags,
			    indent,
			    level,
			    sbuf
			);
		    }
		}
	    } else if (src.compound()) {
		for (n = src.offset(); n < length; n++) {
		    if (src.defined(n)) {
			yoixToXML(
			    src.name(n),
			    src.readable(n) ? src.getObject(n) : null,
			    flags,
			    indent,
			    level,
			    sbuf
			);
		    }
		}
	    } else if (src.isString()) {
		text = src.stringValue();
		matcher = PATTERN_NEED_CDATA.matcher(text);
		if (matcher.find() == false) {
		    matcher = PATTERN_CDATA_END.matcher(text);
		    if (matcher.find() == false) {
			matcher = PATTERN_LEADING_OR_TRAILING_WS.matcher(text);
			if (matcher.find())
			    addCDATA(text, sbuf);
			else sbuf.append(text);
		    } else addCDATA(text, sbuf);
		} else addCDATA(text, sbuf);
	    }
	}

	return(sbuf.toString());
    }


    private static void
    yoixToXML(String key, YoixObject value, int flags, String indent, int level, StringBuffer sbuf) {

	YoixObject content;
	Matcher    matcher;
	String     text;

	//
	// The code that checks value and flags to decide whether to output
	// anything was added on 2/1/11. The default behavior matches the
	// previous version and matches the behavior of xmlToYoix().
	//

	if (acceptName(key, flags)) {
	    if ((value != null && value.notNull()) || (flags&OMIT_NULL_ELEMENTS) == 0) {	// added on 2/2/11
		addIndent(indent, level, sbuf);
		sbuf.append("<");
		sbuf.append(key);
		addAttributes(value, sbuf);
		if ((content = value) != null && content.notNull()) {
		    if (content.compound() || content.isArray()) {
			sbuf.append(">");
			addIndent(indent, 0, sbuf);		// newline - if necessary
			sbuf.append(yoixToXML(content, flags, indent, level+1));
			addIndent(indent, level, sbuf);
			sbuf.append("</");
			sbuf.append(key);
			sbuf.append(">");
		    } else {
			text = content.isString() ? content.stringValue() : content.toString().trim();
			if (text.length() > 0) {
			    sbuf.append(">");
			    matcher = PATTERN_NEED_CDATA.matcher(text);
			    if (matcher.find())
				addCDATA(text, sbuf);
			    else sbuf.append(text);
			    sbuf.append("</");
			    sbuf.append(key);
			    sbuf.append(">");
			} else {
			    if ((flags&USE_EMPTY_ELEMENT_TAG) == 0) {
				sbuf.append(">");
				sbuf.append("</");
				sbuf.append(key);
				sbuf.append(">");
			    } else sbuf.append(" />");
			}
		    }
		} else {
		    if ((flags&USE_EMPTY_ELEMENT_TAG) == 0) {
			sbuf.append(">");
			sbuf.append("</");
			sbuf.append(key);
			sbuf.append(">");
		    } else sbuf.append(" />");
		}
	    }
	} else if (NAMEOF_XML_TEXT.equals(key)) {
	    if (value != null && value.isString()) {
		text = value.stringValue();
		if (text.length() > 0) {
		    matcher = PATTERN_NEED_CDATA.matcher(text);
		    if (matcher.find())
			addCDATA(text, sbuf);
		    else sbuf.append(text);
		}
	    }
	}
    }
}

