package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.service.CartService;
import cn.itcast.core.util.Constans;
import cn.itcast.core.util.CookieUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class BuyerCartController {
    @Reference
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    /**
     * 添加商品到购物车中
     *
     * @CrossOrigin 从详情页面来调用添加购物车的方法 添加成功返回响应接收不到 涉及到了跨域访问
     * 浏览器接收不到返回的请求 需要家注解来解决这个问题  让详情页面接收到添加成功的请求跳转到购物车列表页
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:8089",allowCredentials="true")
    public Result addGoodsToCartList(Long itemId,Integer num){
        try {
            //1. 获取当前登录用户名称
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println(userName);
            //2. 获取购物车列表
            List<BuyerCart> cartList = findCartList();
            //3. 将当前商品加入到购物车列表
            cartList = cartService.addItemToCartList(cartList, itemId, num);
            //4. 判断当前用户是否登录, 未登录用户名为"anonymousUser"
            if ("anonymousUser".equals(userName)) {
                //4.a.如果未登录, 则将购物车列表存入cookie中
                CookieUtil.setCookie(request, response, Constans.CART_LIST_COOKIE, JSON.toJSONString(cartList), 60 * 60 * 24 * 30, "utf-8");
            } else {
                //4.b.如果已登录, 则将购物车列表存入redis中
                cartService.setCartListToRedis(cartList,userName);
            }

            return new Result(true, "添加成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败!");
        }
    }



    /**
     * 获取购物车列表所有数据
     *不论存在cookie中还是redis中 目的就是要获取这个人的购物车列表
     * @return
     */
    @RequestMapping("/findCartList")
    public List<BuyerCart> findCartList() {
        //1. 获取当前登录用户名称
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        //2. 从cookie中获取购物车列表json格式字符串
        String cookieCartListStr = CookieUtil.getCookieValue(request, Constans.CART_LIST_COOKIE, "utf-8");
        //3. 如果购物车列表json串为空则返回"[]"
        if (cookieCartListStr == null || "".equals(cookieCartListStr)) {
            cookieCartListStr = "[]";
        }
        //4. 将购物车列表json转换为对象
        List<BuyerCart> cookieCartList = JSON.parseArray(cookieCartListStr, BuyerCart.class);
        //5. 判断用户是否登录, 未登录用户为"anonymousUser"
        if ("anonymousUser".equals(userName)) {
            //5.a. 未登录, 返回cookie中的购物车列表对象
            return cookieCartList;
        } else {
            //5.b.1.已登录, 从redis中获取购物车列表对象
            List<BuyerCart> redisCartList = cartService.getCarListForRedis(userName);
            //5.b.2.判断cookie中是否存在购物车列表
            if (cookieCartList.size() > 0) {
                //如果cookie中存在购物车列表则和redis中的购物车列表合并成一个对象
                redisCartList = cartService.mergeCookieListToRedisList(cookieCartList, redisCartList);
                //删除cookie中购物车列表
                CookieUtil.deleteCookie(request, response, Constans.CART_LIST_COOKIE);
                //将合并后的购物车列表存入redis中
                cartService.setCartListToRedis(redisCartList,userName);
            }
            //5.b.3.返回购物车列表对象
            return redisCartList;
        }
    }

    @RequestMapping("/delete")
    public Result dele(Long[] ids){
        try {
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            cartService.dele(ids,userName);
            return new Result(true,"删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败!");
        }
    }
}
