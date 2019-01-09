package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.template.TypeTemplate;

import java.util.List;
import java.util.Map;

public interface TemplateService {
    //模板的高级模糊查询
    public PageResult search(TypeTemplate typeTemplate,Integer page,Integer rows);
    //新建模板管理
    public void  add(TypeTemplate typeTemplate);
    //修改数据回显
    public TypeTemplate findOne(Long id);
    //修改数据baocun
    public void update(TypeTemplate typeTemplate);
    //批量删除
    public void  delete(Long[] ids);
    //规格展示数据
    public List<Map> findBySpecList(Long id);
}
