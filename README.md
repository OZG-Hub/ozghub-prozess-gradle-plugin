# Prozess-Deployment-Gradle-Plugin für den OZG-Hub

## Ziel

Mit diesem Gradle-Plugin können Prozesse und Formulare auf OZG-Hub-Umgebungen deployt und undeployt
werden.

## System-Anforderungen

- Das Plugin ist mit Gradle 7.6.3 getestet. Eventuell sind die Funktionen auch mit niedrigeren
  Gradle-Versionen verfügbar.
- Das verwendete Gradle muss minimal unter Java 17 laufen.

## Verwendung

Das Gradle-Plugin stellt Tasks zur Verfügung. Diese sind im [Wiki](https://doku.pmp.seitenbau.com/display/DFO/Tasks+Gradle-PlugIn+OZG-Hub) beschrieben.

Die Schnittstellen, die der OZG-Hub für Prozessmodellierer bereitstellt, werden in der
[OD-Management API Dokumentation](https://doku.pmp.seitenbau.com/x/YAdfAw) beschrieben. Die Dokumentation enthält sowohl
die Schnittstellen, die vom Gradle-Plugin angesprochen werden, als auch weitere Schnittstellen z.B.
zum Zugriff auf Logdateien.
