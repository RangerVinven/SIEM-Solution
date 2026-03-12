package siem.aggregator.controller;

import lombok.RequiredArgsConstructor;
import siem.models.RawSiemEvent;
import siem.aggregator.service.EventPublisherService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AggregatorController {

    private final EventPublisherService service;

    @PostMapping("/v1/aggregate")
    public void aggregate(@RequestBody RawSiemEvent eventPayload) {
        service.publishEvent(eventPayload);
    }
}
