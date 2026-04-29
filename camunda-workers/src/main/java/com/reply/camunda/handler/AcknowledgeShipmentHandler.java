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

@Slf4j
@Component
@ExternalTaskSubscription("acknowledgeShipment")
public class AcknowledgeShipmentHandler implements ExternalTaskHandler {

    private final RestTemplate restTemplate;
    private final String defaultEndpoint;

    public AcknowledgeShipmentHandler(RestTemplate restTemplate,
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

        log.info("[acknowledgeShipment] Bestätige ShipmentID: {}", shipmentId);

        String soapBody = String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <soapenv:Envelope
               xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:transfer="http://www.bipro.net/namespace/transfer"
               xmlns:nachrichten="http://www.bipro.net/namespace/nachrichten">
               <soapenv:Header/>
               <soapenv:Body>
                  <transfer:acknowledgeShipment>
                     <transfer:Request>
                        <nachrichten:BiPROVersion>2.8.5.1.0</nachrichten:BiPROVersion>
                        <transfer:LieferungsID>%s</transfer:LieferungsID>
                     </transfer:Request>
                  </transfer:acknowledgeShipment>
               </soapenv:Body>
            </soapenv:Envelope>
            """, shipmentId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.set("SOAPAction", "acknowledgeShipment");

            restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                new HttpEntity<>(soapBody, headers),
                String.class
            );

            log.info("[acknowledgeShipment] Erfolgreich bestätigt: {}", shipmentId);
            service.complete(task);

        } catch (Exception e) {
            log.error("[acknowledgeShipment] Fehler: {}", e.getMessage(), e);
            service.handleFailure(task,
                "acknowledgeShipment fehlgeschlagen",
                e.getMessage(),
                3,
                5000
            );
        }
    }
}
