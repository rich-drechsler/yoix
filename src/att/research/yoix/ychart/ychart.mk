#
# Makefile for the ychart module.
#

ROOT = ../../../..
MAKEFILE = $(firstword $(MAKEFILE_LIST))

include $(ROOT)/make/common.mk

CLASSPATH = $(ROOT)
YOIXMAIN = att.research.yoix.YoixMain

SOURCE = \
	Constants.java \
	Misc.java \
	Module.java \
	Unicode.java \
	Guide.java \
	Schedule.java

all : $(SOURCE:.java=.class)
	@:

clean :
	rm -f *.class

clobber : clean
	rm -f *.jar

run : all
	@#
	@# During development we often type something like
	@#
	@#     make -f ychart.mk RUNARGS="scripts/ychart.yx data/elements_ychart.yx" run
	@#
	@# to run the interpreter using the source code in this directory. Bottom
	@# line is everything assigned to RUNARGS is passed to the interpreter.
	@#
	@# NOTE - this target makes no attempt at compiling Yoix class files that
	@# aren't in this directory. The assumption is that if you want to use it
	@# you're likely doing some serious development and will figure out what's
	@# wrong if there's a problem (i.e., cd ..; make -f yoix.mk).
	@#
	CLASSPATH=$(CLASSPATH) $(JAVA) $(JAVAFLAGS) $(YOIXMAIN) $(RUNARGS)

install : ychart_linux.bash ychart.bat ychart_elements.bat ychart_schedule.bat ychart_unicode.bat
	@if [ "$(INSTALLDIR)" ]; \
	    then \
		if [ -d "$(INSTALLDIR)" ]; \
		    then \
			echo "install -d -m755 $(INSTALLDIR)/ychart/scripts"; \
			install -d -m755 $(INSTALLDIR)/ychart/scripts; \
			echo "install -m644 scripts/*.yx $(INSTALLDIR)/ychart/scripts"; \
			install -m644 scripts/*.yx $(INSTALLDIR)/ychart/scripts; \
			echo "install -d -m755 $(INSTALLDIR)/ychart/data"; \
			install -d -m755 $(INSTALLDIR)/ychart/data; \
			echo "install -m644 data/*.yx $(INSTALLDIR)/ychart/data"; \
			install -m644 data/*.yx $(INSTALLDIR)/ychart/data; \
			echo "install -d -m755 $(INSTALLDIR)/bin-linux"; \
			echo "install -d -m755 $(INSTALLDIR)/ychart/icons"; \
			install -d -m755 $(INSTALLDIR)/ychart/icons; \
			echo "install -m644 icons/*.png $(INSTALLDIR)/ychart/icons"; \
			install -m644 icons/*.png $(INSTALLDIR)/ychart/icons; \
			install -d -m755 $(INSTALLDIR)/bin-linux; \
			echo "install -m755 ychart_linux.bash $(INSTALLDIR)/bin-linux/ychart"; \
			install -m755 ychart_linux.bash $(INSTALLDIR)/bin-linux/ychart; \
			echo "install -m755 ychart_linux.bash $(INSTALLDIR)/bin-linux/ychart_elements"; \
			install -m755 ychart_linux.bash $(INSTALLDIR)/bin-linux/ychart_elements; \
			echo "install -m755 ychart_linux.bash $(INSTALLDIR)/bin-linux/ychart_schedule"; \
			install -m755 ychart_linux.bash $(INSTALLDIR)/bin-linux/ychart_schedule; \
			echo "install -m755 ychart_linux.bash $(INSTALLDIR)/bin-linux/ychart_unicode"; \
			install -m755 ychart_linux.bash $(INSTALLDIR)/bin-linux/ychart_unicode; \
			echo "install -d -m755 $(INSTALLDIR)/bin-windows"; \
			install -d -m755 $(INSTALLDIR)/bin-windows; \
			echo "install -m644 ychart.bat $(INSTALLDIR)/bin-windows/ychart.bat"; \
			install -m644 ychart.bat $(INSTALLDIR)/bin-windows/ychart.bat; \
			echo "install -m644 ychart_elements.bat $(INSTALLDIR)/bin-windows/ychart_elements.bat"; \
			install -m644 ychart_elements.bat $(INSTALLDIR)/bin-windows/ychart_elements.bat; \
			echo "install -m644 ychart_schedule.bat $(INSTALLDIR)/bin-windows/ychart_schedule.bat"; \
			install -m644 ychart_schedule.bat $(INSTALLDIR)/bin-windows/ychart_schedule.bat; \
			echo "install -m644 ychart_unicode.bat $(INSTALLDIR)/bin-windows/ychart_unicode.bat"; \
			install -m644 ychart_unicode.bat $(INSTALLDIR)/bin-windows/ychart_unicode.bat; \
		    else echo "Skipping install because installation directory $(INSTALLDIR) doesn't exist"; \
		fi; \
	    else \
		if [ "$(INSTALLDIR_ORIGINAL)" ]; \
		    then echo "Skipping install because installation directory $(INSTALLDIR_ORIGINAL) doesn't exist"; \
		    else echo "Skipping install because no installation directory is set"; \
		fi; \
	fi

