package cn.itcast.core.service;

import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;

public interface OrderService {
    /**
     * 添加订单
     * @param order
     */
    public void addOrder(Order order);

    /**
     * 根据登录的用户名查找对应的支付日志
     * @param userName
     * @return
     */
    public PayLog findPayLogFromUserName(String userName);
    /**
     * 根据用户名改变数库中的订单状态 删除redis中的订单对象
     */

    public void updateStatus(String userName);
}
