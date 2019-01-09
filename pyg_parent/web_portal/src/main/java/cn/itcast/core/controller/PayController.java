package cn.itcast.core.controller;


import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.service.OrderService;
import cn.itcast.core.service.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private OrderService orderService;

    @Reference
    private PayService payService;


    /**
     * 获取当前登录的用户名 根据用户名获取redis中订单日志对象,根据订单日志对象获取支付单号和总金额
     * 调用微信自动生成二维码
     *
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        //获取当前用户名
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        //根据当前用户名到redis中查找对应的订单日志对象
        PayLog payLog = orderService.findPayLogFromUserName(userName);
        if (payLog != null) {
            //调用统一下单接口生成支付链接                    (数据写死方便测试)
            Map map = payService.createNative(payLog.getOutTradeNo(), "1");//String.valueOf(payLog.getTotalFee())
            return map;

        }
        return new HashMap();
    }

    /**
     * 根据订单号查询订单是否支付成功
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Result result = null;
        int flag = 1;
        while (true) {
            //判断订单号是否为空 为空直接返回false
            if(out_trade_no==null){
                result=new Result(false,"二维码超时");
                break;
            }
            //调用接口判断是否支付成功
            Map map = payService.queryPayStatus(out_trade_no);
            if ("SUCCESS".equals(map.get("trade_state"))) {
                result = new Result(true, "支付成功");
                //todo 如果支付成功数据库中的订单和订单对象状态发生改变 删除redis中的订单对象
                orderService.updateStatus(userName);
                break;
            }
            try {
                Thread.sleep(3000);//间隔三秒
            } catch (Exception ex) {
                ex.printStackTrace();
            }//超过五分钟还没支付成功
            if (flag > 100) {
                //支付异常
                result = new Result(false, "二维码超时");
                //跳出循环
                break;
            }
            flag++;
        }
        return result;
    }
}
