@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Apache Maven Wrapper startup batch script, version 3.2.0

@IF "%MAVEN_BATCH_ECHO%"=="on"  ECHO %MAVEN_BATCH_ECHO%
@IF "%MAVEN_BATCH_PAUSE%"=="on" PAUSE

@IF "%MAVEN_OPTS%"=="" SET MAVEN_OPTS="-Xmx512m"

@IF NOT "%MAVEN_SKIP_RC%"=="" GOTO skipRcPre
@IF NOT EXIST "%USERPROFILE%\.mavenrc_pre.bat" GOTO skipRcPre
CALL "%USERPROFILE%\.mavenrc_pre.bat"
:skipRcPre

SET ERROR_CODE=0

@SETLOCAL

@IF NOT "%JAVA_HOME%"=="" GOTO OkJHome
FOR /F "tokens=*" %%i IN ('where java.exe 2^>NUL') DO (SET JAVACMD=%%i & GOTO checkJCmd)
GOTO checkJCmd

:OkJHome
SET JAVACMD=%JAVA_HOME%\bin\java.exe

:checkJCmd
IF NOT EXIST "%JAVACMD%" (
    ECHO Error: JAVA_HOME is not defined correctly. >&2
    ECHO Cannot execute %JAVACMD% >&2
    SET ERROR_CODE=1
    GOTO end
)

SET MAVEN_PROJECTBASEDIR=%~dp0
:searchLoop
IF EXIST "%MAVEN_PROJECTBASEDIR%\.mvn" GOTO foundMavenBasedir
IF "%MAVEN_PROJECTBASEDIR%"=="%MAVEN_PROJECTBASEDIR:~0,3%" GOTO baseDirFound
SET "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"
FOR %%i IN ("%MAVEN_PROJECTBASEDIR%") DO SET "MAVEN_PROJECTBASEDIR=%%~dpi"
GOTO searchLoop
:foundMavenBasedir
:baseDirFound

SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
SET WRAPPER_PROPERTIES="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"

IF EXIST %WRAPPER_JAR% GOTO executeWrapper

SET "WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
FOR /F "usebackq tokens=1,2 delims==" %%a IN (%WRAPPER_PROPERTIES%) DO (
    IF "%%a"=="wrapperUrl" SET "WRAPPER_URL=%%b"
)
ECHO Downloading Maven Wrapper from: %WRAPPER_URL%
curl.exe -fsSL -o %WRAPPER_JAR% "%WRAPPER_URL%"
IF "%MVNW_VERBOSE%"=="true" ECHO Finished downloading %WRAPPER_JAR%

:executeWrapper
@SET MAVEN_JAVA_EXE="%JAVACMD%"
SET /P MAVEN_CONFIG=<"%MAVEN_PROJECTBASEDIR%.mvn\jvm.config" 2>NUL
SET MAVEN_OPTS=%MAVEN_CONFIG% %MAVEN_OPTS%

%MAVEN_JAVA_EXE% %MAVEN_OPTS% ^
  -classpath %WRAPPER_JAR% ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  org.apache.maven.wrapper.MavenWrapperMain %MAVEN_CONFIG% %*

SET ERROR_CODE=%ERRORLEVEL%

:end
@ENDLOCAL & SET ERROR_CODE=%ERROR_CODE%

IF "%MAVEN_BATCH_PAUSE%"=="on" PAUSE

IF "%MAVEN_BATCH_ECHO%"=="on" ECHO %MAVEN_BATCH_ECHO%

EXIT /B %ERROR_CODE%
