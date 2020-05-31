package com.mall.ware.service.impl;

import com.mall.ware.vo.SkuHasStockVo;
import com.mall.ware.vo.SkuStockVo;
import com.mall.ware.vo.SkuWareVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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

    @Resource
    WareSkuDao wareSkuDao;
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

}