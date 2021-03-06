package com.mall.member.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.mall.member.vo.LoginVo;
import com.mall.member.vo.RegisterVo;
import com.mall.member.vo.WeiBoInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mall.member.entity.MemberEntity;
import com.mall.member.service.MemberService;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.R;

import javax.print.attribute.standard.RequestingUserName;
import javax.servlet.http.HttpSession;


/**
 * 会员
 *
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:38:26
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    R save(@RequestBody RegisterVo registerVo){
		try {
            memberService.save(registerVo);
        } catch (RuntimeException e) {
		    return R.error(500, e.getMessage());
        }
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @PostMapping("/login")
    R login(@RequestBody LoginVo loginVo){
        try {
            MemberEntity login = memberService.login(loginVo);
            return R.ok().put("user", login);
        } catch (RuntimeException e) {
            return R.error(500, e.getMessage());
        }
    }

    @PostMapping("/login/weibo")
    R oAuthWeiBo(@RequestBody WeiBoInfoVo info){
        MemberEntity memberEntity = memberService.oAuthWeiBo(info);
        return R.ok().put("user", memberEntity);
    }

}
