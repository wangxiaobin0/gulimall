package com.mall.ware.service.impl;

import com.mall.common.constrant.WareConstant;
import com.mall.ware.entity.PurchaseDetailEntity;
import com.mall.ware.service.PurchaseDetailService;
import com.mall.ware.service.WareSkuService;
import com.mall.ware.vo.ItemVo;
import com.mall.ware.vo.MergeVo;
import com.mall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.ware.dao.PurchaseDao;
import com.mall.ware.entity.PurchaseEntity;
import com.mall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils unReceiveList(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        //查询新建和未领取的采购单
        queryWrapper.eq("status", "0").or().eq("status", "1");
        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void merge(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        //采购单不存在就新建一个
        if ( purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Override
    @Transactional
    public void receivedPurchase(List<Long> ids) {
        // 1.查询新建和已分配的采购单
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            //筛选新建和已分配的采购单
            return item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode();
        }).map(item -> {
            //更新状态
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            //更新updateTIME
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());
        //更新状态
        this.updateBatchById(collect);

        collect.forEach(item -> {
            List<PurchaseDetailEntity> list = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> collect1 = list.stream().map(l -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(l.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect1);
        });
    }

    @Override
    @Transactional
    public void donePurchase(PurchaseDoneVo purchaseDoneVo) {
        //1. 更新采购项的状态
        List<ItemVo> items = purchaseDoneVo.getItems();
        Boolean success = true;

        List<PurchaseDetailEntity> upodates = new ArrayList<>();
        for (ItemVo item : items) {
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.FAILED.getCode()){
                success = false;
            }
            //更新采购项
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item.getItemId());
            purchaseDetailEntity.setStatus(item.getStatus());
            upodates.add(purchaseDetailEntity);

            PurchaseDetailEntity byId = purchaseDetailService.getById(item.getItemId());
            //更新sku库存信息. 哪个仓库? 哪个sku? 数量多少?
            wareSkuService.addStock(byId.getWareId(), byId.getSkuId(), byId.getSkuNum());
        }
        purchaseDetailService.updateBatchById(upodates);
        //2. 更新采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseDoneVo.getId());
        purchaseEntity.setStatus(success ? WareConstant.PurchaseStatusEnum.FINISHED.getCode() : WareConstant.PurchaseStatusEnum.FAILED.getCode());
        this.updateById(purchaseEntity);
    }

}