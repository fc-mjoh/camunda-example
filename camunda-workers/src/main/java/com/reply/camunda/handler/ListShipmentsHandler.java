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

import java.util.*;
import java.util.regex.*;

@Slf4j
@Component
@ExternalTaskSubscription("listShipments")
public class ListShipmentsHandler implements ExternalTaskHandler {

    private final String defaultEndpoint;
    private final RestTemplate restTemplate;

    public ListShipmentsHandler(@Value("${bipro.insurer.default-endpoint}") String defaultEndpoint,
                                RestTemplate restTemplate) {
        this.defaultEndpoint = defaultEndpoint;
        this.restTemplate = restTemplate;
    }



    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        String endpoint = task.getVariable("insurerEndpoint");
        if (null == endpoint) {
            endpoint = this.defaultEndpoint;
        }
        log.info("[listShipments] Calling endpoint: {}", endpoint);

        String soapBody = """
            <?xml version="1.0" encoding="UTF-8"?>
            <soapenv:Envelope
               xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:transfer="http://www.bipro.net/namespace/transfer"
               xmlns:nachrichten="http://www.bipro.net/namespace/nachrichten">
               <soapenv:Header/>
               <soapenv:Body>
                  <transfer:listShipments>
                     <transfer:Request>
                        <nachrichten:BiPROVersion>2.8.5.1.0</nachrichten:BiPROVersion>
                        <nachrichten:GeschaeftspartnerID>MVP-001</nachrichten:GeschaeftspartnerID>
                     </transfer:Request>
                  </transfer:listShipments>
               </soapenv:Body>
            </soapenv:Envelope>
            """;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.set("SOAPAction", "listShipments");

            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                new HttpEntity<>(soapBody, headers),
                String.class
            );

            List<String> shipmentIds = parseShipmentIds(response.getBody());
            log.info("[listShipments] Found {} shipments: {}", shipmentIds.size(), shipmentIds);

            Map<String, Object> vars = new HashMap<>();
            vars.put("shipmentIds", shipmentIds);
            vars.put("shipmentsAvailable", !shipmentIds.isEmpty());

            service.complete(task, vars);

        } catch (Exception e) {
            log.error("[listShipments] Fehler: {}", e.getMessage(), e);
            service.handleFailure(task,
                "listShipments fehlgeschlagen",
                e.getMessage(),
                3,    // Retries
                5000  // Retry-Timeout (ms)
            );
        }
    }

    private List<String> parseShipmentIds(String xml) {
        List<String> ids = new ArrayList<>();
        Pattern pattern = Pattern.compile("<transfer:ID>(.*?)</transfer:ID>");
        Matcher matcher = pattern.matcher(xml);
        while (matcher.find()) {
            ids.add(matcher.group(1).trim());
        }
        return ids;
    }
}
