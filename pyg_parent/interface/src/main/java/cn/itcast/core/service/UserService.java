package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.user.User;
import org.springframework.web.bind.annotation.RequestBody;

public interface UserService {
    /**
     * 发送验证码
     * @param phone
     */
    public void sendCode(String phone);
    //判断注册是否成功
    public Boolean checkSmsCode(User user, String smscode);
    //保存用户
    public void add(User user);
}
