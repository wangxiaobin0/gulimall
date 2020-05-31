package com.mall.product.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.mall.product.vo.SpuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mall.product.entity.SpuInfoEntity;
import com.mall.product.service.SpuInfoService;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.R;


/**
 * spu信息
 *
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:42:31
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:spuinfo:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = spuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:spuinfo:info")
    public R info(@PathVariable("id") Long id) {
        SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:spuinfo:save")
    public R save(@RequestBody SpuInfoVo spuInfoVo) {
        spuInfoService.saveSpuInfoVo(spuInfoVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity spuInfo) {
        spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:spuinfo:delete")
    public R delete(@RequestBody Long[] ids) {
        spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 上架
     * @return
     */
    @PostMapping("/{spuId}/up")
    public R up(@PathVariable("spuId") Long spuId) throws IOException {
        Boolean up = spuInfoService.up(spuId);
        if (up) {
            return R.ok();
        } else {
            return R.error();
        }
    }

    /**
     * 信息
     */
    @RequestMapping("/info/sku/{id}")
    public R infoBySkuId(@PathVariable("id") Long id) {
        SpuInfoEntity spuInfo = spuInfoService.getBySkuId(id);

        return R.ok().put("spuInfo", spuInfo);
    }
}
