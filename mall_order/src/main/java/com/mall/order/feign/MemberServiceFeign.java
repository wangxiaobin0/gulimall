package com.mall.order.feign;


import com.mall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient("mall-member")
public interface MemberServiceFeign {

    @GetMapping("/member/memberreceiveaddress")
    List<MemberAddressVo> getAddressByMemberId();
}
