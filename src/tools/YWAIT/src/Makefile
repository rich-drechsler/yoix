#
# This is the top-level Makefile for the Yoix based web application that
# was built by YWAIT and it's the file you use when want to do something
# to the entire package. Makefiles in subdirectories listed in SRCDIRS
# can be used when you're only interested in part of the packages. The
# targets that this Makefile (and the others in this package) recognize
# are:
#
#	    all - builds everything but doesn't install
#
#	  clean - removes most of the files that make built
#
#	clobber - Removes everything built by make
#
#	  diffs - Compares source files to installed code
#
#	install - Builds and installs everything
#
# We usually use
#
#    make all install
#
# because it completely separates the build and install steps, and that
# gives make a chance to quit before anything is installed if the build
# fails.
#
# Most of the selections made when you (or someone on your behalf) ran
# the YWAIT configuration program can be found make/variables.mk and you
# can change those definitions if you want. There's a brief description
# of the most important variables in make/variables.mk and a longer one
# in the top level README file (i.e., ./README).
#
# The selections that aren't defined in make/variables.mk are buried in
# file config/gsubsti._YX. It's a non-trivial Yoix script that's used
# to preprocess the source files that end in special suffixes, like ._PL,
# ._SH, and ._YX, but if you're careful you can change those definitions
# by hand too. Incidentally, the suffixes used to mark source files that
# need preprocessing mean the original and "gsubsti.yx" edited files can
# be distinguished on a case-insensitive file system.
#
# If you create your own source directories make sure they're listed in
# SRCDIRS and have appropriate Makefiles. Copying and then modifying an
# existing Makefile, like examples/Makefile, usually is the best way to
# start. Also notice that the INSTALLDIR definition in examples/Makefile
# determines where the source files are installed, so the names of your
# new source directories isn't fundamentally important.
#
# The SERVER_ROOT variable that's defined below is really supposed to be
# set on the command line, so you should leave it blank in this file. We
# sometimes use it as follows. First we type
#
#	make all
#
# to build everything using the values currently defined in variables.mk.
# After that we might type
#
#	make SERVER_ROOT=/tmp/qwert install
#
# and we would end up installing the server software under /tmp/qwert,
# even though it's supposed to run somewhere else. Sounds strange, but
# it can be useful because you can easily compare the new software to
# whatever is currently installed, and when you're ready you can just
# move (or copy) /tmp/qwert to where it really belongs and it should
# work.
#
# Incidentally, if you prefer you can set SERVER_CGIBIN, SERVER_DOCDIR
# and SERVER_HOMEDIR on the command line, but a single variable (i.e.,
# SERVER_ROOT) is convenient.
# 

###### MAKEFLAGS = --no-print-directory    # gmake only

ROOT = .
SERVER_ROOT =

include $(ROOT)/make/variables.mk

SRCDIRS = \
	admin \
	admin/bin \
	admin/broadcast \
	bin \
	custom \
	cgi-bin \
	etc \
	examples \
	help \
	htdocs \
	jars \
	lib \
	plugins \
	plugins/ydat \
	screens \
	subs

#
# We export several variables defined in variables.mk and call make with
# the -e option so they can be set on the command line and passed on to
# lower level Makefiles. Seems cleaner than putting the definitions on
# the command line in our make call, but that should also work. Exported
# variables are dereferenced twice, so funny characters could introduce
# quoting issues.
#

all install clean clobber diffs : $(GSUBSCRIPT)
	@umask $(UMASK); \
	SERVER_CGIBIN='$(SERVER_ROOT)$(SERVER_CGIBIN)'; export SERVER_CGIBIN; \
	SERVER_DOCDIR='$(SERVER_ROOT)$(SERVER_DOCDIR)'; export SERVER_DOCDIR; \
	SERVER_HOMEDIR='$(SERVER_ROOT)$(SERVER_HOMEDIR)'; export SERVER_HOMEDIR; \
	LIST='$(SRCDIRS)'; \
	for DIR in $$LIST; do \
	    if [ -f $$DIR/Makefile ]; then \
		echo "Making $@ in directory $$DIR"; \
		(cd $$DIR && $(MAKE) -e $@) || exit 1; \
	    fi; \
	done

#
# We need the rule that's used to build $(GSUBSCRIPT).
#

include $(ROOT)/make/gsubsti.mk

