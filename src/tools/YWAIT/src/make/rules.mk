#
# This file is included by common.mk, and common.mk is included by every
# Makefile in this source package except the top-level makefile, but only
# only after variables that are used as targets or prerequisites in this
# file (e.g., ALLFILES or LOCAL_ALL) are defined. Variables that are only
# used in command blocks (e.g., INSTALLDIR) can be set after this file is
# included. Makefiles, like ../etc/Makefile, that have non-standard work
# to do define their own special targets and pass them in using LOCAL_ALL,
# LOCAL_INSTALL, LOCAL_CLEAN, LOCAL_CLOBBER, or LOCAL_DIFFS.
# 
# The directories that your server will need when it's installed must be
# named in $(ALL_DIRECTORIES). The list must include directories that are
# named (as INSTALLDIR) in low level makefiles plus other directories that
# your server will need when it's running.
#

ALL_DIRECTORIES = \
	$(SERVER_HOMEDIR) \
	$(SERVER_HOMEDIR)/admin \
	$(SERVER_HOMEDIR)/admin/bin \
	$(SERVER_HOMEDIR)/admin/broadcast \
	$(SERVER_HOMEDIR)/admin/logs \
	$(SERVER_HOMEDIR)/admin/sessionids \
	$(SERVER_HOMEDIR)/bin \
	$(SERVER_HOMEDIR)/etc \
	$(SERVER_HOMEDIR)/help \
	$(SERVER_HOMEDIR)/home \
	$(SERVER_HOMEDIR)/home/admin \
	$(SERVER_HOMEDIR)/lib \
	$(SERVER_HOMEDIR)/plugins \
	$(SERVER_HOMEDIR)/plugins/ydat \
	$(SERVER_HOMEDIR)/screens \
	$(SERVER_HOMEDIR)/subs \
	$(SERVER_HOMEDIR)/tmp \
	$(SERVER_CGIBIN) \
	$(SERVER_DOCDIR) \
	$(SERVER_DOCDIR)/jars

all : source $(ALLFILES) $(LOCAL_ALL)

install : all directories $(LOCAL_INSTALL)
	@if [ -d "$(INSTALLDIR)" ]; then \
	    for i in $(ALLFILES) ""; do \
		if [ -f "$$i" ]; then \
		    CHANGED=FALSE; \
		    DEST=$(INSTALLDIR)/$(BETA_PREFIX)`echo $$i | sed 's/^ywait/$(ACRONYM_LOWER)/'`; \
		    if cmp -s $$i $$DEST; \
			then true; \
			else \
			    echo "        cp $$i $$DEST"; \
			    touch $$DEST; \
			    cp $$i $$DEST; \
			    CHANGED=TRUE; \
		    fi; \
		    if [ "$(INSTALLMODE)" ]; then \
			if [ $$CHANGED = "TRUE" ]; then \
			    echo "        chmod $(INSTALLMODE) $$DEST"; \
			fi; \
			chmod $(INSTALLMODE) $$DEST; \
			if [ "$(SERVER_SETUIDFILES)" ]; then \
			    if echo " $(SERVER_SETUIDFILES) " | grep " $$i " >/dev/null 2>/dev/null; then \
				if [ $$CHANGED = "TRUE" ]; then \
				    echo "        chmod ug+s $$DEST"; \
				fi ; \
				chmod ug+s $$DEST; \
			    fi; \
			fi; \
		    fi; \
		fi; \
	    done; \
	fi

clean : $(LOCAL_CLEAN)
	@for i in $(ALLFILES) ""; do \
	    if [ "$$i" ]; then \
		if echo "$(SOURCE) " | grep -v "$$i " >/dev/null; then \
		    echo "        rm -f $$i"; \
		    rm -f $$i; \
		fi; \
	    fi; \
	done

clobber : clean $(LOCAL_CLOBBER)
	@if [ -f $(GSUBSCRIPT) ]; then \
	    echo "        rm -f $(GSUBSCRIPT)"; \
	    rm -f $(GSUBSCRIPT); \
	fi

diffs : all $(LOCAL_DIFFS)
	@if [ -d "$(INSTALLDIR)" ]; then \
	    for i in $(ALLFILES) ""; do \
		if [ -f "$$i" ]; then \
		    DEST=$(INSTALLDIR)/$(BETA_PREFIX)`echo $$i | sed 's/^ywait/$(ACRONYM_LOWER)/'`; \
		    if cmp -s $$i $$DEST; \
			then true; \
			else \
			    echo "        diff $(DIFF_OPTIONS) $$DEST $$i"; \
			    diff $(DIFF_OPTIONS) $$DEST $$i 2>&1 || true; \
		    fi; \
		fi; \
	    done; \
	fi

