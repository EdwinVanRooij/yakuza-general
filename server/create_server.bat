@echo off
@title MoopleDEV's INI creator
set CLASSPATH=.;..\Source\out\artifacts\JavaProjIntelliJ_jar\JavaProjIntelliJ.jar
java -Xmx100m net.server.CreateINI
pause