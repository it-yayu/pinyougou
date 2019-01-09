package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.SpecEntity;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    SpecificationDao specificationDao;

    @Autowired
    SpecificationOptionDao optionDao;
    /**
     * 分页高级查询
     * @param spec
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult findPage(Specification spec, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        //创建条件查询
        SpecificationQuery query = new SpecificationQuery();
        SpecificationQuery.Criteria criteria = query.createCriteria();
        if(spec.getSpecName()!=null&&!"".equals(spec.getSpecName())){
            criteria.andSpecNameLike("%"+spec.getSpecName()+"%");
        }
        Page<Specification> specList=(Page)specificationDao.selectByExample(query);

        return new PageResult(specList.getTotal(),specList.getResult());
    }

    /**
     * 添加规格和规格项
     * @param specEntity
     * @return
     *
     * 首先保存规格项  接着判对规格项集合是否为空 然后遍历 设置规格项集合每一项的外键  接着保存
     * (本质插进的是两张表 规格项 和 规格集合项)
     */
    @Override
    public void save(SpecEntity specEntity) {
        //添加规格项
        specificationDao.insertSelective(specEntity.getSpecification());
        //添加规格选项对象(是集合就判对是否为空)
        if(specEntity.getSpecificationOptionList()!=null){
            for (SpecificationOption specificationOption : specEntity.getSpecificationOptionList()) {
                //因为是新添加的所以没有外键  要设置外键
                specificationOption.setSpecId(specEntity.getSpecification().getId());
                //插入规格集合项表
                optionDao.insertSelective(specificationOption);
            }
        }

    }

    /**
     * 修改的回显
     * @param id
     * @return
     */
    @Override
    public SpecEntity findOne(Long id) {
        SpecEntity specEntity = new SpecEntity();
        specEntity.setSpecification(specificationDao.selectByPrimaryKey(id));
        //根据id查规格集合的对象
        SpecificationOptionQuery query = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = query.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<SpecificationOption> options = optionDao.selectByExample(query);
        specEntity.setSpecificationOptionList(options);
        return specEntity;
    }

    /**
     * 规格项根据规格主键更新 ,规格项集合根据规格项id(规格项集合的外键)进行全部删除 再添加 再跟新
     * 修改保存数据
     * @param specEntity
     * @return
     */
    @Override
    public void update(SpecEntity specEntity) {
        //根据规格项进行跟新
        specificationDao.updateByPrimaryKeySelective(specEntity.getSpecification());
        //根据规格项id删除对应的规格项集合
        SpecificationOptionQuery query = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = query.createCriteria();
        //规格项集合中外键是规格id
        criteria.andSpecIdEqualTo(specEntity.getSpecification().getId());
        //删除规格项集合
        optionDao.deleteByExample(query);
        //将新的规格项集合添加到规格项中
        if(specEntity.getSpecificationOptionList()!=null){
            for (SpecificationOption option : specEntity.getSpecificationOptionList()) {
                //设置规格项集合的外键
                option.setSpecId(specEntity.getSpecification().getId());
                //添加规格项
                optionDao.insertSelective(option);
            }
        }

    }


    /**
     * 批量删除
     * @param ids 参数是规格项的主键集合
     */

    @Override
    public void delete(Long[] ids) {
        if(ids!=null){
            for (Long id : ids) {
                //删除规格项
                specificationDao.deleteByPrimaryKey(id);
                //删除规格项集合
                SpecificationOptionQuery query = new SpecificationOptionQuery();
                SpecificationOptionQuery.Criteria criteria = query.createCriteria();
                criteria.andSpecIdEqualTo(id);
                optionDao.deleteByExample(query);
            }
        }
    }

    /**
     * select2下拉列表查询关联规格
     * @return
     */
    @Override
    public List<Map>selectOptionList() {
      return   specificationDao.selectOptionList();
    }


}
