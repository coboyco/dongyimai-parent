package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Cart;
import com.offcn.entity.Result;
import com.offcn.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/cart")
@ResponseBody
public class CartController {

    @Reference(timeout=6000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;


    /**
     * 查询缓存中的购物车列表数据
     * @return
     */

    @RequestMapping("findCartList")
    public List<Cart> findCartList() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(name);
        String cartListStr = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        //判断  说明cookie中没有存数据，对其进行初始化
        if (cartListStr == null || cartListStr.equals("")) {
            cartListStr = "[]";
        }

        List<Cart> cookieCartList = JSON.parseArray(cartListStr, Cart.class);
        if (name.equals("anonymousUser")) {
            return cookieCartList;
        }else {
            //如果已经登录，就查询redis中的数据
            List<Cart> cartRedisList = cartService.findCartListFromRedis(name);
            //查看cookie中是否有数据
            if(cookieCartList !=null && cookieCartList.size()>0){
                //合并购物车
                cartRedisList= cartService.mergeCartList(cookieCartList,cartRedisList);
                //清除浏览器的缓存
                CookieUtil.deleteCookie(request,response,"cartList");
                //将合并后的数据存入redis
                cartService.saveCartListToRedis(name,cartRedisList);
            }
            return cartRedisList;
        }
    }


    /**
     * 用户点购物车运行的方法，添加购物车
     * @param itemId
     * @param num
     * @return
     */
    @CrossOrigin(origins = "http://localhost:9105",allowCredentials="true")
    @RequestMapping("addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId,Integer num){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("用户id为" + username);
        try {
            //查询购物车列表
            List<Cart> cartList = this.findCartList();
            //添加购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if(username.equals("anonymousUser")){
                //将数据更新到cookie中

                CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(cartList));
                System.out.println("向cookie中存入数据");
                return new Result(true,"添加购物车成功！！");
            }else {
                //将数据存入到redis中
                cartService.saveCartListToRedis(username,cartList);
                return new Result(true,"添加购物车成功");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"添加购物车失败！！");
        }
    }
}
