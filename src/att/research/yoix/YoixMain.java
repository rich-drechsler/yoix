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
import java.io.*;
import java.util.*;
import javax.swing.*;

public
class YoixMain

    implements YoixConstants

{

    //
    // A class that processes command line options, executes a single
    // file, and does a good job figuring out what should happen when
    // it finishes the input file (active threads and visible windows
    // that may still be doing real work make the exit decision harder
    // than you might expect). We now enforce the restriction that the
    // main() method in this class is only be called once, but we do it
    // quitely, at least right now.
    //
    // The interpreter's startup behavior can be controlled by property
    // files and command line options. The name of the default property
    // file is yoix.properties, and the Yoix interpreter always looks
    // for them in the three directories that Java associates with the
    // "yoix.home", "user.home", and "user.dir" system properties. 
    //
    // Command line options are always processed after default property
    // files are loaded. The officially supported options are documented
    // by the text strings in the usage array, which can be printed on
    // standard output using --help or --info. The following options may
    // still work, but they're undocumented and probably will be removed
    // in the near future (so don't use them):
    //
    //  --fixfontmetrics=boolean
    //   -x or +x
    //      Initializes VM.fixfontmetrics, which can be used to ask
    //      the interpreter to make small adjustments to the ascent
    //      and maxascent metric values associated with Java fonts.
    //      The value is initialized in setup() and is currently only
    //      set to true on some platforms.
    //
    //      This option will probably disappear or be disabled in the
    //      very near future, so don't use it. The behavior that its
    //      trying to correct probably will be fixed when we rewrite
    //      YoixAWTFontMetrics to use Java's LineMetrics class.
    //
    //   -s string
    //      This is a strange option that we threw in quickly for
    //      an application that required https support and may not
    //      be supported in future releases.
    //
    //      We never used the host mapping capability that it was
    //      designed for (i.e., string looks like "host1=host2"),
    //      but if you follow the code (into YoixMiscSSL.java) you
    //      may notice that any argument triggers some setup code,
    //      and that did turn out to be occasionally useful. So we
    //      sometimes use an option like -s0 to trigger the setup
    //      code even though the option looks like it's designed
    //      to be used like -s"host1=host2".
    //
    // Older releases started the interpreter using a command line that
    // looked something like,
    //
    //	CLASSPATH=yoix.jar java att.research.yoix.YoixMain [options] [script [args...]]
    //
    // which is simple and still works, but the -jar option that was
    // added to Java 1.2 can also be used, so
    //
    //  java -jar yoix.jar [options] [script [args...]]
    //
    // is even easier.
    //
    // Our automatically generated scripts still start the interpreter
    // the old way, however they prepend yoix.jar to whatever's stored
    // in the CLASSPATH environment variable. That means you can point
    // CLASSPATH at user modules and our startup scripts won't hide them
    // from Java. Incidentally, user modules can be disabled completely
    // using the -u option or selectively using the checkYoixModule()
    // function that has been added to the Yoix SecurityManager.
    //
    // We now handle shutdown using a single thread that also calls any
    // Yoix functions that were registered by the new addShutdownHook()
    // builtin. That means Yoix scripts get a chance to cleanup before
    // the Java Virtual Machine (i.e.. JVM) really quits. If you're a
    // Java programmer and decided to customize our source code or add
    // your own modules be warned that you should call VM.exit() rather
    // than System.exit() because the JVM makes System.exit() wait while
    // it's running "shutdown hooks". In other words a System.exit() that
    // gets called from our shutdown thread will cause the JVM to hang
    // forever!!
    //

    private static String  argv[];
    private static int     argc = 0;
    private static int     argn = 0;

    //
    // This is used to accumulate command line include files.
    //

    private static Vector  includes = new Vector();

    //
    // We collect information about the system splash screen that comes
    // from command line options in the following variables.
    // 

    private static String  splashtitle = null;
    private static String  splashforeground = null;
    private static String  splashbackground = null;

    //
    // We sometimes will package startup scripts in the jar file and if
    // we were started by javaws (as a untrusted application) we probably
    // can't use Java's "jar:file:yoix.jar!/path" notation to read the
    // script. However, if you use the --resource option the interpreter
    // will use getResource() to get a path that can be read.
    //

    private static boolean  getresource = false;

    //
    // Our version of getopt() now handles short and long command line
    // options. One character options are described by a single string,
    // while long options are described by an array of strings. In both
    // cases a single colon means the option requires an argument and
    // two colons means the option takes an optional argument. The long
    // option string decriptions can also include an equal sign that's
    // followed by a single character which is the value that getopt()
    // returns when it recognizes the option. The default return value
    // when getopt() recognizes a long option is '-'.
    // 

    private static String  option_letters = "b:d:e:fg::l:m:s:uxz:D:I:O::S:T:V?";

    private static String  option_words[] = {
	"acceptcertificates",
	"addtags:",
	"applet::",
	"bind:",
	"buttonmodel=b:",
	"cookie:",
	"cookiepolicy:",
	"cookies:",
	"create:",
	"debug=d:",
	"diagonal=D:",
	"errorlimit=l:",
	"eventflags:",
	"exitmodel=e:",
	"fixfontmetrics:",
	"fixfonts:",
	"fontmagnification=m:",
	"help=?",
	"include=I:",
	"info",
	"lookandfeel:",
	"parserencoding:",
	"propertyfile:",
	"securityoption=S:",
	"resource",
	"splash::",
	"splashbackground:",
	"splashforeground:",
	"streamencoding:",
	"threadsafe:",
	"tmpdir=T:",
	"trace::",
	"usermodules=u",
	"version",
	"versioninfo",
	"zipped=z:",
    };

    //
    // Usage and option help messages.
    //

    private static String  usage[][] = {
	new String[] {
	    "Try yoix --help for more information."
	},

	//
	// Supposed to be the short description of usage and options.
	//

	new String[] {
	    "Usage is",
	    "",
	    "  yoix [options] [script [args...]]",
	    "",
	    "where script can be a file, URL, or - and options can include:",
	    "  --acceptcertificates",
	    "      Initializes VM.acceptcertificates to true, which tells the",
	    "      interpreter it should accept all SSL certificates.",
	    "",
	    "  --addtags=boolean",
	    "      Initializes VM.addtags, which is used to control debugging",
	    "      information that's included in parse trees.",
	    "",
	    "  --applet[=flags]",
	    "      Tells the interpreter to run scripts as applets rather than",
	    "      applications, which implies (but does not guarantee) stricter",
	    "      security checking. Also sets VM.applet, which is a read-only",
	    "      integer that is non-zero when applet mode is enabled.",
	    "",
	    "  --bind=boolean",
	    "      Initializes VM.bind, which enables or disables name binding",
	    "      in parse trees.",
	    "",
	    "  --buttonmodel=integer",
	    "   -b integer",
	    "      Initializes VM.buttonmodel, which controls low level mouse",
	    "      button behavior.",
	    "",
	    "  --cookie=value",
	    "  --cookies=values",
	    "      Installs one or more cookies into the default cookie manager.",
	    "      Cookies must include a domain argument, otherwise they will",
	    "      be silently ignored. Only works under Java 1.6.",
	    "",
	    "  --cookiepolicy=name",
	    "      Sets the default cookie manager policy to name, which should",
	    "      be all, server, or none. The policy determines how cookies",
	    "      received from servers are managed. Only works under Java 1.6.",
	    "",
	    "  --create=boolean",
	    "      Initializes VM.create, which controls what happens when a",
	    "      a value is assigned to an undeclared variable.",
	    "",
	    "  --debug=integer",
	    "   -d integer",
	    "      Initializes VM.debug, which are flags that can be used to",
	    "      control low level debugging behavior of the interpreter.",
	    "",
	    "  --diagonal=number",
	    "   -D number",
	    "      Initializes VM.screen.diagonal, which is the size of your",
	    "      screen as measured in inches along the diagonal.",
	    "",
	    "  --errorlimit=integer",
	    "   -l integer",
	    "      Tells the interpreter to quit after num errors.",
	    "",
	    "  --exitmodel=integer",
	    "   -e integer",
	    "      Initializes VM.exitmodel, which controls what happens when",
	    "      the main thread quits.",
	    "",
	    "  --fixfonts=boolean",
	    "   -f or +f",
	    "      Initializes VM.fixfonts, which controls low level platform",
	    "      dependent tuning.",
	    "",
	    "  --fontmagnification=number",
	    "   -m number",
	    "      Initializes VM.fontmagnification, which is used to scale",
	    "      font sizes.",
	    "",
	    "   -g",
	    "   -g:trace",
	    "   -g:none",
	    "      With no argument this option asks the interpreter to run in",
	    "      a mode that guarantees line number and source file information",
	    "      will be included in error messages. Use --info to learn more.",
	    "",
	    "  --help",
	    "   -?",
	    "      Prints a short description of the command usage and options",
	    "      on standard output and then exits.",
	    "",
	    "  --include=path",
	    "   -I path",
	    "      Includes the named file after all command line options are",
	    "      processed, but before the input file named on the command",
	    "      line is executed.",
	    "",
	    "  --info",
	    "      Prints a long description of the command usage and options",
	    "      on standard output and then exits.",
	    "",
	    "  --lookandfeel=name",
	    "      Sets the default look-and-feel used by Swing components to",
	    "      name.",
	    "",
	    "  -O[string]",
	    "      Starts the Yoix interpreter in optimized mode, which can",
	    "      improve performance by about 10%.",
	    "",
	    "  --parserencoding=encoding",
	    "      Sets the default encoding for streams that are parsed.",
	    "",
	    "  --propertyfile=path",
	    "      Tells the interpreter to immediately load properties from",
	    "      path. The long description available using --info lists the",
	    "      entries currently recognized in property files.",
	    "",
	    "  --securityoption=string",
	    "   -S string",
	    "      Updates the Yoix interpreter's security options.",
	    "",
	    "  --splash[=text]",
	    "      Show the system splash screen before processing the script",
	    "      named on the command line.",
	    "",
	    "  --splashbackground=color",
	    "      Use color as the splash screen's background, where color is",
	    "      a numeric representation of the rgb components.",
	    "",
	    "  --splashforeground=color",
	    "      Use color as the splash screen's foreground, where color is",
	    "      a numeric representation of the rgb components.",
	    "",
	    "  --streamencoding=encoding",
	    "      Sets the default encoding for I/O streams.",
	    "",
	    "  --threadsafe=boolean",
	    "      Initializes the thread-safe behavior of Swing components.",
	    "",
	    "  --tmpdir=path",
	    "   -T path",
	    "      Initializes VM.tmpdir, which is the default directory the",
	    "      interpreter uses to creates temporary files.",
	    "",
	    "  --trace[=model]",
	    "      Initializes VM.trace, which enables function call tracing",
	    "      when an error occurs if model is omitted or 1 and disables",
	    "      it when model is 0.",
	    "",
	    "  --usermodules",
	    "   -u",
	    "      Disables user modules.",
	    "",
	    "  --version",
	    "  --versioninfo",
	    "   -V or +V",
	    "      Prints version information on standard output and exits.",
	    "",
	    "  --zipped=integer",
	    "   -z integer",
	    "      Initializes VM.zipped, which are read-only flags that control",
	    "      whether gzipped script files or the zipped archives generated",
	    "      by the compiler can be executed.",
	    "",
	    "A script named on the command line script that ends in the suffix",
	    "\".yxs\", is automatically run as an applet, which means it is run",
	    "with limited capabilites that pretty much duplicate the restrictions",
	    "imposed on a Java applet by a browser. In other words, running a script",
	    "that ends in .yxs is essentially like adding --applet as last command",
	    "line option.",
	    "",
	    "Try yoix --info for more information."
	},

	//
	// The really long description of usage and options.
	//

	new String[] {
	    "Usage is",
	    "",
	    "  yoix [options] [script [args...]]",
	    "",
	    "where script can be a file, URL, or - and options can include:",
	    "  --acceptcertificates",
	    "      Initializes VM.acceptcertificates to true, which tells the",
	    "      interpreter it should accept all SSL certificates, but none",
	    "      of them will be permanently saved. VM.acceptcertificates is",
	    "      a readonly field that starts FALSE and can only be changed",
	    "      using this command line option.",
	    "",
	    "      This option is only for an application (e.g., a web crawler)",
	    "      that doesn't want to be prompted by every certificate. It's",
	    "      definitely not for most applications.",
	    "",
	    "  --addtags=boolean",
	    "      Initializes VM.addtags, which is used to control debugging",
	    "      information that's included in parse trees. When true, line",
	    "      number and source file information is included will appear",
	    "      in error messages. The overhead can approach 10%, so setting",
	    "      addtags to false is a good idea for production applications.",
	    "",
	    "      VM.addtags is set to true by default and when the -g option",
	    "      is used. The -O option automatically sets it to false.",
	    "",
	    "  --applet[=flags]",
	    "      Tells the interpreter to run scripts as applets rather than",
	    "      applications, which implies (but does not guarantee) stricter",
	    "      security checking. Also sets VM.applet, which is a read-only",
	    "      integer that is non-zero when applet mode is enabled. Once the",
	    "      interpreter is booted this option installs a security manager",
	    "      (see file YoixSecurityManager.java) that tries to enforce the",
	    "      security policy described by the Java policy files that are",
	    "      installed on your system and optionally modified by -S command",
	    "      line options that precede the --applet option.",
	    "",
	    "      Applet mode can't be disabled once it has been enabled via",
	    "      this command line option and after that any request to modify",
	    "      the security settings using the -S option will be treated as",
	    "      a fatal error. Any -S options that precede this option on the",
	    "      command line are currently allowed and modify the security",
	    "      checking the way you might expect.",
	    "",
	    "      Setting the first bit in the optional flags argument to one",
	    "      requests that top-level windows get a visual warning that",
	    "      identifies them as potentially untrusted. This is currently",
	    "      the only documented flag - others flags may or may not be",
	    "      implemented, but their meaning can changed without notice.",
	    "",
	    "  --bind=boolean",
	    "      Initializes VM.bind, which enables or disables name binding",
	    "      in parse trees and can have a big impact on the performance",
	    "      of functions.",
	    "",
	    "      NOTE - binding currently only applies to functions and is",
	    "      done the first time the function is called if VM.bind was",
	    "      non-zero when the function was defined.",
	    "",
	    "  --buttonmodel=integer",
	    "   -b integer",
	    "      Initializes VM.buttonmodel to a model (0, 1, 2, or 3) that's",
	    "      used to adjust the modifier flags stored in events that are",
	    "      generated when the user interacts with the mouse. This option",
	    "      was sometimes useful with older versions of Java, but it's",
	    "      rarely needed today.",
	    "",
	    "      Model 0, which is the default, makes no changes to the event",
	    "      modifier flags. Model 1 only changes the event modifiers if",
	    "      you're on a Mac and in that case it maps button 1 with CTRL",
	    "      pressed to button 3, which might be useful on pre-10.5 Macs.",
	    "      Model 2 maps button 2 to button 3, which essentially converts",
	    "      a three button mouse to a two button mouse. The behavior of",
	    "      model 3 is undocumented and subject to change.",
	    "",
	    "  --cookie=value",
	    "  --cookies=values",
	    "      Installs one or more cookies into the default cookie manager.",
	    "      Cookies must include a domain argument, otherwise they will",
	    "      be silently ignored. Only works under Java 1.6.",
	    "",
	    "  --cookiepolicy=name",
	    "      Sets the default cookie manager policy to name, which should",
	    "      be all, server, or none. The policy determines how cookies",
	    "      received from servers are managed. Only works under Java 1.6.",
	    "",
	    "  --create=boolean",
	    "      Initializes VM.create, which controls what happens when a",
	    "      script assigns a value to an undeclared variable. Setting",
	    "      VM.create to true, which is the default, means undeclared",
	    "      variables are created when they're assigned a value. When",
	    "      VM.create is false the interpreter will complain when you",
	    "      assign a value to an undeclared variable.",
	    "",
	    "  --debug=integer",
	    "   -d integer",
	    "      Initializes VM.debug, which are flags that can be used to",
	    "      control low level debugging behavior of the interpreter.",
	    "      The flag we use most often is 1 (i.e., -d1), which tells",
	    "      the interpreter to dump the value of every expression that",
	    "      also happens to be a statement. When we're working on the",
	    "      Java code we often use -d16, which tells the interpreter",
	    "      provide a Java trace whenever there's a Yoix error. Using",
	    "      -d2 tells the interpreter to dump parse trees after they're",
	    "      created (setting VM.addtags to false reduces some of the",
	    "      clutter).",
	    "",
	    "      NOTE - look in YoixConstants.java for official definitions",
	    "      of all the debug flags. A leading 0 or 0x can be used when",
	    "      you want to supply integer in octal or hex notation.",
	    "",
	    "  --diagonal=number",
	    "   -D number",
	    "      Initializes VM.screen.diagonal, which is the size of your",
	    "      screen as measured in inches along the diagonal. A number",
	    "      less than or equal to zero disables the calculations that",
	    "      the interpreter uses to estimate the resolution of your",
	    "      screen. Actually that's the default behavior, so omitting",
	    "      the option should accomplish the same thing.",
	    "",
	    "  --errorlimit=integer",
	    "   -l integer",
	    "      Tells the interpreter to quit after num errors, which is",
	    "      a number that can also be set by Yoix scripts using the",
	    "      yoix.system.setErrorLimit() builtin. Using +l instead of",
	    "      -l has the side effect of locking the limit, which means",
	    "      all subsequent requests to change it will be ignored. A",
	    "      number less than or equal to 0 (the default) means there's",
	    "      no limit.",
	    "",
	    "  --exitmodel=integer",
	    "   -e integer",
	    "      Initializes VM.exitmodel, which currently should only be",
	    "      0, 1, or 2. The exitmodel controls what happens when the",
	    "      main thread quits:",
	    "",
	    "          0 Nothing special happens when the main thread",
	    "            quits, so the Yoix program must call exit()",
	    "            to quit.",
	    "",
	    "          1 The interpreter does its best to guess if it",
	    "            should exit() when the main thread quits. The",
	    "            guess currently looks for active threads or",
	    "            visible windows before deciding what to do.",
	    "",
	    "          2 The interpreter always exits when the main",
	    "            thread quits.",
	    "",
	    "      The default value is currently 1.",
	    "",
	    "  --fixfonts=boolean",
	    "   -f or +f",
	    "      Initializes VM.fixfonts, which controls low level platform",
	    "      dependent tuning that tries to calculate a scaling factor",
	    "      that can be applied to the font sizes that are handed to",
	    "      Java. VM.fixfonts is a readonly field that can only be set",
	    "      by command a line option or property file entry.",
	    "",
	    "  --fontmagnification=number",
	    "   -m number",
	    "      Initializes VM.fontmagnification, which is used to scale",
	    "      the sizes that the interpreter hands to Java whenever it",
	    "      asks for a new font. Font magnifications that are too big",
	    "      or too small can result in unpredictable behavior, so try",
	    "      to keep number between 0.5 and 2.0.",
	    "",
	    "   -g",
	    "   -g:trace",
	    "   -g:none",
	    "      With no argument this option asks the interpreter to run in",
	    "      a mode that guarantees line number and source file information",
	    "      will be included in error messages, which overrides VM.addtags.",
	    "      The trace argument asks the interpreter to include a function",
	    "      call trace in all error messages, while none means don't save",
	    "      line number and source file information. The colon separating",
	    "      the -g from trace and none can be replaced by an equal sign",
	    "",
	    "      The -g and -g:trace options are relatively expensive so they",
	    "      probably should only be used when you really need to debug",
	    "      an application, but the fact that they override VM.addtags",
	    "      makes them particularly useful. Right now -g:none and -O are",
	    "      equivalent, but that may change in a future release.",
	    "",
	    "  --help",
	    "   -?",
	    "      Prints a short description of the command usage and options",
	    "      on standard output and then exits. A longer description is",
	    "      available using --info.",
	    "",
	    "  --include=path",
	    "   -I path",
	    "      Includes the named file after all command line options are",
	    "      processed, but before the input file named on the command",
	    "      line is executed. All files included this way run in the",
	    "      environment created for the primary input file.",
	    "",
	    "      This option can be particularly useful when no input file",
	    "      is named on the command line (i.e., the Yoix interpreter",
	    "      reads from standard input) and you want a way to establish",
	    "      a consistent environment, often for debugging purposes.",
	    "",
	    "  --info",
	    "      Prints a long description of the command usage and options",
	    "      on standard output and then exits. A shorter description is",
	    "      available using --help or -?.",
	    "",
	    "  --lookandfeel=name",
	    "      Sets the default look-and-feel used by Swing components to",
	    "      name. The name is case-independent but otherwise should be",
	    "      one of the strings listed in the platform-dependent",
	    "",
	    "          VM.screen.uimanager.lookandfeelnames",
	    "",
	    "      array. An unrecognized name is silently ignored.",
	    "",
	    "  -O[string]",
	    "      Starts the Yoix interpreter in optimized mode, which can",
	    "      improve performance by about 10%, but it currently means",
	    "      you get little or no debugging help from error messages.",
	    "      Currently changes VM.addtags and VM.bind, which can also",
	    "      be done in Yoix scripts. You should use the -g option if",
	    "      you need more help while you're debugging a Yoix script.",
	    "",
	    "      NOTE - the optional argument is currently undocumented and",
	    "      probably is ignored.",
	    "",
	    "  --parserencoding=encoding",
	    "      Sets the default encoding for streams that are parsed as",
	    "      Yoix scripts by the Yoix interpreter. The value is saved",
	    "      in VM.encoding.parser, which can be changed at run-time,",
	    "      and in VM.encoding.VM, which is a read-only string that",
	    "      can't be changed once the interpreter finishes processing",
	    "      the command line options. Changing VM.encoding.parser is",
	    "      allowed and affects commands, like \"include\", \"execute\",",
	    "      and \"eval\" that run after the change.",
	    "",
	    "      NOTE - use the builtin yoix.io.getAvailableCharsets() to",
	    "      list the character encodings that your system recognizes.",
	    "",
	    "  --propertyfile=path",
	    "      Tells the interpreter to immediately load properties from",
	    "      path. The only lines in a property file that are currently",
	    "      recognized are:",
	    "",
	    "          yoix.buttonmodel = integer",
	    "          yoix.diagonal = number",
	    "          yoix.fixfonts = boolean",
	    "          yoix.fontmagnification = number",
	    "          yoix.lookandfeel = name",
	    "          yoix.parserencoding = encoding",
	    "          yoix.streamencoding = encoding",
	    "          yoix.tmpdir = path",
	    "",
	    "      The interpreter looks for files named yoix.properties in",
	    "      the directories that Java associates with the \"yoix.home\",",
	    "      \"user.home\", and \"user.dir\" system properties before the",
	    "      command line options are processed. This option lets you",
	    "      supply additional files that override the default property",
	    "      files or command line options that preceded this option.",
	    "",
	    "  --securityoption=string",
	    "   -S string",
	    "      Updates the Yoix interpreter's security options based on",
	    "      the argument, which should be a string of the form",
	    "",
	    "          \"access:category[:pattern]\"",
	    "",
	    "      and appends the argument to VM.securityoptions, which is a",
	    "      readonly field that records the security options specified",
	    "      on the command line.",
	    "",
	    "      The access token in string picks the access mode and should",
	    "      be \"allow\", \"prompt\", or \"deny\". In some situations \"prompt\"",
	    "      causes deadlock, so even though it can be interesting \"prompt\"",
	    "      often isn't appropriate.",
	    "",
	    "      The category can be the name of a security checking function",
	    "      (e.g., \"checkWrite\") or one of the abbreviations:",
	    "",
	    "          \"accept\"",
	    "          \"addprovider\"",
	    "          \"clipboard\"",
	    "          \"connect\"",
	    "          \"delete\"",
	    "          \"eval\"",
	    "          \"exec\"",
	    "          \"execute\"",
	    "          \"exit\"",
	    "          \"file\"",
	    "          \"include\"",
	    "          \"listen\"",
	    "          \"module\"",
	    "          \"multicast\"",
	    "          \"open\"",
	    "          \"properties\"",
	    "          \"read\"",
	    "          \"readdisplay\"",
	    "          \"readenvironment\"",
	    "          \"readproperty\"",
	    "          \"removeprovider\"",
	    "          \"robot\"",
	    "          \"socket\"",
	    "          \"write\"",
	    "          \"writeproperty\"",
	    "",
	    "          \"cwd\"",
	    "          \"tempfile\"",
	    "          \"tmpfile\"",
	    "          \"update\"",
	    "",
	    "      Three of these currently represent collections rather than a",
	    "      single security checking function. \"file\" stands for \"delete\",",
	    "      \"read\", and \"write\", \"socket\" stands for \"accept\", \"connect\",",
	    "      \"listen\", and \"multicast\", while \"properties\" stands for the",
	    "      \"checkPropertiesAccess\" security function and \"readproperty\".",
	    "      Four others, namely \"cwd\", \"tempfile\", \"tmpfile\", and \"update\"",
	    "      are convenient abbreviations that target specific directories",
	    "      or files on your system. Use \"connect\" to enable or disable",
	    "      access to a web server and in this case pattern, which is",
	    "      discussed below, should be host:port (e.g., www.yoix.org:80)",
	    "      or address:port (e.g., 192.168.10.1:80).",
	    "",          
	    "      The optional pattern is used for shell-style matching of",
	    "      arguments during a security check, but in this case the",
	    "      matching is done using a variation in which the backslash",
	    "      character, which is also the Windows file separator, has",
	    "      no special meaning. For example",
	    "",
	    "          -S'prompt:file:/etc/*'",
	    "",
	    "      prompts with a dialog whenever the Yoix program tries to",
	    "      read, write, delete, or execute files under /etc. In this",
	    "      case access to all other files is denied unless there are",
	    "      other command line options that assign permissions to other",
	    "      files. Omitting optional pattern or supplying it as * are",
	    "      equivalent, so",
	    "",
	    "          -S'prompt:file:/etc/*' -Sallow:file",
	    "",
	    "      would prompt for files under /etc but allow access to all",
	    "      other files. Using temp files is common, so there's an easy",
	    "      way to extend permissions to temp files. The option",
	    "",
	    "          -Sallow:tempfile",
	    "",
	    "      lets Yoix programs read, write, or delete files that are",
	    "      in the official temp directory. The \"update\" category is",
	    "      another special one that enables reading and writing of the",
	    "      two files that a YWAIT application needs to update the jar",
	    "      file that's installed on your system. The \"cwd\" category",
	    "      can be used to apply permissions to files and directories",
	    "      in the user's current working directory.",
	    "",
	    "      The category can also name a SecurityManager function, so",
	    "",
	    "          -Sprompt:checkWrite",
	    "",
	    "      prompts with a dialog whenever Java tries to to find out",
	    "      if it can write to a local file.",
	    "",
	    "  --splash[=text]",
	    "      Show the system splash screen before processing the script",
	    "      named on the command line. The optional text argument should",
	    "      be a short string that's displayed in the splash screen. The",
	    "      splash screen shown using this option can be dismissed by a",
	    "      Yoix script using the hideSystemSplashScreen() builtin.",
	    "",
	    "  --splashbackground=color",
	    "      Use color as the splash screen's background, where color can",
	    "      be a name (e.g., red) or the numeric representation of the",
	    "      rgb components. Hexadecimal representations of colors must",
	    "      start with '0x' or '#'.",
	    "",
	    "  --splashforeground=color",
	    "      Use color as the splash screen's foreground, where color can",
	    "      be a name (e.g., red) or the numeric representation of the",
	    "      rgb components. Hexadecimal representations of colors must",
	    "      start with '0x' or '#'.",
	    "",
	    "  --streamencoding=encoding",
	    "      Sets the default encoding for I/O streams (iso8859-1 by",
	    "      default) to enc, which ends up in VM.encoding.stream. The",
	    "      value is ignored if the JVM doesn't doesn't support the",
	    "      encoding. Use yoix.io.getAvailableCharsets to get a list",
	    "      of recognized character set encodings. VM.encoding.stream",
	    "      can be changed at run time to affect the default of streams",
	    "      opened after the change. The encoding assigned to stdin,",
	    "      stdout and stderr use this value.",
	    "",
	    "  --threadsafe=boolean",
	    "      Initializes the thread-safe behavior of Swing components.",
	    "",
	    "  --tmpdir=path",
	    "   -T path",
	    "      Initializes VM.tmpdir, which is the default directory the",
	    "      interpreter uses to creates temporary files (e.g., via the",
	    "      tempnam() builtin). Point at a temp directory that's not",
	    "      the system default, and the interpreter will try to create",
	    "      it if it doesn't already exist.",
	    "",
	    "  --trace[=model]",
	    "      Initializes VM.trace, which enables function call tracing",
	    "      when an error occurs if model is omitted or 1 and disables",
	    "      it when model is 0. Other model values may be supported but",
	    "      they are not currently documented.",
	    "",
	    "      VM.trace is set to 0 by default and when the -O option is",
	    "      used, while the -g option sets it to 1.",
	    "",
	    "  --usermodules",
	    "   -u",
	    "      Disables user modules, which means any request to load a",
	    "      class that looks like a user module will be rejected with",
	    "      a securitycheck error. Use the checkYoixModule() security",
	    "      checker in the Yoix SecurityManager when you want to be",
	    "      more selective.",
	    "",
	    "  --version",
	    "  --versioninfo",
	    "   -V or +V",
	    "      Prints version information on standard output and exits.",
	    "      Use +V or --versioninfo for a bit more information. The",
	    "      information currently provided by these options is also",
	    "      available in the VM dictionary.",
	    "",
	    "  --zipped=integer",
	    "   -z integer",
	    "      Initializes VM.zipped, which are read-only flags that control",
	    "      whether gzipped script files or the zipped archives generated",
	    "      by the compiler can be executed (via the command line, include",
	    "      statements, or the execute() builtin). Right now only two bits",
	    "      are used. Setting bit 0 means any gzipped Yoix script can be",
	    "      executed, while setting bit 1 means any zipped archive created",
	    "      by the compiler can be executed. VM.zipped can only be changed",
	    "      from its default (currently 1) using command line options.",
	    "",
	    "A script named on the command line script that ends in the suffix",
	    "\".yxs\", is automatically run as an applet, which means it is run",
	    "with limited capabilites that pretty much duplicate the restrictions",
	    "imposed on a Java applet by a browser. In other words, running a script",
	    "that ends in .yxs is essentially like adding --applet as last command",
	    "line option.",
	    "",
	    "Visit http://www.yoix.org/ for more information about Yoix.",
	},
    };

    //
    // Map property file keywords that should be recognized to integers
    // (e.g., option letters) that tune() accepts. A '-' means we call
    // the version of tune() that handles long options.
    //

    private static Hashtable  properties = new Hashtable();

    static {
	properties.put(N_BUTTONMODEL, new Integer('b'));
	properties.put(N_DIAGONAL, new Integer('D'));
	properties.put(N_FIXFONTS, new Integer('-'));
	properties.put(N_FONTMAGNIFICATION, new Integer('m'));
	properties.put(N_LOOKANDFEEL, new Integer('-'));
	properties.put(N_PARSERENCODING, new Integer('-'));
	properties.put(N_STREAMENCODING, new Integer('-'));
	properties.put(N_TMPDIR, new Integer('T'));
    }

    //
    // A boolean that's set to true when this thread is just about done
    // and another that that we use to make sure there's only one call
    // to main().
    //

    private static boolean  finished = false;
    private static boolean  validated = false;

    ///////////////////////////////////
    //
    // YoixMain Methods
    //
    ///////////////////////////////////

    static boolean
    isFinished() {

	return(finished);
    }


    public static void
    main(String args[]) {

	if (validate()) {
	    setup(args);
	    properties(PROPERTYFILE, PROPERTYDIRS);
	    options();
	    cleanup();
	    arguments();
	    done();
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static void
    arguments() {

	InputStream  stream = null;
	String       args[] = null;
	String       resource;

	try {
	    if (argn < argc) {
		args = new String[argc - argn];
		System.arraycopy(argv, argn, args, 0, args.length);
		if (args[0].equals("-")) {
		    args[0] = NAME_STDIN;
		    stream = System.in;
		} else {
		    if (getresource) {
			if ((resource = YoixMisc.getResource(args[0])) != null)
			    stream = YoixMisc.getInputStream(resource);
			else stream = YoixMisc.getInputStream(args[0]);
		    } else stream = YoixMisc.getInputStream(args[0]);
		}
	    } else {
		args = new String[1];
		args[0] = NAME_STDIN;
		stream = System.in;
	    }

	    if (stream != null) {
		VM.checkSecuritySuffix(args[0]);
		YoixSecurityManager.setYoixSource(args[0]);
		if (splashtitle != null)
		    YoixSplashScreen.showSystemSplashScreen(splashtitle, splashbackground, splashforeground, true);
		Yoix.executeStream(stream, args[0], YoixMake.yoixArray(args), includes, false);
	    } else VM.abort(UNREADABLEFILE, args[0]);
	}
	finally {
	    try {
		if (stream != System.in)
		    stream.close();
	    }
	    catch(IOException e) {
		VM.caughtException(e);		// unnecessary overkill!!
	    }
	}
    }


    private static void
    cleanup() {

	//
	// We only allow one main() call, so we can toss things that we
	// won't need again.
	//

	option_letters = null;
	option_words = null;
	usage = null;
    }


    private static void
    done() {

	//
	// Shouldn't always quit when we get here, because the user's
	// program may be running a thread (e.g., for a GUI application)
	// that's doing real work. We check N_EXITMODEL, which is set by
	// the -e option, before deciding what to do.
	//

	finished = true;

	switch (VM.getExitModel()) {
	    case 0:
		break;

	    case 1:
		if (VM.canExit())
		    VM.exit(VM.getErrorCount() == 0 ? 0 : -1);
		break;

	    case 2:
		VM.exit(VM.getErrorCount() == 0 ? 0 : -1);
		break;
	}
    }


    private static void
    options() {

	YoixOption  option = new YoixOption();
	int         value;
	int         ch;

	//
	// YoixOption.java has changed slightly, so we now have to cast
	// option.optarg to a String to make the compiler happy. Most of
	// the cases call tune(), which is the method that also handles
	// property files.
	//

	while ((ch = option.getopt(argv, option_letters, option_words)) != -1) {
	    switch (ch) {
		case 'b':
		    tune(ch, option.optarg);
		    break;

		case 'd':
		    tune(ch, option.optarg);
		    break;

		case 'e':
		    tune(ch, option.optarg);
		    break;

		case 'f':
		    tune(N_FIXFONTS, option.optchar == '+' ? "true" : "false");
		    break;

		case 'g':
		    VM.setDebugging((String)option.optarg);
		    break;

		case 'l':
		    VM.setErrorLimit((String)option.optarg, option.optchar == '+');
		    break;

		case 'm':
		    tune(ch, option.optarg);
		    break;

		case 's':
		    YoixMiscSSL.addHostPair((String)option.optarg);
		    break;

		case 'u':
		    VM.setUserModules(false);
		    break;

		case 'x':
		    tune(N_FIXFONTMETRICS, option.optchar == '+' ? "true" : "false");
		    break;

		case 'z':
		    VM.setZipped(YoixMake.javaInt((String)option.optarg, EXECUTE_ZIPPED_DEFAULT));
		    break;

		case 'D':
		    tune(ch, option.optarg);
		    break;

		case 'I':
		    includes.add((String)option.optarg);
		    break;

		case 'O':
		    VM.setOptimized((String)option.optarg);
		    break;

		case 'S':
		    tune(ch, option.optarg);
		    break;

		case 'T':
		    tune(ch, option.optarg);
		    break;

		case 'V':
		    version(option.optchar == '+');
		    break;

		case '-':
		    tune(option.optword, option.optarg);
		    break;

		case '?':
		    if (option.optstatus != null) {
			VM.warn(OPTIONERROR, option.optstatus);
			usage(0);
		    } else usage(1);
		    break;

		default:
		    VM.abort(INTERNALERROR, new String[] {"case for handling option " + option.optchar + (char)ch + " is missing"});
		    break;
	    }
	}
	argn += option.optind;
    }


    private static void
    properties(String filename, String directories[]) {

	FileInputStream  file;
	Enumeration      enm;
	Properties       prop;
	String           prefix;
	String           dir;
	String           key;
	String           sep;
	String           value;
	int              ch;
	int              n;

	if (filename != null) {
	    //
	    // Catching SecurityException prevents problems when we're running as
	    // an untrusted application under javaws.
	    //
	    try {
		if (directories == null) {
		    directories = new String[] {""};
		    sep = "";
		    prop = new Properties();
		} else {
		    sep = File.separator;
		    prop = new Properties(System.getProperties());
		}
		if (PROPERTYPREFIX != null && PROPERTYPREFIX.length() > 0) {
		    prefix = PROPERTYPREFIX;
		    if (prefix.endsWith(".") == false)
			prefix += ".";
		} else prefix = "";

		for (n = 0; n < directories.length; n++) {
		    try {
			if ((dir = directories[n]) != null) {
			    file = new FileInputStream(dir + sep + filename);
			    prop.load(file);
			    file.close();
			}
		    }
		    catch(IOException e) {}
		}

		for (enm = properties.keys(); enm.hasMoreElements(); ) {
		    key = (String)enm.nextElement();
		    if ((value = prop.getProperty(prefix + key)) != null) {
			ch = ((Integer)properties.get(key)).intValue();
			if (ch != '-') {
			    if (value.equalsIgnoreCase("true"))
				tune(ch, Boolean.TRUE);
			    else if (value.equalsIgnoreCase("false"))
				tune(ch, Boolean.FALSE);
			    else tune(ch, value);
			} else tune(key, value);
		    }
		}
	    }
	    catch(SecurityException se) {}
	}
    }


    private static void
    setup(String args[]) {

	//
	// Decided to restrict the hack to Windows, where it still seems
	// to be needed to help an important AWT application. This is a
	// hack that probably will disappear completely when we replace
	// our use of the FontMetric class with the LineMetric class.
	//

	argv = args;
	argc = args.length;
	argn = 0;

	if (ISWIN) {
	    if (YoixMisc.jvmCompareTo("1.2") >= 0)
		YoixModule.tune(N_FIXFONTMETRICS, Boolean.TRUE);
	}
	//
	// Old version called
	//
	//	YoixTrustPolicy.setupTrustManager();
	//
	// but we now think it can be postponed and only doen if we really
	// use https. Small chance there could be a problem, but the tests
	// that we ran all worked well.
	//
    }


    private static void
    tune(int id, Object value) {

	//
	// Handles the initialization that can come from property files
	// or command line options, but in this case we're dealing with
	// tuning that's associated with an option letter. Almost every
	// case is directed to YoixModule.tune(), which eventually (via
	// reflection) ends up updating the YoixModuleVM.$init table.
	//

	switch (id) {
	    case 'b':
		YoixModule.tune(N_BUTTONMODEL, value);
		break;

	    case 'd':
		if (value instanceof String)
		    value = YoixObject.newNumber((String)value);
		YoixModule.tune(N_DEBUG, value);
		break;

	    case 'e':
		YoixModule.tune(N_EXITMODEL, value);
		break;

	    case 'm':
		YoixModule.tune(N_FONTMAGNIFICATION, value);
		break;

	    case 'D':
		YoixModule.tune(N_DIAGONAL, value);
		break;

	    case 'S':
		VM.addSecurityOption(value);
		break;

	    case 'T':
		YoixModule.tune(N_TMPDIR, value);
		break;
	}
    }


    private static void
    tune(String key, Object value) {

	//
	// Handles the initialization that can come from property files
	// or command line options, but in this case we're dealing with
	// tuning that's only associated with a long option.
	//

	if (key != null) {
	    if (value instanceof String) {
		if (key.equals(N_ADDTAGS))
		    YoixModule.tune(key, YoixMake.javaBooleanObject((String)value));
		else if (key.equals(N_APPLET))
		    VM.setApplet((String)value);
		else if (key.equals(N_BIND))
		    YoixModule.tune(key, YoixMake.javaBooleanObject((String)value));
		else if (key.equals(N_COOKIE) || key.equals(N_COOKIES))
		    YoixBodyCookieManager.setDefaultCookies((String)value);
		else if (key.equals(N_COOKIEPOLICY))
		    YoixBodyCookieManager.setDefaultCookiePolicy((String)value);
		else if (key.equals(N_CREATE))
		    YoixModule.tune(key, YoixMake.javaBooleanObject((String)value));
		else if (key.equals(N_EVENTFLAGS))
		    YoixModule.tune(key, value);
		else if (key.equals(N_FIXFONTMETRICS))
		    YoixModule.tune(key, YoixMake.javaBooleanObject((String)value));
		else if (key.equals(N_FIXFONTS))
		    YoixModule.tune(key, YoixMake.javaBooleanObject((String)value));
		else if (key.equals(N_LOOKANDFEEL))
		    VM.setLookAndFeel((String)value);
		else if (key.equals(N_PARSERENCODING)) {
		    value = YoixConverter.getSupportedEncoding(value, VM.getParserEncoding().stringValue());
		    YoixModule.tune(N_PARSERENCODING, value);
		    VM.setPreBootParserEncoding((String)value);
		} else if (key.equals(N_STREAMENCODING)) {
		    value = YoixConverter.getSupportedEncoding(value, VM.getDefaultEncoding().stringValue());
		    YoixModule.tune(N_STREAMENCODING, value);
		    VM.setPreBootDefaultEncoding((String)value);
		} else if (key.equals("propertyfile"))
		    properties((String)value, null);
		else if (key.equals("splash"))
		    splashtitle = (String)value;
		else if (key.equals("splashbackground"))
		    splashbackground = (String)value;
		else if (key.equals("splashforeground"))
		    splashforeground = (String)value;
		else if (key.equals("threadsafe"))
		    YoixBodyComponentSwing.setThreadSafe(YoixMake.javaBoolean((String)value), true);
		else if (key.equals(N_TRACE))
		    YoixModule.tune(key, value);
	    } else if (value == null) {
		if (key.equals(N_ACCEPTCERTIFICATES)) {
		    VM.setPreBootAcceptCertificates(key, Boolean.TRUE);
		    YoixMiscSSL.addHostPair("");
		} else if (key.equals(N_APPLET))
		    VM.setApplet(null);
		else if (key.equals(N_TRACE))
		    YoixModule.tune(N_TRACE, new Integer(MODEL_YOIXCALLSTACKTRACE));
		else if (key.equals("info"))
		    usage(2);
		else if (key.equals("resource"))
		    getresource = true;
		else if (key.equals("splash"))
		    splashtitle = "";
		else if (key.equals("version"))
		    version(false);
		else if (key.equals("versioninfo"))
		    version(true);
	    }
	}
    }


    private static void
    usage(int level) {

	String  lines[];
	int     n;

	if (usage != null) {
	    if (level >= 0 && level < usage.length) {
		lines = usage[level];
		for (n = 0; n < lines.length; n++)
		    System.out.println(lines[n]);
	    }
	}
	VM.exit(0);
    }


    private static void
    version(boolean full) {

	System.out.println(YoixMisc.getVersionInfo(full));
	VM.exit(0);
    }


    private static synchronized boolean
    validate() {

	boolean  result = false;

	//
	// Makes sure Java version is OK and that main() is only called
	// once.
	//

	if (validated == false) {
	    validated = true;
	    if (YoixMisc.jvmCompareTo(JAVAMINVERSION) < 0) {
		try {
		    JOptionPane.showMessageDialog(null, JAVAVERSIONERROR);
		}
		catch(Exception e) {
		    System.err.println(JAVAVERSIONERROR);
		}
		VM.exit(1);
	    } else result = true;
	}
	return(result);
    }
}

