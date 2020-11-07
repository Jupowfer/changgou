package com.changgou.service;

public interface PageService {

    /**
     * 根据SPUID生成静态页
     * @param id : SpuId
     */
    void createPageHtml(String id);

    /**
     * 生成所有商品的静态页面
     */
    void createPageHtml();
}
