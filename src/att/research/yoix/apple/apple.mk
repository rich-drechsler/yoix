#
# This is very old Apple support stuff - no idea if it's needed today.
#

ROOT = ../../../..
MAKEFILE = $(firstword $(MAKEFILE_LIST))

include $(ROOT)/make/common.mk

CLASSPATH = $(ROOT)

SOURCE = \
	AppleApplicationAdapter.java \
	Constants.java \
	Module.java

all :
	@if $(JAVA) -Xdock:name=test -version 2>/dev/null; \
	    then $(MAKE) -f $(MAKEFILE) JAVACFLAGS='$(JAVACFLAGS)' classfiles; \
	    else \
		if [ -d STORAGE/$(JAVACTARGET) ]; \
		    then cp STORAGE/$(JAVACTARGET)/*.class ./; \
		    else cp STORAGE/*.class ./; chmod u+w *.class; \
		fi; \
		chmod u+w *.class; \
	fi

clean :
	rm -f *.class

clobber : clean
	@:

install run :
	@:

classfiles : $(SOURCE:.java=.class)
	@:

