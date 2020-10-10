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

package att.research.yoix.j3d;
import java.awt.*;
import java.util.HashMap;
import javax.media.j3d.RestrictedAccessException;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import att.research.yoix.*;

class BodyTexture2D extends BodyTexture

    implements Constants

{

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	NL_BOUNDARY,        $LR__,       null,
	NL_FILTER4,         $LR__,       null,
	NL_FORMAT,          $LR__,       null,
	NL_HEIGHT,          $LR__,       null,
	NL_IMAGE,           $LR__,       null,
	NL_MAGFILTER,       $LR__,       null,
	NL_MINFILTER,       $LR__,       null,
	NL_MIPMAP,          $LR__,       null,
	NL_SHARPEN,         $LR__,       null,
	NL_WIDTH,           $LR__,       null,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(25);

    static {
	activefields.put(NL_ANISOTROPICDEGREE, new Integer(VL_ANISOTROPICDEGREE));
	activefields.put(NL_BASELEVEL, new Integer(VL_BASELEVEL));
	activefields.put(NL_BOUNDARY, new Integer(VL_BOUNDARY));
	activefields.put(NL_CAPABILITIES, new Integer(VL_CAPABILITIES));
	activefields.put(NL_COMPILED, new Integer(VL_COMPILED));
	activefields.put(NL_DEFAULTCAPABILITY, new Integer(VL_DEFAULTCAPABILITY));
	activefields.put(NL_ENABLED, new Integer(VL_ENABLED));
	activefields.put(NL_FILTER4, new Integer(VL_FILTER4));
	activefields.put(NL_FORMAT, new Integer(VL_FORMAT));
	activefields.put(NL_HEIGHT, new Integer(VL_HEIGHT));
	activefields.put(NL_IMAGE, new Integer(VL_IMAGE));
	activefields.put(NL_LIVE, new Integer(VL_LIVE));
	activefields.put(NL_LODOFFSET, new Integer(VL_LODOFFSET));
	activefields.put(NL_LODRANGE, new Integer(VL_LODRANGE));
	activefields.put(NL_MAGFILTER, new Integer(VL_MAGFILTER));
	activefields.put(NL_MAXIMUMLEVEL, new Integer(VL_MAXIMUMLEVEL));
	activefields.put(NL_MINFILTER, new Integer(VL_MINFILTER));
	activefields.put(NL_MIPMAP, new Integer(VL_MIPMAP));
	activefields.put(NL_MIPMAPLEVELS, new Integer(VL_MIPMAPLEVELS));
	activefields.put(NL_PATH, new Integer(VL_PATH));
	activefields.put(NL_SHARPEN, new Integer(VL_SHARPEN));
	activefields.put(NL_WIDTH, new Integer(VL_WIDTH));
    }

    //
    // A table that's used to control capabilities - low level setup
    // happens once when the loadCapabilities() methods are called in
    // the static initialization block that follows the table. Current
    // implementation seems error prone because we're required to pass
    // the correct classes to loadCapabilities(), so be careful if you
    // copy this stuff to different classes!!
    //

    private static Object  capabilities[] = {
     //
     // NAME                        CAPABILITY                                     VALUE
     // ----                        ----------                                     -----
    };

    static {
	loadCapabilities(BodyTexture.class, BodyTexture2D.class);
	loadCapabilities(capabilities, BodyTexture2D.class);
	capabilities = null;
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    BodyTexture2D(J3DObject data) {

	this(null, data);
    }


    BodyTexture2D(Texture2D texture) {

	this(texture, (J3DObject)VM.getTypeTemplate(T_TEXTURE2D));
    }


    private
    BodyTexture2D(Texture2D texture, J3DObject data) {

	super(texture, data);
	buildTexture2D(texture);
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

	return(TEXTURE2D);
    }

    ///////////////////////////////////
    //
    // BodyTexture2D Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj = null;
	int         field;

	try {
	    switch (field = activeField(name, activefields)) {
		default:
		    obj = executeField(field, name, argv);
		    break;
	    }
	}
	catch(RestrictedAccessException e) {
	    abort(e, name);
	}
	return(obj);
    }


    protected void
    finalize() {

	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	int  field;

	try {
	    switch (field = activeField(name, activefields)) {
		default:
		    obj = getField(field, obj);
		    break;
	    }
	}
	catch(RestrictedAccessException e) {
	    abort(e, name);
	}
	return(obj);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	int  field;

	if (obj != null) {
	    try {
		switch (field = activeField(name, activefields)) {
		    default:
			setField(field, obj);
			break;
		}
	    }
	    catch(RestrictedAccessException e) {
		abort(e, name);
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
    buildTexture2D(Texture2D texture) {

	YoixObject  obj;
	boolean     mipmap;
	String      format;

	if ((this.texture = texture) == null) {
	    obj = getObject(NL_IMAGE);
	    if (J3DObject.isTextureSource(obj)) {
		mipmap = getBoolean(NL_MIPMAP);
		format = J3DMake.javaString("TextureLoader", getObject(NL_FORMAT));
		if ((this.texture = Make.javaTexture(obj, format, mipmap)) != null)
		    peer = this.texture;
		else VM.abort(BADVALUE, NL_IMAGE);	// probably image.source
	    } else VM.abort(TYPECHECK, NL_IMAGE);

	    setField(NL_DEFAULTCAPABILITY);
	    setField(NL_ANISOTROPICDEGREE);
	    setField(NL_BASELEVEL);
	    setField(NL_MAXIMUMLEVEL);
	    setField(NL_MAGFILTER);
	    setField(NL_MINFILTER);
	    setField(NL_LODRANGE);
	    setField(NL_LODOFFSET);
	    setField(NL_SHARPEN);
	    setField(NL_FILTER4);
	    setField(NL_ENABLED);
	}
	setField(NL_CAPABILITIES);
    }
}

