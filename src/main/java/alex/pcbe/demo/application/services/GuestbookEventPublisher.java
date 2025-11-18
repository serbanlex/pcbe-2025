package alex.pcbe.demo.application.services;

import alex.pcbe.demo.domain.entities.GuestbookEntry;
import alex.pcbe.demo.domain.events.GuestbookEntryAddedEvent;
import alex.pcbe.demo.infrastructure.config.RabbitProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class GuestbookEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public GuestbookEventPublisher(RabbitTemplate rabbitTemplate, RabbitProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = properties.exchange();
        this.routingKey = properties.routingKey();
    }

    public void publishEntryAdded(GuestbookEntry entry) {
        GuestbookEntryAddedEvent event = new GuestbookEntryAddedEvent(
                entry.getId(),
                entry.getName(),
                entry.getMessage(),
                entry.getCreatedAt()
        );
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
