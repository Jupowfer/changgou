package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.changgou.goods.dao.BrandMapper;
import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.dao.SkuMapper;
import com.changgou.goods.dao.SpuMapper;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.GoodsService;
import com.changgou.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private IdWorker idWorker;

    /**
     * 根据商品的spuid查询商品的信息
     *
     * @param spuId
     * @return
     */
    @Override
    public Goods findById(String spuId) {
        //定义返回结果
        Goods goods = new Goods();

        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(spuId);

        //查询sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);

        //设置返回结果
        goods.setSpu(spu);
        goods.setSkuList(skuList);

        return goods;
    }

    /**
     * 新增商品
     *
     * @param goods
     */
    @Override
    public void goodsAdd(Goods goods) {
        //获取spu
        Spu spu = goods.getSpu();

        //修改的情况
        if(!StringUtils.isEmpty(spu.getId())){
            spuMapper.updateByPrimaryKeySelective(spu);
            //删除sku的数据
            Sku sku = new Sku();
            sku.setSpuId(sku.getSpuId());
            skuMapper.delete(sku);
        }else{
            //设置id
            spu.setId("No" + idWorker.nextId());
            //新增spu
            spuMapper.insertSelective(spu);
        }

        //查询品牌
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());

        //查询类别
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());

        //获取skulist
        List<Sku> skuList = goods.getSkuList();
        for (Sku sku : skuList) {
            //id
            sku.setId("No" + idWorker.nextId());

            //规格进行转换
            String spec = sku.getSpec();
            Map<String,String> specMap = JSONObject.parseObject(spec, Map.class);
            sku.setSpec(JSONObject.toJSONString(specMap));
            //name
            String name = spu.getName();
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                name = name + " " + entry.getValue();
            }
            sku.setName(name);

            //createtime
            sku.setCreateTime(new Date());
            //updatetime
            sku.setUpdateTime(new Date());
            //spuid
            sku.setSpuId(spu.getId());
            //categoryid
            sku.setCategoryId(spu.getCategory3Id());
            ///categoryname
            sku.setCategoryName(category.getName());
            //brandname
            sku.setBrandName(brand.getName());

            //sku的新增
            skuMapper.insertSelective(sku);

        }


    }
}
