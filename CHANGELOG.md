# Aktualisierungen

## 2022.11.07-0
- Bugfix: Relative Dateipfade bei `deployProcess` und `deployForms` immmer relativ zum Projektordner
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
