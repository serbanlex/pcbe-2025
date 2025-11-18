package alex.pcbe.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestbookEntry {
    private String id;
    private String name;
    private String message;
    private LocalDateTime createdAt;

    public GuestbookEntry(String name, String message) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}


