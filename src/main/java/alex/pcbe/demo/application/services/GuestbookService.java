package alex.pcbe.demo.application.services;

import alex.pcbe.demo.application.repositories.GuestbookRepository;
import alex.pcbe.demo.domain.entities.GuestbookEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GuestbookService {
    private final GuestbookRepository guestbookRepository;
    private final GuestbookEventPublisher eventPublisher;

    public GuestbookService(GuestbookRepository guestbookRepository, GuestbookEventPublisher eventPublisher) {
        this.guestbookRepository = guestbookRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public GuestbookEntry createEntry(String name, String message) {
        GuestbookEntry entry = new GuestbookEntry(name, message);
        GuestbookEntry saved = guestbookRepository.save(entry);
        eventPublisher.publishEntryAdded(saved);
        return saved;
    }

    public List<GuestbookEntry> getAllEntries() {
        return guestbookRepository.findAll();
    }
}
