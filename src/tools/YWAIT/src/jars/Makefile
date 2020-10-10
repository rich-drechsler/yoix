#
# Makefiles that use common.mk must define variables, like ROOT, ALLFILES,
# LOCAL_ALL, LOCAL_INSTALL, LOCAL_CLEAN and LOCAL_CLOBBER, that appear in
# targets, prerequisites, or variable definitions in rules.mk before they
# include common.mk. Other variables, like INSTALLDIR, that are only used
# in command blocks in rules.mk can be defined after common.mk is included
# and can use definitions from variables.mk.
#
# A complicated makefile that didn't easily fit the model that we use in
# most of the other source directories. Probably would be complicated no
# matter what, because it's doing some difficult work.
#

ROOT = ..

SOURCE = \
	buildinstaller.yx \
	YwaitHostNameVerifier.java \
	YwaitOption.java \
	YwaitX509TrustManager.java \
	Ywait_Client._JAVA \
	Ywait_Installer._HTML \
	Ywait_Installer._YX \
	Ywait_Installer._JAVA

ALLFILES =

LOCAL_ALL = local_all
LOCAL_CLEAN = local_clean
LOCAL_CLOBBER = local_clobber
LOCAL_DIFFS = local_diffs
LOCAL_INSTALL = local_install

include $(ROOT)/make/common.mk

##
## These aren't needed by common.mk, so they can follow the include and
## use definitions made in common.mk.
##

HTDOCSDIR = $(SERVER_DOCDIR)
ADMINBINDIR = $(SERVER_HOMEDIR)/admin/bin
JARSDIR = $(SERVER_DOCDIR)/jars

PROGRAM = $(BETA_PREFIX)$(ACRONYM_UPLOW)
CLIENT = $(PROGRAM)Client
RELEASE = $(JARFILE_RELEASE)
EXECUTABLEJAR = $(PROGRAM).jar_$(RELEASE)
INSTALLER = $(PROGRAM)Installer

##
## HTACCESS is a recent addition that may or may not help enforce the fact
## that a file that ends in ".jar" is an executable jar file not a zip file.
## The entry must be allowed by the httpd config file on your server. Without
## something like this IE on many Windows platforms may insist that jar files
## that we send to a client be downloaded as a .zip file or opened by WinZip.
##

HTACCESS = htaccess

##
## These are the files that are included in the installer as a zip archive.
##

COMPONENTS = \
	$(INSTALLER).yx \
	$(CLIENT).jar \
	$(PROGRAM).sh \
	$(PROGRAM).bat \
	license.zip \
	top.zip \
	yoix.jar

##
## Special targets that common.mk will recognize because they've assigned
## to the special LOCAL_* variables that common.mk supports.
##

local_all : $(GSUBSCRIPT) $(INSTALLER).html $(INSTALLER).jar $(EXECUTABLEJAR)

local_clean :

local_clobber :
	@echo "        rm -f *.class"
	@rm -f *.class
	@echo "        rm -f $(CLIENT).jar $(CLIENT)._JAVA $(CLIENT).java $(CLIENT).mf"
	@rm -f $(CLIENT).jar $(CLIENT)._JAVA $(CLIENT).java $(CLIENT).mf
	@echo "        rm -f $(PROGRAM).jar $(PROGRAM).bat $(PROGRAM).sh"
	@rm -f $(PROGRAM).jar $(PROGRAM).bat $(PROGRAM).sh
	@echo "        rm -f $(INSTALLER).jar $(INSTALLER).mf $(INSTALLER).zip $(INSTALLER).junk"
	@rm -f $(INSTALLER).jar $(INSTALLER).mf $(INSTALLER).zip $(INSTALLER).junk
	@echo "        rm -f $(INSTALLER).java $(INSTALLER).string Ywait_Installer.java"
	@rm -f $(INSTALLER).java $(INSTALLER).string Ywait_Installer.java
	@echo "        rm -f $(INSTALLER)._YX $(INSTALLER).yx $(INSTALLER)._HTML $(INSTALLER).html"
	@rm -f $(INSTALLER)._YX $(INSTALLER).yx $(INSTALLER)._HTML $(INSTALLER).html
	@echo "        rm -f $(EXECUTABLEJAR) license.zip top.zip"
	@rm -f $(EXECUTABLEJAR) license.zip top.zip

local_install :
	@for i in $(INSTALLER).html; do \
	    DEST=$(HTDOCSDIR)/$$i; \
	    if [ -f "$$i" ]; then \
		if cmp -s $$i $$DEST; \
		    then true; \
		    else \
			echo "        cp $$i $$DEST"; \
			touch $$DEST; \
			cp $$i $$DEST; \
			chmod 644 $$DEST; \
		fi; \
	    fi; \
	done
	@if [ -f "$(HTACCESS)" ]; then \
	    DEST=$(JARSDIR)/.htaccess; \
	    if cmp -s $(HTACCESS) $$DEST; \
		then true; \
		else \
		    echo "        cp $(HTACCESS) $$DEST"; \
		    touch $$DEST; \
		    cp $(HTACCESS) $$DEST; \
		    chmod 644 $$DEST; \
	    fi; \
	fi
	@for i in $(INSTALLER).jar $(EXECUTABLEJAR); do \
	    DEST=$(JARSDIR)/$$i; \
	    if [ -f "$$i" ]; then \
		if cmp -s $$i $$DEST; \
		    then true; \
		    else \
			echo "        cp $$i $$DEST"; \
			touch $$DEST; \
			cp $$i $$DEST; \
			chmod 644 $$DEST; \
		fi; \
	    fi; \
	done

