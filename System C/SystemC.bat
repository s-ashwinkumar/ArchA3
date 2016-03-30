%ECHO OFF
%ECHO Starting ECS System
PAUSE
%ECHO Starting Temperature Controller Console
START "TEMPERATURE CONTROLLER CONSOLE" /MIN /NORMAL java TemperatureController %1
%ECHO Starting Humidity Sensor Console
START "HUMIDITY CONTROLLER CONSOLE" /MIN /NORMAL java HumidityController %1
START "TEMPERATURE SENSOR CONSOLE" /MIN /NORMAL java TemperatureSensor %1
%ECHO Starting Humidity Sensor Console
START "HUMIDITY SENSOR CONSOLE" /MIN /NORMAL java HumiditySensor %1
%ECHO ECS Monitoring Console
START "MUSEUM ENVIRONMENTAL CONTROL SYSTEM CONSOLE" /NORMAL java ECSConsole %1

%ECHO Starting SCS System

%ECHO Starting Security Control Console
START "SECURITY CONTROLLER CONSOLE" /MIN /NORMAL java SecurityController %1
%ECHO Starting Security sensor Console
START "SECURITY SENSOR CONSOLE" /MIN /NORMAL java SecuritySensor %1
%ECHO SCS Monitoring Console
START "MUSEUM SECURITY CONTROL SYSTEM CONSOLE" /NORMAL java SCSConsole %1
%ECHO Starting Sprinkler Control Console
START "SECURITY CONTROLLER CONSOLE" /MIN /NORMAL java SprinklerController %1
%ECHO Starting Fire Alarm Control Console
START "SECURITY CONTROLLER CONSOLE" /MIN /NORMAL java FireAlarmController %1
%ECHO Starting Fire Alarm Sensor Console
START "SECURITY CONTROLLER CONSOLE" /MIN /NORMAL java FireSensor %1

%ECHO Starting Maintenance Console System

%ECHO Starting Maintenance Console
START "MAINTENANCE CONSOLE" /MIN /NORMAL java MaintenanceConsole %1
