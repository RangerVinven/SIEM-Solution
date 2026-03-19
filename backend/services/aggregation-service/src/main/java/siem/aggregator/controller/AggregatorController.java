package siem.aggregator.controller;

import lombok.RequiredArgsConstructor;
import siem.models.RawSiemEvent;
import siem.aggregator.service.EventPublisherService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AggregatorController {

    private final EventPublisherService service;

    @PostMapping("/aggregate")
    public void aggregate(@RequestBody List<RawSiemEvent> eventPayloadList) {
        service.publishEvent(eventPayloadList);
    }
}
