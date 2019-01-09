package cn.itcast.core.service;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import cn.itcast.core.util.Constans;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TemplateServiceImpl implements TemplateService {
    @Autowired
    TypeTemplateDao templateDao;
    @Autowired
    SpecificationOptionDao optionDao;
    @Autowired
    RedisTemplate redisTemplate;
    /**
     * 高级模板分页模糊查询
     * @param typeTemplate
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult search(TypeTemplate typeTemplate, Integer page, Integer rows) {
        /**
         * redis中缓存模板所欲数据
         */
        //查询所有数据
        List<TypeTemplate> typeTemplateAll = templateDao.selectByExample(null);
        for (TypeTemplate template : typeTemplateAll) {
            //将品牌集合缓存到redis  模板id作为key 品牌集合作为value
            String brandIdstr = template.getBrandIds();
            //将品牌集合转换为集合数据
            List<Map> brandList = JSON.parseArray(brandIdstr, Map.class);
            redisTemplate.boundHashOps(Constans.BRAND_LIST_REDIS).put(template.getId(),brandList);

            //将规格集合缓存到redis  模板id作为 key 规格集合作为value
            List<Map> specList = findBySpecList(template.getId());
            redisTemplate.boundHashOps(Constans.SPEC_LIST_REDIS).put(template.getId(),specList);

        }

        PageHelper.startPage(page, rows);
        TypeTemplateQuery query = new TypeTemplateQuery();
        TypeTemplateQuery.Criteria criteria = query.createCriteria();
        //进行模糊查询
        if (typeTemplate != null) {
            if (typeTemplate.getName() != null && !"".equals(typeTemplate.getName())) {
                criteria.andNameLike("%"+typeTemplate.getName()+"%");
            }
        }
        //如果模糊查询为null就查询的是所有数据
        Page<TypeTemplate> templateList = (Page<TypeTemplate>)templateDao.selectByExample(query);
        return new PageResult(templateList.getTotal(), templateList.getResult());
    }

    /**
     * 新建模板管理
     * @param typeTemplate
     */

    @Override
    public void add(TypeTemplate typeTemplate) {
        templateDao.insertSelective(typeTemplate);
    }

    /**
     * 修改数据回显
     * @param
     * @return
     */
    @Override
    public TypeTemplate findOne(Long id) {

        return templateDao.selectByPrimaryKey(id);
    }

    /**
     * 修改数据保存
     * @param typeTemplate
     * @return
     */
    @Override
    public void update(TypeTemplate typeTemplate) {
        TypeTemplateQuery query = new TypeTemplateQuery();
        TypeTemplateQuery.Criteria criteria = query.createCriteria();
        criteria.andIdEqualTo(typeTemplate.getId());
        templateDao.deleteByExample(query);
        if(typeTemplate!=null){
            templateDao.insertSelective(typeTemplate);
        }

    }

    /**
     * 批量删除
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if(ids!=null){
            for (Long id : ids) {
                templateDao.deleteByPrimaryKey(id);
            }
        }
    }

    /**
     * 规格展示数据
     * @param id
     * @return
     */
    @Override
    public List<Map> findBySpecList(Long id) {
        //根据id查询模板
        TypeTemplate typeTemplate = templateDao.selectByPrimaryKey(id);
        //根据模板获取规格集合数据 获取到的是json数据
        String specIds = typeTemplate.getSpecIds();
        //将json数据转化为java集合类型的数据对象
        List<Map> maps = JSON.parseArray(specIds, Map.class);
        System.out.println(maps);
        //判断集合
        if(maps!=null){
            for (Map map : maps) {
                //遍历过程根据id查询规格选项的数据
                SpecificationOptionQuery query = new SpecificationOptionQuery();
                SpecificationOptionQuery.Criteria criteria = query.createCriteria();
                //根据规格id查找规格集合的数据
                Long spec_id= Long.parseLong(String.valueOf(map.get("id")));
                criteria.andSpecIdEqualTo(spec_id);
                List<SpecificationOption> optionList = optionDao.selectByExample(query);
                //将规格选项集合封装到原来的map 集合中
                map.put("options",optionList);
            }
        }
        System.out.println(maps);
        return maps;
    }
}
