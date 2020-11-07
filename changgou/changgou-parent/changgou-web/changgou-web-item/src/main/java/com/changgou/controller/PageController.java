package com.changgou.controller;

import com.changgou.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/page")
public class PageController {

    @Autowired
    private PageService pageService;

    /**
     * 创建商品详情的静态页面
     * @param id
     * @return
     */
    @GetMapping(value = "/create/{id}")
    public String createPage(@PathVariable(value = "id")String id){
        pageService.createPageHtml(id);
        return "succss";
    }

    /**
     * 生成所有商品的静态页面
     * @return
     */
    @GetMapping(value = "/create")
    public String create(){
        pageService.createPageHtml();
        return "succss";
    }
}
