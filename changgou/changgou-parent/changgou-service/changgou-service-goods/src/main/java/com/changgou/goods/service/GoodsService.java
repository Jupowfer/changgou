package com.changgou.goods.service;

import com.changgou.goods.pojo.Goods;

public interface GoodsService {

    /**
     * 新增商品
     * @param goods
     */
    public void goodsAdd(Goods goods);

    /**
     * 根据商品的spuid查询商品的信息
     * @param spuId
     * @return
     */
    public Goods findById(String spuId);
}
