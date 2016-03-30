%ECHO OFF
%ECHO Starting SCS System
PAUSE
%ECHO Starting Security Control Console
START "SECURITY CONTROLLER CONSOLE" /MIN /NORMAL java SecurityController %1
%ECHO Starting Security sensor Console
START "SECURITY SENSOR CONSOLE" /MIN /NORMAL java SecuritySensor %1
%ECHO SCS Monitoring Console
START "MUSEUM SECURITY CONTROL SYSTEM CONSOLE" /NORMAL java SCSConsole %1
