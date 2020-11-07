package com.changgou.oauth.controller;

import com.changgou.oauth.service.OauthServive;
import com.changgou.oauth.util.AuthToken;
import com.changgou.oauth.util.CookieUtil;
import com.changgou.util.CookieTools;
import com.changgou.util.Result;
import com.changgou.util.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/user")
public class OauthController {

    @Value("${auth.clientId}")
    private String clientId;

    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    @Value("${auth.cookieMaxAge}")
    private Integer cookieMaxAge;

    @Autowired
    private OauthServive oauthServive;

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private HttpServletRequest request;


    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @PostMapping(value = "/login")
    public Result login(String username, String password){
        AuthToken authToken = oauthServive.login(username, password, clientId, clientSecret);

        String accessToken = authToken.getAccessToken();

        //将token存入cookie
//        CookieUtil.addCookie(response, cookieDomain, "/", "Authorization", accessToken, cookieMaxAge, false);
        CookieTools.setCookie(request, response, "Authorization", accessToken);
        CookieTools.setCookie(request, response, "cuname", username);

        return new Result(true, StatusCode.OK, "登录成功");
    }
}
