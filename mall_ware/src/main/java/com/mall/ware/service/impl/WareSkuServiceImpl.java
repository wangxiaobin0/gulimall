package com.mall.ware.service.impl;

import com.mall.common.constrant.WareMQConstant;
import com.mall.common.to.OrderTo;
import com.mall.common.utils.R;
import com.mall.ware.entity.WareOrderTaskDetailEntity;
import com.mall.ware.entity.WareOrderTaskEntity;
import com.mall.ware.enume.OrderStatusEnum;
import com.mall.ware.feign.OrderServiceFeign;
import com.mall.ware.service.WareOrderTaskDetailService;
import com.mall.ware.service.WareOrderTaskService;
import com.mall.ware.vo.SkuHasStockVo;
import com.mall.ware.vo.SkuStockVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.ware.dao.WareSkuDao;
import com.mall.ware.entity.WareSkuEntity;
import com.mall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    OrderServiceFeign orderServiceFeign;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Resource
    WareSkuDao wareSkuDao;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String wareId = (String) params.get("wareId");
        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        if (StringUtils.isNotEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public void addStock(Long wareId, Long skuId, Integer skuNum) {
        WareSkuEntity wareSkuEntity = new WareSkuEntity();
        wareSkuEntity.setWareId(wareId);
        wareSkuEntity.setSkuId(skuId);
        wareSkuEntity.setStock(skuNum);
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        queryWrapper.eq("ware_id", wareId);
        WareSkuEntity entity = wareSkuDao.selectOne(queryWrapper);
        if (entity == null) {
            this.save(wareSkuEntity);
        } else {
            entity.setStock(entity.getStock() + skuNum);
            this.updateById(entity);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuId) {
        List<SkuHasStockVo> collect = skuId.stream().map(id -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            Long count = wareSkuDao.getSkuStock(id);
            skuHasStockVo.setSkuId(id);
            skuHasStockVo.setHasStock(count != null && count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    @Transactional
    public Boolean lockStock(List<SkuStockVo> skuStockVos) {
        WareOrderTaskEntity orderTaskEntity = new WareOrderTaskEntity();
        orderTaskEntity.setOrderSn(skuStockVos.get(0).getOrderSn());
        wareOrderTaskService.save(orderTaskEntity);

        for (SkuStockVo skuStockVo : skuStockVos) {
            Long skuId = skuStockVo.getSkuId();
            Integer num = skuStockVo.getNum();
            //查询有库存的仓库id
            List<Long> wareIds = this.baseMapper.getWareIdBySku(skuId, num);
            if (wareIds == null || wareIds.isEmpty()) {
                log.info("商品：{}库存不足", skuId);
                throw new RuntimeException("商品：" + skuId + "库存不足");
            }
            //锁定库存
            Boolean locked = false;
            for (Long wareId : wareIds) {
                Integer stock = this.baseMapper.lockStock(wareId, skuId, num);
                if (stock == 1) {
                    locked = true;
                    //保存任务详情
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
                    taskDetailEntity.setSkuId(skuStockVo.getSkuId());
                    taskDetailEntity.setSkuNum(skuStockVo.getNum());
                    taskDetailEntity.setTaskId(orderTaskEntity.getId());
                    taskDetailEntity.setWareId(wareId);
                    taskDetailEntity.setLockStatus(1);
                    wareOrderTaskDetailService.save(taskDetailEntity);
                    rabbitTemplate.convertAndSend(WareMQConstant.WARE_EXCHANGE, WareMQConstant.WARE_LOCK_ROUTING_KEY, taskDetailEntity);

                    log.info("锁库存成功。skuId:{},num:{},wareId:{}", skuId, num, wareId);
                    break;
                }
            }
            if (!locked) {
                log.info("商品：{}库存不足", skuId);
                throw new RuntimeException("商品：" + skuId + "库存不足");
            }
        }
        return true;
    }

    @Override
    public void unlock(WareOrderTaskDetailEntity taskDetailEntity) {
        //获取任务
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(taskDetailEntity.getTaskId());

        //任务为空的话，说明后面发生了异常，已经回滚不需要补偿
        //任务不为空时才进行补偿
        if (taskEntity !=null){
            //根据订单id获取订单
            String orderSn = taskEntity.getOrderSn();
            R r = orderServiceFeign.getByOrderSn(orderSn);
            OrderTo orderVo = r.get("orderInfo", OrderTo.class);
            //订单为空，说明订单没创建出来，此时库存已经减了，必须补偿
            //订单已取消，必须补偿
            if (orderVo == null || orderVo.getStatus() == OrderStatusEnum.CANCLED.getCode()) {
                //获取最新的任务详情，未解锁时才解锁，避免重复消费导致多次补偿
                WareOrderTaskDetailEntity newDetail = wareOrderTaskDetailService.getById(taskDetailEntity.getId());
                if (newDetail.getLockStatus() == 1) {
                    unlockStock(newDetail.getSkuId(), newDetail.getWareId(), newDetail.getSkuNum(), newDetail.getId());
                } else {
                    log.info("补偿已补偿过了。skuId:{},num:{},wareId:{}", newDetail.getSkuId(), newDetail.getSkuNum(), newDetail.getWareId());
                }
            }
        }


    }

    @Override
    @Transactional
    public void unlock(OrderTo orderVo) {
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderVo.getOrderSn()));
        if (taskEntity != null) {
            List<WareOrderTaskDetailEntity> taskDetailEntities = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", taskEntity.getId()));
            for (WareOrderTaskDetailEntity detailEntity : taskDetailEntities) {
                unlockStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum(),detailEntity.getId());
            }
        }
    }

    private void unlockStock(Long skuId, Long wareId, Integer skuNum, Long taskDetailId) {
        Integer integer = wareSkuDao.unlockStock(wareId, skuId, skuNum);
        if (integer != 1) {
            log.info("补偿失败。skuId:{},num:{},wareId:{}", skuId, skuNum, wareId);
            throw new RuntimeException("补偿失败");
        }
        //更新任务详情的锁定状态
        WareOrderTaskDetailEntity afterUnlock = new WareOrderTaskDetailEntity();
        afterUnlock.setId(taskDetailId);
        afterUnlock.setLockStatus(2);
        wareOrderTaskDetailService.updateById(afterUnlock);
        log.info("补偿成功！！！skuId:{},num:{},wareId:{}", skuId, skuNum, wareId);
    }

}