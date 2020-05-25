package com.mall.auth.feign;

import com.mall.auth.vo.LoginVo;
import com.mall.auth.vo.RegisterVo;
import com.mall.auth.vo.WeiBoInfoVo;
import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("mall-member")
public interface MemberServiceFeign {
    @PostMapping("/member/member/save")
    R save(@RequestBody RegisterVo registerVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody LoginVo loginVo);

    @PostMapping("/member/member/login/weibo")
    R oAuthWeiBo(@RequestBody WeiBoInfoVo info);
}
