# Namenskonventionen

| Eigenschaft | 	Wert                        |
|:------------|:-----------------------------|
| Dateiname 	 | camunda-example-process.bpmn | 
| Prozess-ID  | 	camunda-example-process     | 
| Prozessname | 	Camunda Example Process     | 

| Elementtyp         | 	Präfix | 	Beispielname                          | 	Beispiel-ID |
|:-------------------|:--------|:---------------------------------------|:-------------|
| User Task          | ut-     | Review invoice ut-review-invoice       |
| Service Task       | st-     | Send confirmation st-send-confirmation |
| Start Event        | se-     | Order received se-order-received       |
| End Event          | ee-     | Payment completed ee-payment-completed |
| Exclusive Gateway  | xg-     | Invoice approved? xg-invoice-approved  |
| Parallel Gateway   | pg-     | Split processing pg-split-processing   |
| Intermediate Event | ie-     | Timer expired ie-timer-expired         |
| Call Activity      | ca-     | Process payment ca-process-payment     |
| Message Event      | me-     | Message received me-message-received   | 