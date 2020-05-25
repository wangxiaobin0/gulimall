package com.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.member.entity.MemberEntity;
import com.mall.member.vo.LoginVo;
import com.mall.member.vo.RegisterVo;
import com.mall.member.vo.WeiBoInfoVo;

import java.util.Map;

/**
 * 会员
 *
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:38:26
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void save(RegisterVo registerVo);

    void checkMobilNoUnique(String mobileNo);
    void checkUsernameUnique(String username);

    void login(LoginVo loginVo);

    MemberEntity oAuthWeiBo(WeiBoInfoVo info);
}

