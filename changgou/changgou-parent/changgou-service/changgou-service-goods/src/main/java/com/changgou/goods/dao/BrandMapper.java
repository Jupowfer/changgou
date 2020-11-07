package com.changgou.goods.dao;
import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/****
 * @Author:itheima
 * @Description:Brand的Dao
 *****/
public interface BrandMapper extends Mapper<Brand> {

    /**
     * 根据类别id查询品牌列表
     * @param cid
     * @return
     */
    @Select("select tb.* from tb_category_brand tcb,tb_brand tb where tcb.brand_id=tb.id and tcb.category_id =#{cid}")
    List<Brand> findByCategory(Integer cid);
}
