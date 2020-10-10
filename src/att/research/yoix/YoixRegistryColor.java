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

abstract
class YoixRegistryColor

    implements YoixConstants

{

    private static YoixObject  registry = null;
    private static boolean     xloaded = false;

    ///////////////////////////////////
    //
    // YoixRegistryColor Methods
    //
    ///////////////////////////////////

    static boolean
    addColor(String name, Color color) {

	YoixObject  obj;
	String      canonical;
	String      oldname;
	Color       loadedcolor;
	char        chars[];
	char        newchars[];
	int         idx;
	int         jdx;

	if (name != null && color != null) {
	    if (registry == null)
		setRegistry(null);
	    synchronized(registry) {
		if ((loadedcolor = javaColor(name)) == null) {
		    canonical = canonicalColorName(name);
		    if ((loadedcolor = javaColor(canonical)) == null || color.equals(loadedcolor)) {
			VM.pushAccess(LRW_);
			registry.setGrowable(true);
			registry.putColor(name, color);
			if (loadedcolor == null && !name.equals(canonical))
			    registry.putColor(canonical, color);
			registry.setGrowable(false);
			obj = registry.getObject(name);
			obj.setAccessBody(LR__);
			VM.popAccess();
			if (YoixMisc.reversecolor.get(color) == null)
			    YoixMisc.reversecolor.put(color, canonical);
		    } else {
			chars = canonical.toCharArray();
			newchars = new char[chars.length + 3];
			System.arraycopy(chars, 0, newchars, 0, jdx = chars.length);
			idx = 0;
			do {
			    newchars[jdx] = (char)(48 + (int)(idx/100));
			    newchars[jdx+1] = (char)(48 + (int)((idx%100)/10));
			    newchars[jdx+2] = (char)(48 + (int)(idx%10));
			    idx++;
			    canonical = new String(newchars);
			} while ((loadedcolor = javaColor(canonical)) != null && !color.equals(loadedcolor) && idx < 1000);
			if (idx < 1000) {
			    VM.pushAccess(LRW_);
			    registry.setGrowable(true);
			    registry.putColor(name, color);
			    if (loadedcolor == null && !name.equals(canonical))
				registry.putColor(canonical, color);

			    registry.setGrowable(false);
			    obj = registry.getObject(name);
			    obj.setAccessBody(LR__);
			    VM.popAccess();
			    if (YoixMisc.reversecolor.get(color) == null)
				YoixMisc.reversecolor.put(color, canonical);
			} else VM.abort(BADVALUE);
		    }
		} else color = color.equals(loadedcolor) ? color : null;
	    }
	} else color = null;

	return(color != null);
    }


    static boolean
    addColor(YoixObject colordict) {

	YoixObject  yobj;
	boolean     added = false;
	String      name;
	Color       color;
	int         idx;
	int         len;

	if (registry == null)
	    setRegistry(null);

	if (colordict != null) {
	    len = colordict.length();
	    for (idx = colordict.offset(); idx < len; idx++) {
		name = colordict.name(idx);
		if ((yobj = colordict.getObject(name)) != null) {
		    //
		    // For speed, we can assume it is a color -- if not, we'll
		    // just end up with white.
		    //
		    if ((color = YoixMake.javaColor(yobj)) != null) {
			if (YoixRegistryColor.addColor(name, color))
			    added = true;
		    }
		}
	    }
	}

	return(added);
    }


    static boolean
    addColor() {

	boolean  retval = false;

	//
	// The YoixModuleXColor.addColor() call was added after we decided
	// that loading the xcolor module shouldn't automatically trigger
	// an expansion of the Color dictionary. Changed on 3/10/2010.
	//

	if (registry == null)
	    setRegistry(null);

	if (!xloaded) {
	    if (YoixModule.load(YoixModuleXColor.class) != null) {
		setXLoaded();
		YoixModuleXColor.addColor();
	    }
	}
	return(xloaded);
    }


    static synchronized void
    setXLoaded() {

	xloaded = true;
    }


    static synchronized void
    setRegistry(YoixObject colordict) {

	YoixObject  yobj;
	YoixObject  obj;
	boolean     added = false;
	String      name;
	String      canonical;
	String      oldname;
	Color       color;
	Color       loadedcolor;
	char        chars[];
	char        newchars[];
	int         off;
	int         len;
	int         idx;
	int         jdx;

	if (registry == null) {
	    if (colordict == null)
		YoixModule.load(YoixModuleJFC.class);
	    else {
		registry = colordict;
		len = colordict.length();
		for (off = colordict.offset(); off < len; off++) {
		    name = colordict.name(off);
		    color = javaColor(name);
		    canonical = canonicalColorName(name);
		    if (name.equals(canonical)) {
			YoixMisc.reversecolor.put(color, canonical);
		    } else if ((loadedcolor = javaColor(canonical)) == null) {
			VM.pushAccess(LRW_);
			registry.setGrowable(true);
			registry.putColor(canonical, color);
			registry.setGrowable(false);
			obj = registry.getObject(name);
			obj.setAccessBody(LR__);
			VM.popAccess();
			if (YoixMisc.reversecolor.get(color) == null)
			    YoixMisc.reversecolor.put(color, canonical);
		    } else if (color.equals(loadedcolor)) {
			if (YoixMisc.reversecolor.get(color) == null)
			    YoixMisc.reversecolor.put(color, canonical);
		    } else {
			chars = canonical.toCharArray();
			newchars = new char[chars.length + 3];
			System.arraycopy(chars, 0, newchars, 0, jdx = chars.length);
			idx = 0;
			do {
			    newchars[jdx] = (char)(48 + (int)(idx/100));
			    newchars[jdx+1] = (char)(48 + (int)((idx%100)/10));
			    newchars[jdx+2] = (char)(48 + (int)(idx%10));
			    idx++;
			    canonical = new String(newchars);
			} while ((loadedcolor = javaColor(canonical)) != null && !color.equals(loadedcolor) && idx < 1000);
			if (idx < 1000) {
			    VM.pushAccess(LRW_);
			    registry.setGrowable(true);
			    registry.putColor(canonical, color);
			    registry.setGrowable(false);
			    obj = registry.getObject(name);
			    obj.setAccessBody(LR__);
			    VM.popAccess();
			    if (YoixMisc.reversecolor.get(color) == null)
				YoixMisc.reversecolor.put(color, canonical);
			} else VM.abort(BADVALUE);
		    }
		}
	    }
	}
    }


    static Color
    javaColor(String name) {

	return(registry != null ? YoixMake.javaColor(registry.getObject(name)) : null);
    }


    private static String
    canonicalColorName(String name) {

	String  canonical = null;
	char    chars[];
	int     idx;
	int     jdx;

	if (name != null) {
	    canonical = name.toLowerCase().trim();
	    chars = canonical.toCharArray();
	    for (idx=0, jdx=0; idx < chars.length; idx++) {
		if (Character.isLetterOrDigit(chars[idx]))
		    chars[jdx++] = chars[idx];
	    }
	    if (jdx == 0)
		canonical = "color";
	    else if (jdx < idx)
		canonical = new String(chars, 0, jdx);
	}
	return(canonical);
    }
}

