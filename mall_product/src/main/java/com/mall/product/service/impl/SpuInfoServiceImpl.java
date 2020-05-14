package com.mall.product.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.common.constrant.ProductConstant;
import com.mall.common.to.SkuEsModel;
import com.mall.common.to.SkuReductionTo;
import com.mall.common.to.SpuBoundTo;
import com.mall.common.utils.R;
import com.mall.product.entity.*;
import com.mall.product.feign.CouponFeignService;
import com.mall.product.feign.ProductSearchFeignService;
import com.mall.product.feign.WareFeignService;
import com.mall.product.service.*;
import com.mall.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

import javax.annotation.Resource;


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

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Resource
    ProductSearchFeignService productSearchFeignService;

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
            skuInfoEntity.setPrice(new BigDecimal(sku.getPrice()));
            skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());

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
            }).filter(entity -> {
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

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        //状态. 1.上架 2.下架
        String status = (String) params.get("status");
        String key = (String) params.get("key");
        String catelogId = (String) params.get("catelogId");
        String brandId = (String) params.get("brandId");
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(status)) {
                queryWrapper.eq("publish_status", status);
        }
        if (StringUtils.isNotEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
                queryWrapper.eq("catalog_id", catelogId);
        }
        if (StringUtils.isNotEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
                queryWrapper.eq("brand_id", brandId);
        }
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("id", key).like("spu_name", key);
            });
        }
        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public Boolean up(Long spuId) throws IOException {

        //查询所有sku
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkuListBySpuId(spuId);

        List<Long> skuIdList = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //sku的所有参数信息
        List<ProductAttrValueEntity> attrListForSpu = attrService.getAttrListForSpu(spuId);
        //sku的id集合
        List<Long> attrIdList = attrListForSpu.stream().map(productAttrValueEntity -> {
            return productAttrValueEntity.getAttrId();
        }).collect(Collectors.toList());


        /*
        用于搜索的属性
         */
        //可以用于搜索的skuId集合
        List<Long> searchAttrId = attrService.getSearchAttrList(attrIdList);
        //过滤掉不用作搜索的参数
        List<SkuEsModel.Attrs> attrsList = attrListForSpu.stream().filter(productAttrValueEntity -> {
            return searchAttrId.contains(productAttrValueEntity.getAttrId());
        //返回attrs对象
        }).map(item -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());

        /*
        sku库存
         */
        R stock = wareFeignService.getSkuHasStock(skuIdList);
        List<SkuHasStockVo> skuHasStockVos = (List<SkuHasStockVo> )stock.get("skuHasStockVoList", new TypeReference<List<SkuHasStockVo>>() {});
        Map<Long, Boolean> skuStock = skuHasStockVos.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));

        List<SkuEsModel> skuEsModelList = skuInfoEntities.stream().map(skuInfoEntity -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(skuInfoEntity, skuEsModel);
            skuEsModel.setSkuPrice(skuInfoEntity.getPrice());
            skuEsModel.setSkuImg(skuInfoEntity.getSkuDefaultImg());
            skuEsModel.setCategoryId(skuInfoEntity.getCatalogId());
            //是否有库存
            skuEsModel.setHasStock(skuStock.get(skuInfoEntity.getSkuId()));
            //搜索分数
            skuEsModel.setHotScore(0L);
            //品牌名称
            BrandEntity brandEntity = brandService.getById(skuInfoEntity.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            //品牌图片
            skuEsModel.setBrandImg(brandEntity.getLogo());
            //分类名称
            CategoryEntity categoryEntity = categoryService.getById(skuInfoEntity.getCatalogId());
            skuEsModel.setCategoryName(categoryEntity.getName());

            //设置检索属性
            skuEsModel.setAttrs(attrsList);
            return skuEsModel;
        }).collect(Collectors.toList());
        //录入es
        R saveResponse = productSearchFeignService.save(skuEsModelList);
        //修改spu状态为已上架
        SpuInfoEntity entity = new SpuInfoEntity();
        entity.setUpdateTime(new Date());
        entity.setId(spuId);
        entity.setPublishStatus(ProductConstant.ProductUpEnum.PRODUCT_UP.getCode());
        this.updateById(entity);
        return (Integer) saveResponse.get("code") == 0;
    }

    /**
     * 保存Spu图片集
     *
     * @param id     spuId
     * @param images 图片集
     */
    private void saveSpuImages(Long id, List<String> images) {
        spuImagesService.saveSpuImages(id, images);
    }

    /**
     * @param spuInfoDescEntity spu图片详情
     */
    private void saveSpuInfoDesc(SpuInfoDescEntity spuInfoDescEntity) {
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);
    }

    /**
     * 保存Spu基本信息
     *
     * @param spuInfoEntity Spu基本信息
     */
    private void saveSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.save(spuInfoEntity);
    }

}