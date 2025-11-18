package alex.pcbe.demo.infrastructure.repositories;

import alex.pcbe.demo.application.repositories.GuestbookRepository;
import alex.pcbe.demo.domain.entities.GuestbookEntry;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGuestbookRepository implements GuestbookRepository {
    private final Map<String, GuestbookEntry> storage = new ConcurrentHashMap<>();

    @Override
    public GuestbookEntry save(GuestbookEntry entry) {
        storage.put(entry.getId(), entry);
        return entry;
    }

    @Override
    public List<GuestbookEntry> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<GuestbookEntry> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }
}

