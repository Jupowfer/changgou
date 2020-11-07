package com.changgou.order.controller;

import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import com.changgou.order.util.TokenDecode;
import com.changgou.util.Result;
import com.changgou.util.StatusCode;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(value = "/cart")
//@CrossOrigin
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 新增购物车
     * @param id
     * @param num
     * @return
     */
    @GetMapping(value = "/add")
    public Result add(String id,Integer num){
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        cartService.add(num, id, username);
        return new Result(true, StatusCode.OK, "新增购物车成功");
    }

    /**
     * 查询购物车的数据
     * @return
     */
    @GetMapping(value = "/list")
    public Result<List<OrderItem>> findCart(){
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        List<OrderItem> list = cartService.list(username);

        return new Result(true, StatusCode.OK, "查询购物车成功", list);
    }

    /**
     * 根据选择的购物车数据,查询信息
     * @return
     */
    @GetMapping(value = "/list/choose")
    public Result<List<OrderItem>> findCart(@RequestParam(value = "ids") String[] ids){
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        List<OrderItem> list = cartService.list(username, ids);

        return new Result(true, StatusCode.OK, "查询购物车成功", list);
    }
}
