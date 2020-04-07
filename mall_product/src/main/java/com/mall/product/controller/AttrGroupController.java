package com.mall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.mall.product.entity.AttrEntity;
import com.mall.product.service.AttrService;
import com.mall.product.service.CategoryService;
import com.mall.product.vo.AttrGroupVo;
import com.mall.product.vo.AttrGroupWithAttrsVo;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mall.product.entity.AttrGroupEntity;
import com.mall.product.service.AttrGroupService;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.R;



/**
 * 属性分组
 *
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:42:32
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private CategoryService categoryService;
    /**
     * 列表
     */
    @RequestMapping("/list/{categoryId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("categoryId") Long categoryId){
        PageUtils page = attrGroupService.queryPage(params, categoryId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] catelogPath = categoryService.getCatelogPathByCatelogId(catelogId);
        attrGroup.setCatelogPath(catelogPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.delete(Arrays.asList(attrGroupIds));

        return R.ok();
    }


    @GetMapping("/{attrgroupId}/attr/relation")
    public R getAttrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> data = attrService.getAttrRelation(attrgroupId);
        return R.ok().put("data", data);
    }

    /**
     * 查询分组没有关联的attr
     * 规则:
     *  0. group只能关联基本属性,不能关联销售属性
     *  1. group只能关联自己分类下的attr
     *  2. group只能关联其他group没有关联的attr
     *  3. attr只能被一个group关联
     * @param attrgroupId
     * @param params
     * @return
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R getAttrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                               @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.getAttrNoRelation(params, attrgroupId);
        return R.ok().put("page", page);
    }
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupVo[] groupVo) {
        attrGroupService.deleteRelation(groupVo);
        return R.ok();
    }

    /**
     * group添加关联的attr
     * @param groupVos
     * @return
     */
    @PostMapping("attr/relation")
    public R addRelation(@RequestBody List<AttrGroupVo> groupVos) {
        attrService.addRelation(groupVos);
        return R.ok();
    }


    /**
     * 通过分类id查询分类下的分组及分组属性
     * @param catelogId
     * @return
     */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupAndAttrByCatelogId(@PathVariable("catelogId") Long catelogId) {
        List<AttrGroupWithAttrsVo> data = attrGroupService.getAttrGroupAndAttrByCatelogId(catelogId);
        return R.ok().put("data", data);
    }
}
