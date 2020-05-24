package com.mall.auth.service;

import com.mall.auth.vo.LoginVo;
import com.mall.auth.vo.RegisterVo;

public interface ILoginService {
    void register(RegisterVo registerVo);

    void login(LoginVo loginVo);
}
