package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import cn.itcast.core.util.PhoneFormatCheckUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.regex.PatternSyntaxException;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;

    /**
     * 获取验证码
     * @param phone
     * @return
     */
    @RequestMapping("/sendCode")
    public Result sendCode(String phone){
        try {
            if(phone==null||"".equals(phone)){
                return new Result(false,"手机号不能为空!");
            }
            if(!PhoneFormatCheckUtils.isPhoneLegal(phone)){
                return new Result(false,"手机格式不正确!");
            }
            userService.sendCode(phone);
            return new Result(true,"发送成功!");
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
            return new Result(true,"发送失败!");
        }
    }

    /**
     * 判断注册是否成功
     * @param user
     * @param smscode
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody User user,String smscode){
        try {
            Boolean isCheck = userService.checkSmsCode(user, smscode);
            if(!isCheck){
                return new Result(false,"验证码或手机号不正确");
            }
            user.setUpdated(new Date());
            user.setCreated(new Date());
            user.setStatus("Y");
            //设置哪个客户端 1为pc端
            user.setSourceType("1");

            userService.add(user);

            return new Result(true,"注册成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"注册失败!");
        }

    }
}
