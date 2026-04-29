package com.reply.camunda.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@ExternalTaskSubscription("getShipment")
public class GetShipmentHandler implements ExternalTaskHandler {

    private final RestTemplate restTemplate;
    private final String defaultEndpoint;

    public GetShipmentHandler(RestTemplate restTemplate,
                              @Value("${bipro.insurer.default-endpoint}") String defaultEndpoint) {
        this.restTemplate = restTemplate;
        this.defaultEndpoint = defaultEndpoint;
    }

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        String endpoint   = task.getVariable("insurerEndpoint");
        String shipmentId = task.getVariable("currentShipmentId");

        if (null == endpoint) {
            endpoint = this.defaultEndpoint;
        }

        log.info("[getShipment] Abrufen ShipmentID: {}", shipmentId);

        String soapBody = String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <soapenv:Envelope
               xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:transfer="http://www.bipro.net/namespace/transfer"
               xmlns:nachrichten="http://www.bipro.net/namespace/nachrichten">
               <soapenv:Header/>
               <soapenv:Body>
                  <transfer:getShipment>
                     <transfer:Request>
                        <nachrichten:BiPROVersion>2.8.5.1.0</nachrichten:BiPROVersion>
                        <transfer:LieferungsID>%s</transfer:LieferungsID>
                     </transfer:Request>
                  </transfer:getShipment>
               </soapenv:Body>
            </soapenv:Envelope>
            """, shipmentId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.set("SOAPAction", "getShipment");

            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                new HttpEntity<>(soapBody, headers),
                String.class
            );

            String responseBody = response.getBody();

            // 1. Persistieren in externe DB/DMS (hier: Simulation)
            String dbRef = persistShipment(shipmentId, responseBody);
            log.info("[getShipment] Persistiert als Ref: {}", dbRef);

            // 2. Validieren
            boolean isValid = validateShipment(responseBody);
            String defectReason = isValid ? null : "DOKUMENT_UNLESBAR";
            log.info("[getShipment] Validierung: {}", isValid ? "OK" : "FEHLERHAFT");

            // 3. Prozessvariablen setzen
            Map<String, Object> vars = new HashMap<>();
            vars.put("shipmentRef", dbRef);
            vars.put("shipmentValid", isValid);
            vars.put("defectReason", defectReason);

            service.complete(task, vars);

        } catch (Exception e) {
            log.error("[getShipment] Fehler: {}", e.getMessage(), e);
            service.handleFailure(task,
                "getShipment fehlgeschlagen",
                e.getMessage(),
                3,
                5000
            );
        }
    }

    /**
     * Simuliert Persistierung in externe DB/DMS.
     * In Produktion: Speichern in DB, DMS, S3 etc. – nur Referenz zurückgeben.
     */
    private String persistShipment(String shipmentId, String payload) {
        // TODO: Echte Persistierung implementieren
        return "REF-" + shipmentId + "-" + System.currentTimeMillis();
    }

    /**
     * Validiert den Shipment-Inhalt.
     * In Produktion: PDF-Prüfung, XML-Schema-Validierung etc.
     */
    private boolean validateShipment(String xml) {
        // TODO: Echte Validierungslogik implementieren
        return xml != null && xml.contains("StatusID") && !xml.contains("FEHLER");
    }
}
