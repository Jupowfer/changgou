package com.changgou.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.service.PageService;
import com.changgou.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${pagepath}")
    private String pagepath;

    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    /**
     * 生成所有商品的静态页面
     */
    @Override
    public void createPageHtml() {
        //查询出所有的商品的spuid列表
        Result<List<Spu>> result = spuFeign.findAll();
        if(result.isFlag()){
            //获取返回的spu的列表
            List<Spu> spuList = result.getData();

            for (Spu spu : spuList) {
                createPageHtml(spu.getId());
            }
        }
    }

    /**
     * 根据SPUID生成静态页
     *
     * @param id : SpuId
     */
    @Override
    public void createPageHtml(String id) {
        try {
            Context context = new Context();
            //获取商品的数据
            Goods goods = findGoods(id);

            //获取spu的信息
            Spu spu = goods.getSpu();

            //获取一级 二级 三级分类的名字
            Category category1 = categoryFeign.findById(spu.getCategory1Id()).getData();
            Category category2 = categoryFeign.findById(spu.getCategory2Id()).getData();
            Category category3 = categoryFeign.findById(spu.getCategory3Id()).getData();

            //转换规格的数据
            String specItems = spu.getSpecItems();
            Map<String, Object> specMap = JSONObject.parseObject(specItems, Map.class);

            //获取sku的list
            List<Sku> skuList = goods.getSkuList();

            //放入商品的数据
            context.setVariable("spu", spu);
            context.setVariable("category1", category1);
            context.setVariable("category2", category2);
            context.setVariable("category3", category3);
            context.setVariable("specMap", specMap);
            context.setVariable("skuList", skuList);
            context.setVariable("imageList", spu.getImages().split(","));

            File file = new File(pagepath, id+".html");

            PrintWriter printWriter = new PrintWriter(file, "UTF-8");
            //生成模板
            templateEngine.process("item", context, printWriter);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 远程调用商品微服务,查询商品的信息
     * @param id
     * @return
     */
    private Goods findGoods(String id){
        //远程调用商品微服务,查询商品的spu和skulist的信息
        Result<Goods> result = spuFeign.findBySpuId(id);
        //判断请求是否成功
        if(result.isFlag()){
            return result.getData();
        }
        return null;
    }
}
