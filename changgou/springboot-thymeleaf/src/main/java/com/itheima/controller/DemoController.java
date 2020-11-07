package com.itheima.controller;


import com.itheima.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping(value = "/demo")
public class DemoController {

    /**
     * thmeleaf的demo方法
     * @param model
     * @return
     */
    @GetMapping
    public String demo(Model model){

        //设置了一个叫做hello的对象,放置在
        model.addAttribute("hello", "hello word~");

        //设置了一个叫做hello的对象,放置在
        model.addAttribute("demo1",
                "<font style='color:red'>hello word~</font>");

        List<User> userList = new ArrayList<>();
        userList.add(new User(1001,"张一", "深圳1"));
        userList.add(new User(2001,"张二", "深圳2"));
        userList.add(new User(3001,"张三", "深圳3"));
        userList.add(new User(4001,"张四", "深圳4"));
        userList.add(new User(5001,"张五", "深圳5"));
        model.addAttribute("userList", userList);

        model.addAttribute("date", new Date());

        model.addAttribute("age", 20);

        //Map定义
        Map<String,Object> dataMap = new HashMap<String,Object>();
        dataMap.put("No","123");
        dataMap.put("address","深圳");
        model.addAttribute("dataMap",dataMap);

        model.addAttribute("url","/demo/add");

        model.addAttribute("image","https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1602998907485&di=b063dc656b1d3e9f7625822ea05f2cf8&imgtype=0&src=http%3A%2F%2Fa2.att.hudong.com%2F36%2F48%2F19300001357258133412489354717.jpg");

        return "demo";
    }

    /**
     * 接收前端数据
     * @return
     */
    @RequestMapping("/add")
    public String add(String name,String address,Model model) {
        System.out.println(name+"  住址是 "+address);
        return "redirect:http://www.baidu.com";
    }


}
