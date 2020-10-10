#
# Makefiles that use common.mk must define variables, like ROOT, ALLFILES,
# LOCAL_ALL, LOCAL_INSTALL, LOCAL_CLEAN and LOCAL_CLOBBER, that appear in
# targets, prerequisites, or variable definitions in rules.mk before they
# include common.mk. Other variables, like INSTALLDIR, that are only used
# in command blocks in rules.mk can be defined after common.mk is included
# and can use definitions from variables.mk.
#

ROOT = ..

CSOURCE = \
	ywait_exec._C \
	ywait_login._C

PERLSOURCE = \
	writeheader._PL \
	ywait_exec._PL \
	ywait_login._PL \
	ywait_subtester._PL \
	yoix._PL

SOURCE = $(PERLSOURCE) $(CSOURCE)
ALLFILES = $(PERLSOURCE:._PL=.pl)

LOCAL_ALL = local_all
LOCAL_CLEAN = local_clean
LOCAL_CLOBBER = local_clobber
LOCAL_DIFFS = local_diffs
LOCAL_INSTALL = local_install

include $(ROOT)/make/common.mk

INSTALLDIR = $(SERVER_HOMEDIR)/bin
INSTALLMODE = 755

##
## We had trouble building the C programs on Solaris using Sun's make and
## inference rules and .SUFFIXES that were defined in rules.mk, so we use
## brute force and do it all here. Ugly and doesn't seem like it should be
## necesssary - we eventually should revisit this.
##

local_all : ywait_exec ywait_login

local_clean :
	@echo "        rm -f ywait_exec.c ywait_login.c"
	@rm -f ywait_exec.c ywait_login.c

local_clobber : local_clean
	@echo "        rm -f ywait_exec ywait_login"
	@rm -f ywait_exec ywait_login

local_install :
	@for i in ywait_exec ywait_login; do \
	    if [ -f "$$i" ]; then \
		DEST=$(INSTALLDIR)/$(BETA_PREFIX)`echo $$i | sed 's/^ywait/$(ACRONYM_LOWER)/'`; \
		if cmp -s $$i $$DEST; \
		    then true; \
		    else \
			echo "        cp $$i $$DEST"; \
			touch $$DEST; \
			cp $$i $$DEST; \
			echo "        chmod $(INSTALLMODE) $$DEST"; \
			chmod $(INSTALLMODE) $$DEST; \
			if echo " $(SERVER_SETUIDFILES) " | grep " $$i " >/dev/null 2>/dev/null; then \
			    echo "        chmod ug+s $$DEST"; \
			    chmod ug+s $$DEST; \
			fi; \
		fi; \
	    fi; \
	done

local_diffs :
	@for i in ywait_exec ywait_login; do \
	    if [ -f "$$i" ]; then \
		DEST=$(INSTALLDIR)/$(BETA_PREFIX)`echo $$i | sed 's/^ywait/$(ACRONYM_LOWER)/'`; \
		if cmp -s $$i $$DEST; \
		    then true; \
		    else \
			echo "        diff $(DIFF_OPTIONS) $$DEST $$i"; \
			diff $(DIFF_OPTIONS) $$DEST $$i 2>&1 || true; \
		fi; \
	    fi; \
	done

ywait_exec : ywait_exec._C $(GSUBSCRIPT)
	@echo "        $(YOIX) $(GSUBSCRIPT) < $@._C > $@.c"
	@$(YOIX) $(GSUBSCRIPT) < $@._C > $@.c
	@echo "        $(CC) $(CFLAGS) -o $@ $@.c"
	@$(CC) $(CFLAGS) -o $@ $@.c

ywait_login : ywait_login._C $(GSUBSCRIPT)
	@echo "        $(YOIX) $(GSUBSCRIPT) < $@._C > $@.c"
	@$(YOIX) $(GSUBSCRIPT) < $@._C > $@.c
	@echo "        $(CC) $(CFLAGS) -o $@ $@.c"
	@$(CC) $(CFLAGS) -o $@ $@.c