#
# Occasionally CGI scripts can run (or do some of their initialization work)
# outside the $(SERVER_SETUIDFILES) programs and as part of that work they
# may want to create files in the application's temporary directory, so as
# a kludge we now add group write permission to the temporary directory if
# any SERVER_SETUIDFILES are defined. Ugly and doesn't guarantee anything,
# but it does occasionally help.
# 

directories :
	@for DIR in $(ALL_DIRECTORIES); do \
	    if [ ! -d $$DIR ]; then \
		echo "        mkdir -p $$DIR"; \
		mkdir -p $$DIR; \
		if [ "$(SERVER_SETUIDFILES)" ]; then \
		    if [ $$DIR = $(SERVER_HOMEDIR)/tmp ]; then \
			echo "        chmod g+rw $$DIR"; \
			chmod g+rw $$DIR; \
		    fi; \
		fi; \
	    fi; \
	done

#
# This is a bit of kludge that's needed, at least by some versions of
# make, if we want to notice non-existent files listed in $(SOURCE).
# Removing the $(ALLFILES) target (below) is another way to handle it,
# but we like the idea of rebuilding $(ALLFILES) when this file or the
# $(GSUBSCRIPT) file changes.
#

source :
	@for i in $(SOURCE) ""; do \
	    if [ "$$i" ]; then \
		if [ ! -f "$$i" ]; then \
		    echo "make: don't know how to build file $$i" 1>&2; \
		    exit 1; \
		fi; \
	    fi; \
	done

#
# Exaggerates dependencies but it doesn't seem to hurt, except that we
# need the "source" target if we want to notice non-existent files that
# happen to be listed in $(SOURCE).
#

$(ALLFILES) place-holder : $(GSUBSCRIPT)

#
# Inference rules - had trouble building C programs from ._C files using
# .SUFFIXES and inference rules on Solaris using Sun's make, so it's not
# currently supported and the rules and suffixes have been removed. We
# eventually should revisit this.
#

.SUFFIXES : ._HTML .html .java .class ._JAVA .java ._PL .pl ._SH .sh ._TXT .txt ._YX .yx .cgi

.java.class :
	@rm -f $*.class
	@echo "        $(JAVAC) -source 1.2 -target 1.2 $(JAVACFLAGS) $<"
	@$(JAVAC) -source 1.2 -target 1.2 $(JAVACFLAGS) $<

._JAVA.java :
	@echo "        $(YOIX) $(GSUBSCRIPT) < $< > $@"
	@$(YOIX) $(GSUBSCRIPT) < $< > $@

._HTML.html :
	@echo "        $(YOIX) $(GSUBSCRIPT) < $< > $@"
	@$(YOIX) $(GSUBSCRIPT) < $< > $@

._PL.pl :
	@echo "        $(YOIX) $(GSUBSCRIPT) < $< > $@"
	@$(YOIX) $(GSUBSCRIPT) < $< > $@
	@chmod u+x $@

._PL.cgi :
	@echo "        $(YOIX) $(GSUBSCRIPT) < $< > $@"
	@$(YOIX) $(GSUBSCRIPT) < $< > $@
	@chmod u+x $@

._SH.cgi :
	@echo "        $(YOIX) $(GSUBSCRIPT) < $< > $@"
	@$(YOIX) $(GSUBSCRIPT) < $< > $@
	@chmod u+x $@

._SH.sh :
	@echo "        $(YOIX) $(GSUBSCRIPT) < $< > $@"
	@$(YOIX) $(GSUBSCRIPT) < $< > $@
	@chmod u+x $@

._TXT.txt :
	@echo "        $(YOIX) $(GSUBSCRIPT) < $< > $@"
	@$(YOIX) $(GSUBSCRIPT) < $< > $@

._YX.yx :
	@echo "        $(YOIX) $(GSUBSCRIPT) < $< > $@"
	@$(YOIX) $(GSUBSCRIPT) < $< > $@

#
# We need the rule that's used to build $(GSUBSCRIPT).
#

include $(ROOT)/make/gsubsti.mk

