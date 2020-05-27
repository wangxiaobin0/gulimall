package com.mall.product.service.impl;

import com.mall.product.vo.web.SkuItemSaleAttrVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.product.dao.SkuSaleAttrValueDao;
import com.mall.product.entity.SkuSaleAttrValueEntity;
import com.mall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {
        SkuSaleAttrValueDao skuSaleAttrValueDao = this.baseMapper;
        List<SkuItemSaleAttrVo> saleAttrs = skuSaleAttrValueDao.getSaleAttrsBySpuId(spuId);
        return saleAttrs;
    }

    @Override
    public List<String> getSkuAttr(Long skuId) {
        List<String> attrs = this.baseMapper.getSkuAttrBySkuId(skuId);
        return attrs;
    }

}