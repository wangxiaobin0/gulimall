package com.mall.member.service.impl;

import com.mall.member.entity.MemberLevelEntity;
import com.mall.member.service.MemberLevelService;
import com.mall.member.vo.RegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
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

}