package alex.pcbe.demo.domain.events;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

public record GuestbookEntryAddedEvent(
        String id,
        String name,
        String message,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        LocalDateTime createdAt
) implements Serializable {
}