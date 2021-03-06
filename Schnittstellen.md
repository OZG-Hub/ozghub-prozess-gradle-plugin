# Übersicht

Dieses Dokument beschreibt die Schnittstellen einer OZG-Hub-Umgebung. Teilweise werden diese
Schnittstellen durch das Gradle-Plugin genutzt.

## Schnittstellendokumentation

### Schnittstelle zum Deployen eines Prozessmodells (deprecated)

#### Allgemein

Die Schnittstelle ermöglicht das Deployen eines Archives mit den zu einem Prozessmodell gehörenden
Prozessmodelldateien, auf eine spezifische Prozess-Engine.<br />
Ist keine Engine angegeben, wird auf die Standard-Prozess-Engine deployt. Der Aufruf muss als POST
ausgeführt werden.

#### Pfad

`{URL der Umgebung}/prozess/ozghub/deploy`

#### Header-Parameter

| **Name**                  | **Pflicht** | **Typ** | **Beschreibung**     |
| ------------------------- | ----------- | ------- | -------------------- |
| X-OZG-Deployment-Name     | Ja          | String  | Name des Deployments |
| X-OZG-Process-Duplication | Ja          | String  | Wie mit Deployments, welche zu deployende Prozess-Keys enthalten, umgegangen werden soll<br />`IGNORE`, `UNDEPLOY`, `ERROR` |
| X-OZG-Process-Engine      | Nein        | String  | ID der Prozess-Engine, Standard-Prozess-Engine wenn nicht gesetzt |

#### Request-Body

