package com.mall.order.feign;

import com.mall.common.utils.R;
import com.mall.order.vo.SpuInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@FeignClient("mall-product")
public interface ProductServiceFeign {
    @RequestMapping("/product/spuinfo/info/sku/{id}")
    R infoBySkuId(@PathVariable("id") Long id);
}
