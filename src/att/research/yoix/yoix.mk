#
# Yoix makefile
#

ROOT = ../../..
MAKEFILE = $(firstword $(MAKEFILE_LIST))

include $(ROOT)/make/common.mk

CLASSPATH = $(ROOT)

#
# YOIX_PACKAGES names the additional packages that will be included in the Yoix
# jar file. The list doesn't include everything that's available in the source
# (e.g., j3d).
#

YOIX_PACKAGES = ydat ychart jvma apple

#
# The Yoix parser should be last.
#

PARSERS = \
	DOTParser.jjt \
	PatternParser.jjt \
	XMLParser.jjt \
	YoixParser.jjt

SOURCE = \
	Yoix.java \
	YoixAPI.java \
	YoixAPIProtected.java \
	YoixAWTCanvas.java \
	YoixAWTCheckbox.java \
	YoixAWTCheckboxMenuItem.java \
	YoixAWTCheckboxMenuItemGroup.java \
	YoixAWTDialog.java \
	YoixAWTErrorDialog.java \
	YoixAWTFileDialog.java \
	YoixAWTFontMetrics.java \
	YoixAWTFrame.java \
	YoixAWTGridLayout.java \
	YoixAWTInvocationEvent.java \
	YoixAWTList.java \
	YoixAWTOwnerFrame.java \
	YoixAWTPanel.java \
	YoixAWTTableColumn.java \
	YoixAWTTableManager.java \
	YoixAWTTextArea.java \
	YoixAWTTextCanvas.java \
	YoixAWTTextComponent.java \
	YoixAWTTextField.java \
	YoixAWTTextTerm.java \
	YoixAWTTexturePaint.java \
	YoixAWTToolkit.java \
	YoixAWTWindow.java \
	YoixBinder.java \
	YoixBodyArray.java \
	YoixBodyAudioClip.java \
	YoixBodyBlock.java \
	YoixBodyBuiltin.java \
	YoixBodyCalendar.java \
	YoixBodyCertificate.java \
	YoixBodyCipher.java \
	YoixBodyClipboard.java \
	YoixBodyCompiler.java \
	YoixBodyComponent.java \
	YoixBodyComponentAWT.java \
	YoixBodyComponentSwing.java \
	YoixBodyCookieManager.java \
	YoixBodyControl.java \
	YoixBodyDatagramSocket.java \
	YoixBodyDictionary.java \
	YoixBodyDictionaryObject.java \
	YoixBodyDictionaryThis.java \
	YoixBodyElement.java \
	YoixBodyFont.java \
	YoixBodyFunction.java \
	YoixBodyGraphObserver.java \
	YoixBodyGraphics.java \
	YoixBodyHashtable.java \
	YoixBodyImage.java \
	YoixBodyJump.java \
	YoixBodyKey.java \
	YoixBodyKeyStore.java \
	YoixBodyLocale.java \
	YoixBodyMatrix.java \
	YoixBodyMulticastSocket.java \
	YoixBodyNull.java \
	YoixBodyNumber.java \
	YoixBodyOption.java \
	YoixBodyParseTree.java \
	YoixBodyPath.java \
	YoixBodyProcess.java \
	YoixBodyRandom.java \
	YoixBodyRegexp.java \
	YoixBodyScreen.java \
	YoixBodySecurityManager.java \
	YoixBodyServerSocket.java \
	YoixBodySocket.java \
	YoixBodyStream.java \
	YoixBodyString.java \
	YoixBodySubexp.java \
	YoixBodyTag.java \
	YoixBodyThread.java \
	YoixBodyTimeZone.java \
	YoixBodyTransferHandler.java \
	YoixBodyUIManager.java \
	YoixBodyVector.java \
	YoixBodyZipEntry.java \
	YoixChecksum.java \
	YoixClassLoader.java \
	YoixCoderInputStream.java \
	YoixCoderOutputStream.java \
	YoixCompiler.java \
	YoixCompilerConstants.java \
	YoixCompilerSupport.java \
	YoixCompilerSyncLocals.java \
	YoixConstants.java \
	YoixConstantsAWT.java \
	YoixConstantsError.java \
	YoixConstantsErrorName.java \
	YoixConstantsGraph.java \
	YoixConstantsGraphics.java \
	YoixConstantsImage.java \
	YoixConstantsJFC.java \
	YoixConstantsJTree.java \
	YoixConstantsJTable.java \
	YoixConstantsStream.java \
	YoixConstantsSwing.java \
	YoixConstantsXColor.java \
	YoixConverter.java \
	YoixConverterInput.java \
	YoixConverterOutput.java \
	YoixDataInputStream.java \
	YoixDataOutputStream.java \
	YoixDataTransfer.java \
	YoixDragManager.java \
	YoixError.java \
	YoixFontType0.java \
	YoixGraphBase.java \
	YoixGraphElement.java \
	YoixImageObserver.java \
	YoixInterfaceBody.java \
	YoixInterfaceCallable.java \
	YoixInterfaceCloneable.java \
	YoixInterfaceDnD.java \
	YoixInterfaceDragable.java \
	YoixInterfaceDrawable.java \
	YoixInterfaceFileChooser.java \
	YoixInterfaceFont.java \
	YoixInterfaceKillable.java \
	YoixInterfaceListener.java \
	YoixInterfaceMenuBar.java \
	YoixInterfacePointer.java \
	YoixInterfaceShowing.java \
	YoixInterfaceSortable.java \
	YoixInterfaceWindow.java \
	YoixInterpreter.java \
	YoixInterruptable.java \
	YoixMake.java \
	YoixMakeEvent.java \
	YoixMakeScreen.java \
	YoixMain.java \
	YoixMisc.java \
	YoixMiscCtype.java \
	YoixMiscGeom.java \
	YoixMiscGraph.java \
	YoixMiscGraphics.java \
	YoixMiscJFC.java \
	YoixMiscMenu.java \
	YoixMiscPrintf.java \
	YoixMiscQsort.java \
	YoixMiscScanf.java \
	YoixMiscSSL.java \
	YoixMiscTime.java \
	YoixMiscXML.java \
	YoixModule.java \
	YoixModuleAWT.java \
	YoixModuleBorder.java \
	YoixModuleCalendar.java \
	YoixModuleCtype.java \
	YoixModuleDate.java \
	YoixModuleError.java \
	YoixModuleEvent.java \
	YoixModuleFactorial.java \
	YoixModuleGraph.java \
	YoixModuleGraphics.java \
	YoixModuleImage.java \
	YoixModuleIO.java \
	YoixModuleJFC.java \
	YoixModuleLayout.java \
	YoixModuleLocale.java \
	YoixModuleMath.java \
	YoixModuleMisc.java \
	YoixModuleNet.java \
	YoixModuleParser.java \
	YoixModuleRE.java \
	YoixModuleRobot.java \
	YoixModuleSecure.java \
	YoixModuleSound.java \
	YoixModuleStdio.java \
	YoixModuleStream.java \
	YoixModuleString.java \
	YoixModuleSwing.java \
	YoixModuleSwingExtension.java \
	YoixModuleSystem.java \
	YoixModuleThread.java \
	YoixModuleType.java \
	YoixModuleUtil.java \
	YoixModuleVM.java \
	YoixModuleWindows.java \
	YoixModuleXColor.java \
	YoixObject.java \
	YoixOption.java \
	YoixParserBvalue.java \
	YoixParserStream.java \
	YoixPointer.java \
	YoixPointerActive.java \
	YoixReflect.java \
	YoixRERegexp.java \
	YoixRESubexp.java \
	YoixRegistryColor.java \
	YoixRegistryCursor.java \
	YoixSecurityManager.java \
	YoixSecurityOptions.java \
	YoixSimpleDateFormat.java \
	YoixSplashScreen.java \
	YoixStack.java \
	YoixSwingDefaultCaret.java \
	YoixSwingHighlighter.java \
	YoixSwingJCanvas.java \
	YoixSwingJColorChooser.java \
	YoixSwingJComboBox.java \
	YoixSwingJDesktopPane.java \
	YoixSwingJDialog.java \
	YoixSwingJFileChooser.java \
	YoixSwingJFileDialog.java \
	YoixSwingJFrame.java \
	YoixSwingJInternalFrame.java \
	YoixSwingJLayeredPane.java \
	YoixSwingJList.java \
	YoixSwingJPanel.java \
	YoixSwingJScrollBar.java \
	YoixSwingJScrollPane.java \
	YoixSwingJSlider.java \
	YoixSwingJSplitPane.java \
	YoixSwingJTabbedPane.java \
	YoixSwingJTable.java \
	YoixSwingJTextArea.java \
	YoixSwingJTextCanvas.java \
	YoixSwingJTextComponent.java \
	YoixSwingJTextField.java \
	YoixSwingJTextPane.java \
	YoixSwingJTextTerm.java \
	YoixSwingJTree.java \
	YoixSwingJWindow.java \
	YoixSwingLabelItem.java \
	YoixSwingTableColumn.java \
	YoixSwingTransferHandler.java \
	YoixThread.java \
	YoixTipManager.java \
	YoixTrustPolicy.java \
	YoixUtilHashtable.java \
	YoixUtilVector.java \
	YoixVM.java \
	YoixVMCleaner.java \
	YoixVMClipboard.java \
	YoixVMDisposer.java \
	YoixVMError.java \
	YoixVMShutdownThread.java \
	YoixVMThread.java \
	YoixVMThreadData.java \
	SimpleNode.java \
	PatternInterpreter.java

