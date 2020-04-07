package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.SpuInfoEntity;
import com.mall.product.vo.SpuInfoVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:42:31
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfoVo(SpuInfoVo spuInfoVo);
}

