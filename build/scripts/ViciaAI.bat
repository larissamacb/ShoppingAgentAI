@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  ViciaAI startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and VICIA_AI_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\ViciaAI.jar;%APP_HOME%\lib\jadex-distribution-standard-4.0.241.jar;%APP_HOME%\lib\gson-2.10.1.jar;%APP_HOME%\lib\jadex-platform-webservice-jetty-4.0.241.jar;%APP_HOME%\lib\jadex-platform-webservice-websocket-4.0.241.jar;%APP_HOME%\lib\jadex-distribution-minimal-4.0.241.jar;%APP_HOME%\lib\jadex-tools-bdi-4.0.241.jar;%APP_HOME%\lib\jadex-tools-bpmn-4.0.241.jar;%APP_HOME%\lib\jadex-tools-runtimetools-swing-4.0.241.jar;%APP_HOME%\lib\jadex-tools-runtimetools-web-4.0.241.jar;%APP_HOME%\lib\jadex-applications-applib-bdi-4.0.241.jar;%APP_HOME%\lib\jadex-tools-comanalyzer-4.0.241.jar;%APP_HOME%\lib\jadex-rules-tools-4.0.241.jar;%APP_HOME%\lib\jadex-environment-agr-4.0.241.jar;%APP_HOME%\lib\jadex-environment-envsupport-4.0.241.jar;%APP_HOME%\lib\jadex-platform-webservice-grizzly-4.0.241.jar;%APP_HOME%\lib\jadex-platform-webservice-4.0.241.jar;%APP_HOME%\lib\jadex-kernel-microservice-4.0.241.jar;%APP_HOME%\lib\jadex-platform-base-4.0.241.jar;%APP_HOME%\lib\jadex-transport-tcp-4.0.241.jar;%APP_HOME%\lib\jadex-transport-relay-4.0.241.jar;%APP_HOME%\lib\jadex-transport-websocket-4.0.241.jar;%APP_HOME%\lib\jadex-transport-base-4.0.241.jar;%APP_HOME%\lib\jadex-kernel-bdiv3-4.0.241.jar;%APP_HOME%\lib\jadex-kernel-bpmn-4.0.241.jar;%APP_HOME%\lib\jadex-kernel-application-4.0.241.jar;%APP_HOME%\lib\jadex-kernel-component-4.0.241.jar;%APP_HOME%\lib\jadex-kernel-micro-4.0.241.jar;%APP_HOME%\lib\jadex-tools-base-swing-4.0.241.jar;%APP_HOME%\lib\bootstrap-3.4.1.jar;%APP_HOME%\lib\jquery-3.5.1.jar;%APP_HOME%\lib\angular-route-1.5.1.jar;%APP_HOME%\lib\angular-1.5.1.jar;%APP_HOME%\lib\jadex-rules-base-4.0.241.jar;%APP_HOME%\lib\jadex-editor-bpmn-4.0.241.jar;%APP_HOME%\lib\jadex-kernel-model-bpmn-4.0.241.jar;%APP_HOME%\lib\jadex-serialization-xml-4.0.241.jar;%APP_HOME%\lib\jadex-util-gui-4.0.241.jar;%APP_HOME%\lib\jadex-kernel-base-4.0.241.jar;%APP_HOME%\lib\jadex-tools-base-4.0.241.jar;%APP_HOME%\lib\jadex-platform-bridge-4.0.241.jar;%APP_HOME%\lib\jadex-rules-eca-4.0.241.jar;%APP_HOME%\lib\jadex-util-javaparser-4.0.241.jar;%APP_HOME%\lib\jadex-serialization-json-4.0.241.jar;%APP_HOME%\lib\jadex-util-security-4.0.241.jar;%APP_HOME%\lib\jadex-util-concurrent-4.0.241.jar;%APP_HOME%\lib\jadex-serialization-binary-4.0.241.jar;%APP_HOME%\lib\jadex-serialization-traverser-4.0.241.jar;%APP_HOME%\lib\jadex-util-bytecode-4.0.241.jar;%APP_HOME%\lib\jadex-util-commons-4.0.241.jar;%APP_HOME%\lib\mail-1.4.5.jar;%APP_HOME%\lib\smackx-3.1.0.jar;%APP_HOME%\lib\smack-3.1.0.jar;%APP_HOME%\lib\jfreechart-1.0.12.jar;%APP_HOME%\lib\jcommon-1.0.15.jar;%APP_HOME%\lib\jung-visualization-2.0.1.jar;%APP_HOME%\lib\jung-graph-impl-2.0.1.jar;%APP_HOME%\lib\jung-algorithms-2.0.1.jar;%APP_HOME%\lib\jung-api-2.0.1.jar;%APP_HOME%\lib\colt-1.2.0.jar;%APP_HOME%\lib\collections-generic-4.01.jar;%APP_HOME%\lib\jersey-container-grizzly2-http-2.11.jar;%APP_HOME%\lib\jersey-server-2.11.jar;%APP_HOME%\lib\jersey-client-2.11.jar;%APP_HOME%\lib\jersey-media-moxy-2.11.jar;%APP_HOME%\lib\jersey-entity-filtering-2.11.jar;%APP_HOME%\lib\javax.ws.rs-api-2.0.jar;%APP_HOME%\lib\javassist-3.12.1.GA.jar;%APP_HOME%\lib\websocket-server-9.4.19.v20190610.jar;%APP_HOME%\lib\jetty-servlet-9.4.19.v20190610.jar;%APP_HOME%\lib\jetty-security-9.4.19.v20190610.jar;%APP_HOME%\lib\jetty-server-9.4.19.v20190610.jar;%APP_HOME%\lib\websocket-servlet-9.4.19.v20190610.jar;%APP_HOME%\lib\javax.servlet-api-3.1.0.jar;%APP_HOME%\lib\jersey-media-multipart-2.11.jar;%APP_HOME%\lib\jersey-common-2.28.jar;%APP_HOME%\lib\jaxws-api-2.3.1.jar;%APP_HOME%\lib\rt-2.3.2.jar;%APP_HOME%\lib\grizzly-http-servlet-2.3.22.jar;%APP_HOME%\lib\grizzly-http-server-multipart-2.3.16.jar;%APP_HOME%\lib\grizzly-http-server-2.3.22.jar;%APP_HOME%\lib\commons-daemon-1.0.10.jar;%APP_HOME%\lib\xz-1.5.jar;%APP_HOME%\lib\nano-cuckoo-1.0.0.jar;%APP_HOME%\lib\lz4-1.3.0.jar;%APP_HOME%\lib\snappy-java-1.1.2.6.jar;%APP_HOME%\lib\eddsa-0.2.0.jar;%APP_HOME%\lib\nanohttpd-websocket-2.3.1.jar;%APP_HOME%\lib\nv-websocket-client-2.3.jar;%APP_HOME%\lib\Java-WebSocket-1.3.5.jar;%APP_HOME%\lib\antlr-runtime-3.1.3.jar;%APP_HOME%\lib\junit-4.11.jar;%APP_HOME%\lib\opentracing-mock-0.33.0.jar;%APP_HOME%\lib\opentracing-util-0.33.0.jar;%APP_HOME%\lib\opentracing-noop-0.33.0.jar;%APP_HOME%\lib\opentracing-api-0.33.0.jar;%APP_HOME%\lib\asm-util-6.2.1.jar;%APP_HOME%\lib\asm-analysis-6.2.1.jar;%APP_HOME%\lib\asm-tree-6.2.1.jar;%APP_HOME%\lib\asm-6.2.1.jar;%APP_HOME%\lib\activation-1.1.jar;%APP_HOME%\lib\jgraphx-3.4.1.3.jar;%APP_HOME%\lib\batik-codec-1.8.jar;%APP_HOME%\lib\xmlgraphics-commons-2.1.jar;%APP_HOME%\lib\asm-all-5.0.3.jar;%APP_HOME%\lib\concurrent-1.3.4.jar;%APP_HOME%\lib\minimal-json-0.9.4.jar;%APP_HOME%\lib\jakarta.ws.rs-api-2.1.5.jar;%APP_HOME%\lib\jakarta.xml.ws-api-2.3.2.jar;%APP_HOME%\lib\jakarta.annotation-api-1.3.4.jar;%APP_HOME%\lib\jakarta.inject-2.5.0.jar;%APP_HOME%\lib\osgi-resource-locator-1.0.1.jar;%APP_HOME%\lib\jaxb-api-2.3.1.jar;%APP_HOME%\lib\javax.xml.soap-api-1.4.0.jar;%APP_HOME%\lib\javax.annotation-api-1.3.2.jar;%APP_HOME%\lib\jaxb-runtime-2.3.2.jar;%APP_HOME%\lib\streambuffer-1.5.7.jar;%APP_HOME%\lib\saaj-impl-1.5.1.jar;%APP_HOME%\lib\stax-ex-1.8.1.jar;%APP_HOME%\lib\jakarta.xml.bind-api-2.3.2.jar;%APP_HOME%\lib\istack-commons-runtime-3.0.8.jar;%APP_HOME%\lib\jakarta.activation-api-1.2.1.jar;%APP_HOME%\lib\policy-2.7.6.jar;%APP_HOME%\lib\gmbal-4.0.0.jar;%APP_HOME%\lib\pfl-tf-tools-4.0.1.jar;%APP_HOME%\lib\pfl-tf-4.0.1.jar;%APP_HOME%\lib\mimepull-1.9.11.jar;%APP_HOME%\lib\woodstox-core-5.1.0.jar;%APP_HOME%\lib\stax2-api-4.1.jar;%APP_HOME%\lib\ha-api-3.1.12.jar;%APP_HOME%\lib\jakarta.xml.soap-api-1.4.1.jar;%APP_HOME%\lib\jakarta.jws-api-1.1.1.jar;%APP_HOME%\lib\javax.websocket-api-1.1.jar;%APP_HOME%\lib\websocket-client-9.4.19.v20190610.jar;%APP_HOME%\lib\jetty-client-9.4.19.v20190610.jar;%APP_HOME%\lib\jetty-http-9.4.19.v20190610.jar;%APP_HOME%\lib\websocket-common-9.4.19.v20190610.jar;%APP_HOME%\lib\jetty-io-9.4.19.v20190610.jar;%APP_HOME%\lib\grizzly-http-2.3.22.jar;%APP_HOME%\lib\grizzly-framework-2.3.22.jar;%APP_HOME%\lib\hk2-locator-2.3.0-b05.jar;%APP_HOME%\lib\javax.inject-2.3.0-b05.jar;%APP_HOME%\lib\hk2-api-2.3.0-b05.jar;%APP_HOME%\lib\org.eclipse.persistence.moxy-2.5.0.jar;%APP_HOME%\lib\org.eclipse.persistence.antlr-2.5.0.jar;%APP_HOME%\lib\bcpkix-jdk15on-1.59.jar;%APP_HOME%\lib\bcprov-jdk15on-1.59.jar;%APP_HOME%\lib\jna-platform-4.5.2.jar;%APP_HOME%\lib\jna-4.5.2.jar;%APP_HOME%\lib\nanohttpd-2.3.1.jar;%APP_HOME%\lib\stringtemplate-3.2.jar;%APP_HOME%\lib\hamcrest-core-1.3.jar;%APP_HOME%\lib\batik-transcoder-1.8.jar;%APP_HOME%\lib\batik-bridge-1.8.jar;%APP_HOME%\lib\batik-script-1.8.jar;%APP_HOME%\lib\batik-anim-1.8.jar;%APP_HOME%\lib\batik-gvt-1.8.jar;%APP_HOME%\lib\batik-svg-dom-1.8.jar;%APP_HOME%\lib\batik-parser-1.8.jar;%APP_HOME%\lib\batik-svggen-1.8.jar;%APP_HOME%\lib\batik-awt-util-1.8.jar;%APP_HOME%\lib\batik-dom-1.8.jar;%APP_HOME%\lib\batik-css-1.8.jar;%APP_HOME%\lib\batik-xml-1.8.jar;%APP_HOME%\lib\batik-util-1.8.jar;%APP_HOME%\lib\commons-io-1.3.1.jar;%APP_HOME%\lib\commons-logging-1.0.4.jar;%APP_HOME%\lib\javax.activation-api-1.2.0.jar;%APP_HOME%\lib\txw2-2.3.2.jar;%APP_HOME%\lib\FastInfoset-1.2.16.jar;%APP_HOME%\lib\management-api-3.2.1.jar;%APP_HOME%\lib\pfl-basic-tools-4.0.1.jar;%APP_HOME%\lib\pfl-dynamic-4.0.1.jar;%APP_HOME%\lib\pfl-basic-4.0.1.jar;%APP_HOME%\lib\pfl-asm-4.0.1.jar;%APP_HOME%\lib\jetty-xml-9.4.19.v20190610.jar;%APP_HOME%\lib\jetty-util-9.4.19.v20190610.jar;%APP_HOME%\lib\websocket-api-9.4.19.v20190610.jar;%APP_HOME%\lib\validation-api-1.1.0.Final.jar;%APP_HOME%\lib\hk2-utils-2.3.0-b05.jar;%APP_HOME%\lib\aopalliance-repackaged-2.3.0-b05.jar;%APP_HOME%\lib\javassist-3.18.1-GA.jar;%APP_HOME%\lib\org.eclipse.persistence.core-2.5.0.jar;%APP_HOME%\lib\antlr-2.7.7.jar;%APP_HOME%\lib\batik-ext-1.8.jar;%APP_HOME%\lib\xalan-2.7.0.jar;%APP_HOME%\lib\xml-apis-ext-1.3.04.jar;%APP_HOME%\lib\javax.inject-1.jar;%APP_HOME%\lib\org.eclipse.persistence.asm-2.5.0.jar


@rem Execute ViciaAI
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %VICIA_AI_OPTS%  -classpath "%CLASSPATH%" com.unieuro.main.Main %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable VICIA_AI_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%VICIA_AI_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
