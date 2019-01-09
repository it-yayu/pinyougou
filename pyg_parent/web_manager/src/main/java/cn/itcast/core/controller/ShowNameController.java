package cn.itcast.core.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class ShowNameController {

    /**
     * 显示用户名
     * @return
     */
    @RequestMapping("/showName")
    public Map showName(){
        //用springsecurity安全框架获取登录成功后的用户名
        String name= SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String,String> map = new HashMap<>();
        map.put("username",name);
        return map;
    }
}
