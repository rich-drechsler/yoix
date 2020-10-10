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

final
class YoixBodyLocale extends YoixPointerActive

    implements YoixConstants

{

    private Locale  locale;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	N_LANGUAGE,         $LR__,       $LR__,
	N_COUNTRY,          $LR__,       $LR__,
	N_VARIANT,          $LR__,       $LR__,
    };

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyLocale(YoixObject data) {

	this(data, null);
    }


    YoixBodyLocale(YoixObject data, Locale locale) {

	super(data);
	if (locale != null) {
	    this.locale = locale;
	    buildData(false);
	} else buildLocale();

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

	return(LOCALE);
    }

    ///////////////////////////////////
    //
    // YoixBodyLocale Methods
    //
    ///////////////////////////////////

    protected final void
    finalize() {

	locale = null;
	super.finalize();
    }


    protected final Object
    getManagedObject() {

	return(locale);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildData(boolean failed) {

	Locale  locale = this.locale;

	VM.pushAccess(RW_);

	try {
	    data.putString(N_LANGUAGE, locale.getLanguage());
	    data.putString(N_COUNTRY, locale.getCountry());
	    data.putString(N_VARIANT, locale.getVariant());
	    data.putString(N_DISPLAYLANGUAGE, locale.getDisplayLanguage());
	    data.putString(N_DISPLAYCOUNTRY, locale.getDisplayCountry());
	    data.putString(N_DISPLAYVARIANT, locale.getDisplayVariant());
	    try {
		data.putString(N_ISO3LANGUAGE, locale.getISO3Language());
	    }
	    catch(MissingResourceException e) {
		data.putString(N_ISO3LANGUAGE, "");
	    }
	    try {
		data.putString(N_ISO3COUNTRY, locale.getISO3Country());
	    }
	    catch(MissingResourceException e) {
		data.putString(N_ISO3COUNTRY, "");
	    }
	    data.putString(N_DISPLAYNAME, locale.getDisplayName());
	}
	catch(RuntimeException e) {
	    if (failed == false) {
		this.locale = Locale.getDefault();
		buildData(true);
	    } else {		// just in case...
		VM.recordException(e);
		VM.abort(EXCEPTION);
	    }
	}

	VM.popAccess();
    }


    private void
    buildLocale() {

	Locale  locale = null;
	String  language = data.getString(N_LANGUAGE, "");
	String  country = data.getString(N_COUNTRY, "");
	String  variant = data.getString(N_VARIANT, "");

	if (language.length() == 0)
	    language = Locale.getDefault().getLanguage();

	if (country.length() == 0)
	    country = Locale.getDefault().getCountry();

	if (variant.length() == 0)
	    locale = new Locale(language, country);
	else locale = new Locale(language, country, variant);

	this.locale = locale;
	buildData(false);
    }
}

