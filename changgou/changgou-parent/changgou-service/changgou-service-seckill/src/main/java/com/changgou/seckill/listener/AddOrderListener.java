package com.changgou.seckill.listener;

import com.alibaba.fastjson.JSONObject;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import com.changgou.util.IdWorker;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 监听用户的排队信息,异步完成用户下单
 */
@Component
public class AddOrderListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 监听用户的排队信息后,进行异步下单
     * @param message
     */
    @RabbitListener(queues = "SeckillOrderQueue")
    public void addOrder(String message){
        //将消息从string转换为排队对象
        SeckillStatus seckillStatus = JSONObject.parseObject(message, SeckillStatus.class);
        //获取相关的属性
        String goodsId = seckillStatus.getGoodsId();
        String time = seckillStatus.getTime();
        String username = seckillStatus.getUsername();
//        //判断用户是否有未支付的订单:从redis查
//        Object o = redisTemplate.boundValueOps("SeckillOrder_" + username).get();
//        if(o != null){
//            seckillStatus.setStatus(3);
//            String jsonString = JSONObject.toJSONString(seckillStatus);
//            stringRedisTemplate.boundValueOps("SeckillStatus_"+ username).set(jsonString);
//            throw new RuntimeException("下单失败,用户存在未支付的订单");
//        }
        //从商品队列中获取商品对象
        Object o = redisTemplate.boundListOps("SeckillGoodsQueue_" + goodsId).rightPop();
        if(o == null){
            //清除排队的标识
            redisTemplate.delete("SeckillUserQueue_" + username);
            //用户排队信息更新
            seckillStatus.setStatus(3);
            String jsonString = JSONObject.toJSONString(seckillStatus);
            stringRedisTemplate.boundValueOps("SeckillStatus_"+ username).set(jsonString);
            //抛出异常中止下单
            throw new RuntimeException("商品已经售罄");
        }
        //校验:从redis获取商品的信息
        SeckillGoods seckillGoods = (SeckillGoods)redisTemplate.boundHashOps("SeckillGoods_" + time).get(goodsId);
        //判断商品是否已经售罄:超卖的问题
        if(seckillGoods != null && seckillGoods.getStockCount() > 0){
            //生成订单
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId("No" + idWorker.nextId());
            seckillOrder.setSeckillId(goodsId);
            seckillOrder.setMoney(seckillGoods.getCostPrice().toString());
            seckillOrder.setUserId(username);
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setStatus("0");
            //订单的信息存储到redis中去
//            redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);
            redisTemplate.boundValueOps("SeckillOrder_" + username).set(seckillOrder);
            //扣库存:超卖的问题----商品售罄的时候:清除redis的数据,并且将数据同步到数据库中去
//            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            Long count = redisTemplate.boundHashOps("SeckillGoodsStockCount").increment(goodsId, -1);
            seckillGoods.setStockCount(count.intValue());
            //商品售罄
            if(count == 0){
                //将商品的数据从redis中移除
                redisTemplate.boundHashOps("SeckillGoods_" + time).delete(goodsId);
                //将商品的数据同步到数据库
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
            }else{
                redisTemplate.boundHashOps("SeckillGoods_" + time).put(goodsId, seckillGoods);
            }
            //排队的信息更新
            seckillStatus.setOrderId(seckillOrder.getId());
            seckillStatus.setMoney(Float.parseFloat(seckillOrder.getMoney()));
            seckillStatus.setStatus(2);
            String jsonString = JSONObject.toJSONString(seckillStatus);
            stringRedisTemplate.boundValueOps("SeckillStatus_"+ username).set(jsonString);

            //发送延迟消息,倒计时----删除订单,回滚商品的库存
            rabbitTemplate.convertAndSend("delayQueue", (Object) jsonString, new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    MessageProperties messageProperties = message.getMessageProperties();
                    //设置消息的过期的时间
                    messageProperties.setExpiration("600000");
                    return message;
                }
            });
        }


    }
}
