package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.entity.PageResult;

import java.util.List;

public interface ContentService {
    //添加
    public void add(Content Content);
    //根据id查找单个的商品
    public Content findOne(Long id);
    //更新商品
    public void update(Content Content);
    //批量删除操作
    public void delect(Long[] ids);
    //分页模糊查询
    public PageResult findPage(Content Content, Integer page, Integer rows);
    public List<Content> findAll();

    //大广告展示
    public List<Content> findByCategoryId(Long categoryId);
    //大广告展示从redis中
    public List<Content>fingByCategoryIdFromRedis(Long categoryId);
}
