#
# Top level makefile for Java programs. TARGETS must name source
# directories at this level or one level down. Pathnames relative
# to the top level directory are allowed, so
#
#	TARGETS = yoix
#
# and
#
#	TARGETS = research/yoix
#
# refer to the same directory. A target that names a non-existent
# source directory is ignored. Setting TARGETS on the command line
# overrides the default list. The $(TARGETS) action block probably
# is more complicated than you expect, because we always use the
# intermediate level makefile.
#

ROOT = ..
MAKEFILE = $(firstword $(MAKEFILE_LIST))

include $(ROOT)/make/common.mk

RUNARGS =

#
# $(TARGETS) is the default list of things built by make.
#

TARGETS = research

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
	DIRS=`echo */$@ */*/$@`; \
	DIRS="$@ $$DIRS"; \
	HERE=`pwd`; \
	for i in $$DIRS; do \
	    if [ -d "$$i" ]; then \
		PREFIX=`echo $$i | sed s:/\.\*::`; \
		SUFFIX=`echo $$i | sed "s:^$$PREFIX/::"`; \
		cd $$PREFIX; \
		if [ -f "$$PREFIX.mk" ]; then \
		    if [ "$$PREFIX" = "$$SUFFIX" ]; \
			then $(MAKE) -e -f $$PREFIX.mk MAKE=$(MAKE) $(ACTION); \
			else $(MAKE) -e -f $$PREFIX.mk MAKE=$(MAKE) TARGETS=$$SUFFIX $(ACTION); \
		    fi; \
		    if [ $$? -ne 0 ]; then \
			exit 1; \
		    fi; \
		fi; \
		cd $$HERE; \
	    fi; \
	done