local_diffs :
	@for i in $(INSTALLER).html; do \
	    DEST=$(HTDOCSDIR)/$$i; \
	    if [ -f "$$i" ]; then \
		if cmp -s $$i $$DEST; \
		    then true; \
		    else \
			echo "        diff $(DIFF_OPTIONS) $$DEST $$i"; \
			diff $(DIFF_OPTIONS) $$DEST $$i 2>&1 || true; \
		fi; \
	    fi; \
	done
	@if [ -f "$(HTACCESS)" ]; then \
	    DEST=$(JARSDIR)/.htaccess; \
	    if cmp -s $(HTACCESS) $$DEST; \
		then true; \
		else \
		    echo "        diff $(DIFF_OPTIONS) $$DEST $(HTACCESS)"; \
		    diff $(DIFF_OPTIONS) $$DEST $(HTACCESS) 2>&1 || true; \
	    fi; \
	fi
	@for i in $(INSTALLER).jar $(EXECUTABLEJAR); do \
	    DEST=$(JARSDIR)/$$i; \
	    if [ -f "$$i" ]; then \
		if cmp -s $$i $$DEST; \
		    then true; \
		    else \
			echo "        diff $(DIFF_OPTIONS) $$DEST $$i"; \
			diff $(DIFF_OPTIONS) $$DEST $$i 2>&1 || true; \
		fi; \
	    fi; \
	done

##
## Building some of these targets is difficult, but the really hard work
## is buried in the intermediate targets that follow.
##
## NOTE - the makefile in old releases wanted to allow the removal of some
## intermediate files (in local_clean) without triggering a build of these
## targets. It never worked properly, so we decided to make the dependency
## lists more accurate and moved local_clean stuff to local clobber. We may
## revisit this is in the future.
##

$(INSTALLER).html : $(INSTALLER)._HTML Makefile
	@echo "        $(YOIX) $(GSUBSCRIPT) < $(INSTALLER)._HTML > $@"
	@$(YOIX) $(GSUBSCRIPT) < $(INSTALLER)._HTML > $@

$(INSTALLER).jar : $(INSTALLER).class $(INSTALLER).mf Makefile
	@echo "        $(JAR) cfm $@ $(INSTALLER).mf $(INSTALLER).class"
	@$(JAR) cfm $@ $(INSTALLER).mf $(INSTALLER).class

$(EXECUTABLEJAR) : $(PROGRAM).jar Makefile
	@echo "        cp $(PROGRAM).jar $@"
	@cp $(PROGRAM).jar $@
	@echo "        $(JAR) -i $@"
	@$(JAR) -i $@

##
## These are the intermediate targets mentioned above that handle most
## of the hard work, but they're only used when $(MAKE) is called from
## an action block that builds one of the installable targets.
##

top.zip : LICENSE_BINARY.txt
	@rm -f $@
	@zip -q $@ LICENSE_BINARY.txt

license.zip :
	@>$@

$(CLIENT).mf :
	@echo "Main-Class: $(CLIENT)" >$@

$(CLIENT)._JAVA : Ywait_Client._JAVA
	@sed \
	    -e "s:class Ywait_Client:class $(CLIENT):" \
	Ywait_Client._JAVA >$@

$(CLIENT).jar : $(CLIENT)._JAVA $(CLIENT).java $(CLIENT).mf YwaitOption.class YwaitHostNameVerifier.class YwaitX509TrustManager.class
	@echo "        $(JAR) cf $@ YwaitOption.class YwaitHostNameVerifier.class YwaitX509TrustManager.class"
	@$(JAR) cf $@ YwaitOption.class YwaitHostNameVerifier.class YwaitX509TrustManager.class
	@echo "        $(JAVAC) $(JAVACFLAGS) -source 1.2 -target 1.2 $(CLIENT).java"
	@$(JAVAC) $(JAVACFLAGS) -source 1.2 -target 1.2 $(CLIENT).java
	@echo "        $(JAR) ufm $@ $(CLIENT).mf $(CLIENT).class"
	@$(JAR) ufm $@ $(CLIENT).mf $(CLIENT).class 2>/dev/null

$(PROGRAM).bat : ywait._bat
	@cp ywait._bat $@

$(PROGRAM).sh : ywait._sh
	@cp ywait._sh $@

$(PROGRAM).jar : $(CLIENT).jar $(CLIENT).mf yoix.jar
	@cp yoix.jar $@
	@echo "        $(JAR) ufm $@ $(CLIENT).mf $(CLIENT).class YwaitOption.class YwaitHostNameVerifier.class YwaitX509TrustManager.class"
	@$(JAR) ufm $@ $(CLIENT).mf $(CLIENT).class YwaitOption.class YwaitHostNameVerifier.class YwaitX509TrustManager.class 2>/dev/null

$(INSTALLER)._HTML : Ywait_Installer._HTML
	@cp Ywait_Installer._HTML $@

$(INSTALLER)._YX : Ywait_Installer._YX
	@cp Ywait_Installer._YX $@

$(INSTALLER).mf : Makefile
	@echo "Main-Class: $(INSTALLER)" >$@

$(INSTALLER).class : $(COMPONENTS) Ywait_Installer.java buildinstaller.yx Ywait_Installer._YX Makefile
	@zip -q $(INSTALLER).zip $(COMPONENTS)
	@sed "s/Ywait_Installer/$(INSTALLER)/g" Ywait_Installer.java >$(INSTALLER).java
	@$(YOIX) ./buildinstaller.yx $(INSTALLER).java $(INSTALLER).zip >$(INSTALLER).junk
	@mv $(INSTALLER).junk $(INSTALLER).java
	@echo "        $(JAVAC) $(JAVACFLAGS) -source 1.2 -target 1.2 $(INSTALLER).java"
	@$(JAVAC) $(JAVACFLAGS) -source 1.2 -target 1.2 $(INSTALLER).java

