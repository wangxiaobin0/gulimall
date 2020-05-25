package com.mall.member.vo;

import lombok.Data;

@Data
public class WeiBoInfoVo {
    private String accessToken;
    private String remindIn;
    private String expiresIn;
    private String uid;
}
