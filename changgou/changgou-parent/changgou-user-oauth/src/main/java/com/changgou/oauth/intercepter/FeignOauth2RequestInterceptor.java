package com.changgou.oauth.intercepter;

import com.changgou.oauth.util.JwtToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Configuration
public class FeignOauth2RequestInterceptor implements RequestInterceptor {

    /**
     * 一旦实现了RequestInterceptor这个接口以后,使用resttemplate进行远程调用前,都会调用这个实现类的apply这个方法
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {
        //生成临时令牌
        String s = "bearer " + JwtToken.adminJwt();
        template.header("Authorization", s);

        //获取所有的浏览器中请求参数,原封不动的放入resttemplate中去
        //使用RequestContextHolder工具获取request相关变量
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            //取出request
            HttpServletRequest request = attributes.getRequest();
            //获取所有头文件信息的key
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    //头文件的key
                    String name = headerNames.nextElement();
                    //头文件的value
                    String values = request.getHeader(name);
                    //将令牌数据添加到头文件中
                    template.header(name, values);
                }
            }
        }
    }
}