SUPPORT = \
	ExampleModule.java \
	YoixInstaller.java \
	yoix_linux.bash \
	yoix.bat

all clean clobber install :
	@#
	@# Made lots of changes to this file. Not certain if we need to explicitly
	@# export variables - seems to work as is, but probably needs testing.
	@#
	@echo "++++ Making $@ for the yoix interpreter ++++"
	@$(MAKE) -e -f $(MAKEFILE) local_$@
	@for PACKAGE in $(YOIX_PACKAGES); do \
	    echo "++++ Making $@ for the $$PACKAGE package ++++"; \
	    (cd $$PACKAGE && $(MAKE) -e -f $${PACKAGE}.mk $@) || exit 1; \
	done

run : all
	@#
	@# During development we often type something like
	@#
	@#     make -f yoix.mk RUNARGS="Tests/components3.yx" run
	@#
	@# to run the interpreter using the source code in this directory. Bottom
	@# line is everything assigned to RUNARGS is passed to the interpreter.
	@#
	CLASSPATH=$(CLASSPATH) $(JAVA) $(JAVAFLAGS) att.research.yoix.YoixMain $(RUNARGS)

#
# Local (i.e., for this directory) versions of some of the standard targets.
#

local_all : $(PARSERS:.jjt=.java) $(PARSERS:.jjt=.class) $(SOURCE:.java=.class)
	@:

