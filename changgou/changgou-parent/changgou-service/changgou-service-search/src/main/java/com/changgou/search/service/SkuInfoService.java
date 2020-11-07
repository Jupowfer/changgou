package com.changgou.search.service;


import java.util.Map;

public interface SkuInfoService {

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    Map<String, Object> search(Map<String,String> searchMap);

    /**
     * 将数据保存到es中去
     */
    public void save();
}
