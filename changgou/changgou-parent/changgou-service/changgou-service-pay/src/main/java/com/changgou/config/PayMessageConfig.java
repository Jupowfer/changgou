package com.changgou.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayMessageConfig {


    //声明队列
    @Bean("myQueue")
    public Queue myQueue(){
        return QueueBuilder.durable("queue.order").build();
    }


    //声明交换机
    @Bean("myExchange")
    public Exchange myExchange(){
        return ExchangeBuilder.directExchange("exchange.order").build();
    }



    //声明绑定
    @Bean
    public Binding myBinding(@Qualifier("myQueue") Queue myQueue,
                             @Qualifier("myExchange") Exchange myExchange){
        return BindingBuilder.bind(myQueue).to(myExchange).with("queue.order").noargs();
    }

    /***
     * 秒杀支付状态队列创建队列
     */
    @Bean("queueSeckillOrder")
    public Queue queueSeckillOrder(){
        return new Queue("queueSeckillOrder");
    }

    /***
     * 秒杀状态队列绑定交换机
     */
    @Bean
    public Binding basicBindingSeckill(@Qualifier("myExchange")Exchange myExchange,
                                       @Qualifier("queueSeckillOrder") Queue queueSeckillOrder){
        return BindingBuilder.bind(queueSeckillOrder).to(myExchange).with("queueSeckillOrder").noargs();
    }

}
