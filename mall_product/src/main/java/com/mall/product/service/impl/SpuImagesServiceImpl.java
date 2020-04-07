package com.mall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.product.dao.SpuImagesDao;
import com.mall.product.entity.SpuImagesEntity;
import com.mall.product.service.SpuImagesService;


@Service("spuImagesService")
public class SpuImagesServiceImpl extends ServiceImpl<SpuImagesDao, SpuImagesEntity> implements SpuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuImagesEntity> page = this.page(
                new Query<SpuImagesEntity>().getPage(params),
                new QueryWrapper<SpuImagesEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSpuImages(Long id, List<String> images) {
        if (images == null && images.size() == 0) {
            return;
        }
        List<SpuImagesEntity> collect = images.stream().map(image -> {
            SpuImagesEntity entity = new SpuImagesEntity();
            entity.setSpuId(id);
            entity.setImgUrl(image);
            return entity;
        }).collect(Collectors.toList());
        this.saveBatch(collect);
    }

}