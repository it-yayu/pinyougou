package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.SpecEntity;
import cn.itcast.core.pojo.specification.Specification;

import java.util.List;
import java.util.Map;

public interface SpecificationService {
    //分页高级查询
    public PageResult findPage(Specification spec,Integer page,Integer rows);
    //添加规格和规格项
    public void save(SpecEntity specEntity);
    //修改的回显
    public SpecEntity findOne(Long id);
    //修改数据保存
    public void update(SpecEntity specEntity);
    //批量删除
    public void delete(Long[] ids);
    //select2下拉列表
    List<Map> selectOptionList();
}
