#
# Most of these variables are initialized with values that were selected
# when you (or someone on your behalf) ran the YWAIT configuration program.
# Quoting rules described at the end of these comments should be followed
# when a variable contains special characters (currently just ' " & and \).
# There's lots of documentation in the top level README file, so all we're
# going to do here is give a very brief description of the variables: 
#
#    ACRONYM
#	Upper case short name of your application. There currently
#	aren't any restrictions on characters, but quoting rules
#	defined below must be followed.
#
#    ACRONYM_LOWER
#	Lower case short name of your application - spaces and other
#	funny characters will cause problems.
#
#    ACRONYM_UPLOW
#	Mixed case short name of your application - spaces and other
#	funny characters will cause problems.
#
#    BETA_PREFIX
#	A variable that can be set to allow installation of a test
#	version that runs in parallel with a production system. The
#	prefix avoids name clashes with the installed files.
#
#    COMMAND_PATHS
#	A CSV string of keywords and file paths to executables on
#	the server. These will be placed into a global hash called
#	%COMMANDPATHS in the perl scripts.
#
#    DIFF_OPTIONS
#	Command line options that are handed to diff when the diffs
#	target is built. Setting them on the command line when you
#	run make is the best way to temporarily change DIFF_OPTIONS.
#
#    FULLNAME
#	The full name of your application. There are no restrictions
#	on characters, but the quoting rules defeind below must be
#	followed.
#
#    GSUBSCRIPT_BASENAME
#	Base name of Yoix script used to preprocess source files that
#	are marked by special suffixes (e.g., ._PL, ._YX).
#
#    JARFILE_BASENAME
#	Base name of the jar file that gets loaded on client machines
#	when they run the installer.
#
#    JARFILE_RELEASE
#	Should be X.Y.Z (where X, Y, Z are digits) and it's used to
#	identify the jar file that's installed on client systems when
#	they run the installer. It's also used in ../etc/ywait_rc._PL
#	to initialize the list of acceptable releases. It is also
#	displayed to the end-user as the application release number.
#
#    JARFILE_RELEASE_DATE
#	A date displayed to the end-user as the application release
#	date. Something like
#
#	    JARFILE_RELEASE_DATE = $(shell date '+%B %d, %Y')
#
#	works if you're using gmake and you always want to assign the
#	current date to JARFILE_RELEASE_DATE. It's not necessarily the
#	best approach because JARFILE_RELEASE_DATE changes every time
#	gmake runs, but this file's timestamp doesn't change and that
#	means you don't really control the value that's used in source
#	files (unless you clobber everything first). A hardcoded date
#	is the preferred choice.
#	
#    JAVA_BIN
#	Where the appropriate versions of java, javac (the compiler)
#	and jar can be found. We recommend Java version 1.5.0, but
#	any 1.4.X should work.
#
#    OWNER
#	The name of the owner of this application, typically the name
#	of the company paying your salary. The SUBOWNER field can be
#	used to provide more detail.
#
#    PERL_PATH
#	Full path name of an appropriate version of Perl.
#
#    PROPRIETARY_LABEL
#	Text that appears at the bottom of almost every system screen.
#	We often use it for a proprietary label, but you're free to use
#	it for whatever you want. An empty label is allowed and is often
#	the right choice.
#
#    SERVER_ALIAS
#	A URL path that httpd maps to your SERVER_DOCDIR directory.
#
#    SERVER_ALIAS_PREFIX
#	A string, often just SERVER_ALIAS, that can be used to build
#	URLs in the Java class that serves as the "wrapper" for the
#	installer. The implicit assumption is that the 'Alias' portion
#	of the URLS for the PROTOTYPE, PRODUCTION, DEMONSTRATION, and
#	DEVELOPMENT systems can be easily built from this string, which
#	is not always true.
#
#    SERVER_BASEURL
#	A URL that points at your web server - we recommend supplying
#	an IP address rather than relying on DNS.
#
#    SERVER_CGIBIN
#	Where httpd looks for your application's installed cgi scripts.
#
#    SERVER_DOCDIR
#	Where httpd looks for your application's installed documents,
#	which are primarily Yoix scripts that the client executes.
#
#    SERVER_HOMEDIR
#	Everything that doesn't end up SERVER_CGIBIN or SERVER_DOCDIR
#	is installed under this directory.
#
#    SERVER_SCRIPTALIAS
#	A URL path that httpd maps to your SERVER_CGIBIN directory.
#
#    SERVER_SETUIDFILES
#	The names of the executables that are installed as setuid and
#	setgid programs. Probably should be an empty list if your web
#	server handles things for you (i.e., automatically runs your
#	cgi scripts as you). Incidentally, unlike the other variables
#	being set in this file, this variable is used only during the
#	make process and not for any pre-processing of source files.
#
#    SERVER_TYPE
#	Set to PRODUCTION, DEMONSTRATION, DEVELOPMENT, or PROTOTYPE and
#	used to control login messages, screen colors etc.
#
#    SUBOWNER
#	The name of the organization within OWNER that is the owner of
#	this application.
#
#    USER_NAME
#	Normally should be set to NULL, which means the clients have to
#	supply a name and password to login. Other values are allowed,
#	but they require some additional effort (described in the top
#	level README) before they really work.
#
#    YOIX_FONTOPTION
#	An option for the Yoix interpreter that can be used to override
#	the default setting that controls low level font tuning that the
#	interpreter applies when it starts. The value should be -f if you
#	want to disable the tuning, +f to enable it, and empty to use the
#	interpreter's default setting. The interpreter's default setting
#	was +f in 2.1.5 and all older releases, however it may change in
#	future releases. Legacy systems may want to set this option to +f
#	to make sure the font tuning behavior doesn't change when newer
#	versions of the Yoix interpreter become available.
#
# The following quoting rules,
#
#	single quote (') should be escaped using: '"'"'
#	double quote (") should be escaped using: \\\"
#	ampersand (&) should be escaped using: \&
#	backslash (\), except as introduced as above, should become: \\\\
#
# must be followed whenever any of these characters appears in the value
# assigned to a variable.
#

