#
# Common definitions - included by most makefiles.
#

MAKEFLAGS = --no-print-directory

CC = cc
CFLAGS = -g

JAR = jar
JAVA = java
JAVAC = javac

JARFLAGS =
JAVAFLAGS =
JAVACFLAGS =
JAVACLINT = none

#
# JavaCC definitions - by default we use the old version that's included with
# the Yoix source code package. Both of these definitions assume the makefiles
# include this file have already defined ROOT.
#
# NOTE - if you want to get fancier you could use the gmake "or" function to
# pick the executables to use for JAVACC and JJTREE. For example
#
#     $(or $(shell /usr/bin/which javacc >/dev/null && echo javacc), $(realpath $(ROOT)/javacc/bin/javacc), javacc)
#
# looks for javacc in your PATH using /usr/bin/which and if it's not found the
# old version that we supply with the source code is used.
#

JAVACC = $(realpath $(ROOT)/javacc/bin/javacc)
JJTREE = $(realpath $(ROOT)/javacc/bin/jjtree)

JAVACCFLAGS =
JJTREEFLAGS =

#
# Setting JAVACSOURCE to 1.5 or 1.6 generates warnings that eventually should
# be addressed. Until then these settings mean we can't compile using anything
# newer that 1.8.X. Oracle or openjdk versions of the SDK both work, as long as
# they're 1.8.X or older.
#

JAVACSOURCE = 1.4
JAVACTARGET = 1.8

#
# Standard targets - unfortunately some versions of make may complain when we
# include shell actions that are redefined in other makefiles (usually by the
# intermediate level makefiles).
#

all install clean clobber run print :

#
# Java metarules
#

.SUFFIXES : .jjt .jj .java .class

.java.class :
	@rm -f $*.class
	CLASSPATH=$(CLASSPATH) $(JAVAC) -Xlint:$(JAVACLINT) -source $(JAVACSOURCE) -target $(JAVACTARGET) $(JAVACFLAGS) $<

.jjt.java :
	@rm -f $*.jj $*.java $*.class
	$(JJTREE) $(JJTREEFLAGS) $< >/dev/null
	$(JAVACC) $(JAVACCFLAGS) $*.jj >/dev/null
	@@rm -f $*.jj

