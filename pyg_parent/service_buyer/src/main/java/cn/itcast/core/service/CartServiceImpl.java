package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.util.Constans;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private ItemDao itemDao;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 将商品添加到这个人现有的购物车中
     *
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<BuyerCart> addItemToCartList(List<BuyerCart> cartList, Long itemId, Integer num) {
        //1. 根据商品SKU ID查询SKU商品信息
        Item item = itemDao.selectByPrimaryKey(itemId);
        //2. 判断商品是否存在不存在, 抛异常
        if(item==null){
            throw new RuntimeException("商品不存在!");
        }
        //3. 判断商品状态是否为1已审核, 状态不对抛异常
        if(!"1".equals(item.getStatus())){
            throw new RuntimeException("商品审核未通过!");
        }
        //4.获取商家ID
        String sellerId = item.getSellerId();
        //5.根据商家ID查询购物车列表中是否存在该商家的购物车
        BuyerCart buyerCart = findBuyerCartForSellerId(cartList, sellerId);
        //6.判断如果购物车列表中不存在该商家的购物车
        if(buyerCart==null) {
            //6.a.1 新建购物车对象
            buyerCart= new BuyerCart();
            //设置新创建的购物车的卖家id
            buyerCart.setSellerId(sellerId);
            //创建新创建的购物车的卖家名称
            buyerCart.setSellerName(item.getSeller());
            //创建购物项集合
           List<OrderItem> orderItemList=new ArrayList<>();
            //创建购物项
            OrderItem orderItem = createOrderItem(item, num);
            //将购物项添加到购物项集合中
            orderItemList.add(orderItem);
            //将购物项集合添加到购物车中
            buyerCart.setOrderItemList(orderItemList);
            //6.a.2 将新建的购物车对象添加到购物车列表
            cartList.add(buyerCart);
        }else {
            //6.b.1如果购物车列表中存在该商家的购物车(查询购物车明细列表中是否存在该商品)
            List<OrderItem> orderItemList = buyerCart.getOrderItemList();
            OrderItem orderItem = findOrderTemForItemId(orderItemList, itemId);
            //6.b.2判断购物车明细是否为空
            if(orderItem==null) {
                //6.b.3为空，新增购物车明细
                orderItem=createOrderItem(item,num);
                //将新增的购物项添加到购物项集合中
                orderItemList.add(orderItem);
            }else {
                //6.b.4不为空，在原购物车明细上添加数量，更改金额(数量等于原来有的加上现在又买的)
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(orderItem.getPrice().multiply(new BigDecimal(orderItem.getNum())));
                //6.b.5如果购物车明细中数量操作后小于等于0，则移除
                if(orderItem.getNum()<=0) {
                    orderItemList.remove(orderItem);
                }
                //6.b.6如果购物车中购物车明细列表为空,则移除
                if(orderItemList.size()<=0){
                    cartList.remove(buyerCart);
                }
            }
        }
        //7. 返回购物车列表对象
        return cartList;

    }

    /**
     *从当前购物项集合中查询是否有该商品 有的话返回该商品对象 没有的话返回null
     * @return
     */
    private OrderItem findOrderTemForItemId(List<OrderItem> orderItemList,Long itemId){
        if (orderItemList!=null){
            for (OrderItem orderItem : orderItemList) {
                if(orderItem.getItemId().equals(itemId)){
                    return orderItem;
                }
            }
        }
        return  null;
    }

    /**
     * 创建购物项对象
     * @param item  库存对象
     * @param num   购买数量
     * @return
     */
    private  OrderItem createOrderItem(Item item, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("购买数量非法!");
        }
        OrderItem orderItem = new OrderItem();
        //购买数量
        orderItem.setNum(num);
        //商品id
        orderItem.setGoodsId(item.getGoodsId());
        //库存id
        orderItem.setItemId(item.getId());
        //示例图片
        orderItem.setPicPath(item.getImage());
        //单价
        orderItem.setPrice(item.getPrice());
        //卖家id
        orderItem.setSellerId(item.getSellerId());
        //商品库存标题
        orderItem.setTitle(item.getTitle());
        //总价 = 单价 * 购买数量
        orderItem.setTotalFee(item.getPrice().multiply(new BigDecimal(num)));
        return orderItem;
    }
    /**
     * 查询此购物车集合中有没有该商家的购物车
     * 有返回该商家的购物车对象 没有返回null
     * @return
     */
    private BuyerCart findBuyerCartForSellerId(List<BuyerCart> cartList,String sellerId){
        if(cartList!=null){
            for (BuyerCart cart : cartList) {
                if(cart.getSellerId().equals(sellerId)){
                    return cart;
                }
            }
        }
        return null;

    }

    /**
     * 用户登录了 将商品添加到redis中(用户名作文key 购物车集合作为value)
     * @param cartList
     * @param userName
     */
    @Override
    public void setCartListToRedis(List<BuyerCart> cartList, String userName) {
        redisTemplate.boundHashOps(Constans.CART_LIST_REDIS).put(userName,cartList);

    }

    /**
     * 从redis中取出商品
     * @param userName
     * @return
     */
    @Override
    public List<BuyerCart> getCarListForRedis(String userName) {
        List<BuyerCart> cartList=(List<BuyerCart>) redisTemplate.boundHashOps(Constans.CART_LIST_REDIS).get(userName);
        if(cartList==null){
            cartList=new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 合并cookie中的商品和到redis中
     * @param cookieList
     * @param redisList
     * @return
     */
    @Override
    public List<BuyerCart> mergeCookieListToRedisList(List<BuyerCart> cookieList, List<BuyerCart> redisList) {
        if(cookieList!=null){
            //遍历cookie中的购物车集合
            for (BuyerCart cookieCart : cookieList) {
                //遍历购物车中的购物项集合
                for (OrderItem cookieOrderItem : cookieCart.getOrderItemList()) {
                    //将cookie中的购物项加入到redis中的购物车集合中
                    redisList=addItemToCartList(redisList,cookieOrderItem.getItemId(),cookieOrderItem.getNum());
                }
            }
        }

        return redisList;
    }

    /**
     * 删除购物车里的购物项
     * @param ids
     */
    @Override
    public void dele(Long[] ids,String userName) {
        if(ids!=null){
            //获取到商品对象的id
            for (Long id : ids) {
                List<BuyerCart> cartList =(List<BuyerCart>) redisTemplate.boundHashOps(Constans.CART_LIST_REDIS).get(userName);
                for (BuyerCart buyerCart : cartList) {
                    for (OrderItem orderItem : buyerCart.getOrderItemList()) {
                        if(orderItem.getItemId()==id){
                            redisTemplate.boundHashOps(Constans.CART_LIST_REDIS).delete(id);
                        }
                    }
                }

            }
        }
    }
}
