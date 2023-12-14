# Übersicht

Dieses Dokument beschreibt die Schnittstellen einer OZG-Hub-Umgebung. Teilweise werden diese
Schnittstellen durch das Gradle-Plugin genutzt.

## Schnittstellendokumentation

### Authentifizierung

Um die Schnittstellen nutzen zu können, erfolgt die Authentifizierung als Header-Parameter.

#### Basic Auth

Zur Authentifizierung via Basic Auth ist der folgende Header-Parameter anzugeben.

| **Name**       | **Pflicht** | **Typ** | **Beschreibung**                           |
|----------------|-------------|---------|--------------------------------------------|
| Authentication | Ja          | String  | Basic <username:password (Base64-codiert)> |

#### OAuth2

Falls die entsprechende OZG-Hub Umgebung Keycloak anbindet kann alternativ auch der folgende Header-Parameter angegeben werden.

| **Name**       | **Pflicht** | **Typ** | **Beschreibung**      |
|----------------|-------------|---------|-----------------------|
| Authentication | Ja          | String  | Bearer <Bearer Token> |

### Schnittstelle zum Deployen eines Prozessmodells

#### Allgemein

Die Schnittstelle ermöglicht das Deployen eines Archives mit den zu einem Prozessmodell gehörenden
Prozessmodelldateien, auf eine spezifische Prozess-Engine.<br />
Ist keine Engine angegeben, wird auf die Standard-Prozess-Engine deployt. Der Aufruf muss als POST
ausgeführt werden. Optional können zu den Prozessmodelldateien Metadaten übergeben werden.

#### Pfad

`{URL der Umgebung}/prozess/ozghub/deployWithMetadata`

#### Header-Parameter

| **Name**                  | **Pflicht** | **Typ** | **Beschreibung**                                                                                                            |
|---------------------------|-------------|---------|-----------------------------------------------------------------------------------------------------------------------------|
| X-OZG-Process-Duplication | Ja          | String  | Wie mit Deployments, welche zu deployende Prozess-Keys enthalten, umgegangen werden soll<br />`IGNORE`, `UNDEPLOY`, `ERROR` |
| X-OZG-Process-Engine      | Nein        | String  | ID der Prozess-Engine, Standard-Prozess-Engine wenn nicht gesetzt                                                           |

#### Request-Body

- Die Schnittstelle erwartet als Body ein JSON-Objekt mit folgender Struktur:

```json
{
  "deploymentName": "deploymentName",
  "versionName": "v1.0",
  "barArchiveBase64": "barArchiveAlsBase64EncodedString",
  "metadata": {
    "prozessmodellDateiNameOhneErweiterungen": {
      "servicekontolos": true,
      "authenticationTypes": [
        "BUND_ID",
        "MUK"
      ]
    } 
  }  
}
```

| **Name**            | **Pflicht** | **Typ**      | **Beschreibung**                                                                                                                                                                             |
|---------------------|-------------|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| deploymentName      | Ja          | String       | Name des Deployments                                                                                                                                                                         |
| versionName         | Ja          | String       | Name der Version                                                                                                                                                                             |
| barArchiveBase64    | Ja          | String       | BAR-Archiv, welches die Prozessmodell-Dateien enthält, als Base64-encoded String                                                                                                             |
| metadata            | Nein        | Map          | Key ist der Name der Prozessmodell-Datei (ohne Erweiterungen), zu der die Metadaten gehören; Value ist ein ProcessMetadata Objekt, dessen Attribute die zu übergebenden Metadaten enthalten. |
| servicekontolos     | Nein        | Boolean      | Werte true oder false                                                                                                                                                                        |
| authenticationTypes | Nein        | String Array | Zugelassene Authentisierungsmittel                                                                                                                                                           |

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
werden. Ist für das im Body übergebene Formular-JSON bereits eines deployt, welches die gleiche Id
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

| **Name**                                | **Pflicht** | **Typ** | **Beschreibung**                                                                                          |
|-----------------------------------------|-------------|---------|-----------------------------------------------------------------------------------------------------------|
| X-OZG-Deployment-ID                     | Ja          | String  | ID des Deployments                                                                                        |
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

| **Name**            | **Pflicht** | **Typ**  | **Beschreibung**   |
|---------------------|-------------|----------|--------------------|
| X-OZG-Deployment-ID | Ja          | Ganzzahl | ID des Deployments |

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
        "processKey1": "processKey1",
        "processKey2": "processKey2"
      },
      "deploymentDate": 1635270704000,
      "deploymentName": "deploymentName1",
      "deploymentId": "141"
    },
    {
      "processDefinitionKeysAndNames": {
        "processKey3": "processKey3",
        "processKey4": "processKey4"
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

Informationen zum Ansprechen der Schnittstellen können [hier](https://doku.pmp.seitenbau.com/display/DFO/Prozesslogs) gefunden werden.

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

| **Name**       | **Pflicht** | **Typ** | **Beschreibung**                                                                             |
|----------------|-------------|---------|----------------------------------------------------------------------------------------------|
| processKey     | Ja          | String  | Der Prozess-Schlüssel des Prozesses, für den der Prozessparameterwert eingesetzt werden soll |
| parameterValue | Ja          | String  | Der zu verschlüsselnde Prozessparameterwert                                                  |

#### Rückgabewerte

Ein Objekt (`application/json`), das den verschlüsselten Prozessparameterwert enthält.<br />

```json
{
  "encryptedParameterValue": "ozghub:cu:PRPQaRkSuZYAjGPF2dKBZw==:/QQXGErMbU0=:s8WyOiUzZ91HADzjnlJJQw=="
}
```

---------------------------------------------------------------------------------------------------
