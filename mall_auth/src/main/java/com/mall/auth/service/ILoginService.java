package com.mall.auth.service;

import com.mall.auth.vo.LoginVo;
import com.mall.auth.vo.RegisterVo;
import com.mall.common.vo.MemberEntity;

public interface ILoginService {
    void register(RegisterVo registerVo);

    MemberEntity login(LoginVo loginVo);
}
