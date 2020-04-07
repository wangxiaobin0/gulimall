package com.mall.product.service.impl;

import com.mall.common.to.SkuReductionTo;
import com.mall.common.to.SpuBoundTo;
import com.mall.product.entity.*;
import com.mall.product.feign.CouponFeignService;
import com.mall.product.service.*;
import com.mall.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveSpuInfoVo(SpuInfoVo spuInfoVo) {
        //1.保存Spu基本信息   pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        //复制属性
        BeanUtils.copyProperties(spuInfoVo, spuInfoEntity);
        //设置创建时间
        spuInfoEntity.setCreateTime(new Date());
        //设置更新时间
        spuInfoEntity.setUpdateTime(new Date());
        //保存
        saveSpuInfo(spuInfoEntity);

        //2.保存Spu描述图片   pms_spu_info_desc
        List<String> decripts = spuInfoVo.getDecript();
        //以,隔开
        String decript = StringUtils.join(decripts, ',');
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(decript);
        //保存
        saveSpuInfoDesc(spuInfoDescEntity);

        //3.保存Spu图片集     pms_spu_images
        List<String> images = spuInfoVo.getImages();
        saveSpuImages(spuInfoEntity.getId(), images);

        //4.保存Spu参数规格   pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuInfoVo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity entity = new ProductAttrValueEntity();
            entity.setSpuId(spuInfoEntity.getId());
            entity.setAttrId(attr.getAttrId());
            entity.setAttrName(attrService.getById(attr.getAttrId()).getAttrName());
            entity.setAttrValue(attr.getAttrValues());
            entity.setQuickShow(attr.getShowDesc());
            return entity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(collect);
        //5.保存Spu积分信息   sms_spu_bounds
        Bounds bounds = spuInfoVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        couponFeignService.saveBounds(spuBoundTo);

        //6.保存sku
        List<Skus> skus = spuInfoVo.getSkus();
        //6.1. 保存sku基本信息    pms_sku_info
        for (Skus sku : skus) {//由于下面保存需要用到skuId,所以不使用stream
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(sku, skuInfoEntity);
            skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
            skuInfoEntity.setBrandId(skuInfoEntity.getBrandId());
            skuInfoEntity.setSaleCount(0L);
            skuInfoEntity.setSpuId(spuInfoEntity.getId());

            List<Images> skuImages = sku.getImages();
            skuImages.forEach(img -> {
                if (img.getDefaultImg() == 1) {
                    skuInfoEntity.setSkuDefaultImg(img.getImgUrl());
                }
            });
            skuInfoService.saveSkuInfo(skuInfoEntity);
            //6.2. 保存sku图片集      pms_sku_images
            List<SkuImagesEntity> skuImgList = skuImages.stream().map(img -> {
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                BeanUtils.copyProperties(img, skuImagesEntity);
                skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                return skuImagesEntity;
            }).filter(entity-> {
                return !StringUtils.isEmpty(entity.getImgUrl());
            }).collect(Collectors.toList());
            skuImagesService.saveBatch(skuImgList);
            //6.3. 保存sku销售属性    pms_sku_sale_attr_value
            List<Attr> attrs = sku.getAttr();
            List<SkuSaleAttrValueEntity> skuAttrList = attrs.stream().map(attr -> {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                return skuSaleAttrValueEntity;
            }).collect(Collectors.toList());
            skuSaleAttrValueService.saveBatch(skuAttrList);

            //6.4. 保存sku满减信息    sms_sku_full_reduction
            //6.5. 保存sku折扣信息
            SkuReductionTo skuReductionTo = new SkuReductionTo();
            BeanUtils.copyProperties(sku, skuReductionTo);
            skuReductionTo.setSkuId(skuInfoEntity.getSkuId());
            if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) == 1) {
                couponFeignService.saveSkuReduction(skuReductionTo);
            }
        }

    }

    /**
     * 保存Spu图片集
     * @param id spuId
     * @param images 图片集
     */
    private void saveSpuImages(Long id, List<String> images) {
        spuImagesService.saveSpuImages(id, images);
    }

    /**
     *
     * @param spuInfoDescEntity spu图片详情
     */
    private void saveSpuInfoDesc(SpuInfoDescEntity spuInfoDescEntity) {
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);
    }

    /**
     * 保存Spu基本信息
     * @param spuInfoEntity Spu基本信息
     */
    private void saveSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.save(spuInfoEntity);
    }

}