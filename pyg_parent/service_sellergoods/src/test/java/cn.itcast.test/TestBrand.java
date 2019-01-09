package cn.itcast.test;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations ={"classpath*:spring/applicationContext*.xml"} )
public class TestBrand {
    @Autowired
    private BrandDao brandDao;
    //安主键查找
    @Test
    public void findByPremeryKey(){
        Brand brand = brandDao.selectByPrimaryKey(1L);
        System.out.println(brand);
    }
    //查询品牌表的所有数据
    @Test
    public void findAll(){
        List<Brand> brandList = brandDao.selectByExample(null);
        System.out.println(brandList);
    }

    @Test
    public void findBrandByWhere(){
        //创建查询对象
        BrandQuery brandQuery = new BrandQuery();
        //设置去重 不设置默认是false
        brandQuery.setDistinct(true);
        //设置查询的字段不设置默认是查询所有
        brandQuery.setFields("id,name");
        //设置按照id的降序排序
        brandQuery.setOrderByClause("id desc");
        //创建where查询条件对象
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        //查询id等于一的
        criteria.andIdEqualTo(1L);
        //根据首字母模糊查询
        criteria.andFirstCharLike("%L%");
        //根据首字母查询
        criteria.andNameLike("%联%");
        List<Brand> brandList = brandDao.selectByExample(brandQuery);
        System.out.println(brandList);
    }
}
