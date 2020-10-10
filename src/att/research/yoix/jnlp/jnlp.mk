#
# Builds our jnlp module, but Java's jnlp classes currently (as of JDK 1.6)
# sit in the javaws.jar file which means CLASSPATH has to be properly set
# before the compile will work.
#

ROOT = ../../../..
MAKEFILE = $(firstword $(MAKEFILE_LIST))

include $(ROOT)/make/common.mk

#
# You may have to modify the following CLASSPATH definition so it properly
# reflects where the javax.jnlp classes can be found. They're currently in
# javaws.jar and on our systems /usr/local/lib/javaws.jar is a link to the
# jar file that's associated with javac.
#

JAVAWS_JAR = /usr/local/lib/javaws.jar
CLASSPATH = $(ROOT):$(JAVAWS_JAR)

SOURCE = \
	Constants.java \
	Module.java

all :
	@if [ -f $(JAVAWS_JAR) ]; \
	    then $(MAKE) -f $(MAKEFILE) JAVACFLAGS='$(JAVACFLAGS)' classfiles; \
	    else \
		if [ -d STORAGE/$(JAVACTARGET) ]; \
		    then cp STORAGE/$(JAVACTARGET)/*.class .; \
		    else cp STORAGE/*.class .; \
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

