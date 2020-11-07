package com.changgou.test;

import com.changgou.util.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.io.filefilter.FalseFileFilter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Test {

    /**
     * 生成一个jwt的令牌
     *
     */
    @org.junit.Test
    public void test1(){
        //构建jwt的属性
        JwtBuilder jwtBuilder = Jwts.builder()
                .setId("1001")//设置jtw的唯一标识
                .setSubject("java99期") //设置jwt的描述
                .setIssuedAt(new Date()) //jtw的创建时间
                .signWith(SignatureAlgorithm.HS256, "itcast");

        Map<String,Object> map = new HashMap<>();
        map.put("a","颠三倒四");
        map.put("b","dsdsdsdsd");
        jwtBuilder.addClaims(map);
        //生成jwt令牌
        String compact = jwtBuilder.compact();
        System.out.println(compact);
    }

    /**
     * 解析jwt令牌
     */
    @org.junit.Test
    public void test2(){
        Object itcast = Jwts.parser()
                .setSigningKey("itcast")
                .parse("eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxMDAxIiwic3ViIjoiamF2YTk55pyfIiwiaWF0IjoxNjAzMTY1ODMyLCJhIjoi6aKg5LiJ5YCS5ZubIiwiYiI6ImRzZHNkc2RzZCJ9.4Uzo-t_qV6mUu2rhEYjCdhntPsqrsPXXJuINXFcaQMk")
                .getBody();

        System.out.println(itcast);

    }

    @org.junit.Test
    public void test3() throws Exception{
//        String s = WXPayUtil.generateNonceStr();
//        System.out.println(s);

        //将map转换成xml
        Map<String,String> map = new HashMap<>();
        map.put("a","123");
        map.put("b","321");
        map.put("c","1234567");
        String s = WXPayUtil.mapToXml(map);
        String itcast = WXPayUtil.generateSignedXml(map, "itcast");
//        System.out.println(s);
        System.out.println(itcast);

        Map<String, String> map1 = WXPayUtil.xmlToMap(itcast);
        System.out.println(map1);
    }

    @org.junit.Test
    public void test4() throws Exception{
        HttpClient httpClient = new HttpClient("https://www.baidu.com");
        httpClient.setHttps(true);
        httpClient.get();//发送请求
        String content = httpClient.getContent();
        System.out.println(content);
    }
}
