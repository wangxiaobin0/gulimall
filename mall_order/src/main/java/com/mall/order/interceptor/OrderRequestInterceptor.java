package com.mall.order.interceptor;

import com.mall.common.constrant.AuthConstant;
import com.mall.common.vo.MemberEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Configuration
public class OrderRequestInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<MemberEntity> threadLocal = new ThreadLocal<>();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberEntity loginUser = (MemberEntity) session.getAttribute(AuthConstant.LOGIN_USER);
        String requestURI = request.getRequestURI();
        if (requestURI.equals("/order/order/orderSn")) {
            return true;
        }
        if (loginUser == null) {
            response.sendRedirect(AuthConstant.LOGIN_URL);
            return false;
        }
        threadLocal.set(loginUser);
        return true;
    }

    public static MemberEntity getLoginUser() {
        return threadLocal.get();
    }
}
