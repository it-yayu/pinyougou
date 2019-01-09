package cn.itcast.core.service;

import java.util.Map;

public interface SearchService {
    //输入关键字查询与之对应的内容
    public Map<String,Object> search(Map paramMap);
}
