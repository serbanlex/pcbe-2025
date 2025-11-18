package alex.pcbe.demo.application.repositories;

import alex.pcbe.demo.domain.entities.GuestbookEntry;

import java.util.List;
import java.util.Optional;

public interface GuestbookRepository {
    GuestbookEntry save(GuestbookEntry entry);
    List<GuestbookEntry> findAll();
    Optional<GuestbookEntry> findById(String id);
}

