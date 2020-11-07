package com.changgou.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AddOrderMQConfing {

    /**
     * 定义队列
     */
    @Bean("myQueue")
    public Queue myQueue(){
        return QueueBuilder.durable("SeckillOrderQueue").build();
    }

    /**
     * 定义交换机
     */
    @Bean("myExchange")
    public Exchange myExchange(){
        return ExchangeBuilder.directExchange("SeckillOrderExchange").build();
    }

    /**
     * 定义绑定
     */
    @Bean
    public Binding myBinding(@Qualifier("myQueue")Queue myQueue,
                             @Qualifier("myExchange")Exchange myExchange){
        return BindingBuilder.bind(myQueue).to(myExchange).with("SeckillOrderQueue").noargs();
    }

    /**
     * 到期数据队列
     * @return
     */
    @Bean
    public Queue seckillOrderTimerQueue() {
        return new Queue("nomalQueue", true);
    }

    /**
     * 超时数据队列
     * @return
     */
    @Bean
    public Queue delaySeckillOrderTimerQueue() {
        return QueueBuilder.durable("delayQueue")
                .withArgument("x-dead-letter-exchange", "SeckillOrderExchange")        // 消息超时进入死信队列，绑定死信队列交换机
                .withArgument("x-dead-letter-routing-key", "nomalQueue")   // 绑定指定的routing-key
                .build();
    }

    /***
     * 交换机与队列绑定
     * @return
     */
    @Bean
    public Binding basicBinding() {
        return BindingBuilder.bind(seckillOrderTimerQueue())
                .to(myExchange())
                .with("nomalQueue").noargs();
    }
}
