package com.changgou.goods.feign;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.changgou.util.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "goods")
@RequestMapping("/spu")
public interface SpuFeign {

    /**
     * 根据spuid查询商品信息
     * @param id
     * @return
     */
    @GetMapping("/goods/{id}")
    public Result<Goods> findBySpuId(@PathVariable(value = "id") String id);

    /***
     * 查询Spu全部数据
     * @return
     */
    @GetMapping
    public Result<List<Spu>> findAll();

    @GetMapping("/{id}")
    public Result<Spu> findById(@PathVariable(value = "id") String id);
}
