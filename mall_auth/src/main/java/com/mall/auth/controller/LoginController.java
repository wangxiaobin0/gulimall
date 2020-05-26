package com.mall.auth.controller;

import com.mall.auth.constant.SmsConstant;
import com.mall.auth.feign.ThirdPartFeign;
import com.mall.auth.service.ILoginService;
import com.mall.auth.vo.LoginVo;
import com.mall.auth.vo.RegisterVo;
import com.mall.common.constrant.AuthConstant;
import com.mall.common.utils.R;
import com.mall.common.vo.MemberEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ThirdPartFeign thirdPartFeign;

    @Autowired
    ILoginService loginService;

    @GetMapping("/sms/code")
    @ResponseBody
    public R sendCode(@RequestParam("mobile") String mobile) {
        String code = UUID.randomUUID().toString().substring(0, 5);
        String s = redisTemplate.opsForValue().get(SmsConstant.SMS_CODE_PREFIX + mobile);

        if (!StringUtils.isEmpty(s)) {
            Long time = Long.parseLong(s.split("_")[1]);
            if (System.currentTimeMillis() - time < 60 * 1000) {
                return R.error(500, "1分钟内只能获取一次验证码");
            }
        }
        thirdPartFeign.sendSmsCode(mobile, code);
        //redis缓存
        redisTemplate.opsForValue().set(SmsConstant.SMS_CODE_PREFIX + mobile, code + "_" + System.currentTimeMillis(), 15, TimeUnit.MINUTES);
        return R.ok();
    }


    @PostMapping("/register")
    public String register(RegisterVo registerVo) {
        loginService.register(registerVo);
        return "redirect:http://auth.mall.com/login.html";
    }

    @PostMapping("/login")
    public String login(LoginVo loginVo, HttpSession session) {
        MemberEntity entity = loginService.login(loginVo);

        session.setAttribute(AuthConstant.LOGIN_USER, entity);
        return "redirect:http://mall.com";
    }
}
