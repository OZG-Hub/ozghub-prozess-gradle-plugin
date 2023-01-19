# Prozess-Deployment-Gradle-Plugin für den OZG-Hub

## Ziel

Mit diesem Gradle-Plugin können Prozesse und Formulare auf OZG-Hub-Umgebungen deployt und undeployt
werden.

## System-Anforderungen

- Das Plugin ist mit Gradle 7.5.1 getestet. Eventuell sind die Funktionen auch mit niedrigeren
  Gradle-Versionen verfügbar.
- Das verwendete Gradle muss minimal unter Java 11 laufen.

## Verwendung

Das Gradle-Plugin stellt die folgenden Tasks zur Verfügung:

- `deployProcess`: Deployt Prozessdefinitionen
- `deployForms`: Deployt Formulare
- `listProcesses`: Zeigt eine Liste aller deployten Prozesse an
- `listForms`: Zeigt eine Liste aller deployten Formulare an
- `undeployProcess`: Löscht ein Prozess-Deployment
- `undeployForm`: Löscht ein Formular-Deployment
- `encryptParameterValue`: Verschlüsselt einen Prozessparameterwert

Die Tasks sind in [Tasks.md](Tasks.md) genauer beschrieben.

Die Schnittstellen, die der OZG-Hub für Prozessmodellierer bereitstellt, werden in
[Schnittstellen-Dokumentation](Schnittstellen.md) beschrieben. Die Schnittstellen enthalten sowohl
die Schnittstellen, die vom Gradle-Plugin angesprochen werden, als auch weitere Schnittstellen z.B.
zum Zugriff auf Logdateien.
