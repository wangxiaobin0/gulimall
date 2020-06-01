package com.mall.ware.feign;

import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient("mall-order")
public interface OrderServiceFeign {
    @GetMapping("/order/order/orderSn")
    R getByOrderSn(@RequestParam("orderSn") String orderSn);
}
