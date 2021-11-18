# Übersicht

Dieses Dokument beschreibt die verfügbaren Tasks dieses Gradle-Plugins.

## Allgemein

Parameter der Tasks werden als Kommantozeilen-Parameter übergeben.<br />
Die Reihenfolge der Parameter spielt dabei keine Rolle.

Die folgenden Parameter werden von allen Tasks verpflichted benötigt.

| **Name** | **Beschreibung**                   |
| -------- | ---------------------------------- |
| url      | URL zu einer OZG-Hub-Umgebung      |
| user     | Benutzername zur Authentifizierung |
| password | Password zur Authentifizierung     |

## Task-Dokumentation

### Task zum Deployen eines Prozessmodells

#### Allgemein

Der Task `deployProcess` ermöglicht das Deployen von Prozessmodell-Dateien auf eine spezifische
Prozess-Engine.<br />
Ist keine Engine angegeben, wird auf die Standard-Prozess-Engine deployt.

#### Parameter

| **Name**                  | **Default-Wert** | **Beschreibung**                   |
| ------------------------- | ---------------- | ---------------------------------- |
| files                     | /build/models    | Ordner aus dem Prozessmodelle gelesen werden. Es werden auch Unterordner berücksichtigt |
| deploymentName            |                  | Name des Deployments |
| duplicateProcesskeyAction | `ERROR`          | Spezifikation, wie mit bereits deployten Prozess-Keys umgegangen werden soll |
| engine                    | `null`           | ID der Prozess-Engine, auf welche deployt werden soll. Bei `null` wird die Standard-Engine der Umgebung verwendet |

Der Parameter `duplicateProcesskeyAction` definiert, was geschehen soll, wenn vor dem Deployment der
gegebenen Prozessmodell-Dateien festgestellt wird, dass mindestens ein Prozess-Key bereits Teil
eines Deployments ist. Die folgenden Optionen sind möglich:

- `IGNORE`: Ignorieren. Das Deployment wird fortgesetzt
- `UNDEPLOY`: Deployments werden zuvor gelöscht (Undeployen der entsprechenden Prozessdefinitionen)
- `ERROR`: Der Aufruf der Schnittstelle wird mit einer Fehlermeldung abgebrochen

---------------------------------------------------------------------------------------------------

### Task zum Deployen von Formularen

#### Allgemein

Der Task `deployForms` ermöglicht das Deployen von Formularen.<br />
Ist für ein Formular-JSON bereits eines mit der gleichen Id deployed, so wird das vorhandene
ersetzt.

#### Parameter

| **Name**                   | **Default-Wert** | **Beschreibung**                   |
| -------------------------- | ---------------- | ---------------------------------- |
| files                      | /forms           | Ordner aus dem Formulare gelesen werden. Es werden auch Unterordner berücksichtigt |

---------------------------------------------------------------------------------------------------

### Task zum Löschen eines Prozess-Deployments

#### Allgemein

Der Task `undeployProcess` ermöglicht das Löschen eines Prozess-Deployments.

#### Parameter

| **Name**               | **Default-Wert** | **Beschreibung**             |
| ---------------------- | ---------------- | ---------------------------- |
| deploymentId           |                  | ID eines Prozess-Deployments |
| deleteProcessInstances | `false`          | Ob aktive Prozessinstanzen beendet werden sollen |

Der Parameter `deleteProcessInstances` definiert, was geschehen soll, wenn vor dem Undeployment
festgestellt wird, dass noch mindestens eine aktive Prozessinstanz einer Prozessdefinition, die Teil
des Deployments ist, existiert. Folgende Optionen sind möglich:

- `true`: Alle aktiven Prozessinstanzen werden beendet und das Undeployment ausgeführt
- `false`: Der Aufruf der Schnittstelle wird mit einer Fehlermeldung abgebrochen

---------------------------------------------------------------------------------------------------
