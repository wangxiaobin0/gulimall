package com.mall.seckill.interceptor;

import com.mall.common.constrant.AuthConstant;
import com.mall.common.vo.MemberEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class SeckillRequestInterceptor implements HandlerInterceptor {
    private static final ThreadLocal<MemberEntity> threadLocal = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberEntity attribute = (MemberEntity) session.getAttribute(AuthConstant.LOGIN_USER);
        if (attribute == null) {
            response.sendRedirect(AuthConstant.LOGIN_URL);
            return false;
        }
        attribute.setPassword(null);
        threadLocal.set(attribute);
        return true;
    }

    public MemberEntity getLoginUser() {
        return threadLocal.get();
    }
}
