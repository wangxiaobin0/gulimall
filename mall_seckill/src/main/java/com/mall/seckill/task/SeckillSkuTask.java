package com.mall.seckill.task;


import com.mall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
public class SeckillSkuTask {

    @Autowired
    SeckillService seckillService;

    /**
     * 预热秒杀商品
     */
    @Scheduled(cron = "*/3 * * * * ?")
    public void upTomorrowSeckill(){
        log.info("定时任务：上架明天的秒杀商品");
        seckillService.upTomorrowSeckill();
    }
}
