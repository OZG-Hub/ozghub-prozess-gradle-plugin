# Übersicht

Dieses Dokument beschreibt die verfügbaren Tasks dieses Gradle-Plugins.

## Allgemein

Parameter der Tasks werden als Kommandozeilen-Parameter übergeben.<br />
Die Reihenfolge der Parameter spielt dabei keine Rolle.

Die folgenden Parameter werden von allen Tasks verpflichtend benötigt.

| **Name** | **Beschreibung**                   | **Beispiel**                           |
|----------|------------------------------------|----------------------------------------|
| url      | URL zu einer OZG-Hub-Umgebung      | https://sgw.behoerden-serviceportal.de |
| user     | Benutzername zur Authentifizierung |                                        |
| password | Password zur Authentifizierung     |                                        |

Die Tasks funktionieren nur, wenn der Mandant des Prozessmodells / Formulars dem angegebenen
Benutzer entspricht.

## Task-Dokumentation

### Task zum Deployen eines Prozessmodells

#### Allgemein

Der Task `deployProcess` ermöglicht das Deployen von Prozessmodell-Dateien auf eine spezifische
Prozess-Engine.<br />
Alle Prozessmodelle im Quellordner werden in ein Deployment verpackt und auf den OZG-Hub deployt.
Das bedeutet, dass zusammen deployte Prozessmodelle auch nur zusammen undeployt werden können.
Zusätzlich werden (falls vorhanden) in Dateien enthaltene Metadaten zu den Prozessmodell-Dateien
übergeben (s.u.).
Ist keine Engine angegeben, wird auf die Standard-Prozess-Engine deployt.

#### Parameter

| **Name**                   | **Pflicht** | **Default-Wert** | **Beschreibung**                                                                                                                                                                                  |
|----------------------------|-------------|------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| deploymentName             | Ja          |                  | Name des Deployments, in dem die Prozessmodelle deployt werden.                                                                                                                                   |
| versionName                | Ja          |                  | Name der Version, die mit diesem Deployment verknüpft werden soll.                                                                                                                                |
| files                      | Nein        | /build/models    | Ordner aus dem Prozessmodelle gelesen werden. Es werden auch Unterordner berücksichtigt. Ein relativer Pfad ist relativ zum Projektordner.                                                        |
| metadataFiles              | Nein        | /metadata        | Ordner aus dem Metadaten-Dateien gelesen werden. Es werden auch Unterordner berücksichtigt.                                                                                                       |
| duplicateProcesskeyAction  | Nein        | `ERROR`          | Spezifikation, wie mit bereits deployten Prozess-Keys umgegangen werden soll.                                                                                                                     |
| engine                     | Nein        | `null`           | ID der Prozess-Engine, auf welche deployt werden soll. Bei `null` wird die Standard-Engine der Umgebung verwendet.                                                                                |
| undeploymentMessageSubject | Nein        | `null`           | Betreff der Nachricht die beim Undeployment des Prozesses verschickt wird.                                                                                                                        |
| undeploymentMessageBody    | Nein        | `null`           | Inhalt der Nachricht die beim Undeployment des Prozesses verschickt wird. <br/><br/> Hier können folgende Platzhalter verwendet werden: <ul><li>**{{name}}** : Der Name des Empfängers</li></ul>  |

Der Parameter `duplicateProcesskeyAction` definiert, was geschehen soll, wenn vor dem Deployment der
gegebenen Prozessmodell-Dateien festgestellt wird, dass mindestens ein Prozess-Key bereits Teil
eines Deployments ist. Die folgenden Optionen sind möglich:

- `IGNORE`: Ignorieren. Das Deployment wird fortgesetzt
- `UNDEPLOY`: Deployments werden zuvor gelöscht (Undeployen der entsprechenden Prozessdefinitionen)
- `ERROR`: Der Aufruf der Schnittstelle wird mit einer Fehlermeldung abgebrochen

#### Metadaten-Dateien

Die Metadaten-Dateien sind optional und werden im JSON-Format erwartet.
Der Name der Metadaten-Datei zu einer Prozessmodelldatei muss dabei
(nach Entfernen der jeweiligen Erweiterungen) mit dem Namen der Prozessmodelldatei übereinstimmen.
Zum Prozessmodell _test.bpmn20.xml_ gehört folglich die Metadaten-Datei _test.json_.
Als Metadatenattribute werden derzeit die Attribute _servicekontolos_ (true oder false) 
und _authenticationTypes_ (String Array: BUND_ID und MUK) unterstützt.
Das Deployment kann auch durchgeführt werden, wenn keine Metadaten-Dateien vorhanden sind. In diesem
Fall wird das Deployment ohne Metadaten durchgeführt und das Attribut _servicekontolos_ erhält den
Wert false und _authenticationTypes_ den Wert BUND_ID.

