package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import com.changgou.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    private RedisTemplate redisTemplate;

    /***
     * 根据选择的购物车数据,查询信息
     * @param username
     * @param ids
     * @return
     */
    @Override
    public List<OrderItem> list(String username, String[] ids) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (String id : ids) {
            OrderItem orderItem = (OrderItem)redisTemplate.boundHashOps("Cart_" + username).get(id);
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    /***
     * 查询用户的购物车数据
     * @param username
     * @return
     */
    @Override
    public List<OrderItem> list(String username) {
        List<OrderItem> orderItems = redisTemplate.boundHashOps("Cart_" + username).values();
        return orderItems;
    }

    /***
     * 添加购物车
     * @param num :购买商品数量
     * @param id ：购买ID
     * @param username ：购买用户
     * @return
     */
    @Override
    public void add(Integer num, String id, String username) {

        if(num <= 0){
            redisTemplate.boundHashOps("Cart_" + username).delete(id);
            return;
        }
        //查询sku的信息
        Result<Sku> skuResult = skuFeign.findById(id);
        if(skuResult.isFlag()){
            //获取sku的详情
            Sku sku = skuResult.getData();
            //查询spu
            Result<Spu> spuResult = spuFeign.findById(sku.getSpuId());
            if(spuResult.isFlag()){
                Spu spu = spuResult.getData();

                //将spu和sku的信息封装成OrderItem对象
                OrderItem orderItem = setOrderItem(sku, spu, num);
                //存入redis
                redisTemplate.boundHashOps("Cart_" + username).put(id, orderItem);
            }
        }
    }

    /**
     * 将数据转换为OrderItem对象
     * @param sku
     * @param spu
     * @return
     */
    private OrderItem setOrderItem(Sku sku, Spu spu, Integer num){
        OrderItem orderItem = new OrderItem();
        //设置属性
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num * sku.getPrice());
        orderItem.setImage(sku.getImage());

        return orderItem;
    }
}
