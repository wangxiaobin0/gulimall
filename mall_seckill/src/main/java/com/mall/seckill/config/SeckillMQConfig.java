package com.mall.seckill.config;

import com.mall.common.constrant.MQConstant;
import com.mall.common.constrant.OrderMQConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SeckillMQConfig {

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
    Exchange orderEventExchange() {
        Exchange exchange = new TopicExchange(OrderMQConstant.ORDER_EXCHANGE, true, false);
        return exchange;
    }

    @Bean
    Queue createOrderQueue() {
        Map<String, Object> map = new HashMap<>();
        map.put(MQConstant.MQ_DEAD_LETTER_EXCHANGE, OrderMQConstant.DEAD_LETTER_EXCHANGE);
        map.put(MQConstant.MQ_DEAD_LETTER_ROUTING_KEY, OrderMQConstant.DEAD_LETTER_ROUTING_KEY);
        map.put(MQConstant.MQ_DEAD_LETTER_TTL, 20000);
        Queue queue = new Queue(OrderMQConstant.ORDER_CREATE_ORDER_QUEUE, true, false, false, map);
        return queue;
    }

    @Bean
    Queue cancelOrderQueue() {
        Queue queue = new Queue(OrderMQConstant.ORDER_CANCEL_ORDER_QUEUE, true, false, false, null);
        return queue;
    }

    @Bean
    Binding createToExchange() {
        Binding binding = new Binding(OrderMQConstant.ORDER_CREATE_ORDER_QUEUE,
                Binding.DestinationType.QUEUE,
                OrderMQConstant.ORDER_EXCHANGE,
                OrderMQConstant.ORDER_CREATE_ROUTING_KEY,
                null);
        return binding;
    }

    @Bean
    Binding cancelToExchange() {
        Binding binding = new Binding(OrderMQConstant.ORDER_CANCEL_ORDER_QUEUE,
                Binding.DestinationType.QUEUE,
                OrderMQConstant.ORDER_EXCHANGE,
                OrderMQConstant.ORDER_CANCEL_ROUTING_KEY,
                null);
        return binding;
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
