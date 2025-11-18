package alex.pcbe.demo.application.services;

import alex.pcbe.demo.application.repositories.GuestbookRepository;
import alex.pcbe.demo.domain.entities.GuestbookEntry;
import alex.pcbe.demo.domain.events.GuestbookEntryAddedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class GuestbookEventListener {
    private static final Logger logger = LoggerFactory.getLogger(GuestbookEventListener.class);

    @RabbitListener(queues = "#{temporaryQueue.name}")
    public void onEntryAdded(GuestbookEntryAddedEvent event) {
        logger.info(
                "Received GuestbookEntryAddedEvent: id={}, name={}, message={}, createdAt={}",
                event.id(), event.name(), event.message(), event.createdAt()
        );

    }
}

