package com.changgou.controller;

import com.alibaba.fastjson.JSONObject;
import com.changgou.service.PayService;
import com.changgou.util.Result;
import com.changgou.util.StatusCode;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/weixin/pay")
public class PayController {

    @Autowired
    private PayService payService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 微信支付的回调地址
     * @return
     */
    @RequestMapping(value = "/notify/url")
    public String notifyurl (HttpServletRequest request){
        try {
            //获取支付结果回调的输入流
            ServletInputStream inputStream = request.getInputStream();
            //定义一个输出流
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            //关闭资源
            inputStream.close();
            os.close();
            //将字节码转换为字符串,字符串的类型为XML类型的字符串
            String xmlResult = new String(os.toByteArray());
            //将xml类型的数据转换为map类型的数据
            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
            //输出
            System.out.println(map);
            String jsonString = map.get("attach");
            Map<String,String> attach = JSONObject.parseObject(jsonString, Map.class);
            rabbitTemplate.convertAndSend(attach.get("exchange"), attach.get("queue"), JSONObject.toJSONString(map));
            //响应微信服务,收到了支付的结果
            Map<String, String> returnMap = new HashMap<>();
            returnMap.put("return_code","SUCCESS");
            returnMap.put("return_msg","OK");

            return WXPayUtil.mapToXml(returnMap);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成支付的二维码地址
     * @param dataMap
     * @return
     */
    @GetMapping(value = "/create/native")
    public Result<String> createUrl(@RequestParam Map<String,String> dataMap){
        String payUrl = payService.createPayUrl(dataMap);

        return new Result<>(true, StatusCode.OK, "获取二维码地址成功", payUrl);
    }

    /**
     * 验证支付的结果
     * @param orderId
     * @return
     */
    @GetMapping(value = "/status/query")
    public Result<Map<String,String>> checkResult(String orderId){
        Map<String, String> map = payService.checkPayStatus(orderId);

        return new Result<>(true, StatusCode.OK, "获取支付结果成功", map);
    }

    /**
     * 关闭支付
     * @param id
     * @return
     */
    @GetMapping(value = "/close/order/{id}")
    public Result<Map<String,String>> closeOrder(@PathVariable(value = "id") String id){
        Map<String, String> map = payService.closeOrder(id);
        return new Result<>(true, StatusCode.OK, "关闭支付成功", map);
    }
}
