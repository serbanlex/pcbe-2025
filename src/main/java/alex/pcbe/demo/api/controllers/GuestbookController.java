package alex.pcbe.demo.api.controllers;

import alex.pcbe.demo.application.services.GuestbookService;
import alex.pcbe.demo.api.dto.CreateGuestbookEntryRequest;
import alex.pcbe.demo.domain.entities.GuestbookEntry;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/guestbook")
public class GuestbookController {
    private final GuestbookService guestbookService;

    public GuestbookController(GuestbookService guestbookService) {
        this.guestbookService = guestbookService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestbookEntry createEntry(@RequestBody CreateGuestbookEntryRequest request) {
        return guestbookService.createEntry(request.getName(), request.getMessage());
    }

    @GetMapping
    public List<GuestbookEntry> getAllEntries() {
        return guestbookService.getAllEntries();
    }
}
