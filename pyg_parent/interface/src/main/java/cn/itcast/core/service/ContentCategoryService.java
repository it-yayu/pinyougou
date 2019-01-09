package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.ContentCategory;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

public interface ContentCategoryService {
    //添加
    public void add(ContentCategory contentCategory);
    //根据id查找单个的商品
    public ContentCategory findOne(Long id);
    //更新商品
    public void update(ContentCategory contentCategory);
    //批量删除操作
    public void delect(Long[] ids);
    //分页模糊查询
    public PageResult findPage(ContentCategory contentCategory,Integer page,Integer rows);
    public List<ContentCategory> findAll();
}
