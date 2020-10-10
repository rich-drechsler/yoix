#
# Intermediate level makefile.
#

ROOT = ..
MAKEFILE = $(firstword $(MAKEFILE_LIST))

include $(ROOT)/make/common.mk

RUNARGS =

#
# $(TARGETS) is the default list of things built by make.
#

TARGETS = yoix

ACTION = all

all : $(TARGETS)

clean clobber install run print :
	@CLASSNAME=; unset CLASSNAME; \
	CLASSPATH=; unset CLASSPATH; \
	PACKAGE=; unset PACKAGE; \
	ROOT=; unset ROOT; \
	SOURCE=; unset SOURCE; \
	CC='$(CC)'; export CC; \
	CFLAGS='$(CFLAGS)'; export CFLAGS; \
	JAVA='$(JAVA)'; export JAVA; \
	JAVAC='$(JAVAC)'; export JAVAC; \
	JAVACC='$(JAVACC)'; export JAVACC; \
	JJTREE='$(JJTREE)'; export JJTREE; \
	JAVACFLAGS='$(JAVACFLAGS)'; export JAVACFLAGS; \
	JAVACLINT='$(JAVACLINT)'; export JAVACLINT; \
	JAVACCFLAGS='$(JAVACCFLAGS)'; export JAVACCFLAGS; \
	JJTREEFLAGS='$(JJTREEFLAGS)'; export JJTREEFLAGS; \
	RUNARGS='$(RUNARGS)'; export RUNARGS; \
	$(MAKE) -e -f $(MAKEFILE) ACTION=$@ $(TARGETS)

$(TARGETS) ::
	@CLASSNAME=; unset CLASSNAME; \
	CLASSPATH=; unset CLASSPATH; \
	PACKAGE=; unset PACKAGE; \
	ROOT=; unset ROOT; \
	SOURCE=; unset SOURCE; \
	CC='$(CC)'; export CC; \
	CFLAGS='$(CFLAGS)'; export CFLAGS; \
	JAVA='$(JAVA)'; export JAVA; \
	JAVAC='$(JAVAC)'; export JAVAC; \
	JAVACC='$(JAVACC)'; export JAVACC; \
	JJTREE='$(JJTREE)'; export JJTREE; \
	JAVACFLAGS='$(JAVACFLAGS)'; export JAVACFLAGS; \
	JAVACLINT='$(JAVACLINT)'; export JAVACLINT; \
	JAVACCFLAGS='$(JAVACCFLAGS)'; export JAVACCFLAGS; \
	JJTREEFLAGS='$(JJTREEFLAGS)'; export JJTREEFLAGS; \
	RUNARGS='$(RUNARGS)'; export RUNARGS; \
	if [ -d $@ -a -f $@/$@.mk ]; then \
	    cd $@; \
	    echo "==== Making $(ACTION) in directory $$(pwd) ===="; \
	    $(MAKE) -e -f $@.mk MAKE=$(MAKE) $(ACTION); \
	fi

