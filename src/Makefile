#
# The top-level makefile for the entire Yoix package.
#
# NOTE - INSTALLDIR should reference the top level directory that's used during
# the installation of this package. If INSTALLDIR is empty or doesn't reference
# an existing directory, nothing will be installed. INSTALLDIR is converted to
# an absolute pathname using the realpath command, which should be available on
# any Linux system, and then it's exported to the environment.
#

MAKEFILE = $(firstword $(MAKEFILE_LIST))
MAKEFLAGS = --no-print-directory

INSTALLDIR =
TARGET_OSNAME = none

all install clean clobber :
	@#
	@# Apparently gmake gives precedence to variables defined on the command
	@# line, even when the -e option is defined. Makes redefining INSTALLDIR
	@# when it's a relative path difficult. In that case whenever we change
	@# directories and run gmake, even if INSTALLDIR has been converted to
	@# an absolute path and exported, the relative path is the one that will
	@# be assigned to INSTALLDIR. Setting INSTALLDIR on the make command line
	@# seems to fix the bad behavior. Doing the same for TARGET_OSNAME isn't
	@# necessary since it's not changed again.
	@#
	@# NOTE - eventually should take a look at the use of the -e option and
	@# the environment in all makefiles.
	@#
	@if [ -d att -a -f att/att.mk ]; then \
	    INSTALLDIR="$(realpath $(INSTALLDIR))"; export INSTALLDIR; \
	    INSTALLDIR_ORIGINAL="$(INSTALLDIR)"; export INSTALLDIR_ORIGINAL; \
	    TARGET_OSNAME='$(TARGET_OSNAME)'; export TARGET_OSNAME; \
	    cd att; \
	    $(MAKE) -e -f att.mk "INSTALLDIR=$${INSTALLDIR}" $@; \
	fi
	@if [ -d misc -a -f misc/misc.mk ]; then \
	    INSTALLDIR="$(realpath $(INSTALLDIR))"; export INSTALLDIR; \
	    INSTALLDIR_ORIGINAL="$(INSTALLDIR)"; export INSTALLDIR_ORIGINAL; \
	    TARGET_OSNAME='$(TARGET_OSNAME)'; export TARGET_OSNAME; \
	    cd misc; \
	    $(MAKE) -e -f misc.mk "INSTALLDIR=$${INSTALLDIR}" $@; \
	fi
	@#
	@# Install a few text files and finish any OS dependent steps.
	@#
	@if [ "$@" = "install" -a "$(INSTALLDIR)" ]; then \
	    INSTALLDIR=$(realpath $(INSTALLDIR)); \
	    if [ -d "$${INSTALLDIR}" ]; then \
		if [ -f LICENSE.txt ]; then \
		    echo "install LICENSE.txt $${INSTALLDIR}"; \
		    install LICENSE.txt $${INSTALLDIR}; \
		fi; \
		if [ -f README.binary ]; then \
		    echo "install README.binary $${INSTALLDIR}/README"; \
		    install README.binary $${INSTALLDIR}/README; \
		fi; \
		if [ "$(TARGET_OSNAME)" ]; then \
		    TARGET_OSNAME=$$(echo $(TARGET_OSNAME) | tr a-z A-Z); \
		    if [ "$${TARGET_OSNAME}" != "NONE" ]; then \
			echo "++++ Finishing Yoix install for operating system named $(TARGET_OSNAME) ++++"; \
			if [ "$${TARGET_OSNAME}" = "WINDOWS" ]; \
			    then \
				echo "install -d -m755 $${INSTALLDIR}/bin"; \
				install -d -m755 "$${INSTALLDIR}/bin"; \
				echo "install -p $${INSTALLDIR}/bin-windows/* $${INSTALLDIR}/bin"; \
				install -p "$${INSTALLDIR}/bin-windows"/* "$${INSTALLDIR}/bin"; \
			    elif [ "$${TARGET_OSNAME}" = "LINUX" -o "$${TARGET_OSNAME}" = "UNIX" ]; then \
				echo "install -d -m755 $${INSTALLDIR}/bin"; \
				install -d -m755 "$${INSTALLDIR}/bin"; \
				echo "install -p $${INSTALLDIR}/bin-linux/* $${INSTALLDIR}/bin"; \
				install -p "$${INSTALLDIR}/bin-linux"/* "$${INSTALLDIR}/bin"; \
			    elif [ "$${TARGET_OSNAME}" = "MACOS" -o "$${TARGET_OSNAME}" = "MACOSX" -o "$${TARGET_OSNAME}" = "OSX" ]; then \
				echo "install -d -m755 $${INSTALLDIR}/bin"; \
				install -d -m755 "$${INSTALLDIR}/bin"; \
				echo "install -p $${INSTALLDIR}/bin-linux/* $${INSTALLDIR}/bin"; \
				install -p "$${INSTALLDIR}/bin-linux"/* "$${INSTALLDIR}/bin"; \
			    else echo "Don't recognize target operating system $(TARGET_OSNAME)"; \
			fi; \
		    fi; \
		fi; \
	    fi; \
	fi
	@echo

