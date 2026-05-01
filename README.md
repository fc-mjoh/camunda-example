# Namenskonventionen

| Eigenschaft | 	Wert                        |
|:------------|:-----------------------------|
| Dateiname 	 | camunda-example-process.bpmn | 
| Prozess-ID  | 	camunda-example-process     | 
| Prozessname | 	Camunda Example Process     | 

| Elementtyp         | 	Präfix | 	Beispielname     | 	Beispiel-ID         |
|:-------------------|:--------|:------------------|:---------------------|
| User Task          | ut-     | Review invoice    | ut-review-invoice    |
| Service Task       | st-     | Send confirmation | st-send-confirmation |
| Start Event        | se-     | Order received    | se-order-received    |
| End Event          | ee-     | Payment completed | ee-payment-completed |
| Exclusive Gateway  | xg-     | Invoice approved? | xg-invoice-approved  |
| Parallel Gateway   | pg-     | Split processing  | pg-split-processing  |
| Intermediate Event | ie-     | Timer expired     | ie-timer-expired     |
| Call Activity      | ca-     | Process payment   | ca-process-payment   |
| Message Event      | me-     | Message received  | me-message-received  | 

# Wichtige Regeln & Hinweise

* IDs müssen eindeutig sein – auch wenn derselbe Elementtyp mit demselben Namen mehrfach vorkommt (z. B. zwei Task_SendEmail → Task_SendEmail_1, Task_SendEmail_2)
* Konsistenz ist Pflicht – wähle ein Schema und halte es im gesamten Projekt durch
* IDs früh vergeben – späte Umbenennung kann Tests und Prozesslogik brechen
* IDs erscheinen in Fehler-Stack-Traces → lesbare IDs erleichtern das Debugging erheblich
* IDs im Camunda Modeler (Properties Panel) bearbeiten, nicht direkt im XML – sonst können DI-Referenzen inkonsistent werden



|Was|Wie|Beispiel|
|:---|:---|:---
| Prozess-ID    |   PascalCase       |     → GenehmigungsProzess |
| Dateiname:   |       = Prozess-ID + .bpmn |  → GenehmigungsProzess.bpmn| 
| Task-ID:    |        Task_PascalCase   |     → Task_AntragPruefen| 
| Event-ID:   |        StartEvent_PascalCase | → StartEvent_AntragEingegangen| 
| Gateway-ID:    |     Gateway_PascalCase  |   → Gateway_AntragGenehmigt| 
| SequenceFlow-ID: |   SequenceFlow_...   |    → SequenceFlow_AntragGenehmigt_Ja| 
| Message-ID:    |     Message_PascalCase  |   → Message_AntragEingegangen| 
| Error-ID:   |        Error_PascalCase   |    → Error_ValidierungFehler|
| Task-Label:    |     Objekt + Verb     |     → "Antrag prüfen"|
| Event-Label:   |     Objekt + Zustand   |    → "Antrag eingegangen"|
| Gateway-Label:  |    Frage         |         → "Antrag genehmigt?"|
| Flow-Label:    |     Antwort      |         → "Ja" / "Nein"|
| Variablen:     |     camelCase + Präfix  |   → int_antragStatus|

# Wiremock

gradlew :wiremock-server:bootRun

# Camunda-engine

