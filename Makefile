#
# A makefile that we only use to build the source and binary zip files that
# are distributed as a new Yoix release. By default we assume the directory
# that contains this file is a git repository and there's another directory
# named src in this directory that contains the official Yoix source code.
#
# Typing
#
#	make
#
# or
#
#	make release
#
# builds the zip files that correspond to the value assigned to VERSION and
# leaves them in the directory named releases. By default, nothing's built if
# the git working tree isn't clean, but GIT and CHECKCLEAN, which are defined
# later in this file, give you lots of control.
#

MAKEFILE = $(firstword $(MAKEFILE_LIST))
MAKEFLAGS = --no-print-directory
SHELL = bash

VERSION = 3.0.0
TARGET_OSNAME = linux

#
# Using override in the next definition is just a precaution to prevent users
# from setting RELEASE on the command line.
#

override RELEASE = yoix-$(VERSION)

#
# If you want to permanently disable git change the next definition to
#
#       GIT = :
#
# or if you just want to change it temporarily set GIT on the command line
#
#	make GIT=:
#
# and either way everything that depends on git will be skipped. Set CHECKCLEAN
# to FALSE (actually anything other than TRUE) and the working tree check will
# be skipped when you're using git. It's something that's primarily useful when
# we're working on this makefile.
#

GIT = git
CHECKCLEAN = TRUE

release : validate
	@if [ ! -f releases/$(RELEASE)-binary.zip ] || [ ! -f releases/$(RELEASE)-src.zip ]; \
	    then \
		echo "Building version $(VERSION) zip files in directory $$(pwd)/releases"; \
		make -f $(MAKEFILE) update; \
		rm -fr "releases/$(RELEASE)"; \
		rm -f releases/$(RELEASE)-src.zip releases/$(RELEASE)-binary.zip; \
		mkdir -p "releases/$(RELEASE)/src"; \
		cd "releases/$(RELEASE)/src"; \
		cp -pr ../../../src ..; \
		$(MAKE) "INSTALLDIR=.." TARGET_OSNAME=$(TARGET_OSNAME) clobber; \
		$(MAKE) "INSTALLDIR=.." TARGET_OSNAME=$(TARGET_OSNAME) install; \
		$(MAKE) "INSTALLDIR=.." TARGET_OSNAME=$(TARGET_OSNAME) clobber; \
		cd ../..; \
		zip -qr $(RELEASE)-src.zip $(RELEASE)/src; \
		rm -fr $(RELEASE)/src; \
		zip -qr $(RELEASE)-binary.zip $(RELEASE); \
		rm -fr "$(RELEASE)"; \
		cp ../src/LICENSE.txt .; \
		$(GIT) add $(RELEASE)-src.zip $(RELEASE)-binary.zip LICENSE.txt >/dev/null 2>&1; \
		cd ..; \
		$(GIT) commit -m "Release dependent automatically generated changes" >/dev/null 2>&1; \
		echo "Version $(VERSION) zip files successfully created in directory $$(pwd)"; \
	    else echo "Release not built - zip files for version $(VERSION) already exist"; \
	fi

update : validate
	@README_SOURCE="\n                  Yoix Version $(VERSION) - Source Distribution\n                                  $$(date +"%b %e %Y")"; \
	README_BINARY="\n                  Yoix Version $(VERSION) - Binary Distribution\n                                  $$(date +"%b %e %Y")"; \
	YOIX_CREATED="$$(date)"; \
	cd src; \
	echo "Updating README file in directory $$(pwd)"; \
	echo -e "$$README_SOURCE" >README.tmp; \
	sed '1,3d' README >>README.tmp; \
	mv README.tmp README; \
	$(GIT) add README >/dev/null 2>&1; \
	echo "Updating README.binary file in directory $$(pwd)"; \
	echo -e "$$README_BINARY" >README.binary.tmp; \
	sed '1,3d' README.binary >>README.binary.tmp; \
	mv README.binary.tmp README.binary; \
	$(GIT) add README.binary >/dev/null 2>&1; \
	cd att/research/yoix; \
	echo "Updating README file in directory $$(pwd)"; \
	echo -e "$$README_SOURCE" >README.tmp; \
	sed '1,3d' README >>README.tmp; \
	mv README.tmp README; \
	$(GIT) add README >/dev/null 2>&1; \
	echo "Updating YoixConstants.java file in directory $$(pwd)"; \
	sed -e 's/ YOIXVERSION = ".*";$$/ YOIXVERSION = "$(VERSION)";/' -e "s/ YOIXCREATED = \".*\";$$/ YOIXCREATED = \"$$YOIX_CREATED\";/" YoixConstants.java >YoixConstants.java.tmp; \
	mv YoixConstants.java.tmp YoixConstants.java; \
	$(GIT) add YoixConstants.java >/dev/null 2>&1; \
	$(GIT) commit -m "Release dependent automatically generated changes" >/dev/null 2>&1

clean :
	@echo "rm -fr releases/$(RELEASE)"
	@rm -fr "releases/$(RELEASE)"

clobber : clean
	@#
	@# A little harder than you might expect, mostly to make sure it works
	@# whether git is disabled or not.
	@#
	@for FILE in "releases/$(RELEASE)-binary.zip" "releases/$(RELEASE)-src.zip"; do \
	    if $(GIT) rm -f "$$FILE" >/dev/null 2>&1; then \
		$(GIT) add "$$FILE" >/dev/null 2>&1; \
		$(GIT) commit -m "Release dependent automatically generated changes" >/dev/null 2>&1; \
	    fi; \
	    echo rm -f "$$FILE"; \
	    rm -f "$$FILE"; \
	done

validate :
	@STATUS=1; \
	if [ "$(VERSION)" ]; \
	    then \
		if [[ "$(VERSION)" =~ ^[a-zA-Z0-9][a-zA-Z0-9._-]+$$ ]]; \
		    then \
			if [ -d src ]; \
			    then \
				if [ -d .git -o "$(GIT)" = ":" ]; \
				    then \
					if [ "$(CHECKCLEAN)" = "TRUE" ]; \
					    then \
						if [ ! -z "$$($(GIT) status --porcelain 2>/dev/null)" ]; \
						    then echo "Release not built - working tree $$(pwd) is not clean"; \
						    else STATUS=0; \
						fi; \
					    else STATUS=0; \
					fi; \
				    else echo "Git repository $$(pwd)/.git doesn't exist"; \
				fi; \
			    else echo "Source directory $$(pwd)/src doesn't exist"; \
			fi; \
		    else echo "$(VERSION) is not a valid version id"; \
	        fi; \
	    else echo "VERSION make variable has not been set"; \
	fi; \
	exit "$$STATUS"

