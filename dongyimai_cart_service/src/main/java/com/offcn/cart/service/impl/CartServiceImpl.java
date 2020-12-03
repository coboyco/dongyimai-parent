package com.offcn.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Cart;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper tbItemMapper;
    
    @Autowired
    private RedisTemplate redisTemplate;

    //添加购物车
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {

        //根据item的id获取商品的sku信息
        TbItem item = tbItemMapper.selectByPrimaryKey(itemId);

        //对获取的商品进行判断
        if(item == null){
            throw new RuntimeException("商品不存在");
        }

        if(!item.getStatus().equals("1")){
            throw new RuntimeException("商品的状态不对");
        }

        //获取商家的serrlerId
        String sellerId = item.getSellerId();

        //判断购物车列表中的商家sellerId有没有与要新增加的商品的商家sellerId一样
        Cart cart = this.searchCartBySellerId(cartList, sellerId);

        //判断新添加的商品商家ID在购物车列表中是否存在
        if(cart == null){
            //向购物车列表中添加购物车
            //为购物车添加属性
            //设置商家Id
            cart=new Cart();
            cart.setSellerId(sellerId);
            //设置商家名称
            cart.setSellerName(item.getSeller());
            //设置订单详情
            TbOrderItem orderItem = createOrderItem(item, num);
            //条件到购物车中
            List<TbOrderItem> orderItemList = new ArrayList<>();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //将购物车对象对象添加到购物车列表中
            cartList.add(cart);
        }else {
            //购物车列表中存在与要添加商品的商家
            //查询该购物车列表中有没有该商品
            TbOrderItem orderItem = this.searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            //判断 如果没有该商品的订单详情，就该商品的订单详情添加到该购物车下 的订单详情集合
            if(orderItem == null){
                 //新增购物车明细
                TbOrderItem tbOrderItem = this.createOrderItem(item, num);
                //向已经存在商家商品的订单明细中添加订单详情
                cart.getOrderItemList().add(tbOrderItem);
            }else {
                //如果购物明细存在，则更新购物明细的数量和总的价格
                //更新数量
                orderItem.setNum(orderItem.getNum() + num);
                //更新价格
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum() * orderItem.getPrice().doubleValue()));

                //判断此时的数量  如果明细数量<=0将该明细移除掉
                if(orderItem.getNum() <= 0){
                   cart.getOrderItemList().remove(orderItem);
                }

                //如果购物明细<=0则将整个cart移除掉
                if(cart.getOrderItemList().size() <= 0){
                    cartList.remove(cart);
                }
            }

        }

        return cartList;
    }


    /**
     * 判断购物车中是否存在商家
     * @param cartList  传入要查询的购物车列表信息
     * @param sellerId  商家Id
     * @return          返回购物车
     */
    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
        for (Cart cart : cartList) {
            //如果要增加的商品商家id与购物车列表中的某个一样，就将该购物车信息返回，用于后续的更新
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }

    /**
     * 生成订单详情
     * @param item  sku数据
     * @param num  购买商品的数量
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item,int num){
        //判断数量是否合法
        if(num < 0){
            throw new RuntimeException("数量不合法！");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        orderItem.setNum(num);
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        orderItem.setPicPath(item.getImage());
        return orderItem;
    }

    /**
     * 查询购物车清单中是否与新添加的商品一样的详情
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
        for (TbOrderItem orderItem : orderItemList) {
            //如果商品的id与购物车中订单详情的商品id一样，则说明存在有订单详情
            if(orderItem.getItemId().longValue() == itemId){
                return orderItem;
            }
        }
        return null;
    }


    /**
     * 从redis中查询购物车
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("当前登录用户的用户id为" + username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if(cartList == null){
            cartList = new ArrayList<>();
        }
        return cartList;
    }


    /**
     * 将购物车保存到redis
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }


    /**
     * 合并购物车
     * @param cookList  cookie里边的数据
     * @param redisList  redis里边的数据
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cookList, List<Cart> redisList) {
        System.out.println("合并购物车");
        for (Cart cart : cookList) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                //调用添加购物车的方法
                redisList = addGoodsToCartList(redisList,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return redisList;
    }
}
