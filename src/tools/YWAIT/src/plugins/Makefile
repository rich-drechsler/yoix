#
# Makefiles that use common.mk must define variables, like ROOT, ALLFILES,
# LOCAL_ALL, LOCAL_INSTALL, LOCAL_CLEAN and LOCAL_CLOBBER, that appear in
# targets, prerequisites, or variable definitions in rules.mk before they
# include common.mk. Other variables, like INSTALLDIR, that are only used
# in command blocks in rules.mk can be defined after common.mk is included
# and can use definitions from variables.mk.
#
# NOTE - we clear BETA_PREFIX because everything here is only supposed to
# be loosely connected to the YWAIT package. In other words, test versions
# of plugins are not handled by BETA_PREFIX.
#

ROOT = ..

SOURCE = \
	ydat._YX \
	test_1.data \
	test_2.data

ALLFILES = $(SOURCE:._YX=.yx)

include $(ROOT)/make/common.mk

BETA_PREFIX =
INSTALLDIR = $(SERVER_HOMEDIR)/plugins

