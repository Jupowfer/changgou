package com.changgou.search.controller;

import com.changgou.search.service.SkuInfoService;
import com.changgou.util.Result;
import com.changgou.util.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/skuInfo")
@CrossOrigin
public class SkuInfoController {

    @Autowired
    private SkuInfoService skuInfoService;

    /**
     * 商品搜索
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search")
    public Result<Map<String,Object>> search(@RequestParam Map<String,String> searchMap){
        Map<String, Object> result = skuInfoService.search(searchMap);
        return new Result(true, StatusCode.OK, "搜索成功", result);
    }

    /**
     * 将数据从数据库导入到es中去
     * @return
     */
    @GetMapping(value = "/save")
    public Result save(){
        skuInfoService.save();

        return new Result(true, StatusCode.OK, "导入成功");
    }
}
