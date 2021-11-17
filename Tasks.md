# Übersicht

Dieses Dokument beschreibt die verfügbaren Tasks dieses Gradle-Plugins.

## Allgemein

Parameter der Tasks werden als Kommantozeilen-Parameter übergeben. So kann z.B.: der Parameter `url`
an einem Task mittels `-Purl=http://mtest1.ozghub.imbw.dev.seitenbau.net:9081` übergeben. werden Die
Reihenfolge der Parameter spielt dabei keine Rolle.

Die folgenden Parameter werden von allen Tasks verpflichted benötigt.

| **Name** | **Beschreibung**                   |
| -------- | ---------------------------------- |
| url      | URL zu einer OZG-Hub-Umgebung      |
| user     | Benutzername zur Authentifizierung |
| password | Password zur Authentifizierung     |

## Task-Dokumentation

### Task zum Deployen eines Prozessmodells

#### Allgemein

Der Task ermöglicht das Deployen von Prozessmodell-Dateien auf eine spezifische
Prozess-Engine.<br />
Ist keine Engine angegeben, wird auf die Standard-Prozess-Engine deployt.

#### Parameter

| **Name**                   | **Default-Wert** | **Beschreibung**                   |
| -------------------------- | ---------------- | ---------------------------------- |
| files                      | /build/models    | Ordner aus dem Prozessmodelle gelesen werden |
| deploymentName             |                  | Name des Deployments |
| duplicateProcesskeyAction  | `ERROR`          | Spezifikation, wie mit bereits deployten Prozess-Keys umgegangen werden soll |
| engine                     | `null`           | ID der Prozess-Engine, auf welche deployt werden soll. Bei `null` wird die Standard-Engine der Umgebung verwendet |

Der Parameter `duplicateProcesskeyAction` definiert, was geschehen soll, wenn vor dem Deployment der
gegebenen Prozessmodell-Dateien festgestellt wird, dass mindestens ein Prozess-Key bereits Teil
eines Deployments ist. Die folgenden Optionen sind möglich:

- `IGNORE`: Ignorieren, das Deployment wird fortgesetzt
- `UNDEPLOY`: Prozessdefinitionen mit doppelten Prozess-Keys werden zuvor undeployt
- `ERROR`: Der Aufruf der Schnittstelle wird mit einer Fehlermeldung abgebrochen

### Task zum Deployen von Formularen

#### Allgemein

Der Task ermöglicht das Deployen von Formularen.

#### Parameter
| **Name**                   | **Default-Wert** | **Beschreibung**                   |
| -------------------------- | ---------------- | ---------------------------------- |
| files                      | /forms           | Ordner aus dem Formulare gelesen werden. Es werden auch Unterordner berücksichtigt |

---------------------------------------------------------------------------------------------------
