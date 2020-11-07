package com.changgou.seckill.listener;

import com.alibaba.fastjson.JSONObject;
import com.changgou.pay.feign.PayFeign;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import com.changgou.seckill.service.SeckillOrderService;
import com.changgou.util.Result;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 超时未支付的订单的消息消费者
 */
@Component
public class TimeOutOrderListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillOrderService seckillOrderService;

    @Autowired
    private PayFeign payFeign;

    /**
     * 取消订单
     * @param message
     */
    @RabbitListener(queues = "nomalQueue")
    public void cancleOrder(String message){
        //排队状态类型装换
        SeckillStatus seckillStatus = JSONObject.parseObject(message, SeckillStatus.class);
        String username = seckillStatus.getUsername();
        String orderId = seckillStatus.getOrderId();

        //获取订单的信息
        SeckillOrder seckillOrder = (SeckillOrder)redisTemplate.boundValueOps("SeckillOrder_" + username).get();
        //判断订单的状态
        if(seckillOrder.getStatus().equals("0")) {
            //关闭支付
            Result<Map<String, String>> mapResult = payFeign.closeOrder(orderId);
            if (mapResult.isFlag()) {
                Map<String, String> data = mapResult.getData();
                //判断此次支付的结果状态
                String return_code = data.get("return_code");
                if (return_code.equals("SUCCESS")) {
                    String result_code = data.get("result_code");
                    if (result_code.equals("SUCCESS")) {
                        //调用service的订单删除的方法
                        seckillOrderService.deleteSeckillOrder(username);
                    }
                }
            }
        }
    }
}
