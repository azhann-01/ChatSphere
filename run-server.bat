@echo off
setlocal
cd /d "%~dp0"
java -cp "bin;lib\mysql-connector-j-9.7.0.jar" server.ChatServer

