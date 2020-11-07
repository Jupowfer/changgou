package com.changgou.util;

/*****
 * @Author: 黑马训练营
 * @Description: com.changgou.util
 ****/
public class UrlUtils {

    /**
     * 去掉URL中指定的参数
     */
    public static String replateUrlParameter(String url,String... names){
        for (String name : names) {
            url = url.replaceAll("(&"+name+"=([0-9\\w]+))|("+name+"=([0-9\\w]+)&)|("+name+"=([0-9\\w]+))", "");
        }
        return url;
    }

    public static void main(String[] args) {
        String s = replateUrlParameter("http://localhost:18086/search/list?keywords=%E6%89%8B%E6%9C%BA&softField=price&softRule=ASC",
                "softField",
                "softRule");
        System.out.println(s);
    }
}
