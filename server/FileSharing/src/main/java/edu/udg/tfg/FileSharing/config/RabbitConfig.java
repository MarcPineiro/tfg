package edu.udg.tfg.FileSharing.config;

import edu.udg.tfg.FileSharing.queue.Receiver;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {


    public final static String DELTE_SHARING = "deleteSharing";
    public final static String CONFIRM_DELETE = "confirmDelteTrash";
    public final static String HISTORY_QUEUE = "history";
    public static final String DELETE_USER_FS = "fileSharingUserDelete";

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("spring-boot-exchange");
    }

    @Bean
    Queue queue() {
        return new Queue(DELTE_SHARING, false);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(DELTE_SHARING);
    }


    @Bean
    Queue queueDeleteUser() {
        return new Queue(DELETE_USER_FS, false);
    }

    @Bean
    Binding bindingDeleteUser(@Qualifier("queueDeleteUser") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(DELETE_USER_FS);
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                             MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(DELTE_SHARING, DELETE_USER_FS);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }


}
