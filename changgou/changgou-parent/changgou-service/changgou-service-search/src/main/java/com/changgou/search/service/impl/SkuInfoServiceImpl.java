package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuInfoDao;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuInfoService;
import com.changgou.util.Result;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.annotation.ElementType;
import java.util.*;

@Service
public class SkuInfoServiceImpl implements SkuInfoService {

    @Autowired
    private SkuInfoDao skuInfoDao;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 搜索
     *
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        //定义返回的map
        Map<String, Object> resultMap = new HashMap<>();
        //条件构造器
        NativeSearchQueryBuilder builder = createBuild(searchMap);
        //关键字搜索-第一次
        Map<String, Object> skuinfo = dataSearch(builder, searchMap);

        return skuinfo;
    }

    /**
     * 对规格的数据进行去重处理和数据转换
     * @param specList
     * @return
     */
    private Map<String, Set<String>> getSpec(List<String> specList){
        Map<String, Set<String>> result = new HashMap<>();
        //进行遍历
        for (int i = 0; i < specList.size(); i++) {
            //获取每一条数据
            String specString = specList.get(i);
            //将数据转换成map类型:{"显示器":"14英寸","颜色":"紫色","硬盘类型":"1TSSD","CPU":"I3","内存大小":"64G","系统":"Mac"}
            //{"显示器":"14英寸","颜色":"紫色","硬盘类型":"1TSSD","CPU":"I3","内存大小":"64G","系统":"Windows"}
            Map<String,String> specMap = JSONObject.parseObject(specString, Map.class);
            //对map进行遍历
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                String key = entry.getKey();//规格的名字:系统
                String value = entry.getValue();//规格的值:Windows
                //获取一下这个规格是否已经有值了
                Set<String> set = result.get(key);
                //如果这个规格没有值,进行对象的初始化
                if(set == null){
                    set = new HashSet<>();
                }
                set.add(value);
                //将规格的数据进行设置
                result.put(key,set);
            }
        }
        return result;
    }


    /**
     * 规格的聚合查询
     * @param builder
     * @return
     */
    private List<String> searchSpec(NativeSearchQueryBuilder builder){
        List<String> spec = new ArrayList<>();

        //设置聚合的域,取一个别名
        builder.addAggregation(AggregationBuilders.terms("spec").field("spec.keyword").size(100000));

        //执行查询
        //执行查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);

        //获取聚合的结果
        StringTerms stringTerms = skuInfos.getAggregations().get("spec");
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            spec.add(bucket.getKeyAsString());
        }

        return spec;
    }


    /**
     * 获取聚合的结果:品牌和分类
     * @param searchMap
     * @return
     */
    private Map<String, Object> getAggregation(Map<String, String> searchMap, NativeSearchQueryBuilder builder){
        Map<String, Object> result = new HashMap<>();

        String brand = searchMap.get("brand");
        if(StringUtils.isEmpty(brand)){
            //构建品牌的聚合条件
            builder.addAggregation(AggregationBuilders.terms("brand").field("brandName").size(100000));
        }

        String category = searchMap.get("category");
        if(StringUtils.isEmpty(category)){
            //构建品牌的聚合条件
            builder.addAggregation(AggregationBuilders.terms("category").field("categoryName").size(100000));
        }

        //设置聚合的域,取一个别名
        builder.addAggregation(AggregationBuilders.terms("spec").field("spec.keyword").size(100000));

        //执行查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);

        //获取聚合的结果
        Aggregations aggregations = skuInfos.getAggregations();
        if(StringUtils.isEmpty(brand)){
            List<String> brandList = getDetail(aggregations, "brand");
            result.put("brandList",brandList);
        }

        if(StringUtils.isEmpty(category)){
            List<String> categoryList = getDetail(aggregations, "category");
            result.put("categoryList",categoryList);
        }

        //获取规格的聚合结果
        List<String> specList = getDetail(aggregations, "spec");
        Map<String, Set<String>> spec = getSpec(specList);
        result.put("specList",spec);

        return result;
    }

    /**
     * 遍历每一条数据,生成自定义的list结果
     * @param aggregations
     * @return
     */
    private List<String> getDetail(Aggregations aggregations, String name){
        List<String> list = new ArrayList<>();
        //获取聚合的结果
        StringTerms stringTerms = aggregations.get(name);
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            list.add(bucket.getKeyAsString());
        }
        return list;
    }

    /**
     * 品牌聚合查询
     * @param builder
     * @return
     */
    private List<String> searchBrand(NativeSearchQueryBuilder builder){
        List<String> brand = new ArrayList<>();

        //设置聚合查询的域,取一个别名
        builder.addAggregation(AggregationBuilders.terms("brand").field("brandName").size(100000));

        //执行查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);

        //获取聚合的结果
        StringTerms stringTerms = skuInfos.getAggregations().get("brand");
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            brand.add(bucket.getKeyAsString());
        }

        return brand;
    }

    /**
     * 分类的聚合查询
     * @param builder
     * @return
     */
    private List<String> searchCategory(NativeSearchQueryBuilder builder){

        List<String> category = new ArrayList<>();
        //设置聚合查询的域和为域取一个别名:魔法值
        builder.addAggregation(AggregationBuilders.terms("category").field("categoryName").size(10000));
        //执行查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);

        //获取聚合的结果
        StringTerms aggregation = skuInfos.getAggregations().get("category");
        //遍历数据
        for (StringTerms.Bucket bucket : aggregation.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            category.add(keyAsString);
        }
        return category;
    }

    /**
     * 执行搜索
     * @param builder
     * @return
     */
    private Map<String,Object> dataSearch(NativeSearchQueryBuilder builder, Map<String, String> searchMap){
        //定义返回的map
        Map<String, Object> resultMap = new HashMap<>();

        //设置聚合查询的条件----------------------------start
        String brand = searchMap.get("brand");
        if(StringUtils.isEmpty(brand)){
            //构建品牌的聚合条件
            builder.addAggregation(AggregationBuilders.terms("brand").field("brandName").size(100000));
        }

        String category = searchMap.get("category");
        if(StringUtils.isEmpty(category)){
            //构建品牌的聚合条件
            builder.addAggregation(AggregationBuilders.terms("category").field("categoryName").size(100000));
        }

        //设置聚合的域,取一个别名
        builder.addAggregation(AggregationBuilders.terms("spec").field("spec.keyword").size(100000));
        //设置聚合查询的条件----------------------------end

        //执行查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class, new SearchResultMapper() {
            /**
             * 自定义的结果处理逻辑
             * @param response
             * @param clazz
             * @param pageable
             * @param <T>
             * @return
             */
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {

                List<T> skuInfoList = new ArrayList<>();

                Aggregations aggregations = response.getAggregations();
                //获取命中的数据
                SearchHits hits = response.getHits();
                Iterator<SearchHit> iterator = hits.iterator();
                while (iterator.hasNext()) {
                    SearchHit next = iterator.next();//获取每一条数据
                    String sourceAsString = next.getSourceAsString();//字符串类型的数据---json
                    //转换成javabean对象,现在这个对象中的name字段是没有高亮
                    SkuInfo skuInfo = JSONObject.parseObject(sourceAsString, SkuInfo.class);
                    //获取高亮的数据
                    Text[] names = next.getHighlightFields().get("name").getFragments();
                    String name = "";
                    for (Text text : names) {
                        name += text;
                    }
                    //替换高亮的数据
                    skuInfo.setName(name);

                    skuInfoList.add((T)skuInfo);
                }

                /**
                 * 1.符合条件的结果集
                 * 2.分页的信息
                 * 3.总命中的数据数量
                 */
                return new AggregatedPageImpl<T>(skuInfoList, pageable, hits.getTotalHits(), aggregations);
            }
        });
        //获取符合条件的数据
        List<SkuInfo> content = skuInfos.getContent();
        long totalElements = skuInfos.getTotalElements(); //获取数据总数量共有多少条
        Integer size = 30;
        resultMap.put("totalElements", totalElements);
        resultMap.put("pageSize", size);
        //获取聚合的结果
        Aggregations aggregations = skuInfos.getAggregations();
        if(StringUtils.isEmpty(brand)){
            List<String> brandList = getDetail(aggregations, "brand");
            resultMap.put("brandList",brandList);
        }

        if(StringUtils.isEmpty(category)){
            List<String> categoryList = getDetail(aggregations, "category");
            resultMap.put("categoryList",categoryList);
        }

        //获取规格的聚合结果
        List<String> specList = getDetail(aggregations, "spec");
        Map<String, Set<String>> spec = getSpec(specList);
        resultMap.put("specList",spec);

        //设置返回结果
        resultMap.put("skuInfoList", content);

        return resultMap;
    }

    /**
     * 构建查询的条件
     * @param searchMap
     * @return
     */
    private NativeSearchQueryBuilder createBuild(Map<String, String> searchMap){
        //条件构造器
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        //定义高亮的对象
        HighlightBuilder.Field field =
                new HighlightBuilder.Field("name")
                                    .preTags("<font style='color:red'>")
                                    .postTags("</font>")
                                    .fragmentSize(100);//设置高亮的数据的长度


        //构建一个bool条件构造器:must must not should
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //判断查询条件是否为空
        if(searchMap != null){

            //关键字条件
            String keywords = searchMap.get("keywords");
            if(!StringUtils.isEmpty(keywords)){
                //构建查询条件
//                builder.withQuery(QueryBuilders.matchQuery("name",keywords));
                boolQueryBuilder.must(QueryBuilders.matchQuery("name", keywords));
            }

            //品牌条件
            String brand = searchMap.get("brand");
            if(!StringUtils.isEmpty(brand)){
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName", brand));
            }

            //分类条件
            String category = searchMap.get("category");
            if(!StringUtils.isEmpty(category)){
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName", category));
            }

            //组装规格的查询条件,遍历查询条件
            for (String s : searchMap.keySet()) {
                //以spec下划线开头的参数,都是规格查询的参数
                if(s.startsWith("spec_")){
                    String value = searchMap.get(s);
                    String fieldName = "specMap." + s.replace("spec_","") + ".keyword";
                    boolQueryBuilder.must(QueryBuilders.termQuery(fieldName, value));
                }
            }

            //价格查询
            String price = searchMap.get("price");
            if(!StringUtils.isEmpty(price)){
                //去除价格参数中的元 和 以上的中文字符
                price = price.replace("元","").replace("以上", "");
                //对价格区间进行切分
                String[] split = price.split("-");
                //组建条件
                boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(split[0]));
                //价格的终止条件
                if(split.length > 1){
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(split[1]));
                }
            }

            //分页查询
            Integer size = 30;//每页显示多少条数据
            Integer page = getPage(searchMap.get("pageNum"));
            builder.withPageable(PageRequest.of(page - 1, size));

            //排序实现
            String softField = searchMap.get("softField");//排序的域
            String softRule = searchMap.get("softRule");//排序的规则
            if(!StringUtils.isEmpty(softField)&& !StringUtils.isEmpty(softRule)){
                //按照指定的域进行指定的排序
                builder.withSort(SortBuilders.fieldSort(softField).order(SortOrder.valueOf(softRule)));
            }else{
                //默认的排序为按照库存进行降序
                builder.withSort(SortBuilders.fieldSort("num").order(SortOrder.DESC));
            }
            //设置高亮查询的条件
            builder.withHighlightFields(field);

            //保存bool查询条件
            builder.withQuery(boolQueryBuilder);
        }

        return builder;
    }

    /**
     * 转换页码数据
     * @param pageString
     * @return
     */
    private Integer getPage(String pageString){
        try {
            int page = Integer.parseInt(pageString);
            if(page <= 0){
                return 1;
            }
            return page;
        }catch (Exception e){
            return 1;
        }
    }

    /**
     * 将数据保存到es中去
     */
    @Override
    public void save() {
        //通过feign获取商品的数据,条件为:状态=1
        Result<List<Sku>> result = skuFeign.findByStatus("1");
        if(result.isFlag()){
            List<Sku> skuList = result.getData();
            //sku转换成json类型的字符串
            String jsonString = JSONObject.toJSONString(skuList);
            //将skulist转换成SkuinfoList:存储数据,存储的数据类型只能是skuinfo
//            List<SkuInfo> skuInfoList = JSONObject.parseObject(jsonString, List.class);
            List<SkuInfo> skuInfoList = JSONObject.parseArray(jsonString, SkuInfo.class);
            //对数据进行遍历循环
            for (SkuInfo skuInfo : skuInfoList) {
                //获取规格的数据
                String spec = skuInfo.getSpec();
                //将规格的数据转换成map对象
                Map<String,Object> map = JSONObject.parseObject(spec, Map.class);
                //设置specMap的值:方便后面的规格的查询
                skuInfo.setSpecMap(map);
            }
            //数据保存到es中去
            skuInfoDao.saveAll(skuInfoList);
        }
    }
}
