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
import java.net.*;

public
class YoixClassLoader extends URLClassLoader

    implements YoixConstants

{

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    YoixClassLoader(ClassLoader parent) {

	this(new URL[] {}, parent);
    }


    public
    YoixClassLoader(URL urls[], ClassLoader parent) {

	super(urls, parent);
    }


    public
    YoixClassLoader(URL urls[]) {

	super(urls);
    }


    public
    YoixClassLoader(URL urls[], ClassLoader parent, URLStreamHandlerFactory factory) {

	super(urls, parent, factory);
    }

    ///////////////////////////////////
    //
    // YoixClassLoader Methods
    //
    ///////////////////////////////////

    public void
    addURL(URL url) {

	if (url != null)
	    super.addURL(url);
    }


    public Class
    findClass(String name)

	throws ClassNotFoundException

    {

	return(super.findClass(name));
    }
}
