@echo off

chcp 65001
javac .\ua\nure\vovk\task3\server\Main.java
javac .\ua\nure\vovk\task3\server\commands\*.java
java ua.nure.vovk.task3.server.Main

call clean.bat