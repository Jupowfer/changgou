package com.changgou.controller;

import com.changgou.search.feign.SearchFeign;
import com.changgou.util.Page;
import com.changgou.util.Result;
import com.changgou.util.UrlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping(value = "/search")
public class SearchController {

    @Autowired
    private SearchFeign searchFeign;

    @RequestMapping(value = "/list")
    public String search(Model model,
                         @RequestParam(required = false) Map<String,String> searhMap){
        //远程调用搜索微服务
        Result<Map<String, Object>> result = searchFeign.search(searhMap);
        //判断请求处理是否成功
        if(!result.isFlag()){
            throw new RuntimeException("服务器异常,请稍后重试!");
        }
        //获取调用的数据
        Map<String, Object> data = result.getData();
        //将数据放入model对象中去,用于页面展示
        model.addAttribute("result", data);

        model.addAttribute("searhMap", searhMap);

        //获取条件查询的url
        String url = getUrl(searhMap);
        model.addAttribute("url", UrlUtils.replateUrlParameter(url,"pageNum"));

        //获取排序的url
        String surl = UrlUtils.replateUrlParameter(url, "softField", "softRule", "pageNum");
        model.addAttribute("surl", surl);

        Object totalElements = data.get("totalElements");
        Object pageSize = data.get("pageSize");
        String pageNum = searhMap.get("pageNum");
        if(StringUtils.isEmpty(pageNum)){
            pageNum = "1";
        }

        //构建分页的信息
        Page page = new Page(Long.parseLong(totalElements.toString()),
                            Integer.parseInt(pageNum),
                            Integer.parseInt(pageSize.toString()));
        model.addAttribute("page", page);

        return "search";
    }

    /**
     * 拼接url
     * @param searhMap
     * @return
     */
    private String getUrl(Map<String,String> searhMap){
        //定义初始的路径
        String url = "/search/list?";
        //参数循环
        for (Map.Entry<String, String> entry : searhMap.entrySet()) {
            url+= entry.getKey()+ "=" + entry.getValue() + "&";
        }
        return url.substring(0, url.length() - 1);
    }
}
