package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Goods;

public interface GoodsService {
    //sku信息保存
    public void add(GoodsEntity goodsEntity);
    //高级分页查询
    public PageResult findByPage(Goods goods,Integer page,Integer rows);
    //修改数据回显
    public GoodsEntity findOne(Long id);
    //修改数据后的数据保存
    public void update(GoodsEntity goodsEntity);
    //批量删除
    public void delete(Long id);
    //根据商品id修改商品的审核状态
    public void updateStatus(Long id, String status);
}
