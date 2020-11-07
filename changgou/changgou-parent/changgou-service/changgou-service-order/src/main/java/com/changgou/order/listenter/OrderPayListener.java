package com.changgou.order.listenter;

import com.alibaba.fastjson.JSONObject;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderPayListener {

    @Autowired
    private OrderService orderService;


    /***
     * 接受订单的支付消息
     * @param message
     */
    @RabbitListener(queues = "queue.order")
    public void payMessage(String message){
        try {
            //数据的类型转换
            Map<String,String> map = JSONObject.parseObject(message, Map.class);

            //判断此次支付的结果状态
            String return_code = map.get("return_code");
            if(return_code.equals("SUCCESS")){
                String result_code = map.get("result_code");
                String out_trade_no = map.get("out_trade_no");
                if(result_code.equals("SUCCESS")){
                    String transaction_id = map.get("transaction_id");
                    //更新订单
                    orderService.updateOrder(out_trade_no, transaction_id);
                }else{
                    //删除订单
                    orderService.deleteOrder(out_trade_no);
                }
            }
        }catch (Exception e){
            //将消息存储到数据库:数据库中专门有一张表存储处理失败的消息

            //定时任务,定时去扫描这个表
        }
    }
}
