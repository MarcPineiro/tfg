package edu.udg.tfg.FileManagement.config;

import edu.udg.tfg.FileManagement.queue.Receiver;
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


    public final static String DELETE_ELEMENT = "deleteElementQueue";
    public static final String ADD_ACCESS_QUEUE = "addAccessQueue";
    public static final String DELETE_ACCESS_QUEUE = "deleteAccessQueue";
    public final static String CONFIRM_DELETE = "confirmDelteTrash";
    public final static String TRASH_QUEUE_NAME = "trash";
    public final static String HISTORY_QUEUE = "history";
    public static final String DELETE_USER_FM = "fileManagerUserDelete";
    public final static String COMMAND_QUEUE = "commandQueue";

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("spring-boot-exchange");
    }

    @Bean
    Queue queue() {
        return new Queue(DELETE_ELEMENT, false);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(DELETE_ELEMENT);
    }


    @Bean
    Queue queueDeleteUser() {
        return new Queue(DELETE_USER_FM, false);
    }

    @Bean
    Binding bindingDeleteUser(@Qualifier("queueDeleteUser") Queue queueDeleteUser, TopicExchange exchange) {
        return BindingBuilder.bind(queueDeleteUser).to(exchange).with(DELETE_USER_FM);
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                             MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(DELETE_ELEMENT, DELETE_USER_FM);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }


}
