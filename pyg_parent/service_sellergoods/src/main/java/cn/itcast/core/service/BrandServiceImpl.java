package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandDao brandDao;
    //查询所有品牌
    @Override
    public List<Brand> findAll() {
        List<Brand> brandList = brandDao.selectByExample(null);
        return brandList;
    }

    /**
     * 分页查询所有品牌
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult findPage(Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        Page<Brand> brandList = (Page)brandDao.selectByExample(null);
        return new PageResult(brandList.getTotal(),brandList.getResult());
    }

    /**
     * 添加商品
     *
     * @param brand
     */
    @Override
    public void add( Brand brand) {
         brandDao.insertSelective(brand);
    }

    /**
     * 根据id查找单个的商品
     * @param id
     * @return
     */

    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    /**
     * 更新商品
     * @param brand
     */
    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    /**
     * 批量删除操作
     * @param ids
     */
    @Override
    public void delect(Long[] ids) {
        if(ids!=null){
            for (Long id : ids) {
                brandDao.deleteByPrimaryKey(id);
            }
        }
    }

    /**
     * 模糊分页查询
     * @param brand
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult findPage(Brand brand, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        BrandQuery brandQuery = new BrandQuery();
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        if(brand!=null){
            if(brand.getName()!=null&&brand.getName().length()>0){
                criteria.andNameLike("%"+brand.getName()+"%");
            }
            if(brand.getFirstChar()!=null&&brand.getFirstChar().length()>0){
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }
        }
         Page brandList=(Page) brandDao.selectByExample(brandQuery);

        return new PageResult(brandList.getTotal(),brandList.getResult());
    }

    /**
     * 自定义select2 框架 查询商品
     * @return
     */
    @Override
    public List<Map>selectOptionList() {
      return   brandDao.selectOptionList();
    }

}
