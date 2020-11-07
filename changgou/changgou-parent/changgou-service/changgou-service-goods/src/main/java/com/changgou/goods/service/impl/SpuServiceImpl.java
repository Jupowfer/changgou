package com.changgou.goods.service.impl;

import com.changgou.goods.dao.SpuMapper;
import com.changgou.goods.pojo.Spu;
import com.changgou.goods.service.SpuService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;

/****
 * @Author:itheima
 * @Description:Spu业务层接口实现类
 *****/
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    /***
     * 商品批量上架
     * @param ids
     */
    @Override
    public void put(String[] ids) {
//        //方案一的批量更新:多次执行sql效率低
//        for (String id : ids) {
//            //查询spu的信息
//            Spu spu = spuMapper.selectByPrimaryKey(id);
//
//            //判断商品是否被删除了
//            if(spu.getIsDelete().equals("1")){
//                throw new RuntimeException("已经被删除的商品无法被上架");
//            }
//
//            //需要审核通过的商品
//            if(!spu.getStatus().equals("1")){
//                throw new RuntimeException("审核未通过的商品无法被上架");
//            }
//            //将商品设置为上架
//            spu.setIsMarketable("1");
//            //更新数据
//            spuMapper.updateByPrimaryKeySelective(spu);
//        }
        //方案二的批量更新:一次执行sql效率高
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //构建条件
        criteria.andEqualTo("status", "1");
        criteria.andEqualTo("isDelete", "0");
        criteria.andIn("id", Arrays.asList(ids));

        //将商品设置为上架
        Spu spu = new Spu();
        spu.setIsMarketable("1");
        //更新数据
        spuMapper.updateByExample(spu, example);
    }

    /***
     * 商品下架
     * @param spuId
     */
    @Override
    public void pull(String spuId) {
        //查询spu的信息
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //判断商品是否被删除了
        if(spu.getIsDelete().equals("1")){
            throw new RuntimeException("已经被删除的商品无法被下架");
        }
        //下架商品，需要校验是否是被删除的商品，如果未删除则修改上架状态为0
        spu.setIsMarketable("0");
        //更新数据
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /***
     * 商品上架
     * @param spuId
     */
    @Override
    public void put(String spuId) {
        //查询spu的信息
        Spu spu = spuMapper.selectByPrimaryKey(spuId);

        //判断商品是否被删除了
        if(spu.getIsDelete().equals("1")){
            throw new RuntimeException("已经被删除的商品无法被上架");
        }

        //需要审核通过的商品
        if(!spu.getStatus().equals("1")){
            throw new RuntimeException("审核未通过的商品无法被上架");
        }
        //将商品设置为上架
        spu.setIsMarketable("1");
        //更新数据
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /***
     * 商品审核
     * @param spuId
     */
    @Override
    public void audit(String spuId) {
        //查询spu的信息
        Spu spu = spuMapper.selectByPrimaryKey(spuId);

        //判断商品是否被删除了
        if(spu.getIsDelete().equals("1")){
            throw new RuntimeException("已经被删除的商品无法被审核通过");
        }
        //审核状态修改为已审核
        spu.setStatus("1");
        //上架的状态修改为已上架
        spu.setIsMarketable("1");
        //更新数据
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * Spu条件+分页查询
     * @param spu 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Spu> findPage(Spu spu, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(spu);
        //执行搜索
        return new PageInfo<Spu>(spuMapper.selectByExample(example));
    }

    /**
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Spu> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<Spu>(spuMapper.selectAll());
    }

    /**
     * Spu条件查询
     * @param spu
     * @return
     */
    @Override
    public List<Spu> findList(Spu spu){
        //构建查询条件
        Example example = createExample(spu);
        //根据构建的条件查询数据
        return spuMapper.selectByExample(example);
    }


    /**
     * Spu构建查询对象
     * @param spu
     * @return
     */
    public Example createExample(Spu spu){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(spu!=null){
            // 主键
            if(!StringUtils.isEmpty(spu.getId())){
                    criteria.andEqualTo("id",spu.getId());
            }
            // 货号
            if(!StringUtils.isEmpty(spu.getSn())){
                    criteria.andEqualTo("sn",spu.getSn());
            }
            // SPU名
            if(!StringUtils.isEmpty(spu.getName())){
                    criteria.andLike("name","%"+spu.getName()+"%");
            }
            // 副标题
            if(!StringUtils.isEmpty(spu.getCaption())){
                    criteria.andEqualTo("caption",spu.getCaption());
            }
            // 品牌ID
            if(!StringUtils.isEmpty(spu.getBrandId())){
                    criteria.andEqualTo("brandId",spu.getBrandId());
            }
            // 一级分类
            if(!StringUtils.isEmpty(spu.getCategory1Id())){
                    criteria.andEqualTo("category1Id",spu.getCategory1Id());
            }
            // 二级分类
            if(!StringUtils.isEmpty(spu.getCategory2Id())){
                    criteria.andEqualTo("category2Id",spu.getCategory2Id());
            }
            // 三级分类
            if(!StringUtils.isEmpty(spu.getCategory3Id())){
                    criteria.andEqualTo("category3Id",spu.getCategory3Id());
            }
            // 模板ID
            if(!StringUtils.isEmpty(spu.getTemplateId())){
                    criteria.andEqualTo("templateId",spu.getTemplateId());
            }
            // 运费模板id
            if(!StringUtils.isEmpty(spu.getFreightId())){
                    criteria.andEqualTo("freightId",spu.getFreightId());
            }
            // 图片
            if(!StringUtils.isEmpty(spu.getImage())){
                    criteria.andEqualTo("image",spu.getImage());
            }
            // 图片列表
            if(!StringUtils.isEmpty(spu.getImages())){
                    criteria.andEqualTo("images",spu.getImages());
            }
            // 售后服务
            if(!StringUtils.isEmpty(spu.getSaleService())){
                    criteria.andEqualTo("saleService",spu.getSaleService());
            }
            // 介绍
            if(!StringUtils.isEmpty(spu.getIntroduction())){
                    criteria.andEqualTo("introduction",spu.getIntroduction());
            }
            // 规格列表
            if(!StringUtils.isEmpty(spu.getSpecItems())){
                    criteria.andEqualTo("specItems",spu.getSpecItems());
            }
            // 参数列表
            if(!StringUtils.isEmpty(spu.getParaItems())){
                    criteria.andEqualTo("paraItems",spu.getParaItems());
            }
            // 销量
            if(!StringUtils.isEmpty(spu.getSaleNum())){
                    criteria.andEqualTo("saleNum",spu.getSaleNum());
            }
            // 评论数
            if(!StringUtils.isEmpty(spu.getCommentNum())){
                    criteria.andEqualTo("commentNum",spu.getCommentNum());
            }
            // 是否上架,0已下架，1已上架
            if(!StringUtils.isEmpty(spu.getIsMarketable())){
                    criteria.andEqualTo("isMarketable",spu.getIsMarketable());
            }
            // 是否启用规格
            if(!StringUtils.isEmpty(spu.getIsEnableSpec())){
                    criteria.andEqualTo("isEnableSpec",spu.getIsEnableSpec());
            }
            // 是否删除,0:未删除，1：已删除
            if(!StringUtils.isEmpty(spu.getIsDelete())){
                    criteria.andEqualTo("isDelete",spu.getIsDelete());
            }
            // 审核状态，0：未审核，1：已审核，2：审核不通过
            if(!StringUtils.isEmpty(spu.getStatus())){
                    criteria.andEqualTo("status",spu.getStatus());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Spu
     * @param spu
     */
    @Override
    public void update(Spu spu){
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 增加Spu
     * @param spu
     */
    @Override
    public void add(Spu spu){
        spuMapper.insert(spu);
    }

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
    @Override
    public Spu findById(String id){
        return  spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Spu全部数据
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }
}
