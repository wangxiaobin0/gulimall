package com.mall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mall.common.utils.HttpUtils;
import com.mall.member.entity.MemberLevelEntity;
import com.mall.member.service.MemberLevelService;
import com.mall.member.vo.LoginVo;
import com.mall.member.vo.RegisterVo;
import com.mall.member.vo.WeiBoInfoVo;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.member.dao.MemberDao;
import com.mall.member.entity.MemberEntity;
import com.mall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void save(RegisterVo registerVo) {
        //检查手机号是否被占用
        checkMobilNoUnique(registerVo.getMobileNo());
        //检查用户名是否已注册
        checkUsernameUnique(registerVo.getUsername());

        //查询默认会员等级
        MemberLevelEntity defaultMemberLevel = memberLevelService.getDefaultMemberLevel();
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setUsername(registerVo.getUsername());
        memberEntity.setMobile(registerVo.getMobileNo());
        memberEntity.setUsername(registerVo.getUsername());
        memberEntity.setLevelId(defaultMemberLevel.getId());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        memberEntity.setPassword(passwordEncoder.encode(registerVo.getPassword()));
        memberEntity.setCreateTime(new Date());
        this.save(memberEntity);
    }

    @Override
    public void checkMobilNoUnique(String mobileNo) {
        QueryWrapper<MemberEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", mobileNo);
        int count = this.count(queryWrapper);
        if (count != 0) {
            throw new RuntimeException("该手机号已注册");
        }
    }

    @Override
    public void checkUsernameUnique(String username) {
        QueryWrapper<MemberEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        int count = this.count(queryWrapper);
        if (count != 0) {
            throw new RuntimeException("该用户名已注册");
        }
    }

    @Override
    public void login(LoginVo loginVo) {
        String username = loginVo.getUsername();
        String password = loginVo.getPassword();
        QueryWrapper<MemberEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.or();
        queryWrapper.eq("mobile", username);
        MemberEntity one = this.getOne(queryWrapper);
        if (one == null) {
            throw new RuntimeException("请先注册");
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean matches = passwordEncoder.matches(password, one.getPassword());
        if (!matches) {
            throw new RuntimeException("用户名或密码错误");
        }
    }

    @Override
    public MemberEntity oAuthWeiBo(WeiBoInfoVo info) {

        MemberEntity access_token = this.getOne(new QueryWrapper<MemberEntity>().eq("uid", info.getUid()));

        //不是第一次登录,更新token
        if (access_token == null) {
            access_token = new MemberEntity();
            access_token.setUid(info.getUid());
            try {
                Map<String, String> header = new HashMap<>();
                Map<String, String> query = new HashMap<>();
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json?access_token=" + info.getAccessToken(), "GET", header, query);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String userInfo = response.getEntity().toString();
                    JSONObject jsonObject = JSON.parseObject(userInfo);
                    String name = jsonObject.getString("name");
                    String avatar_large = jsonObject.getString("avatar_large");
                    access_token.setNickname(name);
                    access_token.setHeader(avatar_large);
                }
            } catch (Exception e) {
            }
        }
        access_token.setAccessToken(info.getAccessToken());
        access_token.setExpiresIn(info.getExpiresIn());
        //更新token值
        this.saveOrUpdate(access_token);
        access_token.setPassword("");
        return access_token;
    }
}