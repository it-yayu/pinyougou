package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.util.Constans;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class ContentServiceImpl implements ContentService {
    @Autowired
    ContentDao contentDao;
    //注入redis
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public void add(Content Content) {
        //将广告添加到数据库
        contentDao.insertSelective(Content);
        //根据商品id到数据库中删除商品分类所对应的商品集合
        redisTemplate.boundHashOps(Constans.CONTENT_LIST_REDIS).delete(Content.getCategoryId());
    }

    @Override
    public Content findOne(Long id) {
        return contentDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(Content Content) {
        //根据商品id在数据库中查找旧商品对象
      Content oldContent= contentDao.selectByPrimaryKey(Content.getId());
        //将新的数据更新到数据库
        contentDao.updateByPrimaryKeySelective(Content);
        //根据旧商品对象中的的分类id删除redis中的旧商品集合
        redisTemplate.boundHashOps(Constans.CONTENT_LIST_REDIS).delete(oldContent.getCategoryId());
        //根据新的商品对象的分类id删除redis中的新的商品集合商品集合
        redisTemplate.boundHashOps(Constans.CONTENT_LIST_REDIS).delete(Content.getCategoryId());
    }

    @Override
    public void delect(Long[] ids) {
        if(ids!=null){
            //此id是广告id
            for (Long id : ids) {
                //根据id到数据库中查找商品对象(先删除数据库的话redis中的数据就没发更新 就变成了垃圾数据)
                Content content = contentDao.selectByPrimaryKey(id);
                //根据商品对象找到分类id,根据分类id删除redis中的分类id所对应的商品集合
                redisTemplate.boundHashOps(Constans.CONTENT_LIST_REDIS).delete(content.getCategoryId());
                //根据广告id删除数据库所对应的商品对象
                contentDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public PageResult findPage(Content Content, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        ContentQuery query = new ContentQuery();
        ContentQuery.Criteria criteria = query.createCriteria();
        if(Content!=null){
            if(Content.getTitle()!=null&&!"".equals(Content.getTitle())){
                criteria.andTitleLike("%"+Content.getTitle()+"%");
            }
        }
        Page<Content> pagelist=(Page<Content>) contentDao.selectByExample(query);
        return new PageResult(pagelist.getTotal(),pagelist.getResult());
    }

    @Override
    public List<Content> findAll() {
        return contentDao.selectByExample(null);
    }


    /**
     * 大广告展示
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<Content> findByCategoryId(Long categoryId) {
        ContentQuery query = new ContentQuery();
        ContentQuery.Criteria criteria = query.createCriteria();
        criteria.andCategoryIdEqualTo(categoryId);
        return contentDao.selectByExample(query);
    }


    /**
     * 从redis中展示大广告
     *
     * reids 数据库中的key非常宝贵 能少用尽量少用  所以这里用hash存储
     * key  feild   value
     *      分类id    分类id 所对应的分类集合
     * @param categoryId
     * @return
     */
    @Override
    public List<Content> fingByCategoryIdFromRedis(Long categoryId) {
        //首先根据商品id去redis中获取数据
      List<Content> contentList= (List<Content>) redisTemplate.boundHashOps(Constans.CONTENT_LIST_REDIS).get(categoryId);
        //如果redis中没有数据则到数据库中去找
        if(contentList==null){
           contentList= findByCategoryId(categoryId);
            //找到数据后给redis一份
            redisTemplate.boundHashOps(Constans.CONTENT_LIST_REDIS).put(categoryId,contentList);
        }

        return contentList;
    }
}
