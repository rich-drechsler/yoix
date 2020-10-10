#
# Makefiles that use common.mk must define variables, like ROOT, ALLFILES,
# LOCAL_ALL, LOCAL_INSTALL, LOCAL_CLEAN and LOCAL_CLOBBER, that appear in
# targets, prerequisites, or variable definitions in rules.mk before they
# include common.mk. Other variables, like INSTALLDIR, that are only used
# in command blocks in rules.mk can be defined after common.mk is included
# and can use definitions from variables.mk.
#

ROOT = ..

SOURCE = ywait_rc._PL

ALLFILES = $(SOURCE:._PL=.pl)
LOCAL_INSTALL = local_install

include $(ROOT)/make/common.mk

INSTALLDIR = $(SERVER_HOMEDIR)/etc

local_install :
	@if [ -f passwd -a ! -f $(INSTALLDIR)/$(BETA_PREFIX)passwd ]; then \
	    DEST=$(INSTALLDIR)/$(BETA_PREFIX)passwd; \
	    echo "        cp passwd $$DEST"; \
	    cp passwd $$DEST; \
	    echo "        chmod 644 $$DEST"; \
	    chmod 644 $$DEST; \
	fi

