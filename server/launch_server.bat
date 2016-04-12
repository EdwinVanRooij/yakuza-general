echo off
title YakuzaMS Server v83
set CLASSPATH=.;..\Source\out\artifacts\JavaProjIntelliJ_jar\*
java -Xmx4000m -Dwzpath=wz\ net.server.Server
pause