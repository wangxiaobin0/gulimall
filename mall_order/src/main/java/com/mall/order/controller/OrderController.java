package com.mall.order.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.mall.common.to.OrderTo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mall.order.entity.OrderEntity;
import com.mall.order.service.OrderService;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.R;



/**
 * 订单
 *
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:40:24
 */
@RestController
@RequestMapping("order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("order:order:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("order:order:info")
    public R info(@PathVariable("id") Long id){
		OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("order:order:save")
    public R save(@RequestBody OrderEntity order){
		orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("order:order:update")
    public R update(@RequestBody OrderEntity order){
		orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("order:order:delete")
    public R delete(@RequestBody Long[] ids){
		orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @GetMapping("/orderSn")
    public R getByOrderSn(@RequestParam("orderSn") String orderSn) {
        OrderEntity orderEntity = orderService.getByOrderSn(orderSn);
        OrderTo orderTo = null;
        if (orderEntity != null) {
            orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
        }
        return R.ok().put("orderInfo", orderTo);
    }
}
