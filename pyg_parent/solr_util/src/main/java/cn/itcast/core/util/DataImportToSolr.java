package cn.itcast.core.util;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class DataImportToSolr {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private ItemDao itemDao;

    /**
     * 往solr中导入数据库数据:
     * 1.查询出所有符合条件的数据 放入集合中
     * 2.solrtemplate  保存集合
     * 3.提交
     *
     *
     */
    public void importDataToSolr(){
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        criteria.andStatusEqualTo("1");
        //查找审核通过的 数据
        List<Item> items = itemDao.selectByExample(query);

        if (items!=null){
            for (Item item : items) {
                String specJson = item.getSpec();
                Map specMap = JSON.parseObject(specJson, Map.class);
                item.setSpecMap(specMap);
            }
            //保存到solr中
            solrTemplate.saveBeans(items);
            //提交
            solrTemplate.commit();
        }
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
       DataImportToSolr bean=(DataImportToSolr) context.getBean("dataImportToSolr");
       bean.importDataToSolr();
    }
}
