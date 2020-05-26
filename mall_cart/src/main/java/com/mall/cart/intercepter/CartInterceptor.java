package com.mall.cart.intercepter;

import com.mall.cart.to.UserTo;
import com.mall.common.constrant.AuthConstant;
import com.mall.common.vo.MemberEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;


@Component
public class CartInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<UserTo> threadLocal = new ThreadLocal<>();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //UserTo,确定记录当前登录状态共后续处理流程使用，userId == null则未登录
        UserTo userTo = new UserTo();

        //login or not
        HttpSession session = request.getSession();
        MemberEntity loginUser = (MemberEntity) session.getAttribute(AuthConstant.LOGIN_USER);


        //已登录
        if (loginUser != null) {
            userTo.setUserId(loginUser.getId());
        }
        //获取user_key
        Cookie userKeyCookie = getUserKeyCookie(request.getCookies());
        //无论是否登录都判断是否为空，防止有人登录后把cookie删了，保证访问后必有一个user_key
        userTo.setUserKey(userKeyCookie == null ? UUID.randomUUID().toString() : userKeyCookie.getValue());

        //ThreadLocal存入userTo
        threadLocal.set(userTo);
        //无论是否登录都放行
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //设置user_key，有了就更新时间，没有就添加
        UserTo userTo = threadLocal.get();
        Cookie userKeyCookie = new Cookie(AuthConstant.USER_KEY, userTo.getUserKey());
        response.addCookie(userKeyCookie);
    }

    /**
     * 获取user_key Cookie
     * @param cookies
     * @return
     */
    private Cookie getUserKeyCookie(Cookie[] cookies) {
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(AuthConstant.USER_KEY)) {
                return cookie;
            }
        }
        return null;
    }
}
