# Aktualisierungen

## 2024.01-03-0
- Feature: Für die Tasks `deployProcess` und `undeployProcess` wurden neue Parameter hinzugefügt
  - `undeployMessageSubject`: Betreff der Nachricht die beim Undeployment des Prozesses verschickt wird.
  - `undeployMessageText`: Inhalt der Nachricht die beim Undeployment des Prozesses verschickt wird.

## 2023.12-01-0
- Feature: Dependencies aktualisiert

## 2023.09-13-0
- Feature: Metadata um authenticationTypes erweitert
- Feature: Dependencies aktualisiert
- Feature: Gradle Version aktualisiert auf 7.6.2

## 2023.06-20-0
- Feature: Dependencies aktualisiert
- Feature: Gradle Version aktualisiert auf 7.6.1
- Feature: Build mit Java 17
- Feature: Die Schnittstellendokumentation wurde mit Authentifizierungsinformationen ergänzt

## 2023.02-06-0
- Optimierung: Ausgabe der deployten Prozesse für lange Deployment-IDs verbessert
- Feature: Dependencies aktualisiert

## 2023.01.16-0
- Feature: Der Task `encryptParameterValue` wurde um Attribute zum Lesen von Parametern aus Dateien
  und zum Schreiben verschlüsselter Parameter in Dateien ergänzt

## 2022.11.07-0
- Bugfix: Relative Dateipfade bei `deployProcess` und `deployForms` immer relativ zum Projektordner
- Bugfix: Tilde in Dateipfaden korrekt auflösen
- Feature: Dependencies aktualisiert

## 2022.09.13-0
- Feature: Der Task `deployProcess` benötigt den neuen Parameter `versionName`
- Feature: Der Task `listProcesses` zeigt den Wert von `versionName` an

## 2022.07.20-0
- Feature: Dependencies aktualisiert 

## 2022.06.29-0
- Feature: Der Task `encryptParameterValue` wurde hinzugefügt

## 2022.06.14-0
- Feature: Der Task `deployProcess` unterstützt nun das Übergeben von Metadaten an die Schnittstelle

## 2022.04.19-0
- Bugfix: Datumsausgabe bei `listForms`, `listProcesses` im 24h-Format
- Optimierung: changelog.md in CHANGELOG.md umbenannt

## 2022.03.28-0
- Bugfix: Reihenfolge der Spalten für `listProcesses` korrigiert
- Bugfix: `listProcesses` gibt die Prozessdefinition-Schlüssel in alphabetischer Reihenfolge aus
- Optimierung: Tasks.md Dokumentation angepasst
- Optimierung: prozesspipeline umbenannt in prozessdeployment
- Optimierung: Verbesserte Formatierung der Ausgabe von `listForms` und `listProcesses`
