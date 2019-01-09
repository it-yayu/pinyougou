package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SolerManagerServiceImpl implements SolrManagerService {
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public void saveItemToSolr(Long id) {
        //创建查询条件
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(query);
        if(itemList!=null){
            for (Item item : itemList) {
                //获取json格式字符串 转换为map添加到solr中
                String specStr = item.getSpec();
                Map map = JSON.parseObject(specStr, Map.class);
                item.setSpecMap(map);
            }
            //添加到solr
            solrTemplate.saveBeans(itemList);
            //提交
            solrTemplate.commit();
        }
    }

    /**
     * 根据商品id删除solr中的数据
     *
     * @param id
     */
    @Override
    public void deleteItemFromSolr(Long id) {
        //创建删除条件
        Query query = new SimpleQuery();
        //创建删除条件对象
        Criteria criteria=new Criteria("item_goodsid").is(id);
        query.addCriteria(criteria);
        //删除solr中的数据
        solrTemplate.delete(query);
        //提交数据
        solrTemplate.commit();
    }
}