Der Inhalt der Metadaten-Datei ist ein JSON-Objekt mit der folgenden Struktur:

```json
{
  "servicekontolos": true,
  "authenticationTypes": [
    "BUND_ID",
    "MUK"
  ]
}
```

---------------------------------------------------------------------------------------------------

### Task zum Deployen von Formularen

#### Allgemein

Der Task `deployForms` ermöglicht das Deployen von Formularen.<br />
Ist für ein Formular-JSON bereits eines mit der gleichen ID deployt, so wird das vorhandene
ersetzt.
Für jedes enthaltene Formular wird ein eigenes Deployment erzeugt, d.h. die Formulare können einzeln
undeployt werden.

#### Parameter

| **Name** | **Pflicht** | **Default-Wert** | **Beschreibung**                                                                                                                      |
|----------|-------------|------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| files    | Nein        | /forms           | Ordner aus dem Formulare gelesen werden. Es werden auch Unterordner berücksichtigt. Ein relativer Pfad ist relativ zum Projektordner. |

---------------------------------------------------------------------------------------------------

### Task zum Löschen eines Prozess-Deployments

#### Allgemein

Der Task `undeployProcess` ermöglicht das asynchrone Löschen eines Prozess-Deployments.<br />
Prozessdefinitionen, die Teil des Deployments sind, werden undeployt.

Es darf höchstens ein Undeployment pro Prozess-Engine gleichzeitig ausgeführt werden.<br />
Wird von der Prozess-Engine, zu der das gegebene Deployment gehört, bereits ein Undeployment
ausgeführt, wird der Task mit einer Fehlermeldung abgebrochen.

#### Parameter

| **Name**                   | **Pflicht** | **Default-Wert** | **Beschreibung**                                                                                                                                                                                 |
|----------------------------|-------------|------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| deploymentId               | Ja          |                  | ID eines Prozess-Deployments                                                                                                                                                                     |
| deleteProcessInstances     | Nein        | `false`          | Ob aktive Prozessinstanzen beendet werden sollen                                                                                                                                                 |
| undeploymentMessageSubject | Nein        | `null`           | Betreff der Nachricht die beim Undeployment des Prozesses verschickt wird.                                                                                                                       |
| undeploymentMessageBody    | Nein        | `null`           | Inhalt der Nachricht die beim Undeployment des Prozesses verschickt wird. <br/><br/> Hier können folgende Platzhalter verwendet werden: <ul><li>**{{name}}** : Der Name des Empfängers</li></ul> |

Der Parameter `deleteProcessInstances` definiert, was geschehen soll, wenn vor dem Undeployment
festgestellt wird, dass noch mindestens eine aktive Prozessinstanz einer Prozessdefinition, die Teil
des Deployments ist, existiert. Folgende Optionen sind möglich:

- `true`: Alle aktiven Prozessinstanzen werden beendet und das Undeployment ausgeführt
- `false`: Der Aufruf der Schnittstelle wird mit einer Fehlermeldung abgebrochen

---------------------------------------------------------------------------------------------------

### Task zum Löschen eines Formular-Deployments

#### Allgemein

Der Task `undeployForm` ermöglicht das Löschen eines Formular-Deployments.<br />
Formulare, die Teil des Deployments sind, werden undeployt.

#### Parameter

| **Name**     | **Pflicht** | **Beschreibung**              |
|--------------|-------------|-------------------------------|
| deploymentId | Ja          | ID eines Formular-Deployments |

---------------------------------------------------------------------------------------------------

### Task zum Auflisten aller Prozess-Deployments

#### Allgemein

Der Task `listProcesses` liefert die Liste aller auf der Umgebung vorhandenen
Prozess-Deployments.<br />
Ausgegeben wird eine Liste aller vorhandenen Prozessdeployments. Für jedes Prozessdeployment wird in
einer Zeile der Deployment-Zeitpunkt, der Name des Deployments, die Version und die Deployment-ID
ausgegeben. Direkt unter der Deployment-Zeile folgen in weiteren Zeilen, die jeweils mit einem
Spiegelstrich beginnen, die Keys und die Namen der enthaltenen Prozessmodelle. Die nächste Zeile
ohne Spiegelstrich enthält dann das nächste Deployment.

---------------------------------------------------------------------------------------------------

### Task zum Auflisten aller Formular-Deployments

#### Allgemein

Der Task `listForms` liefert die Liste aller auf der Umgebung vorhandenen
Formular-Deployments.<br />
In jeder Zeile wird ein deploytes Formular ausgegeben, und zwar hintereinander das Deployment-Datum,
die Deployment-ID, die Sprache des Formulars (de, en, fr) und die ID des Formulars.

---------------------------------------------------------------------------------------------------

