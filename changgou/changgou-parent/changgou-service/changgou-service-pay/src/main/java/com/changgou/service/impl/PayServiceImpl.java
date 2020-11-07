package com.changgou.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.changgou.service.PayService;
import com.changgou.util.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {

    @Value("${weixin.appid}")
    private String appId;


    @Value("${weixin.partner}")
    private String partner;

    @Value("${weixin.notifyurl}")
    private String notifyurl;

    @Value("${weixin.partnerkey}")
    private String partnerkey;


    /**
     * 关闭支付
     *
     * @param orderId
     * @return
     */
    @Override
    public Map<String, String> closeOrder(String orderId) {
        try {
            //定义调用的地址url
            String url = "https://api.mch.weixin.qq.com/pay/closeorder";

            //封装成一个map类型的参数
            Map<String,String> map = new HashMap<>();
            map.put("appid",appId);
            map.put("mch_id",partner);
            map.put("nonce_str", WXPayUtil.generateNonceStr());
            map.put("out_trade_no",orderId);
            //将请求的参数转换为xml格式的数据,同时生成签名
            String param = WXPayUtil.generateSignedXml(map, partnerkey);

            //将map类型的参数转换成xml格式的参数
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(param);
            httpClient.post();
            //获取xml格式类型的结果
            String content = httpClient.getContent();
            //将xml格式转换成为map类型的数据
            Map<String, String> result = WXPayUtil.xmlToMap(content);

            return result;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 验证支付的结果
     *
     * @param orderId
     * @return
     */
    @Override
    public Map<String, String> checkPayStatus(String orderId) {
        try {
            //定义调用的地址url
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";

            //封装成一个map类型的参数
            Map<String,String> map = new HashMap<>();
            map.put("appid",appId);
            map.put("mch_id",partner);
            map.put("nonce_str", WXPayUtil.generateNonceStr());
            map.put("out_trade_no",orderId);
            //将请求的参数转换为xml格式的数据,同时生成签名
            String param = WXPayUtil.generateSignedXml(map, partnerkey);

            //将map类型的参数转换成xml格式的参数
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(param);
            httpClient.post();
            //获取xml格式类型的结果
            String content = httpClient.getContent();
            //将xml格式转换成为map类型的数据
            Map<String, String> result = WXPayUtil.xmlToMap(content);

            return result;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成二维码的支付地址
     *
     * @param dataMap
     * @return
     */
    @Override
    public String createPayUrl(Map<String,String> dataMap) {

        try {
            //定义调用的地址url
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";

            //封装成一个map类型的参数
            Map<String,String> map = new HashMap<>();
            map.put("appid",appId);
            map.put("mch_id",partner);
            map.put("nonce_str", WXPayUtil.generateNonceStr());
            map.put("body","畅购商城购买的商品");
            map.put("out_trade_no",dataMap.get("orderId"));
            map.put("total_fee", dataMap.get("price"));
            map.put("spbill_create_ip","127.0.0.1");
            map.put("notify_url",notifyurl);
            map.put("trade_type","NATIVE");

            //附加参数:传递什么值给微信,微信在支付完成以后,原封不动返回给我们
            Map<String,String> attach = new HashMap<>();
            attach.put("exchange", dataMap.get("exchange"));
            attach.put("queue", dataMap.get("queue"));
            String username = dataMap.get("username");
            if(!StringUtils.isEmpty(username)){
                attach.put("username", username);
            }
            String jsonString = JSONObject.toJSONString(attach);
            map.put("attach", jsonString);
            //将请求的参数转换为xml格式的数据,同时生成签名
            String param = WXPayUtil.generateSignedXml(map, partnerkey);

            //将map类型的参数转换成xml格式的参数
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(param);
            httpClient.post();
            //获取xml格式类型的结果
            String content = httpClient.getContent();
            //将xml格式转换成为map类型的数据
            Map<String, String> result = WXPayUtil.xmlToMap(content);
            //判断请求是否成功
            if(result.get("return_code").equals("SUCCESS") && result.get("result_code").equals("SUCCESS")){
                String codeUrl = result.get("code_url");

                return codeUrl;
            }
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
