package com.mall.auth.service.impl;

import com.mall.auth.constant.SmsConstant;
import com.mall.auth.feign.MemberServiceFeign;
import com.mall.auth.service.ILoginService;
import com.mall.auth.vo.LoginVo;
import com.mall.auth.vo.RegisterVo;
import com.mall.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LoginServiceImpl implements ILoginService {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberServiceFeign memberServiceFeign;

    @Override
    public void register(RegisterVo registerVo) {
        String mobileNo = registerVo.getMobileNo();
        String code = registerVo.getCode();
        String redis_code = redisTemplate.opsForValue().get(SmsConstant.SMS_CODE_PREFIX + mobileNo);
        if (StringUtils.isEmpty(redis_code)) {
            //throw new RuntimeException("请获取短信验证码");
        } else {
            if (!redis_code.split("_")[0].equals(code)) {
                throw new RuntimeException("验证码错误");
            }
        }

        redisTemplate.delete(SmsConstant.SMS_CODE_PREFIX + mobileNo);
        R save = memberServiceFeign.save(registerVo);
        if (save.get("code", Integer.class) != 0) {
            throw new RuntimeException(save.get("msg", String.class));
        }
    }

    @Override
    public void login(LoginVo loginVo) {
        R login = memberServiceFeign.login(loginVo);
        Integer code = login.get("code", Integer.class);
        if (code != 0) {
            throw new RuntimeException(login.get("msg", String.class));
        }
    }
}
