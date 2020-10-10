#
# Makefiles that use common.mk must define variables, like ROOT, ALLFILES,
# LOCAL_ALL, LOCAL_INSTALL, LOCAL_CLEAN and LOCAL_CLOBBER, that appear in
# targets, prerequisites, or variable definitions in rules.mk before they
# include common.mk. Other variables, like INSTALLDIR, that are only used
# in command blocks in rules.mk can be defined after common.mk is included
# and can use definitions from variables.mk.
#

ROOT = ..

PERLSOURCE = ywait_setup._PL

SHELLSOURCE = \
	ywait_exec._SH \
	ywait_login._SH

SOURCE = $(PERLSOURCE) $(SHELLSOURCE)

ALLFILES = $(SHELLSOURCE:._SH=.cgi) $(PERLSOURCE:._PL=.cgi)

include $(ROOT)/make/common.mk

INSTALLDIR = $(SERVER_CGIBIN)
INSTALLMODE = 755

