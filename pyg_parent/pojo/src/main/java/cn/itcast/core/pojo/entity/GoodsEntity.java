package cn.itcast.core.pojo.entity;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;

import java.io.Serializable;
import java.util.List;


/**
 * 自定义实体类封装
 * 商品对象  商品描述对象   商品库存对象
 *
 */
public class GoodsEntity implements Serializable {
 //商品对象
private Goods goods;
//商品描述对象
private GoodsDesc goodsDesc;
//商品库存集合对象
private List<Item>  itemList;

    public Goods getGoods() {
        return goods;
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
    }

    public GoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(GoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }
}