- Die Schnittstelle erwartet als Body eine ZIP-Datei, welche vom Aufbau her dem Business Archive von
  Activiti entspricht, siehe
  [Activiti User Guide](https://www.activiti.org/userguide/#_business_archives).
- Die Schnittstelle verarbeitet die im Hauptordner des Archives abgelegten `*.bpmn20.xml`-Dateien.

#### Rückgabewerte

Ein Objekt mit den Informationen zu den Vorgängen (`application/json`).<br />
Darunter die ID des erzeugten Deployments, die Prozess-Keys der deployten Prozessdefinitionen und
die Prozess-Keys, welche bereits Teil eines Deployments auf der Umgebung waren.

```json
{
  "deploymentId": "141",
  "processKeys": [
    "m1.testprocess-1",
    "m1.testprocess-2"
  ],
  "duplicateKeys": [
    "m1.testprocess-1"
  ],
  "removedDeploymentIds": [
    "140"
  ]
}
```

---------------------------------------------------------------------------------------------------

### Schnittstelle zum Deployen eines Prozessmodells mit Metadaten

#### Allgemein

Die Schnittstelle ermöglicht das Deployen eines Archives mit den zu einem Prozessmodell gehörenden
Prozessmodelldateien, auf eine spezifische Prozess-Engine.<br />
Ist keine Engine angegeben, wird auf die Standard-Prozess-Engine deployt. Der Aufruf muss als POST
ausgeführt werden. Optional können zu den Prozessmodell-Dateien Metadaten übergeben werden.

#### Pfad

`{URL der Umgebung}/prozess/ozghub/deployWithMetadata`

#### Header-Parameter

| **Name**                  | **Pflicht** | **Typ** | **Beschreibung**     |
| ------------------------- | ----------- | ------- | -------------------- |
| X-OZG-Process-Duplication | Ja          | String  | Wie mit Deployments, welche zu deployende Prozess-Keys enthalten, umgegangen werden soll<br />`IGNORE`, `UNDEPLOY`, `ERROR` |
| X-OZG-Process-Engine      | Nein        | String  | ID der Prozess-Engine, Standard-Prozess-Engine wenn nicht gesetzt |

#### Request-Body

- Die Schnittstelle erwartet als Body ein JSON-Objekt mit folgender Struktur:

```json
{
  "deploymentName": "deploymenName",
  "barArchiveBase64": "barArchiveAlsBase64EncodedString",
  "metadata": {
    "prozessmodellDateiNameOhneExtensions": {
      "servicekontolos": true
    } 
  }  
}
```

| **Name**         | **Pflicht** | **Typ** | **Beschreibung**                                                                                                                                                                                                                                                           |
|------------------|-------------|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| deploymenName    | Ja          | String  | Name des Deployments                                                                                                                                                                                                                                                       |
| barArchiveBase64 | Ja          | String  | BAR-Archiv, welches die Prozessmodell-Dateien enthält, als Base64-encoded String                                                                                                                                                                                           |
| metadata         | Nein        | Map     | Keys ist der Name der Prozessmodell-Datei (ohne Extensions), zu der die Metadaten gehören; Value ist ein Objekt, dessen Attribute die zu übergebenden Metadaten enthalten. Aktuell wird ausschließlich das Attribut _servicekontolos_ (Werte true oder false) unterstützt. |


#### Rückgabewerte

Ein Objekt mit den Informationen zu den Vorgängen (`application/json`).<br />
Darunter die ID des erzeugten Deployments, die Prozess-Keys der deployten Prozessdefinitionen und
die Prozess-Keys, welche bereits Teil eines Deployments auf der Umgebung waren.

```json
{
  "deploymentId": "141",
  "processKeys": [
    "m1.testprocess-1",
    "m1.testprocess-2"
  ],
  "duplicateKeys": [
    "m1.testprocess-1"
  ],
  "removedDeploymentIds": [
    "140"
  ]
}
```

---------------------------------------------------------------------------------------------------

### Schnittstelle zum Deployen eines Formulars

#### Allgemein

Die Schnittstelle ermöglicht das Deployen eines Formulars. Der Aufruf muss als POST ausgeführt
werden. Ist für das im Body übergebene Formular-JSON bereits eines deployed, welches die gleiche Id
hat, so wird das vorhandene ersetzt.

#### Pfad

`{URL der Umgebung}/formulare/ozghub/deploy`

#### Request-Body

Die Schnittstelle erwartet als Body einen JSON-String.

#### Rückgabewerte

Ein Objekt mit den Informationen zu den Vorgängen (`application/json`).<br />
Dieses enthält die ID des erzeugten Deployments.

```json
{
  "deploymentId": "141"
}
```

---------------------------------------------------------------------------------------------------

### Schnittstelle zum Löschen eines Prozess-Deployments

#### Allgemein

Die Schnittstelle ermöglicht das asynchrone Löschen eines Prozess-Deployments.<br />
Prozessdefinitionen, die Teil des Deployments sind, werden undeployt.<br />
Der Aufruf muss als DELETE ausgeführt werden.

Es darf höchstens ein Undeployment pro Prozess-Engine gleichzeitig ausgeführt werden.<br />
Wird von der Prozess-Engine, zu der das gegebene Deployment gehört, bereits ein Undeployment
ausgeführt, wird der Aufruf mit einer Fehlermeldung abgebrochen.

#### Pfad

`{URL der Umgebung}/prozess/ozghub/undeploy`

#### Header-Parameter

| **Name**                                | **Pflicht** | **Typ** | **Beschreibung**   |
| --------------------------------------- | ----------- | ------- | ------------------ |
| X-OZG-Deployment-ID                     | Ja          | String  | ID des Deployments |
| X-OZG-Deployment-DeleteProcessInstances | Ja          | boolean | Wenn `true` werden aktive Prozessinstanzen beendet, wenn `false` dürfen keine Prozessinstanzen existieren |

#### Rückgabewerte

Ein Objekt mit den Informationen zu den Vorgängen (`application/json`).<br />
Darunter die Prozess-Keys der undeployten Prozessdefinitionen.

```json
{
  "processKeys": [
    "m1.testprocess-1",
    "m1.testprocess-2"
  ]
}
```

---------------------------------------------------------------------------------------------------

### Schnittstelle zum Löschen eines Formular-Deployments

#### Allgemein

Die Schnittstelle ermöglicht das Löschen eines Formular-Deployments.<br />
Formulare, die Teil des Deployments sind, werden undeployt.<br />
Der Aufruf muss als DELETE ausgeführt werden.

#### Pfad

`{URL der Umgebung}/formulare/ozghub/undeploy`

#### Header-Parameter

| **Name**             | **Pflicht** | **Typ**  | **Beschreibung**   |
| -------------------- | ----------- | -------- | ------------------ |
| X-OZG-Deployment-ID  | Ja          | Ganzzahl | ID des Deployments |

#### Rückgabewerte

Ein Objekt mit den Informationen zu den Vorgängen (`application/json`).<br />
Darunter die ID des undeployten Formulars.

```json
{
  "id": "1:test-formular:v1.0"
}
```

---------------------------------------------------------------------------------------------------

### Schnittstelle zum Auflisten der Prozess-Deployments

#### Allgemein

Die Schnittstelle liefert eine Liste der auf der Umgebung vorhandenen Prozess-Deployments jeweils
mit Informationen zu Name des Deployments, Deployment-Datum, Deployment-Id sowie den enthaltenen
Prozessdefinitionen. Die Liste ist absteigend nach Deployment-Datum sortiert. Innerhalb eines
Deployments sind die enthaltenen Prozessdefinitionen aufsteigend nach dem Key sortiert.

Es wird außerdem die Information zurückgeliefert, ob die Deployments von allen Prozess-Engines
abgerufen werden konnten

Der Aufruf muss als GET ausgeführt werden.

#### Pfad

`{URL der Umgebung}/prozess/ozghub/list`

#### Rückgabewerte

Ein Objekt (`application/json`) mit der Information, ob alle Prozess-Engines geantwortet haben und
einer Liste der vorhandenen Deployments.<br />

```json
{
  "complete": true,
  "value": [
    {
      "processDefinitionKeysAndNames": {
        "processKey1": "processName1",
        "processKey2": "processName2"
      },
      "deploymentDate": 1635270704000,
      "deploymentName": "deploymentName1",
      "deploymentId": "141"
    },
    {
      "processDefinitionKeysAndNames": {
        "processKey3": "processName3",
        "processKey4": "processName4"
      },
      "deploymentDate": 1629293345000,
      "deploymentName": "deploymentName2",
      "deploymentId": "142"
    }
  ]
}
```

---------------------------------------------------------------------------------------------------

### Schnittstelle zum Auflisten der Formular-Deployments

#### Allgemein

Die Schnittstelle liefert eine Liste der auf der Umgebung vorhandenen Formular-Deployments jeweils
mit Informationen zu Mandant-Id, Name des Formulars, Name der Version, Sprache, Deployment-Datum und
Deployment-Id. Die Liste wird nach den in den Formulardefinitionen angegebenen IDs sortiert.

Der Aufruf muss als GET ausgeführt werden.

#### Pfad

`{URL der Umgebung}/formulare/ozghub/list`

#### Rückgabewerte

Ein Objekt (`application/json`), das eine Liste mit den vorhandenen Deployments enthält.<br />

```json
{
  "deploymentList": [
    {
      "mandantId": "1",
      "formName": "formularName",
      "formVersion": "formularVersion",
      "language": "sprache",
      "deploymentDate": 1629293345000,
      "deploymentId": "141"
    },
    {
      "mandantId": "1",
      "formName": "formularName",
      "formVersion": "formularVersion",
      "language": "sprache",
      "deploymentDate": 1629293345000,
      "deploymentId": "141"
    }
  ]
}
```

---------------------------------------------------------------------------------------------------

### Schnittstelle zum Lesen der Logs

#### Allgemein

Die Schnittstelle liefert eine paginierte Liste der Log-Einträge der Umgebung im JSON Format.
Diverse Filter können eingesetzt werden, um die passenden Logeinträge zu finden

Der Aufruf muss als GET ausgeführt werden.

#### Pfad

`{URL der Umgebung}/logview/ozghub/entries`

#### Query-Parameter

| **Name**             | **Pflicht** | **Typ**  | **Beschreibung**   |
| -------------------- | ----------- | -------- | ------------------ |
| sortProperty         | Nein        | String   | Eigenschaft, welche zur Sortierung herangezogen werden soll. Mögliche Werte: id, createDate, applicationName, level (Sortierung nach Alphabet, nicht semantisch!), logger, message, exception, logEntryType, mandant. Default: createDate |
| sortDirection        | Nein        | String   | Ob aufsteigend (ASC) oder absteigend (DESC) sortiert werden soll. Default: DESC |
| from                 | Nein        | Long     | Untere Grenze für das Datum der angezeigten Logeinträge, als Unix Timestamp. Default : keine untere Grenze  |
| to                   | Nein        | Long     | Obere Grenze für das Datum der angezeigten Logeinträge, als Unix Timestamp. Default : keine obere Grenze |
| mandant              | Nein        | String   | Id des Mandanten, dessen Logeinträge angezeigt werden sollen. Default : es werden Logeinträge aller Mandanten angezeigt. |
| attributeMatch       | Nein        | String   | Filtert nach weiteren Attributen eines Logeintrags. Das Format ist attributeName=value1,value2,value3... Die Values werden verodert. Der Query-Parameter kann mehrfach verwendet werden, in diesem Fall werden die mehreren Verwendungen verundet. Erlaubte Attributnamen sind id, createDate, applicationName, level, logger, message, exception und logEntryType. Die Suche nach message findet auch Einträge, die den gesuchten String in der Message enthalten; für alle anderen Attribute wird nach einem exakten Treffer gesucht. |
| contextMatch         | Nein        | String   | Filter nach Einträgen in der Context-Map eines Logeintrags. Das Format ist contextKey=value1,value2,value3... Die Values werden verodert. Der Query-Parameter kann mehrfach verwendet werden, in diesem Fall werden die mehreren Verwendungen verundet. |
| page                 | Nein        | int      | Die Nummer der Seite für die Paginierung. Default ist 0. |
| pageSize             | Nein        | int      | Wie groß eine Seite der Paginierung sein soll. Muss einen Wert zwischen 1 und 1000 enthalten. Default ist 1000. |

#### Rückgabewerte

Ein Objekt (`application/json`), das die gefundenen Logeinträge und die Paging-Informationen
enthält.<br />

```json
{
  "items": [
    {
      "id": 56,
      "createDate": 1642412152000,
      "applicationName": "prozess-service",
      "level": "INFO",
      "logger": "de.seitenbau.serviceportal.prozess.service.DeploymentService",
      "message": "Prozessmodell m1.testInternal zum ersten Mal deployed.",
      "exception": "someExceptionStacktrace",
      "logEntryType": "PROZESS",
      "mandant": "1",
      "context": [
        {
          "name": "PROCESS_DEFINITION_ID",
          "value": "m1.testInternal:1:28"
        },
        {
          "name": "PROCESS_DEFINITION_KEY",
          "value": "m1.testInternal"
        }
      ]
    }
  ],
  "offset": 0,
  "limit": 1000,
  "total": 1
}
```

---------------------------------------------------------------------------------------------------

### Schnittstelle zum Verschlüsseln eines Prozessparameters

#### Allgemein

Die Schnittstelle verschlüsselt einen Prozessparameter.

Der Aufruf muss als POST ausgeführt werden.

#### Pfad

`{URL der Umgebung}/prozessparameter/parameter/encryptParameterValue`

#### Request-Body

- Die Schnittstelle erwartet als Body ein JSON-Objekt mit folgender Struktur:

```json
{
  "processKey": "Prozess-Schlüssel",
  "parameterValue": "Prozess-Parameterwert"
}
```

| **Name**       | **Pflicht** | **Typ** | **Beschreibung**                                                                              |
|----------------|-------------| ------- |-----------------------------------------------------------------------------------------------|
| processKey     | Ja          | String  | Der Prozess-Schlüssel des Prozesses, für den der Prozessparameterwert eingesetzt werden soll  |
| parameterValue | Ja          | String  | Der zu verschlüsselnde Prozessparameterwert                                                   |

#### Rückgabewerte

Ein Objekt (`application/json`), das den verschlüsselten Prozessparameterwert enthält.<br />

```json
{
  "encryptedParameterValue": "ozghub:cu:PRPQaRkSuZYAjGPF2dKBZw==:/QQXGErMbU0=:s8WyOiUzZ91HADzjnlJJQw=="
}
```

---------------------------------------------------------------------------------------------------