local_clean :
	rm -f *.class

local_clobber : local_clean
	rm -f *.jar
	@#
	@# Quietly remove files that were automatically generated by javacc and
	@# jjtree. This list may need to be periodically updated.
	@#
	@rm -f Node.java ParseException.java Token.java TokenMgrError.java
	@rm -f YoixParser.java YoixParser*Constants.java YoixParserTokenManager.java
	@rm -f JJTYoixParser*.java YoixParser*.jj
	@rm -f *DOTParser*.java DOTParser*.jj
	@rm -f *XMLParser*.java XMLParser*.jj
	@rm -f *PatternParser*.java PatternParser*.jj
	@rm -f ASCII_CharStream.java ASCII_UCodeESC_CharStream.java CharStream.java

local_install : yoix.jar yoix_linux.bash yoix.bat
	@#
	@# Recent addition that can be used to install yoix.jar and scripts that
	@# run the interpreter on several operating systems (e.g., Linux, Windows).
	@# Nothing is installed if INSTALLDIR is empty or doesn't exist.
	@#
	@if [ "$(INSTALLDIR)" ]; \
	    then \
		if [ -d "$(INSTALLDIR)" ]; \
		    then \
			echo "install -d -m755 $(INSTALLDIR)/lib"; \
			install -d -m755 $(INSTALLDIR)/lib; \
			echo "install -m644 yoix.jar $(INSTALLDIR)/lib"; \
			install -m644 yoix.jar $(INSTALLDIR)/lib; \
			echo "install -d -m755 $(INSTALLDIR)/bin-linux"; \
			install -d -m755 $(INSTALLDIR)/bin-linux; \
			echo "install -m755 yoix_linux.bash $(INSTALLDIR)/bin-linux/yoix"; \
			install -m755 yoix_linux.bash $(INSTALLDIR)/bin-linux/yoix; \
			echo "install -d -m755 $(INSTALLDIR)/bin-windows"; \
			install -d -m755 $(INSTALLDIR)/bin-windows; \
			echo "install -m644 yoix.bat $(INSTALLDIR)/bin-windows/yoix.bat"; \
			install -m644 yoix.bat $(INSTALLDIR)/bin-windows/yoix.bat; \
		    else echo "Skipping install because installation directory $(INSTALLDIR) doesn't exist"; \
		fi; \
	    else \
		if [ "$(INSTALLDIR_ORIGINAL)" ]; \
		    then echo "Skipping install because installation directory $(INSTALLDIR_ORIGINAL) doesn't exist"; \
		    else echo "Skipping install because no installation directory is set"; \
		fi; \
	fi

#
# Class files from all the supported packages end up in the yoix.jar file. It's
# just not worth the effort to track them all here, so we just rebuild yoix.jar
# whenever this rule is triggered. Ugly, but good enough for now.
#

yoix.jar : all
	@rm -f $@
	cd $(ROOT); $(JAR) cvfm att/research/yoix/$@ att/research/yoix/yoix.mf att/research/yoix/*.class
	@for DIR in $(YOIX_PACKAGES); do \
	    (cd $(ROOT) && $(JAR) vfu att/research/yoix/$@ att/research/yoix/$$DIR/*.class) || exit 1; \
	done
	cd $(ROOT); $(JAR) vfu att/research/yoix/$@ att/research/yoix/resources/system/*.yx
	$(JAR) i $@

