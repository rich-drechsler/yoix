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

import java.security.cert.*;
import javax.net.ssl.*;

public
class YwaitX509TrustManager

	implements X509TrustManager

{

    //
    // A class that the YWAIT client uses to validate X509Certificates that
    // it receives from the https servers it's trying contact. By default we
    // accept all X509Certificate server certificate chains, however you're
    // free to change the methods in this class if you need a more secure
    // implementation.
    //

    ///////////////////////////////////
    //
    // YwaitX509TrustManager Methods
    //
    ///////////////////////////////////

    public final void
    checkClientTrusted(X509Certificate certs[], String authType)

	throws CertificateException

    {

	//
	// This shouldn't be needed so we always throw the exception.
	//

	throw(new CertificateException());
    }


    public final void
    checkServerTrusted(X509Certificate certs[], String authType)

	throws CertificateException

    {

	//
	// Returns if we accept the certs[] certificate chain, otherwise it
	// should throw a CertificateException. By default we always accept
	// certs[], so you're responsible for providing the code if you need
	// a more secure implementation.
	//
    }


    public final X509Certificate[]
    getAcceptedIssuers() {

	//
	// Don't think this is needed so we return an empty list.
	//

	return(new X509Certificate[0]);
    }
}

