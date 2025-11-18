package alex.pcbe.demo.application.services;

import alex.pcbe.demo.application.repositories.GuestbookRepository;
import alex.pcbe.demo.domain.entities.GuestbookEntry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuestbookService {
    private final GuestbookRepository guestbookRepository;

    public GuestbookService(GuestbookRepository guestbookRepository) {
        this.guestbookRepository = guestbookRepository;
    }

    public GuestbookEntry createEntry(String name, String message) {
        GuestbookEntry entry = new GuestbookEntry(name, message);
        return guestbookRepository.save(entry);
    }

    public List<GuestbookEntry> getAllEntries() {
        return guestbookRepository.findAll();
    }
}
