package siem.normalisation.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import siem.models.RawSiemEvent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import siem.normalisation.repository.NormalisationMappingRepository;
import siem.normalisation.entity.NormalisationMapping;
import java.util.Optional;

@Slf4j
@Service
public class NormalisationService {

    private final KafkaTemplate<String, RawSiemEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final NormalisationMappingRepository mappingRepository;

    public NormalisationService(
            KafkaTemplate<String, RawSiemEvent> kafkaTemplate,
            NormalisationMappingRepository mappingRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.mappingRepository = mappingRepository;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "raw-logs", groupId = "normalisation-service")
    public void normaliseLogs(RawSiemEvent event) {
        RawSiemEvent normalisedEvent = event;

        if (event.event().dataset() != null) {
            normalisedEvent = mapEventFields(event);
        }

        kafkaTemplate.send("normalised-logs", normalisedEvent);
    }

    // Gets the event id from the raw log, and sets the event category (i.e "authentication"), the action (i.e, "logon-failed"), and the outcome (i.e, "failure")
    private RawSiemEvent mapEventFields(RawSiemEvent event) {
        try {
            String sourceId = extractSourceId(event);
            if (sourceId == null) return event;

            // Gets the event mappings
            Optional<NormalisationMapping> mappingOpt = mappingRepository.findBySourceDatasetAndSourceId(
                event.event().dataset(), sourceId);

            if (mappingOpt.isEmpty()) return event;

            NormalisationMapping mapping = mappingOpt.get();

            String level = mapping.getTargetLevel() != null
                    ? mapping.getTargetLevel()
                    : (event.log() != null ? event.log().level() : null);

            RawSiemEvent.Log updatedLog = new RawSiemEvent.Log(
                    event.log() != null ? event.log().filePath() : null,
                    level
            );

            // Creates a new event with the new mapping (new category, action, and outcome)
            return new RawSiemEvent(
                event.timestamp(),
                event.schoolId(),
                new RawSiemEvent.Event(
                    event.event().id(),
                    event.event().kind(),
                    mapping.getTargetCategory() != null ? mapping.getTargetCategory() : event.event().category(),
                    event.event().type(),
                    event.event().dataset(),
                    event.event().provider(),
                    mapping.getTargetAction() != null ? mapping.getTargetAction() : event.event().action(),
                    mapping.getTargetOutcome() != null ? mapping.getTargetOutcome() : event.event().outcome(),
                    event.event().original()
                ),
                event.host(),
                event.message(),
                updatedLog,
                event.tags(),
                event.labels()
            );

        } catch (Exception e) {
            return event;
        }
    }

    private String extractSourceId(RawSiemEvent event) {
        try {
            if ("windows.event".equals(event.event().dataset())) {
                JsonNode windowsEvent = objectMapper.readTree(event.event().original());
                return windowsEvent.get("Id").asText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
