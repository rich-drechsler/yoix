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

import javax.net.ssl.*;

public
class YwaitHostNameVerifier

    implements HostnameVerifier

{

    //
    // A class that the YWAIT client uses to install a callback method that
    // will be used to validate hostnames for https connections when Java's
    // standard hostname verification logic has failed. By default we always
    // return true, which means the connection won't fail because the host
    // can't be verified, however you're free to change the implementation
    // of invoke() to provide a more secure implementation.
    //

    ///////////////////////////////////
    //
    // YwaitHostNameVerifier Methods
    //
    ///////////////////////////////////

    public boolean
    verify(String hostname, SSLSession session) {

	//
	// This is the callback method that's only called if we're using
	// https and Java's standard hostname verification logic fails.
	// You're responsible for providing the code if you need a more
	// secure implementation.
	// 

	return(true);
    }
}

