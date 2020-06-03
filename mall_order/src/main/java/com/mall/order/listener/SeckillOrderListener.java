package com.mall.order.listener;

import com.mall.common.constrant.OrderMQConstant;
import com.mall.common.to.SeckillOrderTo;
import com.mall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@RabbitListener(queues = {OrderMQConstant.ORDER_CREATE_SECKILL_ORDER_QUEUE})
public class SeckillOrderListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void createSeckillOrder(SeckillOrderTo orderTo, Message message, Channel channel) throws IOException {
        try {
            orderService.createSeckillOrder(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }
}
