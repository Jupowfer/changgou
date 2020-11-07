package com.changgou.pay.feign;

import com.changgou.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "pay")
@RequestMapping(value = "/weixin/pay")
public interface PayFeign {


    /**
     * 关闭支付
     * @param id
     * @return
     */
    @GetMapping(value = "/close/order/{id}")
    public Result<Map<String,String>> closeOrder(@PathVariable(value = "id") String id);
}
