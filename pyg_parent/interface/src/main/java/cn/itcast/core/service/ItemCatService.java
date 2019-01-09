package cn.itcast.core.service;

import cn.itcast.core.pojo.item.ItemCat;

import java.util.List;

public interface ItemCatService {
    //分类管理数据显示
    public List<ItemCat> findByParentId(Long parentId);
    //添加分类管理数据
    public void add(ItemCat itemCat);
    //五级联动查找数据对象
    public ItemCat findOne(Long id);
    //查询一级分类 二级分类 三级分类
    public List<ItemCat> findAll();
}
