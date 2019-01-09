package cn.itcast.core.service;

import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.util.Constans;
import cn.itcast.core.util.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private PayLogDao payLogDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;

    /**
     * 添加订单
     *
     * @param order
     */
    @Override
    public void addOrder(Order order) {
        //获取当前用户
        String userName = order.getUserId();
        //获取当前用户的购物车
        List<BuyerCart> cartList=(List<BuyerCart>) redisTemplate.boundHashOps(Constans.CART_LIST_REDIS).get(userName);

        List<String> orderIdList=new ArrayList();//订单ID列表
        double total_money=0;//总金额 （元）

        //遍历购物车
        if(cartList!=null) {
            for (BuyerCart cart : cartList) {
                // 根据购物车对象保存订单数据
                long orderId = idWorker.nextId();
                System.out.println("sellerId:"+cart.getSellerId());
                Order newOrder = new Order();//创建订单对象
                newOrder.setOrderId(orderId);//设置订单id
                newOrder.setUserId(order.getUserId());//用户名
                newOrder.setPaymentType(order.getPaymentType());//支付类型
                newOrder.setStatus("1");//状态：未付款
                newOrder.setCreateTime(new Date());//订单创建日期
                newOrder.setUpdateTime(new Date());//订单更新日期
                newOrder.setReceiverAreaName(order.getReceiverAreaName());//地址
                newOrder.setReceiverMobile(order.getReceiverMobile());//手机号
                newOrder.setReceiver(order.getReceiver());//收货人
                newOrder.setSourceType(order.getSourceType());//订单来源
                newOrder.setSellerId(cart.getSellerId());//商家ID
                //从购物车中获取购物项集合
                List<OrderItem> orderItemList = cart.getOrderItemList();
                //遍历购物项集合
                double money=0;
                if(orderItemList!=null){
                    for (OrderItem orderItem : orderItemList) {
                        // 根据购物项对象保存订单详情
                        orderItem.setId(idWorker.nextId());
                        orderItem.setOrderId(orderId);
                        orderItem.setSellerId(cart.getSellerId());
                        money+=orderItem.getTotalFee().doubleValue();
                        orderItemDao.insertSelective(orderItem);
                    }
                }
                newOrder.setPayment(new BigDecimal(money));
                orderDao.insertSelective(newOrder);
                orderIdList.add(orderId+"");//添加订单列表
                total_money+=money;
            }
        }
        // 计算总价钱保存支付日志
        if("1".equals(order.getPaymentType())){//如果是微信支付
            PayLog payLog=new PayLog();
            String outTradeNo=  idWorker.nextId()+"";//支付订单号
            payLog.setOutTradeNo(outTradeNo);//支付订单号
            payLog.setCreateTime(new Date());//创建时间
            //订单号列表，逗号分隔
            String ids=orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");
            payLog.setOrderList(ids);//订单号列表，逗号分隔
            payLog.setPayType("1");//支付类型
            payLog.setTotalFee( (long)(total_money*100 ) );//总金额(分)
            payLog.setTradeState("0");//支付状态
            payLog.setUserId(order.getUserId());//用户ID
            payLogDao.insertSelective(payLog);//插入到支付日志表
            //使用当前用户作为key 支付日志作为value存入redis数据库中
            redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);//放入缓存
        }
        //根据登录的用户名删除redis中的购物车
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
    }

    /**
     * 根据用户名查找对应的支付日志
     * @param userName
     * @return
     */
    @Override
    public PayLog findPayLogFromUserName(String userName) {
      PayLog payLog=(PayLog) redisTemplate.boundHashOps("payLog").get(userName);
        return payLog;
    }
    /**
     * 根据用户名改变数据库中的订单状态 删除redis中的订单对象
     * @param userName
     */
    @Override
    public void updateStatus(String userName) {
        //根据用户名获取redis中的订单对象
        PayLog payLog=(PayLog) redisTemplate.boundHashOps("payLog").get(userName);
        //根据日志对象删除数据库中的支付状态
        payLog.setTradeState("1");
        payLog.setCreateTime(new Date());
        payLogDao.updateByPrimaryKeySelective(payLog);
        //根据订单id修改订单表的支付状态
        String orderListStr = payLog.getOrderList();
        String[] split = orderListStr.split(",");
        if(split!=null){
            for (String orderId : split) {
                Order order = new Order();
                order.setOrderId(Long.parseLong(orderId));
                order.setStatus("2");
                orderDao.updateByPrimaryKeySelective(order);
            }
        }
        //根据用户名删除redis中的订单对象
        redisTemplate.boundHashOps("payLog").delete(userName);

    }

}

