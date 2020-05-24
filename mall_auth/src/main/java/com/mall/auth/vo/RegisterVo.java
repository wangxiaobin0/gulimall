package com.mall.auth.vo;

import lombok.Data;

@Data
public class RegisterVo {
    private String username;
    private String password;
    private String mobileNo;
    private String code;
}
