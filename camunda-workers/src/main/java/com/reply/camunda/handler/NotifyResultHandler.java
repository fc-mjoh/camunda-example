package com.reply.camunda.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@ExternalTaskSubscription("notifyResult")
@RequiredArgsConstructor
public class NotifyResultHandler implements ExternalTaskHandler {

    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {

        // Prozessvariablen auslesen
        List<String> shipmentIds = task.getVariable("shipmentIds");
        Boolean shipmentsAvailable = task.getVariable("shipmentsAvailable");

        // Ergebnis-Status ableiten
        String status = (shipmentsAvailable != null && shipmentsAvailable)
                ? "COMPLETED"
                : "NO_SHIPMENTS";

        String processInstanceId = task.getProcessInstanceId();
        String businessKey = task.getBusinessKey();

        log.info("[notifyResult] Prozess: {} | BusinessKey: {} | Status: {} | Shipments: {}",
                processInstanceId,
                businessKey,
                status,
                shipmentIds != null ? shipmentIds.size() : 0);

        try {
            // Ergebnis in externe DB persistieren
            persistResult(processInstanceId, businessKey, status, shipmentIds);

            service.complete(task);

        } catch (Exception e) {
            log.error("[notifyResult] Fehler beim Persistieren: {}", e.getMessage(), e);
            service.handleFailure(task,
                    "notifyResult fehlgeschlagen",
                    e.getMessage(),
                    3,
                    5000
            );
        }
    }

    /**
     * Persistiert das Prozessergebnis in die externe Fach-Datenbank.
     * In Produktion: JPA Repository, JDBC, REST-Call etc.
     */
    private void persistResult(String processInstanceId,
                               String businessKey,
                               String status,
                               List<String> shipmentIds) {

        // TODO: Echte Persistierung implementieren, z.B.:
        //   processRunRepository.save(ProcessRun.builder()
        //       .processInstanceId(processInstanceId)
        //       .businessKey(businessKey)
        //       .status(status)
        //       .shipmentCount(shipmentIds != null ? shipmentIds.size() : 0)
        //       .finishedAt(LocalDateTime.now())
        //       .build());

        log.info("[notifyResult] Persistiert → Status: {} | Anzahl Shipments: {} | Zeitpunkt: {}",
                status,
                shipmentIds != null ? shipmentIds.size() : 0,
                LocalDateTime.now());
    }
}
