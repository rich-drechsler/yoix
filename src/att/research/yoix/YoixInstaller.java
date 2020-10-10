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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.*;
import javax.swing.*;

public
class YoixInstaller extends ClassLoader

    implements ActionListener,
	       WindowListener

{

    //
    // Startup performance has been significantly improved by letting
    // unzip() reads entire files whenever possible, but there's still
    // lots that could be done to improve performance and decrease the
    // memory usage (e.g., only unpack the Yoix class files that are
    // really used).
    //

    private static String  argv[];
    private static int     argc = 0;
    private static int     argn = 0;
    private static Vector  argvec;

    //
    // These are usually initialized in a static block that's added to
    // this source file by a C program that's called when we build an
    // image of the web site.
    //

    private static String  hexedstrings[];
    private static int     bytecount;

    //
    // First job is to unpack the included binary data and load the
    // required Yoix classes. Currently extracts all entries from the
    // yoix.jar file, even though most won't be used, but a selective
    // approach would not be too difficult - maybe next time.
    //

    private static YoixInstaller  classloader = new YoixInstaller();
    private static Hashtable      unzipped = null;
    private static Hashtable      yoixclasses = null;
    private static Method         main = null;
    private static String         firstjar = null;
    private static byte           unhexed[] = null;

    private static final String  YOIXMAIN = "att.research.yoix.YoixMain";
    private static final Class   ARGS[] = new Class[] {(new String[0]).getClass()};
    private static final int     BLOCKSIZE = 25000;

    private static JTextArea  splashstatus = null;
    private static JDialog    splashscreen = null;
    private static JLabel     splashfooter = null;

    //
    // Stuff that we may need for checking Java versions. Much of this
    // was copied from YoixMisc.java, so we may be doing more work than
    // is really necessary. For example, is a missing JAVARUNTIMEVERSION
    // or JAVAVMVERSION sufficient to detect versions that are too old?
    // We may revisit this in a future release.
    //

    private static final String  MINVERSION = "1.5.0";
    private static final String  JAVARUNTIMEVERSION = System.getProperty("java.runtime.version");
    private static final String  JAVAVERSION = System.getProperty("java.version");
    private static final String  JAVAVMVERSION = System.getProperty("java.vm.version");

    //
    // Stuff used to build messages that are displayed in dialogs when we
    // think there's a problem.
    //

    private static final String  BADVERSION[] = {
	"Bad Java Version",
	"\n",
	" Your version of Java is too old to be used with this installer.\n",
	" We require version " + MINVERSION + " or newer, which should be available at:\n",
	"\n",
	"         http://java.sun.com/downloads/\n",
	"\n",
	" Look under J2SE and probably start by choosing one of the JRE\n",
	" packages. Download the larger SDK package if you have trouble\n",
	" with JRE or want to build your own Java applications.\n",
    };

    private static Hashtable  actionperformed = new Hashtable();

    //
    // Offical copyright notice - prominently displayed by splashscreen and
    // also printed on standard output.
    //

    private static final String  YOIXINSTALLER = "Yoix Installer";
    private static final String  COPYRIGHT = "Copyright AT&T 2000-2009";
    private static final Color   LIGHTBACKGROUND = new Color(191, 191, 217);
    private static final Color   DARKBACKGROUND = LIGHTBACKGROUND.darker();

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixInstaller() {

    }

    ///////////////////////////////////
    //
    // ActionListener Methods
    //
    ///////////////////////////////////

    public void
    actionPerformed(ActionEvent e) {

	Object  source;
	Object  owner;

	if ((source = e.getSource()) != null) {
	    if ((owner = actionperformed.get(source)) != null) {
		if (owner instanceof Dialog) {
		    ((Dialog)owner).setVisible(false);
		    ((Dialog)owner).dispose();
		}
		actionperformed.remove(owner);
	    }
	}
    }
    ///////////////////////////////////
    //
    // WindowListener Methods
    //
    ///////////////////////////////////

    public void
    windowActivated(WindowEvent e) {

    }


    public void
    windowClosed(WindowEvent e) {

    }


    public void
    windowClosing(WindowEvent e) {

	System.exit(1);
    }


    public void
    windowDeactivated(WindowEvent e) {

    }


    public void
    windowDeiconified(WindowEvent e) {

    }


    public void
    windowIconified(WindowEvent e) {

    }


    public void
    windowOpened(WindowEvent e) {

    }

    ///////////////////////////////////
    //
    // ClassLoader Methods
    //
    ///////////////////////////////////

    public Class
    loadClass(String name) {

	return(loadClass(name, false));
    }


    public Class
    loadClass(String name, boolean resolve) {

	String  key;
	Class   c;
	byte    data[];

	if ((c = findLoadedClass(name)) == null) {
	    try {
		c = findSystemClass(name);
	    }
	    catch(Exception e) {}

	    if (c == null) {
		if (yoixclasses != null) {
		    key = name.replace('.', '/') + ".class";
		    data = (byte[])yoixclasses.get(key);
		    try {
			c = defineClass(name, data, 0, data.length);
			if (c != null && resolve)
			    resolveClass(c);
		    }
		    catch(Throwable t) {}
		}
	    }
	}

	return(c);
    }

    ///////////////////////////////////
    //
    // YoixInstaller Methods
    //
    ///////////////////////////////////

    public static void
    main(String args[]) {

	argv = args;
	argc = args.length;
	argn = 0;
	argvec = new Vector();

	validate();
	setup();
	arguments();
	invoke();
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static void
    arguments() {

	int  n;

	argvec.addElement("--");
	argvec.addElement("-");
	while (argn < argc)
	    argvec.addElement(argv[argn++]);

	argv = new String[argvec.size()];
	for (n = 0; n < argv.length; n++)
	    argv[n] = (String)argvec.elementAt(n);
    }


    private static void
    invoke() {

	try {
	    showStatus("Starting the installer - this may take a few seconds\n");
	    System.setIn(new ZipInputStream(new ByteArrayInputStream(unhexed)));
	    main.invoke(null, new Object[] {argv});
	}
	catch(Exception e) {
	    if (e instanceof InvocationTargetException)
		((InvocationTargetException)e).getTargetException().printStackTrace();
	    else e.printStackTrace();
	    System.exit(1);
	}
    }


    private static String
    javaTrace(Throwable t) {

	StringWriter  writer;

	writer = new StringWriter();
	t.printStackTrace(new PrintWriter(writer));
	return(writer.toString());
    }


    private static void
    loadYoix(Hashtable source) {

	Enumeration  enm;
	String       key;
	byte         jar[];
	int          level;
	int          size;
	int          n;

	if ((jar = (byte[])source.get("yoix.jar")) == null) {
	    if (firstjar != null)
		jar = (byte[])source.get(firstjar);
	}

	if (jar != null) {
	    source = unzip(jar, BLOCKSIZE, 10);
	    yoixclasses = source;
	    size = source.size();
	    level = 0;
	    n = 0;

	    for (enm = source.keys(); enm.hasMoreElements(); ) {
		if (n++ >= level) {
		    showStatus(".");
		    level += size/10;
		}
		key = (String)enm.nextElement();
		if (key.endsWith(".class")) {
		    key = key.substring(0, key.lastIndexOf(".class"));
		    key = key.replace('/', '.');
		    classloader.loadClass(key, false);
		}
	    }
	} else {
	    System.err.println("can't find the yoix jar file");
	    System.exit(1);
	}
    }


    private static void
    setup() {

	splashScreen(YOIXINSTALLER, COPYRIGHT);
	System.out.println(YOIXINSTALLER + " - " + COPYRIGHT);

	try {
	    splashscreen.setVisible(true);
	    showStatus("Unpacking");
	    unhexed = unpack();
	    unzipped = unzip(unhexed, BLOCKSIZE, 1);
	    showStatus("done" + "\n");
	    showStatus("Loading Yoix");
	    loadYoix(unzipped);
	    showStatus("done" + "\n");
	    main = classloader.loadClass(YOIXMAIN, false).getMethod("main", ARGS);
	}
	catch(Exception e) {
	    System.err.println("Exception: e=" + javaTrace(e));
	    System.exit(1);
	}
    }


    private static byte[]
    unpack() {

	String  source;
	byte    bytes[];
	char    ch;
	int     index;
	int     length;
	int     base;
	int     level;
	int     digit;
	int     m;
	int     n;

	bytes = new byte[bytecount];
	base = 0;
	level = 0;

	for (index = 0; index < hexedstrings.length; index++) {
	    if (base >= level) {
		showStatus(".");
		level += bytecount/10;
	    }
	    source = hexedstrings[index];
	    hexedstrings[index] = null;
	    length = source.length();
	    for (n = 0, m = base; n < length; n++) {
		if ((ch = source.charAt(n)) > '9')
		    digit = (ch - 'A') + 10;
		else digit = ch - '0';
		if (n%2 == 0)
		    bytes[m] = (byte)(digit << 4);
		else bytes[m++] |= (byte)digit;
	    }
	    base += n/2;
	}

	hexedstrings = null;
	return(bytes);
    }


    private static Hashtable
    unzip(byte bytes[], int blocksize, int level) {

	ZipInputStream  input;
	Hashtable       table;
	ZipEntry        ze;
	String          name;
	byte            entry[];
	byte            block[];
	byte            tmp[];
	int             size;
	int             count;
	int             total;
	int             entries;

	input = new ZipInputStream(new ByteArrayInputStream(bytes));
	table = new Hashtable();
	entries = 0;
	block = null;

	try {
	    while (true) {
		ze = input.getNextEntry();
		if ((size = (int)ze.getSize()) > 0) {
		    total = 0;
		    entry = new byte[size];
		    while ((count = input.read(entry, total, size - total)) > 0)
			total += count;
		} else {		// this is required!!
		    if (block == null)
			block = new byte[blocksize];
		    entry = new byte[0];
		    while ((count = input.read(block, 0, block.length)) != -1) {
			tmp = new byte[entry.length + count];
			System.arraycopy(entry, 0, tmp, 0, entry.length);
			System.arraycopy(block, 0, tmp, entry.length, count);
			entry = tmp;
		    }
		}
		if (entries++%level == 0)
		    showStatus(".");
		if ((name = ze.getName()) != null) {
		    if (firstjar == null && name.endsWith(".jar"))
			firstjar = name;
		    table.put(name, entry);
		}
	    }
	}
	catch(Exception e) {}

	try {
	    input.close();
	}
	catch(IOException e) {}

	return(table);
    }


    private static void
    validate() {

	String  jvmversion = "1.1";
	Dialog  dialog;
	int     n;

	//
	// This was added quickly and most of it came from YoixMisc.java,
	// so it's probably be more general than we really need. We may
	// revisit it. The hardcoded instructions also aren't a very good 
	// idea!!!
	//

	if (JAVARUNTIMEVERSION == null || JAVARUNTIMEVERSION.length() == 0) {
	    if (JAVAVMVERSION == null || JAVAVMVERSION.length() == 0) {
		if (JAVAVERSION != null && JAVAVERSION.length() > 0) {
		    if (JAVAVERSION.startsWith("1."))
			jvmversion = JAVAVERSION;
		}
	    } else jvmversion = JAVAVMVERSION;
	} else jvmversion = JAVARUNTIMEVERSION;

	if (jvmversion.compareTo(MINVERSION) < 0) {
	    try {
		showError(BADVERSION);
	    }
	    catch(Exception e) {
		for (n = 1; n < BADVERSION.length; n++)
		    System.err.print(BADVERSION[n]);
	    }
	    finally {
		System.exit(1);
	    }
	}
    }


    private static void
    showError(String details[]) {

	LayoutManager  manager;
	Dimension      screensize;
	Rectangle      rect;
	TextArea       textarea;
	Toolkit        toolkit;
	Dialog         dialog;
	String         text = "";
	Button         button;
	Panel          buttonpanel;
	Label          label;
	int            resolution;
	int            rows;
	int            columns;

	//
	// We use AWT because we currently only get here when the version
	// of Java is too old, which means Swing might not be available.
	// 

	if (details.length > 0) {
	    toolkit = Toolkit.getDefaultToolkit();
	    screensize = toolkit.getScreenSize();
	    resolution = toolkit.getScreenResolution();

	    dialog = new Dialog((Frame)new Frame(), true);
	    dialog.setLayout(new BorderLayout(resolution/4, resolution/16));

	    label = new Label(details[0], Label.CENTER);
	    label.setFont(new Font("Helvetica", Font.BOLD, 14));
	    label.setForeground(Color.red);

	    for (rows = 1, columns = 70; rows < details.length; rows++) {
		columns = Math.max(columns, details[rows].length() + 1);
		text += details[rows];
	    }

	    textarea = new TextArea(text, rows, columns, TextArea.SCROLLBARS_NONE);
	    textarea.setFont(new Font("Courier", Font.BOLD, 12));

	    buttonpanel = new Panel();
	    button = new Button("Dismiss");
	    button.addActionListener(classloader);
	    buttonpanel.add(button);
	    actionperformed.put(button, dialog);

	    dialog.add(label, BorderLayout.NORTH);
	    dialog.add(textarea, BorderLayout.CENTER);
	    dialog.add(buttonpanel, BorderLayout.SOUTH);
	    dialog.add(new Canvas(), BorderLayout.EAST);
	    dialog.add(new Canvas(), BorderLayout.WEST);
	    dialog.setBackground(LIGHTBACKGROUND);

	    dialog.pack();
	    rect = dialog.getBounds();
	    rect.width += resolution/2;
	    rect.height += resolution/4;
	    rect.width = Math.max(rect.width, (int)(6.5*resolution));
	    rect.height = Math.max(rect.height, (int)(1.5*resolution));
	    rect.x = (screensize.width - rect.width)/2;
	    rect.y = (screensize.height - rect.height)/2;
	    dialog.setBounds(rect);
	    toolkit.beep();
	    dialog.setVisible(true);
	}
    }


    private static void
    showStatus(String str) {

	if (str != null) {
	    System.err.print(str);
	    if (splashstatus != null)
		splashstatus.append(str);
	}
    }


    private static void
    splashScreen(String title, String footer) {

	LayoutManager  manager;
	Dimension      screensize;
	Rectangle      rect;
	Toolkit        toolkit;
	String         text = "";
	int            resolution;
	int            rows;
	int            columns;

	//
	// Older versions used a Window, but early versions of Java 1.5.0
	// didn't put the installer screen on top of a Window (or JWindow).
	// Making splashscreen a JDialog (an AWT Dialog should also work)
	// fixed that problem.
	//

	toolkit = Toolkit.getDefaultToolkit();
	screensize = toolkit.getScreenSize();
	resolution = toolkit.getScreenResolution();

	splashscreen = new JDialog();
	splashscreen.setTitle(title + " - Status");
	splashscreen.getContentPane().setLayout(new BorderLayout());
	splashscreen.setBackground(DARKBACKGROUND);

	splashstatus = new JTextArea("\n", 12, 50);
	splashstatus.setOpaque(true);
	splashstatus.setEditable(false);
	splashstatus.setFont(new Font("Lucida", Font.BOLD, 12));
	splashstatus.setBackground(LIGHTBACKGROUND);
	splashstatus.setForeground(Color.black);
	splashstatus.append("Closing this screen at any time will stop the installer\n");

	splashfooter = new JLabel(footer, JLabel.CENTER);
	splashfooter.setFont(new Font("Lucida", Font.PLAIN, 12));
	splashfooter.setOpaque(true);
	splashfooter.setBackground(DARKBACKGROUND);
	splashfooter.setForeground(Color.white);

	splashscreen.getContentPane().add(splashstatus, BorderLayout.CENTER);
	splashscreen.getContentPane().add(splashfooter, BorderLayout.SOUTH);
	splashscreen.setForeground(Color.white);
	splashscreen.setBackground(DARKBACKGROUND);

	splashscreen.pack();
	rect = splashscreen.getBounds();
	rect.width += resolution/2;
	rect.height += resolution/4;
        rect.width = Math.min(Math.max(rect.width, (int)(7.0*resolution)), screensize.width/2);
        rect.height = Math.min(Math.max(rect.height, (int)(3.5*resolution)), screensize.height/3);
	rect.x = (screensize.width - rect.width)/2;
	rect.y = (screensize.height - rect.height)/2 - resolution/2;
	splashscreen.setBounds(rect);
	splashscreen.addWindowListener(classloader);
    }
}

