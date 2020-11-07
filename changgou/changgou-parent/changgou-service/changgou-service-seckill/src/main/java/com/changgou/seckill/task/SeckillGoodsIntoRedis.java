package com.changgou.seckill.task;


import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillGoodsIntoRedis {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 定时任务:将数据库中秒杀商品的信息存入到redis中去
     */
    @Scheduled(cron = "0/10 * * * * *")
    public void dataIntoRedis(){
        //计算秒杀时间段
        List<Date> dateMenus = DateUtil.getDateMenus();
        for (Date dateMenu : dateMenus) {
            //时间区间段的开始时间
            String startTime = DateUtil.data2str(dateMenu, "yyyy-MM-dd HH:mm:ss");
            //时间区间段截止时间
            String endTime = DateUtil.data2str(DateUtil.addDateHour(dateMenu, 2), "yyyy-MM-dd HH:mm:ss");
            //构建条件表达式
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            //状态必须为审核通过 status=1
            criteria.andEqualTo("status", "1");
            //商品库存个数>0
            criteria.andGreaterThan("stockCount", 0);
            //活动没有结束 start_time>=startTime  end_time<=endTime
            criteria.andGreaterThanOrEqualTo("startTime", startTime);
            criteria.andLessThanOrEqualTo("endTime", endTime);
            //在Redis中没有该商品的缓存
            String key = DateUtil.data2str(dateMenu, DateUtil.PATTERN_YYYYMMDDHH);
            //获取已经存储在redis中的所有商品的商品id
            Set keys = redisTemplate.boundHashOps("SeckillGoods_" + key).keys();
            if(keys != null && keys.size() > 0){
                criteria.andNotIn("id", keys);
            }
            //执行查询获取对应的结果集
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
            //将活动没有结束的秒杀商品入库
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps("SeckillGoods_" + key).put(seckillGoods.getId(), seckillGoods);
                //为商品生成一个队列,队列的长度或者元素的个数就是商品的库存的数量----决定是否能下单
                String[] ids = getIds(seckillGoods.getId(), seckillGoods.getStockCount());
                redisTemplate.boundListOps("SeckillGoodsQueue_" + seckillGoods.getId()).leftPushAll(ids);
                //为商品做一个自增的对象,自增的值,就是商品的库存,------显示库存和判断库存
                redisTemplate.boundHashOps("SeckillGoodsStockCount").increment(seckillGoods.getId(), seckillGoods.getStockCount());
            }
        }
    }


    /**
     * 生成一个商品库存数量长度的数组
     * @param id
     * @param num
     * @return
     */
    private String[] getIds(String id, Integer num){
        String[] ids = new String[num];

        for (int i = 0; i < num; i++) {
            ids[i] = id;
        }
        return ids;
    }
}
