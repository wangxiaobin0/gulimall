package com.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.ware.entity.PurchaseEntity;
import com.mall.ware.vo.MergeVo;
import com.mall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:44:05
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils unReceiveList(Map<String, Object> params);

    void merge(MergeVo mergeVo);

    void receivedPurchase(List<Long> ids);


    void donePurchase(PurchaseDoneVo purchaseDoneVo);
}

