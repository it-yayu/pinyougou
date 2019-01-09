package cn.itcast.core.service;

import java.util.Map;

public interface PayService {
    /**
     * 根据订单id和总价格生成二维码
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    public Map createNative(String out_trade_no,String total_fee);


    /**
     * 根据订单号调用方法判断是否支付成功
     */
    public Map queryPayStatus(String out_trade_no);


}
