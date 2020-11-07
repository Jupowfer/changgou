package com.changgou.seckill.listener;

import com.alibaba.fastjson.JSONObject;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 监听秒杀订单的支付消息
 */
@Component
public class SeckillOrderPayMessage {

    @Autowired
    private SeckillOrderService seckillOrderService;


    /**
     * 监听秒杀订单的支付状态,修改秒杀订单的状态
     */
    @RabbitListener(queues = "queueSeckillOrder")
    public void updateOrder(String message){
        //数据的类型转换
        Map<String,String> map = JSONObject.parseObject(message, Map.class);
        //判断此次支付的结果状态
        String return_code = map.get("return_code");
        if(return_code.equals("SUCCESS")){
            String result_code = map.get("result_code");
            //获取附加参数
            String jsonSting = map.get("attach");
            Map<String,String> attach = JSONObject.parseObject(jsonSting, Map.class);
            String username = attach.get("username");
            if(result_code.equals("SUCCESS")){
                String transaction_id = map.get("transaction_id");
                //更新秒杀订单
                seckillOrderService.updateSeckillOrder(username, transaction_id);
            }else{
                //删除秒杀订单
                seckillOrderService.deleteSeckillOrder(username);
            }
        }

    }
}
