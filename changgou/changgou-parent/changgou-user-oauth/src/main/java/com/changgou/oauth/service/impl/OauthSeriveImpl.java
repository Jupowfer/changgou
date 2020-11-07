package com.changgou.oauth.service.impl;

import com.changgou.oauth.service.OauthServive;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
public class OauthSeriveImpl implements OauthServive {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    /***
     * 授权认证方法
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     */
    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret) {

        ServiceInstance choose = loadBalancerClient.choose("user-auth");
        String url = choose.getUri().toString() + "/oauth/token";
        //获取服务的调用的url
//        String url= "http://localhost:9001/oauth/token";

        //定义headers
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Authorization", getHeader(clientId, clientSecret));

        //定义body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.set("grant_type","password");
        body.set("username",username);
        body.set("password",password);


        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        Map<String,String> result = exchange.getBody();

        String access_token = result.get("access_token");
        String refresh_token = result.get("refresh_token");
        String jti = result.get("jti");

        AuthToken authToken = new AuthToken();
        authToken.setAccessToken(access_token);
        authToken.setRefreshToken(refresh_token);
        authToken.setJti(jti);

        return authToken;
    }

    /**
     * 生成请求头中的参数
     * @param clientId
     * @param clientSecret
     * @return
     */
    private String getHeader(String clientId, String clientSecret){
        String head = clientId + ":" + clientSecret;
        //进行base64加密
        byte[] encode = Base64.getEncoder().encode(head.getBytes());

        return "Basic " + new String(encode);
    }
}
