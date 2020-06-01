package com.mall.ware.listener;

import com.mall.common.constrant.WareMQConstant;
import com.mall.common.to.OrderTo;
import com.mall.ware.entity.WareOrderTaskDetailEntity;
import com.mall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RabbitListener(queues = {WareMQConstant.WARE_UNLOCK_STOCK_QUEUE})
public class WareRabbitListener {

    @Autowired
    WareSkuService wareSkuService;

    /**
     * 延时补偿
     */
    @RabbitHandler
    public void unlockByTask(WareOrderTaskDetailEntity taskDetailEntity, Message message, Channel channel) throws IOException {
        try {
            wareSkuService.unlock(taskDetailEntity);
            log.info("unlockByTask补偿成功");
            //补偿成功，手动ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            //出现异常，补偿失败，重新加入队列中
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            e.printStackTrace();
        }
    }
    @RabbitHandler
    public void unlockByOrder(OrderTo orderTo, Message message, Channel channel) throws IOException {
        try {
            wareSkuService.unlock(orderTo);
            log.info("unlockByOrder补偿成功");
            //补偿成功，手动ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            //出现异常，补偿失败，重新加入队列中
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            e.printStackTrace();
        }
    }
}
