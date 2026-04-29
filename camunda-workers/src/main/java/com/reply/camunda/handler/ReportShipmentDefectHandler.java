package com.reply.camunda.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@ExternalTaskSubscription("reportShipmentDefect")
@RequiredArgsConstructor
public class ReportShipmentDefectHandler implements ExternalTaskHandler {

    private final RestTemplate restTemplate;

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        String endpoint   = (String) task.getVariable("insurerEndpoint");
        String shipmentId = (String) task.getVariable("currentShipmentId");
        String reason     = (String) task.getVariable("defectReason");
        log.warn("[reportShipmentDefect] Melde Mangel für ShipmentID: {} | Grund: {}", shipmentId, reason);

        String soapBody = String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <soapenv:Envelope
               xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:transfer="http://www.bipro.net/namespace/transfer"
               xmlns:nachrichten="http://www.bipro.net/namespace/nachrichten">
               <soapenv:Header/>
               <soapenv:Body>
                  <transfer:reportShipmentDefect>
                     <transfer:Request>
                        <nachrichten:BiPROVersion>2.8.5.1.0</nachrichten:BiPROVersion>
                        <transfer:LieferungsID>%s</transfer:LieferungsID>
                        <transfer:Mangelgrund>
                           <transfer:ArtID>%s</transfer:ArtID>
                        </transfer:Mangelgrund>
                     </transfer:Request>
                  </transfer:reportShipmentDefect>
               </soapenv:Body>
            </soapenv:Envelope>
            """, shipmentId, reason != null ? reason : "UNBEKANNT");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.set("SOAPAction", "reportShipmentDefect");

            restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                new HttpEntity<>(soapBody, headers),
                String.class
            );

            log.info("[reportShipmentDefect] Mangelanzeige erfolgreich gesendet: {}", shipmentId);
            service.complete(task);

        } catch (Exception e) {
            log.error("[reportShipmentDefect] Fehler: {}", e.getMessage(), e);
            service.handleFailure(task,
                "reportShipmentDefect fehlgeschlagen",
                e.getMessage(),
                3,
                5000
            );
        }
    }
}
