package com.changgou.service;

import java.util.Map;

public interface PayService {

    /**
     * 关闭支付
     *
     * @param orderId
     * @return
     */
    public Map<String, String> closeOrder(String orderId);

    /**
     * 生成二维码的支付地址
     * @param dataMap
     * @return
     */
    String createPayUrl(Map<String,String> dataMap);

    /**
     * 验证支付的结果
     * @param orderId
     * @return
     */
    Map<String, String> checkPayStatus(String orderId);
}
