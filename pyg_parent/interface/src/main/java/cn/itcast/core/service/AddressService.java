package cn.itcast.core.service;

import cn.itcast.core.pojo.address.Address;

import java.util.List;

public interface AddressService {
    /**
     * 根据登录的用户查找其所有的地址(有一个是默认的)
     * @param userName
     * @return
     */
    public List<Address> findByUserName(String userName);
}
