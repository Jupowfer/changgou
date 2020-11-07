package com.changgou.filter;

import com.changgou.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class DefaultFilter implements GlobalFilter, Ordered {

    /**
     * 自定义的过滤器实现的逻辑
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //登录请求,直接放心
        String path = request.getURI().getPath();
        if(path.startsWith("/api/user/login")){
            return chain.filter(exchange);
        }

        //获取url中的令牌参数
        String token = request.getQueryParams().getFirst("Authorization");
        if(StringUtils.isEmpty(token)){
            //去header中获取token
            token = request.getHeaders().getFirst("Authorization");

            //去cookie中取token
            if(StringUtils.isEmpty(token)){
                HttpCookie httpCookie = request.getCookies().getFirst("Authorization");
                if(httpCookie == null){
                    response.setStatusCode(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
                    return response.setComplete();
                }

                token = httpCookie.getValue();

                //将token 放回请求头中去
                request.mutate().header("Authorization", "bearer " + token);

            }
        }

        //url和header中都没有对应token参数
        if(StringUtils.isEmpty(token)){
            response.setStatusCode(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
            return response.setComplete();
        }
//        try {
//            //解析jwt令牌
//            JwtUtil.parseJWT(token);
//
//            //解析令牌无误,直接放行
//            return chain.filter(exchange);
//        }catch (Exception e){
//            response.setStatusCode(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
//            return response.setComplete();
//        }
        //直接放行
        return chain.filter(exchange);
    }

    /**
     * 过滤器执行的顺序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
