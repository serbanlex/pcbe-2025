package alex.pcbe.demo.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitMQConfig {

    private final RabbitProperties properties;

    public RabbitMQConfig(RabbitProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Queue temporaryQueue() {
        return QueueBuilder
                .nonDurable()
                .autoDelete()
                .build();
    }

    @Bean
    public FanoutExchange guestbookExchange() {
        return new FanoutExchange("guestbook.exchange");
    }

    @Bean
    public Binding binding(Queue temporaryQueue, FanoutExchange guestbookExchange) {
        return BindingBuilder.bind(temporaryQueue).to(guestbookExchange);
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
