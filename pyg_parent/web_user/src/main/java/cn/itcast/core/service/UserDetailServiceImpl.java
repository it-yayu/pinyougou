package cn.itcast.core.service;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailServiceImpl implements UserDetailsService {

    /**
     * cas和springsecurity集成
     * 自定义认证类 用户能到这里说明已经通过了cas的密码认证
     * 在这里需要给用户赋予访问权限
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //创建权限集合
        List<GrantedAuthority> authorityList=new ArrayList();
        //往集合中添加权限
        authorityList.add(new SimpleGrantedAuthority("ROLE_USER"));

        return  new User(username,"",authorityList);
    }
}
