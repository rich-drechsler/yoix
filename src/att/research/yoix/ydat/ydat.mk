#
# Yoix YDAT module
#

ROOT = ../../../..
MAKEFILE = $(firstword $(MAKEFILE_LIST))

include $(ROOT)/make/common.mk

CLASSPATH = $(ROOT)
YOIXMAIN = att.research.yoix.YoixMain

SOURCE = \
	Module.java \
	AxisModel.java \
	AxisModelDefault.java \
	AxisModelUnixTime.java \
	BodyComponentSwing.java \
	BoundingBox.java \
	Constants.java \
	DataAxis.java \
	DataColorer.java \
	DataGenerator.java \
	DataManager.java \
	DataPartition.java \
	DataPlot.java \
	DataRecord.java \
	DataTable.java \
	DataViewer.java \
	GraphLayout.java \
	GraphRecord.java \
	HitBuffer.java \
	Misc.java \
	MiscTime.java \
	Palette.java \
	SweepFilter.java \
	SwingDataColorer.java \
	SwingDataPlot.java \
	SwingDataViewer.java \
	SwingJAxis.java \
	SwingJDataTable.java \
	SwingJEventPlot.java \
	SwingJGraphPlot.java \
	SwingJHistogram.java

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
	@#     make -f ydat.mk RUNARGS="scripts/ydat.yx -p -Hscripts -cdemo1 data/demo1.data" run
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

install : ydat_linux.bash ydat.bat ydat_demo1.bat ydat_demo2.bat
	@if [ "$(INSTALLDIR)" ]; \
	    then \
		if [ -d "$(INSTALLDIR)" ]; \
		    then \
			echo "install -d -m755 $(INSTALLDIR)/ydat/scripts"; \
			install -d -m755 $(INSTALLDIR)/ydat/scripts; \
			echo "install -m644 scripts/*.yx $(INSTALLDIR)/ydat/scripts"; \
			install -m644 scripts/*.yx $(INSTALLDIR)/ydat/scripts; \
			echo "install -d -m755 $(INSTALLDIR)/ydat/data"; \
			install -d -m755 $(INSTALLDIR)/ydat/data; \
			echo "install -m644 data/*.data $(INSTALLDIR)/ydat/data"; \
			install -m644 data/*.data $(INSTALLDIR)/ydat/data; \
			echo "install -d -m755 $(INSTALLDIR)/bin-linux"; \
			install -d -m755 $(INSTALLDIR)/bin-linux; \
			echo "install -m755 ydat_linux.bash $(INSTALLDIR)/bin-linux/ydat"; \
			install -m755 ydat_linux.bash $(INSTALLDIR)/bin-linux/ydat; \
			echo "install -m755 ydat_linux.bash $(INSTALLDIR)/bin-linux/ydat_demo1"; \
			install -m755 ydat_linux.bash $(INSTALLDIR)/bin-linux/ydat_demo1; \
			echo "install -m755 ydat_linux.bash $(INSTALLDIR)/bin-linux/ydat_demo2"; \
			install -m755 ydat_linux.bash $(INSTALLDIR)/bin-linux/ydat_demo2; \
			echo "install -d -m755 $(INSTALLDIR)/bin-windows"; \
			install -d -m755 $(INSTALLDIR)/bin-windows; \
			echo "install -m644 ydat.bat $(INSTALLDIR)/bin-windows/ydat.bat"; \
			install -m644 ydat.bat $(INSTALLDIR)/bin-windows/ydat.bat; \
			echo "install -m644 ydat_demo1.bat $(INSTALLDIR)/bin-windows/ydat_demo1.bat"; \
			install -m644 ydat_demo1.bat $(INSTALLDIR)/bin-windows/ydat_demo1.bat; \
			echo "install -m644 ydat_demo2.bat $(INSTALLDIR)/bin-windows/ydat_demo2.bat"; \
			install -m644 ydat_demo2.bat $(INSTALLDIR)/bin-windows/ydat_demo2.bat; \
		    else echo "Skipping install because installation directory $(INSTALLDIR) doesn't exist"; \
		fi; \
	    else\
		if [ "$(INSTALLDIR_ORIGINAL)" ]; \
		    then echo "Skipping install because installation directory $(INSTALLDIR_ORIGINAL) doesn't exist"; \
		    else echo "Skipping install because no installation directory is set"; \
		fi; \
	fi

