package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

public interface BrandService {
    //查询所有品牌
    List<Brand> findAll();
    //分页查询所有品牌
    PageResult findPage(Integer page,Integer rows);
    //添加
    public void add(Brand brand);
    //根据id查找单个的商品
    public Brand findOne(Long id);
    //更新商品
    public void update(Brand brand);
    //批量删除操作
    public void delect(Long[] ids);
    //分页模糊查询
    public PageResult findPage(Brand brand,Integer page,Integer rows);

    //自定义 select2
    List<Map> selectOptionList();
}
