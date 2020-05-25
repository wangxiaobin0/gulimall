package com.mall.auth.controller;

import com.mall.auth.service.IOAuthService;
import com.mall.common.vo.MemberEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class OAuthController {

    @Autowired
    IOAuthService oAuthService;

    @GetMapping("/oauth2/weibo")
    public String oauthLogin(@RequestParam("code") String code, HttpSession session) throws Exception {
        MemberEntity entity = oAuthService.oauthLogin(code);
        session.setAttribute("user", entity);
        System.out.println(entity);
        return "redirect:http://mall.com";
    }
}
