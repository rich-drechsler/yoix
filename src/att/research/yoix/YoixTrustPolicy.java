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
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.net.ssl.*;
import javax.swing.*;

class YoixTrustPolicy

    implements Runnable,
               YoixConstants

{

    //
    // Class that provides support functions for SSL certificate trust
    // acceptance. Eventually we will want to expand these capabilities,
    // but this is a start.
    //
    // There was a late addition that was added rather quickly, mostly so
    // the basic functionality would be in place in this release. There's
    // much more we plan on doing (e.g., maintaining a keystore in disk)
    // in the very near future.
    //

    private static KeyStore  trustkeystore = null;
    private static boolean   passwordentered = false;
    private static boolean   storecertificate = false;
    private static String    truststorepassword = null;
    private static String    truststore = null;

    //
    // This is the thread that we use for initialization. We occasionally
    // may need to wait for it to complete.
    //

    private static YoixTrustPolicy  trustpolicy = new YoixTrustPolicy();
    private static YoixThread       trustsetup = null;

    //
    // String displayed by various dialogs when the user is asked for a
    // response about a certificate.
    //

    private static final String  CERTIFICATE_CHECK = "Certificate Check";

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    private
    YoixTrustPolicy() {

	//
	// Private so we only get one of these.
	//

	super();
    }

    ///////////////////////////////////
    //
    // Runnable Interface
    //
    ///////////////////////////////////

    public synchronized void
    run() {

	TrustManagerFactory  tmf;
	FileInputStream      fis;
	TrustManager         tms[];
	TrustManager         tm;
	SSLContext           sslctxt;
	String               key = "javax.net.ssl.trustStore";
	String               pswd = "javax.net.ssl.trustStorePassword";
	int                  n;

	try {
	    if (trustkeystore == null) {
		trustkeystore = KeyStore.getInstance("JKS");
		truststore = YoixTrustPolicy.getTrustStore(key);
		try {
		    fis = new FileInputStream(truststore);
		}
		catch(IOException e) { // file may not exist yet
		    fis = null;
		}
		try {
		    trustkeystore.load(fis, null); // OK if fis is null
		}
		catch(CertificateException e) {}
		catch(NoSuchAlgorithmException e) {}
	    }
	    tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	    tmf.init(trustkeystore);
	    tms = tmf.getTrustManagers();

	    for (n = 0; n < tms.length; n++) {
		tm = tms[n];
		if (tm instanceof X509TrustManager) {
		    final X509TrustManager ftm = (X509TrustManager)tm; // need this to be final
		    tms[n] = new X509TrustManager() {
			public final X509Certificate[]
			getAcceptedIssuers() {

			    return(ftm.getAcceptedIssuers());
			}

			public final void
			checkClientTrusted(X509Certificate certs[], String authType) throws CertificateException {

			    ftm.checkClientTrusted(certs, authType);
			}

			public final void
			checkServerTrusted(X509Certificate certs[], String authType) throws CertificateException {

			    if (VM.getAcceptCertificates() == false) {
				try {
				    ftm.checkServerTrusted(certs, authType);
				}
				catch(Exception e) {
				    if (certs != null && certs.length > 0) {
					if (!YoixTrustPolicy.checkCertificateTrusted(certs[0])) {
					    //
					    // probably not a good idea to synchronized with
					    // the dialogs inside, but we use static
					    // variables to pass information around and
					    // let's be optimistic that multiple threading
					    // at this point won't usually be an issue
					    //
					    synchronized(trustkeystore) {
						if (!YoixTrustPolicy.showPrompt(certs[0])) {
						    if (e instanceof CertificateException)
							throw((CertificateException)e);
						    else throw(new CertificateException());
						} else YoixTrustPolicy.addCertificateToTrustStore(certs[0]);
					    }
					}
				    }
				}
			    }
			}
		    };
		    break;
		}
	    }
	    sslctxt = SSLContext.getInstance("SSL");
	    sslctxt.init(null, tms, new java.security.SecureRandom());
	    HttpsURLConnection.setDefaultSSLSocketFactory(sslctxt.getSocketFactory());
	}
	catch(Exception e) {}
	finally {
	    notifyAll();
	}
    }

    ///////////////////////////////////
    //
    // YoixTrustPolicy Methods
    //
    ///////////////////////////////////

    static void
    setupTrustManager() {

	//
	// We run the initialization in a separate thread to eliminate the
	// possibility of hanging the interpreter at startup because some
	// implementations (e.g., Linux) end up trying to access the network
	// as part of the SSLContext getSocketFactory call. There's a chance
	// we will want to be a little smarter - later.
	//
	// NOTE - this may still be called from YoixMain, but we eventually
	// want to postpone the setup until it's really needed (i.e., script
	// really uses https or wants to install a strict security manager
	// that might complain some of the setup code).
	//

	if (trustsetup == null) {
	    synchronized(trustpolicy) {
		if (trustsetup == null) {
		    trustsetup = new YoixThread(trustpolicy);
		    trustsetup.setPriority(Math.max(Thread.currentThread().getPriority() - 1, Thread.MIN_PRIORITY));
		    trustsetup.start();
		}
	    }
	}
    }


    static URL
    setupURL(URL url) {

	//
	// This can be called when we want to make sure the setup code has
	// finished if it looks like we're going to be using https. In our
	// older releases we started the setup in YoixMain, but we didn't
	// guarantee it finished when tried to use https and that resulted
	// in unpredictable failures when we tried to open URL that used
	// https before the setup had completed. It didn't happen much but
	// a command line URL was particularly vulnerable. This should fix
	// the behavior, however there's a small chance the synchronization
	// that we added may cause problems. Suspect we eventually need to
	// take a closer look at everything in this class.
	// 
	// NOTE - this method should be able to handle all the setup, but
	// just in case we probably still start things in YoixMain. There's
	// also a chance we need to finish the setup before installing a
	// security manager, and that would mean we probably want a method
	// that only synchronizes on trustpolicy - later.
	// 

	if (url != null) {
	    if (url.getProtocol().startsWith("https")) {
		synchronized(trustpolicy) {
		    if (trustsetup == null) {
			setupTrustManager();
			try {
			    trustpolicy.wait();
			}
			catch(InterruptedException e) {}
		    }
		}
	    }
	}
	return(url);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static void
    addCertificateToTrustStore(X509Certificate certificate) {

	FileOutputStream  ostream;
	boolean           success = false;
	String            alias;

	alias = YoixTrustPolicy.getCertificateAlias(certificate);
	try {
	    trustkeystore.setCertificateEntry(alias, certificate);
	}
	catch(KeyStoreException e) {}
	if (!passwordentered && storecertificate) {
	    passwordentered = true;
	    truststorepassword = getPassword();
	}
	if (truststorepassword != null && storecertificate) {
	    try {
		ostream = new FileOutputStream(truststore);
		trustkeystore.store(ostream, truststorepassword.toCharArray());
		success = true;
		ostream.close();
	    }
	    catch(FileNotFoundException e) {}
	    catch(SecurityException e) {}
	    catch(KeyStoreException e) {}
	    catch(IOException e) {}
	    catch(NoSuchAlgorithmException e) {}
	    catch(CertificateException e) {}
	}
    }


    private static boolean
    checkCertificateTrusted(X509Certificate certificate) {

	boolean  trusted = false;
	String   alias;
	Object   ocert;

	if (certificate != null) {
	    alias = YoixTrustPolicy.getCertificateAlias(certificate);
	    if (alias != null && trustkeystore != null) {
		try {
		    ocert = trustkeystore.getCertificate(alias);
		    trusted = certificate.equals(ocert);
		}
		catch(KeyStoreException e) {}
	    }
	}
	return(trusted);
    }


    private static String
    getCertificateAlias(X509Certificate certificate) {

	//
	// Maybe we should parse this "alias" further and just take everything
	// up to the first whitespace... but will that be unique enough?
	//

	return(splitDN(certificate.getSubjectDN().getName()));
    }


    private static String
    getPassword() {

	JPasswordField  jp1;
	JPasswordField  jp2;
	JLabel          jl;
	JPanel          jp;
	String          pswd = null;
	String          pw1;
	String          pw2;
	int             response;
	int             count = 0;

	try {
	    jp = new JPanel();
	    jp.setLayout(new GridLayout(3, 2, 2, 2));
	    jp.add(new JLabel("Enter yoixTrustStore Password:", SwingConstants.RIGHT));
	    jp.add(jp1 = new JPasswordField(25));
	    jp.add(new JLabel("Re-Enter Password:", SwingConstants.RIGHT));
	    jp.add(jp2 = new JPasswordField(25));
	    jp.add(new JPanel()); // filler
	    jp.add(jl = new JLabel("", SwingConstants.RIGHT));
	    jl.setForeground(Color.red);
	    jp.setBorder(BorderFactory.createTitledBorder("yoixTrustStore Password Entry"));
	    do {
		count++;
		response = JOptionPane.showOptionDialog(
		    null,
		    jp,
		    CERTIFICATE_CHECK,
		    JOptionPane.OK_CANCEL_OPTION,
		    JOptionPane.PLAIN_MESSAGE,
		    null,
		    new String[] {"Enter", "Cancel"},
		    "Enter"
		);
		if (response == JOptionPane.OK_OPTION) {
		    pw1 = jp1.getText().trim();
		    pw2 = jp2.getText().trim();
		    if (!pw1.equals(pw2)) {
			if (count < 3)
			    jl.setText("Mismatch - try again.");
			else jl.setText("Mismatch - last try!");
		    } else {
			pswd = pw1;
			break;
		    }
		} else break;
	    } while(count <= 3);
	}
	catch(HeadlessException e) {}

	return(pswd);
    }


    private static String
    getTrustStore(String key) {

	String  path;
	String  home;

	if ((path = System.getProperty(key)) == null || path.length() == 0) {
	    if ((home = System.getProperty("user.home")) == null || home.length() == 0) {
		if ((home = System.getProperty("user.dir")) == null || home.length() == 0)
		    home = ".";
	    }
	    path = YoixMisc.toYoixPath(home) + "/.yoixTrustStore";
	} else path = YoixMisc.toYoixPath(path);

	return(YoixMisc.toLocalPath(path));
    }


    private static boolean
    showCertificate(X509Certificate certificate) {

	JPanel  jp;
	int     response;

	try {
	    jp = new JPanel();
	    jp.add(new JScrollPane(new JTextArea(certificate.toString(), 10, 60)));
	    jp.setBorder(BorderFactory.createTitledBorder(CERTIFICATE_CHECK));
	    response = JOptionPane.showOptionDialog(
		null,
		jp,
		CERTIFICATE_CHECK,
		JOptionPane.YES_NO_CANCEL_OPTION,
		JOptionPane.QUESTION_MESSAGE,
		null,
		new String[] {"Accept/Store", "Accept Only", "Reject"},
		"Accept/Store"
	    );
	}
	catch(HeadlessException e) {
	    response = JOptionPane.CANCEL_OPTION;
	}
	storecertificate = (response == JOptionPane.YES_OPTION);

	return(response != JOptionPane.CANCEL_OPTION);
    }


    private static boolean
    showPrompt(X509Certificate certificate) {

	String  labels[];
	String  subject;
	String  issuer;
	String  message;
	String  validity;
	JLabel  jl;
	JPanel  jp;
	Date    notafter;
	Date    notbefore;
	Date    now;
	int     response;
	int     valid = 0;
	int     n;

	subject = splitDN(certificate.getSubjectDN().getName());
	issuer = splitDN(certificate.getIssuerDN().getName());

	notafter = certificate.getNotAfter();
	notbefore = certificate.getNotBefore();
	now = new Date();

	if (now.before(notbefore))
	    valid = -1;
	else if (now.after(notafter))
	    valid = 1;

	message = YoixMisc.fmt(
	    "Cannot vouch for certificate from:\n\n    " + subject +
	    "\n\nIssuer is:\n\n    " + issuer +
	    "\n\nValidity:\n\n    " + notbefore + "->" + notafter,
	    75, 85, false, false
	);

	labels = splitLabels(message);

	jp = new JPanel();
	jp.setLayout(new GridLayout(3 + labels.length + (valid == 0 ? 0 : 1),1,2,2));
	jp.add(new JLabel(CERTIFICATE_CHECK));
	jp.add(new JPanel()); // spacer
	for (n=0; n<labels.length; n++)
	    jp.add(new JLabel(labels[n]));
	if (valid != 0) {
	    jl = null; // for compiler
	    if (valid < 0)
		jp.add(new JLabel("Certificate is not valid until a future date!"));
	    else if (valid > 0)
		jp.add(new JLabel("Certificate has expired!"));
	    jl.setForeground(Color.red);
	}
	jp.add(new JPanel()); // spacer

	try {
	    response = JOptionPane.showOptionDialog(
		null,
		jp,
		CERTIFICATE_CHECK,
		-1,
		JOptionPane.QUESTION_MESSAGE,
		null,
		new String[] {"Accept/Store", "Accept Only", "Reject", "Show Certificate"},
		"Accept/Store"
	    );
	}
	catch(HeadlessException e) {
	    response = 2;
	}
	storecertificate = (response == 0);
	return(response == 3 ? showCertificate(certificate) : response != 2);
    }


    private static String
    splitDN(String dn) {

	StringTokenizer  st;
	StringBuffer     sb;
	String           tok;

	st = new StringTokenizer(dn, "=,\"", true);
	sb = new StringBuffer();
	while (st.hasMoreTokens()) {
	    tok = st.nextToken();
	    if ("=".equals(tok)) {
		// we want what follows the '=', but discard what is before it
		if (st.hasMoreTokens()) {
		    tok = st.nextToken();
		    if ("\"".equals(tok)) {
			// take everything up to, but not including,
			// the next double quote (including tokens)
			while (st.hasMoreTokens()) {
			    tok = st.nextToken();
			    if ("\"".equals(tok))
				break;
			    sb.append(tok);
			}
		    } else sb.append(tok);
		}
	    } else if (",".equals(tok))
		sb.append(", ");
	}
	return(sb.toString());
    }


    private static String[]
    splitLabels(String message) {

	StringTokenizer  st;
	String           result[];
	int              n;

	st = new StringTokenizer(message, NL, false);
	result = new String[st.countTokens()];

	for (n = 0; st.hasMoreTokens(); n++)
	    result[n] = st.nextToken();
	return(result);
    }
}

