package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import com.changgou.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "goods")
@RequestMapping("/sku")
public interface SkuFeign {

    /**
     * 根据状态查询商品---sku的列表
     * @param status
     * @return
     */
    @GetMapping(value = "/status/{status}")
    public Result<List<Sku>> findByStatus(@PathVariable(value = "status")String status);

    @GetMapping("/{id}")
    public Result<Sku> findById(@PathVariable(value = "id") String id);

    /****
     * 库存递减实现
     */
    @GetMapping(value = "/decr/count")
    public Result decrCount(@RequestParam Map<String,Object> map);
}
