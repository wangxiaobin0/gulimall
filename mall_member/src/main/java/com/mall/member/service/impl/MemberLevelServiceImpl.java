package com.mall.member.service.impl;

import com.mall.member.entity.MemberEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.member.dao.MemberLevelDao;
import com.mall.member.entity.MemberLevelEntity;
import com.mall.member.service.MemberLevelService;


@Service("memberLevelService")
public class MemberLevelServiceImpl extends ServiceImpl<MemberLevelDao, MemberLevelEntity> implements MemberLevelService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<MemberLevelEntity> queryWrapper = new QueryWrapper<>();
        String key = (String)params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("id", key).like("name", key);
        }
        IPage<MemberLevelEntity> page = this.page(
                new Query<MemberLevelEntity>().getPage(params),queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public MemberLevelEntity getDefaultMemberLevel() {
        QueryWrapper<MemberLevelEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("default_status", 1);
        MemberLevelEntity levelEntity = this.getOne(queryWrapper);
        return levelEntity;
    }

}