%ECHO OFF
%ECHO Starting SCS System
PAUSE

%ECHO SCS Monitoring Console
START "MUSEUM SECURITY CONTROL SYSTEM CONSOLE" /NORMAL java SCSConsole %1
%ECHO Starting Sprinkler Control Console
START "SECURITY CONTROLLER CONSOLE" /MIN /NORMAL java SprinklerController %1
%ECHO Starting Fire Alarm Control Console
START "SECURITY CONTROLLER CONSOLE" /MIN /NORMAL java FireAlarmController %1
%ECHO Starting Fire Alarm Sensor Console
START "SECURITY CONTROLLER CONSOLE" /MIN /NORMAL java FireSensor %1

