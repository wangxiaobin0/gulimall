package com.mall.cart.to;

import lombok.Data;

@Data
public class UserTo {
    /**
     * 登录用户的id
     */
    private Long userId;

    /**
     * 除第一次访问外，都会携带的user_key
     */
    private String userKey;
}
