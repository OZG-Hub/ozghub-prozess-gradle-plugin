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

| **Name**                  | **Pflicht** | **Beschreibung**     |
| ------------------------- | ----------- | -------------------- |
| X-OZG-Deployment-Name     | Ja          | Name des Deployments |
| X-OZG-Process-Duplication | Ja          | Wie mit Deployments, welche zu deployende Prozess-Keys enthalten, umgegangen werden soll<br />`IGNORE`, `UNDEPLOY`, `ERROR`
| X-OZG-Process-Engine      | Nein        | ID der Prozess-Engine, Standard-Prozess-Engine wenn nicht gesetzt

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
Der Aufruf muss als DELETE ausgeführt werden.

#### Pfad

`{URL der Umgebung}/prozess/ozghub/undeploy`

#### Header-Parameter

| **Name**                                | **Pflicht** | **Beschreibung**   |
| --------------------------------------- | ----------- | ------------------ |
| X-OZG-Deployment-ID                     | Ja          | ID des Deployments |
| X-OZG-Deployment-DeleteProcessInstances | Ja          | Wenn `true` werden aktive Prozessinstanzen beendet, wenn `false` dürfen keine Prozessinstanzen existieren |

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
