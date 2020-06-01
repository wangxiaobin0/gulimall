package com.mall.ware.config;

import com.mall.common.constrant.MQConstant;
import com.mall.common.constrant.OrderMQConstant;
import com.mall.common.constrant.WareMQConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.GenericMessageConverter;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class WareMQConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @PostConstruct
    void rabbitTemplate() {
        //设置发布者到交换机的消息发送确认
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * publisher发布一条消息就会触发，无论成功失败
             * @param correlationData 消息的唯一id
             * @param b ack
             * @param s 消息投递失败原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                System.out.println("confirm...correlationData:" + correlationData + "ack:" + b + "cause：" + s);
            }
        });
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只有消息发送到队列失败时才会触发
             * @param message
             * @param replyCode
             * @param replyText
             * @param exchange
             * @param routingKey
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("message: " + message + "replyCode:" + replyCode + "replyText:" + replyText + "exchange:" + exchange + "routingKey:" + routingKey);
            }
        });
    }


    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    Exchange orderEventExchange() {
        Exchange exchange = new TopicExchange(WareMQConstant.WARE_EXCHANGE, true, false);
        return exchange;
    }

    @Bean
    Queue lockStockQueue() {
        Map<String, Object> map = new HashMap<>();
        map.put(MQConstant.MQ_DEAD_LETTER_EXCHANGE, WareMQConstant.DEAD_LETTER_EXCHANGE);
        map.put(MQConstant.MQ_DEAD_LETTER_ROUTING_KEY, WareMQConstant.DEAD_LETTER_ROUTING_KEY);
        map.put(MQConstant.MQ_DEAD_LETTER_TTL, 30000);
        Queue queue = new Queue(WareMQConstant.WARE_LOCK_STOCK_QUEUE, true, false, false, map);
        return queue;
    }

    @Bean
    Queue unlockStockQueue() {
        Queue queue = new Queue(WareMQConstant.WARE_UNLOCK_STOCK_QUEUE, true, false, false, null);
        return queue;
    }

    @Bean
    Binding lockToExchange() {
        Binding binding = new Binding(WareMQConstant.WARE_LOCK_STOCK_QUEUE,
                Binding.DestinationType.QUEUE,
                WareMQConstant.WARE_EXCHANGE,
                WareMQConstant.WARE_LOCK_ROUTING_KEY,
                null);
        return binding;
    }

    @Bean
    Binding unlockToExchange() {
        Binding binding = new Binding(WareMQConstant.WARE_UNLOCK_STOCK_QUEUE,
                Binding.DestinationType.QUEUE,
                WareMQConstant.WARE_EXCHANGE,
                WareMQConstant.WARE_UNLOCK_ROUTING_KEY,
                null);
        return binding;
    }

}
