@echo off
setlocal
cd /d "%~dp0"
if exist gui\*.class del /q gui\*.class
if exist server\*.class del /q server\*.class
if exist main\*.class del /q main\*.class
if exist database\*.class del /q database\*.class
javac -cp "lib\mysql-connector-j-9.7.0.jar;bin" -d bin src\config\AppConfig.java src\database\PasswordUtil.java src\database\DBConnection.java src\gui\LoginFrame.java src\gui\ChatFrame.java src\server\ChatServer.java src\main\Main.java src\main\TestDB.java src\client\ChatClient.java
if errorlevel 1 exit /b %errorlevel%
echo Build successful.
