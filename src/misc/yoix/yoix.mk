#
# Yoix miscellaneous makefile.
#

ROOT = ../..
MAKEFILE = $(firstword $(MAKEFILE_LIST))

include $(ROOT)/make/common.mk

ZIPFILES = \
	examples.zip \
	papers.zip \
	reference.zip

all clean clobber run :
	@:

install : $(ZIPFILES)
	@if [ "$(INSTALLDIR)" ]; \
	    then \
		if [ -d "$(INSTALLDIR)" ]; \
		    then \
			echo "install -d -m755 $(INSTALLDIR)/yoix"; \
			install -d -m755 "$(INSTALLDIR)/yoix"; \
			echo "install -m644 $(ZIPFILES) $(INSTALLDIR)/yoix"; \
			install -m644 $(ZIPFILES) "$(INSTALLDIR)/yoix"; \
		    else echo "Skipping install because installation directory $(INSTALLDIR) doesn't exist"; \
		fi; \
	    else \
		if [ "$(INSTALLDIR_ORIGINAL)" ]; \
		    then echo "Skipping install because installation directory $(INSTALLDIR_ORIGINAL) doesn't exist"; \
		    else echo "Skipping install because no installation directory is set"; \
		fi; \
	fi

