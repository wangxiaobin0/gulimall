package com.mall.auth.feign;

import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("mall-third-party")
public interface ThirdPartFeign {
    @GetMapping("/sms/code")
    R sendSmsCode(@RequestParam("mobile") String mobile, @RequestParam("code") String code);
}
