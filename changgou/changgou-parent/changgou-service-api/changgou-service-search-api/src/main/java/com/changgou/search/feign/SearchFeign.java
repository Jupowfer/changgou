package com.changgou.search.feign;

import com.changgou.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "search")
@RequestMapping(value = "/skuInfo")
public interface SearchFeign {

    /**
     * 商品搜索
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search")
    public Result<Map<String,Object>> search(@RequestParam Map<String,String> searchMap);
}
