package alex.pcbe.demo.infrastructure.mappers;

import alex.pcbe.demo.domain.entities.GuestbookEntry;
import alex.pcbe.demo.infrastructure.db.GuestbookEntryEntity;
import org.springframework.stereotype.Component;

@Component
public class GuestbookEntryMapper {

    public GuestbookEntryEntity toEntity(GuestbookEntry domain) {
        if (domain == null) {
            return null;
        }
        return new GuestbookEntryEntity(
                domain.getId(),
                domain.getName(),
                domain.getMessage(),
                domain.getCreatedAt()
        );
    }

    public GuestbookEntry toDomain(GuestbookEntryEntity entity) {
        if (entity == null) {
            return null;
        }
        return new GuestbookEntry(
                entity.getId(),
                entity.getName(),
                entity.getMessage(),
                entity.getCreatedAt()
        );
    }
}

