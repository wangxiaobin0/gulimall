package com.mall.order.feign;

import com.mall.common.utils.R;
import com.mall.order.vo.SkuStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("mall-ware")
public interface WareServiceFeign {
    @PostMapping("/ware/waresku/sku/stock")
    R lockStock(@RequestBody List<SkuStockVo> skuStockVos);
}
