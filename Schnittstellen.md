# Übersicht

Dieses Dokument beschreibt die Schnittstellen seitens einer OZG-Hub-Umgebung, welche durch das
Gradle-Plugin genutzt werden.

## Schnittstellendokumentation

### Schnittstelle zum Deployen eines Prozessmodells

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

Die Schnittstelle ermöglicht das Löschen eines Prozess-Deployments.<br />
Prozessdefinitionen, die Teil des Deployments sind, werden undeployt.<br />
Der Aufruf muss als DELETE ausgeführt werden.

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
