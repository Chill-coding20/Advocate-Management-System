package advocate.com.advocate_app.service;

import advocate.com.advocate_app.dto.TimelineEventResponseDTO;
import advocate.com.advocate_app.entity.CaseTimelineEvent;
import advocate.com.advocate_app.repository.CaseTimelineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CaseTimelineService {

    @Autowired
    private CaseTimelineRepository repository;

    /**
     * Central method to record a timeline event.
     * Every module calls this — no module creates timeline records directly.
     */
    public TimelineEventResponseDTO recordEvent(
            Long caseId,
            Long clientId,
            Long advocateId,
            String eventType,
            String title,
            String description,
            String performedBy,
            String referenceType,
            Long referenceId,
            Map<String, Object> metadataMap
    ) {
        EventIcon iconDef = getIconForEventType(eventType);

        CaseTimelineEvent event = new CaseTimelineEvent();
        event.setCaseId(caseId);
        event.setClientId(clientId);
        event.setAdvocateId(advocateId);
        event.setEventType(eventType);
        event.setTitle(title);
        event.setDescription(description);
        event.setIcon(iconDef.icon);
        event.setColor(iconDef.color);
        event.setPerformedBy(performedBy);
        event.setReferenceType(referenceType);
        event.setReferenceId(referenceId);
        event.setMetadata(metadataMap != null && !metadataMap.isEmpty()
                ? metadataMap.toString()
                : null);

        CaseTimelineEvent saved = repository.save(event);
        return toDTO(saved);
    }

    /** Convenience overload for events without metadata. */
    public TimelineEventResponseDTO recordEvent(
            Long caseId, Long clientId, Long advocateId,
            String eventType, String title, String description,
            String performedBy, String referenceType, Long referenceId) {
        return recordEvent(caseId, clientId, advocateId, eventType,
                title, description, performedBy, referenceType, referenceId, null);
    }

    /** Get paginated timeline for a case, newest first. */
    public Page<TimelineEventResponseDTO> getTimeline(
            Long caseId, int page, int size,
            List<String> eventTypeFilter,
            LocalDateTime dateFrom, LocalDateTime dateTo,
            String searchText) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CaseTimelineEvent> events;

        if (searchText != null && !searchText.isBlank()) {
            events = repository.findByCaseIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                    caseId, searchText, pageable);
        } else if (eventTypeFilter != null && !eventTypeFilter.isEmpty()) {
            events = repository.findByCaseIdAndEventTypeInOrderByCreatedAtDesc(
                    caseId, eventTypeFilter, pageable);
        } else if (dateFrom != null && dateTo != null) {
            events = repository.findByCaseIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    caseId, dateFrom, dateTo, pageable);
        } else {
            events = repository.findByCaseIdOrderByCreatedAtDesc(caseId, pageable);
        }

        return events.map(this::toDTO);
    }

    /** Get all timeline events for a case (no pagination). */
    public List<TimelineEventResponseDTO> getAllTimeline(Long caseId) {
        return repository.findByCaseIdOrderByCreatedAtDesc(caseId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ===== Event Type Constants =====

    public static final String CASE_CREATED         = "CASE_CREATED";
    public static final String CASE_UPDATED          = "CASE_UPDATED";
    public static final String CASE_STATUS_CHANGED   = "CASE_STATUS_CHANGED";
    public static final String CASE_CLOSED           = "CASE_CLOSED";
    public static final String CASE_REOPENED         = "CASE_REOPENED";
    public static final String CLIENT_ASSIGNED       = "CLIENT_ASSIGNED";
    public static final String HEARING_CREATED       = "HEARING_CREATED";
    public static final String HEARING_UPDATED       = "HEARING_UPDATED";
    public static final String HEARING_RESCHEDULED   = "HEARING_RESCHEDULED";
    public static final String HEARING_COMPLETED     = "HEARING_COMPLETED";
    public static final String DOCUMENT_UPLOADED     = "DOCUMENT_UPLOADED";
    public static final String DOCUMENT_DELETED      = "DOCUMENT_DELETED";
    public static final String PAYMENT_RECEIVED      = "PAYMENT_RECEIVED";
    public static final String PAYMENT_UPDATED       = "PAYMENT_UPDATED";
    public static final String PAYMENT_DELETED       = "PAYMENT_DELETED";
    public static final String EXPENSE_ADDED         = "EXPENSE_ADDED";
    public static final String EXPENSE_UPDATED       = "EXPENSE_UPDATED";
    public static final String EXPENSE_DELETED       = "EXPENSE_DELETED";
    public static final String INVOICE_GENERATED     = "INVOICE_GENERATED";
    public static final String INVOICE_PAID          = "INVOICE_PAID";
    public static final String EMAIL_SENT            = "EMAIL_SENT";
    public static final String WHATSAPP_SENT         = "WHATSAPP_SENT";
    public static final String NOTE_ADDED            = "NOTE_ADDED";

    // ===== Icon & Color Mapping =====

    private record EventIcon(String icon, String color) {}

    private EventIcon getIconForEventType(String type) {
        return switch (type) {
            case CASE_CREATED, CASE_UPDATED, CASE_STATUS_CHANGED, CASE_REOPENED ->
                    new EventIcon("📋", "#6366F1");
            case CASE_CLOSED       -> new EventIcon("🔒", "#EF4444");
            case CLIENT_ASSIGNED   -> new EventIcon("👤", "#8B5CF6");
            case HEARING_CREATED, HEARING_UPDATED, HEARING_RESCHEDULED, HEARING_COMPLETED ->
                    new EventIcon("📅", "#A855F7");
            case DOCUMENT_UPLOADED, DOCUMENT_DELETED ->
                    new EventIcon("📄", "#14B8A6");
            case PAYMENT_RECEIVED, PAYMENT_UPDATED, PAYMENT_DELETED ->
                    new EventIcon("💰", "#22C55E");
            case EXPENSE_ADDED, EXPENSE_UPDATED, EXPENSE_DELETED ->
                    new EventIcon("💸", "#F97316");
            case INVOICE_GENERATED, INVOICE_PAID ->
                    new EventIcon("🧾", "#4F46E5");
            case EMAIL_SENT       -> new EventIcon("📧", "#3B82F6");
            case WHATSAPP_SENT    -> new EventIcon("💬", "#25D366");
            case NOTE_ADDED       -> new EventIcon("📝", "#F59E0B");
            default               -> new EventIcon("📌", "#94A3B8");
        };
    }

    private TimelineEventResponseDTO toDTO(CaseTimelineEvent event) {
        TimelineEventResponseDTO dto = new TimelineEventResponseDTO();
        dto.setId(event.getId());
        dto.setCaseId(event.getCaseId());
        dto.setClientId(event.getClientId());
        dto.setEventType(event.getEventType());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setIcon(event.getIcon());
        dto.setColor(event.getColor());
        dto.setReferenceType(event.getReferenceType());
        dto.setReferenceId(event.getReferenceId());
        dto.setPerformedBy(event.getPerformedBy());
        dto.setMetadata(event.getMetadata());
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }
}
