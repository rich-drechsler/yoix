@echo off
rem
rem The interpreter's startup behavior can be controlled by property
rem files and command line options. The name of the default property
rem file is yoix.properties, and the Yoix interpreter always looks
rem for them in the three directories that Java associates with the
rem "yoix.home", "user.home", and "user.dir" system properties. 
rem
rem Command line options are always processed after default property
rem files are loaded. The list of officially supported options can be
rem printed on standard output using the -?, --help, or --info options.
rem
rem
rem You may have to edit pathnames and point directly at an appropriate
rem version of Java before this script will work.
rem

cd "%~f0\..\.."
set YCLASSPATH=%CD%\lib\yoix.jar;%CLASSPATH%
java -classpath "%YCLASSPATH%" -Xmx1000m -Xms256m -Dyoix.home="%CD%" att.research.yoix.YoixMain -m1.00 "%CD%\ychart\scripts\ychart.yx" %1 %2 %3 %4 %5 %6 %7 %8 %9
