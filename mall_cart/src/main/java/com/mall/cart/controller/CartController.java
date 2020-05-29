package com.mall.cart.controller;

import com.mall.cart.intercepter.CartInterceptor;
import com.mall.cart.service.ICartService;
import com.mall.cart.to.UserTo;
import com.mall.cart.vo.Cart;
import com.mall.cart.vo.CartItemVo;
import com.mall.common.constrant.AuthConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    ICartService cartService;

    @GetMapping
    public String goToCartPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart = cartService.getCart();
        model.addAttribute("cartList", cart);
        return "cartList";
    }


    @GetMapping("/cart.html")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, num);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.mall.com/success";
    }
    @GetMapping("/success")
    public String successPage(@RequestParam("skuId") Long skuId, Model model) {
        CartItemVo cartItemVo = cartService.getCartItemVoBySkuId(skuId);
        model.addAttribute("skuInfo", cartItemVo);
        return "success";
    }

    @GetMapping("/check")
    public String changeCheckState(@RequestParam("skuId") Long skuId, @RequestParam("check") Boolean check) {
        cartService.changeCheckState(skuId, check);
        return "redirect:http://cart.mall.com";
    }

    @GetMapping("/count")
    public String changeSkuCount(@RequestParam("skuId") Long skuId, @RequestParam("count") Integer count) {
        cartService.changeSkuCount(skuId, count);
        return "redirect:http://cart.mall.com";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("skuId") Long skuId) {
        cartService.delete(skuId);
        return "redirect:http://cart.mall.com";
    }

    @GetMapping("/cart/check")
    @ResponseBody
    public List<CartItemVo> getCheckedItems(HttpServletResponse response) throws IOException, ExecutionException, InterruptedException {
        if (CartInterceptor.threadLocal.get().getUserId() == null) {
            response.sendRedirect(AuthConstant.LOGIN_URL);
            return null;
        }
        List<CartItemVo> cartItemVos = cartService.getCheckedItems();
        return cartItemVos;
    }
}
