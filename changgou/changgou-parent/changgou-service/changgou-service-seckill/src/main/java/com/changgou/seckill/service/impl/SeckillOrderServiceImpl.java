package com.changgou.seckill.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import com.changgou.seckill.service.SeckillOrderService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

/****
 * @Author:itheima
 * @Description:SeckillOrder业务层接口实现类
 *****/
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 修改秒杀订单:支付成功的场合
     *
     * @param username
     * @param transaction_id
     */
    @Override
    public void updateSeckillOrder(String username, String transaction_id) {
        //从redis中获取订单的信息
        SeckillOrder seckillOrder = (SeckillOrder)redisTemplate.boundValueOps("SeckillOrder_" + username).get();
        //做判断
        if(seckillOrder.getStatus().equals("0")){
            //修改订单的状态同步到数据库
            seckillOrder.setPayTime(new Date());
            seckillOrder.setStatus("1");
            seckillOrder.setTransactionId(transaction_id);
            //同步到数据库
            int i = seckillOrderMapper.insertSelective(seckillOrder);
            //同步到redis
            if(i > 0){
                redisTemplate.boundValueOps("SeckillOrder_" + username).set(seckillOrder);
            }
            //清除排队的标识
            redisTemplate.delete("SeckillUserQueue_" + username);
            //用户排队信息更新
            String s = stringRedisTemplate.boundValueOps("SeckillStatus_" + username).get();
            SeckillStatus seckillStatus = JSONObject.parseObject(s, SeckillStatus.class);
            seckillStatus.setStatus(4);
            String jsonString = JSONObject.toJSONString(seckillStatus);
            stringRedisTemplate.boundValueOps("SeckillStatus_"+ username).set(jsonString);
        }

    }

    /**
     * 删除秒杀订单:支付失败的场景
     *
     * @param username
     */
    @Override
    public void deleteSeckillOrder(String username) {
        //从redis中获取订单的信息
        SeckillOrder seckillOrder = (SeckillOrder)redisTemplate.boundValueOps("SeckillOrder_" + username).get();
        //用户排队信息更新
        String s = stringRedisTemplate.boundValueOps("SeckillStatus_" + username).get();
        SeckillStatus seckillStatus = JSONObject.parseObject(s, SeckillStatus.class);

        //修改订单的状态---支付失败
        seckillOrder.setStatus("2");
        seckillOrder.setPayTime(new Date());
        //修改排队的状态---失败
        seckillStatus.setStatus(3);
        //redis中订单的信息删除掉
        redisTemplate.delete("SeckillOrder_" + username);
        //订单的信息存储到数据库
        int i = seckillOrderMapper.insertSelective(seckillOrder);
        //清除排队的标识
        redisTemplate.delete("SeckillUserQueue_" + username);
        //库存回滚
        String goodsId = seckillStatus.getGoodsId();
        String time = seckillStatus.getTime();
        //校验:从redis获取商品的信息
        SeckillGoods seckillGoods = (SeckillGoods)redisTemplate.boundHashOps("SeckillGoods_" + time).get(goodsId);
        if(seckillGoods == null){
            //如果redis中没有数据了: 去数据库中回滚数据
            seckillGoods = seckillGoodsMapper.selectByPrimaryKey(goodsId);
            seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
            //同步到数据中去
            seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
        }else{
            //还原redis中商品的队列对象
            redisTemplate.boundListOps("SeckillGoodsQueue_" + goodsId).leftPush(goodsId);
            //还原redis中商品库存对象
            Long count = redisTemplate.boundHashOps("SeckillGoodsStockCount").increment(goodsId, 1);
            //将redis中商品的详情数据的库存进行更新回滚
            seckillGoods.setStockCount(count.intValue());
            redisTemplate.boundHashOps("SeckillGoods_" + time).put(goodsId, seckillGoods);
        }
    }

    /**
     * 新增秒杀订单
     *
     * @param key
     * @param id
     * @param username
     * @return
     */
    @Override
    public Boolean addOrder(String key, String id, String username) {
        //定义排队对象
        SeckillStatus seckillStatus = new SeckillStatus();
        //设置排队对象的属性
        seckillStatus.setUsername(username);
        seckillStatus.setCreateTime(new Date());
        seckillStatus.setStatus(1);
        seckillStatus.setGoodsId(id);
        seckillStatus.setTime(key);

        String jsonString = JSONObject.toJSONString(seckillStatus);

        //redis值自增对象 i++ 初始值0:记录用户排队的次数
        Long increment = redisTemplate.boundValueOps("SeckillUserQueue_" + username).increment(1);
        if(increment > 1){
            throw new RuntimeException("用户已经存在未支付的订单");
        }

        //将排队的信息存储到redis中去:存在的问题---重复排队
        stringRedisTemplate.boundValueOps("SeckillStatus_"+ username).set(jsonString);
        //将排队的信息发送mq
        rabbitTemplate.convertAndSend("SeckillOrderExchange", "SeckillOrderQueue", jsonString);
        //返回结果
        return true;
    }

    /**
     * SeckillOrder条件+分页查询
     * @param seckillOrder 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(seckillOrder);
        //执行搜索
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectByExample(example));
    }

    /**
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<SeckillOrder> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectAll());
    }

    /**
     * SeckillOrder条件查询
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder){
        //构建查询条件
        Example example = createExample(seckillOrder);
        //根据构建的条件查询数据
        return seckillOrderMapper.selectByExample(example);
    }


    /**
     * SeckillOrder构建查询对象
     * @param seckillOrder
     * @return
     */
    public Example createExample(SeckillOrder seckillOrder){
        Example example=new Example(SeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if(seckillOrder!=null){
            // 主键
            if(!StringUtils.isEmpty(seckillOrder.getId())){
                    criteria.andEqualTo("id",seckillOrder.getId());
            }
            // 秒杀商品ID
            if(!StringUtils.isEmpty(seckillOrder.getSeckillId())){
                    criteria.andEqualTo("seckillId",seckillOrder.getSeckillId());
            }
            // 支付金额
            if(!StringUtils.isEmpty(seckillOrder.getMoney())){
                    criteria.andEqualTo("money",seckillOrder.getMoney());
            }
            // 用户
            if(!StringUtils.isEmpty(seckillOrder.getUserId())){
                    criteria.andEqualTo("userId",seckillOrder.getUserId());
            }
            // 创建时间
            if(!StringUtils.isEmpty(seckillOrder.getCreateTime())){
                    criteria.andEqualTo("createTime",seckillOrder.getCreateTime());
            }
            // 支付时间
            if(!StringUtils.isEmpty(seckillOrder.getPayTime())){
                    criteria.andEqualTo("payTime",seckillOrder.getPayTime());
            }
            // 状态，0未支付，1已支付
            if(!StringUtils.isEmpty(seckillOrder.getStatus())){
                    criteria.andEqualTo("status",seckillOrder.getStatus());
            }
            // 收货人地址
            if(!StringUtils.isEmpty(seckillOrder.getReceiverAddress())){
                    criteria.andEqualTo("receiverAddress",seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if(!StringUtils.isEmpty(seckillOrder.getReceiverMobile())){
                    criteria.andEqualTo("receiverMobile",seckillOrder.getReceiverMobile());
            }
            // 收货人
            if(!StringUtils.isEmpty(seckillOrder.getReceiver())){
                    criteria.andEqualTo("receiver",seckillOrder.getReceiver());
            }
            // 交易流水
            if(!StringUtils.isEmpty(seckillOrder.getTransactionId())){
                    criteria.andEqualTo("transactionId",seckillOrder.getTransactionId());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        seckillOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder){
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void add(SeckillOrder seckillOrder){
        seckillOrderMapper.insert(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(String id){
        return  seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询SeckillOrder全部数据
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return seckillOrderMapper.selectAll();
    }
}
