package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.BuyerCart;

import java.util.List;

public interface CartService {
    /**
     * 将商品添加到这个人现有的购物车中
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    public List<BuyerCart> addItemToCartList(List<BuyerCart> cartList,Long itemId,Integer num);

    /**
     * 用户已登录 添加商品到redis中
     * @param cartList
     * @param userName
     */
    public void setCartListToRedis(List<BuyerCart> cartList,String userName);

    /**
     * 从redis中获取购物车对象
     * @param userName
     * @return
     */
    public List<BuyerCart> getCarListForRedis(String userName);

    /**
     * 合并cookieList和redisList
     * 将cookie里面存的商品合并到redis中
     * @param cookieList
     * @param redisList
     * @return
     */
    public List<BuyerCart> mergeCookieListToRedisList(List<BuyerCart> cookieList,List<BuyerCart> redisList );


    /**
     * 删除购物车数据
     */
    public void dele(Long[] ids,String userName);
}
