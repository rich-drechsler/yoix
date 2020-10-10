@echo off

rem
rem This is a batch file that may help if you're stuck on a Windows PC
rem with our source package. You need to install Sun's Java Development
rem Kit for Windows and WebGain's JavaCC Parser Generator before this
rem batch file will run. JDK is available at www.javasoft.com (get the
rem latest 1.X release), while JavaCC (Version 1.2) can be downloaded
rem from www.webgain.com. Both packages are free and easy to install.
rem Once they're on your system update our PATH definition (right after
rem the comments) and that should be it.
rem
rem Typing,
rem
rem	make all
rem
rem builds class files from our source package, while
rem
rem	make clean
rem
rem removes class files and any intermediate files created by jjtree and
rem javacc. Type
rem
rem	make yoix.jar
rem
rem to build a jar file.
rem 

set OPATH=%PATH%
set PATH=\JavaCC\bin;\JavaSoft\JDK117B\bin;%PATH%

set CLASSPATH=..\..\..

if "%1"=="" goto ALL
if "%1"=="all" goto ALL
if "%1"=="clean" goto CLEAN
if "%1"=="clobber" goto CLOBBER
if "%1"=="yoix.jar" goto JAR
goto DONE

:ALL
	echo 	jjtree XMLParser.jjt
	call jjtree XMLParser.jjt

	echo 	javacc XMLParser.jj
	call javacc XMLParser.jj

	echo 	jjtree YoixParser.jjt
	call jjtree YoixParser.jjt

	echo 	javacc YoixParser.jj
	call javacc YoixParser.jj

	echo 	javac XMLParser.java
	call javac XMLParser.java

	echo 	javac YoixMain.java
	call javac YoixMain.java

	echo 	javac YoixMiscCtype.java
	call javac YoixMiscCtype.java

	echo 	javac YoixMiscPrintf.java
	call javac YoixMiscPrintf.java

	echo 	javac YoixMiscScanf.java
	call javac YoixMiscScanf.java

	echo 	javac YoixModuleAWT.java
	call javac YoixModuleAWT.java

	echo 	javac YoixModuleCalendar.java
	call javac YoixModuleCalendar.java

	echo 	javac YoixModuleCtype.java
	call javac YoixModuleCtype.java

	echo 	javac YoixModuleDate.java
	call javac YoixModuleDate.java

	echo 	javac YoixModuleError.java
	call javac YoixModuleError.java

	echo 	javac YoixModuleGraph.java
	call javac YoixModuleGraph.java

	echo 	javac YoixModuleGraphics.java
	call javac YoixModuleGraphics.java

	echo 	javac YoixModuleImage.java
	call javac YoixModuleImage.java

	echo 	javac YoixModuleIO.java
	call javac YoixModuleIO.java

	echo 	javac YoixModuleLocale.java
	call javac YoixModuleLocale.java

	echo 	javac YoixModuleMath.java
	call javac YoixModuleMath.java

	echo 	javac YoixModuleMisc.java
	call javac YoixModuleMisc.java

	echo 	javac YoixModuleNet.java
	call javac YoixModuleNet.java

	echo 	javac YoixModuleParser.java
	call javac YoixModuleParser.java

	echo 	javac YoixModuleRE.java
	call javac YoixModuleRE.java

	echo 	javac YoixModuleSound.java
	call javac YoixModuleSound.java

	echo 	javac YoixModuleStdio.java
	call javac YoixModuleStdio.java

	echo 	javac YoixModuleStream.java
	call javac YoixModuleStream.java

	echo 	javac YoixModuleString.java
	call javac YoixModuleString.java

	echo 	javac YoixModuleSystem.java
	call javac YoixModuleSystem.java

	echo 	javac YoixModuleThread.java
	call javac YoixModuleThread.java

	echo 	javac YoixModuleType.java
	call javac YoixModuleType.java

	echo 	javac YoixModuleUtil.java
	call javac YoixModuleUtil.java

	echo 	javac YoixModuleWindows.java
	call javac YoixModuleWindows.java

	echo 	javac YoixPublic.java
	call javac YoixPublic.java

	echo 	javac YoixPublicConstants.java
	call javac YoixPublicConstants.java

	goto DONE

:CLEAN
	echo dummy file >dummy.class

	echo 	del *.class
	del *.class

	echo 	del JJTYoixParserState.java
	if exist JJTYoixParserState.java del JJTYoixParserState.java

	echo 	del YoixParser.jj
	if exist YoixParser.jj del YoixParser.jj

	echo 	del YoixParser.java
	if exist YoixParser.java del YoixParser.java

	echo 	del YoixParserConstants.java
	if exist YoixParserConstants.java del YoixParserConstants.java

	echo 	del YoixParserTokenManager.java
	if exist YoixParserTokenManager.java del YoixParserTokenManager.java

	echo 	del YoixParserTreeConstants.java
	if exist YoixParserTreeConstants.java del YoixParserTreeConstants.java

	echo 	del JJTXMLParserState.java
	if exist JJTXMLParserState.java del JJTXMLParserState.java

	echo 	del XMLParser.jj
	if exist XMLParser.jj del XMLParser.jj

	echo 	del XMLParser.java
	if exist XMLParser.java del XMLParser.java

	echo 	del XMLParserConstants.java
	if exist XMLParserConstants.java del XMLParserConstants.java

	echo 	del XMLParserTokenManager.java
	if exist XMLParserTokenManager.java del XMLParserTokenManager.java

	echo 	del XMLParserTreeConstants.java
	if exist XMLParserTreeConstants.java del XMLParserTreeConstants.java

	echo 	del Node.java
	if exist Node.java del Node.java

	echo 	del ParseException.java
	if exist ParseException.java del ParseException.java

	echo 	del Token.java
	if exist Token.java del Token.java

	echo 	del TokenMgrError.java
	if exist TokenMgrError.java del TokenMgrError.java

	goto DONE

:CLOBBER
	echo 	del yoix.jar
	if exist yoix.jar del yoix.jar

	goto CLEAN

:JAR
	if not exist YoixMain.class call %0

	cd ..\..\..
	echo 	jar cvf att/research/yoix/yoix.jar att/research/yoix/*.class
	jar cvf att/research/yoix/yoix.jar att/research/yoix/*.class
	cd att\research\yoix

	goto DONE

:DONE
	set PATH=%OPATH%
