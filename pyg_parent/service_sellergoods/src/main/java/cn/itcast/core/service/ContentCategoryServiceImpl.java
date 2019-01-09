package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentCategoryDao;
import cn.itcast.core.pojo.ad.ContentCategory;
import cn.itcast.core.pojo.ad.ContentCategoryQuery;
import cn.itcast.core.pojo.entity.PageResult;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContentCategoryServiceImpl implements ContentCategoryService {
    @Autowired
    ContentCategoryDao categoryDao;


    @Override
    public void add(ContentCategory contentCategory) {
        categoryDao.insertSelective(contentCategory);
    }

    @Override
    public ContentCategory findOne(Long id) {
        return categoryDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(ContentCategory contentCategory) {
        categoryDao.updateByPrimaryKeySelective(contentCategory);
    }

    @Override
    public void delect(Long[] ids) {
        if(ids!=null){
            for (Long id : ids) {
                categoryDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public PageResult findPage(ContentCategory contentCategory, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        ContentCategoryQuery query = new ContentCategoryQuery();
        ContentCategoryQuery.Criteria criteria = query.createCriteria();
        if(contentCategory!=null){
            if(contentCategory.getName()!=null&&!"".equals(contentCategory.getName())){
                criteria.andNameLike("%"+contentCategory.getName()+"%");
            }
        }
        Page<ContentCategory> pagelist=(Page<ContentCategory>) categoryDao.selectByExample(query);
        return new PageResult(pagelist.getTotal(),pagelist.getResult());
    }

    @Override
    public List<ContentCategory> findAll() {
        return categoryDao.selectByExample(null);
    }
}
