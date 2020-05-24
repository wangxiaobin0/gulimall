package com.mall.thirdparty.controller;

import com.mall.common.utils.R;
import com.mall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsController {
    @Autowired
    SmsComponent smsComponent;

    @GetMapping("/code")
    public R sendSmsCode(@RequestParam("mobile") String mobile, @RequestParam("code") String code) {
        smsComponent.sendSms(mobile, code);
        return R.ok();
    }
}
