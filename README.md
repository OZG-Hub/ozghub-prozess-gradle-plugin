# Prozess-Deployment-Gradle-Plugin für den OZG-Hub

## Ziel

Mit diesem Gradle-Plugin können Prozesse und Formulare auf OZG-Hub-Umgebungen deployt und undeployt
werden.

## System-Anforderungen

- Das Plugin ist mit Gradle 7.6.3 getestet. Eventuell sind die Funktionen auch mit niedrigeren
  Gradle-Versionen verfügbar.
- Das verwendete Gradle muss minimal unter Java 17 laufen.

## Verwendung

Das Gradle-Plugin stellt Tasks zur Verfügung. Die Tasks sind in [Tasks.md](Tasks.md) beschrieben.

Die Schnittstellen, die der OZG-Hub für Prozessmodellierer bereitstellt, werden in
[Schnittstellen-Dokumentation](Schnittstellen.md) beschrieben. Die Schnittstellen enthalten sowohl
die Schnittstellen, die vom Gradle-Plugin angesprochen werden, als auch weitere Schnittstellen z.B.
zum Zugriff auf Logdateien.
