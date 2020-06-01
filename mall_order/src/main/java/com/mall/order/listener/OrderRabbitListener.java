package com.mall.order.listener;

import com.mall.common.constrant.OrderMQConstant;
import com.mall.order.entity.OrderEntity;
import com.mall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@RabbitListener(queues = {OrderMQConstant.ORDER_CANCEL_ORDER_QUEUE})
public class OrderRabbitListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void cancelOrder(OrderEntity orderEntity, Message message, Channel channel) throws IOException {
        try {
            orderService.cancelOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            e.printStackTrace();
        }
    }
}
