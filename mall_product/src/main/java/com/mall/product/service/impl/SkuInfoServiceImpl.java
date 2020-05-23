package com.mall.product.service.impl;

import com.mall.common.utils.R;
import com.mall.product.entity.SkuImagesEntity;
import com.mall.product.entity.SpuInfoDescEntity;
import com.mall.product.feign.WareFeignService;
import com.mall.product.service.*;
import com.mall.product.vo.web.SkuItemSaleAttrVo;
import com.mall.product.vo.web.SkuItemVo;
import com.mall.product.vo.web.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.product.dao.SkuInfoDao;
import com.mall.product.entity.SkuInfoEntity;

import javax.annotation.Resource;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.save(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        String catelogId = (String) params.get("catelogId");
        String brandId = (String) params.get("brandId");
        String min = (String) params.get("min");
        String max = (String) params.get("max");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("sku_id", key).like("sku_name", key);
            });
        }
        if (StringUtils.isNotEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        if (StringUtils.isNotEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        if (StringUtils.isNotEmpty(max)) {
            BigDecimal b = new BigDecimal(max);
            if (b.compareTo(new BigDecimal(0)) == 1) {
                queryWrapper.le("sku_price", max);
            }
        }

        if (StringUtils.isNotEmpty(min)) {
            BigDecimal b = new BigDecimal(min);
            if (b.compareTo(new BigDecimal(0)) == 1) {
                queryWrapper.gt("sku_price", min);
            }
        }

        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkuListBySpuId(Long spuId) {
        List<SkuInfoEntity> skuInfoEntities = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return skuInfoEntities;
    }

    @Override
    public SkuItemVo getItemInfo(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo vo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            //Sku信息
            SkuInfoEntity info = this.getById(skuId);
            vo.setInfo(info);
            return info;
        }, executor);

        CompletableFuture<Void> imagesFuture = skuInfoFuture.thenAcceptAsync((res) -> {
            //Sku图片集合
            List<SkuImagesEntity> images = skuImagesService.getImagesBySpuId(res.getSpuId());
            vo.setImages(images);
        }, executor);

        CompletableFuture<Void> saleAttrsFuture = skuInfoFuture.thenAcceptAsync((res) -> {
            //Spu销售属性
            List<SkuItemSaleAttrVo> saleAttrs = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            vo.setSaleAttrs(saleAttrs);
        }, executor);

        CompletableFuture<Void> attrGroupsFuture = skuInfoFuture.thenAcceptAsync((res) -> {
            //Spu规格参数
            List<SpuItemAttrGroupVo> groupAttrs = attrGroupService.getGroupVoBySpuId(res.getSpuId());
            vo.setGroupAttrs(groupAttrs);
        }, executor);

        CompletableFuture<Void> spuDescFuture = skuInfoFuture.thenAcceptAsync((res) -> {
            //Spu详情
            SpuInfoDescEntity descEntity = spuInfoDescService.getById(res.getSpuId());
            vo.setDesc(descEntity);
        }, executor);
        //阻塞等待 图片、销售属性、规格参数、spu详情异步加载完毕再返回vo
        CompletableFuture.allOf(imagesFuture, saleAttrsFuture, attrGroupsFuture, spuDescFuture).get();
        return vo;
    }

}