ACRONYM = YWAITDEMO
ACRONYM_LOWER = ywaitdemo
ACRONYM_UPLOW = YwaitDemo
BETA_PREFIX =
COMMAND_PATHS = SORTCMD,/bin/sort,UNIQCMD,/usr/bin/uniq
DIFF_OPTIONS = -t
FULLNAME = Ywait Demo
GSUBSCRIPT_BASENAME = gsubsti
JARFILE_BASENAME = YwaitDemo
JARFILE_RELEASE = 1.0.0
JARFILE_RELEASE_DATE =
JAVA_BIN = /usr/local/java/bin
OWNER =
PERL_PATH = /usr/bin/perl
PROPRIETARY_LABEL =
SERVER_ALIAS = ywaitdemo
SERVER_ALIAS_PREFIX = ywaitdemo
SERVER_BASEURL = http://127.0.0.1
SERVER_CGIBIN = /var/www/ywaitdemo/cgi-bin
SERVER_DOCDIR = /var/www/ywaitdemo/htdocs
SERVER_HOMEDIR = /var/www/ywaitdemo
SERVER_SCRIPTALIAS = ywaitdemo-cgi
SERVER_SETUIDFILES = ywait_exec ywait_login
SERVER_TYPE = DEMONSTRATION
SUBOWNER =
USER_NAME = NULL
YOIX_FONTOPTION =

#
# Additional common variables and includes
#

UMASK = 022

JAR = $(JAVA_BIN)/jar
JAVA = $(JAVA_BIN)/java
JAVAC = $(JAVA_BIN)/javac
JAVACFLAGS = -g -nowarn

GSUBSCRIPT = $(ROOT)/config/$(GSUBSCRIPT_BASENAME).yx
YOIX = $(JAVA) -Djava.awt.headless=true -jar $(ROOT)/jars/yoix.jar

