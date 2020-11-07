package com.changgou.listener;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.content.feign.ContentFeign;
import com.changgou.content.pojo.Content;
import com.changgou.util.Page;
import com.changgou.util.Result;
import com.xpand.starter.canal.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * canal微服务监听数据事件的类
 */
@CanalEventListener//开启canal事件的监听
public class CanalDataEventListener {

    @Autowired
    private ContentFeign contentFeign;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 新增事件
     */
    @InsertListenPoint
    public void insertEvent(CanalEntry.RowData rowData){
        //获取新增后的数据
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : afterColumnsList) {
            //获取列名字为category_id的列
            if(column.getName().equals("category_id")){
                //根据类别的id更新广告列表
                query(Long.parseLong(column.getValue()));
            }
        }
    }

    /**
     * 修改事件
     */
    @UpdateListenPoint
    public void updateEvent(CanalEntry.RowData rowData){

        //获取修改前的数据
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        for (CanalEntry.Column column : beforeColumnsList) {
            //获取列名字为category_id的列
            if(column.getName().equals("category_id")){
                //根据类别的id更新广告列表
                query(Long.parseLong(column.getValue()));
            }
        }

        //获取修改后的数据
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : afterColumnsList) {
            //获取列名字为category_id的列
            if(column.getName().equals("category_id")){
                //根据类别的id更新广告列表
                query(Long.parseLong(column.getValue()));
            }
        }
    }

    /**
     * 删除事件
     */
    @DeleteListenPoint
    public void deleteEvent(CanalEntry.RowData rowData){
        //获取修改前的数据
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        for (CanalEntry.Column column : beforeColumnsList) {
            //获取列名字为category_id的列
            if(column.getName().equals("category_id")){
                //根据类别的id更新广告列表
                query(Long.parseLong(column.getValue()));
            }
        }
    }

    /**
     * 远程调用查询最新的广告列表,并更新redis中的数据
     * @param cid
     */
    private void query(Long cid){
        //远程调用查询广告列表
        Result<List<Content>> result = contentFeign.findByCategory(cid);
        if(result.isFlag()){
            List<Content> contents = result.getData();
            //更新到redis中去
            stringRedisTemplate.boundValueOps("content_" + cid).set(JSONObject.toJSONString(contents));
        }
    }


//    /**
//     * 自定义事件
//     */
//    @ListenPoint(destination = "example",
//                 schema = "changgou_content",
//                 table = "tb_content",
//                 eventType = {CanalEntry.EventType.UPDATE,
//                         CanalEntry.EventType.DELETE,
//                         CanalEntry.EventType.INSERT})
//    public void defaultEvent(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
//        //获取修改前的数据
//        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
//
//        //新增事件
//        if(beforeColumnsList == null){
//            System.out.println("新增的情况");
//        }
//        //获取修改后的数据
//        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
//        if(afterColumnsList == null){
//            System.out.println("删除的情况");
//        }
//    }


}
