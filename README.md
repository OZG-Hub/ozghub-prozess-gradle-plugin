# Prozess-Deployment-Gradle-Plugin für den OZG-Hub

## Ziel

Mit diesem Gradle-Plugin können Prozesse und Formulare auf OZG-Hub-Umgebungen deployt und undeployt
werden.

## System-Anforderungen

- Das Plugin ist mit Gradle 6.7 getestet. Eventuell sind die Funktionen auch mit niedrigeren
  Gradle-Versionen verfügbar.
- Das verwendete Gradle muss minimal unter Java 11 laufen

## Verwendung

Das Gradle-Plugin stellt die folgenden Tasks zur Verfügung:

- deployProcess: Deployt Prozessdefinitionen
- deployForms: Deployt Formulare
- listProcesses: Zeigt eine Liste aller deployten Prozesse an
- listForms: Zeigt eine Liste aller deployten Formulare an
- undeployProcess: Löscht ein Prozess-Deployment
- undeployForm: Löscht ein Formular-Deployment
- encryptParameterValue: Verschlüsselt einen Prozessparameterwert

Diese Tasks werden im Folgenden genauer beschrieben.

Alle Tasks benötigen die folgenden Pflichtparameter:

- url: die URL zum Service-Gateway einer OZG-Hub-Umgebung,
  z.B. https://sgw.behoerden-serviceportal.de
- user: Benutzername des Schnittstellenbenutzers
- password: Passwort des Schnittstellenbenutzers

### Task deployProcess

Deployt Prozessdefinitionen auf den OZG-Hub.

Alle Prozessmodelle im Quellordner werden in ein Deployment verpackt und auf den OZG-Hub deployt.
Das bedeutet, dass zusammen deployte Prozessmodelle auch nur zusammen undeployt werden können.
Zusätzlich werden (falls vorhanden) in Dateien enthaltene Metadaten zu den Prozessmodell-Dateien 
übergeben (s.u.).

Zusätzliche Parameter:

- deploymentName: Pflicht. Der Name des Deployments, in dem die Prozessmodelle deployt werden
- files: Optional, default ist build/models. Pfad zum Ordner, in dem die zu deployenden Prozessmodelle
  liegen oder Pfad zu einem Prozessmodell. Die Prozessmodelle müssen Activiti-flavored BPMN 
  Dateien sein und die Dateiendung .bpmn20.xml haben.
- metadataFiles: Optional, default ist metadata. Pfad zum Ordner, in dem sich die Metadaten-Dateien 
  zu den Prozessmodell-Dateien befinden oder Pfad zu einer Metadata-Datei.

#### Metadaten-Dateien

Metadaten-Dateien werden default-mäßig im Unterordner _metadata_ des Projektordners erwartet. Über
den Parameter _metadataFiles_ kann auch ein anderer Ablageort angegeben werden. Die Metadaten-Dateien 
werden im JSON-Format erwartet. Der Name der Metadaten-Datei zu einer Prozessmodelldatei muss dabei 
(nach Entfernen der jeweiligen Extensions) mit dem Namen der Prozessmodell-Datei übereinstimmen. 
Aktuell wird als Metadaten-Attribut ausschließlich das Attribut _servicekontolos_ 
(mit Werten true oder false) unterstützt.

Das Deployment kann auch durchgeführt werden, wenn keine Metadaten-Dateien vorhanden sind. In diesem
Fall wird das Deployment ohne Metadaten durchgeführt und das Attribut _servicekontolos_ erhält den
Wert false.

Der Inhalt der Metadaten-Datei ist ein JSON-Objekt mit der folgenden Struktur:

```json
{
  "servicekontolos": true
}
```

### Task deployForms

Deployt Formulare auf den OZG-Hub.

Für jedes enthaltene Formular wird ein eigenes Deployment erzeugt, d.h. die Formulare können einzeln
undeployt werden.

Zusätzliche Parameter:

- files: Optional, default ist forms. Pfaf zum Ordner, in dem die zu deployenden Formulare liegen 
  oder Pfad zu einer Formular-Datei. Wird ein Ordner angegeben, werden auch die Unterordner dieses 
  Ordners durchsucht. Die Formulare müssen Serviceportal-Formulare sein und müssen die Dateiendung 
  .json haben.

### Task listProcesses

Zeigt eine Liste aller deployten Prozesse an.

Ausgegeben wird eine Liste aller vorhandenen Prozessdeployments. Für jedes Prozessdeployment wird in
einer Zeile der Deployment-Zeitpunkt, der Name des Deployments und die Deployment-Id ausgegegeben.
Direkt unter der Deployment-Zeile folgen in weiteren Zeilen, die jeweils mit einem Spiegelstrich
beginnen, die Keys und die Namen der enthaltenen Prozessmodelle. Die nächste Zeile ohne
Spiegelstrich enthält dann das nächste Deployment.

### Task listForms

Zeigt eine Liste aller deployten Formulare an.

In jeder Zeile wird ein deploytes Formular ausgegeben, und zwar hintereinander der Key des
Formulars, die Sprache des Formulars (de,en,fr), die Deployment-Id und das Deployment-Datum.

### Task undeployProcess

Löscht ein Prozess-Deployment. Dadurch werden alle enthaltenen Prozessdefinitionen und die Instanzen
dieser Prozessdefinitionen gelöscht.

Zusätzliche Parameter:

- deploymentId : Pflicht, die Id des zu löschenden Deployments.
- deleteProcessInstances : Optional, default ist false. Muss auf true gesetzt werden, um Deployments
  zu löschen, bei deren Undeployment auch Prozessinstanzen gelöscht werden würden.

### Task undeployForm

Undeployt ein Formular.

Es wird nicht geprüft, ob das Formular noch von Prozessen benutzt wird.

Zusätzliche Parameter:

- deploymentId : Pflicht, die Id des zu löschenden Formular-Deployments

### Task encryptParameterValue

Verschlüsselt einen Prozessparameterwert.

Zusätzliche Parameter:
- processKey : Pflicht, der Prozess-Schlüssel des Prozesses, für den der Prozessparameterwert eingesetzt werden soll.
- parameterValue : Pflicht, der zu verschlüsselnde Prozessparameterwert.

## (weitere) Schnittstellen des OZG-Hubs

Die Schnittstellen, die der OZG-Hub für Prozessmodellierer bereitstellt, werden in
[Schnittstellen-Dokumentation](Schnittstellen.md) beschrieben. Die Schnittstellen enthalten sowohl
die Schnittstellen, die vom Gradle-Plugin angesprochen werden, als auch weitere Schnittstellen z.B.
zum Zugriff auf Logdateien.

## Referenzen

- [Schnittstellen-Dokumentation](Schnittstellen.md)
- [Task-Dokumentation](Tasks.md)
- [Change-Log](CHANGELOG.md)
