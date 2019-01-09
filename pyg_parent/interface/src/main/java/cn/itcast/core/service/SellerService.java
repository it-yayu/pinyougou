package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;
import org.apache.ibatis.annotations.Result;

public interface SellerService {
    //注册添加
    public void add(Seller seller);
    //高级分页模糊查询
    public PageResult search( Seller seller, Integer page, Integer rows);
    //显示详情
    public Seller findOne(String id);
    //更改状态
    public void updateStatus(String sellerId, String status);
}
