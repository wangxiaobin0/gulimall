package com.mall.auth.service;

import com.mall.common.vo.MemberEntity;

public interface IOAuthService {


    MemberEntity oauthLogin(String code) throws Exception;
}