### Task zum Verschlüsseln eines Prozessparameterwertes

#### Allgemein

Der Task `encryptParameterValue` verschlüsselt einen Prozessparameterwert und gibt den
verschlüsselten Wert auf der Konsole aus.<br />
Der so verschlüsselte Wert kann bei Jesaja eingespielt werden und wird
vom OZG-Hub für Prozesse mit dem angegebenen Prozesskey automatisch entschlüsselt.
Die Verschlüsselung muss auf der Umgebung (Prod, Dev, Test, ...) stattfinden,
auf der der Parameter nachher entschlüsselt wird, da der Schlüssel je Umgebung
unterschiedlich ist.

#### Parameter

| **Name**       | **Pflicht** | **Default-Wert** | **Beschreibung**                                                                                                                                                                                                                                                                  | 
|----------------|-------------|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| processKey     | Ja          |                  | Prozess-Schlüssel des Prozesses, für den der Prozessparameterwert eingesetzt werden soll. Achtung, nur für den Prozess mit diesem Prozess-Key kann der verschlüsselte Wert entschlüsselt werden!                                                                                  |
| parameterValue | Nein *      | `null`           | Der zu verschlüsselnde Prozessparameterwert.<br>* Entweder dieser Parameter oder `inputFile` muss gesetzt sein.                                                                                                                                                                   |
| inputFile      | Nein *      | `null`           | Datei deren Inhalt der zu verschlüsselnden Prozessparameterwert ist. Datei muss existieren und eine normale Datei sein. Ein relativer Pfad ist relativ zum Projektordner.<br>* Entweder dieser Parameter oder `parameterValue` muss gesetzt sein.                                 |
| charset        | Nein        | `UTF-8`          | Zeichenkodierung des Dateiinhalts, wenn `inputFile` gesetzt und `base64` gleich `false` ist. Unterstützt werden zum Beispiel `ASCII`, `UTF-8` und `ISO-8859-1`.                                                                                                                   |
| base64         | Nein        | `false`          | Ist der Wert `true` und der Parameter `parameterValue` gesetzt, wird der zu verschlüsselende Wert vor der Verschlüsselung Base64-kordiert. Ist der Wert `true` und der Parameter `inputFile` gesetzt, wird die Datei binär eingelesen und vor der Verschlüsselung Base64-kodiert. |
| outputFile     | Nein        | `null`           | Datei, in die der verschlüsselte Parameter geschrieben werden soll. Die Datei darf nicht bereits existieren. Ein relativer Pfad ist relativ zum Projektordner.                                                                                                                    |

---------------------------------------------------------------------------------------------------

### Task _createScheduledUndeploymentOzg_

#### Allgemein

Erstellt ein zeitgesteuertes Undeployment eines Online-Dienstes.

#### Parameter

| Parameter                          | Pflicht? | Beschreibung                                                                                                                      |
|------------------------------------|----------|-----------------------------------------------------------------------------------------------------------------------------------|
| deploymentId                       | Ja       | Deployment-ID des Online-Dienstes, der undeployt werden soll                                                                      |
| undeploymentDate                   | Ja       | Das Datum, an dem der Online-Dienst undeployt werden soll (TT.MM.YYYY)                                                            |
| undeploymentAnnounceMessageSubject | Nein     | Betreff der Ankündigungsnachricht eines Undeployments                                                                             |
| undeploymentAnnounceMessageBody    | Nein     | Text der Ankündigungsnachricht eines Undeployments                                                                                |
| undeploymentMessageSubject         | Nein     | Betreff der Nachricht eines Undeployments                                                                                         |
| undeploymentMessageBody            | Nein     | Text der Nachricht eines Undeployments                                                                                            |
| undeploymentHintText               | Nein     | Hinweistext der dem Nutzer angezeigt werden soll                                                                                  |
| startToDisplayUndeploymentHint     | Nein     | Das Datum ab dem der Hinweistext angezeigt werden soll. (default = Der Tag an dem das zeitgesteuerte Undeployment erstellt wurde) |

---------------------------------------------------------------------------------------------------

### Task _deleteScheduledUndeploymentOzg_

#### Allgemein

Löscht ein zeitgesteuertes Undeployment eines Online-Dienstes.

#### Parameter

| Parameter     | Pflicht? | Beschreibung                                                                                     |
|---------------|----------|--------------------------------------------------------------------------------------------------|
| deploymentId  | Ja       | Deployment-ID des Online-Dienstes, für den das zeitgesteuerte Undeployment gelöscht werden soll. |

---------------------------------------------------------------------------------------------------

### Task _listScheduledUndeploymentsOzg_

#### Allgemein

Listet alle zeitgesteuerten Undeployments von Online-Diensten auf.

---------------------------------------------------------------------------------------------------
