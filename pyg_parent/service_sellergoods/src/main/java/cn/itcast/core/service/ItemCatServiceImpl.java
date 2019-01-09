package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import cn.itcast.core.util.Constans;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {
    @Autowired
    ItemCatDao itemCatDao;
    @Autowired
    RedisTemplate redisTemplate;
    /**
     * 分类管理的数据展示
     * @param parentId
     * @return
     */

    @Override
    public List<ItemCat> findByParentId(Long parentId) {
        /**
         * redis缓存数据
         */
        //查询所有数据
        List<ItemCat> itemCatAll = itemCatDao.selectByExample(null);
        for (ItemCat itemCat : itemCatAll) {
            //根据分类名称作为key  模板id作为value  缓存到redis
            redisTemplate.boundHashOps(Constans.CATEGORY_LIST_REDIS).put(itemCat.getName(),itemCat.getTypeId());
        }
        //根据父级id 查询子级id
        ItemCatQuery qurey = new ItemCatQuery();
        ItemCatQuery.Criteria criteria = qurey.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        List<ItemCat> itemCats = itemCatDao.selectByExample(qurey);
        return itemCats;
    }

    /**
     * 添加分类管理数据
     * @param itemCat
     */
    @Override
    public void add(ItemCat itemCat) {

    }

    /**
     * 五级联动查找数据对象
     * @param id
     */
    @Override
    public ItemCat findOne(Long id) {
      return   itemCatDao.selectByPrimaryKey(id);
    }

    /**
     * 查询一二三级分类
     * @return
     */
    @Override
    public List<ItemCat> findAll() {
        //查询全部
        return itemCatDao.selectByExample(null);
    }
}
